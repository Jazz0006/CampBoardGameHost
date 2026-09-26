package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService

/**
 * Non-authoritative setup shadow for Red-Herring truth/credibility diagnostics.
 *
 * The visible setup recommendation result remains owned by the legacy setup recommender and is
 * returned unchanged. This shadow only attaches typed SDE evidence beside that result.
 */
internal data class RedHerringSetupShadowEvaluation(
    val visibleResult: SetupRecommendationService.ConstrainedResult,
    val projection: RedHerringSetupPrecommitProjection,
    val legacyRedHerringCandidateIdByStyle: Map<RecommendationStyle, String>,
    val featureEvaluation: DecisionFeatureEvaluation,
)

/**
 * Pure differential attachment for setup-precommit Red-Herring diagnostics.
 *
 * Candidate legality and target identity come from [RedHerringSetupPrecommitProjection]. Exact
 * truth-danger consequence comes from [ExactTruthDangerSourceProjector]. This adapter owns neither
 * setup selection nor commitment and never rewrites the visible setup result.
 */
internal object RedHerringSetupShadowAdapter {
    fun evaluate(
        visibleResult: SetupRecommendationService.ConstrainedResult,
        projection: RedHerringSetupPrecommitProjection,
        validatedRuleset: ValidatedClocktowerRuleset,
        exactContext: ExactHistoricalHypotheticalContext,
    ): RedHerringSetupShadowEvaluation {
        require(projection.candidates.isNotEmpty()) {
            "Red-Herring setup shadow requires an uncommitted legal candidate domain."
        }

        val candidateIdBySeat = projection.candidates.associate { candidate ->
            candidate.targetSeat to candidate.sdeCandidate.candidateId
        }
        require(candidateIdBySeat.size == projection.candidates.size) {
            "Red-Herring setup candidates must target unique seats."
        }

        val visibleWithRedHerring = visibleResult.plans.mapNotNull { plan ->
            val decision = plan.decisions
                .filterIsInstance<StorytellerDecision.RedHerring>()
                .singleOrNull()
                ?: return@mapNotNull null
            plan.style to decision
        }
        require(visibleWithRedHerring.map { it.first }.distinct().size == visibleWithRedHerring.size) {
            "Visible setup result may expose at most one plan per recommendation style."
        }
        val legacyByStyle = visibleWithRedHerring.associate { (style, decision) ->
            val candidateId = candidateIdBySeat[decision.seat]
                ?: error(
                    "Visible setup recommendation selected a Red-Herring target that is absent " +
                        "from the setup-owned legal candidate domain.",
                )
            style to candidateId
        }

        val truthDanger = ExactTruthDangerSourceProjector.evaluate(
            validatedRuleset = validatedRuleset,
            context = exactContext,
            claims = projection.ruleDeterminedHealthySourceClaims,
        )
        val legalCandidateIds = projection.candidates.map { it.sdeCandidate.candidateId }
        val featureEvaluation = when (truthDanger) {
            is ExactTruthDangerSourceProjection.Deferred ->
                DecisionFeatureEvaluation.Deferred(
                    candidateIds = legalCandidateIds,
                    missingCapabilities = truthDanger.missingCapabilities,
                )

            is ExactTruthDangerSourceProjection.Ready -> {
                val featuresByCandidateId = TruthCredibilityFeaturesProjector.project(
                    projection.candidates.map { candidate ->
                        TruthCredibilityCandidateEvidence(
                            candidateId = candidate.sdeCandidate.candidateId,
                            truthDangerSources = truthDanger.impacts.toList(),
                            credibilityDisruptions = candidate.credibilityDisruptions.toList(),
                            unresolvedSourceRefs = projection.unresolvedHealthySourceRefs,
                        )
                    },
                )
                DecisionFeatureEvaluation.Ready(
                    candidates = projection.candidates.map { candidate ->
                        val candidateId = candidate.sdeCandidate.candidateId
                        CandidateDecisionFeatures(
                            candidateId = candidateId,
                            features = DecisionFeatures(
                                strategic = FeatureProjection.Unavailable(
                                    FeatureUnavailableReason.NOT_PROJECTED_YET,
                                ),
                                truthCredibility = FeatureProjection.Projected(
                                    featuresByCandidateId.getValue(candidateId),
                                ),
                            ),
                        )
                    },
                )
            }
        }

        if (featureEvaluation is DecisionFeatureEvaluation.Ready) {
            val diagnosticCandidateIds = featureEvaluation.candidateIds.toSet()
            require(legacyByStyle.values.all(diagnosticCandidateIds::contains)) {
                "Every visible legacy Red-Herring choice must be represented in SDE shadow diagnostics."
            }
        }

        return RedHerringSetupShadowEvaluation(
            visibleResult = visibleResult,
            projection = projection,
            legacyRedHerringCandidateIdByStyle = legacyByStyle,
            featureEvaluation = featureEvaluation,
        )
    }
}