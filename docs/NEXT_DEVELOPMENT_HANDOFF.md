# 当前开发交接

更新：2026-10-17（Australia/Sydney）。

## 当前检查点

准备基线 #315 `704f7b29`；第一阶段 #316 `ef076287` 已移除独立 Recovery，完整 CI/R2 通过。两个 PR 仍未合并，不把后续重构混入其 diff。

第二阶段分支 `codex/remove-duplicate-game-history` 基于 `ef076287`。已删除 ClocktowerEvent、文字计数器、独立整局归档、复盘/历史入口、夜间文字摘要及专属协议。正式序号读取实时 Session 全局游标；管家同夜确认去重归 Session；玩家揭示保留正式观察提交顺序。完整范围见 [实施记录](TEXT_HISTORY_REMOVAL_IMPLEMENTATION_2026-10-17.md)。

本地完整 JVM 1,383 项、FAST 1,376 项和 debug APK 构建通过。新 head 远端 CI/R2 须实时核验。没有真机或真实模型验收声明。

## 明确缺口与保留

当前无异常退出恢复、整局文字复盘或跨局完整游戏归档，不读取、迁移或擦除设备旧存档。事件覆盖仍不完整，不能宣称 AI wire JSON 可用于重建。现有结构化动作、实际观察和已确认裁定保留；Undercover 淘汰记录仍供其正常游戏界面使用。开局轮换历史只服务多样性，不是整局副本。

## 下一阶段

按真实生产调用图收敛重复可写状态和精确历史请求副本。特别检查 App confirmed night fields / phase / round / ghost vote 与 Session 的归属，以及 causal journal 冻结请求与实际裁定的边界；不要因名称含 restore/snapshot 就整类删除。`ClocktowerGameSession.restore(GameSnapshot)` 仅用于现有测试，需迁移必要测试语义后退休；journal typed restore 仍服务原子提交。

App 目前约 241 KB，Host 约 180 KB，仍须在继续减法后按职责审计，不能认为大文件问题已解决。实现最小中断恢复在减法之后，不新增替代快照体系。

旧 main 清单仅是历史：[最小状态清理规格](MINIMAL_GAME_STATE_CLEANUP_2026-10-17.md) 中产品目标有效，具体残留以当前源码为准。分支归档见 [清理报告](BRANCH_CLEANUP_RESULT_2026-10-17.md)。
