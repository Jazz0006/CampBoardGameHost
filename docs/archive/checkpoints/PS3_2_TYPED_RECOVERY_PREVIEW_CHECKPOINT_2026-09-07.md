# PS3.2 Typed Recovery Preview Checkpoint — 2026-09-07

> Status: **PS3.2 COMPLETE / STOP BEFORE PS3.3**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> PS3.1 validated head entering this slice: `ea2cb7ff731731fce2c9b477a3eea6693d8a548b`

## 1. Scope completed

PS3.2 moved the saved-game preview path from the independent legacy raw-JSON reader to the same validated typed Recovery preparation pipeline established in PS3.1.

The production preview path is now:

```text
SharedPreferences active recovery JSON
-> Context.prepareCurrentRecoveryPlan(...)
-> RecoveryRestorePlanner
   -> validity policy
   -> strict decode
   -> complete game validation
   -> current Clocktower runtime/ruleset resolution where required
   -> ValidatedRecoveryPlan
-> RecoveryPreviewLoader
-> RecoveryPreviewMetadata
-> localized SavedGamePreview
```

The old `savedGamePreviewFromJson(...)` raw reader was removed.

## 2. Primary regression fixed

PS2 intentionally omits raw `screen` for `RecoveryEntryPoint.Stable`.

Before PS3.2:

```text
valid Stable typed recovery
-> raw screen absent
-> savedGamePreviewFromJson(...) returned null
-> loadSavedGamePreview(...) cleared active recovery
```

After PS3.2:

```text
valid Stable typed recovery without raw screen
-> typed planner validates recovery
-> preview metadata is produced
-> active recovery is not cleared
```

Typed regression coverage is in `RecoveryPreviewLoaderTest`.

Invalid, expired or incompatible recovery is still rejected and cleared fail-closed. Missing recovery does not trigger an unnecessary clear.

## 3. Additional PS3.1 runtime correction found during preview integration

Preview cutover exposed an over-constraint in the initial PS3.1 runtime contract:

- Trouble Brewing requires reconstructing the current `RulesetRef` from recovered assigned roles and current rules knowledge.
- No Greater Joy does not use that Trouble Brewing runtime ref.

The planner now:

- validates recovered Clocktower actual roles against the selected current script for both scripts;
- requires current ruleset resolution for Trouble Brewing;
- allows No Greater Joy to carry a validated Clocktower runtime with `rulesetRef == null`;
- still fails closed if role/script membership is invalid.

This is covered by `RecoveryRestorePlannerNoGreaterJoyTest`.

No Werewolf recovery support was added.

## 4. Production wiring

Production writer/read token generation now shares `RecoveryCompatibilityToken.currentFor(gameKind)` instead of duplicating the token string in the App.

`RecoveryAppEnvironment.kt` owns the current production adapter for:

- expected recovery token selection;
- Clocktower role resolution;
- Trouble Brewing current ruleset reconstruction.

This adapter is intended to be reused by PS3.3 apply so preview and restore cannot drift into separate compatibility/content-resolution paths.

## 5. Large App edit evidence

`CampBoardGameHostApp.kt` was edited through the repository-mandated locked GitHub Actions one-shot workflow.

One-shot run:

- run: `34092985011`
- bootstrap head: `623c68a3d5b26c724a043c69d76e861bf8a38932`
- locked pre-patch App blob: `31221134f3f16c39e3ea61d1649bbb249f9c15dd`
- product commit: `a7ee31fcc898973341b4c444564bad1a2869b930`
- cleanup commit: `44891a8fd7d36c53a085e576c7150934d7408a79`

The product commit changed exactly one file:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
```

Exact product diff size:

```text
8 insertions
58 deletions
```

The App change is limited to:

1. `loadSavedGamePreview(...)` -> typed `RecoveryPreviewLoader` / `prepareCurrentRecoveryPlan` wiring;
2. deletion of legacy `savedGamePreviewFromJson(...)`;
3. writer compatibility token -> shared `RecoveryCompatibilityToken.currentFor(...)`.

`restoreSavedGame()` was deliberately not rewired in PS3.2. PS3.3 owns the restore/apply cutover.

The temporary one-shot workflow and Python patch script were removed by the cleanup commit and are not part of the live branch.

## 6. Validation evidence

The one-shot workflow passed all of these gates before committing the App patch:

```text
focused typed baseline
-> PASS

exact locked App patch
-> PASS

focused typed tests after App patch
- RecoveryPreviewLoaderTest
- RecoveryRestorePlannerNoGreaterJoyTest
- RecoveryRestorePlannerTest
-> PASS

:app:testFast --rerun-tasks --no-daemon
-> PASS

git diff --check
-> PASS

exact App changed-file allowlist
-> PASS

remote-head race recheck
-> PASS
```

The one-shot intentionally did not run `:app:testFull` / `:app:assembleDebug`; those full checkpoint gates remain for PS3.4 unless a later PS3 slice specifically requires them.

A normal connector-authored checkpoint commit follows this document so the live non-bot branch head receives ordinary PR CI/R2 evidence.

## 7. Boundary after this checkpoint

PS3.2 is complete.

Do not yet:

- replace the large `restoreSavedGame()` mutation sequence;
- apply `ValidatedRecoveryPlan` to live Compose/App state;
- start PS3.4 final checkpoint gates;
- start PS4 cleanup;
- start D6 decomposition;
- merge PR #112.

Next implementation slice is **PS3.3 — Restore cutover**:

```text
load raw recovery
-> prepare validated plan
-> reject/clear invalid recovery without live mutation
-> invalidate old A4 session boundary only after plan exists
-> apply the immutable plan with no further JSON parsing or content/ruleset lookup
```
