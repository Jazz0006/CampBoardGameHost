package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

/** Current effective night choices. Drafts and navigation never enter this value. */
internal data class ClocktowerNightTargets(
    val attackSeat: Int? = null,
    val monkSeat: Int? = null,
    val mayorRedirectSeat: Int? = null,
) {
    fun withAttack(seat: Int?): ClocktowerNightTargets =
        invalidateDependents(attackSeat != seat).copy(attackSeat = seat)

    fun withProtection(seat: Int?): ClocktowerNightTargets =
        invalidateDependents(monkSeat != seat).copy(monkSeat = seat)

    private fun invalidateDependents(changed: Boolean): ClocktowerNightTargets =
        if (changed) copy(mayorRedirectSeat = null) else this

    fun after(
        entry: TimelineBoundActionFact,
        priorPoisonSeat: Int?,
        preserveNightChoices: Boolean,
    ): ClocktowerNightTargets =
        when (val fact = entry.fact) {
            is ActionFact.Attack -> withAttack(fact.targetSeat)
            is ActionFact.Protect -> withProtection(fact.targetSeat)
            is ActionFact.Poison ->
                invalidateDependents(!preserveNightChoices && priorPoisonSeat != fact.targetSeat)
            is ActionFact.MayorRedirectChoice -> copy(mayorRedirectSeat = fact.targetSeat)
            is ActionFact.PhaseAdvance -> if (fact.phase == StorytellerPhase.NIGHT)
                ClocktowerNightTargets() else this
            else -> this
        }
}
