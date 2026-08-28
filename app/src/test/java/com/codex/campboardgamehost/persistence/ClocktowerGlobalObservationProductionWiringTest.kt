package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerGlobalObservationProductionWiringTest {
    private val appSource = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `new Clocktower games start explicit global v1 without changing restored legacy mode`() {
        val reset = appSource
            .substringAfter("fun resetDealState(")
            .substringBefore("fun startUndercoverGame()")

        assertTrue(reset.contains("ClocktowerSemanticHistoryMode.GLOBAL_V1"))
        assertTrue(reset.contains("clocktowerNextTimelineGlobalSequence = 0L"))

        val restore = appSource
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")
        assertTrue(restore.contains("clocktowerSemanticHistoryMode = restoredSemanticHistoryMode"))
        assertFalse(
            restore.substringAfter("clocktowerSemanticHistoryMode = restoredSemanticHistoryMode")
                .substringBefore("screen = restoredScreen")
                .contains("ClocktowerSemanticHistoryMode.GLOBAL_V1"),
        )
    }

    @Test
    fun `app declares commit seam before event producer and writes back only history revision and cursor`() {
        val commitStart = appSource.indexOf("fun recordEpistemicObservation(")
        val eventStart = appSource.indexOf("fun addClocktowerEvent(")
        assertTrue(commitStart >= 0)
        assertTrue(eventStart > commitStart)

        val commit = appSource
            .substring(commitStart, eventStart)

        assertTrue(commit.contains("draft: EpistemicObservationDraft"))
        assertTrue(commit.contains("ClocktowerGameSession.commitGlobalEpistemicObservation("))
        assertTrue(commit.contains("semanticHistoryMode = clocktowerSemanticHistoryMode"))
        assertTrue(commit.contains("observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList())"))
        assertTrue(commit.contains("nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence"))
        assertTrue(commit.contains("playerInputRevision = clocktowerPlayerInputRevision"))
        assertTrue(commit.contains("draft = draft"))
        assertTrue(commit.contains("clocktowerPlayerInputRevision = committed.playerInputRevision"))
        assertTrue(commit.contains("clocktowerNextTimelineGlobalSequence = committed.nextTimelineGlobalSequence"))
        assertTrue(commit.contains("clocktowerEpistemicObservations"))
    }

    @Test
    fun `global exact duplicate returns before observable or durability side effects`() {
        val commit = appSource
            .substringAfter("fun recordEpistemicObservation(")
            .substringBefore("fun addClocktowerEvent(")
        val globalBranch = commit.substringAfter("ClocktowerSemanticHistoryMode.GLOBAL_V1 ->")

        val guard = "if (committed.playerInputRevision == clocktowerPlayerInputRevision) return"
        val guardIndex = globalBranch.indexOf(guard)
        val listMutationIndex = globalBranch.indexOf("clocktowerEpistemicObservations.clear()")
        val durabilityIndex = globalBranch.indexOf("a4ObservationDurabilityGate.markPending(committed.record.recordId)")

        assertTrue(guardIndex >= 0)
        assertTrue(listMutationIndex > guardIndex)
        assertTrue(durabilityIndex > guardIndex)
    }

    @Test
    fun `successful global commit invalidates superseded A4 revision before durability publication`() {
        val commit = appSource
            .substringAfter("fun recordEpistemicObservation(")
            .substringBefore("fun addClocktowerEvent(")
        val globalBranch = commit.substringAfter("ClocktowerSemanticHistoryMode.GLOBAL_V1 ->")

        val guardIndex = globalBranch.indexOf(
            "if (committed.playerInputRevision == clocktowerPlayerInputRevision) return",
        )
        val revisionWriteIndex = globalBranch.indexOf(
            "clocktowerPlayerInputRevision = committed.playerInputRevision",
        )
        val invalidationIndex = globalBranch.indexOf("invalidateA4RevisionScope()")
        val durabilityIndex = globalBranch.indexOf(
            "a4ObservationDurabilityGate.markPending(committed.record.recordId)",
        )

        assertTrue(guardIndex >= 0)
        assertTrue(revisionWriteIndex > guardIndex)
        assertTrue(invalidationIndex > revisionWriteIndex)
        assertTrue(durabilityIndex > invalidationIndex)
    }

    @Test
    fun `restored legacy local games keep recording without entering global commit authority`() {
        val commitStart = appSource.indexOf("fun recordEpistemicObservation(")
        val eventStart = appSource.indexOf("fun addClocktowerEvent(")
        assertTrue(commitStart >= 0)
        assertTrue(eventStart > commitStart)

        val commit = appSource.substring(commitStart, eventStart)
        assertTrue(commit.contains("when (clocktowerSemanticHistoryMode)"))
        assertTrue(commit.contains("ClocktowerSemanticHistoryMode.LEGACY_LOCAL ->"))
        assertTrue(commit.contains("ClocktowerSemanticHistoryMode.GLOBAL_V1 ->"))

        val legacyBranch = commit
            .substringAfter("ClocktowerSemanticHistoryMode.LEGACY_LOCAL ->")
            .substringBefore("ClocktowerSemanticHistoryMode.GLOBAL_V1 ->")
        assertTrue(legacyBranch.contains("clocktowerEpistemicObservations.any { it.recordId == draft.recordId }"))
        assertTrue(legacyBranch.contains("draft.bindLegacyLocal()"))
        assertTrue(legacyBranch.contains("advanceClocktowerPlayerInputRevision()"))
        assertTrue(legacyBranch.contains("a4ObservationDurabilityGate.markPending(draft.recordId)"))
        assertFalse(legacyBranch.contains("commitGlobalEpistemicObservation"))
        assertFalse(legacyBranch.contains("clocktowerNextTimelineGlobalSequence"))
    }

    @Test
    fun `public alive observations use same draft commit authority`() {
        val eventFunction = appSource
            .substringAfter("fun addClocktowerEvent(")
            .substringBefore("fun localizedText(")

        assertTrue(eventFunction.contains("EpistemicObservationDraft("))
        assertTrue(eventFunction.contains("recordEpistemicObservation("))
        assertFalse(eventFunction.contains("clocktowerEpistemicObservations += RecordedEpistemicObservation("))
        assertFalse(eventFunction.contains("advanceClocktowerPlayerInputRevision()"))
    }

    @Test
    fun `public eliminations preflight global observation before caller state publication`() {
        val preflightStart = appSource.indexOf("fun preflightClocktowerPublicAliveObservation(")
        val eventStart = appSource.indexOf("fun addClocktowerEvent(")
        assertTrue(preflightStart >= 0)
        assertTrue(eventStart > preflightStart)

        val preflight = appSource.substring(preflightStart, eventStart)
        assertTrue(preflight.contains("ClocktowerGameSession.commitGlobalEpistemicObservation("))
        assertFalse(preflight.contains("clocktowerEpistemicObservations.clear()"))
        assertFalse(preflight.contains("clocktowerPlayerInputRevision ="))
        assertFalse(preflight.contains("clocktowerNextTimelineGlobalSequence ="))

        val virginPreflight = appSource
            .substringAfter("onPreflightVirginExecution =")
            .substringBefore("onVirginNomination =")
        assertTrue(virginPreflight.contains("preflightClocktowerPublicAliveObservation("))

        val virginMutation = appSource
            .substringAfter("onVirginNomination =")
            .substringBefore("onAdvanceFromFirstNight =")
        assertTrue(virginMutation.contains("clocktowerVirginUsed = true"))
        assertFalse(virginMutation.contains("preflightClocktowerPublicAliveObservation("))

        val day = appSource
            .substringAfter("onConfirmDay =")
            .substringBefore("onConfirmNight =")
        val dayPreflightIndex = day.indexOf("preflightClocktowerPublicAliveObservation(")
        val dayMutationIndex = day.indexOf("advanceClocktowerGameStateRevision()")
        assertTrue(dayPreflightIndex >= 0)
        assertTrue(dayMutationIndex > dayPreflightIndex)

        val night = appSource
            .substringAfter("onConfirmNight =")
            .substringBefore("onShowResults =")
        val nightPreflightIndex = night.indexOf("preflightClocktowerPublicAliveObservation(")
        val nightMutationIndex = night.indexOf("cards[index] = nightDeathCard.copy(eliminatedRound = round)")
        assertTrue(nightPreflightIndex >= 0)
        assertTrue(nightMutationIndex > nightPreflightIndex)
    }

    @Test
    fun `host creates unbound private drafts and never assigns global identity`() {
        val privateProducer = hostSource
            .substringAfter("fun recordReliablePrivateInformation(")
            .substringBefore("val undertakerTarget =")

        assertTrue(privateProducer.contains("onRecordEpistemicObservation(EpistemicObservationDraft("))
        assertFalse(privateProducer.contains("RecordedEpistemicObservation("))
        assertFalse(privateProducer.contains("ObservationTimelineBinding.Global"))
        assertFalse(privateProducer.contains("globalSequence"))
    }

    @Test
    fun `host callback accepts an unbound draft rather than a durable record`() {
        val signature = hostSource
            .substringAfter("internal fun ClocktowerJudgeScreen(")
            .substringBefore(") {")

        assertTrue(signature.contains("onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit"))
        assertFalse(signature.contains("onRecordEpistemicObservation: (RecordedEpistemicObservation) -> Unit"))
    }
}
