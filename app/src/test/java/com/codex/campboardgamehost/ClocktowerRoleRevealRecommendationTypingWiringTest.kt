package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRoleRevealRecommendationTypingWiringTest {
    @Test
    fun `role reveal misinformation keeps typed target seat for Undertaker and Ravenkeeper`() {
        val source = hostScreenSource()

        assertTrue(source.contains("propositionForRole: ((ClocktowerRole) -> InformationProposition)? = null"))
        assertTrue(source.contains("proposition = propositionForRole?.invoke(role)"))
        assertTrue(source.contains("propositionForRole = undertakerTarget?.let { target ->"))
        assertTrue(source.contains("propositionForRole = ravenkeeperTargetCard?.let { target ->"))
        assertTrue(source.contains("InformationProposition.RoleAt(cards.indexOf(target) + 1, RoleId(role.enName))"))
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
