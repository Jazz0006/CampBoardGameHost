package com.codex.campboardgamehost

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationLedger
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerRecommendationUxPolicy
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionKind
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.recommendation.GameBalanceEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCandidate
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionPoolParityRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolDeviceBenchmark
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolDeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.PairInformationRegistration
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.RegistrationDetail
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SpecialRegistrationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
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
import com.codex.campboardgamehost.clocktower.session.DynamicResolutionRequest
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationLock
import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationPrewarmCoordinator
import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationRevealCoordinator
import com.codex.campboardgamehost.clocktower.session.TroubleBrewingFirstNightPrecomputeCoordinator
import com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmark
import com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationCandidate
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationRequest
import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingCommittedSetupAdapter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
import com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkCase
import com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkHarness
import com.codex.campboardgamehost.clocktower.epistemic.A4DeviceBenchmarkReport
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
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.epistemic.ZddFilterStrategy
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.RegistrationInteractionRules
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import java.util.Locale
import java.util.UUID

private enum class Screen {
    Landing,
    Setup,
    GameSelection,
    UndercoverSettings,
    WerewolfSettings,
    ClocktowerSettings,
    Settings,
    PassPhone,
    RevealCard,
    WerewolfJudge,
    ClocktowerJudge,
    Game,
}

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
private const val COMMON_PLAYERS_KEY = "common_players"
private const val LANGUAGE_MODE_KEY = "language_mode"
private const val AUTOMATIC_STORYTELLER_INFO_KEY = "automatic_storyteller_info"
private const val STORYTELLER_AUTOMATION_MODE_KEY = "storyteller_automation_mode"
private const val ACTIVE_GAME_STATE_KEY = "active_game_state"
private const val GAME_HISTORY_KEY = "game_history"
internal const val A4_IDENTITY_PREWARM_LOG_TAG = "A4IdentityPrewarm"
internal const val A4_OBSERVATION_CACHE_UPDATE_LOG_TAG = "A4ObservationCacheUpdate"
internal const val UNIFIED_SETUP_SELECTOR_BENCHMARK_LOG_TAG = "UnifiedSetupSelectorBenchmark"
internal const val UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG = "UnifiedFirstNightPoolBenchmark"
private const val MAX_GAME_HISTORY = 20
internal const val MIN_PLAYERS = 3
internal const val MIN_WEREWOLF_PLAYERS = 4
internal const val MIN_CLOCKTOWER_PLAYERS = 5
internal const val MAX_PLAYERS = 15

private fun Context.localized(languageMode: LanguageMode): Context {
    if (languageMode == LanguageMode.System) return this
    val locale = Locale(languageMode.prefsValue)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

private fun Context.loadLanguageMode(): LanguageMode {
    val value = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(LANGUAGE_MODE_KEY, LanguageMode.System.prefsValue)
    return LanguageMode.entries.firstOrNull { it.prefsValue == value } ?: LanguageMode.System
}

private fun Context.saveLanguageMode(languageMode: LanguageMode) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(LANGUAGE_MODE_KEY, languageMode.prefsValue)
        .apply()
}

private fun Context.loadStorytellerAutomationMode(): StorytellerAutomationMode {
    val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val stored = preferences.getString(STORYTELLER_AUTOMATION_MODE_KEY, null)
    return StorytellerAutomationMode.entries.firstOrNull { it.prefsValue == stored }
        ?: if (preferences.getBoolean(AUTOMATIC_STORYTELLER_INFO_KEY, false)) {
            StorytellerAutomationMode.AUTO_BALANCED
        } else {
            StorytellerAutomationMode.MANUAL
        }
}

private fun Context.saveStorytellerAutomationMode(mode: StorytellerAutomationMode) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(STORYTELLER_AUTOMATION_MODE_KEY, mode.prefsValue)
        .remove(AUTOMATIC_STORYTELLER_INFO_KEY)
        .apply()
}

private fun Context.loadCommonPlayers(): List<String> {
    val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val hasStoredPlayers = preferences.contains(COMMON_PLAYERS_KEY)
    val raw = preferences.getString(COMMON_PLAYERS_KEY, null)
    val storedPlayers = raw?.let { encoded ->
        runCatching {
            val json = JSONArray(encoded)
            List(json.length()) { index -> json.getString(index) }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        }.getOrElse { emptyList() }
    } ?: emptyList()
    return resolveInitialCommonPlayers(
        hasStoredPlayers = hasStoredPlayers,
        storedPlayers = storedPlayers,
    )
}

private fun Context.saveCommonPlayers(players: List<String>) {
    val json = JSONArray()
    players.map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .forEach { json.put(it) }
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(COMMON_PLAYERS_KEY, json.toString())
        .apply()
}

private fun Screen.isActiveGameScreen(): Boolean = when (this) {
    Screen.PassPhone,
    Screen.RevealCard,
    Screen.WerewolfJudge,
    Screen.ClocktowerJudge,
    Screen.Game -> true
    Screen.Landing,
    Screen.Setup,
    Screen.GameSelection,
    Screen.UndercoverSettings,
    Screen.WerewolfSettings,
    Screen.ClocktowerSettings,
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

private fun clocktowerRoleByName(enName: String?): ClocktowerRole? {
    if (enName.isNullOrBlank()) return null
    return completeClocktowerRoles.firstOrNull { it.enName == enName }
}

private fun archivedGameReviewFromJson(entry: JSONObject): ArchivedGameReview? =
    GameArchiveJsonCodec.decodeEntry(entry, ::clocktowerRoleByName)

private fun Context.loadGameHistory(): List<ArchivedGameReview> {
    val raw = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(GAME_HISTORY_KEY, null)
        ?: return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.let { archivedGameReviewFromJson(it)?.let(::add) }
            }
        }
    }.getOrDefault(emptyList())
}

private fun List<ArchivedGameReview>.toClocktowerSetupHistory(): CrossGameHistory = CrossGameHistory(
    asSequence()
        .filter { it.gameKind == GameKind.Clocktower }
        .mapNotNull { review ->
            review.cards
                .firstOrNull { it.clocktowerRole?.enName == "Drunk" }
                ?.clocktowerShownRole
                ?.let { shownRole ->
                    HistoricalClueSignature(
                        decisionType = "setup-plan",
                        drunkShownRole = RoleId(shownRole.enName),
                    )
                }
        }
        .take(CrossGameHistory.MAX_SAVED_GAMES)
        .toList(),
)

private fun Context.archiveGame(record: GameArchiveRecord): List<ArchivedGameReview> {
    if (record.cards.isEmpty()) return loadGameHistory()
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = runCatching { JSONArray(prefs.getString(GAME_HISTORY_KEY, "[]")) }.getOrDefault(JSONArray())
    val archivedAt = System.currentTimeMillis()
    val next = JSONArray().apply {
        put(
            GameArchiveJsonCodec.encodeEntry(
                record = record,
                id = archivedAt,
                archivedAtMillis = archivedAt,
            ),
        )
        for (index in 0 until minOf(existing.length(), MAX_GAME_HISTORY - 1)) {
            existing.optJSONObject(index)?.let(::put)
        }
    }
    prefs.edit().putString(GAME_HISTORY_KEY, next.toString()).commit()
    return loadGameHistory()
}

private fun playerCardFromJson(json: JSONObject): PlayerCard? {
    val name = json.optString("name").takeIf { it.isNotBlank() } ?: return null
    val role = enumByName<Role>(json.optNullableString("role")) ?: return null
    val clocktowerRole = clocktowerRoleByName(json.optNullableString("clocktowerRole"))
    val clocktowerShownRole = clocktowerRoleByName(json.optNullableString("clocktowerShownRole"))
    val clocktowerTeam = enumByName<ClocktowerTeam>(json.optNullableString("clocktowerTeam"))
        ?: clocktowerRole?.team
    return PlayerCard(
        name = name,
        role = role,
        word = json.optString("word"),
        roleLabel = json.optNullableString("roleLabel"),
        actualRoleLabel = json.optNullableString("actualRoleLabel"),
        clocktowerTeam = clocktowerTeam,
        clocktowerRole = clocktowerRole,
        clocktowerShownRole = clocktowerShownRole,
        eliminatedRound = json.optNullableInt("eliminatedRound"),
    )
}

