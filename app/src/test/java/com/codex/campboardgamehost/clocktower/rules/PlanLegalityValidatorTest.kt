package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.ConstraintAuthority
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanLegalityValidatorTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val roles = TroubleBrewingFixtures.roleDefinitions()

    @Test
    fun `fortune teller may legally be the red herring`() {
        val failures = PlanLegalityValidator.validate(game, roles, validPlan(redHerringSeat = 3))

        assertTrue(failures.isEmpty())
    }

    @Test
    fun `evil player cannot be the red herring`() {
        val failures = PlanLegalityValidator.validate(game, roles, validPlan(redHerringSeat = 7))

        assertTrue(failures.any { it is LegalityFailure.EvilRedHerring })
    }

    @Test
    fun `drunk investigator candidates must be two distinct existing seats`() {
        val invalidInfo = StorytellerDecision.DrunkInvestigatorInfo(
            shownMinion = RoleId("Poisoner"),
            candidateSeats = listOf(1, 1),
        )
        val plan = validPlan().replaceDecision(invalidInfo)

        val failures = PlanLegalityValidator.validate(game, roles, plan)

        assertTrue(failures.any { it is LegalityFailure.DuplicateCandidateSeats })
    }

    @Test
    fun `demon bluff cannot be an actual in play role`() {
        val invalidBluffs = StorytellerDecision.DemonBluffs(
            listOf(RoleId("Chef"), RoleId("Monk"), RoleId("Soldier")),
        )
        val plan = validPlan().replaceDecision(invalidBluffs)

        val failures = PlanLegalityValidator.validate(game, roles, plan)

        assertTrue(failures.any { it is LegalityFailure.BluffIsInPlay })
    }

    @Test
    fun `roles outside the active script are rejected`() {
        val invalidInfo = StorytellerDecision.DrunkInvestigatorInfo(
            shownMinion = RoleId("Goblin"),
            candidateSeats = listOf(1, 4),
        )
        val plan = validPlan().replaceDecision(invalidInfo)

        val failures = PlanLegalityValidator.validate(game, roles, plan)

        assertTrue(failures.any { it is LegalityFailure.RoleOutsideScript })
    }

    @Test
    fun `official rule failures and product contract failures remain distinguishable`() {
        assertEquals(
            ConstraintAuthority.OFFICIAL_RULE_REQUIRED,
            LegalityFailure.EvilRedHerring(7).constraintAuthority,
        )
        assertEquals(
            ConstraintAuthority.PRODUCT_POLICY_REQUIRED,
            LegalityFailure.MissingSeat(99).constraintAuthority,
        )
        assertEquals(
            ConstraintAuthority.PRODUCT_POLICY_REQUIRED,
            LegalityFailure.MultipleDecisions("red-herring").constraintAuthority,
        )
    }

    private fun validPlan(redHerringSeat: Int = 5): CandidatePlan = CandidatePlan(
        decisions = listOf(
            StorytellerDecision.RedHerring(redHerringSeat),
            StorytellerDecision.DrunkShownRole(RoleId("Investigator")),
            StorytellerDecision.DrunkInvestigatorInfo(
                shownMinion = RoleId("Poisoner"),
                candidateSeats = listOf(1, 4),
            ),
            StorytellerDecision.DemonBluffs(
                listOf(RoleId("Investigator"), RoleId("Monk"), RoleId("Soldier")),
            ),
        ),
    )

    private inline fun <reified T : StorytellerDecision> CandidatePlan.replaceDecision(
        replacement: T,
    ): CandidatePlan = copy(
        decisions = decisions.filterNot { it is T } + replacement,
    )
}
