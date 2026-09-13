package com.codex.campboardgamehost

/**
 * Dawn remains a semantic/history boundary, but it is no longer a standalone host-facing screen.
 * Once Dawn settlement is complete, the visible host surface continues directly into Day.
 */
internal fun clocktowerVisibleHostPhase(phase: ClocktowerPhase): ClocktowerPhase =
    if (phase == ClocktowerPhase.Dawn) ClocktowerPhase.Day else phase
