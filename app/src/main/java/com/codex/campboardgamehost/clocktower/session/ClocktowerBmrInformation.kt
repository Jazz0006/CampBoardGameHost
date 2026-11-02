package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteraction

internal fun ClocktowerSessionState.bmrChambermaidTargets(actorSeat: Int): Set<Int> = gameState.players.filter {
    it.publiclyAlive && it.seat != actorSeat
}.mapTo(linkedSetOf()) { it.seat }

internal fun ClocktowerSessionState.bmrChambermaidValues(actorSeat: Int, targets: List<Int>): Set<Int> {
    require(targets.size == 2 && targets.distinct().size == 2 && targets.all { it in bmrChambermaidTargets(actorSeat) })
    val goonTriggers = targets.any(::bmrGoonWouldTrigger)
    if (gameState.playerAt(actorSeat)?.impaired == true || goonTriggers) return setOf(0, 1, 2)
    val waking = actionTimeline.entries.filter { it.point.phase == phase && it.point.round == round }.mapNotNull { e -> when(val f = e.fact) {
        is ActionFact.BmrAbilityUse -> f.actorSeat.takeIf { f.woke }
        is ActionFact.PukkaChoice -> f.actorSeat
        else -> null
    } }.toMutableSet()
    epistemicObservationLog.records.filter { it.phase == phase && it.round == round && it.sourceAbility?.value == "Goon" }.mapNotNullTo(waking) { it.sourceSeat }
    return setOf(targets.count { it in waking })
}

/** Actual source/receiver envelope; UI text is rendered only from the selected proposition. */
internal fun ClocktowerSessionState.bmrInformationDraft(step: ClocktowerHostInteraction, proposition: InformationProposition): EpistemicObservationDraft {
    val id = step.id.value
    val receiver = requireNotNull(step.actorSeat)
    val source = when {
        id.startsWith("bmr:lunatic-") -> gameState.players.single { it.actualRole.value == "Lunatic" }.seat
        id.startsWith("bmr:exorcist-notice:") -> actionTimeline.entries.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }.single { it.actionId == id.removePrefix("bmr:exorcist-notice:") }.actorSeat
        else -> receiver
    }
    val sourceRole = when {
        id.startsWith("bmr:lunatic-") -> RoleId("Lunatic")
        id.startsWith("bmr:exorcist-notice:") -> RoleId("Exorcist")
        else -> requireNotNull(step.roleId)
    }
    val impaired = requireNotNull(gameState.playerAt(source)).impaired ||
        (sourceRole.value == "Chambermaid" && proposition is InformationProposition.NumericResult &&
            bmrChambermaidValues(source, proposition.subjectSeats).size > 1)
    return EpistemicObservationDraft(nightInformationId(id), phase, round, nextTimelineGlobalSequence.toInt() + 1,
        source, sourceRole, ObservationVisibility.PRIVATE, setOf(receiver),
        if (impaired) ObservationReliability.KNOWN_MALFUNCTIONING else ObservationReliability.RECEIVED_AS_FUNCTIONING, proposition)
}

