package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Stateless three-slot action bar shared by Storyteller-controlled flow screens.
 *
 * This composable owns presentation only. Callers remain responsible for deciding whether each
 * capability exists, whether it is enabled, and which existing flow callback it invokes.
 */
@Composable
internal fun HostBottomActionBar(
    previousLabel: String,
    hostToolsLabel: String,
    nextLabel: String,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    previousEnabled: Boolean = true,
    hostToolsEnabled: Boolean = true,
    nextEnabled: Boolean = true,
    previousVisible: Boolean = true,
    hostToolsVisible: Boolean = true,
    nextVisible: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NavigationActionButton(
            label = previousLabel,
            enabled = previousEnabled,
            visible = previousVisible,
            onClick = onPrevious,
            emphasis = NavigationActionEmphasis.Secondary,
            modifier = Modifier.weight(1f),
        )
        NavigationActionButton(
            label = hostToolsLabel,
            enabled = hostToolsEnabled,
            visible = hostToolsVisible,
            onClick = onHostTools,
            emphasis = NavigationActionEmphasis.Utility,
            modifier = Modifier.weight(1f),
        )
        NavigationActionButton(
            label = nextLabel,
            enabled = nextEnabled,
            visible = nextVisible,
            onClick = onNext,
            emphasis = NavigationActionEmphasis.Primary,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class NavigationActionEmphasis {
    Secondary,
    Utility,
    Primary,
}

@Composable
private fun NavigationActionButton(
    label: String,
    enabled: Boolean,
    visible: Boolean,
    onClick: () -> Unit,
    emphasis: NavigationActionEmphasis,
    modifier: Modifier,
) {
    if (!visible) {
        Spacer(modifier = modifier.heightIn(min = 48.dp))
        return
    }

    val buttonModifier = modifier.heightIn(min = 48.dp)

    when (emphasis) {
        NavigationActionEmphasis.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            NavigationActionLabel(label)
        }

        NavigationActionEmphasis.Utility -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            NavigationActionLabel(label)
        }

        NavigationActionEmphasis.Primary -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            NavigationActionLabel(label)
        }
    }
}

@Composable
private fun NavigationActionLabel(label: String) {
    Text(
        text = label,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelMedium,
    )
}
