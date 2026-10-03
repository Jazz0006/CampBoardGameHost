# SDE C5 G10 Pair Future-Flexibility Re-entry Audit — 2026-10-03

> Status: **COMPLETE / C5 RE-ENTRY PASS / SDE-3D3 IMPLEMENTATION AUTHORIZED / PRODUCTION CUTOVER NOT YET AUTHORIZED**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> Host audit baseline: `main@86116c7f6bf45bf32eddef9ac039c3cb0236af74`
>
> EvidenceLab accepted baseline: `78f672868ea6603317aeefa20ad91686c5886db9`
>
> Evidence handoff: `ClocktowerEvidenceLab/docs/G10_LIBRARIAN_PAIR_E3_REENTRY_CANDIDATE_AUDIT_2026-10-03.md`
>
> Scope: re-evaluate the preserved C5 / SDE-3D3 evidence gate after the newly admitted G10 Librarian pair-information decision and define the smallest evidence-authorized Host implementation route.

## 1. Decision

C5 may formally re-enter implementation.

The new G10 `16:52` Librarian decision satisfies the exact Host re-entry formula preserved in `SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`:

```text
qualified source / Storyteller
+ decision-time committed state
+ observed choice
+ production-recoverable legal alternatives
+ explicit rationale
+ generic typed feature mapping
= E3 PASS
```

The admitted generic semantic is:

> For a functioning truthful pair-information decision around an actual Outsider, a legal decoy may receive a bounded weak preference when it preserves a meaningful competing recurring-information narrative into future rounds rather than creating a low-consequence ambiguity.

This is sufficient for **SDE-3D3 / the first real evidence-authorized policy delta**.

It is not sufficient for automatic production cutover yet. The Host still needs a stable projector, pair-information shadow/replay composition, an immutable V2 definition, same-history replay acceptance, and an explicit surface-scoped cutover gate.

E4 remains not met and is not required for this first qualitative predicate because the proposed delta uses no numeric threshold, scalar score, weight, or calibrated tradeoff magnitude.

## 2. Evidence accepted for re-entry

EvidenceLab records one human-primary-reviewed historical choice from The Megavoid Game 2:

- nine-seat committed shown-role state is reconstructable;
- seat 1 is the actual Drunk shown Empath;
- seat 4 is a functioning Librarian;
- the Librarian is shown the Drunk-Empath at seat 1 together with the Undertaker at seat 3;
- the Storyteller explicitly explains the Undertaker choice through the fact that Empath and Undertaker both generate recurring information, so uncertainty over which is Drunk changes how later information streams are trusted;
- the observed outcome is legal in the current Host production domain;
- the current Host domain reconstructs **40** functioning-Librarian truthful player-visible outcomes for that exact state;
- the rationale maps to the already-declared `futureFlexibility` / confirmation-chain / information-utility feature family.

The source is admitted as `EXPERIENCED / INDEPENDENT`, not promoted to an official/TPI or stronger trust category.

The evidence does **not** authorize:

- an Undertaker-specific bonus;
- a global Undertaker > other decoy ordering;
- a global recurring-information-role ordering;
- a rule that maximum ambiguity is always desirable;
- a numeric “future flexibility” score;
- a player-count threshold;
- a multi-axis scalar;
- a change to the earlier Drunk-assignment decision;
- a generalization from Librarian to every pair-information family without an explicit semantic gate.

## 3. Host architecture readiness

### 3.1 Already ready

The current Host already has the required architectural pieces below.

#### Typed feature surface

`DecisionFeatures` already owns:

```kotlin
FutureFlexibilityFeatures(
    retainedRouteIds,
    lostRouteIds,
    reasonCodes,
)
```

and exposes it through `DecisionFeatures.futureFlexibility`.

The field is currently explicit `NOT_PROJECTED_YET`; therefore no schema redesign is required.

#### Rules-owned pair legality

`PairInformationLegalDomain` is the complete selectable semantic authority for Washerwoman / Librarian / Investigator pair information.

It already separates:

- legality and display semantics;
- natural truth;
- Spy/Recluse registration witnesses;
- functioning truth-only domains;
- Drunk/Poisoned false-but-well-formed domains.

Recommendation policy remains downstream and must not narrow the manual legal domain.

#### Pair exact-consequence seam

`PairInformationExactConsequenceAdapter` already converts an already-legal truthful pair candidate into exact SDE consequence input while preserving registration witness identity.

