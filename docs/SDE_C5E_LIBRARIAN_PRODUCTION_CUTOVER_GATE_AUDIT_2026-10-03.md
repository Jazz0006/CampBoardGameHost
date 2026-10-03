# C5-E — Functioning Librarian Production Cutover Gate Audit — 2026-10-03

> Status: **AUDIT COMPLETE / BOUNDED IMPLEMENTATION AUTHORIZED / PRODUCTION AUTHORITY STILL PENDING ACCEPTANCE**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> Baseline: `main@92ab550187298734792f8d32a11f35fedaa97591`
>
> C5-D accepted replay checkpoint: `5241518e002e65993eed594632604e13e2979bfd`, CI #3693 / R2 #3401 GREEN
>
> Scope: only Trouble Brewing, First Night, functioning/reliable **Librarian**, automatic Storyteller mode.

## 1. Decision

C5-E may proceed, but the production cutover must not call the full C5-B exact/replay bridge from Compose/UI.

The accepted architecture is:

```text
snapshot-backed first-night pair context
        |
PairInformationLegalDomain (sole legality owner)
        |
functioning Librarian truth-only production fast path
        |
PairInformationFutureFlexibilityProjector
        |
bounded V2/V1-equivalent seeded selection
        |
canonical manual legal option
        |
existing FirstNightInformationMigration / display commit
```

The full C5-B/C5-D exact-world replay remains the **acceptance oracle**, not a runtime dependency.

Production authority does not change until the implementation tests and final exact-head T4 pass.

## 2. Current production surface

The current automatic pair-information path is:

```text
ClocktowerHostScreen
  legalPairInformationOptions(...)
        |
ClocktowerPairManualAuthority.projectLegalOptions(...)
        |
ClocktowerInformationStepBuilder
  manualInformationCandidates = complete legal domain
  automaticInformationCandidates = same legal domain
        |
ClocktowerNightStepUi
  recommendationCoordinator.selectInformation(...)
        |
DynamicCandidateGenerator.select(...)
        |
automaticDisplayOption
        |
ClocktowerPairInformationSquareTableDialog
        |
showRecommendedDisplayOption(...)
        |
FirstNightInformationMigration.resolvePublication(...)
```

This means current Beginner/automatic pair selection still uses the legacy generic dynamic selector even though the legal/manual domain has already migrated to the canonical pair domain.

The assisted/Experienced presentation additionally exposes legacy pair-ranking recommendations from `RegistrationPolicy.recommendPair(...)`. C5-E does **not** replace that assisted surface.

## 3. Exact cutover scope

C5-E is allowed to affect only when all of the following are true:

- script = Trouble Brewing;
- phase = First Night;
- round = 1;
- perceived ability = Librarian;
- ability reliability = `RELIABLE`;
- automatic Storyteller mode is enabled;
- snapshot-backed `TroubleBrewingFirstNightPairDecisionContext` is current;
- the selected candidate belongs to the complete current `PairInformationLegalDomain`.

Explicitly outside C5-E:

- Washerwoman;
- Investigator;
- poisoned Librarian;
- Drunk shown as Librarian;
- later nights;
- non-TB scripts;
- Experienced/manual pair editing;
- manual legal-domain ownership;
- evidence/model/ML export;
- numeric calibration or weights.

## 4. Legality and truth invariant

`PairInformationLegalDomain.generate(..., reliability = RELIABLE)` is authoritative and explicitly filters out every false outcome.

Therefore the C5-E candidate set is truth-only:

```text
RELIABLE pair legal domain
=> every candidate SemanticTruth.TRUE
```

This invariant is required for the bounded production fast path.

C5-E must not generalize this optimization to impaired information, where false-but-well-formed candidates are legal.

## 5. V1 safety relationship

Frozen `BEGINNER_CONSERVATIVE_V1` owns only the structural contradiction gate:

```text
evilTopologyRetention == 0
=> reject as no-credible-evil-world
```

C5-E must not fabricate `StrategicDecisionFeatures` in production merely to call V1/V2.

Instead the production fast path is permitted only on the truth-only functioning-Librarian surface, and must be cross-checked against the accepted exact C5-D replay oracle.

Required proof before cutover:

1. reliable Librarian legal candidates are all semantically true;
2. on the G10 exact 40-candidate oracle, every production-fast-path candidate considered viable by the fast path has the same V1/V2 disposition required by C5-D;
3. the production selected candidate equals the exact V2 selection for the same candidate domain, decision identity and seed;
4. V1-fallback selection is byte-for-byte/stable-ID equivalent to frozen V1 seeded selection when the future-flexibility predicate does not create a strict preferred subset.

If any of those tests fail, C5-E must remain on the current production selector.

## 6. Production selector contract

Add a narrow recommendation-layer owner, provisional conceptual name:

`FunctioningLibrarianV2ProductionSelector`

Inputs:

- `TroubleBrewingFirstNightPairDecisionContext`;
- Librarian source seat;
- stable decision ID;
- selection seed.

It owns no legality.

Algorithm:

