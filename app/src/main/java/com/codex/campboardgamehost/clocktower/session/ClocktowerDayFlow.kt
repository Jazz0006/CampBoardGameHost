package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*

internal val ClocktowerSessionState.outcome: ActionFact.GameEnded?
    get() = actionTimeline.entries.firstNotNullOfOrNull { it.fact as? ActionFact.GameEnded }

/** Night deaths become actionable only on the public Day table; there is no announcement fact. */
internal fun ClocktowerSessionState.pendingKlutzDeath(): ActionFact? {
    if (phase != StorytellerPhase.DAY || gameState.script.value != "no_greater_joy" || outcome != null) return null
    val completed = actionTimeline.entries.mapNotNull { (it.fact as? ActionFact.KlutzChoice)?.deathActionId }.toSet()
    return actionTimeline.entries.firstNotNullOfOrNull { entry ->
        val fact = entry.fact
        val seat = when (fact) {
            is ActionFact.Death -> fact.targetSeat
            is ActionFact.Execution -> fact.targetSeat
            else -> return@firstNotNullOfOrNull null
        }
        val evidence = when (fact) {
            is ActionFact.Death -> fact.klutzDeathTrigger
            is ActionFact.Execution -> fact.klutzDeathTrigger
            else -> null
        }
        fact.takeIf { fact.actionId !in completed && evidence?.wasAlive == true &&
            gameState.playerAt(seat)?.let { !it.alive && it.actualRole.value == "Klutz" } == true }
    }
}

internal fun ActionFact.klutzSeat(): Int = when (this) {
    is ActionFact.Death -> targetSeat
    is ActionFact.Execution -> targetSeat
    else -> error("Klutz requires an actual death")
}

internal fun ClocktowerSessionState.dayResolved(): Boolean = actionTimeline.entries.any {
    it.point.phase == StorytellerPhase.DAY && it.point.round == round &&
        (it.fact is ActionFact.Execution || it.fact is ActionFact.NoExecution)
}

/** A confirmed nomination remains the sole vote identity across recomposition and relaunch. */
internal fun ClocktowerSessionState.pendingVote(): ActionFact.Nomination? {
    if (phase != StorytellerPhase.DAY || dayResolved() || outcome != null) return null
    val today = actionTimeline.entries.filter { it.point.phase == phase && it.point.round == round }
    return today.asReversed().mapNotNull { it.fact as? ActionFact.Nomination }.firstOrNull { n ->
        today.none { (it.fact as? ActionFact.Vote)?.let { v -> v.nominatorSeat == n.nominatorSeat && v.nomineeSeat == n.nomineeSeat } == true }
    }
}

internal data class VirginNominationResult(val first: Boolean, val executes: Boolean, val needsSpyChoice: Boolean)

internal fun ClocktowerSessionState.virginNomination(nominatorSeat: Int, nomineeSeat: Int, spyChoice: Boolean?): VirginNominationResult {
    val actor = requireNotNull(gameState.playerAt(nominatorSeat))
    val virgin = requireNotNull(gameState.playerAt(nomineeSeat))
    val first = virgin.alive && virgin.actualRole.value == "Virgin" && actionTimeline.entries.none {
        (it.fact as? ActionFact.Nomination)?.firstVirginNomination == true
    }
    val needsChoice = first && !virgin.poisoned && actor.actualRole.value == "Spy" && !actor.poisoned
    return VirginNominationResult(first,
        first && !virgin.poisoned && (actor.actualType == CharacterType.TOWNSFOLK || (needsChoice && spyChoice == true)),
        needsChoice && spyChoice == null)
}

internal fun ClocktowerGameSession.finishIfDecided() {
    if (state.outcome != null || state.pendingKlutzDeath() != null) return
    val alive = state.gameState.players.filter { it.alive }
    val reason = when {
        alive.none { it.actualType == CharacterType.DEMON } -> GameEndReason.NO_DEMON
        alive.size <= 2 -> GameEndReason.TWO_ALIVE
        else -> return
    }
    endGame(reason)
}

