package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerDawnExactDemonSuccessorWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Imp self kill defers exact confirmed successor materialization until Dawn`() {
        val nightResolution = appSource
            .substringAfter("var newDemonName: String? = null")
            .substringBefore("val nightOutcome =")

        assertTrue(
            "Imp self-kill night resolution must use the exact confirmed successor fact.",
            nightResolution.contains("clocktowerConfirmedDemonSuccessorTarget"),
        )
        assertFalse(
            "The editable successor draft must not be Dawn mechanical authority.",
            nightResolution.contains("preferredMinionName = clocktowerDemonSuccessorTarget"),
        )
        assertTrue(
            "A missing confirmation with a living Minion must fail closed instead of silently finalizing Dawn.",
            nightResolution.contains("unresolvedDemonSuccessor") &&
                nightResolution.contains("ClocktowerTeam.Minion"),
        )

        val nightOutcomeAndAdvance = appSource
            .substringAfter("val nightOutcome =")
            .substringBefore("onConfirmNewDemon = {")

        assertTrue(
            "A pending exact successor must prevent premature Good-win evaluation before Dawn materialization.",
            nightOutcomeAndAdvance.contains("newDemonName == null"),
        )
        assertTrue(
            "An unresolved required successor must not advance directly to Dawn.",
            nightOutcomeAndAdvance.contains("!unresolvedDemonSuccessor"),
        )

    }
}
