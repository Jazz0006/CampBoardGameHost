package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderGameContextBuilderV1
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1
import com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicChangeCheckpointV2
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2
import com.codex.campboardgamehost.clocktower.session.StorytellerResidualPackageGateV2
import com.codex.campboardgamehost.clocktower.session.HostCurrentOpenDecisionInventoryV2
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticAcceptanceV2
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import kotlinx.coroutines.launch

/**
 * One unconfirmed current-step AI suggestion. It is neither a Session fact
 * nor a second history. The payload/receipt exist only to validate actual
 * player publication and promote advisory strategy at that moment.
 */
internal data class PendingCurrentStepAdvice(
    val recommendation: StorytellerResidualPackageGateV2.Accepted,
    val requestKey: String,
    val request: StorytellerProviderRequestV1,
    val proposal: StorytellerStrategicContinuationV2.Result,
    val receiptId: String,
    val model: String,
)

/** Advisory AI memory, transport and unconfirmed advice; owns no Session or game facts. */
internal class ClocktowerAiConversation {
    var canonicalConversationResponseId by mutableStateOf<String?>(null)
    var canonicalConversationModel by mutableStateOf<String?>(null)
    var openingPlanRecommendation by mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null)
    var strategicPolicyCursor by mutableStateOf<StorytellerStrategicContinuationV2.MemoryCursor?>(null)
    var strategicLastReviewedHost by mutableStateOf<StorytellerStrategicChangeCheckpointV2.Baseline?>(null)
    var pendingCurrentStepAdvice by mutableStateOf<PendingCurrentStepAdvice?>(null)
    var openingRecommendedInformation by mutableStateOf<Map<Int,
        com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2>>(emptyMap())
    var openingRecommendedBluffRoleNames by mutableStateOf<List<String>>(emptyList())
    var openingRecommendedRedHerringSeat by mutableStateOf<Int?>(null)
    var liveAiAdviceKey by mutableStateOf<String?>(null)
    var liveAiAdviceText by mutableStateOf<String?>(null)
    var liveAiAdviceBusy by mutableStateOf(false)
    var liveAiAdviceError by mutableStateOf<String?>(null)

    fun reviewConfirmedCurrentStepAdvice(
        session: ClocktowerGameSession,
        currentMode: StorytellerOperationMode,
        before: ClocktowerSessionState,
        draft: EpistemicObservationDraft,
    ) {
        val pending = pendingCurrentStepAdvice ?: return
        pendingCurrentStepAdvice = null
        if (currentMode == StorytellerOperationMode.MANUAL ||
            before.gameId != pending.recommendation.gameId ||
            before.gameStateRevision != pending.recommendation.gameStateRevision ||
            before.playerInputRevision != pending.recommendation.playerInputRevision ||
            before.nextTimelineGlobalSequence != pending.recommendation.exclusiveGlobalSequence ||
            pending.requestKey != liveAiAdviceKey
        ) return
        val selectedId = pending.recommendation.choices[
            pending.request.identity.decisionId] ?: return
        val published = session.state.epistemicObservationLog.records.lastOrNull {
            it.recordId == draft.recordId
        } ?: return
        if (!draft.matches(published) ||
            !ClocktowerResidualDisplayMatcherV2.matchesConfirmedPublication(
                pending.request, selectedId, draft)
        ) {
            // Human overrides remain confirmed facts, not approved AI policy.
            DebugFlightRecorder.record("RESIDUAL_CONFIRMED_DIFFERENT_RESULT")
            return
        }
        val prior = strategicPolicyCursor ?: return
        val promoted = runCatching {
            prior.afterConfirmedPublication(pending.proposal, before, session.state)
        }.getOrNull() ?: return
        strategicPolicyCursor = promoted
        strategicLastReviewedHost = StorytellerStrategicChangeCheckpointV2.Baseline(
            before, StorytellerStrategicChangeCheckpointV2.Position(draft.phase, draft.round))
        canonicalConversationResponseId = pending.receiptId
        canonicalConversationModel = pending.model
        DebugFlightRecorder.record("RESIDUAL_CONFIRMED_PROMOTED")
    }

    fun requestLiveGlobalAdvice(
        steps: List<ClocktowerNightStepUi>, stepIndex: Int,
        aiSettings: StorytellerAiSettings,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        currentSession: () -> ClocktowerGameSession?,
        currentMode: () -> StorytellerOperationMode,
        isJudgeVisible: () -> Boolean,
        cards: List<PlayerCard>, clocktowerRulesetRef: RulesetRef?,
        fortuneTellerSelectedNames: List<String>,
        mayorPending: PendingMayorRedirectDecision?,
        drunkAiScope: kotlinx.coroutines.CoroutineScope,
        language: String = "en",
    ) {
        val currentClocktowerScript = currentSession()?.state?.gameState?.script?.let { id ->
            ClocktowerScript.entries.single { it.toRecommendationScriptId() == id }
        }
        val clocktowerPhase = currentSession()?.state?.phase?.toClocktowerPhase() ?: ClocktowerPhase.FirstNight
        val round = currentSession()?.state?.round ?: 1
        val clocktowerRedHerring = currentSession()?.state?.redHerringSeat

        val step = steps.getOrNull(stepIndex) ?: return
        if (currentMode() == StorytellerOperationMode.MANUAL ||
            !isJudgeVisible() ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerPhase !in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night)
        ) return
        val session = currentSession() ?: return
        val requestedOperationMode = currentMode()
        val requestedNightRuleset = activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript)
        val pendingPair = pendingGlobalFirstNightPairDecision(
            step, clocktowerPhase, round, cards, session.state.gameState, session.state.gameId,
            StorytellerDecisionRevision(session.state.gameStateRevision, session.state.playerInputRevision),
        )
        val pendingScalar = if (clocktowerPhase in setOf(
                ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
            pendingPair == null
        ) pendingGlobalNightScalarDecision(
            step, clocktowerPhase, round, stepIndex, cards, session.state.gameId,
            StorytellerDecisionRevision(
                session.state.gameStateRevision, session.state.playerInputRevision,
            ),
            fortuneTellerSelectedNames,
            canonicalGameState = session.state.gameState,
        ) else null
        // The Host supplies a currently legal decision. The LLM uses the same
        // global strategy for pair, numeric/Boolean and later-night target decisions.
        val pendingTarget = if (clocktowerPhase == ClocktowerPhase.Night &&
            step.action == ClocktowerNightAction.MayorRedirect && step.isRealAction
        ) mayorPending
        else null
        val identity = pendingPair?.requestIdentity ?: pendingScalar?.requestIdentity
            ?: pendingTarget?.requestIdentity ?: return
        val revision = StorytellerProviderRevisionV1(
            session.state.gameStateRevision, session.state.playerInputRevision,
        )
        val key = ClocktowerResidualRequestIdentityV2.build(
            session.state.gameId, round, stepIndex, identity.requestId,
            revision.gameStateRevision, revision.playerInputRevision,
            session.state.nextTimelineGlobalSequence, requestedOperationMode,
        )
        // Only coalesce an in-flight request. An explicit retry must remain possible
        // after a failed/empty review; old explanatory text is not a result cache.
        if (liveAiAdviceKey == key && liveAiAdviceBusy) return
        liveAiAdviceKey = key
        pendingCurrentStepAdvice = null
        liveAiAdviceText = null
        liveAiAdviceError = null
        // An older in-flight response belongs to a different key and cannot own this state.
        liveAiAdviceBusy = false
        if (!aiSettings.endpoint.startsWith("https://") ||
            aiSettings.token.isBlank()
        ) {
            liveAiAdviceError = "AI unavailable: configure the selected AI connection."
            return
        }
        val providerRequest = runCatching {
            val snapshot = run {
                val rulesetRef = requireNotNull(clocktowerRulesetRef)
                val registry = activeGameClocktowerRulesetCatalog
                    .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                TroubleBrewingGameSnapshotProjector.fromRuntime(
                    gameSnapshot = session.toGameSnapshot(rulesetRef),
                    phase = if (clocktowerPhase == ClocktowerPhase.FirstNight) {
                        StorytellerPhase.FIRST_NIGHT
                    } else StorytellerPhase.NIGHT,
                    round = round,
                    characterRegistry = registry,
                )
            }
            val gameContext = StorytellerProviderGameContextBuilderV1.build(
                snapshot, revision, session.state,
            )
            if (pendingPair != null) {
                StorytellerProviderRequestFactoryV1.fromPairInformation(
                    decision = pendingPair, snapshot = snapshot, gameContext = gameContext,
                )
            } else if (pendingScalar != null) {
                StorytellerProviderRequestFactoryV1.fromScalarInformation(
                    pendingScalar.requestIdentity, pendingScalar.revision,
                    pendingScalar.sourceSeat, pendingScalar.abilityRole, pendingScalar.reliability,
                    pendingScalar.kind, pendingScalar.metric, pendingScalar.subjectSeats,
                    snapshot, gameContext,
                )
            } else {
                StorytellerProviderRequestFactoryV1.fromMayorRedirect(
                    decision = requireNotNull(pendingTarget),
                    snapshot = snapshot, gameContext = gameContext,
                )
            }
        }.getOrElse {
            liveAiAdviceError = "Current game history is not available for live strategy."
            return
        }
        // 3E3: only the one accepted full-game opening may seed dynamic policy.
        // No single-step recommender is permitted to masquerade as a joint plan.
        val wholeOpening = openingPlanRecommendation
        val wholeCursor = strategicPolicyCursor
        val wholeBaseline = strategicLastReviewedHost
        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC &&
            (!aiSettings.directEnabled || wholeOpening == null ||
                wholeCursor == null || wholeBaseline == null)
        ) {
            liveAiAdviceError = "Whole-game strategy is not available; automatic AI paused. Use Host manual decisions."
            DebugFlightRecorder.record("RESIDUAL_AUTO_NO_WHOLE_CONTEXT")
            return
        }
        if (aiSettings.directEnabled && wholeOpening != null &&
            wholeCursor != null && wholeBaseline != null
        ) {
            val phasePosition = StorytellerStrategicChangeCheckpointV2.Position(
                clocktowerPhase.toStorytellerPhase(), round)
            val currentHost = StorytellerStrategicChangeCheckpointV2.capture(session, phasePosition)
            val scopeAttempt = runCatching {
                val snapshot = (providerRequest.state as
                    com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1.TroubleBrewing).snapshot
                ClocktowerCurrentResidualScopeV2.collect(
                    state = session.state, phase = phasePosition.phase, round = round,
                    steps = steps, startIndex = stepIndex, cards = cards,
                    snapshot = snapshot, gameContext = providerRequest.gameContext,
                    fortuneTellerSelectedNames = fortuneTellerSelectedNames,
                    mayorPending = pendingTarget,
                    redHerringAlreadyChosen = clocktowerRedHerring != null,
                )
            }
            val remainingScope = scopeAttempt.getOrElse { error ->
                DebugFlightRecorder.record("RESIDUAL_SCOPE_REJECTED", mapOf(
                    "reason" to PersonalDirectOpenAiV1.safeFailure(error)))
                liveAiAdviceError = "Cannot form current Host residual scope; choose manually."
                return
            }
            val legalOpen = runCatching {
                HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
                    session.state, phasePosition.phase, round, remainingScope.requests.take(1))
            }.getOrElse {
                liveAiAdviceError = "Host rejected pending residual decisions."
                return
            }
            val confirmedDrunkSeat = session.state.gameState.players.singleOrNull {
                it.actualRole.value == "Drunk"
            }?.seat
            val openingPlanConflictsWithHost =
                wholeCursor.version == 0 &&
                    phasePosition.phase == StorytellerPhase.FIRST_NIGHT &&
                    wholeOpening.selection.drunkSeat != confirmedDrunkSeat
            val review = runCatching {
                StorytellerStrategicChangeCheckpointV2.review(
                    wholeBaseline, currentHost, wholeCursor, legalOpen,
                    advisoryPlanContradictsConfirmedState = openingPlanConflictsWithHost,
                    currentPlayerQuerySupplied = pendingScalar != null && step.action in setOf(
                        ClocktowerNightAction.FortuneTeller,
                        ClocktowerNightAction.Chambermaid,
                        ClocktowerNightAction.Ravenkeeper,
                    ))
            }.getOrElse {
                liveAiAdviceError = "Host chronology changed; cannot continue older strategy."
                return
            }
            if (!review.needsReview) {
                DebugFlightRecorder.record("RESIDUAL_MODEL_SKIP", mapOf(
                    "readiness" to review.readiness.name,
                    "barrier" to (remainingScope.barrierReason ?: "NONE")))
                BotcAiWireTrace.residualGate("SKIPPED", legalOpen.size,
                    wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                // A still-valid opening recommendation may cover the unchanged state.
                liveAiAdviceText = if (review.readiness ==
                    StorytellerStrategicChangeCheckpointV2.Readiness.NO_MATERIAL_CHANGE
                ) "沿用首次整局配置；尚无新的重要 Host 变化。" else
                    "当前没有可联合更新的合法未执行决定；后续行动发生后再评估。"
                return
            }
            // The model returns the current semantic decision only; later
            // still-unexecuted steps remain dependent on real Host actions.
            liveAiAdviceBusy = true
            DebugFlightRecorder.record("RESIDUAL_WHOLE_REQUEST", mapOf(
                "openDecisions" to legalOpen.size.toString(),
                "barrier" to (remainingScope.barrierReason ?: "NONE"),
                "memoryVersion" to wholeCursor.version.toString()))
            BotcAiWireTrace.residualGate("REQUEST", legalOpen.size,
                wholeCursor.version, phasePosition.phase.name, phasePosition.round)
            // This is only a provider context hint; the Host frozen snapshot
            // remains complete and authoritative even when a chain is usable.
            val requestedModel = requireNotNull(aiSettings.model)
            val requestedToken = aiSettings.token
            val previousResponseId = canonicalConversationResponseId?.takeIf {
                canonicalConversationModel == requestedModel
            }
            drunkAiScope.launch {
                val result = try {
                    // Coalesce related Host updates before paying for a new request.
                    delay(450)
                    if (liveAiAdviceKey != key || currentSession() !== session ||
                        session.state !== currentHost.state ||
                        strategicPolicyCursor != wholeCursor ||
                        currentMode() != requestedOperationMode ||
                        aiSettings.model != requestedModel || aiSettings.token != requestedToken ||
                        session.state.nextNightInteraction(requestedNightRuleset)?.id != step.interactionId) return@launch
                    runCatching {
                        StorytellerStrategicContinuationV2.recommendWithReceipt(
                            PersonalDirectOpenAiV1.ENDPOINT, requestedToken, requestedModel,
                            session, wholeOpening, legalOpen, wholeCursor, previousResponseId,
                        )
                    }
                } finally {
                    // Credential edits can invalidate this request without changing its Host key.
                    // Release that request on every exit, never a newer request's busy state.
                    if (liveAiAdviceKey == key) liveAiAdviceBusy = false
                }
                if (liveAiAdviceKey != key || currentSession() !== session ||
                    session.state !== currentHost.state ||
                    session.state.nextNightInteraction(requestedNightRuleset)?.id != step.interactionId ||
                    clocktowerPhase.toStorytellerPhase() != phasePosition.phase ||
                    round != phasePosition.round ||
                    strategicPolicyCursor != wholeCursor ||
                    currentMode() != requestedOperationMode ||
                    aiSettings.model != requestedModel || aiSettings.token != requestedToken
                ) return@launch
                result.onSuccess { receipt ->
                    val proposal = receipt.proposal
                    runCatching {
                        val verified = StorytellerResidualPackageGateV2.verify(
                            session.state, phasePosition.phase, phasePosition.round,
                            legalOpen, remainingScope.requests.take(1), proposal, wholeCursor)
                        // Validate the proposed memory transition, but do not install
                        // it or its provider response anchor before a real Host step.
                        StorytellerStrategicChangeCheckpointV2.validateProposal(
                            review, currentHost, proposal, wholeCursor)
                        verified
                    }.onSuccess acceptedResidual@{ verified ->
                        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC &&
                            !ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
                                remainingScope.requests.take(1), verified.choices, steps, cards)
                        ) {
                            // Never turn an unpresentable recommendation into game truth.
                            pendingCurrentStepAdvice = null
                            liveAiAdviceError = "Current Host decision cannot be presented automatically; choose manually."
                            DebugFlightRecorder.record("RESIDUAL_AUTO_UNPRESENTABLE")
                            BotcAiWireTrace.residualGate("AUTO_PAUSED", verified.choices.size,
                                wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                            return@acceptedResidual
                        }
                        // One current-step preselection; the Host re-matches it against
                        // the live legal display domain before player publication.
                        pendingCurrentStepAdvice = PendingCurrentStepAdvice(
                            recommendation = verified,
                            requestKey = key,
                            request = remainingScope.requests.first(),
                            proposal = proposal,
                            receiptId = receipt.responseId,
                            model = requestedModel,
                        )
                        BotcAiWireTrace.residualGate("PROPOSAL_ONLY", verified.choices.size,
                            wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                        liveAiAdviceText = buildString {
                            append(proposal.strategicDirection)
                            if (proposal.revisions.isNotEmpty()) {
                                append("\n\n策略调整：")
                                proposal.revisions.forEach { revision ->
                                    append("\n")
                                    append(revision.reason)
                                }
                            }
                        }
                        liveAiAdviceError = null
                        DebugFlightRecorder.record("RESIDUAL_CURRENT_RECOMMENDED", mapOf(
                            "choiceCount" to verified.choices.size.toString(),
                            "memoryVersion" to wholeCursor.version.toString()))
                        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC) {
                            DebugFlightRecorder.record("RESIDUAL_AUTO_PRESELECTED", mapOf(
                                "choiceCount" to verified.choices.size.toString()))
                        }
                    }.onFailure {
                        liveAiAdviceError =
                            "Whole residual proposal is stale or illegal; continue manually."
                        DebugFlightRecorder.record("RESIDUAL_WHOLE_REJECTED")
                        BotcAiWireTrace.residualGate("HOST_REJECTED", legalOpen.size,
                            wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                    }
                }.onFailure { error ->
                    if (error is StorytellerResponseValidationException) {
                        liveAiAdviceError = if (language == "en")
                            "AI returned a response, but validation rejected it (${error.safeDetail}). Retry or choose manually."
                        else "AI 已返回，但响应未通过校验（${error.safeDetail}）。可重新推荐或手动选择。"
                        DebugFlightRecorder.record("RESIDUAL_RESPONSE_REJECTED")
                        BotcAiWireTrace.residualGate("HOST_REJECTED", legalOpen.size,
                            wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                        return@onFailure
                    }
                    // No retry on a paid call. An expired/missing provider chain
                    // can recover only on the NEXT explicit Host review using
                    // its complete snapshot; never advance a failed anchor.
                    val chainLost = previousResponseId != null &&
                        Regex("OpenAI HTTP (400|404)").containsMatchIn(error.message.orEmpty())
                    if (chainLost && canonicalConversationResponseId == previousResponseId) {
                        canonicalConversationResponseId = null
                        canonicalConversationModel = null
                    }
                    liveAiAdviceError = "Whole-game continuation unavailable (" +
                        PersonalDirectOpenAiV1.safeFailure(error) + "). Continue manually."
                    DebugFlightRecorder.record("RESIDUAL_WHOLE_UNAVAILABLE")
                    BotcAiWireTrace.residualGate("MODEL_UNAVAILABLE", legalOpen.size,
                        wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                }
            }
            return
        }
        // Never fall back to an independent paid V1 recommendation.
        liveAiAdviceError = "Whole-game V2 strategy is unavailable; continue manually."
        DebugFlightRecorder.record("RESIDUAL_V2_CONTEXT_MISSING")
    }

}
