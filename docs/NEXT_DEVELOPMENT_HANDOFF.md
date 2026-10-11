# CampBoardGameHost — Next Development Handoff

> Updated **2026-10-11 Australia/Sydney**. **唯一活动交接。** 进入新会话必须重新查开发机 live state / GitHub main、PR、CI，不能假设下述 commit 仍是最新。当前单一执行路线是 [PROD-GLOBAL-3](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md)；此文件只记录状态及下一步。旧 PROD-GLOBAL-1D/2B/analysis-first NEXT 作为历史，不得再次触发独立酒鬼模型规划。

## 2026-10-12 PROD-GLOBAL-3P/3B1 current handoff (overrides historical sections below)

- GitHub `main` and Oracle Mini MCP `clocktower/main`: `2288d58f4bbc212e1e6d8cdd60f6ced539f32312`, clean/equal at audit; zero open PRs; #292–#295 **all merged**. The old stacked-Draft directions below are superseded.
- Android real-phone follow-up: AI recommendation display succeeded without earlier app exit. Earlier root cause remains undetermined, so keep E2E and double-publication check in 3F, do not invent a fix.
- Source-grounded external prompt study and Luna A/B/B2 findings: [3P design lessons](PROD_GLOBAL_3P_PROMPT_DESIGN_AND_EXTERNAL_PROJECT_REVIEW_2026-10-12.md). Incorporate explicit unknown coverage, rules research supplied by Host, fail-closed ID validation and mechanism opportunity cost; **do not copy** Olalall per-wake-item model calls.
- Current feature branch: `prod-global-3b1-precommit-contract-20261012` starts from that exact main. 3B1 adds a pure Host precommit **one branch per legal Drunk candidate, or one none branch** schema + fail-closed selection. No API call, AI UI, auto-commit or Host candidate computation is yet wired. Validation/CI is required before merging.
- **NEXT** complete 3B2 by deriving every branch's actual legal info/pair/scalar/red-herring/bluff and all deferred dependencies from real Host owners, preserving result-first Spy/Recluse uncertainty and avoiding synthetic ID fallback. Then 3C/3D single background Luna request with identity reveal and strict strategy memory; 3E residual global replanning; 3F phone/CI/R2.
- Mini MCP experiments directory work is a **separate concurrent project**. Do not change that repository while working in Host.

## 2026-10-12 当前 UI 小切片：AI 设置归位与原班人马快速重开

- PR #295 stacked Draft 上已实施：App Settings 统一呈现 AI 模式、直连/Gateway 凭据/模型；开局仅展示默认模式与配置就绪状态，并可进入 App Settings 后返回相同开局步骤。模式默认值保存至 `AppPreferencesStore` 的 `storyteller_operation_mode`；API Key / Gateway token 保持原 Keystore 存储。
- **经源码确认的旧行为**：快速重开直接调用 `startClocktowerGame`，使用尚在内存中的 `storytellerOperationMode`。此前的人工接管会使下一局意外 MANUAL，App 重新启动又默认 MANUAL；现以**独立的全局默认模式**纠正。
- **新行为**：开新局/快速重开都会通过 `storytellerModeForNewGame` 取得新局模式、通过 `storytellerNewGameConfigurationReady` 拒绝未配置的 AI 开局；快速重开先检查凭据再归档旧局，并保留 5/6 人原剧本。NoGreaterJoy 不覆盖全局默认 AI 配置，只在当前不支持的剧本运行手动。
- 此切片的自动化及 Android/真机状态需以本次最新 PR head 的 CI/R2 核对。尚未完成的 PROD-GLOBAL-3 全局开局、真机展示崩溃仍是主线下一项。

## 当前并行清理：LEGACY-EXPERIENCE-RETIREMENT (2026-10-11)

当前工作分支 `legacy-experience-retirement-20261011` 基于 Draft PR #293（其基于 Draft #292），不更改两者原始分支。旧说书人“新手/熟练”运行模式、设置开关/SharedPreferences 键及三个候选推荐专属 UI 已在清理分支删除；Drunk 路由不再接收说书人熟练模式；Fortune Teller/红鲱鱼不能在没有 Host 唯一合法结果时任取首项；Spy/Recluse 多解登记不可被自动定为单个见证。独立合法候选的人工选择和 `PlayerExperienceLevelV1` 均必须保留。

