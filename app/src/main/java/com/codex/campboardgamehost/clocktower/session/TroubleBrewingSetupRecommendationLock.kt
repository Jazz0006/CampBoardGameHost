package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealPlan

internal object TroubleBrewingSetupRecommendationLock {
    @Suppress("UNUSED_PARAMETER")
    fun lockedDecisions(
        dealPlan: TroubleBrewingSetupDealPlan,
        roleDefinitions: List<RoleDefinition>,
    ): List<StorytellerDecision> = emptyList()
}
