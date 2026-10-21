package com.codex.campboardgamehost

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderGameContextBuilderV1
import com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.confirmedPoisonTargetName
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft


/** Transient choices for the current role. Confirmations always write through Session. */
internal class ClocktowerNightDrafts {
    var clocktowerDemonAttackDraftTarget by mutableStateOf<String?>(null)
    var clocktowerPoisonTarget by mutableStateOf<String?>(null)
    var clocktowerFortuneTellerFirst by mutableStateOf<String?>(null)
    var clocktowerFortuneTellerSecond by mutableStateOf<String?>(null)
    var clocktowerChambermaidFirst by mutableStateOf<String?>(null)
    var clocktowerChambermaidSecond by mutableStateOf<String?>(null)
    var clocktowerRavenkeeperTarget by mutableStateOf<String?>(null)
    var clocktowerButlerMaster by mutableStateOf<String?>(null)
    var clocktowerMonkProtectedTarget by mutableStateOf<String?>(null)
    var clocktowerMayorRedirectTarget by mutableStateOf<String?>(null)
    var redHerringDraftName by mutableStateOf<String?>(null)
}

@Composable
internal fun ClocktowerPlayScreen(
    session: ClocktowerGameSession,
    cards: List<PlayerCard>,
    catalog: BuiltInClocktowerRulesetCatalog,
    rulesetRef: RulesetRef?,
    advice: ClocktowerLiveAdvice,
    nightDrafts: ClocktowerNightDrafts,
    operationMode: StorytellerOperationMode,
    openingHintsUsable: Boolean,
    openingInformation: Map<Int, HostOpeningSemanticValueV2>,
    openingBluffs: List<String>,
    openingRedHerringSeat: Int?,
    onPublish: () -> Unit,
    onSaveError: () -> Unit,
    onHostTools: () -> Unit,
    requestAdvice: (List<ClocktowerNightStepUi>, Int) -> Unit,
    mayorDecision: () -> PendingMayorRedirectDecision?,
) {
    val state = session.state
    val round = state.round
    val clocktowerPhase = when (state.phase) {
        StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
        StorytellerPhase.DAY -> ClocktowerPhase.Day
        StorytellerPhase.NIGHT -> ClocktowerPhase.Night
    }
    val currentClocktowerScript = ClocktowerScript.entries.single { it.toRecommendationScriptId() == state.gameState.script }
    val allowRuleDeterminedAutoUi = operationMode == StorytellerOperationMode.AI_AUTOMATIC
    val clocktowerRedHerring = state.redHerringSeat?.let { cards.getOrNull(it - 1)?.name }
    val clocktowerRecommendedDemonBluffRoleNames = state.demonBluffRoleIds.map(RoleId::value)
    fun advanceClocktowerPlayerInputRevision() { session.recordPlayerInput(); onPublish() }
    fun confirmClocktowerStep(confirm: () -> Unit) {
        try { confirm() } catch (_: java.io.IOException) { onSaveError() }
    }
    fun confirmClocktowerNomination(confirm: () -> Boolean): Boolean =
        try { confirm() } catch (_: java.io.IOException) { onSaveError(); false }
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
        localSequence: Int = session.nextInputSequence,
        targetSeat: Int? = null,
    ): String = buildList {
        add(kind)
        add(state.gameId)
        add(session.state.gameStateRevision.toString())
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
        val localSequence = session.nextInputSequence
        session.commitConfirmedPhaseAdvance(
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
            expectedGameStateRevision = session.state.gameStateRevision,
        )
        onPublish()
    }

    /**
     * React ONLY to an actual newly committed Session observation. A verified
     * current-step model answer is not automatically an accepted Host ruling.
     * If the Storyteller chose a different answer, Session truth wins and the
     * next model call reconsiders it from the retained history prefix.
     */
    fun commitClocktowerPrivateInformationPublication(draft: EpistemicObservationDraft, expectedRevision: StorytellerDecisionRevision) = confirmClocktowerStep {
                val before = session.state
        session.commitGlobalEpistemicObservation(draft, expectedRevision)
        if (session.state.playerInputRevision == before.playerInputRevision) return@confirmClocktowerStep
        advice.reviewConfirmedCurrentStepAdvice(session, before, draft, operationMode)
        onPublish()
        if (operationMode != StorytellerOperationMode.MANUAL) {
            BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                advice.strategicPolicyCursor?.version ?: 0,
                draft.phase.name, draft.round)
        }
    }

    val renderedPhase = state.phase
    val renderedRound = round
    ClocktowerJudgeScreen(
    allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
    cards = cards,
    sessionState = session.state,
    globalAiAssisted = operationMode != StorytellerOperationMode.MANUAL,
    globalAiAutomatic = operationMode == StorytellerOperationMode.AI_AUTOMATIC,
    jointOpeningAvailable = openingHintsUsable,
    jointOpeningSemantics = openingInformation.takeIf {
        openingHintsUsable
    }.orEmpty(),
    globalNightPendingDecision = if (
        operationMode != StorytellerOperationMode.MANUAL &&
        clocktowerPhase == ClocktowerPhase.Night
    ) runCatching { mayorDecision() }.getOrNull()
    else null,
    globalAiAdviceKey = advice.liveAiAdviceKey,
    globalAiAdviceText = advice.liveAiAdviceText,
    globalAiAdviceBusy = advice.liveAiAdviceBusy,
    globalAiAdviceError = advice.liveAiAdviceError,
    globalResidualRecommendedChoices = advice.pendingCurrentStepAdvice?.takeIf { pending ->
        val latest = session?.state
        val plan = pending.recommendation
        pending.requestKey == advice.liveAiAdviceKey &&
            latest?.gameId == plan.gameId &&
            latest.gameStateRevision == plan.gameStateRevision &&
            latest.playerInputRevision == plan.playerInputRevision &&
            latest.nextTimelineGlobalSequence == plan.exclusiveGlobalSequence
    }?.recommendation?.choices.orEmpty(),
    onRequestGlobalAiAdvice = requestAdvice,
    demonAttackDraftTarget = nightDrafts.clocktowerDemonAttackDraftTarget,
    poisonDraftTarget = nightDrafts.clocktowerPoisonTarget,
    monkProtectedDraftTarget = nightDrafts.clocktowerMonkProtectedTarget,
    mayorRedirectDraftTarget = nightDrafts.clocktowerMayorRedirectTarget,
    fortuneTellerFirst = nightDrafts.clocktowerFortuneTellerFirst,
    fortuneTellerSecond = nightDrafts.clocktowerFortuneTellerSecond,
    chambermaidFirst = nightDrafts.clocktowerChambermaidFirst,
    chambermaidSecond = nightDrafts.clocktowerChambermaidSecond,
    ravenkeeperTarget = nightDrafts.clocktowerRavenkeeperTarget,
    redHerring = nightDrafts.redHerringDraftName ?: clocktowerRedHerring,
    openingRecommendedRedHerring = openingRedHerringSeat
        ?.takeIf { openingHintsUsable }
        ?.let { cards.getOrNull(it - 1)?.name },
    recommendedDemonBluffRoleNames =
        clocktowerRecommendedDemonBluffRoleNames.ifEmpty {
            openingBluffs.takeIf {
                openingHintsUsable
            }.orEmpty()
        },
    butlerMaster = nightDrafts.clocktowerButlerMaster,
    onCommitConfirmedPrivateInformationPublication = ::commitClocktowerPrivateInformationPublication,
    onHostTools = onHostTools,
    onSelectNightDeath = { selected ->
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerDemonAttackDraftTarget = selected
    },
    onConfirmDemonAttack = { stepId, successorName ->
        confirmClocktowerStep {
                                            val targetSeat = clocktowerSeatFor(requireNotNull(nightDrafts.clocktowerDemonAttackDraftTarget) {
                "Imp must choose a target before confirming the irreversible attack."
            })
            session.confirmNightAttack(
                targetSeat = targetSeat,
                phase = renderedPhase, round = renderedRound, stepId = stepId,
                successorSeat = successorName?.let(::clocktowerSeatFor),
            )
            onPublish()
        }
    },
    onSelectPoisonTarget = { selected ->
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerPoisonTarget = selected
    },
    onConfirmPoisonTarget = { stepId ->
        confirmClocktowerStep {
                                            session.confirmNightPoison(
                targetSeat = nightDrafts.clocktowerPoisonTarget?.let(::clocktowerSeatFor),
                phase = renderedPhase, round = renderedRound, stepId = stepId,
            )
            onPublish()
        }
    },
    onSelectFortuneTellerFirst = {
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerFortuneTellerFirst = it
    },
    onSelectFortuneTellerSecond = {
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerFortuneTellerSecond = it
    },
    onSelectChambermaidFirst = {
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerChambermaidFirst = it
    },
    onSelectChambermaidSecond = {
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerChambermaidSecond = it
    },
    onSelectRavenkeeperTarget = {
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerRavenkeeperTarget = it
    },
    onSelectRedHerring = { name ->
        // A preview is never an actual accepted Red Herring choice.
        if (name != null) {
            require(cards.any { it.name == name }) {
                "Red Herring draft must refer to a current seat."
            }
        }
        nightDrafts.redHerringDraftName = name
    },
    onConfirmRedHerring = { name ->
        confirmClocktowerStep {
            val seat = cards.indexOfFirst { it.name == name }
                .takeIf { it >= 0 }?.plus(1)
                ?: error("Cannot confirm Red Herring outside the Session seats.")
            session.commitRedHerringSeat(
                seat = seat,
                phase = renderedPhase, round = renderedRound,
                sequence = session.nextInputSequence,
            )
            onPublish()
            nightDrafts.redHerringDraftName = null
        }
    },
    onCommitDemonBluffs = { roleNames ->
        confirmClocktowerStep {
            if (roleNames != clocktowerRecommendedDemonBluffRoleNames) {
                session.commitDemonBluffs(
                    roles = roleNames.map(::RoleId),
                    phase = renderedPhase, round = renderedRound,
                    sequence = session.nextInputSequence,
                )
                onPublish()
            }
        }
    },
    onSelectButlerMaster = {
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerButlerMaster = it
    },
    onConfirmButlerMaster = { actorName, masterName, stepId ->
        confirmClocktowerStep {
            session.confirmNightButlerMaster(
                phase = renderedPhase, round = renderedRound, stepId = stepId,
                actorSeat = clocktowerSeatFor(actorName),
                masterSeat = clocktowerSeatFor(masterName),
            )
            onPublish()
        }
    },
    onSelectMonkProtectedTarget = { selected ->
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerMonkProtectedTarget = selected
    },
    onConfirmMonkProtectedTarget = { stepId ->
        confirmClocktowerStep {
                                            session.confirmNightProtection(
                targetSeat = clocktowerSeatFor(requireNotNull(nightDrafts.clocktowerMonkProtectedTarget)),
                phase = renderedPhase, round = renderedRound, stepId = stepId,
            )
            onPublish()
        }
    },
    onSelectMayorRedirectTarget = { selected ->
        advanceClocktowerPlayerInputRevision()
        nightDrafts.clocktowerMayorRedirectTarget = selected
    },
    onConfirmMayorRedirectTarget = {
        confirmClocktowerStep {
            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                val selectedTarget = requireNotNull(nightDrafts.clocktowerMayorRedirectTarget)
                val pendingDecision = requireNotNull(mayorDecision())
                require(pendingDecision.revision == StorytellerDecisionRevision(
                    state.gameStateRevision, state.playerInputRevision,
                )) { "Stale Mayor confirmation." }
                if (session.state.let { state ->
                    state.nightTargets.mayorRedirectSeat?.let(state.gameState::playerAt)?.name
                } != selectedTarget) {
                    val targetSeat = clocktowerSeatFor(selectedTarget)
                                                            val registry = catalog
                        .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                    val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                        gameSnapshot = session.toGameSnapshot(requireNotNull(rulesetRef)),
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
                    onPublish()
                }
            }
        }
    },
    onConfirmNewDemon = {
        confirmClocktowerStep {
            session.confirmImpNotification()
            onPublish()
        }
    },
    onConfirmSpyWake = { stepId ->
        confirmClocktowerStep {
            session.confirmSpyWake(catalog.ruleset(currentClocktowerScript), stepId)
            onPublish()
        }
    },
    onConfirmEvilInformation = { stepId, previewRevision ->
        confirmClocktowerStep {
            session.confirmEvilInformation(catalog.ruleset(currentClocktowerScript), stepId, previewRevision)
            onPublish()
        }
    },
    onConfirmKlutzChoice = { chosenName ->
        confirmClocktowerStep {
                                            val death = requireNotNull(session.state.pendingKlutzDeath())
            session.confirmKlutzChoice(death.actionId, clocktowerSeatFor(chosenName))
            onPublish()
        }
    },
    onConfirmArtistQuestion = { claimant, truth, shown ->
        confirmClocktowerStep {
            session.confirmArtistAnswer(clocktowerSeatFor(claimant), truth, shown)
            onPublish()
        }
    },
    onSlayerShot = { claimant, target, registration ->
        confirmClocktowerStep {
            session.confirmSlayerShot(clocktowerSeatFor(claimant), clocktowerSeatFor(target), registration)
            onPublish()
        }
    },
    onConfirmedNomination = { nominator, nominee, spyChoice, expectedRound ->
        confirmClocktowerNomination {
            val executed = session.confirmNomination(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), spyChoice, expectedRound)
            onPublish()
            executed
        }
    },
    onConfirmedVote = { nominator, nominee, vote, expectedRound ->
        confirmClocktowerStep {
            session.confirmVote(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), vote.voters.map { clocktowerSeatFor(it.playerName) }, expectedRound)
            onPublish()
        }
    },
    onAdvanceFromFirstNight = {
        confirmClocktowerStep {
            commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
            onPublish()
            nightDrafts.clocktowerDemonAttackDraftTarget = null

        }
    },
    onConfirmDay = { expectedRound ->
        confirmClocktowerStep {
            session.confirmEndDay(expectedRound)
            onPublish()
        }
    },
    onCompleteNight = {
        confirmClocktowerStep {
            commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
            nightDrafts.clocktowerFortuneTellerFirst = null
            nightDrafts.clocktowerFortuneTellerSecond = null
            nightDrafts.clocktowerChambermaidFirst = null
            nightDrafts.clocktowerChambermaidSecond = null
            nightDrafts.clocktowerRavenkeeperTarget = null
            nightDrafts.clocktowerMonkProtectedTarget = null
            // PhaseAdvance has already cleared transient night targets.
            onPublish()
            nightDrafts.clocktowerMayorRedirectTarget = null
        }
    },
)
}
