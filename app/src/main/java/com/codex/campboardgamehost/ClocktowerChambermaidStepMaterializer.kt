package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity

internal data class ClocktowerChambermaidStepContent(
    val explanation: String,
    val displayFooter: String,
    val hostInstruction: String,
)

internal fun ClocktowerInformationStepBuilder.buildChambermaidStep(
    content: ClocktowerChambermaidStepContent,
    presentation: ClocktowerChambermaidSelectionPresentation,
): ClocktowerNightStepUi = build(
    roleName = "侍女",
    enName = "Chambermaid",
    tellPlayer = null,
    explanation = content.explanation,
    action = ClocktowerNightAction.Chambermaid,
    informationQuestion = presentation.subjectSeats.takeIf { it.size == 2 && it.distinct().size == 2 }?.let {
        ClocktowerInformationQuestion.Number(com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.PLAYERS_WAKING_FOR_ABILITY, it)
    },
    displaySecondary = presentation.displaySecondary,
    displayFooter = content.displayFooter,
    hostInstruction = content.hostInstruction,

)

internal fun clocktowerChambermaidStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    content: ClocktowerChambermaidStepContent,
    presentation: ClocktowerChambermaidSelectionPresentation,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Chambermaid")),
    build = {
        builder.buildChambermaidStep(
            content = content,
            presentation = presentation,
        )
    },
)
