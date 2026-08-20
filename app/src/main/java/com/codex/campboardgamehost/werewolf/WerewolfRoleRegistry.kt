package com.codex.campboardgamehost

@JvmInline
internal value class WerewolfRoleId(val value: String) {
    init {
        require(ID_PATTERN.matches(value)) { "Werewolf role id must be lowercase alphanumeric/underscore." }
    }

    private companion object {
        val ID_PATTERN = Regex("[a-z0-9_]{1,64}")
    }
}

internal object WerewolfRoleIds {
    val VILLAGER = WerewolfRoleId("villager")
    val WEREWOLF = WerewolfRoleId("werewolf")
    val SEER = WerewolfRoleId("seer")
    val WITCH = WerewolfRoleId("witch")
    val HUNTER = WerewolfRoleId("hunter")
}

internal enum class WerewolfTeam {
    VILLAGE,
    WEREWOLF,
}

internal enum class WerewolfInteractionKind {
    ROLE_ACTION,
    ROLE_STATUS,
    SYSTEM_TRANSITION,
    DAY_ACTION,
}

internal enum class WerewolfWakePolicy {
    ACTIVE_ROLE,
    STATUS_ONLY,
}

internal enum class WerewolfInteractionCompletionPolicy {
    ROLE_RESOLUTION,
    STATUS_ACKNOWLEDGEMENT,
    SYSTEM_CONFIRMATION,
    DAY_RESOLUTION,
}

internal data class WerewolfRoleInteractionDefinition(
    val legacyStep: WerewolfJudgeStep,
    val order: Int,
    val kind: WerewolfInteractionKind,
    val wakePolicy: WerewolfWakePolicy,
    val completionPolicy: WerewolfInteractionCompletionPolicy,
) {
    init {
        require(order > 0) { "Werewolf role interaction order must be positive." }
    }
}

internal data class WerewolfRoleDefinition(
    val id: WerewolfRoleId,
    val legacyRole: Role,
    val team: WerewolfTeam,
    val behaviorKey: String,
    val interaction: WerewolfRoleInteractionDefinition? = null,
) {
    init {
        require(behaviorKey.isNotBlank()) { "Werewolf behaviorKey cannot be blank." }
    }
}

internal class WerewolfRoleRegistry private constructor(
    val definitions: List<WerewolfRoleDefinition>,
) {
    private val byId = definitions.associateBy { it.id }
    private val byLegacyRole = definitions.associateBy { it.legacyRole }

    init {
        kotlin.require(definitions.isNotEmpty()) { "Werewolf role registry cannot be empty." }
        kotlin.require(byId.size == definitions.size) { "Werewolf role ids must be unique." }
        kotlin.require(byLegacyRole.size == definitions.size) { "Werewolf legacy role bindings must be unique." }
    }

    fun find(roleId: WerewolfRoleId): WerewolfRoleDefinition? = byId[roleId]

    fun require(roleId: WerewolfRoleId): WerewolfRoleDefinition =
        find(roleId) ?: error("Unknown Werewolf role id '${roleId.value}'.")

    fun roleIdFor(legacyRole: Role): WerewolfRoleId? = byLegacyRole[legacyRole]?.id

    companion object {
        fun builtIn(): WerewolfRoleRegistry = WerewolfRoleRegistry(
            listOf(
                WerewolfRoleDefinition(
                    id = WerewolfRoleIds.VILLAGER,
                    legacyRole = Role.Villager,
                    team = WerewolfTeam.VILLAGE,
                    behaviorKey = "villager",
                ),
                WerewolfRoleDefinition(
                    id = WerewolfRoleIds.WEREWOLF,
                    legacyRole = Role.Werewolf,
                    team = WerewolfTeam.WEREWOLF,
                    behaviorKey = "werewolf",
                    interaction = WerewolfRoleInteractionDefinition(
                        legacyStep = WerewolfJudgeStep.Wolves,
                        order = 10,
                        kind = WerewolfInteractionKind.ROLE_ACTION,
                        wakePolicy = WerewolfWakePolicy.ACTIVE_ROLE,
                        completionPolicy = WerewolfInteractionCompletionPolicy.ROLE_RESOLUTION,
                    ),
                ),
                WerewolfRoleDefinition(
                    id = WerewolfRoleIds.SEER,
                    legacyRole = Role.Seer,
                    team = WerewolfTeam.VILLAGE,
                    behaviorKey = "seer",
                    interaction = WerewolfRoleInteractionDefinition(
                        legacyStep = WerewolfJudgeStep.Seer,
                        order = 20,
                        kind = WerewolfInteractionKind.ROLE_ACTION,
                        wakePolicy = WerewolfWakePolicy.ACTIVE_ROLE,
                        completionPolicy = WerewolfInteractionCompletionPolicy.ROLE_RESOLUTION,
                    ),
                ),
                WerewolfRoleDefinition(
                    id = WerewolfRoleIds.WITCH,
                    legacyRole = Role.Witch,
                    team = WerewolfTeam.VILLAGE,
                    behaviorKey = "witch",
                    interaction = WerewolfRoleInteractionDefinition(
                        legacyStep = WerewolfJudgeStep.Witch,
                        order = 30,
                        kind = WerewolfInteractionKind.ROLE_ACTION,
                        wakePolicy = WerewolfWakePolicy.ACTIVE_ROLE,
                        completionPolicy = WerewolfInteractionCompletionPolicy.ROLE_RESOLUTION,
                    ),
                ),
                WerewolfRoleDefinition(
                    id = WerewolfRoleIds.HUNTER,
                    legacyRole = Role.Hunter,
                    team = WerewolfTeam.VILLAGE,
                    behaviorKey = "hunter",
                    interaction = WerewolfRoleInteractionDefinition(
                        legacyStep = WerewolfJudgeStep.Hunter,
                        order = 40,
                        kind = WerewolfInteractionKind.ROLE_STATUS,
                        wakePolicy = WerewolfWakePolicy.STATUS_ONLY,
                        completionPolicy = WerewolfInteractionCompletionPolicy.STATUS_ACKNOWLEDGEMENT,
                    ),
                ),
            ),
        )
    }
}
