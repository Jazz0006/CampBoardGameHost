package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.session.PendingPairInformationDecision
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.MayorRedirectDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/**
 * A validated whole-residual candidate becomes only a PRESELECTED current UI
 * option. The Host's original full manual domain remains available for overrides.
 * No ambiguous registration witness is silently committed by this adapter.
 */
internal object ClocktowerResidualDisplayMatcherV2 {
    private fun normalize(value: String): String =
        value.filter(Char::isLetterOrDigit).lowercase()

    /**
     * An API choice can update strategic memory only after the player actually
     * receives that exact semantic answer. This checks the same Host-owned
     * decision context as the live display matcher, not a UI label or model ID.
     * Registration witnesses and non-information actions cannot pass.
     */
    fun matchesConfirmedPublication(
        request: StorytellerProviderRequestV1,
        selectedCandidateId: String,
        publication: EpistemicObservationDraft,
    ): Boolean {
        val candidate = request.legalCandidates.singleOrNull {
            it.candidateId == selectedCandidateId
        } ?: return false
        val context = request.decisionContext
        val seat = when (context) {
            is StorytellerProviderDecisionContextV1.ScalarInformation -> context.sourceSeat
            is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> context.sourceSeat
            else -> return false
        }
        val role = when (context) {
            is StorytellerProviderDecisionContextV1.ScalarInformation -> context.abilityRole.value
            is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> context.abilityRole.value
            else -> return false
        }
        if (publication.sourceSeat != seat ||
            publication.sourceAbility?.value != role ||
            publication.visibility != ObservationVisibility.PRIVATE ||
            publication.recipientSeats != setOf(seat)
        ) return false
        return when (val payload = candidate.payload) {
            is StorytellerProviderCandidatePayloadV1.ScalarResult -> {
                if (context !is StorytellerProviderDecisionContextV1.ScalarInformation) false
                else when (val actual = publication.proposition) {
                    is InformationProposition.NumericResult ->
                        context.metric == actual.metric.name &&
                            context.subjectSeats == actual.subjectSeats &&
                            actual.sourceSeat == seat &&
                            actual.value.toString() == payload.value
                    is InformationProposition.BooleanResult ->
                        context.metric == actual.metric.name &&
                            context.subjectSeats == actual.subjectSeats &&
                            actual.sourceSeat == seat &&
                            actual.value.toString() == payload.value
                    is InformationProposition.RoleAt ->
                        context.metric == "REVEALED_CHARACTER" &&
                            context.subjectSeats == listOf(actual.seat) &&
                            normalize(actual.role.value) == normalize(payload.value)
                    else -> false
                }
            }
            is StorytellerProviderCandidatePayloadV1.PairInformation -> {
                if (context !is StorytellerProviderDecisionContextV1.FirstNightPairInformation ||
                    payload.shownRoleId == null) false
                else {
                    val actual = publication.proposition as? InformationProposition.AnyOf
                    val parts = actual?.alternatives?.filterIsInstance<InformationProposition.RoleAt>()
                        .orEmpty()
                    actual?.alternatives?.size == 2 && parts.size == 2 &&
                        parts.map { it.seat }.sorted() == payload.candidateSeats.sorted() &&
                        parts.all { normalize(it.role.value) == normalize(payload.shownRoleId) }
                }
            }
            else -> false
        }
    }

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
            if (context is StorytellerProviderDecisionContextV1.MayorRedirect) {
                // A SeatTarget is a Host ruling, NOT an information reveal.
                // The provider candidate is already from the canonical pending
                // Mayor domain; the live pending boundary validates it again
                // just before application/commit.
                val seat = (candidate.payload as?
                    StorytellerProviderCandidatePayloadV1.SeatTarget)?.seat
                    ?: return@all false
                return@all seat in 1..cards.size &&
                    context.mayorSeat in 1..cards.size &&
                    steps.count {
                        it.isRealAction && it.action == ClocktowerNightAction.MayorRedirect
                    } == 1
            }
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

    /**
     * Bind the current AI SeatTarget to the SAME live Mayor Host decision.
     * No candidate ID is parsed, no default seat is chosen, and a stale
     * revision or changed live seat eligibility fails closed.
     */
    fun matchMayorRedirect(
        decision: PendingMayorRedirectDecision,
        selectedCandidateId: String?,
        currentRevision: StorytellerDecisionRevision,
        currentLegalSeats: Set<Int>,
    ): Int? {
        val candidateId = selectedCandidateId ?: return null
        val confirmation = decision.confirm(candidateId, currentRevision)
            as? MayorRedirectDecisionConfirmation.Confirmed ?: return null
        return confirmation.targetSeat.takeIf { it in currentLegalSeats }
    }

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
