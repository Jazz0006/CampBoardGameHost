package com.codex.campboardgamehost

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerPoisonSourceCurrentRoleWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `Poisoner persistent effect ends immediately when source loses Poisoner role`() {
        val poisonHelper = hostSource
            .substringAfter("fun effectivePoisonTargetAt(")
            .substringBefore("fun deathTriggerAbilityState(")

        assertTrue(
            "The poison source must still own the Poisoner character at the querying cursor; " +
                "a same-night RoleChanged away from Poisoner ends the old Poisoner effect immediately.",
            poisonHelper.contains(
                "effectiveState.currentRoleId(sourceSeat) == RoleId(\"Poisoner\")",
            ),
        )
        assertTrue(
            "Current-role ownership is an additional lifetime condition, not a replacement for " +
                "the existing alive/poisoned ability-functioning semantics.",
            poisonHelper.contains("AbilityFunctioningSemantics.functionsAs(") &&
                poisonHelper.contains("effectiveState.isMechanicallyAlive(sourceSeat)"),
        )
        assertTrue(
            "The resulting sourceFunctioning value must remain the authority passed into the " +
                "existing PoisonEffectLifecycle.",
            poisonHelper.contains(
                "PoisonEffectLifecycle.effectiveTarget(poisonTarget, true, sourceFunctioning)",
            ),
        )
    }
}
