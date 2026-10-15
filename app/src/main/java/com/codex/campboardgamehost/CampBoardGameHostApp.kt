package com.codex.campboardgamehost

import android.content.Context
import android.content.res.Configuration
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.session.OpeningPlayerExperienceV1
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContextBuilder
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.NoGreaterJoyKlutzHistoryProducerV1
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.MayorRedirectDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.MayorRedirectDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderGameContextBuilderV1
import com.codex.campboardgamehost.clocktower.session.DrunkAssignmentDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.PendingDrunkAssignmentDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionConfirmation
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1
import com.codex.campboardgamehost.clocktower.session.InitialJointOpeningDirectV2
import com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicChangeCheckpointV2
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2
import com.codex.campboardgamehost.clocktower.session.StorytellerResidualPackageGateV2
import com.codex.campboardgamehost.clocktower.session.HostCurrentOpenDecisionInventoryV2
import com.codex.campboardgamehost.clocktower.session.HostOpeningRulesDomainV2
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticAcceptanceV2
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderValidationV1
import com.codex.campboardgamehost.clocktower.session.StorytellerCausalDecisionJournalV1
import com.codex.campboardgamehost.clocktower.session.DayAbilityRegistrationRulingProducerV1
import com.codex.campboardgamehost.clocktower.rules.NoGreaterJoyKlutzChoiceRuleV1
import com.codex.campboardgamehost.clocktower.session.ConfirmedDaySlayerRegistrationV1
import com.codex.campboardgamehost.clocktower.session.ConfirmedDayVirginSpyRegistrationV1
import com.codex.campboardgamehost.clocktower.session.StorytellerCausalJournalRecordV1
import com.codex.campboardgamehost.clocktower.session.ClocktowerConfirmedRegistrationHostWriterV1
import com.codex.campboardgamehost.clocktower.session.ConfirmedRegistrationResolutionInputV1
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitPoisonTargetBoundary
import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision
import com.codex.campboardgamehost.clocktower.session.synchronizePoisonTargetWithinCurrentRevision
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
import com.codex.campboardgamehost.clocktower.session.NightCheckpointHostTransaction
import com.codex.campboardgamehost.clocktower.session.NightCheckpointRevisionIntent
import com.codex.campboardgamehost.clocktower.session.NightResolutionEvent
import com.codex.campboardgamehost.clocktower.session.DawnCommitIntent
import com.codex.campboardgamehost.clocktower.session.DawnDurableMaterializationState
import com.codex.campboardgamehost.clocktower.session.NightDawnDurableMaterializationPlanner
import com.codex.campboardgamehost.clocktower.session.NightDawnPoisonResolutionInput
import com.codex.campboardgamehost.clocktower.session.NightDawnPoisonRecoveryAuthority
import com.codex.campboardgamehost.clocktower.session.DuskPoisonExpiryMaterializationPlanner
import com.codex.campboardgamehost.clocktower.session.DuskPoisonExpiryMaterializationState
import com.codex.campboardgamehost.clocktower.session.DuskPoisonExpiryRecoveryAuthority
import com.codex.campboardgamehost.clocktower.session.NightDawnDeathResolutionInput
import com.codex.campboardgamehost.clocktower.session.NightDawnResolutionPlanner
import com.codex.campboardgamehost.clocktower.session.NightResolutionContinuation
import com.codex.campboardgamehost.clocktower.session.resolveTroubleBrewingImpSelfKillSuccession
import com.codex.campboardgamehost.clocktower.session.TroubleBrewingFirstNightPrecomputeCoordinator
import com.codex.campboardgamehost.clocktower.setup.NoGreaterJoyProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRequest
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRoute
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRouter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPreparedSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupCommitter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
import com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmCoordinator
import com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmRequest
import com.codex.campboardgamehost.clocktower.epistemic.A4MainThreadFrameTelemetry
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildExecutor
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationDurabilityGate
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildRequest
import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
import com.codex.campboardgamehost.clocktower.epistemic.A4ShadowWorldSetCache
import com.codex.campboardgamehost.clocktower.epistemic.A4ShadowLifecycleInvalidator
import com.codex.campboardgamehost.clocktower.epistemic.A4WorldEngineRollout
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInputFactory
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInputJsonCodec
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionContext
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionSemantics
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

private enum class Screen {
    Landing,
    Setup,
    GameSelection,
    UndercoverSettings,
    ClocktowerSettings,
    ClocktowerDrunkSelection,
    ClocktowerJointPass,
    ClocktowerJointReveal,
    ClocktowerJointReview,
    ClocktowerAutoPause,
    Settings,
    PassPhone,
    RevealCard,
    ClocktowerJudge,
    Game,
}

private data class PendingTroubleBrewingDrunkSelection(
    val preparedGameId: String,
    val preparedSetup: TroubleBrewingPreparedSetup,
    val request: TroubleBrewingDrunkSelectionRequest,
    val decision: PendingDrunkAssignmentDecision,
    val providerRequest: StorytellerProviderRequestV1,
)

/** Showing roles may begin while Drunk actual identity is uncommitted. No real session yet. */
private data class PendingTroubleBrewingJointOpening(
    val preparedGameId: String,
    val preparedSetup: TroubleBrewingPreparedSetup,
    val domain: HostOpeningRulesDomainV2,
    val shownOnlyCards: List<PlayerCard>,
)

internal enum class LanguageMode(val prefsValue: String) {
    System("system"),
    Chinese("zh"),
    English("en"),
}

internal fun PlayerCard.abilitySubject(poisonTarget: String?): AbilitySubject = AbilitySubject(
    actualRole = clocktowerRole?.enName,
    shownRole = clocktowerShownRole?.enName,
    isPoisoned = poisonTarget == name && eliminatedRound == null,
    isAlive = eliminatedRound == null,
)

private fun Context.playerName(number: Int): String = getString(R.string.default_player_name_format, number)

private const val PREFS_NAME = "camp_board_game_host"
private const val ACTIVE_GAME_STATE_KEY = "active_game_state"
internal const val A4_IDENTITY_PREWARM_LOG_TAG = "A4IdentityPrewarm"
internal const val A4_OBSERVATION_CACHE_UPDATE_LOG_TAG = "A4ObservationCacheUpdate"
internal const val SDE_HISTORICAL_REPLAY_CAPTURE_LOG_TAG = "SdeHistoricalReplayCapture"
internal const val MIN_PLAYERS = 3
internal const val MIN_CLOCKTOWER_PLAYERS = 5
internal const val MAX_PLAYERS = 15

