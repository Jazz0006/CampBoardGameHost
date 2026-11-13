# 2026-11-02 Session / UI authority and source-size audit

Publication update: PR #372 is merged at main `d10444b7d2f09a924f06676229c3edab46311605`. Head `7fe2b345` passed [full CI #4642](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/41443727697) and [R2 #4179](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/41443727632). The merge passed [main full CI](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/41444310189) and [Field Test APK publication](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/41444310152); both Android acceptance logs were checked for testFull + assembleDebug and BUILD SUCCESSFUL. The review snapshot and local/remote distinction below describe the audit stage. The reported ownership/size follow-ups and device/paid-provider limits remain open.

## Scope and evidence

Owner request: implement Courtier's role-selection-based use/skip interaction, then check the current code for one game authority, Session-controlled writes, read-only UI and the 50,000-byte source limit.

Reviewed working tree: branch `feature/bmr-opening`, base `49fb406b86c2930025d87e99e54f9921cc1ae829`, including the uncommitted BMR page changes in this chat. Live main was `27cf84fc463e1854f7c68a43d9e2e2490faa3b7a`; open Draft PR #372 contains the base implementation. Base CI #4641 and R2 #4178 passed; the Android job log actually ran `:app:testFull :app:assembleDebug`. Those remote results do not validate this uncommitted working tree.

Inventory: 246 production Kotlin files, 145 test Kotlin files, and 399 tracked executable source/build/workflow files in total. Scanned state construction, mutation, Session creation/restoration, persistence and persistent UI cursor patterns across production Kotlin, and followed the relevant entry points and consumers. This is a repository-wide ownership/size scan plus review of the critical runtime paths, not a proof of every role rule or every possible interaction.

The original untracked `package.json`, `package-lock.json` and `node_modules/` are not part of the tracked Android application and were preserved. Generated build/cache files were excluded from the source-size contract.

## Conclusion

The current production paths retain one active game authority and route confirmed writes through `ClocktowerGameSession`. No second independently writable live game, direct UI assignment to canonical player fields, persistent night UI cursor, or second active-game recovery store was found in the audited paths.

However, the stronger contract of UI only mapping Session-provided facts is not fully satisfied: BMR UI still composes some rule conditions and reads chronological facts directly. One existing test source also exceeds 50,000 bytes. Therefore this audit does **not** certify the entire tree as fully compliant.

## Findings to address

### P2: BMR UI still assembles rule-sensitive facts

- `ClocktowerBmrDayUi.kt`, execution section: combines dead/protected/Fool checks, determines whether to offer Pacifist intervention using role/alignment/impairment conditions, and recognizes Zombuul's first registered death. The confirmation in `clocktower/session/ClocktowerBmrDay.kt` checks the same mechanical conditions. These UI conditions affect available controls and proposed execution results, not merely localized wording.
- `ClocktowerBmrNightUi.kt`, current-step preparation: reads Moonchild choices directly from the timeline and combines attack-role classifications, Pukka settlement and Grandmother eligibility to decide whether to request the extra death result. Some underlying calculations already live in Session, but the UI still owns their composition.
- `ClocktowerBmrUiHelpers.kt`, status controls: identifies a functioning Tinker and the absence of a living Demon directly from players. These are read-only, but should be projected as Session facts rather than maintained separately by the UI.

No independent game-state writer results from these conditions. Session still validates and commits the result, so this is a confirmed ownership/duplication issue rather than evidence that the UI has already corrupted a game. No concrete divergent execution outcome was demonstrated in this audit.

