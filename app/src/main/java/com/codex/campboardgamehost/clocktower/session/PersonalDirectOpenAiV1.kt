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

    fun buildRequest(hostPayload: String, model: String): String {
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
            .put("instructions", instructions)
            .put("input", host.toString())
            .put("store", false)
            .put("text", JSONObject().put("format", JSONObject()
                .put("type", "json_schema")
                .put("name", name)
                .put("strict", true).put("schema", schema)))
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
    ): String {
        onStage("PREPARING")
        require(apiKey.startsWith("sk-") && apiKey.length >= 12) {
            "A personal OpenAI API key is required."
        }
        val wire = buildRequest(hostPayload, model)
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
            val adapted = adaptResponse(hostPayload, upstream)
            BotcAiWireTrace.payload(traceCall, "PARSED_MODEL_OUTPUT", adapted)
            val elapsed = (System.nanoTime() - traceStart) / 1_000_000
            BotcAiWireTrace.event(traceCall, "DONE", "elapsedMs=$elapsed")
            return adapted
        } catch (error: Throwable) {
            // Upstream exception text must NOT reach Logcat without review.
            BotcAiWireTrace.event(traceCall, "FAILURE", "transport_or_response")
            throw error
        } finally {
            connection.disconnect()
        }
    }
}
