package com.codex.campboardgamehost

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
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
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CampBoardGameHostApp()
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    private fun enterImmersiveMode() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

private enum class Screen {
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

private enum class Role {
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

private data class PlayerCard(
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
)

private enum class ClocktowerTeam {
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
    Ravenkeeper,
}

private enum class ClocktowerDisplayKind {
    None,
    EitherOne,
    Number,
    YesNo,
    RoleReveal,
    Plain,
    Grimoire,
}

private enum class ClocktowerScript {
    TroubleBrewing,
    NoGreaterJoy,
}

private data class ClocktowerRole(
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
private const val ACTIVE_GAME_STATE_KEY = "active_game_state"
private const val ACTIVE_GAME_STATE_VERSION = 1
private const val MIN_PLAYERS = 3
private const val MIN_WEREWOLF_PLAYERS = 4
private const val MIN_CLOCKTOWER_PLAYERS = 5
private const val MAX_PLAYERS = 12

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
        .apply()
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
        .apply()
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

private fun JSONObject.optNullableString(key: String): String? {
    return if (has(key) && !isNull(key)) optString(key) else null
}

private fun JSONObject.optNullableInt(key: String): Int? {
    return if (has(key) && !isNull(key)) optInt(key) else null
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
    ClocktowerRole(ClocktowerTeam.Townsfolk, "市长", "Mayor", "若只剩三名玩家且无人被处决，好人获胜。", "If only three players live and no one is executed, good wins."),
    ClocktowerRole(ClocktowerTeam.Outsider, "管家", "Butler", "每天选择一名主人，白天只能在主人投票时投票。", "Each day, choose a master. You may only vote if your master votes."),
    ClocktowerRole(ClocktowerTeam.Outsider, "酒鬼", "Drunk", "你以为自己是镇民，但其实能力失效。", "You think you are a Townsfolk, but your ability is not working."),
    ClocktowerRole(ClocktowerTeam.Outsider, "隐士", "Recluse", "你可能被侦测为邪恶或恶魔，即使死亡后也是。", "You might register as evil or as a Demon, even if dead."),
    ClocktowerRole(ClocktowerTeam.Outsider, "圣徒", "Saint", "若你被处决，你的阵营失败。", "If you are executed, your team loses."),
    ClocktowerRole(ClocktowerTeam.Minion, "投毒者", "Poisoner", "每晚选择一名玩家，使其能力暂时失效。", "Each night, choose a player. Their ability temporarily stops working."),
    ClocktowerRole(ClocktowerTeam.Minion, "间谍", "Spy", "你可以查看说书人的魔典。", "You may look at the Storyteller grimoire."),
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

private fun clocktowerRolesForScript(script: ClocktowerScript): List<ClocktowerRole> = when (script) {
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
        else -> mapOf(ClocktowerTeam.Townsfolk to 7, ClocktowerTeam.Outsider to 2, ClocktowerTeam.Minion to 2, ClocktowerTeam.Demon to 1)
    }
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
    val context = remember(languageMode) { baseContext.localized(languageMode) }
    val language = context.resources.configuration.locales[0].language
    var screen by remember { mutableStateOf(Screen.Setup) }
    var currentGameKind by remember { mutableStateOf(GameKind.Undercover) }
    var savedGamePreview by remember(context) { mutableStateOf(baseContext.loadSavedGamePreview(context)) }
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
    var clocktowerButlerMaster by remember { mutableStateOf<String?>(null) }
    var clocktowerMonkProtectedTarget by remember { mutableStateOf<String?>(null) }
    var clocktowerVirginUsed by remember { mutableStateOf(false) }
    var clocktowerSlayerUsed by remember { mutableStateOf(false) }
    var clocktowerSlayerClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerArtistUsed by remember { mutableStateOf(false) }
    var clocktowerArtistClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var clocktowerArtistClaimantName by remember { mutableStateOf<String?>(null) }
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
    }

    fun resetClocktowerFlow() {
        resetClocktowerNightFlow()
        resetClocktowerDayFlow()
    }

    fun clearSavedGameState() {
        baseContext.clearActiveGameState()
        savedGamePreview = null
    }

    fun resetToSetup() {
        clearSavedGameState()
        screen = Screen.Setup
        cards.clear()
        records.clear()
        clocktowerEvents.clear()
        gameOutcome = null
        showResults = false
        lastWordsPromptNames = emptyList()
        pendingNightDeath = null
        seerCheckTarget = null
        witchSaveUsed = false
        witchPoisonUsed = false
        witchSavedTonight = false
        witchPoisonTarget = null
        hunterShotTarget = null
        selectedDayExile = null
        werewolfJudgeStepIndex = 0
        clocktowerPhase = ClocktowerPhase.FirstNight
        currentClocktowerScript = ClocktowerScript.TroubleBrewing
        clocktowerPendingNightDeath = null
        clocktowerSelectedExecution = null
        clocktowerPoisonTarget = null
        clocktowerFortuneTellerFirst = null
        clocktowerFortuneTellerSecond = null
        clocktowerChambermaidFirst = null
        clocktowerChambermaidSecond = null
        clocktowerRavenkeeperTarget = null
        clocktowerRedHerring = null
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerVirginUsed = false
        clocktowerSlayerUsed = false
        clocktowerSlayerClaimedNames = emptyList()
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerArtistClaimantName = null
        clocktowerLastExecutedName = null
        clocktowerPendingKlutzName = null
        clocktowerKlutzChoiceName = null
        clocktowerKlutzReturnToDawn = false
        resetClocktowerFlow()
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
        putNullableString("clocktowerButlerMaster", clocktowerButlerMaster)
        putNullableString("clocktowerMonkProtectedTarget", clocktowerMonkProtectedTarget)
        put("clocktowerVirginUsed", clocktowerVirginUsed)
        put("clocktowerSlayerUsed", clocktowerSlayerUsed)
        put("clocktowerSlayerClaimedNames", stringsToJsonArray(clocktowerSlayerClaimedNames))
        put("clocktowerArtistUsed", clocktowerArtistUsed)
        put("clocktowerArtistClaimedNames", stringsToJsonArray(clocktowerArtistClaimedNames))
        putNullableString("clocktowerArtistClaimantName", clocktowerArtistClaimantName)
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
        put("gameOutcome", gameOutcome?.toJson() ?: JSONObject.NULL)
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
            clocktowerButlerMaster = json.optNullableString("clocktowerButlerMaster")
            clocktowerMonkProtectedTarget = json.optNullableString("clocktowerMonkProtectedTarget")
            clocktowerVirginUsed = json.optBoolean("clocktowerVirginUsed", false)
            clocktowerSlayerUsed = json.optBoolean("clocktowerSlayerUsed", false)
            clocktowerSlayerClaimedNames = json.optJSONArray("clocktowerSlayerClaimedNames")?.toStringList().orEmpty()
            clocktowerArtistUsed = json.optBoolean("clocktowerArtistUsed", false)
            clocktowerArtistClaimedNames = json.optJSONArray("clocktowerArtistClaimedNames")?.toStringList().orEmpty()
            clocktowerArtistClaimantName = json.optNullableString("clocktowerArtistClaimantName")
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

    fun addTemporaryPlayer() {
        var nextNumber = playerNames.size + 1
        var nextName = context.playerName(nextNumber)
        while (nextName in playerNames) {
            nextNumber += 1
            nextName = context.playerName(nextNumber)
        }
        addCurrentPlayer(nextName)
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
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerVirginUsed = false
        clocktowerSlayerUsed = false
        clocktowerSlayerClaimedNames = emptyList()
        clocktowerArtistUsed = false
        clocktowerArtistClaimedNames = emptyList()
        clocktowerArtistClaimantName = null
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

    fun promoteScarletWomanIfNeeded(): String? {
        val alivePlayers = cards.filter { it.eliminatedRound == null }
        if (alivePlayers.size < 5) return null
        val scarletWoman = alivePlayers.firstOrNull { it.clocktowerRole?.enName == "Scarlet Woman" } ?: return null
        val imp = completeTroubleBrewingRoles.first { it.enName == "Imp" }
        setClocktowerActualRole(scarletWoman.name, imp)
        records.add(EliminationRecord(round, scarletWoman.name, context.getString(R.string.clocktower_record_scarlet_woman_promoted)))
        addClocktowerEvent(
            ClocktowerEventType.RoleChange,
            context.getString(R.string.clocktower_event_role_changed),
            context.getString(R.string.clocktower_event_scarlet_woman_became_demon_format, playerSeatLabel(cards, scarletWoman.name, language)),
            listOf(scarletWoman.name),
        )
        return scarletWoman.name
    }

    fun promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen: Boolean): String? {
        promoteScarletWomanIfNeeded()?.let { return it }
        if (!impDeathWasSelfChosen) return null
        val imp = completeTroubleBrewingRoles.first { it.enName == "Imp" }
        val minion = cards
            .filter { it.eliminatedRound == null }
            .firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion }
            ?: return null
        setClocktowerActualRole(minion.name, imp)
        records.add(EliminationRecord(round, minion.name, context.getString(R.string.clocktower_record_imp_passed)))
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
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.displayCutout),
                ) {
                when (screen) {
                    Screen.Setup -> SetupScreen(
                    playerCount = playerCount,
                    savedGamePreview = savedGamePreview,
                    commonPlayers = commonPlayers,
                    playerNames = playerNames,
                    onAddCurrentPlayer = ::addCurrentPlayer,
                    onAddTemporaryPlayer = ::addTemporaryPlayer,
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
                        selectedScript = selectedClocktowerScript ?: defaultClocktowerScriptFor(playerCount),
                        onScriptChange = { selectedClocktowerScript = it },
                        onBack = { screen = Screen.Setup },
                        onStart = ::startClocktowerGame,
                    )

                    Screen.Settings -> SettingsScreen(
                        languageMode = languageMode,
                        commonPlayers = commonPlayers,
                        newCommonPlayerName = newCommonPlayerName,
                        onLanguageModeChange = { nextMode ->
                            languageMode = nextMode
                            baseContext.saveLanguageMode(nextMode)
                        },
                        onNewCommonPlayerNameChange = { newCommonPlayerName = it },
                        onAddCommonPlayer = ::addCommonPlayer,
                        onRemoveCommonPlayer = ::removeCommonPlayer,
                        onBack = { screen = Screen.Setup },
                    )

                    Screen.PassPhone -> PassPhoneScreen(
                    playerName = cards[currentDealIndex].name,
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
                        onNewGame = { resetToSetup() },
                    )

                    Screen.ClocktowerJudge -> ClocktowerJudgeScreen(
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
                        butlerMaster = clocktowerButlerMaster,
                        monkProtectedTarget = clocktowerMonkProtectedTarget,
                        virginUsed = clocktowerVirginUsed,
                        slayerUsed = clocktowerSlayerUsed,
                        slayerClaimedNames = clocktowerSlayerClaimedNames,
                        artistUsed = clocktowerArtistUsed,
                        artistClaimedNames = clocktowerArtistClaimedNames,
                        artistClaimantName = clocktowerArtistClaimantName,
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
                            }
                            if (nextPhase == ClocktowerPhase.Day) {
                                resetClocktowerDayFlow()
                            }
                        },
                        onSelectNightDeath = { clocktowerPendingNightDeath = it },
                        onSelectExecution = { clocktowerSelectedExecution = it },
                        onSelectPoisonTarget = { clocktowerPoisonTarget = it },
                        onSelectFortuneTellerFirst = { clocktowerFortuneTellerFirst = it },
                        onSelectFortuneTellerSecond = { clocktowerFortuneTellerSecond = it },
                        onSelectChambermaidFirst = { clocktowerChambermaidFirst = it },
                        onSelectChambermaidSecond = { clocktowerChambermaidSecond = it },
                        onSelectRavenkeeperTarget = { clocktowerRavenkeeperTarget = it },
                        onSelectRedHerring = { clocktowerRedHerring = it },
                        onSelectButlerMaster = { clocktowerButlerMaster = it },
                        onSelectMonkProtectedTarget = { clocktowerMonkProtectedTarget = it },
                        onSelectKlutzChoice = { clocktowerKlutzChoiceName = it },
                        onConfirmKlutzChoice = {
                            val choice = clocktowerKlutzChoiceName
                            if (choice != null) {
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    context.getString(R.string.clocktower_event_klutz_choice),
                                    "${playerSeatLabel(cards, clocktowerPendingKlutzName)} → ${playerSeatLabel(cards, choice)}",
                                    listOfNotNull(clocktowerPendingKlutzName, choice),
                                )
                                val chosenCard = cards.firstOrNull { it.name == choice }
                                if (chosenCard != null && isClocktowerEvil(chosenCard)) {
                                    gameOutcome = GameOutcome(
                                        title = context.getString(R.string.outcome_clocktower_evil_title),
                                        summary = context.getString(R.string.clocktower_outcome_klutz_summary),
                                        reason = context.getString(R.string.clocktower_outcome_klutz_reason_format, playerSeatLabel(cards, clocktowerPendingKlutzName), playerSeatLabel(cards, choice)),
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
                                        clocktowerPhase = ClocktowerPhase.Night
                                    }
                                    resetClocktowerDayFlow()
                                    resetClocktowerNightFlow()
                                }
                            }
                        },
                        onSelectArtistClaimant = { clocktowerArtistClaimantName = it },
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
                                records.add(EliminationRecord(round, claimantName, context.getString(R.string.clocktower_event_artist_question_recorded)))
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    context.getString(R.string.clocktower_event_artist_question),
                                    playerSeatLabel(cards, claimantName),
                                    listOf(claimantName),
                                )
                                clocktowerArtistClaimantName = null
                                clocktowerDayModeState.value = ClocktowerDayMode.Overview
                            }
                        },
                        onSlayerShot = { claimantName, targetName ->
                            if (claimantName !in clocktowerSlayerClaimedNames) {
                                clocktowerSlayerClaimedNames = clocktowerSlayerClaimedNames + claimantName
                            }
                            val claimantCard = cards.firstOrNull { it.name == claimantName }
                            val targetIndex = cards.indexOfFirst { it.name == targetName }
                            val targetCard = cards.getOrNull(targetIndex)
                            val isRealSlayer = claimantCard?.clocktowerRole?.enName == "Slayer"
                            val canUseSlayerAbility = isRealSlayer && !clocktowerSlayerUsed
                            var shotOutcome: GameOutcome? = null
                            if (canUseSlayerAbility) {
                                clocktowerSlayerUsed = true
                            }
                            if (canUseSlayerAbility && targetIndex >= 0 && targetCard != null && targetCard.eliminatedRound == null && targetCard.clocktowerTeam == ClocktowerTeam.Demon) {
                                cards[targetIndex] = targetCard.copy(eliminatedRound = round)
                                records.add(
                                    EliminationRecord(
                                        round,
                                        targetName,
                                        context.getString(R.string.clocktower_record_slayer_hit, playerSeatLabel(cards, claimantName)),
                                    ),
                                )
                                val promotedName = promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
                                shotOutcome = if (promotedName == null) {
                                    evaluateGameOutcome(context, cards, currentGameKind)
                                } else {
                                    null
                                }
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    context.getString(R.string.clocktower_event_slayer_hit),
                                    context.getString(R.string.clocktower_event_slayer_hit_detail_format, playerSeatLabel(cards, claimantName), playerSeatLabel(cards, targetName)),
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
                                    context.getString(R.string.clocktower_event_slayer_claim),
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
                                        context.getString(R.string.clocktower_event_virgin_execution),
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
                                    context.getString(R.string.clocktower_event_virgin_spent),
                                    context.getString(R.string.clocktower_event_virgin_spent_detail_format, playerSeatLabel(cards, nomineeName)),
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
                                        context.getString(R.string.clocktower_event_execution),
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
                                    context.getString(R.string.clocktower_event_no_execution),
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
                                clocktowerPhase = ClocktowerPhase.Night
                                resetClocktowerDayFlow()
                                resetClocktowerNightFlow()
                            }
                            clocktowerSelectedExecution = null
                        },
                        onConfirmNight = {
                            val demonPoisonedTonight = clocktowerPoisonTarget?.let { name ->
                                cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
                            } == true
                            var nightKlutzName: String? = null
                            val deathName = clocktowerPendingNightDeath.takeUnless { demonPoisonedTonight }
                            if (demonPoisonedTonight) {
                                addClocktowerEvent(
                                    ClocktowerEventType.RoleAction,
                                    context.getString(R.string.clocktower_event_demon_poisoned),
                                    context.getString(R.string.clocktower_event_demon_poisoned_detail),
                                    listOfNotNull(clocktowerPoisonTarget, clocktowerPendingNightDeath),
                                )
                                clocktowerPendingNightDeath = null
                            }
                            if (deathName != null) {
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
                                            context.getString(R.string.clocktower_event_death_prevented),
                                            "${playerSeatLabel(cards, deathName)} · $note",
                                            listOf(deathName),
                                        )
                                        clocktowerPendingNightDeath = null
                                    } else {
                                        val impSelfChosen = nightDeathCard.clocktowerTeam == ClocktowerTeam.Demon
                                        cards[index] = nightDeathCard.copy(eliminatedRound = round)
                                        records.add(EliminationRecord(round, deathName, context.getString(R.string.clocktower_record_night_death)))
                                        addClocktowerEvent(
                                            ClocktowerEventType.Death,
                                            context.getString(R.string.clocktower_event_night_death),
                                            playerSeatLabel(cards, deathName),
                                            listOf(deathName),
                                        )
                                        if (impSelfChosen) {
                                            promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = true)
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
                                }
                            } else if (!demonPoisonedTonight && clocktowerPhase != ClocktowerPhase.FirstNight) {
                                addClocktowerEvent(
                                    ClocktowerEventType.Death,
                                    context.getString(R.string.clocktower_event_no_night_death),
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
                            } else if (nightKlutzName == null) {
                                clocktowerPhase = ClocktowerPhase.Dawn
                                resetClocktowerNightFlow()
                            }
                            clocktowerPoisonTarget = null
                            clocktowerFortuneTellerFirst = null
                            clocktowerFortuneTellerSecond = null
                            clocktowerChambermaidFirst = null
                            clocktowerChambermaidSecond = null
                            clocktowerRavenkeeperTarget = null
                            clocktowerMonkProtectedTarget = null
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
                        onNewGame = { resetToSetup() },
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
                    onNewGame = { resetToSetup() },
                )
                }

                if (showResults) {
                    ResultsDialog(
                        gameKind = currentGameKind,
                        cards = cards,
                        outcome = gameOutcome,
                        onDismiss = { showResults = false },
                        onNewGame = { resetToSetup() },
                    )
                }
                } // Box (displayCutout inset)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupScreen(
    playerCount: Int,
    savedGamePreview: SavedGamePreview?,
    commonPlayers: List<String>,
    playerNames: List<String>,
    onAddCurrentPlayer: (String) -> Unit,
    onAddTemporaryPlayer: () -> Unit,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.undercover_subtitle), color = Color(0xFF5C6A63))
                }
                TextButton(onClick = onOpenSettings) {
                    Text(stringResource(R.string.settings))
                }
            }
        }

        savedGamePreview?.let { preview ->
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(preview.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(preview.subtitle, color = Color(0xFF5C6A63))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onResumeSavedGame,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.continue_saved_game))
                            }
                            OutlinedButton(
                                onClick = onDiscardSavedGame,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.discard_saved_game))
                            }
                        }
                    }
                }
            }
        }

        item {
            RoundTableSetupEditor(
                seatedPlayers = playerNames,
                commonPlayers = commonPlayers,
                canAddPlayer = playerCount < MAX_PLAYERS,
                onAddCurrentPlayer = onAddCurrentPlayer,
                onAddTemporaryPlayer = onAddTemporaryPlayer,
                onRemoveCurrentPlayer = onRemoveCurrentPlayer,
                onMoveCurrentPlayerTo = onMoveCurrentPlayerTo,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.choose_game), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = onOpenUndercoverSettings,
                    enabled = canStartUndercover,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
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
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        if (canStartWerewolf) {
                            stringResource(R.string.game_werewolf)
                        } else {
                            stringResource(R.string.need_werewolf_min_players, MIN_WEREWOLF_PLAYERS)
                        }
                    )
                }
                OutlinedButton(
                    onClick = onOpenClocktowerSettings,
                    enabled = canStartClocktower,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        if (canStartClocktower) {
                            stringResource(R.string.game_clocktower)
                        } else {
                            stringResource(R.string.need_clocktower_min_players, MIN_CLOCKTOWER_PLAYERS)
                        }
                    )
                }
            }
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
    onRemoveCurrentPlayer: (Int) -> Unit,
    onMoveCurrentPlayerTo: (Int, Int) -> Unit,
) {
    var dragState by remember { mutableStateOf<PlayerDragState?>(null) }
    var hoverInsertIndex by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(stringResource(R.string.current_players_section), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.current_players_count_format, seatedPlayers.size, MAX_PLAYERS), color = Color(0xFF6F7B74))
            }
            OutlinedButton(
                onClick = onAddTemporaryPlayer,
                enabled = canAddPlayer,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.add_temporary_player))
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
            val avatarSizeDp = if (useRectangularTable) 52.dp else 64.dp
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
                        color = Color(0xFFE6D8BD),
                        topLeft = Offset(tableLeft, tableTop),
                        size = Size(tableWidth, tableHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    )
                    drawRoundRect(
                        color = Color(0xFF2F5D50),
                        topLeft = Offset(tableLeft, tableTop),
                        size = Size(tableWidth, tableHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = 5.dp.toPx()),
                    )
                    drawRoundRect(
                        color = Color(0x332F5D50),
                        topLeft = Offset(tableLeft, tableTop),
                        size = Size(tableWidth, tableHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                } else {
                    drawCircle(
                        color = Color(0xFFE6D8BD),
                        radius = tableRadius,
                        center = center,
                    )
                    drawCircle(
                        color = Color(0xFF2F5D50),
                        radius = tableRadius,
                        center = center,
                        style = Stroke(width = 5.dp.toPx()),
                    )
                    drawCircle(
                        color = Color(0x332F5D50),
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
                        color = Color(0xFF5C6A63),
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
                        .background(Color(0x332F5D50), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = Color(0xFF2F5D50), fontWeight = FontWeight.Bold, fontSize = 24.sp)
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
        Text(stringResource(R.string.bench_area_hint), color = Color(0xFF6F7B74))
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
            BenchPlayerChip(
                name = stringResource(R.string.add_temporary_player),
                enabled = canAddPlayer,
                label = stringResource(R.string.add_temporary_player),
                onClick = onAddTemporaryPlayer,
            )
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
                .background(Color(0xFF2F5D50), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge, color = Color.White, fontWeight = FontWeight.Black)
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
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    languageMode: LanguageMode,
    commonPlayers: List<String>,
    newCommonPlayerName: String,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onNewCommonPlayerNameChange: (String) -> Unit,
    onAddCommonPlayer: () -> Unit,
    onRemoveCommonPlayer: (String) -> Unit,
    onBack: () -> Unit,
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

@Composable
private fun ClocktowerSettingsScreen(
    playerCount: Int,
    selectedScript: ClocktowerScript,
    onScriptChange: (ClocktowerScript) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    val distribution = clocktowerDistribution(playerCount)
    val showScriptChoice = playerCount in 5..6
    val effectiveScript = if (showScriptChoice) selectedScript else ClocktowerScript.TroubleBrewing
    val canStart = playerCount >= MIN_CLOCKTOWER_PLAYERS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GameSettingsHeader(
                title = stringResource(R.string.game_clocktower),
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
                    Text(stringResource(R.string.clocktower_script), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (showScriptChoice) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClocktowerScript.entries.forEach { script ->
                                if (script == effectiveScript) {
                                    Button(
                                        onClick = { onScriptChange(script) },
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(script.nameFor(language))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onScriptChange(script) },
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(script.nameFor(language))
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "7 人及以上使用 ${ClocktowerScript.TroubleBrewing.nameFor(language)}。",
                            color = Color(0xFF6F7B74),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (effectiveScript == ClocktowerScript.NoGreaterJoy) {
                        Text(
                            "5–6 人默认使用 No Greater Joy；可切换回暗流涌动。",
                            color = Color(0xFF6F7B74),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    HorizontalDivider()
                    ClocktowerTeam.entries.forEach { team ->
                        val count = distribution[team] ?: 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(team.label(LocalContext.current), fontWeight = FontWeight.SemiBold)
                            Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text(
                    when {
                        playerCount < MIN_CLOCKTOWER_PLAYERS -> stringResource(R.string.need_clocktower_min_players, MIN_CLOCKTOWER_PLAYERS)
                        else -> stringResource(R.string.start_dealing)
                    }
                )
            }
        }
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
    current: Int,
    total: Int,
    onReveal: () -> Unit,
) {
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
    onNewGame: () -> Unit,
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
                TextButton(onClick = onNewGame) {
                    Text(stringResource(R.string.new_game))
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
            HostInstructionBlock(
                label = stringResource(R.string.host_script_label),
                text = script,
                backgroundColor = Color(0xFFFFF4DC),
                textColor = Color(0xFF1F2925),
            )
            HostInstructionBlock(
                label = stringResource(R.string.host_action_label),
                text = action,
                backgroundColor = Color(0xFFEAF2EA),
                textColor = Color(0xFF2F5D50),
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
            Text(it, color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
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
    val language = LocalContext.current.resources.configuration.locales[0].language
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { card ->
            val selected = selectedName == card.name
            if (selected) {
                Button(onClick = { onSelect(card.name) }, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
                    Text(card.seatLabel(allCards, language))
                }
            } else {
                OutlinedButton(onClick = { onSelect(card.name) }, enabled = enabled, shape = RoundedCornerShape(8.dp)) {
                    Text(card.seatLabel(allCards, language))
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
                ButtonDefaults.buttonColors(containerColor = Color(0xFFD94F38), contentColor = Color.White)
            } else {
                ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2925))
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

private fun PlayerCard.seatLabel(cards: List<PlayerCard>, language: String = "zh"): String {
    val seat = cards.indexOfFirst { it.name == name } + 1
    return if (language == "en") "$seat — $name" else "${seat}号 $name"
}

private fun playerSeatLabel(cards: List<PlayerCard>, playerName: String?, language: String = "zh"): String =
    cards.firstOrNull { it.name == playerName }?.seatLabel(cards, language) ?: playerName.orEmpty()

private fun isClocktowerEvil(card: PlayerCard): Boolean =
    card.clocktowerTeam == ClocktowerTeam.Minion || card.clocktowerTeam == ClocktowerTeam.Demon

private fun clocktowerRedHerringCandidates(cards: List<PlayerCard>, aliveCards: List<PlayerCard>): List<PlayerCard> {
    val fortuneTellerName = actualClocktowerRoleCards(cards, "Fortune Teller").firstOrNull()?.name
    return aliveCards.filter { card ->
        card.clocktowerTeam != ClocktowerTeam.Minion &&
            card.clocktowerTeam != ClocktowerTeam.Demon &&
            card.name != fortuneTellerName
    }
}

private fun actualClocktowerRoleCards(cards: List<PlayerCard>, enName: String): List<PlayerCard> =
    cards.filter { it.clocktowerRole?.enName == enName }

private fun chefEvilPairs(cards: List<PlayerCard>): Int {
    if (cards.size < 2) return 0
    return cards.indices.count { index ->
        val next = cards[(index + 1) % cards.size]
        isClocktowerEvil(cards[index]) && isClocktowerEvil(next)
    }
}

private fun livingNeighbors(cards: List<PlayerCard>, playerName: String): List<PlayerCard> {
    val aliveCards = cards.filter { it.eliminatedRound == null }
    if (aliveCards.size <= 1) return emptyList()
    val index = aliveCards.indexOfFirst { it.name == playerName }
    if (index < 0) return emptyList()
    val left = aliveCards[(index - 1 + aliveCards.size) % aliveCards.size]
    val right = aliveCards[(index + 1) % aliveCards.size]
    return listOf(left, right).distinctBy { it.name }
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
)

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
    val wakeText: String? = null,
    val roleEnName: String? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClocktowerJudgeScreen(
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
    butlerMaster: String?,
    monkProtectedTarget: String?,
    virginUsed: Boolean,
    slayerUsed: Boolean,
    slayerClaimedNames: List<String>,
    artistUsed: Boolean,
    artistClaimedNames: List<String>,
    artistClaimantName: String?,
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
    onSelectButlerMaster: (String?) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onSelectKlutzChoice: (String?) -> Unit,
    onConfirmKlutzChoice: () -> Unit,
    onSelectArtistClaimant: (String?) -> Unit,
    onConfirmArtistQuestion: () -> Unit,
    onSlayerShot: (String, String) -> Unit,
    onVirginNomination: (String, String, Boolean) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: () -> Unit,
    onConfirmNight: () -> Unit,
    onShowResults: () -> Unit,
    onNewGame: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val aliveCards = remember(cards) { cards.filter { it.eliminatedRound == null } }
    val firstNightWasherwoman = remember(cards) { actualClocktowerRoleCards(cards, "Washerwoman").firstOrNull() }
    val firstNightLibrarian = remember(cards) { actualClocktowerRoleCards(cards, "Librarian").firstOrNull() }
    val firstNightInvestigator = remember(cards) { actualClocktowerRoleCards(cards, "Investigator").firstOrNull() }
    val chefPlayer = remember(cards) { actualClocktowerRoleCards(cards, "Chef").firstOrNull() }
    val empathPlayers = remember(cards) { actualClocktowerRoleCards(cards, "Empath").filter { it.eliminatedRound == null } }
    val fortuneTellerPlayers = remember(cards) { actualClocktowerRoleCards(cards, "Fortune Teller").filter { it.eliminatedRound == null } }
    val poisonerPlayers = remember(cards) { actualClocktowerRoleCards(cards, "Poisoner").filter { it.eliminatedRound == null } }
    val butlerPlayers = remember(cards) { actualClocktowerRoleCards(cards, "Butler").filter { it.eliminatedRound == null } }
    val demonPoisonedTonight = poisonTarget?.let { name ->
        cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
    } == true
    val ravenkeeperNightDeath = pendingNightDeath
        ?.takeUnless { demonPoisonedTonight }
        ?.let { name -> cards.firstOrNull { it.name == name && it.clocktowerRole?.enName == "Ravenkeeper" } }
    val ravenkeeperTrigger = ravenkeeperNightDeath?.takeUnless { monkProtectedTarget == it.name }

    val fortuneTellerResult = if (fortuneTellerFirst != null && fortuneTellerSecond != null) {
        val targets = setOf(fortuneTellerFirst, fortuneTellerSecond)
        val matched = aliveCards.any { it.name in targets && (it.clocktowerTeam == ClocktowerTeam.Demon || it.name == redHerring) }
        if (matched) stringResource(R.string.clocktower_yes) else stringResource(R.string.clocktower_no)
    } else {
        null
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
        setOf("Clockmaker", "Investigator", "Empath", "Chambermaid")
    } else {
        buildSet {
            add("Chambermaid")
            add("Empath")
            add("Poisoner")
            add("Fortune Teller")
            add("Butler")
            add("Monk")
            add("Imp")
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
        if (!step.isRealAction || step.action == ClocktowerNightAction.None) return
        val names = when (step.action) {
            ClocktowerNightAction.RedHerring -> listOfNotNull(redHerring)
            ClocktowerNightAction.Poison -> listOfNotNull(step.actor?.name, poisonTarget)
            ClocktowerNightAction.ButlerMaster -> listOfNotNull(step.actor?.name, butlerMaster)
            ClocktowerNightAction.MonkProtect -> listOfNotNull(step.actor?.name, monkProtectedTarget)
            ClocktowerNightAction.FortuneTeller -> listOfNotNull(step.actor?.name, fortuneTellerFirst, fortuneTellerSecond)
            ClocktowerNightAction.Chambermaid -> listOfNotNull(step.actor?.name, chambermaidFirst, chambermaidSecond)
            ClocktowerNightAction.DemonKill -> listOfNotNull(step.actor?.name, pendingNightDeath)
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
            ClocktowerNightAction.Ravenkeeper -> playerSeatLabel(cards, ravenkeeperTarget)
            ClocktowerNightAction.None -> step.tellPlayer ?: step.storytellerAction
        }
        onRecordEvent(ClocktowerEventType.RoleAction, step.title, detail, names)
    }
    val phaseTitle = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_phase_first_night)
        ClocktowerPhase.Dawn -> stringResource(R.string.clocktower_phase_dawn)
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_phase_day, round)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_phase_night, round)
    }
    val phaseProgress = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_progress_first_night)
        ClocktowerPhase.Dawn -> stringResource(R.string.clocktower_progress_dawn)
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_progress_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_progress_night)
    }
    val phaseScript = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_script_first_night)
        ClocktowerPhase.Dawn -> stringResource(R.string.clocktower_script_dawn)
        ClocktowerPhase.Day -> stringResource(R.string.clocktower_script_day)
        ClocktowerPhase.Night -> stringResource(R.string.clocktower_script_night)
    }
    val phaseAction = when (phase) {
        ClocktowerPhase.FirstNight -> stringResource(R.string.clocktower_action_first_night)
        ClocktowerPhase.Dawn -> stringResource(R.string.clocktower_action_dawn)
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
    val executionThreshold = (aliveCards.size + 1) / 2
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
    fun pickTwoPlayers(pool: List<PlayerCard>, key: String): Pair<PlayerCard, PlayerCard>? {
        if (pool.size < 2) return null
        val sorted = pool.sortedBy { cards.indexOf(it).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
        val start = stableIndex(key, sorted.size)
        return sorted[start] to sorted[(start + 1) % sorted.size]
    }
    fun displayOption(label: String, kind: ClocktowerDisplayKind, title: String, primary: String?, secondary: String? = null, footer: String? = null) =
        ClocktowerDisplayOption(label, kind, title, primary, secondary, footer)
    fun fakeEitherOneOptions(enName: String, team: ClocktowerTeam, title: String, emptyText: String, actor: PlayerCard, trueTarget: PlayerCard?): List<ClocktowerDisplayOption> {
        val rolePool = clocktowerRolesForScript(script).filter { it.team == team }
        val roleA = rolePool.getOrNull(stableIndex("$enName-role-a", rolePool.size))
        val roleB = rolePool.getOrNull(stableIndex("$enName-role-b", rolePool.size))
        val basePool = cards.filter { it.name != actor.name && it.name != trueTarget?.name }
        val fallbackPool = cards.filter { it.name != actor.name }
        val pool = if (basePool.size >= 2) basePool else fallbackPool
        val pairA = pickTwoPlayers(pool, "$enName-fake-a-${actor.name}")
        val pairB = pickTwoPlayers(pool.reversed(), "$enName-fake-b-${actor.name}")
        return listOf(
            displayOption("展示选项 A", ClocktowerDisplayKind.EitherOne, title, roleA?.nameFor(language) ?: emptyText, seatNumbersText(pairA), if (pairA == null) "" else "在下面两位玩家之中"),
            displayOption("展示选项 B", ClocktowerDisplayKind.EitherOne, title, roleB?.nameFor(language) ?: emptyText, seatNumbersText(pairB), if (pairB == null) "" else "在下面两位玩家之中"),
        )
    }
    fun fakeNumberOptions(title: String, trueValue: Int, maxValue: Int, footer: String): List<ClocktowerDisplayOption> {
        val values = (0..maxOf(0, maxValue)).filter { it != trueValue }
        val fallback = values.ifEmpty { listOf(trueValue) }
        return listOf(
            displayOption("展示选项 A", ClocktowerDisplayKind.Number, title, fallback.first().toString(), footer = footer),
            displayOption("展示选项 B", ClocktowerDisplayKind.Number, title, fallback.last().toString(), footer = footer),
        )
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
    ): ClocktowerNightStepUi {
        val actor = roleActor(enName)
        val actorIsDrunkShownRole = actor?.clocktowerRole?.enName == "Drunk" && actor.clocktowerShownRole?.enName == enName
        val hostUnreliableNote = if (actorIsDrunkShownRole) {
            context.getString(R.string.clocktower_host_unreliable_drunk_format, roleName)
        } else if (actorIsPoisoned(actor)) {
            context.getString(R.string.clocktower_host_unreliable_poisoned)
        } else {
            null
        }
        val unreliableOptions = if (actor != null && actorIsUnreliable(enName, actor)) displayOptions(actor) else emptyList()
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
            displayOptions = unreliableOptions,
            roleEnName = enName,
        )
    }

    val washerwomanActor = roleActor("Washerwoman")
    val washerwomanTarget = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Townsfolk && it.clocktowerRole?.enName != "Washerwoman" }
    val washerwomanPair = washerwomanTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(washerwomanActor?.name)) }
    val washerwomanOrderedPair = orderedPair(washerwomanPair?.first, washerwomanPair?.second, "Washerwoman-${washerwomanPair?.first?.name}-${washerwomanPair?.second?.name}")
    val librarianActor = roleActor("Librarian")
    val librarianTarget = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Outsider }
    val librarianPair = librarianTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(librarianActor?.name)) }
    val librarianOrderedPair = orderedPair(librarianPair?.first, librarianPair?.second, "Librarian-${librarianPair?.first?.name}-${librarianPair?.second?.name}")
    val investigatorActor = roleActor("Investigator")
    val investigatorTarget = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion }
    val investigatorPair = investigatorTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(investigatorActor?.name)) }
    val investigatorOrderedPair = orderedPair(investigatorPair?.first, investigatorPair?.second, "Investigator-${investigatorPair?.first?.name}-${investigatorPair?.second?.name}")
    val clockmakerValue = clockmakerNumber()
    val clockmakerNumber = clockmakerValue.toString()
    val empathActor = roleActor("Empath")
    val empathNeighbors = empathActor?.let { livingNeighbors(cards, it.name) }.orEmpty()
    val chefValue = chefEvilPairs(cards)
    val chefNumber = chefValue.toString()
    val empathValue = empathNeighbors.count(::isClocktowerEvil)
    val empathNumber = empathValue.toString()
    val demonCard = cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Demon }
    val sageNightDeath = pendingNightDeath
        ?.takeUnless { demonPoisonedTonight }
        ?.let { name -> cards.firstOrNull { it.name == name && it.clocktowerRole?.enName == "Sage" } }
        ?.takeUnless { monkProtectedTarget == it.name }
    val sagePair = demonCard?.let { storytellerPairHint(it, cards) }
    val minionCards = cards.filter { it.clocktowerTeam == ClocktowerTeam.Minion }
    fun seatNumberText(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    fun twoSeatNumbers(first: PlayerCard?, second: PlayerCard?): String? =
        if (first != null && second != null) "${seatNumberText(first)}   ${seatNumberText(second)}" else null
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
    val demonBluffs = completeTroubleBrewingRoles
        .filter { role -> role.team == ClocktowerTeam.Townsfolk && cards.none { it.clocktowerRole?.enName == role.enName } }
        .take(3)
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
                displayKind = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo && minionInfoText != null) ClocktowerDisplayKind.Plain else ClocktowerDisplayKind.None,
                displayTitle = stringResource(R.string.clocktower_first_night_minion_title),
                displayPrimary = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) minionInfoText else null,
                displayFooter = if (minionCards.isNotEmpty() && shouldGiveFirstNightEvilInfo) stringResource(R.string.clocktower_first_night_minion_explain) else null,
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
                displayKind = if (demonCard != null && shouldGiveFirstNightEvilInfo) ClocktowerDisplayKind.Plain else ClocktowerDisplayKind.None,
                displayTitle = stringResource(R.string.clocktower_first_night_demon_title),
                displayPrimary = if (demonCard != null && shouldGiveFirstNightEvilInfo) demonInfoText else null,
                displayFooter = if (demonCard != null && shouldGiveFirstNightEvilInfo) stringResource(R.string.clocktower_first_night_demon_explain) else null,
            ),
            ClocktowerNightStepUi(
                title = "占卜师红鲱鱼",
                actor = null,
                isRealAction = actualClocktowerRoleCards(cards, "Fortune Teller").isNotEmpty(),
                reason = if (actualClocktowerRoleCards(cards, "Fortune Teller").isEmpty()) "本局没有占卜师，此步骤只用于首夜配置。" else "",
                storytellerAction = "不要公开说明这个选择。请选择一名好人玩家作为红鲱鱼，不要选择占卜师本人。",
                tellPlayer = redHerring?.let { "已选择：${playerSeatLabel(cards, it)}" },
                explanation = "红鲱鱼是占卜师规则的一部分。占卜师查到恶魔或红鲱鱼时，都会得到“是”。",
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
                displayOptions = { fakeNumberOptions("钟表匠信息", clockmakerValue, cards.size / 2, "恶魔到最近爪牙的距离") },
            ),
            infoStep(
                roleName = "洗衣妇",
                enName = "Washerwoman",
                tellPlayer = washerwomanTarget?.let { "${it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${washerwomanOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${washerwomanOrderedPair?.second?.seatLabel(cards).orEmpty()}" },
                explanation = "洗衣妇会得知某个镇民在两名玩家之一中。",
                displayPrimary = washerwomanTarget?.clocktowerRole?.nameFor(language),
                displaySecondary = seatNumbersText(washerwomanOrderedPair),
                displayFooter = "在下面两位玩家之中",
                hostInstruction = "轻拍洗衣妇，示意睁眼。点击“全屏展示给玩家”，只给她看；看完后收回手机，示意闭眼。",
                displayOptions = { actor -> fakeEitherOneOptions("Washerwoman", ClocktowerTeam.Townsfolk, "洗衣妇信息", "没有镇民", actor, washerwomanTarget) },
            ),
            infoStep(
                roleName = "图书管理员",
                enName = "Librarian",
                tellPlayer = librarianTarget?.let { "${it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${librarianOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${librarianOrderedPair?.second?.seatLabel(cards).orEmpty()}" } ?: "本局没有外来者。",
                explanation = "图书管理员会得知某个外来者在两名玩家之一中，或得知没有外来者。",
                displayPrimary = librarianTarget?.clocktowerRole?.nameFor(language) ?: "没有外来者",
                displaySecondary = seatNumbersText(librarianOrderedPair),
                displayFooter = if (librarianTarget == null) "" else "在下面两位玩家之中",
                hostInstruction = "轻拍图书管理员，示意睁眼。把结果只给他看；如果显示“没有外来者”，也只告诉他本人。",
                displayOptions = { actor -> fakeEitherOneOptions("Librarian", ClocktowerTeam.Outsider, "图书管理员信息", "没有外来者", actor, librarianTarget) },
            ),
            infoStep(
                roleName = "调查员",
                enName = "Investigator",
                tellPlayer = investigatorTarget?.let { "${it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${investigatorOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${investigatorOrderedPair?.second?.seatLabel(cards).orEmpty()}" } ?: "本局没有爪牙。",
                explanation = "调查员会得知某个爪牙在两名玩家之一中，或得知没有爪牙。",
                displayPrimary = investigatorTarget?.clocktowerRole?.nameFor(language) ?: "没有爪牙",
                displaySecondary = seatNumbersText(investigatorOrderedPair),
                displayFooter = if (investigatorTarget == null) "" else "在下面两位玩家之中",
                hostInstruction = "轻拍调查员，示意睁眼。把结果只给他看；不要让其他玩家看到被点名的两人。",
                displayOptions = { actor -> fakeEitherOneOptions("Investigator", ClocktowerTeam.Minion, "调查员信息", "没有爪牙", actor, investigatorTarget) },
            ),
            infoStep(
                roleName = "厨师",
                enName = "Chef",
                tellPlayer = chefNumber,
                explanation = "这个数字表示有几对邪恶玩家相邻而坐。",
                hostInstruction = "轻拍厨师，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。",
                displayOptions = { fakeNumberOptions("厨师信息", chefValue, cards.size, "邪恶玩家相邻对数") },
            ),
            infoStep(
                roleName = "共情者",
                enName = "Empath",
                tellPlayer = empathNumber,
                explanation = "这个数字表示共情者两个存活邻居中有几个邪恶玩家。",
                hostInstruction = "轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。",
                displayOptions = { fakeNumberOptions("共情者信息", empathValue, 2, "邪恶存活邻居数量") },
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
            ),
            infoStep(
                roleName = "占卜师",
                enName = "Fortune Teller",
                tellPlayer = fortuneTellerResult,
                explanation = "让占卜师选择两名玩家。如果其中有恶魔或红鲱鱼，告诉他“是”。",
                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = "查询这两名玩家",
                hostInstruction = "轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后只告诉他“是”或“否”。",
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
                tellPlayer = cards.joinToString("\n") { "${it.seatLabel(cards)}：${it.hostRoleLabel(context, GameKind.Clocktower)}" },
                explanation = "间谍可以查看所有玩家的真实身份。",
                displayKind = ClocktowerDisplayKind.Grimoire,
                displayTitle = "魔典",
                displayFooter = "这些是所有玩家的真实身份。只给间谍短暂查看。",
                hostInstruction = "轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。",
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
                explanation = "这个数字表示共情者两个存活邻居中有几个邪恶玩家。",
                hostInstruction = "轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。",
                displayOptions = { fakeNumberOptions("共情者信息", empathValue, 2, "邪恶存活邻居数量") },
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
            ),
            )
            add(
            infoStep(
                roleName = "占卜师",
                enName = "Fortune Teller",
                tellPlayer = fortuneTellerResult,
                explanation = "让占卜师选择两名玩家。如果其中有恶魔或红鲱鱼，告诉他“是”。",
                action = ClocktowerNightAction.FortuneTeller,
                displaySecondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                    .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                    .joinToString("   ") { seatNumberText(it) }
                    .takeIf { it.isNotBlank() },
                displayFooter = "查询这两名玩家",
                hostInstruction = "轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后只告诉他“是”或“否”。",
            ),
            )
            if (lastExecutedName != null) {
                add(
                    infoStep(
                        roleName = "送葬者",
                        enName = "Undertaker",
                        tellPlayer = "${playerSeatLabel(cards, lastExecutedName)} 的真实身份是 ${cards.firstOrNull { it.name == lastExecutedName }?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()}",
                        explanation = "送葬者每晚得知今天被处决玩家的真实身份。",
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        hostInstruction = "轻拍送葬者，示意睁眼。把今天被处决玩家的真实身份只给他看；看完后收回手机，示意闭眼。",
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
            if (sageNightDeath != null && demonCard != null && sagePair != null) {
                add(
                    ClocktowerNightStepUi(
                        title = "贤者",
                        actor = sageNightDeath,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = "如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。",
                        tellPlayer = "${demonCard.seatLabel(cards)} / ${sagePair.second.seatLabel(cards)}",
                        explanation = "贤者被恶魔杀死时，得知恶魔是两名玩家之一。",
                        displayKind = ClocktowerDisplayKind.EitherOne,
                        displayTitle = "贤者信息",
                        displayPrimary = "恶魔",
                        displaySecondary = twoSeatNumbers(demonCard, sagePair.second),
                        displayFooter = "在下面两位玩家之中",
                        roleEnName = "Sage",
                    ),
                )
            }
            if (ravenkeeperTrigger != null) {
                add(
                    ClocktowerNightStepUi(
                        title = "守鸦人",
                        actor = ravenkeeperTrigger,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = "轻拍 ${ravenkeeperTrigger.seatLabel(cards)}，示意睁眼。让他指一名玩家，在下面记录后把该玩家角色只给他看。",
                        tellPlayer = ravenkeeperTarget?.let { "${playerSeatLabel(cards, it)} 的真实角色是 ${cards.firstOrNull { card -> card.name == it }?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()}" },
                        explanation = "守鸦人只有在夜晚死亡时才会当晚醒来，选择一名玩家并得知其真实身份。",
                        action = ClocktowerNightAction.Ravenkeeper,
                        displayKind = if (ravenkeeperTarget != null) ClocktowerDisplayKind.RoleReveal else ClocktowerDisplayKind.None,
                        roleEnName = "Ravenkeeper",
                    ),
                )
            }
        }
    }

    val nightSteps = unfilteredNightSteps.filter { step ->
        (step.roleEnName == null || step.roleEnName in scriptRoleNames) &&
            !(script == ClocktowerScript.NoGreaterJoy && step.title in setOf(minionInfoTitle, demonInfoTitle))
    }

    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            onDismiss = { playerDisplayStep = null },
        )
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
                TextButton(onClick = onNewGame) {
                    Text(stringResource(R.string.new_game))
                }
            }
        }

        if (phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) {
            if (!nightStarted) {
                item {
                    HostScriptCard(
                        title = stringResource(R.string.clocktower_host_night_ready_title),
                        script = stringResource(R.string.clocktower_host_night_ready_script),
                        action = stringResource(R.string.clocktower_host_night_ready_action),
                    ) {
                        Button(
                            onClick = {
                                nightStarted = true
                            },
                            enabled = gameOutcome == null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.clocktower_host_start_night_flow))
                        }
                    }
                }
            } else {
                val currentStep = nightSteps[nightStepIndex.coerceIn(0, nightSteps.lastIndex)]
                item {
                    HostProgressCard(
                        title = if (phase == ClocktowerPhase.FirstNight) stringResource(R.string.clocktower_host_first_night_title) else stringResource(R.string.clocktower_host_night_title_format, round),
                        subtitle = stringResource(R.string.clocktower_current_step_format, currentStep.title),
                        progress = stringResource(R.string.clocktower_step_count_format, nightStepIndex + 1, nightSteps.size),
                    )
                }
                item {
                    ClocktowerNightStepCardLocalized(
                        cards = cards,
                        aliveCards = aliveCards,
                        step = currentStep,
                        selectedName = when (currentStep.action) {
                            ClocktowerNightAction.RedHerring -> redHerring
                            ClocktowerNightAction.Poison -> poisonTarget
                            ClocktowerNightAction.ButlerMaster -> butlerMaster
                            ClocktowerNightAction.MonkProtect -> monkProtectedTarget
                            ClocktowerNightAction.DemonKill -> pendingNightDeath
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
                                ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(if (ravenkeeperTarget == name) null else name)
                                else -> Unit
                            }
                        },
                        onSelectFortuneTellerFirst = { onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it) },
                        onSelectFortuneTellerSecond = { onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it) },
                        onSelectChambermaidFirst = { onSelectChambermaidFirst(if (chambermaidFirst == it) null else it) },
                        onSelectChambermaidSecond = { onSelectChambermaidSecond(if (chambermaidSecond == it) null else it) },
                        onShowPlayerDisplay = { displayStep ->
                            val actor = displayStep.actor
                            val unreliable = actor?.clocktowerRole?.enName == "Drunk" || actor?.name == poisonTarget
                            if (unreliable) {
                                onRecordEvent(
                                    ClocktowerEventType.UnreliableInformation,
                                    text("不可靠信息", "Unreliable information"),
                                    "${actor?.seatLabel(cards).orEmpty()}：${displayStep.displayPrimary ?: displayStep.tellPlayer.orEmpty()}",
                                    listOfNotNull(actor?.name),
                                )
                            }
                            playerDisplayStep = displayStep
                        },
                        canGoPrevious = nightStepIndex > 0,
                        onPrevious = {
                            if (nightStepIndex > 0) {
                                nightStepIndex -= 1
                            }
                        },
                        onNext = {
                            recordNightStep(currentStep)
                            if (nightStepIndex < nightSteps.lastIndex) {
                                nightStepIndex += 1
                            } else {
                                onConfirmNight()
                            }
                        },
                    )
                }
            }
        } else if (phase == ClocktowerPhase.Dawn) {
            val deathText = pendingNightDeath?.let { playerSeatLabel(cards, it) } ?: stringResource(R.string.clocktower_dawn_no_death)
            item {
                HostScriptCard(
                    title = stringResource(R.string.clocktower_dawn_title),
                    script = stringResource(R.string.clocktower_script_dawn),
                    action = stringResource(R.string.clocktower_action_dawn),
                ) {
                    HostInstructionBlock(
                        label = stringResource(R.string.clocktower_dawn_last_death_label),
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
                        Text(stringResource(R.string.clocktower_enter_day))
                    }
                }
            }
        } else {
            item {
                HostProgressCard(
                    title = stringResource(R.string.clocktower_day_title_format, round),
                    subtitle = stringResource(R.string.clocktower_day_alive_execution_format, aliveCards.size, executionThreshold),
                    progress = highestVoteName?.let { stringResource(R.string.clocktower_day_highest_vote_format, playerSeatLabel(cards, it), highestVoteCount) } ?: stringResource(R.string.clocktower_day_no_highest_vote),
                )
            }
            when (dayMode) {
                ClocktowerDayMode.Overview -> {
                    item {
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_script_day_overview_title),
                            script = stringResource(R.string.clocktower_script_day_overview_script),
                            action = stringResource(R.string.clocktower_script_day_overview_action),
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
                                Text(stringResource(R.string.clocktower_start_nomination))
                            }
                            if (scriptHasSlayer) {
                                OutlinedButton(
                                    onClick = {
                                        slayerClaimantName = null
                                        slayerTargetName = null
                                        dayMode = ClocktowerDayMode.Slayer
                                    },
                                    enabled = gameOutcome == null && slayerClaimantCandidates.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(stringResource(R.string.clocktower_slayer_action))
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
                                    Text(stringResource(R.string.clocktower_artist_question))
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
                                Text(stringResource(R.string.clocktower_end_day))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Slayer -> {
                    item {
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_slayer_action),
                            script = stringResource(R.string.clocktower_slayer_script),
                            action = stringResource(R.string.clocktower_slayer_action_hint),
                        ) {
                            if (slayerClaimantCandidates.isEmpty()) {
                                HostInstructionBlock(
                                    label = stringResource(R.string.clocktower_slayer_label),
                                    text = stringResource(R.string.clocktower_slayer_all_claimed),
                                    backgroundColor = Color(0xFFFFFCF6),
                                    textColor = Color(0xFF6F7B74),
                                )
                            } else {
                                HostActionSection(
                                    title = stringResource(R.string.clocktower_slayer_choose_claimant),
                                    helper = stringResource(R.string.clocktower_slayer_claimed_hint),
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
                                HostActionSection(title = stringResource(R.string.clocktower_slayer_choose_target)) {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name != slayerClaimantName },
                                        selectedName = slayerTargetName,
                                        enabled = gameOutcome == null,
                                        allCards = cards,
                                        onSelect = { slayerTargetName = if (slayerTargetName == it) null else it },
                                    )
                                }
                                Button(
                                    onClick = {
                                        val claimantName = slayerClaimantName
                                        val targetName = slayerTargetName
                                        if (claimantName != null && targetName != null) {
                                            onSlayerShot(claimantName, targetName)
                                            slayerClaimantName = null
                                            slayerTargetName = null
                                            dayMode = ClocktowerDayMode.Overview
                                        }
                                    },
                                    enabled = slayerClaimantName != null && slayerTargetName != null && gameOutcome == null,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(stringResource(R.string.clocktower_slayer_resolve))
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    slayerClaimantName = null
                                    slayerTargetName = null
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_back_to_day))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Artist -> {
                    item {
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_artist_question),
                            script = stringResource(R.string.clocktower_artist_script),
                            action = stringResource(R.string.clocktower_artist_action),
                        ) {
                            HostActionSection(
                                title = stringResource(R.string.clocktower_artist_choose_claimant),
                                helper = stringResource(R.string.clocktower_artist_claimed_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = artistClaimantCandidates,
                                    selectedName = artistClaimantName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { onSelectArtistClaimant(if (artistClaimantName == it) null else it) },
                                )
                            }
                            Button(
                                onClick = onConfirmArtistQuestion,
                                enabled = artistClaimantName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_artist_record))
                            }
                            OutlinedButton(
                                onClick = {
                                    onSelectArtistClaimant(null)
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_back_to_day))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Klutz -> {
                    item {
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_event_klutz_choice),
                            script = stringResource(R.string.clocktower_klutz_script_format, playerSeatLabel(cards, pendingKlutzName)),
                            action = stringResource(R.string.clocktower_klutz_action),
                        ) {
                            HostActionSection(title = stringResource(R.string.clocktower_klutz_choose_player)) {
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != pendingKlutzName },
                                    selectedName = klutzChoiceName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { onSelectKlutzChoice(if (klutzChoiceName == it) null else it) },
                                )
                            }
                            Button(
                                onClick = onConfirmKlutzChoice,
                                enabled = klutzChoiceName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_klutz_confirm))
                            }
                        }
                    }
                }

                ClocktowerDayMode.Nomination -> {
                    item {
                        val nominatorCard = cards.firstOrNull { it.name == nominatorName }
                        val nomineeCard = cards.firstOrNull { it.name == nomineeName }
                        val virginFirstNomination = nomineeCard?.clocktowerRole?.enName == "Virgin" && !virginUsed
                        val virginExecutes = virginFirstNomination && nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_nomination_title),
                            script = if (nominatorName != null && nomineeName != null) {
                                stringResource(R.string.clocktower_nomination_script_format, playerSeatLabel(cards, nominatorName), playerSeatLabel(cards, nomineeName))
                            } else {
                                stringResource(R.string.clocktower_nomination_script_empty)
                            },
                            action = when {
                                virginExecutes -> stringResource(R.string.clocktower_nomination_virgin_executes_action)
                                virginFirstNomination -> stringResource(R.string.clocktower_nomination_virgin_spent_action)
                                else -> stringResource(R.string.clocktower_nomination_normal_action)
                            },
                        ) {
                            HostActionSection(title = stringResource(R.string.clocktower_nomination_choose_nominator)) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = nominatorName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { nominatorName = if (nominatorName == it) null else it },
                                )
                            }
                            HostActionSection(title = stringResource(R.string.clocktower_nomination_choose_nominee)) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = nomineeName,
                                    enabled = gameOutcome == null,
                                    allCards = cards,
                                    onSelect = { nomineeName = if (nomineeName == it) null else it },
                                )
                            }
                            if (virginFirstNomination) {
                                HostInstructionBlock(
                                    label = stringResource(R.string.clocktower_nomination_virgin_label),
                                    text = if (virginExecutes) {
                                        stringResource(R.string.clocktower_nomination_virgin_executes_detail, playerSeatLabel(cards, nomineeName), playerSeatLabel(cards, nominatorName))
                                    } else {
                                        stringResource(R.string.clocktower_nomination_virgin_spent_detail, playerSeatLabel(cards, nomineeName), playerSeatLabel(cards, nominatorName))
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
                                        onVirginNomination(chosenNominator, chosenNominee, virginExecutes)
                                    }
                                    if (chosenNominator != null && chosenNominee != null) {
                                        onRecordEvent(
                                            ClocktowerEventType.Nomination,
                                            text("提名", "Nomination"),
                                            "${playerSeatLabel(cards, chosenNominator)} → ${playerSeatLabel(cards, chosenNominee)}",
                                            listOf(chosenNominator, chosenNominee),
                                        )
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
                                        virginExecutes -> stringResource(R.string.clocktower_nomination_execute_nominator)
                                        virginFirstNomination -> stringResource(R.string.clocktower_nomination_virgin_spent_start_vote)
                                        else -> stringResource(R.string.clocktower_nomination_start_vote)
                                    },
                                )
                            }
                        }
                    }
                }

                ClocktowerDayMode.Vote -> {
                    item {
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_vote_title),
                            script = stringResource(R.string.clocktower_vote_script_format, playerSeatLabel(cards, nomineeName)),
                            action = stringResource(R.string.clocktower_vote_action_format, executionThreshold),
                        ) {
                            StepperRow(
                                label = stringResource(R.string.clocktower_vote_count_label),
                                value = currentVoteCount,
                                range = 0..aliveCards.size,
                                onChange = { currentVoteCount = it },
                            )
                            val reached = currentVoteCount >= executionThreshold
                            HostInstructionBlock(
                                label = stringResource(R.string.clocktower_vote_result_label),
                                text = if (reached) {
                                    stringResource(R.string.clocktower_vote_reached_format, playerSeatLabel(cards, nomineeName), currentVoteCount)
                                } else {
                                    stringResource(R.string.clocktower_vote_not_reached_format, playerSeatLabel(cards, nomineeName), currentVoteCount)
                                },
                                backgroundColor = if (reached) Color(0xFFEAF2EA) else Color(0xFFFFFCF6),
                                textColor = if (reached) Color(0xFF2F5D50) else Color(0xFF6F7B74),
                            )
                            Button(
                                onClick = {
                                    onRecordEvent(
                                        ClocktowerEventType.Vote,
                                        text("投票结果", "Vote result"),
                                        "${playerSeatLabel(cards, nomineeName)} · $currentVoteCount/$executionThreshold",
                                        listOfNotNull(nomineeName),
                                    )
                                    if (reached && currentVoteCount >= highestVoteCount) {
                                        highestVoteName = nomineeName
                                        highestVoteCount = currentVoteCount
                                    }
                                    dayMode = ClocktowerDayMode.Overview
                                },
                                enabled = nomineeName != null && gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_vote_continue_nomination))
                            }
                            OutlinedButton(
                                onClick = {
                                    if (reached && currentVoteCount >= highestVoteCount) {
                                        highestVoteName = nomineeName
                                        highestVoteCount = currentVoteCount
                                    }
                                    onSelectExecution(highestVoteName?.takeIf { highestVoteCount >= executionThreshold })
                                    dayMode = ClocktowerDayMode.EndConfirm
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_end_day))
                            }
                        }
                    }
                }

                ClocktowerDayMode.EndConfirm -> {
                    item {
                        val target = selectedExecution
                        HostScriptCard(
                            title = stringResource(R.string.clocktower_end_confirm_title),
                            script = target?.let { stringResource(R.string.clocktower_end_confirm_script_execution, playerSeatLabel(cards, it), highestVoteCount) } ?: stringResource(R.string.clocktower_end_confirm_script_no_execution),
                            action = target?.let { stringResource(R.string.clocktower_end_confirm_action_execution, playerSeatLabel(cards, it)) } ?: stringResource(R.string.clocktower_end_confirm_action_no_execution),
                        ) {
                            Button(
                                onClick = onConfirmDay,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(target?.let { stringResource(R.string.clocktower_end_confirm_execute) } ?: stringResource(R.string.clocktower_end_confirm_enter_night))
                            }
                            OutlinedButton(
                                onClick = { dayMode = ClocktowerDayMode.Overview },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.clocktower_back_to_day))
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
                TextButton(onClick = onNewGame) {
                    Text(stringResource(R.string.new_game))
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
                                cards = aliveCards.filter { it.clocktowerTeam != ClocktowerTeam.Demon },
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
                                neighbors.count(::isClocktowerEvil),
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
private fun ClocktowerPlayerDisplayCardLocalized(
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
                        secondary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                color = Color.White,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
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
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
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
                        secondary?.let {
                            Text(
                                it,
                                color = Color(0xFFFFF4DC),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

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

@Composable
private fun ClocktowerNightStepCardLocalized(
    cards: List<PlayerCard>,
    aliveCards: List<PlayerCard>,
    step: ClocktowerNightStepUi,
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
    onShowPlayerDisplay: (ClocktowerNightStepUi) -> Unit,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val command = when {
        !step.isRealAction -> "${step.title} 的占位操作"
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> "唤醒占卜师：${step.actor.seatLabel(cards)}"
        step.actor != null -> "唤醒 ${step.actor.seatLabel(cards)}"
        step.wakeText != null -> step.wakeText
        else -> step.title
    }
    val helper = when {
        !step.isRealAction && step.reason.isNotBlank() -> step.reason
        step.action == ClocktowerNightAction.Chambermaid -> "让她选择两名玩家，点查询后直接展示数字。"
        step.action == ClocktowerNightAction.FortuneTeller -> "让他选择两名玩家，点查询后直接展示结果。"
        step.action == ClocktowerNightAction.RedHerring -> "只给说书人看，不公开。"
        step.action == ClocktowerNightAction.Poison -> "让他指一名玩家，在下方记录。"
        step.action == ClocktowerNightAction.MonkProtect -> "让他指一名除自己外的玩家。"
        step.action == ClocktowerNightAction.DemonKill -> "现在只记录，天亮再宣布。"
        step.action == ClocktowerNightAction.Ravenkeeper -> "选目标后，把该玩家角色只给他看。"
        step.displayKind != ClocktowerDisplayKind.None -> "只给被唤醒的玩家看，确认后示意闭眼。"
        else -> step.explanation
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "${step.title} · ${if (step.isRealAction) "真实行动" else "占位"}",
                color = Color(0xFF2F5D50),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                command.orEmpty(),
                color = if (step.isRealAction) Color(0xFF1F2925) else Color(0xFF9A4B36),
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Black,
            )

        when (step.action) {
            ClocktowerNightAction.RedHerring -> {
                if (step.isRealAction) {
                    val candidates = clocktowerRedHerringCandidates(cards, aliveCards)
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
                    Button(
                        onClick = { onShowPlayerDisplay(step) },
                        enabled = step.isRealAction && fortuneTellerFirst != null && fortuneTellerSecond != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("查询并展示")
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
                    Button(
                        onClick = { onShowPlayerDisplay(step) },
                        enabled = step.isRealAction && chambermaidFirst != null && chambermaidSecond != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("查询并展示")
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
                    Text(it, color = Color(0xFF2F5D50), fontWeight = FontWeight.SemiBold)
                }

            if (step.displayOptions.isNotEmpty()) {
                Text("能力不可靠：请选择一个结果展示。", color = Color(0xFF8C4B20), fontWeight = FontWeight.Bold)
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
            } else if (step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid) {
                OutlinedButton(
                    onClick = { onShowPlayerDisplay(step) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.clocktower_host_show_to_player))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = canGoPrevious,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.previous_step))
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.clocktower_host_finish_next))
                }
            }

            Text(helper, color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
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
        .filterNot { it.type == ClocktowerEventType.System || it.type == ClocktowerEventType.Phase || it.type == ClocktowerEventType.Information }
        .sortedBy { it.sequence }

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
    onNewGame: () -> Unit,
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
                TextButton(onClick = onNewGame) {
                    Text(stringResource(R.string.new_game))
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
            TextButton(onClick = onNewGame) {
                Text(stringResource(R.string.play_again))
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
