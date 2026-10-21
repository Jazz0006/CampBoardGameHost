package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRouter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRoute
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1
import com.codex.campboardgamehost.clocktower.session.InitialJointOpeningDirectV2
import com.codex.campboardgamehost.clocktower.session.InitialOpeningRequestV2
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticAcceptanceV2
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRequest
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPreparedSetup
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import kotlinx.coroutines.launch


/** Unconfirmed pre-session Drunk choice; never persisted as a gameplay operation. */
internal data class PendingTroubleBrewingDrunkSelection(
    val preparedGameId: String,
    val preparedSetup: TroubleBrewingPreparedSetup,
    val request: TroubleBrewingDrunkSelectionRequest,
)

/** Showing roles may begin while Drunk actual identity is uncommitted. No real session yet. */
internal data class PendingTroubleBrewingJointOpening(
    val preparedGameId: String,
    val preparedSetup: TroubleBrewingPreparedSetup,
    val request: InitialOpeningRequestV2,
    val shownOnlyCards: List<PlayerCard>,
)

/** Prepared opening and shown-role handoff only; no writable GameState or Session. */
internal class ClocktowerOpeningUi {
    var pendingTroubleBrewingDrunkSelection by mutableStateOf<PendingTroubleBrewingDrunkSelection?>(null)
    var pendingJointOpening by mutableStateOf<PendingTroubleBrewingJointOpening?>(null)
    var jointOpeningResult by mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null)
    // Opaque provider IDs are provisional until Host accepts the opening. Never
    // persist them to game facts, logs or player's visible information.
    var pendingOpeningResponseId by mutableStateOf<String?>(null)
    var pendingOpeningModel by mutableStateOf<String?>(null)
    var jointOpeningBusy by mutableStateOf(false)
    var jointOpeningError by mutableStateOf<String?>(null)
    var jointOpeningSerial by mutableStateOf(0)
    var jointRevealIndex by mutableStateOf(0)
    var shownOnlyDealCompleted by mutableStateOf(false)

    /** 3C: the only INITIAL paid model request for personal-direct TB opening. */
    fun request(
        pending: PendingTroubleBrewingJointOpening,
        connection: StorytellerAiSettings,
        storytellerOperationMode: StorytellerOperationMode,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        drunkAiScope: kotlinx.coroutines.CoroutineScope,
        isCurrent: () -> Boolean,
    ) {
        if (pendingJointOpening !== pending || jointOpeningBusy ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            !connection.directEnabled) return
        if (connection.token.isBlank() || !connection.endpoint.startsWith("https://")) {
            jointOpeningError = "AI credentials unavailable. You can continue manually."
            return
        }
        val requestedKey = connection.token
        val requestedModel = connection.model ?: PersonalDirectOpenAiV1.DEFAULT_MODEL
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        jointOpeningSerial += 1
        val serial = jointOpeningSerial
        jointOpeningBusy = true
        jointOpeningResult = null
        jointOpeningError = null
        val registry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        BotcAiTrace.firstNightEvent(serial, "JOINT_PRECOMMIT_START")
        drunkAiScope.launch {
            val result = runCatching {
                InitialJointOpeningDirectV2.recommendWithReceipt(
                    endpoint = PersonalDirectOpenAiV1.ENDPOINT,
                    accessToken = requestedKey,
                    request = pending.request,
                    registry = registry,
                    model = requestedModel,
                    onStage = { stage -> BotcAiTrace.firstNightStage(serial, stage) },
                )
            }
            // Precommit has NO committed session. Never compare against a
            // fake session state; bind directly to this immutable pending game.
            if (serial != jointOpeningSerial || pendingJointOpening !== pending ||
                !isCurrent() || !connection.directEnabled
            ) return@launch
            result.onSuccess { receipt ->
                if (receipt.accepted.selection.validFor(pending.request)) {
                    jointOpeningResult = receipt.accepted
                    pendingOpeningResponseId = receipt.responseId
                    pendingOpeningModel = requestedModel
                    jointOpeningError = null
                    BotcAiTrace.firstNightEvent(serial, "JOINT_PRECOMMIT_READY")
                } else {
                    jointOpeningError = "Opening is stale. Retry whole planning or take over manually."
                }
            }.onFailure { error ->
                jointOpeningResult = null
                pendingOpeningResponseId = null
                pendingOpeningModel = null
                jointOpeningError = "Whole-game AI plan failed (" +
                    PersonalDirectOpenAiV1.safeFailure(error) + "). Manual takeover is available."
                BotcAiTrace.firstNightEvent(serial, "JOINT_PRECOMMIT_FAILED")
            }
            jointOpeningBusy = false
        }
    }

    /**
     * The first actual choice after shown-only role handoff is the Drunk.
     * The opening response is only an advisory joint plan, never a bundle commit.
     */
    fun confirmJointOpeningDrunk(
        pending: PendingTroubleBrewingJointOpening,
        candidate: TroubleBrewingDrunkCandidate?,
        storytellerOperationMode: StorytellerOperationMode,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        advice: ClocktowerLiveAdvice,
        commitAndStart: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> ClocktowerGameSession,
    ): Screen? {
        val accepted = jointOpeningResult ?: return null
        if (pendingJointOpening !== pending ||
            !accepted.selection.validFor(pending.request) ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL) return null
        val legalRoute = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (legalRoute) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> require(candidate == null)
            is TroubleBrewingDrunkSelectionRoute.ManualSelection ->
                require(candidate in legalRoute.request.candidates)
        }
        val selectedSeat = candidate?.seat
        val recommendationMatchesActualDrunk = selectedSeat == accepted.selection.drunkSeat
        jointOpeningSerial += 1
        jointOpeningBusy = false
        pendingJointOpening = null
        pendingTroubleBrewingDrunkSelection = null
        shownOnlyDealCompleted = false
        // Drunk becomes true only after this explicit current-step confirmation.
        val committedSession = commitAndStart(pending.preparedGameId, pending.preparedSetup, candidate)
        // The recommendation is advice about this game, not a committed plan.
        // An overridden Drunk must invalidate all dependent opening display hints.
        advice.openingPlanRecommendation = accepted
        advice.openingRecommendedInformation = if (recommendationMatchesActualDrunk) {
            accepted.selection.information
        } else emptyMap()
        advice.openingRecommendedBluffRoleNames = if (recommendationMatchesActualDrunk) {
            val registry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
            accepted.selection.demonBluffs.orEmpty().map { externalId ->
                requireNotNull(registry.findByExternalId(externalId)).id.value
            }
        } else emptyList()
        advice.openingRecommendedRedHerringSeat = if (recommendationMatchesActualDrunk) {
            accepted.selection.redHerringSeat
        } else null
        advice.strategicPolicyCursor = accepted.strategyMemory?.let {
            StorytellerStrategicContinuationV2.MemoryCursor.initial(accepted)
        }
        advice.canonicalConversationResponseId =
            pendingOpeningResponseId.takeIf { recommendationMatchesActualDrunk }
        advice.canonicalConversationModel =
            pendingOpeningModel.takeIf { recommendationMatchesActualDrunk }
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        jointOpeningResult = null
        advice.strategicLastReviewedHost = committedSession.let { committedSession ->
            StorytellerStrategicChangeCheckpointV2.capture(committedSession,
                StorytellerStrategicChangeCheckpointV2.Position(
                    committedSession.state.phase, committedSession.state.round))
        }
        return Screen.ClocktowerJudge
    }

    fun beginJointOpeningAtDrunkStep(
        screen: Screen,
        storytellerOperationMode: StorytellerOperationMode,
        confirmDrunk: (PendingTroubleBrewingJointOpening, TroubleBrewingDrunkCandidate?) -> Unit,
    ): Screen? {
        val pending = pendingJointOpening ?: return null
        val accepted = jointOpeningResult ?: return null
        if (screen != Screen.ClocktowerJointReview || jointOpeningBusy ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            !accepted.selection.validFor(pending.request)) return null
        val legal = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        val recommended = when (legal) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                require(accepted.selection.drunkSeat == null)
                null
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection ->
                legal.request.candidates.singleOrNull {
                    it.seat == accepted.selection.drunkSeat
                } ?: run {
                    jointOpeningError = "This Drunk recommendation cannot be applied. Retry planning or take over manually."
                    return null
                }
        }
        if (legal !is TroubleBrewingDrunkSelectionRoute.ManualSelection ||
            storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC
        ) {
            confirmDrunk(pending, recommended)
            return null
        }
        pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
            pending.preparedGameId, pending.preparedSetup,
            legal.request.copy(recommendedCandidate = recommended),
        )
        return Screen.ClocktowerDrunkSelection
    }

    fun takeOverJointOpeningManually(
        screen: Screen,
        advice: ClocktowerLiveAdvice,
        onManualMode: () -> Unit,
        commitAndStart: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> Unit,
    ): Screen? {
        val pending = pendingJointOpening ?: return null
        if (screen !in setOf(Screen.ClocktowerJointPass,
                Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)) return null
        jointOpeningSerial += 1
        jointOpeningBusy = false
        jointOpeningResult = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        advice.canonicalConversationResponseId = null
        advice.canonicalConversationModel = null
        jointOpeningError = null
        pendingJointOpening = null
        onManualMode()
        shownOnlyDealCompleted = true
        val route = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (route) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                commitAndStart(
                    pending.preparedGameId, pending.preparedSetup, null)
                return Screen.ClocktowerJudge // shown roles already dealt
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
                    pending.preparedGameId, pending.preparedSetup, route.request)
                return Screen.ClocktowerDrunkSelection
                // Deliberately NO standalone AI call. Manual fallback is local.
            }
        }
    }


}

