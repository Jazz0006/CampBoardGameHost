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

        val selected = survivorIds.minBy { candidateId ->
            digest(
                listOf(
                    selectionSeed.toString(),
                    decisionId,
                    evaluation.policyVersion.value,
                    candidateId,
                ).joinToString("|"),
            )
        }
        return PolicySelection(
            policyVersion = evaluation.policyVersion,
            candidateId = selected,
            method = PolicySelectionMethod.SEEDED_HASH_V1,
        )
    }

    private fun digest(payload: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
