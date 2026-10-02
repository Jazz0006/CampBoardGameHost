# DLB Drunk Assignment C3-Q04 Re-entry Audit — 2026-10-02

> Status: **IMPLEMENTATION + REPLAY COMPLETE / BEGINNER WIRED / T4 CUTOVER ACCEPTANCE PENDING**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> Host implementation base: `main@048caaef8386f6d1821b31a78d1a0b32b86fb8af`
>
> EvidenceLab accepted checkpoint: `08d95a0c258f687187c0476a0f430fa5ff8229cb`
>
> Scope: map the first VERIFIED C3 Stage-1 Drunk comparison into the existing TBGS-1 decision boundary and determine the smallest evidence-authorized production policy slice.

## 1. Evidence handoff accepted

ClocktowerEvidenceLab C3-Q04 is now human-verified from primary audio:

- source: `12: Monk (Trouble Brewing)`;
- source ID: `podcast:91574d66c261bb0dd8bcb08469531a9c`;
- GUID: `e8241f55-824b-4dc5-8314-db398b5ece55`;
- primary-audio window: `00:43:28–00:44:30`;
- EvidenceLab handoff: `docs/C3_Q04_VERIFIED_MONK_CONDITIONAL_PREFERENCE_HANDOFF_2026-10-02.md` at EvidenceLab checkpoint `08d95a0c258f687187c0476a0f430fa5ff8229cb`.

Verified bounded meaning:

1. the Drunk is chosen after inspecting the seated layout;
2. the source describes an Empath seated between two Good players and away from the Demon;
3. under that condition, Monk can be preferred as the Drunk;
4. the reason is to preserve healthy Empath / information-role information.

This satisfies the Host re-entry requirement for one bounded candidate preference with explicit context, alternatives, rationale and provenance.

## 2. What the evidence authorizes

Q04 authorizes only one pairwise conditional semantic:

```text
under the source-described layout condition:
Monk may be preferred over Empath for Drunk assignment
because doing so preserves healthy Empath information
```

It does **not** authorize:

- a global Monk > Empath ranking;
- Monk > any other Townsfolk;
- non-information-role > information-role as a general rule;
- a numeric score or weight;
- mutation of `BEGINNER_CONSERVATIVE_V1`;
- promotion of `DRUNK_ASSIGNMENT_SHADOW_V1` into production authority.

All unmentioned candidate comparisons remain unknown.

## 3. Existing Host context is sufficient

No TBGS snapshot extension is required.

The accepted `DrunkAssignmentDecisionContext` already provides:

- the setup-precommit `TroubleBrewingGameSnapshotV1`;
- rules-owned legal candidate refs with seat + shown role ID;
- stable decision identity and seed.

The accepted `DrunkAssignmentFeatureEvaluation` already provides candidate-aligned topology:

- previous/next seat;
- adjacent Evil seats;
- adjacent Demon seats;
- adjacent Minion seats.

For the named Empath candidate, the source-described “between two Good players / away from the Demon” condition can be represented conservatively by the existing topology projection:

```text
Empath topology is projected
AND adjacentEvilSeats is empty
AND adjacentDemonSeats is empty
```

The Demon clause is redundant when adjacent Evil is empty, but retaining it in the contract keeps the evidence wording visible.

No first-night-information, longitudinal-narrative, player-experience, recent-role-history or public-claim enrichment is required for this first predicate.

## 4. Smallest production-capable policy boundary

The first version must preserve all unproven ordering semantics.

Recommended version identity:

```text
DRUNK_ASSIGNMENT_Q04_V1
```

Required inputs:

```text
DrunkAssignmentDecisionContext
+ DrunkAssignmentFeatureEvaluation
+ current compatibility-confirmed candidate identity
```

The compatibility candidate is required so the policy can act as one evidence-bounded override rather than manufacturing a total ordering.

### 4.1 Predicate

