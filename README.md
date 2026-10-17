# CampBoardGameHost

离线优先的 Android 桌游主持应用。当前代码中的游戏类型为血染钟楼和谁是卧底；主要研发围绕 Trouble Brewing 的规则、游戏流程及 AI 说书人推荐。

## 当前方向（2026-10-17）

**先做减法：删除独立存档恢复、重复文字记录和冗余状态，再审计超大源文件。** 游戏中断后的最终需求只是恢复身份与足以继续游戏的进度；未来恢复、AI 上下文和文字展示应共用权威游戏记录。

当前完成的是本地审计与路线纠正，生产删除尚未实施。清理允许暂缺恢复与部分历史展示，不以先建完替代系统为条件保留旧代码。规则合法性、正常手动游戏与已确认事实仍需保护。

AI 长期方向仍是一次联合开局规划和基于确认事件的后续修订；完整自动主持和手机端到端验收不能据现有实验代码宣称完成。

## 权威入口

- [开发规范](AGENTS.md)
- [当前路线](docs/CURRENT_DEVELOPMENT_ROADMAP.md)
- [当前交接](docs/NEXT_DEVELOPMENT_HANDOFF.md)
- [本轮清理规格与审计](docs/MINIMAL_GAME_STATE_CLEANUP_2026-10-17.md)
- [文档索引](docs/README.md)与[测试策略](docs/TESTING_STRATEGY.md)

## 工程结构与运行

`app/` 是当前唯一 Gradle 模块，包含 Android UI、开局生成、规则、Session、推荐及持久化。`tools/` 存放 Gateway、实验和 ASP 验证工具；`player/`、`ui/`、`web/` 为其他客户端或界面资产，未作为独立 Gradle 模块接入。

用 Android Studio 打开仓库根目录。Android JVM 检查：

```bash
./gradlew :app:testFast
./gradlew :app:testFull
```

ASP 检查：

```bash
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
```

真实模型调用须另有明确预算授权，不隐含在本地清理或 CI 中。
