package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft

internal fun ClocktowerSessionState.bmrExorcised(seat: Int): Boolean = actionTimeline.entries.any { e ->
    e.point.phase == phase && e.point.round == round && (e.fact as? ActionFact.BmrAbilityUse)?.let { use ->
        use.role.value == "Exorcist" && use.tookEffect && use.used && use.targetSeats == listOf(seat)
    } == true
}

/** Exorcism prevents a real wake/choice, while Pukka poison and Shabaloth regurgitation stay separate. */
internal fun ClocktowerGameSession.confirmBmrExorcisedSkip(stepId: String, expectedRevision: Long, ruleset: ValidatedClocktowerRuleset) {
    if (state.bmrStepUse(stepId) != null) return
    check(state.gameStateRevision == expectedRevision)
    val step = requireNotNull(state.nextBmrNightInteraction(ruleset))
    val actor = requireNotNull(state.gameState.playerAt(requireNotNull(step.actorSeat)))
    check(step.id.value == stepId && actor.actualType == CharacterType.DEMON && state.bmrExorcised(actor.seat))
    commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("bmr:${state.gameId}:${state.round}:$stepId", state.phase,
        state.round, nextInputSequence, actor.seat, actor.actualRole, emptyList(), null, false, stepId, false, false), state.gameState, state.gameStateRevision)
}

/** Passive death effects are actual resolutions, never fake player wakes. */
internal fun ClocktowerGameSession.confirmBmrNightEvent(stepId: String, targetSeat: Int?, dies: Boolean,
    expectedRevision: Long, ruleset: ValidatedClocktowerRuleset) = confirmAtomically {
    val old = state.bmrStepUse(stepId)
    if (old != null) { check(old.targetSeats == listOfNotNull(targetSeat)); return@confirmAtomically }
    check(state.gameStateRevision == expectedRevision)
    val step = requireNotNull(state.nextBmrNightInteraction(ruleset))
    check(step.id.value == stepId)
    val actor = requireNotNull(state.gameState.playerAt(requireNotNull(step.actorSeat)))
    val role = requireNotNull(step.roleId).value
    val works = when(role) {
        "Gossip" -> {
            require(actor.alive)
            val statement = state.actionTimeline.entries.firstOrNull { it.point.phase == StorytellerPhase.DAY && it.point.round == state.round - 1 &&
                (it.fact as? ActionFact.GossipStatement)?.claimantSeat == actor.seat }?.fact as? ActionFact.GossipStatement
            requireNotNull(statement)
            if (!actor.impaired && statement.truthful) require(targetSeat != null && state.gameState.playerAt(targetSeat)?.publiclyAlive == true)
            else require(targetSeat == null)
            statement.truthful && !actor.impaired
        }
        "Moonchild" -> {
            val choice = state.actionTimeline.entries.lastOrNull { it.point.phase == StorytellerPhase.DAY && it.point.round == state.round - 1 &&
                (it.fact as? ActionFact.MoonchildChoice)?.actorSeat == actor.seat }?.fact as? ActionFact.MoonchildChoice
            requireNotNull(choice)
            require(choice.targetSeat == targetSeat)
            choice.chosenAlignment == Alignment.GOOD && !actor.impaired
        }
        else -> error("Unsupported passive BMR resolution: $role")
    }
    require(works || !dies)
    val id = "bmr:${state.gameId}:${state.round}:$stepId"
    commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse(id, state.phase, state.round, nextInputSequence,
        actor.seat, actor.actualRole, listOfNotNull(targetSeat), null, true, stepId, false, works), state.gameState, state.gameStateRevision)
    if (works) resolveBmrDeath("$id:death", requireNotNull(targetSeat), dies)
}

internal fun ClocktowerGameSession.completeBmrNight(expectedRevision: Long, ruleset: ValidatedClocktowerRuleset) {
    check(state.gameState.script == BadMoonRisingOpening.script && state.phase != StorytellerPhase.DAY && state.outcome == null)
    check(state.gameStateRevision == expectedRevision && state.nextBmrNightInteraction(ruleset) == null)
    commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night-to-day:${state.gameId}:${state.round}", state.phase,
        state.round, nextInputSequence, StorytellerPhase.DAY, state.round), state.gameStateRevision)
}
