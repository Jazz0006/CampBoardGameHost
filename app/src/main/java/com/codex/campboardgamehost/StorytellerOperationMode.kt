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
