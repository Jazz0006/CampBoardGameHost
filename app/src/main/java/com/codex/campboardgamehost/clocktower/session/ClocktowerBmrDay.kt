package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*

/** A Moonchild still publicly chooses while impaired; the chosen alignment is fixed TODAY. */
internal fun ClocktowerSessionState.pendingBmrMoonchild(): TimelineBoundActionFact? {
    if (gameState.script != BadMoonRisingOpening.script || phase != StorytellerPhase.DAY || outcome != null) return null
    val moonchild = gameState.players.singleOrNull { it.actualRole.value == "Moonchild" && !it.alive } ?: return null
    val chosenDeaths = actionTimeline.entries.mapNotNull { (it.fact as? ActionFact.MoonchildChoice)?.deathActionId }.toSet()
    return actionTimeline.entries.lastOrNull { e -> e.point.globalSequence > bmrLifeStart(moonchild.seat) &&
        e.fact.actionId !in chosenDeaths && when(val f = e.fact) {
            is ActionFact.Death -> f.targetSeat == moonchild.seat
            is ActionFact.Execution -> f.causedDeath && f.targetSeat == moonchild.seat
            else -> false
        }
    }
}

internal fun ClocktowerGameSession.confirmBmrMoonchildChoice(deathId: String, targetSeat: Int, expectedRevision: Long) {
    val old = state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.MoonchildChoice }.firstOrNull { it.deathActionId == deathId }
    if (old != null) { check(old.targetSeat == targetSeat); return }
    check(state.gameStateRevision == expectedRevision)
    val death = requireNotNull(state.pendingBmrMoonchild())
    check(death.fact.actionId == deathId)
    val source = state.gameState.players.single { it.actualRole.value == "Moonchild" }
    val target = requireNotNull(state.gameState.playerAt(targetSeat))
    require(target.publiclyAlive)
    commitConfirmedGameAction(ActionFactDraft.MoonchildChoice("moonchild:$deathId", state.phase, state.round,
        nextInputSequence, source.seat, target.seat, target.actualAlignment, deathId), state.gameState, state.gameStateRevision)
}

internal fun ClocktowerGameSession.confirmBmrGossip(claimant: Int, statement: String, truthful: Boolean, expectedRevision: Long) {
    check(state.gameState.script == BadMoonRisingOpening.script && state.phase == StorytellerPhase.DAY && state.outcome == null && !state.dayResolved())
    val id = "gossip:${state.gameId}:${state.round}:$claimant"
    val prior = state.actionTimeline.entries.firstOrNull { it.fact.actionId == id }?.fact
    if (prior != null) { check(prior is ActionFact.GossipStatement && prior.statement == statement.trim() && prior.truthful == truthful); return }
    check(state.gameStateRevision == expectedRevision)
    require(state.gameState.playerAt(claimant)?.publiclyAlive == true && statement.isNotBlank())
    commitConfirmedGameAction(ActionFactDraft.GossipStatement(id, state.phase, state.round, nextInputSequence,
        claimant, statement.trim(), truthful), state.gameState, state.gameStateRevision)
}

