package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId

internal interface ClocktowerCharacterInteractionHandler {
    val roleId: RoleId

    fun isRoleInteractionEligible(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean = true

    fun beforeRoleInteractions(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> = emptyList()

    fun afterRoleInteractions(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> = emptyList()
}

internal class ClocktowerCharacterInteractionRegistry(
    handlers: Iterable<ClocktowerCharacterInteractionHandler>,
) {
    private val byRoleId: Map<RoleId, ClocktowerCharacterInteractionHandler>

    init {
        val handlerList = handlers.toList()
        require(handlerList.map { it.roleId }.distinct().size == handlerList.size) {
            "Clocktower interaction handlers must have unique RoleIds."
        }
        byRoleId = handlerList.associateBy { it.roleId }
    }

    fun isRoleInteractionEligible(
        roleId: RoleId,
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean = byRoleId[roleId]?.isRoleInteractionEligible(phase, resolvedFacts) ?: true

    fun beforeRoleInteractions(
        roleId: RoleId,
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> =
        byRoleId[roleId]?.beforeRoleInteractions(phase, resolvedFacts).orEmpty()

    fun afterRoleInteractions(
        roleId: RoleId,
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> =
        byRoleId[roleId]?.afterRoleInteractions(phase, resolvedFacts).orEmpty()

    companion object {
        fun builtIn(): ClocktowerCharacterInteractionRegistry = ClocktowerCharacterInteractionRegistry(
            listOf(
                FortuneTellerInteractionHandler,
                ImpInteractionHandler,
                ScarletWomanInteractionHandler,
                MayorInteractionHandler,
                RavenkeeperInteractionHandler,
                SageInteractionHandler,
                UndertakerInteractionHandler,
            ),
        )
    }
}

private object FortuneTellerInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Fortune Teller")

    override fun beforeRoleInteractions(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> =
        if (phase == ClocktowerNightFlowPhase.FIRST_NIGHT) {
            listOf(
                ClocktowerHostInteraction(
                    id = ClocktowerInteractionId("first_night:fortune_teller:red_herring"),
                    phase = phase,
                    roleId = roleId,
                    kind = ClocktowerHostInteractionKind.STORYTELLER_SETUP,
                    completionPolicy = ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION,
                ),
            )
        } else {
            emptyList()
        }
}

private object ImpInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Imp")

    override fun afterRoleInteractions(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> =
        if (
            phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
            ClocktowerResolvedFlowFact.DEMON_SUCCESSION_REQUIRED in resolvedFacts
        ) {
            listOf(
                eventResolutionInteraction(
                    id = "other_night:event:imp:demon_successor",
                    phase = phase,
                    roleId = roleId,
                ),
            )
        } else {
            emptyList()
        }
}

/**
 * The Scarlet Woman token is normally only an ordering anchor. If the rules layer has already
 * resolved that she became the Demon during the day, the same token becomes the next-night
 * private role-change interaction. Imp self-kill succession remains an Imp after-interaction event.
 */
private object ScarletWomanInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Scarlet Woman")

    override fun isRoleInteractionEligible(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean =
        phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
            ClocktowerResolvedFlowFact.SCARLET_WOMAN_BECAME_DEMON in resolvedFacts
}

private object MayorInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Mayor")

    override fun isRoleInteractionEligible(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean = false

    override fun beforeRoleInteractions(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): List<ClocktowerHostInteraction> =
        if (
            phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
            ClocktowerResolvedFlowFact.MAYOR_REDIRECT_ELIGIBLE in resolvedFacts
        ) {
            listOf(
                eventResolutionInteraction(
                    id = "other_night:event:mayor:death_resolution",
                    phase = phase,
                    roleId = roleId,
                ),
            )
        } else {
            emptyList()
        }
}

private object RavenkeeperInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Ravenkeeper")

    override fun isRoleInteractionEligible(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean =
        phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
            ClocktowerResolvedFlowFact.RAVENKEEPER_DIED_AT_NIGHT in resolvedFacts
}

private object SageInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Sage")

    override fun isRoleInteractionEligible(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean =
        phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
            ClocktowerResolvedFlowFact.SAGE_KILLED_BY_DEMON in resolvedFacts
}

private object UndertakerInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Undertaker")

    override fun isRoleInteractionEligible(
        phase: ClocktowerNightFlowPhase,
        resolvedFacts: ClocktowerResolvedFlowFacts,
    ): Boolean =
        phase == ClocktowerNightFlowPhase.OTHER_NIGHT &&
            ClocktowerResolvedFlowFact.EXECUTION_OCCURRED_TODAY in resolvedFacts
}

private fun eventResolutionInteraction(
    id: String,
    phase: ClocktowerNightFlowPhase,
    roleId: RoleId,
): ClocktowerHostInteraction = ClocktowerHostInteraction(
    id = ClocktowerInteractionId(id),
    phase = phase,
    roleId = roleId,
    kind = ClocktowerHostInteractionKind.EVENT_RESOLUTION,
    completionPolicy = ClocktowerInteractionCompletionPolicy.STORYTELLER_SELECTION,
)
