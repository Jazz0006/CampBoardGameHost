package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.session.PendingPairInformationDecision
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/** Direct semantic advice becomes a UI preselection; only confirmation records a fact. */
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
        selected: SemanticResult,
        publication: EpistemicObservationDraft,
    ): Boolean {
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
        return when (selected.kind) {
            "SCALAR_RESULT" -> {
                if (context !is StorytellerProviderDecisionContextV1.ScalarInformation) false
                else when (val actual = publication.proposition) {
                    is InformationProposition.NumericResult ->
                        context.metric == actual.metric.name &&
                            context.subjectSeats == actual.subjectSeats &&
                            actual.sourceSeat == seat &&
                            actual.value.toString() == selected.scalarValue
                    is InformationProposition.BooleanResult ->
                        context.metric == actual.metric.name &&
                            context.subjectSeats == actual.subjectSeats &&
                            actual.sourceSeat == seat &&
                            actual.value.toString() == selected.scalarValue
                    is InformationProposition.RoleAt ->
                        context.metric == "REVEALED_CHARACTER" &&
                            context.subjectSeats == listOf(actual.seat) &&
                            normalize(actual.role.value) == normalize(selected.scalarValue.orEmpty())
                    else -> false
                }
            }
            "PAIR_RESULT" -> {
                if (context !is StorytellerProviderDecisionContextV1.FirstNightPairInformation) false
                else if (selected.pairRoleId == null) {
                    val actual = publication.proposition as? InformationProposition.AllOf
                    val team = when (context.abilityRole.value) {
                        "Washerwoman" -> ClocktowerTeam.Townsfolk
                        "Investigator" -> ClocktowerTeam.Minion
                        else -> ClocktowerTeam.Outsider
                    }
                    val roles = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
                        .filter { it.team == team }.map { normalize(it.enName) }.toSet()
                    val statements = actual?.propositions?.filterIsInstance<InformationProposition.RoleInPlay>().orEmpty()
                    actual != null && statements.size == actual.propositions.size &&
                        statements.all { !it.inPlay } && statements.map { normalize(it.role.value) }.toSet() == roles
                } else {
                    val actual = publication.proposition as? InformationProposition.AnyOf
                    val parts = actual?.alternatives?.filterIsInstance<InformationProposition.RoleAt>()
                        .orEmpty()
                    actual?.alternatives?.size == 2 && parts.size == 2 &&
                        parts.map { it.seat }.sorted() == selected.pairSeats.sorted() &&
                        parts.all { normalize(it.role.value) == normalize(selected.pairRoleId) }
                }
            }
            else -> false
        }
    }

    /** A response must address one actual current wake; no menu membership or truth check. */
    fun allChoicesCanBePresented(
        requests: List<StorytellerProviderRequestV1>,
        choices: Map<String, SemanticResult>,
        steps: List<ClocktowerNightStepUi>,
        cards: List<PlayerCard>,
    ): Boolean {
        if (requests.isEmpty() || requests.map { it.identity.decisionId }.distinct().size != requests.size ||
            choices.keys != requests.map { it.identity.decisionId }.toSet()) return false
        return requests.all { request ->
            val result = choices[request.identity.decisionId] ?: return@all false
            if (runCatching { result.validate(request.decisionContext, cards.size) }.isFailure) return@all false
            val context = request.decisionContext
            val seat = when (context) {
                is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> context.sourceSeat
                is StorytellerProviderDecisionContextV1.ScalarInformation -> context.sourceSeat
                is StorytellerProviderDecisionContextV1.MayorRedirect -> return@all steps.count {
                    it.isRealAction && it.action == ClocktowerNightAction.MayorRedirect
                } == 1
                else -> return@all false
            }
            val role = when (context) {
                is StorytellerProviderDecisionContextV1.FirstNightPairInformation -> context.abilityRole.value
                is StorytellerProviderDecisionContextV1.ScalarInformation -> context.abilityRole.value
                else -> return@all false
            }
            steps.count { it.isRealAction && it.actor?.name == cards.getOrNull(seat - 1)?.name &&
                it.roleEnName == role } == 1
        }
    }

    fun matchMayorRedirect(
        decision: PendingMayorRedirectDecision,
        selected: SemanticResult?,
        currentRevision: StorytellerDecisionRevision,
        currentSeats: Set<Int>,
    ): Int? {
        if (selected?.kind != "SEAT_TARGET") return null
        if (decision.revision != currentRevision) return null
        return selected.seat?.takeIf { it in currentSeats }
    }

    /** Materialize exactly the model's structured result; never choose a Host alternative. */
    fun matchPair(
        decision: PendingPairInformationDecision,
        selected: SemanticResult?,
        cards: List<PlayerCard>,
        language: String,
    ): ClocktowerDisplayOption? {
        if (selected?.kind != "PAIR_RESULT") return null
        val context = StorytellerProviderDecisionContextV1.FirstNightPairInformation(
            decision.sourceSeat, decision.abilityRole, decision.reliability)
        if (runCatching { selected.validate(context, cards.size) }.isFailure) return null
        val role = selected.pairRoleId?.let { id ->
            clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).singleOrNull {
                normalize(it.enName) == normalize(id)
            } ?: return null
        }
        if (role == null) {
            val team = when (decision.abilityRole.value) {
                "Washerwoman" -> ClocktowerTeam.Townsfolk
                "Investigator" -> ClocktowerTeam.Minion
                else -> ClocktowerTeam.Outsider
            }
            val text = when (team) {
                ClocktowerTeam.Townsfolk -> if (language == "en") "No Townsfolk" else "没有镇民"
                ClocktowerTeam.Minion -> if (language == "en") "No Minions" else "没有爪牙"
                else -> if (language == "en") "No Outsiders" else "没有外来者"
            }
            return ClocktowerDisplayOption(text, ClocktowerDisplayKind.EitherOne,
                decision.abilityRole.value, text, null, null,
                proposition = InformationProposition.AllOf(
                    clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).filter { it.team == team }
                        .map { InformationProposition.RoleInPlay(RoleId(it.enName), false) }))
        }
        val seats = selected.pairSeats.sorted()
        val shown = role.nameFor(language)
        val names = seats.map { cards[it - 1].name }
        return ClocktowerDisplayOption(
            label = "$shown: ${names.joinToString(" / ")}",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = role.nameFor(language), displayPrimary = shown,
            displaySecondary = names.joinToString(" / "), displayFooter = null,
            proposition = InformationProposition.AnyOf(seats.map {
                InformationProposition.RoleAt(it, RoleId(role.enName))
            }), presentationSubjectSeats = seats, presentationRoleId = role.enName,
        )
    }

    fun matchScalar(
        decision: PendingGlobalScalarInformationDecision,
        selected: SemanticResult?,
        language: String = "en",
    ): ClocktowerDisplayOption? {
        if (selected?.kind != "SCALAR_RESULT") return null
        val value = selected.scalarValue ?: return null
        // Roster and response references were checked at decode/adoption; this only renders.
        val proposition = when (decision.kind) {
            StorytellerProviderScalarKindV1.NUMBER -> InformationProposition.NumericResult(
                NumericMetric.valueOf(decision.metric), decision.sourceSeat,
                decision.subjectSeats, value.toIntOrNull()?.takeIf { it >= 0 } ?: return null)
            StorytellerProviderScalarKindV1.BOOLEAN -> InformationProposition.BooleanResult(
                BooleanMetric.valueOf(decision.metric), decision.sourceSeat,
                decision.subjectSeats, value.toBooleanStrictOrNull() ?: return null)
            StorytellerProviderScalarKindV1.ROLE -> {
                val role = clocktowerRolesForScript(ClocktowerScript.TroubleBrewing).singleOrNull {
                    normalize(it.enName) == normalize(value)
                } ?: return null
                InformationProposition.RoleAt(decision.subjectSeats.single(), RoleId(role.enName))
            }
        }
        val kind = when (decision.kind) {
            StorytellerProviderScalarKindV1.NUMBER -> ClocktowerDisplayKind.Number
            StorytellerProviderScalarKindV1.BOOLEAN -> ClocktowerDisplayKind.YesNo
            StorytellerProviderScalarKindV1.ROLE -> ClocktowerDisplayKind.RoleReveal
        }
        val displayValue = when (decision.kind) {
            StorytellerProviderScalarKindV1.BOOLEAN -> if (value == "true") {
                if (language == "en") "Yes" else "有"
            } else if (language == "en") "No" else "没有"
            StorytellerProviderScalarKindV1.ROLE -> clocktowerRolesForScript(ClocktowerScript.TroubleBrewing)
                .single { normalize(it.enName) == normalize(value) }.nameFor(language)
            else -> value
        }
        return ClocktowerDisplayOption(
            label = displayValue, displayKind = kind, displayTitle = decision.abilityRole.value,
            displayPrimary = displayValue, displaySecondary = null, displayFooter = null,
            proposition = proposition, presentationSubjectSeats = decision.subjectSeats,
            presentationRoleId = (proposition as? InformationProposition.RoleAt)?.role?.value,
        )
    }
}
