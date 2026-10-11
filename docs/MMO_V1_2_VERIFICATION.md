# MMO 1.2：分类背包补齐

启用“大背包”时，新局与读取已有存档都会补齐绒布袋、卷轴筒、药剂挎带、魔法筒袋。主背包加这四种袋子共有五个标签。法杖与投掷武器由魔法筒袋共用，不另造分类袋。

按类型检查已有袋子，重复读取不重复赠送。通过原版 collect/grabItems 流程移动道具，保留快捷栏、法杖充能与投掷武器筒袋效果，并同步商店袋子生成标志。遗失背包状态下暂不补发。未开启大背包时不赠送。

验证：

- 85 项回归检查通过，其中 7 项检查分类袋的按挑战启用、保留现有袋子、自动归类、筒袋机制、重复调用与商店标志。
- Android 调试构建成功，覆盖安装到 emulator-5554。
- 实际加载用户当前 game1 存档，背包下方出现五个标签，能切换魔法筒袋。
- 验证前后角色位置 595、HP 9/20、挑战掩码 523776、147 个怪物不变。
- 加载与背包切换期间 AndroidRuntime 无致命异常。
- 安装前备份：.research/shattered-runtime/mmo-v1-2-before-category-bags.tar。
- 截图：.research/shattered-runtime/v1-2-all-bags.png、v1-2-holster.png。

APK：release/Shattered-4.0.2-MMO-v1.2-debug.apk（位于仓库上级工作区）。仅提交本地 Git，不推送。
