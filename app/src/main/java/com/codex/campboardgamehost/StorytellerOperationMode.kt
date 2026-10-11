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

/** The saved default applies to each new game; unsupported scripts remain purely manual. */
internal fun storytellerModeForNewGame(
    preferredMode: StorytellerOperationMode,
    script: ClocktowerScript,
): StorytellerOperationMode =
    if (script == ClocktowerScript.TroubleBrewing) preferredMode else StorytellerOperationMode.MANUAL

/**
 * A new AI game must not start with missing or not-yet-restored credentials. Both
 * initial setup and same-player quick restart use this single permission gate.
 */
internal fun storytellerNewGameConfigurationReady(
    mode: StorytellerOperationMode,
    directEnabled: Boolean,
    directLoaded: Boolean,
    directModel: String,
    directKey: String,
    gatewayLoaded: Boolean,
    gatewayEndpoint: String,
    gatewayToken: String,
): Boolean = when {
    mode == StorytellerOperationMode.MANUAL -> true
    directEnabled -> directLoaded && directModel.isNotBlank() && directKey.isNotBlank()
    else -> gatewayLoaded && gatewayEndpoint.startsWith("https://") && gatewayToken.isNotBlank()
}

/**
 * A response must never gain fresh commit authority when the operator switches modes
 * during a network request. MANUAL does not accept any AI response.
 */
internal fun storytellerAiResponseMayApply(
    requestedMode: StorytellerOperationMode,
    currentMode: StorytellerOperationMode,
): Boolean = requestedMode != StorytellerOperationMode.MANUAL && requestedMode == currentMode

