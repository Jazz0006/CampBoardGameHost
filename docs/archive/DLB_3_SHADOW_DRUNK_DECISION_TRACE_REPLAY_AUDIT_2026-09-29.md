# DLB-3 Shadow Drunk Decision / DecisionTrace / Replay — Architecture Audit

> Date: 2026-09-29 Australia/Sydney
> Baseline: `main@c2b293ff02cb8849673be9df9d973ea23eb8f0c1`
> Branch: `dlb-3-shadow-drunk-decision-trace`
> Scope: DLB-3 only — shadow SDE surface for late-bound Drunk assignment.
> Authority: `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`.

## 1. Key audit conclusion

DLB-2 already owns the complete legal candidate domain and immutable hypothetical `GameState` per dealt Townsfolk seat.

Existing SDE infrastructure already owns:

- `SdeDecisionCandidate` and `SetupPrecommit` lifecycle;
- `DecisionFeatureEvaluation`;
- frozen `BEGINNER_CONSERVATIVE_V1`;
- frozen `BeginnerConservativeV1Selector`;
- `DecisionTrace`;
- `MultiPolicyReplayInput` and `MultiPolicyReplayEngine`.

No second trace/replay framework is needed.

However, the existing Drunk whole-bundle evaluators solve a different problem: **after the actual Drunk seat is known**, they compare legal misinformation outputs for that Drunk shown ability. They are not themselves a Drunk-seat selector.

The existing first-night candidate-space auditor can inspect every DLB-2 hypothetical setup and expose its first-night ecology, including which information producer became Drunk and which producer domains remain. But there is currently no evidence-approved mapping from that setup-level ecology to a new V1 rejection/preference semantic.

Therefore DLB-3 must not manufacture a winner by changing V1 or by treating a deterministic tie-break as evidence.

## 2. Internal DLB-3 route

### DLB-3A — typed shadow envelope + ecology census + trace/replay

Build the setup-precommit SDE surface now:

```text
IntermediateSetup
-> rules-owned legal Drunk candidates
-> DLB-2 hypothetical GameState per candidate
-> existing FirstNightBundleCandidateSpaceAuditor per hypothetical
-> SdeDecisionCandidate envelope per legal candidate
-> current DecisionFeatureEvaluation contract
-> frozen BEGINNER_CONSERVATIVE_V1
-> DecisionTrace + MultiPolicyReplayInput
```

At DLB-3A, the ecology census is diagnostic evidence beside the SDE candidate. It is **not** silently converted into a policy weight/gate.

Until DLB-3B authorizes a setup-level strategic feature projection, the `strategic` feature is explicitly `NOT_PROJECTED_YET`. The frozen V1 must therefore return:

`Deferred(STRATEGIC_FEATURE_UNAVAILABLE)`

and `policySelection == null`.

This is a successful shadow result, not an error. It proves the candidate/trace/replay lifecycle while correctly refusing to invent policy semantics.

### DLB-3B — exact / whole-ecology feature bridge

After 3A, add only evidence-supported setup-level consequence projection.

Candidate projected state changes which Townsfolk function is actually impaired. A valid 3B must compare the **resulting first-night ecology**, not merely role names or asset provenance.

Existing reusable authorities include:

- DLB-2 projected `GameState`;
- `TroubleBrewingFirstNightBundleCandidateSpaceAuditor`;
- exact/topology evaluators;
- pair/numeric/Fortune-Teller Drunk whole-bundle evaluators after the candidate is materialized;
- current score-free SDE feature contract.

Important current limitation:

- the 7-player healthy-bundle harness explicitly stages Drunk out and cannot directly serve as a generic Drunk-seat comparator;
- player-controlled Fortune Teller targets and future Poisoner targets cannot be consumed as hindsight;
- not every Townsfolk shown ability has the same Night-1 information shape.

DLB-3B therefore needs a bounded setup-level consequence contract; it must not fake this by calling legacy scalar `SetupEvaluator` scores or by introducing role-name heuristics.

DLB-3 remains incomplete until 3B is accepted.

## 3. DLB-3A typed decision surface

Add a Trouble-Brewing-specific pure shadow owner under `clocktower/recommendation/sde`.

Request input:

- stable `gameId`;
- `TroubleBrewingIntermediateSetup`;
- validated Trouble Brewing character registry / role definitions;
- explicit `InformationDecisionRevision`.

The revision is supplied by the caller rather than invented by the shadow owner. Tests may use `0/0` for pristine setup-precommit.

For each DLB-2 legal candidate:

- stable candidate ID: `setup:drunk-seat:<seat>`;
- lifecycle: `SetupPrecommit`;
- source interaction ID = decision ID;
- input bindings = captured empty set;
- history prefix = canonical empty `Global(gameId)` prefix;
- legality provenance owner = `TroubleBrewingDrunkCandidateDomain`;
- hypothetical ref contains a stable projected-setup effect ref;
- proposed commit ref is emitted but **not committed/bound**.

