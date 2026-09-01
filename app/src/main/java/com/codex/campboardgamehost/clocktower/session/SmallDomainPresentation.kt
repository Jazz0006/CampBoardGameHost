package com.codex.campboardgamehost.clocktower.session

/**
 * Stable presentation projection for genuinely small legal outcome domains.
 *
 * Legality is owned entirely by [legalCandidates]. Recommendation ordering may choose which legal
 * candidate is primary, but it cannot add, remove, or replace selectable outcomes. When no
 * recommendation matches the legal domain, every legal candidate remains directly selectable.
 */
internal data class SmallDomainPresentation<T> private constructor(
    val primary: T?,
    val remaining: List<T>,
    val selectable: List<T>,
) {
    companion object {
        fun <T> from(
            legalCandidates: List<T>,
            recommendedCandidateIds: List<String>,
            candidateId: (T) -> String,
        ): SmallDomainPresentation<T> {
            val legalIds = legalCandidates.map(candidateId)
            require(legalIds.distinct().size == legalIds.size) {
                "Small-domain legal candidate identities must be unique."
            }

            val legalById = legalCandidates.associateBy(candidateId)
            val primary = recommendedCandidateIds
                .asSequence()
                .mapNotNull(legalById::get)
                .firstOrNull()

            return SmallDomainPresentation(
                primary = primary,
                remaining = if (primary == null) {
                    legalCandidates
                } else {
                    legalCandidates.filter { candidateId(it) != candidateId(primary) }
                },
                selectable = legalCandidates,
            )
        }
    }
}
