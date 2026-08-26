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

    @Test
    fun `Spy and Recluse registration poison status must use the querying interaction cursor`() {
        val registrationHelpers = hostSource
            .substringAfter("fun spyCanRegister")
            .substringBefore("val firstNightWasherwoman")

        assertFalse(
            "Registration poison status must receive the querying role/interaction identity; " +
                "a generic registration helper must not silently query the Spy cursor.",
            registrationHelpers.contains("effectivePoisonForRole(\"Spy\")"),
        )
    }

    @Test
    fun `durable private information reliability must use effective cursor state`() {
        val publication = hostSource
            .substringAfter("fun recordReliablePrivateInformation")
            .substringBefore("val informationStepBuilder")

        assertFalse(
            "Durable night information publication must share the interaction-specific effective " +
                "reliability authority and must not use raw confirmed poison directly.",
            publication.contains("actor.name == poisonTarget"),
        )
    }

    @Test
    fun `registration-free night steps must not require a role identity`() {
        assertTrue(
            "Registration-free system steps must not evaluate roleEnName eagerly. Registration " +
                "authority must be entered only when a registration key exists, while a keyed " +
                "registration still requires an explicit querying role.",
            hostSource.contains("spyRegistrationGood = if (currentStep.spyRegistrationKey != null && currentStep.roleEnName != null)") &&
                hostSource.contains("spyCanRegister = if (currentStep.spyRegistrationKey != null && currentStep.roleEnName != null)") &&
                hostSource.contains("recluseRegistrationEvil = if (currentStep.recluseRegistrationKey != null && currentStep.roleEnName != null)") &&
                hostSource.contains("recluseCanRegister = if (currentStep.recluseRegistrationKey != null && currentStep.roleEnName != null)") &&
                hostSource.contains("currentStep.spyRegistrationKey?.let { key ->") &&
                hostSource.contains("currentStep.recluseRegistrationKey?.let { key ->"),
        )
    }

    @Test
    fun `Chambermaid targets must use effective alive state at its interaction cursor`() {
        assertTrue(
            "Chambermaid target legality must project mechanically alive cards at the Chambermaid " +
                "interaction BEFORE cursor in both Host call sites, rather than reusing generic " +
                "public alive cards.",
            hostSource.contains("val chambermaidInteractionId =") &&
                hostSource.contains("RoleId(\"Chambermaid\")") &&
                hostSource.contains("ClocktowerInteractionBoundary.BEFORE") &&
                hostSource.contains("val chambermaidTargetCards =") &&
                hostSource.contains("chambermaidTargetCards = chambermaidTargetCards") &&
                nightStepUiSource.contains("chambermaidTargetCards:") &&
                nightStepUiSource.contains("val candidates = chambermaidTargetCards.filter"),
        )
    }

    @Test
    fun `Ravenkeeper death trigger must override the later normal actor lookup`() {
        val ravenkeeperMaterializer = hostSource
            .substringAfter("identity = ClocktowerProductionNightStepIdentity.role(RoleId(\"Ravenkeeper\"))")
            .substringBefore("identity = ClocktowerProductionNightStepIdentity.role(RoleId(\"Spy\"))")

        assertTrue(
            "Ravenkeeper must pass the resolved death trigger actor explicitly to the information " +
                "builder instead of relying on roleActor at the later post-death cursor.",
            ravenkeeperMaterializer.contains("val trigger = requireNotNull(ravenkeeperTrigger)") &&
                ravenkeeperMaterializer.contains("actorOverride = trigger"),
        )
    }

    @Test
    fun `Sage death trigger must override the later normal actor lookup`() {
        val sageMaterializer = hostSource
            .substringAfter("identity = ClocktowerProductionNightStepIdentity.role(RoleId(\"Sage\"))")
            .substringBefore("identity = ClocktowerProductionNightStepIdentity.role(RoleId(\"Ravenkeeper\"))")

        assertTrue(
            "Sage must pass the explicit Demon-killed trigger actor to the information builder " +
                "instead of relying on roleActor at the later post-death cursor.",
            sageMaterializer.contains("val sageDemon = requireNotNull(demonCard)") &&
                sageMaterializer.contains("val trigger = requireNotNull(sageNightDeath)") &&
                sageMaterializer.contains("actorOverride = trigger"),
        )
    }

    @Test
    fun `death trigger ability state must use the mechanical death BEFORE cursor`() {
        assertTrue(
            "Death-trigger ability state must use the resolved mechanical death interaction BEFORE " +
                "cursor, including cursor-relative poison, rather than the later role cursor.",
            hostSource.contains("fun deathTriggerAbilityState") &&
                hostSource.contains("effectiveAt.interactionId") &&
                hostSource.contains("effectivePoisonTargetAt(") &&
                hostSource.contains("AbilityFunctioningSemantics.stateFor") &&
                hostSource.contains("ClocktowerInteractionBoundary.BEFORE"),
        )
    }
}
