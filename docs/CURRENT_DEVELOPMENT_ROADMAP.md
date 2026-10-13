# CampBoardGameHost — Current Development Roadmap

> Updated: **2026-10-11 Australia/Sydney**. **唯一当前执行优先级权威。** Historical PROD-GLOBAL-1D/2B instructions are NOT current NEXT; see [PROD-GLOBAL-3 unified opening plan](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md) and [handoff](NEXT_DEVELOPMENT_HANDOFF.md). Previous checkpoint detail remains in original route documents and [archive](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md).

## CURRENT CHECKPOINT — 3E3 × AI-DIAG-1 live ADB integration (2026-10-13)

Selective implementation from already-green separate #305 into current #307: DEBUG-only explicit-ADB opt-in `BotcAiWireTrace` with exact real personal-direct Host/model request and provider response chunks, plus decode/validation and 3E3 Host lifecycle events (REQUEST, SKIPPED, PRIVATE_ADOPTED, HOST_REJECTED/AUTO_PAUSED, MEMORY_ADVANCED, HOST_PUBLISHED). There is **no** blanket branch merge: old code and handoff documents from #305 do not replace 3E3. Existing ZIP history export untouched. Reference: [live ADB developer guide](AI_DIAG1_LIVE_ADB_LOGCAT_ROUTE_2026-10-12.md). Independent CI/R2 for exact new #307 HEAD and a physical USB/wireless-ADB DEBUG phone remain required before real-device acceptance.

## CURRENT DEVELOPMENT — PROD-GLOBAL-3E3 joint residual protocol and Host package gate (2026-10-12)

New feature branch `feature/prod-global-3e3-whole-residual-20261012` is stacked on Draft #306. One multi-decision `StorytellerStrategicContinuationV2` response now carries **exact all-current Host pending legal candidates + combined remainingChoices + versioned strategy memory**, with strict whole-package checks, candidate public semantics and no unique registration witness fabrication. Added `StorytellerResidualPackageGateV2`: full same-game/same-causal-cutoff/current-Host-domain comparison before proposal adoption; memory cannot advance on an incomplete illegal bundle. **Second checkpoint now partially production wired (assisted):** `ClocktowerCurrentResidualScopeV2` gathers all currently generated first-night pair and any-night typed scalar domains before a real unconfirmed action barrier, accepts a current player-query scalar and active Mayor redirect; the App calls the ONE generic continuation only on material 3E2 change and validates its full bundle and memory. Assisted UI allows one-click private preselection with current typed manual-option matching, never auto-publishes or fixes an ambiguous witness. **New 3E3 role-result slice:** same typed `ScalarInformation` contract now supports Host-anchored `ROLE` results for already fixed Undertaker/Ravenkeeper targets, with semantic role IDs and no registration witness commitment; tests include unselected target and ambiguous registration. **2026-10-13 auto bridge:** both assisted and automatic modes now use the same single Host-bounded continuation and versioned strategy; auto privately adopts without extra buttons ONLY after full Host scope and all-current-choice witness-free UI matching. Unknown/ambiguous whole-package matches fail closed to manual takeover; no early player disclosure. **Next acceptance slice:** auto package must be fully presentable and adopted before strategy cursor advances; full Host domain remains unchanged, operation-mode cache identity is isolated, and first-night suggestions are never reused during later nights. Added cross-night GLOBAL_V1 observation-domain test. **Still not full production completion:** complete multi-night Host-domain coverage, automatic Host-confirmed publication, stronger debounce/coalescing, genuine multi-night phone tests and V1 per-step retirement remain. [3E3 route](PROD_GLOBAL_3E3_WHOLE_RESIDUAL_PLAN_GATE_2026-10-12.md). Independent exact CI/R2 pending for this new slice. #305 stays an independently open Draft; its live tracing implementation has been selectively ported to #307 without merging the two Git histories.

### Previous 3E2 checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3E2 generic Host state-change checkpoint (2026-10-12)

