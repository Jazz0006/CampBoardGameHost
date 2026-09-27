# 最新推荐代码审计 — 2026-09-25

审计基线：`15fd898faa88795b73ea769c41dc042ec5a91680`，本地分支 `codex/sde-history-prefix-route-closure`；最近生产代码提交 `8855d461`。按用户要求直接读取本地代码并执行本机 Gradle。本次不修改生产实现；审计产物随后会单独提交并同步。

**结论：架构可以继续沿当前路线推进，但尚不能认定已实现接近真人说书人的高质量推荐。发现一项直接影响叙事质量的语义 BUG、一项应用集成闭环缺口，以及三项回放/运行时边界问题。** 当前 V1 未使用叙事特征排序，所以叙事 BUG 目前污染特征和后续校准依据，尚不等于已经改变正式 V1 的推荐输出。

审计范围是设计目标相关的推荐链路：当前路线和集成契约、旧版实际选择入口、结构化候选/确认、历史 exact consequence、确认链/健康信息/醉毒叙事特征、V1 策略、运行时 shadow、trace 关联/存储，以及 C1/C2 回放输入。不是对仓库全部文件、全部游戏角色或全部 Android 交互作无遗漏保证。

**1. [P1] 醉毒叙事将“只能靠失常解释”误判为“兼容且不可察觉”**

位置：`StructuredInformationShadowAdapter.kt:212–224`、`ImpairedNarrativeFeatures.kt:214–223`；默认假设在 `SdeOfflineReplayCoordinator.kt:23`。

真实 typed 链路复现使用五人 TB 的 Chef / Drunk（显示 Empath）/ Saint / Baron / Imp。Empath 的两个相邻座位不变，没有死亡、转职或投毒动作；首晚显示 0，第二晚对 0/1/2 进行候选评估。没有手工编造 DecisionFeatures。

- 通过同一个 exact evaluator 的 `FUNCTIONING_ONLY` 对照：继续显示 0 有 **2,972** 个解释；改成 2 有 **0** 个解释。
- 正式默认 `MECHANICALLY_CREDIBLE` 链路却将候选 2 输出为 `COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE`、`transitionNecessity=NONE`、`detectabilitySignal=NONE`。
- 复现测试在上述对照断言通过后，准确失败于“应识别叙事破裂、实际返回兼容”的断言。

根因：`TroubleBrewingWorldObservationEvaluator` 在世界中的信息源失常时允许任意输出；这是规则/机械可信性所需的行为。叙事 projector 却直接复用同一组 confirmation 结果，再把“没有找到矛盾 observation ID”当成兼容证据。失常解释存活，并不能证明玩家相信能力正常时仍有连贯的叙事。

影响：后续策略即使开始使用已完成的 impairedNarrative 特征，也无法可靠避开暴露醉毒的输出；现有模拟和特征连接测试没有覆盖这一语义差别。

修复边界：在共享叙事 owner 中补充能力正常/可信感知世界的延续证据，与机械合法性、全部可信世界保留分开。不要把全局 exact 默认假设改成 FUNCTIONING_ONLY，也不要针对 Empath 或本次样例加特判。补充跨数值/布尔/类别信息、重复历史、不可避免变化、醉毒生命周期的 typed 回归，再考虑新的策略版本。

**2. [P1，集成缺口] App 采集的 trace 没有形成独立可恢复的回放输入链路**

位置：`CampBoardGameHostApp.kt:899–928`、`:1312`；`SdeHistoricalReplayInputJsonCodec` 在生产源码中没有 encode/decode 调用者；`SdeOfflineReplayCoordinator.kt:17–23` 仍需要调用方提供 `InformationDecisionContext`。

运行时 captureFresh 生成 replayInput 后，只将 pendingTrace 保存到 archive。trace 存的是历史引用、候选 ID 和派生特征，不包含初始 setup、完整决策请求/候选语义及可从磁盘重建该请求的绑定。App 恢复函数明确把 `committedClocktowerSetup` 置为 null，此后 shadow 在取 setup 时直接返回。

具体触发：开始可采集的五人 TB → 产生一个 trace → 保存并重启 App → 恢复游戏。静态调用链表明恢复后的运行时不再采集该 shadow；磁盘 trace 本身也不能驱动当前 offline coordinator 重新生成法律候选和特征。开始新游戏后，旧游戏的当前恢复存档还可能被覆盖，而 archive 会继续保留旧 trace。

