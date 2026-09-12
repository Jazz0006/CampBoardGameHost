package com.codex.campboardgamehost

import kotlin.test.Test
import kotlin.test.assertEquals

class ClocktowerDawnPresentationPolicyTest {
    @Test
    fun `completed dawn is not exposed as a standalone host screen`() {
        assertEquals(
            ClocktowerPhase.Day,
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
