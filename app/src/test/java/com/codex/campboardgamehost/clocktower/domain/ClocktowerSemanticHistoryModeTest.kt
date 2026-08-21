package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSemanticHistoryModeTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = game.script,
        scriptContentHash = "0123456789abcdef0123456789abcdef",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.VERIFIED,
    )

    @Test
    fun `snapshot defaults to legacy local semantic history`() {
        assertEquals(ClocktowerSemanticHistoryMode.LEGACY_LOCAL, snapshot().semanticHistoryMode)
    }

    @Test
    fun `legacy mode rejects a nonempty global observation history`() {
        assertFails {
            snapshot(
                mode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
                log = EpistemicObservationLog(listOf(globalRecord(globalSequence = 4L))),
                cursor = 5L,
            )
        }
    }

    @Test
    fun `global mode rejects a nonempty legacy local observation history`() {
        assertFails {
            snapshot(
                mode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                log = EpistemicObservationLog(listOf(legacyRecord())),
                cursor = 1L,
            )
        }
    }

    @Test
    fun `global mode requires cursor strictly beyond every committed global observation`() {
        assertFails {
            snapshot(
                mode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                log = EpistemicObservationLog(listOf(globalRecord(globalSequence = 7L))),
                cursor = 7L,
            )
        }

        val valid = snapshot(
            mode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            log = EpistemicObservationLog(listOf(globalRecord(globalSequence = 7L))),
            cursor = 8L,
        )
        assertEquals(8L, valid.nextTimelineGlobalSequence)
    }

    private fun snapshot(
        mode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        log: EpistemicObservationLog = EpistemicObservationLog(),
        cursor: Long = 0L,
    ): GameSnapshot = GameSnapshot(
        gameId = "semantic-history-test",
        gameStateRevision = 0L,
        playerInputRevision = 0L,
        gameSeed = game.seed,
        rulesetRef = rulesetRef,
        gameState = game,
        epistemicObservationLog = log,
        semanticHistoryMode = mode,
        nextTimelineGlobalSequence = cursor,
    )

    private fun legacyRecord(): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = "legacy-record",
        phase = StorytellerPhase.DAWN,
        round = 1,
        sequence = 0,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, false),
    )

    private fun globalRecord(globalSequence: Long): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = "global-record-$globalSequence",
        phase = StorytellerPhase.DAWN,
        round = 1,
        sequence = 0,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(1, false),
        timelineBinding = ObservationTimelineBinding.Global(
            TimelinePoint(
                phase = StorytellerPhase.DAWN,
                round = 1,
                sequence = 0,
                globalSequence = globalSequence,
            ),
        ),
    )

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected semantic-history validation to fail closed.", failed)
    }
}
