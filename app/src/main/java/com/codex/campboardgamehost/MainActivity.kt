package com.codex.campboardgamehost

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionKind
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.history.InformationReferenceExtractor
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationService
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState
import com.codex.campboardgamehost.clocktower.recommendation.AutomaticStorytellerSelector
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationCandidate
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationRecommender
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationRegistration
import com.codex.campboardgamehost.clocktower.recommendation.MayorRedirectRecommender
import com.codex.campboardgamehost.clocktower.recommendation.DemonSuccessorRecommender
import com.codex.campboardgamehost.clocktower.recommendation.RegistrationDetail
import com.codex.campboardgamehost.clocktower.recommendation.SpecialRegistrationContext
import com.codex.campboardgamehost.clocktower.recommendation.SpecialRegistrationRecommender
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableCategoricalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableCategoricalInformationRecommender
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.recommendation.UnreliableNumberInformationRecommender
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.RegistrationInteractionRules
import kotlinx.coroutines.Dispatchers
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampBoardGameHostApp()
        }
    }
}

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

private enum class GameKind {
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

private enum class WerewolfJudgeStep {
    Wolves,
    Seer,
    Witch,
    Hunter,
    Dawn,
    DayVote,
}

private enum class LastWordsMode {
    None,
    FirstDay,
    FirstTwoDays,
    Always,
}

private data class WerewolfTemplate(
    val playerCount: Int,
    val werewolfCount: Int,
    val includeSeer: Boolean,
    val includeWitch: Boolean,
    val includeHunter: Boolean,
)

private data class WordPair(
    val civilianWord: String,
    val undercoverWord: String,
    val category: String,
)

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

private data class EliminationRecord(
    val round: Int,
    val playerName: String,
    val note: String? = null,
)

private data class GameOutcome(
    val title: String,
    val summary: String,
    val reason: String,
)

private enum class ClocktowerEventType {
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

private data class ClocktowerEvent(
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

private enum class ClocktowerPhase {
    FirstNight,
    Dawn,
    Day,
    Night,
}

private enum class ClocktowerDayMode {
    Overview,
    Slayer,
    Artist,
    Klutz,
    Nomination,
    Vote,
    EndConfirm,
    ExecutionResult,
}

private enum class ClocktowerNightAction {
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

private enum class ClocktowerDisplayKind {
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

private val chineseWordPairs = listOf(
    WordPair("帐篷", "天幕", "露营"),
    WordPair("营地灯", "手电筒", "露营"),
    WordPair("睡袋", "防潮垫", "露营"),
    WordPair("烤肉", "火锅", "食物"),
    WordPair("可乐", "雪碧", "饮料"),
    WordPair("咖啡", "奶茶", "饮料"),
    WordPair("苹果", "梨", "水果"),
    WordPair("西瓜", "哈密瓜", "水果"),
    WordPair("牙刷", "毛巾", "生活"),
    WordPair("雨伞", "雨衣", "生活"),
    WordPair("高铁", "地铁", "交通"),
    WordPair("飞机", "热气球", "交通"),
    WordPair("猫", "狗", "动物"),
    WordPair("狮子", "老虎", "动物"),
    WordPair("医生", "护士", "职业"),
    WordPair("老师", "教练", "职业"),
)

private val englishWordPairs = listOf(
    WordPair("Tent", "Tarp", "Camping"),
    WordPair("Lantern", "Flashlight", "Camping"),
    WordPair("Sleeping bag", "Sleeping pad", "Camping"),
    WordPair("Barbecue", "Hot pot", "Food"),
    WordPair("Cola", "Lemon-lime soda", "Drink"),
    WordPair("Coffee", "Milk tea", "Drink"),
    WordPair("Apple", "Pear", "Fruit"),
    WordPair("Watermelon", "Cantaloupe", "Fruit"),
    WordPair("Toothbrush", "Towel", "Daily"),
    WordPair("Umbrella", "Raincoat", "Daily"),
    WordPair("High-speed train", "Subway", "Transport"),
    WordPair("Airplane", "Hot air balloon", "Transport"),
    WordPair("Cat", "Dog", "Animal"),
    WordPair("Lion", "Tiger", "Animal"),
    WordPair("Doctor", "Nurse", "Job"),
    WordPair("Teacher", "Coach", "Job"),
)

private fun Context.playerName(number: Int): String = getString(R.string.default_player_name_format, number)

private const val PREFS_NAME = "camp_board_game_host"
private const val COMMON_PLAYERS_KEY = "common_players"
private const val LANGUAGE_MODE_KEY = "language_mode"
private const val AUTOMATIC_STORYTELLER_INFO_KEY = "automatic_storyteller_info"
private const val ACTIVE_GAME_STATE_KEY = "active_game_state"
private const val GAME_HISTORY_KEY = "game_history"
private const val ACTIVE_GAME_STATE_VERSION = 1
private const val MAX_GAME_HISTORY = 20
private const val MIN_PLAYERS = 3
private const val MIN_WEREWOLF_PLAYERS = 4
private const val MIN_CLOCKTOWER_PLAYERS = 5
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

private fun Context.loadAutomaticStorytellerInfo(): Boolean =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(AUTOMATIC_STORYTELLER_INFO_KEY, false)

private fun Context.saveAutomaticStorytellerInfo(enabled: Boolean) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(AUTOMATIC_STORYTELLER_INFO_KEY, enabled)
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

private fun Context.saveActiveGameState(snapshot: JSONObject) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(ACTIVE_GAME_STATE_KEY, snapshot.toString())
        .commit()
}

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
    if (json.optInt("version", 0) != ACTIVE_GAME_STATE_VERSION) return null
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

private fun wordPairsFor(language: String): List<WordPair> {
    return if (language == "en") englishWordPairs else chineseWordPairs
}

private fun Role.werewolfDescription(context: Context): String = when (this) {
    Role.Villager -> context.getString(R.string.role_villager_desc)
    Role.Werewolf -> context.getString(R.string.role_werewolf_desc)
    Role.Seer -> context.getString(R.string.role_seer_desc)
    Role.Witch -> context.getString(R.string.role_witch_desc)
    Role.Hunter -> context.getString(R.string.role_hunter_desc)
    else -> ""
}

private fun werewolfRolesFor(
    playerCount: Int,
    werewolfCount: Int,
    includeSeer: Boolean,
    includeWitch: Boolean,
    includeHunter: Boolean,
): List<Role> {
    val specialRoles = buildList {
        if (includeSeer) add(Role.Seer)
        if (includeWitch) add(Role.Witch)
        if (includeHunter) add(Role.Hunter)
    }
    val villagerCount = (playerCount - werewolfCount - specialRoles.size).coerceAtLeast(0)
    return buildList {
        repeat(werewolfCount) { add(Role.Werewolf) }
        addAll(specialRoles)
        repeat(villagerCount) { add(Role.Villager) }
    }.shuffled()
}

private val werewolfTemplates = listOf(
    WerewolfTemplate(playerCount = 4, werewolfCount = 1, includeSeer = true, includeWitch = false, includeHunter = false),
    WerewolfTemplate(playerCount = 5, werewolfCount = 1, includeSeer = true, includeWitch = true, includeHunter = false),
    WerewolfTemplate(playerCount = 6, werewolfCount = 2, includeSeer = true, includeWitch = true, includeHunter = false),
    WerewolfTemplate(playerCount = 7, werewolfCount = 2, includeSeer = true, includeWitch = true, includeHunter = false),
    WerewolfTemplate(playerCount = 8, werewolfCount = 2, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 9, werewolfCount = 3, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 10, werewolfCount = 3, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 11, werewolfCount = 3, includeSeer = true, includeWitch = true, includeHunter = true),
    WerewolfTemplate(playerCount = 12, werewolfCount = 4, includeSeer = true, includeWitch = true, includeHunter = true),
)

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

private val completeTroubleBrewingRoles = (troubleBrewingRoles + listOf(
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

private fun ClocktowerTeam.label(context: Context): String = when (this) {
    ClocktowerTeam.Townsfolk -> context.getString(R.string.clocktower_team_townsfolk)
    ClocktowerTeam.Outsider -> context.getString(R.string.clocktower_team_outsider)
    ClocktowerTeam.Minion -> context.getString(R.string.clocktower_team_minion)
    ClocktowerTeam.Demon -> context.getString(R.string.clocktower_team_demon)
}

private fun ClocktowerRole.nameFor(language: String): String = if (language == "en") enName else zhName

private fun ClocktowerRole.descriptionFor(language: String): String = if (language == "en") enDescription else zhDescription

private fun ClocktowerScript.nameFor(language: String): String = when (this) {
    ClocktowerScript.TroubleBrewing -> if (language == "en") "Trouble Brewing" else "暗流涌动"
    ClocktowerScript.NoGreaterJoy -> "No Greater Joy"
}

private fun defaultClocktowerScriptFor(playerCount: Int): ClocktowerScript =
    if (playerCount in 5..6) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing

private fun canStartClocktowerScript(script: ClocktowerScript): Boolean =
    script == ClocktowerScript.TroubleBrewing || script == ClocktowerScript.NoGreaterJoy

private fun LastWordsMode.labelResId(): Int = when (this) {
    LastWordsMode.None -> R.string.last_words_none
    LastWordsMode.FirstDay -> R.string.last_words_first_day
    LastWordsMode.FirstTwoDays -> R.string.last_words_first_two_days
    LastWordsMode.Always -> R.string.last_words_always
}

private fun clocktowerDistribution(playerCount: Int): Map<ClocktowerTeam, Int> {
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
                .firstOrNull { candidate -> candidate !in actualRoles }
                ?: townsfolkPool.random()
            ClocktowerAssignment(actualRole = role, shownRole = fakeRole)
        } else {
            ClocktowerAssignment(actualRole = role, shownRole = role)
        }
    }
}

@Composable
private fun CampBoardGameHostApp() {
    val baseContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }
    var automaticStorytellerInfo by remember { mutableStateOf(baseContext.loadAutomaticStorytellerInfo()) }
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
    var clocktowerPendingNightDeath by remember { mutableStateOf<String?>(null) }
    var clocktowerSelectedExecution by remember { mutableStateOf<String?>(null) }
    var clocktowerPoisonTarget by remember { mutableStateOf<String?>(null) }
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
    var clocktowerMayorRedirectTarget by remember { mutableStateOf<String?>(null) }
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
    var showResults by remember { mutableStateOf(false) }
    var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }
    var newCommonPlayerName by remember { mutableStateOf("") }
    val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }
    val playerNames = remember { mutableStateListOf<String>() }
    val cards = remember { mutableStateListOf<PlayerCard>() }
    val records = remember { mutableStateListOf<EliminationRecord>() }
    val clocktowerEvents = remember { mutableStateListOf<ClocktowerEvent>() }
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
    val playerCount = playerNames.size

    fun addClocktowerEvent(
        type: ClocktowerEventType,
        title: String,
        detail: String,
        playerNames: List<String> = emptyList(),
        eventPhase: ClocktowerPhase = clocktowerPhase,
        eventRound: Int = round,
    ) {
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
        putNullableString("clocktowerPendingNightDeath", clocktowerPendingNightDeath)
        putNullableString("clocktowerSelectedExecution", clocktowerSelectedExecution)
        putNullableString("clocktowerPoisonTarget", clocktowerPoisonTarget)
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
        putNullableString("clocktowerMayorRedirectTarget", clocktowerMayorRedirectTarget)
        putNullableString("clocktowerPendingNewDemonName", clocktowerPendingNewDemonName)
        putNullableString("clocktowerDemonSuccessorTarget", clocktowerDemonSuccessorTarget)
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
    }

    fun persistActiveGameStateIfNeeded() {
        if (screen.isActiveGameScreen() && cards.isNotEmpty()) {
            baseContext.saveActiveGameState(activeGameSnapshotJson())
        }
    }

