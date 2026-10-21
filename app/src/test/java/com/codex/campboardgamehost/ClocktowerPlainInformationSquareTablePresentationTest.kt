package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPlainInformationSquareTablePresentationTest {
    private val unconfirmed = ClocktowerNightStepUi(
        title = "Empath",
        actor = PlayerCard("Alice"),
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = "Undetermined",
        explanation = "",
        roleEnName = "Empath",
        displayKind = ClocktowerDisplayKind.Number,
        displayPrimary = "Undetermined",
    )

    @Test fun `automatic plain fallback waits instead of showing untyped placeholder`() {
        val automatic = requireNotNull(clocktowerPlainInformationSquareTablePresentation(
            unconfirmed, actorSeat = 1, wakeInstruction = "Wake", automaticOnly = true,
        ))
        assertFalse(automatic.showPlayerDisplayAction)
        assertTrue(automatic.waitingForAi)

        val manual = requireNotNull(clocktowerPlainInformationSquareTablePresentation(
            unconfirmed, actorSeat = 1, wakeInstruction = "Wake", automaticOnly = false,
        ))
        assertFalse(manual.showPlayerDisplayAction)
        assertFalse(manual.waitingForAi)
    }

    @Test fun `typed Host information can be shown immediately without AI`() {
        val typed = unconfirmed.copy(
            displayProposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), 0,
            ),
            displayPrimary = "0",
        )
        val automatic = clocktowerPlainInformationSquareTablePresentation(
            typed, actorSeat = 1, wakeInstruction = "Wake", automaticOnly = true,
        )
        assertNotNull(automatic)
        assertTrue(automatic!!.showPlayerDisplayAction)
        assertFalse(automatic.waitingForAi)
    }
}
