package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState

internal data class ClocktowerSageStepContent(
    val explanation: String,
    val displayTitle: String,
    val displayPrimary: String,
    val displayFooter: String,
    val hostInstruction: String,
)

internal fun clocktowerSageStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    triggerActor: PlayerCard?,
    abilityState: AbilityFunctioningState?,
    content: ClocktowerSageStepContent,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Sage")),
    build = {
        val resolvedTrigger = requireNotNull(triggerActor)
        builder.build(
            roleName = "贤者",
            enName = "Sage",
            actorOverride = resolvedTrigger,
            abilityStateOverride = abilityState,
            tellPlayer = content.displayPrimary,
            explanation = content.explanation,
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = content.displayTitle,
            displayPrimary = content.displayPrimary,
            displayFooter = content.displayFooter,
            hostInstruction = content.hostInstruction,
        )
    },
)


/** Records the two seats the Storyteller actually chose, without deriving a legal Sage candidate menu. */
internal fun clocktowerSageManualPlayerDisplay(
    step: ClocktowerNightStepUi,
    selectedSeats: List<Int>,
    seatCount: Int,
): ClocktowerNightStepUi? {
    if (step.roleEnName != "Sage" || step.actor == null || !step.isRealAction ||
        !clocktowerSageValidPair(selectedSeats, seatCount)
    ) return null
    val seats = selectedSeats.sorted()
    val demonRole = (step.displayProposition as? InformationProposition.AnyOf)
        ?.alternatives?.filterIsInstance<InformationProposition.RoleAt>()?.firstOrNull()?.role
        ?: RoleId("Imp")
    return step.copy(
        displayKind = ClocktowerDisplayKind.EitherOne,
        tellPlayer = step.displayPrimary ?: "Demon",
        displayPrimary = step.displayPrimary ?: "Demon",
        displaySecondary = seats.joinToString("   "),
        presentationSubjectSeats = seats,
        displayProposition = InformationProposition.AnyOf(seats.map { seat ->
            InformationProposition.RoleAt(seat, demonRole)
        }),
        displayOptions = emptyList(),
        recommendedDisplayOptions = emptyList(),
        selectedInformationTruthful = null,
    )
}