#### Pair-specific descriptive projector precedent

`PairInformationRegistrationAmbiguityExposureProjector` already demonstrates the correct ownership pattern:

```text
rules-owned legal candidate
-> pair-specific descriptive projector
-> generic typed feature
-> no role-name policy / no score / no selection authority
```

Its tests already require symmetry across pair-information abilities instead of named-role policy branches.

#### Canonical TB runtime context

TBGS-2A / TBGS-2B already provide snapshot-backed first-night pair context and manual/publication legality through `TroubleBrewingGameSnapshotV1`.

No snapshot extension is required for the G10 state.

### 3.2 Missing pieces

Four bounded gaps remain.

#### Gap 1 — no canonical future-flexibility projector

`FutureFlexibilityFeatures` exists only as a typed/persisted surface. Searches of production code find no projector populating `retainedRouteIds` / `lostRouteIds`.

Therefore C5 must not treat the field as neutral or derive a preference from UI metadata.

#### Gap 2 — pair information is not on the existing structured production-shadow transport

`StructuredInformationShadowAdapter` is currently generic only over `DynamicInformationOutcome`, whose production variants are Number and Category.

`PairInformationOutcome` is a separate typed outcome and does not enter that adapter.

As a result, the existing `MultiPolicyReplayInput.fromStructuredShadow` / SDE-3C5 path is not directly available for pair information even though pair legality and exact-consequence primitives already exist.

This is an orchestration gap, not a reason to merge pair information into an unrelated dynamic-outcome type.

#### Gap 3 — no V2 policy exists

Production `DecisionPolicyReplayRegistry` currently contains only `BEGINNER_CONSERVATIVE_V1`.

There is no `BEGINNER_CONSERVATIVE_V2` implementation or definition.

#### Gap 4 — production recommendation still uses the pre-C5 ranking path

Current pair recommendation and presentation code uses its established dynamic/style/default ranking path while `ClocktowerPairManualAuthority` preserves the complete legal manual domain.

C5 must first prove shadow/replay behavior. It must not silently replace this production selection path during projector work.

## 4. Generic recurring-information route semantics

The G10 evidence speaks about **future recurring information routes**, not merely “wakes on other nights.”

The ruleset already records `otherNightOrder`, but this is insufficient as the policy feature by itself: roles such as Monk, Poisoner, Imp, Mayor and Butler may appear in the other-night flow without representing a recurring information stream received by that player.

Likewise, `ClocktowerInformationStepBuilder` contains UI-oriented role-name-to-display-kind mappings. Those mappings are presentation mechanics and must not become recommendation policy authority.

The smallest safe design is a rules/recommendation-owned typed capability describing whether a perceived role exposes a recurring **player-information route**.

Requirements:

1. the capability is factual/descriptive, not a preference weight;
2. policy code consumes only the typed capability and never branches on `Undertaker` or `Empath` names;
3. Trouble Brewing may begin with a bounded rules-owned classifier while the architecture remains extensible to later scripts;
4. tests must prove semantic symmetry for at least one non-G10 equivalent case;
5. other-night wake order may support the classifier, but cannot alone define it;
6. UI display-kind metadata is not an authoritative input.

## 5. Smallest evidence-authorized V2 semantic

The first V2 delta should be a **weak preference / partial ordering**, not a hard rejection.

Proposed typed reason:

```text
preserves-competing-recurring-information-routes
```

Bounded evaluation shape:

```text
start from candidates that survive the frozen V1 structural safety gate

IF
  the decision belongs to the admitted functioning truthful
  actual-Outsider pair-information surface,
  AND future-flexibility is projected,
  AND one or more survivors preserve a meaningful competing
      recurring-information narrative,
THEN
  keep those candidates in the final preferred survivor band,
  leave other otherwise-legal V1 survivors ACCEPTED rather than REJECTED,
  and use SEEDED_HASH_V1 only inside the preferred survivor band
ELSE
  preserve exact V1 survivor equivalence / selection behavior
```

Properties:

- no candidate is made illegal;
- no candidate is hard-rejected solely by this E3 predicate;
- no scalar comparison exists;
- all unresolved dimensions remain tied;
- multiple future-flexibility-equivalent candidates remain tied;
- feature unavailable / not applicable means V1 fallback, never neutral-zero inference;
- the observed Undertaker candidate is not encoded as a named positive label.