Confirmed Draft #304 HEAD `df058ee5` CI #4126/R2 #3748 GREEN; AI-DIAG1 stacked Draft #305 HEAD `048d310f` CI #4127/R2 #3749 GREEN (kept separate). New development branch `feature/prod-global-3e-generic-host-checkpoint-20261012`, based on #304, implements `StorytellerStrategicChangeCheckpointV2` to compare frozen actual Host mechanics, confirmed action/observation timeline, player context/claims and decision history and phase/round **without role-/event-specific AI branches**. Material change only, no-op revisions do not trigger another call; strict monotonic revision and immutable published observations. `HostCurrentOpenDecisionInventoryV2` derives open IDs from current real Host provider requests, refusing stale/published/duplicate domains. App captures a checkpoint and initial memory cursor on V2 opening adoption and evaluates it when a current Host pending decision is materialized, logging advisory only. `StorytellerStrategicContinuationV2` now exports unsequenced confirmed decisions separately from provable ordered action/observation facts; no fictional ordering or witness. **Current stage is a shadow runtime checkpoint**: no additional paid call or full remaining-plan selection, no automatic execution. Contract tests/new CI/R2 mandatory. Next: generate ALL currently coupled legal open decisions as authoritative Host residual scope, then one typed whole-plan model reply/Host validation, trigger/debounce and auto/assisted adoption, Luna quality and Android phone. [Route](PROD_GLOBAL_3E2_GENERIC_HOST_CHECKPOINT_ROUTE_2026-10-12.md).

### Previous 3E milestone

## CURRENT DEVELOPMENT — PROD-GLOBAL-3E typed generic multi-night continuation protocol (2026-10-12)

Previous stacked Draft PR #304 at `4f4da2c9` accepted by CI #4124/R2 #3746 GREEN. This slice implements model-invocable `StorytellerStrategicContinuationV2`: one current frozen `ClocktowerSessionState` + typed `StorytellerProviderHistoryPrefixMaterializerV1` chronological action/observation history with truthful PARTIAL/UNKNOWN coverage, original private `HostOpeningStrategyMemoryV2`, current actual/shown roster, recorded experience/claims, and **Host-supplied current still-open decision IDs**. Strict Responses schema only allows KEEP/REVISE/RETIRE verdict for every prior intention, next bounded strategy, and IDs subset of still-open Host decisions. No isolated event-specific AI model, illegal precommitted future clue, changed previous observation or published recommendation; stale session identity rejects. Added contract tests using real GLOBAL_V1 session with action+observation+player claim and changed life state. **NOT YET production wired or strategically rated**; open-decision inputs must come from authoritative live Host domains and generic material-change dispatcher, full whole-residual plan and auto/assisted adoption still needed. New CI/R2 on current exact head required. [3E route](PROD_GLOBAL_3E_GENERIC_STRATEGIC_CONTINUATION_ROUTE_2026-10-12.md).

### Previous 3C/3D policy checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3C/3D general multi-night strategy correction (2026-10-12)

User-approved **invariant**: the expert strategist plans across the entire game and all future nights for **any role**, including all Drunk outcomes and all information/ability families; no Empath/Fortune Teller special-strategy case. After **any material Host fact, action, claim, or Storyteller override**, update the entire coupled **unpublished** residual using bounded prior strategic memory + exact confirmed Host prefix. Poisoner is only one example. Future observations remain contingent, not predetermined. Rules-owning validators may be role-specific; planning/update orchestration must not be. Existing first-night Poisoner/FT validation does NOT satisfy this requirement. On stacked Draft PR #304 latest last-confirmed `a88a833f`, CI #4123 and R2 #3745 GREEN before current edits. Changed active `InitialJointOpeningDirectV2` prompt for generic multi-night coherence and `HostOpeningSemanticAcceptanceV2` to **retain typed bounded strategy memory** after Host validation; tests pending fresh exact CI/R2. Uncommitted Poisoner/FT-only Host-journal bridge draft deleted to avoid proliferation. Generic Host-prefix continuation, KEEP/REVISE/RETIRE and runtime revision pipeline still not implemented. [Current route](PROD_GLOBAL_3C3D_SINGLE_JOINT_OPENING_CUTOVER_2026-10-12.md).

### Previous 3C/3D app checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3C/3D app route wired, needs independent acceptance (2026-10-12)

Active stacked Draft PR #304: first commit `e7e044b2` creates precommit Responses V2 transport and Host semantic gate; current unpushed app-level continuation adds **shown-only parallel revealing**, single joint request at shown roster lock for personal-direct TB AI, private same-game plan acceptance, late Drunk commit, Red Herring/bluffs adoption and strict current-Host first-night typed matching with no second initial paid request. Android CI/R2 and actual Luna schema acceptance not yet verified for this continuation. Manual + external gateway paths remain as before until full fanout. Conditional future event validation must connect to real Host session, and auto mode/phone/recovery must be audited before acceptance. [Active route](PROD_GLOBAL_3C3D_SINGLE_JOINT_OPENING_CUTOVER_2026-10-12.md).

