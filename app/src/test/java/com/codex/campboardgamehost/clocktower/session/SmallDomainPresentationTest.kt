package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class SmallDomainPresentationTest {
    private data class Candidate(
        val id: String,
        val value: String,
    )

    @Test
    fun `small numeric domain exposes recommended primary plus every remaining legal value`() {
        val zero = Candidate("zero", "0")
        val one = Candidate("one", "1")
        val two = Candidate("two", "2")

        val presentation = SmallDomainPresentation.from(
            legalCandidates = listOf(zero, one, two),
            recommendedCandidateIds = listOf("one"),
            candidateId = Candidate::id,
        )

        assertSame(one, presentation.primary)
        assertEquals(listOf(zero, two), presentation.remaining)
        assertEquals(listOf(zero, one, two), presentation.selectable)
    }

    @Test
    fun `provider ordering changes primary without changing the selectable legal domain`() {
        val no = Candidate("no", "No")
        val yes = Candidate("yes", "Yes")
        val legal = listOf(no, yes)

        val yesFirst = SmallDomainPresentation.from(
            legalCandidates = legal,
            recommendedCandidateIds = listOf("yes", "no"),
            candidateId = Candidate::id,
        )
        val noFirst = SmallDomainPresentation.from(
            legalCandidates = legal,
            recommendedCandidateIds = listOf("no", "yes"),
            candidateId = Candidate::id,
        )

        assertSame(yes, yesFirst.primary)
        assertEquals(listOf(no), yesFirst.remaining)
        assertEquals(legal, yesFirst.selectable)

        assertSame(no, noFirst.primary)
        assertEquals(listOf(yes), noFirst.remaining)
        assertEquals(legal, noFirst.selectable)
    }

    @Test
    fun `recommendation absence leaves every legal value selectable`() {
        val zero = Candidate("zero", "0")
        val one = Candidate("one", "1")
        val two = Candidate("two", "2")
        val legal = listOf(zero, one, two)

        val presentation = SmallDomainPresentation.from(
            legalCandidates = legal,
            recommendedCandidateIds = emptyList(),
            candidateId = Candidate::id,
        )

        assertNull(presentation.primary)
        assertEquals(legal, presentation.remaining)
        assertEquals(legal, presentation.selectable)
    }

    @Test
    fun `unknown recommendation cannot expand or replace the legal domain`() {
        val no = Candidate("no", "No")
        val yes = Candidate("yes", "Yes")
        val legal = listOf(no, yes)

        val presentation = SmallDomainPresentation.from(
            legalCandidates = legal,
            recommendedCandidateIds = listOf("synthetic", "yes"),
            candidateId = Candidate::id,
        )

        assertSame(yes, presentation.primary)
        assertEquals(listOf(no), presentation.remaining)
        assertEquals(legal, presentation.selectable)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate legal candidate identity is rejected`() {
        SmallDomainPresentation.from(
            legalCandidates = listOf(
                Candidate("same", "0"),
                Candidate("same", "1"),
            ),
            recommendedCandidateIds = listOf("same"),
            candidateId = Candidate::id,
        )
    }
}
