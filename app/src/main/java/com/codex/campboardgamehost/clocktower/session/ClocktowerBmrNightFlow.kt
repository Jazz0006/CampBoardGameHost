package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.flow.*

/** The Moonchild's public choice made during the previous day; one lookup for flow, selection and resolution. */
internal fun ClocktowerSessionState.bmrPreviousDayMoonchildChoice(actorSeat: Int): ActionFact.MoonchildChoice? =
    actionTimeline.entries.lastOrNull { e -> e.point.phase == StorytellerPhase.DAY && e.point.round == round - 1 &&
        (e.fact as? ActionFact.MoonchildChoice)?.actorSeat == actorSeat }?.fact as? ActionFact.MoonchildChoice

internal fun ClocktowerSessionState.bmrPreviousDayGossipStatement(actorSeat: Int): ActionFact.GossipStatement? =
    actionTimeline.entries.firstOrNull { e -> e.point.phase == StorytellerPhase.DAY && e.point.round == round - 1 &&
        (e.fact as? ActionFact.GossipStatement)?.claimantSeat == actorSeat }?.fact as? ActionFact.GossipStatement

/** Seats that actually died during the previous day; a Zombuul's registered death counts only when asked. */
internal fun ClocktowerSessionState.bmrPreviousDayDeathSeats(includeRegistration: Boolean): List<Int> =
    actionTimeline.entries.filter { it.point.phase == StorytellerPhase.DAY && it.point.round == round - 1 }.mapNotNull { when(val fact = it.fact) {
        is ActionFact.Death -> fact.targetSeat
        is ActionFact.DeathRegistration -> fact.targetSeat.takeIf { includeRegistration }
        is ActionFact.Execution -> fact.targetSeat.takeIf { fact.causedDeath }
        else -> null
    } }