private fun JSONArray.toPlayerCards(): List<PlayerCard> = buildList {
    for (index in 0 until length()) {
        optJSONObject(index)?.let { playerCardFromJson(it)?.let(::add) }
    }
}

private fun eliminationRecordFromJson(json: JSONObject): EliminationRecord? {
    val playerName = json.optString("playerName").takeIf { it.isNotBlank() } ?: return null
    return EliminationRecord(
        round = json.optInt("round", 1),
        playerName = playerName,
        note = json.optNullableString("note"),
    )
}

private fun JSONArray.toEliminationRecords(): List<EliminationRecord> = buildList {
    for (index in 0 until length()) {
        optJSONObject(index)?.let { eliminationRecordFromJson(it)?.let(::add) }
    }
}

private fun clocktowerEventFromJson(json: JSONObject): ClocktowerEvent? {
    val title = json.optString("title").takeIf { it.isNotBlank() } ?: return null
    return ClocktowerEvent(
        sequence = json.optInt("sequence", 0),
        type = enumByName<ClocktowerEventType>(json.optNullableString("type")) ?: ClocktowerEventType.System,
        title = title,
        detail = json.optString("detail"),
        playerNames = json.optJSONArray("playerNames")?.toStringList().orEmpty(),
        phase = enumByName<ClocktowerPhase>(json.optNullableString("phase")) ?: ClocktowerPhase.FirstNight,
        round = json.optInt("round", 1).coerceAtLeast(1),
    )
}

private fun JSONArray.toClocktowerEvents(): List<ClocktowerEvent> = buildList {
    for (index in 0 until length()) {
        optJSONObject(index)?.let { clocktowerEventFromJson(it)?.let(::add) }
    }
}

private fun JSONArray.toRecordedEpistemicObservations(): List<RecordedEpistemicObservation> = buildList {
    for (index in 0 until length()) {
        val json = optJSONObject(index)
            ?: throw IllegalArgumentException("Epistemic observation at index $index is not a JSON object.")
        val record = try {
            EpistemicSemanticJson.decodeRecordedEpistemicObservation(json.toString())
        } catch (error: Exception) {
            throw IllegalArgumentException("Cannot restore epistemic observation at index $index.", error)
        }
        add(record)
    }
}

private fun gameOutcomeFromJson(json: JSONObject?): GameOutcome? {
    if (json == null) return null
    val title = json.optString("title").takeIf { it.isNotBlank() } ?: return null
    return GameOutcome(
        title = title,
        summary = json.optString("summary"),
        reason = json.optString("reason"),
    )
}



internal fun Role.labelResId(): Int = when (this) {
    Role.Civilian -> R.string.role_civilian
    Role.Undercover -> R.string.role_undercover
    Role.Blank -> R.string.role_blank
    Role.Villager -> R.string.role_villager
    Role.Werewolf -> R.string.role_werewolf
    Role.Seer -> R.string.role_seer
    Role.Witch -> R.string.role_witch
    Role.Hunter -> R.string.role_hunter
}

internal fun LanguageMode.labelResId(): Int = when (this) {
    LanguageMode.System -> R.string.language_system
    LanguageMode.Chinese -> R.string.language_chinese
    LanguageMode.English -> R.string.language_english
}

private val troubleBrewingRoles = listOf(
    ClocktowerRole(ClocktowerTeam.Townsfolk, "洗衣妇", "Washerwoman", "得知某个镇民在两名玩家之一中。", "Learn that one of two players is a particular Townsfolk."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "图书管理员", "Librarian", "得知某个外来者在两名玩家之一中，或得知没有外来者。", "Learn that one of two players is a particular Outsider, or that there are no Outsiders."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "调查员", "Investigator", "得知某个爪牙在两名玩家之一中，或得知没有爪牙。", "Learn that one of two players is a particular Minion, or that there are no Minions."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "厨师", "Chef", "得知有多少对邪恶玩家相邻而坐。", "Learn how many pairs of evil players are sitting next to each other."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "共情者", "Empath", "每晚得知相邻存活玩家中有几名邪恶玩家。", "Each night, learn how many living neighbors are evil."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "占卜师", "Fortune Teller", "每晚选择两名玩家，得知其中是否有恶魔。", "Each night, choose two players and learn if either is the Demon."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "守鸦人", "Ravenkeeper", "若在夜晚死亡，选择一名玩家并得知其角色。", "If you die at night, choose a player and learn their character."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "士兵", "Soldier", "你不会因恶魔而死亡。", "You are safe from the Demon."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "市长", "Mayor", "若只剩三名存活玩家且白天无人被处决，你的阵营获胜。若你将在夜晚死亡，另一名玩家可能代替你死亡。", "If only three players live and no execution occurs, your team wins. If you would die at night, another player might die instead."),
    ClocktowerRole(ClocktowerTeam.Outsider, "管家", "Butler", "每天选择一名主人，白天只能在主人投票时投票。", "Each day, choose a master. You may only vote if your master votes."),
    ClocktowerRole(ClocktowerTeam.Outsider, "酒鬼", "Drunk", "你以为自己是镇民，但其实能力失效。", "You think you are a Townsfolk, but your ability is not working."),
    ClocktowerRole(ClocktowerTeam.Outsider, "隐士", "Recluse", "你可能被登记为邪恶、爪牙或恶魔，即使死亡后也是。", "You might register as evil and as a Minion or Demon, even if dead."),
    ClocktowerRole(ClocktowerTeam.Outsider, "圣徒", "Saint", "若你被处决，你的阵营失败。", "If you are executed, your team loses."),
    ClocktowerRole(ClocktowerTeam.Minion, "投毒者", "Poisoner", "每晚选择一名玩家，使其能力暂时失效。", "Each night, choose a player. Their ability temporarily stops working."),
    ClocktowerRole(ClocktowerTeam.Minion, "间谍", "Spy", "每晚查看说书人的魔典；你可能被登记为善良、镇民或外来者，即使死亡后也是。", "Each night, view the Storyteller grimoire. You might register as good and as a Townsfolk or Outsider, even if dead."),
    ClocktowerRole(ClocktowerTeam.Minion, "男爵", "Baron", "本局加入额外外来者。", "Extra Outsiders are in play."),
    ClocktowerRole(ClocktowerTeam.Minion, "猩红女巫", "Scarlet Woman", "若恶魔在五人以上时死亡，你可能变成恶魔。", "If the Demon dies with five or more players alive, you may become the Demon."),
    ClocktowerRole(ClocktowerTeam.Demon, "小恶魔", "Imp", "每晚选择一名玩家死亡；可选择自己并传递恶魔身份。", "Each night, choose a player to die. You may choose yourself to pass on the Demon role."),
)

internal val completeTroubleBrewingRoles = (troubleBrewingRoles + listOf(
    ClocktowerRole(ClocktowerTeam.Townsfolk, "送葬者", "Undertaker", "每个夜晚，得知今天被处决玩家的角色。", "Each night, learn which character died by execution today."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "僧侣", "Monk", "每个夜晚，选择除自己以外的一名玩家，使其免受恶魔伤害。", "Each night, choose a player other than yourself: they are safe from the Demon tonight."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "圣女", "Virgin", "首次被镇民提名时，提名者立即被处决。", "The first time you are nominated by a Townsfolk, the nominator is executed immediately."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "杀手", "Slayer", "每局一次，白天选择一名玩家；若其是恶魔，该玩家死亡。", "Once per game during the day, choose a player: if they are the Demon, that player dies."),
)).distinctBy { it.enName }

