package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImpairedInformationSemanticsContractTest {
    @Test
    fun `poisoned information strongly prefers false regardless of game balance`() {
        val masses = RecommendationStyle.entries.flatMap { style ->
            listOf(-100, 0, 100).map { evilAdvantage ->
                DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = InformationReliability.POISONED,
                    style = style,
                    evilAdvantage = evilAdvantage,
                )
            }
        }

        assertTrue(
            "Poisoned information should strongly prefer false without making truth vanishingly rare.",
            masses.all { it in 875_000L..925_000L },
        )
    }

    @Test
    fun `drunk information strongly prefers false regardless of game balance`() {
        val masses = RecommendationStyle.entries.flatMap { style ->
            listOf(-100, 0, 100).map { evilAdvantage ->
                DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = InformationReliability.DRUNK,
                    style = style,
                    evilAdvantage = evilAdvantage,
                )
            }
        }

        assertTrue(
            "Drunk information should strongly prefer false without making truth vanishingly rare.",
            masses.all { it in 875_000L..925_000L },
        )
    }

    @Test
    fun `game balance cannot own the impaired truthful versus false boundary`() {
        RecommendationStyle.entries.forEach { style ->
            listOf(InformationReliability.DRUNK, InformationReliability.POISONED).forEach { reliability ->
                val goodAhead = DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = reliability,
                    style = style,
                    evilAdvantage = -100,
                )
                val evilAhead = DynamicCandidateGenerator.misinformationMassFixedPoint(
                    reliability = reliability,
                    style = style,
                    evilAdvantage = 100,
                )

                assertEquals(
                    "Balance may rank legal false candidates, but must not flip the reliability family budget.",
                    goodAhead,
                    evilAhead,
                )
            }
        }
    }

    @Test
    fun `healthy information never receives malfunction false probability`() {
        RecommendationStyle.entries.forEach { style ->
            listOf(-100, 0, 100).forEach { evilAdvantage ->
                assertEquals(
                    0L,
                    DynamicCandidateGenerator.misinformationMassFixedPoint(
                        reliability = InformationReliability.RELIABLE,
                        style = style,
                        evilAdvantage = evilAdvantage,
                    ),
                )
            }
        }
    }

    @Test
    fun `no legal false candidate produces an explicit truthful fallback budget`() {
        val budget = ImpairedInformationPolicy.familyBudget(
            reliability = InformationReliability.POISONED,
            hasTruthfulCandidate = true,
            hasFalseCandidate = false,
        )

        assertEquals(1_000_000L, budget.truthfulMassFixedPoint)
        assertEquals(0L, budget.falseMassFixedPoint)
        assertEquals(ImpairedInformationPolicyReason.NO_LEGAL_FALSE_CANDIDATE, budget.reason)
    }

    @Test
    fun `avoid exposing impairment is an explicit truthful exception`() {
        val budget = ImpairedInformationPolicy.familyBudget(
            reliability = InformationReliability.DRUNK,
            hasTruthfulCandidate = true,
            hasFalseCandidate = true,
            truthfulException = ImpairedTruthfulException.AVOID_EXPOSING_IMPAIRMENT,
        )

        assertEquals(1_000_000L, budget.truthfulMassFixedPoint)
        assertEquals(0L, budget.falseMassFixedPoint)
        assertEquals(ImpairedInformationPolicyReason.AVOID_EXPOSING_IMPAIRMENT, budget.reason)
    }

    @Test
    fun `healthy information refuses a false-only candidate pool`() {
        val budget = ImpairedInformationPolicy.familyBudget(
            reliability = InformationReliability.RELIABLE,
            hasTruthfulCandidate = false,
            hasFalseCandidate = true,
        )

        assertEquals(0L, budget.truthfulMassFixedPoint)
        assertEquals(0L, budget.falseMassFixedPoint)
        assertEquals(ImpairedInformationPolicyReason.NO_LEGAL_TRUTHFUL_CANDIDATE, budget.reason)
    }

    @Test
    fun `healthy selector refuses a false-only candidate pool`() {
        data class Option(val id: String, val truthful: Boolean)
        val falseOnly = Option("false-only", false)

        val selected = DynamicCandidateGenerator.select(
            options = listOf(falseOnly),
            reliability = InformationReliability.RELIABLE,
            style = RecommendationStyle.BALANCED,
            evilAdvantage = 0,
            stableKey = "healthy:false-only",
            recentMisinformationStreak = 0,
            stableIdOf = Option::id,
            isTruthful = Option::truthful,
            misinformationPressure = { 1 },
            styleOf = { RecommendationStyle.BALANCED },
        )

        assertNull(selected)
    }

    @Test
    fun `truthful exception propagates through coordinator selection seam`() {
        data class Option(val id: String, val truthful: Boolean)
        val truth = Option("truth", true)
        val falsehood = Option("falsehood", false)

        val selected = ClocktowerRecommendationCoordinator().selectInformation(
            options = listOf(truth, falsehood),
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.AGGRESSIVE,
            evilAdvantage = -100,
            stableKey = "poisoned:avoid-exposing-impairment",
            recentMisinformationStreak = 0,
            stableIdOf = Option::id,
            isTruthful = Option::truthful,
            misinformationPressure = { if (it.truthful) 0 else 4 },
            styleOf = { RecommendationStyle.AGGRESSIVE },
            truthfulException = ImpairedTruthfulException.AVOID_EXPOSING_IMPAIRMENT,
        )

        assertEquals(truth, selected)
    }

    @Test
    fun `truthful exception reason survives into selection audit metadata`() {
        data class Option(val id: String, val truthful: Boolean)
        val truth = Option("truth", true)
        val falsehood = Option("falsehood", false)
        val recorder = SelectionDistributionTelemetryRecorder()
        val dimensions = SelectionAuditDimensions(
            playerCount = 8,
            phase = StorytellerPhase.NIGHT,
            style = RecommendationStyle.AGGRESSIVE,
        )
        val selectionId = "audit:poisoned:avoid-exposing-impairment"

        val selected = ClocktowerRecommendationCoordinator().selectInformation(
            options = listOf(truth, falsehood),
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.AGGRESSIVE,
            evilAdvantage = 100,
            stableKey = selectionId,
            recentMisinformationStreak = 0,
            stableIdOf = Option::id,
            isTruthful = Option::truthful,
            misinformationPressure = { if (it.truthful) 0 else 4 },
            styleOf = { RecommendationStyle.AGGRESSIVE },
            selectionAudit = SelectionAuditContext(selectionId, dimensions, recorder),
            truthfulException = ImpairedTruthfulException.AVOID_EXPOSING_IMPAIRMENT,
        )

        assertEquals(truth, selected)
        assertEquals(
            setOf("impaired-information.truth.avoid-exposing-impairment"),
            recorder.previewReasonCodes(selectionId, dimensions),
        )
    }

    @Test
    fun `truthful selection reason distinguishes all allowed exception families`() {
        assertEquals(
            "impaired-information.truth.deliberate-uncertainty",
            DynamicCandidateGenerator.selectionAuditReasonCode(
                ImpairedInformationPolicyReason.IMPAIRED_FALSE_PREFERRED,
                selectedTruthful = true,
            ),
        )
        assertEquals(
            "impaired-information.truth.no-legal-false-candidate",
            DynamicCandidateGenerator.selectionAuditReasonCode(
                ImpairedInformationPolicyReason.NO_LEGAL_FALSE_CANDIDATE,
                selectedTruthful = true,
            ),
        )
        assertEquals(
            "impaired-information.truth.avoid-exposing-impairment",
            DynamicCandidateGenerator.selectionAuditReasonCode(
                ImpairedInformationPolicyReason.AVOID_EXPOSING_IMPAIRMENT,
                selectedTruthful = true,
            ),
        )
    }
}
