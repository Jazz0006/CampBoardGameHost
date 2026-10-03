# C5-E — Functioning Librarian Production Cutover Gate Audit — 2026-10-03

> Status: **COMPLETE / ACCEPTED**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> Current branch baseline: `main@65f514b770a575c49e8be1c4787de8dd6adb5965`
>
> C5-D accepted replay checkpoint: `5241518e002e65993eed594632604e13e2979bfd`, CI #3693 / R2 #3401 GREEN
>
> C5-E1 selector checkpoint: `f35b4dffae679947589d39fa8ec1c51b40cf1590`, CI #3711 / R2 #3417 GREEN
>
> C5-E2 automatic cutover checkpoint: `29c06f7e3900002cdac968207464e008cdcb1056`, CI #3713 / R2 #3419 GREEN
>
> Final exact-head T4: `6afc3b08416eaf6f3fb74a53bb48b8e144044341`, CI #3717 / R2 #3421 GREEN across Android FULL/assemble, ASP and Real Clingo
>
> Scope: only Trouble Brewing, First Night, functioning/reliable **Librarian**, automatic Storyteller mode.
>
> Parallel ML lane: HOST-ML1A typed neutral export is already COMPLETE / ACCEPTED and remains separate from this product cutover.

## 1. Decision

C5-E is bounded to the functioning Librarian automatic surface. The production cutover does **not** call the full C5-B exact/replay bridge from Compose/UI.

Accepted architecture:

```text
snapshot-backed first-night pair context
        |
PairInformationLegalDomain (sole legality owner)
        |
FunctioningLibrarianV2ProductionSelector
        |
PairInformationFutureFlexibilityProjector
        |
V2 preferred-band / exact V1 seeded fallback
        |
rules candidate -> ClocktowerPairManualAuthority
        |
current automatic legal domain rebind
        |
existing confirmation / FirstNightInformationMigration publication
```

The full C5-B/C5-D exact-world replay remains the **acceptance oracle**, not a runtime dependency.

## 2. Current production surface and C5-E change

Before C5-E, automatic pair information used:

```text
complete pair legal domain
-> ClocktowerInformationStepBuilder.automaticInformationCandidates
-> recommendationCoordinator.selectInformation(...)
-> DynamicCandidateGenerator.select(...)
-> automaticDisplayOption
```

C5-E changes only the functioning Librarian automatic surface:

```text
complete pair legal domain
+ optional policy recommendation
        |
clocktowerAutomaticInformationOption(...)
        |
policy recommendation rebinds into CURRENT automatic domain
        | success                         | stale/missing/failure
        v                                 v
use canonical current option       existing generic selector fallback
```

The automatic candidate domain itself is unchanged.

## 3. Exact cutover scope

C5-E policy authority applies only when all are true:

- script = Trouble Brewing;
- phase = First Night;
- round = 1;
- perceived ability = Librarian;
- ability state = `FUNCTIONING`;
- reliability = `RELIABLE`;
- automatic Storyteller mode is enabled;
- snapshot-backed `TroubleBrewingFirstNightPairDecisionContext` is available;
- selected candidate belongs to the current complete `PairInformationLegalDomain`;
- mapped presentation option is present in the current automatic candidate domain.

Explicitly outside C5-E:

- Washerwoman;
- Investigator;
- poisoned Librarian;
- Drunk shown as Librarian;
- later nights;
- non-TB scripts;
- Experienced/manual pair editing;
- manual legal-domain ownership;
- publication/history ownership;
- ML export/training;
- numeric weights/calibration.

## 4. Legality and truth invariant

`PairInformationLegalDomain.generate(..., reliability = RELIABLE)` filters out false outcomes.

Therefore the admitted production domain is truth-only:

```text
functioning Librarian + RELIABLE pair legal domain
=> every candidate SemanticTruth.TRUE
```

This optimization must not be generalized to impaired information, where false-but-well-formed candidates remain legal.

## 5. V1/V2 safety relationship

Frozen `BEGINNER_CONSERVATIVE_V1` remains immutable.

C5-E does not fabricate strategic features in production. Instead:

- production uses only the accepted qualitative future-flexibility predicate on the narrowly admitted truth-only Librarian surface;
- preferred-band selection hashes with the V2 policy identity;
- when there is no strict preferred subset, fallback hashes with the frozen V1 policy identity;
- the same `PolicySeededHashSelector` implementation is shared by V1, V2 and the production fast path;
- C5-D exact replay remains the oracle proving that the production fast path selects the same candidate as full V2 on G10.

The G10 full-domain acceptance test now explicitly compares the production selector result against the exact C5-D V2 `policySelection`.

