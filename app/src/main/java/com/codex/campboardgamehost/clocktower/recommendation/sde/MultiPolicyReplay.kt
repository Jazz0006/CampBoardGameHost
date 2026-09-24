package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * Policy-neutral replay input extracted from a freshly recomputed structured shadow evaluation.
 *
 * This contains derived feature evidence only for the current replay run. Canonical mechanical and
 * epistemic history remains owned by the referenced Global prefix.
 */
internal data class MultiPolicyReplayInput(
    val decisionId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceRevision: InformationDecisionRevision,
    val historyPrefixRef: SdeHistoricalPrefixRef.Global,
    val legalCandidateIds: List<String>,
    val featureEvaluation: DecisionFeatureEvaluation,
    val selectionSeed: Long,
) {
    init {
        require(decisionId.isNotBlank()) { "Multi-policy replay decision ID cannot be blank." }
        require(legalCandidateIds.isNotEmpty()) { "Multi-policy replay requires legal candidates." }
        require(legalCandidateIds.all(String::isNotBlank) && legalCandidateIds.distinct().size == legalCandidateIds.size) {
            "Multi-policy replay candidate IDs must be non-blank and unique."
        }
        require(featureEvaluation.candidateIds == legalCandidateIds) {
            "Multi-policy replay features must preserve the complete legal-candidate order."
        }
    }

    companion object {
        fun fromStructuredShadow(
            shadow: StructuredInformationShadowEvaluation,
        ): MultiPolicyReplayInput {
            val candidates = shadow.sdeCandidates
            require(candidates.isNotEmpty()) { "Structured replay requires SDE candidates." }

            val decisionIds = candidates.map(SdeDecisionCandidate::decisionId).distinct()
            require(decisionIds.size == 1) { "Structured replay requires one decision identity." }

            val lifecycleStages = candidates.map(SdeDecisionCandidate::lifecycleStage).distinct()
            require(lifecycleStages.size == 1) { "Structured replay requires one lifecycle stage." }

            val sourceRevisions = candidates.map(SdeDecisionCandidate::sourceRevision).distinct()
            require(sourceRevisions.size == 1) { "Structured replay requires one source revision." }

            val historyPrefixes = candidates.map(SdeDecisionCandidate::historyPrefixRef).distinct()
            require(historyPrefixes.size == 1) { "Structured replay requires one committed history prefix." }
            val globalPrefix = historyPrefixes.single() as? SdeHistoricalPrefixRef.Global
                ?: throw IllegalArgumentException(
                    "Structured multi-policy replay requires a canonical Global history prefix.",
                )

            val legalCandidateIds = shadow.informationSnapshot.legalCandidateIds
            require(candidates.map(SdeDecisionCandidate::candidateId) == legalCandidateIds) {
                "Structured replay must preserve upstream legal-candidate order."
            }

            return MultiPolicyReplayInput(
                decisionId = decisionIds.single(),
                lifecycleStage = lifecycleStages.single(),
                sourceRevision = sourceRevisions.single(),
                historyPrefixRef = globalPrefix,
                legalCandidateIds = legalCandidateIds,
                featureEvaluation = shadow.featureEvaluation,
                selectionSeed = shadow.selectionSeed,
            )
        }
    }
}

/**
 * Pure multi-policy replay over one freshly recomputed canonical decision input.
 *
 * The source historical trace contributes identity cross-checks and authoritative actual-choice
 * metadata only. Its persisted feature snapshot is deliberately not used as policy input.
 */
internal object MultiPolicyReplayEngine {
    fun replay(
        sourceTrace: DecisionTrace,
        recomputedInput: MultiPolicyReplayInput,
        policyVersions: List<PolicyVersion>,
        evidenceCheckpoints: Map<PolicyVersion, EvidenceCheckpointId>,
        registry: DecisionPolicyReplayRegistry = DecisionPolicyReplayRegistry.production(),
    ): List<DecisionTrace> {
        require(policyVersions.isNotEmpty()) {
            "Multi-policy replay requires at least one explicit policy version."
        }
        require(policyVersions.distinct().size == policyVersions.size) {
            "Multi-policy replay policy versions must be unique."
        }
        require(evidenceCheckpoints.keys == policyVersions.toSet()) {
            "Multi-policy replay requires exactly one evidence checkpoint per requested policy version."
        }

        require(sourceTrace.decisionId == recomputedInput.decisionId) {
            "Replay decision identity does not match the source historical trace."
        }
        require(sourceTrace.lifecycleStage == recomputedInput.lifecycleStage) {
            "Replay lifecycle does not match the source historical trace."
        }
        require(sourceTrace.sourceRevision == recomputedInput.sourceRevision) {
            "Replay source revision does not match the source historical trace."
        }
        require(sourceTrace.historyPrefixRef == recomputedInput.historyPrefixRef) {
            "Replay canonical history prefix does not match the source historical trace."
        }
        require(sourceTrace.legalCandidateIds == recomputedInput.legalCandidateIds) {
            "Replay legal candidate domain does not match the source historical trace."
        }

        return policyVersions.map { policyVersion ->
            val runner = registry.requireRunner(policyVersion)
            require(runner.policyVersion == policyVersion) {
                "Policy replay registry returned a mismatched runner."
            }
            val run = runner.run(
                featureEvaluation = recomputedInput.featureEvaluation,
                decisionId = recomputedInput.decisionId,
                selectionSeed = recomputedInput.selectionSeed,
            )
            require(run.policySnapshot.policyVersion == policyVersion) {
                "Policy replay output version does not match the requested policy version."
            }
            require(run.policySnapshot.candidateIds == recomputedInput.legalCandidateIds) {
                "Policy replay output must preserve the complete legal-candidate order."
            }
            require(run.policySelection == null || run.policySelection.policyVersion == policyVersion) {
                "Policy replay selection version does not match the requested policy version."
            }

            DecisionTrace(
                evidenceCheckpoint = evidenceCheckpoints.getValue(policyVersion),
                decisionId = recomputedInput.decisionId,
                lifecycleStage = recomputedInput.lifecycleStage,
                sourceRevision = recomputedInput.sourceRevision,
                historyPrefixRef = recomputedInput.historyPrefixRef,
                legalCandidateIds = recomputedInput.legalCandidateIds,
                featureEvaluation = recomputedInput.featureEvaluation,
                policySnapshot = run.policySnapshot,
                policySelection = run.policySelection,
                actualChoice = sourceTrace.actualChoice,
            )
        }
    }
}
