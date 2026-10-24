# 当前开发路线

> **2026-10-24 真机修复更新：** #361/#362 与恶魔掩饰修复 #363 已合并，当前本批基线为 `492ecd267`。后续夜间重复预览、虚线、唤醒箭头、身份信息隐私/唯一结果、厨师小屏与认知范围、传承方桌及继任攻击修复见[完整问题记录](DEVICE_NIGHT_FLOW_FIXES_2026-10-24.md)。本地完整 644 项和 APK 通过，用户已授权本批合并与同步；以下 Batch 5 待合并状态为旧检查点，执行前以实际 Git/PR 为准。

- 已合并：Host UI Batch 3 #359（`2aa7abbf`）、Batch 4 #360（`17949e72`）；main CI #4611 / APK #175 通过。
- 待审阅：Batch 5 [Draft #361](https://github.com/Jazz0006/CampBoardGameHost/pull/361)，`05f6a21f`；完整 CI #4612 / R2 #4161 通过。[验收](HOST_UI_BATCH5_ACCEPTANCE.md)。未获进一步合并授权。
- 本次文档工作：完成 [GameSession 简明共享说明](GAME_SESSION_SHARED_GUIDE.md)，清理文档入口及历史文件位置，不改运行代码或存档格式。
- 下一项功能优先级：异常恢复与 AI 不可用时仍能完成本局——统一夜间手动接管入口、人工触发的可选随机、恢复后 ID/游标/差额事件及过期响应验证。尚未实现，不能用“请手动继续”的错误提示代替可用入口。
- 之前 Batch 6 的跨项目 JSON 工作收缩为上述简明参考说明；跨语言 SDK、大规模兼容平台和 EvidenceLab 评分/训练体系不是当前前提。未来具体消费者需要时再增加少量互通样本。
- 真机视觉、来电/后台/杀进程及付费服务验证仍单列待办。

约束：[最小事实合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)、[重构合同](MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md)。[旧累计路线](archive/legacy-host/PRE_SHARED_GUIDE_CURRENT_DEVELOPMENT_ROADMAP.md) 保留追溯，不能作为当前 NEXT。
