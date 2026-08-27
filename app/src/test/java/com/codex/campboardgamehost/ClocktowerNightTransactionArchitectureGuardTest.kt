package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SNE-7.8 minimal architecture guard.
 *
 * Gameplay semantics are proved by typed reducer/planner/reconstruction/integration tests. These
 * checks protect only coarse ownership boundaries that are still impractical to invoke directly
 * through the Compose/App surface from JVM tests.
 */
class ClocktowerNightTransactionArchitectureGuardTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)
    private val reducerSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/session/NightCheckpointReducer.kt",
    ).readText(Charsets.UTF_8)
    private val plannerSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/session/NightDawnResolutionPlanner.kt",
    ).readText(Charsets.UTF_8)
    private val hostTransactionSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/session/NightCheckpointHostTransaction.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production night checkpoint transitions route through typed owners`() {
        listOf(
            "NightResolutionEvent.EditPoisonDraft",
            "NightResolutionEvent.ConfirmPoison",
            "NightResolutionEvent.EditMonkProtectionDraft",
            "NightResolutionEvent.ConfirmMonkProtection",
            "NightResolutionEvent.EditDemonAttackDraft",
            "NightResolutionEvent.ConfirmDemonAttack",
            "NightResolutionEvent.EditMayorRedirectDraft",
            "NightResolutionEvent.ConfirmMayorRedirect",
        ).forEach { command ->
            assertTrue("App must continue routing $command through the typed checkpoint seam.", appSource.contains(command))
        }
        assertTrue(appSource.contains("NightCheckpointReducer.reduce("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.editDemonSuccessor("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.confirmDemonSuccessor("))
        assertTrue(appSource.contains("NightCheckpointHostTransaction.movePrevious("))
        assertTrue(hostSource.contains("onMovePreviousNightStep: () -> Unit"))
        assertFalse(
            "Host navigation must not bypass the checkpoint transition owner.",
            hostSource.contains("nightStepIndex = currentStepIndex - 1"),
        )
    }

    @Test
    fun `Dawn planner remains pure while App retains durable commit authority`() {
        val pureTransitionSources = reducerSource + plannerSource + hostTransactionSource
        listOf(
            "recordClocktowerAction(",
            "ActionFactDraft.",
            "setClocktowerActualRole(",
            "eliminatedRound = round",
        ).forEach { durableMutationShape ->
            assertFalse(
                "Pure checkpoint/planner seams must not absorb App durable commit authority: $durableMutationShape",
                pureTransitionSources.contains(durableMutationShape),
            )
        }

        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.Poison("))
        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.Protect("))
        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.Attack("))
        assertTrue(appSource.contains("cards[index] = nightDeathCard.copy(eliminatedRound = round)"))
        assertTrue(appSource.contains("setClocktowerActualRole("))
    }

    @Test
    fun `App consumes canonical Dawn planner and checkpoint projection`() {
        assertTrue(appSource.contains("NightDawnResolutionPlanner.planValidatedNightDeath("))
        assertTrue(appSource.contains("NightDawnResolutionPlanner.confirmNewDemonIdentity("))
        assertTrue(appSource.contains("currentClocktowerNightCheckpoint()"))
        assertFalse(
            "App must not restore a parallel Mayor redirect legality authority during Dawn planning.",
            appSource.contains("MayorRedirectLegality.canReceiveRedirect("),
        )
    }
}