## 6. Evidence scope for the first policy slice

The first admitted production-policy surface should be narrower than the projector.

Projector scope may be generic over truthful pair candidates.

Policy-consumption scope for the first V2 slice:

```text
Trouble Brewing
+ first night
+ functioning / reliable pair-information ability
+ truthful legal outcomes
+ actual Outsider pair-information semantic
+ future-flexibility feature projected
```

This intentionally matches the G10 mechanism conservatively.

Do not automatically apply the new weak preference to:

- impaired/false pair information;
- Investigator Minion pairs;
- Washerwoman Townsfolk pairs;
- registration-exposure decisions whose rationale belongs to a different E3 predicate;
- later scripts or Travellers.

Those surfaces may reuse the projector later after their own evidence/policy gate.

## 7. Pair-information shadow / replay architecture

Do **not** broaden `DynamicInformationOutcome` merely to make pair information fit `StructuredInformationShadowAdapter`.

The lower-risk route is a bounded pair-information SDE bridge that composes existing owners:

```text
TroubleBrewingFirstNightPairDecisionContext / canonical snapshot
-> PairInformationLegalDomain
-> proposition materialization
-> PairInformationExactConsequenceAdapter
-> StorytellerDecisionEngine exact consequence evaluation
-> DecisionFeatures
-> pair future-flexibility projector
-> V1 / V2 shadow policy replay
-> DecisionTrace / actual-choice correlation
```

The bridge must preserve:

- exact legal-candidate order;
- registration witnesses;
- canonical history prefix;
- source revision/freshness identity;
- manual choice as authoritative gameplay truth;
- diagnostic/replay no-write/no-blocking behavior.

After this route is stable, a later refactor may determine whether pair and number/category shadow transports share a more generic interface. That refactor is not a prerequisite for C5.

## 8. Evidence provenance in Host

EvidenceLab remains the canonical external evidence store.

Do not introduce a second mutable Host evidence catalog merely to mirror `st-the-megavoid`.

Host provenance for V2 should use:

1. the new audit document;
2. a stable test/replay evidence fixture carrying source key `st-the-megavoid` and G10 decision identity;
3. immutable `StorytellerPolicyDefinition.evidenceCheckpoint` bound to EvidenceLab checkpoint `78f672868ea6603317aeefa20ad91686c5886db9`.

Recommended checkpoint identity:

```text
clocktower-evidence-lab-g10-librarian-future-flexibility-78f672868ea6603317aeefa20ad91686c5886db9
```

This is provenance, not runtime game state.

## 9. Ordered implementation route

### C5-A — pair future-flexibility feature projector

Goal:

- define the typed recurring-player-information-route capability;
- implement the pair future-flexibility projector;
- project only descriptive route IDs/reason codes;
- keep V1 behavior byte/semantic-equivalent.

Required RED/GREEN:

- G10 observed candidate projects the intended future-flexibility reason;
- low-consequence decoy contrast does not manufacture the same route evidence;
- at least one non-G10 semantically equivalent case proves role-name neutrality;
- unavailable/not-applicable remains explicit;
- registration witness and legal candidate identity are unchanged.

Exit:

`FutureFlexibilityFeatures` is stable and replay-persistable for the admitted pair surface; **no V2 selection yet**.

### C5-B — pair SDE shadow + replay bridge

Goal:

- compose the complete current pair legal domain into SDE exact consequences/features;
- expose a pair replay input with canonical prefix and source identity;
- persist/replay diagnostic traces without changing visible recommendation or manual selection.

Required checks:

- exact G10 legal domain contains 40 truthful outcomes;
- observed `Drunk + {1,3}` remains legal;
- legal order is preserved end-to-end;
- no candidate is added/removed by SDE;
- current visible recommendation/manual authority is unchanged;
- replay never mutates historical truth.

Exit:

G10 and synthetic semantic-regression cases can run through V1-compatible pair shadow/replay.

### C5-C — `BEGINNER_CONSERVATIVE_V2` weak preference

Goal:

- create a real immutable V2 definition;
- bind its evidence checkpoint;
- inherit V1 hard safety semantics unchanged;
- add only the evidence-authorized future-flexibility soft preference;
- use the existing seeded hash method inside the final survivor band.

Required behavior:

