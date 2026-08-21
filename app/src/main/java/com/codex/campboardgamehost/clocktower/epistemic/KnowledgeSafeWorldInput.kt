package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RulesetRef

/**
 * Structural input that a player-world constructor is allowed to learn from the actual formal state.
 *
 * The type deliberately contains no actual role/alignment/type, poison state, shown role, or
 * storyteller-only proposition. The formal snapshot ID is an opaque version binding, not a secret
 * payload; player knowledge must already be bound to that exact version before construction.
 */
data class KnowledgeSafeWorldInput(
    val formalSnapshotId: String,
    val rulesetRef: RulesetRef,
    val playerSeats: List<Int>,
) {
    init {
        require(formalSnapshotId.isNotBlank()) { "formalSnapshotId cannot be blank." }
        require(playerSeats.isNotEmpty()) { "Knowledge-safe world input requires at least one player seat." }
        require(playerSeats.all { it > 0 }) { "Player seats must be positive." }
        require(playerSeats.distinct().size == playerSeats.size) { "Player seats must be unique." }
        require(playerSeats == playerSeats.sorted()) { "Player seats must use canonical order." }
    }

    val playerCount: Int get() = playerSeats.size
}

/** One-way projection: actual storyteller truth cannot be recovered from this value. */
fun FormalGameState.toKnowledgeSafeWorldInput(): KnowledgeSafeWorldInput = KnowledgeSafeWorldInput(
    formalSnapshotId = snapshotId,
    rulesetRef = rulesetRef,
    playerSeats = players.map { it.seat }.sorted(),
)
