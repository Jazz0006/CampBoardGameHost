package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService

internal class TroubleBrewingSetupRecommendationPrewarmCoordinator(
    private val build: (SetupCoordinationRequest) -> SetupRecommendationService.ConstrainedResult,
) {
    private var readyRequest: SetupCoordinationRequest? = null
    private var readyResult: SetupRecommendationService.ConstrainedResult? = null

    fun prewarm(request: SetupCoordinationRequest): SetupRecommendationService.ConstrainedResult {
        readyFor(request)?.let { return it }

        return build(request).also { result ->
            readyRequest = request
            readyResult = result
        }
    }

    fun readyFor(request: SetupCoordinationRequest): SetupRecommendationService.ConstrainedResult? =
        readyResult.takeIf { readyRequest == request }
}