private fun Context.localized(languageMode: LanguageMode): Context {
    if (languageMode == LanguageMode.System) return this
    val locale = Locale(languageMode.prefsValue)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

private fun Screen.isActiveGameScreen(): Boolean = when (this) {
    Screen.PassPhone,
    Screen.RevealCard,
    Screen.ClocktowerAutoPause,
    Screen.ClocktowerJudge,
    Screen.Game -> true
    Screen.Landing,
    Screen.Setup,
    Screen.GameSelection,
    Screen.UndercoverSettings,
    Screen.ClocktowerSettings,
    Screen.ClocktowerDrunkSelection,
    Screen.ClocktowerJointPass,
    Screen.ClocktowerJointReveal,
    Screen.ClocktowerJointReview,
    Screen.Settings -> false
}

private fun Context.saveActiveGameState(snapshot: JSONObject): Boolean =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(ACTIVE_GAME_STATE_KEY, snapshot.toString())
        .commit()

private fun Context.loadActiveGameStateJson(): JSONObject? {
    val raw = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(ACTIVE_GAME_STATE_KEY, null)
        ?: return null
    return runCatching { JSONObject(raw) }.getOrNull()
}

private fun Context.clearActiveGameState() {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(ACTIVE_GAME_STATE_KEY)
        .commit()
}

private fun Context.loadSavedGamePreview(localizedContext: Context): SavedGamePreview? =
    RecoveryPreviewLoader.load(
        raw = loadActiveGameStateJson(),
        prepare = { raw -> prepareCurrentRecoveryPlan(raw) },
        clearRejected = { clearActiveGameState() },
    )?.toSavedGamePreview(localizedContext)

internal fun Role.labelResId(): Int = when (this) {
    Role.Civilian -> R.string.role_civilian
    Role.Undercover -> R.string.role_undercover
    Role.Blank -> R.string.role_blank
}

internal fun LanguageMode.labelResId(): Int = when (this) {
    LanguageMode.System -> R.string.language_system
    LanguageMode.Chinese -> R.string.language_chinese
    LanguageMode.English -> R.string.language_english
}

private fun defaultClocktowerScriptFor(playerCount: Int): ClocktowerScript =
    if (playerCount in 5..6) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing

internal fun canStartClocktowerScript(script: ClocktowerScript): Boolean =
    script == ClocktowerScript.TroubleBrewing || script == ClocktowerScript.NoGreaterJoy

@Composable
internal fun CampBoardGameHostApp() {
    val baseContext = LocalContext.current
    val appPreferencesStore = remember(baseContext) { AppPreferencesStore(baseContext) }
    val gatewayConnectionStore = remember(baseContext) {
        StorytellerGatewayConnectionStore(baseContext)
    }
    val personalOpenAiStore = remember(baseContext) {
        PersonalOpenAiSettingsStore(baseContext)
    }
    val gameArchivePreferencesStore = remember(baseContext) {
        GameArchivePreferencesStore(
            context = baseContext,
            roleByName = { roleName -> clocktowerRoleByName(roleName) },
        )
    }
    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var languageMode by remember { mutableStateOf(appPreferencesStore.loadLanguageMode()) }
    val context = remember(languageMode) { baseContext.localized(languageMode) }
    val language = context.resources.configuration.locales[0].language
    var screen by remember { mutableStateOf(Screen.Landing) }
    var settingsReturnScreen by remember { mutableStateOf(Screen.Setup) }
    var pendingTroubleBrewingDrunkSelection by remember {
        mutableStateOf<PendingTroubleBrewingDrunkSelection?>(null)
    }
    var preferredStorytellerOperationMode by remember {
        mutableStateOf(appPreferencesStore.loadStorytellerOperationMode())
    }
    // Per-game mode can change for manual takeover; never overwrite the saved new-game default.
    var storytellerOperationMode by remember { mutableStateOf(preferredStorytellerOperationMode) }
    // Mode-derived, constrained to Host rule-deterministic UI paths; multi-choice remains manual.
    val allowRuleDeterminedAutoUi = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC
    var storytellerGatewayEndpoint by remember { mutableStateOf("") }
    var storytellerGatewayToken by remember { mutableStateOf("") }
    var gatewayConfigurationLoaded by remember { mutableStateOf(false) }
    var gatewayCredentialSaveFailed by remember { mutableStateOf(false) }
    var personalDirectEnabled by remember { mutableStateOf(false) }
    var personalDirectKey by remember { mutableStateOf("") }
    var personalDirectModel by remember {
        mutableStateOf(PersonalDirectOpenAiV1.DEFAULT_MODEL)
    }
    var personalDirectLoaded by remember { mutableStateOf(false) }
    var personalDirectSaveFailed by remember { mutableStateOf(false) }
    LaunchedEffect(personalOpenAiStore) {
        val restored = withContext(Dispatchers.IO) {
            Triple(personalOpenAiStore.loadEnabled(),
                personalOpenAiStore.loadModel(), personalOpenAiStore.loadKey())
        }
        personalDirectEnabled = restored.first
        personalDirectModel = restored.second
        personalDirectKey = restored.third
        personalDirectLoaded = true
    }
    LaunchedEffect(personalDirectLoaded, personalDirectEnabled, personalDirectModel, personalDirectKey) {
        if (!personalDirectLoaded) return@LaunchedEffect
        delay(700)
        val enabled = personalDirectEnabled
        val model = personalDirectModel
        val key = personalDirectKey
        val saved = withContext(Dispatchers.IO) {
            runCatching {
                personalOpenAiStore.saveEnabled(enabled)
                personalOpenAiStore.saveModel(model)
                personalOpenAiStore.saveKey(key)
            }.getOrDefault(false)
        }
        personalDirectSaveFailed = !saved
    }
    val aiEndpoint = if (personalDirectEnabled) PersonalDirectOpenAiV1.ENDPOINT
        else storytellerGatewayEndpoint
    val aiToken = if (personalDirectEnabled) personalDirectKey else storytellerGatewayToken
    val aiDirectModel = if (personalDirectEnabled) personalDirectModel.trim() else null
    LaunchedEffect(gatewayConnectionStore) {
        val restored = withContext(Dispatchers.IO) {
            gatewayConnectionStore.loadEndpoint() to gatewayConnectionStore.loadToken()
        }
        storytellerGatewayEndpoint = restored.first
        storytellerGatewayToken = restored.second
        gatewayConfigurationLoaded = true
    }
    LaunchedEffect(gatewayConfigurationLoaded, storytellerGatewayEndpoint, storytellerGatewayToken) {
        if (!gatewayConfigurationLoaded) return@LaunchedEffect
        // Do not run AndroidKeyStore AES/GCM on every keystroke or on the UI thread.
        delay(700)
        val endpoint = storytellerGatewayEndpoint
        val token = storytellerGatewayToken
        val saved = withContext(Dispatchers.IO) {
            runCatching {
                gatewayConnectionStore.saveEndpoint(endpoint)
                gatewayConnectionStore.saveToken(token)
            }.getOrDefault(false)
        }
        gatewayCredentialSaveFailed = !saved
    }
    var pendingJointOpening by remember { mutableStateOf<PendingTroubleBrewingJointOpening?>(null) }
    var jointOpeningResult by remember { mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null) }
    // Opaque provider IDs are provisional until Host accepts the opening. Never
    // persist them to game facts, logs or player's visible information.
    var pendingOpeningResponseId by remember { mutableStateOf<String?>(null) }
    var pendingOpeningModel by remember { mutableStateOf<String?>(null) }
    var canonicalConversationResponseId by remember { mutableStateOf<String?>(null) }
    var canonicalConversationModel by remember { mutableStateOf<String?>(null) }
    var jointOpeningBusy by remember { mutableStateOf(false) }
    var jointOpeningError by remember { mutableStateOf<String?>(null) }
    var jointOpeningSerial by remember { mutableStateOf(0) }
    var jointRevealIndex by remember { mutableStateOf(0) }
    var jointOpeningApplied by remember { mutableStateOf(false) }
    var adoptedJointOpening by remember { mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null) }
    var strategicPolicyCursor by remember {
        mutableStateOf<StorytellerStrategicContinuationV2.MemoryCursor?>(null)
    }
    var strategicLastReviewedHost by remember {
        mutableStateOf<StorytellerStrategicChangeCheckpointV2.Baseline?>(null)
    }
    var residualProposedPackage by remember {
        mutableStateOf<StorytellerResidualPackageGateV2.Accepted?>(null)
    }
    var residualProposedKey by remember { mutableStateOf<String?>(null) }
    var residualProposedOpen by remember {
        mutableStateOf<List<StorytellerStrategicContinuationV2.OpenDecision>>(emptyList())
    }
    var residualProposedPosition by remember {
        mutableStateOf<StorytellerStrategicChangeCheckpointV2.Position?>(null)
    }
    var residualAdoptedPackage by remember {
        mutableStateOf<StorytellerResidualPackageGateV2.Accepted?>(null)
    }
    var residualAdoptedOperationMode by remember {
        mutableStateOf<StorytellerOperationMode?>(null)
    }
    var adoptedJointInformation by remember { mutableStateOf<Map<Int,
        com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2>>(emptyMap()) }
    var shownOnlyDealCompleted by remember { mutableStateOf(false) }
    var liveAiAdviceKey by remember { mutableStateOf<String?>(null) }
    var liveAiAdviceText by remember { mutableStateOf<String?>(null) }
    var liveAiAdviceBusy by remember { mutableStateOf(false) }
    var liveAiAdviceError by remember { mutableStateOf<String?>(null) }
    val drunkAiScope = rememberCoroutineScope()
    var currentGameKind by remember { mutableStateOf(GameKind.Undercover) }
    var savedGamePreview by remember(context) { mutableStateOf(baseContext.loadSavedGamePreview(context)) }
    var gameHistory by remember { mutableStateOf(gameArchivePreferencesStore.loadGameHistory()) }
    var showHostTools by remember { mutableStateOf(false) }
    var hostToolTab by remember { mutableStateOf(HostToolTab.Roles) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var undercoverCount by remember { mutableStateOf(1) }
    var includeBlank by remember { mutableStateOf(false) }
    var currentDealIndex by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(1) }
    var selectedElimination by remember { mutableStateOf<String?>(null) }
    var clocktowerPhase by remember { mutableStateOf(ClocktowerPhase.FirstNight) }
    // The Demon may revise this while awake. Only the confirmed attack may
    // reach protection, Mayor redirection, death, or succession resolution.
    var clocktowerDemonAttackDraftTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingNightDeath by remember { mutableStateOf<String?>(null) }
    var clocktowerSelectedExecution by remember { mutableStateOf<String?>(null) }
    var clocktowerPoisonTarget by remember { mutableStateOf<String?>(null) }
    // A target is provisional while the Poisoner is still awake. It becomes a
    // mechanical fact only when the night step is advanced.
    var clocktowerConfirmedPoisonTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerFortuneTellerFirst by remember { mutableStateOf<String?>(null) }
    var clocktowerFortuneTellerSecond by remember { mutableStateOf<String?>(null) }
    var clocktowerChambermaidFirst by remember { mutableStateOf<String?>(null) }
    var clocktowerChambermaidSecond by remember { mutableStateOf<String?>(null) }
    var clocktowerRavenkeeperTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerRedHerring by remember { mutableStateOf<String?>(null) }
    var clocktowerRecommendedDemonBluffRoleNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerButlerMaster by remember { mutableStateOf<String?>(null) }
    var clocktowerMonkProtectedTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerConfirmedMonkProtectedTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerMayorRedirectTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerConfirmedMayorRedirectTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingNewDemonName by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingNightNewDemonIdentityName by remember { mutableStateOf<String?>(null) }
    var clocktowerDemonSuccessorTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerConfirmedDemonSuccessorTarget by remember { mutableStateOf<String?>(null) }
    fun clearConfirmedDemonSuccessorTarget() {
        clocktowerConfirmedDemonSuccessorTarget = null
    }
    var clocktowerVirginUsed by remember { mutableStateOf(false) }
    var clocktowerSlayerUsed by remember { mutableStateOf(false) }
    var clocktowerSlayerClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerArtistUsed by remember { mutableStateOf(false) }
    var clocktowerArtistClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerLastExecutedName by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingKlutzName by remember { mutableStateOf<String?>(null) }
    var clocktowerKlutzChoiceName by remember { mutableStateOf<String?>(null) }
    var clocktowerKlutzReturnToDawn by remember { mutableStateOf(false) }
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var clocktowerGameSession by remember { mutableStateOf<ClocktowerGameSession?>(null) }
    var clocktowerSessionView by remember { mutableStateOf<ClocktowerSessionView?>(null) }
    // Host-owned, current-session causal event store; Recovery restores only validated typed entries.
    val clocktowerCausalJournals = remember { mutableMapOf<String, StorytellerCausalDecisionJournalV1>() }
    val currentClocktowerScript = clocktowerSessionView?.scriptId
        ?.let { scriptId ->
            ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
        }
        ?: ClocktowerScript.TroubleBrewing
    val clocktowerGameId = clocktowerSessionView?.gameId.orEmpty()
    val clocktowerGameSeed = clocktowerSessionView?.gameSeed ?: 0L
    val clocktowerGameStateRevision = clocktowerSessionView?.gameStateRevision ?: 0L
    val clocktowerPlayerInputRevision = clocktowerSessionView?.playerInputRevision ?: 0L
    val clocktowerSemanticHistoryMode =
        clocktowerSessionView?.semanticHistoryMode ?: ClocktowerSemanticHistoryMode.LEGACY_LOCAL
    val clocktowerNextTimelineGlobalSequence =
        clocktowerSessionView?.nextTimelineGlobalSequence ?: 0L
    val clocktowerActionTimeline = clocktowerSessionView?.actionTimeline ?: ActionFactTimeline()
    val clocktowerEpistemicObservations =
        clocktowerSessionView?.epistemicObservationLog?.records.orEmpty()
    var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
    var committedTroubleBrewingSetupRotationRecord by remember {
        mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
    }
    var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
    var showResults by remember { mutableStateOf(false) }
    var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }
    var newCommonPlayerName by remember { mutableStateOf("") }
    val commonPlayers = remember {
        mutableStateListOf<String>().apply { addAll(appPreferencesStore.loadCommonPlayers()) }
    }
    // A persistent player profile keyed by the current unique player name.
    // Moving a person to a different seat must never change their experience.
    val playerExperienceByName = remember {
        mutableStateMapOf<String, PlayerExperienceLevelV1>().apply {
            putAll(appPreferencesStore.loadPlayerExperienceByName())
        }
    }
    fun playerExperience(name: String): PlayerExperienceLevelV1 =
        playerExperienceByName[name] ?: PlayerExperienceLevelV1.BEGINNER
    fun openingExperience(name: String): OpeningPlayerExperienceV1 = when (playerExperience(name)) {
        PlayerExperienceLevelV1.BEGINNER -> OpeningPlayerExperienceV1.BEGINNER
        PlayerExperienceLevelV1.NORMAL -> OpeningPlayerExperienceV1.REGULAR
        PlayerExperienceLevelV1.EXPERT -> OpeningPlayerExperienceV1.VETERAN
    }
    fun setPlayerExperience(name: String, level: PlayerExperienceLevelV1) {
        require(name.isNotBlank())
        if (level == PlayerExperienceLevelV1.BEGINNER) playerExperienceByName.remove(name)
        else playerExperienceByName[name] = level
        appPreferencesStore.savePlayerExperienceByName(playerExperienceByName)
    }
    val playerNames = remember { mutableStateListOf<String>() }
    var hostSeatingSetupFlow by remember { mutableStateOf(HostSeatingSetupFlow()) }
    // Retain the setup step when the user visits app-wide AI settings and returns.
    var clocktowerSetupStep by remember { mutableStateOf(0) }
    val cards = remember { mutableStateListOf<PlayerCard>() }
    val records = remember { mutableStateListOf<EliminationRecord>() }
    val recoveryWriteGate = remember { RecoveryWriteGate() }
    val clocktowerEvents = remember { mutableStateListOf<ClocktowerEvent>() }
    var clocktowerEventCounter by remember { mutableStateOf(0) }
    val clocktowerNightStartedState = remember { mutableStateOf(false) }
    val clocktowerNightStepIndexState = remember { mutableStateOf(0) }
    val clocktowerDayModeState = remember { mutableStateOf(ClocktowerDayMode.Overview) }
    val clocktowerGhostVoteAuthorityState = remember { mutableStateOf(ClocktowerGhostVoteAuthority()) }
    val clocktowerHighestVoteNameState = remember { mutableStateOf<String?>(null) }
    val clocktowerHighestVoteCountState = remember { mutableStateOf(0) }
    val troubleBrewingFirstNightPrecomputeScope = rememberCoroutineScope()
    val troubleBrewingFirstNightPrecomputeCoordinator = remember {
        val recommendationCoordinator = ClocktowerRecommendationCoordinator()
        TroubleBrewingFirstNightPrecomputeCoordinator<TroubleBrewingFirstNightPairDecisionContext, List<DecisionCandidate<SetupClueOutcome>>> { request ->
            recommendationCoordinator.naturalPairCandidates(request)
        }
    }
    val a4ShadowWorldSetCache = remember { A4ShadowWorldSetCache() }
    val a4IdentityRevealPrewarmer = remember(a4ShadowWorldSetCache) {
        A4IdentityRevealPrewarmCoordinator(cache = a4ShadowWorldSetCache)
    }
    val a4ObservationCacheRebuildExecutor = remember(a4ShadowWorldSetCache) {
        A4ObservationCacheRebuildExecutor(a4ShadowWorldSetCache)
    }
    val a4ObservationDurabilityGate = remember(clocktowerGameId) { A4ObservationDurabilityGate() }
    var a4ObservationCacheRebuildRequest by remember { mutableStateOf<A4ObservationCacheRebuildRequest?>(null) }
    val a4ShadowLifecycleInvalidator = remember(a4ShadowWorldSetCache, a4ObservationDurabilityGate) {
        A4ShadowLifecycleInvalidator(
            invalidateGame = a4ShadowWorldSetCache::invalidateGame,
            clearPendingObservation = a4ObservationDurabilityGate::clear,
            cancelObservationRebuild = { a4ObservationCacheRebuildRequest = null },
        )
    }

    fun invalidateA4RevisionScope() {
        a4ShadowLifecycleInvalidator.revisionSuperseded(clocktowerGameId)
    }

    fun invalidateA4SessionBoundary() {
        a4ShadowLifecycleInvalidator.sessionBoundary(clocktowerGameId)
    }

    fun publishClocktowerSessionView() {
        clocktowerSessionView = clocktowerGameSession?.view
    }

    fun requireClocktowerGameSession(): ClocktowerGameSession =
        requireNotNull(clocktowerGameSession) {
            "Clocktower session authority is unavailable."
        }

    fun currentClocktowerCausalJournal(): StorytellerCausalDecisionJournalV1 {
        val session = requireClocktowerGameSession()
        return clocktowerCausalJournals.getOrPut(session.state.gameId) {
            StorytellerCausalDecisionJournalV1(session.state.gameId)
        }
    }

    fun currentTroubleBrewingFirstNightPairDecisionContext(): TroubleBrewingFirstNightPairDecisionContext? {
        if (
            currentGameKind != GameKind.Clocktower ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerPhase != ClocktowerPhase.FirstNight ||
            round != 1
        ) {
            return null
        }
        val session = clocktowerGameSession ?: return null
        val rulesetRef = clocktowerRulesetRef ?: return null
        val registry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = session.toGameSnapshot(rulesetRef),
            phase = StorytellerPhase.FIRST_NIGHT,
            round = round,
            characterRegistry = registry,
        )
        return TroubleBrewingFirstNightPairDecisionContextBuilder.build(
            snapshot = snapshot,
            characterRegistry = registry,
        )
    }

    fun currentTroubleBrewingMayorRedirectPendingDecision(): PendingMayorRedirectDecision? {
        if (
            currentGameKind != GameKind.Clocktower ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerPhase != ClocktowerPhase.Night
        ) {
            return null
        }
        val session = clocktowerGameSession ?: return null
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

    fun advanceClocktowerGameStateRevision() {
        requireClocktowerGameSession().advanceGameStateRevision()
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
    }

    fun advanceClocktowerPlayerInputRevision() {
        requireClocktowerGameSession().recordPlayerInput()
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
    }
    val playerCount = playerNames.size

    fun newClocktowerSeed(): Long = UUID.randomUUID().let { uuid ->
        (uuid.mostSignificantBits xor uuid.leastSignificantBits).takeIf { it != 0L } ?: 1L
    }

    fun troubleBrewingRulesetKnowledge() = runCatching {
        val json = baseContext.assets
            .open("rules/trouble_brewing.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        RulesetJsonLoader.parse(json)
    }.getOrNull()

    fun troubleBrewingRulesetRefFor(basis: ClocktowerRulesetPersistenceBasis): RulesetRef? {
        val knowledge = troubleBrewingRulesetKnowledge() ?: return null
        return runCatching { TroubleBrewingRulesetPersistence.refFor(knowledge, basis) }.getOrNull()
    }

    fun a4InitialIdentityPrewarmRequestOrNull(): A4IdentityRevealPrewarmRequest? {
        val activeRuleset = clocktowerRulesetRef ?: return null
        if (!BuildConfig.DEBUG || currentGameKind != GameKind.Clocktower ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing || cards.size != 5 ||
            cards.any { it.clocktowerRole == null || it.clocktowerShownRole == null }
        ) return null
        val gameState = cards.toClocktowerGameState(
            currentClocktowerScript,
            clocktowerGameSeed,
            poisonedPlayerName = null,
        )
        val snapshot = GameSnapshot(
            gameId = clocktowerGameId,
            gameStateRevision = clocktowerGameStateRevision,
            playerInputRevision = clocktowerPlayerInputRevision,
            gameSeed = clocktowerGameSeed,
            rulesetRef = activeRuleset,
            gameState = gameState,
        )
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, round = 1)
        val perceivedRolesBySeat = cards.mapIndexed { index, card ->
            index + 1 to RoleId(requireNotNull(card.clocktowerShownRole).enName)
        }.toMap()
        return A4IdentityRevealPrewarmRequest(
            formal = formal,
            playerInputRevision = clocktowerPlayerInputRevision,
            knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
                formal = formal,
                perceivedRolesBySeat = perceivedRolesBySeat,
                observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
            ).associateBy(PlayerKnowledgeSnapshot::recipientSeat),
            revealOrder = cards.indices.map { it + 1 },
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
        )
    }

    fun a4ObservationCacheRebuildRequestOrNull(recordId: String): A4ObservationCacheRebuildRequest? {
        val activeRuleset = clocktowerRulesetRef ?: return null
        if (!BuildConfig.DEBUG || currentGameKind != GameKind.Clocktower ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing || cards.size != 5 ||
            cards.any { it.clocktowerRole == null || it.clocktowerShownRole == null }
        ) return null
        val record = clocktowerEpistemicObservations.singleOrNull { it.recordId == recordId } ?: return null
        val gameState = cards.toClocktowerGameState(currentClocktowerScript, clocktowerGameSeed, poisonedPlayerName = null)
        val snapshot = GameSnapshot(
            gameId = clocktowerGameId,
            gameStateRevision = clocktowerGameStateRevision,
            playerInputRevision = clocktowerPlayerInputRevision,
            gameSeed = clocktowerGameSeed,
            rulesetRef = activeRuleset,
            gameState = gameState,
        )
        val formal = FormalGameState.from(snapshot, record.phase, record.round)
        return A4ObservationCacheRebuildRequest(
            formal = formal,
            playerInputRevision = clocktowerPlayerInputRevision,
            perceivedRolesBySeat = cards.mapIndexed { index, card ->
                index + 1 to RoleId(requireNotNull(card.clocktowerShownRole).enName)
            }.toMap(),
            observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
            appendedRecordId = recordId,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
            rollout = A4WorldEngineRollout.ZDD_SHADOW,
        )
    }

    val identityRevealActive = screen == Screen.PassPhone || screen == Screen.RevealCard
    val identityRevealAssignmentFingerprint = cards.joinToString("|") { card ->
        "${card.clocktowerRole?.enName}:${card.clocktowerShownRole?.enName}"
    }
    LaunchedEffect(
        identityRevealActive,
        currentGameKind,
        currentClocktowerScript,
        clocktowerGameId,
        clocktowerGameStateRevision,
        clocktowerPlayerInputRevision,
        clocktowerRulesetRef,
        identityRevealAssignmentFingerprint,
    ) {
        val eligible = BuildConfig.DEBUG && identityRevealActive &&
            currentGameKind == GameKind.Clocktower &&
            currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
            cards.size == 5 && clocktowerRulesetRef != null &&
            cards.all { it.clocktowerRole != null && it.clocktowerShownRole != null }
        if (!eligible) return@LaunchedEffect
        val request = a4InitialIdentityPrewarmRequestOrNull() ?: return@LaunchedEffect
        val session = a4IdentityRevealPrewarmer.start(request)
        val frameTelemetry = A4MainThreadFrameTelemetry()
        val frameMonitor = launch {
            while (isActive) {
                withFrameNanos(frameTelemetry::recordFrame)
            }
        }
        var completedReportLogged = false
        try {
            val report = withContext(Dispatchers.Default) {
                a4IdentityRevealPrewarmer.run(
                    session = session,
                    prioritizedRecipientSeat = currentDealIndex + 1,
                )
            }
            Log.i(A4_IDENTITY_PREWARM_LOG_TAG, report.toLogLine(frameTelemetry.summary()))
            completedReportLogged = true
        } finally {
            frameMonitor.cancel()
            val cancellation = a4IdentityRevealPrewarmer.cancel(session)
            if (cancellation.cancelledEntries > 0) {
                Log.i(A4_IDENTITY_PREWARM_LOG_TAG, cancellation.toLogLine())
            }
            if (!completedReportLogged) {
                Log.i(
                    A4_IDENTITY_PREWARM_LOG_TAG,
                    a4IdentityRevealPrewarmer.report(session).toLogLine(frameTelemetry.summary()),
                )
            }
        }
    }
    LaunchedEffect(a4ObservationCacheRebuildRequest) {
        val request = a4ObservationCacheRebuildRequest ?: return@LaunchedEffect
        val report = withContext(Dispatchers.Default) {
            val workerScope = this
            a4ObservationCacheRebuildExecutor.execute(request) { !workerScope.isActive }
        }
        Log.i(A4_OBSERVATION_CACHE_UPDATE_LOG_TAG, report.toLogLine(request))
    }
    fun storytellerPhaseFor(phase: ClocktowerPhase = clocktowerPhase): StorytellerPhase =
        phase.toStorytellerPhase()

    fun clocktowerSeatFor(playerName: String): Int =
        cards.indexOfFirst { it.name == playerName }
            .takeIf { index -> index >= 0 }
            ?.plus(1)
            ?: error("Unknown Clocktower player '$playerName'.")

    fun currentClocktowerNightCheckpoint(): ClocktowerNightCheckpoint = ClocktowerNightCheckpoint(
        phaseName = clocktowerPhase.name,
        round = round,
        gameStateRevision = clocktowerGameStateRevision,
        playerInputRevision = clocktowerPlayerInputRevision,
        nightStarted = clocktowerNightStartedState.value,
        nightStepIndex = clocktowerNightStepIndexState.value,
        confirmedAttackTarget = clocktowerPendingNightDeath,
        attackDraftTarget = clocktowerDemonAttackDraftTarget,
        confirmedPoisonTarget = clocktowerConfirmedPoisonTarget,
        poisonDraftTarget = clocktowerPoisonTarget,
        confirmedMonkTarget = clocktowerConfirmedMonkProtectedTarget,
        monkDraftTarget = clocktowerMonkProtectedTarget,
        confirmedMayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
        mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
        pendingNewDemonName = clocktowerPendingNewDemonName,
        pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
        demonSuccessorDraftTarget = clocktowerDemonSuccessorTarget,
        confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
    )

    fun clocktowerActionId(
        kind: String,
        actionRound: Int = round,
        localSequence: Int = clocktowerEventCounter + 1,
        targetSeat: Int? = null,
    ): String = buildList {
        add(kind)
        add(clocktowerGameId)
        add(clocktowerGameStateRevision.toString())
        add(actionRound.toString())
        add(localSequence.toString())
        targetSeat?.let { add(it.toString()) }
    }.joinToString("-")

    fun recordClocktowerAction(draft: ActionFactDraft) {
        if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
        requireClocktowerGameSession().commitGlobalActionFact(draft)
        publishClocktowerSessionView()
    }

    /** An announced death is learned at the real Host transition into the Klutz public choice.
     * No replay/backfill from an earlier death or a localized note is permitted.
     */
    fun captureKlutzLearnedDeathOnPublicAnnouncement(name: String) {
        if (currentClocktowerScript != ClocktowerScript.NoGreaterJoy ||
            clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
        val seat = clocktowerSeatFor(name)
        val source = requireClocktowerGameSession()
        NoGreaterJoyKlutzHistoryProducerV1.learned(
            source.state, seat,
            clocktowerActionId(kind = "klutz-learned", targetSeat = seat),
            round, clocktowerEventCounter + 1,
        )?.let(::recordClocktowerAction)
    }

    fun materializeClocktowerPoisonExpiryAtDusk() {
        check(clocktowerPhase == ClocktowerPhase.Day) {
            "Clocktower poison expiry must materialize from the outgoing Day."
        }

        val currentPoisonTargetSeat =
            clocktowerConfirmedPoisonTarget?.let(::clocktowerSeatFor)

        val durablePreviousPoisonTargetSeat =
            currentPoisonTargetSeat
                ?: DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(
                    actionTimeline = clocktowerActionTimeline,
                    round = round,
                )

        val materialization = DuskPoisonExpiryMaterializationPlanner.plan(
            gameId = clocktowerGameId,
            round = round,
            previousTargetSeat = durablePreviousPoisonTargetSeat,
            state = DuskPoisonExpiryMaterializationState(
                currentPoisonTargetSeat = currentPoisonTargetSeat,
                committedActionIds = clocktowerActionTimeline.entries
                    .map { it.fact.actionId }
                    .toSet(),
            ),
        ) ?: return

        materialization.actionIdToCommit?.let { actionId ->
            val localSequence = clocktowerEventCounter + 1
            recordClocktowerAction(
                ActionFactDraft.Poison(
                    actionId = actionId,
                    phase = storytellerPhaseFor(),
                    round = round,
                    sequence = localSequence,
                    targetSeat = null,
                ),
            )
        }

        if (materialization.stateMutationRequired) {
            requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(targetSeat = null)
            publishClocktowerSessionView()
            clocktowerPoisonTarget = null
            clocktowerConfirmedPoisonTarget = null
        }
    }

    fun recordClocktowerPhaseAdvance(
        nextPhase: ClocktowerPhase,
        nextRound: Int = round,
    ) {
        if (nextPhase == clocktowerPhase && nextRound == round) return
        val localSequence = clocktowerEventCounter + 1
        recordClocktowerAction(ActionFactDraft.PhaseAdvance(
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
        ))
    }

    fun recordEpistemicObservation(draft: EpistemicObservationDraft) {
        val session = requireClocktowerGameSession()
        when (clocktowerSemanticHistoryMode) {
            ClocktowerSemanticHistoryMode.LEGACY_LOCAL -> {
                if (clocktowerEpistemicObservations.any { it.recordId == draft.recordId }) return
                session.recordEpistemicObservation(draft.bindLegacyLocal())
                publishClocktowerSessionView()
                invalidateA4RevisionScope()
                a4ObservationDurabilityGate.markPending(draft.recordId)
            }
            ClocktowerSemanticHistoryMode.GLOBAL_V1 -> {
                val beforeRevision = clocktowerPlayerInputRevision
                val committed = session.commitGlobalEpistemicObservation(draft)
                if (session.view.playerInputRevision == beforeRevision) return
                publishClocktowerSessionView()
                invalidateA4RevisionScope()
                a4ObservationDurabilityGate.markPending(committed.recordId)
                if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
                    BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                        strategicPolicyCursor?.version ?: 0,
                        draft.phase.name, draft.round)
                }
            }
        }
    }

    fun commitConfirmedInformationDecision(confirmed: ConfirmedInformationDecision) {
        if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            recordEpistemicObservation(confirmed.draft)
            return
        }
        val session = requireClocktowerGameSession()
        val beforeRevision = session.view.playerInputRevision
        val committed = session.commitGlobalEpistemicObservation(confirmed.draft)
        if (session.view.playerInputRevision == beforeRevision) return
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
        a4ObservationDurabilityGate.markPending(committed.recordId)
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
            BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                strategicPolicyCursor?.version ?: 0,
                confirmed.draft.phase.name, confirmed.draft.round)
        }
    }

    /**
     * Real Host confirmation after player information was published, never an option preview.
     * The shared writer validates the persisted typed observation, complete legal witness,
     * per-subject rules and causal revision before mutating the Host-owned journal.
     */
    fun canCommitConfirmedRegistrationResult(publication: ClocktowerConfirmedRegistrationPublicationV1): Boolean {
        // LEGACY_LOCAL games must continue to display information without claiming a typed
        // historical ruling. The production typed writer itself is GLOBAL_V1-only.
        if (currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1
        ) return true
        val session = clocktowerGameSession ?: return false
        return publication.isRulesConsistent(
            session.state.gameState,
            clocktowerRoleDefinitionsForScript(currentClocktowerScript),
        )
    }

    fun commitConfirmedRegistrationResult(publication: ClocktowerConfirmedRegistrationPublicationV1) {
        if (currentGameKind != GameKind.Clocktower ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1
        ) return
        val session = requireClocktowerGameSession()
        val rulesetRef = requireNotNull(clocktowerRulesetRef)
        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
            gameSnapshot = session.toGameSnapshot(rulesetRef),
            phase = storytellerPhaseFor(clocktowerPhase),
            round = round,
            characterRegistry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry,
        )
        ClocktowerConfirmedRegistrationHostWriterV1.commit(
            publication = publication,
            session = session,
            journal = currentClocktowerCausalJournal(),
            snapshot = snapshot,
            legalRoles = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
        )
    }

    fun preflightClocktowerPublicAliveObservation(
        playerName: String,
        eventSequence: Int,
        eventPhase: ClocktowerPhase = clocktowerPhase,
        eventRound: Int = round,
        recordId: String? = null,
    ) {
        if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
        val seat = cards.indexOfFirst { it.name == playerName }
            .takeIf { index -> index >= 0 }
            ?.plus(1)
            ?: return
        val epistemicPhase = eventPhase.toStorytellerPhase()
        val committed = requireClocktowerGameSession().preflightGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = recordId ?: "public-alive-${clocktowerGameId}-${eventSequence}-$seat",
                phase = epistemicPhase,
                round = eventRound,
                sequence = eventSequence,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(seat, false),
            ),
        )
        check(committed.playerInputRevision != clocktowerPlayerInputRevision) {
            "A new public elimination cannot reuse an existing observation ID."
        }
    }

    fun addClocktowerEvent(
        type: ClocktowerEventType,
        title: String,
        detail: String,
        playerNames: List<String> = emptyList(),
        eventPhase: ClocktowerPhase = clocktowerPhase,
        eventRound: Int = round,
        projectSemanticHistory: Boolean = true,
    ) {
        advanceClocktowerGameStateRevision()
        clocktowerEventCounter += 1
        clocktowerEvents.add(
            ClocktowerEvent(
                sequence = clocktowerEventCounter,
                type = type,
                title = title,
                detail = detail,
                playerNames = playerNames.distinct(),
                phase = eventPhase,
                round = eventRound,
            ),
        )
        if (!projectSemanticHistory) return
        if (type !in setOf(ClocktowerEventType.Death, ClocktowerEventType.Execution)) return
        val eliminatedSeats = playerNames.mapNotNull { playerName ->
            cards.indexOfFirst { it.name == playerName }
                .takeIf { index -> index >= 0 && cards[index].eliminatedRound != null }
                ?.plus(1)
        }.distinct()
        if (eliminatedSeats.isEmpty()) return
        val epistemicPhase = storytellerPhaseFor(eventPhase)
        eliminatedSeats.forEach { seat ->
            when (type) {
                ClocktowerEventType.Execution -> recordClocktowerAction(ActionFactDraft.Execution(
                    actionId = clocktowerActionId(
                        kind = "execution",
                        actionRound = eventRound,
                        localSequence = clocktowerEventCounter,
                        targetSeat = seat,
                    ),
                    phase = epistemicPhase,
                    round = eventRound,
                    sequence = clocktowerEventCounter,
                    targetSeat = seat,
                ))
                ClocktowerEventType.Death -> recordClocktowerAction(ActionFactDraft.Death(
                    actionId = clocktowerActionId(
                        kind = "death",
                        actionRound = eventRound,
                        localSequence = clocktowerEventCounter,
                        targetSeat = seat,
                    ),
                    phase = epistemicPhase,
                    round = eventRound,
                    sequence = clocktowerEventCounter,
                    targetSeat = seat,
                ))
                else -> Unit
            }
        }
        eliminatedSeats.forEach { seat ->
            val observationId = "public-alive-${clocktowerGameId}-${clocktowerEventCounter}-$seat"
            recordEpistemicObservation(EpistemicObservationDraft(
                recordId = observationId,
                phase = epistemicPhase,
                round = eventRound,
                sequence = clocktowerEventCounter,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(seat, false),
            ))
        }
    }

    fun localizedText(zh: String, en: String): String = if (language == "en") en else zh

    fun addOutcomeEvent(outcome: GameOutcome?) {
        if (outcome == null || clocktowerEvents.lastOrNull()?.type == ClocktowerEventType.GameEnd) return
        addClocktowerEvent(
            type = ClocktowerEventType.GameEnd,
            title = outcome.title,
            detail = listOf(outcome.summary, outcome.reason).filter { it.isNotBlank() }.joinToString(" · "),
        )
    }

    fun resetClocktowerNightFlow() {
        clocktowerNightStartedState.value = false
        clocktowerNightStepIndexState.value = 0
    }

    fun resetClocktowerDayFlow() {
        clocktowerDayModeState.value = ClocktowerDayMode.Overview
        clocktowerHighestVoteNameState.value = null
        clocktowerHighestVoteCountState.value = 0
    }

    fun resetClocktowerFlow() {
        resetClocktowerNightFlow()
        resetClocktowerDayFlow()
    }

    fun clearSavedGameState() {
        baseContext.clearActiveGameState()
        recoveryWriteGate.clear()
        savedGamePreview = null
    }

    fun localizedRestoredCard(card: PlayerCard): PlayerCard {
        if (card.clocktowerRole == null || card.clocktowerShownRole == null) return card
        return card.copy(
            roleLabel = card.clocktowerShownRole.nameFor(language),
            actualRoleLabel = card.clocktowerRole.nameFor(language),
            word = context.getString(
                R.string.clocktower_card_desc_format,
                card.clocktowerShownRole.team.label(context),
                card.clocktowerShownRole.descriptionFor(language),
            ),
        )
    }

    fun activeGameRecoverySnapshot(): RecoverySnapshot {
        when (currentGameKind) {
            GameKind.Undercover -> Unit
            GameKind.Clocktower -> {
                ClocktowerActiveSessionValidator.validateForRecoverySave(
                    script = activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript).script,
                    assignedRoleIds = cards.map { card ->
                        RoleId(requireNotNull(card.clocktowerRole) {
                            "Clocktower recovery save is missing an assigned role."
                        }.enName)
                    },
                )
            }
        }
        val entryPoint = when (screen) {
            Screen.PassPhone -> RecoveryEntryPoint.PassPhone
            Screen.RevealCard -> RecoveryEntryPoint.RevealCard
            else -> RecoveryEntryPoint.Stable
        }
        val recoveryDealIndex = if (entryPoint == RecoveryEntryPoint.Stable) 0 else currentDealIndex
        val commonCards = cards.toList()
        val commonRecords = records.toList()
        val sdeHistoricalReplayInputJson = if (currentGameKind == GameKind.Clocktower) {
            val setup = committedClocktowerSetup
            val session = clocktowerGameSession
            val rulesetRef = clocktowerRulesetRef
            if (setup != null && session != null && rulesetRef != null &&
                session.view.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1
            ) {
                runCatching {
                    SdeHistoricalReplayInputJsonCodec.encode(
                        SdeHistoricalReplayInputFactory.captureFresh(
                            committedSetup = setup,
                            currentSnapshot = session.toGameSnapshot(rulesetRef),
                        ).input,
                    )
                }.onFailure { failure ->
                    Log.w(
                        SDE_HISTORICAL_REPLAY_CAPTURE_LOG_TAG,
                        "recovery_replay_capture_failed:${failure::class.java.simpleName}",
                    )
                }.getOrNull()
            } else {
                null
            }
        } else {
            null
        }
        val recoveryGame: RecoveryGame = when (currentGameKind) {
            GameKind.Undercover -> UndercoverRecovery(
                entryPoint = entryPoint,
                currentDealIndex = recoveryDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
            )
            GameKind.Clocktower -> ClocktowerRecovery(
                entryPoint = entryPoint,
                currentDealIndex = recoveryDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
                identity = ClocktowerRecoveryIdentity(
                    script = currentClocktowerScript,
                    gameId = clocktowerGameId,
                    gameSeed = clocktowerGameSeed,
                ),
                troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,
                sdeHistoricalReplayInputJson = sdeHistoricalReplayInputJson,
                position = ClocktowerRecoveryPosition(
                    phase = clocktowerPhase,
                    nightStarted = clocktowerNightStartedState.value,
                    nightStepIndex = clocktowerNightStepIndexState.value,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = clocktowerPendingNightDeath,
                    confirmedPoisonTarget = clocktowerConfirmedPoisonTarget,
                    confirmedMonkProtectedTarget = clocktowerConfirmedMonkProtectedTarget,
                    confirmedMayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
                    pendingNewDemonName = clocktowerPendingNewDemonName,
                    pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
                    confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget,
                    redHerring = clocktowerRedHerring,
                    demonBluffRoleNames = clocktowerRecommendedDemonBluffRoleNames.toList(),
                    butlerMaster = clocktowerButlerMaster,
                    virginUsed = clocktowerVirginUsed,
                    slayerUsed = clocktowerSlayerUsed,
                    slayerClaimedNames = clocktowerSlayerClaimedNames.toList(),
                    artistUsed = clocktowerArtistUsed,
                    artistClaimedNames = clocktowerArtistClaimedNames.toList(),
                    lastExecutedName = clocktowerLastExecutedName,
                    pendingKlutzName = clocktowerPendingKlutzName,
                    klutzReturnToDawn = clocktowerKlutzReturnToDawn,
                    ghostVoteAuthority = clocktowerGhostVoteAuthorityState.value,
                    highestVoteName = clocktowerHighestVoteNameState.value,
                    highestVoteCount = clocktowerHighestVoteCountState.value,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = clocktowerGameStateRevision,
                    playerInputRevision = clocktowerPlayerInputRevision,
                    storytellerPlayerContextBySeat =
                        requireClocktowerGameSession().state.storytellerPlayerContextBySeat,
                    actionTimeline = clocktowerActionTimeline,
                    nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
                    events = clocktowerEvents.toList(),
                    epistemicObservations = clocktowerEpistemicObservations.toList(),
                    causalDecisionJournal = currentClocktowerCausalJournal().archive(),
                ),
            )
        }
        return RecoverySnapshot(
            compatibilityToken = RecoveryCompatibilityToken.currentFor(currentGameKind),
            savedAtMillis = System.currentTimeMillis(),
            game = recoveryGame,
        )
    }

    fun persistActiveGameStateIfNeeded(force: Boolean = false): Boolean {
        if (!screen.isActiveGameScreen() || cards.isEmpty()) return false
        val snapshot = activeGameRecoverySnapshot()
        return recoveryWriteGate.persist(snapshot, force = force) { durableSnapshot ->
            baseContext.saveActiveGameState(RecoverySnapshotJsonCodec.encode(durableSnapshot))
        }
    }

    fun persistAndReleaseA4ObservationRebuildIfDurable(force: Boolean = false) {
        val persisted = persistActiveGameStateIfNeeded(force = force)
        val recordId = a4ObservationDurabilityGate.releaseAfterPersistence(persisted) ?: return
        a4ObservationCacheRebuildRequest = a4ObservationCacheRebuildRequestOrNull(recordId)
    }

    fun applyValidatedRecoveryPlan(plan: ValidatedRecoveryPlan) {
        val game = plan.snapshot.game
        val restoredCards = game.cards.map(::localizedRestoredCard)
        val restoredPlayerNames = restoredCards.map(PlayerCard::name)

        playerNames.clear()
        playerNames.addAll(restoredPlayerNames)
        hostSeatingSetupFlow = HostSeatingSetupFlow.recoveredActiveGame(
            playerNames = restoredPlayerNames,
            game = game.gameKind,
        )
        cards.clear()
        cards.addAll(restoredCards)
        records.clear()
        records.addAll(game.records)

        currentGameKind = game.gameKind
        currentDealIndex = game.currentDealIndex
        round = game.round
        gameOutcome = game.outcome
        showResults = plan.presentResults
        savedGamePreview = null
        showHostTools = false
        showNewGameConfirmation = false

        undercoverCount = 1
        includeBlank = false
        selectedElimination = null

        selectedClocktowerScript = null
        clocktowerGameSession = null
        publishClocktowerSessionView()
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        clocktowerRulesetRef = null
        clocktowerPhase = ClocktowerPhase.FirstNight
        clocktowerNightStartedState.value = false
        clocktowerNightStepIndexState.value = 0

        clocktowerEvents.clear()
        clocktowerEventCounter = 0

        clocktowerPendingNightDeath = null
        clocktowerDemonAttackDraftTarget = null
        clocktowerSelectedExecution = null
        clocktowerPoisonTarget = null
        clocktowerConfirmedPoisonTarget = null
        clocktowerFortuneTellerFirst = null
        clocktowerFortuneTellerSecond = null
        clocktowerChambermaidFirst = null
        clocktowerChambermaidSecond = null
        clocktowerRavenkeeperTarget = null
        clocktowerRedHerring = null
        clocktowerRecommendedDemonBluffRoleNames = emptyList()
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerConfirmedMonkProtectedTarget = null
        clocktowerMayorRedirectTarget = null
        clocktowerConfirmedMayorRedirectTarget = null
        clocktowerPendingNewDemonName = null
        clocktowerPendingNightNewDemonIdentityName = null
        clocktowerDemonSuccessorTarget = null
        clocktowerConfirmedDemonSuccessorTarget = null
        clocktowerVirginUsed = false
        clocktowerSlayerUsed = false
        clocktowerSlayerClaimedNames = emptyList()
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerLastExecutedName = null
        clocktowerPendingKlutzName = null
        clocktowerKlutzChoiceName = null
        clocktowerKlutzReturnToDawn = false

        clocktowerDayModeState.value = ClocktowerDayMode.Overview
        clocktowerGhostVoteAuthorityState.value = ClocktowerGhostVoteAuthority()
        clocktowerHighestVoteNameState.value = null
        clocktowerHighestVoteCountState.value = 0

        when (game) {
            is UndercoverRecovery -> {
                undercoverCount = game.cards.count { it.role == Role.Undercover }
                includeBlank = game.cards.any { it.role == Role.Blank }
            }
            is ClocktowerRecovery -> {
                val runtime = plan.clocktowerRuntime
                val safeClocktower = plan.safeReentry as? RecoverySafeReentry.ClocktowerJudge
                val mechanics = game.mechanics
                val history = game.history

                committedTroubleBrewingSetupRotationRecord =
                    game.troubleBrewingSetupRotationRecord
                val recoveredGameState = restoredCards.toClocktowerGameState(
                    script = game.identity.script,
                    seed = game.identity.gameSeed,
                    poisonedPlayerName = mechanics.confirmedPoisonTarget,
                )
                clocktowerGameSession = ClocktowerGameSession.restoreProduction(
                    ClocktowerSessionState(
                        gameId = game.identity.gameId,
                        gameStateRevision = history.gameStateRevision,
                        playerInputRevision = history.playerInputRevision,
                        gameSeed = game.identity.gameSeed,
                        gameState = recoveredGameState,
                        storytellerPlayerContextBySeat = history.storytellerPlayerContextBySeat,
                        actionTimeline = history.actionTimeline,
                        epistemicObservationLog = EpistemicObservationLog(history.epistemicObservations),
                        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                        nextTimelineGlobalSequence = history.nextTimelineGlobalSequence,
                    ),
                )
                publishClocktowerSessionView()
                clocktowerCausalJournals[game.identity.gameId] =
                    history.causalDecisionJournal?.let { archive ->
                        StorytellerCausalDecisionJournalV1.restore(archive, requireClocktowerGameSession().state)
                    } ?: StorytellerCausalDecisionJournalV1(game.identity.gameId)
                clocktowerRulesetRef = runtime?.rulesetRef
                committedClocktowerSetup =
                    runtime?.sdeReplayMaterialization?.input?.toCommittedSetup()
                clocktowerPhase = safeClocktower?.phase ?: game.position.phase
                clocktowerNightStartedState.value = game.position.nightStarted
                clocktowerNightStepIndexState.value =
                    safeClocktower?.nightStepIndex ?: game.position.nightStepIndex

                clocktowerEvents.addAll(history.events)
                clocktowerEventCounter = history.events.maxOfOrNull(ClocktowerEvent::sequence) ?: 0

                clocktowerPendingNightDeath = mechanics.confirmedAttackTarget
                clocktowerConfirmedPoisonTarget = mechanics.confirmedPoisonTarget
                clocktowerConfirmedMonkProtectedTarget = mechanics.confirmedMonkProtectedTarget
                clocktowerConfirmedMayorRedirectTarget = mechanics.confirmedMayorRedirectTarget
                clocktowerPendingNewDemonName = mechanics.pendingNewDemonName
                clocktowerPendingNightNewDemonIdentityName = mechanics.pendingNightNewDemonIdentityName
                clocktowerConfirmedDemonSuccessorTarget = mechanics.confirmedDemonSuccessorTarget
                clocktowerRedHerring = mechanics.redHerring
                clocktowerRecommendedDemonBluffRoleNames = mechanics.demonBluffRoleNames
                clocktowerButlerMaster = mechanics.butlerMaster
                clocktowerVirginUsed = mechanics.virginUsed
                clocktowerSlayerUsed = mechanics.slayerUsed
                clocktowerSlayerClaimedNames = mechanics.slayerClaimedNames
                clocktowerArtistUsed = mechanics.artistUsed
                clocktowerArtistClaimedNames = mechanics.artistClaimedNames
                clocktowerLastExecutedName = mechanics.lastExecutedName
                clocktowerPendingKlutzName = mechanics.pendingKlutzName
                clocktowerKlutzReturnToDawn = mechanics.klutzReturnToDawn
                clocktowerGhostVoteAuthorityState.value = mechanics.ghostVoteAuthority
                clocktowerHighestVoteNameState.value = mechanics.highestVoteName
                clocktowerHighestVoteCountState.value = mechanics.highestVoteCount
                clocktowerDayModeState.value =
                    if (safeClocktower?.continuation == ClocktowerRecoveryContinuation.Klutz) {
                        ClocktowerDayMode.Klutz
                    } else {
                        ClocktowerDayMode.Overview
                    }
            }
        }

        screen = when (plan.safeReentry) {
            RecoverySafeReentry.UndercoverGame -> Screen.Game
            is RecoverySafeReentry.PassPhone -> Screen.PassPhone
            is RecoverySafeReentry.RevealCard -> Screen.RevealCard
            is RecoverySafeReentry.ClocktowerJudge -> Screen.ClocktowerJudge
        }
    }

    fun restoreSavedGame() {
        // Recovery has Host facts but no verified provider cursor. Fall back to
        // complete Host snapshot rather than attaching to an untrusted old ID.
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        RecoveryApplicationCoordinator.apply(
            raw = baseContext.loadActiveGameStateJson(),
            prepare = { raw -> baseContext.prepareCurrentRecoveryPlan(raw) },
            clearRejected = ::clearSavedGameState,
            crossSessionBoundary = ::invalidateA4SessionBoundary,
            applyValidated = ::applyValidatedRecoveryPlan,
        )
    }

    val latestPersistActiveGameState by rememberUpdatedState { force: Boolean ->
        persistAndReleaseA4ObservationRebuildIfDurable(force = force)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            persistRecoveryForLifecycleEvent(event, latestPersistActiveGameState)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SideEffect {
        persistAndReleaseA4ObservationRebuildIfDurable()
    }

    fun maxUndercoverFor(count: Int): Int {
        return ((if (includeBlank) count - 2 else count - 1).coerceAtLeast(1))
    }

    fun clampUndercoverCount() {
        undercoverCount = undercoverCount.coerceIn(1, maxUndercoverFor(playerNames.size))
    }

    fun addCurrentPlayer(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty() && playerNames.size < MAX_PLAYERS && trimmedName !in playerNames) {
            playerNames.add(trimmedName)
            clampUndercoverCount()
        }
    }

    fun removeCurrentPlayer(index: Int) {
        if (index in playerNames.indices) {
            playerNames.removeAt(index)
            clampUndercoverCount()
        }
    }

    fun moveCurrentPlayerTo(index: Int, targetIndex: Int) {
        if (index !in playerNames.indices || targetIndex !in playerNames.indices) return
        if (index == targetIndex) return
        val reordered = reorderHostTableItems(
            items = playerNames,
            fromIndex = index,
            targetIndex = targetIndex,
        )
        playerNames.clear()
        playerNames.addAll(reordered)
    }

    fun addCommonPlayer() {
        val trimmedName = newCommonPlayerName.trim()
        if (trimmedName.isNotEmpty() && trimmedName !in commonPlayers) {
            commonPlayers.add(trimmedName)
            appPreferencesStore.saveCommonPlayers(commonPlayers)
            newCommonPlayerName = ""
        }
    }

    fun removeCommonPlayer(name: String) {
        commonPlayers.remove(name)
        appPreferencesStore.saveCommonPlayers(commonPlayers)
    }

    fun resetDealState(
        nextGameKind: GameKind,
        clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
        preparedClocktowerSeed: Long? = null,
        preparedClocktowerInitialState: GameState? = null,
        preparedClocktowerGameId: String? = null,
        persistInitialState: Boolean = true,
    ) {
        invalidateA4SessionBoundary()
        clearSavedGameState()
        pendingTroubleBrewingDrunkSelection = null
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        currentGameKind = nextGameKind
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        records.clear()
        clocktowerEvents.clear()
        clocktowerGameSession = null
        publishClocktowerSessionView()
        clocktowerEventCounter = 0
        currentDealIndex = 0
        round = 1
        showResults = false
        gameOutcome = null
        selectedElimination = null
        clocktowerPhase = ClocktowerPhase.FirstNight
        if (nextGameKind == GameKind.Clocktower) {
            val gameId = preparedClocktowerGameId ?: UUID.randomUUID().toString()
            require(gameId.isNotBlank()) { "Prepared Clocktower game ID cannot be blank." }
            val gameSeed = preparedClocktowerSeed ?: newClocktowerSeed()
            val initialGameState = preparedClocktowerInitialState
                ?.also { preparedState ->
                    require(preparedState.seed == gameSeed) {
                        "Prepared Clocktower state seed must match the prepared game seed."
                    }
                    require(preparedState.script == clocktowerScript.toRecommendationScriptId()) {
                        "Prepared Clocktower state script must match the selected script."
                    }
                }
                ?: cards.toClocktowerGameState(
                    script = clocktowerScript,
                    seed = gameSeed,
                    poisonedPlayerName = null,
                )
            clocktowerGameSession = ClocktowerGameSession.createProduction(
                gameId = gameId,
                gameSeed = gameSeed,
                initialState = initialGameState,
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                initialPlayerContextBySeat = cards.mapIndexedNotNull { index, card ->
                    val level = playerExperience(card.name)
                    if (level == PlayerExperienceLevelV1.BEGINNER) null
                    else (index + 1) to StorytellerPlayerContextInputV1(experienceLevel = level)
                }.toMap(),
            )
            publishClocktowerSessionView()
            if (clocktowerScript == ClocktowerScript.TroubleBrewing) {
                val rulesetBasis = ClocktowerRulesetPersistenceBasis(
                    cards.map { card ->
                        RoleId(requireNotNull(card.clocktowerRole) {
                            "Trouble Brewing setup is missing an assigned role."
                        }.enName)
                    }.toSet(),
                )
                clocktowerRulesetRef = troubleBrewingRulesetRefFor(rulesetBasis)
                    ?: error("Unable to resolve Trouble Brewing ruleset reference at setup.")
            } else {
                clocktowerRulesetRef = null
            }
        } else {
            clocktowerRulesetRef = null
        }
        clocktowerPendingNightDeath = null
        clocktowerDemonAttackDraftTarget = null
        clocktowerSelectedExecution = null
        clocktowerPoisonTarget = null
        clocktowerConfirmedPoisonTarget = null
        clocktowerFortuneTellerFirst = null
        clocktowerFortuneTellerSecond = null
        clocktowerChambermaidFirst = null
        clocktowerChambermaidSecond = null
        clocktowerRavenkeeperTarget = null
        clocktowerRedHerring = null
        clocktowerRecommendedDemonBluffRoleNames = emptyList()
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerConfirmedMonkProtectedTarget = null
        clocktowerMayorRedirectTarget = null
        clocktowerConfirmedMayorRedirectTarget = null
        clocktowerPendingNewDemonName = null
        clocktowerPendingNightNewDemonIdentityName = null
        clocktowerDemonSuccessorTarget = null
        clearConfirmedDemonSuccessorTarget()
        clocktowerVirginUsed = false
        clocktowerSlayerUsed = false
        clocktowerSlayerClaimedNames = emptyList()
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerLastExecutedName = null
        clocktowerPendingKlutzName = null
        clocktowerKlutzChoiceName = null
        clocktowerKlutzReturnToDawn = false
        clocktowerGhostVoteAuthorityState.value = ClocktowerGhostVoteAuthority()
        resetClocktowerFlow()
        screen = Screen.PassPhone
        if (persistInitialState) {
            persistActiveGameStateIfNeeded()
        }
    }

    fun startUndercoverGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Undercover)
        if (playerNames.size < MIN_PLAYERS) return
        val pair = wordPairsFor(language).random()
        val blankCount = if (includeBlank) 1 else 0
        val roles = buildList {
            repeat(undercoverCount) { add(Role.Undercover) }
            repeat(blankCount) { add(Role.Blank) }
            repeat(playerNames.size - undercoverCount - blankCount) { add(Role.Civilian) }
        }.shuffled()

        cards.clear()
        cards.addAll(playerNames.mapIndexed { index, name ->
            val role = roles[index]
            val word = when (role) {
                Role.Civilian -> pair.civilianWord
                Role.Undercover -> pair.undercoverWord
                Role.Blank -> context.getString(R.string.blank_word)
            }
            PlayerCard(name = name.ifBlank { context.playerName(index + 1) }, role = role, word = word)
        })
        resetDealState(GameKind.Undercover)
    }

    fun adoptWholeResidualProposal(steps: List<ClocktowerNightStepUi>, stepIndex: Int) {
        if (storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            residualProposedKey != liveAiAdviceKey) return
        val proposal = residualProposedPackage ?: return
        val latest = clocktowerGameSession?.state ?: return
        val position = residualProposedPosition ?: return
        if (proposal.gameId != latest.gameId ||
            proposal.gameStateRevision != latest.gameStateRevision ||
            proposal.playerInputRevision != latest.playerInputRevision ||
            proposal.exclusiveGlobalSequence != latest.nextTimelineGlobalSequence ||
            position.phase != clocktowerPhase.toStorytellerPhase() ||
            position.round != round ||
            clocktowerNightStepIndexState.value != stepIndex) return
        // Regenerate the ENTIRE pending domain against the latest Host and UI
        // action inputs. Revision equality alone cannot authorize a changed
        // registration explanation or manually altered current candidate pool.
        val sameScope = runCatching {
            val session = requireClocktowerGameSession()
            val rulesetRef = requireNotNull(clocktowerRulesetRef)
            val registry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
            val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                session.toGameSnapshot(rulesetRef), position.phase, position.round, registry)
            val revision = StorytellerProviderRevisionV1(
                latest.gameStateRevision, latest.playerInputRevision)
            val context = StorytellerProviderGameContextBuilderV1.build(
                snapshot, revision, latest)
            val step = steps[stepIndex]
            val mayor = if (position.phase == StorytellerPhase.NIGHT &&
                step.action == ClocktowerNightAction.MayorRedirect)
                currentTroubleBrewingMayorRedirectPendingDecision() else null
            val scope = ClocktowerCurrentResidualScopeV2.collect(
                latest, position.phase, position.round, steps, stepIndex, cards,
                snapshot, context, currentTroubleBrewingFirstNightPairDecisionContext(),
                listOfNotNull(clocktowerFortuneTellerFirst, clocktowerFortuneTellerSecond),
                mayor, redHerringAlreadyChosen = clocktowerRedHerring != null)
            val open = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
                latest, position.phase, position.round, scope.requests)
            open == residualProposedOpen &&
                (storytellerOperationMode != StorytellerOperationMode.AI_AUTOMATIC ||
                    ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
                        scope.requests, proposal.choices, steps, cards))
        }.getOrDefault(false)
        if (!sameScope) {
            residualProposedPackage = null
            residualProposedKey = null
            liveAiAdviceError = "Current Host legal decisions changed; regenerate the whole plan."
            DebugFlightRecorder.record("RESIDUAL_WHOLE_ADOPTION_STALE")
            BotcAiWireTrace.residualGate("STALE", proposal.choices.size,
                strategicPolicyCursor?.version ?: 0, position.phase.name, position.round)
            return
        }
        // Adopting is a private preselection for still-open steps, not a
        // publication. Every step must re-match its current Host-legal option.
        residualAdoptedPackage = proposal
        residualAdoptedOperationMode = storytellerOperationMode
        DebugFlightRecorder.record("RESIDUAL_WHOLE_ADOPTED", mapOf(
            "choiceCount" to proposal.choices.size.toString()))
        BotcAiWireTrace.residualGate("PRIVATE_ADOPTED", proposal.choices.size,
            strategicPolicyCursor?.version ?: 0, position.phase.name, position.round)
    }

    fun requestLiveGlobalAdvice(steps: List<ClocktowerNightStepUi>, stepIndex: Int) {
        val step = steps.getOrNull(stepIndex) ?: return
        if (storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            screen != Screen.ClocktowerJudge ||
            currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            clocktowerPhase !in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night)
        ) return
        val session = clocktowerGameSession ?: return
        val requestedOperationMode = storytellerOperationMode
        val context = currentTroubleBrewingFirstNightPairDecisionContext()
        val pendingPair = context?.let {
            pendingGlobalFirstNightPairDecision(
                step, clocktowerPhase, round, cards, it,
            )
        }
        val pendingScalar = if (clocktowerPhase in setOf(
                ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
            pendingPair == null
        ) pendingGlobalNightScalarDecision(
            step, clocktowerPhase, round, stepIndex, cards, session.state.gameId,
            StorytellerDecisionRevision(
                session.state.gameStateRevision, session.state.playerInputRevision,
            ),
            listOfNotNull(clocktowerFortuneTellerFirst, clocktowerFortuneTellerSecond),
        ) else null
        // The Host supplies a currently legal decision. The LLM uses the same
        // global strategy for pair, numeric/Boolean and later-night target decisions.
        val pendingTarget = if (clocktowerPhase == ClocktowerPhase.Night &&
            step.action == ClocktowerNightAction.MayorRedirect && step.isRealAction
        ) runCatching { currentTroubleBrewingMayorRedirectPendingDecision() }.getOrNull()
        else null
        val identity = pendingPair?.requestIdentity ?: pendingScalar?.requestIdentity
            ?: pendingTarget?.requestIdentity ?: return
        val revision = StorytellerProviderRevisionV1(
            session.state.gameStateRevision, session.state.playerInputRevision,
        )
        val legalIds = pendingPair?.legalCandidates?.map { it.candidateId }
            ?: pendingScalar?.legalCandidates?.map { it.candidateId }
            ?: pendingTarget?.pending?.legalCandidates?.map { it.candidateId }
            ?: return
        val key = ClocktowerResidualRequestIdentityV2.build(
            session.state.gameId, round, stepIndex, identity.requestId,
            revision.gameStateRevision, revision.playerInputRevision,
            session.state.nextTimelineGlobalSequence, legalIds, requestedOperationMode,
        )
        if (liveAiAdviceKey == key && (liveAiAdviceBusy || liveAiAdviceText != null)) return
        liveAiAdviceKey = key
        residualProposedPackage = null
        residualProposedKey = null
        residualProposedOpen = emptyList()
        residualProposedPosition = null
        liveAiAdviceText = null
        liveAiAdviceError = null
        // An older in-flight response belongs to a different key and cannot own this state.
        liveAiAdviceBusy = false
        if (!aiEndpoint.startsWith("https://") ||
            aiToken.isBlank()
        ) {
            liveAiAdviceError = "AI unavailable: configure the selected AI connection."
            return
        }
        val providerRequest = runCatching {
            val snapshot = context?.snapshot ?: run {
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
                    pendingScalar.legalCandidates, snapshot, gameContext,
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
        val wholeOpening = adoptedJointOpening
        val wholeCursor = strategicPolicyCursor
        val wholeBaseline = strategicLastReviewedHost
        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC &&
            (!personalDirectEnabled || wholeOpening == null ||
                wholeCursor == null || wholeBaseline == null)
        ) {
            liveAiAdviceError = "Whole-game strategy is not available; automatic AI paused. Use Host manual decisions."
            DebugFlightRecorder.record("RESIDUAL_AUTO_NO_WHOLE_CONTEXT")
            return
        }
        if (personalDirectEnabled && wholeOpening != null &&
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
                    pairContext = context,
                    fortuneTellerSelectedNames = listOfNotNull(
                        clocktowerFortuneTellerFirst, clocktowerFortuneTellerSecond),
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
                    session.state, phasePosition.phase, round, remainingScope.requests)
            }.getOrElse {
                liveAiAdviceError = "Host rejected pending residual decisions."
                return
            }
            val review = runCatching {
                StorytellerStrategicChangeCheckpointV2.review(
                    wholeBaseline, currentHost, wholeCursor, legalOpen)
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
                // Initial adopted joint opening already covers the unchanged state.
                liveAiAdviceText = if (review.readiness ==
                    StorytellerStrategicChangeCheckpointV2.Readiness.NO_MATERIAL_CHANGE
                ) "沿用首次整局配置；尚无新的重要 Host 变化。" else
                    "当前没有可联合更新的合法未执行决定；后续行动发生后再评估。"
                return
            }
            // Host owns any already-determined result across ALL information
            // roles/nights. A different candidate ID or registration witness
            // does not create an AI decision when the public result is identical.
            if (!StorytellerStrategicChangeCheckpointV2.pendingNeedsJudgment(legalOpen)) {
                DebugFlightRecorder.record("RESIDUAL_MODEL_SKIP", mapOf(
                    "readiness" to "HOST_DETERMINED",
                    "openDecisions" to legalOpen.size.toString()))
                BotcAiWireTrace.residualGate("HOST_DETERMINED", legalOpen.size,
                    wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                liveAiAdviceText = "当前未公布的信息结果由 Host 规则确定；无需调用 AI，沿用已有整局战略。"
                return
            }
            // A single model call covers EVERY genuinely open discretionary
            // decision in this causal segment. Later player action is unknown.
            liveAiAdviceBusy = true
            DebugFlightRecorder.record("RESIDUAL_WHOLE_REQUEST", mapOf(
                "openDecisions" to legalOpen.size.toString(),
                "barrier" to (remainingScope.barrierReason ?: "NONE"),
                "memoryVersion" to wholeCursor.version.toString()))
            BotcAiWireTrace.residualGate("REQUEST", legalOpen.size,
                wholeCursor.version, phasePosition.phase.name, phasePosition.round)
            // This is only a provider context hint; the Host frozen snapshot
            // remains complete and authoritative even when a chain is usable.
            val requestedModel = requireNotNull(aiDirectModel)
            val requestedToken = aiToken
            val previousResponseId = canonicalConversationResponseId?.takeIf {
                canonicalConversationModel == requestedModel
            }
            drunkAiScope.launch {
                // Coalesce rapid related Host updates before paying for a new
                // whole-game request. A newer state/key cancels this pending call.
                delay(450)
                if (liveAiAdviceKey != key || clocktowerGameSession !== session ||
                    session.state !== currentHost.state ||
                    strategicPolicyCursor != wholeCursor ||
                    storytellerOperationMode != requestedOperationMode ||
                    aiDirectModel != requestedModel || aiToken != requestedToken ||
                    clocktowerNightStepIndexState.value != stepIndex) return@launch
                val result = runCatching {
                    StorytellerStrategicContinuationV2.recommendWithReceipt(
                        PersonalDirectOpenAiV1.ENDPOINT, requestedToken, requestedModel,
                        session, wholeOpening, legalOpen, wholeCursor, previousResponseId,
                    )
                }
                if (liveAiAdviceKey != key || clocktowerGameSession !== session ||
                    session.state !== currentHost.state ||
                    clocktowerNightStepIndexState.value != stepIndex ||
                    clocktowerPhase.toStorytellerPhase() != phasePosition.phase ||
                    round != phasePosition.round ||
                    strategicPolicyCursor != wholeCursor ||
                    storytellerOperationMode != requestedOperationMode ||
                    aiDirectModel != requestedModel || aiToken != requestedToken
                ) return@launch
                liveAiAdviceBusy = false
                result.onSuccess { receipt ->
                    val proposal = receipt.proposal
                    runCatching {
                        val verified = StorytellerResidualPackageGateV2.verify(
                                session.state, phasePosition.phase, phasePosition.round,
                                legalOpen, remainingScope.requests, proposal, wholeCursor)
                        val advanced = StorytellerStrategicChangeCheckpointV2.accept(
                            review, currentHost, proposal, wholeCursor)
                        verified to advanced
                    }.onSuccess acceptedResidual@{ (verified, advanced) ->
                        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC &&
                            !ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
                                remainingScope.requests, verified.choices, steps, cards)
                        ) {
                            // The model decision is legal but not safely executable by
                            // the current Host presentation. NEVER advance memory or
                            // publish a partial automatic package.
                            liveAiAdviceError = "Whole plan has an unresolved Host decision; manual takeover required."
                            residualAdoptedPackage = null
                            residualAdoptedOperationMode = null
                            DebugFlightRecorder.record("RESIDUAL_AUTO_UNPRESENTABLE")
                            BotcAiWireTrace.residualGate("AUTO_PAUSED", verified.choices.size,
                                wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                            return@acceptedResidual
                        }
                        residualProposedPackage = verified
                        residualProposedKey = key
                        residualProposedOpen = legalOpen
                        residualProposedPosition = phasePosition
                        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC) {
                            // Recompute the current Host domain at the actual adoption
                            // boundary, before accepting the model's memory revision.
                            adoptWholeResidualProposal(steps, stepIndex)
                            if (residualAdoptedPackage != verified) {
                                residualProposedPackage = null
                                residualProposedKey = null
                                residualAdoptedPackage = null
                                residualAdoptedOperationMode = null
                                liveAiAdviceError = "Automatic plan became stale; manual takeover required."
                                DebugFlightRecorder.record("RESIDUAL_AUTO_PAUSED")
                                BotcAiWireTrace.residualGate("AUTO_PAUSED", verified.choices.size,
                                    wholeCursor.version, phasePosition.phase.name, phasePosition.round)
                                return@acceptedResidual
                            }
                        }
                        strategicPolicyCursor = advanced
                        strategicLastReviewedHost = currentHost
                        // An assisted suggestion is not an adoption: it MUST NOT
                        // advance the provider chain. Automatic mode promotes
                        // only after the complete private Host preselection gate.
                        if (requestedOperationMode == StorytellerOperationMode.AI_AUTOMATIC &&
                            residualAdoptedPackage == verified) {
                            canonicalConversationResponseId = receipt.responseId
                            canonicalConversationModel = requestedModel
                        }
                        BotcAiWireTrace.residualGate("MEMORY_ADVANCED", verified.choices.size,
                            advanced.version, phasePosition.phase.name, phasePosition.round)
                        liveAiAdviceText = buildString {
                            append("本次整局续规划：")
                            append(verified.choices.size)
                            append(" 项当前合法选择（尚未发布，必须在 Host 确认）。")
                            verified.choices.forEach { (id, candidateId) ->
                                val decision = legalOpen.single { it.decisionId == id }
                                val meaning = decision.legalCandidates.single {
                                    it.candidateId == candidateId
                                }.payload
                                append("\n")
                                append(decision.abilityRoleId ?: decision.family)
                                append("（")
                                append(decision.sourceSeat ?: "-")
                                append("号）：")
                                append(meaning.toString().take(160))
                            }
                            append("\n")
                            append(proposal.strategicDirection.take(300))
                            remainingScope.barrierReason?.let {
                                append("\n尚未发生的玩家行动之后的决定仍保留为条件计划。")
                            }
                        }
                        liveAiAdviceError = null
                        DebugFlightRecorder.record("RESIDUAL_WHOLE_ACCEPTED", mapOf(
                            "choiceCount" to verified.choices.size.toString(),
                            "memoryVersion" to advanced.version.toString()))
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

    fun commitAndStartTroubleBrewingGame(
        preparedGameId: String,
        preparedSetup: TroubleBrewingPreparedSetup,
        confirmedDrunkCandidate: TroubleBrewingDrunkCandidate?,
    ) {
        require(preparedGameId.isNotBlank()) { "Trouble Brewing prepared game ID cannot be blank." }
        pendingTroubleBrewingDrunkSelection = null
        val preparedSeed = preparedSetup.intermediateSetup.gameSeed
        val characterRegistry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        val committedSetup = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = preparedSetup.intermediateSetup,
            confirmedDrunkCandidate = confirmedDrunkCandidate,
            characterRegistry = characterRegistry,
        )
        val resolvedAssignments = TroubleBrewingDealRoleResolver.resolveCommitted(
            committedSetup = committedSetup,
            characterRegistry = characterRegistry,
            availableRoles = completeTroubleBrewingRoles,
        )

        val committedCards = resolvedAssignments.map { assignment ->
            val role = assignment.actualRole
            val shownRole = assignment.shownRole

            PlayerCard(
                name = assignment.playerName.ifBlank {
                    context.playerName(assignment.seat)
                },
                role = Role.Civilian,
                roleLabel = shownRole.nameFor(language),
                actualRoleLabel = role.nameFor(language),
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shownRole,
                word = context.getString(
                    R.string.clocktower_card_desc_format,
                    shownRole.team.label(context),
                    shownRole.descriptionFor(language),
                ),
            )
        }

        cards.clear()
        cards.addAll(committedCards)

        resetDealState(
            nextGameKind = GameKind.Clocktower,
            clocktowerScript = ClocktowerScript.TroubleBrewing,
            preparedClocktowerSeed = preparedSeed,
            preparedClocktowerInitialState = committedSetup.gameState,
            preparedClocktowerGameId = preparedGameId,
            persistInitialState = false,
        )
        committedTroubleBrewingSetupRotationRecord =
            TroubleBrewingSetupRotationRecordFactory.fromCommittedSetup(
                preparedSetup = preparedSetup,
                committedSetup = committedSetup,
                characterRegistry = characterRegistry,
            )
        committedClocktowerSetup = committedSetup.committedSetup
        persistActiveGameStateIfNeeded()
        currentTroubleBrewingFirstNightPairDecisionContext()?.let { request ->
            troubleBrewingFirstNightPrecomputeCoordinator.prewarm(
                request = request,
                launchBackground = { work ->
                    troubleBrewingFirstNightPrecomputeScope.launch(Dispatchers.Default) {
                        work()
                    }
                },
            )
        }
    }


    /** 3C: the only INITIAL paid model request for personal-direct TB opening. */
    fun requestJointOpening(pending: PendingTroubleBrewingJointOpening) {
        if (pendingJointOpening !== pending || jointOpeningBusy ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            !personalDirectEnabled) return
        if (aiToken.isBlank() || !aiEndpoint.startsWith("https://")) {
            jointOpeningError = "AI credentials unavailable. You can continue manually."
            return
        }
        val requestedMode = storytellerOperationMode
        val requestedKey = aiToken
        val requestedModel = aiDirectModel ?: PersonalDirectOpenAiV1.DEFAULT_MODEL
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
                    domain = pending.domain,
                    registry = registry,
                    model = requestedModel,
                    onStage = { stage -> BotcAiTrace.firstNightStage(serial, stage) },
                )
            }
            // Precommit has NO committed session. Never compare against a
            // fake session state; bind directly to this immutable pending game.
            if (serial != jointOpeningSerial || pendingJointOpening !== pending ||
                storytellerOperationMode != requestedMode || !personalDirectEnabled ||
                screen !in setOf(Screen.ClocktowerJointPass,
                    Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)
            ) return@launch
            result.onSuccess { receipt ->
                if (receipt.accepted.selection.validFor(pending.domain.scope)) {
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

    fun adoptJointOpeningAfterReveal() {
        val pending = pendingJointOpening ?: return
        val accepted = jointOpeningResult ?: return
        if (screen != Screen.ClocktowerJointReview ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            !accepted.selection.validFor(pending.domain.scope)
        ) return
        val selectedDrunk = accepted.selection.drunkSeat
        val route = TroubleBrewingDrunkSelectionRouter.route(
            preparedSetup = pending.preparedSetup,
            recommendedCandidate = null,
        )
        val candidate = when (route) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                require(selectedDrunk == null)
                null
            }
            is TroubleBrewingDrunkSelectionRoute.RuleDetermined -> {
                require(selectedDrunk == route.candidate.seat)
                route.candidate
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection ->
                requireNotNull(route.request.candidates.singleOrNull {
                    it.seat == selectedDrunk
                }) { "AI Drunk is not in current Host legal domain." }
        }
        // The full model proposal is already Host-validated. Commit the
        // actual Drunk only NOW, after all shown roles were revealed.
        jointOpeningSerial += 1
        jointOpeningBusy = false
        pendingJointOpening = null
        jointOpeningApplied = true
        adoptedJointOpening = accepted
        strategicPolicyCursor = accepted.strategyMemory?.let {
            StorytellerStrategicContinuationV2.MemoryCursor.initial(accepted)
        }
        val projection = pending.domain.forDrunkSeat(selectedDrunk)
        adoptedJointInformation = accepted.selection.choices.mapNotNull { (id, option) ->
            projection.candidatesByDecision[id]?.get(option)?.let { meaning ->
                meaning.sourceSeat to meaning
            }
        }.toMap()
        commitAndStartTroubleBrewingGame(
            preparedGameId = pending.preparedGameId,
            preparedSetup = pending.preparedSetup,
            confirmedDrunkCandidate = candidate,
        )
        // Adopt only the Host-accepted opening as this game's chain head.
        canonicalConversationResponseId = pendingOpeningResponseId
        canonicalConversationModel = pendingOpeningModel
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        strategicLastReviewedHost = clocktowerGameSession?.let { committedSession ->
            StorytellerStrategicChangeCheckpointV2.capture(committedSession,
                StorytellerStrategicChangeCheckpointV2.Position(
                    clocktowerPhase.toStorytellerPhase(), round))
        }
        val registry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        accepted.selection.demonBluffs?.let { externalIds ->
            clocktowerRecommendedDemonBluffRoleNames = externalIds.map { role ->
                requireNotNull(registry.findByExternalId(role)).id.value
            }
        }
        val redId = accepted.selection.choices["setup.red-herring"]
        val redSeat = pending.domain.forDrunkSeat(selectedDrunk)
            .redHerringCandidatesBySeat.entries.singleOrNull { it.value == redId }?.key
        redSeat?.let { seat ->
            clocktowerRedHerring = cards[seat - 1].name
        }
        BotcAiWireTrace.hostAdoption(selectedDrunk, adoptedJointInformation.size)
        screen = Screen.ClocktowerJudge
    }

    fun takeOverJointOpeningManually() {
        val pending = pendingJointOpening ?: return
        if (screen !in setOf(Screen.ClocktowerJointPass,
                Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)) return
        jointOpeningSerial += 1
        jointOpeningBusy = false
        jointOpeningResult = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        jointOpeningError = null
        pendingJointOpening = null
        storytellerOperationMode = StorytellerOperationMode.MANUAL
        shownOnlyDealCompleted = true
        val route = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (route) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                commitAndStartTroubleBrewingGame(
                    pending.preparedGameId, pending.preparedSetup, null)
                screen = Screen.ClocktowerJudge // shown roles already dealt
            }
            is TroubleBrewingDrunkSelectionRoute.RuleDetermined -> {
                commitAndStartTroubleBrewingGame(
                    pending.preparedGameId, pending.preparedSetup, route.candidate)
                screen = Screen.ClocktowerJudge
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                val registry = activeGameClocktowerRulesetCatalog
                    .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
                    pending.preparedGameId, pending.preparedSetup.intermediateSetup)
                val decision = DrunkAssignmentDecisionBoundary.create(
                    snapshot, registry, StorytellerDecisionRevision(0, 0))
                val providerRequest = StorytellerProviderRequestFactoryV1.fromDrunkAssignment(
                    decision, snapshot,
                    StorytellerProviderGameContextBuilderV1.build(
                        snapshot, StorytellerProviderRevisionV1(0, 0)))
                pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
                    pending.preparedGameId, pending.preparedSetup, route.request,
                    decision, providerRequest)
                screen = Screen.ClocktowerDrunkSelection
                // Deliberately NO standalone AI call. Manual fallback is local.
            }
        }
    }

    fun startTroubleBrewingGame() {
        // Both AI modes must enter the one-request shown-role-first joint path.
        // A legacy Gateway configuration must NOT fall through into the paid
        // Drunk-only request followed by a second global analysis.
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL &&
            (!personalDirectEnabled || !personalDirectLoaded || aiToken.isBlank() ||
                aiDirectModel.isNullOrBlank())
        ) return
        // Invalidate any prior asynchronous callback before preparing the next setup.
        jointOpeningSerial += 1
        jointOpeningApplied = false
        adoptedJointOpening = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        strategicPolicyCursor = null
        strategicLastReviewedHost = null
        adoptedJointInformation = emptyMap()
        shownOnlyDealCompleted = false
        pendingJointOpening = null
        jointOpeningResult = null
        jointOpeningBusy = false
        jointOpeningError = null
        jointRevealIndex = 0
        pendingTroubleBrewingDrunkSelection = null
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
        val preparedSeed = newClocktowerSeed()
        val preparedGameId = UUID.randomUUID().toString()

        val datasetJson = baseContext.assets
            .open("setup/trouble_brewing_setup_presets_v2_final.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val dataset = TroubleBrewingSetupPresetJson.parse(datasetJson)
        val rotationHistoryStore = TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
        val rotationHistory = rotationHistoryStore.historyFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
            playerCount = playerNames.size,
        )
        val playerRotationHistory = rotationHistoryStore.recentPlayerStartingIdentityHistoryFor(
            datasetId = dataset.datasetId,
            schemaVersion = dataset.schemaVersion,
        )
        val characterRegistry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        val preparedSetup = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = characterRegistry,
            orderedPlayerNames = playerNames.toList(),
            gameSeed = preparedSeed,
            recentSetupRotationHistory = rotationHistory,
            recentPlayerStartingIdentityHistory = playerRotationHistory,
        )
        if (personalDirectEnabled &&
            storytellerOperationMode != StorytellerOperationMode.MANUAL
        ) {
            val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
                preparedGameId, preparedSetup.intermediateSetup)
            val domain = HostOpeningRulesDomainV2.fromHostSnapshot(
                snapshot, characterRegistry,
                clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing),
                playerExperience = playerNames.mapIndexed { index, name ->
                    (index + 1) to openingExperience(name)
                }.toMap(),
            )
            val visibleCards = preparedSetup.intermediateSetup.shownSeatAssignments.map { assignment ->
                val role = requireNotNull(completeTroubleBrewingRoles.singleOrNull { available ->
                    available.enName.filter(Char::isLetterOrDigit).lowercase() == assignment.shownRoleId
                }) { "Missing shown-only precommit role." }
                PlayerCard(
                    name = assignment.playerName,
                    role = Role.Civilian,
                    word = role.descriptionFor(language),
                    roleLabel = role.nameFor(language),
                    actualRoleLabel = null,
                    clocktowerTeam = role.team,
                    clocktowerRole = null, // No speculative actual Drunk identity.
                    clocktowerShownRole = role,
                )
            }
            val pending = PendingTroubleBrewingJointOpening(
                preparedGameId, preparedSetup, domain, visibleCards)
            pendingJointOpening = pending
            jointRevealIndex = 0
            screen = Screen.ClocktowerJointPass
            requestJointOpening(pending) // One network request, concurrently with visible-only dealing.
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
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    confirmedDrunkCandidate = null,
                )

            is TroubleBrewingDrunkSelectionRoute.RuleDetermined ->
                commitAndStartTroubleBrewingGame(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    confirmedDrunkCandidate = route.candidate,
                )

            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
                    gameId = preparedGameId,
                    intermediateSetup = preparedSetup.intermediateSetup,
                )
                val decision = DrunkAssignmentDecisionBoundary.create(
                    snapshot = snapshot,
                    characterRegistry = characterRegistry,
                    revision = StorytellerDecisionRevision(0, 0),
                )
                val revision = StorytellerProviderRevisionV1(0, 0)
                val providerRequest = StorytellerProviderRequestFactoryV1.fromDrunkAssignment(
                    decision = decision,
                    snapshot = snapshot,
                    gameContext = StorytellerProviderGameContextBuilderV1.build(snapshot, revision),
                )
                require(route.request.candidates.map { it.seat to it.shownRoleId } ==
                    decision.legalCandidates.map { it.payload.seat to it.payload.shownRoleId }) {
                    "Displayed Drunk candidates must exactly match Host's legal domain."
                }
                val pending = PendingTroubleBrewingDrunkSelection(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    request = route.request,
                    decision = decision,
                    providerRequest = providerRequest,
                )
                pendingTroubleBrewingDrunkSelection = pending
                screen = Screen.ClocktowerDrunkSelection
                // Only the manual operator reaches this local picker. AI paid
                // Drunk-only planning was replaced by the joint-opening request.
            }
        }
    }

    fun readyForNewClocktowerGame(script: ClocktowerScript): Boolean =
        storytellerNewGameConfigurationReady(
            mode = storytellerModeForNewGame(preferredStorytellerOperationMode, script),
            directEnabled = personalDirectEnabled,
            directLoaded = personalDirectLoaded,
            directModel = personalDirectModel,
            directKey = personalDirectKey,
            gatewayLoaded = gatewayConfigurationLoaded,
            gatewayEndpoint = storytellerGatewayEndpoint,
            gatewayToken = storytellerGatewayToken,
        )

    fun startClocktowerGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
        if (playerNames.size < MIN_CLOCKTOWER_PLAYERS) return
        val script = if (playerNames.size in 5..6) {
            selectedClocktowerScript ?: defaultClocktowerScriptFor(playerNames.size)
        } else {
            ClocktowerScript.TroubleBrewing
        }
        if (!canStartClocktowerScript(script) || !readyForNewClocktowerGame(script)) return
        // Reapply the global preference on every new game, including a quick restart
        // after the previous game's AI pause/manual takeover.
        storytellerOperationMode = storytellerModeForNewGame(preferredStorytellerOperationMode, script)

        if (script == ClocktowerScript.TroubleBrewing) {
            startTroubleBrewingGame()
            return
        }

        val preparedSeed = newClocktowerSeed()
        val preparedSetup = NoGreaterJoyProductionSetupPreparer.prepare(
            ruleset = activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.NoGreaterJoy),
            playerCount = playerNames.size,
            gameSeed = preparedSeed,
        )
        val availableRolesById = clocktowerRolesForScript(script).associateBy { role -> RoleId(role.enName) }
        val committedCards = preparedSetup.assignments.map { assignment ->
            val name = playerNames[assignment.seat - 1]
            val role = requireNotNull(availableRolesById[assignment.actualRole]) {
                "Committed No Greater Joy actual role '${assignment.actualRole.value}' is unavailable."
            }
            val shownRole = requireNotNull(availableRolesById[assignment.shownRole]) {
                "Committed No Greater Joy shown role '${assignment.shownRole.value}' is unavailable."
            }
            PlayerCard(
                name = name.ifBlank { context.playerName(assignment.seat) },
                role = Role.Civilian,
                roleLabel = shownRole.nameFor(language),
                actualRoleLabel = role.nameFor(language),
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shownRole,
                word = context.getString(
                    R.string.clocktower_card_desc_format,
                    shownRole.team.label(context),
                    shownRole.descriptionFor(language),
                ),
            )
        }
        cards.clear()
        cards.addAll(committedCards)
        resetDealState(GameKind.Clocktower, script, preparedSeed)
        committedClocktowerSetup = preparedSetup
        persistActiveGameStateIfNeeded()
    }

    fun persistCompletedTroubleBrewingSetupIfNeeded(): Boolean {
        if (currentGameKind != GameKind.Clocktower) return true
        if (currentClocktowerScript != ClocktowerScript.TroubleBrewing) return true
        if (gameOutcome == null) return true
        val record = committedTroubleBrewingSetupRotationRecord ?: return true
        return TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
            .recordCompletedGame(
                gameId = clocktowerGameId,
                record = record,
            )
    }

    fun archiveCurrentGameForRestart(): Boolean {
        if (cards.isEmpty()) return false
        if (!persistCompletedTroubleBrewingSetupIfNeeded()) return false
        invalidateA4SessionBoundary()
        gameHistory = gameArchivePreferencesStore.archiveGame(
            GameArchiveRecord(
                gameKind = currentGameKind,
                round = round,
                cards = cards.toList(),
                records = records.toList(),
                events = clocktowerEvents.toList(),
                outcome = gameOutcome,
            ),
        )
        clearSavedGameState()
        clocktowerGameSession = null
        publishClocktowerSessionView()
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
    }

    fun archiveAndReturnToPlayerManagement() {
        if (!archiveCurrentGameForRestart()) return
        cards.clear()
        records.clear()
        clocktowerEvents.clear()
        clocktowerEventCounter = 0
        gameOutcome = null
        currentDealIndex = 0
        resetClocktowerFlow()
        screen = Screen.Setup
    }

    fun archiveAndStartNewGame() {
        val restartScript = if (currentGameKind == GameKind.Clocktower) currentClocktowerScript else null
        if (restartScript != null && !readyForNewClocktowerGame(restartScript)) {
            // Do not archive the old game when the selected AI transport cannot start the next one.
            settingsReturnScreen = screen
            screen = Screen.Settings
            showNewGameConfirmation = false
            return
        }
        if (!archiveCurrentGameForRestart()) return
        when (currentGameKind) {
            GameKind.Undercover -> startUndercoverGame()
            GameKind.Clocktower -> {
                // A five/six-player quick restart must not silently switch scripts.
                selectedClocktowerScript = restartScript
                startClocktowerGame()
            }
        }
    }

    fun recordClocktowerRoleChangeAction(
        targetSeat: Int,
        nextRole: ClocktowerRole,
        actionId: String,
    ) {
        val localSequence = clocktowerEventCounter + 1
        recordClocktowerAction(ActionFactDraft.RoleChange(
            actionId = actionId,
            phase = storytellerPhaseFor(),
            round = round,
            sequence = localSequence,
            targetSeat = targetSeat,
            role = RoleId(nextRole.enName),
            alignment = when (nextRole.team) {
                ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> ClocktowerAlignment.GOOD
                ClocktowerTeam.Minion, ClocktowerTeam.Demon -> ClocktowerAlignment.EVIL
            },
            type = when (nextRole.team) {
                ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
                ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                ClocktowerTeam.Minion -> CharacterType.MINION
                ClocktowerTeam.Demon -> CharacterType.DEMON
            },
        ))
    }

    fun setClocktowerActualRole(
        playerName: String,
        nextRole: ClocktowerRole,
        recordSemanticHistory: Boolean = true,
    ) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            val targetSeat = index + 1
            if (recordSemanticHistory) {
                recordClocktowerRoleChangeAction(
                    targetSeat = targetSeat,
                    nextRole = nextRole,
                    actionId = clocktowerActionId(
                        kind = "role-change-${nextRole.enName.lowercase().replace(' ', '-')}",
                        localSequence = clocktowerEventCounter + 1,
                        targetSeat = targetSeat,
                    ),
                )
            }
            requireClocktowerGameSession().commitActualRoleBoundary(
                seat = targetSeat,
                actualRole = RoleId(nextRole.enName),
                actualAlignment = when (nextRole.team) {
                    ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> ClocktowerAlignment.GOOD
                    ClocktowerTeam.Minion, ClocktowerTeam.Demon -> ClocktowerAlignment.EVIL
                },
                actualType = when (nextRole.team) {
                    ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
                    ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                    ClocktowerTeam.Minion -> CharacterType.MINION
                    ClocktowerTeam.Demon -> CharacterType.DEMON
                },
            )
            publishClocktowerSessionView()
            invalidateA4RevisionScope()
            cards[index] = cards[index].copy(
                actualRoleLabel = nextRole.nameFor(language),
                clocktowerTeam = nextRole.team,
                clocktowerRole = nextRole,
            )
        }
    }

    fun promoteScarletWomanIfNeeded(): String? {
        val alivePlayers = cards.filter { it.eliminatedRound == null }
        val functioningScarletWomanSeat = cards.indexOfFirst { card ->
            card.eliminatedRound == null && AbilityFunctioningSemantics.functionsAs(
                card.abilitySubject(clocktowerConfirmedPoisonTarget),
                "Scarlet Woman",
            )
        }.takeIf { it >= 0 }?.plus(1)
        val resolution = DemonSuccessionSemantics.resolve(
            DemonSuccessionContext(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = false,
                // Every caller has already materialized the Demon death in cards.
                aliveCountBeforeDemonDeath = alivePlayers.size + 1,
                functioningScarletWomanSeat = functioningScarletWomanSeat,
                livingMinionSeats = cards.mapIndexedNotNull { index, card ->
                    (index + 1).takeIf {
                        card.eliminatedRound == null && card.clocktowerTeam == ClocktowerTeam.Minion
                    }
                }.toSet(),
            ),
        )
        val targetSeat = (resolution as? DemonSuccessionResolution.Forced)?.targetSeat ?: return null
        val scarletWoman = cards[targetSeat - 1]
        val imp = completeTroubleBrewingRoles.first { it.enName == "Imp" }
        setClocktowerActualRole(scarletWoman.name, imp)
        records.add(EliminationRecord(round, scarletWoman.name, context.getString(R.string.clocktower_record_scarlet_woman_promoted)))
        addClocktowerEvent(
            ClocktowerEventType.RoleChange,
            localizedText("角色变化", "Role changed"),
            localizedText("${playerSeatLabel(cards, scarletWoman.name)} 成为新的恶魔。", "${playerSeatLabel(cards, scarletWoman.name)} became the new Demon."),
            listOf(scarletWoman.name),
        )
        return scarletWoman.name
    }

    fun promoteDemonSuccessorIfNeeded(
        impDeathWasSelfChosen: Boolean,
    ): String? {
        if (impDeathWasSelfChosen) return null
        return promoteScarletWomanIfNeeded()
    }

    fun applyHostSeatingBack(origin: HostSeatingBackOrigin) {
        val transition = hostSeatingBackTransition(
            flow = hostSeatingSetupFlow,
            origin = origin,
        )
        hostSeatingSetupFlow = transition.flow
        screen = when (transition.destination) {
            HostSeatingSetupDestination.Seating -> Screen.Setup
            HostSeatingSetupDestination.GameSelection -> Screen.GameSelection
        }
    }

    val hostSeatingBackOrigin = when (screen) {
        Screen.GameSelection -> HostSeatingBackOrigin.GameSelection
        Screen.UndercoverSettings,
        Screen.ClocktowerSettings -> HostSeatingBackOrigin.GameSettings
        else -> null
    }
    BackHandler(enabled = hostSeatingBackOrigin != null) {
        applyHostSeatingBack(requireNotNull(hostSeatingBackOrigin))
    }

    val aiSettingsSection: @Composable () -> Unit = {
        StorytellerAiSettingsPanel(
            preferredMode = preferredStorytellerOperationMode,
            onPreferredModeChange = { mode ->
                preferredStorytellerOperationMode = mode
                appPreferencesStore.saveStorytellerOperationMode(mode)
            },
            directEnabled = personalDirectEnabled,
            onDirectEnabledChange = { personalDirectEnabled = it },
            directModel = personalDirectModel,
            onDirectModelChange = { personalDirectModel = it },
            directKey = personalDirectKey,
            onDirectKeyChange = { personalDirectKey = it },
            directSaveFailed = personalDirectSaveFailed,
            gatewayEndpoint = storytellerGatewayEndpoint,
            onGatewayEndpointChange = { storytellerGatewayEndpoint = it },
            gatewayToken = storytellerGatewayToken,
            onGatewayTokenChange = { storytellerGatewayToken = it },
            gatewaySaveFailed = gatewayCredentialSaveFailed,
        )
    }

    CompositionLocalProvider(LocalContext provides context) {
        MaterialTheme(
            colorScheme = androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF2F5D50),
                secondary = Color(0xFFD96C3B),
                background = Color(0xFFF8F6F0),
                surface = Color(0xFFFFFCF6),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF1F2925),
                onSurface = Color(0xFF1F2925),
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (screen) {
                    Screen.Landing -> ClocktowerLandingScreen(
                        hasSavedGame = savedGamePreview != null,
                        onStartGame = {
                            hostSeatingSetupFlow = HostSeatingSetupFlow()
                            screen = Screen.Setup
                        },
                        onContinueGame = ::restoreSavedGame,
                    )

                    Screen.Setup -> SeatingFirstSetupScreen(
                    savedGamePreview = savedGamePreview,
                    commonPlayers = commonPlayers,
                    playerExperienceByName = playerExperienceByName,
                    onPlayerExperienceChange = ::setPlayerExperience,
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onRemoveCurrentPlayer = ::removeCurrentPlayer,
                    onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
                    onResumeSavedGame = ::restoreSavedGame,
                    onDiscardSavedGame = ::clearSavedGameState,
                    onOpenSettings = {
                        settingsReturnScreen = Screen.Setup
                        screen = Screen.Settings
                    },
                    onConfirmSeats = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.confirmSeats(playerNames)
                        screen = Screen.GameSelection
                    },
                )

                    Screen.GameSelection -> SeatingFirstGameSelectionScreen(
                    seating = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
                        "Game selection requires confirmed seating"
                    },
                    onBackToSeating = {
                        applyHostSeatingBack(HostSeatingBackOrigin.GameSelection)
                    },
                    onOpenUndercoverSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Undercover)
                        screen = Screen.UndercoverSettings
                    },
                    onOpenClocktowerSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Clocktower)
                        clocktowerSetupStep = 0
                        screen = Screen.ClocktowerSettings
                    },
                )

                    Screen.UndercoverSettings -> UndercoverSettingsScreen(
                        playerCount = playerCount,
                        undercoverCount = undercoverCount,
                        includeBlank = includeBlank,
                        onUndercoverCountChange = { undercoverCount = it },
                        onIncludeBlankChange = { checked ->
                            includeBlank = checked
                            clampUndercoverCount()
                        },
                        onBack = {
                            applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
                        },
                        onStart = ::startUndercoverGame,
                    )

                    Screen.ClocktowerSettings -> ClocktowerSettingsScreen(
                        playerCount = playerCount,
                        playerNames = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
                            "Clocktower settings require confirmed seating"
                        }.playerNames,
                        step = clocktowerSetupStep,
                        onStepChange = { clocktowerSetupStep = it },
                        selectedScript = selectedClocktowerScript ?: defaultClocktowerScriptFor(playerCount),
                        preferredOperationMode = preferredStorytellerOperationMode,
                        aiConfigurationReady = readyForNewClocktowerGame(
                            if (playerCount in 5..6) selectedClocktowerScript
                                ?: defaultClocktowerScriptFor(playerCount)
                            else ClocktowerScript.TroubleBrewing
                        ),
                        onOpenAiSettings = {
                            settingsReturnScreen = Screen.ClocktowerSettings
                            screen = Screen.Settings
                        },
                        onScriptChange = { selectedClocktowerScript = it },
                        onBack = {
                            applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
                        },
                        onStart = ::startClocktowerGame,
                    )

                    Screen.ClocktowerJointPass -> {
                        val pending = requireNotNull(pendingJointOpening)
                        val previewCards = pending.shownOnlyCards
                        require(jointRevealIndex in previewCards.indices)
                        PassPhoneScreen(
                            playerName = previewCards[jointRevealIndex].name,
                            playerNames = previewCards.map { it.name },
                            gameKind = GameKind.Clocktower,
                            current = jointRevealIndex + 1,
                            total = previewCards.size,
                            onReveal = { screen = Screen.ClocktowerJointReveal },
                            onPrevious = {
                                if (jointRevealIndex > 0) jointRevealIndex -= 1
                            },
                            onHostTools = {}, // No actual Host grimoire exists yet.
                            onNext = {
                                if (jointRevealIndex == previewCards.lastIndex) {
                                    screen = Screen.ClocktowerJointReview
                                } else {
                                    jointRevealIndex += 1
                                }
                            },
                        )
                    }

                    Screen.ClocktowerJointReveal -> {
                        val pending = requireNotNull(pendingJointOpening)
                        RevealCardScreen(
                            card = pending.shownOnlyCards[jointRevealIndex],
                            gameKind = GameKind.Clocktower,
                            current = jointRevealIndex + 1,
                            total = pending.shownOnlyCards.size,
                            onHide = { screen = Screen.ClocktowerJointPass },
                        )
                    }

                    Screen.ClocktowerJointReview -> {
                        val pending = requireNotNull(pendingJointOpening)
                        ClocktowerJointOpeningReviewScreen(
                            acceptance = jointOpeningResult?.takeIf {
                                it.selection.validFor(pending.domain.scope)
                            },
                            busy = jointOpeningBusy,
                            error = jointOpeningError,
                            onAdopt = ::adoptJointOpeningAfterReveal,
                            onRetry = { requestJointOpening(pending) },
                            onManual = ::takeOverJointOpeningManually,
                        )
                    }

                    Screen.ClocktowerDrunkSelection -> {
                        val pending = requireNotNull(pendingTroubleBrewingDrunkSelection) {
                            "Trouble Brewing Drunk selection screen requires a pending manual request."
                        }
                        val characterRegistry = activeGameClocktowerRulesetCatalog
                            .ruleset(ClocktowerScript.TroubleBrewing)
                            .characterRegistry
                        ClocktowerDrunkSelectionScreen(
                            request = pending.request,
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
                                pendingTroubleBrewingDrunkSelection = null
                                screen = Screen.ClocktowerSettings
                            },
                            onConfirm = { candidate ->
                                require(candidate in pending.request.candidates) {
                                    "Trouble Brewing Drunk selection confirmation must use a displayed legal candidate."
                                }
                                val legalId = requireNotNull(pending.decision.legalCandidates.singleOrNull {
                                    it.payload.seat == candidate.seat &&
                                        it.payload.shownRoleId == candidate.shownRoleId
                                }) { "Drunk candidate not in current Host domain." }.candidateId
                                val fresh = pendingTroubleBrewingDrunkSelection === pending &&
                                    screen == Screen.ClocktowerDrunkSelection
                                val confirmation = if (fresh) {
                                    pending.decision.confirm(legalId, pending.decision.revision)
                                } else null
                                if (confirmation !is StorytellerDecisionConfirmation.Confirmed<*>) {
                                    DebugFlightRecorder.record("MANUAL_DRUNK_SELECTION_STALE")
                                    return@ClocktowerDrunkSelectionScreen
                                }
                                commitAndStartTroubleBrewingGame(
                                    preparedGameId = pending.preparedGameId,
                                    preparedSetup = pending.preparedSetup,
                                    confirmedDrunkCandidate = candidate,
                                )
                                if (shownOnlyDealCompleted) {
                                    shownOnlyDealCompleted = false
                                    screen = Screen.ClocktowerJudge
                                }
                            },
                        )
                    }

                    Screen.Settings -> SettingsScreen(
                        languageMode = languageMode,
                        commonPlayers = commonPlayers,
                        playerExperienceByName = playerExperienceByName,
                        onPlayerExperienceChange = ::setPlayerExperience,
                        newCommonPlayerName = newCommonPlayerName,
                        onLanguageModeChange = { nextMode ->
                            languageMode = nextMode
                            appPreferencesStore.saveLanguageMode(nextMode)
                        },
                        onNewCommonPlayerNameChange = { newCommonPlayerName = it },
                        onAddCommonPlayer = ::addCommonPlayer,
                        onRemoveCommonPlayer = ::removeCommonPlayer,
                        aiSettingsSection = aiSettingsSection,
                        onBack = { screen = settingsReturnScreen },
                    )

                    Screen.PassPhone -> PassPhoneScreen(
                        playerName = cards[currentDealIndex].name,
                        playerNames = cards.map { it.name },
                        gameKind = currentGameKind,
                        current = currentDealIndex + 1,
                        total = cards.size,
                        onReveal = { screen = Screen.RevealCard },
                        onPrevious = {
                            if (currentGameKind == GameKind.Clocktower && currentDealIndex > 0) {
                                currentDealIndex -= 1
                            }
                        },
                        onHostTools = {
                            if (currentGameKind == GameKind.Clocktower) {
                                hostToolTab = HostToolTab.Roles
                                showHostTools = true
                            }
                        },
                        onNext = {
                            if (currentGameKind == GameKind.Clocktower) {
                                if (currentDealIndex == cards.lastIndex) {
                                    screen = when {
                                        currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
                                            storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC ->
                                            Screen.ClocktowerAutoPause
                                        else -> Screen.ClocktowerJudge
                                    }
                                } else {
                                    currentDealIndex += 1
                                }
                            }
                        },
                    )

                    Screen.RevealCard -> RevealCardScreen(
                        card = cards[currentDealIndex],
                        gameKind = currentGameKind,
                        current = currentDealIndex + 1,
                        total = cards.size,
                        onHide = {
                            when (currentGameKind) {
                                GameKind.Clocktower -> screen = Screen.PassPhone
                                GameKind.Undercover -> {
                                    if (currentDealIndex == cards.lastIndex) {
                                        screen = Screen.Game
                                    } else {
                                        currentDealIndex += 1
                                        screen = Screen.PassPhone
                                    }
                                }
                            }
                        },
                    )

                    Screen.ClocktowerAutoPause -> StorytellerAutoPauseScreen(
                        hasGlobalPlan = jointOpeningApplied,
                        language = language,
                        onTakeOverManually = {
                            storytellerOperationMode = StorytellerOperationMode.MANUAL
                            screen = Screen.ClocktowerJudge
                        },
                    )

                    Screen.ClocktowerJudge -> ClocktowerJudgeScreen(
                        allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                        cards = cards,
                        events = clocktowerEvents,
                        script = currentClocktowerScript,
                        gameId = clocktowerGameId,
                        gameSeed = clocktowerGameSeed,
                        firstNightPairDecisionContext = currentTroubleBrewingFirstNightPairDecisionContext(),
                        globalAiAssisted = storytellerOperationMode != StorytellerOperationMode.MANUAL,
                        globalAiAutomatic = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC,
                        jointOpeningAdopted = jointOpeningApplied,
                        jointOpeningSemantics = adoptedJointInformation,
                        globalNightPendingDecision = if (
                            storytellerOperationMode != StorytellerOperationMode.MANUAL &&
                            clocktowerPhase == ClocktowerPhase.Night
                        ) runCatching { currentTroubleBrewingMayorRedirectPendingDecision() }.getOrNull()
                        else null,
                        globalAiAdviceKey = liveAiAdviceKey,
                        globalAiAdviceText = liveAiAdviceText,
                        globalAiAdviceBusy = liveAiAdviceBusy,
                        globalAiAdviceError = liveAiAdviceError,
                        globalResidualAdoptedChoices = residualAdoptedPackage?.takeIf { plan ->
                            val latest = clocktowerGameSession?.state
                            if (residualAdoptedOperationMode != storytellerOperationMode) return@takeIf false
                            latest?.gameId == plan.gameId &&
                                latest.gameStateRevision == plan.gameStateRevision &&
                                latest.playerInputRevision == plan.playerInputRevision &&
                                latest.nextTimelineGlobalSequence == plan.exclusiveGlobalSequence
                        }?.choices.orEmpty(),
                        globalResidualProposalAvailable = residualProposedPackage?.let { plan ->
                            val latest = clocktowerGameSession?.state
                            residualProposedKey == liveAiAdviceKey &&
                                latest?.gameId == plan.gameId &&
                                latest.gameStateRevision == plan.gameStateRevision &&
                                latest.playerInputRevision == plan.playerInputRevision &&
                                latest.nextTimelineGlobalSequence == plan.exclusiveGlobalSequence &&
                                residualAdoptedPackage != plan
                        } == true,
                        onAdoptGlobalResidual = ::adoptWholeResidualProposal,
                        onRequestGlobalAiAdvice = ::requestLiveGlobalAdvice,
                        firstNightNaturalPairReadyProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingFirstNightPrecomputeCoordinator::readyFor
                            } else {
                                null
                            },
                        firstNightNaturalPairResultProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingFirstNightPrecomputeCoordinator::resultFor
                            } else {
                                null
                            },
                        phase = clocktowerPhase,
                        round = round,
                        nightCheckpoint = currentClocktowerNightCheckpoint(),
                        selectedExecution = clocktowerSelectedExecution,
                        fortuneTellerFirst = clocktowerFortuneTellerFirst,
                        fortuneTellerSecond = clocktowerFortuneTellerSecond,
                        chambermaidFirst = clocktowerChambermaidFirst,
                        chambermaidSecond = clocktowerChambermaidSecond,
                        ravenkeeperTarget = clocktowerRavenkeeperTarget,
                        redHerring = clocktowerRedHerring,
                        recommendedDemonBluffRoleNames = clocktowerRecommendedDemonBluffRoleNames,
                        butlerMaster = clocktowerButlerMaster,
                        virginUsed = clocktowerVirginUsed,
                        slayerUsed = clocktowerSlayerUsed,
                        slayerClaimedNames = clocktowerSlayerClaimedNames,
                        artistUsed = clocktowerArtistUsed,
                        artistClaimedNames = clocktowerArtistClaimedNames,
                        lastExecutedName = clocktowerLastExecutedName,
                        pendingKlutzName = clocktowerPendingKlutzName,
                        klutzChoiceName = clocktowerKlutzChoiceName,
                        nightStartedState = clocktowerNightStartedState,
                        nightStepIndexState = clocktowerNightStepIndexState,
                        onMovePreviousNightStep = {
                            val transaction = NightCheckpointHostTransaction.movePrevious(
                                checkpoint = currentClocktowerNightCheckpoint(),
                            )
                            when (transaction.revisionIntent) {
                                NightCheckpointRevisionIntent.NONE -> Unit
                                NightCheckpointRevisionIntent.PLAYER_INPUT -> advanceClocktowerPlayerInputRevision()
                                NightCheckpointRevisionIntent.GAME_STATE -> advanceClocktowerGameStateRevision()
                            }
                            clocktowerNightStepIndexState.value = transaction.checkpoint.nightStepIndex
                        },
                        dayModeState = clocktowerDayModeState,
                        ghostVoteAuthority = clocktowerGhostVoteAuthorityState.value,
                        onGhostVoteAuthorityChange = { clocktowerGhostVoteAuthorityState.value = it },
                        highestVoteNameState = clocktowerHighestVoteNameState,
                        highestVoteCountState = clocktowerHighestVoteCountState,
                        gameOutcome = gameOutcome,
                        onRecordEvent = { type, title, detail, names ->
                            addClocktowerEvent(type, title, detail, names)
                        },
                        onRecordEpistemicObservation = ::recordEpistemicObservation,
                        onCommitConfirmedInformationDecision = ::commitConfirmedInformationDecision,
                        onCommitConfirmedRegistrationResult = ::commitConfirmedRegistrationResult,
                        onPreflightConfirmedRegistrationResult = ::canCommitConfirmedRegistrationResult,
                        onHostTools = {
                            hostToolTab = HostToolTab.Roles
                            showHostTools = true
                        },
                        onPreviousFromFirstNightReady = {
                            currentDealIndex = cards.lastIndex
                            screen = Screen.PassPhone
                        },
                        onSelectNightDeath = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            val reducedCheckpoint = NightCheckpointReducer.reduce(
                                checkpoint = currentClocktowerNightCheckpoint(),
                                event = NightResolutionEvent.EditDemonAttackDraft(selected),
                            )
                            clocktowerDemonAttackDraftTarget = reducedCheckpoint.attackDraftTarget
                        },
                        onConfirmDemonAttack = {
                            val transaction = NightCheckpointHostTransaction.confirmDemonAttack(
                                checkpoint = currentClocktowerNightCheckpoint(),
                            )
                            if (transaction.revisionIntent == NightCheckpointRevisionIntent.GAME_STATE) {
                                val targetName = transaction.checkpoint.confirmedAttackTarget
                                if (targetName != null) {
                                    val targetSeat = clocktowerSeatFor(targetName)
                                    val localSequence = clocktowerEventCounter + 1
                                    recordClocktowerAction(ActionFactDraft.Attack(
                                        actionId = clocktowerActionId(
                                            kind = "attack",
                                            localSequence = localSequence,
                                            targetSeat = targetSeat,
                                        ),
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = localSequence,
                                        targetSeat = targetSeat,
                                    ))
                                }
                                clocktowerPendingNightDeath = transaction.checkpoint.confirmedAttackTarget
                                clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
                                clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSelectExecution = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerSelectedExecution = it
                        },
                        onSelectPoisonTarget = { selectedTarget ->
                            advanceClocktowerPlayerInputRevision()
                            val reducedCheckpoint = NightCheckpointReducer.reduce(
                                checkpoint = currentClocktowerNightCheckpoint(),
                                event = NightResolutionEvent.EditPoisonDraft(selectedTarget),
                            )
                            clocktowerPoisonTarget = reducedCheckpoint.poisonDraftTarget
                        },
                        onConfirmPoisonTarget = {
                            val transaction = NightCheckpointHostTransaction.confirmPoison(
                                checkpoint = currentClocktowerNightCheckpoint(),
                            )
                            if (transaction.revisionIntent == NightCheckpointRevisionIntent.GAME_STATE) {
                                val targetSeat = transaction.checkpoint.confirmedPoisonTarget?.let(::clocktowerSeatFor)
                                val localSequence = clocktowerEventCounter + 1
                                recordClocktowerAction(ActionFactDraft.Poison(
                                    actionId = clocktowerActionId(
                                        kind = "poison",
                                        localSequence = localSequence,
                                        targetSeat = targetSeat,
                                    ),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = localSequence,
                                    targetSeat = targetSeat,
                                ))
                                requireClocktowerGameSession().commitPoisonTargetBoundary(targetSeat)
                                publishClocktowerSessionView()
                                invalidateA4RevisionScope()
                                clocktowerConfirmedPoisonTarget = transaction.checkpoint.confirmedPoisonTarget
                                clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
                                clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
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
                        onSelectRedHerring = {
                            // An accepted joint choice belongs to Host in AI auto.
                            // Assisted hosting may override the private preselection.
                            if (storytellerOperationMode != StorytellerOperationMode.AI_AUTOMATIC ||
                                !jointOpeningApplied
                            ) {
                                advanceClocktowerPlayerInputRevision()
                                clocktowerRedHerring = it
                            }
                        },
                        onCommitDemonBluffs = { roleNames ->
                            if (roleNames != clocktowerRecommendedDemonBluffRoleNames) {
                                advanceClocktowerPlayerInputRevision()
                                clocktowerRecommendedDemonBluffRoleNames = roleNames
                            }
                        },
                        onSelectButlerMaster = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerButlerMaster = it
                        },
                        onSelectMonkProtectedTarget = { selectedTarget ->
                            advanceClocktowerPlayerInputRevision()
                            val reducedCheckpoint = NightCheckpointReducer.reduce(
                                checkpoint = currentClocktowerNightCheckpoint(),
                                event = NightResolutionEvent.EditMonkProtectionDraft(selectedTarget),
                            )
                            clocktowerMonkProtectedTarget = reducedCheckpoint.monkDraftTarget
                        },
                        onConfirmMonkProtectedTarget = {
                            val transaction = NightCheckpointHostTransaction.confirmMonkProtection(
                                checkpoint = currentClocktowerNightCheckpoint(),
                            )
                            if (transaction.revisionIntent == NightCheckpointRevisionIntent.GAME_STATE) {
                                val targetName = transaction.checkpoint.confirmedMonkTarget
                                if (targetName != null) {
                                    val targetSeat = clocktowerSeatFor(targetName)
                                    val localSequence = clocktowerEventCounter + 1
                                    recordClocktowerAction(ActionFactDraft.Protect(
                                        actionId = clocktowerActionId(
                                            kind = "protect",
                                            localSequence = localSequence,
                                            targetSeat = targetSeat,
                                        ),
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = localSequence,
                                        targetSeat = targetSeat,
                                    ))
                                }
                                clocktowerConfirmedMonkProtectedTarget = transaction.checkpoint.confirmedMonkTarget
                                clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
                                clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSelectMayorRedirectTarget = { selectedTarget ->
                            advanceClocktowerPlayerInputRevision()
                            val reducedCheckpoint = NightCheckpointReducer.reduce(
                                checkpoint = currentClocktowerNightCheckpoint(),
                                event = NightResolutionEvent.EditMayorRedirectDraft(selectedTarget),
                            )
                            clocktowerMayorRedirectTarget = reducedCheckpoint.mayorRedirectDraftTarget
                        },
                        onConfirmMayorRedirectTarget = {
                            val checkpoint = currentClocktowerNightCheckpoint()
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                val selectedTarget = requireNotNull(checkpoint.mayorRedirectDraftTarget) {
                                    "Trouble Brewing Mayor redirect confirmation requires a selected legal target."
                                }
                                val pendingDecision = requireNotNull(currentTroubleBrewingMayorRedirectPendingDecision()) {
                                    "Trouble Brewing Mayor redirect confirmation requires a current engine decision."
                                }
                                val targetSeat = clocktowerSeatFor(selectedTarget)
                                val candidateId = requireNotNull(pendingDecision.candidateIdForSeat(targetSeat)) {
                                    "Selected Mayor redirect target is outside the current rules-owned legal domain."
                                }
                                when (
                                    val confirmation = pendingDecision.confirm(
                                        candidateId = candidateId,
                                        currentRevision = StorytellerDecisionRevision(
                                            gameStateRevision = clocktowerGameStateRevision,
                                            playerInputRevision = clocktowerPlayerInputRevision,
                                        ),
                                    )
                                ) {
                                    is MayorRedirectDecisionConfirmation.Confirmed -> {
                                        require(confirmation.targetSeat == targetSeat) {
                                            "Mayor redirect confirmation changed the selected target."
                                        }
                                        if (checkpoint.confirmedMayorRedirectTarget != selectedTarget) {
                                            // Rules-owned confirmation has passed, but no mechanical
                                            // mutation/observation has been published yet. Capture the
                                            // exact global prefix BEFORE updating the game revision.
                                            val session = requireClocktowerGameSession()
                                            val rulesetRef = requireNotNull(clocktowerRulesetRef)
                                            val registry = activeGameClocktowerRulesetCatalog
                                                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                                            val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                                                gameSnapshot = session.toGameSnapshot(rulesetRef),
                                                phase = StorytellerPhase.NIGHT,
                                                round = round,
                                                characterRegistry = registry,
                                            )
                                            val journal = currentClocktowerCausalJournal()
                                            val request = StorytellerProviderRequestFactoryV1.fromMayorRedirect(
                                                decision = pendingDecision,
                                                snapshot = snapshot,
                                                gameContext = journal.contextForRequest(snapshot, session.state),
                                            )
                                            val previousConfirmed = journal.effectiveNow().lastOrNull { earlier ->
                                                earlier.eventId.startsWith("confirmed:night:${round}:mayor-redirect:")
                                            }
                                            val frozen = journal.captureBeforeDecision(request, session.state)
                                            val newEventId = "confirmed:${request.identity.decisionId}"
                                            journal.commit(request.identity.decisionId, StorytellerProviderPriorDecisionV1(
                                                eventId = newEventId,
                                                gameStateRevision = frozen.revision.gameStateRevision,
                                                playerInputRevision = frozen.revision.playerInputRevision,
                                                selectedCandidateId = candidateId,
                                                selectedOutcome = DecisionOutcomeSnapshot(
                                                    decisionType = "mayor-redirect",
                                                    canonicalFields = sortedMapOf("targetSeat" to targetSeat.toString()),
                                                ),
                                                abilityState = AbilityState.FUNCTIONING,
                                                truthRelation = TruthRelation.NOT_APPLICABLE,
                                                registrations = emptyList(),
                                            ))
                                            // Reconfirmation is a *later correction*, not two
                                            // simultaneously effective Mayor decisions. Earlier
                                            // frozen inputs still observe the old confirmation.
                                            previousConfirmed?.let { prior ->
                                                journal.correct(
                                                    correctionId = "revised:${newEventId}",
                                                    replacedEventId = prior.eventId,
                                                    replacementEventId = newEventId,
                                                )
                                            }
                                        }
                                    }
                                    is MayorRedirectDecisionConfirmation.Blocked -> {
                                        error("Mayor redirect confirmation blocked: ${confirmation.reason}")
                                    }
                                }
                            }
                            val reducedCheckpoint = NightCheckpointReducer.reduce(
                                checkpoint = checkpoint,
                                event = NightResolutionEvent.ConfirmMayorRedirect,
                            )
                            if (reducedCheckpoint.confirmedMayorRedirectTarget != checkpoint.confirmedMayorRedirectTarget) {
                                clocktowerConfirmedMayorRedirectTarget = reducedCheckpoint.confirmedMayorRedirectTarget
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSelectDemonSuccessor = { selectedTarget ->
                            val transaction = NightCheckpointHostTransaction.editDemonSuccessor(
                                checkpoint = currentClocktowerNightCheckpoint(),
                                selectedTarget = selectedTarget,
                            )
                            when (transaction.revisionIntent) {
                                NightCheckpointRevisionIntent.NONE -> Unit
                                NightCheckpointRevisionIntent.PLAYER_INPUT -> advanceClocktowerPlayerInputRevision()
                                NightCheckpointRevisionIntent.GAME_STATE -> advanceClocktowerGameStateRevision()
                            }
                            clocktowerDemonSuccessorTarget = transaction.checkpoint.demonSuccessorDraftTarget
                        },
                        onConfirmDemonSuccessorTarget = { _ ->
                            val transaction = NightCheckpointHostTransaction.confirmDemonSuccessor(
                                checkpoint = currentClocktowerNightCheckpoint(),
                            )
                            when (transaction.revisionIntent) {
                                NightCheckpointRevisionIntent.NONE -> Unit
                                NightCheckpointRevisionIntent.PLAYER_INPUT -> {
                                    clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
                                    advanceClocktowerPlayerInputRevision()
                                }
                                NightCheckpointRevisionIntent.GAME_STATE -> {
                                    clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
                                    advanceClocktowerGameStateRevision()
                                }
                            }
                        },
                        onConfirmNewDemon = {
                            val pendingName = clocktowerPendingNewDemonName
                            var dawnPhaseActionIdToCommit: String? = null
                            var dawnPhaseStateMutationRequired = false
                            val canEnterDawn =
                                if (pendingName != null) {
                                    val baseGameState = cards.toClocktowerGameState(
                                        currentClocktowerScript,
                                        clocktowerGameSeed,
                                        poisonedPlayerName = clocktowerConfirmedPoisonTarget,
                                    )
                                    val checkpoint = currentClocktowerNightCheckpoint()
                                    val effectiveNightState = ClocktowerEffectiveNightState(
                                        effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
                                            (index + 1).takeIf { card.eliminatedRound == null }
                                        }.toSet(),
                                        effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                            card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                        }.toMap(),
                                    )
                                    val poisoner = cards.mapIndexedNotNull { index, card ->
                                        card.clocktowerRole
                                            ?.takeIf { role -> card.eliminatedRound == null && role.enName == "Poisoner" }
                                            ?.let { role -> index + 1 to role }
                                    }.firstOrNull()
                                    val demonRole = requireNotNull(cards.firstOrNull {
                                        it.clocktowerTeam == ClocktowerTeam.Demon
                                    }?.clocktowerRole)
                                    val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
                                        baseGameState = baseGameState,
                                        checkpoint = checkpoint,
                                        demonRoleId = RoleId(demonRole.enName),
                                        poisonResolutionInput = poisoner?.let { (poisonerSeat, poisonerRole) ->
                                            NightDawnPoisonResolutionInput(
                                                poisonerSeat = poisonerSeat,
                                                poisonerRoleId = RoleId(poisonerRole.enName),
                                                effectiveNightState = effectiveNightState,
                                            )
                                        },
                                        durablePreviousPoisonTargetSeat = NightDawnPoisonRecoveryAuthority
                                            .latestTargetSeatForRound(
                                                actionTimeline = clocktowerActionTimeline,
                                                round = round,
                                            ),
                                    )
                                    val dawnCommitIntent = transition.dawnCommitIntent
                                    if (transition.continuation == NightResolutionContinuation.DAWN && dawnCommitIntent != null) {
                                        val durableMaterializationPlan = NightDawnDurableMaterializationPlanner.plan(
                                            gameId = clocktowerGameId,
                                            round = round,
                                            intent = dawnCommitIntent,
                                            state = DawnDurableMaterializationState(
                                                aliveSeats = cards.mapIndexedNotNull { index, card ->
                                                    (index + 1).takeIf { card.eliminatedRound == null }
                                                }.toSet(),
                                                roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                                    card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                                }.toMap(),
                                                currentPhase = storytellerPhaseFor(),
                                                currentPoisonTargetSeat =
                                                    clocktowerConfirmedPoisonTarget?.let(::clocktowerSeatFor),
                                                committedActionIds = clocktowerActionTimeline.entries
                                                    .map { it.fact.actionId }
                                                    .toSet(),
                                                committedObservationRecordIds = clocktowerEpistemicObservations
                                                    .map { it.recordId }
                                                    .toSet(),
                                            ),
                                            advanceToDawn = true,
                                        )
                                        val phaseAdvance = durableMaterializationPlan.phaseAdvance
                                        val poisonMaterialization = durableMaterializationPlan.poison
                                        durableMaterializationPlan.roleChanges.forEach { roleChangeMaterialization ->
                                            val roleChange = roleChangeMaterialization.intent
                                            val targetName = cards.getOrNull(roleChange.targetSeat - 1)?.name
                                            val nextRole = clocktowerRolesForScript(currentClocktowerScript)
                                                .firstOrNull { role -> RoleId(role.enName) == roleChange.roleId }
                                            if (targetName != null && nextRole != null) {
                                                roleChangeMaterialization.actionIdToCommit?.let { actionId ->
                                                    recordClocktowerRoleChangeAction(
                                                        targetSeat = roleChange.targetSeat,
                                                        nextRole = nextRole,
                                                        actionId = actionId,
                                                    )
                                                }
                                                if (roleChangeMaterialization.stateMutationRequired) {
                                                    setClocktowerActualRole(
                                                        targetName,
                                                        nextRole,
                                                        recordSemanticHistory = false,
                                                    )
                                                    records.add(
                                                        EliminationRecord(
                                                            round,
                                                            targetName,
                                                            context.getString(R.string.clocktower_record_imp_passed),
                                                        ),
                                                    )
                                                    addClocktowerEvent(
                                                        ClocktowerEventType.RoleChange,
                                                        localizedText("角色变化", "Role changed"),
                                                        localizedText(
                                                            "${playerSeatLabel(cards, targetName)} 成为新的小恶魔。",
                                                            "${playerSeatLabel(cards, targetName)} became the new Imp.",
                                                        ),
                                                        listOf(targetName),
                                                    )
                                                }
                                            }
                                        }
                                        poisonMaterialization?.let { materialization ->
                                            materialization.actionIdToCommit?.let { actionId ->
                                                val localSequence = clocktowerEventCounter + 1
                                                recordClocktowerAction(
                                                    ActionFactDraft.Poison(
                                                        actionId = actionId,
                                                        phase = storytellerPhaseFor(),
                                                        round = round,
                                                        sequence = localSequence,
                                                        targetSeat = materialization.intent.targetSeat,
                                                    ),
                                                )
                                            }
                                            if (materialization.stateMutationRequired) {
                                                requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
                                                    targetSeat = materialization.intent.targetSeat,
                                                )
                                                publishClocktowerSessionView()
                                                val poisonTargetName = materialization.intent.targetSeat
                                                    ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                                                clocktowerConfirmedPoisonTarget = poisonTargetName
                                                clocktowerPoisonTarget = poisonTargetName
                                            }
                                        }
                                        clocktowerPendingNewDemonName = transition.checkpoint.pendingNewDemonName
                                        clocktowerPendingNightNewDemonIdentityName = transition.checkpoint.pendingNightNewDemonIdentityName
                                        clocktowerDemonSuccessorTarget = transition.checkpoint.demonSuccessorDraftTarget
                                        clocktowerConfirmedDemonSuccessorTarget = transition.checkpoint.confirmedDemonSuccessorTarget
                                        dawnPhaseActionIdToCommit = phaseAdvance?.actionIdToCommit
                                        dawnPhaseStateMutationRequired = phaseAdvance?.stateMutationRequired == true
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            if (canEnterDawn) {
                                clocktowerPendingNewDemonName = null
                                clocktowerDemonSuccessorTarget = null
                                clearConfirmedDemonSuccessorTarget()
                                dawnPhaseActionIdToCommit?.let { actionId ->
                                    val localSequence = clocktowerEventCounter + 1
                                    recordClocktowerAction(ActionFactDraft.PhaseAdvance(
                                        actionId = actionId,
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = localSequence,
                                        nextPhase = StorytellerPhase.DAWN,
                                        nextRound = round,
                                    ))
                                }
                                if (dawnPhaseStateMutationRequired) {
                                    clocktowerPhase = ClocktowerPhase.Dawn
                                    advanceClocktowerGameStateRevision()
                                }
                                resetClocktowerNightFlow()
                            }
                        },
                        onSelectKlutzChoice = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerKlutzChoiceName = it
                        },
                        onConfirmKlutzChoice = {
                            val choice = clocktowerKlutzChoiceName
                            if (choice != null) {
                                // The real NGJ Klutz learns of death and publicly chooses a
                                // living player. No Spy appears on NGJ; this is a player action,
                                // never an invented Storyteller registration ruling.
                                val session = requireClocktowerGameSession()
                                val choiceResult = NoGreaterJoyKlutzChoiceRuleV1.resolve(
                                    gameState = session.state.gameState,
                                    klutzSeat = clocktowerSeatFor(requireNotNull(clocktowerPendingKlutzName)),
                                    chosenSeat = clocktowerSeatFor(choice),
                                    scriptRoles = clocktowerRoleDefinitionsForScript(requireNotNull(currentClocktowerScript)),
                                )
                                val klutzSeat = choiceResult.klutzSeat
                                NoGreaterJoyKlutzHistoryProducerV1.choice(
                                    session.state, klutzSeat, choiceResult.chosenSeat,
                                    clocktowerActionId(kind = "klutz-choice", targetSeat = choiceResult.chosenSeat),
                                    round, clocktowerEventCounter + 1,
                                )?.let(::recordClocktowerAction)
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("呆瓜选择", "Klutz choice"),
                                    "${playerSeatLabel(cards, clocktowerPendingKlutzName)} → ${playerSeatLabel(cards, choice)}",
                                    listOfNotNull(clocktowerPendingKlutzName, choice),
                                )
                                if (choiceResult.evilWins) {
                                    gameOutcome = GameOutcome(
                                        title = context.getString(R.string.outcome_clocktower_evil_title),
                                        summary = localizedText("呆瓜选择了邪恶玩家，善良阵营失败。", "The Klutz chose an evil player, so the good team loses."),
                                        reason = localizedText("${playerSeatLabel(cards, clocktowerPendingKlutzName)} 选择了 ${playerSeatLabel(cards, choice)}。", "${playerSeatLabel(cards, clocktowerPendingKlutzName)} chose ${playerSeatLabel(cards, choice)}."),
                                    )
                                    showResults = true
                                    addOutcomeEvent(gameOutcome)
                                } else {
                                    clocktowerPendingKlutzName = null
                                    clocktowerKlutzChoiceName = null
                                    if (clocktowerKlutzReturnToDawn) {
                                        recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)
                                        clocktowerPhase = ClocktowerPhase.Dawn
                                        clocktowerKlutzReturnToDawn = false
                                    } else {
                                        val nextRound = round + 1
                                        materializeClocktowerPoisonExpiryAtDusk()
                                        recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
                                        round = nextRound
                                        clocktowerPhase = ClocktowerPhase.Night
                                    }
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                    advanceClocktowerGameStateRevision()
                                }
                            }
                        },
                        onConfirmArtistQuestion = { claimantName, truthfulAnswer, shownAnswer ->
                            if (claimantName !in clocktowerArtistClaimedNames) {
                                clocktowerArtistClaimedNames = clocktowerArtistClaimedNames + claimantName
                            }
                            val claimantCard = cards.firstOrNull { it.name == claimantName }
                            if (claimantCard?.clocktowerRole?.enName == "Artist" && !clocktowerArtistUsed) {
                                clocktowerArtistUsed = true
                            }
                            records.add(EliminationRecord(round, claimantName, localizedText("艺术家提问已处理", "Artist question resolved")))
                            addClocktowerEvent(
                                ClocktowerEventType.RoleAction,
                                localizedText("艺术家提问", "Artist question"),
                                localizedText(
                                    "${playerSeatLabel(cards, claimantName)} · 真实答案：${if (truthfulAnswer) "是" else "否"} · 展示：${if (shownAnswer) "是" else "否"}",
                                    "${playerSeatLabel(cards, claimantName)} · truthful: ${if (truthfulAnswer) "yes" else "no"} · shown: ${if (shownAnswer) "yes" else "no"}",
                                ),
                                listOf(claimantName),
                            )
                            clocktowerDayModeState.value = ClocktowerDayMode.Overview
                            advanceClocktowerGameStateRevision()
                        },
                        onSlayerShot = { claimantName, targetName, recluseRegistersAsDemon ->
                            val claimantCard = cards.firstOrNull { it.name == claimantName }
                            val targetIndex = cards.indexOfFirst { it.name == targetName }
                            val targetCard = cards.getOrNull(targetIndex)
                            val slayerDecision = AbilityFunctioningSemantics.oneShotDecision(
                                subject = claimantCard?.abilitySubject(clocktowerConfirmedPoisonTarget),
                                role = "Slayer",
                                alreadyUsed = clocktowerSlayerUsed,
                            )
                            if (claimantName !in clocktowerSlayerClaimedNames) {
                                clocktowerSlayerClaimedNames = clocktowerSlayerClaimedNames + claimantName
                            }
                            val targetRegistersAsDemon = targetCard?.clocktowerTeam == ClocktowerTeam.Demon ||
                                (targetCard?.clocktowerRole?.enName == "Recluse" &&
                                    targetCard.name != clocktowerConfirmedPoisonTarget &&
                                    recluseRegistersAsDemon == true)
                            val shotWillHit = slayerDecision.effectApplies &&
                                targetIndex >= 0 && targetCard != null &&
                                targetCard.eliminatedRound == null && targetRegistersAsDemon
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
                                clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1 &&
                                claimantCard != null && targetCard != null
                            ) {
                                // Player's confirmed public shot precedes the optional Storyteller
                                // registration ruling; misses and non-functioning claims remain facts.
                                recordClocktowerAction(ActionFactDraft.SlayerShot(
                                    actionId = clocktowerActionId("slayer-shot", targetSeat = targetIndex + 1),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = clocktowerEventCounter + 1,
                                    claimantSeat = clocktowerSeatFor(claimantName),
                                    targetSeat = targetIndex + 1,
                                    abilityConsumed = slayerDecision.consumesUse,
                                    hit = shotWillHit,
                                ))
                            }
                            if (
                                currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
                                clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1 &&
                                slayerDecision.effectApplies &&
                                !clocktowerSlayerUsed &&
                                targetCard?.clocktowerRole?.enName == "Recluse" &&
                                targetCard.eliminatedRound == null &&
                                recluseRegistersAsDemon != null
                            ) {
                                // Real public ability adjudication, recorded BEFORE the shot changes
                                // player death/ability-used state. No fake private information is made.
                                val session = requireClocktowerGameSession()
                                val actorSeat = clocktowerSeatFor(claimantName)
                                val subjectSeat = targetIndex + 1
                                val rulesetRef = requireNotNull(clocktowerRulesetRef)
                                val registry = activeGameClocktowerRulesetCatalog
                                    .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                                val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                                    gameSnapshot = session.toGameSnapshot(rulesetRef),
                                    phase = StorytellerPhase.DAY,
                                    round = round,
                                    characterRegistry = registry,
                                )
                                DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                                    session = session,
                                    journal = currentClocktowerCausalJournal(),
                                    snapshot = snapshot,
                                    legalRoles = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
                                    input = ConfirmedDaySlayerRegistrationV1(
                                        interactionId = "day:$round:slayer:$actorSeat:$subjectSeat",
                                        slayerSeat = actorSeat,
                                        recluseSeat = subjectSeat,
                                        registeredDemonRole = if (recluseRegistersAsDemon) RoleId("Imp") else null,
                                    ),
                                )
                            }
                            if (slayerDecision.effectApplies && targetCard?.clocktowerRole?.enName == "Recluse" &&
                                targetCard.eliminatedRound == null && targetCard.name != clocktowerConfirmedPoisonTarget &&
                                recluseRegistersAsDemon == true
                            ) {
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("隐士登记裁定", "Recluse registration"),
                                    localizedText(
                                        "${playerSeatLabel(cards, targetName)} 在杀手判定中登记为小恶魔。",
                                        "${playerSeatLabel(cards, targetName)} registered as the Imp for the Slayer.",
                                    ),
                                    listOf(targetName),
                                )
                            }
                            var shotOutcome: GameOutcome? = null
                            if (slayerDecision.consumesUse) {
                                clocktowerSlayerUsed = true
                                advanceClocktowerGameStateRevision()
                            }
                            if (shotWillHit && targetIndex >= 0 && targetCard != null) {
                                val targetSeat = targetIndex + 1
                                val localSequence = clocktowerEventCounter + 1
                                preflightClocktowerPublicAliveObservation(
                                    playerName = targetName,
                                    eventSequence = localSequence,
                                )
                                recordClocktowerAction(ActionFactDraft.Death(
                                    actionId = clocktowerActionId(
                                        kind = "slayer-death",
                                        localSequence = localSequence,
                                        targetSeat = targetSeat,
                                    ),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = localSequence,
                                    targetSeat = targetSeat,
                                ))
                                requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                    targetSeat = targetSeat,
                                )
                                publishClocktowerSessionView()
                                cards[targetIndex] = targetCard.copy(eliminatedRound = round)
                                recordEpistemicObservation(EpistemicObservationDraft(
                                    recordId = "public-alive-${clocktowerGameId}-${localSequence}-$targetSeat",
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = localSequence,
                                    sourceSeat = null,
                                    sourceAbility = null,
                                    visibility = ObservationVisibility.PUBLIC,
                                    recipientSeats = emptySet(),
                                    reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                                    proposition = InformationProposition.AliveAt(targetSeat, false),
                                ))
                                records.add(
                                    EliminationRecord(
                                        round,
                                        targetName,
                                        context.getString(R.string.clocktower_record_slayer_hit, playerSeatLabel(cards, claimantName)),
                                    ),
                                )
                                val promotedName = if (targetCard.clocktowerTeam == ClocktowerTeam.Demon) {
                                    promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
                                } else {
                                    null
                                }
                                if (promotedName != null) {
                                    clocktowerPendingNightNewDemonIdentityName = promotedName
                                }
                                shotOutcome = if (targetCard.clocktowerTeam != ClocktowerTeam.Demon || promotedName != null) null
                                else evaluateGameOutcome(context, cards, currentGameKind)
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("杀手命中", "Slayer hit"),
                                    localizedText("${playerSeatLabel(cards, claimantName)} 击杀了 ${playerSeatLabel(cards, targetName)}。", "${playerSeatLabel(cards, claimantName)} killed ${playerSeatLabel(cards, targetName)}."),
                                    listOf(claimantName, targetName),
                                )
                            } else {
                                val recordText = when {
                                    slayerDecision.consumesUse && slayerDecision.state == AbilityFunctioningState.POISONED ->
                                        context.getString(R.string.clocktower_record_slayer_poisoned, playerSeatLabel(cards, targetName))
                                    slayerDecision.state != null && !slayerDecision.mayAttempt ->
                                        context.getString(R.string.clocktower_record_slayer_already_used, playerSeatLabel(cards, targetName))
                                    slayerDecision.consumesUse ->
                                        context.getString(R.string.clocktower_record_slayer_miss, playerSeatLabel(cards, targetName))
                                    else ->
                                        context.getString(R.string.clocktower_record_slayer_fake, playerSeatLabel(cards, targetName))
                                }
                                records.add(EliminationRecord(round, claimantName, recordText))
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("杀手行动", "Slayer claim"),
                                    recordText,
                                    listOf(claimantName, targetName),
                                )
                            }
                            gameOutcome = shotOutcome
                            if (shotOutcome != null) {
                                showResults = true
                                addOutcomeEvent(shotOutcome)
                            }
                        },
                        onPreflightVirginExecution = { nominatorName, spyRegistrationWillRecord ->
                            val preflightIndex = cards.indexOfFirst { it.name == nominatorName }
                            val preflightCard = cards.getOrNull(preflightIndex)
                            if (preflightIndex >= 0 && preflightCard != null && preflightCard.eliminatedRound == null) {
                                preflightClocktowerPublicAliveObservation(
                                    playerName = nominatorName,
                                    eventSequence = clocktowerEventCounter + if (spyRegistrationWillRecord) 2 else 1,
                                )
                            }
                        },
                        onConfirmedNomination = { nominatorName, nomineeName ->
                            if (currentClocktowerScript in setOf(
                                    ClocktowerScript.TroubleBrewing, ClocktowerScript.NoGreaterJoy,
                                ) && clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1
                            ) {
                                // Public nomination belongs to both supported scripts. The hidden
                                // first-Virgin marker is only meaningful in real Trouble Brewing.
                                val nominatedCard = cards.firstOrNull { it.name == nomineeName }
                                val firstVirginNomination = nominatedCard?.let {
                                    AbilityFunctioningSemantics.interactsAs(
                                        it.abilitySubject(clocktowerConfirmedPoisonTarget), "Virgin",
                                    )
                                } == true && !clocktowerVirginUsed &&
                                    currentClocktowerScript == ClocktowerScript.TroubleBrewing
                                recordClocktowerAction(ActionFactDraft.Nomination(
                                    actionId = clocktowerActionId("nomination", targetSeat = clocktowerSeatFor(nomineeName)),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = clocktowerEventCounter + 1,
                                    nominatorSeat = clocktowerSeatFor(nominatorName),
                                    nomineeSeat = clocktowerSeatFor(nomineeName),
                                    firstVirginNomination = firstVirginNomination,
                                ))
                            }
                        },
                        onConfirmedVote = { nominatorName, nomineeName, voteRecord ->
                            if (currentClocktowerScript in setOf(
                                    ClocktowerScript.TroubleBrewing, ClocktowerScript.NoGreaterJoy,
                                ) && clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1
                            ) {
                                val voterSeats = voteRecord.voters.map { clocktowerSeatFor(it.playerName) }
                                recordClocktowerAction(ActionFactDraft.Vote(
                                    actionId = clocktowerActionId("vote", targetSeat = clocktowerSeatFor(nomineeName)),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = clocktowerEventCounter + 1,
                                    nominatorSeat = clocktowerSeatFor(nominatorName),
                                    nomineeSeat = clocktowerSeatFor(nomineeName),
                                    voterSeats = voterSeats,
                                    ghostVoterSeats = voteRecord.voters.filter { it.isGhostVote }
                                        .map { clocktowerSeatFor(it.playerName) },
                                ))
                            }
                        },
                        onVirginNomination = { nominatorName, nomineeName, executeNominator, explicitSpyRegistersTownsfolk ->
                            if (
                                explicitSpyRegistersTownsfolk != null &&
                                !clocktowerVirginUsed &&
                                currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
                                clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1
                            ) {
                                val virginCard = cards.firstOrNull { it.name == nomineeName }
                                val spyCard = cards.firstOrNull { it.name == nominatorName }
                                if (
                                    virginCard?.clocktowerRole?.enName == "Virgin" &&
                                    spyCard?.clocktowerRole?.enName == "Spy"
                                ) {
                                    // The player chose the public nomination; only this Spy type
                                    // registration is Storyteller discretion. Freeze BEFORE
                                    // virginUsed, the execution, death or phase changes.
                                    val session = requireClocktowerGameSession()
                                    val rulesetRef = requireNotNull(clocktowerRulesetRef)
                                    val registry = activeGameClocktowerRulesetCatalog
                                        .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                                    val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                                        gameSnapshot = session.toGameSnapshot(rulesetRef),
                                        phase = StorytellerPhase.DAY,
                                        round = round,
                                        characterRegistry = registry,
                                    )
                                    DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
                                        session = session,
                                        journal = currentClocktowerCausalJournal(),
                                        snapshot = snapshot,
                                        legalRoles = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
                                        input = ConfirmedDayVirginSpyRegistrationV1(
                                            interactionId = "day:$round:virgin:${clocktowerSeatFor(nomineeName)}:" +
                                                clocktowerSeatFor(nominatorName),
                                            virginSeat = clocktowerSeatFor(nomineeName),
                                            spyNominatorSeat = clocktowerSeatFor(nominatorName),
                                            registersAsTownsfolk = explicitSpyRegistersTownsfolk,
                                        ),
                                        confirmedExecution = executeNominator,
                                    )
                                }
                            }
                            clocktowerVirginUsed = true
                            advanceClocktowerGameStateRevision()
                            if (executeNominator) {
                                val index = cards.indexOfFirst { it.name == nominatorName }
                                val nominatorCard = cards.getOrNull(index)
                                if (index >= 0 && nominatorCard != null && nominatorCard.eliminatedRound == null) {
                                    requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                        targetSeat = index + 1,
                                    )
                                    publishClocktowerSessionView()
                                    cards[index] = nominatorCard.copy(eliminatedRound = round)
                                    records.add(
                                        EliminationRecord(
                                            round,
                                            nominatorName,
                                            context.getString(
                                                R.string.clocktower_record_virgin_execution,
                                                playerSeatLabel(cards, nomineeName),
                                            ),
                                        ),
                                    )
                                    addClocktowerEvent(
                                        ClocktowerEventType.Execution,
                                        localizedText("圣女能力处决", "Virgin execution"),
                                        playerSeatLabel(cards, nominatorName),
                                        listOf(nominatorName, nomineeName),
                                    )
                                }
                                clocktowerLastExecutedName = nominatorName
                                val outcome = evaluateGameOutcome(context, cards, currentGameKind)
                                gameOutcome = outcome
                                if (outcome != null) {
                                    showResults = true
                                    addOutcomeEvent(outcome)
                                } else {
                                    val nextRound = round + 1
                                    materializeClocktowerPoisonExpiryAtDusk()
                                    recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
                                    round = nextRound
                                    clocktowerPhase = ClocktowerPhase.Night
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                }
                                clocktowerSelectedExecution = null
                            } else {
                                records.add(
                                    EliminationRecord(
                                        round,
                                        nomineeName,
                                        context.getString(
                                            R.string.clocktower_record_virgin_spent,
                                            playerSeatLabel(cards, nominatorName),
                                        ),
                                    ),
                                )
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("圣女能力已触发", "Virgin ability spent"),
                                    localizedText("${playerSeatLabel(cards, nomineeName)} 的能力已使用，但提名人未被处决。", "${playerSeatLabel(cards, nomineeName)} spent the ability without executing the nominator."),
                                    listOf(nominatorName, nomineeName),
                                )
                            }
                        },
                        onAdvanceFromFirstNight = {
                            recordClocktowerPhaseAdvance(ClocktowerPhase.Day)
                            clocktowerPhase = ClocktowerPhase.Day
                            advanceClocktowerGameStateRevision()
                            clocktowerPendingNightDeath = null
                            clocktowerDemonAttackDraftTarget = null
                            resetClocktowerNightFlow()
                            resetClocktowerDayFlow()
                        },
                        onConfirmDay = {
                            val preflightExecutionName = clocktowerSelectedExecution
                            val preflightIndex = preflightExecutionName
                                ?.let { selectedName -> cards.indexOfFirst { it.name == selectedName } }
                                ?: -1
                            val preflightCard = cards.getOrNull(preflightIndex)
                            val proposedSequence = clocktowerEventCounter + 1
                            val proposedObservationId = if (preflightIndex >= 0) {
                                "public-alive-${clocktowerGameId}-${proposedSequence}-${preflightIndex + 1}"
                            } else {
                                ""
                            }
                            val observationAlreadyExists = proposedObservationId.isNotEmpty() &&
                                clocktowerEpistemicObservations.any { observation ->
                                    observation.recordId == proposedObservationId
                                }
                            val executionDebugFields = mapOf(
                                "lastCriticalAction" to "CONFIRM_EXECUTION_CLICKED",
                                "phaseAtAction" to clocktowerPhase.name,
                                "roundAtAction" to round.toString(),
                                "selectedSeat" to if (preflightIndex >= 0) (preflightIndex + 1).toString() else "",
                                "targetAlive" to (preflightCard?.eliminatedRound == null).toString(),
                                "eventCounter" to clocktowerEventCounter.toString(),
                                "gameStateRevision" to clocktowerGameStateRevision.toString(),
                                "playerInputRevision" to clocktowerPlayerInputRevision.toString(),
                                "eventCount" to clocktowerEvents.size.toString(),
                                "observationCount" to clocktowerEpistemicObservations.size.toString(),
                                "proposedObservationId" to proposedObservationId,
                                "observationAlreadyExists" to observationAlreadyExists.toString(),
                            )
                            DebugFlightRecorder.updateState(executionDebugFields)
                            DebugFlightRecorder.record(
                                event = "CONFIRM_EXECUTION_CLICKED",
                                fields = executionDebugFields,
                            )
                            if (preflightExecutionName != null &&
                                preflightIndex >= 0 &&
                                preflightCard != null &&
                                preflightCard.eliminatedRound == null
                            ) {
                                DebugFlightRecorder.record(
                                    event = "EXECUTION_PREFLIGHT",
                                    fields = executionDebugFields + mapOf(
                                        "proposedSequence" to proposedSequence.toString(),
                                    ),
                                )
                                preflightClocktowerPublicAliveObservation(
                                    playerName = preflightExecutionName,
                                    eventSequence = proposedSequence,
                                )
                            }
                            // Confirming the day commits its execution/no-execution result and
                            // closes the day decision window, including when no one dies.
                            advanceClocktowerGameStateRevision()
                            val aliveBeforeExecution = cards.filter { it.eliminatedRound == null }
                            val executionName = clocktowerSelectedExecution
                            var executionOutcome: GameOutcome? = null
                            if (executionName != null) {
                                val index = cards.indexOfFirst { it.name == executionName }
                                val executedCard = cards.getOrNull(index)
                                if (index >= 0 && executedCard != null && executedCard.eliminatedRound == null) {
                                    val isKlutzDeathCapture = executedCard.clocktowerRole?.enName == "Klutz" &&
                                        clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1
                                    if (isKlutzDeathCapture) {
                                        // Commit the exact execution before death clears Klutz.poisoned.
                                        // The canonical session (not the UI) stamps ability-state evidence.
                                        val actionSequence = clocktowerEventCounter + 1
                                        recordClocktowerAction(ActionFactDraft.Execution(
                                            actionId = clocktowerActionId(
                                                kind = "execution",
                                                actionRound = round,
                                                localSequence = actionSequence,
                                                targetSeat = index + 1,
                                            ),
                                            phase = storytellerPhaseFor(),
                                            round = round,
                                            sequence = actionSequence,
                                            targetSeat = index + 1,
                                        ))
                                    }
                                    requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                        targetSeat = index + 1,
                                    )
                                    publishClocktowerSessionView()
                                    cards[index] = executedCard.copy(eliminatedRound = round)
                                    records.add(EliminationRecord(round, executionName, context.getString(R.string.clocktower_record_execution)))
                                    addClocktowerEvent(
                                        ClocktowerEventType.Execution,
                                        localizedText("处决", "Execution"),
                                        playerSeatLabel(cards, executionName),
                                        listOf(executionName),
                                        projectSemanticHistory = !isKlutzDeathCapture,
                                    )
                                    clocktowerLastExecutedName = executionName
                                    if (executedCard.clocktowerRole?.enName == "Saint") {
                                        executionOutcome = GameOutcome(
                                            title = context.getString(R.string.outcome_clocktower_evil_title),
                                            summary = context.getString(R.string.clocktower_outcome_saint_summary),
                                            reason = context.getString(R.string.clocktower_outcome_saint_reason, executionName),
                                        )
                                    } else if (executedCard.clocktowerRole?.enName == "Klutz") {
                                        clocktowerPendingKlutzName = executionName
                                        clocktowerKlutzChoiceName = null
                                        clocktowerKlutzReturnToDawn = false
                                        clocktowerPhase = ClocktowerPhase.Day
                                        clocktowerDayModeState.value = ClocktowerDayMode.Klutz
                                        captureKlutzLearnedDeathOnPublicAnnouncement(executionName)
                                        executionOutcome = null
                                    } else if (executedCard.clocktowerTeam == ClocktowerTeam.Demon) {
                                        val promotedName = promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
                                        if (promotedName != null) {
                                            clocktowerPendingNightNewDemonIdentityName = promotedName
                                        }
                                        executionOutcome = if (promotedName == null) {
                                            evaluateGameOutcome(context, cards, currentGameKind)
                                        } else {
                                            null
                                        }
                                    } else {
                                        executionOutcome = evaluateGameOutcome(context, cards, currentGameKind)
                                    }
                                }
                            } else {
                                clocktowerLastExecutedName = null
                                if (clocktowerSemanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1 &&
                                    currentClocktowerScript in setOf(
                                        ClocktowerScript.TroubleBrewing, ClocktowerScript.NoGreaterJoy,
                                    )
                                ) {
                                    // A confirmed no-execution is positive public chronology,
                                    // NOT absence of a death fact and NOT a Storyteller recommendation.
                                    // Commit before possible Mayor win and day/phase changes.
                                    recordClocktowerAction(ActionFactDraft.NoExecution(
                                        actionId = clocktowerActionId("no-execution"),
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = clocktowerEventCounter + 1,
                                    ))
                                }
                                addClocktowerEvent(
                                    ClocktowerEventType.Execution,
                                    localizedText("无人被处决", "No execution"),
                                    "",
                                )
                            }
                            if (executionName == null && aliveBeforeExecution.size == 3 && aliveBeforeExecution.any {
                                    AbilityFunctioningSemantics.functionsAs(it.abilitySubject(clocktowerConfirmedPoisonTarget), "Mayor")
                                }
                            ) {
                                executionOutcome = GameOutcome(
                                    title = context.getString(R.string.outcome_clocktower_good_title),
                                    summary = context.getString(R.string.clocktower_outcome_mayor_summary),
                                    reason = context.getString(R.string.clocktower_outcome_mayor_reason),
                                )
                            }
                            gameOutcome = executionOutcome
                            if (executionOutcome != null) {
                                showResults = true
                                addOutcomeEvent(executionOutcome)
                            } else if (clocktowerPendingKlutzName == null) {
                                val nextRound = round + 1
                                materializeClocktowerPoisonExpiryAtDusk()
                                recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
                                round = nextRound
                                clocktowerPhase = ClocktowerPhase.Night
                                resetClocktowerDayFlow()
                                resetClocktowerNightFlow()
                            }
                            clocktowerSelectedExecution = null
                        },
                        onConfirmNight = {
                            // Dawn resolution commits deaths, role changes and the next phase as
                            // one timeline boundary. Earlier action confirmations have already
                            // revisioned their own facts; this closes the night as a whole.
                            advanceClocktowerGameStateRevision()
                            clocktowerPendingNightNewDemonIdentityName = null
                            val demonPoisonedTonight = clocktowerConfirmedPoisonTarget?.let { name ->
                                cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
                            } == true
                            var nightKlutzName: String? = null
                            var newDemonName: String? = null
                            var unresolvedDemonSuccessor = false
                            val originalDeathName = clocktowerPendingNightDeath
                            val dawnDeathFacts = resolveTroubleBrewingDawnDeathFacts(
                                cards = cards,
                                targetName = originalDeathName,
                                poisonedPlayerName = clocktowerConfirmedPoisonTarget,
                                monkProtectedTargetName = clocktowerConfirmedMonkProtectedTarget,
                            )
                            val mayorCanRedirect = dawnDeathFacts.mayorSeat != null
                            val baseGameState = cards.toClocktowerGameState(
                                currentClocktowerScript,
                                clocktowerGameSeed,
                                poisonedPlayerName = clocktowerConfirmedPoisonTarget,
                            )
                            val effectiveNightState = ClocktowerEffectiveNightState(
                                effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
                                    (index + 1).takeIf { card.eliminatedRound == null }
                                }.toSet(),
                                effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                    card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                }.toMap(),
                            )
                            val demonRoleIds = cards.mapNotNull { card ->
                                card.clocktowerRole
                                    ?.takeIf { card.clocktowerTeam == ClocktowerTeam.Demon }
                                    ?.let { role -> RoleId(role.enName) }
                            }.toSet()
                            val deathTransition = NightDawnResolutionPlanner.planValidatedNightDeath(
                                baseGameState = baseGameState,
                                checkpoint = currentClocktowerNightCheckpoint(),
                                input = NightDawnDeathResolutionInput(
                                    originalDeathSeat = dawnDeathFacts.originalDeathSeat,
                                    mayorSeat = dawnDeathFacts.mayorSeat,
                                    mayorRedirectMayApply = mayorCanRedirect,
                                    attackOutcome = dawnDeathFacts.attackOutcome,
                                    demonSafeSeats = dawnDeathFacts.demonSafeSeats,
                                    effectiveNightState = effectiveNightState,
                                    demonRoleIds = demonRoleIds,
                                ),
                            )
                            val resolvedDeathName = deathTransition.dawnCommitIntent?.death?.targetSeat
                                ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                            val deathName = resolvedDeathName
                            val safeMayorRedirectName = clocktowerConfirmedMayorRedirectTarget
                                ?.takeIf { targetName ->
                                    val targetSeat = cards.indexOfFirst { it.name == targetName }
                                        .takeIf { it >= 0 }
                                        ?.plus(1)
                                    mayorCanRedirect &&
                                        resolvedDeathName == null &&
                                        targetSeat != null &&
                                        targetSeat in dawnDeathFacts.demonSafeSeats
                                }
                            val redirectEventTargetName = when {
                                mayorCanRedirect && resolvedDeathName != null && resolvedDeathName != originalDeathName -> resolvedDeathName
                                safeMayorRedirectName != null -> safeMayorRedirectName
                                else -> null
                            }
                            val dawnDeathMaterialization = NightDawnDurableMaterializationPlanner.plan(
                                gameId = clocktowerGameId,
                                round = round,
                                intent = DawnCommitIntent(death = deathTransition.dawnCommitIntent?.death),
                                state = DawnDurableMaterializationState(
                                    aliveSeats = cards.mapIndexedNotNull { index, card ->
                                        (index + 1).takeIf { card.eliminatedRound == null }
                                    }.toSet(),
                                    roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                        card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                    }.toMap(),
                                    currentPhase = storytellerPhaseFor(),
                                    committedActionIds = clocktowerActionTimeline.entries
                                        .map { it.fact.actionId }
                                        .toSet(),
                                    committedObservationRecordIds = clocktowerEpistemicObservations
                                        .map { it.recordId }
                                        .toSet(),
                                ),
                                advanceToDawn = false,
                            ).death
                            if (
                                deathName != null &&
                                dawnDeathMaterialization?.publicAliveObservationIdToCommit != null
                            ) {
                                preflightClocktowerPublicAliveObservation(
                                    playerName = deathName,
                                    eventSequence = clocktowerEventCounter + if (redirectEventTargetName != null) 2 else 1,
                                    recordId = dawnDeathMaterialization.publicAliveObservationIdToCommit,
                                )
                            }
                            if (redirectEventTargetName != null) {
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("市长死亡转移", "Mayor death redirect"),
                                    localizedText(
                                        playerSeatLabel(cards, originalDeathName) + " → " + playerSeatLabel(cards, redirectEventTargetName),
                                        playerSeatLabel(cards, originalDeathName) + " → " + playerSeatLabel(cards, redirectEventTargetName),
                                    ),
                                    listOfNotNull(originalDeathName, redirectEventTargetName),
                                )
                            }
                            if (demonPoisonedTonight) {
                                clocktowerPendingNightDeath?.let { targetName ->
                                    addClocktowerEvent(
                                        ClocktowerEventType.RoleAction,
                                        localizedText("恶魔击杀", "Demon kill"),
                                        localizedText(
                                            "${playerSeatLabel(cards, targetName)} · 失败（恶魔中毒）",
                                            "${playerSeatLabel(cards, targetName)} · failed (Demon poisoned)",
                                        ),
                                        listOfNotNull(clocktowerConfirmedPoisonTarget, targetName),
                                    )
                                }
                                clocktowerPendingNightDeath = null
                            }
                            var detailedAttackFailureRecorded = false
                            val canonicalOriginalDeathSeat = dawnDeathFacts.originalDeathSeat
                            val failedProtectedAttackName = when {
                                demonPoisonedTonight || deathName != null -> null
                                safeMayorRedirectName != null -> safeMayorRedirectName
                                canonicalOriginalDeathSeat != null &&
                                    canonicalOriginalDeathSeat in dawnDeathFacts.demonSafeSeats -> originalDeathName
                                else -> null
                            }
                            if (failedProtectedAttackName != null) {
                                val failedAttackCard = cards.firstOrNull { it.name == failedProtectedAttackName }
                                val apparentMonk = cards.firstOrNull {
                                    AbilityFunctioningSemantics.interactsAs(
                                        it.abilitySubject(clocktowerConfirmedPoisonTarget),
                                        "Monk",
                                    )
                                }
                                val protectedByMonkForRecord = AbilityFunctioningSemantics.selectedMechanicalEffectApplies(
                                    subject = apparentMonk?.abilitySubject(clocktowerConfirmedPoisonTarget),
                                    role = "Monk",
                                    selectionMatches = clocktowerConfirmedMonkProtectedTarget == failedProtectedAttackName,
                                )
                                val protectedBySoldierForRecord = failedAttackCard?.let { card ->
                                    AbilityFunctioningSemantics.functionsAs(
                                        card.abilitySubject(clocktowerConfirmedPoisonTarget),
                                        "Soldier",
                                    )
                                } == true
                                val protectionNote = when {
                                    protectedBySoldierForRecord -> context.getString(R.string.clocktower_record_soldier_safe)
                                    protectedByMonkForRecord -> context.getString(R.string.clocktower_record_monk_protected)
                                    else -> null
                                }
                                if (protectionNote != null) {
                                    records.add(EliminationRecord(round, failedProtectedAttackName, protectionNote))
                                    addClocktowerEvent(
                                        ClocktowerEventType.RoleAction,
                                        localizedText("恶魔击杀", "Demon kill"),
                                        localizedText(
                                            "${playerSeatLabel(cards, failedProtectedAttackName)} · 失败（$protectionNote）",
                                            "${playerSeatLabel(cards, failedProtectedAttackName)} · failed ($protectionNote)",
                                        ),
                                        listOf(failedProtectedAttackName),
                                    )
                                    clocktowerPendingNightDeath = null
                                    detailedAttackFailureRecorded = true
                                }
                            }
                            if (deathName != null) {
                                clocktowerPendingNightDeath = deathName
                                val index = cards.indexOfFirst { it.name == deathName }
                                val nightDeathCard = cards.getOrNull(index)
                                if (index >= 0 && nightDeathCard != null && dawnDeathMaterialization != null) {
                                    val demonDied = nightDeathCard.clocktowerTeam == ClocktowerTeam.Demon
                                    val impSelfChosen = demonDied && originalDeathName == deathName
                                    val deathLocalSequence = clocktowerEventCounter + 1
                                    dawnDeathMaterialization.actionIdToCommit?.let { actionId ->
                                        recordClocktowerAction(ActionFactDraft.Death(
                                            actionId = actionId,
                                            phase = storytellerPhaseFor(),
                                            round = round,
                                            sequence = deathLocalSequence,
                                            targetSeat = dawnDeathMaterialization.intent.targetSeat,
                                        ))
                                    }
                                    if (dawnDeathMaterialization.stateMutationRequired) {
                                        requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
                                            targetSeat = dawnDeathMaterialization.intent.targetSeat,
                                        )
                                        publishClocktowerSessionView()
                                        cards[index] = nightDeathCard.copy(eliminatedRound = round)
                                        records.add(EliminationRecord(round, deathName, context.getString(R.string.clocktower_record_night_death)))
                                        addClocktowerEvent(
                                            ClocktowerEventType.Death,
                                            localizedText("恶魔击杀", "Demon kill"),
                                            localizedText(
                                                "${playerSeatLabel(cards, deathName)} · 死亡",
                                                "${playerSeatLabel(cards, deathName)} · killed",
                                            ),
                                            listOf(deathName),
                                            projectSemanticHistory = false,
                                        )
                                    }
                                    if (dawnDeathMaterialization.publicAliveObservationIdToCommit != null) {
                                        recordEpistemicObservation(EpistemicObservationDraft(
                                            recordId = dawnDeathMaterialization.publicAliveObservationIdToCommit,
                                            phase = storytellerPhaseFor(),
                                            round = round,
                                            sequence = deathLocalSequence,
                                            sourceSeat = null,
                                            sourceAbility = null,
                                            visibility = ObservationVisibility.PUBLIC,
                                            recipientSeats = emptySet(),
                                            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                                            proposition = InformationProposition.AliveAt(
                                                dawnDeathMaterialization.intent.targetSeat,
                                                false,
                                            ),
                                        ))
                                    }
                                    if (demonDied) {
                                        if (impSelfChosen) {
                                            val demonRoleId = RoleId(requireNotNull(nightDeathCard.clocktowerRole).enName)
                                            val successionResolution = resolveTroubleBrewingImpSelfKillSuccession(
                                                baseGameState = baseGameState,
                                                checkpoint = currentClocktowerNightCheckpoint(),
                                                demonRoleId = demonRoleId,
                                            )
                                            val successionTransition = NightDawnResolutionPlanner.planDemonSuccession(
                                                baseGameState = baseGameState,
                                                checkpoint = currentClocktowerNightCheckpoint(),
                                                successionResolution = successionResolution,
                                                demonRoleId = demonRoleId,
                                            )
                                            clocktowerPendingNewDemonName = successionTransition.checkpoint.pendingNewDemonName
                                            clocktowerDemonSuccessorTarget = successionTransition.checkpoint.demonSuccessorDraftTarget
                                            clocktowerConfirmedDemonSuccessorTarget = successionTransition.checkpoint.confirmedDemonSuccessorTarget
                                            newDemonName = successionTransition.checkpoint.pendingNewDemonName
                                            unresolvedDemonSuccessor =
                                                successionTransition.continuation == NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR
                                        } else {
                                            newDemonName = promoteDemonSuccessorIfNeeded(
                                                impDeathWasSelfChosen = false,
                                            )
                                        }
                                    }
                                    if (nightDeathCard.clocktowerRole?.enName == "Klutz") {
                                        nightKlutzName = deathName
                                    }
                                    if (
                                        AbilityFunctioningSemantics.interactsAs(
                                            nightDeathCard.abilitySubject(clocktowerConfirmedPoisonTarget),
                                            "Ravenkeeper",
                                        ) && clocktowerRavenkeeperTarget != null
                                    ) {
                                        records.add(
                                            EliminationRecord(
                                                round,
                                                deathName,
                                                context.getString(
                                                    R.string.clocktower_record_ravenkeeper_check,
                                                    clocktowerRavenkeeperTarget!!,
                                                ),
                                            ),
                                        )
                                    }
                                } else {
                                    clocktowerPendingNightDeath = null
                                    addClocktowerEvent(
                                        ClocktowerEventType.Death,
                                        localizedText("平安夜", "No night death"),
                                        "",
                                    )
                                }
                            } else if (
                                !demonPoisonedTonight &&
                                !detailedAttackFailureRecorded &&
                                clocktowerPhase != ClocktowerPhase.FirstNight
                            ) {
                                addClocktowerEvent(
                                    ClocktowerEventType.Death,
                                    localizedText("平安夜", "No night death"),
                                    "",
                                )
                            }
                            val dawnPoisoner = cards.mapIndexedNotNull { index, card ->
                                card.clocktowerRole
                                    ?.takeIf { role -> role.enName == "Poisoner" }
                                    ?.let { role -> index + 1 to role }
                            }.firstOrNull()

                            val postDeathEffectiveNightState = ClocktowerEffectiveNightState(
                                effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
                                    (index + 1).takeIf { card.eliminatedRound == null }
                                }.toSet(),
                                effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                    card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                }.toMap(),
                            )

                            val durablePreviousPoisonTargetSeat =
                                NightDawnPoisonRecoveryAuthority.latestTargetSeatForRound(
                                    actionTimeline = clocktowerActionTimeline,
                                    round = round,
                                )

                            val ordinaryDawnPoisonIntent = dawnPoisoner?.let { (poisonerSeat, poisonerRole) ->
                                NightDawnResolutionPlanner.planPoisonCarry(
                                    baseGameState = baseGameState,
                                    checkpoint = currentClocktowerNightCheckpoint(),
                                    input = NightDawnPoisonResolutionInput(
                                        poisonerSeat = poisonerSeat,
                                        poisonerRoleId = RoleId(poisonerRole.enName),
                                        effectiveNightState = postDeathEffectiveNightState,
                                    ),
                                    durablePreviousPoisonTargetSeat = durablePreviousPoisonTargetSeat,
                                )
                            }

                            ordinaryDawnPoisonIntent?.let { poisonIntent ->
                                val poisonMaterialization = requireNotNull(
                                    NightDawnDurableMaterializationPlanner.plan(
                                        gameId = clocktowerGameId,
                                        round = round,
                                        intent = DawnCommitIntent(poisonCarry = poisonIntent),
                                        state = DawnDurableMaterializationState(
                                            aliveSeats = cards.mapIndexedNotNull { index, card ->
                                                (index + 1).takeIf { card.eliminatedRound == null }
                                            }.toSet(),
                                            roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                                card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                            }.toMap(),
                                            currentPhase = storytellerPhaseFor(),
                                            currentPoisonTargetSeat =
                                                clocktowerConfirmedPoisonTarget?.let(::clocktowerSeatFor),
                                            committedActionIds = clocktowerActionTimeline.entries
                                                .map { it.fact.actionId }
                                                .toSet(),
                                            committedObservationRecordIds = clocktowerEpistemicObservations
                                                .map { it.recordId }
                                                .toSet(),
                                        ),
                                        advanceToDawn = false,
                                    ).poison,
                                )

                                poisonMaterialization.actionIdToCommit?.let { actionId ->
                                    val localSequence = clocktowerEventCounter + 1
                                    recordClocktowerAction(
                                        ActionFactDraft.Poison(
                                            actionId = actionId,
                                            phase = storytellerPhaseFor(),
                                            round = round,
                                            sequence = localSequence,
                                            targetSeat = poisonMaterialization.intent.targetSeat,
                                        ),
                                    )
                                }

                                if (poisonMaterialization.stateMutationRequired) {
                                    requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
                                        targetSeat = poisonMaterialization.intent.targetSeat,
                                    )
                                    publishClocktowerSessionView()
                                    val poisonTargetName = poisonMaterialization.intent.targetSeat
                                        ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
                                    clocktowerConfirmedPoisonTarget = poisonTargetName
                                    clocktowerPoisonTarget = poisonTargetName
                                }
                            }

                            if (nightKlutzName != null) {
                                clocktowerPendingKlutzName = nightKlutzName
                                clocktowerKlutzChoiceName = null
                                clocktowerKlutzReturnToDawn = true
                                recordClocktowerPhaseAdvance(ClocktowerPhase.Day)
                                clocktowerPhase = ClocktowerPhase.Day
                                clocktowerDayModeState.value = ClocktowerDayMode.Klutz
                                captureKlutzLearnedDeathOnPublicAnnouncement(nightKlutzName)
                            }
                            val nightOutcome =
                                if (
                                    nightKlutzName == null &&
                                    newDemonName == null &&
                                    !unresolvedDemonSuccessor
                                ) {
                                    evaluateGameOutcome(context, cards, currentGameKind)
                                } else {
                                    null
                                }
                            gameOutcome = nightOutcome
                            if (nightOutcome != null) {
                                showResults = true
                                addOutcomeEvent(nightOutcome)
                            } else if (nightKlutzName == null && newDemonName != null) {
                                clocktowerPendingNewDemonName = newDemonName
                            } else if (nightKlutzName == null && !unresolvedDemonSuccessor) {
                                val dawnPhasePlan = NightDawnDurableMaterializationPlanner.plan(
                                    gameId = clocktowerGameId,
                                    round = round,
                                    intent = DawnCommitIntent(),
                                    state = DawnDurableMaterializationState(
                                        aliveSeats = cards.mapIndexedNotNull { index, card ->
                                            (index + 1).takeIf { card.eliminatedRound == null }
                                        }.toSet(),
                                        roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
                                            card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
                                        }.toMap(),
                                        currentPhase = storytellerPhaseFor(),
                                        committedActionIds = clocktowerActionTimeline.entries
                                            .map { it.fact.actionId }
                                            .toSet(),
                                        committedObservationRecordIds = clocktowerEpistemicObservations
                                            .map { it.recordId }
                                            .toSet(),
                                    ),
                                    advanceToDawn = true,
                                )
                                val phaseAdvance = requireNotNull(dawnPhasePlan.phaseAdvance)
                                phaseAdvance.actionIdToCommit?.let { actionId ->
                                    val localSequence = clocktowerEventCounter + 1
                                    recordClocktowerAction(ActionFactDraft.PhaseAdvance(
                                        actionId = actionId,
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = localSequence,
                                        nextPhase = phaseAdvance.targetPhase,
                                        nextRound = round,
                                    ))
                                }
                                if (phaseAdvance.stateMutationRequired) {
                                    clocktowerPhase = ClocktowerPhase.Dawn
                                }
                                resetClocktowerNightFlow()
                            }
                            clocktowerFortuneTellerFirst = null
                            clocktowerFortuneTellerSecond = null
                            clocktowerChambermaidFirst = null
                            clocktowerChambermaidSecond = null
                            clocktowerRavenkeeperTarget = null
                            clocktowerMonkProtectedTarget = null
                            clocktowerConfirmedMonkProtectedTarget = null
                            clocktowerMayorRedirectTarget = null
                            clocktowerConfirmedMayorRedirectTarget = null
                            if (clocktowerPendingNewDemonName == null) {
                                clocktowerDemonSuccessorTarget = null
                                clearConfirmedDemonSuccessorTarget()
                            }
                        },
                    )

                    Screen.Game -> GameScreen(
                    gameKind = currentGameKind,
                    onHostTools = {
                        hostToolTab = HostToolTab.Roles
                        showHostTools = true
                    },
                    cards = cards,
                    records = records,
                    round = round,
                    gameOutcome = gameOutcome,
                    selectedElimination = selectedElimination,
                    onSelectElimination = { selectedElimination = it },
                    onConfirmElimination = {
                        val name = selectedElimination
                        if (name != null) {
                            val index = cards.indexOfFirst { it.name == name }
                            if (index >= 0) {
                                cards[index] = cards[index].copy(eliminatedRound = round)
                                records.add(EliminationRecord(round, name))
                                selectedElimination = null
                                gameOutcome = evaluateGameOutcome(context, cards, currentGameKind)
                                if (gameOutcome != null) {
                                    showResults = true
                                }
                                round += 1
                            }
                        }
                    },
                    onShowResults = {
                        gameOutcome = gameOutcome ?: GameOutcome(
                            title = context.getString(R.string.outcome_manual_title),
                            summary = context.getString(R.string.outcome_manual_summary),
                            reason = context.getString(R.string.outcome_manual_reason),
                        )
                        showResults = true
                    },
                )
                        }
                    }
                }

                if (showResults) {
                    if (currentGameKind == GameKind.Clocktower) {
                        ClocktowerResultsDialog(
                            cards = cards,
                            outcome = gameOutcome,
                            onDismiss = { showResults = false },
                            onReview = {
                                showResults = false
                                hostToolTab = HostToolTab.Records
                                showHostTools = true
                            },
                            onNewGame = { showNewGameConfirmation = true },
                        )
                    } else {
                        ResultsDialog(
                            gameKind = currentGameKind,
                            cards = cards,
                            outcome = gameOutcome,
                            onDismiss = { showResults = false },
                            onReview = {
                                showResults = false
                                hostToolTab = HostToolTab.Records
                                showHostTools = true
                            },
                            onNewGame = { showNewGameConfirmation = true },
                        )
                    }
                }

                if (showHostTools) {
                    HostGameToolsScreen(
                        gameKind = currentGameKind,
                        cards = cards,
                        records = records,
                        events = clocktowerEvents,
                        history = gameHistory,
                        initialTab = hostToolTab,
                        settingsContent = {
                            SettingsContent(
                                languageMode = languageMode,
                                commonPlayers = commonPlayers,
                                playerExperienceByName = playerExperienceByName,
                                onPlayerExperienceChange = ::setPlayerExperience,
                                newCommonPlayerName = newCommonPlayerName,
                                onLanguageModeChange = { nextMode ->
                                    languageMode = nextMode
                                    appPreferencesStore.saveLanguageMode(nextMode)
                                },
                                onNewCommonPlayerNameChange = { newCommonPlayerName = it },
                                onAddCommonPlayer = ::addCommonPlayer,
                                onRemoveCommonPlayer = ::removeCommonPlayer,
                                aiSettingsSection = aiSettingsSection,
                            )
                        },
                        onDismiss = { showHostTools = false },
                        onNewGame = {
                            showHostTools = false
                            showNewGameConfirmation = true
                        },
                    )
                }

                if (showNewGameConfirmation) {
                    NewGameConfirmationDialog(
                        gameKind = currentGameKind,
                        onDismiss = { showNewGameConfirmation = false },
                        onManagePlayers = ::archiveAndReturnToPlayerManagement,
                        onQuickRestart = ::archiveAndStartNewGame,
                    )
                }
            }
        }
    }
}

