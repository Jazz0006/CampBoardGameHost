package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerVirginPreflightProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `virgin execution preflights before spy registration event publication`() {
        val signature = hostSource
            .substringAfter("internal fun ClocktowerJudgeScreen(")
            .substringBefore(") {")
        assertTrue(signature.contains("onPreflightVirginExecution: (String, Boolean) -> Unit"))

        val pendingNominationFlow = hostSource
            .substringAfter("ClocktowerPendingNominationTableScreen(")
            .substringBefore("onCancel =")
        val nominationPreflightIndex = pendingNominationFlow.indexOf("onPreflightVirginExecution(")
        val nominationRegistrationIndex = pendingNominationFlow.indexOf("recordSpyRegistration(")
        assertTrue(nominationPreflightIndex >= 0)
        assertTrue(nominationRegistrationIndex > nominationPreflightIndex)

        val manualVirginFlow = hostSource
            .substringAfter("label = text(\"圣女能力\", \"Virgin ability\")")
            .substringBefore("enabled = nominatorName != null && nomineeName != null && gameOutcome == null")
        val manualPreflightIndex = manualVirginFlow.indexOf("onPreflightVirginExecution(")
        val manualRegistrationIndex = manualVirginFlow.indexOf("recordSpyRegistration(")
        assertTrue(manualPreflightIndex >= 0)
        assertTrue(manualRegistrationIndex > manualPreflightIndex)

        val preflightWiring = appSource
            .substringAfter("onPreflightVirginExecution =")
            .substringBefore("onVirginNomination =")
        assertTrue(preflightWiring.contains("preflightClocktowerPublicAliveObservation("))
        assertTrue(
            preflightWiring.contains(
                "eventSequence = clocktowerEventCounter + if (spyRegistrationWillRecord) 2 else 1",
            ),
        )

        val virginMutation = appSource
            .substringAfter("onVirginNomination =")
            .substringBefore("onAdvanceFromFirstNight =")
        assertFalse(virginMutation.contains("preflightClocktowerPublicAliveObservation("))
    }
}
