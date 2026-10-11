package com.codex.campboardgamehost

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val ClocktowerHostColorScheme = androidx.compose.material3.darkColorScheme(
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
            primaryContainer = ClocktowerContrastPolicy.surface,
            onPrimaryContainer = ClocktowerContrastPolicy.foreground,
            secondaryContainer = ClocktowerContrastPolicy.secondarySurface,
            onSecondaryContainer = ClocktowerContrastPolicy.foreground,
            tertiary = ClocktowerContrastPolicy.accent,
            onTertiary = ClocktowerContrastPolicy.accentForeground,
            tertiaryContainer = ClocktowerContrastPolicy.surface,
            onTertiaryContainer = ClocktowerContrastPolicy.foreground,
            outline = ClocktowerContrastPolicy.mutedForeground,
            outlineVariant = ClocktowerContrastPolicy.secondary,
            inverseSurface = ClocktowerContrastPolicy.foreground,
            inverseOnSurface = ClocktowerContrastPolicy.background,
            inversePrimary = ClocktowerContrastPolicy.secondary,
            error = Color(0xFFE1786F),
            onError = ClocktowerContrastPolicy.background,
        )

@Composable
internal fun ClocktowerDarkTheme(content: @Composable () -> Unit) {
    val typography = MaterialTheme.typography
    MaterialTheme(
        colorScheme = ClocktowerHostColorScheme,
        typography = typography,
    ) {
        // A dark Material colorScheme does not by itself reset LocalContentColor.
        // Views using Modifier.background() rather than Surface inherited the
        // outer app's dark text on a near-black canvas. Bind a legible default
        // for all Clocktower dark-theme screens; Card/Surface may override it.
        // Own the canvas as well as the semantic foreground. Providing just a
        // dark colorScheme or Modifier.background leaves local text colors
        // inherited from outer light-theme hosts on some full-screen paths.
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            ) {
                content()
            }
        }
    }
}
