# SDE-1E Structured Information Shadow Integration Completion

> Date: 2026-09-18 Australia/Sydney  
> Branch: `sde-1-orchestration-seam`  
> PR: #144  
> Status: **COMPLETE**

## 1. Scope closed

SDE-1E proves one real healthy structured-information path can be evaluated through the Storyteller Decision Engine in shadow mode without changing production selection, confirmation, flow, or durable session ownership.

Validated chain:

```text
existing structured numeric candidate generation
→ existing InformationDecisionContext / typed EpistemicObservationDraft
→ StructuredInformationShadowAdapter
→ StorytellerDecisionEngine.evaluateExactConsequences(...)
→ ExactHistoricalHypotheticalObservationBundleEvaluator
→ disposable PlannedDecisionRef provenance
→ existing InformationDecisionContext confirmation
→ explicit ClocktowerGameSession durable observation commit
```

No SDE diagnostic is used as production selection authority in this slice.

## 2. Production boundary

The completed seam uses:

- `StructuredNumberInformationUiModel.shadowDecisionContext` only to expose the already-existing Foundation decision context to a non-UI shadow consumer;
- `StructuredInformationProductionShadow.evaluateFirstNight(...)` to compose canonical production inputs;
- `CommittedClocktowerSetup` for the immutable first-night identity baseline;
- the current canonical `GameSnapshot` from `ClocktowerGameSession.toGameSnapshot(...)` for revisions and durable semantic history;
- existing role definitions and `EpistemicHypothesis`;
- `StructuredInformationShadowAdapter` for pure draft-to-hypothetical projection and exact SDE evaluation.

The UI does not own or invoke the exact evaluator directly, and no Compose rendering path became an exact-evaluation authority.

## 3. Freshness correction discovered during integration

SDE-1E exposed an important distinction:

```text
historical initialSnapshot revision
!=
current structured decision revision
```

`ExactConsequenceContext` therefore carries current game/input revisions independently of the historical baseline snapshot. This keeps historical replay semantics correct while allowing `PlannedDecisionRef` and `InformationDecisionContext` freshness to bind to the actual current session revisions.

Backward-compatible construction still defaults current revisions to the initial snapshot where that is semantically correct.

## 4. Ownership proof

Focused integration evidence proves:

- candidate legality remains owned by the existing structured/dynamic candidate pipeline;
- typed observation materialization remains owned by the established adapter/context path;
- exact world consequences remain owned by `ExactHistoricalHypotheticalObservationBundleEvaluator`;
- SDE receives the same stable candidate IDs and returns diagnostics beside, not instead of, the existing recommendation;
- visible numeric choices and recommended identity remain unchanged;
- `PlannedDecisionRef` stores only disposable identity/freshness provenance;
- shadow evaluation does not allocate a timeline point;
- shadow evaluation does not append an observation;
- shadow evaluation does not change game-state or player-input revisions;
- confirmation remains owned by `InformationDecisionContext`;
- confirmation alone still does not mutate the session;
- durable observation history and revision movement happen only when `ClocktowerGameSession.commitGlobalEpistemicObservation(...)` is explicitly invoked;
- exact capability deferral remains separate from production selection and has no heuristic fallback.

## 5. Executable evidence

Final executable SDE-1E SHA:

`e7bb31937db32863e5606044b443818011d16236`

GitHub Actions:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

CI run: `35285847867`

Live `main` at validation remained:

`4d6e90a7a268570d261048931a3557433ea01d83`

PR #144 remained draft, open, mergeable, and unmerged.

## 6. Non-goals preserved

SDE-1E did not:

- cut production selection over to SDE diagnostics;
- add BEGINNER Badness thresholds;
- implement Drunk uncertainty;
- implement Spy/Recluse uncertainty selection;
- implement Poisoner invalidation/replanning;
- migrate all `ConsequenceEvaluator` callers;
- delete `ConsequenceEvaluator`;
- create another state/history/revision owner;
- move durable commit into SDE.

## 7. Next stage

SDE-1 is now complete through SDE-1E.

Next current stage:

```text
SDE-2 — first-night uncertainty:
    Drunk
    → Spy/Recluse registration branches
    → Poisoner-driven invalidation/replanning
```

SDE-2 must build on the frozen SDE-1 ownership boundaries rather than widening the shadow seam into a second production authority.
