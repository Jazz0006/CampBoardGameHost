package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerFirstNightPlayerRevealHandoffTest {
    @Test fun `manual pair already committed at lifecycle boundary still opens reveal without duplicate publication`() {
        val handoff = resolveClocktowerPlayerRevealHandoff(
            publicationAllowed = true,
            firstNightPublicationCreated = false,
        )

        assertTrue(handoff.openReveal)
        assertFalse(handoff.recordPublication)
    }

    @Test fun `fresh first night publication records once and opens reveal`() {
        val handoff = resolveClocktowerPlayerRevealHandoff(
            publicationAllowed = true,
            firstNightPublicationCreated = true,
        )

        assertTrue(handoff.openReveal)
        assertTrue(handoff.recordPublication)
    }

    @Test fun `publication guard denial blocks both publication and reveal`() {
        val handoff = resolveClocktowerPlayerRevealHandoff(
            publicationAllowed = false,
            firstNightPublicationCreated = true,
        )

        assertFalse(handoff.openReveal)
        assertFalse(handoff.recordPublication)
    }
    @Test fun `fresh publication executes private observation then marker then reveal`() {
        val events = mutableListOf<String>()
        val result = performClocktowerPlayerRevealHandoff(
            authorize = { events += "authorize"; true },
            publicationNeeded = { events += "needed"; true },
            recordPrivateInformation = { events += "private" },
            markFirstNightPublished = { events += "mark" },
            openReveal = { events += "reveal" },
        )
        assertEquals(listOf("authorize", "needed", "private", "mark", "reveal"), events)
        assertEquals(ClocktowerPlayerRevealHandoff(true, true), result)
    }

    @Test fun `reopening a published decision does not repeat observation`() {
        val events = mutableListOf<String>()
        var published = false
        repeat(2) {
            performClocktowerPlayerRevealHandoff(
                authorize = { true },
                publicationNeeded = { !published },
                recordPrivateInformation = { events += "private" },
                markFirstNightPublished = { published = true },
                openReveal = { events += "reveal" },
            )
        }
        assertEquals(listOf("private", "reveal", "reveal"), events)
    }

    @Test fun `authorization denial does not invoke any downstream effect`() {
        val result = performClocktowerPlayerRevealHandoff(
            authorize = { false },
            publicationNeeded = { error("must not prepare") },
            recordPrivateInformation = { error("must not record private") },
            markFirstNightPublished = { error("must not mark") },
            openReveal = { error("must not reveal") },
        )
        assertEquals(ClocktowerPlayerRevealHandoff(false, false), result)
    }

    @Test fun `effect failures propagate without later effects or invented rollback`() {
        val stages = listOf("authorize", "needed", "private", "mark", "reveal")
        for (failedStage in stages) {
            val events = mutableListOf<String>()
            val failure = IllegalStateException(failedStage)
            fun effect(stage: String) {
                events += stage
                if (stage == failedStage) throw failure
            }
            try {
                performClocktowerPlayerRevealHandoff(
                    authorize = { effect("authorize"); true },
                    publicationNeeded = { effect("needed"); true },
                    recordPrivateInformation = { effect("private") },
                    markFirstNightPublished = { effect("mark") },
                    openReveal = { effect("reveal") },
                )
                error("Expected effect failure")
            } catch (actual: IllegalStateException) {
                assertSame(failure, actual)
            }
            assertEquals(stages.take(stages.indexOf(failedStage) + 1), events)
        }
    }

    @Test fun `failed observation does not consume first-night marker and same decision may retry`() {
        var marked = false
        var observationAttempts = 0
        val events = mutableListOf<String>()
        repeat(2) { attempt ->
            try {
                performClocktowerPlayerRevealHandoff(
                    authorize = { true },
                    publicationNeeded = { !marked },
                    recordPrivateInformation = {
                        observationAttempts++
                        if (attempt == 0) error("observation did not persist")
                        events += "observed"
                    },
                    markFirstNightPublished = { marked = true; events += "marked" },
                    openReveal = { events += "revealed" },
                )
            } catch (failure: IllegalStateException) {
                assertEquals("observation did not persist", failure.message)
            }
            if (attempt == 0) {
                assertFalse(marked)
                assertTrue(events.isEmpty())
            }
        }
        assertTrue(marked)
        assertEquals(2, observationAttempts)
        assertEquals(listOf("observed", "marked", "revealed"), events)
    }

    @Test fun `publication keeps revision safety without candidate authorization`() {
        val revision = StorytellerDecisionRevision(5, 6)
        assertTrue(clocktowerInformationPublicationAllowed(null, revision))
        assertTrue(clocktowerInformationPublicationAllowed(revision, revision))
        for (current in listOf(revision.copy(gameStateRevision = 7), revision.copy(playerInputRevision = 7))) {
            val result = performClocktowerPlayerRevealHandoff(
                authorize = { clocktowerInformationPublicationAllowed(revision, current) },
                publicationNeeded = { error("stale publication") },
                recordPrivateInformation = { error("stale observation") },
                markFirstNightPublished = { error("stale mark") },
                openReveal = { error("stale reveal") })
            assertFalse(result.openReveal)
        }
    }
}
