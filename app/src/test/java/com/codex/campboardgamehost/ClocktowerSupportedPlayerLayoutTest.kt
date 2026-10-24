package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

/** The real supported 5–15 player range must fit on a compact portrait phone. */
class ClocktowerSupportedPlayerLayoutTest {
    @Test
    fun `setup sized portrait preserves strict capacity for every supported player count`() {
        for (playerCount in 5..15) {
            val resolved = resolveHostTableSurfaceLayout(
                availableWidth = 360f,
                availableHeight = 520f,
                playerCount = playerCount,
            )

            assertEquals(playerCount, resolved.layout.slots.size)
            assertEquals(
                resolved.seatDensity.cardWidth,
                resolved.layout.constraints.seatCardWidth,
            )
            assertEquals(
                resolved.seatDensity.cardHeight,
                resolved.layout.constraints.seatCardHeight,
            )
            hostTableLayout(
                playerCount = playerCount,
                constraints = resolved.layout.constraints,
            )
        }
    }

}
