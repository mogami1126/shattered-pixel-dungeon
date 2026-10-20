# 破碎 4.0.2：装备升级能力与原版成长公式

核对基准：本地未修改源码快照 `56eb1aa`。本次只做调查与文档，不修改游戏。表中的源码链接指向工作区内导出的原版文件，避免与当前 MMO 修改混淆。

L 表示强化后的有效装备等级；T 表示原生品阶；H 表示英雄等级；B 表示戒指的合计有效加成。默认无诅咒、无天赋、无增幅石、无额外状态。公式中的整数除法向零取整，正数时等于向下取整。表列出基础主属性；伤害并非固定取区间中点，实际还经过原版随机、命中、护甲、附魔与技能。

## 1. 可用升级卷轴强化的装备

核心装备包括 32 种近战武器、15 种普通投掷武器、5 种基础防具及其职业护甲形态、13 种法杖、12 种戒指。破损纹章是一次性可升级的非装备例外。

普通强化令永久等级加一。原版没有用卷轴反复升原生品阶的机制。神器、饰品和灵能弓有自己的成长系统，但不属于普通卷轴强化对象。

## 2. 近战武器完整表

通用最低伤害 `T＋L`，通用最高伤害 `5(T＋1)＋(T＋1)L`。各物品覆盖最高伤害如下。武器的攻速、命中、触及距离、偷袭和决斗技能会进一步改变实际收益；不能仅按这一张伤害表排名。

| 物品 | 品阶 | 最低伤害 | 最高伤害 |
|---|---:|---|---|
| [权杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Cudgel.java>) | 1 | 1＋1L | 8＋2L |
| [匕首](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Dagger.java>) | 1 | 1＋1L | 8＋2L |
| [镶钉手套](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Gloves.java>) | 1 | 1＋1L | 5＋1L |
| [法师魔杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/MagesStaff.java>) | 1 | 1＋1L | 6＋2L |
| [刺剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Rapier.java>) | 1 | 1＋1L | 8＋2L |
| [破旧的短剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/WornShortsword.java>) | 1 | 1＋1L | 10＋2L |
| [长匕首](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Dirk.java>) | 2 | 2＋1L | 12＋3L |
| [手斧](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/HandAxe.java>) | 2 | 2＋1L | 12＋3L |
| [铁头棍](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Quarterstaff.java>) | 2 | 2＋1L | 12＋3L |
| [短剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Shortsword.java>) | 2 | 2＋1L | 15＋3L |
| [短柄镰](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Sickle.java>) | 2 | 2＋1L | 20＋3L |
| [长矛](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Spear.java>) | 2 | 2＋1L | 20＋4L |
| [硬头锤](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Mace.java>) | 3 | 3＋1L | 16＋4L |
| [圆盾](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/RoundShield.java>) | 3 | 3＋1L | 12＋2L |
| [双钗](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Sai.java>) | 3 | 3＋1L | 10＋2L |
| [弯刀](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Scimitar.java>) | 3 | 3＋1L | 16＋4L |
| [单手剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Sword.java>) | 3 | 3＋1L | 20＋4L |
| [长鞭](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Whip.java>) | 3 | 3＋1L | 15＋3L |
| [暗杀之刃](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/AssassinsBlade.java>) | 4 | 4＋1L | 20＋5L |
| [战斧](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/BattleAxe.java>) | 4 | 4＋1L | 20＋5L |
| [十字弩](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Crossbow.java>) | 4 | 4＋1L | 20＋4L |
| [链枷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Flail.java>) | 4 | 4＋1L | 35＋8L |
| [武士刀](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Katana.java>) | 4 | 4＋1L | 20＋5L |
| [长剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Longsword.java>) | 4 | 4＋1L | 25＋5L |
| [符文之刃](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/RunicBlade.java>) | 4 | 4＋1L | 20＋6L |
| [魔岩拳套](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Gauntlet.java>) | 5 | 5＋1L | 15＋3L |
| [关刀](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Glaive.java>) | 5 | 5＋1L | 40＋8L |
| [巨斧](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Greataxe.java>) | 5 | 5＋1L | 45＋6L |
| [巨型方盾](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Greatshield.java>) | 5 | 5＋1L | 18＋4L |
| [巨剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/Greatsword.java>) | 5 | 5＋1L | 30＋6L |
| [战锤](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/WarHammer.java>) | 5 | 5＋1L | 24＋6L |
| [战镰](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/melee/WarScythe.java>) | 5 | 5＋1L | 40＋6L |

补充：圆盾提供额外防御上限 `4＋L`；巨型方盾为 `6＋2L`。刺剑、铁头棍、武士刀分别提供固定的 1、2、3 额外防御。盾牌给的是防御掷骰能力，不是固定百分比免伤。

