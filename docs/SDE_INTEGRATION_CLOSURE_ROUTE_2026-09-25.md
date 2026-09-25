# SDE integration closure — implementation and acceptance contract

> Updated: 2026-09-25 Australia/Sydney
> Scope: close the canonical-history / durable replay / runtime shadow loop before adding SDE-3D2 features.
> Current status belongs only to [roadmap](CURRENT_DEVELOPMENT_ROADMAP.md); the continuation point belongs to [handoff](NEXT_DEVELOPMENT_HANDOFF.md).

## Why this precedes 3D2

SDE-3C0–3C5 completed typed modules and bounded tests, not application integration. `StructuredInformationProductionShadow`, trace factory/store and `MultiPolicyReplayEngine` do not yet form a reachable App end-to-end path. Historical evaluation needs initial committed setup; current App recovery does not restore `committedClocktowerSetup`. Existing topology benchmarks do not measure the actual historical shadow chain, including leave-one-out confirmation evaluations. These are integration gaps, not permission to discard the completed modules or replace canonical owners.

## Ordered checkpoints

| Checkpoint | Bounded implementation | Exit evidence |
| --- | --- | --- |
| C0 — correlation and route closure | Validate exact pre-commit history at the common correlator; reconcile current policy and documentation | Typed RED/GREEN; archive no-write-on-rejection and retry coverage; affected tests, FAST and debug build; explicit remote acceptance status |
| C1 — replay input contract | **LOCALLY COMPLETE** — versioned, read-only replay input/export using existing setup, rules/script identity, seed, canonical actions and observations | Fresh/durable origin is explicit; deterministic round-trip and baseline reconstruction covered; missing/unsupported/unknown shape fails closed; no process-memory-only setup prerequisite |
| C2 — offline vertical slice | **LOCALLY COMPLETE** — one existing numeric-information interaction through real legal candidate owner, historical consequences/features, trace, authoritative choice, archive reload and replay | Reconstructed real-game prefix plus deterministic typed integration test; equal version/seed/input gives equal output; manual choice preserved; no fabricated feature bundle presented as end-to-end proof |
| C3 — measured, controlled runtime shadow | **LOCALLY COMPLETE** — measured actual historical chain and connected the same slice as diagnostic-only shadow | 5-player actual-chain/history-depth measurements plus explicit 6–15 admission rejection; documented budget; cancellation, stale revision, restore, unavailable capability and I/O failure coverage; diagnostic failure never blocks a valid canonical commit |
| C4 — resume SDE-3D2 | Add generic truth-danger / credibility-disruption and contextual Red-Herring descriptive features through the accepted loop | Owner/fanout audit, E1/E2 semantic evidence, replay regressions; V1 output and frozen definition unchanged |
| C5 — evidence-backed policy and cutover | Create a new policy version only for independently qualified predicates, then evaluate per decision surface | E3/E4 as applicable, same-history version comparison, 3D0 gates, explicit fallback/override and legacy retirement plan; separate user authorization for release/merge |

Do not implement all checkpoints as one broad patch. Each checkpoint ends with an updated roadmap/handoff and its actual evidence. External targeted evidence acquisition continues in parallel. C1 does not authorize a general save-game redesign, Traveller expansion, a second solver or a second mutable truth store. Durable replay input is a versioned projection/export of existing authorities, not a competing authority. Define immutable input identity and compatibility before wiring UI.

## C1 accepted local contract

- `SdeHistoricalReplayInput` is the immutable transport projection. Schema v1 contains game/revision identity, exact `RulesetRef`, committed setup (including provenance and actual/shown roles), canonical seat names, seed through committed setup, action timeline, observation log and global cursor.
- `captureFresh` accepts only matching setup/snapshot script, seed and seats under `GLOBAL_V1`. The strict JSON decoder marks the materialization as `DURABLE_EXPORT`, so fresh and restored provenance is explicit without changing semantic input equality.
- Decode requires exact root/nested shape and the supported schema version. It reuses existing action/observation codecs and existing semantic-history invariants; it does not invent a parallel history codec or tolerate legacy-local inference.
- Reconstructed `GameSnapshot` is an initial-baseline projection with canonical history attached. Role definitions are resolved from the exact external ruleset identity; missing definitions fail closed. It is designed for C2 offline replay, not direct live-session restoration.
- C1 deliberately adds no App/Compose caller, SharedPreferences owner, generic recovery-schema change or policy output. C2 will consume this contract through one real numeric-information vertical slice.
- Local checkpoint evidence: focused C1/production-shadow/multi-policy replay GREEN; FAST 1521 tests / 350 suites with no failures, errors or skips; debug assembly and `git diff --check` GREEN. Remote T4/CI/R2 remains pending.

## C2/C3 architecture pre-flight

- current owner: legal candidates and confirmations remain `InformationDecisionContext`; canonical commits remain `ClocktowerGameSession`; historical evaluation remains `StructuredInformationProductionShadow`; traces remain `DecisionTraceArchiveStore`.
- proposed responsibility: one narrow offline coordinator composes C1 reconstruction → existing historical shadow → pending trace/replay input. One runtime coordinator adds cancellation/staleness/failure isolation and diagnostic persistence around that same operation.
- authoritative state owner(s): committed setup and `ClocktowerGameSession` snapshot/history remain authoritative; `ValidatedClocktowerRuleset` resolves rules; DecisionTrace storage remains diagnostic only.
- narrow typed input/output seam: C1 replay input + typed decision context + validated rules/role definitions → immutable shadow/trace/replay result; runtime wrapper → stored/stale/failed report. Confirmed choice is correlated only after the existing canonical commit succeeds.
- keep in current owner / extract: keep evaluation, confirmation, commit and archive invariants in their current owners; extract only orchestration into SDE-named coordinators. App receives two narrow callbacks (prepared numeric decision; confirmed structured decision) and owns no SDE semantics.
- reason: `CampBoardGameHostApp.kt` and `ClocktowerHostScreen.kt` are protected broad composition owners. A small callback/wiring edit is justified only to make the typed diagnostic path reachable; evaluation, timing, stale checks and failures must stay outside those files. No generic manager/context bag, visible recommendation change or blocking commit dependency.

