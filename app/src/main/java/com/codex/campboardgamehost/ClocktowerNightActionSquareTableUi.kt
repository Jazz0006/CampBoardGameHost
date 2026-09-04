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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

internal fun clocktowerSingleTargetSeatState(
    seatNumber: Int,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
): ClocktowerSquareTableSeatState = when {
    selectedSeat == seatNumber -> ClocktowerSquareTableSeatState.SelectedFirst
    seatNumber in selectableSeats -> ClocktowerSquareTableSeatState.Selectable
    else -> ClocktowerSquareTableSeatState.Disabled
}

internal data class ClocktowerNightActionSeatPresentation(
    val targetState: ClocktowerSquareTableSeatState,
    val isCurrentActor: Boolean,
)

internal fun clocktowerSingleTargetSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = clocktowerSingleTargetSeatState(
        seatNumber = seatNumber,
        selectedSeat = selectedSeat,
        selectableSeats = selectableSeats,
    ),
    isCurrentActor = seatNumber == actorSeat,
)

internal fun clocktowerTwoTargetSeatState(
    seatNumber: Int,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
): ClocktowerSquareTableSeatState = when {
    selectedSeats.getOrNull(0) == seatNumber -> ClocktowerSquareTableSeatState.SelectedFirst
    selectedSeats.getOrNull(1) == seatNumber -> ClocktowerSquareTableSeatState.SelectedSecond
    seatNumber in selectableSeats -> ClocktowerSquareTableSeatState.Selectable
    else -> ClocktowerSquareTableSeatState.Disabled
}

internal fun clocktowerTwoTargetSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = clocktowerTwoTargetSeatState(
        seatNumber = seatNumber,
        selectedSeats = selectedSeats,
        selectableSeats = selectableSeats,
    ),
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerNightActionWakeInstruction(instruction: String?) {
    instruction?.takeIf { it.isNotBlank() }?.let { value ->
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
internal fun ClocktowerSingleTargetSquareTableDialog(
    seats: List<HostSeatPresentation>,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    title: String,
    helper: String?,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    secondaryActionLabel: String? = null,
    secondaryActionEnabled: Boolean = false,
    onSecondaryAction: () -> Unit = {},
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSingleTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeat = selectedSeat,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
        onSeatSelected = onSeatSelected,
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            helper?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = selectedSeat?.let { seat ->
                    if (language == "en") "Selected: P$seat" else "已选择：P$seat"
                } ?: if (language == "en") "Select a player" else "选择一名玩家",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selectedSeat != null) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )

            secondaryActionLabel?.let { label ->
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onSecondaryAction,
                    enabled = secondaryActionEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(label)
                }
            }

            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Finish / Next" else "完成 / 下一步")
            }

            if (canGoPrevious) {
                TextButton(onClick = onPrevious) {
                    Text(if (language == "en") "Previous step" else "上一步")
                }
            }
        }
    }
}

@Composable
internal fun ClocktowerNightActionSquareTableDialog(
    seats: List<HostSeatPresentation>,
    enabled: Boolean,
    language: String,
    seatPresentation: (Int) -> ClocktowerNightActionSeatPresentation,
    onSeatSelected: (Int) -> Unit,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    centerContent: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            if (canGoPrevious) onPrevious()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    val presentation = seatPresentation(seat.seatId.number)
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = presentation.targetState,
                        isCurrentActor = presentation.isCurrentActor,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = if (enabled) {
                    ClocktowerSquareTableInteractionMode.Selectable
                } else {
                    ClocktowerSquareTableInteractionMode.ReadOnly
                },
                onSeatClick = { renderKey ->
                    seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }
                        ?.seatId
                        ?.number
                        ?.let(onSeatSelected)
                },
            ) {
                centerContent()
            }
        }
    }
}
