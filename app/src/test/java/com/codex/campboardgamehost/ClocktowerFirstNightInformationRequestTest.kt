package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerFirstNightInformationRequestTest {
    private val cards = listOf("Investigator", "Chef", "Empath", "Poisoner", "Imp").mapIndexed { index, name ->
        val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).single { it.enName == name }
        PlayerCard("P${index + 1}", clocktowerRole = role, clocktowerTeam = role.team)
    }

    private fun step(role: String = "Chef") = ClocktowerNightStepUi(
        title = "information", actor = cards[0], isRealAction = true, reason = "",
        storytellerAction = "", tellPlayer = "2", explanation = "", roleEnName = role,
        displayKind = ClocktowerDisplayKind.Number,
    )

    private fun request(step: ClocktowerNightStepUi, phase: ClocktowerPhase = ClocktowerPhase.FirstNight) =
        clocktowerFirstNightInformationRequest(step, phase, 1, cards)

    @Test fun `non-first-night and non-actor displays do not publish an observation`() {
        assertNull(request(step(), ClocktowerPhase.Night))
        assertNull(request(step().copy(actor = null)))
        assertNull(request(step().copy(actor = cards[0].copy(name = "absent"))))
        assertNull(request(step("Monk")))
    }

    @Test fun `manual numeric publication records exactly the shown number, not claimed truth`() {
        val result = requireNotNull(request(step().copy(
            informationReliability = InformationReliability.POISONED,
            selectedInformationTruthful = false,
        )))
        val observed = result.candidates.single().observation
        assertEquals(result.selectedCandidateId, result.candidates.single().id)
        assertEquals(2, observed.shownNumber)
        assertEquals(ReliabilityState.POISONED, observed.reliability)
        assertEquals(SemanticTruth.NOT_APPLICABLE, observed.semanticTruth)
    }

    @Test fun `pair publication accepts structurally valid displayed seats without Rules verdict`() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(4, RoleId("Poisoner")),
            InformationProposition.RoleAt(2, RoleId("Poisoner")),
        ))
        val result = requireNotNull(request(step("Investigator").copy(
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayPrimary = "Poisoner",
            displaySecondary = "1 / 3",
            displayProposition = proposition,
        )))
        assertEquals(listOf(2, 4), result.candidates.single().observation.candidateSeats)
        assertEquals(RoleId("Poisoner"), result.candidates.single().observation.shownRole)
        assertEquals(SemanticTruth.NOT_APPLICABLE, result.candidates.single().observation.semanticTruth)
        assertEquals(emptyList<Any>(), result.candidates.single().observation.registrations)
    }

    @Test fun `malformed seat claims cannot be committed`() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(2, RoleId("Poisoner")),
            InformationProposition.RoleAt(99, RoleId("Poisoner")),
        ))
        assertNull(request(step("Investigator").copy(
            displayKind = ClocktowerDisplayKind.EitherOne, displayProposition = proposition,
        )))
    }
}

