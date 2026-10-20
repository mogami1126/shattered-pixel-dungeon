"""Two regions of actual initial spawns; discrete BIOCHIP duel comparisons."""
from pathlib import Path
import csv,statistics,runpy,math
REPO=Path(__file__).resolve().parents[2]
model=runpy.run_path(str(REPO/'docs/models/mmo_region_model.py'))
samples=list(csv.DictReader(open(REPO.parent/'.research/initial-two-region-sample.csv',encoding='utf-8')))
yields={int(r['depth']):float(r['extra_equivalent']) for r in csv.DictReader(open(REPO/'docs/MMO_LOOT_EQUIVALENT.csv',encoding='utf-8-sig'))}
totals=[sum(int(r['initial_enemies'])*yields[int(r['depth'])] for r in samples if int(r['seed'])==i)+yields[5]+yields[10] for i in range(1,31)]
U=statistics.mean(totals);L=round(U/5)
def rank(s,curve):
    return 16**(s-1) if curve in ['16','16_old'] else 10**((2 if curve=='power100' else 3)*(s-1)**2)
def phit(a,e):
    return 1-e/(2*a) if a>=e else a/(2*e)
def fight(name,depth,curve,weapon,armor,credit=0,mode='melee'):
    p={k:float(v) for k,v in model['player'](1,L).items()};m=model['lookup'][name]
    wr=rank(weapon,curve);ar=rank(armor,curve)
    p['damage']*=wr;p['armor']*=ar
    if curve!='16_old':
        p['accuracy']*=math.sqrt(wr);p['evasion']*=math.sqrt(ar)
    g=1.5**(depth-1);ehp=m['hp']*g;hp=p['hp']
    dr=m['armor'][0]+(m['armor'][1]-m['armor'][0])//2
    a,b=m['damage'];damage=(a+(b-a)//2)*g
    if mode=='magic':damage={'DM100':6,'Shaman':10,'Warlock':15}[name]*g
    if mode=='beam':damage=40*g
    pout=phit(p['accuracy'],m['evasion']*depth)
    pin=phit(m['accuracy']*depth*(2 if mode!='melee' else 1),p['evasion'])
    hc=credit;ec=0.;ht=0.;et=2. if mode=='beam' else 0.;erate=m['attack_rate'];hits=0
    for event in range(10000):
        if ht<et: # conservative: enemy acts first on equal timestamps
            t=ht;ht+=1/p['rate'];hc+=pout
            if hc+1e-12>=1:
                hc-=1;out=max(0,p['damage']-dr)
                if mode=='beam' and et-2<=t<et:out/=4
                ehp-=out;hits+=1
                if ehp<=0:return True,t,hits
        else:
            t=et;et+=7 if mode=='beam' else 1/erate;ec+=pin
            if ec+1e-12>=1:
                ec-=1;hp-=damage*(1-p['magic_reduction']) if mode!='melee' else max(0,damage-p['armor'])
                if hp<=0:return False,t,hits
    return False,float('inf'),hits
def roster(depth):
    if depth<=24:names=model['ROTATIONS'][depth]
    else:names=['Succubus','Eye','Scorpio'] # explicit hypothetical Halls continuation
    out=[]
    for name in names:
        m=model['lookup'][name]
        if not m['damage'] or not m['accuracy']:continue
        out.append((name,'melee'))
        if name in ['DM100','Shaman','Warlock']:out.append((name,'magic'))
        if name=='Eye':out.append((name,'beam'))
    return out
results=[]
for curve in ['16_old','16','power100','power1000']:
    for label,w,a in [('武器二阶＋防具二阶',2,2),('武器三阶＋防具一阶',3,1)]:
        for credit in [0,.5,.99]:
            failure=None
            for d in range(11,121):
                if d%5==0:continue
                failures=[(n,mode,fight(n,d,curve,w,a,credit,mode)) for n,mode in roster(d) if not fight(n,d,curve,w,a,credit,mode)[0]]
                if failures:
                    failure=(d,failures[0][0],failures[0][1]);break
            results.append(dict(curve=curve,route=label,initial_hit_credit=credit,first_failed_floor=failure[0],failed_mob=failure[1],mode=failure[2]))
with (REPO/'docs/MMO_SECOND_REGION_FREEZE.csv').open('w',newline='',encoding='utf-8-sig') as f:
    cw=csv.DictWriter(f,fieldnames=list(results[0]));cw.writeheader();cw.writerows(results)
md=['# 清完第二阶段冻结：升阶倍率比较','',
 '仅为 MMO 新成长模型候选，不是当前 APK 实战结果。普通模式不变，怪物继续采用 1.5^(d−1)；本次未修改游戏。','',
 '## 资源与有限升阶','',
 f'30 个种子，共 240 张普通层地图，1～4 与 6～9 层初始怪物数量由游戏地图生成代码抽样；5 层粘咕、10 层天狗各计一次额外掉落。共有额外材料量最少 {min(totals):.2f}，平均 {U:.2f}，最多 {max(totals):.2f}。原生掉落、地面物资和财富等加成未计。','',
 f'平均分配五个强化池约 +{L}，u=L/(L+20)={L/(L+20):.4f}，已经接近一阶极限；这再次说明半饱和等级 20 需要调整。假设每个 Boss 提供一次单件升阶，共两次，不把两份材料算成全套升阶。若每个 Boss 提供几份，结论需另算。','',
 '## 比较的升阶函数','', '| 方案 | R(s) | 二阶倍率 | 三阶倍率 |','|---|---|---:|---:|',
 '| 原候选/附加命中闪避版 | 16^(s−1) | 16 | 256 |',
 '| 平方指数 A | 10^[2(s−1)²] | 100 | 100,000,000 |',
 '| 平方指数 B | 10^[3(s−1)²] | 1,000 | 1,000,000,000,000 |','',
 '16_old 仅放大伤害/物理防御，与前一版有限材料比较一致。其他方案为落实“全方位提升”，让武器伤害乘 R、命中属性乘 sqrt(R)，防具防御乘 R、闪避属性乘 sqrt(R)。这些命中、闪避乘区是新增候选参数，不是仅替换原曲线底数的结果。英雄生命与攻速、元素减伤不因这两件装备自动增加，也没有免费升级其他戒指。','',
 '不建议直接做伤害=D^s：它会把物品的基础数值单位、强化和阶级纠缠在一起。对独立乘区 R(s) 使用平方指数，更容易保留各类别自身的等级曲线。','',
 '## 冻结后的普通怪对战边界','',
 '本次逐次攻击结算：必须打出完整一次攻击，不能靠平均 DPS 得到小于一次攻击的击杀；BIOCHIP 命中额度累积。新敌人命中额度为零，英雄初始额度测试 0、0.5、0.99。敌人同时间优先。护甲、法术与射线分别处理，不把移动速度当攻速。','',
 '| 升阶方案 | 两份材料分配 | 首次遇到无法站桩击败的普通怪楼层范围 | 障碍 |','|---|---|---:|---|']
for curve in ['16_old','16','power100','power1000']:
    for route in ['武器二阶＋防具二阶','武器三阶＋防具一阶']:
        subset=[r for r in results if r['curve']==curve and r['route']==route]
        floors=[r['first_failed_floor'] for r in subset];fail=', '.join(sorted({r['failed_mob']+'/'+r['mode'] for r in subset}))
        md.append(f'| {curve} | {route} | {min(floors)}～{max(floors)} | {fail} |')
md+=['','以上是已建模普通怪的对战潜力，不是“保证能一路通关到该层”。精英、多个敌人、暴徒狂暴、蜘蛛毒网、术士降级、魅魔魅惑、食尸鬼复活等仍可能提前造成失败。恶魔之眼计入蓄力期间伤害除以四、2 回合首次蓄力和带魔法命中的 30～50 射线；后续周期取约 7 回合估计，没有模拟移动、躲射线与完整冷却随机性。','',
 'Boss 的护盾、阶段、召唤、机关必须独立处理，不能用单次普通怪边界声称已通关 DM-300、矮人国王或 Yog。冻结时假设玩家已打赢天狗，未证明升级途中怎么达成。','',
 '## 无限模式限制','',
 '当前破碎代码第 26 层为终点，之后不是已实现的无限地牢。表中超过 25 的结果只是数学外推：假设延续恶魔大厅怪物种类，基础属性不再换种，MMO 楼层倍率继续增长；不是当前可玩的楼层。','',
 '任何固定阶级、固定强化的有限属性，都会被持续的 1.5^d 超过。平方指数升阶可以形成很大的阶段突破，并让每一次进一步升阶延长可推进距离；它不能让某次冻结后的角色永远无敌。','',
 '平方指数 B 从二阶到三阶一次跳 10^9 倍，非常极端；它是比较用上界候选，不是已确定的最终曲线。倍率再大仍需要校准升阶次数、金币、Boss 材料数量、控制效果与无限楼层内容。']
md+=['','## Boss 普通攻击的必要检查','', '下表仅检查 Boss 的血量与普通攻击，不含多阶段、无敌、机关、召唤和特殊伤害。失败说明连基础单挑都不足；成功不能证明完整战斗通关。','', '| 方案 | 二阶武器＋二阶防具 | DM-300 普通攻击模型 | 矮人国王普通攻击模型 |','|---|---|---|']
for curve in ['16_old','16','power100','power1000']:
    outcomes=[]
    for name,depth,hp,accuracy,evasion in [('DM300',15,300,20,15),('DwarfKing',20,300,26,22)]:
        model['lookup'][name]=dict(name=name,hp=hp,accuracy=accuracy,evasion=evasion,damage=[15,25],armor=[0,10],attack_rate=1)
        wins=[fight(name,depth,curve,2,2,c)[0] for c in [0,.5,.99]]
        outcomes.append('三种命中额度均失败' if not any(wins) else '基础检查均通过，机制未验证' if all(wins) else '结果依赖命中额度')
    md.append(f'| {curve} | 各消耗一次升阶 | {outcomes[0]} | {outcomes[1]} |')
(REPO/'docs/MMO_SECOND_REGION_FREEZE.md').write_text('\n'.join(md)+'\n',encoding='utf-8')
(REPO/'docs/MMO_TWO_REGION_ENEMY_SAMPLE.csv').write_text((REPO.parent/'.research/initial-two-region-sample.csv').read_text(encoding='utf-8'),encoding='utf-8-sig')
print('resource mean',U,'L',L)
for r in results:print(r)
