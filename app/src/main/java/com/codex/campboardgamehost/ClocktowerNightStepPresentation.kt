package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.flow.*
import com.codex.campboardgamehost.clocktower.rules.*
import com.codex.campboardgamehost.clocktower.session.*

/** Read-only role content for Session-selected interactions. No cursor, writer or completion state. */
internal class ClocktowerNightStepPresentation(
    private val cards: List<PlayerCard>,
    private val sessionState: ClocktowerSessionState,
    private val nightInteractions: List<ClocktowerHostInteraction>,
    private val script: ClocktowerScript,
    private val language: String,
    fortuneTellerFirst: String?,
    fortuneTellerSecond: String?,
    chambermaidFirst: String?,
    chambermaidSecond: String?,
    private val ravenkeeperTarget: String?,
    private val redHerring: String?,
    private val butlerMaster: String?,
    private val recommendedDemonBluffRoleNames: List<String>,
) {
    private val sessionGameState = sessionState.gameState
    private val phase = if(sessionState.phase == StorytellerPhase.FIRST_NIGHT) ClocktowerPhase.FirstNight else ClocktowerPhase.Night
    private val poisonTarget = sessionState.confirmedPoisonTargetName(sessionState.round)
    private val monkProtectedTarget = sessionState.nightTargets.monkSeat?.let(sessionGameState::playerAt)?.name
    private val mayorRedirectTarget = sessionState.nightTargets.mayorRedirectSeat?.let(sessionGameState::playerAt)?.name
    private val pendingNightDeath = sessionState.nightTargets.attackSeat?.let(sessionGameState::playerAt)?.name
    private val lastExecutedName = sessionState.actionTimeline.entries.asReversed().firstNotNullOfOrNull {
        (it.fact as? ActionFact.Execution)?.targetSeat
    }?.let(sessionGameState::playerAt)?.name
    private val legalDemonBluffs = clocktowerRolesForScript(script).distinctBy { it.enName }
    private fun text(zh: String, en: String) = if(language == "en") en else zh
    val spySeatLabel = cards.firstOrNull { it.clocktowerRole?.enName == "Spy" }?.seatLabel(cards)
    val nightHostProjection = ClocktowerNightHostProjection(cards, sessionState)
    val mayorTarget = nightHostProjection.mayorTarget
    val mayorRedirectTargetCards = nightHostProjection.mayorRedirectTargetCards
    val ravenkeeperTrigger = nightHostProjection.ravenkeeperTrigger
    val demonCard = nightHostProjection.demonCard
    val demonPoisonedForActionExplanation = nightHostProjection.demonPoisonedForActionExplanation
    val sageNightDeath = nightHostProjection.sageNightDeath
    val chambermaidTargetCards = nightHostProjection.chambermaidTargetCards
    val ravenkeeperDeathTriggerAbilityState = nightHostProjection.ravenkeeperDeathTriggerAbilityState
    val sageDeathTriggerAbilityState = nightHostProjection.sageDeathTriggerAbilityState

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

    @Composable
    fun steps(): List<ClocktowerNightStepUi> {
    val undertakerTarget = lastExecutedName?.let { name -> cards.firstOrNull { it.name == name } }
    val ravenkeeperTargetCard = ravenkeeperTarget?.let { name -> cards.firstOrNull { it.name == name } }
    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val minionCards = cards.filter { it.clocktowerRole?.team == ClocktowerTeam.Minion }
    val shouldGiveFirstNightEvilInfo = cards.size >= 7
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

    val commonRoleMaterializers = listOf(
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
                    tellPlayer = null,
                    explanation = text("让间谍查看当前说书人方桌；收回手机后点击下一步。", "Let the Spy view the current Storyteller table, then take back the phone and tap Next."),
                    displayKind = ClocktowerDisplayKind.None,
                    hostInstruction = text("唤醒 ${spySeatLabel ?: "间谍"}，让他查看当前方桌；收回手机，示意闭眼，然后点击下一步。", "Wake ${spySeatLabel ?: "the Spy"}, show them the current table, take back the phone, signal them to close their eyes, then tap Next."),
                )
            },
        ),
        empathStepMaterializer,
        chambermaidMaterializer,
        fortuneTellerStepMaterializer,
    )

    return if (phase == ClocktowerPhase.FirstNight) {
        val firstNightMaterializers = ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            entries = commonRoleMaterializers + listOf(
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.minionInfo(),
            build = { firstNightEvilInformationSteps.minion },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.demonInfo(),
            build = { firstNightEvilInformationSteps.demon },
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
                    roleName = "钟表匠", enName = "Clockmaker", tellPlayer = null,
                    explanation = text("输入实际展示的数字。", "Enter the actual shown number."),
                    informationQuestion = ClocktowerInformationQuestion.Number(NumericMetric.STEPS_TO_NEAREST_MINION, cards.indices.map { it + 1 }),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Washerwoman")),
            build = {
                informationStepBuilder.build(
                    roleName = "洗衣妇", enName = "Washerwoman", tellPlayer = null,
                    explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Librarian")),
            build = {
                informationStepBuilder.build(
                    roleName = "图书管理员", enName = "Librarian", tellPlayer = null,
                    explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Investigator")),
            build = {
                informationStepBuilder.build(
                    roleName = "调查员", enName = "Investigator", tellPlayer = null,
                    explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Chef")),
            build = {
                informationStepBuilder.build(
                    roleName = "厨师", enName = "Chef", tellPlayer = null,
                    explanation = text("输入实际展示的数字。", "Enter the actual shown number."),
                    informationQuestion = ClocktowerInformationQuestion.Number(NumericMetric.ADJACENT_EVIL_PAIRS, cards.indices.map { it + 1 }),
                )
            },
        ),


            ),
        )
        firstNightMaterializers.materialize(nightInteractions)
    } else {
    val otherNightMaterializers = ClocktowerNightStepMaterializerRegistry(
        phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
        entries = commonRoleMaterializers + listOf(

        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Undertaker")),
            build = {
                informationStepBuilder.build(
                    roleName = "送葬者", enName = "Undertaker", tellPlayer = null,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = undertakerTarget?.let { ClocktowerInformationQuestion.Role(cards.indexOf(it) + 1) },
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
            triggerActor = sageNightDeath,
            abilityState = sageDeathTriggerAbilityState,
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
                    actorOverride = requireNotNull(ravenkeeperTrigger), abilityStateOverride = ravenkeeperDeathTriggerAbilityState,
                    action = ClocktowerNightAction.Ravenkeeper,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = ravenkeeperTargetCard?.let { ClocktowerInformationQuestion.Role(cards.indexOf(it) + 1) },
                )
            },
        ),

        ),
    )
        otherNightMaterializers.materialize(
            nightInteractions,
        )
    }

    }
}
