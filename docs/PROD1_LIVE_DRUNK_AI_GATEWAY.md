# PROD-1 — Live TB Drunk recommendation: first production-facing vertical

Status: implementation candidate / **not accepted until Android CI, R2 and an authenticated HTTPS device-to-gateway smoke pass**.

## Owner path

1. Actual App `startTroubleBrewingGame` produces `TroubleBrewingPreparedSetup` from the TB rules/data source. If Drunk exists and >1 legal candidate, the existing manual selection router remains the control surface.
2. Before a `ClocktowerGameSession` exists, Host uses `TroubleBrewingGameSnapshotProjector.fromIntermediate` (**SETUP_PRECOMMIT**, Townsfolk actual roles UNCOMMITTED), `DrunkAssignmentDecisionBoundary.create`, `StorytellerProviderRequestFactoryV1` and `StorytellerProviderGameContextBuilderV1` to materialize the current request. Every shown seat/role and full legal domain is sent. No nonexistent Session history is invented.
3. User may select directly offline, or optionally enter a **dedicated HTTPS gateway URL and gateway-only bearer access token** and request assistance. The token exists in transient UI state only, never BuildConfig or app preferences; **never enter the OpenAI API key here**.
4. `ProductionDrunkAiGatewayV1` serializes the versioned request and calls the secure gateway; it parses into existing `StorytellerProviderResponseV1`, runs existing ID/revision/legal validator, **and** requires the exact current pending Host decision instance/domain. Back/new setup cancels relevance; a late reply is ignored.
5. UI shows primary selected, distinct alternatives and rationale. Host manually confirms through `PendingDrunkAssignmentDecision.confirm` then the **existing** `TroubleBrewingSetupCommitter`; provider never writes GameState. All manual choices remain available.
6. If Drunk absent or exactly one candidate, original automatic rules path is retained and no AI request is made. AI_AUTOMATIC remains PROD-2, not silently enabled.

## Secure gateway deployment

`tools/storyteller_gateway.py` uses Python standard library. Requires server-only `OPENAI_API_KEY`, `OPENAI_MODEL` (actual API model ID; **not** a ChatGPT display nickname) and `GATEWAY_ACCESS_TOKEN` (separate random dedicated access token). Optional `GATEWAY_MAX_CALLS_PER_HOUR` defaults to 10 and `GATEWAY_PORT` defaults to 8765. No credential is checked into the repository.

Launch only on the trusted server with `python3 tools/storyteller_gateway.py`. The service **binds 127.0.0.1 exclusively** and cannot be contacted from Android directly. Publish `/v1/storyteller/recommend` only behind a configured **TLS reverse proxy** with authenticated authorization, reasonable per-client/IP rate limits, request-size and connection limits. Client enforces HTTPS and rejects redirects. Avoid logging headers, private game state and results in the proxy. Use an unprivileged account and an independent spending limit on the provider account. Never expose the loopback HTTP listener publicly or assume Oracle developer-machine tunnel is a secured public gateway.

Provide the app with `https://<your-gateway-domain>/v1/storyteller/recommend` and its **gateway access token**, not the OpenAI key. The Android app asks for these per attempt; their persistence and onboarding are deliberately outside the first vertical. Production-ready rollout requires a publicly resolvable HTTPS gateway domain, certificate and ingress authentication, which are **not provisioned** by this PR.

## Validation

- Offline Android JVM: `ProductionDrunkAiGatewayV1Test` checks all shown roles, legal IDs, uncommitted Drunk, invalid model recommendation, current-decision stale rejection.
- Offline Python: `python3 -m unittest tools.tests.test_storyteller_gateway` checks prompt/data completeness, fake/duplicate IDs and quota. No network or paid calls.
- Required Android GitHub CI/R2: compile, host setup and UI regressions.
- Pending device smoke: real TB Drunk multi-candidate setup -> authenticated HTTPS gateway -> chosen configurable model -> legal primary/alternative shown -> Host confirmed setup. Test network loss and invalid/stale responses. No claim of acceptance until verified.

## Boundaries

This is PROD-1 AI_ASSISTED for one real TB decision, not full first-night AI or full autonomous Storyteller. On error the existing manual path remains authoritative. No new heuristic ranking, Recovery expansion or MEM0 benchmark gate is introduced. PROD-2 adds explicit mode switching and safe automatic Host confirmation without changing the provider's write authority.