Add `SdeCommittedDecisionInputKind.DRUNK_SEAT`. This is typed identity for the eventual DLB-4 commit, not a commit action.

## 4. Ecology evidence

For each candidate projected `GameState`, run:

`TroubleBrewingFirstNightBundleCandidateSpaceAuditor.inspect(projectedGame, roleDefinitions)`.

Retain the full typed `FirstNightBundleCandidateSpaceAudit` beside the candidate.

This captures, without scoring:

- first-night factor identities and legal option domains;
- rule-determined vs Storyteller-controlled factors;
- public-good-info exposure;
- player-controlled elements excluded from setup-time choice;
- staged complexities such as Poisoner target / Spy-Recluse registration.

It must not be copied into `DecisionFeatures` until a dedicated evidence-backed projector exists.

## 5. Frozen policy behavior at 3A

Create candidate-aligned `DecisionFeatureEvaluation.Ready` where every candidate uses:

`DecisionFeatures.unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET)`.

Run unchanged:

- `BeginnerConservativeV1Policy.evaluate`;
- `BeginnerConservativeV1Selector.select`.

Expected invariant:

- policy evaluation is Deferred with `STRATEGIC_FEATURE_UNAVAILABLE`;
- policy selection is null;
- no candidate is recommended/committed.

This is intentionally stronger than using seeded tie selection across evidence-equivalent candidates: 3A refuses to imply that a random deterministic tie is calibrated Drunk-assignment intelligence.

## 6. DecisionTrace / replay correlation

DLB-3A should construct a normal `DecisionTrace` using the frozen V1 definition:

- complete ordered legal candidate IDs;
- `SetupPrecommit`;
- explicit source revision;
- canonical empty Global setup prefix;
- 3A feature evaluation;
- deferred V1 policy snapshot;
- null selection;
- `actualChoice = Pending`.

It should also expose a normal `MultiPolicyReplayInput` so the existing `MultiPolicyReplayEngine` can replay the same candidate/features against registered policy versions.

No DecisionTrace schema change is needed for 3A.

## 7. Test boundary for 3A

A new typed owning test must prove:

1. DLB-2 candidate order is preserved exactly;
2. every legal candidate has one DLB-2 hypothetical state and one ecology audit;
3. every SDE candidate is `SetupPrecommit`, has the same revision/prefix, and points back to DLB-2 legality;
4. every proposed commit ref has new kind `DRUNK_SEAT`;
5. proposed commit refs are **not** present in committed input bindings;
6. ecology audits differ when candidate choice changes first-night role/function shape where the fixture makes that observable;
7. 3A feature evaluation explicitly reports unavailable strategic projection;
8. unchanged V1 returns `STRATEGIC_FEATURE_UNAVAILABLE`, with no policy selection;
9. DecisionTrace preserves the complete ordered domain and remains `Pending`;
10. MultiPolicy replay of V1 reproduces the deferred policy snapshot and still has no selection;
11. evaluation is pure and does not mutate the intermediate setup.

Because this adds a stable SDE decision surface and a new commit-reference enum kind, use test-first RED.

## 8. DLB-3A intended allowlist

Production:

- new `DrunkSetupShadowAdapter.kt`;
- `SdeDecisionCandidate.kt` only to add `DRUNK_SEAT` enum kind.

Tests:

- new `DrunkSetupShadowAdapterTest.kt`.

Docs:

- this audit and later roadmap/handoff closure.

Do not modify:

- `BEGINNER_CONSERVATIVE_V1`;
- selector;
- App/Host/UI;
- DLB-1 compatibility deal;
- DLB-2 legality/projector;
- session mutation;
- persistence/Recovery;
- first-night flow.

## 9. Explicit NO-GO

- no canonical Drunk commit;
- no automatic Beginner Drunk authority;
- no experienced-mode manual UI;
- no role-name ranking heuristic;
- no legacy SetupEvaluator scalar score as SDE Drunk policy;
- no future Poisoner-target hindsight;
- no assumed Fortune Teller target;
- no DecisionTrace schema expansion just to carry ecology census;
- no broad generic transaction/setup-effect owner.


## 10. DLB-3A RED / GREEN evidence

### RED

Test-only head:

`70c02992ba02046a514d8f3818755aa9a05f6c63`

GitHub CI #3524 / run `36515063391` failed Android compilation exactly because the new DLB-3A symbols did not yet exist:

- unresolved `DrunkSetupShadowAdapter`;
- unresolved `SdeCommittedDecisionInputKind.DRUNK_SEAT`;
- dependent candidate-shadow fields could not be resolved.

R2 #3266 was GREEN.

This is the expected test-first RED for the new typed shadow decision surface.

### GREEN / T1

Production head:

`3e940e61221ae56636032f198a4d6da20f052b30`

