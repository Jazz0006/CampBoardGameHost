1: package com.codex.campboardgamehost
2: 
3: import android.content.Context
4: import android.content.res.Configuration
5: import android.util.Log
6: import androidx.activity.compose.BackHandler
7: import androidx.compose.foundation.background
8: import androidx.compose.foundation.layout.Box
9: import androidx.compose.foundation.layout.Column
10: import androidx.compose.foundation.layout.fillMaxSize
11: import androidx.compose.foundation.layout.fillMaxWidth
12: import androidx.compose.foundation.layout.padding
13: import androidx.compose.foundation.layout.size
14: import androidx.compose.foundation.lazy.items
15: import androidx.compose.foundation.shape.RoundedCornerShape
16: import androidx.compose.material3.Card
17: import androidx.compose.material3.CardDefaults
18: import androidx.compose.material3.MaterialTheme
19: import androidx.compose.material3.Surface
20: import androidx.compose.material3.Text
21: import androidx.compose.runtime.Composable
22: import androidx.compose.runtime.CompositionLocalProvider
23: import androidx.compose.runtime.DisposableEffect
24: import androidx.compose.runtime.LaunchedEffect
25: import androidx.compose.runtime.SideEffect
26: import androidx.compose.runtime.getValue
27: import androidx.compose.runtime.key
28: import androidx.compose.runtime.mutableStateListOf
29: import androidx.compose.runtime.mutableStateOf
30: import androidx.compose.runtime.remember
31: import androidx.compose.runtime.rememberCoroutineScope
32: import androidx.compose.runtime.rememberUpdatedState
33: import androidx.compose.runtime.setValue
34: import androidx.compose.runtime.withFrameNanos
35: import androidx.compose.ui.Modifier
36: import androidx.compose.ui.graphics.Color
37: import androidx.compose.ui.platform.LocalContext
38: import androidx.compose.ui.res.stringResource
39: import androidx.compose.ui.unit.dp
40: import androidx.lifecycle.LifecycleEventObserver
41: import androidx.lifecycle.compose.LocalLifecycleOwner
42: import com.codex.campboardgamehost.clocktower.domain.RoleId
43: import com.codex.campboardgamehost.clocktower.domain.RulesetRef
44: import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
45: import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
46: import com.codex.campboardgamehost.clocktower.domain.GameState
47: import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
48: import com.codex.campboardgamehost.clocktower.domain.CharacterType
49: import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
50: import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
51: import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
52: import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode
53: import com.codex.campboardgamehost.clocktower.domain.StorytellerRecommendationUxPolicy
54: import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
55: import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
56: import com.codex.campboardgamehost.clocktower.domain.kind
57: import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
58: import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
59: import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
60: import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
61: import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
62: import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
63: import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
64: import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
65: import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
66: import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
67: import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
68: import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel
69: import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
70: import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
71: import com.codex.campboardgamehost.clocktower.session.commitPoisonTargetBoundary
72: import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision
73: import com.codex.campboardgamehost.clocktower.session.synchronizePoisonTargetWithinCurrentRevision
74: import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
75: import com.codex.campboardgamehost.clocktower.session.NightCheckpointHostTransaction
76: import com.codex.campboardgamehost.clocktower.session.NightCheckpointRevisionIntent
77: import com.codex.campboardgamehost.clocktower.session.NightResolutionEvent
78: import com.codex.campboardgamehost.clocktower.session.DawnCommitIntent
79: import com.codex.campboardgamehost.clocktower.session.DawnDurableMaterializationState
80: import com.codex.campboardgamehost.clocktower.session.NightDawnDurableMaterializationPlanner
81: import com.codex.campboardgamehost.clocktower.session.NightDawnPoisonResolutionInput
82: import com.codex.campboardgamehost.clocktower.session.NightDawnPoisonRecoveryAuthority
83: import com.codex.campboardgamehost.clocktower.session.DuskPoisonExpiryMaterializationPlanner
84: import com.codex.campboardgamehost.clocktower.session.DuskPoisonExpiryMaterializationState
85: import com.codex.campboardgamehost.clocktower.session.DuskPoisonExpiryRecoveryAuthority
86: import com.codex.campboardgamehost.clocktower.session.NightDawnDeathResolutionInput
87: import com.codex.campboardgamehost.clocktower.session.NightDawnResolutionPlanner
88: import com.codex.campboardgamehost.clocktower.session.NightResolutionContinuation
89: import com.codex.campboardgamehost.clocktower.session.resolveTroubleBrewingImpSelfKillSuccession
90: import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
91: import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationPrewarmCoordinator
92: import com.codex.campboardgamehost.clocktower.session.TroubleBrewingSetupRecommendationRevealCoordinator
93: import com.codex.campboardgamehost.clocktower.session.TroubleBrewingFirstNightPrecomputeCoordinator
94: import com.codex.campboardgamehost.clocktower.setup.NoGreaterJoyProductionSetupPreparer
95: import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingCommittedSetupAdapter
96: import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
97: import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingProductionSetupPreparer
98: import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupPresetJson
99: import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
100: import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
101: import com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmCoordinator
102: import com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmRequest
103: import com.codex.campboardgamehost.clocktower.epistemic.A4MainThreadFrameTelemetry
104: import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildExecutor
105: import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationDurabilityGate
106: import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildRequest
107: import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
108: import com.codex.campboardgamehost.clocktower.epistemic.A4ShadowWorldSetCache
109: import com.codex.campboardgamehost.clocktower.epistemic.A4ShadowLifecycleInvalidator
110: import com.codex.campboardgamehost.clocktower.epistemic.A4WorldEngineRollout
111: import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
112: import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
113: import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
114: import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
115: import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
116: import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
117: import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
118: import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
119: import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
120: import com.codex.campboardgamehost.clocktower.epistemic.PlayerKnowledgeSnapshot
121: import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInputFactory
122: import com.codex.campboardgamehost.clocktower.recommendation.sde.SdePostCommitCorrelationCoordinator
123: import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeRuntimeShadowCoordinator
124: import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeRuntimeShadowIdentity
125: import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
126: import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
127: import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
128: import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
129: import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
130: import com.codex.campboardgamehost.debug.DebugFlightRecorder
131: import kotlinx.coroutines.Dispatchers
132: import kotlinx.coroutines.isActive
133: import kotlinx.coroutines.launch
134: import kotlinx.coroutines.withContext
135: import org.json.JSONArray
136: import org.json.JSONObject
137: import java.util.Locale
138: import java.util.UUID
139: 
140: private enum class Screen {
141:     Landing,
142:     Setup,
143:     GameSelection,
144:     UndercoverSettings,
145:     ClocktowerSettings,
146:     Settings,
147:     PassPhone,
148:     RevealCard,
149:     ClocktowerJudge,
150:     Game,
151: }
152: 
153: internal enum class LanguageMode(val prefsValue: String) {
154:     System("system"),
155:     Chinese("zh"),
156:     English("en"),
157: }
158: 
159: internal fun PlayerCard.abilitySubject(poisonTarget: String?): AbilitySubject = AbilitySubject(
160:     actualRole = clocktowerRole?.enName,
161:     shownRole = clocktowerShownRole?.enName,
162:     isPoisoned = poisonTarget == name && eliminatedRound == null,
163:     isAlive = eliminatedRound == null,
164: )
165: 
166: private fun Context.playerName(number: Int): String = getString(R.string.default_player_name_format, number)
167: 
168: private const val PREFS_NAME = "camp_board_game_host"
169: private const val COMMON_PLAYERS_KEY = "common_players"
170: private const val LANGUAGE_MODE_KEY = "language_mode"
171: private const val STORYTELLER_EXPERIENCE_MODE_KEY = "storyteller_experience_mode"
172: private const val ACTIVE_GAME_STATE_KEY = "active_game_state"
173: private const val GAME_HISTORY_KEY = "game_history"
174: internal const val A4_IDENTITY_PREWARM_LOG_TAG = "A4IdentityPrewarm"
175: internal const val A4_OBSERVATION_CACHE_UPDATE_LOG_TAG = "A4ObservationCacheUpdate"
176: internal const val SDE_RUNTIME_SHADOW_LOG_TAG = "SdeRuntimeShadow"
177: private const val MAX_GAME_HISTORY = 20
178: internal const val MIN_PLAYERS = 3
179: internal const val MIN_CLOCKTOWER_PLAYERS = 5
180: internal const val MAX_PLAYERS = 15
181: 
182: private fun Context.localized(languageMode: LanguageMode): Context {
183:     if (languageMode == LanguageMode.System) return this
184:     val locale = Locale(languageMode.prefsValue)
185:     val config = Configuration(resources.configuration)
186:     config.setLocale(locale)
187:     return createConfigurationContext(config)
188: }
189: 
190: private fun Context.loadLanguageMode(): LanguageMode {
191:     val value = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
192:         .getString(LANGUAGE_MODE_KEY, LanguageMode.System.prefsValue)
193:     return LanguageMode.entries.firstOrNull { it.prefsValue == value } ?: LanguageMode.System
194: }
195: 
196: private fun Context.saveLanguageMode(languageMode: LanguageMode) {
197:     getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
198:         .edit()
199:         .putString(LANGUAGE_MODE_KEY, languageMode.prefsValue)
200:         .apply()
201: }
202: 
203: private fun Context.loadStorytellerExperienceMode(): StorytellerExperienceMode =
204:     StorytellerExperienceMode.fromPrefsValue(
205:         getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
206:             .getString(STORYTELLER_EXPERIENCE_MODE_KEY, null),
207:     )
208: 
209: private fun Context.saveStorytellerExperienceMode(mode: StorytellerExperienceMode) {
210:     getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
211:         .edit()
212:         .putString(STORYTELLER_EXPERIENCE_MODE_KEY, mode.prefsValue)
213:         .apply()
214: }
215: 
216: private fun Context.loadCommonPlayers(): List<String> {
217:     val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
218:     val hasStoredPlayers = preferences.contains(COMMON_PLAYERS_KEY)
219:     val raw = preferences.getString(COMMON_PLAYERS_KEY, null)
220:     val storedPlayers = raw?.let { encoded ->
221:         runCatching {
222:             val json = JSONArray(encoded)
223:             List(json.length()) { index -> json.getString(index) }
224:                 .map { it.trim() }
225:                 .filter { it.isNotEmpty() }
226:                 .distinct()
227:         }.getOrElse { emptyList() }
228:     } ?: emptyList()
229:     return resolveInitialCommonPlayers(
230:         hasStoredPlayers = hasStoredPlayers,
231:         storedPlayers = storedPlayers,
232:     )
233: }
234: 
235: private fun Context.saveCommonPlayers(players: List<String>) {
236:     val json = JSONArray()
237:     players.map { it.trim() }
238:         .filter { it.isNotEmpty() }
239:         .distinct()
240:         .forEach { json.put(it) }
241:     getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
242:         .edit()
243:         .putString(COMMON_PLAYERS_KEY, json.toString())
244:         .apply()
245: }
246: 
247: private fun Screen.isActiveGameScreen(): Boolean = when (this) {
248:     Screen.PassPhone,
249:     Screen.RevealCard,
250:     Screen.ClocktowerJudge,
251:     Screen.Game -> true
252:     Screen.Landing,
253:     Screen.Setup,
254:     Screen.GameSelection,
255:     Screen.UndercoverSettings,
256:     Screen.ClocktowerSettings,
257:     Screen.Settings -> false
258: }
259: 
260: private fun Context.saveActiveGameState(snapshot: JSONObject): Boolean =
261:     getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
262:         .edit()
263:         .putString(ACTIVE_GAME_STATE_KEY, snapshot.toString())
264:         .commit()
265: 
266: private fun Context.loadActiveGameStateJson(): JSONObject? {
267:     val raw = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
268:         .getString(ACTIVE_GAME_STATE_KEY, null)
269:         ?: return null
270:     return runCatching { JSONObject(raw) }.getOrNull()
271: }
272: 
273: private fun Context.clearActiveGameState() {
274:     getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
275:         .edit()
276:         .remove(ACTIVE_GAME_STATE_KEY)
277:         .commit()
278: }
279: 
280: private fun Context.loadSavedGamePreview(localizedContext: Context): SavedGamePreview? =
281:     RecoveryPreviewLoader.load(
282:         raw = loadActiveGameStateJson(),
283:         prepare = { raw -> prepareCurrentRecoveryPlan(raw) },
284:         clearRejected = { clearActiveGameState() },
285:     )?.toSavedGamePreview(localizedContext)
286: 
287: private fun clocktowerRoleByName(enName: String?): ClocktowerRole? {
288:     if (enName.isNullOrBlank()) return null
289:     return completeClocktowerRoles.firstOrNull { it.enName == enName }
290: }
291: 
292: private fun archivedGameReviewFromJson(entry: JSONObject): ArchivedGameReview? =
293:     GameArchiveJsonCodec.decodeEntry(entry, ::clocktowerRoleByName)
294: 
295: private fun Context.loadGameHistory(): List<ArchivedGameReview> {
296:     val raw = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
297:         .getString(GAME_HISTORY_KEY, null)
298:         ?: return emptyList()
299:     return runCatching {
300:         val array = JSONArray(raw)
301:         buildList {
302:             for (index in 0 until array.length()) {
303:                 array.optJSONObject(index)?.let { archivedGameReviewFromJson(it)?.let(::add) }
304:             }
305:         }
306:     }.getOrDefault(emptyList())
307: }
308: 
309: private fun Context.archiveGame(record: GameArchiveRecord): List<ArchivedGameReview> {
310:     if (record.cards.isEmpty()) return loadGameHistory()
311:     val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
312:     val existing = runCatching { JSONArray(prefs.getString(GAME_HISTORY_KEY, "[]")) }.getOrDefault(JSONArray())
313:     val archivedAt = System.currentTimeMillis()
314:     val next = JSONArray().apply {
315:         put(
316:             GameArchiveJsonCodec.encodeEntry(
317:                 record = record,
318:                 id = archivedAt,
319:                 archivedAtMillis = archivedAt,
320:             ),
321:         )
322:         for (index in 0 until minOf(existing.length(), MAX_GAME_HISTORY - 1)) {
323:             existing.optJSONObject(index)?.let(::put)
324:         }
325:     }
326:     prefs.edit().putString(GAME_HISTORY_KEY, next.toString()).commit()
327:     return loadGameHistory()
328: }
329: 
330: internal fun Role.labelResId(): Int = when (this) {
331:     Role.Civilian -> R.string.role_civilian
332:     Role.Undercover -> R.string.role_undercover
333:     Role.Blank -> R.string.role_blank
334:     Role.Villager -> R.string.role_villager
335:     Role.Werewolf -> R.string.role_werewolf
336:     Role.Seer -> R.string.role_seer
337:     Role.Witch -> R.string.role_witch
338:     Role.Hunter -> R.string.role_hunter
339: }
340: 
341: internal fun LanguageMode.labelResId(): Int = when (this) {
342:     LanguageMode.System -> R.string.language_system
343:     LanguageMode.Chinese -> R.string.language_chinese
344:     LanguageMode.English -> R.string.language_english
345: }
346: 
347: private val troubleBrewingRoles = listOf(
348:     ClocktowerRole(ClocktowerTeam.Townsfolk, "洗衣妇", "Washerwoman", "得知某个镇民在两名玩家之一中。", "Learn that one of two players is a particular Townsfolk."),
349:     ClocktowerRole(ClocktowerTeam.Townsfolk, "图书管理员", "Librarian", "得知某个外来者在两名玩家之一中，或得知没有外来者。", "Learn that one of two players is a particular Outsider, or that there are no Outsiders."),
350:     ClocktowerRole(ClocktowerTeam.Townsfolk, "调查员", "Investigator", "得知某个爪牙在两名玩家之一中，或得知没有爪牙。", "Learn that one of two players is a particular Minion, or that there are no Minions."),
351:     ClocktowerRole(ClocktowerTeam.Townsfolk, "厨师", "Chef", "得知有多少对邪恶玩家相邻而坐。", "Learn how many pairs of evil players are sitting next to each other."),
352:     ClocktowerRole(ClocktowerTeam.Townsfolk, "共情者", "Empath", "每晚得知相邻存活玩家中有几名邪恶玩家。", "Each night, learn how many living neighbors are evil."),
353:     ClocktowerRole(ClocktowerTeam.Townsfolk, "占卜师", "Fortune Teller", "每晚选择两名玩家，得知其中是否有恶魔。", "Each night, choose two players and learn if either is the Demon."),
354:     ClocktowerRole(ClocktowerTeam.Townsfolk, "守鸦人", "Ravenkeeper", "若在夜晚死亡，选择一名玩家并得知其角色。", "If you die at night, choose a player and learn their character."),
355:     ClocktowerRole(ClocktowerTeam.Townsfolk, "士兵", "Soldier", "你不会因恶魔而死亡。", "You are safe from the Demon."),
356:     ClocktowerRole(ClocktowerTeam.Townsfolk, "市长", "Mayor", "若只剩三名存活玩家且白天无人被处决，你的阵营获胜。若你将在夜晚死亡，另一名玩家可能代替你死亡。", "If only three players live and no execution occurs, your team wins. If you would die at night, another player might die instead."),
357:     ClocktowerRole(ClocktowerTeam.Outsider, "管家", "Butler", "每天选择一名主人，白天只能在主人投票时投票。", "Each day, choose a master. You may only vote if your master votes."),
358:     ClocktowerRole(ClocktowerTeam.Outsider, "酒鬼", "Drunk", "你以为自己是镇民，但其实能力失效。", "You think you are a Townsfolk, but your ability is not working."),
359:     ClocktowerRole(ClocktowerTeam.Outsider, "隐士", "Recluse", "你可能被登记为邪恶、爪牙或恶魔，即使死亡后也是。", "You might register as evil and as a Minion or Demon, even if dead."),
360:     ClocktowerRole(ClocktowerTeam.Outsider, "圣徒", "Saint", "若你被处决，你的阵营失败。", "If you are executed, your team loses."),
361:     ClocktowerRole(ClocktowerTeam.Minion, "投毒者", "Poisoner", "每晚选择一名玩家，使其能力暂时失效。", "Each night, choose a player. Their ability temporarily stops working."),
362:     ClocktowerRole(ClocktowerTeam.Minion, "间谍", "Spy", "每晚查看说书人的魔典；你可能被登记为善良、镇民或外来者，即使死亡后也是。", "Each night, view the Storyteller grimoire. You might register as good and as a Townsfolk or Outsider, even if dead."),
363:     ClocktowerRole(ClocktowerTeam.Minion, "男爵", "Baron", "本局加入额外外来者。", "Extra Outsiders are in play."),
364:     ClocktowerRole(ClocktowerTeam.Minion, "猩红女巫", "Scarlet Woman", "若恶魔在五人以上时死亡，你可能变成恶魔。", "If the Demon dies with five or more players alive, you may become the Demon."),
365:     ClocktowerRole(ClocktowerTeam.Demon, "小恶魔", "Imp", "每晚选择一名玩家死亡；可选择自己并传递恶魔身份。", "Each night, choose a player to die. You may choose yourself to pass on the Demon role."),
366: )
367: 
368: internal val completeTroubleBrewingRoles = (troubleBrewingRoles + listOf(
369:     ClocktowerRole(ClocktowerTeam.Townsfolk, "送葬者", "Undertaker", "每个夜晚，得知今天被处决玩家的角色。", "Each night, learn which character died by execution today."),
370:     ClocktowerRole(ClocktowerTeam.Townsfolk, "僧侣", "Monk", "每个夜晚，选择除自己以外的一名玩家，使其免受恶魔伤害。", "Each night, choose a player other than yourself: they are safe from the Demon tonight."),
371:     ClocktowerRole(ClocktowerTeam.Townsfolk, "圣女", "Virgin", "首次被镇民提名时，提名者立即被处决。", "The first time you are nominated by a Townsfolk, the nominator is executed immediately."),
372:     ClocktowerRole(ClocktowerTeam.Townsfolk, "杀手", "Slayer", "每局一次，白天选择一名玩家；若其是恶魔，该玩家死亡。", "Once per game during the day, choose a player: if they are the Demon, that player dies."),
373: )).distinctBy { it.enName }
374: 
375: private val noGreaterJoyExtraRoles = listOf(
376:     ClocktowerRole(ClocktowerTeam.Townsfolk, "钟表匠", "Clockmaker", "第一夜得知恶魔到最近爪牙相隔几步。", "On the first night, learn how many steps from the Demon to their nearest Minion."),
377:     ClocktowerRole(ClocktowerTeam.Townsfolk, "侍女", "Chambermaid", "每晚选择两名存活玩家，得知其中有几人当晚因自己的能力醒来。", "Each night, choose two alive players and learn how many woke tonight due to their ability."),
378:     ClocktowerRole(ClocktowerTeam.Townsfolk, "艺术家", "Artist", "每局一次，白天私下向说书人询问一个是非问题。", "Once per game during the day, privately ask the Storyteller a yes/no question."),
379:     ClocktowerRole(ClocktowerTeam.Townsfolk, "贤者", "Sage", "如果被恶魔杀死，得知恶魔是两名玩家之一。", "If the Demon kills you, learn that it is one of two players."),
380:     ClocktowerRole(ClocktowerTeam.Outsider, "呆瓜", "Klutz", "当你得知自己死亡时，公开选择一名存活玩家；若对方邪恶，你的阵营失败。", "When you learn that you died, publicly choose one alive player: if they are evil, your team loses."),
381: )
382: 
383: private val completeClocktowerRoles = (completeTroubleBrewingRoles + noGreaterJoyExtraRoles).distinctBy { it.enName }
384: 
385: private val noGreaterJoyRoleNames = setOf(
386:     "Clockmaker",
387:     "Investigator",
388:     "Empath",
389:     "Chambermaid",
390:     "Artist",
391:     "Sage",
392:     "Drunk",
393:     "Klutz",
394:     "Baron",
395:     "Scarlet Woman",
396:     "Imp",
397: )
398: 
399: internal fun clocktowerRolesForScript(script: ClocktowerScript): List<ClocktowerRole> = when (script) {
400:     ClocktowerScript.TroubleBrewing -> completeTroubleBrewingRoles
401:     ClocktowerScript.NoGreaterJoy -> completeClocktowerRoles.filter { it.enName in noGreaterJoyRoleNames }
402: }
403: 
404: internal fun ClocktowerTeam.label(context: Context): String = when (this) {
405:     ClocktowerTeam.Townsfolk -> context.getString(R.string.clocktower_team_townsfolk)
406:     ClocktowerTeam.Outsider -> context.getString(R.string.clocktower_team_outsider)
407:     ClocktowerTeam.Minion -> context.getString(R.string.clocktower_team_minion)
408:     ClocktowerTeam.Demon -> context.getString(R.string.clocktower_team_demon)
409: }
410: 
411: internal fun ClocktowerRole.nameFor(language: String): String = if (language == "en") enName else zhName
412: 
413: internal fun ClocktowerRole.descriptionFor(language: String): String = if (language == "en") enDescription else zhDescription
414: 
415: internal fun ClocktowerScript.nameFor(language: String): String = when (this) {
416:     ClocktowerScript.TroubleBrewing -> if (language == "en") "Trouble Brewing" else "暗流涌动"
417:     ClocktowerScript.NoGreaterJoy -> "No Greater Joy"
418: }
419: 
420: private fun defaultClocktowerScriptFor(playerCount: Int): ClocktowerScript =
421:     if (playerCount in 5..6) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing
422: 
423: internal fun canStartClocktowerScript(script: ClocktowerScript): Boolean =
424:     script == ClocktowerScript.TroubleBrewing || script == ClocktowerScript.NoGreaterJoy
425: 
426: @Composable
427: internal fun CampBoardGameHostApp() {
428:     val baseContext = LocalContext.current
429:     val activeGameClocktowerRulesetCatalog = remember(baseContext) {
430:         BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
431:     }
432:     val decisionTraceArchiveStore = remember(baseContext) {
433:         DecisionTraceArchivePreferencesStorage.fromContext(baseContext)
434:     }
435:     val lifecycleOwner = LocalLifecycleOwner.current
436:     var languageMode by remember { mutableStateOf(baseContext.loadLanguageMode()) }
437:     var storytellerExperienceMode by remember { mutableStateOf(baseContext.loadStorytellerExperienceMode()) }
438:     val storytellerRecommendationUxPolicy =
439:         StorytellerRecommendationUxPolicy.fromExperienceMode(storytellerExperienceMode)
440:     val automaticStorytellerInfo = storytellerRecommendationUxPolicy.automaticExecution
441:     val context = remember(languageMode) { baseContext.localized(languageMode) }
442:     val language = context.resources.configuration.locales[0].language
443:     var screen by remember { mutableStateOf(Screen.Landing) }
444:     var currentGameKind by remember { mutableStateOf(GameKind.Undercover) }
445:     var savedGamePreview by remember(context) { mutableStateOf(baseContext.loadSavedGamePreview(context)) }
446:     var gameHistory by remember { mutableStateOf(baseContext.loadGameHistory()) }
447:     var showHostTools by remember { mutableStateOf(false) }
448:     var hostToolTab by remember { mutableStateOf(HostToolTab.Roles) }
449:     var showNewGameConfirmation by remember { mutableStateOf(false) }
450:     var undercoverCount by remember { mutableStateOf(1) }
451:     var includeBlank by remember { mutableStateOf(false) }
452:     var lastWordsMode by remember { mutableStateOf(LastWordsMode.FirstDay) }
453:     var currentDealIndex by remember { mutableStateOf(0) }
454:     var round by remember { mutableStateOf(1) }
455:     var selectedElimination by remember { mutableStateOf<String?>(null) }
456:     var clocktowerPhase by remember { mutableStateOf(ClocktowerPhase.FirstNight) }
457:     // The Demon may revise this while awake. Only the confirmed attack may
458:     // reach protection, Mayor redirection, death, or succession resolution.
459:     var clocktowerDemonAttackDraftTarget by remember { mutableStateOf<String?>(null) }
460:     var clocktowerPendingNightDeath by remember { mutableStateOf<String?>(null) }
461:     var clocktowerSelectedExecution by remember { mutableStateOf<String?>(null) }
462:     var clocktowerPoisonTarget by remember { mutableStateOf<String?>(null) }
463:     // A target is provisional while the Poisoner is still awake. It becomes a
464:     // mechanical fact only when the night step is advanced.
465:     var clocktowerConfirmedPoisonTarget by remember { mutableStateOf<String?>(null) }
466:     var clocktowerFortuneTellerFirst by remember { mutableStateOf<String?>(null) }
467:     var clocktowerFortuneTellerSecond by remember { mutableStateOf<String?>(null) }
468:     var clocktowerChambermaidFirst by remember { mutableStateOf<String?>(null) }
469:     var clocktowerChambermaidSecond by remember { mutableStateOf<String?>(null) }
470:     var clocktowerRavenkeeperTarget by remember { mutableStateOf<String?>(null) }
471:     var clocktowerRedHerring by remember { mutableStateOf<String?>(null) }
472:     var clocktowerRecommendedDemonBluffRoleNames by remember { mutableStateOf<List<String>>(emptyList()) }
473:     var clocktowerRecommendedDrunkInvestigatorRoleName by remember { mutableStateOf<String?>(null) }
474:     var clocktowerRecommendedDrunkInvestigatorSeats by remember { mutableStateOf<List<Int>>(emptyList()) }
475:     var clocktowerButlerMaster by remember { mutableStateOf<String?>(null) }
476:     var clocktowerMonkProtectedTarget by remember { mutableStateOf<String?>(null) }
477:     var clocktowerConfirmedMonkProtectedTarget by remember { mutableStateOf<String?>(null) }
478:     var clocktowerMayorRedirectTarget by remember { mutableStateOf<String?>(null) }
479:     var clocktowerConfirmedMayorRedirectTarget by remember { mutableStateOf<String?>(null) }
480:     var clocktowerPendingNewDemonName by remember { mutableStateOf<String?>(null) }
481:     var clocktowerPendingNightNewDemonIdentityName by remember { mutableStateOf<String?>(null) }
482:     var clocktowerDemonSuccessorTarget by remember { mutableStateOf<String?>(null) }
483:     var clocktowerConfirmedDemonSuccessorTarget by remember { mutableStateOf<String?>(null) }
484:     fun clearConfirmedDemonSuccessorTarget() {
485:         clocktowerConfirmedDemonSuccessorTarget = null
486:     }
487:     var clocktowerVirginUsed by remember { mutableStateOf(false) }
488:     var clocktowerSlayerUsed by remember { mutableStateOf(false) }
489:     var clocktowerSlayerClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
490:     var clocktowerArtistUsed by remember { mutableStateOf(false) }
491:     var clocktowerArtistClaimedNames by remember { mutableStateOf<List<String>>(emptyList()) }
492:     var clocktowerLastExecutedName by remember { mutableStateOf<String?>(null) }
493:     var clocktowerPendingKlutzName by remember { mutableStateOf<String?>(null) }
494:     var clocktowerKlutzChoiceName by remember { mutableStateOf<String?>(null) }
495:     var clocktowerKlutzReturnToDawn by remember { mutableStateOf(false) }
496:     var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
497:     var clocktowerGameSession by remember { mutableStateOf<ClocktowerGameSession?>(null) }
498:     var clocktowerSessionView by remember { mutableStateOf<ClocktowerSessionView?>(null) }
499:     val currentClocktowerScript = clocktowerSessionView?.scriptId
500:         ?.let { scriptId ->
501:             ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
502:         }
503:         ?: ClocktowerScript.TroubleBrewing
504:     val clocktowerGameId = clocktowerSessionView?.gameId.orEmpty()
505:     val clocktowerGameSeed = clocktowerSessionView?.gameSeed ?: 0L
506:     val clocktowerGameStateRevision = clocktowerSessionView?.gameStateRevision ?: 0L
507:     val clocktowerPlayerInputRevision = clocktowerSessionView?.playerInputRevision ?: 0L
508:     val clocktowerSemanticHistoryMode =
509:         clocktowerSessionView?.semanticHistoryMode ?: ClocktowerSemanticHistoryMode.LEGACY_LOCAL
510:     val clocktowerNextTimelineGlobalSequence =
511:         clocktowerSessionView?.nextTimelineGlobalSequence ?: 0L
512:     val clocktowerActionTimeline = clocktowerSessionView?.actionTimeline ?: ActionFactTimeline()
513:     val clocktowerEpistemicObservations =
514:         clocktowerSessionView?.epistemicObservationLog?.records.orEmpty()
515:     var committedClocktowerSetup by remember { mutableStateOf<CommittedClocktowerSetup?>(null) }
516:     var committedTroubleBrewingSetupRotationRecord by remember {
517:         mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
518:     }
519:     var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
520:     var clocktowerRulesetRoleIds by remember { mutableStateOf<Set<RoleId>>(emptySet()) }
521:     var showResults by remember { mutableStateOf(false) }
522:     var gameOutcome by remember { mutableStateOf<GameOutcome?>(null) }
523:     var newCommonPlayerName by remember { mutableStateOf("") }
524:     val commonPlayers = remember { mutableStateListOf<String>().apply { addAll(baseContext.loadCommonPlayers()) } }
525:     val playerNames = remember { mutableStateListOf<String>() }
526:     var hostSeatingSetupFlow by remember { mutableStateOf(HostSeatingSetupFlow()) }
527:     val cards = remember { mutableStateListOf<PlayerCard>() }
528:     val records = remember { mutableStateListOf<EliminationRecord>() }
529:     val recoveryWriteGate = remember { RecoveryWriteGate() }
530:     val clocktowerEvents = remember { mutableStateListOf<ClocktowerEvent>() }
531:     var clocktowerEventCounter by remember { mutableStateOf(0) }
532:     val clocktowerNightStartedState = remember { mutableStateOf(false) }
533:     val clocktowerNightStepIndexState = remember { mutableStateOf(0) }
534:     val clocktowerDayModeState = remember { mutableStateOf(ClocktowerDayMode.Overview) }
535:     val clocktowerGhostVoteAuthorityState = remember { mutableStateOf(ClocktowerGhostVoteAuthority()) }
536:     val clocktowerHighestVoteNameState = remember { mutableStateOf<String?>(null) }
537:     val clocktowerHighestVoteCountState = remember { mutableStateOf(0) }
538:     val troubleBrewingSetupRecommendationScope = rememberCoroutineScope()
539:     val troubleBrewingSetupRecommendationPrewarmer = remember {
540:         val recommendationCoordinator = ClocktowerRecommendationCoordinator()
541:         TroubleBrewingSetupRecommendationPrewarmCoordinator { request ->
542:             recommendationCoordinator.recommendSetup(request)
543:         }
544:     }
545:     val troubleBrewingSetupRecommendationRevealCoordinator =
546:         remember(troubleBrewingSetupRecommendationPrewarmer) {
547:             TroubleBrewingSetupRecommendationRevealCoordinator(
548:                 prewarmer = troubleBrewingSetupRecommendationPrewarmer,
549:             )
550:         }
551:     val troubleBrewingFirstNightPrecomputeScope = rememberCoroutineScope()
552:     val troubleBrewingFirstNightPrecomputeCoordinator = remember {
553:         val recommendationCoordinator = ClocktowerRecommendationCoordinator()
554:         TroubleBrewingFirstNightPrecomputeCoordinator<GameState, List<DecisionCandidate<SetupClueOutcome>>> { request ->
555:             recommendationCoordinator.naturalPairCandidates(request)
556:         }
557:     }
558:     val a4ShadowWorldSetCache = remember { A4ShadowWorldSetCache() }
559:     val a4IdentityRevealPrewarmer = remember(a4ShadowWorldSetCache) {
560:         A4IdentityRevealPrewarmCoordinator(cache = a4ShadowWorldSetCache)
561:     }
562:     val a4ObservationCacheRebuildExecutor = remember(a4ShadowWorldSetCache) {
563:         A4ObservationCacheRebuildExecutor(a4ShadowWorldSetCache)
564:     }
565:     val a4ObservationDurabilityGate = remember(clocktowerGameId) { A4ObservationDurabilityGate() }
566:     var a4ObservationCacheRebuildRequest by remember { mutableStateOf<A4ObservationCacheRebuildRequest?>(null) }
567:     val a4ShadowLifecycleInvalidator = remember(a4ShadowWorldSetCache, a4ObservationDurabilityGate) {
568:         A4ShadowLifecycleInvalidator(
569:             invalidateGame = a4ShadowWorldSetCache::invalidateGame,
570:             clearPendingObservation = a4ObservationDurabilityGate::clear,
571:             cancelObservationRebuild = { a4ObservationCacheRebuildRequest = null },
572:         )
573:     }
574: 
575:     fun invalidateA4RevisionScope() {
576:         a4ShadowLifecycleInvalidator.revisionSuperseded(clocktowerGameId)
577:     }
578: 
579:     fun invalidateA4SessionBoundary() {
580:         a4ShadowLifecycleInvalidator.sessionBoundary(clocktowerGameId)
581:     }
582: 
583:     fun publishClocktowerSessionView() {
584:         clocktowerSessionView = clocktowerGameSession?.view
585:     }
586: 
587:     fun requireClocktowerGameSession(): ClocktowerGameSession =
588:         requireNotNull(clocktowerGameSession) {
589:             "Clocktower session authority is unavailable."
590:         }
591: 
592:     fun advanceClocktowerGameStateRevision() {
593:         requireClocktowerGameSession().advanceGameStateRevision()
594:         publishClocktowerSessionView()
595:         invalidateA4RevisionScope()
596:     }
597: 
598:     fun advanceClocktowerPlayerInputRevision() {
599:         requireClocktowerGameSession().recordPlayerInput()
600:         publishClocktowerSessionView()
601:         invalidateA4RevisionScope()
602:     }
603:     val playerCount = playerNames.size
604: 
605:     fun newClocktowerSeed(): Long = UUID.randomUUID().let { uuid ->
606:         (uuid.mostSignificantBits xor uuid.leastSignificantBits).takeIf { it != 0L } ?: 1L
607:     }
608: 
609:     fun troubleBrewingRulesetKnowledge() = runCatching {
610:         val json = baseContext.assets
611:             .open("rules/trouble_brewing.json")
612:             .bufferedReader(Charsets.UTF_8)
613:             .use { it.readText() }
614:         RulesetJsonLoader.parse(json)
615:     }.getOrNull()
616: 
617:     fun troubleBrewingRulesetRefFor(basis: ClocktowerRulesetPersistenceBasis): RulesetRef? {
618:         val knowledge = troubleBrewingRulesetKnowledge() ?: return null
619:         return runCatching { TroubleBrewingRulesetPersistence.refFor(knowledge, basis) }.getOrNull()
620:     }
621: 
622:     fun a4InitialIdentityPrewarmRequestOrNull(): A4IdentityRevealPrewarmRequest? {
623:         val activeRuleset = clocktowerRulesetRef ?: return null
624:         if (!BuildConfig.DEBUG || currentGameKind != GameKind.Clocktower ||
625:             currentClocktowerScript != ClocktowerScript.TroubleBrewing || cards.size != 5 ||
626:             cards.any { it.clocktowerRole == null || it.clocktowerShownRole == null }
627:         ) return null
628:         val gameState = cards.toClocktowerGameState(
629:             currentClocktowerScript,
630:             clocktowerGameSeed,
631:             poisonedPlayerName = null,
632:         )
633:         val snapshot = GameSnapshot(
634:             gameId = clocktowerGameId,
635:             gameStateRevision = clocktowerGameStateRevision,
636:             playerInputRevision = clocktowerPlayerInputRevision,
637:             gameSeed = clocktowerGameSeed,
638:             rulesetRef = activeRuleset,
639:             gameState = gameState,
640:         )
641:         val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, round = 1)
642:         val perceivedRolesBySeat = cards.mapIndexed { index, card ->
643:             index + 1 to RoleId(requireNotNull(card.clocktowerShownRole).enName)
644:         }.toMap()
645:         return A4IdentityRevealPrewarmRequest(
646:             formal = formal,
647:             playerInputRevision = clocktowerPlayerInputRevision,
648:             knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
649:                 formal = formal,
650:                 perceivedRolesBySeat = perceivedRolesBySeat,
651:                 observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
652:             ).associateBy(PlayerKnowledgeSnapshot::recipientSeat),
653:             revealOrder = cards.indices.map { it + 1 },
654:             hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
655:             roleDefinitions = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
656:         )
657:     }
658: 
659:     fun a4ObservationCacheRebuildRequestOrNull(recordId: String): A4ObservationCacheRebuildRequest? {
660:         val activeRuleset = clocktowerRulesetRef ?: return null
661:         if (!BuildConfig.DEBUG || currentGameKind != GameKind.Clocktower ||
662:             currentClocktowerScript != ClocktowerScript.TroubleBrewing || cards.size != 5 ||
663:             cards.any { it.clocktowerRole == null || it.clocktowerShownRole == null }
664:         ) return null
665:         val record = clocktowerEpistemicObservations.singleOrNull { it.recordId == recordId } ?: return null
666:         val gameState = cards.toClocktowerGameState(currentClocktowerScript, clocktowerGameSeed, poisonedPlayerName = null)
667:         val snapshot = GameSnapshot(
668:             gameId = clocktowerGameId,
669:             gameStateRevision = clocktowerGameStateRevision,
670:             playerInputRevision = clocktowerPlayerInputRevision,
671:             gameSeed = clocktowerGameSeed,
672:             rulesetRef = activeRuleset,
673:             gameState = gameState,
674:         )
675:         val formal = FormalGameState.from(snapshot, record.phase, record.round)
676:         return A4ObservationCacheRebuildRequest(
677:             formal = formal,
678:             playerInputRevision = clocktowerPlayerInputRevision,
679:             perceivedRolesBySeat = cards.mapIndexed { index, card ->
680:                 index + 1 to RoleId(requireNotNull(card.clocktowerShownRole).enName)
681:             }.toMap(),
682:             observationLog = EpistemicObservationLog(clocktowerEpistemicObservations.toList()),
683:             appendedRecordId = recordId,
684:             hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
685:             roleDefinitions = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
686:             rollout = A4WorldEngineRollout.ZDD_SHADOW,
687:         )
688:     }
689: 
690:     val identityRevealActive = screen == Screen.PassPhone || screen == Screen.RevealCard
691:     val identityRevealAssignmentFingerprint = cards.joinToString("|") { card ->
692:         "${card.clocktowerRole?.enName}:${card.clocktowerShownRole?.enName}"
693:     }
694:     LaunchedEffect(
695:         identityRevealActive,
696:         currentGameKind,
697:         currentClocktowerScript,
698:         clocktowerGameId,
699:         clocktowerGameStateRevision,
700:         clocktowerPlayerInputRevision,
701:         clocktowerRulesetRef,
702:         identityRevealAssignmentFingerprint,
703:     ) {
704:         val eligible = BuildConfig.DEBUG && identityRevealActive &&
705:             currentGameKind == GameKind.Clocktower &&
706:             currentClocktowerScript == ClocktowerScript.TroubleBrewing &&
707:             cards.size == 5 && clocktowerRulesetRef != null &&
708:             cards.all { it.clocktowerRole != null && it.clocktowerShownRole != null }
709:         if (!eligible) return@LaunchedEffect
710:         val request = a4InitialIdentityPrewarmRequestOrNull() ?: return@LaunchedEffect
711:         val session = a4IdentityRevealPrewarmer.start(request)
712:         val frameTelemetry = A4MainThreadFrameTelemetry()
713:         val frameMonitor = launch {
714:             while (isActive) {
715:                 withFrameNanos(frameTelemetry::recordFrame)
716:             }
717:         }
718:         var completedReportLogged = false
719:         try {
720:             val report = withContext(Dispatchers.Default) {
721:                 a4IdentityRevealPrewarmer.run(
722:                     session = session,
723:                     prioritizedRecipientSeat = currentDealIndex + 1,
724:                 )
725:             }
726:             Log.i(A4_IDENTITY_PREWARM_LOG_TAG, report.toLogLine(frameTelemetry.summary()))
727:             completedReportLogged = true
728:         } finally {
729:             frameMonitor.cancel()
730:             val cancellation = a4IdentityRevealPrewarmer.cancel(session)
731:             if (cancellation.cancelledEntries > 0) {
732:                 Log.i(A4_IDENTITY_PREWARM_LOG_TAG, cancellation.toLogLine())
733:             }
734:             if (!completedReportLogged) {
735:                 Log.i(
736:                     A4_IDENTITY_PREWARM_LOG_TAG,
737:                     a4IdentityRevealPrewarmer.report(session).toLogLine(frameTelemetry.summary()),
738:                 )
739:             }
740:         }
741:     }
742:     LaunchedEffect(a4ObservationCacheRebuildRequest) {
743:         val request = a4ObservationCacheRebuildRequest ?: return@LaunchedEffect
744:         val report = withContext(Dispatchers.Default) {
745:             val workerScope = this
746:             a4ObservationCacheRebuildExecutor.execute(request) { !workerScope.isActive }
747:         }
748:         Log.i(A4_OBSERVATION_CACHE_UPDATE_LOG_TAG, report.toLogLine(request))
749:     }
750:     var a4InitialRecommendationDemandRecorded by remember(clocktowerGameId) { mutableStateOf(false) }
751:     val recordA4InitialRecommendationDemand = demand@{
752:         if (a4InitialRecommendationDemandRecorded) return@demand
753:         val request = a4InitialIdentityPrewarmRequestOrNull() ?: return@demand
754:         val report = a4IdentityRevealPrewarmer.probe(request)
755:         Log.i(A4_IDENTITY_PREWARM_LOG_TAG, report.toLogLine())
756:         a4InitialRecommendationDemandRecorded = true
757:     }
758: 
759:     fun storytellerPhaseFor(phase: ClocktowerPhase = clocktowerPhase): StorytellerPhase =
760:         phase.toStorytellerPhase()
761: 
762:     fun clocktowerSeatFor(playerName: String): Int =
763:         cards.indexOfFirst { it.name == playerName }
764:             .takeIf { index -> index >= 0 }
765:             ?.plus(1)
766:             ?: error("Unknown Clocktower player '$playerName'.")
767: 
768:     fun currentClocktowerNightCheckpoint(): ClocktowerNightCheckpoint = ClocktowerNightCheckpoint(
769:         phaseName = clocktowerPhase.name,
770:         round = round,
771:         gameStateRevision = clocktowerGameStateRevision,
772:         playerInputRevision = clocktowerPlayerInputRevision,
773:         nightStarted = clocktowerNightStartedState.value,
774:         nightStepIndex = clocktowerNightStepIndexState.value,
775:         confirmedAttackTarget = clocktowerPendingNightDeath,
776:         attackDraftTarget = clocktowerDemonAttackDraftTarget,
777:         confirmedPoisonTarget = clocktowerConfirmedPoisonTarget,
778:         poisonDraftTarget = clocktowerPoisonTarget,
779:         confirmedMonkTarget = clocktowerConfirmedMonkProtectedTarget,
780:         monkDraftTarget = clocktowerMonkProtectedTarget,
781:         confirmedMayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
782:         mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
783:         pendingNewDemonName = clocktowerPendingNewDemonName,
784:         pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
785:         demonSuccessorDraftTarget = clocktowerDemonSuccessorTarget,
786:         confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget,
787:         nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
788:     )
789: 
790:     fun clocktowerActionId(
791:         kind: String,
792:         actionRound: Int = round,
793:         localSequence: Int = clocktowerEventCounter + 1,
794:         targetSeat: Int? = null,
795:     ): String = buildList {
796:         add(kind)
797:         add(clocktowerGameId)
798:         add(clocktowerGameStateRevision.toString())
799:         add(actionRound.toString())
800:         add(localSequence.toString())
801:         targetSeat?.let { add(it.toString()) }
802:     }.joinToString("-")
803: 
804:     fun recordClocktowerAction(draft: ActionFactDraft) {
805:         if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
806:         requireClocktowerGameSession().commitGlobalActionFact(draft)
807:         publishClocktowerSessionView()
808:     }
809: 
810:     fun materializeClocktowerPoisonExpiryAtDusk() {
811:         check(clocktowerPhase == ClocktowerPhase.Day) {
812:             "Clocktower poison expiry must materialize from the outgoing Day."
813:         }
814: 
815:         val currentPoisonTargetSeat =
816:             clocktowerConfirmedPoisonTarget?.let(::clocktowerSeatFor)
817: 
818:         val durablePreviousPoisonTargetSeat =
819:             currentPoisonTargetSeat
820:                 ?: DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(
821:                     actionTimeline = clocktowerActionTimeline,
822:                     round = round,
823:                 )
824: 
825:         val materialization = DuskPoisonExpiryMaterializationPlanner.plan(
826:             gameId = clocktowerGameId,
827:             round = round,
828:             previousTargetSeat = durablePreviousPoisonTargetSeat,
829:             state = DuskPoisonExpiryMaterializationState(
830:                 currentPoisonTargetSeat = currentPoisonTargetSeat,
831:                 committedActionIds = clocktowerActionTimeline.entries
832:                     .map { it.fact.actionId }
833:                     .toSet(),
834:             ),
835:         ) ?: return
836: 
837:         materialization.actionIdToCommit?.let { actionId ->
838:             val localSequence = clocktowerEventCounter + 1
839:             recordClocktowerAction(
840:                 ActionFactDraft.Poison(
841:                     actionId = actionId,
842:                     phase = storytellerPhaseFor(),
843:                     round = round,
844:                     sequence = localSequence,
845:                     targetSeat = null,
846:                 ),
847:             )
848:         }
849: 
850:         if (materialization.stateMutationRequired) {
851:             requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(targetSeat = null)
852:             publishClocktowerSessionView()
853:             clocktowerPoisonTarget = null
854:             clocktowerConfirmedPoisonTarget = null
855:         }
856:     }
857: 
858:     fun recordClocktowerPhaseAdvance(
859:         nextPhase: ClocktowerPhase,
860:         nextRound: Int = round,
861:     ) {
862:         if (nextPhase == clocktowerPhase && nextRound == round) return
863:         val localSequence = clocktowerEventCounter + 1
864:         recordClocktowerAction(ActionFactDraft.PhaseAdvance(
865:             actionId = clocktowerActionId(
866:                 kind = "phase-${nextPhase.name.lowercase()}-$nextRound",
867:                 actionRound = round,
868:                 localSequence = localSequence,
869:             ),
870:             phase = storytellerPhaseFor(clocktowerPhase),
871:             round = round,
872:             sequence = localSequence,
873:             nextPhase = storytellerPhaseFor(nextPhase),
874:             nextRound = nextRound,
875:         ))
876:     }
877: 
878:     fun recordEpistemicObservation(draft: EpistemicObservationDraft) {
879:         val session = requireClocktowerGameSession()
880:         when (clocktowerSemanticHistoryMode) {
881:             ClocktowerSemanticHistoryMode.LEGACY_LOCAL -> {
882:                 if (clocktowerEpistemicObservations.any { it.recordId == draft.recordId }) return
883:                 session.recordEpistemicObservation(draft.bindLegacyLocal())
884:                 publishClocktowerSessionView()
885:                 invalidateA4RevisionScope()
886:                 a4ObservationDurabilityGate.markPending(draft.recordId)
887:             }
888:             ClocktowerSemanticHistoryMode.GLOBAL_V1 -> {
889:                 val beforeRevision = clocktowerPlayerInputRevision
890:                 val committed = session.commitGlobalEpistemicObservation(draft)
891:                 if (session.view.playerInputRevision == beforeRevision) return
892:                 publishClocktowerSessionView()
893:                 invalidateA4RevisionScope()
894:                 a4ObservationDurabilityGate.markPending(committed.recordId)
895:             }
896:         }
897:     }
898: 
899:     suspend fun evaluateSdeRuntimeShadow(model: StructuredNumberInformationUiModel) {
900:         if (!BuildConfig.DEBUG || currentClocktowerScript != ClocktowerScript.TroubleBrewing) return
901:         val session = clocktowerGameSession ?: return
902:         val setup = committedClocktowerSetup ?: return
903:         val rulesetRef = clocktowerRulesetRef ?: return
904:         val replayInput = runCatching {
905:             SdeHistoricalReplayInputFactory.captureFresh(
906:                 committedSetup = setup,
907:                 currentSnapshot = session.toGameSnapshot(rulesetRef),
908:             ).input
909:         }.getOrElse { failure ->
910:             Log.w(SDE_RUNTIME_SHADOW_LOG_TAG, "capture_failed:${failure::class.java.simpleName}")
911:             return
912:         }
913:         val report = SdeRuntimeShadowCoordinator.evaluate(
914:             replayInput = replayInput,
915:             decisionContext = model.shadowDecisionContext,
916:             validatedRuleset = activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript),
917:             roleDefinitions = clocktowerRoleDefinitionsForScript(currentClocktowerScript),
918:             currentIdentity = {
919:                 clocktowerGameSession?.view?.let { view ->
920:                     SdeRuntimeShadowIdentity(
921:                         requestIdentity = model.shadowDecisionContext.requestIdentity,
922:                         gameStateRevision = view.gameStateRevision,
923:                         playerInputRevision = view.playerInputRevision,
924:                         nextTimelineGlobalSequence = view.nextTimelineGlobalSequence,
925:                     ).takeIf { it.gameId == view.gameId }
926:                 }
927:             },
928:             appendTrace = decisionTraceArchiveStore::append,
929:         )
930:         Log.i(
931:             SDE_RUNTIME_SHADOW_LOG_TAG,
932:             "outcome=${report.outcome} elapsedMs=${report.elapsedMillis} " +
933:                 "heapDeltaBytes=${report.coarseHeapDeltaBytes} failure=${report.failureType}",
934:         )
935:     }
936: 
937:     fun commitConfirmedInformationDecision(confirmed: ConfirmedInformationDecision) {
938:         if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) {
939:             recordEpistemicObservation(confirmed.draft)
940:             return
941:         }
942:         val session = requireClocktowerGameSession()
943:         val beforeRevision = session.view.playerInputRevision
944:         val committed = session.commitGlobalEpistemicObservation(confirmed.draft)
945:         if (session.view.playerInputRevision == beforeRevision) return
946:         publishClocktowerSessionView()
947:         invalidateA4RevisionScope()
948:         a4ObservationDurabilityGate.markPending(committed.recordId)
949: 
950:         val correlation = SdePostCommitCorrelationCoordinator.correlate(
951:             store = decisionTraceArchiveStore,
952:             confirmed = confirmed,
953:             committedObservation = committed,
954:             postCommitSession = session.view,
955:         )
956:         if (!correlation.completed) {
957:             Log.w(SDE_RUNTIME_SHADOW_LOG_TAG, "correlation_failed:${correlation.failureType}")
958:         }
959:     }
960: 
961:     fun preflightClocktowerPublicAliveObservation(
962:         playerName: String,
963:         eventSequence: Int,
964:         eventPhase: ClocktowerPhase = clocktowerPhase,
965:         eventRound: Int = round,
966:         recordId: String? = null,
967:     ) {
968:         if (clocktowerSemanticHistoryMode != ClocktowerSemanticHistoryMode.GLOBAL_V1) return
969:         val seat = cards.indexOfFirst { it.name == playerName }
970:             .takeIf { index -> index >= 0 }
971:             ?.plus(1)
972:             ?: return
973:         val epistemicPhase = eventPhase.toStorytellerPhase()
974:         val committed = requireClocktowerGameSession().preflightGlobalEpistemicObservation(
975:             EpistemicObservationDraft(
976:                 recordId = recordId ?: "public-alive-${clocktowerGameId}-${eventSequence}-$seat",
977:                 phase = epistemicPhase,
978:                 round = eventRound,
979:                 sequence = eventSequence,
980:                 sourceSeat = null,
981:                 sourceAbility = null,
982:                 visibility = ObservationVisibility.PUBLIC,
983:                 recipientSeats = emptySet(),
984:                 reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
985:                 proposition = InformationProposition.AliveAt(seat, false),
986:             ),
987:         )
988:         check(committed.playerInputRevision != clocktowerPlayerInputRevision) {
989:             "A new public elimination cannot reuse an existing observation ID."
990:         }
991:     }
992: 
993:     fun nextNightPublicAliveObservationPreflightOrNull(): Pair<String, Int>? {
994:         val originalDeathName = clocktowerPendingNightDeath
995:         val dawnDeathFacts = resolveTroubleBrewingDawnDeathFacts(
996:             cards = cards,
997:             targetName = originalDeathName,
998:             poisonedPlayerName = clocktowerConfirmedPoisonTarget,
999:             monkProtectedTargetName = clocktowerConfirmedMonkProtectedTarget,
1000:         )
1001:         val baseGameState = cards.toClocktowerGameState(
1002:             currentClocktowerScript,
1003:             clocktowerGameSeed,
1004:             poisonedPlayerName = clocktowerConfirmedPoisonTarget,
1005:         )
1006:         val effectiveNightState = ClocktowerEffectiveNightState(
1007:             effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
1008:                 (index + 1).takeIf { card.eliminatedRound == null }
1009:             }.toSet(),
1010:             effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
1011:                 card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
1012:             }.toMap(),
1013:         )
1014:         val demonRoleIds = cards.mapNotNull { card ->
1015:             card.clocktowerRole
1016:                 ?.takeIf { card.clocktowerTeam == ClocktowerTeam.Demon }
1017:                 ?.let { role -> RoleId(role.enName) }
1018:         }.toSet()
1019:         val deathTransition = NightDawnResolutionPlanner.planValidatedNightDeath(
1020:             baseGameState = baseGameState,
1021:             checkpoint = currentClocktowerNightCheckpoint(),
1022:             input = NightDawnDeathResolutionInput(
1023:                 originalDeathSeat = dawnDeathFacts.originalDeathSeat,
1024:                 mayorSeat = dawnDeathFacts.mayorSeat,
1025:                 mayorRedirectMayApply = dawnDeathFacts.mayorSeat != null,
1026:                 attackOutcome = dawnDeathFacts.attackOutcome,
1027:                 demonSafeSeats = dawnDeathFacts.demonSafeSeats,
1028:                 effectiveNightState = effectiveNightState,
1029:                 demonRoleIds = demonRoleIds,
1030:             ),
1031:         )
1032:         val resolvedDeathSeat = deathTransition.dawnCommitIntent?.death?.targetSeat ?: return null
1033:         val resolvedDeathName = cards.getOrNull(resolvedDeathSeat - 1)?.name ?: return null
1034:         val eventOffset =
1035:             if (
1036:                 dawnDeathFacts.mayorSeat != null &&
1037:                 dawnDeathFacts.originalDeathSeat != null &&
1038:                 resolvedDeathSeat != dawnDeathFacts.originalDeathSeat
1039:             ) {
1040:                 2
1041:             } else {
1042:                 1
1043:             }
1044:         return resolvedDeathName to (clocktowerEventCounter + eventOffset)
1045:     }
1046: 
1047:     fun addClocktowerEvent(
1048:         type: ClocktowerEventType,
1049:         title: String,
1050:         detail: String,
1051:         playerNames: List<String> = emptyList(),
1052:         eventPhase: ClocktowerPhase = clocktowerPhase,
1053:         eventRound: Int = round,
1054:         projectSemanticHistory: Boolean = true,
1055:     ) {
1056:         advanceClocktowerGameStateRevision()
1057:         clocktowerEventCounter += 1
1058:         clocktowerEvents.add(
1059:             ClocktowerEvent(
1060:                 sequence = clocktowerEventCounter,
1061:                 type = type,
1062:                 title = title,
1063:                 detail = detail,
1064:                 playerNames = playerNames.distinct(),
1065:                 phase = eventPhase,
1066:                 round = eventRound,
1067:             ),
1068:         )
1069:         if (!projectSemanticHistory) return
1070:         if (type !in setOf(ClocktowerEventType.Death, ClocktowerEventType.Execution)) return
1071:         val eliminatedSeats = playerNames.mapNotNull { playerName ->
1072:             cards.indexOfFirst { it.name == playerName }
1073:                 .takeIf { index -> index >= 0 && cards[index].eliminatedRound != null }
1074:                 ?.plus(1)
1075:         }.distinct()
1076:         if (eliminatedSeats.isEmpty()) return
1077:         val epistemicPhase = storytellerPhaseFor(eventPhase)
1078:         eliminatedSeats.forEach { seat ->
1079:             when (type) {
1080:                 ClocktowerEventType.Execution -> recordClocktowerAction(ActionFactDraft.Execution(
1081:                     actionId = clocktowerActionId(
1082:                         kind = "execution",
1083:                         actionRound = eventRound,
1084:                         localSequence = clocktowerEventCounter,
1085:                         targetSeat = seat,
1086:                     ),
1087:                     phase = epistemicPhase,
1088:                     round = eventRound,
1089:                     sequence = clocktowerEventCounter,
1090:                     targetSeat = seat,
1091:                 ))
1092:                 ClocktowerEventType.Death -> recordClocktowerAction(ActionFactDraft.Death(
1093:                     actionId = clocktowerActionId(
1094:                         kind = "death",
1095:                         actionRound = eventRound,
1096:                         localSequence = clocktowerEventCounter,
1097:                         targetSeat = seat,
1098:                     ),
1099:                     phase = epistemicPhase,
1100:                     round = eventRound,
1101:                     sequence = clocktowerEventCounter,
1102:                     targetSeat = seat,
1103:                 ))
1104:                 else -> Unit
1105:             }
1106:         }
1107:         eliminatedSeats.forEach { seat ->
1108:             val observationId = "public-alive-${clocktowerGameId}-${clocktowerEventCounter}-$seat"
1109:             recordEpistemicObservation(EpistemicObservationDraft(
1110:                 recordId = observationId,
1111:                 phase = epistemicPhase,
1112:                 round = eventRound,
1113:                 sequence = clocktowerEventCounter,
1114:                 sourceSeat = null,
1115:                 sourceAbility = null,
1116:                 visibility = ObservationVisibility.PUBLIC,
1117:                 recipientSeats = emptySet(),
1118:                 reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
1119:                 proposition = InformationProposition.AliveAt(seat, false),
1120:             ))
1121:         }
1122:     }
1123: 
1124:     fun localizedText(zh: String, en: String): String = if (language == "en") en else zh
1125: 
1126:     fun addOutcomeEvent(outcome: GameOutcome?) {
1127:         if (outcome == null || clocktowerEvents.lastOrNull()?.type == ClocktowerEventType.GameEnd) return
1128:         addClocktowerEvent(
1129:             type = ClocktowerEventType.GameEnd,
1130:             title = outcome.title,
1131:             detail = listOf(outcome.summary, outcome.reason).filter { it.isNotBlank() }.joinToString(" · "),
1132:         )
1133:     }
1134: 
1135:     fun resetClocktowerNightFlow() {
1136:         clocktowerNightStartedState.value = false
1137:         clocktowerNightStepIndexState.value = 0
1138:     }
1139: 
1140:     fun resetClocktowerDayFlow() {
1141:         clocktowerDayModeState.value = ClocktowerDayMode.Overview
1142:         clocktowerHighestVoteNameState.value = null
1143:         clocktowerHighestVoteCountState.value = 0
1144:     }
1145: 
1146:     fun resetClocktowerFlow() {
1147:         resetClocktowerNightFlow()
1148:         resetClocktowerDayFlow()
1149:     }
1150: 
1151:     fun clearSavedGameState() {
1152:         baseContext.clearActiveGameState()
1153:         recoveryWriteGate.clear()
1154:         savedGamePreview = null
1155:     }
1156: 
1157:     fun localizedRestoredCard(card: PlayerCard): PlayerCard {
1158:         if (card.clocktowerRole == null || card.clocktowerShownRole == null) return card
1159:         return card.copy(
1160:             roleLabel = card.clocktowerShownRole.nameFor(language),
1161:             actualRoleLabel = card.clocktowerRole.nameFor(language),
1162:             word = context.getString(
1163:                 R.string.clocktower_card_desc_format,
1164:                 card.clocktowerShownRole.team.label(context),
1165:                 card.clocktowerShownRole.descriptionFor(language),
1166:             ),
1167:         )
1168:     }
1169: 
1170:     fun activeGameRecoverySnapshot(): RecoverySnapshot {
1171:         when (currentGameKind) {
1172:             GameKind.Undercover -> Unit
1173:             GameKind.Clocktower -> {
1174:                 ClocktowerActiveSessionValidator.validateForRecoverySave(
1175:                     script = activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript).script,
1176:                     assignedRoleIds = cards.map { card ->
1177:                         RoleId(requireNotNull(card.clocktowerRole) {
1178:                             "Clocktower recovery save is missing an assigned role."
1179:                         }.enName)
1180:                     },
1181:                 )
1182:             }
1183:             GameKind.Werewolf -> error("Werewolf runtime has been removed.")
1184:         }
1185:         val entryPoint = when (screen) {
1186:             Screen.PassPhone -> RecoveryEntryPoint.PassPhone
1187:             Screen.RevealCard -> RecoveryEntryPoint.RevealCard
1188:             else -> RecoveryEntryPoint.Stable
1189:         }
1190:         val commonCards = cards.toList()
1191:         val commonRecords = records.toList()
1192:         val recoveryGame: RecoveryGame = when (currentGameKind) {
1193:             GameKind.Undercover -> UndercoverRecovery(
1194:                 entryPoint = entryPoint,
1195:                 currentDealIndex = currentDealIndex,
1196:                 round = round,
1197:                 cards = commonCards,
1198:                 records = commonRecords,
1199:                 outcome = gameOutcome,
1200:                 undercoverCount = undercoverCount,
1201:                 includeBlank = includeBlank,
1202:                 lastWordsMode = lastWordsMode,
1203:             )
1204:             GameKind.Werewolf -> error("Werewolf runtime has been removed.")
1205:             GameKind.Clocktower -> ClocktowerRecovery(
1206:                 entryPoint = entryPoint,
1207:                 currentDealIndex = currentDealIndex,
1208:                 round = round,
1209:                 cards = commonCards,
1210:                 records = commonRecords,
1211:                 outcome = gameOutcome,
1212:                 identity = ClocktowerRecoveryIdentity(
1213:                     script = currentClocktowerScript,
1214:                     gameId = clocktowerGameId,
1215:                     gameSeed = clocktowerGameSeed,
1216:                 ),
1217:                 troubleBrewingSetupRotationRecord = committedTroubleBrewingSetupRotationRecord,
1218:                 position = ClocktowerRecoveryPosition(
1219:                     phase = clocktowerPhase,
1220:                     nightStarted = clocktowerNightStartedState.value,
1221:                     nightStepIndex = clocktowerNightStepIndexState.value,
1222:                 ),
1223:                 mechanics = ClocktowerRecoveryMechanics(
1224:                     confirmedAttackTarget = clocktowerPendingNightDeath,
1225:                     confirmedPoisonTarget = clocktowerConfirmedPoisonTarget,
1226:                     confirmedMonkProtectedTarget = clocktowerConfirmedMonkProtectedTarget,
1227:                     confirmedMayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
1228:                     pendingNewDemonName = clocktowerPendingNewDemonName,
1229:                     pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
1230:                     confirmedDemonSuccessorTarget = clocktowerConfirmedDemonSuccessorTarget,
1231:                     redHerring = clocktowerRedHerring,
1232:                     demonBluffRoleNames = clocktowerRecommendedDemonBluffRoleNames.toList(),
1233:                     butlerMaster = clocktowerButlerMaster,
1234:                     virginUsed = clocktowerVirginUsed,
1235:                     slayerUsed = clocktowerSlayerUsed,
1236:                     slayerClaimedNames = clocktowerSlayerClaimedNames.toList(),
1237:                     artistUsed = clocktowerArtistUsed,
1238:                     artistClaimedNames = clocktowerArtistClaimedNames.toList(),
1239:                     lastExecutedName = clocktowerLastExecutedName,
1240:                     pendingKlutzName = clocktowerPendingKlutzName,
1241:                     klutzChoiceName = clocktowerKlutzChoiceName,
1242:                     klutzReturnToDawn = clocktowerKlutzReturnToDawn,
1243:                     ghostVoteAuthority = clocktowerGhostVoteAuthorityState.value,
1244:                     highestVoteName = clocktowerHighestVoteNameState.value,
1245:                     highestVoteCount = clocktowerHighestVoteCountState.value,
1246:                 ),
1247:                 history = ClocktowerRecoveryHistory(
1248:                     gameStateRevision = clocktowerGameStateRevision,
1249:                     playerInputRevision = clocktowerPlayerInputRevision,
1250:                     semanticHistoryMode = clocktowerSemanticHistoryMode,
1251:                     actionTimeline = clocktowerActionTimeline,
1252:                     nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
1253:                     events = clocktowerEvents.toList(),
1254:                     epistemicObservations = clocktowerEpistemicObservations.toList(),
1255:                 ),
1256:             )
1257:         }
1258:         return RecoverySnapshot(
1259:             compatibilityToken = RecoveryCompatibilityToken.currentFor(currentGameKind),
1260:             savedAtMillis = System.currentTimeMillis(),
1261:             game = recoveryGame,
1262:         )
1263:     }
1264: 
1265:     fun persistActiveGameStateIfNeeded(force: Boolean = false): Boolean {
1266:         if (!screen.isActiveGameScreen() || cards.isEmpty()) return false
1267:         val snapshot = activeGameRecoverySnapshot()
1268:         return recoveryWriteGate.persist(snapshot, force = force) { durableSnapshot ->
1269:             baseContext.saveActiveGameState(RecoverySnapshotJsonCodec.encode(durableSnapshot))
1270:         }
1271:     }
1272: 
1273:     fun persistAndReleaseA4ObservationRebuildIfDurable(force: Boolean = false) {
1274:         val persisted = persistActiveGameStateIfNeeded(force = force)
1275:         val recordId = a4ObservationDurabilityGate.releaseAfterPersistence(persisted) ?: return
1276:         a4ObservationCacheRebuildRequest = a4ObservationCacheRebuildRequestOrNull(recordId)
1277:     }
1278: 
1279:     fun applyValidatedRecoveryPlan(plan: ValidatedRecoveryPlan) {
1280:         val game = plan.snapshot.game
1281:         val restoredCards = game.cards.map(::localizedRestoredCard)
1282:         val restoredPlayerNames = restoredCards.map(PlayerCard::name)
1283: 
1284:         playerNames.clear()
1285:         playerNames.addAll(restoredPlayerNames)
1286:         hostSeatingSetupFlow = HostSeatingSetupFlow.recoveredActiveGame(
1287:             playerNames = restoredPlayerNames,
1288:             game = game.gameKind,
1289:         )
1290:         cards.clear()
1291:         cards.addAll(restoredCards)
1292:         records.clear()
1293:         records.addAll(game.records)
1294: 
1295:         currentGameKind = game.gameKind
1296:         currentDealIndex = game.currentDealIndex
1297:         round = game.round
1298:         gameOutcome = game.outcome
1299:         showResults = plan.presentResults
1300:         savedGamePreview = null
1301:         showHostTools = false
1302:         showNewGameConfirmation = false
1303: 
1304:         undercoverCount = 1
1305:         includeBlank = false
1306:         lastWordsMode = LastWordsMode.FirstDay
1307:         selectedElimination = null
1308: 
1309:         selectedClocktowerScript = null
1310:         clocktowerGameSession = null
1311:         publishClocktowerSessionView()
1312:         committedClocktowerSetup = null
1313:         committedTroubleBrewingSetupRotationRecord = null
1314:         clocktowerRulesetRoleIds = emptySet()
1315:         clocktowerRulesetRef = null
1316:         clocktowerPhase = ClocktowerPhase.FirstNight
1317:         clocktowerNightStartedState.value = false
1318:         clocktowerNightStepIndexState.value = 0
1319: 
1320:         clocktowerEvents.clear()
1321:         clocktowerEventCounter = 0
1322: 
1323:         clocktowerPendingNightDeath = null
1324:         clocktowerDemonAttackDraftTarget = null
1325:         clocktowerSelectedExecution = null
1326:         clocktowerPoisonTarget = null
1327:         clocktowerConfirmedPoisonTarget = null
1328:         clocktowerFortuneTellerFirst = null
1329:         clocktowerFortuneTellerSecond = null
1330:         clocktowerChambermaidFirst = null
1331:         clocktowerChambermaidSecond = null
1332:         clocktowerRavenkeeperTarget = null
1333:         clocktowerRedHerring = null
1334:         clocktowerRecommendedDemonBluffRoleNames = emptyList()
1335:         clocktowerRecommendedDrunkInvestigatorRoleName = null
1336:         clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
1337:         clocktowerButlerMaster = null
1338:         clocktowerMonkProtectedTarget = null
1339:         clocktowerConfirmedMonkProtectedTarget = null
1340:         clocktowerMayorRedirectTarget = null
1341:         clocktowerConfirmedMayorRedirectTarget = null
1342:         clocktowerPendingNewDemonName = null
1343:         clocktowerPendingNightNewDemonIdentityName = null
1344:         clocktowerDemonSuccessorTarget = null
1345:         clocktowerConfirmedDemonSuccessorTarget = null
1346:         clocktowerVirginUsed = false
1347:         clocktowerSlayerUsed = false
1348:         clocktowerSlayerClaimedNames = emptyList()
1349:         clocktowerArtistUsed = false
1350:         clocktowerArtistClaimedNames = emptyList()
1351:         clocktowerLastExecutedName = null
1352:         clocktowerPendingKlutzName = null
1353:         clocktowerKlutzChoiceName = null
1354:         clocktowerKlutzReturnToDawn = false
1355: 
1356:         clocktowerDayModeState.value = ClocktowerDayMode.Overview
1357:         clocktowerGhostVoteAuthorityState.value = ClocktowerGhostVoteAuthority()
1358:         clocktowerHighestVoteNameState.value = null
1359:         clocktowerHighestVoteCountState.value = 0
1360: 
1361:         when (game) {
1362:             is UndercoverRecovery -> {
1363:                 undercoverCount = game.undercoverCount
1364:                 includeBlank = game.includeBlank
1365:                 lastWordsMode = game.lastWordsMode
1366:             }
1367:             is ClocktowerRecovery -> {
1368:                 val runtime = plan.clocktowerRuntime
1369:                 val safeClocktower = plan.safeReentry as? RecoverySafeReentry.ClocktowerJudge
1370:                 val mechanics = game.mechanics
1371:                 val history = game.history
1372: 
1373:                 committedTroubleBrewingSetupRotationRecord =
1374:                     game.troubleBrewingSetupRotationRecord
1375:                 val recoveredGameState = restoredCards.toClocktowerGameState(
1376:                     script = game.identity.script,
1377:                     seed = game.identity.gameSeed,
1378:                     poisonedPlayerName = mechanics.confirmedPoisonTarget,
1379:                 )
1380:                 clocktowerGameSession = ClocktowerGameSession.restoreProduction(
1381:                     ClocktowerSessionState(
1382:                         gameId = game.identity.gameId,
1383:                         gameStateRevision = history.gameStateRevision,
1384:                         playerInputRevision = history.playerInputRevision,
1385:                         gameSeed = game.identity.gameSeed,
1386:                         gameState = recoveredGameState,
1387:                         actionTimeline = history.actionTimeline,
1388:                         epistemicObservationLog = EpistemicObservationLog(history.epistemicObservations),
1389:                         semanticHistoryMode = history.semanticHistoryMode,
1390:                         nextTimelineGlobalSequence = history.nextTimelineGlobalSequence,
1391:                     ),
1392:                 )
1393:                 publishClocktowerSessionView()
1394:                 clocktowerRulesetRoleIds = if (game.identity.script == ClocktowerScript.TroubleBrewing) {
1395:                     runtime?.rulesetBasis?.roleIds.orEmpty()
1396:                 } else {
1397:                     emptySet()
1398:                 }
1399:                 clocktowerRulesetRef = runtime?.rulesetRef
1400:                 clocktowerPhase = safeClocktower?.phase ?: game.position.phase
1401:                 clocktowerNightStartedState.value = game.position.nightStarted
1402:                 clocktowerNightStepIndexState.value =
1403:                     safeClocktower?.nightStepIndex ?: game.position.nightStepIndex
1404: 
1405:                 clocktowerEvents.addAll(history.events)
1406:                 clocktowerEventCounter = history.events.maxOfOrNull(ClocktowerEvent::sequence) ?: 0
1407: 
1408:                 clocktowerPendingNightDeath = mechanics.confirmedAttackTarget
1409:                 clocktowerConfirmedPoisonTarget = mechanics.confirmedPoisonTarget
1410:                 clocktowerConfirmedMonkProtectedTarget = mechanics.confirmedMonkProtectedTarget
1411:                 clocktowerConfirmedMayorRedirectTarget = mechanics.confirmedMayorRedirectTarget
1412:                 clocktowerPendingNewDemonName = mechanics.pendingNewDemonName
1413:                 clocktowerPendingNightNewDemonIdentityName = mechanics.pendingNightNewDemonIdentityName
1414:                 clocktowerConfirmedDemonSuccessorTarget = mechanics.confirmedDemonSuccessorTarget
1415:                 clocktowerRedHerring = mechanics.redHerring
1416:                 clocktowerRecommendedDemonBluffRoleNames = mechanics.demonBluffRoleNames
1417:                 clocktowerButlerMaster = mechanics.butlerMaster
1418:                 clocktowerVirginUsed = mechanics.virginUsed
1419:                 clocktowerSlayerUsed = mechanics.slayerUsed
1420:                 clocktowerSlayerClaimedNames = mechanics.slayerClaimedNames
1421:                 clocktowerArtistUsed = mechanics.artistUsed
1422:                 clocktowerArtistClaimedNames = mechanics.artistClaimedNames
1423:                 clocktowerLastExecutedName = mechanics.lastExecutedName
1424:                 clocktowerPendingKlutzName = mechanics.pendingKlutzName
1425:                 clocktowerKlutzReturnToDawn = mechanics.klutzReturnToDawn
1426:                 clocktowerGhostVoteAuthorityState.value = mechanics.ghostVoteAuthority
1427:                 clocktowerHighestVoteNameState.value = mechanics.highestVoteName
1428:                 clocktowerHighestVoteCountState.value = mechanics.highestVoteCount
1429:                 clocktowerDayModeState.value =
1430:                     if (safeClocktower?.continuation == ClocktowerRecoveryContinuation.Klutz) {
1431:                         ClocktowerDayMode.Klutz
1432:                     } else {
1433:                         ClocktowerDayMode.Overview
1434:                     }
1435:             }
1436:             is WerewolfRecovery -> Unit
1437:         }
1438: 
1439:         screen = when (plan.safeReentry) {
1440:             RecoverySafeReentry.UndercoverGame -> Screen.Game
1441:             is RecoverySafeReentry.PassPhone -> Screen.PassPhone
1442:             is RecoverySafeReentry.RevealCard -> Screen.RevealCard
1443:             is RecoverySafeReentry.ClocktowerJudge -> Screen.ClocktowerJudge
1444:         }
1445:     }
1446: 
1447:     fun restoreSavedGame() {
1448:         RecoveryApplicationCoordinator.apply(
1449:             raw = baseContext.loadActiveGameStateJson(),
1450:             prepare = { raw -> baseContext.prepareCurrentRecoveryPlan(raw) },
1451:             clearRejected = ::clearSavedGameState,
1452:             crossSessionBoundary = ::invalidateA4SessionBoundary,
1453:             applyValidated = ::applyValidatedRecoveryPlan,
1454:         )
1455:     }
1456: 
1457:     val latestPersistActiveGameState by rememberUpdatedState { force: Boolean ->
1458:         persistAndReleaseA4ObservationRebuildIfDurable(force = force)
1459:     }
1460: 
1461:     DisposableEffect(lifecycleOwner) {
1462:         val observer = LifecycleEventObserver { _, event ->
1463:             persistRecoveryForLifecycleEvent(event, latestPersistActiveGameState)
1464:         }
1465:         lifecycleOwner.lifecycle.addObserver(observer)
1466:         onDispose {
1467:             lifecycleOwner.lifecycle.removeObserver(observer)
1468:         }
1469:     }
1470: 
1471:     SideEffect {
1472:         persistAndReleaseA4ObservationRebuildIfDurable()
1473:     }
1474: 
1475:     fun maxUndercoverFor(count: Int): Int {
1476:         return ((if (includeBlank) count - 2 else count - 1).coerceAtLeast(1))
1477:     }
1478: 
1479:     fun clampUndercoverCount() {
1480:         undercoverCount = undercoverCount.coerceIn(1, maxUndercoverFor(playerNames.size))
1481:     }
1482: 
1483:     fun addCurrentPlayer(name: String) {
1484:         val trimmedName = name.trim()
1485:         if (trimmedName.isNotEmpty() && playerNames.size < MAX_PLAYERS && trimmedName !in playerNames) {
1486:             playerNames.add(trimmedName)
1487:             clampUndercoverCount()
1488:         }
1489:     }
1490: 
1491:     fun removeCurrentPlayer(index: Int) {
1492:         if (index in playerNames.indices) {
1493:             playerNames.removeAt(index)
1494:             clampUndercoverCount()
1495:         }
1496:     }
1497: 
1498:     fun moveCurrentPlayerTo(index: Int, targetIndex: Int) {
1499:         if (index !in playerNames.indices || targetIndex !in playerNames.indices) return
1500:         if (index == targetIndex) return
1501:         val reordered = reorderHostTableItems(
1502:             items = playerNames,
1503:             fromIndex = index,
1504:             targetIndex = targetIndex,
1505:         )
1506:         playerNames.clear()
1507:         playerNames.addAll(reordered)
1508:     }
1509: 
1510:     fun addCommonPlayer() {
1511:         val trimmedName = newCommonPlayerName.trim()
1512:         if (trimmedName.isNotEmpty() && trimmedName !in commonPlayers) {
1513:             commonPlayers.add(trimmedName)
1514:             baseContext.saveCommonPlayers(commonPlayers)
1515:             newCommonPlayerName = ""
1516:         }
1517:     }
1518: 
1519:     fun removeCommonPlayer(name: String) {
1520:         commonPlayers.remove(name)
1521:         baseContext.saveCommonPlayers(commonPlayers)
1522:     }
1523: 
1524:     fun resetDealState(
1525:         nextGameKind: GameKind,
1526:         clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
1527:         preparedClocktowerSeed: Long? = null,
1528:     ) {
1529:         invalidateA4SessionBoundary()
1530:         clearSavedGameState()
1531:         committedClocktowerSetup = null
1532:         committedTroubleBrewingSetupRotationRecord = null
1533:         currentGameKind = nextGameKind
1534:         records.clear()
1535:         clocktowerEvents.clear()
1536:         clocktowerGameSession = null
1537:         publishClocktowerSessionView()
1538:         clocktowerEventCounter = 0
1539:         currentDealIndex = 0
1540:         round = 1
1541:         showResults = false
1542:         gameOutcome = null
1543:         selectedElimination = null
1544:         clocktowerPhase = ClocktowerPhase.FirstNight
1545:         if (nextGameKind == GameKind.Clocktower) {
1546:             val gameId = UUID.randomUUID().toString()
1547:             val gameSeed = preparedClocktowerSeed ?: newClocktowerSeed()
1548:             clocktowerGameSession = ClocktowerGameSession.createProduction(
1549:                 gameId = gameId,
1550:                 gameSeed = gameSeed,
1551:                 initialState = cards.toClocktowerGameState(
1552:                     script = clocktowerScript,
1553:                     seed = gameSeed,
1554:                     poisonedPlayerName = null,
1555:                 ),
1556:                 semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
1557:             )
1558:             publishClocktowerSessionView()
1559:             if (clocktowerScript == ClocktowerScript.TroubleBrewing) {
1560:                 val rulesetBasis = ClocktowerRulesetPersistenceBasis(
1561:                     cards.map { card ->
1562:                         RoleId(requireNotNull(card.clocktowerRole) {
1563:                             "Trouble Brewing setup is missing an assigned role."
1564:                         }.enName)
1565:                     }.toSet(),
1566:                 )
1567:                 clocktowerRulesetRoleIds = rulesetBasis.roleIds
1568:                 clocktowerRulesetRef = troubleBrewingRulesetRefFor(rulesetBasis)
1569:                     ?: error("Unable to resolve Trouble Brewing ruleset reference at setup.")
1570:             } else {
1571:                 clocktowerRulesetRoleIds = emptySet()
1572:                 clocktowerRulesetRef = null
1573:             }
1574:         } else {
1575:             clocktowerRulesetRoleIds = emptySet()
1576:             clocktowerRulesetRef = null
1577:         }
1578:         clocktowerPendingNightDeath = null
1579:         clocktowerDemonAttackDraftTarget = null
1580:         clocktowerSelectedExecution = null
1581:         clocktowerPoisonTarget = null
1582:         clocktowerConfirmedPoisonTarget = null
1583:         clocktowerFortuneTellerFirst = null
1584:         clocktowerFortuneTellerSecond = null
1585:         clocktowerChambermaidFirst = null
1586:         clocktowerChambermaidSecond = null
1587:         clocktowerRavenkeeperTarget = null
1588:         clocktowerRedHerring = null
1589:         clocktowerRecommendedDemonBluffRoleNames = emptyList()
1590:         // Identity is committed before reveal; a Drunk's concrete first-night
1591:         // clue is not, because the Poisoner may still change its legality.
1592:         clocktowerRecommendedDrunkInvestigatorRoleName = null
1593:         clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
1594:         clocktowerButlerMaster = null
1595:         clocktowerMonkProtectedTarget = null
1596:         clocktowerConfirmedMonkProtectedTarget = null
1597:         clocktowerMayorRedirectTarget = null
1598:         clocktowerConfirmedMayorRedirectTarget = null
1599:         clocktowerPendingNewDemonName = null
1600:         clocktowerPendingNightNewDemonIdentityName = null
1601:         clocktowerDemonSuccessorTarget = null
1602:         clearConfirmedDemonSuccessorTarget()
1603:         clocktowerVirginUsed = false
1604:         clocktowerSlayerUsed = false
1605:         clocktowerSlayerClaimedNames = emptyList()
1606:         clocktowerArtistUsed = false
1607:         clocktowerArtistClaimedNames = emptyList()
1608:         clocktowerLastExecutedName = null
1609:         clocktowerPendingKlutzName = null
1610:         clocktowerKlutzChoiceName = null
1611:         clocktowerKlutzReturnToDawn = false
1612:         clocktowerGhostVoteAuthorityState.value = ClocktowerGhostVoteAuthority()
1613:         resetClocktowerFlow()
1614:         screen = Screen.PassPhone
1615:         persistActiveGameStateIfNeeded()
1616:     }
1617: 
1618:     fun startUndercoverGame() {
1619:         val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Undercover)
1620:         if (playerNames.size < MIN_PLAYERS) return
1621:         val pair = wordPairsFor(language).random()
1622:         val blankCount = if (includeBlank) 1 else 0
1623:         val roles = buildList {
1624:             repeat(undercoverCount) { add(Role.Undercover) }
1625:             repeat(blankCount) { add(Role.Blank) }
1626:             repeat(playerNames.size - undercoverCount - blankCount) { add(Role.Civilian) }
1627:         }.shuffled()
1628: 
1629:         cards.clear()
1630:         cards.addAll(playerNames.mapIndexed { index, name ->
1631:             val role = roles[index]
1632:             val word = when (role) {
1633:                 Role.Civilian -> pair.civilianWord
1634:                 Role.Undercover -> pair.undercoverWord
1635:                 Role.Blank -> context.getString(R.string.blank_word)
1636:                 else -> ""
1637:             }
1638:             PlayerCard(name = name.ifBlank { context.playerName(index + 1) }, role = role, word = word)
1639:         })
1640:         resetDealState(GameKind.Undercover)
1641:     }
1642: 
1643:     fun startTroubleBrewingGame() {
1644:         val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
1645:         val preparedSeed = newClocktowerSeed()
1646: 
1647:         val datasetJson = baseContext.assets
1648:             .open("setup/trouble_brewing_setup_presets_v2_final.json")
1649:             .bufferedReader(Charsets.UTF_8)
1650:             .use { it.readText() }
1651: 
1652:         val dataset = TroubleBrewingSetupPresetJson.parse(datasetJson)
1653: 
1654:         val rotationHistoryStore = TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
1655:         val rotationHistory = rotationHistoryStore.historyFor(
1656:             datasetId = dataset.datasetId,
1657:             schemaVersion = dataset.schemaVersion,
1658:             playerCount = playerNames.size,
1659:         )
1660:         val playerRotationHistory = rotationHistoryStore.recentPlayerStartingIdentityHistoryFor(
1661:             datasetId = dataset.datasetId,
1662:             schemaVersion = dataset.schemaVersion,
1663:         )
1664: 
1665:         val characterRegistry = BuiltInClocktowerRulesetCatalog
1666:             .fromContext(baseContext)
1667:             .ruleset(ClocktowerScript.TroubleBrewing)
1668:             .characterRegistry
1669: 
1670:         val preparedSetup = TroubleBrewingProductionSetupPreparer.prepare(
1671:             dataset = dataset,
1672:             characterRegistry = characterRegistry,
1673:             orderedPlayerNames = playerNames.toList(),
1674:             gameSeed = preparedSeed,
1675:             recentSetupRotationHistory = rotationHistory,
1676:             recentPlayerStartingIdentityHistory = playerRotationHistory,
1677:         )
1678: 
1679:         val resolvedAssignments = TroubleBrewingDealRoleResolver.resolve(
1680:             dealPlan = preparedSetup.dealPlan,
1681:             availableRoles = completeTroubleBrewingRoles,
1682:         )
1683: 
1684:         val committedCards = resolvedAssignments.map { assignment ->
1685:             val role = assignment.actualRole
1686:             val shownRole = assignment.shownRole
1687: 
1688:             PlayerCard(
1689:                 name = assignment.playerName.ifBlank {
1690:                     context.playerName(assignment.seat)
1691:                 },
1692:                 role = Role.Civilian,
1693:                 roleLabel = shownRole.nameFor(language),
1694:                 actualRoleLabel = role.nameFor(language),
1695:                 clocktowerTeam = role.team,
1696:                 clocktowerRole = role,
1697:                 clocktowerShownRole = shownRole,
1698:                 word = context.getString(
1699:                     R.string.clocktower_card_desc_format,
1700:                     shownRole.team.label(context),
1701:                     shownRole.descriptionFor(language),
1702:                 ),
1703:             )
1704:         }
1705: 
1706:         val setupRecommendationRoleDefinitions =
1707:             clocktowerRoleDefinitionsForScript(ClocktowerScript.TroubleBrewing)
1708:         val initialSetupRecommendationRequest = SetupCoordinationRequest(
1709:             game = committedCards.toClocktowerGameState(
1710:                 script = ClocktowerScript.TroubleBrewing,
1711:                 seed = preparedSeed,
1712:                 poisonedPlayerName = null,
1713:             ),
1714:             roles = setupRecommendationRoleDefinitions,
1715:             lockedDecisions = emptyList(),
1716:             history = CrossGameHistory(),
1717:         )
1718:         val initialFirstNightPrecomputeRequest = committedCards.toClocktowerGameState(
1719:             script = ClocktowerScript.TroubleBrewing,
1720:             seed = preparedSeed,
1721:             poisonedPlayerName = null,
1722:         )
1723: 
1724:         cards.clear()
1725:         cards.addAll(committedCards)
1726: 
1727:         troubleBrewingSetupRecommendationRevealCoordinator.onCommittedDeal(
1728:             request = initialSetupRecommendationRequest,
1729:             enterReveal = {
1730:                 resetDealState(
1731:                     nextGameKind = GameKind.Clocktower,
1732:                     clocktowerScript = ClocktowerScript.TroubleBrewing,
1733:                     preparedClocktowerSeed = preparedSeed,
1734:                 )
1735:                 committedTroubleBrewingSetupRotationRecord =
1736:                     TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(preparedSetup)
1737:                 committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(
1738:                     dealPlan = preparedSetup.dealPlan,
1739:                     resolvedAssignments = resolvedAssignments,
1740:                 )
1741:                 persistActiveGameStateIfNeeded()
1742:                 troubleBrewingFirstNightPrecomputeCoordinator.prewarm(
1743:                     request = initialFirstNightPrecomputeRequest,
1744:                     launchBackground = { work ->
1745:                         troubleBrewingFirstNightPrecomputeScope.launch(Dispatchers.Default) {
1746:                             work()
1747:                         }
1748:                     },
1749:                 )
1750:             },
1751:             launchBackground = { work ->
1752:                 troubleBrewingSetupRecommendationScope.launch(Dispatchers.Default) {
1753:                     work()
1754:                 }
1755:             },
1756:         )
1757:     }
1758: 
1759:     fun startClocktowerGame() {
1760:         val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
1761:         if (playerNames.size < MIN_CLOCKTOWER_PLAYERS) return
1762:         val script = if (playerNames.size in 5..6) {
1763:             selectedClocktowerScript ?: defaultClocktowerScriptFor(playerNames.size)
1764:         } else {
1765:             ClocktowerScript.TroubleBrewing
1766:         }
1767:         if (!canStartClocktowerScript(script)) return
1768: 
1769:         if (script == ClocktowerScript.TroubleBrewing) {
1770:             startTroubleBrewingGame()
1771:             return
1772:         }
1773: 
1774:         val preparedSeed = newClocktowerSeed()
1775:         val preparedSetup = NoGreaterJoyProductionSetupPreparer.prepare(
1776:             ruleset = activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.NoGreaterJoy),
1777:             playerCount = playerNames.size,
1778:             gameSeed = preparedSeed,
1779:         )
1780:         val availableRolesById = clocktowerRolesForScript(script).associateBy { role -> RoleId(role.enName) }
1781:         val committedCards = preparedSetup.assignments.map { assignment ->
1782:             val name = playerNames[assignment.seat - 1]
1783:             val role = requireNotNull(availableRolesById[assignment.actualRole]) {
1784:                 "Committed No Greater Joy actual role '${assignment.actualRole.value}' is unavailable."
1785:             }
1786:             val shownRole = requireNotNull(availableRolesById[assignment.shownRole]) {
1787:                 "Committed No Greater Joy shown role '${assignment.shownRole.value}' is unavailable."
1788:             }
1789:             PlayerCard(
1790:                 name = name.ifBlank { context.playerName(assignment.seat) },
1791:                 role = Role.Civilian,
1792:                 roleLabel = shownRole.nameFor(language),
1793:                 actualRoleLabel = role.nameFor(language),
1794:                 clocktowerTeam = role.team,
1795:                 clocktowerRole = role,
1796:                 clocktowerShownRole = shownRole,
1797:                 word = context.getString(
1798:                     R.string.clocktower_card_desc_format,
1799:                     shownRole.team.label(context),
1800:                     shownRole.descriptionFor(language),
1801:                 ),
1802:             )
1803:         }
1804:         cards.clear()
1805:         cards.addAll(committedCards)
1806:         resetDealState(GameKind.Clocktower, script, preparedSeed)
1807:         committedClocktowerSetup = preparedSetup
1808:         persistActiveGameStateIfNeeded()
1809:     }
1810: 
1811:     fun persistCompletedTroubleBrewingSetupIfNeeded(): Boolean {
1812:         if (currentGameKind != GameKind.Clocktower) return true
1813:         if (currentClocktowerScript != ClocktowerScript.TroubleBrewing) return true
1814:         if (gameOutcome == null) return true
1815:         val record = committedTroubleBrewingSetupRotationRecord ?: return true
1816:         return TroubleBrewingSetupRotationHistoryStore.fromContext(baseContext)
1817:             .recordCompletedGame(
1818:                 gameId = clocktowerGameId,
1819:                 record = record,
1820:             )
1821:     }
1822: 
1823:     fun archiveCurrentGameForRestart(): Boolean {
1824:         if (cards.isEmpty()) return false
1825:         if (!persistCompletedTroubleBrewingSetupIfNeeded()) return false
1826:         invalidateA4SessionBoundary()
1827:         gameHistory = baseContext.archiveGame(
1828:             GameArchiveRecord(
1829:                 gameKind = currentGameKind,
1830:                 round = round,
1831:                 cards = cards.toList(),
1832:                 records = records.toList(),
1833:                 events = clocktowerEvents.toList(),
1834:                 outcome = gameOutcome,
1835:             ),
1836:         )
1837:         clearSavedGameState()
1838:         clocktowerGameSession = null
1839:         publishClocktowerSessionView()
1840:         showNewGameConfirmation = false
1841:         showHostTools = false
1842:         showResults = false
1843:         return true
1844:     }
1845: 
1846:     fun archiveAndReturnToPlayerManagement() {
1847:         if (!archiveCurrentGameForRestart()) return
1848:         cards.clear()
1849:         records.clear()
1850:         clocktowerEvents.clear()
1851:         clocktowerEventCounter = 0
1852:         gameOutcome = null
1853:         currentDealIndex = 0
1854:         resetClocktowerFlow()
1855:         screen = Screen.Setup
1856:     }
1857: 
1858:     fun archiveAndStartNewGame() {
1859:         if (!archiveCurrentGameForRestart()) return
1860:         when (currentGameKind) {
1861:             GameKind.Undercover -> startUndercoverGame()
1862:             GameKind.Werewolf -> error("Werewolf runtime has been removed.")
1863:             GameKind.Clocktower -> startClocktowerGame()
1864:         }
1865:     }
1866: 
1867:     fun recordClocktowerRoleChangeAction(
1868:         targetSeat: Int,
1869:         nextRole: ClocktowerRole,
1870:         actionId: String,
1871:     ) {
1872:         val localSequence = clocktowerEventCounter + 1
1873:         recordClocktowerAction(ActionFactDraft.RoleChange(
1874:             actionId = actionId,
1875:             phase = storytellerPhaseFor(),
1876:             round = round,
1877:             sequence = localSequence,
1878:             targetSeat = targetSeat,
1879:             role = RoleId(nextRole.enName),
1880:             alignment = when (nextRole.team) {
1881:                 ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> ClocktowerAlignment.GOOD
1882:                 ClocktowerTeam.Minion, ClocktowerTeam.Demon -> ClocktowerAlignment.EVIL
1883:             },
1884:             type = when (nextRole.team) {
1885:                 ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
1886:                 ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
1887:                 ClocktowerTeam.Minion -> CharacterType.MINION
1888:                 ClocktowerTeam.Demon -> CharacterType.DEMON
1889:             },
1890:         ))
1891:     }
1892: 
1893:     fun setClocktowerActualRole(
1894:         playerName: String,
1895:         nextRole: ClocktowerRole,
1896:         recordSemanticHistory: Boolean = true,
1897:     ) {
1898:         val index = cards.indexOfFirst { it.name == playerName }
1899:         if (index >= 0) {
1900:             val targetSeat = index + 1
1901:             if (recordSemanticHistory) {
1902:                 recordClocktowerRoleChangeAction(
1903:                     targetSeat = targetSeat,
1904:                     nextRole = nextRole,
1905:                     actionId = clocktowerActionId(
1906:                         kind = "role-change-${nextRole.enName.lowercase().replace(' ', '-')}",
1907:                         localSequence = clocktowerEventCounter + 1,
1908:                         targetSeat = targetSeat,
1909:                     ),
1910:                 )
1911:             }
1912:             requireClocktowerGameSession().commitActualRoleBoundary(
1913:                 seat = targetSeat,
1914:                 actualRole = RoleId(nextRole.enName),
1915:                 actualAlignment = when (nextRole.team) {
1916:                     ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> ClocktowerAlignment.GOOD
1917:                     ClocktowerTeam.Minion, ClocktowerTeam.Demon -> ClocktowerAlignment.EVIL
1918:                 },
1919:                 actualType = when (nextRole.team) {
1920:                     ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
1921:                     ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
1922:                     ClocktowerTeam.Minion -> CharacterType.MINION
1923:                     ClocktowerTeam.Demon -> CharacterType.DEMON
1924:                 },
1925:             )
1926:             publishClocktowerSessionView()
1927:             invalidateA4RevisionScope()
1928:             cards[index] = cards[index].copy(
1929:                 actualRoleLabel = nextRole.nameFor(language),
1930:                 clocktowerTeam = nextRole.team,
1931:                 clocktowerRole = nextRole,
1932:             )
1933:         }
1934:     }
1935: 
1936:     fun setClocktowerShownRole(playerName: String, nextRole: ClocktowerRole) {
1937:         val index = cards.indexOfFirst { it.name == playerName }
1938:         if (index >= 0) {
1939:             val targetSeat = index + 1
1940:             requireClocktowerGameSession().commitShownRoleBoundary(
1941:                 seat = targetSeat,
1942:                 shownRole = RoleId(nextRole.enName),
1943:             )
1944:             publishClocktowerSessionView()
1945:             invalidateA4RevisionScope()
1946:             cards[index] = cards[index].copy(
1947:                 roleLabel = nextRole.nameFor(language),
1948:                 clocktowerShownRole = nextRole,
1949:                 word = context.getString(
1950:                     R.string.clocktower_card_desc_format,
1951:                     nextRole.team.label(context),
1952:                     nextRole.descriptionFor(language),
1953:                 ),
1954:             )
1955:         }
1956:     }
1957: 
1958:     fun promoteScarletWomanIfNeeded(): String? {
1959:         val alivePlayers = cards.filter { it.eliminatedRound == null }
1960:         // The Demon is already marked dead when this runs: four alive now means
1961:         // there were five alive immediately before the Demon died.
1962:         if (alivePlayers.size < 4) return null
1963:         val scarletWoman = alivePlayers.firstOrNull {
1964:             it.clocktowerRole?.enName == "Scarlet Woman" && it.name != clocktowerConfirmedPoisonTarget
1965:         } ?: return null
1966:         val imp = completeTroubleBrewingRoles.first { it.enName == "Imp" }
1967:         setClocktowerActualRole(scarletWoman.name, imp)
1968:         records.add(EliminationRecord(round, scarletWoman.name, context.getString(R.string.clocktower_record_scarlet_woman_promoted)))
1969:         addClocktowerEvent(
1970:             ClocktowerEventType.RoleChange,
1971:             localizedText("角色变化", "Role changed"),
1972:             localizedText("${playerSeatLabel(cards, scarletWoman.name)} 成为新的恶魔。", "${playerSeatLabel(cards, scarletWoman.name)} became the new Demon."),
1973:             listOf(scarletWoman.name),
1974:         )
1975:         return scarletWoman.name
1976:     }
1977: 
1978:     fun promoteDemonSuccessorIfNeeded(
1979:         impDeathWasSelfChosen: Boolean,
1980:     ): String? {
1981:         if (impDeathWasSelfChosen) return null
1982:         return promoteScarletWomanIfNeeded()
1983:     }
1984: 
1985:     fun applyHostSeatingBack(origin: HostSeatingBackOrigin) {
1986:         val transition = hostSeatingBackTransition(
1987:             flow = hostSeatingSetupFlow,
1988:             origin = origin,
1989:         )
1990:         hostSeatingSetupFlow = transition.flow
1991:         screen = when (transition.destination) {
1992:             HostSeatingSetupDestination.Seating -> Screen.Setup
1993:             HostSeatingSetupDestination.GameSelection -> Screen.GameSelection
1994:         }
1995:     }
1996: 
1997:     val hostSeatingBackOrigin = when (screen) {
1998:         Screen.GameSelection -> HostSeatingBackOrigin.GameSelection
1999:         Screen.UndercoverSettings,
2000:         Screen.ClocktowerSettings -> HostSeatingBackOrigin.GameSettings
2001:         else -> null
2002:     }
2003:     BackHandler(enabled = hostSeatingBackOrigin != null) {
2004:         applyHostSeatingBack(requireNotNull(hostSeatingBackOrigin))
2005:     }
2006: 
2007:     CompositionLocalProvider(LocalContext provides context) {
2008:         MaterialTheme(
2009:             colorScheme = androidx.compose.material3.lightColorScheme(
2010:                 primary = Color(0xFF2F5D50),
2011:                 secondary = Color(0xFFD96C3B),
2012:                 background = Color(0xFFF8F6F0),
2013:                 surface = Color(0xFFFFFCF6),
2014:                 onPrimary = Color.White,
2015:                 onSecondary = Color.White,
2016:                 onBackground = Color(0xFF1F2925),
2017:                 onSurface = Color(0xFF1F2925),
2018:             )
2019:         ) {
2020:             Surface(
2021:                 modifier = Modifier
2022:                     .fillMaxSize()
2023:                     .background(MaterialTheme.colorScheme.background),
2024:                 color = MaterialTheme.colorScheme.background,
2025:             ) {
2026:                 Column(
2027:                     modifier = Modifier
2028:                         .fillMaxSize(),
2029:                 ) {
2030:                     Box(modifier = Modifier.weight(1f)) {
2031:                         when (screen) {
2032:                     Screen.Landing -> ClocktowerLandingScreen(
2033:                         hasSavedGame = savedGamePreview != null,
2034:                         onStartGame = {
2035:                             hostSeatingSetupFlow = HostSeatingSetupFlow()
2036:                             screen = Screen.Setup
2037:                         },
2038:                         onContinueGame = ::restoreSavedGame,
2039:                     )
2040: 
2041:                     Screen.Setup -> SeatingFirstSetupScreen(
2042:                     savedGamePreview = savedGamePreview,
2043:                     commonPlayers = commonPlayers,
2044:                     playerNames = playerNames,
2045:                     onAddCurrentPlayer = ::addCurrentPlayer,
2046:                     onRemoveCurrentPlayer = ::removeCurrentPlayer,
2047:                     onMoveCurrentPlayerTo = ::moveCurrentPlayerTo,
2048:                     onResumeSavedGame = ::restoreSavedGame,
2049:                     onDiscardSavedGame = ::clearSavedGameState,
2050:                     onOpenSettings = { screen = Screen.Settings },
2051:                     onConfirmSeats = {
2052:                         hostSeatingSetupFlow = hostSeatingSetupFlow.confirmSeats(playerNames)
2053:                         screen = Screen.GameSelection
2054:                     },
2055:                 )
2056: 
2057:                     Screen.GameSelection -> SeatingFirstGameSelectionScreen(
2058:                     seating = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
2059:                         "Game selection requires confirmed seating"
2060:                     },
2061:                     onBackToSeating = {
2062:                         applyHostSeatingBack(HostSeatingBackOrigin.GameSelection)
2063:                     },
2064:                     onOpenUndercoverSettings = {
2065:                         hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Undercover)
2066:                         screen = Screen.UndercoverSettings
2067:                     },
2068:                     onOpenClocktowerSettings = {
2069:                         hostSeatingSetupFlow = hostSeatingSetupFlow.chooseGame(GameKind.Clocktower)
2070:                         screen = Screen.ClocktowerSettings
2071:                     },
2072:                 )
2073: 
2074:                     Screen.UndercoverSettings -> UndercoverSettingsScreen(
2075:                         playerCount = playerCount,
2076:                         undercoverCount = undercoverCount,
2077:                         includeBlank = includeBlank,
2078:                         onUndercoverCountChange = { undercoverCount = it },
2079:                         onIncludeBlankChange = { checked ->
2080:                             includeBlank = checked
2081:                             clampUndercoverCount()
2082:                         },
2083:                         onBack = {
2084:                             applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
2085:                         },
2086:                         onStart = ::startUndercoverGame,
2087:                     )
2088: 
2089:                     Screen.ClocktowerSettings -> ClocktowerSettingsScreen(
2090:                         playerCount = playerCount,
2091:                         playerNames = requireNotNull(hostSeatingSetupFlow.confirmedSeating) {
2092:                             "Clocktower settings require confirmed seating"
2093:                         }.playerNames,
2094:                         selectedScript = selectedClocktowerScript ?: defaultClocktowerScriptFor(playerCount),
2095:                         onScriptChange = { selectedClocktowerScript = it },
2096:                         onBack = {
2097:                             applyHostSeatingBack(HostSeatingBackOrigin.GameSettings)
2098:                         },
2099:                         onStart = ::startClocktowerGame,
2100:                     )
2101: 
2102:                     Screen.Settings -> SettingsScreen(
2103:                         languageMode = languageMode,
2104:                         storytellerExperienceMode = storytellerExperienceMode,
2105:                         commonPlayers = commonPlayers,
2106:                         newCommonPlayerName = newCommonPlayerName,
2107:                         onLanguageModeChange = { nextMode ->
2108:                             languageMode = nextMode
2109:                             baseContext.saveLanguageMode(nextMode)
2110:                         },
2111:                         onStorytellerExperienceModeChange = { mode ->
2112:                             storytellerExperienceMode = mode
2113:                             baseContext.saveStorytellerExperienceMode(mode)
2114:                         },
2115:                         onNewCommonPlayerNameChange = { newCommonPlayerName = it },
2116:                         onAddCommonPlayer = ::addCommonPlayer,
2117:                         onRemoveCommonPlayer = ::removeCommonPlayer,
2118:                         onBack = { screen = Screen.Setup },
2119:                     )
2120: 
2121:                     Screen.PassPhone -> PassPhoneScreen(
2122:                         playerName = cards[currentDealIndex].name,
2123:                         playerNames = cards.map { it.name },
2124:                         gameKind = currentGameKind,
2125:                         current = currentDealIndex + 1,
2126:                         total = cards.size,
2127:                         onReveal = { screen = Screen.RevealCard },
2128:                         onPrevious = {
2129:                             if (currentGameKind == GameKind.Clocktower && currentDealIndex > 0) {
2130:                                 currentDealIndex -= 1
2131:                             }
2132:                         },
2133:                         onHostTools = {
2134:                             if (currentGameKind == GameKind.Clocktower) {
2135:                                 hostToolTab = HostToolTab.Roles
2136:                                 showHostTools = true
2137:                             }
2138:                         },
2139:                         onNext = {
2140:                             if (currentGameKind == GameKind.Clocktower) {
2141:                                 if (currentDealIndex == cards.lastIndex) {
2142:                                     screen = Screen.ClocktowerJudge
2143:                                 } else {
2144:                                     currentDealIndex += 1
2145:                                 }
2146:                             }
2147:                         },
2148:                     )
2149: 
2150:                     Screen.RevealCard -> RevealCardScreen(
2151:                         card = cards[currentDealIndex],
2152:                         gameKind = currentGameKind,
2153:                         current = currentDealIndex + 1,
2154:                         total = cards.size,
2155:                         onHide = {
2156:                             when (currentGameKind) {
2157:                                 GameKind.Werewolf -> error("Werewolf runtime has been removed.")
2158:                                 GameKind.Clocktower -> screen = Screen.PassPhone
2159:                                 GameKind.Undercover -> {
2160:                                     if (currentDealIndex == cards.lastIndex) {
2161:                                         screen = Screen.Game
2162:                                     } else {
2163:                                         currentDealIndex += 1
2164:                                         screen = Screen.PassPhone
2165:                                     }
2166:                                 }
2167:                             }
2168:                         },
2169:                     )
2170: 
2171:                     Screen.ClocktowerJudge -> ClocktowerJudgeScreen(
2172:                         automaticStorytellerInfo = automaticStorytellerInfo,
2173:                         automaticStorytellerStyle = storytellerRecommendationUxPolicy.recommendationStyle,
2174:                         cards = cards,
2175:                         events = clocktowerEvents,
2176:                         script = currentClocktowerScript,
2177:                         gameId = clocktowerGameId,
2178:                         gameSeed = clocktowerGameSeed,
2179:                         gameStateRevision = clocktowerGameStateRevision,
2180:                         playerInputRevision = clocktowerPlayerInputRevision,
2181:                         setupHistory = CrossGameHistory(),
2182:                         setupRecommendationResultProvider =
2183:                             if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
2184:                                 troubleBrewingSetupRecommendationRevealCoordinator::resultFor
2185:                             } else {
2186:                                 null
2187:                             },
2188:                         firstNightNaturalPairReadyProvider =
2189:                             if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
2190:                                 troubleBrewingFirstNightPrecomputeCoordinator::readyFor
2191:                             } else {
2192:                                 null
2193:                             },
2194:                         firstNightNaturalPairResultProvider =
2195:                             if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
2196:                                 troubleBrewingFirstNightPrecomputeCoordinator::resultFor
2197:                             } else {
2198:                                 null
2199:                             },
2200:                         onInitialRecommendationDemand = recordA4InitialRecommendationDemand,
2201:                         phase = clocktowerPhase,
2202:                         round = round,
2203:                         nightCheckpoint = currentClocktowerNightCheckpoint(),
2204:                         pendingNightDeath = clocktowerPendingNightDeath,
2205:                         demonAttackDraftTarget = clocktowerDemonAttackDraftTarget,
2206:                         selectedExecution = clocktowerSelectedExecution,
2207:                         // The draft is visible only while the Poisoner is choosing.
2208:                         // All ability and outcome evaluation must use the confirmed fact.
2209:                         poisonTarget = clocktowerConfirmedPoisonTarget,
2210:                         poisonDraftTarget = clocktowerPoisonTarget,
2211:                         fortuneTellerFirst = clocktowerFortuneTellerFirst,
2212:                         fortuneTellerSecond = clocktowerFortuneTellerSecond,
2213:                         chambermaidFirst = clocktowerChambermaidFirst,
2214:                         chambermaidSecond = clocktowerChambermaidSecond,
2215:                         ravenkeeperTarget = clocktowerRavenkeeperTarget,
2216:                         redHerring = clocktowerRedHerring,
2217:                         recommendedDemonBluffRoleNames = clocktowerRecommendedDemonBluffRoleNames,
2218:                         recommendedDrunkInvestigatorRoleName = clocktowerRecommendedDrunkInvestigatorRoleName,
2219:                         recommendedDrunkInvestigatorSeats = clocktowerRecommendedDrunkInvestigatorSeats,
2220:                         butlerMaster = clocktowerButlerMaster,
2221:                         monkProtectedTarget = clocktowerConfirmedMonkProtectedTarget,
2222:                         monkProtectedDraftTarget = clocktowerMonkProtectedTarget,
2223:                         mayorRedirectTarget = clocktowerConfirmedMayorRedirectTarget,
2224:                         mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
2225:                         pendingNewDemonName = clocktowerPendingNewDemonName,
2226:                         pendingNightNewDemonIdentityName = clocktowerPendingNightNewDemonIdentityName,
2227:                         demonSuccessorTarget = clocktowerDemonSuccessorTarget,
2228:                         virginUsed = clocktowerVirginUsed,
2229:                         slayerUsed = clocktowerSlayerUsed,
2230:                         slayerClaimedNames = clocktowerSlayerClaimedNames,
2231:                         artistUsed = clocktowerArtistUsed,
2232:                         artistClaimedNames = clocktowerArtistClaimedNames,
2233:                         lastExecutedName = clocktowerLastExecutedName,
2234:                         pendingKlutzName = clocktowerPendingKlutzName,
2235:                         klutzChoiceName = clocktowerKlutzChoiceName,
2236:                         nightStartedState = clocktowerNightStartedState,
2237:                         nightStepIndexState = clocktowerNightStepIndexState,
2238:                         onMovePreviousNightStep = {
2239:                             val transaction = NightCheckpointHostTransaction.movePrevious(
2240:                                 checkpoint = currentClocktowerNightCheckpoint(),
2241:                             )
2242:                             when (transaction.revisionIntent) {
2243:                                 NightCheckpointRevisionIntent.NONE -> Unit
2244:                                 NightCheckpointRevisionIntent.PLAYER_INPUT -> advanceClocktowerPlayerInputRevision()
2245:                                 NightCheckpointRevisionIntent.GAME_STATE -> advanceClocktowerGameStateRevision()
2246:                             }
2247:                             clocktowerNightStepIndexState.value = transaction.checkpoint.nightStepIndex
2248:                         },
2249:                         dayModeState = clocktowerDayModeState,
2250:                         ghostVoteAuthority = clocktowerGhostVoteAuthorityState.value,
2251:                         onGhostVoteAuthorityChange = { clocktowerGhostVoteAuthorityState.value = it },
2252:                         highestVoteNameState = clocktowerHighestVoteNameState,
2253:                         highestVoteCountState = clocktowerHighestVoteCountState,
2254:                         gameOutcome = gameOutcome,
2255:                         onRecordEvent = { type, title, detail, names ->
2256:                             addClocktowerEvent(type, title, detail, names)
2257:                         },
2258:                         onRecordEpistemicObservation = ::recordEpistemicObservation,
2259:                         onStructuredNumberDecisionPrepared = ::evaluateSdeRuntimeShadow,
2260:                         onCommitConfirmedInformationDecision = ::commitConfirmedInformationDecision,
2261:                         onHostTools = {
2262:                             hostToolTab = HostToolTab.Roles
2263:                             showHostTools = true
2264:                         },
2265:                         onPreviousFromFirstNightReady = {
2266:                             currentDealIndex = cards.lastIndex
2267:                             screen = Screen.PassPhone
2268:                         },
2269:                         onSelectNightDeath = { selected ->
2270:                             advanceClocktowerPlayerInputRevision()
2271:                             val reducedCheckpoint = NightCheckpointReducer.reduce(
2272:                                 checkpoint = currentClocktowerNightCheckpoint(),
2273:                                 event = NightResolutionEvent.EditDemonAttackDraft(selected),
2274:                             )
2275:                             clocktowerDemonAttackDraftTarget = reducedCheckpoint.attackDraftTarget
2276:                         },
2277:                         onConfirmDemonAttack = {
2278:                             val transaction = NightCheckpointHostTransaction.confirmDemonAttack(
2279:                                 checkpoint = currentClocktowerNightCheckpoint(),
2280:                             )
2281:                             if (transaction.revisionIntent == NightCheckpointRevisionIntent.GAME_STATE) {
2282:                                 val targetName = transaction.checkpoint.confirmedAttackTarget
2283:                                 if (targetName != null) {
2284:                                     val targetSeat = clocktowerSeatFor(targetName)
2285:                                     val localSequence = clocktowerEventCounter + 1
2286:                                     recordClocktowerAction(ActionFactDraft.Attack(
2287:                                         actionId = clocktowerActionId(
2288:                                             kind = "attack",
2289:                                             localSequence = localSequence,
2290:                                             targetSeat = targetSeat,
2291:                                         ),
2292:                                         phase = storytellerPhaseFor(),
2293:                                         round = round,
2294:                                         sequence = localSequence,
2295:                                         targetSeat = targetSeat,
2296:                                     ))
2297:                                 }
2298:                                 clocktowerPendingNightDeath = transaction.checkpoint.confirmedAttackTarget
2299:                                 clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
2300:                                 clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
2301:                                 advanceClocktowerGameStateRevision()
2302:                             }
2303:                         },
2304:                         onSelectExecution = {
2305:                             advanceClocktowerPlayerInputRevision()
2306:                             clocktowerSelectedExecution = it
2307:                         },
2308:                         onSelectPoisonTarget = { selectedTarget ->
2309:                             advanceClocktowerPlayerInputRevision()
2310:                             val reducedCheckpoint = NightCheckpointReducer.reduce(
2311:                                 checkpoint = currentClocktowerNightCheckpoint(),
2312:                                 event = NightResolutionEvent.EditPoisonDraft(selectedTarget),
2313:                             )
2314:                             clocktowerPoisonTarget = reducedCheckpoint.poisonDraftTarget
2315:                         },
2316:                         onConfirmPoisonTarget = {
2317:                             val transaction = NightCheckpointHostTransaction.confirmPoison(
2318:                                 checkpoint = currentClocktowerNightCheckpoint(),
2319:                             )
2320:                             if (transaction.revisionIntent == NightCheckpointRevisionIntent.GAME_STATE) {
2321:                                 val targetSeat = transaction.checkpoint.confirmedPoisonTarget?.let(::clocktowerSeatFor)
2322:                                 val localSequence = clocktowerEventCounter + 1
2323:                                 recordClocktowerAction(ActionFactDraft.Poison(
2324:                                     actionId = clocktowerActionId(
2325:                                         kind = "poison",
2326:                                         localSequence = localSequence,
2327:                                         targetSeat = targetSeat,
2328:                                     ),
2329:                                     phase = storytellerPhaseFor(),
2330:                                     round = round,
2331:                                     sequence = localSequence,
2332:                                     targetSeat = targetSeat,
2333:                                 ))
2334:                                 requireClocktowerGameSession().commitPoisonTargetBoundary(targetSeat)
2335:                                 publishClocktowerSessionView()
2336:                                 invalidateA4RevisionScope()
2337:                                 clocktowerConfirmedPoisonTarget = transaction.checkpoint.confirmedPoisonTarget
2338:                                 clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
2339:                                 clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
2340:                                 // A Drunk's shown role is committed, but any concrete
2341:                                 // first-night clue remains provisional until displayed.
2342:                                 clocktowerRecommendedDrunkInvestigatorRoleName = null
2343:                                 clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
2344:                             }
2345:                         },
2346:                         onSelectFortuneTellerFirst = {
2347:                             advanceClocktowerPlayerInputRevision()
2348:                             clocktowerFortuneTellerFirst = it
2349:                         },
2350:                         onSelectFortuneTellerSecond = {
2351:                             advanceClocktowerPlayerInputRevision()
2352:                             clocktowerFortuneTellerSecond = it
2353:                         },
2354:                         onSelectChambermaidFirst = {
2355:                             advanceClocktowerPlayerInputRevision()
2356:                             clocktowerChambermaidFirst = it
2357:                         },
2358:                         onSelectChambermaidSecond = {
2359:                             advanceClocktowerPlayerInputRevision()
2360:                             clocktowerChambermaidSecond = it
2361:                         },
2362:                         onSelectRavenkeeperTarget = {
2363:                             advanceClocktowerPlayerInputRevision()
2364:                             clocktowerRavenkeeperTarget = it
2365:                         },
2366:                         onSelectRedHerring = {
2367:                             advanceClocktowerPlayerInputRevision()
2368:                             clocktowerRedHerring = it
2369:                         },
2370:                         onApplyRecommendation = { plan ->
2371:                             // Applying the same automatic plan is a no-op.  In particular, do
2372:                             // not advance playerInputRevision here: that revision is part of the
2373:                             // recommendation key, so an unconditional increment creates an
2374:                             // endless Loading -> Ready -> apply -> Loading cycle.
2375:                             var setupChanged = false
2376:                             val recommendedRedHerring = plan.decisions
2377:                                 .filterIsInstance<StorytellerDecision.RedHerring>()
2378:                                 .singleOrNull()
2379:                                 ?.let { decision -> cards.getOrNull(decision.seat - 1)?.name }
2380:                             if (recommendedRedHerring != null && recommendedRedHerring != clocktowerRedHerring) {
2381:                                 clocktowerRedHerring = recommendedRedHerring
2382:                                 setupChanged = true
2383:                             }
2384:                             // Concrete Drunk information is provisional: never carry a
2385:                             // setup recommendation across a later Poisoner decision.
2386:                             if (clocktowerRecommendedDrunkInvestigatorRoleName != null ||
2387:                                 clocktowerRecommendedDrunkInvestigatorSeats.isNotEmpty()
2388:                             ) {
2389:                                 clocktowerRecommendedDrunkInvestigatorRoleName = null
2390:                                 clocktowerRecommendedDrunkInvestigatorSeats = emptyList()
2391:                                 setupChanged = true
2392:                             }
2393:                             val recommendedDemonBluffs = plan.decisions
2394:                                 .filterIsInstance<StorytellerDecision.DemonBluffs>()
2395:                                 .singleOrNull()
2396:                                 ?.roles
2397:                                 ?.map(RoleId::value)
2398:                                 .orEmpty()
2399:                             if (recommendedDemonBluffs != clocktowerRecommendedDemonBluffRoleNames) {
2400:                                 clocktowerRecommendedDemonBluffRoleNames = recommendedDemonBluffs
2401:                                 setupChanged = true
2402:                             }
2403:                             if (setupChanged) advanceClocktowerPlayerInputRevision()
2404:                         },
2405:                         onSelectButlerMaster = {
2406:                             advanceClocktowerPlayerInputRevision()
2407:                             clocktowerButlerMaster = it
2408:                         },
2409:                         onSelectMonkProtectedTarget = { selectedTarget ->
2410:                             advanceClocktowerPlayerInputRevision()
2411:                             val reducedCheckpoint = NightCheckpointReducer.reduce(
2412:                                 checkpoint = currentClocktowerNightCheckpoint(),
2413:                                 event = NightResolutionEvent.EditMonkProtectionDraft(selectedTarget),
2414:                             )
2415:                             clocktowerMonkProtectedTarget = reducedCheckpoint.monkDraftTarget
2416:                         },
2417:                         onConfirmMonkProtectedTarget = {
2418:                             val transaction = NightCheckpointHostTransaction.confirmMonkProtection(
2419:                                 checkpoint = currentClocktowerNightCheckpoint(),
2420:                             )
2421:                             if (transaction.revisionIntent == NightCheckpointRevisionIntent.GAME_STATE) {
2422:                                 val targetName = transaction.checkpoint.confirmedMonkTarget
2423:                                 if (targetName != null) {
2424:                                     val targetSeat = clocktowerSeatFor(targetName)
2425:                                     val localSequence = clocktowerEventCounter + 1
2426:                                     recordClocktowerAction(ActionFactDraft.Protect(
2427:                                         actionId = clocktowerActionId(
2428:                                             kind = "protect",
2429:                                             localSequence = localSequence,
2430:                                             targetSeat = targetSeat,
2431:                                         ),
2432:                                         phase = storytellerPhaseFor(),
2433:                                         round = round,
2434:                                         sequence = localSequence,
2435:                                         targetSeat = targetSeat,
2436:                                     ))
2437:                                 }
2438:                                 clocktowerConfirmedMonkProtectedTarget = transaction.checkpoint.confirmedMonkTarget
2439:                                 clocktowerConfirmedMayorRedirectTarget = transaction.checkpoint.confirmedMayorRedirectTarget
2440:                                 clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
2441:                                 advanceClocktowerGameStateRevision()
2442:                             }
2443:                         },
2444:                         onSelectMayorRedirectTarget = { selectedTarget ->
2445:                             advanceClocktowerPlayerInputRevision()
2446:                             val reducedCheckpoint = NightCheckpointReducer.reduce(
2447:                                 checkpoint = currentClocktowerNightCheckpoint(),
2448:                                 event = NightResolutionEvent.EditMayorRedirectDraft(selectedTarget),
2449:                             )
2450:                             clocktowerMayorRedirectTarget = reducedCheckpoint.mayorRedirectDraftTarget
2451:                         },
2452:                         onConfirmMayorRedirectTarget = {
2453:                             val checkpoint = currentClocktowerNightCheckpoint()
2454:                             val reducedCheckpoint = NightCheckpointReducer.reduce(
2455:                                 checkpoint = checkpoint,
2456:                                 event = NightResolutionEvent.ConfirmMayorRedirect,
2457:                             )
2458:                             if (reducedCheckpoint.confirmedMayorRedirectTarget != checkpoint.confirmedMayorRedirectTarget) {
2459:                                 clocktowerConfirmedMayorRedirectTarget = reducedCheckpoint.confirmedMayorRedirectTarget
2460:                                 advanceClocktowerGameStateRevision()
2461:                             }
2462:                         },
2463:                         onSelectDemonSuccessor = { selectedTarget ->
2464:                             val transaction = NightCheckpointHostTransaction.editDemonSuccessor(
2465:                                 checkpoint = currentClocktowerNightCheckpoint(),
2466:                                 selectedTarget = selectedTarget,
2467:                             )
2468:                             when (transaction.revisionIntent) {
2469:                                 NightCheckpointRevisionIntent.NONE -> Unit
2470:                                 NightCheckpointRevisionIntent.PLAYER_INPUT -> advanceClocktowerPlayerInputRevision()
2471:                                 NightCheckpointRevisionIntent.GAME_STATE -> advanceClocktowerGameStateRevision()
2472:                             }
2473:                             clocktowerDemonSuccessorTarget = transaction.checkpoint.demonSuccessorDraftTarget
2474:                         },
2475:                         onConfirmDemonSuccessorTarget = { _ ->
2476:                             val transaction = NightCheckpointHostTransaction.confirmDemonSuccessor(
2477:                                 checkpoint = currentClocktowerNightCheckpoint(),
2478:                             )
2479:                             when (transaction.revisionIntent) {
2480:                                 NightCheckpointRevisionIntent.NONE -> Unit
2481:                                 NightCheckpointRevisionIntent.PLAYER_INPUT -> {
2482:                                     clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
2483:                                     advanceClocktowerPlayerInputRevision()
2484:                                 }
2485:                                 NightCheckpointRevisionIntent.GAME_STATE -> {
2486:                                     clocktowerConfirmedDemonSuccessorTarget = transaction.checkpoint.confirmedDemonSuccessorTarget
2487:                                     advanceClocktowerGameStateRevision()
2488:                                 }
2489:                             }
2490:                         },
2491:                         onConfirmNewDemon = {
2492:                             val pendingName = clocktowerPendingNewDemonName
2493:                             var dawnPhaseActionIdToCommit: String? = null
2494:                             var dawnPhaseStateMutationRequired = false
2495:                             val canEnterDawn =
2496:                                 if (pendingName != null) {
2497:                                     val baseGameState = cards.toClocktowerGameState(
2498:                                         currentClocktowerScript,
2499:                                         clocktowerGameSeed,
2500:                                         poisonedPlayerName = clocktowerConfirmedPoisonTarget,
2501:                                     )
2502:                                     val checkpoint = currentClocktowerNightCheckpoint()
2503:                                     val effectiveNightState = ClocktowerEffectiveNightState(
2504:                                         effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
2505:                                             (index + 1).takeIf { card.eliminatedRound == null }
2506:                                         }.toSet(),
2507:                                         effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
2508:                                             card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
2509:                                         }.toMap(),
2510:                                     )
2511:                                     val poisoner = cards.mapIndexedNotNull { index, card ->
2512:                                         card.clocktowerRole
2513:                                             ?.takeIf { role -> card.eliminatedRound == null && role.enName == "Poisoner" }
2514:                                             ?.let { role -> index + 1 to role }
2515:                                     }.firstOrNull()
2516:                                     val demonRole = requireNotNull(cards.firstOrNull {
2517:                                         it.clocktowerTeam == ClocktowerTeam.Demon
2518:                                     }?.clocktowerRole)
2519:                                     val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
2520:                                         baseGameState = baseGameState,
2521:                                         checkpoint = checkpoint,
2522:                                         demonRoleId = RoleId(demonRole.enName),
2523:                                         poisonResolutionInput = poisoner?.let { (poisonerSeat, poisonerRole) ->
2524:                                             NightDawnPoisonResolutionInput(
2525:                                                 poisonerSeat = poisonerSeat,
2526:                                                 poisonerRoleId = RoleId(poisonerRole.enName),
2527:                                                 effectiveNightState = effectiveNightState,
2528:                                             )
2529:                                         },
2530:                                         durablePreviousPoisonTargetSeat = NightDawnPoisonRecoveryAuthority
2531:                                             .latestTargetSeatForRound(
2532:                                                 actionTimeline = clocktowerActionTimeline,
2533:                                                 round = round,
2534:                                             ),
2535:                                     )
2536:                                     val dawnCommitIntent = transition.dawnCommitIntent
2537:                                     if (transition.continuation == NightResolutionContinuation.DAWN && dawnCommitIntent != null) {
2538:                                         val durableMaterializationPlan = NightDawnDurableMaterializationPlanner.plan(
2539:                                             gameId = clocktowerGameId,
2540:                                             round = round,
2541:                                             intent = dawnCommitIntent,
2542:                                             state = DawnDurableMaterializationState(
2543:                                                 aliveSeats = cards.mapIndexedNotNull { index, card ->
2544:                                                     (index + 1).takeIf { card.eliminatedRound == null }
2545:                                                 }.toSet(),
2546:                                                 roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
2547:                                                     card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
2548:                                                 }.toMap(),
2549:                                                 currentPhase = storytellerPhaseFor(),
2550:                                                 currentPoisonTargetSeat =
2551:                                                     clocktowerConfirmedPoisonTarget?.let(::clocktowerSeatFor),
2552:                                                 committedActionIds = clocktowerActionTimeline.entries
2553:                                                     .map { it.fact.actionId }
2554:                                                     .toSet(),
2555:                                                 committedObservationRecordIds = clocktowerEpistemicObservations
2556:                                                     .map { it.recordId }
2557:                                                     .toSet(),
2558:                                             ),
2559:                                             advanceToDawn = true,
2560:                                         )
2561:                                         val phaseAdvance = durableMaterializationPlan.phaseAdvance
2562:                                         val poisonMaterialization = durableMaterializationPlan.poison
2563:                                         durableMaterializationPlan.roleChanges.forEach { roleChangeMaterialization ->
2564:                                             val roleChange = roleChangeMaterialization.intent
2565:                                             val targetName = cards.getOrNull(roleChange.targetSeat - 1)?.name
2566:                                             val nextRole = clocktowerRolesForScript(currentClocktowerScript)
2567:                                                 .firstOrNull { role -> RoleId(role.enName) == roleChange.roleId }
2568:                                             if (targetName != null && nextRole != null) {
2569:                                                 roleChangeMaterialization.actionIdToCommit?.let { actionId ->
2570:                                                     recordClocktowerRoleChangeAction(
2571:                                                         targetSeat = roleChange.targetSeat,
2572:                                                         nextRole = nextRole,
2573:                                                         actionId = actionId,
2574:                                                     )
2575:                                                 }
2576:                                                 if (roleChangeMaterialization.stateMutationRequired) {
2577:                                                     setClocktowerActualRole(
2578:                                                         targetName,
2579:                                                         nextRole,
2580:                                                         recordSemanticHistory = false,
2581:                                                     )
2582:                                                     records.add(
2583:                                                         EliminationRecord(
2584:                                                             round,
2585:                                                             targetName,
2586:                                                             context.getString(R.string.clocktower_record_imp_passed),
2587:                                                         ),
2588:                                                     )
2589:                                                     addClocktowerEvent(
2590:                                                         ClocktowerEventType.RoleChange,
2591:                                                         localizedText("角色变化", "Role changed"),
2592:                                                         localizedText(
2593:                                                             "${playerSeatLabel(cards, targetName)} 成为新的小恶魔。",
2594:                                                             "${playerSeatLabel(cards, targetName)} became the new Imp.",
2595:                                                         ),
2596:                                                         listOf(targetName),
2597:                                                     )
2598:                                                 }
2599:                                             }
2600:                                         }
2601:                                         poisonMaterialization?.let { materialization ->
2602:                                             materialization.actionIdToCommit?.let { actionId ->
2603:                                                 val localSequence = clocktowerEventCounter + 1
2604:                                                 recordClocktowerAction(
2605:                                                     ActionFactDraft.Poison(
2606:                                                         actionId = actionId,
2607:                                                         phase = storytellerPhaseFor(),
2608:                                                         round = round,
2609:                                                         sequence = localSequence,
2610:                                                         targetSeat = materialization.intent.targetSeat,
2611:                                                     ),
2612:                                                 )
2613:                                             }
2614:                                             if (materialization.stateMutationRequired) {
2615:                                                 requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
2616:                                                     targetSeat = materialization.intent.targetSeat,
2617:                                                 )
2618:                                                 publishClocktowerSessionView()
2619:                                                 val poisonTargetName = materialization.intent.targetSeat
2620:                                                     ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
2621:                                                 clocktowerConfirmedPoisonTarget = poisonTargetName
2622:                                                 clocktowerPoisonTarget = poisonTargetName
2623:                                             }
2624:                                         }
2625:                                         clocktowerPendingNewDemonName = transition.checkpoint.pendingNewDemonName
2626:                                         clocktowerPendingNightNewDemonIdentityName = transition.checkpoint.pendingNightNewDemonIdentityName
2627:                                         clocktowerDemonSuccessorTarget = transition.checkpoint.demonSuccessorDraftTarget
2628:                                         clocktowerConfirmedDemonSuccessorTarget = transition.checkpoint.confirmedDemonSuccessorTarget
2629:                                         dawnPhaseActionIdToCommit = phaseAdvance?.actionIdToCommit
2630:                                         dawnPhaseStateMutationRequired = phaseAdvance?.stateMutationRequired == true
2631:                                         true
2632:                                     } else {
2633:                                         false
2634:                                     }
2635:                                 } else {
2636:                                     false
2637:                                 }
2638:                             if (canEnterDawn) {
2639:                                 clocktowerPendingNewDemonName = null
2640:                                 clocktowerDemonSuccessorTarget = null
2641:                                 clearConfirmedDemonSuccessorTarget()
2642:                                 dawnPhaseActionIdToCommit?.let { actionId ->
2643:                                     val localSequence = clocktowerEventCounter + 1
2644:                                     recordClocktowerAction(ActionFactDraft.PhaseAdvance(
2645:                                         actionId = actionId,
2646:                                         phase = storytellerPhaseFor(),
2647:                                         round = round,
2648:                                         sequence = localSequence,
2649:                                         nextPhase = StorytellerPhase.DAWN,
2650:                                         nextRound = round,
2651:                                     ))
2652:                                 }
2653:                                 if (dawnPhaseStateMutationRequired) {
2654:                                     clocktowerPhase = ClocktowerPhase.Dawn
2655:                                     advanceClocktowerGameStateRevision()
2656:                                 }
2657:                                 resetClocktowerNightFlow()
2658:                             }
2659:                         },
2660:                         onSelectKlutzChoice = {
2661:                             advanceClocktowerPlayerInputRevision()
2662:                             clocktowerKlutzChoiceName = it
2663:                         },
2664:                         onConfirmKlutzChoice = { spyRegistersGoodForChoice ->
2665:                             val choice = clocktowerKlutzChoiceName
2666:                             if (choice != null) {
2667:                                 addClocktowerEvent(
2668:                                     ClocktowerEventType.RoleAction,
2669:                                     localizedText("呆瓜选择", "Klutz choice"),
2670:                                     "${playerSeatLabel(cards, clocktowerPendingKlutzName)} → ${playerSeatLabel(cards, choice)}",
2671:                                     listOfNotNull(clocktowerPendingKlutzName, choice),
2672:                                 )
2673:                                 val chosenCard = cards.firstOrNull { it.name == choice }
2674:                                 if (chosenCard != null && isClocktowerEvil(chosenCard) && !(chosenCard.clocktowerRole?.enName == "Spy" && spyRegistersGoodForChoice)) {
2675:                                     gameOutcome = GameOutcome(
2676:                                         title = context.getString(R.string.outcome_clocktower_evil_title),
2677:                                         summary = localizedText("呆瓜选择了邪恶玩家，善良阵营失败。", "The Klutz chose an evil player, so the good team loses."),
2678:                                         reason = localizedText("${playerSeatLabel(cards, clocktowerPendingKlutzName)} 选择了 ${playerSeatLabel(cards, choice)}。", "${playerSeatLabel(cards, clocktowerPendingKlutzName)} chose ${playerSeatLabel(cards, choice)}."),
2679:                                     )
2680:                                     showResults = true
2681:                                     addOutcomeEvent(gameOutcome)
2682:                                 } else {
2683:                                     clocktowerPendingKlutzName = null
2684:                                     clocktowerKlutzChoiceName = null
2685:                                     if (clocktowerKlutzReturnToDawn) {
2686:                                         recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)
2687:                                         clocktowerPhase = ClocktowerPhase.Dawn
2688:                                         clocktowerKlutzReturnToDawn = false
2689:                                     } else {
2690:                                         val nextRound = round + 1
2691:                                         materializeClocktowerPoisonExpiryAtDusk()
2692:                                         recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
2693:                                         round = nextRound
2694:                                         clocktowerPhase = ClocktowerPhase.Night
2695:                                     }
2696:                                     resetClocktowerDayFlow()
2697:                                     resetClocktowerNightFlow()
2698:                                     advanceClocktowerGameStateRevision()
2699:                                 }
2700:                             }
2701:                         },
2702:                         onConfirmArtistQuestion = { claimantName, truthfulAnswer, shownAnswer ->
2703:                             if (claimantName !in clocktowerArtistClaimedNames) {
2704:                                 clocktowerArtistClaimedNames = clocktowerArtistClaimedNames + claimantName
2705:                             }
2706:                             val claimantCard = cards.firstOrNull { it.name == claimantName }
2707:                             if (claimantCard?.clocktowerRole?.enName == "Artist" && !clocktowerArtistUsed) {
2708:                                 clocktowerArtistUsed = true
2709:                             }
2710:                             records.add(EliminationRecord(round, claimantName, localizedText("艺术家提问已处理", "Artist question resolved")))
2711:                             addClocktowerEvent(
2712:                                 ClocktowerEventType.RoleAction,
2713:                                 localizedText("艺术家提问", "Artist question"),
2714:                                 localizedText(
2715:                                     "${playerSeatLabel(cards, claimantName)} · 真实答案：${if (truthfulAnswer) "是" else "否"} · 展示：${if (shownAnswer) "是" else "否"}",
2716:                                     "${playerSeatLabel(cards, claimantName)} · truthful: ${if (truthfulAnswer) "yes" else "no"} · shown: ${if (shownAnswer) "yes" else "no"}",
2717:                                 ),
2718:                                 listOf(claimantName),
2719:                             )
2720:                             clocktowerDayModeState.value = ClocktowerDayMode.Overview
2721:                             advanceClocktowerGameStateRevision()
2722:                         },
2723:                         onSlayerShot = { claimantName, targetName, recluseRegistersAsDemon ->
2724:                             val claimantCard = cards.firstOrNull { it.name == claimantName }
2725:                             val targetIndex = cards.indexOfFirst { it.name == targetName }
2726:                             val targetCard = cards.getOrNull(targetIndex)
2727:                             val slayerDecision = AbilityFunctioningSemantics.oneShotDecision(
2728:                                 subject = claimantCard?.abilitySubject(clocktowerConfirmedPoisonTarget),
2729:                                 role = "Slayer",
2730:                                 alreadyUsed = clocktowerSlayerUsed,
2731:                             )
2732:                             if (claimantName !in clocktowerSlayerClaimedNames) {
2733:                                 clocktowerSlayerClaimedNames = clocktowerSlayerClaimedNames + claimantName
2734:                             }
2735:                             val targetRegistersAsDemon = targetCard?.clocktowerTeam == ClocktowerTeam.Demon ||
2736:                                 (targetCard?.clocktowerRole?.enName == "Recluse" && recluseRegistersAsDemon)
2737:                             if (targetCard?.clocktowerRole?.enName == "Recluse" && recluseRegistersAsDemon) {
2738:                                 addClocktowerEvent(
2739:                                     ClocktowerEventType.RoleAction,
2740:                                     localizedText("隐士登记裁定", "Recluse registration"),
2741:                                     localizedText(
2742:                                         "${playerSeatLabel(cards, targetName)} 在杀手判定中登记为小恶魔。",
2743:                                         "${playerSeatLabel(cards, targetName)} registered as the Imp for the Slayer.",
2744:                                     ),
2745:                                     listOf(targetName),
2746:                                 )
2747:                             }
2748:                             var shotOutcome: GameOutcome? = null
2749:                             if (slayerDecision.consumesUse) {
2750:                                 clocktowerSlayerUsed = true
2751:                                 advanceClocktowerGameStateRevision()
2752:                             }
2753:                             if (slayerDecision.effectApplies && targetIndex >= 0 && targetCard != null && targetCard.eliminatedRound == null && targetRegistersAsDemon) {
2754:                                 val targetSeat = targetIndex + 1
2755:                                 val localSequence = clocktowerEventCounter + 1
2756:                                 preflightClocktowerPublicAliveObservation(
2757:                                     playerName = targetName,
2758:                                     eventSequence = localSequence,
2759:                                 )
2760:                                 recordClocktowerAction(ActionFactDraft.Death(
2761:                                     actionId = clocktowerActionId(
2762:                                         kind = "slayer-death",
2763:                                         localSequence = localSequence,
2764:                                         targetSeat = targetSeat,
2765:                                     ),
2766:                                     phase = storytellerPhaseFor(),
2767:                                     round = round,
2768:                                     sequence = localSequence,
2769:                                     targetSeat = targetSeat,
2770:                                 ))
2771:                                 requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
2772:                                     targetSeat = targetSeat,
2773:                                 )
2774:                                 publishClocktowerSessionView()
2775:                                 cards[targetIndex] = targetCard.copy(eliminatedRound = round)
2776:                                 recordEpistemicObservation(EpistemicObservationDraft(
2777:                                     recordId = "public-alive-${clocktowerGameId}-${localSequence}-$targetSeat",
2778:                                     phase = storytellerPhaseFor(),
2779:                                     round = round,
2780:                                     sequence = localSequence,
2781:                                     sourceSeat = null,
2782:                                     sourceAbility = null,
2783:                                     visibility = ObservationVisibility.PUBLIC,
2784:                                     recipientSeats = emptySet(),
2785:                                     reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
2786:                                     proposition = InformationProposition.AliveAt(targetSeat, false),
2787:                                 ))
2788:                                 records.add(
2789:                                     EliminationRecord(
2790:                                         round,
2791:                                         targetName,
2792:                                         context.getString(R.string.clocktower_record_slayer_hit, playerSeatLabel(cards, claimantName)),
2793:                                     ),
2794:                                 )
2795:                                 val promotedName = if (targetCard.clocktowerTeam == ClocktowerTeam.Demon) {
2796:                                     promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
2797:                                 } else {
2798:                                     null
2799:                                 }
2800:                                 if (promotedName != null) {
2801:                                     clocktowerPendingNightNewDemonIdentityName = promotedName
2802:                                 }
2803:                                 shotOutcome = if (targetCard.clocktowerTeam != ClocktowerTeam.Demon || promotedName != null) null
2804:                                 else evaluateGameOutcome(context, cards, currentGameKind)
2805:                                 addClocktowerEvent(
2806:                                     ClocktowerEventType.RoleAction,
2807:                                     localizedText("杀手命中", "Slayer hit"),
2808:                                     localizedText("${playerSeatLabel(cards, claimantName)} 击杀了 ${playerSeatLabel(cards, targetName)}。", "${playerSeatLabel(cards, claimantName)} killed ${playerSeatLabel(cards, targetName)}."),
2809:                                     listOf(claimantName, targetName),
2810:                                 )
2811:                             } else {
2812:                                 val recordText = when {
2813:                                     slayerDecision.consumesUse && slayerDecision.state == AbilityFunctioningState.POISONED ->
2814:                                         context.getString(R.string.clocktower_record_slayer_poisoned, playerSeatLabel(cards, targetName))
2815:                                     slayerDecision.state != null && !slayerDecision.mayAttempt ->
2816:                                         context.getString(R.string.clocktower_record_slayer_already_used, playerSeatLabel(cards, targetName))
2817:                                     slayerDecision.consumesUse ->
2818:                                         context.getString(R.string.clocktower_record_slayer_miss, playerSeatLabel(cards, targetName))
2819:                                     else ->
2820:                                         context.getString(R.string.clocktower_record_slayer_fake, playerSeatLabel(cards, targetName))
2821:                                 }
2822:                                 records.add(EliminationRecord(round, claimantName, recordText))
2823:                                 addClocktowerEvent(
2824:                                     ClocktowerEventType.RoleAction,
2825:                                     localizedText("杀手行动", "Slayer claim"),
2826:                                     recordText,
2827:                                     listOf(claimantName, targetName),
2828:                                 )
2829:                             }
2830:                             gameOutcome = shotOutcome
2831:                             if (shotOutcome != null) {
2832:                                 showResults = true
2833:                                 addOutcomeEvent(shotOutcome)
2834:                             }
2835:                         },
2836:                         onPreflightVirginExecution = { nominatorName, spyRegistrationWillRecord ->
2837:                             val preflightIndex = cards.indexOfFirst { it.name == nominatorName }
2838:                             val preflightCard = cards.getOrNull(preflightIndex)
2839:                             if (preflightIndex >= 0 && preflightCard != null && preflightCard.eliminatedRound == null) {
2840:                                 preflightClocktowerPublicAliveObservation(
2841:                                     playerName = nominatorName,
2842:                                     eventSequence = clocktowerEventCounter + if (spyRegistrationWillRecord) 2 else 1,
2843:                                 )
2844:                             }
2845:                         },
2846:                         onVirginNomination = { nominatorName, nomineeName, executeNominator ->
2847:                             clocktowerVirginUsed = true
2848:                             advanceClocktowerGameStateRevision()
2849:                             if (executeNominator) {
2850:                                 val index = cards.indexOfFirst { it.name == nominatorName }
2851:                                 val nominatorCard = cards.getOrNull(index)
2852:                                 if (index >= 0 && nominatorCard != null && nominatorCard.eliminatedRound == null) {
2853:                                     requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
2854:                                         targetSeat = index + 1,
2855:                                     )
2856:                                     publishClocktowerSessionView()
2857:                                     cards[index] = nominatorCard.copy(eliminatedRound = round)
2858:                                     records.add(
2859:                                         EliminationRecord(
2860:                                             round,
2861:                                             nominatorName,
2862:                                             context.getString(
2863:                                                 R.string.clocktower_record_virgin_execution,
2864:                                                 playerSeatLabel(cards, nomineeName),
2865:                                             ),
2866:                                         ),
2867:                                     )
2868:                                     addClocktowerEvent(
2869:                                         ClocktowerEventType.Execution,
2870:                                         localizedText("圣女能力处决", "Virgin execution"),
2871:                                         playerSeatLabel(cards, nominatorName),
2872:                                         listOf(nominatorName, nomineeName),
2873:                                     )
2874:                                 }
2875:                                 clocktowerLastExecutedName = nominatorName
2876:                                 val outcome = evaluateGameOutcome(context, cards, currentGameKind)
2877:                                 gameOutcome = outcome
2878:                                 if (outcome != null) {
2879:                                     showResults = true
2880:                                     addOutcomeEvent(outcome)
2881:                                 } else {
2882:                                     val nextRound = round + 1
2883:                                     materializeClocktowerPoisonExpiryAtDusk()
2884:                                     recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
2885:                                     round = nextRound
2886:                                     clocktowerPhase = ClocktowerPhase.Night
2887:                                     resetClocktowerDayFlow()
2888:                                     resetClocktowerNightFlow()
2889:                                 }
2890:                                 clocktowerSelectedExecution = null
2891:                             } else {
2892:                                 records.add(
2893:                                     EliminationRecord(
2894:                                         round,
2895:                                         nomineeName,
2896:                                         context.getString(
2897:                                             R.string.clocktower_record_virgin_spent,
2898:                                             playerSeatLabel(cards, nominatorName),
2899:                                         ),
2900:                                     ),
2901:                                 )
2902:                                 addClocktowerEvent(
2903:                                     ClocktowerEventType.RoleAction,
2904:                                     localizedText("圣女能力已触发", "Virgin ability spent"),
2905:                                     localizedText("${playerSeatLabel(cards, nomineeName)} 的能力已使用，但提名人未被处决。", "${playerSeatLabel(cards, nomineeName)} spent the ability without executing the nominator."),
2906:                                     listOf(nominatorName, nomineeName),
2907:                                 )
2908:                             }
2909:                         },
2910:                         onAdvanceFromFirstNight = {
2911:                             recordClocktowerPhaseAdvance(ClocktowerPhase.Day)
2912:                             clocktowerPhase = ClocktowerPhase.Day
2913:                             advanceClocktowerGameStateRevision()
2914:                             clocktowerPendingNightDeath = null
2915:                             clocktowerDemonAttackDraftTarget = null
2916:                             resetClocktowerNightFlow()
2917:                             resetClocktowerDayFlow()
2918:                         },
2919:                         onConfirmDay = {
2920:                             val preflightExecutionName = clocktowerSelectedExecution
2921:                             val preflightIndex = preflightExecutionName
2922:                                 ?.let { selectedName -> cards.indexOfFirst { it.name == selectedName } }
2923:                                 ?: -1
2924:                             val preflightCard = cards.getOrNull(preflightIndex)
2925:                             val proposedSequence = clocktowerEventCounter + 1
2926:                             val proposedObservationId = if (preflightIndex >= 0) {
2927:                                 "public-alive-${clocktowerGameId}-${proposedSequence}-${preflightIndex + 1}"
2928:                             } else {
2929:                                 ""
2930:                             }
2931:                             val observationAlreadyExists = proposedObservationId.isNotEmpty() &&
2932:                                 clocktowerEpistemicObservations.any { observation ->
2933:                                     observation.recordId == proposedObservationId
2934:                                 }
2935:                             val executionDebugFields = mapOf(
2936:                                 "lastCriticalAction" to "CONFIRM_EXECUTION_CLICKED",
2937:                                 "phaseAtAction" to clocktowerPhase.name,
2938:                                 "roundAtAction" to round.toString(),
2939:                                 "selectedSeat" to if (preflightIndex >= 0) (preflightIndex + 1).toString() else "",
2940:                                 "targetAlive" to (preflightCard?.eliminatedRound == null).toString(),
2941:                                 "eventCounter" to clocktowerEventCounter.toString(),
2942:                                 "gameStateRevision" to clocktowerGameStateRevision.toString(),
2943:                                 "playerInputRevision" to clocktowerPlayerInputRevision.toString(),
2944:                                 "eventCount" to clocktowerEvents.size.toString(),
2945:                                 "observationCount" to clocktowerEpistemicObservations.size.toString(),
2946:                                 "proposedObservationId" to proposedObservationId,
2947:                                 "observationAlreadyExists" to observationAlreadyExists.toString(),
2948:                             )
2949:                             DebugFlightRecorder.updateState(executionDebugFields)
2950:                             DebugFlightRecorder.record(
2951:                                 event = "CONFIRM_EXECUTION_CLICKED",
2952:                                 fields = executionDebugFields,
2953:                             )
2954:                             if (preflightExecutionName != null &&
2955:                                 preflightIndex >= 0 &&
2956:                                 preflightCard != null &&
2957:                                 preflightCard.eliminatedRound == null
2958:                             ) {
2959:                                 DebugFlightRecorder.record(
2960:                                     event = "EXECUTION_PREFLIGHT",
2961:                                     fields = executionDebugFields + mapOf(
2962:                                         "proposedSequence" to proposedSequence.toString(),
2963:                                     ),
2964:                                 )
2965:                                 preflightClocktowerPublicAliveObservation(
2966:                                     playerName = preflightExecutionName,
2967:                                     eventSequence = proposedSequence,
2968:                                 )
2969:                             }
2970:                             // Confirming the day commits its execution/no-execution result and
2971:                             // closes the day decision window, including when no one dies.
2972:                             advanceClocktowerGameStateRevision()
2973:                             val aliveBeforeExecution = cards.filter { it.eliminatedRound == null }
2974:                             val executionName = clocktowerSelectedExecution
2975:                             var executionOutcome: GameOutcome? = null
2976:                             if (executionName != null) {
2977:                                 val index = cards.indexOfFirst { it.name == executionName }
2978:                                 val executedCard = cards.getOrNull(index)
2979:                                 if (index >= 0 && executedCard != null && executedCard.eliminatedRound == null) {
2980:                                     requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
2981:                                         targetSeat = index + 1,
2982:                                     )
2983:                                     publishClocktowerSessionView()
2984:                                     cards[index] = executedCard.copy(eliminatedRound = round)
2985:                                     records.add(EliminationRecord(round, executionName, context.getString(R.string.clocktower_record_execution)))
2986:                                     addClocktowerEvent(
2987:                                         ClocktowerEventType.Execution,
2988:                                         localizedText("处决", "Execution"),
2989:                                     playerSeatLabel(cards, executionName),
2990:                                         listOf(executionName),
2991:                                     )
2992:                                     clocktowerLastExecutedName = executionName
2993:                                     if (executedCard.clocktowerRole?.enName == "Saint") {
2994:                                         executionOutcome = GameOutcome(
2995:                                             title = context.getString(R.string.outcome_clocktower_evil_title),
2996:                                             summary = context.getString(R.string.clocktower_outcome_saint_summary),
2997:                                             reason = context.getString(R.string.clocktower_outcome_saint_reason, executionName),
2998:                                         )
2999:                                     } else if (executedCard.clocktowerRole?.enName == "Klutz") {
3000:                                         clocktowerPendingKlutzName = executionName
3001:                                         clocktowerKlutzChoiceName = null
3002:                                         clocktowerKlutzReturnToDawn = false
3003:                                         clocktowerPhase = ClocktowerPhase.Day
3004:                                         clocktowerDayModeState.value = ClocktowerDayMode.Klutz
3005:                                         executionOutcome = null
3006:                                     } else if (executedCard.clocktowerTeam == ClocktowerTeam.Demon) {
3007:                                         val promotedName = promoteDemonSuccessorIfNeeded(impDeathWasSelfChosen = false)
3008:                                         if (promotedName != null) {
3009:                                             clocktowerPendingNightNewDemonIdentityName = promotedName
3010:                                         }
3011:                                         executionOutcome = if (promotedName == null) {
3012:                                             evaluateGameOutcome(context, cards, currentGameKind)
3013:                                         } else {
3014:                                             null
3015:                                         }
3016:                                     } else {
3017:                                         executionOutcome = evaluateGameOutcome(context, cards, currentGameKind)
3018:                                     }
3019:                                 }
3020:                             } else {
3021:                                 clocktowerLastExecutedName = null
3022:                                 addClocktowerEvent(
3023:                                     ClocktowerEventType.Execution,
3024:                                     localizedText("无人被处决", "No execution"),
3025:                                     "",
3026:                                 )
3027:                             }
3028:                             if (executionName == null && aliveBeforeExecution.size == 3 && aliveBeforeExecution.any {
3029:                                     AbilityFunctioningSemantics.functionsAs(it.abilitySubject(clocktowerConfirmedPoisonTarget), "Mayor")
3030:                                 }
3031:                             ) {
3032:                                 executionOutcome = GameOutcome(
3033:                                     title = context.getString(R.string.outcome_clocktower_good_title),
3034:                                     summary = context.getString(R.string.clocktower_outcome_mayor_summary),
3035:                                     reason = context.getString(R.string.clocktower_outcome_mayor_reason),
3036:                                 )
3037:                             }
3038:                             gameOutcome = executionOutcome
3039:                             if (executionOutcome != null) {
3040:                                 showResults = true
3041:                                 addOutcomeEvent(executionOutcome)
3042:                             } else if (clocktowerPendingKlutzName == null) {
3043:                                 val nextRound = round + 1
3044:                                 materializeClocktowerPoisonExpiryAtDusk()
3045:                                 recordClocktowerPhaseAdvance(ClocktowerPhase.Night, nextRound)
3046:                                 round = nextRound
3047:                                 clocktowerPhase = ClocktowerPhase.Night
3048:                                 resetClocktowerDayFlow()
3049:                                 resetClocktowerNightFlow()
3050:                             }
3051:                             clocktowerSelectedExecution = null
3052:                         },
3053:                         onConfirmNight = {
3054:                             // Dawn resolution commits deaths, role changes and the next phase as
3055:                             // one timeline boundary. Earlier action confirmations have already
3056:                             // revisioned their own facts; this closes the night as a whole.
3057:                             advanceClocktowerGameStateRevision()
3058:                             clocktowerPendingNightNewDemonIdentityName = null
3059:                             val demonPoisonedTonight = clocktowerConfirmedPoisonTarget?.let { name ->
3060:                                 cards.firstOrNull { it.name == name && it.eliminatedRound == null }?.clocktowerTeam == ClocktowerTeam.Demon
3061:                             } == true
3062:                             var nightKlutzName: String? = null
3063:                             var newDemonName: String? = null
3064:                             var unresolvedDemonSuccessor = false
3065:                             val originalDeathName = clocktowerPendingNightDeath
3066:                             val dawnDeathFacts = resolveTroubleBrewingDawnDeathFacts(
3067:                                 cards = cards,
3068:                                 targetName = originalDeathName,
3069:                                 poisonedPlayerName = clocktowerConfirmedPoisonTarget,
3070:                                 monkProtectedTargetName = clocktowerConfirmedMonkProtectedTarget,
3071:                             )
3072:                             val mayorCanRedirect = dawnDeathFacts.mayorSeat != null
3073:                             val baseGameState = cards.toClocktowerGameState(
3074:                                 currentClocktowerScript,
3075:                                 clocktowerGameSeed,
3076:                                 poisonedPlayerName = clocktowerConfirmedPoisonTarget,
3077:                             )
3078:                             val effectiveNightState = ClocktowerEffectiveNightState(
3079:                                 effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
3080:                                     (index + 1).takeIf { card.eliminatedRound == null }
3081:                                 }.toSet(),
3082:                                 effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
3083:                                     card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
3084:                                 }.toMap(),
3085:                             )
3086:                             val demonRoleIds = cards.mapNotNull { card ->
3087:                                 card.clocktowerRole
3088:                                     ?.takeIf { card.clocktowerTeam == ClocktowerTeam.Demon }
3089:                                     ?.let { role -> RoleId(role.enName) }
3090:                             }.toSet()
3091:                             val deathTransition = NightDawnResolutionPlanner.planValidatedNightDeath(
3092:                                 baseGameState = baseGameState,
3093:                                 checkpoint = currentClocktowerNightCheckpoint(),
3094:                                 input = NightDawnDeathResolutionInput(
3095:                                     originalDeathSeat = dawnDeathFacts.originalDeathSeat,
3096:                                     mayorSeat = dawnDeathFacts.mayorSeat,
3097:                                     mayorRedirectMayApply = mayorCanRedirect,
3098:                                     attackOutcome = dawnDeathFacts.attackOutcome,
3099:                                     demonSafeSeats = dawnDeathFacts.demonSafeSeats,
3100:                                     effectiveNightState = effectiveNightState,
3101:                                     demonRoleIds = demonRoleIds,
3102:                                 ),
3103:                             )
3104:                             val resolvedDeathName = deathTransition.dawnCommitIntent?.death?.targetSeat
3105:                                 ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
3106:                             val deathName = resolvedDeathName
3107:                             val safeMayorRedirectName = clocktowerConfirmedMayorRedirectTarget
3108:                                 ?.takeIf { targetName ->
3109:                                     val targetSeat = cards.indexOfFirst { it.name == targetName }
3110:                                         .takeIf { it >= 0 }
3111:                                         ?.plus(1)
3112:                                     mayorCanRedirect &&
3113:                                         resolvedDeathName == null &&
3114:                                         targetSeat != null &&
3115:                                         targetSeat in dawnDeathFacts.demonSafeSeats
3116:                                 }
3117:                             val redirectEventTargetName = when {
3118:                                 mayorCanRedirect && resolvedDeathName != null && resolvedDeathName != originalDeathName -> resolvedDeathName
3119:                                 safeMayorRedirectName != null -> safeMayorRedirectName
3120:                                 else -> null
3121:                             }
3122:                             val dawnDeathMaterialization = NightDawnDurableMaterializationPlanner.plan(
3123:                                 gameId = clocktowerGameId,
3124:                                 round = round,
3125:                                 intent = DawnCommitIntent(death = deathTransition.dawnCommitIntent?.death),
3126:                                 state = DawnDurableMaterializationState(
3127:                                     aliveSeats = cards.mapIndexedNotNull { index, card ->
3128:                                         (index + 1).takeIf { card.eliminatedRound == null }
3129:                                     }.toSet(),
3130:                                     roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
3131:                                         card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
3132:                                     }.toMap(),
3133:                                     currentPhase = storytellerPhaseFor(),
3134:                                     committedActionIds = clocktowerActionTimeline.entries
3135:                                         .map { it.fact.actionId }
3136:                                         .toSet(),
3137:                                     committedObservationRecordIds = clocktowerEpistemicObservations
3138:                                         .map { it.recordId }
3139:                                         .toSet(),
3140:                                 ),
3141:                                 advanceToDawn = false,
3142:                             ).death
3143:                             if (
3144:                                 deathName != null &&
3145:                                 dawnDeathMaterialization?.publicAliveObservationIdToCommit != null
3146:                             ) {
3147:                                 preflightClocktowerPublicAliveObservation(
3148:                                     playerName = deathName,
3149:                                     eventSequence = clocktowerEventCounter + if (redirectEventTargetName != null) 2 else 1,
3150:                                     recordId = dawnDeathMaterialization.publicAliveObservationIdToCommit,
3151:                                 )
3152:                             }
3153:                             if (redirectEventTargetName != null) {
3154:                                 addClocktowerEvent(
3155:                                     ClocktowerEventType.RoleAction,
3156:                                     localizedText("市长死亡转移", "Mayor death redirect"),
3157:                                     localizedText(
3158:                                         playerSeatLabel(cards, originalDeathName) + " → " + playerSeatLabel(cards, redirectEventTargetName),
3159:                                         playerSeatLabel(cards, originalDeathName) + " → " + playerSeatLabel(cards, redirectEventTargetName),
3160:                                     ),
3161:                                     listOfNotNull(originalDeathName, redirectEventTargetName),
3162:                                 )
3163:                             }
3164:                             if (demonPoisonedTonight) {
3165:                                 clocktowerPendingNightDeath?.let { targetName ->
3166:                                     addClocktowerEvent(
3167:                                         ClocktowerEventType.RoleAction,
3168:                                         localizedText("恶魔击杀", "Demon kill"),
3169:                                         localizedText(
3170:                                             "${playerSeatLabel(cards, targetName)} · 失败（恶魔中毒）",
3171:                                             "${playerSeatLabel(cards, targetName)} · failed (Demon poisoned)",
3172:                                         ),
3173:                                         listOfNotNull(clocktowerConfirmedPoisonTarget, targetName),
3174:                                     )
3175:                                 }
3176:                                 clocktowerPendingNightDeath = null
3177:                             }
3178:                             var detailedAttackFailureRecorded = false
3179:                             val canonicalOriginalDeathSeat = dawnDeathFacts.originalDeathSeat
3180:                             val failedProtectedAttackName = when {
3181:                                 demonPoisonedTonight || deathName != null -> null
3182:                                 safeMayorRedirectName != null -> safeMayorRedirectName
3183:                                 canonicalOriginalDeathSeat != null &&
3184:                                     canonicalOriginalDeathSeat in dawnDeathFacts.demonSafeSeats -> originalDeathName
3185:                                 else -> null
3186:                             }
3187:                             if (failedProtectedAttackName != null) {
3188:                                 val failedAttackCard = cards.firstOrNull { it.name == failedProtectedAttackName }
3189:                                 val apparentMonk = cards.firstOrNull {
3190:                                     AbilityFunctioningSemantics.interactsAs(
3191:                                         it.abilitySubject(clocktowerConfirmedPoisonTarget),
3192:                                         "Monk",
3193:                                     )
3194:                                 }
3195:                                 val protectedByMonkForRecord = AbilityFunctioningSemantics.selectedMechanicalEffectApplies(
3196:                                     subject = apparentMonk?.abilitySubject(clocktowerConfirmedPoisonTarget),
3197:                                     role = "Monk",
3198:                                     selectionMatches = clocktowerConfirmedMonkProtectedTarget == failedProtectedAttackName,
3199:                                 )
3200:                                 val protectedBySoldierForRecord = failedAttackCard?.let { card ->
3201:                                     AbilityFunctioningSemantics.functionsAs(
3202:                                         card.abilitySubject(clocktowerConfirmedPoisonTarget),
3203:                                         "Soldier",
3204:                                     )
3205:                                 } == true
3206:                                 val protectionNote = when {
3207:                                     protectedBySoldierForRecord -> context.getString(R.string.clocktower_record_soldier_safe)
3208:                                     protectedByMonkForRecord -> context.getString(R.string.clocktower_record_monk_protected)
3209:                                     else -> null
3210:                                 }
3211:                                 if (protectionNote != null) {
3212:                                     records.add(EliminationRecord(round, failedProtectedAttackName, protectionNote))
3213:                                     addClocktowerEvent(
3214:                                         ClocktowerEventType.RoleAction,
3215:                                         localizedText("恶魔击杀", "Demon kill"),
3216:                                         localizedText(
3217:                                             "${playerSeatLabel(cards, failedProtectedAttackName)} · 失败（$protectionNote）",
3218:                                             "${playerSeatLabel(cards, failedProtectedAttackName)} · failed ($protectionNote)",
3219:                                         ),
3220:                                         listOf(failedProtectedAttackName),
3221:                                     )
3222:                                     clocktowerPendingNightDeath = null
3223:                                     detailedAttackFailureRecorded = true
3224:                                 }
3225:                             }
3226:                             if (deathName != null) {
3227:                                 clocktowerPendingNightDeath = deathName
3228:                                 val index = cards.indexOfFirst { it.name == deathName }
3229:                                 val nightDeathCard = cards.getOrNull(index)
3230:                                 if (index >= 0 && nightDeathCard != null && dawnDeathMaterialization != null) {
3231:                                     val demonDied = nightDeathCard.clocktowerTeam == ClocktowerTeam.Demon
3232:                                     val impSelfChosen = demonDied && originalDeathName == deathName
3233:                                     val deathLocalSequence = clocktowerEventCounter + 1
3234:                                     dawnDeathMaterialization.actionIdToCommit?.let { actionId ->
3235:                                         recordClocktowerAction(ActionFactDraft.Death(
3236:                                             actionId = actionId,
3237:                                             phase = storytellerPhaseFor(),
3238:                                             round = round,
3239:                                             sequence = deathLocalSequence,
3240:                                             targetSeat = dawnDeathMaterialization.intent.targetSeat,
3241:                                         ))
3242:                                     }
3243:                                     if (dawnDeathMaterialization.stateMutationRequired) {
3244:                                         requireClocktowerGameSession().synchronizePlayerDeathWithinCurrentRevision(
3245:                                             targetSeat = dawnDeathMaterialization.intent.targetSeat,
3246:                                         )
3247:                                         publishClocktowerSessionView()
3248:                                         cards[index] = nightDeathCard.copy(eliminatedRound = round)
3249:                                         records.add(EliminationRecord(round, deathName, context.getString(R.string.clocktower_record_night_death)))
3250:                                         addClocktowerEvent(
3251:                                             ClocktowerEventType.Death,
3252:                                             localizedText("恶魔击杀", "Demon kill"),
3253:                                             localizedText(
3254:                                                 "${playerSeatLabel(cards, deathName)} · 死亡",
3255:                                                 "${playerSeatLabel(cards, deathName)} · killed",
3256:                                             ),
3257:                                             listOf(deathName),
3258:                                             projectSemanticHistory = false,
3259:                                         )
3260:                                     }
3261:                                     if (dawnDeathMaterialization.publicAliveObservationIdToCommit != null) {
3262:                                         recordEpistemicObservation(EpistemicObservationDraft(
3263:                                             recordId = dawnDeathMaterialization.publicAliveObservationIdToCommit,
3264:                                             phase = storytellerPhaseFor(),
3265:                                             round = round,
3266:                                             sequence = deathLocalSequence,
3267:                                             sourceSeat = null,
3268:                                             sourceAbility = null,
3269:                                             visibility = ObservationVisibility.PUBLIC,
3270:                                             recipientSeats = emptySet(),
3271:                                             reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
3272:                                             proposition = InformationProposition.AliveAt(
3273:                                                 dawnDeathMaterialization.intent.targetSeat,
3274:                                                 false,
3275:                                             ),
3276:                                         ))
3277:                                     }
3278:                                     if (demonDied) {
3279:                                         if (impSelfChosen) {
3280:                                             val demonRoleId = RoleId(requireNotNull(nightDeathCard.clocktowerRole).enName)
3281:                                             val successionResolution = resolveTroubleBrewingImpSelfKillSuccession(
3282:                                                 baseGameState = baseGameState,
3283:                                                 checkpoint = currentClocktowerNightCheckpoint(),
3284:                                                 demonRoleId = demonRoleId,
3285:                                             )
3286:                                             val successionTransition = NightDawnResolutionPlanner.planDemonSuccession(
3287:                                                 baseGameState = baseGameState,
3288:                                                 checkpoint = currentClocktowerNightCheckpoint(),
3289:                                                 successionResolution = successionResolution,
3290:                                                 demonRoleId = demonRoleId,
3291:                                             )
3292:                                             clocktowerPendingNewDemonName = successionTransition.checkpoint.pendingNewDemonName
3293:                                             clocktowerDemonSuccessorTarget = successionTransition.checkpoint.demonSuccessorDraftTarget
3294:                                             clocktowerConfirmedDemonSuccessorTarget = successionTransition.checkpoint.confirmedDemonSuccessorTarget
3295:                                             newDemonName = successionTransition.checkpoint.pendingNewDemonName
3296:                                             unresolvedDemonSuccessor =
3297:                                                 successionTransition.continuation == NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR
3298:                                         } else {
3299:                                             newDemonName = promoteDemonSuccessorIfNeeded(
3300:                                                 impDeathWasSelfChosen = false,
3301:                                             )
3302:                                         }
3303:                                     }
3304:                                     if (nightDeathCard.clocktowerRole?.enName == "Klutz") {
3305:                                         nightKlutzName = deathName
3306:                                     }
3307:                                     if (
3308:                                         AbilityFunctioningSemantics.interactsAs(
3309:                                             nightDeathCard.abilitySubject(clocktowerConfirmedPoisonTarget),
3310:                                             "Ravenkeeper",
3311:                                         ) && clocktowerRavenkeeperTarget != null
3312:                                     ) {
3313:                                         records.add(
3314:                                             EliminationRecord(
3315:                                                 round,
3316:                                                 deathName,
3317:                                                 context.getString(
3318:                                                     R.string.clocktower_record_ravenkeeper_check,
3319:                                                     clocktowerRavenkeeperTarget!!,
3320:                                                 ),
3321:                                             ),
3322:                                         )
3323:                                     }
3324:                                 } else {
3325:                                     clocktowerPendingNightDeath = null
3326:                                     addClocktowerEvent(
3327:                                         ClocktowerEventType.Death,
3328:                                         localizedText("平安夜", "No night death"),
3329:                                         "",
3330:                                     )
3331:                                 }
3332:                             } else if (
3333:                                 !demonPoisonedTonight &&
3334:                                 !detailedAttackFailureRecorded &&
3335:                                 clocktowerPhase != ClocktowerPhase.FirstNight
3336:                             ) {
3337:                                 addClocktowerEvent(
3338:                                     ClocktowerEventType.Death,
3339:                                     localizedText("平安夜", "No night death"),
3340:                                     "",
3341:                                 )
3342:                             }
3343:                             val dawnPoisoner = cards.mapIndexedNotNull { index, card ->
3344:                                 card.clocktowerRole
3345:                                     ?.takeIf { role -> role.enName == "Poisoner" }
3346:                                     ?.let { role -> index + 1 to role }
3347:                             }.firstOrNull()
3348: 
3349:                             val postDeathEffectiveNightState = ClocktowerEffectiveNightState(
3350:                                 effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
3351:                                     (index + 1).takeIf { card.eliminatedRound == null }
3352:                                 }.toSet(),
3353:                                 effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
3354:                                     card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
3355:                                 }.toMap(),
3356:                             )
3357: 
3358:                             val durablePreviousPoisonTargetSeat =
3359:                                 NightDawnPoisonRecoveryAuthority.latestTargetSeatForRound(
3360:                                     actionTimeline = clocktowerActionTimeline,
3361:                                     round = round,
3362:                                 )
3363: 
3364:                             val ordinaryDawnPoisonIntent = dawnPoisoner?.let { (poisonerSeat, poisonerRole) ->
3365:                                 NightDawnResolutionPlanner.planPoisonCarry(
3366:                                     baseGameState = baseGameState,
3367:                                     checkpoint = currentClocktowerNightCheckpoint(),
3368:                                     input = NightDawnPoisonResolutionInput(
3369:                                         poisonerSeat = poisonerSeat,
3370:                                         poisonerRoleId = RoleId(poisonerRole.enName),
3371:                                         effectiveNightState = postDeathEffectiveNightState,
3372:                                     ),
3373:                                     durablePreviousPoisonTargetSeat = durablePreviousPoisonTargetSeat,
3374:                                 )
3375:                             }
3376: 
3377:                             ordinaryDawnPoisonIntent?.let { poisonIntent ->
3378:                                 val poisonMaterialization = requireNotNull(
3379:                                     NightDawnDurableMaterializationPlanner.plan(
3380:                                         gameId = clocktowerGameId,
3381:                                         round = round,
3382:                                         intent = DawnCommitIntent(poisonCarry = poisonIntent),
3383:                                         state = DawnDurableMaterializationState(
3384:                                             aliveSeats = cards.mapIndexedNotNull { index, card ->
3385:                                                 (index + 1).takeIf { card.eliminatedRound == null }
3386:                                             }.toSet(),
3387:                                             roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
3388:                                                 card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
3389:                                             }.toMap(),
3390:                                             currentPhase = storytellerPhaseFor(),
3391:                                             currentPoisonTargetSeat =
3392:                                                 clocktowerConfirmedPoisonTarget?.let(::clocktowerSeatFor),
3393:                                             committedActionIds = clocktowerActionTimeline.entries
3394:                                                 .map { it.fact.actionId }
3395:                                                 .toSet(),
3396:                                             committedObservationRecordIds = clocktowerEpistemicObservations
3397:                                                 .map { it.recordId }
3398:                                                 .toSet(),
3399:                                         ),
3400:                                         advanceToDawn = false,
3401:                                     ).poison,
3402:                                 )
3403: 
3404:                                 poisonMaterialization.actionIdToCommit?.let { actionId ->
3405:                                     val localSequence = clocktowerEventCounter + 1
3406:                                     recordClocktowerAction(
3407:                                         ActionFactDraft.Poison(
3408:                                             actionId = actionId,
3409:                                             phase = storytellerPhaseFor(),
3410:                                             round = round,
3411:                                             sequence = localSequence,
3412:                                             targetSeat = poisonMaterialization.intent.targetSeat,
3413:                                         ),
3414:                                     )
3415:                                 }
3416: 
3417:                                 if (poisonMaterialization.stateMutationRequired) {
3418:                                     requireClocktowerGameSession().synchronizePoisonTargetWithinCurrentRevision(
3419:                                         targetSeat = poisonMaterialization.intent.targetSeat,
3420:                                     )
3421:                                     publishClocktowerSessionView()
3422:                                     val poisonTargetName = poisonMaterialization.intent.targetSeat
3423:                                         ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }
3424:                                     clocktowerConfirmedPoisonTarget = poisonTargetName
3425:                                     clocktowerPoisonTarget = poisonTargetName
3426:                                 }
3427:                             }
3428: 
3429:                             if (nightKlutzName != null) {
3430:                                 clocktowerPendingKlutzName = nightKlutzName
3431:                                 clocktowerKlutzChoiceName = null
3432:                                 clocktowerKlutzReturnToDawn = true
3433:                                 recordClocktowerPhaseAdvance(ClocktowerPhase.Day)
3434:                                 clocktowerPhase = ClocktowerPhase.Day
3435:                                 clocktowerDayModeState.value = ClocktowerDayMode.Klutz
3436:                             }
3437:                             val nightOutcome =
3438:                                 if (
3439:                                     nightKlutzName == null &&
3440:                                     newDemonName == null &&
3441:                                     !unresolvedDemonSuccessor
3442:                                 ) {
3443:                                     evaluateGameOutcome(context, cards, currentGameKind)
3444:                                 } else {
3445:                                     null
3446:                                 }
3447:                             gameOutcome = nightOutcome
3448:                             if (nightOutcome != null) {
3449:                                 showResults = true
3450:                                 addOutcomeEvent(nightOutcome)
3451:                             } else if (nightKlutzName == null && newDemonName != null) {
3452:                                 clocktowerPendingNewDemonName = newDemonName
3453:                             } else if (nightKlutzName == null && !unresolvedDemonSuccessor) {
3454:                                 val dawnPhasePlan = NightDawnDurableMaterializationPlanner.plan(
3455:                                     gameId = clocktowerGameId,
3456:                                     round = round,
3457:                                     intent = DawnCommitIntent(),
3458:                                     state = DawnDurableMaterializationState(
3459:                                         aliveSeats = cards.mapIndexedNotNull { index, card ->
3460:                                             (index + 1).takeIf { card.eliminatedRound == null }
3461:                                         }.toSet(),
3462:                                         roleIdsBySeat = cards.mapIndexedNotNull { index, card ->
3463:                                             card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
3464:                                         }.toMap(),
3465:                                         currentPhase = storytellerPhaseFor(),
3466:                                         committedActionIds = clocktowerActionTimeline.entries
3467:                                             .map { it.fact.actionId }
3468:                                             .toSet(),
3469:                                         committedObservationRecordIds = clocktowerEpistemicObservations
3470:                                             .map { it.recordId }
3471:                                             .toSet(),
3472:                                     ),
3473:                                     advanceToDawn = true,
3474:                                 )
3475:                                 val phaseAdvance = requireNotNull(dawnPhasePlan.phaseAdvance)
3476:                                 phaseAdvance.actionIdToCommit?.let { actionId ->
3477:                                     val localSequence = clocktowerEventCounter + 1
3478:                                     recordClocktowerAction(ActionFactDraft.PhaseAdvance(
3479:                                         actionId = actionId,
3480:                                         phase = storytellerPhaseFor(),
3481:                                         round = round,
3482:                                         sequence = localSequence,
3483:                                         nextPhase = phaseAdvance.targetPhase,
3484:                                         nextRound = round,
3485:                                     ))
3486:                                 }
3487:                                 if (phaseAdvance.stateMutationRequired) {
3488:                                     clocktowerPhase = ClocktowerPhase.Dawn
3489:                                 }
3490:                                 resetClocktowerNightFlow()
3491:                             }
3492:                             clocktowerFortuneTellerFirst = null
3493:                             clocktowerFortuneTellerSecond = null
3494:                             clocktowerChambermaidFirst = null
3495:                             clocktowerChambermaidSecond = null
3496:                             clocktowerRavenkeeperTarget = null
3497:                             clocktowerMonkProtectedTarget = null
3498:                             clocktowerConfirmedMonkProtectedTarget = null
3499:                             clocktowerMayorRedirectTarget = null
3500:                             clocktowerConfirmedMayorRedirectTarget = null
3501:                             if (clocktowerPendingNewDemonName == null) {
3502:                                 clocktowerDemonSuccessorTarget = null
3503:                                 clearConfirmedDemonSuccessorTarget()
3504:                             }
3505:                         },
3506:                     )
3507: 
3508:                     Screen.Game -> GameScreen(
3509:                     gameKind = currentGameKind,
3510:                     onHostTools = {
3511:                         hostToolTab = HostToolTab.Roles
3512:                         showHostTools = true
3513:                     },
3514:                     cards = cards,
3515:                     records = records,
3516:                     round = round,
3517:                     gameOutcome = gameOutcome,
3518:                     selectedElimination = selectedElimination,
3519:                     onSelectElimination = { selectedElimination = it },
3520:                     onConfirmElimination = {
3521:                         val name = selectedElimination
3522:                         if (name != null) {
3523:                             val index = cards.indexOfFirst { it.name == name }
3524:                             if (index >= 0) {
3525:                                 cards[index] = cards[index].copy(eliminatedRound = round)
3526:                                 records.add(EliminationRecord(round, name))
3527:                                 selectedElimination = null
3528:                                 gameOutcome = evaluateGameOutcome(context, cards, currentGameKind)
3529:                                 if (gameOutcome != null) {
3530:                                     showResults = true
3531:                                 }
3532:                                 round += 1
3533:                             }
3534:                         }
3535:                     },
3536:                     onShowResults = {
3537:                         gameOutcome = gameOutcome ?: GameOutcome(
3538:                             title = context.getString(R.string.outcome_manual_title),
3539:                             summary = context.getString(R.string.outcome_manual_summary),
3540:                             reason = context.getString(R.string.outcome_manual_reason),
3541:                         )
3542:                         showResults = true
3543:                     },
3544:                 )
3545:                         }
3546:                     }
3547:                 }
3548: 
3549:                 if (showResults) {
3550:                     if (currentGameKind == GameKind.Clocktower) {
3551:                         ClocktowerResultsDialog(
3552:                             cards = cards,
3553:                             outcome = gameOutcome,
3554:                             onDismiss = { showResults = false },
3555:                             onReview = {
3556:                                 showResults = false
3557:                                 hostToolTab = HostToolTab.Records
3558:                                 showHostTools = true
3559:                             },
3560:                             onNewGame = { showNewGameConfirmation = true },
3561:                         )
3562:                     } else {
3563:                         ResultsDialog(
3564:                             gameKind = currentGameKind,
3565:                             cards = cards,
3566:                             outcome = gameOutcome,
3567:                             onDismiss = { showResults = false },
3568:                             onReview = {
3569:                                 showResults = false
3570:                                 hostToolTab = HostToolTab.Records
3571:                                 showHostTools = true
3572:                             },
3573:                             onNewGame = { showNewGameConfirmation = true },
3574:                         )
3575:                     }
3576:                 }
3577: 
3578:                 if (showHostTools) {
3579:                     HostGameToolsScreen(
3580:                         gameKind = currentGameKind,
3581:                         cards = cards,
3582:                         records = records,
3583:                         events = clocktowerEvents,
3584:                         history = gameHistory,
3585:                         initialTab = hostToolTab,
3586:                         settingsContent = {
3587:                             SettingsContent(
3588:                                 languageMode = languageMode,
3589:                                 storytellerExperienceMode = storytellerExperienceMode,
3590:                                 commonPlayers = commonPlayers,
3591:                                 newCommonPlayerName = newCommonPlayerName,
3592:                                 onLanguageModeChange = { nextMode ->
3593:                                     languageMode = nextMode
3594:                                     baseContext.saveLanguageMode(nextMode)
3595:                                 },
3596:                                 onStorytellerExperienceModeChange = { mode ->
3597:                                     storytellerExperienceMode = mode
3598:                                     baseContext.saveStorytellerExperienceMode(mode)
3599:                                 },
3600:                                 onNewCommonPlayerNameChange = { newCommonPlayerName = it },
3601:                                 onAddCommonPlayer = ::addCommonPlayer,
3602:                                 onRemoveCommonPlayer = ::removeCommonPlayer,
3603:                             )
3604:                         },
3605:                         onDismiss = { showHostTools = false },
3606:                         onNewGame = {
3607:                             showHostTools = false
3608:                             showNewGameConfirmation = true
3609:                         },
3610:                     )
3611:                 }
3612: 
3613:                 if (showNewGameConfirmation) {
3614:                     NewGameConfirmationDialog(
3615:                         gameKind = currentGameKind,
3616:                         onDismiss = { showNewGameConfirmation = false },
3617:                         onManagePlayers = ::archiveAndReturnToPlayerManagement,
3618:                         onQuickRestart = ::archiveAndStartNewGame,
3619:                     )
3620:                 }
3621:             }
3622:         }
3623:     }
3624: }
3625: 
3626: private fun evaluateGameOutcome(context: Context, cards: List<PlayerCard>, gameKind: GameKind): GameOutcome? {
3627:     val activeCards = cards.filter { it.eliminatedRound == null }
3628:     if (gameKind == GameKind.Clocktower) {
3629:         val activeDemons = activeCards.count { it.clocktowerTeam == ClocktowerTeam.Demon }
3630:         return when {
3631:             activeDemons == 0 -> GameOutcome(
3632:                 title = context.getString(R.string.outcome_clocktower_good_title),
3633:                 summary = context.getString(R.string.outcome_clocktower_good_summary),
3634:                 reason = context.getString(R.string.outcome_clocktower_good_reason),
3635:             )
3636: 
3637:             activeCards.size <= 2 -> GameOutcome(
3638:                 title = context.getString(R.string.outcome_clocktower_evil_title),
3639:                 summary = context.getString(R.string.outcome_clocktower_evil_summary),
3640:                 reason = context.getString(R.string.outcome_clocktower_evil_reason, activeCards.size, activeDemons),
3641:             )
3642: 
3643:             else -> null
3644:         }
3645:     }
3646: 
3647:     if (gameKind == GameKind.Werewolf) {
3648:         val activeWerewolves = activeCards.count { it.role == Role.Werewolf }
3649:         val activeGoodPlayers = activeCards.size - activeWerewolves
3650:         return when {
3651:             activeWerewolves == 0 -> GameOutcome(
3652:                 title = context.getString(R.string.outcome_good_title),
3653:                 summary = context.getString(R.string.outcome_good_summary),
3654:                 reason = context.getString(R.string.outcome_good_reason, activeGoodPlayers),
3655:             )
3656: 
3657:             activeWerewolves >= activeGoodPlayers -> GameOutcome(
3658:                 title = context.getString(R.string.outcome_werewolf_title),
3659:                 summary = context.getString(R.string.outcome_werewolf_summary),
3660:                 reason = context.getString(R.string.outcome_werewolf_reason, activeWerewolves, activeGoodPlayers),
3661:             )
3662: 
3663:             else -> null
3664:         }
3665:     }
3666: 
3667:     val activeCivilians = activeCards.count { it.role == Role.Civilian }
3668:     val activeUndercovers = activeCards.count { it.role == Role.Undercover }
3669:     val activeBlanks = activeCards.count { it.role == Role.Blank }
3670: 
3671:     return when {
3672:         activeUndercovers == 0 && activeBlanks == 0 -> GameOutcome(
3673:             title = context.getString(R.string.outcome_civilian_title),
3674:             summary = context.getString(R.string.outcome_civilian_summary),
3675:             reason = context.getString(R.string.outcome_civilian_reason, activeCivilians),
3676:         )
3677: 
3678:         activeUndercovers > 0 && activeUndercovers >= activeCivilians -> GameOutcome(
3679:             title = context.getString(R.string.outcome_undercover_title),
3680:             summary = context.getString(R.string.outcome_undercover_summary),
3681:             reason = context.getString(R.string.outcome_undercover_reason, activeUndercovers, activeCivilians),
3682:         )
3683: 
3684:         activeCivilians == 0 && activeUndercovers == 0 && activeBlanks > 0 -> GameOutcome(
3685:             title = context.getString(R.string.outcome_blank_title),
3686:             summary = context.getString(R.string.outcome_blank_summary),
3687:             reason = context.getString(R.string.outcome_blank_reason, activeBlanks),
3688:         )
3689: 
3690:         else -> null
3691:     }
3692: }
3693: 
3694: 
3695: @Composable
3696: internal fun EmptyStateCard(text: String) {
3697:     Card(
3698:         shape = RoundedCornerShape(8.dp),
3699:         colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
3700:         elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
3701:     ) {
3702:         Text(
3703:             text = text,
3704:             modifier = Modifier
3705:                 .fillMaxWidth()
3706:                 .padding(14.dp),
3707:             color = Color(0xFF6F7B74),
3708:         )
3709:     }
3710: }
3711: 
3712: @Composable
3713: internal fun EliminationRecord.displayText(): String {
3714:     val base = stringResource(R.string.elimination_record_format, round, playerName)
3715:     return note?.let { stringResource(R.string.elimination_record_with_note_format, base, it) } ?: base
3716: }
3717: 
3718: internal fun PlayerCard.hostRoleLabel(context: Context, gameKind: GameKind): String = when (gameKind) {
3719:     GameKind.Clocktower -> actualRoleLabel ?: roleLabel ?: context.getString(role.labelResId())
3720:     GameKind.Werewolf -> roleLabel ?: context.getString(role.labelResId())
3721:     GameKind.Undercover -> context.getString(role.labelResId())
3722: }
3723: 
3724: internal fun PlayerCard.seatLabel(cards: List<PlayerCard>): String =
3725:     "#${cards.indexOfFirst { it.name == name } + 1} $name"
3726: 
3727: internal fun playerSeatLabel(cards: List<PlayerCard>, playerName: String?): String =
3728:     cards.firstOrNull { it.name == playerName }?.seatLabel(cards) ?: playerName.orEmpty()
