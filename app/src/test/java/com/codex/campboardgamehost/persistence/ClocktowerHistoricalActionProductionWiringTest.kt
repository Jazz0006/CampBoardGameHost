package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerHistoricalActionProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production owns one action timeline and routes both event types through the shared cursor`() {
        assertTrue(appSource.contains("var clocktowerActionTimeline by remember { mutableStateOf(ActionFactTimeline()) }"))

        val actionCommit = appSource
            .substringAfter("fun recordClocktowerAction(")
            .substringBefore("fun recordEpistemicObservation(")
        assertTrue(actionCommit.contains("draft: ActionFactDraft"))
        assertTrue(actionCommit.contains("ClocktowerGameSession.commitGlobalActionFact("))
        assertTrue(actionCommit.contains("actionTimeline = clocktowerActionTimeline"))
        assertTrue(actionCommit.contains("observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList())"))
        assertTrue(actionCommit.contains("nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence"))
        assertTrue(actionCommit.contains("clocktowerActionTimeline = committed.actionTimeline"))
        assertTrue(actionCommit.contains("clocktowerNextTimelineGlobalSequence = committed.nextTimelineGlobalSequence"))

        val observationCommit = appSource
            .substringAfter("fun recordEpistemicObservation(")
            .substringBefore("fun preflightClocktowerPublicAliveObservation(")
        assertTrue(observationCommit.contains("actionTimeline = clocktowerActionTimeline"))

        val publicPreflight = appSource
            .substringAfter("fun preflightClocktowerPublicAliveObservation(")
            .substringBefore("fun nextNightPublicAliveObservationPreflightOrNull(")
        assertTrue(publicPreflight.contains("actionTimeline = clocktowerActionTimeline"))
    }

    @Test
    fun `active game save restore and new game reset preserve action history without upgrading legacy history`() {
        val save = appSource
            .substringAfter("fun activeGameSnapshotJson()")
            .substringBefore("fun persistActiveGameStateIfNeeded()")
        assertTrue(save.contains("ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY"))
        assertTrue(save.contains("ClocktowerSemanticHistoryPersistence.encodeActionTimeline(clocktowerActionTimeline)"))

        val restore = appSource
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")
        assertTrue(restore.contains("ClocktowerSemanticHistoryPersistence.decodeActionTimeline(json)"))
        assertTrue(restore.contains("actionTimeline = restoredClocktowerActionTimeline"))
        assertTrue(restore.contains("clocktowerActionTimeline = restoredClocktowerActionTimeline"))
        assertFalse(restore.contains("ActionFactTimeline(restoredClocktowerEvents"))

        val reset = appSource
            .substringAfter("fun resetDealState(")
            .substringBefore("fun startUndercoverGame()")
        assertTrue(reset.contains("clocktowerActionTimeline = ActionFactTimeline()"))
    }

    @Test
    fun `confirmed night selections emit action drafts rather than persisting provisional choices`() {
        val poison = appSource
            .substringAfter("onConfirmPoisonTarget =")
            .substringBefore("onSelectDemonAttackTarget =")
        assertTrue(poison.contains("recordClocktowerAction(ActionFactDraft.Poison("))
        assertFalse(poison.substringBefore("onConfirmPoisonTarget =").contains("ActionFactDraft.Poison("))

        val attack = appSource
            .substringAfter("onConfirmDemonAttack =")
            .substringBefore("onSelectMonkProtectedTarget =")
        assertTrue(attack.contains("recordClocktowerAction(ActionFactDraft.Attack("))

        val protect = appSource
            .substringAfter("onConfirmMonkProtectedTarget =")
            .substringBefore("onSelectMayorRedirectTarget =")
        assertTrue(protect.contains("recordClocktowerAction(ActionFactDraft.Protect("))
    }

    @Test
    fun `execution death role change and phase transitions become replayable semantic actions`() {
        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.Execution("))
        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.Death("))
        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.RoleChange("))
        assertTrue(appSource.contains("recordClocktowerAction(ActionFactDraft.PhaseAdvance("))

        val roleChange = appSource
            .substringAfter("fun setClocktowerActualRole(")
            .substringBefore("fun setClocktowerShownRole(")
        val recordIndex = roleChange.indexOf("ActionFactDraft.RoleChange(")
        val stateMutationIndex = roleChange.indexOf("cards[index] = cards[index].copy(")
        assertTrue(recordIndex >= 0)
        assertTrue(stateMutationIndex > recordIndex)
    }
}
