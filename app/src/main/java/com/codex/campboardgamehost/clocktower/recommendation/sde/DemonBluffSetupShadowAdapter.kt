package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService

/**
 * Non-authoritative setup shadow result for SDE-2D2.
 *
 * [visibleResult] is deliberately the exact existing setup recommendation result object. The shadow
 * layer may compare its legacy bluff choices with SDE diagnostics, but it cannot rewrite plans,
 * scores, selection, commitment, or presentation.
 */
internal data class DemonBluffSetupShadowEvaluation(
    val visibleResult: SetupRecommendationService.ConstrainedResult,
    val legacyBluffCandidateIdByStyle: Map<RecommendationStyle, String>,
    val jointOutput: DemonBluffJointOutputEvaluation,
)

/**
 * Pure differential attachment between the current setup recommendation authority and SDE bluff
 * diagnostics.
 *
 * Candidate legality is supplied by the existing setup generator. Exact evaluation is supplied by
 * [TroubleBrewingDemonBluffJointOutputEvaluator]. This adapter only proves which already-visible
 * legacy bluff triplet corresponds to which legal candidate and attaches that comparison beside the
 * unchanged visible result.
 */
internal object DemonBluffSetupShadowAdapter {
    fun attach(
        visibleResult: SetupRecommendationService.ConstrainedResult,
        legalCandidates: List<DecisionCandidate<SetupClueOutcome>>,
        jointOutput: DemonBluffJointOutputEvaluation,
    ): DemonBluffSetupShadowEvaluation {
        val projected = SetupDemonBluffJointOutputAdapter.fromLegalCandidates(legalCandidates)
        require(projected.map(DemonBluffJointOutputCandidate::candidateId).distinct().size == projected.size) {
            "Setup-owned Demon bluff candidates must have unique candidate IDs."
        }

        val candidateIdByRoles = projected.associateBy(
            keySelector = { candidate -> canonicalBluffRoles(candidate.roles) },
            valueTransform = DemonBluffJointOutputCandidate::candidateId,
        )
        require(candidateIdByRoles.size == projected.size) {
            "Setup-owned Demon bluff candidates must have unique role triplets."
        }

        val plansWithBluffs = visibleResult.plans.mapNotNull { plan ->
            val bluff = plan.decisions.filterIsInstance<StorytellerDecision.DemonBluffs>().singleOrNull()
                ?: return@mapNotNull null
            plan.style to bluff
        }
        require(plansWithBluffs.map { it.first }.distinct().size == plansWithBluffs.size) {
            "Visible setup result may expose at most one plan per recommendation style."
        }
        val legacyByStyle = plansWithBluffs.associate { (style, bluff) ->
            val candidateId = candidateIdByRoles[canonicalBluffRoles(bluff.roles)]
                ?: error(
                    "Visible setup recommendation selected a Demon bluff triplet that is absent " +
                        "from the setup-owned legal candidate domain.",
                )
            style to candidateId
        }

        if (jointOutput is DemonBluffJointOutputEvaluation.Ready) {
            val diagnosticCandidateIds =
                jointOutput.candidates.mapTo(linkedSetOf(), DemonBluffJointOutputDiagnostics::candidateId)
            require(legacyByStyle.values.all(diagnosticCandidateIds::contains)) {
                "Every visible legacy Demon bluff choice must be represented in SDE shadow diagnostics."
            }
        }

        return DemonBluffSetupShadowEvaluation(
            visibleResult = visibleResult,
            legacyBluffCandidateIdByStyle = legacyByStyle,
            jointOutput = jointOutput,
        )
    }

    private fun canonicalBluffRoles(
        roles: List<com.codex.campboardgamehost.clocktower.domain.RoleId>,
    ): List<com.codex.campboardgamehost.clocktower.domain.RoleId> =
        roles.sortedBy { it.value }
}
