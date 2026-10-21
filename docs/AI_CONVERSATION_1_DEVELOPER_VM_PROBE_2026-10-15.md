> **2026-10-21：历史实验记录。** 该独立实验运行器、专属测试和旧输入夹具/工作流（适用者）已在全仓清理阶段退休；下方命令不再是当前可执行入口。现用 Android V3 continuation、保留会话链、请求/响应与安全回归保留，未运行付费调用。

# P1 — AI-CONVERSATION-1: developer-VM feasibility test (2026-10-15)

**Prepared only. No paid OpenAI calls have been authorized or executed.**
This experiment branch is separate from open Draft PR #307; Android production
behavior and the two V2 Host schema contracts must remain unchanged.

The Python 3 stdlib runner is \`tools/ai_conversation_probe.py\`. It accepts the
**exact existing \`PersonalDirectOpenAiV1.buildRequest\` JSON** captured privately
from a current production V2 Host request (initial or continuation). It does not
synthesize Host legal options or use old V1 request types.

Modes:
- \`opening\`: make a private copy of the exact APP opening request, with only
  \`store\` changed from false to true.
- \`chain\`: take a **completed and independently Kotlin Host-validated**
  opening response and insert its \`response.id\` as
  \`previous_response_id\`; keep the continuation request's instructions
  and strict structured schema. This mode also changes \`store\` to true.
- \`baseline\`: preserve the independent \`store:false\` V2 continuation.
- Optional \`--reasoning all_turns\` is a **model support probe** only; inspect
  returned effective \`reasoning.context\` and 4xx errors, never assume support.
- Optional \`--delta\` replaces continuation input with a Host-derived
  experimental delta; it is **not** a P2-approved production payload.

Each \`send\` invokes **one and only one** paid HTTPS call; it requires an
explicit operator flag and an installed \`OPENAI_API_KEY\` environment variable.
It never retries and stores raw responses and usage reports in mode-0600 files
under a private mode-0700 directory. The output may contain private game data
or opaque reasoning artifacts; **do not commit** it. The code never prints the
key or upstream error body. The CLI is not a substitute for the user's separate
approval of the exact paid request count, model and estimated budget in chat.

Offline setup on Oracle developer VM:

\`\`\`sh
cd /home/opc/repos/CampBoardGameHost
python3 -m unittest discover -s tools -p 'test_ai_conversation_probe.py'
umask 077
mkdir -p /home/opc/private-botc-p1
python3 tools/ai_conversation_probe.py prepare \
 --app-request /home/opc/private-botc-p1/opening-app-request.json \
 --mode opening \
 --output /home/opc/private-botc-p1/opening-prepared.json
# Do not run 'send' until the user approves paid calls.
\`\`\`

Minimum credible A/B: one stored initial V2 opening; if and only if its result
passes the **real Kotlin Host semantic gate**, one chained V2 continuation;
one independent V2 continuation control with the same game and legal domain.
Add a separate all_turns support probe only if needed. A baseline and a chain
must compare the same underlying Host state and available legal decisions.
No model output may update canonical truth or history by itself.

For every run capture: request SHA-256, status, response ID, effective reasoning
mode, input/cached/output/reasoning tokens, latency, and qualitative result.
Server-side chained history is **not free**: earlier tokens can be billed again.
\`store:true\` has different response-retention implications than APP's current
\`store:false\`. If the model rejects all_turns, fall back to explicit compact
strategic memory, not a made-up persisted reasoning feature.

This script tests Responses **transport feasibility**, not Android interoperability
or Kotlin Host semantic validation. The real app stays unchanged pending P1 results.

## 2026-10-15 execution gate finding (no billable requests made)

- Kotlin synthetic eight-seat opening **really serialized** by production `InitialJointOpeningDirectV2.encode` + `PersonalDirectOpenAiV1.buildRequest`: isolated GitHub Actions fixture job #38666521668 completed GREEN. Its output is synthetic, not user gameplay, and was not checked into the repository.
- The existing Mini MCP `app-responses` paid experiment validator rejects the essential `store:true` modification with `APP Responses wire envelope is not approved` **before any network request**. Therefore the old `store:false` APP-parity runner cannot verify `previous_response_id` or persisted reasoning.
- Do **not** spend authorized calls on the old independent `store:false` route merely to make a request count. A safe P1 runner must take the locally generated exact Kotlin request as a private artifact; enforce the explicit model/call-count/budget scope; allow `store:true` only for a scoped trial; preserve exact strict JSON Schema and instructions; and support conditional `previous_response_id` chaining only after successful Host acceptance. No blind retries.
- Private `/home/opc/private-botc-p1` contains no source captures yet. CI-exposed synthetic wire cannot be treated as a private developer-VM capture merely because it exists in GitHub logs. Any new artifact handoff must be permission-scoped and avoid copying credentials or raw game payloads into Git history.
- **Paid usage recorded for this attempt: 0 of approved maximum 3 calls; $0 incurred by P1 API experiment.** No `response.id` anchor, model support verdict, Host semantic verdict or latency/token comparison exists yet. #308 stays Draft. 

## P1 live experiment result — October 15, 2026

The refreshed Mini MCP plugin exposes `app-continuity` and PR #42 is merged/deployed on Mini MCP master. Two **real** `gpt-6-luna` Responses requests were sent on this synthetic eight-seat Trouble Brewing game (and **no third call**).

| Step | Private experiment ID | Provider duration | Input / output / reasoning tokens | Reported-token estimated USD |
|---|---|---:|---|---:|
| Stored Kotlin V2 opening | `p1-luna-opening-20261015` | 26,044 ms | 3,095 / 2,317 / 1,423 | 0.00235675 |
| Actual same-game chained Kotlin V2 continuation | `p1-luna-chain2-20261015` | 7,750 ms | 5,724 / 624 / 160 | 0.00161280 |

Both returned HTTP 200, `status:completed`, a stored response ID, and provider `reasoning.context:all_turns`; neither reported cached input tokens. Kotlin Host legality accepted the **exact first model output** via `InitialJointOpeningDirectV2.decode` against the original synthetic Host rules domain (GitHub Actions P1 fixture workflow GREEN). The second provider response returned 2 KEEP strategy revisions, zero invented decisions, and an updated concise multi-night memory; the **actual second model output** also passed `StorytellerStrategicContinuationV2.decode` in the Kotlin P1 workflow, GREEN. These two Kotlin checks do not represent real-user game adoption.

A byte-matched standalone V2 continuation baseline was generated by Kotlin and privately registered as `p1-luna-baseline-20261015` but **not run**: conservative P1 preflight estimates were $0.0281032 opening + $0.053214 chained + $0.0269836 standalone = $0.1083008, above prior **US$0.10 total estimated authorization**. The server already irrevocably reserved $0.03 + $0.06 in its P1 pilot ledger; remaining $0.01 is insufficient for the third plan. Actual reported-usage cost estimate across 2 successful calls was $0.00396955, which is **not** a vendor invoice or a release of the reserved cap. Do not bypass quota with renamed plans or alternate roots.

**Evidence boundaries:** this continuation deliberately has 0 current open legal decisions and no invented future player actions, so it is a transport and multi-night-memory continuation probe, *not* a benchmark of second-night clue choice or incremental event replanning. Its 7.75 s vs 26.04 s response times concern different prompt workloads; they cannot establish that stateful API requests are faster than stateless ones. Even when using a prior response ID, past context can remain billable and the chained request reports more input tokens than the initial call. Next testing gate requires an explicitly newly approved budget/contract version for a **true identical-input independent baseline**, followed by a separate Host-recorded event/replanning scenario.
