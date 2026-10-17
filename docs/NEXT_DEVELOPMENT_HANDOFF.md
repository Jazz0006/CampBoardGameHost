# 当前开发交接：进入最小 Host 新轮次

更新：2026-10-17（Australia/Sydney）。先读 AGENTS.md 顶部与 [产品合同](MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md)，再读 [当前路线](CURRENT_DEVELOPMENT_ROADMAP.md)。不得按旧 D5 NEXT 继续拆分规则模块。

## 已有基线

本轮生产末端为 #328 `db16d38c`；它包含 #315–#327 的全部提交。#315–#328 各最新 head 的 CI/R2 已核对成功。收尾集成分支为 `codex/minimal-host-handoff`，面向 main 整合现有链和新权威文档；最终合并状态以 GitHub 为准。下一轮必须从该收尾已合并的 main 建立新分支，不能从旧 main `770ed32f` 开始。

已删除独立 Recovery、重复文字/整局归档及部分状态副本；Session 是现有游戏事实单源。App 3,768 行 / 212,701 bytes，Host 2,616 行 / 159,659 bytes；Host 仍有 90 参数、40 个操作回调，大文件治理未完成。

## 最新产品决定（覆盖旧文档）

删除 Host 游戏规则校验、线索计算、合法候选枚举、登记解释推导和手动规则/线索辅助。AI 推荐什么，实际确认什么，就记录并展示什么。保留玩家/座位/对局身份、原子记录、重复确认防护、过期响应隔离和私密信息保护；规则正确性由说书人承担。

旧信息发布函数会按角色从准备变量回推记录；下一轮应重新审计并删除这种不必要的耦合，不把它原样包装成新模块。旧 candidate/semantic gate/provider 请求结构有深层依赖，必须成链清理。现有 AI 自动模式不因新方向而获得任意改状态权限。

## 2026-10-18 真实实施检查点

- **M0 已通过**：#330 `codex/minimal-host-reset` HEAD `946b23b`，CI #4312 与 R2 #3892 成功；Undercover（谁是卧底）全链已删除，净 -1039 行，含独立选择页。Draft 尚未合并 main。
- **M1a 已实施，待最新 Android CI 最终验收**：堆叠 Draft PR #331，基于 #330，分支 `codex/minimal-host-m1-publication-20261018`。删除信息展示前 Spy/Recluse 合法登记见证解释与阻断、App 层登记规则写入回调、无消费者的 `ClocktowerAutomaticRegistrationEffect.kt` 及其旧 R2 守卫和对应无效测试。真实私密展示仍通过 Session 的 confirmed observation 入口，保留去重、旧查询不冲突、过期响应隔离、私密性。
- 本次使用 GitHub Connector 直接小步提交；本地开发机 `clocktower` 可保持在 M0 工作树，**不能仅凭旧 HEAD 断言 M1 的本地 Git 状态**。

## M1b Session 第一批实施与风险（2026-10-18）

Draft PR #332 基于 #331、代码分支 `codex/minimal-host-m1b-session-20261018`。已删除 `ClocktowerConfirmedRegistrationPublicationV1.kt`、`ClocktowerConfirmedRegistrationHostWriterV1.kt`、`StorytellerRegistrationRulingProducerV1.kt` 和相关专属旧测试；`ClocktowerGameSession.commitConfirmedPrivateInformationPublication` 已移除，App 直接通过现有唯一 `commitGlobalEpistemicObservation` 提交。并清理不再使用的手动登记见证匹配函数及其测试。保留 Session 事实记录、去重和时间线结构防护，不将“显示的值”反推为登记见证。

首个代码 HEAD `8f9ac02` 完整 CI #4318/R2 #3898 均成功；补充 HEAD `c124e70` 的 CI #4319 与 R2 #3899 以实际最新结果为准。新分支尚未合并 #331、#330；这三个 Draft PR 均为堆叠结构。

下一任务 M1b 第二轮必须处理所有人工信息结果形态（数字、Yes/No、角色、两座位信息），让说书人的最终输入独立于 Host 真实答案和旧合法候选池。当前 `ClocktowerNightStepUi`、`ClocktowerInformationStepBuilder`、`ClocktowerFirstNightInformationRequest`、各类方桌 UI 仍保留旧合法候选/真值语义。审查时特别防止将人工选择的数值误记为“Host 证明的真实/虚假”结果；语义需只记录被确认的展示事实。迁移完所有生产消费者后整条删旧计算与测试。禁止重新建立并行 Session/存档/规则助手。

