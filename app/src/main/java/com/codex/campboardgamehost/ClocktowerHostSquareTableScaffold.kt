package com.codex.campboardgamehost

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/** Optional presentation supplied by the owning page; the table knows no feature state. */
internal val LocalClocktowerTableCenterHeader = compositionLocalOf<@Composable () -> Unit> { {} }

/** Shared full-screen navigation and stable-seat routing for Clocktower square-table surfaces. */
@Composable
internal fun ClocktowerHostSquareTableScaffold(
    seats: List<HostSeatPresentation>,
    language: String,
    previousEnabled: Boolean,
    onPrevious: () -> Unit = {},
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    interactionMode: ClocktowerSquareTableInteractionMode,
    seatUiModel: (HostSeatPresentation) -> ClocktowerSquareTableSeatUiModel,
    onSeatSelected: ((Int) -> Unit)? = null,
    nextEnabled: Boolean = true,
    nextLabel: String = if (language == "en") "Next →" else "下一步 →",
    onBack: () -> Unit = { if (previousEnabled) onPrevious() },
    centerHeader: @Composable () -> Unit = LocalClocktowerTableCenterHeader.current,
    centerContent: @Composable () -> Unit,
) {
    ClocktowerHostFullScreenScaffold(
        previousLabel = if (language == "en") "← Previous" else "← 上一步",
        hostToolsLabel = if (language == "en") "Host Tools" else "主持工具",
        nextLabel = nextLabel,
        previousEnabled = previousEnabled,
        nextEnabled = nextEnabled,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        onBack = onBack,
    ) {
        ClocktowerSquareTableSeatSurface(
            seats = seats.map { seat ->
                seatUiModel(seat).copy(
                    isDrunk = seat.actualRole?.roleId.equals("drunk", ignoreCase = true),
                    isPoisoned = seat.isPoisoned,
                )
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
            // Pair-information, Investigator and all future full-screen role
            // surfaces share the same legible semantic foreground by contract.
            // Do not let Text() inherit the outer app's light-theme dark ink.
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            ) {
                Column(Modifier.fillMaxSize()) {
                    centerHeader()
                    Box(Modifier.weight(1f)) { centerContent() }
                }
            }
        }
    }
}
