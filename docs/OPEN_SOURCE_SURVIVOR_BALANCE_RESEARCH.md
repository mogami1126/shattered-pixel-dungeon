# 开源割草游戏数值源码调查（2026-10-13）

仅静态研究公开源码，没有运行第三方项目或改变游戏模型。三个仓库均查到 MIT 许可文本；Canvas 项目 GitHub API 将许可证识别为 other，但 LICENSE 内容标明 SPDX MIT。小型独立项目／开发模板，不将其当成商业游戏的已验证平衡标准。源码下载副本位于工作区 .research/survivor-source-audit，SHA256.json 记录已下载文件。

## 1. Python / Pygame Vampire_Survivors-clone

仓库：https://github.com/yagizkoryurek/Vampire_Survivors-clone
本次 API tree SHA：bd40c610ca9775ca2f4547dcc516fb257145e20f。
源码：https://github.com/yagizkoryurek/Vampire_Survivors-clone/blob/main/main.py

- Player.gain_xp：首级需求10，升级后需求 floor(前次需求*1.3)，扣除消耗经验，余量保留。经验换三选一升级，不是一经验等于一属性。
- 基础怪 HP40、伤害8、XP5；快怪20/5/8；坦克160/8/28。经验按类型固定，生命与伤害变强时经验不自动一起变强。
- wave=floor(t/30秒)，普通怪 HP=floor(H0*(1+.09wave))，伤害=floor(D0*(1+.02wave))。刷怪普通间隔 max(20,120-5wave) 帧；每60秒额外刷20+5wave只。另有精英与特殊怪定时刷怪。
- Aura 初始伤害8、半径80、间隔40帧；每次升级伤害+4、半径+15、间隔减5帧，最低20帧。对范围内每个敌人伤害，不是均分伤害。
- 四次Aura升级：伤害24，半径140，间隔20。单目标持续输出为原来6倍；均匀且足够高敌人密度下覆盖面积约3.06倍，两者相乘为约18.38倍总伤害潜力，不是保证18.38倍击杀或经验。供怪、过量伤害、接敌时间都会限制收益。
- 护甲加点每次+2，生命每次+40，回血每次+1/秒，移速每次乘1.1。受伤后45帧无敌，固定FPS60时对应约.75秒，不能将每只接触怪的伤害全部并发相加。

## 2. Canvas Vampire Survivors

仓库：https://github.com/tangdan2204/canvas-vampire-survivors-
本次 API tree SHA：16173c135fe09d62afc1526590c11e8e21545cf4。
公式：https://github.com/tangdan2204/canvas-vampire-survivors-/blob/main/src/weapons.js
角色：https://github.com/tangdan2204/canvas-vampire-survivors-/blob/main/src/entities.js
敌人：https://github.com/tangdan2204/canvas-vampire-survivors-/blob/main/src/main.js

- 升级需求从50开始，每级 floor(前次*1.2)，扣除经验后保留余量。
- 单次伤害 D=D0*[1+.2(L-1)]*玩家伤害倍率，另处理进化及暴击。
- 冷却 C=C0*.92^(L-1)*玩家冷却倍率，另处理进化；基础玩家冷却被动每层乘.92，玩家被动合计下限.2（不等于武器最终冷却下限.2秒）。
- 范围 R=R0*[1+.1(L-1)]*玩家范围倍率；环绕弹体还会随武器等级增加数量，最终数量不超过12。
- 伤害、范围、经验被动各每层乘1.1；另有暴击、磁吸、回血等升级。
- 敌人生命与伤害时间倍率是 1+.3*floor(t/60秒)，再乘难度与场景修正。这是线性阶梯，不能按 BALANCE.md 中另一个 1.3^分钟 的说法处理。
- 刷怪间隔 max(.2,1.2-t/200)/(难度刷怪倍率*波次倍率)，活跃怪上限 min(CONFIG.MAX_ENEMIES,20+floor(t/10))。供怪效率独立于单体怪物属性。
- 受击无敌.5秒，护甲减法至少剩1伤害，另有减伤和闪避上限。它采用若干上限，与用户当前追求无上限的模式不同。

## 3. Godot C# SurvivorsStarterKit

仓库：https://github.com/DarkRewar/SurvivorsStarterKit
本次 API tree SHA：a26c6cbc58bf0d693edd6966c19634459938b66c。
经验：https://github.com/DarkRewar/SurvivorsStarterKit/blob/main/Scripts/GameManager.cs
射击：https://github.com/DarkRewar/SurvivorsStarterKit/blob/main/Scripts/Players/Shooting.cs
敌人：https://github.com/DarkRewar/SurvivorsStarterKit/blob/main/Scripts/Enemies/EnemyManager.cs

- 当前等级n的升级需求 round(50log10(n)+5)：1级5，10级55，100级105。升级选择结束直接清空经验，超额不保留。
- 射击默认伤害5、频率1/秒；伤害升级+1，攻速升级+.1/秒；冷却等于1/总频率。对应资源限制每种最多选6次。
- 敌人HP=基础HP*round(log10(10n))+额外HP奖励，每次敌人生命升级+5；每次玩家三选一同时附带一个敌人升级，可以改变怪种、血量、伤害、移速、刷怪频率或生成Boss。
- 这是玩家升级带动敌人提升的方案；它可能抵消刷强后的领先感，和用户此前不希望敌人追赶玩家的方向不同。
- UI资源描述写百分比，但Shooting实际为加算；引用实际代码，不照抄描述。

## 对经验即点数模型的启发（研究者推导）

经验收入/秒 = 实际击杀数/秒 * 每只经验 * 收集率。

足够密集、低过量伤害的场景中：击杀潜力/秒约等于每击伤害*攻击次数/秒*每击有效目标数/敌人平均HP；但供怪不足时受可击杀敌人数限制，严格计算还要考虑ceil(HP/伤害)和命中、移动、控制。不能把这项连续估计当单击秒杀公式。

至少需要四条分别可调曲线：经验产出；经验兑换强化；强化对输出/覆盖的作用；敌人血量、危险度与数量。多个线性属性乘在一起，可以产生很强的整体非线性，无需每项属性都指数增长。AOE、穿透和多弹体直接影响经验收入，应进入成长模型。

本轮不把此前装备升阶模型替换为新实现；先保留研究结论。