private fun evaluateGameOutcome(context: Context, cards: List<PlayerCard>, gameKind: GameKind): GameOutcome? {
    val activeCards = cards.filter { it.eliminatedRound == null }
    if (gameKind == GameKind.Clocktower) {
        val activeDemons = activeCards.count { it.clocktowerTeam == ClocktowerTeam.Demon }
        return when {
            activeDemons == 0 -> GameOutcome(
                title = context.getString(R.string.outcome_clocktower_good_title),
                summary = context.getString(R.string.outcome_clocktower_good_summary),
                reason = context.getString(R.string.outcome_clocktower_good_reason),
            )

            activeCards.size <= 2 -> GameOutcome(
                title = context.getString(R.string.outcome_clocktower_evil_title),
                summary = context.getString(R.string.outcome_clocktower_evil_summary),
                reason = context.getString(R.string.outcome_clocktower_evil_reason, activeCards.size, activeDemons),
            )

            else -> null
        }
    }

    val activeCivilians = activeCards.count { it.role == Role.Civilian }
    val activeUndercovers = activeCards.count { it.role == Role.Undercover }
    val activeBlanks = activeCards.count { it.role == Role.Blank }

    return when {
        activeUndercovers == 0 && activeBlanks == 0 -> GameOutcome(
            title = context.getString(R.string.outcome_civilian_title),
            summary = context.getString(R.string.outcome_civilian_summary),
            reason = context.getString(R.string.outcome_civilian_reason, activeCivilians),
        )

        activeUndercovers > 0 && activeUndercovers >= activeCivilians -> GameOutcome(
            title = context.getString(R.string.outcome_undercover_title),
            summary = context.getString(R.string.outcome_undercover_summary),
            reason = context.getString(R.string.outcome_undercover_reason, activeUndercovers, activeCivilians),
        )

        activeCivilians == 0 && activeUndercovers == 0 && activeBlanks > 0 -> GameOutcome(
            title = context.getString(R.string.outcome_blank_title),
            summary = context.getString(R.string.outcome_blank_summary),
            reason = context.getString(R.string.outcome_blank_reason, activeBlanks),
        )

        else -> null
    }
}


@Composable
internal fun EmptyStateCard(text: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            color = Color(0xFF6F7B74),
        )
    }
}

internal fun PlayerCard.clocktowerHostRoleLabel(language: String): String? =
    clocktowerRole?.nameFor(language) ?: actualRoleLabel ?: roleLabel

internal fun PlayerCard.hostRoleLabel(context: Context, gameKind: GameKind): String = when (gameKind) {
    GameKind.Clocktower -> clocktowerHostRoleLabel(context.resources.configuration.locales[0].language)
        ?: context.getString(role.labelResId())
    GameKind.Undercover -> context.getString(role.labelResId())
}

internal fun PlayerCard.seatLabel(cards: List<PlayerCard>): String =
    "#${cards.indexOfFirst { it.name == name } + 1} $name"

internal fun playerSeatLabel(cards: List<PlayerCard>, playerName: String?): String =
    cards.firstOrNull { it.name == playerName }?.seatLabel(cards) ?: playerName.orEmpty()
