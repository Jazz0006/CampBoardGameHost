package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

@Composable
internal fun CampBoardGameHostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF2F5D50),
            secondary = Color(0xFFD96C3B),
            background = Color(0xFFF8F6F0),
            surface = Color(0xFFFFFCF6),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1F2925),
            onSurface = Color(0xFF1F2925),
        ),
        content = content,
    )
}
