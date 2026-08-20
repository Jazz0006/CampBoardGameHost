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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
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
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationLedger
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionKind
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
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
import com.codex.campboardgamehost.clocktower.session.DynamicResolutionRequest
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmark
import com.codex.campboardgamehost.clocktower.session.UnifiedSetupSelectorDeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationCandidate
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationRequest
import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult
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
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
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
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.RegistrationInteractionRules
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
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

internal enum class GameKind {
    Undercover,
    Werewolf,
    Clocktower,
}

private enum class LanguageMode(val prefsValue: String) {
    System("system"),
    Chinese("zh"),
    English("en"),
}

internal enum class Role {
    Civilian,
    Undercover,
    Blank,
    Villager,
    Werewolf,
    Seer,
    Witch,
    Hunter,
}

internal data class PlayerCard(
    val name: String,
    val role: Role,
    val word: String,
    val roleLabel: String? = null,
    val actualRoleLabel: String? = null,
    val clocktowerTeam: ClocktowerTeam? = null,
    val clocktowerRole: ClocktowerRole? = null,
    val clocktowerShownRole: ClocktowerRole? = null,
    val eliminatedRound: Int? = null,
)

internal data class EliminationRecord(
    val round: Int,
    val playerName: String,
    val note: String? = null,
)

internal data class GameOutcome(
    val title: String,
    val summary: String,
    val reason: String,
)

internal enum class ClocktowerEventType {
    System,
    Phase,
    RoleAction,
    Information,
    UnreliableInformation,
    Nomination,
    Vote,
    Execution,
    Death,
    RoleChange,
    GameEnd,
}

internal data class ClocktowerEvent(
    val sequence: Int,
    val type: ClocktowerEventType,
    val title: String,
    val detail: String,
    val playerNames: List<String>,
    val phase: ClocktowerPhase,
    val round: Int,
)

private data class SavedGamePreview(
    val title: String,
    val subtitle: String,
    val savedAtLabel: String?,
)

private data class ArchivedGameReview(
    val id: Long,
    val archivedAtMillis: Long,
    val gameKind: GameKind,
    val round: Int,
    val cards: List<PlayerCard>,
    val records: List<EliminationRecord>,
    val events: List<ClocktowerEvent>,
    val outcome: GameOutcome?,
)

private enum class HostToolTab {
    Roles,
    Records,
    History,
}

internal enum class ClocktowerTeam {
    Townsfolk,
    Outsider,
    Minion,
    Demon,
}

internal enum class ClocktowerPhase {
    FirstNight,
    Dawn,
    Day,
    Night,
}

internal enum class ClocktowerDayMode {
    Overview,
    Slayer,
    Artist,
    Klutz,
    Nomination,
    Vote,
    EndConfirm,
    ExecutionResult,
}

internal enum class ClocktowerNightAction {
    None,
    RedHerring,
    Poison,
    ButlerMaster,
    MonkProtect,
    Chambermaid,
    FortuneTeller,
    DemonKill,
    MayorRedirect,
    DemonSuccessor,
    Ravenkeeper,
}

internal enum class ClocktowerDisplayKind {
    None,
    EitherOne,
    Number,
    YesNo,
    RoleReveal,
    Plain,
    EvilInfo,
    Grimoire,
}

internal enum class ClocktowerScript {
    TroubleBrewing,
    NoGreaterJoy,
}

internal data class ClocktowerRole(
    val team: ClocktowerTeam,
    val zhName: String,
    val enName: String,
    val zhDescription: String,
    val enDescription: String,
)

private sealed class DraggedPlayer {
    data class Bench(val name: String) : DraggedPlayer()
    data class Seated(val originalIndex: Int, val name: String) : DraggedPlayer()
}

private data class PlayerDragState(
    val player: DraggedPlayer,
    val center: Offset,
)

private fun Context.playerName(number: Int): String = getString(R.string.default_player_name_format, number)

private const val PREFS_NAME = "camp_board_game_host"
private const val COMMON_PLAYERS_KEY = "common_players"
private const val LANGUAGE_MODE_KEY = "language_mode"
private const val AUTOMATIC_STORYTELLER_INFO_KEY = "automatic_storyteller_info"
private const val STORYTELLER_AUTOMATION_MODE_KEY = "storyteller_automation_mode"
private const val ACTIVE_GAME_STATE_KEY = "active_game_state"
private const val GAME_HISTORY_KEY = "game_history"
private const val ACTIVE_GAME_STATE_VERSION = ActiveGamePersistenceCoordinator.CURRENT_VERSION
internal const val A4_IDENTITY_PREWARM_LOG_TAG = "A4IdentityPrewarm"
internal const val A4_OBSERVATION_CACHE_UPDATE_LOG_TAG = "A4ObservationCacheUpdate"
internal const val UNIFIED_SETUP_SELECTOR_BENCHMARK_LOG_TAG = "UnifiedSetupSelectorBenchmark"
internal const val UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG = "UnifiedFirstNightPoolBenchmark"
private const val MAX_GAME_HISTORY = 20
internal const val MIN_PLAYERS = 3
internal const val MIN_WEREWOLF_PLAYERS = 4
internal const val MIN_CLOCKTOWER_PLAYERS = 5
private const val MAX_PLAYERS = 15

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
    val raw = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(COMMON_PLAYERS_KEY, null) ?: return emptyList()
    return runCatching {
        val json = JSONArray(raw)
        List(json.length()) { index -> json.getString(index) }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }.getOrDefault(emptyList())
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

private fun Context.loadSavedGamePreview(localizedContext: Context): SavedGamePreview? {
    val json = loadActiveGameStateJson() ?: return null
    val preview = runCatching { savedGamePreviewFromJson(localizedContext, json) }
        .getOrElse {
            clearActiveGameState()
            null
        }
    if (preview == null) {
        clearActiveGameState()
    }
    return preview
}

private inline fun <reified T : Enum<T>> enumByName(name: String?): T? {
    if (name.isNullOrBlank()) return null
    return runCatching { enumValueOf<T>(name) }.getOrNull()
}

private fun JSONObject.putNullableString(key: String, value: String?) {
    put(key, value ?: JSONObject.NULL)
}

private fun JSONObject.putNullableInt(key: String, value: Int?) {
    put(key, value ?: JSONObject.NULL)
}

private fun JSONObject.putNullableBoolean(key: String, value: Boolean?) {
    put(key, value ?: JSONObject.NULL)
}

private fun JSONObject.optNullableString(key: String): String? {
    return if (has(key) && !isNull(key)) optString(key) else null
}

private fun JSONObject.optNullableInt(key: String): Int? {
    return if (has(key) && !isNull(key)) optInt(key) else null
}

private fun JSONObject.optNullableBoolean(key: String): Boolean? {
    return if (has(key) && !isNull(key)) optBoolean(key) else null
}

private fun stringsToJsonArray(values: List<String>): JSONArray {
    val json = JSONArray()
    values.forEach { json.put(it) }
    return json
}

private fun JSONArray.toStringList(): List<String> = buildList {
    for (index in 0 until length()) {
        optString(index).takeIf { it.isNotBlank() }?.let(::add)
    }
}

private fun clocktowerRoleByName(enName: String?): ClocktowerRole? {
    if (enName.isNullOrBlank()) return null
    return completeClocktowerRoles.firstOrNull { it.enName == enName }
}

private fun archivedGameReviewFromJson(entry: JSONObject): ArchivedGameReview? {
    val snapshot = entry.optJSONObject("snapshot") ?: return null
    val gameKind = enumByName<GameKind>(snapshot.optNullableString("currentGameKind")) ?: return null
    val cards = snapshot.optJSONArray("cards")?.toPlayerCards().orEmpty()
    if (cards.isEmpty()) return null
    return ArchivedGameReview(
        id = entry.optLong("id", entry.optLong("archivedAtMillis", 0L)),
        archivedAtMillis = entry.optLong("archivedAtMillis", 0L),
        gameKind = gameKind,
        round = snapshot.optInt("round", 1).coerceAtLeast(1),
        cards = cards,
        records = snapshot.optJSONArray("records")?.toEliminationRecords().orEmpty(),
        events = snapshot.optJSONArray("clocktowerEvents")?.toClocktowerEvents().orEmpty(),
        outcome = gameOutcomeFromJson(snapshot.optJSONObject("gameOutcome")),
    )
}

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

private fun Context.archiveGame(snapshot: JSONObject): List<ArchivedGameReview> {
    if (snapshot.optJSONArray("cards")?.length() == 0) return loadGameHistory()
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = runCatching { JSONArray(prefs.getString(GAME_HISTORY_KEY, "[]")) }.getOrDefault(JSONArray())
    val archivedAt = System.currentTimeMillis()
    val next = JSONArray().apply {
        put(JSONObject().apply {
            put("id", archivedAt)
            put("archivedAtMillis", archivedAt)
            put("snapshot", JSONObject(snapshot.toString()))
        })
        for (index in 0 until minOf(existing.length(), MAX_GAME_HISTORY - 1)) {
            existing.optJSONObject(index)?.let(::put)
        }
    }
    prefs.edit().putString(GAME_HISTORY_KEY, next.toString()).commit()
    return loadGameHistory()
}

