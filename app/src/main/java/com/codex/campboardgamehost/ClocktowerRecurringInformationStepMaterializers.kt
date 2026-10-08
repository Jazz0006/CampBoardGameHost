package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity

internal data class ClocktowerEmpathStepContent(
    val result: String,
    val registrationHint: String?,
    val previousShownNumber: Int?,
    val spyRegistrationKey: String?,
    val recluseRegistrationKey: String?,
)

internal fun clocktowerEmpathStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    content: ClocktowerEmpathStepContent,
    text: (String, String) -> String,
    displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption>,
    legalSelectionOptions: (PlayerCard) -> List<ClocktowerDisplayOption>,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Empath")),
    build = {
        builder.build(
            roleName = "共情者",
            enName = "Empath",
            tellPlayer = content.result,
            explanation = listOfNotNull(
                text("这个数字表示共情者两个存活邻居中有几个邪恶玩家。", "This number is how many of the Empath's living neighbors are evil."),
                content.registrationHint,
            ).joinToString("\n"),
            hostInstruction = text("轻拍共情者，示意睁眼。把数字只给他看；不要解释是哪位邻居。", "Tap the Empath to wake them. Show only the number; do not identify either neighbor."),
            displayOptions = displayOptions,
            previousShownNumber = content.previousShownNumber,
            legalSelectionOptions = legalSelectionOptions,
            spyRegistrationKey = content.spyRegistrationKey,
            spyRegistrationTeams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
            spyRegistrationDetail = ClocktowerRegistrationDetail.AlignmentOnly,
            spyRegistrationHint = content.registrationHint,
            recluseRegistrationKey = content.recluseRegistrationKey,
        )
    },
)

internal data class ClocktowerFortuneTellerStepContent(
    val result: String?,
    val matched: Boolean?,
    val selectedNames: List<String>,
    val displaySecondary: String?,
    val proposition: InformationProposition.BooleanResult?,
    val recluseRegistrationKey: String?,
)

internal fun clocktowerFortuneTellerStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    content: ClocktowerFortuneTellerStepContent,
    cards: List<PlayerCard>,
    text: (String, String) -> String,
    legalSelectionOptions: (PlayerCard) -> List<ClocktowerDisplayOption>,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Fortune Teller")),
    build = {
        builder.build(
            roleName = "占卜师",
            enName = "Fortune Teller",
            tellPlayer = content.result,
            explanation = text("如果两名玩家中包含恶魔或红鲱鱼，向占卜师展示“有”；否则展示“没有”。", "Show Yes if either selected player is the Demon or red herring; otherwise show No."),
            action = ClocktowerNightAction.FortuneTeller,
            displayProposition = content.proposition,
            displaySecondary = content.displaySecondary,
            displayFooter = text("查询这两名玩家", "Checking these two players"),
            hostInstruction = text("轻拍占卜师，示意睁眼。让他依次指两名玩家，在下面记录；结果出现后展示“有”或“没有”。", "Tap the Fortune Teller to wake them. Have them point to two players, record both, then show Yes or No."),
            displayOptions = { actor ->
                content.matched?.let { matched ->
                    ClocktowerNeutralInformationPreparation.yesNoOptions(
                        title = text("占卜师信息", "Fortune Teller information"),
                        truthfulYes = matched,
                        secondary = content.displaySecondary,
                        footer = text("查询这两名玩家", "Checking these two players"),
                        text = text,
                        propositionForValue = { value ->
                            InformationProposition.BooleanResult(
                                com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                                cards.indexOf(actor) + 1,
                                content.selectedNames.mapNotNull { name ->
                                    cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
                                },
                                value,
                            )
                        },
                    )
                }.orEmpty()
            },
            legalSelectionOptions = legalSelectionOptions,
            recluseRegistrationKey = content.recluseRegistrationKey,
            recluseRegistrationTeams = listOf(ClocktowerTeam.Demon),
        )
    },
)
