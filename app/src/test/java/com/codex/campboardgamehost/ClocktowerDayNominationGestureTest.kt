package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerDayNominationGestureTest {
    @Test
    fun `only living players nominate but dead players remain nominees`() {
        val script = ClocktowerScript.TroubleBrewing
        val defs = clocktowerRoleDefinitionsForScript(script).associateBy { it.id.value }
        val game = GameState(script.toRecommendationScriptId(),
            listOf("Imp", "Soldier", "Chef", "Saint", "Empath").mapIndexed { i, role ->
                val d = requireNotNull(defs[role])
                PlayerState(i + 1, "P${i + 1}", d.id, d.alignment, d.type, alive = i + 1 != 2)
            }, 81)

        val policy = clocktowerDayNominationGesturePolicy(clocktowerDayOverviewTableState(game))

        assertEquals(setOf(1, 3, 4, 5).map(::ClocktowerSeatId).toSet(), policy.sourceSeatIds)
        assertEquals((1..5).map(::ClocktowerSeatId).toSet(), policy.targetSeatIds)
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
}
