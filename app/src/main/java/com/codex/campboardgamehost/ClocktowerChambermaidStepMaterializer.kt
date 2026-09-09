package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity

internal data class ClocktowerChambermaidStepContent(
    val explanation: String,
    val displayFooter: String,
    val hostInstruction: String,
)

internal fun ClocktowerInformationStepBuilder.buildChambermaidStep(
    content: ClocktowerChambermaidStepContent,
    result: String?,
    presentation: ClocktowerChambermaidSelectionPresentation,
    displayProposition: InformationProposition.NumericResult?,
    displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption>,
): ClocktowerNightStepUi = build(
    roleName = "侍女",
    enName = "Chambermaid",
    tellPlayer = result,
    explanation = content.explanation,
    action = ClocktowerNightAction.Chambermaid,
    displayProposition = displayProposition,
    displaySecondary = presentation.displaySecondary,
    displayFooter = content.displayFooter,
    hostInstruction = content.hostInstruction,
    displayOptions = displayOptions,
)

internal fun clocktowerChambermaidStepMaterializer(
    builder: ClocktowerInformationStepBuilder,
    content: ClocktowerChambermaidStepContent,
    result: String?,
    presentation: ClocktowerChambermaidSelectionPresentation,
    displayProposition: InformationProposition.NumericResult?,
    displayOptions: (PlayerCard) -> List<ClocktowerDisplayOption>,
): ClocktowerNightStepMaterializerRegistry.Entry = ClocktowerNightStepMaterializerRegistry.Entry(
    identity = ClocktowerProductionNightStepIdentity.role(RoleId("Chambermaid")),
    build = {
        builder.buildChambermaidStep(
            content = content,
            result = result,
            presentation = presentation,
            displayProposition = displayProposition,
            displayOptions = displayOptions,
        )
    },
)
