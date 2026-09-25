package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource

/** Policy-neutral persisted/replay diagnostic code for a decision-level deferral. */
internal data class PolicyDeferralCode(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Policy deferral code cannot be blank." }
    }
}

/**
 * Policy-version-neutral trace snapshot.
 *
 * Production policy implementations may have version-specific wrappers, but DecisionTrace must not
 * bake BEGINNER_CONSERVATIVE_V1's wrapper type into the durable replay schema.
 */
internal sealed interface DecisionTracePolicySnapshot {
    val policyVersion: PolicyVersion
    val candidateIds: List<String>

    data class Ready(
        override val policyVersion: PolicyVersion,
        val evaluations: List<PolicyEvaluation>,
        val limitations: Set<PolicyLimitationCode> = emptySet(),
    ) : DecisionTracePolicySnapshot {
        init {
            require(evaluations.isNotEmpty()) { "Ready trace policy snapshot requires candidates." }
            require(evaluations.map(PolicyEvaluation::candidateId).distinct().size == evaluations.size) {
                "Ready trace policy candidate IDs must be unique."
            }
            require(evaluations.all { it.policyVersion == policyVersion }) {
                "Ready trace policy evaluations must use the snapshot policy version."
            }
            require(evaluations.any { it.disposition == PolicyDisposition.SURVIVOR }) {
                "Ready trace policy snapshot requires at least one survivor."
            }
        }

        override val candidateIds: List<String>
            get() = evaluations.map(PolicyEvaluation::candidateId)
    }

    data class Deferred(
        override val policyVersion: PolicyVersion,
        override val candidateIds: List<String>,
        val reasons: Set<PolicyDeferralCode>,
    ) : DecisionTracePolicySnapshot {
        init {
            require(candidateIds.isNotEmpty()) { "Deferred trace policy snapshot requires candidates." }
            require(candidateIds.all(String::isNotBlank) && candidateIds.distinct().size == candidateIds.size) {
                "Deferred trace policy candidate IDs must be non-blank and unique."
            }
            require(reasons.isNotEmpty()) { "Deferred trace policy snapshot requires reasons." }
        }
    }
}

internal data class DecisionTraceOverrideReason(
    val code: String? = null,
    val text: String? = null,
) {
    init {
        require(code == null || code.isNotBlank()) { "Override reason code cannot be blank." }
        require(text == null || text.isNotBlank()) { "Override reason text cannot be blank." }
        require(code != null || text != null) { "Override reason requires a structured code or text." }
    }
}

/**
 * Actual authoritative choice state. Shadow creation starts Pending; SDE-3C4 correlates a successful
 * authoritative confirmation later without allowing this diagnostic record to perform that commit.
 */
internal sealed interface DecisionTraceActualChoice {
    object Pending : DecisionTraceActualChoice

    data class Committed(
        val candidateId: String,
        val source: InformationDecisionSource,
        val manualOverride: Boolean,
        val overrideReason: DecisionTraceOverrideReason? = null,
    ) : DecisionTraceActualChoice {
        init {
            require(candidateId.isNotBlank()) { "Committed trace candidate ID cannot be blank." }
            require(!manualOverride || source == InformationDecisionSource.MANUAL) {
                "A trace manual override must come from a manual authoritative choice."
            }
            require(overrideReason == null || manualOverride) {
                "Override rationale is only valid for an explicit manual override."
            }
        }
    }
}

/**
 * Versioned diagnostic/replay record for one Storyteller-controlled decision.
 *
 * This is intentionally not canonical game state. The history reference points back to the
 * ActionFactTimeline / EpistemicObservationLog-owned committed prefix instead of copying that
 * mechanical/epistemic payload into a second history owner.
 */
internal data class DecisionTrace(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val evidenceCheckpoint: EvidenceCheckpointId,
    val decisionId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceRevision: InformationDecisionRevision,
    val historyPrefixRef: SdeHistoricalPrefixRef,
    val legalCandidateIds: List<String>,
    val featureEvaluation: DecisionFeatureEvaluation,
    val policySnapshot: DecisionTracePolicySnapshot,
    val policySelection: PolicySelection?,
    val actualChoice: DecisionTraceActualChoice = DecisionTraceActualChoice.Pending,
) {
    init {
        require(schemaVersion == CURRENT_SCHEMA_VERSION) {
            "Unsupported in-memory DecisionTrace schema version $schemaVersion."
        }
        require(decisionId.isNotBlank()) { "DecisionTrace decision ID cannot be blank." }
        require(legalCandidateIds.isNotEmpty()) { "DecisionTrace requires legal candidates." }
        require(legalCandidateIds.all(String::isNotBlank) && legalCandidateIds.distinct().size == legalCandidateIds.size) {
            "DecisionTrace legal candidate IDs must be non-blank and unique."
        }
        require(featureEvaluation.candidateIds == legalCandidateIds) {
            "DecisionTrace features must preserve the complete legal candidate order."
        }
        require(policySnapshot.candidateIds == legalCandidateIds) {
            "DecisionTrace policy snapshot must preserve the complete legal candidate order."
        }

        when (policySnapshot) {
            is DecisionTracePolicySnapshot.Ready -> {
                val selection = requireNotNull(policySelection) {
                    "Ready DecisionTrace policy snapshot requires a shadow recommendation."
                }
                require(selection.policyVersion == policySnapshot.policyVersion) {
                    "DecisionTrace shadow recommendation must use the traced policy version."
                }
                val selectedEvaluation = policySnapshot.evaluations.singleOrNull {
                    it.candidateId == selection.candidateId
                }
                require(selectedEvaluation?.disposition == PolicyDisposition.SURVIVOR) {
                    "DecisionTrace shadow recommendation must select a policy survivor."
                }
            }

            is DecisionTracePolicySnapshot.Deferred ->
                require(policySelection == null) {
                    "Deferred DecisionTrace policy snapshot cannot carry a shadow recommendation."
                }
        }

        if (actualChoice is DecisionTraceActualChoice.Committed) {
            require(actualChoice.candidateId in legalCandidateIds) {
                "Committed DecisionTrace choice must belong to the traced legal candidate set."
            }
        }
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1
    }
}