private val noGreaterJoyExtraRoles = listOf(
    ClocktowerRole(ClocktowerTeam.Townsfolk, "钟表匠", "Clockmaker", "第一夜得知恶魔到最近爪牙相隔几步。", "On the first night, learn how many steps from the Demon to their nearest Minion."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "侍女", "Chambermaid", "每晚选择两名存活玩家，得知其中有几人当晚因自己的能力醒来。", "Each night, choose two alive players and learn how many woke tonight due to their ability."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "艺术家", "Artist", "每局一次，白天私下向说书人询问一个是非问题。", "Once per game during the day, privately ask the Storyteller a yes/no question."),
    ClocktowerRole(ClocktowerTeam.Townsfolk, "贤者", "Sage", "如果被恶魔杀死，得知恶魔是两名玩家之一。", "If the Demon kills you, learn that it is one of two players."),
    ClocktowerRole(ClocktowerTeam.Outsider, "呆瓜", "Klutz", "当你得知自己死亡时，公开选择一名存活玩家；若对方邪恶，你的阵营失败。", "When you learn that you died, publicly choose one alive player: if they are evil, your team loses."),
)

private val completeClocktowerRoles = (completeTroubleBrewingRoles + noGreaterJoyExtraRoles).distinctBy { it.enName }

private val noGreaterJoyRoleNames = setOf(
    "Clockmaker",
    "Investigator",
    "Empath",
    "Chambermaid",
    "Artist",
    "Sage",
    "Drunk",
    "Klutz",
    "Baron",
    "Scarlet Woman",
    "Imp",
)

internal fun clocktowerRolesForScript(script: ClocktowerScript): List<ClocktowerRole> = when (script) {
    ClocktowerScript.TroubleBrewing -> completeTroubleBrewingRoles
    ClocktowerScript.NoGreaterJoy -> completeClocktowerRoles.filter { it.enName in noGreaterJoyRoleNames }
}

internal fun ClocktowerTeam.label(context: Context): String = when (this) {
    ClocktowerTeam.Townsfolk -> context.getString(R.string.clocktower_team_townsfolk)
    ClocktowerTeam.Outsider -> context.getString(R.string.clocktower_team_outsider)
    ClocktowerTeam.Minion -> context.getString(R.string.clocktower_team_minion)
    ClocktowerTeam.Demon -> context.getString(R.string.clocktower_team_demon)
}

internal fun ClocktowerRole.nameFor(language: String): String = if (language == "en") enName else zhName

internal fun ClocktowerRole.descriptionFor(language: String): String = if (language == "en") enDescription else zhDescription

internal fun ClocktowerScript.nameFor(language: String): String = when (this) {
    ClocktowerScript.TroubleBrewing -> if (language == "en") "Trouble Brewing" else "暗流涌动"
    ClocktowerScript.NoGreaterJoy -> "No Greater Joy"
}

private fun defaultClocktowerScriptFor(playerCount: Int): ClocktowerScript =
    if (playerCount in 5..6) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing

internal fun canStartClocktowerScript(script: ClocktowerScript): Boolean =
    script == ClocktowerScript.TroubleBrewing || script == ClocktowerScript.NoGreaterJoy

internal fun clocktowerDistribution(playerCount: Int): Map<ClocktowerTeam, Int> {
    return when (playerCount) {
        5 -> mapOf(ClocktowerTeam.Townsfolk to 3, ClocktowerTeam.Outsider to 0, ClocktowerTeam.Minion to 1, ClocktowerTeam.Demon to 1)
        6 -> mapOf(ClocktowerTeam.Townsfolk to 3, ClocktowerTeam.Outsider to 1, ClocktowerTeam.Minion to 1, ClocktowerTeam.Demon to 1)
        7 -> mapOf(ClocktowerTeam.Townsfolk to 5, ClocktowerTeam.Outsider to 0, ClocktowerTeam.Minion to 1, ClocktowerTeam.Demon to 1)
        8 -> mapOf(ClocktowerTeam.Townsfolk to 5, ClocktowerTeam.Outsider to 1, ClocktowerTeam.Minion to 1, ClocktowerTeam.Demon to 1)
        9 -> mapOf(ClocktowerTeam.Townsfolk to 5, ClocktowerTeam.Outsider to 2, ClocktowerTeam.Minion to 1, ClocktowerTeam.Demon to 1)
        10 -> mapOf(ClocktowerTeam.Townsfolk to 7, ClocktowerTeam.Outsider to 0, ClocktowerTeam.Minion to 2, ClocktowerTeam.Demon to 1)
        11 -> mapOf(ClocktowerTeam.Townsfolk to 7, ClocktowerTeam.Outsider to 1, ClocktowerTeam.Minion to 2, ClocktowerTeam.Demon to 1)
        12 -> mapOf(ClocktowerTeam.Townsfolk to 7, ClocktowerTeam.Outsider to 2, ClocktowerTeam.Minion to 2, ClocktowerTeam.Demon to 1)
        13 -> mapOf(ClocktowerTeam.Townsfolk to 9, ClocktowerTeam.Outsider to 0, ClocktowerTeam.Minion to 3, ClocktowerTeam.Demon to 1)
        14 -> mapOf(ClocktowerTeam.Townsfolk to 9, ClocktowerTeam.Outsider to 1, ClocktowerTeam.Minion to 3, ClocktowerTeam.Demon to 1)
        else -> mapOf(ClocktowerTeam.Townsfolk to 9, ClocktowerTeam.Outsider to 2, ClocktowerTeam.Minion to 3, ClocktowerTeam.Demon to 1)
    }
}

private fun clocktowerRolesFor(playerCount: Int): List<ClocktowerRole> {
    val distribution = clocktowerDistribution(playerCount)
    return distribution.flatMap { (team, count) ->
        completeTroubleBrewingRoles.filter { it.team == team }.shuffled().take(count)
    }.shuffled()
}

private data class ClocktowerAssignment(
    val actualRole: ClocktowerRole,
    val shownRole: ClocktowerRole,
)

private fun generateClocktowerAssignments(playerCount: Int, script: ClocktowerScript): List<ClocktowerAssignment> {
    val roles = clocktowerRolesForScript(script)
    val baseDistribution = clocktowerDistribution(playerCount)
    val demon = roles.filter { it.team == ClocktowerTeam.Demon }.random()
    val baseOutsiderCount = baseDistribution.getValue(ClocktowerTeam.Outsider)
    val minions = roles
        .filter { it.team == ClocktowerTeam.Minion }
        .shuffled()
        .take(baseDistribution.getValue(ClocktowerTeam.Minion))
    val includesBaron = minions.any { it.enName == "Baron" }
    val baronOutsiderIncrease = if (includesBaron) {
        if (script == ClocktowerScript.NoGreaterJoy) (2 - baseOutsiderCount).coerceIn(0, 2) else 2
    } else {
        0
    }
    val outsiderCount = baseOutsiderCount + baronOutsiderIncrease
    val townsfolkCount = (baseDistribution.getValue(ClocktowerTeam.Townsfolk) - baronOutsiderIncrease).coerceAtLeast(0)
    val outsiders = roles
        .filter { it.team == ClocktowerTeam.Outsider }
        .shuffled()
        .take(outsiderCount)
    val townsfolk = roles
        .filter { it.team == ClocktowerTeam.Townsfolk }
        .shuffled()
        .take(townsfolkCount)
    val actualRoles = (listOf(demon) + minions + outsiders + townsfolk).shuffled()
    val townsfolkPool = roles.filter { it.team == ClocktowerTeam.Townsfolk }
    return actualRoles.map { role ->
        if (role.enName == "Drunk") {
            val fakeRole = townsfolkPool
                .filterNot { candidate -> candidate in actualRoles }
                .randomOrNull()
                ?: townsfolkPool.random()
            ClocktowerAssignment(actualRole = role, shownRole = fakeRole)
        } else {
            ClocktowerAssignment(actualRole = role, shownRole = role)
        }
    }
}

