package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft

/** Confirmed actual source activity and original duration; no separate mutable effects store. */
internal fun ClocktowerSessionState.bmrDrunkennessEffects(): List<ActionFact.BmrDrunkenness> {
    val latest = linkedMapOf<String, ActionFact.BmrDrunkenness>()
    actionTimeline.entries.forEach { entry -> when (val fact = entry.fact) {
        is ActionFact.BmrDrunkenness -> latest[fact.effectId] = fact
        is ActionFact.Revival -> latest.entries.removeAll { it.value.sourceSeat == fact.targetSeat }
        is ActionFact.RoleChange -> latest.entries.removeAll { it.value.sourceSeat == fact.targetSeat }
        else -> Unit
    } }
    return latest.values.filter { effect -> effect.throughDayRound >= round &&
        gameState.playerAt(effect.sourceSeat)?.let { it.alive && it.actualRole == effect.sourceRole } == true }
}

internal fun ClocktowerSessionState.withBmrDrunkenness(): ClocktowerSessionState {
    if (gameState.script != BadMoonRisingOpening.script) return this
    val seats = bmrDrunkennessEffects().filter { it.active }.flatMapTo(mutableSetOf()) { it.targetSeats }
    return copy(gameState = gameState.copy(players = gameState.players.map { it.copy(drunk = it.seat in seats) }))
}

internal fun ClocktowerGameSession.startBmrDrunkenness(sourceActionId: String, sourceSeat: Int, targetSeats: List<Int>) {
    val source = requireNotNull(state.gameState.playerAt(sourceSeat))
    require(source.alive && source.actualRole.value in setOf("Sailor", "Innkeeper", "Courtier", "Minstrel", "Goon"))
    require(targetSeats.distinct().size == targetSeats.size && targetSeats.all { state.gameState.playerAt(it) != null })
    val throughDay = state.round + when (source.actualRole.value) { "Courtier" -> 2; "Minstrel" -> 1; else -> 0 }
    commitConfirmedGameAction(ActionFactDraft.BmrDrunkenness("$sourceActionId:drunk", state.phase, state.round,
        nextInputSequence, sourceActionId, sourceSeat, source.actualRole, targetSeats.toList(), throughDay, true),
        state.gameState, state.gameStateRevision)
}

/** Manual BMR complex cross-source suspension/resumption; never silently solve circular drunkenness. */
internal fun ClocktowerGameSession.confirmBmrDrunkActivity(effectId: String, active: Boolean, expectedRevision: Long) {
    check(state.gameState.script == BadMoonRisingOpening.script && state.gameStateRevision == expectedRevision)
    val effect = state.bmrDrunkennessEffects().single { it.effectId == effectId }
    if (effect.active == active) return
    commitConfirmedGameAction(ActionFactDraft.BmrDrunkenness("$effectId:activity:$expectedRevision", state.phase,
        state.round, nextInputSequence, effectId, effect.sourceSeat, effect.sourceRole, effect.targetSeats,
        effect.throughDayRound, active), state.gameState, expectedRevision)
}

/** A visible manual review hint, never an automatic circular-effect solver or a blocking cursor. */
internal fun ClocktowerSessionState.bmrDrunkennessReview(): List<ActionFact.BmrDrunkenness> = bmrDrunkennessEffects().filter { effect ->
    effect.sourceSeat !in effect.targetSeats && gameState.playerAt(effect.sourceSeat)?.let { source ->
        effect.active && source.impaired || !effect.active && !source.impaired
    } == true
}
