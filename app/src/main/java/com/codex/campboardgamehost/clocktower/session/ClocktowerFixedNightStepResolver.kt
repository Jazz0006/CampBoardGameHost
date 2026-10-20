package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline

/**
 * Session-fact-backed selection of the next not-yet-performed mechanical night action.
 *
 * This is deliberately narrow: it may skip only the four fixed actions which have
 * an accepted, phase/round-scoped Session fact (or an actor who is no longer alive).
 * Other night interactions, including private information, are NOT treated as
 * completed until their own Session publication contract is migrated.
 *
 * The presentation cursor only locates not-yet-migrated information steps; it must NEVER
 * skip an earlier still-uncommitted mechanical action. Completed actions come solely
 * from the Session timeline for the exact night, not a Compose checkpoint.
 */
internal data class ClocktowerFixedNightStepStatus(
    val kind: NightOperationKind?,
    val actorAlive: Boolean = true,
    val realAction: Boolean = true,
)

/** Read accepted fixed actions from the one Session timeline, scoped to this exact night. */
internal fun clocktowerAcceptedFixedNightKinds(
    timeline: ActionFactTimeline,
    phase: StorytellerPhase,
    round: Int,
): Set<NightOperationKind> = timeline.entries.asSequence()
    .filter { it.point.phase == phase && it.point.round == round }
    .mapNotNull { entry ->
        when (entry.fact) {
            is ActionFact.Poison -> NightOperationKind.POISON
            is ActionFact.Protect -> NightOperationKind.PROTECT
            is ActionFact.Attack -> NightOperationKind.ATTACK
            is ActionFact.ButlerMasterChoice -> NightOperationKind.BUTLER
            else -> null
        }
    }.toSet()

/**
 * Null means no still-renderable step remains. Existing UI cursor is ONLY a
 * temporary presentation hint for non-migrated information roles; if it has
 * moved past an unconfirmed mechanical action, rewind to that mandatory action.
 * Never infer an information step completed just because a later fact exists.
 */
internal fun clocktowerNextUnperformedFixedNightStepIndex(
    cursor: Int,
    steps: List<ClocktowerFixedNightStepStatus>,
    acceptedFactsInCurrentNight: Set<NightOperationKind>,
): Int? {
    require(cursor >= 0) { "Night presentation cursor cannot be negative." }
    if (steps.isEmpty()) return null
    fun unperformedMechanical(step: ClocktowerFixedNightStepStatus): Boolean =
        step.kind != null && step.actorAlive && step.realAction &&
            step.kind !in acceptedFactsInCurrentNight
    // A stale/out-of-bounds UI cursor cannot authorize skipping a player's
    // mandatory mechanic. Session facts or confirmed death are the only skips.
    for (index in 0 until cursor.coerceAtMost(steps.size)) {
        if (unperformedMechanical(steps[index])) return index
    }
    for (index in cursor.coerceAtMost(steps.size) until steps.size) {
        val step = steps[index]
        if (step.kind != null && !unperformedMechanical(step)) continue
        return index
    }
    return null
}