正式操作权限仅有 `MANUAL`（全程手动且无 AI 请求）、`AI_ASSISTED`（推荐/明确采纳，不自动提交）与 `AI_AUTOMATIC`（只有 Host 核验当前合法性、版本和明确候选成功才可自动提交；失败安全暂停）。规则计算/唯一确定结果不取决于 AI。这是目标契约；**完整无人说书人尚未实现**。本清理不会修复 PROD-GLOBAL-3 旧独立 Drunk 请求的时序、Android 展示崩溃，亦不会分裂初始 Drunk + 全首夜一次联合规划目标。已创建 stacked Draft PR #295。角色简化夜间展示也须仅在唯一合法候选时展示快捷确认，绝不能因旧文本样式自动提交多个候选中的第一项。旧 CI/R2 仅针对 main；#295 已改为支持 stacked PR 直接触发，临时验收 PR #296 因与 main 冲突而关闭（从未合并）。#295 在 `2d1954f` 获 CI #4090 / R2 #3717 GREEN；本轮语义审查又发现 `allowRuleDeterminedAutoUi` 影响操作模式无关的 UI 排版、候选展示及 Pair 信息人工覆盖入口，已继续解除相关耦合，**需重跑更新后的 CI/R2**。#292 已通过双父合并提交 `02a19015` 吸收 main 的 TB schema-v3 (#294)，消除了上游的 GitHub 冲突；#293 集成父分支的提交为 `3c8b5724`。#295 此次需纳入上述历史并以 schema-v3 修正旧测试 fixture；三层均保持 Draft，CI/R2 与真机产品验收不可混为一谈。

## 1. 当前基线 — VERIFIED at this documentation session

