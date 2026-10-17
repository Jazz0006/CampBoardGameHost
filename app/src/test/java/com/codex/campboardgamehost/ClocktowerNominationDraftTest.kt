package com.codex.campboardgamehost

import org.junit.Assert.*
import org.junit.Test

class ClocktowerNominationDraftTest {
    private val vote = ClocktowerConfirmedVoteRecord(emptyList())

    @Test fun `successful vote passes the pending pair before clearing it`() {
        val draft = ClocktowerNominationDraft()
        draft.select("Alice", "Bob")
        var calls = 0
        draft.confirmVote(vote) { source, target, record ->
            calls++
            assertEquals("Alice", source)
            assertEquals("Bob", target)
            assertSame(vote, record)
            assertEquals("Alice", draft.nominatorName)
            assertEquals("Bob", draft.nomineeName)
        }
        assertEquals(1, calls)
        assertNull(draft.nominatorName)
        assertNull(draft.nomineeName)
    }

    @Test fun `rejected vote retains pair for retry and missing pair cannot confirm`() {
        val draft = ClocktowerNominationDraft()
        assertThrows(IllegalArgumentException::class.java) { draft.confirmVote(vote) { _, _, _ -> fail() } }
        draft.select("Alice", "Bob")
        assertThrows(IllegalStateException::class.java) { draft.confirmVote(vote) { _, _, _ -> error("Host rejected") } }
        assertEquals("Alice", draft.nominatorName)
        assertEquals("Bob", draft.nomineeName)
        draft.clear()
        assertNull(draft.nominatorName)
        assertNull(draft.nomineeName)
    }
}
