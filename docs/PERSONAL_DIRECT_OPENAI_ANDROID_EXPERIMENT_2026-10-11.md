# PERSONAL-DIRECT-1 — Android owner-only OpenAI Responses experiment

Status: **isolated experimental branch** based on PROD-GLOBAL-1 PR #292.
This is not a public mobile release, a verified device-to-OpenAI smoke test or a substitute for the private gateway.

## User-facing workflow

1. Install a build of this experimental branch on **your own trusted Android device**.
2. Configure a new TB game, select **AI assisted** (or bounded AI automatic), and set **OpenAI direct** under AI connection.
3. Paste your **own newly generated, scoped OpenAI project API Key** into the masked field. It is encrypted with a distinct Android Keystore AES-GCM key and held in `noBackupFilesDir`, not compiled into the APK or Git.
4. Leave model ID at `gpt-5.6-luna` or explicitly enter another available Responses API model ID such as `gpt-5.6-sol`. This is an API model ID, not a ChatGPT UI setting.
5. Begin TB; the existing precommit whole-board Drunk assessment, post-deal strategy and compact live recommendation paths use the exact same Host serializer/validator/Host commit control as the existing gateway transport.
6. To stop using the personal credential, tap **Erase local API Key**. To resume the old server design, switch back to **Gateway**; the two credentials are stored under different Keystore aliases, AAD, and no-backup ciphertext files. Disabling direct mode does not silently send the personal Key to the gateway.

## Security boundaries

- **Known exception to OpenAI guidance:** OpenAI advises against API keys in client apps, including keys entered by users. This exception is strictly an owner-only test on a trusted physical device; do not distribute the APK with retained credentials or repurpose for public release.
- A dedicated project, restricted/expiring API Key, monthly project budget monitoring and the owner's reported $20 budget reduce but do not eliminate financial risk. Confirm whether the specific account's configured dollar budget is an enforced hard cap rather than an alert; do not treat it as guaranteed.
- No arbitrary URL for direct mode: fixed `https://api.openai.com/v1/responses`, HTTPS-only, no redirects. Never put Authorization headers in logs, share a debug bundle containing a secret, or let game archives export the key.
- Android Keystore protects at-rest storage, **not** against an unlocked or compromised device while the app makes a request. Erase the key when no longer needed.
- No model output can directly commit game state. Existing pending-decision ID, source revision, rules-legal candidates, and state-freshness validators remain the authority. Offline manual mode requires no network.
- The direct adapter is explicitly compatible with the three already implemented flows: full global Drunk selection, confirmed post-deal analysis-only, and compact next legal decision. No alternate per-role recommender, no separate prompts in application screens.

## Acceptance

Before marking ready: Android Kotlin/JVM tests (full/compact/analysis conversion and malicious/illegal candidate), old gateway tests, build and independent CI/R2; one real Android -> Responses call with valid account Key in **device input only**, lawful Host recommendation, and manual/offline regression. Neither CI nor source control may contain an API Key or paid live call.

References:
- https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety
- https://developers.openai.com/api/docs/guides/structured-outputs

## Troubleshooting the post-commit analysis spinner (2026-10-11)

A real Xiaomi field test confirmed `POST https://api.openai.com/v1/responses`, **HTTP 200**, ~20.6 seconds, ~24.1 KiB response, while the application remained on “确认阵容后全局分析”. This demonstrates that the HTTP exchange succeeded, NOT that the model JSON passed Host validation or was accepted by the UI. No secret-bearing request/response was collected.

The previous callback had a silent-release bug: `committedAiBusy = false` executed only if ALL freshness conditions held, including hard-coded `gameStateRevision == 0` and `playerInputRevision == 0`. A legitimate nonzero revision or changed screen could leave the UI indefinitely busy after a network response.

The repair:
- Captures and round-trips actual Host revisions for the analysis contract rather than assuming zero.
- Always releases the spinner when the current request finishes; discarded results surface an explicit categorical stale reason, not a hanging screen.
- Displays elapsed time and named `PREPARING`, `CONNECTING`, `AWAITING_MODEL`, `PARSING_RESPONSE`, `VALIDATING_HOST_RESPONSE`, `SUCCESS`, `FAILED` or `STALE` phases.
- Uses safe, bounded `DebugFlightRecorder` breadcrumb event names, elapsed milliseconds and error categories; **never records the API key, bearer headers, payload JSON, player names or model text** in the new AI events.
- Includes a debug-bundle export control. The generic crash recorder may contain information from other parts of the app, so **review the archive before sharing**. Do not upload Network Inspector request screenshots containing Authorization headers.
- Host rules, source revision checks, legal candidates and Manual takeover remain authoritative.

Acceptance: Full Android CI and independent R2; run the same Xiaomi setup with the device still connected. Record the displayed stage and sanitized error after HTTP 200. A 200 response by itself is NOT a successful strategic analysis; further repair may be required if the diagnostics surface an output-contract violation.

## Real-phone second pass: first stale, retry successful, low text contrast

User confirmed the first whole-game analysis was discarded; retry accepted the resulting global strategy. The **exact stale reason remains unconfirmed**. The existing diagnostic export writes a `GLOBAL_AI_STALE` breadcrumb with a bounded `reason` field such as `PLAYER_REVISION_CHANGED`, `SCREEN_CHANGED` or `GAME_REVISION_CHANGED`. Ask for that **reason code only**, not the full secret-bearing Network Inspector request.

No ordinary identity-reveal action is expected to mutate the canonical game state. Do not loosen revision freshness based on the hypothesis that some version increment must have caused the rejection. Existing strict Host freshness checks continue to reject altered game facts or moved gameplay phases. If a proven benign revision-only change is found, fix the triggering revision semantics first.

The old poor-readability pattern was reconfirmed: a dark Material colorScheme did not bind `LocalContentColor` for uncoloured Compose `Text` beneath a plain `Modifier.background()`. The dark Clocktower theme now explicitly supplies `onBackground` as the default content colour through `CompositionLocalProvider`. This maintains the previously agreed dark background, warm-white body, gold headings and dark cards. No reflow, model/prompt changes, or unproven stale-revision acceptance logic are shipped as part of the colour correction.

Pending acceptance: CI Android full tests + device screenshot verifying readable actual foreground text; capture the *specific* first-discard reason with no API credential and test whether subsequent attempt starts cleanly.

## First-class Android Studio / ADB tracing (debug builds only)

For a local Android Studio debug build, open **Logcat** and filter with:

```text
tag:BotcAiTrace
```

Or, from PowerShell/Linux shell with the phone connected and authorized:

```sh
adb devices
adb logcat -v time -s BotcAiTrace:I
```

Reproduce the post-commit whole-game analysis. The trace contains only `START`,
`STAGE`, `DISCARDED`, `ACCEPTED`, `FAILED` or `BLOCKED` events.
`DISCARDED reason=...` provides the **precise first-discard cause**.
`sentGameRev` and `currentGameRev`, plus player-input counterparts, distinguish
real revision drift from a screen/phase mismatch.
No API key, bearer header, model body, game UUID or grimoire content is logged.
Debug logs are disabled in release builds. This does not change Host validation logic.

**Do not treat the previously guessed revision drift as confirmed.**
For test feedback, share only lines beginning with `postcommit`, or the
`reason` + four revision numbers. Avoid sharing an unfiltered Logcat transcript.
