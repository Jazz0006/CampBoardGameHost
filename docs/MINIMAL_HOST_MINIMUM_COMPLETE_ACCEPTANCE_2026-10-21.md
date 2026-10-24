# 最小完整游戏流程验收（2026-10-21）

> **后续状态**：阶段一的代表性整局自动化验收结论仍有效；此后 #334/#335、全仓清理 #337 均已合并到 `main`（2026-10-21 复核 HEAD `6230b595`）。本页保留当时的验收证据及当时的进度/文件大小作为历史，不表示现行尚需全仓清理。第三阶段缺口见 [当前路线图](CURRENT_DEVELOPMENT_ROADMAP.md) / [独立审计](archive/legacy-host/MINIMAL_HOST_POST_MERGE_CODE_AUDIT_2026-10-21.md)。

## 结论与范围

基于唯一 `ClocktowerGameSession` 的最小完整流程已实现。本批从生产首页、玩家/座位设置、真实剧本配置和生产发牌入口验证，补齐了发牌初始存储与恢复后再开局两个阻断。Cloud full 654（147 suites / 38 UI，零失败/错误/跳过）、Debug APK、原 R2 shell 和 diff 检查已通过；生产代码 `e73e4807691ab974dddfa86532a30efe89973753` 的[完整 Android CI #4503](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618033) 与 [R2 #4079](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618124) 均 SUCCESS，已独立核对全量 Android/APK 上传、ASP、Clingo 和 gate；全部通过，第一阶段已关闭，下一阶段为全仓遍历/大规模删除，界面细节最后优化。

这里的完成指两内置剧本代表性真实 App 路径，以及必要角色分支/人数和保存合同的自动化验收；不等于穷举全部阵容、所有视觉细节或真机/实际杀进程/付费模型验收。不存在本批已确认但未修复的整局阻断。

## 本批修复

1. **发牌时已有唯一存档**：原先只在 Judge 页面绑定 persist，发牌时虽然 Session 已创建但尚无存档。本批 Session 创建后立即绑定原 AtomicFile 保存；移除 screen 门槛、screen effect key 和无用 activeGameSaveAttached 引用。页面切换不再成为是否保存的依赖。
2. **中断发牌安全续局**：恢复首夜且没有已确认动作/信息的 Session 时，从同一角色名单重新逐人展示；之后恢复由已确认事实推导的步骤。没有持久发牌游标、第二恢复器或 UI 草稿存档。已确认的角色和 gameId 不变。
3. **恢复后可以原班再开一局**：原 quick restart 在旧准备页 confirmedSeating 丢失后抛出 `Production start requires confirmed seating`。现在先从当前 Session 的规范座位/姓名取得下一局的确认名单，再清旧局并准备下一局；不依赖恢复前的准备对象。保留原剧本与两项跨局轮换链。
4. **可复现但不替代生产逻辑**：把原 UUID seed 函数移为内部 App 的可传入参数，默认行为不变；测试只固定随机源。实际角色、路由、Session、输入回调与 AtomicFile 全部使用生产实现，无注入 GameState 或虚假游戏回调。

## 整局证据

`ClocktowerCompleteAppUiTest` 新增 3 项真实 App Compose/Robolectric 回归，修复前全部失败：两条完整轨迹均到终局后在恢复后的 quick restart 抛座位异常；发牌中断用例因初始文件尚未保存超时。修复后三项通过。

| 环节 | 生产入口与验证 |
| --- | --- |
| 创建/名单/座位/剧本/发牌 | 首页 → 添加 5 名玩家 → 确认座位 → 选 TB/NGJ → 正式发牌 → 每名玩家实际 Show identity/Hide/Next；角色来自生产准备器与合法内置配置 |
| 首夜 | TB seed 34（Chef/Empath/Slayer/Scarlet Woman/Imp）与 NGJ seed 26（Clockmaker/Artist/Sage/Scarlet Woman/Imp）；当前信息展示、返回、最终 Next 全走真实 App，无额外开始页面 |
| 白天 | 真正长按拖动方桌座位发起提名 → Start voting → 玩家逐座投票 → Confirm vote → 同页 End day；没有直接调用 Session 的测试输入替代手势 |
| 后夜/死亡 | 处决 Scarlet Woman 后进入第二夜；Imp 实际点选并 Next 确认杀死另一玩家，进入第二个白天，私有文件中的 Death 与生死一致 |
| 恢复/继续 | 销毁 App composable、仅留真实私有文件，再创建全新 App；状态完全相同，从同一 GameId 接着提名/投票/处决 Imp |
| 终局/隐私 | 唯一 GameEnded(NO_DEMON)；终局不先显示完整名单/角色，明确点击揭晓后才进入收尾；结束文件重载保持一致 |
| 下一局 | 从恢复的终局实际点击 Quick restart，同一玩家/座位与原剧本，新 gameId；TB 实测下一阵容不重复 |
| 发牌中断 | 第一名玩家看完身份后卸载 App composable；重新加载仍有原 Session，安全重新发牌并进入首夜，不需要 setupCards 镜像 |

App composable 重建是本地自动化恢复证据，不冒称 Android 真机被杀进程。

## 已有必要分支与同一对象合同

- `ClocktowerNightFlowTest`：两剧本固定夜序、Drunk 展示身份、5–15/NGJ 5–6 人配置轨迹、真实条件唤醒/跳过、Ravenkeeper/Sage、红鲱鱼、Spy/邪恶信息与继任通知。不是所有合法阵容穷举。
- `ConfirmedNightOperationTest` / `ConfirmedNightAttackTest` / 毒边界及来源退出回归：本步确认、保护/中毒/攻击/死亡/市长/Imp 自杀继任、重复/过期隔离；机械效果来自 Session。
- `ClocktowerDayFlowTest` / `LiveKlutzScriptReachabilityTest`：提名、投票、鬼票/管家、Virgin、Slayer、Artist、Klutz、特殊终局与正常终局；无独立日结页/呆瓜 learned 事件。
- `ClocktowerD2UiTest`：全部现有真实 Host UI（含两代表性整局）读取同一 Session，不提供 setupCards，预览不是事实、仅最终 Next 记录一次。
- `ClocktowerActiveGameStoreTest` / Session 各边界：真实 AtomicFile 中断/保存失败/重读、整批回滚、规范身份/顺序/接收者和幂等。
- 原 AI 首夜/后夜、模式、记忆、receipt/stale、两项跨局轮换测试保留；本批未调用付费 API。AI/剧本元数据/存储是适配层，不是并行事实来源。

## 验证与规模

Cloud full **654 tests / 147 suites / 38 UI，0 failure/error/skipped**、Debug APK、原 R2 shell 与 diff 检查通过；生产代码 `e73e4807691ab974dddfa86532a30efe89973753` 的[完整 Android CI #4503](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618033) 与 [R2 #4079](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39565618124) 均 SUCCESS，已独立核对全量 Android/APK 上传、ASP、Clingo 和 gate。前一生产基线为 `3268440f` / 651 / CI #4501 / R2 #4077，不替代本批证据。

本批生产仅 App +15/-12，净 +3 行 / +316B（必要闭环修复）；新增 1 测试文件 / 160 行 / 3 个高价值用例。删除生产/专属测试文件均 0，无新生产文件、持久字段、状态对象或通用框架。App 116000B、Night Host 63787B 仍超 50000B，Session 47578B；这些归全仓清理阶段收敛。原 #335/#334 已按所有者授权合并；main `40050941efc3` 的完整 CI #4506 与 Field Test APK #157 均成功，第一阶段关闭。旧 Draft/不得合并记录不再适用。