现有 `SdeOfflineVerticalReplayTest` 先保存 input 字符串，但第二次 replay 仍传入原来的 `model.shadowDecisionContext`。因此它证明了“运输输入解码 + 保留的请求上下文”可组合，未证明冷启动后仅凭持久化材料就能重建整次决策。C1 的纯 transport 范围本身不是错误；把当前状态当成完整应用数据闭环则超过代码证据。

修复边界：沿现有 canonical owner 导出有版本和身份绑定的 setup/history/decision-request 运输材料，提供真正的磁盘导出/载入入口；从恢复后的输入重新调用法律候选 owner，不能序列化一份并行 mutable 游戏真相。验收应跨进程或销毁所有初始对象后，仅依靠持久化字节与固定规则集完成候选重建、特征重算、actual-choice 保留和策略对比。若明确暂缓 App 恢复，应把它作为未完成范围保留在路线中。

**3. [P2] shadow 耗时不包含存储，诊断存储仍同步运行在 UI 调用线程**

位置：`SdeRuntimeShadowCoordinator.kt:122–149`、`DecisionTraceArchiveStore.kt:18–28,74–84`、`persistence/DecisionTraceArchivePreferencesStorage.kt:19–22`；App 关联入口 `CampBoardGameHostApp.kt:950`。

只有 offline evaluation 被切到 `Dispatchers.Default`；返回后在 Compose 调用线程进行 archive 全量解码、编码与 SharedPreferences `commit()`。确认后的关联同样同步执行，并在匹配到 trace 后再次 load 整个 archive。archive 跨游戏增长且没有保留上限。

确定性时钟探针令 `appendTrace` 消耗 2,000 ms，实际返回仍是 `STORED, elapsedMillis=0`。这是注入时钟的边界复现，不是宣称本机磁盘写入实测耗时两秒。代码同时表明 1,500 ms 是评估完成后的发布门槛，不能解释为整次工作的硬超时；同步 exact 枚举内部没有取消检查。

影响：当前预算/耗时日志不足以支持整条运行时链路的性能结论；慢存储或 archive 增长会阻塞界面回调，诊断失败隔离并没有隔离延迟。

修复边界：将诊断 I/O 交给串行的后台存储路径，保留原子追加/关联与幂等语义，避免简单并行写引入丢记录；分别记录计算和持久化耗时，测试取消、积压、存储增长及慢设备。canonical commit 继续由现有 session 先完成。

**4. [P2] 回放仅核对 revision，接受了另一局游戏的决策上下文**

位置：`SdeOfflineReplayCoordinator.kt:25–38`、`StructuredInformationProductionShadow.kt:79–88`、`StructuredInformationShadowAdapter.kt:105–124`。

复现：input 的 gameId 是 `runtime-game`，通过正式数值候选 adapter 创建 gameId 为 `different-game`、revision 相同的决策上下文。evaluate 成功，产生：

```text
archiveKey.gameId = runtime-game
decisionId = numeric|Empath|different-game|FirstNight|1|10|2|LIVING_EVIL_NEIGHBOURS
```

根因：上下文只有自由字符串 semanticIdentity 与 revision，组合边界校验 ruleset/seed/revision 后，没有核实请求属于同一 game。运行时 expectedIdentity 也只绑定 replayInput 和当前 session，没有绑定传入的决策模型。后续 actual-choice correlator 的校验不能撤销已经生成或存储的错误 shadow 特征。

影响：离线请求拼接错误或陈旧调用方上下文会污染回放身份和特征归属。这里证明的是 typed 接口缺少拒绝能力，未声称已在实际 UI 中复现跨局竞态。

修复边界：由信息决策 owner 提供显式 typed game/request identity；在进入 exact 计算前与 replayInput/current session 一起校验。不要依赖解析带分隔符的显示/语义字符串。

**5. [P2] C1 的 strict 解码未覆盖嵌套 history 语义**

位置：`SdeHistoricalReplayInputJsonCodec.kt:48–55`，下游 `ClocktowerSemanticHistoryPersistence.decodeActionTimeline` 和 `EpistemicSemanticJson`。