法师魔杖同时是近战武器和内嵌法杖载体：升级魔杖后，内嵌法杖等级同步到魔杖等级；内嵌法杖额外增加一个最大充能，仍封顶 10，正常升级还补充一充能。不同于普通法杖。

力量需求：武器基本为 `8＋2T－floor((sqrt(8×max(L,0)＋1)－1)/2)`，部分武器再加自身修正；实际还受超重惩罚、力量及天赋影响。

## 3. 投掷武器完整表

通用伤害 `2T＋L` 到 `5T＋TL`。神射之戒会额外增加用于伤害公式的等级。

| 物品 | 品阶 | 最低伤害 | 最高伤害 |
|---|---:|---|---|
| [飞刀](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ThrowingKnife.java>) | 1 | 2＋1L | 6＋2L |
| [飞刺](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ThrowingSpike.java>) | 1 | 2＋1L | 5＋1L |
| [投石](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ThrowingStone.java>) | 1 | 2＋1L | 5＋1L |
| [捕鱼矛](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/FishingSpear.java>) | 2 | 4＋1L | 10＋2L |
| [手里剑](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/Shuriken.java>) | 2 | 4＋1L | 8＋2L |
| [飞槌](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ThrowingClub.java>) | 2 | 4＋1L | 8＋2L |
| [流星索](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/Bolas.java>) | 3 | 4 | 9＋2L |
| [苦无](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/Kunai.java>) | 3 | 6＋1L | 12＋3L |
| [投矛](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ThrowingSpear.java>) | 3 | 6＋1L | 15＋3L |
| [重型回旋镖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/HeavyBoomerang.java>) | 4 | 8＋1L | 16＋3L |
| [标枪](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/Javelin.java>) | 4 | 8＋1L | 20＋4L |
| [飞斧](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/Tomahawk.java>) | 4 | 6＋1L | 16＋3L |
| [震爆方石](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ForceCube.java>) | 5 | 10＋1L | 30＋5L |
| [投掷锤](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/ThrowingHammer.java>) | 5 | 10＋1L | 20＋5L |
| [三叉戟](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/weapon/missiles/Trident.java>) | 5 | 10＋1L | 25＋5L |

耐久单独成长：可使用次数的基础部分为 `baseUses×1.5^L`，再乘天赋、增幅与神射戒指修正；达到至少 100 次时，普通逻辑的每次耐久消耗归零。升级也修复耐久。各投掷物的 baseUses 和部分特殊耐久覆盖见对应原版源码；不能把耐久一起按伤害倍率放大。

飞镖及涂药飞镖：原版 `isUpgradable=false`，不允许直接卷轴强化；仍可吃十字弩与神射戒指的间接加成。

## 4. 防具

| 基础防具 | 品阶 | 通常最低防御 | 通常最高防御 |
|---|---:|---|---|
| 布甲 | 1 | L | 2＋L |
| 皮甲 | 2 | L | 4＋2L |
| 链甲 | 3 | L | 6＋3L |
| 鳞甲 | 4 | L | 8＋4L |
| 板甲 | 5 | L | 10＋5L |

正常、非负等级下：最低防御是 L，最高是 `T(2＋L)`，增幅石再改变防御。代码为负等级和特殊情况下另有分支，不能把简化式应用于所有值。“信仰护体”挑战改为最低 0、最高 `1＋T＋L＋增幅修正`。职业护甲保留转换前防具的品阶与强化等级；护甲能力充能不等于基础防御成长。

力量需求同样使用平方根降需求公式，并受精通药剂、天赋等修正。防御是区间掷骰，不是固定减伤比例。

## 5. 法杖完整表

最大充能通常为 `min(2＋永久等级,10)`；魔弹法杖起始为 3。充能和伤害等级是不同概念，树脂、诅咒灌注、临时法杖增益会影响有效效果等级。

