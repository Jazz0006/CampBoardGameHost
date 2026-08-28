package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary GCR-1 production ownership guard.
 *
 * Current-Demon behavior is proved by typed authority tests. This source check only protects the
 * non-callable Compose Host boundary until a presentation/materialization seam exists.
 */
class ClocktowerCurrentDemonProductionWiringTest {
    @Test
    fun `host consumes canonical current Demon context`() {
        val source = File(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        ).readText(Charsets.UTF_8)

        assertTrue(
            "Host Demon presentation/action ownership must consume the canonical current-Demon context.",
            source.contains("resolveCurrentDemonHostContext("),
        )
    }
}
