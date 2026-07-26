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
import org.junit.Assert.assertTrue
import org.junit.Test

class DemonSuccessorRecommenderTest {
    @Test
    fun `healthy Scarlet Woman is mandatory with five alive`() {
        val recommendations = DemonSuccessorRecommender.recommend(
            request(
                players = listOf(
                    player(1, "Imp", CharacterType.DEMON),
                    player(2, "Scarlet Woman", CharacterType.MINION),
                    player(3, "Poisoner", CharacterType.MINION),
                    player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
                    player(5, "Mayor", CharacterType.TOWNSFOLK, Alignment.GOOD),
                ),
            ),
        )

        assertEquals(1, recommendations.size)
        assertEquals(2, recommendations.single().targetSeat())
        assertEquals(RecommendationStyle.BALANCED, recommendations.single().style)
        assertTrue(recommendations.single().warnings.any { it.ruleId == "scarlet-woman-mandatory" })
    }

    @Test
    fun `poisoned Scarlet Woman is not mandatory`() {
        val recommendations = DemonSuccessorRecommender.recommend(
            request(
                players = listOf(
                    player(1, "Imp", CharacterType.DEMON),
                    player(2, "Scarlet Woman", CharacterType.MINION, poisoned = true),
                    player(3, "Baron", CharacterType.MINION),
                    player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
                    player(5, "Mayor", CharacterType.TOWNSFOLK, Alignment.GOOD),
                ),
            ),
        )

        assertTrue(recommendations.map { it.targetSeat() }.contains(3))
        assertTrue(recommendations.none { it.warnings.any { warning -> warning.ruleId == "scarlet-woman-mandatory" } })
    }

    @Test
    fun `balanced recommendation preserves active Poisoner when another minion is available`() {
        val recommendations = DemonSuccessorRecommender.recommend(
            request(
                players = listOf(
                    player(1, "Imp", CharacterType.DEMON),
                    player(2, "Poisoner", CharacterType.MINION),
                    player(3, "Baron", CharacterType.MINION),
                    player(4, "Chef", CharacterType.TOWNSFOLK, Alignment.GOOD),
                ),
            ),
        )

        assertEquals(
            3,
            recommendations.first { it.style == RecommendationStyle.BALANCED }.targetSeat(),
        )
    }

    private fun request(players: List<PlayerState>) = DynamicDecisionRequest(
        id = "night-2-successor",
        type = StorytellerDecisionType.DEMON_SUCCESSION,
        sourceAbility = RoleId("Imp"),
        state = DynamicGameState(
            game = GameState(ScriptId("trouble-brewing"), players, seed = 17),
            phase = StorytellerPhase.NIGHT,
            round = 2,
        ),
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        alignment: Alignment = Alignment.EVIL,
        poisoned: Boolean = false,
    ) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
        poisoned = poisoned,
    )

    private fun com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRecommendation.targetSeat() =
        (candidate.choice as DynamicStorytellerChoice.DemonSuccessor).targetSeat
}
