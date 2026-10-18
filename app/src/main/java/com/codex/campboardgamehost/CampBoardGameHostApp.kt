package com.codex.campboardgamehost

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.session.OpeningPlayerExperienceV1
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
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
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.session.StorytellerCausalDecisionJournalV1
import com.codex.campboardgamehost.clocktower.session.DayAbilityRegistrationRulingProducerV1
import com.codex.campboardgamehost.clocktower.rules.NoGreaterJoyKlutzChoiceRuleV1
import com.codex.campboardgamehost.clocktower.session.ConfirmedDaySlayerRegistrationV1
import com.codex.campboardgamehost.clocktower.session.ConfirmedDayVirginSpyRegistrationV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.commitConfirmedActualRoleChange
import com.codex.campboardgamehost.clocktower.session.commitDawnPoisonCarry
import com.codex.campboardgamehost.clocktower.session.expirePoisonAtDusk
import com.codex.campboardgamehost.clocktower.session.DawnCommitIdentity
import com.codex.campboardgamehost.clocktower.session.DawnPoisonCarryIntent
import com.codex.campboardgamehost.clocktower.session.commitConfirmedPoisonTransition
import com.codex.campboardgamehost.clocktower.session.confirmedPoisonTargetName
import com.codex.campboardgamehost.clocktower.session.commitConfirmedPlayerDeath
import com.codex.campboardgamehost.clocktower.session.NightDawnPoisonResolutionInput
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
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
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
import java.util.Locale
import java.util.UUID

private enum class Screen {
    Landing,
    Setup,
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
}

/**
 * One unconfirmed current-step AI suggestion. It is neither a Session fact
 * nor a second history. The payload/receipt exist only to validate actual
 * player publication and promote advisory strategy at that moment.
 */
private data class PendingCurrentStepAdvice(
    val recommendation: StorytellerResidualPackageGateV2.Accepted,
    val requestKey: String,
    val request: StorytellerProviderRequestV1,
    val proposal: StorytellerStrategicContinuationV2.Result,
    val receiptId: String,
    val model: String,
)

