package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

internal fun ClocktowerPhase.toStorytellerPhase(): StorytellerPhase = when (this) {
    ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
    ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
    ClocktowerPhase.Day -> StorytellerPhase.DAY
    ClocktowerPhase.Night -> StorytellerPhase.NIGHT
}
