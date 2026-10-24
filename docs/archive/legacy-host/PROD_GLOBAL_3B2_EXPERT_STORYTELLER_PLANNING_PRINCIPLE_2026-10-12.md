# PROD-GLOBAL-3B2 EXPERT — 专家说书人优先的自主联合规划路线（2026-10-12）

> **ACTIVE / PRODUCT-OWNER CONFIRMED / supersedes previous 3B2 candidate-enumeration-first instructions.**
> Scope: `Jazz0006/CampBoardGameHost`, Trouble Brewing, first-opening LLM and continuing strategy. This is a product/architecture decision and NEXT experiment specification, **not a claim that the model has already demonstrated expert-level reliability**.
> See [PROD-GLOBAL-3 main route](PROD_GLOBAL_3_UNIFIED_OPENING_STRATEGIC_CONTINUITY_ROUTE_2026-10-11.md) and [3P A/B/B2 evidence](PROD_GLOBAL_3P_PROMPT_DESIGN_AND_EXTERNAL_PROJECT_REVIEW_2026-10-12.md).

## 1. Product goal, model authority and division of work

**The product aims to approximate a skilled human Storyteller, not an LLM ranking machine over pre-enumerated Host options.**

- **AI as expert Storyteller**: independently understand script rules, role interactions, registration ambiguity, player levels, information ecology, plausible player-visible worlds, fair Good solvability, Evil playability and whole-game entertainment; compare coherent opening bundles and choose one legal, thoughtful complete plan. AI should not shift elementary rule understanding to Host through boilerplate disclaimers such as "if Host allows a Townsfolk Drunk seat", when this is unambiguously known from the input.
- **Host as authoritative execution engine and independent safety net**: owns canonical setup and timing, rules, actual legal validation and commit, per-player secrecy, deterministic outcomes, deduplication, revision/fingerprint checks, manual/offline fallback and immutable published observations. The AI's understanding of rules is a desired competence, **not a legal guarantee**. Invalid or inconsistent bundles are rejected, never silently converted to the first legal candidate or partially published.
- **Joint initial strategy**: exactly one normal initial LLM planning request after the shown roster is fixed, while the application shows identities in parallel. When present, Drunk selection is planned **together** with first-night clues, red herring (if applicable), Demon bluffs and contingent future intentions. No required separate Drunk-only then after-commit one-shot calls.
- **Strategic continuity**: after verified player events or Storyteller overrides, refresh the affected *remaining whole package* using Host facts + concise private `strategyMemory`. Do not invent future actions, erase information already shown, or add single-character special-case recommenders.

## 2. Default input to the expert model: a game-world description, not a menu

**Required for initial planning:**
- Script identity/version and role rules/concise canonical rules reference when necessary; complete *shown* roster, seat adjacency/order, players' identities and currently authoritative actual facts.
- `hasDrunk`, plus explicit `UNCOMMITTED` versus `NOT_APPLICABLE`: if Drunk exists it has not yet been assigned; if absent there must be no Drunk selection. Drunk shown Townsfolk are not secretly treated as fixed actual roles before AI selection.
- Each player's recorded experience level; missing is `UNKNOWN`, not presumed `REGULAR`.
- Current game phase and confirmed events versus never-observed or not-recorded facts. Unknown Poisoner targets, Fortune Teller target pairs and conditional Ravenkeeper actions are **not** invented.

**Optional:**
- Bounded, privacy-conscious summaries of recent games / recurring lineup and player-experience outcomes. Their purpose is *moderate avoidance* of repetitive Storyteller decisions, not rigid prohibition, automated win balancing or a growing archive/recovery subsystem.
- In-game causal history and bounded previous strategic summary for later global residual updates.

**Do not send by default:** a complete list of every legal Drunk-seat branch, all pair-information combinations, Cartesian cross-products of roles/registration outcomes, or thousands of Host-generated candidate IDs/descriptions. A model should normally know that an uncommitted Drunk may be assigned to a legal shown Townsfolk on the current script.

Host can still provide focused, verified rule context for genuinely tricky or custom-script interactions; a model failure reveals the need for better rules knowledge, not a blanket instruction to enumerate all combinations.

## 3. Expert output and validation

