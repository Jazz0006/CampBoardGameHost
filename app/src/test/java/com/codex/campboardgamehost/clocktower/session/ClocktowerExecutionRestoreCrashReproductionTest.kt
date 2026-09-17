package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerExecutionRestoreCrashReproductionTest {
    @Test
    fun `restored lagging event counter reproduces execution preflight crash`() {
        val gameId = "restore-crash-repro"
        val targetSeat = 5
        val restoredEventCounter = 7
        val proposedSequence = restoredEventCounter + 1
        val proposedObservationId = "public-alive-$gameId-$proposedSequence-$targetSeat"
        val draft = publicAliveDraft(
            recordId = proposedObservationId,
            sequence = proposedSequence,
            targetSeat = targetSeat,
        )

        // Model the persisted half-state suspected from the device incident: the public-alive
        // observation for sequence 8 already exists, while the restored event counter is still 7.
        val persisted = ClocktowerGameSession.commitGlobalEpistemicObservation(
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            observationLog = EpistemicObservationLog(),
            nextTimelineGlobalSequence = 0L,
            playerInputRevision = 19L,
            draft = draft,
        )
        val restoredPlayerInputRevision = persisted.playerInputRevision

        assertEquals(8, persisted.record.sequence)
        assertEquals(proposedObservationId, persisted.record.recordId)
        assertEquals(20L, restoredPlayerInputRevision)

        // Confirm Execution regenerates eventCounter + 1, therefore regenerating exactly the
        // already-persisted public-alive ID. Session semantics correctly treat that as idempotent.
        val repeatedPreflight = ClocktowerGameSession.commitGlobalEpistemicObservation(
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            observationLog = persisted.observationLog,
            nextTimelineGlobalSequence = persisted.nextTimelineGlobalSequence,
            playerInputRevision = restoredPlayerInputRevision,
            draft = draft,
        )

        assertSame(persisted.record, repeatedPreflight.record)
        assertEquals(restoredPlayerInputRevision, repeatedPreflight.playerInputRevision)
        assertEquals(persisted.nextTimelineGlobalSequence, repeatedPreflight.nextTimelineGlobalSequence)

        // Mirror the production preflight guard. Because the duplicate is idempotent, this is the
        // exact IllegalStateException path that currently terminates the process.
        val thrown = try {
            check(repeatedPreflight.playerInputRevision != restoredPlayerInputRevision) {
                "A new public elimination cannot reuse an existing observation ID."
            }
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertNotNull("Expected the restored duplicate preflight to reproduce the crash.", thrown)
        assertEquals(
            "A new public elimination cannot reuse an existing observation ID.",
            thrown?.message,
        )
    }

    private fun publicAliveDraft(
        recordId: String,
        sequence: Int,
        targetSeat: Int,
    ): EpistemicObservationDraft = EpistemicObservationDraft(
        recordId = recordId,
        phase = StorytellerPhase.DAY,
        round = 1,
        sequence = sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(targetSeat, false),
    )
}
