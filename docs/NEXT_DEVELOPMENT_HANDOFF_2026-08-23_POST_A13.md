# CampBoardGameHost — Post-A13 / PR #43 Final Review Handoff

> Date: 2026-08-23  
> Scope: PR #43 `codex/source-decomposition-clocktower-host`  
> Role: immediate continuation handoff after A13  
> Do not treat SHA values below as permanent; always re-query GitHub first.

## 1. Current validated implementation baseline

```text
main:                    efd63b360ca9aba8c7890594449aa5e21817f560
PR #43 implementation:  b37f0067b674a0cd4bee5ff311840d1c52ce8c05
A13 CI:                  #534 SUCCESS
A13 R2:                  #473 SUCCESS
ASP / Real Clingo:       SUCCESS / SUCCESS
```

Documentation commits may appear after the implementation baseline. Audit them separately; do not mistake a docs-only head for a new code implementation.

PR #43 must remain Draft / Open / Not merged until the user explicitly authorizes otherwise.

## 2. A13 completed architecture

Other Night was cut over from the transitional eager path:

```text
eager supported step construction
-> filteredNightSteps
-> ClocktowerProductionOtherNightFlow.order(...)
```

into:

```text
ClocktowerProductionOtherNightFlow.interactions(...)
-> ClocktowerNightStepMaterializerRegistry(OTHER_NIGHT)
-> lazy materialize projected interactions
```

First Night remains on the A12 planner-first path.

Other Night role materializer identities:

```text
Poisoner
Butler
Empath
Chambermaid
Fortune Teller
Undertaker
Monk
Imp
Sage
Ravenkeeper
Spy
```

Event identities:

```text
new Demon identity
demon succession
Mayor redirect
```

Resolved facts remain Host-derived rule outcomes; planner/projector owns interaction existence/order. Materializer closures only translate projected stable identities to current production `ClocktowerNightStepUi` representations.

## 3. A13 evidence chain

```text
A13 RED:
4e638c345ed50a3bc65abdc22ac5487172bf9f32

Missed obsolete New-Demon source-contract migration:
bae29d5fccb988f641a95e743f899be56ae84299

A13 GREEN:
b37f0067b674a0cd4bee5ff311840d1c52ce8c05
```

The contract migration only replaced obsolete assertions for production `.order()` / `identityOf` with the new canonical `interactions()` + OTHER_NIGHT registry + lazy materialization contract.

A13 GREEN production commit changed `ClocktowerHostScreen.kt` only.

## 4. Protected transaction boundary

Do not move or reorder `advanceNightStep` during final cleanup or review.

Current conceptual order:

```text
confirm poison
-> confirm monk protection
-> confirm demon attack
-> Mayor redirect automatic audit + confirm
-> Demon successor automatic audit
-> Spy registration record
-> Recluse registration record
-> semantic night-step record
-> step index advance OR onConfirmNight
```

A13 added `ClocktowerAdvanceNightStepTransactionOwnershipTest` to characterize this boundary.

Also preserve:

- Compose `remember` state lifetime;
- `LaunchedEffect` lifetime;
- recommendation coordinator / telemetry lifetime;
- Spy/Recluse registration maps;
- first-night information migration lifecycle;
- player-display / observation commit ordering;
- session/global timeline authority;
- day-action transaction ordering.

## 5. Post-A13 decomposition decision

Measured at A13 implementation head:

```text
ClocktowerHostScreen.kt     295,644 bytes
ClocktowerDayScreen.kt       63,135 bytes
ClocktowerNightStepUi.kt     45,251 bytes
ClocktowerHistoryScreen.kt   38,365 bytes
ClocktowerNightScreen.kt     17,833 bytes
ClocktowerSetupScreen.kt     17,362 bytes
```

The file-size guideline remains soft.

The optional A14 audit concluded **do not implement A14**:

- Overview / Vote / EndConfirm presentation screens already have clean owners in `ClocktowerDayScreen.kt`;
- Host retains mostly state transitions and callback transactions for those paths;
- Nomination/Virgin, Slayer, Artist and Klutz are tightly coupled to registration/recommendation state and commit ordering;
- further extraction would likely introduce a context/parameter bag or move protected transaction/lifetime ownership for little architectural benefit.

Therefore PR #43 decomposition is considered implementation-complete after A13.

Do not start A14 merely to reduce `ClocktowerHostScreen.kt` bytes.

## 6. Immediate next task — final PR #43 review / merge-readiness audit

Before any merge recommendation:

1. Re-query live main, PR #43 state/head, mergeability and latest checks.
2. Separate docs-only commits after `b37f0067...` from implementation changes.
3. Review the complete PR changed-file list for scope drift.
4. Review unresolved PR review threads, submitted reviews and meaningful comments.
5. Audit final diff against protected invariants:
   - rule semantics / precedence;
   - recommendation ordering;
   - registration + impairment ordering;
   - persistence/history/global identity;
   - Compose state/effect lifetime;
   - First Night A12 planner-first authority;
   - Other Night A13 planner-first authority;
   - `advanceNightStep` ordering;
   - day stateful transaction ordering.
6. Confirm no generated/debug/local workspace artifacts were committed.
7. Confirm final docs match implementation.
8. If clean, report `MERGE-READY` to the user with any residual risks.
9. Stop. Do **not** mark ready, merge, rebase, force-push, or begin product A3 without explicit user authorization.

## 7. After PR #43

Only after explicit user authorization and successful PR #43 merge should product development continue to:

```text
A3 historical multi-night exact baseline
using EnumeratedWorldSet
```

Do not mix A3, B4/ZDD production promotion, history UI redesign, misinformation expansion, or broader manual UI rollout into PR #43.

## 8. Working model

```text
ChatGPT / Chat
  = architecture / scope / risk decisions
  = test strategy
  = final remote audit

GitHub connector
  = preferred safe small-file writer

Codex / Luna
  = constrained executor for large/mechanical local edits
```

Luna must not independently reopen A14 or redesign ownership boundaries.
