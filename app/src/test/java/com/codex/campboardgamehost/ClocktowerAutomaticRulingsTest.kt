package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerAutomaticRulingsTest {
    @Test
    fun `automatic rulings advance only after the rule-owned target is applied`() {
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                allowRuleDeterminedAutoUi = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = null,
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                allowRuleDeterminedAutoUi = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Bob",
                automaticTargetName = "Alice",
            ),
        )
        assertTrue(
            clocktowerAutomaticMayorRulingShouldAdvance(
                allowRuleDeterminedAutoUi = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                allowRuleDeterminedAutoUi = false,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
        assertTrue(
            clocktowerAutomaticMayorRulingShouldAdvance(
                allowRuleDeterminedAutoUi = true,
                action = ClocktowerNightAction.DemonSuccessor,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
    }
}