## 6. Manual authority remains unchanged

`ClocktowerPairManualAuthority.projectLegalOptions(...)` remains the complete Manual legal domain.

C5-E adds one overload that maps a rules-owned `PairInformationLegalCandidate` back to the canonical current Manual presentation by structured pair key. It does not:

- add or remove legal outcomes;
- reconstruct rules from labels;
- alter Spy/Recluse registration facts;
- bypass manual confirmation;
- narrow Experienced choices.

Experienced/manual mode remains unchanged.

## 7. Stale / failure behavior

The policy recommendation is not trusted as a publication object.

`clocktowerAutomaticInformationOption(...)` recomputes its semantic option identity and accepts it only when an equivalent option exists in the **current** automatic domain.

Otherwise it lazily invokes the pre-C5-E generic selector.

Fallback therefore covers:

- no pair context;
- non-functioning Librarian;
- selector returns null;
- candidate cannot be found in current legal domain;
- candidate cannot map into current Manual presentation;
- policy option is stale relative to the current automatic domain.

A C5-E failure must never make the Librarian step unavailable.

## 8. Recommendation-to-publication seam

No new publication path exists.

The selected option still flows through:

```text
current canonical legal option
-> PairInformation square-table presentation
-> confirmation
-> FirstNightInformationMigration.resolvePublication(...)
-> display(...)
-> typed AbilityObservation
```

The existing migration/display boundary remains authoritative.

## 9. Legacy coexistence after cutover

- Beginner/automatic functioning Librarian: V2 fast path first, generic selector fallback;
- Experienced/assisted Librarian: existing recommendation presentation unchanged;
- unreliable Librarian: existing unreliable selector unchanged;
- Washerwoman/Investigator: unchanged;
- manual legal domain: unchanged;
- exact replay: acceptance/debug oracle, not UI runtime work.

No global legacy-selector retirement occurs in C5-E.

## 10. Tests-first evidence

C5-E1:

- historical RED checkpoint on superseded PR #207: `3f4bbf537d0f5f183d3ea46035c444cf9f8cf10f`, CI #3697 failed because the production selector did not exist;
- rebased GREEN checkpoint: `f35b4dffae679947589d39fa8ec1c51b40cf1590`, CI #3711 / R2 #3417 GREEN.

C5-E2:

- RED checkpoint: `c2c9e941ecb046ee4a9add1d8bd4b51f013eafa7`, CI #3712 failed only because the candidate-to-manual mapping overload and current-domain policy-option rebind helper did not exist; R2 #3418 GREEN;
- implementation checkpoint: `29c06f7e3900002cdac968207464e008cdcb1056`, CI #3713 / R2 #3419 GREEN.

## 11. Final acceptance requirements

Final acceptance evidence:

1. G10 40-candidate exact V2 and production fast path select the same candidate;
2. the V2 reason remains generic and does not encode Undertaker-specific policy;
3. no strict preferred subset reproduces frozen V1 seeded selection identity;
4. non-Librarian and unreliable scopes fail closed;
5. policy recommendation must belong to the current automatic domain or fall back lazily;
6. rules candidate maps only through canonical Manual structured key;
7. complete Manual legal domain remains unchanged;
8. Experienced/manual mode remains unchanged;
9. publication still creates typed observations through the existing migration/display seam;
10. production selection performs no exact-world evaluation from Compose/UI;
11. final exact-head T4 passes Android FULL/assemble + ASP + Real Clingo + R2.

## 12. Gate state

```text
C5-A projector                 COMPLETE / ACCEPTED
C5-B pair shadow/replay        COMPLETE / ACCEPTED
C5-C V2                        COMPLETE / ACCEPTED
C5-D canonical V1/V2 replay    COMPLETE / ACCEPTED
C5-E gate audit                COMPLETE
C5-E1 production selector      COMPLETE / GREEN
C5-E2 automatic cutover        COMPLETE / GREEN
C5-E3 final T4                 COMPLETE / GREEN
production authority           ACCEPTED — FUNCTIONING LIBRARIAN AUTOMATIC SURFACE
```

## 13. Closure

C5-E is **COMPLETE / ACCEPTED**. The functioning Librarian automatic surface now consumes the bounded V2 production selector, with canonical legal-domain rebind and legacy fallback. Experienced/manual editing, publication authority, impaired-information behavior and all other pair surfaces remain unchanged.

After PR #210 merges and live `main` is synchronized, re-evaluate the next product lane independently of HOST-ML1B. TBGS-2D Demon succession remains implementation-ready; HOST-ML1B remains deferred until a concrete offline consumer requires machine-readable materialization.
