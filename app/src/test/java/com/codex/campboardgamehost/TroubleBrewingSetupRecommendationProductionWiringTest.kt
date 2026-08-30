package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary TBSP-6G-B production-wiring guard.
 *
 * Exact-request cache behavior is owned by the typed session tests. This coarse source guard exists
 * only because the Compose App/Host lifecycle is not yet a callable typed seam. Retire it once the
 * reveal-to-Judge owner is extracted.
 */
class TroubleBrewingSetupRecommendationProductionWiringTest {
    @Test
    fun `committed Trouble Brewing deal dispatches prewarm only after entering reveal`() {
        val source = File(
            "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
        ).readText()

        assertTrue(source.contains("troubleBrewingSetupRecommendationRevealCoordinator.onCommittedDeal("))
        assertTrue(source.contains("launch(Dispatchers.Default)"))
    }

    @Test
    fun `Judge first setup consumer uses the shared exact request result provider`() {
        val appSource = File(
            "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
        ).readText()
        val hostSource = File(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        ).readText()

        assertTrue(
            appSource.contains(
                "setupRecommendationResultFor = troubleBrewingSetupRecommendationRevealCoordinator::resultFor",
            ),
        )
        assertTrue(hostSource.contains("setupRecommendationResultFor"))
        assertTrue(hostSource.contains("setupRecommendationResultFor?.invoke(setupRecommendationRequest)"))
    }
}
