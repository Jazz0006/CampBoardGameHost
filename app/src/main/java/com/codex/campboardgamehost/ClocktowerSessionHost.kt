package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.MayorRedirectDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderGameContextBuilderV1
import com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.debug.DebugFlightRecorder

/**
 * The only night-step Session confirmation dispatch. A draft from another game, night or
 * interaction is rejected; the Session remains the only fact writer.
 */
internal fun ClocktowerGameSession.confirmNightStep(
    confirmation: ClocktowerNightStepConfirmation,
    ruleset: ValidatedClocktowerRuleset,
    confirmMayorRedirect: (targetSeat: Int) -> Unit,
): Boolean {
    val step = confirmation.step
    val draft = confirmation.draft
    if (draft.gameId != state.gameId || draft.phase != state.phase || draft.round != state.round ||
        draft.interactionId != step.interactionId || state.nextNightInteraction(ruleset)?.id != draft.interactionId
    ) {
        DebugFlightRecorder.record("NIGHT_STEP_CONFIRMATION_STALE")
        return false
    }
    confirmation.observation?.let { observation ->
        val before = state.playerInputRevision
        commitGlobalEpistemicObservation(observation, requireNotNull(confirmation.previewRevision))
        return state.playerInputRevision != before
    }
    // Saved action IDs embed these exact step-ID strings; keep both forms for restored-game idempotency.
    val actionStepId = step.interactionId.toString()
    val boundaryStepId = draft.interactionId.value
    val target = draft.selectedSeats.firstOrNull()
    // Non-real night placeholders never commit an action. Only the active player/ruling has authority.
    if (step.isRealAction) when (step.action) {
        ClocktowerNightAction.RedHerring -> {
            val seat = target ?: state.redHerringSeat
            if (seat !in state.redHerringSelectableSeats()) return false
            commitRedHerringSeat(requireNotNull(seat), draft.phase, draft.round, nextInputSequence)
        }
        ClocktowerNightAction.Poison -> confirmNightPoison(target, draft.phase, draft.round, actionStepId)
        ClocktowerNightAction.MonkProtect -> confirmNightProtection(requireNotNull(target), draft.phase, draft.round, actionStepId)
        ClocktowerNightAction.DemonKill -> confirmNightAttack(
            targetSeat = requireNotNull(target) { "Imp must choose a target before confirming the irreversible attack." },
            phase = draft.phase, round = draft.round, stepId = actionStepId, successorSeat = confirmation.successorSeat,
        )
        ClocktowerNightAction.MayorRedirect -> confirmMayorRedirect(requireNotNull(target))
        ClocktowerNightAction.ButlerMaster -> confirmNightButlerMaster(
            actorSeat = requireNotNull(state.gameState.players.firstOrNull { it.name == step.actor?.name }).seat,
            masterSeat = requireNotNull(target) { "Butler must choose a master before proceeding." },
            phase = draft.phase, round = draft.round, stepId = actionStepId,
        )
        else -> Unit
    }
    if (step.roleEnName == "Spy") confirmSpyWake(ruleset, boundaryStepId)
    if (step.displayKind == ClocktowerDisplayKind.EvilInfo) {
        confirmEvilInformation(ruleset, boundaryStepId, requireNotNull(confirmation.previewRevision))
    }
    return true
}

