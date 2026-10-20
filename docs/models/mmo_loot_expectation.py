"""Analytic scroll-equivalent yield, current MMO drops + proposed forging rules."""
from pathlib import Path
from fractions import Fraction as F
import re,csv,math

REPO=Path(__file__).resolve().parents[2]
BASE=REPO/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon'
generator=(BASE/'items/Generator.java').read_text(encoding='utf-8')
def deck(cat):
    vals=[]
    for suffix in ['', '2']:
        m=re.search(cat+r'\.defaultProbs'+suffix+r'\s*=\s*new float\[\]\{([^}]+)',generator)
        if m:vals.append([F(n.strip()) for n in m[1].split(',')])
    return [sum(x) for x in zip(*vals)]
pweights=deck('POTION');sweights=deck('SCROLL')
assert sweights[0]==0 # Normal random scroll generation never selects ScrollOfUpgrade.
potion=F(4,5)+F(1,5)*pweights[-1]/sum(pweights)
scroll=F(4,5);stone=seed=F(1,2)
low=(stone+potion+scroll)/4 # Gold contributes zero.
mid=(2*low+F(4,5)+F(4,5)+F(2,5))/6 # Brew .8 / spell 0; bombs/honey 0.
high=(2*mid+stone+1+F(4,5))/4
cons=[]
for level in [0,5,10,15,20]:
    pl=max(F(0),F(3,5)-F(level,25))
    cutoff=max(pl,min(F(1),F(9,10)-F(level,50)))
    pm=cutoff-pl;ph=1-cutoff
    cons.append(pl*low+pm*mid+ph*high)
