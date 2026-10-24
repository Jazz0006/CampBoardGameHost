package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.HostNumericResolution
import com.codex.campboardgamehost.clocktower.session.NumericInformationResolver
import com.codex.campboardgamehost.clocktower.session.aliveSeats
import com.codex.campboardgamehost.clocktower.session.allSeats
import com.codex.campboardgamehost.clocktower.session.impSelfKillSuccession

/** What the current night step is, without cards or labels; built once at the UI edge. */
internal data class ClocktowerNightStepFacts(
    val interactionId: ClocktowerInteractionId,
    val action: ClocktowerNightAction,
    val roleEnName: String?,
    val isRealAction: Boolean,
    val actorSeat: Int?,
    val isEvilInformation: Boolean,
    val question: ClocktowerInformationQuestion?,
)

/**
 * Host rules for the current night step. Reads only Session, the built-in ruleset and the
 * current draft: no AI input, no preview state, no cards. Next readiness also needs the UI's
 * last preview and successor choice, which stay with the screen.
 */
internal data class HostNightStep(
    val facts: ClocktowerNightStepFacts,
    val isPrivateInformation: Boolean,
    val isEvilInformation: Boolean,
    val numericMetric: NumericMetric?,
    val numericResolution: HostNumericResolution?,
    val booleanResolution: HostBooleanResolution?,
    val roleResolution: HostRoleResolution?,
    /** Washerwoman/Librarian/Investigator whose pair query Session resolves. */
    val pairRole: String?,
    val selectedSeat: Int?,
    val legalTargetSeats: Set<Int>,
    /** Player target, Mayor ruling and Red Herring inputs required before Next. */
    val targetReady: Boolean,
    val successorSeats: List<Int>,
) {
    val hostDeterminedNumeric: Boolean get() = numericResolution?.values?.size == 1
    val hostDeterminedInformation: Boolean get() = hostDeterminedNumeric || booleanResolution?.values?.size == 1 || roleResolution?.roles?.size == 1
    fun successorSeat(chosen: Int?): Int? = when (successorSeats.size) {
        0 -> null
        1 -> successorSeats.single()
        else -> chosen?.takeIf { it in successorSeats }
    }
    fun successorReady(chosen: Int?): Boolean = successorSeats.size <= 1 || successorSeat(chosen) != null
}

private val pairInformationRoles = setOf("Washerwoman", "Librarian", "Investigator")
private val privateInformationActions = setOf(
    ClocktowerNightAction.None, ClocktowerNightAction.FortuneTeller,
    ClocktowerNightAction.Chambermaid, ClocktowerNightAction.Ravenkeeper,
)
private val singleTargetActions = setOf(
    ClocktowerNightAction.Poison, ClocktowerNightAction.ButlerMaster, ClocktowerNightAction.MonkProtect,
    ClocktowerNightAction.DemonKill, ClocktowerNightAction.MayorRedirect, ClocktowerNightAction.Ravenkeeper,
)


/**
 * Identity, role, action, actor and query are all derived from the one current Session
 * interaction. The UI cannot supply or override the factual part of a Host step.
 */
private fun ClocktowerSessionState.currentNightStepFacts(
    ruleset: ValidatedClocktowerRuleset,
    draft: ClocktowerNightStepDraft?,
): ClocktowerNightStepFacts {
    val current = requireNotNull(nextNightInteraction(ruleset)) { "No pending Session night interaction." }
    require(draft == null || (
        draft.gameId == gameId && draft.phase == phase &&
            draft.round == round && draft.interactionId == current.id
    )) { "Night draft does not belong to the current Session interaction." }
    val role = current.roleId?.value
    val redHerring = current.id.value == "first_night:fortune_teller:red_herring"
    val mayorRuling = current.id.value == "other_night:event:mayor:death_resolution"
    val actorSeat = if (current.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION) {
        current.roleId?.let(::wakingPlayerForRole)?.seat
    } else null
    val action = when {
        redHerring -> ClocktowerNightAction.RedHerring
        mayorRuling -> ClocktowerNightAction.MayorRedirect
        current.kind != ClocktowerHostInteractionKind.ROLE_PHASE_ACTION -> ClocktowerNightAction.None
        role == "Poisoner" -> ClocktowerNightAction.Poison
        role == "Monk" -> ClocktowerNightAction.MonkProtect
        role == "Imp" -> ClocktowerNightAction.DemonKill
        role == "Butler" -> ClocktowerNightAction.ButlerMaster
        role == "Fortune Teller" -> ClocktowerNightAction.FortuneTeller
        role == "Chambermaid" -> ClocktowerNightAction.Chambermaid
        role == "Ravenkeeper" -> ClocktowerNightAction.Ravenkeeper
        else -> ClocktowerNightAction.None
    }
    val selected = draft?.seatSlots.orEmpty()
    val querySeats = selected.filterNotNull()
    val chambermaidSubjects = revalidateTwoPlayerSelection(
        selected.getOrNull(0), selected.getOrNull(1), aliveSeats(),
    ).let { listOfNotNull(it.first, it.second) }
    val question = when (role) {
        "Chef" -> ClocktowerInformationQuestion.Number(NumericMetric.ADJACENT_EVIL_PAIRS, allSeats().sorted())
        "Empath" -> ClocktowerInformationQuestion.Number(
            NumericMetric.LIVING_EVIL_NEIGHBOURS, actorSeat?.let(::livingNeighbourSeats).orEmpty(),
        )
        "Clockmaker" -> ClocktowerInformationQuestion.Number(NumericMetric.STEPS_TO_NEAREST_MINION, allSeats().sorted())
        "Chambermaid" -> chambermaidSubjects.takeIf { it.size == 2 && it.distinct().size == 2 }?.let {
            ClocktowerInformationQuestion.Number(NumericMetric.PLAYERS_WAKING_FOR_ABILITY, it)
        }
        "Fortune Teller" -> querySeats.takeIf { !redHerring && it.size == 2 && it.distinct().size == 2 }?.let {
            ClocktowerInformationQuestion.Boolean(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, it)
        }
        "Undertaker" -> actionTimeline.entries.asReversed().firstNotNullOfOrNull {
            (it.fact as? ActionFact.Execution)?.targetSeat
        }?.takeIf { gameState.playerAt(it) != null }?.let(ClocktowerInformationQuestion::Role)
        "Ravenkeeper" -> querySeats.firstOrNull()?.let(ClocktowerInformationQuestion::Role)
        else -> null
    }
    return ClocktowerNightStepFacts(
        current.id, action, role,
        isRealAction = actorSeat != null || redHerring || mayorRuling ||
            current.kind == ClocktowerHostInteractionKind.EVIL_INFORMATION,
        actorSeat = actorSeat,
        isEvilInformation = current.kind == ClocktowerHostInteractionKind.EVIL_INFORMATION,
        question = question,
    )
}

