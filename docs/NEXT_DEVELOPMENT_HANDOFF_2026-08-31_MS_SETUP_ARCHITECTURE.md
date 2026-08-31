# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Replanned: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6C COMPLETE / ACCEPTED — MS-S6D NEXT — MS-S7 BLOCKED ON S6D**

## 1. Live / accepted checkpoints

Live `main` at the replan audit:

`eed51bade5163790316a31e8295e2e841df90357`

Campaign branch:

`codex/ms-setup-generic-architecture`

Draft PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN / UNMERGED`

Accepted S6C code/test checkpoint:

`38a04c1353c883c3bda4b4a506085c3c1d2766bd`

This is an empty `[full-ci]` checkpoint over the exact focused-GREEN code tree:

`70e7f41d1e30e5e701c02ceb95660572a99d27d4`

Acceptance evidence:

```text
Focused GREEN:
70e7f41d1e30e5e701c02ceb95660572a99d27d4
CI #1289 / run 33393595657: Android FAST SUCCESS / CI gate SUCCESS
R2 #1206 / run 33393595784: SUCCESS

Full checkpoint:
38a04c1353c883c3bda4b4a506085c3c1d2766bd
CI #1290 / run 33393872108:
  Android :app:testFull + :app:assembleDebug SUCCESS
  ASP contract SUCCESS
  Real Clingo SUCCESS
  CI gate SUCCESS
R2 #1207 / run 33393872097: SUCCESS
```

The pre-replan branch head `e5a49800fb716e6f8254aa1e9126608a06a713df` was docs-only relative to accepted S6C. The current replan adds docs-only commits as well. None of these replace accepted code checkpoint `38a04c1...`.

Accepted slice checkpoints:

```text
MS-S1   f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b
MS-S1R  2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e
MS-S2   d4001863f134ebbe7d26819f40ac34c7d1de200c
MS-S3   6b15822e75680fb8e718f5db24358e1a935b5523
MS-S4   6de0e8c99c89a091615c513255adbdb773b3cc69
MS-S5   86c3ce651025de9ccbe1094b161becc171514e69
MS-S6A  5823d66d0eb756a0005df86f1aea7db5902cae60
MS-S6B  d4cf3969aabcea7433b96b5b320171fbc821853e
MS-S6C  38a04c1353c883c3bda4b4a506085c3c1d2766bd
```

Latest accepted checkpoint document:

`docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md`

## 2. Why this handoff changed

S6C remains accepted. Do **not** reopen or invalidate its checkpoint.

The replan follows a cross-check of:

```text
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/phase_a_exit_review_2026-08-20.md
docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md
docs/design_plan_audit_2026-08-21.md
current main production first-night information wiring
accepted S6C implementation/checkpoint
```

The global finding is:

1. A3 exact Possible Worlds correctness is already complete and accepted as the correctness baseline.
2. A4/ZDD and A4.5 cache infrastructure remain shadow/prototype, not production recommendation authority.
3. R6 already established the correct conceptual layering: role ability semantics first, then generic Drunk/Poisoned impairment policy.
4. Current first-night production is a migration hybrid: role-local/legacy candidate construction is wrapped by a newer unified first-night selection layer.
5. S6C proved the generic information architecture for migrated domains, but did **not** prove complete perceived-ability semantic coverage for every TB first-night information role a Drunk may believe they are.
6. Therefore going directly to S7 risks cementing an incomplete `actualRole`/`shownRole` boundary into production cutover.

The corrective action is a narrow new slice: **MS-S6D — First-night Perceived-Ability Semantic Completion**.

## 3. Frozen architecture through accepted S6C

The causal setup order remains:

```text
Composition
-> Identity
-> Information
```

Accepted ownership:

```text
script + playerCount + setupSeed
-> actual-composition candidates / legality          [S2/S3/S4 ACCEPTED]
-> actual-composition diversity selection            [S5 ACCEPTED]
-> shown-identity legal options                       [S6A ACCEPTED]
-> deterministic shown-identity commitment            [S6B ACCEPTED]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> perceived ability + healthy semantics where migrated [S6C ACCEPTED]
-> RELIABLE / DRUNK / POISONED generic impairment    [S6C ACCEPTED]
-> deterministic generic information output          [S6C ACCEPTED]
```

Governing rules remain:

- shown identity is a committed setup fact; recommendation may consume but never choose/reroll it;
- S5 sees actual composition only;
- S6A owns identity legality/options only;
- S6B owns deterministic identity commitment only;
- S6C/S6D own information semantics only;
- role-specific code owns legal result/display shape;
- generic impairment code owns DRUNK/POISONED truth-vs-false family behavior after the legal role semantic space exists;
- the same perceived role must not have separate Healthy/Poisoned/Drunk implementations.

## 4. Accepted S6C result — preserve, but read its coverage correctly

Accepted S6C pipeline:

```text
committed shown identity
-> perceived ability role
-> role-specific ability/display semantics
-> healthy information candidate space
-> InformationReliability.RELIABLE / DRUNK / POISONED
-> generic ImpairedInformationPolicy
-> generic consequence/history ranking
-> deterministic AbilityObservation
```

Accepted S6C behavior includes:

- active recommendation no longer owns/rerolls Drunk shown identity;
- legacy Drunk-Investigator payloads are compatibility-only where still required;
- Drunk and Poisoned share the same impairment-family semantics for supported common information domains;
- default product policy is approximately 90% false / 10% truthful and configurable;
- style primarily changes misinformation severity/ranking inside the selected family;
- healthy Investigator Recluse/Spy registration correctness is repaired without a broad registration rewrite.

**Important coverage clarification:** the existing generic pair-information seam currently demonstrates the architecture, but S6C acceptance is not equivalent to "all TB first-night perceived information roles are now typed and production-ready." S6D closes that gap.

## 5. Next slice — MS-S6D

### 5.1 Objective

Complete first-night perceived-ability semantics for the original v2.2 B2 Trouble Brewing information set:

```text
Washerwoman
Librarian
Investigator
Chef
Empath
Fortune Teller
```

The target contract is:

```text
committed perceived role
-> one role semantic evaluator / legal display space
-> healthy truthful result
-> RELIABLE / DRUNK / POISONED
-> accepted generic impairment policy
-> deterministic recommendation
```

Example of the required abstraction:

```text
Empath semantics
  ├─ RELIABLE
  ├─ POISONED
  └─ DRUNK
