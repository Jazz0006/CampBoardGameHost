package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.*
import org.junit.Assert.*
import org.junit.Test

class ClocktowerGameRecordsTest {
    @Test fun restoredRecordsKeepInterleavedOrderAndActualPrivateAnswerWithoutWritingState() {
        val initial = TroubleBrewingFixtures.eightPlayerExample()
        val session = ClocktowerGameSession.createProduction("records", initial.seed, initial, ClocktowerSemanticHistoryMode.GLOBAL_V1)
        session.commitRedHerringSeat(2, StorytellerPhase.FIRST_NIGHT, 1, 0)
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            "shown-answer", StorytellerPhase.FIRST_NIGHT, 1, 1, 1, RoleId("Fortune Teller"),
            ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.KNOWN_MALFUNCTIONING,
            InformationProposition.BooleanResult(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 3), false)))
        session.commitRedHerringSeat(3, StorytellerPhase.FIRST_NIGHT, 1, 2)
        val saved = ClocktowerSessionJson.encode(session.state)
        val restored = ClocktowerSessionJson.decode(saved)
        val rows = gameRecordRows(restored, "en")
        assertEquals(listOf(1L, 2L), rows.map { it.order })
        assertEquals(rows, gameRecordRows(session.state, "en"))
        assertEquals(2, rows.map { it.id }.distinct().size)
        val answer = rows[0]
        assertTrue(answer.summary.startsWith("Fortune Teller information"))
        assertTrue(answer.summary.contains("Fortune Teller"))
        assertTrue(answer.summary.endsWith(", No"))
        assertEquals(setOf(1, 2, 3), answer.seats)
        assertTrue(answer.details.contains("malfunctioning"))
        assertEquals(listOf(answer), rows.filter { it.category == GameRecordCategory.INFORMATION })
        gameRecordRows(restored, "zh")
        assertEquals(saved, ClocktowerSessionJson.encode(restored))
        assertEquals(saved, ClocktowerSessionJson.encode(session.state))
    }
}
