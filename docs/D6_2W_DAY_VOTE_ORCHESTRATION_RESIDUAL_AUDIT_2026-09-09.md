# D6.2w — Day vote orchestration residual audit

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / READ-ONLY / NO-GO FOR ANOTHER VOTE WRAPPER  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.  
> D6.2v docs checkpoint: `ef24787c18c1aed95a8c9904a73596e5e62f5dc7`.  
> Latest validated production checkpoint remains `a692cc722f1e597e747154bf05a2689fee9bed4c`; final validated code/test head remains `b2263cd08bc2ce223598698324bf2b22243c91f2`.

## Decision

Do **not** add another Day-vote controller, state bag or wrapper around the surviving square-table vote flow.

After D6.2i, the vote path already has the intended ownership split:

```text
Judge-local nomination pair
    -> ClocktowerVoteTableScreen
       owns pending seat taps through ClocktowerTableVoteState
    -> commitClocktowerVoteTransaction(...)
       computes one immutable semantic transaction result
    -> Host/Judge applies that result to durable external owners
       + records vote chronology
       + clears transient nomination
       + routes Day back to Overview
```

The remaining Host callback is not accidental UI ownership. It is the application boundary where an immutable vote transaction crosses into durable ghost-vote/highest-vote/history/route authority. Moving it into the vote screen would make the screen a second durable state owner and would couple it to Recovery/session orchestration.

## Existing boundaries

### Pending vote state belongs to the vote table

`ClocktowerVoteTableScreen` already owns the interaction-local pending vote state with `remember(...)` and initializes it from `clocktowerTableVoteState(...)`.

Seat taps only call `togglePendingVoter(...)`. The table owns:

- ordered clockwise voter sequence ending with the nominee;
- selectable seats;
- selected pending voters;
- pending vote count;
- table interaction/highlight/locked-seat presentation;
- confirm/cancel UI.

No durable vote is recorded until `onConfirm(voteState)`.

This is exactly the transient ownership boundary established by the D6.2h/i audit.

### `ClocktowerTableVoteState` owns pending-vote invariants

The typed state validates:

- physical seats are unique and contiguous;
- the nominee belongs to the table;
- pending voters are currently selectable;
- ghost-vote authority determines selectable dead/alive seats;
- selected voter ordering follows the canonical clockwise vote order.

`togglePendingVoter(...)` returns a new validated table state rather than mutating durable game/session state.

### `commitClocktowerVoteTransaction` owns atomic semantic commit calculation

The transaction function consumes one immutable pending table state and the durable high-vote baseline, then returns one `ClocktowerVoteTransactionResult` containing:

- confirmed vote record;
- consumed/updated ghost-vote authority;
- next highest-vote name/count;
- execution candidate.

The function's documentation explicitly states that UI only collects pending taps and that durable voter snapshot, ghost-vote consumption and high/tie standing are committed from the same immutable pending state.

This is already the cohesive vote semantic owner D6 would otherwise seek to create.

## Remaining Host/Judge callback is legitimate orchestration

The current `onConfirm` callback performs only the cross-boundary application work after obtaining the typed transaction result:

- `onGhostVoteAuthorityChange(voteTransaction.ghostVoteAuthority)`;
- update durable `highestVoteName` / `highestVoteCount`;
- `recordVoteEvent(voteTransaction.voteRecord)`;
- clear Judge-local `nominatorName` / `nomineeName`;
- route `dayMode` back to `Overview`.

These writes intentionally span different owners/lifetimes:

- ghost-vote authority and high-vote state are Recovery/durable vote mechanics;
- vote record is durable chronology;
- nomination pair is transient Judge state;
- day mode is route/orchestration state still written by multiple special Day paths.

A new wrapper would not reduce those authorities. It would merely hide their application behind callbacks or capture them into a broad mutable context.

## Rejected shapes

This audit rejects:

1. a `DayVoteArgs` / `VoteControllerState` bag;
2. moving highest-vote or ghost-vote durable state into `ClocktowerVoteTableScreen`;
3. making the vote screen write chronology/session/Recovery state;
4. wrapping `commitClocktowerVoteTransaction` solely to shorten the Host callback;
5. moving `dayModeState` into the vote owner while Artist/Klutz/other Day routing still writes it;
6. merging nomination, vote and execution into one broad Day transaction merely for decomposition.

## D6 consequence

The Day vote R2 pilot is already at an honest typed boundary after D6.2i plus the existing table-state and vote-transaction extraction. There is no remaining safe vote-specific extraction with meaningful ownership gain.

### Next: D6.2x — setup effect-owner necessity audit

The global D6 audit listed only a **necessary** setup-effect owner as the final R2 candidate. Perform a read-only re-audit of surviving setup Compose effects/side effects after R0–R2 changes and determine whether any effect still combines:

- setup/recommendation preparation;
- session mutation;
- persistence/recovery;
- route transition;
- telemetry/publication;
- A4/live prewarming.

Extract only if one cohesive effect transaction can be named and proven without moving canonical session authority or introducing a broad effect context. If current effects already delegate to typed services/transactions and only sequence owner callbacks, record NO-GO.

After D6.2x, perform the planned first-round R0–R2 acceptance remeasurement before considering R3.

## Validation classification

This slice is documentation/read-only architecture work only. No production source, tests, workflow, persistence, vote semantics, Recovery state or runtime behavior changed. Per `AGENTS.md` and `TESTING_STRATEGY.md`, no Android RED/GREEN or broad regression run is required for this docs-only checkpoint.