- Repo: Jazz0006/CampBoardGameHost；Oracle 开发机 Mini MCP alias: clocktower，目录 /home/opc/repos/CampBoardGameHost。
- Current local branch at start: experiment/personal-openai-direct-20261011, HEAD c01ddb3b61f638f7725230093563d24d4d5aed65; clean, remote same-name head equal. New **docs-only** commit after this checkpoint will advance HEAD; re-verify.
- GitHub main at audit: 3e1cc4070911f0aa9d6510f48041985a14cedebb; local main older (ddfca4e38f00e78950a15d2256eefd4a9d633914). Do not rebase/change base or merge without independent audit.
- [PR #292](https://github.com/Jazz0006/CampBoardGameHost/pull/292) is DRAFT, head prod-1-live-drunk-ai-assisted@c2dad5a9, base main; existing checks 7/7 GREEN.
- [PR #293](https://github.com/Jazz0006/CampBoardGameHost/pull/293) is DRAFT, head experiment/personal-openai-direct-20261011@c01ddb3b **before this documentation change**, base PR #292 branch; existing checks 7/7 GREEN. Keep stacked PR architecture unless user authorizes change. Green old-head CI ≠ acceptance of new design.

## 2. Confirmed real-phone feedback / outstanding product defects

1. In a setup containing Drunk, old AI Drunk recommendation is called **before** any identity reveal, with a blocking waiting screen. It still displays lengthy situation/issue/alternative essays. It has not used the identity-dealing time to run the one complete opening plan.
2. Single-legal-result Empath (e.g. both neighbours unambiguously Good) is unnecessarily included as a model choice rather than transmitted as deterministic context.
3. Tapping show on an AI recommended information result causes the Android app to exit. **Exact crash cause unknown** until a bounded AndroidRuntime FATAL EXCEPTION stacktrace; no unverified blame or global catch-all fix.
4. FirstNightOneShotRequestV1 currently requires SETUP_COMMITTED. A coded personal-direct AI_ASSISTED **post-Drunk** one-tap plan exists, but the actual Drunk seat cannot be optimized jointly with its first-night information/bluffs/red herring. It has not passed phone E2E. Prior dark UI contrast issue **accepted/adequate** on this pass; do not distract with a new theme redesign.

## 3. Product owner confirmed new direction

- **ONE initial model request** as soon as full shown roster is locked, running concurrently with shown-role-only player reveal. Not a Drunk request followed by a separate postcommit opening request. AI chooses Drunk + discretionary first-night information + red herring + legal Demon 3 bluff IDs as **one coordinated package**, and returns a private compact strategyMemory. Host uniquely determined information is passed as context and displayed by Host without model selection; future Poisoner/FT actions are deferred.
- Host owns legality/conditional candidate scopes for every hypothetical legal Drunk, snapshots, revisions, confirmed facts and actual publication. LLM owns noncanonical strategic preferences; never infer unique Spy/Recluse registration witness from a result. After actual events, update the **global affected remaining plan**, not individual-role AI policies. Player's already-seen information is immutable.
- Fix model to current experimental **gpt-5.6-luna**. **Do not compare another model or initiate training** this stage; improve instruction quality, complete inputs (especially player proficiency), legality encoding, compact outputs and strategy memory.
- Existing Responses path store:false and no conversation continuation: no automatic AI memory. Implement client-managed compact strategic context plus canonical Host event prefix before considering an optional API continuation design. Cross-game anti-repeat strategy is deferred.

## 4. Next implementation order / hard gates

| Phase | Immediately actionable work | Exit |
| --- | --- | --- |
| 3A P0 | Reproduce app exit with safe ADB Logcat AndroidRuntime FATAL EXCEPTION; trace recommendation → reveal handoff | Proven fix + regression + phone confirmation; no double/partial Host publication |
| 3B | Host setup-precommit **conditional global legal scope** with all lawful Drunk hypotheses, legal info/bluffs/red herring, deterministic context and player inputs | No standalone Drunk planning, no fabricated actual roles, complete legal coverage |
| 3C | SHOWN_ROSTER_LOCKED starts *same single* Luna request while cards are shown; one-tap accepted plan after dealing | Parallel UX, fresh Host validation, legal Drunk commit, no premature observation |
| 3D | Joint Luna prompt and openingPlan + strategyMemory strict schema | One configuration, no analysis wall; false/illegal IDs fail closed; private state later reusable |
| 3E | Host causal events trigger global residual-plan revision | Reuse valid pending choices; no per-role recommender, no past rewrite, manual fallback |
| 3F | Full Android 8-person TB and variations, independent CI/R2; retire superseded production AI paths | Human acceptance before Draft merge; no claim of complete AI_AUTOMATIC |

Regression anchor: previously discussed eight-seat Chef / Fortune Teller / Ravenkeeper / Butler / Investigator / Recluse / Baron / Imp with novice evil seats. Model must assess Chef–Investigator–Recluse–Drunk–bluff/red-herring interactions without hardcoding the example's proposed Chef-as-Drunk, information 0, Investigator 6/7, red herring 4, bluff trio. Test no-Drunk, late player choices, incompatible/stale candidates, one-tap/display, offline Manual.

## 5. Commands / tool workflow / preserved boundaries

- First read root [AGENTS.md](../AGENTS.md), [roadmap](CURRENT_DEVELOPMENT_ROADMAP.md), [PROD-GLOBAL-3](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md), [test strategy](TESTING_STRATEGY.md); inspect actual current code and Git, not the old long handoff or historical PR NEXT.
- For real-device crash: Android Studio Logcat filter `AndroidRuntime` or ADB `adb logcat -v time -s AndroidRuntime:E BotcAiTrace:I`; share only bounded relevant FATAL EXCEPTION and sanitized oneshot lines, never API keys/auth headers or full secret-bearing logs.
- Retain Manual entirely offline; Host truth > LLM plans/memory; no old heuristic resurrection; no new Recovery migration (current-format / ≤4-hour accidental-close only); registration-result witness ambiguity remains.
- Do NOT change PR #292/#293 from Draft merely on docs/unit CI. Keep current branch and use exact-state bounded diff review; CI/R2 checks after push are independent evidence, not user real-phone signoff.
- Current documents index: [docs/README.md](README.md). Earlier detailed design [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md) and experiment [PERSONAL-DIRECT-1](PERSONAL_DIRECT_OPENAI_ANDROID_EXPERIMENT_2026-10-11.md) remain useful **historical evidence**; contradictory single-Drunk/long-essay or mandatory model-comparison instructions have been superseded.

**Next chat begins at PROD-GLOBAL-3A and prepares unified 3B–3D architecture, not another Drunk-only model call.**


## TB setup simplification

TB presets use schema v3: full shown `townsfolk`, `drunk` boolean, no Drunk outsider or runtime three-choice selection. See `TB_SETUP_SIMPLIFICATION_TEMPLATE_AUDIT.json`; the late canonical Drunk assignment still happens after shown-role dealing.
