package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import org.junit.Assert.*
import org.junit.Test

class ClocktowerFirstNightInformationRequestTest {
    private val cards = listOf("Investigator", "Chef", "Empath", "Poisoner", "Imp").mapIndexed { index, name ->
        val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).single { it.enName == name }
        PlayerCard("P${index + 1}", Role.Civilian, "", clocktowerRole = role, clocktowerTeam = role.team)
    }
    private fun step(role: String = "Chef") = ClocktowerNightStepUi(
        title = "information", actor = cards[0], isRealAction = true, reason = "",
        storytellerAction = "", tellPlayer = "2", explanation = "", roleEnName = role,
        displayKind = ClocktowerDisplayKind.Number,
    )
    private fun request(step: ClocktowerNightStepUi, phase: ClocktowerPhase = ClocktowerPhase.FirstNight) =
        clocktowerFirstNightInformationRequest(step, phase, 3, cards, ClocktowerScript.TroubleBrewing,
            42L, null, "en", RecommendationStyle.BALANCED)

    @Test fun `request gating rejects non-first-night missing actor and unsupported family`() {
        assertNull(request(step(), ClocktowerPhase.Night))
        assertNull(request(step().copy(actor = null)))
        assertNull(request(step().copy(actor = cards[0].copy(name = "absent"))))
        assertNull(request(step("Monk")))
    }

    @Test fun `numeric request preserves identity fallback reliability and selected truth`() {
        val result = requireNotNull(request(step().copy(
            informationReliability = InformationReliability.POISONED, selectedInformationTruthful = false,
        )))
        assertEquals("first-night:FirstNight:3:CHEF:1", result.decisionId)
        val candidate = result.migratedCandidates.single()
        assertEquals(result.selectedCandidateId, candidate.id)
        assertEquals(result.legacyCandidates, result.migratedCandidates)
        assertEquals(2, candidate.observation.shownNumber)
        assertEquals(ReliabilityState.POISONED, candidate.observation.reliability)
        assertEquals(SemanticTruth.FALSE, candidate.observation.semanticTruth)
        assertEquals(900_000L, candidate.rankFixedPoint)
    }

    @Test fun `legacy duplicate preserves first option ranking and metadata`() {
        val option = ClocktowerDisplayOption("template", ClocktowerDisplayKind.Number, "information",
            "2", null, null, isDefaultRecommendation = true, reasonCodes = listOf("reason"))
        val result = requireNotNull(request(step().copy(legacyInformationCandidates = listOf(option, option))))
        assertEquals(1, result.legacyCandidates.size)
        assertEquals(1_000_000L, result.legacyCandidates.single().rankFixedPoint)
        assertEquals(listOf("reason"), result.legacyCandidates.single().reasonCodes)
    }

    @Test fun `pair request resolves structured legal observation independently of display seats`() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(4, RoleId("Poisoner")),
            InformationProposition.RoleAt(2, RoleId("Poisoner")),
        ))
        val result = requireNotNull(request(step("Investigator").copy(
            displayKind = ClocktowerDisplayKind.EitherOne, displayPrimary = "Poisoner",
            displaySecondary = "1 / 3", displayProposition = proposition,
        )))
        assertEquals(listOf(2, 4), result.migratedCandidates.single().observation.candidateSeats)
        assertEquals(RoleId("Poisoner"), result.migratedCandidates.single().observation.shownRole)
        assertEquals(SemanticTruth.TRUE, result.migratedCandidates.single().observation.semanticTruth)
        assertEquals(listOf(1, 3), result.legacyCandidates.single().observation.candidateSeats)
    }
}
