package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationDisplayDomain
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics

/** One surface-valid first-night numeric output annotated against healthy role truth. */
internal data class FirstNightNumericLegalCandidate(
    val candidateId: String,
    val value: Int,
    val semanticTruth: SemanticTruth,
) {
    init {
        require(candidateId == "value-$value")
        require(value >= 0)
        require(semanticTruth == SemanticTruth.TRUE || semanticTruth == SemanticTruth.FALSE)
    }
}

/**
 * Complete selectable numeric domain.
 *
 * Rules own the surface range and healthy truth space. Reliability only controls whether
 * well-formed false values are admitted.
 */
internal object FirstNightNumericLegalDomain {
    fun generate(
        game: GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        reliability: ReliabilityState,
    ): List<FirstNightNumericLegalCandidate> {
        val displayDomain = FirstNightNumericInformationDisplayDomain.domain(game, sourceSeat)
            ?: return emptyList()
        if (displayDomain.abilityRole != abilityRole) return emptyList()

        val truthfulValues = FirstNightNumericInformationSemantics
            .healthyTruthValues(game, sourceSeat)

        return displayDomain.values.mapNotNull { value ->
            val truthful = value in truthfulValues
            if (reliability == ReliabilityState.RELIABLE && !truthful) {
                return@mapNotNull null
            }
            FirstNightNumericLegalCandidate(
                candidateId = "value-$value",
                value = value,
                semanticTruth = if (truthful) SemanticTruth.TRUE else SemanticTruth.FALSE,
            )
        }
    }
}