/** Commits the LAST displayed observation and its real setup/choice together, on explicit Next. */
internal fun ClocktowerGameSession.confirmBmrInformation(stepId: String, observation: EpistemicObservationDraft,
    expectedRevision: StorytellerDecisionRevision, ruleset: ValidatedClocktowerRuleset) = confirmAtomically {
    val already = state.epistemicObservationLog.records.singleOrNull { it.recordId == state.nightInformationId(stepId) }
    if (already != null) { check(already.proposition == observation.proposition); return@confirmAtomically }
    check(expectedRevision == StorytellerDecisionRevision(state.gameStateRevision, state.playerInputRevision)) { "Stale BMR information preview." }
    val step = requireNotNull(state.nextBmrNightInteraction(ruleset))
    check(step.id.value == stepId)
    require(observation == state.bmrInformationDraft(step, observation.proposition)) { "Invalid private BMR information envelope." }
    val actor = requireNotNull(state.gameState.playerAt(requireNotNull(step.actorSeat)))
    val role = requireNotNull(step.roleId).value
    val prop = observation.proposition
    val impaired = actor.impaired
    var targets = emptyList<Int>()
    var woke = false
    when {
        stepId.startsWith("bmr:info:Grandmother:") -> {
            val child = requireNotNull(state.gameState.playerAt(requireNotNull(state.bmrGrandchild(actor.seat))))
            require(prop is InformationProposition.RoleAt && state.gameState.playerAt(prop.seat) != null)
            require(!impaired || prop.role in state.bmrNightSelection(step, emptyList(), true, false, null).displayRoles)
            require(impaired || prop == InformationProposition.RoleAt(child.seat, child.actualRole))
            targets = listOf(child.seat); woke = true
        }
        stepId.startsWith("bmr:info:Godfather:") -> {
            require(prop is InformationProposition.CharactersInPlay && prop.characterType == CharacterType.OUTSIDER)
            val real = state.gameState.players.filter { it.actualType == CharacterType.OUTSIDER }.map { it.actualRole }.toSet()
            require(impaired || prop.roles.toSet() == real)
            require(!impaired || prop.roles.all { it in state.bmrNightSelection(step, emptyList(), true, false, null).displayRoles })
            woke = true
        }
        stepId.startsWith("bmr:role:Chambermaid:") -> {
            require(prop is InformationProposition.NumericResult && prop.metric == NumericMetric.PLAYERS_WAKING_FOR_ABILITY && prop.sourceSeat == actor.seat)
            require(prop.value in state.bmrChambermaidValues(actor.seat, prop.subjectSeats))
            targets = prop.subjectSeats
            val choiceId = "bmr:${state.gameId}:${state.round}:$stepId"
            targets.forEach { triggerBmrGoon(actor.seat, it, choiceId) }
            woke = true
        }
        stepId.startsWith("bmr:lunatic-info:") -> {
            require(prop is InformationProposition.AllOf)
            val minions = prop.propositions.filterIsInstance<InformationProposition.CharacterTypeAt>()
            val bluffs = prop.propositions.filterIsInstance<InformationProposition.RoleInPlay>()
            val input = state.bmrNightSelection(step, minions.map { it.seat }, true, false, null)
            require(minions.size + bluffs.size == prop.propositions.size && bluffs.size == input.displayRolesMaximum)
            require(input.targetsValid(minions.map { it.seat }) && minions.all { it.characterType == CharacterType.MINION })
            require(bluffs.all { !it.inPlay && it.role in input.displayRoles } && bluffs.map { it.role }.distinct().size == bluffs.size)
            woke = true
        }
        stepId.startsWith("bmr:lunatic-identity:") -> {
            val lunatic = state.gameState.players.single { it.actualRole.value == "Lunatic" }
            require(prop == InformationProposition.AllOf(listOf(InformationProposition.RoleAt(actor.seat, actor.actualRole), InformationProposition.RoleAt(lunatic.seat, lunatic.actualRole))))
        }
        stepId.startsWith("bmr:lunatic-targets:") -> {
            val choice = state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }.single { it.actionId == stepId.removePrefix("bmr:lunatic-targets:") }
            require(prop == InformationProposition.PlayerTargets(choice.actorSeat, choice.targetSeats))
        }
        stepId.startsWith("bmr:exorcist-notice:") -> {
            val use = state.actionTimeline.entries.mapNotNull { it.fact as? ActionFact.BmrAbilityUse }.single { it.actionId == stepId.removePrefix("bmr:exorcist-notice:") }
            require(prop == InformationProposition.RoleAt(use.actorSeat, RoleId("Exorcist")))
        }
        stepId.startsWith("bmr:goon-notice:") -> require(prop == InformationProposition.AlignmentAt(actor.seat, actor.actualAlignment))
        else -> error("Unsupported BMR information operation: $stepId")
    }
    val accepted = observation.copy(sequence = nextInputSequence + 1)
    val actualUse = woke
    val effective = !requireNotNull(state.gameState.playerAt(actor.seat)).impaired
    commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("bmr-info:${state.gameId}:${state.round}:$stepId", state.phase, state.round,
        nextInputSequence, actor.seat, RoleId(role), targets, null, actualUse, stepId, woke, effective), state.gameState,
        state.gameStateRevision, publishedObservation = accepted)
}

/** The actual good grandchild is selected before first-night player choices can change alignment. */
internal fun ClocktowerSessionState.bmrGrandchild(seat: Int): Int? = bmrUses(seat, "Grandmother").lastOrNull {
    it.stepId.startsWith("bmr:grandchild:")
}?.targetSeats?.singleOrNull()

internal fun ClocktowerGameSession.confirmBmrGrandchild(stepId: String, targetSeat: Int, expectedRevision: Long,
    ruleset: ValidatedClocktowerRuleset) {
    state.bmrStepUse(stepId)?.let { check(it.targetSeats == listOf(targetSeat)); return }
    check(state.gameStateRevision == expectedRevision)
    val step = requireNotNull(state.nextBmrNightInteraction(ruleset))
    check(step.id.value == stepId && stepId.startsWith("bmr:grandchild:"))
    val child = requireNotNull(state.gameState.playerAt(targetSeat))
    require(state.bmrNightSelection(step, listOf(child.seat), true, false, null).targetsValid(listOf(child.seat)))
    val actor = requireNotNull(state.gameState.playerAt(requireNotNull(step.actorSeat)))
    commitConfirmedGameAction(ActionFactDraft.BmrAbilityUse("bmr:${state.gameId}:${state.round}:$stepId", state.phase,
        state.round, nextInputSequence, actor.seat, actor.actualRole, listOf(targetSeat), null, true, stepId, false, true), state.gameState, state.gameStateRevision)
}
