# 当前开发交接

> **2026-10-24 真机修复更新：** #361/#362 与恶魔掩饰修复 #363 已合并，当前本批基线为 `492ecd267`。后续夜间重复预览、虚线、唤醒箭头、身份信息隐私/唯一结果、厨师小屏与认知范围、传承方桌及继任攻击修复见[完整问题记录](DEVICE_NIGHT_FLOW_FIXES_2026-10-24.md)。本地完整 644 项和 APK 通过，用户已授权本批合并与同步；以下 Batch 5 待合并状态为旧检查点，执行前以实际 Git/PR 为准。

1. 先核对实际 Git HEAD、工作区及 PR 状态。main 基线 `17949e72`；Batch 5 #361 仍为 Draft，代码 `05f6a21f` 已通过 full CI #4612 / R2 #4161。
2. 当前文档分支叠加于 Batch 5，新增 [共享说明](GAME_SESSION_SHARED_GUIDE.md)，归档顶层旧路线并修复引用。审阅本次增量时以 Batch 5 分支为 base；不擅自合并或改写历史。
3. 用户确认的模型：一局 GameSession，保存布局/配置与按顺序发生的行动和实际信息；说书人理由属于可选附加说明。EvidenceLab 采集同样过程，WebHost 按相同语义实施。
4. 当前续局使用当前状态+已确认记录；AI 对话 ID/游标/记忆在 metadata 中。独立初始布局副本、通用逐步备注、统一夜间手动接管及人工触发随机尚不能视为已实现。以后优先保证断网能继续游戏。
5. Session 唯一写事实；无持久 UI pending、第二游戏对象或独立恢复系统；D2 预览不入账，Next 保存最后一次展示。保留确定规则、隐私、原子保存、两 builtin 与现有 AI。
6. 仅文档变更检查引用和差异即可，不把 docs-only CI 当作新代码测试。真机/付费模型未验收；无后续合并或付费调用授权。

[当前路线](CURRENT_DEVELOPMENT_ROADMAP.md) · [旧累计交接](archive/legacy-host/PRE_SHARED_GUIDE_NEXT_DEVELOPMENT_HANDOFF.md) · [归档清理清单](DOCS_CLEANUP_2026-10-24.md)。
