package com.codex.campboardgamehost

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolDeviceBenchmark
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPoolDeviceBenchmarkReport
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ClocktowerNightStepCardLocalized(
    recommendationCoordinator: ClocktowerRecommendationCoordinator,
    automaticStorytellerInfo: Boolean,
    automaticStorytellerStyle: RecommendationStyle,
    phase: ClocktowerPhase,
    gameId: String,
    round: Int,
    sequence: Int,
    gameStateRevision: Long,
    playerInputRevision: Long,
    debugDiagnosticsExpanded: Boolean,
    selectionDistributionTelemetry: SelectionDistributionTelemetryRecorder,
    evilAdvantage: Int,
    informationDecisionKey: String,
    cards: List<PlayerCard>,
    aliveCards: List<PlayerCard>,
    chambermaidTargetCards: List<PlayerCard>,
    mayorRedirectTargetCards: List<PlayerCard>,
    demonSuccessorTargetCards: List<PlayerCard>,
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
    fun optionId(option: ClocktowerDisplayOption): String = clocktowerInformationCandidateId(option)
    // B7.3's first production slice: a single complete first-night pool is
    // projected differently by execution policy. Later-night families retain
    // their legacy lists until individually migrated.
    fun projectFirstNightCandidateSource(
        source: List<ClocktowerDisplayOption>,
    ): List<ClocktowerDisplayOption> {
        val numericSourceSeat = source
            .asSequence()
            .mapNotNull { (it.proposition as? InformationProposition.NumericResult)?.sourceSeat }
            .firstOrNull()
        return numericSourceSeat?.let { sourceSeat ->
            val poisonedPlayerName = cards
                .getOrNull(sourceSeat - 1)
                ?.name
                ?.takeIf { step.informationReliability == InformationReliability.POISONED }
            projectFirstNightNumericInformationOptions(
                phase = phase,
                roleEnName = step.roleEnName.orEmpty(),
                sourceSeat = sourceSeat,
                players = cards.toClocktowerPlayerStates(poisonedPlayerName = poisonedPlayerName),
                options = source,
            )
        } ?: source
    }
    val projectedFirstNightInformationCandidates =
        projectFirstNightCandidateSource(step.legacyInformationCandidates)
    val projectedAutomaticFirstNightInformationCandidates = projectFirstNightCandidateSource(
        step.automaticInformationCandidates.ifEmpty { step.legacyInformationCandidates },
    )
    val firstNightAutomaticPool = projectedAutomaticFirstNightInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: "first-night-information",
            automaticStyle = automaticStorytellerStyle,
        ) }
    // The legacy assisted recommendation surface keeps the curated compatibility pool.
    // Pair Manual selection below consumes step.manualInformationCandidates directly.
    val firstNightPool = projectedFirstNightInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: "first-night-information",
            automaticStyle = automaticStorytellerStyle,
        ) }
    var firstNightPoolBenchmarkRuns by remember(
        phase,
        step.roleEnName,
        projectedAutomaticFirstNightInformationCandidates,
        automaticStorytellerStyle,
    ) { mutableStateOf(0) }
    var firstNightPoolBenchmarkReport by remember { mutableStateOf<UnifiedSelectionPoolDeviceBenchmarkReport?>(null) }
    var firstNightPoolBenchmarkError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(firstNightPoolBenchmarkRuns) {
        if (firstNightPoolBenchmarkRuns == 0) return@LaunchedEffect
        firstNightPoolBenchmarkReport = null
        firstNightPoolBenchmarkError = null
        runCatching {
            val options = requireNotNull(projectedAutomaticFirstNightInformationCandidates.takeIf {
                phase == ClocktowerPhase.FirstNight && it.isNotEmpty()
            })
            val family = step.roleEnName ?: "first-night-information"
            withContext(Dispatchers.Default) {
                UnifiedSelectionPoolDeviceBenchmark.run(
                    poolFactory = {
                        unifiedFirstNightInformationPool(options, family, automaticStorytellerStyle)
                    },
                    playerCount = cards.size,
                    phase = StorytellerPhase.FIRST_NIGHT,
                    style = automaticStorytellerStyle,
                    styleOf = ClocktowerDisplayOption::recommendationStyle,
                )
            }
        }.onSuccess { report ->
            firstNightPoolBenchmarkReport = report
            Log.i(
                UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG,
                report.toLogLine(step.roleEnName ?: "first-night-information"),
            )
        }.onFailure { error ->
            firstNightPoolBenchmarkError = error.message ?: error.javaClass.simpleName
            Log.e(UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG, "Unified first-night pool diagnostic failed", error)
        }
    }
    val automaticInformationOptions = firstNightAutomaticPool
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?: step.recommendedDisplayOptions
    val assistedInformationOptions = firstNightPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: step.recommendedDisplayOptions
    val displayedInformationOptions = if (automaticStorytellerInfo) automaticInformationOptions else assistedInformationOptions
    val pairRecommendationPresentation = if (
        !automaticStorytellerInfo &&
        phase == ClocktowerPhase.FirstNight &&
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator")
    ) {
        clocktowerRecommendationPresentation(displayedInformationOptions)
    } else {
        null
    }
    val manualPairCandidates = if (
        !automaticStorytellerInfo &&
        phase == ClocktowerPhase.FirstNight &&
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator")
    ) {
        step.manualInformationCandidates
    } else {
        emptyList()
    }
    val usesResultFirstRegistration =
        !automaticStorytellerInfo && step.usesResultFirstRegistrationDomain()
    val resultFirstRegistrationCandidates = if (usesResultFirstRegistration) {
        distinctClocktowerFinalInformationResults(step.manualInformationCandidates)
    } else {
        emptyList()
    }
    val nonPairResultFirstCandidates = resultFirstRegistrationCandidates.takeUnless {
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator") ||
            step.action == ClocktowerNightAction.FortuneTeller
    }.orEmpty()

    var showManualPairSelection by remember(
        informationDecisionKey,
        step.roleEnName,
    ) { mutableStateOf(false) }
    val dynamicDecisionFamily = when (step.action) {
        ClocktowerNightAction.MayorRedirect -> "mayor-redirect"
        ClocktowerNightAction.DemonSuccessor -> "demon-succession"
        else -> null
    }
    val dynamicDecisionPool = dynamicDecisionFamily
        ?.let { family -> unifiedDecisionPool(step.decisionOptions, family) }
    val automaticDecisionOptions = dynamicDecisionPool
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?: step.decisionOptions
    val assistedDecisionOptions = dynamicDecisionPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: step.decisionOptions
    val automaticDecision = WeightedStableSelector.selectStyle(
        automaticDecisionOptions,
        automaticStorytellerStyle,
        ClocktowerDecisionOption::recommendationStyle,
    )
    val automaticDecisionTargetName = automaticDecision?.targetName
        ?: demonSuccessorTargetCards.singleOrNull()?.takeIf {
            step.action == ClocktowerNightAction.DemonSuccessor
        }?.name
    val selectionAudit = if (automaticStorytellerInfo) {
        SelectionAuditContext(
            selectionId = informationDecisionKey,
            dimensions = SelectionAuditDimensions(
                playerCount = cards.size,
                phase = if (phase == ClocktowerPhase.FirstNight) StorytellerPhase.FIRST_NIGHT else StorytellerPhase.NIGHT,
                style = automaticStorytellerStyle,
            ),
            recorder = selectionDistributionTelemetry,
        )
    } else {
        null
    }
    val automaticDisplayOption = recommendationCoordinator.selectInformation(
        options = automaticInformationOptions,
        reliability = step.informationReliability,
        style = automaticStorytellerStyle,
        evilAdvantage = evilAdvantage,
        stableKey = informationDecisionKey,
        recentMisinformationStreak = step.recentMisinformationStreak,
        stableIdOf = ::optionId,
        isTruthful = ClocktowerDisplayOption::isTruthful,
        misinformationPressure = ClocktowerDisplayOption::misinformationPressure,
        styleOf = ClocktowerDisplayOption::recommendationStyle,
        selectionAudit = selectionAudit,
    )
    fun numericOptionValue(option: ClocktowerDisplayOption?): Int? =
        (option?.proposition as? InformationProposition.NumericResult)?.value
            ?: option?.displayPrimary?.toIntOrNull()
    val structuredEmpathActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val structuredEmpathSubjectSeats = step
        .legacyInformationCandidates
        .asSequence()
        .mapNotNull { it.proposition as? InformationProposition.NumericResult }
        .firstOrNull { it.metric == NumericMetric.LIVING_EVIL_NEIGHBOURS }
        ?.subjectSeats
        ?: (step.displayProposition as? InformationProposition.NumericResult)
            ?.takeIf { it.metric == NumericMetric.LIVING_EVIL_NEIGHBOURS }
            ?.subjectSeats
        ?: emptyList()
    val structuredEmpathTruthValue = step
        .takeIf { it.roleEnName == "Empath" }
        ?.legacyInformationCandidates
        ?.firstOrNull { it.isTruthful }
        ?.let(::numericOptionValue)
        ?: (step.displayProposition as? InformationProposition.NumericResult)?.value
        ?: step.tellPlayer?.toIntOrNull()
    fun structuredEmpathSelectionIsTruthful(value: Int): Boolean =
        projectedFirstNightInformationCandidates
            .firstOrNull { numericOptionValue(it) == value }
            ?.isTruthful
            ?: (value == structuredEmpathTruthValue)
    val structuredEmpathRecommendedOption = if (automaticStorytellerInfo) {
        automaticDisplayOption
    } else {
        displayedInformationOptions.firstOrNull { it.isDefaultRecommendation }
            ?: step.displayOptions.firstOrNull { it.isDefaultRecommendation }
            ?: automaticDisplayOption
    }
    val structuredEmpathRecommendedValue = numericOptionValue(structuredEmpathRecommendedOption)
    val structuredEmpathUiModel = if (
        step.roleEnName == "Empath" &&
        step.spyRegistrationKey == null &&
        step.recluseRegistrationKey == null &&
        structuredEmpathActorSeat != null &&
        structuredEmpathSubjectSeats.isNotEmpty() &&
        structuredEmpathTruthValue != null
    ) {
        prepareEmpathNumberInformationUiModel(
            coordinator = recommendationCoordinator,
            gameId = gameId,
            phase = phase,
            round = round,
            sequence = sequence,
            actorSeat = structuredEmpathActorSeat,
            subjectSeats = structuredEmpathSubjectSeats,
            trueValue = structuredEmpathTruthValue,
            reliability = step.informationReliability,
            recommendationStyle = if (automaticStorytellerInfo) automaticStorytellerStyle else RecommendationStyle.BALANCED,
            revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
            recommendedValue = structuredEmpathRecommendedValue,
            previousShownValue = step.previousShownNumber,
            pressureCostPerPoint = 1,
        )
    } else {
        null
    }
    val structuredChefActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val structuredChefProposition = (step.displayProposition as? InformationProposition.NumericResult)
        ?.takeIf { it.metric == NumericMetric.ADJACENT_EVIL_PAIRS }
    val structuredChefUiModel = if (
        step.roleEnName == "Chef" &&
        step.informationReliability != InformationReliability.RELIABLE &&
        step.spyRegistrationKey == null &&
        step.recluseRegistrationKey == null &&
        structuredChefActorSeat != null &&
        structuredChefProposition != null &&
        step.numericMinimumValue != null &&
        step.numericMaximumValue != null
    ) {
        prepareNumericInformationUiModel(
            coordinator = recommendationCoordinator,
            gameId = gameId,
            phase = phase,
            round = round,
            sequence = sequence,
            actorSeat = structuredChefActorSeat,
            abilityRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = structuredChefProposition.subjectSeats,
            trueValue = structuredChefProposition.value,
            minimumValue = step.numericMinimumValue,
            maximumValue = step.numericMaximumValue,
            reliability = step.informationReliability,
            recommendationStyle = if (automaticStorytellerInfo) automaticStorytellerStyle else RecommendationStyle.BALANCED,
            revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
            recommendedValue = structuredEmpathRecommendedValue,
            previousShownValue = step.previousShownNumber,
            pressureCostPerPoint = 1,
        )
    } else {
        null
    }
    val structuredFortuneTellerActorSeat = step.actor
        ?.let { actor -> cards.indexOf(actor).plus(1).takeIf { it > 0 } }
    val fortuneTellerSelectedSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
        .mapNotNull { selectedName ->
            cards.indexOfFirst { it.name == selectedName }
                .takeIf { it >= 0 }
                ?.plus(1)
        }
    val structuredFortuneTellerSelectedSeats = fortuneTellerSelectedSeats
        .takeIf { seats -> seats.size == 2 && seats.distinct().size == 2 }
    val fortuneTellerSelectableSeats = when (fortuneTellerSelectedSeats.size) {
        0 -> cards.indices.mapTo(linkedSetOf()) { index -> index + 1 }
        1 -> cards.indices
            .map { index -> index + 1 }
            .filterTo(linkedSetOf()) { seat -> seat !in fortuneTellerSelectedSeats }
        else -> emptySet()
    }
    val structuredFortuneTellerProposition = (step.displayProposition as? InformationProposition.BooleanResult)
        ?.takeIf { proposition ->
            proposition.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT &&
                proposition.sourceSeat == structuredFortuneTellerActorSeat &&
                proposition.subjectSeats == structuredFortuneTellerSelectedSeats
        }
    val structuredFortuneTellerRecommendedOption = if (automaticStorytellerInfo) {
        automaticDisplayOption
    } else {
        displayedInformationOptions.firstOrNull { it.isDefaultRecommendation }
            ?: step.displayOptions.firstOrNull { it.isDefaultRecommendation }
            ?: automaticDisplayOption
    }
    val structuredFortuneTellerRecommendedValue =
        (structuredFortuneTellerRecommendedOption?.proposition as? InformationProposition.BooleanResult)
            ?.takeIf { proposition ->
                proposition.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT &&
                    proposition.sourceSeat == structuredFortuneTellerActorSeat &&
                    proposition.subjectSeats == structuredFortuneTellerSelectedSeats
            }
            ?.value
    val structuredFortuneTellerUiModel = if (
        step.action == ClocktowerNightAction.FortuneTeller &&
        step.roleEnName == "Fortune Teller" &&
        structuredFortuneTellerActorSeat != null &&
        structuredFortuneTellerSelectedSeats != null &&
        structuredFortuneTellerProposition != null
    ) {
        prepareBooleanInformationUiModel(
            coordinator = recommendationCoordinator,
            gameId = gameId,
            phase = phase,
            round = round,
            sequence = sequence,
            actorSeat = structuredFortuneTellerActorSeat,
            abilityRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = structuredFortuneTellerSelectedSeats,
            trueValue = structuredFortuneTellerProposition.value,
            reliability = step.informationReliability,
            recommendationStyle = if (automaticStorytellerInfo) automaticStorytellerStyle else RecommendationStyle.BALANCED,
            revision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
            recommendedValue = structuredFortuneTellerRecommendedValue,
        )
    } else {
        null
    }
    val resultFirstFortuneTellerOptions = resultFirstRegistrationCandidates.filter { option ->
        val proposition = option.proposition as? InformationProposition.BooleanResult
        proposition?.metric == BooleanMetric.DEMON_OR_RED_HERRING_PRESENT
    }
    val fortuneTellerLegalResults = if (resultFirstFortuneTellerOptions.isNotEmpty()) {
        resultFirstFortuneTellerOptions.mapNotNullTo(linkedSetOf()) { option ->
            (option.proposition as? InformationProposition.BooleanResult)?.value
        }
    } else {
        structuredFortuneTellerUiModel
            ?.choices
            ?.mapTo(linkedSetOf()) { choice -> choice.value }
            .orEmpty()
    }
    val fortuneTellerRecommendedResult = structuredFortuneTellerUiModel
        ?.choices
        ?.firstOrNull { choice -> choice.recommended && choice.value in fortuneTellerLegalResults }
        ?.value
    val structuredNumberUiModel = structuredEmpathUiModel ?: structuredChefUiModel

    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {
        onApplyRecommendedDisplayOption(option)
        selectionAudit?.let { audit ->
            audit.recorder.recordCommittedSelection(
                SelectionAuditCommit(
                    selectionId = audit.selectionId,
                    dimensions = audit.dimensions,
                    selectedFamilyId = DynamicCandidateGenerator.selectionAuditFamilyId(
                        reliability = step.informationReliability,
                        truthful = option.isTruthful,
                    ),
                ),
            )
        }
        onShowPlayerDisplay(
            step.copy(
                tellPlayer = option.displayPrimary,
                displayKind = option.displayKind,
                displayTitle = option.displayTitle,
                displayPrimary = option.displayPrimary,
                displaySecondary = option.displaySecondary,
                displayFooter = option.displayFooter,
                displayProposition = option.proposition,
                selectedInformationTruthful = option.isTruthful,
                displayOptions = emptyList(),
                recommendedDisplayOptions = emptyList(),
            ),
        )
    }

    fun showStructuredFortuneTellerResult(value: Boolean) {
        resultFirstFortuneTellerOptions.firstOrNull { option ->
            (option.proposition as? InformationProposition.BooleanResult)?.value == value
        }?.let { option ->
            showRecommendedDisplayOption(option)
            return
        }
        val model = structuredFortuneTellerUiModel ?: return
        val actorSeat = structuredFortuneTellerActorSeat ?: return
        val subjectSeats = structuredFortuneTellerSelectedSeats ?: return
        val choice = model.choices.firstOrNull { it.value == value } ?: return
        val currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision)
        val confirmation = if (choice.recommended) {
            model.acceptRecommendation(choice.candidateId, currentRevision)
        } else {
            model.chooseManually(choice.candidateId, currentRevision)
        }
        val confirmed = confirmation.confirmed ?: return
        val selectedOption = findBooleanDisplayOption(
            options = (
                displayedInformationOptions +
                    step.displayOptions +
                    step.legacyInformationCandidates
                ).distinctBy(::optionId),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = actorSeat,
            subjectSeats = subjectSeats,
            value = value,
        )
        val displayStep = selectedOption?.let { option ->
            step.copy(
                tellPlayer = option.displayPrimary,
                displayKind = option.displayKind,
                displayTitle = option.displayTitle,
                displayPrimary = option.displayPrimary,
                displaySecondary = option.displaySecondary,
                displayFooter = option.displayFooter,
                displayProposition = confirmed.draft.proposition,
                selectedInformationTruthful = option.isTruthful,
                informationDecisionConfirmation = confirmed,
                informationDecisionExpectedSnapshot = model.contextSnapshot,
                displayOptions = emptyList(),
                recommendedDisplayOptions = emptyList(),
            )
        } ?: step.copy(
            displayProposition = confirmed.draft.proposition,
            informationDecisionConfirmation = confirmed,
            informationDecisionExpectedSnapshot = model.contextSnapshot,
            displayOptions = emptyList(),
            recommendedDisplayOptions = emptyList(),
        )
        onShowPlayerDisplay(displayStep)
    }

    LaunchedEffect(automaticStorytellerInfo, step.title, automaticDecisionTargetName) {
        if (automaticStorytellerInfo && automaticDecisionTargetName != null && selectedName != automaticDecisionTargetName) {
            onSelectName(automaticDecisionTargetName)
        }
    }
    val command = when {
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
            if (language == "en") {
                "Ask ${step.actor.seatLabel(cards)} to choose two players to check"
            } else {
                "让 ${step.actor.seatLabel(cards)} 选择两名想要查验的玩家"
            }
        }
        step.wakeText != null -> step.wakeText
        step.actor != null -> if (language == "en") "Wake ${step.actor.seatLabel(cards)}" else "唤醒 ${step.actor.seatLabel(cards)}"
        else -> step.title
    }
    val helper = when {
        step.action == ClocktowerNightAction.Chambermaid -> if (language == "en") "Have them choose two players, then show only the number." else "让她选择两名玩家，点查询后直接展示数字。"
        step.action == ClocktowerNightAction.FortuneTeller -> if (language == "en") "Have them choose two players, then show Yes or No." else "让他选择两名玩家，点查询后展示“有”或“没有”。"
        step.action == ClocktowerNightAction.RedHerring -> if (language == "en") "Choose a good player as the red herring. The Fortune Teller detects that player as a Demon." else "选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。"
        step.action == ClocktowerNightAction.Poison -> if (language == "en") "Record the poisoned player." else "记录中毒的玩家。"
        step.action == ClocktowerNightAction.ButlerMaster -> if (language == "en") "Have the Butler choose a master. During voting, the Butler may vote only if their master votes." else "让他选择主人。白天计票时，只有主人投票时，管家才能投票，需要管家自律。"
        step.action == ClocktowerNightAction.MonkProtect -> if (language == "en") "Record the protected player." else "记录被保护的玩家。"
        step.action == ClocktowerNightAction.DemonKill -> if (language == "en") "Record the selected kill target." else "记录被击杀的玩家。"
        step.action == ClocktowerNightAction.MayorRedirect -> if (language == "en") "Let the Mayor die or redirect the death to another player." else "选择让市长死亡，或将死亡转移给另一名玩家。"
        step.action == ClocktowerNightAction.DemonSuccessor -> if (language == "en") "Choose an eligible living Minion to become the new Imp." else "选择一名合法的存活爪牙成为新的小恶魔。"
        step.action == ClocktowerNightAction.Ravenkeeper -> if (language == "en") "After choosing a target, show that character only to the Ravenkeeper." else "选目标后，把该玩家角色只给他看。"
        step.displayKind != ClocktowerDisplayKind.None -> if (language == "en") "Show the information to the player." else "展示信息给玩家。"
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

            if (!usesResultFirstRegistration && step.spyRegistrationKey != null && spyCard != null && spyCanRegister) {
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
                    selectionAudit = selectionAudit?.copy(selectionId = "$informationDecisionKey|spy-registration"),
                    onRegistersGoodChange = onSpyRegistrationGoodChange,
                    onRoleChange = onSpyRegistrationRoleChange,
                )
            }
            if (!usesResultFirstRegistration && step.recluseRegistrationKey != null && recluseCard != null && recluseCanRegister) {
                RecluseRegistrationPanel(
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    automaticStorytellerStyle = automaticStorytellerStyle,
                    cards = cards,
                    recluse = recluseCard,
                    teams = step.recluseRegistrationTeams,
                    registersEvil = recluseRegistrationEvil,
                    registeredRoleEnName = recluseRegisteredRoleEnName,
                    recommendations = recluseRegistrationRecommendations,
                    enabled = recluseCanRegister,
                    selectionAudit = selectionAudit?.copy(selectionId = "$informationDecisionKey|recluse-registration"),
                    onRegistersEvilChange = onRecluseRegistrationEvilChange,
                    onRoleChange = onRecluseRegistrationRoleChange,
                )
            }
            if (step.decisionOptions.isNotEmpty()) {
                HostActionSection(
                    title = if (language == "en") "Recommended ruling" else "推荐裁定",
                    helper = if (automaticStorytellerInfo) {
                        if (language == "en") "The selected automatic ruling has been applied." else "已采用当前自动模式的裁定。"
                    } else if (language == "en") {
                        "The balanced option is the beginner default. You can still choose manually below."
                    } else {
                        if (language == "en") "The balanced option is the beginner default; you can still choose manually below." else "平衡方案是新手默认建议；仍可在下方手动裁定。"
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

        val nightActionSeats = cards.mapIndexed { index, card ->
            ClocktowerNightActionSeatUiModel(
                seatId = "seat-${index + 1}",
                seatNumber = index + 1,
                label = card.name,
            )
        }
        fun seatNumberForName(name: String?): Int? = name
            ?.let { selected -> cards.indexOfFirst { card -> card.name == selected } }
            ?.takeIf { index -> index >= 0 }
            ?.plus(1)
        fun selectableSeatNumbers(candidates: List<PlayerCard>): Set<Int> = candidates
            .mapNotNull { candidate -> seatNumberForName(candidate.name) }
            .toSet()

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
                val candidates = aliveCards
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_poison_target),
                    helper = null,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }

            ClocktowerNightAction.ButlerMaster -> {
                val candidates = cards.filter { it.name != step.actor?.name }
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = if (language == "en") "Choose the Butler's master" else "选择管家的主人",
                    helper = null,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }

            ClocktowerNightAction.MonkProtect -> {
                val candidates = clocktowerMonkTargetCards(cards, step.actor?.name)
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }

            ClocktowerNightAction.FortuneTeller -> {
                ClocktowerFortuneTellerSquareTableDialog(
                    seats = cards.mapIndexed { index, card ->
                        ClocktowerFortuneTellerSeatUiModel(
                            seatId = "seat-${index + 1}",
                            seatNumber = index + 1,
                            label = card.name,
                        )
                    },
                    selectedSeats = fortuneTellerSelectedSeats,
                    selectableSeats = fortuneTellerSelectableSeats,
                    enabled = step.isRealAction,
                    legalResults = fortuneTellerLegalResults,
                    recommendedResult = fortuneTellerRecommendedResult,
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let { name ->
                            when (twoPlayerSelectionAction(fortuneTellerFirst, fortuneTellerSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> onSelectFortuneTellerFirst(name)
                                TwoPlayerSelectionAction.ToggleSecond -> onSelectFortuneTellerSecond(name)
                                TwoPlayerSelectionAction.RejectLimit -> Unit
                            }
                        }
                    },
                    onResultSelected = ::showStructuredFortuneTellerResult,
                    onAutomaticResultSelected = ::showStructuredFortuneTellerResult,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }

            ClocktowerNightAction.Chambermaid -> {
                val candidates = chambermaidTargetCards.filter { it.name != step.actor?.name }
                val selectedSeats = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull(::seatNumberForName)
                val candidateSeats = selectableSeatNumbers(candidates)
                val selectableSeats = when (selectedSeats.size) {
                    0 -> candidateSeats
                    1 -> candidateSeats - selectedSeats.first()
                    else -> emptySet()
                }
                val resultOptions = when {
                    step.displayOptions.isEmpty() -> emptyList()
                    automaticStorytellerInfo -> listOfNotNull(automaticDisplayOption)
                    else -> step.displayOptions
                }
                ClocktowerChambermaidSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeats = selectedSeats,
                    selectableSeats = selectableSeats,
                    enabled = step.isRealAction,
                    resultOptions = resultOptions,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let { name ->
                            when (twoPlayerSelectionAction(chambermaidFirst, chambermaidSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> onSelectChambermaidFirst(name)
                                TwoPlayerSelectionAction.ToggleSecond -> onSelectChambermaidSecond(name)
                                TwoPlayerSelectionAction.RejectLimit -> Unit
                            }
                        }
                    },
                    onShowDeterminedResult = { onShowPlayerDisplay(step) },
                    onResultSelected = ::showRecommendedDisplayOption,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }

            ClocktowerNightAction.DemonKill -> {
                val candidates = aliveCards
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_night_death),
                    helper = stringResource(R.string.clocktower_host_choose_night_death_hint),
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }

            ClocktowerNightAction.MayorRedirect -> {
                val mayor = aliveCards.firstOrNull { it.clocktowerRole?.enName == "Mayor" }
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == "en") "The Demon attacked the Mayor" else "市长被恶魔击杀",
                    helper = if (language == "en") "Choosing a dead or protected player as the redirect target can result in no death tonight." else "选择死亡或受保护的玩家作为转移目标，可能导致今夜无人死亡。",
                ) {
                    if (mayor != null) {
                        if (selectedName == mayor.name) {
                            Button(
                                onClick = { onSelectName(mayor.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (language == "en") "Mayor dies" else "市长死亡")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(mayor.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (language == "en") "Mayor dies" else "市长死亡")
                            }
                        }
                        Text(if (language == "en") "Or redirect the death to:" else "或将死亡转移给：", fontWeight = FontWeight.SemiBold)
                        SelectablePlayerChips(
                        cards = mayorRedirectTargetCards,
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
                if (!automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == "en") "Choose the new Imp" else "选择新小恶魔",
                    helper = step.explanation,
                ) {
                    SelectablePlayerChips(
                        cards = demonSuccessorTargetCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
                }
            }

            ClocktowerNightAction.Ravenkeeper -> {
                val candidates = clocktowerRavenkeeperTargetCards(cards)
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                    secondaryActionLabel = stringResource(R.string.clocktower_host_show_to_player),
                    secondaryActionEnabled = resultFirstRegistrationCandidates.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None,
                    onSecondaryAction = { onShowPlayerDisplay(step) },
                )
            }

            else -> Unit
        }

            step.tellPlayer
                ?.takeIf { step.isRealAction && it.isNotBlank() && step.displayKind == ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid }
                ?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

            structuredNumberUiModel?.let { model ->
                val template = structuredEmpathRecommendedOption
                    ?: displayedInformationOptions.firstOrNull()
                    ?: step.displayOptions.firstOrNull()
                StructuredNumberInformationDecisionPanel(
                    model = model,
                    currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    language = language,
                    roleLabel = step.title,
                    onConfirmed = { confirmed, value ->
                        if (automaticDisplayOption != null) {
                            selectionAudit?.let { audit ->
                                audit.recorder.recordCommittedSelection(
                                    SelectionAuditCommit(
                                        selectionId = audit.selectionId,
                                        dimensions = audit.dimensions,
                                        selectedFamilyId = DynamicCandidateGenerator.selectionAuditFamilyId(
                                            reliability = step.informationReliability,
                                            truthful = structuredEmpathSelectionIsTruthful(value),
                                        ),
                                    ),
                                )
                            }
                        }
                        onShowPlayerDisplay(
                            step.copy(
                                tellPlayer = value.toString(),
                                displayKind = ClocktowerDisplayKind.Number,
                                displayTitle = template?.displayTitle ?: step.displayTitle,
                                displayPrimary = value.toString(),
                                displaySecondary = template?.displaySecondary ?: step.displaySecondary,
                                displayFooter = template?.displayFooter ?: step.displayFooter ?: step.explanation,
                                displayProposition = confirmed.draft.proposition,
                                selectedInformationTruthful = structuredEmpathSelectionIsTruthful(value),
                                informationDecisionConfirmation = confirmed,
                                informationDecisionExpectedSnapshot = structuredNumberUiModel.contextSnapshot,
                                displayOptions = emptyList(),
                                recommendedDisplayOptions = emptyList(),
                            ),
                        )
                    },
                )
            }

            pairRecommendationPresentation?.let { presentation ->
                ClocktowerPairRecommendationPresentationSection(
                    presentation = presentation,
                    language = language,
                    onSelect = ::showRecommendedDisplayOption,
                )
            }

            if (
                pairRecommendationPresentation == null &&
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                resultFirstRegistrationCandidates.isEmpty() &&
                displayedInformationOptions.isNotEmpty() &&
                step.action != ClocktowerNightAction.FortuneTeller
            ) {
                Text(if (language == "en") "Recommended information" else "推荐给说书人的完整信息", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    if (automaticStorytellerInfo) {
                        if (language == "en") "The automatic mode selected this information. Use the button below to show it." else "已按当前自动模式选定信息；点击下方按钮即可向玩家展示。"
                    } else {
                        if (language == "en") "The balanced option is the default; other options apply different pressure. Choosing one also updates this interaction's Spy or Recluse registration." else "平衡方案适合直接采用；其他方案提供不同压力。选择后会同步本次间谍或隐士登记。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                displayedInformationOptions
                    .filter { !automaticStorytellerInfo || it == automaticDisplayOption }
                    .sortedBy { if (it.isDefaultRecommendation) 0 else 1 }
                    .forEach { option ->
                        val onClick = {
                            showRecommendedDisplayOption(option)
                        }
                        if (option.isDefaultRecommendation) {
                            Button(
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text("${if (automaticStorytellerInfo) { if (language == "en") "Auto" else "自动" } else { if (language == "en") "Default" else "默认" }} · ${option.label}")
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
                if (!automaticStorytellerInfo && firstNightPool == null) {
                    OutlinedButton(
                        onClick = { onShowPlayerDisplay(step.copy(recommendedDisplayOptions = emptyList())) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(if (language == "en") "Use the manual ruling above" else "使用上方手动裁定")
                    }
                }
            }


            if (nonPairResultFirstCandidates.isNotEmpty()) {
                Text(
                    if (language == "en") "Choose the final information" else "选择最终展示信息",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (language == "en") {
                        "Any Spy or Recluse registration needed for the chosen result is resolved automatically."
                    } else {
                        "选择结果即可；该结果所需的间谍或隐士登记会自动完成。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                nonPairResultFirstCandidates.forEach { option ->
                    OutlinedButton(
                        onClick = { showRecommendedDisplayOption(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                }
            }

            if (manualPairCandidates.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showManualPairSelection = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(if (language == "en") "Manually choose clue" else "手动选择展示信息")
                }
                if (showManualPairSelection) {
                    ClocktowerPairManualSelectionDialog(
                        interactionKey = informationDecisionKey,
                        candidates = manualPairCandidates,
                        seats = cards.mapIndexed { index, card ->
                            ClocktowerPairManualSeatUiModel(
                                seatId = "seat-${index + 1}",
                                seatNumber = index + 1,
                                label = card.name,
                            )
                        },
                        roleLabel = { roleId -> clocktowerRoleLabel(com.codex.campboardgamehost.clocktower.domain.RoleId(roleId), language) },
                        onDismiss = { showManualPairSelection = false },
                        onConfirm = { manualOption ->
                            showRecommendedDisplayOption(manualOption)
                            showManualPairSelection = false
                        },
                    )
                }
            }

            if (
                resultFirstRegistrationCandidates.isEmpty() &&
                structuredNumberUiModel == null &&
                structuredFortuneTellerUiModel == null &&
                firstNightPool == null && step.displayOptions.isNotEmpty() &&
                step.action != ClocktowerNightAction.FortuneTeller
            ) {
                Text(if (language == "en") "This ability is unreliable. Choose a result to show." else "能力不可靠：请选择一个结果展示。", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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
                                    displayProposition = option.proposition,
                                    selectedInformationTruthful = option.isTruthful,
                                    displayOptions = emptyList(),
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                    RecommendationReasonSummary(option.reasonCodes, option.warningCodes, language)
                }
            } else if (resultFirstRegistrationCandidates.isEmpty() && structuredNumberUiModel == null && step.recommendedDisplayOptions.isEmpty() && step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None && step.action != ClocktowerNightAction.FortuneTeller && step.action != ClocktowerNightAction.Chambermaid) {
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
