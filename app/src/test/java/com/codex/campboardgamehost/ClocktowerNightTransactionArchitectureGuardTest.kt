package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse architecture guard for the remaining non-callable App/Host night-transaction boundary.
 *
 * Gameplay, invalidation, planner, reconstruction, and replay semantics belong to typed tests.
 * These checks protect only production ownership and the pure-versus-durable boundary.
 */
class ClocktowerNightTransactionArchitectureGuardTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val pureTransitionSources = listOf(
        "src/main/java/com/codex/campboardgamehost/clocktower/session/NightCheckpointReducer.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlanner.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/session/NightCheckpointHostTransaction.kt",
    ).joinToString(separator = "\n") { path -> File(path).readText(Charsets.UTF_8) }

    @Test
    fun `production checkpoint transitions remain owned by typed transaction seams`() {
        assertTrue(appSource.contains("NightCheckpointReducer.reduce("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.confirmPoison("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.confirmDemonAttack("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.confirmMonkProtection("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.confirmDemonSuccessor("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.movePrevious("))
        assertTrue(hostSource.contains("onMovePreviousNightStep: () -> Unit"))
        assertFalse(hostSource.contains("nightStepIndex = currentStepIndex - 1"))
    }

    @Test
    fun `pure night transition owners do not absorb durable App mutations`() {
        listOf(
            "recordClocktowerAction(",
            "ActionFactDraft.",
            "setClocktowerActualRole(",
            "eliminatedRound = round",
        ).forEach { durableMutationShape ->
            assertFalse(
                "Pure night seams must not absorb durable App authority: $durableMutationShape",
                pureTransitionSources.contains(durableMutationShape),
            )
        }
    }

    @Test
    fun `App Dawn consumes canonical Trouble Brewing facts and planner authority`() {
        assertTrue(appSource.contains("resolveTroubleBrewingDawnDeathFacts("))
        assertTrue(appSource.contains("NightDawnResolutionPlanner.planValidatedNightDeath("))
        assertTrue(appSource.contains("NightDawnResolutionPlanner.confirmNewDemonIdentity("))
        assertTrue(appSource.contains("currentClocktowerNightCheckpoint()"))
    }

    @Test
    fun `Host death presentation consumes checkpoint backed canonical resolution`() {
        assertTrue(appSource.contains("nightCheckpoint = currentClocktowerNightCheckpoint()"))
        assertTrue(hostSource.contains("nightCheckpoint: ClocktowerNightCheckpoint"))
        assertTrue(hostSource.contains("resolveTroubleBrewingDawnDeathResolution("))
        assertTrue(hostSource.contains("checkpoint = nightCheckpoint"))
    }
}