average_consumable=sum(cons)/5
category={m[1]:F(int(m[2])+int(m[3])) for m in re.finditer(r'^\s*(\w+)\s*\(\s*(\d+)\s*,\s*(\d+)\s*,',generator,re.M)}
assert sum(category.values())==70
tiers=re.search(r'floorSetTierProbs\s*=\s*new float\[\]\[\]\s*\{(.*?)\n\s*\};',generator,re.S)[1]
tierrows=[[F(n.strip()) for n in row.split(',')] for row in re.findall(r'\{([^}]+)\}',tiers)]
def gear_values(depth):
    probs=tierrows[min(depth//5,len(tierrows)-1)];total=sum(probs)
    # Target is a tier-3+ melee weapon such as Longsword: same-name materials
    # already have capped coefficient 1, so no additional same-name uncertainty.
    weapon=F(13,10)*sum(w*min(F(1),F(4,5)*(1+F(t,10))) for t,w in enumerate(probs,1))/total
    armor=F(13,10)*sum(w*min(F(1),F(16,25)*(1+F(t,10))) for t,w in enumerate(probs,1))/total
    return weapon,armor,F(7,5)*F(64,125),F(7,5)*F(16,25)
def general(depth,artifacts_exhausted=False):
    w,a,wand,ring=gear_values(depth)
    values={'WEAPON':w,'ARMOR':a,'WAND':wand,'RING':ring,'POTION':potion,'SCROLL':scroll,'SEED':seed,'STONE':stone}
    if artifacts_exhausted:values['ARTIFACT']=ring
    return sum(weight*values.get(cat,F(0)) for cat,weight in category.items())/70
def expected(depth,exhausted=False):
    count=2+math.floor(max(math.sqrt(min(1.5**(depth-1),2147483647)),depth)+.5)
    g=general(depth,exhausted)
    if depth<=2:per=(3*g+sum(cons))/10
    elif depth<=6:per=(2*g+sum(cons))/8
    elif depth<=10:per=(g+sum(cons))/6
    else:per=average_consumable
    return count,per,count*per
rows=[]
for depth in range(1,26):
    n,per,total=expected(depth)
    rows.append(dict(depth=depth,expected_draws=n,equivalent_per_draw=float(per),extra_equivalent=float(total),
                     extra_equivalent_after_artifact_exhaustion=float(expected(depth,True)[2])))
with (REPO/'docs/MMO_LOOT_EQUIVALENT.csv').open('w',newline='',encoding='utf-8-sig') as f:
    w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
md=['# 当前 MMO：击杀的升级卷轴等效收益','',
 '这是当前掉落代码与拟议打造规则的联合计算，打造规则尚未加入游戏。单位是强化等级增量，不是实际升级卷轴数量。用户确认仅计算装备、药水、卷轴、符石和种子；金币、能量、Boss 材料、食物、炸弹、蜂蜜、神器及其他未定材料按零计算。','',
 '假设 MMO 预设开启 MASS_LOOT/UNLIMITED_GROWTH，没有财富戒指、幸运附魔和影响生成的饰品，普通敌人死亡触发额外掉落一次。转入一件三阶及以上近战武器，例如长剑。原生品阶使用破碎当前生成表，强化等级按随机装备分布计算。投掷物不能投入近战武器，计入未使用资源而不强行折算。','',
 '## 每件物品的价值','',
 '- 升级卷轴、经验药水：1；其他药水和卷轴（含衍生类）：0.8；符石、种子：0.5。','- 装备：(显示等级＋1)×min(1,分类系数×(1＋原生品阶/10))，无品阶则不乘品阶项。转入近战武器时，其他近战系数 .8、防具/戒指 .64、法杖 .512。','- 近战武器/防具随机等级为 0/1/2，概率 .75/.20/.05，E[L＋1]=1.3。戒指/法杖为 2/3、4/15、1/15，E[L＋1]=1.4。','- 数量乘在每件贡献上。二倍掉落按整叠二倍计入，不把一堆物品误算成一件。','',
 '## 两段掉落相加','',
 '每只敌人的收益 E=MMO 额外掉落收益＋该种怪原生掉落收益；财富戒指、幸运附魔、任务奖励等若存在，还需另加。LOOT_PARADISE 增加的是楼层生成物资，不是每只怪死亡收益，不能再乘 5。','',
 '额外抽取次数 N=随机 1～3＋round(max(楼层,sqrt(1.5^(楼层−1))))，平均为 2＋后面固定项。','',
 '单次抽取：1～2 层为 30% Generator.random、50% 消耗品、20% 空；3～6 层为 25%、62.5%、12.5%；7～10 层为 1/6 普通生成、5/6 消耗品；11 层以后 100% 消耗品。因此后期额外掉落不再直接抽装备，但骷髅、守卫、萨满等原生掉落仍可产装备。','',
 f'普通随机药水期望价值 {float(potion):.9f}；普通随机卷轴 .8。低/中/高价值消耗品组的等效期望分别为 {float(low):.9f}、{float(mid):.9f}、{float(high):.9f}。计算包含二倍数量、异种药水/卷轴、经验药水和不可转换物品的空收益。','',
 f'11 层以后五个消耗品档位等概率，合计相当于低组 24%、中组 46%、高组 30%，每次抽取期望 {float(average_consumable):.9f} 升级量。','',
 '## 任意普通敌人的共有额外收益','', '| 楼层 | 平均抽取次数 | 等效升级量/击杀 |','|---:|---:|---:|']
for d in [1,2,4,5,6,9,10,11,14,15,16,19,20,21,24,25]:
    n,_,v=expected(d);md.append(f'| {d} | {n} | {float(v):.3f} |')
md+=['','这些是共有额外收益，不包含物种原生掉落。1～10 层的 Generator 是牌堆式生成，表采用完整牌堆的长期类别频率；某一局某一次击杀还受剩余牌堆影响。神器耗尽后该类别改产戒指，早期收益上限见 CSV；11 层以后不受这个分支影响。','',
 '## 加入具体怪物原生掉落','', '| 击杀例子 | 额外掉落 | 原生掉落增量 | 合计 |','|---|---:|---:|---:|']
examples=[]
for depth,name,native in [(6,'骷髅，尚未掉过武器',F('0.1667')*gear_values(6)[0]),
                          (9,'守卫，尚未掉过防具',F(1,5)*gear_values(9)[1]),
                          (9,'DM-100，无额外饰品修正',F(1,4)*scroll),
                          (11,'蝙蝠，尚未掉过治疗药水',F('0.1667')*F(4,5)),
                          (11,'暴徒，原生只掉金币',F(0)),
                          (11,'萨满，尚未掉过法杖',F('0.03')*gear_values(11)[2])]:
    extra=expected(depth)[2];md.append(f'| {depth}层 {name} | {float(extra):.3f} | {float(native):.3f} | {float(extra+native):.3f} |')
md+=['','骷髅武器与守卫防具、萨满法杖的原生概率每成功掉落一次约除以 3；蝙蝠治疗药水概率随全局计数下降，约 7 次后耗尽。不能把上表初始概率用于无限刷怪的每一只。DM-100 的原生卷轴期望仍约 .2，因为普通卷轴随机池全都价值 .8。','',
 '当前随机卷轴池中 ScrollOfUpgrade 的权重是 0；高价值消耗品池给的是经验药水和嬗变卷轴，不是升级卷轴。所以上述 MMO 额外掉落的真正升级卷轴数量期望为 0，等效升级量主要来自其他材料。楼层固定升级卷轴及特殊奖励不能归入单只怪共有收益。','',
 '## 对装备成长模型的意义','',
 '后期每只怪的材料供给约随 1.5^((d−1)/2) 增长，而怪物生命和伤害随 1.5^(d−1) 增长。掉落数量比怪物数值增长慢，但大量怪物与 AOE 会迅速累积等级。','',
 '11 层共有收益约 7.9：约 3 次击杀就提供 +20 的材料量，13 次左右提供 +100。25 层每次约 80：两次左右超过 +100。这里是总量期望估算，不是保证每次掉落或全部物资都被投入同一装备。','',
 '因此半饱和等级 L=20 会很快到达；不能只按一次击杀提供一张升级卷轴来设计成长节奏。先前严格零伤害门槛不采用，用户目标改为几个数量级的属性软压制；后续应以此收益曲线设每阶成长速度与上限。','',
 '可复跑：python docs/models/mmo_loot_expectation.py。源码依据 MmoRules.extraLootDraws/extraLoot、RingOfWealth.genConsumableDrop 及三个消费组、Generator 牌堆/品阶表、装备 random()、对应怪物 lootChance/createLoot。']
(REPO/'docs/MMO_LOOT_EQUIVALENT.md').write_text('\n'.join(md)+'\n',encoding='utf-8')
print('consumable E:',float(average_consumable),'low/mid/high:',float(low),float(mid),float(high))
for d in [1,6,9,11,14,20,25]:print(d,expected(d)[0],float(expected(d)[2]))