```

not:

```text
HealthyEmpathAlgorithm
PoisonedEmpathAlgorithm
DrunkEmpathAlgorithm
```

### 5.2 S6D-0 — first action: read-only coverage audit

Before writing REDs or production, audit the real current path for each of the six roles.

At minimum inspect:

```text
ClocktowerInformationStepBuilder
FirstNightInformationMigration
NaturalPairInformationCandidateGenerator
PairInformationAbilityRecommender
ImpairedInformationPolicy
ClocktowerRecommendationCoordinator
first-night UI/display projection that consumes legacyInformationCandidates
```

For each role, record:

| Question | Required answer |
|---|---|
| How is actor/perceived role resolved? | actual vs shown role explicitly identified |
| What owns healthy truth? | exact source named |
| What owns legal false candidates? | exact source named |
| What is the typed proposition/display shape? | pair / numeric / boolean |
| Does Spy/Recluse registration apply? | yes/no and layer |
| Is production candidate authority still legacy/role-local? | yes/no |
| Does a typed semantic source already exist? | yes/no |
| Can `actual Drunk + shownRole=X` reach X semantics? | must be proven |

Do not assume pair-information coverage generalizes automatically to numeric/boolean information.

### 5.3 RED design

Use risk-based behavior REDs only. Do not create source-string, field-existence, class-existence or exact call-chain tests.

A good minimal RED set should prove two information shapes rather than six copy-pasted role tests:

1. **pair shape:** an actual Drunk with a committed missing pair-information shown role, preferably Washerwoman, receives that perceived ability's legal information candidates without changing shown identity;
2. **numeric/boolean shape:** an actual Drunk shown as Chef or Empath reaches that role's semantic domain with `DRUNK` reliability;
3. the corresponding poisoned real role uses the same semantic domain with `POISONED` reliability;
4. a healthy holder still resolves the correct truthful result;
5. recommendation never mutates committed shown identity.

If the audit reveals a concrete additional correctness gap, add only the smallest behavior test needed for that risk.

Do **not** test the internal 90/10 constant directly. That accepted product policy stays unchanged in S6D.

### 5.4 GREEN boundary

Preferred implementation direction:

- reuse/generalize the pair semantic seam for Washerwoman/Librarian/Investigator rather than adding Drunk-specific branches;
- introduce the minimum typed numeric/boolean semantic boundary required for Chef/Empath/Fortune Teller;
- derive perceived ability from the committed shown identity for an actual Drunk;
- derive reliability independently (`RELIABLE`, `POISONED`, `DRUNK`);
- apply the same accepted `ImpairedInformationPolicy` after role semantics;
- preserve registration as a separate semantic layer;
- project the typed result into the existing first-night UI/migration adapter where necessary.

Do not implement A3/A4 Possible Worlds candidate scoring here.

### 5.5 S6D exit criteria

S6D is accepted only when:

- all six B2 TB first-night information families have an explicit audited disposition;
- all missing semantic categories needed for those families are implemented or explicitly shown already correct;
- there is no known path where a committed Drunk shown information role is skipped merely because no actual holder of that role exists;
- Drunk and Poisoned share role semantics and diverge only at reliability/provenance where appropriate;
- focused risk-based tests pass;
- logical-checkpoint CI/R2 and required full acceptance evidence pass;
- S6C accepted behavior remains unchanged except for intended coverage completion.

Create a dedicated S6D checkpoint document at acceptance; do not rewrite the S6C checkpoint as if S6C had failed.

## 6. MS-S7 — blocked until S6D acceptance

S7 remains the Trouble Brewing controlled semantic cutover.

Target production flow after S6D:

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6D-complete perceived-ability semantics
```

