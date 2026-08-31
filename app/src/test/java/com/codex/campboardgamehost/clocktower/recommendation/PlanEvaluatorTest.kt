package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupEvaluatorTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val roles = TroubleBrewingFixtures.roleDefinitions()

    @Test
    fun `legacy Investigator info that hits actual evil has no dedicated downgrade`() {
        val evaluated = SetupEvaluator.evaluate(
            game,
            roles,
            legacyPlan(listOf(1, 7)),
            RecommendationProfiles.balanced,
        )

        assertEquals(QualityTier.RECOMMENDED, evaluated.qualityTier)
        assertTrue(evaluated.warnings.none { it.ruleId == "drunk-info-hits-real-evil" })
        assertTrue(evaluated.scoreItems.none { it.ruleId == "drunk-info-hits-real-evil" })
    }

    @Test
    fun `committed Investigator identity produces generic Drunk observation without recommendation identity decision`() {
        val candidate = activePlan()
        val evaluated = SetupEvaluator.evaluate(
            game,
            roles,
            candidate,
            RecommendationProfiles.balanced,
        )

        assertTrue(candidate.decisions.none {
            it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
        })
        val observation = evaluated.observations.single()
        assertEquals(6, observation.sourceSeat)
        assertEquals(RoleId("Investigator"), observation.perceivedRole)
        assertEquals(ReliabilityState.DRUNK, observation.reliability)
        assertEquals(evaluated.scoreItems.sumOf { it.delta }, evaluated.totalScore)
    }

    @Test
    fun `committed non pair Drunk identity does not synthesize Investigator information`() {
        val committedMonk = game.copy(
            players = game.players.map { player ->
                if (player.actualRole == RoleId("Drunk")) player.copy(shownRole = RoleId("Monk")) else player
            },
        )
        val evaluated = SetupEvaluator.evaluate(
            committedMonk,
            TroubleBrewingFixtures.fullRoleDefinitions(),
            activePlan(),
            RecommendationProfiles.balanced,
        )

        assertEquals(QualityTier.RECOMMENDED, evaluated.qualityTier)
        assertTrue(evaluated.observations.isEmpty())
        assertTrue(evaluated.warnings.none { it.ruleId == "drunk-non-information-role" })
    }

    private fun legacyPlan(candidateSeats: List<Int>): CandidatePlan = CandidatePlan(
        decisions = listOf(
            StorytellerDecision.RedHerring(5),
            StorytellerDecision.DrunkInvestigatorInfo(
                shownMinion = RoleId("Poisoner"),
                candidateSeats = candidateSeats,
            ),
            StorytellerDecision.DemonBluffs(
                listOf(RoleId("Investigator"), RoleId("Monk"), RoleId("Soldier")),
            ),
        ),
    )

    private fun activePlan(): CandidatePlan = CandidatePlan(
        decisions = listOf(
            StorytellerDecision.RedHerring(5),
            StorytellerDecision.DemonBluffs(
                listOf(RoleId("Investigator"), RoleId("Monk"), RoleId("Soldier")),
            ),
        ),
    )
}
