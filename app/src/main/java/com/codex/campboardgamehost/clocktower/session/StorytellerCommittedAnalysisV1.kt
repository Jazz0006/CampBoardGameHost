package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure informational checkpoint, separate from a Host PendingStorytellerDecision.
 * Rebuilds the entire confirmed role/seat view; never carries candidate IDs or grants authority.
 */
internal object StorytellerCommittedAnalysisV1 {
    private const val REQUEST_SCHEMA = "botc.storyteller-global-analysis-request"
    private const val RESPONSE_SCHEMA = "botc.storyteller-global-analysis-response"

    /**
     * Read current Host authority at response time. A plain Compose-derived
     * gameId captured by a setup click handler can predate the immediately
     * created session until the next composition pass.
     */
    fun isCurrentGame(snapshot: TroubleBrewingGameSnapshotV1, session: ClocktowerGameSession?): Boolean =
        session?.state?.gameId == snapshot.gameId

    private fun <T> field(value: SnapshotField<T>): Any = when (value) {
        is SnapshotField.Known -> value.value as Any
        SnapshotField.Uncommitted -> "UNCOMMITTED"
        SnapshotField.NotApplicable -> "NOT_APPLICABLE"
        SnapshotField.Unknown -> "UNKNOWN"
    }

    fun encode(
        snapshot: TroubleBrewingGameSnapshotV1,
        prior: StorytellerGlobalStrategyV1?,
        gameStateRevision: Long = 0,
        playerInputRevision: Long = 0,
    ): String {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_COMMITTED) {
            "Global post-deal analysis needs the canonical committed setup."
        }
        require(gameStateRevision >= 0 && playerInputRevision >= 0)
        val roster = snapshot.grimoireSeats.sortedBy { it.seat }
        require(roster.map { it.seat } == (1..roster.size).toList())
        require(roster.all { it.shownRoleId is SnapshotField.Known && it.actualRoleId is SnapshotField.Known })
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            .put("schemaVersion", 1)
            .put("analysisIdentity", JSONObject()
                .put("gameId", snapshot.gameId)
                .put("scriptId", snapshot.script.value)
                .put("analysisId", "committed-setup:" + snapshot.gameId))
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", gameStateRevision)
                .put("playerInputRevision", playerInputRevision))
            .put("state", JSONObject()
                .put("stage", "SETUP_COMMITTED")
                .put("hasDrunk", field(snapshot.setupState.hasDrunk))
                .put("drunkAssignmentSeat", field(snapshot.setupState.drunkAssignmentSeat))
                .put("seats", JSONArray().also { out ->
                    roster.forEach { seat ->
                        out.put(JSONObject()
                            .put("seat", seat.seat)
                            .put("shownRoleId", field(seat.shownRoleId))
                            .put("actualRoleId", field(seat.actualRoleId))
                            .put("alive", field(seat.alive))
                            .put("poisoned", field(seat.poisoned)))
                    }
                }))
            .put("historyCoverage", "SETUP_ONLY_NO_FUTURE_OBSERVATIONS")
            .put("priorStrategy", prior?.let { strategy ->
                JSONObject()
                    .put("sourceGameId", snapshot.gameId)
                    .put("gameStateRevision", gameStateRevision)
                    .put("strategy", strategy.toJson())
            } ?: JSONObject.NULL)
            .toString()
    }

    fun decode(
        raw: String,
        snapshot: TroubleBrewingGameSnapshotV1,
        expectedGameStateRevision: Long = 0,
        expectedPlayerInputRevision: Long = 0,
    ): StorytellerGlobalStrategyV1 {
        val json = JSONObject(raw)
        require(json.getString("schemaId") == RESPONSE_SCHEMA && json.getInt("schemaVersion") == 1)
        require(json.getString("analysisId") == "committed-setup:" + snapshot.gameId)
        val revision = json.getJSONObject("sourceRevision")
        require(revision.getLong("gameStateRevision") == expectedGameStateRevision &&
            revision.getLong("playerInputRevision") == expectedPlayerInputRevision) {
            "Stale global analysis response"
        }
        return StorytellerGlobalStrategyV1.decode(
            json.getJSONObject("strategy"), snapshot.grimoireSeats.map { it.seat }.toSet(),
        )
    }

    suspend fun analyse(
        endpoint: String,
        accessToken: String,
        snapshot: TroubleBrewingGameSnapshotV1,
        prior: StorytellerGlobalStrategyV1?,
        directModel: String? = null,
        gameStateRevision: Long = 0,
        playerInputRevision: Long = 0,
        onStage: (String) -> Unit = {},
    ): StorytellerGlobalStrategyV1 {
        onStage("BUILDING_HOST_CONTEXT")
        val request = encode(snapshot, prior, gameStateRevision, playerInputRevision)
        val response = ProductionDrunkAiGatewayV1.post(
            endpoint, accessToken, request, directModel, onStage,
        )
        onStage("VALIDATING_HOST_RESPONSE")
        return decode(response, snapshot, gameStateRevision, playerInputRevision)
    }
}