@Composable
internal fun CampBoardGameHostApp() {
    val baseContext = LocalContext.current
    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
    val activeGameWerewolfSaveValidator = remember {
        WerewolfActiveGameSaveValidator(WerewolfRoleRegistry.builtIn())
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }
    var storytellerAutomationMode by remember { mutableStateOf(baseContext.loadStorytellerAutomationMode()) }
    val storytellerRecommendationUxPolicy =
        StorytellerRecommendationUxPolicy.fromLegacyMode(storytellerAutomationMode)
    val automaticStorytellerInfo = storytellerRecommendationUxPolicy.automaticExecution
    val context = remember(languageMode) { baseContext.localized(languageMode) }
    val language = context.resources.configuration.locales[0].language
    var screen by remember { mutableStateOf(Screen.Landing) }
    var currentGameKind by remember { mutableStateOf(GameKind.Undercover) }
    var savedGamePreview by remember(context) { mutableStateOf(baseContext.loadSavedGamePreview(context)) }
    var gameHistory by remember { mutableStateOf(baseContext.loadGameHistory()) }
    var showHostTools by remember { mutableStateOf(false) }
    var hostToolTab by remember { mutableStateOf(HostToolTab.Roles) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var undercoverCount by remember { mutableStateOf(1) }
    var includeBlank by remember { mutableStateOf(false) }
    var werewolfCount by remember { mutableStateOf(1) }
    var includeSeer by remember { mutableStateOf(true) }
    var includeWitch by remember { mutableStateOf(false) }
    var includeHunter by remember { mutableStateOf(false) }
    var lastWordsMode by remember { mutableStateOf(LastWordsMode.FirstDay) }
    var lastWordsPromptNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentDealIndex by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(1) }
    var selectedElimination by remember { mutableStateOf<String?>(null) }
    var werewolfJudgeStepIndex by remember { mutableStateOf(0) }
    var pendingNightDeath by remember { mutableStateOf<String?>(null) }
    var seerCheckTarget by remember { mutableStateOf<String?>(null) }
    var witchSaveUsed by remember { mutableStateOf(false) }
    var witchPoisonUsed by remember { mutableStateOf(false) }
    var witchSavedTonight by remember { mutableStateOf(false) }
    var witchPoisonTarget by remember { mutableStateOf<String?>(null) }
    var hunterShotTarget by remember { mutableStateOf<String?>(null) }
    var selectedDayExile by remember { mutableStateOf<String?>(null) }
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
    var clocktowerRecommendedDrunkInvestigatorRoleName by remember { mutableStateOf<String?>(null) }
    var clocktowerRecommendedDrunkInvestigatorSeats by remember { mutableStateOf<List<Int>>(emptyList()) }
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
    var clocktowerRulesetRoleIds by remember { mutableStateOf<Set<RoleId>>(emptySet()) }
    var showResults by remember { mutableStateOf(false) }
    var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }
    var newCommonPlayerName by remember { mutableStateOf("") }
    val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }
    val playerNames = remember { mutableStateListOf<String>() }
    var hostSeatingSetupFlow by remember { mutableStateOf(HostSeatingSetupFlow()) }
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
    val troubleBrewingSetupRecommendationScope = rememberCoroutineScope()
    val troubleBrewingSetupRecommendationPrewarmer = remember {
        val recommendationCoordinator = ClocktowerRecommendationCoordinator()
        TroubleBrewingSetupRecommendationPrewarmCoordinator { request ->
            recommendationCoordinator.recommendSetup(request)
        }
    }
    val troubleBrewingSetupRecommendationRevealCoordinator =
        remember(troubleBrewingSetupRecommendationPrewarmer) {
            TroubleBrewingSetupRecommendationRevealCoordinator(
                prewarmer = troubleBrewingSetupRecommendationPrewarmer,
            )
        }
    val troubleBrewingFirstNightPrecomputeScope = rememberCoroutineScope()
    val troubleBrewingFirstNightPrecomputeCoordinator = remember {
        val recommendationCoordinator = ClocktowerRecommendationCoordinator()
        TroubleBrewingFirstNightPrecomputeCoordinator<GameState, List<DecisionCandidate<SetupClueOutcome>>> { request ->
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
    var a4InitialRecommendationDemandRecorded by remember(clocktowerGameId) { mutableStateOf(false) }
    val recordA4InitialRecommendationDemand = demand@{
        if (a4InitialRecommendationDemandRecorded) return@demand
        val request = a4InitialIdentityPrewarmRequestOrNull() ?: return@demand
        val report = a4IdentityRevealPrewarmer.probe(request)
        Log.i(A4_IDENTITY_PREWARM_LOG_TAG, report.toLogLine())
        a4InitialRecommendationDemandRecorded = true
    }

    fun storytellerPhaseFor(phase: ClocktowerPhase = clocktowerPhase): StorytellerPhase = when (phase) {
        ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
        ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
        ClocktowerPhase.Day -> StorytellerPhase.DAY
        ClocktowerPhase.Night -> StorytellerPhase.NIGHT
    }

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
            }
        }
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
        val epistemicPhase = when (eventPhase) {
            ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
            ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
            ClocktowerPhase.Day -> StorytellerPhase.DAY
            ClocktowerPhase.Night -> StorytellerPhase.NIGHT
        }
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

    fun nextNightPublicAliveObservationPreflightOrNull(): Pair<String, Int>? {
        val originalDeathName = clocktowerPendingNightDeath
        val dawnDeathFacts = resolveTroubleBrewingDawnDeathFacts(
            cards = cards,
            targetName = originalDeathName,
            poisonedPlayerName = clocktowerConfirmedPoisonTarget,
            monkProtectedTargetName = clocktowerConfirmedMonkProtectedTarget,
        )
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
                mayorRedirectMayApply = dawnDeathFacts.mayorSeat != null,
                attackOutcome = dawnDeathFacts.attackOutcome,
                demonSafeSeats = dawnDeathFacts.demonSafeSeats,
                effectiveNightState = effectiveNightState,
                demonRoleIds = demonRoleIds,
            ),
        )
        val resolvedDeathSeat = deathTransition.dawnCommitIntent?.death?.targetSeat ?: return null
        val resolvedDeathName = cards.getOrNull(resolvedDeathSeat - 1)?.name ?: return null
        val eventOffset =
            if (
                dawnDeathFacts.mayorSeat != null &&
                dawnDeathFacts.originalDeathSeat != null &&
                resolvedDeathSeat != dawnDeathFacts.originalDeathSeat
            ) {
                2
            } else {
                1
            }
        return resolvedDeathName to (clocktowerEventCounter + eventOffset)
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
            GameKind.Werewolf -> {
                activeGameWerewolfSaveValidator.validate(
                    assignedRoles = cards.map { card -> card.role },
                    werewolfCount = werewolfCount,
                    includeSeer = includeSeer,
                    includeWitch = includeWitch,
                    includeHunter = includeHunter,
                )
            }
        }
        val entryPoint = when (screen) {
            Screen.PassPhone -> RecoveryEntryPoint.PassPhone
            Screen.RevealCard -> RecoveryEntryPoint.RevealCard
            else -> RecoveryEntryPoint.Stable
        }
        val commonCards = cards.toList()
        val commonRecords = records.toList()
        val recoveryGame: RecoveryGame = when (currentGameKind) {
            GameKind.Undercover -> UndercoverRecovery(
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
                undercoverCount = undercoverCount,
                includeBlank = includeBlank,
                lastWordsMode = lastWordsMode,
            )
            GameKind.Werewolf -> WerewolfRecovery(
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
                round = round,
                cards = commonCards,
                records = commonRecords,
                outcome = gameOutcome,
                werewolfCount = werewolfCount,
                includeSeer = includeSeer,
                includeWitch = includeWitch,
                includeHunter = includeHunter,
                lastWordsMode = lastWordsMode,
                judgeStepIndex = werewolfJudgeStepIndex,
                pendingNightDeath = pendingNightDeath,
                seerCheckTarget = seerCheckTarget,
                witchSaveUsed = witchSaveUsed,
                witchPoisonUsed = witchPoisonUsed,
                witchSavedTonight = witchSavedTonight,
                witchPoisonTarget = witchPoisonTarget,
                hunterShotTarget = hunterShotTarget,
            )
            GameKind.Clocktower -> ClocktowerRecovery(
                entryPoint = entryPoint,
                currentDealIndex = currentDealIndex,
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
                    klutzChoiceName = clocktowerKlutzChoiceName,
                    klutzReturnToDawn = clocktowerKlutzReturnToDawn,
                    ghostVoteAuthority = clocktowerGhostVoteAuthorityState.value,
                    highestVoteName = clocktowerHighestVoteNameState.value,
                    highestVoteCount = clocktowerHighestVoteCountState.value,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = clocktowerGameStateRevision,
                    playerInputRevision = clocktowerPlayerInputRevision,
                    semanticHistoryMode = clocktowerSemanticHistoryMode,
                    actionTimeline = clocktowerActionTimeline,
                    nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
                    events = clocktowerEvents.toList(),
                    epistemicObservations = clocktowerEpistemicObservations.toList(),
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
        lastWordsMode = LastWordsMode.FirstDay
        lastWordsPromptNames = emptyList()
        selectedElimination = null

        werewolfCount = 1
        includeSeer = true
        includeWitch = false
        includeHunter = false
        werewolfJudgeStepIndex = 0
        pendingNightDeath = null
        seerCheckTarget = null
        witchSaveUsed = false
        witchPoisonUsed = false
        witchSavedTonight = false
        witchPoisonTarget = null
        hunterShotTarget = null
        selectedDayExile = null

        selectedClocktowerScript = null
        clocktowerGameSession = null
        publishClocktowerSessionView()
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        clocktowerRulesetRoleIds = emptySet()
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
        clocktowerRecommendedDrunkInvestigatorRoleName = null
        clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
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
                undercoverCount = game.undercoverCount
                includeBlank = game.includeBlank
                lastWordsMode = game.lastWordsMode
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
                        actionTimeline = history.actionTimeline,
                        epistemicObservationLog = EpistemicObservationLog(history.epistemicObservations),
                        semanticHistoryMode = history.semanticHistoryMode,
                        nextTimelineGlobalSequence = history.nextTimelineGlobalSequence,
                    ),
                )
                publishClocktowerSessionView()
                clocktowerRulesetRoleIds = if (game.identity.script == ClocktowerScript.TroubleBrewing) {
                    runtime?.rulesetBasis?.roleIds.orEmpty()
                } else {
                    emptySet()
                }
                clocktowerRulesetRef = runtime?.rulesetRef
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
            is WerewolfRecovery -> Unit
        }

        screen = when (plan.safeReentry) {
            RecoverySafeReentry.UndercoverGame -> Screen.Game
            is RecoverySafeReentry.PassPhone -> Screen.PassPhone
            is RecoverySafeReentry.RevealCard -> Screen.RevealCard
            is RecoverySafeReentry.ClocktowerJudge -> Screen.ClocktowerJudge
        }
    }

    fun restoreSavedGame() {
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

    fun maxWerewolfFor(count: Int): Int {
        return (count - 1).coerceAtLeast(1)
    }

    fun clampWerewolfSettings() {
        werewolfCount = werewolfCount.coerceIn(1, maxWerewolfFor(playerNames.size))
        val selectedSpecials = listOf(includeSeer, includeWitch, includeHunter).count { it }
        if (werewolfCount + selectedSpecials > playerNames.size) {
            werewolfCount = (playerNames.size - selectedSpecials).coerceAtLeast(1)
        }
    }

    fun shouldPromptLastWords(): Boolean = when (lastWordsMode) {
        LastWordsMode.None -> false
        LastWordsMode.FirstDay -> round <= 1
        LastWordsMode.FirstTwoDays -> round <= 2
        LastWordsMode.Always -> true
    }

    fun addCurrentPlayer(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty() && playerNames.size < MAX_PLAYERS && trimmedName !in playerNames) {
            playerNames.add(trimmedName)
            clampUndercoverCount()
            clampWerewolfSettings()
        }
    }

    fun removeCurrentPlayer(index: Int) {
        if (index in playerNames.indices) {
            playerNames.removeAt(index)
            clampUndercoverCount()
            clampWerewolfSettings()
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
            baseContext.saveCommonPlayers(commonPlayers)
            newCommonPlayerName = ""
        }
    }

    fun removeCommonPlayer(name: String) {
        commonPlayers.remove(name)
        baseContext.saveCommonPlayers(commonPlayers)
    }

    fun resetDealState(
        nextGameKind: GameKind,
        clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
        preparedClocktowerSeed: Long? = null,
    ) {
        invalidateA4SessionBoundary()
        clearSavedGameState()
        committedClocktowerSetup = null
        committedTroubleBrewingSetupRotationRecord = null
        currentGameKind = nextGameKind
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
        werewolfJudgeStepIndex = 0
        lastWordsPromptNames = emptyList()
        pendingNightDeath = null
        seerCheckTarget = null
        witchSaveUsed = false
        witchPoisonUsed = false
        witchSavedTonight = false
        witchPoisonTarget = null
        hunterShotTarget = null
        selectedDayExile = null
        clocktowerPhase = ClocktowerPhase.FirstNight
        if (nextGameKind == GameKind.Clocktower) {
            val gameId = UUID.randomUUID().toString()
            val gameSeed = preparedClocktowerSeed ?: newClocktowerSeed()
            clocktowerGameSession = ClocktowerGameSession.createProduction(
                gameId = gameId,
                gameSeed = gameSeed,
                initialState = cards.toClocktowerGameState(
                    script = clocktowerScript,
                    seed = gameSeed,
                    poisonedPlayerName = null,
                ),
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
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
                clocktowerRulesetRoleIds = rulesetBasis.roleIds
                clocktowerRulesetRef = troubleBrewingRulesetRefFor(rulesetBasis)
                    ?: error("Unable to resolve Trouble Brewing ruleset reference at setup.")
            } else {
                clocktowerRulesetRoleIds = emptySet()
                clocktowerRulesetRef = null
            }
        } else {
            clocktowerRulesetRoleIds = emptySet()
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
        // Identity is committed before reveal; a Drunk's concrete first-night
        // clue is not, because the Poisoner may still change its legality.
        clocktowerRecommendedDrunkInvestigatorRoleName = null
        clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
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
        persistActiveGameStateIfNeeded()
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
                else -> ""
            }
            PlayerCard(name = name.ifBlank { context.playerName(index + 1) }, role = role, word = word)
        })
        resetDealState(GameKind.Undercover)
    }

    fun startWerewolfGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Werewolf)
        if (playerNames.size < MIN_WEREWOLF_PLAYERS) return
        val roles = werewolfRolesFor(
            playerCount = playerNames.size,
            werewolfCount = werewolfCount,
            includeSeer = includeSeer,
            includeWitch = includeWitch,
            includeHunter = includeHunter,
        )
        cards.clear()
        cards.addAll(playerNames.mapIndexed { index, name ->
            val role = roles[index]
            PlayerCard(
                name = name.ifBlank { context.playerName(index + 1) },
                role = role,
                roleLabel = context.getString(role.labelResId()),
                word = role.werewolfDescription(context),
            )
        })
        resetDealState(GameKind.Werewolf)
    }

    fun startTroubleBrewingGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
        val preparedSeed = newClocktowerSeed()

        val datasetJson = baseContext.assets
            .open("setup/trouble_brewing_setup_presets_v2_final.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val dataset = TroubleBrewingSetupPresetJson.parse(datasetJson)

        val rotationHistory = TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
            .historyFor(
                datasetId = dataset.datasetId,
                schemaVersion = dataset.schemaVersion,
                playerCount = playerNames.size,
            )

        val characterRegistry = BuiltInClocktowerRulesetCatalog
            .fromContext(baseContext)
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry

        val preparedSetup = TroubleBrewingProductionSetupPreparer.prepare(
            dataset = dataset,
            characterRegistry = characterRegistry,
            orderedPlayerNames = playerNames.toList(),
            gameSeed = preparedSeed,
            recentSetupRotationHistory = rotationHistory,
        )

        val resolvedAssignments = TroubleBrewingDealRoleResolver.resolve(
            dealPlan = preparedSetup.dealPlan,
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

        val setupRecommendationRoleDefinitions =
            clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing)
        val initialSetupRecommendationRequest = SetupCoordinationRequest(
            game = committedCards.toClocktowerGameState(
                script = ClocktowerScript.TroubleBrewing,
                seed = preparedSeed,
                poisonedPlayerName = null,
            ),
            roles = setupRecommendationRoleDefinitions,
            lockedDecisions = TroubleBrewingSetupRecommendationLock.lockedDecisions(
                dealPlan = preparedSetup.dealPlan,
                roleDefinitions = setupRecommendationRoleDefinitions,
            ),
            history = gameHistory.toClocktowerSetupHistory(),
        )
        val initialFirstNightPrecomputeRequest = committedCards.toClocktowerGameState(
            script = ClocktowerScript.TroubleBrewing,
            seed = preparedSeed,
            poisonedPlayerName = null,
        )

        cards.clear()
        cards.addAll(committedCards)

        troubleBrewingSetupRecommendationRevealCoordinator.onCommittedDeal(
            request = initialSetupRecommendationRequest,
            enterReveal = {
                resetDealState(
                    nextGameKind = GameKind.Clocktower,
                    clocktowerScript = ClocktowerScript.TroubleBrewing,
                    preparedClocktowerSeed = preparedSeed,
                )
                committedTroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecordFactory.fromSelection(
                    preparedSetup.selection,
                )
                committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(
                    dealPlan = preparedSetup.dealPlan,
                    resolvedAssignments = resolvedAssignments,
                )
                persistActiveGameStateIfNeeded()
                troubleBrewingFirstNightPrecomputeCoordinator.prewarm(
                    request = initialFirstNightPrecomputeRequest,
                    launchBackground = { work ->
                        troubleBrewingFirstNightPrecomputeScope.launch(Dispatchers.Default) {
                            work()
                        }
                    },
                )
            },
            launchBackground = { work ->
                troubleBrewingSetupRecommendationScope.launch(Dispatchers.Default) {
                    work()
                }
            },
        )
    }

    fun startClocktowerGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
        if (playerNames.size < MIN_CLOCKTOWER_PLAYERS) return
        val script = if (playerNames.size in 5..6) {
            selectedClocktowerScript ?: defaultClocktowerScriptFor(playerNames.size)
        } else {
            ClocktowerScript.TroubleBrewing
        }
        if (!canStartClocktowerScript(script)) return

        if (script == ClocktowerScript.TroubleBrewing) {
            startTroubleBrewingGame()
            return
        }

        val assignments = generateClocktowerAssignments(playerNames.size, script)
        if (assignments.size != playerNames.size) return
        val preparedSeed = newClocktowerSeed()
        val preparedCards = playerNames.mapIndexed { index, name ->
            val assignment = assignments[index]
            val role = assignment.actualRole
            val shownRole = assignment.shownRole
            PlayerCard(
                name = name.ifBlank { context.playerName(index + 1) },
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
        val preparedSetupPlan = if (assignments.any { it.actualRole.enName == "Drunk" }) {
            runCatching {
                ClocktowerRecommendationCoordinator()
                    .selectSetupPlan(
                        request = SetupCoordinationRequest(
                            game = preparedCards.toClocktowerGameState(script, preparedSeed),
                            roles = clocktowerRoleDefinitionsForScript(script),
                            history = gameHistory.toClocktowerSetupHistory(),
                        ),
                        style = storytellerAutomationMode.style ?: RecommendationStyle.BALANCED,
                    )
            }.getOrNull()
        } else {
            null
        }
        val recommendedDrunkShownRole = preparedSetupPlan
            ?.decisions
            ?.filterIsInstance<StorytellerDecision.DrunkShownRole>()
            ?.singleOrNull()
            ?.role
            ?.value
            ?.let { roleName ->
                clocktowerRolesForScript(script).firstOrNull { role ->
                    role.enName == roleName && role.team == ClocktowerTeam.Townsfolk
                }
            }
        val committedCards = if (recommendedDrunkShownRole == null) {
            preparedCards
        } else {
            preparedCards.map { card ->
                if (card.clocktowerRole?.enName != "Drunk") {
                    card
                } else {
                    card.copy(
                        roleLabel = recommendedDrunkShownRole.nameFor(language),
                        clocktowerShownRole = recommendedDrunkShownRole,
                        word = context.getString(
                            R.string.clocktower_card_desc_format,
                            recommendedDrunkShownRole.team.label(context),
                            recommendedDrunkShownRole.descriptionFor(language),
                        ),
                    )
                }
            }
        }
        cards.clear()
        cards.addAll(committedCards)
        resetDealState(GameKind.Clocktower, script, preparedSeed)
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
        gameHistory = baseContext.archiveGame(
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
        lastWordsPromptNames = emptyList()
        resetClocktowerFlow()
        screen = Screen.Setup
    }

    fun archiveAndStartNewGame() {
        if (!archiveCurrentGameForRestart()) return
        when (currentGameKind) {
            GameKind.Undercover -> startUndercoverGame()
            GameKind.Werewolf -> startWerewolfGame()
            GameKind.Clocktower -> startClocktowerGame()
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

    fun setClocktowerShownRole(playerName: String, nextRole: ClocktowerRole) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            val targetSeat = index + 1
            requireClocktowerGameSession().commitShownRoleBoundary(
                seat = targetSeat,
                shownRole = RoleId(nextRole.enName),
            )
            publishClocktowerSessionView()
            invalidateA4RevisionScope()
            cards[index] = cards[index].copy(
                roleLabel = nextRole.nameFor(language),
                clocktowerShownRole = nextRole,
                word = context.getString(
                    R.string.clocktower_card_desc_format,
                    nextRole.team.label(context),
                    nextRole.descriptionFor(language),
                ),
            )
        }
    }

    fun promoteScarletWomanIfNeeded(): String? {
        val alivePlayers = cards.filter { it.eliminatedRound == null }
        // The Demon is already marked dead when this runs: four alive now means
        // there were five alive immediately before the Demon died.
        if (alivePlayers.size < 4) return null
        val scarletWoman = alivePlayers.firstOrNull {
            it.clocktowerRole?.enName == "Scarlet Woman" && it.name != clocktowerConfirmedPoisonTarget
        } ?: return null
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
        Screen.WerewolfSettings,
        Screen.ClocktowerSettings -> HostSeatingBackOrigin.GameSettings
        else -> null
    }
    BackHandler(enabled = hostSeatingBackOrigin != null) {
        applyHostSeatingBack(requireNotNull(hostSeatingBackOrigin))
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
                    if (
                        !showResults && (
                            screen == Screen.WerewolfJudge ||
                            screen == Screen.ClocktowerJudge ||
                            screen == Screen.Game
                        )
                    ) {
                        HostToolsTopBar(
                            onOpen = {
                                hostToolTab = HostToolTab.Roles
                                showHostTools = true
                            },
                        )
                    }
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
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onRemoveCurrentPlayer = ::removeCurrentPlayer,
                    onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
                    onResumeSavedGame = ::restoreSavedGame,
                    onDiscardSavedGame = ::clearSavedGameState,
                    onOpenSettings = { screen = Screen.Settings },
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
                    onOpenWerewolfSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Werewolf)
                        screen = Screen.WerewolfSettings
                    },
                    onOpenClocktowerSettings = {
                        hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Clocktower)
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

                    Screen.WerewolfSettings -> WerewolfSettingsScreen(
                        playerCount = playerCount,
                        werewolfCount = werewolfCount,
                        includeSeer = includeSeer,
                        includeWitch = includeWitch,
                        includeHunter = includeHunter,
                        lastWordsMode = lastWordsMode,
                        onWerewolfCountChange = { next ->
                            werewolfCount = next
                            clampWerewolfSettings()
                        },
                        onIncludeSeerChange = {
                            includeSeer = it
                            clampWerewolfSettings()
                        },
                        onIncludeWitchChange = {
                            includeWitch = it
                            clampWerewolfSettings()
                        },
                        onIncludeHunterChange = {
                            includeHunter = it
                            clampWerewolfSettings()
                        },
                        onLastWordsModeChange = { lastWordsMode = it },
                        onApplyTemplate = { template ->
                            werewolfCount = template.werewolfCount
                            includeSeer = template.includeSeer
                            includeWitch = template.includeWitch
                            includeHunter = template.includeHunter
                            clampWerewolfSettings()
                        },
                        onBack = {
                            applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
                        },
                        onStart = ::startWerewolfGame,
                    )

                    Screen.ClocktowerSettings -> ClocktowerSettingsScreen(
                        playerCount = playerCount,
                        playerNames = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
                            "Clocktower settings require confirmed seating"
                        }.playerNames,
                        selectedScript = selectedClocktowerScript ?: defaultClocktowerScriptFor(playerCount),
                        onScriptChange = { selectedClocktowerScript = it },
                        onBack = {
                            applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
                        },
                        onStart = ::startClocktowerGame,
                    )

                    Screen.Settings -> SettingsScreen(
                        languageMode = languageMode,
                        storytellerAutomationMode = storytellerAutomationMode,
                        commonPlayers = commonPlayers,
                        newCommonPlayerName = newCommonPlayerName,
                        onLanguageModeChange = { nextMode ->
                            languageMode = nextMode
                            baseContext.saveLanguageMode(nextMode)
                        },
                        onStorytellerAutomationModeChange = { mode ->
                            storytellerAutomationMode = mode
                            baseContext.saveStorytellerAutomationMode(mode)
                        },
                        onNewCommonPlayerNameChange = { newCommonPlayerName = it },
                        onAddCommonPlayer = ::addCommonPlayer,
                        onRemoveCommonPlayer = ::removeCommonPlayer,
                        onBack = { screen = Screen.Setup },
                    )

                    Screen.PassPhone -> PassPhoneScreen(
                    playerName = cards[currentDealIndex].name,
                    gameKind = currentGameKind,
                    current = currentDealIndex + 1,
                    total = cards.size,
                    onReveal = { screen = Screen.RevealCard },
                )

                    Screen.RevealCard -> RevealCardScreen(
                    card = cards[currentDealIndex],
                    gameKind = currentGameKind,
                    current = currentDealIndex + 1,
                    total = cards.size,
                    onHide = {
                        if (currentDealIndex == cards.lastIndex) {
                            screen = when (currentGameKind) {
                                GameKind.Werewolf -> Screen.WerewolfJudge
                                GameKind.Clocktower -> Screen.ClocktowerJudge
                                GameKind.Undercover -> Screen.Game
                            }
                        } else {
                            currentDealIndex += 1
                            screen = Screen.PassPhone
                        }
                    },
                )

                    Screen.WerewolfJudge -> WerewolfJudgeScreen(
                        cards = cards,
                        records = records,
                        nightNumber = round,
                        stepIndex = werewolfJudgeStepIndex,
                        pendingNightDeath = pendingNightDeath,
                        seerCheckTarget = seerCheckTarget,
                        witchSaveUsed = witchSaveUsed,
                        witchPoisonUsed = witchPoisonUsed,
                        witchSavedTonight = witchSavedTonight,
                        witchPoisonTarget = witchPoisonTarget,
                        hunterShotTarget = hunterShotTarget,
                        selectedDayExile = selectedDayExile,
                        gameOutcome = gameOutcome,
                        lastWordsPromptNames = lastWordsPromptNames,
                        onStepIndexChange = { werewolfJudgeStepIndex = it },
                        onSelectNightDeath = { pendingNightDeath = it },
                        onSelectSeerCheck = { seerCheckTarget = it },
                        onToggleWitchSave = { witchSavedTonight = it },
                        onSelectWitchPoison = { witchPoisonTarget = it },
                        onSelectHunterShot = { hunterShotTarget = it },
                        onConfirmDawn = { deathEvents ->
                            lastWordsPromptNames = emptyList()
                            val eliminatedNames = mutableListOf<String>()
                            deathEvents.distinctBy { it.first }.forEach { (deathName, note) ->
                                val index = cards.indexOfFirst { it.name == deathName }
                                if (index >= 0 && cards[index].eliminatedRound == null) {
                                    cards[index] = cards[index].copy(eliminatedRound = round)
                                    records.add(EliminationRecord(round, deathName, note))
                                    eliminatedNames.add(deathName)
                                }
                            }
                            val dawnOutcome = evaluateGameOutcome(context, cards, currentGameKind)
                            gameOutcome = dawnOutcome
                            if (dawnOutcome != null) showResults = true
                            if (dawnOutcome == null && eliminatedNames.isNotEmpty() && shouldPromptLastWords()) {
                                lastWordsPromptNames = eliminatedNames
                            }
                            if (witchSavedTonight) witchSaveUsed = true
                            if (witchPoisonTarget != null) witchPoisonUsed = true
                            pendingNightDeath = null
                            seerCheckTarget = null
                            witchSavedTonight = false
                            witchPoisonTarget = null
                            hunterShotTarget = null
                        },
                        onSelectDayExile = { selectedDayExile = it },
                        onConfirmDayExile = {
                            lastWordsPromptNames = emptyList()
                            val exileName = selectedDayExile
                            val eliminatedNames = mutableListOf<String>()
                            var dayOutcome: GameOutcome? = null
                            if (exileName != null) {
                                val index = cards.indexOfFirst { it.name == exileName }
                                val exiledRole = cards.getOrNull(index)?.role
                                if (index >= 0 && cards[index].eliminatedRound == null) {
                                    cards[index] = cards[index].copy(eliminatedRound = round)
                                    records.add(EliminationRecord(round, exileName, context.getString(R.string.werewolf_record_day_exile)))
                                    eliminatedNames.add(exileName)
                                }
                                val shotName = hunterShotTarget?.takeIf { exiledRole == Role.Hunter && it != exileName }
                                if (shotName != null) {
                                    val shotIndex = cards.indexOfFirst { it.name == shotName }
                                    if (shotIndex >= 0 && cards[shotIndex].eliminatedRound == null) {
                                        cards[shotIndex] = cards[shotIndex].copy(eliminatedRound = round)
                                        records.add(EliminationRecord(round, shotName, context.getString(R.string.werewolf_record_hunter_shot)))
                                        eliminatedNames.add(shotName)
                                    }
                                }
                                dayOutcome = evaluateGameOutcome(context, cards, currentGameKind)
                                gameOutcome = dayOutcome
                                if (dayOutcome != null) showResults = true
                            }
                            selectedDayExile = null
                            pendingNightDeath = null
                            seerCheckTarget = null
                            witchSavedTonight = false
                            witchPoisonTarget = null
                            hunterShotTarget = null
                            if (dayOutcome == null) {
                                if (eliminatedNames.isNotEmpty() && shouldPromptLastWords()) {
                                    lastWordsPromptNames = eliminatedNames
                                }
                                werewolfJudgeStepIndex = 0
                                round += 1
                            }
                        },
                        onDismissLastWordsPrompt = { lastWordsPromptNames = emptyList() },
                        onShowResults = {
                            gameOutcome = gameOutcome ?: GameOutcome(
                                title = context.getString(R.string.outcome_manual_title),
                                summary = context.getString(R.string.outcome_manual_summary),
                                reason = context.getString(R.string.outcome_manual_reason),
                            )
                            showResults = true
                        },
                    )

                    Screen.ClocktowerJudge -> ClocktowerJudgeScreen(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        automaticStorytellerStyle = storytellerRecommendationUxPolicy.recommendationStyle,
                        cards = cards,
                        events = clocktowerEvents,
                        script = currentClocktowerScript,
                        gameId = clocktowerGameId,
                        gameSeed = clocktowerGameSeed,
                        gameStateRevision = clocktowerGameStateRevision,
                        playerInputRevision = clocktowerPlayerInputRevision,
                        setupHistory = gameHistory.toClocktowerSetupHistory(),
                        setupRecommendationResultProvider =
                            if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                troubleBrewingSetupRecommendationRevealCoordinator::resultFor
                            } else {
                                null
                            },
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
                        onInitialRecommendationDemand = recordA4InitialRecommendationDemand,
                        phase = clocktowerPhase,
                        round = round,
                        nightCheckpoint = currentClocktowerNightCheckpoint(),
                        pendingNightDeath = clocktowerPendingNightDeath,
                        demonAttackDraftTarget = clocktowerDemonAttackDraftTarget,
                        selectedExecution = clocktowerSelectedExecution,
                        // The draft is visible only while the Poisoner is choosing.
                        // All ability and outcome evaluation must use the confirmed fact.
                        poisonTarget = clocktowerConfirmedPoisonTarget,
                        poisonDraftTarget = clocktowerPoisonTarget,
                        fortuneTellerFirst = clocktowerFortuneTellerFirst,
                        fortuneTellerSecond = clocktowerFortuneTellerSecond,
                        chambermaidFirst = clocktowerChambermaidFirst,
                        chambermaidSecond = clocktowerChambermaidSecond,
                        ravenkeeperTarget = clocktowerRavenkeeperTarget,
                        redHerring = clocktowerRedHerring,
                        recommendedDemonBluffRoleNames = clocktowerRecommendedDemonBluffRoleNames,
                        recommendedDrunkInvestigatorRoleName = clocktowerRecommendedDrunkInvestigatorRoleName,
                        recommendedDrunkInvestigatorSeats = clocktowerRecommendedDrunkInvestigatorSeats,
                        butlerMaster = clocktowerButlerMaster,
                        monkProtectedTarget = clocktowerConfirmedMonkProtectedTarget,
                        monkProtectedDraftTarget = clocktowerMonkProtectedTarget,
                        mayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
                        mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
                        pendingNewDemonName = clocktowerPendingNewDemonName,
                        pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
                        demonSuccessorTarget = clocktowerDemonSuccessorTarget,
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
                                // A Drunk's shown role is committed, but any concrete
                                // first-night clue remains provisional until displayed.
                                clocktowerRecommendedDrunkInvestigatorRoleName = null
                                clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
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
                            advanceClocktowerPlayerInputRevision()
                            clocktowerRedHerring = it
                        },
                        onApplyRecommendation = { plan ->
                            // Applying the same automatic plan is a no-op.  In particular, do
                            // not advance playerInputRevision here: that revision is part of the
                            // recommendation key, so an unconditional increment creates an
                            // endless Loading -> Ready -> apply -> Loading cycle.
                            var setupChanged = false
                            val recommendedRedHerring = plan.decisions
                                .filterIsInstance<StorytellerDecision.RedHerring>()
                                .singleOrNull()
                                ?.let { decision -> cards.getOrNull(decision.seat - 1)?.name }
                            if (recommendedRedHerring != null && recommendedRedHerring != clocktowerRedHerring) {
                                clocktowerRedHerring = recommendedRedHerring
                                setupChanged = true
                            }
                            plan.decisions.filterIsInstance<StorytellerDecision.DrunkShownRole>().singleOrNull()?.let { decision ->
                                val drunkPlayer = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" }
                                val shownRole = clocktowerRolesForScript(currentClocktowerScript)
                                    .firstOrNull { it.enName == decision.role.value }
                                // A shown identity is committed as soon as dealing starts. Recommendation
                                // plans are constrained to it and may only fill a missing legacy value.
                                if (drunkPlayer != null && shownRole != null && drunkPlayer.clocktowerShownRole == null) {
                                    setClocktowerShownRole(drunkPlayer.name, shownRole)
                                    setupChanged = true
                                }
                            }
                            // Concrete Drunk information is provisional: never carry a
                            // setup recommendation across a later Poisoner decision.
                            if (clocktowerRecommendedDrunkInvestigatorRoleName != null ||
                                clocktowerRecommendedDrunkInvestigatorSeats.isNotEmpty()
                            ) {
                                clocktowerRecommendedDrunkInvestigatorRoleName = null
                                clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
                                setupChanged = true
                            }
                            val recommendedDemonBluffs = plan.decisions
                                .filterIsInstance<StorytellerDecision.DemonBluffs>()
                                .singleOrNull()
                                ?.roles
                                ?.map(RoleId::value)
                                .orEmpty()
                            if (recommendedDemonBluffs != clocktowerRecommendedDemonBluffRoleNames) {
                                clocktowerRecommendedDemonBluffRoleNames = recommendedDemonBluffs
                                setupChanged = true
                            }
                            if (setupChanged) advanceClocktowerPlayerInputRevision()
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
                        onConfirmKlutzChoice = { spyRegistersGoodForChoice ->
                            val choice = clocktowerKlutzChoiceName
                            if (choice != null) {
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("呆瓜选择", "Klutz choice"),
                                    "${playerSeatLabel(cards, clocktowerPendingKlutzName)} → ${playerSeatLabel(cards, choice)}",
                                    listOfNotNull(clocktowerPendingKlutzName, choice),
                                )
                                val chosenCard = cards.firstOrNull { it.name == choice }
                                if (chosenCard != null && isClocktowerEvil(chosenCard) && !(chosenCard.clocktowerRole?.enName == "Spy" && spyRegistersGoodForChoice)) {
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
                                (targetCard?.clocktowerRole?.enName == "Recluse" && recluseRegistersAsDemon)
                            if (targetCard?.clocktowerRole?.enName == "Recluse" && recluseRegistersAsDemon) {
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
                            if (slayerDecision.effectApplies && targetIndex >= 0 && targetCard != null && targetCard.eliminatedRound == null && targetRegistersAsDemon) {
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
                        onVirginNomination = { nominatorName, nomineeName, executeNominator ->
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

    if (gameKind == GameKind.Werewolf) {
        val activeWerewolves = activeCards.count { it.role == Role.Werewolf }
        val activeGoodPlayers = activeCards.size - activeWerewolves
        return when {
            activeWerewolves == 0 -> GameOutcome(
                title = context.getString(R.string.outcome_good_title),
                summary = context.getString(R.string.outcome_good_summary),
                reason = context.getString(R.string.outcome_good_reason, activeGoodPlayers),
            )

            activeWerewolves >= activeGoodPlayers -> GameOutcome(
                title = context.getString(R.string.outcome_werewolf_title),
                summary = context.getString(R.string.outcome_werewolf_summary),
                reason = context.getString(R.string.outcome_werewolf_reason, activeWerewolves, activeGoodPlayers),
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

@Composable
internal fun EliminationRecord.displayText(): String {
    val base = stringResource(R.string.elimination_record_format, round, playerName)
    return note?.let { stringResource(R.string.elimination_record_with_note_format, base, it) } ?: base
}

internal fun PlayerCard.hostRoleLabel(context: Context, gameKind: GameKind): String = when (gameKind) {
    GameKind.Clocktower -> actualRoleLabel ?: roleLabel ?: context.getString(role.labelResId())
    GameKind.Werewolf -> roleLabel ?: context.getString(role.labelResId())
    GameKind.Undercover -> context.getString(role.labelResId())
}

internal fun PlayerCard.seatLabel(cards: List<PlayerCard>): String =
    "#${cards.indexOfFirst { it.name == name } + 1} $name"

internal fun playerSeatLabel(cards: List<PlayerCard>, playerName: String?): String =
    cards.firstOrNull { it.name == playerName }?.seatLabel(cards) ?: playerName.orEmpty()
