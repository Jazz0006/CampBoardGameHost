package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId

/**
 * Final immutable Trouble Brewing setup materialized by the DLB canonical commit boundary.
 *
 * [confirmedDrunkCandidate] records only the already-confirmed rules-legal input. This result owns
 * no recommendation/ranking authority.
 */
internal data class TroubleBrewingCommittedSetupResult(
    val committedSetup: CommittedClocktowerSetup,
    val gameState: GameState,
    val confirmedDrunkCandidate: TroubleBrewingDrunkCandidate?,
)

/**
 * Canonical DLB commit boundary.
 *
 * Rules/setup remains the legality owner through [TroubleBrewingDrunkCandidateDomain]. This seam
 * accepts an explicitly confirmed candidate, verifies that it still belongs to the current legal
 * domain, and materializes final setup truth. It never chooses or ranks a candidate.
 */
internal object TroubleBrewingSetupCommitter {
    fun commit(
        intermediateSetup: TroubleBrewingIntermediateSetup,
        confirmedDrunkCandidate: TroubleBrewingDrunkCandidate?,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingCommittedSetupResult {
        val gameState = if (intermediateSetup.visibleRoster.hasDrunk) {
            val confirmed = requireNotNull(confirmedDrunkCandidate) {
                "Trouble Brewing Drunk setup requires an explicitly confirmed legal candidate."
            }
            val legal = TroubleBrewingDrunkCandidateDomain
                .legalCandidates(intermediateSetup)
                .singleOrNull { candidate -> candidate == confirmed }
            requireNotNull(legal) {
                "Trouble Brewing Drunk commit requires a candidate from the current legal domain."
            }
            TroubleBrewingDrunkHypotheticalProjector.project(
                intermediateSetup = intermediateSetup,
                candidate = legal,
                characterRegistry = characterRegistry,
            ).gameState
        } else {
            require(confirmedDrunkCandidate == null) {
                "Trouble Brewing setup without Drunk cannot commit a Drunk candidate."
            }
            projectWithoutDrunk(intermediateSetup, characterRegistry)
        }

        val drunkRole = requireNotNull(characterRegistry.findByExternalId(DRUNK_EXTERNAL_ID)) {
            "Trouble Brewing canonical commit requires the Drunk definition."
        }.id
        val actualDrunkCount = gameState.players.count { player -> player.actualRole == drunkRole }
        require(actualDrunkCount == if (intermediateSetup.visibleRoster.hasDrunk) 1 else 0) {
            "Trouble Brewing canonical commit must contain exactly one Drunk iff the setup has Drunk."
        }

        val confirmedSeat = confirmedDrunkCandidate?.seat
        gameState.players.forEach { player ->
            if (player.seat == confirmedSeat) {
                require(player.actualRole == drunkRole) {
                    "Trouble Brewing confirmed Drunk seat must commit the canonical Drunk role."
                }
                require(player.shownRole != null && player.shownRole != player.actualRole) {
                    "Trouble Brewing Drunk must retain its dealt Townsfolk shown identity."
                }
            } else {
                require(player.actualRole == player.shownRole) {
                    "Trouble Brewing non-Drunk seats must keep actual role equal to shown role at setup commit."
                }
            }
        }

        val committedSetup = CommittedClocktowerSetup(
            script = ClocktowerScript.TroubleBrewing.toRecommendationScriptId(),
            setupSeed = intermediateSetup.gameSeed,
            assignments = gameState.players
                .sortedBy(PlayerState::seat)
                .map { player ->
                    CommittedSetupSeat(
                        seat = player.seat,
                        actualRole = player.actualRole,
                        shownRole = requireNotNull(player.shownRole) {
                            "Trouble Brewing canonical setup requires every seat to have a shown role."
                        },
                    )
                },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.TEMPLATE,
                providerId = intermediateSetup.datasetId,
                candidateId = intermediateSetup.presetId,
            ),
        )

        return TroubleBrewingCommittedSetupResult(
            committedSetup = committedSetup,
            gameState = gameState,
            confirmedDrunkCandidate = confirmedDrunkCandidate,
        )
    }

    private fun projectWithoutDrunk(
        intermediateSetup: TroubleBrewingIntermediateSetup,
        characterRegistry: ClocktowerCharacterRegistry,
    ): GameState {
        require(!intermediateSetup.visibleRoster.hasDrunk) {
            "Non-Drunk projection cannot materialize a Drunk-bearing setup."
        }
        return GameState(
            script = ClocktowerScript.TroubleBrewing.toRecommendationScriptId(),
            players = intermediateSetup.shownSeatAssignments.map { assignment ->
                val role = requireNotNull(characterRegistry.findByExternalId(assignment.shownRoleId)) {
                    "Trouble Brewing shown role '${assignment.shownRoleId}' is missing from the character registry."
                }
                PlayerState(
                    seat = assignment.seat,
                    name = assignment.playerName,
                    actualRole = role.id,
                    actualAlignment = role.team.toDomainAlignment(),
                    actualType = role.team.toDomainCharacterType(),
                    shownRole = role.id,
                    alive = true,
                    poisoned = false,
                )
            },
            seed = intermediateSetup.gameSeed,
        )
    }

    private fun ClocktowerCatalogTeam.toDomainAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON -> Alignment.EVIL

        else -> throw IllegalArgumentException(
            "Trouble Brewing canonical setup cannot contain unsupported team $this.",
        )
    }

    private fun ClocktowerCatalogTeam.toDomainCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> throw IllegalArgumentException(
            "Trouble Brewing canonical setup cannot contain unsupported team $this.",
        )
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
