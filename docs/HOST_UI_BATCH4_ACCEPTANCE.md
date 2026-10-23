# Host UI Batch 4 acceptance

Status: implemented for stacked Draft review, not merged. Parent: Batch 3 #359 at `85d3833cb252e87db6ae61b99217d92c20a1b313`, full CI #4608 / R2 #4159 passed. Batch 3 remains unmerged; this branch does not merge or duplicate that review. Exact Batch 4 HEAD and remote full CI/R2 are recorded in its PR.

## Accepted snapshot ownership

`ClocktowerSessionUiState` binds one Session instance and exposes a Compose-observable snapshot with a private setter. Its generic `commit { session -> ... }` executes the existing Session operation, publishes only after success and preserves its return value, including `false`. IOException leaves the previous snapshot visible and raises the existing save error. Session retains its existing persist-before-update and atomic rollback behavior; no Compose dependency, listener, API or JSON changes enter the Session core.

The App creates a holder on new game/restore, deactivates the previous holder before replacement, and deactivates on unmount. A captured old-holder callback is rejected before its block runs, preventing old-session writes as well as publication into a new game. Existing AI completion checks still compare Session instance and revisions. Main-thread synchronous commits remain the contract; this adds no concurrency/transaction engine.

SessionHost receives the holder, reads its snapshot and sends all night/day/setup-handoff confirmations and player-input revisions through `commit`. App persistence binding/retry uses the same error boundary. Boolean nomination and Night Next return values remain intact. Cards are keyed by accepted snapshot and language, and their read-only getter observes the latest accepted snapshot even within a callback before recomposition. Round, phase, results and host-tools theme derive from that snapshot.

## Retired paths and scope

Zero production references to `clocktowerUiTick`, `refreshClocktowerUi`, `onChanged`, `confirmClocktowerStep` and `confirmClocktowerNomination`. The tick, refresh callback chain, both duplicated step-confirmation wrappers and special Boolean wrapper are removed. No whole production or test files retired. No authoritative game facts move out of Session; rules, AI provider/prompts/memory, persistence format, setup diversity and Batch 5 lifecycle ownership are unchanged.

Production source net **−274 bytes** relative to Batch 3. App root: **48,742 B**; SessionHost: **20,869 B**; new holder: **1,154 B**; cards: **2,541 B**. All production source files stay below 50,000 B; Session remains 46,770 B.

## Validation

Local `:app:testFull :app:assembleDebug`: **616 tests / 134 suites, zero failures/errors/skips**, Debug APK built. Original R2 workflow shell and whitespace check pass. Local Gradle uses the external proxy/CA init script; it is not checked in.

Three new holder regressions verify:

- During atomic save and after IOException, neither snapshot nor card death advances; retry publishes the accepted state.
- Consecutive commits update the observable revision and preserve false/true return values.
- A suspended callback resumed after replacement cannot execute its old Session operation or overwrite the restored holder, including a restore with the same game ID.

The 39-test D2 Host suite now uses the production holder for its snapshot and Night Next. Existing double-click Next, one final Observation, cross-role/night preview invalidation and failed-save retry tests pass. External fixture mutations explicitly publish accepted snapshots; the pre-render Fortune Teller red-herring fixture was updated accordingly. The five actual App regressions pass with production holder wiring: MANUAL TB/NGJ launch/deal/play/ending/restored restart, and assisted/automatic restored ongoing-game night/death/later-wake/ending flows. Provider setup and paid calls are not part of those AI-mode tests.

No real-device visual, calls/background/process-kill or paid-provider acceptance is claimed. Next: review Batch 3 then Batch 4 with exact full CI/R2; Batch 5 remains unimplemented. No merge authorization was added by the request to continue Batch 4.
