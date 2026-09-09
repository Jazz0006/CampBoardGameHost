package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel

internal enum class ClocktowerChefResultSourceKind {
    DisplayOption,
    Structured,
    Direct,
}

internal data class ClocktowerChefResultChoice(
    val key: String,
    val value: Int,
    val sourceKind: ClocktowerChefResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val structuredCandidateId: String? = null,
    val recommended: Boolean = false,
    val effectivePairSeats: Set<Int> = emptySet(),
)

internal data class ClocktowerChefSeatVisual(
    val state: ClocktowerSquareTableSeatState,
    val badge: String?,
    val isCurrentActor: Boolean,
)

/**
 * Storyteller-only Chef evidence. Actual evil identity is a read-only information hint; a Recluse
 * is a separate registration hint; seats participating in the currently chosen counted pair(s)
 * are the only seats promoted to a selected/highlighted state.
 */
internal fun clocktowerChefSeatVisual(
    seatNumber: Int,
    actorSeat: Int?,
    actualEvilSeats: Set<Int>,
    recluseSeat: Int?,
    effectivePairSeats: Set<Int>,
    language: String,
): ClocktowerChefSeatVisual = ClocktowerChefSeatVisual(
    state = when {
        seatNumber in effectivePairSeats -> ClocktowerSquareTableSeatState.SelectedHighlighted
        seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation
        else -> ClocktowerSquareTableSeatState.Neutral
    },
    badge = if (seatNumber == recluseSeat) {
        if (language == "en") "R" else "隐"
    } else {
        null
    },
    isCurrentActor = seatNumber == actorSeat,
)

internal fun clocktowerChefActualEvilSeats(players: List<PlayerState>): Set<Int> = players
    .filter { player -> player.actualAlignment == Alignment.EVIL }
    .mapTo(linkedSetOf()) { player -> player.seat }

internal fun clocktowerChefRecluseSeat(players: List<PlayerState>): Int? = players
    .firstOrNull { player -> player.actualRole.value == "Recluse" }
    ?.seat

/** Apply the already-selected typed Spy/Recluse registration witness attached to the result. */
internal fun clocktowerChefEffectiveEvilSeats(
    players: List<PlayerState>,
    option: ClocktowerDisplayOption?,
): Set<Int> {
    val effective = clocktowerChefActualEvilSeats(players).toMutableSet()
    players.firstOrNull { it.actualRole.value == "Spy" }?.let { spy ->
        when (option?.spyRegistersGood) {
            true -> effective.remove(spy.seat)
            false -> effective.add(spy.seat)
            null -> Unit
        }
    }
    players.firstOrNull { it.actualRole.value == "Recluse" }?.let { recluse ->
        when (option?.recluseRegistersEvil) {
            true -> effective.add(recluse.seat)
            false -> effective.remove(recluse.seat)
            null -> Unit
        }
    }
    return effective
}

/**
 * Presentation-only witness projection. The rules evaluator remains authoritative for the numeric
 * result; if this witness does not reproduce that value, visual selection fails closed.
 */
internal fun clocktowerChefEffectivePairSeats(
    players: List<PlayerState>,
    option: ClocktowerDisplayOption?,
    value: Int,
): Set<Int> {
    val effectiveEvilSeats = clocktowerChefEffectiveEvilSeats(players, option)
    val evaluatedValue = FixedInformationEvaluator.chefEvilPairs(players) { player ->
        player.seat in effectiveEvilSeats
    }
    if (evaluatedValue != value) return emptySet()

    val seated = players.sortedBy(PlayerState::seat)
    if (seated.size < 2) return emptySet()
    return buildSet {
        seated.indices.forEach { index ->
            val first = seated[index]
            val second = seated[(index + 1) % seated.size]
            if (first.seat in effectiveEvilSeats && second.seat in effectiveEvilSeats) {
                add(first.seat)
                add(second.seat)
            }
        }
    }
}

