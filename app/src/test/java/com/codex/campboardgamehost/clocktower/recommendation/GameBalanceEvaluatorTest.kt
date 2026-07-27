package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.PublicBalanceHint
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameBalanceEvaluatorTest {
    @Test
    fun `more dead good players marks evil ahead`() {
        val assessment = GameBalanceEvaluator.evaluate(
            game = game(goodAlive = 2, goodDead = 4, evilAlive = 2, evilDead = 0),
            round = 3,
        )

        assertEquals(PublicBalanceHint.EVIL_AHEAD, assessment.hint)
        assertTrue(assessment.evilAdvantage > 0)
    }

    @Test
    fun `dead evil players marks good ahead`() {
        val assessment = GameBalanceEvaluator.evaluate(
            game = game(goodAlive = 6, goodDead = 0, evilAlive = 1, evilDead = 1),
            round = 2,
        )

        assertEquals(PublicBalanceHint.GOOD_AHEAD, assessment.hint)
        assertTrue(assessment.evilAdvantage < 0)
    }

    @Test
    fun `information style helps the trailing team`() {
        assertEquals(
            RecommendationStyle.GENTLE,
            GameBalanceEvaluator.adjustInformationStyle(RecommendationStyle.BALANCED, evilAdvantage = 40),
        )
        assertEquals(
            RecommendationStyle.AGGRESSIVE,
            GameBalanceEvaluator.adjustInformationStyle(RecommendationStyle.BALANCED, evilAdvantage = -40),
        )
    }

    private fun game(
        goodAlive: Int,
        goodDead: Int,
        evilAlive: Int,
        evilDead: Int,
    ): GameState {
        val players = buildList {
            repeat(goodAlive + goodDead) { index ->
                add(player(index + 1, Alignment.GOOD, alive = index < goodAlive))
            }
            repeat(evilAlive + evilDead) { index ->
                add(
                    player(
                        goodAlive + goodDead + index + 1,
                        Alignment.EVIL,
                        alive = index < evilAlive,
                    ),
                )
            }
        }
        return GameState(ScriptId("test"), players, seed = 42)
    }

    private fun player(seat: Int, alignment: Alignment, alive: Boolean) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(if (alignment == Alignment.GOOD) "Townsfolk" else "Minion"),
        actualAlignment = alignment,
        actualType = if (alignment == Alignment.GOOD) CharacterType.TOWNSFOLK else CharacterType.MINION,
        alive = alive,
    )
}
