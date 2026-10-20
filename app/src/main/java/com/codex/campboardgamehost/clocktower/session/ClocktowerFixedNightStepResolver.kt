package com.codex.campboardgamehost.clocktower.session

/**
 * Session-fact-backed selection of the next not-yet-performed mechanical night action.
 *
 * This is deliberately narrow: it may skip only the four fixed actions which have
 * an accepted, phase/round-scoped Session fact (or an actor who is no longer alive).
 * Other night interactions, including private information, are NOT treated as
 * completed until their own Session publication contract is migrated.
 *
 * [cursor] is a presentation hint, not a source of committed facts.
 */
internal data class ClocktowerFixedNightStepStatus(
    val kind: NightOperationKind?,
    val actorAlive: Boolean = true,
    val realAction: Boolean = true,
)

/** Null means no still-renderable step remains at or after the presentation hint. */
internal fun clocktowerNextUnperformedFixedNightStepIndex(
    cursor: Int,
    steps: List<ClocktowerFixedNightStepStatus>,
    acceptedFactsInCurrentNight: Set<NightOperationKind>,
): Int? {
    require(cursor >= 0) { "Night presentation cursor cannot be negative." }
    if (steps.isEmpty()) return null
    for (index in cursor.coerceAtMost(steps.size) until steps.size) {
        val step = steps[index]
        if (step.kind != null && (
                step.kind in acceptedFactsInCurrentNight ||
                !step.actorAlive ||
                !step.realAction
            )
        ) continue
        return index
    }
    return null
}
