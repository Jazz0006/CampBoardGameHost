package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvancePolicyTest {
    @Test
    fun `automatic red herring advance waits for a real recommendation but skips non-actions`() {
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = false,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }
}
