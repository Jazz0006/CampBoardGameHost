package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingSetupRecommendationLockTest {
    @Test
    fun `selector owned Drunk shown role becomes the only setup lock`() {
        val dealPlan = investigatorDrunkDealPlan()

        val lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
            dealPlan = dealPlan,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertEquals(
            listOf(StorytellerDecision.DrunkShownRole(RoleId("Investigator"))),
            lockedDecisions,
        )
    }

    @Test
    fun `locked Investigator identity survives setup recommendation and keeps compatible Drunk information`() {
        val roles = TroubleBrewingFixtures.fullRoleDefinitions()
        val lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
            dealPlan = investigatorDrunkDealPlan(),
            roleDefinitions = roles,
        )

        val result = ClocktowerRecommendationCoordinator().recommendSetup(
            SetupCoordinationRequest(
                game = TroubleBrewingFixtures.eightPlayerExample(),
                roles = roles,
                lockedDecisions = lockedDecisions,
            ),
        )

        assertTrue(result.failureCodes.isEmpty())
        assertTrue(result.plans.isNotEmpty())
        result.plans.forEach { plan ->
            assertEquals(
                listOf(RoleId("Investigator")),
                plan.decisions
                    .filterIsInstance<StorytellerDecision.DrunkShownRole>()
                    .map { it.role },
            )
            assertEquals(
                1,
                plan.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().size,
            )
        }
    }

    private fun investigatorDrunkDealPlan() = TroubleBrewingSetupDealPlan(
        datasetId = "test-dataset",
        schemaVersion = 2,
        presetId = "test-preset",
        playerCount = 8,
        gameSeed = 4_001L,
        selectedDrunkShownRole = "investigator",
        assignments = listOf(
            TroubleBrewingSetupDealAssignment(
                seat = 1,
                playerName = "Player 1",
                actualRoleId = "drunk",
                shownRoleId = "investigator",
            ),
        ),
    )
}
