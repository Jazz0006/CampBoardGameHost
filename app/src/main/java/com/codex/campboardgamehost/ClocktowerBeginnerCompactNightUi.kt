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


internal fun clocktowerBeginnerNeutralSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerBeginnerReadOnlyRevealDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShow: () -> Unit,
    buttonLabel: String? = null,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = false,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerBeginnerNeutralSeatPresentation(seatNumber, actorSeat)
        },
        onSeatSelected = {},
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            androidx.compose.material3.Button(onClick = onShow) {
                androidx.compose.material3.Text(
                    buttonLabel ?: if (language == "en") "Show to player" else "展示给玩家",
                )
            }
        }
    }
}

@Composable
internal fun ClocktowerBeginnerSingleTargetRevealDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    selectedSeat: Int?,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShow: (() -> Unit)?,
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
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            onShow?.let { reveal ->
                androidx.compose.material3.Button(onClick = reveal) {
                    androidx.compose.material3.Text(if (language == "en") "Show to player" else "展示给玩家")
                }
            }
        }
    }
}

@Composable
internal fun ClocktowerBeginnerTwoTargetRevealDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    wakeInstruction: String?,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onShow: (() -> Unit)?,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = enabled,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerTwoTargetSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                selectedSeats = selectedSeats,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
        onSeatSelected = onSeatSelected,
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            onShow?.let { reveal ->
                androidx.compose.material3.Button(onClick = reveal) {
                    androidx.compose.material3.Text(if (language == "en") "Show to player" else "展示给玩家")
                }
            }
        }
    }
}
