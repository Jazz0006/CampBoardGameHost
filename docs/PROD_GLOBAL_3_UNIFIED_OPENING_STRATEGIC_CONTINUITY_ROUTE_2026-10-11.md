# PROD-GLOBAL-3 — Unified Opening Plan & Strategic Continuity (2026-10-11)

> **ACTIVE / PRODUCT-OWNER CONFIRMED.** Authoritative NEXT for TB AI_ASSISTED personal-direct Android experiment. Replaces old standalone Drunk-model request followed by post-Drunk first-night model request, long strategy essay UI, and multi-model comparison proposals. Historical documents are evidence, not active execution orders. **Design accepted; implementation/phone acceptance NOT completed.**

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

## 3B — Single Host precommit conditional legal scope

Create neutral InitialGlobalPlanRequest / response contract based on SHOWN_ROSTER_LOCKED / SETUP_PRECOMMIT, **before Drunk selection**. Include actual fixed non-Townsfolk, shown roles, seat adjacency, hasDrunk, and legal Drunk candidate domain. For each Drunk candidate, expose Host-derived **hypothetical** downstream legal information consequences without committing any actual role, plus no-Drunk case. Support compact lossless conditional domains without enumerating a prohibitive cartesian product. Include all applicable discretionary first-night information/registration results, distinct legal off-board bluff IDs, Good red-herring candidates. Distinguish unique Host-determined numeric/boolean result as **context** rather than discretionary candidate. Include actual per-seat experience where entered, claims/pressure if known, trusted causal/game history, and prior strategy when relevant. Preserve result-first Spy/Recluse witness ambiguity; never turn a possible explanation into an asserted registration.

## 3C — Parallel dealing, adoption and Host authority

SHOWN_ROSTER_LOCKED launches **exactly one initial LLM call**, foreground begins **shown-only** identity dealing without network barrier. Neither cards nor player screen can expose grimoire or unbound actual Drunk. After reveal, Host-only page offers one valid opening and one-tap adopt, with clear pending/error/offline manual fallback. Verify source game/roster fingerprint/revisions and whole selected conditional-domain coverage; bind actual Drunk via Host, then verify other pending entries under confirmed legal domains. Only publish each player-facing observation at its true night step; do not precommit info, registrations or future player actions. No extra mandatory post-Drunk initial request.

## 3D — Luna prompt, output and advisory memory

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

## Next session exact sequence

1. Check live GitHub main, both Draft PRs, local branch/HEAD/worktree, CI, AGENTS, roadmap and handoff.
2. Begin 3A by capturing bounded fatal stack and reproducing the show crash; fix and test only the verified cause.
3. Implement 3B–3D as **one architectural slice**: precommit global conditional domain + concurrent shown-only dealing + single joint Luna prompt/result and advisory memory. **No stopgap independent Drunk planning call.**
4. Add 3E globally event-driven remaining-plan revision, then 3F full real-device E2E and retirement/merge gates.

The archived PROD-GLOBAL-1D/2B logic is retained for audit but MUST NOT be read as permission to reintroduce independent Drunk or essay-first planning.
