package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken

/**
 * Converts the pure base night-token plan into stable flow interactions without touching Compose.
 * Role-specific setup hooks live in ClocktowerCharacterInteractionRegistry instead of script-name
 * or UI-level ordering branches.
 */
internal class ClocktowerHostInteractionProjector(
    private val registry: ClocktowerCharacterInteractionRegistry = ClocktowerCharacterInteractionRegistry.builtIn(),
) {
    fun projectNight(
        phase: ClocktowerNightFlowPhase,
        basePlan: List<NightOrderToken>,
    ): List<ClocktowerHostInteraction> {
        val interactions = buildList {
            basePlan.forEach { token ->
                when (token) {
                    is NightOrderToken.Character -> {
                        addAll(registry.beforeRoleInteractions(token.roleId, phase))
                        add(roleInteraction(phase, token))
                    }
                    NightOrderToken.System.DUSK -> add(
                        systemInteraction(
                            phase = phase,
                            suffix = "dusk",
                            kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY,
                            completion = ClocktowerInteractionCompletionPolicy.SYSTEM_TRANSITION,
                        ),
                    )
                    NightOrderToken.System.MINION_INFO -> add(
                        systemInteraction(
                            phase = phase,
                            suffix = "minion_info",
                            kind = ClocktowerHostInteractionKind.EVIL_INFORMATION,
                            completion = ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY,
                        ),
                    )
                    NightOrderToken.System.DEMON_INFO -> add(
                        systemInteraction(
                            phase = phase,
                            suffix = "demon_info",
                            kind = ClocktowerHostInteractionKind.EVIL_INFORMATION,
                            completion = ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY,
                        ),
                    )
                    NightOrderToken.System.DAWN -> add(
                        systemInteraction(
                            phase = phase,
                            suffix = "dawn",
                            kind = ClocktowerHostInteractionKind.SYSTEM_BOUNDARY,
                            completion = ClocktowerInteractionCompletionPolicy.SYSTEM_TRANSITION,
                        ),
                    )
                }
            }
        }
        require(interactions.map { it.id }.distinct().size == interactions.size) {
            "Clocktower projected interaction IDs must be unique."
        }
        return interactions
    }

    private fun roleInteraction(
        phase: ClocktowerNightFlowPhase,
        token: NightOrderToken.Character,
    ): ClocktowerHostInteraction = ClocktowerHostInteraction(
        id = ClocktowerInteractionId("${phase.idPrefix()}:role:${token.roleId.value}"),
        phase = phase,
        roleId = token.roleId,
        kind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION,
        completionPolicy = ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION,
    )

    private fun systemInteraction(
        phase: ClocktowerNightFlowPhase,
        suffix: String,
        kind: ClocktowerHostInteractionKind,
        completion: ClocktowerInteractionCompletionPolicy,
    ): ClocktowerHostInteraction = ClocktowerHostInteraction(
        id = ClocktowerInteractionId("${phase.idPrefix()}:system:$suffix"),
        phase = phase,
        roleId = null,
        kind = kind,
        completionPolicy = completion,
    )

    private fun ClocktowerNightFlowPhase.idPrefix(): String = when (this) {
        ClocktowerNightFlowPhase.FIRST_NIGHT -> "first_night"
        ClocktowerNightFlowPhase.OTHER_NIGHT -> "other_night"
    }
}
