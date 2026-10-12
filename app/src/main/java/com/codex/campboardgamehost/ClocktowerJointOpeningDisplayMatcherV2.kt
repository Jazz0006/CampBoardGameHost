package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
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
    ): ClocktowerDisplayOption? {
        if (plan.sourceSeat != currentSourceSeat) return null
        val safeMatches = currentOptions.filter { candidate ->
            // Do not turn a possible registration explanation into a forced witness.
            candidate.spyRegistersGood == null &&
                candidate.spyRegisteredRoleEnName == null &&
                candidate.recluseRegistersEvil == null &&
                candidate.recluseRegisteredRoleEnName == null &&
                candidate.proposition?.let { proposition ->
                    compatible(plan, proposition)
                } == true
        }
        return safeMatches.singleOrNull()
    }

    private fun normalize(name: String): String =
        name.filter(Char::isLetterOrDigit).lowercase()

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
