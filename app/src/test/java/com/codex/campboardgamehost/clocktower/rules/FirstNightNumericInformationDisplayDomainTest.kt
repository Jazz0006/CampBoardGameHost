package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirstNightNumericInformationDisplayDomainTest {
    @Test
    fun `Drunk shown Empath uses the complete zero through two display domain`() {
        val game = game(
            player(1, "Chef", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(3, "Soldier", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val domain = requireNotNull(FirstNightNumericInformationDisplayDomain.domain(game, 2))

        assertEquals(RoleId("Empath"), domain.abilityRole)
        assertEquals(0..2, domain.values)
    }

    @Test
    fun `Drunk shown Chef preserves the existing Host upper-bound behavior`() {
        val game = game(
            player(1, "Recluse", CharacterType.OUTSIDER),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Chef"),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val domain = requireNotNull(FirstNightNumericInformationDisplayDomain.domain(game, 2))

        assertEquals(RoleId("Chef"), domain.abilityRole)
        assertEquals(0..3, domain.values)
    }

    @Test
    fun `non numeric perceived role has no first-night numeric display domain`() {
        val game = game(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Drunk", CharacterType.OUTSIDER, shownRole = "Investigator"),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        assertNull(FirstNightNumericInformationDisplayDomain.domain(game, 2))
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
