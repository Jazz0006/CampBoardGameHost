# GSP-API0 — Optional Responses API Benchmark Transport (2026-10-09)

> Scope: **experimental developer-side transport only**, in parallel with MEM0 independent testing.
> User-approved priority change: preparing the API interface need not await completed MEM0 blind scores; **actual production cutover and network gameplay dependency remain gated**.
> Preserves: Android offline Manual gameplay, Host authoritative state, legal candidate validation, provider-neutral RES contract, Recovery <=4h.

## Decision

We already have model-neutral \`StorytellerProviderRequestV1\`, \`StorytellerProviderResponseV1\`, \`StorytellerProviderResponseValidatorV1\`, and factories in \`app/src/main/.../clocktower/domain\` / \`session\`. No need for a new recommender or bespoke Drunk/Mayor HTTP pathway. API-0 does **not** serialize Android Kotlin objects, cannot mutate the Host and does **not** constitute GSP-R4 production request materialization. It works directly with the already frozen **synthetic** MEM0 fixture for **independent model experimentation only**.

The separate server-side script \`tools/gsp_api0_responses.py\` supports:
- **offline by default:** construct 9 independently frozen \`A/B/C × D0/D1/D2\` as-of prompts with stable hashes; no API key necessary;
- explicit \`--live\`, model selection, \`OPENAI_API_KEY\`, \`--max-requests 1..9\`, and private output directory **outside the repo** to permit one bounded batch of HTTPS Responses API requests;
- fixed endpoint \`POST https://api.openai.com/v1/responses\` with \`text.format=json_schema\` strict structured recommendations, \`store:false\`;
- parse structured \`output_text\`, handle refusal/incomplete/API error, validate primary and alternative IDs against **the exact input candidates**, and preserve unmodified answer samples separated from private arm mappings;
- no retries, cost estimator promises, automatic commits, device calls or silent fallback to legacy heuristics.

**Fixture safety:** \`GSP_MEM0_TB8_SEQUENTIAL_SYNTHETIC_V1.json\` includes authoring metadata that reveals the future D0 Drunk choice. API-0 explicitly constructs each decision input from \`checkpoint.canonical\`, and does **not** serialize the fixture's global \`seats\` / \`scenario\`; this prevents future-answer leakage into D0. Always audit each \`as-of\` projection if the fixture schema changes.

## Developer usage

From repository root, Python 3.10+; no third-party package required:

\`\`\`sh
python3 tools/gsp_api0_responses.py
python3 -m unittest discover -v -s tools/tests -p 'test_gsp_api0_responses.py'
\`\`\`

The first command prints a manifest with 9 prompt hashes and **network_calls: 0**. To make a *paid* experiment call, set \`OPENAI_API_KEY\` and \`OPENAI_MODEL\` securely in a private developer environment (not source, Gradle, logs, or ChatGPT messages). Then explicitly run:

\`\`\`sh
python3 tools/gsp_api0_responses.py --live --max-requests 1 --output-dir /tmp/botc-api0-first-batch
\`\`\`

Use a NEW path for every batch. Increase to \`--max-requests 9\` only after inspecting the one-call behavior and budget. This runner never chooses a model for you, never launches background jobs, and needs actual API credentials to execute. The API usage can incur separate billing from a ChatGPT subscription.

Output:
- \`S001.json\`, \`S002.json\`, etc. are model answers for blind graders;
- \`S001.prompt.txt\`... capture exact submitted input;
- \`private_arm_mapping.json\` includes arm, decision point, selected model, prompt hash, response ID, usage and per-request status. **Keep it hidden from blind graders**.
- Never commit these outputs; they may contain hidden roles, private player information and strategic notes even if the pilot fixture is synthetic.

OpenAI reference:
- https://developers.openai.com/api/docs/guides/structured-outputs
- https://developers.openai.com/api/docs/guides/conversation-state
- https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety

## Follow-on: API-1 architecture (not implemented here)

\`\`\`text
Android offline Host (canonical state + legal candidates)
   -> authenticated, opt-in, timeboxed request via own TLS inference gateway
       -> provider transport adapter (OpenAI Responses first, interchangeable later)
       -> structured output
   -> Host local response schema / legal-domain / revision validator
   -> display recommendation + rationale + alternatives only
   -> human Storyteller manually confirms; Game Engine owns commit
\`\`\`

1. **Key custody:** NEVER embed shared OpenAI keys in an Android APK, browser bundle or game JSON. For a distributed mobile product, put credentials in a private server/gateway with authentication, rate limiting, request quota and secret rotation. A personal developer-side runner is not an APK deployment design.
2. **Failure:** timeouts, offline, quota exhaustion, malformed answer, invalid candidate or stale decision leave the existing Manual flow working. No silent auto-commit and no fabricated legal outcome.
3. **Memory:** \`store:false\` uses a self-contained per-request prompt for the first A/B/C test. Host-portable strategic summaries are allowed. A future genuine conversation-memory trial may use explicit conversation state or safely resend controlled prior turns, with separate consent, data-retention and leakage experiments. Never treat the model's remembered facts as authority.
4. **Privacy:** names can be replaced by seat IDs; minimize hidden roster information, avoid unexpected cross-game identity transfer, disclose remote inference, encrypt in transit, delete raw prompts/results appropriately.
5. **Non-duplication:** do not build new Recovery history or run GSP-R1C2C-2/3 producers just to satisfy API transport. API0 and MEM0 are separate branches of progress.

## Acceptance / boundaries

Accept API-0 when the offline tests and the no-key Github workflow pass, no Android files/permissions/keys change, manual neutral provider contract remains untouched, the exact PR diff is audited, and documents correctly distinguish API capability from live network/model quality.

**Tested:** one authenticated developer-side S001 OpenAI request returned a structurally valid, Host-legal recommendation (1 attempted / 1 succeeded). **Not tested/claimed:** nine-group blind MEM0 outcome, statistical/qualitative model superiority, continuous conversation quality, Android live API display, Android secret delivery, deployment, or reliable cost extrapolation. Any further paid runs require new explicit authorization and withheld blind identity mapping.
