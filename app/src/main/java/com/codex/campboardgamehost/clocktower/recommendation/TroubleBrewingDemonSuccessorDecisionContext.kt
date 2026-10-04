package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution

/**
 * Snapshot-backed production recommendation context for Trouble Brewing Demon succession.
 *
 * Rules own the legal target domain through [successionResolution]. [recommendationGameState] is a
 * read-only compatibility projection from the immutable snapshot for the existing scoring model.
 * Pressure/spent inputs are explicit legacy-equivalent enrichment, not mechanical authority.
 */
internal data class TroubleBrewingDemonSuccessorDecisionContext(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val recommendationGameState: GameState,
    val successionResolution: DemonSuccessionResolution,
    val playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
    val spentAbilitySeats: Set<Int>,
    val evilAdvantage: Int,
) {
    init {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "Demon successor context requires a runtime Trouble Brewing snapshot."
        }
        require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.NIGHT)) {
            "Demon successor context requires NIGHT."
        }
        val round = (snapshot.position.round as? SnapshotField.Known<Int>)?.value
        require(round != null && round > 0) {
            "Demon successor context requires a known positive round."
        }
        val seats = recommendationGameState.players.mapTo(linkedSetOf()) { it.seat }
        require(playerInformationPressureBySeat.keys.all { it in seats }) {
            "Demon successor pressure enrichment references an unknown seat."
        }
        require(spentAbilitySeats.all { it in seats }) {
            "Demon successor spent-ability enrichment references an unknown seat."
        }
        legalTargetSeats().forEach { seat ->
            require(recommendationGameState.playerAt(seat) != null) {
                "Demon succession resolution references unknown seat " + seat + "."
            }
        }
    }

    fun legalTargetSeats(): Set<Int> = when (val resolution = successionResolution) {
        DemonSuccessionResolution.None -> emptySet()
        is DemonSuccessionResolution.Forced -> setOf(resolution.targetSeat)
        is DemonSuccessionResolution.Choice -> resolution.targetSeats
    }
}

internal object TroubleBrewingDemonSuccessorDecisionContextBuilder {
    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
        successionResolution: DemonSuccessionResolution,
        playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
        spentAbilitySeats: Set<Int>,
    ): TroubleBrewingDemonSuccessorDecisionContext {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "Demon successor context requires a runtime Trouble Brewing snapshot."
        }
        require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.NIGHT)) {
            "Demon successor context requires NIGHT."
        }
        val round = (snapshot.position.round as? SnapshotField.Known<Int>)?.value
            ?: error("Demon successor context requires a known round.")

        val players = snapshot.grimoireSeats.map { seat ->
            val actualExternalId = seat.actualRoleId.requireKnown("actual role", seat.seat)
            val shownExternalId = seat.shownRoleId.requireKnown("shown role", seat.seat)
            val actual = requireNotNull(characterRegistry.findByExternalId(actualExternalId)) {
                "Unknown Trouble Brewing actual role '" + actualExternalId + "' at seat " + seat.seat + "."
            }
            val shown = requireNotNull(characterRegistry.findByExternalId(shownExternalId)) {
                "Unknown Trouble Brewing shown role '" + shownExternalId + "' at seat " + seat.seat + "."
            }
            PlayerState(
                seat = seat.seat,
                name = "Seat " + seat.seat,
                actualRole = actual.id,
                actualAlignment = actual.team.toAlignment(),
                actualType = actual.team.toCharacterType(),
                shownRole = shown.id,
                alive = seat.alive.requireKnown("alive state", seat.seat),
                poisoned = seat.poisoned.requireKnown("poison state", seat.seat),
            )
        }
        val game = GameState(
            script = snapshot.script,
            players = players,
            seed = snapshot.gameSeed,
        )
        val balance = GameBalanceEvaluator.evaluate(
            game = game,
            round = round,
            spentAbilitySeats = spentAbilitySeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
        )
        return TroubleBrewingDemonSuccessorDecisionContext(
            snapshot = snapshot,
            recommendationGameState = game,
            successionResolution = successionResolution,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
            spentAbilitySeats = spentAbilitySeats,
            evilAdvantage = balance.evilAdvantage,
        )
    }

    private fun <T> SnapshotField<T>.requireKnown(label: String, seat: Int): T =
        (this as? SnapshotField.Known<T>)?.value
            ?: error("Demon successor context requires known " + label + " at seat " + seat + ".")

    private fun ClocktowerCatalogTeam.toAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER,
        -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON,
        -> Alignment.EVIL

        else -> error("Demon successor context does not support catalog team " + this + ".")
    }

    private fun ClocktowerCatalogTeam.toCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> error("Demon successor context does not support catalog team " + this + ".")
    }
}
