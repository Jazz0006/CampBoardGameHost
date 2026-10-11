package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import org.json.JSONArray
import org.json.JSONObject

/**
 * One paid joint first-night recommendation, no diagnostic prose in the result.
 * The model sees ALL seats, ALL current Host-legal candidates, and every
 * not-yet-knowable dependency in the same prompt.
 */
internal object FirstNightOneShotRequestV1 {
    const val REQUEST_SCHEMA = "botc.first-night-one-shot-request"

    private fun <T> field(value: SnapshotField<T>): Any = when (value) {
        is SnapshotField.Known<*> -> value.value as Any
        SnapshotField.Uncommitted -> "UNCOMMITTED"
        SnapshotField.NotApplicable -> "NOT_APPLICABLE"
        SnapshotField.Unknown -> "UNKNOWN"
    }

    fun encode(
        snapshot: TroubleBrewingGameSnapshotV1,
        scope: FirstNightOneShotScopeV1,
    ): String {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_COMMITTED)
        require(scope.gameId == snapshot.gameId)
        val seats = snapshot.grimoireSeats.sortedBy { it.seat }
        require(seats.map { it.seat } == (1..seats.size).toList())
        require(seats.all { it.shownRoleId is SnapshotField.Known<*> &&
            it.actualRoleId is SnapshotField.Known<*> })
        return JSONObject()
            .put("schemaId", REQUEST_SCHEMA)
            .put("schemaVersion", 1)
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", scope.gameStateRevision)
                .put("playerInputRevision", scope.playerInputRevision))
            .put("state", JSONObject()
                .put("scriptId", snapshot.script.value)
                .put("gameId", snapshot.gameId)
                .put("seats", JSONArray().also { array ->
                    seats.forEach { seat ->
                        array.put(JSONObject()
                            .put("seat", seat.seat)
                            .put("actualRoleId", field(seat.actualRoleId))
                            .put("shownRoleId", field(seat.shownRoleId))
                            .put("alive", field(seat.alive)))
                    }
                })
                .put("drunkAssignmentSeat", field(snapshot.setupState.drunkAssignmentSeat)))
            .put("legalScope", scope.providerLegalScope())
            .put("unknownFuturePlayerActionsAreNotFacts", true)
            .put("outputProfile", "ONE_COMPLETE_LEGAL_OPENING_NO_PROSE")
            .toString()
    }

    fun decode(raw: String, scope: FirstNightOneShotScopeV1): FirstNightOneShotPlanV1 =
        FirstNightOneShotPlanV1.decode(JSONObject(raw), scope)

    suspend fun recommend(
        endpoint: String,
        accessToken: String,
        snapshot: TroubleBrewingGameSnapshotV1,
        scope: FirstNightOneShotScopeV1,
        model: String,
        onStage: (String) -> Unit = {},
    ): FirstNightOneShotPlanV1 {
        // Gateway adapter has not yet been upgraded to this schema. Never send
        // an unsupported request to it and then reinterpret an unrelated reply.
        require(endpoint == PersonalDirectOpenAiV1.ENDPOINT) {
            "One-shot first-night experiment currently requires personal direct OpenAI"
        }
        onStage("BUILDING_HOST_CONTEXT")
        val request = encode(snapshot, scope)
        val response = ProductionDrunkAiGatewayV1.post(
            endpoint, accessToken, request, model, onStage,
        )
        onStage("VALIDATING_HOST_RESPONSE")
        return decode(response, scope)
    }
}
