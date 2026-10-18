package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2

/**
 * Strict V2 public-semantic -> current Host UI manual-option adapter.
 * Return null on ambiguity, missing typed semantics or implicit registration.
 * An earlier plan NEVER authorizes publication: the current Host step still
 * owns confirmation, effective poison, revision, legality and player display.
 */
internal object ClocktowerJointOpeningDisplayMatcherV2 {
    fun match(
        plan: HostOpeningSemanticValueV2,
        currentSourceSeat: Int,
        currentOptions: List<ClocktowerDisplayOption>,
        question: ClocktowerInformationQuestion? = null,
    ): ClocktowerDisplayOption? {
        if (plan.sourceSeat != currentSourceSeat) return null
        val template = currentOptions.firstOrNull()
        val proposition = when (plan.observationKind) {
            "NUMBER" -> {
                val numericQuestion = question as? ClocktowerInformationQuestion.Number ?: return null
                val number = plan.number?.takeIf { it >= 0 } ?: return null
                InformationProposition.NumericResult(numericQuestion.metric, currentSourceSeat, numericQuestion.subjectSeats, number)
            }
            "ROLE" -> {
                if (plan.subjectMode != "EVIDENCE_PAIR" || plan.subjectSeats.size != 2) return null
                val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).singleOrNull {
                    normalize(it.enName) == normalize(plan.roleId.orEmpty())
                } ?: return null
                InformationProposition.AnyOf(plan.subjectSeats.sorted().map {
                    InformationProposition.RoleAt(it, com.codex.campboardgamehost.clocktower.domain.RoleId(role.enName))
                })
            }
            "ZERO_OUTSIDERS" -> InformationProposition.AllOf(
                clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
                    .filter { it.team == ClocktowerTeam.Outsider }.map {
                        InformationProposition.RoleInPlay(com.codex.campboardgamehost.clocktower.domain.RoleId(it.enName), false)
                    })
            else -> return null // Player query must first provide its actual subjects.
        }
        val value = when (proposition) {
            is InformationProposition.NumericResult -> proposition.value.toString()
            is InformationProposition.AnyOf -> plan.roleId
            else -> "No Outsiders"
        }
        return ClocktowerDisplayOption(
            label = value.orEmpty(),
            displayKind = if (proposition is InformationProposition.NumericResult) ClocktowerDisplayKind.Number
                else ClocktowerDisplayKind.EitherOne,
            displayTitle = template?.displayTitle ?: plan.abilityRoleId,
            displayPrimary = value,
            displaySecondary = plan.subjectSeats.takeIf { it.isNotEmpty() }?.joinToString(" / "),
            displayFooter = null, proposition = proposition,
            presentationSubjectSeats = plan.subjectSeats,
        )
    }

    private fun normalize(name: String): String =
        name.filter(Char::isLetterOrDigit).lowercase()

    /**
     * Fail closed when the real Session diverges from the opening advice.
     * This reads existing confirmed facts only; it does not introduce a parallel
     * plan state or require a role-specific invalidation callback.
     *
     * Opening choices that were planned and confirmed (bluffs, Red Herring) are
     * harmless. A later gameplay action or a different published clue forces
     * remaining current-step decisions to use a fresh strategic recommendation.
     */
    fun stillUsable(
        session: ClocktowerSessionState,
        plannedDrunkSeat: Int?,
        plannedInformation: Map<Int, HostOpeningSemanticValueV2>,
        plannedRedHerringSeat: Int?,
        plannedBluffRoleNames: List<String>,
    ): Boolean {
        val actualDrunkSeat = session.gameState.players.singleOrNull {
            it.actualRole.value == "Drunk"
        }?.seat
        if (actualDrunkSeat != plannedDrunkSeat) return false

        if (session.actionTimeline.entries.any { event ->
                when (val action = event.fact) {
                    is ActionFact.PhaseAdvance -> false
                    is ActionFact.RedHerringChoice ->
                        action.selectedSeat != plannedRedHerringSeat
                    is ActionFact.DemonBluffChoice ->
                        action.roles.map { it.value } != plannedBluffRoleNames
                    else -> true
                }
            }) return false

        return session.epistemicObservationLog.records.all { actual ->
            val planned = actual.sourceSeat?.let(plannedInformation::get)
                ?: return@all true
            if (actual.phase != StorytellerPhase.FIRST_NIGHT ||
                actual.visibility != ObservationVisibility.PRIVATE ||
                actual.round != 1
            ) return@all true
            actual.recipientSeats == setOf(planned.sourceSeat) &&
                normalize(actual.sourceAbility?.value.orEmpty()) ==
                    normalize(planned.abilityRoleId) &&
                compatible(planned, actual.proposition)
        }
    }

    private fun compatible(
        plan: HostOpeningSemanticValueV2,
        proposition: InformationProposition,
    ): Boolean = when (plan.observationKind) {
        "NUMBER" -> proposition is InformationProposition.NumericResult &&
            proposition.sourceSeat == plan.sourceSeat &&
            proposition.value == plan.number
        "BOOLEAN" -> proposition is InformationProposition.BooleanResult &&
            proposition.sourceSeat == plan.sourceSeat &&
            proposition.subjectSeats.sorted() == plan.subjectSeats.sorted() &&
            proposition.value == plan.boolean
        "ROLE" -> {
            val pair = proposition as? InformationProposition.AnyOf
            pair != null && plan.subjectMode == "EVIDENCE_PAIR" &&
                pair.alternatives.size == 2 &&
                pair.alternatives.all { it is InformationProposition.RoleAt } &&
                pair.alternatives.filterIsInstance<InformationProposition.RoleAt>()
                    .map { it.seat }.sorted() == plan.subjectSeats.sorted() &&
                pair.alternatives.filterIsInstance<InformationProposition.RoleAt>()
                    .all { normalize(it.role.value) == plan.roleId }
        }
        // A zero-Outsider statement requires its own typed semantic observation;
        // never pretend NONE or a UI label is reliable evidence of that result.
        else -> false
    }
}
