package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage

/** Neutral runtime projection from canonical Trouble Brewing snapshot data to rules GameState. */
internal object TroubleBrewingRuntimeGameProjector {
    fun project(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): GameState {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "Trouble Brewing runtime projection requires a runtime snapshot."
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
                poisoned = seat.poisoned.requireKnown("poison state", seat.seat),
            )
        }

        return GameState(
            script = snapshot.script,
            players = players,
            seed = snapshot.gameSeed,
        )
    }

    private fun <T> SnapshotField<T>.requireKnown(label: String, seat: Int): T =
        (this as? SnapshotField.Known<T>)?.value
            ?: error("Trouble Brewing runtime projection requires known $label at seat $seat.")

    private fun ClocktowerCatalogTeam.toAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER,
        -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON,
        -> Alignment.EVIL

        else -> error("Trouble Brewing runtime projection does not support catalog team $this.")
    }

    private fun ClocktowerCatalogTeam.toCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> error("Trouble Brewing runtime projection does not support catalog team $this.")
    }
}