S7 owns:

- tracing/replacing remaining legacy TB setup-selection wiring;
- wiring the validated 480 templates into accepted S2–S6D ownership;
- TB-specific Minion/style diversity adaptation at an adapter boundary rather than generic S5;
- retiring `selectedDrunkShownRole -> preset finalWeight` coupling;
- making committed identity the single authority consumed by first-night information semantics;
- parity/shadow evidence before removing an old authority;
- preserving deterministic TB legality and behavior during the cutover.

`legacyInformationCandidates` may remain temporarily as a UI/compatibility projection. Do not delete it simply because typed semantic sources now exist. Remove/retire an old authority only after the new path is behaviorally proven and there is no dual ownership.

### S7-0 after S6D

Perform a fresh read-only path audit:

```text
validated TB template asset / repository
-> current production setup selection
-> candidate/provider registry
-> S5 selector
-> S6A policy
-> S6B committer
-> seat/deal materialization
-> CommittedClocktowerSetup
-> S6D semantic recommendation consumption
-> UI projection
```

Then establish only the durable behavior REDs required for the controlled cutover.

## 7. After S7 — restore the A3/A4 algorithm-consistency route

After S7 acceptance, **do not silently continue into ALG-B2R inside PR #61**.

The intended next algorithm campaign is:

**ALG-B2R — First-night Epistemic Gate resumption**

