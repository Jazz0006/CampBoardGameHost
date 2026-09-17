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
    fun `legal target pairs include self and every distinct living pair exactly once`() {
        val pairs = FortuneTellerInformationSemantics.legalTargetPairs(game())

        assertEquals(21, pairs.size)
        assertEquals(pairs.size, pairs.distinct().size)
        assertTrue(1 to 2 in pairs)
        assertTrue(2 to 7 in pairs)
    }

    private fun game() = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(3, "Slayer", CharacterType.TOWNSFOLK),
            player(4, "Undertaker", CharacterType.TOWNSFOLK),
            player(5, "Virgin", CharacterType.TOWNSFOLK),
            player(6, "Scarlet Woman", CharacterType.MINION),
            player(7, "Imp", CharacterType.DEMON),
        ),
        seed = 20260917L,
    )

    private fun player(seat: Int, role: String, type: CharacterType) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )
}
