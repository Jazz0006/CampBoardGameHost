package com.codex.campboardgamehost

import androidx.compose.runtime.Composable
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.session.*

/**
 * Disposable display assembly. Session owns order, wake/skip and facts ([ClocktowerNightFacts]);
 * the current draft's seats come routed through [ClocktowerNightDraftSubjects]. This maps seats
 * to cards and labels only.
 */
@Composable
internal fun clocktowerNightPresentation(
    facts: ClocktowerNightFacts,
    subjects: ClocktowerNightDraftSubjects,
    script: ClocktowerScript,
    cards: List<PlayerCard>,
    recommendedDemonBluffRoleNames: List<String>,
    language: String,
): List<ClocktowerNightStepUi> {
    fun text(zh: String, en: String) = if (language == "en") en else zh
    fun cardAt(seat: Int?): PlayerCard? = seat?.let { cards.getOrNull(it - 1) }
    fun nameAt(seat: Int?): String? = cardAt(seat)?.name
    val redHerring = nameAt(subjects.redHerringSeat ?: facts.redHerringSeat)
    val demonCard = cardAt(facts.livingDemonSeat)
    fun abilityStateFor(enName: String, actor: PlayerCard?): AbilityFunctioningState? =
        facts.abilityState(enName, cards.seatOf(actor?.name))
    fun roleActor(enName: String): PlayerCard? = cardAt(facts.wakingSeat(enName))
    fun roleMissingReason(enName: String): String {
        val status = facts.roleStatus(enName)
        val notInPlay = text("本局没有这个角色。", "This character is not in play.")
        val roleCard = cardAt(status.seat)
        return when {
            status.availability == ClocktowerRoleAvailability.DRUNK_SHOWN_ONLY -> ""
            status.availability == ClocktowerRoleAvailability.NOT_IN_PLAY || roleCard == null -> notInPlay
            status.availability == ClocktowerRoleAvailability.DEAD -> text(
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

    // Evil-team introductions always use true identities. Registration choices
    // for the Spy/Recluse only affect abilities that explicitly allow them.
    val minionCards = facts.minionSeats.mapNotNull(::cardAt)
    val legalDemonBluffs = clocktowerRolesForScript(script).distinctBy { it.enName }
    val demonBluffPresentation = resolveDemonBluffPresentation(
        recommendedRoleNames = recommendedDemonBluffRoleNames.takeIf { it.isNotEmpty() },
        legalRoles = legalDemonBluffs,
    )
    val firstNightEvilInformationSteps = clocktowerFirstNightEvilInformationSteps(
        minionActor = minionCards.firstOrNull(),
        demonActor = demonCard,
        minionSeatLabels = minionCards.map { it.seatLabel(cards) },
        demonSeatLabel = demonCard?.seatLabel(cards),
        shouldGiveInformation = facts.evilInformationRequired,
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
        presentation = clocktowerChambermaidSelectionPresentation(cards, subjects.chambermaid),
    )
    val empathStepMaterializer = clocktowerEmpathStepMaterializer(
        informationStepBuilder, facts.empathNeighbourSeats, ::text,
    )
    val fortuneTellerStepMaterializer = clocktowerFortuneTellerStepMaterializer(
        informationStepBuilder, subjects.fortuneTellerSeats, ::text,
    )

    val commonRoleMaterializers = clocktowerCommonNightMaterializers(
        informationStepBuilder, cards, ::text, nameAt(facts.confirmedPoisonTargetSeat), nameAt(subjects.butlerMasterSeat),
        cardAt(facts.spySeat)?.seatLabel(cards),
    ) + listOf(empathStepMaterializer, chambermaidMaterializer, fortuneTellerStepMaterializer)

    return if (facts.phase == StorytellerPhase.FIRST_NIGHT) {
        ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.FIRST_NIGHT,
            entries = commonRoleMaterializers + clocktowerFirstNightMaterializers(
                informationStepBuilder, cards, ::text, firstNightEvilInformationSteps, redHerring,
                fortuneTellerInPlay = facts.fortuneTellerInPlay,
            ),
        ).materialize(facts.interactions)
    } else {
        ClocktowerNightStepMaterializerRegistry(
            phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
            entries = commonRoleMaterializers + otherNightDisplayEntries(
                cards, facts, informationStepBuilder, ::text, subjects.ravenkeeperSeat,
            ),
        ).materialize(facts.interactions)
    }
}

/** Later-night step builders: display only; facts come from [ClocktowerNightFacts]. */
private fun otherNightDisplayEntries(
    cards: List<PlayerCard>,
    facts: ClocktowerNightFacts,
    informationStepBuilder: ClocktowerInformationStepBuilder,
    text: (String, String) -> String,
    ravenkeeperTargetSeat: Int?,
): List<ClocktowerNightStepMaterializerRegistry.Entry> {
    fun cardAt(seat: Int?) = seat?.let { cards.getOrNull(it - 1) }
    val demonCard = cardAt(facts.livingDemonSeat)
    val demonPoisonedForActionExplanation = facts.livingDemonPoisoned
    val mayorTarget = cardAt(facts.mayorRulingSeat)
    val undertakerTargetSeat = facts.lastExecutedSeat
    val monkProtectedTarget = cardAt(facts.monkSeat)?.name
    val pendingNightDeath = cardAt(facts.attackSeat)?.name
    val mayorRedirectTarget = cardAt(facts.mayorRedirectSeat)?.name
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
            triggerActor = cardAt(facts.deathTriggerSeat("Sage")),
            abilityState = facts.deathTriggerAbilityState("Sage"),
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
                    actorOverride = requireNotNull(cardAt(facts.deathTriggerSeat("Ravenkeeper"))),
                    abilityStateOverride = facts.deathTriggerAbilityState("Ravenkeeper"),
                    action = ClocktowerNightAction.Ravenkeeper,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = ravenkeeperTargetSeat?.let { ClocktowerInformationQuestion.Role(it) },
                )
            },
        ),

        )
}
