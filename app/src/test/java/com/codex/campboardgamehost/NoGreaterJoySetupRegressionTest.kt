package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TBSP cutover acceptance for P10: No Greater Joy remains on its existing setup contract.
 *
 * Trouble Brewing owns the curated preset cutover. NGJ must retain its established role pool,
 * supported player-count distributions, and start eligibility.
 */
class NoGreaterJoySetupRegressionTest {
    @Test
    fun `No Greater Joy keeps its established setup role pool`() {
        assertEquals(
            setOf(
                "Clockmaker",
                "Investigator",
                "Empath",
                "Chambermaid",
                "Artist",
                "Sage",
                "Drunk",
                "Klutz",
                "Baron",
                "Scarlet Woman",
                "Imp",
            ),
            clocktowerRolesForScript(ClocktowerScript.NoGreaterJoy)
                .map { it.enName }
                .toSet(),
        )
    }

    @Test
    fun `No Greater Joy keeps five and six player distributions`() {
        assertEquals(
            mapOf(
                ClocktowerTeam.Townsfolk to 3,
                ClocktowerTeam.Outsider to 0,
                ClocktowerTeam.Minion to 1,
                ClocktowerTeam.Demon to 1,
            ),
            clocktowerDistribution(5),
        )
        assertEquals(
            mapOf(
                ClocktowerTeam.Townsfolk to 3,
                ClocktowerTeam.Outsider to 1,
                ClocktowerTeam.Minion to 1,
                ClocktowerTeam.Demon to 1,
            ),
            clocktowerDistribution(6),
        )
    }

    @Test
    fun `No Greater Joy remains a startable script`() {
        assertTrue(canStartClocktowerScript(ClocktowerScript.NoGreaterJoy))
    }
}
