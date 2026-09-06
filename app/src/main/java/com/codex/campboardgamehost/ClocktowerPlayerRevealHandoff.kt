package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot

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

/** Legacy displays have no structured confirmation; confirmed displays must match exactly. */
internal fun clocktowerInformationPublicationAllowed(
    confirmation: ConfirmedInformationDecision?,
    expectedSnapshot: InformationDecisionSnapshot?,
    currentRevision: InformationDecisionRevision,
): Boolean {
    if (confirmation == null) return true
    if (expectedSnapshot == null) return false
    return confirmation.authorizes(expectedSnapshot, currentRevision)
}

/**
 * Orders the existing synchronous publication effects without taking ownership of their state.
 * Authorization is evaluated before publication; repeated publication may still reopen the reveal.
 * Exceptions propagate and stop subsequent effects. This is not a rollback-capable transaction.
 */
internal fun performClocktowerPlayerRevealHandoff(
    authorize: () -> Boolean,
    publishFirstNight: () -> Boolean,
    recordPrivateInformation: () -> Unit,
    recordHistory: () -> Unit,
    openReveal: () -> Unit,
): ClocktowerPlayerRevealHandoff {
    val allowed = authorize()
    val handoff = resolveClocktowerPlayerRevealHandoff(
        publicationAllowed = allowed,
        firstNightPublicationCreated = allowed && publishFirstNight(),
    )
    if (!handoff.openReveal) return handoff
    if (handoff.recordPublication) {
        recordPrivateInformation()
        recordHistory()
    }
    openReveal()
    return handoff
}
