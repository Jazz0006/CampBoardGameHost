package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse App ownership guard only. Demon succession gameplay, repeated succession and restore
 * semantics are proved by typed rules/session tests.
 */
class ClocktowerDemonSuccessionProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production Dawn consumes canonical succession resolver and planner`() {
        assertTrue(appSource.contains("resolveTroubleBrewingImpSelfKillSuccession("))
        assertTrue(appSource.contains("NightDawnResolutionPlanner.planDemonSuccession("))
        assertTrue(appSource.contains("NightDawnResolutionPlanner.confirmNewDemonIdentity("))
        assertFalse(
            "Production must not restore a confirmed-choice-only succession authority.",
            appSource.contains("newDemonName = clocktowerConfirmedDemonSuccessorTarget"),
        )
    }
}
