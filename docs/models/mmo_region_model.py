"""Read-only source audit and MMO-only balance model; standard-library Python."""
from pathlib import Path
from fractions import Fraction as F
import csv, json, re, math, hashlib

REPO = Path(__file__).resolve().parents[2]
WORK = REPO.parent
BASE = 'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/'
MOBS = BASE + 'actors/mobs/'
OUT = REPO / 'docs'
NAMES = ['Rat','Snake','Gnoll','Crab','Slime','Swarm','Skeleton','Thief','DM100','Guard',
         'Necromancer','Bat','Brute','Shaman','Spinner','DM200','Ghoul','Warlock','Monk',
         'Golem','Succubus','Eye','Scorpio']
ROTATIONS = {
 1:['Rat','Snake'],2:['Rat','Snake','Gnoll'],3:['Rat','Snake','Gnoll','Swarm','Crab'],4:['Gnoll','Swarm','Crab','Slime'],
 6:['Skeleton','Thief','Swarm'],7:['Skeleton','Thief','DM100','Guard'],8:['Skeleton','Thief','DM100','Guard','Necromancer'],9:['Skeleton','Thief','DM100','Guard','Necromancer'],
 11:['Bat','Brute','Shaman'],12:['Bat','Brute','Shaman','Spinner'],13:['Bat','Brute','Shaman','Spinner','DM200'],14:['Bat','Brute','Shaman','Spinner','DM200'],
 16:['Ghoul','Warlock'],17:['Ghoul','Warlock','Monk'],18:['Ghoul','Warlock','Monk','Golem'],19:['Warlock','Monk','Golem'],
 21:['Succubus','Eye'],22:['Succubus','Eye'],23:['Succubus','Eye','Scorpio'],24:['Succubus','Eye','Scorpio']}
# Elemental subclasses and special attacks must be audited separately, not invented here.
def method(source, name):
    m=re.search(r'(?:public|private|protected)\s+(?:int|float|void|boolean)\s+'+name+r'\([^)]*\)\s*\{',source)
    if not m:return ''
    start=end=m.end(); depth=1
    while depth and end<len(source):
        depth+=(source[end]=='{')-(source[end]=='}');end+=1
    return source[start:end-1]
def extract(source):
    hp=re.search(r'HP\s*=\s*HT\s*=\s*(\d+)\s*;',source)
    ev=re.search(r'defenseSkill\s*=\s*(\d+)\s*;',source)
    ac=re.search(r'return\s+(\d+)\s*;',method(source,'attackSkill'))
    ranges=re.findall(r'(?:range|NormalIntRange)\(\s*(\d+)\s*,\s*(\d+)\s*\)',method(source,'damageRoll'))
    dr=re.findall(r'(?:range|NormalIntRange)\(\s*(\d+)\s*,\s*(\d+)\s*\)',method(source,'drRoll'))
    exp=re.search(r'EXP\s*=\s*(\d+)\s*;',source)
    # Brute's last branch is non-enraged. Empty ranges mean a nonstandard attacker.
    return dict(hp=int(hp[1]) if hp else None,evasion=int(ev[1]) if ev else None,
                accuracy=int(ac[1]) if ac else None,damage=list(map(int,ranges[-1])) if ranges else None,
                armor=list(map(int,dr[0])) if dr else [0,0],exp=int(exp[1]) if exp else 1,
                attack_rate=2 if '*0.5f' in method(source,'attackDelay') else 1)
def hit(a,e):
    if a<=0:return F(0)
    if e<=0:return F(1)
    return 1-e/(2*a) if a>=e else a/(2*e)
def player(stage,level):
    u=F(1) if level is None else F(level,level+20)
    rank=16**(stage-1)
    return dict(damage=rank*(40+360*u),armor=rank*(20+130*u),hp=rank*(200+300*u),
                accuracy=(100+120*u)*F(stage+1,2),evasion=(80+120*u)*F(stage+1,2),
                rate=1+u/2,magic_reduction=u/2)
