package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

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

/** A displayed response belongs to the same Host revision that produced its current question. */
internal fun clocktowerInformationPublicationAllowed(
    sourceRevision: StorytellerDecisionRevision?,
    currentRevision: StorytellerDecisionRevision,
): Boolean = sourceRevision == null || sourceRevision == currentRevision

/**
 * Authoritative observation (and its confirmed registration rulings) must commit BEFORE
 * the transient first-night displayed marker. If the durable write fails, the marker
 * remains unconsumed so a retry can safely use Session's idempotent observation identity.
 */
internal fun performClocktowerPlayerRevealHandoff(
    authorize: () -> Boolean,
    publicationNeeded: () -> Boolean,
    recordPrivateInformation: () -> Unit,
    markFirstNightPublished: () -> Unit,
    openReveal: () -> Unit,
): ClocktowerPlayerRevealHandoff {
    if (!authorize()) return ClocktowerPlayerRevealHandoff(false, false)
    val handoff = resolveClocktowerPlayerRevealHandoff(
        publicationAllowed = true,
        firstNightPublicationCreated = publicationNeeded(),
    )
    if (handoff.recordPublication) {
        recordPrivateInformation()
        markFirstNightPublished()
    }
    openReveal()
    return handoff
}
