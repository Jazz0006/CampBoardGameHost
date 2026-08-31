package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupRecommendationShownIdentityOwnershipTest {
    @Test
    fun `recommendation consumes committed Investigator identity without recommending identity or legacy info`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()

        val plans = SetupRecommendationService.recommend(
            game = game,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(plans.isNotEmpty())
        assertTrue(plans.all { plan ->
            plan.decisions.none {
                it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
            }
        })
        val observations = plans.flatMap { it.observations }
        assertTrue(observations.isNotEmpty())
        assertTrue(observations.all { observation ->
            observation.perceivedRole == RoleId("Investigator") &&
                observation.reliability == ReliabilityState.DRUNK
        })
    }

    @Test
    fun `non Investigator committed identity is never replaced with Investigator recommendation`() {
        val template = TroubleBrewingFixtures.eightPlayerExample()
        val game = template.copy(
            players = template.players.map { player ->
                if (player.actualRole == RoleId("Drunk")) {
                    player.copy(shownRole = RoleId("Chef"))
                } else {
                    player
                }
            },
        )

        val plans = SetupRecommendationService.recommend(
            game = game,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(plans.isNotEmpty())
        assertTrue(plans.all { plan ->
            plan.decisions.none {
                it is StorytellerDecision.DrunkShownRole ||
                    it is StorytellerDecision.DrunkInvestigatorInfo
            }
        })
        assertTrue(plans.all { it.observations.isEmpty() })
    }

    @Test
    fun `shown identity cannot be supplied as a locked recommendation decision`() {
        val result = SetupRecommendationService.recommendConstrained(
            game = TroubleBrewingFixtures.eightPlayerExample(),
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            lockedDecisions = listOf(
                StorytellerDecision.DrunkShownRole(RoleId("Investigator")),
            ),
        )

        assertTrue(result.plans.isEmpty())
        assertEquals(
            listOf("shown-identity-is-committed-setup-fact"),
            result.failureCodes,
        )
    }
}
