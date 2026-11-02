package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*

/** Transient manual input; only accepted actions/results, never this draft, are saved. */
internal data class BmrNightChoice(
    val stepId: String,
    val targets: List<Int> = emptyList(),
    val chosenRole: RoleId? = null,
    val useAbility: Boolean = true,
    val drunkSeat: Int? = null,
    val deaths: List<Boolean> = emptyList(),
    val grandmotherDies: Boolean? = null,
)

internal fun ClocktowerSessionState.bmrLifeStart(seat: Int): Long = actionTimeline.entries.lastOrNull {
    (it.fact as? ActionFact.Revival)?.targetSeat == seat
}?.point?.globalSequence ?: -1L

internal fun ClocktowerSessionState.bmrUses(seat: Int, role: String): List<ActionFact.BmrAbilityUse> {
    val start = bmrLifeStart(seat)
    return actionTimeline.entries.filter { it.point.globalSequence > start }.mapNotNull {
        (it.fact as? ActionFact.BmrAbilityUse)?.takeIf { f -> f.actorSeat == seat && f.role.value == role }
    }
}

internal fun ClocktowerSessionState.bmrGoonChosenTonight(seat: Int): Boolean = actionTimeline.entries.any {
    it.point.phase == phase && it.point.round == round && it.point.globalSequence > bmrLifeStart(seat) &&
        (it.fact as? ActionFact.BmrAbilityUse)?.let { f -> f.actorSeat == seat && f.role.value == "Goon" && f.used } == true
}

internal fun ClocktowerSessionState.bmrGoonWouldTrigger(targetSeat: Int): Boolean = gameState.playerAt(targetSeat)?.let {
    it.alive && !it.impaired && it.actualRole.value == "Goon" && !bmrGoonChosenTonight(it.seat)
} == true

internal fun ClocktowerSessionState.bmrChoiceFunctioning(actorSeat: Int, targets: List<Int>): Boolean =
    gameState.playerAt(actorSeat)?.let { it.alive && !it.impaired } == true && targets.none(::bmrGoonWouldTrigger)

internal fun ClocktowerSessionState.bmrGamblerCanDie(actorSeat: Int, targetSeat: Int, guessedRole: RoleId): Boolean =
    bmrChoiceFunctioning(actorSeat, listOf(targetSeat)) && gameState.playerAt(targetSeat)?.actualRole != guessedRole

internal fun ClocktowerSessionState.bmrFoolCanSave(seat: Int): Boolean = gameState.playerAt(seat)?.let {
    it.alive && !it.impaired && it.actualRole.value == "Fool" && bmrUses(seat, "Fool").none { use -> use.used }
} == true

/** Trigger immediately, including when the chooser was already impaired. ST choices never call this. */
internal fun ClocktowerGameSession.triggerBmrGoon(actorSeat: Int, targetSeat: Int, choiceId: String) {
    val goon = requireNotNull(state.gameState.playerAt(targetSeat))
    if (!state.bmrGoonWouldTrigger(targetSeat)) return
    val actor = requireNotNull(state.gameState.playerAt(actorSeat))
    val id = "$choiceId:goon"
    commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse(id, state.phase, state.round, nextInputSequence,
        goon.seat, goon.actualRole, listOf(actorSeat), null, true, id, false, true), state.gameState, state.gameStateRevision)
    startBmrDrunkenness(id, goon.seat, listOf(actorSeat))
    if (goon.actualAlignment != actor.actualAlignment) commitConfirmedGameAction(ActionFactDraft.AlignmentChange(
        "$id:alignment", state.phase, state.round, nextInputSequence, goon.seat, actor.actualAlignment),
        state.gameState.copy(players = state.gameState.players.map { if (it.seat == goon.seat) it.copy(actualAlignment = actor.actualAlignment) else it }),
        state.gameStateRevision)
}

