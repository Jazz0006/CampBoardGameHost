package com.codex.campboardgamehost

internal data class ClocktowerPlayerRevealHandoff(
    val openReveal: Boolean,
    val recordPublication: Boolean,
)

/**
 * Keeps the player-facing reveal lifecycle separate from the exactly-once
 * first-night publication lifecycle.
 */
internal fun resolveClocktowerPlayerRevealHandoff(
    publicationAllowed: Boolean,
    firstNightPublicationCreated: Boolean,
): ClocktowerPlayerRevealHandoff = when {
    !publicationAllowed -> ClocktowerPlayerRevealHandoff(
        openReveal = false,
        recordPublication = false,
    )
    firstNightPublicationCreated -> ClocktowerPlayerRevealHandoff(
        openReveal = true,
        recordPublication = true,
    )
    else -> ClocktowerPlayerRevealHandoff(
        openReveal = true,
        recordPublication = false,
    )
}
