package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerRegistrationResultPresentationTest {
    @Test
    fun `numeric duplicate result retains current witness and seat proposition`() {
        val options = ClocktowerRegistrationResultPresentation.numericOptions(
            title = "Chef information",
            sourceSeat = 1,
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = listOf(1, 2, 3),
            footer = "pairs",
            witnesses = listOf(
                ClocktowerAlignmentRegistrationWitness(true, false),
                ClocktowerAlignmentRegistrationWitness(false, false),
            ),
            valueFor = { 1 },
        )

        assertEquals(1, options.size)
        assertEquals(true, options.single().spyRegistersGood)
        assertEquals(
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), 1),
            options.single().proposition,
        )
    }

    @Test
    fun `fortune teller result keeps current recluse witness when outcomes coincide`() {
        val options = ClocktowerRegistrationResultPresentation.fortuneTellerOptions(
            sourceSeat = 1,
            subjectSeats = listOf(2, 3),
            secondary = "2   3",
            currentRecluseRegistersEvil = true,
            demonRegistrationRole = role("Imp"),
            text = { _, en -> en },
            matches = { true },
        )

        assertEquals(1, options.size)
        assertEquals(true, options.single().recluseRegistersEvil)
        assertEquals("Imp", options.single().recluseRegisteredRoleEnName)
        assertEquals(
            InformationProposition.BooleanResult(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 3), true),
            options.single().proposition,
        )
    }

    @Test
    fun `role reveal keeps selected special registration ahead of duplicate legal roles`() {
        val spyRole = role("Spy")
        val selected = role("Washerwoman")
        val options = ClocktowerRegistrationResultPresentation.roleRevealOptions(
            title = "Undertaker information",
            targetSeat = 2,
            footer = "executed",
            ruling = ClocktowerRegistrationResultPresentation.RoleRevealRuling(
                specialRegistration = ClocktowerRegistrationResultPresentation.SpecialRegistration.Spy,
                specialSelected = true,
                actualRole = spyRole,
                selectedRole = selected,
                legalSpecialRoles = listOf(selected),
            ),
            roleLabel = { it.enName },
        )

        assertEquals(listOf("Washerwoman", "Spy"), options.map { it.displayPrimary })
        assertEquals(true, options.first().spyRegistersGood)
        assertEquals("Washerwoman", options.first().spyRegisteredRoleEnName)
        assertEquals(false, options.last().spyRegistersGood)
        assertNull(options.last().spyRegisteredRoleEnName)
    }

    private fun role(name: String) = completeTroubleBrewingRoles.single { it.enName == name }
}
