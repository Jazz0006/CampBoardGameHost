package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealPlan

internal object TroubleBrewingSetupRecommendationLock {
    fun lockedDecisions(
        dealPlan: TroubleBrewingSetupDealPlan,
        roleDefinitions: List<RoleDefinition>,
    ): List<StorytellerDecision> {
        val shownExternalId = dealPlan.selectedDrunkShownRole ?: return emptyList()
        val matchingRoles = roleDefinitions.filter { definition ->
            definition.id.value
                .filter(Char::isLetterOrDigit)
                .lowercase() == shownExternalId
        }
        require(matchingRoles.size == 1) {
            "Selected Trouble Brewing Drunk shown role '$shownExternalId' must resolve uniquely."
        }
        return listOf(
            StorytellerDecision.DrunkShownRole(matchingRoles.single().id),
        )
    }
}
