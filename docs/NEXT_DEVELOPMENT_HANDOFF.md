# CampBoardGameHost — Next Development Handoff

> Updated **2026-10-11 Australia/Sydney**. **唯一活动交接。** 进入新会话必须重新查开发机 live state / GitHub main、PR、CI，不能假设下述 commit 仍是最新。当前单一执行路线是 [PROD-GLOBAL-3](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md)；此文件只记录状态及下一步。旧 PROD-GLOBAL-1D/2B/analysis-first NEXT 作为历史，不得再次触发独立酒鬼模型规划。

## 当前并行清理：LEGACY-EXPERIENCE-RETIREMENT (2026-10-11)

当前工作分支 `legacy-experience-retirement-20261011` 基于 Draft PR #293（其基于 Draft #292），不更改两者原始分支。旧说书人“新手/熟练”运行模式、设置开关/SharedPreferences 键及三个候选推荐专属 UI 已在清理分支删除；Drunk 路由不再接收说书人熟练模式；Fortune Teller/红鲱鱼不能在没有 Host 唯一合法结果时任取首项；Spy/Recluse 多解登记不可被自动定为单个见证。独立合法候选的人工选择和 `PlayerExperienceLevelV1` 均必须保留。

正式操作权限仅有 `MANUAL`（全程手动且无 AI 请求）、`AI_ASSISTED`（推荐/明确采纳，不自动提交）与 `AI_AUTOMATIC`（只有 Host 核验当前合法性、版本和明确候选成功才可自动提交；失败安全暂停）。规则计算/唯一确定结果不取决于 AI。这是目标契约；**完整无人说书人尚未实现**。本清理不会修复 PROD-GLOBAL-3 旧独立 Drunk 请求的时序、Android 展示崩溃，亦不会分裂初始 Drunk + 全首夜一次联合规划目标。已创建 stacked Draft PR #295。角色简化夜间展示也须仅在唯一合法候选时展示快捷确认，绝不能因旧文本样式自动提交多个候选中的第一项。旧 CI/R2 仅针对 main；#295 已改为支持 stacked PR 直接触发，临时验收 PR #296 因与 main 冲突而关闭（从未合并）。#295 在 `2d1954f` 获 CI #4090 / R2 #3717 GREEN；本轮语义审查又发现 `allowRuleDeterminedAutoUi` 影响操作模式无关的 UI 排版、候选展示及 Pair 信息人工覆盖入口，已继续解除相关耦合，**需重跑更新后的 CI/R2**。最新 main `8ac78163` 已合入 #294 TB 模板简化，而 Draft #292 还基于其之前的 `3e1cc407`，现存在 merge conflict；不能覆盖新模板或直接强合。应按 #292→#293→#295 依赖顺序先解决上游冲突和集成复验，再重回 PROD-GLOBAL-3。

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

