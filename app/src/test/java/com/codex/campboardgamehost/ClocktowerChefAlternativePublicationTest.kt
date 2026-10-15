package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationResolutionStatusV1
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightPublicationResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Both outcomes are legal in the same state; this is a cross-role contract, not a Chef heuristic. */
class ClocktowerChefAlternativePublicationTest {
    @Test fun bothRecluseRegistrationResultsCanPassRevealAndUnresolvedWitnessPlanning() {
        // Ordinary evil players (Baron/Imp) are not adjacent; the Recluse next
        // to the Imp can legally register good (Chef 0) or evil (Chef 1).
        val cards = listOf("Chef", "Butler", "Baron", "Fortune Teller", "Imp", "Recluse")
            .mapIndexed { index, name ->
                val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
                    .single { it.enName == name }
                PlayerCard("S${index + 1}", Role.Civilian, "",
                    clocktowerRole = role, clocktowerTeam = role.team)
            }
        val choices = ClocktowerRegistrationResultPresentation.numericOptions(
            title = "Chef",
            sourceSeat = 1,
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = (1..cards.size).toList(),
            footer = "Adjacent evil pairs",
            witnesses = listOf(
                ClocktowerAlignmentRegistrationWitness(null, false),
                ClocktowerAlignmentRegistrationWitness(null, true),
            ),
        ) { witness ->
            chefEvilPairs(cards) { card ->
                isClocktowerEvil(card) ||
                    (card.clocktowerRole?.enName == "Recluse" &&
                        witness.recluseRegistersEvil == true)
            }
        }
        assertEquals(setOf("0", "1"), choices.map { it.displayPrimary }.toSet())
        val step = ClocktowerNightStepUi(
            title = "Chef", actor = cards.first(), isRealAction = true,
            reason = "", storytellerAction = "", tellPlayer = "0", explanation = "",
            roleEnName = "Chef", displayKind = ClocktowerDisplayKind.Number,
            manualInformationCandidates = choices,
            legacyInformationCandidates = choices,
        )
        for (choice in choices) {
            val displayed = resolveClocktowerPlayerDisplay(step, choice)
            val request = requireNotNull(clocktowerFirstNightInformationRequest(
                displayed, ClocktowerPhase.FirstNight, 1, cards,
                ClocktowerScript.TroubleBrewing, 42L, null, "en",
            ))
            val migration = FirstNightInformationMigration()
            val publication = migration.resolvePublication(request)
            assertTrue(publication is FirstNightPublicationResolution.Published)
            val gate = (publication as FirstNightPublicationResolution.Published).migration
            assertTrue(gate.isDisplayed(request.decisionId))
            assertEquals(choice.displayPrimary?.toInt(), request.candidates.single { it.id == request.selectedCandidateId }.observation.shownNumber)
            val plan = clocktowerPlanConfirmedResultRegistrations(
                shownProposition = displayed.displayProposition,
                shownKind = displayed.displayKind, shownPrimary = displayed.displayPrimary,
                legalCandidates = choices,
                spy = null, spySeat = null, spyQuestion = RegistrationQuestion.ALIGNMENT,
                recluse = null, recluseSeat = 6, recluseQuestion = RegistrationQuestion.ALIGNMENT,
            )
            assertTrue(plan is ClocktowerResultRegistrationPlanV1.Ready)
            val ready = plan as ClocktowerResultRegistrationPlanV1.Ready
            assertEquals(1, ready.choices.size)
            assertEquals(RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
                ready.choices.single().status)
            assertEquals(1, ready.legalResultWitnesses.size)
        }
    }
}
