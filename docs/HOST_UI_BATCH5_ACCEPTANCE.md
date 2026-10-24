# Host UI Batch 5 acceptance

Status: implemented for Draft review, not merged. Parent main: `17949e72705fda0785b2d982aacfcad09e7dfa3b`. Owner-authorized Batch 3 #359 merged as `2aa7abbf` with ancestry retained; Batch 4 #360 retargeted to main and squash-merged as `17949e72`. Combined main full CI #4611 and Field Test APK #175 passed. Batch 5 exact HEAD/full CI/R2 evidence is tracked in its PR.

## Ownership

- `ClocktowerGameLifecycle` coordinates TB preparation/commit, NGJ creation, Session installation, restore, persistence binding/retry, finish/restart and completed-game rotation recording. It has no Screen/dialog state, no duplicate roles/phase/deaths/observations and no per-step confirmation wrappers. The existing SessionUi holder remains the only accepted UI snapshot adapter; Session remains the only fact writer.
- The lifecycle owns active `operationMode` with a private setter. Saved default mode stays in StorytellerAiSettings. Manual takeover persists the current game's mode; restore reads that mode; a new game reapplies the saved default and retains the NGJ manual boundary.
- App owns Screen, dialogs, deal/reveal indices and maps explicit start/opening/restore/restart results to navigation. No Session creation/confirmation or setup committer calls remain in App. New Session installation follows successful initial persistence; failed Drunk setup keeps the pending choice for retry.
- OpeningDraft calls concrete lifecycle operations and returns DEAL/JOINT/DRUNK/HOST/UNCHANGED/FAILED results. Screen callbacks, mode getter/setter, Session getter and setup-commit callback chains are removed. Pending identity/serial/mode checks remain; disposal invalidates pending requests. Joint reveal index now belongs to App.
- `AppPreferencesState` owns roster/common players/experience/language and persists through AppPreferencesStore; both SettingsContent surfaces receive that same owner. The lifecycle does not own the roster as a cached game fact.
- Restore decoding/validation lives beside ClocktowerActiveGameMetadata. Existing JSON is unchanged. Shared CampBoardGameHostTheme, PlayerCard builder, phase conversion and seat lookup replace duplicated paths. The AI adapter's phase conversion uses the shared helper; provider/request/parser/memory logic is unchanged.

## Retirement and size

Delete `TroubleBrewingDealRoleResolver.kt` and its exclusive one-test file after removing their final production consumer. Its canonical Drunk actual/shown identity and seating assertions now exercise the lifecycle→Session card path. Remove old App setup/reset/restore/finish implementations and duplicated settings argument lists in the same migration.

Production net **−293 lines / −12,305 bytes** versus merged main. App root **28,063 B** (was 48,742 B); lifecycle **8,446 B**; preferences **2,587 B**; opening draft **17,459 B**; metadata **8,571 B**. App remains above the approximate 20 KB reference because navigation and UI composition remain there; it no longer owns setup/Session write orchestration. All production sources remain below 50,000 B; largest is unchanged Session, 46,770 B.

## Validation

Local `:app:testFull :app:assembleDebug`: **620 tests / 134 suites, zero failures/errors/skips**, Debug APK built. Original R2 workflow shell and whitespace check pass. Local proxy/CA setup is external, not a repository change.

Five new lifecycle/opening regressions cover:

1. Restore an ongoing game with its active mode distinct from saved preference; manual takeover persists; quick restart reapplies the default; retired holder cannot write.
2. Initial Drunk setup save failure keeps the pending selection and installs no Session; repaired storage permits retry; restore repeats dealing; actual/shown Drunk identity and roster remain correct.
3. Assisted Drunk override vs automatic recommendation return explicit destinations, preserve/invalidate receipt hints appropriately, and persist advisory metadata without provider calls.
4. Manual takeover from shown-only dealing with/without Drunk goes to local selection or Host without repeating the deal; request serial invalidates outstanding advice.
5. Unready restart preserves the current Session/save, while NGJ still starts in MANUAL.

Existing actual App suite (5 tests), D2 suite (39 tests), both builtin scripts, night death/later wakes, save/reload, privacy and both inter-game diversity histories continue to pass. New tests use Compose's normal test-rule scheduler/cleanup: plain Robolectric lifecycle tests initially contaminated later Compose idling, so they were moved to the standard Compose rule; no production workaround or skipped assertions was introduced.

No paid API, physical-device visuals, call/background or process-kill verification was performed. AI opening tests use already-accepted advice fixtures; they are not provider acceptance. Batch 6 portable JSON documentation/fixtures and existing temporary AI adapter redesign remain separate future work. Batch 5 is not automatically merged.
