package com.codex.campboardgamehost

import androidx.compose.ui.graphics.Color

/**
 * Central dark host foreground/background pairs. Surfaces and high-value text must
 * use these semantic colors; avoid inheriting an unrelated light-theme foreground.
 *
 * WCAG AA normal-size text minimum: 4.5:1.
 */
internal object ClocktowerContrastPolicy {
    val background = Color(0xFF0B0D10)
    val foreground = Color(0xFFF1EADC)
    val surface = Color(0xFF14171C)
    val secondarySurface = Color(0xFF1B1F25)
    val mutedForeground = Color(0xFFAAA397)
    val accent = Color(0xFFC5A56A)
    val accentForeground = Color(0xFF17120A)
    val secondary = Color(0xFF506677)
    val secondaryForeground = Color(0xFFF7F1E6)
    // Phase-independent true-alignment identity, shared by both table themes.
    val goodSeat = Color(0xFF24568D)
    val evilSeat = Color(0xFF8F2D3D)
}
