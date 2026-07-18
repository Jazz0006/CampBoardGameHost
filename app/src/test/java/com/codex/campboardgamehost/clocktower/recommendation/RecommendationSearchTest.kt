package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationSearchTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val roles = TroubleBrewingFixtures.roleDefinitions()

    @Test
    fun `search returns three safe and behaviorally different styles`() {
        val recommendations = RecommendationSearch.recommend(game, roles)

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
        val first = RecommendationSearch.recommend(game, roles)
        val second = RecommendationSearch.recommend(game, roles)

        assertEquals(first.map { it.decisions }, second.map { it.decisions })
    }

    @Test
    fun `balanced ranking selects the documented plan A structure`() {
        val top = RecommendationSearch
            .rankedPlans(game, roles, RecommendationProfiles.balanced)
            .first()
        val redHerring = top.decisions.filterIsInstance<StorytellerDecision.RedHerring>().single()
        val shownRole = top.decisions.filterIsInstance<StorytellerDecision.DrunkShownRole>().single()
        val info = top.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().single()

        assertEquals(5, redHerring.seat)
        assertEquals(RoleId("Investigator"), shownRole.role)
        assertEquals(RoleId("Poisoner"), info.shownMinion)
        assertEquals(listOf(1, 4), info.candidateSeats)
    }

    @Test
    fun `aggressive ranking selects the documented high conflict pair`() {
        val top = RecommendationSearch
            .rankedPlans(game, roles, RecommendationProfiles.aggressive)
            .first()
        val info = top.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().single()

        assertEquals(listOf(2, 3), info.candidateSeats)
    }

    @Test
    fun `bounded ranking handles the full Trouble Brewing candidate stream`() {
        val retained = RecommendationSearch.rankedPlans(
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
        val recommendations = RecommendationSearch.recommend(
            game,
            TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertEquals(3, recommendations.size)
        assertEquals(3, recommendations.map { it.effectSignature }.distinct().size)
    }

    @Test
    fun `constrained search preserves every locked decision`() {
        val locked = listOf(
            StorytellerDecision.RedHerring(3),
            StorytellerDecision.DrunkShownRole(RoleId("Investigator")),
            StorytellerDecision.DrunkInvestigatorInfo(RoleId("Poisoner"), listOf(1, 4)),
        )

        val result = RecommendationSearch.recommendConstrained(game, roles, locked)

        assertTrue(result.failureCodes.isEmpty())
        assertTrue(result.plans.isNotEmpty())
        assertTrue(result.plans.all { plan -> locked.all { it in plan.decisions } })
    }

    @Test
    fun `illegal locked red herring is rejected instead of ignored`() {
        val result = RecommendationSearch.recommendConstrained(
            game,
            roles,
            listOf(StorytellerDecision.RedHerring(7)),
        )

        assertTrue(result.plans.isEmpty())
        assertEquals(listOf("locked-decisions-illegal-or-incompatible"), result.failureCodes)
    }

    @Test
    fun `incompatible drunk locks are rejected`() {
        val result = RecommendationSearch.recommendConstrained(
            game,
            roles,
            listOf(
                StorytellerDecision.DrunkShownRole(RoleId("Monk")),
                StorytellerDecision.DrunkInvestigatorInfo(RoleId("Poisoner"), listOf(1, 4)),
            ),
        )

        assertTrue(result.plans.isEmpty())
        assertEquals(listOf("locked-decisions-illegal-or-incompatible"), result.failureCodes)
    }
}
