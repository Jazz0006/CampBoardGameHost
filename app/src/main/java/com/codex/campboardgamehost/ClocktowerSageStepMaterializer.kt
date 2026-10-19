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
    cards: List<PlayerCard>,
    triggerActor: PlayerCard?,
    demon: PlayerCard?,
    directPair: Pair<PlayerCard, PlayerCard>?,
    abilityState: AbilityFunctioningState?,
    content: ClocktowerSageStepContent,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Sage")),
    build = {
        val resolvedDemon = requireNotNull(demon)
        val resolvedDirectPair = requireNotNull(directPair)
        val resolvedTrigger = requireNotNull(triggerActor)
        val directSubjectSeats = clocktowerSageSubjectSeats(
            cards = cards,
            first = resolvedDemon,
            second = resolvedDirectPair.second,
        )
        builder.build(
            roleName = "贤者",
            enName = "Sage",
            actorOverride = resolvedTrigger,
            abilityStateOverride = abilityState,
            tellPlayer = "${resolvedDemon.seatLabel(cards)} / ${resolvedDirectPair.second.seatLabel(cards)}",
            explanation = content.explanation,
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = content.displayTitle,
            displayPrimary = content.displayPrimary,
            displaySecondary = directSubjectSeats.takeIf { it.size == 2 }?.joinToString("   "),
            displayFooter = content.displayFooter,
            displayProposition = resolvedDemon.clocktowerRole?.enName?.let(::RoleId)
                ?.takeIf { directSubjectSeats.size == 2 }
                ?.let { demonRole ->
                    InformationProposition.AnyOf(directSubjectSeats.map { seat ->
                        InformationProposition.RoleAt(seat, demonRole)
                    })
                },
            presentationSubjectSeats = directSubjectSeats,
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

private fun clocktowerSageSubjectSeats(
    cards: List<PlayerCard>,
    first: PlayerCard,
    second: PlayerCard,
): List<Int> {
    val seats = listOf(cards.indexOf(first) + 1, cards.indexOf(second) + 1)
    return seats.takeIf { values ->
        values.size == 2 &&
            values.distinct().size == 2 &&
            values.all { it in 1..cards.size }
    }.orEmpty()
}
