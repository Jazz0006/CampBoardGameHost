package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCoordinationHorizonV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1

/** Direct RES-1 engine decision -> RES-2 neutral provider request materialization. */
internal object StorytellerProviderRequestFactoryV1 {
    fun fromDrunkAssignment(
        decision: PendingDrunkAssignmentDecision,
        snapshot: TroubleBrewingGameSnapshotV1,
        gameContext: StorytellerProviderGameContextV1 = StorytellerProviderGameContextV1.EMPTY,
        coordinationHorizon: StorytellerProviderCoordinationHorizonV1 =
            StorytellerProviderCoordinationHorizonV1.CURRENT_DECISION_ONLY,
    ): StorytellerProviderRequestV1 {
        require(decision.requestIdentity.gameId == snapshot.gameId) {
            "Drunk provider request must use the pending decision's canonical game."
        }
        val context = StorytellerProviderDecisionContextV1.DrunkAssignment
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                gameId = snapshot.gameId,
                scriptId = snapshot.script.value,
                decisionTypeId = context.decisionTypeId,
                decisionId = decision.requestIdentity.requestId,
            ),
            sourceRevision = decision.revision.toProviderRevision(),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = context,
            legalCandidates = decision.legalCandidates.map { candidate ->
                StorytellerProviderCandidateV1(
                    candidateId = candidate.candidateId,
                    payload = StorytellerProviderCandidatePayloadV1.DrunkAssignment(
                        seat = candidate.payload.seat,
                        shownRoleId = candidate.payload.shownRoleId,
                    ),
                )
            },
            gameContext = gameContext,
            coordinationHorizon = coordinationHorizon,
        )
    }

    fun fromPairInformation(
        decision: PendingPairInformationDecision,
        snapshot: TroubleBrewingGameSnapshotV1,
        gameContext: StorytellerProviderGameContextV1 = StorytellerProviderGameContextV1.EMPTY,
        coordinationHorizon: StorytellerProviderCoordinationHorizonV1 =
            StorytellerProviderCoordinationHorizonV1.CURRENT_DECISION_ONLY,
    ): StorytellerProviderRequestV1 {
        require(decision.requestIdentity.gameId == snapshot.gameId) {
            "Pair provider request must use the pending decision's canonical game."
        }
        val context = StorytellerProviderDecisionContextV1.FirstNightPairInformation(
            sourceSeat = decision.sourceSeat,
            abilityRole = decision.abilityRole,
            reliability = decision.reliability,
        )
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                gameId = snapshot.gameId,
                scriptId = snapshot.script.value,
                decisionTypeId = context.decisionTypeId,
                decisionId = decision.requestIdentity.requestId,
            ),
            sourceRevision = decision.revision.toProviderRevision(),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = context,
            legalCandidates = decision.legalCandidates.map { candidate ->
                StorytellerProviderCandidateV1(
                    candidateId = candidate.candidateId,
                    payload = StorytellerProviderCandidatePayloadV1.PairInformation(
                        shownRoleId = candidate.outcome.shownRole?.value,
                        candidateSeats = candidate.outcome.candidateSeats.toList(),
                        semanticTruth = candidate.semanticTruth,
                        registrations = candidate.registrations.toList(),
                    ),
                )
            },
            gameContext = gameContext,
            coordinationHorizon = coordinationHorizon,
        )
    }

    /** Adapts an existing Host scalar result domain into the common LLM interface. */
    fun fromScalarInformation(
        identity: StorytellerDecisionRequestIdentity,
        revision: StorytellerDecisionRevision,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
        kind: StorytellerProviderScalarKindV1,
        metric: String,
        subjectSeats: List<Int>,
        legalCandidates: List<StorytellerProviderCandidateV1>,
        snapshot: TroubleBrewingGameSnapshotV1,
        gameContext: StorytellerProviderGameContextV1,
    ): StorytellerProviderRequestV1 {
        require(identity.gameId == snapshot.gameId)
        val context = StorytellerProviderDecisionContextV1.ScalarInformation(
            sourceSeat, abilityRole, reliability, kind, metric, subjectSeats,
        )
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                snapshot.gameId, snapshot.script.value, context.decisionTypeId, identity.requestId,
            ),
            sourceRevision = revision.toProviderRevision(),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = context,
            legalCandidates = legalCandidates,
            gameContext = gameContext,
        )
    }

    fun fromMayorRedirect(
        decision: PendingMayorRedirectDecision,
        snapshot: TroubleBrewingGameSnapshotV1,
        gameContext: StorytellerProviderGameContextV1 = StorytellerProviderGameContextV1.EMPTY,
        coordinationHorizon: StorytellerProviderCoordinationHorizonV1 =
            StorytellerProviderCoordinationHorizonV1.CURRENT_DECISION_ONLY,
    ): StorytellerProviderRequestV1 {
        require(decision.requestIdentity.gameId == snapshot.gameId) {
            "Mayor provider request must use the pending decision's canonical game."
        }
        val context = StorytellerProviderDecisionContextV1.MayorRedirect(
            mayorSeat = decision.mayorSeat,
        )
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                gameId = snapshot.gameId,
                scriptId = snapshot.script.value,
                decisionTypeId = context.decisionTypeId,
                decisionId = decision.requestIdentity.requestId,
            ),
            sourceRevision = decision.revision.toProviderRevision(),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = context,
            legalCandidates = decision.pending.legalCandidates.map { candidate ->
                StorytellerProviderCandidateV1(
                    candidateId = candidate.candidateId,
                    payload = StorytellerProviderCandidatePayloadV1.SeatTarget(
                        seat = candidate.payload,
                    ),
                )
            },
            gameContext = gameContext,
            coordinationHorizon = coordinationHorizon,
        )
    }

    private fun StorytellerDecisionRevision.toProviderRevision() =
        StorytellerProviderRevisionV1(
            gameStateRevision = gameStateRevision,
            playerInputRevision = playerInputRevision,
        )
}
