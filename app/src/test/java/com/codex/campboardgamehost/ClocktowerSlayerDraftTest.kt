package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerSlayerDraftTest {
    @Test fun `retarget and deselect discard previous registration choice`() {
        val draft = ClocktowerSlayerDraft()
        draft.selectSeat("Alice")
        draft.selectSeat("Bob")
        draft.chooseRecluseRegistration(true)
        draft.selectSeat("Carol")
        assertEquals("Alice", draft.claimantName)
        assertEquals("Carol", draft.targetName)
        assertNull(draft.recluseRegistersDemon)
        draft.chooseRecluseRegistration(false)
        draft.selectSeat("Carol")
        assertNull(draft.targetName)
        assertNull(draft.recluseRegistersDemon)
        draft.clear()
        assertNull(draft.claimantName)
    }

    @Test fun `only healthy Recluse receives explicit choice and success clears draft`() {
        for (healthy in listOf(false, true)) for (choice in listOf(null, false, true)) {
            val draft = ClocktowerSlayerDraft()
            draft.selectSeat("Alice")
            draft.selectSeat("Bob")
            choice?.let(draft::chooseRecluseRegistration)
            var calls = 0
            assertTrue(draft.confirm(healthy) { actor, target, registration ->
                calls++
                assertEquals("Alice", actor)
                assertEquals("Bob", target)
                assertEquals(if (healthy) choice else null, registration)
                assertEquals("Bob", draft.targetName)
            })
            assertEquals(1, calls)
            assertNull(draft.claimantName)
            assertNull(draft.targetName)
            assertNull(draft.recluseRegistersDemon)
        }
    }

    @Test fun `incomplete shot is not submitted and Host failure preserves draft`() {
        val draft = ClocktowerSlayerDraft()
        assertFalse(draft.confirm(true) { _, _, _ -> fail() })
        draft.selectSeat("Alice")
        assertFalse(draft.confirm(true) { _, _, _ -> fail() })
        draft.selectSeat("Bob")
        draft.chooseRecluseRegistration(true)
        assertThrows(IllegalStateException::class.java) {
            draft.confirm(true) { _, _, _ -> error("Host rejected") }
        }
        assertEquals("Alice", draft.claimantName)
        assertEquals("Bob", draft.targetName)
        assertEquals(true, draft.recluseRegistersDemon)
    }
}
