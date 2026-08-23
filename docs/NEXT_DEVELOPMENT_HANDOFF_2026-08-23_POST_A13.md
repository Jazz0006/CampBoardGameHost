# CampBoardGameHost — Post-A13 / PR #43 Final Review Handoff

> Date: 2026-08-23  
> Scope: PR #43 `codex/source-decomposition-clocktower-host`  
> Role: PR #43 completion evidence and merge handoff  
> Do not treat SHA values below as permanent; always re-query GitHub first.

## 1. Current validated implementation baseline

```text
main before PR #43 merge: efd63b360ca9aba8c7890594449aa5e21817f560
PR #43 implementation:   b37f0067b674a0cd4bee5ff311840d1c52ce8c05
A13 CI:                   #534 SUCCESS
A13 R2:                   #473 SUCCESS
ASP / Real Clingo:        SUCCESS / SUCCESS
```

Documentation commits appear after the implementation baseline. Audit them separately; do not mistake a docs-only head for a new code implementation.

PR #43 final review was reported clean / merge-ready. The user subsequently authorized documentation updates and PR #43 merge, with the next task to begin in a new conversation.

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

Do not move or reorder `advanceNightStep` during future cleanup or product work without an explicit behavior-changing design.

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

The optional A14 audit concluded **do not implement A14 inside PR #43**:

- Overview / Vote / EndConfirm presentation screens already have clean owners in `ClocktowerDayScreen.kt`;
- Host retains mostly state transitions and callback transactions for those paths;
- Nomination/Virgin, Slayer, Artist and Klutz are tightly coupled to registration/recommendation state and commit ordering;
- further extraction inside PR #43 would likely introduce a context/parameter bag or move protected transaction/lifetime ownership for little architectural benefit.

Therefore PR #43 decomposition is implementation-complete after A13.

## 6. New architectural rule — Host growth freeze

`ClocktowerHostScreen.kt` is allowed to remain large after PR #43, but it is no longer an acceptable default landing zone for new feature bodies.

Future work should place new algorithms, history/session behavior, recommendation policy, role interaction presentation and persistence logic in their natural owners. Host additions should normally be thin orchestration/wiring or protected transaction/state-lifetime code.

If a future feature would add hundreds of lines to Host, stop and identify a stronger owner first.

This is a growth freeze on **new responsibility**, not a mechanical byte limit.

## 7. Large-file state after PR #43

The earlier R2 `MainActivity` decomposition is complete; `MainActivity.kt` is now only a small Android shell.

The largest remaining production sources are approximately:

```text
CampBoardGameHostApp.kt      325,556 bytes
ClocktowerHostScreen.kt      295,644 bytes
ClocktowerDayScreen.kt        63,135 bytes
ClocktowerNightStepUi.kt      45,251 bytes
```

Therefore the next structural priority is not another A14 Host slice. It is the app-root owner:

```text
CampBoardGameHostApp.kt
```

## 8. After PR #43 merge — revised next task

Do **not** go directly to A3.

The user-approved sequence is:

```text
merge PR #43
-> start a NEW conversation
-> create a fresh structural branch from live main
-> audit and decompose CampBoardGameHostApp.kt by cohesive ownership
-> remeasure remaining large production files
-> only then resume A3 historical multi-night exact baseline
```

The authoritative next-task handoff is:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md
```

`ClocktowerDayScreen.kt` (~63 KiB) may be audited after App-root decomposition, but only split it if a natural low-coupling owner exists.

Do not mix A3, B4/ZDD production promotion, history UI redesign, misinformation expansion, broader manual UI rollout or a new state-management framework into the structural App-root PR.

## 9. Working model

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

Luna must not independently reopen A14, redesign ownership boundaries, or begin A3 inside the structural App-root task.
