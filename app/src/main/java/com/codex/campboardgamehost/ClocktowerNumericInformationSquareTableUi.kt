package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Chef and Empath render on the one shared Host square table, not a parallel
 * number-only screen. Seats are read-only Session projections; the only mutable
 * draft is the selected number. Next remains owned by the shared navigation bar.
 */
@Composable
internal fun ClocktowerNumericInformationSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    values: List<Int>,
    recommendedValue: Int?,
    lastPreviewValue: Int?,
    automaticOnly: Boolean,
    language: String,
    draftKey: String,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (Int) -> Unit,
    onDraftChanged: () -> Unit,
) {
    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = false,
        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
        seatUiModel = { seat ->
            val content = hostSeatContentPresentation(seat, language)
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(),
                seatNumber = seat.seatId.number,
                label = content.primaryLabel,
                detailLabels = content.detailLabels,
                actualAlignment = seat.actualAlignment,
                isAlive = seat.isAlive,
                hasUnspentGhostVote = seat.hasUnspentGhostVote,
                isCurrentActor = seat.seatId.number == actorSeat,
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            if (values.isEmpty()) {
                Text(
                    if (language == "en") "Current numeric information unavailable"
                    else "当前数字信息尚不可用",
                )
            } else {
                ClocktowerResolvedNumericResultUi(
                    values = values,
                    recommendedValue = recommendedValue,
                    lastPreviewValue = lastPreviewValue,
                    automaticOnly = automaticOnly,
                    language = language,
                    draftKey = draftKey,
                    onConfirm = onConfirm,
                    onDraftChanged = onDraftChanged,
                )
            }
        }
    }
}