private fun ClocktowerGameSession.endGame(reason: GameEndReason, subject: Int? = null) {
    if (state.outcome != null) return
    commitConfirmedGameAction(ActionFactDraft.GameEnded("game-ended-${state.gameId}", state.phase, state.round,
        nextInputSequence, reason, subject), state.gameState, state.gameStateRevision)
}

private fun ClocktowerGameSession.requireOpenDay() {
    check(state.phase == StorytellerPhase.DAY && state.outcome == null && !state.dayResolved() && state.pendingKlutzDeath() == null)
}

/** Fixed Scarlet Woman succession uses the roster immediately BEFORE the real death. */
private fun ClocktowerGameSession.killInDay(seat: Int, execution: Boolean, id: String) {
    val before = state.gameState
    val target = requireNotNull(before.playerAt(seat))
    // Executing an already dead nominee is still a real execution; it causes no second death.
    val successor = if (target.alive && target.actualType == CharacterType.DEMON && before.players.count { it.alive } >= 5)
        before.players.singleOrNull { it.alive && !it.poisoned && it.actualRole.value == "Scarlet Woman" } else null
    val draft = if (execution) ActionFactDraft.Execution(id, state.phase, state.round, nextInputSequence, seat)
        else ActionFactDraft.Death(id, state.phase, state.round, nextInputSequence, seat)
    if (target.alive) commitConfirmedPlayerDeath(draft, state.gameStateRevision)
    else commitConfirmedGameAction(draft, before, state.gameStateRevision)
    if (successor != null) commitConfirmedActualRoleChange(ActionFactDraft.RoleChange("$id:succession", state.phase,
        state.round, nextInputSequence, successor.seat, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON), state.gameStateRevision)
    if (execution && target.alive && !target.poisoned && target.actualRole.value == "Saint") endGame(GameEndReason.SAINT, seat)
}

/** A single table confirmation accepts the standing, special effects, victory and next phase. */
internal fun ClocktowerGameSession.confirmEndDay(expectedRound: Int) = confirmAtomically {
    if (state.actionTimeline.entries.any { it.fact.actionId == "end-day-${state.gameId}-$expectedRound" } ||
        state.actionTimeline.entries.any { it.fact.actionId == "day-execution-${state.gameId}-$expectedRound" }) return@confirmAtomically
    requireOpenDay()
    check(state.round == expectedRound && state.pendingVote() == null) { "Complete the current vote before ending Day." }
    val target = state.voteStanding.nomineeSeat
    if (target != null) killInDay(target, true, "day-execution-${state.gameId}-$expectedRound")
    else {
        val mayorWins = state.gameState.players.count { it.alive } == 3 && state.gameState.players.any {
            it.alive && !it.poisoned && it.actualRole.value == "Mayor"
        }
        commitConfirmedGameAction(ActionFactDraft.NoExecution("end-day-${state.gameId}-$expectedRound", state.phase,
            state.round, nextInputSequence), state.gameState, state.gameStateRevision)
        if (mayorWins) endGame(GameEndReason.MAYOR)
    }
    finishIfDecided()
    continueAfterDayResolution()
}

private fun ClocktowerGameSession.continueAfterDayResolution() {
    if (state.outcome != null || state.pendingKlutzDeath() != null || !state.dayResolved()) return
    expirePoisonAtDusk(state.gameStateRevision)
    commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day-to-night-${state.gameId}-${state.round}",
        state.phase, state.round, nextInputSequence, StorytellerPhase.NIGHT, state.round + 1), state.gameStateRevision)
}

