# 当前开发交接

更新：2026-10-17（Australia/Sydney）。

## 当前检查点

准备基线 #315 `704f7b29`；独立 Recovery 删除 #316 `ef076287`；文字/整局归档删除 #317 `60f61c3b`。#316/#317 完整 CI/R2 已通过，三个 PR 仍未合并。

C1 #318 `fb663d6a` 已完成幽灵票、phase/round 单源投影及 journal 请求副本/旧恢复支架删除，完整 CI/R2 通过，仍未合并。

C2 分支 `codex/converge-night-vote-state`（#319，`9769f8a4`）基于 #318。C2 将已确认攻击/保护/市长转移/待生效继任人和当日最高票/平票归 Session；删除 App/Host 写副本及旧 checkpoint reducer/transaction、UI vote transaction。具体证据和测试退休映射见 [C2 实施](NIGHT_VOTE_OWNERSHIP_IMPLEMENTATION_2026-10-17.md)。远端检查和合并状态以当前 PR 实时状态为准。

第四阶段 D1/D2：#320 `b3c0f248`，完整 CI/R2 已通过，未合并。D3a #321 `1a3e34c7` 的完整 CI/R2 已通过，未合并。D3b #322 `1b5effe3` 完整 CI/R2 已通过，未合并。D4a #323 `16f32d8e` 已推送，检查以实时 PR 为准。当前 D4b 分支 `codex/extract-registration-projection` 基于 #323。

## 当前第四阶段 D4b / 继续 D4

D1 审计见 [第四阶段审计](APP_HOST_STAGE_D_AUDIT_2026-10-17.md)。D2 已删除手动酒鬼未消费请求字段/工厂及 App 旧模式分支，见 [实施记录](APP_LEGACY_PATH_REMOVAL_D2_2026-10-17.md)。D3a 已统一三条中毒结算入口并删除重复恢复查询与中间计划，见 [实施记录](POISON_SETTLEMENT_D3A_2026-10-17.md)。D3b 已删除死亡、身份和阶段中间计划，直接使用现有原子 Session 边界，见 [D3b 记录](DAWN_SETTLEMENT_D3B_2026-10-17.md)。D4a 已独立 Artist 交互与草稿，见 [实施记录](ARTIST_INTERACTION_D4A_2026-10-17.md)。D4b 已独立共用只读登记投影，见 [实施记录](REGISTRATION_PROJECTION_D4B_2026-10-17.md)。继续处理提名/投票交互和白天早分流；后续拆夜间和 AI 生命周期。App 3,768 行 / 212,701 bytes，Host 2,834 行 / 169,852 bytes；大文件治理尚未完成。

仍保留的夜间 checkpoint 仅是规则 planner 的临时输入，未存入 Session 或磁盘。夜间空目标确认、依赖失效和投票时门槛尚未全部进入事件记录，不能用最后一条动作冒充完整有效状态。后续补事件覆盖和最小继续游戏时明确处理这些缺口，不复活旧存档框架。

## 缺口

仍无退出恢复、完整文字复盘或跨局完整归档；不读取、迁移或擦除旧设备数据。现有结构化动作/实际观察/正式裁定保留，但事件覆盖尚不完整，不能称 AI JSON 可直接恢复游戏。开局轮换历史和 Undercover 正常淘汰/轮数继续保留。没有真机或付费模型验收。

持续授权：本次重构后续本地提交、推送到 Jazz0006/CampBoardGameHost 的 codex/* 分支、Draft PR 创建/更新及 CI/R2 无需逐次确认；强制推送、删除远端分支和合并须另行确认。
