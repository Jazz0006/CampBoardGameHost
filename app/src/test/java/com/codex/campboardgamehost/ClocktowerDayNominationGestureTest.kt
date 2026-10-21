package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerDayNominationGestureTest {
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
}
