package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production ownership guard for the non-callable Host information-step adapter.
 *
 * Information semantics belong to typed builder/decision/recommendation tests. This source check
 * only ensures the Host continues routing generic information-step construction through the typed
 * builder instead of reintroducing a parallel inline implementation.
 */
class ClocktowerInformationStepBuilderOwnershipTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Host routes generic information steps through typed builder`() {
        assertTrue(hostSource.contains("ClocktowerInformationStepBuilder("))
        assertTrue(hostSource.contains("informationStepBuilder.build("))
    }
}