| 法杖 | 原版主成长 |
|---|---|
| [冲击波法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfBlastWave.java>) | 伤害 1＋L ～ 3＋3L；击退还有距离、地形及目标修正。 |
| [酸蚀法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfCorrosion.java>) | 酸蚀强度 2＋L，初始气体量 50＋10L；这是持续效果，不是一次直伤。 |
| [腐化法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfCorruption.java>) | 腐化力量 3＋L/3（浮点）；敌人当前血量、经验及已有减益改变抵抗。减益持续 6＋3L。 |
| [解离法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfDisintegration.java>) | 伤害 2＋L ～ 8＋4L；穿透目标与地形另有伤害加成。 |
| [焰浪法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfFireblast.java>) | 消耗 C=1/2/3 充能：最低 C(1＋L)，最高分别为 2＋2L、8＋4L、18＋6L；扇形范围与状态另行计算。 |
| [冰霜法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfFrost.java>) | 伤害 2＋L ～ 8＋5L；水中、冻伤/冻结状态另有修正。 |
| [雷霆法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfLightning.java>) | 伤害 5＋L ～ 10＋5L；连锁目标分配与导电修正另算。 |
| [灵壤法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfLivingEarth.java>) | 直伤 4 ～ 6＋2L；岩石护甲容量 8＋4L；守护者生命 16＋8L，防御区间依目标种类为 L～2＋L 或 L～3＋3L。 |
| [魔弹法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfMagicMissile.java>) | 伤害 2＋L ～ 8＋2L；高等级魔弹可暂时强化下一次其他法杖效果。 |
| [棱光法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfPrismaticLight.java>) | 伤害 1＋L ～ 5＋3L；对恶魔/亡灵的修正、照明和致盲另算。 |
| [再生法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfRegrowth.java>) | 无普通直伤；每次消耗 C 充能，铺草量 round((3.67＋L/3)C)。总充能使用限额随英雄等级及法杖等级增加，+10 时取消该限额。金莲生命 25＋3L，种子保留概率 min(1,0.40＋0.04L)。 |
| [注魂法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfTransfusion.java>) | 对亡灵伤害 3＋L ～ 6＋2L；治疗消耗施法者约 5% 最大生命，治疗量为该消耗＋3L，溢出成为目标护盾；相关施法者护盾为 5＋L。 |
| [哨卫法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfWarding.java>) | 哨卫伤害 2＋L ～ 8＋4L；召唤物阶段 1～6 独立决定形态、生命、攻击次数及升级/治疗逻辑。 |

普通法杖充能基础延迟为 `10＋40×scalingFactor^缺失充能数`，每回合累加进度；能量戒指、饥饿、神器及天赋可改变充能。不能把 +L 理解为施法速度线性变快。

## 6. 戒指完整表

无诅咒的单枚 +L 戒指贡献 B=L＋1；同效果戒指把 B 相加后进入函数。诅咒时贡献为 min(0,L－2)。有些效果使用临时增益后的 B，有些使用永久 B，具体见源码。

| 戒指 | 原版主公式 |
|---|---|
| [精准之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfAccuracy.java>) | 命中属性倍率 1.3^B；不是命中概率直接增加 30%。 |
| [奥术之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfArcana.java>) | 附魔/刻印效力倍率 1.175^B；具体附魔如何采用该倍率各自决定。 |
| [元素之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfElements.java>) | 支持的元素伤害/相关持续效果乘 0.825^B。 |
| [能量之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfEnergy.java>) | 法杖和支持的神器充能倍率 1.175^B，再叠加部分天赋。 |
| [闪避之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfEvasion.java>) | 闪避属性倍率 1.125^B；不是固定概率闪避。 |
| [武力之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfForce.java>) | 装备武器时提供加成 B；徒手使用力量推算等效品阶 Q，伤害 round(Q＋B) ～ round((Q＋1)(5＋B))。力量超过 18 后，Q 的力量成长减半。 |
| [狂怒之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfFuror.java>) | 攻击速度倍率 1.09051^B；攻击时间相应除以该倍率。 |
| [疾速之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfHaste.java>) | 移动速度倍率 1.15^B；不直接提高攻击速度。 |
| [根骨之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfMight.java>) | 力量增加 B，最大生命倍率 1.035^B。 |
| [神射之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfSharpshooting.java>) | 投掷物伤害公式的有效等级增加 B；耐久使用次数倍率 1.2^B。 |
| [韧性之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfTenacity.java>) | 伤害倍率 0.85^(B×已损失生命比例)；满血时无这部分减伤。 |
| [财富之戒](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/rings/RingOfWealth.java>) | 普通掉落概率倍率 1.2^B；额外战利品另有次数计数及奖励池，不能把所有奖励都简化为这个倍率。 |

力量之戒等效品阶：先取 Q=max(1,(力量－8)/2)，若 Q>5，再取 Q=5＋(Q－5)/2。如果有效加成 B≤0，则徒手计算强制 Q=1。

## 7. 升级还会执行的原版效果

- 武器、防具升级清除 cursed 标记，但诅咒附魔/刻印有单独处理；普通升级约有 1/3 概率移除诅咒附魔/刻印。
- 普通附魔/刻印从升级前 +4/+5/+6/+7/+8 开始有约 10%/20%/40%/80%/100% 丢失概率；硬化保护从 +6 开始按相似阶梯丢失。刻印有天赋例外。
- 戒指和普通法杖升级各有 1/3 概率清除诅咒。法杖永久升级会消耗一个已有树脂加成，并补充一充能。
- 破损纹章只有 +0 时能直接升级一次，之后可把这一级转给防具；它不是可以无限强化的独立装备。

## 8. 有专属成长、不能直接用卷轴强化的物品

神器原生 isUpgradable=false，不能用升级卷轴；其内部 upgrade() 由各自专属玩法触发。展示等级通常换算到 +0～+10：round(内部等级×10/内部上限)。下表的 L 是内部等级，不一定等于显示的 +等级。源码存在的神器不等于都会在常规生成池出现，部分为保留类或非随机掉落物。

