package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal fun clocktowerUsesBeginnerCompactNightGuidance(instruction: String?): Boolean {
    val lines = instruction
        ?.lines()
        ?.map(String::trim)
        ?.filter(String::isNotBlank)
        .orEmpty()
    return lines.size in 2..3 &&
        (lines.firstOrNull()?.startsWith("唤醒 ") == true || lines.firstOrNull()?.startsWith("Wake ") == true)
}

internal fun clocktowerBeginnerPairSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerBeginnerSingleTargetAbilityDialog(
    seats: List<HostSeatPresentation>,
    presentation: ClocktowerSingleTargetAbilityPresentation,
    language: String,
    canGoPrevious: Boolean,
    onHostTools: () -> Unit,
    onEvent: (ClocktowerSingleTargetEvent) -> Unit,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = presentation.selection.enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSingleTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = presentation.actorSeat,
                selectedSeat = presentation.selection.selectedSeat,
                selectableSeats = if (presentation.selection.enabled) presentation.selection.selectableSeats else emptySet(),
            )
        },
        onSeatSelected = { onEvent(ClocktowerSingleTargetEvent.SelectSeat(it)) },
        canGoPrevious = canGoPrevious,
        onPrevious = { onEvent(ClocktowerSingleTargetEvent.Previous) },
        onHostTools = onHostTools,
        onNext = { onEvent(ClocktowerSingleTargetEvent.Next) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(presentation.wakeInstruction)
        }
    }
}
