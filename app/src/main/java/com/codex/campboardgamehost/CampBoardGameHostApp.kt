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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ActionFact
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
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.MayorRedirectDecisionBoundary
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderRequestFactoryV1
import com.codex.campboardgamehost.clocktower.session.StorytellerProviderGameContextBuilderV1
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1
import com.codex.campboardgamehost.clocktower.session.InitialJointOpeningDirectV2
import com.codex.campboardgamehost.clocktower.session.BotcAiWireTrace
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicChangeCheckpointV2
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2
import com.codex.campboardgamehost.clocktower.session.StorytellerResidualPackageGateV2
import com.codex.campboardgamehost.clocktower.session.HostCurrentOpenDecisionInventoryV2
import com.codex.campboardgamehost.clocktower.session.InitialOpeningRequestV2
import com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticAcceptanceV2
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.confirmedPoisonTargetName
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
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import com.codex.campboardgamehost.debug.DebugFlightRecorder
import kotlinx.coroutines.Dispatchers
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
)

/** Showing roles may begin while Drunk actual identity is uncommitted. No real session yet. */
private data class PendingTroubleBrewingJointOpening(
    val preparedGameId: String,
    val preparedSetup: TroubleBrewingPreparedSetup,
    val request: InitialOpeningRequestV2,
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
    val aiSettings = rememberStorytellerAiSettings(baseContext)
    val activeGameClocktowerRulesetCatalog = remember(baseContext) {
        BuiltInClocktowerRulesetCatalog.fromContext(baseContext)
    }
    var languageMode by remember { mutableStateOf(appPreferencesStore.loadLanguageMode()) }
    val context = remember(languageMode) { baseContext.localized(languageMode) }
    val language = context.resources.configuration.locales[0].language
    var screen by remember { mutableStateOf(Screen.Landing) }
    val activeGameStore = remember(baseContext) { ClocktowerActiveGameStore.fromContext(baseContext) }
    var activeGameSaveAttached by remember { mutableStateOf<ClocktowerGameSession?>(null) }
    var activeGameRestoreError by remember { mutableStateOf(false) }
    var activeGameSaveError by remember { mutableStateOf(false) }
    var settingsReturnScreen by remember { mutableStateOf(Screen.Setup) }
    var pendingTroubleBrewingDrunkSelection by remember {
        mutableStateOf<PendingTroubleBrewingDrunkSelection?>(null)
    }
    // Per-game takeover stays separate from the saved default for future games.
    var storytellerOperationMode by remember { mutableStateOf(aiSettings.preferredMode) }
    val allowRuleDeterminedAutoUi = storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC
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
    var selectedClocktowerScript by remember { mutableStateOf<ClocktowerScript?>(null) }
    var clocktowerGameSession by remember { mutableStateOf<ClocktowerGameSession?>(null) }
    var clocktowerSessionView by remember { mutableStateOf<ClocktowerSessionView?>(null) }
    val clocktowerPhase by remember {
        derivedStateOf {
            when (clocktowerSessionView?.phase ?: StorytellerPhase.FIRST_NIGHT) {
                StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
                StorytellerPhase.DAY -> ClocktowerPhase.Day
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
    val clocktowerEpistemicObservations =
        clocktowerSessionView?.epistemicObservationLog?.records.orEmpty()
    var committedTroubleBrewingSetupRotationRecord by remember {
        mutableStateOf<TroubleBrewingSetupRotationRecord?>(null)
    }
    var clocktowerRulesetRef by remember { mutableStateOf<RulesetRef?>(null) }
    var showResults by remember { mutableStateOf(false) }
    val gameOutcome = clocktowerSessionView?.let { clocktowerGameSession?.state?.outcome?.present(context, clocktowerGameSession!!.state) }
    val clocktowerLastExecutedName = clocktowerActionTimeline.entries.asReversed().firstNotNullOfOrNull {
        (it.fact as? ActionFact.Execution)?.targetSeat
    }?.let { clocktowerGameSession?.state?.gameState?.playerAt(it)?.name }
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
    val clocktowerGhostVoteAuthority = ClocktowerGhostVoteAuthority.from(clocktowerActionTimeline)
    fun publishClocktowerSessionView() {
        clocktowerSessionView = clocktowerGameSession?.view
    }

    fun confirmClocktowerStep(confirm: () -> Unit) {
        try { confirm() }
        catch (_: java.io.IOException) { activeGameSaveError = true }
    }
    fun confirmClocktowerNomination(confirm: () -> Boolean): Boolean = try { confirm() }
        catch (_: java.io.IOException) { activeGameSaveError = true; false }

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
    fun confirmedClocktowerPoisonTarget(): String? =
        clocktowerGameSession?.state?.confirmedPoisonTargetName(round)

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

    fun advanceClocktowerPlayerInputRevision() {
        requireClocktowerGameSession().recordPlayerInput()
        publishClocktowerSessionView()
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

    fun storytellerPhaseFor(phase: ClocktowerPhase = clocktowerPhase): StorytellerPhase =
        phase.toStorytellerPhase()

    fun clocktowerSeatFor(playerName: String): Int =
        cards.indexOfFirst { it.name == playerName }
            .takeIf { index -> index >= 0 }
            ?.plus(1)
            ?: error("Unknown Clocktower player '$playerName'.")

    fun currentClocktowerNightCheckpoint(): ClocktowerNightCheckpoint {
        val notification = clocktowerGameSession?.state?.takeIf { it.phase == StorytellerPhase.NIGHT }
            ?.pendingImpNotification()?.targetSeat?.let { clocktowerGameSession?.state?.gameState?.playerAt(it)?.name }
        return ClocktowerNightCheckpoint(
            phaseName = clocktowerPhase.name,
            round = round,
            gameStateRevision = clocktowerGameStateRevision,
            playerInputRevision = clocktowerPlayerInputRevision,
            confirmedAttackTarget = confirmedNightAttackName(),
            attackDraftTarget = clocktowerDemonAttackDraftTarget,
            confirmedPoisonTarget = confirmedClocktowerPoisonTarget(),
            poisonDraftTarget = clocktowerPoisonTarget,
            confirmedMonkTarget = confirmedNightMonkName(),
            monkDraftTarget = clocktowerMonkProtectedTarget,
            confirmedMayorRedirectTarget = confirmedNightMayorName(),
            mayorRedirectDraftTarget = clocktowerMayorRedirectTarget,
            pendingNewDemonName = notification,
            nextTimelineGlobalSequence = clocktowerNextTimelineGlobalSequence,
        )
    }

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

    fun commitClocktowerPrivateInformationPublication(draft: EpistemicObservationDraft, expectedRevision: StorytellerDecisionRevision) = confirmClocktowerStep {
        val session = requireClocktowerGameSession()
        val before = session.state
        session.commitGlobalEpistemicObservation(draft, expectedRevision)
        if (session.state.playerInputRevision == before.playerInputRevision) return@confirmClocktowerStep
        reviewConfirmedCurrentStepAdvice(before, draft)
        publishClocktowerSessionView()
        if (storytellerOperationMode != StorytellerOperationMode.MANUAL) {
            BotcAiWireTrace.residualGate("HOST_PUBLISHED", 1,
                strategicPolicyCursor?.version ?: 0,
                draft.phase.name, draft.round)
        }
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
        pendingCurrentStepAdvice = null
        committedTroubleBrewingSetupRotationRecord = null
        canonicalConversationResponseId = null
        canonicalConversationModel = null
        clocktowerGameSession = null
        publishClocktowerSessionView()
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
        clocktowerPoisonTarget = null
        clocktowerFortuneTellerFirst = null
        clocktowerFortuneTellerSecond = null
        clocktowerChambermaidFirst = null
        clocktowerChambermaidSecond = null
        clocktowerRavenkeeperTarget = null
        clocktowerButlerMaster = null
        clocktowerMonkProtectedTarget = null
        clocktowerMayorRedirectTarget = null

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
        val requestedNightRuleset = activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript)
        val pendingPair = pendingGlobalFirstNightPairDecision(
            step, clocktowerPhase, round, cards, session.state.gameState, session.state.gameId,
            StorytellerDecisionRevision(session.state.gameStateRevision, session.state.playerInputRevision),
        )
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
        val key = ClocktowerResidualRequestIdentityV2.build(
            session.state.gameId, round, stepIndex, identity.requestId,
            revision.gameStateRevision, revision.playerInputRevision,
            session.state.nextTimelineGlobalSequence, requestedOperationMode,
        )
        // Only coalesce an in-flight request. An explicit retry must remain possible
        // after a failed/empty review; old explanatory text is not a result cache.
        if (liveAiAdviceKey == key && liveAiAdviceBusy) return
        liveAiAdviceKey = key
        pendingCurrentStepAdvice = null
        liveAiAdviceText = null
        liveAiAdviceError = null
        // An older in-flight response belongs to a different key and cannot own this state.
        liveAiAdviceBusy = false
        if (!aiSettings.endpoint.startsWith("https://") ||
            aiSettings.token.isBlank()
        ) {
            liveAiAdviceError = "AI unavailable: configure the selected AI connection."
            return
        }
        val providerRequest = runCatching {
            val snapshot = run {
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
                    snapshot, gameContext,
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
            (!aiSettings.directEnabled || wholeOpening == null ||
                wholeCursor == null || wholeBaseline == null)
        ) {
            liveAiAdviceError = "Whole-game strategy is not available; automatic AI paused. Use Host manual decisions."
            DebugFlightRecorder.record("RESIDUAL_AUTO_NO_WHOLE_CONTEXT")
            return
        }
        if (aiSettings.directEnabled && wholeOpening != null &&
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
                    advisoryPlanContradictsConfirmedState = openingPlanConflictsWithHost,
                    currentPlayerQuerySupplied = pendingScalar != null && step.action in setOf(
                        ClocktowerNightAction.FortuneTeller,
                        ClocktowerNightAction.Chambermaid,
                        ClocktowerNightAction.Ravenkeeper,
                    ))
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
            val requestedModel = requireNotNull(aiSettings.model)
            val requestedToken = aiSettings.token
            val previousResponseId = canonicalConversationResponseId?.takeIf {
                canonicalConversationModel == requestedModel
            }
            drunkAiScope.launch {
                val result = try {
                    // Coalesce related Host updates before paying for a new request.
                    delay(450)
                    if (liveAiAdviceKey != key || clocktowerGameSession !== session ||
                        session.state !== currentHost.state ||
                        strategicPolicyCursor != wholeCursor ||
                        storytellerOperationMode != requestedOperationMode ||
                        aiSettings.model != requestedModel || aiSettings.token != requestedToken ||
                        session.state.nextNightInteraction(requestedNightRuleset)?.id != step.interactionId) return@launch
                    runCatching {
                        StorytellerStrategicContinuationV2.recommendWithReceipt(
                            PersonalDirectOpenAiV1.ENDPOINT, requestedToken, requestedModel,
                            session, wholeOpening, legalOpen, wholeCursor, previousResponseId,
                        )
                    }
                } finally {
                    // Credential edits can invalidate this request without changing its Host key.
                    // Release that request on every exit, never a newer request's busy state.
                    if (liveAiAdviceKey == key) liveAiAdviceBusy = false
                }
                if (liveAiAdviceKey != key || clocktowerGameSession !== session ||
                    session.state !== currentHost.state ||
                    session.state.nextNightInteraction(requestedNightRuleset)?.id != step.interactionId ||
                    clocktowerPhase.toStorytellerPhase() != phasePosition.phase ||
                    round != phasePosition.round ||
                    strategicPolicyCursor != wholeCursor ||
                    storytellerOperationMode != requestedOperationMode ||
                    aiSettings.model != requestedModel || aiSettings.token != requestedToken
                ) return@launch
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
                            verified.choices.forEach { (id, meaning) ->
                                val decision = legalOpen.single { it.decisionId == id }
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
    }

    /** 3C: the only INITIAL paid model request for personal-direct TB opening. */
    fun requestJointOpening(pending: PendingTroubleBrewingJointOpening) {
        if (pendingJointOpening !== pending || jointOpeningBusy ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL ||
            !aiSettings.directEnabled) return
        if (aiSettings.token.isBlank() || !aiSettings.endpoint.startsWith("https://")) {
            jointOpeningError = "AI credentials unavailable. You can continue manually."
            return
        }
        val requestedMode = storytellerOperationMode
        val requestedKey = aiSettings.token
        val requestedModel = aiSettings.model ?: PersonalDirectOpenAiV1.DEFAULT_MODEL
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
                    request = pending.request,
                    registry = registry,
                    model = requestedModel,
                    onStage = { stage -> BotcAiTrace.firstNightStage(serial, stage) },
                )
            }
            // Precommit has NO committed session. Never compare against a
            // fake session state; bind directly to this immutable pending game.
            if (serial != jointOpeningSerial || pendingJointOpening !== pending ||
                storytellerOperationMode != requestedMode || !aiSettings.directEnabled ||
                screen !in setOf(Screen.ClocktowerJointPass,
                    Screen.ClocktowerJointReveal, Screen.ClocktowerJointReview)
            ) return@launch
            result.onSuccess { receipt ->
                if (receipt.accepted.selection.validFor(pending.request)) {
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
            !accepted.selection.validFor(pending.request) ||
            storytellerOperationMode == StorytellerOperationMode.MANUAL) return
        val legalRoute = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        when (legalRoute) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> require(candidate == null)
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
            accepted.selection.information
        } else emptyMap()
        openingRecommendedBluffRoleNames = if (recommendationMatchesActualDrunk) {
            val registry = activeGameClocktowerRulesetCatalog
                .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
            accepted.selection.demonBluffs.orEmpty().map { externalId ->
                requireNotNull(registry.findByExternalId(externalId)).id.value
            }
        } else emptyList()
        openingRecommendedRedHerringSeat = if (recommendationMatchesActualDrunk) {
            accepted.selection.redHerringSeat
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
            !accepted.selection.validFor(pending.request)) return
        val legal = TroubleBrewingDrunkSelectionRouter.route(pending.preparedSetup, null)
        val recommended = when (legal) {
            TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded -> {
                require(accepted.selection.drunkSeat == null)
                null
            }
            is TroubleBrewingDrunkSelectionRoute.ManualSelection ->
                legal.request.candidates.singleOrNull {
                    it.seat == accepted.selection.drunkSeat
                } ?: run {
                    jointOpeningError = "This Drunk recommendation cannot be applied. Retry planning or take over manually."
                    return
                }
        }
        if (legal !is TroubleBrewingDrunkSelectionRoute.ManualSelection ||
            storytellerOperationMode == StorytellerOperationMode.AI_AUTOMATIC
        ) {
            confirmJointOpeningDrunk(pending, recommended)
            return
        }
        pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
            pending.preparedGameId, pending.preparedSetup,
            legal.request.copy(recommendedCandidate = recommended),
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
            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                pendingTroubleBrewingDrunkSelection = PendingTroubleBrewingDrunkSelection(
                    pending.preparedGameId, pending.preparedSetup, route.request)
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
            (!aiSettings.directEnabled || !aiSettings.directLoaded || aiSettings.token.isBlank() ||
                aiSettings.model.isNullOrBlank())
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
                preparedGameId, preparedSetup, openingRequest, visibleCards)
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

            is TroubleBrewingDrunkSelectionRoute.ManualSelection -> {
                val pending = PendingTroubleBrewingDrunkSelection(
                    preparedGameId = preparedGameId,
                    preparedSetup = preparedSetup,
                    request = route.request,
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
        activeGameSaveAttached = null
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
        currentDealIndex = 0
        screen = Screen.Setup
    }

    fun restartGame() {
        val restartScript = currentClocktowerScript
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
        committedTroubleBrewingSetupRotationRecord, openingPlanRecommendation, strategicPolicyCursor,
        canonicalConversationResponseId, canonicalConversationModel)

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
            setupCards.addAll(active.state.gameState.players.sortedBy { it.seat }.map {
                PlayerCard(it.name, clocktowerRole = clocktowerRoleByName(it.actualRole.value),
                    clocktowerShownRole = it.shownRole?.value?.let(::clocktowerRoleByName))
            })
            playerNames.clear(); playerNames.addAll(setupCards.map { it.name })
            clocktowerRulesetRef = ref
            committedTroubleBrewingSetupRotationRecord = rotation
            storytellerOperationMode = StorytellerOperationMode.valueOf(metadata.getString("mode"))
            openingPlanRecommendation = restoredOpening
            openingRecommendedInformation = restoredOpening?.selection?.information.orEmpty()
            openingRecommendedRedHerringSeat = restoredOpening?.selection?.redHerringSeat
            openingRecommendedBluffRoleNames = restoredOpening?.selection?.demonBluffs.orEmpty().map { external ->
                requireNotNull(activeGameClocktowerRulesetCatalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry.findByExternalId(external)).id.value
            }
            strategicPolicyCursor = restoredCursor
            canonicalConversationResponseId = metadata.optString("responseId").takeIf { it.isNotBlank() }
            canonicalConversationModel = metadata.optString("responseModel").takeIf { it.isNotBlank() }
            clocktowerGameSession = restored
            publishClocktowerSessionView()
            strategicLastReviewedHost = StorytellerStrategicChangeCheckpointV2.capture(restored,
                StorytellerStrategicChangeCheckpointV2.Position(active.state.phase, active.state.round))
            screen = Screen.ClocktowerJudge
        }.onFailure { activeGameRestoreError = true }
    }
    LaunchedEffect(screen, clocktowerGameSession, openingPlanRecommendation, strategicPolicyCursor,
        storytellerOperationMode, canonicalConversationResponseId, canonicalConversationModel) {
        val session = clocktowerGameSession ?: return@LaunchedEffect
        if (screen != Screen.ClocktowerJudge && activeGameSaveAttached !== session) return@LaunchedEffect
        confirmClocktowerStep {
            session.persistWith { confirmed -> activeGameStore.save(confirmed, activeMetadata()) }
            activeGameSaveAttached = session
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
                        activeGameSaveAttached = session
                    }
                }
            }) { Text(localizedText("重试保存", "Retry save")) } })
    }
    if (activeGameRestoreError) {
        androidx.compose.material3.AlertDialog(onDismissRequest = { activeGameRestoreError = false },
            text = { Text(localizedText("无法恢复对局，原存档已保留。", "Unable to restore the game. The saved file has been preserved.")) },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { activeGameRestoreError = false }) { Text("OK") } })
    }

    val renderedPhase = storytellerPhaseFor()
    val renderedRound = round
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
                        sessionState = requireClocktowerGameSession().state,
                        script = currentClocktowerScript,
                        gameId = clocktowerGameId,
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
                        phase = clocktowerPhase,
                        round = round,
                        nightCheckpoint = currentClocktowerNightCheckpoint(),
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
                        lastExecutedName = clocktowerLastExecutedName,
                        ghostVoteAuthority = clocktowerGhostVoteAuthority,
                        onCommitConfirmedPrivateInformationPublication = ::commitClocktowerPrivateInformationPublication,
                        onHostTools = {
                            hostToolTab = HostToolTab.Roles
                            showHostTools = true
                        },
                        onSelectNightDeath = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerDemonAttackDraftTarget = selected
                        },
                        onConfirmDemonAttack = { stepId, successorName ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                val targetSeat = clocktowerSeatFor(requireNotNull(clocktowerDemonAttackDraftTarget) {
                                    "Imp must choose a target before confirming the irreversible attack."
                                })
                                session.confirmNightAttack(
                                    targetSeat = targetSeat,
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                    successorSeat = successorName?.let(::clocktowerSeatFor),
                                )
                                publishClocktowerSessionView()
                            }
                        },
                        onSelectPoisonTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerPoisonTarget = selected
                        },
                        onConfirmPoisonTarget = { stepId ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                session.confirmNightPoison(
                                    targetSeat = clocktowerPoisonTarget?.let(::clocktowerSeatFor),
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                )
                                publishClocktowerSessionView()
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
                            confirmClocktowerStep {
                                val seat = cards.indexOfFirst { it.name == name }
                                    .takeIf { it >= 0 }?.plus(1)
                                    ?: error("Cannot confirm Red Herring outside the Session seats.")
                                requireClocktowerGameSession().commitRedHerringSeat(
                                    seat = seat,
                                    phase = renderedPhase, round = renderedRound,
                                    sequence = requireClocktowerGameSession().nextInputSequence,
                                )
                                publishClocktowerSessionView()
                                redHerringDraftName = null
                            }
                        },
                        onCommitDemonBluffs = { roleNames ->
                            confirmClocktowerStep {
                                if (roleNames != clocktowerRecommendedDemonBluffRoleNames) {
                                    requireClocktowerGameSession().commitDemonBluffs(
                                        roles = roleNames.map(::RoleId),
                                        phase = renderedPhase, round = renderedRound,
                                        sequence = requireClocktowerGameSession().nextInputSequence,
                                    )
                                    publishClocktowerSessionView()
                                }
                            }
                        },
                        onSelectButlerMaster = {
                            advanceClocktowerPlayerInputRevision()
                            clocktowerButlerMaster = it
                        },
                        onConfirmButlerMaster = { actorName, masterName, stepId ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmNightButlerMaster(
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                    actorSeat = clocktowerSeatFor(actorName),
                                    masterSeat = clocktowerSeatFor(masterName),
                                )
                                publishClocktowerSessionView()
                            }
                        },
                        onSelectMonkProtectedTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMonkProtectedTarget = selected
                        },
                        onConfirmMonkProtectedTarget = { stepId ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                session.confirmNightProtection(
                                    targetSeat = clocktowerSeatFor(requireNotNull(clocktowerMonkProtectedTarget)),
                                    phase = renderedPhase, round = renderedRound, stepId = stepId,
                                )
                                publishClocktowerSessionView()
                            }
                        },
                        onSelectMayorRedirectTarget = { selected ->
                            advanceClocktowerPlayerInputRevision()
                            clocktowerMayorRedirectTarget = selected
                        },
                        onConfirmMayorRedirectTarget = {
                            confirmClocktowerStep {
                                val checkpoint = currentClocktowerNightCheckpoint()
                                if (currentClocktowerScript == ClocktowerScript.TroubleBrewing) {
                                    val selectedTarget = requireNotNull(checkpoint.mayorRedirectDraftTarget)
                                    val pendingDecision = requireNotNull(currentTroubleBrewingMayorRedirectPendingDecision())
                                    require(pendingDecision.revision == StorytellerDecisionRevision(
                                        clocktowerGameStateRevision, clocktowerPlayerInputRevision,
                                    )) { "Stale Mayor confirmation." }
                                    if (checkpoint.confirmedMayorRedirectTarget != selectedTarget) {
                                        val targetSeat = clocktowerSeatFor(selectedTarget)
                                        val session = requireClocktowerGameSession()
                                        val registry = activeGameClocktowerRulesetCatalog
                                            .ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
                                        val snapshot = TroubleBrewingGameSnapshotProjector.fromRuntime(
                                            gameSnapshot = session.toGameSnapshot(requireNotNull(clocktowerRulesetRef)),
                                            phase = StorytellerPhase.NIGHT, round = round, characterRegistry = registry,
                                        )
                                        val request = StorytellerProviderRequestFactoryV1.fromMayorRedirect(
                                            decision = pendingDecision, snapshot = snapshot,
                                            gameContext = StorytellerProviderGameContextBuilderV1.build(
                                                snapshot, StorytellerProviderRevisionV1(
                                                    session.state.gameStateRevision, session.state.playerInputRevision,
                                                ), session.state,
                                            ),
                                        )
                                        session.commitConfirmedMayorRedirect(
                                            draft = ActionFactDraft.MayorRedirectChoice(
                                                actionId = clocktowerActionId("mayor-redirect", targetSeat = targetSeat),
                                                phase = StorytellerPhase.NIGHT, round = round,
                                                sequence = session.nextInputSequence,
                                                mayorSeat = pendingDecision.mayorSeat, targetSeat = targetSeat,
                                            ),
                                            request = request, expectedGameStateRevision = session.state.gameStateRevision,
                                        )
                                        publishClocktowerSessionView()
                                    }
                                }
                            }
                        },
                        onConfirmNewDemon = {
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmImpNotification()
                                publishClocktowerSessionView()
                            }
                        },
                        onConfirmSpyWake = { stepId ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmSpyWake(activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript), stepId)
                                publishClocktowerSessionView()
                            }
                        },
                        onConfirmEvilInformation = { stepId, previewRevision ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmEvilInformation(activeGameClocktowerRulesetCatalog.ruleset(currentClocktowerScript), stepId, previewRevision)
                                publishClocktowerSessionView()
                            }
                        },
                        onConfirmKlutzChoice = { chosenName ->
                            confirmClocktowerStep {
                                val session = requireClocktowerGameSession()
                                val death = requireNotNull(session.state.pendingKlutzDeath())
                                session.confirmKlutzChoice(death.actionId, clocktowerSeatFor(chosenName))
                                publishClocktowerSessionView()
                            }
                        },
                        onConfirmArtistQuestion = { claimant, truth, shown ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmArtistAnswer(clocktowerSeatFor(claimant), truth, shown)
                                publishClocktowerSessionView()
                            }
                        },
                        onSlayerShot = { claimant, target, registration ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmSlayerShot(clocktowerSeatFor(claimant), clocktowerSeatFor(target), registration)
                                publishClocktowerSessionView()
                            }
                        },
                        onConfirmedNomination = { nominator, nominee, spyChoice, expectedRound ->
                            confirmClocktowerNomination {
                                val executed = requireClocktowerGameSession().confirmNomination(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), spyChoice, expectedRound)
                                publishClocktowerSessionView()
                                executed
                            }
                        },
                        onConfirmedVote = { nominator, nominee, vote, expectedRound ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmVote(clocktowerSeatFor(nominator), clocktowerSeatFor(nominee), vote.voters.map { clocktowerSeatFor(it.playerName) }, expectedRound)
                                publishClocktowerSessionView()
                            }
                        },
                        onAdvanceFromFirstNight = {
                            confirmClocktowerStep {
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                                publishClocktowerSessionView()
                                clocktowerDemonAttackDraftTarget = null

                            }
                        },
                        onConfirmDay = { expectedRound ->
                            confirmClocktowerStep {
                                requireClocktowerGameSession().confirmEndDay(expectedRound)
                                publishClocktowerSessionView()
                            }
                        },
                        onCompleteNight = {
                            confirmClocktowerStep {
                                commitClocktowerPhaseAdvance(ClocktowerPhase.Day)
                                clocktowerFortuneTellerFirst = null
                                clocktowerFortuneTellerSecond = null
                                clocktowerChambermaidFirst = null
                                clocktowerChambermaidSecond = null
                                clocktowerRavenkeeperTarget = null
                                clocktowerMonkProtectedTarget = null
                                // PhaseAdvance has already cleared transient night targets.
                                publishClocktowerSessionView()
                                clocktowerMayorRedirectTarget = null
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
