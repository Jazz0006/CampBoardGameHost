package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderConfidenceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCoordinationHorizonV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPlayerContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRecommendationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1

/**
 * RES-2 one-way compatibility adapter.
 *
 * Existing HOST-ML/SDE exports may feed the neutral provider boundary while they are still useful
 * for benchmark/replay. No neutral contract type depends back on these legacy export structures.
 */
internal object StorytellerProviderContractAdapterV1 {
    fun fromLegacy(
        request: StorytellerPolicyRequestV1,
        gameContext: StorytellerPolicyGameContextV1? = null,
    ): StorytellerProviderRequestV1 {
        val input = request.input
        val decisionContext = input.context.toNeutralDecisionContext()
        val candidates = input.context.toNeutralCandidates()

        require(candidates.map { it.candidateId } == input.legalCandidateIds) {
            "Legacy provider adapter must preserve the complete legal-candidate order."
        }

        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                gameId = input.snapshot.gameId,
                scriptId = input.snapshot.script.value,
                decisionTypeId = decisionContext.decisionTypeId,
                decisionId = input.decisionId,
            ),
            sourceRevision = input.sourceRevision.toNeutralRevision(),
            state = StorytellerProviderGameStateV1.TroubleBrewing(input.snapshot),
            decisionContext = decisionContext,
            legalCandidates = candidates,
            gameContext = gameContext?.toNeutralGameContext() ?: StorytellerProviderGameContextV1.EMPTY,
            coordinationHorizon = when (request.coordinationHorizon) {
                CoordinationHorizonV1.CURRENT_DECISION_ONLY ->
                    StorytellerProviderCoordinationHorizonV1.CURRENT_DECISION_ONLY
                CoordinationHorizonV1.BOUNDED_DOWNSTREAM_DECISIONS ->
                    StorytellerProviderCoordinationHorizonV1.BOUNDED_DOWNSTREAM_DECISIONS
            },
        )
    }

    fun fromLegacy(response: StorytellerPolicyResponseV1): StorytellerProviderResponseV1 =
        StorytellerProviderResponseV1(
            decisionId = response.decisionId,
            sourceRevision = response.sourceRevision.toNeutralRevision(),
            outcome = when (val outcome = response.outcome) {
                is StorytellerPolicyOutcomeV1.Recommendation ->
                    StorytellerProviderOutcomeV1.Recommendation(
                        primary = outcome.primary.toNeutralRecommendation(),
                        alternatives = outcome.alternatives.map { it.toNeutralRecommendation() },
                    )

                is StorytellerPolicyOutcomeV1.Deferred ->
                    StorytellerProviderOutcomeV1.Deferred(
                        reasons = outcome.reasons.toList(),
                        missingContext = outcome.missingContext.toList(),
                    )
            },
            confidence = when (response.confidence) {
                StorytellerPolicyConfidenceV1.LOW -> StorytellerProviderConfidenceV1.LOW
                StorytellerPolicyConfidenceV1.MEDIUM -> StorytellerProviderConfidenceV1.MEDIUM
                StorytellerPolicyConfidenceV1.HIGH -> StorytellerProviderConfidenceV1.HIGH
                StorytellerPolicyConfidenceV1.UNSPECIFIED -> StorytellerProviderConfidenceV1.UNSPECIFIED
            },
            uncertainty = response.uncertainty.toList(),
            providerProvenance = response.providerProvenance,
        )

    private fun RecommendationDecisionContextV1.toNeutralDecisionContext(): StorytellerProviderDecisionContextV1 =
        when (this) {
            is RecommendationDecisionContextV1.DrunkAssignment ->
                StorytellerProviderDecisionContextV1.DrunkAssignment

            is RecommendationDecisionContextV1.FirstNightPairInformation ->
                StorytellerProviderDecisionContextV1.FirstNightPairInformation(
                    sourceSeat = sourceSeat,
                    abilityRole = abilityRole,
                    reliability = reliability,
                )
        }

    private fun RecommendationDecisionContextV1.toNeutralCandidates(): List<StorytellerProviderCandidateV1> =
        when (this) {
            is RecommendationDecisionContextV1.DrunkAssignment ->
                legalCandidates.map { candidate ->
                    StorytellerProviderCandidateV1(
                        candidateId = candidate.candidateId,
                        payload = StorytellerProviderCandidatePayloadV1.DrunkAssignment(
                            seat = candidate.seat,
                            shownRoleId = candidate.shownRoleId,
                        ),
                    )
                }

            is RecommendationDecisionContextV1.FirstNightPairInformation ->
                legalCandidates.map { candidate ->
                    StorytellerProviderCandidateV1(
                        candidateId = candidate.candidateId,
                        payload = StorytellerProviderCandidatePayloadV1.PairInformation(
                            shownRoleId = candidate.shownRoleId,
                            candidateSeats = candidate.candidateSeats.toList(),
                            semanticTruth = candidate.semanticTruth,
                            registrations = candidate.registrations.toList(),
                        ),
                    )
                }
        }

    private fun StorytellerPolicyGameContextV1.toNeutralGameContext(): StorytellerProviderGameContextV1 =
        StorytellerProviderGameContextV1(
            players = players.map { player ->
                StorytellerProviderPlayerContextV1(
                    seat = player.seat,
                    experienceLevel = player.experienceLevel,
                    claimedRoleIds = player.claimedRoleIds.toList(),
                    pressureLevel = player.pressureLevel,
                )
            },
            priorDecisions = priorDecisions.map { decision ->
                StorytellerProviderPriorDecisionV1(
                    eventId = decision.eventId,
                    gameStateRevision = decision.gameStateRevision,
                    playerInputRevision = decision.playerInputRevision,
                    selectedCandidateId = decision.selectedCandidateId,
                    selectedOutcome = decision.selectedOutcome,
                    abilityState = decision.abilityState,
                    truthRelation = decision.truthRelation,
                    registrations = decision.registrations.toList(),
                )
            },
        )

    private fun StorytellerPolicyRecommendationV1.toNeutralRecommendation() =
        StorytellerProviderRecommendationV1(
            candidateId = candidateId,
            rationale = rationale.toList(),
            tradeoffs = tradeoffs.toList(),
            risks = risks.toList(),
        )

    private fun com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision.toNeutralRevision() =
        StorytellerProviderRevisionV1(
            gameStateRevision = gameStateRevision,
            playerInputRevision = playerInputRevision,
        )
}
