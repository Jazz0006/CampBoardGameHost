package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.codex.campboardgamehost.clocktower.session.OpeningPlayerExperienceV1
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1
import com.codex.campboardgamehost.clocktower.session.InitialJointOpeningDirectV2
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicChangeCheckpointV2
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2
import com.codex.campboardgamehost.clocktower.session.InitialOpeningRequestV2
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticAcceptanceV2
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRequest
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRoute
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRouter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPreparedSetup
import kotlinx.coroutines.launch

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

/** Pre-Session shown-role handoff and unconfirmed opening inputs; never stores a live game. */
internal class ClocktowerOpeningDraft {
    enum class Result { UNCHANGED, DEAL, JOINT, DRUNK, HOST, FAILED }
    private fun started(result: ClocktowerGameLifecycle.StartResult) =
        if (result == ClocktowerGameLifecycle.StartResult.STARTED) Result.DEAL else Result.FAILED
    var pendingTroubleBrewingDrunkSelection by mutableStateOf<PendingTroubleBrewingDrunkSelection?>(null)
    var pendingJointOpening by mutableStateOf<PendingTroubleBrewingJointOpening?>(null)
    var jointOpeningResult by mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null)
    var pendingOpeningResponseId by mutableStateOf<String?>(null)
    var pendingOpeningModel by mutableStateOf<String?>(null)
    var jointOpeningBusy by mutableStateOf(false)
    var jointOpeningError by mutableStateOf<String?>(null)
    var jointOpeningSerial by mutableStateOf(0)
    var shownOnlyDealCompleted by mutableStateOf(false)
    fun reset(conversation: ClocktowerAiConversation) {
        // Invalidate any prior asynchronous callback before preparing the next setup.
        jointOpeningSerial += 1
        conversation.openingPlanRecommendation = null
        conversation.openingRecommendedBluffRoleNames = emptyList()
        conversation.openingRecommendedRedHerringSeat = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        conversation.canonicalConversationResponseId = null
        conversation.canonicalConversationModel = null
        conversation.strategicPolicyCursor = null
        conversation.strategicLastReviewedHost = null
        conversation.openingRecommendedInformation = emptyMap()
        conversation.pendingCurrentStepAdvice = null
        shownOnlyDealCompleted = false
        pendingJointOpening = null
        jointOpeningResult = null
        jointOpeningBusy = false
        jointOpeningError = null
        pendingTroubleBrewingDrunkSelection = null
    }
    fun requestJointOpening(
        pending: PendingTroubleBrewingJointOpening,
        aiSettings: StorytellerAiSettings,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        lifecycle: ClocktowerGameLifecycle,
        drunkAiScope: kotlinx.coroutines.CoroutineScope,
    ) {
        if (pendingJointOpening !== pending || jointOpeningBusy ||
            lifecycle.operationMode == StorytellerOperationMode.MANUAL ||
            !aiSettings.directEnabled) return
        if (aiSettings.token.isBlank() || !aiSettings.endpoint.startsWith("https://")) {
            jointOpeningError = "AI credentials unavailable. You can continue manually."
            return
        }
        val requestedMode = lifecycle.operationMode
        val requestedKey = aiSettings.token
        val requestedModel = aiSettings.model ?: PersonalDirectOpenAiV1.DEFAULT_MODEL
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
                lifecycle.operationMode != requestedMode || !aiSettings.directEnabled
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

    fun confirmJointOpeningDrunk(
        pending: PendingTroubleBrewingJointOpening,
        candidate: TroubleBrewingDrunkCandidate?,
        conversation: ClocktowerAiConversation,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        lifecycle: ClocktowerGameLifecycle,
    ): Result {
        val accepted = jointOpeningResult ?: return Result.UNCHANGED
        if (pendingJointOpening !== pending ||
            !accepted.selection.validFor(pending.request) ||
            lifecycle.operationMode == StorytellerOperationMode.MANUAL) return Result.UNCHANGED
        val legalRoute = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (legalRoute) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> require(candidate == null)
            is TroubleBrewingDrunkSelectionRoute.ManualSelection ->
                require(candidate in legalRoute.request.candidates)
        }
        val selectedSeat = candidate?.seat
        val recommendationMatchesActualDrunk = selectedSeat == accepted.selection.drunkSeat
        if (lifecycle.commitTroubleBrewingSetup(pending.preparedGameId, pending.preparedSetup, candidate) !=
            ClocktowerGameLifecycle.StartResult.STARTED) return Result.FAILED
        jointOpeningSerial += 1
        jointOpeningBusy = false
        pendingJointOpening = null
        pendingTroubleBrewingDrunkSelection = null
        shownOnlyDealCompleted = false
        // The recommendation is advice about this game, not a committed plan.
        // An overridden Drunk must invalidate all dependent opening display hints.
        conversation.openingPlanRecommendation = accepted
        conversation.openingRecommendedInformation = if (recommendationMatchesActualDrunk) {
            accepted.selection.information
        } else emptyMap()
        conversation.openingRecommendedBluffRoleNames = if (recommendationMatchesActualDrunk) {
            val registry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
            accepted.selection.demonBluffs.orEmpty().map { externalId ->
                requireNotNull(registry.findByExternalId(externalId)).id.value
            }
        } else emptyList()
        conversation.openingRecommendedRedHerringSeat = if (recommendationMatchesActualDrunk) {
            accepted.selection.redHerringSeat
        } else null
        conversation.strategicPolicyCursor = accepted.strategyMemory?.let {
            StorytellerStrategicContinuationV2.MemoryCursor.initial(accepted)
        }
        conversation.canonicalConversationResponseId =
            pendingOpeningResponseId.takeIf { recommendationMatchesActualDrunk }
        conversation.canonicalConversationModel =
            pendingOpeningModel.takeIf { recommendationMatchesActualDrunk }
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        jointOpeningResult = null
        conversation.strategicLastReviewedHost = lifecycle.sessionUi?.session?.let { committedSession ->
            StorytellerStrategicChangeCheckpointV2.capture(committedSession,
                StorytellerStrategicChangeCheckpointV2.Position(
                    committedSession.state.phase, committedSession.state.round))
        }
        lifecycle.persistMetadata()
        return Result.HOST
    }

    fun beginJointOpeningAtDrunkStep(
        conversation: ClocktowerAiConversation,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        lifecycle: ClocktowerGameLifecycle,
    ): Result {
        val pending = pendingJointOpening ?: return Result.UNCHANGED
        val accepted = jointOpeningResult ?: return Result.UNCHANGED
        if (jointOpeningBusy ||
            lifecycle.operationMode == StorytellerOperationMode.MANUAL ||
            !accepted.selection.validFor(pending.request)) return Result.UNCHANGED
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
                    return Result.UNCHANGED
                }
        }
        if (legal !is TroubleBrewingDrunkSelectionRoute.ManualSelection ||
            lifecycle.operationMode == StorytellerOperationMode.AI_AUTOMATIC
        ) {
            return confirmJointOpeningDrunk(pending, recommended, conversation, activeGameClocktowerRulesetCatalog, lifecycle)
        }
        pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
            pending.preparedGameId, pending.preparedSetup,
            legal.request.copy(recommendedCandidate = recommended),
        )
        return Result.DRUNK
    }

    fun takeOverJointOpeningManually(
        conversation: ClocktowerAiConversation,
        lifecycle: ClocktowerGameLifecycle,
    ): Result {
        val pending = pendingJointOpening ?: return Result.UNCHANGED
        jointOpeningSerial += 1
        jointOpeningBusy = false
        jointOpeningResult = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        conversation.canonicalConversationResponseId = null
        conversation.canonicalConversationModel = null
        jointOpeningError = null
        pendingJointOpening = null
        lifecycle.takeOverManually()
        shownOnlyDealCompleted = true
        val route = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (route) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                val result = lifecycle.commitTroubleBrewingSetup(pending.preparedGameId, pending.preparedSetup, null)
                if (result != ClocktowerGameLifecycle.StartResult.STARTED) {
                    pendingJointOpening = pending
                    return Result.FAILED
                }
                shownOnlyDealCompleted = false
                return Result.HOST
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
                    pending.preparedGameId, pending.preparedSetup, route.request)
                // Deliberately NO standalone AI call. Manual fallback is local.
                return Result.DRUNK
            }
        }
    }

    fun confirmManualDrunk(pending: PendingTroubleBrewingDrunkSelection,
        candidate: TroubleBrewingDrunkCandidate, lifecycle: ClocktowerGameLifecycle): Result {
        if (pendingTroubleBrewingDrunkSelection !== pending) return Result.UNCHANGED
        require(candidate in pending.request.candidates)
        val result = lifecycle.commitTroubleBrewingSetup(pending.preparedGameId, pending.preparedSetup, candidate)
        if (result != ClocktowerGameLifecycle.StartResult.STARTED) return Result.FAILED
        pendingTroubleBrewingDrunkSelection = null
        return if (shownOnlyDealCompleted) { shownOnlyDealCompleted = false; Result.HOST } else Result.DEAL
    }

    fun beginPrepared(
        preparedGameId: String, preparedSetup: TroubleBrewingPreparedSetup,
        playerExperience: Map<Int, OpeningPlayerExperienceV1>,
        language: String, aiSettings: StorytellerAiSettings,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        lifecycle: ClocktowerGameLifecycle,
        drunkAiScope: kotlinx.coroutines.CoroutineScope,
    ): Result {
        if (aiSettings.directEnabled &&
            lifecycle.operationMode != StorytellerOperationMode.MANUAL
        ) {
            val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
                preparedGameId, preparedSetup.intermediateSetup)
            val openingRequest = InitialOpeningRequestV2.fromHostSnapshot(
                snapshot, activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry,
                playerExperience = playerExperience,
            )
            val visibleCards = preparedSetup.intermediateSetup.shownSeatAssignments.map { assignment ->
                val role = requireNotNull(completeTroubleBrewingRoles.singleOrNull { available ->
                    available.enName.filter(Char::isLetterOrDigit).lowercase() == assignment.shownRoleId
                }) { "Missing shown-only precommit role." }
                clocktowerPlayerCard(assignment.playerName, null, role, language)
            }
            val pending = PendingTroubleBrewingJointOpening(
                preparedGameId, preparedSetup, openingRequest, visibleCards)
            pendingJointOpening = pending
            requestJointOpening(pending, aiSettings, activeGameClocktowerRulesetCatalog, lifecycle, drunkAiScope) // One network request, concurrently with visible-only dealing.
            return Result.JOINT
        }
        when (
            val route = TroubleBrewingDrunkSelectionRouter.route(
                preparedSetup = preparedSetup,
                recommendedCandidate = null,
            )
        ) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded ->
                return started(lifecycle.commitTroubleBrewingSetup(preparedGameId, preparedSetup, null))

            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                val pending = PendingTroubleBrewingDrunkSelection(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    request = route.request,
                )
                pendingTroubleBrewingDrunkSelection = pending
                // Only the manual operator reaches this local picker. AI paid
                // Drunk-only planning was replaced by the joint-opening request.
                return Result.DRUNK
            }
        }
    }
}