- Return **one** structured `openingPlan` with **concrete semantic decisions** (Drunk seat if applicable, each currently decidable Storyteller information result, red herring when a Fortune Teller actually exists and script setup requires it, three legal off-board distinct Demon bluffs, clearly deferred event-dependent decisions) plus **short private `strategyMemory`**.
- Prefer concise, unambiguous semantic fields such as seat numbers, role IDs, roles shown to pair-information players, scalar result, and deferred event IDs. **Do not require the model to select opaque Host pre-enumerated candidate IDs as the default interface**. Host resolves/checks its choices against its own current legal domain and role rules when adopting.
- Separate genuinely deterministic outcomes (calculated by Host, not a model discretion) from actual Storyteller choices. Truth/validity for Recluse/Spy must use result-first semantics; possible registration witnesses are not automatically committed facts.
- All necessary model decisions must be present and legal at the specified as-of revision. If invalid: report the **specific rule/field** and optionally request **whole-bundle correction** against current facts (bounded, explicit cost/latency policy); if still unusable, return manual takeover. Do not automatically replace one item with a convenient local choice because that breaks global strategy.
- In normal successful output, avoid vague Host-legality disclaimers about obviously legal, confirmed facts. The Host validator runs independently and quietly.

## 4. How to reach genuine expert Storyteller quality

Three separable quality layers:

1. **Rules competence**: exact script/role/registration/time-of-effect knowledge; reliable legal choices. Evaluate legality with Host independently rather than rewarding confidence.
2. **Global strategic judgment**: judge information cross-validation, plausible player interpretations, meaningful alternate worlds, Evil players' experience and pressure, use/overlap of misinformation tools, chance of premature forced win vs unsolvable Good position, long-game coherence. Compare whole bundles internally; return only the best plan and minimal selection rationale, not an analysis wall.
3. **Experience continuity**: preserve intra-game strategic intentions, later use bounded optional recent-game summaries to modestly avoid repetitive surprises; no formulaic "always choose X" or source-specific fixtures.

Do not assume prompt improvements imply perfect legality or human-level judgment. Evaluate each layer separately.

## 5. NEXT phase — 3B2-EXPERT-PROBE, before more Host enumeration implementation

**Experiment first, without production code refactor:**
1. Build a representative **full-game-world JSON** input from verified game facts, recorded player experience and concise script rules. A/B compare *expert self-contained prompt without enumerated legal candidates* versus the current candidate-centric B2 reference at **same Luna model and consistent decoding/time budgets**. Do not vary models or assume prior 1-run latency differences are statistical evidence.
2. Test several legal TB setups, not just the known 8-seat case: one/no Drunk, pair roles, deterministic number, Spy/Recluse registration ambiguity, Poisoner pending action and FT query, candidate mechanism overlap, varied player proficiency. Use more than one replicate when budget permits. Do not hardcode Chef Drunk / Investigator 6-7 / red herring as the expected optimum.
3. Independently score **legality** (Host checks, zero fabricated/omitted applicable fields), **strategy quality** (cross-role and player-visible worlds, game experience, whole-bundle tradeoffs), **completeness** (deferred dependency honesty) and **performance** (latency, tokens, paid calls). Preserve raw responses and input manifests in Mini MCP's evolving **separate**, authorized experiment directory; no API keys/log leaks.
4. If standalone expert proposals are reliable and strategically strong, proceed with concise semantic input/output and post-validation; if failures cluster in rules knowledge, improve bounded canonical rules context and retest; if failures persist in narrow mechanics, provide **targeted** Host constraints to those mechanisms, not a new global enumeration architecture.
5. Only then decide the minimal 3B2 production adapter / 3C–3D API and UI integration.

**Status**: 3B1 pure Host `InitialGlobalOpeningScopeV1` and validators are already merged in PR #297. **Keep them** as an internal safety/validation artifact where useful; do not presume that their per-Drunk-branch candidate lists are a required LLM request shape. Do not delete 3B1 just to express this route. If the experiment validates this design, simplify/adapt the provider boundary, not the Host's legality authority.

## 6. Scope safeguards

- User is sole developer/current operator; no unnecessary cross-version migration or widening current-game Recovery.
- Maintain explicit `MANUAL`, `AI_ASSISTED`, `AI_AUTOMATIC` authority; LLM never directly mutates canonical state.
- No regression to retired weighted/ranking/heuristic style engines or per-role isolated LLM policies.
- This is **general expert competence** work. No Chef-seat-1 special-case prompt, test whitelist, override or "fix" for a single benchmark answer.
- Mini MCP generic experiments facility is being upgraded in parallel in its *own* repository. Do not touch that repo from Host changes. Use it if available, otherwise a bounded script/fixture can be used without compromising keys.
