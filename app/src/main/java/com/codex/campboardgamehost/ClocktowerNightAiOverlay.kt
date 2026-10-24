package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult

/** Current AI advice state for the night step; read-only. */
internal data class NightAiInputs(
    val assisted: Boolean,
    val automatic: Boolean,
    val adviceKey: String?,
    val adviceBusy: Boolean,
    val adviceError: String?,
    val residualChoices: Map<String, SemanticResult>,
    /** Verified joint-opening plan by seat; empty when it no longer fits Session. */
    val jointOpeningSemantics: Map<Int, HostOpeningSemanticValueV2>,
    val mayorPending: PendingMayorRedirectDecision?,
    val fortuneTellerSelectedNames: List<String>,
)

internal data class NightAiOverlay(
    /** Request key of this step's AI decision; null when the step has none (always null in MANUAL). */
    val currentKey: String? = null,
    val requestBlocked: Boolean = false,
    /** A current suggestion that fits [HostNightStep]'s Session query. */
    val validatedOption: ClocktowerDisplayOption? = null,
    val recommendedMayorSeat: Int? = null,
    val shouldRequest: Boolean = false,
) {
    /** What the card may highlight: never a blocked (busy/failed) request's advice. */
    val suggestedOption: ClocktowerDisplayOption? get() = validatedOption.takeUnless { requestBlocked }
}

/**
 * AI advice for the current night step. Pure and read-only: it never calls Session and can only
 * return an option inside [host]'s Session query. Cards and language feed only the existing AI
 * display matchers ([pendingGlobalFirstNightPairDecision], [ClocktowerResidualDisplayMatcherV2]).
 */
internal fun nightAiOverlay(
    host: HostNightStep,
    inputs: NightAiInputs,
    state: ClocktowerSessionState,
    ruleset: ValidatedClocktowerRuleset,
    step: ClocktowerNightStepUi,
    stepIndex: Int,
    phase: ClocktowerPhase,
    cards: List<PlayerCard>,
    language: String,
    scriptRoles: List<ClocktowerRole>,
): NightAiOverlay {
    if (!inputs.assisted) return NightAiOverlay()
    val revision = StorytellerDecisionRevision(state.gameStateRevision, state.playerInputRevision)
    val round = state.round
    val pairPending = if (!host.hostDeterminedInformation && phase == ClocktowerPhase.FirstNight) {
        pendingGlobalFirstNightPairDecision(step, phase, round, cards, state.gameState, state.gameId, revision)
    } else null
    val scalarPending = if (!host.hostDeterminedInformation &&
        phase in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) && pairPending == null
    ) pendingGlobalNightScalarDecision(
        step, phase, round, stepIndex, cards, state.gameId, revision, inputs.fortuneTellerSelectedNames,
    ) else null
    val mayorPending = inputs.mayorPending?.takeIf {
        phase == ClocktowerPhase.Night && host.facts.action == ClocktowerNightAction.MayorRedirect && host.facts.isRealAction
    }
    val identity = pairPending?.requestIdentity ?: scalarPending?.requestIdentity ?: mayorPending?.requestIdentity
    val currentKey = identity?.let {
        ClocktowerResidualRequestIdentityV2.build(
            state.gameId, round, stepIndex, it.requestId,
            state.gameStateRevision, state.playerInputRevision, state.nextTimelineGlobalSequence,
            if (inputs.automatic) StorytellerOperationMode.AI_AUTOMATIC else StorytellerOperationMode.AI_ASSISTED,
        )
    }
    val adviceIsCurrent = currentKey != null && inputs.adviceKey == currentKey
    // An opening information choice is NEVER a valid default for a later night,
    // even when the same role wakes again and the number matches.
    val residualOption = if (adviceIsCurrent) when {
        pairPending != null -> ClocktowerResidualDisplayMatcherV2.matchPair(
            pairPending, inputs.residualChoices[pairPending.requestIdentity.requestId], cards, language)
        scalarPending != null -> ClocktowerResidualDisplayMatcherV2.matchScalar(
            scalarPending, inputs.residualChoices[scalarPending.requestIdentity.requestId])
        else -> null
    } else null
    // Busy/error belong to their exact Host-current request; a stale failed or
    // in-flight earlier step must not block a new deterministic step.
    val blocked = clocktowerCurrentAiRequestBlocked(currentKey, inputs.adviceKey, inputs.adviceBusy, inputs.adviceError)
    // Match direct AI target advice to the current question, revision and canonical seats.
    val recommendedMayorSeat = mayorPending?.takeIf { adviceIsCurrent && !blocked }?.let { decision ->
        ClocktowerResidualDisplayMatcherV2.matchMayorRedirect(
            decision = decision,
            selected = inputs.residualChoices[decision.requestIdentity.requestId],
            currentRevision = revision,
            currentSeats = state.mayorRedirectEligibleSeats() +
                listOfNotNull(decision.mayorSeat.takeIf { it in state.allSeats() }),
        )
    }
    // MANUAL never consumes an AI opening plan; a failed current automatic request may not
    // silently reuse an old recommendation.
    val actorSeat = host.facts.actorSeat
    val proposedOption = if (inputs.automatic && blocked) null else
        residualOption ?: actorSeat?.takeIf { phase == ClocktowerPhase.FirstNight }?.let { seat ->
            inputs.jointOpeningSemantics[seat]?.let { planned ->
                ClocktowerJointOpeningDisplayMatcherV2.match(
                    question = host.facts.question,
                    plan = planned,
                    currentSourceSeat = seat,
                    currentOptions = step.manualInformationCandidates,
                )
            }
        }
    // Validate the suggestion and any retry against the same current Session query.
    val validatedOption = proposedOption?.takeIf { option ->
        val pairRole = host.pairRole
        when {
            host.roleResolution != null -> option.displayKind == ClocktowerDisplayKind.RoleReveal &&
                (option.proposition as? InformationProposition.RoleAt)?.let(host.roleResolution::accepts) == true
            pairRole != null -> actorSeat != null && clocktowerCurrentPairRecommendation(
                option = option,
                abilityRole = pairRole,
                seatCount = state.gameState.players.size,
                scriptRoles = scriptRoles,
                resolvePair = { first, second ->
                    PairInformationResolver.resolve(state, ruleset, RoleId(pairRole), actorSeat, first, second)
                },
            ) != null
            host.facts.action == ClocktowerNightAction.FortuneTeller -> {
                val boolean = option.proposition as? InformationProposition.BooleanResult
                option.displayKind == ClocktowerDisplayKind.YesNo && boolean != null &&
                    host.booleanResolution?.accepts(boolean) == true
            }
            host.numericMetric == null -> true
            else -> {
                val numeric = option.proposition as? InformationProposition.NumericResult
                val resolved = host.numericResolution
                numeric != null && resolved != null &&
                    option.displayKind == ClocktowerDisplayKind.Number &&
                    host.numericMetric == numeric.metric &&
                    actorSeat == numeric.sourceSeat &&
                    numeric.subjectSeats == resolved.subjectSeats &&
                    numeric.value in resolved.values
            }
        }
    }
    return NightAiOverlay(
        currentKey = currentKey,
        requestBlocked = blocked,
        validatedOption = validatedOption,
        recommendedMayorSeat = recommendedMayorSeat,
        shouldRequest = currentKey != null && !adviceIsCurrent && validatedOption == null &&
            !host.hostDeterminedInformation &&
            !(host.facts.action == ClocktowerNightAction.MayorRedirect && host.selectedSeat != null),
    )
}
