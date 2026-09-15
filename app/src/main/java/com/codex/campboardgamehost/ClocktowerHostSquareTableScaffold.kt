package com.codex.campboardgamehost

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Shared full-screen navigation and stable-seat routing for Clocktower square-table surfaces. */
@Composable
internal fun ClocktowerHostSquareTableScaffold(
    seats: List<HostSeatPresentation>,
    language: String,
    previousEnabled: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    interactionMode: ClocktowerSquareTableInteractionMode,
    seatUiModel: (HostSeatPresentation) -> ClocktowerSquareTableSeatUiModel,
    onSeatSelected: ((Int) -> Unit)? = null,
    nextEnabled: Boolean = true,
    onBack: () -> Unit = { if (previousEnabled) onPrevious() },
    centerContent: @Composable () -> Unit,
) {
    ClocktowerHostFullScreenScaffold(
        previousLabel = if (language == "en") "← Previous" else "← 上一步",
        hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
        nextLabel = if (language == "en") "Next →" else "下一步 →",
        previousEnabled = previousEnabled,
        nextEnabled = nextEnabled,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        onBack = onBack,
    ) {
        ClocktowerSquareTableSeatSurface(
            seats = seats.map { seat ->
                val model = seatUiModel(seat)
                if (seat.isPoisoned) {
                    model.copy(
                        badge = listOfNotNull("☠", model.badge).joinToString(" "),
                        badgeTone = ClocktowerSquareTableBadgeTone.Warning,
                    )
                } else {
                    model
                }
            },
            modifier = Modifier.fillMaxSize(),
            interactionMode = interactionMode,
            onSeatClick = { renderKey ->
                onSeatSelected?.let { select ->
                    seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }
                        ?.seatId
                        ?.number
                        ?.let(select)
                }
            },
        ) {
            centerContent()
        }
    }
}
