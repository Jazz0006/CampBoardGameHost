package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Host-owned script night sequencing. Only roles and already-resolved event flags
 * determine wakeups; neither AI nor the player chooses the schedule.
 */
internal class ClocktowerHostInteractionProjector {
    fun projectNight(
        phase: ClocktowerNightFlowPhase,
        basePlan: List<NightOrderToken>,
        resolvedFacts: ClocktowerResolvedFlowFacts = ClocktowerResolvedFlowFacts.EMPTY,
        actualRoleIds: Set<RoleId>? = null,
    ): List<ClocktowerHostInteraction> {
        val actualRoles = actualRoleIds ?: basePlan
            .filterIsInstance<NightOrderToken.Character>()
            .mapTo(linkedSetOf()) { it.roleId }
        val interactions = buildList {
            basePlan.forEach { token ->
                when (token) {
                    is NightOrderToken.Character -> {
                        val roleId = token.roleId
                        val role = roleId.value
                        if (phase == ClocktowerNightFlowPhase.FIRST_NIGHT &&
                            role == "Fortune Teller" && roleId in actualRoles
                        ) {
                            add(ClocktowerHostInteraction(
                                id = ClocktowerInteractionId("first_night:fortune_teller:red_herring"),
                                phase = phase,
                                roleId = roleId,
                                kind = ClocktowerHostInteractionKind.STORYTELLER_SETUP,
                                completionPolicy = ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION,
                            ))
                        }
                        if (phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
                            role == "Imp" &&
                            ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON in resolvedFacts
                        ) {
                            add(event(phase, roleId, "other_night:event:imp:new_demon_identity",
                                ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY))
                        }
                        if (phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
                            role == "Mayor" &&
                            ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE in resolvedFacts
                        ) {
                            add(event(phase, roleId, "other_night:event:mayor:death_resolution",
                                ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION))
                        }
                        val eligible = when (role) {
                            "Scarlet Woman", "Mayor" -> false
                            "Ravenkeeper" -> phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
                                ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT in resolvedFacts
                            "Sage" -> phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
                                ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON in resolvedFacts
                            "Undertaker" -> phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
                                ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY in resolvedFacts
                            else -> true
                        }
                        if (eligible) add(ClocktowerHostInteraction(
                            id = ClocktowerInteractionId("${phase.idPrefix()}:role:$role"),
                            phase = phase,
                            roleId = roleId,
                            kind = ClocktowerHostInteractionKind.ROLE_PHASE_ACTION,
                            completionPolicy = ClocktowerInteractionCompletionPolicy.ROLE_RESOLUTION,
                        ))
                        if (phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
                            role == "Imp" &&
                            ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED in resolvedFacts
                        ) {
                            add(event(phase, roleId, "other_night:event:imp:demon_successor",
                                ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION))
                        }
                    }
                    NightOrderToken.System.DUSK -> add(system(phase, "dusk",
                        ClocktowerHostInteractionKind.SYSTEM_BOUNDARY,
                        ClocktowerInteractionCompletionPolicy.SYSTEM_TRANSITION))
                    NightOrderToken.System.MINION_INFO -> add(system(phase, "minion_info",
                        ClocktowerHostInteractionKind.EVIL_INFORMATION,
                        ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY))
                    NightOrderToken.System.DEMON_INFO -> add(system(phase, "demon_info",
                        ClocktowerHostInteractionKind.EVIL_INFORMATION,
                        ClocktowerInteractionCompletionPolicy.INFORMATION_DISPLAY))
                    // Script metadata includes a closing "dawn" token, but the Host
                    // simply finishes the final real role and transitions to Day.
                    NightOrderToken.System.DAWN -> Unit
                }
            }
        }
        require(interactions.map { it.id }.distinct().size == interactions.size) {
            "Clocktower projected interaction IDs must be unique."
        }
        return interactions
    }

    private fun event(
        phase: ClocktowerNightFlowPhase,
        roleId: RoleId,
        id: String,
        completion: ClocktowerInteractionCompletionPolicy,
    ) = ClocktowerHostInteraction(
        id = ClocktowerInteractionId(id),
        phase = phase,
        roleId = roleId,
        kind = ClocktowerHostInteractionKind.EVENT_RESOLUTION,
        completionPolicy = completion,
    )

    private fun system(
        phase: ClocktowerNightFlowPhase,
        suffix: String,
        kind: ClocktowerHostInteractionKind,
        completion: ClocktowerInteractionCompletionPolicy,
    ) = ClocktowerHostInteraction(
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

