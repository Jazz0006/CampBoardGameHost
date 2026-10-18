package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightPublicationResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Both outcomes are legal in the same state; this is a cross-role contract, not a Chef heuristic. */
class ClocktowerChefAlternativePublicationTest {
    @Test fun actualNumbersCanPassRevealWithoutTruthOrRegistrationCalculation() {
        // Ordinary evil players (Baron/Imp) are not adjacent; the Recluse next
        // to the Imp can legally register good (Chef 0) or evil (Chef 1).
        val cards = listOf("Chef", "Butler", "Baron", "Fortune Teller", "Imp", "Recluse")
            .mapIndexed { index, name ->
                val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
                    .single { it.enName == name }
                PlayerCard("S${index + 1}",
                    clocktowerRole = role, clocktowerTeam = role.team)
            }
        val step = ClocktowerNightStepUi(
            title = "Chef", actor = cards.first(), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "0", explanation = "",
            roleEnName = "Chef", displayKind = ClocktowerDisplayKind.Number,
            informationQuestion = ClocktowerInformationQuestion.Number(NumericMetric.ADJACENT_EVIL_PAIRS, (1..cards.size).toList()),
        )
        for (value in listOf(0, 1, 99)) {
            val displayed = requireNotNull(clocktowerDirectNumericResult(step, 1, value, cards))
            val request = requireNotNull(clocktowerFirstNightInformationRequest(
                displayed, ClocktowerPhase.FirstNight, 1, cards,
            ))
            val migration = FirstNightInformationMigration()
            val publication = migration.resolvePublication(request)
            assertTrue(publication is FirstNightPublicationResolution.Published)
            val gate = (publication as FirstNightPublicationResolution.Published).migration
            assertTrue(gate.isDisplayed(request.decisionId))
            assertEquals(value, request.candidates.single { it.id == request.selectedCandidateId }.observation.shownNumber)

        }
    }
}
