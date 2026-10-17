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
 * Authoritative observation (and its confirmed registration rulings) must commit BEFORE
 * the transient first-night displayed marker. If the durable write fails, the marker
 * remains unconsumed so a retry can safely use Session's idempotent observation identity.
 * Narrative history is display-only; no second game fact is inferred from it.
 */
internal fun performClocktowerPlayerRevealHandoff(
    authorize: () -> Boolean,
    publicationNeeded: () -> Boolean,
    recordPrivateInformation: () -> Unit,
    markFirstNightPublished: () -> Unit,
    recordHistory: () -> Unit,
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
        recordHistory()
    }
    openReveal()
    return handoff
}