**2026-11-13 follow-up (branch `claude/modest-einstein-os4fez`):** addressed. `BmrNightSelection` now also carries the step type (information / exorcised / attacks), the fixed Storyteller-resolution target, the old Pukka choice and the Grandmother risk; the night UI reads these and `nextBmrNightInteraction` instead of re-deriving them. `bmrExecutionOptions()` feeds both the Day dialog and `confirmBmrEndDay`; `bmrTinkerStatus()` / `bmrNoLivingDemon()` feed the status controls. Repeated lookups (previous day's Moonchild choice, Gossip statement and deaths; attack and once-per-game role sets) now have one Session definition. The review found and fixed three divergences: the table offered "dies: Yes" for a Demon/Assassin attack on the Goon and for a protected Grandmother, which confirmation rejected; and a Pacifist save was accepted for a dead or certainly protected target. Owner rule ruling the same day: a drunk ability never takes real effect, so the Goon also stops the Assassin's kill (previously Storyteller choice). Local testFull 662/0 failures and assembleDebug passed; remote CI is per the pushed commit. The D2 test-file size item below remains open.

Recommended next change (historical): extend existing Session fact projections / narrow BMR selection facts with execution options, actual current-step subjects and status-tool availability; let UI map seats to labels and collect discretionary choices. Reuse those facts in confirmation where applicable. Do not introduce a generic manager, another rule engine or persistent pending operation. Keep normal input drafts such as a selected character, radio choice and fullscreen visibility local to UI.

### P2: one existing test source exceeds the source limit

`app/src/test/java/com/codex/campboardgamehost/ClocktowerD2UiTest.kt` is **57,536 bytes**, 7,536 bytes over the strict 50,000-byte limit. It was already oversized before this chat's changes and was not changed here.

All production Kotlin files are below the limit. Do not hide this test exception by reporting only production sizes. Recommended follow-up: split the existing D2 scenarios by coherent interaction responsibility while retaining their actual privacy, final-preview and confirmation behavior; do not add tests just to reorganize them or split out arbitrary single-function files.

### Growth warning: BMR night UI remains under the limit but is accumulating responsibilities

`ClocktowerBmrNightUi.kt` is **36,333 bytes**, compared with **14,273 bytes** at the base commit. The page unification in this chat added role-specific presentation to this file. It does not currently violate the size limit, but further role work should move coherent presentation responsibilities out before the file reaches the boundary, alongside the Session-fact convergence above. A file below 50,000 bytes is not by itself proof of good responsibility separation.

## Verified ownership boundaries

| Boundary | Evidence and result |
| --- | --- |
| New active game | `ClocktowerGameLifecycle.installNew()` creates the production Session, installs persistence, then deactivates the prior UI adapter before publishing the replacement. |
| Restore | `ClocktowerActiveGameMetadata.restore()` creates the restored Session; lifecycle guards against an already active session, completes its initial save before adopting AI metadata and publishing its adapter. |
| UI snapshot | `ClocktowerSessionUiState.snapshot` has a private setter and is refreshed from the same Session only after a successful confirmation. It is an accepted-state projection, not another game engine. |
| UI actions | BMR controls accept a `ClocktowerSessionState` plus an intent callback. `ClocktowerSessionHost.confirmBmr()` routes that callback through `sessionUi.commit` and `confirmBmrHostAction()` on the live Session. Other scripts' confirmation dispatch also uses the same adapter and Session. |
| Canonical mutation | `PlayerState`, `GameState` and `ClocktowerSessionState` expose `val` facts. The Session's `state` setter is private. Session extension functions construct candidate copies and submit confirmed operations; no production UI assignment to canonical player facts was found. |
| Pregame state | `bmrDraft` and setup materializers exist before Session creation. BMR's draft is cleared after a successful start. They are not maintained as live-game authorities. |
| Advisory state | Cards, `GameSnapshot`, TB runtime projectors and AI memory are derived/context data. Their production consumers do not replace the canonical Session game. `updateGameState()` has no production caller; its presence alone is not evidence of a live UI bypass. |
| Ordered confirmation | BMR confirmation functions validate the current step / revision or a stable accepted action ID and publish facts through Session. Night completion is checked against remaining Session interactions. No saved UI step counter or pending-advance engine was found. |
| D2/private display | Information remains in temporary previews until Next. Player renderers receive the selected information and empty host-card lists on the BMR path. The existing handoff test checks actual recipients, no publication during display and only the final preview's confirmation. |
| Save/rollback | `confirmAtomically()` batches facts, persists once and rolls state back on failure. `ClocktowerActiveGameStore` uses one `active-clocktower.json` AtomicFile, validates saved bytes and uses the same file for continuation. Credential and setup-diversity stores are separate concerns, not competing current-game stores. |
| Stale AI work | `ClocktowerAiConversation` checks current Session identity, accepted snapshot identity, current step, mode, credentials and request key before and after the request. Strategic continuation also checks the frozen Session state. Physical interruption and paid-provider execution are not claimed. |

## Exact source sizes at the audited working tree

Limit: **50,000 bytes**, not 50 KiB.

| Source | Bytes | Status |
| --- | ---: | --- |
| `ClocktowerD2UiTest.kt` | 57,536 | Existing violation, test source |
| `clocktower/session/ClocktowerGameSession.kt` | 47,116 | Largest production file; 2,884 bytes remain |
| `ClocktowerSquareTableUi.kt` | 39,846 | Below limit |
| `ClocktowerNightStepUi.kt` | 36,637 | Below limit |
| `ClocktowerBmrNightUi.kt` | 36,333 | Below limit, growth warning |
| `clocktower/session/StorytellerStrategicContinuationV2.kt` | 31,318 | Below limit |
| `CampBoardGameHostApp.kt` | 31,167 | Below limit |

Other tracked files above 50,000 bytes are data/resources/documents: the 331,689-byte TB setup JSON, the 1,387,505-byte launch PNG, and historical audit/roadmap documents. They are not executable source-size violations.

## Courtier change and validation

Courtier now uses one transient selected character: empty means skip, a selected character means use, and Clear returns to skip. The page uses the shared wake table and marks only the Courtier as the waking player. The character list comes from the Session selection facts even before a character is selected. It includes out-of-play script characters. There is no player result display or separate yes/no input.

Session still controls once-per-game consumption, role validity, effect application and duration. The only Session projection change makes Courtier's allowed character list available before use is selected; no action schema, saved JSON or restoration format changed. Selecting or clearing a character does not write facts.

Validation: local `:app:testFull :app:assembleDebug --offline --no-daemon` passed on the working tree. The added representative real UI flow checks selection/clear, skipping without spending, using on the next night, exactly one consumed use and the resulting drunkenness/restore. Its initial test failure was a selector ambiguity between the table's Po label and the dropdown's Po option; scoping the selector to the popup fixed it. The complete suite then passed. Existing single-Session, atomic save/restore, stale-response and private-display tests were included.

Not performed: current-tree remote CI/R2, device visual acceptance, physical process-kill, or paid API calls. The findings above are reported for a focused follow-up; no broad rule/architecture rewrite or test-file split was bundled into this page change.
