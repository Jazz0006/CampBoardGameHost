# 当前开发交接

更新：2026-10-17（Australia/Sydney）。

## 当前基线

准备分支已整合 #315 `06dbb954` 与 main `770ed32f`，并保留本会话的最小状态清理规格。生产代码与 #315 原 head 完全相同；本轮只处理分支、归档和文档，不执行新的生产删除。

#315 已有：实际 Session 确认边界、逐步 AI 建议采纳、PlayerCard 只读投影及若干消费者迁移、SDE 历史重建支架删除、Recovery 部分瘦身。详细历史在 [ARCH-CLEANUP-2](ARCH_CLEANUP_2_SINGLE_SESSION_AND_PRACTICAL_RECOVERY_2026-10-16.md)。独立 Recovery、文字记录及部分 App 状态镜像仍存在；不可将它们误写成已删除。

## 当前实施检查点

`codex/remove-independent-recovery` 从准备基线 `704f7b29` 实施第一阶段删除，范围和验证见 [实施记录](RECOVERY_REMOVAL_IMPLEMENTATION_2026-10-17.md)。已删除独立 Recovery、两个入口、快照读写和专属协议；A4 诊断刷新不再依赖存档。正常昼夜规则读取和 Session 裁定仍保留。当前有意不支持异常退出后的游戏恢复，也不迁移或擦除旧存档。

本地完整 JVM 回归与 debug APK 构建通过。远端新 head CI/R2 状态须实时核验；没有真机或真实模型验收声明。#315 仍是准备基线 Draft，不把本阶段叠入其长 diff。

## 下一步

第二阶段先解除文字计数器与动作 ID、Host 夜间确认去重的耦合，再删除独立文字列表、重复归档和专属界面。其后收敛状态与历史请求副本，再审计 App/Host。不要以补齐恢复为删除前提。

旧 main 清单仅是历史：[最小状态清理规格](MINIMAL_GAME_STATE_CLEANUP_2026-10-17.md) 中目标仍有效，但旧 `addClocktowerEvent` 推进事实、从文字解析历史数字等描述已不适用于准备基线。当前 typed 事实与投影应保留。

分支归档与恢复入口见 [分支清理报告](BRANCH_CLEANUP_RESULT_2026-10-17.md)。
