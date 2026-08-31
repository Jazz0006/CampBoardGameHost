package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstNightNumericInformationSemanticsTest {
    @Test
    fun `Empath healthy truth space is registration aware and independent of impairment`() {
        val healthy = game(source = player(2, "Empath", CharacterType.TOWNSFOLK))
        val poisoned = game(source = player(2, "Empath", CharacterType.TOWNSFOLK, poisoned = true))
        val drunk = game(source = player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"))

        val expectedTruthSpace = setOf(0, 1)

        assertEquals(expectedTruthSpace, FirstNightNumericInformationSemantics.healthyTruthValues(healthy, 2))
        assertEquals(expectedTruthSpace, FirstNightNumericInformationSemantics.healthyTruthValues(poisoned, 2))
        assertEquals(expectedTruthSpace, FirstNightNumericInformationSemantics.healthyTruthValues(drunk, 2))
    }

    private fun game(source: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Recluse", CharacterType.OUTSIDER),
            source,
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        ),
        seed = 20260901L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        poisoned: Boolean = false,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
        poisoned = poisoned,
    )
}