### Earlier 3C/3D transport checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3C/3D single expert precommit call (2026-10-12)

Prerequisite Draft #303 final head `58303c130bf26f84355a0c684363b19dec8ea0f9`, CI #4120 + R2 #3742 GREEN. Active branch `feature/prod-global-3c3d-single-precommit-opening-20261012` introduces `InitialJointOpeningDirectV2`: a genuine personal-direct Responses request, formed from the actual uncommitted shown roster plus compact rules/experience, returning ONE entire structured strategy plan. It binds output to all current Host Drunk-conditional legal domains with `HostOpeningSemanticGateV2`, never enumerates legal candidate outcomes into model input, never commits/publishes implicitly. **This is the transport seam, not yet a production flow replacement.** Exact CI/R2 required. NEXT within 3C: shown-only identity dealing in parallel, one-time result/Host commit and one-tap night adoption; explicitly stop old standalone Drunk + postcommit one-shot calls and connect confirmed-event residual gate to real Host journal. Do not call production fixed before phone/Host semantic/model quality acceptance. [3C/3D route](PROD_GLOBAL_3C3D_SINGLE_JOINT_OPENING_CUTOVER_2026-10-12.md).

### Previous 3B3D checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3B3D-2B Host event-time residual gate (2026-10-12)

3B3D-2A Draft PR #302 final head `9d5331b3`, CI #4118 / R2 #3740 GREEN. New experimental `HostOpeningResidualEventGateV2` reviews prior single-opening plan against **confirmed** first-night Host state after Poisoner/FT, recomputes legal pair/numeric/FT outcomes, never infers one Spy/Recluse witness, preserves published record IDs, and returns **whole-unpublished-residual replan required** if any proposed information becomes illegal or incomplete. This does not mutate game state or authorize publishing. **Still outstanding** before 3C production call replacement: attach actual atomic Host confirmed-action/observation journal (not caller-fabricated facts), initial V2 request to live exact Host inventory, full adaptive remaining-plan revision and precommit one-call provider, then 3F device acceptance. Independent new PR CI/R2 pending. [3B3D active route](PROD_GLOBAL_3B3D_HOST_SEMANTIC_GATE_ROUTE_2026-10-12.md).

### Previous 3B3D-2A checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3B3D-2A real rules-owned Host projection (2026-10-12)

PR #301 exact head `9056b6b` CI #4116 / R2 #3738 GREEN, Draft and stacked on #300. New 3B3D-2A experimental branch implements real TB first-night domains, in-memory per-Drunk hypothesis, Host red herring/bluffs, numeric and pair rule domains, unknown player action barriers and public semantic matching. Uses existing TB rules-owning generators, not hardcoded benchmark answers or a new scorer; importantly, multiple registration witnesses may support the same public result. Android real-domain tests and current PR CI/R2 must be audited before declaring even this slice accepted. **Remaining:** event-time revalidation/conditional-intent feasibility under confirmed poison/queries, demonstrated full-domain/per-mode acceptance, 3C single joint provider request and 3F phone/production retirement. [3B3D route](PROD_GLOBAL_3B3D_HOST_SEMANTIC_GATE_ROUTE_2026-10-12.md).

### Previous 3B3D-1 checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3B3D Host-native semantic gate (2026-10-12)

3B3 upstream Draft PR #300 exact head `6c2286b7`, CI #4114 / R2 #3736 GREEN; upstream 3B2 Draft PR #299 remains GREEN. 3B3D-1 is now an **isolated experimental Kotlin Host acceptance gate** on a new stacked branch; it binds full game identity/revisions, exact logical decision IDs, typed observation and pending event inventory to the 3B1 Host branch, fails the whole plan closed, and never publishes an observation. This is a **pure acceptance seam with synthetic Host-domain tests**, not real TB semantic acceptance, new Luna evidence, or production cutover. [Active implementation route and remaining 3B3D-2](PROD_GLOBAL_3B3D_HOST_SEMANTIC_GATE_ROUTE_2026-10-12.md). NEXT after independent CI: connect actual rules-owned current semantic candidate projections and verify genuine Host full-domain parity before paid blind tests or any production adoption.

