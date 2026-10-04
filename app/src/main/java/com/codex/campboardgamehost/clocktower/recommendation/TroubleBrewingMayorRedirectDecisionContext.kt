package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.PublicBalanceHint
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectDecisionDomain
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegalDomain

/**
 * Snapshot-backed recommendation context for a Trouble Brewing Mayor night-death decision.
 *
 * The rules-owned [decisionDomain] is the sole legality source. Protection/spent/history-derived
 * values remain explicit runtime enrichment until their canonical owners are migrated separately.
 */
internal data class TroubleBrewingMayorRedirectDecisionContext(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val recommendationGameState: GameState,
    val decisionDomain: MayorRedirectDecisionDomain,
    val protectedSeats: Set<Int>,
    val playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
    val spentAbilitySeats: Set<Int>,
    val publicBalanceHint: PublicBalanceHint,
) {
    init {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "Mayor redirect context requires a runtime Trouble Brewing snapshot."
        }
        require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.NIGHT)) {
            "Mayor redirect context requires NIGHT."
        }
        val round = (snapshot.position.round as? SnapshotField.Known<Int>)?.value
        require(round != null && round > 0) { "Mayor redirect context requires a known positive round." }

        val seats = recommendationGameState.players.mapTo(linkedSetOf()) { it.seat }
        require(decisionDomain.legalTargetSeats.all { it in seats }) {
            "Mayor redirect decision domain references an unknown seat."
        }
        require(protectedSeats.all { it in seats }) {
            "Mayor redirect protection enrichment references an unknown seat."
        }
        require(playerInformationPressureBySeat.keys.all { it in seats }) {
            "Mayor redirect pressure enrichment references an unknown seat."
        }
        require(spentAbilitySeats.all { it in seats }) {
            "Mayor redirect spent-ability enrichment references an unknown seat."
        }
    }
}

internal fun TroubleBrewingMayorRedirectDecisionContext.toDynamicRequest(
    requestId: String,
): DynamicDecisionRequest {
    require(requestId.isNotBlank()) { "Mayor redirect request ID cannot be blank." }
    val round = (snapshot.position.round as? SnapshotField.Known<Int>)?.value
        ?: error("Mayor redirect context requires a known round.")
    return DynamicDecisionRequest(
        id = requestId,
        type = StorytellerDecisionType.MAYOR_DEATH_RESOLUTION,
        sourceAbility = RoleId("Mayor"),
        state = DynamicGameState(
            game = recommendationGameState,
            phase = StorytellerPhase.NIGHT,
            round = round,
            protectedSeats = protectedSeats,
            spentAbilitySeats = spentAbilitySeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
            publicBalanceHint = publicBalanceHint,
        ),
    )
}

internal object TroubleBrewingMayorRedirectDecisionContextBuilder {
    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
        mayorSeat: Int,
        protectedSeats: Set<Int>,
        playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
        spentAbilitySeats: Set<Int>,
    ): TroubleBrewingMayorRedirectDecisionContext {
        val game = projectRuntimeGame(snapshot, characterRegistry)
        return build(
            snapshot = snapshot,
            recommendationGameState = game,
            decisionDomain = MayorRedirectLegalDomain.resolve(game, mayorSeat),
            protectedSeats = protectedSeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
            spentAbilitySeats = spentAbilitySeats,
        )
    }

    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
        decisionDomain: MayorRedirectDecisionDomain,
        protectedSeats: Set<Int>,
        playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
        spentAbilitySeats: Set<Int>,
    ): TroubleBrewingMayorRedirectDecisionContext {
        val game = projectRuntimeGame(snapshot, characterRegistry)
        return build(
            snapshot = snapshot,
            recommendationGameState = game,
            decisionDomain = decisionDomain,
            protectedSeats = protectedSeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
            spentAbilitySeats = spentAbilitySeats,
        )
    }

    private fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        recommendationGameState: GameState,
        decisionDomain: MayorRedirectDecisionDomain,
        protectedSeats: Set<Int>,
        playerInformationPressureBySeat: Map<Int, PlayerInformationPressure>,
        spentAbilitySeats: Set<Int>,
    ): TroubleBrewingMayorRedirectDecisionContext {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "Mayor redirect context requires a runtime Trouble Brewing snapshot."
        }
        require(snapshot.position.phase == SnapshotField.Known(StorytellerPhase.NIGHT)) {
            "Mayor redirect context requires NIGHT."
        }
        val round = (snapshot.position.round as? SnapshotField.Known<Int>)?.value
            ?: error("Mayor redirect context requires a known round.")
        val balance = GameBalanceEvaluator.evaluate(
            game = recommendationGameState,
            round = round,
            spentAbilitySeats = spentAbilitySeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
        )
        return TroubleBrewingMayorRedirectDecisionContext(
            snapshot = snapshot,
            recommendationGameState = recommendationGameState,
            decisionDomain = decisionDomain,
            protectedSeats = protectedSeats,
            playerInformationPressureBySeat = playerInformationPressureBySeat,
            spentAbilitySeats = spentAbilitySeats,
            publicBalanceHint = balance.hint,
        )
    }

    private fun projectRuntimeGame(
        snapshot: TroubleBrewingGameSnapshotV1,
        characterRegistry: ClocktowerCharacterRegistry,
    ): GameState {
        require(snapshot.position.stage == TroubleBrewingSnapshotStage.RUNTIME) {
            "Mayor redirect context requires a runtime Trouble Brewing snapshot."
        }
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
        return GameState(
            script = snapshot.script,
            players = players,
            seed = snapshot.gameSeed,
        )
    }

    private fun <T> SnapshotField<T>.requireKnown(label: String, seat: Int): T =
        (this as? SnapshotField.Known<T>)?.value
            ?: error("Mayor redirect context requires known " + label + " at seat " + seat + ".")

    private fun ClocktowerCatalogTeam.toAlignment(): Alignment = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK,
        ClocktowerCatalogTeam.OUTSIDER,
        -> Alignment.GOOD

        ClocktowerCatalogTeam.MINION,
        ClocktowerCatalogTeam.DEMON,
        -> Alignment.EVIL

        else -> error("Mayor redirect context does not support catalog team " + this + ".")
    }

    private fun ClocktowerCatalogTeam.toCharacterType(): CharacterType = when (this) {
        ClocktowerCatalogTeam.TOWNSFOLK -> CharacterType.TOWNSFOLK
        ClocktowerCatalogTeam.OUTSIDER -> CharacterType.OUTSIDER
        ClocktowerCatalogTeam.MINION -> CharacterType.MINION
        ClocktowerCatalogTeam.DEMON -> CharacterType.DEMON
        else -> error("Mayor redirect context does not support catalog team " + this + ".")
    }
}
