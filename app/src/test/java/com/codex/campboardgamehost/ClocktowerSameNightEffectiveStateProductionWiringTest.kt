package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production ownership guards for same-night effective mechanical state.
 *
 * Gameplay semantics are proved by typed behavior tests. These source checks exist only because the
 * Compose Host boundary is not directly callable from JVM tests.
 */
class ClocktowerSameNightEffectiveStateProductionWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `normal night actor eligibility consumes effective ability subject authority`() {
        val roleActor = hostSource
            .substringAfter("fun roleActor(enName: String): PlayerCard? {")
            .substringBefore("fun roleMissingReason(enName: String)")

        assertTrue(
            "Production normal-actor eligibility must consume the canonical effective-subject seam.",
            roleActor.contains("effectiveAbilitySubjectForRole(enName, candidate)"),
        )
    }

    @Test
    fun `production consumes poison source lifetime authority`() {
        assertTrue(
            "Production must consume PoisonEffectLifecycle instead of treating a confirmed poison choice as perpetual authority.",
            hostSource.contains("PoisonEffectLifecycle."),
        )
    }
}
