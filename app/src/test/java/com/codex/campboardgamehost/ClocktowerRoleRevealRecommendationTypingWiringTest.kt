package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRoleRevealRecommendationTypingWiringTest {
    @Test
    fun `role reveal misinformation carries presentation metadata without creating a player proposition`() {
        val source = hostScreenSource()

        assertTrue(source.contains("roleRevealTargetSeat: Int? = null"))
        assertTrue(source.contains("roleRevealPresentation = roleRevealTargetSeat?.let { targetSeat ->"))
        assertTrue(source.contains("ClocktowerRoleRevealPresentation(targetSeat, RoleId(role.enName))"))
        assertTrue(source.contains("roleRevealTargetSeat = undertakerTarget?.let { cards.indexOf(it) + 1 }"))
        assertTrue(source.contains("roleRevealTargetSeat = ravenkeeperTargetCard?.let { cards.indexOf(it) + 1 }"))
    }

    private fun hostScreenSource(): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("ClocktowerHostScreen.kt source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
