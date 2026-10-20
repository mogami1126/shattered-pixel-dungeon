"""MMO planning only. Uses actual-map samples; never modifies game sources/saves."""
from pathlib import Path
import csv, math, statistics, json

REPO = Path(__file__).resolve().parents[2]
DOCS = REPO / 'docs'
samples = list(csv.DictReader((REPO.parent / '.research/initial-all-region-sample.csv').open(encoding='utf-8')))
yields = {int(r['depth']): float(r['extra_equivalent']) for r in csv.DictReader((DOCS/'MMO_LOOT_EQUIVALENT.csv').open(encoding='utf-8-sig'))}
assert len(samples) == 600
assert all(len([r for r in samples if int(r['seed']) == s]) == 20 for s in range(1, 31))

# White reference weapon damage is the unit. Native weapon differences are
# retained later as relative factors; this does not simulate every item/skill.
RANK_BASE = 128
END_GAIN = 1024
OFFENSE_SHARE = ARMOR_SHARE = .30
HP_LOCAL = [1.5, 3, 32, 256, 4096]
DAMAGE_LOCAL = [1.2, 1.5, 2.3, 8, 600]

def growth(level, budget, power=2):
    return (1 + (END_GAIN**(1/power)-1)*level/budget)**power

def stats(region, off_level, armor_level, budget, wand=False):
    rank = RANK_BASE**(region-1)
    attack = rank * growth(off_level, budget, 2.2 if wand else 2)
    armor_gain = growth(armor_level, budget, 1.8)
    return {'damage': attack, 'dr': .5*rank*armor_gain,
            'hp': 20*rank*math.sqrt(armor_gain)}

def encounter(player, region, local, magic=False):
    rank = RANK_BASE**(region-1)
    hp = rank * HP_LOCAL[local-1]
    # Boss is checked through the reference physical attack only. Full phases
    # and mandatory mechanics are outside this numeric envelope.
    raw = rank * DAMAGE_LOCAL[local-1]
    dr = hp * .02
    out = max(0, player['damage']-dr)
    hits = math.ceil(hp/out) if out else math.inf
    taken = max(0, raw-player['dr']*(.25 if magic else 1))
    survival = player['hp']/taken if taken else math.inf
    return dict(landed_hits=hits, survival_hits=survival, margin=survival/hits,
                enemy_hp=hp, enemy_damage=raw, net_received=taken)

