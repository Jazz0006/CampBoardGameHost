package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft

/**
 * Test fixture for a real Host-accepted action: the same atomic Session
 * boundary production uses. No separate history-only writer.
 */
internal fun ClocktowerGameSession.commitAcceptedTestAction(draft: ActionFactDraft) =
    commitConfirmedGameAction(
        draft = draft,
        nextGameState = state.gameState,
        expectedGameStateRevision = state.gameStateRevision,
    )
