package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1 ownership/order regression: every Day -> Night poison expiry entry point must share one typed
 * owner and finish expiry materialization before the following Night phase becomes durable.
 */
class DuskPoisonExpiryOwnershipTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `app root no longer owns dynamic poison-expire history identity`() {
        assertFalse(
            "App root must not generate retry-unsafe poison-expire action IDs.",
            appSource.contains("kind = \"poison-expire\""),
        )
    }

    @Test
    fun `all three next-night entry points share dusk expiry before phase advance`() {
        val klutz = appSource
            .substringAfter("onConfirmKlutzChoice =")
            .substringBefore("onSelectArtistClaimant =")
        val virgin = appSource
            .substringAfter("onVirginNomination =")
            .substringBefore("onAdvanceFromFirstNight =")
        val normalDay = appSource
            .substringAfter("onConfirmDay =")
            .substringBefore("onConfirmNight =")

        listOf(
            "Klutz continuation" to klutz,
            "Virgin immediate execution" to virgin,
            "normal Day confirmation" to normalDay,
        ).forEach { (label, block) ->
            val expiryIndex = block.indexOf("materializeClocktowerPoisonExpiryAtDusk()")
            val phaseIndex = block.indexOf("recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)")
            assertTrue("$label must call the shared dusk poison expiry owner.", expiryIndex >= 0)
            assertTrue("$label must advance into the next Night.", phaseIndex >= 0)
            assertTrue(
                "$label must finish poison expiry before recording the next-Night phase boundary.",
                expiryIndex < phaseIndex,
            )
        }
    }

    @Test
    fun `shared dusk helper owns recovery planner history and mechanical convergence`() {
        val helper = appSource
            .substringAfter("fun materializeClocktowerPoisonExpiryAtDusk()")
            .substringBefore("fun recordClocktowerPhaseAdvance(")

        assertTrue(helper.contains("DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound("))
        assertTrue(helper.contains("DuskPoisonExpiryMaterializationPlanner.plan("))
        assertTrue(helper.contains("DuskPoisonExpiryMaterializationState("))
        assertTrue(helper.contains("materialization.actionIdToCommit?.let { actionId ->"))
        assertTrue(helper.contains("ActionFactDraft.Poison("))
        assertTrue(helper.contains("if (materialization.stateMutationRequired)"))
    }
}
