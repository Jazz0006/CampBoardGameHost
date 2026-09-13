package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerHostBottomInsetPolicyTest {
    @Test
    fun `immersive host bottom actions reserve only visible navigation bars`() {
        assertEquals(
            ClocktowerHostBottomInsetPolicy.VisibleNavigationBars,
            clocktowerHostBottomInsetPolicy(),
        )
    }
}