internal fun clocktowerChefResultChoices(
    step: ClocktowerNightStepUi,
    players: List<PlayerState>,
    automaticStorytellerInfo: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
    resultFirstRegistrationCandidates: List<ClocktowerDisplayOption>,
    structuredNumberUiModel: StructuredNumberInformationUiModel?,
): List<ClocktowerChefResultChoice> {
    fun numericValue(option: ClocktowerDisplayOption): Int? =
        (option.proposition as? InformationProposition.NumericResult)?.value

    if (automaticStorytellerInfo) {
        return listOfNotNull(automaticDisplayOption?.let { option ->
            val value = numericValue(option) ?: return@let null
            ClocktowerChefResultChoice(
                key = clocktowerInformationCandidateId(option),
                value = value,
                sourceKind = ClocktowerChefResultSourceKind.DisplayOption,
                displayOption = option,
                recommended = true,
                effectivePairSeats = clocktowerChefEffectivePairSeats(players, option, value),
            )
        })
    }

    if (resultFirstRegistrationCandidates.isNotEmpty()) {
        return resultFirstRegistrationCandidates.mapNotNull { option ->
            val value = numericValue(option) ?: return@mapNotNull null
            ClocktowerChefResultChoice(
                key = clocktowerInformationCandidateId(option),
                value = value,
                sourceKind = ClocktowerChefResultSourceKind.DisplayOption,
                displayOption = option,
                recommended = option.isDefaultRecommendation,
                effectivePairSeats = clocktowerChefEffectivePairSeats(players, option, value),
            )
        }
    }

    structuredNumberUiModel?.let { model ->
        return model.choices.map { choice ->
            ClocktowerChefResultChoice(
                key = choice.candidateId,
                value = choice.value,
                sourceKind = ClocktowerChefResultSourceKind.Structured,
                structuredCandidateId = choice.candidateId,
                recommended = choice.recommended,
                // An impaired Chef may legally receive an arbitrary value. There is no truthful
                // seat witness to invent for such a value, so keep only the static information hints.
                effectivePairSeats = emptySet(),
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
            effectivePairSeats = clocktowerChefEffectivePairSeats(players, option = null, value = direct.value),
        ),
    )
}

@Composable
internal fun ClocktowerChefSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    actualEvilSeats: Set<Int>,
    recluseSeat: Int?,
    choices: List<ClocktowerChefResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerChefResultChoice) -> Unit,
) {
    if (choices.isEmpty()) return
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.first()
    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice.key) }
    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice

    Dialog(
        onDismissRequest = { if (canGoPrevious) onPrevious() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    val visual = clocktowerChefSeatVisual(
                        seatNumber = seat.seatId.number,
                        actorSeat = actorSeat,
                        actualEvilSeats = actualEvilSeats,
                        recluseSeat = recluseSeat,
                        effectivePairSeats = selectedChoice.effectivePairSeats,
                        language = language,
                    )
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = visual.state,
                        isCurrentActor = visual.isCurrentActor,
                        badge = visual.badge,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
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
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (language == "en") {
                                "★ evil · R Recluse · ✓★ counted in the selected result"
                            } else {
                                "★ 邪恶提示 · 隐 隐士 · ✓★ 当前结果计入相邻对"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))

                        if (choices.size > 1) {
                            Text(
                                text = if (language == "en") "Choose the number to show" else "选择要展示的数字",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            choices.chunked(3).forEach { rowChoices ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    rowChoices.forEach { choice ->
                                        if (choice.key == selectedChoice.key) {
                                            Button(
                                                onClick = { selectedKey = choice.key },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(choice.value.toString(), maxLines = 1)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { selectedKey = choice.key },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(choice.value.toString(), maxLines = 1)
                                            }
                                        }
                                    }
                                    repeat(3 - rowChoices.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                            Button(
                                onClick = { onConfirm(selectedChoice) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (language == "en") {
                                        "Show information: ${selectedChoice.value}"
                                    } else {
                                        "展示信息：${selectedChoice.value}"
                                    },
                                    maxLines = 1,
                                )
                            }
                        } else {
                            Button(
                                onClick = { onConfirm(selectedChoice) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (language == "en") {
                                        "Show information: ${selectedChoice.value}"
                                    } else {
                                        "展示信息：${selectedChoice.value}"
                                    },
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    ClocktowerSquareTableStepNavigation(
                        language = language,
                        canGoPrevious = canGoPrevious,
                        onPrevious = onPrevious,
                        onNext = onNext,
                    )
                }
            }
        }
    }
}
