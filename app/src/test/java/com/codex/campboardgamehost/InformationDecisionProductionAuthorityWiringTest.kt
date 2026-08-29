package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Coarse Host publication-ownership guard only. Confirmation freshness, candidate legality and
 * revision semantics are proved by typed InformationDecision tests.
 */
class InformationDecisionProductionAuthorityWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `durable private information publication remains confirmation-authorized`() {
        assertTrue(hostSource.contains("informationDecisionPublicationAllowed("))
        assertTrue(hostSource.contains("confirmation.authorizes("))
        assertTrue(hostSource.contains("recordReliablePrivateInformation(displayStep)"))
        assertFalse(
            "A prepared draft alone must not become publication authority.",
            hostSource.contains("informationDecisionDraft != null"),
        )
    }
}
