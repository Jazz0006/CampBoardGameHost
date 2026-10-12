# AI-DIAG-1 — Android Studio / ADB live expert-Storyteller debugging

> 2026-10-12. Development-time diagnostic feature. The ZIP recorder remains unchanged for later offline real-game evidence; it is **not** the primary developer workflow.

## Developer workflow (Android DEBUG APK only)

The current Android Debug app id is `com.codex.campboardgamehost.fieldtest`. Connect via USB debugging (or wireless ADB). For a DEBUG build, detailed Host/model wire evidence is OFF by default and must be enabled explicitly for the duration of the test.

1. Enable:
   `adb shell setprop log.tag.BotcAiWire VERBOSE`
2. Observe in a terminal:
   `adb logcat -v time -s BotcAiWire:V BotcAiTrace:I '*:S'`
   In Android Studio Logcat, select the debug app process and filter `tag:BotcAiWire`. Standard `BotcAiTrace` emits safe stage/duration summaries without verbose activation.
3. Reproduce a fresh AI request. Reconstructed full JSON is available via ordered `part=1/N` ... `part=N/N` lines of **the same `call` and stage**. The current sequence is:
   - `HOST_INPUT`: exactly the Host JSON submitted to the transport
   - `MODEL_REQUEST`: actual Responses body (instructions, input, model, schema, `store:false`)
   - `HTTP`: numeric status
   - `RAW_PROVIDER_RESPONSE`: original upstream JSON
   - `PARSED_MODEL_OUTPUT`: extracted structured model JSON
   - `HOST_VALIDATION`: Host accepted or rejected the full bundle/strategy, bounded reason on failure
   - `HOST_ADOPTED`: a separate Host action when the opening is actually adopted
   - `DONE` / `FAILURE`: duration or sanitized network category
4. Disable immediately after the debug session:
   `adb shell setprop log.tag.BotcAiWire INFO`

The on-device output can include genuine game-state secrets and real player names. Do not capture public games, attach Logcat to a public issue, share full screen recordings, or turn on detailed logs outside a controlled developer test. Do not log HTTP authorization headers. The trace applies regex credential redaction defensively before writing; the known current request body does not include the API key. Debug logging must not change Host choices or provider network bytes.

## Scope and safeguards

- `BotcAiWireTrace` guards every sensitive emit with **both** `BuildConfig.DEBUG` and Android's live `Log.isLoggable(tag, VERBOSE)` setting. Release builds cannot be enabled with ADB property. Default is OFF.
- Strings are bounded to 1,800-byte UTF-8 Logcat chunks (preserving Unicode code points), with one request ID and ordered part counters. Large histories can still overrun Logcat's finite ring buffer; use a live logcat reader, not a dump after a long session.
- The raw OpenAI upstream JSON is included for debugging structure/status and response metadata. Header, bearer token and API key are not read into this log path.
- Primary development acceptance: ADB trace of *one initial V2 opening* and *one V2 continuation* includes all phases, valid Host rejection explains the failure without silent substitution, and one adoption is only shown after the actual commit. Unit tests protect chunk reconstruction and credential redaction. CI/R2 guards Android release/debug compatibility. A physical DEBUG phone with a real model call is still necessary for field acceptance.
- Existing `DebugFlightRecorder.shareDebugBundle()` in Settings is **untouched** and remains the offline fieldgame/recovery path. Later, if needed, a shared event interface could feed both live and ZIP; don't add a second full archive now.

## Not covered

This is a diagnostic seam, not the outstanding generic 3E Host transition observer, whole-unpublished residual re-planner, phone semantic acceptance, or historical archive. Model input/output is observable only when a supported personal-direct API request actually occurs; merely opening a screen does not fabricate a provider call.
