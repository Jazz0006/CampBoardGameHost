package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerGlobalFirstNightScalarDecisionTest {
    private val cards = (1..8).map { PlayerCard("P$it", Role.Civilian, "") }
    private val revision = StorytellerDecisionRevision(4, 2)

    private fun step(
        seat: Int,
        role: String,
        proposition: InformationProposition,
        reliability: InformationReliability = InformationReliability.RELIABLE,
        manual: List<ClocktowerDisplayOption> = emptyList(),
    ) = ClocktowerNightStepUi(
        title = role, actor = cards[seat - 1], isRealAction = true, reason = "",
        storytellerAction = "", tellPlayer = null, explanation = "",
        action = if (role == "Fortune Teller") ClocktowerNightAction.FortuneTeller
            else ClocktowerNightAction.None,
        roleEnName = role, displayProposition = proposition,
        informationReliability = reliability,
        manualInformationCandidates = manual,
        numericMinimumValue = 0, numericMaximumValue = 3,
    )

    private fun candidate(
        value: Int, metric: NumericMetric, source: Int, subjects: List<Int>,
        name: String,
    ) = ClocktowerDisplayOption(
        label = name, displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Number", displayPrimary = value.toString(),
        displaySecondary = null, displayFooter = null,
        proposition = InformationProposition.NumericResult(metric, source, subjects, value),
    )

    private fun build(
        step: ClocktowerNightStepUi, selected: List<String> = emptyList(),
        phase: ClocktowerPhase = ClocktowerPhase.FirstNight,
    ) = pendingGlobalFirstNightScalarDecision(
        step, phase, 1, 4, cards, "game-810", revision, selected,
    )

    private fun values(decision: PendingGlobalScalarInformationDecision) =
        decision.legalCandidates.map {
            (it.payload as StorytellerProviderCandidatePayloadV1.ScalarResult).value
        }.toSet()

    @Test
    fun `reliable numeric without registration offers only Foundation legal truth`() {
        val chef = step(1, "Chef", InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..8).toList(), 1,
        ))
        val pending = requireNotNull(build(chef))
        assertEquals(StorytellerProviderScalarKindV1.NUMBER, pending.kind)
        assertEquals(setOf("1"), values(pending))
        assertEquals(1, pending.legalCandidates.size)
    }

    @Test
    fun `result first multiple registration witnesses collapse to observable values`() {
        val metric = NumericMetric.ADJACENT_EVIL_PAIRS
        val subjects = (1..8).toList()
        val options = listOf(
            candidate(0, metric, 1, subjects, "witness-a"),
            candidate(1, metric, 1, subjects, "witness-b"),
            candidate(1, metric, 1, subjects, "witness-c"),
        )
        val chef = step(1, "Chef",
            InformationProposition.NumericResult(metric, 1, subjects, 0),
            manual = options,
        )
        val pending = requireNotNull(build(chef))
        assertEquals(setOf("0", "1"), values(pending))
        assertEquals(2, pending.legalCandidates.size)
        assertEquals(2, pending.legalCandidates.map { it.candidateId }.toSet().size)
    }

    @Test
    fun `poisoned Empath uses full Foundation numeric domain not curated list`() {
        val empath = step(2, "Empath", InformationProposition.NumericResult(
            NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 1,
        ), reliability = InformationReliability.POISONED)
        val pending = requireNotNull(build(empath))
        assertEquals(setOf("0", "1", "2"), values(pending))
        assertTrue(pending.legalCandidates.isNotEmpty())
    }

    @Test
    fun `Fortune Teller requires completed exact player pair before exposing truth domain`() {
        val ft = step(3, "Fortune Teller", InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 3, listOf(1, 8), true,
        ), reliability = InformationReliability.POISONED)
        assertNull(build(ft))
        assertNull(build(ft, listOf("P1")))
        assertNull(build(ft, listOf("P1", "P7")))
        val pending = requireNotNull(build(ft, listOf("P1", "P8")))
        assertEquals(StorytellerProviderScalarKindV1.BOOLEAN, pending.kind)
        assertEquals(setOf("true", "false"), values(pending))
        assertEquals(listOf(1, 8), pending.subjectSeats)
    }

    @Test
    fun `does not infer an information decision from a nonexistent or completed phase actor`() {
        val chef = step(1, "Chef", InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..8).toList(), 0,
        ))
        assertNull(build(chef.copy(actor = null)))
        assertNull(build(chef.copy(isRealAction = false)))
        assertNull(build(chef, phase = ClocktowerPhase.Day))
    }
}
