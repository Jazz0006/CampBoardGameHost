# R6 / P1 Entry Audit

Date: 2026-08-21  
Baseline: `main` at `12f257486b3cee9cdff5ad3721cbae9e4336d608`  
Development branch: `codex/r6-timeline-point-foundation`

## Current R6 position

The historical R5/R5.5 gate in
`storyteller_revision_driven_dynamic_decision_engine_plan.md` is satisfied. Its Batch 0–6
execution records are already present, and Batch 7 has partial telemetry/rollout foundations.
Post-R5.5 work must therefore not restart Batch 0 or create another flow-order authority. A dynamic
decision point remains downstream of the script-aware `ClocktowerFlowPlanner` seam.

Formal multi-night player-world reasoning is still blocked by the three P1 semantic prerequisites.

## P1 audit

### P1.1 Spy Grimoire reminder tokens — open

`GrimoireSeatView.reminderTokens` is currently a canonical list of stable string IDs. The model does
not distinguish mechanically true tokens from storyteller notes, UI markers, or player-visible
artifacts. A production Spy perspective cannot safely consume this field until that rules boundary
is typed and covered by script/rule fixtures.

This is not the first slice because selecting token categories now would silently make a rules
decision without an existing contract.

### P1.2 observation timeline identity — foundation started

The code already had `TimelinePoint(phase, round, sequence)`, but it was used only by registration
queries. Durable observations sort by `round -> sequence -> recordId`; `ActionFact` uses its own
`Long sequence`; first-night UI observations use a local night-step index; public death observations
use the separate event counter. Those values do not define one cross-phase, cross-night identity.

The first vertical slice establishes the missing primitive contract:

- `TimelinePoint.globalSequence: Long` is required and non-negative;
- global sequence is the primary ordering authority across phase and round boundaries;
- phase, round, and local sequence remain replay/display context, not global-order authority;
- canonical registration-query JSON includes the global sequence;
- changing only the global sequence changes the serialized interaction identity;
- no UI, recommendation selection, action reducer, or observation persistence behavior changes.

This foundation does **not** close P1.2. Remaining slices must add a persisted per-game allocator,
migrate `ActionFact` and recorded observations to the shared point, define uniqueness/restore
validation, and decide exactly which timeline fields enter recipient knowledge identity and dynamic
recommendation digests. Existing saves require an explicit migration policy; local indices must not
be guessed into global identities.

### P1.3 actual truth vs knowledge-safe builder input — open

`FormalGameState` still contains actual roles, alignments, poison state, storyteller-only
propositions, and the B4 action timeline. `A4PlayerKnowledgeFactory` produces recipient-scoped
knowledge, but world-building entry points can still receive the actual formal state alongside that
knowledge. The next audit must introduce an explicit knowledge-safe build-input contract and prove
that secret-only changes cannot perturb recipient-visible identity or candidates.

This is broader than the TimelinePoint foundation because it crosses runtime, factory, enumerator,
cache-key, and golden/Oracle boundaries.

## Tests-first evidence for the selected slice

The new contract test first failed to compile because `globalSequence` and total ordering did not
exist. After the implementation it proves cross-phase ordering, JSON round-trip, and interaction
identity invalidation when global sequence changes.

Focused command:

```zsh
./gradlew testDebugUnitTest --no-daemon \
  --tests 'com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticModelTest'
```

Validation completed on this branch:

- focused semantic, player-knowledge, B4 shadow, and dynamic-key JVM tests: PASS;
- full `:app:testDebugUnitTest` plus `:app:assembleDebug`: PASS;
- ASP Oracle harness unit tests: 14/14 PASS;
- golden corpus validation: 52 scenarios PASS;
- local R2 structural boundary verifier: PASS;
- `git diff --check`: PASS.

The real-Clingo cross-validation was not rerun locally because this environment has no `clingo`
binary or Python module. This slice does not change the golden corpus, ASP adapter, formal state, or
world enumeration; normal CI remains the authoritative real-Clingo gate.

## Next smallest slice

After this foundation is green, the next P1.2 slice should be a pure session/domain allocator and
restore contract for monotonically assigning `TimelinePoint.globalSequence`. It should be tested
independently before changing persisted observation or action schemas.
