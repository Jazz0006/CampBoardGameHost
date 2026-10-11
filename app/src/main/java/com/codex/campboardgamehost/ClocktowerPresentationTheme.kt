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
            primary = Color(0xFFC5A56A),
            onPrimary = Color(0xFF17120A),
            secondary = Color(0xFF61798A),
            onSecondary = Color(0xFFF7F1E6),
            background = Color(0xFF0B0D10),
            onBackground = Color(0xFFF1EADC),
            surface = Color(0xFF14171C),
            onSurface = Color(0xFFF1EADC),
            surfaceVariant = Color(0xFF1B1F25),
            onSurfaceVariant = Color(0xFFAAA397),
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
