# D6.2c Artist Confirmation Contract Audit

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/d6-2-ui-composition`
> Latest validated production-code checkpoint entering this audit: `58bc1e51440d44f36e14d1a9d5a45cfe9c235955`
> Scope: **read/design-only characterization; no Artist production edit in this document**

## Result

D6.2c confirms Artist is a real cohesive ownership boundary, but unlike Slayer it requires one narrow callback-contract change before its transient selection state can move down cleanly.

Selected next production shape:

```text
Judge/UI-local transient Artist selection
  claimantName
  truthfulAnswer
  shownAnswer

        |
        | confirm complete values
        v

App durable boundary
  onConfirmArtistQuestion(
      claimantName: String,
      truthfulAnswer: Boolean,
      shownAnswer: Boolean,
  )
```

Do **not** introduce `ArtistState`, `ArtistActions`, a broad Day controller or a ViewModel for this slice.

## Current boundary

`ClocktowerJudgeScreen` currently receives three Artist values from App:

```text
artistClaimantName: String?
artistTruthfulAnswer: Boolean?
artistShownAnswer: Boolean?
```

and four Artist callbacks:

```text
onSelectArtistClaimant: (String?) -> Unit
onSelectArtistTruthfulAnswer: (Boolean?) -> Unit
onSelectArtistShownAnswer: (Boolean?) -> Unit
onConfirmArtistQuestion: () -> Unit
```

This creates a round trip for purely transient UI selection:

```text
Judge user interaction
-> selection callback
-> App transient state mutation
-> value passed back to Judge
-> confirm callback
-> App rereads the same transient values
```

The selection half is UI ownership. The confirm half is durable orchestration.

## Actual Judge consumers

### Opening Artist mode

The Day overview currently calls `onSelectArtistClaimant(null)` before entering Artist mode. App's claimant-selection callback also clears truthful/shown answers, so this is effectively a three-field reset hidden behind one callback.

Equivalent local behavior after ownership movement:

```text
artistClaimantName = null
artistTruthfulAnswer = null
artistShownAnswer = null
dayMode = Artist
```

### Claimant selection

Current App callback:

```text
set claimant
clear truthful answer
clear shown answer
```

This is a cohesive UI-selection transition and should be local to Judge.

### Truthful-answer selection

Current App callback:

```text
set truthful answer
clear shown answer
```

This is also a local UI-selection transition: changing the truth invalidates any previously chosen displayed answer.

### Shown-answer selection

Current App callback only stores the chosen displayed answer. Automatic storyteller mode also writes this value through the same callback from a `LaunchedEffect`.

After localization, the effect can assign the local `artistShownAnswer` directly.

### Back/cancel

Current Artist back path calls `onSelectArtistClaimant(null)`, which indirectly clears all three fields, then returns to Day overview.

After localization, clear all three fields directly before returning to overview.

### Confirmation

Judge currently enables the primary action only when:

```text
claimantName != null
truthfulAnswer != null
shownAnswer != null
gameOutcome == null
```

but passes only `onConfirmArtistQuestion()`.

App then rereads all three App-owned transient values, updates durable mechanics/history, clears the transient values, returns Day mode to Overview and advances game-state revision.

This is the actual architectural mismatch.

## Durable responsibility that must remain above

The App confirmation body currently owns real durable behavior and should remain there:

- append claimant to `artistClaimedNames` if needed;
- mark `artistUsed` when a real Artist makes the claim;
- append the Artist resolution record;
- append the `RoleAction` event including truthful/shown answers;
- return `dayModeState` to Overview;
- advance game-state revision.

None of those should move into Judge or a Compose-local state holder.

## Selected confirmation contract

Change only the durable callback shape:

```kotlin
onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit
```

The three values are complete and already validated by the UI before confirmation.

App should consume the arguments rather than read App transient state.

This mirrors the already-correct Slayer boundary:

```text
Slayer UI selection -> onSlayerShot(complete values) -> durable App logic
Artist UI selection -> onConfirmArtistQuestion(complete values) -> durable App logic
```

The abilities remain separate contracts; there is no justification for a shared mega action model.

## Expected production ownership move

App should lose:

```text
clocktowerArtistClaimantName
clocktowerArtistTruthfulAnswer
clocktowerArtistShownAnswer
```

plus their reset/forwarding plumbing and the three selection callbacks supplied to Judge.

Judge should own:

```kotlin
var artistClaimantName by remember(gameId) { mutableStateOf<String?>(null) }
var artistTruthfulAnswer by remember(gameId) { mutableStateOf<Boolean?>(null) }
var artistShownAnswer by remember(gameId) { mutableStateOf<Boolean?>(null) }
```

`remember(gameId)` prevents stale transient values crossing game/session boundaries, consistent with D6.2b Slayer ownership.

## Expected boundary delta

Starting from validated D6.2b metrics:

```text
Judge parameters:        101
callbacks:                39
providers:                 3
MutableState params:       8
App-root clocktower vars: 39
```

If implemented exactly as characterized:

```text
remove 3 Artist value parameters
remove 3 Artist selection callbacks
retain 1 Artist durable confirmation callback, but make it value-carrying

expected Judge parameters:        95
expected callbacks:                36
expected providers:                 3
expected MutableState params:       8   (Artist values were plain values, not MutableState params)
expected App-root clocktower vars: 36
```

This is a larger real fan-out reduction than D6.2b while still remaining one ability-specific boundary.

## Test audit

The root unit-test inventory contains no dedicated `ClocktowerArtist...Test` file at this checkpoint.

Existing generic table/selection tests protect shared UI primitives, but no dedicated Artist test was found that asserts this confirmation payload boundary.

However, the proposed contract itself is strongly compile-time constrained:

```text
() -> Unit
becomes
(String, Boolean, Boolean) -> Unit
```

The compiler therefore forces the Judge confirmation callsite and App consumer to agree on all three values.

Do **not** create a generic snapshot/source-string RED merely to satisfy process.

Before production implementation, add a focused RED only if implementation requires introducing a separately testable Artist transition/helper abstraction. If the implementation remains direct local Compose state + typed callback, use the existing test suite plus compilation/FULL acceptance rather than inventing an abstraction solely for testing.

## Exact behavior to preserve

1. Opening Artist starts with all three selections empty.
2. Changing claimant clears truthful + shown answers.
3. Changing truthful answer clears shown answer.
4. Automatic storyteller answer updates only the shown answer for the current claimant/truth.
5. Confirm is disabled until all three values are present.
6. Back clears all three selections.
7. Successful confirm clears transient state and returns to Day overview.
8. Durable record/event text receives the exact selected truthful/shown values.
9. `artistUsed` / `artistClaimedNames` semantics do not change.
10. Exactly the existing game-state revision behavior is preserved.
11. Recovery v2 remains unaware of these transient selections.

## D6.2d authorized scope

Next production slice may implement only the characterized Artist ownership move:

```text
3 App-root transient Artist vars -> Judge-local remember(gameId)
3 selection callbacks -> local Judge mutations
onConfirmArtistQuestion() -> value-carrying durable callback
remove corresponding reset/forwarding plumbing
```

Explicitly out of scope:

- `records` / `onPhaseChange` dead-parameter cleanup;
- nomination/vote ownership;
- Night navigation;
- Recovery changes;
- recommendation algorithm changes;
- Artist gameplay semantics changes;
- broad Day state/action bags.
