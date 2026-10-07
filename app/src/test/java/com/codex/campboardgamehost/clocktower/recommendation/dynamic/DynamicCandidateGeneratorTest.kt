package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicCandidateGeneratorTest {
    private val poisonedContext = DynamicGenerationContext(
        abilityRole = RoleId("Empath"),
        recipientSeat = 2,
        reliability = InformationReliability.POISONED,
    )

    @Test
    fun `numeric generation retains every legal value and separates truth families`() {
        val evaluations = DynamicCandidateGenerator.generateNumeric(
            UnreliableNumberContext(trueValue = 1, minimumValue = 0, maximumValue = 2),
            poisonedContext,
        )

        assertEquals(setOf(0, 1, 2), evaluations.map { it.candidate.outcome.value }.toSet())
        assertEquals(3, evaluations.map { it.candidate.candidateId }.distinct().size)
        assertEquals(
            "malfunction-truth",
            evaluations.single { it.candidate.outcome.value == 1 }.candidate.candidateFamilyId,
        )
        assertTrue(evaluations.filter { it.candidate.outcome.value != 1 }.all {
            it.candidate.candidateFamilyId == "malfunction-falsehood-numeric" &&
                it.candidate.truthRelation == TruthRelation.FALSE_TO_ACTUAL_STATE
        })
        assertTrue(evaluations.all { it.candidate.abilityState == AbilityState.MALFUNCTIONING_POISONED })
        assertTrue(evaluations.all { it.warnings.isEmpty() })
    }

    @Test
    fun `numeric generation preserves policy neutral longitudinal warnings`() {
        val evaluations = DynamicCandidateGenerator.generateNumeric(
            UnreliableNumberContext(
                trueValue = 0,
                minimumValue = 0,
                maximumValue = 2,
                previousShownValue = 0,
            ),
            poisonedContext,
        )

        assertTrue("large-history-jump" in evaluations.single { it.candidate.outcome.value == 2 }.warnings)
        assertTrue(evaluations.single { it.candidate.outcome.value == 1 }.warnings.none {
            it == "large-history-jump"
        })
    }

    @Test
    fun `categorical generation produces the complete typed legal candidate set without ranking`() {
        val evaluations = DynamicCandidateGenerator.generateCategorical(
            listOf(
                UnreliableCategoricalCandidate("yes", true),
                UnreliableCategoricalCandidate("no", false),
            ),
            poisonedContext,
        )

        assertEquals(
            setOf("yes", "no"),
            evaluations.map { (it.candidate.outcome as DynamicInformationOutcome.Category).id }.toSet(),
        )
        assertTrue(evaluations.all { it.warnings.isEmpty() })
    }
}
