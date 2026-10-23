package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.session.*

/** Disposable display assembly. Session owns order, wake/skip and facts; this owns labels only. */
@Composable
internal fun clocktowerNightPresentation(
    sessionState: ClocktowerSessionState,
    nightRuleset: ValidatedClocktowerRuleset,
    cards: List<PlayerCard>,
    nightDraft: ClocktowerNightStepDraft?,
    recommendedDemonBluffRoleNames: List<String>,
    language: String,
): List<ClocktowerNightStepUi> {
    val sessionGameState = sessionState.gameState
    val phase = sessionState.phase
    val script = ClocktowerScript.entries.single { it.toRecommendationScriptId() == sessionGameState.script }
    val round = sessionState.round
    val flowPhase = if (phase == StorytellerPhase.FIRST_NIGHT) ClocktowerNightFlowPhase.FIRST_NIGHT else ClocktowerNightFlowPhase.OTHER_NIGHT
    val poisonTarget = sessionState.confirmedPoisonTargetName(round)
    fun text(zh: String, en: String) = if (language == "en") en else zh
    fun nameAt(seat: Int?): String? = seat?.let { cards.getOrNull(it - 1)?.name }
    // Builders run for every step; only the current interaction gets its draft.
    fun draftSlotsFor(identity: ClocktowerProductionNightStepIdentity): List<Int?> =
        if (nightDraft?.interactionId == identity.interactionId(flowPhase)) nightDraft?.seatSlots.orEmpty() else emptyList()
    val fortuneTellerSlots = draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller")))
    val chambermaidSlots = draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Chambermaid")))
    val ravenkeeperTargetSeat = draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper"))).firstOrNull()
    val butlerMaster = nameAt(draftSlotsFor(ClocktowerProductionNightStepIdentity.role(RoleId("Butler"))).firstOrNull())
    // Accepted Red Herring comes from Session; its draft exists only on its own first-night step.
    val redHerring = nameAt((if (flowPhase == ClocktowerNightFlowPhase.FIRST_NIGHT) draftSlotsFor(
        ClocktowerProductionNightStepIdentity.fortuneTellerRedHerring()).firstOrNull() else null) ?: sessionState.redHerringSeat)

    // Host night facts are Session seat reads; cards only provide labels.
    val spySeatLabel = sessionState.actualRoleSeats("Spy").firstOrNull()?.let({ cards.getOrNull(it - 1) })?.seatLabel(cards)
    val nightInteractions = sessionState.nightInteractions(nightRuleset)
    val aliveSeats = sessionState.aliveSeats()
    val demonCard = sessionState.livingDemonSeat()?.let({ cards.getOrNull(it - 1) })
    fun abilityStateFor(enName: String, actor: PlayerCard?): AbilityFunctioningState? =
        sessionState.abilitySubjectForRole(enName, cards.seatOf(actor?.name))?.let { AbilityFunctioningSemantics.stateFor(it, enName) }
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

    val informationStepBuilder = ClocktowerInformationStepBuilder(
        cards = cards,
        language = language,
        text = ::text,
        roleActor = ::roleActor,
        roleMissingReason = ::roleMissingReason,
        abilityStateFor = ::abilityStateFor,
    )

    val empathActor = roleActor("Empath")
    val empathNeighborSeats = cards.seatOf(empathActor?.name)?.let(sessionState::livingNeighbourSeats).orEmpty()
    val fortuneTellerSubjectSeats = fortuneTellerSlots.filterNotNull()
    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val minionCards = sessionState.actualTypeSeats(CharacterType.MINION).mapNotNull { cards.getOrNull(it - 1) }
    val shouldGiveFirstNightEvilInfo = sessionGameState.players.size >= 7
    val legalDemonBluffs = clocktowerRolesForScript(script).distinctBy { it.enName }
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

    return if (phase == StorytellerPhase.FIRST_NIGHT) {
        val firstNightMaterializers = ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            entries = commonRoleMaterializers + clocktowerFirstNightMaterializers(
                informationStepBuilder, cards, ::text, firstNightEvilInformationSteps, redHerring,
                fortuneTellerInPlay = sessionState.actualRoleSeats("Fortune Teller").isNotEmpty(),
            ),
        )
        firstNightMaterializers.materialize(nightInteractions)
    } else {
        ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            entries = commonRoleMaterializers + otherNightDisplayEntries(
                cards, sessionState, informationStepBuilder, ::text, ravenkeeperTargetSeat,
            ),
        ).materialize(nightInteractions)
    }
}

/**
 * Later-night step builders: display only. Night facts come from Session seat reads
 * ([livingDemonSeat], [currentNightDeathTriggerSeat], [deathTriggerAbilityState]); order and
 * mechanical effects stay with Session.
 */
private fun otherNightDisplayEntries(
    cards: List<PlayerCard>,
    state: ClocktowerSessionState,
    informationStepBuilder: ClocktowerInformationStepBuilder,
    text: (String, String) -> String,
    ravenkeeperTargetSeat: Int?,
): List<ClocktowerNightStepMaterializerRegistry.Entry> {
    val game = state.gameState
    fun cardAt(seat: Int?) = seat?.let { cards.getOrNull(it - 1) }
    val demonCard = cardAt(state.livingDemonSeat())
    val demonPoisonedForActionExplanation = state.livingDemonPoisoned()
    val mayorTarget = cardAt(state.mayorRulingSeat())
    val lastExecutedSeat = state.actionTimeline.entries.asReversed().firstNotNullOfOrNull {
        (it.fact as? ActionFact.Execution)?.targetSeat
    }
    val undertakerTargetSeat = lastExecutedSeat?.takeIf { game.playerAt(it) != null }
    val monkProtectedTarget = state.nightTargets.monkSeat?.let(game::playerAt)?.name
    val pendingNightDeath = state.nightTargets.attackSeat?.let(game::playerAt)?.name
    val mayorRedirectTarget = state.nightTargets.mayorRedirectSeat?.let(game::playerAt)?.name
    return listOf(

        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Undertaker")),
            build = {
                informationStepBuilder.build(
                    roleName = "送葬者", enName = "Undertaker", tellPlayer = null,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = undertakerTargetSeat?.let { ClocktowerInformationQuestion.Role(it) },
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
            triggerActor = cardAt(state.currentNightDeathTriggerSeat("Sage")),
            abilityState = state.deathTriggerAbilityState("Sage"),
            content = ClocktowerSageStepContent(
                explanation = text("贤者被恶魔杀死时，得知恶魔是两名玩家之一。", "When killed by the Demon, the Sage learns that the Demon is one of two players."),
                displayTitle = text("贤者信息", "Sage information"),
                displayPrimary = text("恶魔", "Demon"),
                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。", "If the Demon killed the Sage tonight, wake the Sage and show only them two players, one of whom is the Demon."),
            ),
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper")),
            build = {
                informationStepBuilder.build(
                    roleName = "守鸦人", enName = "Ravenkeeper", tellPlayer = null,
                    actorOverride = requireNotNull(cardAt(state.currentNightDeathTriggerSeat("Ravenkeeper"))),
                    abilityStateOverride = state.deathTriggerAbilityState("Ravenkeeper"),
                    action = ClocktowerNightAction.Ravenkeeper,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = ravenkeeperTargetSeat?.let { ClocktowerInformationQuestion.Role(it) },
                )
            },
        ),

        )
}