    fun restoreSavedGame() {
        val json = baseContext.loadActiveGameStateJson() ?: return
        val restored = runCatching {
            if (json.optInt("version", 0) != ACTIVE_GAME_STATE_VERSION) {
                error("Unsupported active game state version")
            }
            val restoredGameKind = enumByName<GameKind>(json.optNullableString("currentGameKind"))
                ?: error("Missing game kind")
            val restoredCards = json.optJSONArray("cards")?.toPlayerCards().orEmpty()
            if (restoredCards.isEmpty()) error("Missing player cards")
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
            val savedClocktowerScript = enumByName<ClocktowerScript>(json.optNullableString("currentClocktowerScript"))
                ?: defaultClocktowerScriptFor(localizedRestoredCards.size)
            val restoredHasNoGreaterJoyOnlyRole = localizedRestoredCards.any {
                it.clocktowerRole?.enName in setOf("Clockmaker", "Chambermaid", "Artist", "Sage", "Klutz")
            }
            currentClocktowerScript = if (localizedRestoredCards.size in 5..6 && restoredHasNoGreaterJoyOnlyRole) {
                ClocktowerScript.NoGreaterJoy
            } else {
                savedClocktowerScript
            }
            clocktowerPendingNightDeath = json.optNullableString("clocktowerPendingNightDeath")
            clocktowerSelectedExecution = json.optNullableString("clocktowerSelectedExecution")
            clocktowerPoisonTarget = json.optNullableString("clocktowerPoisonTarget")
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
            clocktowerMayorRedirectTarget = json.optNullableString("clocktowerMayorRedirectTarget")
            clocktowerPendingNewDemonName = json.optNullableString("clocktowerPendingNewDemonName")
            clocktowerDemonSuccessorTarget = json.optNullableString("clocktowerDemonSuccessorTarget")
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

    val latestPersistActiveGameState by rememberUpdatedState { persistActiveGameStateIfNeeded() }

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
        persistActiveGameStateIfNeeded()
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

    fun resetDealState(nextGameKind: GameKind, clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing) {
        clearSavedGameState()
        currentGameKind = nextGameKind
        records.clear()
        clocktowerEvents.clear()
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
        clocktowerPendingNightDeath = null
        clocktowerSelectedExecution = null
        clocktowerPoisonTarget = null
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
        clocktowerMayorRedirectTarget = null
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
        cards.clear()
        cards.addAll(playerNames.mapIndexed { index, name ->
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
        })
        resetDealState(GameKind.Clocktower, script)
    }

    fun archiveCurrentGameForRestart(): Boolean {
        if (cards.isEmpty()) return false
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
            it.clocktowerRole?.enName == "Scarlet Woman" && it.name != clocktowerPoisonTarget
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
                        .fillMaxSize()
                        .safeDrawingPadding(),
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
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        commonPlayers = commonPlayers,
                        newCommonPlayerName = newCommonPlayerName,
                        onLanguageModeChange = { nextMode ->
                            languageMode = nextMode
                            baseContext.saveLanguageMode(nextMode)
                        },
                        onAutomaticStorytellerInfoChange = { enabled ->
                            automaticStorytellerInfo = enabled
                            baseContext.saveAutomaticStorytellerInfo(enabled)
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
                        cards = cards,
                        records = records,
                        events = clocktowerEvents,
                        script = currentClocktowerScript,
                        phase = clocktowerPhase,
                        round = round,
                        pendingNightDeath = clocktowerPendingNightDeath,
                        selectedExecution = clocktowerSelectedExecution,
                        poisonTarget = clocktowerPoisonTarget,
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
                        monkProtectedTarget = clocktowerMonkProtectedTarget,
                        mayorRedirectTarget = clocktowerMayorRedirectTarget,
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
                        onPhaseChange = { nextPhase ->
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
                            clocktowerPendingNightDeath = selected
                            val livingDemonName = cards.firstOrNull {
                                it.eliminatedRound == null && it.clocktowerTeam == ClocktowerTeam.Demon
                            }?.name
                            if (selected != livingDemonName) {
                                clocktowerDemonSuccessorTarget = null
                            }
                        },
                        onSelectExecution = { clocktowerSelectedExecution = it },
                        onSelectPoisonTarget = { clocktowerPoisonTarget = it },
                        onSelectFortuneTellerFirst = { clocktowerFortuneTellerFirst = it },
                        onSelectFortuneTellerSecond = { clocktowerFortuneTellerSecond = it },
                        onSelectChambermaidFirst = { clocktowerChambermaidFirst = it },
                        onSelectChambermaidSecond = { clocktowerChambermaidSecond = it },
                        onSelectRavenkeeperTarget = { clocktowerRavenkeeperTarget = it },
                        onSelectRedHerring = { clocktowerRedHerring = it },
                        onApplyRecommendation = { plan ->
                            plan.decisions.filterIsInstance<StorytellerDecision.RedHerring>().singleOrNull()?.let { decision ->
                                clocktowerRedHerring = cards.getOrNull(decision.seat - 1)?.name
                            }
                            plan.decisions.filterIsInstance<StorytellerDecision.DrunkShownRole>().singleOrNull()?.let { decision ->
                                val drunkPlayer = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" }
                                val shownRole = clocktowerRolesForScript(currentClocktowerScript)
                                    .firstOrNull { it.enName == decision.role.value }
                                if (drunkPlayer != null && shownRole != null) {
                                    setClocktowerShownRole(drunkPlayer.name, shownRole)
                                }
                            }
                            val drunkInfo = plan.decisions
                                .filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>()
                                .singleOrNull()
                            clocktowerRecommendedDrunkInvestigatorRoleName = drunkInfo?.shownMinion?.value
                            clocktowerRecommendedDrunkInvestigatorSeats = drunkInfo?.candidateSeats.orEmpty()
                            clocktowerRecommendedDemonBluffRoleNames = plan.decisions
                                .filterIsInstance<StorytellerDecision.DemonBluffs>()
                                .singleOrNull()
                                ?.roles
                                ?.map(RoleId::value)
                                .orEmpty()
                        },
                        onSelectButlerMaster = { clocktowerButlerMaster = it },
                        onSelectMonkProtectedTarget = { clocktowerMonkProtectedTarget = it },
                        onSelectMayorRedirectTarget = { clocktowerMayorRedirectTarget = it },
                        onSelectDemonSuccessor = { clocktowerDemonSuccessorTarget = it },
                        onConfirmNewDemon = {
                            clocktowerPendingNewDemonName = null
                            clocktowerPhase = ClocktowerPhase.Dawn
                            resetClocktowerNightFlow()
                        },
                        onSelectKlutzChoice = { clocktowerKlutzChoiceName = it },
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
                                        summary = "呆瓜选择了邪恶玩家，善良阵营失败。",
                                        reason = "${playerSeatLabel(cards, clocktowerPendingKlutzName)} 选择了 ${playerSeatLabel(cards, choice)}。",
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
                                        clocktowerPhase = ClocktowerPhase.Night
                                    }
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
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
                                records.add(EliminationRecord(round, claimantName, "艺术家提问已处理"))
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
                            }
                            if (canUseSlayerAbility && targetIndex >= 0 && targetCard != null && targetCard.eliminatedRound == null && targetRegistersAsDemon) {
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
                                    clocktowerPhase = ClocktowerPhase.Night
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                    clocktowerNightStartedState.value = true
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
                            resetClocktowerNightFlow()
                            resetClocktowerDayFlow()
                        },
                        onConfirmDay = {
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
                                clocktowerPhase = ClocktowerPhase.Night
                                resetClocktowerDayFlow()
                                resetClocktowerNightFlow()
                                clocktowerNightStartedState.value = true
                            }
                            clocktowerSelectedExecution = null
                        },
                        onConfirmNight = {
                            val demonPoisonedTonight = clocktowerPoisonTarget?.let { name ->
                                cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
                            } == true
                            var nightKlutzName: String? = null
                            var newDemonName: String? = null
                            val originalDeathName = clocktowerPendingNightDeath
                            val originalDeathCard = originalDeathName?.let { name -> cards.firstOrNull { it.name == name } }
                            val mayorCanRedirect = originalDeathCard?.clocktowerRole?.enName == "Mayor" &&
                                originalDeathCard.eliminatedRound == null &&
                                clocktowerPoisonTarget != originalDeathName &&
                                !demonPoisonedTonight
                            val resolvedDeathName = if (mayorCanRedirect) {
                                clocktowerMayorRedirectTarget ?: originalDeathName
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
                                        listOfNotNull(clocktowerPoisonTarget, targetName),
                                    )
                                }
                                clocktowerPendingNightDeath = null
                            }
                            if (deathName != null) {
                                clocktowerPendingNightDeath = deathName
                                val index = cards.indexOfFirst { it.name == deathName }
                                val nightDeathCard = cards.getOrNull(index)
                                if (index >= 0 && nightDeathCard != null && nightDeathCard.eliminatedRound == null) {
                                    val protectedByMonk = clocktowerMonkProtectedTarget == deathName
                                    val protectedBySoldier = nightDeathCard.clocktowerRole?.enName == "Soldier"
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
                            clocktowerPoisonTarget = PoisonEffectLifecycle.afterNight(
                                target = clocktowerPoisonTarget,
                                poisonerAlive = cards.any {
                                    it.eliminatedRound == null && it.clocktowerRole?.enName == "Poisoner"
                                },
                            )
                            clocktowerFortuneTellerFirst = null
                            clocktowerFortuneTellerSecond = null
                            clocktowerChambermaidFirst = null
                            clocktowerChambermaidSecond = null
                            clocktowerRavenkeeperTarget = null
                            clocktowerMonkProtectedTarget = null
                            clocktowerMayorRedirectTarget = null
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
    automaticStorytellerInfo: Boolean,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onAutomaticStorytellerInfoChange: (Boolean) -> Unit,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAutomaticStorytellerInfoChange(!automaticStorytellerInfo) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = automaticStorytellerInfo,
                        onCheckedChange = onAutomaticStorytellerInfoChange,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (language == "en") "Automatic Storyteller information" else "全自动说书人信息",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (language == "en") {
                                "Automatically applies the balanced recommendation during play and hides alternative rulings."
                            } else {
                                "游戏中自动采用平衡推荐，不再显示其他信息与裁定选项。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
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
private fun UndercoverSettingsScreen(
    playerCount: Int,
    undercoverCount: Int,
    includeBlank: Boolean,
    onUndercoverCountChange: (Int) -> Unit,
    onIncludeBlankChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val maxUndercover = if (includeBlank) playerCount - 2 else playerCount - 1
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GameSettingsHeader(
                title = stringResource(R.string.game_who_is_undercover),
                subtitle = stringResource(R.string.game_settings_subtitle, playerCount),
                onBack = onBack,
            )
        }
        item {
            SettingsPanel(
                playerCount = playerCount,
                undercoverCount = undercoverCount,
                includeBlank = includeBlank,
                maxUndercover = maxUndercover,
                onUndercoverCountChange = onUndercoverCountChange,
                onIncludeBlankChange = onIncludeBlankChange,
            )
        }
        item {
            Button(
                onClick = onStart,
                enabled = playerCount >= MIN_PLAYERS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.start_dealing))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WerewolfSettingsScreen(
    playerCount: Int,
    werewolfCount: Int,
    includeSeer: Boolean,
    includeWitch: Boolean,
    includeHunter: Boolean,
    lastWordsMode: LastWordsMode,
    onWerewolfCountChange: (Int) -> Unit,
    onIncludeSeerChange: (Boolean) -> Unit,
    onIncludeWitchChange: (Boolean) -> Unit,
    onIncludeHunterChange: (Boolean) -> Unit,
    onLastWordsModeChange: (LastWordsMode) -> Unit,
    onApplyTemplate: (WerewolfTemplate) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val specialCount = listOf(includeSeer, includeWitch, includeHunter).count { it }
    val villagerCount = playerCount - werewolfCount - specialCount
    val roleTotal = werewolfCount + specialCount + villagerCount.coerceAtLeast(0)
    val canStart = playerCount >= MIN_WEREWOLF_PLAYERS && villagerCount >= 0
    val recommendedTemplates = werewolfTemplates.filter { it.playerCount == playerCount }
    val otherTemplates = werewolfTemplates.filter { it.playerCount != playerCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GameSettingsHeader(
                title = stringResource(R.string.game_werewolf),
                subtitle = stringResource(R.string.game_settings_subtitle, playerCount),
                onBack = onBack,
            )
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
                    Text(stringResource(R.string.werewolf_template_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.werewolf_template_hint), color = Color(0xFF6F7B74))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendedTemplates.forEach { template ->
                            Button(
                                onClick = { onApplyTemplate(template) },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(templateLabel(template))
                            }
                        }
                        otherTemplates.forEach { template ->
                            OutlinedButton(
                                onClick = { onApplyTemplate(template) },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(templateLabel(template))
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(stringResource(R.string.werewolf_role_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.werewolf_role_summary, roleTotal, playerCount, villagerCount.coerceAtLeast(0)), color = Color(0xFF6F7B74))
                    StepperRow(
                        label = stringResource(R.string.role_werewolf),
                        value = werewolfCount,
                        range = 1..(playerCount - specialCount).coerceAtLeast(1),
                        onChange = onWerewolfCountChange,
                    )
                    RoleToggleRow(
                        roleName = stringResource(R.string.role_seer),
                        description = stringResource(R.string.role_seer_desc),
                        checked = includeSeer,
                        onCheckedChange = onIncludeSeerChange,
                    )
                    RoleToggleRow(
                        roleName = stringResource(R.string.role_witch),
                        description = stringResource(R.string.role_witch_desc),
                        checked = includeWitch,
                        onCheckedChange = onIncludeWitchChange,
                    )
                    RoleToggleRow(
                        roleName = stringResource(R.string.role_hunter),
                        description = stringResource(R.string.role_hunter_desc),
                        checked = includeHunter,
                        onCheckedChange = onIncludeHunterChange,
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(stringResource(R.string.role_villager), fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.villager_auto_fill_hint), color = Color(0xFF6F7B74))
                        }
                        Text(villagerCount.coerceAtLeast(0).toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    Text(stringResource(R.string.last_words_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.last_words_settings_hint), color = Color(0xFF6F7B74))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LastWordsMode.entries.forEach { mode ->
                            if (mode == lastWordsMode) {
                                Button(
                                    onClick = { onLastWordsModeChange(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(stringResource(mode.labelResId()))
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onLastWordsModeChange(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(stringResource(mode.labelResId()))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = onStart,
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (canStart) stringResource(R.string.start_dealing) else stringResource(R.string.werewolf_roles_invalid))
            }
        }
    }
}

@Composable
private fun templateLabel(template: WerewolfTemplate): String {
    val specials = buildList {
        if (template.includeSeer) add(stringResource(R.string.role_seer_short))
        if (template.includeWitch) add(stringResource(R.string.role_witch_short))
        if (template.includeHunter) add(stringResource(R.string.role_hunter_short))
    }.joinToString("")
        .ifBlank { stringResource(R.string.no_special_roles_short) }
    return stringResource(R.string.werewolf_template_label_format, template.playerCount, template.werewolfCount, specials)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClocktowerSettingsScreen(
    playerCount: Int,
    playerNames: List<String>,
    selectedScript: ClocktowerScript,
    onScriptChange: (ClocktowerScript) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val context = LocalContext.current
    var step by remember(playerCount) { mutableStateOf(0) }
    val distribution = clocktowerDistribution(playerCount)
    val showScriptChoice = playerCount in 5..6
    val effectiveScript = if (showScriptChoice) selectedScript else ClocktowerScript.TroubleBrewing
    val canStart = playerCount >= MIN_CLOCKTOWER_PLAYERS && canStartClocktowerScript(effectiveScript)
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val stepTitles = listOf(
        text("确认玩家", "Confirm players"),
        text("选择剧本", "Choose script"),
        text("开局确认", "Final review"),
    )

    ClocktowerDarkTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { if (step == 0) onBack() else step -= 1 }) {
                            Text(stringResource(R.string.back))
                        }
                        Text(
                            text = text("配置游戏", "GAME SETUP"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            text = "${step + 1} / 3",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        if (index <= step) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(50),
                                    ),
                            )
                        }
                    }
                    Text(
                        text = stepTitles[step],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = when (step) {
                            0 -> text("核对围桌顺序。座位号将用于整局主持。", "Check the seating order. Seat numbers stay with the game.")
                            1 -> text("剧本决定本局可出现的角色和夜间流程。", "The script defines the character pool and night order.")
                            else -> text("最后核对一次；开始后将进入逐人发牌。", "Review everything once more before dealing begins.")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when (step) {
                0 -> item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = text("$playerCount 名玩家", "$playerCount players"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            playerNames.forEachIndexed { index, name ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                }
                                if (index < playerNames.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
                        }
                    }
                }

                1 -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (showScriptChoice) {
                            ClocktowerScript.entries.forEach { script ->
                                Card(
                                    onClick = { onScriptChange(script) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (script == effectiveScript) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (script == effectiveScript) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(script.nameFor(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = when (script) {
                                                ClocktowerScript.TroubleBrewing ->
                                                    text("经典入门剧本，角色互动完整，适合标准人数。", "The classic introductory script with the full core interaction set.")
                                                ClocktowerScript.NoGreaterJoy ->
                                                    text("为 5–6 人小局准备的精简角色组合。", "A focused character set designed for 5–6 players.")
                                            },
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        if (script == effectiveScript) {
                                            Text(
                                                text = text("已选择", "SELECTED"),
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(effectiveScript.nameFor(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = text(
                                            "7 人及以上固定使用暗流涌动，确保角色数量和夜间流程完整。",
                                            "Games with 7 or more players use Trouble Brewing for the complete distribution and night flow.",
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                else -> item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(text("准备就绪", "Ready to begin"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            ClocktowerSetupSummaryRow(text("玩家", "Players"), text("$playerCount 人", "$playerCount"))
                            ClocktowerSetupSummaryRow(text("剧本", "Script"), effectiveScript.nameFor(language))
                            ClocktowerSetupSummaryRow(
                                text("阵营", "Teams"),
                                ClocktowerTeam.entries.joinToString(" · ") { team ->
                                    "${team.label(context)} ${distribution[team] ?: 0}"
                                },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Text(
                                text = text(
                                    "点击开始后才会随机生成角色并保存本局。返回上一步不会丢失当前选择。",
                                    "Characters are randomized and saved only after you start. Going back keeps your choices.",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (step < 2) step += 1 else onStart()
                    },
                    enabled = if (step == 2) canStart else true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (step < 2) {
                            text("下一步", "Continue")
                        } else if (canStart) {
                            stringResource(R.string.start_dealing)
                        } else {
                            stringResource(R.string.need_clocktower_min_players, MIN_CLOCKTOWER_PLAYERS)
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (step == 0) {
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(text("返回首页修改玩家", "Edit players on home screen"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerSetupSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GameSettingsHeader(
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
private fun RoleToggleRow(
    roleName: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.weight(1f)) {
            Text(roleName, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SettingsPanel(
    playerCount: Int,
    undercoverCount: Int,
    includeBlank: Boolean,
    maxUndercover: Int,
    onUndercoverCountChange: (Int) -> Unit,
    onIncludeBlankChange: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.game_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.player_count_summary, playerCount), color = Color(0xFF6F7B74))
            StepperRow(
                label = stringResource(R.string.undercover_count),
                value = undercoverCount,
                range = 1..maxUndercover.coerceAtLeast(1),
                onChange = onUndercoverCountChange,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeBlank, onCheckedChange = onIncludeBlankChange)
                Text(stringResource(R.string.include_blank))
            }
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
private fun StepperRow(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WerewolfJudgeScreen(
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    nightNumber: Int,
    stepIndex: Int,
    pendingNightDeath: String?,
    seerCheckTarget: String?,
    witchSaveUsed: Boolean,
    witchPoisonUsed: Boolean,
    witchSavedTonight: Boolean,
    witchPoisonTarget: String?,
    hunterShotTarget: String?,
    selectedDayExile: String?,
    gameOutcome: GameOutcome?,
    lastWordsPromptNames: List<String>,
    onStepIndexChange: (Int) -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onSelectSeerCheck: (String?) -> Unit,
    onToggleWitchSave: (Boolean) -> Unit,
    onSelectWitchPoison: (String?) -> Unit,
    onSelectHunterShot: (String?) -> Unit,
    onConfirmDawn: (List<Pair<String, String>>) -> Unit,
    onSelectDayExile: (String?) -> Unit,
    onConfirmDayExile: () -> Unit,
    onDismissLastWordsPrompt: () -> Unit,
    onShowResults: () -> Unit,
) {
    val steps = buildList {
        add(WerewolfJudgeStep.Wolves)
        if (cards.any { it.role == Role.Seer }) add(WerewolfJudgeStep.Seer)
        if (cards.any { it.role == Role.Witch }) add(WerewolfJudgeStep.Witch)
        if (cards.any { it.role == Role.Hunter }) add(WerewolfJudgeStep.Hunter)
        add(WerewolfJudgeStep.Dawn)
        add(WerewolfJudgeStep.DayVote)
    }
    val currentIndex = stepIndex.coerceIn(0, steps.lastIndex)
    val currentStep = steps[currentIndex]
    val aliveCards = cards.filter { it.eliminatedRound == null }
    val wolfAttackDeath = pendingNightDeath?.takeUnless { witchSavedTonight }
    val baseNightDeathEvents = buildList {
        if (wolfAttackDeath != null) add(wolfAttackDeath to stringResource(R.string.werewolf_record_night_death))
        if (witchPoisonTarget != null) add(witchPoisonTarget to stringResource(R.string.werewolf_record_witch_poison))
    }.distinctBy { it.first }
    val baseNightDeathNames = baseNightDeathEvents.map { it.first }
    val hunterDiesTonight = baseNightDeathNames.any { name -> cards.firstOrNull { it.name == name }?.role == Role.Hunter }
    val selectedDayExileCard = cards.firstOrNull { it.name == selectedDayExile }
    val hunterCanShootAfterDayExile = selectedDayExileCard?.role == Role.Hunter
    val hunterShotEvent = hunterShotTarget
        ?.takeIf { hunterDiesTonight || hunterCanShootAfterDayExile }
        ?.let { it to stringResource(R.string.werewolf_record_hunter_shot) }
    val nightDeathEvents = (baseNightDeathEvents + listOfNotNull(hunterShotEvent)).distinctBy { it.first }
    val nightDeathNames = nightDeathEvents.map { it.first }

    fun roleCards(role: Role): List<PlayerCard> = cards.filter { it.role == role }

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
                    Text(stringResource(R.string.werewolf_judge_assistant), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        gameOutcome?.title ?: stringResource(R.string.werewolf_night_format, nightNumber),
                        color = Color(0xFF5C6A63),
                    )
                }
            }
        }

        if (lastWordsPromptNames.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4DC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.last_words_prompt_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.last_words_prompt_names, lastWordsPromptNames.joinToString(stringResource(R.string.name_separator))))
                        Text(stringResource(R.string.last_words_prompt_hint), color = Color(0xFF6F7B74))
                        Button(
                            onClick = onDismissLastWordsPrompt,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.got_it))
                        }
                    }
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
            HostProgressCard(
                title = stringResource(R.string.host_current_step),
                subtitle = stringResource(currentStep.titleResId()),
                progress = stringResource(R.string.host_step_progress_format, currentIndex + 1, steps.size),
            )
        }

        item {
            HostScriptCard(
                title = stringResource(currentStep.titleResId()),
                script = stringResource(currentStep.scriptResId()),
                action = stringResource(currentStep.actionResId()),
            ) {
                    when (currentStep) {
                        WerewolfJudgeStep.Wolves -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_werewolf), players = roleCards(Role.Werewolf))
                            HostActionSection(
                                title = stringResource(R.string.werewolf_choose_night_death),
                                helper = stringResource(R.string.host_choose_one_player_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = pendingNightDeath,
                                    onSelect = { onSelectNightDeath(if (pendingNightDeath == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                        }

                        WerewolfJudgeStep.Seer -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_seer), players = roleCards(Role.Seer))
                            HostActionSection(
                                title = stringResource(R.string.werewolf_choose_seer_check),
                                helper = stringResource(R.string.host_choose_one_player_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = seerCheckTarget,
                                    onSelect = { onSelectSeerCheck(if (seerCheckTarget == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                            seerCheckTarget?.let { targetName ->
                                val target = cards.firstOrNull { it.name == targetName }
                                val result = if (target?.role == Role.Werewolf) {
                                    stringResource(R.string.seer_result_werewolf)
                                } else {
                                    stringResource(R.string.seer_result_good)
                                }
                                Text(stringResource(R.string.seer_result_format, targetName, result), color = Color(0xFF2F5D50), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        WerewolfJudgeStep.Witch -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_witch), players = roleCards(Role.Witch))
                            HostInstructionBlock(
                                label = stringResource(R.string.host_current_result_label),
                                text = pendingNightDeath?.let { stringResource(R.string.werewolf_pending_death_format, it) }
                                    ?: stringResource(R.string.werewolf_no_pending_death),
                                backgroundColor = Color(0xFFFFFCF6),
                                textColor = Color(0xFF5C6A63),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = witchSavedTonight,
                                    onCheckedChange = onToggleWitchSave,
                                    enabled = !witchSaveUsed && pendingNightDeath != null && gameOutcome == null,
                                )
                                Text(
                                    if (witchSaveUsed) stringResource(R.string.witch_save_used) else stringResource(R.string.witch_use_save),
                                    color = if (witchSaveUsed) Color(0xFF9A4B36) else Color(0xFF1F2925),
                                )
                            }
                            HostActionSection(
                                title = if (witchPoisonUsed) stringResource(R.string.witch_poison_used) else stringResource(R.string.witch_choose_poison),
                                helper = stringResource(R.string.host_optional_action_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != pendingNightDeath || !witchSavedTonight },
                                    selectedName = witchPoisonTarget,
                                    onSelect = { onSelectWitchPoison(if (witchPoisonTarget == it) null else it) },
                                    enabled = !witchPoisonUsed && gameOutcome == null,
                                )
                            }
                        }

                        WerewolfJudgeStep.Hunter -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_hunter), players = roleCards(Role.Hunter))
                            Text(stringResource(R.string.hunter_status_hint), color = Color(0xFF6F7B74))
                        }

                        WerewolfJudgeStep.Dawn -> {
                            if (nightDeathEvents.isEmpty()) {
                                HostInstructionBlock(
                                    label = stringResource(R.string.host_current_result_label),
                                    text = stringResource(R.string.werewolf_no_final_death),
                                    backgroundColor = Color(0xFFFFFCF6),
                                    textColor = Color(0xFF5C6A63),
                                )
                            } else {
                                HostActionSection(title = stringResource(R.string.werewolf_final_deaths)) {
                                    nightDeathEvents.forEach { (name, note) ->
                                        Text(stringResource(R.string.werewolf_death_event_format, name, note), color = Color(0xFF6F7B74))
                                    }
                                }
                            }
                            if (hunterDiesTonight) {
                                HostActionSection(
                                    title = stringResource(R.string.hunter_choose_shot),
                                    helper = stringResource(R.string.host_optional_action_hint),
                                ) {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name !in nightDeathNames },
                                        selectedName = hunterShotTarget,
                                        onSelect = { onSelectHunterShot(if (hunterShotTarget == it) null else it) },
                                        enabled = gameOutcome == null,
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    onConfirmDawn(nightDeathEvents)
                                    onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex))
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.werewolf_confirm_dawn))
                            }
                        }

                        WerewolfJudgeStep.DayVote -> {
                            HostActionSection(
                                title = stringResource(R.string.werewolf_choose_day_exile),
                                helper = stringResource(R.string.host_skip_allowed_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = selectedDayExile,
                                    onSelect = { onSelectDayExile(if (selectedDayExile == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                            if (hunterCanShootAfterDayExile) {
                                HostActionSection(
                                    title = stringResource(R.string.hunter_choose_shot),
                                    helper = stringResource(R.string.host_optional_action_hint),
                                ) {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name != selectedDayExile },
                                        selectedName = hunterShotTarget,
                                        onSelect = { onSelectHunterShot(if (hunterShotTarget == it) null else it) },
                                        enabled = gameOutcome == null,
                                    )
                                }
                            }
                            Button(
                                onClick = onConfirmDayExile,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (selectedDayExile == null) stringResource(R.string.werewolf_no_exile_next_night) else stringResource(R.string.werewolf_confirm_day_exile))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onStepIndexChange((currentIndex - 1).coerceAtLeast(0)) },
                            enabled = currentIndex > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.previous_step))
                        }
                        Button(
                            onClick = { onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex)) },
                            enabled = currentIndex < steps.lastIndex,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.next_step))
                        }
                    }
            }
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.player_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(cards) { card ->
            WerewolfPlayerStatusRow(card)
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.elimination_records), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text(stringResource(R.string.no_eliminations), color = Color(0xFF6F7B74))
            }
        }

        items(records) { record ->
            Text(record.displayText(), modifier = Modifier.padding(vertical = 4.dp))
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

private fun WerewolfJudgeStep.titleResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_step_wolves_title
    WerewolfJudgeStep.Seer -> R.string.werewolf_step_seer_title
    WerewolfJudgeStep.Witch -> R.string.werewolf_step_witch_title
    WerewolfJudgeStep.Hunter -> R.string.werewolf_step_hunter_title
    WerewolfJudgeStep.Dawn -> R.string.werewolf_step_dawn_title
    WerewolfJudgeStep.DayVote -> R.string.werewolf_step_day_vote_title
}

private fun WerewolfJudgeStep.instructionResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_step_wolves_instruction
    WerewolfJudgeStep.Seer -> R.string.werewolf_step_seer_instruction
    WerewolfJudgeStep.Witch -> R.string.werewolf_step_witch_instruction
    WerewolfJudgeStep.Hunter -> R.string.werewolf_step_hunter_instruction
    WerewolfJudgeStep.Dawn -> R.string.werewolf_step_dawn_instruction
    WerewolfJudgeStep.DayVote -> R.string.werewolf_step_day_vote_instruction
}

private fun WerewolfJudgeStep.scriptResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_script_wolves
    WerewolfJudgeStep.Seer -> R.string.werewolf_script_seer
    WerewolfJudgeStep.Witch -> R.string.werewolf_script_witch
    WerewolfJudgeStep.Hunter -> R.string.werewolf_script_hunter
    WerewolfJudgeStep.Dawn -> R.string.werewolf_script_dawn
    WerewolfJudgeStep.DayVote -> R.string.werewolf_script_day_vote
}

private fun WerewolfJudgeStep.actionResId(): Int = when (this) {
    WerewolfJudgeStep.Wolves -> R.string.werewolf_action_wolves
    WerewolfJudgeStep.Seer -> R.string.werewolf_action_seer
    WerewolfJudgeStep.Witch -> R.string.werewolf_action_witch
    WerewolfJudgeStep.Hunter -> R.string.werewolf_action_hunter
    WerewolfJudgeStep.Dawn -> R.string.werewolf_action_dawn
    WerewolfJudgeStep.DayVote -> R.string.werewolf_action_day_vote
}

@Composable
private fun HostProgressCard(
    title: String,
    subtitle: String,
    progress: String,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Text(subtitle, color = Color(0xFF5C6A63))
            }
            Text(progress, color = Color(0xFF2F5D50), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ClocktowerDarkTheme(content: @Composable () -> Unit) {
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
private fun ClocktowerNightActiveScreen(
    title: String,
    subtitle: String,
    progress: String,
    canGoPrevious: Boolean,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    content: @Composable () -> Unit,
) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            subtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        progress,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { content() }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier
                            .weight(0.78f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.previous_step))
                    }
                    Button(
                        onClick = onNext,
                        enabled = nextEnabled,
                        modifier = Modifier
                            .weight(1.22f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_finish_next))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerNewDemonConfirmationScreen(
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
private fun ClocktowerDayPlayerTile(
    seatNumber: Int,
    card: PlayerCard,
) {
    val isAlive = card.eliminatedRound == null
    val stateColor = if (isAlive) Color(0xFF5D8B72) else Color(0xFF777D79)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = stateColor.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = stateColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        seatNumber.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Text(
                card.name,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ClocktowerDayOverviewScreen(
    round: Int,
    cards: List<PlayerCard>,
    aliveCount: Int,
    executionThreshold: Int,
    highestVoteText: String,
    showSlayerAction: Boolean,
    slayerActionEnabled: Boolean,
    showArtistAction: Boolean,
    artistActionEnabled: Boolean,
    actionsEnabled: Boolean,
    onStartNomination: () -> Unit,
    onOpenSlayer: () -> Unit,
    onOpenArtist: () -> Unit,
    onEndDay: () -> Unit,
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text("第 $round 天", "Day $round"),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text("白天管理", "Day management"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            highestVoteText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = Color(0xFF5D8B72).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text("$aliveCount 人存活", "$aliveCount alive"),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = Color(0xFFA6D8BA),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text("$executionThreshold 票可处决", "$executionThreshold to execute"),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text("自由讨论", "Open discussion"),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text(
                                    "有人提名时开始提名流程。身份默认隐藏，避免玩家窥屏。",
                                    "Start nominations when someone nominates. Roles remain hidden to prevent leaks.",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text("玩家状态", "Player status"),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }

                items(cards.chunked(2)) { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowCards.forEach { card ->
                            Box(modifier = Modifier.weight(1f)) {
                                ClocktowerDayPlayerTile(
                                    seatNumber = cards.indexOf(card) + 1,
                                    card = card,
                                )
                            }
                        }
                        if (rowCards.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (showSlayerAction || showArtistAction) {
                    item {
                        Column(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text("公开特殊能力", "Public abilities"),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                            )
                            if (showSlayerAction) {
                                OutlinedButton(
                                    onClick = onOpenSlayer,
                                    enabled = actionsEnabled && slayerActionEnabled,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("杀手行动", "Slayer action"))
                                }
                            }
                            if (showArtistAction) {
                                OutlinedButton(
                                    onClick = onOpenArtist,
                                    enabled = actionsEnabled && artistActionEnabled,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("艺术家提问", "Artist question"))
                                }
                            }
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Button(
                        onClick = onStartNomination,
                        enabled = actionsEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text("开始提名", "Start nomination"))
                    }
                    TextButton(
                        onClick = onEndDay,
                        enabled = actionsEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text(text("结束白天", "End day"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerDawnSummaryScreen(
    round: Int,
    cards: List<PlayerCard>,
    events: List<ClocktowerEvent>,
    pendingNightDeath: String?,
    onEnterDay: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var showPublicAnnouncement by remember(round, pendingNightDeath) { mutableStateOf(false) }
    val deathLabel = pendingNightDeath?.let { playerSeatLabel(cards, it) }
    val privateEvents = events
        .filter { event ->
            event.round == round &&
                event.phase in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
                event.type in setOf(
                    ClocktowerEventType.RoleAction,
                    ClocktowerEventType.Death,
                    ClocktowerEventType.RoleChange,
                )
        }
        .takeLast(8)

    ClocktowerDarkTheme {
        if (showPublicAnnouncement) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text("天亮了", "DAWN"),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = deathLabel?.let {
                        text("昨晚，$it 死亡。", "$it died last night.")
                    } ?: text("昨晚，没有人死亡。", "Nobody died last night."),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(36.dp))
                OutlinedButton(
                    onClick = { showPublicAnnouncement = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text("收回手机", "Return to host"))
                }
            }
            return@ClocktowerDarkTheme
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text("夜晚已结算", "NIGHT RESOLVED"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text("第 $round 天 · 天亮", "Day $round · Dawn"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text("先私下复核结算，再向所有玩家播报。", "Review the private resolution before making the public announcement."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text("说书人私密复核", "HOST-ONLY REVIEW"),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    text("不要展示给玩家", "PRIVATE"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            if (privateEvents.isEmpty()) {
                                Text(
                                    text("没有需要额外复核的夜间事件。", "No additional night events need review."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                privateEvents.forEachIndexed { index, event ->
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(event.title, fontWeight = FontWeight.Bold)
                                        if (event.detail.isNotBlank()) {
                                            Text(
                                                event.detail,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                    if (index < privateEvents.lastIndex) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text("公开播报", "PUBLIC ANNOUNCEMENT"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = deathLabel?.let {
                                    text("天亮了。昨晚，$it 死亡。", "Dawn has arrived. $it died last night.")
                                } ?: text("天亮了。昨晚，没有人死亡。", "Dawn has arrived. Nobody died last night."),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text(
                                    "只播报死亡结果，不说明保护、中毒、转移或具体角色。",
                                    "Announce only the death result. Do not reveal protection, poison, redirects, or roles.",
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            OutlinedButton(
                                onClick = { showPublicAnnouncement = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(text("全屏展示播报内容", "Show announcement full screen"))
                            }
                        }
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onEnterDay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text("已完成播报，进入白天", "Announcement complete — enter day"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClocktowerNominationScreen(
    round: Int,
    cards: List<PlayerCard>,
    aliveCards: List<PlayerCard>,
    executionThreshold: Int,
    nominatorName: String?,
    nomineeName: String?,
    specialNotice: String?,
    specialNoticeIsDanger: Boolean,
    continueLabel: String,
    actionsEnabled: Boolean,
    onSelectNominator: (String) -> Unit,
    onSelectNominee: (String) -> Unit,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    specialContent: @Composable ColumnScope.() -> Unit = {},
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ClocktowerDayActionHeader(
                round = round,
                currentStep = 0,
                executionThreshold = executionThreshold,
                title = text("记录提名", "Record nomination"),
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text("谁发起提名？", "Who is nominating?"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            SelectablePlayerChips(
                                cards = aliveCards,
                                selectedName = nominatorName,
                                enabled = actionsEnabled,
                                allCards = cards,
                                onSelect = onSelectNominator,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Text(
                                text("谁被提名？", "Who is nominated?"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            SelectablePlayerChips(
                                cards = aliveCards,
                                selectedName = nomineeName,
                                enabled = actionsEnabled,
                                allCards = cards,
                                onSelect = onSelectNominee,
                            )
                        }
                    }
                }
                if (nominatorName != null && nomineeName != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text("提名关系", "NOMINATION"),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "${playerSeatLabel(cards, nominatorName)}  →  ${playerSeatLabel(cards, nomineeName)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    text("请让提名人陈述理由，再让被提名人辩护。", "Let the nominator speak, then allow the nominee to defend."),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                if (specialNotice != null) {
                    item {
                        Surface(
                            color = if (specialNoticeIsDanger) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text("角色能力检查", "ABILITY CHECK"),
                                    color = if (specialNoticeIsDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(specialNotice, color = MaterialTheme.colorScheme.onSurface)
                                specialContent()
                            }
                        }
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Button(
                        onClick = onContinue,
                        enabled = actionsEnabled && nominatorName != null && nomineeName != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(continueLabel, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text(text("取消并返回白天", "Cancel and return to day"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerVoteScreen(
    round: Int,
    cards: List<PlayerCard>,
    aliveCount: Int,
    executionThreshold: Int,
    nominatorName: String?,
    nomineeName: String?,
    voteCount: Int,
    highestVoteText: String,
    actionsEnabled: Boolean,
    onVoteCountChange: (Int) -> Unit,
    onRecordAndContinue: () -> Unit,
    onRecordAndEndDay: () -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val reached = voteCount >= executionThreshold

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ClocktowerDayActionHeader(
                round = round,
                currentStep = 1,
                executionThreshold = executionThreshold,
                title = text("记录投票", "Record vote"),
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text("本次提名", "CURRENT NOMINATION"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "${playerSeatLabel(cards, nominatorName)}  →  ${playerSeatLabel(cards, nomineeName)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            StepperRow(
                                label = text("实际票数", "Votes cast"),
                                value = voteCount,
                                range = 0..aliveCount,
                                onChange = onVoteCountChange,
                            )
                            Surface(
                                color = if (reached) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = if (reached) text("达到处决门槛", "Threshold reached")
                                        else text("尚未达到门槛", "Below threshold"),
                                        color = if (reached) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = text(
                                            "$voteCount 票 / 需要 $executionThreshold 票",
                                            "$voteCount votes / $executionThreshold required",
                                        ),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                            }
                            Text(
                                highestVoteText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onRecordAndContinue,
                        enabled = actionsEnabled && nomineeName != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text("记录投票，继续提名", "Record vote and continue"), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onRecordAndEndDay,
                        enabled = actionsEnabled && nomineeName != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text("记录投票并结束白天", "Record vote and end day"))
                    }
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(text("返回修改提名", "Back to nomination"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerExecutionConfirmScreen(
    round: Int,
    cards: List<PlayerCard>,
    executionThreshold: Int,
    selectedExecution: String?,
    highestVoteCount: Int,
    actionsEnabled: Boolean,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val targetLabel = selectedExecution?.let { playerSeatLabel(cards, it) }

    ClocktowerDarkTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ClocktowerDayActionHeader(
                round = round,
                currentStep = 2,
                executionThreshold = executionThreshold,
                title = text("结束白天", "Resolve the day"),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = if (targetLabel != null) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        1.dp,
                        if (targetLabel != null) MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (targetLabel != null) text("即将记录处决", "EXECUTION TO RECORD")
                            else text("今日无人被处决", "NO EXECUTION TODAY"),
                            color = if (targetLabel != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = targetLabel ?: text("进入夜晚", "Continue to night"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (targetLabel != null) {
                                text("最高票 $highestVoteCount；确认后将立即结算角色能力与胜负。", "Highest vote: $highestVoteCount. Confirming resolves abilities and victory.")
                            } else {
                                text("确认后将结束今天并进入夜晚。", "Confirm to close the day and continue to night.")
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onConfirm,
                        enabled = actionsEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (targetLabel != null) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                    ) {
                        Text(
                            text = if (targetLabel != null) text("确认处决 $targetLabel", "Confirm execution: $targetLabel")
                            else text("确认无人被处决，进入夜晚", "Confirm no execution and continue"),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(text("返回白天检查", "Return to day"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClocktowerDayActionHeader(
    round: Int,
    currentStep: Int,
    executionThreshold: Int,
    title: String,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(
                        text("第 $round 天 · $executionThreshold 票可处决", "Day $round · $executionThreshold votes to execute"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "${currentStep + 1} / 3",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= currentStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(50),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HostScriptCard(
    title: String,
    script: String,
    action: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                backgroundColor = Color(0xFFFFF4DC),
                textColor = Color(0xFF1F2925),
            )
            content()
        }
    }
}

@Composable
private fun HostInstructionBlock(
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
        Text(label, color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Text(text, color = textColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HostActionSection(
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
private fun WerewolfRoleLine(roleName: String, players: List<PlayerCard>) {
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
private fun SelectablePlayerChips(
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
private fun SelectableSeatNumbers(
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
private fun WerewolfPlayerStatusRow(card: PlayerCard) {
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
private fun EliminationRecord.displayText(): String {
    val base = stringResource(R.string.elimination_record_format, round, playerName)
    return note?.let { stringResource(R.string.elimination_record_with_note_format, base, it) } ?: base
}

private fun PlayerCard.hostRoleLabel(context: Context, gameKind: GameKind): String = when (gameKind) {
    GameKind.Clocktower -> actualRoleLabel ?: roleLabel ?: context.getString(role.labelResId())
    GameKind.Werewolf -> roleLabel ?: context.getString(role.labelResId())
    GameKind.Undercover -> context.getString(role.labelResId())
}

private fun PlayerCard.seatLabel(cards: List<PlayerCard>): String =
    "${cards.indexOfFirst { it.name == name } + 1}号 $name"

private fun playerSeatLabel(cards: List<PlayerCard>, playerName: String?): String =
    cards.firstOrNull { it.name == playerName }?.seatLabel(cards) ?: playerName.orEmpty()

private fun isClocktowerEvil(card: PlayerCard): Boolean =
    card.clocktowerTeam == ClocktowerTeam.Minion || card.clocktowerTeam == ClocktowerTeam.Demon

private fun clocktowerRedHerringCandidates(aliveCards: List<PlayerCard>): List<PlayerCard> =
    aliveCards.filter { card -> card.clocktowerTeam?.isLegalRedHerringTeam() == true }

private fun actualClocktowerRoleCards(cards: List<PlayerCard>, enName: String): List<PlayerCard> =
    cards.filter { it.clocktowerRole?.enName == enName }

private fun chefEvilPairs(cards: List<PlayerCard>, isEvil: (PlayerCard) -> Boolean = ::isClocktowerEvil): Int {
    val evilSeats = cards.mapIndexedNotNull { index, card -> (index + 1).takeIf { isEvil(card) } }.toSet()
    return FixedInformationEvaluator.chefEvilPairs(cards.toClocktowerPlayerStates()) { it.seat in evilSeats }
}

private fun livingNeighbors(cards: List<PlayerCard>, playerName: String): List<PlayerCard> {
    val sourceSeat = cards.indexOfFirst { it.name == playerName } + 1
    if (sourceSeat <= 0) return emptyList()
    val neighborSeats = FixedInformationEvaluator
        .livingNeighbors(cards.toClocktowerPlayerStates(), sourceSeat)
        .map { it.seat }
    return neighborSeats.mapNotNull { seat -> cards.getOrNull(seat - 1) }
}

private fun empathEvilNeighborCount(
    cards: List<PlayerCard>,
    playerName: String,
    isEvil: (PlayerCard) -> Boolean = ::isClocktowerEvil,
): Int {
    val sourceSeat = cards.indexOfFirst { it.name == playerName } + 1
    if (sourceSeat <= 0) return 0
    val evilSeats = cards.mapIndexedNotNull { index, card -> (index + 1).takeIf { isEvil(card) } }.toSet()
    return FixedInformationEvaluator.empathEvilNeighborCount(cards.toClocktowerPlayerStates(), sourceSeat) {
        it.seat in evilSeats
    }
}

private fun storytellerPairHint(
    target: PlayerCard,
    cards: List<PlayerCard>,
    fallbackPool: List<PlayerCard> = cards,
    excludeNames: Set<String> = emptySet(),
): Pair<PlayerCard, PlayerCard>? {
    val decoy = fallbackPool.firstOrNull { it.name != target.name && it.name !in excludeNames }
        ?: fallbackPool.firstOrNull { it.name != target.name }
        ?: return null
    return target to decoy
}

private fun PlayerCard.clocktowerShownAsDifferentRole(): Boolean =
    clocktowerRole?.enName != null && clocktowerShownRole?.enName != null && clocktowerRole?.enName != clocktowerShownRole?.enName

private data class ClocktowerDisplayOption(
    val label: String,
    val displayKind: ClocktowerDisplayKind,
    val displayTitle: String,
    val displayPrimary: String?,
    val displaySecondary: String?,
    val displayFooter: String?,
    val spyRegistersGood: Boolean? = null,
    val spyRegisteredRoleEnName: String? = null,
    val recluseRegistersEvil: Boolean? = null,
    val recluseRegisteredRoleEnName: String? = null,
    val isDefaultRecommendation: Boolean = false,
)

private data class ClocktowerDecisionOption(
    val label: String,
    val targetName: String,
    val explanation: String,
    val isDefaultRecommendation: Boolean = false,
)

private data class ClocktowerRegistrationRecommendationOption(
    val label: String,
    val usesSpecialRegistration: Boolean,
    val registeredRoleEnName: String?,
    val isDefaultRecommendation: Boolean = false,
)

private enum class ClocktowerRegistrationDetail {
    AlignmentOnly,
    Role,
}

private enum class ClocktowerPairInformationAbility {
    Washerwoman,
    Librarian,
    Investigator,
}

private data class ClocktowerNightStepUi(
    val title: String,
    val actor: PlayerCard?,
    val isRealAction: Boolean,
    val reason: String,
    val storytellerAction: String,
    val tellPlayer: String?,
    val explanation: String,
    val action: ClocktowerNightAction = ClocktowerNightAction.None,
    val displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.None,
    val displayTitle: String = title,
    val displayPrimary: String? = null,
    val displaySecondary: String? = null,
    val displayFooter: String? = null,
    val displayOptions: List<ClocktowerDisplayOption> = emptyList(),
    val recommendedDisplayOptions: List<ClocktowerDisplayOption> = emptyList(),
    val decisionOptions: List<ClocktowerDecisionOption> = emptyList(),
    val wakeText: String? = null,
    val roleEnName: String? = null,
    val spyRegistrationKey: String? = null,
    val spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    val spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    val spyRegistrationHint: String? = null,
    val recluseRegistrationKey: String? = null,
    val recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClocktowerJudgeScreen(
    automaticStorytellerInfo: Boolean,
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    events: List<ClocktowerEvent>,
    script: ClocktowerScript,
    phase: ClocktowerPhase,
    round: Int,
    pendingNightDeath: String?,
    selectedExecution: String?,
    poisonTarget: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    ravenkeeperTarget: String?,
    redHerring: String?,
    recommendedDemonBluffRoleNames: List<String>,
    recommendedDrunkInvestigatorRoleName: String?,
    recommendedDrunkInvestigatorSeats: List<Int>,
    butlerMaster: String?,
    monkProtectedTarget: String?,
    mayorRedirectTarget: String?,
    pendingNewDemonName: String?,
    demonSuccessorTarget: String?,
    virginUsed: Boolean,
    slayerUsed: Boolean,
    slayerClaimedNames: List<String>,
    artistUsed: Boolean,
    artistClaimedNames: List<String>,
    artistClaimantName: String?,
    artistTruthfulAnswer: Boolean?,
    artistShownAnswer: Boolean?,
    lastExecutedName: String?,
    pendingKlutzName: String?,
    klutzChoiceName: String?,
    nightStartedState: MutableState<Boolean>,
    nightStepIndexState: MutableState<Int>,
    dayModeState: MutableState<ClocktowerDayMode>,
    nominatorNameState: MutableState<String?>,
    nomineeNameState: MutableState<String?>,
    currentVoteCountState: MutableState<Int>,
    highestVoteNameState: MutableState<String?>,
    highestVoteCountState: MutableState<Int>,
    slayerClaimantNameState: MutableState<String?>,
    slayerTargetNameState: MutableState<String?>,
    gameOutcome: GameOutcome?,
    onRecordEvent: (ClocktowerEventType, String, String, List<String>) -> Unit,
    onPhaseChange: (ClocktowerPhase) -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onSelectExecution: (String?) -> Unit,
    onSelectPoisonTarget: (String?) -> Unit,
    onSelectFortuneTellerFirst: (String?) -> Unit,
    onSelectFortuneTellerSecond: (String?) -> Unit,
    onSelectChambermaidFirst: (String?) -> Unit,
    onSelectChambermaidSecond: (String?) -> Unit,
    onSelectRavenkeeperTarget: (String?) -> Unit,
    onSelectRedHerring: (String?) -> Unit,
    onApplyRecommendation: (RecommendationPlan) -> Unit,
    onSelectButlerMaster: (String?) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onSelectMayorRedirectTarget: (String?) -> Unit,
    onSelectDemonSuccessor: (String?) -> Unit,
    onConfirmNewDemon: () -> Unit,
    onSelectKlutzChoice: (String?) -> Unit,
    onConfirmKlutzChoice: (Boolean) -> Unit,
    onSelectArtistClaimant: (String?) -> Unit,
    onSelectArtistTruthfulAnswer: (Boolean?) -> Unit,
    onSelectArtistShownAnswer: (Boolean?) -> Unit,
    onConfirmArtistQuestion: () -> Unit,
    onSlayerShot: (String, String, Boolean) -> Unit,
    onVirginNomination: (String, String, Boolean) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: () -> Unit,
    onConfirmNight: () -> Unit,
    onShowResults: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val aliveCards = cards.filter { it.eliminatedRound == null }
    val spyCard = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }
    val recluseCard = cards.firstOrNull { it.clocktowerRole?.enName == "Recluse" }
    val spyRegistrationGood = remember { mutableStateMapOf<String, Boolean>() }
    val spyRegistrationRole = remember { mutableStateMapOf<String, String>() }
    val recordedSpyRegistrations = remember { mutableStateMapOf<String, Boolean>() }
    val recluseRegistrationEvil = remember { mutableStateMapOf<String, Boolean>() }
    val recluseRegistrationRole = remember { mutableStateMapOf<String, String>() }
    val recordedRecluseRegistrations = remember { mutableStateMapOf<String, Boolean>() }
    fun registrationKey(ability: String, subject: String = "spy") = "${phase.name}:$round:$ability:$subject"
    fun spyCanRegister(): Boolean = spyCard != null && poisonTarget != spyCard.name
    fun spyRegistersGood(key: String?): Boolean = key != null && spyCanRegister() && spyRegistrationGood[key] == true
    fun registeredRole(key: String?, teams: List<ClocktowerTeam>): ClocktowerRole? {
        if (!spyRegistersGood(key)) return spyCard?.clocktowerRole
        val allowed = completeTroubleBrewingRoles.filter { it.team in teams && it.enName != "Spy" }
        return allowed.firstOrNull { it.enName == spyRegistrationRole[key] } ?: allowed.firstOrNull()
    }
    fun recordSpyRegistration(
        key: String?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    ) {
        if (key == null || recordedSpyRegistrations[key] == true || spyCard == null) return
        recordedSpyRegistrations[key] = true
        val registrationDetail = when {
            !spyCanRegister() -> text("中毒，按真实邪恶身份登记", "poisoned; registered as actual evil identity")
            !spyRegistersGood(key) -> text("按真实邪恶身份登记", "registered as actual evil identity")
            detail == ClocktowerRegistrationDetail.AlignmentOnly -> text("登记为善良", "registered as good")
            else -> text(
                "登记为${registeredRole(key, teams)?.nameFor(language).orEmpty()}",
                "registered as ${registeredRole(key, teams)?.nameFor(language).orEmpty()}",
            )
        }
        onRecordEvent(
            ClocktowerEventType.RoleAction,
            text("间谍登记裁定", "Spy registration"),
            "${spyCard.seatLabel(cards)} · $registrationDetail",
            listOf(spyCard.name),
        )
    }
    fun recluseCanRegister(): Boolean = recluseCard != null && poisonTarget != recluseCard.name
    fun recluseRegistersEvil(key: String?): Boolean =
        key != null && recluseCanRegister() && recluseRegistrationEvil[key] == true
    fun recluseRegisteredRole(key: String?, teams: List<ClocktowerTeam>): ClocktowerRole? {
        if (!recluseRegistersEvil(key)) return recluseCard?.clocktowerRole
        val allowed = completeTroubleBrewingRoles.filter { it.team in teams }
        return allowed.firstOrNull { it.enName == recluseRegistrationRole[key] } ?: allowed.firstOrNull()
    }
    fun recordRecluseRegistration(key: String?, teams: List<ClocktowerTeam>) {
        if (key == null || !recluseRegistersEvil(key) || recordedRecluseRegistrations[key] == true || recluseCard == null) return
        recordedRecluseRegistrations[key] = true
        val registeredAs = recluseRegisteredRole(key, teams)
        onRecordEvent(
            ClocktowerEventType.RoleAction,
            text("隐士登记裁定", "Recluse registration"),
            if (registeredAs != null && teams.isNotEmpty()) {
                "${recluseCard.seatLabel(cards)} → ${registeredAs.nameFor(language)}"
            } else {
                text("${recluseCard.seatLabel(cards)} → 邪恶", "${recluseCard.seatLabel(cards)} → evil")
            },
            listOf(recluseCard.name),
        )
    }
    val firstNightWasherwoman = actualClocktowerRoleCards(cards, "Washerwoman").firstOrNull()
    val firstNightLibrarian = actualClocktowerRoleCards(cards, "Librarian").firstOrNull()
    val firstNightInvestigator = actualClocktowerRoleCards(cards, "Investigator").firstOrNull()
    val chefPlayer = actualClocktowerRoleCards(cards, "Chef").firstOrNull()
    val empathPlayers = actualClocktowerRoleCards(cards, "Empath").filter { it.eliminatedRound == null }
    val fortuneTellerPlayers = actualClocktowerRoleCards(cards, "Fortune Teller").filter { it.eliminatedRound == null }
    val poisonerPlayers = actualClocktowerRoleCards(cards, "Poisoner").filter { it.eliminatedRound == null }
    val butlerPlayers = actualClocktowerRoleCards(cards, "Butler").filter { it.eliminatedRound == null }
    val demonPoisonedTonight = poisonTarget?.let { name ->
        cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
    } == true
    val mayorTarget = pendingNightDeath
        ?.let { name -> cards.firstOrNull { it.name == name && it.eliminatedRound == null && it.clocktowerRole?.enName == "Mayor" } }
    val mayorCanRedirect =
        mayorTarget != null &&
            !demonPoisonedTonight &&
            poisonTarget != mayorTarget.name &&
            monkProtectedTarget != mayorTarget.name
    val resolvedNightDeathName = if (mayorCanRedirect && mayorRedirectTarget != null) mayorRedirectTarget else pendingNightDeath
    val resolvedNightDeathCard = resolvedNightDeathName?.let { name -> cards.firstOrNull { it.name == name } }
    val nightDeathWillOccur =
        resolvedNightDeathCard != null &&
            !demonPoisonedTonight &&
            resolvedNightDeathCard.eliminatedRound == null &&
            resolvedNightDeathCard.name != monkProtectedTarget &&
            resolvedNightDeathCard.clocktowerRole?.enName != "Soldier"
    val ravenkeeperTrigger = resolvedNightDeathCard
        ?.takeIf { nightDeathWillOccur && it.clocktowerRole?.enName == "Ravenkeeper" }

    val fortuneTellerRecluseRegistrationKey = recluseCard
        ?.takeIf { it.name == fortuneTellerFirst || it.name == fortuneTellerSecond }
        ?.let { registrationKey("FortuneTellerRecluse", it.name) }
    val fortuneTellerMatched = if (fortuneTellerFirst != null && fortuneTellerSecond != null) {
        val targets = setOf(fortuneTellerFirst, fortuneTellerSecond)
        aliveCards.any {
            it.name in targets && (
                it.clocktowerTeam == ClocktowerTeam.Demon ||
                    it.name == redHerring ||
                    (it.name == recluseCard?.name && recluseRegistersEvil(fortuneTellerRecluseRegistrationKey))
                )
        }
    } else {
        null
    }
    val fortuneTellerResult = fortuneTellerMatched?.let { matched ->
        if (matched) stringResource(R.string.clocktower_yes) else stringResource(R.string.clocktower_no)
    }
    fun clockmakerNumber(): Int {
        val demonIndex = cards.indexOfFirst { it.clocktowerTeam == ClocktowerTeam.Demon }
        val minionIndexes = cards.mapIndexedNotNull { index, card -> index.takeIf { card.clocktowerTeam == ClocktowerTeam.Minion } }
        if (demonIndex < 0 || minionIndexes.isEmpty() || cards.isEmpty()) return 0
        return minionIndexes.minOf { minionIndex ->
            val clockwise = (minionIndex - demonIndex + cards.size) % cards.size
            val counterClockwise = (demonIndex - minionIndex + cards.size) % cards.size
            minOf(clockwise, counterClockwise)
        }
    }
    fun chambermaidWakeRoles(): Set<String> = if (phase == ClocktowerPhase.FirstNight) {
        setOf("Clockmaker", "Investigator", "Empath", "Chambermaid", "Spy")
    } else {
        buildSet {
            add("Chambermaid")
            add("Empath")
            add("Poisoner")
            add("Fortune Teller")
            add("Butler")
            add("Monk")
            add("Imp")
            add("Spy")
            if (lastExecutedName != null) add("Undertaker")
            if (ravenkeeperTrigger != null) add("Ravenkeeper")
        }
    }
    val chambermaidResult = if (chambermaidFirst != null && chambermaidSecond != null) {
        val targets = setOf(chambermaidFirst, chambermaidSecond)
        cards.count { it.name in targets && it.clocktowerRole?.enName in chambermaidWakeRoles() }.toString()
    } else {
        null
    }
    fun recordNightStep(step: ClocktowerNightStepUi) {
        if (!step.isRealAction || step.action == ClocktowerNightAction.None || step.action == ClocktowerNightAction.DemonKill) return
        val names = when (step.action) {
            ClocktowerNightAction.RedHerring -> listOfNotNull(redHerring)
            ClocktowerNightAction.Poison -> listOfNotNull(step.actor?.name, poisonTarget)
            ClocktowerNightAction.ButlerMaster -> listOfNotNull(step.actor?.name, butlerMaster)
            ClocktowerNightAction.MonkProtect -> listOfNotNull(step.actor?.name, monkProtectedTarget)
            ClocktowerNightAction.FortuneTeller -> listOfNotNull(step.actor?.name, fortuneTellerFirst, fortuneTellerSecond)
            ClocktowerNightAction.Chambermaid -> listOfNotNull(step.actor?.name, chambermaidFirst, chambermaidSecond)
            ClocktowerNightAction.DemonKill -> listOfNotNull(step.actor?.name, pendingNightDeath)
            ClocktowerNightAction.MayorRedirect -> listOfNotNull(mayorRedirectTarget)
            ClocktowerNightAction.DemonSuccessor -> listOfNotNull(demonSuccessorTarget)
            ClocktowerNightAction.Ravenkeeper -> listOfNotNull(step.actor?.name, ravenkeeperTarget)
            ClocktowerNightAction.None -> listOfNotNull(step.actor?.name)
        }
        val selected = names.drop(if (step.actor != null) 1 else 0).joinToString { playerSeatLabel(cards, it) }
        val detail = when (step.action) {
            ClocktowerNightAction.RedHerring -> playerSeatLabel(cards, redHerring)
            ClocktowerNightAction.Poison -> playerSeatLabel(cards, poisonTarget)
            ClocktowerNightAction.ButlerMaster -> playerSeatLabel(cards, butlerMaster)
            ClocktowerNightAction.MonkProtect -> playerSeatLabel(cards, monkProtectedTarget)
            ClocktowerNightAction.FortuneTeller -> "$selected · $fortuneTellerResult"
            ClocktowerNightAction.Chambermaid -> "$selected · $chambermaidResult"
            ClocktowerNightAction.DemonKill -> playerSeatLabel(cards, pendingNightDeath)
            ClocktowerNightAction.MayorRedirect -> mayorRedirectTarget?.let { target ->
                val mayor = cards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                if (target == mayor?.name) "市长死亡" else "死亡转移给 ${playerSeatLabel(cards, target)}"
            }.orEmpty()
            ClocktowerNightAction.DemonSuccessor -> playerSeatLabel(cards, demonSuccessorTarget)
            ClocktowerNightAction.Ravenkeeper -> playerSeatLabel(cards, ravenkeeperTarget)
            ClocktowerNightAction.None -> step.tellPlayer ?: step.storytellerAction
        }
        onRecordEvent(ClocktowerEventType.RoleAction, step.title, detail, names)
    }
    val phaseTitle = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_phase_first_night)
        ClocktowerPhase.Dawn -> "天亮"
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_phase_day, round)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_phase_night, round)
    }
    val phaseProgress = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_progress_first_night)
        ClocktowerPhase.Dawn -> "天亮"
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_progress_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_progress_night)
    }
    val phaseScript = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_script_first_night)
        ClocktowerPhase.Dawn -> "天亮了，所有人睁眼。"
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_script_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_script_night)
    }
    val phaseAction = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_action_first_night)
        ClocktowerPhase.Dawn -> "宣布昨晚死亡，然后进入白天。"
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_action_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_action_night)
    }

    var nightStarted by nightStartedState
    var nightStepIndex by nightStepIndexState
    var dayMode by dayModeState
    var nominatorName by nominatorNameState
    var nomineeName by nomineeNameState
    var currentVoteCount by currentVoteCountState
    var highestVoteName by highestVoteNameState
    var highestVoteCount by highestVoteCountState
    var slayerClaimantName by slayerClaimantNameState
    var slayerTargetName by slayerTargetNameState
    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    var slayerRecluseRegistersDemon by remember { mutableStateOf(false) }
    val recommendationKey = buildString {
        append(script.name)
        cards.forEachIndexed { index, card ->
            append('|')
            append(index + 1)
            append(':')
            append(card.clocktowerRole?.enName.orEmpty())
        }
    }
    val recommendationCards = cards.toList()
    var recommendationUiState by remember(recommendationKey) {
        mutableStateOf<RecommendationUiState>(RecommendationUiState.Loading)
    }
    var selectedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf(RecommendationStyle.BALANCED)
    }
    var appliedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf<RecommendationStyle?>(null)
    }
    var lockedRecommendationDecisions by remember(recommendationKey) {
        mutableStateOf<List<StorytellerDecision>>(emptyList())
    }
    LaunchedEffect(recommendationKey, lockedRecommendationDecisions) {
        recommendationUiState = RecommendationUiState.Loading
        val result = withContext(Dispatchers.Default) {
            runCatching {
                RecommendationService.recommendConstrained(
                    game = recommendationCards.toClocktowerGameState(
                        script = script,
                        seed = recommendationKey.hashCode().toLong(),
                        poisonedPlayerName = poisonTarget,
                    ),
                    roleDefinitions = clocktowerRoleDefinitionsForScript(script),
                    lockedDecisions = lockedRecommendationDecisions,
                )
            }
        }
        recommendationUiState = result.fold(
            onSuccess = { constrained ->
                when {
                    constrained.failureCodes.isNotEmpty() -> RecommendationUiState.InvalidLocks(constrained.failureCodes)
                    constrained.plans.isEmpty() -> RecommendationUiState.Empty
                    else -> RecommendationUiState.Ready(constrained.plans)
                }
            },
            onFailure = { error ->
                RecommendationUiState.Error(error.message ?: text("推荐计算失败", "Recommendation failed"))
            },
        )
    }
    LaunchedEffect(automaticStorytellerInfo, recommendationUiState) {
        if (automaticStorytellerInfo && appliedRecommendationStyle != RecommendationStyle.BALANCED) {
            val balancedPlan = (recommendationUiState as? RecommendationUiState.Ready)
                ?.plans
                ?.let { plans ->
                    AutomaticStorytellerSelector.select(plans) {
                        it.style == RecommendationStyle.BALANCED
                    }
                }
            if (balancedPlan != null) {
                onApplyRecommendation(balancedPlan)
                selectedRecommendationStyle = RecommendationStyle.BALANCED
                appliedRecommendationStyle = RecommendationStyle.BALANCED
            }
        }
    }
    val executionThreshold = (aliveCards.size + 1) / 2
    fun recordCurrentVote(): String? {
        if (currentVoteCount >= executionThreshold) {
            when {
                currentVoteCount > highestVoteCount -> {
                    highestVoteName = nomineeName
                    highestVoteCount = currentVoteCount
                }
                currentVoteCount == highestVoteCount -> {
                    highestVoteName = null
                }
            }
        }
        return highestVoteName?.takeIf { highestVoteCount >= executionThreshold }
    }
    val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
    val scriptHasSlayer = "Slayer" in scriptRoleNames
    val scriptHasArtist = "Artist" in scriptRoleNames
    val slayerClaimantCandidates = aliveCards.filter { card ->
        card.name !in slayerClaimedNames && !(slayerUsed && card.clocktowerRole?.enName == "Slayer")
    }
    val artistClaimantCandidates = aliveCards.filter { card ->
        card.name !in artistClaimedNames && !(artistUsed && card.clocktowerRole?.enName == "Artist")
    }

    fun roleActor(enName: String): PlayerCard? =
        cards.firstOrNull {
            it.eliminatedRound == null &&
                (it.clocktowerRole?.enName == enName || (it.clocktowerRole?.enName == "Drunk" && it.clocktowerShownRole?.enName == enName))
        }

    fun roleMissingReason(enName: String): String {
        val roleCard = actualClocktowerRoleCards(cards, enName).firstOrNull()
        val drunkShownAsRole = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" && it.clocktowerShownRole?.enName == enName }
        return when {
            roleCard == null && drunkShownAsRole != null -> ""
            roleCard == null -> "本局没有这个角色。"
            roleCard.eliminatedRound != null -> "${roleCard.seatLabel(cards)} 已经死亡，死亡后不再执行这个能力。"
            else -> ""
        }
    }

    fun stableIndex(key: String, size: Int): Int = if (size <= 0) 0 else Math.floorMod(key.hashCode(), size)
    fun actorIsPoisoned(actor: PlayerCard?): Boolean = actor != null && actor.eliminatedRound == null && poisonTarget == actor.name
    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        actor != null && ((actor.clocktowerRole?.enName == "Drunk" && actor.clocktowerShownRole?.enName == enName) || actorIsPoisoned(actor))
    fun orderedPair(first: PlayerCard?, second: PlayerCard?, key: String): Pair<PlayerCard, PlayerCard>? =
        if (first == null || second == null) null else if (stableIndex(key, 2) == 0) first to second else second to first
    fun seatNumberFor(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    fun seatNumbersText(pair: Pair<PlayerCard, PlayerCard>?): String? =
        pair?.let { "${seatNumberFor(it.first)}   ${seatNumberFor(it.second)}" }
    fun displayOption(
        label: String,
        kind: ClocktowerDisplayKind,
        title: String,
        primary: String?,
        secondary: String? = null,
        footer: String? = null,
        isDefaultRecommendation: Boolean = false,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = kind,
        displayTitle = title,
        displayPrimary = primary,
        displaySecondary = secondary,
        displayFooter = footer,
        isDefaultRecommendation = isDefaultRecommendation,
    )
    fun recommendedDrunkInvestigatorOption(actor: PlayerCard): ClocktowerDisplayOption? {
        if (actor.clocktowerRole?.enName != "Drunk" || actor.clocktowerShownRole?.enName != "Investigator") return null
        val minionRole = recommendedDrunkInvestigatorRoleName
            ?.let { roleName -> clocktowerRolesForScript(script).firstOrNull { it.enName == roleName } }
            ?: return null
        val candidateCards = recommendedDrunkInvestigatorSeats
            .mapNotNull { seat -> cards.getOrNull(seat - 1) }
        if (candidateCards.size != 2) return null
        return displayOption(
            label = text("采用的推荐信息", "Applied recommendation"),
            kind = ClocktowerDisplayKind.EitherOne,
            title = text("调查员信息", "Investigator information"),
            primary = minionRole.nameFor(language),
            secondary = recommendedDrunkInvestigatorSeats.joinToString("   "),
            footer = text("在下面两位玩家之中", "One of these two players"),
            isDefaultRecommendation = true,
        )
    }
    fun previousUnreliableNumber(title: String, actor: PlayerCard): Int? = events
        .asReversed()
        .firstOrNull { event ->
            event.type == ClocktowerEventType.UnreliableInformation &&
                actor.name in event.playerNames &&
                event.title.startsWith(title)
        }
        ?.detail
        ?.let { detail ->
            val payload = when {
                "：" in detail -> detail.substringAfter("：")
                ": " in detail -> detail.substringAfter(": ")
                else -> detail
            }
            Regex("\\d+").find(payload)?.value?.toIntOrNull()
        }

    fun recommendationStyleLabel(style: RecommendationStyle): String = when (style) {
        RecommendationStyle.GENTLE -> text("推荐·稳健", "Recommended · gentle")
        RecommendationStyle.BALANCED -> text("推荐·平衡", "Recommended · balanced")
        RecommendationStyle.AGGRESSIVE -> text("专家·激进", "Expert · aggressive")
    }

    fun recommendedNumberOptions(
        title: String,
        actor: PlayerCard,
        trueValue: Int,
        maxValue: Int,
        footer: String,
        pressureCostPerPoint: Int = 0,
        secondary: String? = null,
    ): List<ClocktowerDisplayOption> {
        return UnreliableNumberInformationRecommender.recommend(
            UnreliableNumberContext(
                trueValue = trueValue,
                minimumValue = 0,
                maximumValue = maxOf(trueValue, maxValue),
                previousShownValue = previousUnreliableNumber(title, actor)
                    ?.takeIf { it in 0..maxOf(trueValue, maxValue) },
                pressureCostPerPoint = pressureCostPerPoint,
            ),
        ).map { recommendation ->
            val styleLabel = recommendationStyleLabel(recommendation.style)
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "$styleLabel：${recommendation.value}$warning",
                kind = ClocktowerDisplayKind.Number,
                title = title,
                primary = recommendation.value.toString(),
                secondary = secondary,
                footer = footer,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun recommendedYesNoOptions(
        title: String,
        truthfulYes: Boolean,
        secondary: String?,
        footer: String,
    ): List<ClocktowerDisplayOption> {
        val yesText = text("有", "Yes")
        val noText = text("没有", "No")
        val candidates = listOf(
            UnreliableCategoricalCandidate("yes", isTruthful = truthfulYes, misinformationPressure = if (truthfulYes) 0 else 3),
            UnreliableCategoricalCandidate("no", isTruthful = !truthfulYes, misinformationPressure = if (truthfulYes) 3 else 0),
        )
        return UnreliableCategoricalInformationRecommender.recommend(candidates).map { recommendation ->
            val value = if (recommendation.candidateId == "yes") yesText else noText
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$value$warning",
                kind = ClocktowerDisplayKind.YesNo,
                title = title,
                primary = value,
                secondary = secondary,
                footer = footer,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun recommendedRoleRevealOptions(
        title: String,
        truthfulRole: ClocktowerRole?,
        footer: String,
    ): List<ClocktowerDisplayOption> {
        if (truthfulRole == null) return emptyList()
        val roles = (listOf(truthfulRole) + clocktowerRolesForScript(script)).distinctBy(ClocktowerRole::enName)
        val candidates = roles.map { role ->
            val metadata = TroubleBrewingRecommendationMetadata.forRole(RoleId(role.enName))
            UnreliableCategoricalCandidate(
                id = role.enName,
                isTruthful = role.enName == truthfulRole.enName,
                misinformationPressure = if (role.enName == truthfulRole.enName) {
                    0
                } else {
                    ((metadata.exposureSensitivity + metadata.discussionValue) / 2).coerceIn(1, 5)
                },
            )
        }
        return UnreliableCategoricalInformationRecommender.recommend(candidates).mapNotNull { recommendation ->
            val role = roles.firstOrNull { it.enName == recommendation.candidateId } ?: return@mapNotNull null
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：${role.nameFor(language)}$warning",
                kind = ClocktowerDisplayKind.RoleReveal,
                title = title,
                primary = role.nameFor(language),
                footer = footer,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun recommendedSageOptions(
        actor: PlayerCard,
        demon: PlayerCard,
        truthfulOnly: Boolean = false,
    ): List<ClocktowerDisplayOption> {
        val pool = cards.filter { it.name != actor.name }
        val pairs = buildList {
            for (firstIndex in 0 until pool.lastIndex) {
                for (secondIndex in firstIndex + 1 until pool.size) {
                    add(pool[firstIndex] to pool[secondIndex])
                }
            }
        }
        if (pairs.isEmpty()) return emptyList()
        fun pairId(pair: Pair<PlayerCard, PlayerCard>): String = listOf(cards.indexOf(pair.first), cards.indexOf(pair.second)).sorted().joinToString(":")
        val byId = pairs.associateBy(::pairId)
        val candidates = pairs.map { pair ->
            val isTruthful = pair.first.name == demon.name || pair.second.name == demon.name
            val evilCount = listOf(pair.first, pair.second).count(::isClocktowerEvil)
            UnreliableCategoricalCandidate(
                id = pairId(pair),
                isTruthful = isTruthful,
                misinformationPressure = if (isTruthful) 0 else when (evilCount) {
                    1 -> 2
                    2 -> 3
                    else -> 4
                },
            )
        }.filter { !truthfulOnly || it.isTruthful }
        return UnreliableCategoricalInformationRecommender.recommend(candidates).mapNotNull { recommendation ->
            val pair = byId[recommendation.candidateId] ?: return@mapNotNull null
            val seats = "${seatNumberFor(pair.first)}   ${seatNumberFor(pair.second)}"
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$seats$warning",
                kind = ClocktowerDisplayKind.EitherOne,
                title = text("贤者信息", "Sage information"),
                primary = text("恶魔", "Demon"),
                secondary = seats,
                footer = text("在下面两位玩家之中", "One of these two players"),
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    data class PairInformationEffect(
        val id: String,
        val shownRole: ClocktowerRole?,
        val target: PlayerCard?,
        val decoy: PlayerCard?,
        val registration: PairInformationRegistration,
    )

    fun informationHistoryPressure(card: PlayerCard?): Int {
        if (card == null) return 0
        return events.count { event ->
            event.type in setOf(ClocktowerEventType.Information, ClocktowerEventType.UnreliableInformation) &&
                card.name in event.playerNames.drop(1)
        }
    }

    fun dynamicStorytellerState(): DynamicGameState {
        val spentRoleNames = buildSet {
            if (virginUsed) add("Virgin")
            if (slayerUsed) add("Slayer")
            if (artistUsed) add("Artist")
        }
        return DynamicGameState(
            game = cards.toClocktowerGameState(
                script = script,
                seed = recommendationKey.hashCode().toLong(),
                poisonedPlayerName = poisonTarget,
            ),
            phase = when (phase) {
                ClocktowerPhase.FirstNight -> StorytellerPhase.FIRST_NIGHT
                ClocktowerPhase.Dawn -> StorytellerPhase.DAWN
                ClocktowerPhase.Day -> StorytellerPhase.DAY
                ClocktowerPhase.Night -> StorytellerPhase.NIGHT
            },
            round = round,
            protectedSeats = setOfNotNull(
                monkProtectedTarget
                    ?.let { name -> cards.indexOfFirst { it.name == name } + 1 }
                    ?.takeIf { it > 0 },
            ),
            spentAbilitySeats = cards.mapIndexedNotNull { index, card ->
                (index + 1).takeIf { card.clocktowerRole?.enName in spentRoleNames }
            }.toSet(),
            informationPressureBySeat = cards.mapIndexed { index, card ->
                (index + 1) to informationHistoryPressure(card)
            }.toMap(),
        )
    }

    fun registrationRecommendationOptions(
        key: String?,
        roleEnName: String?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail,
        subject: PlayerCard?,
        isSpy: Boolean,
        suppressForJointRecommendation: Boolean = false,
        outcomeMisinformationPressure: Int = 0,
    ): List<ClocktowerRegistrationRecommendationOption> {
        if (subject == null || suppressForJointRecommendation) return emptyList()
        if (key == null || teams.isEmpty()) return emptyList()
        val allowedRoleNames = completeTroubleBrewingRoles
            .filter { it.team in teams && (!isSpy || it.enName != "Spy") }
            .map { it.enName }
            .toSet()
        val allowedRoles = clocktowerRoleDefinitionsForScript(script).filter { it.id.value in allowedRoleNames }
        if (allowedRoles.isEmpty()) return emptyList()
        val subjectSeat = cards.indexOfFirst { it.name == subject.name } + 1
        if (subjectSeat <= 0) return emptyList()
        val request = DynamicDecisionRequest(
            id = key,
            type = StorytellerDecisionType.SPECIAL_REGISTRATION,
            sourceAbility = RoleId(roleEnName ?: return emptyList()),
            state = dynamicStorytellerState(),
        )
        return SpecialRegistrationRecommender.recommend(
            request = request,
            context = SpecialRegistrationContext(
                subjectSeat = subjectSeat,
                allowedRoles = allowedRoles,
                detail = if (isSpy && detail == ClocktowerRegistrationDetail.AlignmentOnly) {
                    RegistrationDetail.ALIGNMENT_ONLY
                } else {
                    RegistrationDetail.ROLE
                },
                canMisregister = subject.name != poisonTarget,
                outcomeMisinformationPressure = outcomeMisinformationPressure,
            ),
        ).map { recommendation ->
            val choice = recommendation.candidate.choice as DynamicStorytellerChoice.Registration
            val role = completeTroubleBrewingRoles.firstOrNull { it.enName == choice.registeredRole.value }
            val decisionText = when {
                !choice.usesSpecialAbility -> text("按真实身份登记", "Register actual identity")
                isSpy && detail == ClocktowerRegistrationDetail.AlignmentOnly ->
                    text("登记为善良", "Register as good")
                !isSpy && teams.isEmpty() -> text("登记为邪恶", "Register as evil")
                else -> text(
                    "登记为 ${role?.nameFor(language) ?: choice.registeredRole.value}",
                    "Register as ${role?.nameFor(language) ?: choice.registeredRole.value}",
                )
            }
            val warning = if (recommendation.warnings.size > 1) text(" · 高影响", " · high impact") else ""
            ClocktowerRegistrationRecommendationOption(
                label = "${recommendationStyleLabel(recommendation.style)} · $decisionText$warning",
                usesSpecialRegistration = choice.usesSpecialAbility,
                registeredRoleEnName = choice.registeredRole.value.takeIf { choice.usesSpecialAbility },
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun registrationRecommendationOptions(
        step: ClocktowerNightStepUi,
        subject: PlayerCard?,
        isSpy: Boolean,
    ): List<ClocktowerRegistrationRecommendationOption> = registrationRecommendationOptions(
        key = if (isSpy) step.spyRegistrationKey else step.recluseRegistrationKey,
        roleEnName = step.roleEnName,
        teams = if (isSpy) step.spyRegistrationTeams else step.recluseRegistrationTeams,
        detail = if (isSpy) step.spyRegistrationDetail else ClocktowerRegistrationDetail.Role,
        subject = subject,
        isSpy = isSpy,
        suppressForJointRecommendation = step.recommendedDisplayOptions.isNotEmpty(),
    )

    fun mayorDecisionOptions(mayor: PlayerCard): List<ClocktowerDecisionOption> {
        val mayorSeat = cards.indexOfFirst { it.name == mayor.name } + 1
        if (mayorSeat <= 0) return emptyList()
        val request = DynamicDecisionRequest(
            id = registrationKey("MayorRedirect", mayor.name),
            type = StorytellerDecisionType.MAYOR_DEATH_RESOLUTION,
            sourceAbility = RoleId("Mayor"),
            state = dynamicStorytellerState(),
        )
        return MayorRedirectRecommender.recommend(request, mayorSeat).mapNotNull { recommendation ->
            val choice = recommendation.candidate.choice as DynamicStorytellerChoice.MayorDeathResolution
            val target = cards.getOrNull(choice.targetSeat - 1) ?: return@mapNotNull null
            val outcome = recommendation.candidate.outcome as PredictedDecisionOutcome.NightDeath
            val result = when {
                outcome.actualDeathSeat == null -> text("今夜无人死亡", "No death tonight")
                outcome.actualDeathSeat == mayorSeat -> text("市长死亡", "Mayor dies")
                else -> text("${target.seatLabel(cards)} 死亡", "${target.seatLabel(cards)} dies")
            }
            val warning = if (recommendation.warnings.isNotEmpty()) text(" · 注意风险", " · review risk") else ""
            ClocktowerDecisionOption(
                label = "${recommendationStyleLabel(recommendation.style)} · $result$warning",
                targetName = target.name,
                explanation = result,
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun demonSuccessorDecisionOptions(): List<ClocktowerDecisionOption> {
        val request = DynamicDecisionRequest(
            id = registrationKey("DemonSuccessor"),
            type = StorytellerDecisionType.DEMON_SUCCESSION,
            sourceAbility = RoleId("Imp"),
            state = dynamicStorytellerState(),
        )
        return DemonSuccessorRecommender.recommend(request).mapNotNull { recommendation ->
            val choice = recommendation.candidate.choice as DynamicStorytellerChoice.DemonSuccessor
            val target = cards.getOrNull(choice.targetSeat - 1) ?: return@mapNotNull null
            val warning = when {
                recommendation.warnings.any { it.ruleId == "scarlet-woman-mandatory" } ->
                    text(" · 规则要求", " · required")
                recommendation.warnings.isNotEmpty() -> text(" · 注意风险", " · review risk")
                else -> ""
            }
            ClocktowerDecisionOption(
                label = "${recommendationStyleLabel(recommendation.style)} · ${target.seatLabel(cards)}$warning",
                targetName = target.name,
                explanation = text(
                    "${target.seatLabel(cards)} 成为新的小恶魔",
                    "${target.seatLabel(cards)} becomes the new Imp",
                ),
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun recommendedPairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
        val scriptRoles = clocktowerRolesForScript(script)
        val targetEffects = buildList<PairInformationEffect> {
            fun addTargets(
                targets: List<PlayerCard>,
                roleForTarget: (PlayerCard) -> ClocktowerRole?,
                registration: PairInformationRegistration = PairInformationRegistration.NONE,
            ) {
                targets.forEach { target ->
                    val shownRole = roleForTarget(target) ?: return@forEach
                    cards.filter { decoy -> decoy.name != actor.name && decoy.name != target.name }
                        .forEach { decoy ->
                            val targetSeat = cards.indexOf(target) + 1
                            val decoySeat = cards.indexOf(decoy) + 1
                            add(
                                PairInformationEffect(
                                    id = "${ability.name}:${shownRole.enName}:$targetSeat:$decoySeat:${registration.name}",
                                    shownRole = shownRole,
                                    target = target,
                                    decoy = decoy,
                                    registration = registration,
                                ),
                            )
                        }
                }
            }

            when (ability) {
                ClocktowerPairInformationAbility.Washerwoman -> {
                    addTargets(
                        targets = cards.filter { it.name != actor.name && it.clocktowerTeam == ClocktowerTeam.Townsfolk },
                        roleForTarget = { it.clocktowerRole },
                    )
                    if (spyCanRegister() && spyCard != null) {
                        scriptRoles.filter { it.team == ClocktowerTeam.Townsfolk }.forEach { role ->
                            addTargets(
                                targets = listOf(spyCard),
                                roleForTarget = { role },
                                registration = PairInformationRegistration.SPY_AS_GOOD_ROLE,
                            )
                        }
                    }
                }

                ClocktowerPairInformationAbility.Librarian -> {
                    val outsiders = cards.filter { it.name != actor.name && it.clocktowerTeam == ClocktowerTeam.Outsider }
                    addTargets(outsiders, roleForTarget = { it.clocktowerRole })
                    if (outsiders.isEmpty()) {
                        add(
                            PairInformationEffect(
                                id = "${ability.name}:none",
                                shownRole = null,
                                target = null,
                                decoy = null,
                                registration = PairInformationRegistration.NONE,
                            ),
                        )
                    }
                    if (spyCanRegister() && spyCard != null) {
                        scriptRoles.filter { it.team == ClocktowerTeam.Outsider }.forEach { role ->
                            addTargets(
                                targets = listOf(spyCard),
                                roleForTarget = { role },
                                registration = PairInformationRegistration.SPY_AS_GOOD_ROLE,
                            )
                        }
                    }
                }

                ClocktowerPairInformationAbility.Investigator -> {
                    addTargets(
                        targets = cards.filter { it.name != actor.name && it.clocktowerTeam == ClocktowerTeam.Minion },
                        roleForTarget = { it.clocktowerRole },
                    )
                    if (recluseCanRegister() && recluseCard != null) {
                        scriptRoles.filter { it.team == ClocktowerTeam.Minion }.forEach { role ->
                            addTargets(
                                targets = listOf(recluseCard),
                                roleForTarget = { role },
                                registration = PairInformationRegistration.RECLUSE_AS_EVIL_ROLE,
                            )
                        }
                    }
                }
            }
        }.distinctBy(PairInformationEffect::id)

        val candidates = targetEffects.map { effect ->
            val targetMetadata = effect.target?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val decoyMetadata = effect.decoy?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val specialRegistration = effect.registration != PairInformationRegistration.NONE
            val evilPlayersNamed = listOfNotNull(effect.target, effect.decoy).count(::isClocktowerEvil)
            PairInformationCandidate(
                id = effect.id,
                registration = effect.registration,
                targetExposure = targetMetadata?.exposureSensitivity ?: 0,
                decoyExposure = decoyMetadata?.exposureSensitivity ?: 0,
                discussionValue = (targetMetadata?.discussionValue ?: 0) + (decoyMetadata?.discussionValue ?: 0),
                misinformationPressure = (
                    (if (specialRegistration) 3 else 0) +
                        evilPlayersNamed +
                        if ((targetMetadata?.exposureSensitivity ?: 0) >= 4) 1 else 0
                    ).coerceIn(0, 5),
                historyPressure = informationHistoryPressure(effect.target) + informationHistoryPressure(effect.decoy),
            )
        }
        val effectsById = targetEffects.associateBy(PairInformationEffect::id)
        return PairInformationRecommender.recommend(candidates).mapNotNull { recommendation ->
            val effect = effectsById[recommendation.candidateId] ?: return@mapNotNull null
            val roleText = effect.shownRole?.nameFor(language) ?: text("没有外来者", "No Outsiders")
            val seats = if (effect.target != null && effect.decoy != null) {
                "${seatNumberFor(effect.target)}   ${seatNumberFor(effect.decoy)}"
            } else {
                null
            }
            val registrationText = when (effect.registration) {
                PairInformationRegistration.NONE -> ""
                PairInformationRegistration.SPY_AS_GOOD_ROLE -> text(" · 间谍登记", " · Spy registration")
                PairInformationRegistration.RECLUSE_AS_EVIL_ROLE -> text(" · 隐士登记", " · Recluse registration")
            }
            val warning = if (recommendation.warningIds.any { it != "special-registration" }) {
                text(" ⚠ 高压", " ⚠ high pressure")
            } else {
                ""
            }
            ClocktowerDisplayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$roleText${seats?.let { " · $it" }.orEmpty()}$registrationText$warning",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayTitle = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman -> text("洗衣妇信息", "Washerwoman information")
                    ClocktowerPairInformationAbility.Librarian -> text("图书管理员信息", "Librarian information")
                    ClocktowerPairInformationAbility.Investigator -> text("调查员信息", "Investigator information")
                },
                displayPrimary = roleText,
                displaySecondary = seats,
                displayFooter = if (seats == null) "" else text("在下面两位玩家之中", "One of these two players"),
                spyRegistersGood = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman,
                    ClocktowerPairInformationAbility.Librarian,
                    ClocktowerPairInformationAbility.Investigator
                    -> effect.registration == PairInformationRegistration.SPY_AS_GOOD_ROLE
                },
                spyRegisteredRoleEnName = effect.shownRole?.enName
                    ?.takeIf { effect.registration == PairInformationRegistration.SPY_AS_GOOD_ROLE },
                recluseRegistersEvil = if (ability == ClocktowerPairInformationAbility.Investigator) {
                    effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE
                } else {
                    null
                },
                recluseRegisteredRoleEnName = effect.shownRole?.enName
                    ?.takeIf { effect.registration == PairInformationRegistration.RECLUSE_AS_EVIL_ROLE },
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun recommendedUnreliablePairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
        val roleTeam = when (ability) {
            ClocktowerPairInformationAbility.Washerwoman -> ClocktowerTeam.Townsfolk
            ClocktowerPairInformationAbility.Librarian -> ClocktowerTeam.Outsider
            ClocktowerPairInformationAbility.Investigator -> ClocktowerTeam.Minion
        }
        val roles = clocktowerRolesForScript(script).filter { it.team == roleTeam }
        val pool = cards.filter { it.name != actor.name }
        val effects = buildList<PairInformationEffect> {
            roles.forEach { role ->
                for (firstIndex in 0 until pool.lastIndex) {
                    for (secondIndex in firstIndex + 1 until pool.size) {
                        val first = pool[firstIndex]
                        val second = pool[secondIndex]
                        add(
                            PairInformationEffect(
                                id = "unreliable:${ability.name}:${role.enName}:${cards.indexOf(first) + 1}:${cards.indexOf(second) + 1}",
                                shownRole = role,
                                target = first,
                                decoy = second,
                                registration = PairInformationRegistration.NONE,
                            ),
                        )
                    }
                }
            }
            if (ability != ClocktowerPairInformationAbility.Washerwoman) {
                add(
                    PairInformationEffect(
                        id = "unreliable:${ability.name}:none",
                        shownRole = null,
                        target = null,
                        decoy = null,
                        registration = PairInformationRegistration.NONE,
                    ),
                )
            }
        }
        val candidates = effects.map { effect ->
            val namedPlayers = listOfNotNull(effect.target, effect.decoy)
            val truthful = if (effect.shownRole == null) {
                cards.none { it.clocktowerTeam == roleTeam }
            } else {
                namedPlayers.any { it.clocktowerRole?.enName == effect.shownRole.enName }
            }
            val targetMetadata = effect.target?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val decoyMetadata = effect.decoy?.clocktowerRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            val goodPlayersNamed = namedPlayers.count { !isClocktowerEvil(it) }
            val shownMetadata = effect.shownRole?.enName
                ?.let(::RoleId)
                ?.let(TroubleBrewingRecommendationMetadata::forRole)
            PairInformationCandidate(
                id = effect.id,
                registration = PairInformationRegistration.NONE,
                isTruthful = truthful,
                targetExposure = targetMetadata?.exposureSensitivity ?: 0,
                decoyExposure = decoyMetadata?.exposureSensitivity ?: 0,
                discussionValue = (targetMetadata?.discussionValue ?: 0) +
                    (decoyMetadata?.discussionValue ?: 0) +
                    (shownMetadata?.discussionValue ?: 0),
                misinformationPressure = if (truthful) {
                    0
                } else {
                    (2 +
                        (if (goodPlayersNamed == 2) 1 else 0) +
                        (if ((shownMetadata?.exposureSensitivity ?: 0) >= 4) 1 else 0))
                        .coerceIn(0, 5)
                },
                historyPressure = informationHistoryPressure(effect.target) + informationHistoryPressure(effect.decoy),
            )
        }
        val effectsById = effects.associateBy(PairInformationEffect::id)
        return PairInformationRecommender.recommend(candidates).mapNotNull { recommendation ->
            val effect = effectsById[recommendation.candidateId] ?: return@mapNotNull null
            val noRoleText = when (ability) {
                ClocktowerPairInformationAbility.Librarian -> text("没有外来者", "No Outsiders")
                ClocktowerPairInformationAbility.Investigator -> text("没有爪牙", "No Minions")
                ClocktowerPairInformationAbility.Washerwoman -> text("没有镇民", "No Townsfolk")
            }
            val roleText = effect.shownRole?.nameFor(language) ?: noRoleText
            val seats = if (effect.target != null && effect.decoy != null) {
                "${seatNumberFor(effect.target)}   ${seatNumberFor(effect.decoy)}"
            } else {
                null
            }
            val warning = if (recommendation.warningIds.isNotEmpty()) text(" ⚠ 高压", " ⚠ high pressure") else ""
            displayOption(
                label = "${recommendationStyleLabel(recommendation.style)}：$roleText${seats?.let { " · $it" }.orEmpty()}$warning",
                kind = ClocktowerDisplayKind.EitherOne,
                title = when (ability) {
                    ClocktowerPairInformationAbility.Washerwoman -> text("洗衣妇信息", "Washerwoman information")
                    ClocktowerPairInformationAbility.Librarian -> text("图书管理员信息", "Librarian information")
                    ClocktowerPairInformationAbility.Investigator -> text("调查员信息", "Investigator information")
                },
                primary = roleText,
                secondary = seats,
                footer = if (seats == null) "" else text("在下面两位玩家之中", "One of these two players"),
                isDefaultRecommendation = recommendation.style == RecommendationStyle.BALANCED,
            )
        }
    }

    fun infoStep(
        roleName: String,
        enName: String,
        tellPlayer: String?,
        explanation: String,
        action: ClocktowerNightAction = ClocktowerNightAction.None,
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.Plain,
        displayPrimary: String? = tellPlayer,
        displaySecondary: String? = null,
        displayFooter: String? = explanation,
        displayTitle: String = "$roleName 信息",
        hostInstruction: String? = null,
        displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        reliableDisplayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { emptyList() },
        spyRegistrationKey: String? = null,
        spyRegistrationTeams: List<ClocktowerTeam> = emptyList(),
        spyRegistrationDetail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
        spyRegistrationHint: String? = null,
        recluseRegistrationKey: String? = null,
        recluseRegistrationTeams: List<ClocktowerTeam> = emptyList(),
    ): ClocktowerNightStepUi {
        val actor = roleActor(enName)
        val actorIsDrunkShownRole = actor?.clocktowerRole?.enName == "Drunk" && actor.clocktowerShownRole?.enName == enName
        val hostUnreliableNote = if (actorIsDrunkShownRole) {
            "注意：这名玩家真实身份是酒鬼，显示为$roleName。请照常唤醒并给信息，但信息可以不可靠或完全错误。"
        } else if (actorIsPoisoned(actor)) {
            "注意：这名玩家今晚中毒，能力信息可以不可靠或错误。"
        } else {
            null
        }
        val actorAbilityUnreliable = actor != null && actorIsUnreliable(enName, actor)
        val unreliableOptions = actor?.takeIf { actorAbilityUnreliable }?.let(displayOptions).orEmpty()
        val reliableRecommendations = actor?.takeUnless { actorAbilityUnreliable }?.let(reliableDisplayOptions).orEmpty()
        val automaticRecommendations = when {
            !automaticStorytellerInfo -> reliableRecommendations
            actorAbilityUnreliable -> unreliableOptions
            else -> reliableRecommendations
        }
        val resolvedDisplayKind = when (enName) {
            "Chef", "Empath", "Clockmaker", "Chambermaid" -> ClocktowerDisplayKind.Number
            "Fortune Teller" -> ClocktowerDisplayKind.YesNo
            "Ravenkeeper", "Undertaker" -> ClocktowerDisplayKind.RoleReveal
            "Washerwoman", "Librarian", "Investigator" -> ClocktowerDisplayKind.EitherOne
            else -> displayKind
        }
        return ClocktowerNightStepUi(
            title = roleName,
            actor = actor,
            isRealAction = actor != null,
            reason = if (actor != null) "" else roleMissingReason(enName),
            storytellerAction = if (actor != null) {
                listOfNotNull(
                    hostInstruction ?: "轻拍 ${actor.seatLabel(cards)}，示意睁眼。把本步骤信息只给他看；看完后收回手机，示意闭眼。",
                    hostUnreliableNote,
                ).joinToString("\n")
            } else {
                "不要唤醒任何玩家。为了避免玩家通过流程判断角色是否在场，请停顿 2-3 秒，然后点击下一步。"
            },
            tellPlayer = if (actor != null && unreliableOptions.isEmpty()) tellPlayer else null,
            explanation = listOfNotNull(explanation, hostUnreliableNote).joinToString("\n"),
            action = action,
            displayKind = if (actor != null && unreliableOptions.isEmpty() && !tellPlayer.isNullOrBlank()) resolvedDisplayKind else ClocktowerDisplayKind.None,
            displayTitle = displayTitle,
            displayPrimary = if (actor != null && unreliableOptions.isEmpty()) displayPrimary ?: tellPlayer else null,
            displaySecondary = if (actor != null && unreliableOptions.isEmpty()) displaySecondary else null,
            displayFooter = if (actor != null && unreliableOptions.isEmpty()) displayFooter ?: explanation else null,
            displayOptions = if (automaticStorytellerInfo) emptyList() else unreliableOptions,
            recommendedDisplayOptions = automaticRecommendations,
            roleEnName = enName,
            spyRegistrationKey = RegistrationInteractionRules.effectiveRegistrationKey(
                spyRegistrationKey,
                informationAbilityReliable = !actorAbilityUnreliable,
            ),
            spyRegistrationTeams = spyRegistrationTeams,
            spyRegistrationDetail = spyRegistrationDetail,
            spyRegistrationHint = spyRegistrationHint,
            recluseRegistrationKey = RegistrationInteractionRules.effectiveRegistrationKey(
                recluseRegistrationKey,
                informationAbilityReliable = !actorAbilityUnreliable,
            ),
            recluseRegistrationTeams = recluseRegistrationTeams,
        )
    }

    val washerwomanActor = roleActor("Washerwoman")
    val washerwomanRegistrationKey = washerwomanActor?.let { registrationKey("Washerwoman") }
    val washerwomanTarget = spyCard?.takeIf { spyRegistersGood(washerwomanRegistrationKey) }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Townsfolk && it.clocktowerRole?.enName != "Washerwoman" }
    val washerwomanPair = washerwomanTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(washerwomanActor?.name)) }
    val washerwomanOrderedPair = orderedPair(washerwomanPair?.first, washerwomanPair?.second, "Washerwoman-${washerwomanPair?.first?.name}-${washerwomanPair?.second?.name}")
    val librarianActor = roleActor("Librarian")
    val librarianRegistrationKey = librarianActor?.let { registrationKey("Librarian") }
    val librarianTarget = spyCard?.takeIf { spyRegistersGood(librarianRegistrationKey) }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Outsider }
    val librarianPair = librarianTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(librarianActor?.name)) }
    val librarianOrderedPair = orderedPair(librarianPair?.first, librarianPair?.second, "Librarian-${librarianPair?.first?.name}-${librarianPair?.second?.name}")
    val investigatorActor = roleActor("Investigator")
    val investigatorRegistrationKey = investigatorActor?.let { registrationKey("Investigator") }
    val investigatorRecluseRegistrationKey = investigatorActor?.let {
        recluseCard?.let { recluse -> registrationKey("InvestigatorRecluse", recluse.name) }
    }
    val investigatorTarget = recluseCard?.takeIf { recluseRegistersEvil(investigatorRecluseRegistrationKey) }
        ?: spyCard?.takeIf { !spyRegistersGood(investigatorRegistrationKey) }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion && it.clocktowerRole?.enName != "Spy" }
    val investigatorRevealedRole = if (investigatorTarget?.name == recluseCard?.name) {
        recluseRegisteredRole(investigatorRecluseRegistrationKey, listOf(ClocktowerTeam.Minion))
    } else {
        investigatorTarget?.clocktowerRole
    }
    val investigatorPair = investigatorTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(investigatorActor?.name)) }
    val investigatorOrderedPair = orderedPair(investigatorPair?.first, investigatorPair?.second, "Investigator-${investigatorPair?.first?.name}-${investigatorPair?.second?.name}")
    val clockmakerValue = clockmakerNumber()
    val clockmakerNumber = clockmakerValue.toString()
    val empathActor = roleActor("Empath")
    val empathNeighbors = empathActor?.let { livingNeighbors(cards, it.name) }.orEmpty()
    val empathAbilityUnreliable = empathActor?.let { actorIsUnreliable("Empath", it) } == true
    val empathRegistrationKey = empathActor?.takeIf { actor -> empathNeighbors.any { it.name == spyCard?.name } }?.let { registrationKey("Empath", it.name) }
    val empathRecluseRegistrationKey = empathActor
        ?.takeIf { empathNeighbors.any { neighbor -> neighbor.name == recluseCard?.name } }
        ?.let { registrationKey("EmpathRecluse", it.name) }
    fun registeredIsEvil(card: PlayerCard, spyKey: String?, recluseKey: String?): Boolean = when {
        card.name == spyCard?.name && spyRegistersGood(spyKey) -> false
        card.name == recluseCard?.name && recluseRegistersEvil(recluseKey) -> true
        else -> isClocktowerEvil(card)
    }
    val chefActor = roleActor("Chef")
    val chefAbilityUnreliable = chefActor?.let { actorIsUnreliable("Chef", it) } == true
    val chefRegistrationKey = chefActor?.let { registrationKey("Chef") }
    val chefRecluseRegistrationKey = chefActor?.let {
        recluseCard?.let { recluse -> registrationKey("ChefRecluse", recluse.name) }
    }
    val chefValue = chefEvilPairs(cards) { card -> registeredIsEvil(card, chefRegistrationKey, chefRecluseRegistrationKey) }
    val chefActualIdentityValue = chefEvilPairs(cards)
    val chefSpyGoodValue = chefEvilPairs(cards) { card ->
        if (card.name == spyCard?.name) false else isClocktowerEvil(card)
    }
    val chefReferenceValue = if (chefAbilityUnreliable) chefActualIdentityValue else chefValue
    val chefRegistrationHint = when {
        chefAbilityUnreliable && (spyCard != null || recluseCard != null) -> text(
            "厨师能力不可靠：直接选择最终展示数字，不需要先裁定间谍或隐士如何登记。",
            "The Chef is unreliable: choose the final shown number directly; no Spy or Recluse registration ruling is required.",
        )
        chefRegistrationKey != null && spyCard != null -> text(
            "结果预览：间谍按真实邪恶登记时为 $chefActualIdentityValue；登记善良时为 $chefSpyGoodValue。",
            "Result preview: $chefActualIdentityValue if the Spy registers as actual evil; $chefSpyGoodValue if the Spy registers as good.",
        )
        else -> null
    }
    val chefMaximumValue = maxOf(
        chefValue,
        cards.count(::isClocktowerEvil) + if (recluseCard != null) 1 else 0,
    )
    val chefNumber = chefValue.toString()
    val empathValue = empathActor?.let { actor ->
        empathEvilNeighborCount(cards, actor.name) {
            registeredIsEvil(it, empathRegistrationKey, empathRecluseRegistrationKey)
        }
    } ?: 0
    val empathActualIdentityValue = empathNeighbors.count(::isClocktowerEvil)
    val empathSpyActualValue = empathNeighbors.count { neighbor ->
        if (neighbor.name == spyCard?.name) true else registeredIsEvil(neighbor, null, empathRecluseRegistrationKey)
    }
    val empathSpyGoodValue = empathNeighbors.count { neighbor ->
        if (neighbor.name == spyCard?.name) false else registeredIsEvil(neighbor, null, empathRecluseRegistrationKey)
    }
    val empathReferenceValue = if (empathAbilityUnreliable) empathActualIdentityValue else empathValue
    val empathRegistrationHint = when {
        empathAbilityUnreliable && (empathRegistrationKey != null || empathRecluseRegistrationKey != null) -> text(
            "共情者能力不可靠：直接选择最终展示数字，不需要先裁定间谍或隐士如何登记。",
            "The Empath is unreliable: choose the final shown number directly; no Spy or Recluse registration ruling is required.",
        )
        empathRegistrationKey != null -> text(
            "结果预览：保持其他裁定不变，间谍按真实邪恶登记时为 $empathSpyActualValue；登记善良时为 $empathSpyGoodValue。",
            "Result preview with other rulings unchanged: $empathSpyActualValue if the Spy registers as actual evil; $empathSpyGoodValue if the Spy registers as good.",
        )
        else -> null
    }
    val empathNumber = empathValue.toString()
    val undertakerTarget = lastExecutedName?.let { name -> cards.firstOrNull { it.name == name } }
    val undertakerRegistrationKey = undertakerTarget?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Undertaker", it.name) }
    val undertakerRecluseRegistrationKey = undertakerTarget?.takeIf { it.name == recluseCard?.name }?.let { registrationKey("UndertakerRecluse", it.name) }
    val ravenkeeperTargetCard = ravenkeeperTarget?.let { name -> cards.firstOrNull { it.name == name } }
    val ravenkeeperRegistrationKey = ravenkeeperTargetCard?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Ravenkeeper", it.name) }
    val ravenkeeperRecluseRegistrationKey = ravenkeeperTargetCard?.takeIf { it.name == recluseCard?.name }?.let { registrationKey("RavenkeeperRecluse", it.name) }
    val demonCard = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon }
    val sageNightDeath = resolvedNightDeathCard
        ?.takeIf { nightDeathWillOccur && it.clocktowerRole?.enName == "Sage" }
    val sagePair = demonCard?.let { storytellerPairHint(it, cards) }
    val minionCards = cards.filter { it.clocktowerTeam == ClocktowerTeam.Minion }
    fun seatNumberText(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    fun twoSeatNumbers(first: PlayerCard?, second: PlayerCard?): String? =
        if (first != null && second != null) "${seatNumberText(first)}   ${seatNumberText(second)}" else null
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
    val legalDemonBluffs = legalDemonBluffRoles(
        scriptRoles = clocktowerRolesForScript(script),
        inPlayRoleNames = cards.mapNotNull { it.clocktowerRole?.enName }.toSet(),
    )
    val appliedDemonBluffs = recommendedDemonBluffRoleNames
        .mapNotNull { roleName -> legalDemonBluffs.firstOrNull { it.enName == roleName } }
        .distinctBy(ClocktowerRole::enName)
    val demonBluffs = if (appliedDemonBluffs.size == 3) appliedDemonBluffs else legalDemonBluffs.take(3)
    val minionInfoText = demonCard?.let { stringResource(R.string.clocktower_first_night_minion_info_format, it.seatLabel(cards)) }
    val demonInfoText = buildList {
        add(
            if (minionCards.isEmpty()) {
                stringResource(R.string.clocktower_first_night_demon_no_minions)
            } else {
                stringResource(
                    R.string.clocktower_first_night_demon_minions_format,
                    minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                )
            },
        )
        add(
            stringResource(
                R.string.clocktower_first_night_demon_bluffs_format,
                demonBluffs.joinToString(stringResource(R.string.name_separator)) { it.nameFor(language) },
            ),
        )
    }.joinToString("\n")
    val minionInfoTitle = stringResource(R.string.clocktower_first_night_minion_title)
    val demonInfoTitle = stringResource(R.string.clocktower_first_night_demon_title)
    val unfilteredNightSteps = if (phase == ClocktowerPhase.FirstNight) {
        listOf(
            ClocktowerNightStepUi(
                title = stringResource(R.string.clocktower_first_night_minion_title),
                actor = minionCards.firstOrNull().takeIf { shouldGiveFirstNightEvilInfo },
                isRealAction = minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo,
                reason = when {
                    !shouldGiveFirstNightEvilInfo -> stringResource(R.string.clocktower_first_night_small_game_no_evil_info_reason)
                    minionCards.isEmpty() -> stringResource(R.string.clocktower_first_night_no_minions_reason)
                    else -> ""
                },
                storytellerAction = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) {
                    stringResource(
                        R.string.clocktower_first_night_minion_action_format,
                        minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                    )
                } else {
                    stringResource(R.string.clocktower_first_night_placeholder_action)
                },
                tellPlayer = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) minionInfoText else null,
                explanation = if (shouldGiveFirstNightEvilInfo) {
                    stringResource(R.string.clocktower_first_night_minion_explain)
                } else {
                    stringResource(R.string.clocktower_first_night_small_game_no_evil_info_explain)
                },
                displayKind = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo && minionInfoText != null) ClocktowerDisplayKind.EvilInfo else ClocktowerDisplayKind.None,
                displayTitle = stringResource(R.string.clocktower_first_night_minion_title),
                displayPrimary = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) "${stringResource(R.string.clocktower_evil_display_demon)}\n${demonCard?.seatLabel(cards).orEmpty()}" else null,
                displayFooter = null,
                wakeText = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) {
                    stringResource(
                        R.string.clocktower_first_night_minion_wake_format,
                        minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) },
                    )
                } else {
                    null
                },
            ),
            ClocktowerNightStepUi(
                title = stringResource(R.string.clocktower_first_night_demon_title),
                actor = demonCard.takeIf { shouldGiveFirstNightEvilInfo },
                isRealAction = demonCard != null && shouldGiveFirstNightEvilInfo,
                reason = when {
                    !shouldGiveFirstNightEvilInfo -> stringResource(R.string.clocktower_first_night_small_game_no_evil_info_reason)
                    demonCard == null -> stringResource(R.string.clocktower_first_night_no_demon_reason)
                    else -> ""
                },
                storytellerAction = if (demonCard != null && shouldGiveFirstNightEvilInfo) {
                    stringResource(R.string.clocktower_first_night_demon_action_format, demonCard.seatLabel(cards))
                } else {
                    stringResource(R.string.clocktower_first_night_placeholder_action)
                },
                tellPlayer = if (demonCard != null && shouldGiveFirstNightEvilInfo) demonInfoText else null,
                explanation = if (shouldGiveFirstNightEvilInfo) {
                    stringResource(R.string.clocktower_first_night_demon_explain)
                } else {
                    stringResource(R.string.clocktower_first_night_small_game_no_evil_info_explain)
                },
                displayKind = if (demonCard != null && shouldGiveFirstNightEvilInfo) ClocktowerDisplayKind.EvilInfo else ClocktowerDisplayKind.None,
                displayTitle = stringResource(R.string.clocktower_first_night_demon_title),
                displayPrimary = if (demonCard != null && shouldGiveFirstNightEvilInfo) "${stringResource(R.string.clocktower_evil_display_minions)}\n${if (minionCards.isEmpty()) stringResource(R.string.clocktower_first_night_demon_no_minions) else minionCards.joinToString(stringResource(R.string.name_separator)) { it.seatLabel(cards) }}" else null,
                displaySecondary = if (demonCard != null && shouldGiveFirstNightEvilInfo) "${stringResource(R.string.clocktower_evil_display_bluffs)}\n${demonBluffs.joinToString(stringResource(R.string.name_separator)) { it.nameFor(language) }}" else null,
                displayFooter = null,
            ),
            infoStep(
                roleName = "投毒者",
                enName = "Poisoner",
                tellPlayer = poisonTarget?.let { "已选择：${playerSeatLabel(cards, it)}" },
                explanation = "投毒者选择一名玩家，使其能力暂时失效。",
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = "轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。",
            ),
            ClocktowerNightStepUi(
                title = "占卜师红鲱鱼",
                actor = null,
                isRealAction = actualClocktowerRoleCards(cards, "Fortune Teller").isNotEmpty(),
                reason = if (actualClocktowerRoleCards(cards, "Fortune Teller").isEmpty()) "本局没有占卜师，此步骤只用于首夜配置。" else "",
                storytellerAction = "不要公开说明这个选择。请选择一名善良玩家作为红鲱鱼；可以选择占卜师本人。",
                tellPlayer = redHerring?.let { "已选择：${playerSeatLabel(cards, it)}" },
                explanation = "选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。",
                action = ClocktowerNightAction.RedHerring,
                roleEnName = "Fortune Teller",
            ),
            infoStep(
                roleName = "钟表匠",
                enName = "Clockmaker",
                tellPlayer = clockmakerNumber,
                explanation = "这个数字表示恶魔到最近爪牙相隔几步。",
                displayFooter = "恶魔到最近爪牙的距离",
                hostInstruction = "轻拍钟表匠，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。",
                displayOptions = { actor -> recommendedNumberOptions("钟表匠信息", actor, clockmakerValue, cards.size / 2, "恶魔到最近爪牙的距离") },
            ),
            infoStep(
                roleName = "洗衣妇",
                enName = "Washerwoman",
                tellPlayer = washerwomanTarget?.let { "${if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk))?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${washerwomanOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${washerwomanOrderedPair?.second?.seatLabel(cards).orEmpty()}" },
                explanation = "洗衣妇会得知某个镇民在两名玩家之一中。",
                displayPrimary = washerwomanTarget?.let { if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk))?.nameFor(language) else it.clocktowerRole?.nameFor(language) },
                displaySecondary = seatNumbersText(washerwomanOrderedPair),
                displayFooter = "在下面两位玩家之中",
                hostInstruction = "轻拍洗衣妇，示意睁眼。点击“全屏展示给玩家”，只给她看；看完后收回手机，示意闭眼。",
                displayOptions = { actor ->
                    recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Washerwoman, actor)
                },
                reliableDisplayOptions = { actor ->
                    recommendedPairInformationOptions(ClocktowerPairInformationAbility.Washerwoman, actor)
                },
                spyRegistrationKey = washerwomanRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk),
            ),
            infoStep(
                roleName = "图书管理员",
                enName = "Librarian",
                tellPlayer = librarianTarget?.let { "${if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider))?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${librarianOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${librarianOrderedPair?.second?.seatLabel(cards).orEmpty()}" } ?: "本局没有外来者。",
                explanation = "图书管理员会得知某个外来者在两名玩家之一中，或得知没有外来者。",
                displayPrimary = librarianTarget?.let { if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider))?.nameFor(language) else it.clocktowerRole?.nameFor(language) } ?: "没有外来者",
                displaySecondary = seatNumbersText(librarianOrderedPair),
                displayFooter = if (librarianTarget == null) "" else "在下面两位玩家之中",
                hostInstruction = "轻拍图书管理员，示意睁眼。把结果只给他看；如果显示“没有外来者”，也只告诉他本人。",
                displayOptions = { actor ->
                    recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Librarian, actor)
                },
                reliableDisplayOptions = { actor ->
                    recommendedPairInformationOptions(ClocktowerPairInformationAbility.Librarian, actor)
                },
                spyRegistrationKey = librarianRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Outsider),
            ),
            infoStep(
                roleName = "调查员",
                enName = "Investigator",
                tellPlayer = investigatorTarget?.let { "${investigatorRevealedRole?.nameFor(language).orEmpty()} 在这两人之中：${investigatorOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${investigatorOrderedPair?.second?.seatLabel(cards).orEmpty()}" } ?: "本局没有爪牙。",
                explanation = "调查员会得知某个爪牙在两名玩家之一中，或得知没有爪牙。",
                displayPrimary = investigatorRevealedRole?.nameFor(language) ?: "没有爪牙",
                displaySecondary = seatNumbersText(investigatorOrderedPair),
                displayFooter = if (investigatorTarget == null) "" else "在下面两位玩家之中",
                hostInstruction = "轻拍调查员，示意睁眼。把结果只给他看；不要让其他玩家看到被点名的两人。",
                displayOptions = { actor ->
                    listOfNotNull(recommendedDrunkInvestigatorOption(actor)) +
                        recommendedUnreliablePairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                },
                reliableDisplayOptions = { actor ->
                    recommendedPairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                },
                spyRegistrationKey = investigatorRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                recluseRegistrationKey = investigatorRecluseRegistrationKey,
                recluseRegistrationTeams = listOf(ClocktowerTeam.Minion),
            ),
            infoStep(
                roleName = "厨师",
                enName = "Chef",
                tellPlayer = chefNumber,
                explanation = listOfNotNull("这个数字表示有几对邪恶玩家相邻而坐。", chefRegistrationHint).joinToString("\n"),
                hostInstruction = "轻拍厨师，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。",
                displayOptions = { actor -> recommendedNumberOptions("厨师信息", actor, chefReferenceValue, chefMaximumValue, "邪恶玩家相邻对数", pressureCostPerPoint = 1) },
                spyRegistrationKey = chefRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                spyRegistrationHint = chefRegistrationHint,
                recluseRegistrationKey = chefRecluseRegistrationKey,
            ),
            infoStep(
                roleName = "共情者",
                enName = "Empath",
                tellPlayer = empathNumber,
                explanation = listOfNotNull("这个数字表示共情者两个存活邻居中有几个邪恶玩家。", empathRegistrationHint).joinToString("\n"),
                hostInstruction = "轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。",
                displayOptions = { actor -> recommendedNumberOptions("共情者信息", actor, empathReferenceValue, 2, "邪恶存活邻居数量", pressureCostPerPoint = 1) },
                spyRegistrationKey = empathRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                spyRegistrationHint = empathRegistrationHint,
                recluseRegistrationKey = empathRecluseRegistrationKey,
            ),
            infoStep(
                roleName = "侍女",
                enName = "Chambermaid",
                tellPlayer = chambermaidResult,
                explanation = "侍女选择两名玩家，得知其中有几人今晚因自己的能力醒来。",
                action = ClocktowerNightAction.Chambermaid,
                displaySecondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = "查询这两名玩家",
                hostInstruction = "轻拍侍女，示意睁眼。让她依次指两名玩家，不能选自己；点查询后只展示数字。",
                displayOptions = { actor ->
                    chambermaidResult?.toIntOrNull()?.let { trueValue ->
                        recommendedNumberOptions(
                            title = "侍女信息",
                            actor = actor,
                            trueValue = trueValue,
                            maxValue = 2,
                            footer = "查询这两名玩家",
                            pressureCostPerPoint = 1,
                            secondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                        )
                    }.orEmpty()
                },
            ),
            infoStep(
                roleName = "占卜师",
                enName = "Fortune Teller",
                tellPlayer = fortuneTellerResult,
                explanation = "如果两名玩家中包含恶魔或红鲱鱼，向占卜师展示“有”；否则展示“没有”。",
                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = "查询这两名玩家",
                hostInstruction = "轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后展示“有”或“没有”。",
                displayOptions = {
                    fortuneTellerMatched?.let { matched ->
                        recommendedYesNoOptions(
                            title = "占卜师信息",
                            truthfulYes = matched,
                            secondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                            footer = "查询这两名玩家",
                        )
                    }.orEmpty()
                },
                recluseRegistrationKey = fortuneTellerRecluseRegistrationKey,
                recluseRegistrationTeams = listOf(ClocktowerTeam.Demon),
            ),
            infoStep(
                roleName = "管家",
                enName = "Butler",
                tellPlayer = butlerMaster?.let { "今天的主人：${playerSeatLabel(cards, it)}" },
                explanation = "管家每天选择一名主人，白天只能在主人投票时投票。",
                action = ClocktowerNightAction.ButlerMaster,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = "轻拍管家，示意睁眼。让他指一名玩家作为今天的主人；记在心里，白天投票时提醒自己核对。",
            ),
            infoStep(
                roleName = "间谍",
                enName = "Spy",
                tellPlayer = if (poisonTarget == spyCard?.name) null else cards.joinToString("\n") { "${it.seatLabel(cards)}：${it.hostRoleLabel(context, GameKind.Clocktower)}" },
                explanation = if (poisonTarget == spyCard?.name) "间谍已中毒：仍照常唤醒，但不要展示真实魔典，也不能改变登记身份。" else "存活间谍每晚可以查看所有玩家的真实身份。",
                displayKind = ClocktowerDisplayKind.Grimoire,
                displayTitle = "魔典",
                displayFooter = "这些是所有玩家的真实身份。只给间谍短暂查看。",
                hostInstruction = if (poisonTarget == spyCard?.name) "照常轻拍间谍示意睁眼，但不要展示真实魔典；停顿后示意闭眼。" else "轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。",
            ),
        )
    } else {
        buildList {
            add(
            infoStep(
                roleName = "投毒者",
                enName = "Poisoner",
                tellPlayer = poisonTarget?.let { "已选择：${playerSeatLabel(cards, it)}" },
                explanation = "投毒者选择一名玩家，使其能力暂时失效。",
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = "轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。",
            ),
            )
            add(
            infoStep(
                roleName = "管家",
                enName = "Butler",
                tellPlayer = butlerMaster?.let { "今天的主人：${playerSeatLabel(cards, it)}" },
                explanation = "管家每天选择一名主人。",
                action = ClocktowerNightAction.ButlerMaster,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = "轻拍管家，示意睁眼。让他指今天的主人；白天投票时用这个记录提醒自己。",
            ),
            )
            add(
            infoStep(
                roleName = "共情者",
                enName = "Empath",
                tellPlayer = empathNumber,
                explanation = listOfNotNull("这个数字表示共情者两个存活邻居中有几个邪恶玩家。", empathRegistrationHint).joinToString("\n"),
                hostInstruction = "轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。",
                displayOptions = { actor -> recommendedNumberOptions("共情者信息", actor, empathReferenceValue, 2, "邪恶存活邻居数量", pressureCostPerPoint = 1) },
                spyRegistrationKey = empathRegistrationKey,
                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                spyRegistrationHint = empathRegistrationHint,
                recluseRegistrationKey = empathRecluseRegistrationKey,
            ),
            )
            add(
            infoStep(
                roleName = "侍女",
                enName = "Chambermaid",
                tellPlayer = chambermaidResult,
                explanation = "侍女选择两名玩家，得知其中有几人今晚因自己的能力醒来。",
                action = ClocktowerNightAction.Chambermaid,
                displaySecondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = "查询这两名玩家",
                hostInstruction = "轻拍侍女，示意睁眼。让她依次指两名玩家，不能选自己；点查询后只展示数字。",
                displayOptions = { actor ->
                    chambermaidResult?.toIntOrNull()?.let { trueValue ->
                        recommendedNumberOptions(
                            title = "侍女信息",
                            actor = actor,
                            trueValue = trueValue,
                            maxValue = 2,
                            footer = "查询这两名玩家",
                            pressureCostPerPoint = 1,
                            secondary = listOfNotNull(chambermaidFirst, chambermaidSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                        )
                    }.orEmpty()
                },
            ),
            )
            add(
            infoStep(
                roleName = "占卜师",
                enName = "Fortune Teller",
                tellPlayer = fortuneTellerResult,
                explanation = "如果两名玩家中包含恶魔或红鲱鱼，向占卜师展示“有”；否则展示“没有”。",
                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = "查询这两名玩家",
                hostInstruction = "轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后展示“有”或“没有”。",
                displayOptions = {
                    fortuneTellerMatched?.let { matched ->
                        recommendedYesNoOptions(
                            title = "占卜师信息",
                            truthfulYes = matched,
                            secondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                            footer = "查询这两名玩家",
                        )
                    }.orEmpty()
                },
                recluseRegistrationKey = fortuneTellerRecluseRegistrationKey,
                recluseRegistrationTeams = listOf(ClocktowerTeam.Demon),
            ),
            )
            if (lastExecutedName != null) {
                add(
                    infoStep(
                        roleName = "送葬者",
                        enName = "Undertaker",
                        tellPlayer = "${playerSeatLabel(cards, lastExecutedName)} 的角色是 ${when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language).orEmpty()
                            else -> undertakerTarget?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}",
                        explanation = "送葬者每晚得知今天被处决玩家的真实身份。",
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = "送葬者信息",
                        displayPrimary = when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language)
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language)
                            else -> undertakerTarget?.clocktowerRole?.nameFor(language)
                        },
                        displayFooter = "今天被处决：${playerSeatLabel(cards, lastExecutedName)}",
                        hostInstruction = "轻拍送葬者，示意睁眼。把今天被处决玩家的真实身份只给他看；看完后收回手机，示意闭眼。",
                        displayOptions = {
                            recommendedRoleRevealOptions(
                                title = "送葬者信息",
                                truthfulRole = undertakerTarget?.clocktowerRole,
                                footer = "今天被处决：${playerSeatLabel(cards, lastExecutedName)}",
                            )
                        },
                        spyRegistrationKey = undertakerRegistrationKey,
                        spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        recluseRegistrationKey = undertakerRecluseRegistrationKey,
                        recluseRegistrationTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                    ),
                )
            }
            add(
            infoStep(
                roleName = "僧侣",
                enName = "Monk",
                tellPlayer = monkProtectedTarget?.let { "已选择保护：${playerSeatLabel(cards, it)}。如果恶魔今晚选择该玩家，他不会死亡。" },
                explanation = "僧侣每晚选择除自己以外的一名玩家。若恶魔今晚攻击被保护的玩家，天亮时宣布无人死亡；不要透露是僧侣保护导致。",
                action = ClocktowerNightAction.MonkProtect,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = "轻拍僧侣，示意睁眼。让他指一名除自己以外的玩家，在下面记录为今晚保护目标。",
            ),
            )
            add(
            ClocktowerNightStepUi(
                title = "恶魔行动",
                actor = aliveCards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon },
                isRealAction = aliveCards.any { it.clocktowerTeam == ClocktowerTeam.Demon },
                reason = if (aliveCards.none { it.clocktowerTeam == ClocktowerTeam.Demon }) "当前没有存活恶魔。" else "",
                storytellerAction = aliveCards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon }?.let {
                    "轻拍 ${it.seatLabel(cards)}，示意睁眼。让他指今晚要杀死的玩家，在下面记录；记录后示意闭眼。"
                } ?: "不要唤醒任何玩家，停顿 2-3 秒后继续。",
                tellPlayer = if (aliveCards.any { it.clocktowerTeam == ClocktowerTeam.Demon }) {
                    if (demonPoisonedTonight) {
                        "恶魔已中毒，今晚杀人会失效。"
                    } else {
                        pendingNightDeath?.let { "已记录：今晚恶魔选择杀死 ${playerSeatLabel(cards, it)}。现在不要宣布死亡，等天亮统一宣布。" }
                    }
                } else {
                    null
                },
                explanation = if (demonPoisonedTonight) "可以记录恶魔选择，但天亮不会因此死亡。" else "恶魔选择的死亡目标会在天亮时统一公布。",
                action = ClocktowerNightAction.DemonKill,
            ),
            )
            val livingImp = demonCard?.takeIf {
                it.eliminatedRound == null && it.clocktowerRole?.enName == "Imp"
            }
            val impSelfKillNeedsSuccessor =
                livingImp != null &&
                    pendingNightDeath == livingImp.name &&
                    !demonPoisonedTonight &&
                    aliveCards.any { it.clocktowerTeam == ClocktowerTeam.Minion }
            if (impSelfKillNeedsSuccessor) {
                add(
                    ClocktowerNightStepUi(
                        title = text("选择新小恶魔", "Choose the new Imp"),
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text(
                            "小恶魔选择自杀。请选择一名存活爪牙成为新的小恶魔。",
                            "The Imp chose themself. Choose a living Minion to become the new Imp.",
                        ),
                        tellPlayer = demonSuccessorTarget?.let { playerSeatLabel(cards, it) },
                        explanation = text(
                            "五名或更多玩家存活且猩红女巫能力正常时，必须由猩红女巫继承。",
                            "With five or more alive, a healthy Scarlet Woman must inherit.",
                        ),
                        action = ClocktowerNightAction.DemonSuccessor,
                        roleEnName = "Imp",
                        decisionOptions = demonSuccessorDecisionOptions(),
                    ),
                )
            }
            if (mayorCanRedirect) {
                val targetedMayor = requireNotNull(mayorTarget)
                add(
                    ClocktowerNightStepUi(
                        title = "市长死亡裁定",
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = "市长被恶魔击杀。选择让市长死亡，或将死亡转移给另一名玩家。",
                        tellPlayer = mayorRedirectTarget?.let { target ->
                            if (target == targetedMayor.name) {
                                "市长死亡"
                            } else {
                                "死亡转移给 ${playerSeatLabel(cards, target)}"
                            }
                        },
                        explanation = "市长保持存活时，可以让另一名玩家代替死亡。选择死亡或受保护的玩家，可能导致今夜无人死亡。",
                        action = ClocktowerNightAction.MayorRedirect,
                        displayKind = ClocktowerDisplayKind.None,
                        roleEnName = "Mayor",
                        decisionOptions = mayorDecisionOptions(targetedMayor),
                    ),
                )
            }
            if (sageNightDeath != null && demonCard != null && sagePair != null) {
                add(
                    infoStep(
                        roleName = "贤者",
                        enName = "Sage",
                        tellPlayer = "${demonCard.seatLabel(cards)} / ${sagePair.second.seatLabel(cards)}",
                        explanation = "贤者被恶魔杀死时，得知恶魔是两名玩家之一。",
                        displayKind = ClocktowerDisplayKind.EitherOne,
                        displayTitle = "贤者信息",
                        displayPrimary = "恶魔",
                        displaySecondary = twoSeatNumbers(demonCard, sagePair.second),
                        displayFooter = "在下面两位玩家之中",
                        hostInstruction = "如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。",
                        displayOptions = { actor -> recommendedSageOptions(actor, demonCard) },
                        reliableDisplayOptions = { actor ->
                            recommendedSageOptions(actor, demonCard, truthfulOnly = true)
                        },
                    ),
                )
            }
            if (ravenkeeperTrigger != null) {
                add(
                    infoStep(
                        roleName = "守鸦人",
                        enName = "Ravenkeeper",
                        tellPlayer = ravenkeeperTarget?.let { "${playerSeatLabel(cards, it)} 的角色是 ${when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language).orEmpty()
                            else -> ravenkeeperTargetCard?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}" },
                        explanation = "守鸦人只有在夜晚死亡时才会当晚醒来，选择一名玩家并得知其真实身份。",
                        action = ClocktowerNightAction.Ravenkeeper,
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = "守鸦人信息",
                        displayPrimary = when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))?.nameFor(language)
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon))?.nameFor(language)
                            else -> ravenkeeperTargetCard?.clocktowerRole?.nameFor(language)
                        },
                        displayFooter = ravenkeeperTarget?.let { "查询目标：${playerSeatLabel(cards, it)}" },
                        hostInstruction = "轻拍 ${ravenkeeperTrigger.seatLabel(cards)}，示意睁眼。让他指一名玩家，在下面记录后把该玩家角色只给他看。",
                        displayOptions = {
                            recommendedRoleRevealOptions(
                                title = "守鸦人信息",
                                truthfulRole = ravenkeeperTargetCard?.clocktowerRole,
                                footer = ravenkeeperTarget?.let { "查询目标：${playerSeatLabel(cards, it)}" }.orEmpty(),
                            )
                        },
                        spyRegistrationKey = ravenkeeperRegistrationKey,
                        spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        recluseRegistrationKey = ravenkeeperRecluseRegistrationKey,
                        recluseRegistrationTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                    ),
                )
            }
            add(
                infoStep(
                    roleName = "间谍",
                    enName = "Spy",
                    tellPlayer = if (poisonTarget == spyCard?.name) null else cards.joinToString("\n") { "${it.seatLabel(cards)}：${it.hostRoleLabel(context, GameKind.Clocktower)}" },
                    explanation = if (poisonTarget == spyCard?.name) "间谍已中毒：仍照常唤醒，但不要展示真实魔典，也不能改变登记身份。" else "存活间谍每晚查看真实魔典。",
                    displayKind = ClocktowerDisplayKind.Grimoire,
                    displayTitle = "魔典",
                    displayFooter = "这些是所有玩家的真实身份。只给间谍短暂查看。",
                    hostInstruction = if (poisonTarget == spyCard?.name) "照常唤醒间谍，但不要展示真实魔典。" else "轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。",
                ),
            )
        }
    }

    val filteredNightSteps = unfilteredNightSteps.filter { step ->
        step.isRealAction &&
            (step.roleEnName == null || step.roleEnName in scriptRoleNames) &&
            !(script == ClocktowerScript.NoGreaterJoy && step.title in setOf(minionInfoTitle, demonInfoTitle))
    }
    fun officialNightOrder(step: ClocktowerNightStepUi): Int = if (phase == ClocktowerPhase.FirstNight) {
        when {
            step.title == minionInfoTitle -> 0
            step.title == demonInfoTitle -> 1
            step.roleEnName == "Poisoner" -> 2
            step.roleEnName == "Spy" -> 3
            step.roleEnName == "Clockmaker" -> 4
            step.roleEnName == "Washerwoman" -> 5
            step.roleEnName == "Librarian" -> 6
            step.roleEnName == "Investigator" -> 7
            step.roleEnName == "Chef" -> 8
            step.roleEnName == "Empath" -> 9
            step.action == ClocktowerNightAction.RedHerring -> 10
            step.roleEnName == "Fortune Teller" -> 11
            step.roleEnName == "Butler" -> 12
            step.roleEnName == "Chambermaid" -> 13
            else -> 100
        }
    } else {
        when {
            step.roleEnName == "Poisoner" -> 0
            step.roleEnName == "Monk" -> 1
            step.roleEnName == "Spy" -> 2
            step.action == ClocktowerNightAction.DemonKill -> 3
            step.action == ClocktowerNightAction.DemonSuccessor -> 4
            step.action == ClocktowerNightAction.MayorRedirect -> 5
            step.roleEnName == "Sage" -> 6
            step.roleEnName == "Ravenkeeper" -> 7
            step.roleEnName == "Undertaker" -> 8
            step.roleEnName == "Empath" -> 9
            step.roleEnName == "Fortune Teller" -> 10
            step.roleEnName == "Butler" -> 11
            step.roleEnName == "Chambermaid" -> 12
            else -> 100
        }
    }
    val nightSteps = filteredNightSteps.sortedBy(::officialNightOrder)

    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            onDismiss = { playerDisplayStep = null },
        )
        return
    }

    pendingNewDemonName?.let { newDemonName ->
        val newDemon = cards.firstOrNull { it.name == newDemonName }
        val newDemonStep = ClocktowerNightStepUi(
            title = text("新恶魔", "New Demon"),
            actor = newDemon,
            isRealAction = newDemon != null,
            reason = "",
            storytellerAction = text("唤醒新的小恶魔。", "Wake the new Imp."),
            tellPlayer = text("你现在是小恶魔", "You are now the Imp"),
            explanation = "",
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = text("新身份", "New role"),
            displayPrimary = text("你现在是小恶魔", "You are now the Imp"),
            displayFooter = "",
        )
        ClocktowerNewDemonConfirmationScreen(
            newDemonLabel = newDemon?.seatLabel(cards).orEmpty(),
            hasNewDemon = newDemon != null,
            onShowPlayerDisplay = { playerDisplayStep = newDemonStep },
            onConfirm = onConfirmNewDemon,
        )
        return
    }

    if (phase == ClocktowerPhase.Dawn) {
        ClocktowerDawnSummaryScreen(
            round = round,
            cards = cards,
            events = events,
            pendingNightDeath = pendingNightDeath,
            onEnterDay = onAdvanceFromFirstNight,
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Overview) {
        val highestVoteText = when {
            highestVoteName != null -> text(
                "最高票 · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                "Highest · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
            )
            highestVoteCount >= executionThreshold -> text(
                "最高票 · 平票 $highestVoteCount 票",
                "Highest · tie at $highestVoteCount",
            )
            else -> text("最高票 · 无", "Highest · none")
        }
        ClocktowerDayOverviewScreen(
            round = round,
            cards = cards,
            aliveCount = aliveCards.size,
            executionThreshold = executionThreshold,
            highestVoteText = highestVoteText,
            showSlayerAction = scriptHasSlayer,
            slayerActionEnabled = slayerClaimantCandidates.isNotEmpty(),
            showArtistAction = scriptHasArtist,
            artistActionEnabled = artistClaimantCandidates.isNotEmpty(),
            actionsEnabled = gameOutcome == null,
            onStartNomination = {
                nominatorName = null
                nomineeName = null
                dayMode = ClocktowerDayMode.Nomination
            },
            onOpenSlayer = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
                dayMode = ClocktowerDayMode.Slayer
            },
            onOpenArtist = {
                onSelectArtistClaimant(null)
                dayMode = ClocktowerDayMode.Artist
            },
            onEndDay = {
                onSelectExecution(highestVoteName?.takeIf { highestVoteCount >= executionThreshold })
                dayMode = ClocktowerDayMode.EndConfirm
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Nomination) {
        val nominatorCard = cards.firstOrNull { it.name == nominatorName }
        val nomineeCard = cards.firstOrNull { it.name == nomineeName }
        val virginFirstNomination = nomineeCard?.clocktowerRole?.enName == "Virgin" && !virginUsed
        val virginAbilityWorks = virginFirstNomination && poisonTarget != nomineeCard?.name
        val virginRegistrationKey = nominatorCard
            ?.takeIf { it.name == spyCard?.name && virginFirstNomination }
            ?.let { registrationKey("Virgin", it.name) }
        val virginExecutes = virginAbilityWorks &&
            (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || spyRegistersGood(virginRegistrationKey))
        val specialNotice = when {
            virginExecutes -> text(
                "${playerSeatLabel(cards, nomineeName)} 首次被真实镇民提名：不进行投票，提名者将立即被处决。",
                "${playerSeatLabel(cards, nomineeName)} was first nominated by a Townsfolk: skip voting and execute the nominator.",
            )
            virginFirstNomination -> text(
                "这是圣女第一次被提名，但能力不会处决提名者；记录能力已用过后继续投票。",
                "This is the Virgin's first nomination, but the ability does not execute the nominator. Mark it spent and continue.",
            )
            else -> null
        }
        ClocktowerNominationScreen(
            round = round,
            cards = cards,
            aliveCards = aliveCards,
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            specialNotice = specialNotice,
            specialNoticeIsDanger = virginExecutes,
            continueLabel = when {
                virginExecutes -> text("确认并处决提名者", "Confirm and execute nominator")
                virginFirstNomination -> text("记录能力，进入投票", "Record ability and continue")
                else -> text("确认提名，进入投票", "Confirm nomination and vote")
            },
            actionsEnabled = gameOutcome == null,
            onSelectNominator = { nominatorName = if (nominatorName == it) null else it },
            onSelectNominee = { nomineeName = if (nomineeName == it) null else it },
            onContinue = {
                val chosenNominator = nominatorName
                val chosenNominee = nomineeName
                if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {
                    recordSpyRegistration(virginRegistrationKey, listOf(ClocktowerTeam.Townsfolk))
                    onVirginNomination(chosenNominator, chosenNominee, virginExecutes)
                }
                if (chosenNominator != null && chosenNominee != null && virginExecutes) {
                    onRecordEvent(
                        ClocktowerEventType.Nomination,
                        text("提名", "Nomination"),
                        "${playerSeatLabel(cards, chosenNominator)} → ${playerSeatLabel(cards, chosenNominee)}",
                        listOf(chosenNominator, chosenNominee),
                    )
                }
                if (!virginExecutes) {
                    currentVoteCount = 0
                    dayMode = ClocktowerDayMode.Vote
                }
            },
            onCancel = {
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
            specialContent = {
                if (virginRegistrationKey != null && spyCard != null) {
                    SpyRegistrationPanel(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        cards = cards,
                        spy = spyCard,
                        teams = listOf(ClocktowerTeam.Townsfolk),
                        registersGood = spyRegistersGood(virginRegistrationKey),
                        registeredRoleEnName = spyRegistrationRole[virginRegistrationKey],
                        recommendations = registrationRecommendationOptions(
                            key = virginRegistrationKey,
                            roleEnName = "Virgin",
                            teams = listOf(ClocktowerTeam.Townsfolk),
                            detail = ClocktowerRegistrationDetail.Role,
                            subject = spyCard,
                            isSpy = true,
                            outcomeMisinformationPressure = 5,
                        ),
                        enabled = spyCanRegister(),
                        onRegistersGoodChange = { good ->
                            spyRegistrationGood[virginRegistrationKey] = good
                            if (good && spyRegistrationRole[virginRegistrationKey] == null) {
                                spyRegistrationRole[virginRegistrationKey] = "Washerwoman"
                            }
                        },
                        onRoleChange = { spyRegistrationRole[virginRegistrationKey] = it },
                    )
                }
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Vote) {
        val highestVoteText = when {
            highestVoteName != null -> text(
                "当前最高：${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                "Current highest: ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
            )
            highestVoteCount >= executionThreshold -> text(
                "当前最高为平票：$highestVoteCount 票；暂时无人被处决。",
                "Current high vote is tied at $highestVoteCount; nobody is set for execution.",
            )
            else -> text("当前还没有达到门槛的最高票。", "No qualifying high vote has been recorded yet.")
        }
        val recordVoteEvent = {
            onRecordEvent(
                ClocktowerEventType.Vote,
                text("提名与投票", "Nomination and vote"),
                "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                listOfNotNull(nominatorName, nomineeName),
            )
        }
        ClocktowerVoteScreen(
            round = round,
            cards = cards,
            aliveCount = aliveCards.size,
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            voteCount = currentVoteCount,
            highestVoteText = highestVoteText,
            actionsEnabled = gameOutcome == null,
            onVoteCountChange = { currentVoteCount = it },
            onRecordAndContinue = {
                recordVoteEvent()
                recordCurrentVote()
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
            onRecordAndEndDay = {
                recordVoteEvent()
                onSelectExecution(recordCurrentVote())
                dayMode = ClocktowerDayMode.EndConfirm
            },
            onBack = {
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Nomination
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.EndConfirm) {
        ClocktowerExecutionConfirmScreen(
            round = round,
            cards = cards,
            executionThreshold = executionThreshold,
            selectedExecution = selectedExecution,
            highestVoteCount = highestVoteCount,
            actionsEnabled = gameOutcome == null,
            onConfirm = onConfirmDay,
            onBack = { dayMode = ClocktowerDayMode.Overview },
        )
        return
    }

    if (phase == ClocktowerPhase.FirstNight && !nightStarted) {
        ClocktowerStorytellerRecommendationScreen(
            onStartNight = { nightStarted = true },
        ) {
            StorytellerRecommendationCard(
                automaticStorytellerInfo = automaticStorytellerInfo,
                state = recommendationUiState,
                selectedStyle = selectedRecommendationStyle,
                appliedStyle = appliedRecommendationStyle,
                cards = cards,
                script = script,
                language = language,
                lockedDecisions = lockedRecommendationDecisions,
                onSelectStyle = { selectedRecommendationStyle = it },
                onApply = { plan ->
                    onApplyRecommendation(plan)
                    appliedRecommendationStyle = plan.style
                },
                onReevaluate = { nextLockedDecisions ->
                    lockedRecommendationDecisions = nextLockedDecisions
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                    appliedRecommendationStyle = null
                },
                onClearLocks = {
                    lockedRecommendationDecisions = emptyList()
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                },
            )
        }
        return
    }

    val nightFlowActive = nightStarted || phase == ClocktowerPhase.Night
    if ((phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) && nightFlowActive && nightSteps.isNotEmpty()) {
        val currentStepIndex = nightStepIndex.coerceIn(0, nightSteps.lastIndex)
        val currentStep = nightSteps[currentStepIndex]
        val selectedNightName = when (currentStep.action) {
            ClocktowerNightAction.RedHerring -> redHerring
            ClocktowerNightAction.Poison -> poisonTarget
            ClocktowerNightAction.ButlerMaster -> butlerMaster
            ClocktowerNightAction.MonkProtect -> monkProtectedTarget
            ClocktowerNightAction.DemonKill -> pendingNightDeath
            ClocktowerNightAction.MayorRedirect -> mayorRedirectTarget
            ClocktowerNightAction.DemonSuccessor -> demonSuccessorTarget
            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
            else -> null
        }
        val advanceNightStep = {
            recordSpyRegistration(
                currentStep.spyRegistrationKey,
                currentStep.spyRegistrationTeams,
                currentStep.spyRegistrationDetail,
            )
            recordRecluseRegistration(currentStep.recluseRegistrationKey, currentStep.recluseRegistrationTeams)
            recordNightStep(currentStep)
            if (currentStepIndex < nightSteps.lastIndex) {
                nightStepIndex = currentStepIndex + 1
            } else {
                onConfirmNight()
            }
        }

        ClocktowerNightActiveScreen(
            title = if (phase == ClocktowerPhase.FirstNight) {
                text("第 1 夜", "Night 1")
            } else {
                text("第 $round 夜", "Night $round")
            },
            subtitle = text("当前阶段：${currentStep.title}", "Current: ${currentStep.title}"),
            progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
            canGoPrevious = currentStepIndex > 0,
            nextEnabled = currentStep.action !in setOf(
                ClocktowerNightAction.MayorRedirect,
                ClocktowerNightAction.DemonSuccessor,
            ) || selectedNightName != null,
            onPrevious = {
                if (currentStepIndex > 0) {
                    nightStepIndex = currentStepIndex - 1
                }
            },
            onNext = advanceNightStep,
        ) {
            ClocktowerNightStepCardLocalized(
                automaticStorytellerInfo = automaticStorytellerInfo,
                cards = cards,
                aliveCards = aliveCards,
                step = currentStep,
                spyCard = spyCard,
                spyRegistrationGood = spyRegistersGood(currentStep.spyRegistrationKey),
                spyRegisteredRoleEnName = currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] },
                spyRegistrationRecommendations = registrationRecommendationOptions(currentStep, spyCard, isSpy = true),
                spyCanRegister = spyCanRegister(),
                onSpyRegistrationGoodChange = { good ->
                    currentStep.spyRegistrationKey?.let { key ->
                        spyRegistrationGood[key] = good
                        if (good && currentStep.spyRegistrationDetail == ClocktowerRegistrationDetail.Role && spyRegistrationRole[key] == null) {
                            spyRegistrationRole[key] = completeTroubleBrewingRoles
                                .firstOrNull { it.team in currentStep.spyRegistrationTeams && it.enName != "Spy" }
                                ?.enName
                                .orEmpty()
                        }
                        if (!good && redHerring == spyCard?.name && currentStep.action == ClocktowerNightAction.RedHerring) {
                            onSelectRedHerring(null)
                        }
                    }
                },
                onSpyRegistrationRoleChange = { roleName ->
                    currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] = roleName }
                },
                recluseCard = recluseCard,
                recluseRegistrationEvil = recluseRegistersEvil(currentStep.recluseRegistrationKey),
                recluseRegisteredRoleEnName = currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] },
                recluseRegistrationRecommendations = registrationRecommendationOptions(currentStep, recluseCard, isSpy = false),
                recluseCanRegister = recluseCanRegister(),
                onRecluseRegistrationEvilChange = { evil ->
                    currentStep.recluseRegistrationKey?.let { key ->
                        recluseRegistrationEvil[key] = evil
                        if (evil && currentStep.recluseRegistrationTeams.isNotEmpty() && recluseRegistrationRole[key] == null) {
                            recluseRegistrationRole[key] = completeTroubleBrewingRoles
                                .firstOrNull { it.team in currentStep.recluseRegistrationTeams }
                                ?.enName
                                .orEmpty()
                        }
                    }
                },
                onRecluseRegistrationRoleChange = { roleName ->
                    currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] = roleName }
                },
                selectedName = selectedNightName,
                fortuneTellerFirst = fortuneTellerFirst,
                fortuneTellerSecond = fortuneTellerSecond,
                chambermaidFirst = chambermaidFirst,
                chambermaidSecond = chambermaidSecond,
                onSelectName = { name ->
                    when (currentStep.action) {
                        ClocktowerNightAction.RedHerring -> onSelectRedHerring(if (redHerring == name) null else name)
                        ClocktowerNightAction.Poison -> onSelectPoisonTarget(if (poisonTarget == name) null else name)
                        ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(if (butlerMaster == name) null else name)
                        ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(if (monkProtectedTarget == name) null else name)
                        ClocktowerNightAction.DemonKill -> onSelectNightDeath(if (pendingNightDeath == name) null else name)
                        ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(if (mayorRedirectTarget == name) null else name)
                        ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(if (demonSuccessorTarget == name) null else name)
                        ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(if (ravenkeeperTarget == name) null else name)
                        else -> Unit
                    }
                },
                onSelectFortuneTellerFirst = {
                    onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it)
                },
                onSelectFortuneTellerSecond = {
                    onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it)
                },
                onSelectChambermaidFirst = {
                    onSelectChambermaidFirst(if (chambermaidFirst == it) null else it)
                },
                onSelectChambermaidSecond = {
                    onSelectChambermaidSecond(if (chambermaidSecond == it) null else it)
                },
                onApplyRecommendedDisplayOption = { option ->
                    currentStep.spyRegistrationKey?.let { key ->
                        option.spyRegistersGood?.let { good ->
                            spyRegistrationGood[key] = good
                            if (good) {
                                option.spyRegisteredRoleEnName?.let { spyRegistrationRole[key] = it }
                            } else {
                                spyRegistrationRole.remove(key)
                            }
                        }
                    }
                    currentStep.recluseRegistrationKey?.let { key ->
                        option.recluseRegistersEvil?.let { evil ->
                            recluseRegistrationEvil[key] = evil
                            if (evil) {
                                option.recluseRegisteredRoleEnName?.let { recluseRegistrationRole[key] = it }
                            } else {
                                recluseRegistrationRole.remove(key)
                            }
                        }
                    }
                    recordSpyRegistration(
                        currentStep.spyRegistrationKey,
                        currentStep.spyRegistrationTeams,
                        currentStep.spyRegistrationDetail,
                    )
                    recordRecluseRegistration(
                        currentStep.recluseRegistrationKey,
                        currentStep.recluseRegistrationTeams,
                    )
                },
                onShowPlayerDisplay = { displayStep ->
                    val actor = displayStep.actor
                    val unreliable = actor?.clocktowerRole?.enName == "Drunk" || actor?.name == poisonTarget
                    val shownInformation = listOfNotNull(
                        displayStep.displayPrimary ?: displayStep.tellPlayer,
                        displayStep.displaySecondary,
                        displayStep.displayFooter,
                    ).filter { it.isNotBlank() }.joinToString(" · ")
                    val referencedPlayerNames = InformationReferenceExtractor.extractSeatNumbers(
                        values = listOf(displayStep.displaySecondary, displayStep.displayFooter),
                        maximumSeat = cards.size,
                    ).mapNotNull { seat -> cards.getOrNull(seat - 1)?.name }
                    onRecordEvent(
                        if (unreliable) ClocktowerEventType.UnreliableInformation else ClocktowerEventType.Information,
                        if (unreliable) {
                            text("${displayStep.displayTitle}（不可靠）", "${displayStep.displayTitle} (unreliable)")
                        } else {
                            displayStep.displayTitle
                        },
                        "${actor?.seatLabel(cards).orEmpty()}：$shownInformation",
                        (listOfNotNull(actor?.name) + referencedPlayerNames).distinct(),
                    )
                    playerDisplayStep = displayStep
                },
                canGoPrevious = currentStepIndex > 0,
                onPrevious = {
                    if (currentStepIndex > 0) {
                        nightStepIndex = currentStepIndex - 1
                    }
                },
                onNext = advanceNightStep,
                showNavigationActions = false,
            )
        }
        return
    }

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
                    Text(stringResource(R.string.clocktower_judge_assistant), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(gameOutcome?.title ?: phaseTitle, color = Color(0xFF5C6A63))
                }
            }
        }

        if (phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) {
            if (nightStarted) {
                val currentStepIndex = nightStepIndex.coerceIn(0, nightSteps.lastIndex)
                val currentStep = nightSteps[currentStepIndex]
                item {
                    HostProgressCard(
                        title = if (phase == ClocktowerPhase.FirstNight) "第 1 夜" else "第 $round 夜",
                        subtitle = "当前阶段：${currentStep.title}",
                        progress = "步骤 ${currentStepIndex + 1} / ${nightSteps.size}",
                    )
                }
                item {
                    ClocktowerNightStepCardLocalized(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        cards = cards,
                        aliveCards = aliveCards,
                        step = currentStep,
                        spyCard = spyCard,
                        spyRegistrationGood = spyRegistersGood(currentStep.spyRegistrationKey),
                        spyRegisteredRoleEnName = currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] },
                        spyRegistrationRecommendations = registrationRecommendationOptions(currentStep, spyCard, isSpy = true),
                        spyCanRegister = spyCanRegister(),
                        onSpyRegistrationGoodChange = { good ->
                            currentStep.spyRegistrationKey?.let { key ->
                                spyRegistrationGood[key] = good
                                if (good && currentStep.spyRegistrationDetail == ClocktowerRegistrationDetail.Role && spyRegistrationRole[key] == null) {
                                    spyRegistrationRole[key] = completeTroubleBrewingRoles.firstOrNull { it.team in currentStep.spyRegistrationTeams && it.enName != "Spy" }?.enName.orEmpty()
                                }
                                if (!good && redHerring == spyCard?.name && currentStep.action == ClocktowerNightAction.RedHerring) onSelectRedHerring(null)
                            }
                        },
                        onSpyRegistrationRoleChange = { roleName -> currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] = roleName } },
                        recluseCard = recluseCard,
                        recluseRegistrationEvil = recluseRegistersEvil(currentStep.recluseRegistrationKey),
                        recluseRegisteredRoleEnName = currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] },
                        recluseRegistrationRecommendations = registrationRecommendationOptions(currentStep, recluseCard, isSpy = false),
                        recluseCanRegister = recluseCanRegister(),
                        onRecluseRegistrationEvilChange = { evil ->
                            currentStep.recluseRegistrationKey?.let { key ->
                                recluseRegistrationEvil[key] = evil
                                if (evil && currentStep.recluseRegistrationTeams.isNotEmpty() && recluseRegistrationRole[key] == null) {
                                    recluseRegistrationRole[key] = completeTroubleBrewingRoles
                                        .firstOrNull { it.team in currentStep.recluseRegistrationTeams }
                                        ?.enName
                                        .orEmpty()
                                }
                            }
                        },
                        onRecluseRegistrationRoleChange = { roleName ->
                            currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] = roleName }
                        },
                        selectedName = when (currentStep.action) {
                            ClocktowerNightAction.RedHerring -> redHerring
                            ClocktowerNightAction.Poison -> poisonTarget
                            ClocktowerNightAction.ButlerMaster -> butlerMaster
                            ClocktowerNightAction.MonkProtect -> monkProtectedTarget
                            ClocktowerNightAction.DemonKill -> pendingNightDeath
                            ClocktowerNightAction.MayorRedirect -> mayorRedirectTarget
                            ClocktowerNightAction.DemonSuccessor -> demonSuccessorTarget
                            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
                            else -> null
                        },
                        fortuneTellerFirst = fortuneTellerFirst,
                        fortuneTellerSecond = fortuneTellerSecond,
                        chambermaidFirst = chambermaidFirst,
                        chambermaidSecond = chambermaidSecond,
                        onSelectName = { name ->
                            when (currentStep.action) {
                                ClocktowerNightAction.RedHerring -> onSelectRedHerring(if (redHerring == name) null else name)
                                ClocktowerNightAction.Poison -> onSelectPoisonTarget(if (poisonTarget == name) null else name)
                                ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(if (butlerMaster == name) null else name)
                                ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(if (monkProtectedTarget == name) null else name)
                                ClocktowerNightAction.DemonKill -> onSelectNightDeath(if (pendingNightDeath == name) null else name)
                                ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(if (mayorRedirectTarget == name) null else name)
                                ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(if (demonSuccessorTarget == name) null else name)
                                ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(if (ravenkeeperTarget == name) null else name)
                                else -> Unit
                            }
                        },
                        onSelectFortuneTellerFirst = { onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it) },
                        onSelectFortuneTellerSecond = { onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it) },
                        onSelectChambermaidFirst = { onSelectChambermaidFirst(if (chambermaidFirst == it) null else it) },
                        onSelectChambermaidSecond = { onSelectChambermaidSecond(if (chambermaidSecond == it) null else it) },
                        onApplyRecommendedDisplayOption = { option ->
                            currentStep.spyRegistrationKey?.let { key ->
                                option.spyRegistersGood?.let { good ->
                                    spyRegistrationGood[key] = good
                                    if (good) {
                                        option.spyRegisteredRoleEnName?.let { spyRegistrationRole[key] = it }
                                    } else {
                                        spyRegistrationRole.remove(key)
                                    }
                                }
                            }
                            currentStep.recluseRegistrationKey?.let { key ->
                                option.recluseRegistersEvil?.let { evil ->
                                    recluseRegistrationEvil[key] = evil
                                    if (evil) {
                                        option.recluseRegisteredRoleEnName?.let { recluseRegistrationRole[key] = it }
                                    } else {
                                        recluseRegistrationRole.remove(key)
                                    }
                                }
                            }
                            recordSpyRegistration(
                                currentStep.spyRegistrationKey,
                                currentStep.spyRegistrationTeams,
                                currentStep.spyRegistrationDetail,
                            )
                            recordRecluseRegistration(
                                currentStep.recluseRegistrationKey,
                                currentStep.recluseRegistrationTeams,
                            )
                        },
                        onShowPlayerDisplay = { displayStep ->
                            val actor = displayStep.actor
                            val unreliable = actor?.clocktowerRole?.enName == "Drunk" || actor?.name == poisonTarget
                            val shownInformation = listOfNotNull(
                                displayStep.displayPrimary ?: displayStep.tellPlayer,
                                displayStep.displaySecondary,
                                displayStep.displayFooter,
                            ).filter { it.isNotBlank() }.joinToString(" · ")
                            val referencedPlayerNames = InformationReferenceExtractor.extractSeatNumbers(
                                values = listOf(
                                displayStep.displaySecondary,
                                displayStep.displayFooter,
                                ),
                                maximumSeat = cards.size,
                            ).mapNotNull { seat -> cards.getOrNull(seat - 1)?.name }
                            onRecordEvent(
                                if (unreliable) ClocktowerEventType.UnreliableInformation else ClocktowerEventType.Information,
                                if (unreliable) {
                                    text("${displayStep.displayTitle}（不可靠）", "${displayStep.displayTitle} (unreliable)")
                                } else {
                                    displayStep.displayTitle
                                },
                                "${actor?.seatLabel(cards).orEmpty()}：$shownInformation",
                                (listOfNotNull(actor?.name) + referencedPlayerNames).distinct(),
                            )
                            playerDisplayStep = displayStep
                        },
                        canGoPrevious = currentStepIndex > 0,
                        onPrevious = {
                            if (currentStepIndex > 0) {
                                nightStepIndex = currentStepIndex - 1
                            }
                        },
                        onNext = {
                            recordSpyRegistration(
                                currentStep.spyRegistrationKey,
                                currentStep.spyRegistrationTeams,
                                currentStep.spyRegistrationDetail,
                            )
                            recordRecluseRegistration(currentStep.recluseRegistrationKey, currentStep.recluseRegistrationTeams)
                            recordNightStep(currentStep)
                            if (currentStepIndex < nightSteps.lastIndex) {
                                nightStepIndex = currentStepIndex + 1
                            } else {
                                onConfirmNight()
                            }
                        },
                    )
                }
            }
        } else if (phase == ClocktowerPhase.Dawn) {
            val deathText = pendingNightDeath?.let { playerSeatLabel(cards, it) } ?: "无"
            item {
                HostScriptCard(
                    title = "天亮了",
                    script = "天亮了，所有人睁眼。",
                    action = if (pendingNightDeath == null) {
                        "请宣布：昨晚没有人死亡。"
                    } else {
                        "请宣布：昨晚，$deathText 死亡。"
                    },
                ) {
                    HostInstructionBlock(
                        label = "昨晚死亡",
                        text = deathText,
                        backgroundColor = Color(0xFFFFFCF6),
                        textColor = Color(0xFF1F2925),
                    )
                    Button(
                        onClick = {
                            onSelectNightDeath(null)
                            onAdvanceFromFirstNight()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("进入白天")
                    }
                }
            }
        } else {
            item {
                HostProgressCard(
                    title = "第 $round 天 白天",
                    subtitle = "存活玩家：${aliveCards.size}，处决所需票数：$executionThreshold",
                    progress = when {
                        highestVoteName != null -> "最高票：${playerSeatLabel(cards, highestVoteName)}，$highestVoteCount 票"
                        highestVoteCount >= executionThreshold -> "最高票：平票，$highestVoteCount 票（无人被处决）"
                        else -> "最高票：无"
                    },
                )
            }
            when (dayMode) {
                ClocktowerDayMode.Overview -> {
                    item {
                        HostScriptCard(
                            title = "白天管理",
                            script = "现在自由讨论。有人提名时，点击开始提名。",
                            action = "管理提名、投票、处决。今天结束前会确认是否有人被处决。",
                        ) {
                            Button(
                                onClick = {
                                    nominatorName = null
                                    nomineeName = null
                                    dayMode = ClocktowerDayMode.Nomination
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("开始提名")
                            }
                            if (scriptHasSlayer) {
                                OutlinedButton(
                                    onClick = {
                                        slayerClaimantName = null
                                        slayerTargetName = null
                                        slayerRecluseRegistersDemon = false
                                        dayMode = ClocktowerDayMode.Slayer
                                    },
                                    enabled = gameOutcome == null && slayerClaimantCandidates.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text("杀手行动")
                                }
                            }
                            if (scriptHasArtist) {
                                OutlinedButton(
                                    onClick = {
                                        onSelectArtistClaimant(null)
                                        dayMode = ClocktowerDayMode.Artist
                                    },
                                    enabled = gameOutcome == null && artistClaimantCandidates.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text("艺术家提问")
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    onSelectExecution(highestVoteName?.takeIf { highestVoteCount >= executionThreshold })
                                    dayMode = ClocktowerDayMode.EndConfirm
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("结束白天")
                            }
                        }
                    }
                }

                ClocktowerDayMode.Slayer -> {
                    item {
                        HostScriptCard(
                            title = "杀手行动",
                            script = "选择公开声称自己是杀手的玩家，再选择目标。",
                            action = "真实杀手首次使用时，真实恶魔会死亡；隐士也可由说书人裁定登记为恶魔并死亡。",
                        ) {
                            if (slayerClaimantCandidates.isEmpty()) {
                                HostInstructionBlock(
                                    label = "杀手",
                                    text = "所有存活玩家都已经声称过杀手行动，本局不再提供声称者。",
                                    backgroundColor = Color(0xFFFFFCF6),
                                    textColor = Color(0xFF6F7B74),
                                )
                            } else {
                                HostActionSection(
                                    title = "选择声称者",
                                    helper = "已经声称过杀手行动的玩家不会再出现。",
                                ) {
                                    SelectablePlayerChips(
                                        cards = slayerClaimantCandidates,
                                        selectedName = slayerClaimantName,
                                        enabled = gameOutcome == null,
                                        allCards = cards,
                                        onSelect = {
                                            slayerClaimantName = if (slayerClaimantName == it) null else it
                                            if (slayerTargetName == it) slayerTargetName = null
                                        },
                                    )
                                }
                                HostActionSection(title = "选择目标") {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name != slayerClaimantName },
                                        selectedName = slayerTargetName,
                                        enabled = gameOutcome == null,
                                        allCards = cards,
                                        onSelect = {
                                            slayerTargetName = if (slayerTargetName == it) null else it
                                            slayerRecluseRegistersDemon = false
                                        },
                                    )
                                }
                                if (cards.firstOrNull { it.name == slayerTargetName }?.clocktowerRole?.enName == "Recluse") {
                                    val slayerRecluse = cards.first { it.name == slayerTargetName }
                                    RecluseRegistrationPanel(
                                        automaticStorytellerInfo = automaticStorytellerInfo,
                                        cards = cards,
                                        recluse = slayerRecluse,
                                        teams = listOf(ClocktowerTeam.Demon),
                                        registersEvil = slayerRecluseRegistersDemon,
                                        registeredRoleEnName = if (slayerRecluseRegistersDemon) "Imp" else null,
                                        recommendations = registrationRecommendationOptions(
                                            key = registrationKey("SlayerRecluse", slayerRecluse.name),
                                            roleEnName = "Slayer",
                                            teams = listOf(ClocktowerTeam.Demon),
                                            detail = ClocktowerRegistrationDetail.Role,
                                            subject = slayerRecluse,
                                            isSpy = false,
                                            outcomeMisinformationPressure = 4,
                                        ),
                                        enabled = poisonTarget != slayerTargetName,
                                        onRegistersEvilChange = { slayerRecluseRegistersDemon = it },
                                        onRoleChange = {},
                                    )
                                }
                                Button(
                                    onClick = {
                                        val claimantName = slayerClaimantName
                                        val targetName = slayerTargetName
                                        if (claimantName != null && targetName != null) {
                                            onSlayerShot(claimantName, targetName, slayerRecluseRegistersDemon)
                                            slayerClaimantName = null
                                            slayerTargetName = null
                                            slayerRecluseRegistersDemon = false
                                            dayMode = ClocktowerDayMode.Overview
                                        }
                                    },
                                    enabled = slayerClaimantName != null && slayerTargetName != null && gameOutcome == null,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text("结算杀手行动")
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    slayerClaimantName = null
                                    slayerTargetName = null
                                    slayerRecluseRegistersDemon = false
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("返回白天")
                            }
                        }
                    }
                }

                ClocktowerDayMode.Artist -> {
                    item {
                        HostScriptCard(
                            title = "艺术家提问",
                            script = "选择公开声称自己是艺术家的玩家。艺术家每局一次，可以私下问说书人一个是/否问题。",
                            action = "如果是真艺术家首次提问，请根据魔典回答是/否；如果是酒鬼或假声称，可以给不可靠回答。",
                        ) {
                            HostActionSection(
                                title = "选择提问者",
                                helper = "已经提问过或声称提问过的玩家不会再出现。",
                            ) {
                                SelectablePlayerChips(
                                    cards = artistClaimantCandidates,
                                    selectedName = artistClaimantName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { onSelectArtistClaimant(if (artistClaimantName == it) null else it) },
                                )
                            }
                            val artistClaimant = cards.firstOrNull { it.name == artistClaimantName }
                            if (artistClaimant != null) {
                                HostActionSection(
                                    title = text("输入问题的真实答案", "Enter the truthful answer"),
                                    helper = text(
                                        "系统不知道玩家提出的问题语义，请说书人先根据魔典判断真实答案。",
                                        "The app cannot interpret the player's question. First judge its truthful answer from the Grimoire.",
                                    ),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(true, false).forEach { answer ->
                                            val label = if (answer) text("是", "Yes") else text("否", "No")
                                            if (artistTruthfulAnswer == answer) {
                                                Button(
                                                    onClick = { onSelectArtistTruthfulAnswer(answer) },
                                                    modifier = Modifier.weight(1f),
                                                ) { Text(label) }
                                            } else {
                                                OutlinedButton(
                                                    onClick = { onSelectArtistTruthfulAnswer(answer) },
                                                    modifier = Modifier.weight(1f),
                                                ) { Text(label) }
                                            }
                                        }
                                    }
                                }
                            }
                            if (artistClaimant != null && artistTruthfulAnswer != null) {
                                val artistReliable =
                                    artistClaimant.clocktowerRole?.enName == "Artist" &&
                                        artistClaimant.name != poisonTarget
                                val answerRecommendations = if (artistReliable) {
                                    listOf(Triple(RecommendationStyle.BALANCED, artistTruthfulAnswer, false))
                                } else {
                                    UnreliableCategoricalInformationRecommender.recommend(
                                        listOf(
                                            UnreliableCategoricalCandidate(
                                                id = "yes",
                                                isTruthful = artistTruthfulAnswer,
                                                misinformationPressure = if (artistTruthfulAnswer) 0 else 3,
                                            ),
                                            UnreliableCategoricalCandidate(
                                                id = "no",
                                                isTruthful = !artistTruthfulAnswer,
                                                misinformationPressure = if (artistTruthfulAnswer) 3 else 0,
                                            ),
                                        ),
                                    ).map { recommendation ->
                                        Triple(
                                            recommendation.style,
                                            recommendation.candidateId == "yes",
                                            recommendation.warningIds.isNotEmpty(),
                                        )
                                    }
                                }
                                val automaticArtistRecommendation = AutomaticStorytellerSelector.select(answerRecommendations) {
                                    it.first == RecommendationStyle.BALANCED
                                }
                                val automaticArtistAnswer = automaticArtistRecommendation?.second
                                LaunchedEffect(automaticStorytellerInfo, artistClaimantName, artistTruthfulAnswer, automaticArtistAnswer) {
                                    if (automaticStorytellerInfo && automaticArtistAnswer != null && artistShownAnswer != automaticArtistAnswer) {
                                        onSelectArtistShownAnswer(automaticArtistAnswer)
                                    }
                                }
                                HostActionSection(
                                    title = text("推荐回答", "Recommended answer"),
                                    helper = if (automaticStorytellerInfo) {
                                        text("已自动采用平衡回答。", "The balanced answer has been applied automatically.")
                                    } else if (artistReliable) {
                                        text("能力可靠，必须回答真实结果。", "The ability is reliable; give the truthful result.")
                                    } else {
                                        text("能力不可靠，可以给出真实或错误答案。", "The ability is unreliable; either answer is legal.")
                                    },
                                ) {
                                    answerRecommendations
                                        .filter { !automaticStorytellerInfo || it == automaticArtistRecommendation }
                                        .forEach { (style, answer, warning) ->
                                        val answerLabel = if (answer) text("是", "Yes") else text("否", "No")
                                        val label = if (artistReliable) {
                                            text("规则结果 · $answerLabel", "Rules result · $answerLabel")
                                        } else {
                                            "${recommendationStyleLabel(style)} · $answerLabel${if (warning) text(" · 高影响", " · high impact") else ""}"
                                        }
                                        if (automaticStorytellerInfo) {
                                            Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        } else if (style == RecommendationStyle.BALANCED) {
                                            Button(
                                                onClick = { onSelectArtistShownAnswer(answer) },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) { Text(label) }
                                        } else {
                                            OutlinedButton(
                                                onClick = { onSelectArtistShownAnswer(answer) },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) { Text(label) }
                                        }
                                    }
                                    artistShownAnswer?.let { shown ->
                                        Text(
                                            text(
                                                "当前准备回答：${if (shown) "是" else "否"}",
                                                "Prepared answer: ${if (shown) "Yes" else "No"}",
                                            ),
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = onConfirmArtistQuestion,
                                enabled = artistClaimantName != null &&
                                    artistTruthfulAnswer != null &&
                                    artistShownAnswer != null &&
                                    gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("记录艺术家提问")
                            }
                            OutlinedButton(
                                onClick = {
                                    onSelectArtistClaimant(null)
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("返回白天")
                            }
                        }
                    }
                }

                ClocktowerDayMode.Klutz -> {
                    item {
                        val klutzChoiceCard = cards.firstOrNull { it.name == klutzChoiceName }
                        val klutzRegistrationKey = klutzChoiceCard?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Klutz", it.name) }
                        HostScriptCard(
                            title = "呆瓜选择",
                            script = "${playerSeatLabel(cards, pendingKlutzName)} 是呆瓜，得知自己死亡后必须公开选择一名存活玩家。",
                            action = "如果他选择邪恶玩家，善良阵营失败；选择善良玩家则游戏继续。",
                        ) {
                            HostActionSection(title = "选择呆瓜公开指定的玩家") {
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != pendingKlutzName },
                                    selectedName = klutzChoiceName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { onSelectKlutzChoice(if (klutzChoiceName == it) null else it) },
                                )
                            }
                            if (klutzRegistrationKey != null && spyCard != null) {
                                SpyRegistrationPanel(
                                    automaticStorytellerInfo = automaticStorytellerInfo,
                                    cards = cards,
                                    spy = spyCard,
                                    teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                    registersGood = spyRegistersGood(klutzRegistrationKey),
                                    registeredRoleEnName = spyRegistrationRole[klutzRegistrationKey],
                                    recommendations = registrationRecommendationOptions(
                                        key = klutzRegistrationKey,
                                        roleEnName = "Klutz",
                                        teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                        detail = ClocktowerRegistrationDetail.Role,
                                        subject = spyCard,
                                        isSpy = true,
                                        outcomeMisinformationPressure = 5,
                                    ),
                                    enabled = spyCanRegister(),
                                    onRegistersGoodChange = { good ->
                                        spyRegistrationGood[klutzRegistrationKey] = good
                                        if (good && spyRegistrationRole[klutzRegistrationKey] == null) spyRegistrationRole[klutzRegistrationKey] = "Washerwoman"
                                    },
                                    onRoleChange = { spyRegistrationRole[klutzRegistrationKey] = it },
                                )
                            }
                            Button(
                                onClick = {
                                    recordSpyRegistration(klutzRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))
                                    onConfirmKlutzChoice(spyRegistersGood(klutzRegistrationKey))
                                },
                                enabled = klutzChoiceName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("确认呆瓜选择")
                            }
                        }
                    }
                }

                ClocktowerDayMode.Nomination -> {
                    item {
                        val nominatorCard = cards.firstOrNull { it.name == nominatorName }
                        val nomineeCard = cards.firstOrNull { it.name == nomineeName }
                        val virginFirstNomination = nomineeCard?.clocktowerRole?.enName == "Virgin" && !virginUsed
                        val virginAbilityWorks = virginFirstNomination && poisonTarget != nomineeCard?.name
                        val virginRegistrationKey = nominatorCard?.takeIf { it.name == spyCard?.name && virginFirstNomination }?.let { registrationKey("Virgin", it.name) }
                        val virginExecutes = virginAbilityWorks && (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || spyRegistersGood(virginRegistrationKey))
                        HostScriptCard(
                            title = "提名",
                            script = if (nominatorName != null && nomineeName != null) {
                                "请宣布：${playerSeatLabel(cards, nominatorName)} 提名 ${playerSeatLabel(cards, nomineeName)}。然后请提名人说明理由，再请被提名人辩护。"
                            } else {
                                "选择提名人和被提名人。"
                            },
                            action = when {
                                virginExecutes -> "这是圣女第一次被镇民提名。不要投票，直接处决提名者。"
                                virginFirstNomination -> "这是圣女第一次被提名，但提名者不是真实镇民。圣女能力用过，继续正常投票。"
                                else -> "两名玩家都选好后，进入投票。"
                            },
                        ) {
                            HostActionSection(title = "选择提名人") {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = nominatorName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { nominatorName = if (nominatorName == it) null else it },
                                )
                            }
                            HostActionSection(title = "选择被提名人") {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = nomineeName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { nomineeName = if (nomineeName == it) null else it },
                                )
                            }
                            if (virginRegistrationKey != null && spyCard != null) {
                                SpyRegistrationPanel(
                                    automaticStorytellerInfo = automaticStorytellerInfo,
                                    cards = cards,
                                    spy = spyCard,
                                    teams = listOf(ClocktowerTeam.Townsfolk),
                                    registersGood = spyRegistersGood(virginRegistrationKey),
                                    registeredRoleEnName = spyRegistrationRole[virginRegistrationKey],
                                    recommendations = registrationRecommendationOptions(
                                        key = virginRegistrationKey,
                                        roleEnName = "Virgin",
                                        teams = listOf(ClocktowerTeam.Townsfolk),
                                        detail = ClocktowerRegistrationDetail.Role,
                                        subject = spyCard,
                                        isSpy = true,
                                        outcomeMisinformationPressure = 5,
                                    ),
                                    enabled = spyCanRegister(),
                                    onRegistersGoodChange = { good ->
                                        spyRegistrationGood[virginRegistrationKey] = good
                                        if (good && spyRegistrationRole[virginRegistrationKey] == null) spyRegistrationRole[virginRegistrationKey] = "Washerwoman"
                                    },
                                    onRoleChange = { spyRegistrationRole[virginRegistrationKey] = it },
                                )
                            }
                            if (virginFirstNomination) {
                                HostInstructionBlock(
                                    label = "圣女能力",
                                    text = if (virginExecutes) {
                                        "${playerSeatLabel(cards, nomineeName)} 第一次被真实镇民提名。${playerSeatLabel(cards, nominatorName)} 立即被处决，本次提名不进入投票，白天结束。"
                                    } else {
                                        "${playerSeatLabel(cards, nomineeName)} 第一次被提名，但 ${playerSeatLabel(cards, nominatorName)} 不是真实镇民。不要处决提名者；记录圣女能力已用过，然后继续投票。"
                                    },
                                    backgroundColor = if (virginExecutes) Color(0xFFFFF4DC) else Color(0xFFFFFCF6),
                                    textColor = if (virginExecutes) Color(0xFF9A4B36) else Color(0xFF5C6A63),
                                )
                            }
                            Button(
                                onClick = {
                                    val chosenNominator = nominatorName
                                    val chosenNominee = nomineeName
                                    if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {
                                        recordSpyRegistration(virginRegistrationKey, listOf(ClocktowerTeam.Townsfolk))
                                        onVirginNomination(chosenNominator, chosenNominee, virginExecutes)
                                    }
                                    if (chosenNominator != null && chosenNominee != null) {
                                        if (virginExecutes) {
                                            onRecordEvent(
                                                ClocktowerEventType.Nomination,
                                                text("提名", "Nomination"),
                                                "${playerSeatLabel(cards, chosenNominator)} → ${playerSeatLabel(cards, chosenNominee)}",
                                                listOf(chosenNominator, chosenNominee),
                                            )
                                        }
                                    }
                                    if (!virginExecutes) {
                                        currentVoteCount = executionThreshold
                                        dayMode = ClocktowerDayMode.Vote
                                    }
                                },
                                enabled = nominatorName != null && nomineeName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    when {
                                        virginExecutes -> "处决提名者"
                                        virginFirstNomination -> "记录圣女已用过，开始投票"
                                        else -> "开始投票"
                                    },
                                )
                            }
                        }
                    }
                }

                ClocktowerDayMode.Vote -> {
                    item {
                        HostScriptCard(
                            title = "投票",
                            script = "正在投票：是否处决 ${playerSeatLabel(cards, nomineeName)}。",
                            action = "输入票数。达到 $executionThreshold 票才可能成为今天处决目标。",
                        ) {
                            StepperRow(
                                label = "票数",
                                value = currentVoteCount,
                                range = 0..aliveCards.size,
                                onChange = { currentVoteCount = it },
                            )
                            val reached = currentVoteCount >= executionThreshold
                            HostInstructionBlock(
                                label = "结果",
                                text = if (reached) {
                                    "${playerSeatLabel(cards, nomineeName)} 获得 $currentVoteCount 票，达到处决门槛。"
                                } else {
                                    "${playerSeatLabel(cards, nomineeName)} 获得 $currentVoteCount 票，未达到处决门槛。"
                                },
                                backgroundColor = if (reached) Color(0xFFEAF2EA) else Color(0xFFFFFCF6),
                                textColor = if (reached) Color(0xFF2F5D50) else Color(0xFF6F7B74),
                            )
                            Button(
                                onClick = {
                                    onRecordEvent(
                                        ClocktowerEventType.Vote,
                                        text("提名与投票", "Nomination and vote"),
                                        "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                                        listOfNotNull(nominatorName, nomineeName),
                                    )
                                    recordCurrentVote()
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                enabled = nomineeName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("继续提名")
                            }
                            OutlinedButton(
                                onClick = {
                                    onRecordEvent(
                                        ClocktowerEventType.Vote,
                                        text("提名与投票", "Nomination and vote"),
                                        "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                                        listOfNotNull(nominatorName, nomineeName),
                                    )
                                    onSelectExecution(recordCurrentVote())
                                    dayMode = ClocktowerDayMode.EndConfirm
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("结束白天")
                            }
                        }
                    }
                }

                ClocktowerDayMode.EndConfirm -> {
                    item {
                        val target = selectedExecution
                        HostScriptCard(
                            title = "准备结束白天",
                            script = target?.let { "当前将被处决：${playerSeatLabel(cards, it)}，票数：$highestVoteCount。" } ?: "今天没有玩家被处决。",
                            action = target?.let { "确认处决 ${playerSeatLabel(cards, it)} 吗？" } ?: "确认进入夜晚吗？",
                        ) {
                            Button(
                                onClick = onConfirmDay,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(target?.let { "确认处决" } ?: "进入夜晚")
                            }
                            OutlinedButton(
                                onClick = { dayMode = ClocktowerDayMode.Overview },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("返回白天")
                            }
                        }
                    }
                }

                ClocktowerDayMode.ExecutionResult -> Unit
            }
        }

        item {
            ClocktowerGameRecordPanel(
                cards = cards,
                events = events,
                language = language,
            )
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

    return

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
                    Text(stringResource(R.string.clocktower_judge_assistant), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(gameOutcome?.title ?: phaseTitle, color = Color(0xFF5C6A63))
                }
            }
        }

        item {
            HostProgressCard(
                title = stringResource(R.string.host_current_stage),
                subtitle = phaseTitle,
                progress = phaseProgress,
            )
        }

        item {
            HostScriptCard(
                title = phaseTitle,
                script = phaseScript,
                action = phaseAction,
            ) {
                Text(stringResource(R.string.clocktower_storyteller_hint_body), color = Color(0xFF6F7B74))
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

        when (phase) {
            ClocktowerPhase.FirstNight -> {
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_red_herring_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.clocktower_red_herring_hint), color = Color(0xFF6F7B74))
                            SelectablePlayerChips(
                                cards = clocktowerRedHerringCandidates(aliveCards),
                                selectedName = redHerring,
                                onSelect = { onSelectRedHerring(if (redHerring == it) null else it) },
                                enabled = gameOutcome == null,
                            )
                        }
                    }
                }
                firstNightWasherwoman?.let { washerwoman ->
                    val target = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Townsfolk && it.name != washerwoman.name }
                    val pair = target?.let { storytellerPairHint(it, cards) }
                    if (pair != null && target.clocktowerRole != null) {
                        item {
                            ClocktowerInfoCard(
                                title = stringResource(R.string.clocktower_role_info_format, washerwoman.name, washerwoman.hostRoleLabel(context, GameKind.Clocktower)),
                                body = stringResource(
                                    R.string.clocktower_washerwoman_info,
                                    target.clocktowerRole!!.nameFor(language),
                                    pair.first.name,
                                    pair.second.name,
                                ),
                            )
                        }
                    }
                }
                firstNightLibrarian?.let { librarian ->
                    val outsider = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Outsider }
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, librarian.name, librarian.hostRoleLabel(context, GameKind.Clocktower)),
                            body = if (outsider?.clocktowerRole != null) {
                                val pair = storytellerPairHint(outsider, cards)
                                if (pair != null) {
                                    stringResource(
                                        R.string.clocktower_librarian_info,
                                        outsider.clocktowerRole!!.nameFor(language),
                                        pair.first.name,
                                        pair.second.name,
                                    )
                                } else {
                                    stringResource(R.string.clocktower_no_info)
                                }
                            } else {
                                stringResource(R.string.clocktower_librarian_none)
                            },
                        )
                    }
                }
                firstNightInvestigator?.let { investigator ->
                    val minion = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion }
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, investigator.name, investigator.hostRoleLabel(context, GameKind.Clocktower)),
                            body = if (minion?.clocktowerRole != null) {
                                val pair = storytellerPairHint(minion, cards)
                                if (pair != null) {
                                    stringResource(
                                        R.string.clocktower_investigator_info,
                                        minion.clocktowerRole!!.nameFor(language),
                                        pair.first.name,
                                        pair.second.name,
                                    )
                                } else {
                                    stringResource(R.string.clocktower_no_info)
                                }
                            } else {
                                stringResource(R.string.clocktower_investigator_none)
                            },
                        )
                    }
                }
                chefPlayer?.let { chef ->
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, chef.name, chef.hostRoleLabel(context, GameKind.Clocktower)),
                            body = stringResource(R.string.clocktower_chef_info, chefEvilPairs(cards)),
                        )
                    }
                }
                if (actualClocktowerRoleCards(cards, "Spy").isNotEmpty() || actualClocktowerRoleCards(cards, "Baron").isNotEmpty()) {
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_special_reminders_title),
                            body = buildList {
                                if (actualClocktowerRoleCards(cards, "Spy").isNotEmpty()) {
                                    add(stringResource(R.string.clocktower_spy_hint))
                                }
                                if (actualClocktowerRoleCards(cards, "Baron").isNotEmpty()) {
                                    add(stringResource(R.string.clocktower_baron_hint))
                                }
                            }.joinToString("\n"),
                        )
                    }
                }
                item {
                    Button(
                        onClick = onAdvanceFromFirstNight,
                        enabled = gameOutcome == null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_start_day_one))
                    }
                }
            }

            ClocktowerPhase.Dawn -> Unit

            ClocktowerPhase.Day -> {
                if (butlerPlayers.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_butler_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.clocktower_butler_hint), color = Color(0xFF6F7B74))
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name !in butlerPlayers.map(PlayerCard::name) },
                                    selectedName = butlerMaster,
                                    onSelect = { onSelectButlerMaster(if (butlerMaster == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_execution_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.clocktower_execution_hint), color = Color(0xFF6F7B74))
                            SelectablePlayerChips(
                                cards = aliveCards,
                                selectedName = selectedExecution,
                                onSelect = { onSelectExecution(if (selectedExecution == it) null else it) },
                                enabled = gameOutcome == null,
                            )
                            Button(
                                onClick = onConfirmDay,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    if (selectedExecution == null) {
                                        stringResource(R.string.clocktower_no_execution)
                                    } else {
                                        stringResource(R.string.clocktower_confirm_execution)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            ClocktowerPhase.Night -> {
                if (poisonerPlayers.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_poisoner_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.clocktower_poisoner_hint), color = Color(0xFF6F7B74))
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = poisonTarget,
                                    onSelect = { onSelectPoisonTarget(if (poisonTarget == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                        }
                    }
                }
                empathPlayers.forEach { empath ->
                    val neighbors = livingNeighbors(cards, empath.name)
                    item {
                        ClocktowerInfoCard(
                            title = stringResource(R.string.clocktower_role_info_format, empath.name, empath.hostRoleLabel(context, GameKind.Clocktower)),
                            body = stringResource(
                                R.string.clocktower_empath_info,
                                neighbors.joinToString(stringResource(R.string.name_separator)) { it.name },
                                empathEvilNeighborCount(cards, empath.name),
                            ),
                        )
                    }
                }
                fortuneTellerPlayers.forEach { fortuneTeller ->
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    stringResource(R.string.clocktower_role_info_format, fortuneTeller.name, fortuneTeller.hostRoleLabel(context, GameKind.Clocktower)),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(stringResource(R.string.clocktower_fortune_teller_hint), color = Color(0xFF6F7B74))
                                Text(stringResource(R.string.clocktower_choose_first_target), fontWeight = FontWeight.SemiBold)
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = fortuneTellerFirst,
                                    onSelect = { onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                                Text(stringResource(R.string.clocktower_choose_second_target), fontWeight = FontWeight.SemiBold)
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != fortuneTellerFirst },
                                    selectedName = fortuneTellerSecond,
                                    onSelect = { onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                                fortuneTellerResult?.let { result ->
                                    Text(
                                        stringResource(
                                            R.string.clocktower_fortune_teller_result,
                                            fortuneTellerFirst ?: "",
                                            fortuneTellerSecond ?: "",
                                            result,
                                        ),
                                        color = Color(0xFF2F5D50),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_demon_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.clocktower_demon_hint), color = Color(0xFF6F7B74))
                            SelectablePlayerChips(
                                cards = aliveCards,
                                selectedName = pendingNightDeath,
                                onSelect = { onSelectNightDeath(if (pendingNightDeath == it) null else it) },
                                enabled = gameOutcome == null,
                            )
                            ravenkeeperTrigger?.let {
                                Text(stringResource(R.string.clocktower_ravenkeeper_hint), fontWeight = FontWeight.SemiBold)
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { card -> card.name != ravenkeeperTrigger.name },
                                    selectedName = ravenkeeperTarget,
                                    onSelect = { onSelectRavenkeeperTarget(if (ravenkeeperTarget == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                                ravenkeeperTarget?.let { targetName ->
                                    val target = cards.firstOrNull { it.name == targetName }
                                    if (target != null) {
                                        Text(
                                            stringResource(
                                                R.string.clocktower_ravenkeeper_result,
                                                target.name,
                                                target.hostRoleLabel(context, GameKind.Clocktower),
                                            ),
                                            color = Color(0xFF2F5D50),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = onConfirmNight,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_confirm_night))
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        when (phase) {
                            ClocktowerPhase.FirstNight -> Unit
                            ClocktowerPhase.Dawn -> onPhaseChange(ClocktowerPhase.Night)
                            ClocktowerPhase.Day -> onPhaseChange(ClocktowerPhase.FirstNight)
                            ClocktowerPhase.Night -> onPhaseChange(ClocktowerPhase.Day)
                        }
                    },
                    enabled = phase != ClocktowerPhase.FirstNight,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.previous_step))
                }
                Button(
                    onClick = {
                        when (phase) {
                            ClocktowerPhase.FirstNight -> onAdvanceFromFirstNight()
                            ClocktowerPhase.Dawn -> onAdvanceFromFirstNight()
                            ClocktowerPhase.Day -> onPhaseChange(ClocktowerPhase.Night)
                            ClocktowerPhase.Night -> onPhaseChange(ClocktowerPhase.Day)
                        }
                    },
                    enabled = gameOutcome == null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.next_step))
                }
            }
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.player_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(cards) { card ->
            ClocktowerPlayerStatusRow(card)
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.elimination_records), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text(stringResource(R.string.no_eliminations), color = Color(0xFF6F7B74))
            }
        }

        items(records) { record ->
            Text(record.displayText(), modifier = Modifier.padding(vertical = 4.dp))
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
private fun EvilInfoDisplay(
    primary: String,
    secondary: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        listOfNotNull(primary, secondary?.takeIf { it.isNotBlank() }).forEach { section ->
            val lines = section.lines()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B3833), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    lines.firstOrNull().orEmpty(),
                    color = Color(0xFFAFC7BC),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    lines.drop(1).joinToString("\n"),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerStorytellerRecommendationScreen(
    onStartNight: () -> Unit,
    content: @Composable () -> Unit,
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
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text("说书人开局准备", "STORYTELLER SETUP"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text("首夜裁定推荐", "First-night recommendations"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text(
                            "这是说书人私密页面。确认推荐与裁定后，直接进入首夜流程。",
                            "This is a private Storyteller screen. Review the plan, then begin the first night.",
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text(
                                "不要向玩家展示推荐、真实角色或说书人裁定。",
                                "Do not show recommendations, actual roles, or Storyteller rulings to players.",
                            ),
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item { content() }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                Button(
                    onClick = onStartNight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text("确认裁定，开始首夜", "Confirm plan and begin first night"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StorytellerRecommendationCard(
    automaticStorytellerInfo: Boolean,
    state: RecommendationUiState,
    selectedStyle: RecommendationStyle,
    appliedStyle: RecommendationStyle?,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    language: String,
    lockedDecisions: List<StorytellerDecision>,
    onSelectStyle: (RecommendationStyle) -> Unit,
    onApply: (RecommendationPlan) -> Unit,
    onReevaluate: (List<StorytellerDecision>) -> Unit,
    onClearLocks: () -> Unit,
) {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    fun styleName(style: RecommendationStyle): String = when (style) {
        RecommendationStyle.GENTLE -> text("稳健", "Gentle")
        RecommendationStyle.BALANCED -> text("平衡", "Balanced")
        RecommendationStyle.AGGRESSIVE -> text("激进", "Aggressive")
    }
    fun roleName(roleId: RoleId): String = clocktowerRolesForScript(script)
        .firstOrNull { it.enName == roleId.value }
        ?.nameFor(language)
        ?: roleId.value
    fun seatLabel(seat: Int): String = cards.getOrNull(seat - 1)?.seatLabel(cards)
        ?: text("${seat}号", "Seat $seat")
    fun scoreReason(ruleId: String): String = when (ruleId) {
        "red-herring-role-suitability" -> text("红鲱鱼身份适合制造可解释的误导", "The red herring creates explainable misinformation")
        "red-herring-sensitive-role" -> text("避免让关键善良角色承受过重压力", "Avoids excessive pressure on a key good role")
        "drunk-shown-role-suitability" -> text("酒鬼展示身份适合持续提供错误信息", "The Drunk's shown role supports ongoing misinformation")
        "drunk-non-information-role" -> text("酒鬼展示为非信息角色，误导空间较少", "A non-information shown role gives the Drunk less useful misinformation")
        "investigator-display-suitability" -> text("调查员假信息具有清晰的讨论价值", "The Investigator misinformation creates a clear discussion hook")
        "drunk-info-avoids-real-evil" -> text("假信息不会直接压中真实邪恶玩家", "The misinformation avoids directly naming real evil")
        "drunk-info-hits-real-evil" -> text("假信息会直接命中真实邪恶玩家", "The misinformation directly names real evil")
        "red-herring-overlaps-drunk-info" -> text("不同误导线索没有堆在同一名玩家身上", "Different misinformation threads do not pile onto one player")
        "one-empath-protected-candidate" -> text("候选人中保留一名可被邻座信息交叉验证的玩家", "One candidate can be cross-checked by neighboring information")
        "both-candidates-empath-protected" -> text("两名候选人都容易被邻座信息快速洗清", "Both candidates may be cleared too quickly by neighboring information")
        "drunk-points-to-self" -> text("避免让酒鬼自己的信息指向自己", "Avoids having the Drunk's information point to themself")
        "candidate-critical-exposure" -> text("控制关键角色被集中怀疑的风险", "Controls the risk of exposing a critical role")
        "candidate-discussion-value" -> text("候选组合能产生有价值的桌面讨论", "The candidate pair should generate useful discussion")
        "candidate-seat-spacing" -> text("候选座位距离符合当前风格", "Candidate spacing fits this recommendation style")
        "demon-bluff-ease" -> text("恶魔伪装较容易解释和维持", "The Demon bluffs are practical to maintain")
        else -> ruleId
    }

    var showOtherPlans by remember { mutableStateOf(false) }
    var showDetails by remember(selectedStyle) { mutableStateOf(false) }
    var editingDecisions by remember { mutableStateOf(false) }
    val plans = (state as? RecommendationUiState.Ready)?.plans.orEmpty()
    val selectedPlan = plans.firstOrNull { it.style == selectedStyle } ?: plans.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text("说书人首夜推荐", "Storyteller first-night recommendation"),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (automaticStorytellerInfo) {
                    text("全自动模式已采用平衡方案，不显示其他候选裁定。", "Automatic mode has applied the balanced plan; alternative rulings are hidden.")
                } else {
                    text("默认选择平衡方案；熟练说书人可比较三种风格。", "Balanced is the default; experienced Storytellers can compare all three styles.")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (!automaticStorytellerInfo && lockedDecisions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text("已锁定 ${lockedDecisions.size} 项裁定", "${lockedDecisions.size} decision(s) locked"),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onClearLocks) { Text(text("解除全部", "Clear all")) }
                }
            }

            if (!automaticStorytellerInfo && plans.isNotEmpty() && showOtherPlans) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecommendationStyle.entries.forEach { style ->
                        val enabled = plans.any { it.style == style }
                        if (style == selectedStyle) {
                            Button(
                                onClick = { onSelectStyle(style) },
                                enabled = enabled,
                                shape = RoundedCornerShape(18.dp),
                            ) { Text(styleName(style)) }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectStyle(style) },
                                enabled = enabled,
                                shape = RoundedCornerShape(18.dp),
                            ) { Text(styleName(style)) }
                        }
                    }
                }
            }
            if (!automaticStorytellerInfo && plans.size > 1) {
                TextButton(
                    onClick = {
                        showOtherPlans = !showOtherPlans
                        if (!showOtherPlans) onSelectStyle(RecommendationStyle.BALANCED)
                    },
                ) {
                    Text(if (showOtherPlans) text("只看默认方案", "Show default only") else text("查看其他方案", "View other plans"))
                }
            }

            when (state) {
                RecommendationUiState.Loading -> Text(text("正在计算高质量线索…", "Calculating high-quality information…"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                RecommendationUiState.Empty -> Text(text("当前配置没有找到合法推荐，请使用首夜手动流程。", "No legal recommendation was found; use the manual first-night flow."), color = MaterialTheme.colorScheme.secondary)
                is RecommendationUiState.InvalidLocks -> {
                    Text(text("锁定的裁定不合法或互相冲突，请解除锁定后重试。", "The locked decisions are illegal or incompatible. Clear the locks and try again."), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onClearLocks, modifier = Modifier.fillMaxWidth()) {
                        Text(text("解除锁定并恢复推荐", "Clear locks and restore recommendations"))
                    }
                }
                is RecommendationUiState.Error -> Text(text("推荐暂时不可用：", "Recommendation unavailable: ") + state.message, color = MaterialTheme.colorScheme.error)
                is RecommendationUiState.Ready -> selectedPlan?.let { plan ->
                    val drunkPlayer = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" }
                    val actionLines = plan.decisions.map { decision ->
                        val line = when (decision) {
                            is StorytellerDecision.RedHerring -> text("红鲱鱼：", "Red herring: ") + seatLabel(decision.seat)
                            is StorytellerDecision.DrunkShownRole -> text("酒鬼展示身份：", "Show the Drunk as: ") + roleName(decision.role) + drunkPlayer?.let { " · ${it.seatLabel(cards)}" }.orEmpty()
                            is StorytellerDecision.DrunkInvestigatorInfo -> text("酒鬼调查员信息：", "Drunk Investigator information: ") + roleName(decision.shownMinion) + " · " + decision.candidateSeats.joinToString(" / ") { seatLabel(it) }
                            is StorytellerDecision.DemonBluffs -> text("恶魔伪装：", "Demon bluffs: ") + decision.roles.joinToString(text("、", ", ")) { roleName(it) }
                        }
                        if (lockedDecisions.any { it.kind() == decision.kind() }) "🔒 $line" else line
                    }
                    actionLines.forEach { line -> Text("• $line", style = MaterialTheme.typography.bodyMedium) }

                    if (!automaticStorytellerInfo && editingDecisions) {
                        RecommendationDecisionEditor(
                            plan = plan,
                            lockedDecisions = lockedDecisions,
                            cards = cards,
                            script = script,
                            language = language,
                            onCancel = { editingDecisions = false },
                            onSubmit = { nextLocks ->
                                editingDecisions = false
                                onReevaluate(nextLocks)
                            },
                        )
                    } else if (!automaticStorytellerInfo) {
                        OutlinedButton(
                            onClick = { editingDecisions = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(text("修改裁定", "Edit decisions"))
                        }
                    }

                    val qualityLabel = when (plan.qualityTier) {
                        QualityTier.RECOMMENDED -> text("推荐", "Recommended")
                        QualityTier.ACCEPTABLE_WITH_WARNING -> text("可用，但需留意警告", "Usable with warnings")
                        QualityTier.EXPERT_ONLY -> text("仅建议熟练说书人使用", "Expert only")
                        QualityTier.REJECTED -> text("不可用", "Rejected")
                    }
                    Text(
                        text("质量：", "Quality: ") + qualityLabel,
                        color = if (plan.qualityTier == QualityTier.RECOMMENDED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (showDetails) {
                        Text(text("评分：${plan.totalScore}", "Score: ${plan.totalScore}"), fontWeight = FontWeight.SemiBold)
                        plan.scoreItems
                            .sortedByDescending { kotlin.math.abs(it.delta) }
                            .take(6)
                            .forEach { item ->
                                val sign = if (item.delta >= 0) "+" else ""
                                Text("$sign${item.delta} · ${scoreReason(item.ruleId)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        plan.warnings.forEach { warning ->
                            Text(text("注意：", "Warning: ") + scoreReason(warning.ruleId), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showDetails = !showDetails }, modifier = Modifier.weight(1f)) {
                            Text(if (showDetails) text("收起理由", "Hide reasons") else text("查看推荐理由", "Why this plan"))
                        }
                        if (!automaticStorytellerInfo) {
                            Button(
                                onClick = { onApply(plan) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (appliedStyle == plan.style) text("已采用", "Applied") else text("采用推荐", "Apply plan"))
                            }
                        }
                    }
                    Text(
                        if (automaticStorytellerInfo) {
                            text("以下首夜步骤将直接使用这套平衡信息。", "The first-night steps below will use this balanced information automatically.")
                        } else {
                            text("采用后仍可在下方首夜步骤中手动修改具体裁定。", "After applying, you can still edit individual decisions in the first-night steps below.")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendationDecisionEditor(
    plan: RecommendationPlan,
    lockedDecisions: List<StorytellerDecision>,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    language: String,
    onCancel: () -> Unit,
    onSubmit: (List<StorytellerDecision>) -> Unit,
) {
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    fun roleName(role: ClocktowerRole): String = role.nameFor(language)
    val scriptRoles = clocktowerRolesForScript(script)
    val inPlayRoleNames = cards.mapNotNull { it.clocktowerRole?.enName }.toSet()
    val redHerringOptions = cards.filter {
        it.clocktowerTeam == ClocktowerTeam.Townsfolk || it.clocktowerTeam == ClocktowerTeam.Outsider
    }
    val drunkShownRoleOptions = scriptRoles.filter {
        it.team == ClocktowerTeam.Townsfolk && it.enName !in inPlayRoleNames
    }
    val minionRoleOptions = scriptRoles.filter { it.team == ClocktowerTeam.Minion }
    val demonBluffOptions = scriptRoles.filter {
        it.team in setOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider) && it.enName !in inPlayRoleNames
    }
    var draftDecisions by remember(plan.effectSignature) { mutableStateOf(plan.decisions) }
    var modifiedKinds by remember(plan.effectSignature) { mutableStateOf<Set<StorytellerDecisionKind>>(emptySet()) }

    fun replaceDecision(kind: StorytellerDecisionKind, decision: StorytellerDecision?) {
        draftDecisions = draftDecisions.filterNot { it.kind() == kind } + listOfNotNull(decision)
        modifiedKinds = modifiedKinds + kind
    }

    val redHerring = draftDecisions.filterIsInstance<StorytellerDecision.RedHerring>().singleOrNull()
    val drunkShownRole = draftDecisions.filterIsInstance<StorytellerDecision.DrunkShownRole>().singleOrNull()
    val drunkInfo = draftDecisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().singleOrNull()
    val demonBluffs = draftDecisions.filterIsInstance<StorytellerDecision.DemonBluffs>().singleOrNull()
    val isValidDraft = (drunkShownRole?.role != RoleId("Investigator") || drunkInfo?.candidateSeats?.size == 2) &&
        (demonBluffs == null || demonBluffs.roles.size == 3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text("修改后，该项会被锁定；其他项目将重新计算。", "Changed items will be locked; all other items will be recalculated."), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

            redHerring?.let { current ->
                Text(text("红鲱鱼", "Red herring"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    redHerringOptions.forEach { card ->
                        val seat = cards.indexOf(card) + 1
                        val selected = current.seat == seat
                        if (selected) {
                            Button(onClick = { }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("$seat. ${card.name}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { replaceDecision(StorytellerDecisionKind.RED_HERRING, StorytellerDecision.RedHerring(seat)) },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text("${cards.indexOf(card) + 1}. ${card.name}") }
                        }
                    }
                }
                HorizontalDivider()
            }

            drunkShownRole?.let { current ->
                Text(text("酒鬼展示身份", "Drunk shown role"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    drunkShownRoleOptions.forEach { role ->
                        val selected = current.role.value == role.enName
                        if (selected) {
                            Button(onClick = { }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                Text(roleName(role))
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val nextRole = StorytellerDecision.DrunkShownRole(RoleId(role.enName))
                                    replaceDecision(StorytellerDecisionKind.DRUNK_SHOWN_ROLE, nextRole)
                                    if (role.enName == "Investigator") {
                                        if (drunkInfo == null) {
                                            val defaultMinion = minionRoleOptions.firstOrNull()?.let { RoleId(it.enName) }
                                            if (defaultMinion != null && cards.size >= 2) {
                                                replaceDecision(
                                                    StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                                    StorytellerDecision.DrunkInvestigatorInfo(defaultMinion, listOf(1, 2)),
                                                )
                                            }
                                        }
                                    } else {
                                        replaceDecision(StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO, null)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        }
                    }
                }
                HorizontalDivider()
            }

            if (drunkShownRole?.role == RoleId("Investigator") && drunkInfo != null) {
                Text(text("酒鬼调查员展示的爪牙", "Minion shown to the Drunk Investigator"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    minionRoleOptions.forEach { role ->
                        val selected = drunkInfo.shownMinion.value == role.enName
                        if (selected) {
                            Button(onClick = { }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) { Text(roleName(role)) }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    replaceDecision(
                                        StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                        drunkInfo.copy(shownMinion = RoleId(role.enName)),
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        }
                    }
                }
                Text(text("选择两名候选玩家", "Select two candidate players"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cards.forEachIndexed { index, card ->
                        val seat = index + 1
                        val selected = seat in drunkInfo.candidateSeats
                        if (selected) {
                            Button(
                                onClick = {
                                    replaceDecision(
                                        StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                        drunkInfo.copy(candidateSeats = drunkInfo.candidateSeats.filterNot { it == seat }),
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text("$seat. ${card.name}") }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (drunkInfo.candidateSeats.size < 2) {
                                        replaceDecision(
                                            StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO,
                                            drunkInfo.copy(candidateSeats = (drunkInfo.candidateSeats + seat).sorted()),
                                        )
                                    }
                                },
                                enabled = drunkInfo.candidateSeats.size < 2,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text("$seat. ${card.name}") }
                        }
                    }
                }
                if (drunkInfo.candidateSeats.size != 2) {
                    Text(text("必须选择正好两名玩家。", "Select exactly two players."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
            }

            demonBluffs?.let { current ->
                Text(text("恶魔伪装（选择三个）", "Demon bluffs (select three)"), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    demonBluffOptions.forEach { role ->
                        val roleId = RoleId(role.enName)
                        val selected = roleId in current.roles
                        if (selected) {
                            Button(
                                onClick = {
                                    replaceDecision(
                                        StorytellerDecisionKind.DEMON_BLUFFS,
                                        current.copy(roles = current.roles.filterNot { it == roleId }),
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (current.roles.size < 3) {
                                        replaceDecision(
                                            StorytellerDecisionKind.DEMON_BLUFFS,
                                            current.copy(
                                                roles = (current.roles + roleId).sortedBy { selectedRole ->
                                                    demonBluffOptions.indexOfFirst { it.enName == selectedRole.value }
                                                },
                                            ),
                                        )
                                    }
                                },
                                enabled = current.roles.size < 3,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) { Text(roleName(role)) }
                        }
                    }
                }
                if (current.roles.size != 3) {
                    Text(text("必须选择正好三个伪装角色。", "Select exactly three bluff roles."), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(text("取消", "Cancel")) }
                Button(
                    onClick = {
                        val affectedKinds = modifiedKinds
                        val nextLocks = lockedDecisions.filterNot { it.kind() in affectedKinds } +
                            draftDecisions.filter { it.kind() in affectedKinds }
                        onSubmit(nextLocks)
                    },
                    enabled = modifiedKinds.isNotEmpty() && isValidDraft,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text("锁定并重新评价", "Lock and re-evaluate"))
                }
            }
        }
    }
}

@Composable
private fun ClocktowerPlayerDisplayCardLocalized(
    step: ClocktowerNightStepUi,
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0D10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                step.displayTitle,
                color = Color(0xFFF1EADC),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                when (step.displayKind) {
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                        Text(
                            primary,
                            color = Color(0xFFC5A56A),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        secondary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                color = Color(0xFFF7F1E6),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            footer,
                            color = Color(0xFFF1EADC),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EitherOne -> {
                        Text(
                            primary,
                            color = Color(0xFFF7F1E6),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFC5A56A),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

                    ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                        Text(
                            primary,
                            color = Color(0xFFF7F1E6),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.Grimoire -> {
                        val lines = primary.lines().filter { it.isNotBlank() }
                        val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            lines.forEach { line ->
                                val parts = line.split("：", limit = 2)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1B1F25), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        parts.firstOrNull().orEmpty(),
                                        color = Color(0xFFF1EADC),
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.35f),
                                    )
                                    Text(
                                        parts.getOrNull(1).orEmpty(),
                                        color = Color(0xFFC5A56A),
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.85f),
                                    )
                                }
                            }
                        }
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFAAA397),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.None -> Unit
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC5A56A)),
            ) {
                Text(stringResource(R.string.clocktower_display_close))
            }
        }
    }
}

@Composable
private fun ClocktowerPlayerDisplayCard(
    step: ClocktowerNightStepUi,
    onDismiss: () -> Unit,
) {
    val primary = step.displayPrimary ?: step.tellPlayer.orEmpty()
    val secondary = step.displaySecondary
    val footer = step.displayFooter ?: step.explanation
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1F2925),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                step.displayTitle,
                color = Color(0xFFEAF2EA),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                when (step.displayKind) {
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo -> {
                        Text(
                            primary,
                            color = Color(0xFFFFF4DC),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            footer,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EitherOne -> {
                        Text(
                            primary,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFFFF4DC),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Text(
                            footer,
                            color = Color(0xFFEAF2EA),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    ClocktowerDisplayKind.EvilInfo -> EvilInfoDisplay(primary, secondary)

                    ClocktowerDisplayKind.RoleReveal, ClocktowerDisplayKind.Plain -> {
                        Text(
                            primary,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFEAF2EA),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.Grimoire -> {
                        val lines = primary.lines().filter { it.isNotBlank() }
                        val rowFontSize = if (lines.size >= 10) 14.sp else 16.sp
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            lines.forEach { line ->
                                val parts = line.split("：", limit = 2)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2B3833), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        parts.firstOrNull().orEmpty(),
                                        color = Color.White,
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.35f),
                                    )
                                    Text(
                                        parts.getOrNull(1).orEmpty(),
                                        color = Color(0xFFFFF4DC),
                                        fontSize = rowFontSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.85f),
                                    )
                                }
                            }
                        }
                        if (footer.isNotBlank()) {
                            Text(
                                footer,
                                color = Color(0xFFEAF2EA),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ClocktowerDisplayKind.None -> Unit
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("关闭展示卡")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpyRegistrationPanel(
    cards: List<PlayerCard>,
    spy: PlayerCard,
    teams: List<ClocktowerTeam>,
    registersGood: Boolean,
    registeredRoleEnName: String?,
    detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    hint: String? = null,
    recommendations: List<ClocktowerRegistrationRecommendationOption> = emptyList(),
    automaticStorytellerInfo: Boolean = false,
    enabled: Boolean,
    onRegistersGoodChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val roles = completeTroubleBrewingRoles.filter { it.team in teams && it.enName != "Spy" }
    val automaticRecommendation = AutomaticStorytellerSelector.select(recommendations) {
        it.isDefaultRecommendation
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRecommendation) {
        if (automaticStorytellerInfo && enabled && automaticRecommendation != null) {
            onRegistersGoodChange(automaticRecommendation.usesSpecialRegistration)
            if (automaticRecommendation.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                automaticRecommendation.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                },
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (language == "en") "Private Storyteller ruling" else "说书人私密裁定", fontWeight = FontWeight.Black)
        Text(
            "${spy.seatLabel(cards)} · ${if (language == "en") "this interaction only" else "仅影响本次交互"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        hint?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        if (!enabled) {
            Text(
                if (language == "en") "The Spy is poisoned; registration cannot change." else "间谍已中毒，本次不能改变登记身份。",
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            if (automaticStorytellerInfo && automaticRecommendation != null) {
                Text(
                    if (language == "en") "Automatic balanced ruling" else "已自动采用平衡裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(automaticRecommendation.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (recommendations.isNotEmpty()) {
                Text(
                    if (language == "en") "Recommended ruling" else "推荐裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                recommendations.forEach { recommendation ->
                    val apply = {
                        onRegistersGoodChange(recommendation.usesSpecialRegistration)
                        if (recommendation.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                            recommendation.registeredRoleEnName?.let(onRoleChange)
                        }
                    }
                    if (recommendation.isDefaultRecommendation) {
                        Button(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    } else {
                        OutlinedButton(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    }
                }
                Text(
                    if (language == "en") "Or choose manually" else "或手动裁定",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!automaticStorytellerInfo) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (registersGood) {
                    OutlinedButton(onClick = { onRegistersGoodChange(false) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Actual" else "真实身份")
                    }
                    Button(onClick = { onRegistersGoodChange(true) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Register good" else "登记善良")
                    }
                } else {
                    Button(onClick = { onRegistersGoodChange(false) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Actual" else "真实身份")
                    }
                    OutlinedButton(onClick = { onRegistersGoodChange(true) }, modifier = Modifier.weight(1f)) {
                        Text(if (language == "en") "Register good" else "登记善良")
                    }
                }
            }
            if (registersGood && detail == ClocktowerRegistrationDetail.Role && roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { role ->
                        if (registeredRoleEnName == role.enName) {
                            Button(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        } else {
                            OutlinedButton(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        }
                    }
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecluseRegistrationPanel(
    cards: List<PlayerCard>,
    recluse: PlayerCard,
    teams: List<ClocktowerTeam>,
    registersEvil: Boolean,
    registeredRoleEnName: String?,
    recommendations: List<ClocktowerRegistrationRecommendationOption> = emptyList(),
    automaticStorytellerInfo: Boolean = false,
    enabled: Boolean,
    onRegistersEvilChange: (Boolean) -> Unit,
    onRoleChange: (String) -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val roles = completeTroubleBrewingRoles.filter { it.team in teams }
    val automaticRecommendation = AutomaticStorytellerSelector.select(recommendations) {
        it.isDefaultRecommendation
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRecommendation) {
        if (automaticStorytellerInfo && enabled && automaticRecommendation != null) {
            onRegistersEvilChange(automaticRecommendation.usesSpecialRegistration)
            if (automaticRecommendation.usesSpecialRegistration) {
                automaticRecommendation.registeredRoleEnName?.let(onRoleChange)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                },
                RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(if (language == "en") "Recluse registration" else "隐士登记裁定", fontWeight = FontWeight.Black)
        Text(
            "${recluse.seatLabel(cards)} · ${if (language == "en") "this interaction only" else "仅影响本次交互"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!enabled) {
            Text(
                if (language == "en") "The Recluse is poisoned and must register normally." else "隐士已中毒，本次只能按真实身份登记。",
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            if (automaticStorytellerInfo && automaticRecommendation != null) {
                Text(
                    if (language == "en") "Automatic balanced ruling" else "已自动采用平衡裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(automaticRecommendation.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (recommendations.isNotEmpty()) {
                Text(
                    if (language == "en") "Recommended ruling" else "推荐裁定",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                recommendations.forEach { recommendation ->
                    val apply = {
                        onRegistersEvilChange(recommendation.usesSpecialRegistration)
                        if (recommendation.usesSpecialRegistration) {
                            recommendation.registeredRoleEnName?.let(onRoleChange)
                        }
                    }
                    if (recommendation.isDefaultRecommendation) {
                        Button(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    } else {
                        OutlinedButton(onClick = apply, modifier = Modifier.fillMaxWidth()) {
                            Text(recommendation.label)
                        }
                    }
                }
                Text(
                    if (language == "en") "Or choose manually" else "或手动裁定",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (!automaticStorytellerInfo) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!registersEvil) {
                    Button(onClick = { onRegistersEvilChange(false) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Actual" else "真实身份") }
                } else {
                    OutlinedButton(onClick = { onRegistersEvilChange(false) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Actual" else "真实身份") }
                }
                if (registersEvil) {
                    Button(onClick = { onRegistersEvilChange(true) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Register evil" else "登记邪恶") }
                } else {
                    OutlinedButton(onClick = { onRegistersEvilChange(true) }, modifier = Modifier.weight(1f)) { Text(if (language == "en") "Register evil" else "登记邪恶") }
                }
            }
            if (registersEvil && roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { role ->
                        if (registeredRoleEnName == role.enName) {
                            Button(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        } else {
                            OutlinedButton(onClick = { onRoleChange(role.enName) }) { Text(role.nameFor(language)) }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun ClocktowerNightStepCardLocalized(
    automaticStorytellerInfo: Boolean,
    cards: List<PlayerCard>,
    aliveCards: List<PlayerCard>,
    step: ClocktowerNightStepUi,
    spyCard: PlayerCard?,
    spyRegistrationGood: Boolean,
    spyRegisteredRoleEnName: String?,
    spyRegistrationRecommendations: List<ClocktowerRegistrationRecommendationOption>,
    spyCanRegister: Boolean,
    onSpyRegistrationGoodChange: (Boolean) -> Unit,
    onSpyRegistrationRoleChange: (String) -> Unit,
    recluseCard: PlayerCard?,
    recluseRegistrationEvil: Boolean,
    recluseRegisteredRoleEnName: String?,
    recluseRegistrationRecommendations: List<ClocktowerRegistrationRecommendationOption>,
    recluseCanRegister: Boolean,
    onRecluseRegistrationEvilChange: (Boolean) -> Unit,
    onRecluseRegistrationRoleChange: (String) -> Unit,
    selectedName: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    onSelectName: (String) -> Unit,
    onSelectFortuneTellerFirst: (String) -> Unit,
    onSelectFortuneTellerSecond: (String) -> Unit,
    onSelectChambermaidFirst: (String) -> Unit,
    onSelectChambermaidSecond: (String) -> Unit,
    onApplyRecommendedDisplayOption: (ClocktowerDisplayOption) -> Unit,
    onShowPlayerDisplay: (ClocktowerNightStepUi) -> Unit,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    showNavigationActions: Boolean = true,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val automaticDecision = AutomaticStorytellerSelector.select(step.decisionOptions) {
        it.isDefaultRecommendation
    }
    val automaticDisplayOption = AutomaticStorytellerSelector.select(step.recommendedDisplayOptions) {
        it.isDefaultRecommendation
    }
    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecision?.targetName) {
        if (automaticStorytellerInfo && automaticDecision != null && selectedName != automaticDecision.targetName) {
            onSelectName(automaticDecision.targetName)
        }
    }
    val command = when {
        step.wakeText != null -> step.wakeText
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> "唤醒占卜师：${step.actor.seatLabel(cards)}"
        step.actor != null -> "唤醒 ${step.actor.seatLabel(cards)}"
        else -> step.title
    }
    val helper = when {
        step.action == ClocktowerNightAction.Chambermaid -> "让她选择两名玩家，点查询后直接展示数字。"
        step.action == ClocktowerNightAction.FortuneTeller -> "让他选择两名玩家，点查询后展示“有”或“没有”。"
        step.action == ClocktowerNightAction.RedHerring -> "选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。"
        step.action == ClocktowerNightAction.Poison -> "记录中毒的玩家。"
        step.action == ClocktowerNightAction.ButlerMaster -> "让他选择主人。白天计票时，只有主人投票时，管家才能投票，需要管家自律。"
        step.action == ClocktowerNightAction.MonkProtect -> "记录被保护的玩家。"
        step.action == ClocktowerNightAction.DemonKill -> "记录被击杀的玩家。"
        step.action == ClocktowerNightAction.MayorRedirect -> "选择让市长死亡，或将死亡转移给另一名玩家。"
        step.action == ClocktowerNightAction.DemonSuccessor -> "选择一名合法的存活爪牙成为新的小恶魔。"
        step.action == ClocktowerNightAction.Ravenkeeper -> "选目标后，把该玩家角色只给他看。"
        step.displayKind != ClocktowerDisplayKind.None -> "展示信息给玩家。"
        else -> step.explanation
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (step.actor != null) {
                    if (language == "en") "CURRENT PLAYER" else "当前玩家"
                } else {
                    if (language == "en") "CURRENT STEP" else "当前步骤"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
            )
            Text(
                command.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Black,
            )

            if (step.spyRegistrationKey != null && spyCard != null) {
                SpyRegistrationPanel(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    cards = cards,
                    spy = spyCard,
                    teams = step.spyRegistrationTeams,
                    registersGood = spyRegistrationGood,
                    registeredRoleEnName = spyRegisteredRoleEnName,
                    detail = step.spyRegistrationDetail,
                    hint = step.spyRegistrationHint,
                    recommendations = spyRegistrationRecommendations,
                    enabled = spyCanRegister,
                    onRegistersGoodChange = onSpyRegistrationGoodChange,
                    onRoleChange = onSpyRegistrationRoleChange,
                )
            }
            if (step.recluseRegistrationKey != null && recluseCard != null) {
                RecluseRegistrationPanel(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    cards = cards,
                    recluse = recluseCard,
                    teams = step.recluseRegistrationTeams,
                    registersEvil = recluseRegistrationEvil,
                    registeredRoleEnName = recluseRegisteredRoleEnName,
                    recommendations = recluseRegistrationRecommendations,
                    enabled = recluseCanRegister,
                    onRegistersEvilChange = onRecluseRegistrationEvilChange,
                    onRoleChange = onRecluseRegistrationRoleChange,
                )
            }
            if (step.decisionOptions.isNotEmpty()) {
                HostActionSection(
                    title = if (language == "en") "Recommended ruling" else "推荐裁定",
                    helper = if (automaticStorytellerInfo) {
                        if (language == "en") "The balanced ruling has been applied automatically." else "已自动采用平衡裁定。"
                    } else if (language == "en") {
                        "The balanced option is the beginner default. You can still choose manually below."
                    } else {
                        "平衡方案是新手默认建议；仍可在下方手动裁定。"
                    },
                ) {
                    step.decisionOptions
                        .filter { !automaticStorytellerInfo || it == automaticDecision }
                        .forEach { option ->
                        if (automaticStorytellerInfo) {
                            Text(option.label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else
                        if (automaticStorytellerInfo || option.isDefaultRecommendation) {
                            Button(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        }
                    }
                }
            }

        when (step.action) {
            ClocktowerNightAction.RedHerring -> {
                if (step.isRealAction) {
                    val candidates = clocktowerRedHerringCandidates(aliveCards)
                    HostActionSection(
                        title = stringResource(R.string.clocktower_host_choose_red_herring),
                        helper = stringResource(R.string.clocktower_host_choose_red_herring_hint),
                    ) {
                        if (candidates.isEmpty()) {
                            Text(
                                stringResource(R.string.clocktower_host_no_red_herring_candidates),
                                color = Color(0xFF6F7B74),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            SelectablePlayerChips(
                                cards = candidates,
                                selectedName = selectedName,
                                enabled = true,
                                allCards = cards,
                                onSelect = onSelectName,
                            )
                        }
                    }
                }
            }

            ClocktowerNightAction.Poison -> {
                HostActionSection(title = stringResource(R.string.clocktower_host_choose_poison_target)) {
                    SelectablePlayerChips(
                        cards = aliveCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.ButlerMaster -> {
                HostActionSection(
                    title = if (LocalContext.current.resources.configuration.locales[0].language == "en") "Choose the Butler's master" else "选择管家的主人",
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.MonkProtect -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.FortuneTeller -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SelectableSeatNumbers(
                        cards = aliveCards,
                        selectedName = fortuneTellerFirst,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectFortuneTellerFirst,
                    )
                    SelectableSeatNumbers(
                        cards = aliveCards.filter { it.name != fortuneTellerFirst },
                        selectedName = fortuneTellerSecond,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectFortuneTellerSecond,
                    )
                    if (step.displayOptions.isEmpty()) {
                        Button(
                            onClick = { onShowPlayerDisplay(step) },
                            enabled = step.isRealAction && fortuneTellerFirst != null && fortuneTellerSecond != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("查询并展示")
                        }
                    } else {
                        Text("能力不可靠：请在下方选择最终展示结果。", color = Color(0xFF8C4B20))
                    }
                }
            }

            ClocktowerNightAction.Chambermaid -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val candidates = aliveCards.filter { it.name != step.actor?.name }
                    SelectableSeatNumbers(
                        cards = candidates,
                        selectedName = chambermaidFirst,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectChambermaidFirst,
                    )
                    SelectableSeatNumbers(
                        cards = candidates.filter { it.name != chambermaidFirst },
                        selectedName = chambermaidSecond,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectChambermaidSecond,
                    )
                    if (step.displayOptions.isEmpty()) {
                        Button(
                            onClick = { onShowPlayerDisplay(step) },
                            enabled = step.isRealAction && chambermaidFirst != null && chambermaidSecond != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("查询并展示")
                        }
                    } else {
                        Text("能力不可靠：请在下方选择最终展示结果。", color = Color(0xFF8C4B20))
                    }
                }
            }

            ClocktowerNightAction.DemonKill -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_night_death),
                    helper = stringResource(R.string.clocktower_host_choose_night_death_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            ClocktowerNightAction.MayorRedirect -> {
                val mayor = aliveCards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = "市长被恶魔击杀",
                    helper = "选择死亡或受保护的玩家作为转移目标，可能导致今夜无人死亡。",
                ) {
                    if (mayor != null) {
                        if (selectedName == mayor.name) {
                            Button(
                                onClick = { onSelectName(mayor.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("市长死亡")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(mayor.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("市长死亡")
                            }
                        }
                        Text("或将死亡转移给：", fontWeight = FontWeight.SemiBold)
                        SelectablePlayerChips(
                            cards = cards.filter { it.name != mayor.name },
                            selectedName = selectedName,
                            enabled = step.isRealAction,
                            allCards = cards,
                            onSelect = onSelectName,
                        )
                    }
                }
                }
            }

            ClocktowerNightAction.DemonSuccessor -> {
                val legalNames = step.decisionOptions.map { it.targetName }.toSet()
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == "en") "Choose the new Imp" else "选择新小恶魔",
                    helper = step.explanation,
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name in legalNames },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
                }
            }

            ClocktowerNightAction.Ravenkeeper -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }

            else -> Unit
        }

            step.tellPlayer
                ?.takeIf { step.isRealAction && it.isNotBlank() && step.displayKind == ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid }
                ?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

            if (step.recommendedDisplayOptions.isNotEmpty()) {
                Text("推荐给说书人的完整信息", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    if (automaticStorytellerInfo) {
                        "已自动选定平衡信息；点击下方按钮即可向玩家展示。"
                    } else {
                        "平衡方案适合直接采用；其他方案提供不同压力。选择后会同步本次间谍或隐士登记。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                step.recommendedDisplayOptions
                    .filter { !automaticStorytellerInfo || it == automaticDisplayOption }
                    .sortedBy { if (it.isDefaultRecommendation) 0 else 1 }
                    .forEach { option ->
                        val onClick = {
                            onApplyRecommendedDisplayOption(option)
                            onShowPlayerDisplay(
                                step.copy(
                                    tellPlayer = option.displayPrimary,
                                    displayKind = option.displayKind,
                                    displayTitle = option.displayTitle,
                                    displayPrimary = option.displayPrimary,
                                    displaySecondary = option.displaySecondary,
                                    displayFooter = option.displayFooter,
                                    displayOptions = emptyList(),
                                    recommendedDisplayOptions = emptyList(),
                                ),
                            )
                        }
                        if (option.isDefaultRecommendation) {
                            Button(
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("${if (automaticStorytellerInfo) "自动" else "默认"} · ${option.label}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        }
                    }
                if (!automaticStorytellerInfo) {
                    OutlinedButton(
                        onClick = { onShowPlayerDisplay(step.copy(recommendedDisplayOptions = emptyList())) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("使用上方手动裁定")
                    }
                }
            }

            if (step.displayOptions.isNotEmpty()) {
                Text("能力不可靠：请选择一个结果展示。", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                step.displayOptions.forEach { option ->
                    OutlinedButton(
                        onClick = {
                            onShowPlayerDisplay(
                                step.copy(
                                    tellPlayer = option.displayPrimary,
                                    displayKind = option.displayKind,
                                    displayTitle = option.displayTitle,
                                    displayPrimary = option.displayPrimary,
                                    displaySecondary = option.displaySecondary,
                                    displayFooter = option.displayFooter,
                                    displayOptions = emptyList(),
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                }
            } else if (step.recommendedDisplayOptions.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid) {
                OutlinedButton(
                    onClick = { onShowPlayerDisplay(step) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.clocktower_host_show_to_player))
                }
            }

            if (showNavigationActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPrevious,
                        enabled = canGoPrevious,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.previous_step))
                    }
                    Button(
                        onClick = onNext,
                        enabled = step.action !in setOf(
                            ClocktowerNightAction.MayorRedirect,
                            ClocktowerNightAction.DemonSuccessor,
                        ) || selectedName != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(stringResource(R.string.clocktower_host_finish_next))
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        if (language == "en") "STEP NOTE" else "步骤提示",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        helper,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClocktowerInfoCard(
    title: String,
    body: String,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, color = Color(0xFF5C6A63))
        }
    }
}

@Composable
private fun ClocktowerGameRecordPanel(
    cards: List<PlayerCard>,
    events: List<ClocktowerEvent>,
    language: String,
) {
    var expanded by remember { mutableStateOf(true) }
    val context = LocalContext.current
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val visibleEvents = events
        .filterNot { it.type == ClocktowerEventType.System || it.type == ClocktowerEventType.Phase }
        .sortedByDescending { it.sequence }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text("游戏记录", "Game record"), fontWeight = FontWeight.Bold)
                    Text(
                        text("${visibleEvents.size} 条", "${visibleEvents.size} events"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6F7B74),
                    )
                }
                Text(if (expanded) "▲" else "▼", fontSize = 14.sp)
            }
        }

        if (expanded) {
            if (visibleEvents.isEmpty()) {
                Text(text("暂无记录", "No events yet"), color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    visibleEvents.groupBy { it.phase to it.round }.values.forEach { group ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                clocktowerEventPhaseLabel(group.first(), language),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF6F7B74),
                            )
                            group.forEach { event -> ClocktowerTimelineRow(event = event) }
                        }
                    }
                }
            }

        }

        Text(text("角色信息", "Players"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cards.forEachIndexed { index, card ->
                val alive = card.eliminatedRound == null
                val actualRole = card.clocktowerRole?.nameFor(language) ?: card.hostRoleLabel(context, GameKind.Clocktower)
                val shownSuffix = if (card.clocktowerShownAsDifferentRole() && card.clocktowerShownRole != null) {
                    text("（展示：${card.clocktowerShownRole.nameFor(language)}）", " (shown: ${card.clocktowerShownRole.nameFor(language)})")
                } else {
                    ""
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (alive) Color(0xFFF5F8F6) else Color(0xFFF8F3F1),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("${index + 1}", fontWeight = FontWeight.Black, color = Color(0xFF5C6A63), modifier = Modifier.width(24.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (alive) Color(0xFF2F7D5A) else Color(0xFFB24D3E), CircleShape),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(card.name, fontWeight = FontWeight.SemiBold)
                            Text(actualRole + shownSuffix, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C6A63))
                        }
                        card.clocktowerTeam?.let {
                            Text(it.label(context), style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F7B74))
                        }
                    }
                }
            }
        }
    }
}

private fun clocktowerEventPhaseLabel(event: ClocktowerEvent, language: String): String = when (event.phase) {
    ClocktowerPhase.FirstNight -> if (language == "en") "First night" else "第一夜"
    ClocktowerPhase.Dawn -> if (language == "en") "Dawn" else "天亮"
    ClocktowerPhase.Day -> if (language == "en") "Day ${event.round}" else "第 ${event.round} 天"
    ClocktowerPhase.Night -> if (language == "en") "Night ${event.round}" else "第 ${event.round} 夜"
}

@Composable
private fun ClocktowerTimelineRow(event: ClocktowerEvent) {
    val accent = when (event.type) {
        ClocktowerEventType.System, ClocktowerEventType.Phase -> Color(0xFF3D6F63)
        ClocktowerEventType.Information, ClocktowerEventType.UnreliableInformation -> Color(0xFF3973A8)
        ClocktowerEventType.Nomination, ClocktowerEventType.Vote -> Color(0xFF8C6A22)
        ClocktowerEventType.Execution, ClocktowerEventType.Death, ClocktowerEventType.GameEnd -> Color(0xFFAA493B)
        ClocktowerEventType.RoleAction, ClocktowerEventType.RoleChange -> Color(0xFF76539A)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .background(accent, CircleShape),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(event.title, fontWeight = FontWeight.SemiBold)
            if (event.detail.isNotBlank()) {
                Text(event.detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C6A63))
            }
        }
    }
}

@Composable
private fun ClocktowerPlayerStatusRow(card: PlayerCard) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(card.name, fontWeight = FontWeight.SemiBold)
                    Text(card.hostRoleLabel(context, GameKind.Clocktower), color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
                }
                val status = card.eliminatedRound?.let { stringResource(R.string.eliminated_round_format, it) }
                    ?: stringResource(R.string.active_status)
                Text(status, color = if (card.eliminatedRound == null) Color(0xFF2F5D50) else Color(0xFF9A4B36))
            }
            if (card.clocktowerShownAsDifferentRole() && card.clocktowerShownRole != null) {
                Text(
                    stringResource(R.string.clocktower_shown_role_format, card.clocktowerShownRole.nameFor(context.resources.configuration.locales[0].language)),
                    color = Color(0xFF9A4B36),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

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
private fun ClocktowerResultsDialog(
    cards: List<PlayerCard>,
    outcome: GameOutcome?,
    onDismiss: () -> Unit,
    onReview: () -> Unit,
    onNewGame: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    var rolesRevealed by remember(cards) { mutableStateOf(false) }
    val resultTitle = outcome?.title ?: text("游戏结束", "Game over")
    val goodWon = resultTitle.contains("好人") || resultTitle.contains("Good", ignoreCase = true)
    val evilWon = resultTitle.contains("邪恶") || resultTitle.contains("Evil", ignoreCase = true)
    val accentColor = when {
        goodWon -> Color(0xFF8FB6D6)
        evilWon -> Color(0xFFD96B70)
        else -> Color(0xFFC5A56A)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        ClocktowerDarkTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
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
                                    text = if (rolesRevealed) text("角色揭晓", "ROLE REVEAL") else text("游戏结束", "GAME OVER"),
                                    color = accentColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                )
                                Text(
                                    text = if (rolesRevealed) text("完整魔典", "Final grimoire") else text("胜负结算", "Game result"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            TextButton(onClick = onDismiss) {
                                Text(text("返回主持界面", "Back to host"))
                            }
                        }
                    }

                    if (!rolesRevealed) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                color = accentColor.copy(alpha = 0.13f),
                                shape = RoundedCornerShape(26.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = resultTitle,
                                        color = accentColor,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                    )
                                    outcome?.let {
                                        Text(
                                            text = it.summary,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        )
                                        Text(
                                            text = it.reason,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(18.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Text(
                                        text("角色仍然隐藏", "ROLES ARE STILL HIDDEN"),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                    )
                                    Text(
                                        text(
                                            "确认所有玩家都准备好后，再揭晓真实角色和伪装角色。",
                                            "Reveal only when every player is ready to see actual and shown characters.",
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Button(
                                    onClick = { rolesRevealed = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("确认并揭晓全部角色", "Confirm and reveal all roles"), fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                    Text(text("暂不揭晓", "Not yet"))
                                }
                                TextButton(onClick = onReview, modifier = Modifier.fillMaxWidth()) {
                                    Text(text("复盘操作记录", "Review game log"))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            item {
                                Surface(
                                    color = accentColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(18.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        Text(resultTitle, color = accentColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                        outcome?.let {
                                            Text(it.summary, fontWeight = FontWeight.SemiBold)
                                            Text(it.reason, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                            item {
                                Text(
                                    text("全部玩家与真实角色", "All players and actual roles"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            items(cards) { card ->
                                ClocktowerResultPlayerRow(
                                    card = card,
                                    cards = cards,
                                    context = context,
                                    language = language,
                                )
                            }
                        }
                        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                OutlinedButton(
                                    onClick = onReview,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("复盘本局记录", "Review this game"))
                                }
                                OutlinedButton(
                                    onClick = onNewGame,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(text("结束收尾，准备下一局", "Finish and prepare next game"))
                                }
                                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                    Text(text("返回主持界面", "Back to host"))
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
private fun ClocktowerResultPlayerRow(
    card: PlayerCard,
    cards: List<PlayerCard>,
    context: Context,
    language: String,
) {
    val team = card.clocktowerTeam
    val teamColor = when (team) {
        ClocktowerTeam.Townsfolk -> Color(0xFF8FB6D6)
        ClocktowerTeam.Outsider -> Color(0xFF9AAEC0)
        ClocktowerTeam.Minion -> Color(0xFFD09A6A)
        ClocktowerTeam.Demon -> Color(0xFFD96B70)
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val actualRole = card.hostRoleLabel(context, GameKind.Clocktower)
    val shownRole = card.clocktowerShownRole
        ?.takeIf { card.clocktowerShownAsDifferentRole() }
        ?.nameFor(language)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(teamColor.copy(alpha = 0.17f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (cards.indexOfFirst { it.name == card.name } + 1).toString(),
                    color = teamColor,
                    fontWeight = FontWeight.Black,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(card.name, fontWeight = FontWeight.Bold)
                Text(
                    text = listOfNotNull(team?.label(context), actualRole).joinToString(" · "),
                    color = teamColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                shownRole?.let {
                    Text(
                        text = if (language == "en") "Shown to player: $it" else "对玩家展示为：$it",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = card.eliminatedRound?.let {
                    if (language == "en") "Dead · day $it" else "死亡 · 第 $it 天"
                } ?: if (language == "en") "Alive" else "存活",
                color = if (card.eliminatedRound == null) Color(0xFFA6D8BA) else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
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
                .fillMaxSize()
                .safeDrawingPadding(),
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
    val visibleEvents = events.filterNot { it.type == ClocktowerEventType.System }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (gameKind == GameKind.Clocktower) {
            if (visibleEvents.isEmpty()) {
                item { Text(text("还没有操作记录。", "No game records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(visibleEvents, key = { it.sequence }) { event ->
                GameRecordRow(
                    title = event.title,
                    detail = event.detail,
                    phase = when (event.phase) {
                        ClocktowerPhase.FirstNight -> text("第 1 夜", "Night 1")
                        ClocktowerPhase.Dawn -> text("天亮", "Dawn")
                        ClocktowerPhase.Day -> text("第 ${event.round} 天", "Day ${event.round}")
                        ClocktowerPhase.Night -> text("第 ${event.round} 夜", "Night ${event.round}")
                    },
                )
            }
        } else {
            if (records.isEmpty()) {
                item { Text(text("还没有操作记录。", "No game records yet."), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(records) { record ->
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
private fun GameRecordRow(title: String, detail: String, phase: String) {
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
            Text(phase, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(outcome.title, fontWeight = FontWeight.Black)
                        Text(outcome.summary)
                        Text(outcome.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Text(text("角色身份", "Roles"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) }
        items(review.cards) { card ->
            val role = card.hostRoleLabel(context, review.gameKind)
            GameRecordRow(
                title = "${review.cards.indexOf(card) + 1}号 ${card.name}",
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
