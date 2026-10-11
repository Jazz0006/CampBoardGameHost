package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * PRIVATE DEVICE EXPERIMENT ONLY. A user-entered key stays in Android Keystore-backed
 * no-backup storage; the application must never contain a compiled-in API credential.
 * This adapter keeps the same Host request, result validation and commit boundaries
 * as the gateway. It is NOT an architecture for distributing mobile applications.
 */
internal object PersonalDirectOpenAiV1 {
    const val ENDPOINT = "https://api.openai.com/v1/responses"
    const val DEFAULT_MODEL = "gpt-5.6-luna"
    private const val MAX_RESPONSE_BYTES = 256_000
    private val modelPattern = Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{2,79}")

    private val fullSchema = JSONObject("""{"type":"object","properties":{"strategy":{"type":"object","properties":{"situationSummary":{"type":"string"},"issues":{"type":"array","items":{"type":"object","properties":{"issueId":{"type":"string"},"priority":{"type":"integer"},"seats":{"type":"array","items":{"type":"integer"}},"diagnosis":{"type":"string"},"futureEffect":{"type":"string"}},"required":["issueId","priority","seats","diagnosis","futureEffect"],"additionalProperties":false}},"relations":{"type":"array","items":{"type":"object","properties":{"fromSeat":{"type":"integer"},"toSeat":{"type":"integer"},"label":{"type":"string"},"issueId":{"type":"string"}},"required":["fromSeat","toSeat","label","issueId"],"additionalProperties":false}},"intentions":{"type":"array","items":{"type":"object","properties":{"trigger":{"type":"string"},"approach":{"type":"string"},"tradeoff":{"type":"string"}},"required":["trigger","approach","tradeoff"],"additionalProperties":false}},"planRevisionNote":{"type":"string"}},"required":["situationSummary","issues","relations","intentions","planRevisionNote"],"additionalProperties":false},"primaryCandidateId":{"type":"string"},"rationale":{"type":"string"},"alternatives":{"type":"array","items":{"type":"object","properties":{"candidateId":{"type":"string"},"rationale":{"type":"string"}},"required":["candidateId","rationale"],"additionalProperties":false}},"uncertainty":{"type":"array","items":{"type":"string"}}},"required":["strategy","primaryCandidateId","rationale","alternatives","uncertainty"],"additionalProperties":false}""")
    private val analysisSchema = JSONObject("""{"type":"object","properties":{"strategy":{"type":"object","properties":{"situationSummary":{"type":"string"},"issues":{"type":"array","items":{"type":"object","properties":{"issueId":{"type":"string"},"priority":{"type":"integer"},"seats":{"type":"array","items":{"type":"integer"}},"diagnosis":{"type":"string"},"futureEffect":{"type":"string"}},"required":["issueId","priority","seats","diagnosis","futureEffect"],"additionalProperties":false}},"relations":{"type":"array","items":{"type":"object","properties":{"fromSeat":{"type":"integer"},"toSeat":{"type":"integer"},"label":{"type":"string"},"issueId":{"type":"string"}},"required":["fromSeat","toSeat","label","issueId"],"additionalProperties":false}},"intentions":{"type":"array","items":{"type":"object","properties":{"trigger":{"type":"string"},"approach":{"type":"string"},"tradeoff":{"type":"string"}},"required":["trigger","approach","tradeoff"],"additionalProperties":false}},"planRevisionNote":{"type":"string"}},"required":["situationSummary","issues","relations","intentions","planRevisionNote"],"additionalProperties":false}},"required":["strategy"],"additionalProperties":false}""")
    private val compactSchema = JSONObject("""{"type":"object","properties":{"candidateId":{"type":"string"},"planMemo":{"type":"string"}},"required":["candidateId","planMemo"],"additionalProperties":false}""")

    private val oneShotSchema = JSONObject("""{"type":"object","properties":{"gameId":{"type":"string"},"gameStateRevision":{"type":"integer"},"playerInputRevision":{"type":"integer"},"demonBluffRoleIds":{"type":["array","null"],"items":{"type":"string"}},"choices":{"type":"array","items":{"type":"object","properties":{"decisionId":{"type":"string"},"candidateId":{"type":"string"}},"required":["decisionId","candidateId"],"additionalProperties":false}},"deferredDecisionIds":{"type":"array","items":{"type":"string"}}},"required":["gameId","gameStateRevision","playerInputRevision","demonBluffRoleIds","choices","deferredDecisionIds"],"additionalProperties":false}""")

    private const val ONE_SHOT_INSTRUCTIONS = """
You are a Trouble Brewing expert Storyteller building ONE FULL-BOARD coordinated first-night
configuration. Reason internally across EVERY actual/shown seat and all legal choice domains:
Good information intersections, fair alternative worlds, evil pressure, player experience, and
how Demon bluffs interact with the available information.
Output ONLY a single complete legal first-night package, no commentary or alternatives.
For each legalScope.availableDecisions return EXACTLY ONE candidateId from its legalCandidateIds.
When legalDemonBluffRoleIds is not null, return EXACTLY THREE distinct legal bluff role IDs.
When it is null, return null. Copy deferredDecisionIds EXACTLY; they represent choices whose
player action or future dependency is not yet known. Do NOT invent poison targets, selected
Fortune Teller query seats, observations or registration witnesses. Never select an ID outside
the Host legalScope. Copy gameId/revisions exactly. The Host owns all truth and confirmation.
"""

