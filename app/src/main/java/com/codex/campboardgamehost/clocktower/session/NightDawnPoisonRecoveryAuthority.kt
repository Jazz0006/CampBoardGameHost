package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline

/**
 * Recovers the last durably confirmed poison outcome for one night.
 *
 * The mutable checkpoint target may already reflect a partially materialized Dawn clear. The
 * ordered action timeline retains the preceding confirmed Poison fact and therefore remains the
 * fallback authority needed to reconstruct the same Dawn transition identity after restore.
 */
internal object NightDawnPoisonRecoveryAuthority {
    fun latestTargetSeatForRound(
        actionTimeline: ActionFactTimeline,
        round: Int,
    ): Int? {
        require(round > 0) { "Poison recovery round must be positive." }
        return actionTimeline.entries
            .lastOrNull { entry ->
                entry.point.phase == StorytellerPhase.NIGHT &&
                    entry.point.round == round &&
                    entry.fact is ActionFact.Poison
            }
            ?.fact
            ?.let { fact -> (fact as ActionFact.Poison).targetSeat }
    }
}
