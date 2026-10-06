package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.DynamicDecisionRequest
import com.codex.campboardgamehost.clocktower.domain.DynamicGameState
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerInformationPressure
import com.codex.campboardgamehost.clocktower.domain.PublicBalanceHint
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectDecisionDomain
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegalDomain
import com.codex.campboardgamehost.clocktower.session.TroubleBrewingRuntimeGameProjector

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
        val game = TroubleBrewingRuntimeGameProjector.project(snapshot, characterRegistry)
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
        val game = TroubleBrewingRuntimeGameProjector.project(snapshot, characterRegistry)
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

}
