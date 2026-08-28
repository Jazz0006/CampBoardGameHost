package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse ownership guard for the non-callable Compose Host restore boundary.
 *
 * Reconstruction and succession semantics are proved by typed tests. This check only ensures the
 * production Host consumes reconstructed events from the restored unfinished-night checkpoint.
 */
class ClocktowerNightRestoreProductionOwnershipTest {
    @Test
    fun `Host consumes canonical night reconstruction from restored checkpoint`() {
        val hostSource = File(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        ).readText(Charsets.UTF_8)

        val projectionBlock = hostSource
            .substringAfter("val otherNightCanonicalInteractionIds =")
            .substringBefore("fun effectiveNightStateAt(")

        assertTrue(projectionBlock.contains("NightTransactionReconstructor.reconstruct("))
        assertTrue(projectionBlock.contains("checkpoint = nightCheckpoint"))
    }
}