/** Explicit execution outcome; the resolved Day remains visible for Moonchild and end-game review. */
internal fun ClocktowerGameSession.confirmBmrEndDay(dies: Boolean, expectedRevision: Long, pacifistSaves: Boolean = false) = confirmAtomically {
    check(state.gameState.script == BadMoonRisingOpening.script && state.phase == StorytellerPhase.DAY && state.outcome == null)
    if (state.dayResolved()) return@confirmAtomically
    check(state.gameStateRevision == expectedRevision && state.pendingVote() == null && state.pendingBmrMoonchild() == null)
    val targetSeat = state.voteStanding.nomineeSeat
    if (targetSeat == null) {
        require(!dies)
        commitConfirmedGameAction(ActionFactDraft.NoExecution("end-day-${state.gameId}-${state.round}", state.phase,
            state.round, nextInputSequence), state.gameState, state.gameStateRevision)
        return@confirmAtomically
    }
    val target = requireNotNull(state.gameState.playerAt(targetSeat))
    val id = "day-execution-${state.gameId}-${state.round}"
    val pacifist = state.gameState.players.singleOrNull { it.alive && !it.impaired && it.actualRole.value == "Pacifist" }
    if (pacifistSaves) require(!dies && target.actualAlignment == Alignment.GOOD && pacifist != null)
    var actualDeath = dies && target.alive
    var registersDead = false
    var foolSaves = false
    if (!target.alive) require(!dies)
    if (state.bmrCertainlyProtected(targetSeat, execution = true)) { require(!dies); actualDeath = false }
    if (state.bmrFoolCanSave(targetSeat) && !state.bmrCertainlyProtected(targetSeat, execution = true) && !pacifistSaves) {
        require(!dies); foolSaves = true; actualDeath = false
    }
    if (dies && !target.impaired && target.actualRole.value == "Zombuul" && !target.registersAsDead) {
        registersDead = true; actualDeath = false
    }
    val minstrel = state.gameState.players.singleOrNull { it.alive && !it.impaired && it.actualRole.value == "Minstrel" }
    val mastermind = state.gameState.players.singleOrNull { it.alive && !it.impaired && it.actualRole.value == "Mastermind" }
    val execution = ActionFactDraft.Execution(id, state.phase, state.round, nextInputSequence, targetSeat, causedDeath = actualDeath)
    if (actualDeath) commitConfirmedPlayerDeath(execution, state.gameStateRevision)
    else commitConfirmedGameAction(execution, state.gameState, state.gameStateRevision)
    if (pacifistSaves) commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("$id:pacifist", state.phase, state.round,
        nextInputSequence, requireNotNull(pacifist).seat, pacifist.actualRole, listOf(targetSeat), null, true, "$id:pacifist", false, true), state.gameState, state.gameStateRevision)
    if (registersDead) confirmDeathRegistration("$id:registration", targetSeat)
    if (foolSaves) commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("$id:fool", state.phase, state.round,
        nextInputSequence, targetSeat, target.actualRole, listOf(targetSeat), null, true, "$id:fool", false, true), state.gameState, state.gameStateRevision)
    if (actualDeath && target.actualType == CharacterType.MINION && minstrel != null) {
        commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("$id:minstrel", state.phase, state.round,
            nextInputSequence, minstrel.seat, minstrel.actualRole, listOf(targetSeat), null, true, "$id:minstrel", false, true), state.gameState, state.gameStateRevision)
        startBmrDrunkenness("$id:minstrel", minstrel.seat, state.gameState.players.filter { it.seat != minstrel.seat }.map { it.seat })
    }
    if (actualDeath && target.actualType == CharacterType.DEMON && mastermind != null) {
        commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("$id:mastermind", state.phase, state.round,
            nextInputSequence, mastermind.seat, mastermind.actualRole, listOf(targetSeat), null, true, "$id:mastermind", false, true), state.gameState, state.gameStateRevision)
    }
}

internal fun ClocktowerSessionState.bmrMastermindExtraDay(): Int? = actionTimeline.entries.lastOrNull {
    (it.fact as? ActionFact.BmrAbilityUse)?.let { f -> f.role.value == "Mastermind" && f.tookEffect && f.used } == true
}?.point?.round?.plus(1)

internal fun ClocktowerGameSession.continueBmrAfterDay(expectedRevision: Long) {
    check(state.gameState.script == BadMoonRisingOpening.script && state.phase == StorytellerPhase.DAY && state.outcome == null &&
        state.gameStateRevision == expectedRevision && state.dayResolved() && state.pendingBmrMoonchild() == null)
    commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day-to-night-${state.gameId}-${state.round}", state.phase,
        state.round, nextInputSequence, StorytellerPhase.NIGHT, state.round + 1), state.gameStateRevision)
}

internal fun ClocktowerGameSession.confirmBmrGameEnd(goodWins: Boolean, expectedRevision: Long) {
    check(state.gameState.script == BadMoonRisingOpening.script)
    val reason = if (goodWins) GameEndReason.BMR_GOOD else GameEndReason.BMR_EVIL
    if (state.outcome != null) { check(state.outcome?.reason == reason); return }
    check(state.gameStateRevision == expectedRevision)
    commitConfirmedGameAction(ActionFactDraft.GameEnded("game-ended-${state.gameId}", state.phase, state.round,
        nextInputSequence, reason), state.gameState, state.gameStateRevision)
}

internal fun ClocktowerGameSession.confirmBmrTinkerDeath(expectedRevision: Long) = confirmAtomically {
    check(state.gameState.script == BadMoonRisingOpening.script && state.gameStateRevision == expectedRevision && state.outcome == null)
    val tinker = state.gameState.players.single { it.alive && !it.impaired && it.actualRole.value == "Tinker" }
    resolveBmrDeath("tinker:${state.gameId}:${state.round}:${state.phase}:${state.bmrLifeStart(tinker.seat)}", tinker.seat, true)
}
