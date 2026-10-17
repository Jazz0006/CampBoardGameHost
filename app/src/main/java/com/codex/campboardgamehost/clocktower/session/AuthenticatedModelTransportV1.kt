package com.codex.campboardgamehost.clocktower.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import javax.net.ssl.HttpsURLConnection

/** Shared authenticated transport; legacy V1 paid decision policy is not retained. */
internal object AuthenticatedModelTransportV1 {
    private const val MAX_RESPONSE_BYTES = 65536

    /** Shared authenticated HTTPS transport for decisions and analysis-only strategy. */
    suspend fun post(
        endpoint: String,
        accessToken: String,
        payload: String,
        directModel: String? = null,
        onStage: (String) -> Unit = {},
    ): String = withContext(Dispatchers.IO) {
        if (endpoint.trim() == PersonalDirectOpenAiV1.ENDPOINT) {
            return@withContext PersonalDirectOpenAiV1.post(
                accessToken, payload,
                requireNotNull(directModel) { "Personal direct mode requires a model ID." },
                onStage,
            )
        }
        val uri = URI(endpoint.trim())
        require(uri.scheme == "https" && !uri.host.isNullOrBlank() &&
            uri.userInfo == null && uri.fragment == null && uri.rawQuery == null && uri.port != 0) {
            "Gateway must be an HTTPS endpoint without embedded credentials."
        }
        val url = uri.toURL()
        require(accessToken.isNotBlank()) { "Gateway access token is required." }
        val connection = (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = 10000
            readTimeout = 90000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer ${accessToken.trim()}")
        }
        try {
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode != 200) {
                throw IllegalStateException("Gateway unavailable (HTTP ${connection.responseCode}).")
            }
            val raw = connection.inputStream.use { stream ->
                val buffer = ByteArray(4096)
                val output = java.io.ByteArrayOutputStream()
                while (true) {
                    val count = stream.read(buffer)
                    if (count == -1) break
                    require(output.size() + count <= MAX_RESPONSE_BYTES) {
                        "Gateway response exceeds size limit."
                    }
                    output.write(buffer, 0, count)
                }
                output.toString("UTF-8")
            }
            raw
        } finally {
            connection.disconnect()
        }
    }
}
