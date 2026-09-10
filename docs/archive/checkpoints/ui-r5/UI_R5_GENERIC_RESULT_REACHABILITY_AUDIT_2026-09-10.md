# UI-R5 Generic Result Reachability Audit — 2026-09-10

> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117  
> Scope: `ClocktowerNightStepUi.kt` generic recommendation / result-first / unreliable / direct result surfaces after Sage convergence and pair-manual legacy retirement.

## 1. Preconditions

The legacy `ClocktowerPairManualSelectionDialog` and `ClocktowerPairManualCenterControls` were retired at:

```text
9e67fd53b21089a84c897e73ca9abb4c0f4ce17d
```

The retained pair helpers and `ClocktowerPairManualSelectionModel` remain used by the accepted `ClocktowerPairInformationSquareTableDialog` path.

Automated validation for that retirement:

```text
CI #2133 / run 34433654157
- Android FAST unit tests PASS
- full Android + APK skipped / UI-only slice
- ASP skipped
- Real Clingo skipped
- CI gate PASS
R2 #2000 / run 34433654190 PASS
```

## 2. Architecture pre-flight

This audit precedes any production edit to the large `ClocktowerNightStepUi.kt` composition owner.

```text
Architecture pre-flight:
- current owner:
  ClocktowerNightStepUi owns presentation routing among dedicated role/action square-table
  surfaces and the remaining generic information fallbacks. Typed legality/result domains
  remain upstream in step materializers, registration domains, and role-specific projectors.
- proposed responsibility:
  no new responsibility. Retire generic result branches that no longer have a reachable
  production owner, and prevent Chambermaid's dedicated result surface from also falling
  through to generic result controls.
- authoritative state owner(s):
  unchanged. ClocktowerNightStepUi does not become authoritative for rules, registration,
  recommendation generation, reliability, or session state; it only chooses the render owner
  from already prepared step/candidate data.
- narrow stable input/output seam:
  existing ClocktowerNightStepUi + typed specialized projector outputs + current execution mode
  -> exactly one appropriate Storyteller result surface.
- keep in current owner / extract:
  KEEP routing in ClocktowerNightStepUi; do not introduce another routing manager/helper solely
  for this cleanup.
- reason:
  choosing which presentation surface renders is composition ownership already belonging to
  NightStep. The change reduces duplicate/unreachable routing and adds no domain responsibility.
```

## 3. Generic recommendation block — KEEP, narrow ownership

The generic `Recommended information` block is still reachable and must not be deleted wholesale.

In particular, first-night Washerwoman / Librarian / Investigator automatic mode does not enter the manual pair square-table surface, so the generic selected-information reveal remains a valid owner there.

However Chambermaid is different: `ClocktowerChambermaidSquareTableDialog` already owns both target selection and unreliable result choice. The current generic recommendation guard excludes Fortune Teller but not Chambermaid, so automatic impaired Chambermaid can expose a second result surface. This duplicate fallback should be suppressed.

## 4. Generic non-pair result-first branch — RETIRE

`usesResultFirstRegistrationDomain()` requires:

```text
manualInformationCandidates non-empty
AND
Spy or Recluse registration key present
```

Current production `legalSelectionOptions` sources with such result-first domains are limited to:

- Washerwoman;
- Librarian;
- Investigator;
- Chef;
- Empath;
- Fortune Teller;
- Undertaker;
- Ravenkeeper.

The first three are explicitly excluded from `nonPairResultFirstCandidates`; Fortune Teller is explicitly excluded by action. The remaining four all have dedicated typed square-table result projectors:

- Chef consumes typed numeric result-first candidates;
- Empath consumes typed living-neighbour numeric result-first candidates;
- Undertaker consumes typed role-reveal result-first candidates;
- Ravenkeeper consumes typed role-reveal result-first candidates.

For valid production candidates these dedicated projectors own the result surface. Their fail-closed behavior rejects malformed typed data rather than handing malformed data to a generic alternate UI. Therefore `nonPairResultFirstCandidates` and its generic rendering block no longer represent a legitimate production fallback and should be retired.

## 5. Generic unreliable fallback — KEEP, exclude Chambermaid

The generic unreliable fallback is not globally dead because it remains compatibility coverage for information roles that have not replaced every possible generic result path.

But Chambermaid must be excluded because its dedicated square-table dialog already consumes:

```text
manual mode    -> step.displayOptions
automatic mode -> selected automaticDisplayOption only
```

Allowing the generic unreliable block as well creates duplicate Storyteller controls for the same interaction.

## 6. Generic direct reveal — KEEP

The generic direct reveal remains live. It still serves intentionally specialized/private player handoffs such as first-night Minion information, first-night Demon information, and other direct-display steps that do not need a Storyteller spatial result picker.

Fortune Teller and Chambermaid are already excluded from this direct fallback and must remain so.

## 7. Planned minimal production diff

1. remove the local `nonPairResultFirstCandidates` derivation;
2. remove its now-unreachable generic render block;
3. suppress generic `Recommended information` for `ClocktowerNightAction.Chambermaid`;
4. suppress generic unreliable-result fallback for `ClocktowerNightAction.Chambermaid`;
5. retain generic direct reveal and first-night pair automatic behavior;
6. do not change Host/session/rules/recommendation/registration semantics.

A minimal source-wiring ownership test is appropriate here because the contract is composition ownership and there is no narrower typed runtime seam that can prove absence of duplicate render owners without introducing a false abstraction.
