# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-18 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

Completed foundation relevant to the current program:

```text
D6 decomposition / ownership cleanup                  COMPLETE
Beginner / Experienced host modes                     COMPLETE
Same-night effective-state / transaction foundation   COMPLETE
EPI-MQ capability boundary                            COMPLETE / PR #135
Exact historical hypothetical bundle seam             COMPLETE / PR #137
First-night experiment contract                       COMPLETE / PR #138
FN-BUNDLE-0 candidate-space + pair ownership           COMPLETE / PR #139
FN-BUNDLE-1 proposition / ShownRoleAt semantics        COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle exact harness         COMPLETE / PR #142
SDE-0 BEGINNER strategic-robustness corpus             COMPLETE / PR #143
SDE-1A global fanout / orchestration seam audit        COMPLETE / PR #144 branch
SDE-1B thin bounded exact-consequence contracts        COMPLETE / PR #144 branch
SDE-1C exact-evaluator orchestration differential      COMPLETE / PR #144 branch
SDE-1D lifecycle ownership / planned freshness         COMPLETE / PR #144 branch
SDE-1E structured production shadow integration        COMPLETE / PR #144 branch
SDE-2A Drunk ownership / revision replanning contract  COMPLETE / PR #144 branch
SDE-2B Spy/Recluse exact registration witness binding   COMPLETE / PR #144 branch
SDE-2C Poisoner invalidation / broad replanning          COMPLETE / PR #144 branch
```

SDE-0 was squash-merged to `main` as:

`5dd32e085a7db0d3eb14ed8bce3ed3c75f694c6e`

SDE-1 authority/evidence:

- `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`
- `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`
- `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`
- `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`

Final executable validation head for SDE-1E:

`e7bb31937db32863e5606044b443818011d16236`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

Live `main` at validation:

`4d6e90a7a268570d261048931a3557433ea01d83`

Always query live `main` and PR #144 before executable edits.

## 2. CURRENT

**SDE-3 — cross-night impaired / registration decisions**

SDE-2 first-night uncertainty is complete.

Current objective:

```text
later-game interaction begins
→ derive current effective state from canonical session/history
→ existing rules/candidate owner generates legal impaired/registration outcomes
→ materialize typed hypothetical observation/effect
→ SDE exact historical evaluation
→ freshness-bound planning
→ existing confirmation/commit authority
```

Start with an audit before executable changes. Prefer one narrow later-night information interaction that exercises the already-proven Drunk/poison/registration boundaries without creating a second history/effective-state owner.

## 3. NEXT

```text
SDE-4  — production cutover + legacy heuristic retirement
```

## 4. SDE-1 completion summary

### SDE-1A — fanout / seam audit — COMPLETE

Frozen results:

- `ClocktowerGameSession` remains canonical actual-state/revision/history authority;
- rules/candidate domains remain legal-outcome owners;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` remains exact consequence authority;
- `InformationDecisionContext` remains structured-information freshness/confirmation boundary;
- flow remains interaction-ordering owner;
- host UI/coordinator are migration callers, not target authorities;
- SDE-0 healthy bundle harness remains experiment/evidence infrastructure, not runtime owner;
- `ConsequenceEvaluator` still has three production caller families that must all be migrated before retirement:
  - `DynamicCandidateGenerator.evaluation(...)` when state is supplied;
  - `RegistrationPolicy.generateCandidates(...)`;
  - `DayRecommendationModule` malfunction path.

### SDE-1B/1C — exact consequence seam — COMPLETE

Implemented under `clocktower/recommendation/sde`:

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
CandidateConsequence
ExactConsequenceEvaluation
StorytellerDecisionEngine.evaluateExactConsequences(...)
```

The exact evaluator remains the oracle. SDE does not select, commit, mutate session state, alter interaction ordering, or provide heuristic fallback.

### SDE-1D — lifecycle ownership — COMPLETE

Frozen lifecycle:

```text
PERSISTENT
    session/setup-owned durable commitments

COMMITTED
    session/history-owned executed/shown facts

PLANNED / UNCOMMITTED
    SDE-owned disposable identity/freshness metadata only
```

`PlannedDecisionRef` stores only stable decision/candidate identity plus existing revision/semantic freshness provenance.

### SDE-1E — structured production shadow integration — COMPLETE

Validated path:

```text
existing structured numeric legality/materialization
→ InformationDecisionContext
→ StructuredInformationShadowAdapter
→ StorytellerDecisionEngine
→ exact historical evaluator
→ PlannedDecisionRef shadow provenance
→ existing confirmation
→ explicit ClocktowerGameSession durable commit
```

Proof:

- visible choices/recommendations are unchanged;
- exact diagnostics remain shadow-only;
- historical baseline revision and current decision revision are explicitly separated;
- no shadow timeline allocation or observation append occurs;
- no shadow revision movement occurs;
- confirmation remains `InformationDecisionContext` ownership;
- durable observation/revision mutation remains `ClocktowerGameSession` ownership;
- UI/Compose does not become exact-evaluation authority.

