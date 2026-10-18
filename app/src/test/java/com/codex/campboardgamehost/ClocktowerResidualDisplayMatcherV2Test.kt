package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerResidualDisplayMatcherV2Test {
    private fun decision() = PendingGlobalScalarInformationDecision(
        requestIdentity = StorytellerDecisionRequestIdentity("same-game", "chef-legal"),
        revision = StorytellerDecisionRevision(3, 6),
        sourceSeat = 1,
        abilityRole = RoleId("Chef"),
        reliability = ReliabilityState.RELIABLE,
        kind = StorytellerProviderScalarKindV1.NUMBER,
        metric = NumericMetric.ADJACENT_EVIL_PAIRS.name,
        subjectSeats = emptyList(),

    )

    private fun display(value: Int, explicitWitness: Boolean = false) =
        ClocktowerDisplayOption(
            label = "$value", displayKind = ClocktowerDisplayKind.Number,
            displayTitle = "number", displayPrimary = "$value",
            displaySecondary = null, displayFooter = null,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = value),
            spyRegistersGood = if (explicitWitness) true else null)

    @Test fun typedPresentationRoleIdDoesNotChangeUnrelatedLegacyCandidateIds() {
        val existing = display(1)
        val baseline = clocktowerInformationCandidateId(existing)
        val extended = clocktowerInformationCandidateId(existing.copy(
            presentationRoleId = "Recluse"))
        assertEquals("$baseline|shownRole:Recluse", extended)
        assertEquals(baseline, clocktowerInformationCandidateId(existing.copy(
            presentationRoleId = null)))
    }

    @Test fun directNumberDoesNotDependOnHostMenuOrRegistrationWitness() {
        val advice = com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult(
            "SCALAR_RESULT", scalarValue = "99")
        val option = ClocktowerResidualDisplayMatcherV2.matchScalar(decision(), advice, emptyList())!!
        assertEquals(99, (option.proposition as InformationProposition.NumericResult).value)
        assertNull(option.spyRegistersGood)
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(decision(), advice.copy(scalarValue = "-1"), emptyList()))
        val role = decision().copy(kind = StorytellerProviderScalarKindV1.ROLE,
            metric = "REVEALED_CHARACTER", subjectSeats = listOf(4))
        val result = ClocktowerResidualDisplayMatcherV2.matchScalar(role, advice.copy(scalarValue = "Recluse"), emptyList())!!
        assertEquals(InformationProposition.RoleAt(4, RoleId("Recluse")), result.proposition)
        assertNull(result.recluseRegistersEvil)
    }
}
