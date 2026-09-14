package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerPhaseStorytellerAdapterTest {
    @Test
    fun `every app phase maps exhaustively to its storyteller phase`() {
        assertEquals(
            mapOf(
                ClocktowerPhase.FirstNight to StorytellerPhase.FIRST_NIGHT,
                ClocktowerPhase.Dawn to StorytellerPhase.DAWN,
                ClocktowerPhase.Day to StorytellerPhase.DAY,
                ClocktowerPhase.Night to StorytellerPhase.NIGHT,
            ),
            ClocktowerPhase.entries.associateWith(ClocktowerPhase::toStorytellerPhase),
        )
    }
}
