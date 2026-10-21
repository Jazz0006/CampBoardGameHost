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

/**
 * Day and Night share one theme/canvas owner. Only the phase palette changes;
 * game/session facts and card alignment never own UI theme selection.
 */
internal val ClocktowerDayColorScheme = androidx.compose.material3.lightColorScheme(
    primary = ClocktowerContrastPolicy.goodSeat,
    onPrimary = Color.White,
    secondary = Color(0xFF374151),
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF17212F),
    surface = Color(0xFFF5F7FA),
    onSurface = Color(0xFF17212F),
    surfaceVariant = Color(0xFFE9EDF2),
    onSurfaceVariant = Color(0xFF374151),
    primaryContainer = Color(0xFFDFEAF6),
    onPrimaryContainer = Color(0xFF153B64),
    secondaryContainer = Color(0xFFE7EBF0),
    onSecondaryContainer = Color(0xFF17212F),
    tertiary = ClocktowerContrastPolicy.evilSeat,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8E1E5),
    onTertiaryContainer = Color(0xFF6D1F30),
    outline = Color(0xFF636C78),
    outlineVariant = Color(0xFFC7CFD9),
    error = Color(0xFFAE2436),
    onError = Color.White,
)

@Composable
internal fun ClocktowerDarkTheme(content: @Composable () -> Unit) =
    ClocktowerPhaseTheme(isNight = true, content = content)

@Composable
internal fun ClocktowerLightTheme(content: @Composable () -> Unit) =
    ClocktowerPhaseTheme(isNight = false, content = content)

@Composable
private fun ClocktowerPhaseTheme(isNight: Boolean, content: @Composable () -> Unit) {
    val typography = MaterialTheme.typography
    MaterialTheme(
        colorScheme = if (isNight) ClocktowerHostColorScheme else ClocktowerDayColorScheme,
        typography = typography,
    ) {
        // The chosen phase palette must own both canvas and foreground;
        // Modifier.background() alone can inherit ink from an outer theme.
        // Components may override only with a contrast-safe surface pair.
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
