package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MayorRedirectRecommenderTest {
    @Test
    fun `beginner styles avoid no-death trick and evil target`() {
        val request = request(
            protectedSeats = setOf(3),
            spentAbilitySeats = setOf(4),
        )

        val recommendations = MayorRedirectRecommender.recommend(request, mayorSeat = 1)

        assertEquals(1, recommendations.getValue(RecommendationStyle.GENTLE).targetSeat())
        assertEquals(4, recommendations.getValue(RecommendationStyle.BALANCED).targetSeat())
        assertEquals(2, recommendations.getValue(RecommendationStyle.AGGRESSIVE).targetSeat())
        assertTrue(
            recommendations
                .getValue(RecommendationStyle.BALANCED)
                .warnings
                .none { it.ruleId == "no-night-death" },
        )
    }

    @Test
    fun `dead protected and Soldier redirect targets resolve to no death`() {
        val game = request(
            protectedSeats = setOf(3),
            spentAbilitySeats = emptySet(),
            deadSeats = setOf(4),
        )

        val recommendations = MayorRedirectRecommender.recommend(game, mayorSeat = 1)
        val allOutcomes = game.state.game.players.associate { player ->
            val outcome = MayorRedirectRecommender.resolveOutcome(game, mayorSeat = 1, targetSeat = player.seat)
            player.seat to outcome
        }

        assertNull(allOutcomes.getValue(2).actualDeathSeat)
        assertNull(allOutcomes.getValue(3).actualDeathSeat)
        assertNull(allOutcomes.getValue(4).actualDeathSeat)
        assertEquals(1, allOutcomes.getValue(1).actualDeathSeat)
        assertTrue(recommendations.isNotEmpty())
    }

    @Test
    fun `poisoned Mayor produces no discretionary recommendation`() {
        assertTrue(
            MayorRedirectRecommender.recommend(
                request = request(),
                mayorSeat = 1,
                abilityReliable = false,
            ).isEmpty(),
        )
    }

    private fun request(
        protectedSeats: Set<Int> = emptySet(),
        spentAbilitySeats: Set<Int> = emptySet(),
        deadSeats: Set<Int> = emptySet(),
    ): DynamicDecisionRequest {
        val players = listOf(
            player(1, "Mayor", Alignment.GOOD, CharacterType.TOWNSFOLK, deadSeats),
            player(2, "Soldier", Alignment.GOOD, CharacterType.TOWNSFOLK, deadSeats),
            player(3, "Virgin", Alignment.GOOD, CharacterType.TOWNSFOLK, deadSeats),
            player(4, "Butler", Alignment.GOOD, CharacterType.OUTSIDER, deadSeats),
            player(5, "Imp", Alignment.EVIL, CharacterType.DEMON, deadSeats),
        )
        return DynamicDecisionRequest(
            id = "night-2-mayor",
            type = StorytellerDecisionType.MAYOR_DEATH_RESOLUTION,
            sourceAbility = RoleId("Mayor"),
            state = DynamicGameState(
                game = GameState(ScriptId("trouble-brewing"), players, seed = 7),
                phase = StorytellerPhase.NIGHT,
                round = 2,
                protectedSeats = protectedSeats,
                spentAbilitySeats = spentAbilitySeats,
            ),
        )
    }

    private fun player(
        seat: Int,
        role: String,
        alignment: Alignment,
        type: CharacterType,
        deadSeats: Set<Int>,
    ) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
        alive = seat !in deadSeats,
    )

    private fun List<com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation>.getValue(
        style: RecommendationStyle,
    ) = first { it.style == style }

    private fun com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation.targetSeat(): Int =
        (candidate.choice as DynamicStorytellerChoice.MayorDeathResolution).targetSeat

}
