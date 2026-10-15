package com.codex.campboardgamehost.clocktower.session

import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/** Personal direct Responses transport. Only V2 whole-game requests are accepted. */
internal object PersonalDirectOpenAiV1 {
    const val ENDPOINT = "https://api.openai.com/v1/responses"
    const val DEFAULT_MODEL = "gpt-6-luna"
    private const val MAX_RESPONSE_BYTES = 256_000
    private val modelPattern = Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{2,79}")
    private val responseIdPattern = Regex("resp_[a-zA-Z0-9_-]{8,160}")

    internal data class ResponseReceipt(val text: String, val responseId: String)

    // Legacy default remains store:false for callers that do not explicitly
    // opt into same-game provider retention. Chain IDs are opaque, never logged.
    fun buildRequest(
        hostPayload: String,
        model: String,
        previousResponseId: String? = null,
        retainForContinuation: Boolean = false,
    ): String {
        require(previousResponseId == null || (retainForContinuation &&
            responseIdPattern.matches(previousResponseId))) {
            "Invalid or nonretained Responses continuation anchor."
        }
        require(modelPattern.matches(model)) { "Invalid OpenAI model ID." }
        val host = JSONObject(hostPayload)
        val opening = host.optString("schemaId") == InitialJointOpeningDirectV2.REQUEST_SCHEMA
        val continuation = host.optString("schemaId") == StorytellerStrategicContinuationV2.REQUEST_SCHEMA
        require(opening || continuation) { "Only whole-game V2 schemas are supported." }
        val schema = if (opening) InitialJointOpeningDirectV2.responseSchema
            else StorytellerStrategicContinuationV2.responseSchema
        val instructions = if (opening) InitialJointOpeningDirectV2.INSTRUCTIONS
            else StorytellerStrategicContinuationV2.INSTRUCTIONS
        val name = if (opening) "botc_initial_joint_opening_v2"
            else "botc_whole_game_strategic_continuation_v2"
        return JSONObject()
            .put("model", model)
            // Top-level instructions and strict schema MUST be sent on every
            // chained call; previous_response_id never carries instructions.
            .put("instructions", instructions)
            .put("input", host.toString())
            .put("store", retainForContinuation)
            .put("text", JSONObject().put("format", JSONObject()
                .put("type", "json_schema")
                .put("name", name)
                .put("strict", true).put("schema", schema)))
            .apply { previousResponseId?.let { put("previous_response_id", it) } }
            .toString()
    }

    fun adaptResponse(hostPayload: String, upstreamBody: String): String {
        val id = JSONObject(hostPayload).optString("schemaId")
        require(id == InitialJointOpeningDirectV2.REQUEST_SCHEMA ||
            id == StorytellerStrategicContinuationV2.REQUEST_SCHEMA) {
            "Only whole-game V2 responses are supported."
        }
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
        // Strictly pass through untrusted V2 data. Host verifies all choices, no autofix.
        return JSONObject(text.toString()).toString()
    }

    internal fun adaptRetainedResponse(hostPayload: String, upstreamBody: String): ResponseReceipt {
        val upstream = JSONObject(upstreamBody)
        val responseId = upstream.getString("id")
        require(responseIdPattern.matches(responseId)) { "Invalid Responses anchor ID." }
        return ResponseReceipt(adaptResponse(hostPayload, upstreamBody), responseId)
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
                error is IllegalArgumentException -> "Host/model contract validation"
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
    ): String = postImpl(apiKey, hostPayload, model, onStage, null, false).first

    /** Opt-in stored Responses chain, still subject to the caller's Host gate. */
    fun postRetained(
        apiKey: String,
        hostPayload: String,
        model: String,
        previousResponseId: String? = null,
        onStage: (String) -> Unit = {},
    ): ResponseReceipt {
        val (text, id) = postImpl(apiKey, hostPayload, model, onStage,
            previousResponseId, true)
        return ResponseReceipt(text, requireNotNull(id))
    }

    private fun postImpl(
        apiKey: String,
        hostPayload: String,
        model: String,
        onStage: (String) -> Unit,
        previousResponseId: String?,
        retainForContinuation: Boolean,
    ): Pair<String, String?> {
        onStage("PREPARING")
        require(apiKey.startsWith("sk-") && apiKey.length >= 12) {
            "A personal OpenAI API key is required."
        }
        val wire = buildRequest(hostPayload, model, previousResponseId, retainForContinuation)
        // Debug-only opt-in: records request/answer bytes, NEVER Authorization.
        val traceCall = BotcAiWireTrace.begin(hostPayload, wire)
        val traceStart = System.nanoTime()
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
            BotcAiWireTrace.event(traceCall, "HTTP", "code=$status")
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
            val upstream = raw.toString("UTF-8")
            BotcAiWireTrace.payload(traceCall, "RAW_PROVIDER_RESPONSE", upstream)
            val parsed = if (retainForContinuation) {
                adaptRetainedResponse(hostPayload, upstream)
            } else null
            val adapted = parsed?.text ?: adaptResponse(hostPayload, upstream)
            BotcAiWireTrace.payload(traceCall, "PARSED_MODEL_OUTPUT", adapted)
            val elapsed = (System.nanoTime() - traceStart) / 1_000_000
            BotcAiWireTrace.event(traceCall, "DONE", "elapsedMs=$elapsed")
            return adapted to parsed?.responseId
        } catch (error: Throwable) {
            // Upstream exception text must NOT reach Logcat without review.
            BotcAiWireTrace.event(traceCall, "FAILURE", "transport_or_response")
            throw error
        } finally {
            connection.disconnect()
        }
    }
}