internal fun ClocktowerGameSession.confirmKlutzChoice(deathActionId: String, chosenSeat: Int) = confirmAtomically {
    val prior = state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.KlutzChoice }
        .singleOrNull { it.deathActionId == deathActionId }
    if (prior != null) { check(prior.chosenSeat == chosenSeat); return@confirmAtomically }
    val death = requireNotNull(state.pendingKlutzDeath())
    check(death.actionId == deathActionId)
    val chosen = requireNotNull(state.gameState.playerAt(chosenSeat))
    require(chosen.alive && chosenSeat != death.klutzSeat())
    commitConfirmedGameAction(ActionFactDraft.KlutzChoice("klutz-choice:$deathActionId", state.phase, state.round,
        nextInputSequence, death.klutzSeat(), chosenSeat, deathActionId), state.gameState, state.gameStateRevision)
    // Current NGJ has no impairment source or registration exception.
    if (chosen.actualAlignment == Alignment.EVIL) endGame(GameEndReason.KLUTZ, chosenSeat)
    finishIfDecided()
    continueAfterDayResolution()
}

internal fun ClocktowerGameSession.confirmNomination(nominator: Int, nominee: Int, spyChoice: Boolean?, expectedRound: Int = state.round): Boolean = confirmAtomically {
    val id = "nomination-${state.gameId}-$expectedRound-$nominator-$nominee"
    val prior = state.actionTimeline.entries.singleOrNull { it.fact.actionId == id }?.fact as? ActionFact.Nomination
    if (prior != null) {
        check(prior.nominatorRegistersAsTownsfolk == spyChoice)
        return@confirmAtomically state.actionTimeline.entries.any { it.fact.actionId == "$id:virgin-execution" }
    }
    requireOpenDay()
    check(state.round == expectedRound && state.pendingVote() == null) { "Complete the current vote first." }
    val actor = requireNotNull(state.gameState.playerAt(nominator))
    require(actor.alive && nominator != nominee && state.gameState.playerAt(nominee) != null)
    val today = state.actionTimeline.entries.filter { it.point.phase == state.phase && it.point.round == state.round }
    require(today.none { (it.fact as? ActionFact.Nomination)?.let { it.nominatorSeat == nominator || it.nomineeSeat == nominee } == true })
    val result = state.virginNomination(nominator, nominee, spyChoice)
    check(!result.needsSpyChoice) { "Spy registration needs an explicit Storyteller choice." }
    commitConfirmedGameAction(ActionFactDraft.Nomination(id, state.phase, state.round, nextInputSequence,
        nominator, nominee, result.first, spyChoice), state.gameState, state.gameStateRevision)
    if (result.executes) {
        killInDay(nominator, true, "$id:virgin-execution")
        finishIfDecided()
        continueAfterDayResolution()
    }
    result.executes
}

internal fun ClocktowerGameSession.confirmVote(nominator: Int, nominee: Int, voters: List<Int>, expectedRound: Int = state.round) = confirmAtomically {
    val nomination = requireNotNull(state.actionTimeline.entries.lastOrNull { entry ->
        entry.point.phase == StorytellerPhase.DAY && entry.point.round == expectedRound &&
            (entry.fact as? ActionFact.Nomination)?.let { it.nominatorSeat == nominator && it.nomineeSeat == nominee } == true
    })
    val id = "vote:${nomination.fact.actionId}"
    val prior = state.actionTimeline.entries.singleOrNull { it.fact.actionId == id }?.fact as? ActionFact.Vote
    if (prior != null) { check(prior.voterSeats == voters); return@confirmAtomically }
    requireOpenDay()
    check(state.round == expectedRound && state.pendingVote()?.actionId == nomination.fact.actionId)
    require(voters.distinct().size == voters.size)
    val spent = state.actionTimeline.entries.flatMap { (it.fact as? ActionFact.Vote)?.ghostVoterSeats.orEmpty() }.toSet()
    val ghosts = voters.filter { seat ->
        val player = requireNotNull(state.gameState.playerAt(seat))
        require(player.alive || seat !in spent) { "A spent ghost vote cannot be used twice." }
        !player.alive
    }
    val butler = state.gameState.players.singleOrNull { it.alive && !it.poisoned && it.actualRole.value == "Butler" }
    if (butler != null && butler.seat in voters) {
        require((state.actionTimeline.entries.asReversed().firstNotNullOfOrNull { (it.fact as? ActionFact.ButlerMasterChoice)?.masterSeat }) in voters) { "Butler may only vote with their master." }
    }
    commitConfirmedGameAction(ActionFactDraft.Vote(id, state.phase, state.round, nextInputSequence,
        nominator, nominee, voters, ghosts), state.gameState, state.gameStateRevision)
}

