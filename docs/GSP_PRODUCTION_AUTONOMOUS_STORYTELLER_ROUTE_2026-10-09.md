# GSP-PROD-AUTO — Production LLM Recommendation to Autonomous Storyteller Route (2026-10-09)

> **LONG-TERM PRODUCT AUTHORITY, NOT A SECOND CURRENT IMPLEMENTATION SCHEDULE.** Full autonomous Storyteller remains the objective, but the exact present priority and confirmed status are maintained only in [current roadmap](CURRENT_DEVELOPMENT_ROADMAP.md), [handoff](NEXT_DEVELOPMENT_HANDOFF.md), and [PROD-GLOBAL-1/1D](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md).
>
> **2026-10-10 progress reconciliation:** PR #292 (Draft) now contains whole-game global strategy + several genuine Host first-night pair/numeric/Boolean AI_ASSISTED paths, Android secured Gateway connection persistence and model-continuation code. Oracle Gateway has completed three genuine paid Luna *synthetic* tests, but the Android real-play E2E, full initial overnight planning and fully autonomous runtime are **not** accepted. The former single Drunk spike and PROD-0 entry audit are historical. **Shown roles may be dealt before Drunk is bound**; the next main work is measured API latency and a single background whole-game first-night plan overlapping card reveal.

## Why this route

- The product owner has obtained stronger whole-game recommendation quality from a higher-capability conversational model (Sol) than from the lower-capability model used for the 2026-10-09 API0 pilot (Luna). This is an **observed qualitative difference**, not a blinded model benchmark or a verified mapping to API model IDs. Do not demand that the lower-quality model match the stronger model before integration. Production provider/model selection must be configurable, with quality-first selection and bounded spending.
- API0 has demonstrated genuine structured calls. Mini MCP DEV-EXEC1/2 has executed Oracle tasks. A read-only audit of private B2 reported **9 attempts / 9 successful / 9 response files**, 5,187 input + 12,667 output tokens; it returned no arm mapping. These are transport/integrity observations, **not** proof of excellent recommendation quality, a finished MEM0 blinded comparison or an Android provider connection. The B1 startup directory collision is fixed, and B1 must not be retried. Do not begin another paid batch just to extend MEM0.
- The production product must guide the roadmap. Extra Recovery replay, exhaustive synthetic history capture, 27-repeat MEM0 and trained-model infrastructure are **not preconditions** for the first live recommendation or autonomous TB vertical slice.

## Product operation modes

1. **MANUAL / offline:** existing rules, grimoire, legal choices and game flow work without LLM or network. Remains fully supported.
2. **AI_ASSISTED:** when Host exposes a pending discretionary decision, build its complete as-of provider request; an optional remote provider returns a ranked primary, genuinely different alternatives, rationale and uncertainty. Host checks response schema, IDs, decision identity, exact current revision and rules domain before displaying. Human Storyteller commits through **existing Host confirmation path**; provider never writes state.
3. **AI_AUTOMATIC (opt-in):** the user explicitly activates autonomous hosting. For exactly one rules-legal choice Host commits deterministically; for multiple choices request an LLM recommendation, validate identity/legal domain/current revision, and **Host** executes its existing legal decision-confirmation path automatically. No synthetic heuristic fallback and no unchecked model action. On response timeout, illegal IDs, missing information, unresolvable uncertainty, stale state, auth/network/budget failure, or unsupported decision, **pause and expose a bounded resolution/Manual handoff**. An autonomous mode cannot secretly choose arbitrary outcomes merely to remain unattended; it must report its coverage and interruptions.
4. **FULL TB AUTONOMY:** all required TB Storyteller arbitration families and setup/night/day sequencing are covered. Human players still need to give actions/choices, nominations, votes and whatever social/public information the product can actually observe; audio/social claims are not assumed known. Player input surfaces and onboarding are part of the product, not permission for the LLM to fabricate observed speech. Full autonomy must be assessed by end-to-end **real playable TB flow**, not by a demo for one Drunk case.

## Authority and state boundaries (non-negotiable)

```text
Host canonical GameState + rules-derived pending decision + full legal IDs
  + real recorded player actions/observations/claims/level/pressure when known
  + bounded chronology and fallible strategic notes (as-of cutoff, no future facts)
    -> versioned provider serializer / redaction / strict transport request
    -> authenticated, rate-limited, secret-owning server-side LLM gateway
    -> model-configurable ranked recommendation / alternatives / rationale / uncertainty
    -> Host response validator + re-read current pending decision/source revision
       -> AI_ASSISTED: show recommendation -> user-confirmed Host commit
       -> AI_AUTOMATIC: only valid in-scope recommendation -> Host-owned commit
       -> failure/unsupported: explicit pause/manual handoff; never legacy heuristic
```

