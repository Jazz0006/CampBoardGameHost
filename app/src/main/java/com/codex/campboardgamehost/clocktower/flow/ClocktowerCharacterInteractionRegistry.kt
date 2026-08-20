package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.domain.RoleId

internal interface ClocktowerCharacterInteractionHandler {
    val roleId: RoleId

    fun beforeRoleInteractions(phase: ClocktowerNightFlowPhase): List<ClocktowerHostInteraction> = emptyList()
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

    fun beforeRoleInteractions(
        roleId: RoleId,
        phase: ClocktowerNightFlowPhase,
    ): List<ClocktowerHostInteraction> = byRoleId[roleId]?.beforeRoleInteractions(phase).orEmpty()

    companion object {
        fun builtIn(): ClocktowerCharacterInteractionRegistry = ClocktowerCharacterInteractionRegistry(
            listOf(FortuneTellerInteractionHandler),
        )
    }
}

private object FortuneTellerInteractionHandler : ClocktowerCharacterInteractionHandler {
    override val roleId: RoleId = RoleId("Fortune Teller")

    override fun beforeRoleInteractions(phase: ClocktowerNightFlowPhase): List<ClocktowerHostInteraction> =
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
