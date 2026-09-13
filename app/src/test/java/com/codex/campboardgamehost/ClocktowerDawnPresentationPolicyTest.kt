package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerDawnPresentationPolicyTest {
    @Test
    fun `dawn remains a standalone host screen until announcement is complete`() {
        assertEquals(
            ClocktowerPhase.Dawn,
            clocktowerVisibleHostPhase(ClocktowerPhase.Dawn),
        )
    }

    @Test
    fun `non dawn host phases retain their visible phase`() {
        assertEquals(ClocktowerPhase.FirstNight, clocktowerVisibleHostPhase(ClocktowerPhase.FirstNight))
        assertEquals(ClocktowerPhase.Day, clocktowerVisibleHostPhase(ClocktowerPhase.Day))
        assertEquals(ClocktowerPhase.Night, clocktowerVisibleHostPhase(ClocktowerPhase.Night))
    }
}