internal fun ClocktowerSessionState.slayerNeedsRegistration(claimant: Int, target: Int): Boolean =
    gameState.playerAt(claimant)?.let { it.alive && it.actualRole.value == "Slayer" && !it.poisoned } == true &&
        actionTimeline.entries.none { (it.fact as? ActionFact.SlayerShot)?.abilityConsumed == true } &&
        gameState.playerAt(target)?.let { it.alive && !it.poisoned && it.actualRole.value == "Recluse" } == true

internal fun ClocktowerGameSession.confirmSlayerShot(claimant: Int, target: Int, registersAsDemon: Boolean?) = confirmAtomically {
    val id = "slayer-${state.gameId}-$claimant"
    val previous = state.actionTimeline.entries.singleOrNull { it.fact.actionId == id }?.fact as? ActionFact.SlayerShot
    if (previous != null) {
        check(previous.targetSeat == target && previous.targetRegistersAsDemon == registersAsDemon)
        return@confirmAtomically
    }
    requireOpenDay()
    val actor = requireNotNull(state.gameState.playerAt(claimant))
    val victim = requireNotNull(state.gameState.playerAt(target))
    require(actor.alive)
    val consumed = actor.actualRole.value == "Slayer" && state.actionTimeline.entries.none {
        (it.fact as? ActionFact.SlayerShot)?.abilityConsumed == true }
    check(!state.slayerNeedsRegistration(claimant, target) || registersAsDemon != null)
    val hit = consumed && !actor.poisoned && victim.alive && (victim.actualType == CharacterType.DEMON ||
        (state.slayerNeedsRegistration(claimant, target) && registersAsDemon == true))
    commitConfirmedGameAction(ActionFactDraft.SlayerShot(id, state.phase, state.round, nextInputSequence,
        claimant, target, consumed, hit, registersAsDemon), state.gameState, state.gameStateRevision)
    if (hit) killInDay(target, false, "$id:death")
    finishIfDecided()
}

/** The player asks an arbitrary real question; the Storyteller supplies its truth value. */
internal fun ClocktowerSessionState.artistHasFunctioningAbility(claimant: Int): Boolean =
    gameState.playerAt(claimant)?.let { it.alive && it.actualRole.value == "Artist" && !it.poisoned } == true

internal fun ClocktowerGameSession.confirmArtistAnswer(claimant: Int, truthful: Boolean, shown: Boolean) = confirmAtomically {
    val id = "artist-${state.gameId}-$claimant"
    val previous = state.actionTimeline.entries.singleOrNull { it.fact.actionId == id }?.fact as? ActionFact.ArtistAnswer
    if (previous != null) { check(previous.shownAnswer == shown); return@confirmAtomically }
    requireOpenDay()
    val actor = requireNotNull(state.gameState.playerAt(claimant))
    require(actor.alive)
    val consumed = actor.actualRole.value == "Artist"
    require(!state.artistHasFunctioningAbility(claimant) || truthful == shown) { "A functioning Artist receives the truthful answer." }
    commitConfirmedGameAction(ActionFactDraft.ArtistAnswer(id, state.phase, state.round, nextInputSequence, claimant, shown, consumed),
        state.gameState, state.gameStateRevision,
        publishedObservation = EpistemicObservationDraft("$id:answer", state.phase, state.round, nextInputSequence + 1,
            claimant, RoleId("Artist"), ObservationVisibility.PRIVATE, setOf(claimant),
            if (consumed && !actor.poisoned) ObservationReliability.RECEIVED_AS_FUNCTIONING else ObservationReliability.KNOWN_MALFUNCTIONING,
            InformationProposition.BooleanResult(BooleanMetric.PLAYER_QUESTION_ANSWER, claimant, listOf(claimant), shown)))
}