/** Script order and actual conditions, read from confirmed facts. No persistent step cursor. */
internal fun ClocktowerSessionState.bmrNightInteractions(ruleset: ValidatedClocktowerRuleset): List<ClocktowerHostInteraction> {
    require(gameState.script == BadMoonRisingOpening.script && ruleset.script.id == gameState.script)
    if (phase == StorytellerPhase.DAY || outcome != null) return emptyList()
    val first = phase == StorytellerPhase.FIRST_NIGHT
    val order = requireNotNull(if (first) ruleset.script.firstNightOverride else ruleset.script.otherNightOverride)
    val flow = if (first) ClocktowerNightFlowPhase.FIRST_NIGHT else ClocktowerNightFlowPhase.OTHER_NIGHT
    fun step(id: String, seat: Int, role: String, kind: ClocktowerHostInteractionKind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION) =
        ClocktowerHostInteraction(ClocktowerInteractionId(id), flow, RoleId(role), kind,
            ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION, actorSeat = seat)
    fun id(kind: String, seat: Int, role: String) = "bmr:$kind:$role:$seat:${bmrLifeStart(seat)}"
    val today = actionTimeline.entries.filter { it.point.phase == phase && it.point.round == round }
    fun diedToday(): Boolean = bmrPreviousDayDeathSeats(includeRegistration = true).isNotEmpty()
    val roles = order.filterIsInstance<NightOrderToken.Character>().map { it.roleId.value }
    // A resurrected ordinary role acts only if its position has not already passed.
    fun passedSinceRevival(seat: Int, role: String): Boolean {
        val revived = bmrLifeStart(seat)
        if (revived < 0 || today.none { it.point.globalSequence == revived }) return false
        return today.filter { it.point.globalSequence < revived }.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }.any {
            (it.stepId.startsWith("bmr:role:") || it.stepId.startsWith("bmr:attack:") || it.stepId.startsWith("bmr:regurgitate:")) && roles.indexOf(it.role.value) >= roles.indexOf(role)
        }
    }
    val lunatic = gameState.players.singleOrNull { it.alive && it.actualRole.value == "Lunatic" }
    val demon = gameState.players.singleOrNull { it.alive && it.actualType == CharacterType.DEMON }
    return buildList {
        gameState.players.filter { it.alive && it.actualRole.value == "Grandmother" && bmrGrandchild(it.seat) == null }.forEach {
            add(step(id("grandchild", it.seat, "Grandmother"), it.seat, "Grandmother", ClocktowerHostInteractionKind.STORYTELLER_SETUP))
        }
        today.mapNotNull { it.fact as? ActionFact.AlignmentChange }.forEach { change ->
            val trigger = today.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }.firstOrNull { it.actionId == change.actionId.removeSuffix(":alignment") }
            if(trigger?.targetSeats?.singleOrNull()?.let(::bmrAttackUnfinished) == true) return@forEach
            add(step("bmr:goon-notice:${change.actionId}", change.targetSeat, "Goon", ClocktowerHostInteractionKind.EVENT_RESOLUTION))
        }
        today.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }.filter { it.tookEffect && it.woke }.forEach { use ->
            if (use.role.value == "Exorcist" && demon?.seat in use.targetSeats && demon != null)
                add(step("bmr:exorcist-notice:${use.actionId}", demon.seat, "Exorcist", ClocktowerHostInteractionKind.EVENT_RESOLUTION))
            if (gameState.playerAt(use.actorSeat)?.actualRole?.value == "Lunatic" && use.stepId.startsWith("bmr:role:") && demon != null)
                add(step("bmr:lunatic-targets:${use.actionId}", demon.seat, "Lunatic", ClocktowerHostInteractionKind.EVENT_RESOLUTION))
        }
        // New life start-knowing information is immediate, including resurrection after the usual position.
        if (!first) gameState.players.filter { it.alive && it.actualRole.value in setOf("Grandmother", "Godfather") &&
            bmrLifeStart(it.seat) >= 0 && bmrUses(it.seat, it.actualRole.value).none { use -> use.stepId.startsWith("bmr:info:") }
        }.forEach { add(step(id("info", it.seat, it.actualRole.value), it.seat, it.actualRole.value, ClocktowerHostInteractionKind.STORYTELLER_SETUP)) }
        order.forEach { token -> when (token) {
            is NightOrderToken.Character -> {
                val role = token.roleId.value
                if (role == "Lunatic") {
                    if (lunatic != null) {
                        if (!first && passedSinceRevival(lunatic.seat, "Lunatic")) return@forEach
                        if (first && firstNightEvilInformationRequired()) add(step(id("lunatic-info", lunatic.seat, role), lunatic.seat, role, ClocktowerHostInteractionKind.EVIL_INFORMATION))
                        if (first && demon != null) add(step(id("lunatic-identity", lunatic.seat, role), demon.seat, role, ClocktowerHostInteractionKind.EVENT_RESOLUTION))
                        val shown = requireNotNull(lunatic.shownRole).value
                        if ((!first || shown == "Pukka") && (shown != "Zombuul" || !diedToday()))
                            add(step(id("role", lunatic.seat, shown), lunatic.seat, shown))
                    }
                    return@forEach
                }
                val actor = gameState.players.singleOrNull { it.actualRole.value == role } ?: return@forEach
                if (role == "Moonchild") {
                    if (bmrPreviousDayMoonchildChoice(actor.seat) != null) add(step(id("moonchild", actor.seat, role), actor.seat, role, ClocktowerHostInteractionKind.EVENT_RESOLUTION))
                    return@forEach
                }
                if (!actor.alive) return@forEach
                if (role == "Gossip") {
                    if (bmrPreviousDayGossipStatement(actor.seat)?.truthful == true && !passedSinceRevival(actor.seat, role))
                        add(step(id("gossip", actor.seat, role), actor.seat, role, ClocktowerHostInteractionKind.EVENT_RESOLUTION))
                    return@forEach
                }
                if (role in setOf("Tea Lady", "Minstrel", "Fool", "Pacifist", "Goon", "Mastermind", "Tinker")) return@forEach
                if (role == "Grandmother" || (first && role == "Godfather")) {
                    if (first) add(step(id("info", actor.seat, role), actor.seat, role, ClocktowerHostInteractionKind.STORYTELLER_SETUP))
                    return@forEach
                }
                if (passedSinceRevival(actor.seat, role)) return@forEach
                if (role in BMR_ONCE_PER_GAME_ROLES && bmrUses(actor.seat, role).any { it.used }) return@forEach
                if (role == "Zombuul" && diedToday()) return@forEach
                if (role == "Godfather" && bmrPreviousDayDeathSeats(includeRegistration = false).none {
                    gameState.playerAt(it)?.actualType == CharacterType.OUTSIDER }) return@forEach
                if (role == "Shabaloth" && !first && bmrRegurgitationCandidates(actor.seat).isNotEmpty()) add(step(id("regurgitate", actor.seat, role), actor.seat, role, ClocktowerHostInteractionKind.EVENT_RESOLUTION))
                if(role in setOf("Po", "Shabaloth")) {
                    val confirmed = bmrAttackChoices(actor.seat)
                    if(confirmed.none { it.targetSeats.isEmpty() } && confirmed.size < bmrAttackQuota(actor.seat, role))
                        add(step(id("attack", actor.seat, role) + ":${confirmed.size}", actor.seat, role))
                } else add(step(id("role", actor.seat, role), actor.seat, role))
                if (role == "Pukka" && !first) pukkaPoisonsDue().forEach { choice ->
                    add(step("bmr:pukka-settlement:${choice.fact.actionId}", actor.seat, role, ClocktowerHostInteractionKind.EVENT_RESOLUTION))
                }
            }
            NightOrderToken.System.MINION_INFO, NightOrderToken.System.DEMON_INFO -> if (first && firstNightEvilInformationRequired()) {
                val minionInfo = token == NightOrderToken.System.MINION_INFO
                val actor = if (minionInfo) gameState.players.firstOrNull { it.actualType == CharacterType.MINION } else demon
                if (actor != null) add(ClocktowerHostInteraction(ClocktowerInteractionId(if(minionInfo) "first_night:system:minion_info" else "first_night:system:demon_info"),
                    flow, null, ClocktowerHostInteractionKind.EVIL_INFORMATION, ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY, actorSeat = actor.seat))
            }
            else -> Unit
        } }
    }
}

