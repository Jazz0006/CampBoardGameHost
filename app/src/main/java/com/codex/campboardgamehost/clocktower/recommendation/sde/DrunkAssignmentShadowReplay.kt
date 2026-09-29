package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * Freshly recomputed input for versioned Drunk-assignment policy experiments.
 *
 * The source historical DecisionTrace is used only for identity/domain/actual-choice cross-checks.
 * Its ordinary DecisionFeatureEvaluation is deliberately not an input here.
 */
internal data class DrunkAssignmentShadowReplayInput(
    val decisionId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceRevision: InformationDecisionRevision,
    val historyPrefixRef: SdeHistoricalPrefixRef.Global,
    val legalCandidateIds: List<String>,
    val featureEvaluation: DrunkAssignmentFeatureEvaluation,
    val selectionSeed: Long,
) {
    init {
        require(decisionId.isNotBlank()) {
            "Drunk-assignment replay decision ID cannot be blank."
        }
        require(lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
            "Drunk-assignment replay must remain at SetupPrecommit lifecycle."
        }
        require(legalCandidateIds.isNotEmpty()) {
            "Drunk-assignment replay requires legal candidates."
        }
        require(
            legalCandidateIds.all(String::isNotBlank) &&
                legalCandidateIds.distinct().size == legalCandidateIds.size,
        ) {
            "Drunk-assignment replay candidate IDs must be non-blank and unique."
        }
        require(featureEvaluation.candidateIds == legalCandidateIds) {
            "Drunk-assignment replay features must preserve complete legal-candidate order."
        }
    }

    companion object {
        fun fromShadow(
            shadow: DrunkSetupShadowEvaluation,
        ): DrunkAssignmentShadowReplayInput {
            val trace = shadow.decisionTrace
            val globalPrefix = trace.historyPrefixRef as? SdeHistoricalPrefixRef.Global
                ?: throw IllegalArgumentException(
                    "Drunk-assignment shadow replay requires a canonical Global history prefix.",
                )
            require(
                shadow.drunkAssignmentFeatureEvaluation.candidateIds ==
                    trace.legalCandidateIds,
            ) {
                "Drunk-assignment shadow replay must preserve traced legal-candidate order."
            }

            return DrunkAssignmentShadowReplayInput(
                decisionId = trace.decisionId,
                lifecycleStage = trace.lifecycleStage,
                sourceRevision = trace.sourceRevision,
                historyPrefixRef = globalPrefix,
                legalCandidateIds = trace.legalCandidateIds,
                featureEvaluation = shadow.drunkAssignmentFeatureEvaluation,
                selectionSeed = shadow.replayInput.selectionSeed,
            )
        }
    }
}

/**
 * Diagnostic output for one Drunk-assignment policy experiment replay.
 *
 * This is not canonical setup state and does not replace DecisionTrace. It carries the dedicated
 * Drunk feature surface that DecisionTrace intentionally does not own.
 */
internal data class DrunkAssignmentShadowReplayRecord(
    val evidenceCheckpoint: EvidenceCheckpointId,
    val decisionId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceRevision: InformationDecisionRevision,
    val historyPrefixRef: SdeHistoricalPrefixRef.Global,
    val legalCandidateIds: List<String>,
    val featureEvaluation: DrunkAssignmentFeatureEvaluation,
    val policySnapshot: DecisionTracePolicySnapshot,
    val policySelection: PolicySelection?,
    val actualChoice: DecisionTraceActualChoice,
) {
    init {
        require(decisionId.isNotBlank()) {
            "Drunk-assignment replay record decision ID cannot be blank."
        }
        require(lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
            "Drunk-assignment replay record must remain at SetupPrecommit lifecycle."
        }
        require(legalCandidateIds.isNotEmpty()) {
            "Drunk-assignment replay record requires legal candidates."
        }
        require(
            legalCandidateIds.all(String::isNotBlank) &&
                legalCandidateIds.distinct().size == legalCandidateIds.size,
        ) {
            "Drunk-assignment replay record candidate IDs must be non-blank and unique."
        }
        require(featureEvaluation.candidateIds == legalCandidateIds) {
            "Drunk-assignment replay record features must preserve legal-candidate order."
        }
        require(policySnapshot.candidateIds == legalCandidateIds) {
            "Drunk-assignment replay record policy snapshot must preserve legal-candidate order."
        }

        when (policySnapshot) {
            is DecisionTracePolicySnapshot.Ready -> {
                val selection = requireNotNull(policySelection) {
                    "Ready Drunk-assignment replay policy requires a selection."
                }
                require(selection.policyVersion == policySnapshot.policyVersion) {
                    "Drunk-assignment replay selection must use the replayed policy version."
                }
                val selected = policySnapshot.evaluations.singleOrNull {
                    it.candidateId == selection.candidateId
                }
                require(selected?.disposition == PolicyDisposition.SURVIVOR) {
                    "Drunk-assignment replay selection must choose a policy survivor."
                }
            }

            is DecisionTracePolicySnapshot.Deferred ->
                require(policySelection == null) {
                    "Deferred Drunk-assignment replay policy cannot carry a selection."
                }
        }

        if (actualChoice is DecisionTraceActualChoice.Committed) {
            require(actualChoice.candidateId in legalCandidateIds) {
                "Historical Drunk-assignment choice must belong to the replayed legal domain."
            }
        }
    }
}

