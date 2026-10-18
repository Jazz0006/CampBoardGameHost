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

/** The final confirmed setup, without hypothetical alternative GameStates. */
internal data class TroubleBrewingCommittedSetupResult(
    val committedSetup: CommittedClocktowerSetup,
    val gameState: GameState,
    val confirmedDrunkCandidate: TroubleBrewingDrunkCandidate?,
)

/** Materialize the confirmed identities once; gameplay legality belongs outside Host. */
internal object TroubleBrewingSetupCommitter {
    fun commit(
        intermediateSetup: TroubleBrewingIntermediateSetup,
        confirmedDrunkCandidate: TroubleBrewingDrunkCandidate?,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingCommittedSetupResult {
        require(intermediateSetup.visibleRoster.hasDrunk == (confirmedDrunkCandidate != null)) {
            "The confirmed setup must resolve its pending Drunk assignment."
        }
        confirmedDrunkCandidate?.let { confirmed ->
            require(intermediateSetup.shownSeatAssignments.any {
                it.seat == confirmed.seat && it.playerName == confirmed.playerName &&
                    it.shownRoleId == confirmed.shownRoleId
            }) { "Drunk assignment references a changed or unknown dealt seat." }
        }
        val gameState = materialize(intermediateSetup, confirmedDrunkCandidate, characterRegistry)

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

    private fun materialize(
        intermediateSetup: TroubleBrewingIntermediateSetup,
        confirmed: TroubleBrewingDrunkCandidate?,
        characterRegistry: ClocktowerCharacterRegistry,
    ): GameState {
        return GameState(
            script = ClocktowerScript.TroubleBrewing.toRecommendationScriptId(),
            players = intermediateSetup.shownSeatAssignments.map { assignment ->
                val role = requireNotNull(characterRegistry.findByExternalId(assignment.shownRoleId)) {
                    "Trouble Brewing shown role '${assignment.shownRoleId}' is missing from the character registry."
                }
                val actual = if (assignment.seat == confirmed?.seat)
                    requireNotNull(characterRegistry.findByExternalId(DRUNK_EXTERNAL_ID))
                else role
                PlayerState(
                    seat = assignment.seat,
                    name = assignment.playerName,
                    actualRole = actual.id,
                    actualAlignment = actual.team.toDomainAlignment(),
                    actualType = actual.team.toDomainCharacterType(),
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
