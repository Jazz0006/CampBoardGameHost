package com.codex.campboardgamehost

import android.widget.Toast
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.kind
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionFirstNightFlow
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationResolution
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.recommendation.SelectionPoolParityRecorder
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationMigration
import com.codex.campboardgamehost.clocktower.session.FirstNightShadowResult
import com.codex.campboardgamehost.clocktower.session.FirstNightPublicationResolution
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerJudgeScreen(
    automaticStorytellerInfo: Boolean,
    cards: List<PlayerCard>,
    events: List<ClocktowerEvent>,
    script: ClocktowerScript,
    gameId: String,
    gameSeed: Long,
    firstNightPairDecisionContext: TroubleBrewingFirstNightPairDecisionContext? = null,
    globalAiAssisted: Boolean = false,
    oneShotEnabled: Boolean = false,
    oneShotPlan: com.codex.campboardgamehost.clocktower.session.FirstNightOneShotPlanV1? = null,
    oneShotBusy: Boolean = false,
    oneShotError: String? = null,
    oneShotAccepted: Boolean = false,
    adoptedOneShotChoices: Map<String, String> = emptyMap(),
    onRequestOneShot: (com.codex.campboardgamehost.clocktower.session.FirstNightOneShotScopeV1) -> Unit = {},
    onAdoptOneShot: (com.codex.campboardgamehost.clocktower.session.FirstNightOneShotPlanV1,
        com.codex.campboardgamehost.clocktower.session.FirstNightOneShotScopeV1) -> Unit = { _, _ -> },
    globalNightPendingDecision: PendingMayorRedirectDecision? = null,
    globalAiAdviceKey: String? = null,
    globalAiAdviceText: String? = null,
    globalAiAdviceBusy: Boolean = false,
    globalAiAdviceError: String? = null,
    onRequestGlobalAiAdvice: (ClocktowerNightStepUi, Int) -> Unit = { _, _ -> },
    firstNightNaturalPairReadyProvider: ((TroubleBrewingFirstNightPairDecisionContext) -> List<DecisionCandidate<SetupClueOutcome>>?)? = null,
    firstNightNaturalPairResultProvider: (suspend (TroubleBrewingFirstNightPairDecisionContext) -> List<DecisionCandidate<SetupClueOutcome>>)? = null,
    phase: ClocktowerPhase,
    round: Int,
    nightCheckpoint: ClocktowerNightCheckpoint,
    selectedExecution: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    ravenkeeperTarget: String?,
    redHerring: String?,
    recommendedDemonBluffRoleNames: List<String>,
    butlerMaster: String?,
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
    onCommitConfirmedInformationDecision: (ConfirmedInformationDecision) -> Unit,
    onCommitConfirmedRegistrationResult: (ClocktowerConfirmedRegistrationPublicationV1) -> Unit,
    onPreflightConfirmedRegistrationResult: (ClocktowerConfirmedRegistrationPublicationV1) -> Boolean,
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
    onCommitDemonBluffs: (List<String>) -> Unit,
    onSelectButlerMaster: (String?) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onConfirmMonkProtectedTarget: () -> Unit,
    onSelectMayorRedirectTarget: (String?) -> Unit,
    onConfirmMayorRedirectTarget: () -> Unit,
    onSelectDemonSuccessor: (String?) -> Unit,
    onConfirmDemonSuccessorTarget: (String) -> Unit,
    onConfirmNewDemon: () -> Unit,
    onSelectKlutzChoice: (String?) -> Unit,
    onConfirmKlutzChoice: () -> Unit,
    onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onPreflightVirginExecution: (String, Boolean) -> Unit,
    onVirginNomination: (String, String, Boolean, Boolean?) -> Unit,
    onConfirmedNomination: (String, String) -> Unit,
    onConfirmedVote: (String, String, ClocktowerConfirmedVoteRecord) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: () -> Unit,
    onConfirmNight: () -> Unit,
) {
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val gameStateRevision = nightCheckpoint.gameStateRevision
    val playerInputRevision = nightCheckpoint.playerInputRevision
    val pendingNightDeath = nightCheckpoint.confirmedAttackTarget
    val demonAttackDraftTarget = nightCheckpoint.attackDraftTarget
    val poisonTarget = nightCheckpoint.confirmedPoisonTarget
    val poisonDraftTarget = nightCheckpoint.poisonDraftTarget
    val monkProtectedTarget = nightCheckpoint.confirmedMonkTarget
    val monkProtectedDraftTarget = nightCheckpoint.monkDraftTarget
    val mayorRedirectTarget = nightCheckpoint.confirmedMayorRedirectTarget
    val mayorRedirectDraftTarget = nightCheckpoint.mayorRedirectDraftTarget
    val pendingNewDemonName = nightCheckpoint.pendingNewDemonName
    val pendingNightNewDemonIdentityName = nightCheckpoint.pendingNightNewDemonIdentityName
    val demonSuccessorTarget = nightCheckpoint.demonSuccessorDraftTarget
    val recommendationCoordinator = remember(gameSeed) { ClocktowerRecommendationCoordinator() }
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
            displayStep,
            phase,
            round,
            cards,
            script,
            gameSeed,
            poisonTarget,
            language,
            firstNightPairDecisionContext,
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
    val registrationState = remember { ClocktowerRegistrationInteractionState() }
    val recordedNightSteps = remember { mutableStateMapOf<String, Boolean>() }
    var effectivePoisonForRole: (String) -> String? = { poisonTarget }
    var effectiveRoleForRegistration: (String, PlayerCard) -> RoleId? = { _, card ->
        card.clocktowerRole?.enName?.let(::RoleId)
    }
    fun registrationKey(ability: String, subject: String = "spy") = "${phase.name}:$round:$ability:$subject"
    fun registrationSubject(
        queryingRoleEnName: String,
        card: PlayerCard,
    ): TroubleBrewingRegistrationSubject? {
        val actualRole = card.clocktowerRole ?: return null
        val seat = cards.indexOf(card).plus(1).takeIf { it > 0 } ?: return null
        val actualType = when (actualRole.team) {
            ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
            ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
            ClocktowerTeam.Minion -> CharacterType.MINION
            ClocktowerTeam.Demon -> CharacterType.DEMON
        }
        val actualAlignment = when (actualRole.team) {
            ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD
            ClocktowerTeam.Minion, ClocktowerTeam.Demon -> com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL
        }
        return TroubleBrewingRegistrationSubject(
            seat = seat,
            actualRole = RoleId(actualRole.enName),
            actualAlignment = actualAlignment,
            actualType = actualType,
            effectiveRole = effectiveRoleForRegistration(queryingRoleEnName, card),
            poisoned = effectivePoisonForRole(queryingRoleEnName) == card.name,
        )
    }
    fun registrationResolution(
        key: String?,
        queryingRoleEnName: String,
        card: PlayerCard?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
        question: RegistrationQuestion = if (detail == ClocktowerRegistrationDetail.AlignmentOnly) {
            RegistrationQuestion.ALIGNMENT
        } else {
            RegistrationQuestion.ROLE
        },
    ): TroubleBrewingRegistrationResolution? {
        if (key == null || card == null || teams.isEmpty()) return null
        val subject = registrationSubject(queryingRoleEnName, card) ?: return null
        val allowedTypes = teams.mapTo(mutableSetOf()) { team ->
            when (team) {
                ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
                ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                ClocktowerTeam.Minion -> CharacterType.MINION
                ClocktowerTeam.Demon -> CharacterType.DEMON
            }
        }
        return TroubleBrewingRegistrationDomain.resolve(
            subject = subject,
            allowedRoles = clocktowerRoleDefinitionsForScript(script).filter { it.type in allowedTypes },
            question = question,
        )
    }
    fun spyCanRegister(queryingRoleEnName: String): Boolean = spyCard
        ?.let { registrationSubject(queryingRoleEnName, it) }
        ?.let(TroubleBrewingRegistrationDomain::specialReason) == RegistrationReason.SPY_ABILITY
    fun legalRegistrationRoles(
        key: String?,
        queryingRoleEnName: String,
        card: PlayerCard?,
        teams: List<ClocktowerTeam>,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    ): List<ClocktowerRole> {
        val legalRoleIds = registrationResolution(key, queryingRoleEnName, card, teams, detail)
            ?.special
            ?.mapTo(mutableSetOf()) { it.registeredRole }
            .orEmpty()
        return completeTroubleBrewingRoles.filter { RoleId(it.enName) in legalRoleIds }
    }
    fun spyRegistersGood(key: String?, queryingRoleEnName: String): Boolean =
        key != null && spyCanRegister(queryingRoleEnName) && registrationState.spyIsGood(key)
    fun registeredRole(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String): ClocktowerRole? {
        if (!spyRegistersGood(key, queryingRoleEnName)) return spyCard?.clocktowerRole
        val allowed = legalRegistrationRoles(key, queryingRoleEnName, spyCard, teams)
        return allowed.firstOrNull { it.enName == registrationState.spyRole(key) }
    }
    fun spyRegistrationWillRecord(key: String?): Boolean =
        registrationState.spyWillRecord(key) && spyCard != null
    fun recordSpyRegistration(
        key: String?,
        teams: List<ClocktowerTeam>,
        queryingRoleEnName: String,
        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,
    ) {
        if (key == null || spyCard == null || !registrationState.markSpyRecorded(key)) return
        val registrationDetail = when {
            !spyCanRegister(queryingRoleEnName) -> text("中毒，按真实邪恶身份登记", "poisoned; registered as actual evil identity")
            !spyRegistersGood(key, queryingRoleEnName) -> text("按真实邪恶身份登记", "registered as actual evil identity")
            detail == ClocktowerRegistrationDetail.AlignmentOnly -> text("登记为善良", "registered as good")
            else -> registeredRole(key, teams, queryingRoleEnName)?.let { selected ->
                text("登记为${selected.nameFor(language)}", "registered as ${selected.nameFor(language)}")
            } ?: text("登记为善良，未指定具体角色", "registered as good, with no specific role selected")
        }
        onRecordEvent(
            ClocktowerEventType.RoleAction,
            text("间谍登记裁定", "Spy registration"),
            "${spyCard.seatLabel(cards)} · $registrationDetail",
            listOf(spyCard.name),
        )
    }
    fun recluseCanRegister(queryingRoleEnName: String): Boolean = recluseCard
        ?.let { registrationSubject(queryingRoleEnName, it) }
        ?.let(TroubleBrewingRegistrationDomain::specialReason) == RegistrationReason.RECLUSE_ABILITY
    fun recluseRegistersEvil(key: String?, queryingRoleEnName: String): Boolean =
        key != null && recluseCanRegister(queryingRoleEnName) && registrationState.recluseIsEvil(key)
    fun recluseRegisteredRole(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String): ClocktowerRole? {
        if (!recluseRegistersEvil(key, queryingRoleEnName)) return recluseCard?.clocktowerRole
        val allowed = legalRegistrationRoles(key, queryingRoleEnName, recluseCard, teams)
        return allowed.firstOrNull { it.enName == registrationState.recluseRole(key) }
    }
    fun recordRecluseRegistration(key: String?, teams: List<ClocktowerTeam>, queryingRoleEnName: String) {
        if (key == null || !recluseRegistersEvil(key, queryingRoleEnName) || recluseCard == null || !registrationState.markRecluseRecorded(key)) return
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
    val nightHostProjection = ClocktowerNightHostProjectionFactory.project(
        cards = cards,
        script = script,
        ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
        gameSeed = gameSeed,
        phase = phase,
        poisonTarget = poisonTarget,
        checkpoint = nightCheckpoint,
        pendingNightNewDemonIdentityName = pendingNightNewDemonIdentityName,
        lastExecutedName = lastExecutedName,
    )
    val mayorCanRedirect = nightHostProjection.mayorCanRedirect
    val mayorTarget = nightHostProjection.mayorTarget
    val mayorRedirectTargetCards = nightHostProjection.mayorRedirectTargetCards
    val resolvedNightDeathName = nightHostProjection.resolvedNightDeathName
    val ravenkeeperTrigger = nightHostProjection.ravenkeeperTrigger
    val demonCard = nightHostProjection.demonCard
    val demonPoisonedForActionExplanation = nightHostProjection.demonPoisonedForActionExplanation
    val demonSuccessorTargetSeats = nightHostProjection.demonSuccessorTargetSeats
    val demonSuccessorTargetCards = nightHostProjection.demonSuccessorTargetCards
    val sageNightDeath = nightHostProjection.sageNightDeath
    val otherNightInteractions = nightHostProjection.otherNightInteractions
    val otherNightCanonicalInteractionIds = nightHostProjection.otherNightCanonicalInteractionIds
    val baseRoleIdsBySeat = nightHostProjection.baseRoleIdsBySeat
    val chambermaidTargetCards = nightHostProjection.chambermaidTargetCards
    val ravenkeeperDeathTriggerAbilityState = nightHostProjection.ravenkeeperDeathTriggerAbilityState
    val sageDeathTriggerAbilityState = nightHostProjection.sageDeathTriggerAbilityState

    fun effectiveNightStateAt(
        interactionId: ClocktowerInteractionId,
        boundary: ClocktowerInteractionBoundary,
    ) = nightHostProjection.effectiveNightStateAt(interactionId, boundary)

    fun effectiveAbilitySubjectForRole(enName: String, actor: PlayerCard?): AbilitySubject? =
        nightHostProjection.effectiveAbilitySubjectForRole(enName, actor)

    effectivePoisonForRole = nightHostProjection::effectivePoisonForRole
    effectiveRoleForRegistration = nightHostProjection::effectiveRoleForRegistration

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
    var slayerRecluseRegistersDemon by remember { mutableStateOf<Boolean?>(null) }
    val firstNightNaturalPairPrecomputeRequest = if (
        script == ClocktowerScript.TroubleBrewing &&
        phase == ClocktowerPhase.FirstNight &&
        firstNightNaturalPairResultProvider != null
    ) {
        firstNightPairDecisionContext
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
    fun recentMisinformationStreak(card: PlayerCard?): Int {
        if (card == null) return 0
        return events.asReversed()
            .filter { event ->
                event.type in setOf(ClocktowerEventType.Information, ClocktowerEventType.UnreliableInformation) &&
                    event.playerNames.firstOrNull() == card.name
            }
            .takeWhile { event ->
                event.title.contains("misleading", ignoreCase = true) || event.title.contains("误导")
            }
            .count()
    }

    fun legalPairInformationOptions(
        ability: ClocktowerPairInformationAbility,
        actor: PlayerCard,
    ): List<ClocktowerDisplayOption> {
        val reliability = when (
            effectiveAbilitySubjectForRole(ability.name, actor)?.let { subject ->
                AbilityFunctioningSemantics.stateFor(subject, ability.name)
            }
        ) {
            AbilityFunctioningState.DRUNK -> ReliabilityState.DRUNK
            AbilityFunctioningState.POISONED -> ReliabilityState.POISONED
            else -> ReliabilityState.RELIABLE
        }
        return ClocktowerNeutralInformationPreparation.legalPairInformationOptions(
            ability = ability,
            actor = actor,
            cards = cards,
            scriptRoles = clocktowerRolesForScript(script),
            phase = phase,
            game = cards.toClocktowerGameState(script, gameSeed, poisonTarget),
            roleDefinitions = clocktowerRoleDefinitionsForScript(script),
            firstNightContext = firstNightPairDecisionContext,
            requireFirstNightContext =
                script == ClocktowerScript.TroubleBrewing && phase == ClocktowerPhase.FirstNight,
            reliability = reliability,
            roleLabel = { role -> role.nameFor(language) },
            text = ::text,
        )
    }
    val informationStepBuilder = ClocktowerInformationStepBuilder(
        cards = cards,
        language = language,
        automaticStorytellerInfo = automaticStorytellerInfo,
        text = ::text,
        roleActor = ::roleActor,
        roleMissingReason = ::roleMissingReason,
        abilityStateFor = { enName, actor ->
            effectiveAbilitySubjectForRole(enName, actor)?.let { subject ->
                AbilityFunctioningSemantics.stateFor(subject, enName)
            }
        },
        actorIsUnreliable = ::actorIsUnreliable,
        recentMisinformationStreak = ::recentMisinformationStreak,
    )

    val washerwomanActor = roleActor("Washerwoman")
    val washerwomanRegistrationKey = washerwomanActor?.let { registrationKey("Washerwoman") }
    val washerwomanTarget = spyCard?.takeIf { spyRegistersGood(washerwomanRegistrationKey, "Washerwoman") }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Townsfolk && it.clocktowerRole?.enName != "Washerwoman" }
    val washerwomanPair = washerwomanTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(washerwomanActor?.name)) }
    val washerwomanRevealedRole = if (washerwomanTarget?.name == spyCard?.name) {
        registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk), "Washerwoman")
    } else washerwomanTarget?.clocktowerRole
    val washerwomanOrderedPair = orderedPair(washerwomanPair?.first, washerwomanPair?.second, "Washerwoman-${washerwomanPair?.first?.name}-${washerwomanPair?.second?.name}")
    val librarianActor = roleActor("Librarian")
    val librarianRegistrationKey = librarianActor?.let { registrationKey("Librarian") }
    val librarianTarget = spyCard?.takeIf { spyRegistersGood(librarianRegistrationKey, "Librarian") }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Outsider }
    val librarianPair = librarianTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(librarianActor?.name)) }
    val librarianRevealedRole = if (librarianTarget?.name == spyCard?.name) {
        registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider), "Librarian")
    } else librarianTarget?.clocktowerRole
    val librarianOrderedPair = orderedPair(librarianPair?.first, librarianPair?.second, "Librarian-${librarianPair?.first?.name}-${librarianPair?.second?.name}")
    val investigatorActor = roleActor("Investigator")
    val investigatorRegistrationKey = investigatorActor?.let { registrationKey("Investigator") }
    val investigatorRecluseRegistrationKey = investigatorActor?.let {
        recluseCard?.let { recluse -> registrationKey("InvestigatorRecluse", recluse.name) }
    }
    val investigatorTarget = recluseCard?.takeIf { recluseRegistersEvil(investigatorRecluseRegistrationKey, "Investigator") }
        ?: spyCard?.takeIf { !spyRegistersGood(investigatorRegistrationKey, "Investigator") }
        ?: cards.firstOrNull { it.clocktowerTeam == ClocktowerTeam.Minion && it.clocktowerRole?.enName != "Spy" }
    val investigatorRevealedRole = if (investigatorTarget?.name == recluseCard?.name) {
        recluseRegisteredRole(investigatorRecluseRegistrationKey, listOf(ClocktowerTeam.Minion), "Investigator")
    } else {
        investigatorTarget?.clocktowerRole
    }
    val investigatorPair = investigatorTarget?.let { storytellerPairHint(it, cards, excludeNames = setOfNotNull(investigatorActor?.name)) }
    val investigatorOrderedPair = orderedPair(investigatorPair?.first, investigatorPair?.second, "Investigator-${investigatorPair?.first?.name}-${investigatorPair?.second?.name}")
    val clockmakerValue = clockmakerNumber()
    val clockmakerNumber = clockmakerValue.toString()
    val empathActor = roleActor("Empath")
    val empathInteractionId = ClocktowerProductionNightStepIdentity.role(RoleId("Empath"))
        .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)
    val empathStateBefore = empathActor?.takeIf { phase == ClocktowerPhase.Night }
        ?.let { effectiveNightStateAt(empathInteractionId, ClocktowerInteractionBoundary.BEFORE) }
    val effectiveEmpathCards = empathStateBefore?.let { state ->
        cards.filter { card ->
            val seat = cards.indexOf(card).plus(1)
            seat > 0 && state.isMechanicallyAlive(seat)
        }
    } ?: cards
    val empathNeighbors = empathActor?.let { livingNeighbors(effectiveEmpathCards, it.name) }.orEmpty()
    val empathAbilityUnreliable = empathActor?.let { actorIsUnreliable("Empath", it) } == true
    val empathRegistrationKey = empathActor?.takeIf { actor -> empathNeighbors.any { it.name == spyCard?.name } }?.let { registrationKey("Empath", it.name) }
    val empathRecluseRegistrationKey = empathActor
        ?.takeIf { empathNeighbors.any { neighbor -> neighbor.name == recluseCard?.name } }
        ?.let { registrationKey("EmpathRecluse", it.name) }
    fun registeredIsEvil(card: PlayerCard, queryingRoleEnName: String, spyKey: String?, recluseKey: String?): Boolean = when {
        card.name == spyCard?.name && spyRegistersGood(spyKey, queryingRoleEnName) -> false
        card.name == recluseCard?.name && recluseRegistersEvil(recluseKey, queryingRoleEnName) -> true
        else -> isClocktowerEvil(card)
    }
    val chefActor = roleActor("Chef")
    val chefAbilityUnreliable = chefActor?.let { actorIsUnreliable("Chef", it) } == true
    val chefRegistrationKey = chefActor?.let { registrationKey("Chef") }
    val chefRecluseRegistrationKey = chefActor?.let {
        recluseCard?.let { recluse -> registrationKey("ChefRecluse", recluse.name) }
    }
    val chefValue = chefEvilPairs(cards) { card -> registeredIsEvil(card, "Chef", chefRegistrationKey, chefRecluseRegistrationKey) }
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
        empathNeighbors.count { neighbor ->
            registeredIsEvil(neighbor, "Empath", empathRegistrationKey, empathRecluseRegistrationKey)
        }
    } ?: 0
    val empathActualIdentityValue = empathNeighbors.count(::isClocktowerEvil)
    val empathSpyActualValue = empathNeighbors.count { neighbor ->
        if (neighbor.name == spyCard?.name) true else registeredIsEvil(neighbor, "Empath", null, empathRecluseRegistrationKey)
    }
    val empathSpyGoodValue = empathNeighbors.count { neighbor ->
        if (neighbor.name == spyCard?.name) false else registeredIsEvil(neighbor, "Empath", null, empathRecluseRegistrationKey)
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
    fun informationDecisionPublicationAllowed(displayStep: ClocktowerNightStepUi): Boolean =
        clocktowerInformationPublicationAllowed(
            confirmation = displayStep.informationDecisionConfirmation,
            expectedSnapshot = displayStep.informationDecisionExpectedSnapshot,
            currentRevision = InformationDecisionRevision(gameStateRevision, playerInputRevision),
        )
    fun recordReliablePrivateInformation(displayStep: ClocktowerNightStepUi) {
        val actor = displayStep.actor ?: return
        val actorSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return
        if (!informationDecisionPublicationAllowed(displayStep)) return
        displayStep.informationDecisionConfirmation?.let { confirmation ->
            onCommitConfirmedInformationDecision(confirmation)
            return
        }
        if (displayStep.displayProposition == null &&
            actorIsUnreliable(displayStep.roleEnName ?: return, actor)) return
        val proposition = displayStep.displayProposition ?: when (displayStep.roleEnName) {
            "Chef" -> InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, actorSeat, cards.indices.map { it + 1 }, chefReferenceValue,
            )
            "Empath" -> InformationProposition.NumericResult(
                NumericMetric.LIVING_EVIL_NEIGHBOURS, actorSeat,
                empathNeighbors.map { cards.indexOf(it) + 1 }, empathReferenceValue,
            )
            "Fortune Teller" -> InformationProposition.BooleanResult(
                BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, actorSeat,
                listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
                    cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                }, fortuneTellerMatched ?: return,
            )
            "Investigator" -> {
                val pair = investigatorPair
                val revealedRole = investigatorRevealedRole
                if (pair != null && revealedRole != null) {
                    val firstSeat = cards.indexOf(pair.first).takeIf { it >= 0 }?.plus(1) ?: return
                    val secondSeat = cards.indexOf(pair.second).takeIf { it >= 0 }?.plus(1) ?: return
                    InformationProposition.AnyOf(listOf(
                        InformationProposition.RoleAt(firstSeat, RoleId(revealedRole.enName)),
                        InformationProposition.RoleAt(secondSeat, RoleId(revealedRole.enName)),
                    ))
                } else {
                    InformationProposition.AllOf(completeTroubleBrewingRoles
                        .filter { it.team == ClocktowerTeam.Minion }
                        .map { InformationProposition.RoleInPlay(RoleId(it.enName), false) })
                }
            }
            "Washerwoman", "Librarian" -> {
                val pair = (if (displayStep.roleEnName == "Washerwoman") washerwomanPair else librarianPair) ?: return
                val role = if (displayStep.roleEnName == "Washerwoman") washerwomanRevealedRole else librarianRevealedRole
                val firstSeat = cards.indexOf(pair.first).takeIf { index -> index >= 0 }?.plus(1) ?: return
                val secondSeat = pair.second.let { cards.indexOf(it).takeIf { index -> index >= 0 }?.plus(1) } ?: return
                InformationProposition.AnyOf(listOf(
                    InformationProposition.RoleAt(firstSeat, RoleId(role?.enName ?: return)),
                    InformationProposition.RoleAt(secondSeat, RoleId(role.enName)),
                ))
            }
            else -> return
        }
        onRecordEpistemicObservation(EpistemicObservationDraft(
            recordId = clocktowerPrivateObservationRecordId(
                gameId = gameId,
                phase = phase,
                round = round,
                roleEnName = requireNotNull(displayStep.roleEnName),
                actorSeat = actorSeat,
                proposition = proposition,
            ),
            phase = phase.toStorytellerPhase(),
            round = round, sequence = nightStepIndex, sourceSeat = actorSeat,
            sourceAbility = RoleId(requireNotNull(displayStep.roleEnName)), visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(actorSeat),
            reliability = if (clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)) {
                ObservationReliability.KNOWN_MALFUNCTIONING
            } else {
                ObservationReliability.RECEIVED_AS_FUNCTIONING
            },
            proposition = proposition,
        ))
    }
    val undertakerTarget = lastExecutedName?.let { name -> cards.firstOrNull { it.name == name } }
    val undertakerRegistrationKey = undertakerTarget?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Undertaker", it.name) }
    val undertakerRecluseRegistrationKey = undertakerTarget?.takeIf { it.name == recluseCard?.name }?.let { registrationKey("UndertakerRecluse", it.name) }
    val ravenkeeperTargetCard = ravenkeeperTarget?.let { name -> cards.firstOrNull { it.name == name } }
    val ravenkeeperRegistrationKey = ravenkeeperTargetCard?.takeIf { it.name == spyCard?.name }?.let { registrationKey("Ravenkeeper", it.name) }
    val ravenkeeperRecluseRegistrationKey = ravenkeeperTargetCard?.takeIf { it.name == recluseCard?.name }?.let { registrationKey("RavenkeeperRecluse", it.name) }
    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val sagePair = demonCard?.let { storytellerPairHint(it, cards) }
    val spyDelta: String? = run {
        if (spyCard == null || phase == ClocktowerPhase.FirstNight) return@run null
        val prevRound = round - 1
        val excludedTitles = setOf(
            text("间谍登记裁定", "Spy registration"),
            text("今日主人", "Master"),
            text("红鲱鱼", "Red herring"),
            text("魔典", "Grimoire"),
            text("爪牙信息", "Minion info"),
            text("恶魔信息", "Demon info"),
            text("杀手行动", "Slayer claim"),
            text("杀手命中", "Slayer hit"),
        )
        val deltaEvents = events.filter { e ->
            e.type in setOf(
                ClocktowerEventType.RoleAction,
                ClocktowerEventType.Information,
                ClocktowerEventType.UnreliableInformation,
            ) &&
                e.title !in excludedTitles &&
                ((e.phase == ClocktowerPhase.Night && e.round == prevRound) ||
                    (e.phase == ClocktowerPhase.FirstNight && e.round == prevRound) ||
                    (e.phase == ClocktowerPhase.Day && e.round == prevRound))
        }
        if (deltaEvents.isEmpty()) null
        else deltaEvents.joinToString("\n") { "• ${it.title}：${it.detail}" }
    }
    val minionCards = cards.filter { it.clocktowerRole?.team == ClocktowerTeam.Minion }
    fun seatNumberText(card: PlayerCard): String = ((cards.indexOf(card) + 1).takeIf { it > 0 } ?: 0).toString()
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
    val legalDemonBluffs = legalDemonBluffRoles(
        scriptRoles = clocktowerRolesForScript(script),
        inPlayRoleNames = cards.mapNotNull { it.clocktowerRole?.enName }.toSet(),
    )
    val legalDemonBluffRoleNames = legalDemonBluffs.mapTo(linkedSetOf()) { it.enName }
    var manualDemonBluffDraft by remember(
        gameId,
        recommendedDemonBluffRoleNames,
        legalDemonBluffRoleNames,
    ) {
        mutableStateOf(
            recommendedDemonBluffRoleNames
                .filter { it in legalDemonBluffRoleNames }
                .distinct()
                .take(3),
        )
    }
    val manualDemonBluffsRequired = shouldGiveFirstNightEvilInfo && demonCard != null
    val manualDemonBluffsReady = !manualDemonBluffsRequired ||
        manualDemonBluffSelectionReady(
            selectedRoleNames = manualDemonBluffDraft,
            legalRoles = legalDemonBluffs,
        )
    val demonBluffRoleNames = recommendedDemonBluffRoleNames.takeIf { it.isNotEmpty() }
    val demonBluffPresentation = resolveDemonBluffPresentation(
        recommendedRoleNames = demonBluffRoleNames,
        legalRoles = legalDemonBluffs,
    )
    val firstNightEvilInformationSteps = clocktowerFirstNightEvilInformationSteps(
        minionActor = minionCards.firstOrNull(),
        demonActor = demonCard,
        minionSeatLabels = minionCards.map { it.seatLabel(cards) },
        demonSeatLabel = demonCard?.seatLabel(cards),
        shouldGiveInformation = shouldGiveFirstNightEvilInfo,
        demonBluffPresentation = demonBluffPresentation,
        language = language,
    )
    val firstNightActualRoleIds = buildSet {
        cards.forEach { card ->
            card.clocktowerRole?.enName?.let { add(RoleId(it)) }
        }
    }
    val firstNightWakingRoleIds = buildSet {
        cards.forEach { card ->
            card.clocktowerRole?.enName?.let { add(RoleId(it)) }
            if (card.clocktowerRole?.enName == "Drunk") {
                card.clocktowerShownRole?.enName?.let { add(RoleId(it)) }
            }
        }
    }
    fun resultFirstNumericRegistrationOptions(
        title: String,
        actor: PlayerCard,
        roleEnName: String,
        metric: NumericMetric,
        subjectSeats: List<Int>,
        footer: String,
        spyKey: String?,
        recluseKey: String?,
        valueFor: (ClocktowerAlignmentRegistrationWitness) -> Int,
    ): List<ClocktowerDisplayOption> {
        val spySelectable = spyKey != null && spyCanRegister(roleEnName)
        val recluseSelectable = recluseKey != null && recluseCanRegister(roleEnName)
        if (!spySelectable && !recluseSelectable) return emptyList()
        val sourceSeat = cards.indexOf(actor) + 1
        if (sourceSeat <= 0) return emptyList()
        val witnesses = clocktowerAlignmentRegistrationWitnesses(
            currentSpyRegistersGood = spyRegistersGood(spyKey, roleEnName),
            spySelectable = spySelectable,
            currentRecluseRegistersEvil = recluseRegistersEvil(recluseKey, roleEnName),
            recluseSelectable = recluseSelectable,
        )
        return ClocktowerRegistrationResultPresentation.numericOptions(
            title = title,
            sourceSeat = sourceSeat,
            metric = metric,
            subjectSeats = subjectSeats,
            footer = footer,
            witnesses = witnesses,
            valueFor = valueFor,
        )
    }

    fun resultFirstFortuneTellerRegistrationOptions(actor: PlayerCard): List<ClocktowerDisplayOption> {
        val key = fortuneTellerRecluseRegistrationKey ?: return emptyList()
        if (!recluseCanRegister("Fortune Teller")) return emptyList()
        val sourceSeat = cards.indexOf(actor) + 1
        val subjectSeats = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond).mapNotNull { name ->
            cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
        }
        if (sourceSeat <= 0 || subjectSeats.size != 2 || subjectSeats.distinct().size != 2) return emptyList()
        val current = recluseRegistersEvil(key, "Fortune Teller")
        val demonRole = legalRegistrationRoles(
            key,
            "Fortune Teller",
            recluseCard,
            listOf(ClocktowerTeam.Demon),
        ).firstOrNull()
        return ClocktowerRegistrationResultPresentation.fortuneTellerOptions(
            sourceSeat = sourceSeat,
            subjectSeats = subjectSeats,
            secondary = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                .joinToString("   ") { seatNumberText(it) }
                .takeIf { it.isNotBlank() },
            currentRecluseRegistersEvil = current,
            demonRegistrationRole = demonRole,
            text = ::text,
            matches = ::fortuneTellerMatches,
        )
    }

    fun resultFirstRoleRevealRegistrationOptions(
        title: String,
        roleEnName: String,
        target: PlayerCard?,
        footer: String,
        spyKey: String?,
        spyTeams: List<ClocktowerTeam>,
        recluseKey: String?,
        recluseTeams: List<ClocktowerTeam>,
    ): List<ClocktowerDisplayOption> {
        val resolvedTarget = target ?: return emptyList()
        val targetSeat = cards.indexOf(resolvedTarget) + 1
        if (targetSeat <= 0) return emptyList()
        val ruling = when {
            resolvedTarget.name == spyCard?.name && spyKey != null && spyCanRegister(roleEnName) -> {
                val selected = spyRegistersGood(spyKey, roleEnName)
                ClocktowerRegistrationResultPresentation.RoleRevealRuling(
                    specialRegistration = ClocktowerRegistrationResultPresentation.SpecialRegistration.Spy,
                    specialSelected = selected,
                    actualRole = resolvedTarget.clocktowerRole,
                    selectedRole = if (selected) registeredRole(spyKey, spyTeams, roleEnName) else null,
                    legalSpecialRoles = legalRegistrationRoles(spyKey, roleEnName, spyCard, spyTeams),
                )
            }
            resolvedTarget.name == recluseCard?.name && recluseKey != null && recluseCanRegister(roleEnName) -> {
                val selected = recluseRegistersEvil(recluseKey, roleEnName)
                ClocktowerRegistrationResultPresentation.RoleRevealRuling(
                    specialRegistration = ClocktowerRegistrationResultPresentation.SpecialRegistration.Recluse,
                    specialSelected = selected,
                    actualRole = resolvedTarget.clocktowerRole,
                    selectedRole = if (selected) recluseRegisteredRole(recluseKey, recluseTeams, roleEnName) else null,
                    legalSpecialRoles = legalRegistrationRoles(recluseKey, roleEnName, recluseCard, recluseTeams),
                )
            }
            else -> return emptyList()
        }
        return ClocktowerRegistrationResultPresentation.roleRevealOptions(
            title = title,
            targetSeat = targetSeat,
            footer = footer,
            ruling = ruling,
            roleLabel = { role -> role.nameFor(language) },
        )
    }

    val chambermaidStepContent = ClocktowerChambermaidStepContent(
        explanation = text("侍女选择两名玩家，得知其中有几人今晚因自己的能力醒来。", "The Chambermaid chooses two players and learns how many woke tonight because of their own ability."),
        displayFooter = text("查询这两名玩家", "Checking these two players"),
        hostInstruction = text("轻拍侍女，示意睁眼。让她依次指两名玩家，不能选自己；点查询后只展示数字。", "Tap the Chambermaid to wake them. Have them point to two players other than themself, then show only the number."),
    )
    val chambermaidMaterializer = clocktowerChambermaidStepMaterializer(
        builder = informationStepBuilder,
        content = chambermaidStepContent,
        result = chambermaidResult,
        presentation = chambermaidPresentation,
        displayProposition = chambermaidResult?.toIntOrNull()?.let { value ->
            roleActor("Chambermaid")?.let { actor ->
                chambermaidPresentation.proposition(cards.indexOf(actor) + 1, value)
            }
        },
        displayOptions = { actor ->
            chambermaidResult?.toIntOrNull()?.let { trueValue ->
                ClocktowerNeutralInformationPreparation.numericOptions(
                    title = text("侍女信息", "Chambermaid information"),
                    trueValue = trueValue,
                    maxValue = 2,
                    footer = chambermaidStepContent.displayFooter,
                    secondary = chambermaidPresentation.displaySecondary,
                    propositionForValue = { value ->
                        chambermaidPresentation.proposition(cards.indexOf(actor) + 1, value)
                    },
                )
            }.orEmpty()
        },
    )

    val empathSubjectSeats = empathNeighbors.map { cards.indexOf(it) + 1 }
    val empathDisplayOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { actor ->
        ClocktowerNeutralInformationPreparation.numericOptions(
            title = text("共情者信息", "Empath information"),
            trueValue = empathReferenceValue,
            maxValue = 2,
            footer = text("邪恶存活邻居数量", "Evil living neighbors"),
            propositionForValue = if (phase == ClocktowerPhase.FirstNight) {
                { value ->
                    InformationProposition.NumericResult(
                        NumericMetric.LIVING_EVIL_NEIGHBOURS,
                        cards.indexOf(actor) + 1,
                        empathSubjectSeats,
                        value,
                    )
                }
            } else null,
        )
    }
    val empathLegalSelectionOptions: (PlayerCard) -> List<ClocktowerDisplayOption> = { actor ->
        if (empathAbilityUnreliable) emptyList() else resultFirstNumericRegistrationOptions(
            title = text("共情者信息", "Empath information"),
            actor = actor,
            roleEnName = "Empath",
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            subjectSeats = empathSubjectSeats,
            footer = text("邪恶存活邻居数量", "Evil living neighbors"),
            spyKey = empathRegistrationKey,
            recluseKey = empathRecluseRegistrationKey,
        ) { witness ->
            empathNeighbors.count { card ->
                when {
                    card.name == spyCard?.name && witness.spyRegistersGood != null -> !witness.spyRegistersGood
                    card.name == recluseCard?.name && witness.recluseRegistersEvil != null -> witness.recluseRegistersEvil
                    else -> isClocktowerEvil(card)
                }
            }
        }
    }
    val empathStepMaterializer = clocktowerEmpathStepMaterializer(
        builder = informationStepBuilder,
        content = ClocktowerEmpathStepContent(
            result = empathNumber,
            registrationHint = empathRegistrationHint,
            previousShownNumber = empathActor?.let { actor ->
                previousClocktowerUnreliableNumber(events, text("共情者信息", "Empath information"), actor.name)
                    ?.takeIf { it in 0..2 }
            },
            spyRegistrationKey = empathRegistrationKey,
            recluseRegistrationKey = empathRecluseRegistrationKey,
        ),
        text = ::text,
        displayOptions = empathDisplayOptions,
        legalSelectionOptions = empathLegalSelectionOptions,
    )

    val fortuneTellerSelectedNames = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
    val fortuneTellerSubjectSeats = fortuneTellerSelectedNames.mapNotNull { name ->
        cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
    }
    val fortuneTellerSecondary = fortuneTellerSelectedNames
        .mapNotNull { name -> cards.firstOrNull { it.name == name } }
        .joinToString("   ") { seatNumberText(it) }
        .takeIf { it.isNotBlank() }
    val fortuneTellerStepMaterializer = clocktowerFortuneTellerStepMaterializer(
        builder = informationStepBuilder,
        content = ClocktowerFortuneTellerStepContent(
            result = fortuneTellerResult,
            matched = fortuneTellerMatched,
            selectedNames = fortuneTellerSelectedNames,
            displaySecondary = fortuneTellerSecondary,
            proposition = fortuneTellerMatched?.let { matched ->
                roleActor("Fortune Teller")?.let { actor ->
                    InformationProposition.BooleanResult(
                        BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                        cards.indexOf(actor) + 1,
                        fortuneTellerSubjectSeats,
                        matched,
                    )
                }
            },
            recluseRegistrationKey = fortuneTellerRecluseRegistrationKey,
        ),
        cards = cards,
        text = ::text,
        legalSelectionOptions = { actor ->
            if (actorIsUnreliable("Fortune Teller", actor)) emptyList()
            else resultFirstFortuneTellerRegistrationOptions(actor)
        },
    )

    val nightSteps = if (phase == ClocktowerPhase.FirstNight) {
        val firstNightInteractions =
            ClocktowerProductionFirstNightFlow.interactions(
                ruleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script),
                playerCount = cards.size,
                inPlayRoleIds = firstNightWakingRoleIds,
                actualRoleIds = firstNightActualRoleIds,
            )
        val firstNightMaterializers = ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            entries = listOf(
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.minionInfo(),
            build = { firstNightEvilInformationSteps.minion },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.demonInfo(),
            build = { firstNightEvilInformationSteps.demon },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner")),
            build = {
                informationStepBuilder.build(
                                roleName = "投毒者",
                                enName = "Poisoner",
                                tellPlayer = poisonTarget?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                                explanation = text("投毒者选择一名玩家，使其能力暂时失效。", "The Poisoner chooses a player whose ability stops working temporarily."),
                                action = ClocktowerNightAction.Poison,
                                displayKind = ClocktowerDisplayKind.None,
                                hostInstruction = text("轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。", "Tap the Poisoner to wake them. Have them point to one player and record that player as tonight's poisoned target."),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring(),
            build = {
                ClocktowerNightStepUi(
                                title = text("占卜师红鲱鱼", "Fortune Teller red herring"),
                                actor = null,
                                isRealAction = actualClocktowerRoleCards(cards, "Fortune Teller").isNotEmpty(),
                                reason = if (actualClocktowerRoleCards(cards, "Fortune Teller").isEmpty()) text("本局没有占卜师，此步骤只用于首夜配置。", "No Fortune Teller is in play; this is only a first-night setup step.") else "",
                                storytellerAction = text("不要公开说明这个选择。请选择一名善良玩家作为红鲱鱼；可以选择占卜师本人。", "Keep this choice private. Choose a good player as the red herring; the Fortune Teller may be chosen."),
                                tellPlayer = redHerring?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                                explanation = text("选择一名善良玩家成为红鲱鱼。占卜师查询他时，结果为“有”，他会被标记为恶魔。", "Choose a good player as the red herring. The Fortune Teller detects that player as a Demon."),
                                action = ClocktowerNightAction.RedHerring,
                                roleEnName = "Fortune Teller",
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Clockmaker")),
            build = {
                informationStepBuilder.build(
                                roleName = "钟表匠",
                                enName = "Clockmaker",
                                tellPlayer = clockmakerNumber,
                                explanation = text("这个数字表示恶魔到最近爪牙相隔几步。", "This number is the distance from the Demon to the nearest Minion."),
                                displayFooter = text("恶魔到最近爪牙的距离", "Distance from Demon to nearest Minion"),
                                hostInstruction = text("轻拍钟表匠，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。", "Tap the Clockmaker to wake them. Show the number only to that player, then take the phone back and signal them to close their eyes."),
                                displayOptions = { _ -> ClocktowerNeutralInformationPreparation.numericOptions(text("钟表匠信息", "Clockmaker information"), clockmakerValue, cards.size / 2, text("恶魔到最近爪牙的距离", "Distance from Demon to nearest Minion")) },
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Washerwoman")),
            build = {
                informationStepBuilder.build(
                                roleName = "洗衣妇",
                                enName = "Washerwoman",
                                tellPlayer = washerwomanTarget?.let { text("${if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk), "Washerwoman")?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${washerwomanOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${washerwomanOrderedPair?.second?.seatLabel(cards).orEmpty()}", "${if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk), "Washerwoman")?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} is one of these two players: ${washerwomanOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${washerwomanOrderedPair?.second?.seatLabel(cards).orEmpty()}") },
                                explanation = text("洗衣妇会得知某个镇民在两名玩家之一中。", "The Washerwoman learns that a particular Townsfolk is one of two players."),
                                displayPrimary = washerwomanTarget?.let { if (it.name == spyCard?.name) registeredRole(washerwomanRegistrationKey, listOf(ClocktowerTeam.Townsfolk), "Washerwoman")?.nameFor(language) else it.clocktowerRole?.nameFor(language) },
                                displaySecondary = seatNumbersText(washerwomanOrderedPair),
                                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                                hostInstruction = text("轻拍洗衣妇，示意睁眼。点击“全屏展示给玩家”，只给她看；看完后收回手机，示意闭眼。", "Tap the Washerwoman to wake them. Show the full-screen information only to that player, then take the phone back and signal them to close their eyes."),
                                displayOptions = { actor ->
                                    legalPairInformationOptions(ClocktowerPairInformationAbility.Washerwoman, actor)
                                },
                                legalSelectionOptions = { actor ->
                                    legalPairInformationOptions(ClocktowerPairInformationAbility.Washerwoman, actor)
                                },
                                spyRegistrationKey = washerwomanRegistrationKey,
                                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Librarian")),
            build = {
                informationStepBuilder.build(
                                roleName = "图书管理员",
                                enName = "Librarian",
                                tellPlayer = librarianTarget?.let { text("${if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider), "Librarian")?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} 在这两人之中：${librarianOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${librarianOrderedPair?.second?.seatLabel(cards).orEmpty()}", "${if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider), "Librarian")?.nameFor(language).orEmpty() else it.clocktowerRole?.nameFor(language).orEmpty()} is one of these two players: ${librarianOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${librarianOrderedPair?.second?.seatLabel(cards).orEmpty()}") } ?: text("本局没有外来者。", "There are no Outsiders in play."),
                                explanation = text("图书管理员会得知某个外来者在两名玩家之一中，或得知没有外来者。", "The Librarian learns that an Outsider is one of two players, or that no Outsiders are in play."),
                                displayPrimary = librarianTarget?.let { if (it.name == spyCard?.name) registeredRole(librarianRegistrationKey, listOf(ClocktowerTeam.Outsider), "Librarian")?.nameFor(language) else it.clocktowerRole?.nameFor(language) } ?: text("没有外来者", "No Outsiders"),
                                displaySecondary = seatNumbersText(librarianOrderedPair),
                                displayFooter = if (librarianTarget == null) "" else text("在下面两位玩家之中", "One of these two players"),
                                hostInstruction = text("轻拍图书管理员，示意睁眼。把结果只给他看；如果显示“没有外来者”，也只告诉他本人。", "Tap the Librarian to wake them. Show the result only to that player, including a No Outsiders result."),
                                displayOptions = { actor ->
                                    legalPairInformationOptions(ClocktowerPairInformationAbility.Librarian, actor)
                                },
                                legalSelectionOptions = { actor ->
                                    legalPairInformationOptions(ClocktowerPairInformationAbility.Librarian, actor)
                                },
                                spyRegistrationKey = librarianRegistrationKey,
                                spyRegistrationTeams = listOf(ClocktowerTeam.Outsider),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Investigator")),
            build = {
                informationStepBuilder.build(
                                roleName = "调查员",
                                enName = "Investigator",
                                tellPlayer = investigatorTarget?.let { text("${investigatorRevealedRole?.nameFor(language).orEmpty()} 在这两人之中：${investigatorOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${investigatorOrderedPair?.second?.seatLabel(cards).orEmpty()}", "${investigatorRevealedRole?.nameFor(language).orEmpty()} is one of these two players: ${investigatorOrderedPair?.first?.seatLabel(cards).orEmpty()} / ${investigatorOrderedPair?.second?.seatLabel(cards).orEmpty()}") } ?: text("本局没有爪牙。", "There are no Minions in play."),
                                explanation = text("调查员会得知某个爪牙在两名玩家之一中，或得知没有爪牙。", "The Investigator learns that a Minion is one of two players, or that no Minions are in play."),
                                displayPrimary = investigatorRevealedRole?.nameFor(language) ?: text("没有爪牙", "No Minions"),
                                displaySecondary = seatNumbersText(investigatorOrderedPair),
                                displayFooter = if (investigatorTarget == null) "" else text("在下面两位玩家之中", "One of these two players"),
                                hostInstruction = text("轻拍调查员，示意睁眼。把结果只给他看；不要让其他玩家看到被点名的两人。", "Tap the Investigator to wake them. Show the result only to that player; do not let anyone else see the two named players."),
                                displayOptions = { actor ->
                                    legalPairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                                },
                                legalSelectionOptions = { actor ->
                                    legalPairInformationOptions(ClocktowerPairInformationAbility.Investigator, actor)
                                },
                                spyRegistrationKey = investigatorRegistrationKey,
                                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                recluseRegistrationKey = investigatorRecluseRegistrationKey,
                                recluseRegistrationTeams = listOf(ClocktowerTeam.Minion),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Chef")),
            build = {
                informationStepBuilder.build(
                                roleName = "厨师",
                                enName = "Chef",
                                tellPlayer = chefNumber,
                                explanation = listOfNotNull(text("这个数字表示有几对邪恶玩家相邻而坐。", "This number is the number of adjacent evil pairs."), chefRegistrationHint).joinToString("\n"),
                                hostInstruction = text("轻拍厨师，示意睁眼。把数字只给他看；确认后收回手机，示意闭眼。", "Tap the Chef to wake them. Show the number only to that player, then take the phone back and signal them to close their eyes."),
                                displayProposition = chefActor?.let { actor ->
                                    InformationProposition.NumericResult(
                                        NumericMetric.ADJACENT_EVIL_PAIRS,
                                        cards.indexOf(actor) + 1,
                                        cards.indices.map { it + 1 },
                                        chefReferenceValue,
                                    )
                                },
                                numericMinimumValue = 0,
                                numericMaximumValue = chefMaximumValue,
                                displayOptions = { actor -> ClocktowerNeutralInformationPreparation.numericOptions(text("厨师信息", "Chef information"), chefReferenceValue, chefMaximumValue, text("邪恶玩家相邻对数", "Adjacent evil pairs"), propositionForValue = { value -> InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, cards.indexOf(actor) + 1, cards.indices.map { it + 1 }, value) }) },
                                legalSelectionOptions = { actor ->
                                    if (chefAbilityUnreliable) {
                                        emptyList()
                                    } else {
                                        resultFirstNumericRegistrationOptions(
                                            title = text("厨师信息", "Chef information"),
                                            actor = actor,
                                            roleEnName = "Chef",
                                            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
                                            subjectSeats = cards.indices.map { it + 1 },
                                            footer = text("邪恶玩家相邻对数", "Adjacent evil pairs"),
                                            spyKey = chefRegistrationKey,
                                            recluseKey = chefRecluseRegistrationKey,
                                        ) { witness ->
                                            chefEvilPairs(cards) { card ->
                                                when {
                                                    card.name == spyCard?.name && witness.spyRegistersGood != null -> !witness.spyRegistersGood
                                                    card.name == recluseCard?.name && witness.recluseRegistersEvil != null -> witness.recluseRegistersEvil
                                                    else -> isClocktowerEvil(card)
                                                }
                                            }
                                        }
                                    }
                                },
                                spyRegistrationKey = chefRegistrationKey,
                                spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
                                spyRegistrationHint = chefRegistrationHint,
                                recluseRegistrationKey = chefRecluseRegistrationKey,
                            )
            },
        ),
        empathStepMaterializer,
        chambermaidMaterializer,
        fortuneTellerStepMaterializer,
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Butler")),
            build = {
                informationStepBuilder.build(
                                roleName = "管家",
                                enName = "Butler",
                                tellPlayer = butlerMaster?.let { text("今天的主人：${playerSeatLabel(cards, it)}", "Today's master: ${playerSeatLabel(cards, it)}") },
                                explanation = text("管家每天选择一名主人，白天只能在主人投票时投票。", "The Butler chooses a master each day and may vote only when that master votes."),
                                action = ClocktowerNightAction.ButlerMaster,
                                displayKind = ClocktowerDisplayKind.None,
                                hostInstruction = text("轻拍管家，示意睁眼。让他指一名玩家作为今天的主人；记在心里，白天投票时提醒自己核对。", "Tap the Butler to wake them. Have them point to today's master and keep the choice available for checking during voting."),
                            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Spy")),
            build = {
                informationStepBuilder.build(
                                roleName = "间谍",
                                enName = "Spy",
                                tellPlayer = if (poisonTarget == spyCard?.name) null else {
                                    val grimoire = cards.joinToString("\n") { "${it.seatLabel(cards)}${text("：", ": ")}${it.hostRoleLabel(context, GameKind.Clocktower)}" }
                                    listOfNotNull(spyDelta, grimoire).joinToString("\n\n")
                                },
                                explanation = if (poisonTarget == spyCard?.name) text("间谍已中毒：仍照常唤醒，但不要展示真实魔典，也不能改变登记身份。", "The Spy is poisoned: wake them normally, but do not show the real grimoire or alter registration.") else text("存活间谍每晚可以查看所有玩家的真实身份。", "A living Spy may view every player's true identity each night."),
                                displayKind = ClocktowerDisplayKind.Grimoire,
                                displayTitle = text("魔典", "Grimoire"),
                                displayFooter = text("这些是所有玩家的真实身份。只给间谍短暂查看。", "These are every player's true identities. Show this only briefly to the Spy."),
                                displayProposition = if (poisonTarget == spyCard?.name) null else InformationProposition.GrimoireState(
                                    cards.mapIndexed { index, card -> GrimoireSeatView(index + 1, RoleId(requireNotNull(card.clocktowerRole).enName), card.eliminatedRound == null) },
                                ),
                                hostInstruction = if (poisonTarget == spyCard?.name) text("照常轻拍间谍示意睁眼，但不要展示真实魔典；停顿后示意闭眼。", "Wake the Spy normally, but do not show the real grimoire. Pause, then signal them to close their eyes.") else text("轻拍间谍，示意睁眼。把说书人总览给他短暂查看；收回手机后示意闭眼。", "Tap the Spy to wake them. Briefly show the Storyteller overview, then take the phone back and signal them to close their eyes."),
                            )
            },
        )
            ),
        )
        firstNightMaterializers.materialize(firstNightInteractions)
    } else {
    val otherNightMaterializers = ClocktowerNightStepMaterializerRegistry(
        phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
        entries = listOf(
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Poisoner")),
            build = {
            informationStepBuilder.build(
                roleName = "投毒者",
                enName = "Poisoner",
                tellPlayer = poisonTarget?.let { text("已选择：${playerSeatLabel(cards, it)}", "Selected: ${playerSeatLabel(cards, it)}") },
                explanation = text("投毒者选择一名玩家，使其能力暂时失效。", "The Poisoner chooses a player whose ability stops working temporarily."),
                action = ClocktowerNightAction.Poison,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍投毒者，示意睁眼。让他指一名玩家，在下面记录为今晚中毒目标。", "Tap the Poisoner to wake them. Have them point to one player and record that player as tonight's poisoned target."),
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Butler")),
            build = {
            informationStepBuilder.build(
                roleName = "管家",
                enName = "Butler",
                tellPlayer = butlerMaster?.let { text("今天的主人：${playerSeatLabel(cards, it)}", "Today's master: ${playerSeatLabel(cards, it)}") },
                explanation = text("管家每天选择一名主人。", "The Butler chooses a master each day."),
                action = ClocktowerNightAction.ButlerMaster,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍管家，示意睁眼。让他指今天的主人；白天投票时用这个记录提醒自己。", "Tap the Butler to wake them and have them point to today's master. Keep the choice available for checking during voting."),
            )
            },
        ),
        empathStepMaterializer,
        chambermaidMaterializer,
        fortuneTellerStepMaterializer,
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Undertaker")),
            build = {
            val executedName = requireNotNull(lastExecutedName)
                    informationStepBuilder.build(
                        roleName = "送葬者",
                        enName = "Undertaker",
                        tellPlayer = text("${playerSeatLabel(cards, executedName)} 的角色是 ${when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Undertaker")?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Undertaker")?.nameFor(language).orEmpty()
                            else -> undertakerTarget?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}", "${playerSeatLabel(cards, executedName)} was ${when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Undertaker")?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Undertaker")?.nameFor(language).orEmpty()
                            else -> undertakerTarget?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}"),
                        explanation = text("送葬者每晚得知今天被处决玩家的真实身份。", "Each night, the Undertaker learns the character of the player executed today."),
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("送葬者信息", "Undertaker information"),
                        displayPrimary = when (undertakerTarget?.name) {
                            spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Undertaker")?.nameFor(language)
                            recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Undertaker")?.nameFor(language)
                            else -> undertakerTarget?.clocktowerRole?.nameFor(language)
                        },
                        displayProposition = undertakerTarget?.let { target ->
                            val shownRole = when (target.name) {
                                spyCard?.name -> registeredRole(undertakerRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Undertaker")
                                recluseCard?.name -> recluseRegisteredRole(undertakerRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Undertaker")
                                else -> target.clocktowerRole
                            } ?: return@let null
                            InformationProposition.RoleAt(cards.indexOf(target) + 1, RoleId(shownRole.enName))
                        },
                        displayFooter = text("今天被处决：${playerSeatLabel(cards, executedName)}", "Executed today: ${playerSeatLabel(cards, executedName)}"),
                        hostInstruction = text("轻拍送葬者，示意睁眼。把今天被处决玩家的真实身份只给他看；看完后收回手机，示意闭眼。", "Tap the Undertaker to wake them. Show the executed player's identity only to that player, then take the phone back and signal them to close their eyes."),
                        displayOptions = {
                            ClocktowerNeutralInformationPreparation.roleRevealOptions(
                                title = text("送葬者信息", "Undertaker information"),
                                truthfulRole = undertakerTarget?.clocktowerRole,
                                scriptRoles = clocktowerRolesForScript(script),
                                roleLabel = { role -> role.nameFor(language) },
                                footer = text("今天被处决：${playerSeatLabel(cards, executedName)}", "Executed today: ${playerSeatLabel(cards, executedName)}"),
                            )
                        },
                        legalSelectionOptions = { actor ->
                            if (actorIsUnreliable("Undertaker", actor)) {
                                emptyList()
                            } else {
                                resultFirstRoleRevealRegistrationOptions(
                                    title = text("送葬者信息", "Undertaker information"),
                                    roleEnName = "Undertaker",
                                    target = undertakerTarget,
                                    footer = text("今天被处决：${playerSeatLabel(cards, lastExecutedName)}", "Executed today: ${playerSeatLabel(cards, lastExecutedName)}"),
                                    spyKey = undertakerRegistrationKey,
                                    spyTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                    recluseKey = undertakerRecluseRegistrationKey,
                                    recluseTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                                )
                            }
                        },
                        spyRegistrationKey = undertakerRegistrationKey,
                        spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        recluseRegistrationKey = undertakerRecluseRegistrationKey,
                        recluseRegistrationTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                    )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Monk")),
            build = {
            informationStepBuilder.build(
                roleName = "僧侣",
                enName = "Monk",
                tellPlayer = monkProtectedTarget?.let { text("已选择保护：${playerSeatLabel(cards, it)}。如果恶魔今晚选择该玩家，他不会死亡。", "Protected: ${playerSeatLabel(cards, it)}. If the Demon chooses this player tonight, they will not die.") },
                explanation = text("僧侣每晚选择除自己以外的一名玩家。若恶魔今晚攻击被保护的玩家，天亮时宣布无人死亡；不要透露是僧侣保护导致。", "Each night, the Monk protects one other player from the Demon. If that player is attacked, announce no death at dawn without revealing the protection."),
                action = ClocktowerNightAction.MonkProtect,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍僧侣，示意睁眼。让他指一名除自己以外的玩家，在下面记录为今晚保护目标。", "Tap the Monk to wake them. Have them point to another player and record that player as tonight's protected target."),
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.newDemonIdentity(),
            build = {
            val newDemon = requireNotNull(
                publicAliveCards.firstOrNull {
                    it.name == pendingNightNewDemonIdentityName &&
                        it.clocktowerRole?.enName == "Imp"
                },
            ) {
                "Pending next-night new-Demon identity must reference the current living Imp."
            }
                    ClocktowerNightStepUi(
                        title = text("新恶魔身份", "New Demon identity"),
                        actor = newDemon,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text(
                            "轻拍 ${newDemon.seatLabel(cards)}，示意睁眼。把手机交给他，确认他现在是小恶魔；看完后收回手机并示意闭眼。",
                            "Tap ${newDemon.seatLabel(cards)} to wake them. Hand them the phone to confirm they are now the Imp, then take it back and signal them to close their eyes.",
                        ),
                        tellPlayer = text("你现在是小恶魔。", "You are now the Imp."),
                        explanation = text(
                            "猩红女巫在白天因恶魔死亡而继任。必须在新的小恶魔本夜行动前私下确认身份。",
                            "The Scarlet Woman became the Demon during the day. Confirm the new identity privately before the Imp acts tonight.",
                        ),
                        action = ClocktowerNightAction.NewDemonIdentity,
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("新身份", "New role"),
                        displayPrimary = text("小恶魔", "Imp"),
                        displayFooter = "",
                        roleEnName = "Imp",
                    )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Imp")),
            build = {
            ClocktowerNightStepUi(
                title = text("恶魔行动", "Demon action"),
                actor = demonCard,
                isRealAction = demonCard != null,
                reason = if (demonCard == null) text("当前没有存活恶魔。", "There is no living Demon.") else "",
                storytellerAction = demonCard?.let {
                    text("轻拍 ${it.seatLabel(cards)}，示意睁眼。让他指今晚要杀死的玩家，在下面记录；记录后示意闭眼。", "Tap ${it.seatLabel(cards)} to wake them. Have them point to tonight's kill target, record it, then signal them to close their eyes.")
                } ?: text("不要唤醒任何玩家，停顿 2-3 秒后继续。", "Do not wake anyone. Pause for 2–3 seconds, then continue."),
                tellPlayer = if (demonCard != null) {
                    if (demonPoisonedForActionExplanation) {
                        text("恶魔已中毒，今晚杀人会失效。", "The Demon is poisoned, so tonight's kill will fail.")
                    } else {
                        pendingNightDeath?.let { text("已记录：今晚恶魔选择杀死 ${playerSeatLabel(cards, it)}。现在不要宣布死亡，等天亮统一宣布。", "Recorded: the Demon chose ${playerSeatLabel(cards, it)}. Do not announce the death until dawn.") }
                    }
                } else {
                    null
                },
                explanation = if (demonPoisonedForActionExplanation) text("可以记录恶魔选择，但天亮不会因此死亡。", "Record the Demon's choice, but it will not cause a death at dawn.") else text("恶魔选择的死亡目标会在天亮时统一公布。", "The Demon's chosen target is announced at dawn."),
                action = ClocktowerNightAction.DemonKill,
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.demonSuccessor(),
            build = {
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
                    )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.mayorRedirect(),
            build = {
            val targetedMayor = requireNotNull(mayorTarget)
                    ClocktowerNightStepUi(
                        title = text("市长死亡裁定", "Mayor death ruling"),
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text("市长被恶魔击杀。选择让市长死亡，或将死亡转移给另一名玩家。", "The Demon attacked the Mayor. Let the Mayor die or redirect the death to another player."),
                        tellPlayer = mayorRedirectTarget?.let { target ->
                            if (target == targetedMayor.name) {
                                text("市长死亡", "Mayor dies")
                            } else {
                                text("死亡转移给 ${playerSeatLabel(cards, target)}", "Death redirected to ${playerSeatLabel(cards, target)}")
                            }
                        },
                        explanation = text("市长保持存活时，可以让另一名玩家代替死亡。选择死亡或受保护的玩家，可能导致今夜无人死亡。", "To keep the Mayor alive, another player may die instead. Choosing a dead or protected player can result in no death tonight."),
                        action = ClocktowerNightAction.MayorRedirect,
                        displayKind = ClocktowerDisplayKind.None,
                        roleEnName = "Mayor",
                    )
            },
        ),
        clocktowerSageStepMaterializer(
            builder = informationStepBuilder,
            cards = cards,
            triggerActor = sageNightDeath,
            demon = demonCard,
            directPair = sagePair,
            abilityState = sageDeathTriggerAbilityState,
            content = ClocktowerSageStepContent(
                explanation = text("贤者被恶魔杀死时，得知恶魔是两名玩家之一。", "When killed by the Demon, the Sage learns that the Demon is one of two players."),
                displayTitle = text("贤者信息", "Sage information"),
                displayPrimary = text("恶魔", "Demon"),
                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。", "If the Demon killed the Sage tonight, wake the Sage and show only them two players, one of whom is the Demon."),
                highPressureSuffix = text(" ⚠ 高压", " ⚠ high pressure"),
            ),
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper")),
            build = {
            val trigger = requireNotNull(ravenkeeperTrigger)
                    informationStepBuilder.build(
                        roleName = "守鸦人",
                        enName = "Ravenkeeper",
                        actorOverride = trigger,
                        abilityStateOverride = ravenkeeperDeathTriggerAbilityState,
                        tellPlayer = ravenkeeperTarget?.let { text("${playerSeatLabel(cards, it)} 的角色是 ${when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Ravenkeeper")?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Ravenkeeper")?.nameFor(language).orEmpty()
                            else -> ravenkeeperTargetCard?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}", "${playerSeatLabel(cards, it)} is ${when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Ravenkeeper")?.nameFor(language).orEmpty()
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Ravenkeeper")?.nameFor(language).orEmpty()
                            else -> ravenkeeperTargetCard?.hostRoleLabel(context, GameKind.Clocktower).orEmpty()
                        }}") },
                        explanation = text("守鸦人只有在夜晚死亡时才会当晚醒来，选择一名玩家并得知其真实身份。", "The Ravenkeeper wakes only when they die at night, then chooses a player and learns that player's character."),
                        action = ClocktowerNightAction.Ravenkeeper,
                        displayKind = ClocktowerDisplayKind.RoleReveal,
                        displayTitle = text("守鸦人信息", "Ravenkeeper information"),
                        displayPrimary = when (ravenkeeperTargetCard?.name) {
                            spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Ravenkeeper")?.nameFor(language)
                            recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Ravenkeeper")?.nameFor(language)
                            else -> ravenkeeperTargetCard?.clocktowerRole?.nameFor(language)
                        },
                        displayProposition = ravenkeeperTargetCard?.let { target ->
                            val shownRole = when (target.name) {
                                spyCard?.name -> registeredRole(ravenkeeperRegistrationKey, listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider), "Ravenkeeper")
                                recluseCard?.name -> recluseRegisteredRole(ravenkeeperRecluseRegistrationKey, listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon), "Ravenkeeper")
                                else -> target.clocktowerRole
                            } ?: return@let null
                            InformationProposition.RoleAt(cards.indexOf(target) + 1, RoleId(shownRole.enName))
                        },
                        displayFooter = ravenkeeperTarget?.let { text("查询目标：${playerSeatLabel(cards, it)}", "Checked player: ${playerSeatLabel(cards, it)}") },
                        hostInstruction = text("轻拍 ${trigger.seatLabel(cards)}，示意睁眼。让他指一名玩家，在下面记录后把该玩家角色只给他看。", "Tap ${trigger.seatLabel(cards)} to wake them. Have them point to a player, record the target, and show that character only to the Ravenkeeper."),
                        displayOptions = {
                            ClocktowerNeutralInformationPreparation.roleRevealOptions(
                                title = text("守鸦人信息", "Ravenkeeper information"),
                                truthfulRole = ravenkeeperTargetCard?.clocktowerRole,
                                scriptRoles = clocktowerRolesForScript(script),
                                roleLabel = { role -> role.nameFor(language) },
                                footer = ravenkeeperTarget?.let { text("查询目标：${playerSeatLabel(cards, it)}", "Checked player: ${playerSeatLabel(cards, it)}") }.orEmpty(),
                            )
                        },
                        legalSelectionOptions = { actor ->
                            if (actorIsUnreliable("Ravenkeeper", actor)) {
                                emptyList()
                            } else {
                                resultFirstRoleRevealRegistrationOptions(
                                    title = text("守鸦人信息", "Ravenkeeper information"),
                                    roleEnName = "Ravenkeeper",
                                    target = ravenkeeperTargetCard,
                                    footer = ravenkeeperTarget?.let { text("查询目标：${playerSeatLabel(cards, it)}", "Checked player: ${playerSeatLabel(cards, it)}") }.orEmpty(),
                                    spyKey = ravenkeeperRegistrationKey,
                                    spyTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                                    recluseKey = ravenkeeperRecluseRegistrationKey,
                                    recluseTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                                )
                            }
                        },
                        spyRegistrationKey = ravenkeeperRegistrationKey,
                        spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        recluseRegistrationKey = ravenkeeperRecluseRegistrationKey,
                        recluseRegistrationTeams = listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon),
                    )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Spy")),
            build = {
                informationStepBuilder.build(
                    roleName = "间谍",
                    enName = "Spy",
                    tellPlayer = if (effectivePoisonForRole("Spy") == spyCard?.name) null else {
                        val grimoire = cards.joinToString("\n") { "${it.seatLabel(cards)}${text("：", ": ")}${it.hostRoleLabel(context, GameKind.Clocktower)}" }
                        listOfNotNull(spyDelta, grimoire).joinToString("\n\n")
                    },
                    explanation = if (effectivePoisonForRole("Spy") == spyCard?.name) text("间谍已中毒：仍照常唤醒，但不要展示真实魔典，也不能改变登记身份。", "The Spy is poisoned: wake them normally, but do not show the real grimoire or alter registration.") else text("存活间谍每晚查看真实魔典。", "A living Spy views the true grimoire each night."),
                    displayKind = ClocktowerDisplayKind.Grimoire,
                    displayTitle = text("魔典", "Grimoire"),
                    displayFooter = text("这些是所有玩家的真实身份。只给间谍短暂查看。", "These are every player's true identities. Show this only briefly to the Spy."),
                    displayProposition = if (effectivePoisonForRole("Spy") == spyCard?.name) null else InformationProposition.GrimoireState(
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
            compact = !automaticStorytellerInfo,
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
                slayerRecluseRegistersDemon = null
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
        val virginSpyRegistersGood = spyRegistersGood(virginRegistrationKey, "Virgin")
        val virginExecutes = virginAbilityWorks &&
            (nominatorCard?.clocktowerTeam == ClocktowerTeam.Townsfolk || virginSpyRegistersGood)
        val specialNotice = when {
            virginExecutes -> text(
                "${playerSeatLabel(cards, nomineeName)} 首次被登记为镇民的玩家提名：不进行投票，提名者将立即被处决。",
                "${playerSeatLabel(cards, nomineeName)} was first nominated by a player registering as Townsfolk: skip voting and execute the nominator.",
            )
            virginFirstNomination -> text(
                "这是圣女第一次被提名，但能力不会处决提名者；记录能力已用过后继续投票。",
                "This is the Virgin's first nomination, but the ability does not execute the nominator. Mark it spent and continue.",
            )
            else -> null
        }
        ClocktowerPendingNominationTableScreen(
            round = round,
            compact = !automaticStorytellerInfo,
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
                        false, // Day ruling is causal, not a fake preceding localized event.
                    )
                }
                if (chosenNominator != null && chosenNominee != null) {
                    onConfirmedNomination(chosenNominator, chosenNominee)
                }
                if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {
                    val explicitSpyRuling = registrationState.explicitChoice(
                        ClocktowerRegistrationSubject.SPY, virginRegistrationKey,
                    )?.takeIf { virginAbilityWorks && spyCanRegister("Virgin") }
                    onVirginNomination(
                        chosenNominator, chosenNominee, virginExecutes,
                        explicitSpyRuling?.usesSpecialRegistration,
                    )
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
                if (virginRegistrationKey != null && spyCard != null) {
                    ClocktowerSpyTownsfolkTypeDecisionControls(
                        registersAsTownsfolk = spyRegistersGood(virginRegistrationKey, "Virgin"),
                        hasExplicitChoice = registrationState.spyHasExplicitChoice(virginRegistrationKey),
                        enabled = spyCanRegister("Virgin"),
                        language = language,
                        onRegistersAsTownsfolkChange = { chosen ->
                            registrationState.chooseSpy(virginRegistrationKey, chosen)
                        },
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
                onConfirmedVote(requireNotNull(nominatorName), requireNotNull(nomineeName), voteTransaction.voteRecord)
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
            compact = !automaticStorytellerInfo,
            onHostTools = onHostTools,
            onSeatClick = { seatId ->
                val selectedName = slayerTableState.playerNameForSeat(seatId)
                if (slayerClaimantName == null) {
                    slayerClaimantName = selectedName
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = null
                } else {
                    slayerTargetName = if (slayerTargetName == selectedName) null else selectedName
                    slayerRecluseRegistersDemon = null
                }
            },
            onResetClaimant = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = null
            },
            onResolve = {
                val claimantName = slayerClaimantName
                val targetName = slayerTargetName
                if (claimantName != null && targetName != null) {
                    val targetIsHealthyRecluse =
                        slayerTargetCard?.clocktowerRole?.enName == "Recluse" &&
                            poisonTarget != targetName
                    val recluseRegistersDemon =
                        if (targetIsHealthyRecluse) slayerRecluseRegistersDemon else null
                    onSlayerShot(claimantName, targetName, recluseRegistersDemon)
                    slayerClaimantName = null
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = null
                    dayMode = ClocktowerDayMode.Overview
                }
            },
            onBack = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = null
                dayMode = ClocktowerDayMode.Overview
            },
            specialContent = {
                if (slayerTargetCard?.clocktowerRole?.enName == "Recluse") {
                    ClocktowerRecluseRegistrationDecisionControls(
                        legalRoles = legalRegistrationRoles(
                            registrationKey("SlayerRecluse", requireNotNull(slayerTargetCard).name),
                            "Slayer",
                            slayerTargetCard,
                            listOf(ClocktowerTeam.Demon),
                        )
                            .map { it.enName to it.nameFor(language) },
                        registersEvil = slayerRecluseRegistersDemon == true,
                        registeredRoleEnName = if (slayerRecluseRegistersDemon == true) "Imp" else null,
                        enabled = recluseCanRegister("Slayer"),
                        hasExplicitChoice = slayerRecluseRegistersDemon != null,
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
        val artistAnswerOptions = if (artistClaimant != null && currentArtistTruthfulAnswer != null) {
            if (artistReliable) listOf(currentArtistTruthfulAnswer) else listOf(true, false)
        } else {
            emptyList()
        }
        val artistManualRequired = !artistReliable && artistAnswerOptions.size > 1
        val automaticArtistAnswer = currentArtistTruthfulAnswer
            ?.takeIf { automaticStorytellerInfo && artistReliable }
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
                    artistAnswerOptions.forEach { answer ->
                        val label = if (answer) text("是", "Yes") else text("否", "No")
                        if (automaticStorytellerInfo && !artistManualRequired) {
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
            onConfirm = onConfirmKlutzChoice,
        )
        return
    }

    if (phase == ClocktowerPhase.FirstNight && !nightStarted) {
        val oneShotScope = if (oneShotEnabled && globalAiAssisted &&
            script == ClocktowerScript.TroubleBrewing
        ) clocktowerFirstNightOneShotScope(
            gameId, gameStateRevision, playerInputRevision,
            script, cards, nightSteps,
        ) else null
        // Load the ONE entire legal opening while the Storyteller is at
        // private preflight, rather than waiting for an isolated role step.
        LaunchedEffect(oneShotScope, oneShotAccepted, oneShotBusy, oneShotError) {
            if (oneShotScope != null && !oneShotAccepted && !oneShotBusy &&
                oneShotError == null && oneShotPlan?.validFor(oneShotScope) != true
            ) onRequestOneShot(oneShotScope)
        }
        val legalOneShotPlan = oneShotScope?.let { scope ->
            oneShotPlan?.takeIf { it.validFor(scope) }
        }
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
            startEnabled = manualDemonBluffsReady,
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ClocktowerNightReadyCard()
                if (oneShotScope != null && !oneShotAccepted) {
                    if (oneShotBusy) {
                        Text(text("AI 正在联合配置完整首夜…", "AI planning all first-night information…"))
                    }
                    oneShotError?.let { failure ->
                        Text(
                            text("AI 推荐未完成，可继续手动主持。", "AI plan unavailable; Manual hosting remains available."),
                            color = MaterialTheme.colorScheme.error,
                        )
                        OutlinedButton(onClick = { onRequestOneShot(oneShotScope) }) {
                            Text(text("重新生成完整首夜配置", "Retry one full opening plan"))
                        }
                    }
                    if (legalOneShotPlan != null) {
                        Text(
                            text("唯一推荐首夜配置", "One recommended first-night configuration"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        legalOneShotPlan.demonBluffRoleIds?.let { roles ->
                            Text(text("恶魔掩饰身份：", "Demon bluffs: ") + roles.joinToString(" · "))
                        }
                        oneShotScope.availableDecisions.forEach { decision ->
                            val id = legalOneShotPlan.candidateByDecisionId[decision.decisionId]
                            Text(
                                "${decision.family} · ${decision.sourceSeat ?: "—"}："+
                                    (id?.let(decision.candidateDescriptions::get) ?: id.orEmpty()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (oneShotScope.deferredDecisionIds.isNotEmpty()) {
                            Text(
                                text("等待玩家行动：", "Waiting for player actions: ") +
                                    oneShotScope.deferredDecisionIds.joinToString(" · "),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Button(
                            onClick = { onAdoptOneShot(legalOneShotPlan, oneShotScope) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text("一键采纳推荐配置", "Adopt recommended configuration"))
                        }
                    }
                } else if (oneShotAccepted) {
                    Text(
                        text("已采纳整套推荐。各信息仍在对应夜间步骤由 Host 核验后展示。",
                            "Full proposal adopted. Host validates each disclosure when the night reaches it."),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (manualDemonBluffsRequired && !oneShotAccepted) {
                    ClocktowerDemonBluffManualPicker(
                        legalRoles = legalDemonBluffs,
                        selectedRoleNames = manualDemonBluffDraft,
                        language = language,
                        onSelectionChange = { nextSelection ->
                            manualDemonBluffDraft = nextSelection
                            if (
                                manualDemonBluffSelectionReady(
                                    selectedRoleNames = nextSelection,
                                    legalRoles = legalDemonBluffs,
                                )
                            ) {
                                onCommitDemonBluffs(nextSelection)
                            }
                        },
                    )
                }
            }
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
        val globalAiPending = if (globalAiAssisted && phase == ClocktowerPhase.FirstNight) {
            pendingGlobalFirstNightPairDecision(
                currentStep, phase, round, cards, firstNightPairDecisionContext,
            )
        } else null
        val globalAiScalarPending = if (globalAiAssisted &&
            phase == ClocktowerPhase.FirstNight && globalAiPending == null
        ) pendingGlobalFirstNightScalarDecision(
            currentStep, phase, round, currentStepIndex, cards, gameId,
            InformationDecisionRevision(gameStateRevision, playerInputRevision),
            listOfNotNull(fortuneTellerFirst, fortuneTellerSecond),
            recommendationCoordinator,
        ) else null
        val globalAiTargetPending = globalNightPendingDecision?.takeIf {
            globalAiAssisted && phase == ClocktowerPhase.Night &&
                currentStep.action == ClocktowerNightAction.MayorRedirect &&
                currentStep.isRealAction
        }
        val globalAiDecisionIdentity = globalAiPending?.requestIdentity
            ?: globalAiScalarPending?.requestIdentity
            ?: globalAiTargetPending?.requestIdentity
        val globalAiLegalIds = globalAiPending?.legalCandidates?.map { it.candidateId }
            ?: globalAiScalarPending?.legalCandidates?.map { it.candidateId }
            ?: globalAiTargetPending?.pending?.legalCandidates?.map { it.candidateId }
        val globalAiCurrentKey = globalAiDecisionIdentity?.let { identity ->
            val legalIds = requireNotNull(globalAiLegalIds)
            listOf(
                gameId, round, currentStepIndex, identity.requestId,
                gameStateRevision, playerInputRevision, nightCheckpoint.nextTimelineGlobalSequence,
                legalIds.size, legalIds.hashCode(),
            ).joinToString(":")
        }
        // Revalidate any adopted proposed option against the CURRENT Host
        // legal choices after each real player action (especially Poisoner).
        val preplannedSeat = currentStep.actor?.let { actor ->
            cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
        }
        val preplannedChoice = preplannedSeat?.let { seat ->
            adoptedOneShotChoices["first-night:${currentStep.roleEnName}:seat-$seat"]
        }
        val approvedOpeningOption = preplannedChoice?.let { id ->
            val options = currentStep.manualInformationCandidates.ifEmpty {
                currentStep.legacyInformationCandidates.takeIf { legacy ->
                    currentStep.roleEnName in setOf("Chef", "Empath") &&
                        currentStep.displayPrimary != null &&
                        legacy.size == 1 &&
                        legacy.single().displayPrimary == currentStep.displayPrimary
                }.orEmpty()
            }
            // A genuine player action may have changed the legal domain.
            // Re-match against CURRENT Host-produced choices, never just the
            // stale initial candidate ID from the accepted bundle.
            options.singleOrNull { clocktowerOneShotCandidateId(it) == id }
        }
        LaunchedEffect(globalAiCurrentKey, globalAiAssisted, approvedOpeningOption) {
            if (globalAiAssisted && globalAiCurrentKey != null &&
                globalAiAdviceKey != globalAiCurrentKey && approvedOpeningOption == null
            ) {
                onRequestGlobalAiAdvice(currentStep, currentStepIndex)
            }
        }
        val currentSurfacePlan = clocktowerNightSurfacePlan(currentStep, phase)
        val currentSpyRegistrationResolution = currentStep.roleEnName?.let { roleEnName ->
            registrationResolution(
                currentStep.spyRegistrationKey,
                roleEnName,
                spyCard,
                currentStep.spyRegistrationTeams,
                currentStep.spyRegistrationDetail,
            )
        }
        val currentSpyLegalSpecialRoleEnNames = currentSpyRegistrationResolution
            ?.special
            ?.map { it.registeredRole.value }
            .orEmpty()
        val currentRecluseRegistrationResolution = currentStep.roleEnName?.let { roleEnName ->
            // Numeric/Boolean alignment questions do not name a specific Evil role; an empty
            // role filter means ALIGNMENT_ONLY, not that the Recluse is irrelevant.
            val filteredTeams = currentStep.recluseRegistrationTeams.ifEmpty {
                listOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)
            }
            registrationResolution(
                currentStep.recluseRegistrationKey,
                roleEnName,
                recluseCard,
                filteredTeams,
                detail = if (currentStep.recluseRegistrationTeams.isEmpty()) {
                    ClocktowerRegistrationDetail.AlignmentOnly
                } else ClocktowerRegistrationDetail.Role,
            )
        }
        val currentRecluseLegalSpecialRoleEnNames = currentRecluseRegistrationResolution
            ?.special
            ?.map { it.registeredRole.value }
            .orEmpty()
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
                onConfirmMayorRedirectTarget()
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (globalAiAssisted && globalAiDecisionIdentity != null &&
                    globalAiCurrentKey != null
                ) {
                    OutlinedButton(
                        onClick = { onRequestGlobalAiAdvice(currentStep, currentStepIndex) },
                        enabled = !globalAiAdviceBusy || globalAiAdviceKey != globalAiCurrentKey,
                    ) {
                        Text(text("AI 全局局势与当前合法裁量", "Global AI advice"))
                    }
                    if (globalAiAdviceKey == globalAiCurrentKey) {
                        if (globalAiAdviceBusy) Text(
                            text("正在重新评估已确认的游戏事件…", "Analysing committed game history…"),
                        )
                        globalAiAdviceError?.let { error -> Text(text = error) }
                        globalAiAdviceText?.let { advice -> Text(text = advice) }
                    }
                }
                ClocktowerNightStepCardLocalized(
                recommendationCoordinator = recommendationCoordinator,
                automaticStorytellerInfo = automaticStorytellerInfo,
                phase = phase,
                gameId = gameId,
                round = round,
                sequence = currentStepIndex,
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                informationDecisionKey = "$recommendationKey:${phase.name}:$round:${currentStep.title}:${currentStep.actor?.name}",
                cards = cards,
                ghostVoteAuthority = ghostVoteAuthority,
                poisonedPlayerName = currentStep.roleEnName?.let(effectivePoisonForRole),
                aliveCards = publicAliveCards,
                chambermaidTargetCards = chambermaidTargetCards,
                mayorRedirectTargetCards = mayorRedirectTargetCards,
                demonSuccessorTargetCards = demonSuccessorTargetCards,
                step = approvedOpeningOption?.let { chosen ->
                    currentStep.copy(
                        legacyInformationCandidates = listOf(chosen),
                        automaticInformationCandidates = listOf(chosen),
                        recommendedDisplayOptions = listOf(chosen),
                    )
                } ?: currentStep,
                surfacePlan = currentSurfacePlan,
                spyCard = spyCard,
                spyRegistrationResolution = currentSpyRegistrationResolution,
                onSpyRegistrationGoodChange = { good ->
                    currentStep.spyRegistrationKey?.let { key ->
                        registrationState.chooseSpy(key, good)
                        if (!good && redHerring == spyCard?.name && currentStep.action == ClocktowerNightAction.RedHerring) {
                            onSelectRedHerring(null)
                        }
                    }
                },
                onSpyRegistrationRoleChange = { roleName ->
                    currentStep.spyRegistrationKey?.let { registrationState.chooseSpyRole(it, roleName) }
                },
                recluseCard = recluseCard,
                recluseRegistrationResolution = currentRecluseRegistrationResolution,
                onRecluseRegistrationEvilChange = { evil ->
                    currentStep.recluseRegistrationKey?.let { key ->
                        registrationState.chooseRecluse(key, evil)
                    }
                },
                onRecluseRegistrationRoleChange = { roleName ->
                    currentStep.recluseRegistrationKey?.let { registrationState.chooseRecluseRole(it, roleName) }
                },
                selectedName = selectedNightName,
                fortuneTellerFirst = fortuneTellerFirst,
                fortuneTellerSecond = fortuneTellerSecond,
                redHerring = redHerring,
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
                    // Preview never writes an observed result or a registration ruling.
                    registrationState.manualChoicesMatchResult(
                        currentStep.spyRegistrationKey,
                        currentStep.recluseRegistrationKey,
                        option,
                    )
                },
                onShowPlayerDisplay = { displayStep ->
                    val spyChoice = registrationState.explicitChoice(
                        ClocktowerRegistrationSubject.SPY, currentStep.spyRegistrationKey,
                    )
                    val recluseChoice = registrationState.explicitChoice(
                        ClocktowerRegistrationSubject.RECLUSE, currentStep.recluseRegistrationKey,
                    )
                    val plan = if (clocktowerDisplayedInformationIsUnreliable(
                            displayStep, ::actorIsUnreliable,
                        )
                    ) {
                        // Drunk/poisoned results are arbitrary: a matching truthful witness is
                        // not a historical explanation. Do not turn it into a canonical ruling.
                        if (spyChoice != null || recluseChoice != null) {
                            ClocktowerResultRegistrationPlanV1.ConflictingManualChoice
                        } else {
                            ClocktowerResultRegistrationPlanV1.NoVerifiedWitness
                        }
                    } else clocktowerPlanConfirmedResultRegistrations(
                        shownProposition = displayStep.informationDecisionConfirmation?.draft?.proposition
                            ?: displayStep.displayProposition,
                        shownKind = displayStep.displayKind,
                        shownPrimary = displayStep.displayPrimary,
                        legalCandidates = currentStep.manualInformationCandidates,
                        spy = spyChoice,
                        spySeat = spyCard?.let { cards.indexOf(it).takeIf { index -> index >= 0 }?.plus(1) }
                            ?.takeIf { currentStep.spyRegistrationKey != null &&
                                currentSpyRegistrationResolution?.canUseSpecialAbility == true },
                        spyQuestion = if (currentStep.spyRegistrationDetail == ClocktowerRegistrationDetail.AlignmentOnly) {
                            RegistrationQuestion.ALIGNMENT
                        } else RegistrationQuestion.ROLE,
                        recluse = recluseChoice,
                        recluseSeat = recluseCard?.let { cards.indexOf(it).takeIf { index -> index >= 0 }?.plus(1) }
                            ?.takeIf { currentStep.recluseRegistrationKey != null &&
                                currentRecluseRegistrationResolution?.canUseSpecialAbility == true },
                        recluseQuestion = if (currentStep.recluseRegistrationTeams.isEmpty()) {
                            RegistrationQuestion.ALIGNMENT
                        } else RegistrationQuestion.ROLE,
                    )
                    if (plan is ClocktowerResultRegistrationPlanV1.ConflictingManualChoice) {
                        Toast.makeText(
                            context,
                            text(
                                "当前显示结果与已选择的登记裁定冲突，或缺乏可验证的登记证据。",
                                "The result conflicts with the selected registration or lacks verified witnesses.",
                            ),
                            Toast.LENGTH_LONG,
                        ).show()
                    } else {
                        val ready = plan as? ClocktowerResultRegistrationPlanV1.Ready
                        val actor = displayStep.actor
                        val shown = displayStep.informationDecisionConfirmation?.draft?.proposition
                            ?: displayStep.displayProposition
                        val publication = if (ready != null && actor != null &&
                            shown != null && displayStep.roleEnName != null &&
                            displayStep.interactionId != null
                        ) {
                            val actorSeat = cards.indexOfFirst { it.name == actor.name }
                                .takeIf { it >= 0 }?.plus(1)
                            actorSeat?.let { seat ->
                                val recordId = displayStep.informationDecisionConfirmation?.draft?.recordId
                                    ?: clocktowerPrivateObservationRecordId(
                                        gameId = gameId,
                                        phase = phase,
                                        round = round,
                                        roleEnName = requireNotNull(displayStep.roleEnName),
                                        actorSeat = seat,
                                        proposition = shown,
                                    )
                                ClocktowerConfirmedRegistrationPublicationV1(
                                    interactionId = "${phase.name}:$round:${displayStep.interactionId.value}",
                                    observationRecordId = recordId,
                                    sourceSeat = seat,
                                    shownProposition = shown,
                                    choices = ready.choices,
                                    legalResultWitnesses = ready.legalResultWitnesses,
                                )
                            }
                        } else null
                        val commitVerifiedRegistration: () -> Unit = {
                            publication?.let(onCommitConfirmedRegistrationResult)
                        }
                        val handoff = performClocktowerPlayerRevealHandoff(
                            authorize = {
                                informationDecisionPublicationAllowed(displayStep) &&
                                    (ready == null || (publication != null &&
                                        onPreflightConfirmedRegistrationResult(publication)))
                            },
                            publishFirstNight = { publishFirstNightInformation(displayStep) },
                            recordPrivateInformation = {
                                recordReliablePrivateInformation(displayStep)
                                commitVerifiedRegistration()
                            },
                            recordHistory = {
                                val unreliable = clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)
                                val payload = clocktowerInformationHistoryPayload(
                                    displayStep = displayStep,
                                    orderedPlayerNames = cards.map { it.name },
                                    unreliable = unreliable,
                                    text = ::text,
                                )
                                onRecordEvent(
                                    payload.type,
                                    payload.title,
                                    payload.detail,
                                    payload.playerNames,
                                )
                            },
                            openReveal = { playerDisplayStep = displayStep },
                        )
                        // Re-opening an unchanged confirmed result can correct an explicitly
                        // adjudicated witness without publishing a duplicate player observation.
                        // The Host still requires the exact already-stored most recent observation.
                        if (handoff.openReveal && !handoff.recordPublication) {
                            commitVerifiedRegistration()
                        }
                    }
                },
                canGoPrevious = currentStepIndex > 0,
                onPrevious = onMovePreviousNightStep,
                onHostTools = onHostTools,
                onNext = advanceNightStep,
                )
            }
        }
        return
    }
}
