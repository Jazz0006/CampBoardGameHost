# D6.2h Surviving Square-Table Ownership Audit

> Date: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/d6-2-ui-composition`  
> Latest validated production checkpoint: `15342f9e22ac204602680e6ef831fb4e95c7b0bf`  
> Status: **D6.2h READ-ONLY AUDIT COMPLETE — D6.2i DAY NOMINATION TRANSIENT OWNERSHIP IS THE NEXT RECOMMENDED SLICE**

## Context

D6.2e/f/g closed the retired Storyteller path:

```text
D6.2e  prove legacy HostScriptCard-style tail unreachable
D6.2f  delete unreachable tail + writerless ExecutionResult
D6.2g  remove zero-consumer plumbing exposed by that deletion
```

The live UI debt is now the surviving square-table/table path. D6.2h therefore re-audits only live composition ownership and explicitly distinguishes:

```text
transient UI selection state
vs
Recovery / durable mechanics authority
```

Do not move a value merely because it is currently passed as `MutableState<T>`.

## Current validated boundary after D6.2g

```text
ClocktowerJudgeScreen
  parameters:              92
  on... callbacks:          34
  provider functions:        3
  MutableState parameters:   8

App-root Clocktower vars:    36
```

D6.2 baseline was 103 parameters / 39 callbacks / 10 MutableState parameters / 41 App-root Clocktower vars.

## Day state re-audit

Current Judge Day-related mutable inputs include:

```text
dayModeState
nominatorNameState
nomineeNameState
currentVoteCountState
ghostVoteAuthority
highestVoteNameState
highestVoteCountState
```

These do **not** share one ownership class.

### `currentVoteCountState` is dead

The modern square-table vote flow no longer reads `currentVoteCount`.

Actual pending vote state is owned by `ClocktowerVoteTableScreen` through typed `ClocktowerTableVoteState` and is committed via:

```text
onConfirm(voteState)
-> commitClocktowerVoteTransaction(...)
```

The outer `currentVoteCount` is only repeatedly assigned `0` when nomination/vote modes open, cancel or complete. No live behavior depends on its value.

Conclusion:

> `currentVoteCountState` should be deleted, not moved or wrapped.

### `nominatorNameState` + `nomineeNameState` are transient interaction state

App-side consumers are limited to:

- declaration;
- Day-flow/reset cleanup;
- forwarding to `ClocktowerJudgeScreen`.

They are not serialized into Clocktower Recovery mechanics and are not restored as durable mechanics.

Live consumers are inside the square-table Day composition:

```text
Overview directional nomination gesture
-> Nomination confirmation table
-> Vote table
-> clear pair on cancel / vote completion
```

The pair therefore belongs to the live Day nomination interaction surface rather than App root.

### `dayModeState` must remain externally owned for now

`dayModeState` still has legitimate App writers, including:

- Recovery continuation (`Klutz` vs `Overview`);
- Artist durable confirmation routing back to Overview;
- execution/Klutz routing;
- other phase transitions.

Moving it now would require a broader routing contract and would enlarge the slice unnecessarily.

### `ghostVoteAuthority` + highest vote state remain durable / Recovery-coupled

These are persisted and restored through Clocktower Recovery mechanics:

```text
ghostVoteAuthority
highestVoteName
highestVoteCount
```

They influence durable vote authority and current execution standing across nominations. They must **not** be localized with transient nomination selection.

## Lifecycle proof for local nomination pair

Recommended local ownership:

```kotlin
var nominatorName by remember(gameId, round) { mutableStateOf<String?>(null) }
var nomineeName by remember(gameId, round) { mutableStateOf<String?>(null) }
```

Current round lifecycle is:

```text
FirstNight round 1
-> Day round 1
-> Night round 2
-> Dawn round 2
-> Day round 2
-> Night round 3
...
```

Normal Day completion increments `round` before entering Night. Therefore a `remember(gameId, round)` nomination pair automatically becomes empty at the same durable boundary where App currently resets Day transient state.

### Virgin immediate-execution path

If Virgin executes the nominator and the game continues:

```text
round += 1
phase = Night
reset Day flow
```

The keyed local pair therefore expires naturally. If the game ends, stale hidden transient selection has no visible consumer.

### Klutz paths

- Day-side Klutz is reached after the vote/nomination pair has already been cleared by the modern vote flow; continuing the game advances to the next round.
- Night death -> Klutz -> Dawn occurs in the already advanced round; no Day nomination pair exists, and returning to Dawn does not require restoring one.

No Recovery requirement was found for preserving an in-progress nomination pair across process restart.

## Recommended D6.2i cut

Remove these App-root states:

```text
clocktowerNominatorNameState
clocktowerNomineeNameState
clocktowerCurrentVoteCountState
```

Inside Judge:

```text
- remove three MutableState parameters
- own nominatorName + nomineeName with remember(gameId, round)
- delete currentVoteCount entirely
- preserve explicit pair clearing on cancel and vote completion
- preserve directional gesture semantics
- preserve Virgin durable callbacks
```

Keep external:

```text
dayModeState
ghostVoteAuthority
highestVoteNameState
highestVoteCountState
```

Expected metrics:

```text
ClocktowerJudgeScreen parameters:     92 -> 89
on... callbacks:                      34 unchanged
MutableState parameters:               8 -> 5
App-root Clocktower vars:              36 -> 33
```

Do not create `DayState`, `NominationState`, action bags, Controller or ViewModel solely to hide the parameters.

## Existing relevant tests

Existing behavior coverage already includes:

- `ClocktowerDayNominationGestureTest`
- `ClocktowerTableVoteStateTest`
- `ClocktowerVoteTransactionTest`

Do not manufacture source-string tests for the ownership move. This is a behavior-preserving refactor; compile + existing focused behavior coverage + FAST are the minimum implementation gate.

Because D6.2i changes Compose state lifetime across App/Judge, treat the final clean D6.2i production commit as an ownership checkpoint and use normal PR CI/R2; `[full-ci]` is preferred for the final clean checkpoint before documenting completion.

## Frozen invariants

D6.2i must not alter:

- nomination legality / directional gesture behavior;
- Virgin registration or execution semantics;
- ghost-vote consumption;
- highest-vote/tie/execution-candidate mechanics;
- Day execution routing;
- Recovery v2 schema or write-gate topology;
- ClocktowerGameSession canonical ownership;
- revision cadence;
- recommendation semantics;
- Undercover/Werewolf behavior.
