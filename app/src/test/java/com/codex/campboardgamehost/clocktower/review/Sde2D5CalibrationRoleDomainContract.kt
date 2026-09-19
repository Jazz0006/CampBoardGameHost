package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

internal enum class Sde2D5CalibrationRoleDomainCompleteness {
    BOUNDED_FIXTURE,
    FULL_SCRIPT_DOMAIN,
}

/**
 * Calibration-only completeness guard.
 *
 * Exact evaluation is exact inside the role domain supplied by its caller. D5 gate calibration,
 * unlike a bounded correctness fixture, must therefore prove that the supplied domain is the full
 * Trouble Brewing script before its evidence may enter human review.
 */
internal object Sde2D5CalibrationRoleDomainContract {
    fun classifyTroubleBrewing(
        roleDefinitions: Collection<RoleDefinition>,
    ): Sde2D5CalibrationRoleDomainCompleteness {
        val expectedRoleIds = TroubleBrewingFixtures.fullRoleDefinitions()
            .mapTo(linkedSetOf()) { it.id }
        val actualRoleIds = roleDefinitions.mapTo(linkedSetOf()) { it.id }
        require(actualRoleIds.size == roleDefinitions.size) {
            "D5 calibration role definitions must not contain duplicate role IDs."
        }
        return if (actualRoleIds == expectedRoleIds) {
            Sde2D5CalibrationRoleDomainCompleteness.FULL_SCRIPT_DOMAIN
        } else {
            Sde2D5CalibrationRoleDomainCompleteness.BOUNDED_FIXTURE
        }
    }

    fun requireFullTroubleBrewing(
        roleDefinitions: Collection<RoleDefinition>,
    ): Sde2D5CalibrationRoleDomainCompleteness {
        val completeness = classifyTroubleBrewing(roleDefinitions)
        require(completeness == Sde2D5CalibrationRoleDomainCompleteness.FULL_SCRIPT_DOMAIN) {
            val expectedRoleIds = TroubleBrewingFixtures.fullRoleDefinitions()
                .mapTo(linkedSetOf()) { it.id }
            val actualRoleIds = roleDefinitions.mapTo(linkedSetOf()) { it.id }
            val missing = (expectedRoleIds - actualRoleIds).map { it.value }.sorted()
            val extra = (actualRoleIds - expectedRoleIds).map { it.value }.sorted()
            "D5 policy calibration requires the full Trouble Brewing role domain; " +
                "missing=${missing.joinToString()}, extra=${extra.joinToString()}."
        }
        return completeness
    }
}
