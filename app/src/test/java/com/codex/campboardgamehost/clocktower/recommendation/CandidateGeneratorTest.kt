package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.PlanLegalityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupCandidateGeneratorTest {
    @Test
    fun `eight player fixture generates every legal combination`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val roles = TroubleBrewingFixtures.roleDefinitions()

        val plans = SetupCandidateGenerator.generatePlans(game, roles).toList()

        // 6 red herrings * (112 Investigator information options + Monk + Soldier) * 4 bluff sets.
        assertEquals(2_736, plans.size)
        assertTrue(plans.all { PlanLegalityValidator.validate(game, roles, it).isEmpty() })
    }

    @Test
    fun `fortune teller themself is generated as a red herring`() {
        val plans = SetupCandidateGenerator.generatePlans(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.roleDefinitions(),
        )

        assertTrue(
            plans.any { plan ->
                plan.decisions
                    .filterIsInstance<StorytellerDecision.RedHerring>()
                    .single()
                    .seat == 3
            },
        )
    }

    @Test
    fun `drunk investigator options cover minion roles and unordered seat pairs`() {
        val plans = SetupCandidateGenerator.generatePlans(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.roleDefinitions(),
        )

        assertTrue(
            plans.any { plan ->
                plan.decisions
                    .filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>()
                    .singleOrNull()
                    ?.let { it.shownMinion == RoleId("Poisoner") && it.candidateSeats == listOf(1, 4) } == true
            },
        )
    }

    @Test
    fun `full Trouble Brewing catalog exposes the expected search space`() {
        val count = SetupCandidateGenerator.generatePlans(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.fullRoleDefinitions(),
        ).count()

        assertEquals(117_810, count)
    }

    @Test
    fun `locked decisions reduce generation to matching combinations`() {
        val locked = listOf(
            StorytellerDecision.RedHerring(3),
            StorytellerDecision.DrunkInvestigatorInfo(RoleId("Poisoner"), listOf(1, 4)),
        )

        val plans = SetupCandidateGenerator.generatePlans(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.roleDefinitions(),
            locked,
        ).toList()

        assertEquals(4, plans.size)
        assertTrue(plans.all { plan -> locked.all { it in plan.decisions } })
    }
}
