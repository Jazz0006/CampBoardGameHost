package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage

/**
 * Snapshot-backed mechanical/rules base for Trouble Brewing setup recommendations.
 *
 * Recommendation locks and cross-game history deliberately remain outside this context because
 * they are coordination/enrichment inputs rather than canonical grimoire truth.
 */
internal class TroubleBrewingSetupRecommendationDecisionContext(
    val snapshot: TroubleBrewingGameSnapshotV1,
    internal val recommendationGameState: GameState,
    internal val roleDefinitions: List<RoleDefinition>,
)

internal object TroubleBrewingSetupRecommendationDecisionContextBuilder {
    // SetupCandidateGenerator preserves role-definition input order inside DemonBluffs. Keep the
    // pre-TBGS production ordering so snapshot migration cannot change the visible bluff-role order.
    // Definitions themselves still come exclusively from the validated rules registry.
    private val historicalSetupRecommendationRoleOrder = listOf(
        "Washerwoman",
        "Librarian",
        "Investigator",
        "Chef",
        "Empath",
        "Fortune Teller",
        "Ravenkeeper",
        "Soldier",
        "Mayor",
        "Butler",
        "Drunk",
        "Recluse",
        "Saint",
        "Poisoner",
        "Spy",
        "Baron",
        "Scarlet Woman",
        "Imp",
        "Undertaker",
        "Monk",
        "Virgin",
        "Slayer",
    ).map(::RoleId)

    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingSetupRecommendationDecisionContext {
        requireValidPosition(snapshot)

        val players = snapshot.grimoireSeats.map { seat ->
            val actualExternalId = seat.actualRoleId.requireKnown("actual role", seat.seat)
            val shownExternalId = seat.shownRoleId.requireKnown("shown role", seat.seat)
            val actual = requireNotNull(characterRegistry.findByExternalId(actualExternalId)) {
                "Unknown Trouble Brewing actual role '$actualExternalId' at seat ${seat.seat}."
            }
            val shown = requireNotNull(characterRegistry.findByExternalId(shownExternalId)) {
                "Unknown Trouble Brewing shown role '$shownExternalId' at seat ${seat.seat}."
            }
            PlayerState(
                seat = seat.seat,
                name = "Seat ${seat.seat}",
                actualRole = actual.id,
                actualAlignment = actual.team.toAlignment(),
                actualType = actual.team.toCharacterType(),
                shownRole = shown.id,
                alive = seat.alive.requireKnown("alive state", seat.seat),
                poisoned = seat.poisoned.requireKnown("poison state", seat.seat),
            )
        }

        val definitionsByRoleId = characterRegistry.definitions
            .filter { definition ->
                definition.team == ClocktowerCatalogTeam.TOWNSFOLK ||
                    definition.team == ClocktowerCatalogTeam.OUTSIDER ||
                    definition.team == ClocktowerCatalogTeam.MINION ||
                    definition.team == ClocktowerCatalogTeam.DEMON
            }
            .associateBy { definition -> definition.id }
        require(definitionsByRoleId.keys == historicalSetupRecommendationRoleOrder.toSet()) {
            "Trouble Brewing setup recommendation context requires the complete historical TB role domain."
        }
        val roleDefinitions = historicalSetupRecommendationRoleOrder.map { roleId ->
            val definition = definitionsByRoleId.getValue(roleId)
            RoleDefinition(
                id = definition.id,
                alignment = definition.team.toAlignment(),
                type = definition.team.toCharacterType(),
                scriptIds = setOf(snapshot.script),
            )
        }

        return TroubleBrewingSetupRecommendationDecisionContext(
            snapshot = snapshot,
            recommendationGameState = GameState(
                script = snapshot.script,
                players = players,
                seed = snapshot.gameSeed,
            ),
            roleDefinitions = roleDefinitions,
        )
    }

    private fun requireValidPosition(snapshot: TroubleBrewingGameSnapshotV1) {
        when (snapshot.position.stage) {
            TroubleBrewingSnapshotStage.SETUP_COMMITTED -> {
                require(snapshot.position.phase == SnapshotField.NotApplicable) {
                    "Committed Trouble Brewing setup recommendation context requires no runtime phase."
                }
                require(snapshot.position.round == SnapshotField.NotApplicable) {
                    "Committed Trouble Brewing setup recommendation context requires no runtime round."
                }
            }

            TroubleBrewingSnapshotStage.RUNTIME -> {
                require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.FIRST_NIGHT)) {
                    "Runtime Trouble Brewing setup recommendation context requires FIRST_NIGHT."
                }
                require(snapshot.position.round == SnapshotField.Known(1)) {
                    "Runtime Trouble Brewing setup recommendation context requires round 1."
                }
            }

            TroubleBrewingSnapshotStage.SETUP_PRECOMMIT -> error(
                "Trouble Brewing setup recommendation context requires finalized setup truth.",
            )
        }
    }

    private fun <T> SnapshotField<T>.requireKnown(label: String, seat: Int): T =
        (this as? SnapshotField.Known<T>)?.value
            ?: error("Trouble Brewing setup recommendation context requires known $label at seat $seat.")

    private fun ClocktowerCatalogTeam.toAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER,
        -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON,
        -> Alignment.EVIL

        else -> error("Trouble Brewing setup recommendation context does not support catalog team $this.")
    }

    private fun ClocktowerCatalogTeam.toCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> error("Trouble Brewing setup recommendation context does not support catalog team $this.")
    }
}