internal fun hostNightStep(
    state: ClocktowerSessionState,
    ruleset: ValidatedClocktowerRuleset,
    draft: ClocktowerNightStepDraft?,
): HostNightStep {
    val facts = state.currentNightStepFacts(ruleset, draft)
    val action = facts.action
    // Builders put Chef/Empath/Clockmaker metrics on the question; Chambermaid's appears only
    // once two targets are chosen, so its action identifies it before then.
    val metric = (facts.question as? ClocktowerInformationQuestion.Number)?.metric
        ?: NumericMetric.PLAYERS_WAKING_FOR_ABILITY.takeIf { action == ClocktowerNightAction.Chambermaid }
    val chambermaidSubjects = draft?.seatSlots.orEmpty().let { slots ->
        revalidateTwoPlayerSelection(slots.getOrNull(0), slots.getOrNull(1), state.aliveSeats())
    }.let { listOfNotNull(it.first, it.second) }
    // Chef/Empath truth and discretionary domains come from Session, never a display or AI narrative.
    val numericResolution = if (facts.isRealAction && metric != null &&
        (action == ClocktowerNightAction.None || action == ClocktowerNightAction.Chambermaid)
    ) facts.actorSeat?.let { seat ->
        when (metric) {
            NumericMetric.STEPS_TO_NEAREST_MINION, NumericMetric.PLAYERS_WAKING_FOR_ABILITY ->
                NumericInformationResolver.resolveLaterNight(state, ruleset, metric, seat, chambermaidSubjects)
            else -> NumericInformationResolver.resolve(state, metric, seat)
        }
    } else null
    val selectedSeat = when (action) {
        // The accepted Red Herring is a Session fact; its draft exists only on its own step.
        ClocktowerNightAction.RedHerring -> draft?.selectedSeats?.firstOrNull() ?: state.redHerringSeat
        in singleTargetActions -> draft?.selectedSeats?.firstOrNull()
        else -> null
    }
    val legalTargetSeats = when (action) {
        ClocktowerNightAction.Poison, ClocktowerNightAction.DemonKill -> state.aliveSeats()
        ClocktowerNightAction.ButlerMaster -> state.allSeats() - listOfNotNull(facts.actorSeat).toSet()
        ClocktowerNightAction.MonkProtect -> state.allSeats()
        else -> emptySet()
    }
    val succession = if (action == ClocktowerNightAction.DemonKill && facts.isRealAction) {
        state.impSelfKillSuccession(selectedSeat)
    } else DemonSuccessionResolution.None
    return HostNightStep(
        facts = facts,
        isPrivateInformation = facts.isRealAction && facts.actorSeat != null && facts.roleEnName != null &&
            action in privateInformationActions && facts.roleEnName != "Spy",
        isEvilInformation = facts.isEvilInformation,
        numericMetric = metric,
        numericResolution = numericResolution,
        booleanResolution = if (action == ClocktowerNightAction.FortuneTeller) facts.actorSeat?.let {
            FortuneTellerInformationResolver.resolve(state, it, draft?.selectedSeats.orEmpty())
        } else null,
        roleResolution = (facts.question as? ClocktowerInformationQuestion.Role)?.let { query ->
            facts.actorSeat?.let { RoleInformationResolver.resolve(state, ruleset, facts.roleEnName.orEmpty(), it, query.subjectSeat) }
        },
        pairRole = facts.roleEnName?.takeIf { facts.isRealAction && it in pairInformationRoles },
        selectedSeat = selectedSeat,
        legalTargetSeats = legalTargetSeats,
        targetReady = clocktowerLivePlayerTargetReady(action, facts.isRealAction, selectedSeat, legalTargetSeats) &&
            (action != ClocktowerNightAction.MayorRedirect || selectedSeat != null) &&
            (action != ClocktowerNightAction.RedHerring || !facts.isRealAction || selectedSeat != null),
        successorSeats = when (succession) {
            is DemonSuccessionResolution.Forced -> listOf(succession.targetSeat)
            is DemonSuccessionResolution.Choice -> succession.targetSeats.toList()
            DemonSuccessionResolution.None -> emptyList()
        },
    )
}
