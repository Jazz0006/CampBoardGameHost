# 当前开发交接

更新：2026-10-17（Australia/Sydney）。

## 当前检查点

准备基线 #315 `704f7b29`；独立 Recovery 删除 #316 `ef076287`；文字/整局归档删除 #317 `60f61c3b`。#316/#317 完整 CI/R2 已通过，三个 PR 仍未合并。

C1 #318 `fb663d6a` 已完成幽灵票、phase/round 单源投影及 journal 请求副本/旧恢复支架删除，完整 CI/R2 通过，仍未合并。

C2 分支 `codex/converge-night-vote-state`（#319，`9769f8a4`）基于 #318。C2 将已确认攻击/保护/市长转移/待生效继任人和当日最高票/平票归 Session；删除 App/Host 写副本及旧 checkpoint reducer/transaction、UI vote transaction。具体证据和测试退休映射见 [C2 实施](NIGHT_VOTE_OWNERSHIP_IMPLEMENTATION_2026-10-17.md)。远端检查和合并状态以当前 PR 实时状态为准。

第四阶段审计分支：`codex/stage-d-app-host-audit`，基于 C2；本检查点仅改文档。

## 当前第四阶段 D1 / 下一检查点 D2

D1 只读审计已记录于 [第四阶段审计](APP_HOST_STAGE_D_AUDIT_2026-10-17.md)。下一步 D2 删除手动酒鬼未消费 provider request 和 App 无来源的 Legacy 对局分支，再按结算、白天交互、夜间信息与 AI 生命周期拆分。App 4,065 行 / 234,187 bytes，Host 3,022 行 / 179,448 bytes；大文件治理尚未完成。不能通过搬运巨型回调或新增 God Context 宣称完成。

仍保留的夜间 checkpoint 仅是规则 planner 的临时输入，未存入 Session 或磁盘。夜间空目标确认、依赖失效和投票时门槛尚未全部进入事件记录，不能用最后一条动作冒充完整有效状态。后续补事件覆盖和最小继续游戏时明确处理这些缺口，不复活旧存档框架。

## 缺口

仍无退出恢复、完整文字复盘或跨局完整归档；不读取、迁移或擦除旧设备数据。现有结构化动作/实际观察/正式裁定保留，但事件覆盖尚不完整，不能称 AI JSON 可直接恢复游戏。开局轮换历史和 Undercover 正常淘汰/轮数继续保留。没有真机或付费模型验收。
