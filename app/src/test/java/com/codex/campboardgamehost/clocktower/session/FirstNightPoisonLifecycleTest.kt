package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Test

class FirstNightPoisonLifecycleTest {
    @Test fun `P1 draft target only advances input revision then confirmation invalidates all unopened generations`() {
        val draft = FirstNightPoisonLifecycle(4, 2).selectDraft(3)
        assertEquals(4, draft.gameStateRevision)
        assertEquals(3, draft.playerInputRevision)
        val confirmed = draft.confirm()
        assertEquals(5, confirmed.gameStateRevision)
        assertEquals(3, confirmed.playerInputRevision)
        assertEquals(2, confirmed.invalidatedGeneration)
    }

    @Test fun `P2 committed observations are outside poison lifecycle and cannot be rewritten by later target changes`() {
        val confirmed = FirstNightPoisonLifecycle(0, 0).selectDraft(2).confirm()
        val nextDraft = confirmed.selectDraft(4)
        assertEquals(2, confirmed.confirmedTargetSeat)
        assertEquals(2, nextDraft.confirmedTargetSeat)
        assertEquals(4, nextDraft.draftTargetSeat)
    }

    @Test fun `P3 repeated confirmation is idempotent while a changed target gets a new state revision`() {
        val first = FirstNightPoisonLifecycle(0, 0).selectDraft(2).confirm()
        assertEquals(first, first.confirm())
        val changed = first.selectDraft(5).confirm()
        assertEquals(2, changed.gameStateRevision)
        assertEquals(5, changed.confirmedTargetSeat)
    }
}
