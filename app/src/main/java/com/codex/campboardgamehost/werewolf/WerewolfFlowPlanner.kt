package com.codex.campboardgamehost

@JvmInline
internal value class WerewolfInteractionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Werewolf interaction id cannot be blank." }
    }
}

internal data class WerewolfHostInteraction(
    val id: WerewolfInteractionId,
    val roleId: WerewolfRoleId?,
    val legacyStep: WerewolfJudgeStep,
    val kind: WerewolfInteractionKind,
    val completionPolicy: WerewolfInteractionCompletionPolicy,
)

/**
 * Pure R5.5 shadow planner for the current Werewolf board cycle.
 *
 * Role-specific wake/action metadata comes from WerewolfRoleRegistry. This planner knows only how
 * to order eligible registered role interactions and append the two legacy system/day boundaries.
 * Production WerewolfJudgeScreen continues to build its own legacy step list during S3 validation.
 */
internal class WerewolfFlowPlanner {
    fun plan(
        board: WerewolfBoardDefinition,
        roleRegistry: WerewolfRoleRegistry,
    ): List<WerewolfHostInteraction> {
        val roleInteractions = board.roleDeck.keys
            .map { roleId ->
                val definition = roleRegistry.require(roleId)
                definition.interaction?.let { interaction -> definition to interaction }
            }
            .filterNotNull()
            .sortedWith(
                compareBy<Pair<WerewolfRoleDefinition, WerewolfRoleInteractionDefinition>> { it.second.order }
                    .thenBy { it.first.id.value },
            )
            .map { (definition, interaction) ->
                WerewolfHostInteraction(
                    id = WerewolfInteractionId("cycle:role:${definition.id.value}"),
                    roleId = definition.id,
                    legacyStep = interaction.legacyStep,
                    kind = interaction.kind,
                    completionPolicy = interaction.completionPolicy,
                )
            }

        val result = roleInteractions + listOf(
            WerewolfHostInteraction(
                id = WerewolfInteractionId("cycle:system:dawn"),
                roleId = null,
                legacyStep = WerewolfJudgeStep.Dawn,
                kind = WerewolfInteractionKind.SYSTEM_TRANSITION,
                completionPolicy = WerewolfInteractionCompletionPolicy.SYSTEM_CONFIRMATION,
            ),
            WerewolfHostInteraction(
                id = WerewolfInteractionId("cycle:day:vote"),
                roleId = null,
                legacyStep = WerewolfJudgeStep.DayVote,
                kind = WerewolfInteractionKind.DAY_ACTION,
                completionPolicy = WerewolfInteractionCompletionPolicy.DAY_RESOLUTION,
            ),
        )
        require(result.map { it.id }.distinct().size == result.size) {
            "Werewolf projected interaction IDs must be unique."
        }
        return result
    }
}
