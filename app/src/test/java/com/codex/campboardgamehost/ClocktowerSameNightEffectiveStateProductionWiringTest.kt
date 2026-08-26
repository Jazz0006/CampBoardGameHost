package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * RED production-wiring contracts for same-night mechanical state.
 *
 * These tests deliberately assert the required authority boundary against the current production
 * source. They must fail by JUnit assertion on the pre-fix implementation; production code is not
 * changed in this checkpoint.
 */
class ClocktowerSameNightEffectiveStateProductionWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val nightStepUiSource = File(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `same-night mechanics must not use public eliminated state as the current alive authority`() {
        assertFalse(
            "Night deaths are mechanically effective before dawn, so later-night mechanics cannot " +
                "derive the current alive set only from PlayerCard.eliminatedRound.",
            hostSource.contains("val aliveCards = cards.filter { it.eliminatedRound == null }"),
        )
    }

    @Test
    fun `later normal actor eligibility must consume effective same-night state`() {
        val roleActor = hostSource
            .substringAfter("fun roleActor(enName: String): PlayerCard? =")
            .substringBefore("fun roleMissingReason(enName: String)")

        assertTrue(
            "Normal actor eligibility must query the effective state at the role interaction BEFORE " +
                "boundary, while preserving the existing impairment subject composition.",
            roleActor.contains("effectiveNightStateAt") &&
                roleActor.contains("ClocktowerInteractionBoundary.BEFORE") &&
                roleActor.contains("isMechanicallyAlive"),
        )
    }

    @Test
    fun `Empath living neighbours must use the state at the Empath interaction cursor`() {
        assertFalse(
            "If the Demon killed an Empath neighbour earlier tonight, structured Empath information " +
                "must skip that mechanically dead player instead of recomputing neighbours from " +
                "the public cards snapshot.",
            nightStepUiSource.contains("livingNeighbors(cards, actor.name)"),
        )
    }

    @Test
    fun `confirmed poison must not remain active after the Poisoner loses the ability`() {
        val effectivePoisonHelper = hostSource
            .substringAfter("fun effectiveNightStateAt(")
            .substringBefore("val fortuneTellerRecluseRegistrationKey")

        assertTrue(
            "Production must derive poison at the requested interaction cursor: the helper must " +
                "consume the confirmed target, canonical effective night state, Poisoner ability " +
                "functioning semantics, and PoisonEffectLifecycle rather than treating the raw " +
                "confirmed target as current poison authority.",
            hostSource.contains("effectivePoisonTargetAt(") &&
                effectivePoisonHelper.contains("PoisonEffectLifecycle.") &&
                effectivePoisonHelper.contains("effectiveNightStateAt") &&
                effectivePoisonHelper.contains("AbilityFunctioningSemantics.functionsAs") &&
                effectivePoisonHelper.contains("Poisoner"),
        )
    }

    @Test
    fun `Fortune Teller truthful result must still detect a dead Demon`() {
        val fortuneTellerTruth = hostSource
            .substringAfter("val fortuneTellerMatched =")
            .substringBefore("val fortuneTellerResult =")

        assertFalse(
            "Fortune Teller may choose living or dead players; a dead Demon must still produce Yes, " +
                "so truthful detection cannot search only aliveCards.",
            fortuneTellerTruth.contains("aliveCards.any") ||
                fortuneTellerTruth.contains("publicAliveCards.any"),
        )
    }

    @Test
    fun `Butler target contract must allow a dead Master`() {
        val butlerTargetUi = nightStepUiSource
            .substringAfter("ClocktowerNightAction.ButlerMaster -> {")
            .substringBefore("ClocktowerNightAction.MonkProtect")

        assertFalse(
            "The Butler may choose a dead player as Master; Butler target legality must not be " +
                "implemented as an alive-only candidate list.",
            butlerTargetUi.contains("cards = aliveCards.filter"),
        )
    }

    @Test
    fun `death-trigger exception remains explicit for Ravenkeeper`() {
        assertTrue(
            "Ravenkeeper is a death-trigger exception: fixing normal actor eligibility must preserve " +
                "the explicit resolved-night-death trigger path instead of globally suppressing dead actors.",
            hostSource.contains("nightDeathWillOccur") &&
                hostSource.contains("\"Ravenkeeper\"") &&
                hostSource.contains("ravenkeeperTrigger"),
        )
    }
}