```text
legal = PairInformationLegalDomain.generate(RELIABLE Librarian)

require:
  non-empty
  all SemanticTruth.TRUE

future = PairInformationFutureFlexibilityProjector.project(legal)

preferred = candidates carrying
  preserves-competing-recurring-information-routes

if preferred is a strict non-empty subset:
    choose deterministically with V2 seeded-hash identity
    mode = V2_PREFERRED_BAND
else:
    choose deterministically from complete legal domain
    using the exact frozen V1 seeded-hash identity
    mode = V1_FALLBACK
```

Do not duplicate the SHA-256 seeded-hash implementation. Extract/reuse one stable helper so frozen V1, V2 and the production fast path share exact selection semantics without changing their policy identities.

Output should be policy-neutral enough for the presentation layer to map by stable candidate ID/outcome, e.g.:

- selected candidate ID;
- selection mode;
- evidence-backed reason codes.

No `ClocktowerDisplayOption` construction belongs inside the selector.

## 7. UI / presentation seam

The complete manual domain remains:

`ClocktowerPairManualAuthority.projectLegalOptions(...)`

C5-E should add one optional automatic policy recommendation to the night-step presentation model rather than narrowing `automaticInformationCandidates`.

The automatic path becomes:

```text
complete automatic legal domain
        +
optional C5-E policy recommendation
        |
if policy recommendation is current + maps into manual legal domain:
    use it as automaticDisplayOption
else:
    retain existing recommendationCoordinator.selectInformation(...) fallback
```

This preserves:

- complete manual legality;
- current publication semantics;
- current result display;
- current stale/failure fallback;
- all non-C5-E pair surfaces.

The policy recommendation must be canonicalized back through `ClocktowerPairManualAuthority` before display/confirmation.

## 8. Manual authority

Manual legality must not change.

Experienced mode continues to expose the complete authoritative pair domain.

The C5-E automatic recommendation may not:

- remove legal manual options;
- bypass `ClocktowerPairManualAuthority`;
- publish a free-form result;
- change registration facts;
- change the display commit boundary.

## 9. Recommendation-to-publication seam

No new publication path is authorized.

The selected C5-E recommendation must flow through the current chain:

```text
canonical legal option
-> pair square-table presentation
-> confirmation
-> FirstNightInformationMigration
-> display()
-> typed AbilityObservation
```

`FirstNightInformationMigration.resolvePublication(...)` remains the publication/observation boundary.

## 10. Stale / failure behavior

C5-E is fail-safe and non-blocking.

Fallback to the existing production selector when:

- pair decision context is absent;
- context revision is stale;
- actor/source seat cannot be resolved;
- scope is not exactly functioning Librarian;
- candidate mapping back to the current manual legal domain fails;
- future-flexibility projection is unavailable for the complete domain;
- the new production selector throws or returns no candidate.

A C5-E failure must never make the Librarian step unavailable.

No stale recommendation may be reused across a changed snapshot/revision.

## 11. Legacy ranking coexistence

After C5-E cutover:

- Beginner/automatic functioning Librarian: V2 fast path first, current generic selector fallback;
- Experienced/assisted Librarian: existing recommendation presentation remains unchanged;
- unreliable Librarian: existing unreliable selector remains unchanged;
- Washerwoman/Investigator: unchanged;
- manual legal domain: unchanged.

The legacy generic selector is therefore **not retired globally** by C5-E.

## 12. Acceptance tests

Minimum executable acceptance:

1. scope rejects non-TB, non-first-night, non-Librarian and unreliable inputs;
2. reliable Librarian domain is truth-only;
3. G10 40-candidate production fast-path candidate equals C5-D exact V2 candidate for the same decision ID/seed;
4. preferred-band reason remains generic and never role-names Undertaker;
5. when no strict preferred subset exists, production selection equals frozen V1 seeded selection;
6. complete manual legal domain is unchanged before/after cutover;
7. automatic C5-E candidate canonicalizes into that manual domain;
8. missing/stale/failed C5-E result falls back to the existing automatic selector;
9. Experienced/manual editing is unchanged;
10. publication still creates exactly one typed observation through the existing migration/display seam;
11. no exact-world evaluation occurs from Compose/UI during production selection;
12. final exact-head T4: Android FULL/assemble + ASP + Real Clingo + R2 GREEN.

## 13. Gate verdict

```text
C5-A projector                 COMPLETE / ACCEPTED
C5-B pair shadow/replay        COMPLETE / ACCEPTED
C5-C V2                        COMPLETE / ACCEPTED
C5-D canonical V1/V2 replay    COMPLETE / ACCEPTED
C5-E audit                     COMPLETE
C5-E implementation            AUTHORIZED / BOUNDED
C5-E production authority      STILL PENDING IMPLEMENTATION + T4 ACCEPTANCE
```

## 14. Immediate next task

Implement tests-first:

`C5-E1 — functioning-Librarian V2 production selector contract`

Then:

`C5-E2 — bounded automatic night-step cutover + legacy fallback`

Then:

`C5-E3 — exact-head production acceptance / cutover closure`

HOST-ML1 remains a separate eligible lane and must not be bundled into this PR.
