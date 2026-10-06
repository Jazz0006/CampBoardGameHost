package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Temporary compatibility facade while RES migrates callers to the rules-owned legal domain.
 *
 * New engine/session code must import the rules owner directly.
 */
internal object PairInformationLegalDomain {
    fun generate(
        game: GameState,
        roleDefinitions: List<RoleDefinition>,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
    ): List<PairInformationLegalCandidate> =
        com.codex.campboardgamehost.clocktower.rules.PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roleDefinitions,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = reliability,
        )
}

internal typealias PairInformationLegalCandidate =
    com.codex.campboardgamehost.clocktower.rules.PairInformationLegalCandidate
