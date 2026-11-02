package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction

internal fun ClocktowerSessionState.bmrInformationPreview(step: ClocktowerHostInteraction, selection: BmrNightSelection,
    targets: List<Int>, bluffs: List<RoleId>, number: Int?, shownSeat: Int?, shownRole: RoleId?): InformationProposition? {
    val state = this
    val actor = requireNotNull(gameState.playerAt(requireNotNull(step.actorSeat)))
    val role = step.roleId?.value
    val id = step.id.value
    val maximum = selection.maximumTargets
    if (step.roleId == null) return firstNightEvilProposition(id.endsWith("minion_info"))
    return when {
        id.startsWith("bmr:info:Grandmother:") -> state.bmrGrandchild(requireNotNull(actor).seat)?.let { child ->
            val p = requireNotNull(state.gameState.playerAt(child))
            if(actor?.impaired == true) InformationProposition.RoleAt(shownSeat ?: child, shownRole ?: p.actualRole) else InformationProposition.RoleAt(child, p.actualRole)
        }
        id.startsWith("bmr:info:Godfather:") -> InformationProposition.CharactersInPlay(CharacterType.OUTSIDER,
            if(actor?.impaired == true) bluffs else state.gameState.players.filter { it.actualType == CharacterType.OUTSIDER }.map { it.actualRole })
        id.startsWith("bmr:lunatic-info:") -> if(targets.size == maximum && bluffs.size == 3) InformationProposition.AllOf(targets.map { InformationProposition.CharacterTypeAt(it, CharacterType.MINION) } + bluffs.map { InformationProposition.RoleInPlay(it, false) }) else null
        id.startsWith("bmr:lunatic-identity:") -> {
            val lunatic = state.gameState.players.single { it.actualRole.value == "Lunatic" }
            InformationProposition.AllOf(listOf(InformationProposition.RoleAt(requireNotNull(actor).seat, actor.actualRole), InformationProposition.RoleAt(lunatic.seat, lunatic.actualRole)))
        }
        id.startsWith("bmr:lunatic-targets:") -> (state.actionTimeline.entries.single { it.fact.actionId == id.removePrefix("bmr:lunatic-targets:") }.fact as ActionFact.BmrAbilityUse).let { InformationProposition.PlayerTargets(it.actorSeat, it.targetSeats) }
        id.startsWith("bmr:exorcist-notice:") -> (state.actionTimeline.entries.single { it.fact.actionId == id.removePrefix("bmr:exorcist-notice:") }.fact as ActionFact.BmrAbilityUse).let { InformationProposition.RoleAt(it.actorSeat, RoleId("Exorcist")) }
        id.startsWith("bmr:goon-notice:") -> InformationProposition.AlignmentAt(requireNotNull(actor).seat, actor.actualAlignment)
        role == "Chambermaid" && targets.size == 2 -> {
            val values = selection.numericValues
            val value = values.singleOrNull() ?: number?.takeIf { it in values }
            value?.let { InformationProposition.NumericResult(NumericMetric.PLAYERS_WAKING_FOR_ABILITY, actor.seat, targets, it) }
        }
        else -> null
    }
}
