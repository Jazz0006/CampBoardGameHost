package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity

internal fun clocktowerEmpathStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    subjectSeats: List<Int>,
    text: (String, String) -> String,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Empath")),
    build = { builder.build(
        roleName = "共情者", enName = "Empath", tellPlayer = null,
        explanation = text("输入实际向共情者展示的数字。", "Enter the number actually shown to the Empath."),
        informationQuestion = ClocktowerInformationQuestion.Number(NumericMetric.LIVING_EVIL_NEIGHBOURS, subjectSeats),
    ) },
)

internal fun clocktowerFortuneTellerStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    subjectSeats: List<Int>,
    text: (String, String) -> String,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller")),
    build = { builder.build(
        roleName = "占卜师", enName = "Fortune Teller", tellPlayer = null,
        explanation = text("记录实际查询的两名玩家及展示结果。", "Record the two queried players and the actual shown result."),
        action = ClocktowerNightAction.FortuneTeller,
        informationQuestion = subjectSeats.takeIf { it.size == 2 && it.distinct().size == 2 }?.let {
            ClocktowerInformationQuestion.Boolean(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, it)
        },
    ) },
)

/** Stateless shared action presentation; only Session chooses which entries are used. */
internal fun clocktowerCommonNightMaterializers(
    informationStepBuilder: ClocktowerInformationStepBuilder,
    cards: List<PlayerCard>,
    text: (String, String) -> String,
    poisonTarget: String?,
    butlerMaster: String?,
    spySeatLabel: String?,
): List<ClocktowerNightStepMaterializerRegistry.Entry> {
    return listOf(
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
    )

}

/** First-night display construction, independent of flow confirmation and D2 preview state. */
internal fun clocktowerFirstNightMaterializers(
    informationStepBuilder: ClocktowerInformationStepBuilder,
    cards: List<PlayerCard>,
    text: (String, String) -> String,
    firstNightEvilInformationSteps: ClocktowerFirstNightEvilInformationSteps,
    redHerring: String?,
    /** Session fact: an actual Fortune Teller is seated. */
    fortuneTellerInPlay: Boolean,
): List<ClocktowerNightStepMaterializerRegistry.Entry> {
    fun pair(roleName: String, enName: String) = ClocktowerNightStepMaterializerRegistry.Entry(
        identity = ClocktowerProductionNightStepIdentity.role(RoleId(enName)),
        build = { informationStepBuilder.build(
            roleName = roleName, enName = enName, tellPlayer = null,
            explanation = text("选择实际展示的角色和两名玩家，或缺席结果。", "Choose the actually shown role and two players, or the absence result."),
        ) },
    )
    return listOf(
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
                                isRealAction = fortuneTellerInPlay,
                                reason = if (!fortuneTellerInPlay) text("本局没有占卜师，此步骤只用于首夜配置。", "No Fortune Teller is in play; this is only a first-night setup step.") else "",
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
        pair("洗衣妇", "Washerwoman"),
        pair("图书管理员", "Librarian"),
        pair("调查员", "Investigator"),
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


            )
}
