package com.codex.campboardgamehost

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

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

    /** 8-bit sRGB foreground/background contrast, including full alpha requirement. */
    fun contrastRatio(foreground: Color, background: Color): Double {
        require(foreground.alpha == 1f && background.alpha == 1f) {
            "Text contrast requires opaque semantic colors."
        }
        fun linear(component: Float): Double {
            val v = component.toDouble()
            return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        fun luminance(color: Color): Double =
            0.2126 * linear(color.red) +
                0.7152 * linear(color.green) +
                0.0722 * linear(color.blue)
        val first = luminance(foreground)
        val second = luminance(background)
        return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    }
}