| 神器 | 内部等级上限 | 主成长公式或进度 |
|---|---:|---|
| [炼金工具箱](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/AlchemistsToolkit.java>) | 10 | 投入炼金能量成长；预热时间约 (10－L)²；充能成长项 (2＋L)。 |
| [荆棘斗篷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/CapeOfThorns.java>) | 10 | 充能获取 damage×(0.5＋0.05L)；升级经验门槛 5(L＋1)。 |
| [蓄血圣杯](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/ChaliceOfBlood.java>) | 10 | 自然回血间隔 10－(1.33＋0.667L)；刺击伤害区间 ceil(3＋2.5L²)～floor(7＋3.5L²)。 |
| [暗影斗篷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/CloakOfShadows.java>) | 10 | 隐身使用经验升级；充能上限 min(L＋3,10)，升级经验门槛 50(L＋1)。 |
| [干枯玫瑰](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/DriedRose.java>) | 10 | 花瓣升级；幽灵力量 13＋floor(L/2)，其他生命与装备互动见源码。 |
| [虚空锁链](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/EtherealChains.java>) | 5 | 通过经验成长；充能软上限 5＋2L，升级经验门槛 100＋100L。 |
| [神圣法典](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/HolyTome.java>) | 10 | 施法获得经验；充能上限 min(L＋3,10)，升级经验门槛 50(L＋1)，法术解锁另有表。 |
| [丰饶之角](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/HornOfPlenty.java>) | 10 | 喂食成长；容量 5＋floor(L/2)，充能收益系数 0.25＋0.125L。 |
| [时空道标](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/LloydsBeacon.java>) | 3 | 专属信标功能与内部 0～3 阶段，不能卷轴强化，保留代码。 |
| [神偷袖章](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/MasterThievesArmband.java>) | 10 | 偷窃/使用成长；容量 5＋floor(L/2)，经验门槛 10＋round(3.33L)。 |
| [自然之履](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/SandalsOfNature.java>) | 3 | 喂不同种子升形态，需要 3＋3L 种种子；成长相关充能项 (3＋2L)/6。 |
| [骷髅钥匙](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/SkeletonKey.java>) | 10 | 使用钥匙专属成长；充能上限 3＋floor(L/2)，经验进度门槛 4＋L。 |
| [先见护符](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/TalismanOfForesight.java>) | 10 | 发现隐藏内容成长；感知时间主项 5＋2L，经验门槛 100＋50L，充能项 0.05＋0.005L。 |
| [时光沙漏](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/TimekeepersHourglass.java>) | 5 | 沙袋成长，内部上限 5；时间控制与容量为专属机制。 |
| [无序魔典](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/UnstableSpellbook.java>) | 10 | 喂指定卷轴成长；充能上限 floor(0.6L)＋2，可用卷轴池随成长变化。 |

灵能弓：不能用升级卷轴；无诅咒灌注时展示等级 floor(H/5)，伤害主项为 `1＋floor(H/5)＋神射加成` 到 `6＋floor(H/2.5)＋2×神射加成`。这是英雄等级成长。

饰品是随身被动，不占普通装备槽，也不能用升级卷轴。通过炼金能量升级到 +3。苔藓丛簇、残魔余卷、奇迹树脂的升级能量成本是 10＋5L；其他列出的饰品为 6＋2L（获取触媒和生成饰品的成本另算）。各自效果曲线是专用机制，原版源码逐件附在后面。

饰品清单：[混沌香炉](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ChaoticCenser.java>)、[幻象裂镜](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/CrackedSpyglass.java>)、[位面日晷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/DimensionalSundial.java>)、[奇异能晶](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ExoticCrystals.java>)、[蝾螈魔眼](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/EyeOfNewt.java>)、[雪貂绒束](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/FerretTuft.java>)、[拟箱利齿](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/MimicTooth.java>)、[苔藓丛簇](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/MossyClump.java>)、[残魔余卷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ParchmentScrap.java>)、[石化种子](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/PetrifiedSeed.java>)、[巨鼠头骨](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/RatSkull.java>)、[盐晶立方](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/SaltCube.java>)、[遗忘碎片](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ShardOfOblivion.java>)、[十三叶草](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ThirteenLeafClover.java>)、[陷阱元件](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/TrapMechanism.java>)、[凝血试管](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/VialOfBlood.java>)、[奇迹树脂](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/WondrousResin.java>)。

## 9. 与当前 MMO 实现的区别

以上表格是原版，不包含本项目已有的超过 +20 的 GameNumber 外推、1.035 指数放大、戒指数值替代、确定性命中、合并、批量升级或新升阶乘区。现有升阶/打造方案仍在讨论，尚未实现。本报告不把这些讨论方案当成现行代码。

## 10. 原版机制核对附件

