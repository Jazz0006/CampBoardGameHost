package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.flow.*

/** Read the next real interaction from the one Session. No UI cursor or saved pending operation. */
internal fun ClocktowerSessionState.nightInteractions(ruleset: ValidatedClocktowerRuleset): List<ClocktowerHostInteraction> {
    if (phase == StorytellerPhase.DAY) return emptyList()
    require(ruleset.script.id == gameState.script)
    check(gameState.players.count { it.alive && it.actualRole.value == "Imp" } <= 1) { "Session has multiple living Imps." }
    val first = phase == StorytellerPhase.FIRST_NIGHT
    val flowPhase = if (first) ClocktowerNightFlowPhase.FIRST_NIGHT else ClocktowerNightFlowPhase.OTHER_NIGHT
    val prefix = if (first) "first_night" else "other_night"
    val order = requireNotNull(if (first) ruleset.script.firstNightOverride else ruleset.script.otherNightOverride)
    val sameNight = actionTimeline.entries.filter { it.point.phase == phase && it.point.round == round }
    val deadSeats = sameNight.mapNotNull { (it.fact as? ActionFact.Death)?.targetSeat }.toSet()
    val hadExecution = actionTimeline.entries.any {
        it.point.phase == StorytellerPhase.DAY && it.point.round == round - 1 && it.fact is ActionFact.Execution
    }
    fun interaction(id: String, role: RoleId?, kind: ClocktowerHostInteractionKind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION) =
        ClocktowerHostInteraction(ClocktowerInteractionId(id), flowPhase, role, kind,
            if (kind == ClocktowerHostInteractionKind.EVIL_INFORMATION) ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY
            else ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION)
    return buildList {
        if (!first && pendingImpNotification() != null) add(interaction(
            "other_night:event:imp:new_demon_identity", RoleId("Imp"), ClocktowerHostInteractionKind.EVENT_RESOLUTION))
        order.forEach { token ->
            when (token) {
                is NightOrderToken.Character -> {
                    val role = token.roleId
                    val actor = wakingPlayerForRole(role) ?: return@forEach
                    if (role.value in setOf("Mayor", "Scarlet Woman")) return@forEach
                    val triggered = !first && role.value in setOf("Ravenkeeper", "Sage") && actor.seat in deadSeats
                    if (!actor.alive && !triggered) return@forEach
                    if (role.value == "Undertaker" && !hadExecution) return@forEach
                    if (!first && role.value in setOf("Ravenkeeper", "Sage") && !triggered) return@forEach
                    if (first && role.value == "Fortune Teller" && actor.actualRole == role) add(interaction(
                        "first_night:fortune_teller:red_herring", role, ClocktowerHostInteractionKind.STORYTELLER_SETUP))
                    add(interaction("$prefix:role:${role.value}", role))
                    if (!first && role.value == "Imp" && mayorRulingSeat() != null) add(interaction(
                        "other_night:event:mayor:death_resolution", RoleId("Mayor"), ClocktowerHostInteractionKind.EVENT_RESOLUTION))
                }
                NightOrderToken.System.MINION_INFO -> if (first && firstNightEvilInformationRequired() &&
                    gameState.players.any { it.actualType == CharacterType.MINION }) add(interaction(
                    "first_night:system:minion_info", null, ClocktowerHostInteractionKind.EVIL_INFORMATION))
                NightOrderToken.System.DEMON_INFO -> if (first && firstNightEvilInformationRequired() &&
                    gameState.players.any { it.actualType == CharacterType.DEMON }) add(interaction(
                    "first_night:system:demon_info", null, ClocktowerHostInteractionKind.EVIL_INFORMATION))
                NightOrderToken.System.DUSK, NightOrderToken.System.DAWN -> Unit
            }
        }
    }.also { require(it.map { step -> step.id }.distinct().size == it.size) }
}

internal fun PlayerState.wakingRole(): RoleId = if (actualRole.value == "Drunk") shownRole ?: actualRole else actualRole

/** A real role change remains unannounced until its private information has been confirmed. */
internal fun ClocktowerSessionState.pendingImpNotification(): ActionFact.RoleChange? =
    actionTimeline.entries.asReversed().mapNotNull { it.fact as? ActionFact.RoleChange }.firstOrNull { change ->
        change.role == RoleId("Imp") && gameState.playerAt(change.targetSeat)?.let { it.alive && it.actualRole == change.role } == true &&
            epistemicObservationLog.records.none { it.recordId == "role-notification:${change.actionId}" }
    }

internal fun ClocktowerSessionState.mayorRulingSeat(): Int? {
    val target = nightTargets.attackSeat?.let(gameState::playerAt) ?: return null
    if (phase != StorytellerPhase.NIGHT || !target.alive || target.poisoned || target.actualRole.value != "Mayor") return null
    if (gameState.players.none { it.alive && !it.poisoned && it.actualRole.value == "Imp" }) return null
    if (nightTargets.monkSeat == target.seat && gameState.players.any {
        it.alive && !it.poisoned && it.actualRole.value == "Monk"
    }) return null
    if (actionTimeline.entries.any { it.point.phase == phase && it.point.round == round && it.fact is ActionFact.MayorRedirectChoice }) return null
    return target.seat
}