- Host owns rules, night dependency barriers, legal candidates, deterministic consequences, private/public visibility, canonical facts, mode state and every commitment. LLM suggestions are advice, even in automatic mode. Preserve Spy/Recluse **result-first** registration ambiguity: do not fabricate a single registration witness to explain a legal displayed result.
- Model cannot set roles, invent hidden facts/claims, publish information, perform extra kills, change turn order, or commit an unspecified action. The provider request is immutable per call; when relevant state changes, reject stale replies instead of applying them.
- The existing `StorytellerProviderRequestV1`, `StorytellerProviderResponseV1`, factories, response validator and `StorytellerProviderGameContextBuilderV1` are starting points. **Do not build a second role-specific recommender.** Audit the real request's populated game context: API0's D0 synthetic prompt omitted critical *displayed roles* for alternative Drunk candidates even though the fixture authoring roster had them, so do not repeat that omission in production.
- Keep secrets off the Android APK/client, prompts out of Git/logs, use an **authenticated TLS gateway with quota, timeouts and cancellation**. Oracle VM can be used as a private development prototype **only** until external access, authentication and secret lifecycle are explicitly designed. Do not expose an unauthenticated developer endpoint.
- Do not silently equate chat UI model names with API model IDs or assert a model quality ranking without deployment-specific evidence. Allow provider/model configuration and observe quality, cost and latency from gameplay. No model training required for the initial rollout.

## Long-term implementation phases; current NOW is PROD-GLOBAL-1D, not the original PROD-0

| Product stage | Purpose | Current interpretation |
| --- | --- | --- |
| **PROD-0 (historical)** | Entry/ownership contract, first real Host decision | Entry audit already used; **not** next work |
| **PROD-GLOBAL-1C / PROD-1 (active Draft)** | Whole-game strategy, validated Host decisions, MANUAL/ASSISTED flow | First-night legal information families integrated, but actual Android E2E still missing |
| **PROD-GLOBAL-1D (NEXT)** | Real API latency decomposition, conditional Drunk + first-night full-board background planning during shown-role reveal, event-driven global residual updates | Under-5-second interactive updates are **an unproven acceptance target**, not a claim that short continuation is fast |
| **PROD-2** | Validate and commit opt-in automated decisions with pause/retry/manual takeover | Only current explicitly supported families; no invented default |
| **PROD-3** | Full legal TB setup and first-night automation | Every action/target/observation chronologically consistent |
| **PROD-4** | Whole TB automatic Storyteller | Real-play end-to-end rules, players' actions and error handling |

Earlier PROD-0/1 instructions below are retained only as historical decision rationale. Refer to the current roadmap/handoff before executing any item.

Keep PRs reviewable by coherent playable slices rather than splitting every field into a PR. Use minimal targeted tests for gameplay correctness, identity/staleness/security and full-slice regression; **do not** require new 9/27-sample blind quality research before PROD-1.

## Deferred or optional, not erased

- **GSP-MEM0:** B2 nine-response archive remains valid. Full blinded A/B/C scoring is **optional/background**, not a production launch gate. Known D0 data-completeness and some responses' rule claims should inform the live serializer and error review, not another long synthetic data program. Distinguish measured pilot transport from unproven memory superiority.
- **MEM1 / EvidenceLab:** resume purposefully when real-play recommendations identify an evidence/context gap or quality regression. Prioritize complete games and source-provenanced evaluations, not case-specific selector rules or more mandatory save history.
- **GSP-R2/R3:** player level, claims, pressure, continuity and cross-game diversity only as actual model-request inputs/experience surfaces that improve game decisions; use existing optional fields/defaults and avoid rebuilding a broad history database.
- **GSP-R1C2C-2/3:** remains paused, unless a real autonomous playable decision demonstrates a missing essential confirmed fact.
- **Recovery:** current-format, <=4-hour emergency game resumption only; no second replay engine. Manual remains offline-complete.
- **Model fine-tuning:** revisit only after production use and high-quality evidence expose persistent limits of configurable general-purpose LLMs.

## Current handoff pointer (replaces 2026-10-09 instructions)

**Begin PROD-GLOBAL-1D-0:** check live GitHub main / PR #292 / local branch and worktree, then instrument Gateway API timing and usage without secrets. Run the **same legal, chronological TB Host scenario** for initial and follow-up with configurable reasoning settings and compact versus full structured outputs; compare response quality. Next implement shown-role reveal parallel with first global preplanning and Drunk binding after dealing; then safe state/event invalidation and still-pending decisions. Retain this document for the long-term autonomous product stages; [handoff](NEXT_DEVELOPMENT_HANDOFF.md) is the only live next-task authority.
