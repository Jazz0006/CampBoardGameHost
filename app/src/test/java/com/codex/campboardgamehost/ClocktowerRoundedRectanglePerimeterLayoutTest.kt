package com.codex.campboardgamehost

import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRoundedRectanglePerimeterLayoutTest {
    private val portraitConstraints = HostTableLayoutConstraints(
        availableWidth = 360f,
        availableHeight = 600f,
        seatCardWidth = 64f,
        seatCardHeight = 50f,
        minimumSafeSeparation = 4f,
        centerWorkspaceWidth = 200f,
        centerWorkspaceHeight = 312f,
    )

    @Test
    fun `representative rounded perimeter slots preserve card separation including corners`() {
        listOf(5, 8, 12, 15).forEach { playerCount ->
            val layout = hostTableLayout(
                playerCount = playerCount,
                constraints = portraitConstraints,
            )

            layout.slots.forEachIndexed { firstIndex, first ->
                layout.slots.drop(firstIndex + 1).forEach { second ->
                    val horizontalClearance = abs(first.centerX - second.centerX) + 0.001f >=
                        portraitConstraints.seatCardWidth + portraitConstraints.minimumSafeSeparation
                    val verticalClearance = abs(first.centerY - second.centerY) + 0.001f >=
                        portraitConstraints.seatCardHeight + portraitConstraints.minimumSafeSeparation
                    assertTrue(
                        "$playerCount-player slots ${first.ringIndex} and ${second.ringIndex} overlap or violate safe separation",
                        horizontalClearance || verticalClearance,
                    )
                }
            }
        }
    }

}
