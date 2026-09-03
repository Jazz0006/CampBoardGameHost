package com.codex.campboardgamehost

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
}
