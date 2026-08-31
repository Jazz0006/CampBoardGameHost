package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupRecommendationServiceTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val roles = TroubleBrewingFixtures.roleDefinitions()

    @Test
    fun `search returns three safe and behaviorally different styles`() {
        val recommendations = SetupRecommendationService.recommend(game, roles)

        assertEquals(3, recommendations.size)
        assertEquals(
            setOf(RecommendationStyle.GENTLE, RecommendationStyle.BALANCED, RecommendationStyle.AGGRESSIVE),
            recommendations.map { it.style }.toSet(),
        )
        assertTrue(recommendations.all { it.qualityTier == QualityTier.RECOMMENDED })
        assertEquals(3, recommendations.map { it.effectSignature }.distinct().size)
    }

    @Test
    fun `same seed produces the same recommendations`() {
        val first = SetupRecommendationService.recommend(game, roles)
        val second = SetupRecommendationService.recommend(game, roles)

        assertEquals(first.map { it.decisions }, second.map { it.decisions })
    }

    @Test
    fun `same seed and same cross game history produce the same recommendations`() {
        val history = CrossGameHistory(
            listOf(HistoricalClueSignature("setup-plan", drunkShownRole = RoleId("Investigator"))),
        )

        val first = SetupRecommendationService.recommend(game, roles, history)
        val second = SetupRecommendationService.recommend(game, roles, history)

        assertEquals(first.map { it.decisions }, second.map { it.decisions })
    }

    @Test
    fun `ranked plans consume committed Investigator without legacy recommendation decisions`() {
        listOf(RecommendationProfiles.balanced, RecommendationProfiles.aggressive).forEach { profile ->
            val top = SetupRecommendationService.rankedPlans(game, roles, profile).first()

            assertTrue(top.decisions.none {
                it is StorytellerDecision.DrunkShownRole || it is StorytellerDecision.DrunkInvestigatorInfo
            })
            assertTrue(top.observations.any { observation ->
                observation.sourceSeat == 6 &&
                    observation.perceivedRole == RoleId("Investigator") &&
                    observation.reliability == ReliabilityState.DRUNK
            })
        }
    }

    @Test
    fun `bounded ranking handles the full Trouble Brewing candidate stream`() {
        val retained = SetupRecommendationService.rankedPlans(
            game = game,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            profile = RecommendationProfiles.balanced,
            maxResults = 16,
        )

        assertEquals(16, retained.size)
        assertTrue(retained.all { it.qualityTier == QualityTier.RECOMMENDED })
    }

    @Test
    fun `full Trouble Brewing catalog returns three diverse recommendations`() {
        val recommendations = SetupRecommendationService.recommend(
            game,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertEquals(3, recommendations.size)
        assertEquals(3, recommendations.map { it.effectSignature }.distinct().size)
    }

    @Test
    fun `constrained search preserves explicit legacy information lock for compatibility`() {
        val locked = listOf(
            StorytellerDecision.RedHerring(3),
            StorytellerDecision.DrunkInvestigatorInfo(RoleId("Poisoner"), listOf(1, 4)),
        )

        val result = SetupRecommendationService.recommendConstrained(game, roles, locked)

        assertTrue(result.failureCodes.isEmpty())
        assertTrue(result.plans.isNotEmpty())
        assertTrue(result.plans.all { plan -> locked.all { it in plan.decisions } })
    }

    @Test
    fun `illegal locked red herring is rejected instead of ignored`() {
        val result = SetupRecommendationService.recommendConstrained(
            game,
            roles,
            listOf(StorytellerDecision.RedHerring(7)),
        )

        assertTrue(result.plans.isEmpty())
        assertEquals(listOf("locked-decisions-illegal-or-incompatible"), result.failureCodes)
    }

    @Test
    fun `legacy Drunk information lock is incompatible with a different committed identity`() {
        val committedMonk = game.copy(
            players = game.players.map { player ->
                if (player.actualRole == RoleId("Drunk")) player.copy(shownRole = RoleId("Monk")) else player
            },
        )
        val result = SetupRecommendationService.recommendConstrained(
            committedMonk,
            roles,
            listOf(StorytellerDecision.DrunkInvestigatorInfo(RoleId("Poisoner"), listOf(1, 4))),
        )

        assertTrue(result.plans.isEmpty())
        assertEquals(listOf("locked-decisions-illegal-or-incompatible"), result.failureCodes)
    }
}
