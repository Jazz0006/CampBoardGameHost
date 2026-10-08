package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationResolutionStatusV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerConfirmedRegistrationPublicationV1Test {
    private val numeric = InformationProposition.NumericResult(
        NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 1,
    )
    private val witnesses = listOf(
        ClocktowerRegistrationWitness(spyRegistersGood = false, recluseRegistersEvil = false),
        ClocktowerRegistrationWitness(spyRegistersGood = true, recluseRegistersEvil = true),
    )
    private fun option(
        proposition: InformationProposition = numeric,
        kind: ClocktowerDisplayKind = ClocktowerDisplayKind.Number,
        shown: String = "1",
        witness: ClocktowerRegistrationWitness,
    ) = ClocktowerDisplayOption(
        label = "shown", displayKind = kind, displayTitle = "Result",
        displayPrimary = shown, displaySecondary = null, displayFooter = null,
        proposition = proposition, spyRegistersGood = witness.spyRegistersGood,
        recluseRegistersEvil = witness.recluseRegistersEvil,
    )
    private fun plan(
        proposition: InformationProposition = numeric,
        kind: ClocktowerDisplayKind = ClocktowerDisplayKind.Number,
        shown: String = "1",
        options: List<ClocktowerDisplayOption> = witnesses.map { option(witness = it) },
        spy: ClocktowerManualRegistrationChoice? = null,
        recluse: ClocktowerManualRegistrationChoice? = null,
    ) = clocktowerPlanConfirmedResultRegistrations(
        shownProposition = proposition, shownKind = kind, shownPrimary = shown,
        legalCandidates = options, spy = spy, spySeat = 1,
        spyQuestion = RegistrationQuestion.ALIGNMENT,
        recluse = recluse, recluseSeat = 3, recluseQuestion = RegistrationQuestion.ALIGNMENT,
    )
    private fun manual(
        subject: ClocktowerRegistrationSubject,
        enabled: Boolean,
        role: String? = null,
    ) = ClocktowerManualRegistrationChoice(subject, "night:1:empath", enabled, role)

    @Test fun `same Empath one result has two witnesses and neither is silently committed`() {
        val value = plan()
        assertTrue(value is ClocktowerResultRegistrationPlanV1.Ready)
        assertEquals(listOf(
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
        ), (value as ClocktowerResultRegistrationPlanV1.Ready).choices.map { it.status })
    }

    @Test fun `one explicit spy selection does not force the remaining Recluse witness`() {
        val value = plan(spy = manual(ClocktowerRegistrationSubject.SPY, true))
        val choices = (value as ClocktowerResultRegistrationPlanV1.Ready).choices
        assertEquals(RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, choices.first().status)
        assertEquals(null, choices.first().selectedRole)
        assertEquals(RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED, choices.last().status)
        assertTrue(plan(
            spy = manual(ClocktowerRegistrationSubject.SPY, true),
            recluse = manual(ClocktowerRegistrationSubject.RECLUSE, false),
        ) is ClocktowerResultRegistrationPlanV1.ConflictingManualChoice)
    }

    @Test fun `explicit false remains actual and different result cannot borrow a witness`() {
        val choices = (plan(spy = manual(ClocktowerRegistrationSubject.SPY, false))
            as ClocktowerResultRegistrationPlanV1.Ready).choices
        assertEquals(RegistrationResolutionStatusV1.EXPLICIT_ACTUAL, choices.first().status)
        assertTrue(plan(
            shown = "0",
            spy = manual(ClocktowerRegistrationSubject.SPY, false),
        ) is ClocktowerResultRegistrationPlanV1.ConflictingManualChoice)
        assertTrue(plan(shown = "0") is ClocktowerResultRegistrationPlanV1.NoVerifiedWitness)
    }

    @Test fun `boolean and role information use same result first adjudication`() {
        val boolean = InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 2, listOf(1, 3), true,
        )
        val boolPlan = plan(
            proposition = boolean, kind = ClocktowerDisplayKind.YesNo, shown = "Yes",
            options = witnesses.map { option(boolean, ClocktowerDisplayKind.YesNo, "Yes", it) },
        )
        assertEquals(2, (boolPlan as ClocktowerResultRegistrationPlanV1.Ready).choices.size)
        val role = InformationProposition.RoleAt(3, RoleId("Imp"))
        val rolePlan = plan(
            proposition = role, kind = ClocktowerDisplayKind.RoleReveal, shown = "Imp",
            options = witnesses.map { option(role, ClocktowerDisplayKind.RoleReveal, "Imp", it) },
        )
        assertEquals(1, (rolePlan as ClocktowerResultRegistrationPlanV1.Ready).choices.size)
        assertEquals(3, rolePlan.choices.single().subjectSeat)
    }

    @Test fun `unknown or missing legal evidence cannot authorize explicit history`() {
        assertTrue(plan(options = emptyList()) is ClocktowerResultRegistrationPlanV1.NoVerifiedWitness)
        assertTrue(plan(
            options = emptyList(), spy = manual(ClocktowerRegistrationSubject.SPY, true),
        ) is ClocktowerResultRegistrationPlanV1.ConflictingManualChoice)
        assertTrue(plan(
            options = listOf(option(witness = witnesses.first()).copy(
                spyRegistersGood = null, recluseRegistersEvil = null,
            )),
        ) is ClocktowerResultRegistrationPlanV1.NoVerifiedWitness)
    }
}
