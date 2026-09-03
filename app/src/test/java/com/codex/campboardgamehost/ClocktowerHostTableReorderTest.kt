package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerHostTableReorderTest {
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
    fun `nearest ring index uses the same computed spatial slots as rendering`() {
        val layout = hostTableLayout(
            playerCount = 12,
            constraints = portraitConstraints,
        )

        layout.slots.forEach { slot ->
            assertEquals(
                slot.ringIndex,
                nearestHostTableRingIndex(
                    layout = layout,
                    pointerX = slot.centerX,
                    pointerY = slot.centerY,
                ),
            )
        }
    }

    @Test
    fun `nearest ring index crosses a top-right corner in deterministic clockwise order`() {
        val layout = hostTableLayout(
            playerCount = 12,
            constraints = portraitConstraints,
        )
        val lastTop = layout.slots.last { it.edge == ClocktowerSquareTableEdge.Top }
        val firstRight = layout.slots.first { it.edge == ClocktowerSquareTableEdge.Right }

        assertEquals(lastTop.ringIndex + 1, firstRight.ringIndex)
        assertEquals(
            firstRight.ringIndex,
            nearestHostTableRingIndex(
                layout = layout,
                pointerX = firstRight.centerX,
                pointerY = firstRight.centerY,
            ),
        )
        assertEquals(
            lastTop.ringIndex,
            nearestHostTableRingIndex(
                layout = layout,
                pointerX = lastTop.centerX,
                pointerY = lastTop.centerY,
            ),
        )
    }

    @Test
    fun `drag across top-right corner inserts at the resolved clockwise slot`() {
        val layout = hostTableLayout(
            playerCount = 12,
            constraints = portraitConstraints,
        )
        val original = List(12) { index -> "P${index + 1}" }
        val sourceSlot = layout.slots.last { it.edge == ClocktowerSquareTableEdge.Top }
        val targetSlot = layout.slots.first { it.edge == ClocktowerSquareTableEdge.Right }
        val draggedPlayer = original[sourceSlot.ringIndex]
        val targetRingIndex = nearestHostTableRingIndex(
            layout = layout,
            pointerX = targetSlot.centerX,
            pointerY = targetSlot.centerY,
        )

        val reordered = reorderHostTableItems(
            items = original,
            fromIndex = sourceSlot.ringIndex,
            targetIndex = targetRingIndex,
        )

        assertEquals(sourceSlot.ringIndex + 1, targetRingIndex)
        assertEquals(draggedPlayer, reordered[targetRingIndex])
        assertEquals(original.toSet(), reordered.toSet())
        assertEquals(original.size, reordered.size)
    }

    @Test
    fun `equidistant drag target resolves to the smaller ring index`() {
        val layout = hostTableLayout(
            playerCount = 8,
            constraints = portraitConstraints,
        )
        val first = layout.slots[0]
        val second = layout.slots[1]

        assertEquals(
            first.ringIndex,
            nearestHostTableRingIndex(
                layout = layout,
                pointerX = (first.centerX + second.centerX) / 2f,
                pointerY = (first.centerY + second.centerY) / 2f,
            ),
        )
    }

    @Test
    fun `reorder target index means final physical slot for forward and backward moves`() {
        val original = listOf("Alice", "Bob", "Casey", "Dana")

        assertEquals(
            listOf("Alice", "Casey", "Dana", "Bob"),
            reorderHostTableItems(
                items = original,
                fromIndex = 1,
                targetIndex = 3,
            ),
        )
        assertEquals(
            listOf("Dana", "Alice", "Bob", "Casey"),
            reorderHostTableItems(
                items = original,
                fromIndex = 3,
                targetIndex = 0,
            ),
        )
        assertEquals(
            original,
            reorderHostTableItems(
                items = original,
                fromIndex = 2,
                targetIndex = 2,
            ),
        )
    }

    @Test
    fun `reorder preserves every player exactly once then confirmation freezes the new order`() {
        val original = listOf("Alice", "Bob", "Casey", "Dana", "Evan")
        val reordered = reorderHostTableItems(
            items = original,
            fromIndex = 0,
            targetIndex = 4,
        )
        val confirmed = confirmHostSeating(reordered)

        assertEquals(listOf("Bob", "Casey", "Dana", "Evan", "Alice"), reordered)
        assertEquals(original.toSet(), reordered.toSet())
        assertEquals(original.size, reordered.size)
        assertEquals(reordered, confirmed.playerNames)
        assertEquals(
            (1..reordered.size).map(::ClocktowerSeatId),
            confirmed.seats.map(HostSeatAssignment::seatId),
        )
    }

    @Test
    fun `drag helpers fail closed on invalid coordinates indices or empty layouts`() {
        val layout = hostTableLayout(
            playerCount = 5,
            constraints = portraitConstraints,
        )

        assertThrows(IllegalArgumentException::class.java) {
            nearestHostTableRingIndex(
                layout = layout,
                pointerX = Float.NaN,
                pointerY = 0f,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            nearestHostTableRingIndex(
                layout = hostTableLayout(0, portraitConstraints),
                pointerX = 0f,
                pointerY = 0f,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            reorderHostTableItems(
                items = listOf("Alice", "Bob"),
                fromIndex = -1,
                targetIndex = 0,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            reorderHostTableItems(
                items = listOf("Alice", "Bob"),
                fromIndex = 0,
                targetIndex = 2,
            )
        }
    }
}