    private const val SETUP_INSTRUCTIONS = """
You are an expert Trouble Brewing whole-game Storyteller strategist, not a Drunk selector.
FIRST diagnose the FULL actual/shown seat roster, Good information interaction, evil pressure,
plausible alternate worlds and fairness. Provide 1-4 NONDUPLICATE seat-linked issues,
0-4 seat relationships as HYPOTHESES and 1-4 CONDITIONAL future intentions.
Only AFTER the global assessment, choose one legal candidateId and a truly distinct legal
alternative where available. Never assume an UNCOMMITTED actual Townsfolk identity is known.
Never fabricate game actions, claims, future facts or Spy/Recluse registration witnesses.
The Host alone owns rules and commits. Return concise Chinese; no hidden reasoning.
"""
    private const val COMMITTED_INSTRUCTIONS = """
You are an expert Trouble Brewing whole-game Storyteller strategist.
ANALYSIS ONLY AFTER SETUP COMMIT: every actual/shown seat and Drunk assignment is Host truth.
FIRST diagnose multi-seat information ecology, evil pressure, alternate worlds and fair play.
Provide 1-4 NONDUPLICATE issues, 0-4 tentative seat relations and 1-4 conditional intentions.
Use priorStrategy only as fallible advice and explicitly KEEP/REVISE/RETIRE plans based on
confirmed Drunk assignment or its absence. Do not invent future actions, observations or
registrations. NEVER select a candidate in this checkpoint. Output concise Chinese.
"""
    private const val LIVE_INSTRUCTIONS = """
You are a whole-game Trouble Brewing Storyteller strategist. FIRST assess the COMPLETE
current actual/shown roster, legal candidates and exact ordered causalHistory as of this
pending decision. Confirmed actions and player-received observations are distinct; unrecorded
speech is UNKNOWN. Prior strategy/memo is fallible and may be wrong; prefer Host facts.
Consider Good-information interactions, evil pressure, multiple worlds and fair play across
the board, NOT a single role in isolation. Spy/Recluse registration ambiguity is hypothetical.
Only THEN select exactly ONE candidateId from current legalCandidates. Return candidateId
and one compact Chinese planMemo (80 Chinese characters or fewer) connecting an actual
cross-seat issue to a conditional future implication. Do not invent facts or hidden reasoning.
"""

    /** Convert the already-serialized, versioned Host input into a strict Responses call. */
    fun buildRequest(hostPayload: String, model: String): String {
        require(modelPattern.matches(model)) { "Invalid OpenAI model ID." }
        val case = JSONObject(hostPayload)
        val analysis = case.optString("schemaId") == "botc.storyteller-global-analysis-request"
        val oneShot = case.optString("schemaId") == FirstNightOneShotRequestV1.REQUEST_SCHEMA
        val compact = case.optString("responseProfile") == "COMPACT_MEMO_V1"
        require(!(analysis && compact) && !(oneShot && compact))
        val schema = when {
            oneShot -> oneShotSchema
            analysis -> analysisSchema
            compact -> compactSchema
            else -> fullSchema
        }
        val instructions = when {
            oneShot -> ONE_SHOT_INSTRUCTIONS
            analysis -> COMMITTED_INSTRUCTIONS
            compact -> LIVE_INSTRUCTIONS
            else -> SETUP_INSTRUCTIONS
        }
        return JSONObject()
            .put("model", model)
            .put("instructions", instructions)
            .put("input", case.toString())
            .put("store", false)
            .put("text", JSONObject().put("format", JSONObject()
                .put("type", "json_schema")
                .put("name", when {
                    oneShot -> "botc_first_night_one_shot_v1"
                    analysis -> "botc_global_analysis_v1"
                    compact -> "botc_global_compact_memo_v1"
                    else -> "botc_global_storyteller_v1"
                })
                .put("strict", true).put("schema", schema)))
            .toString()
    }

