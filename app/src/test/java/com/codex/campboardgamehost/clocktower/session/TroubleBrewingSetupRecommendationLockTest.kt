package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
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
    fun `selector owned Drunk shown role is committed state not a recommendation lock`() {
        val lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
            dealPlan = investigatorDrunkDealPlan(),
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(lockedDecisions.isEmpty())
    }

    @Test
    fun `committed Investigator identity survives recommendation without recommendation ownership`() {
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
            assertTrue(plan.decisions.none {
                it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
            })
            assertTrue(plan.observations.any { observation ->
                observation.sourceSeat == 6 &&
                    observation.perceivedRole == RoleId("Investigator") &&
                    observation.reliability == ReliabilityState.DRUNK
            })
        }
    }

    @Test
    fun `non Drunk deal plan contributes no setup recommendation lock`() {
        val actualRoleIds = listOf(
            "chef",
            "empath",
            "fortuneteller",
            "undertaker",
            "virgin",
            "butler",
            "scarletwoman",
            "imp",
        )
        val dealPlan = TroubleBrewingSetupDealPlan(
            datasetId = "test-dataset",
            schemaVersion = 2,
            presetId = "test-non-drunk-preset",
            playerCount = actualRoleIds.size,
            gameSeed = 4_002L,
            selectedDrunkShownRole = null,
            assignments = actualRoleIds.mapIndexed { index, roleId ->
                TroubleBrewingSetupDealAssignment(
                    seat = index + 1,
                    playerName = "Player ${index + 1}",
                    actualRoleId = roleId,
                    shownRoleId = roleId,
                )
            },
        )

        assertTrue(
            TroubleBrewingSetupRecommendationLock.lockedDecisions(
                dealPlan = dealPlan,
                roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            ).isEmpty(),
        )
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
