package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1

internal data class ClocktowerPlayerContextEditState(
    val experienceLevel: PlayerExperienceLevelV1,
    val claimedRoleIds: List<RoleId>,
    val pressureLevel: StorytellerDeclaredPressureLevelV1?,
) {
    init {
        require(claimedRoleIds.distinct().size == claimedRoleIds.size) {
            "Edited claimed roles must be unique."
        }
    }

    fun toggleClaim(roleId: RoleId): ClocktowerPlayerContextEditState =
        copy(
            claimedRoleIds = if (roleId in claimedRoleIds) {
                claimedRoleIds - roleId
            } else {
                claimedRoleIds + roleId
            },
        )

    fun toInput(): StorytellerPlayerContextInputV1 =
        StorytellerPlayerContextInputV1(
            experienceLevel = experienceLevel,
            claimedRoleIds = claimedRoleIds,
            pressureLevel = pressureLevel,
        )

    companion object {
        fun from(input: StorytellerPlayerContextInputV1): ClocktowerPlayerContextEditState =
            ClocktowerPlayerContextEditState(
                experienceLevel = input.experienceLevel,
                claimedRoleIds = input.claimedRoleIds,
                pressureLevel = input.pressureLevel,
            )
    }
}
