package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
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
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft

/** Actual Session-to-Host UI adapter. Only drafts live here; all confirmations call the one Session. */
@Composable
internal fun ClocktowerSessionHost(
    session: ClocktowerGameSession,
    sessionState: ClocktowerSessionState,
    cards: List<PlayerCard>,
    activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
    clocktowerRulesetRef: RulesetRef?,
    aiConversation: ClocktowerAiConversation,
    aiSettings: StorytellerAiSettings,
    storytellerOperationMode: StorytellerOperationMode,
    currentMode: () -> StorytellerOperationMode,
    currentSession: () -> ClocktowerGameSession?,
    isJudgeVisible: () -> Boolean,
    drunkAiScope: kotlinx.coroutines.CoroutineScope,
    onHostTools: () -> Unit,
    onChanged: () -> Unit,
    onSaveError: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val currentClocktowerScript = ClocktowerScript.entries.single { it.toRecommendationScriptId() == sessionState.gameState.script }
    val clocktowerPhase = when (sessionState.phase) {
        StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
        StorytellerPhase.DAY -> ClocktowerPhase.Day
        StorytellerPhase.NIGHT -> ClocktowerPhase.Night
    }
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
    var clocktowerDemonAttackDraftTarget by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerPoisonTarget by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerFortuneTellerFirst by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerFortuneTellerSecond by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerChambermaidFirst by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerChambermaidSecond by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerRavenkeeperTarget by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerButlerMaster by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerMonkProtectedTarget by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var clocktowerMayorRedirectTarget by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    var redHerringDraftName by remember(sessionState.gameId) { mutableStateOf<String?>(null) }
    // Accepted setup choices come from Session, not separate writable Compose state.
    val clocktowerRedHerring = sessionState?.redHerringSeat
        ?.let { seat -> cards.getOrNull(seat - 1)?.name }
    val clocktowerRecommendedDemonBluffRoleNames =
        sessionState?.demonBluffRoleIds?.map(RoleId::value).orEmpty()
    // This is a derived validity check, not a second mutable truth or stale-plan cache.
    // A confirmed Host action or an overridden published clue can invalidate
    // the whole linked opening proposal while leaving actual Session facts intact.
    val openingHintsUsable = openingPlanRecommendation?.let { opening ->
        sessionState.let { actual ->
            ClocktowerJointOpeningDisplayMatcherV2.stillUsable(
                session = actual,
                plannedDrunkSeat = opening.selection.drunkSeat,
                plannedInformation = openingRecommendedInformation,
                plannedRedHerringSeat = openingRecommendedRedHerringSeat,
                plannedBluffRoleNames = openingRecommendedBluffRoleNames,
            )
        }
    } == true
    // UI-only selection; the Red Herring enters Session at its first-night Next step.

    fun refreshClocktowerUi() = onChanged()
    fun requireClocktowerGameSession() = session
    fun confirmClocktowerStep(confirm: () -> Unit) {
        try { confirm() } catch (_: java.io.IOException) { onSaveError() }
    }
    fun confirmClocktowerNomination(confirm: () -> Boolean): Boolean = try { confirm() }
        catch (_: java.io.IOException) { onSaveError(); false }
    fun currentTroubleBrewingMayorRedirectPendingDecision(): PendingMayorRedirectDecision? {
        if (
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerPhase != ClocktowerPhase.Night
        ) {
            return null
        }
        val session = currentSession() ?: return null
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
        requireClocktowerGameSession().recordPlayerInput()
        refreshClocktowerUi()
    }
    fun storytellerPhaseFor(phase: ClocktowerPhase = clocktowerPhase): StorytellerPhase =
        phase.toStorytellerPhase()

    fun clocktowerSeatFor(playerName: String): Int =
        cards.indexOfFirst { it.name == playerName }
            .takeIf { index -> index >= 0 }
            ?.plus(1)
            ?: error("Unknown Clocktower player '$playerName'.")

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
        refreshClocktowerUi()
    }

    fun commitClocktowerPrivateInformationPublication(draft: EpistemicObservationDraft, expectedRevision: StorytellerDecisionRevision) = confirmClocktowerStep {
        val session = requireClocktowerGameSession()
        val before = session.state
        session.commitGlobalEpistemicObservation(draft, expectedRevision)
        if (session.state.playerInputRevision == before.playerInputRevision) return@confirmClocktowerStep
        aiConversation.reviewConfirmedCurrentStepAdvice(session, storytellerOperationMode, before, draft)
        refreshClocktowerUi()
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
            BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                aiConversation.strategicPolicyCursor?.version ?: 0,
                draft.phase.name, draft.round)
        }
    }


    fun requestLiveGlobalAdvice(steps: List<ClocktowerNightStepUi>, stepIndex: Int) =
        aiConversation.requestLiveGlobalAdvice(
            steps, stepIndex, aiSettings = aiSettings,
            activeGameClocktowerRulesetCatalog = activeGameClocktowerRulesetCatalog,
            currentSession = currentSession, currentMode = currentMode, isJudgeVisible = isJudgeVisible,
            cards = cards, clocktowerRulesetRef = clocktowerRulesetRef,
            fortuneTellerSelectedNames = listOfNotNull(clocktowerFortuneTellerFirst, clocktowerFortuneTellerSecond),
            mayorPending = runCatching { currentTroubleBrewingMayorRedirectPendingDecision() }.getOrNull(),
            drunkAiScope = drunkAiScope,
        )
    val renderedPhase = storytellerPhaseFor()
    val renderedRound = round
    ClocktowerJudgeScreen(
                        allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                        cards = cards,
                        sessionState = requireClocktowerGameSession().state,
                        globalAiAssisted = storytellerOperationMode != StorytellerOperationMode.MANUAL,
                        globalAiAutomatic = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC,
                        jointOpeningAvailable = openingHintsUsable,
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
                            val latest = currentSession()?.state
                            val plan = pending.recommendation
                            pending.requestKey == liveAiAdviceKey &&
                                latest?.gameId == plan.gameId &&
                                latest.gameStateRevision == plan.gameStateRevision &&
                                latest.playerInputRevision == plan.playerInputRevision &&
                                latest.nextTimelineGlobalSequence == plan.exclusiveGlobalSequence
                        }?.recommendation?.choices.orEmpty(),
                        onRequestGlobalAiAdvice = ::requestLiveGlobalAdvice,
                        demonAttackDraftTarget = clocktowerDemonAttackDraftTarget,
                        poisonDraftTarget = clocktowerPoisonTarget,
                        monkProtectedDraftTarget = clocktowerMonkProtectedTarget,
                        mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
                        fortuneTellerFirst = clocktowerFortuneTellerFirst,
                        fortuneTellerSecond = clocktowerFortuneTellerSecond,
                        chambermaidFirst = clocktowerChambermaidFirst,
                        chambermaidSecond = clocktowerChambermaidSecond,
                        ravenkeeperTarget = clocktowerRavenkeeperTarget,
                        redHerring = redHerringDraftName ?: clocktowerRedHerring,
                        openingRecommendedRedHerring = openingRecommendedRedHerringSeat
                            ?.takeIf { openingHintsUsable }
                            ?.let { cards.getOrNull(it - 1)?.name },
                        recommendedDemonBluffRoleNames =
                            clocktowerRecommendedDemonBluffRoleNames.ifEmpty {
                                openingRecommendedBluffRoleNames.takeIf {
                                    openingHintsUsable
                                }.orEmpty()
                            },
                        butlerMaster = clocktowerButlerMaster,
                        onCommitConfirmedPrivateInformationPublication = ::commitClocktowerPrivateInformationPublication,
                        onHostTools = onHostTools,
                        onSelectNightDeath = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerDemonAttackDraftTarget = selected
                        },
                        onConfirmDemonAttack = { stepId, successorName ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                val targetSeat = clocktowerSeatFor(requireNotNull(clocktowerDemonAttackDraftTarget) {
                                    "Imp must choose a target before confirming the irreversible attack."
                                })
                                session.confirmNightAttack(
                                    targetSeat = targetSeat,
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                    successorSeat = successorName?.let(::clocktowerSeatFor),
                                )
                                refreshClocktowerUi()
                            }
                        },
                        onSelectPoisonTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerPoisonTarget = selected
                        },
                        onConfirmPoisonTarget = { stepId ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                session.confirmNightPoison(
                                    targetSeat = clocktowerPoisonTarget?.let(::clocktowerSeatFor),
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                )
                                refreshClocktowerUi()
                            }
                        },
                        onSelectFortuneTellerFirst = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerFortuneTellerFirst = it
                        },
                        onSelectFortuneTellerSecond = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerFortuneTellerSecond = it
                        },
                        onSelectChambermaidFirst = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerChambermaidFirst = it
                        },
                        onSelectChambermaidSecond = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerChambermaidSecond = it
                        },
                        onSelectRavenkeeperTarget = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerRavenkeeperTarget = it
                        },
                        onSelectRedHerring = { name ->
                            // A preview is never an actual accepted Red Herring choice.
                            if (name != null) {
                                require(cards.any { it.name == name }) {
                                    "Red Herring draft must refer to a current seat."
                                }
                            }
                            redHerringDraftName = name
                        },
                        onConfirmRedHerring = { name ->
                            confirmClocktowerStep {
                                val seat = cards.indexOfFirst { it.name == name }
                                    .takeIf { it >= 0 }?.plus(1)
                                    ?: error("Cannot confirm Red Herring outside the Session seats.")
                                requireClocktowerGameSession().commitRedHerringSeat(
                                    seat = seat,
                                    phase = renderedPhase, round = renderedRound,
                                    sequence = requireClocktowerGameSession().nextInputSequence,
                                )
                                refreshClocktowerUi()
                                redHerringDraftName = null
                            }
                        },
                        onCommitDemonBluffs = { roleNames ->
                            confirmClocktowerStep {
                                if (roleNames != clocktowerRecommendedDemonBluffRoleNames) {
                                    requireClocktowerGameSession().commitDemonBluffs(
                                        roles = roleNames.map(::RoleId),
                                        phase = renderedPhase, round = renderedRound,
                                        sequence = requireClocktowerGameSession().nextInputSequence,
                                    )
                                    refreshClocktowerUi()
                                }
                            }
                        },
                        onSelectButlerMaster = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerButlerMaster = it
                        },
                        onConfirmButlerMaster = { actorName, masterName, stepId ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmNightButlerMaster(
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                    actorSeat = clocktowerSeatFor(actorName),
                                    masterSeat = clocktowerSeatFor(masterName),
                                )
                                refreshClocktowerUi()
                            }
                        },
                        onSelectMonkProtectedTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMonkProtectedTarget = selected
                        },
                        onConfirmMonkProtectedTarget = { stepId ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                session.confirmNightProtection(
                                    targetSeat = clocktowerSeatFor(requireNotNull(clocktowerMonkProtectedTarget)),
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                )
                                refreshClocktowerUi()
                            }
                        },
                        onSelectMayorRedirectTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMayorRedirectTarget = selected
                        },
                        onConfirmMayorRedirectTarget = {
                            confirmClocktowerStep {
                                if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                    val selectedTarget = requireNotNull(clocktowerMayorRedirectTarget)
                                    val pendingDecision = requireNotNull(currentTroubleBrewingMayorRedirectPendingDecision())
                                    require(pendingDecision.revision == StorytellerDecisionRevision(
                                        clocktowerGameStateRevision, clocktowerPlayerInputRevision,
                                    )) { "Stale Mayor confirmation." }
                                    if (requireClocktowerGameSession().state.let { state ->
                                        state.nightTargets.mayorRedirectSeat?.let(state.gameState::playerAt)?.name
                                    } != selectedTarget) {
                                        val targetSeat = clocktowerSeatFor(selectedTarget)
                                        val session = requireClocktowerGameSession()
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
                                        refreshClocktowerUi()
                                    }
                                }
                            }
                        },
                        onConfirmNewDemon = {
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmImpNotification()
                                refreshClocktowerUi()
                            }
                        },
                        onConfirmSpyWake = { stepId ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmSpyWake(activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript), stepId)
                                refreshClocktowerUi()
                            }
                        },
                        onConfirmEvilInformation = { stepId, previewRevision ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmEvilInformation(activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript), stepId, previewRevision)
                                refreshClocktowerUi()
                            }
                        },
                        onConfirmKlutzChoice = { chosenName ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                val death = requireNotNull(session.state.pendingKlutzDeath())
                                session.confirmKlutzChoice(death.actionId, clocktowerSeatFor(chosenName))
                                refreshClocktowerUi()
                            }
                        },
                        onConfirmArtistQuestion = { claimant, truth, shown ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmArtistAnswer(clocktowerSeatFor(claimant), truth, shown)
                                refreshClocktowerUi()
                            }
                        },
                        onSlayerShot = { claimant, target, registration ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmSlayerShot(clocktowerSeatFor(claimant), clocktowerSeatFor(target), registration)
                                refreshClocktowerUi()
                            }
                        },
                        onConfirmedNomination = { nominator, nominee, spyChoice, expectedRound ->
                            confirmClocktowerNomination {
                                val executed = requireClocktowerGameSession().confirmNomination(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), spyChoice, expectedRound)
                                refreshClocktowerUi()
                                executed
                            }
                        },
                        onConfirmedVote = { nominator, nominee, vote, expectedRound ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmVote(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), vote.voters.map { clocktowerSeatFor(it.playerName) }, expectedRound)
                                refreshClocktowerUi()
                            }
                        },
                        onAdvanceFromFirstNight = {
                            confirmClocktowerStep {
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                                refreshClocktowerUi()
                                clocktowerDemonAttackDraftTarget = null

                            }
                        },
                        onConfirmDay = { expectedRound ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmEndDay(expectedRound)
                                refreshClocktowerUi()
                            }
                        },
                        onCompleteNight = {
                            confirmClocktowerStep {
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                                clocktowerFortuneTellerFirst = null
                                clocktowerFortuneTellerSecond = null
                                clocktowerChambermaidFirst = null
                                clocktowerChambermaidSecond = null
                                clocktowerRavenkeeperTarget = null
                                clocktowerMonkProtectedTarget = null
                                // PhaseAdvance has already cleared transient night targets.
                                refreshClocktowerUi()
                                clocktowerMayorRedirectTarget = null
                            }
                        },
                    )
}
