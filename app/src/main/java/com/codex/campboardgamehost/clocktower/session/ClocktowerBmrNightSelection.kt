package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.badMoonRisingRoles
import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind

/** Only confirmed real target choices count toward an ordered attack, never UI selection or a wake. */
internal fun ClocktowerSessionState.bmrAttackChoices(actorSeat: Int): List<ActionFact.BmrAbilityUse> = actionTimeline.entries.filter {
    it.point.phase == phase && it.point.round == round && (it.fact as? ActionFact.BmrAbilityUse)?.let { use ->
        use.actorSeat == actorSeat && use.used && use.stepId.startsWith("bmr:attack:")
    } == true
}.map { it.fact as ActionFact.BmrAbilityUse }

internal fun ClocktowerSessionState.bmrAttackQuota(actorSeat: Int, role: String): Int = if(role == "Shabaloth") 2 else {
    val life = bmrLifeStart(actorSeat)
    val last = actionTimeline.entries.lastOrNull { e -> e.point.globalSequence > life && e.point.round < round &&
        (e.fact as? ActionFact.BmrAbilityUse)?.let { use -> use.actorSeat == actorSeat && use.role.value == "Po" &&
            (use.stepId.startsWith("bmr:attack:") || use.stepId.startsWith("bmr:role:")) && use.used && use.woke } == true
    }?.fact as? ActionFact.BmrAbilityUse
    if(last?.targetSeats?.isEmpty() == true) 3 else 1
}

internal fun ClocktowerSessionState.bmrAttackUnfinished(actorSeat: Int): Boolean {
    val actor = gameState.playerAt(actorSeat) ?: return false
    if(!actor.alive || actor.actualRole.value !in setOf("Po", "Shabaloth")) return false
    val choices = bmrAttackChoices(actorSeat)
    return choices.isNotEmpty() && choices.none { it.targetSeats.isEmpty() } && choices.size < bmrAttackQuota(actorSeat, actor.actualRole.value)
}

internal data class BmrNightSelection(
    val minimumTargets: Int,
    val maximumTargets: Int,
    val selectableSeats: Set<Int>,
    val optionalUse: Boolean,
    val chargeAllowed: Boolean,
    val roleRequired: Boolean,
    val allowedRoles: Set<RoleId>,
    val drunkSeats: Set<Int>,
    val deathSeats: List<Int>,
    val forcedNoDeathSeats: Set<Int>,
    val numericValues: Set<Int>,
    val displayRoles: Set<RoleId>,
    val displayRolesMaximum: Int,
    val attackNumber: Int?,
    val attackTotal: Int?,
) {
    fun targetsValid(seats: List<Int>) = seats.size in minimumTargets..maximumTargets &&
        seats.distinct().size == seats.size && seats.all { it in selectableSeats }
}