GitHub CI #3525 / run `36516847324` executed:

`./gradlew :app:testFast --no-daemon --build-cache`

and completed `BUILD SUCCESSFUL`. R2 #3267 was GREEN.

Exact executable scope:

- new `DrunkSetupShadowAdapter.kt`;
- `SdeDecisionCandidate.kt`: add only `DRUNK_SEAT` to the committed-input kind enum;
- owning `DrunkSetupShadowAdapterTest.kt`.

The adapter remains pure and shadow-only. It:

- preserves DLB-2 legal-candidate order;
- projects every legal candidate through the DLB-2 hypothetical projector;
- records one existing first-night ecology audit per candidate;
- emits typed SetupPrecommit SDE envelopes and unbound proposed `DRUNK_SEAT` refs;
- builds normal `DecisionTrace` and `MultiPolicyReplayInput`;
- leaves strategic features explicitly `NOT_PROJECTED_YET`;
- therefore receives frozen V1 `STRATEGIC_FEATURE_UNAVAILABLE` deferral and no policy selection.

No App/Host/session/persistence/V1/selector file changed.

### T4 escalation

The following documentation-only commit carries `[full-ci]` to escalate the exact DLB-3A executable tree above to full acceptance. It adds no production behavior.


## 11. DLB-3A T4 acceptance

Accepted DLB-3A executable checkpoint:

`0a59c29af047b91b5d10b62ce4019f60df632e83`

The executable production tree is the preceding `3e940e61221ae56636032f198a4d6da20f052b30`; the acceptance head adds only this audit evidence and full-CI trigger.

Exact-head remote acceptance:

- CI #3526 / run `36517120680`: GREEN;
- Android: `./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks` — GREEN, `BUILD SUCCESSFUL`;
- ASP contract tests — GREEN;
- Real Clingo cross-validation — GREEN;
- aggregate CI gate — GREEN;
- R2 #3268 / run `36517120713` — GREEN.

DLB-3A is COMPLETE / ACCEPTED.

The accepted semantics remain deliberately shadow-only:

- every DLB-2 legal Townsfolk seat is represented in the SDE domain;
- every candidate carries its immutable hypothetical setup and first-night ecology census;
- DecisionTrace and multi-policy replay use the normal existing SDE infrastructure;
- the proposed `DRUNK_SEAT` reference is unbound and non-authoritative;
- strategic projection remains explicitly unavailable;
- frozen `BEGINNER_CONSERVATIVE_V1` therefore defers with `STRATEGIC_FEATURE_UNAVAILABLE`;
- no candidate is selected or committed.

## 12. DLB-3B readiness conclusion

A follow-up live-code/evidence audit after 3A produced a NO-GO on directly converting the existing Drunk whole-bundle evaluators into a Drunk-seat selector.

Why:

1. `TroubleBrewingFirstNightDrunkPairWholeBundleEvaluator`,
   `TroubleBrewingFirstNightDrunkNumericWholeBundleEvaluator`, and
   `TroubleBrewingFirstNightDrunkFortuneTellerWholeBundleEvaluator`
   all require a **fixed actual Drunk seat** and then compare legal misinformation outputs for that already-fixed Drunk.
2. `TroubleBrewingFirstNightDrunkWholeBundleExactEvaluator` compares one fixed HealthyCore against those output candidates. It does not compare which Townsfolk should become Drunk.
3. `FirstNightBundleHealthyHarnessAcceptanceTest` explicitly excludes Drunk (along with Spy/Recluse/Poisoner) from its healthy exact harness, so it is not a generic setup-level Drunk-seat baseline.
4. Fortune Teller target choice is player-controlled and cannot be invented at setup time.
5. The current targeted EvidenceLab corpus supports multiple distinct assignment motives rather than one scalar seat heuristic:
   - suppressing dangerous healthy information (for example Empath / Evil-neighbour topology);
   - creating a useful or believable misinformation route (for example a Drunk Chef extreme-number plan);
   - broader player/role/setup-first Storyteller considerations.
6. The current EvidenceLab targeted assignment package remains short of its intended replay quota, so converting any one of those motives into frozen V1 strategic rejection/preference semantics now would overfit incomplete evidence.

Therefore DLB-3B must define an explicit setup-level consequence contract before any strategic projection is authorized. It must compare candidate-level **consequence envelopes**, not role names and not one arbitrarily chosen later misinformation output.

Until that contract is evidence-qualified:

- keep DLB-3A feature evaluation deferred;
- do not change `BEGINNER_CONSERVATIVE_V1`;
- do not invoke seeded survivor selection to create a pseudo-recommendation;
- do not use legacy scalar `SetupEvaluator` scores;
- do not treat output-level whole-bundle evaluation as assignment evidence.

DLB-3A can merge independently. DLB-3B remains the next DLB implementation/evidence gate, and DLB-4 must not treat the 3A shadow as automatic Drunk authority.
