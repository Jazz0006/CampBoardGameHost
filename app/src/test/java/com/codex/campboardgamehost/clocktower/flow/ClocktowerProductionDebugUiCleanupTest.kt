package com.codex.campboardgamehost.clocktower.flow

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class ClocktowerProductionDebugUiCleanupTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production debug build no longer exposes developer diagnostic buttons in host flow`() {
        listOf(
            "Show developer diagnostics",
            "Hide developer diagnostics",
            "Run A4 benchmark (11 samples)",
            "Run A4 prewarm cancellation probe",
            "Run unified setup selector benchmark (11 samples)",
            "Run first-night unified pool benchmark (11 samples)",
        ).forEach { label ->
            assertFalse("Developer diagnostic button must be removed from host UI: $label", source.contains(label))
        }
    }
}
