package com.codex.campboardgamehost

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun ClocktowerDarkTheme(content: @Composable () -> Unit) {
    val typography = MaterialTheme.typography
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = ClocktowerContrastPolicy.accent,
            onPrimary = ClocktowerContrastPolicy.accentForeground,
            secondary = ClocktowerContrastPolicy.secondary,
            onSecondary = ClocktowerContrastPolicy.secondaryForeground,
            background = ClocktowerContrastPolicy.background,
            onBackground = ClocktowerContrastPolicy.foreground,
            surface = ClocktowerContrastPolicy.surface,
            onSurface = ClocktowerContrastPolicy.foreground,
            surfaceVariant = ClocktowerContrastPolicy.secondarySurface,
            onSurfaceVariant = ClocktowerContrastPolicy.mutedForeground,
            error = Color(0xFFC9574A),
            onError = Color(0xFFF7F1E6),
        ),
        typography = typography,
    ) {
        // A dark Material colorScheme does not by itself reset LocalContentColor.
        // Views using Modifier.background() rather than Surface inherited the
        // outer app's dark text on a near-black canvas. Bind a legible default
        // for all Clocktower dark-theme screens; Card/Surface may override it.
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
        ) {
            content()
        }
    }
}