根节点、ruleset 和 setup 做了 exact-key 校验，但动作/观察直接交给兼容性 decoder。两个独立探针均复现了违约：给合法 poison fact 增加未知 `futurePoisonSemantics` 字段，decodeStrict 仍成功、未知字段被静默丢弃；把 observation 的 numeric-result.value 改为 **1.9**，解码仍成功并将其静默截断成 **1**。后者来自 `EpistemicSemanticJson.kt:327` 的 `getInt` 转换。

影响：不满足 C1 的 unknown-shape fail-closed 约束。尤其是在版本演进或外部回放输入出现新嵌套语义时，会把无法理解的材料当成已验证输入继续评估。

修复边界：复用现有 codec 的共享严格模式或前置 schema 校验，覆盖 action/point/observation/proposition 的键集合、数值类型和必需字段；不要无审计地改变旧游戏存档的兼容性行为。检查整段受影响 schema，不仅补一个字段断言。

**设计要求与实际运行范围**

| 要求 | 当前代码情况 | 判断 |
| --- | --- | --- |
| 规则合法性与推荐分离 | Foundation 候选/确认、canonical session commit 保持独立；shadow 不改显示值 | 基础边界成立 |
| 接近真人的优劣选择 | V1 只拒绝零 Evil topology，其余等价后 seeded hash；正式显示仍走旧选择器 | 尚未实现，不应擅改冻结 V1 |
| 确认链、健康信息、醉毒叙事 | 有 typed projector 和历史调用；叙事存在上述误判 | 模块存在不等于质量已达标 |
| truth danger / credibility disruption / future flexibility | contract 字段存在，主要仍 NOT_PROJECTED_YET | 需要 C4/3D2 及后续实施 |
| 全部推荐入口继承新版 | App shadow 仅 Debug + TB + 5 人 + 历史条目 ≤16；数值准备仅 Empath/失常 Chef，注册分支直接返回 null | 当前是受限诊断入口 |
| 首夜 pair、Red Herring、Demon bluff 的联动质量 | 有离线/实验 owner；未沿本次 App runtime 路径实现普遍 cutover | 不能用局部 numeric 闭环代表全部表面 |
| 独立可重放的真实采集 | transport/trace/策略回放组件齐备，但 App 没有持久化完整 replay request | 集成仍有缺口 |
| 真人质量校准 | 当前路线明确证据分级、V1 冻结和逐表面切换门槛 | 测试成功不等于专家质量等价 |

建议顺序：先修叙事与回放身份/schema 的正确性 → 补真实导出/恢复闭环与诊断 I/O → 再实施 C4 的共性特征 → 以真人证据及同历史多版本比较形成新的 policy → 按具体表面逐步扩大 player count/history 支持并切换。继续保持 V1 定义与 canonical owner 不变。

**验证与交付**

本次验证结果：

| 验证 | 实际执行结果 |
| --- | --- |
| SDE + 信息决策针对性测试 | 130 tests / 34 suites，全过 |
| 默认 FAST 全套 | 1,528 tests / 352 suites，全过 |
| FAST 外的 ExpertRecommendationReviewTest + StorytellerV4BaselineSimulationTest | 4 tests / 2 suites，全过 |
| 审计专用缺陷探针 | 5 tests，5 个预期 assertion failures；0 errors / skips |
| git diff --check | 通过 |

FAST 与两个额外类在同次 Gradle 调用中实际执行，耗时 3m41s；编译依赖复用缓存。这不是完整 T4。针对性 130 项与 FAST 有重叠，不能相加宣称覆盖更多独立测试。五个探针对应第 1/3/4/5 项（schema 有两个探针），第 2 项以生产调用链和恢复赋值证据确认，没有宣称跑过 Android 冷启动实测。

证据：`baseline-summary.json`、`baseline-gradle.log`、`focused-gradle.log`、`probe-summary.json`、`probe-results.xml`、`probe-observations.txt` 和 `probe-gradle.log`。探针使用预期正确行为作为断言，在当前代码上的失败是缺陷复现，不是修复后的 GREEN。

正常 Gradle source set 不包含本目录探针。显式重现命令：

```sh
./gradlew -I artifacts/sde-audit-2026-09-25/audit.init.gradle :app:testFast --tests '*SdeAuditProbeTest' --rerun --no-daemon
```

没有修改生产代码、现有正式测试或路线权威文档。本次未运行 Android 真机交互、完整 T4 或远端 CI/R2，也不作合并验收。持久记忆选择 NONE：这些缺陷状态及提交信息应由修复后的 live code 和回归测试重新验证。
