package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary GCR-1 production-wiring guard.
 *
 * Runtime current-Demon behavior is owned by typed authority tests. This coarse source guard exists
 * only because the Compose Host is not yet a callable materialization seam; retire it when that
 * presentation owner is extracted.
 */
class ClocktowerCurrentDemonProductionWiringTest {
    @Test
    fun `host consumes canonical current Demon context instead of local historical lookup`() {
        val source = File(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        ).readText()

        assertTrue(source.contains("resolveCurrentDemonHostContext("))
        assertFalse(
            source.contains(
                "val demonCard = cards.firstOrNull { it.clocktowerRole?.team == ClocktowerTeam.Demon }",
            ),
        )
        assertFalse(
            source.contains(
                "publicAliveCards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon }",
            ),
        )
        assertFalse(
            source.contains(
                "publicAliveCards.any { it.clocktowerTeam == ClocktowerTeam.Demon }",
            ),
        )
    }
}
