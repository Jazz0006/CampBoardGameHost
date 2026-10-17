package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerDirectBooleanResultTest {
    @Test fun `manual No is confirmable even when Host candidate list only contains Yes`() {
        val yes = InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 3), true,
        )
        val step = ClocktowerNightStepUi(
            title = "Fortune Teller", actor = PlayerCard("Alice"), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "Yes", explanation = "",
            action = ClocktowerNightAction.FortuneTeller,
            displayProposition = yes,
            manualInformationCandidates = listOf(
                ClocktowerDisplayOption("Yes", ClocktowerDisplayKind.YesNo, "Fortune Teller",
                    "Yes", null, null, proposition = yes),
            ),
        )
        val actual = requireNotNull(clocktowerDirectBooleanResult(step, 1, listOf(2, 3), false, "en"))
        assertEquals("No", actual.displayPrimary)
        val observation = actual.displayProposition as InformationProposition.BooleanResult
        assertEquals(false, observation.value)
        assertEquals(listOf(2, 3), observation.subjectSeats)
        assertNull(actual.selectedInformationTruthful)
        assertNull(clocktowerDirectBooleanResult(step, 1, listOf(2, 2), false, "en"))
        assertNull(clocktowerDirectBooleanResult(step, 1, listOf(2), false, "en"))
    }
}
