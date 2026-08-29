package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline

/**
 * Recovers the latest durably committed poison target for the outgoing round.
 *
 * Unlike Dawn-only recovery, this intentionally accepts First Night, ordinary Night, and a
 * previously committed Day/Dusk clear. Poisoner acts on First Night, so filtering to NIGHT would
 * lose the state-first retry authority for the first Day -> Night transition.
 */
internal object DuskPoisonExpiryRecoveryAuthority {
    fun latestTargetSeatForRound(
        actionTimeline: ActionFactTimeline,
        round: Int,
    ): Int? {
        require(round > 0) { "Dusk poison recovery round must be positive." }
        return actionTimeline.entries
            .lastOrNull { entry ->
                entry.point.round == round && entry.fact is ActionFact.Poison
            }
            ?.fact
            ?.let { fact -> (fact as ActionFact.Poison).targetSeat }
    }
}