### Previous 3B3 status at checkpoint

## CURRENT DEVELOPMENT — PROD-GLOBAL-3B3 / expert semantic interface and quality (2026-10-12)

3B2 real `gpt-6-luna` E05/E02/E03/E04 paid samples were executed externally (26–43s; format warnings in E03/E04). Previous 3B2 design-only and `NOT RUN` lines below refer to the **time of document creation**, not the current observed run. None of those raw outputs has passed independent Host semantic acceptance. See [3B3 active experimental V2 route](PROD_GLOBAL_3B3_SEMANTIC_V2_AND_EXPERT_QUALITY_ROUTE_2026-10-12.md), [V2 expert prompt](PROD_GLOBAL_3B3_EXPERT_PROMPT_V2.txt), and sealed [new blind holdouts](PROD_GLOBAL_3B3_BLIND_HOLDOUTS_V1.json). V2 semantics/structure currently reside in offline Python experimental tooling only: **do not replace the postcommit production API**, do not describe V2 screening as Host legality, and do not use EXPT mock as paid-experiment authorization. NEXT: offline test/diff validation, private frozen-result diagnostics when an authorized reader is available, then independent Host-native semantic adapter, small paid holdout only after real bounded paid interface, and ultimately Android 3C/3F acceptance.

### Historical 3B2 priority at its original checkpoint

## NEW CURRENT PRIORITY — 2026-10-12 / expert Storyteller first

**Product-owner-confirmed correction; supersedes old "3B2 produce all Host-legal candidates for the model" instructions below.** See the authoritative [3B2 Expert Storyteller Principle / Experimental Route](PROD_GLOBAL_3B2_EXPERT_STORYTELLER_PLANNING_PRINCIPLE_2026-10-12.md).

- **Value proposition:** approximate a skilled, creative, lawful human Storyteller. The AI knows rules and chooses a coherent whole-game plan independently, with current setup (including whether Drunk exists), actual recorded per-player experience, confirmed events, and *optional recent-game summaries* to mildly avoid predictable repetition. Do not treat AI as merely selecting opaque Host candidate IDs from exhaustive precomputed menus.
- **Host remains independent authoritative validator** and keeper of game state, deterministic facts, publication timing, recovery and offline Manual. It may compute legal domains internally or offer specific rules when needed; it must **not** enumerate every Drunk/first-night combination into the default Luna input. Fail closed or request bounded full-plan correction for invalid model output.
- PR #297 (**3B1**) merged: `d720a845a113af0889360ed73717cdc9621f8abc`, CI #4103 / R2 #3727 GREEN; Oracle local `main` synchronized and clean at completion. Keep 3B1 typed scope/validator as an internal safety tool, **not** the required model I/O format.
- **NEXT = 3B2-EXPERT-PROBE:** Luna-only controlled experiments without enumerated legal candidates, compare legality/quality/coverage/cost on diverse legitimate setups; *then* choose minimal production semantic schema / Host adapter, 3C/3D single joint request, 3E global continuity, 3F Android acceptance. No model training or Sol comparison now.
- Previous QA caveat remains: a later phone smoke no longer reproduced the recommendation show crash, but root cause was never proved; keep real-device verification in 3F. Mini MCP generalized experiment directory development is a separate parallel project.

## Historical checkpoint — 2026-10-12 (supersedes stale PR status below)

- Verified live `main = 2288d58f4bbc212e1e6d8cdd60f6ced539f32312`, local `main` clean and remote-equal, **zero open PRs**; PR #292/#293/#294/#295 **merged**. The stacked Draft/status text below is historical, not the current authority.
- User re-tested first-night AI recommendation **display successfully** on Android; previously observed crash is not currently reproducible. No fatal stack/root fix has been established; final crash/double-publication regression remains in 3F.
- **PROD-GLOBAL-3P** prompt A/B/B2 findings and external engineering review recorded at [this design note](PROD_GLOBAL_3P_PROMPT_DESIGN_AND_EXTERNAL_PROJECT_REVIEW_2026-10-12.md). Retain Luna-only one-bundle strategy, evaluate player-visible vs Host-known worlds, rule mechanism opportunity costs, and explicit conditional dependencies. External per-wake-item recommender architecture is **not** adopted.
- **3B1 in isolated feature PR development**: new precommit conditional domain contract/validator, no Drunk commit and no provider call. This does **not** mean full 3B/3C/3D wired or accepted. NEXT: real Host legal candidate producers for every conditional branch (3B2), then one background Luna initial call paired with shown-only identity reveal (3C/3D).

