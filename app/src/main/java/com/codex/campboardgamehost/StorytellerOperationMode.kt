package com.codex.campboardgamehost

/**
 * Opt-in model operation, independent of the Storyteller's experience level.
 * AI_AUTOMATIC is currently bounded to the covered setup decision; all later
 * unsupported rulings must pause/manual-handoff, never run retired heuristics.
 */
internal enum class StorytellerOperationMode {
    MANUAL,
    AI_ASSISTED,
    AI_AUTOMATIC,
}

/**
 * A response must never gain fresh commit authority when the operator switches modes
 * during a network request. MANUAL does not accept any AI response.
 */
internal fun storytellerAiResponseMayApply(
    requestedMode: StorytellerOperationMode,
    currentMode: StorytellerOperationMode,
): Boolean = requestedMode != StorytellerOperationMode.MANUAL && requestedMode == currentMode

