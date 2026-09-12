package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared full-screen shell for persistent Day table workspaces.
 *
 * The square table owns the flexible body only. Global Previous / Host Tools / Next navigation is
 * always rendered as a sibling below the table, never inside the table's center content.
 */
@OptIn(ExperimentalLayoutApi::class)
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    tableContent()
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
                ) {
                    HostBottomActionBar(
                        previousLabel = previousLabel,
                        hostToolsLabel = hostToolsLabel,
                        nextLabel = nextLabel,
                        previousEnabled = previousEnabled,
                        nextEnabled = nextEnabled,
                        onPrevious = onPrevious,
                        onHostTools = onHostTools,
                        onNext = onNext,
                        modifier = Modifier
                            .windowInsetsPadding(
                                WindowInsets.navigationBarsIgnoringVisibility.only(WindowInsetsSides.Bottom),
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
