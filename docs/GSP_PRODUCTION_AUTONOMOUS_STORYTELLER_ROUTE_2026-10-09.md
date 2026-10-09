# GSP-PROD-AUTO — Production LLM Recommendation to Autonomous Storyteller Route (2026-10-09)

> **ACTIVE PRODUCT DIRECTION / PRIORITY OVERRIDE.** Replaces the earlier MEM0-first research gate in the current roadmap/handoff. This is a **product decision and implementation route**, not a claim that API-1 or autonomous runtime has already shipped.
>
> **PRODUCT-OWNER RE-ENTRY (2026-10-09):** the immediate first objective is **whole-game situation diagnosis -> conditional strategic plan -> current Host-legal recommendation -> situation/relationship overview on the existing square table**, rather than invoking the LLM independently for each isolated clue. See [PROD-GLOBAL-1](PROD_GLOBAL_FIRST_STORYTELLER_STRATEGY_REENTRY_2026-10-09.md) for superseding implementation/UX and private single-user transport decisions. PR #292's standalone Drunk-screen recommendation remains **DRAFT / DO NOT MERGE AS IS**, despite green CI. Long-term objective: **fully automatic Storyteller** (no human Storyteller needed) with complete, independently running rules-engine authority.

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

## Near-term implementation order (do not turn into a research campaign)

| Order | Slice / actual consumer | Exit criterion |
| --- | --- | --- |
| **NOW: PROD-0 — architecture-to-code entry audit** | Identify the **real** Host pending-decision/confirmation owner, existing RES neutral provider request/response seam, available game context, Android coroutine/network permission situation and gateway deployment boundary. Choose a single **real TB discretionary decision** (prefer Drunk assignment) and locate exact test/UI consumers. | A short owner map and one bounded executable cut; **not** another large code/module refactor or MEM0 scoring campaign |
| **PROD-1 — AI_ASSISTED playable vertical** | Serialize **live Host** request with full role roster/candidates and as-of history/strategic summary, call configurable remote gateway, show legal primary and alternative with explanation, confirm via existing Host path. | One real TB decision from actual running app -> real model response -> correct Host confirmation; invalid, stale, timeout and offline test cases pass; manual remains available |
| **PROD-2 — autonomous decision gate** | Opt-in AI_AUTOMATIC mode; after fresh legality/revision validation commit via Host's existing confirmation barrier; dedupe network/resume requests; expose pause/manual takeover. | The **same** end-to-end decision can progress without human Storyteller confirmation; no wrong, repeated or stale application; model controls no game facts |
| **PROD-3 — first-night autonomous TB loop** | Incrementally cover additional first-night discretionary families (e.g. Investigator/Librarian pair, Fortune Teller red herring, poison/drunk effects) and preserve earliest/latest-safe sequencing. | Complete TB setup + first night for selected supported setup without human Storyteller decision; precise paused/unsupported report elsewhere |
| **PROD-4 — full TB Storyteller** | Iterate remaining setup, night, day adjudications, player action collection and terminal outcome; use genuine-play simulation. | TB play-through from start to game end without a human Storyteller, with explicitly recorded player input and no illegal or invented outcomes |

Keep PRs reviewable by coherent playable slices rather than splitting every field into a PR. Use minimal targeted tests for gameplay correctness, identity/staleness/security and full-slice regression; **do not** require new 9/27-sample blind quality research before PROD-1.

## Deferred or optional, not erased

- **GSP-MEM0:** B2 nine-response archive remains valid. Full blinded A/B/C scoring is **optional/background**, not a production launch gate. Known D0 data-completeness and some responses' rule claims should inform the live serializer and error review, not another long synthetic data program. Distinguish measured pilot transport from unproven memory superiority.
- **MEM1 / EvidenceLab:** resume purposefully when real-play recommendations identify an evidence/context gap or quality regression. Prioritize complete games and source-provenanced evaluations, not case-specific selector rules or more mandatory save history.
- **GSP-R2/R3:** player level, claims, pressure, continuity and cross-game diversity only as actual model-request inputs/experience surfaces that improve game decisions; use existing optional fields/defaults and avoid rebuilding a broad history database.
- **GSP-R1C2C-2/3:** remains paused, unless a real autonomous playable decision demonstrates a missing essential confirmed fact.
- **Recovery:** current-format, <=4-hour emergency game resumption only; no second replay engine. Manual remains offline-complete.
- **Model fine-tuning:** revisit only after production use and high-quality evidence expose persistent limits of configurable general-purpose LLMs.

## Immediate handoff

**Implement PROD-0 then PROD-1**. First confirm live repo state. Reuse existing Kotlin provider-neutral RES seam and existing Host confirmation actions; inspect whether `StorytellerProviderResponseValidatorV1` guards the *current* revision when applying results, not merely the request's self-contained revision. Implement secure gateway contract and one end-to-end TB decision before new role families. Maintain a narrow test surface (correct legal IDs, full current role visibility, one stale response, one timeout, no key in app, no duplicate commit). Report results with real runtime evidence, not benchmark-only success.

**Product-owner accepted priority:** strong LLM recommendation is promising enough to integrate first; the special goal is **fully automatic Storyteller**, rather than permanently human-confirmed assisted advice.
