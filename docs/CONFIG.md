本分支支持 Minecraft 1.21.10，NeoForge 版本范围为 `[21.10,)`，使用 Java 21。

铁砧允许将附魔书应用于任意可保存附魔的物品，并允许原本互斥的附魔共存。相同等级附魔可以继续合并至 255 级，有效铁砧操作最多消耗 1 级经验；原版无效输入仍不产生结果。

保留 11 至 255 级附魔的罗马数字显示。本模组没有额外配置项。

构建：`./gradlew.bat compileJava build --console=plain`。游戏测试：`./gradlew.bat runGameTestServer --console=plain`，覆盖物品限制、冲突附魔、高等级合并、铁砧费用和实际修复操作。测试代码仅在 GameTest 运行中加载。

构建产物为 `build/libs/no_enchancement_conflict-0.0.1-mc1.21.10.jar`。