internal fun ClocktowerSessionState.bmrNightInteractionConfirmed(step: ClocktowerHostInteraction): Boolean {
    if (step.id.value.startsWith("bmr:pukka-settlement:")) return actionTimeline.entries.any {
        (it.fact as? ActionFact.PukkaPoisonEnded)?.choiceActionId == step.id.value.removePrefix("bmr:pukka-settlement:") }
    if (step.roleId?.value == "Pukka" && gameState.playerAt(step.actorSeat ?: 0)?.actualRole?.value == "Pukka" && step.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION)
        return actionTimeline.entries.any { it.point.phase == phase && it.point.round == round &&
            (it.fact as? ActionFact.PukkaChoice)?.actorSeat == step.actorSeat } || bmrStepUse(step.id.value) != null
    return bmrStepUse(step.id.value) != null || epistemicObservationLog.records.any {
        it.phase == phase && it.round == round && it.recordId == nightInformationId(step.id.value) }
}

internal fun ClocktowerSessionState.bmrStepUse(id: String): ActionFact.BmrAbilityUse? = actionTimeline.entries.lastOrNull {
    it.point.phase == phase && it.point.round == round && (it.fact as? ActionFact.BmrAbilityUse)?.stepId == id
}?.fact as? ActionFact.BmrAbilityUse

internal fun ClocktowerSessionState.nextBmrNightInteraction(ruleset: ValidatedClocktowerRuleset): ClocktowerHostInteraction? =
    bmrNightInteractions(ruleset).firstOrNull { !bmrNightInteractionConfirmed(it) }

internal fun ClocktowerSessionState.bmrRegurgitationCandidates(actorSeat: Int): Set<Int> = actionTimeline.entries.filter {
    it.point.phase == StorytellerPhase.NIGHT && it.point.round == round - 1 &&
        (it.fact as? ActionFact.BmrAbilityUse)?.let { use -> use.actorSeat == actorSeat && use.role.value == "Shabaloth" && use.stepId.startsWith("bmr:attack:") } == true
}.flatMap { (it.fact as ActionFact.BmrAbilityUse).targetSeats }.filterTo(linkedSetOf()) { gameState.playerAt(it)?.alive == false }

internal fun ClocktowerGameSession.confirmBmrRegurgitation(stepId: String, targetSeat: Int?, expectedRevision: Long,
    ruleset: ValidatedClocktowerRuleset) = confirmAtomically {
    state.bmrStepUse(stepId)?.let { check(it.targetSeats == listOfNotNull(targetSeat)); return@confirmAtomically }
    check(state.gameStateRevision == expectedRevision)
    val step = requireNotNull(state.nextBmrNightInteraction(ruleset))
    check(step.id.value == stepId && stepId.startsWith("bmr:regurgitate:"))
    val actor = requireNotNull(state.gameState.playerAt(requireNotNull(step.actorSeat)))
    require(actor.alive && (targetSeat == null || !actor.impaired && targetSeat in state.bmrRegurgitationCandidates(actor.seat)))
    val id = "bmr:${state.gameId}:${state.round}:$stepId"
    commitConfirmedGameAction(com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.BmrAbilityUse(id, state.phase,
        state.round, nextInputSequence, actor.seat, actor.actualRole, listOfNotNull(targetSeat), null, targetSeat != null,
        stepId, false, targetSeat != null), state.gameState, state.gameStateRevision)
    if (targetSeat != null) confirmRevival("$id:revive", targetSeat)
}
