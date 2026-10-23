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
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import com.codex.campboardgamehost.clocktower.session.PendingMayorRedirectDecision
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
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
    onRequestGlobalAiAdvice: (List<ClocktowerNightStepUi>, Int, List<String>) -> Unit = { _, _, _ -> },
    recommendedDemonBluffRoleNames: List<String>,
    onCommitConfirmedPrivateInformationPublication: (EpistemicObservationDraft, StorytellerDecisionRevision) -> Unit,
    onHostTools: () -> Unit,
    /** Advances only the AI-staleness input revision; never a game fact or event. */
    onRecordPlayerInput: () -> Unit,
    onConfirmNightStep: (ClocktowerNightStepConfirmation) -> Unit,
    onCommitDemonBluffs: (List<String>) -> Unit,
    onConfirmNewDemon: () -> Unit,
    onConfirmKlutzChoice: (String) -> Unit,
    onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onConfirmedNomination: (String, String, Boolean?, Int) -> Boolean,
    onConfirmedVote: (String, String, ClocktowerConfirmedVoteRecord, Int) -> Unit,
    rulesetCatalog: BuiltInClocktowerRulesetCatalog = BuiltInClocktowerRulesetCatalog.fromContext(LocalContext.current),
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

    val nightRuleset = rulesetCatalog.ruleset(script)
    val flowPhase = when {
        phase == ClocktowerPhase.FirstNight -> ClocktowerNightFlowPhase.FIRST_NIGHT
        phase == ClocktowerPhase.Night -> ClocktowerNightFlowPhase.OTHER_NIGHT
        else -> null
    }
    val nextNightInteraction = flowPhase?.let { sessionState.nextNightInteraction(nightRuleset) }
    // UI-only, keyed to this exact interaction (IDs carry no round); outlives the fullscreen preview.
    var nightDraft by remember(gameId, phase, round, nextNightInteraction?.id) {
        mutableStateOf(nextNightInteraction?.let { ClocktowerNightStepDraft(gameId, sessionState.phase, round, it.id) })
    }
    fun seatOf(name: String?): Int? = name?.let { selected ->
        cards.indexOfFirst { it.name == selected }.takeIf { it >= 0 }?.plus(1)
    }
    fun nameAt(seat: Int?): String? = seat?.let { cards.getOrNull(it - 1)?.name }
    // Builders run for every step; only the current interaction gets its draft.
    fun draftSlotsFor(identity: ClocktowerProductionNightStepIdentity): List<Int?> =
        if (flowPhase != null && nightDraft?.interactionId == identity.interactionId(flowPhase)) nightDraft?.seatSlots.orEmpty() else emptyList()
    val fortuneTellerSlots = draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller")))
    val fortuneTellerFirst = nameAt(fortuneTellerSlots.getOrNull(0))
    val fortuneTellerSecond = nameAt(fortuneTellerSlots.getOrNull(1))
    val chambermaidSlots = draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Chambermaid")))
    val ravenkeeperTargetSeat = draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper"))).firstOrNull()
    val butlerMaster = nameAt(draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Butler"))).firstOrNull())
    // Accepted Red Herring comes from Session; its draft exists only on its own first-night step.
    val redHerring = nameAt((if (flowPhase == ClocktowerNightFlowPhase.FIRST_NIGHT) draftSlotsFor(
        ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()).firstOrNull() else null) ?: sessionState.redHerringSeat)

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

    // Host night facts are Session seat reads; cards only provide labels.
    val spySeatLabel = sessionState.actualRoleSeats("Spy").firstOrNull()?.let({ cards.getOrNull(it - 1) })?.seatLabel(cards)
    val nightInteractions = sessionState.nightInteractions(nightRuleset)
    val aliveSeats = sessionState.aliveSeats()
    val mayorRedirectTargetSeats = sessionState.mayorRedirectEligibleSeats()
    val demonCard = sessionState.livingDemonSeat()?.let({ cards.getOrNull(it - 1) })
    fun abilityStateFor(enName: String, actor: PlayerCard?): AbilityFunctioningState? =
        sessionState.abilitySubjectForRole(enName, seatOf(actor?.name))?.let { AbilityFunctioningSemantics.stateFor(it, enName) }
    val chambermaidSelection = revalidateTwoPlayerSelection(chambermaidSlots.getOrNull(0), chambermaidSlots.getOrNull(1), aliveSeats)
    val chambermaidPresentation = clocktowerChambermaidSelectionPresentation(cards, chambermaidSelection)
    fun roleActor(enName: String): PlayerCard? = sessionGameState.players.firstOrNull {
        it.alive && it.wakingRole() == RoleId(enName)
    }?.let { cards.getOrNull(it.seat - 1) }

    fun roleMissingReason(enName: String): String {
        val rolePlayer = sessionGameState.players.firstOrNull { it.actualRole.value == enName }
        val roleCard = rolePlayer?.let { cards.getOrNull(it.seat - 1) }
        val drunkShownAsRole = sessionGameState.players.any { it.actualRole.value == "Drunk" && it.shownRole?.value == enName }
        return when {
            rolePlayer == null && drunkShownAsRole -> ""
            rolePlayer == null || roleCard == null -> text("本局没有这个角色。", "This character is not in play.")
            !rolePlayer.alive -> text(
                "${roleCard.seatLabel(cards)} 已经死亡，死亡后不再执行这个能力。",
                "${roleCard.seatLabel(cards)} is dead and no longer uses this ability.",
            )
            else -> ""
        }
    }

    fun actorIsUnreliable(enName: String, actor: PlayerCard?): Boolean =
        abilityStateFor(enName, actor) in setOf(AbilityFunctioningState.DRUNK, AbilityFunctioningState.POISONED)
    val informationStepBuilder = ClocktowerInformationStepBuilder(
        cards = cards,
        language = language,
        text = ::text,
        roleActor = ::roleActor,
        roleMissingReason = ::roleMissingReason,
        abilityStateFor = ::abilityStateFor,
    )

    val empathActor = roleActor("Empath")
    val empathNeighborSeats = seatOf(empathActor?.name)?.let(sessionState::livingNeighbourSeats).orEmpty()
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
    fun displayIsUnreliable(displayStep: ClocktowerNightStepUi) =
        clocktowerDisplayedInformationIsUnreliable(displayStep, ::actorIsUnreliable)
    val recordableUnreliableKinds = setOf(
        ClocktowerDisplayKind.Number, ClocktowerDisplayKind.YesNo, ClocktowerDisplayKind.EitherOne, ClocktowerDisplayKind.RoleReveal,
    )
    // The exact proposition Next records for this preview. Misinformation is never silently
    // omitted; the true display proposition may only serve a reliable ability.
    fun shownPropositionFor(displayStep: ClocktowerNightStepUi): InformationProposition? {
        if (!displayIsUnreliable(displayStep)) return displayStep.displayProposition
        val actorSeat = seatOf(displayStep.actor?.name) ?: return null
        if (displayStep.displayKind !in recordableUnreliableKinds) return null
        return clocktowerShownUnreliableProposition(
            step = displayStep,
            actorSeat = actorSeat,
            allSeats = cards.indices.map { it + 1 },
            empathNeighborSeats = empathNeighborSeats,
            fortuneTellerSubjectSeats = fortuneTellerSubjectSeats,
        )
    }
    // Previews of unsupported unreliable kinds stay viewable; their Next is refused.
    fun unreliableInformationCanBeRecorded(displayStep: ClocktowerNightStepUi): Boolean =
        !displayIsUnreliable(displayStep) || displayStep.displayKind !in recordableUnreliableKinds ||
            shownPropositionFor(displayStep) != null
    fun recordReliablePrivateInformation(displayStep: ClocktowerNightStepUi, stepId: String): Boolean {
        val actorSeat = seatOf(displayStep.actor?.name) ?: return false
        if (!informationDecisionPublicationAllowed(displayStep)) return false
        val proposition = shownPropositionFor(displayStep) ?: return false
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
            reliability = if (displayIsUnreliable(displayStep)) {
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
    val minionCards = sessionState.actualTypeSeats(CharacterType.MINION).mapNotNull { cards.getOrNull(it - 1) }
    val shouldGiveFirstNightEvilInfo = sessionGameState.players.size >= 7
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
        informationStepBuilder, empathNeighborSeats, ::text,
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
                fortuneTellerInPlay = sessionState.actualRoleSeats("Fortune Teller").isNotEmpty(),
            ),
        )
        firstNightMaterializers.materialize(nightInteractions)
    } else {
    val otherNightMaterializers = ClocktowerNightStepMaterializerRegistry(
        phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
        entries = commonRoleMaterializers + clocktowerOtherNightMaterializers(
            cards, sessionState, informationStepBuilder, ::text, ravenkeeperTargetSeat,
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
        val host = hostNightStep(sessionState, nightRuleset, nightDraft)
        // Verify the UI materializer matches the actual pending Session interaction.
        // Display projections may not assign role, action, actor or numeric query authority.
        check(currentStep.interactionId == host.facts.interactionId &&
            currentStep.action == host.facts.action &&
            currentStep.isRealAction == host.facts.isRealAction &&
            currentStep.informationQuestion == host.facts.question
        ) { "Night display differs from the authoritative Session interaction." }
        val ai = nightAiOverlay(host, NightAiInputs(
            globalAiAssisted, globalAiAutomatic, globalAiAdviceKey, globalAiAdviceBusy, globalAiAdviceError,
            globalResidualRecommendedChoices, jointOpeningSemantics, globalNightPendingDecision, fortuneTellerSelectedNames,
        ), sessionState, nightRuleset, currentStep, currentStepIndex, phase, cards, language, clocktowerRolesForScript(script))
        LaunchedEffect(ai.currentKey, globalAiAssisted, ai.validatedOption, jointOpeningAvailable, phase) {
            if (ai.shouldRequest) onRequestGlobalAiAdvice(nightSteps, currentStepIndex, fortuneTellerSelectedNames)
        }
        val currentSurfacePlan = clocktowerNightSurfacePlan(currentStep, phase)
        fun updateNightDraft(next: ClocktowerNightStepDraft) {
            if (currentStep.action in setOf(
                    ClocktowerNightAction.Ravenkeeper, ClocktowerNightAction.FortuneTeller, ClocktowerNightAction.Chambermaid,
                )) lastInformationPreview = null
            // Red Herring stays a pure draft until its Next; it does not advance the AI input revision.
            if (currentStep.action != ClocktowerNightAction.RedHerring) onRecordPlayerInput()
            nightDraft = next
        }
        var selectedSelfKillSuccessor by remember(
            gameId, phase, round, currentStep.interactionId,
        ) { mutableStateOf<Int?>(null) }
        val successorSeat = host.successorSeat(selectedSelfKillSuccessor)
        val successorReady = host.successorReady(selectedSelfKillSuccessor)
        val advanceNightStep = advance@{
            // Check readiness before the only irreversible Observation write.
            if (!host.targetReady || !successorReady) {
                Toast.makeText(context, text(
                    "请先完成当前行动目标及必要的恶魔继任选择。",
                    "Select the current target and any required Imp successor before continuing.",
                ), Toast.LENGTH_LONG).show()
                return@advance
            }
            if (host.isPrivateInformation) {
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
            // Every step's Next goes through the one Session dispatcher; steps without an action are no-ops.
            onConfirmNightStep(ClocktowerNightStepConfirmation(
                step = currentStep,
                draft = requireNotNull(nightDraft) { "A current night step always has its draft." },
                successorSeat = successorSeat,
                previewRevision = currentInformationPreview?.informationSourceRevision,
            ))
        }
        LaunchedEffect(ai.recommendedMayorSeat, currentStep.interactionId, host.selectedSeat, globalAiAssisted) {
            if (globalAiAssisted && currentStep.action == ClocktowerNightAction.MayorRedirect && host.selectedSeat == null) {
                ai.recommendedMayorSeat?.let { seat ->
                    nightDraft?.let { draft -> updateNightDraft(draft.copy(seatSlots = listOf(seat))) }
                }
            }
        }

        ClocktowerNightActiveScreen(
            progress = text("步骤 ${currentStepIndex + 1} / ${nightSteps.size}", "Step ${currentStepIndex + 1} / ${nightSteps.size}"),
            nextEnabled = (!(host.isPrivateInformation || host.isEvilInformation) || currentInformationPreview != null) &&
                host.targetReady && successorReady,
            onHostTools = onHostTools,
            onNext = advanceNightStep,
            contentOwnsFullScreen = currentSurfacePlan.ownsFullScreenHostSurface,
            lastPreviewLabel = currentInformationPreview?.takeIf {
                host.numericResolution == null && host.pairRole == null &&
                    (host.isPrivateInformation || host.isEvilInformation)
            }?.let { preview ->
                text("✓ 上次预览（下一步确认）", "✓ Last preview (confirm with Next)") + " · " +
                    listOfNotNull(preview.displayPrimary, preview.displaySecondary)
                        .filter(String::isNotBlank).joinToString(" · ")
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val aiKey = ai.currentKey
                if (aiKey != null) {
                    if (!globalAiAutomatic ||
                        (ai.validatedOption == null && (!globalAiAdviceBusy || globalAiAdviceKey != aiKey))
                    ) {
                        OutlinedButton(
                            onClick = { onRequestGlobalAiAdvice(nightSteps, currentStepIndex, fortuneTellerSelectedNames) },
                            enabled = !globalAiAdviceBusy || globalAiAdviceKey != aiKey,
                        ) {
                            Text(if (globalAiAutomatic) text("重新请求 AI 信息", "Retry AI information")
                                else text("AI 全局局势与当前合法裁量", "Global AI advice"))
                        }
                    }
                    if (globalAiAdviceKey == aiKey) {
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
                poisonedPlayerName = nameAt(currentStep.roleEnName?.let(sessionState::poisonedSeatForRole)),
                aliveSeats = aliveSeats,
                chambermaidTargetSeats = aliveSeats,
                mayorRedirectTargetSeats = mayorRedirectTargetSeats,
                livingMayorSeat = sessionGameState.players.firstOrNull { it.alive && it.actualRole.value == "Mayor" }?.seat,
                // One optional current-step suggestion for both AI modes.
                // The card retains the complete Host legal domain for manual edits.
                // Automatic mode can use the same verified choice at this step only.
                aiSuggestedInformationOption = ai.suggestedOption,
                numericResolution = host.numericResolution,
                pairResolver = { role, seat, first, second ->
                    PairInformationResolver.resolve(
                        sessionState, nightRuleset, RoleId(role), seat, first, second,
                    )
                },
                // AI may select the result but NEVER open the player fullscreen
                // automatically. Showing private information requires a live tap.
                step = currentStep,
                surfacePlan = currentSurfacePlan,
                selectedSeatSlots = when (currentStep.action) {
                    ClocktowerNightAction.RedHerring -> listOfNotNull(host.selectedSeat)
                    ClocktowerNightAction.Chambermaid ->
                        listOf(chambermaidSelection.first, chambermaidSelection.second)
                    else -> nightDraft?.seatSlots.orEmpty()
                },
                redHerringSeat = sessionState.redHerringSeat,
                onSelectSeat = { seat ->
                    if (currentStep.action in clocktowerNightSeatDraftActions) {
                        nightDraft?.withTappedSeat(currentStep.action, seat)?.let(::updateNightDraft)
                    }
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
                if (host.successorSeats.size > 1 && !successorReady) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text(text("小恶魔自杀 · 选择继任者", "Imp self-kill · successor")) },
                        text = {
                            Column {
                                host.successorSeats.forEach { seat ->
                                    TextButton(onClick = { selectedSelfKillSuccessor = seat }) {
                                        Text(cards.getOrNull(seat - 1)?.seatLabel(cards).orEmpty())
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
