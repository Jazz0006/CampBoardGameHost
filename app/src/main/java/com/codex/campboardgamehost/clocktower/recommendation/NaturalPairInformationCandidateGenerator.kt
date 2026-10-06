package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Temporary compatibility facade while RES migrates callers to the rules-owned generator.
 *
 * New engine/session code must import the rules owner directly.
 */
internal object NaturalPairInformationCandidateGenerator {
    fun generate(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
    ): List<DecisionCandidate<PairInformationOutcome>> =
        com.codex.campboardgamehost.clocktower.rules.NaturalPairInformationCandidateGenerator.generate(
            game = game,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
        )

    fun generatePerceivedFirstNightInformationSpace(
        game: GameState,
        roleDefinitions: List<RoleDefinition> = emptyList(),
    ): List<DecisionCandidate<PairInformationOutcome>> =
        com.codex.campboardgamehost.clocktower.rules.NaturalPairInformationCandidateGenerator
            .generatePerceivedFirstNightInformationSpace(
                game = game,
                roleDefinitions = roleDefinitions,
            )

    fun generateHealthyInformationSpace(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        roleDefinitions: List<RoleDefinition> = emptyList(),
    ): List<DecisionCandidate<PairInformationOutcome>> =
        com.codex.campboardgamehost.clocktower.rules.NaturalPairInformationCandidateGenerator
            .generateHealthyInformationSpace(
                game = game,
                sourceSeat = sourceSeat,
                abilityRole = abilityRole,
                roleDefinitions = roleDefinitions,
            )
}
