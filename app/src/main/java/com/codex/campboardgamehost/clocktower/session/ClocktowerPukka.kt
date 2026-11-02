package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact

/** Applied native BMR poison in the current Pukka's life. No second mutable effect state. */
internal fun ClocktowerSessionState.pukkaPoisons(): List<TimelineBoundActionFact> {
    val source = gameState.players.singleOrNull { it.alive && it.actualRole.value == "Pukka" } ?: return emptyList()
    val active = mutableListOf<TimelineBoundActionFact>()
    actionTimeline.entries.forEach { entry -> when (val fact = entry.fact) {
        is ActionFact.PukkaChoice -> if (fact.actorSeat == source.seat && fact.poisonApplied) active.add(entry)
        is ActionFact.PukkaPoisonEnded -> active.removeAll { it.fact.actionId == fact.choiceActionId }
        is ActionFact.Revival -> if (fact.targetSeat == source.seat) active.clear()
        is ActionFact.RoleChange -> if (fact.targetSeat == source.seat) active.clear()
        else -> Unit
    } }
    return active
}

/** Source death/role exit clears its poison; self-poison persists until the source leaves. */
internal fun ClocktowerSessionState.withPukkaPoisonState(): ClocktowerSessionState {
    if (gameState.script != BadMoonRisingOpening.script || actionTimeline.entries.none { it.fact is ActionFact.PukkaChoice }) return this
    val source = gameState.players.singleOrNull { it.alive && it.actualRole.value == "Pukka" }
    val poisoned = pukkaPoisons().map { it.fact as ActionFact.PukkaChoice }
        .filter { source?.drunk != true || it.targetSeat == source.seat }.mapTo(mutableSetOf()) { it.targetSeat }
    return copy(gameState = gameState.copy(players = gameState.players.map { it.copy(poisoned = it.seat in poisoned) }))
}

/** Only the actual Pukka enters here. A Lunatic's simulated choice has no mechanical poison. */
internal fun ClocktowerGameSession.confirmPukkaChoice(targetSeat: Int, expectedRevision: Long) = confirmAtomically {
    check(state.gameState.script == BadMoonRisingOpening.script && state.phase != StorytellerPhase.DAY)
    val actor = state.gameState.players.single { it.actualRole.value == "Pukka" }
    val life = state.actionTimeline.entries.lastOrNull { (it.fact as? ActionFact.Revival)?.targetSeat == actor.seat }?.fact?.actionId.orEmpty()
    val id = "pukka-choice:${state.gameId}:${state.phase}:${state.round}:${actor.seat}:$life"
    val previous = state.actionTimeline.entries.firstOrNull { it.fact.actionId == id }?.fact
    if (previous != null) {
        check(previous is ActionFact.PukkaChoice && previous.targetSeat == targetSeat)
        return@confirmAtomically
    }
    check(state.gameStateRevision == expectedRevision) { "Stale Pukka choice." }
    require(actor.alive && !state.bmrExorcised(actor.seat))
    requireNotNull(state.gameState.playerAt(targetSeat))
    val priorChoices = state.pukkaPoisons()
    triggerBmrGoon(actor.seat, targetSeat, id)
    val applied = !requireNotNull(state.gameState.playerAt(actor.seat)).impaired
    commitConfirmedGameAction(ActionFactDraft.PukkaChoice(id, state.phase, state.round, nextInputSequence,
        actor.seat, targetSeat, applied), state.gameState, state.gameStateRevision)
    // Owner ruling: self-poison stops the old victim's death and permanently disables later picks.
    if (applied && targetSeat == actor.seat) priorChoices.forEach { choice ->
        endPukkaPoison("$id:end:${choice.fact.actionId}", choice.fact.actionId)
    }
}

/** Delayed settlement is independent of whether an Exorcist permitted a new choice tonight. */
internal fun ClocktowerSessionState.pukkaPoisonsDue(): List<TimelineBoundActionFact> =
    if (phase != StorytellerPhase.NIGHT || gameState.players.none { it.alive && it.actualRole.value == "Pukka" && !it.impaired }) emptyList()
    else pukkaPoisons().filter { it.point.round < round }

/** The old application ends; a new application to the SAME player remains in force. */
internal fun ClocktowerGameSession.confirmPukkaPoisonResolution(choiceActionId: String, dies: Boolean, expectedRevision: Long, grandmotherDies: Boolean? = null) = confirmAtomically {
    val id = "pukka-resolved:$choiceActionId"
    if (state.actionTimeline.entries.any { it.fact.actionId == id }) {
        check(state.actionTimeline.entries.any { it.fact.actionId == "$id:death" } == dies)
        return@confirmAtomically
    }
    check(state.gameStateRevision == expectedRevision) { "Stale Pukka settlement." }
    val choice = state.pukkaPoisonsDue().single { it.fact.actionId == choiceActionId }.fact as ActionFact.PukkaChoice
    val target = requireNotNull(state.gameState.playerAt(choice.targetSeat))
    // The manual Storyteller confirms death/protection. Preserve poison through death, then expire only the old application.
    require(!dies || target.alive) { "A dead target cannot die again." }
    resolveBmrDeath("$id:death", target.seat, dies, sourceIsDemon = true, grandmotherDies = grandmotherDies)
    endPukkaPoison(id, choiceActionId)
}

private fun ClocktowerGameSession.endPukkaPoison(id: String, choiceActionId: String) {
    commitConfirmedGameAction(ActionFactDraft.PukkaPoisonEnded(id, state.phase, state.round, nextInputSequence, choiceActionId),
        state.gameState, state.gameStateRevision)
}
