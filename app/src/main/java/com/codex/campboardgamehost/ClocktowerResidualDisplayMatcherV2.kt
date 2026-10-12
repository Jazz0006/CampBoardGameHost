package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
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

    /**
     * Whole-package automatic safety gate. A legal model choice must also have
     * exactly one witness-free Host presentation at its current real step.
     * This never publishes information and never infers a future player target.
     */
    fun allChoicesCanBePresented(
        requests: List<StorytellerProviderRequestV1>,
        choices: Map<String, String>,
        steps: List<ClocktowerNightStepUi>,
        cards: List<PlayerCard>,
    ): Boolean {
        if (requests.isEmpty() ||
            requests.map { it.identity.decisionId }.distinct().size != requests.size ||
            choices.keys != requests.map { it.identity.decisionId }.toSet()) return false
        return requests.all { request ->
            val id = choices[request.identity.decisionId] ?: return@all false
            val candidate = request.legalCandidates.singleOrNull { it.candidateId == id }
                ?: return@all false
            val context = request.decisionContext
            val actorSeat = when (context) {
                is StorytellerProviderDecisionContextV1.ScalarInformation -> context.sourceSeat
                is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> context.sourceSeat
                else -> return@all false // no supported automatic Host presentation
            }
            val abilityRole = when (context) {
                is StorytellerProviderDecisionContextV1.ScalarInformation -> context.abilityRole.value
                is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> context.abilityRole.value
                else -> return@all false
            }
            val actorName = cards.getOrNull(actorSeat - 1)?.name ?: return@all false
            steps.count { step ->
                if (!step.isRealAction || step.actor?.name != actorName ||
                    step.roleEnName != abilityRole) return@count false
                val options = step.manualInformationCandidates.ifEmpty {
                    step.automaticInformationCandidates
                }
                options.filter(::safe).count { option ->
                    when (val payload = candidate.payload) {
                        is StorytellerProviderCandidatePayloadV1.ScalarResult -> {
                            if (context !is StorytellerProviderDecisionContextV1.ScalarInformation) {
                                false
                            } else when (val proposition = option.proposition) {
                                is InformationProposition.NumericResult ->
                                    proposition.sourceSeat == context.sourceSeat &&
                                        proposition.metric.name == context.metric &&
                                        proposition.subjectSeats == context.subjectSeats &&
                                        proposition.value.toString() == payload.value
                                is InformationProposition.BooleanResult ->
                                    proposition.sourceSeat == context.sourceSeat &&
                                        proposition.metric.name == context.metric &&
                                        proposition.subjectSeats == context.subjectSeats &&
                                        proposition.value.toString() == payload.value
                                is InformationProposition.RoleAt ->
                                    context.metric == "REVEALED_CHARACTER" &&
                                        context.subjectSeats == listOf(proposition.seat) &&
                                        proposition.role.value == payload.value
                                else ->
                                    context.metric == "REVEALED_CHARACTER" &&
                                        option.displayKind == ClocktowerDisplayKind.RoleReveal &&
                                        option.presentationSubjectSeats == context.subjectSeats &&
                                        option.presentationRoleId == payload.value
                            }
                        }
                        is StorytellerProviderCandidatePayloadV1.PairInformation -> {
                            if (context !is StorytellerProviderDecisionContextV1.FirstNightPairInformation) {
                                false
                            } else {
                                val proposition = option.proposition as? InformationProposition.AnyOf
                                val parts = proposition?.alternatives
                                    ?.filterIsInstance<InformationProposition.RoleAt>().orEmpty()
                                parts.size == 2 &&
                                    parts.map { it.seat }.sorted() == payload.candidateSeats.sorted() &&
                                    payload.shownRoleId != null &&
                                    parts.all { normalize(it.role.value) ==
                                        normalize(payload.shownRoleId) }
                            }
                        }
                        else -> false
                    }
                } == 1
            } == 1
        }
    }

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