## AI-SETTINGS-RELOCATION — 2026-10-12 (on stacked Draft #295, Android CI pending)

- AI 运行**默认模式**、网关/个人 OpenAI 直连、模型 ID 和凭据统一在应用“设置”界面管理；开局确认页只显示本局有效模式、配置状态及设置入口，不再编辑连接参数或切换模式。
- 默认模式通过新增独立 `storyteller_operation_mode` 应用设置键持久化（默认 MANUAL，未知值回退 MANUAL）；现有 Keystore 密钥和网关凭据存储不迁移、不复制。此前已删除的旧 `storyteller_experience_mode` 不复活。
- “原班人马快速再来一局”重新采用**全局默认模式**与现有连接配置，不继承上一局故障时的临时 MANUAL takeover；保留原剧本及座位。AI 凭据尚未就绪时**不归档前局**而提示进入设置；非 TB 剧本仍使用规则允许的手动模式。开局与快速重开共用合法的配置就绪门。
- 变更仅属于 AI 设置/重开 UX；不宣称 PROD-GLOBAL-3 的酒鬼与首夜联合 LLM 规划已完成，也不代表真机全局推荐展示崩溃已验收。

## LEGACY-EXPERIENCE-RETIREMENT — 2026-10-11 (stacked Draft #295; final semantic cleanup)

- 独立清理分支 `legacy-experience-retirement-20261011` 从 PR #293 head 建立；其依赖链是本分支 → Draft #293 → Draft #292 → main。必须逐层验收/合并，不得把下游直接合到 main。
- 旧说书人“新手/熟练”开关已移除：删除 `StorytellerExperienceMode`、`StorytellerRecommendationUxPolicy`、设置页开关及 `storyteller_experience_mode` 持久化；**不做旧设置迁移**。这与玩家自己的 `PlayerExperienceLevelV1` 熟练度毫无关系。
- 唯一运行操作模式仍是 `MANUAL`（不发 AI 请求/人工主持）、`AI_ASSISTED`（AI 建议、人工确认/覆盖）、`AI_AUTOMATIC`（限定范围内、经 Host 当前合法域与 revision 核验才可提交；否则暂停/人工接管）。Host 唯一合法结果为规则确定性，不等于 AI 授权。新老模式不得直接映射。
- 已删除旧“三项推荐”界面。独立人工候选选择保留；多个合法裁量不能由旧展示布尔值、首个候选或缺失的模型建议决定。Fortune Teller、红鲱鱼与注册/夜间信息入口向**唯一合法结果**或显式人工选择收敛；非唯一 Spy/Recluse 登记见证仍不是事实。
- 本次只退役旧模式和错误自动控制链，**并未完成** PROD-GLOBAL-3 的统一初始 Drunk/首夜联合规划、条件合法域、持续战略记忆或完整自动说书人。当前 Draft Android 的旧 Drunk 请求时序/展示故障仍按 PROD-GLOBAL-3A–3F 处理；不得标为已发布能力。
- 旧版 #295 HEAD `2d1954f` 已获 CI #4090 / R2 #3717 GREEN；但额外语义审查发现 `allowRuleDeterminedAutoUi` 仍间接控制展示格式、Host 私有资料和手动候选入口，因此已继续解除这些布局/候选域耦合，**新 HEAD 必须重跑 CI/R2**。不得用旧 GREEN 代替新提交验收。
- 上游集成：#292 已将 #294 的 TB schema-v3 模板、setup、测试与文档以双父合并提交 `02a19015` 纳入，原 `mergeable=false/dirty` 已解除；须以组合 HEAD 的 CI/R2 再验收。#293 随后接入父分支（`3c8b5724`），#295 还需集成其上游并重验收，全部仍保留 Draft、不得绕过真机产品验收。

## 1. 当前唯一执行顺序 — PROD-GLOBAL-3