@Composable
internal fun ClocktowerOpeningScreens(
    screen: Screen,
    openingUi: ClocktowerOpeningUi,
    storytellerOperationMode: StorytellerOperationMode,
    activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
    language: String,
    navigate: (Screen) -> Unit,
    isDrunkSelectionCurrent: () -> Boolean,
    onContinue: () -> Unit,
    onManual: () -> Unit,
    onRetry: (PendingTroubleBrewingJointOpening) -> Unit,
    onConfirmOpeningDrunk: (PendingTroubleBrewingJointOpening, TroubleBrewingDrunkCandidate?) -> Unit,
    onStartGame: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> Unit,
) {
    when (screen) {
        Screen.ClocktowerJointPass -> {
            val pending = requireNotNull(openingUi.pendingJointOpening)
            val previewCards = pending.shownOnlyCards
            require(openingUi.jointRevealIndex in previewCards.indices)
            PassPhoneScreen(
                playerName = previewCards[openingUi.jointRevealIndex].name,
                playerNames = previewCards.map { it.name },
                                            current = openingUi.jointRevealIndex + 1,
                total = previewCards.size,
                onReveal = { navigate(Screen.ClocktowerJointReveal) },
                onPrevious = {
                    if (openingUi.jointRevealIndex > 0) openingUi.jointRevealIndex -= 1
                },
                onHostTools = {}, // No actual Host grimoire exists yet.
                onNext = {
                    if (openingUi.jointRevealIndex == previewCards.lastIndex) {
                        navigate(Screen.ClocktowerJointReview)
                    } else {
                        openingUi.jointRevealIndex += 1
                    }
                },
            )
        }

        Screen.ClocktowerJointReveal -> {
            val pending = requireNotNull(openingUi.pendingJointOpening)
            RevealCardScreen(
                card = pending.shownOnlyCards[openingUi.jointRevealIndex],
                                            current = openingUi.jointRevealIndex + 1,
                total = pending.shownOnlyCards.size,
                onHide = { navigate(Screen.ClocktowerJointPass) },
            )
        }

        Screen.ClocktowerJointReview -> {
            val pending = requireNotNull(openingUi.pendingJointOpening)
            ClocktowerJointOpeningReviewScreen(
                acceptance = openingUi.jointOpeningResult?.takeIf {
                    it.selection.validFor(pending.request)
                },
                busy = openingUi.jointOpeningBusy,
                error = openingUi.jointOpeningError,
                onContinue = onContinue,
                autoContinue = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC,
                onRetry = { onRetry(pending) },
                onManual = onManual,
            )
        }

        Screen.ClocktowerDrunkSelection -> {
            val pending = requireNotNull(openingUi.pendingTroubleBrewingDrunkSelection) {
                "Trouble Brewing Drunk selection screen requires a pending manual request."
            }
            val characterRegistry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing)
                .characterRegistry
            ClocktowerDrunkSelectionScreen(
                request = pending.request,
                operationMode = storytellerOperationMode,
                language = language,
                roleNameForExternalId = { externalId ->
                    val definition = requireNotNull(
                        characterRegistry.findByExternalId(externalId),
                    ) {
                        "Trouble Brewing Drunk selection references unknown role '$externalId'."
                    }
                    completeTroubleBrewingRoles
                        .singleOrNull { role -> role.enName == definition.id.value }
                        ?.nameFor(language)
                        ?: definition.name
                },
                onBack = {
                    openingUi.pendingTroubleBrewingDrunkSelection = null
                    navigate(if (openingUi.pendingJointOpening != null) {
                        Screen.ClocktowerJointReview
                    } else Screen.ClocktowerSettings)
                },
                onConfirm = { candidate ->
                    require(candidate in pending.request.candidates) {
                        "Trouble Brewing Drunk selection confirmation must use a displayed legal candidate."
                    }
                    if (openingUi.pendingTroubleBrewingDrunkSelection !== pending ||
                        !isDrunkSelectionCurrent()) {
                        DebugFlightRecorder.record("MANUAL_DRUNK_SELECTION_STALE")
                        return@ClocktowerDrunkSelectionScreen
                    }
                    val opening = openingUi.pendingJointOpening?.takeIf {
                        it.preparedGameId == pending.preparedGameId &&
                            storytellerOperationMode != StorytellerOperationMode.MANUAL
                    }
                    if (opening != null) {
                        onConfirmOpeningDrunk(opening, candidate)
                    } else {
                        onStartGame(
                            pending.preparedGameId,
                            pending.preparedSetup,
                            candidate,
                        )
                    }
                    if (opening == null && openingUi.shownOnlyDealCompleted) {
                        openingUi.shownOnlyDealCompleted = false
                        navigate(Screen.ClocktowerJudge)
                    }
                },
            )
        }

        else -> error("Not an opening screen: $screen")
    }
}
