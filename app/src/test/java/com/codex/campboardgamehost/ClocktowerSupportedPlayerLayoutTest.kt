package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The real supported 5–15 player range must fit on a compact portrait phone. */
class ClocktowerSupportedPlayerLayoutTest {
    @Test fun wakeArrowsStayBetweenSeatsAndCentralControlsAtCompactSizes() {
        for (height in listOf(448f, 520f)) for (count in 5..15) {
            val layout = resolveHostTableSurfaceLayout(360f, height, count).layout
            val c = layout.constraints
            val controls = androidx.compose.ui.geometry.Rect(
                (360f - c.centerWorkspaceWidth) / 2 + 12f,
                (height - c.centerWorkspaceHeight) / 2 + 12f,
                (360f + c.centerWorkspaceWidth) / 2 - 12f,
                (height + c.centerWorkspaceHeight) / 2 - 12f,
            )
            for (slot in layout.slots) {
                val points = clocktowerWakeArrowPoints(slot, layout)
                assertTrue("$count seats, $height high: missing wake arrow", points.isNotEmpty())
                val bounds = androidx.compose.ui.geometry.Rect(
                    points.minOf { it.x } - 1f, points.minOf { it.y } - 1f,
                    points.maxOf { it.x } + 1f, points.maxOf { it.y } + 1f,
                )
                assertTrue("$count seats, $height high: arrow outside table", bounds.left >= 0 &&
                    bounds.top >= 0 && bounds.right <= 360f && bounds.bottom <= height)
                assertTrue("$count seats, $height high: arrow $bounds over central controls $controls at $slot", !bounds.overlaps(controls))
                layout.slots.forEach { other ->
                    val seatBounds = androidx.compose.ui.geometry.Rect(
                        other.centerX - c.seatCardWidth / 2, other.centerY - c.seatCardHeight / 2,
                        other.centerX + c.seatCardWidth / 2, other.centerY + c.seatCardHeight / 2,
                    )
                    assertTrue("$count seats, $height high: arrow over seat ${other.ringIndex}", !bounds.overlaps(seatBounds))
                }
            }
        }
    }

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
