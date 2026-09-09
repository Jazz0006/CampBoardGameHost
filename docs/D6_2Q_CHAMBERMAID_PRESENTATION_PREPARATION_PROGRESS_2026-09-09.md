# D6.2q — Chambermaid presentation preparation progress

> Date: 2026-09-09 Australia/Sydney
> Status: COMPLETE / VALIDATED
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Production/test checkpoint: `a5f1654fe9adf56ce7fc3cbc07cd039972715a55`.

## Result

Chambermaid selected-player presentation is now prepared once beside `chambermaidResolution` and consumed by both first-night and other-night materializers.

`ClocktowerChambermaidSelectionPresentation` owns only immutable presentation facts:

- ordered selected target names;
- ordered current subject seats;
- the optional three-space seat-label text;
- construction and validation of the typed `PLAYERS_WAKING_FOR_ABILITY` numeric proposition from an explicit source seat and value.

The existing `clocktowerChambermaidDisplayProposition` function remains as a compatibility wrapper over the new seam. Selection eligibility and woke-count truth remain owned by `resolveChambermaidSelection`; Host retains recommendation invocation, localized content, action/result wiring and materializer registration.

## Exact scope

The production/test checkpoint changes exactly three files:

```text
ClocktowerChambermaidPresentationSemantics.kt      +46 / -14
ClocktowerHostScreen.kt                            +14 / -58
ClocktowerChambermaidPresentationSemanticsTest.kt  +53 /  -0
total                                             +113 / -72
```

Production-only net change is +60 / -72. `ClocktowerHostScreen.kt` is now 4,476 lines / 263,777 bytes, down 44 lines in this slice. The typed presentation owner is 61 lines / 2,273 bytes.

Both Chambermaid registry entries remain present and in their original order. Their display proposition, secondary text and recommended-value proposition factories now consume the single prepared value; localized text, navigation/action, result, recommendation call, selection state and publication lifecycle are unchanged.

## Typed coverage

`ClocktowerChambermaidPresentationSemanticsTest` covers:

- complete selection projection with ordered names and seat indices;
- partial and empty selection secondary text;
- typed proposition construction;
- invalid source seat and invalid displayed value;
- duplicate subject-seat rejection;
- the pre-existing compatibility proposition function.

Static scope checks also confirmed four Host proposition uses, four secondary-text uses, zero direct compatibility-helper calls in Host and exactly two Chambermaid materializer identities.

## Validation

```text
git diff --check — PASS
R2 34297489458 — PASS
CI 34297489467 — PASS
  Android production Kotlin compile — PASS
  Android test Kotlin compile — PASS
  :app:testFast — PASS
  Real Clingo cross-validation — PASS
  CI gate — PASS
```

The Android job completed with `BUILD SUCCESSFUL in 1m 40s`. FULL was correctly not selected for this bounded pure-preparation refactor; the immediately preceding D6.2l checkpoint remains the latest full JVM/debug APK + ASP gate. Local Android GREEN is not claimed because the local environment cannot bootstrap the Gradle distribution/SDK.

## Architecture outcome and next boundary

This slice removes the duplicated Chambermaid seat/proposition calculation without introducing a role-step mega-bag, callbacks, Compose state or a second selection authority. A Chambermaid presentation change can now be tested against the typed owner without opening the full Host composition.

The D6.2r read-only audit completed at the following docs checkpoint and scoped a narrow D6.2s materializer extraction. The assembler may receive prepared immutable content/proposition and one lazy Chambermaid option provider; recommendation invocation and lifecycle remain in Host. See `docs/D6_2R_CHAMBERMAID_MATERIALIZER_EXTRACTION_AUDIT_2026-09-09.md`.
