# CampBoardGameHost

离线优先的 Android 桌游主持/辅助应用。目前代码中包含「谁是卧底」「狼人杀」和 Blood on the Clocktower（血染钟楼）主持流程；当前主要工程重点仍是 Trouble Brewing（暗流涌动）自动说书人的规则正确性、玩家认知一致性、主持流程稳定性与可维护性。

## 当前开发状态

Persistence Simplification 已完成；D6.1 / D6.2 架构与 UI-composition decomposition 已完成并合入。D6 的可安全、高收益拆分已经结束；R3 对 `CampBoardGameHostApp.kt` 深层事务应用边界的 read-only viability audit 得出 **NO-GO**，因此不再为了缩小文件而继续创建 transaction controller / callback bag / second coordinator。

当前 active campaign 已切换为：

```text
square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

UI-R5 的目标是统一现有方桌 Storyteller 交互语言并完成真机稳定性，而不是修改推荐算法、规则语义或 Persistence/Recovery 架构。完成 UI-R5 并通过真机验收后，才进入 EPI-MQ / Productive Uncertainty。

D6 之后继续保持：`ClocktowerGameSession` 是 canonical writable game/session authority；Planner/Reducer 负责纯语义与 durable intent planning；App 保留跨 owner 的 Compose-facing application choreography。大 composition root 本身不再作为继续拆分的充分理由。

A4/ZDD 仍不切换到 production。

**开发前请先阅读 [`docs/README.md`](docs/README.md)、[`docs/CURRENT_DEVELOPMENT_ROADMAP.md`](docs/CURRENT_DEVELOPMENT_ROADMAP.md) 和当前 active handoff。** 历史 D6/R3 audit、checkpoint 与旧 handoff 已归档；其中的 `PASS / COMPLETE / READY / NEXT` 只作为证据，不控制当前优先级。

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
./gradlew :app:testFast
./gradlew :app:testFull
```

ASP Oracle 工具测试：

```bash
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
```

## 文档维护约定

- 当前开发状态与全局优先级只写入 `docs/CURRENT_DEVELOPMENT_ROADMAP.md`。
- `docs/README.md` 只维护当前默认阅读入口，不复制详细 checkpoint 历史。
- 当前 active handoff 只允许一份；完成或被取代后移入 `docs/archive/handoffs/`。
- 已完成的 slice audit/progress/acceptance 证据移入 `docs/archive/checkpoints/`。
- 总体认知一致性架构以 v2.2 主规范为准；专项 spec 不能绕过规则权威和玩家知识边界。
