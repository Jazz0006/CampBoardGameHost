# GSP-R1B — Neutral GLOBAL_V1 Live History Prefix Materializer Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **GSP-R1B COMPLETE / ACCEPTED — live-prefix foundation only**  
> Executable PR: [#250](https://github.com/Jazz0006/CampBoardGameHost/pull/250); squash merge `8e788db2f6ad0be1f3c8b1c844306d6210e66cd7`  
> Exact PR head: `b35884961c0f993896a7b92e5a43e87b6069492b`  
> Independent checks: CI **#3849 GREEN**, R2 **#3518 GREEN**. CI ran Android FAST unit tests and Real Clingo; full Android `testFull`/debug assemble was **skipped** at this checkpoint. Do not mislabel FAST as full Android T4 acceptance.

## Accepted narrow R1B contract

The neutral provider context now has an **optional** `historyPrefix: StorytellerProviderHistoryPrefixV1?`, so older constructions remain valid; `null` means not supplied, **not** a verified empty/complete history. The Host's `StorytellerProviderGameContextBuilderV1.build(snapshot, revision, sessionState)` captures a single Session state and attaches the typed immutable prefix.

New files:

- `clocktower/domain/StorytellerProviderHistoryPrefixV1.kt` — neutral chronological Action/Observation entries, exact source revision, game ID, live-exclusive global sequence, cutoff provenance, honest coverage by dimension, deeply defensive nested proposition/recipient collections.
- `clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1.kt` — read-only snapshot-to-prefix conversion; revision-locked Session, GLOBAL_V1 action/observation merged chronology, fail-closed LegacyLocal mode.
- `clocktower/session/StorytellerProviderGameContextBuilderV1.kt` and `clocktower/domain/StorytellerProviderContractV1.kt` — request identity/revision constraints, scoped attachment; decision events lacking a causal cutover bridge are **not** smuggled into a globally ordered Session-based prefix.
- `StorytellerProviderHistoryPrefixMaterializerV1Test.kt` and `StorytellerProviderRequestFactoryV1Test.kt` — typed chronological capture, same-revision future exclusion using frozen original Session state, deep nested immutability, LegacyLocal unavailability, malformed cutoff, revision mismatch, and provider context attachment.

The accepted producer coverage markers are intentionally conservative: **MECHANICAL PARTIAL**, **PRIVATE_INFORMATION PARTIAL**, **PUBLIC_NARRATIVE PARTIAL**, **REGISTRATION_RULINGS UNKNOWN**, **PRIOR_DECISIONS UNKNOWN**. `LEGACY_LOCAL` returns `UNAVAILABLE` / `UNRECONSTRUCTABLE` for every dimension without invented global timestamps. A `LIVE_CAPTURED` cutoff is exclusive; `globalSequence < exclusiveGlobalSequence`.

## Boundary limitations — not completed by R1B

1. **Not a complete game narrative.** Typed producers still cover only known action types and some player-observed information. Textual `ClocktowerEvent`/localized history is not semantic truth.
2. **No historical decision replay.** R1B materializes *live frozen Session states* only. There is no accepted historical `FROZEN_EXPLICIT` API or persisted decision-time identity/cutoff/revisions/player context; never generate a historical prefix from the current cursor.
3. **No complete decision archive.** The old revision-filtered effective `DecisionHistoryArchive` cannot establish causal order against action/observation timeline or later corrections; Session overload does not elevate it to a chronology.
4. **No canonical Spy/Recluse registration producer.** Result-first UI dedup may choose one witness, but that candidate is not proof of an explicit Storyteller ruling. Follow [accepted ambiguity contract](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md): observed result canonical; candidate witness derived; explicit adjudication optional. R1B reports **UNKNOWN registration coverage**, never fabricates a fact.
5. **Not a live production provider.** Current request factories/builders still lack general automatic production consumer; no API, trained model, ranking authority or named policy was added. Manual/offline operation is unchanged.

## Next implementation: GSP-R1C — replay/recovery and frozen-prefix acceptance

At the next slice, check freshly observed live HEAD and review `AGENTS.md`, `TESTING_STRATEGY.md`, [R1A audit](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md), [registration ambiguity contract](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md), and this closure first.

1. **Fresh/Recovery equality.** At the same frozen logical Session/cutoff, provider prefix must compare equal across a real current-format Recovery encode/decode/restore, preserving action+observation ordering, reliability, recipients, immutable provenance and coverage flags. Include cursor/collision/next-sequence validator cases.
2. **Historical cutoff gate.** If a genuine old decision-time identity/cutoff/revisions/frozen player context does not exist, report `HISTORICAL_CUTOFF_UNAVAILABLE`; never reconstruct one from game revisions, phase or present Session. A separate durable bridge may be justified, but do not broaden current-only Recovery into a complete cross-game archive.
3. **Correction/decision causal proof.** Verify applied/corrected decision exposure across an exact old prefix; no later correction may rewrite prior recommendation input. Retain explicit missing coverage where old events cannot be causally bounded.
4. **Mixed semantics.** Test FirstNight/OtherNight/Day events, poisoning/drunkenness vs perceived observation reliability, action-before-observation and vice versa, invalid/untyped/missing registration producer handling, and interactions involving Spy/Recluse. The illustrated functioning Empath=1 with Spy+Recluse has multiple legal witnesses: **none is committed** unless explicitly selected. No false highlighted contributor or cross-interaction carry.
5. **Producer/UI follow-up remains separate.** New canonical typed explicit registration adjudication and ambiguity-aware Storyteller controls/Recovery need a dedicated bounded producer-and-UI implementation audit; do not quietly add it as a historical replay “fix.”
6. **Acceptance:** focused T0 tests + :app:testFast T1; T2 affected Recovery and stage-specific suites, CI/R2 T4 when migration impact warrants. Document which suites actually ran. No network provider production before blinded GSP-3A.

Route after accepted R1C: R2 player experience/claims/pressure context -> R3 cross-game memory/diversity -> R4 structured prompt/result -> GSP-3A blind quality benchmark -> only then reconsider optional production LLM integration.
