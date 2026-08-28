package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** GCR-0 authority-convergence regressions for current Demon identity. */
class ClocktowerCurrentDemonAuthorityTest {
    @Test
    fun `host context and attack mechanics agree on live successor Demon`() {
        val cards = listOf(
            card(
                name = "Imp0",
                roleName = "Imp",
                team = ClocktowerTeam.Demon,
                eliminatedRound = 2,
            ),
            card(
                name = "Imp1",
                roleName = "Imp",
                team = ClocktowerTeam.Demon,
            ),
            card(
                name = "Mayor",
                roleName = "Mayor",
                team = ClocktowerTeam.Townsfolk,
            ),
        )

        val hostContext = resolveCurrentDemonHostContext(
            cards = cards,
            poisonedPlayerName = "Imp1",
        )

        assertEquals("Imp1", hostContext?.actor?.name)
        assertTrue(hostContext?.isPoisoned == true)
        assertEquals(
            DemonNightAttackOutcome.NO_DEATH,
            resolveTroubleBrewingDemonNightAttackOutcome(
                cards = cards,
                targetName = "Mayor",
                poisonedPlayerName = "Imp1",
                monkProtectedTargetName = null,
            ),
        )
    }

    @Test
    fun `current Demon authority fails closed when two live Demons exist`() {
        val cards = listOf(
            card("Imp1", "Imp", ClocktowerTeam.Demon),
            card("Imp2", "Imp", ClocktowerTeam.Demon),
        )

        assertEquals(null, resolveCurrentDemonCard(cards))
    }

    private fun card(
        name: String,
        roleName: String,
        team: ClocktowerTeam,
        eliminatedRound: Int? = null,
    ) = PlayerCard(
        name = name,
        role = Role.Civilian,
        word = "",
        clocktowerTeam = team,
        clocktowerRole = ClocktowerRole(
            team = team,
            zhName = roleName,
            enName = roleName,
            zhDescription = "",
            enDescription = "",
        ),
        eliminatedRound = eliminatedRound,
    )
}
