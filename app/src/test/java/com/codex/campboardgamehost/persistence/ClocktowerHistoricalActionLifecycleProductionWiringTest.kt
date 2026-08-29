package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerHistoricalActionLifecycleProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `klutz continuation records phase boundaries and routes poison expiry before next night`() {
        val klutz = appSource
            .substringAfter("onConfirmKlutzChoice =")
            .substringBefore("onSelectArtistClaimant =")

        assertTrue(klutz.contains("recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)"))
        assertPoisonExpiryBeforeNextNight(klutz)
    }

    @Test
    fun `virgin immediate execution routes poison expiry before transition into next night`() {
        val virgin = appSource
            .substringAfter("onVirginNomination =")
            .substringBefore("onAdvanceFromFirstNight =")

        assertPoisonExpiryBeforeNextNight(virgin)
    }

    @Test
    fun `night resolution records klutz phase and typed poison lifecycle changes`() {
        val night = appSource
            .substringAfter("onConfirmNight =")
            .substringBefore("onShowResults =")

        assertTrue(night.contains("recordClocktowerPhaseAdvance(ClocktowerPhase.Day)"))

        assertTrue(
            night.contains("NightDawnPoisonRecoveryAuthority.latestTargetSeatForRound("),
        )
        assertTrue(
            night.contains("NightDawnResolutionPlanner.planPoisonCarry("),
        )
        assertTrue(
            night.contains("intent = DawnCommitIntent(poisonCarry = poisonIntent)"),
        )
        assertTrue(
            night.contains("poisonMaterialization.actionIdToCommit?.let { actionId ->"),
        )
        assertTrue(
            night.contains("ActionFactDraft.Poison("),
        )

        assertFalse(night.contains("poisonCarriedIntoTomorrow"))
        assertFalse(night.contains("poison-after-night"))
    }

    @Test
    fun `slayer death publishes the same public alive observation as other deaths`() {
        val slayer = appSource
            .substringAfter("onSlayerShot =")
            .substringBefore("onPreflightVirginExecution =")

        assertTrue(slayer.contains("preflightClocktowerPublicAliveObservation("))
        assertTrue(slayer.contains("recordClocktowerAction(ActionFactDraft.Death("))
        assertTrue(slayer.contains("recordEpistemicObservation(EpistemicObservationDraft("))
        assertTrue(slayer.contains("InformationProposition.AliveAt(targetSeat, false)"))
    }

    private fun assertPoisonExpiryBeforeNextNight(block: String) {
        val expiryIndex = block.indexOf("materializeClocktowerPoisonExpiryAtDusk()")
        val nextNightIndex = block.indexOf("recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)")

        assertTrue("Expected typed dusk poison expiry owner in Day -> Night path.", expiryIndex >= 0)
        assertTrue("Expected next-Night phase transition in Day -> Night path.", nextNightIndex >= 0)
        assertTrue(
            "Poison expiry must materialize before the following Night phase transition.",
            expiryIndex < nextNightIndex,
        )
        assertFalse(block.contains("kind = \"poison-expire\""))
    }
}
