package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal enum class ClocktowerChefResultSourceKind {
    DisplayOption,
    Direct,
}

internal data class ClocktowerChefResultChoice(
    val key: String,
    val value: Int,
    val sourceKind: ClocktowerChefResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val recommended: Boolean = false,
)

internal data class ClocktowerChefSeatVisual(
    val state: ClocktowerSquareTableSeatState,
    val badge: String?,
    val isCurrentActor: Boolean,
)

/**
 * Storyteller-only Chef evidence. Actual evil identity is a read-only information hint; a Recluse
 * is a separate role hint. A displayed number never implies an unconfirmed registration witness.
 */
internal fun clocktowerChefSeatVisual(
    seatNumber: Int,
    actorSeat: Int?,
    actualEvilSeats: Set<Int>,
    spySeat: Int?,
    recluseSeat: Int?,
    language: String,
): ClocktowerChefSeatVisual {
    val isSpy = seatNumber == spySeat
    val isRecluse = seatNumber == recluseSeat
    val isRegistrationRole = isSpy || isRecluse
    return ClocktowerChefSeatVisual(
        state = when {
            isRegistrationRole -> ClocktowerSquareTableSeatState.RegistrationHint
            seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation
            else -> ClocktowerSquareTableSeatState.Neutral
        },
        badge = when {
            isSpy -> if (language == "en") "S" else "间"
            isRecluse -> if (language == "en") "R" else "隐"
            else -> null
        },
        isCurrentActor = seatNumber == actorSeat,
    )
}

internal fun clocktowerChefActualEvilSeats(players: List<PlayerState>): Set<Int> = players
    .filter { player -> player.actualAlignment == ClocktowerAlignment.EVIL }
    .mapTo(linkedSetOf()) { player -> player.seat }

internal fun clocktowerChefSpySeat(players: List<PlayerState>): Int? = players
    .firstOrNull { player -> player.actualRole.value == "Spy" }
    ?.seat

internal fun clocktowerChefRecluseSeat(players: List<PlayerState>): Int? = players
    .firstOrNull { player -> player.actualRole.value == "Recluse" }
    ?.seat

internal fun clocktowerChefResultChoices(
    step: ClocktowerNightStepUi,
    allowRuleDeterminedAutoUi: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
    resultFirstRegistrationCandidates: List<ClocktowerDisplayOption>,
    recommendedOptionIds: Set<String> = emptySet(),
    recommendedValues: Set<Int> = emptySet(),
): List<ClocktowerChefResultChoice> {
    fun numericValue(option: ClocktowerDisplayOption): Int? =
        (option.proposition as? InformationProposition.NumericResult)?.value

    if (allowRuleDeterminedAutoUi) {
        automaticDisplayOption?.let { option ->
            val value = numericValue(option)
            if (value != null) {
                return listOf(
                    ClocktowerChefResultChoice(
                        key = clocktowerInformationCandidateId(option),
                        value = value,
                        sourceKind = ClocktowerChefResultSourceKind.DisplayOption,
                        displayOption = option,
                        recommended = true,
                    ),
                )
            }
        }
    } else if (resultFirstRegistrationCandidates.isNotEmpty()) {
        return resultFirstRegistrationCandidates.mapNotNull { option ->
            val value = numericValue(option) ?: return@mapNotNull null
            ClocktowerChefResultChoice(
                key = clocktowerInformationCandidateId(option),
                value = value,
                sourceKind = ClocktowerChefResultSourceKind.DisplayOption,
                displayOption = option,
                recommended = clocktowerInformationCandidateId(option) in recommendedOptionIds ||
                    (recommendedOptionIds.isEmpty() && option.isDefaultRecommendation),
            )
        }
    }

    val direct = step.displayProposition as? InformationProposition.NumericResult ?: return emptyList()
    return listOf(
        ClocktowerChefResultChoice(
            key = "direct|${direct.value}",
            value = direct.value,
            sourceKind = ClocktowerChefResultSourceKind.Direct,
            recommended = true,
        ),
    )
}

@Composable
internal fun ClocktowerChefSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    actualEvilSeats: Set<Int>,
    spySeat: Int?,
    recluseSeat: Int?,
    choices: List<ClocktowerChefResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerChefResultChoice) -> Unit,
) {
    if (choices.isEmpty()) return
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.first()
    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction) &&
        clocktowerRuleOnlyCompactChoice(choices) != null
    ) {
        ClocktowerBeginnerReadOnlyRevealDialog(
            seats = seats,
            actorSeat = actorSeat,
            wakeInstruction = wakeInstruction,
            language = language,
            canGoPrevious = canGoPrevious,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            onShow = { onConfirm(initialChoice) },
        )
        return
    }
    val recommendedChoices = choices.filter { it.recommended }.take(1)

    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
        seatUiModel = { seat ->
            val content = hostSeatContentPresentation(seat, language)
            val visual = clocktowerChefSeatVisual(
                seatNumber = seat.seatId.number,
                actorSeat = actorSeat,
                actualEvilSeats = actualEvilSeats,
                spySeat = spySeat,
                recluseSeat = recluseSeat,
                language = language,
            )
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(),
                seatNumber = seat.seatId.number,
                label = content.primaryLabel,
                detailLabels = content.detailLabels,
                isAlive = seat.isAlive,
                hasUnspentGhostVote = seat.hasUnspentGhostVote,
                state = visual.state,
                isCurrentActor = visual.isCurrentActor,
                badge = visual.badge,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                Text(
                    text = if (language == "en") "Chef" else "厨师",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))

                ClocktowerSingleNumericChoiceRows(
                    choices = choices,
                    recommendedChoices = recommendedChoices,
                    language = language,
                    valueOf = ClocktowerChefResultChoice::value,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}
