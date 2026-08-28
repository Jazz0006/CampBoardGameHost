package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse SNE-7.9E ownership guard for the non-callable Compose Host boundary.
 *
 * Succession semantics are proved by typed reconstruction/restore tests. This guard only ensures
 * production Host consumes that authority instead of rebuilding a Choice-only parallel projection.
 */
class ClocktowerNightRestoreProductionOwnershipTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Host consumes reconstructed succession events instead of confirmed choice projection`() {
        val projectionBlock = hostSource
            .substringAfter("val otherNightCanonicalInteractionIds =")
            .substringBefore("fun effectiveNightStateAt(")

        assertTrue(
            "Host must derive same-night succession mechanics through the canonical reconstructor.",
            projectionBlock.contains("NightTransactionReconstructor.reconstruct("),
        )
        assertTrue(
            "Host chronology projection must consume the reconstructor's canonical events.",
            projectionBlock.contains("confirmedEvents"),
        )
        assertTrue(
            "Host reconstruction must consume the App-restored durable checkpoint.",
            projectionBlock.contains("checkpoint = nightCheckpoint"),
        )
        assertFalse(
            "Host must not retain confirmed-target-only succession authority; Forced succession has no confirmed choice.",
            projectionBlock.contains("val confirmedDemonSuccessorSeat ="),
        )
    }
}