rows=[]; summaries=[]
for region in range(1,6):
    start=5*(region-1)+1
    subset=[r for r in samples if (int(r['depth'])-1)//5+1==region]
    totals=[sum(int(r['initial_enemies'])*yields[int(r['depth'])] for r in subset if int(r['seed'])==s)+yields[region*5] for s in range(1,31)]
    counts=[sum(int(r['initial_enemies']) for r in subset if int(r['seed'])==s) for s in range(1,31)]
    mean=statistics.mean(totals); budget=OFFENSE_SHARE*mean
    white=stats(region,0,0,budget)
    assert encounter(white,region,1)['landed_hits']==2
    for scenario,amount in [('min',min(totals)),('mean',mean),('max',max(totals))]:
        p=stats(region,amount*OFFENSE_SHARE,amount*ARMOR_SHARE,budget)
        for local in [1,2,3]:
            e=encounter(p,region+1,local,magic=True)
            rows.append(dict(region=region,scenario=scenario,total_upgrade=amount,
                off_level=amount*OFFENSE_SHARE,armor_level=amount*ARMOR_SHARE,
                damage=p['damage'],dr=p['dr'],hp=p['hp'],next_local=local,**e))
    # Follow each region's mean material acquisition and reset at its entrance.
    accumulated=0;progress=[]
    for local in range(1,6):
        p=stats(region,accumulated*.30,accumulated*.30,budget)
        e=encounter(p,region,local,magic=local<5)
        progress.append(dict(local=local,spent_level=accumulated*.30,**e))
        if local<5:
            accumulated+=statistics.mean([int(r['initial_enemies']) for r in subset if int(r['depth'])==start+local-1])*yields[start+local-1]
    frozen=stats(region,budget,budget,budget)
    assert encounter(frozen,region+1,1,True)['landed_hits']==1
    assert encounter(frozen,region+1,2,True)['landed_hits']==1
    third=encounter(frozen,region+1,3,True)
    assert third['landed_hits']>=4 and third['survival_hits']<third['landed_hits']
    summaries.append(dict(region=region,mean_enemies=statistics.mean(counts),min_upgrade=min(totals),
        mean_upgrade=mean,max_upgrade=max(totals),budget=budget,
        rank=RANK_BASE**(region-1),frozen=frozen,progress=progress))

with (DOCS/'MMO_STAGE_RESET_MODEL.csv').open('w',newline='',encoding='utf-8-sig') as f:
    w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
(DOCS/'MMO_STAGE_RESET_MODEL.json').write_text(json.dumps(summaries,ensure_ascii=False,indent=2),encoding='utf-8')
(DOCS/'MMO_ALL_REGION_ENEMY_SAMPLE.csv').write_text((REPO.parent/'.research/initial-all-region-sample.csv').read_text(encoding='utf-8'),encoding='utf-8-sig')

lines=['# MMO 重置装备、逐区域成长：数值策划第一版','',
'设计模型，未修改游戏、未打包、未推送。此前无上限方案已另行存档；本版只允许武器、法杖、防具升阶。怪物公式现在允许重新设计，不再要求维持 1.5^(d−1)。','',
'## 校准口径','',
'每区入口换 +0 装备，成长等阶为区域编号减一。击杀本区全部普通初始敌人，再击杀一次 Boss。冻结发生在 Boss 死后、使用其升阶材料或转移装备之前，保留刚打赢 Boss 的装备；否则把强化清零与“冻结”混为一件事，结果不同。','',
'30 个固定种子，五区共 600 张地图，调用当前游戏对应的 Sewer/Prison/Caves/City/Halls 生成器。禁用 createItems；地图绘制中已出现的敌人计入，物品放置产生的拟态、刷新、后续召唤、蜂群分裂、任务支线、Boss 召唤物不计。不是对实际全部遭遇的模拟。','',
'收益沿用已有 MMO 额外掉落期望及材料换算，目标为三阶及以上近战武器。每只 Boss 单计一次额外掉落。仅计装备、药水、卷轴、符石、种子；不加入原生掉落、地面卷轴、金币、能量、财富和幸运加成。法杖、防具目标的早期装备材料系数不同，表中共用近战等效预算仅为初版校准代理。','',
'资源分配：30% 输出、30% 防具、40% 保留消耗品或其他用途。法杖路线替代武器的输出池，不免费复制一份资源给法杖。戒指均取无成长加成的参考值；保留原公式的戒指允许突破这个基线。','',
'## 五区材料预算','',
'| 区域 | 平均初始敌人 | 总强化量：最少 / 平均 / 最多 | 输出、防具各投入（均值） |','|---|---:|---:|---:|']
names=['下水道 1–5','监狱 6–10','洞穴 11–15','都市 16–20','大厅 21–25']
for s,n in zip(summaries,names):
    lines.append(f"| {n} | {s['mean_enemies']:.1f} | {s['min_upgrade']:.0f} / {s['mean_upgrade']:.0f} / {s['max_upgrade']:.0f} | +{s['budget']:.0f} |")
lines += ['', '## 装备函数与独立升阶','',
'令 r 为成长等阶，R=128^r；第一区 r=0。令 B_r 为上表区域平均强化预算的 30%，L 为该件装备实际强化等级。F_p(L)=[1+(1024^(1/p)−1)L/B_r]^p。','',
'武器 p=2、法杖 p=2.2、防具 p=1.8，各自无上限；L=B_r 时均为 1024。参考输出 D=R·F；参考防御 DR=0.5R·F_1.8。原生品阶、武器伤害范围与特殊属性作为相对参考物的系数保留，不能把所有不同物品都改成相同伤害。具体每件法杖的持续效果与 AOE 另按原始用途映射，尚未逐件模拟。','',
'为了让高阶白板防具在法术区域也能生存，提出 MMO 防具额外生命乘区：参考生命 H=20R·sqrt(F_1.8)，原本职业生命与根骨效果另行相乘；魔法吸收量为物理 DR 的 25%。这是新的防具效果，不是原版已有属性，尚未实现。不给武器、法杖附赠生命。','',
'普通升阶保留 L。因为 B_r 在不同等阶变化，不能仅乘 128 后忽略 F 的变化；需按完整公式重算。按本次五区预算，同等级升阶不会降低参考属性，程序另有检查。等阶转移消耗全部强化及进度，输出 +0，不能复制等阶。','',
'策划建议每个 Boss 给两份升阶材料，允许一件输出装备及一件防具晋升；也允许玩家把机会花给两件输出装备，但该路线不保证高阶防具生存目标。材料严格 0→1→2→3→4→5，不能跳级。两份数量是本版策划选择，不是此前用户已指定数量。','',
'## 敌人的区域包络','',
'以所在区域 R 为单位，五个位置对应普通第 1/2/3/4 层与 Boss。','',
'| 局部楼层 | 最强参考生命 | 参考护甲 | 参考单击伤害 |','|---|---:|---:|---:|']
for j,(h,d) in enumerate(zip(HP_LOCAL,DAMAGE_LOCAL),1):
    lines.append(f'| {j} | {h}R | {h*.02:g}R | {d}R |')
lines += ['',
'普通怪实际数值以本层最强模板为锚：H_i=H包络·(原生H_i/本层原生H最大值)，伤害_i=伤害包络·(原生伤害中点_i/本层对应攻击类型的最大中点)，DR_i=DR包络·(原生DR中点_i/本层最大DR中点)；最大DR为零时该层DR为零。保留快攻、远程、蓄力、回血、复活等身份，攻击频率不自动归一。特殊技能按对应攻击类型分别归一，不能把非伤害效果当伤害。这里先设计参考包络，未完成逐敌人、逐技能验证。第三层吃力主要由法术／穿透型攻击实现：高强化防具可能完全挡住其普通物理攻击，不能宣称每种怪都同样吃力。','',
'命中率、攻速、位移、状态效果与 Boss 阶段在这份包络表里未展开。表中“1 击”是一次命中后的击杀，不等于一定在一个操作或一个游戏回合内完成。未设置阶级免疫或强制剩余 1 HP。','',
'## 冻结结果：平均预算、无戒指加成','',
'每区结束 F=1024；下一区 R 增加 128 倍。冻结后，下一区第 1 层一击所需输出约 195.84R旧，第 2 层约 391.68R旧，均小于输出 1024R旧，一次命中击杀。第 3 层生命为 4096R旧、护甲 81.92R旧，需要 5 次命中。','',
'冻结防御 512R旧，参考生命 640R旧。第三层参考法术为 294.4R旧，扣魔法吸收 128R旧后，每次损失 166.4R旧，只能承受约 3.85 次；等速站桩会输，需要走位、控制、药水或继续成长。此处对“吃力”作明确数值定义。','',
'换新的同区域高阶白板：首层怪生命 1.5R、护甲 .03R，对白板输出 R 需 2 次命中；参考法术每击 1.075R，参考生命 20R 可承受约 18.6 次，能够开始刷怪。','',
'Boss 前已经获取四个普通层的资源，参考 Boss 血量 4096R、护甲 81.92R，输出约 1024R 时需 5 次命中；参考物理单击 600R，扣护甲约损失 88R，可承受约 7.3 次。完整 Boss 多阶段、机关、无敌和召唤尚未模拟，不能把这项基础胜负当成实战通关证明。','',
'## 方差与适用边界','',
'每个种子的材料总量不同，CSV 给出各区最低、均值、最高总量的冻结结果。在采样的材料总量低点、均值和高点下，参考输出都能一击击杀下一阶段前两层参考怪。第三层需要 3～12 次命中：低收益时明显失败，高收益时可能轻松通过，其中第四区最高材料情形仍有约 5.16 倍生存裕量。因此目前只是平均预算的第三层压力目标通过，不能保证全种子同一边界；低伤害白板、强敌快攻和真实掉落随机仍可提前遇阻，集中投入输出或戒指可冲得更远。剩余戒指保持指数成长，故不能同时承诺所有配装都“第三层必吃力”。','',
'大背包自动合并可能改变材料选择和回收效率；需要在正式打造规则实现后重新跑实际掉落模拟。当前收益为期望值，并非逐件掉落实测。','',
'第五区之后以 r=5 进行数学外推，用同一局部包络；当前游戏未实现 26 层后的无限地图。后续无限区域的 B_r 必须由对应生成、掉落预算预先确定，不能读玩家当前属性或投入动态追赶。','',
'## 戒指与安全例外','',
'戒指不能升阶。攻速、移速、命中、闪避、奥术等保留当前破碎 MMO 的原公式，不改回英烈底数，不为本阶段模型压低收益。','',
'根骨、财富（用户称幸运，若另指幸运附魔需分别处理）列入安全例外：后续可显著削弱其生命／掉落实质增长，优先提出根骨由指数生命改为无上限线性生命、财富用对数级有效掉落等级。具体底数与参数暂不写入游戏；旧无限数值方案先保留为存档。这是允许削弱的候选实现，不是本轮重新平衡全部戒指。','',
'攻速仍需要可推进的调度和等效批量结算；保留总收益不代表允许创建无限循环。大数中间运算、治疗比例、血量余量与掉落实体总量需要独立处理。','',
'复跑：在工作区运行 `.research/run_all_region_model.py` 采样，再运行 `python shattered-mmo/docs/models/stage_reset_model.py`。本脚本只写模型报告，不改游戏或存档。']

for s in summaries[:-1]:
    next_s=summaries[s['region']]
    for level in [0,s['budget'],s['budget']*100]:
        for power in [1.8,2,2.2]:
            assert RANK_BASE*growth(level,next_s['budget'],power)>growth(level,s['budget'],power)
(DOCS/'MMO_STAGE_RESET_MODEL.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('PASS: 600 maps, 5 regional mean freeze boundaries, white starts, and promotion monotonicity.')
for s in summaries:
    print(s['region'], 'mean material', round(s['mean_upgrade'],2), 'offense/armor level',round(s['budget'],2), 'progress landed hits',[p['landed_hits'] for p in s['progress']])
