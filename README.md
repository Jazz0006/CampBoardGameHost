# CampBoardGameHost

离线优先的 Android 桌游主持/辅助应用。目前代码中包含「谁是卧底」「狼人杀」和 Blood on the Clocktower（血染钟楼）主持流程；当前主要工程重点是 Trouble Brewing（暗流涌动）自动说书人的规则正确性、玩家认知一致性、主持流程稳定性与可维护架构。

## 当前开发状态

当前 active campaign 是 **Persistence Simplification / Recent Emergency Recovery**。

产品需求已经重新确认：进行中的游戏只需要应对来电、切换 App、进程回收、崩溃或误关闭后的短时恢复；**不需要长期 Save Game，也不要求今天保存、明天继续，或跨 App 版本精确恢复整个 UI/runtime**。

因此当前工作不属于 D6 大文件拆分。实施顺序调整为：

1. 冻结 Recent Emergency Recovery 产品合同；
2. 将 active recovery 与长期 archive/history 分离；
3. 建立最小 typed `RecoverySnapshot`，只保存已提交游戏事实和必要 continuation；
4. 简化 restore：恢复游戏，不恢复整个 App UI；
5. 删除旧 active-save compatibility、preview、重复 checkpoint/draft persistence 等遗留路径；
6. 最后审计 persistence trigger，避免无意义的整棵运行时频繁同步写盘。

Night Step D1–D5 已完成并通过 PR #106 合入；后续 crash/initialization/Poisoner 修复 #107、#108、#110 也已进入 `main`。原 D6 planning PR #111 已关闭且不合并，因为它基于“完整存档/恢复”前提；Persistence Simplification 完成并合入后，再对缩小后的 App/Host 重新做一次独立 D6 ownership/decomposition audit。

之后的总体顺序是：

```text
Persistence Simplification
-> fresh D6 App/Host ownership audit + decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 recommendation-provider replacement
```

A4/ZDD 仍不切换到 production。

**开发前请先阅读 [`docs/README.md`](docs/README.md) 和 [`docs/CURRENT_DEVELOPMENT_ROADMAP.md`](docs/CURRENT_DEVELOPMENT_ROADMAP.md)。** 其他设计文档中的旧 `PASS / COMPLETE / READY / NEXT` 状态如果与当前路线冲突，以这两份入口文档为准。

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