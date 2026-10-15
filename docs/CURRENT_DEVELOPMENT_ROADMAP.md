# CampBoardGameHost — Current Development Roadmap

> **活动入口（2026-10-15）：[ARCH-CLEANUP-1 整合 Draft #315](https://github.com/Jazz0006/CampBoardGameHost/pull/315)**。开发机分支 `cleanup/arch-cleanup-1-20261015`；PR 已直接指向 `main` 且 GitHub 判定 mergeable/clean，但**未合并、未通过真机端到端验收**。此前 P1 #304/#306/#307/#308 的全部提交均属于 #315 祖先链，这四个以及过时的 #310–#314 已全部关闭、未合并。当前唯一开放 PR 是 #315。旧算法归零 HEAD `0e563911` Android CI #4199、R2 #3815 GREEN；单项语义 V3 HEAD `63b5d111` Android CI #4202、R2 #3818 GREEN。Session-seat 边界 HEAD `ef3438c` Android CI #4203 仍待确认（R2 #3819 GREEN）；最新首夜去酒鬼分支菜单 HEAD `4cb657e` 的 Android CI #4205 / R2 #3821 需独立验收；不把上个提交的 GREEN 冒充最新。

> 2026-10-16 复审更新：基线 HEAD `50a1c05f` 的 CI #4206 / R2 #3822 均 SUCCESS，Real Clingo / ASP SKIPPED。已进一步清理首夜旧 Shadow 候选双轨、旧合法值回退及冗余 UI 观察镜像，并用标准类型化 JSON 输出续接历史；以新代码 HEAD CI/R2 结果为准。**A 权威写入与完整结构化历史仍 PARTIAL；B 模型外发无候选源码合同 YES；C 旧评分/推荐/Shadow 算法已无生产推荐路线，但保留正常 Host 规则、setup diversity、epistemic 正确性。ARCH-CLEANUP-1 NOT CLOSED，#315 继续 Draft，不合并。**

> 2026-10-16 A 专项实施：Session 已持有因果日志、已确认的 Red Herring/Demon bluffs 与对应有序类型化动作；正式身份/死亡 UI 镜像执行 Session 事实前置核验，AI V3 续接使用 Session 的真实首夜设定。**A 仍 PARTIAL**：并非全部根 Compose 事实、信息公布或历史时点重建已收敛。最新 CI/R2 要按实际源码 HEAD 独立验证。仅推进 A，不开启 B/C 清理、P2 新功能或合并 #315。

## 当前唯一任务：完成架构收口和 P1 验收

- **已完成**：多批真实消费链收敛，清除未写入的 DecisionHistory、所有旧 SDE 评分/一致性启发式/Shadow 推荐、角色独立整包评估和 FN-BUNDLE-3 穷举、旧 rollout、UI 文本反解析及无调用的旧模型序列化接口；清理细节见 [ARCH_CLEANUP_1 审计](ARCH_CLEANUP_1_ARCHITECTURE_CONVERGENCE_2026-10-15.md)。
- **保留现役**：整局联合首夜开局 `InitialJointOpeningDirectV2`、Host 合法性验证与采纳、`store:true` 和 Host 确认后的 `previous_response_id` 续接、实际事件/观察/因果决定、Recovery、Archive 与 TBGS V1。首夜模型请求不再输出合法红鲱鱼座位、Demon bluff 清单或任何逐酒鬼合法分支表；后续模型请求已改为 **V3 当前唯一语义结果**，没有模型可见的 `legalCandidates` 或 `candidateId`。Host 内部保留一个当前结果的 `remainingChoices` 选择映射供已有合法展示/采纳入口使用，这不属于外发模型合同；对未知登记见证继续 fail-closed。
- **尚未通过**：真机 8 人阵容手动独立运行、身份展示/首夜信息、Chef 点击显示后的 FIELD-1 退出问题、首夜 Host 战略合同 FIELD-2、AI_ASSISTED/AI_AUTOMATIC 的私有预选/采纳、覆盖事件和次夜、所有确认提名/逐人投票、Recovery 与 ADB 实证。Android CI 不能证明上述实际操作。真实付费模型实验必须另行明确预算授权。
- **本轮边界**：仅清理仍在生产的旧请求语义合同与收敛 Session 写入/读取边界；不新建通用事件引擎、复活角色单点规划、独立选 Drunk、模型 `candidateId` 输出或筛掉投票记录。真实 Host 整局状态、增量历史、缓存与跨局规划仍需后续独立验收；没有批准不进行付费 API 实验。

## 长期产品原则

1. Host 是唯一真实 GameState、规则合法域、事实历史和发布权威；MANUAL、AI_ASSISTED、AI_AUTOMATIC 同源规则，AI 只做资深说书人的全局裁量，Host 独立校验后才能采纳。
2. 阵容确定即开始一次联合开局计划，与展示身份并行；统一考虑本局合法 Drunk、首夜信息、Red Herring、Demon bluffs、玩家经验（未指定时默认 BEGINNER）与跨夜一致性。不能猜尚未发生的玩家行动。
3. **现已实现 V3 当前单项语义输出（仍待真实游戏验收）**：有有效预选直接复用；Host 唯一合法可见结果不调用模型；否则从最后 Host 已确认 Responses 锚点提交全部新增**结构化因果事实**（含人工覆盖、全部提名、逐人投票、能力动作/观察），模型直接输出当前一条数字/真假/角色/配对等语义值，**不是候选 ID**。Host 后验合法性、见证多义、版本/游标与并发校验；无法证明完整增量则提供完整快照，不杜撰因果顺序。
4. 整局战略记忆可修订，不是另一份权威游戏事实；建议、拒绝、真实接受与公布分别记录，已公布信息不可改写。跨局以真实执行历史软去重，优先公平、可推理及新手体验。
5. `ClocktowerGameSession`、`ActionFactTimeline`、`EpistemicObservationLog`、`StorytellerCausalDecisionJournalV1` 继续保留；EvidenceLab/TBGS 只读状态投影不得破坏。Compose `PlayerCard` 的实时编辑尚未全部统一进 Session 写入，不能机械改读可能过时的 Session 对象；Recovery（异常恢复）与 Archive（复盘）故意有不同合同。

## 以后按顺序恢复的工作

| 顺序 | 任务 | 独立验收 |
| --- | --- | --- |
| **P1** | #315 的联合开局/同局 Responses 续接 | 完整 Android CI/R2、Host 合法性、真实 Android Studio/ADB、FIELD-1/2、手动/辅助/自动与恢复 |
| **P2** | AI-EVENT-JIT-1 | 真实增量历史 + 唯一当前语义结果 + 有效缓存零调用 + Host 后置验证与故障接管 |
| **P3** | PLAYER-CLAIMS-1 | 声明来源/可见性/撤回追加事件，禁止当作真实身份 |
| **P4** | CROSS-GAME-STRATEGY-1 | 真实采纳历史软去重、醉酒线索跨夜一致 |
| **P5** | MULTI-NIGHT-FIELD-EVAL | 8/15 人真机与合理预算的模型表现/延迟/成本验证 |

历史全量 roadmap 在 [归档快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_CONTINUITY_SYNC_2026-10-15.md)。详见 [NEXT 交接](NEXT_DEVELOPMENT_HANDOFF.md)、[连续对话设计](AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md)、[TBGS/UGSM 审计](TBGS_UGSM_DECISION_TIME_AND_AI_EVENT_INPUT_AUDIT_2026-10-15.md) 和 [AGENTS.md](../AGENTS.md)。