下面保留每件神器、饰品与法杖中与等级直接相关的源码行，方便后续逐个设计；每条链接可查看完整原版文件。单行摘录只是定位依据，条件、调用时机、取整和状态修正以完整函数为准。

### 炼金工具箱

[炼金工具箱](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/AlchemistsToolkit.java>)

```java
51: levelCap = 10;
67: if (level() < levelCap) {
96: final int maxLevels = Math.min(levelCap - level(), Dungeon.energy/6);
226: if (level() == 10){
231: float turnsToWarmUp = (int) Math.pow(10 - level(), 2);
247: float chargeGain = (2 + level()) * levelPortion;
```

### 荆棘斗篷

[荆棘斗篷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/CapeOfThorns.java>)

```java
39: levelCap = 10;
95: charge += damage*(0.5+level()*0.05);
98: cooldown = 10+level();
113: if (exp >= (level()+1)*5 && level() < levelCap){
114: exp -= (level()+1)*5;
```

### 蓄血圣杯

[蓄血圣杯](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/ChaliceOfBlood.java>)

```java
56: levelCap = 10;
65: && level() < levelCap
120: return (int)Math.ceil(3 + 2.5f*(level()*level()));
124: return (int)Math.floor(7 + 3.5f*(level()*level()));
172: if (level() >= 6)
174: else if (level() >= 2)
182: if (level() >= 7) image = ItemSpriteSheet.ARTIFACT_CHALICE3;
183: else if (level() >= 3) image = ItemSpriteSheet.ARTIFACT_CHALICE2;
198: float healDelay = 10f - (1.33f + level()*0.667f);
223: else if (level() == 0)
225: else if (level() < levelCap)
```

### 暗影斗篷

[暗影斗篷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/CloakOfShadows.java>)

```java
56: levelCap = 10;
58: charge = Math.min(level()+3, 10);
60: chargeCap = Math.min(level()+3, 10);
236: if (level() > 7) missing += 5*(level() - 7)/3f;
332: int lvlDiffFromTarget = ((Hero) target).lvl - (1+level()*2);
334: if (level() >= 7){
335: lvlDiffFromTarget -= level()-6;
343: if (exp >= (level() + 1) * 50 && level() < levelCap) {
346: exp -= level() * 50;
```

### 干枯玫瑰

[干枯玫瑰](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/DriedRose.java>)

```java
91: levelCap = 10;
236: return 13 + level()/2;
251: if (level() < levelCap)
329: int heal = Math.round((1 + level()/3f)*amount);
340: if (level() >= 9)
342: else if (level() >= 4)
346: droppedPetals = Math.max( level(), droppedPetals );
509: } if ( rose.level() >= rose.levelCap ){
517: if (rose.level() == rose.levelCap) {
598: HT = 40 + 10*rose.level();
749: return Math.max(super.glyphLevel(cls), armor().buffedLvl());
```

### 虚空锁链

[虚空锁链](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/EtherealChains.java>)

```java
64: levelCap = 5;
120: charge = 5+(level()*2); //sets charge to soft cap
290: int chargeTarget = 5+(level()*2);
319: int chargeTarget = 5+(level()*2);
350: if (charge > 5+(level()*2)){
351: levelPortion *= (5+((float)level()*2))/charge;
355: if (exp > 100+level()*100 && level() < levelCap){
356: exp -= 100+level()*100;
```

### 神圣法典

[神圣法典](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/HolyTome.java>)

```java
55: levelCap = 10;
57: charge = Math.min(level()+3, 10);
59: chargeCap = Math.min(level()+3, 10);
161: int lvlDiffFromTarget = Dungeon.hero.lvl - (1+level()*2);
163: if (level() >= 7){
164: lvlDiffFromTarget -= level()-6;
173: if (exp >= (level() + 1) * 50 && level() < levelCap) {
176: exp -= level() * 50;
292: if (level() > 7) missing += 5*(level() - 7)/3f;
```

### 丰饶之角

[丰饶之角](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/HornOfPlenty.java>)

```java
61: levelCap = 10;
65: chargeCap = 5 + level()/2;
84: if (isEquipped( hero ) && level() < levelCap && !cursed) {
203: if (level() < levelCap)
216: chargeCap = 5 + level()/2;
223: chargeCap = 5 + level()/2;
228: if (level() >= 10) return;
239: upgrades = Math.min(upgrades, 10 - level());
243: if (level() == 10){
283: float chargeGain = Hunger.STARVING * levelPortion * (0.25f + (0.125f*level()));
```

### 时空道标

[时空道标](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/LloydsBeacon.java>)

```java
67: levelCap = 3;
70: chargeCap = 3+level();
294: if (level() == levelCap) return this;
```

### 神偷袖章

[神偷袖章](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/MasterThievesArmband.java>)