/** One role choice and its actual ordered outcomes share the existing Session atomic save. */
internal fun ClocktowerGameSession.confirmBmrNightChoice(input: BmrNightChoice, expectedRevision: Long, ruleset: com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset) = confirmAtomically {
    check(state.gameState.script == BadMoonRisingOpening.script && state.phase != StorytellerPhase.DAY && state.outcome == null)
    val prior = state.bmrStepUse(input.stepId)
    if (prior != null) {
        check(prior.targetSeats == input.targets && prior.chosenRole == input.chosenRole && prior.used == input.useAbility)
        return@confirmAtomically
    }
    check(state.gameStateRevision == expectedRevision) { "Stale BMR choice." }
    val step = requireNotNull(state.nextBmrNightInteraction(ruleset))
    check(step.id.value == input.stepId)
    val actor = requireNotNull(state.gameState.playerAt(requireNotNull(step.actorSeat)))
    val role = requireNotNull(step.roleId).value
    check(step.kind == com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind.ROLE_PHASE_ACTION)
    check(role in setOf("Sailor", "Courtier", "Innkeeper", "Gambler", "Devil's Advocate", "Exorcist", "Zombuul", "Pukka", "Shabaloth", "Po", "Assassin", "Godfather", "Professor"))
    check(role != "Pukka" || actor.actualRole.value == "Lunatic")
    val fake = actor.actualRole.value == "Lunatic"
    require(fake || actor.actualType != CharacterType.DEMON || !state.bmrExorcised(actor.seat))
    val selection = state.bmrNightSelection(step, input.targets, input.useAbility, input.targets.isEmpty(), input.chosenRole)
    require(selection.targetsValid(input.targets))
    require(selection.optionalUse || input.useAbility)
    if (selection.roleRequired) require(input.chosenRole in selection.allowedRoles) else require(input.chosenRole == null)
    require(input.deaths.size == selection.deathSeats.size)
    selection.deathSeats.forEachIndexed { index, seat -> require(seat !in selection.forcedNoDeathSeats || !input.deaths[index]) }
    if (selection.drunkSeats.isEmpty()) require(input.drunkSeat == null) else require(input.drunkSeat in selection.drunkSeats)
    if (selection.optionalUse) require(state.bmrUses(actor.seat, role).none { it.used })
    val id = "bmr:${state.gameId}:${state.round}:${input.stepId}"
    val available = !actor.impaired
    if (fake || role !in setOf("Shabaloth", "Po", "Zombuul", "Assassin", "Godfather")) {
        val selected = if (role == "Courtier") state.gameState.players.filter { it.actualRole == input.chosenRole }.map { it.seat } else input.targets
        selected.forEach { triggerBmrGoon(actor.seat, it, id) }
    }
    val effective = input.useAbility && available && !requireNotNull(state.gameState.playerAt(actor.seat)).impaired
    commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse(id, state.phase, state.round, nextInputSequence,
        actor.seat, RoleId(role), input.targets.toList(), input.chosenRole, input.useAbility, input.stepId, selection.attackNumber == null || selection.attackNumber == 1, effective),
        state.gameState, state.gameStateRevision)
    if (fake) return@confirmAtomically
    when (role) {
        "Sailor", "Innkeeper" -> if (effective) {
            startBmrDrunkenness(id, actor.seat, listOf(requireNotNull(input.drunkSeat)))
        }
        "Courtier" -> if (effective && input.useAbility) {
            val target = state.gameState.players.singleOrNull { it.actualRole == input.chosenRole }
            if (target != null) startBmrDrunkenness(id, actor.seat, listOf(target.seat))
        }
        "Professor" -> if (effective && input.useAbility) {
            val target = state.gameState.playerAt(input.targets.single())!!
            if (!target.alive && target.actualType == CharacterType.TOWNSFOLK) confirmRevival("$id:revive", target.seat)
        }
        "Gambler" -> {
            require(input.chosenRole != null)
            require(input.deaths.size == 1)
            if (state.bmrGamblerCanDie(actor.seat, input.targets.single(), requireNotNull(input.chosenRole)))
                resolveBmrDeath("$id:gamble", actor.seat, input.deaths.single())
            else require(!input.deaths.single())
        }
        "Shabaloth", "Po", "Zombuul", "Assassin", "Godfather" -> {
            require(input.deaths.size == input.targets.size)
            input.targets.forEachIndexed { index, seat ->
                val before = requireNotNull(state.gameState.playerAt(actor.seat))
                val functionedBeforeGoon = before.alive && !before.impaired
                triggerBmrGoon(actor.seat, seat, "$id:$index")
                val after = requireNotNull(state.gameState.playerAt(actor.seat))
                val canKill = functionedBeforeGoon && (role == "Assassin" || !after.impaired)
                if (canKill) resolveBmrDeath("$id:death:$index", seat, input.deaths[index], bypassProtection = role == "Assassin", sourceIsDemon = role in setOf("Shabaloth", "Po", "Zombuul"), grandmotherDies = input.grandmotherDies)
                else require(!input.deaths[index]) { "An impaired attacker cannot cause a death." }
            }
        }
    }
}