/** Session-to-Host adapter and confirmation dispatcher. Destinations own UI drafts; Session owns facts. */
@Composable
internal fun ClocktowerSessionHost(
    lifecycle: ClocktowerGameLifecycle,
    cards: List<PlayerCard>,
    activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
    aiConversation: ClocktowerAiConversation,
    aiSettings: StorytellerAiSettings,
    isJudgeVisible: () -> Boolean,
    drunkAiScope: kotlinx.coroutines.CoroutineScope,
    onHostTools: () -> Unit,
) {
    val sessionUi = requireNotNull(lifecycle.sessionUi)
    val storytellerOperationMode = lifecycle.operationMode
    val clocktowerRulesetRef = lifecycle.rulesetRef
    val session = sessionUi.session
    val sessionState = sessionUi.snapshot
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val currentClocktowerScript = ClocktowerScript.entries.single { it.toRecommendationScriptId() == sessionState.gameState.script }
    val clocktowerPhase = sessionState.phase.toClocktowerPhase()
    val round = sessionState.round
    val clocktowerGameId = sessionState.gameId
    val clocktowerGameStateRevision = sessionState.gameStateRevision
    val clocktowerPlayerInputRevision = sessionState.playerInputRevision
    val allowRuleDeterminedAutoUi = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC
    val openingPlanRecommendation = aiConversation.openingPlanRecommendation
    val openingRecommendedInformation = aiConversation.openingRecommendedInformation
    val openingRecommendedBluffRoleNames = aiConversation.openingRecommendedBluffRoleNames
    val openingRecommendedRedHerringSeat = aiConversation.openingRecommendedRedHerringSeat
    val pendingCurrentStepAdvice = aiConversation.pendingCurrentStepAdvice
    val liveAiAdviceKey = aiConversation.liveAiAdviceKey
    val liveAiAdviceText = aiConversation.liveAiAdviceText
    val liveAiAdviceBusy = aiConversation.liveAiAdviceBusy
    val liveAiAdviceError = aiConversation.liveAiAdviceError
    // Accepted setup choices come from Session, not separate writable Compose state.
    val clocktowerRecommendedDemonBluffRoleNames =
        sessionState?.demonBluffRoleIds?.map(RoleId::value).orEmpty()
    // This is a derived validity check, not a second mutable truth or stale-plan cache.
    // A confirmed Host action or an overridden published clue can invalidate
    // the whole linked opening proposal while leaving actual Session facts intact.
    val openingInvalidation = openingPlanRecommendation?.let { opening ->
        sessionState.let { actual ->
            ClocktowerJointOpeningDisplayMatcherV2.invalidation(
                session = actual,
                plannedDrunkSeat = opening.selection.drunkSeat,
                plannedInformation = openingRecommendedInformation,
                plannedRedHerringSeat = openingRecommendedRedHerringSeat,
                plannedBluffRoleNames = openingRecommendedBluffRoleNames,
            )
        }
    }
    val openingHintsUsable = openingPlanRecommendation != null && openingInvalidation == null
    LaunchedEffect(sessionState.gameId, openingPlanRecommendation != null, openingInvalidation) {
        if (openingPlanRecommendation != null) BotcAiWireTrace.openingAdvice(
            openingInvalidation?.name ?: "REUSABLE")
    }
    fun requireClocktowerGameSession() = session
    fun currentTroubleBrewingMayorRedirectPendingDecision(): PendingMayorRedirectDecision? {
        if (
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerPhase != ClocktowerPhase.Night
        ) {
            return null
        }
        val session = lifecycle.sessionUi?.session ?: return null
        val rulesetRef = clocktowerRulesetRef ?: return null
        val registry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = session.toGameSnapshot(rulesetRef),
            phase = StorytellerPhase.NIGHT,
            round = round,
            characterRegistry = registry,
        )
        return MayorRedirectDecisionBoundary.create(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = snapshot.gameId,
                requestId = "night:${round}:mayor-redirect:game-${session.state.gameStateRevision}:input-${session.state.playerInputRevision}:cursor-${session.state.nextTimelineGlobalSequence}",
            ),
            revision = StorytellerDecisionRevision(
                gameStateRevision = clocktowerGameStateRevision,
                playerInputRevision = clocktowerPlayerInputRevision,
            ),
            snapshot = snapshot,
            characterRegistry = registry,
        )
    }

    fun advanceClocktowerPlayerInputRevision() {
        sessionUi.commit { it.recordPlayerInput() }
    }
    fun storytellerPhaseFor(phase: ClocktowerPhase = clocktowerPhase): StorytellerPhase =
        phase.toStorytellerPhase()

    fun clocktowerSeatFor(playerName: String): Int =
        requireNotNull(cards.seatOf(playerName)) { "Unknown Clocktower player '$playerName'." }

    fun clocktowerActionId(
        kind: String,
        actionRound: Int = round,
        localSequence: Int = requireClocktowerGameSession().nextInputSequence,
        targetSeat: Int? = null,
    ): String = buildList {
        add(kind)
        add(clocktowerGameId)
        add(requireClocktowerGameSession().state.gameStateRevision.toString())
        add(actionRound.toString())
        add(localSequence.toString())
        targetSeat?.let { add(it.toString()) }
    }.joinToString("-")

    /** Host-accepted action: one Session step for typed chronology and revision. */
    fun commitClocktowerPhaseAdvance(
        nextPhase: ClocktowerPhase,
        nextRound: Int = round,
    ) {
        if (nextPhase == clocktowerPhase && nextRound == round) return
        val localSequence = requireClocktowerGameSession().nextInputSequence
        requireClocktowerGameSession().commitConfirmedPhaseAdvance(
            draft = ActionFactDraft.PhaseAdvance(
                actionId = clocktowerActionId(
                    kind = "phase-${nextPhase.name.lowercase()}-$nextRound",
                    actionRound = round,
                    localSequence = localSequence,
                ),
                phase = storytellerPhaseFor(clocktowerPhase),
                round = round,
                sequence = localSequence,
                nextPhase = storytellerPhaseFor(nextPhase),
                nextRound = nextRound,
            ),
            // Dusk expiry or another accepted step may have advanced Session in this callback.
            expectedGameStateRevision = requireClocktowerGameSession().state.gameStateRevision,
        )
    }

    fun requestLiveGlobalAdvice(steps: List<ClocktowerNightStepUi>, stepIndex: Int, fortuneTellerSelectedNames: List<String>) =
        aiConversation.requestLiveGlobalAdvice(
            steps, stepIndex, aiSettings = aiSettings,
            activeGameClocktowerRulesetCatalog = activeGameClocktowerRulesetCatalog,
            currentSession = { lifecycle.sessionUi?.session }, currentMode = { lifecycle.operationMode }, isJudgeVisible = isJudgeVisible,
            cards = cards, clocktowerRulesetRef = clocktowerRulesetRef,
            fortuneTellerSelectedNames = fortuneTellerSelectedNames,
            mayorPending = runCatching { currentTroubleBrewingMayorRedirectPendingDecision() }.getOrNull(),
            drunkAiScope = drunkAiScope,
            language = context.resources.configuration.locales[0].language,
        )
    fun confirmTroubleBrewingMayorRedirect(targetSeat: Int) {
        if (currentClocktowerScript != ClocktowerScript.TroubleBrewing) return
        val pendingDecision = requireNotNull(currentTroubleBrewingMayorRedirectPendingDecision())
        require(pendingDecision.revision == StorytellerDecisionRevision(
            clocktowerGameStateRevision, clocktowerPlayerInputRevision,
        )) { "Stale Mayor confirmation." }
        val session = requireClocktowerGameSession()
        if (session.state.nightTargets.mayorRedirectSeat == targetSeat) return
        val registry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = session.toGameSnapshot(requireNotNull(clocktowerRulesetRef)),
            phase = StorytellerPhase.NIGHT, round = round, characterRegistry = registry,
        )
        val request = StorytellerProviderRequestFactoryV1.fromMayorRedirect(
            decision = pendingDecision, snapshot = snapshot,
            gameContext = StorytellerProviderGameContextBuilderV1.build(
                snapshot, StorytellerProviderRevisionV1(
                    session.state.gameStateRevision, session.state.playerInputRevision,
                ), session.state,
            ),
        )
        session.commitConfirmedMayorRedirect(
            draft = ActionFactDraft.MayorRedirectChoice(
                actionId = clocktowerActionId("mayor-redirect", targetSeat = targetSeat),
                phase = StorytellerPhase.NIGHT, round = round,
                sequence = session.nextInputSequence,
                mayorSeat = pendingDecision.mayorSeat, targetSeat = targetSeat,
            ),
            request = request, expectedGameStateRevision = session.state.gameStateRevision,
        )
    }
    ClocktowerJudgeScreen(
        allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
        cards = cards,
        sessionState = sessionState,
        jointOpeningAvailable = openingHintsUsable,
        jointOpeningReason = openingPlanRecommendation?.tradeoffSummary.takeIf { openingHintsUsable },
        recommendedDemonBluffRoleNames =
            clocktowerRecommendedDemonBluffRoleNames.ifEmpty {
                openingRecommendedBluffRoleNames.takeIf {
                    openingHintsUsable
                }.orEmpty()
            },
        onHostTools = onHostTools,
        onCommitDemonBluffs = { roleNames ->
            sessionUi.commit {
                if (roleNames != clocktowerRecommendedDemonBluffRoleNames) {
                    requireClocktowerGameSession().commitDemonBluffs(
                        roles = roleNames.map(::RoleId),
                        phase = storytellerPhaseFor(), round = round,
                        sequence = requireClocktowerGameSession().nextInputSequence,
                    )
                }
            }
        },
        onConfirmNewDemon = {
            sessionUi.commit {
                val session = requireClocktowerGameSession()
                if (session.state.gameId == sessionState.gameId &&
                    session.state.phase == sessionState.phase && session.state.round == sessionState.round &&
                    session.state.gameStateRevision == sessionState.gameStateRevision &&
                    session.state.playerInputRevision == sessionState.playerInputRevision &&
                    session.state.pendingImpNotification()?.actionId == sessionState.pendingImpNotification()?.actionId
                ) session.confirmImpNotification()
            }
        },
        onConfirmKlutzChoice = { chosenName ->
            sessionUi.commit {
                val session = requireClocktowerGameSession()
                val death = requireNotNull(session.state.pendingKlutzDeath())
                session.confirmKlutzChoice(death.actionId, clocktowerSeatFor(chosenName))
            }
        },
        onConfirmArtistQuestion = { claimant, truth, shown ->
            sessionUi.commit {
                requireClocktowerGameSession().confirmArtistAnswer(clocktowerSeatFor(claimant), truth, shown)
            }
        },
        onSlayerShot = { claimant, target, registration ->
            sessionUi.commit {
                requireClocktowerGameSession().confirmSlayerShot(clocktowerSeatFor(claimant), clocktowerSeatFor(target), registration)
            }
        },
        onConfirmedNomination = { nominator, nominee, spyChoice, expectedRound ->
            sessionUi.commit {
                val executed = requireClocktowerGameSession().confirmNomination(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), spyChoice, expectedRound)
                executed
            } ?: false
        },
        onConfirmedVote = { nominator, nominee, vote, expectedRound ->
            sessionUi.commit {
                requireClocktowerGameSession().confirmVote(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), vote.voters.map { clocktowerSeatFor(it.playerName) }, expectedRound)
            }
        },
        onConfirmDay = { expectedRound ->
            sessionUi.commit {
                requireClocktowerGameSession().confirmEndDay(expectedRound)
            }
        },
        nightContent = {
            ClocktowerNightHostScreen(
                allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                cards = cards,
                sessionState = sessionState,
                globalAiAssisted = storytellerOperationMode != StorytellerOperationMode.MANUAL,
                globalAiAutomatic = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC,
                jointOpeningAvailable = openingHintsUsable,
                jointOpeningReason = openingPlanRecommendation?.tradeoffSummary.takeIf { openingHintsUsable },
                recommendedRedHerringSeat = openingRecommendedRedHerringSeat.takeIf { openingHintsUsable },
                jointOpeningSemantics = openingRecommendedInformation.takeIf {
                    openingHintsUsable
                }.orEmpty(),
                globalNightPendingDecision = if (
                    storytellerOperationMode != StorytellerOperationMode.MANUAL &&
                    clocktowerPhase == ClocktowerPhase.Night
                ) runCatching { currentTroubleBrewingMayorRedirectPendingDecision() }.getOrNull()
                else null,
                globalAiAdviceKey = liveAiAdviceKey,
                globalAiAdviceText = liveAiAdviceText,
                globalAiAdviceBusy = liveAiAdviceBusy,
                globalAiAdviceError = liveAiAdviceError,
                globalResidualRecommendedChoices = pendingCurrentStepAdvice?.takeIf { pending ->
                    val latest = lifecycle.sessionUi?.session?.state
                    val plan = pending.recommendation
                    pending.requestKey == liveAiAdviceKey &&
                        latest?.gameId == plan.gameId &&
                        latest.gameStateRevision == plan.gameStateRevision &&
                        latest.playerInputRevision == plan.playerInputRevision &&
                        latest.nextTimelineGlobalSequence == plan.exclusiveGlobalSequence
                }?.recommendation?.choices.orEmpty(),
                onRequestGlobalAiAdvice = ::requestLiveGlobalAdvice,
                recommendedDemonBluffRoleNames =
                    clocktowerRecommendedDemonBluffRoleNames.ifEmpty {
                        openingRecommendedBluffRoleNames.takeIf {
                            openingHintsUsable
                        }.orEmpty()
                    },
                onHostTools = onHostTools,
                onRecordPlayerInput = ::advanceClocktowerPlayerInputRevision,
                onConfirmNightStep = { confirmation ->
                    sessionUi.commit { session ->
                        val before = session.state
                        val accepted = session.confirmNightStep(confirmation,
                            activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript),
                            ::confirmTroubleBrewingMayorRedirect)
                        if (accepted) {
                            confirmation.observation?.let { observation ->
                                aiConversation.reviewConfirmedCurrentStepAdvice(session, storytellerOperationMode, before, observation)
                                if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
                                    BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                                        aiConversation.strategicPolicyCursor?.version ?: 0,
                                        observation.phase.name, observation.round)
                                }
                            }
                        }
                        accepted
                    } ?: false
                },
                rulesetCatalog = activeGameClocktowerRulesetCatalog,
                onAdvanceFromFirstNight = {
                    sessionUi.commit {
                        commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                    }
                },
                onCompleteNight = {
                    sessionUi.commit {
                        commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                    }
                },
            )
        },
    )
}
