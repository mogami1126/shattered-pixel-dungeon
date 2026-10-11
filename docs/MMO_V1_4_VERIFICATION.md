# MMO 1.4：等级和诅咒信息揭示

启用装备吞噬时，拾取可升级物品揭示 levelKnown、cursedKnown。合并后的目标同样保持两项已知，合并前高等级物品的真实诅咒状态仍按原规则继承；揭示不会解除诅咒。不调用 identify，不学习戒指类型，不改变法杖 curChargeKnown。正常装备与鉴定进度继续按破碎规则处理。

已有存档在读取与整理装备时同步揭示两项基础信息，包含已经装备的物品。未开启装备吞噬时不改变原版鉴定流程。

验证：109 项回归检查通过，Android 构建成功。MMO.1.4 已覆盖安装到 emulator-5554，实际加载 game1 并打开背包；原先隐藏的防具等级正确显示。安装前后装备等级不变，所有普通装备的等级和诅咒信息已知，法杖 curChargeKnown 和全部戒指类型记录不变。角色位置 288、HP 7/40、92 个怪物不变，没有推进回合；AndroidRuntime 无致命异常。

备份与截图位于工作区 .research/shattered-runtime：mmo-v1-4-before.tar、mmo-v1-4-after.tar、v1-4-revealed.png。构建日志 .research/mmo-v1-4-build.log。APK 为工作区 release/Shattered-4.0.2-MMO-v1.4-debug.apk。

仅提交本地 Git，不推送。