/** Narrow BMR input facts shared by the table and confirmation; no worlds/candidates/rules engine. */
internal fun ClocktowerSessionState.bmrNightSelection(step: ClocktowerHostInteraction, targets: List<Int>, use: Boolean?,
    charge: Boolean, chosenRole: RoleId?): BmrNightSelection {
    val actor = requireNotNull(gameState.playerAt(requireNotNull(step.actorSeat)))
    val role = step.roleId?.value
    val id = step.id.value
    val fake = actor.actualRole.value == "Lunatic"
    val information = step.roleId == null || listOf("bmr:info:", "bmr:lunatic-info:", "bmr:lunatic-identity:", "bmr:lunatic-targets:", "bmr:exorcist-notice:", "bmr:goon-notice:").any(id::startsWith) || role == "Chambermaid"
    val exorcised = actor.actualType == CharacterType.DEMON && bmrExorcised(actor.seat) && step.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION
    val segmented = !fake && role in setOf("Po", "Shabaloth") && step.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION && !exorcised
    val completed = if(segmented) bmrAttackChoices(actor.seat) else emptyList()
    val quota = if(role in setOf("Po", "Shabaloth")) bmrAttackQuota(actor.seat, requireNotNull(role)) else 1
    val optional = role in setOf("Courtier", "Professor", "Assassin") && !information && !exorcised
    val chargeAllowed = role == "Po" && quota == 1 && (!segmented || completed.isEmpty()) && !exorcised
    val oldPoison = if(id.startsWith("bmr:pukka-settlement:")) pukkaPoisonsDue().single { it.fact.actionId == id.removePrefix("bmr:pukka-settlement:") }.fact as ActionFact.PukkaChoice else null
    val moonchild = if(id.startsWith("bmr:moonchild:")) actionTimeline.entries.lastOrNull { e -> e.point.phase == StorytellerPhase.DAY && e.point.round == round - 1 && (e.fact as? ActionFact.MoonchildChoice)?.actorSeat == actor.seat }?.fact as? ActionFact.MoonchildChoice else null
    val gossipInactive = role == "Gossip" && actor.impaired
    val maximum = when {
        exorcised || oldPoison != null || moonchild != null || gossipInactive -> 0
        id.startsWith("bmr:regurgitate:") -> 1
        id.startsWith("bmr:lunatic-info:") -> gameState.players.count { it.actualType == CharacterType.MINION }
        information && role !in setOf("Chambermaid") -> 0
        role == "Courtier" || optional && use != true -> 0
        role == "Po" && chargeAllowed && charge -> 0
        segmented -> 1
        role in setOf("Innkeeper", "Shabaloth", "Chambermaid") -> 2
        role == "Po" -> quota
        else -> 1
    }
    val minimum = if(id.startsWith("bmr:regurgitate:")) 0 else maximum
    val previous = bmrUses(actor.seat, role.orEmpty()).lastOrNull { it.woke }
    val selected = when {
        maximum == 0 -> emptySet()
        id.startsWith("bmr:grandchild:") -> gameState.players.filter { it.actualAlignment == Alignment.GOOD }.mapTo(mutableSetOf()) { it.seat }
        id.startsWith("bmr:regurgitate:") -> if(actor.impaired) emptySet() else bmrRegurgitationCandidates(actor.seat)
        id.startsWith("bmr:lunatic-info:") -> gameState.players.mapTo(mutableSetOf()) { it.seat } - actor.seat
        role == "Chambermaid" -> bmrChambermaidTargets(actor.seat)
        role in setOf("Sailor", "Devil's Advocate") -> gameState.players.filter { it.publiclyAlive && (role != "Chambermaid" || it.seat != actor.seat) }.mapTo(mutableSetOf()) { it.seat }
        role == "Professor" -> gameState.players.filter { !it.publiclyAlive }.mapTo(mutableSetOf()) { it.seat }
        role == "Gossip" -> gameState.players.filter { it.publiclyAlive }.mapTo(mutableSetOf()) { it.seat }
        else -> gameState.players.mapTo(mutableSetOf()) { it.seat }
    }.let { seats ->
        val excluded = if(role in setOf("Exorcist", "Devil's Advocate")) previous?.targetSeats.orEmpty().toSet() else emptySet()
        seats - excluded - completed.flatMap { it.targetSeats }.toSet()
    }
    val attacks = !fake && role in setOf("Po", "Shabaloth", "Assassin", "Godfather", "Zombuul") && !exorcised
    val deaths = when {
        oldPoison != null -> listOf(oldPoison.targetSeat)
        moonchild != null -> listOf(moonchild.targetSeat)
        gossipInactive -> emptyList()
        role == "Gossip" || attacks -> targets
        role == "Gambler" && !fake && targets.size == 1 && chosenRole != null -> listOf(actor.seat)
        else -> emptyList()
    }
    val forced = deaths.filterTo(mutableSetOf()) { seat ->
        gameState.playerAt(seat)?.alive == false ||
            attacks && actor.impaired ||
            role == "Gambler" && targets.size == 1 && chosenRole != null && !bmrGamblerCanDie(actor.seat, targets.single(), chosenRole) ||
            moonchild != null && (moonchild.chosenAlignment != Alignment.GOOD || actor.impaired) ||
            role != "Assassin" && (bmrCertainlyProtected(seat) || bmrFoolCanSave(seat))
    }
    val drunk = if(!fake && role in setOf("Sailor", "Innkeeper") && targets.size == maximum && bmrChoiceFunctioning(actor.seat, targets))
        (targets + if(role == "Sailor") listOf(actor.seat) else emptyList()).toSet() else emptySet()
    val roleRequired = role == "Gambler" || role == "Courtier" && use == true
    return BmrNightSelection(minimum, maximum, selected, optional, chargeAllowed, roleRequired,
        if(roleRequired || role == "Courtier") badMoonRisingRoles.mapTo(mutableSetOf()) { RoleId(it.enName) } else emptySet(),
        drunk, deaths, forced,
        if(role == "Chambermaid" && targets.size == 2 && targets.distinct().size == 2 && targets.all { it in bmrChambermaidTargets(actor.seat) }) bmrChambermaidValues(actor.seat, targets) else emptySet(),
        badMoonRisingRoles.filter { r -> when {
            id.startsWith("bmr:lunatic-info:") -> r.team in setOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider)
            id.startsWith("bmr:info:Godfather:") && actor.impaired -> r.team == ClocktowerTeam.Outsider
            id.startsWith("bmr:info:Grandmother:") && actor.impaired -> true
            else -> false
        } }.mapTo(mutableSetOf()) { RoleId(it.enName) },
        when { id.startsWith("bmr:lunatic-info:") -> 3; role == "Godfather" -> 4; else -> 1 },
        if(segmented) completed.size + 1 else null, if(segmented) quota else null)
}
