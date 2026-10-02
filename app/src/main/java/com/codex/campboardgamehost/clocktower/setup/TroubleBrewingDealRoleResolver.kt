package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerRole
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry

internal data class TroubleBrewingResolvedDealAssignment(
    val seat: Int,
    val playerName: String,
    val actualRole: ClocktowerRole,
    val shownRole: ClocktowerRole,
)

/**
 * Resolves already-committed external Trouble Brewing role IDs into the existing App role model.
 * This bridge performs no selection and has no substitution/fallback behavior.
 */
internal object TroubleBrewingDealRoleResolver {
    fun resolveCommitted(
        committedSetup: TroubleBrewingCommittedSetupResult,
        characterRegistry: ClocktowerCharacterRegistry,
        availableRoles: List<ClocktowerRole>,
    ): List<TroubleBrewingResolvedDealAssignment> {
        val players = committedSetup.gameState.players
        require(players.map { it.seat }.distinct().size == players.size) {
            "Trouble Brewing committed seats must be unique."
        }

        return players.sortedBy { it.seat }.map { player ->
            val actualDefinition = requireNotNull(characterRegistry.findByRoleId(player.actualRole)) {
                "Trouble Brewing committed actual role '${player.actualRole}' is missing from the registry."
            }
            val shownRoleId = requireNotNull(player.shownRole) {
                "Trouble Brewing committed player '${player.name}' is missing a shown role."
            }
            val shownDefinition = requireNotNull(characterRegistry.findByRoleId(shownRoleId)) {
                "Trouble Brewing committed shown role '$shownRoleId' is missing from the registry."
            }

            TroubleBrewingResolvedDealAssignment(
                seat = player.seat,
                playerName = player.name,
                actualRole = resolveRole(actualDefinition.externalId, availableRoles),
                shownRole = resolveRole(shownDefinition.externalId, availableRoles),
            )
        }
    }

    private fun resolveRole(
        externalRoleId: String,
        availableRoles: List<ClocktowerRole>,
    ): ClocktowerRole {
        val canonicalExternalId = canonicalRoleId(externalRoleId)
        require(canonicalExternalId.isNotEmpty()) { "Trouble Brewing external role ID cannot be blank." }
        val matches = availableRoles.filter { role -> canonicalRoleId(role.enName) == canonicalExternalId }
        require(matches.size == 1) {
            "Trouble Brewing external role '$externalRoleId' must resolve to exactly one App role."
        }
        return matches.single()
    }

    private fun canonicalRoleId(value: String): String =
        value.filter(Char::isLetterOrDigit).lowercase()
}
