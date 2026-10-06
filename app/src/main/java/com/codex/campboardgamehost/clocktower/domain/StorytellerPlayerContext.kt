package com.codex.campboardgamehost.clocktower.domain

/**
 * Player skill used as Storyteller recommendation enrichment.
 *
 * NORMAL is the product default when no player-profile value has been supplied yet.
 */
internal enum class PlayerExperienceLevelV1 {
    BEGINNER,
    NORMAL,
    EXPERT,
}

/**
 * Optional Storyteller-entered pressure calibration for the current game.
 *
 * This is intentionally distinct from PlayerInformationPressure, which is derived from historical
 * events. These qualitative labels carry no built-in numeric policy weight.
 */
internal enum class StorytellerDeclaredPressureLevelV1 {
    LOW,
    MEDIUM,
    HIGH,
}

/**
 * Host-owned, per-seat Storyteller input for recommendation context.
 *
 * The session may store only non-default overrides; consumers must treat absence as this default.
 */
internal data class StorytellerPlayerContextInputV1(
    val experienceLevel: PlayerExperienceLevelV1 = PlayerExperienceLevelV1.NORMAL,
    val claimedRoleIds: List<RoleId> = emptyList(),
    val pressureLevel: StorytellerDeclaredPressureLevelV1? = null,
) {
    init {
        require(claimedRoleIds.distinct().size == claimedRoleIds.size) {
            "Player claimed roles must be unique while preserving Storyteller-entered order."
        }
    }

    val isDefault: Boolean
        get() = this == DEFAULT

    companion object {
        val DEFAULT = StorytellerPlayerContextInputV1()
    }
}
