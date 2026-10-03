package com.codex.campboardgamehost.clocktower.recommendation.sde

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

internal enum class PolicySelectionMethod {
    SEEDED_HASH_V1,
}

internal data class PolicySelection(
    val policyVersion: PolicyVersion,
    val candidateId: String,
    val method: PolicySelectionMethod,
) {
    init {
        require(candidateId.isNotBlank()) { "Selected policy candidate ID cannot be blank." }
    }
}

/**
 * Deterministic, weight-free survivor selector for BEGINNER_CONSERVATIVE_V1.
 *
 * The selector is intentionally independent of incidental evaluation order. It hashes the stable
 * decision identity, game/decision seed, policy version and candidate ID, then chooses the lowest
 * canonical digest among SURVIVOR candidates.
 */
internal object PolicySeededHashSelector {
    fun select(
        candidateIds: List<String>,
        decisionId: String,
        selectionSeed: Long,
        hashPolicyVersion: PolicyVersion,
    ): String {
        require(candidateIds.isNotEmpty()) { "Seeded policy selection requires candidates." }
        require(candidateIds.all(String::isNotBlank)) { "Seeded policy candidate IDs cannot be blank." }
        require(candidateIds.distinct().size == candidateIds.size) { "Seeded policy candidate IDs must be unique." }
        require(decisionId.isNotBlank()) { "Seeded policy selection decision ID cannot be blank." }

        return candidateIds.minBy { candidateId ->
            digest(
                listOf(
                    selectionSeed.toString(),
                    decisionId,
                    hashPolicyVersion.value,
                    candidateId,
                ).joinToString("|"),
            )
        }
    }

    private fun digest(payload: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}

internal object BeginnerConservativeV1Selector {
    fun select(
        evaluation: BeginnerConservativePolicyEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): PolicySelection? {
        require(decisionId.isNotBlank()) { "Policy selection decision ID cannot be blank." }

        if (evaluation is BeginnerConservativePolicyEvaluation.Deferred) return null

        evaluation as BeginnerConservativePolicyEvaluation.Ready
        val survivorIds = evaluation.evaluations
            .filter { it.disposition == PolicyDisposition.SURVIVOR }
            .map(PolicyEvaluation::candidateId)
        require(survivorIds.isNotEmpty()) { "Ready policy evaluation must expose at least one survivor." }

        val selected = PolicySeededHashSelector.select(
            candidateIds = survivorIds,
            decisionId = decisionId,
            selectionSeed = selectionSeed,
            hashPolicyVersion = evaluation.policyVersion,
        )
        return PolicySelection(
            policyVersion = evaluation.policyVersion,
            candidateId = selected,
            method = PolicySelectionMethod.SEEDED_HASH_V1,
        )
    }
}
