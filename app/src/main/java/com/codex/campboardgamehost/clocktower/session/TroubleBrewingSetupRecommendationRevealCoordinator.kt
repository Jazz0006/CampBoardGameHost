package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService

internal class TroubleBrewingSetupRecommendationRevealCoordinator(
    private val prewarmer: TroubleBrewingSetupRecommendationPrewarmCoordinator,
) {
    fun onCommittedDeal(
        request: SetupCoordinationRequest,
        enterReveal: () -> Unit,
        launchBackground: ((() -> Unit) -> Unit),
    ) {
        enterReveal()
        launchBackground { prewarmer.prewarm(request) }
    }

    fun resultFor(request: SetupCoordinationRequest): SetupRecommendationService.ConstrainedResult =
        prewarmer.readyFor(request) ?: prewarmer.prewarm(request)
}
