package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseValidatorV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderValidationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import org.json.JSONArray
import org.json.JSONObject

/**
 * Event-driven, current-decision adapter. The role is merely the engine's legal-decision
 * context; selection policy is ALWAYS a whole-game assessment against its causal prefix.
 * This initial bridge covers an existing first-night pair-information pending decision.
 */
internal object StorytellerGlobalDecisionRequestV1 {
    private fun <T> field(value: SnapshotField<T>): Any = when (value) {
        is SnapshotField.Known<*> -> value.value as Any
        SnapshotField.Uncommitted -> "UNCOMMITTED"
        SnapshotField.NotApplicable -> "NOT_APPLICABLE"
        SnapshotField.Unknown -> "UNKNOWN"
    }

    fun encode(
        request: StorytellerProviderRequestV1,
        priorStrategy: StorytellerGlobalStrategyV1?,
    ): String {
        val context = request.decisionContext as? StorytellerProviderDecisionContextV1.FirstNightPairInformation
            ?: error("This bounded production bridge needs an engine-owned first-night pair decision.")
        val snapshot = (request.state as StorytellerProviderGameStateV1.TroubleBrewing).snapshot
        val prefix = requireNotNull(request.gameContext.historyPrefix) {
            "Global continuation requires a frozen Host causal prefix."
        }
        require(prefix.cutoffSource == StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED) {
            "Cannot recommend from unavailable or reconstructed chronology."
        }
        require(snapshot.grimoireSeats.isNotEmpty() &&
            snapshot.grimoireSeats.all { it.actualRoleId is SnapshotField.Known &&
                it.shownRoleId is SnapshotField.Known }) {
            "Global continuation requires confirmed shown and actual roles."
        }
        require(request.gameContext.players.map { it.seat } ==
            snapshot.grimoireSeats.map { it.seat }) {
            "Global continuation requires complete current player context."
        }
        return JSONObject()
            .put("schemaId", request.schemaId).put("schemaVersion", request.schemaVersion)
            .put("identity", JSONObject().put("gameId", request.identity.gameId)
                .put("scriptId", request.identity.scriptId)
                .put("decisionTypeId", request.identity.decisionTypeId)
                .put("decisionId", request.identity.decisionId))
            .put("sourceRevision", JSONObject()
                .put("gameStateRevision", request.sourceRevision.gameStateRevision)
                .put("playerInputRevision", request.sourceRevision.playerInputRevision))
            .put("state", JSONObject()
                .put("stage", snapshot.position.stage.name)
                .put("round", field(snapshot.position.round))
                .put("phase", field(snapshot.position.phase))
                .put("hasDrunk", field(snapshot.setupState.hasDrunk))
                .put("drunkAssignmentSeat", field(snapshot.setupState.drunkAssignmentSeat))
                .put("seats", JSONArray().also { array ->
                    snapshot.grimoireSeats.sortedBy { it.seat }.forEach { seat ->
                        array.put(JSONObject().put("seat", seat.seat)
                            .put("actualRoleId", field(seat.actualRoleId))
                            .put("shownRoleId", field(seat.shownRoleId))
                            .put("alive", field(seat.alive))
                            .put("poisoned", field(seat.poisoned)))
                    }
                }))
            .put("decisionContext", JSONObject()
                .put("sourceSeat", context.sourceSeat)
                .put("abilityRoleId", context.abilityRole.value)
                .put("reliability", context.reliability.name))
            .put("legalCandidates", JSONArray().also { array ->
                request.legalCandidates.forEach { candidate ->
                    val payload = candidate.payload as StorytellerProviderCandidatePayloadV1.PairInformation
                    array.put(JSONObject()
                        .put("candidateId", candidate.candidateId)
                        .put("shownRoleId", payload.shownRoleId ?: JSONObject.NULL)
                        .put("candidateSeats", JSONArray(payload.candidateSeats))
                        .put("semanticTruth", payload.semanticTruth.name)
                        // Witness possibilities never assert one uniquely committed registration.
                        .put("registrationWitnessesArePossibilities", true))
                }
            })
            .put("playerContext", JSONArray().also { array ->
                request.gameContext.players.forEach { player ->
                    array.put(JSONObject()
                        .put("seat", player.seat)
                        .put("experienceLevel", player.experienceLevel.name)
                        .put("claimedRoleIds", JSONArray(player.claimedRoleIds.map { it.value }))
                        .put("pressureLevel", player.pressureLevel?.name ?: JSONObject.NULL))
                }
            })
            .put("causalHistory", StorytellerGlobalEventProjectionV1.encode(prefix))
            .put("priorStrategy", priorStrategy?.toJson() ?: JSONObject.NULL)
            .put("strategicPlanningScope", "GLOBAL_EVENT_DRIVEN_CONTINUATION")
            .put("coordinationHorizon", request.coordinationHorizon.name)
            .toString()
    }

    /** Freshness includes the GLOBAL cursor, not just game/player revision counters. */
    fun validateCurrent(
        request: StorytellerProviderRequestV1,
        original: PendingPairInformationDecision,
        current: PendingPairInformationDecision?,
        latestSessionState: ClocktowerSessionState,
        response: StorytellerProviderResponseV1,
    ): StorytellerProviderValidationV1? {
        val expected = request.sourceRevision
        if (current == null ||
            current.requestIdentity != original.requestIdentity ||
            current.revision != original.revision ||
            current.legalCandidates.map { it.candidateId } != request.legalCandidateIds ||
            current.requestIdentity.requestId != request.identity.decisionId ||
            current.requestIdentity.gameId != latestSessionState.gameId ||
            StorytellerProviderRevisionV1(latestSessionState.gameStateRevision,
                latestSessionState.playerInputRevision) != expected ||
            current.revision.gameStateRevision != expected.gameStateRevision ||
            current.revision.playerInputRevision != expected.playerInputRevision ||
            request.gameContext.historyPrefix?.exclusiveGlobalSequence != latestSessionState.nextTimelineGlobalSequence
        ) return null
        return StorytellerProviderResponseValidatorV1.validate(request, response)
    }

    suspend fun recommend(
        endpoint: String,
        accessToken: String,
        request: StorytellerProviderRequestV1,
        priorStrategy: StorytellerGlobalStrategyV1?,
    ): StorytellerGlobalAdviceV1 {
        val raw = ProductionDrunkAiGatewayV1.post(
            endpoint, accessToken, encode(request, priorStrategy),
        )
        val strategy = StorytellerGlobalStrategyV1.decode(
            JSONObject(raw).getJSONObject("strategy"),
            (request.state as StorytellerProviderGameStateV1.TroubleBrewing)
                .snapshot.grimoireSeats.map { it.seat }.toSet(),
        )
        return StorytellerGlobalAdviceV1(
            response = ProductionDrunkAiGatewayV1.decode(raw),
            globalStrategy = strategy,
        )
    }
}
