package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.session.OpeningPlayerExperienceV1
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicChangeCheckpointV2
import com.codex.campboardgamehost.clocktower.setup.NoGreaterJoyProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDealRoleResolver
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPreparedSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupCommitter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

internal enum class Screen {
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

internal enum class LanguageMode(val prefsValue: String) {
    System("system"),
    Chinese("zh"),
    English("en"),
}

private fun Context.playerName(number: Int): String = getString(R.string.default_player_name_format, number)

internal const val MIN_CLOCKTOWER_PLAYERS = 5
internal const val MAX_PLAYERS = 15

private fun Context.localized(languageMode: LanguageMode): Context {
    if (languageMode == LanguageMode.System) return this
    val locale = Locale(languageMode.prefsValue)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

private fun defaultClocktowerScriptFor(playerCount: Int): ClocktowerScript =
    if (playerCount in 5..6) ClocktowerScript.NoGreaterJoy else ClocktowerScript.TroubleBrewing

internal fun canStartClocktowerScript(script: ClocktowerScript): Boolean =
    script == ClocktowerScript.TroubleBrewing || script == ClocktowerScript.NoGreaterJoy

@Composable
internal fun CampBoardGameHostApp(
    newClocktowerSeed: () -> Long = { UUID.randomUUID().let { uuid ->
        (uuid.mostSignificantBits xor uuid.leastSignificantBits).takeIf { it != 0L } ?: 1L
    } },
) {
    val baseContext = LocalContext.current
    val appPreferencesStore = remember(baseContext) { AppPreferencesStore(baseContext) }
    val aiSettings = rememberStorytellerAiSettings(baseContext)
    val aiConversation = remember { ClocktowerAiConversation() }
    val openingDraft = remember { ClocktowerOpeningDraft() }
    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
    var languageMode by remember { mutableStateOf(appPreferencesStore.loadLanguageMode()) }
    val context = remember(languageMode) { baseContext.localized(languageMode) }
    val language = context.resources.configuration.locales[0].language
    var screen by remember { mutableStateOf(Screen.Landing) }
    val activeGameStore = remember(baseContext) { ClocktowerActiveGameStore.fromContext(baseContext) }
    var activeGameRestoreError by remember { mutableStateOf(false) }
    var activeGameSaveError by remember { mutableStateOf(false) }
    var settingsReturnScreen by remember { mutableStateOf(Screen.Setup) }
    var pendingTroubleBrewingDrunkSelection by openingDraft::pendingTroubleBrewingDrunkSelection
    // Per-game takeover stays separate from the saved default for future games.
    var storytellerOperationMode by remember { mutableStateOf(aiSettings.preferredMode) }
    var pendingJointOpening by openingDraft::pendingJointOpening
    var jointOpeningResult by openingDraft::jointOpeningResult
    // Opaque provider IDs are provisional until Host accepts the opening. Never
    // persist them to game facts, logs or player's visible information.
    var jointOpeningBusy by openingDraft::jointOpeningBusy
    var jointOpeningError by openingDraft::jointOpeningError
    var jointRevealIndex by openingDraft::jointRevealIndex
    // An advisory opening plan is never a committed package of future facts.
    // Current-step recommendation only: never a separate adoption or game-state fact.
    var shownOnlyDealCompleted by openingDraft::shownOnlyDealCompleted
    val drunkAiScope = rememberCoroutineScope()
    var showHostTools by remember { mutableStateOf(false) }
    var hostToolTab by remember { mutableStateOf(HostToolTab.Roles) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var currentDealIndex by remember { mutableStateOf(0) }
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var clocktowerSessionUi by remember { mutableStateOf<ClocktowerSessionUiState?>(null) }
    fun replaceClocktowerSession(session: ClocktowerGameSession?) {
        clocktowerSessionUi?.deactivate()
        clocktowerSessionUi = session?.let { ClocktowerSessionUiState(it) { activeGameSaveError = true } }
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { clocktowerSessionUi?.deactivate() }
    }
    val clocktowerSessionState = clocktowerSessionUi?.snapshot
    val round = clocktowerSessionState?.round ?: 1
    val currentClocktowerScript = clocktowerSessionState?.gameState?.script
        ?.let { scriptId ->
            ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
        }
        ?: ClocktowerScript.TroubleBrewing
    val clocktowerGameId = clocktowerSessionState?.gameId.orEmpty()
    var committedTroubleBrewingSetupRotationRecord by remember {
        mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
    }
    var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
    var showResults by remember { mutableStateOf(false) }
    val gameOutcome = clocktowerSessionState?.let { it.outcome?.present(context, it) }
    LaunchedEffect(gameOutcome) { if (gameOutcome != null) showResults = true }
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
    val cards: List<PlayerCard> = remember(setupCards, clocktowerSessionState, language) {
        ClocktowerSessionPlayerCards(
            setupCards = setupCards,
            currentSession = {
                clocktowerSessionUi?.snapshot
            },
            language = language,
        )
    }
    fun requireClocktowerGameSession(): ClocktowerGameSession =
        requireNotNull(clocktowerSessionUi?.session) {
            "Clocktower session authority is unavailable."
        }

    val playerCount = playerNames.size

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

    fun localizedText(zh: String, en: String): String = if (language == "en") en else zh

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
        pendingTroubleBrewingDrunkSelection = null
        aiConversation.pendingCurrentStepAdvice = null
        committedTroubleBrewingSetupRotationRecord = null
        aiConversation.canonicalConversationResponseId = null
        aiConversation.canonicalConversationModel = null
        replaceClocktowerSession(null)
        currentDealIndex = 0
        showResults = false

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
            replaceClocktowerSession(ClocktowerGameSession.createProduction(
                gameId = gameId,
                gameSeed = gameSeed,
                initialState = initialGameState,
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                initialPlayerContextBySeat = cards.mapIndexedNotNull { index, card ->
                    val level = playerExperience(card.name)
                    if (level == PlayerExperienceLevelV1.BEGINNER) null
                    else (index + 1) to StorytellerPlayerContextInputV1(experienceLevel = level)
                }.toMap(),
            ))
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
        

        screen = Screen.PassPhone
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
    }

    /** 3C: the only INITIAL paid model request for personal-direct TB opening. */
    fun requestJointOpening(pending: PendingTroubleBrewingJointOpening) = openingDraft.requestJointOpening(
        pending, aiSettings, activeGameClocktowerRulesetCatalog,
        { storytellerOperationMode }, { screen }, drunkAiScope,
    )
    fun confirmJointOpeningDrunk(pending: PendingTroubleBrewingJointOpening, candidate: TroubleBrewingDrunkCandidate?) =
        openingDraft.confirmJointOpeningDrunk(pending, candidate, aiConversation, activeGameClocktowerRulesetCatalog,
            { storytellerOperationMode }, { clocktowerSessionUi?.session }, ::commitAndStartTroubleBrewingGame, { screen = it })
    fun beginJointOpeningAtDrunkStep() = openingDraft.beginJointOpeningAtDrunkStep(
        { screen }, { storytellerOperationMode }, aiConversation, activeGameClocktowerRulesetCatalog,
        { clocktowerSessionUi?.session }, ::commitAndStartTroubleBrewingGame, { screen = it },
    )
    fun takeOverJointOpeningManually() = openingDraft.takeOverJointOpeningManually(
        { screen }, aiConversation, { storytellerOperationMode = it }, ::commitAndStartTroubleBrewingGame, { screen = it },
    )

    fun startTroubleBrewingGame() {
        // Both AI modes must enter the one-request shown-role-first joint path.
        // A legacy Gateway configuration must NOT fall through into the paid
        // Drunk-only request followed by a second global analysis.
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL &&
            (!aiSettings.directEnabled || !aiSettings.directLoaded || aiSettings.token.isBlank() ||
                aiSettings.model.isNullOrBlank())
        ) return
        openingDraft.reset(aiConversation)
        val playerNames = hostSeatingSetupFlow.playerNamesFor()
        val preparedSeed = newClocktowerSeed()
        val preparedGameId = UUID.randomUUID().toString()

        val preparedSetup = openingDraft.prepare(baseContext, playerNames, preparedSeed,
            activeGameClocktowerRulesetCatalog)
        openingDraft.beginPrepared(
            preparedGameId, preparedSetup,
            playerNames.mapIndexed { index, name -> index + 1 to openingExperience(name) }.toMap(),
            language, aiSettings, activeGameClocktowerRulesetCatalog,
            { storytellerOperationMode }, { screen }, drunkAiScope, ::commitAndStartTroubleBrewingGame, { screen = it },
        )
    }

    fun readyForNewClocktowerGame(script: ClocktowerScript): Boolean =
        storytellerNewGameConfigurationReady(
            mode = storytellerModeForNewGame(aiSettings.preferredMode, script),
            directEnabled = aiSettings.directEnabled,
            directLoaded = aiSettings.directLoaded,
            directModel = aiSettings.directModel,
            directKey = aiSettings.directKey,
            gatewayLoaded = aiSettings.gatewayLoaded,
            gatewayEndpoint = aiSettings.gatewayEndpoint,
            gatewayToken = aiSettings.gatewayToken,
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
        storytellerOperationMode = storytellerModeForNewGame(aiSettings.preferredMode, script)

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
        activeGameStore.clear()
        replaceClocktowerSession(null)
        showNewGameConfirmation = false
        showHostTools = false
        showResults = false
        return true
    }

    fun returnToPlayerManagement() {
        if (!finishCurrentGameForRestart()) return
        setupCards.clear()
        currentDealIndex = 0
        screen = Screen.Setup
    }

    fun restartGame() {
        val restartScript = currentClocktowerScript
        val restartSeating = confirmHostSeating(requireClocktowerGameSession().state.gameState.players
            .sortedBy { it.seat }.map { it.name })
        if (!readyForNewClocktowerGame(restartScript)) {
            // Do not leave the old game when the selected AI transport cannot start the next one.
            settingsReturnScreen = screen
            screen = Screen.Settings
            showNewGameConfirmation = false
            return
        }
        if (!finishCurrentGameForRestart()) return
        // A five/six-player quick restart must not silently switch scripts.
        selectedClocktowerScript = restartScript
        hostSeatingSetupFlow = HostSeatingSetupFlow(restartSeating)
        startClocktowerGame()
    }

    /** Confirm real role changes once; card presentation reads Session. */
    fun reopenSeatingSetup() {
        hostSeatingSetupFlow = hostSeatingSetupFlow.reopenSeating()
        screen = Screen.Setup
    }

    BackHandler(enabled = screen == Screen.ClocktowerSettings) {
        reopenSeatingSetup()
    }

    val aiSettingsSection: @Composable () -> Unit = { StorytellerAiSettingsPanel(aiSettings) }

    fun activeMetadata() = ClocktowerActiveGameMetadata.encode(storytellerOperationMode, clocktowerRulesetRef,
        committedTroubleBrewingSetupRotationRecord, aiConversation.openingPlanRecommendation, aiConversation.strategicPolicyCursor,
        aiConversation.canonicalConversationResponseId, aiConversation.canonicalConversationModel)

    LaunchedEffect(Unit) {
        val saved = runCatching { withContext(Dispatchers.IO) { activeGameStore.load() } }
        if (saved.isFailure) { activeGameRestoreError = true; return@LaunchedEffect }
        val active = saved.getOrNull() ?: return@LaunchedEffect
        if (clocktowerSessionUi?.session != null || screen != Screen.Landing) return@LaunchedEffect
        runCatching {
            val metadata = active.metadata
            val restored = ClocktowerGameSession.restore(active.state)
            val restoredOpening = ClocktowerActiveGameMetadata.opening(metadata)
            val restoredCursor = ClocktowerActiveGameMetadata.cursor(metadata)
            require(restoredOpening == null || restoredOpening.selection.gameId == active.state.gameId)
            require(restoredCursor == null || (restoredCursor.gameId == active.state.gameId &&
                restoredCursor.exclusiveGlobalSequence <= active.state.nextTimelineGlobalSequence))
            val ref = ClocktowerActiveGameMetadata.ruleset(metadata)
            val rotation = ClocktowerActiveGameMetadata.rotation(metadata)
            setupCards.clear()
            playerNames.clear()
            playerNames.addAll(active.state.gameState.players.sortedBy { it.seat }.map { it.name })
            clocktowerRulesetRef = ref
            committedTroubleBrewingSetupRotationRecord = rotation
            storytellerOperationMode = StorytellerOperationMode.valueOf(metadata.getString("mode"))
            aiConversation.openingPlanRecommendation = restoredOpening
            aiConversation.openingRecommendedInformation = restoredOpening?.selection?.information.orEmpty()
            aiConversation.openingRecommendedRedHerringSeat = restoredOpening?.selection?.redHerringSeat
            aiConversation.openingRecommendedBluffRoleNames = restoredOpening?.selection?.demonBluffs.orEmpty().map { external ->
                requireNotNull(activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry.findByExternalId(external)).id.value
            }
            aiConversation.strategicPolicyCursor = restoredCursor
            aiConversation.canonicalConversationResponseId = metadata.optString("responseId").takeIf { it.isNotBlank() }
            aiConversation.canonicalConversationModel = metadata.optString("responseModel").takeIf { it.isNotBlank() }
            replaceClocktowerSession(restored)
            aiConversation.strategicLastReviewedHost = StorytellerStrategicChangeCheckpointV2.capture(restored,
                StorytellerStrategicChangeCheckpointV2.Position(active.state.phase, active.state.round))
            // No first-night action was confirmed: repeat dealing from the same roster.
            // This also safely handles interruption after a preview but before its Next.
            currentDealIndex = 0
            screen = if (restored.state.phase == StorytellerPhase.FIRST_NIGHT &&
                restored.state.actionTimeline.entries.isEmpty() &&
                restored.state.epistemicObservationLog.records.isEmpty()) Screen.PassPhone else Screen.ClocktowerJudge
        }.onFailure { activeGameRestoreError = true }
    }
    LaunchedEffect(clocktowerSessionUi?.session, aiConversation.openingPlanRecommendation, aiConversation.strategicPolicyCursor,
        storytellerOperationMode, aiConversation.canonicalConversationResponseId, aiConversation.canonicalConversationModel) {
        val holder = clocktowerSessionUi ?: return@LaunchedEffect
        holder.commit { session ->
            session.persistWith { confirmed -> activeGameStore.save(confirmed, activeMetadata()) }
        }
    }
    if (activeGameSaveError) {
        androidx.compose.material3.AlertDialog(onDismissRequest = {},
            text = { Text(localizedText("对局未能保存，本次确认没有生效。请腾出存储空间后重试。", "The game could not be saved. This confirmation was not accepted. Free storage and retry.")) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = {
                activeGameSaveError = false
                clocktowerSessionUi?.commit { session ->
                    session.persistWith { confirmed -> activeGameStore.save(confirmed, activeMetadata()) }
                }
            }) { Text(localizedText("重试保存", "Retry save")) } })
    }
    if (activeGameRestoreError) {
        androidx.compose.material3.AlertDialog(onDismissRequest = { activeGameRestoreError = false },
            text = { Text(localizedText("无法恢复对局，原存档已保留。", "Unable to restore the game. The saved file has been preserved.")) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { activeGameRestoreError = false }) { Text("OK") } })
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
                        preferredOperationMode = aiSettings.preferredMode,
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
                                it.selection.validFor(pending.request)
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
                            preparedSetup = pending.preparedSetup,
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
                                if (pendingTroubleBrewingDrunkSelection !== pending ||
                                    screen != Screen.ClocktowerDrunkSelection) {
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

                    Screen.Settings -> SettingsContent(
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
                        hasGlobalPlan = aiConversation.openingPlanRecommendation != null,
                        language = language,
                        onTakeOverManually = {
                            storytellerOperationMode = StorytellerOperationMode.MANUAL
                            screen = Screen.ClocktowerJudge
                        },
                    )

                    Screen.ClocktowerJudge -> ClocktowerSessionHost(
                        sessionUi = requireNotNull(clocktowerSessionUi),
                        cards = cards,
                        activeGameClocktowerRulesetCatalog = activeGameClocktowerRulesetCatalog,
                        clocktowerRulesetRef = clocktowerRulesetRef,
                        aiConversation = aiConversation,
                        aiSettings = aiSettings,
                        storytellerOperationMode = storytellerOperationMode,
                        currentMode = { storytellerOperationMode },
                        currentSession = { clocktowerSessionUi?.session },
                        isJudgeVisible = { screen == Screen.ClocktowerJudge },
                        drunkAiScope = drunkAiScope,
                        onHostTools = { hostToolTab = HostToolTab.Roles; showHostTools = true },
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

                val hostUiIsNight = clocktowerSessionUi?.snapshot?.phase != StorytellerPhase.DAY
                if (showHostTools) {
                    HostGameToolsScreen(
                        cards = cards,
                        isNight = hostUiIsNight,
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
                        isNight = hostUiIsNight,
                        onDismiss = { showNewGameConfirmation = false },
                        onManagePlayers = ::returnToPlayerManagement,
                        onQuickRestart = ::restartGame,
                    )
                }
            }
        }
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
