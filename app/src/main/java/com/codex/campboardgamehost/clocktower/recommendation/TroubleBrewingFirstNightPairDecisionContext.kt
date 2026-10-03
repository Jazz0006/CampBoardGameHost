package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage

/**
 * Snapshot-backed required context for the Trouble Brewing first-night natural pair-information
 * candidate space.
 *
 * The canonical snapshot remains available for freshness/diagnostics. [naturalPairGameState] is a
 * compatibility projection for the existing candidate generator only; it is derived solely from
 * the immutable snapshot and is never a mutable authority.
 *
 * Poison is intentionally normalized away in [naturalPairGameState]. The natural pair candidate
 * space describes the perceived ability's truthful semantic domain; Drunk/Poisoned reliability is
 * applied downstream. This preserves the historical production precompute behavior, which supplied
 * a GameState with no poison target.
 */
internal class TroubleBrewingFirstNightPairDecisionContext(
    val snapshot: TroubleBrewingGameSnapshotV1,
    internal val naturalPairGameState: GameState,
    internal val roleDefinitions: List<RoleDefinition>,
) {
    init {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "First-night pair decision context requires a runtime Trouble Brewing snapshot."
        }
        require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.FIRST_NIGHT)) {
            "First-night pair decision context requires FIRST_NIGHT."
        }
        require(snapshot.position.round == SnapshotField.Known(1)) {
            "First-night pair decision context requires round 1."
        }
    }

    // The precompute coordinator historically keyed exact reuse by GameState equality. Preserve
    // that behavior rather than invalidating natural candidate-space work on unrelated input
    // revisions or Poisoner reliability changes.
    override fun equals(other: Any?): Boolean =
        other is TroubleBrewingFirstNightPairDecisionContext &&
            naturalPairGameState == other.naturalPairGameState

    override fun hashCode(): Int = naturalPairGameState.hashCode()
}

internal object TroubleBrewingFirstNightPairDecisionContextBuilder {
    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): TroubleBrewingFirstNightPairDecisionContext {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "First-night pair decision context requires a runtime Trouble Brewing snapshot."
        }
        require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.FIRST_NIGHT)) {
            "First-night pair decision context requires FIRST_NIGHT."
        }
        require(snapshot.position.round == SnapshotField.Known(1)) {
            "First-night pair decision context requires round 1."
        }

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
                poisoned = false,
            )
        }

        val roleDefinitions = characterRegistry.definitions.mapNotNull { definition ->
            when (definition.team) {
                ClocktowerCatalogTeam.TOWNSFOLK,
                ClocktowerCatalogTeam.OUTSIDER,
                ClocktowerCatalogTeam.MINION,
                ClocktowerCatalogTeam.DEMON,
                -> RoleDefinition(
                    id = definition.id,
                    alignment = definition.team.toAlignment(),
                    type = definition.team.toCharacterType(),
                    scriptIds = setOf(snapshot.script),
                )

                else -> null
            }
        }

        return TroubleBrewingFirstNightPairDecisionContext(
            snapshot = snapshot,
            naturalPairGameState = GameState(
                script = snapshot.script,
                players = players,
                seed = snapshot.gameSeed,
            ),
            roleDefinitions = roleDefinitions,
        )
    }

    private fun <T> SnapshotField<T>.requireKnown(label: String, seat: Int): T =
        (this as? SnapshotField.Known<T>)?.value
            ?: error("Trouble Brewing first-night pair context requires known $label at seat $seat.")

    private fun ClocktowerCatalogTeam.toAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER,
        -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON,
        -> Alignment.EVIL

        else -> error("Trouble Brewing pair context does not support catalog team $this.")
    }

    private fun ClocktowerCatalogTeam.toCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> error("Trouble Brewing pair context does not support catalog team $this.")
    }
}
