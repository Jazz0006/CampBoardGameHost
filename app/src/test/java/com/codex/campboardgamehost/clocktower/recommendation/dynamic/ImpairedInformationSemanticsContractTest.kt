package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
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
            "Poisoned information should keep false information in the intended near-certain policy band.",
            masses.all { it in 950_000L..990_000L },
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
            "Drunk information should keep false information in the intended near-certain policy band.",
            masses.all { it in 950_000L..990_000L },
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
}
