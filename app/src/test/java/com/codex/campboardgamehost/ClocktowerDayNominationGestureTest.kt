package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerDayNominationGestureTest {
    @Test
    fun `day nomination gesture only exposes alive typed seats`() {
        val tableState = ClocktowerDayOverviewTableState(
            seats = listOf(
                seat(ClocktowerSeatId(3), "Casey", isAlive = true),
                seat(ClocktowerSeatId(1), "Alice", isAlive = true),
                seat(ClocktowerSeatId(2), "Bob", isAlive = false),
            ),
            interaction = HostTableInteractionState(mode = HostTableInteractionMode.ReadOnly),
        )

        val policy = clocktowerDayNominationGesturePolicy(tableState)

        assertEquals(setOf(ClocktowerSeatId(1), ClocktowerSeatId(3)), policy.sourceSeatIds)
        assertEquals(setOf(ClocktowerSeatId(1), ClocktowerSeatId(3)), policy.targetSeatIds)
    }

    @Test
    fun `directional drag resolves against the rendered slot ring and rejects self or illegal targets`() {
        val layout = hostTableLayout(
            playerCount = 5,
            constraints = hostTableSurfaceLayoutConstraints(
                availableWidth = 420f,
                availableHeight = 820f,
            ),
        )
        val legalTargets = setOf(2, 3, 4)
        val target = layout.slots[3]

        assertEquals(
            3,
            resolveHostTableDirectionalTargetRingIndex(
                layout = layout,
                pointerX = target.centerX,
                pointerY = target.centerY,
                sourceRingIndex = 0,
                eligibleTargetRingIndices = legalTargets,
            ),
        )

        val self = layout.slots[0]
        assertNull(
            resolveHostTableDirectionalTargetRingIndex(
                layout = layout,
                pointerX = self.centerX,
                pointerY = self.centerY,
                sourceRingIndex = 0,
                eligibleTargetRingIndices = legalTargets + 0,
            ),
        )

        val illegal = layout.slots[1]
        assertNull(
            resolveHostTableDirectionalTargetRingIndex(
                layout = layout,
                pointerX = illegal.centerX,
                pointerY = illegal.centerY,
                sourceRingIndex = 0,
                eligibleTargetRingIndices = legalTargets,
            ),
        )
    }

    private fun seat(
        seatId: ClocktowerSeatId,
        name: String,
        isAlive: Boolean,
    ): HostSeatPresentation = HostSeatPresentation(
        seatId = seatId,
        playerName = name,
        isAlive = isAlive,
    )
}