## 立即下一项任务

1. 核查 #331 最新 HEAD 的 CI/R2；Android 若失败直接读取失败 job log 并修复直至通过，不把旧测试重新引入。
2. M1b 从人工最小流程入手：数值、布尔、角色、两座位信息由说书人直接确认并发布，无 Host 计算真实答案或按合法候选限制；迁移全部消费者后删除 `ClocktowerInformationStepBuilder` 的规则候选调用和相关旧 UI。
3. 删除仍存在于 `ClocktowerGameSession.commitConfirmedPrivateInformationPublication` 中的可选 registration/witness 事务、`ClocktowerConfirmedRegistrationHostWriterV1`、`ClocktowerConfirmedRegistrationPublicationV1` 及纯旧规则测试；区分真实行动事实与自动推导登记，不复活第二权威状态。
4. 后续 M2–M5 清理 AI 旧候选协议和规则结算链。避免把多条旧接口长期并存。

## 2026-10-18 新指令（覆盖前文旧合法性方案）

用户要求严格按两阶段：**当前所有清理工作先实现完全自由的人工信息输入**，等旧 Host 规则、候选及辅助实现全部删除并验收后，才独立启动新状态感知建议 UX。后期第二阶段分别为 pair 先选两个座位再按真实座位/间谍/隐士/醉酒中毒状态缩减角色下拉列表、Chef 在阵容确定时准备数字范围并按状态显示、Empath 每次展示前动态查状态和左右邻居并显示候选按钮。本阶段以上三项均不写新计算，不作为 M1 验收要求。参见 `docs/MANUAL_INFORMATION_TWO_STAGE_PLAN_2026-10-18.md`。

立即推进剩余第一阶段：pair 角色及座位声明、角色展示、Chambermaid 数字等自由操作；只校验结果类型/座位/角色/会话结构，不按已知真相过滤；确认展示和值入统一记录；迁移完成后删掉旧代码/测试。不要利用这个方向为新版本重新建立规则域。已通过的 #330–#333 均暂保留 Draft 堆叠关系；后续 PR 单独 CI/R2，禁止声称通过未执行验收。

## M1c 下一步交接（2026-10-18）

堆叠 Draft PR #333 `codex/minimal-host-m1c-direct-number-20261018`，基于已完成 Session 双轨清理的 #332。代码新增 `ClocktowerDirectNumericResultUi.kt`、`ClocktowerDirectBooleanResult.kt` 及两项关键行为测试，并修改 `ClocktowerNightStepUi.kt` 与首夜历史语义适配。Manual/AI_ASSISTED 的数字及占卜师真假不再受 Host 的合法候选过滤。使用严格的输入类型与查询座位身份，只记录说书人决定展示的实际结果，未做规则真假核定不自动写 TRUE/FALSE。请先核对 #333 最后代码 CI、R2，必要时读 Android job log 直接修复。

下阶段依次清除 Chambermaid 查询后数字、各类角色揭示、洗衣妇/图书管理员/调查员二座位线索的 Host 计算与合法候选 UI。更重要的是迁移完消费者后应彻底删除旧 `ClocktowerInformationStepBuilder` 候选域生产、`ClocktowerRegistrationOptionPreparation` 登记见证枚举及 `ClocktowerFirstNightInformationRequest` 中余下按合法语义检验的路径；不能长期叠两套 UI。暂不处理 AI_AUTOMATIC 任意自由写状态，不恢复已删除的独立 Recovery 或 Undercover。

## 已知缺口与权限

仍无退出恢复、完整复盘或跨局归档；事件覆盖不完整。没有真机和付费模型验收。旧设备数据不迁移、不擦除。Undercover 已按用户最新指令整体退休。

持续授权仍包括本轮后续本地提交、codex/* 推送、Draft PR 创建/更新、CI/R2。所有者另于本次收尾明确授权必要同步、合并和 PR 清理，以及建立新的分支/环境；此授权用于本轮收尾，不自动成为下一轮所有 PR 的无条件合并授权。强制推送不在授权内。删除远端分支不是收尾必要条件，保留历史引用不影响关闭旧 PR。

在新的独立 worktree 使用 `codex/minimal-host-reset`（从收尾后 origin/main 创建）。不要复制旧 .gradle/build 缓存或未跟踪文件。新会话先验证目录、分支、HEAD 和工作区干净，再按上述任务开始。不要修改另一个工作区的未提交内容。
