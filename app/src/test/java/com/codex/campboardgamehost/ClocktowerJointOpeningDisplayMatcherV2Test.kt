package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerJointOpeningDisplayMatcherV2Test {
    private fun numericOption(value: Int) = ClocktowerDisplayOption(
        label = "numeric", displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chef", displayPrimary = value.toString(),
        displaySecondary = null, displayFooter = null,
        proposition = InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, listOf(1, 2, 3), value),
    )
    private val chefOne = HostOpeningSemanticValueV2(
        sourceSeat = 1, abilityRoleId = "chef",
        effectiveAbilityRoleId = "chef", basis = "FUNCTIONING",
        subjectMode = "NONE", subjectSeats = emptyList(),
        observationKind = "NUMBER", number = 1,
    )

    @Test fun currentTypedNumericHostOptionMustMatchPlanExactly() {
        val chosen = numericOption(1)
        assertEquals(chosen, ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(numericOption(0), chosen)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(numericOption(0))))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 2, listOf(chosen)))
        // A duplicated visible interpretation is not a license to pick a witness.
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(chosen, chosen)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(chosen.copy(spyRegistersGood = true))))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            chefOne, 1, listOf(chosen.copy(proposition = null))))
    }

    @Test fun pairUsesTypedSeatRoleDisjunctionAndNotLocalizedText() {
        val proposition = InformationProposition.AnyOf(listOf(
            InformationProposition.RoleAt(6, RoleId("Baron")),
            InformationProposition.RoleAt(7, RoleId("Baron"))))
        val option = ClocktowerDisplayOption(
            label = "translated label", displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "任意", displayPrimary = "男爵",
            displaySecondary = "任意文本", displayFooter = null,
            proposition = proposition, presentationSubjectSeats = listOf(6, 7),
        )
        val proposed = HostOpeningSemanticValueV2(
            sourceSeat = 5, abilityRoleId = "investigator",
            effectiveAbilityRoleId = "investigator", basis = "FUNCTIONING",
            subjectMode = "EVIDENCE_PAIR", subjectSeats = listOf(6, 7),
            observationKind = "ROLE", roleId = "baron",
        )
        assertEquals(option, ClocktowerJointOpeningDisplayMatcherV2.match(
            proposed, 5, listOf(option)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            proposed.copy(subjectSeats = listOf(5, 7)), 5, listOf(option)))
        assertNull(ClocktowerJointOpeningDisplayMatcherV2.match(
            proposed.copy(roleId = "spy"), 5, listOf(option)))
    }
}
