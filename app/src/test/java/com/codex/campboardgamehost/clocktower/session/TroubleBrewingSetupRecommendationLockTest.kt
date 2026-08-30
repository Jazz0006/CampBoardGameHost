package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealAssignment
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupDealPlan
import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleBrewingSetupRecommendationLockTest {
    @Test
    fun `selector owned Drunk shown role becomes the only setup lock`() {
        val dealPlan = TroubleBrewingSetupDealPlan(
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

        val lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
            dealPlan = dealPlan,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertEquals(
            listOf(StorytellerDecision.DrunkShownRole(RoleId("Investigator"))),
            lockedDecisions,
        )
    }
}
