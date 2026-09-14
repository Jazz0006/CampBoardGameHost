Warning: truncated output (original token count: 65488)
Total output lines: 4478

package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegality

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
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
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.DynamicStorytellerChoice
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PredictedDecisionOutcome
import com.codex.campboardgamehost.clocktower.domain.RegistrationLedger
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionKind
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionOtherNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFact
import com.codex.campboardgamehost.clocktower.flow.ClocktowerResolvedFlowFacts
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightCursor
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightStateProjector
import com.codex.campboardgamehost.clocktower.rules.ClocktowerOptionalNightSourceChronology
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.ResolvedNightMechanicalEvent
import com.codex.campboardgamehost.clocktower.config.TroubleBrewingRecommendationMetadata
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState
import com.codex.campboardgamehost.clocktower.recommendation.SetupRecommendationLockPolicy
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.recommendation.GameBalanceEvaluator
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupRecommendationService
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCandidate
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionPoolParityRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
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
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.NightTransactionReconstructor
import com.codex.campboardgamehost.clocktower.session.DynamicResolutionRequest
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult
import com.codex.campboardgamehost.clocktower.session.FirstNightPublicationResolution
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildExecutor
import com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheRebuildRequest
import com.codex.campboardgamehost.clocktower.epistemic.A4ShadowWorldSetCache
import com.codex.campboardgamehost.clocktower.epistemic.A4WorldEngineRollout
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicSemanticJson
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightChronology
import com.codex.campboardgamehost.clocktower.rules.RegistrationInteractionRules
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerJudgeScreen(
    automaticStorytellerInfo: Boolean,
    automaticStorytellerStyle: RecommendationStyle,
    cards: List<PlayerCard>,
    events: List<ClocktowerEvent>,
    script: ClocktowerScript,
    gameId: String,
    gameSeed: Long,
    gameStateRevision: Long,
    playerInputRevision: Long,
    setupHistory: CrossGameHistory,
    setupRecommendationResultProvider: ((SetupCoordinationRequest) -> SetupRecommendationService.ConstrainedResult)? = null,
    firstNightNaturalPairReadyProvider: ((GameState) -> List<DecisionCandidate<SetupClueOutcome>>?)? = null,
    firstNightNaturalPairResultProvider: (suspend (GameState) -> List<DecisionCandidate<SetupClueOutcome>>)? = null,
    onInitialRecommendationDemand: () -> Unit,
    phase: ClocktowerPhase,
    round: Int,
    nightCheckpoint: ClocktowerNightCheckpoint,
    pendingNightDeath: String?,
    demonAttackDraftTarget: String?,
    selectedExecution: String?,
    poisonTarget: String?,
    poisonDraftTarget: String?,
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
    monkProtectedDraftTarget: String?,
    mayorRedirectTarget: String?,
    mayorRedirectDraftTarget: String?,
    pendingNewDemonName: String?,
    pendingNightNewDemonIdentityName: String?,
    demonSuccessorTarget: String?,
    virginUsed: Boolean,
    slayerUsed: Boolean,
    slayerClaimedNames: List<String>,
    artistUsed: Boolean,
    artistClaimedNames: List<String>,
    lastExecutedName: String?,
    pendingKlutzName: String?,
    klutzChoiceName: String?,
    nightStartedState: MutableState<Boolean>,
    nightStepIndexState: MutableState<Int>,
    dayModeState: MutableState<ClocktowerDayMode>,
    ghostVoteAuthority: ClocktowerGhostVoteAuthority,
    highestVoteNameState: MutableState<String?>,
    highestVoteCountState: MutableState<Int>,
    gameOutcome: GameOutcome?,
    onGhostVoteAuthorityChange: (ClocktowerGhostVoteAuthority) -> Unit,
    onRecordEvent: (ClocktowerEventType, String, String, List<String>) -> Unit,
    onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit,
    onHostTools: () -> Unit,
    onPreviousFromFirstNightReady: () -> Unit,
    onMovePreviousNightStep: () -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onConfirmDemonAttack: () -> Unit,
    onSelectExecution: (String?) -> Unit,
    onSelectPoisonTarget: (String?) -> Unit,
    onConfirmPoisonTarget: () -> Unit,
    onSelectFortuneTellerFirst: (String?) -> Unit,
    onSelectFortuneTellerSecond: (String?) -> Unit,
    onSelectChambermaidFirst: (String?) -> Unit,
    onSelectChambermaidSecond: (String?) -> Unit,
    onSelectRavenkeeperTarget: (String?) -> Unit,
    onSelectRedHerring: (String?) -> Unit,
    onApplyRecommendation: (RecommendationPlan) -> Unit,
    onSelectButlerMaster: (String?) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onConfirmMonkProtectedTarget: () -> Unit,
    onSelectMayorRedirectTarget: (String?) -> Unit,
    onConfirmMayorRedirectTarget: () -> Unit,
    onSelectDemonSuccessor: (String?) -> Unit,
    onConfirmDemonSuccessorTarget: (String) -> Unit,
    onConfirmNewDemon: () -> Unit,
    onSelectKlutzChoice: (String?) -> Unit,
    onConfirmKlutzChoice: (Boolean) -> Unit,
    onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit,
    onSlayerShot: (String, String, Boolean) -> Unit,
    onPreflightVirginExecution: (String, Boolean) -> Unit,
    onVirginNomination: (String, String, Boolean) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: () -> Unit,
    onConfirmNight: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val recommendationCoordinator = remember(gameSeed) { ClocktowerRecommendationCoordinator() }
    // Aggregate-only C8 telemetry lives for this game UI session. The recorder
    // de-duplicates stable decision IDs, so Compose recomposition is not a new selection.
    val selectionDistributionTelemetry = remember(gameId) { SelectionDistributionTelemetryRecorder() }
    // B7.2 shadow telemetry stores only parity totals; candidate IDs and game facts stay local.
    val firstNightPoolParity = remember(gameId) { SelectionPoolParityRecorder() }
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val publicAliveCards = cards.filter { it.eliminatedRound == null }
    // The UI still owns rendering, but first-night information now crosses one
    // common lifecycle boundary before it is committed to the event log.
    var firstNightInformationMigration by remember(gameId, round) {
        mutableStateOf(FirstNightInformationMigration())
    }
    var observedFirstNightPoisonTarget by remember(gameId, round) { mutableStateOf<String?>(null) }
    var hasObservedFirstNightPoisonTarget by remember(gameId, round) { mutableStateOf(false) }
    LaunchedEffect(phase, poisonTarget) {
        if (phase != ClocktowerPhase.FirstNight) return@LaunchedEffect
        if (hasObservedFirstNightPoisonTarget && observedFirstNightPoisonTarget != poisonTarget) {
            firstNightInformationMigration = firstNightInformationMigration.invalidateUnshown()
        }
        observedFirstNightPoisonTarget = poisonTarget
        hasObservedFirstNightPoisonTarget = true
    }

    fun publishFirstNightInformation(displayStep: ClocktowerNightStepUi): Boolean {
        val request = clocktowerFirstNightInformationRequest(
            displayStep, phase, round, cards, script, gameSeed, poisonTarget, language, automaticStorytellerStyle,
        ) ?: return true
        val shadow = firstNightInformationMigration.shadow(request)
        firstNightPoolParity.recordResult(
            familyId = request.family.name.lowercase(),
            matches = shadow is FirstNightShadowResult.Ready,
        )
        return when (val result = firstNightInformationMigration.resolvePublication(request, shadow)) {
            is FirstNightPublicationResolution.Published -> {
                firstNightInformationMigration = result.migration
                true
            }
            FirstNightPublicationResolution.AlreadyDisplayed -> false
            FirstNightPublicationResolution.LegacyFallback -> true
        }
    }
    val spyCard = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }
    val recluseCard = cards.firstOrNull { it.clocktowerRole?.enName == "Recluse" }
    val spyRegistrationGood = remember { mutableStateMapOf<String, Boolean>() }
    val spyRegistrationRole = remember { mutableStateMapOf<String, String>() }
    val recordedSpyRegistrations = remember { mutableStateMapOf<String, Boolean>() }
    val recluseRegistrationEvil = remember { mutableStateMapOf<String, Boolean>() }
    val recluseRegistrationRole = remember { mutableStateMapOf<String, String>() }
    val recordedRecluseRegistrations = remember { mutableStateMapOf<String, Boolean>() }
    val recordedNightSteps = remember { mutableStateMapOf<String, Boolean>() }
    var effectivePoisonForRole: (String) -> String? = { poisonTarget }
    var effectiveRoleForRegistration: (String, PlayerCard) -> RoleId? = { _, card ->
        card.clocktowerRole?.enName?.let(::RoleId)
    }
    fun registrationKey(ability: String, subject: String = "spy") = "${phase.name}:$round:$ability:$subject"
    fun spyCanRegister(queryingRoleEnName: String): Boolean =
        spyCard != null &&
            effectiveRoleForRegistration(queryingRoleEnName, spyCard) == RoleId("Spy") &&
            effectivePoisonForRole(queryingRoleEnName) != spyCard.name
    fun spyRegistersGood(key: String?, queryingRoleEnName: String): Boolean = key != null && spyCanRegister(queryingRoleEnName) && spyRegistrationGood[key] == true
    fun registeredRole(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String): ClocktowerRole? {
        if (!spyRegistersGood(key, queryingRoleEnName)) return spyCard?.clocktowerRole
        val allowed = completeTroubleBrewingRoles.filter { it.team in teams && it.enName != "Spy" }
        return allowed.firstOrNull { it.enName == spyRegistrationRole[key] } ?: allowed.firstOrNull()
    }
    fun spyRegistrationWillRecord(key: String?): Boolean =
        key != null && recordedSpyRegistrations[key] != true && spyCard != null
    fun recordSpyRegistration(
        key: String?,
        teams: List<ClocktowerTeam>,
        queryingRoleEnName: String,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    ) {
        if (key == null || recordedSpyRegistrations[key] == true || spyCard == null) return
        recordedSpyRegistrations[key] = true
        val registrationDetail = when {
            !spyCanRegister(queryingRoleEnName) -> text("中毒，按真实邪恶身份登记", "poisoned; registered as actual evil identity")
            !spyRegistersGood(key, queryingRoleEnName) -> text("按真实邪恶身份登记", "registered as actual evil identity")
            detail == ClocktowerRegistrationDetail.AlignmentOnly -> text("登记为善良", "registered as good")
            else -> text(
                "登记为${registeredRole(key, teams, queryingRoleEnName)?.nameFor(language).orEmpty()}",
                "registered as ${registeredRole(key, teams, queryingRoleEnName)?.nameFor(language).orEmpty()}",
            )
        }
        onRecordEvent(
            ClocktowerEventType.RoleAction,
            text("间谍登记裁定", "Spy registration"),
            "${spyCard.seatLabel(cards)} · $registrationDetail",
            listOf(spyCard.name),
        )
    }
    fun recluseCanRegister(queryingRoleEnName: String): Boolean =
        recluseCard != null &&
            effectiveRoleForRegistration(queryingRoleEnName, recluseCard) == RoleId("Recluse") &&
            effectivePoisonForRole(queryingRoleEnName) != recluseCard.name
    fun recluseRegistersEvil(key: String?, queryingRoleEnName: String): Boolean =
        key != null && recluseCanRegister(queryingRoleEnName) && recluseRegistrationEvil[key] == true
    fun recluseRegisteredRole(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String): ClocktowerRole? {
        if (!recluseRegistersEvil(key, queryingRoleEnName)) return recluseCard?.clocktowerRole
        val allowed = completeTroubleBrewingRoles.filter { it.team in teams }
        return allowed.firstOrNull { it.enName == recluseRegistrationRole[key] } ?: allowed.firstOrNull()
    }
    fun recordRecluseRegistration(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String) {
        if (key == null || !recluseRegistersEvil(key, queryingRoleEnName) || recordedRecluseRegistrations[key] == true || recluseCard == null) return
        recordedRecluseRegistrations[key] = true
        val registeredAs = recluseRegisteredRole(key, teams, queryingRoleEnName)
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
    val canonicalNightDeathResolution = resolveTroubleBrewingDawnDeathResolution(
        cards = cards,
        script = script,
        gameSeed = gameSeed,
        checkpoint = nightCheckpoint,
    )
    val mayorCanRedirect = canonicalNightDeathResolution.mayorRedirectEligible
    val mayorTarget = canonicalNightDeathResolution.facts.mayorSeat
        ?.let { targetSeat -> cards.getOrNull(targetSeat - 1) }
    val mayorRedirectTargetCards = cards.filter { card ->
        card.name != mayorTarget?.name &&
            MayorRedirectLegality.canReceiveRedirect(
                targetIsDemon = card.clocktowerTeam == ClocktowerTeam.Demon,
            )
    }
    val resolvedNightDeathName = canonicalNightDeathResolution.resolvedDeathName
    val resolvedNightDeathCard = resolvedNightDeathName?.let { name -> cards.firstOrNull { it.name == name } }
    val nightDeathWillOccur = canonicalNightDeathResolution.resolvedDeathSeat != null
    val ravenkeeperTrigger = resolvedNightDeathCard
        ?.takeIf {
            nightDeathWillOccur &&
                AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Ravenkeeper")
        }

    val currentDemonHostContext = resolveCurrentDemonHostContext(
        cards = cards,
        poisonedPlayerName = nightCheckpoint.confirmedPoisonTarget,
    )
    val demonCard = currentDemonHostContext?.actor
    val demonPoisonedForActionExplanation = currentDemonHostContext?.isPoisoned == true
    val nightBaseGameState = cards.toClocktowerGameState(script, gameSeed, poisonTarget)
    val demonSuccessorRoleId = resolveNightReconstructionDemonRoleId(
        cards = cards,
        currentDemonHostContext = currentDemonHostContext,
        confirmedDemonAttackerName = nightCheckpoint.confirmedAttackTarget,
    )
    val demonSuccessionResolution = if (phase == ClocktowerPhase.Night) {
        resolveNightDemonSuccessionForHost(
            baseGameState = nightBaseGameState,
            checkpoint = nightCheckpoint,
            currentDemonHostContext = currentDemonHostContext,
            demonRoleId = demonSuccessorRoleId,
        )
    } else {
        DemonSuccessionResolution.None
    }
    val demonSuccessorTargetSeats = when (val resolution = demonSuccessionResolution) {
        DemonSuccessionResolution.None -> emptySet()
        is DemonSuccessionResolution.Forced -> setOf(resolution.targetSeat)
        is DemonSuccessionResolution.Choice -> resolution.targetSeats
    }
    val demonSuccessorTargetCards = cards.filterIndexed { index, _ ->
        index + 1 in demonSuccessorTargetSeats
    }
    val impSelfKillNeedsSuccessor =
        demonSuccessorTargetSeats.isNotEmpty()
    val sageNightDeath = resolvedNightDeathCard
        ?.takeIf { nightDeathWillOccur && AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Sage") }
    val otherNightWakingRoleIds = clocktowerOtherNightWakingRoleIds(
        cards = cards,
        pendingSuccessionDemonRoleId = demonSuccessorRoleId.takeIf { impSelfKillNeedsSuccessor },
    )
    val otherNightResolvedFacts = ClocktowerResolvedFlowFacts(
        buildSet {
            if (pendingNightNewDemonIdentityName != null) add(ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON)
            if (lastExecutedName != null) add(ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY)
            if (ravenkeeperTrigger != null) add(ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT)
            if (mayorCanRedirect) add(ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE)
            if (impSelfKillNeedsSuccessor) add(ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED)
            if (sageNightDeath != null) add(ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON)
        },
    )
    val otherNightInteractions = if (phase == ClocktowerPhase.Night) {
        ClocktowerProductionOtherNightFlow.interactions(
            ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
            playerCount = cards.size,
            wakingRoleIds = otherNightWakingRoleIds,
            resolvedFacts = otherNightResolvedFacts,
        )
    } else {
        emptyList()
    }
    val otherNightCanonicalInteractionIds = otherNightInteractions.map { it.id }
    val baseRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
        card.clocktowerRole?.enName?.let { roleName -> index + 1 to RoleId(roleName) }
    }.toMap()
    val demonSuccessorInteractionId = ClocktowerProductionNightStepIdentity.demonSuccessor()
        .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
    val canonicalNightReconstruction = if (phase == ClocktowerPhase.Night) {
        NightTransactionReconstructor.reconstruct(
            baseGameState = nightBaseGameState,
            checkpoint = nightCheckpoint,
            canonicalInteractionIds = otherNightCanonicalInteractionIds,
            demonSuccessorInteractionId = demonSuccessorInteractionId,
            demonRoleId = requireNotNull(demonSuccessorRoleId) {
                "Night transaction reconstruction requires a canonical Demon role."
            },
        )
    } else {
        null
    }
    val resolvedMechanicalEvents = buildList<ResolvedNightMechanicalEvent> {
        if (phase == ClocktowerPhase.Night && nightDeathWillOccur) {
            val targetSeat = cards.indexOf(resolvedNightDeathCard).plus(1)
            require(targetSeat > 0) { "Resolved night death must identify a valid target seat." }
            val effectiveInteractionId = if (mayorCanRedirect) {
                ClocktowerProductionNightStepIdentity.mayorRedirect().interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
            } else {
                val demonRoleId = requireNotNull(demonSuccessorRoleId) {
                    "Resolved night death requires a canonical Demon interaction."
                }
                ClocktowerProductionNightStepIdentity.role(demonRoleId).interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
            }
            add(ResolvedNightMechanicalEvent.MechanicalDeath(
                targetSeat = targetSeat,
                effectiveAt = ClocktowerEffectiveNightCursor(effectiveInteractionId, ClocktowerInteractionBoundary.AFTER),
            ))
        }
        if (phase == ClocktowerPhase.Night && canonicalNightReconstruction != null) {
            addAll(
                canonicalNightReconstruction.confirmedEvents
                    .filterIsInstance<ResolvedNightMechanicalEvent.RoleChanged>(),
            )
        }
    }
    fun effectiveNightStateAt(
        interactionId: ClocktowerInteractionId,
        boundary: ClocktowerInteractionBoundary,
    ) = ClocktowerEffectiveNightStateProjector.projectAt(
        baseAliveSeats = publicAliveCards.map { cards.indexOf(it).plus(1) }.toSet(),
        canonicalInteractionIds = otherNightCanonicalInteractionIds,
        confirmedEvents = resolvedMechanicalEvents,
        cursor = ClocktowerEffectiveNightCursor(interactionId, boundary),
        baseRoleIdsBySeat = baseRoleIdsBySeat,
    )

    val chambermaidInteractionId = ClocktowerProductionNightStepIdentity
        .role(RoleId("Chambermaid"))
        .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
    val chambermaidTargetCards = if (
        phase == ClocktowerPhase.Night && chambermaidInteractionId in otherNightCanonicalInteractionIds
    ) {
        val chambermaidState = effectiveNightStateAt(
            chambermaidInteractionId,
            ClocktowerInteractionBoundary.BEFORE,
        )
        cards.filterIndexed { index, _ -> chambermaidState.isMechanicallyAlive(index + 1) }
    } else {
        publicAliveCards
    }

    fun effectivePoisonTargetAt(
        interactionId: ClocktowerInteractionId,
        boundary: ClocktowerInteractionBoundary,
    ): String? {
        val source = actualClocktowerRoleCards(cards, "Poisoner").firstOrNull() ?: return null
        val sourceSeat = cards.indexOf(source).plus(1).takeIf { it > 0 } ?: return null
        val cursor = ClocktowerEffectiveNightCursor(interactionId, boundary)
        val sourceInteractionId = ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        if (!ClocktowerOptionalNightSourceChronology.hasActedBy(
                canonicalInteractionIds = otherNightCanonicalInteractionIds,
                cursor = cursor,
                sourceInteractionId = sourceInteractionId,
            )
        ) return null
        val effectiveState = effectiveNightStateAt(interactionId, boundary)
        val sourceFunctioning =
            effectiveState.currentRoleId(sourceSeat) == RoleId("Poisoner") &&
                AbilityFunctioningSemantics.functionsAs(
                    source.abilitySubject(null).copy(
                        isAlive = effectiveState.isMechanicallyAlive(sourceSeat),
                    ),
                    "Poisoner",
                )
        return PoisonEffectLifecycle.effectiveTarget(
            poisonTarget,
            true,
            sourceFunctioning,
        )
    }

    fun deathTriggerAbilityState(
        roleEnName: String,
        triggerActor: PlayerCard?,
    ): AbilityFunctioningState? {
        if (triggerActor == null) return null
        val deathEvent = resolvedMechanicalEvents.singleOrNull()
            as? ResolvedNightMechanicalEvent.MechanicalDeath
            ?: return null
        val deathInteractionId = deathEvent.effectiveAt.interactionId
        val beforeDeathState = effectiveNightStateAt(
            deathInteractionId,
            ClocktowerInteractionBoundary.BEFORE,
        )
        val effectivePoison = effectivePoisonTargetAt(
            deathInteractionId,
            ClocktowerInteractionBoundary.BEFORE,
        )
        val seat = cards.indexOf(triggerActor).plus(1).takeIf { it > 0 } ?: return null
        val subject = triggerActor.abilitySubject(effectivePoison).copy(
            isAlive = beforeDeathState.isMechanicallyAlive(seat),
        )
        return AbilityFunctioningSemantics.stateFor(subject, roleEnName)
    }

    val ravenkeeperDeathTriggerAbilityState = deathTriggerAbilityState("Ravenkeeper", ravenkeeperTrigger)
    val sageDeathTriggerAbilityState = deathTriggerAbilityState("Sage", sageNightDeath)

    fun effectiveAbilitySubjectForRole(enName: String, actor: PlayerCard?): AbilitySubject? {
        if (actor == null) return null
        val interactionId = ClocktowerProductionNightStepIdentity.role(RoleId(enName))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        if (phase != ClocktowerPhase.Night || interactionId !in otherNightCanonicalInteractionIds) {
            return actor.abilitySubject(poisonTarget)
        }
        val seat = cards.indexOf(actor).plus(1).takeIf { it > 0 } ?: return actor.abilitySubject(poisonTarget)
        val state = effectiveNightStateAt(interactionId, ClocktowerInteractionBoundary.BEFORE)
        return actor.abilitySubject(
            effectivePoisonTargetAt(
                interactionId,
                ClocktowerInteractionBoundary.BEFORE,
            ),
        ).copy(
            actualRole = state.currentRoleId(seat)?.value,
            isAlive = state.isMechanicallyAlive(seat),
        )
    }

    effectivePoisonForRole = { enName ->
        if (phase != ClocktowerPhase.Night) poisonTarget else effectivePoisonTargetAt(
            ClocktowerProductionNightStepIdentity.role(RoleId(enName))
                .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT),
            ClocktowerInteractionBoundary.BEFORE,
        )
    }

    effectiveRoleForRegistration = { enName, card ->
        if (phase != ClocktowerPhase.Night) {
            card.clocktowerRole?.enName?.let(::RoleId)
        } else {
            val interactionId = ClocktowerProductionNightStepIdentity
                .role(RoleId(enName))
                .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
            val seat = cards.indexOf(card).plus(1)
            if (interactionId !in otherNightCanonicalInteractionIds || seat <= 0) {
                null
            } else {
                effectiveNightStateAt(
                    interactionId,
                    ClocktowerInteractionBoundary.BEFORE,
                ).currentRoleId(seat)
            }
        }
    }

    val fortuneTellerRecluseRegistrationKey = recluseCard
        ?.takeIf { it.name == fortuneTellerFirst || it.name == fortuneTellerSecond }
        ?.let { registrationKey("FortuneTellerRecluse", it.name) }
    val roleDefinitionsById = clocktowerRoleDefinitionsForScript(script)
        .associateBy { it.id }
    fun fortuneTellerMatches(recluseRegistersAsDemon: Boolean): Boolean? {
        if (fortuneTellerFirst == null || fortuneTellerSecond == null) return null
        val targets = setOf(fortuneTellerFirst, fortuneTellerSecond)
        val fortuneTellerInteractionId = ClocktowerProductionNightStepIdentity
            .role(RoleId("Fortune Teller"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
        val fortuneTellerEffectiveState = lazy {
            effectiveNightStateAt(
                fortuneTellerInteractionId,
                ClocktowerInteractionBoundary.BEFORE,
            )
        }
        return cards.any { card ->
            val seat = cards.indexOf(card) + 1
            val currentRoleIsDemon =
                seat > 0 &&
                    clocktowerFortuneTellerRoleAuthority(
                        phase = phase,
                        baseRole = baseRoleIdsBySeat[seat],
                        otherNightRole = {
                            fortuneTellerEffectiveState.value.currentRoleId(seat)
                        },
                    )
                        ?.let(roleDefinitionsById::get)
                        ?.type == CharacterType.DEMON

            card.name in targets && (
                currentRoleIsDemon ||
                    card.name == redHerring ||
                    (card.name == recluseCard?.name && recluseRegistersAsDemon)
                )
        }
    }
    val fortuneTellerMatched = fortuneTellerMatches(
        recluseRegistersEvil(fortuneTellerRecluseRegistrationKey, "Fortune Teller"),
    )
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
    val chambermaidResolution = resolveChambermaidSelection(
        first = chambermaidFirst,
        second = chambermaidSecond,
        eligibleNames = chambermaidTargetCards.mapTo(mutableSetOf()) { it.name },
        wokeBecauseOwnAbilityNames = cards
            .filter { it.clocktowerRole?.enName in chambermaidWakeRoles() }
            .mapTo(mutableSetOf()) { it.name },
    )
    val chambermaidResult = chambermaidResolution.wokeCount?.toString()
    val chambermaidPresentation = clocktowerChambermaidSelectionPresentation(
        cards = cards,
        selection = chambermaidResolution.selection,
    )
    fun recordNightStep(step: ClocktowerNightStepUi) {
        if (!step.isRealAction || step.action == ClocktowerNightAction.DemonKill) return
        // Information shown to a player is recorded by onShowPlayerDisplay with the
        // final displayed result. Unreliable roles (including the Drunk) keep
        // displayKind=None until an option is chosen, so also identify them by
        // their information action/options.
        if (
            step.action == ClocktowerNightAction.None ||
            step.action in setOf(
                ClocktowerNightAction.FortuneTeller,
                ClocktowerNightAction.Chambermaid,
                ClocktowerNightAction.Ravenkeeper,
            ) ||
            step.displayKind != ClocktowerDisplayKind.None ||
            step.displayOptions.isNotEmpty() ||
            step.recommendedDisplayOptions.isNotEmpty()
        ) return
        val recordKey = "${phase.name}:$round:${step.action.name}:${step.actor?.name.orEmpty()}:${step.title}"
        val alreadyRecorded = recordedNightSteps[recordKey] == true || events.any { event ->
            event.type == ClocktowerEventType.RoleAction &&
                event.phase == phase &&
                event.round == round &&
                event.title == step.title &&
                (step.actor == null || step.actor.name in event.playerNames)
        }
        if (alreadyRecorded) {
            recordedNightSteps[recordKey] = true
            return
        }
        val names = when (step.action) {
            ClocktowerNightAction.RedHerring -> listOfNotNull(redHerring)
            ClocktowerNightAction.Poison -> listOfNotNull(step.actor?.name, poisonTarget)
            ClocktowerNightAction.ButlerMaster -> listOfNotNull(step.actor?.name, butlerMaster)
            ClocktowerNightAction.MonkProtect -> listOfNotNull(step.actor?.name, monkProtectedTarget)
            ClocktowerNightAction.FortuneTeller -> listOfNotNull(step.actor?.name, fortuneTellerFirst, fortuneTellerSecond)
            ClocktowerNightAction.Chambermaid -> listOfNotNull(step.actor?.name, chambermaidResolution.selection.first, chambermaidResolution.selection.second)
            ClocktowerNightAction.NewDemonIdentity -> listOfNotNull(step.actor?.name)
            ClocktowerNightAction.DemonKill -> listOfNotNull(step.actor?.name, pendingNightDeath)
            ClocktowerNightAction.MayorRedirect -> listOfNotNull(mayorRedirectTarget)
            ClocktowerNightAction.DemonSuccessor -> listOfNotNull(demonSuccessorTarget)
            ClocktowerNightAction.Ravenkeeper -> listOfNotNull(step.actor?.name, ravenkeeperTarget)
            ClocktowerNightAction.None -> listOfNotNull(step.actor?.name)
        }
        val detail = when (step.action) {
            ClocktowerNightAction.RedHerring ->
                text("红鲱鱼：${playerSeatLabel(cards, redHerring)}", "Red herring: ${playerSeatLabel(cards, redHerring)}")
            ClocktowerNightAction.Poison ->
                text("中毒目标：${playerSeatLabel(cards, poisonTarget)}", "Poisoned: ${playerSeatLabel(cards, poisonTarget)}")
            ClocktowerNightAction.ButlerMaster ->
                text("今日主人：${playerSeatLabel(cards, butlerMaster)}", "Master: ${playerSeatLabel(cards, butlerMaster)}")
            ClocktowerNightAction.MonkProtect ->
                text("保护目标：${playerSeatLabel(cards, monkProtectedTarget)}", "Protected: ${playerSeatLabel(cards, monkProtectedTarget)}")
            ClocktowerNightAction.FortuneTeller -> {
                val targets = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).joinToString(" + ") { playerSeatLabel(cards, it) }
                text("查验 $targets：$fortuneTellerResult", "Checked $targets: $fortuneTellerResult")
            }
            ClocktowerNightAction.Chambermaid -> {
                val targets = listOfNotNull(chambermaidResolution.selection.first, chambermaidResolution.selection.second).joinToString(" + ") { playerSeatLabel(cards, it) }
                text("查验 $targets：$chambermaidResult 人今晚醒来", "Checked $targets: $chambermaidResult woke tonight")
            }
            ClocktowerNightAction.NewDemonIdentity -> step.tellPlayer ?: step.storytellerAction
            ClocktowerNightAction.DemonKill ->
                text("击杀目标：${playerSeatLabel(cards, pendingNightDeath)}", "Kill target: ${playerSeatLabel(cards, pendingNightDeath)}")
            ClocktowerNightAction.MayorRedirect -> mayorRedirectTarget?.let { target ->
                val mayor = cards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                if (target == mayor?.name)
                    text("市长死亡", "Mayor dies")
                else
                    text("死亡转移给 ${playerSeatLabel(cards, target)}", "Death redirected to ${playerSeatLabel(cards, target)}")
            }.orEmpty()
            ClocktowerNightAction.DemonSuccessor ->
                text("新恶魔：${playerSeatLabel(cards, demonSuccessorTarget)}", "New Demon: ${playerSeatLabel(cards, demonSuccessorTarget)}")
            ClocktowerNightAction.Ravenkeeper ->
                text("查验：${playerSeatLabel(cards, ravenkeeperTarget)}", "Checked: ${playerSeatLabel(cards, ravenkeeperTarget)}")
            ClocktowerNightAction.None -> step.tellPlayer ?: step.storytellerAction
        }
        onRecordEvent(ClocktowerEventType.RoleAction, step.title, detail, names)
        recordedNightSteps[recordKey] = true
    }
    var nightStarted by nightStartedState
    var nightStepIndex by nightStepIndexState
    var dayMode by dayModeState
    var pendingNightAdvance by remember(gameId, round, phase) {
        mutableStateOf<ClocktowerNightAdvanceDirective.AwaitRefreshedFlow?>(null)
    }
    var nominatorName by remember(gameId, round) { mutableStateOf<String?>(null) }
    var nomineeName by remember(gameId, round) { mutableStateOf<String?>(null) }
    var highestVoteName by highestVoteNameState
    var highestVoteCount by highestVoteCountState
    var slayerClaimantName by remember(gameId) { mutableStateOf<String?>(null) }
    var slayerTargetName by remember(gameId) { mutableStateOf<String?>(null) }
    var artistClaimantName by remember(gameId) { mutableStateOf<String?>(null) }
    var artistTruthfulAnswer by remember(gameId) { mutableStateOf<Boolean?>(null) }
    var artistShownAnswer by remember(gameId) { mutableStateOf<Boolean?>(null) }
    fun selectArtistClaimant(next: String?) {
        artistClaimantName = next
        artistTruthfulAnswer = null
        artistShownAnswer = null
    }
    fun selectArtistTruthfulAnswer(next: Boolean?) {
        artistTruthfulAnswer = next
        artistShownAnswer = null
    }
    fun selectArtistShownAnswer(next: Boolean?) {
        artistShownAnswer = next
    }
    val confirmArtistQuestion = {
        val claimantName = requireNotNull(artistClaimantName) { "Artist confirmation requires a claimant." }
        val truthfulAnswer = requireNotNull(artistTruthfulAnswer) { "Artist confirmation requires a truthful answer." }
        val shownAnswer = requireNotNull(artistShownAnswer) { "Artist confirmation requires a shown answer." }
        onConfirmArtistQuestion(claimantName, truthfulAnswer, shownAnswer)
        selectArtistClaimant(null)
    }
    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    var slayerRecluseRegistersDemon by remember { mutableStateOf(false) }
    val firstNightNaturalPairPrecomputeRequest = if (
        script == ClocktowerScript.TroubleBrewing &&
        phase == ClocktowerPhase.FirstNight &&
        firstNightNaturalPairResultProvider != null
    ) {
        cards.toClocktowerGameState(
            script = script,
            seed = gameSeed,
            poisonedPlayerName = null,
        )
    } else {
        null
    }
    var firstNightNaturalPairCandidates by remember(gameId, gameSeed) {
        mutableStateOf<List<DecisionCandidate<SetupClueOutcome>>?>(null)
    }
    var firstNightNaturalPairStartRequested by remember(gameId, gameSeed) { mutableStateOf(false) }
    var firstNightNaturalPairLoadFailed by remember(gameId, gameSeed) { mutableStateOf(false) }
    var firstNightNaturalPairRetryGeneration by remember(gameId, gameSeed) { mutableStateOf(0) }
    LaunchedEffect(firstNightNaturalPairPrecomputeRequest, firstNightNaturalPairRetryGeneration) {
        val request = firstNightNaturalPairPrecomputeRequest
        val resultProvider = firstNightNaturalPairResultProvider
        if (request == null || resultProvider == null) {
            firstNightNaturalPairCandidates = null
            firstNightNaturalPairLoadFailed = false
            return@LaunchedEffect
        }
        firstNightNaturalPairCandidates = firstNightNaturalPairReadyProvider?.invoke(request)
        if (firstNightNaturalPairCandidates != null) {
            firstNightNaturalPairLoadFailed = false
            return@LaunchedEffect
        }
        val result = runCatching {
            withContext(Dispatchers.Default) {
                resultProvider(request)
            }
        }
        if (!isActive) return@LaunchedEffect
        result.fold(
            onSuccess = { candidates ->
                firstNightNaturalPairCandidates = candidates
                firstNightNaturalPairLoadFailed = false
            },
            onFailure = {
                firstNightNaturalPairCandidates = null
                firstNightNaturalPairLoadFailed = true
            },
        )
    }
    val firstNightNaturalPairPrecomputeReady =
        firstNightNaturalPairPrecomputeRequest == null || firstNightNaturalPairCandidates != null
    LaunchedEffect(firstNightNaturalPairStartRequested, firstNightNaturalPairPrecomputeReady) {
        if (firstNightNaturalPairStartRequested && firstNightNaturalPairPrecomputeReady) {
            firstNightNaturalPairStartRequested = false
            nightStarted = true
        }
    }
    val recommendationKey = buildString {
        append(script.name)
        append("|seed:")
        append(gameSeed)
        append("|state:")
        append(gameStateRevision)
        append("|input:")
        append(playerInputRevision)
        append("|phase:")
        append(phase.name)
        append("|round:")
        append(round)
        append("|poison:")
        append(poisonTarget.orEmpty())
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
        mutableStateOf(automaticStorytellerStyle)
    }
    var appliedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf<RecommendationStyle?>(null)
    }
    var lockedRecommendationDecisions by remember(recommendationKey) {
        mutableStateOf(SetupRecommendationLockPolicy.initialLocks())
    }
    val recommendationRequest = SetupCoordinationRequest(
        game = recommendationCards.toClocktowerGameState(
            script = script,
            seed = gameSeed,
            poisonedPlayerName = poisonTarget,
        ),
        roles = clocktowerRoleDefinitionsForScript(script),
        lockedDecisions = lockedRecommendationDecisions,
        history = setupHistory,
    )
    LaunchedEffect(recommendationKey, lockedRecommendationDecisions) {
        onInitialRecommendationDemand()
        recommendationUiState = RecommendationUiState.Loading
        val result = withContext(Dispatchers.Default) {
            runCatching {
                setupRecommendationResultProvider?.invoke(recommendationRequest)
                    ?: recommendationCoordinator.recommendSetup(recommendationRequest)
            }
        }
        // A changed revision/key cancels this effect. Never publish a completed
        // old generation into the state created for the new first-night input.
        if (!isActive) return@LaunchedEffect
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
    LaunchedEffect(automaticStorytellerInfo, automaticStorytellerStyle, recommendationUiState) {
        if (automaticStorytellerInfo && appliedRecommendationStyle != automaticStorytellerStyle) {
            val setupPlans = (recommendationUiState as? RecommendationUiState.Ready)?.plans.orEmpty()
            val automaticPlan = recommendationCoordinator.selectSetupPlan(setupPlans, automaticStorytellerStyle)
            if (automaticPlan != null) {
                fun setupFamily(plan: RecommendationPlan): String = plan.decisions
                    .filterIsInstance<StorytellerDecision.DrunkShownRole>()
                    .singleOrNull()
                    ?.role
                    ?.value
                    ?.let { "drunk-shown-role:$it" }
                    ?: "setup-plan"
                val setupAuditId = "$recommendationKey|setup"
                val setupDimensions = SelectionAuditDimensions(
                    playerCount = cards.size,
                    phase = StorytellerPhase.FIRST_NIGHT,
                    style = automaticStorytellerStyle,
                )
                selectionDistributionTelemetry.recordPreview(
                    SelectionAuditRecord(
                        selectionId = setupAuditId,
                        dimensions = setupDimensions,
                        candidates = setupPlans.map { plan ->
                            SelectionAuditCandidate(
                                familyId = setupFamily(plan),
                                qualityTier = plan.qualityTier,
                            )
                        },
                    ),
                )
                onApplyRecommendation(automaticPlan)
                selectionDistributionTelemetry.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = setupAuditId,
                        dimensions = setupDimensions,
                    selectedFamilyId = setupFamily(automaticPlan),
                    ),
                )
                selectedRecommendationStyle = automaticPlan.style
                appliedRecommendationStyle = automaticPlan.style
            }
        }
    }
    val executionThreshold = (publicAliveCards.size + 1) / 2
    val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
    val scriptHasSlayer = "Slayer" in scriptRoleNames
    val scriptHasArtist = "Artist" in scriptRoleNames
    val slayerClaimantCandidates = publicAliveCards.filter { card ->
        card.name !in slayerClaimedNames && !(slayerUsed && card.clocktowerRole?.enName == "Slayer")
    }
    val artistClaimantCandidates = publicAliveCards.filter { card ->
        card.name !in artistClaimedNames && !(artistUsed && card.clocktowerRole?.enName == "Artist")
    }

    fun roleActor(enName: String): PlayerCard? {
        if (phase != ClocktowerPhase.Night) {
            return cards.firstOrNull {
                AbilityFunctioningSemantics.interactsAs(
                    it.abilitySubject(null),
                    enName,
                )
            }
        }
        val interactionId = ClocktowerProductionNightStepIdentity
            .role(RoleId(enName))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)

        if (interactionId !in otherNightCanonicalInteractionIds) {
            return null
        }

        return cards.firstOrNull { candidate ->
            val effectiveSubject =
                effectiveAbilitySubjectForRole(enName, candidate)
                    ?: return@firstOrNull false

            AbilityFunctioningSemantics.interactsAs(
                effectiveSubject,
                enName,
            )
        }
    }

    fun roleMissingReason(enName: String): String {
        val roleCard = actualClocktowerRoleCards(cards, enName).firstOrNull()
        val drunkShownAsRole = cards.firstOrNull { it.clocktowerRole?.enName == "Drunk" && it.clocktowerShownRole?.enName == enName }
        return when {
            roleCard == null && drunkShownAsRole != null -> ""
            roleCard == null -> text("本局没有这个角色。", "This character is not in play.")
            roleCard.eliminatedRound != null -> text(
                "${roleCard.seatLabel(cards)} 已经死亡，死亡后不再执行这个能力。",
                "${roleCard.seatLabel(cards)} is dead and no longer uses this ability.",
            )
            else -> ""
        }
    }

    fun stableIndex(key: String, size: Int): Int = if (size <= 0) 0 else Math.floorMod(key.hashCode(), size)
    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        (effectiveAbilitySubjectForRole(enName, actor)?.let { subject ->
            AbilityFunctioningSemantics.stateFor(subject, enName)
        } in
            setOf(AbilityFunctioningState.DRUNK, AbilityFunctioningState.POISONED)
        )
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
        proposition: InformationProposition? = null,
        recommendationStyle: RecommendationStyle = RecommendationStyle.BALANCED,
        isTruthful: Boolean = true,
        misinformationPressure: Int = 0,
        isDefaultRecommendation: Boolean = false,
        reasonCodes: List<String> = emptyList(),
        warningCodes: List<String> = emptyList(),
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = kind,
        displayTitle = title,
        displayPrimary = primary,
        displaySecondary = secondary,
        displayFooter = footer,
        proposition = proposition,
        recommendationStyle = recommendationStyle,
        isTruthful = isTruthful,
        misinformationPressure = misinformationPressure,
        isD…35488 tokens truncated…on.GrimoireState(
                        cards.mapIndexed { index, card -> GrimoireSeatView(index + 1, RoleId(requireNotNull(card.clocktowerRole).enName), card.eliminatedRound == null) },
                    ),
                    hostInstruction = if (effectivePoisonForRole("Spy") == spyCard?.name) text("照常唤醒间谍，但不要展示真实魔典。", "Wake the Spy normally, but do not show the real grimoire.") else text("轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。", "Tap the Spy to wake them. Briefly show the Storyteller overview, then take the phone back and signal them to close their eyes."),
                )
            },
        ),
        ),
    )
        otherNightMaterializers.materialize(otherNightInteractions)
    }

    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            cards = cards,
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
            onHostTools = onHostTools,
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
            onHostTools = onHostTools,
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
        val dayTableState = clocktowerDayOverviewTableState(
            cards.toClocktowerGameState(
                script = script,
                seed = gameSeed,
                poisonedPlayerName = poisonTarget,
            ),
            roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
            ghostVoteAuthority = ghostVoteAuthority,
        )
        ClocktowerDayOverviewScreen(
            round = round,
            tableState = dayTableState,
            aliveCount = publicAliveCards.size,
            executionThreshold = executionThreshold,
            highestVoteText = highestVoteText,
            showSlayerAction = scriptHasSlayer,
            slayerActionEnabled = slayerClaimantCandidates.isNotEmpty(),
            showArtistAction = scriptHasArtist,
            artistActionEnabled = artistClaimantCandidates.isNotEmpty(),
            actionsEnabled = gameOutcome == null,
            diagnosticContent = null,
            onHostTools = onHostTools,
            onNominationGesture = { sourceSeatId, targetSeatId ->
                val sourceName = dayTableState.seats
                    .firstOrNull { seat -> seat.seatId == sourceSeatId && seat.isAlive }
                    ?.playerName
                val targetName = dayTableState.seats
                    .firstOrNull { seat -> seat.seatId == targetSeatId && seat.isAlive }
                    ?.playerName
                if (sourceName != null && targetName != null && sourceName != targetName) {
                    nominatorName = sourceName
                    nomineeName = targetName
                    dayMode = ClocktowerDayMode.Nomination
                }
            },
            onOpenSlayer = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
                dayMode = ClocktowerDayMode.Slayer
            },
            onOpenArtist = {
                selectArtistClaimant(null)
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
        val virginFirstNomination = nomineeCard?.let {
            AbilityFunctioningSemantics.interactsAs(it.abilitySubject(poisonTarget), "Virgin")
        } == true && !virginUsed
        val virginAbilityWorks = nomineeCard?.let {
            AbilityFunctioningSemantics.functionsAs(it.abilitySubject(poisonTarget), "Virgin")
        } == true && virginFirstNomination
        val virginRegistrationKey = nominatorCard
            ?.takeIf { it.name == spyCard?.name && virginFirstNomination }
            ?.let { registrationKey("Virgin", it.name) }
        val virginSpyLegalRoles = completeTroubleBrewingRoles
            .filter { it.team == ClocktowerTeam.Townsfolk && it.enName != "Spy" }
        val virginSpyRecommendations = if (virginRegistrationKey != null && spyCard != null) {
            registrationRecommendationOptions(
                key = virginRegistrationKey,
                roleEnName = "Virgin",
                teams = listOf(ClocktowerTeam.Townsfolk),
                detail = ClocktowerRegistrationDetail.Role,
                subject = spyCard,
                isSpy = true,
                outcomeMisinformationPressure = 5,
            )
        } else {
            emptyList()
        }
        val automaticVirginSpyRegistration = if (
            automaticStorytellerInfo &&
            virginRegistrationKey != null &&
            spyCard != null &&
            spyCanRegister("Virgin")
        ) {
            clocktowerTemporaryRegistrationSelection(
                legalSpecialRoleEnNames = virginSpyLegalRoles.map { it.enName },
                decisionKey = clocktowerTemporaryRegistrationDecisionKey(
                    gameId = gameId,
                    phase = phase,
                    round = round,
                    registrationKey = virginRegistrationKey,
                ),
            ).selected.payload
        } else {
            null
        }
        val virginSpyRegistersGood = automaticVirginSpyRegistration?.usesSpecialRegistration
            ?: spyRegistersGood(virginRegistrationKey, "Virgin")
        val virginExecutes = virginAbilityWorks &&
            (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || virginSpyRegistersGood)
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
        ClocktowerPendingNominationTableScreen(
            round = round,
            cards = cards,
            tableState = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            ),
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            specialNotice = specialNotice,
            specialNoticeIsDanger = virginExecutes,
            continueLabel = when {
                virginExecutes -> text("确认并处决提名者", "Confirm and execute nominator")
                virginFirstNomination -> text("记录能力，进入投票", "Record ability and continue")
                else -> text("开始投票", "Start voting")
            },
            actionsEnabled = gameOutcome == null,
            onHostTools = onHostTools,
            onContinue = {
                val chosenNominator = nominatorName
                val chosenNominee = nomineeName
                if (chosenNominator != null && chosenNominee != null && virginFirstNomination && virginExecutes) {
                    onPreflightVirginExecution(
                        chosenNominator,
                        spyRegistrationWillRecord(virginRegistrationKey),
                    )
                }
                if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {
                    if (
                        automaticStorytellerInfo &&
                        virginRegistrationKey != null &&
                        automaticVirginSpyRegistration != null
                    ) {
                        spyRegistrationGood[virginRegistrationKey] =
                            automaticVirginSpyRegistration.usesSpecialRegistration
                        if (automaticVirginSpyRegistration.usesSpecialRegistration) {
                            automaticVirginSpyRegistration.registeredRoleEnName?.let { roleEnName ->
                                spyRegistrationRole[virginRegistrationKey] = roleEnName
                            }
                        }
                    }
                    recordSpyRegistration(virginRegistrationKey, listOf(ClocktowerTeam.Townsfolk), "Virgin")
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
                    dayMode = ClocktowerDayMode.Vote
                }
            },
            onCancel = {
                nominatorName = null
                nomineeName = null
                dayMode = ClocktowerDayMode.Overview
            },
            specialContent = {
                if (!automaticStorytellerInfo && virginRegistrationKey != null && spyCard != null) {
                    ClocktowerSpyRegistrationDecisionControls(
                        recommendations = virginSpyRecommendations,
                        legalRoles = virginSpyLegalRoles.map { it.enName to it.nameFor(language) },
                        registersGood = spyRegistersGood(virginRegistrationKey, "Virgin"),
                        registeredRoleEnName = spyRegistrationRole[virginRegistrationKey],
                        enabled = spyCanRegister("Virgin"),
                        language = language,
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
        val recordVoteEvent: (ClocktowerConfirmedVoteRecord) -> Unit = { voteRecord ->
            val voterDetail = voteRecord.voterDetail(
                playerLabel = { playerName -> playerSeatLabel(cards, playerName) },
                ghostVoteSuffix = text("（幽灵票）", " (ghost vote)"),
                noVotesLabel = text("无人投票", "No votes"),
            )
            onRecordEvent(
                ClocktowerEventType.Vote,
                text("提名与投票", "Nomination and vote"),
                "${playerSeatLabel(cards, nominatorName)} → ${playerSeatLabel(cards, nomineeName)} · ${voteRecord.voteCount}/$executionThreshold · ${text("投票人：", "Voters: ")}$voterDetail",
                listOfNotNull(nominatorName, nomineeName) + voteRecord.voters.map { voter -> voter.playerName },
            )
        }
        ClocktowerVoteTableScreen(
            round = round,
            cards = cards,
            ghostVoteAuthority = ghostVoteAuthority,
            tableState = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            ),
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            highestVoteText = highestVoteText,
            actionsEnabled = gameOutcome == null,
            onHostTools = onHostTools,
            onConfirm = { voteState ->
                val voteTransaction = commitClocktowerVoteTransaction(
                    voteState = voteState,
                    nomineeName = requireNotNull(nomineeName) { "Confirmed vote requires nominee" },
                    executionThreshold = executionThreshold,
                    highestVoteName = highestVoteName,
                    highestVoteCount = highestVoteCount,
                )
                onGhostVoteAuthorityChange(voteTransaction.ghostVoteAuthority)
                highestVoteName = voteTransaction.highestVoteName
                highestVoteCount = voteTransaction.highestVoteCount
                recordVoteEvent(voteTransaction.voteRecord)
                nominatorName = null
                nomineeName = null
                dayMode = ClocktowerDayMode.Overview
            },
            onCancel = {
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
            onHostTools = onHostTools,
            onConfirm = onConfirmDay,
            onBack = { dayMode = ClocktowerDayMode.Overview },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Slayer) {
        val slayerTargetCard = cards.firstOrNull { it.name == slayerTargetName }
        val slayerRecluseRecommendations = slayerTargetCard
            ?.takeIf { it.clocktowerRole?.enName == "Recluse" }
            ?.let { recluse ->
                registrationRecommendationOptions(
                    key = registrationKey("SlayerRecluse", recluse.name),
                    roleEnName = "Slayer",
                    teams = listOf(ClocktowerTeam.Demon),
                    detail = ClocktowerRegistrationDetail.Role,
                    subject = recluse,
                    isSpy = false,
                    outcomeMisinformationPressure = 4,
                    specialRegistrationBalanceImpact = 1,
                )
            }
            .orEmpty()
        val automaticSlayerRecluseRegistration = WeightedStableSelector.selectStyle(
            slayerRecluseRecommendations,
            automaticStorytellerStyle,
            ClocktowerRegistrationRecommendationOption::style,
        )
        val slayerTableState = clocktowerSlayerTableState(
            seats = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            ).seats,
            claimantCandidateNames = slayerClaimantCandidates.mapTo(mutableSetOf()) { it.name },
            alivePlayerNames = publicAliveCards.mapTo(mutableSetOf()) { it.name },
            claimantName = slayerClaimantName,
            targetName = slayerTargetName,
        )
        ClocktowerSlayerTableScreen(
            round = round,
            tableState = slayerTableState,
            actionsEnabled = gameOutcome == null,
            onHostTools = onHostTools,
            onSeatClick = { seatId ->
                val selectedName = slayerTableState.playerNameForSeat(seatId)
                if (slayerClaimantName == null) {
                    slayerClaimantName = selectedName
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = false
                } else {
                    slayerTargetName = if (slayerTargetName == selectedName) null else selectedName
                    slayerRecluseRegistersDemon = false
                }
            },
            onResetClaimant = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
            },
            onResolve = {
                val claimantName = slayerClaimantName
                val targetName = slayerTargetName
                if (claimantName != null && targetName != null) {
                    val targetIsHealthyRecluse =
                        slayerTargetCard?.clocktowerRole?.enName == "Recluse" &&
                            poisonTarget != targetName
                    val recluseRegistersDemon = if (
                        automaticStorytellerInfo &&
                        targetIsHealthyRecluse &&
                        automaticSlayerRecluseRegistration != null
                    ) {
                        automaticSlayerRecluseRegistration.usesSpecialRegistration
                    } else {
                        slayerRecluseRegistersDemon
                    }
                    onSlayerShot(claimantName, targetName, recluseRegistersDemon)
                    slayerClaimantName = null
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = false
                    dayMode = ClocktowerDayMode.Overview
                }
            },
            onBack = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
                dayMode = ClocktowerDayMode.Overview
            },
            specialContent = {
                if (!automaticStorytellerInfo && slayerTargetCard?.clocktowerRole?.enName == "Recluse") {
                    ClocktowerRecluseRegistrationDecisionControls(
                        recommendations = slayerRecluseRecommendations,
                        legalRoles = completeTroubleBrewingRoles
                            .filter { it.team == ClocktowerTeam.Demon }
                            .map { it.enName to it.nameFor(language) },
                        registersEvil = slayerRecluseRegistersDemon,
                        registeredRoleEnName = if (slayerRecluseRegistersDemon) "Imp" else null,
                        enabled = poisonTarget != slayerTargetName,
                        language = language,
                        onRegistersEvilChange = { slayerRecluseRegistersDemon = it },
                        onRoleChange = {},
                    )
                }
            },
        )
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Artist) {
        val artistClaimant = cards.firstOrNull { it.name == artistClaimantName }
        val currentArtistTruthfulAnswer = artistTruthfulAnswer
        val artistReliable = artistClaimant?.let {
            it.clocktowerRole?.enName == "Artist" && it.name != poisonTarget
        } == true
        val answerRecommendations = if (artistClaimant != null && currentArtistTruthfulAnswer != null) {
            if (artistReliable) {
                listOf(Triple(RecommendationStyle.BALANCED, currentArtistTruthfulAnswer, false))
            } else {
                recommendationCoordinator.recommendCategory(
                    listOf(
                        UnreliableCategoricalCandidate(
                            id = "yes",
                            isTruthful = currentArtistTruthfulAnswer,
                            misinformationPressure = if (currentArtistTruthfulAnswer) 0 else 3,
                        ),
                        UnreliableCategoricalCandidate(
                            id = "no",
                            isTruthful = !currentArtistTruthfulAnswer,
                            misinformationPressure = if (currentArtistTruthfulAnswer) 3 else 0,
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
        } else {
            emptyList()
        }
        val artistInformationReliability = when {
            artistClaimant?.name == poisonTarget -> InformationReliability.POISONED
            artistClaimant?.clocktowerRole?.enName == "Drunk" &&
                artistClaimant.clocktowerShownRole?.enName == "Artist" ->
                InformationReliability.DRUNK
            else -> InformationReliability.RELIABLE
        }
        val automaticArtistRecommendation = if (artistInformationReliability != InformationReliability.RELIABLE) {
            recommendationCoordinator.selectInformation(
                options = answerRecommendations,
                reliability = artistInformationReliability,
                style = automaticStorytellerStyle,
                evilAdvantage = currentDynamicStorytellerState.evilAdvantage,
                stableKey = "$recommendationKey:artist:$round:${artistClaimant?.name}",
                recentMisinformationStreak = recentMisinformationStreak(artistClaimant),
                stableIdOf = { "${it.first.name}:${it.second}" },
                isTruthful = { it.second == currentArtistTruthfulAnswer },
                misinformationPressure = { if (it.second == currentArtistTruthfulAnswer) 0 else 3 },
                styleOf = { it.first },
            )
        } else {
            WeightedStableSelector.selectStyle(
                answerRecommendations,
                automaticInformationStyle,
            ) { it.first }
        }
        val automaticArtistAnswer = automaticArtistRecommendation?.second
        LaunchedEffect(automaticStorytellerInfo, artistClaimantName, currentArtistTruthfulAnswer, automaticArtistAnswer) {
            if (automaticStorytellerInfo && automaticArtistAnswer != null && artistShownAnswer != automaticArtistAnswer) {
                selectArtistShownAnswer(automaticArtistAnswer)
            }
        }
        val artistTableState = clocktowerArtistTableState(
            seats = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            ).seats,
            claimantCandidateNames = artistClaimantCandidates.mapTo(mutableSetOf()) { it.name },
            claimantName = artistClaimantName,
        )
        ClocktowerArtistTableScreen(
            round = round,
            tableState = artistTableState,
            actionsEnabled = gameOutcome == null,
            primaryEnabled = artistClaimantName != null &&
                currentArtistTruthfulAnswer != null &&
                artistShownAnswer != null &&
                gameOutcome == null,
            onHostTools = onHostTools,
            onSeatClick = { seatId ->
                val claimant = artistTableState.playerNameForSeat(seatId)
                selectArtistClaimant(if (artistClaimantName == claimant) null else claimant)
            },
            onPrimary = confirmArtistQuestion,
            onBack = {
                selectArtistClaimant(null)
                dayMode = ClocktowerDayMode.Overview
            },
        ) {
            if (artistClaimant != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                HostActionSection(title = text("问题的真实答案", "Truthful answer")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(true, false).forEach { answer ->
                            val label = if (answer) text("是", "Yes") else text("否", "No")
                            if (currentArtistTruthfulAnswer == answer) {
                                Button(
                                    onClick = { selectArtistTruthfulAnswer(answer) },
                                    modifier = Modifier.weight(1f),
                                ) { Text(label) }
                            } else {
                                OutlinedButton(
                                    onClick = { selectArtistTruthfulAnswer(answer) },
                                    modifier = Modifier.weight(1f),
                                ) { Text(label) }
                            }
                        }
                    }
                }
            }
            if (artistClaimant != null && currentArtistTruthfulAnswer != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                HostActionSection(title = text("告诉玩家的答案", "Answer to show")) {
                    answerRecommendations
                        .filter { !automaticStorytellerInfo || it == automaticArtistRecommendation }
                        .forEach { (style, answer, warning) ->
                            val answerLabel = if (answer) text("是", "Yes") else text("否", "No")
                            val label = if (artistReliable) {
                                answerLabel
                            } else {
                                "${recommendationStyleLabel(style)} · $answerLabel${if (warning) text(" · 高影响", " · high impact") else ""}"
                            }
                            if (automaticStorytellerInfo) {
                                Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            } else if (artistShownAnswer == answer) {
                                Button(
                                    onClick = { selectArtistShownAnswer(answer) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(label) }
                            } else {
                                OutlinedButton(
                                    onClick = { selectArtistShownAnswer(answer) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(label) }
                            }
                        }
                }
            }
        }
        return
    }

    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Klutz) {
        val klutzChoiceCard = cards.firstOrNull { it.name == klutzChoiceName }
        val klutzRegistrationKey = klutzChoiceCard
            ?.takeIf { it.name == spyCard?.name }
            ?.let { registrationKey("Klutz", it.name) }
        val klutzSpyRecommendations = if (klutzRegistrationKey != null && spyCard != null) {
            registrationRecommendationOptions(
                key = klutzRegistrationKey,
                roleEnName = "Klutz",
                teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                detail = ClocktowerRegistrationDetail.Role,
                subject = spyCard,
                isSpy = true,
                outcomeMisinformationPressure = 5,
                specialRegistrationBalanceImpact = -1,
            )
        } else {
            emptyList()
        }
        val automaticKlutzSpyRegistration = WeightedStableSelector.selectStyle(
            klutzSpyRecommendations,
            automaticStorytellerStyle,
            ClocktowerRegistrationRecommendationOption::style,
        )
        val klutzTableState = clocktowerKlutzTableState(
            seats = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            ).seats,
            klutzName = pendingKlutzName,
            alivePlayerNames = publicAliveCards.mapTo(mutableSetOf()) { it.name },
            choiceName = klutzChoiceName,
        )
        ClocktowerKlutzTableScreen(
            round = round,
            tableState = klutzTableState,
            actionsEnabled = gameOutcome == null,
            onHostTools = onHostTools,
            onSeatClick = { seatId ->
                val playerName = klutzTableState.playerNameForSeat(seatId)
                onSelectKlutzChoice(if (klutzChoiceName == playerName) null else playerName)
            },
            onConfirm = {
                if (
                    automaticStorytellerInfo &&
                    spyCanRegister("Klutz") &&
                    klutzRegistrationKey != null &&
                    automaticKlutzSpyRegistration != null
                ) {
                    spyRegistrationGood[klutzRegistrationKey] =
                        automaticKlutzSpyRegistration.usesSpecialRegistration
                    if (automaticKlutzSpyRegistration.usesSpecialRegistration) {
                        automaticKlutzSpyRegistration.registeredRoleEnName?.let {
                            spyRegistrationRole[klutzRegistrationKey] = it
                        }
                    }
                }
                recordSpyRegistration(
                    klutzRegistrationKey,
                    listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                    "Klutz",
                )
                onConfirmKlutzChoice(spyRegistersGood(klutzRegistrationKey, "Klutz"))
            },
            specialContent = {
                if (!automaticStorytellerInfo && klutzRegistrationKey != null && spyCard != null) {
                    ClocktowerSpyRegistrationDecisionControls(
                        recommendations = klutzSpyRecommendations,
                        legalRoles = completeTroubleBrewingRoles
                            .filter { it.team in listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider) && it.enName != "Spy" }
                            .map { it.enName to it.nameFor(language) },
                        registersGood = spyRegistersGood(klutzRegistrationKey, "Klutz"),
                        registeredRoleEnName = spyRegistrationRole[klutzRegistrationKey],
                        enabled = spyCanRegister("Klutz"),
                        language = language,
                        onRegistersGoodChange = { good ->
                            spyRegistrationGood[klutzRegistrationKey] = good
                            if (good && spyRegistrationRole[klutzRegistrationKey] == null) {
                                spyRegistrationRole[klutzRegistrationKey] = "Washerwoman"
                            }
                        },
                        onRoleChange = { spyRegistrationRole[klutzRegistrationKey] = it },
                    )
                }
            },
        )
        return
    }

    if (phase == ClocktowerPhase.FirstNight && !nightStarted) {
        ClocktowerStorytellerRecommendationScreen(
            title = text("身份展示完成", "IDENTITY DISPLAY COMPLETE"),
            subtitle = text("准备进入首夜", "Prepare for the first night"),
            description = text(
                "这是说书人私密页面。可返回最后一位玩家重新展示身份，或确认首夜裁定后开始夜晚。",
                "This is a private Storyteller screen. You may return to the final player to show the role again, or confirm the first-night rulings and begin the night.",
            ),
            buttonLabel = text("确认裁定，开始首夜", "Confirm plan and begin first night"),
            onHostTools = onHostTools,
            onPrevious = onPreviousFromFirstNightReady,
            onStartNight = {
                if (firstNightNaturalPairPrecomputeReady) {
                    nightStarted = true
                } else {
                    firstNightNaturalPairStartRequested = true
                    if (firstNightNaturalPairLoadFailed) {
                        firstNightNaturalPairRetryGeneration += 1
                    }
                }
            },
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
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.replaceWith(nextLockedDecisions)
                    selectedRecommendationStyle = automaticStorytellerStyle
                    appliedRecommendationStyle = null
                },
                onClearLocks = {
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.clear()
                    selectedRecommendationStyle = automaticStorytellerStyle
                },
            )
        }
        return
    }

    if (phase == ClocktowerPhase.Night && !nightStarted) {
        ClocktowerStorytellerRecommendationScreen(
            title = text("说书人", "STORYTELLER"),
            subtitle = text("第 $round 夜即将开始", "Night $round is about to begin"),
            description = text(
                "这是说书人私密页面。准备好后开始夜晚流程。",
                "This is a private Storyteller screen. Begin the night flow when ready.",
            ),
            buttonLabel = text("开始第 $round 夜流程", "Begin night $round"),
            onHostTools = onHostTools,
            onStartNight = { nightStarted = true },
        ) {
            ClocktowerNightReadyCard()
        }
        return
    }

    if ((phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) && nightStarted) {
        require(nightSteps.isNotEmpty()) { "A started night must contain an actionable step." }
        val currentStepIndex = nightStepIndex.coerceIn(0, nightSteps.lastIndex)
        val currentStep = nightSteps[currentStepIndex]
        val currentSurfacePlan = clocktowerNightSurfacePlan(currentStep, phase)
        val selectedNightName = when (currentStep.action) {
            ClocktowerNightAction.RedHerring -> redHerring
            ClocktowerNightAction.Poison -> poisonDraftTarget
            ClocktowerNightAction.ButlerMaster -> butlerMaster
            ClocktowerNightAction.MonkProtect -> monkProtectedDraftTarget
            ClocktowerNightAction.DemonKill -> demonAttackDraftTarget
            ClocktowerNightAction.MayorRedirect -> mayorRedirectDraftTarget
            ClocktowerNightAction.DemonSuccessor -> demonSuccessorTarget
            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
            else -> null
        }
        val advanceNightStep = {
            if (currentStep.action == ClocktowerNightAction.Poison) {
                onConfirmPoisonTarget()
            }
            if (currentStep.action == ClocktowerNightAction.MonkProtect) {
                onConfirmMonkProtectedTarget()
            }
            if (currentStep.action == ClocktowerNightAction.DemonKill) {
                onConfirmDemonAttack()
            }
            if (currentStep.action == ClocktowerNightAction.MayorRedirect) {
                if (automaticStorytellerInfo) {
                    val autoOptions = unifiedDecisionPool(currentStep.decisionOptions, "mayor-redirect")
                        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
                        ?.map { it.payload }
                        .orEmpty()
                    val selected = WeightedStableSelector.selectStyle(
                        autoOptions,
                        automaticStorytellerStyle,
                        ClocktowerDecisionOption::recommendationStyle,
                    )
                    if (selected != null && mayorRedirectDraftTarget == selected.targetName) {
                        val auditId = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}|mayor-redirect"
                        val dimensions = SelectionAuditDimensions(
                            playerCount = cards.size,
                            phase = StorytellerPhase.NIGHT,
                            style = automaticStorytellerStyle,
                        )
                        selectionDistributionTelemetry.recordPreview(
                            SelectionAuditRecord(
                                selectionId = auditId,
                                dimensions = dimensions,
                                candidates = currentStep.decisionOptions.map { option ->
                                    SelectionAuditCandidate("mayor-redirect", if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING)
                                },
                            ),
                        )
                        selectionDistributionTelemetry.recordCommittedSelection(
                            SelectionAuditCommit(auditId, dimensions, "mayor-redirect"),
                        )
                    }
                }
                onConfirmMayorRedirectTarget()
            }
            if (currentStep.action == ClocktowerNightAction.DemonSuccessor && automaticStorytellerInfo) {
                val autoOptions = unifiedDecisionPool(currentStep.decisionOptions, "demon-succession")
                    ?.candidatesFor(SelectionExecutionPolicy.AUTO)
                    ?.map { it.payload }
                    .orEmpty()
                val selected = WeightedStableSelector.selectStyle(
                    autoOptions,
                    automaticStorytellerStyle,
                    ClocktowerDecisionOption::recommendationStyle,
                )
                if (selected != null && demonSuccessorTarget == selected.targetName) {
                    val auditId = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}|demon-succession"
                    val dimensions = SelectionAuditDimensions(cards.size, StorytellerPhase.NIGHT, automaticStorytellerStyle)
                    selectionDistributionTelemetry.recordPreview(
                        SelectionAuditRecord(
                            selectionId = auditId,
                            dimensions = dimensions,
                            candidates = currentStep.decisionOptions.map { option ->
                                SelectionAuditCandidate("demon-succession", if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING)
                            },
                        ),
                    )
                    selectionDistributionTelemetry.recordCommittedSelection(
                        SelectionAuditCommit(auditId, dimensions, "demon-succession"),
                    )
                }
            }
            if (currentStep.action == ClocktowerNightAction.DemonSuccessor) {
                val selectedTarget = requireNotNull(demonSuccessorTarget) {
                    "Demon successor confirmation requires a selected target."
                }
                require(demonSuccessorTargetCards.any { it.name == selectedTarget }) {
                    "Demon successor confirmation requires a rules-legal target."
                }
                onConfirmDemonSuccessorTarget(selectedTarget)
            }
            currentStep.spyRegistrationKey?.let { key ->
                currentStep.roleEnName?.let { role ->
                    recordSpyRegistration(key, currentStep.spyRegistrationTeams, role, currentStep.spyRegistrationDetail)
                }
            }
            currentStep.recluseRegistrationKey?.let { key ->
                currentStep.roleEnName?.let { role ->
                    recordRecluseRegistration(key, currentStep.recluseRegistrationTeams, role)
                }
            }
            recordNightStep(currentStep)
            val flowMayExpandAfterConfirmation = currentStep.action in setOf(
                ClocktowerNightAction.DemonKill,
                ClocktowerNightAction.MayorRedirect,
            )
            when (
                val directive = clocktowerNightAdvanceDirective(
                    currentStepIndex = currentStepIndex,
                    currentStepCount = nightSteps.size,
                    flowMayExpandAfterConfirmation = flowMayExpandAfterConfirmation,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> nightStepIndex = directive.stepIndex
                is ClocktowerNightAdvanceDirective.AwaitRefreshedFlow -> pendingNightAdvance = directive
                ClocktowerNightAdvanceDirective.CompleteNight -> onConfirmNight()
            }
        }

        LaunchedEffect(nightSteps.size, pendingNightAdvance) {
            val pending = pendingNightAdvance ?: return@LaunchedEffect
            when (
                val directive = clocktowerRefreshedNightAdvanceDirective(
                    pending = pending,
                    refreshedStepCount = nightSteps.size,
                )
            ) {
                is ClocktowerNightAdvanceDirective.MoveTo -> {
                    pendingNightAdvance = null
                    nightStepIndex = directive.stepIndex
                }
                is ClocktowerNightAdvanceDirective.AwaitRefreshedFlow ->
                    error("Refreshed night advance cannot remain pending.")
                ClocktowerNightAdvanceDirective.CompleteNight -> {
                    pendingNightAdvance = null
                    onConfirmNight()
                }
            }
        }

        LaunchedEffect(
            automaticStorytellerInfo,
            currentStepIndex,
            currentStep.action,
            currentStep.isRealAction,
            redHerring,
        ) {
            if (
                shouldAutoAdvanceRedHerring(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    isRedHerringStep = currentStep.action == ClocktowerNightAction.RedHerring,
                    isRealAction = currentStep.isRealAction,
                    hasSelectedRedHerring = redHerring != null,
                )
            ) {
                advanceNightStep()
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
            onPrevious = onMovePreviousNightStep,
            onHostTools = onHostTools,
            onNext = advanceNightStep,
            contentOwnsFullScreen = currentSurfacePlan.ownsFullScreenHostSurface,
        ) {
            ClocktowerNightStepCardLocalized(
                recommendationCoordinator = recommendationCoordinator,
                automaticStorytellerInfo = automaticStorytellerInfo,
                automaticStorytellerStyle = automaticStorytellerStyle,
                phase = phase,
                gameId = gameId,
                round = round,
                sequence = currentStepIndex,
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                selectionDistributionTelemetry = selectionDistributionTelemetry,
                evilAdvantage = currentDynamicStorytellerState.evilAdvantage,
                informationDecisionKey = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}",
                cards = cards,
                ghostVoteAuthority = ghostVoteAuthority,
                aliveCards = publicAliveCards,
                chambermaidTargetCards = chambermaidTargetCards,
                mayorRedirectTargetCards = mayorRedirectTargetCards,
                demonSuccessorTargetCards = demonSuccessorTargetCards,
                step = currentStep,
                surfacePlan = currentSurfacePlan,
                spyCard = spyCard,
                spyRegistrationGood = if (currentStep.spyRegistrationKey != null && currentStep.roleEnName != null) {
                    spyRegistersGood(currentStep.spyRegistrationKey, currentStep.roleEnName)
                } else false,
                spyRegisteredRoleEnName = currentStep.spyRegistrationKey?.let { spyRegistrationRole[it] },
                spyRegistrationRecommendations = registrationRecommendationOptions(currentStep, spyCard, isSpy = true),
                spyCanRegister = if (currentStep.spyRegistrationKey != null && currentStep.roleEnName != null) {
                    spyCanRegister(currentStep.roleEnName)
                } else false,
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
                recluseRegistrationEvil = if (currentStep.recluseRegistrationKey != null && currentStep.roleEnName != null) {
                    recluseRegistersEvil(currentStep.recluseRegistrationKey, currentStep.roleEnName)
                } else false,
                recluseRegisteredRoleEnName = currentStep.recluseRegistrationKey?.let { recluseRegistrationRole[it] },
                recluseRegistrationRecommendations = registrationRecommendationOptions(currentStep, recluseCard, isSpy = false),
                recluseCanRegister = if (currentStep.recluseRegistrationKey != null && currentStep.roleEnName != null) {
                    recluseCanRegister(currentStep.roleEnName)
                } else false,
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
                chambermaidFirst = chambermaidResolution.selection.first,
                chambermaidSecond = chambermaidResolution.selection.second,
                onSelectName = { name ->
                    val nextSelection = clocktowerToggledSingleTargetSelection(
                        currentSelection = selectedNightName,
                        tappedSelection = name,
                    )
                    when (currentStep.action) {
                        ClocktowerNightAction.RedHerring -> onSelectRedHerring(nextSelection)
                        ClocktowerNightAction.Poison -> onSelectPoisonTarget(nextSelection)
                        ClocktowerNightAction.ButlerMaster -> onSelectButlerMaster(nextSelection)
                        ClocktowerNightAction.MonkProtect -> onSelectMonkProtectedTarget(nextSelection)
                        ClocktowerNightAction.DemonKill -> onSelectNightDeath(nextSelection)
                        ClocktowerNightAction.MayorRedirect -> onSelectMayorRedirectTarget(nextSelection)
                        ClocktowerNightAction.DemonSuccessor -> onSelectDemonSuccessor(nextSelection)
                        ClocktowerNightAction.Ravenkeeper -> onSelectRavenkeeperTarget(nextSelection)
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
                    currentStep.spyRegistrationKey?.let { key ->
                        currentStep.roleEnName?.let { role ->
                            recordSpyRegistration(key, currentStep.spyRegistrationTeams, role, currentStep.spyRegistrationDetail)
                        }
                    }
                    currentStep.recluseRegistrationKey?.let { key ->
                        currentStep.roleEnName?.let { role ->
                            recordRecluseRegistration(key, currentStep.recluseRegistrationTeams, role)
                        }
                    }
                },
                onShowPlayerDisplay = { displayStep ->
                    performClocktowerPlayerRevealHandoff(
                        authorize = { informationDecisionPublicationAllowed(displayStep) },
                        publishFirstNight = { publishFirstNightInformation(displayStep) },
                        recordPrivateInformation = { recordReliablePrivateInformation(displayStep) },
                        recordHistory = {
                            val actor = displayStep.actor
                            val unreliable = clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)
                            val primary = displayStep.displayPrimary ?: displayStep.tellPlayer
                            val secondary = displayStep.displaySecondary
                            val recordDetail = when (displayStep.displayKind) {
                                ClocktowerDisplayKind.EitherOne ->
                                    if (primary != null && secondary != null)
                                        text("$primary 在 ${secondary.trim().replace("   ", " / ")} 号之中", "$primary: seats ${secondary.trim().replace("   ", " / ")}")
                                    else primary.orEmpty()
                                ClocktowerDisplayKind.Number ->
                                    if (primary != null)
                                        text("${displayStep.displayFooter.orEmpty()}：$primary", "${displayStep.displayFooter.orEmpty()}: $primary")
                                    else primary.orEmpty()
                                ClocktowerDisplayKind.YesNo ->
                                    if (secondary != null && primary != null)
                                        text("查验 ${secondary.trim().replace("   ", " + ")} 号：$primary", "Checked seats ${secondary.trim().replace("   ", " + ")}: $primary")
                                    else primary.orEmpty()
                                ClocktowerDisplayKind.RoleReveal ->
                                    primary.orEmpty()
                                ClocktowerDisplayKind.Grimoire ->
                                    text("间谍查看了魔典", "Spy viewed the grimoire")
                                else ->
                                    primary.orEmpty()
                            }
                            val referencedPlayerNames = DecisionHistoryRepository.extractSeatNumbers(
                                values = listOf(displayStep.displaySecondary, displayStep.displayFooter),
                                maximumSeat = cards.size,
                            ).mapNotNull { seat -> cards.getOrNull(seat - 1)?.name }
                            onRecordEvent(
                                if (unreliable) ClocktowerEventType.UnreliableInformation else ClocktowerEventType.Information,
                                if (unreliable) {
                                    if (displayStep.selectedInformationTruthful == false) {
                                        text("${displayStep.displayTitle}（误导）", "${displayStep.displayTitle} (misleading)")
                                    } else {
                                        text("${displayStep.displayTitle}（不可靠）", "${displayStep.displayTitle} (unreliable)")
                                    }
                                } else {
                                    displayStep.displayTitle
                                },
                                recordDetail,
                                (listOfNotNull(actor?.name) + referencedPlayerNames).distinct(),
                            )
                        },
                        openReveal = { playerDisplayStep = displayStep },
                    )
                },
                canGoPrevious = currentStepIndex > 0,
                onPrevious = onMovePreviousNightStep,
                onHostTools = onHostTools,
                onNext = advanceNightStep,
            )
        }
        return
    }
}