```java
60: levelCap = 10;
64: chargeCap = 5+level()/2;
137: float lootMultiplier = 1f + 0.1f*level();
138: int debuffDuration = 3 + level()/2;
188: while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
189: exp -= 10 + Math.round(3.33f * level());
242: chargeCap = 5 + (level()+1)/2;
279: float chargeGain = (3f + 0.15f*level()) * levelPortion;
310: while (exp >= (10 + Math.round(3.33f * level())) && level() < levelCap) {
311: exp -= 10 + Math.round(3.33f * level());
323: float val = chargesUsed * (10 + level()/2f);
332: valUsing += 10 + level()/2f;
```

### 自然之履

[自然之履](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/SandalsOfNature.java>)

```java
75: levelCap = 3;
192: if (level() == 0)   return super.name();
226: if (level() < 0)        image = ItemSpriteSheet.ARTIFACT_SANDALS;
227: else if (level() == 0)  image = ItemSpriteSheet.ARTIFACT_SHOES;
228: else if (level() == 1)  image = ItemSpriteSheet.ARTIFACT_BOOTS;
229: else if (level() >= 2)  image = ItemSpriteSheet.ARTIFACT_GREAVES;
236: && (level() < 3 || curSeedEffect != item.getClass());
266: if (level() == 1)  image = ItemSpriteSheet.ARTIFACT_SHOES;
267: else if (level() == 2)  image = ItemSpriteSheet.ARTIFACT_BOOTS;
268: else if (level() >= 3)  image = ItemSpriteSheet.ARTIFACT_GREAVES;
276: float chargeGain = (3f + 2*level())/6f;
309: if (level() < 3) seeds.add(0, item.getClass());
317: if (seeds.size() >= 3+(level()*3)){
320: Catalog.countUses(SandalsOfNature.class, level() == 3 ? 4 : 3);
321: if (level() >= 1 && level() <= 3) {
```

### 骷髅钥匙

[骷髅钥匙](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/SkeletonKey.java>)

```java
69: levelCap = 10;
71: charge = 3+level()/2;
73: chargeCap = 3+level()/2;
116: if (level() == levelCap){
121: if (exp > 4+level()){
122: exp -= 4+level();
463: chargeCap = 3 + (level()+1)/2;
```

### 先见护符

[先见护符](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/TalismanOfForesight.java>)

```java
61: levelCap = 10;
136: return Math.min(5 + 2*level(), (charge-3)/1.08f);
194: Buff.append(curUser, CharAwareness.class, 5 + 2*level()).charID = ch.id();
205: Buff.append(curUser, HeapAwareness.class, 5 + 2*level()).pos = h.pos;
215: if (exp >= 100 + 50*level() && level() < levelCap) {
216: exp -= 100 + 50*level();
285: float chargeGain = (0.05f+(level()*0.005f));
```

### 时光沙漏

[时光沙漏](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/TimekeepersHourglass.java>)

```java
64: levelCap = 5;
66: charge = 5+level();
68: chargeCap = 5+level();
200: while (level()+1 > sandBags)
212: if (level() < levelCap )
516: if (hourglass.level() == hourglass.levelCap)
```

### 无序魔典

[无序魔典](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/artifacts/UnstableSpellbook.java>)

```java
71: levelCap = 10;
73: charge = (int)(level()*0.6f)+2;
75: chargeCap = (int)(level()*0.6f)+2;
113: if (isEquipped( hero ) && level() < levelCap && !cursed && hero.buff(MagicImmune.class) == null) {
281: chargeCap = (int)((level()+1)*0.6f)+2;
284: while (!scrolls.isEmpty() && scrolls.size() > (levelCap-1-level())) {
295: while (!scrolls.isEmpty() && scrolls.size() > (levelCap-1-level())) {
309: if (level() < levelCap && scrolls.size() > 0) {
317: if (level() > 0) {
```

### 混沌香炉

[混沌香炉](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ChaoticCenser.java>)

```java
63: protected int upgradeEnergyCost() {
65: return 6+2*level();
78: return averageTurnsUntilGas(trinketLevel(ChaoticCenser.class));
152: int level = trinketLevel(ChaoticCenser.class);
```

### 幻象裂镜

