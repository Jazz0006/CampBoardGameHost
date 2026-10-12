package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.PendingPairInformationDecision

/**
 * A validated whole-residual candidate becomes only a PRESELECTED current UI
 * option. The Host's original full manual domain remains available for overrides.
 * No ambiguous registration witness is silently committed by this adapter.
 */
internal object ClocktowerResidualDisplayMatcherV2 {
    private fun normalize(value: String): String =
        value.filter(Char::isLetterOrDigit).lowercase()

    private fun safe(option: ClocktowerDisplayOption): Boolean =
        option.spyRegistersGood == null &&
            option.spyRegisteredRoleEnName == null &&
            option.recluseRegistersEvil == null &&
            option.recluseRegisteredRoleEnName == null

    fun matchPair(
        decision: PendingPairInformationDecision,
        selectedCandidateId: String?,
        currentOptions: List<ClocktowerDisplayOption>,
    ): ClocktowerDisplayOption? {
        if (selectedCandidateId == null) return null
        val chosen = decision.legalCandidates.singleOrNull {
            it.candidateId == selectedCandidateId
        } ?: return null
        val role = chosen.outcome.shownRole?.value ?: return null
        val seats = chosen.outcome.candidateSeats.sorted()
        if (seats.size != 2) return null
        return currentOptions.filter(::safe).singleOrNull { option ->
            val statement = option.proposition as? InformationProposition.AnyOf
                ?: return@singleOrNull false
            val parts = statement.alternatives.filterIsInstance<InformationProposition.RoleAt>()
            parts.size == 2 &&
                parts.map { it.seat }.sorted() == seats &&
                parts.all { normalize(it.role.value) == normalize(role) }
        }
    }

    fun matchScalar(
        decision: PendingGlobalScalarInformationDecision,
        selectedCandidateId: String?,
        currentOptions: List<ClocktowerDisplayOption>,
    ): ClocktowerDisplayOption? {
        if (selectedCandidateId == null) return null
        val chosen = decision.legalCandidates.singleOrNull {
            it.candidateId == selectedCandidateId
        }?.payload as? StorytellerProviderCandidatePayloadV1.ScalarResult ?: return null
        return currentOptions.filter(::safe).singleOrNull { option ->
            when (val statement = option.proposition) {
                is InformationProposition.NumericResult ->
                    statement.sourceSeat == decision.sourceSeat &&
                        statement.metric.name == decision.metric &&
                        statement.subjectSeats == decision.subjectSeats &&
                        statement.value.toString() == chosen.value
                is InformationProposition.BooleanResult ->
                    statement.sourceSeat == decision.sourceSeat &&
                        statement.metric.name == decision.metric &&
                        statement.subjectSeats == decision.subjectSeats &&
                        statement.value.toString() == chosen.value
                is InformationProposition.RoleAt ->
                    decision.metric == "REVEALED_CHARACTER" &&
                        decision.subjectSeats == listOf(statement.seat) &&
                        statement.role.value == chosen.value
                else -> decision.kind ==
                    com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1.ROLE &&
                    statement == null &&
                    option.displayKind == ClocktowerDisplayKind.RoleReveal &&
                    option.presentationSubjectSeats == decision.subjectSeats &&
                    option.presentationRoleId == chosen.value
            }
        }
    }
}
