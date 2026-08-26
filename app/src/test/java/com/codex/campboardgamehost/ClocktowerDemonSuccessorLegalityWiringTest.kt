package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerDemonSuccessorLegalityWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    private val nightStepUiSource = File(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `successor legality is projected from Demon succession resolution`() {
        val successionBlock = hostSource
            .substringAfter("val demonSuccessionResolution =")
            .substringBefore("val sageNightDeath =")

        assertTrue(
            "Production must project a legal target-seat set from DemonSuccessionResolution.",
            successionBlock.contains("val demonSuccessorTargetSeats = when") &&
                successionBlock.contains("DemonSuccessionResolution.None ->") &&
                successionBlock.contains("is DemonSuccessionResolution.Forced ->") &&
                successionBlock.contains("setOf(resolution.targetSeat)") &&
                successionBlock.contains("is DemonSuccessionResolution.Choice ->") &&
                successionBlock.contains("resolution.targetSeats"),
        )
        assertTrue(
            "The successor interaction must exist exactly when the rules authority exposes legal targets.",
            successionBlock.contains("demonSuccessorTargetSeats.isNotEmpty()"),
        )
    }

    @Test
    fun `successor recommendation is constrained by legal target seats`() {
        val recommendationBlock = hostSource
            .substringAfter("fun demonSuccessorDecisionOptions")
            .substringBefore("fun recommendedPairInformationOptions")

        assertTrue(
            "The recommendation adapter must receive the rules-owned legal target seats.",
            recommendationBlock.contains("legalTargetSeats: Set<Int>"),
        )
        assertTrue(
            "A recommendation outside the legal target set must be discarded.",
            recommendationBlock.contains("choice.targetSeat !in legalTargetSeats") &&
                recommendationBlock.contains("return@mapNotNull null"),
        )
        assertTrue(
            "The DemonSuccessor materializer must pass the rules-owned target seats into recommendation.",
            hostSource.contains("demonSuccessorDecisionOptions(\n                            demonSuccessorTargetSeats") ||
                hostSource.contains("demonSuccessorDecisionOptions(demonSuccessorTargetSeats)"),
        )
    }

    @Test
    fun `manual successor UI uses legal target cards rather than recommendation shortlist`() {
        assertTrue(
            "ClocktowerNightStepCardLocalized must receive the complete legal successor target cards.",
            nightStepUiSource.contains("demonSuccessorTargetCards: List<PlayerCard>"),
        )

        val successorUiBlock = nightStepUiSource
            .substringAfterLast("ClocktowerNightAction.DemonSuccessor ->")
            .substringBefore("ClocktowerNightAction.Ravenkeeper ->")

        assertTrue(
            "Manual successor selection must render the rules-owned legal target cards directly.",
            successorUiBlock.contains("cards = demonSuccessorTargetCards"),
        )
        assertFalse(
            "Recommendation output must not define successor legality.",
            successorUiBlock.contains("val legalNames = assistedDecisionOptions") ||
                successorUiBlock.contains("it.name in legalNames"),
        )
    }

    @Test
    fun `both Host render paths pass successor legal target cards`() {
        assertEquals(
            "Both ClocktowerNightStepCardLocalized call sites must receive the same rules-owned successor targets.",
            2,
            hostSource.windowed(
                size = "demonSuccessorTargetCards = demonSuccessorTargetCards".length,
                step = 1,
                partialWindows = false,
            ).count { it == "demonSuccessorTargetCards = demonSuccessorTargetCards" },
        )
    }

    @Test
    fun `automatic mode can select a sole legal successor without recommendation coverage`() {
        assertTrue(
            "Automatic mode needs a rules fallback when the legal successor set is a singleton.",
            nightStepUiSource.contains("val automaticDecisionTargetName =") &&
                nightStepUiSource.contains("demonSuccessorTargetCards") &&
                nightStepUiSource.contains("singleOrNull()") &&
                nightStepUiSource.contains("step.action == ClocktowerNightAction.DemonSuccessor"),
        )
        assertTrue(
            "Auto-selection must consume the resolved target name, not require a recommendation object.",
            nightStepUiSource.contains("automaticDecisionTargetName != null") &&
                nightStepUiSource.contains("onSelectName(automaticDecisionTargetName)"),
        )
    }
}
