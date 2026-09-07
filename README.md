# CampBoardGameHost

离线优先的 Android 桌游主持/辅助应用。目前代码中包含「谁是卧底」「狼人杀」和 Blood on the Clocktower（血染钟楼）主持流程；当前主要工程重点是 Trouble Brewing（暗流涌动）自动说书人的规则正确性、玩家认知一致性和动态决策架构。

## 当前开发状态

当前正在规划 **D6：Host/App 根的状态与持久化职责拆分**。路线为存档编码、完整恢复解析与应用边界，随后复审夜间状态和命令副作用所有权。具体范围、当前状态与验证证据见开发路线。

Night Step UI / 发布边界拆分已通过 #106 合并，后续 #107/#108 增加并提前安装崩溃记录器，#110 修复投毒者步骤缺席时的时序查询。UI-R5 真机稳定性验收仍需记录；之后继续 EPI-MQ 信息质量与 UX-R6 推荐提供者替换。A4/ZDD 仍不切换到生产。

**开发前请先阅读 [`docs/README.md`](docs/README.md) 和 [`docs/CURRENT_DEVELOPMENT_ROADMAP.md`](docs/CURRENT_DEVELOPMENT_ROADMAP.md)。** 其他设计文档中的旧 `PASS / COMPLETE / READY` 状态如果与当前路线冲突，以这两份入口文档为准。

## 项目结构

- `app/` — Android 应用与测试。
- `docs/` — 当前规范、开发路线、验证参考与历史归档。
- `tools/asp_oracle/` — 冻结 ASP Oracle 的开发/测试工具和 golden fixtures。
- `player/`, `ui/`, `web/` — 项目中的其他客户端/界面资源目录。

## 打开与运行 Android 项目

1. 用 Android Studio 打开仓库根目录。
2. 等待 Gradle 同步完成。
3. 连接 Android 真机或启动模拟器。
4. 运行 `app`。

常用 JVM 回归测试：

```bash
./gradlew testDebugUnitTest --no-daemon
```

ASP Oracle 工具测试：

```bash
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
```

## 文档维护约定

- 当前开发状态只写入 `docs/CURRENT_DEVELOPMENT_ROADMAP.md`。
- 总体认知一致性架构以 v2.2 主规范为准。
- 阶段专项 spec 不能绕过主规范的规则权威和玩家知识边界。
- 已完成、被取代或验收结论失效的文档移入 `docs/archive/`，不再作为新开发入口。
