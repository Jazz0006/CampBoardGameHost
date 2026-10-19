package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerSlayerDraftTest {
    @Test fun `retarget and deselect are player-selected facts`() {
        val draft = ClocktowerSlayerDraft()
        draft.selectSeat("Alice")
        draft.selectSeat("Bob")
        draft.selectSeat("Carol")
        assertEquals("Alice", draft.claimantName)
        assertEquals("Carol", draft.targetName)
        draft.selectSeat("Carol")
        assertNull(draft.targetName)
        draft.clear()
        assertNull(draft.claimantName)
    }

    @Test fun `confirmed result is forwarded unchanged without role verdict`() {
        for (actualDeath in listOf(false, true)) {
            val draft = ClocktowerSlayerDraft()
            draft.selectSeat("Alice")
            draft.selectSeat("Bob")
            assertTrue(draft.confirm(actualDeath) { actor, target, died ->
                assertEquals("Alice", actor)
                assertEquals("Bob", target)
                assertEquals(actualDeath, died)
            })
            assertNull(draft.claimantName)
            assertNull(draft.targetName)
        }
    }

    @Test fun `incomplete input cannot commit and failed commit retains draft`() {
        val draft = ClocktowerSlayerDraft()
        assertFalse(draft.confirm(false) { _, _, _ -> fail() })
        draft.selectSeat("Alice")
        assertFalse(draft.confirm(true) { _, _, _ -> fail() })
        draft.selectSeat("Bob")
        assertThrows(IllegalStateException::class.java) {
            draft.confirm(true) { _, _, _ -> error("Session rejected") }
        }
        assertEquals("Alice", draft.claimantName)
        assertEquals("Bob", draft.targetName)
    }
}

