package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.CandidatePlan
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupMigrationTest {
    @Test
    fun `committed Librarian identity keeps Drunk information in setup recommendation`() {
        val template = TroubleBrewingFixtures.eightPlayerExample()
        val drunkSeat = template.players.single { it.actualRole == RoleId("Drunk") }.seat
        val game = template.copy(
            players = template.players.map { player ->
                if (player.seat == drunkSeat) {
                    player.copy(shownRole = RoleId("Librarian"))
                } else {
                    player
                }
            },
        )

        val plans = SetupRecommendationService.recommend(
            game,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(plans.isNotEmpty())
        assertTrue(plans.all { plan ->
            plan.observations.any { observation ->
                observation.sourceSeat == drunkSeat &&
                    observation.perceivedRole == RoleId("Librarian") &&
                    observation.reliability == ReliabilityState.DRUNK
            }
        })
    }

    @Test
    fun `impaired Investigator pair is not downgraded solely for containing actual Evil`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val context = SetupEvaluator.createContext(game)

        fun evaluate(candidateSeats: List<Int>) = SetupEvaluator.evaluateGenerated(
            context = context,
            candidate = CandidatePlan(
                decisions = listOf(
                    StorytellerDecision.DrunkInvestigatorInfo(
                        shownMinion = RoleId("Poisoner"),
                        candidateSeats = candidateSeats,
                    ),
                ),
            ),
            profile = RecommendationProfiles.balanced,
        )

        val goodOnlyPair = evaluate(listOf(4, 5))
        val actualEvilPair = evaluate(listOf(7, 8))

        assertEquals(goodOnlyPair.qualityTier, actualEvilPair.qualityTier)
    }

    @Test
    fun `red herring and demon bluffs have dedicated families and effects`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val redHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
        val bluffs = SetupCandidateGenerator.generateDemonBluffCandidates(game, TroubleBrewingFixtures.roleDefinitions())

        assertEquals(6, redHerrings.size)
        assertTrue(redHerrings.all { it.candidateFamilyId == "red-herring" })
        assertEquals(4, bluffs.size)
        assertTrue(bluffs.all {
            val effect = it.effects.single() as com.codex.campboardgamehost.clocktower.domain.EffectDraft.PlayerInformation
            (effect.value as InformationValue.RoleSet).roleIds.size == 3
        })
    }

    @Test
    fun `committed Investigator identity constrains every later setup recommendation`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val committedInformation = StorytellerDecision.DrunkInvestigatorInfo(
            shownMinion = RoleId("Poisoner"),
            candidateSeats = listOf(2, 4),
        )

        val plans = SetupRecommendationService.recommendConstrained(
            game = game,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            lockedDecisions = listOf(committedInformation),
        ).plans

        assertTrue(plans.isNotEmpty())
        assertTrue(plans.all { plan ->
            plan.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().single() == committedInformation
        })
    }
}
