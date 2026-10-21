package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.widget.Toast
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.ClocktowerResidualDisplayMatcherV2
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClocktowerJudgeScreen(
    allowRuleDeterminedAutoUi: Boolean,
    cards: List<PlayerCard>,
    // Confirmed mechanical facts come from the one Session, never rebuilt from UI cards.
    sessionState: ClocktowerSessionState,
    globalAiAssisted: Boolean = false,
    /** A verified joint plan is advisory; only confirmed current steps affect Session. */
    globalAiAutomatic: Boolean = false,
    jointOpeningAvailable: Boolean = false,
    jointOpeningSemantics: Map<Int, com.codex.campboardgamehost.clocktower.session.HostOpeningSemanticValueV2> = emptyMap(),
    globalNightPendingDecision: PendingMayorRedirectDecision? = null,
    globalAiAdviceKey: String? = null,
    globalAiAdviceText: String? = null,
    globalAiAdviceBusy: Boolean = false,
    globalAiAdviceError: String? = null,
    globalResidualRecommendedChoices: Map<String, com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult> = emptyMap(),
    onRequestGlobalAiAdvice: (List<ClocktowerNightStepUi>, Int) -> Unit = { _, _ -> },
    demonAttackDraftTarget: String?,
    poisonDraftTarget: String?,
    monkProtectedDraftTarget: String?,
    mayorRedirectDraftTarget: String?,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    ravenkeeperTarget: String?,
    redHerring: String?,
    openingRecommendedRedHerring: String? = null,
    recommendedDemonBluffRoleNames: List<String>,
    butlerMaster: String?,
    onCommitConfirmedPrivateInformationPublication: (EpistemicObservationDraft, StorytellerDecisionRevision) -> Unit,
    onHostTools: () -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onConfirmDemonAttack: (String, String?) -> Unit,
    onSelectPoisonTarget: (String?) -> Unit,
    onConfirmPoisonTarget: (String) -> Unit,
    onSelectFortuneTellerFirst: (String?) -> Unit,
    onSelectFortuneTellerSecond: (String?) -> Unit,
    onSelectChambermaidFirst: (String?) -> Unit,
    onSelectChambermaidSecond: (String?) -> Unit,
    onSelectRavenkeeperTarget: (String?) -> Unit,
    onSelectRedHerring: (String?) -> Unit,
    onConfirmRedHerring: (String) -> Unit,
    onCommitDemonBluffs: (List<String>) -> Unit,
    onSelectButlerMaster: (String?) -> Unit,
    onConfirmButlerMaster: (String, String, String) -> Unit,
    onSelectMonkProtectedTarget: (String?) -> Unit,
    onConfirmMonkProtectedTarget: (String) -> Unit,
    onSelectMayorRedirectTarget: (String?) -> Unit,
    onConfirmMayorRedirectTarget: () -> Unit,
    onConfirmNewDemon: () -> Unit,
    onConfirmSpyWake: (String) -> Unit,
    onConfirmEvilInformation: (String, StorytellerDecisionRevision) -> Unit,
    onConfirmKlutzChoice: (String) -> Unit,
    onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onConfirmedNomination: (String, String, Boolean?, Int) -> Boolean,
    onConfirmedVote: (String, String, ClocktowerConfirmedVoteRecord, Int) -> Unit,
    onAdvanceFromFirstNight: () -> Unit,
    onConfirmDay: (Int) -> Unit,
    onCompleteNight: () -> Unit,
) {
    if (sessionState.outcome != null) return
    val sessionGameState = sessionState.gameState
    val context = LocalContext.current
    val language = context.resources.configuration.locales[0].language
    val gameId = sessionState.gameId
    val round = sessionState.round
    val phase = when (sessionState.phase) {
        StorytellerPhase.FIRST_NIGHT -> ClocktowerPhase.FirstNight
        StorytellerPhase.DAY -> ClocktowerPhase.Day
        StorytellerPhase.NIGHT -> ClocktowerPhase.Night
    }
    val script = ClocktowerScript.entries.single { it.toRecommendationScriptId() == sessionGameState.script }
    val gameStateRevision = sessionState.gameStateRevision
    val playerInputRevision = sessionState.playerInputRevision
    val poisonTarget = sessionState.confirmedPoisonTargetName(round)
    val pendingNewDemonName = sessionState.takeIf { phase == ClocktowerPhase.Night }
        ?.pendingImpNotification()?.targetSeat?.let(sessionGameState::playerAt)?.name
    val ghostVoteAuthority = ClocktowerGhostVoteAuthority.from(sessionState.actionTimeline)
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val publicAliveCards = cards.filter { it.eliminatedRound == null }

    var playerDisplayStep by remember { mutableStateOf<ClocktowerNightStepUi?>(null) }
    // UI-only D2 draft must outlive the fullscreen early return below.
    // Match the interaction identity before reuse: no persisted night cursor or Session event.
    var lastInformationPreview by remember(gameId, phase, round) {
        mutableStateOf<ClocktowerNightStepUi?>(null)
    }
    val legalDemonBluffs = clocktowerRolesForScript(script).distinctBy { it.enName }
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

    playerDisplayStep?.let { displayStep ->
        ClocktowerPlayerDisplayCardLocalized(
            step = displayStep,
            cards = cards,
            // D2: closing a preview returns to this role without any Session fact.
            onDismiss = { playerDisplayStep = null },
        )
        return
    }

    pendingNewDemonName?.let { newDemonName ->
        val newDemon = cards.firstOrNull { it.name == newDemonName }
        val newDemonStep = ClocktowerNightStepUi(
            interactionId = ClocktowerProductionNightStepIdentity.newDemonIdentity().interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT),
            action = ClocktowerNightAction.NewDemonIdentity,
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
            confirmationEnabled = lastInformationPreview?.interactionId == newDemonStep.interactionId,
            onHostTools = onHostTools,
            onShowPlayerDisplay = { lastInformationPreview = newDemonStep; playerDisplayStep = newDemonStep },
            onConfirm = { onConfirmNewDemon(); lastInformationPreview = null },
        )
        return
    }

    if (phase == ClocktowerPhase.Day) {
        ClocktowerDayHostScreen(sessionState, cards, script, allowRuleDeterminedAutoUi, onHostTools, onConfirmDay, onConfirmKlutzChoice,
            onConfirmArtistQuestion, onSlayerShot, onConfirmedNomination, onConfirmedVote)
        return
    }

    val spySeatLabel = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }?.seatLabel(cards)
    val nightRuleset = BuiltInClocktowerRulesetCatalog.fromContext(context).ruleset(script)
    val nightInteractions = sessionState.nightInteractions(nightRuleset)
    val nextNightInteraction = sessionState.nextNightInteraction(nightRuleset)
    val nightHostProjection = ClocktowerNightHostProjection(cards, sessionState)
    val mayorRedirectTargetCards = nightHostProjection.mayorRedirectTargetCards
    val demonCard = nightHostProjection.demonCard
    val chambermaidTargetCards = nightHostProjection.chambermaidTargetCards

    val effectiveAbilitySubjectForRole = nightHostProjection::effectiveAbilitySubjectForRole

    val effectivePoisonForRole = nightHostProjection::effectivePoisonForRole
    val chambermaidSelection = revalidateTwoPlayerSelection(
        first = chambermaidFirst, second = chambermaidSecond,
        eligibleNames = chambermaidTargetCards.mapTo(mutableSetOf()) { it.name },
    )
    val chambermaidPresentation = clocktowerChambermaidSelectionPresentation(cards, chambermaidSelection)
    fun roleActor(enName: String): PlayerCard? = sessionGameState.players.firstOrNull {
        it.alive && it.wakingRole() == RoleId(enName)
    }?.let { cards.getOrNull(it.seat - 1) }

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

    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        (effectiveAbilitySubjectForRole(enName, actor)?.let { subject ->
            AbilityFunctioningSemantics.stateFor(subject, enName)
        } in
            setOf(AbilityFunctioningState.DRUNK, AbilityFunctioningState.POISONED)
        )
    val informationStepBuilder = ClocktowerInformationStepBuilder(
        cards = cards,
        language = language,
        text = ::text,
        roleActor = ::roleActor,
        roleMissingReason = ::roleMissingReason,
        abilityStateFor = { enName, actor ->
            effectiveAbilitySubjectForRole(enName, actor)?.let { subject ->
                AbilityFunctioningSemantics.stateFor(subject, enName)
            }
        },
    )

    val empathActor = roleActor("Empath")
    val effectiveEmpathCards = cards.filterIndexed { index, _ ->
        sessionGameState.playerAt(index + 1)?.alive == true
    }
    val empathNeighbors = empathActor?.let { livingNeighbors(effectiveEmpathCards, it.name) }.orEmpty()
    // Capture query seats before the information-publication callbacks that record the
    // actual Fortune Teller result; the materializer below reuses this same seat list.
    val fortuneTellerSelectedNames = listOfNotNull(fortuneTellerFirst, fortuneTellerSecond)
    val fortuneTellerSubjectSeats = fortuneTellerSelectedNames.mapNotNull { name ->
        cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
    }
    fun informationDecisionPublicationAllowed(displayStep: ClocktowerNightStepUi): Boolean =
        clocktowerInformationPublicationAllowed(
            sourceRevision = displayStep.informationSourceRevision,
            currentRevision = StorytellerDecisionRevision(gameStateRevision, playerInputRevision),
        )
    fun unreliableInformationCanBeRecorded(displayStep: ClocktowerNightStepUi): Boolean {
        if (!clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)) return true
        if (displayStep.displayKind !in setOf(
                ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo,
                ClocktowerDisplayKind.EitherOne, ClocktowerDisplayKind.RoleReveal,
            )) return true
        val actorSeat = displayStep.actor?.let { cards.indexOf(it) + 1 }
            ?.takeIf { it > 0 } ?: return false
        return clocktowerShownUnreliableProposition(
            step = displayStep,
            actorSeat = actorSeat,
            allSeats = cards.indices.map { it + 1 },
            empathNeighborSeats = empathNeighbors.map { cards.indexOf(it) + 1 },
            fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
        ) != null
    }
    fun recordReliablePrivateInformation(displayStep: ClocktowerNightStepUi, stepId: String): Boolean {
        val actor = displayStep.actor ?: return false
        val actorSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return false
        if (!informationDecisionPublicationAllowed(displayStep)) return false
        val unreliable = clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)
        val shownUnreliable = if (unreliable) clocktowerShownUnreliableProposition(
            step = displayStep,
            actorSeat = actorSeat,
            allSeats = cards.indices.map { it + 1 },
            empathNeighborSeats = empathNeighbors.map { cards.indexOf(it) + 1 },
            fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
        ) else null
        // Never silently omit misinformation after the first-night marker was accepted.
        // In particular, these fallbacks are TRUE answers and may only serve reliable abilities.
        if (unreliable) {
            if (displayStep.displayKind !in setOf(
                    ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo,
                    ClocktowerDisplayKind.EitherOne, ClocktowerDisplayKind.RoleReveal,
                )) return false
            requireNotNull(shownUnreliable) { "Actual unreliable displayed information lacks a typed proposition." }
        }
        val proposition = shownUnreliable ?: displayStep.displayProposition ?: return false
        onCommitConfirmedPrivateInformationPublication(EpistemicObservationDraft(
            recordId = clocktowerPrivateObservationRecordId(
                gameId = gameId,
                phase = phase,
                round = round,
                roleEnName = requireNotNull(displayStep.roleEnName),
                actorSeat = actorSeat,
                stepId = stepId,
            ),
            phase = phase.toStorytellerPhase(),
            round = round, sequence = (sessionState.nextTimelineGlobalSequence + 1).toInt(), sourceSeat = actorSeat,
            sourceAbility = RoleId(requireNotNull(displayStep.roleEnName)), visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(actorSeat),
            reliability = if (clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)) {
                ObservationReliability.KNOWN_MALFUNCTIONING
            } else {
                ObservationReliability.RECEIVED_AS_FUNCTIONING
            },
            proposition = proposition,
        ), displayStep.informationSourceRevision ?: StorytellerDecisionRevision(gameStateRevision, playerInputRevision))
        return true
    }
    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val minionCards = cards.filter { it.clocktowerRole?.team == ClocktowerTeam.Minion }
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
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
    // Session owns true and shown roles; player cards are only a presentation surface.
    val chambermaidMaterializer = clocktowerChambermaidStepMaterializer(
        builder = informationStepBuilder,
        content = ClocktowerChambermaidStepContent(
            explanation = text("记录侍女实际查询的目标和展示数字。", "Record the Chambermaid's actual targets and shown number."),
            displayFooter = text("查询这两名玩家", "Checking these two players"),
            hostInstruction = text("唤醒侍女，记录她选择的两名玩家，再输入展示数字。", "Wake the Chambermaid, record the two chosen players, then enter the shown number."),
        ),
        presentation = chambermaidPresentation,
    )
    val empathStepMaterializer = clocktowerEmpathStepMaterializer(
        informationStepBuilder, empathNeighbors.map { cards.indexOf(it) + 1 }, ::text,
    )
    val fortuneTellerStepMaterializer = clocktowerFortuneTellerStepMaterializer(
        informationStepBuilder, fortuneTellerSubjectSeats, ::text,
    )

    val commonRoleMaterializers = clocktowerCommonNightMaterializers(
        informationStepBuilder, cards, ::text, poisonTarget, butlerMaster, spySeatLabel,
    ) + listOf(empathStepMaterializer, chambermaidMaterializer, fortuneTellerStepMaterializer)

    val nightSteps = if (phase == ClocktowerPhase.FirstNight) {
        val firstNightMaterializers = ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            entries = commonRoleMaterializers + clocktowerFirstNightMaterializers(
                informationStepBuilder, cards, ::text, firstNightEvilInformationSteps, redHerring,
            ),
        )
        firstNightMaterializers.materialize(nightInteractions)
    } else {
    val otherNightMaterializers = ClocktowerNightStepMaterializerRegistry(
        phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
        entries = commonRoleMaterializers + nightHostProjection.otherNightMaterializers(
            informationStepBuilder, ::text, ravenkeeperTarget,
        ),
    )
        otherNightMaterializers.materialize(
            nightInteractions,
        )
    }

    // The first night has no generic preparation page. The only possible interruption
    // before its first actionable role is a REAL uncommitted Demon-bluff decision.
    if (phase == ClocktowerPhase.FirstNight && sessionState.demonBluffRoleIds.isEmpty() && manualDemonBluffsRequired) {
        // Automatic mode may accept an already verified joint-opening recommendation.
        // It must not fast-forward any player information display.
        LaunchedEffect(
            globalAiAutomatic, jointOpeningAvailable, manualDemonBluffsReady,
            manualDemonBluffDraft,
        ) {
            if (globalAiAutomatic && jointOpeningAvailable && manualDemonBluffsReady && sessionState.demonBluffRoleIds.isEmpty()) {
                onCommitDemonBluffs(manualDemonBluffDraft)
            }
        }
        ClocktowerStorytellerRecommendationScreen(
            title = text("恶魔伪装身份", "DEMON BLUFFS"),
            subtitle = text("确认实际恶魔伪装", "Confirm the actual Demon bluffs"),
            description = text(
                "请确认本局恶魔实际获得的三项伪装身份。这个真实配置决定确认后，直接进入首夜角色。",
                "Confirm the three actual Demon bluffs. This is a real setup decision; the first night starts directly afterward.",
            ),
            buttonLabel = text("确认恶魔伪装", "Confirm Demon bluffs"),
            onHostTools = onHostTools,
            startEnabled = manualDemonBluffsReady,
            onStartNight = {
                if (manualDemonBluffsReady) {
                    onCommitDemonBluffs(manualDemonBluffDraft)
                    }
            },
        ) {
            ClocktowerDemonBluffManualPicker(
                legalRoles = legalDemonBluffs,
                selectedRoleNames = manualDemonBluffDraft,
                language = language,
                onSelectionChange = { nextSelection ->
                    manualDemonBluffDraft = nextSelection
                },
            )
        }
        return
    }

    // Later nights and first nights without an outstanding real setup decision
    // enter their first actionable step directly, regardless of the old UI start flag.
    if (phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) {
        val currentStepIndex = nightSteps.indexOfFirst { it.interactionId == nextNightInteraction?.id }
        LaunchedEffect(gameId, phase, round, nextNightInteraction?.id, gameStateRevision, playerInputRevision) {
            if (nextNightInteraction == null && sessionState.outcome == null) {
                if (phase == ClocktowerPhase.FirstNight) onAdvanceFromFirstNight() else onCompleteNight()
            }
        }
        if (nextNightInteraction == null) return
        check(currentStepIndex >= 0) { "Missing presentation for current Session night interaction." }
        val currentStep = nightSteps[currentStepIndex]
        // A fullscreen preview temporarily replaces this role's composable subtree.
        // Only a draft for this exact Host interaction can enable final Next.
        val currentInformationPreview = lastInformationPreview?.takeIf {
            it.interactionId == currentStep.interactionId && informationDecisionPublicationAllowed(it)
        }
        val isEvilInformationStep = currentStep.displayKind == ClocktowerDisplayKind.EvilInfo
        val isPrivateInformationStep = currentStep.isRealAction &&
            currentStep.actor != null && currentStep.roleEnName != null &&
            currentStep.action in setOf(
                ClocktowerNightAction.None,
                ClocktowerNightAction.FortuneTeller,
                ClocktowerNightAction.Chambermaid,
                ClocktowerNightAction.Ravenkeeper,
            ) && currentStep.roleEnName != "Spy"
        // Only a reliable, matching Host-authored *numeric* fact is known to
        // require no AI discretion. Other typed statements may still be discretionary.
        val currentActorSeatForInformation = currentStep.actor?.name?.let { name ->
            cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
        }
        val hostDeterminedNumeric = currentActorSeatForInformation?.let { seat ->
            clocktowerDeterminedNumericResult(currentStep, seat)
        } != null
        val globalAiPending = if (globalAiAssisted && !hostDeterminedNumeric &&
            phase == ClocktowerPhase.FirstNight) {
            pendingGlobalFirstNightPairDecision(
                currentStep, phase, round, cards, sessionGameState, gameId,
                com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision(
                    gameStateRevision, playerInputRevision),
            )
        } else null
        val globalAiScalarPending = if (globalAiAssisted &&
            !hostDeterminedNumeric &&
            phase in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) &&
            globalAiPending == null
        ) pendingGlobalNightScalarDecision(
            currentStep, phase, round, currentStepIndex, cards, gameId,
            StorytellerDecisionRevision(gameStateRevision, playerInputRevision),
            listOfNotNull(fortuneTellerFirst, fortuneTellerSecond),
        ) else null
        val globalAiTargetPending = globalNightPendingDecision?.takeIf {
            globalAiAssisted && phase == ClocktowerPhase.Night &&
                currentStep.action == ClocktowerNightAction.MayorRedirect &&
                currentStep.isRealAction
        }
        val globalAiDecisionIdentity = globalAiPending?.requestIdentity
            ?: globalAiScalarPending?.requestIdentity
            ?: globalAiTargetPending?.requestIdentity
        val globalAiCurrentKey = globalAiDecisionIdentity?.let { identity ->
            ClocktowerResidualRequestIdentityV2.build(
                gameId, round, currentStepIndex, identity.requestId,
                gameStateRevision, playerInputRevision, sessionState.nextTimelineGlobalSequence,
                if (globalAiAutomatic) StorytellerOperationMode.AI_AUTOMATIC
                else StorytellerOperationMode.AI_ASSISTED,
            )
        }
        // Host revalidates each currently recommended option against the live
        // legal display domain; no package-level confirmation grants authority.
        val preplannedSeat = currentStep.actor?.let { actor ->
            cards.indexOfFirst { it.name == actor.name }.takeIf { it >= 0 }?.plus(1)
        }
        // An opening information choice is NEVER a valid default for a later
        // night, even when the same role wakes again and the number matches.
        val residualRecommendedOption = if (globalAiCurrentKey != null &&
            globalAiAdviceKey == globalAiCurrentKey
        ) when {
            globalAiPending != null -> ClocktowerResidualDisplayMatcherV2.matchPair(
                globalAiPending,
                globalResidualRecommendedChoices[globalAiPending.requestIdentity.requestId],
                cards, language)
            globalAiScalarPending != null -> ClocktowerResidualDisplayMatcherV2.matchScalar(
                globalAiScalarPending,
                globalResidualRecommendedChoices[globalAiScalarPending.requestIdentity.requestId])
            else -> null
        } else null
        // Busy/error belong to their exact Host-current request. A stale
        // failed/in-flight earlier step must not block a new deterministic step.
        val currentAiRequestBlocked = clocktowerCurrentAiRequestBlocked(
            globalAiCurrentKey, globalAiAdviceKey, globalAiAdviceBusy, globalAiAdviceError,
        )
        // Match direct AI target advice to the current question, revision and canonical seats.
        val recommendedMayorTargetName = globalAiTargetPending?.takeIf {
            globalAiCurrentKey != null &&
                globalAiAdviceKey == globalAiCurrentKey &&
                !currentAiRequestBlocked
        }?.let { decision ->
            val visibleEligibleNames = mayorRedirectTargetCards.mapTo(mutableSetOf()) { it.name }
            cards.getOrNull(decision.mayorSeat - 1)?.name?.let(visibleEligibleNames::add)
            val eligibleSeats = cards.mapIndexedNotNull { index, card ->
                (index + 1).takeIf { card.name in visibleEligibleNames }
            }.toSet()
            ClocktowerResidualDisplayMatcherV2.matchMayorRedirect(
                decision = decision,
                selected =
                    globalResidualRecommendedChoices[decision.requestIdentity.requestId],
                currentRevision = com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision(
                    gameStateRevision, playerInputRevision,
                ),
                currentSeats = eligibleSeats,
            )?.let { seat -> cards.getOrNull(seat - 1)?.name }
        }
        // MANUAL never consumes an AI opening plan; a failed current automatic request
        // may not silently reuse an old recommendation.
        val approvedOpeningOption = if (!globalAiAssisted ||
            (globalAiAutomatic && currentAiRequestBlocked)) null else
            residualRecommendedOption ?: preplannedSeat?.takeIf {
                phase == ClocktowerPhase.FirstNight
            }?.let { seat ->
            jointOpeningSemantics[seat]?.let { planned ->
                ClocktowerJointOpeningDisplayMatcherV2.match(
                    question = currentStep.informationQuestion,
                    plan = planned,
                    currentSourceSeat = seat,
                    currentOptions = currentStep.manualInformationCandidates,
                )
            }
        }
        LaunchedEffect(globalAiCurrentKey, globalAiAssisted, approvedOpeningOption,
            jointOpeningAvailable, phase) {
            if (globalAiAssisted &&
                globalAiCurrentKey != null &&
                globalAiAdviceKey != globalAiCurrentKey &&
                approvedOpeningOption == null &&
                !(currentStep.action == ClocktowerNightAction.MayorRedirect &&
                    mayorRedirectDraftTarget != null)
            ) {
                onRequestGlobalAiAdvice(nightSteps, currentStepIndex)
            }
        }
        val currentSurfacePlan = clocktowerNightSurfacePlan(currentStep, phase)
        val selectedNightName = when (currentStep.action) {
            ClocktowerNightAction.RedHerring -> redHerring
            ClocktowerNightAction.Poison -> poisonDraftTarget
            ClocktowerNightAction.ButlerMaster -> butlerMaster
            ClocktowerNightAction.MonkProtect -> monkProtectedDraftTarget
            ClocktowerNightAction.DemonKill -> demonAttackDraftTarget
            ClocktowerNightAction.MayorRedirect -> mayorRedirectDraftTarget
            ClocktowerNightAction.Ravenkeeper -> ravenkeeperTarget
            else -> null
        }
        // Reuse exactly the current square-table's Host target domain. Player
        // actions remain player choices, never AI-selected defaults.
        val legalPlayerTargetNames = when (currentStep.action) {
            ClocktowerNightAction.Poison, ClocktowerNightAction.DemonKill ->
                publicAliveCards.mapTo(mutableSetOf()) { it.name }
            ClocktowerNightAction.ButlerMaster ->
                cards.filter { it.name != currentStep.actor?.name }.mapTo(mutableSetOf()) { it.name }
            ClocktowerNightAction.MonkProtect ->
                cards.mapTo(mutableSetOf()) { it.name }
            else -> emptySet()
        }
        val playerTargetReady = clocktowerLivePlayerTargetReady(
            currentStep.action, currentStep.isRealAction,
            selectedNightName, legalPlayerTargetNames,
        )
        val succession = if (currentStep.action == ClocktowerNightAction.DemonKill && currentStep.isRealAction) {
            sessionState.impSelfKillSuccession(selectedNightName?.let { name ->
                sessionGameState.players.firstOrNull { it.name == name }?.seat
            })
        } else DemonSuccessionResolution.None
        val eligibleSuccessors = when (succession) {
            is DemonSuccessionResolution.Forced -> listOf(succession.targetSeat)
            is DemonSuccessionResolution.Choice -> succession.targetSeats.toList()
            DemonSuccessionResolution.None -> emptyList()
        }.map { requireNotNull(sessionGameState.playerAt(it)) }
        var selectedSelfKillSuccessor by remember(
            gameId, phase, round, currentStep.interactionId,
        ) { mutableStateOf<String?>(null) }
        val successorName = when (eligibleSuccessors.size) {
            0 -> null
            1 -> eligibleSuccessors.single().name
            else -> selectedSelfKillSuccessor
        }
        val successorReady = eligibleSuccessors.size <= 1 ||
            successorName in eligibleSuccessors.map { it.name }
        val advanceNightStep = advance@{
            // Check readiness before the only irreversible Observation write.
            if (!playerTargetReady || !successorReady) {
                Toast.makeText(context, text(
                    "请先完成当前行动目标及必要的恶魔继任选择。",
                    "Select the current target and any required Imp successor before continuing.",
                ), Toast.LENGTH_LONG).show()
                return@advance
            }
            if (isPrivateInformationStep) {
                val preview = currentInformationPreview
                if (preview == null ||
                    preview.actor?.name != currentStep.actor?.name ||
                    preview.roleEnName != currentStep.roleEnName
                ) {
                    Toast.makeText(context, text(
                        "请先点击展示按钮，再点击下一步确认最后展示的信息。",
                        "Preview the selected information before confirming Next.",
                    ), Toast.LENGTH_LONG).show()
                    return@advance
                }
                // Only the final preview is recorded. The previous previews
                // have no Session footprint and may be discarded on interruption.
                if (!recordReliablePrivateInformation(preview, currentStep.interactionId.toString())) {
                    Toast.makeText(context, text(
                        "当前展示信息无效或已经过期，请重新选择并展示。",
                        "The preview is invalid or stale; select and preview again.",
                    ), Toast.LENGTH_LONG).show()
                    return@advance
                }
                lastInformationPreview = null
            }
            if (currentStep.action == ClocktowerNightAction.RedHerring &&
                currentStep.isRealAction
            ) {
                onConfirmRedHerring(requireNotNull(redHerring) {
                    "Red Herring requires a selected rules-legal player."
                })
            }
            // Non-real night placeholders never commit an action or a
            // synthetic revision. Only the active player/ruling has authority.
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.Poison) {
                onConfirmPoisonTarget(currentStep.interactionId.toString())
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.MonkProtect) {
                onConfirmMonkProtectedTarget(currentStep.interactionId.toString())
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.DemonKill) {
                onConfirmDemonAttack(
                    currentStep.interactionId.toString(),
                    successorName,
                )
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.MayorRedirect) {
                onConfirmMayorRedirectTarget()
            }
            if (currentStep.isRealAction && currentStep.action == ClocktowerNightAction.ButlerMaster) {
                onConfirmButlerMaster(
                    requireNotNull(currentStep.actor).name,
                    requireNotNull(butlerMaster) { "Butler must choose a master before proceeding." },
                    currentStep.interactionId.toString(),
                )
            }
            if (currentStep.roleEnName == "Spy") onConfirmSpyWake(requireNotNull(currentStep.interactionId).value)
            if (currentStep.displayKind == ClocktowerDisplayKind.EvilInfo) {
                onConfirmEvilInformation(requireNotNull(currentStep.interactionId).value, requireNotNull(currentInformationPreview?.informationSourceRevision))
            }
        }
        LaunchedEffect(recommendedMayorTargetName, currentStep.interactionId, selectedNightName, globalAiAssisted) {
            if (globalAiAssisted && currentStep.action == ClocktowerNightAction.MayorRedirect && selectedNightName == null) {
                recommendedMayorTargetName?.let(onSelectMayorRedirectTarget)
            }
        }

        ClocktowerNightActiveScreen(
            progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
            nextEnabled =
                (!(isPrivateInformationStep || isEvilInformationStep) || currentInformationPreview != null) &&
                playerTargetReady && successorReady && (currentStep.action !in setOf(
                ClocktowerNightAction.MayorRedirect,
            ) || selectedNightName != null) &&
                (currentStep.action != ClocktowerNightAction.RedHerring ||
                    !currentStep.isRealAction || redHerring != null),
            onHostTools = onHostTools,
            onNext = advanceNightStep,
            contentOwnsFullScreen = currentSurfacePlan.ownsFullScreenHostSurface,
            lastPreviewLabel = currentInformationPreview?.takeIf { isPrivateInformationStep || isEvilInformationStep }?.let { preview ->
                text("✓ 上次预览（下一步确认）", "✓ Last preview (confirm with Next)") + " · " +
                    listOfNotNull(preview.displayPrimary, preview.displaySecondary)
                        .filter(String::isNotBlank).joinToString(" · ")
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (globalAiAssisted && globalAiDecisionIdentity != null &&
                    globalAiCurrentKey != null
                ) {
                    if (!globalAiAutomatic ||
                        (approvedOpeningOption == null &&
                            (!globalAiAdviceBusy || globalAiAdviceKey != globalAiCurrentKey))
                    ) {
                        OutlinedButton(
                            onClick = { onRequestGlobalAiAdvice(nightSteps, currentStepIndex) },
                            enabled = !globalAiAdviceBusy || globalAiAdviceKey != globalAiCurrentKey,
                        ) {
                            Text(if (globalAiAutomatic) text("重新请求 AI 信息", "Retry AI information")
                                else text("AI 全局局势与当前合法裁量", "Global AI advice"))
                        }
                    }
                    if (globalAiAdviceKey == globalAiCurrentKey) {
                        if (globalAiAdviceBusy) Text(
                            text("正在重新评估已确认的游戏事件…", "Analysing committed game history…"),
                        )
                        globalAiAdviceError?.let { error -> Text(text = error) }
                        globalAiAdviceText?.let { advice ->
                            Text(text = if (globalAiAutomatic && globalAiAdviceError == null)
                                text("AI 已更新整局规划，合法结果将在相应夜晚步骤使用。",
                                    "Whole-game strategy updated; legal results are used in their night steps.")
                            else advice)
                        }
                    }
                }
                ClocktowerNightStepCardLocalized(
                scriptRoles = clocktowerRolesForScript(script),
                allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                phase = phase,
                gameId = gameId,
                round = round,
                sequence = currentStepIndex,
                gameStateRevision = gameStateRevision,
                playerInputRevision = playerInputRevision,
                informationDecisionKey = "$gameId:$gameStateRevision:$playerInputRevision:${currentStep.interactionId}",
                cards = cards,
                actualAlignmentBySeat = sessionGameState.players.associate { it.seat to it.actualAlignment },
                ghostVoteAuthority = ghostVoteAuthority,
                poisonedPlayerName = currentStep.roleEnName?.let(effectivePoisonForRole),
                aliveCards = publicAliveCards,
                chambermaidTargetCards = chambermaidTargetCards,
                mayorRedirectTargetCards = mayorRedirectTargetCards,
                // One optional current-step suggestion for both AI modes.
                // The card retains the complete Host legal domain for manual edits.
                // Automatic mode can use the same verified choice at this step only.
                aiSuggestedInformationOption = approvedOpeningOption.takeUnless {
                    currentAiRequestBlocked
                },
                // AI may select the result but NEVER open the player fullscreen
                // automatically. Showing private information requires a live tap.
                step = currentStep,
                surfacePlan = currentSurfacePlan,
                selectedName = selectedNightName,
                fortuneTellerFirst = fortuneTellerFirst,
                fortuneTellerSecond = fortuneTellerSecond,
                redHerring = redHerring,
                chambermaidFirst = chambermaidSelection.first,
                chambermaidSecond = chambermaidSelection.second,
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
                            ClocktowerNightAction.Ravenkeeper -> {
                                lastInformationPreview = null
                                onSelectRavenkeeperTarget(nextSelection)
                            }
                            else -> Unit
                        }
                },
                onSelectFortuneTellerFirst = {
                    lastInformationPreview = null
                    onSelectFortuneTellerFirst(if (fortuneTellerFirst == it) null else it)
                },
                onSelectFortuneTellerSecond = {
                    lastInformationPreview = null
                    onSelectFortuneTellerSecond(if (fortuneTellerSecond == it) null else it)
                },
                onSelectChambermaidFirst = {
                    lastInformationPreview = null
                    onSelectChambermaidFirst(if (chambermaidFirst == it) null else it)
                },
                onSelectChambermaidSecond = {
                    lastInformationPreview = null
                    onSelectChambermaidSecond(if (chambermaidSecond == it) null else it)
                },
                onShowPlayerDisplay = { selectedStep ->
                    val preview = selectedStep.copy(informationSourceRevision =
                        StorytellerDecisionRevision(gameStateRevision, playerInputRevision))
                    if (!informationDecisionPublicationAllowed(preview) ||
                        !unreliableInformationCanBeRecorded(preview)
                    ) {
                        Toast.makeText(context, text(
                            "当前信息缺少准确的结构化展示值，不能预览。请重新选择。",
                            "The selected information cannot be previewed safely. Choose a concrete result.",
                        ), Toast.LENGTH_LONG).show()
                    } else {
                        // D2: these are UI-only previews. Returning to this role
                        // permits replacing the selection. The final typed
                        // observation is written only by this role's Next.
                        lastInformationPreview = preview
                        playerDisplayStep = preview
                    }
                },
                onInformationDraftChanged = { lastInformationPreview = null },
                lastPreviewProposition = currentInformationPreview?.displayProposition,
                        onHostTools = onHostTools,
                onNext = advanceNightStep,
                )
                if (eligibleSuccessors.size > 1 &&
                    !successorReady) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text(text("小恶魔自杀 · 选择继任者", "Imp self-kill · successor")) },
                        text = {
                            Column {
                                eligibleSuccessors.forEach { candidate ->
                                    TextButton(onClick = {
                                        selectedSelfKillSuccessor = candidate.name
                                    }) {
                                        Text(playerSeatLabel(cards, candidate.name))
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                    )
                }
            }
        }
        return
    }
}