**Product correction (2026-10-11):** 酒鬼是整局联合规划的一部分，绝不可先发一个独立酒鬼模型请求、选完酒鬼再发另一个初始首夜请求。Host 分阶段提交的规则边界不等于 AI 分阶段规划的边界。身份展示全部固定后立即发起 **一次** 完整初始 Luna 请求，后台规划与前台逐人展示身份并行；返回唯一开局配置 + 私有简短战略记忆。Host 提供每个合法酒鬼假设下的条件合法域、所有已知确定信息与玩家熟练度；AI 同时协调酒鬼、调查员/厨师/共情者等可裁量结果、红鲱鱼、适用的恶魔三项掩饰身份。未发生的投毒/占卜师选人只作为待定依赖，不能当事实。后续依据 Host 确认事件 **整体更新受影响的未执行计划**。

**实际代码边界，不可误写为已完成：** 当前个人 Android 直连 AI_ASSISTED 的 PROD-GLOBAL-2B 已编码后酒鬼一包推荐/一键采纳，但在需要酒鬼的 setup 仍于发身份前等待旧 Drunk 请求并显示冗长报告；首夜一包请求又要求 SETUP_COMMITTED；只有唯一合法结果的共情者也被放入模型返回；真机点击展示推荐后异常退出，根因等待安全的 AndroidRuntime stack 验证。旧版色彩可读性已获用户认可，不开展无证据的配色返工。功能验收**未通过**。

**Model decision:** 本阶段固定实验版 `gpt-5.6-luna`，**不比较 Sol 或其他模型、不训练新模型**。集中改善提示词、完整 Host 输入、联合合法候选/假设表示、简短结构化输出与局内战略记忆。过往合成性能试验为历史证据，不能代替本轮实际 Luna 行为和 Android E2E。低于5秒残余重算仍为体验目标，非已达标承诺。

| 次序 | 工作 | 当前状态 / 退出标准 |
| --- | --- | --- |
| **P0 / 3A** | 推荐信息点击展示崩溃 | 取得精确安全的 Android FATAL EXCEPTION、修复已证实故障、Host 不部分/重复发布、回归测试与真机成功 |
| **3B** | 3B1 Host 内部合法性协议已合并；3B2-EXPERT-PROBE 当前 NEXT | 用完整魔典+规则知识+玩家熟练度（可选近期经验）让专家 LLM 自主规划，Host 事后独立校验；不默认预枚举全部酒鬼假设和合法信息给模型 |
| **3C** | 身份展示与**同一初始请求**并行 | 发身份不等待 LLM，结束后一次采纳联合方案，Host 才绑定酒鬼并验证后续裁量 |
| **3D** | Luna prompt + strict single response + strategyMemory | 模型只回一套完整配置与私有可延续战略摘要，唯一合法结果由 Host 直接确定；无长篇分析 |
| **3E** | 全局剩余计划事件续航 | 玩家动作/Host确认/覆盖驱动条件校验及**整包剩余部分**修订，事实和已发信息不回写 |
| **3F** | Android 真机贯通、验收与旧分叉退役 | 8人联合案例、Drunk/no-Drunk、Poisoner/FT 后续依赖、显示崩溃/超时/手动回退；CI/R2独立 GREEN 后再评估 PR 合并 |