/** Manual death resolution with shared fixed survival/registration checks; outcomes are separate facts. */
internal fun ClocktowerGameSession.resolveBmrDeath(id: String, seat: Int, dies: Boolean, bypassProtection: Boolean = false,
    sourceIsDemon: Boolean = false, grandmotherDies: Boolean? = null) {
    val target = requireNotNull(state.gameState.playerAt(seat))
    if (!target.alive) { require(!dies); return }
    if (!bypassProtection && state.bmrCertainlyProtected(seat)) { require(!dies) { "The target is protected from this death." }; return }
    if (!bypassProtection && state.bmrFoolCanSave(seat)) {
        require(!dies) { "The Fool's first death is prevented." }
        commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("$id:fool", state.phase, state.round, nextInputSequence,
            seat, target.actualRole, listOf(seat), null, true, "$id:fool", false, true), state.gameState, state.gameStateRevision)
        return
    }
    if (!dies) return
    if (!bypassProtection && target.actualRole.value == "Zombuul" && !target.registersAsDead && !target.impaired) {
        confirmDeathRegistration("$id:registration", seat)
        return
    }
    val grandmother = if (sourceIsDemon) state.bmrGrandmotherAtRisk(seat)?.let(state.gameState::playerAt) else null
    if (grandmother != null) require(grandmotherDies != null) { "Confirm the Grandmother's actual death/protection result." }
    commitConfirmedPlayerDeath(ActionFactDraft.Death(id, state.phase, state.round, nextInputSequence, seat), state.gameStateRevision)
    if (grandmother != null) {
        commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("$id:grandmother", state.phase, state.round, nextInputSequence,
            grandmother.seat, grandmother.actualRole, listOf(seat), null, true, "$id:grandmother", false, true), state.gameState, state.gameStateRevision)
        resolveBmrDeath("$id:grandmother-death", grandmother.seat, requireNotNull(grandmotherDies))
    }
}

internal fun ClocktowerSessionState.bmrCertainlyProtected(seat: Int, execution: Boolean = false): Boolean {
    val target = gameState.playerAt(seat) ?: return false
    if (!target.impaired && target.actualRole.value == "Sailor") return true
    val tea = gameState.players.singleOrNull { it.alive && !it.impaired && it.actualRole.value == "Tea Lady" }
    if (tea != null) {
        val living = gameState.players.filter { it.publiclyAlive }.sortedBy { it.seat }
        val i = living.indexOfFirst { it.seat == tea.seat }
        if (i >= 0 && living.size >= 3) {
            val neighbours = listOf(living[(i + living.size - 1) % living.size], living[(i + 1) % living.size])
            if (neighbours.all { it.actualAlignment == Alignment.GOOD } && neighbours.any { it.seat == seat }) return true
        }
    }
    val today = actionTimeline.entries.filter { it.point.round == round }.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }
    return today.any { use -> use.tookEffect && use.used && seat in use.targetSeats &&
        gameState.playerAt(use.actorSeat)?.let { it.alive && !it.impaired } == true &&
        ((phase != StorytellerPhase.DAY && use.role.value == "Innkeeper") ||
            (execution && phase == StorytellerPhase.DAY && use.role.value == "Devil's Advocate")) }
}

internal fun ClocktowerSessionState.bmrGrandmotherAtRisk(childSeat: Int): Int? = gameState.players.singleOrNull { gm ->
    gm.alive && !gm.impaired && gm.seat != childSeat && gm.actualRole.value == "Grandmother" && bmrGrandchild(gm.seat) == childSeat
}?.seat
