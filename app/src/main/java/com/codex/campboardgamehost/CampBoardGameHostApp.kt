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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
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
import com.codex.campboardgamehost.clocktower.session.MayorRedirectDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicChangeCheckpointV2
import com.codex.campboardgamehost.clocktower.session.InitialOpeningRequestV2
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.NoGreaterJoyProductionSetupPreparer
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidate
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRoute
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkSelectionRouter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingPreparedSetup
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupCommitter
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecord
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupRotationRecordFactory
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

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
internal fun CampBoardGameHostApp(
    newClocktowerSeed: () -> Long = { UUID.randomUUID().let { uuid ->
        (uuid.mostSignificantBits xor uuid.leastSignificantBits).takeIf { it != 0L } ?: 1L
    } },
) {
    val baseContext = LocalContext.current
    val appPreferencesStore = remember(baseContext) { AppPreferencesStore(baseContext) }
    val aiSettings = rememberStorytellerAiSettings(baseContext)
    val advice = remember { ClocktowerLiveAdvice() }
    val openingUi = remember { ClocktowerOpeningUi() }
    val nightDrafts = remember { ClocktowerNightDrafts() }
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
    // Per-game takeover stays separate from the saved default for future games.
    var storytellerOperationMode by remember { mutableStateOf(aiSettings.preferredMode) }
    // Opaque provider IDs are provisional until Host accepts the opening. Never
    // persist them to game facts, logs or player's visible information.
    // An advisory opening plan is never a committed package of future facts.
    // Current-step recommendation only: never a separate adoption or game-state fact.
    val drunkAiScope = rememberCoroutineScope()
    var showHostTools by remember { mutableStateOf(false) }
    var hostToolTab by remember { mutableStateOf(HostToolTab.Roles) }
    var showNewGameConfirmation by remember { mutableStateOf(false) }
    var currentDealIndex by remember { mutableStateOf(0) }
    // The Demon may revise this while awake. Only the confirmed attack may
    // reach protection, Mayor redirection, death, or succession resolution.
    // A target is provisional while the Poisoner is still awake. It becomes a
    // mechanical fact only when the night step is advanced.
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var clocktowerGameSession by remember { mutableStateOf<ClocktowerGameSession?>(null) }
    // Render invalidation only. All game data is read from the one live Session.
    var clocktowerUiTick by remember { mutableStateOf(0) }
    clocktowerUiTick
    val clocktowerSessionState = clocktowerGameSession?.state
    val clocktowerPhase by remember {
        derivedStateOf {
            clocktowerUiTick
            when (clocktowerGameSession?.state?.phase ?: StorytellerPhase.FIRST_NIGHT) {
                StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
                StorytellerPhase.DAY -> ClocktowerPhase.Day
                StorytellerPhase.NIGHT -> ClocktowerPhase.Night
            }
        }
    }
    val round by remember { derivedStateOf { clocktowerUiTick; clocktowerGameSession?.state?.round ?: 1 } }
    val currentClocktowerScript = clocktowerSessionState?.gameState?.script
        ?.let { scriptId ->
            ClocktowerScript.entries.singleOrNull { script -> script.toRecommendationScriptId() == scriptId }
        }
        ?: ClocktowerScript.TroubleBrewing
    val clocktowerGameId = clocktowerSessionState?.gameId.orEmpty()
    val clocktowerGameStateRevision = clocktowerSessionState?.gameStateRevision ?: 0L
    val clocktowerPlayerInputRevision = clocktowerSessionState?.playerInputRevision ?: 0L
    val clocktowerActionTimeline = clocktowerSessionState?.actionTimeline ?: ActionFactTimeline()
    // One-shot consumptions belong to the committed action timeline, not Compose mutable flags.
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
    val cards: List<PlayerCard> = remember(setupCards, language) {
        ClocktowerSessionPlayerCards(
            setupCards = setupCards,
            currentSession = {
                clocktowerGameSession?.state
            },
            language = language,
        )
    }
    // This is a derived validity check, not a second mutable truth or stale-plan cache.
    // A confirmed Host action or an overridden published clue can invalidate
    // the whole linked opening proposal while leaving actual Session facts intact.
    val openingHintsUsable = advice.openingPlanRecommendation?.let { opening ->
        clocktowerGameSession?.state?.let { actual ->
            ClocktowerJointOpeningDisplayMatcherV2.stillUsable(
                session = actual,
                plannedDrunkSeat = opening.selection.drunkSeat,
                plannedInformation = advice.openingRecommendedInformation,
                plannedRedHerringSeat = advice.openingRecommendedRedHerringSeat,
                plannedBluffRoleNames = advice.openingRecommendedBluffRoleNames,
            )
        }
    } == true
    // UI-only selection; the Red Herring enters Session at its first-night Next step.
    fun refreshClocktowerUi() {
        clocktowerUiTick++
    }

    fun confirmClocktowerStep(confirm: () -> Unit) {
        try { confirm() }
        catch (_: java.io.IOException) { activeGameSaveError = true }
    }
    fun requireClocktowerGameSession(): ClocktowerGameSession =
        requireNotNull(clocktowerGameSession) {
            "Clocktower session authority is unavailable."
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
        openingUi.pendingTroubleBrewingDrunkSelection = null
        advice.pendingCurrentStepAdvice = null
        committedTroubleBrewingSetupRotationRecord = null
        advice.canonicalConversationResponseId = null
        advice.canonicalConversationModel = null
        clocktowerGameSession = null
        refreshClocktowerUi()
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
            refreshClocktowerUi()
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
        
        nightDrafts.clocktowerDemonAttackDraftTarget = null
        nightDrafts.clocktowerPoisonTarget = null
        nightDrafts.clocktowerFortuneTellerFirst = null
        nightDrafts.clocktowerFortuneTellerSecond = null
        nightDrafts.clocktowerChambermaidFirst = null
        nightDrafts.clocktowerChambermaidSecond = null
        nightDrafts.clocktowerRavenkeeperTarget = null
        nightDrafts.clocktowerButlerMaster = null
        nightDrafts.clocktowerMonkProtectedTarget = null
        nightDrafts.clocktowerMayorRedirectTarget = null

        screen = Screen.PassPhone
    }

    fun requestLiveGlobalAdvice(steps: List<ClocktowerNightStepUi>, stepIndex: Int) {
        if (screen != Screen.ClocktowerJudge || currentClocktowerScript != ClocktowerScript.TroubleBrewing ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL) return
        val session = clocktowerGameSession ?: return
        val mode = storytellerOperationMode
        advice.request(ClocktowerLiveAdviceInput(session, clocktowerRulesetRef,
            activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript), steps, stepIndex, cards, mode,
            listOfNotNull(nightDrafts.clocktowerFortuneTellerFirst, nightDrafts.clocktowerFortuneTellerSecond),
            currentTroubleBrewingMayorRedirectPendingDecision()), aiSettings, drunkAiScope) {
            screen == Screen.ClocktowerJudge && clocktowerGameSession === session && storytellerOperationMode == mode
        }
    }

    fun commitAndStartTroubleBrewingGame(
        preparedGameId: String,
        preparedSetup: TroubleBrewingPreparedSetup,
        confirmedDrunkCandidate: TroubleBrewingDrunkCandidate?,
    ) {
        require(preparedGameId.isNotBlank()) { "Trouble Brewing prepared game ID cannot be blank." }
        openingUi.pendingTroubleBrewingDrunkSelection = null
        val preparedSeed = preparedSetup.intermediateSetup.gameSeed
        val characterRegistry = activeGameClocktowerRulesetCatalog
            .ruleset(ClocktowerScript.TroubleBrewing)
            .characterRegistry
        val committedSetup = TroubleBrewingSetupCommitter.commit(
            intermediateSetup = preparedSetup.intermediateSetup,
            confirmedDrunkCandidate = confirmedDrunkCandidate,
            characterRegistry = characterRegistry,
        )
        val committedCards = ClocktowerSetupPreparation.committedCards(committedSetup, characterRegistry, context, language)

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
    fun requestJointOpening(pending: PendingTroubleBrewingJointOpening) {
        val mode = storytellerOperationMode
        openingUi.request(pending, aiSettings, mode, activeGameClocktowerRulesetCatalog, drunkAiScope) {
            storytellerOperationMode == mode && screen in setOf(
                Screen.ClocktowerJointPass, Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)
        }
    }

    fun confirmJointOpeningDrunk(pending: PendingTroubleBrewingJointOpening, candidate: TroubleBrewingDrunkCandidate?) {
        openingUi.confirmJointOpeningDrunk(pending, candidate, storytellerOperationMode,
            activeGameClocktowerRulesetCatalog, advice) { id, setup, drunk ->
            commitAndStartTroubleBrewingGame(id, setup, drunk)
            requireClocktowerGameSession()
        }?.let { screen = it }
    }
    fun beginJointOpeningAtDrunkStep() {
        openingUi.beginJointOpeningAtDrunkStep(screen, storytellerOperationMode, ::confirmJointOpeningDrunk)
            ?.let { screen = it }
    }
    fun takeOverJointOpeningManually() {
        openingUi.takeOverJointOpeningManually(screen, advice,
            { storytellerOperationMode = StorytellerOperationMode.MANUAL }, ::commitAndStartTroubleBrewingGame)
            ?.let { screen = it }
    }

    fun startTroubleBrewingGame() {
        // Both AI modes must enter the one-request shown-role-first joint path.
        // A legacy Gateway configuration must NOT fall through into the paid
        // Drunk-only request followed by a second global analysis.
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL &&
            (!aiSettings.directEnabled || !aiSettings.directLoaded || aiSettings.token.isBlank() ||
                aiSettings.model.isNullOrBlank())
        ) return
        // Invalidate any prior asynchronous callback before preparing the next setup.
        openingUi.jointOpeningSerial += 1
        advice.openingPlanRecommendation = null
        advice.openingRecommendedBluffRoleNames = emptyList()
        advice.openingRecommendedRedHerringSeat = null
        nightDrafts.redHerringDraftName = null
        openingUi.pendingOpeningResponseId = null
        openingUi.pendingOpeningModel = null
        advice.canonicalConversationResponseId = null
        advice.canonicalConversationModel = null
        advice.strategicPolicyCursor = null
        advice.strategicLastReviewedHost = null
        advice.openingRecommendedInformation = emptyMap()
        advice.pendingCurrentStepAdvice = null
        openingUi.shownOnlyDealCompleted = false
        openingUi.pendingJointOpening = null
        openingUi.jointOpeningResult = null
        openingUi.jointOpeningBusy = false
        openingUi.jointOpeningError = null
        openingUi.jointRevealIndex = 0
        openingUi.pendingTroubleBrewingDrunkSelection = null
        val playerNames = hostSeatingSetupFlow.playerNamesFor()
        val preparedSeed = newClocktowerSeed()
        val preparedGameId = UUID.randomUUID().toString()

        val preparedSetup = ClocktowerSetupPreparation.prepareTroubleBrewing(
            baseContext, activeGameClocktowerRulesetCatalog, playerNames, preparedSeed)
        val characterRegistry = activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        if (aiSettings.directEnabled &&
            storytellerOperationMode != StorytellerOperationMode.MANUAL
        ) {
            val snapshot = TroubleBrewingGameSnapshotProjector.fromIntermediate(
                preparedGameId, preparedSetup.intermediateSetup)
            val openingRequest = InitialOpeningRequestV2.fromHostSnapshot(
                snapshot, characterRegistry,
                playerExperience = playerNames.mapIndexed { index, name ->
                    (index + 1) to openingExperience(name)
                }.toMap(),
            )
            val visibleCards = ClocktowerSetupPreparation.shownCards(preparedSetup, language)
            val pending = PendingTroubleBrewingJointOpening(
                preparedGameId, preparedSetup, openingRequest, visibleCards)
            openingUi.pendingJointOpening = pending
            openingUi.jointRevealIndex = 0
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

            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                val pending = PendingTroubleBrewingDrunkSelection(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    request = route.request,
                )
                openingUi.pendingTroubleBrewingDrunkSelection = pending
                screen = Screen.ClocktowerDrunkSelection
                // Only the manual operator reaches this local picker. AI paid
                // Drunk-only planning was replaced by the joint-opening request.
            }
        }
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
        val committedCards = ClocktowerSetupPreparation.noGreaterJoyCards(preparedSetup, script, playerNames, context, language)
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
        clocktowerGameSession = null
        refreshClocktowerUi()
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
        committedTroubleBrewingSetupRotationRecord, advice.openingPlanRecommendation, advice.strategicPolicyCursor,
        advice.canonicalConversationResponseId, advice.canonicalConversationModel)

    LaunchedEffect(Unit) {
        val saved = runCatching { withContext(Dispatchers.IO) { activeGameStore.load() } }
        if (saved.isFailure) { activeGameRestoreError = true; return@LaunchedEffect }
        val active = saved.getOrNull() ?: return@LaunchedEffect
        if (clocktowerGameSession != null || screen != Screen.Landing) return@LaunchedEffect
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
            advice.openingPlanRecommendation = restoredOpening
            advice.openingRecommendedInformation = restoredOpening?.selection?.information.orEmpty()
            advice.openingRecommendedRedHerringSeat = restoredOpening?.selection?.redHerringSeat
            advice.openingRecommendedBluffRoleNames = restoredOpening?.selection?.demonBluffs.orEmpty().map { external ->
                requireNotNull(activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry.findByExternalId(external)).id.value
            }
            advice.strategicPolicyCursor = restoredCursor
            advice.canonicalConversationResponseId = metadata.optString("responseId").takeIf { it.isNotBlank() }
            advice.canonicalConversationModel = metadata.optString("responseModel").takeIf { it.isNotBlank() }
            clocktowerGameSession = restored
            refreshClocktowerUi()
            advice.strategicLastReviewedHost = StorytellerStrategicChangeCheckpointV2.capture(restored,
                StorytellerStrategicChangeCheckpointV2.Position(active.state.phase, active.state.round))
            // No first-night action was confirmed: repeat dealing from the same roster.
            // This also safely handles interruption after a preview but before its Next.
            currentDealIndex = 0
            screen = if (restored.state.phase == StorytellerPhase.FIRST_NIGHT &&
                restored.state.actionTimeline.entries.isEmpty() &&
                restored.state.epistemicObservationLog.records.isEmpty()) Screen.PassPhone else Screen.ClocktowerJudge
        }.onFailure { activeGameRestoreError = true }
    }
    LaunchedEffect(clocktowerGameSession, advice.openingPlanRecommendation, advice.strategicPolicyCursor,
        storytellerOperationMode, advice.canonicalConversationResponseId, advice.canonicalConversationModel) {
        val session = clocktowerGameSession ?: return@LaunchedEffect
        confirmClocktowerStep {
            session.persistWith { confirmed -> activeGameStore.save(confirmed, activeMetadata()) }
        }
    }
    if (activeGameSaveError) {
        androidx.compose.material3.AlertDialog(onDismissRequest = {},
            text = { Text(localizedText("对局未能保存，本次确认没有生效。请腾出存储空间后重试。", "The game could not be saved. This confirmation was not accepted. Free storage and retry.")) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = {
                activeGameSaveError = false
                confirmClocktowerStep {
                    clocktowerGameSession?.let { session ->
                        session.persistWith { confirmed -> activeGameStore.save(confirmed, activeMetadata()) }
                    }
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

                    Screen.ClocktowerJointPass, Screen.ClocktowerJointReveal,
                    Screen.ClocktowerJointReview, Screen.ClocktowerDrunkSelection -> ClocktowerOpeningScreens(
                        screen, openingUi, storytellerOperationMode, activeGameClocktowerRulesetCatalog, language,
                        navigate = { screen = it }, isDrunkSelectionCurrent = { screen == Screen.ClocktowerDrunkSelection },
                        onContinue = ::beginJointOpeningAtDrunkStep, onManual = ::takeOverJointOpeningManually,
                        onRetry = ::requestJointOpening, onConfirmOpeningDrunk = ::confirmJointOpeningDrunk,
                        onStartGame = ::commitAndStartTroubleBrewingGame,
                    )

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
                        hasGlobalPlan = advice.openingPlanRecommendation != null,
                        language = language,
                        onTakeOverManually = {
                            storytellerOperationMode = StorytellerOperationMode.MANUAL
                            screen = Screen.ClocktowerJudge
                        },
                    )

                    Screen.ClocktowerJudge -> ClocktowerPlayScreen(
                        session = requireClocktowerGameSession(), cards = cards,
                        catalog = activeGameClocktowerRulesetCatalog, rulesetRef = clocktowerRulesetRef,
                        advice = advice, nightDrafts = nightDrafts, operationMode = storytellerOperationMode,
                        openingHintsUsable = openingHintsUsable, openingInformation = advice.openingRecommendedInformation,
                        openingBluffs = advice.openingRecommendedBluffRoleNames, openingRedHerringSeat = advice.openingRecommendedRedHerringSeat,
                        onPublish = ::refreshClocktowerUi,
                        onSaveError = { activeGameSaveError = true },
                        onHostTools = { hostToolTab = HostToolTab.Roles; showHostTools = true },
                        requestAdvice = ::requestLiveGlobalAdvice,
                        mayorDecision = ::currentTroubleBrewingMayorRedirectPendingDecision,
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