    /** OpenAI output is untrusted until the existing Host validator accepts the decision. */
    fun adaptResponse(hostPayload: String, upstreamBody: String): String {
        val case = JSONObject(hostPayload)
        val upstream = JSONObject(upstreamBody)
        require(upstream.optString("status") == "completed") { "OpenAI response incomplete." }
        val messages = upstream.getJSONArray("output")
        val text = StringBuilder()
        for (i in 0 until messages.length()) {
            val item = messages.getJSONObject(i)
            if (item.optString("type") != "message") continue
            val content = item.getJSONArray("content")
            for (j in 0 until content.length()) {
                val chunk = content.getJSONObject(j)
                if (chunk.optString("type") == "output_text") text.append(chunk.getString("text"))
            }
        }
        require(text.isNotEmpty()) { "OpenAI returned no structured text." }
        val result = JSONObject(text.toString())
        if (case.optString("schemaId") == FirstNightOneShotRequestV1.REQUEST_SCHEMA) {
            // The caller validates the entire returned bundle against the
            // exact in-memory Host legal scope and current revision. Never
            // fill missing or illegal values with a default.
            return result.toString()
        }
        val analysis = case.optString("schemaId") == "botc.storyteller-global-analysis-request"
        val compact = case.optString("responseProfile") == "COMPACT_MEMO_V1"
        val response = JSONObject()
            .put("sourceRevision", case.getJSONObject("sourceRevision"))
            .put("schemaVersion", 1)
        if (analysis) {
            require(result.length() == 1 && result.has("strategy")) { "Missing global strategy." }
            return response.put("schemaId", "botc.storyteller-global-analysis-response")
                .put("analysisId", case.getJSONObject("analysisIdentity").getString("analysisId"))
                .put("strategy", result.getJSONObject("strategy")).toString()
        }

        val legal = case.getJSONArray("legalCandidates")
        val ids = (0 until legal.length()).map {
            legal.getJSONObject(it).getString("candidateId")
        }.toSet()
        require(ids.isNotEmpty()) { "Host has no legal candidates." }
        val primary = if (compact) result.getString("candidateId")
                      else result.getString("primaryCandidateId")
        require(primary in ids) { "OpenAI returned an illegal candidate." }
        response.put("schemaId", "botc.storyteller-provider-response")
            .put("decisionId", case.getJSONObject("identity").getString("decisionId"))
            .put("primaryCandidateId", primary)
        if (compact) {
            require(result.length() == 2 && result.has("planMemo")) {
                "Invalid compact OpenAI output."
            }
            val memo = result.getString("planMemo")
            require(memo.isNotBlank() && memo.length <= 200)
            return response.put("responseProfile", "COMPACT_MEMO_V1")
                .put("rationale", memo)
                .put("alternatives", JSONArray())
                .put("uncertainty", JSONArray()).toString()
        }
        require(result.has("strategy")) { "Missing global strategy." }
        val alternatives = result.getJSONArray("alternatives")
        val selected = mutableSetOf(primary)
        for (i in 0 until alternatives.length()) {
            val alt = alternatives.getJSONObject(i)
            require(alt.getString("candidateId") in ids &&
                selected.add(alt.getString("candidateId")) &&
                alt.getString("rationale").isNotBlank()) {
                "Invalid or duplicate alternative."
            }
        }
        require(ids.size <= 1 || alternatives.length() > 0) {
            "Global recommendation omitted a distinct legal alternative."
        }
        require(result.getString("rationale").isNotBlank())
        return response.put("strategy", result.getJSONObject("strategy"))
            .put("rationale", result.getString("rationale"))
            .put("alternatives", alternatives)
            .put("uncertainty", result.getJSONArray("uncertainty")).toString()
    }

    /** Show only a safe network/error category; never expose payloads, tokens or responses. */
    fun safeFailure(error: Throwable): String = when (error) {
        is javax.net.ssl.SSLException -> "TLS certificate"
        is java.net.UnknownHostException -> "DNS"
        is java.net.SocketTimeoutException -> "timeout"
        is java.net.ConnectException -> "network connection"
        is org.json.JSONException -> "JSON response shape"
        else -> {
            val code = Regex("OpenAI HTTP ([0-9]{3})").find(error.message.orEmpty())
                ?.groupValues?.get(1)
            when {
                code != null -> "HTTP $code"
                error.message?.contains("incomplete", ignoreCase = true) == true ->
                    "incomplete model response"
                error.message?.contains("structured text", ignoreCase = true) == true ->
                    "missing structured output"
                error is IllegalArgumentException -> "strategy contract validation"
                else -> "response or configuration validation"
            }
        }
    }

    /** No redirects, arbitrary destinations, request logging, or upstream body in errors. */
    fun post(
        apiKey: String,
        hostPayload: String,
        model: String,
        onStage: (String) -> Unit = {},
    ): String {
        onStage("PREPARING")
        require(apiKey.startsWith("sk-") && apiKey.length >= 12) {
            "A personal OpenAI API key is required."
        }
        val wire = buildRequest(hostPayload, model)
        onStage("CONNECTING")
        val connection = URL(ENDPOINT).openConnection() as HttpsURLConnection
        connection.apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = 10000
            readTimeout = 90000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        }
        try {
            connection.outputStream.use { it.write(wire.toByteArray(Charsets.UTF_8)) }
            onStage("AWAITING_MODEL")
            val status = connection.responseCode
            onStage("HTTP_RESPONSE")
            if (status != 200) {
                throw IllegalStateException("OpenAI HTTP $status (credential, model, quota or network).")
            }
            val buffer = ByteArray(4096)
            val raw = java.io.ByteArrayOutputStream()
            connection.inputStream.use { stream ->
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    require(raw.size() + count <= MAX_RESPONSE_BYTES) {
                        "OpenAI response exceeds safe size limit."
                    }
                    raw.write(buffer, 0, count)
                }
            }
            onStage("PARSING_RESPONSE")
            return adaptResponse(hostPayload, raw.toString("UTF-8"))
        } finally {
            connection.disconnect()
        }
    }
}
