package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.RoleId

data class HistoricalClueSignature(
    val decisionType: String,
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
        shownCharacter?.value.orEmpty(),
        candidateAlignmentPattern.orEmpty(),
        candidateSeatDistance?.toString().orEmpty(),
        redHerringRole?.value.orEmpty(),
        demonBluffs.map { it.value }.sorted().joinToString(","),
    ).joinToString("|")

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
