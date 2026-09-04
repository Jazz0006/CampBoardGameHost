package com.codex.campboardgamehost

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
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
    fun `5 8 12 and 15 players are equal path samples around one rounded rectangle`() {
        listOf(5, 8, 12, 15).forEach { playerCount ->
            val layout = hostTableLayout(
                playerCount = playerCount,
                constraints = portraitConstraints,
            )
            val expectedPerimeter = ExpectedRoundedPerimeter(portraitConstraints)
            val pathStep = expectedPerimeter.totalLength / playerCount.toFloat()

            layout.slots.forEachIndexed { index, slot ->
                val expected = expectedPerimeter.pointAt(pathStep * index)
                assertEquals(
                    "$playerCount-player slot $index x must be sampled from the continuous rounded perimeter",
                    expected.x.toDouble(),
                    slot.centerX.toDouble(),
                    0.05,
                )
                assertEquals(
                    "$playerCount-player slot $index y must be sampled from the continuous rounded perimeter",
                    expected.y.toDouble(),
                    slot.centerY.toDouble(),
                    0.05,
                )
            }
        }
    }

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

    private data class ExpectedPoint(
        val x: Float,
        val y: Float,
    )

    /**
     * Independent geometry oracle for the F7.2 contract.
     *
     * The seat-center ring starts at the top-left tangent, proceeds clockwise, and uses one
     * continuous rounded-rectangle path. The radius reserves at least one seat-card major extent
     * plus the configured safe separation before turning the corner.
     */
    private class ExpectedRoundedPerimeter(
        constraints: HostTableLayoutConstraints,
    ) {
        private val left = constraints.seatCardWidth / 2f
        private val top = constraints.seatCardHeight / 2f
        private val right = constraints.availableWidth - constraints.seatCardWidth / 2f
        private val bottom = constraints.availableHeight - constraints.seatCardHeight / 2f
        private val pathWidth = right - left
        private val pathHeight = bottom - top
        private val radius = minOf(
            maxOf(constraints.seatCardWidth, constraints.seatCardHeight) + constraints.minimumSafeSeparation,
            pathWidth / 2f,
            pathHeight / 2f,
        )
        private val horizontalStraight = pathWidth - 2f * radius
        private val verticalStraight = pathHeight - 2f * radius
        private val quarterArc = (PI.toFloat() * radius) / 2f

        val totalLength: Float =
            2f * horizontalStraight + 2f * verticalStraight + 4f * quarterArc

        fun pointAt(rawDistance: Float): ExpectedPoint {
            var distance = ((rawDistance % totalLength) + totalLength) % totalLength

            if (distance <= horizontalStraight) {
                return ExpectedPoint(left + radius + distance, top)
            }
            distance -= horizontalStraight

            if (distance <= quarterArc) {
                return arcPoint(
                    centerX = right - radius,
                    centerY = top + radius,
                    startAngleRadians = -PI.toFloat() / 2f,
                    arcDistance = distance,
                )
            }
            distance -= quarterArc

            if (distance <= verticalStraight) {
                return ExpectedPoint(right, top + radius + distance)
            }
            distance -= verticalStraight

            if (distance <= quarterArc) {
                return arcPoint(
                    centerX = right - radius,
                    centerY = bottom - radius,
                    startAngleRadians = 0f,
                    arcDistance = distance,
                )
            }
            distance -= quarterArc

            if (distance <= horizontalStraight) {
                return ExpectedPoint(right - radius - distance, bottom)
            }
            distance -= horizontalStraight

            if (distance <= quarterArc) {
                return arcPoint(
                    centerX = left + radius,
                    centerY = bottom - radius,
                    startAngleRadians = PI.toFloat() / 2f,
                    arcDistance = distance,
                )
            }
            distance -= quarterArc

            if (distance <= verticalStraight) {
                return ExpectedPoint(left, bottom - radius - distance)
            }
            distance -= verticalStraight

            return arcPoint(
                centerX = left + radius,
                centerY = top + radius,
                startAngleRadians = PI.toFloat(),
                arcDistance = distance,
            )
        }

        private fun arcPoint(
            centerX: Float,
            centerY: Float,
            startAngleRadians: Float,
            arcDistance: Float,
        ): ExpectedPoint {
            val angle = startAngleRadians + arcDistance / radius
            return ExpectedPoint(
                x = centerX + radius * cos(angle.toDouble()).toFloat(),
                y = centerY + radius * sin(angle.toDouble()).toFloat(),
            )
        }
    }
}