```text
IF
  compatibility candidate is the Empath,
  Monk is also in the current rules-legal candidate domain,
  Empath topology is available,
  Empath has no adjacent Evil seat,
  and Empath has no adjacent Demon seat,
THEN
  select Monk
  with reason = preserve healthy Empath information
ELSE
  preserve the existing compatibility candidate
  with explicit compatibility-fallback disposition
```

This compares only the evidence-authorized pair. Other legal candidates are neither preferred nor rejected.

### 4.2 Unavailable / fallback behavior

- missing Monk candidate -> compatibility fallback;
- compatibility candidate is not Empath -> compatibility fallback;
- required Empath topology unavailable -> compatibility fallback;
- Q04 topology condition not met -> compatibility fallback.

Fallback is continuity of the already accepted transitional behavior, not a newly inferred candidate ranking.

## 5. Replay requirement

Before production wiring, replay the new version against available reconstructable Drunk cases.

Replay acceptance is diagnostic, not “match the historical expert choice”.

Required checks:

1. legal domain identity is unchanged;
2. Q04 override can select only the legal Monk candidate;
3. the policy cannot override a non-Empath compatibility candidate;
4. the policy cannot override when Monk is absent;
5. the policy cannot override when the Empath topology condition fails or is unavailable;
6. unmentioned candidates receive no ordering semantics;
7. `BEGINNER_CONSERVATIVE_V1` and `DRUNK_ASSIGNMENT_SHADOW_V1` remain unchanged.

## 6. Cutover gate recheck after C3-Q04

| Gate | Result | Reason |
| --- | --- | --- |
| Legal Drunk candidate domain | **PASS** | Existing TBGS-1 rules-owned domain. |
| Candidate consequence / topology projection | **PASS** | Existing candidate-aligned topology expresses Q04. |
| DecisionTrace / replay support | **PASS** | Existing Drunk replay lane is available. |
| Current Beginner compatibility fallback | **PASS** | Remains legal and is the explicit non-Q04 fallback. |
| Cross-project snapshot semantics | **PASS** | Already accepted by TBGS-1. |
| Evidence supports a bounded production preference predicate | **PASS** | VERIFIED C3-Q04. |
| Production-capable versioned Drunk selection contract | **PASS / IMPLEMENTED** | `DRUNK_ASSIGNMENT_Q04_V1` is implemented as the bounded Empath -> Monk override plus explicit compatibility fallback. RED provenance is `d1e1b8fb972595a507637d18817eabb2981ab1fa`; evaluator/replay GREEN checkpoint is `5cf72a54a62c87763279c02014485a847724b72e` with CI #3631 / R2 #3351 GREEN. |
| Beginner production wiring | **PASS / CANDIDATE** | `0ef3760ee233b0e20fa0dd272b12380abe8ee4a4` routes Beginner Drunk setup through the Q04 production adapter while retaining the compatibility route as an explicit pre-DLB-6 fallback. |
| Production cutover acceptance | **T4 PENDING** | Final authority still requires an explicit `[full-ci]` exact-head checkpoint plus independent R2 before the gate may be marked PASS. |

Overall:

```text
architecture / snapshot / interoperability  PASS
legality / topology / replay              PASS
current playable fallback                 PASS
bounded ordering evidence                 PASS
versioned production policy               PASS / IMPLEMENTED + REPLAYED
Beginner production wiring                 PASS / CUTOVER CANDIDATE
production cutover                         T4 PENDING
```

## 7. Immediate route

```text
C3-Q04 VERIFIED / ACCEPTED
-> DRUNK_ASSIGNMENT_Q04_V1 typed request + bounded evaluator COMPLETE
-> replay coverage + replay acceptance COMPLETE
-> Beginner route wiring COMPLETE / CUTOVER CANDIDATE
-> explicit [full-ci] T4 exact-head + independent R2 NEXT
-> rerun/close production cutover gate after T4 evidence
-> Beginner automatic authority only if PASS
-> DLB-6 retirement later
```

C5 / `BEGINNER_CONSERVATIVE_V2` remains a separate evidence gate and is unchanged by this re-entry.