internal fun ClocktowerSessionState.nextNightInteraction(ruleset: ValidatedClocktowerRuleset): ClocktowerHostInteraction? =
    nightInteractions(ruleset).firstOrNull { !nightInteractionConfirmed(it) }

internal fun ClocktowerSessionState.nightInteractionConfirmed(step: ClocktowerHostInteraction): Boolean {
    val id = step.id.value
    if (id == "other_night:event:imp:new_demon_identity") return pendingImpNotification() == null
    if (id == "first_night:fortune_teller:red_herring") return redHerringSeat != null
    val facts = actionTimeline.entries.filter { it.point.phase == phase && it.point.round == round }.map { it.fact }
    if (id == "other_night:event:mayor:death_resolution") return facts.any { it is ActionFact.MayorRedirectChoice }
    return when (step.roleId?.value) {
        "Poisoner" -> facts.any { it is ActionFact.Poison && it.actionId.startsWith("night-") }
        "Monk" -> facts.any { it is ActionFact.Protect }
        "Imp" -> facts.any { it is ActionFact.Attack }
        "Butler" -> facts.any { it is ActionFact.ButlerMasterChoice }
        "Spy" -> facts.any { it is ActionFact.NightWake && it.stepId == id }
        else -> epistemicObservationLog.records.any { observation ->
            observation.phase == phase && observation.round == round && observation.visibility == ObservationVisibility.PRIVATE &&
                if (step.kind == ClocktowerHostInteractionKind.EVIL_INFORMATION) observation.recordId == nightInformationId(id)
                else observation.sourceAbility == step.roleId && gameState.playerAt(observation.sourceSeat ?: -1)?.wakingRole() == step.roleId
        }
    }
}

private fun ClocktowerSessionState.nightInformationId(stepId: String) = "night-information:$gameId:${phase.name}:$round:$stepId"

/** Spy sees the existing table. Record only the real wake, never a Grimoire mirror. */
internal fun ClocktowerGameSession.confirmSpyWake(ruleset: ValidatedClocktowerRuleset, stepId: String) {
    val prior = state.actionTimeline.entries.any { it.point.phase == state.phase && it.point.round == state.round &&
        (it.fact as? ActionFact.NightWake)?.stepId == stepId }
    if (prior) return
    val current = requireNotNull(state.nextNightInteraction(ruleset))
    check(current.id.value == stepId && current.roleId == RoleId("Spy")) { "Spy wake is not the current step." }
    val actor = state.gameState.players.single { it.alive && it.wakingRole() == RoleId("Spy") }
    commitConfirmedGameAction(ActionFactDraft.NightWake(
        "night-wake-${state.gameId}-${state.phase}-${state.round}-$stepId", state.phase, state.round, nextInputSequence,
        actor.seat, RoleId("Spy"), stepId,
    ), state.gameState, state.gameStateRevision)
}

/** Explicit Next confirms exactly the evil information actually previewed, with its real recipients. */
internal fun ClocktowerGameSession.confirmEvilInformation(
    ruleset: ValidatedClocktowerRuleset,
    stepId: String,
    expectedRevision: StorytellerDecisionRevision = StorytellerDecisionRevision(state.gameStateRevision, state.playerInputRevision),
) {
    val recordId = state.nightInformationId(stepId)
    if (state.epistemicObservationLog.records.any { it.recordId == recordId }) return
    check(expectedRevision == StorytellerDecisionRevision(state.gameStateRevision, state.playerInputRevision)) { "Stale evil information preview." }
    val current = requireNotNull(state.nextNightInteraction(ruleset))
    check(current.id.value == stepId && current.kind == ClocktowerHostInteractionKind.EVIL_INFORMATION)
    val minions = state.gameState.players.filter { it.actualType == CharacterType.MINION }
    val demon = state.gameState.players.single { it.actualType == CharacterType.DEMON }
    val minionInfo = stepId.endsWith("minion_info")
    val recipients = if (minionInfo) minions.mapTo(linkedSetOf()) { it.seat } else setOf(demon.seat)
    val propositions = minions.map { InformationProposition.CharacterTypeAt(it.seat, CharacterType.MINION) } +
        if (minionInfo) listOf(InformationProposition.CharacterTypeAt(demon.seat, CharacterType.DEMON))
        else state.demonBluffRoleIds.map { InformationProposition.RoleInPlay(it, false) }
    commitGlobalEpistemicObservation(EpistemicObservationDraft(
        recordId, state.phase, state.round, nextInputSequence,
        if (minionInfo) minions.first().seat else demon.seat,
        null, ObservationVisibility.PRIVATE,
        recipients, ObservationReliability.NOT_ABILITY_INFORMATION, InformationProposition.AllOf(propositions),
    ))
}

internal fun ClocktowerGameSession.confirmImpNotification() {
    val change = state.pendingImpNotification() ?: return
    check(state.phase == StorytellerPhase.NIGHT)
    commitGlobalEpistemicObservation(EpistemicObservationDraft(
        "role-notification:${change.actionId}", state.phase, state.round, nextInputSequence, change.targetSeat, RoleId("Imp"),
        ObservationVisibility.PRIVATE, setOf(change.targetSeat), ObservationReliability.NOT_ABILITY_INFORMATION,
        InformationProposition.RoleAt(change.targetSeat, RoleId("Imp")),
    ))
}