Executable evidence: `e7bb31937db32863e5606044b443818011d16236`.

## 5. SDE-2 starting route

Start SDE-2 with a fresh audit before implementation.

### 5.1 Drunk — COMPLETE

Authority/evidence:

- `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`
- executable SHA `6c9d7fe8776fea4723004e22790ba11c1f140b8c`
- Android FAST and CI gate SUCCESS.

Frozen result:

```text
Drunk shown role      -> PERSISTENT setup/session truth
unshown Drunk clue    -> PLANNED / disposable
shown committed clue  -> COMMITTED / immutable history
Poisoner draft        -> playerInputRevision invalidation
Poisoner confirm      -> gameStateRevision invalidation
```

No SDE-specific dependency store or replanning revision counter was added.

### 5.2 Spy / Recluse — COMPLETE

Authority/evidence:

- `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
- core exact-registration semantics: `a977c01c0f4ae634dd60e4999759838f4005228c` with R2 / Android FAST / Real Clingo / CI gate SUCCESS;
- final SDE forwarding and pair projection: `ec970aaa302b7ea6ba5f869aec43cf8f3a82b950` with R2 / Android FAST / CI gate SUCCESS.

Frozen result:

```text
TroubleBrewingRegistrationDomain -> legality authority
WorldObservationResult           -> complete successful witness alternatives
ExactRegistrationWitnessBinding  -> one selected interaction-local witness
ExactConsequenceCandidate        -> forwards optional witness binding
PairInformationExactConsequenceAdapter
                                 -> pure projection from already-legal candidate
canonical player identity        -> unchanged
```

### 5.3 Poisoner — COMPLETE

Authority/evidence:

- `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
- executable SHA `fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`;
- R2 / Android FAST / CI gate SUCCESS.

Frozen result:

```text
Poisoner draft          -> playerInputRevision invalidation
Poisoner confirmation   -> durable hidden Poison action + gameStateRevision invalidation
uncommitted plans       -> stale across source revision changes
fresh planning          -> regenerate/re-evaluate through existing owners
committed observations  -> immutable
hidden poison target    -> excluded from recipient epistemic baseline
new lifecycle counter   -> none
```

Broad replanning was proven with both poisoned Empath and unaffected Chef. The unaffected role may keep the same legal IDs/diagnostics, but its old plan is still stale because its source revision is obsolete.

### 5.4 Acceptance target — COMPLETE

SDE-2 now proves:

```text
persistent setup commitments
+ current durable history
+ interaction-scoped registration/impaired uncertainty
→ exact consequence evaluation
→ stale-plan invalidation when source facts change
→ fresh replanning
→ existing confirmation/commit authorities
```

## 6. SDE-2 non-goals

Do not during SDE-2:

- cut all production recommendation selection to SDE;
- introduce a second session/history/rules authority;
- turn Spy/Recluse into permanent identity mutations;
- rewrite committed player-visible information;
- add cross-night policy beyond what is needed to preserve first-night semantics;
- retire `ConsequenceEvaluator` before all remaining caller families are migrated;
- introduce opaque global scalar optimization.

## 7. Frozen architecture decisions

- rules own legal outcomes and registration legality;
- session/game state remains actual-state and revision authority;
- flow owns interaction ordering/projection;
- exact epistemic evaluation owns hypothetical world consequences;
- `InformationDecisionContext` remains structured-information confirmation boundary;
- strategic evil topology matters more than raw role-world count;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- Poisoner may invalidate uncommitted decisions but not committed facts;
- BEGINNER / ordinary-player policy is the first profile;
- no opaque global-optimum scalar;
- lifecycle metadata references, never duplicates, durable session truth.

## 8. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE-2:

- tests-first for each uncertainty boundary;
- exact evaluator remains oracle where possible;
- prove stale-plan invalidation with existing revisions;
- prove no committed history rewrite;
- prove registration legality remains in the registration domain;
- keep expensive corpus/calibration work outside ordinary FAST regression;
- use the final executable SHA, not a later docs-only SHA, as validation evidence.

## 9. Authority documents for a new development conversation

Read in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
7. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
8. `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`;
9. `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`;
10. `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
11. `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
12. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` as architecture background;
13. query live `main`, PR #144 and current checks.

## 10. Stable rule

> **SDE-1A/B/C/D/E and SDE-2A/B/C are complete on PR #144. Final SDE-2C executable evidence is `fd90b8dc0433dbd925f0be9f94f3a2693e416f4b` with R2, Android FAST and CI gate SUCCESS. Current work is SDE-3: audit one later-game impaired/registration interaction and route it through the existing exact historical orchestration seam without duplicating session, effective-state, registration, or history ownership. Production selection remains unchanged until an explicit later cutover.**
