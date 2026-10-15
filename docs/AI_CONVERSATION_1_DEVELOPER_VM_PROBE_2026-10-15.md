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