[幻象裂镜](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/CrackedSpyglass.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
42: if (buffedLvl() >= 2){
53: return extraLootChance(trinketLevel(CrackedSpyglass.class));
```

### 位面日晷

[位面日晷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/DimensionalSundial.java>)

```java
38: protected int upgradeEnergyCost() {
40: return 6+2*level();
48: (int)(100*(1f - enemySpawnMultiplierDaytime(buffedLvl()))),
49: (int)(100*(enemySpawnMultiplierNighttime(buffedLvl())-1f)));
60: if (trinketLevel(DimensionalSundial.class) != -1) {
77: return enemySpawnMultiplierDaytime(trinketLevel(DimensionalSundial.class));
89: return enemySpawnMultiplierNighttime(trinketLevel(DimensionalSundial.class));
```

### 奇异能晶

[奇异能晶](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ExoticCrystals.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
49: return consumableExoticChance(trinketLevel(ExoticCrystals.class));
```

### 蝾螈魔眼

[蝾螈魔眼](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/EyeOfNewt.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
44: mindVisionRange(buffedLvl()));
53: return visionRangeMultiplier(trinketLevel(EyeOfNewt.class));
65: return mindVisionRange(trinketLevel(EyeOfNewt.class));
```

### 雪貂绒束

[雪貂绒束](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/FerretTuft.java>)

```java
35: protected int upgradeEnergyCost() {
37: return 6+2*level();
50: return evasionMultiplier(trinketLevel(FerretTuft.class));
```

### 拟箱利齿

[拟箱利齿](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/MimicTooth.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
53: return mimicChanceMultiplier(trinketLevel(MimicTooth.class));
65: return trinketLevel(MimicTooth.class) >= 0;
69: return ebonyMimicChance(trinketLevel(MimicTooth.class));
```

### 苔藓丛簇

[苔藓丛簇](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/MossyClump.java>)

```java
40: protected int upgradeEnergyCost() {
42: return 10+5*level();
55: return overrideNormalLevelChance(trinketLevel(MossyClump.class));
```

### 残魔余卷

[残魔余卷](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ParchmentScrap.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 10+5*level();
49: return enchantChanceMultiplier(trinketLevel(ParchmentScrap.class));
68: return curseChanceMultiplier(trinketLevel(ParchmentScrap.class));
```

### 石化种子

[石化种子](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/PetrifiedSeed.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
53: return grassLootMultiplier(trinketLevel(PetrifiedSeed.class));
65: return stoneInsteadOfSeedChance(trinketLevel(PetrifiedSeed.class));
```

### 巨鼠头骨

[巨鼠头骨](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/RatSkull.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
49: return exoticChanceMultiplier(trinketLevel(RatSkull.class));
```

### 盐晶立方

[盐晶立方](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/SaltCube.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 6+2*level();
55: return hungerGainMultiplier(trinketLevel(SaltCube.class));
67: return healthRegenMultiplier(trinketLevel(SaltCube.class));
```

### 遗忘碎片

[遗忘碎片](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ShardOfOblivion.java>)

```java
56: protected int upgradeEnergyCost() {
58: return 6+2*level();
139: return trinketLevel(ShardOfOblivion.class) >= 0;
193: return lootChanceMultiplier(trinketLevel(ShardOfOblivion.class));
```

### 十三叶草

[十三叶草](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/ThirteenLeafClover.java>)

```java
35: protected int upgradeEnergyCost() {
37: return 6+2*level();
50: return alterHeroDamageChance(trinketLevel(ThirteenLeafClover.class));
```

### 陷阱元件

[陷阱元件](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/TrapMechanism.java>)

```java
40: protected int upgradeEnergyCost() {
42: return 6+2*level();
55: return overrideNormalLevelChance(trinketLevel(TrapMechanism.class));
67: return revealHiddenTrapChance(trinketLevel(TrapMechanism.class));
```

### 凝血试管

[凝血试管](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/VialOfBlood.java>)

```java
35: protected int upgradeEnergyCost() {
37: return 6+2*level();
46: Integer.toString(maxHealPerTurn(buffedLvl())));
56: return trinketLevel(VialOfBlood.class) != -1;
60: return trinketLevel(VialOfBlood.class);
64: return totalHealMultiplier(trinketLevel(VialOfBlood.class));
76: return maxHealPerTurn(trinketLevel(VialOfBlood.class));
```

### 奇迹树脂

[奇迹树脂](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/trinkets/WondrousResin.java>)

```java
34: protected int upgradeEnergyCost() {
36: return 10+5*level();
59: return positiveCurseEffectChance( trinketLevel(WondrousResin.class) );
71: return extraCurseEffectChance( trinketLevel(WondrousResin.class) );
```

### 冲击波法杖

[冲击波法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfBlastWave.java>)

```java
96: int strength = Math.round(1.5f + buffedLvl() / 2f);
113: int strength = buffedLvl() + 3;
202: int dmg = Hero.heroDamageIntRange(8+2*buffedLvl(), 12+3*buffedLvl());
```

### 酸蚀法杖

[酸蚀法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfCorrosion.java>)

```java
59: CorrosiveGas gas = Blob.seed(bolt.collisionPos, 50 + 10 * buffedLvl(), CorrosiveGas.class);
61: gas.setStrength(2 + buffedLvl(), getClass());
94: int level = Math.max( 0, buffedLvl() );
```

### 腐化法杖

[腐化法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfCorruption.java>)

```java
142: float corruptingPower = 3 + buffedLvl()/3f;
214: Buff.append(enemy, debuffCls, 6 + buffedLvl()*3);
240: int level = Math.max( 0, buffedLvl() );
```

### 解离法杖

[解离法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfDisintegration.java>)

```java
77: int level = buffedLvl();
138: return buffedLvl()*2 + 6;
```

### 焰浪法杖

[焰浪法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfFireblast.java>)

```java
203: ch.damage(Math.round(powerMulti*Hero.heroDamageIntRange(2 + 2*buffedLvl(), 8 + 4*buffedLvl())), this);
```

### 冰霜法杖

[冰霜法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfFrost.java>)

```java
103: Buff.affect(ch, Chill.class, 4+buffedLvl());
105: Buff.affect(ch, Chill.class, 2+buffedLvl());
```

### 雷霆法杖

[雷霆法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfLightning.java>)

```java
111: float procChance = (buffedLvl()+1f)/(buffedLvl()+4f) * procChanceMultiplier(attacker);
```

### 灵壤法杖

[灵壤法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfLivingEarth.java>)

```java
101: buff.addArmor( buffedLvl(), armorToAdd);
108: guardian.setInfo(curUser, buffedLvl(), armorToAdd);
117: guardian.setInfo(curUser, buffedLvl(), buff.armor);
187: guardian.setInfo(curUser, buffedLvl(), armorToAdd);
238: guardian.setInfo(Dungeon.hero, buffedLvl(), armor);
241: Buff.affect(attacker, RockArmor.class).addArmor( buffedLvl(), armor);
267: private int wandLevel;
285: private void addArmor(int wandLevel, int toAdd ){
286: this.wandLevel = Math.max(this.wandLevel, wandLevel);
292: return 8 + wandLevel*4;
384: private int wandLevel = -1;
386: public void setInfo(Hero hero, int wandLevel, int healthToAdd){
387: if (wandLevel > this.wandLevel) {
388: this.wandLevel = wandLevel;
389: HT = 16 + 8 * wandLevel;
420: return dr + Random.NormalIntRange(wandLevel, 2 + wandLevel);
422: return dr + Random.NormalIntRange(wandLevel, 3 + 3 * wandLevel);
468: Buff.affect(Dungeon.hero, RockArmor.class).addArmor(wandLevel, HP);
```

### 魔弹法杖

[魔弹法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfMagicMissile.java>)

```java
69: if (wandCharger.wand().buffedLvl() < buffedLvl() || curUser.buff(MagicCharge.class) != null){
108: if (level < wand.buffedLvl()){
109: this.level = wand.buffedLvl();
120: public int level(){
```

### 棱光法杖

[棱光法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfPrismaticLight.java>)

```java
74: Buff.prolong( curUser, Light.class, 2f + buffedLvl());
76: Buff.prolong( curUser, Light.class, 10f+buffedLvl()*5);
91: if (Random.Int(5+buffedLvl()) >= 3) {
92: Buff.prolong(ch, Blindness.class, 2f + (buffedLvl() * 0.333f));
166: Buff.prolong( defender, Cripple.class, Math.round((1+staff.buffedLvl())*procChanceMultiplier(attacker)));
```

### 再生法杖

[再生法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfRegrowth.java>)

```java
97: int grassToPlace = Math.round((3.67f+buffedLvl()/3f)*chrgUsed);
130: l.setLevel(buffedLvl());
204: return chargeLimit(  heroLvl, level() );
232: int level = Math.max(0, staff.buffedLvl());
```

### 注魂法杖

[注魂法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfTransfusion.java>)

```java
95: int healing = selfDmg + 3*buffedLvl();
125: Buff.affect(curUser, Barrier.class).setShield((5 + buffedLvl()));
165: int shieldToGive = Math.round((2*(5 + buffedLvl()))*procChanceMultiplier(attacker));
```

### 哨卫法杖

[哨卫法杖](<C:/Users/MOGAMI/Documents/ChatGPT/英烈的像素地牢/.research/original-upgrade-source/wands/WandOfWarding.java>)

```java
102: maxWardEnergy += 2 + ((Charger) buff).wand().level();
144: ((Ward) ch).upgrade( buffedLvl() );
146: ((Ward) ch).wandHeal( buffedLvl() );
161: ward.wandLevel = buffedLvl();
186: int level = Math.max( 0, staff.buffedLvl() );
198: ((Ward) ch).wandHeal(staff.buffedLvl(), powerMulti);
236: private int wandLevel = 1;
257: public void upgrade(int wandLevel ){
258: if (this.wandLevel < wandLevel){
259: this.wandLevel = wandLevel;
278: wandHeal(wandLevel);
301: public void wandHeal( int wandLevel ){
302: wandHeal( wandLevel, 1f );
305: public void wandHeal( int wandLevel, float healFactor ){
306: if (this.wandLevel < wandLevel){
307: this.wandLevel = wandLevel;
379: int dmg = Hero.heroDamageIntRange( 2 + wandLevel, 8 + 4*wandLevel );
383: Wand.wandProc(enemy, wandLevel, 1);
```
