package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerHostTableLayoutTest {
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
    fun `same constraints and player count produce the same ordered slot ring`() {
        val first = hostTableLayout(
            playerCount = 15,
            constraints = portraitConstraints,
        )
        val second = hostTableLayout(
            playerCount = 15,
            constraints = portraitConstraints,
        )

        assertEquals(first, second)
        assertEquals((0 until 15).toList(), first.slots.map { it.ringIndex })
        assertEquals(15, first.slots.distinct().size)
    }

    @Test
    fun `portrait constraints naturally allocate more seats to vertical edges`() {
        val layout = hostTableLayout(
            playerCount = 12,
            constraints = portraitConstraints,
        )
        val counts = ClocktowerSquareTableEdge.values().associateWith { edge ->
            layout.slots.count { it.edge == edge }
        }

        assertTrue(counts.getValue(ClocktowerSquareTableEdge.Right) > counts.getValue(ClocktowerSquareTableEdge.Top))
        assertTrue(counts.getValue(ClocktowerSquareTableEdge.Left) > counts.getValue(ClocktowerSquareTableEdge.Bottom))
    }

    @Test
    fun `slot ring follows deterministic clockwise edge direction`() {
        val layout = hostTableLayout(
            playerCount = 12,
            constraints = portraitConstraints,
        )

        val top = layout.slots.filter { it.edge == ClocktowerSquareTableEdge.Top }
        val right = layout.slots.filter { it.edge == ClocktowerSquareTableEdge.Right }
        val bottom = layout.slots.filter { it.edge == ClocktowerSquareTableEdge.Bottom }
        val left = layout.slots.filter { it.edge == ClocktowerSquareTableEdge.Left }

        assertStrictlyIncreasing(top.map { it.centerX })
        assertStrictlyIncreasing(right.map { it.centerY })
        assertStrictlyDecreasing(bottom.map { it.centerX })
        assertStrictlyDecreasing(left.map { it.centerY })
        assertEquals(
            listOf(
                ClocktowerSquareTableEdge.Top,
                ClocktowerSquareTableEdge.Right,
                ClocktowerSquareTableEdge.Bottom,
                ClocktowerSquareTableEdge.Left,
            ),
            layout.slots.map { it.edge }.distinct(),
        )
    }

    @Test
    fun `seat cards preserve center clearance and minimum same-edge separation`() {
        val layout = hostTableLayout(
            playerCount = 15,
            constraints = portraitConstraints,
        )
        val constraints = layout.constraints
        val centerLeft = (constraints.availableWidth - constraints.centerWorkspaceWidth) / 2f
        val centerRight = centerLeft + constraints.centerWorkspaceWidth
        val centerTop = (constraints.availableHeight - constraints.centerWorkspaceHeight) / 2f
        val centerBottom = centerTop + constraints.centerWorkspaceHeight
        val epsilon = 0.001f

        layout.slots.forEach { slot ->
            when (slot.edge) {
                ClocktowerSquareTableEdge.Top -> assertTrue(
                    slot.centerY + constraints.seatCardHeight / 2f + constraints.minimumSafeSeparation <= centerTop + epsilon,
                )
                ClocktowerSquareTableEdge.Right -> assertTrue(
                    slot.centerX - constraints.seatCardWidth / 2f - constraints.minimumSafeSeparation >= centerRight - epsilon,
                )
                ClocktowerSquareTableEdge.Bottom -> assertTrue(
                    slot.centerY - constraints.seatCardHeight / 2f - constraints.minimumSafeSeparation >= centerBottom - epsilon,
                )
                ClocktowerSquareTableEdge.Left -> assertTrue(
                    slot.centerX + constraints.seatCardWidth / 2f + constraints.minimumSafeSeparation <= centerLeft + epsilon,
                )
            }
        }

        ClocktowerSquareTableEdge.values().forEach { edge ->
            val slots = layout.slots.filter { it.edge == edge }
            slots.zipWithNext().forEach { (first, second) ->
                val axisDistance = when (edge) {
                    ClocktowerSquareTableEdge.Top,
                    ClocktowerSquareTableEdge.Bottom,
                    -> kotlin.math.abs(second.centerX - first.centerX)
                    ClocktowerSquareTableEdge.Right,
                    ClocktowerSquareTableEdge.Left,
                    -> kotlin.math.abs(second.centerY - first.centerY)
                }
                val minimumDistance = when (edge) {
                    ClocktowerSquareTableEdge.Top,
                    ClocktowerSquareTableEdge.Bottom,
                    -> constraints.seatCardWidth + constraints.minimumSafeSeparation
                    ClocktowerSquareTableEdge.Right,
                    ClocktowerSquareTableEdge.Left,
                    -> constraints.seatCardHeight + constraints.minimumSafeSeparation
                }
                assertTrue(axisDistance + epsilon >= minimumDistance)
            }
        }
    }

    @Test
    fun `insufficient perimeter capacity fails closed`() {
        val tooSmall = portraitConstraints.copy(
            availableWidth = 180f,
            availableHeight = 220f,
            centerWorkspaceWidth = 40f,
            centerWorkspaceHeight = 100f,
        )

        assertThrows(IllegalArgumentException::class.java) {
            hostTableLayout(
                playerCount = 15,
                constraints = tooSmall,
            )
        }
    }

    private fun assertStrictlyIncreasing(values: List<Float>) {
        assertTrue(values.size >= 2)
        assertTrue(values.zipWithNext().all { (first, second) -> second > first })
    }

    private fun assertStrictlyDecreasing(values: List<Float>) {
        assertTrue(values.size >= 2)
        assertTrue(values.zipWithNext().all { (first, second) -> second < first })
    }
}
