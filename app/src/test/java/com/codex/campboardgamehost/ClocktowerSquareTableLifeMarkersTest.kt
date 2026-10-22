package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSquareTableLifeMarkersTest {
    @Test
    fun `dead player with unspent ghost vote shows death mark and vote marker`() {
        val markers = clocktowerSquareTableLifeMarkers(
            isAlive = false,
            hasUnspentGhostVote = true,
        )

        assertTrue(markers.showDeathMark)
        assertTrue(markers.showUnspentGhostVote)
    }

    @Test
    fun `dead player with spent ghost vote keeps death mark without vote marker`() {
        val markers = clocktowerSquareTableLifeMarkers(
            isAlive = false,
            hasUnspentGhostVote = false,
        )

        assertTrue(markers.showDeathMark)
        assertFalse(markers.showUnspentGhostVote)
    }

    @Test
    fun `alive player never shows death or ghost vote marker`() {
        val markers = clocktowerSquareTableLifeMarkers(
            isAlive = true,
            hasUnspentGhostVote = true,
        )

        assertFalse(markers.showDeathMark)
        assertFalse(markers.showUnspentGhostVote)
    }
}
