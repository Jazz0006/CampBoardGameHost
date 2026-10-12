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

    @Test fun typedPresentationRoleIdDoesNotChangeUnrelatedLegacyCandidateIds() {
        val existing = display(1)
        val baseline = clocktowerInformationCandidateId(existing)
        val extended = clocktowerInformationCandidateId(existing.copy(
            presentationRoleId = "Recluse"))
        assertEquals("$baseline|shownRole:Recluse", extended)
        assertEquals(baseline, clocktowerInformationCandidateId(existing.copy(
            presentationRoleId = null)))
    }

    @Test fun semanticRoleResultMatchesOnlyOneSafeHostOptionWithoutFixingRegistration() {
        val decision = decision().copy(
            kind = StorytellerProviderScalarKindV1.ROLE,
            metric = "REVEALED_CHARACTER", subjectSeats = listOf(4),
            legalCandidates = listOf(StorytellerProviderCandidateV1(
                "role-result:4:Recluse",
                StorytellerProviderCandidatePayloadV1.ScalarResult("Recluse"))),
        )
        fun roleOption(witness: Boolean? = null) = ClocktowerDisplayOption(
            label = "Recluse", displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = "role", displayPrimary = "Recluse",
            displaySecondary = null, displayFooter = null,
            proposition = InformationProposition.RoleAt(4, RoleId("Recluse")),
            recluseRegistersEvil = witness)
        assertEquals("Recluse", ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision, "role-result:4:Recluse", listOf(roleOption()))?.displayPrimary)
        val misinformation = roleOption().copy(proposition = null,
            presentationSubjectSeats = listOf(4), presentationRoleId = "Recluse")
        assertNull(misinformation.proposition) // never creates a false canonical observation
        assertEquals("Recluse", ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision, "role-result:4:Recluse", listOf(misinformation))?.displayPrimary)
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision, "role-result:4:Recluse", listOf(roleOption(true))))
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision, "role-result:4:Recluse", listOf(roleOption(), roleOption())))
        assertNull(ClocktowerResidualDisplayMatcherV2.matchScalar(
            decision, "role-result:4:Recluse", listOf(roleOption().copy(
                proposition = InformationProposition.RoleAt(5, RoleId("Recluse"))))))
    }

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