private data class PendingTroubleBrewingDrunkSelection(
    val preparedGameId: String,
    val preparedSetup: TroubleBrewingPreparedSetup,
    val request: TroubleBrewingDrunkSelectionRequest,
    val decision: PendingDrunkAssignmentDecision,
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

internal const val A4_IDENTITY_PREWARM_LOG_TAG = "A4IdentityPrewarm"
internal const val A4_OBSERVATION_CACHE_UPDATE_LOG_TAG = "A4ObservationCacheUpdate"
internal const val MIN_CLOCKTOWER_PLAYERS = 5
internal const val MAX_PLAYERS = 15

private fun Context.localized(languageMode: LanguageMode): Context {
    if (languageMode == LanguageMode.System) return this
    val locale = Locale(languageMode.prefsValue)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
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
    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
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
    // An advisory opening plan is never a committed package of future facts.
    var openingPlanRecommendation by remember {
        mutableStateOf<HostOpeningSemanticAcceptanceV2?>(null)
    }
    var strategicPolicyCursor by remember {
        mutableStateOf<StorytellerStrategicContinuationV2.MemoryCursor?>(null)
    }
    var strategicLastReviewedHost by remember {
        mutableStateOf<StorytellerStrategicChangeCheckpointV2.Baseline?>(null)
    }
    // Current-step recommendation only: never a separate adoption or game-state fact.
    var pendingCurrentStepAdvice by remember { mutableStateOf<PendingCurrentStepAdvice?>(null) }
    var openingRecommendedInformation by remember { mutableStateOf<Map<Int,
        com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2>>(emptyMap()) }
    var openingRecommendedBluffRoleNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var openingRecommendedRedHerringSeat by remember { mutableStateOf<Int?>(null) }
    var shownOnlyDealCompleted by remember { mutableStateOf(false) }
    var liveAiAdviceKey by remember { mutableStateOf<String?>(null) }
    var liveAiAdviceText by remember { mutableStateOf<String?>(null) }
    var liveAiAdviceBusy by remember { mutableStateOf(false) }
    var liveAiAdviceError by remember { mutableStateOf<String?>(null) }
    val drunkAiScope = rememberCoroutineScope()
    var showHostTools by remember { mutableStateOf(false) }
    var hostToolTab by remember { mutableStateOf(HostToolTab.Roles) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var currentDealIndex by remember { mutableStateOf(0) }
    // The Demon may revise this while awake. Only the confirmed attack may
    // reach protection, Mayor redirection, death, or succession resolution.
    var clocktowerDemonAttackDraftTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerSelectedExecution by remember { mutableStateOf<String?>(null) }
    var clocktowerPoisonTarget by remember { mutableStateOf<String?>(null) }
    // A target is provisional while the Poisoner is still awake. It becomes a
    // mechanical fact only when the night step is advanced.
    var clocktowerFortuneTellerFirst by remember { mutableStateOf<String?>(null) }
    var clocktowerFortuneTellerSecond by remember { mutableStateOf<String?>(null) }
    var clocktowerChambermaidFirst by remember { mutableStateOf<String?>(null) }
    var clocktowerChambermaidSecond by remember { mutableStateOf<String?>(null) }
    var clocktowerRavenkeeperTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerButlerMaster by remember { mutableStateOf<String?>(null) }
    var clocktowerMonkProtectedTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerMayorRedirectTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingNewDemonName by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingNightNewDemonIdentityName by remember { mutableStateOf<String?>(null) }
    var clocktowerDemonSuccessorTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerArtistUsed by remember { mutableStateOf(false) }
    var clocktowerArtistClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerLastExecutedName by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingKlutzName by remember { mutableStateOf<String?>(null) }
    var clocktowerKlutzChoiceName by remember { mutableStateOf<String?>(null) }
    var clocktowerKlutzReturnToDawn by remember { mutableStateOf(false) }
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var clocktowerGameSession by remember { mutableStateOf<ClocktowerGameSession?>(null) }
    var clocktowerSessionView by remember { mutableStateOf<ClocktowerSessionView?>(null) }
    val clocktowerPhase by remember {
        derivedStateOf {
            when (clocktowerSessionView?.phase ?: StorytellerPhase.FIRST_NIGHT) {
                StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
                StorytellerPhase.DAY -> ClocktowerPhase.Day
                StorytellerPhase.DAWN -> ClocktowerPhase.Dawn
                StorytellerPhase.NIGHT -> ClocktowerPhase.Night
            }
        }
    }
    val round by remember { derivedStateOf { clocktowerSessionView?.round ?: 1 } }
    val currentClocktowerScript = clocktowerSessionView?.scriptId
        ?.let { scriptId ->
            ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
        }
        ?: ClocktowerScript.TroubleBrewing
    val clocktowerGameId = clocktowerSessionView?.gameId.orEmpty()
    val clocktowerGameSeed = clocktowerSessionView?.gameSeed ?: 0L
    val clocktowerGameStateRevision = clocktowerSessionView?.gameStateRevision ?: 0L
    val clocktowerPlayerInputRevision = clocktowerSessionView?.playerInputRevision ?: 0L
    val clocktowerNextTimelineGlobalSequence =
        clocktowerSessionView?.nextTimelineGlobalSequence ?: 0L
    val clocktowerActionTimeline = clocktowerSessionView?.actionTimeline ?: ActionFactTimeline()
    // One-shot consumptions belong to the committed action timeline, not Compose mutable flags.
    val clocktowerVirginUsed = clocktowerActionTimeline.entries.any { entry ->
        (entry.fact as? com.codex.campboardgamehost.clocktower.domain.ActionFact.Nomination)
            ?.firstVirginNomination == true
    }
    val clocktowerSlayerUsed = clocktowerActionTimeline.entries.any { entry ->
        (entry.fact as? com.codex.campboardgamehost.clocktower.domain.ActionFact.SlayerShot)
            ?.abilityConsumed == true
    }
    val clocktowerEpistemicObservations =
        clocktowerSessionView?.epistemicObservationLog?.records.orEmpty()
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
    val setupCards = remember { mutableStateListOf<PlayerCard>() }
    // The live game has a read-only card presentation of Session facts.
    // Only setup cards are mutable.
    val cards: List<PlayerCard> = remember(setupCards, language) {
        ClocktowerSessionPlayerCards(
            setupCards = setupCards,
            currentSession = {
                clocktowerGameSession?.state
            },
            language = language,
        )
    }
    val clocktowerSlayerClaimedNames = clocktowerActionTimeline.entries
        .mapNotNull { entry ->
            (entry.fact as? com.codex.campboardgamehost.clocktower.domain.ActionFact.SlayerShot)
                ?.claimantSeat?.let { seat -> cards.getOrNull(seat - 1)?.name }
        }.distinct()
    // Accepted setup choices come from Session, not separate writable Compose state.
    val clocktowerRedHerring = clocktowerSessionView?.redHerringSeat
        ?.let { seat -> cards.getOrNull(seat - 1)?.name }
    val clocktowerRecommendedDemonBluffRoleNames =
        clocktowerSessionView?.demonBluffRoleIds?.map(RoleId::value).orEmpty()
    // This is a derived validity check, not a second mutable truth or stale-plan cache.
    // A confirmed Host action or an overridden published clue can invalidate
    // the whole linked opening proposal while leaving actual Session facts intact.
    val openingHintsUsable = openingPlanRecommendation?.let { opening ->
        clocktowerGameSession?.state?.let { actual ->
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
    var redHerringDraftName by remember { mutableStateOf<String?>(null) }
    val clocktowerNightStartedState = remember { mutableStateOf(false) }
    val clocktowerNightStepIndexState = remember { mutableStateOf(0) }
    val clocktowerDayModeState = remember { mutableStateOf(ClocktowerDayMode.Overview) }
    val clocktowerGhostVoteAuthority = ClocktowerGhostVoteAuthority.from(clocktowerActionTimeline)
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
    var pendingA4ObservationRecordId by remember(clocktowerGameId) { mutableStateOf<String?>(null) }
    var a4ObservationCacheRebuildRequest by remember { mutableStateOf<A4ObservationCacheRebuildRequest?>(null) }
    val a4ShadowLifecycleInvalidator = remember(a4ShadowWorldSetCache, clocktowerGameId) {
        A4ShadowLifecycleInvalidator(
            invalidateGame = a4ShadowWorldSetCache::invalidateGame,
            clearPendingObservation = { pendingA4ObservationRecordId = null },
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

    // Read live Session targets, including changes made earlier in the same callback.
    fun confirmedNightAttackName(): String? = clocktowerGameSession?.state?.let {
        it.nightTargets.attackSeat?.let(it.gameState::playerAt)?.name
    }
    fun confirmedNightMonkName(): String? = clocktowerGameSession?.state?.let {
        it.nightTargets.monkSeat?.let(it.gameState::playerAt)?.name
    }
    fun confirmedNightMayorName(): String? = clocktowerGameSession?.state?.let {
        it.nightTargets.mayorRedirectSeat?.let(it.gameState::playerAt)?.name
    }
    fun confirmedNightSuccessorName(): String? = clocktowerGameSession?.state?.let {
        it.nightTargets.successorSeat?.let(it.gameState::playerAt)?.name
    }
    fun clearConfirmedDemonSuccessorTarget() {
        requireClocktowerGameSession().clearPendingDemonSuccessor()
        publishClocktowerSessionView()
    }

    fun confirmedClocktowerPoisonTarget(): String? =
        clocktowerGameSession?.state?.confirmedPoisonTargetName(round)

    fun currentClocktowerCausalJournal(): StorytellerCausalDecisionJournalV1 =
        requireClocktowerGameSession().causalDecisionJournal

    fun currentTroubleBrewingFirstNightPairDecisionContext(): TroubleBrewingFirstNightPairDecisionContext? {
        if (
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
        val session = clocktowerGameSession ?: return null
        if (!BuildConfig.DEBUG || currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            session.state.gameState.players.size != 5 ||
            session.state.gameState.players.any { it.shownRole == null }
        ) return null
        val snapshot = session.toGameSnapshot(activeRuleset)
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, round = 1)
        val perceivedRolesBySeat = session.state.gameState.players.associate { player ->
            player.seat to requireNotNull(player.shownRole)
        }
        return A4IdentityRevealPrewarmRequest(
            formal = formal,
            playerInputRevision = clocktowerPlayerInputRevision,
            knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
                formal = formal,
                perceivedRolesBySeat = perceivedRolesBySeat,
                observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
            ).associateBy(PlayerKnowledgeSnapshot::recipientSeat),
            revealOrder = session.state.gameState.players.map { it.seat },
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
        )
    }

    fun a4ObservationCacheRebuildRequestOrNull(recordId: String): A4ObservationCacheRebuildRequest? {
        val activeRuleset = clocktowerRulesetRef ?: return null
        val session = clocktowerGameSession ?: return null
        if (!BuildConfig.DEBUG || currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            session.state.gameState.players.size != 5 ||
            session.state.gameState.players.any { it.shownRole == null }
        ) return null
        val record = clocktowerEpistemicObservations.singleOrNull { it.recordId == recordId } ?: return null
        val snapshot = session.toGameSnapshot(activeRuleset)
        val formal = FormalGameState.from(snapshot, record.phase, record.round)
        return A4ObservationCacheRebuildRequest(
            formal = formal,
            playerInputRevision = clocktowerPlayerInputRevision,
            perceivedRolesBySeat = session.state.gameState.players.associate { player ->
                player.seat to requireNotNull(player.shownRole)
            },
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
        currentClocktowerScript,
        clocktowerGameId,
        clocktowerGameStateRevision,
        clocktowerPlayerInputRevision,
        clocktowerRulesetRef,
        identityRevealAssignmentFingerprint,
    ) {
        val eligible = BuildConfig.DEBUG && identityRevealActive &&
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
        confirmedAttackTarget = confirmedNightAttackName(),
        attackDraftTarget = clocktowerDemonAttackDraftTarget,
        confirmedPoisonTarget = confirmedClocktowerPoisonTarget(),
        poisonDraftTarget = clocktowerPoisonTarget,
        confirmedMonkTarget = confirmedNightMonkName(),
        monkDraftTarget = clocktowerMonkProtectedTarget,
        confirmedMayorRedirectTarget = confirmedNightMayorName(),
        mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
        pendingNewDemonName = clocktowerPendingNewDemonName,
        pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
        demonSuccessorDraftTarget = clocktowerDemonSuccessorTarget,
        confirmedDemonSuccessorTarget = confirmedNightSuccessorName(),
        nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
    )

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
    fun commitClocktowerConfirmedAction(draft: ActionFactDraft) {
        val session = requireClocktowerGameSession()
        session.commitConfirmedGameAction(
            draft = draft,
            nextGameState = session.state.gameState,
            expectedGameStateRevision = session.state.gameStateRevision,
        )
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
    }

    /** An announced death is learned at the real Host transition into the Klutz public choice.
     * No replay/backfill from an earlier death or a localized note is permitted.
     */
    fun captureKlutzLearnedDeathOnPublicAnnouncement(name: String) {
        if (currentClocktowerScript != ClocktowerScript.NoGreaterJoy) return
        val seat = clocktowerSeatFor(name)
        val source = requireClocktowerGameSession()
        NoGreaterJoyKlutzHistoryProducerV1.learned(
            source.state, seat,
            clocktowerActionId(kind = "klutz-learned", targetSeat = seat),
            round, requireClocktowerGameSession().nextInputSequence,
        )?.let(::commitClocktowerConfirmedAction)
    }

    fun materializeClocktowerPoisonExpiryAtDusk() {
        val session = requireClocktowerGameSession()
        if (session.expirePoisonAtDusk(session.state.gameStateRevision)) {
            publishClocktowerSessionView()
            invalidateA4RevisionScope()
        }
        clocktowerPoisonTarget = null
    }

    fun commitClocktowerDawnPoison(intent: DawnPoisonCarryIntent) {
        val session = requireClocktowerGameSession()
        if (session.commitDawnPoisonCarry(intent, session.state.gameStateRevision)) {
            publishClocktowerSessionView()
            invalidateA4RevisionScope()
        }
        clocktowerPoisonTarget = session.state.confirmedPoisonTargetName(round)
    }

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
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
    }

    /**
     * React ONLY to an actual newly committed Session observation. A verified
     * current-step model answer is not automatically an accepted Host ruling.
     * If the Storyteller chose a different answer, Session truth wins and the
     * next model call reconsiders it from the retained history prefix.
     */
    fun reviewConfirmedCurrentStepAdvice(
        before: ClocktowerSessionState,
        draft: EpistemicObservationDraft,
    ) {
        val pending = pendingCurrentStepAdvice ?: return
        pendingCurrentStepAdvice = null
        val session = clocktowerGameSession ?: return
        if (storytellerOperationMode == StorytellerOperationMode.MANUAL ||
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

    fun recordEpistemicObservation(draft: EpistemicObservationDraft) {
        val session = requireClocktowerGameSession()
        // A callback may have already published another observation since the Compose
        // snapshot was captured. Deduplicate against the live Session revision.
        val before = session.state
        val committed = session.commitGlobalEpistemicObservation(draft)
        if (session.view.playerInputRevision == before.playerInputRevision) return
        reviewConfirmedCurrentStepAdvice(before, draft)
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
        pendingA4ObservationRecordId = committed.recordId
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
            BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                strategicPolicyCursor?.version ?: 0,
                draft.phase.name, draft.round)
        }
    }

    fun commitClocktowerPrivateInformationPublication(draft: EpistemicObservationDraft) {
        val session = requireClocktowerGameSession()
        val before = session.state
        val committed = session.commitGlobalEpistemicObservation(draft)
        if (session.state.playerInputRevision == before.playerInputRevision) return
        reviewConfirmedCurrentStepAdvice(before, draft)
        publishClocktowerSessionView()
        invalidateA4RevisionScope()
        pendingA4ObservationRecordId = committed.recordId
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
            BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                strategicPolicyCursor?.version ?: 0,
                draft.phase.name, draft.round)
        }
    }

    fun preflightClocktowerPublicAliveObservation(
        playerName: String,
        eventSequence: Int,
        eventPhase: ClocktowerPhase = clocktowerPhase,
        eventRound: Int = round,
        recordId: String? = null,
    ) {
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

    fun localizedText(zh: String, en: String): String = if (language == "en") en else zh

    fun resetClocktowerNightFlow() {
        clocktowerNightStartedState.value = false
        clocktowerNightStepIndexState.value = 0
    }

    fun resetClocktowerDayFlow() {
        clocktowerDayModeState.value = ClocktowerDayMode.Overview
    }

    fun resetClocktowerFlow() {
        resetClocktowerNightFlow()
        resetClocktowerDayFlow()
    }

    SideEffect {
        pendingA4ObservationRecordId?.let { recordId ->
            pendingA4ObservationRecordId = null
            a4ObservationCacheRebuildRequest = a4ObservationCacheRebuildRequestOrNull(recordId)
        }
    }

    fun addCurrentPlayer(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty() && playerNames.size < MAX_PLAYERS && trimmedName !in playerNames) {
            playerNames.add(trimmedName)
        }
    }

    fun removeCurrentPlayer(index: Int) {
        if (index in playerNames.indices) {
            playerNames.removeAt(index)
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
        clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
        preparedClocktowerSeed: Long? = null,
        preparedClocktowerInitialState: GameState? = null,
        preparedClocktowerGameId: String? = null,
    ) {
        invalidateA4SessionBoundary()
        pendingTroubleBrewingDrunkSelection = null
        pendingCurrentStepAdvice = null
        committedTroubleBrewingSetupRotationRecord = null
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        clocktowerGameSession = null
        publishClocktowerSessionView()
        currentDealIndex = 0
        showResults = false
        gameOutcome = null

        
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
        
        clocktowerDemonAttackDraftTarget = null
        clocktowerSelectedExecution = null
        clocktowerPoisonTarget = null
        clocktowerFortuneTellerFirst = null
        clocktowerFortuneTellerSecond = null
        clocktowerChambermaidFirst = null
        clocktowerChambermaidSecond = null
        clocktowerRavenkeeperTarget = null
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerMayorRedirectTarget = null
        clocktowerPendingNewDemonName = null
        clocktowerPendingNightNewDemonIdentityName = null
        clocktowerDemonSuccessorTarget = null
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerLastExecutedName = null
        clocktowerPendingKlutzName = null
        clocktowerKlutzChoiceName = null
        clocktowerKlutzReturnToDawn = false
        resetClocktowerFlow()
        screen = Screen.PassPhone
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
            canonicalGameState = session.state.gameState,
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
        pendingCurrentStepAdvice = null
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
        val wholeOpening = openingPlanRecommendation
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
                    advisoryPlanContradictsConfirmedState = openingPlanConflictsWithHost)
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
                                roleLabel = shownRole.nameFor(language),
                actualRoleLabel = role.nameFor(language),
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shownRole,
            )
        }

        setupCards.clear()
        setupCards.addAll(committedCards)

        resetDealState(
            clocktowerScript = ClocktowerScript.TroubleBrewing,
            preparedClocktowerSeed = preparedSeed,
            preparedClocktowerInitialState = committedSetup.gameState,
            preparedClocktowerGameId = preparedGameId,
        )
        committedTroubleBrewingSetupRotationRecord =
            TroubleBrewingSetupRotationRecordFactory.fromCommittedSetup(
                preparedSetup = preparedSetup,
                committedSetup = committedSetup,
                characterRegistry = characterRegistry,
            )
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

    /**
     * The first actual choice after shown-only role handoff is the Drunk.
     * The opening response is only an advisory joint plan, never a bundle commit.
     */
    fun confirmJointOpeningDrunk(
        pending: PendingTroubleBrewingJointOpening,
        candidate: TroubleBrewingDrunkCandidate?,
    ) {
        val accepted = jointOpeningResult ?: return
        if (pendingJointOpening !== pending ||
            !accepted.selection.validFor(pending.domain.scope) ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL) return
        val legalRoute = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (legalRoute) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> require(candidate == null)
            is TroubleBrewingDrunkSelectionRoute.RuleDetermined ->
                require(candidate == legalRoute.candidate)
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
            preparedGameId = pending.preparedGameId,
            preparedSetup = pending.preparedSetup,
            confirmedDrunkCandidate = candidate,
        )
        // The recommendation is advice about this game, not a committed plan.
        // An overridden Drunk must invalidate all dependent opening display hints.
        openingPlanRecommendation = accepted
        openingRecommendedInformation = if (recommendationMatchesActualDrunk) {
            val projection = pending.domain.forDrunkSeat(selectedSeat)
            accepted.selection.choices.mapNotNull { (id, option) ->
                projection.candidatesByDecision[id]?.get(option)?.let { meaning ->
                    meaning.sourceSeat to meaning
                }
            }.toMap()
        } else emptyMap()
        openingRecommendedBluffRoleNames = if (recommendationMatchesActualDrunk) {
            val registry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
            accepted.selection.demonBluffs.orEmpty().map { externalId ->
                requireNotNull(registry.findByExternalId(externalId)).id.value
            }
        } else emptyList()
        openingRecommendedRedHerringSeat = if (recommendationMatchesActualDrunk) {
            val redId = accepted.selection.choices["setup.red-herring"]
            pending.domain.forDrunkSeat(selectedSeat).redHerringCandidatesBySeat
                .entries.singleOrNull { it.value == redId }?.key
        } else null
        strategicPolicyCursor = accepted.strategyMemory?.let {
            StorytellerStrategicContinuationV2.MemoryCursor.initial(accepted)
        }
        canonicalConversationResponseId =
            pendingOpeningResponseId.takeIf { recommendationMatchesActualDrunk }
        canonicalConversationModel =
            pendingOpeningModel.takeIf { recommendationMatchesActualDrunk }
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        jointOpeningResult = null
        strategicLastReviewedHost = clocktowerGameSession?.let { committedSession ->
            StorytellerStrategicChangeCheckpointV2.capture(committedSession,
                StorytellerStrategicChangeCheckpointV2.Position(
                    clocktowerPhase.toStorytellerPhase(), round))
        }
        screen = Screen.ClocktowerJudge
    }

    fun beginJointOpeningAtDrunkStep() {
        val pending = pendingJointOpening ?: return
        val accepted = jointOpeningResult ?: return
        if (screen != Screen.ClocktowerJointReview || jointOpeningBusy ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            !accepted.selection.validFor(pending.domain.scope)) return
        val legal = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        val recommended = when (legal) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                require(accepted.selection.drunkSeat == null)
                null
            }
            is TroubleBrewingDrunkSelectionRoute.RuleDetermined -> {
                require(accepted.selection.drunkSeat == legal.candidate.seat)
                legal.candidate
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection ->
                requireNotNull(legal.request.candidates.singleOrNull {
                    it.seat == accepted.selection.drunkSeat
                }) { "Joint Drunk recommendation no longer belongs to the Host legal domain." }
        }
        if (legal !is TroubleBrewingDrunkSelectionRoute.ManualSelection ||
            storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC
        ) {
            confirmJointOpeningDrunk(pending, recommended)
            return
        }
        val registry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
            pending.preparedGameId, pending.preparedSetup.intermediateSetup)
        val decision = DrunkAssignmentDecisionBoundary.create(
            snapshot, registry, StorytellerDecisionRevision(0, 0))
        require(legal.request.candidates.map { it.seat to it.shownRoleId } ==
            decision.legalCandidates.map { it.payload.seat to it.payload.shownRoleId })
        pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
            pending.preparedGameId, pending.preparedSetup,
            legal.request.copy(recommendedCandidate = recommended),
            decision,
        )
        screen = Screen.ClocktowerDrunkSelection
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
                pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
                    pending.preparedGameId, pending.preparedSetup, route.request,
                    decision)
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
        openingPlanRecommendation = null
        openingRecommendedBluffRoleNames = emptyList()
        openingRecommendedRedHerringSeat = null
        redHerringDraftName = null
        pendingOpeningResponseId = null
        pendingOpeningModel = null
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        strategicPolicyCursor = null
        strategicLastReviewedHost = null
        openingRecommendedInformation = emptyMap()
        pendingCurrentStepAdvice = null
        shownOnlyDealCompleted = false
        pendingJointOpening = null
        jointOpeningResult = null
        jointOpeningBusy = false
        jointOpeningError = null
        jointRevealIndex = 0
        pendingTroubleBrewingDrunkSelection = null
        val playerNames = hostSeatingSetupFlow.playerNamesFor()
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
                require(route.request.candidates.map { it.seat to it.shownRoleId } ==
                    decision.legalCandidates.map { it.payload.seat to it.payload.shownRoleId }) {
                    "Displayed Drunk candidates must exactly match Host's legal domain."
                }
                val pending = PendingTroubleBrewingDrunkSelection(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    request = route.request,
                    decision = decision,
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
        val playerNames = hostSeatingSetupFlow.playerNamesFor()
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
                                roleLabel = shownRole.nameFor(language),
                actualRoleLabel = role.nameFor(language),
                clocktowerTeam = role.team,
                clocktowerRole = role,
                clocktowerShownRole = shownRole,
            )
        }
        setupCards.clear()
        setupCards.addAll(committedCards)
        resetDealState(clocktowerScript = script, preparedClocktowerSeed = preparedSeed)
    }

    fun persistCompletedTroubleBrewingSetupIfNeeded(): Boolean {
        if (currentClocktowerScript != ClocktowerScript.TroubleBrewing) return true
        if (gameOutcome == null) return true
        val record = committedTroubleBrewingSetupRotationRecord ?: return true
        return TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
            .recordCompletedGame(
                gameId = clocktowerGameId,
                record = record,
            )
    }

    fun finishCurrentGameForRestart(): Boolean {
        if (cards.isEmpty()) return false
        if (!persistCompletedTroubleBrewingSetupIfNeeded()) return false
        invalidateA4SessionBoundary()
        clocktowerGameSession = null
        publishClocktowerSessionView()
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
    }

    fun returnToPlayerManagement() {
        if (!finishCurrentGameForRestart()) return
        setupCards.clear()
        gameOutcome = null
        currentDealIndex = 0
        resetClocktowerFlow()
        screen = Screen.Setup
    }

    fun restartGame() {
        val restartScript = currentClocktowerScript
        if (restartScript != null && !readyForNewClocktowerGame(restartScript)) {
            // Do not leave the old game when the selected AI transport cannot start the next one.
            settingsReturnScreen = screen
            screen = Screen.Settings
            showNewGameConfirmation = false
            return
        }
        if (!finishCurrentGameForRestart()) return
        // A five/six-player quick restart must not silently switch scripts.
        selectedClocktowerScript = restartScript
        startClocktowerGame()
    }

    /** Confirm real role changes once; card presentation reads Session. */
    fun commitClocktowerActualRole(
        playerName: String,
        nextRole: ClocktowerRole,
        actionId: String? = null,
    ) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            val targetSeat = index + 1
            val session = requireClocktowerGameSession()
            val previous = actionId?.let { id -> session.state.actionTimeline.entries.firstOrNull { it.fact.actionId == id } }
            val localSequence = previous?.point?.sequence ?: session.nextInputSequence
            val beforeRevision = session.state.gameStateRevision
            val draft = ActionFactDraft.RoleChange(
                actionId = actionId ?: clocktowerActionId(
                    kind = "role-change-${nextRole.enName.lowercase().replace(' ', '-')}",
                    localSequence = localSequence,
                    targetSeat = targetSeat,
                ),
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
            )
            requireClocktowerGameSession().commitConfirmedActualRoleChange(
                draft = draft,
                expectedGameStateRevision = requireClocktowerGameSession().state.gameStateRevision,
            )
            if (session.state.gameStateRevision != beforeRevision) {
                publishClocktowerSessionView()
                invalidateA4RevisionScope()
            }
        }
    }

    fun promoteScarletWomanIfNeeded(): String? {
        val alivePlayers = cards.filter { it.eliminatedRound == null }
        val functioningScarletWomanSeat = cards.indexOfFirst { card ->
            card.eliminatedRound == null && AbilityFunctioningSemantics.functionsAs(
                card.abilitySubject(confirmedClocktowerPoisonTarget()),
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
        commitClocktowerActualRole(scarletWoman.name, imp)
        return scarletWoman.name
    }

    fun promoteDemonSuccessorIfNeeded(
        impDeathWasSelfChosen: Boolean,
    ): String? {
        if (impDeathWasSelfChosen) return null
        return promoteScarletWomanIfNeeded()
    }

    fun reopenSeatingSetup() {
        hostSeatingSetupFlow = hostSeatingSetupFlow.reopenSeating()
        screen = Screen.Setup
    }

    BackHandler(enabled = screen == Screen.ClocktowerSettings) {
        reopenSeatingSetup()
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
                        onStartGame = {
                            hostSeatingSetupFlow = HostSeatingSetupFlow()
                            screen = Screen.Setup
                        },
                    )

                    Screen.Setup -> SeatingFirstSetupScreen(
                    commonPlayers = commonPlayers,
                    playerExperienceByName = playerExperienceByName,
                    onPlayerExperienceChange = ::setPlayerExperience,
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onRemoveCurrentPlayer = ::removeCurrentPlayer,
                    onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
                    onOpenSettings = {
                        settingsReturnScreen = Screen.Setup
                        screen = Screen.Settings
                    },
                    onConfirmSeats = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.confirmSeats(playerNames)
                        clocktowerSetupStep = 0
                        screen = Screen.ClocktowerSettings
                    },
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
                            reopenSeatingSetup()
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
                            onContinue = ::beginJointOpeningAtDrunkStep,
                            autoContinue = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC,
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
                                pendingTroubleBrewingDrunkSelection = null
                                screen = if (pendingJointOpening != null) {
                                    Screen.ClocktowerJointReview
                                } else Screen.ClocktowerSettings
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
                                val opening = pendingJointOpening?.takeIf {
                                    it.preparedGameId == pending.preparedGameId &&
                                        storytellerOperationMode != StorytellerOperationMode.MANUAL
                                }
                                if (opening != null) {
                                    confirmJointOpeningDrunk(opening, candidate)
                                } else {
                                    commitAndStartTroubleBrewingGame(
                                        preparedGameId = pending.preparedGameId,
                                        preparedSetup = pending.preparedSetup,
                                        confirmedDrunkCandidate = candidate,
                                    )
                                }
                                if (opening == null && shownOnlyDealCompleted) {
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
                        current = currentDealIndex + 1,
                        total = cards.size,
                        onReveal = { screen = Screen.RevealCard },
                        onPrevious = {
                            if (currentDealIndex > 0) {
                                currentDealIndex -= 1
                            }
                        },
                        onHostTools = {
                            
                                hostToolTab = HostToolTab.Roles
                                showHostTools = true
                            
                        },
                        onNext = {
                            
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
                            
                        },
                    )

                    Screen.RevealCard -> RevealCardScreen(
                        card = cards[currentDealIndex],
                        current = currentDealIndex + 1,
                        total = cards.size,
                        onHide = {
                            screen = Screen.PassPhone
                        },
                    )

                    Screen.ClocktowerAutoPause -> StorytellerAutoPauseScreen(
                        hasGlobalPlan = openingPlanRecommendation != null,
                        language = language,
                        onTakeOverManually = {
                            storytellerOperationMode = StorytellerOperationMode.MANUAL
                            screen = Screen.ClocktowerJudge
                        },
                    )

                    Screen.ClocktowerJudge -> ClocktowerJudgeScreen(
                        allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                        cards = cards,
                        sessionGameState = requireClocktowerGameSession().state.gameState,
                        actualInformationHistory = clocktowerEpistemicObservations,
                        confirmedGrimoireSeats = requireClocktowerGameSession().state.gameState
                            .toHostSeatPresentations { roleId -> clocktowerRoleLabel(roleId, language) },
                        confirmedRedHerringSeat = requireClocktowerGameSession().state.redHerringSeat,
                        script = currentClocktowerScript,
                        gameId = clocktowerGameId,
                        gameSeed = clocktowerGameSeed,
                        firstNightPairDecisionContext = currentTroubleBrewingFirstNightPairDecisionContext(),
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
                            val latest = clocktowerGameSession?.state
                            val plan = pending.recommendation
                            pending.requestKey == liveAiAdviceKey &&
                                latest?.gameId == plan.gameId &&
                                latest.gameStateRevision == plan.gameStateRevision &&
                                latest.playerInputRevision == plan.playerInputRevision &&
                                latest.nextTimelineGlobalSequence == plan.exclusiveGlobalSequence
                        }?.recommendation?.choices.orEmpty(),
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
                            clocktowerNightStepIndexState.value = (clocktowerNightStepIndexState.value - 1).coerceAtLeast(0)
                        },
                        dayModeState = clocktowerDayModeState,
                        ghostVoteAuthority = clocktowerGhostVoteAuthority,
                        highestVoteName = clocktowerSessionView?.voteStanding?.nomineeSeat
                            ?.let { requireClocktowerGameSession().state.gameState.playerAt(it)?.name },
                        highestVoteCount = clocktowerSessionView?.voteStanding?.count ?: 0,
                        gameOutcome = gameOutcome,
                        onCommitConfirmedPrivateInformationPublication = ::commitClocktowerPrivateInformationPublication,
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
                            clocktowerDemonAttackDraftTarget = selected
                        },
                        onConfirmDemonAttack = {
                            val targetName = clocktowerDemonAttackDraftTarget
                            if (targetName != confirmedNightAttackName()) {
                                val session = requireClocktowerGameSession()
                                if (targetName == null) {
                                    session.clearConfirmedNightAttack(session.state.gameStateRevision)
                                    publishClocktowerSessionView()
                                    invalidateA4RevisionScope()
                                } else {
                                    val targetSeat = clocktowerSeatFor(targetName)
                                    commitClocktowerConfirmedAction(ActionFactDraft.Attack(
                                        actionId = clocktowerActionId("attack", targetSeat = targetSeat),
                                        phase = storytellerPhaseFor(), round = round,
                                        sequence = session.nextInputSequence, targetSeat = targetSeat,
                                    ))
                                }
                            }
                        },
                        onSelectExecution = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerSelectedExecution = it
                        },
                        onSelectPoisonTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerPoisonTarget = selected
                        },
                        onConfirmPoisonTarget = {
                            if (clocktowerPoisonTarget != confirmedClocktowerPoisonTarget()) {
                                val session = requireClocktowerGameSession()
                                val targetSeat = clocktowerPoisonTarget?.let(::clocktowerSeatFor)
                                session.commitConfirmedPoisonTransition(
                                    draft = ActionFactDraft.Poison(
                                        actionId = clocktowerActionId("poison", targetSeat = targetSeat),
                                        phase = storytellerPhaseFor(), round = round,
                                        sequence = session.nextInputSequence, targetSeat = targetSeat,
                                    ),
                                    expectedGameStateRevision = session.state.gameStateRevision,
                                )
                                publishClocktowerSessionView()
                                invalidateA4RevisionScope()
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
                            val seat = cards.indexOfFirst { it.name == name }
                                .takeIf { it >= 0 }?.plus(1)
                                ?: error("Cannot confirm Red Herring outside the Session seats.")
                            requireClocktowerGameSession().commitRedHerringSeat(
                                seat = seat,
                                phase = storytellerPhaseFor(), round = round,
                                sequence = requireClocktowerGameSession().nextInputSequence,
                            )
                            publishClocktowerSessionView()
                            invalidateA4RevisionScope()
                            redHerringDraftName = null
                        },
                        onCommitDemonBluffs = { roleNames ->
                            if (roleNames != clocktowerRecommendedDemonBluffRoleNames) {
                                requireClocktowerGameSession().commitDemonBluffs(
                                    roles = roleNames.map(::RoleId),
                                    phase = storytellerPhaseFor(), round = round,
                                    sequence = requireClocktowerGameSession().nextInputSequence,
                                )
                                publishClocktowerSessionView()
                                invalidateA4RevisionScope()
                            }
                        },
                        onSelectButlerMaster = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerButlerMaster = it
                        },
                        onConfirmButlerMaster = { actorName, masterName ->
                            requireClocktowerGameSession().confirmButlerMaster(
                                phase = storytellerPhaseFor(), round = round,
                                butlerSeat = clocktowerSeatFor(actorName),
                                masterSeat = clocktowerSeatFor(masterName),
                            )
                            publishClocktowerSessionView()
                            invalidateA4RevisionScope()
                        },
                        onSelectMonkProtectedTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMonkProtectedTarget = selected
                        },
                        onConfirmMonkProtectedTarget = {
                            val targetName = clocktowerMonkProtectedTarget
                            if (targetName != confirmedNightMonkName()) {
                                val session = requireClocktowerGameSession()
                                if (targetName == null) {
                                    session.clearConfirmedNightProtection(session.state.gameStateRevision)
                                    publishClocktowerSessionView()
                                    invalidateA4RevisionScope()
                                } else {
                                    val targetSeat = clocktowerSeatFor(targetName)
                                    commitClocktowerConfirmedAction(ActionFactDraft.Protect(
                                        actionId = clocktowerActionId("protect", targetSeat = targetSeat),
                                        phase = storytellerPhaseFor(), round = round,
                                        sequence = session.nextInputSequence, targetSeat = targetSeat,
                                    ))
                                }
                            }
                        },
                        onSelectMayorRedirectTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMayorRedirectTarget = selected
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
                                            val request = StorytellerProviderRequestFactoryV1.fromMayorRedirect(
                                                decision = pendingDecision,
                                                snapshot = snapshot,
                                                gameContext = currentClocktowerCausalJournal().contextForRequest(snapshot, session.state),
                                            )
                                            session.commitConfirmedMayorRedirect(
                                                draft = ActionFactDraft.MayorRedirectChoice(
                                                    actionId = clocktowerActionId("mayor-redirect", targetSeat = targetSeat),
                                                    phase = StorytellerPhase.NIGHT,
                                                    round = round,
                                                    sequence = requireClocktowerGameSession().nextInputSequence,
                                                    mayorSeat = pendingDecision.mayorSeat,
                                                    targetSeat = targetSeat,
                                                ),
                                                request = request,
                                                candidateId = candidateId,
                                                expectedGameStateRevision = session.state.gameStateRevision,
                                            )
                                            publishClocktowerSessionView()
                                            invalidateA4RevisionScope()
                                        }
                                    }
                                    is MayorRedirectDecisionConfirmation.Blocked -> {
                                        error("Mayor redirect confirmation blocked: ${confirmation.reason}")
                                    }
                                }
                            }
                        },
                        onSelectDemonSuccessor = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerDemonSuccessorTarget = selected
                        },
                        onConfirmDemonSuccessorTarget = { _ ->
                            val session = requireClocktowerGameSession()
                            val previousRevision = session.state.playerInputRevision
                            session.confirmDemonSuccessor(clocktowerDemonSuccessorTarget?.let(::clocktowerSeatFor))
                            publishClocktowerSessionView()
                            if (session.state.playerInputRevision != previousRevision) invalidateA4RevisionScope()
                        },
                        onConfirmNewDemon = {
                            val pendingName = clocktowerPendingNewDemonName
                            val canEnterDawn =
                                if (pendingName != null) {
                                    val baseGameState = requireClocktowerGameSession().state.gameState
                                    val checkpoint = currentClocktowerNightCheckpoint()
                                    val effectiveNightState = baseGameState.toBaseEffectiveNightState()
                                    val poisoner = baseGameState.players.firstOrNull {
                                        it.alive && it.actualRole == RoleId("Poisoner")
                                    }
                                    val demonRole = requireNotNull(baseGameState.players.firstOrNull {
                                        it.actualType == CharacterType.DEMON
                                    }?.actualRole)
                                    val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
                                        baseGameState = baseGameState,
                                        checkpoint = checkpoint,
                                        demonRoleId = demonRole,
                                        poisonResolutionInput = poisoner?.let { source ->
                                            NightDawnPoisonResolutionInput(
                                                poisonerSeat = source.seat,
                                                poisonerRoleId = source.actualRole,
                                                effectiveNightState = effectiveNightState,
                                            )
                                        },
                                    )
                                    val dawnCommitIntent = transition.dawnCommitIntent
                                    if (transition.continuation == NightResolutionContinuation.DAWN && dawnCommitIntent != null) {
                                        dawnCommitIntent.roleChanges.forEach { roleChange ->
                                            val targetName = cards.getOrNull(roleChange.targetSeat - 1)?.name
                                            val nextRole = clocktowerRolesForScript(currentClocktowerScript)
                                                .firstOrNull { role -> RoleId(role.enName) == roleChange.roleId }
                                            check(targetName != null && nextRole != null) {
                                                "Confirmed Dawn role change target/role is missing from Host roster."
                                            }
                                            commitClocktowerActualRole(
                                                playerName = targetName,
                                                nextRole = nextRole,
                                                actionId = DawnCommitIdentity.roleChange(
                                                    clocktowerGameId, round, roleChange.targetSeat, roleChange.roleId,
                                                ),
                                            )
                                        }
                                        dawnCommitIntent.poisonCarry?.let(::commitClocktowerDawnPoison)
                                        clocktowerPendingNewDemonName = transition.checkpoint.pendingNewDemonName
                                        clocktowerPendingNightNewDemonIdentityName = transition.checkpoint.pendingNightNewDemonIdentityName
                                        clocktowerDemonSuccessorTarget = transition.checkpoint.demonSuccessorDraftTarget
                                        if (transition.checkpoint.confirmedDemonSuccessorTarget == null) clearConfirmedDemonSuccessorTarget()
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
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Dawn)
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
                                    round, requireClocktowerGameSession().nextInputSequence,
                                )?.let(::commitClocktowerConfirmedAction)
                                if (choiceResult.evilWins) {
                                    gameOutcome = GameOutcome(
                                        title = context.getString(R.string.outcome_clocktower_evil_title),
                                        summary = localizedText("呆瓜选择了邪恶玩家，善良阵营失败。", "The Klutz chose an evil player, so the good team loses."),
                                        reason = localizedText("${playerSeatLabel(cards, clocktowerPendingKlutzName)} 选择了 ${playerSeatLabel(cards, choice)}。", "${playerSeatLabel(cards, clocktowerPendingKlutzName)} chose ${playerSeatLabel(cards, choice)}."),
                                    )
                                    showResults = true
                                } else {
                                    clocktowerPendingKlutzName = null
                                    clocktowerKlutzChoiceName = null
                                    if (clocktowerKlutzReturnToDawn) {
                                        commitClocktowerPhaseAdvance(ClocktowerPhase.Dawn)
                                        clocktowerKlutzReturnToDawn = false
                                    } else {
                                        val nextRound = round + 1
                                        materializeClocktowerPoisonExpiryAtDusk()
                                        commitClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
                                    }
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                }
                            }
                        },
                        onConfirmArtistQuestion = { claimantName ->
                            if (claimantName !in clocktowerArtistClaimedNames) {
                                clocktowerArtistClaimedNames = clocktowerArtistClaimedNames + claimantName
                            }
                            val claimantCard = cards.firstOrNull { it.name == claimantName }
                            if (claimantCard?.clocktowerRole?.enName == "Artist" && !clocktowerArtistUsed) {
                                clocktowerArtistUsed = true
                            }
                            // The confirmed answer consumes the Artist's day decision, not its log text.
                            advanceClocktowerGameStateRevision()
                            clocktowerDayModeState.value = ClocktowerDayMode.Overview
                        },
                        onSlayerShot = { claimantName, targetName, recluseRegistersAsDemon ->
                            val claimantCard = cards.firstOrNull { it.name == claimantName }
                            val targetIndex = cards.indexOfFirst { it.name == targetName }
                            val targetCard = cards.getOrNull(targetIndex)
                            val slayerDecision = AbilityFunctioningSemantics.oneShotDecision(
                                subject = claimantCard?.abilitySubject(confirmedClocktowerPoisonTarget()),
                                role = "Slayer",
                                alreadyUsed = clocktowerSlayerUsed,
                            )
                            val targetRegistersAsDemon = targetCard?.clocktowerTeam == ClocktowerTeam.Demon ||
                                (targetCard?.clocktowerRole?.enName == "Recluse" &&
                                    targetCard.name != confirmedClocktowerPoisonTarget() &&
                                    recluseRegistersAsDemon == true)
                            val shotWillHit = slayerDecision.effectApplies &&
                                targetIndex >= 0 && targetCard != null &&
                                targetCard.eliminatedRound == null && targetRegistersAsDemon
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
                                claimantCard != null && targetCard != null
                            ) {
                                // Player's confirmed public shot precedes the optional Storyteller
                                // registration ruling; misses and non-functioning claims remain facts.
                                commitClocktowerConfirmedAction(ActionFactDraft.SlayerShot(
                                    actionId = clocktowerActionId("slayer-shot", targetSeat = targetIndex + 1),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = requireClocktowerGameSession().nextInputSequence,
                                    claimantSeat = clocktowerSeatFor(claimantName),
                                    targetSeat = targetIndex + 1,
                                    abilityConsumed = slayerDecision.consumesUse,
                                    hit = shotWillHit,
                                ))
                            }
                            if (
                                currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
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
                                    input = ConfirmedDaySlayerRegistrationV1(
                                        interactionId = "day:$round:slayer:$actorSeat:$subjectSeat",
                                        slayerSeat = actorSeat,
                                        recluseSeat = subjectSeat,
                                        registeredDemonRole = if (recluseRegistersAsDemon) RoleId("Imp") else null,
                                    ),
                                )
                            }
                            var shotOutcome: GameOutcome? = null
                            if (shotWillHit && targetIndex >= 0 && targetCard != null) {
                                val targetSeat = targetIndex + 1
                                val localSequence = requireClocktowerGameSession().nextInputSequence
                                preflightClocktowerPublicAliveObservation(
                                    playerName = targetName,
                                    eventSequence = localSequence,
                                )
                                requireClocktowerGameSession().commitConfirmedPlayerDeath(
                                    draft = ActionFactDraft.Death(
                                        actionId = clocktowerActionId(
                                            kind = "slayer-death",
                                            localSequence = localSequence,
                                            targetSeat = targetSeat,
                                        ),
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = localSequence,
                                        targetSeat = targetSeat,
                                    ),
                                    expectedGameStateRevision = requireClocktowerGameSession().state.gameStateRevision,
                                    publishedObservationId = "public-alive-${clocktowerGameId}-${localSequence}-$targetSeat",
                                )
                                publishClocktowerSessionView()
                                invalidateA4RevisionScope()
                                pendingA4ObservationRecordId = "public-alive-${clocktowerGameId}-${localSequence}-$targetSeat"
                                val promotedName = if (targetCard.clocktowerTeam == ClocktowerTeam.Demon) {
                                    promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
                                } else {
                                    null
                                }
                                if (promotedName != null) {
                                    clocktowerPendingNightNewDemonIdentityName = promotedName
                                }
                                shotOutcome = if (targetCard.clocktowerTeam != ClocktowerTeam.Demon || promotedName != null) null
                                else evaluateGameOutcome(context, cards)
                            }
                            gameOutcome = shotOutcome
                            if (shotOutcome != null) {
                                showResults = true
                            }
                        },
                        onPreflightVirginExecution = { nominatorName ->
                            val preflightIndex = cards.indexOfFirst { it.name == nominatorName }
                            val preflightCard = cards.getOrNull(preflightIndex)
                            if (preflightIndex >= 0 && preflightCard != null && preflightCard.eliminatedRound == null) {
                                preflightClocktowerPublicAliveObservation(
                                    playerName = nominatorName,
                                    // The nomination is committed before the Virgin execution.
                                    eventSequence = Math.addExact(requireClocktowerGameSession().nextInputSequence, 1),
                                )
                            }
                        },
                        onConfirmedNomination = { nominatorName, nomineeName ->
                            if (currentClocktowerScript in setOf(
                                    ClocktowerScript.TroubleBrewing, ClocktowerScript.NoGreaterJoy,
                                )
                            ) {
                                // Public nomination belongs to both supported scripts. The hidden
                                // first-Virgin marker is only meaningful in real Trouble Brewing.
                                val nominatedCard = cards.firstOrNull { it.name == nomineeName }
                                val firstVirginNomination = nominatedCard?.let {
                                    AbilityFunctioningSemantics.interactsAs(
                                        it.abilitySubject(confirmedClocktowerPoisonTarget()), "Virgin",
                                    )
                                } == true && !clocktowerVirginUsed &&
                                    currentClocktowerScript == ClocktowerScript.TroubleBrewing
                                commitClocktowerConfirmedAction(ActionFactDraft.Nomination(
                                    actionId = clocktowerActionId("nomination", targetSeat = clocktowerSeatFor(nomineeName)),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = requireClocktowerGameSession().nextInputSequence,
                                    nominatorSeat = clocktowerSeatFor(nominatorName),
                                    nomineeSeat = clocktowerSeatFor(nomineeName),
                                    firstVirginNomination = firstVirginNomination,
                                ))
                            }
                        },
                        onConfirmedVote = { nominatorName, nomineeName, voteRecord ->
                            if (currentClocktowerScript in setOf(
                                    ClocktowerScript.TroubleBrewing, ClocktowerScript.NoGreaterJoy,
                                )
                            ) {
                                val voterSeats = voteRecord.voters.map { clocktowerSeatFor(it.playerName) }
                                commitClocktowerConfirmedAction(ActionFactDraft.Vote(
                                    actionId = clocktowerActionId("vote", targetSeat = clocktowerSeatFor(nomineeName)),
                                    phase = storytellerPhaseFor(),
                                    round = round,
                                    sequence = requireClocktowerGameSession().nextInputSequence,
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
                                currentClocktowerScript == ClocktowerScript.TroubleBrewing
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
                            if (executeNominator) {
                                val index = cards.indexOfFirst { it.name == nominatorName }
                                val nominatorCard = cards.getOrNull(index)
                                if (index >= 0 && nominatorCard != null && nominatorCard.eliminatedRound == null) {
                                    val actionSequence = requireClocktowerGameSession().nextInputSequence
                                    requireClocktowerGameSession().commitConfirmedPlayerDeath(
                                        draft = ActionFactDraft.Execution(
                                            actionId = clocktowerActionId(
                                                kind = "virgin-execution",
                                                localSequence = actionSequence,
                                                targetSeat = index + 1,
                                            ),
                                            phase = storytellerPhaseFor(),
                                            round = round,
                                            sequence = actionSequence,
                                            targetSeat = index + 1,
                                        ),
                                        expectedGameStateRevision = requireClocktowerGameSession().state.gameStateRevision,
                                        publishedObservationId = "public-alive-${clocktowerGameId}-$actionSequence-${index + 1}",
                                    )
                                    publishClocktowerSessionView()
                                    invalidateA4RevisionScope()
                                    pendingA4ObservationRecordId = "public-alive-${clocktowerGameId}-$actionSequence-${index + 1}"
                                }
                                clocktowerLastExecutedName = nominatorName
                                val outcome = evaluateGameOutcome(context, cards)
                                gameOutcome = outcome
                                if (outcome != null) {
                                    showResults = true
                                } else {
                                    val nextRound = round + 1
                                    materializeClocktowerPoisonExpiryAtDusk()
                                    commitClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                }
                                clocktowerSelectedExecution = null
                            }
                        },
                        onAdvanceFromFirstNight = {
                            commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                            requireClocktowerGameSession().clearResolvedNightDeath()
                            publishClocktowerSessionView()
                            clocktowerDemonAttackDraftTarget = null
                            resetClocktowerNightFlow()
                            resetClocktowerDayFlow()
                        },
                        onConfirmDay = onConfirmDay@{
                            val preflightExecutionName = clocktowerSelectedExecution
                            val preflightIndex = preflightExecutionName
                                ?.let { selectedName -> cards.indexOfFirst { it.name == selectedName } }
                                ?: -1
                            val preflightCard = cards.getOrNull(preflightIndex)
                            // A stale or dead selection is not a confirmed execution. Drop the
                            // draft and remain in the day instead of advancing Session's revision
                            // without a corresponding accepted action fact.
                            if (preflightExecutionName != null && (
                                    preflightCard == null ||
                                        preflightCard.eliminatedRound != null ||
                                        requireClocktowerGameSession().state.gameState
                                            .playerAt(preflightIndex + 1)?.alive != true
                                )
                            ) {
                                clocktowerSelectedExecution = null
                                advanceClocktowerPlayerInputRevision()
                                return@onConfirmDay
                            }
                            val proposedSequence = requireClocktowerGameSession().nextInputSequence
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
                                "gameStateRevision" to clocktowerGameStateRevision.toString(),
                                "playerInputRevision" to clocktowerPlayerInputRevision.toString(),
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
                            // Execution and NoExecution own their revision with the accepted fact.
                            val aliveBeforeExecution = cards.filter { it.eliminatedRound == null }
                            val executionName = clocktowerSelectedExecution
                            var executionOutcome: GameOutcome? = null
                            if (executionName != null) {
                                val index = cards.indexOfFirst { it.name == executionName }
                                val executedCard = cards.getOrNull(index)
                                if (index >= 0 && executedCard != null && executedCard.eliminatedRound == null) {
                                    // Session binds death-time Klutz evidence before clearing poison.
                                    val actionSequence = requireClocktowerGameSession().nextInputSequence
                                    requireClocktowerGameSession().commitConfirmedPlayerDeath(
                                        draft = ActionFactDraft.Execution(
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
                                        ),
                                        expectedGameStateRevision = requireClocktowerGameSession().state.gameStateRevision,
                                        publishedObservationId = "public-alive-${clocktowerGameId}-$actionSequence-${index + 1}",
                                    )
                                    publishClocktowerSessionView()
                                    invalidateA4RevisionScope()
                                    pendingA4ObservationRecordId = "public-alive-${clocktowerGameId}-$actionSequence-${index + 1}"
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

                                        clocktowerDayModeState.value = ClocktowerDayMode.Klutz
                                        captureKlutzLearnedDeathOnPublicAnnouncement(executionName)
                                        executionOutcome = null
                                    } else if (executedCard.clocktowerTeam == ClocktowerTeam.Demon) {
                                        val promotedName = promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
                                        if (promotedName != null) {
                                            clocktowerPendingNightNewDemonIdentityName = promotedName
                                        }
                                        executionOutcome = if (promotedName == null) {
                                            evaluateGameOutcome(context, cards)
                                        } else {
                                            null
                                        }
                                    } else {
                                        executionOutcome = evaluateGameOutcome(context, cards)
                                    }
                                }
                            } else {
                                clocktowerLastExecutedName = null
                                if (currentClocktowerScript in setOf(
                                        ClocktowerScript.TroubleBrewing, ClocktowerScript.NoGreaterJoy,
                                    )
                                ) {
                                    // A confirmed no-execution is positive public chronology,
                                    // NOT absence of a death fact and NOT a Storyteller recommendation.
                                    // Commit before possible Mayor win and day/phase changes.
                                    commitClocktowerConfirmedAction(ActionFactDraft.NoExecution(
                                        actionId = clocktowerActionId("no-execution"),
                                        phase = storytellerPhaseFor(),
                                        round = round,
                                        sequence = requireClocktowerGameSession().nextInputSequence,
                                    ))
                                } else {
                                    advanceClocktowerGameStateRevision()
                                }
                            }
                            if (executionName == null && aliveBeforeExecution.size == 3 && aliveBeforeExecution.any {
                                    AbilityFunctioningSemantics.functionsAs(it.abilitySubject(confirmedClocktowerPoisonTarget()), "Mayor")
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
                            } else if (clocktowerPendingKlutzName == null) {
                                val nextRound = round + 1
                                materializeClocktowerPoisonExpiryAtDusk()
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
                                resetClocktowerDayFlow()
                                resetClocktowerNightFlow()
                            }
                            clocktowerSelectedExecution = null
                        },
                        onConfirmNight = {
                            // Actual confirmed deaths, role changes, poison carry and phase progress
                            // each own their Session transition. A night-close without any newly
                            // accepted typed action still needs one revision invalidation.
                            val nightCloseStartingRevision = requireClocktowerGameSession().state.gameStateRevision
                            clocktowerPendingNightNewDemonIdentityName = null
                            var nightKlutzName: String? = null
                            var newDemonName: String? = null
                            var unresolvedDemonSuccessor = false
                            val originalDeathName = confirmedNightAttackName()
                            val dawnDeathFacts = resolveTroubleBrewingDawnDeathFacts(
                                cards = cards,
                                targetName = originalDeathName,
                                poisonedPlayerName = confirmedClocktowerPoisonTarget(),
                                monkProtectedTargetName = confirmedNightMonkName(),
                            )
                            val mayorCanRedirect = dawnDeathFacts.mayorSeat != null
                            val baseGameState = requireClocktowerGameSession().state.gameState
                            val effectiveNightState = baseGameState.toBaseEffectiveNightState()
                            val demonRoleIds = baseGameState.players
                                .filter { it.actualType == CharacterType.DEMON }
                                .mapTo(mutableSetOf()) { it.actualRole }
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
                            if (deathName != null) {
                                val index = cards.indexOfFirst { it.name == deathName }
                                val nightDeathCard = cards.getOrNull(index)
                                if (index >= 0 && nightDeathCard != null) {
                                    val demonDied = nightDeathCard.clocktowerTeam == ClocktowerTeam.Demon
                                    val impSelfChosen = demonDied && originalDeathName == deathName
                                    val session = requireClocktowerGameSession()
                                    val targetSeat = index + 1
                                    val actionId = DawnCommitIdentity.death(clocktowerGameId, round, targetSeat)
                                    val observationId = DawnCommitIdentity.deathObservation(clocktowerGameId, round, targetSeat)
                                    val previous = session.state.actionTimeline.entries.firstOrNull { it.fact.actionId == actionId }
                                    val beforeRevision = session.state.gameStateRevision
                                    session.commitConfirmedPlayerDeath(
                                        draft = ActionFactDraft.Death(
                                            actionId = actionId,
                                            phase = storytellerPhaseFor(),
                                            round = round,
                                            sequence = previous?.point?.sequence ?: session.nextInputSequence,
                                            targetSeat = targetSeat,
                                        ),
                                        expectedGameStateRevision = beforeRevision,
                                        publishedObservationId = observationId,
                                    )
                                    if (session.state.gameStateRevision != beforeRevision) {
                                        publishClocktowerSessionView()
                                        invalidateA4RevisionScope()
                                        pendingA4ObservationRecordId = observationId
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
                                            if (successionTransition.checkpoint.confirmedDemonSuccessorTarget == null) clearConfirmedDemonSuccessorTarget()
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
                                } else {
                                    requireClocktowerGameSession().clearResolvedNightDeath()
                                    publishClocktowerSessionView()
                                }
                            } else {
                                // The validated night resolution owns the outcome, including no death.
                                requireClocktowerGameSession().clearResolvedNightDeath()
                                publishClocktowerSessionView()
                            }
                            val postDeathGameState = requireClocktowerGameSession().state.gameState
                            val dawnPoisoner = postDeathGameState.players.firstOrNull {
                                it.actualRole == RoleId("Poisoner")
                            }
                            val postDeathEffectiveNightState = postDeathGameState.toBaseEffectiveNightState()

                            val ordinaryDawnPoisonIntent = dawnPoisoner?.let { source ->
                                NightDawnResolutionPlanner.planPoisonCarry(
                                    baseGameState = baseGameState,
                                    checkpoint = currentClocktowerNightCheckpoint(),
                                    input = NightDawnPoisonResolutionInput(
                                        poisonerSeat = source.seat,
                                        poisonerRoleId = source.actualRole,
                                        effectiveNightState = postDeathEffectiveNightState,
                                    ),
                                )
                            }

                            ordinaryDawnPoisonIntent?.let(::commitClocktowerDawnPoison)

                            if (nightKlutzName != null) {
                                clocktowerPendingKlutzName = nightKlutzName
                                clocktowerKlutzChoiceName = null
                                clocktowerKlutzReturnToDawn = true
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                                clocktowerDayModeState.value = ClocktowerDayMode.Klutz
                                captureKlutzLearnedDeathOnPublicAnnouncement(nightKlutzName)
                            }
                            val nightOutcome =
                                if (
                                    nightKlutzName == null &&
                                    newDemonName == null &&
                                    !unresolvedDemonSuccessor
                                ) {
                                    evaluateGameOutcome(context, cards)
                                } else {
                                    null
                                }
                            gameOutcome = nightOutcome
                            if (nightOutcome != null) {
                                showResults = true
                            } else if (nightKlutzName == null && newDemonName != null) {
                                clocktowerPendingNewDemonName = newDemonName
                            } else if (nightKlutzName == null && !unresolvedDemonSuccessor) {
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Dawn)
                                resetClocktowerNightFlow()
                            }
                            clocktowerFortuneTellerFirst = null
                            clocktowerFortuneTellerSecond = null
                            clocktowerChambermaidFirst = null
                            clocktowerChambermaidSecond = null
                            clocktowerRavenkeeperTarget = null
                            clocktowerMonkProtectedTarget = null
                            requireClocktowerGameSession().finishNightResolution()
                            publishClocktowerSessionView()
                            clocktowerMayorRedirectTarget = null
                            if (clocktowerPendingNewDemonName == null) {
                                clocktowerDemonSuccessorTarget = null
                                clearConfirmedDemonSuccessorTarget()
                            }
                            if (requireClocktowerGameSession().state.gameStateRevision == nightCloseStartingRevision) {
                                // No death, poison, role or phase fact was committed. Preserve
                                // invalidation for a genuinely accepted no-change night close.
                                advanceClocktowerGameStateRevision()
                            }
                        },
                    )

                        }
                    }
                }

                if (showResults) {
                    ClocktowerResultsDialog(
                        cards = cards,
                        outcome = gameOutcome,
                        onDismiss = { showResults = false },
                        onNewGame = { showNewGameConfirmation = true },
                    )
                }

                if (showHostTools) {
                    HostGameToolsScreen(
                        cards = cards,
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
                        onDismiss = { showNewGameConfirmation = false },
                        onManagePlayers = ::returnToPlayerManagement,
                        onQuickRestart = ::restartGame,
                    )
                }
            }
        }
    }
}

private fun evaluateGameOutcome(context: Context, cards: List<PlayerCard>): GameOutcome? {
    val activeCards = cards.filter { it.eliminatedRound == null }
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

internal fun PlayerCard.hostRoleLabel(context: Context): String =
    clocktowerHostRoleLabel(context.resources.configuration.locales[0].language)
        ?: "?"

internal fun PlayerCard.seatLabel(cards: List<PlayerCard>): String =
    "#${cards.indexOfFirst { it.name == name } + 1} $name"

internal fun playerSeatLabel(cards: List<PlayerCard>, playerName: String?): String =
    cards.firstOrNull { it.name == playerName }?.seatLabel(cards) ?: playerName.orEmpty()