internal object DecisionTraceFactory {
    fun fromStructuredShadow(
        shadow: StructuredInformationShadowEvaluation,
    ): DecisionTrace {
        val definition = StorytellerPolicyDefinitions.BEGINNER_CONSERVATIVE_V1
        require(shadow.policyEvaluation.policyVersion == definition.policyVersion) {
            "Structured shadow trace policy version must match the frozen V1 definition."
        }
        require(
            shadow.policySelection == null ||
                shadow.policySelection.method == definition.selectionMethod
        ) {
            "Structured shadow trace selection method must match the frozen V1 definition."
        }

        val candidates = shadow.sdeCandidates
        require(candidates.isNotEmpty()) { "Structured shadow trace requires SDE candidates." }

        val decisionIds = candidates.map(SdeDecisionCandidate::decisionId).distinct()
        require(decisionIds.size == 1) { "Structured shadow trace requires one decision identity." }

        val lifecycleStages = candidates.map(SdeDecisionCandidate::lifecycleStage).distinct()
        require(lifecycleStages.size == 1) { "Structured shadow trace requires one lifecycle stage." }

        val sourceRevisions = candidates.map(SdeDecisionCandidate::sourceRevision).distinct()
        require(sourceRevisions.size == 1) { "Structured shadow trace requires one source revision." }

        val historyPrefixes = candidates.map(SdeDecisionCandidate::historyPrefixRef).distinct()
        require(historyPrefixes.size == 1) { "Structured shadow trace requires one committed-prefix reference." }

        val legalCandidateIds = shadow.informationSnapshot.legalCandidateIds
        require(candidates.map(SdeDecisionCandidate::candidateId) == legalCandidateIds) {
            "Structured shadow trace must preserve upstream legal-candidate order."
        }

        return DecisionTrace(
            evidenceCheckpoint = definition.evidenceCheckpoint,
            decisionId = decisionIds.single(),
            lifecycleStage = lifecycleStages.single(),
            sourceRevision = sourceRevisions.single(),
            historyPrefixRef = historyPrefixes.single(),
            legalCandidateIds = legalCandidateIds,
            featureEvaluation = shadow.featureEvaluation,
            policySnapshot = shadow.policyEvaluation.toDecisionTracePolicySnapshot(),
            policySelection = shadow.policySelection,
        )
    }

}

internal fun BeginnerConservativePolicyEvaluation.toDecisionTracePolicySnapshot(): DecisionTracePolicySnapshot =
    when (this) {
        is BeginnerConservativePolicyEvaluation.Ready ->
            DecisionTracePolicySnapshot.Ready(
                policyVersion = policyVersion,
                evaluations = evaluations,
                limitations = limitations,
            )

        is BeginnerConservativePolicyEvaluation.Deferred ->
            DecisionTracePolicySnapshot.Deferred(
                policyVersion = policyVersion,
                candidateIds = candidateIds,
                reasons = reasons.mapTo(linkedSetOf()) { it.toDecisionTraceDeferralCode() },
            )
    }

private fun BeginnerConservativePolicyDeferralReason.toDecisionTraceDeferralCode(): PolicyDeferralCode =
    PolicyDeferralCode(
        when (this) {
            BeginnerConservativePolicyDeferralReason.UPSTREAM_FEATURE_EVALUATION_DEFERRED ->
                "beginner-conservative-v1.upstream-feature-evaluation-deferred"
            BeginnerConservativePolicyDeferralReason.STRATEGIC_FEATURE_UNAVAILABLE ->
                "beginner-conservative-v1.strategic-feature-unavailable"
            BeginnerConservativePolicyDeferralReason.STRATEGIC_BASELINE_UNDEFINED ->
                "beginner-conservative-v1.strategic-baseline-undefined"
            BeginnerConservativePolicyDeferralReason.NO_NON_CONTRADICTORY_SURVIVOR ->
                "beginner-conservative-v1.no-non-contradictory-survivor"
        },
    )
