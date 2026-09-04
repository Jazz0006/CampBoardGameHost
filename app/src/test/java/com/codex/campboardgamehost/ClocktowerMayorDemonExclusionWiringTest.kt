package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Coarse production ownership guard only. Mayor redirect legality and Demon exclusion are proved by
 * typed rule/planner tests; this file protects the remaining non-callable Host/UI consumption seam.
 */
class ClocktowerMayorDemonExclusionWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val nightUiSource = File(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production Mayor selection consumes rules-owned legal target set`() {
        assertTrue(hostSource.contains("MayorRedirectLegality.canReceiveRedirect"))
        assertTrue(hostSource.contains("mayorRedirectTargetCards"))
        assertTrue(
            "Square-table Mayor selection must consume the rules-owned redirect target set.",
            nightUiSource.contains("selectableSeatNumbers(mayorRedirectTargetCards)"),
        )
        assertFalse(
            "Manual Mayor legality must not be reconstructed from assisted recommendations.",
            nightUiSource.contains("assistedDecisionOptions.map(ClocktowerDecisionOption::targetName)"),
        )
    }
}
