package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSquareTableLifeMarkersTest {
    @Test
    fun `dead player with unspent ghost vote shows death cross and vote marker`() {
        val markers = clocktowerSquareTableLifeMarkers(
            isAlive = false,
            hasUnspentGhostVote = true,
        )

        assertTrue(markers.showDeathCross)
        assertTrue(markers.showUnspentGhostVote)
    }

    @Test
    fun `dead player with spent ghost vote keeps death cross without vote marker`() {
        val markers = clocktowerSquareTableLifeMarkers(
            isAlive = false,
            hasUnspentGhostVote = false,
        )

        assertTrue(markers.showDeathCross)
        assertFalse(markers.showUnspentGhostVote)
    }

    @Test
    fun `alive player never shows death or ghost vote marker`() {
        val markers = clocktowerSquareTableLifeMarkers(
            isAlive = true,
            hasUnspentGhostVote = true,
        )

        assertFalse(markers.showDeathCross)
        assertFalse(markers.showUnspentGhostVote)
    }
}
