package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRegistrationResultDomainTest {
    @Test
    fun `duplicate final result keeps the first registration witness`() {
        val currentWitness = numberOption(value = 1, spyRegistersGood = false)
        val alternateWitness = numberOption(value = 1, spyRegistersGood = true)

        val projected = distinctClocktowerFinalInformationResults(
            listOf(currentWitness, alternateWitness),
        )

        assertEquals(1, projected.size)
        assertEquals(false, projected.single().spyRegistersGood)
    }

    @Test
    fun `different final results remain independently selectable`() {
        val projected = distinctClocktowerFinalInformationResults(
            listOf(
                numberOption(value = 0, spyRegistersGood = true),
                numberOption(value = 1, spyRegistersGood = false),
            ),
        )

        assertEquals(listOf(0, 1), projected.map { (it.proposition as InformationProposition.NumericResult).value })
    }

    @Test
    fun `result first policy requires an exact manual domain and registration interaction`() {
        val option = numberOption(value = 1, spyRegistersGood = false)
        val resultFirst = step(
            manualInformationCandidates = listOf(option),
            spyRegistrationKey = "Chef|Spy",
        )
        val noRegistration = step(manualInformationCandidates = listOf(option))
        val noDomain = step(spyRegistrationKey = "Chef|Spy")

        assertTrue(resultFirst.usesResultFirstRegistrationDomain())
        assertFalse(noRegistration.usesResultFirstRegistrationDomain())
        assertFalse(noDomain.usesResultFirstRegistrationDomain())
    }

    private fun numberOption(
        value: Int,
        spyRegistersGood: Boolean,
    ) = ClocktowerDisplayOption(
        label = value.toString(),
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chef information",
        displayPrimary = value.toString(),
        displaySecondary = null,
        displayFooter = "Adjacent evil pairs",
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = 1,
            subjectSeats = listOf(1, 2, 3, 4, 5),
            value = value,
        ),
        spyRegistersGood = spyRegistersGood,
    )

    private fun step(
        manualInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
        spyRegistrationKey: String? = null,
    ) = ClocktowerNightStepUi(
        title = "Chef",
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = null,
        explanation = "",
        manualInformationCandidates = manualInformationCandidates,
        spyRegistrationKey = spyRegistrationKey,
    )
}
