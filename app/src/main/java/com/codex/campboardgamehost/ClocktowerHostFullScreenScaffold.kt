package com.codex.campboardgamehost

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Single Activity-window owner for persistent Clocktower host workspaces.
 *
 * The body owns game content only. This scaffold exclusively owns the persistent Previous / Host
 * Tools / Next row and its navigation-bar inset policy, so Day and Night cannot drift into parallel
 * window or bottom-navigation implementations.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerHostFullScreenScaffold(
    previousLabel: String,
    hostToolsLabel: String,
    nextLabel: String,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    previousEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    onBack: (() -> Unit)? = null,
    body: @Composable () -> Unit,
) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }
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
                body()
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
                        .clocktowerHostBottomNavigationBarPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
