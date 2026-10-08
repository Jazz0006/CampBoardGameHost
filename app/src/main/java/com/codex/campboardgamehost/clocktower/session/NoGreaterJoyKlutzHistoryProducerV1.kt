package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.rules.NoGreaterJoyKlutzChoiceRuleV1

/**
 * NGJ-only Host mechanical action planner. The user-selected seat is NEVER a Storyteller
 * ruling. An unrecorded legacy death remains UNKNOWN: never manufacture a learn event.
 *
 * The legal NGJ roster contains no poisoning source. This explicit script-gate is the
 * learn-time functioning authority for current NGJ, NOT the pre-death poison snapshot.
 */
internal object NoGreaterJoyKlutzHistoryProducerV1 {
    private val ngj = ScriptId("no_greater_joy")

    fun learned(
        state: ClocktowerSessionState,
        klutzSeat: Int,
        actionId: String,
        round: Int,
        localSequence: Int,
    ): ActionFactDraft.KlutzLearnedDeath? {
        if (state.semanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1 ||
            state.gameState.script != ngj) return null
        val klutz = state.gameState.playerAt(klutzSeat) ?: return null
        if (klutz.actualRole != RoleId("Klutz") || klutz.alive) return null
        val actions = state.actionTimeline.reducerFacts()
        if (actions.any { it is ActionFact.KlutzLearnedDeath && it.klutzSeat == klutzSeat }) return null
        // Only a committed, provably real death is an acceptable predecessor.
        val death = actions.lastOrNull {
            (it is ActionFact.Death && it.targetSeat == klutzSeat) ||
                (it is ActionFact.Execution && it.targetSeat == klutzSeat)
        } ?: return null
        val proof = when (death) {
            is ActionFact.Death -> death.klutzDeathTrigger
            is ActionFact.Execution -> death.klutzDeathTrigger
            else -> null
        }
        if (proof?.actualRole != RoleId("Klutz") || proof.wasAlive != true) return null
        return ActionFactDraft.KlutzLearnedDeath(
            actionId, StorytellerPhase.DAY, round, localSequence,
            klutzSeat, death.actionId, functioningWhenLearned = true,
        )
    }

    fun choice(
        state: ClocktowerSessionState,
        klutzSeat: Int,
        chosenSeat: Int,
        actionId: String,
        round: Int,
        localSequence: Int,
    ): ActionFactDraft.KlutzChoice? {
        if (state.semanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1 ||
            state.gameState.script != ngj) return null
        val facts = state.actionTimeline.reducerFacts()
        if (facts.any { it is ActionFact.KlutzChoice && it.klutzSeat == klutzSeat }) return null
        val learned = facts.filterIsInstance<ActionFact.KlutzLearnedDeath>()
            .lastOrNull { it.klutzSeat == klutzSeat } ?: return null
        // The choice is only capturable after an actual recorded learned-death event.
        if (!learned.functioningWhenLearned) return null
        NoGreaterJoyKlutzChoiceRuleV1.resolve(
            state.gameState, klutzSeat, chosenSeat,
            com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript(
                ClocktowerScript.NoGreaterJoy,
            ),
        )
        return ActionFactDraft.KlutzChoice(
            actionId, StorytellerPhase.DAY, round, localSequence,
            klutzSeat, chosenSeat, learned.actionId,
        )
    }
}
