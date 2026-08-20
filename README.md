# CampBoardGameHost

离线优先的 Android 桌游主持/辅助应用。目前代码中包含「谁是卧底」「狼人杀」和 Blood on the Clocktower（血染钟楼）主持流程；当前主要工程重点是 Trouble Brewing（暗流涌动）自动说书人的规则正确性、玩家认知一致性和动态决策架构。

## 当前开发状态

自动说书人正在进行 **Phase A Possible Worlds / epistemic engine 修复与重新验收**。

- A0 / A1 / A1.1 基础语义与外部参考基本稳定；
- A2 / A2.1 需要补 schema-v2 fixture 契约；
- A3 已在 2026-08-19 审计后重新打开，存在 poisoned Spy/Recluse 数字登记语义缺口；
- A4 ZDD 仍是性能/表示层原型，生产不得切换到 `ZDD_DEVICE_VALIDATED`；
- A4.5 observation cache rebuild 已重新打开，需要补 durability、lifecycle cancellation/invalidation 和 cache invariant；
- Phase B / 动态决策下一批在 Phase A 重新通过前保持阻塞。

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