This is the deferred second layer that evaluates information quality using the recipient's exact Possible Worlds:

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> apply candidate observation
-> recipient PlayerWorldSet AFTER
-> epistemic metrics / quality gates
-> candidate ranking
```

Initial TB scope returns to the v2.2 B2 set:

```text
Washerwoman / Librarian / Investigator
Chef / Empath / Fortune Teller
Spy / Recluse local registration
```

This work is **not part of PR #61**. After S7 acceptance, choose the PR #61 disposition explicitly. Preferred default is to start ALG-B2R on a fresh follow-up branch from the then-current accepted `main` after the setup campaign has a clean landing point; do not stack it automatically.

Until ALG-B2R/C9 later authorizes otherwise:

- A3 EnumeratedWorldSet is the exact correctness baseline;
- A4/ZDD remains shadow/prototype and requires separate device validation before runtime promotion;
- A3/A4 results do not generally own production recommendation ranking;
- failures/time/resource limits never become false UNSAT;
- actual-world narrative metrics and player-world epistemic metrics stay separate.

## 8. What happens to S8 / S9

Default revised sequence:

```text
S6D
-> S7 TB controlled cutover
-> ALG-B2R separate first-night epistemic campaign
-> return to MS-S8 NGJ/no-template cutover
-> MS-S9 future-script acceptance
```

If product priority changes, S8 may be reprioritized explicitly, but do not let an agent infer that merely because S7 is complete.

The reason for the default pause is to stabilize one complete TB chain from committed setup identity through semantic information and exact player-knowledge evaluation before widening production setup support to additional scripts.

## 9. Longer-term algorithm boundary

The v2.2 route remains:

```text
ALG-B2R First-night Epistemic Gate
-> later historical/dynamic Phase B work as required
-> Phase C productive uncertainty / fairness / quality / distribution gates
-> C9 Unified Selector Production Rollout
```

C9, not S6D or S7, is the eventual general production authority switch for Possible Worlds-driven recommendation after correctness, quality, replay, distribution and device gates pass.

## 10. Non-goals for S6D / S7

Do not during the current PR #61 work:

- implement A3/A4 `before -> observation -> after` ranking;
- promote ZDD to production runtime;
- start NGJ/no-template cutover before the revised boundary is explicitly reached;
- change S5 generic diversity ownership;
- change S6A shown-identity legality without a concrete correctness defect;
- change S6B commitment namespace/algorithm;
- redesign the accepted S6C 90/10 policy;
- create role-specific Drunk/Poisoned recommendation strategies;
- perform broad persistence/recovery redesign — REC-R1;
- perform broad App/Host decomposition;
- aggressively delete compatibility schema;
- merge or mark PR #61 Ready without user authorization.

## 11. Protected predecessor invariants

Preserve:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
S6C/S6D recommendation cannot mutate committed identity.
Healthy/Poisoned/Drunk of the same perceived role share one role semantic domain before impairment selection.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality until explicit migration.

## 12. Validation workflow

Follow:

```text
AGENTS.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Use risk-based T0/T1/T2/T3 evidence.

- Begin with the smallest behavior RED that proves a real product/semantic gap.
- Use `:app:testFast` for logical T1 checkpoints.
- Escalate based on changed recommendation/rules/production scope.
- Logical acceptance checkpoints require GitHub CI/R2 and exact remote diff audit.
- Use full Android test/build and ASP/Real Clingo when required by the risk router.

No source-string tests, exact internal constant tests, field/class-existence tests, or exact helper-call tests when a stable behavior seam exists.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.

## 13. Documentation authority

Read in this order for the next turn:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md
docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md
```

For the global semantic boundary also consult:

```text
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/phase_a_exit_review_2026-08-20.md
docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md
docs/design_plan_audit_2026-08-21.md
```

Then consult predecessor MS-SETUP checkpoints only when a concrete ownership question requires them.

## 14. New-conversation resume guard

1. read root `AGENTS.md`;
2. read current roadmap;
3. read this handoff;
4. read S6C accepted checkpoint and do not reinterpret it as failed;
5. re-query live `main`, branch and Draft PR #61 head/state/checks;
6. distinguish docs-only carrier commits from accepted S6C code checkpoint `38a04c1353c883c3bda4b4a506085c3c1d2766bd`;
7. start **S6D-0**, not S7-0;
8. produce the six-role first-night perceived-ability coverage matrix before implementation;
9. establish only behaviorally meaningful REDs for real gaps;
10. keep A3/A4 epistemic scoring out of S6D/S7;
11. do not start S8/REC-R1/broad App/Host work;
12. keep PR #61 Draft and unmerged.
