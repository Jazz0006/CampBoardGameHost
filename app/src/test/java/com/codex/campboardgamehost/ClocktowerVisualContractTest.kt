package com.codex.campboardgamehost

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** High-value safety checks for the Day/Night table and private actual-alignment projection. */
class ClocktowerVisualContractTest {
    private fun contrast(foreground: Color, background: Color): Double {
        val light = maxOf(foreground.luminance().toDouble(), background.luminance().toDouble())
        val dark = minOf(foreground.luminance().toDouble(), background.luminance().toDouble())
        return (light + 0.05) / (dark + 0.05)
    }

    @Test fun dayAndNightForegroundsRemainReadable() {
        val day = ClocktowerDayColorScheme
        val night = ClocktowerHostColorScheme
        assertEquals(Color.White, day.background)
        assertTrue("Night canvas should be near black", night.background.luminance() < 0.015f)
        listOf(day, night).forEach { scheme ->
            assertTrue(contrast(scheme.onBackground, scheme.background) >= 4.5)
            assertTrue(contrast(scheme.onSurface, scheme.surface) >= 4.5)
            assertTrue(contrast(scheme.onSurfaceVariant, scheme.surfaceVariant) >= 4.5)
            assertTrue(contrast(scheme.primary, scheme.surface) >= 4.5)
            assertTrue(contrast(scheme.error, scheme.surface) >= 4.5)
        }
    }

    @Test fun goodAndEvilCardsAreDistinctAndReadableOnBothPhases() {
        val good = clocktowerAlignmentSeatColor(Alignment.GOOD)
        val evil = clocktowerAlignmentSeatColor(Alignment.EVIL)
        assertTrue(good != evil)
        for (background in listOf(good, evil)) {
            assertTrue("Normal white text needs 4.5:1 contrast", contrast(Color.White, background) >= 4.5)
        }
    }

    @Test fun playerTrueAlignmentIsProjectedWithoutInferringFromShownIdentity() {
        val game = GameState(
            script = ScriptId("trouble_brewing"),
            seed = 1L,
            players = listOf(
                PlayerState(1, "Drunk", RoleId("Drunk"), Alignment.GOOD, CharacterType.OUTSIDER, shownRole = RoleId("Empath")),
                PlayerState(2, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, shownRole = RoleId("Washerwoman")),
            ),
        )
        val before = game.toHostSeatPresentations()
        assertEquals(Alignment.GOOD, before[0].actualAlignment)
        assertEquals(Alignment.EVIL, before[1].actualAlignment)
        val changed = game.copy(players = game.players.map {
            if (it.seat == 1) it.copy(actualAlignment = Alignment.EVIL) else it
        }).toHostSeatPresentations()
        assertEquals(Alignment.EVIL, changed[0].actualAlignment)
    }
}
