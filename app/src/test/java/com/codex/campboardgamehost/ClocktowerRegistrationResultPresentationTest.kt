package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerRegistrationResultPresentationTest {
    @Test
    fun `numeric duplicate result retains all legal alternatives without a ruling`() {
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
        assertNull(options.single().spyRegistersGood)
        assertEquals(setOf(true, false), options.single().legalRegistrationWitnesses.map { it.spyRegistersGood }.toSet())
        assertEquals(
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), 1),
            options.single().proposition,
        )
    }

    @Test
    fun `fortune teller coincident result keeps both witnesses without silently selecting one`() {
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
        assertNull(options.single().recluseRegistersEvil)
        assertNull(options.single().recluseRegisteredRoleEnName)
        assertEquals(setOf(true, false), options.single().legalRegistrationWitnesses.map { it.recluseRegistersEvil }.toSet())
        assertEquals(
            InformationProposition.BooleanResult(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 3), true),
            options.single().proposition,
        )
    }

    @Test
    fun `role reveal keeps legal explanation but does not commit it as a ruling`() {
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
        assertNull(options.first().spyRegistersGood)
        assertNull(options.first().spyRegisteredRoleEnName)
        assertEquals(true, options.first().legalRegistrationWitnesses.single().spyRegistersGood)
        assertEquals("Washerwoman", options.first().legalRegistrationWitnesses.single().spyRegisteredRoleEnName)
        assertNull(options.last().spyRegistersGood)
        assertNull(options.last().spyRegisteredRoleEnName)
        assertEquals(false, options.last().legalRegistrationWitnesses.single().spyRegistersGood)
    }

    private fun role(name: String) = completeTroubleBrewingRoles.single { it.enName == name }


    @Test
    fun `Chef and Empath pair substitutions use the same nonunique result contract`() {
        val witnesses = listOf(
            ClocktowerAlignmentRegistrationWitness(false, false),
            ClocktowerAlignmentRegistrationWitness(false, true),
            ClocktowerAlignmentRegistrationWitness(true, false),
            ClocktowerAlignmentRegistrationWitness(true, true),
        )
        for (metric in listOf(NumericMetric.ADJACENT_EVIL_PAIRS, NumericMetric.LIVING_EVIL_NEIGHBOURS)) {
            // Spy is next to an Imp and Imp is next to Recluse.
            // (Spy evil, Recluse good) and (Spy good, Recluse evil) each give one.
            val results = ClocktowerRegistrationResultPresentation.numericOptions(
                title = metric.name,
                sourceSeat = 4,
                metric = metric,
                subjectSeats = listOf(1, 2, 3),
                footer = "result",
                witnesses = witnesses,
                valueFor = { w ->
                    (if (w.spyRegistersGood == false) 1 else 0) +
                        (if (w.recluseRegistersEvil == true) 1 else 0)
                },
            )
            assertEquals(listOf(1, 2, 0), results.map {
                (it.proposition as InformationProposition.NumericResult).value
            })
            val one = results.single {
                (it.proposition as InformationProposition.NumericResult).value == 1
            }
            assertNull(one.spyRegistersGood)
            assertNull(one.recluseRegistersEvil)
            assertEquals(
                setOf(
                    ClocktowerRegistrationWitness(spyRegistersGood = false, recluseRegistersEvil = false),
                    ClocktowerRegistrationWitness(spyRegistersGood = true, recluseRegistersEvil = true),
                ),
                one.legalRegistrationWitnesses.toSet(),
            )
        }
    }
}
