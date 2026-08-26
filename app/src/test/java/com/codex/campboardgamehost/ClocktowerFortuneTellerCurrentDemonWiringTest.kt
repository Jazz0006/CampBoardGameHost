package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerFortuneTellerCurrentDemonWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Fortune Teller detects Demon from effective current role while preserving special registrations`() {
        val fortuneTellerBlock = hostSource
            .substringAfter("val fortuneTellerMatched =")
            .substringBefore("val fortuneTellerResult =")

        assertTrue(
            "Fortune Teller Demon truth must read the effective state at the Fortune Teller interaction cursor.",
            fortuneTellerBlock.contains("effectiveNightStateAt(") &&
                fortuneTellerBlock.contains("ClocktowerInteractionBoundary.BEFORE") &&
                fortuneTellerBlock.contains("currentRoleId("),
        )
        assertTrue(
            "The effective current role must be classified through role metadata as a Demon.",
            fortuneTellerBlock.contains("clocktowerRoleDefinitionsForScript(script)") &&
                fortuneTellerBlock.contains("CharacterType.DEMON"),
        )
        assertFalse(
            "Fortune Teller must not use the persisted PlayerCard team as Demon truth after same-night role changes.",
            fortuneTellerBlock.contains("clocktowerTeam == ClocktowerTeam.Demon"),
        )
        assertTrue(
            "Red herring and Recluse registration remain independent Fortune Teller Yes paths.",
            fortuneTellerBlock.contains("redHerring") &&
                fortuneTellerBlock.contains("recluseRegistersEvil"),
        )
    }
}
