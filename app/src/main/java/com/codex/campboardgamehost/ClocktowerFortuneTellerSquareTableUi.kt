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

/** Both Boolean answers remain confirmable; an AI recommendation only changes display order. */
internal fun clocktowerFortuneTellerResultActions(recommendedResult: Boolean?): List<Boolean> =
    if (recommendedResult == null) listOf(true, false) else listOf(recommendedResult, !recommendedResult)

/** Automatic mode cannot display an answer that AI has not chosen. */
internal fun clocktowerFortuneTellerVisibleResults(
    recommendedResult: Boolean?,
    automaticOnly: Boolean,
): List<Boolean> = if (automaticOnly) listOfNotNull(recommendedResult)
else clocktowerFortuneTellerResultActions(recommendedResult)

@Composable
internal fun ClocktowerFortuneTellerSquareTableDialog(
    seats: List<HostSeatPresentation>,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    redHerringSeat: Int? = null,
    recommendedResult: Boolean?,
    lastPreviewResult: Boolean?,
    automaticOnly: Boolean = false,
    language: String,
    onSeatSelected: (Int) -> Unit,
    onResultSelected: (Boolean) -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
) {
    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = false,

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
                actualAlignment = seat.actualAlignment,
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
            recommendedResult = recommendedResult,
            lastPreviewResult = lastPreviewResult,
            automaticOnly = automaticOnly,
            language = language,
            onResultSelected = onResultSelected,
        )
    }
}

@Composable
private fun ClocktowerFortuneTellerCenterControls(
    wakeInstruction: String?,
    selectedSeats: List<Int>,
    recommendedResult: Boolean?,
    lastPreviewResult: Boolean?,
    automaticOnly: Boolean,
    language: String,
    onResultSelected: (Boolean) -> Unit,
) {
    val choices = clocktowerFortuneTellerVisibleResults(recommendedResult, automaticOnly)
    val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2
    Column(
        modifier = Modifier.fillMaxSize().padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClocktowerNightActionWakeInstruction(wakeInstruction)
        if (completePair) {
            if (automaticOnly && choices.isEmpty()) {
                Text(
                    if (language == "en") "Waiting for AI result" else "等待 AI 确定当前信息",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            choices.forEachIndexed { index, result ->
                Text(
                    text = if (result == lastPreviewResult) {
                        if (language == "en") "Last preview" else "最近预览"
                    } else if (recommendedResult != null && result == recommendedResult) {
                        if (language == "en") "Recommended" else "推荐"
                    } else {
                        if (language == "en") "Other option" else "另一个选项"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
                if (result == lastPreviewResult || (lastPreviewResult == null && result == recommendedResult)) {
                    Button(
                        onClick = { onResultSelected(result) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(clocktowerFortuneTellerResultLabel(result, language))
                    }
                } else {
                    OutlinedButton(
                        onClick = { onResultSelected(result) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(clocktowerFortuneTellerResultLabel(result, language))
                    }
                }
                if (index != choices.lastIndex) Spacer(Modifier.height(4.dp))
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
