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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.debug.DebugFlightRecorder
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
    val preferences = remember(baseContext) { AppPreferencesState(AppPreferencesStore(baseContext)) }
    val aiSettings = rememberStorytellerAiSettings(baseContext)
    val aiConversation = remember { ClocktowerAiConversation() }
    val openingDraft = remember { ClocktowerOpeningDraft() }
    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
    val languageMode = preferences.languageMode
    val context = remember(languageMode) { baseContext.localized(languageMode) }
    val language = context.resources.configuration.locales[0].language
    var screen by remember { mutableStateOf(Screen.Landing) }
    var activeGameRestoreError by remember { mutableStateOf(false) }
    var activeGameSaveError by remember { mutableStateOf(false) }
    var settingsReturnScreen by remember { mutableStateOf(Screen.Setup) }
    var pendingTroubleBrewingDrunkSelection by openingDraft::pendingTroubleBrewingDrunkSelection
    // Per-game takeover stays separate from the saved default for future games.
    val lifecycle = remember(baseContext) {
        ClocktowerGameLifecycle(baseContext, activeGameClocktowerRulesetCatalog, preferences,
            aiSettings, aiConversation, newClocktowerSeed, onSaveError = { activeGameSaveError = true })
    }
    val storytellerOperationMode = lifecycle.operationMode
    var pendingJointOpening by openingDraft::pendingJointOpening
    var jointOpeningResult by openingDraft::jointOpeningResult
    // Opaque provider IDs are provisional until Host accepts the opening. Never
    // persist them to game facts, logs or player's visible information.
    var jointOpeningBusy by openingDraft::jointOpeningBusy
    var jointOpeningError by openingDraft::jointOpeningError
    var jointRevealIndex by remember { mutableStateOf(0) }
    // An advisory opening plan is never a committed package of future facts.
    // Current-step recommendation only: never a separate adoption or game-state fact.
    val drunkAiScope = rememberCoroutineScope()
    var showHostTools by remember { mutableStateOf(false) }
    var hostToolTab by remember { mutableStateOf(HostToolTab.Records) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var currentDealIndex by remember { mutableStateOf(0) }
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    val clocktowerSessionUi = lifecycle.sessionUi
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { openingDraft.reset(aiConversation); lifecycle.dispose() }
    }
    val clocktowerSessionState = clocktowerSessionUi?.snapshot
    val currentClocktowerScript = clocktowerSessionState?.gameState?.script
        ?.let { scriptId ->
            ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
        }
        ?: ClocktowerScript.TroubleBrewing
    var showResults by remember { mutableStateOf(false) }
    val gameOutcome = clocktowerSessionState?.let { it.outcome?.present(context, it) }
    LaunchedEffect(gameOutcome) { if (gameOutcome != null) showResults = true }
    val playerNames = preferences.playerNames
    var hostSeatingSetupFlow by remember { mutableStateOf(HostSeatingSetupFlow()) }
    // Retain the setup step when the user visits app-wide AI settings and returns.
    var clocktowerSetupStep by remember { mutableStateOf(0) }
    val cards: List<PlayerCard> = remember(clocktowerSessionState, language) {
        ClocktowerSessionPlayerCards(emptyList(), { lifecycle.sessionUi?.snapshot }, language)
    }
    val playerCount = playerNames.size

    fun localizedText(zh: String, en: String): String = if (language == "en") en else zh

    fun applyOpeningResult(result: ClocktowerOpeningDraft.Result) {
        when (result) {
            ClocktowerOpeningDraft.Result.DEAL -> { currentDealIndex = 0; showResults = false; screen = Screen.PassPhone }
            ClocktowerOpeningDraft.Result.JOINT -> { jointRevealIndex = 0; screen = Screen.ClocktowerJointPass }
            ClocktowerOpeningDraft.Result.DRUNK -> { showResults = false; screen = Screen.ClocktowerDrunkSelection }
            ClocktowerOpeningDraft.Result.HOST -> { showResults = false; screen = Screen.ClocktowerJudge }
            ClocktowerOpeningDraft.Result.UNCHANGED, ClocktowerOpeningDraft.Result.FAILED -> Unit
        }
    }
    fun requestJointOpening(pending: PendingTroubleBrewingJointOpening) = openingDraft.requestJointOpening(
        pending, aiSettings, activeGameClocktowerRulesetCatalog, lifecycle, drunkAiScope)
    fun confirmJointOpeningDrunk(pending: PendingTroubleBrewingJointOpening, candidate: TroubleBrewingDrunkCandidate?) =
        applyOpeningResult(openingDraft.confirmJointOpeningDrunk(pending, candidate, aiConversation,
            activeGameClocktowerRulesetCatalog, lifecycle))
    fun beginJointOpeningAtDrunkStep() = applyOpeningResult(openingDraft.beginJointOpeningAtDrunkStep(
        aiConversation, activeGameClocktowerRulesetCatalog, lifecycle))
    fun takeOverJointOpeningManually() = applyOpeningResult(openingDraft.takeOverJointOpeningManually(aiConversation, lifecycle))
    fun readyForNewClocktowerGame(script: ClocktowerScript) = lifecycle.readyForNewGame(script)

    fun startClocktowerGame() {
        val names = hostSeatingSetupFlow.playerNamesFor()
        if (names.size < MIN_CLOCKTOWER_PLAYERS) return
        val script = if (names.size in 5..6) selectedClocktowerScript ?: defaultClocktowerScriptFor(names.size)
            else ClocktowerScript.TroubleBrewing
        if (!canStartClocktowerScript(script) || !lifecycle.beginNewGame(script)) return
        openingDraft.reset(aiConversation)
        if (script == ClocktowerScript.TroubleBrewing) {
            val prepared = lifecycle.prepareTroubleBrewing(names)
            applyOpeningResult(openingDraft.beginPrepared(UUID.randomUUID().toString(), prepared,
                names.mapIndexed { index, name -> index + 1 to preferences.openingExperience(name) }.toMap(),
                language, aiSettings, activeGameClocktowerRulesetCatalog, lifecycle, drunkAiScope))
        } else if (lifecycle.startNoGreaterJoy(names) == ClocktowerGameLifecycle.StartResult.STARTED) {
            currentDealIndex = 0; showResults = false; screen = Screen.PassPhone
        }
    }
    fun closeGameDialogs() {
        showNewGameConfirmation = false; showHostTools = false; showResults = false
    }
    fun returnToPlayerManagement() {
        if (!lifecycle.finish()) return
        closeGameDialogs()
        currentDealIndex = 0
        screen = Screen.Setup
    }
    fun restartGame() {
        when (val result = lifecycle.prepareRestart()) {
            ClocktowerGameLifecycle.RestartResult.NotReady -> {
                settingsReturnScreen = screen; screen = Screen.Settings; showNewGameConfirmation = false
            }
            ClocktowerGameLifecycle.RestartResult.Failed -> Unit
            is ClocktowerGameLifecycle.RestartResult.Ready -> {
                closeGameDialogs()
                selectedClocktowerScript = result.script
                hostSeatingSetupFlow = HostSeatingSetupFlow(confirmHostSeating(result.players))
                startClocktowerGame()
                // The old game is finished. If the new one did not start, do not stay on a screen
                // that reads the live Session; return to the start screen with the same seating.
                if (lifecycle.sessionUi == null && screen in setOf(
                        Screen.PassPhone, Screen.RevealCard, Screen.ClocktowerAutoPause, Screen.ClocktowerJudge,
                    )) screen = Screen.ClocktowerSettings
            }
        }
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

    LaunchedEffect(screen == Screen.Landing) {
        if (screen != Screen.Landing) return@LaunchedEffect
        when (val result = lifecycle.restore()) {
            ClocktowerGameLifecycle.RestoreResult.NONE -> Unit
            ClocktowerGameLifecycle.RestoreResult.FAILED -> activeGameRestoreError = true
            else -> {
                val restored = requireNotNull(lifecycle.sessionUi).snapshot
                playerNames.clear()
                playerNames.addAll(restored.gameState.players.sortedBy { it.seat }.map { it.name })
                currentDealIndex = 0
                screen = if (result == ClocktowerGameLifecycle.RestoreResult.DEAL) Screen.PassPhone else Screen.ClocktowerJudge
            }
        }
    }
    LaunchedEffect(clocktowerSessionUi, aiConversation.openingPlanRecommendation, aiConversation.strategicPolicyCursor,
        storytellerOperationMode, aiConversation.canonicalConversationResponseId, aiConversation.canonicalConversationModel) {
        lifecycle.persistMetadata()
    }
    if (activeGameSaveError) {
        androidx.compose.material3.AlertDialog(onDismissRequest = {},
            text = { Text(localizedText("对局未能保存，本次确认没有生效。请腾出存储空间后重试。", "The game could not be saved. This confirmation was not accepted. Free storage and retry.")) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = {
                activeGameSaveError = false
                lifecycle.persistMetadata()
            }) { Text(localizedText("重试保存", "Retry save")) } })
    }
    if (activeGameRestoreError) {
        androidx.compose.material3.AlertDialog(onDismissRequest = { activeGameRestoreError = false },
            text = { Text(localizedText("无法恢复对局，原存档已保留。", "Unable to restore the game. The saved file has been preserved.")) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { activeGameRestoreError = false }) { Text("OK") } })
    }

    CompositionLocalProvider(LocalContext provides context) {
        CampBoardGameHostTheme {
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
                    commonPlayers = preferences.commonPlayers,
                    playerExperienceByName = preferences.playerExperienceByName,
                    onPlayerExperienceChange = preferences::setExperience,
                    playerNames = playerNames,
                    onAddCurrentPlayer = preferences::addPlayer,
                    onRemoveCurrentPlayer = preferences::removePlayer,
                    onMoveCurrentPlayerTo = preferences::movePlayer,
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
                            aiActions = pendingJointOpening?.takeIf {
                                storytellerOperationMode != StorytellerOperationMode.MANUAL &&
                                    jointOpeningResult?.selection?.validFor(it.request) == true
                            }?.let { opening ->
                                ClocktowerAiRecommendationActions(
                                    identity = jointOpeningResult!!.selection,
                                    reason = jointOpeningResult?.tradeoffSummary,
                                    openingReason = true,
                                    onRetry = {
                                        pendingTroubleBrewingDrunkSelection = null
                                        screen = Screen.ClocktowerJointReview
                                        requestJointOpening(opening)
                                    },
                                )
                            },
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
                                    applyOpeningResult(openingDraft.confirmManualDrunk(pending, candidate, lifecycle))
                                }
                            },
                        )
                    }

                    Screen.Settings -> SettingsContent(
                        preferences = preferences,
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
                            
                                hostToolTab = HostToolTab.Records
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
                            lifecycle.takeOverManually()
                            screen = Screen.ClocktowerJudge
                        },
                    )

                    Screen.ClocktowerJudge -> ClocktowerSessionHost(
                        lifecycle = lifecycle,
                        cards = cards,
                        activeGameClocktowerRulesetCatalog = activeGameClocktowerRulesetCatalog,
                        aiConversation = aiConversation,
                        aiSettings = aiSettings,
                        isJudgeVisible = { screen == Screen.ClocktowerJudge },
                        drunkAiScope = drunkAiScope,
                        onHostTools = { hostToolTab = HostToolTab.Records; showHostTools = true },
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
                        sessionState = clocktowerSessionUi?.snapshot,
                        isNight = hostUiIsNight,
                        initialTab = hostToolTab,
                        settingsContent = {
                            SettingsContent(
                        preferences = preferences,
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
