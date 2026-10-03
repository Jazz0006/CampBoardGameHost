package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

internal enum class BeginnerConservativeV2SelectionMode {
    V1_FALLBACK,
    PREFERRED_BAND,
}

internal object BeginnerConservativeV2PolicyReasons {
    val PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES =
        PolicyReasonCode(
            PairInformationFutureFlexibilityReasonCodes
                .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
        )
}

/**
 * Versioned V2 wrapper over the frozen V1 hard-safety result.
 *
 * V2 may narrow only the final V1 survivor band on the admitted, evidence-backed pair surface.
 * Rejections are inherited exactly from V1. Missing scope/feature evidence or a non-discriminating
 * preference falls back to V1 candidate dispositions/equivalence without inventing a delta.
 */
internal sealed interface BeginnerConservativeV2PolicyEvaluation {
    val policyVersion: PolicyVersion
    val candidateIds: List<String>

    data class Ready(
        val evaluations: List<PolicyEvaluation>,
        val limitations: Set<PolicyLimitationCode> = emptySet(),
        val preferenceApplied: Boolean,
        val selectionMode: BeginnerConservativeV2SelectionMode,
    ) : BeginnerConservativeV2PolicyEvaluation {
        override val policyVersion: PolicyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2

        init {
            require(evaluations.isNotEmpty()) { "Ready V2 policy evaluation requires candidates." }
            require(evaluations.map(PolicyEvaluation::candidateId).distinct().size == evaluations.size) {
                "Ready V2 policy candidate IDs must be unique."
            }
            require(evaluations.all { it.policyVersion == policyVersion }) {
                "All V2 candidate evaluations must use BEGINNER_CONSERVATIVE_V2."
            }
            require(evaluations.any { it.disposition == PolicyDisposition.SURVIVOR }) {
                "Ready V2 policy evaluation requires at least one survivor."
            }
            require(preferenceApplied == (selectionMode == BeginnerConservativeV2SelectionMode.PREFERRED_BAND)) {
                "V2 preferenceApplied must match the selection mode."
            }
        }

        override val candidateIds: List<String>
            get() = evaluations.map(PolicyEvaluation::candidateId)
    }

    data class Deferred(
        override val candidateIds: List<String>,
        val reasons: Set<BeginnerConservativePolicyDeferralReason>,
    ) : BeginnerConservativeV2PolicyEvaluation {
        override val policyVersion: PolicyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2

        init {
            require(candidateIds.isNotEmpty()) { "Deferred V2 policy evaluation requires candidates." }
            require(candidateIds.all(String::isNotBlank) && candidateIds.distinct().size == candidateIds.size) {
                "Deferred V2 policy candidate IDs must be non-blank and unique."
            }
            require(reasons.isNotEmpty()) { "Deferred V2 policy evaluation requires reasons." }
        }
    }
}

/** C5-C evidence-bounded weak preference over the immutable V1 survivor band. */
internal object BeginnerConservativeV2Policy {
    private val troubleBrewing = ScriptId("trouble_brewing")
    private val admittedReason =
        PairInformationFutureFlexibilityReasonCodes
            .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES

