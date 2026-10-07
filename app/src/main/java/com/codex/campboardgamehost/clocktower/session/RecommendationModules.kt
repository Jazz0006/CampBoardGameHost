package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext

internal class NightRecommendationModule {
    fun resolveNumberInformation(
        request: InformationResolutionRequest.Number,
    ): List<DecisionEvaluation<DynamicInformationOutcome.Number>> =
        DynamicCandidateGenerator.generateNumeric(request.context, request.generation)

    fun resolveInformation(request: InformationResolutionRequest): List<DecisionEvaluation<out DynamicInformationOutcome>> =
        when (request) {
            is InformationResolutionRequest.Number -> resolveNumberInformation(request)
            is InformationResolutionRequest.Category ->
                DynamicCandidateGenerator.generateCategorical(request.candidates, request.generation)
        }
}

internal sealed interface InformationResolutionRequest {
    val generation: DynamicGenerationContext

    data class Number(
        val context: UnreliableNumberContext,
        override val generation: DynamicGenerationContext,
    ) : InformationResolutionRequest

    data class Category(
        val candidates: List<UnreliableCategoricalCandidate>,
        override val generation: DynamicGenerationContext,
    ) : InformationResolutionRequest

}
