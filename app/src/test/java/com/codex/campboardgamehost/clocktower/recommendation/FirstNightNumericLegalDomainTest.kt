package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightNumericLegalDomainTest {
    @Test
    fun `Drunk shown Empath keeps complete display range including false and accidental truth`() {
        val game = game(
            player(1, "Recluse", CharacterType.OUTSIDER),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = FirstNightNumericLegalDomain.generate(
            game = game,
            sourceSeat = 2,
            abilityRole = RoleId("Empath"),
            reliability = ReliabilityState.DRUNK,
        )

        assertEquals(listOf(0, 1, 2), candidates.map { it.value })
        assertTrue(candidates.any { it.semanticTruth == SemanticTruth.TRUE })
        assertTrue(candidates.any { it.semanticTruth == SemanticTruth.FALSE })
    }

    @Test
    fun `reliable numeric domain excludes false surface values`() {
        val game = game(
            player(1, "Recluse", CharacterType.OUTSIDER),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = FirstNightNumericLegalDomain.generate(
            game = game,
            sourceSeat = 2,
            abilityRole = RoleId("Empath"),
            reliability = ReliabilityState.RELIABLE,
        )

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.all { it.semanticTruth == SemanticTruth.TRUE })
        assertTrue(candidates.map { it.value }.toSet() != setOf(0, 1, 2))
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20260918L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
