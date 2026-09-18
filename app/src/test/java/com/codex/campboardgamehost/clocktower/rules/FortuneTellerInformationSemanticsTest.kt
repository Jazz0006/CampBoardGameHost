package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FortuneTellerInformationSemanticsTest {
    @Test
    fun `healthy result is yes for Demon or selected red herring and no otherwise`() {
        val game = game()

        assertTrue(FortuneTellerInformationSemantics.healthyResult(game, setOf(2, 7), redHerringSeat = 4))
        assertTrue(FortuneTellerInformationSemantics.healthyResult(game, setOf(2, 4), redHerringSeat = 4))
        assertFalse(FortuneTellerInformationSemantics.healthyResult(game, setOf(2, 3), redHerringSeat = 4))
    }

    @Test
    fun `legal target pairs include self dead players and every distinct pair exactly once`() {
        val game = game(deadSeat = 5)
        val pairs = FortuneTellerInformationSemantics.legalTargetPairs(game)

        assertEquals(21, pairs.size)
        assertEquals(pairs.size, pairs.distinct().size)
        assertTrue(1 to 2 in pairs)
        assertTrue(2 to 5 in pairs)
        assertTrue(2 to 7 in pairs)
        assertTrue(FortuneTellerInformationSemantics.healthyResult(game, setOf(2, 5), redHerringSeat = 5))
    }

    @Test
    fun `Drunk shown Fortune Teller has the same player-controlled target domain`() {
        val game = GameState(
            script = ScriptId("trouble_brewing"),
            players = listOf(
                player(1, "Librarian", CharacterType.TOWNSFOLK, alive = true),
                PlayerState(
                    seat = 2,
                    name = "P2",
                    actualRole = RoleId("Drunk"),
                    actualAlignment = Alignment.GOOD,
                    actualType = CharacterType.OUTSIDER,
                    shownRole = RoleId("Fortune Teller"),
                ),
                player(3, "Chef", CharacterType.TOWNSFOLK, alive = true),
                player(4, "Soldier", CharacterType.TOWNSFOLK, alive = true),
                player(5, "Scarlet Woman", CharacterType.MINION, alive = true),
                player(6, "Imp", CharacterType.DEMON, alive = true),
            ),
            seed = 20260918L,
        )

        val pairs = FortuneTellerInformationSemantics.legalTargetPairs(game, sourceSeat = 2)

        assertEquals(15, pairs.size)
        assertTrue(1 to 2 in pairs)
        assertTrue(2 to 6 in pairs)
        assertEquals(emptyList<Pair<Int, Int>>(), FortuneTellerInformationSemantics.legalTargetPairs(game))
    }

    private fun game(deadSeat: Int? = null) = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Librarian", CharacterType.TOWNSFOLK, alive = deadSeat != 1),
            player(2, "Fortune Teller", CharacterType.TOWNSFOLK, alive = deadSeat != 2),
            player(3, "Slayer", CharacterType.TOWNSFOLK, alive = deadSeat != 3),
            player(4, "Undertaker", CharacterType.TOWNSFOLK, alive = deadSeat != 4),
            player(5, "Virgin", CharacterType.TOWNSFOLK, alive = deadSeat != 5),
            player(6, "Scarlet Woman", CharacterType.MINION, alive = deadSeat != 6),
            player(7, "Imp", CharacterType.DEMON, alive = deadSeat != 7),
        ),
        seed = 20260917L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        alive: Boolean,
    ) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
        alive = alive,
    )
}
