package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRecommendationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderValidationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseValidatorV1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import javax.net.ssl.HttpsURLConnection

/**
 * PROD-1 transport adapter. The OpenAI credential lives only in a separate gateway.
 * A short-lived gateway access token is supplied interactively, never stored or compiled into the APK.
 */
internal data class StorytellerGlobalAdviceV1(
    val response: StorytellerProviderResponseV1,
    val globalStrategy: StorytellerGlobalStrategyV1?,
)

internal object ProductionDrunkAiGatewayV1 {
    private const val MAX_RESPONSE_BYTES = 65536

    private fun <T> field(field: SnapshotField<T>): Any = when (field) {
        is SnapshotField.Known -> field.value as Any
        SnapshotField.Uncommitted -> "UNCOMMITTED"
        SnapshotField.NotApplicable -> "NOT_APPLICABLE"
        SnapshotField.Unknown -> "UNKNOWN"
    }

    fun encode(request: StorytellerProviderRequestV1): String {
        require(request.identity.decisionTypeId == "drunk-assignment")
        val snapshot = (request.state as StorytellerProviderGameStateV1.TroubleBrewing).snapshot
        val seats = JSONArray()
        snapshot.grimoireSeats.forEach { seat ->
            seats.put(JSONObject()
                .put("seat", seat.seat)
                .put("shownRoleId", field(seat.shownRoleId))
                .put("actualRoleId", field(seat.actualRoleId))
                .put("alive", field(seat.alive))
                .put("poisoned", field(seat.poisoned)))
        }
        val candidates = JSONArray()
        request.legalCandidates.forEach { candidate ->
            val payload = candidate.payload as StorytellerProviderCandidatePayloadV1.DrunkAssignment
            candidates.put(JSONObject()
                .put("candidateId", candidate.candidateId)
                .put("seat", payload.seat)
                .put("shownRoleId", payload.shownRoleId))
        }
        val players = JSONArray()
        request.gameContext.players.forEach { player ->
            players.put(JSONObject()
                .put("seat", player.seat)
                .put("experienceLevel", player.experienceLevel.name)
                .put("claimedRoleIds", JSONArray(player.claimedRoleIds.map { it.value }))
                .put("pressureLevel", player.pressureLevel?.name ?: JSONObject.NULL))
        }
        return JSONObject()
            .put("schemaId", request.schemaId)
            .put("schemaVersion", request.schemaVersion)
            .put("identity", JSONObject()
                .put("gameId", request.identity.gameId)
                .put("scriptId", request.identity.scriptId)
                .put("decisionTypeId", request.identity.decisionTypeId)
                .put("decisionId", request.identity.decisionId))
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", request.sourceRevision.gameStateRevision)
                .put("playerInputRevision", request.sourceRevision.playerInputRevision))
            .put("state", JSONObject()
                .put("stage", snapshot.position.stage.name)
                .put("hasDrunk", field(snapshot.setupState.hasDrunk))
                .put("drunkAssignmentSeat", field(snapshot.setupState.drunkAssignmentSeat))
                .put("seats", seats))
            .put("legalCandidates", candidates)
            .put("playerContext", players)
            .put("historyCoverage", "NOT_AVAILABLE_AT_SETUP_PRECOMMIT")
            .put("strategicAnalysisStage", "WHOLE_GAME_PRECOMMIT_BEFORE_DRUNK")
            .put("strategicPlanningScope", "GLOBAL_ISSUE_FIRST_THEN_LEGAL_DECISION")
            .put("coordinationHorizon", request.coordinationHorizon.name)
            .toString()
    }

    fun decodeGlobal(raw: String, request: StorytellerProviderRequestV1): StorytellerGlobalAdviceV1 {
        val json = JSONObject(raw)
        val snapshot = (request.state as StorytellerProviderGameStateV1.TroubleBrewing).snapshot
        val seats = snapshot.grimoireSeats.map { it.seat }.toSet()
        val strategy = StorytellerGlobalStrategyV1.decode(json.getJSONObject("strategy"), seats)
        return StorytellerGlobalAdviceV1(decode(raw), strategy)
    }

    fun decode(raw: String): StorytellerProviderResponseV1 {
        val json = JSONObject(raw)
        require(json.getString("schemaId") == StorytellerProviderResponseV1.SCHEMA_ID)
        require(json.getInt("schemaVersion") == StorytellerProviderResponseV1.SCHEMA_VERSION)
        val revision = json.getJSONObject("sourceRevision")
        val alternatives = json.getJSONArray("alternatives")
        val recommendations = (0 until alternatives.length()).map { index ->
            val item = alternatives.getJSONObject(index)
            StorytellerProviderRecommendationV1(
                candidateId = item.getString("candidateId"),
                rationale = listOf(item.getString("rationale")),
            )
        }
        return StorytellerProviderResponseV1(
            decisionId = json.getString("decisionId"),
            sourceRevision = StorytellerProviderRevisionV1(
                gameStateRevision = revision.getLong("gameStateRevision"),
                playerInputRevision = revision.getLong("playerInputRevision"),
            ),
            outcome = StorytellerProviderOutcomeV1.Recommendation(
                primary = StorytellerProviderRecommendationV1(
                    candidateId = json.getString("primaryCandidateId"),
                    rationale = listOf(json.getString("rationale")),
                ),
                alternatives = recommendations,
            ),
            uncertainty = json.getJSONArray("uncertainty").let { array ->
                (0 until array.length()).map(array::getString)
            },
        )
    }

    /** Fail closed if the pending Host decision was replaced while the HTTP call was in flight. */
    fun validateCurrent(
        request: StorytellerProviderRequestV1,
        original: PendingDrunkAssignmentDecision,
        current: PendingDrunkAssignmentDecision?,
        response: StorytellerProviderResponseV1,
    ): StorytellerProviderValidationV1? {
        if (current !== original ||
            current.requestIdentity.requestId != request.identity.decisionId ||
            current.revision.gameStateRevision != request.sourceRevision.gameStateRevision ||
            current.revision.playerInputRevision != request.sourceRevision.playerInputRevision ||
            current.legalCandidates.map { it.candidateId } != request.legalCandidateIds
        ) return null
        return StorytellerProviderResponseValidatorV1.validate(request, response)
    }

    suspend fun recommend(
        endpoint: String,
        accessToken: String,
        request: StorytellerProviderRequestV1,
        directModel: String? = null,
    ): StorytellerGlobalAdviceV1 = decodeGlobal(
        post(endpoint, accessToken, encode(request), directModel), request,
    )

    /** Shared authenticated HTTPS transport for decisions and analysis-only strategy. */
    suspend fun post(
        endpoint: String,
        accessToken: String,
        payload: String,
        directModel: String? = null,
    ): String = withContext(Dispatchers.IO) {
        if (endpoint.trim() == PersonalDirectOpenAiV1.ENDPOINT) {
            return@withContext PersonalDirectOpenAiV1.post(
                accessToken, payload,
                requireNotNull(directModel) { "Personal direct mode requires a model ID." },
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
