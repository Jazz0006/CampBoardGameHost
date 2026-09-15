package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal fun clocktowerFortuneTellerSeatState(
    seatNumber: Int,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
): ClocktowerSquareTableSeatState = when {
    seatNumber in selectedSeats -> ClocktowerSquareTableSeatState.SelectedHighlighted
    seatNumber in selectableSeats -> ClocktowerSquareTableSeatState.Selectable
    else -> ClocktowerSquareTableSeatState.Disabled
}

internal fun clocktowerFortuneTellerResultActions(
    legalResults: Set<Boolean>,
    recommendedResult: Boolean?,
): List<Boolean> {
    if (legalResults.isEmpty()) return emptyList()

    return buildList {
        recommendedResult
            ?.takeIf { it in legalResults }
            ?.let(::add)
        legalResults
            .asSequence()
            .filterNot { it == recommendedResult }
            .sortedDescending()
            .forEach(::add)
    }
}

@Composable
internal fun ClocktowerFortuneTellerSquareTableDialog(
    seats: List<HostSeatPresentation>,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    redHerringSeat: Int? = null,
    legalResults: Set<Boolean>,
    recommendedResult: Boolean?,
    automaticStorytellerInfo: Boolean,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onResultSelected: (Boolean) -> Unit,
    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
) {
    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = if (enabled) {
            ClocktowerSquareTableInteractionMode.Selectable
        } else {
            ClocktowerSquareTableInteractionMode.ReadOnly
        },
        onSeatSelected = onSeatSelected,
        seatUiModel = { seat ->
            val content = hostSeatContentPresentation(seat, language)
            val isSelected = seat.seatId.number in selectedSeats
            val isRedHerring = seat.seatId.number == redHerringSeat
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(),
                seatNumber = seat.seatId.number,
                label = content.primaryLabel,
                detailLabels = content.detailLabels,
                isAlive = seat.isAlive,
                hasUnspentGhostVote = seat.hasUnspentGhostVote,
                state = clocktowerFortuneTellerSeatState(
                    seatNumber = seat.seatId.number,
                    selectedSeats = selectedSeats,
                    selectableSeats = if (enabled) selectableSeats else emptySet(),
                ),
                isCurrentActor = seat.seatId.number == actorSeat,
                suppressDefaultStateMarker = isSelected,
                badge = if (isRedHerring) {
                    if (language == "en") "RH" else "鲱"
                } else {
                    null
                },
                badgeTone = if (isRedHerring) {
                    ClocktowerSquareTableBadgeTone.Warning
                } else {
                    ClocktowerSquareTableBadgeTone.Default
                },
            )
        },
    ) {
        ClocktowerFortuneTellerCenterControls(
            wakeInstruction = wakeInstruction,
            selectedSeats = selectedSeats,
            legalResults = legalResults,
            recommendedResult = recommendedResult,
            automaticStorytellerInfo = automaticStorytellerInfo,
            language = language,
            onResultSelected = onResultSelected,
            onAutomaticResultSelected = onAutomaticResultSelected,
        )
    }
}

@Composable
private fun ClocktowerFortuneTellerCenterControls(
    wakeInstruction: String?,
    selectedSeats: List<Int>,
    legalResults: Set<Boolean>,
    recommendedResult: Boolean?,
    automaticStorytellerInfo: Boolean,
    language: String,
    onResultSelected: (Boolean) -> Unit,
    onAutomaticResultSelected: (Boolean) -> Unit,
) {
    val actions = clocktowerFortuneTellerResultActions(
        legalResults = legalResults,
        recommendedResult = recommendedResult,
    )
    val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClocktowerNightActionWakeInstruction(wakeInstruction)
        if (completePair) {
            when {
                actions.isEmpty() -> {
                    Text(
                        text = if (language == "en") "No legal result" else "无可用结果",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                automaticStorytellerInfo -> {
                    val result = recommendedResult?.takeIf { it in legalResults } ?: actions.first()
                    Button(
                        onClick = { onAutomaticResultSelected(result) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(clocktowerFortuneTellerResultLabel(result, language))
                    }
                }

                actions.size == 1 -> {
                    Button(
                        onClick = { onResultSelected(actions.single()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(clocktowerFortuneTellerResultLabel(actions.single(), language))
                    }
                }

                else -> actions.forEachIndexed { index, value ->
                    Text(
                        text = if (index == 0 && value == recommendedResult) {
                            if (language == "en") "Recommended" else "推荐"
                        } else {
                            if (language == "en") "Other option" else "另一个选项"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                    if (index == 0 && value == recommendedResult) {
                        Button(
                            onClick = { onResultSelected(value) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(clocktowerFortuneTellerResultLabel(value, language))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onResultSelected(value) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(clocktowerFortuneTellerResultLabel(value, language))
                        }
                    }
                    if (index != actions.lastIndex) Spacer(Modifier.height(4.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

private fun clocktowerFortuneTellerResultLabel(
    value: Boolean,
    language: String,
): String = when {
    language == "en" && value -> "YES"
    language == "en" -> "NO"
    value -> "有"
    else -> "没有"
}
