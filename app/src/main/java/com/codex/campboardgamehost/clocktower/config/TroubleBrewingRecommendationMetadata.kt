package com.codex.campboardgamehost.clocktower.config

import com.codex.campboardgamehost.clocktower.domain.RoleId

internal data class RoleRecommendationMetadata(
    val exposureSensitivity: Int = 2,
    val discussionValue: Int = 2,
    val bluffDifficulty: Int = 2,
    val drunkSuitability: Int = 2,
    val redHerringSuitability: Int = 0,
    val investigatorDisplaySuitability: Int = 0,
)

internal object TroubleBrewingRecommendationMetadata {
    private val neutral = RoleRecommendationMetadata()

    private val roles: Map<RoleId, RoleRecommendationMetadata> = mapOf(
        "Washerwoman" to RoleRecommendationMetadata(2, 3, 2, 4, 1),
        "Librarian" to RoleRecommendationMetadata(2, 3, 2, 4, 1),
        "Investigator" to RoleRecommendationMetadata(3, 4, 2, 5, 1),
        "Chef" to RoleRecommendationMetadata(1, 2, 1, 4, 2),
        "Empath" to RoleRecommendationMetadata(3, 4, 3, 4, 0),
        "Fortune Teller" to RoleRecommendationMetadata(5, 5, 4, 4, -2),
        "Undertaker" to RoleRecommendationMetadata(4, 4, 3, 3, 0),
        "Monk" to RoleRecommendationMetadata(4, 3, 2, 2, 1),
        "Ravenkeeper" to RoleRecommendationMetadata(4, 3, 3, 2, 1),
        "Virgin" to RoleRecommendationMetadata(3, 4, 3, 2, 5),
        "Slayer" to RoleRecommendationMetadata(4, 4, 2, 1, 1),
        "Soldier" to RoleRecommendationMetadata(2, 2, 1, 2, 3),
        "Mayor" to RoleRecommendationMetadata(4, 4, 3, 1, 1),
        "Butler" to RoleRecommendationMetadata(1, 2, 1, 0, 3),
        // Avoid stacking the Fortune Teller's persistent false positive on the already unreliable Drunk.
        "Drunk" to RoleRecommendationMetadata(1, 3, 4, 0, -3),
        "Recluse" to RoleRecommendationMetadata(2, 4, 3, 0, 3),
        "Saint" to RoleRecommendationMetadata(4, 5, 3, 0, 4),
        "Poisoner" to RoleRecommendationMetadata(investigatorDisplaySuitability = 3),
        "Spy" to RoleRecommendationMetadata(investigatorDisplaySuitability = 1),
        "Baron" to RoleRecommendationMetadata(investigatorDisplaySuitability = 0),
        "Scarlet Woman" to RoleRecommendationMetadata(investigatorDisplaySuitability = 1),
    ).mapKeys { (roleName, _) -> RoleId(roleName) }

    fun forRole(role: RoleId): RoleRecommendationMetadata = roles[role] ?: neutral
}
