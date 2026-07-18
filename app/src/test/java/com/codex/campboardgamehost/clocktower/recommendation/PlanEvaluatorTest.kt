package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanEvaluatorTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val roles = TroubleBrewingFixtures.roleDefinitions()

    @Test
    fun `balanced profile prefers documented plan A over high conflict plan B`() {
        val planA = PlanEvaluator.evaluate(game, roles, plan(listOf(1, 4)), RecommendationProfiles.balanced)
        val planB = PlanEvaluator.evaluate(game, roles, plan(listOf(2, 3)), RecommendationProfiles.balanced)

        assertTrue(planA.totalScore > planB.totalScore)
        assertEquals(QualityTier.RECOMMENDED, planA.qualityTier)
    }

    @Test
    fun `aggressive profile prefers documented high conflict plan B over plan A`() {
        val planA = PlanEvaluator.evaluate(game, roles, plan(listOf(1, 4)), RecommendationProfiles.aggressive)
        val planB = PlanEvaluator.evaluate(game, roles, plan(listOf(2, 3)), RecommendationProfiles.aggressive)

        assertTrue(planB.totalScore > planA.totalScore)
    }

    @Test
    fun `drunk information that hits real evil is downgraded with warning`() {
        val evaluated = PlanEvaluator.evaluate(
            game,
            roles,
            plan(listOf(1, 7)),
            RecommendationProfiles.balanced,
        )

        assertEquals(QualityTier.ACCEPTABLE_WITH_WARNING, evaluated.qualityTier)
        assertTrue(evaluated.warnings.any { it.ruleId == "drunk-info-hits-real-evil" })
    }

    @Test
    fun `drunk observation records unreliable false information separately`() {
        val evaluated = PlanEvaluator.evaluate(
            game,
            roles,
            plan(listOf(1, 4)),
            RecommendationProfiles.balanced,
        )

        assertEquals(SemanticTruth.FALSE, evaluated.observations.single().semanticTruth)
        assertTrue(evaluated.scoreItems.isNotEmpty())
        assertEquals(evaluated.scoreItems.sumOf { it.delta }, evaluated.totalScore)
    }

    private fun plan(candidateSeats: List<Int>): CandidatePlan = CandidatePlan(
        decisions = listOf(
            StorytellerDecision.RedHerring(5),
            StorytellerDecision.DrunkShownRole(RoleId("Investigator")),
            StorytellerDecision.DrunkInvestigatorInfo(
                shownMinion = RoleId("Poisoner"),
                candidateSeats = candidateSeats,
            ),
            StorytellerDecision.DemonBluffs(
                listOf(RoleId("Investigator"), RoleId("Monk"), RoleId("Soldier")),
            ),
        ),
    )
}