/**
 * Pure replay engine for dedicated Drunk-assignment feature experiments.
 *
 * This engine is intentionally parallel to MultiPolicyReplayEngine. It never calls the ordinary
 * production replay registry and never writes canonical setup/session state.
 */
internal object DrunkAssignmentShadowReplayEngine {
    fun replay(
        sourceTrace: DecisionTrace,
        recomputedInput: DrunkAssignmentShadowReplayInput,
        policyVersions: List<PolicyVersion>,
        registry: DrunkAssignmentPolicyExperimentRegistry =
            DrunkAssignmentPolicyExperimentRegistry.experimental(),
    ): List<DrunkAssignmentShadowReplayRecord> {
        require(policyVersions.isNotEmpty()) {
            "Drunk-assignment replay requires at least one explicit experiment policy version."
        }
        require(policyVersions.distinct().size == policyVersions.size) {
            "Drunk-assignment replay policy versions must be unique."
        }

        require(sourceTrace.decisionId == recomputedInput.decisionId) {
            "Drunk-assignment replay decision identity does not match the source trace."
        }
        require(sourceTrace.lifecycleStage == recomputedInput.lifecycleStage) {
            "Drunk-assignment replay lifecycle does not match the source trace."
        }
        require(sourceTrace.sourceRevision == recomputedInput.sourceRevision) {
            "Drunk-assignment replay source revision does not match the source trace."
        }
        require(sourceTrace.historyPrefixRef == recomputedInput.historyPrefixRef) {
            "Drunk-assignment replay history prefix does not match the source trace."
        }
        require(sourceTrace.legalCandidateIds == recomputedInput.legalCandidateIds) {
            "Drunk-assignment replay legal candidate domain does not match the source trace."
        }

        return policyVersions.map { policyVersion ->
            val runner = registry.requireRunner(policyVersion)
            require(runner.policyVersion == policyVersion) {
                "Drunk-assignment experiment registry returned a mismatched runner."
            }

            val run = runner.run(
                featureEvaluation = recomputedInput.featureEvaluation,
                decisionId = recomputedInput.decisionId,
                selectionSeed = recomputedInput.selectionSeed,
            )
            require(run.policySnapshot.policyVersion == policyVersion) {
                "Drunk-assignment replay output version does not match the requested version."
            }
            require(run.policySnapshot.candidateIds == recomputedInput.legalCandidateIds) {
                "Drunk-assignment replay output must preserve complete legal-candidate order."
            }
            require(
                run.policySelection == null ||
                    run.policySelection.policyVersion == policyVersion,
            ) {
                "Drunk-assignment replay selection version does not match the requested version."
            }
            require(
                run.policySelection == null ||
                    run.policySelection.method == runner.definition.selectionMethod,
            ) {
                "Drunk-assignment replay selection method does not match the experiment definition."
            }

            DrunkAssignmentShadowReplayRecord(
                evidenceCheckpoint = runner.definition.evidenceCheckpoint,
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
