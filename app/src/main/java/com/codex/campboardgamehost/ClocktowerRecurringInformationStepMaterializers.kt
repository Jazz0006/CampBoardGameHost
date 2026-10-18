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
