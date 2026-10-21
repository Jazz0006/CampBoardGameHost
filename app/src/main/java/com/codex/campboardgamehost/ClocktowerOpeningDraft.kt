package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.codex.campboardgamehost.clocktower.session.OpeningPlayerExperienceV1
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
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
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
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
    var pendingTroubleBrewingDrunkSelection by mutableStateOf<PendingTroubleBrewingDrunkSelection?>(null)
    var pendingJointOpening by mutableStateOf<PendingTroubleBrewingJointOpening?>(null)
    var jointOpeningResult by mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null)
    var pendingOpeningResponseId by mutableStateOf<String?>(null)
    var pendingOpeningModel by mutableStateOf<String?>(null)
    var jointOpeningBusy by mutableStateOf(false)
    var jointOpeningError by mutableStateOf<String?>(null)
    var jointOpeningSerial by mutableStateOf(0)
    var jointRevealIndex by mutableStateOf(0)
    var shownOnlyDealCompleted by mutableStateOf(false)
    fun prepare(
        context: android.content.Context,
        playerNames: List<String>, gameSeed: Long,
        catalog: BuiltInClocktowerRulesetCatalog,
    ): TroubleBrewingPreparedSetup {
        val datasetJson = context.assets
            .open("setup/trouble_brewing_setup_presets_v2_final.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val dataset = TroubleBrewingSetupPresetJson.parse(datasetJson)
        val rotationHistoryStore = TroubleBrewingSetupRotationHistoryStore.fromContext(context)
        val rotationHistory = rotationHistoryStore.historyFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            playerCount = playerNames.size,
        )
        val playerRotationHistory = rotationHistoryStore.recentPlayerStartingIdentityHistoryFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
        )
        val characterRegistry = catalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        return TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = characterRegistry,
            orderedPlayerNames = playerNames.toList(),
            gameSeed = gameSeed,
            recentSetupRotationHistory = rotationHistory,
            recentPlayerStartingIdentityHistory = playerRotationHistory,
        )
    }

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
        jointRevealIndex = 0
        pendingTroubleBrewingDrunkSelection = null
    }
    fun requestJointOpening(
        pending: PendingTroubleBrewingJointOpening,
        aiSettings: StorytellerAiSettings,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        currentMode: () -> StorytellerOperationMode,
        currentScreen: () -> Screen,
        drunkAiScope: kotlinx.coroutines.CoroutineScope,
    ) {
        if (pendingJointOpening !== pending || jointOpeningBusy ||
            currentMode() == StorytellerOperationMode.MANUAL ||
            !aiSettings.directEnabled) return
        if (aiSettings.token.isBlank() || !aiSettings.endpoint.startsWith("https://")) {
            jointOpeningError = "AI credentials unavailable. You can continue manually."
            return
        }
        val requestedMode = currentMode()
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
                currentMode() != requestedMode || !aiSettings.directEnabled ||
                currentScreen() !in setOf(Screen.ClocktowerJointPass,
                    Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)
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
        currentMode: () -> StorytellerOperationMode,
        currentSession: () -> ClocktowerGameSession?,
        commitAndStartTroubleBrewingGame: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> Unit,
        onScreenChange: (Screen) -> Unit,
    ) {
        val accepted = jointOpeningResult ?: return
        if (pendingJointOpening !== pending ||
            !accepted.selection.validFor(pending.request) ||
            currentMode() == StorytellerOperationMode.MANUAL) return
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
        commitAndStartTroubleBrewingGame(
            pending.preparedGameId, pending.preparedSetup, candidate,
        )
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
        conversation.strategicLastReviewedHost = currentSession()?.let { committedSession ->
            StorytellerStrategicChangeCheckpointV2.capture(committedSession,
                StorytellerStrategicChangeCheckpointV2.Position(
                    committedSession.state.phase, committedSession.state.round))
        }
        onScreenChange(Screen.ClocktowerJudge)
    }

    fun beginJointOpeningAtDrunkStep(
        currentScreen: () -> Screen,
        currentMode: () -> StorytellerOperationMode,
        conversation: ClocktowerAiConversation,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        currentSession: () -> ClocktowerGameSession?,
        commit: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> Unit,
        onScreenChange: (Screen) -> Unit,
    ) {
        val pending = pendingJointOpening ?: return
        val accepted = jointOpeningResult ?: return
        if (currentScreen() != Screen.ClocktowerJointReview || jointOpeningBusy ||
            currentMode() == StorytellerOperationMode.MANUAL ||
            !accepted.selection.validFor(pending.request)) return
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
                    return
                }
        }
        if (legal !is TroubleBrewingDrunkSelectionRoute.ManualSelection ||
            currentMode() == StorytellerOperationMode.AI_AUTOMATIC
        ) {
            confirmJointOpeningDrunk(pending, recommended, conversation, activeGameClocktowerRulesetCatalog, currentMode, currentSession, commit, onScreenChange)
            return
        }
        pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
            pending.preparedGameId, pending.preparedSetup,
            legal.request.copy(recommendedCandidate = recommended),
        )
        onScreenChange(Screen.ClocktowerDrunkSelection)
    }

    fun takeOverJointOpeningManually(
        currentScreen: () -> Screen,
        conversation: ClocktowerAiConversation,
        onModeChange: (StorytellerOperationMode) -> Unit,
        commitAndStartTroubleBrewingGame: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> Unit,
        onScreenChange: (Screen) -> Unit,
    ) {
        val pending = pendingJointOpening ?: return
        if (currentScreen() !in setOf(Screen.ClocktowerJointPass,
                Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)) return
        jointOpeningSerial += 1
        jointOpeningBusy = false
        jointOpeningResult = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        conversation.canonicalConversationResponseId = null
        conversation.canonicalConversationModel = null
        jointOpeningError = null
        pendingJointOpening = null
        onModeChange(StorytellerOperationMode.MANUAL)
        shownOnlyDealCompleted = true
        val route = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (route) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                commitAndStartTroubleBrewingGame(
                    pending.preparedGameId, pending.preparedSetup, null)
                onScreenChange(Screen.ClocktowerJudge) // shown roles already dealt
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
                    pending.preparedGameId, pending.preparedSetup, route.request)
                onScreenChange(Screen.ClocktowerDrunkSelection)
                // Deliberately NO standalone AI call. Manual fallback is local.
            }
        }
    }

    fun beginPrepared(
        preparedGameId: String, preparedSetup: TroubleBrewingPreparedSetup,
        playerExperience: Map<Int, OpeningPlayerExperienceV1>,
        language: String, aiSettings: StorytellerAiSettings,
        activeGameClocktowerRulesetCatalog: BuiltInClocktowerRulesetCatalog,
        currentMode: () -> StorytellerOperationMode, currentScreen: () -> Screen,
        drunkAiScope: kotlinx.coroutines.CoroutineScope,
        commitAndStartTroubleBrewingGame: (String, TroubleBrewingPreparedSetup, TroubleBrewingDrunkCandidate?) -> Unit,
        onScreenChange: (Screen) -> Unit,
    ) {
        if (aiSettings.directEnabled &&
            currentMode() != StorytellerOperationMode.MANUAL
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
                PlayerCard(
                    name = assignment.playerName,
                    roleLabel = role.nameFor(language),
                    actualRoleLabel = null,
                    clocktowerTeam = role.team,
                    clocktowerRole = null, // No speculative actual Drunk identity.
                    clocktowerShownRole = role,
                )
            }
            val pending = PendingTroubleBrewingJointOpening(
                preparedGameId, preparedSetup, openingRequest, visibleCards)
            pendingJointOpening = pending
            jointRevealIndex = 0
            onScreenChange(Screen.ClocktowerJointPass)
            requestJointOpening(pending, aiSettings, activeGameClocktowerRulesetCatalog, currentMode, currentScreen, drunkAiScope) // One network request, concurrently with visible-only dealing.
            return
        }
        when (
            val route = TroubleBrewingDrunkSelectionRouter.route(
                preparedSetup = preparedSetup,
                recommendedCandidate = null,
            )
        ) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded ->
                commitAndStartTroubleBrewingGame(
                    preparedGameId, preparedSetup, null,
                )

            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                val pending = PendingTroubleBrewingDrunkSelection(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    request = route.request,
                )
                pendingTroubleBrewingDrunkSelection = pending
                onScreenChange(Screen.ClocktowerDrunkSelection)
                // Only the manual operator reaches this local picker. AI paid
                // Drunk-only planning was replaced by the joint-opening request.
            }
        }
    }
}
