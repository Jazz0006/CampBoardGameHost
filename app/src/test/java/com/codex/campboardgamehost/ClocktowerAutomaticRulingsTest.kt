package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerAutomaticRulingsTest {
    @Test
    fun `automatic rulings advance only after the rule-owned target is applied`() {
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = null,
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Bob",
                automaticTargetName = "Alice",
            ),
        )
        assertTrue(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
        assertFalse(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = false,
                action = ClocktowerNightAction.MayorRedirect,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
        assertTrue(
            clocktowerAutomaticMayorRulingShouldAdvance(
                automaticStorytellerInfo = true,
                action = ClocktowerNightAction.DemonSuccessor,
                selectedName = "Alice",
                automaticTargetName = "Alice",
            ),
        )
    }
}
