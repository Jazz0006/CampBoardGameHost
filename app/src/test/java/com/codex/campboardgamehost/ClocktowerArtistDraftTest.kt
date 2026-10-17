package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerArtistDraftTest {
    @Test fun `changing claimant or truth clears dependent answers`() {
        val draft = ClocktowerArtistDraft()
        draft.selectClaimant("Alice")
        draft.selectTruthfulAnswer(true)
        draft.selectShownAnswer(false)
        draft.selectTruthfulAnswer(false)
        assertEquals("Alice", draft.claimantName)
        assertEquals(false, draft.truthfulAnswer)
        assertNull(draft.shownAnswer)
        draft.selectShownAnswer(true)
        draft.selectClaimant("Bob")
        assertEquals("Bob", draft.claimantName)
        assertNull(draft.truthfulAnswer)
        assertNull(draft.shownAnswer)
        draft.selectClaimant(null)
        assertNull(draft.claimantName)
    }

    @Test fun `incomplete or rejected confirmation retains draft and cannot silently accept`() {
        val draft = ClocktowerArtistDraft()
        var calls = 0
        assertThrows(IllegalArgumentException::class.java) { draft.confirm { calls++ } }
        draft.selectClaimant("Alice")
        draft.selectTruthfulAnswer(true)
        assertThrows(IllegalArgumentException::class.java) { draft.confirm { calls++ } }
        assertEquals(0, calls)
        draft.selectShownAnswer(false)
        assertThrows(IllegalStateException::class.java) { draft.confirm { error("Host rejected") } }
        assertEquals("Alice", draft.claimantName)
        assertEquals(true, draft.truthfulAnswer)
        assertEquals(false, draft.shownAnswer)
    }

    @Test fun `successful confirmation calls Host before clearing all answers`() {
        val draft = ClocktowerArtistDraft()
        draft.selectClaimant("Alice")
        draft.selectTruthfulAnswer(true)
        draft.selectShownAnswer(false)
        var accepted: String? = null
        draft.confirm {
            accepted = it
            assertEquals(true, draft.truthfulAnswer)
            assertEquals(false, draft.shownAnswer)
        }
        assertEquals("Alice", accepted)
        assertNull(draft.claimantName)
        assertNull(draft.truthfulAnswer)
        assertNull(draft.shownAnswer)
    }
}