## C2 accepted local contract

- `SdeOfflineReplayCoordinator` composes the C1 baseline with the existing `StructuredInformationProductionShadow`, `DecisionTraceFactory` and `MultiPolicyReplayInput`; it owns no legality, policy, canonical history or persistence semantics.
- The typed vertical regression uses a restored durable input and a real Empath Night-2 prefix containing a shown-role observation, phase advance and poison action. It exercises the production legal-candidate context, historical consequence/feature evaluation, pending trace storage, manual authoritative commit, exact history correlation, archive reload and V1 replay.
- Re-evaluating the same durable bytes, decision context, ruleset and role definitions yields the same recomputed replay input and trace content. The manual non-recommended actual choice and override rationale survive archive reload and replay.
- No feature bundle is hand-built as end-to-end proof; no V1 policy definition, canonical owner or visible recommendation changes.

## C3 accepted local contract and budget

- Runtime reachability is deliberately **Debug-only**, Trouble Brewing-only and diagnostic-only. A Compose `LaunchedEffect` keyed by immutable semantic decision state invokes the typed runtime coordinator; changing the decision cancels the old coroutine.
- Admission is currently exact: 5 players, at most 16 total action/observation entries, and a 1500 ms elapsed budget. Only an identity-current, within-budget result may enter `DecisionTraceArchiveStore`. Six through fifteen players return `INELIGIBLE` before historical evaluation; this is an explicit unavailable surface, not claimed support.
- Two forced local samples measured the actual 5-player chain at history depth 0 as **62–68 ms / 0 coarse heap delta**, and the real restored depth-3 C2 chain as **235–563 ms / 34,786,984–69,759,576 bytes coarse heap delta**. JVM heap deltas are diagnostic high-water observations, not allocation guarantees. Admission-only probes for 6/8/12/15 players completed in 319–371/5–7/2–3/2 microseconds respectively and did not invoke the evaluator.
- The production budget leaves more than 2.5x elapsed headroom over the observed depth-3 chain. Broader player counts remain disabled until their actual historical chain is independently measured and budgeted; this checkpoint does not generalize the 5-player result.
- Stale identity, cancellation, elapsed-budget breach, unsupported player count, storage rejection and thrown I/O fail without trace publication. Missing exact capability is persisted and reported explicitly as `STORED_DEFERRED`, never fabricated as ready.
- Structured confirmation still commits through `ClocktowerGameSession` first. Optional pending-trace lookup and exact correlation happen only afterward through a failure-isolated diagnostic coordinator; absence or failure cannot block a valid canonical commit.
- C1 strict durable restore plus the C2 restored real-prefix regression is the restore evidence for C3. The App does not gain a second recovery or mutable history owner.
- Final local checkpoint: focused C1–C3/correlation tests GREEN; `:app:testFast :app:assembleDebug` succeeded in 2m52s with **1528 tests / 352 suites, 0 failures or skips** and a successful debug APK assembly; `git diff --check` GREEN. These are not device UX, remote T4/CI/R2 or release acceptance. C4/3D2 may begin locally, while remote acceptance remains separately pending.

## C0 invariant and ownership

- Current owner: `DecisionTraceAuthoritativeChoiceCorrelator`; its production caller is `DecisionTraceArchiveStore.correlateCommittedChoice` and must inherit validation (no exemption).
- Authoritative state: session-owned `ActionFactTimeline` and `EpistemicObservationLog`; the trace stores references only.
- Seam: pending trace + authoritative confirmation + committed observation + post-commit session → finalized diagnostic trace.
- Keep validation in this owner; no new manager, persistence schema, policy version or gameplay mutation.
- Reconstruct the complete canonical prefix strictly before the committed observation's global sequence; compare ordered action and observation identity/sequence lists exactly. Reject missing, omitted, altered, reordered, current/future references and pre-commit records that are not strictly before the decision lifecycle.
- Preserve existing identity/revision/domain/commit-evidence checks. Later history must not itself invalidate an otherwise valid retry; do not weaken the existing game-state revision check to achieve this.
- Rejection must happen before an archive write. Exact successful archive retries remain idempotent.

## Policy authority and acceptance limits

`BEGINNER_CONSERVATIVE_V1` is frozen: exact zero Evil-topology rejection, otherwise survivor equivalence with `SEEDED_HASH_V1`. Descriptive features do not imply preferences. Librarian→Recluse / Investigator→Spy avoidance severity lacks E3 authorization; examples are diagnostic probes, not named-role policy branches. Narrative coherence is derived from canonical history, never a second mutable narrative state.

Local tests/build are local evidence only. Before remote acceptance, inspect the exact pushed head, resolve base conflicts with explicit authority, and obtain the applicable full CI/R2 gate. The prior `f562887c` FAST/R2 success is not T4 for this work. Do not merge, mark ready, rebase or push as an implicit consequence of this document.

Execution environment for this continuation: local Codex filesystem, Git and Gradle as explicitly requested; no Mini MCP or Oracle VM dependency.
