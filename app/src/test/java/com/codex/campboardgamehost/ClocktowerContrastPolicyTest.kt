package com.codex.campboardgamehost

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ClocktowerContrastPolicyTest {
    /** Every standard text/background pairing must pass WCAG AA normal-text contrast. */
    @Test
    fun `all dark host text tokens meet minimum contrast`() {
        val p = ClocktowerContrastPolicy
        val pairs = mapOf(
            "body on background" to (p.foreground to p.background),
            "muted on background" to (p.mutedForeground to p.background),
            "accent on background" to (p.accent to p.background),
            "body on surface" to (p.foreground to p.surface),
            "muted on surface" to (p.mutedForeground to p.surface),
            "accent on surface" to (p.accent to p.surface),
            "body on secondary surface" to (p.foreground to p.secondarySurface),
            "muted on secondary surface" to (p.mutedForeground to p.secondarySurface),
            "accent foreground on accent" to (p.accentForeground to p.accent),
            "secondary foreground on secondary" to (p.secondaryForeground to p.secondary),
        )
        pairs.forEach { (description, pair) ->
            assertTrue(
                "$description has insufficient WCAG AA contrast: ${contrastRatio(pair.first, pair.second)}",
                contrastRatio(pair.first, pair.second) >= 4.5,
            )
        }
    }

    @Test
    fun `actual shared Material colors are readable on every role surface`() {
        val c = ClocktowerHostColorScheme
        val pairs = mapOf(
            "host background" to (c.onBackground to c.background),
            "host card" to (c.onSurface to c.surface),
            "host secondary card" to (c.onSurfaceVariant to c.surfaceVariant),
            "primary button" to (c.onPrimary to c.primary),
            "primary container" to (c.onPrimaryContainer to c.primaryContainer),
            "secondary button" to (c.onSecondary to c.secondary),
            "secondary container" to (c.onSecondaryContainer to c.secondaryContainer),
            "tertiary button" to (c.onTertiary to c.tertiary),
            "error button" to (c.onError to c.error),
            "error text on card" to (c.error to c.surface),
            "error text on background" to (c.error to c.background),
            "inverted UI" to (c.inverseOnSurface to c.inverseSurface),
            "secondary text on host background" to (c.onSurfaceVariant to c.background),
        )
        pairs.forEach { (name, pair) ->
            assertTrue("$name is not legible at 4.5:1",
                contrastRatio(pair.first, pair.second) >= 4.5)
        }
    }

    @Test
    fun `contrast checker detects illegible inherited light-theme text`() {
        val p = ClocktowerContrastPolicy
        assertTrue(contrastRatio(Color.Black, p.background) < 4.5)
        assertTrue(contrastRatio(p.foreground, p.background) >= 4.5)
    }
    /** 8-bit sRGB foreground/background contrast, including full alpha requirement. */
    private fun contrastRatio(foreground: Color, background: Color): Double {
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
