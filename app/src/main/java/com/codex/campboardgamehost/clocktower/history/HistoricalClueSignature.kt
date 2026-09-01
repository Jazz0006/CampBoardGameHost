package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RoleId

data class HistoricalClueSignature(
    val decisionType: String,
    val drunkShownRole: RoleId? = null,
    val shownCharacter: RoleId? = null,
    val candidateAlignmentPattern: String? = null,
    val candidateSeatDistance: Int? = null,
    val redHerringRole: RoleId? = null,
    val demonBluffs: Set<RoleId> = emptySet(),
) {
    init {
        require(decisionType.isNotBlank()) { "decisionType cannot be blank." }
        require(candidateSeatDistance == null || candidateSeatDistance >= 0)
    }

    fun canonical(): String = listOf(
        decisionType,
        drunkShownRole?.value.orEmpty(),
        shownCharacter?.value.orEmpty(),
        candidateAlignmentPattern.orEmpty(),
        candidateSeatDistance?.toString().orEmpty(),
        redHerringRole?.value.orEmpty(),
        demonBluffs.map { it.value }.sorted().joinToString(","),
    ).joinToString("|")

    companion object {
        fun fromSetupPlan(game: GameState, plan: RecommendationPlan): HistoricalClueSignature {
            val informationObservation = plan.observations.firstOrNull()
            val seats = informationObservation?.candidateSeats
                ?.sorted()
                ?: plan.effectSignature.suspectedSeats.sorted()
            val alignmentPattern = seats
                .mapNotNull { game.playerAt(it)?.actualAlignment?.name }
                .sorted()
                .joinToString(",")
                .ifBlank { null }
            val distance = if (seats.size == 2 && game.players.isNotEmpty()) {
                val direct = kotlin.math.abs(seats[0] - seats[1])
                minOf(direct, game.players.size - direct)
            } else {
                null
            }
            return HistoricalClueSignature(
                decisionType = "setup-plan",
                drunkShownRole = informationObservation?.perceivedRole ?: plan.effectSignature.drunkShownRole,
                shownCharacter = informationObservation?.shownRole ?: plan.effectSignature.drunkInvestigatorShownMinion,
                candidateAlignmentPattern = alignmentPattern,
                candidateSeatDistance = distance,
                redHerringRole = plan.effectSignature.redHerringSeat?.let(game::playerAt)?.actualRole,
                demonBluffs = plan.effectSignature.demonBluffs,
            )
        }
    }
}

data class CrossGameHistory(
    val recentSignatures: List<HistoricalClueSignature> = emptyList(),
) {
    init {
        require(recentSignatures.size <= MAX_SAVED_GAMES) { "At most $MAX_SAVED_GAMES games are retained." }
    }

    fun append(signature: HistoricalClueSignature): CrossGameHistory =
        CrossGameHistory((listOf(signature) + recentSignatures).take(MAX_SAVED_GAMES))

    fun digest(): String = recentSignatures.joinToString(";") { it.canonical() }

    companion object {
        const val MAX_SAVED_GAMES = 10
    }
}
