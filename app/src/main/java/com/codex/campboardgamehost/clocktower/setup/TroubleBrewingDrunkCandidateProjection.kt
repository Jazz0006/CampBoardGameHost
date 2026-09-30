package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.domain.toRecommendationScriptId

internal data class TroubleBrewingDrunkCandidateRef(
    val seat: Int,
    val shownRoleId: String,
) {
    init {
        require(seat > 0) { "Trouble Brewing Drunk candidate seat must be positive." }
        require(shownRoleId.isNotBlank()) { "Trouble Brewing Drunk candidate shown role cannot be blank." }
    }
}

internal data class TroubleBrewingDrunkCandidate(
    val seat: Int,
    val playerName: String,
    val shownRoleId: String,
) {
    init {
        require(seat > 0) { "Trouble Brewing Drunk candidate seat must be positive." }
        require(playerName.isNotBlank()) { "Trouble Brewing Drunk candidate player name cannot be blank." }
        require(shownRoleId.isNotBlank()) { "Trouble Brewing Drunk candidate shown role cannot be blank." }
    }
}

/**
 * Rules-owned legal Drunk-seat domain for a post-seat Trouble Brewing intermediate setup.
 *
 * Legality is intentionally independent of compatibility finalization, SDE ranking and runtime
 * session state. Every dealt Townsfolk is a peer candidate once the visible roster has been seated.
 */
internal object TroubleBrewingDrunkCandidateDomain {
    fun legalCandidateRefs(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): List<TroubleBrewingDrunkCandidateRef> {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.SETUP_PRECOMMIT) {
            "Trouble Brewing Drunk candidate domain requires a setup-precommit snapshot."
        }
        val hasDrunk = (snapshot.setupState.hasDrunk as? SnapshotField.Known<Boolean>)?.value
            ?: throw IllegalArgumentException(
                "Trouble Brewing Drunk candidate domain requires known hasDrunk state.",
            )
        if (!hasDrunk) return emptyList()
        require(snapshot.setupState.drunkAssignmentSeat === SnapshotField.Uncommitted) {
            "Trouble Brewing Drunk candidate domain requires an uncommitted Drunk assignment."
        }

        return snapshot.grimoireSeats.mapNotNull { seat ->
            val shownRoleId = (seat.shownRoleId as? SnapshotField.Known<String>)?.value
                ?: throw IllegalArgumentException(
                    "Trouble Brewing Drunk candidate domain requires known shown roles.",
                )
            val shownDefinition = requireNotNull(characterRegistry.findByExternalId(shownRoleId)) {
                "Trouble Brewing shown role '$shownRoleId' is missing from the character registry."
            }
            if (shownDefinition.team != ClocktowerCatalogTeam.TOWNSFOLK) return@mapNotNull null
            require(seat.actualRoleId === SnapshotField.Uncommitted) {
                "Every setup-precommit Townsfolk candidate must have UNCOMMITTED actual role."
            }
            TroubleBrewingDrunkCandidateRef(
                seat = seat.seat,
                shownRoleId = shownRoleId,
            )
        }
    }

    fun legalCandidates(
        intermediateSetup: TroubleBrewingIntermediateSetup,
    ): List<TroubleBrewingDrunkCandidate> {
        if (!intermediateSetup.visibleRoster.hasDrunk) return emptyList()

        val townsfolkRoleIds = intermediateSetup.visibleRoster.townsfolkRoleIds.toSet()
        return intermediateSetup.shownSeatAssignments
            .asSequence()
            .filter { assignment -> assignment.shownRoleId in townsfolkRoleIds }
            .sortedBy { assignment -> assignment.seat }
            .map { assignment ->
                TroubleBrewingDrunkCandidate(
                    seat = assignment.seat,
                    playerName = assignment.playerName,
                    shownRoleId = assignment.shownRoleId,
                )
            }
            .toList()
            .also { candidates ->
                require(candidates.size == intermediateSetup.visibleRoster.townsfolkRoleIds.size) {
                    "Trouble Brewing Drunk candidate domain must contain every dealt Townsfolk exactly once."
                }
            }
    }
}

internal data class TroubleBrewingDrunkHypotheticalSetup(
    val candidate: TroubleBrewingDrunkCandidate,
    val gameState: GameState,
)

/**
 * Pure DLB projector from one rules-legal Drunk candidate to its hypothetical effective setup.
 *
 * The projection materializes no canonical setup/session mutation. It is safe to evaluate multiple
 * alternatives independently before DLB-4 commits one candidate.
 */
internal object TroubleBrewingDrunkHypotheticalProjector {
    fun project(
        intermediateSetup: TroubleBrewingIntermediateSetup,
        candidate: TroubleBrewingDrunkCandidate,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingDrunkHypotheticalSetup {
        val legalCandidate = TroubleBrewingDrunkCandidateDomain
            .legalCandidates(intermediateSetup)
            .singleOrNull { legal -> legal == candidate }
        requireNotNull(legalCandidate) {
            "Trouble Brewing hypothetical Drunk projection requires a rules-legal dealt Townsfolk candidate."
        }

        val drunkDefinition = requireNotNull(characterRegistry.findByExternalId(DRUNK_EXTERNAL_ID)) {
            "Trouble Brewing hypothetical projection requires the canonical Drunk definition."
        }
        require(drunkDefinition.team == ClocktowerCatalogTeam.OUTSIDER) {
            "Canonical Trouble Brewing Drunk must be an Outsider."
        }

        val players = intermediateSetup.shownSeatAssignments.map { assignment ->
            val shownDefinition = requireNotNull(
                characterRegistry.findByExternalId(assignment.shownRoleId),
            ) {
                "Trouble Brewing shown role '${assignment.shownRoleId}' is missing from the character registry."
            }

            if (assignment.seat == legalCandidate.seat) {
                require(shownDefinition.team == ClocktowerCatalogTeam.TOWNSFOLK) {
                    "Trouble Brewing Drunk candidate must have a dealt Townsfolk shown identity."
                }
                PlayerState(
                    seat = assignment.seat,
                    name = assignment.playerName,
                    actualRole = drunkDefinition.id,
                    actualAlignment = Alignment.GOOD,
                    actualType = CharacterType.OUTSIDER,
                    shownRole = shownDefinition.id,
                    alive = true,
                    poisoned = false,
                )
            } else {
                PlayerState(
                    seat = assignment.seat,
                    name = assignment.playerName,
                    actualRole = shownDefinition.id,
                    actualAlignment = shownDefinition.team.toDomainAlignment(),
                    actualType = shownDefinition.team.toDomainCharacterType(),
                    shownRole = shownDefinition.id,
                    alive = true,
                    poisoned = false,
                )
            }
        }

        return TroubleBrewingDrunkHypotheticalSetup(
            candidate = legalCandidate,
            gameState = GameState(
                script = ClocktowerScript.TroubleBrewing.toRecommendationScriptId(),
                players = players,
                seed = intermediateSetup.gameSeed,
            ),
        )
    }

    private fun ClocktowerCatalogTeam.toDomainAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON -> Alignment.EVIL

        else -> throw IllegalArgumentException(
            "Trouble Brewing hypothetical setup cannot contain unsupported team $this.",
        )
    }

    private fun ClocktowerCatalogTeam.toDomainCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> throw IllegalArgumentException(
            "Trouble Brewing hypothetical setup cannot contain unsupported team $this.",
        )
    }

    private const val DRUNK_EXTERNAL_ID = "drunk"
}
