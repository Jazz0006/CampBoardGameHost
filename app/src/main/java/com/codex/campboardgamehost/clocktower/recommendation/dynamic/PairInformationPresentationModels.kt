package com.codex.campboardgamehost.clocktower.recommendation.dynamic

/**
 * Registration semantics carried by pair-information presentation candidates.
 *
 * This is descriptive/legal presentation state only; it carries no ranking or recommendation
 * preference.
 */
internal enum class PairInformationRegistration {
    NONE,
    SPY_AS_GOOD_ROLE,
    RECLUSE_AS_EVIL_ROLE,
}
