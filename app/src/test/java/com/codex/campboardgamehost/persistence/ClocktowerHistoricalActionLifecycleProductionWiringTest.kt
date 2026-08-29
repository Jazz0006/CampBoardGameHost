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
    fun `klutz continuation records phase boundaries and poison expiry`() {
        val klutz = appSource
            .substringAfter("onConfirmKlutzChoice =")
            .substringBefore("onSelectArtistClaimant =")

        assertTrue(klutz.contains("recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)"))
        assertTrue(klutz.contains("recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)"))
        assertTrue(klutz.contains("recordClocktowerAction(ActionFactDraft.Poison("))
    }

    @Test
    fun `virgin immediate execution records the transition into the next night`() {
        val virgin = appSource
            .substringAfter("onVirginNomination =")
            .substringBefore("onAdvanceFromFirstNight =")

        assertTrue(virgin.contains("recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)"))
        assertTrue(virgin.contains("recordClocktowerAction(ActionFactDraft.Poison("))
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
}
