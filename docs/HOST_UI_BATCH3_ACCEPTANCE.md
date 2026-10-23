# Host UI Batch 3 acceptance

**Current status:** Merged as `2aa7abbf` via #359; combined main after Batch 4 is `17949e72`, full CI #4611 / APK #175 successful.

Status: implemented for Draft review; not merged. Baseline: P1 #358 squash merge `01ef5e14217e29d7c34db5efc086a023c7f1bf8f`. Baseline main full CI #4607 and Field Test APK #173 passed. Exact Batch 3 commit and remote CI/R2 evidence belong to its PR.

## Ownership and retirement

- `ClocktowerJudgeScreen` routes New Demon, Demon bluffs, Day and Night; destinations own disposable UI state. It contains no night confirmation or AI decision logic.
- `ClocktowerNightHostScreen` owns the current interaction draft, fullscreen preview, last shown information and one Next handler. State is keyed by game, phase, round and interaction, so an open or closed preview cannot cross a night/role boundary. It renders the existing `HostNightStep` and `NightAiOverlay`; its display index is derived, never authoritative.
- Next sends one confirmation intent, including the final Observation when applicable, to `ClocktowerSessionHost`. The dispatcher rejects a stale live interaction and invokes the existing Session confirmation API. A failed save retains the preview for retry. The separate private-information publication callback is removed.
- Delete `ClocktowerNightHostProjection.kt`. Its remaining rendering assembly moves to stateless `ClocktowerNightPresentation.kt`, consumed by the night screen; no replacement rules engine or cursor is introduced.
- Demon-bluff selection and New-Demon disclosure own local state alongside their existing renderers. No tests/files are retired other than the replaced production projection file.

Session remains the sole writer. Session APIs, persisted JSON, action identities, rules and AI provider/prompts/memory are unchanged. Existing AI→UI adapters remain the approved temporary debt. Manual UI refresh ticks are Batch 4, not this change.

## Validation

Cloud `:app:testFull :app:assembleDebug`: **613 tests / 133 suites, zero failures/errors/skips**, Debug APK built. Original R2 workflow shell and `git diff --check` pass. Local Gradle uses an external temporary proxy/CA init script, not a repository change.

Six added regressions cover:

- Open fullscreen preview discarded when Session changes night.
- Chef preview cannot enable Empath's Next.
- Failed save retains preview; retry commits exactly once.
- Demon-bluff confirmation routes into separately previewed Minion and Demon private information.
- Actual App continuation in AI_ASSISTED and AI_AUTOMATIC through information preview, night death affecting later wake, day and ending (two tests).

The existing self-kill/successor test also verifies the New-Demon preview gate and confirmed private notification. D2 suite: 39 tests; actual App suite: 5. Existing MANUAL TB/NGJ launch→deal→ending→restored quick restart remain covered. The new AI-mode App tests restore a game already in progress; they do not test AI provider setup or paid requests. Existing three-mode Host regressions continue to exercise the interactive night surface.

## Size and limits

Production source net **+2,725 bytes** relative to the merged baseline; this is responsibility separation, not a net-deletion claim. Judge: 2,449 B (previously 36,440 B); NightHostScreen: 24,388 B; stateless presentation: 17,457 B; SessionHost: 21,850 B. NightHostScreen exceeds the plan's approximate 15–20 KB reference; existing role rendering/AI adapters remain intact rather than adding artificial wrapper layers. All production source files remain below 50,000 B; largest App root 49,208 B, Session 46,770 B.

Real-device visual checks, calls/background/process-kill continuation and paid-provider testing were not performed. Automated tests do not constitute those acceptances. Next: review Batch 3 and exact full CI/R2, then Batch 4 observable Session snapshot and tick retirement. Batch 4/5 and AI redesign are not included.