    fun evaluate(
        featureEvaluation: DecisionFeatureEvaluation,
        scope: DecisionPolicyReplayScope,
    ): BeginnerConservativeV2PolicyEvaluation {
        val v1 = BeginnerConservativeV1Policy.evaluate(featureEvaluation)
        if (v1 is BeginnerConservativePolicyEvaluation.Deferred) {
            return BeginnerConservativeV2PolicyEvaluation.Deferred(
                candidateIds = v1.candidateIds,
                reasons = v1.reasons,
            )
        }

        v1 as BeginnerConservativePolicyEvaluation.Ready
        if (!scope.isAdmittedPairSurface()) {
            return v1Fallback(v1)
        }

        featureEvaluation as DecisionFeatureEvaluation.Ready
        val futureByCandidateId = linkedMapOf<String, FutureFlexibilityFeatures>()
        featureEvaluation.candidates.forEach { candidate ->
            val future = when (val projection = candidate.features.futureFlexibility) {
                is FeatureProjection.Projected -> projection.value
                is FeatureProjection.Unavailable -> return v1Fallback(v1)
            }
            futureByCandidateId[candidate.candidateId] = future
        }

        val v1SurvivorIds = v1.evaluations
            .filter { it.disposition == PolicyDisposition.SURVIVOR }
            .map(PolicyEvaluation::candidateId)
        val preferredSurvivorIds = v1SurvivorIds.filter { candidateId ->
            admittedReason in futureByCandidateId.getValue(candidateId).reasonCodes
        }

        if (preferredSurvivorIds.isEmpty() || preferredSurvivorIds.size == v1SurvivorIds.size) {
            return v1Fallback(v1)
        }

        val preferredSet = preferredSurvivorIds.toCollection(linkedSetOf())
        val equivalence = if (preferredSurvivorIds.size == 1) {
            PolicyEquivalenceState.Unique
        } else {
            PolicyEquivalenceState.Tied(preferredSet)
        }

        val v1ById = v1.evaluations.associateBy(PolicyEvaluation::candidateId)
        return BeginnerConservativeV2PolicyEvaluation.Ready(
            evaluations = v1.candidateIds.map { candidateId ->
                val prior = v1ById.getValue(candidateId)
                when {
                    prior.disposition == PolicyDisposition.REJECTED ->
                        prior.copy(policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2)

                    candidateId in preferredSet ->
                        PolicyEvaluation(
                            candidateId = candidateId,
                            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2,
                            disposition = PolicyDisposition.SURVIVOR,
                            softPreferenceReasons = setOf(
                                BeginnerConservativeV2PolicyReasons
                                    .PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
                            ),
                            equivalenceState = equivalence,
                        )

                    else ->
                        PolicyEvaluation(
                            candidateId = candidateId,
                            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2,
                            disposition = PolicyDisposition.ACCEPTED,
                        )
                }
            },
            limitations = v1.limitations,
            preferenceApplied = true,
            selectionMode = BeginnerConservativeV2SelectionMode.PREFERRED_BAND,
        )
    }

    private fun v1Fallback(
        v1: BeginnerConservativePolicyEvaluation.Ready,
    ): BeginnerConservativeV2PolicyEvaluation.Ready =
        BeginnerConservativeV2PolicyEvaluation.Ready(
            evaluations = v1.evaluations.map { evaluation ->
                evaluation.copy(policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2)
            },
            limitations = v1.limitations,
            preferenceApplied = false,
            selectionMode = BeginnerConservativeV2SelectionMode.V1_FALLBACK,
        )

    private fun DecisionPolicyReplayScope.isAdmittedPairSurface(): Boolean {
        val pair = this as? DecisionPolicyReplayScope.PairInformation ?: return false
        return pair.script == troubleBrewing &&
            pair.phase == StorytellerPhase.FIRST_NIGHT &&
            pair.round == 1 &&
            pair.targetType == CharacterType.OUTSIDER &&
            pair.reliability == ReliabilityState.RELIABLE &&
            pair.truthfulLegalOutcomes
    }
}

/**
 * Deterministic V2 selector.
 *
 * V1 fallback deliberately hashes with the V1 policy identity so the selected candidate remains
 * exactly V1-equivalent. The preferred-band path hashes with the V2 identity.
 */
internal object BeginnerConservativeV2Selector {
    fun select(
        evaluation: BeginnerConservativeV2PolicyEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): PolicySelection? {
        require(decisionId.isNotBlank()) { "V2 policy selection decision ID cannot be blank." }
        if (evaluation is BeginnerConservativeV2PolicyEvaluation.Deferred) return null

        evaluation as BeginnerConservativeV2PolicyEvaluation.Ready
        val survivorIds = evaluation.evaluations
            .filter { it.disposition == PolicyDisposition.SURVIVOR }
            .map(PolicyEvaluation::candidateId)
        require(survivorIds.isNotEmpty()) { "Ready V2 policy evaluation requires survivors." }

        val hashVersion = when (evaluation.selectionMode) {
            BeginnerConservativeV2SelectionMode.V1_FALLBACK ->
                PolicyVersions.BEGINNER_CONSERVATIVE_V1

            BeginnerConservativeV2SelectionMode.PREFERRED_BAND ->
                PolicyVersions.BEGINNER_CONSERVATIVE_V2
        }
        val selected = survivorIds.minBy { candidateId ->
            digest(
                listOf(
                    selectionSeed.toString(),
                    decisionId,
                    hashVersion.value,
                    candidateId,
                ).joinToString("|"),
            )
        }
        return PolicySelection(
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V2,
            candidateId = selected,
            method = PolicySelectionMethod.SEEDED_HASH_V1,
        )
    }

    private fun digest(payload: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