def encounter(mob,depth,stage,level,magic=False,overrides=None):
    p=player(stage,level)
    if overrides:p.update(overrides)
    g=F(3,2)**(depth-1)
    low,high=mob['damage']; damage=(low+(high-low)//2)*g
    armor=F(mob['armor'][0]+(mob['armor'][1]-mob['armor'][0])//2)
    if magic: damage=(6 if mob['name']=='DM100' else 10)*g
    p_out=hit(p['accuracy'],F(mob['evasion']*depth))
    p_in=hit(F(mob['accuracy']*depth*(2 if magic else 1)),p['evasion'])
    outgoing=max(F(0),p['damage']-armor)*p_out*p['rate']
    incoming=(damage*(1-p['magic_reduction']) if magic else max(F(0),damage-p['armor']))*p_in*mob['attack_rate']
    ttk=F(mob['hp'])*g/outgoing if outgoing else None
    ttd=p['hp']/incoming if incoming else None
    # Infinite TTD means no modeled incoming damage, not immunity to all real skills.
    wins=ttk is not None and (ttd is None or ttk<ttd)
    return dict(depth=depth,region=(depth-1)//5+1,mob=mob['name'],mode='magic' if magic else 'melee',stage=stage,
                level='limit' if level is None else level,hp=mob['hp']*g,damage=damage,armor=armor,
                accuracy=mob['accuracy']*depth,evasion=mob['evasion']*depth,p_out=p_out,p_in=p_in,
                player_dps=outgoing,mob_dps=incoming,ttk=ttk,ttd=ttd,expected_win=wins)
def num(v):
    if v is None:return 'inf'
    if isinstance(v,F):return round(float(v),6)
    return v

data=[]; evidence=[]
for name in NAMES:
    p=REPO/MOBS/(name+'.java'); old=WORK/MOBS/(name+'.java')
    s=p.read_text(encoding='utf-8'); row=extract(s);row['name']=name;data.append(row)
    evidence.append(dict(name=name,current=row,old=extract(old.read_text(encoding='utf-8')) if old.exists() else None,
                         path=str(p),sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
lookup={m['name']:m for m in data};results=[]
for depth,names in ROTATIONS.items():
    for name in names:
        mob=lookup[name]
        if mob['damage'] is None or mob['accuracy'] is None:continue
        for stage in [1,2,3,4,5]:
            for level in [0,10,20,50,100,1000,None]:
                results.append(encounter(mob,depth,stage,level))
                if name in ['DM100','Shaman']:results.append(encounter(mob,depth,stage,level,True))
with (OUT/'MMO_REGION_MODEL.csv').open('w',newline='',encoding='utf-8-sig') as f:
    w=csv.DictWriter(f,fieldnames=list(results[0]));w.writeheader();w.writerows({k:num(v) for k,v in r.items()} for r in results)
(OUT/'MMO_MONSTER_SOURCE_AUDIT.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2),encoding='utf-8')

md=['# MMO 区域成长模型：按当前强化怪物校准','',
 '后续用户已明确：目标是几个数量级的属性软压制，不要求绝对无法击杀。下文严格抵抗下限只是边界分析，不采用该方案。掉落供给已另见 MMO_LOOT_EQUIVALENT.md，半饱和等级 20 只是演示参数，需要重新校准。','',
 '范围：仅讨论 MMO 模式。普通模式沿用破碎原版。本文没有修改游戏战斗代码。当前数值与英烈对照来自本地源码，分析程序可复跑：`python docs/models/mmo_region_model.py`。','',
 '## 当前与英烈公式','',
 '| 项目 | 当前破碎 MMO | 英烈 GRINDING_3/MMO |','|---|---|---|',
 '| 楼层倍率 G(d) | 1.5^(d−1) | 相同 |',
 '| 怪物生命 | 原生生命×G，以独立倍率保存，不改原生整数血量 | 原生 HP、HT×G，写回 int，取整且有溢出风险 |',
 '| 攻击伤害 | 原生伤害×G，再处理护甲/抗性及生命坐标转换 | damageFactor 返回 int(原生伤害×G)，乘区调用次序依实际攻击路径 |',
 '| 怪物经验 | floor(原生 EXP×G)，当前每次奖励仍有 int 上限 | int(原生 EXP×G) |',
 '| 命中与闪避 | 原生技能值×d，再进入命中概率函数 | 原生技能值×d，再以随机掷骰判定 |',
 '| 基础护甲、攻击间隔、移动速度 | 不因 MMO 自动乘 G | MMO Buff 本身也不放大这些属性 |','',
 'BIOCHIP 预设用整数中点伤害：a＋floor((b−a)/2)，不是永远等于浮点均值。当前命中概率采用原版独立均匀掷骰对应的分段概率：A≥E 时 p=1−E/(2A)，A<E 时 p=A/(2E)。BIOCHIP 累积命中额度，不能把长期命中率误认为每击稳定命中。两版基础普通怪有多处完全相同的数据，逐类对照见 JSON；这不意味着两个版本所有技能和 Boss 实现相同。','',
 '## 真实楼层数量级','', '| 区域 | 普通层倍率 | Boss 层倍率 |','|---|---:|---:|']
for k,name in enumerate(['下水道','监狱','洞穴','矮人都市','恶魔大厅'],1):
    first=5*(k-1)+1;last=first+3;boss=first+4
    md.append(f'| {name} | {1.5**(first-1):.3f}～{1.5**(last-1):.3f} | {1.5**(boss-1):.3f} |')
md+=['','跨五层仅 MMO 倍率增加 7.59375 倍，再叠加不同怪物原生属性变化。此前 8～24 伤害的示例不适用于监狱 MMO。','',
 '## 普通怪校准锚点','', '| 层数 | 怪物 | MMO 生命 | BIOCHIP 普通攻击伤害 | 原生护甲区间 | MMO 命中/闪避属性 |','|---:|---|---:|---:|---|---|']
for depth,name in [(6,'Skeleton'),(9,'Guard'),(9,'DM100'),(11,'Bat'),(11,'Brute'),(11,'Shaman'),(14,'DM200'),(19,'Golem'),(24,'Scorpio')]:
    m=lookup[name];g=1.5**(depth-1);a,b=m['damage']
    md.append(f'| {depth} | {name} | {m["hp"]*g:.2f} | {(a+(b-a)//2)*g:.2f} | {m["armor"][0]}～{m["armor"][1]} | {m["accuracy"]*depth}/{m["evasion"]*depth} |')
md+=['','这里的“伤害”不是已经扣除英雄防御的每回合损血。Thief/Monk 普通攻击间隔为 0.5；Crab/Bat 的 baseSpeed=2 是移动速度，不能直接当作攻击频率翻倍。DM-100 电击与萨满法术有单独伤害范围和魔法命中。','',
 '## 校准模型候选','',
 '令 u=L/(L＋20)，强化无限但同阶收益趋近有限上限；令 R=16^(装备成长阶级−1)。下列数值是参考整套配装的属性预算，并非把所有物品强行改成同值。','',
 '| 属性 | 候选函数 | 一阶极限 |','|---|---|---:|',
 '| 单击最终伤害预算 | R×(40＋360u) | 400 |',
 '| 物理防御预算 | R×(20＋130u) | 150 |',
 '| 总生命预算 | R×(200＋300u) | 500 |',
 '| 命中属性 | (100＋120u)×(1＋0.5(s−1)) | 220 |',
 '| 闪避属性 | (80＋120u)×(1＋0.5(s−1)) | 200 |',
 '| 每回合攻击次数 | 1＋0.5u | 1.5 |',
 '| 元素伤害减免 | 0.5u | 50% |','',
 '选择 R=16 的一项理由：二阶白板伤害预算 640，大于一阶无限强化极限 400；同时区域相隔五层的 MMO 放大量约为 7.594，但原生怪物能力也会上升。16 不是所有类别的最终倍率，正式设计仍需逐类曲线和不同装备品质修正。','',
 '对战计算：输出 DPS=命中率×攻击频率×max(0,伤害−护甲)；TTK=怪物实际生命/输出 DPS。输入 DPS 同理，法术走抗性而非物理防御；TTD=英雄实际生命/输入 DPS。TTK<TTD 仅表示简化站桩模型有正收益，不等于游戏中必胜。','',
 '## 一阶强化极限的实际对战结果','', '| 对手 | 预计击杀时间 | 预计生存时间 | 简化结果 |','|---|---:|---:|---|']
for depth,name,magic in [(9,'Guard',False),(9,'DM100',True),(11,'Bat',False),(11,'Brute',False),(11,'Shaman',False),(11,'Shaman',True),(14,'DM200',False)]:
    r=encounter(lookup[name],depth,1,None,magic)
    md.append(f'| {depth}层 {name}{"法术" if magic else "近战"} | {float(r["ttk"]):.2f} | {float(r["ttd"]):.2f} | {"能对抗" if r["expected_win"] else "站桩失败"} |')
md+=['','完整 CSV 扫描强化等级 0、10、20、50、100、1000 与无限极限，以及一至五阶配装。Necromancer 不具普通近战模型，未虚构其伤害；Elemental 子类、毒网、狂暴、回血、复活、死亡爆炸和精英均不在这些基础站桩结果中。无处不在的围攻会把多名敌人的 DPS 相加，不能据单挑结果宣称能清层。','',
 '## Boss 必须独立校准','', '| Boss/层数 | 基础生命 | MMO 实际生命 |','|---|---:|---:|']
for name,depth,hp in [('Goo',5,100),('Tengu',10,200),('DM300',15,300),('DwarfKing',20,300),('YogDzewa',25,1000)]:
    md.append(f'| {name}/{depth} | {hp} | {hp*1.5**(depth-1):.2f} |')
md+=['','上述 Boss 基础血量是在未开启更强 Boss 时的值；护盾、阶段、召唤、陷阱与无敌另算，不能用这张血量表直接计算完整通关。Yog 也没有被当作普通近战攻击者。','',
 '## “绝对无法越两区域”的边界','',
 '保留当前怪物公式时，只能得到站桩战斗不划算，不能证明永远无法击杀。洞穴怪的护甲仍很小，一阶仍能造成正伤害；无限生命、无限速度、药水、风筝及百分比持续伤害都可能绕过上述配装预算。尤其当前 mmoRemainder 会保留亚整数伤害，并不是单击归零就等于永远没有伤害。','',
 '若坚持严格边界，需要额外 MMO 专属规则，不能伪称原公式天然具备。例如新增最终输出抵抗下限：区域 k≥3 时，G_floor(d)=420×16^(k−3)×1.5^(d−该区域首层)。每条输出渠道都在对应的阶级预算内结算，伤害=max(0,最终伤害−该下限−原生护甲)。','',
 '这样一阶最高 400 低于洞穴首层抵抗 420，二阶最高 6400 低于都市首层抵抗 6720。无论攻击多少次，越两区域输出为零。这是新增机制，不是原有护甲；只有用户确认严格门槛后才能实施。阶段/伤害零值判定必须先于生命坐标归一化。','',
 '如果不想新增抵抗下限，则保留原怪物，仅采用有界成长与站桩差距，允许操作、技能和资源制造少量越阶。两个方向不能同时宣称“原公式不动”且“绝对永远无法击杀”。','',
 '当前无限英雄等级、根骨生命、狂怒速度补偿到伤害包、附魔和非直接伤害法杖均未按上述预算收敛，所以此报告是拟议模型，不是 MMO.1.6 的现行平衡。','',
 '证据：MmoRules.java 的 mobGrowth/initialize/physical/hit/normalized/damage；英烈 MMO.java 和 Char.hit；两版普通怪物类；破碎 MobSpawner.standardMobRotation。CSV 与 JSON 为模型附件。']
md+=['','## 有限 Boss 材料下的混合配装','', '下表只改变升阶物品对应的属性：输出升阶只把伤害乘 16，防具升阶只把物理防御乘 16；英雄生命、命中、闪避、攻速和元素抗性均保留一阶极限。不会把一次升阶偷换成全套升阶。这个检验用于发现预算缺口，不代表防具真实具有额外生命加成。','', '| 升阶选择 | 对手 | 击杀时间 | 生存时间 | 简化结果 |','|---|---:|---:|---|']
for label,patch in [('仅输出升二阶',{'damage':F(6400)}),('仅防具升二阶',{'armor':F(2400)}),('输出及防具升二阶',{'damage':F(6400),'armor':F(2400)})]:
    for depth,name,magic in [(11,'Bat',False),(11,'Shaman',True),(14,'DM200',False)]:
        r=encounter(lookup[name],depth,1,None,magic,patch)
        fmt=lambda x:'无限' if x is None else f'{float(x):.2f}'
        md.append(f'| {label} | {depth}层 {name}{"法术" if magic else "近战"} | {fmt(r["ttk"])} | {fmt(r["ttd"])} | {"能对抗" if r["expected_win"] else "站桩失败"} |')
md+=['','结论：普通怪单挑的强度预算并未证明一份 Boss 材料就能支撑全部下一地区。生命侧、抗性侧以及多敌人遭遇还需独立校准，不能只用伤害倍率做升阶设计。']
(OUT/'MMO_REGION_CALIBRATION.md').write_text('\n'.join(md)+'\n',encoding='utf-8')
assert player(2,0)['damage']>player(1,None)['damage']
assert player(1,None)['damage']<420
assert player(2,None)['damage']<420*16
for name in ['Bat','Brute','Shaman']:
    assert not encounter(lookup[name],11,1,None)['expected_win']
assert encounter(lookup['Guard'],9,1,None)['expected_win']
print('Generated',len(results),'modeled encounters; source-audited',len(data),'mob classes.')
print(OUT/'MMO_REGION_CALIBRATION.md')
