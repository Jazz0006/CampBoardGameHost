package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

internal fun ClocktowerPhase.toStorytellerPhase(): StorytellerPhase = when (this) {
    ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
    ClocktowerPhase.Day -> StorytellerPhase.DAY
    ClocktowerPhase.Night -> StorytellerPhase.NIGHT
}

internal fun StorytellerPhase.toClocktowerPhase(): ClocktowerPhase = when (this) {
    StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
    StorytellerPhase.DAY -> ClocktowerPhase.Day
    StorytellerPhase.NIGHT -> ClocktowerPhase.Night
}
