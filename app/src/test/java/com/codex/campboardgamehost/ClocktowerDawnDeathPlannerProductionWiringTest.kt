package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SNE-7.4F temporary production-ownership RED for the first Dawn closeout slice.
 *
 * Typed Mayor/night-death resolution already belongs to NightDawnResolutionPlanner. The App still
 * owns durable death materialization, records, events, phase changes, and later Dawn concerns. This
 * guard proves only that onConfirmNight consumes the existing typed death plan instead of retaining
 * a parallel Mayor redirect resolver. Retire it once a callable App/session Dawn transaction seam
 * supersedes this source-level ownership proof.
 */
class ClocktowerDawnDeathPlannerProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    private val confirmNightBlock = appSource
        .substringAfter("onConfirmNight = {")
        .substringBefore("onShowResults = {")

    @Test
    fun `night confirmation delegates validated Mayor and death planning to Dawn planner`() {
        assertTrue(
            "onConfirmNight must consume the existing typed Dawn death planner.",
            confirmNightBlock.contains("NightDawnResolutionPlanner.planValidatedNightDeath("),
        )
        assertTrue(
            "Dawn planning must reuse the shared checkpoint projection rather than inventing another snapshot owner.",
            confirmNightBlock.contains("currentClocktowerNightCheckpoint()"),
        )
    }

    @Test
    fun `App no longer independently owns Mayor redirect target legality during Dawn planning`() {
        assertFalse(
            "Mayor redirect target validation inside onConfirmNight duplicates NightDawnResolutionPlanner authority.",
            confirmNightBlock.contains("MayorRedirectLegality.canReceiveRedirect("),
        )
    }

    @Test
    fun `durable night death materialization remains at the App transaction boundary`() {
        assertTrue(
            "This slice must not move public death materialization into the pure planner.",
            confirmNightBlock.contains("cards[index] = nightDeathCard.copy(eliminatedRound = round)"),
        )
        assertTrue(
            "Durable death timeline/event ownership remains in App for this slice.",
            confirmNightBlock.contains("ClocktowerEventType.Death"),
        )
    }
}
