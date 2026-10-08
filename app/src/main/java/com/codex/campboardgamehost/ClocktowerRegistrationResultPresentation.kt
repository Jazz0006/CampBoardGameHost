package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric

/** Projects already-legal registration rulings into final player-visible results. */
internal object ClocktowerRegistrationResultPresentation {
    fun numericOptions(
        title: String,
        sourceSeat: Int,
        metric: NumericMetric,
        subjectSeats: List<Int>,
        footer: String,
        witnesses: List<ClocktowerAlignmentRegistrationWitness>,
        valueFor: (ClocktowerAlignmentRegistrationWitness) -> Int,
    ): List<ClocktowerDisplayOption> = distinctClocktowerFinalInformationResults(
        witnesses.map { witness ->
            val value = valueFor(witness)
            ClocktowerDisplayOption(
                label = value.toString(),
                displayKind = ClocktowerDisplayKind.Number,
                displayTitle = title,
                displayPrimary = value.toString(),
                displaySecondary = null,
                displayFooter = footer,
                proposition = InformationProposition.NumericResult(metric, sourceSeat, subjectSeats, value),
                spyRegistersGood = witness.spyRegistersGood,
                recluseRegistersEvil = witness.recluseRegistersEvil,
                isTruthful = true,
            )
        },
    )

    fun fortuneTellerOptions(
        sourceSeat: Int,
        subjectSeats: List<Int>,
        secondary: String?,
        currentRecluseRegistersEvil: Boolean,
        demonRegistrationRole: ClocktowerRole?,
        text: (String, String) -> String,
        matches: (Boolean) -> Boolean?,
    ): List<ClocktowerDisplayOption> = distinctClocktowerFinalInformationResults(
        listOf(currentRecluseRegistersEvil, !currentRecluseRegistersEvil).mapNotNull { recluseEvil ->
            val value = matches(recluseEvil) ?: return@mapNotNull null
            val resultText = if (value) text("有", "Yes") else text("没有", "No")
            ClocktowerDisplayOption(
                label = resultText,
                displayKind = ClocktowerDisplayKind.YesNo,
                displayTitle = text("占卜师信息", "Fortune Teller information"),
                displayPrimary = resultText,
                displaySecondary = secondary,
                displayFooter = text("查询这两名玩家", "Checking these two players"),
                proposition = InformationProposition.BooleanResult(
                    BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, sourceSeat, subjectSeats, value,
                ),
                recluseRegistersEvil = recluseEvil,
                recluseRegisteredRoleEnName = demonRegistrationRole?.enName?.takeIf { recluseEvil },
                isTruthful = true,
            )
        },
    )

    enum class SpecialRegistration { Spy, Recluse }

    data class RoleRevealRuling(
        val specialRegistration: SpecialRegistration,
        val specialSelected: Boolean,
        val actualRole: ClocktowerRole?,
        val selectedRole: ClocktowerRole?,
        val legalSpecialRoles: List<ClocktowerRole>,
    )

    fun roleRevealOptions(
        title: String,
        targetSeat: Int,
        footer: String,
        ruling: RoleRevealRuling,
        roleLabel: (ClocktowerRole) -> String,
    ): List<ClocktowerDisplayOption> {
        fun option(role: ClocktowerRole, special: Boolean): ClocktowerDisplayOption {
            val roleName = roleLabel(role)
            val spyGood = special.takeIf { ruling.specialRegistration == SpecialRegistration.Spy }
            val recluseEvil = special.takeIf { ruling.specialRegistration == SpecialRegistration.Recluse }
            return ClocktowerDisplayOption(
                label = roleName,
                displayKind = ClocktowerDisplayKind.RoleReveal,
                displayTitle = title,
                displayPrimary = roleName,
                displaySecondary = null,
                displayFooter = footer,
                proposition = InformationProposition.RoleAt(targetSeat, RoleId(role.enName)),
                spyRegistersGood = spyGood,
                spyRegisteredRoleEnName = role.enName.takeIf { spyGood == true },
                recluseRegistersEvil = recluseEvil,
                recluseRegisteredRoleEnName = role.enName.takeIf { recluseEvil == true },
                isTruthful = true,
            )
        }
        val candidates = buildList {
            if (ruling.specialSelected) ruling.selectedRole?.let { add(option(it, true)) }
            ruling.actualRole?.let { add(option(it, false)) }
            ruling.legalSpecialRoles.forEach { add(option(it, true)) }
        }
        return distinctClocktowerFinalInformationResults(candidates)
    }
}
