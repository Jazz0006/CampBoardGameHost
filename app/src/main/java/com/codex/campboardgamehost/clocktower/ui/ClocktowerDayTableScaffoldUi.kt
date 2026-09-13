package com.codex.campboardgamehost

import androidx.compose.runtime.Composable

/** Shared Day table adapter onto the single Activity-root Clocktower host scaffold. */
@Composable
internal fun ClocktowerDayTableScaffold(
    previousLabel: String,
    hostToolsLabel: String,
    nextLabel: String,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    tableContent: @Composable () -> Unit,
) {
    ClocktowerDarkTheme {
        ClocktowerHostFullScreenScaffold(
            previousLabel = previousLabel,
            hostToolsLabel = hostToolsLabel,
            nextLabel = nextLabel,
            previousEnabled = previousEnabled,
            nextEnabled = nextEnabled,
            onPrevious = onPrevious,
            onHostTools = onHostTools,
            onNext = onNext,
            body = tableContent,
        )
    }
}
