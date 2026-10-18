package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/** Structure-only pair presentation. No Rules candidate IDs, legality, or inferred registration. */
internal object ClocktowerPairManualAuthority {
    /** Interpret already-authoritative options once, before they reach the Manual renderer. */
    fun selectionPresentation(options: List<ClocktowerDisplayOption>): ClocktowerPairManualPresentation {
        val pairs = mutableListOf<ClocktowerPairManualCandidate>()
        var zeroCase: ClocktowerDisplayOption? = null
        options.forEach { option ->
            val key = option.pairInformationKeyOrNull() ?: return@forEach
            if (key.shownRole != null) {
                pairs += ClocktowerPairManualCandidate(option, key.shownRole.value, key.candidateSeats)
            } else if (zeroCase == null) {
                zeroCase = option
            }
        }
        return ClocktowerPairManualPresentation(options.toList(), pairs, zeroCase)
    }

    /**
     * Maps a recommendation projection back into the complete Manual legal presentation using only
     * its structured pair proposition. Recommendation labels and ranking metadata are presentation
     * concerns and are deliberately ignored here.
     */
    fun canonicalManualOption(
        presentation: ClocktowerPairManualPresentation,
        recommendedOption: ClocktowerDisplayOption?,
    ): ClocktowerDisplayOption? {
        val key = recommendedOption?.pairInformationKeyOrNull() ?: return null
        return presentation.canonicalOptionFor(key)
    }

}

private data class PairInformationPresentationKey(
    val shownRole: RoleId?,
    val candidateSeats: List<Int>,
)

private fun ClocktowerDisplayOption.pairInformationKeyOrNull(): PairInformationPresentationKey? {
    return when (val structured = proposition) {
        is InformationProposition.AnyOf -> {
            val roleAt = structured.alternatives.map { alternative ->
                alternative as? InformationProposition.RoleAt ?: return null
            }
            val shownRole = roleAt.map { it.role }.distinct().singleOrNull() ?: return null
            val seats = roleAt.map { it.seat }.distinct().sorted()
            if (seats.size != 2) return null
            PairInformationPresentationKey(
                shownRole = shownRole,
                candidateSeats = seats,
            )
        }

        is InformationProposition.AllOf -> {
            val roleInPlay = structured.propositions.map { proposition ->
                proposition as? InformationProposition.RoleInPlay ?: return null
            }
            if (roleInPlay.isEmpty() || roleInPlay.any { it.inPlay }) return null
            PairInformationPresentationKey(
                shownRole = null,
                candidateSeats = emptyList(),
            )
        }

        else -> null
    }
}

/** Original options participate in equality to preserve candidate-change selection resets,
 * including changes to ignored malformed options. They are not exposed to the renderer.
 */
internal data class ClocktowerPairManualPresentation(
    private val sourceOptions: List<ClocktowerDisplayOption>,
    val candidates: List<ClocktowerPairManualCandidate>,
    val zeroCaseOption: ClocktowerDisplayOption?,
)

private fun ClocktowerPairManualPresentation.canonicalOptionFor(
    key: PairInformationPresentationKey,
): ClocktowerDisplayOption? = if (key.shownRole == null) {
    zeroCaseOption
} else {
    candidates.firstOrNull { candidate ->
        candidate.roleId == key.shownRole.value && candidate.seats == key.candidateSeats
    }?.option
}

internal data class ClocktowerPairManualCandidate(
    val option: ClocktowerDisplayOption,
    val roleId: String,
    val seats: List<Int>,
)
