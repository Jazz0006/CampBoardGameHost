# D6.2m — App decoder island cleanup

> Date: 2026-09-09
> Status: COMPLETE / VALIDATED; Android FAST + CI gate and R2 PASS.
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Parent: `ddee0d3343ce7a13d5d8b12b7ef07112ca0922c0`.
> Production checkpoint: `4c16f4c09ae7df693a3b16d6910ab026373e05cd`.
> Production tree: `91385660d36c1d872f734beca99e6170b991f26e`.
> main: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.

## Scope and decision

This implements section 3.C and completes the R0 dead-code cleanup from `D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md`. Whole-repository Kotlin searches reconfirmed that the private App JSON decoder functions formed an isolated call subgraph, with no entry point outside that block. The separate `clocktowerRolesFor(playerCount)` function had only its definition.

Current history loading and writing use `GameArchiveJsonCodec`, which delegates cards, records, events and outcome data to `AppGameStateJsonCodec`. Recovery continues through RecoverySnapshotJsonCodec and RecoveryRestorePlanner. Game initialization continues through `generateClocktowerAssignments(playerCount, script)` and `clocktowerRolesForScript(script)`.

## Result

Removed from `CampBoardGameHostApp.kt`:

- playerCardFromJson and JSONArray.toPlayerCards;
- eliminationRecordFromJson and JSONArray.toEliminationRecords;
- clocktowerEventFromJson and JSONArray.toClocktowerEvents;
- JSONArray.toRecordedEpistemicObservations;
- gameOutcomeFromJson;
- the unused random `clocktowerRolesFor(playerCount)` function;
- the two directly orphaned RecordedEpistemicObservation and EpistemicSemanticJson imports.

Exact diff: one production file, +0 / -94. App changed from 4236 to 4142 lines and 236956 to 233013 bytes. No codec, schema, data class, test, workflow, dependency or UI behavior changed.

## Preserved paths

A normalized exact comparison against the parent proves that the App diff consists only of the two audited code blocks and two imports. `GameArchiveJsonCodec.decodeEntry` and `encodeEntry` remain the archive entry points. The assignment generator still has one definition and one live call. Active App state, transaction ordering, setup, Recovery v2, A4 and all other games are byte-identical.

## Evidence

- Whole-repository reference audit: PASS.
- Exact single-file semantic diff and `git diff --check`: PASS.
- Local staged tree matched the GitHub tree; remote compare shows one commit, zero behind and exactly +0 / -94.
- Normal fast-forward from the expected parent; no force-push or workflow edit.
- [CI 34294858685](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34294858685) PASS: `:app:compileDebugKotlin`, executed `:app:testFast`, and CI gate. FULL/debug APK, ASP and Real Clingo were correctly skipped.
- [R2 34294858697](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34294858697) PASS.
- Android job logs confirm `BUILD SUCCESSFUL`; this is executed remote evidence rather than workflow status alone.

This deletion does not define or change a stable behavior contract, so no new RED test was created. The existing archive and Recovery tests remain the owning evidence and are selected by the ordinary FAST gate. The immediately preceding D6.2l checkpoint supplied a full suite and debug APK build. Local Android execution remains unavailable in this workspace; no local Android GREEN or real-device validation is claimed.

## Next

R0 dead-code cleanup is complete. Re-audit the live Host information preparation and role-step materializer boundary against the post-cleanup source before choosing the first extraction family. Separate pure candidate/fact preparation from recommendation selection, telemetry and information publication effects. Preserve interaction ordering and avoid a broad NightContext or callback/state bag.
