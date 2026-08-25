package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class InformationDecisionProductionAuthorityWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val nightStepUiSource = File(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `bare information decision draft cannot authorize durable private observation`() {
        val privateProducer = hostSource
            .substringAfter("fun recordReliablePrivateInformation(")
            .substringBefore("val undertakerTarget =")
        val commitAuthority = privateProducer
            .substringBefore("onRecordEpistemicObservation(EpistemicObservationDraft(")

        assertFalse(
            "A prepared InformationDecision draft is only a candidate/display artifact; " +
                "durable publication must require explicit confirmation authority.",
            commitAuthority.contains("informationDecisionDraft != null"),
        )
    }

    @Test
    fun `structured information display must not strip confirmation down to a naked draft`() {
        assertFalse(
            "The InformationDecision path must preserve immutable confirmation provenance through " +
                "the display boundary instead of retaining only EpistemicObservationDraft.",
            nightStepUiSource.contains("informationDecisionDraft = confirmed.draft"),
        )
    }
}
