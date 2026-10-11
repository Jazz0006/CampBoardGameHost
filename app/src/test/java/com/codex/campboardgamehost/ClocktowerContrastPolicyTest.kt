package com.codex.campboardgamehost

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

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
                "$description has insufficient WCAG AA contrast: ${p.contrastRatio(pair.first, pair.second)}",
                p.contrastRatio(pair.first, pair.second) >= 4.5,
            )
        }
    }

    @Test
    fun `contrast checker detects illegible inherited light-theme text`() {
        val p = ClocktowerContrastPolicy
        assertTrue(p.contrastRatio(Color.Black, p.background) < 4.5)
        assertTrue(p.contrastRatio(p.foreground, p.background) >= 4.5)
    }
}