private fun PlayerCard.toJson(): JSONObject = JSONObject().apply {
    put("name", name)
    put("role", role.name)
    put("word", word)
    putNullableString("roleLabel", roleLabel)
    putNullableString("actualRoleLabel", actualRoleLabel)
    putNullableString("clocktowerTeam", clocktowerTeam?.name)
    putNullableString("clocktowerRole", clocktowerRole?.enName)
    putNullableString("clocktowerShownRole", clocktowerShownRole?.enName)
    putNullableInt("eliminatedRound", eliminatedRound)
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

private fun playerCardsToJsonArray(cards: List<PlayerCard>): JSONArray {
    val json = JSONArray()
    cards.forEach { json.put(it.toJson()) }
    return json
}

private fun JSONArray.toPlayerCards(): List<PlayerCard> = buildList {
    for (index in 0 until length()) {
        optJSONObject(index)?.let { playerCardFromJson(it)?.let(::add) }
    }
}

private fun EliminationRecord.toJson(): JSONObject = JSONObject().apply {
    put("round", round)
    put("playerName", playerName)
    putNullableString("note", note)
}

private fun eliminationRecordFromJson(json: JSONObject): EliminationRecord? {
    val playerName = json.optString("playerName").takeIf { it.isNotBlank() } ?: return null
    return EliminationRecord(
        round = json.optInt("round", 1),
        playerName = playerName,
        note = json.optNullableString("note"),
    )
}

private fun eliminationRecordsToJsonArray(records: List<EliminationRecord>): JSONArray {
    val json = JSONArray()
    records.forEach { json.put(it.toJson()) }
    return json
}

private fun JSONArray.toEliminationRecords(): List<EliminationRecord> = buildList {
    for (index in 0 until length()) {
        optJSONObject(index)?.let { eliminationRecordFromJson(it)?.let(::add) }
    }
}

private fun GameOutcome.toJson(): JSONObject = JSONObject().apply {
    put("title", title)
    put("summary", summary)
    put("reason", reason)
}

private fun ClocktowerEvent.toJson(): JSONObject = JSONObject().apply {
    put("sequence", sequence)
    put("type", type.name)
    put("title", title)
    put("detail", detail)
    put("playerNames", stringsToJsonArray(playerNames))
    put("phase", phase.name)
    put("round", round)
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

private fun clocktowerEventsToJsonArray(events: List<ClocktowerEvent>): JSONArray {
    val json = JSONArray()
    events.forEach { json.put(it.toJson()) }
    return json
}

private fun JSONArray.toClocktowerEvents(): List<ClocktowerEvent> = buildList {
    for (index in 0 until length()) {
        optJSONObject(index)?.let { clocktowerEventFromJson(it)?.let(::add) }
    }
}

private fun recordedEpistemicObservationsToJsonArray(records: List<RecordedEpistemicObservation>): JSONArray = JSONArray().apply {
    records.forEach { put(JSONObject(EpistemicSemanticJson.encode(it))) }
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

private fun savedGamePreviewFromJson(context: Context, json: JSONObject): SavedGamePreview? {
    if (!ActiveGamePersistenceCoordinator.isSupportedVersion(json.optInt("version", 0))) return null
    val gameKind = enumByName<GameKind>(json.optNullableString("currentGameKind")) ?: return null
    val screen = enumByName<Screen>(json.optNullableString("screen")) ?: return null
    val playerCount = json.optJSONArray("cards")?.length() ?: 0
    if (playerCount == 0) return null
    val round = json.optInt("round", 1)
    val gameName = when (gameKind) {
        GameKind.Undercover -> context.getString(R.string.game_who_is_undercover)
        GameKind.Werewolf -> context.getString(R.string.game_werewolf)
        GameKind.Clocktower -> context.getString(R.string.game_clocktower)
    }
    val stage = when {
        json.optBoolean("showResults", false) || json.optJSONObject("gameOutcome") != null ->
            context.getString(R.string.saved_game_stage_results)
        screen == Screen.PassPhone || screen == Screen.RevealCard ->
            context.getString(R.string.saved_game_stage_dealing)
        gameKind == GameKind.Clocktower -> {
            when (enumByName<ClocktowerPhase>(json.optNullableString("clocktowerPhase")) ?: ClocktowerPhase.FirstNight) {
                ClocktowerPhase.FirstNight -> context.getString(R.string.clocktower_phase_first_night)
                ClocktowerPhase.Dawn -> context.getString(R.string.saved_game_stage_dawn)
                ClocktowerPhase.Day -> context.getString(R.string.clocktower_phase_day, round)
                ClocktowerPhase.Night -> context.getString(R.string.clocktower_phase_night, round)
            }
        }
        else -> context.getString(R.string.round_format, round)
    }
    return SavedGamePreview(
        title = context.getString(R.string.resume_saved_game),
        subtitle = context.getString(R.string.saved_game_summary_format, gameName, stage, playerCount),
        savedAtLabel = json.optLong("savedAtMillis", 0L)
            .takeIf { it > 0L }
            ?.let { savedAtMillis ->
                val locale = context.resources.configuration.locales[0]
                val pattern = if (locale.language == "en") "MMM d, HH:mm" else "M月d日 HH:mm"
                java.text.SimpleDateFormat(pattern, locale).format(java.util.Date(savedAtMillis))
            },
    )
}

private fun Role.labelResId(): Int = when (this) {
    Role.Civilian -> R.string.role_civilian
    Role.Undercover -> R.string.role_undercover
    Role.Blank -> R.string.role_blank
    Role.Villager -> R.string.role_villager
    Role.Werewolf -> R.string.role_werewolf
    Role.Seer -> R.string.role_seer
    Role.Witch -> R.string.role_witch
    Role.Hunter -> R.string.role_hunter
}

private fun LanguageMode.labelResId(): Int = when (this) {
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
    ClocktowerRole(ClocktowerTeam.Townsfolk, "杀手", "Slayer", "每局一次，白天选择一名玩家；若其是恶魔，该玩家死亡。", "Once per game during the day, choose a player: if they are the Demon, they die."),
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

private fun ClocktowerRole.descriptionFor(language: String): String = if (language == "en") enDescription else zhDescription

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
    val activeGamePersistenceCoordinator = remember(baseContext) {
        ActiveGamePersistenceCoordinator.fromContext(baseContext)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }
    var storytellerAutomationMode by remember { mutableStateOf(baseContext.loadStorytellerAutomationMode()) }
    val automaticStorytellerInfo = storytellerAutomationMode.isAutomatic
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
    var clocktowerDemonSuccessorTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerVirginUsed by remember { mutableStateOf(false) }
    var clocktowerSlayerUsed by remember { mutableStateOf(false) }
    var clocktowerSlayerClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerArtistUsed by remember { mutableStateOf(false) }
    var clocktowerArtistClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerArtistClaimantName by remember { mutableStateOf<String?>(null) }
    var clocktowerArtistTruthfulAnswer by remember { mutableStateOf<Boolean?>(null) }
    var clocktowerArtistShownAnswer by remember { mutableStateOf<Boolean?>(null) }
    var clocktowerLastExecutedName by remember { mutableStateOf<String?>(null) }
    var clocktowerPendingKlutzName by remember { mutableStateOf<String?>(null) }
    var clocktowerKlutzChoiceName by remember { mutableStateOf<String?>(null) }
    var clocktowerKlutzReturnToDawn by remember { mutableStateOf(false) }
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var currentClocktowerScript by remember { mutableStateOf(ClocktowerScript.TroubleBrewing) }
    var clocktowerGameId by remember { mutableStateOf("") }
    var clocktowerGameSeed by remember { mutableStateOf(0L) }
    var clocktowerGameStateRevision by remember { mutableStateOf(0L) }
    var clocktowerPlayerInputRevision by remember { mutableStateOf(0L) }
    var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
    var clocktowerRulesetRoleIds by remember { mutableStateOf<Set<RoleId>>(emptySet()) }
    var showResults by remember { mutableStateOf(false) }
    var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }
    var newCommonPlayerName by remember { mutableStateOf("") }
    val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }
    val playerNames = remember { mutableStateListOf<String>() }
    val cards = remember { mutableStateListOf<PlayerCard>() }
    val records = remember { mutableStateListOf<EliminationRecord>() }
    val clocktowerEvents = remember { mutableStateListOf<ClocktowerEvent>() }
    val clocktowerEpistemicObservations = remember { mutableStateListOf<RecordedEpistemicObservation>() }
    var clocktowerEventCounter by remember { mutableStateOf(0) }
    val clocktowerNightStartedState = remember { mutableStateOf(false) }
    val clocktowerNightStepIndexState = remember { mutableStateOf(0) }
    val clocktowerDayModeState = remember { mutableStateOf(ClocktowerDayMode.Overview) }
    val clocktowerNominatorNameState = remember { mutableStateOf<String?>(null) }
    val clocktowerNomineeNameState = remember { mutableStateOf<String?>(null) }
    val clocktowerCurrentVoteCountState = remember { mutableStateOf(0) }
    val clocktowerHighestVoteNameState = remember { mutableStateOf<String?>(null) }
    val clocktowerHighestVoteCountState = remember { mutableStateOf(0) }
    val clocktowerSlayerClaimantNameState = remember { mutableStateOf<String?>(null) }
    val clocktowerSlayerTargetNameState = remember { mutableStateOf<String?>(null) }
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

    fun advanceClocktowerGameStateRevision() {
        clocktowerGameStateRevision = clocktowerGameStateRevision + 1
        invalidateA4RevisionScope()
    }

    fun advanceClocktowerPlayerInputRevision() {
        clocktowerPlayerInputRevision = clocktowerPlayerInputRevision + 1
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

    fun addClocktowerEvent(
        type: ClocktowerEventType,
        title: String,
        detail: String,
        playerNames: List<String> = emptyList(),
        eventPhase: ClocktowerPhase = clocktowerPhase,
        eventRound: Int = round,
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
        if (type !in setOf(ClocktowerEventType.Death, ClocktowerEventType.Execution)) return
        val eliminatedSeats = playerNames.mapNotNull { playerName ->
            cards.indexOfFirst { it.name == playerName }
                .takeIf { index -> index >= 0 && cards[index].eliminatedRound != null }
                ?.plus(1)
        }.distinct()
        if (eliminatedSeats.isEmpty()) return
        val epistemicPhase = when (eventPhase) {
            ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
            ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
            ClocktowerPhase.Day -> StorytellerPhase.DAY
            ClocktowerPhase.Night -> StorytellerPhase.NIGHT
        }
        val appendedObservationIds = mutableListOf<String>()
        eliminatedSeats.forEach { seat ->
            val observationId = "public-alive-${clocktowerGameId}-${clocktowerEventCounter}-$seat"
            clocktowerEpistemicObservations += RecordedEpistemicObservation(
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
            )
            appendedObservationIds += observationId
        }
        advanceClocktowerPlayerInputRevision()
        appendedObservationIds.lastOrNull()?.let { recordId ->
            a4ObservationDurabilityGate.markPending(recordId)
        }
    }

    fun recordEpistemicObservation(record: RecordedEpistemicObservation) {
        if (clocktowerEpistemicObservations.any { it.recordId == record.recordId }) return
        clocktowerEpistemicObservations += record
        advanceClocktowerPlayerInputRevision()
        a4ObservationDurabilityGate.markPending(record.recordId)
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
        clocktowerNominatorNameState.value = null
        clocktowerNomineeNameState.value = null
        clocktowerCurrentVoteCountState.value = 0
        clocktowerHighestVoteNameState.value = null
        clocktowerHighestVoteCountState.value = 0
        clocktowerSlayerClaimantNameState.value = null
        clocktowerSlayerTargetNameState.value = null
        clocktowerArtistClaimantName = null
        clocktowerArtistTruthfulAnswer = null
        clocktowerArtistShownAnswer = null
    }

    fun resetClocktowerFlow() {
        resetClocktowerNightFlow()
        resetClocktowerDayFlow()
    }

    fun clearSavedGameState() {
        baseContext.clearActiveGameState()
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

    fun activeGameSnapshotJson(): JSONObject = JSONObject().apply {
        put("version", ACTIVE_GAME_STATE_VERSION)
        put("savedAtMillis", System.currentTimeMillis())
        put("screen", screen.name)
        put("currentGameKind", currentGameKind.name)
        val gameContentIdentity = activeGamePersistenceCoordinator.identityForSave(
            ActiveGamePersistenceInputs(
                gameKind = currentGameKind,
                clocktowerScript = currentClocktowerScript,
                assignedClocktowerRoleIds = if (currentGameKind == GameKind.Clocktower) {
                    cards.map { card ->
                        RoleId(requireNotNull(card.clocktowerRole) {
                            "Clocktower active save is missing an assigned role."
                        }.enName)
                    }
                } else {
                    emptyList()
                },
                assignedWerewolfRoles = if (currentGameKind == GameKind.Werewolf) {
                    cards.map { it.role }
                } else {
                    emptyList()
                },
                werewolfCount = werewolfCount,
                includeSeer = includeSeer,
                includeWitch = includeWitch,
                includeHunter = includeHunter,
                lastWordsMode = lastWordsMode,
            ),
        )
        put(
            PersistedActiveGameIdentityJsonCodec.ROOT_KEY,
            PersistedActiveGameIdentityJsonCodec.encode(gameContentIdentity),
        )
        put("undercoverCount", undercoverCount)
        put("includeBlank", includeBlank)
        put("werewolfCount", werewolfCount)
        put("includeSeer", includeSeer)
        put("includeWitch", includeWitch)
        put("includeHunter", includeHunter)
        put("lastWordsMode", lastWordsMode.name)
        put("lastWordsPromptNames", stringsToJsonArray(lastWordsPromptNames))
        put("currentDealIndex", currentDealIndex)
        put("round", round)
        putNullableString("selectedElimination", selectedElimination)
        put("werewolfJudgeStepIndex", werewolfJudgeStepIndex)
        putNullableString("pendingNightDeath", pendingNightDeath)
        putNullableString("seerCheckTarget", seerCheckTarget)
        put("witchSaveUsed", witchSaveUsed)
        put("witchPoisonUsed", witchPoisonUsed)
        put("witchSavedTonight", witchSavedTonight)
        putNullableString("witchPoisonTarget", witchPoisonTarget)
        putNullableString("hunterShotTarget", hunterShotTarget)
        putNullableString("selectedDayExile", selectedDayExile)
        put("clocktowerPhase", clocktowerPhase.name)
        put("currentClocktowerScript", currentClocktowerScript.name)
        put("clocktowerGameId", clocktowerGameId)
        put("clocktowerGameSeed", clocktowerGameSeed)
        put("clocktowerGameStateRevision", clocktowerGameStateRevision)
        put("clocktowerPlayerInputRevision", clocktowerPlayerInputRevision)
        if (currentGameKind == GameKind.Clocktower &&
            currentClocktowerScript == ClocktowerScript.TroubleBrewing
        ) {
            put(
                "clocktowerRulesetRoleIds",
                ClocktowerRulesetPersistenceBasisJsonCodec.encode(
                    ClocktowerRulesetPersistenceBasis(clocktowerRulesetRoleIds),
                ),
            )
        } else {
            put("clocktowerRulesetRoleIds", JSONObject.NULL)
        }
        if (clocktowerRulesetRef == null) {
            put("clocktowerRulesetRef", JSONObject.NULL)
        } else {
            put("clocktowerRulesetRef", JSONObject().apply {
                put("scriptId", clocktowerRulesetRef!!.scriptId.value)
                put("scriptContentHash", clocktowerRulesetRef!!.scriptContentHash)
                put("rulesetVersion", clocktowerRulesetRef!!.rulesetVersion)
                put("sourceRevision", clocktowerRulesetRef!!.sourceRevision)
                put("coverage", clocktowerRulesetRef!!.coverage.name)
            })
        }
        putNullableString("clocktowerPendingNightDeath", clocktowerPendingNightDeath)
        putNullableString("clocktowerDemonAttackDraftTarget", clocktowerDemonAttackDraftTarget)
        putNullableString("clocktowerSelectedExecution", clocktowerSelectedExecution)
        putNullableString("clocktowerPoisonTarget", clocktowerPoisonTarget)
        putNullableString("clocktowerConfirmedPoisonTarget", clocktowerConfirmedPoisonTarget)
        putNullableString("clocktowerFortuneTellerFirst", clocktowerFortuneTellerFirst)
        putNullableString("clocktowerFortuneTellerSecond", clocktowerFortuneTellerSecond)
        putNullableString("clocktowerChambermaidFirst", clocktowerChambermaidFirst)
        putNullableString("clocktowerChambermaidSecond", clocktowerChambermaidSecond)
        putNullableString("clocktowerRavenkeeperTarget", clocktowerRavenkeeperTarget)
        putNullableString("clocktowerRedHerring", clocktowerRedHerring)
        put("clocktowerRecommendedDemonBluffRoleNames", stringsToJsonArray(clocktowerRecommendedDemonBluffRoleNames))
        putNullableString("clocktowerRecommendedDrunkInvestigatorRoleName", clocktowerRecommendedDrunkInvestigatorRoleName)
        put("clocktowerRecommendedDrunkInvestigatorSeats", JSONArray(clocktowerRecommendedDrunkInvestigatorSeats))
        putNullableString("clocktowerButlerMaster", clocktowerButlerMaster)
        putNullableString("clocktowerMonkProtectedTarget", clocktowerMonkProtectedTarget)
        putNullableString("clocktowerConfirmedMonkProtectedTarget", clocktowerConfirmedMonkProtectedTarget)
        putNullableString("clocktowerMayorRedirectTarget", clocktowerMayorRedirectTarget)
        putNullableString("clocktowerConfirmedMayorRedirectTarget", clocktowerConfirmedMayorRedirectTarget)
        putNullableString("clocktowerPendingNewDemonName", clocktowerPendingNewDemonName)
        putNullableString("clocktowerDemonSuccessorTarget", clocktowerDemonSuccessorTarget)
        // Store the unfinished-night continuation as one checkpoint as well as
        // the legacy flat keys above. This keeps old saves compatible while
        // making draft/confirmed restoration an explicit tested boundary.
        ClocktowerNightCheckpoint(
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
            demonSuccessorDraftTarget = clocktowerDemonSuccessorTarget,
        ).persistedValues().forEach { (key, value) -> put(key, value ?: JSONObject.NULL) }
        put("clocktowerVirginUsed", clocktowerVirginUsed)
        put("clocktowerSlayerUsed", clocktowerSlayerUsed)
        put("clocktowerSlayerClaimedNames", stringsToJsonArray(clocktowerSlayerClaimedNames))
        put("clocktowerArtistUsed", clocktowerArtistUsed)
        put("clocktowerArtistClaimedNames", stringsToJsonArray(clocktowerArtistClaimedNames))
        putNullableString("clocktowerArtistClaimantName", clocktowerArtistClaimantName)
        putNullableBoolean("clocktowerArtistTruthfulAnswer", clocktowerArtistTruthfulAnswer)
        putNullableBoolean("clocktowerArtistShownAnswer", clocktowerArtistShownAnswer)
        putNullableString("clocktowerLastExecutedName", clocktowerLastExecutedName)
        putNullableString("clocktowerPendingKlutzName", clocktowerPendingKlutzName)
        putNullableString("clocktowerKlutzChoiceName", clocktowerKlutzChoiceName)
        put("clocktowerKlutzReturnToDawn", clocktowerKlutzReturnToDawn)
        put("clocktowerNightStarted", clocktowerNightStartedState.value)
        put("clocktowerNightStepIndex", clocktowerNightStepIndexState.value)
        put("clocktowerDayMode", clocktowerDayModeState.value.name)
        putNullableString("clocktowerNominatorName", clocktowerNominatorNameState.value)
        putNullableString("clocktowerNomineeName", clocktowerNomineeNameState.value)
        put("clocktowerCurrentVoteCount", clocktowerCurrentVoteCountState.value)
        putNullableString("clocktowerHighestVoteName", clocktowerHighestVoteNameState.value)
        put("clocktowerHighestVoteCount", clocktowerHighestVoteCountState.value)
        putNullableString("clocktowerSlayerClaimantName", clocktowerSlayerClaimantNameState.value)
        putNullableString("clocktowerSlayerTargetName", clocktowerSlayerTargetNameState.value)
        put("showResults", showResults)
        if (gameOutcome == null) {
            put("gameOutcome", JSONObject.NULL)
        } else {
            put("gameOutcome", gameOutcome!!.toJson())
        }
        put("playerNames", stringsToJsonArray(playerNames))
        put("cards", playerCardsToJsonArray(cards))
        put("records", eliminationRecordsToJsonArray(records))
        put("clocktowerEventCounter", clocktowerEventCounter)
        put("clocktowerEvents", clocktowerEventsToJsonArray(clocktowerEvents))
        put("clocktowerEpistemicObservations", recordedEpistemicObservationsToJsonArray(clocktowerEpistemicObservations))
    }

    fun persistActiveGameStateIfNeeded(): Boolean {
        if (!screen.isActiveGameScreen() || cards.isEmpty()) return false
        return baseContext.saveActiveGameState(activeGameSnapshotJson())
    }

    fun persistAndReleaseA4ObservationRebuildIfDurable() {
        val persisted = persistActiveGameStateIfNeeded()
        val recordId = a4ObservationDurabilityGate.releaseAfterPersistence(persisted) ?: return
        a4ObservationCacheRebuildRequest = a4ObservationCacheRebuildRequestOrNull(recordId)
    }

    fun restoreSavedGame() {
        val json = baseContext.loadActiveGameStateJson() ?: return
        invalidateA4SessionBoundary()
        val restored = runCatching {
            if (!ActiveGamePersistenceCoordinator.isSupportedVersion(json.optInt("version", 0))) {
                error("Unsupported active game state version")
            }
            val restoredGameKind = enumByName<GameKind>(json.optNullableString("currentGameKind"))
                ?: error("Missing game kind")
            val restoredCards = json.optJSONArray("cards")?.toPlayerCards().orEmpty()
            if (restoredCards.isEmpty()) error("Missing player cards")
            val restoredPersistence = activeGamePersistenceCoordinator.resolveForRestore(
                json = json,
                gameKind = restoredGameKind,
                assignedClocktowerRoleIds = if (restoredGameKind == GameKind.Clocktower) {
                    restoredCards.map { card ->
                        RoleId(requireNotNull(card.clocktowerRole) {
                            "Clocktower restored save is missing an assigned role."
                        }.enName)
                    }
                } else {
                    emptyList()
                },
                assignedWerewolfRoles = if (restoredGameKind == GameKind.Werewolf) {
                    restoredCards.map { it.role }
                } else {
                    emptyList()
                },
            )
            val restoredClocktowerRulesetRef = json.opt("clocktowerRulesetRef")
                .takeUnless { raw -> raw == null || raw == JSONObject.NULL }
                ?.let { raw ->
                    val ref = raw as? JSONObject
                        ?: error("Invalid Clocktower ruleset reference payload.")
                    RulesetRef(
                        scriptId = ScriptId(ref.getString("scriptId")),
                        scriptContentHash = ref.getString("scriptContentHash"),
                        rulesetVersion = ref.getString("rulesetVersion"),
                        sourceRevision = ref.getString("sourceRevision"),
                        coverage = enumByName<RuleCoverage>(ref.getString("coverage"))
                            ?: error("Invalid Clocktower ruleset coverage."),
                    )
                }
            val restoredRulesetBasis = if (
                restoredGameKind == GameKind.Clocktower &&
                restoredPersistence.clocktowerScript == ClocktowerScript.TroubleBrewing
            ) {
                when (json.optInt("version", 0)) {
                    ActiveGamePersistenceCoordinator.LEGACY_VERSION ->
                        TroubleBrewingRulesetPersistence.resolveLegacyBasisForRestore(
                            knowledge = troubleBrewingRulesetKnowledge()
                                ?: error("Unable to resolve current Trouble Brewing ruleset knowledge."),
                            assignedRoleIds = restoredCards.map { card ->
                                RoleId(requireNotNull(card.clocktowerRole) {
                                    "Legacy Clocktower save is missing an assigned role."
                                }.enName)
                            },
                            persistedRef = restoredClocktowerRulesetRef,
                        )
                    ActiveGamePersistenceCoordinator.CURRENT_VERSION ->
                        ClocktowerRulesetPersistenceBasisJsonCodec.decode(
                            json.optJSONArray("clocktowerRulesetRoleIds")
                                ?: error("Version 2 Clocktower save is missing ruleset role basis."),
                        )
                    else -> error("Unsupported active game state version")
                }
            } else {
                null
            }
            val resolvedClocktowerRulesetRef = if (
                restoredGameKind == GameKind.Clocktower &&
                restoredPersistence.clocktowerScript == ClocktowerScript.TroubleBrewing
            ) {
                TroubleBrewingRulesetPersistence.resolveForRestore(
                    knowledge = troubleBrewingRulesetKnowledge()
                        ?: error("Unable to resolve current Trouble Brewing ruleset knowledge."),
                    persistedRef = restoredClocktowerRulesetRef,
                    basis = requireNotNull(restoredRulesetBasis),
                    allowLegacyFallback = restoredPersistence.allowLegacyClocktowerRulesetFallback,
                )
            } else {
                restoredClocktowerRulesetRef
            }
            val localizedRestoredCards = restoredCards.map(::localizedRestoredCard)
            val restoredScreen = enumByName<Screen>(json.optNullableString("screen"))
                ?.takeIf { it.isActiveGameScreen() }
                ?: when (restoredGameKind) {
                    GameKind.Undercover -> Screen.Game
                    GameKind.Werewolf -> Screen.WerewolfJudge
                    GameKind.Clocktower -> Screen.ClocktowerJudge
                }
            val restoredPlayerNames = json.optJSONArray("playerNames")
                ?.toStringList()
                .orEmpty()
                .ifEmpty { localizedRestoredCards.map { it.name } }

            playerNames.clear()
            playerNames.addAll(restoredPlayerNames)
            cards.clear()
            cards.addAll(localizedRestoredCards)
            records.clear()
            records.addAll(json.optJSONArray("records")?.toEliminationRecords().orEmpty())
            clocktowerEvents.clear()
            clocktowerEvents.addAll(json.optJSONArray("clocktowerEvents")?.toClocktowerEvents().orEmpty())
            clocktowerEpistemicObservations.clear()
            clocktowerEpistemicObservations.addAll(
                json.optJSONArray("clocktowerEpistemicObservations")?.toRecordedEpistemicObservations().orEmpty(),
            )
            clocktowerEventCounter = maxOf(
                json.optInt("clocktowerEventCounter", 0),
                clocktowerEvents.maxOfOrNull { it.sequence } ?: 0,
            )

            currentGameKind = restoredGameKind
            undercoverCount = json.optInt("undercoverCount", 1).coerceAtLeast(1)
            includeBlank = json.optBoolean("includeBlank", false)
            werewolfCount = json.optInt("werewolfCount", 1).coerceAtLeast(1)
            includeSeer = json.optBoolean("includeSeer", true)
            includeWitch = json.optBoolean("includeWitch", false)
            includeHunter = json.optBoolean("includeHunter", false)
            lastWordsMode = enumByName<LastWordsMode>(json.optNullableString("lastWordsMode")) ?: LastWordsMode.FirstDay
            lastWordsPromptNames = json.optJSONArray("lastWordsPromptNames")?.toStringList().orEmpty()
            currentDealIndex = json.optInt("currentDealIndex", 0).coerceIn(0, localizedRestoredCards.lastIndex)
            round = json.optInt("round", 1).coerceAtLeast(1)
            selectedElimination = json.optNullableString("selectedElimination")
            werewolfJudgeStepIndex = json.optInt("werewolfJudgeStepIndex", 0).coerceAtLeast(0)
            pendingNightDeath = json.optNullableString("pendingNightDeath")
            seerCheckTarget = json.optNullableString("seerCheckTarget")
            witchSaveUsed = json.optBoolean("witchSaveUsed", false)
            witchPoisonUsed = json.optBoolean("witchPoisonUsed", false)
            witchSavedTonight = json.optBoolean("witchSavedTonight", false)
            witchPoisonTarget = json.optNullableString("witchPoisonTarget")
            hunterShotTarget = json.optNullableString("hunterShotTarget")
            selectedDayExile = json.optNullableString("selectedDayExile")
            clocktowerPhase = enumByName<ClocktowerPhase>(json.optNullableString("clocktowerPhase")) ?: ClocktowerPhase.FirstNight
            if (restoredGameKind == GameKind.Clocktower) {
                currentClocktowerScript = requireNotNull(restoredPersistence.clocktowerScript)
            }
            clocktowerGameId = json.optString("clocktowerGameId")
                .takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()
            clocktowerGameSeed = if (json.has("clocktowerGameSeed")) {
                json.optLong("clocktowerGameSeed")
            } else {
                newClocktowerSeed()
            }
            clocktowerGameStateRevision = json.optLong("clocktowerGameStateRevision", 0L).coerceAtLeast(0L)
            clocktowerPlayerInputRevision = json.optLong("clocktowerPlayerInputRevision", 0L).coerceAtLeast(0L)
            clocktowerRulesetRoleIds = restoredRulesetBasis?.roleIds.orEmpty()
            clocktowerRulesetRef = resolvedClocktowerRulesetRef
            clocktowerPendingNightDeath = json.optNullableString("clocktowerPendingNightDeath")
            clocktowerDemonAttackDraftTarget = json.optNullableString("clocktowerDemonAttackDraftTarget")
                ?: clocktowerPendingNightDeath
            clocktowerSelectedExecution = json.optNullableString("clocktowerSelectedExecution")
            clocktowerPoisonTarget = json.optNullableString("clocktowerPoisonTarget")
            clocktowerConfirmedPoisonTarget = json.optNullableString("clocktowerConfirmedPoisonTarget")
                ?: clocktowerPoisonTarget
            clocktowerFortuneTellerFirst = json.optNullableString("clocktowerFortuneTellerFirst")
            clocktowerFortuneTellerSecond = json.optNullableString("clocktowerFortuneTellerSecond")
            clocktowerChambermaidFirst = json.optNullableString("clocktowerChambermaidFirst")
            clocktowerChambermaidSecond = json.optNullableString("clocktowerChambermaidSecond")
            clocktowerRavenkeeperTarget = json.optNullableString("clocktowerRavenkeeperTarget")
            clocktowerRedHerring = json.optNullableString("clocktowerRedHerring")
            clocktowerRecommendedDemonBluffRoleNames = json
                .optJSONArray("clocktowerRecommendedDemonBluffRoleNames")
                ?.toStringList()
                .orEmpty()
            clocktowerRecommendedDrunkInvestigatorRoleName = json.optNullableString("clocktowerRecommendedDrunkInvestigatorRoleName")
            clocktowerRecommendedDrunkInvestigatorSeats = json
                .optJSONArray("clocktowerRecommendedDrunkInvestigatorSeats")
                ?.let { seats -> (0 until seats.length()).map { index -> seats.optInt(index) }.filter { it > 0 } }
                .orEmpty()
            clocktowerButlerMaster = json.optNullableString("clocktowerButlerMaster")
            clocktowerMonkProtectedTarget = json.optNullableString("clocktowerMonkProtectedTarget")
            clocktowerConfirmedMonkProtectedTarget = json.optNullableString("clocktowerConfirmedMonkProtectedTarget")
                ?: clocktowerMonkProtectedTarget
            clocktowerMayorRedirectTarget = json.optNullableString("clocktowerMayorRedirectTarget")
            clocktowerConfirmedMayorRedirectTarget = json.optNullableString("clocktowerConfirmedMayorRedirectTarget")
                ?: clocktowerMayorRedirectTarget
            clocktowerPendingNewDemonName = json.optNullableString("clocktowerPendingNewDemonName")
            clocktowerDemonSuccessorTarget = json.optNullableString("clocktowerDemonSuccessorTarget")
            val restoredNightCheckpoint = ClocktowerNightCheckpoint.fromPersistedValues(mapOf(
                "clocktowerPhase" to json.optNullableString("clocktowerPhase"),
                "round" to json.optInt("round", 1),
                "clocktowerGameStateRevision" to json.optLong("clocktowerGameStateRevision", 0L),
                "clocktowerPlayerInputRevision" to json.optLong("clocktowerPlayerInputRevision", 0L),
                "clocktowerNightStarted" to json.optBoolean("clocktowerNightStarted", false),
                "clocktowerNightStepIndex" to json.optInt("clocktowerNightStepIndex", 0),
                "clocktowerPendingNightDeath" to json.optNullableString("clocktowerPendingNightDeath"),
                "clocktowerDemonAttackDraftTarget" to json.optNullableString("clocktowerDemonAttackDraftTarget"),
                "clocktowerConfirmedPoisonTarget" to json.optNullableString("clocktowerConfirmedPoisonTarget"),
                "clocktowerPoisonTarget" to json.optNullableString("clocktowerPoisonTarget"),
                "clocktowerConfirmedMonkProtectedTarget" to json.optNullableString("clocktowerConfirmedMonkProtectedTarget"),
                "clocktowerMonkProtectedTarget" to json.optNullableString("clocktowerMonkProtectedTarget"),
                "clocktowerConfirmedMayorRedirectTarget" to json.optNullableString("clocktowerConfirmedMayorRedirectTarget"),
                "clocktowerMayorRedirectTarget" to json.optNullableString("clocktowerMayorRedirectTarget"),
                "clocktowerPendingNewDemonName" to json.optNullableString("clocktowerPendingNewDemonName"),
                "clocktowerDemonSuccessorTarget" to json.optNullableString("clocktowerDemonSuccessorTarget"),
            ))
            clocktowerPhase = enumByName<ClocktowerPhase>(restoredNightCheckpoint.phaseName) ?: ClocktowerPhase.FirstNight
            round = restoredNightCheckpoint.round
            clocktowerGameStateRevision = restoredNightCheckpoint.gameStateRevision
            clocktowerPlayerInputRevision = restoredNightCheckpoint.playerInputRevision
            clocktowerNightStartedState.value = restoredNightCheckpoint.nightStarted
            clocktowerNightStepIndexState.value = restoredNightCheckpoint.nightStepIndex
            clocktowerPendingNightDeath = restoredNightCheckpoint.confirmedAttackTarget
            clocktowerDemonAttackDraftTarget = restoredNightCheckpoint.attackDraftTarget
            clocktowerConfirmedPoisonTarget = restoredNightCheckpoint.confirmedPoisonTarget
            clocktowerPoisonTarget = restoredNightCheckpoint.poisonDraftTarget
            clocktowerConfirmedMonkProtectedTarget = restoredNightCheckpoint.confirmedMonkTarget
            clocktowerMonkProtectedTarget = restoredNightCheckpoint.monkDraftTarget
            clocktowerConfirmedMayorRedirectTarget = restoredNightCheckpoint.confirmedMayorRedirectTarget
            clocktowerMayorRedirectTarget = restoredNightCheckpoint.mayorRedirectDraftTarget
            clocktowerPendingNewDemonName = restoredNightCheckpoint.pendingNewDemonName
            clocktowerDemonSuccessorTarget = restoredNightCheckpoint.demonSuccessorDraftTarget
            clocktowerVirginUsed = json.optBoolean("clocktowerVirginUsed", false)
            clocktowerSlayerUsed = json.optBoolean("clocktowerSlayerUsed", false)
            clocktowerSlayerClaimedNames = json.optJSONArray("clocktowerSlayerClaimedNames")?.toStringList().orEmpty()
            clocktowerArtistUsed = json.optBoolean("clocktowerArtistUsed", false)
            clocktowerArtistClaimedNames = json.optJSONArray("clocktowerArtistClaimedNames")?.toStringList().orEmpty()
            clocktowerArtistClaimantName = json.optNullableString("clocktowerArtistClaimantName")
            clocktowerArtistTruthfulAnswer = json.optNullableBoolean("clocktowerArtistTruthfulAnswer")
            clocktowerArtistShownAnswer = json.optNullableBoolean("clocktowerArtistShownAnswer")
            clocktowerLastExecutedName = json.optNullableString("clocktowerLastExecutedName")
            clocktowerPendingKlutzName = json.optNullableString("clocktowerPendingKlutzName")
            clocktowerKlutzChoiceName = json.optNullableString("clocktowerKlutzChoiceName")
            clocktowerKlutzReturnToDawn = json.optBoolean("clocktowerKlutzReturnToDawn", false)
            clocktowerNightStartedState.value = json.optBoolean("clocktowerNightStarted", false)
            clocktowerNightStepIndexState.value = json.optInt("clocktowerNightStepIndex", 0).coerceAtLeast(0)
            clocktowerDayModeState.value = enumByName<ClocktowerDayMode>(json.optNullableString("clocktowerDayMode"))
                ?: ClocktowerDayMode.Overview
            clocktowerNominatorNameState.value = json.optNullableString("clocktowerNominatorName")
            clocktowerNomineeNameState.value = json.optNullableString("clocktowerNomineeName")
            clocktowerCurrentVoteCountState.value = json.optInt("clocktowerCurrentVoteCount", 0).coerceAtLeast(0)
            clocktowerHighestVoteNameState.value = json.optNullableString("clocktowerHighestVoteName")
            clocktowerHighestVoteCountState.value = json.optInt("clocktowerHighestVoteCount", 0).coerceAtLeast(0)
            clocktowerSlayerClaimantNameState.value = json.optNullableString("clocktowerSlayerClaimantName")
            clocktowerSlayerTargetNameState.value = json.optNullableString("clocktowerSlayerTargetName")
            gameOutcome = gameOutcomeFromJson(json.optJSONObject("gameOutcome"))
            showResults = json.optBoolean("showResults", false)
            screen = restoredScreen
        }
        if (restored.isFailure) {
            clearSavedGameState()
        }
    }

    val latestPersistActiveGameState by rememberUpdatedState { persistAndReleaseA4ObservationRebuildIfDurable() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                latestPersistActiveGameState()
            }
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

    fun moveCurrentPlayerTo(index: Int, insertIndex: Int) {
        if (index !in playerNames.indices) return
        val name = playerNames.removeAt(index)
        val adjustedIndex = if (insertIndex > index) insertIndex - 1 else insertIndex
        playerNames.add(adjustedIndex.coerceIn(0, playerNames.size), name)
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
        preparedSetupPlan: RecommendationPlan? = null,
    ) {
        invalidateA4SessionBoundary()
        clearSavedGameState()
        currentGameKind = nextGameKind
        records.clear()
        clocktowerEvents.clear()
        clocktowerEpistemicObservations.clear()
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
        currentClocktowerScript = clocktowerScript
        if (nextGameKind == GameKind.Clocktower) {
            clocktowerGameId = UUID.randomUUID().toString()
            clocktowerGameSeed = preparedClocktowerSeed ?: newClocktowerSeed()
            clocktowerGameStateRevision = 0L
            clocktowerPlayerInputRevision = 0L
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
            clocktowerGameId = ""
            clocktowerGameSeed = 0L
            clocktowerGameStateRevision = 0L
            clocktowerPlayerInputRevision = 0L
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
        clocktowerDemonSuccessorTarget = null
        clocktowerVirginUsed = false
        clocktowerSlayerUsed = false
        clocktowerSlayerClaimedNames = emptyList()
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerArtistClaimantName = null
        clocktowerArtistTruthfulAnswer = null
        clocktowerArtistShownAnswer = null
        clocktowerLastExecutedName = null
        clocktowerPendingKlutzName = null
        clocktowerKlutzChoiceName = null
        clocktowerKlutzReturnToDawn = false
        resetClocktowerFlow()
        screen = Screen.PassPhone
        persistActiveGameStateIfNeeded()
    }

    fun startUndercoverGame() {
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

    fun startClocktowerGame() {
        if (playerNames.size < MIN_CLOCKTOWER_PLAYERS) return
        val script = if (playerNames.size in 5..6) {
            selectedClocktowerScript ?: defaultClocktowerScriptFor(playerNames.size)
        } else {
            ClocktowerScript.TroubleBrewing
        }
        if (!canStartClocktowerScript(script)) return
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
        resetDealState(GameKind.Clocktower, script, preparedSeed, preparedSetupPlan)
    }

    fun archiveCurrentGameForRestart(): Boolean {
        if (cards.isEmpty()) return false
        invalidateA4SessionBoundary()
        gameHistory = baseContext.archiveGame(activeGameSnapshotJson())
        clearSavedGameState()
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
        clocktowerEpistemicObservations.clear()
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

    fun setClocktowerActualRole(playerName: String, nextRole: ClocktowerRole) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            advanceClocktowerGameStateRevision()
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
            advanceClocktowerGameStateRevision()
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
        preferredMinionName: String? = null,
    ): String? {
        promoteScarletWomanIfNeeded()?.let { return it }
        if (!impDeathWasSelfChosen) return null
        val imp = completeTroubleBrewingRoles.first { it.enName == "Imp" }
        val livingMinions = cards.filter {
            it.eliminatedRound == null && it.clocktowerTeam == ClocktowerTeam.Minion
        }
        val minion = livingMinions.firstOrNull { it.name == preferredMinionName }
            ?: livingMinions.firstOrNull()
            ?: return null
        setClocktowerActualRole(minion.name, imp)
        records.add(EliminationRecord(round, minion.name, context.getString(R.string.clocktower_record_imp_passed)))
        addClocktowerEvent(
            ClocktowerEventType.RoleChange,
            localizedText("角色变化", "Role changed"),
            localizedText("${playerSeatLabel(cards, minion.name)} 成为新的小恶魔。", "${playerSeatLabel(cards, minion.name)} became the new Imp."),
            listOf(minion.name),
        )
        return minion.name
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
                        onStartGame = { screen = Screen.Setup },
                        onContinueGame = ::restoreSavedGame,
                    )

                    Screen.Setup -> SetupScreen(
                    playerCount = playerCount,
                    savedGamePreview = savedGamePreview,
                    commonPlayers = commonPlayers,
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onAddTemporaryPlayer = ::addCurrentPlayer,
                    onRemoveCurrentPlayer = ::removeCurrentPlayer,
                    onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
                    onResumeSavedGame = ::restoreSavedGame,
                    onDiscardSavedGame = ::clearSavedGameState,
                    onOpenSettings = { screen = Screen.Settings },
                    onOpenUndercoverSettings = { screen = Screen.UndercoverSettings },
                    onOpenWerewolfSettings = { screen = Screen.WerewolfSettings },
                    onOpenClocktowerSettings = { screen = Screen.ClocktowerSettings },
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
                        onBack = { screen = Screen.Setup },
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
                        onBack = { screen = Screen.Setup },
                        onStart = ::startWerewolfGame,
                    )

                    Screen.ClocktowerSettings -> ClocktowerSettingsScreen(
                        playerCount = playerCount,
                        playerNames = playerNames,
                        selectedScript = selectedClocktowerScript ?: defaultClocktowerScriptFor(playerCount),
                        onScriptChange = { selectedClocktowerScript = it },
                        onBack = { screen = Screen.Setup },
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
                        automaticStorytellerStyle = storytellerAutomationMode.style ?: RecommendationStyle.BALANCED,
                        cards = cards,
                        records = records,
                        events = clocktowerEvents,
                        script = currentClocktowerScript,
                        gameId = clocktowerGameId,
                        gameSeed = clocktowerGameSeed,
                        gameStateRevision = clocktowerGameStateRevision,
                        playerInputRevision = clocktowerPlayerInputRevision,
                        rulesetRef = clocktowerRulesetRef,
                        setupHistory = gameHistory.toClocktowerSetupHistory(),
                        onInitialRecommendationDemand = recordA4InitialRecommendationDemand,
                        phase = clocktowerPhase,
                        round = round,
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
                        demonSuccessorTarget = clocktowerDemonSuccessorTarget,
                        virginUsed = clocktowerVirginUsed,
                        slayerUsed = clocktowerSlayerUsed,
                        slayerClaimedNames = clocktowerSlayerClaimedNames,
                        artistUsed = clocktowerArtistUsed,
                        artistClaimedNames = clocktowerArtistClaimedNames,
                        artistClaimantName = clocktowerArtistClaimantName,
                        artistTruthfulAnswer = clocktowerArtistTruthfulAnswer,
                        artistShownAnswer = clocktowerArtistShownAnswer,
                        lastExecutedName = clocktowerLastExecutedName,
                        pendingKlutzName = clocktowerPendingKlutzName,
                        klutzChoiceName = clocktowerKlutzChoiceName,
                        nightStartedState = clocktowerNightStartedState,
                        nightStepIndexState = clocktowerNightStepIndexState,
                        dayModeState = clocktowerDayModeState,
                        nominatorNameState = clocktowerNominatorNameState,
                        nomineeNameState = clocktowerNomineeNameState,
                        currentVoteCountState = clocktowerCurrentVoteCountState,
                        highestVoteNameState = clocktowerHighestVoteNameState,
                        highestVoteCountState = clocktowerHighestVoteCountState,
                        slayerClaimantNameState = clocktowerSlayerClaimantNameState,
                        slayerTargetNameState = clocktowerSlayerTargetNameState,
                        gameOutcome = gameOutcome,
                        onRecordEvent = { type, title, detail, names ->
                            addClocktowerEvent(type, title, detail, names)
                        },
                        onRecordEpistemicObservation = ::recordEpistemicObservation,
                        onPhaseChange = { nextPhase ->
                            // A phase switch ends the preceding decision window. It is a
                            // timeline fact, not a provisional UI edit, so stale drafts must
                            // not be allowed to publish into the new phase.
                            advanceClocktowerGameStateRevision()
                            clocktowerPhase = nextPhase
                            if (nextPhase == ClocktowerPhase.FirstNight || nextPhase == ClocktowerPhase.Night) {
                                resetClocktowerNightFlow()
                                clocktowerDemonSuccessorTarget = null
                            }
                            if (nextPhase == ClocktowerPhase.Day) {
                                resetClocktowerDayFlow()
                            }
                        },
                        onSelectNightDeath = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerDemonAttackDraftTarget = selected
                            val livingDemonName = cards.firstOrNull {
                                it.eliminatedRound == null && it.clocktowerTeam == ClocktowerTeam.Demon
                            }?.name
                            if (selected != livingDemonName) {
                                clocktowerDemonSuccessorTarget = null
                            }
                        },
                        onConfirmDemonAttack = {
                            if (clocktowerPendingNightDeath != clocktowerDemonAttackDraftTarget) {
                                clocktowerPendingNightDeath = clocktowerDemonAttackDraftTarget
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSelectExecution = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerSelectedExecution = it
                        },
                        onSelectPoisonTarget = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerPoisonTarget = it
                        },
                        onConfirmPoisonTarget = {
                            if (clocktowerConfirmedPoisonTarget != clocktowerPoisonTarget) {
                                clocktowerConfirmedPoisonTarget = clocktowerPoisonTarget
                                advanceClocktowerGameStateRevision()
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
                        onSelectMonkProtectedTarget = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMonkProtectedTarget = it
                        },
                        onConfirmMonkProtectedTarget = {
                            if (clocktowerConfirmedMonkProtectedTarget != clocktowerMonkProtectedTarget) {
                                clocktowerConfirmedMonkProtectedTarget = clocktowerMonkProtectedTarget
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSelectMayorRedirectTarget = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMayorRedirectTarget = it
                        },
                        onConfirmMayorRedirectTarget = {
                            if (clocktowerConfirmedMayorRedirectTarget != clocktowerMayorRedirectTarget) {
                                clocktowerConfirmedMayorRedirectTarget = clocktowerMayorRedirectTarget
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSelectDemonSuccessor = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerDemonSuccessorTarget = it
                        },
                        onConfirmNewDemon = {
                            clocktowerPendingNewDemonName = null
                            clocktowerPhase = ClocktowerPhase.Dawn
                            advanceClocktowerGameStateRevision()
                            resetClocktowerNightFlow()
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
                                        clocktowerPhase = ClocktowerPhase.Dawn
                                        clocktowerKlutzReturnToDawn = false
                                    } else {
                                        round += 1
                                        clocktowerPoisonTarget = PoisonEffectLifecycle.atStartOfNextNight()
                                        clocktowerConfirmedPoisonTarget = null
                                        clocktowerPhase = ClocktowerPhase.Night
                                    }
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                    advanceClocktowerGameStateRevision()
                                }
                            }
                        },
                        onSelectArtistClaimant = {
                            clocktowerArtistClaimantName = it
                            clocktowerArtistTruthfulAnswer = null
                            clocktowerArtistShownAnswer = null
                        },
                        onSelectArtistTruthfulAnswer = {
                            clocktowerArtistTruthfulAnswer = it
                            clocktowerArtistShownAnswer = null
                        },
                        onSelectArtistShownAnswer = { clocktowerArtistShownAnswer = it },
                        onConfirmArtistQuestion = {
                            val claimantName = clocktowerArtistClaimantName
                            if (claimantName != null) {
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
                                        "${playerSeatLabel(cards, claimantName)} · 真实答案：${if (clocktowerArtistTruthfulAnswer == true) "是" else "否"} · 展示：${if (clocktowerArtistShownAnswer == true) "是" else "否"}",
                                        "${playerSeatLabel(cards, claimantName)} · truthful: ${if (clocktowerArtistTruthfulAnswer == true) "yes" else "no"} · shown: ${if (clocktowerArtistShownAnswer == true) "yes" else "no"}",
                                    ),
                                    listOf(claimantName),
                                )
                                clocktowerArtistClaimantName = null
                                clocktowerArtistTruthfulAnswer = null
                                clocktowerArtistShownAnswer = null
                                clocktowerDayModeState.value = ClocktowerDayMode.Overview
                                advanceClocktowerGameStateRevision()
                            }
                        },
                        onSlayerShot = { claimantName, targetName, recluseRegistersAsDemon ->
                            if (claimantName !in clocktowerSlayerClaimedNames) {
                                clocktowerSlayerClaimedNames = clocktowerSlayerClaimedNames + claimantName
                            }
                            val claimantCard = cards.firstOrNull { it.name == claimantName }
                            val targetIndex = cards.indexOfFirst { it.name == targetName }
                            val targetCard = cards.getOrNull(targetIndex)
                            val isRealSlayer = claimantCard?.clocktowerRole?.enName == "Slayer"
                            val slayerPoisoned = isRealSlayer && clocktowerConfirmedPoisonTarget == claimantName
                            val canUseSlayerAbility = isRealSlayer && !clocktowerSlayerUsed
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
                            if (canUseSlayerAbility) {
                                clocktowerSlayerUsed = true
                                advanceClocktowerGameStateRevision()
                            }
                            if (canUseSlayerAbility && !slayerPoisoned && targetIndex >= 0 && targetCard != null && targetCard.eliminatedRound == null && targetRegistersAsDemon) {
                                cards[targetIndex] = targetCard.copy(eliminatedRound = round)
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
                                    canUseSlayerAbility && slayerPoisoned ->
                                        context.getString(R.string.clocktower_record_slayer_poisoned, playerSeatLabel(cards, targetName))
                                    isRealSlayer && clocktowerSlayerUsed && !canUseSlayerAbility ->
                                        context.getString(R.string.clocktower_record_slayer_already_used, playerSeatLabel(cards, targetName))
                                    canUseSlayerAbility ->
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
                        onVirginNomination = { nominatorName, nomineeName, executeNominator ->
                            clocktowerVirginUsed = true
                            advanceClocktowerGameStateRevision()
                            if (executeNominator) {
                                val index = cards.indexOfFirst { it.name == nominatorName }
                                val nominatorCard = cards.getOrNull(index)
                                if (index >= 0 && nominatorCard != null && nominatorCard.eliminatedRound == null) {
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
                                    round += 1
                                    clocktowerPoisonTarget = PoisonEffectLifecycle.atStartOfNextNight()
                                    clocktowerConfirmedPoisonTarget = null
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
                            clocktowerPhase = ClocktowerPhase.Day
                            advanceClocktowerGameStateRevision()
                            clocktowerPendingNightDeath = null
                            clocktowerDemonAttackDraftTarget = null
                            resetClocktowerNightFlow()
                            resetClocktowerDayFlow()
                        },
                        onConfirmDay = {
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
                            if (executionName == null && aliveBeforeExecution.size == 3 && aliveBeforeExecution.any { it.clocktowerRole?.enName == "Mayor" }) {
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
                                round += 1
                                clocktowerPoisonTarget = PoisonEffectLifecycle.atStartOfNextNight()
                                clocktowerConfirmedPoisonTarget = null
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
                            val demonPoisonedTonight = clocktowerConfirmedPoisonTarget?.let { name ->
                                cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
                            } == true
                            var nightKlutzName: String? = null
                            var newDemonName: String? = null
                            val originalDeathName = clocktowerPendingNightDeath
                            val originalDeathCard = originalDeathName?.let { name -> cards.firstOrNull { it.name == name } }
                            val mayorCanRedirect = originalDeathCard?.clocktowerRole?.enName == "Mayor" &&
                                originalDeathCard.eliminatedRound == null &&
                                clocktowerConfirmedPoisonTarget != originalDeathName &&
                                !demonPoisonedTonight
                            val resolvedDeathName = if (mayorCanRedirect) {
                                clocktowerConfirmedMayorRedirectTarget ?: originalDeathName
                            } else {
                                originalDeathName
                            }
                            val deathName = resolvedDeathName.takeUnless { demonPoisonedTonight }
                            if (mayorCanRedirect && resolvedDeathName != originalDeathName) {
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    localizedText("市长死亡转移", "Mayor death redirect"),
                                    localizedText(
                                        "${playerSeatLabel(cards, originalDeathName)} → ${playerSeatLabel(cards, resolvedDeathName)}",
                                        "${playerSeatLabel(cards, originalDeathName)} → ${playerSeatLabel(cards, resolvedDeathName)}",
                                    ),
                                    listOfNotNull(originalDeathName, resolvedDeathName),
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
                            if (deathName != null) {
                                clocktowerPendingNightDeath = deathName
                                val index = cards.indexOfFirst { it.name == deathName }
                                val nightDeathCard = cards.getOrNull(index)
                                if (index >= 0 && nightDeathCard != null && nightDeathCard.eliminatedRound == null) {
                                    val protectedByMonk = clocktowerConfirmedMonkProtectedTarget == deathName
                                    val soldierPoisoned = clocktowerConfirmedPoisonTarget == deathName
                                    val protectedBySoldier = nightDeathCard.clocktowerRole?.enName == "Soldier" && !soldierPoisoned
                                    if (protectedByMonk || protectedBySoldier) {
                                        val note = if (protectedBySoldier) {
                                            context.getString(R.string.clocktower_record_soldier_safe)
                                        } else {
                                            context.getString(R.string.clocktower_record_monk_protected)
                                        }
                                        records.add(EliminationRecord(round, deathName, note))
                                        addClocktowerEvent(
                                            ClocktowerEventType.RoleAction,
                                            localizedText("恶魔击杀", "Demon kill"),
                                            localizedText(
                                                "${playerSeatLabel(cards, deathName)} · 失败（$note）",
                                                "${playerSeatLabel(cards, deathName)} · failed ($note)",
                                            ),
                                            listOf(deathName),
                                        )
                                        clocktowerPendingNightDeath = null
                                    } else {
                                        val demonDied = nightDeathCard.clocktowerTeam == ClocktowerTeam.Demon
                                        val impSelfChosen = demonDied && originalDeathName == deathName
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
                                        )
                                        if (demonDied) {
                                            newDemonName = promoteDemonSuccessorIfNeeded(
                                                impDeathWasSelfChosen = impSelfChosen,
                                                preferredMinionName = clocktowerDemonSuccessorTarget,
                                            )
                                        }
                                        if (nightDeathCard.clocktowerRole?.enName == "Klutz") {
                                            nightKlutzName = deathName
                                        }
                                    }
                                    if (!protectedByMonk && !protectedBySoldier && nightDeathCard.clocktowerRole?.enName == "Ravenkeeper" && clocktowerRavenkeeperTarget != null) {
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
                            } else if (!demonPoisonedTonight && clocktowerPhase != ClocktowerPhase.FirstNight) {
                                addClocktowerEvent(
                                    ClocktowerEventType.Death,
                                    localizedText("平安夜", "No night death"),
                                    "",
                                )
                            }
                            if (nightKlutzName != null) {
                                clocktowerPendingKlutzName = nightKlutzName
                                clocktowerKlutzChoiceName = null
                                clocktowerKlutzReturnToDawn = true
                                clocktowerPhase = ClocktowerPhase.Day
                                clocktowerDayModeState.value = ClocktowerDayMode.Klutz
                            }
                            val nightOutcome = if (nightKlutzName == null) evaluateGameOutcome(context, cards, currentGameKind) else null
                            gameOutcome = nightOutcome
                            if (nightOutcome != null) {
                                showResults = true
                                addOutcomeEvent(nightOutcome)
                            } else if (nightKlutzName == null && newDemonName != null) {
                                clocktowerPendingNewDemonName = newDemonName
                            } else if (nightKlutzName == null) {
                                clocktowerPhase = ClocktowerPhase.Dawn
                                resetClocktowerNightFlow()
                            }
                            val poisonCarriedIntoTomorrow = PoisonEffectLifecycle.afterNight(
                                target = clocktowerConfirmedPoisonTarget,
                                poisonerAlive = cards.any {
                                    it.eliminatedRound == null && it.clocktowerRole?.enName == "Poisoner"
                                },
                            )
                            // Keep the persisted draft aligned with the confirmed fact while
                            // poison lasts through the following day. At next dusk both are
                            // cleared before the Poisoner chooses again.
                            clocktowerConfirmedPoisonTarget = poisonCarriedIntoTomorrow
                            clocktowerPoisonTarget = poisonCarriedIntoTomorrow
                            clocktowerFortuneTellerFirst = null
                            clocktowerFortuneTellerSecond = null
                            clocktowerChambermaidFirst = null
                            clocktowerChambermaidSecond = null
                            clocktowerRavenkeeperTarget = null
                            clocktowerMonkProtectedTarget = null
                            clocktowerConfirmedMonkProtectedTarget = null
                            clocktowerMayorRedirectTarget = null
                            clocktowerConfirmedMayorRedirectTarget = null
                            clocktowerDemonSuccessorTarget = null
                        },
                        onShowResults = {
                            gameOutcome = gameOutcome ?: GameOutcome(
                                title = context.getString(R.string.outcome_manual_title),
                                summary = context.getString(R.string.outcome_manual_summary),
                                reason = context.getString(R.string.outcome_manual_reason),
                            )
                            showResults = true
                            addOutcomeEvent(gameOutcome)
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
private fun ClocktowerLandingScreen(
    hasSavedGame: Boolean,
    onStartGame: () -> Unit,
    onContinueGame: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val brass = Color(0xFFC4A469)
    val warmWhite = Color(0xFFF3EFE5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030911)),
    ) {
        Image(
            painter = painterResource(R.drawable.clocktower_launch_hero),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0xA6030911),
                            0.28f to Color(0x16030911),
                            0.58f to Color(0x32030911),
                            1f to Color(0xF5030911),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(54.dp))
            Canvas(modifier = Modifier.size(42.dp)) {
                val stroke = 1.dp.toPx()
                drawCircle(
                    color = brass.copy(alpha = 0.75f),
                    radius = size.minDimension * 0.46f,
                    style = Stroke(width = stroke),
                )
                drawLine(
                    color = brass,
                    start = center,
                    end = Offset(center.x, center.y - size.height * 0.27f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = brass,
                    start = center,
                    end = Offset(center.x + size.width * 0.19f, center.y - size.height * 0.13f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = brass,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height * 0.1f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = brass,
                    start = Offset(center.x, size.height * 0.9f),
                    end = Offset(center.x, size.height),
                    strokeWidth = stroke,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = text("说书人专用控制台", "STORYTELLER CONSOLE"),
                color = brass,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.2.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text("血染钟楼说书人助手", "Clocktower Storyteller Assistant"),
                color = warmWhite,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text(
                    "离线主持工具，解决说书的所有问题。",
                    "An offline host toolkit for every part of storytelling.",
                ),
                color = warmWhite.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brass,
                    contentColor = Color(0xFF101319),
                ),
            ) {
                Text(
                    text("开始游戏", "Start game"),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(modifier = Modifier.height(11.dp))
            OutlinedButton(
                onClick = onContinueGame,
                enabled = hasSavedGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, brass.copy(alpha = if (hasSavedGame) 0.62f else 0.22f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = warmWhite.copy(alpha = 0.9f),
                    disabledContentColor = warmWhite.copy(alpha = 0.3f),
                    containerColor = Color(0xA8060D16),
                    disabledContainerColor = Color(0x78060D16),
                ),
            ) {
                Text(
                    text("继续上次游戏", "Continue last game"),
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!hasSavedGame) {
                Text(
                    text = text("暂无进行中的游戏", "No game in progress"),
                    modifier = Modifier.padding(top = 8.dp),
                    color = warmWhite.copy(alpha = 0.38f),
                    style = MaterialTheme.typography.labelSmall,
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupScreen(
    playerCount: Int,
    savedGamePreview: SavedGamePreview?,
    commonPlayers: List<String>,
    playerNames: List<String>,
    onAddCurrentPlayer: (String) -> Unit,
    onAddTemporaryPlayer: (String) -> Unit,
    onRemoveCurrentPlayer: (Int) -> Unit,
    onMoveCurrentPlayerTo: (Int, Int) -> Unit,
    onResumeSavedGame: () -> Unit,
    onDiscardSavedGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUndercoverSettings: () -> Unit,
    onOpenWerewolfSettings: () -> Unit,
    onOpenClocktowerSettings: () -> Unit,
) {
    val canStartUndercover = playerCount >= MIN_PLAYERS
    val canStartWerewolf = playerCount >= MIN_WEREWOLF_PLAYERS
    val canStartClocktower = playerCount >= MIN_CLOCKTOWER_PLAYERS
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var showTemporaryPlayerDialog by remember { mutableStateOf(false) }
    var temporaryPlayerName by remember { mutableStateOf("") }
    var temporaryPlayerNameWasEdited by remember { mutableStateOf(false) }
    val trimmedTemporaryPlayerName = temporaryPlayerName.trim()
    val temporaryPlayerNameExists = trimmedTemporaryPlayerName in playerNames

    fun nextTemporaryPlayerName(): String {
        var number = 1
        var candidate = text("临时玩家$number", "Temp Player $number")
        while (candidate in playerNames) {
            number += 1
            candidate = text("临时玩家$number", "Temp Player $number")
        }
        return candidate
    }

    ClocktowerDarkTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            savedGamePreview?.let { preview ->
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = text("进行中的游戏", "GAME IN PROGRESS"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                            )
                            Text(preview.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(preview.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = preview.savedAtLabel?.let {
                                    text("最后保存：$it", "Last saved: $it")
                                } ?: text("已安全保存在本机", "Safely stored on this device"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Button(
                                onClick = onResumeSavedGame,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(stringResource(R.string.continue_saved_game), fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = onDiscardSavedGame,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            ) {
                                Text(stringResource(R.string.discard_saved_game))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    RoundTableSetupEditor(
                        seatedPlayers = playerNames,
                        commonPlayers = commonPlayers,
                        canAddPlayer = playerCount < MAX_PLAYERS,
                        onAddCurrentPlayer = onAddCurrentPlayer,
                        onAddTemporaryPlayer = {
                            temporaryPlayerName = nextTemporaryPlayerName()
                            temporaryPlayerNameWasEdited = false
                            showTemporaryPlayerDialog = true
                        },
                        onOpenSettings = onOpenSettings,
                        onRemoveCurrentPlayer = onRemoveCurrentPlayer,
                        onMoveCurrentPlayerTo = onMoveCurrentPlayerTo,
                        modifier = Modifier.padding(18.dp),
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.choose_game), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = onOpenClocktowerSettings,
                        enabled = canStartClocktower,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            if (canStartClocktower) {
                                stringResource(R.string.game_clocktower)
                            } else {
                                stringResource(R.string.need_clocktower_min_players, MIN_CLOCKTOWER_PLAYERS)
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenUndercoverSettings,
                        enabled = canStartUndercover,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            if (canStartUndercover) {
                                stringResource(R.string.game_who_is_undercover)
                            } else {
                                stringResource(R.string.need_min_players, MIN_PLAYERS)
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenWerewolfSettings,
                        enabled = canStartWerewolf,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            if (canStartWerewolf) {
                                stringResource(R.string.game_werewolf)
                            } else {
                                stringResource(R.string.need_werewolf_min_players, MIN_WEREWOLF_PLAYERS)
                            }
                        )
                    }
                }

            }
        }

        if (showTemporaryPlayerDialog) {
            AlertDialog(
                onDismissRequest = { showTemporaryPlayerDialog = false },
                title = { Text(text("添加临时玩家", "Add temporary player")) },
                text = {
                    OutlinedTextField(
                        value = temporaryPlayerName,
                        onValueChange = {
                            temporaryPlayerName = it
                            temporaryPlayerNameWasEdited = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && !temporaryPlayerNameWasEdited) {
                                    temporaryPlayerName = ""
                                    temporaryPlayerNameWasEdited = true
                                }
                            },
                        label = { Text(stringResource(R.string.player_name_input_label)) },
                        singleLine = true,
                        isError = temporaryPlayerNameExists,
                        supportingText = if (temporaryPlayerNameExists) {
                            { Text(text("该名字已在本局玩家中", "This name is already in the game")) }
                        } else {
                            null
                        },
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onAddTemporaryPlayer(trimmedTemporaryPlayerName)
                            showTemporaryPlayerDialog = false
                        },
                        enabled = trimmedTemporaryPlayerName.isNotEmpty() &&
                            !temporaryPlayerNameExists &&
                            playerNames.size < MAX_PLAYERS,
                    ) {
                        Text(stringResource(R.string.add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTemporaryPlayerDialog = false }) {
                        Text(text("取消", "Cancel"))
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoundTableSetupEditor(
    seatedPlayers: List<String>,
    commonPlayers: List<String>,
    canAddPlayer: Boolean,
    onAddCurrentPlayer: (String) -> Unit,
    onAddTemporaryPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onRemoveCurrentPlayer: (Int) -> Unit,
    onMoveCurrentPlayerTo: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragState by remember { mutableStateOf<PlayerDragState?>(null) }
    var hoverInsertIndex by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val tableFillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val tableStrokeColor = MaterialTheme.colorScheme.primary
    val tableGuideColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(stringResource(R.string.current_players_section), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (seatedPlayers.size >= MAX_PLAYERS) {
                        text("当前 ${seatedPlayers.size} 人 · 已达上限", "${seatedPlayers.size} players · Maximum reached")
                    } else {
                        text("当前 ${seatedPlayers.size} 人", "${seatedPlayers.size} players")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onAddTemporaryPlayer,
                    enabled = canAddPlayer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.add_temporary_player))
                }
                IconButton(onClick = onOpenSettings) {
                    Text(
                        text = "⚙",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val useRectangularTable = seatedPlayers.size > 8
            val avatarSizeDp = when {
                seatedPlayers.size >= 13 -> 46.dp
                useRectangularTable -> 52.dp
                else -> 64.dp
            }
            val center = Offset(widthPx / 2f, heightPx / 2f)
            val avatarSizePx = with(density) { avatarSizeDp.toPx() }
            val safeRadius = (min(widthPx, heightPx) - avatarSizePx * 2.2f) / 2f
            val tableRadius = safeRadius.coerceAtLeast(avatarSizePx * 1.25f)
            val seatRadius = tableRadius
            val tableDropRadius = tableRadius + avatarSizePx * 1.2f
            val tableLeft = avatarSizePx * 0.95f
            val tableRight = widthPx - avatarSizePx * 0.95f
            val tableTop = avatarSizePx * 1.05f
            val tableBottom = heightPx - avatarSizePx * 1.65f
            val tableWidth = tableRight - tableLeft
            val tableHeight = tableBottom - tableTop

            fun rectangularSideCounts(count: Int): IntArray {
                return when (count) {
                    0 -> intArrayOf(0, 0, 0, 0)
                    1 -> intArrayOf(1, 0, 0, 0)
                    2 -> intArrayOf(1, 0, 1, 0)
                    3 -> intArrayOf(1, 1, 1, 0)
                    4 -> intArrayOf(1, 1, 1, 1)
                    5 -> intArrayOf(2, 1, 1, 1)
                    6 -> intArrayOf(2, 1, 2, 1)
                    7 -> intArrayOf(2, 2, 2, 1)
                    8 -> intArrayOf(2, 2, 2, 2)
                    9 -> intArrayOf(3, 2, 2, 2)
                    10 -> intArrayOf(3, 2, 3, 2)
                    11 -> intArrayOf(3, 3, 3, 2)
                    12 -> intArrayOf(4, 2, 4, 2)
                    else -> intArrayOf(4, 3, 4, (count - 11).coerceAtLeast(2))
                }
            }

            fun rectangularSeatPosition(index: Int, count: Int): Offset {
                if (count == 0) return center
                val sideCounts = rectangularSideCounts(count)
                var remainingIndex = index
                val topCount = sideCounts[0]
                if (remainingIndex < topCount) {
                    val x = tableLeft + tableWidth * (remainingIndex + 1) / (topCount + 1)
                    return Offset(x, tableTop)
                }
                remainingIndex -= topCount

                val rightCount = sideCounts[1]
                if (remainingIndex < rightCount) {
                    val y = tableTop + tableHeight * (remainingIndex + 1) / (rightCount + 1)
                    return Offset(tableRight, y)
                }
                remainingIndex -= rightCount

                val bottomCount = sideCounts[2]
                if (remainingIndex < bottomCount) {
                    val x = tableRight - tableWidth * (remainingIndex + 1) / (bottomCount + 1)
                    return Offset(x, tableBottom)
                }
                remainingIndex -= bottomCount

                val leftCount = sideCounts[3].coerceAtLeast(1)
                val y = tableBottom - tableHeight * (remainingIndex + 1) / (leftCount + 1)
                return Offset(tableLeft, y)
            }

            fun circularSeatPosition(index: Int, count: Int): Offset {
                if (count == 0) return center
                val angle = (-PI / 2.0) + (2.0 * PI * index / count)
                return Offset(
                    x = center.x + (cos(angle) * seatRadius).toFloat(),
                    y = center.y + (sin(angle) * seatRadius).toFloat(),
                )
            }

            fun seatPosition(index: Int, count: Int): Offset {
                return if (useRectangularTable) {
                    rectangularSeatPosition(index, count)
                } else {
                    circularSeatPosition(index, count)
                }
            }

            fun insertMarkerPosition(insertIndex: Int, count: Int): Offset {
                if (count == 0) return center
                if (!useRectangularTable) {
                    val angle = (-PI / 2.0) + (2.0 * PI * (insertIndex - 0.5) / count)
                    return Offset(
                        x = center.x + (cos(angle) * seatRadius).toFloat(),
                        y = center.y + (sin(angle) * seatRadius).toFloat(),
                    )
                }

                val currentSeat = seatPosition(if (insertIndex == count) 0 else insertIndex.coerceIn(0, count - 1), count)
                val previousSeat = seatPosition((insertIndex - 1 + count) % count, count)
                return Offset(
                    x = (previousSeat.x + currentSeat.x) / 2f,
                    y = (previousSeat.y + currentSeat.y) / 2f,
                )
            }

            fun insertIndexChangesOrder(insertIndex: Int, originalIndex: Int): Boolean {
                val adjustedInsertIndex = if (insertIndex > originalIndex) insertIndex - 1 else insertIndex
                return adjustedInsertIndex != originalIndex
            }

            fun nearestInsertIndex(point: Offset, count: Int): Int {
                if (count == 0) return 0
                if (useRectangularTable) {
                    return (0..count)
                        .minByOrNull { index -> insertMarkerPosition(index, count).let { marker -> (marker - point).getDistance() } }
                        ?.coerceIn(0, count)
                        ?: 0
                }
                val angle = atan2(point.y - center.y, point.x - center.x)
                val normalized = ((angle + PI / 2.0 + 2.0 * PI) % (2.0 * PI))
                return ((normalized / (2.0 * PI) * count).roundToInt()).coerceIn(0, count)
            }

            fun isInTable(point: Offset): Boolean {
                if (useRectangularTable) {
                    val margin = avatarSizePx * 1.5f
                    return point.x in (tableLeft - margin)..(tableRight + margin) &&
                        point.y in (tableTop - margin)..(tableBottom + margin)
                }
                return (point - center).getDistance() <= tableDropRadius
            }

            fun isRemoveDrop(point: Offset): Boolean {
                if (useRectangularTable) {
                    val margin = avatarSizePx * 2.1f
                    val insideExpandedTable = point.x in (tableLeft - margin)..(tableRight + margin) &&
                        point.y in (tableTop - margin)..(tableBottom + margin)
                    return seatedPlayers.isNotEmpty() && !insideExpandedTable
                }
                return seatedPlayers.isNotEmpty() && (point - center).getDistance() > tableDropRadius + avatarSizePx
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (useRectangularTable) {
                    val cornerRadius = 24.dp.toPx()
                    drawRoundRect(
                        color = tableFillColor,
                        topLeft = Offset(tableLeft, tableTop),
                        size = Size(tableWidth, tableHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    )
                    drawRoundRect(
                        color = tableStrokeColor,
                        topLeft = Offset(tableLeft, tableTop),
                        size = Size(tableWidth, tableHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = 5.dp.toPx()),
                    )
                    drawRoundRect(
                        color = tableGuideColor,
                        topLeft = Offset(tableLeft, tableTop),
                        size = Size(tableWidth, tableHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                } else {
                    drawCircle(
                        color = tableFillColor,
                        radius = tableRadius,
                        center = center,
                    )
                    drawCircle(
                        color = tableStrokeColor,
                        radius = tableRadius,
                        center = center,
                        style = Stroke(width = 5.dp.toPx()),
                    )
                    drawCircle(
                        color = tableGuideColor,
                        radius = seatRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            if (seatedPlayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.round_table_empty_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            val draggedSeatedPlayer = dragState?.player as? DraggedPlayer.Seated
            val previewInsertIndex = hoverInsertIndex
                ?.takeIf { insertIndex ->
                    draggedSeatedPlayer?.let { insertIndexChangesOrder(insertIndex, it.originalIndex) } == true
                }
                ?.coerceIn(0, seatedPlayers.size)

            fun previewSeatIndex(originalIndex: Int): Int {
                val draggedIndex = draggedSeatedPlayer?.originalIndex ?: return originalIndex
                val insertIndex = previewInsertIndex ?: return originalIndex
                val adjustedInsertIndex = if (insertIndex > draggedIndex) insertIndex - 1 else insertIndex
                if (originalIndex == draggedIndex) return adjustedInsertIndex.coerceAtMost(seatedPlayers.lastIndex)

                val indexAfterRemovingDraggedPlayer = if (originalIndex > draggedIndex) originalIndex - 1 else originalIndex
                return if (indexAfterRemovingDraggedPlayer >= adjustedInsertIndex) {
                    indexAfterRemovingDraggedPlayer + 1
                } else {
                    indexAfterRemovingDraggedPlayer
                }
            }

            previewInsertIndex?.let { insertIndex ->
                val marker = insertMarkerPosition(insertIndex, seatedPlayers.size)
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (marker.x - avatarSizePx / 2f).roundToInt(),
                                (marker.y - avatarSizePx / 2f).roundToInt(),
                            )
                        }
                        .size(avatarSizeDp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }

            seatedPlayers.forEachIndexed { index, name ->
                key(name) {
                    val isDraggedPlayer = draggedSeatedPlayer?.originalIndex == index
                    val position = if (isDraggedPlayer) {
                        dragState?.center ?: seatPosition(index, seatedPlayers.size)
                    } else {
                        seatPosition(previewSeatIndex(index), seatedPlayers.size)
                    }
                    DraggableAvatar(
                        name = name,
                        badge = (index + 1).toString(),
                        center = position,
                        avatarSizePx = avatarSizePx,
                        avatarSizeDp = avatarSizeDp,
                        isDragging = isDraggedPlayer,
                        enabled = dragState == null || (dragState?.player as? DraggedPlayer.Seated)?.originalIndex == index,
                        onDragStart = {
                            hoverInsertIndex = null
                            dragState = PlayerDragState(DraggedPlayer.Seated(index, name), position)
                        },
                        onDrag = { centerPoint ->
                            dragState = PlayerDragState(DraggedPlayer.Seated(index, name), centerPoint)
                            hoverInsertIndex = if (isInTable(centerPoint)) nearestInsertIndex(centerPoint, seatedPlayers.size) else null
                        },
                        onDragEnd = { centerPoint ->
                            val insertIndex = hoverInsertIndex
                            when {
                                isRemoveDrop(centerPoint) -> onRemoveCurrentPlayer(index)
                                insertIndex != null && insertIndexChangesOrder(insertIndex, index) -> onMoveCurrentPlayerTo(index, insertIndex)
                            }
                            dragState = null
                            hoverInsertIndex = null
                        },
                        onDragCancel = {
                            dragState = null
                            hoverInsertIndex = null
                        },
                    )
                }
            }
        }

        Text(stringResource(R.string.bench_area), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.bench_area_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (commonPlayers.isEmpty()) {
                EmptyStateCard(text = stringResource(R.string.no_common_players_setup))
            } else {
                commonPlayers.forEach { name ->
                    val alreadyJoined = name in seatedPlayers
                    BenchPlayerChip(
                        name = name,
                        enabled = !alreadyJoined && canAddPlayer,
                        label = if (alreadyJoined) stringResource(R.string.common_player_joined_format, name) else name,
                        onClick = { onAddCurrentPlayer(name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableAvatar(
    name: String,
    badge: String,
    center: Offset,
    avatarSizePx: Float,
    avatarSizeDp: Dp,
    isDragging: Boolean,
    enabled: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit,
) {
    var dragCenter by remember { mutableStateOf(center) }
    val animatedCenter by animateOffsetAsState(
        targetValue = center,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "seat-position",
    )
    val displayedCenter = if (isDragging) dragCenter else animatedCenter
    val latestDisplayedCenter by rememberUpdatedState(displayedCenter)
    val latestCenter by rememberUpdatedState(center)
    Column(
        modifier = Modifier
            .zIndex(if (isDragging) 2f else 1f)
            .offset {
                IntOffset(
                    (displayedCenter.x - avatarSizePx / 2f).roundToInt(),
                    (displayedCenter.y - avatarSizePx / 2f).roundToInt(),
                )
            }
            .width(avatarSizeDp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragCenter = latestDisplayedCenter
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragCenter += dragAmount
                        onDrag(dragCenter)
                    },
                    onDragEnd = {
                        onDragEnd(dragCenter)
                        dragCenter = latestCenter
                    },
                    onDragCancel = {
                        dragCenter = latestCenter
                        onDragCancel()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(avatarSizeDp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
        }
        Text(
            text = name,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun BenchPlayerChip(
    name: String,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    var dragDistance by remember { mutableStateOf(Offset.Zero) }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.pointerInput(enabled, name) {
            if (!enabled) return@pointerInput
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragDistance += dragAmount
                },
                onDragEnd = {
                    if (dragDistance.y < -80f) onClick()
                    dragDistance = Offset.Zero
                },
                onDragCancel = { dragDistance = Offset.Zero },
            )
        },
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    languageMode: LanguageMode,
    storytellerAutomationMode: StorytellerAutomationMode,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onStorytellerAutomationModeChange: (StorytellerAutomationMode) -> Unit,
    onNewCommonPlayerNameChange: (String) -> Unit,
    onAddCommonPlayer: () -> Unit,
    onRemoveCommonPlayer: (String) -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_subtitle), color = Color(0xFF5C6A63))
                }
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.back))
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (language == "en") "Storyteller decisions" else "说书人判定方式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (language == "en") {
                                "Choose manual control or an automatic style. Automatic rulings also consider the global game balance."
                            } else {
                                "选择手动控制或全自动风格；自动裁定还会结合全局局势。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    val automationModes = listOf(
                        StorytellerAutomationMode.MANUAL to (
                            if (language == "en") "Manual" to "Show legal recommendations and let the Storyteller decide."
                            else "手动" to "显示合法建议，由说书人自行决定。"
                        ),
                        StorytellerAutomationMode.AUTO_BALANCED to (
                            if (language == "en") "Automatic · Balanced" to "Moderate information, risk, and assistance to the trailing team."
                            else "全自动－均衡" to "适度控制信息、风险，并帮助当前落后的一方。"
                        ),
                        StorytellerAutomationMode.AUTO_AGGRESSIVE to (
                            if (language == "en") "Automatic · Aggressive" to "Allows more deception and high-impact rulings while preserving balance."
                            else "全自动－激进" to "允许更多误导和高影响裁定，同时保持局势平衡。"
                        ),
                        StorytellerAutomationMode.AUTO_GENTLE to (
                            if (language == "en") "Automatic · Gentle" to "Prefers clear, low-risk, and less disruptive rulings."
                            else "全自动－稳健" to "优先清晰、低风险、较少改变局势的裁定。"
                        ),
                    )
                    automationModes.forEach { (mode, copy) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStorytellerAutomationModeChange(mode) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = storytellerAutomationMode == mode,
                                onClick = { onStorytellerAutomationModeChange(mode) },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(copy.first, fontWeight = FontWeight.SemiBold)
                                Text(
                                    copy.second,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.language_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LanguageMode.entries.forEach { mode ->
                        val selected = mode == languageMode
                        if (selected) {
                            Button(
                                onClick = { onLanguageModeChange(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(mode.labelResId()))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onLanguageModeChange(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(mode.labelResId()))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.common_players_management), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = newCommonPlayerName,
                            onValueChange = onNewCommonPlayerNameChange,
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.player_name_input_label)) },
                            singleLine = true,
                        )
                        Button(
                            onClick = onAddCommonPlayer,
                            enabled = newCommonPlayerName.trim().isNotEmpty() && newCommonPlayerName.trim() !in commonPlayers,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }

                    if (commonPlayers.isEmpty()) {
                        EmptyStateCard(text = stringResource(R.string.no_common_players_settings))
                    } else {
                        commonPlayers.forEach { name ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { onRemoveCommonPlayer(name) }) {
                                    Text(stringResource(R.string.remove))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GameSettingsHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF5C6A63))
        }
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
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
internal fun StepperRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onChange((value - 1).coerceAtLeast(range.first)) },
                enabled = value > range.first,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("-", fontSize = 22.sp, textAlign = TextAlign.Center)
            }
            Text(
                value.toString(),
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(
                onClick = { onChange((value + 1).coerceAtMost(range.last)) },
                enabled = value < range.last,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("+", fontSize = 22.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PassPhoneScreen(
    playerName: String,
    gameKind: GameKind,
    current: Int,
    total: Int,
    onReveal: () -> Unit,
) {
    if (gameKind == GameKind.Clocktower) {
        ClocktowerDealHandoffScreen(
            playerName = playerName,
            current = current,
            total = total,
            onReveal = onReveal,
        )
        return
    }
    FullScreenColumn {
        Text("$current / $total", color = Color(0xFF6F7B74))
        Text(stringResource(R.string.pass_phone_to), style = MaterialTheme.typography.titleLarge)
        Text(playerName, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.reveal_privacy_hint), color = Color(0xFF5C6A63), textAlign = TextAlign.Center)
        Button(
            onClick = onReveal,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(stringResource(R.string.reveal_my_card))
        }
    }
}

@Composable
private fun RevealCardScreen(
    card: PlayerCard,
    gameKind: GameKind,
    current: Int,
    total: Int,
    onHide: () -> Unit,
) {
    if (gameKind == GameKind.Clocktower) {
        ClocktowerPlayerRoleRevealScreen(
            card = card,
            current = current,
            total = total,
            onHide = onHide,
        )
        return
    }
    FullScreenColumn {
        Text("$current / $total", color = Color(0xFF6F7B74))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(card.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (gameKind == GameKind.Werewolf || gameKind == GameKind.Clocktower) {
                    Text(card.roleLabel ?: stringResource(card.role.labelResId()), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    Text(card.word, color = Color(0xFF5C6A63), textAlign = TextAlign.Center)
                    Text(stringResource(R.string.remember_role_hint), color = Color(0xFF5C6A63))
                } else {
                    Text(card.word, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    Text(stringResource(R.string.remember_word_hint), color = Color(0xFF5C6A63))
                }
            }
        }
        Button(
            onClick = onHide,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (current == total) stringResource(R.string.all_done_return_to_host) else stringResource(R.string.hide_and_next))
        }
    }
}

@Composable
internal fun HostProgressCard(
    title: String,
    subtitle: String,
    progress: String,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(progress, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun ClocktowerDarkTheme(content: @Composable () -> Unit) {
    val typography = MaterialTheme.typography
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFFC5A56A),
            onPrimary = Color(0xFF17120A),
            secondary = Color(0xFF61798A),
            onSecondary = Color(0xFFF7F1E6),
            background = Color(0xFF0B0D10),
            onBackground = Color(0xFFF1EADC),
            surface = Color(0xFF14171C),
            onSurface = Color(0xFFF1EADC),
            surfaceVariant = Color(0xFF1B1F25),
            onSurfaceVariant = Color(0xFFAAA397),
            error = Color(0xFFC9574A),
            onError = Color(0xFFF7F1E6),
        ),
        typography = typography,
        content = content,
    )
}

@Composable
internal fun ClocktowerNewDemonConfirmationScreen(
    newDemonLabel: String,
    hasNewDemon: Boolean,
    onShowPlayerDisplay: () -> Unit,
    onConfirm: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text("小恶魔自杀", "Imp self-kill"),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text("恶魔传承 · 私密操作", "Demon succession · Private action"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text("告知新恶魔", "INFORM THE NEW DEMON"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            newDemonLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text("说书人操作", "STORYTELLER ACTION"),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    text(
                                        "轻拍并唤醒这名玩家，只向他展示新的身份。确认看完后收回手机，并示意闭眼。",
                                        "Wake this player and show the new identity privately. Take back the phone and signal them to close their eyes.",
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text(
                                    "不要向其他玩家宣布恶魔已经更换。",
                                    "Do not announce the Demon change to other players.",
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onShowPlayerDisplay,
                        enabled = hasNewDemon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_show_to_player))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = hasNewDemon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text("已告知，进入天亮", "Informed, continue to dawn"),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HostScriptCard(
    title: String,
    script: String,
    action: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val instruction = listOf(script.trim(), action.trim())
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString("\n")
            HostInstructionBlock(
                label = stringResource(R.string.host_instruction_label),
                text = instruction,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                textColor = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
internal fun HostInstructionBlock(
    label: String,
    text: String,
    backgroundColor: Color,
    textColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, color = textColor.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Text(text, color = textColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun HostActionSection(
    title: String,
    helper: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        helper?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        content()
    }
}

@Composable
internal fun WerewolfRoleLine(roleName: String, players: List<PlayerCard>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(roleName, fontWeight = FontWeight.SemiBold)
        Text(
            text = if (players.isEmpty()) stringResource(R.string.no_role_players) else players.joinToString { it.name },
            color = Color(0xFF6F7B74),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectablePlayerChips(
    cards: List<PlayerCard>,
    selectedName: String?,
    enabled: Boolean,
    allCards: List<PlayerCard> = cards,
    onSelect: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val selected = selectedName == card.name
            if (selected) {
                Button(onClick = { onSelect(card.name) }, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
                    Text(card.seatLabel(allCards))
                }
            } else {
                OutlinedButton(onClick = { onSelect(card.name) }, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
                    Text(card.seatLabel(allCards))
                }
            }
        }
    }
}

internal enum class TwoPlayerSelectionAction {
    ToggleFirst,
    ToggleSecond,
    RejectLimit,
}

internal fun twoPlayerSelectionAction(
    first: String?,
    second: String?,
    selectedName: String,
): TwoPlayerSelectionAction = when {
    selectedName == first -> TwoPlayerSelectionAction.ToggleFirst
    selectedName == second -> TwoPlayerSelectionAction.ToggleSecond
    first == null -> TwoPlayerSelectionAction.ToggleFirst
    second == null -> TwoPlayerSelectionAction.ToggleSecond
    else -> TwoPlayerSelectionAction.RejectLimit
}

internal fun shouldAutoAdvanceRedHerring(
    automaticStorytellerInfo: Boolean,
    isRedHerringStep: Boolean,
    isRealAction: Boolean,
    hasSelectedRedHerring: Boolean,
): Boolean = automaticStorytellerInfo &&
    isRedHerringStep &&
    (!isRealAction || hasSelectedRedHerring)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectableTwoPlayerChips(
    cards: List<PlayerCard>,
    firstSelectedName: String?,
    secondSelectedName: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    val selectedNames = setOfNotNull(firstSelectedName, secondSelectedName)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val label = if (card.eliminatedRound != null) {
                stringResource(R.string.clocktower_player_dead_format, card.seatLabel(cards))
            } else {
                card.seatLabel(cards)
            }
            if (card.name in selectedNames) {
                Button(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(label)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun ClocktowerDealHandoffScreen(
    playerName: String,
    current: Int,
    total: Int,
    onReveal: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text("秘密发牌", "PRIVATE DEAL"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            "$current / $total",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(total) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        if (index < current) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(50),
                                    ),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = current.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text("请把手机交给", "Pass the phone to"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    playerName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text("隐私确认", "PRIVACY CHECK"),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text(
                                "确认只有 $playerName 能看到屏幕后，再查看身份。",
                                "Make sure only $playerName can see the screen before revealing.",
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onReveal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text("我是 $playerName，查看身份", "I am $playerName — reveal my role"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClocktowerPlayerRoleRevealScreen(
    card: PlayerCard,
    current: Int,
    total: Int,
    onHide: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val shownRole = card.clocktowerShownRole
    val roleName = shownRole?.nameFor(language) ?: card.roleLabel ?: stringResource(card.role.labelResId())
    val team = shownRole?.team
    val teamName = team?.label(context)
    val description = shownRole?.descriptionFor(language) ?: card.word
    val accentColor = when (team) {
        ClocktowerTeam.Townsfolk -> Color(0xFF8FB6D6)
        ClocktowerTeam.Outsider -> Color(0xFF9AAEC0)
        ClocktowerTeam.Minion -> Color(0xFFD09A6A)
        ClocktowerTeam.Demon -> Color(0xFFD96B70)
        null -> Color(0xFFC5A56A)
    }

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text("仅供你查看", "FOR YOUR EYES ONLY"),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                        )
                        Text(card.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "$current / $total",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.48f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        teamName?.let {
                            Surface(
                                color = accentColor.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    color = accentColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                        Text(
                            roleName,
                            color = accentColor,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        HorizontalDivider(color = accentColor.copy(alpha = 0.28f))
                        Text(
                            text("你的能力", "YOUR ABILITY"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            description,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text(
                        "记住角色和能力。不要讨论身份，隐藏页面后把手机交回说书人或下一位玩家。",
                        "Remember your character and ability. Hide this screen before passing the phone back.",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onHide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (current == total) {
                            text("隐藏身份，交回说书人", "Hide role and return to host")
                        } else {
                            text("隐藏身份，交给下一位玩家", "Hide role and pass to next player")
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectableSeatNumbers(
    cards: List<PlayerCard>,
    selectedName: String?,
    enabled: Boolean,
    allCards: List<PlayerCard> = cards,
    onSelect: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val selected = selectedName == card.name
            val seatNumber = (allCards.indexOfFirst { it.name == card.name } + 1).takeIf { it > 0 } ?: 0
            val colors = if (selected) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            }
            if (selected) {
                Button(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = colors,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(58.dp),
                ) {
                    Text(seatNumber.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(card.name) },
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = colors,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(58.dp),
                ) {
                    Text(seatNumber.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
internal fun WerewolfPlayerStatusRow(card: PlayerCard) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.name, fontWeight = FontWeight.SemiBold)
                Text(card.roleLabel ?: stringResource(card.role.labelResId()), color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
            }
            val status = card.eliminatedRound?.let { stringResource(R.string.eliminated_round_format, it) }
                ?: stringResource(R.string.active_status)
            Text(status, color = if (card.eliminatedRound == null) Color(0xFF2F5D50) else Color(0xFF9A4B36))
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameScreen(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    round: Int,
    gameOutcome: GameOutcome?,
    selectedElimination: String?,
    onSelectElimination: (String) -> Unit,
    onConfirmElimination: () -> Unit,
    onShowResults: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(stringResource(R.string.host_panel), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    val gameName = when (gameKind) {
                        GameKind.Werewolf -> stringResource(R.string.game_werewolf)
                        GameKind.Clocktower -> stringResource(R.string.game_clocktower)
                        GameKind.Undercover -> stringResource(R.string.game_who_is_undercover)
                    }
                    Text("$gameName · ${gameOutcome?.title ?: stringResource(R.string.round_format, round)}", color = Color(0xFF5C6A63))
                }
            }
        }

        if (gameOutcome != null) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(gameOutcome.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(gameOutcome.summary)
                        Text(gameOutcome.reason, color = Color(0xFF5C6A63))
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.select_elimination), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cards.filter { it.eliminatedRound == null }.forEach { card ->
                    val selected = selectedElimination == card.name
                    if (selected) {
                        Button(onClick = { onSelectElimination(card.name) }, shape = RoundedCornerShape(8.dp)) {
                            Text(card.name)
                        }
                    } else {
                        OutlinedButton(onClick = { onSelectElimination(card.name) }, shape = RoundedCornerShape(8.dp)) {
                            Text(card.name)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onConfirmElimination,
                enabled = selectedElimination != null && gameOutcome == null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.record_elimination))
            }
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.player_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(cards) { card ->
            PlayerStatusRow(card)
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.elimination_records), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text(stringResource(R.string.no_eliminations), color = Color(0xFF6F7B74))
            }
        }

        items(records) { record ->
            Text(stringResource(R.string.elimination_record_format, record.round, record.playerName), modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            Button(
                onClick = onShowResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(if (gameOutcome == null) stringResource(R.string.end_and_reveal) else stringResource(R.string.view_results))
            }
        }
    }
}

@Composable
private fun PlayerStatusRow(card: PlayerCard) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(card.name, fontWeight = FontWeight.SemiBold)
            val status = card.eliminatedRound?.let { stringResource(R.string.eliminated_round_format, it) }
                ?: stringResource(R.string.active_status)
            Text(status, color = if (card.eliminatedRound == null) Color(0xFF2F5D50) else Color(0xFF9A4B36))
        }
    }
}

@Composable
private fun ResultsDialog(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    outcome: GameOutcome?,
    onDismiss: () -> Unit,
    onReview: () -> Unit,
    onNewGame: () -> Unit,
) {
    val defaultTitle = if (gameKind == GameKind.Werewolf) {
        stringResource(R.string.werewolf_role_results)
    } else if (gameKind == GameKind.Clocktower) {
        stringResource(R.string.clocktower_role_results)
    } else {
        stringResource(R.string.identity_results)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(outcome?.title ?: defaultTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (outcome != null) {
                    Text(outcome.summary, fontWeight = FontWeight.SemiBold)
                    Text(outcome.reason, color = Color(0xFF5C6A63))
                    HorizontalDivider()
                }
                cards.forEach { card ->
                    if (gameKind == GameKind.Werewolf || gameKind == GameKind.Clocktower) {
                        val roleText = if (gameKind == GameKind.Clocktower && card.clocktowerShownAsDifferentRole() && card.clocktowerShownRole != null) {
                            stringResource(
                                R.string.clocktower_result_role_format,
                                card.hostRoleLabel(LocalContext.current, GameKind.Clocktower),
                                card.clocktowerShownRole.nameFor(LocalContext.current.resources.configuration.locales[0].language),
                            )
                        } else {
                            card.hostRoleLabel(LocalContext.current, gameKind)
                        }
                        Text(stringResource(R.string.result_role_format, card.name, roleText))
                    } else {
                        Text(stringResource(R.string.result_card_format, card.name, stringResource(card.role.labelResId()), card.word))
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onReview) {
                    Text(if (LocalContext.current.resources.configuration.locales[0].language == "en") "Review log" else "复盘记录")
                }
                TextButton(onClick = onNewGame) {
                    Text(if (LocalContext.current.resources.configuration.locales[0].language == "en") "Prepare next game" else "准备下一局")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        },
    )
}

@Composable
private fun HostToolsTopBar(onOpen: () -> Unit) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF14171C),
            shadowElevation = 5.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text("主持模式", "HOST MODE"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text("私密操作入口", "Private controls"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                OutlinedButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text("主持工具", "Host tools"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

@Composable
private fun NewGameConfirmationDialog(
    gameKind: GameKind,
    onDismiss: () -> Unit,
    onManagePlayers: () -> Unit,
    onQuickRestart: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val gameName = when (gameKind) {
        GameKind.Undercover -> stringResource(R.string.game_who_is_undercover)
        GameKind.Werewolf -> stringResource(R.string.game_werewolf)
        GameKind.Clocktower -> stringResource(R.string.game_clocktower)
    }
    ClocktowerDarkTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text("结束当前游戏？", "End this game?")) },
            text = {
                Text(
                    text(
                        "“$gameName”的角色身份和操作记录会保存到历史复盘。建议先返回玩家管理，确认本局参与者后再发牌。",
                        "Roles and game records for $gameName will be saved to history. Review the player list before dealing the next game.",
                    ),
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onManagePlayers,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("保存并调整玩家", "Save & manage players"), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onQuickRestart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("原班人马快速再来一局", "Quick restart with same players"))
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("继续当前游戏", "Continue game"))
                    }
                }
            },
            dismissButton = {},
        )
    }
}

@Composable
private fun HostGameToolsScreen(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    events: List<ClocktowerEvent>,
    history: List<ArchivedGameReview>,
    initialTab: HostToolTab,
    onDismiss: () -> Unit,
    onNewGame: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var selectedHistoryId by remember { mutableStateOf<Long?>(null) }
    val selectedHistory = history.firstOrNull { it.id == selectedHistoryId }

    BackHandler(onBack = onDismiss)

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text("私密 · 仅主持人", "PRIVATE · HOST ONLY"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text("主持工具", "Host tools"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text("关闭", "Close"))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HostToolTab.entries.forEach { tab ->
                        val label = when (tab) {
                            HostToolTab.Roles -> text("角色身份", "Roles")
                            HostToolTab.Records -> text("操作记录", "Game log")
                            HostToolTab.History -> text("历史复盘 ${history.size}", "History ${history.size}")
                        }
                        if (selectedTab == tab) {
                            Button(
                                onClick = {
                                    selectedTab = tab
                                    selectedHistoryId = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            ) {
                                Text(label, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    selectedTab = tab
                                    selectedHistoryId = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                            ) {
                                Text(label, maxLines = 1)
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

                when (selectedTab) {
                    HostToolTab.Roles -> HostRolesList(
                        gameKind = gameKind,
                        cards = cards,
                        modifier = Modifier.weight(1f),
                    )
                    HostToolTab.Records -> HostRecordsList(
                        gameKind = gameKind,
                        records = records,
                        events = events,
                        modifier = Modifier.weight(1f),
                    )
                    HostToolTab.History -> {
                        if (selectedHistory == null) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (history.isEmpty()) {
                                    item {
                                        Text(
                                            text("结束一局后，角色和记录会保存在这里。", "Finished games will be saved here."),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                items(history, key = { it.id }) { review ->
                                    ArchivedGameCard(
                                        review = review,
                                        onClick = { selectedHistoryId = review.id },
                                    )
                                }
                            }
                        } else {
                            ArchivedGameReviewContent(
                                review = selectedHistory,
                                onBack = { selectedHistoryId = null },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp,
                ) {
                    TextButton(
                        onClick = onNewGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text("结束当前游戏并开始新一局", "End current game and start a new one"),
                            color = Color(0xFFC9574A),
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostRolesList(
    gameKind: GameKind,
    cards: List<PlayerCard>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text("请确认屏幕仅对主持人可见。", "Make sure only the host can see this screen."),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        items(cards) { card ->
            val role = card.hostRoleLabel(context, gameKind)
            val shown = if (
                gameKind == GameKind.Clocktower &&
                card.clocktowerShownAsDifferentRole() &&
                card.clocktowerShownRole != null
            ) {
                text(
                    " · 展示为 ${card.clocktowerShownRole.nameFor(language)}",
                    " · shown as ${card.clocktowerShownRole.nameFor(language)}",
                )
            } else {
                ""
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (cards.indexOf(card) + 1).toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(card.name, fontWeight = FontWeight.Bold)
                        Text(role + shown, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        card.eliminatedRound?.let { text("死亡", "Dead") } ?: text("存活", "Alive"),
                        color = if (card.eliminatedRound == null) Color(0xFF2F7A57) else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun HostRecordsList(
    gameKind: GameKind,
    records: List<EliminationRecord>,
    events: List<ClocktowerEvent>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val visibleEvents = events
        .filterNot { it.type == ClocktowerEventType.System }
        .filter { it.phase != ClocktowerPhase.Dawn }
        .sortedByDescending { it.sequence }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (gameKind == GameKind.Clocktower) {
            if (visibleEvents.isEmpty()) {
                item { Text(text("还没有操作记录。", "No game records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                val grouped = visibleEvents
                    .groupBy { clocktowerEventPhaseLabel(it, language) }
                    .entries
                    .sortedByDescending { (_, g) -> g.first().sequence }
                grouped.forEach { (label, group) ->
                    item {
                        Text(
                            label,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                        )
                    }
                    items(group, key = { it.sequence }) { event ->
                        GameRecordRow(
                            title = event.title,
                            detail = event.detail,
                        )
                    }
                }
            }
        } else {
            if (records.isEmpty()) {
                item { Text(text("还没有操作记录。", "No game records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(records.asReversed()) { record ->
                GameRecordRow(
                    title = record.playerName,
                    detail = record.note.orEmpty(),
                    phase = text("第 ${record.round} 轮", "Round ${record.round}"),
                )
            }
        }
    }
}

@Composable
private fun GameRecordRow(title: String, detail: String, phase: String? = null) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (phase != null) {
                Text(phase, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Text(title, fontWeight = FontWeight.Bold)
            if (detail.isNotBlank()) Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ArchivedGameCard(review: ArchivedGameReview, onClick: () -> Unit) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val gameName = when (review.gameKind) {
        GameKind.Undercover -> stringResource(R.string.game_who_is_undercover)
        GameKind.Werewolf -> stringResource(R.string.game_werewolf)
        GameKind.Clocktower -> stringResource(R.string.game_clocktower)
    }
    val pattern = if (language == "en") "MMM d, HH:mm" else "M月d日 HH:mm"
    val date = java.text.SimpleDateFormat(pattern, context.resources.configuration.locales[0])
        .format(java.util.Date(review.archivedAtMillis))
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(gameName, fontWeight = FontWeight.Black)
                Text(
                    "$date · ${if (language == "en") "Round ${review.round}" else "第 ${review.round} 轮"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun ArchivedGameReviewContent(
    review: ArchivedGameReview,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("← ${text("全部历史对局", "All past games")}")
            }
        }
        review.outcome?.let { outcome ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(outcome.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                        Text(outcome.summary, color = MaterialTheme.colorScheme.onSurface)
                        Text(outcome.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Text(text("角色身份", "Roles"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
        items(review.cards) { card ->
            val role = card.hostRoleLabel(context, review.gameKind)
            GameRecordRow(
                title = "#${review.cards.indexOf(card) + 1} ${card.name}",
                detail = role,
                phase = card.eliminatedRound?.let { text("死亡", "Dead") } ?: text("存活", "Alive"),
            )
        }
        item { Text(text("操作记录", "Game log"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
        if (review.gameKind == GameKind.Clocktower) {
            items(review.events.filterNot { it.type == ClocktowerEventType.System }) { event ->
                GameRecordRow(event.title, event.detail, text("第 ${event.round} 轮", "Round ${event.round}"))
            }
        } else {
            items(review.records) { record ->
                GameRecordRow(record.playerName, record.note.orEmpty(), text("第 ${record.round} 轮", "Round ${record.round}"))
            }
        }
        if (review.events.isEmpty() && review.records.isEmpty()) {
            item { Text(text("本局没有操作记录。", "This game has no records."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun FullScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}
