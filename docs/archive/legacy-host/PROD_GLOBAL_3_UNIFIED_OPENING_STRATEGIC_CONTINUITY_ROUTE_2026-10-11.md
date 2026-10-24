# PROD-GLOBAL-3 — Unified Opening Plan & Strategic Continuity (2026-10-11)

> **HISTORICAL PRODUCT ROUTE; its "ACTIVE" marker below is superseded as of 2026-10-15.** See the [current roadmap](../../CURRENT_DEVELOPMENT_ROADMAP.md) and [continuous conversation architecture](../../AI_STORYTELLER_CONTINUOUS_CONVERSATION_AND_CROSS_GAME_MEMORY_2026-10-15.md). Keep the one-joint-opening decision, but do not follow old per-request memory or V1 retirement sequencing.

> **ACTIVE / PRODUCT-OWNER CONFIRMED.** Authoritative NEXT for TB AI_ASSISTED personal-direct Android experiment. Replaces old standalone Drunk-model request followed by post-Drunk first-night model request, long strategy essay UI, and multi-model comparison proposals. Historical documents are evidence, not active execution orders. **Design accepted; implementation/phone acceptance NOT completed.**

## 2026-10-12 PRODUCT OWNER DESIGN UPDATE — any role, any material event, multi-night first

Authoritative detailed contract: [3C/3D generic strategic continuity correction](PROD_GLOBAL_3C3D_SINGLE_JOINT_OPENING_CUTOVER_2026-10-12.md#2026-10-12-product-owner-correction--generic-whole-game-and-multi-night-strategic-continuity-mandatory).

The expert model **starts by examining the whole game and future nights for EVERY affected role**. The single opening output is a concrete legal now-plan PLUS a short, conditional, revisable strategy memory covering information-coherence and interaction over possible later nights. Not just the first-night snapshot. Any impaired role, not just Empath/Fortune Teller, changes future information ecology. Long-run coherence does not fix or fabricate a future number/answer.

All **material confirmed Host facts and Storyteller overrides** use one generic strategy revision mechanism, not separate Poisoner, FT, Drunk or role-specific recommenders. Host role/event-specific legal checks remain necessary but **never define the model's planning architecture or the update trigger**. After a manual changed Drunk assignment or any meaningful later action: combine actual Host prefix and previously retained strategy memory, compare KEEP/REVISE/RETIRE, globally update affected uncommitted plan only, freeze historical published facts. A statement or player claim is only a claim; don't pretend to know a human motive. No fixed sample configuration or output oracle.

This is the current strategic principle. Existing first-night Poisoner/FT-only gating code is a limited legality validator, **not** proof of general continuous Storyteller planning. Current PR #304 must still pass genuine model/phone/continuation acceptance.

## 2026-10-12 NEXT ROUTE — PROD-GLOBAL-3B3 semantic/quality convergence

3B2 actual Luna 4-case run is now observed; V1 JSON shape annotations reveal structural issues. **New active route:** [PROD-GLOBAL-3B3](PROD_GLOBAL_3B3_SEMANTIC_V2_AND_EXPERT_QUALITY_ROUTE_2026-10-12.md). Current production one-shot remains postcommit/candidate-centric until independent Host semantic adapter + whole-bundle acceptance are proven. Experimental V2 and prompt are not a cutover. Read previous 3B2 NEXT below as historical, and do not infer that green 3B2 CI means Host correctness.

## 2026-10-12 PRODUCT-OWNER CORRECTION — Expert Storyteller first (current authority)

**Supersedes older 3B2 pre-enumerated-candidate input instructions and the former "NEXT" section below.** Read [EXPERT-STORYTELLER principle & 3B2 test route](PROD_GLOBAL_3B2_EXPERT_STORYTELLER_PLANNING_PRINCIPLE_2026-10-12.md) before making any 3B2 provider/input changes.

- Goal: AI independently plans as an expert human Storyteller, not as a ranker of Host-enumerated possible outputs. Normal initial model input is the full game-world roster, whether Drunk exists and its uncommitted state, player proficiency (UNKNOWN when not recorded), confirmed facts, concise script/rules knowledge if needed, and optionally recent-game experience. **Do not by default enumerate every Drunk hypothesis and every first-night candidate in the prompt.**
- AI judges what is legal and strategically good; Host independently **validates** semantic decisions against its authoritative rules/revisions and commits them only at the lawful game step. Invalid output is refused, with specific bounded whole-bundle correction or manual fallback. Host legal-domain code remains essential **internally**.
- PR #297 completed **3B1** pure precommit conditional scope/validator and merged into `main` at `d720a845a113af0889360ed73717cdc9621f8abc`. Retain it for Host's safety boundary; no need to expose its entire branch/candidate enumeration to the model.
- **NEW NEXT: 3B2-EXPERT-PROBE first**, Luna-only real-model experiments on several TB setups *without supplied candidate menus*, separately scoring legal correctness, strategy quality, dependency completeness and cost/latency. After evidence, decide minimal Host/model adapter and proceed with 3C/3D one background joint call; do not prematurely implement the prior exhaustive 3B2 producer.
- Current relative role of older 3B/3D descriptions below: historical engineering option, **not current instruction** to build/enumerate a provider-facing menu. Original single-joint opening planning, deferred unknown actions, `strategyMemory` and Host authoritative validation remain in force.

## Invariant: one initial jointly optimized plan

Once Host fixes the visible roster, seating and **all shown identities** (SHOWN_ROSTER_LOCKED), start **one paid Luna whole-opening planning request** immediately while the foreground deals shown-role cards to players. Drunk actual seat remains UNCOMMITTED. A single model planning action must jointly choose Drunk when present, every *currently decidable discretionary* first-night clue, Good red herring as applicable, and three unique off-board Demon bluffs when TB rules require. Host's unique deterministic information must be supplied as strategy context, not a model choice. Unknown player-driven Poisoner target and Fortune Teller query seats are dependencies, not invented actions. Model returns **one proposed opening package** and **compact private strategic memory**, not multiple alternatives or analysis essays.

**Host's COMMIT timing does NOT define model's PLAN timing.** Bind Drunk with the existing Host committer after the global plan/reveal; do not call a second initial LLM simply because Drunk became confirmed. Only real changes to relevant conditions may trigger **global residual replanning of all affected still-open decisions**. Committed observations are immutable.

## Verified divergence on 2026-10-11

- CampBoardGameHostApp.startTroubleBrewingGame routes to ClocktowerDrunkSelection and requests the old Drunk-only candidate before committing and creating revealable cards. It blocks identity dealing during the API call. SETUP_INSTRUCTIONS/DrunkAiDisplay still request and render long whole-board essays and alternatives.
- FirstNightOneShotRequestV1 currently requires SETUP_COMMITTED and offers an integrated *post-Drunk* package. Its personal-direct AI_ASSISTED preflight/one-tap path has been coded, but cannot compare joint Drunk–Investigator–Chef/Empath–bluff–red-herring combinations. Existing result is only advisory except actual Host confirmations.
- Old Drunk request player experience defaults to NORMAL absent overrides; current postcommit one-shot payload lacks player proficiency and strategic continuity despite its prompt wording.
- Host's fromIntermediate snapshot correctly has shown roles plus uncommitted actual Townsfolk; DrunkCandidateDomain and DrunkHypotheticalSetup already model candidate/hypothetical states. Reuse rules-owned legality, never fake a committed Drunk or hardcode benchmark examples.
- Phone observations: Drunk call blocks reveal and returns essays; single-legal Empath result is redundantly returned; tapping to show recommended information exited the app. Crash root cause **NOT YET KNOWN** (need sanitized AndroidRuntime FATAL EXCEPTION stack). Theme readability accepted for this pass.
- PR #292 and #293 remain DRAFT and unaccepted until real Android E2E.

## 3A — Crash fix (P0)

Reproduce one-tap acceptance → show a recommended Investigator/Empath information result on a real legal TB fixture. Collect bounded AndroidRuntime fatal stack and safe BotcAiTrace events, no secrets. Inspect current-candidate/revision matching, onShowPlayerDisplay and Host publication/history/reveal handoff; fix **proven** exception and non-atomic/double-publication risks. Add regression. Never silence all exceptions, relax Host validation or leak private facts.

## 3B — Host precommit safety / expert-model interface (UPDATED 2026-10-12)

**3B1 is already merged (PR #297).** Its multi-hypothesis and legal candidate contract is an **internal pure Host validation foundation**, not a mandate to send a candidate list to Luna. 3B2 is now the **expert autonomous planning experiment** defined in the [current route](PROD_GLOBAL_3B2_EXPERT_STORYTELLER_PLANNING_PRINCIPLE_2026-10-12.md). Hold off adding production enumeration/projection/adapters until that experiment compares legal accuracy and strategic quality. The following paragraph documents the prior 3B2 proposal, which is **superseded for the model input boundary**.

### Superseded 3B2 candidate-enumeration proposal (reference only)

Create neutral InitialGlobalPlanRequest / response contract based on SHOWN_ROSTER_LOCKED / SETUP_PRECOMMIT, **before Drunk selection**. Include actual fixed non-Townsfolk, shown roles, seat adjacency, hasDrunk, and legal Drunk candidate domain. For each Drunk candidate, expose Host-derived **hypothetical** downstream legal information consequences without committing any actual role, plus no-Drunk case. Support compact lossless conditional domains without enumerating a prohibitive cartesian product. Include all applicable discretionary first-night information/registration results, distinct legal off-board bluff IDs, Good red-herring candidates. Distinguish unique Host-determined numeric/boolean result as **context** rather than discretionary candidate. Include actual per-seat experience where entered, claims/pressure if known, trusted causal/game history, and prior strategy when relevant. Preserve result-first Spy/Recluse witness ambiguity; never turn a possible explanation into an asserted registration.

## 3C — Parallel dealing, adoption and Host authority

SHOWN_ROSTER_LOCKED launches **exactly one initial LLM call**, foreground begins **shown-only** identity dealing without network barrier. Neither cards nor player screen can expose grimoire or unbound actual Drunk. After reveal, Host-only page offers one valid opening and one-tap adopt, with clear pending/error/offline manual fallback. Verify source game/roster fingerprint/revisions and whole selected conditional-domain coverage; bind actual Drunk via Host, then verify other pending entries under confirmed legal domains. Only publish each player-facing observation at its true night step; do not precommit info, registrations or future player actions. No extra mandatory post-Drunk initial request.

## 3D — Luna prompt, output and advisory memory

**Updated interface principle:** one semantic `openingPlan` plus private `strategyMemory`; seat/role/info decisions are concrete structured semantic choices, **not necessarily opaque Host candidate IDs supplied in advance**. Host translates and independently validates legal choices, fails closed if invalid, and does not silently substitute candidates. Keep model output short while its prompt explicitly demands internally comparing whole-game alternatives. Exact transport/schema is determined by the 3B2-EXPERT-PROBE results, not assumed complete now.

**Keep the same experimental Luna model (currently gpt-5.6-luna); no cross-model comparisons or model training now.** Improve the whole-board input and task instructions: identify interacting Good-information pressure, evil-player proficiency and positioning, alternative plausible worlds, legitimate Recluse/Spy ambiguity, cross-Drunk information consequences, bluffs/red-herring opportunity cost, conditional future intentions. Optimize for a fair, interesting, solvable game, never automatic Good/Evil victory. Jointly choose one legal package; return **only** strict structured IDs, version/coverage/dependency markers, no public essay or alternatives.

Response logical fields: openingPlan (Drunk/no Drunk + every now-decidable discretionary candidate + bluffs + red herring + deferred decisions), strategyMemory (compact private, revisable cross-seat tensions/hypotheses/conditional next steps, explicitly not canonical facts or hidden chain of thought), and asOf game/roster/revision marker. Host adds deterministic information to the UI package itself. Later model updates must compare previous strategyMemory with confirmed Host causal facts and KEEP/REVISE/RETIRE ideas. Current Responses transport uses store:false and does **not** provide automatic continuing conversation; use explicit bounded client-managed strategic summary + actual Host prefix at minimum. Server-side continuation is optional future engineering subject to privacy/token-cost considerations. Across-game anti-repeat memory is future scope, not this acceptance gate.

## 3E — Global residual-plan updates

For Host-confirmed changes in actual game, player knowledge or Storyteller overrides: first commit authoritative facts; evaluate original package dependency markers and current legal candidates; retain unaffected still-legal choices; otherwise use one **global** remaining-plan update grounded in the complete relevant as-of causal prefix and strategyMemory. No new isolated Poisoner/Drunk/Slayer/Virgin recommender; no replayed past player observation. Unknown claims/actions remain unknown. Measure Luna-only request count, latency, token usage/output length, quality; less-than-five-second residual update remains an **aspirational UX target**, not proven.

## 3F — Acceptance, cleanup and merge

Use the discussed TB 8-seat regression (Chef, Fortune Teller, Ravenkeeper, Butler, Investigator, Recluse, Baron, Imp; inexperienced evil seats) to test **interactions**, not hardcode the Chef-Drunk/Investigator/bluffs answer as oracle. Add other legal setups with no Drunk and actual Drunk, multiple information roles, deterministic Empath, changed Poisoner target, late FT query, ambiguous Spy/Recluse registration, stale/invalid IDs, duplicate/on-board bluffs, double-tap, offline Manual, and safe show/reveal crash regression. Verify identity reveal runs while single opening request is pending, one-tap adopt, Host fact publication only at rightful step, current-session recovery stays within existing scope, no player-facing secret leak. Validate real Android behavior plus independent CI/R2. **Only after successful acceptance** retire production standalone Drunk request/essay and superseded per-role AI authority. Keep legitimate Host rules/transport, no retired heuristics. PR #292/#293 stay draft until accepted.

## 2026-10-12 adopted prompt and engineering lessons / 3B1 checkpoint

- Prompt benchmark A/B/B2 and source-derived external project review are recorded in [PROD-GLOBAL-3P design note](PROD_GLOBAL_3P_PROMPT_DESIGN_AND_EXTERNAL_PROJECT_REVIEW_2026-10-12.md). Preserve comparative full-board strategy and risk self-check internally but emit only one typed opening plan plus advisory memory. Add mechanism opportunity costs and complete dependencies; do not hardcode the eight-player proposed bundle.
- Host-context quality: encode **confirmed facts versus absent/unknown evidence**, explicitly identify unentered social claims or incomplete knowledge, do not silently assume healthy status or `REGULAR` proficiency. Rule knowledge stays in structured Host context; strict host validators reject missing/extra/illegal candidates and unknown events regardless of the model wording.
- External Olalall project employs task-specific setup candidate ranking, single-wake-step night settlement and review prompts; its explicit `unknownSeatIds`, `needs_input` and independent `ready` outcome validation are reusable engineering lessons. Its per-role model-request architecture is **not** compatible with this product's one joint opening model call.
- **3B1 feature branch, not main yet**: a pure SETUP_PRECOMMIT scoped contract contains all legal Drunk hypotheses or only a no-Drunk hypothesis, per-hypothesis discretionary/determined/deferred decisions, source fingerprints and validation. It is **not** a legal-result producer. 3B2 must wire real Host legal results for all branches before enabling a provider call.
- At audit `main` is `2288d58`, #292–#295 merged, no open PRs, previous Draft references above/below historical. User's Android retest showed recommended information display without crash; root cause remains unproven and final safety acceptance remains 3F.

## Next session exact sequence (UPDATED 2026-10-12 — replaces historical sequence below)

1. Check live GitHub and Mini MCP `clocktower` Git state and current docs; verify docs-only principle PR/merge state before source edits.
2. Design and perform **3B2-EXPERT-PROBE** Luna tests with game-world inputs and no pre-enumerated choices, including the existing eight-player anchor plus diverse Drunk/no-Drunk and dependency/registration cases. Independent legality and whole-game quality scoring; preserve raw results.
3. Based on measured model competence choose minimal production 3B2 semantic result adapter and bounded rules context. Preserve Host independent validation and 3B1 internals without making candidate enumeration the model's default input.
4. Continue 3C/3D single background initial request while identities are revealed, 3E global residual replanning and 3F real-phone/CI/R2 acceptance and old code retirement. Manual/offline authority stays intact.

### Historical 2026-10-11 handoff (superseded)

1. Check live GitHub main, both Draft PRs, local branch/HEAD/worktree, CI, AGENTS, roadmap and handoff.
2. Begin 3A by capturing bounded fatal stack and reproducing the show crash; fix and test only the verified cause.
3. Implement 3B–3D as **one architectural slice**: precommit global conditional domain + concurrent shown-only dealing + single joint Luna prompt/result and advisory memory. **No stopgap independent Drunk planning call.**
4. Add 3E globally event-driven remaining-plan revision, then 3F full real-device E2E and retirement/merge gates.

The archived PROD-GLOBAL-1D/2B logic is retained for audit but MUST NOT be read as permission to reintroduce independent Drunk or essay-first planning.
