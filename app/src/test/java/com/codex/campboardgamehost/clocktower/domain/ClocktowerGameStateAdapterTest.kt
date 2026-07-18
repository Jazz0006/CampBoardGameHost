package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.ClocktowerRole
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.PlayerCard
import com.codex.campboardgamehost.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerGameStateAdapterTest {
    @Test
    fun `adapter separates actual and shown roles and preserves state`() {
        val drunk = role(ClocktowerTeam.Outsider, "Drunk")
        val investigator = role(ClocktowerTeam.Townsfolk, "Investigator")
        val imp = role(ClocktowerTeam.Demon, "Imp")
        val cards = listOf(
            card("Alice", drunk, shownRole = investigator),
            card("Bob", imp, eliminatedRound = 1),
        )

        val state = cards.toClocktowerGameState(
            script = ClocktowerScript.TroubleBrewing,
            seed = 123L,
            poisonedPlayerName = "Alice",
        )

        val alice = state.playerAt(1)!!
        val bob = state.playerAt(2)!!
        assertEquals("trouble_brewing", state.script.value)
        assertEquals(RoleId("Drunk"), alice.actualRole)
        assertEquals(RoleId("Investigator"), alice.shownRole)
        assertEquals(Alignment.GOOD, alice.actualAlignment)
        assertEquals(CharacterType.OUTSIDER, alice.actualType)
        assertTrue(alice.poisoned)
        assertFalse(bob.alive)
        assertEquals(Alignment.EVIL, bob.actualAlignment)
    }

    private fun card(
        name: String,
        actualRole: ClocktowerRole,
        shownRole: ClocktowerRole = actualRole,
        eliminatedRound: Int? = null,
    ): PlayerCard = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = actualRole.team,
        clocktowerRole = actualRole,
        clocktowerShownRole = shownRole,
        eliminatedRound = eliminatedRound,
    )

    private fun role(team: ClocktowerTeam, enName: String): ClocktowerRole = ClocktowerRole(
        team = team,
        zhName = enName,
        enName = enName,
        zhDescription = "",
        enDescription = "",
    )
}
