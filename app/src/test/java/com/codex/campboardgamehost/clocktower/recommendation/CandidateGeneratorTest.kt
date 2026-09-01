package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.PlanLegalityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupCandidateGeneratorTest {
    @Test
    fun `active setup generation excludes recommendation owned Drunk identity and information`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val roles = TroubleBrewingFixtures.roleDefinitions()

        val plans = SetupCandidateGenerator.generatePlans(game, roles).toList()

        assertTrue(plans.isNotEmpty())
        assertTrue(plans.all { PlanLegalityValidator.validate(game, roles, it).isEmpty() })
        assertTrue(plans.all { plan ->
            plan.decisions.none {
                it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
            }
        })
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
    fun `legacy Drunk Investigator generator remains available only for compatibility`() {
        val candidates = SetupCandidateGenerator.generateDrunkCandidates(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.roleDefinitions(),
        )

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.any { candidate ->
            val outcome = candidate.outcome as SetupClueOutcome.DrunkShownRole
            outcome.investigatorInformation?.let { info ->
                info.shownMinion == RoleId("Poisoner") && info.candidateSeats == listOf(1, 4)
            } == true
        })
    }

    @Test
    fun `larger role catalog expands active setup space without reactivating legacy Drunk decisions`() {
        val basicCount = SetupCandidateGenerator.generatePlans(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.roleDefinitions(),
        ).count()
        val fullPlans = SetupCandidateGenerator.generatePlans(
            TroubleBrewingFixtures.eightPlayerExample(),
            TroubleBrewingFixtures.fullRoleDefinitions(),
        ).toList()

        assertTrue(fullPlans.size > basicCount)
        assertTrue(fullPlans.all { plan ->
            plan.decisions.none {
                it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
            }
        })
    }

    @Test
    fun `explicit legacy locked information is preserved for compatibility`() {
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
