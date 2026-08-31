package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

/** Exact lookup key for optional template-backed setup candidates. */
internal data class TemplateBucketKey(
    val script: ScriptId,
    val playerCount: Int,
) {
    init {
        require(playerCount > 0) { "Template bucket playerCount must be positive." }
    }
}

/**
 * Pure repository of optional template-backed setup candidates.
 *
 * The repository owns only immutable bucket lookup and template identity validation. Dataset parsing,
 * script-specific template validation, diversity scoring, shown identities and persistence remain
 * outside this boundary.
 */
internal class TemplateRepository(
    buckets: Map<TemplateBucketKey, List<SetupCandidate>>,
) : SetupCandidateSource {
    private val candidatesByBucket: Map<TemplateBucketKey, List<SetupCandidate>> =
        buckets.entries.associate { (key, candidates) ->
            val snapshot = candidates.toList()
            validateBucket(key, snapshot)
            key to snapshot.sortedWith(TEMPLATE_ORDER)
        }

    fun find(script: ScriptId, playerCount: Int): List<SetupCandidate> {
        require(playerCount > 0) { "Template lookup playerCount must be positive." }
        return candidatesByBucket[TemplateBucketKey(script, playerCount)] ?: emptyList()
    }

    override fun candidates(request: SetupCandidateRequest): List<SetupCandidate> =
        find(request.script, request.playerCount)

    private fun validateBucket(
        key: TemplateBucketKey,
        candidates: List<SetupCandidate>,
    ) {
        require(candidates.all { it.script == key.script }) {
            "Template repository bucket contains a candidate for another script."
        }
        require(candidates.all { it.playerCount == key.playerCount }) {
            "Template repository bucket contains a candidate with a mismatched player count."
        }
        require(candidates.all { it.provenance.sourceKind == SetupSourceKind.TEMPLATE }) {
            "Template repository cannot contain generated setup candidates."
        }
        require(candidates.all { it.provenance.candidateId != null }) {
            "Template repository candidates require a durable candidateId."
        }

        val identities = candidates.map { candidate ->
            candidate.provenance.providerId to requireNotNull(candidate.provenance.candidateId)
        }
        require(identities.distinct().size == identities.size) {
            "Template repository bucket cannot contain duplicate provider/candidate identities."
        }
    }

    private companion object {
        val TEMPLATE_ORDER = compareBy<SetupCandidate>(
            { it.provenance.providerId },
            { requireNotNull(it.provenance.candidateId) },
            { it.actualRoles.joinToString(separator = "\u0000", transform = { role -> role.value }) },
        )
    }
}