- outside admitted surface -> exact V1 fallback;
- feature unavailable -> exact V1 fallback;
- no qualifying preferred subset -> exact V1 fallback;
- one/multiple qualifying preferred candidates -> prefer that subset without hard rejection;
- unrelated feature dimensions do not change ordering;
- V1 definition and outputs remain immutable.

Exit:

V2 is **shadow/replay capable**, not yet production-authoritative.

### C5-D — canonical V1/V2 replay and cutover recheck

Run the same committed histories through V1 and V2.

Minimum acceptance:

1. G10 full 40-candidate domain replays deterministically;
2. the generic reason is visible and not Undertaker-named;
3. actual expert choice is preserved as historical metadata, not used as a direct policy label;
4. V2 differences occur only on the admitted predicate;
5. V1 remains unchanged;
6. structural/safety invariants remain unchanged;
7. no hindsight or future observation enters the decision-time prefix;
8. no numeric threshold/weight is introduced;
9. same input/version/seed produces the same output;
10. feature/provenance survives trace archive round-trip.

Only after C5-D passes may a separate C5-E / SDE-3D5 production cutover gate decide whether the functioning Librarian automatic recommendation surface should consume V2.

### C5-E — surface-scoped production cutover

Not authorized by this audit alone.

The cutover audit must name:

- exact automatic surface;
- V1 fallback behavior;
- manual override behavior;
- recommendation-to-publication seam;
- failure/staleness behavior;
- legacy ranking retirement or coexistence;
- acceptance test tier and exact-head CI/R2 evidence.

Manual legal-domain authority must remain available even after automatic cutover.

## 10. Relationship to TBGS-2

TBGS-2D Demon-succession migration remains implementation-ready and is not invalidated.

C5 is a separate policy-evolution lane. The new G10 evidence means it is no longer evidence-blocked, so C5-A may proceed now without waiting for additional EvidenceLab acquisition or TBGS-2D.

Do not bundle TBGS-2D Demon succession with C5 pair policy work in one implementation PR.

## 11. Cutover gate status after re-entry

| Gate | Result | Reason |
| --- | --- | --- |
| C5 E3 re-entry evidence | **PASS** | G10 16:52 meets the preserved Host formula. |
| E4 numeric calibration | **NOT MET / NOT REQUIRED FOR C5-A–C** | First delta is qualitative and weight-free. |
| Pair legal domain | **PASS** | `PairInformationLegalDomain` owns the full functioning domain. |
| Snapshot/runtime state | **PASS** | TBGS-2A/B provide sufficient first-night state; no snapshot extension required. |
| Typed future-flexibility contract | **PASS / SURFACE EXISTS** | `FutureFlexibilityFeatures` already exists. |
| Future-flexibility production projector | **GAP / C5-A** | No production projector exists yet. |
| Pair exact-consequence primitives | **PASS** | Existing pair exact adapter and projector precedents. |
| Pair production shadow/replay composition | **GAP / C5-B** | Existing structured shadow supports Number/Category, not pair outcome. |
| Real V2 policy definition | **GAP / C5-C** | No `BEGINNER_CONSERVATIVE_V2` exists. |
| Same-history V1/V2 replay | **GAP / C5-D** | Must run after V2 exists. |
| Production automatic cutover | **BLOCKED / C5-E** | Requires completed projector/replay/V2 acceptance and explicit surface gate. |

Overall:

```text
C5 evidence re-entry                         PASS
SDE-3D3 implementation                      AUTHORIZED
C5-A projector                              READY
C5-B pair shadow/replay                     PLANNED AFTER C5-A
C5-C BEGINNER_CONSERVATIVE_V2              PLANNED AFTER C5-B
C5-D V1/V2 canonical replay                PLANNED AFTER C5-C
C5-E production cutover                    BLOCKED UNTIL C5-D ACCEPTANCE
E4 numeric calibration                     STILL UNPROVEN / NOT NEEDED FOR FIRST DELTA
```

## 12. Immediate next task

Begin **C5-A — pair future-flexibility feature projector** with tests first.

Do not wait for more EvidenceLab evidence for this predicate.

EvidenceLab should continue high-value acquisition in parallel, especially evidence that could later authorize:

- impaired-information believability;
- role-function exposure preference;
- Demon-bluff set comparison;
- multi-axis strength/tradeoff calibration.

Those future lanes must not broaden the bounded G10 predicate during C5-A–D.
