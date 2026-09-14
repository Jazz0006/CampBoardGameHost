package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Stable No Greater Joy product-surface contracts around the generic production setup owner.
 *
 * Exact generated distributions and committed identities are proved at the typed setup seam.
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
    fun `No Greater Joy remains a startable script`() {
        assertTrue(canStartClocktowerScript(ClocktowerScript.NoGreaterJoy))
    }
}