**PR 状态（启动时再次核对）：** [PR #292](https://github.com/Jazz0006/CampBoardGameHost/pull/292) Draft 基于 main，[PR #293](https://github.com/Jazz0006/CampBoardGameHost/pull/293) Draft 基于 #292。2026-10-11 文档会话起点实验分支 `experiment/personal-openai-direct-20261011@c01ddb3b` clean，远程同头；GitHub main 为 `3e1cc407`。旧校验 GREEN 不代表新产品验收；本轮仅文档更新，不修改生产代码、不合并 Draft。

## 2. 冻结的架构与产品原则

1. **规则与游戏权威独立：** Host/Game Engine 拥有 canonical current game/session、rules、conditional legal candidates、committed facts 和游戏状态写入。LLM/provider 仅返回**一套全局联合推荐**和私有非权威战略摘要（不是必选备选或长篇解释），**provider 本身不得写入事实**；AI_ASSISTED 由人确认，显式启用 AI_AUTOMATIC 后才可由 Host 在即时合法性/版本核验通过后按合法流程自动提交。
2. **离线优先：** 在没有模型、网络、额度、有效响应时，Manual 主持、规则与合法选择仍可运行。唯一合法结果可由 rules 自动确定；多个未获合格推荐的合法结果：手动模式为 `MANUAL_REQUIRED`，自动模式须暂停并提示/支持人工接管；**不得**擅自选择任意结果。
3. **旧引擎退役：** RES-0～5 的 heuristic/style/weighted/named special-policy 自动推荐权限已移除或物理删除；GSP-1 已撤销 Q04 Drunk、functioning Librarian V2、INV1-A 的多候选特例自动权威。旧案例/算法只能供证据、benchmark 或审计。
4. **Strategic memory 纳入本轮合同：** Host 真实事实和 legal domain 优先；LLM 输出可持续修订的**局内简短战略摘要**属于 advisory，区分 FACT / INTENT / HYPOTHESIS / UNCERTAINTY。Responses 当前 `store:false`、无模型自动续接，须在后续请求显式携带 strategyMemory + Host 因果前缀；跨局反套路与长期模型对话为后续目标，不是本轮阻塞。
5. **Recovery 的产品范围严格限定：** 仅同一当前版本格式、精确兼容令牌、**4 小时以内**的当前游戏突发关闭恢复（restore game, not App）。不新增跨版本迁移、长期未完局存档、任意历史 cut-off 复演、无可达风险的重复 setup 校验；既有已验证的真实机械正确性与短期恢复保持不变。
6. **结果优先的登记语义：** Spy/Recluse 可具有多种合法见证解释（如 Chef/Empath 数字），显示结果不唯一指定登记；仅记录真实明确选择，不把推导的第一个 witness 当成裁定。
7. **训练/数据边界：** EvidenceLab 收集来源可信、可追溯的完整对局，HOST-ML1 提供中立合法候选导出；Host 不依赖训练模型，也不重回单案例 if/else 排序。

## 3. 阶段进度（聚合状态，不复制逐 PR 历史）

| 轨道 | 最新认可状态 |
| --- | --- |
| D6 / App-Host ownership / EPI / SDE foundations / C4 / IF-D / RH-E | 已完成；部分旧 SDE policy 历史身份冻结，无生产推荐权威 |
| DLB-0～7 / first-night dependency、TBGS-0/1 | **COMPLETE / ACCEPTED** |
| TBGS-2A～2E | **COMPLETE / ACCEPTED**，2F 未选择 |
| C5-A～E / old Investigator / Q04 special policies | 实验和历史验收保留，**GSP-1 后自动裁量权已撤销** |
| HOST-ML0 / HOST-ML1 | **COMPLETE / ACCEPTED** 中立模型/benchmark 边界 |
| RSR、LRE-1、GSP-0/1/2 基础、RES-0～5 | **COMPLETE / ACCEPTED**；旧 LRE family-by-family 路线已替换 |
| GSP-R1A / R1B / R1C1 / R1C2A / R1C2B | **COMPLETE / ACCEPTED**，但不意味着任意历史时点可恢复 |
| GSP-R1C2C-1 已限定的真实脚本裁定/白天公共动作 | **已验收**（PR #263、#265、#267、#269、#276、#278、#280、#282、#284）；Drunk-shown Virgin P1 已修复；非真实脚本的 synthetic Klutz/Spy writer 已撤销 |
| GSP-R1C2C-2/3、R1C overall | **未完成且当前暂停**，只保持有用的现有事实与安全功能 |
| GSP-MEM0 | B2 经只读批次审计：9 attempted / 9 succeeded / 9 response JSON；正式盲评仍未完成，转为 NON-BLOCKING |
| GSP-API0 / DEV-EXEC1/2 / PROD-GLOBAL-1C | 历史 API0、B2 9/9 与当前 Gateway 三次真实 Luna 合成测试均有独立记录；Mini MCP 新固定报告读取器已实读验收。Android Keystore/Gateway 客户端本轮 CI GREEN；**全局预规划、低于 5 秒响应及真机全流程均待验收** |

## 4. 执行与验收门

- **下轮先执行 PROD-GLOBAL-3A 真机展示崩溃定位，然后 3B–3D 一次初始联合规划，再 3E/3F。** 每轮核对 live main/open PR/worktree/CI，并用真实 Host 条件合法域、TB 首夜次序及 Android E2E 验收；详细退出条件以 [PROD-GLOBAL-3](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md) 为准。
- PROD-1 的 **必要**测试：真实全座位/显示身份与合法候选上下文、模型结构化有效返回、非法 ID、已变更 decision/revision、超时离线/额度失败、密钥不进入 APK、手动流程可用；无 MEM0 大样本质量研究前置门。
- PROD-2 的 **必要**测试：自动模式必须显式选择、单次合法确认、取消/去重/过期拒绝/状态重复进入、无提供方时暂停；所有状态提交属于 Host。整个完整自动 TB 游戏是 PROD-4 的独立目标，不能由单决策成功宣称完成。
- Oracle VM 是 **开发机**：Gateway 已有本地服务端密钥、Bearer、配额与 Tailscale Funnel HTTPS；开发机/本机 smoke 已实测，**仍须手机真实调用及运行时安全性、时序、配额测试**。Mini MCP 工具权限不等于 Android Gateway 权限。
- MEM0 旧冻结合成 fixture/评分协议继续作为证据但 **不阻塞** 产品；若研发时遇到具体质量退化才开展针对性评估。Host 缺什么字段就对着真实用例补，不扩展任意历史 Replay/Recovery。
- 当前新方案不创建旧 heuristic/special-policy 推荐，不改变规则权威、Spy/Recluse 多 witness 结果优先语义、现有 Recovery 范围。无关文档和分支清理不可阻塞 PROD-0/1。

## 5. 权威索引

- **[当前 PROD-GLOBAL-3 唯一初始联合规划与战略连续性实施路线](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md)**；旧 PROD-GLOBAL-1D/2B 作为历史设计和失败边界。

- [最新产品路线：生产 LLM 接入与自动说书人](GSP_PRODUCTION_AUTONOMOUS_STORYTELLER_ROUTE_2026-10-09.md)。
- [唯一当期 handoff](NEXT_DEVELOPMENT_HANDOFF.md)、[文档导航](README.md) 与根目录 [AGENTS.md](../AGENTS.md)。
- [GSP 通用 policy 设计](GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md)，以本路线、2026-10-09 memory/recovery 决定作为后续优先级覆盖。
- [RES 架构解耦和旧引擎退役](RES_ENGINE_RECOMMENDATION_SEPARATION_AND_PURGE_ROUTE_2026-10-06.md)、[核心所有权](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md)、[TB canonical snapshot](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)、[HOST-ML1](HOST_ML1_NEUTRAL_DECISION_EXPORT_IMPLEMENTATION_2026-10-03.md)。
- 2026-10-09 之前全部工程阶段详情与 exact-head 验收：参阅 [旧 roadmap 快照](archive/checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md)；不再在唯一执行计划里堆叠几百行历史。


### HISTORICAL 2026-10-11 PROD-GLOBAL-2B implementation checkpoint — superseded by PROD-GLOBAL-3 (not NEXT)

The owner accepted revised cross-screen color readability on real Android and requested implementation priority on a SINGLE recommended whole-night configuration. In experimental personal direct AI_ASSISTED mode only, code now constructs one Host-supplied legal information scope from current first-night materialized Manual domains, legal off-board Demon bluff role IDs and actual Good red herring seats. It provides compact hashed candidate IDs with Host-authored result descriptions so the model can analyze the whole roster in ONE structured Responses call. The model output contains exactly one eligible candidate per available decision, one distinct legal bluff trio if applicable, and exactly all deferred choice/dependency IDs — without prose issue essays or alternatives. All are fail-closed validated and gated on exact game/revisions.

The private first-night preflight can display that one recommended package and a one-tap accept action, after which only Demon bluff preselection and FT red herring are stored. Other candidate IDs remain advisory and are re-matched to the exact step's current Host Manual-legal candidates before use. A Poisoner choice may invalidate a previously recommended candidate; the normal step-level live advice/manual control remains fallback; no old proposed observation is forcibly published. Unknown FT player query is deferred. The old verbose whole-board checkpoint is skipped ONLY for personal direct assisted first-night path; Gateway and automatic remain unchanged until independently upgraded.

**Acceptance not yet granted** until current-head full Android CI and device end-to-end: one full actual opening including Demon's triple, Investigator, Chef or Empath, Poisoner and deferred FT, plus change/staleness, offline manual fallback, latency and cost. Never treat unexecuted accepted plan IDs as Host observations or Recovery facts. Real-phone readable-color feedback applies to current visual baseline.

## TB setup simplification checkpoint (2026-10-11)

TB setup schema v3 stores the full shown Townsfolk roster, `drunk: true/false`, and no `drunk` outsider or three-option runtime field. The 208 old-three-to-one selections are audited in `TB_SETUP_SIMPLIFICATION_TEMPLATE_AUDIT.json`; late Drunk binding and shown-roster history rotation are preserved.
