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
        legalCandidates = listOf(
            StorytellerProviderCandidateV1("n0", StorytellerProviderCandidatePayloadV1.ScalarResult("0")),
            StorytellerProviderCandidateV1("n1", StorytellerProviderCandidatePayloadV1.ScalarResult("1")),
        ),
    )

    private fun display(value: Int, explicitWitness: Boolean = false) =
        ClocktowerDisplayOption(
            label = "$value", displayKind = ClocktowerDisplayKind.Number,
            displayTitle = "number", displayPrimary = "$value",
            displaySecondary = null, displayFooter = null,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = value),
            spyRegistersGood = if (explicitWitness) true else null)

    @Test fun onlyUniqueTypedHostLegalCurrentOptionCanBePreselected() {
        val chosen = ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision(), "n1", listOf(display(0), display(1)))
        assertEquals("1", chosen?.displayPrimary)
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision(), "unknown", listOf(display(0), display(1))))
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision(), "n1", listOf(display(0), display(1), display(1))))
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision(), "n1", listOf(display(1, explicitWitness = true))))
    }
}
