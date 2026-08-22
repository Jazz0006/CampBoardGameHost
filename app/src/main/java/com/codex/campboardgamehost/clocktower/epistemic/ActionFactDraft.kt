package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * Identity-free mechanical action proposed by production code before durable commit.
 *
 * A draft deliberately has no global timeline identity. [ClocktowerGameSession] is the only
 * authority allowed to bind one to the shared action/observation timeline.
 */
sealed interface ActionFactDraft {
    val actionId: String
    val phase: StorytellerPhase
    val round: Int
    val sequence: Int

    data class Poison(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val targetSeat: Int?,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            targetSeat?.let(::requireSeat)
        }
    }

    data class Protect(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val targetSeat: Int,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            requireSeat(targetSeat)
        }
    }

    data class Attack(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val targetSeat: Int,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            requireSeat(targetSeat)
        }
    }

    data class Execution(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val targetSeat: Int,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            requireSeat(targetSeat)
        }
    }

    data class Death(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val targetSeat: Int,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            requireSeat(targetSeat)
        }
    }

    data class RoleChange(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val targetSeat: Int,
        val role: RoleId,
        val alignment: Alignment,
        val type: CharacterType,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            requireSeat(targetSeat)
        }
    }

    data class PhaseAdvance(
        override val actionId: String,
        override val phase: StorytellerPhase,
        override val round: Int,
        override val sequence: Int,
        val nextPhase: StorytellerPhase,
        val nextRound: Int,
    ) : ActionFactDraft {
        init {
            requireIdentity(actionId, round, sequence)
            require(nextRound > 0) { "Action target round must be positive." }
        }
    }

    companion object {
        private fun requireIdentity(actionId: String, round: Int, sequence: Int) {
            require(actionId.isNotBlank()) { "Action draft ID cannot be blank." }
            require(round > 0) { "Action draft round must be positive." }
            require(sequence >= 0) { "Action draft local sequence cannot be negative." }
        }

        private fun requireSeat(seat: Int) {
            require(seat > 0) { "Action draft target seat must be positive." }
        }
    }
}

internal fun ActionFactDraft.bindGlobal(point: TimelinePoint): TimelineBoundActionFact {
    require(point.phase == phase && point.round == round && point.sequence == sequence) {
        "Action draft can only bind to a TimelinePoint with matching local chronology."
    }
    return TimelineBoundActionFact(
        fact = toActionFact(point.globalSequence),
        point = point,
    )
}

internal fun ActionFactDraft.matches(entry: TimelineBoundActionFact): Boolean =
    entry.point.phase == phase &&
        entry.point.round == round &&
        entry.point.sequence == sequence &&
        entry.fact == toActionFact(entry.point.globalSequence)

private fun ActionFactDraft.toActionFact(globalSequence: Long): ActionFact = when (this) {
    is ActionFactDraft.Poison -> ActionFact.Poison(actionId, globalSequence, targetSeat)
    is ActionFactDraft.Protect -> ActionFact.Protect(actionId, globalSequence, targetSeat)
    is ActionFactDraft.Attack -> ActionFact.Attack(actionId, globalSequence, targetSeat)
    is ActionFactDraft.Execution -> ActionFact.Execution(actionId, globalSequence, targetSeat)
    is ActionFactDraft.Death -> ActionFact.Death(actionId, globalSequence, targetSeat)
    is ActionFactDraft.RoleChange -> ActionFact.RoleChange(
        actionId = actionId,
        sequence = globalSequence,
        targetSeat = targetSeat,
        role = role,
        alignment = alignment,
        type = type,
    )
    is ActionFactDraft.PhaseAdvance -> ActionFact.PhaseAdvance(
        actionId = actionId,
        sequence = globalSequence,
        phase = nextPhase,
        round = nextRound,
    )
}
