package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Narrow production ownership guards for same-night mechanical state.
 *
 * Gameplay semantics belong in typed behavior tests. These source checks are retained only where
 * they protect a coarse production-consumer boundary that is not yet callable as an integration
 * seam; they deliberately avoid exact formatting/local-expression shape.
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
    fun `later normal actor eligibility consumes the effective ability subject seam`() {
        val roleActor = hostSource
            .substringAfter("fun roleActor(enName: String): PlayerCard? {")
            .substringBefore("fun roleMissingReason(enName: String)")

        assertTrue(
            "Normal night actor eligibility must consume the effective subject seam so same-night " +
                "alive/current-role/poison state is applied at the interaction cursor.",
            roleActor.contains("effectiveAbilitySubjectForRole(enName, candidate)"),
        )
        assertFalse(
            "roleActor must not independently rebuild public-state ability semantics once the " +
                "effective subject seam owns that projection.",
            roleActor.contains("candidate.abilitySubject("),
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
        val lifecycleCalls = Regex("PoisonEffectLifecycle\\.").findAll(hostSource).count()

        assertTrue(
            "Production must consume PoisonEffectLifecycle source-lifetime semantics. A raw poison " +
                "confirmation and an effectively active poison are different facts.",
            lifecycleCalls > 0,
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
            fortuneTellerTruth.contains("aliveCards.any"),
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
