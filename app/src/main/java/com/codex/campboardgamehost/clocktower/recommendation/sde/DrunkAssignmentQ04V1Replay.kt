package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingDrunkCandidateRef

internal data class DrunkAssignmentQ04V1ReplayInput(
    val decisionContext: DrunkAssignmentDecisionContext,
    val featureEvaluation: DrunkAssignmentFeatureEvaluation,
    val compatibilityCandidate: TroubleBrewingDrunkCandidateRef,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val historyPrefixRef: SdeHistoricalPrefixRef.Global,
) {
    val decisionId: String = decisionContext.decisionId
    val sourceRevision: InformationDecisionRevision = decisionContext.sourceRevision
    val legalCandidateIds: List<String> = decisionContext.legalCandidateIds

    init {
        require(lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
            "Q04 Drunk-assignment replay must remain at SetupPrecommit lifecycle."
        }
        DrunkAssignmentQ04V1Request(
            decisionContext = decisionContext,
            featureEvaluation = featureEvaluation,
            compatibilityCandidate = compatibilityCandidate,
        )
    }

    fun toPolicyRequest(): DrunkAssignmentQ04V1Request =
        DrunkAssignmentQ04V1Request(
            decisionContext = decisionContext,
            featureEvaluation = featureEvaluation,
            compatibilityCandidate = compatibilityCandidate,
        )

    companion object {
        fun fromShadow(
            shadow: DrunkSetupShadowEvaluation,
            compatibilityCandidate: TroubleBrewingDrunkCandidateRef,
        ): DrunkAssignmentQ04V1ReplayInput {
            val trace = shadow.decisionTrace
            val globalPrefix = trace.historyPrefixRef as? SdeHistoricalPrefixRef.Global
                ?: throw IllegalArgumentException(
                    "Q04 Drunk-assignment replay requires a canonical Global history prefix.",
                )
            return DrunkAssignmentQ04V1ReplayInput(
                decisionContext = shadow.decisionContext,
                featureEvaluation = shadow.drunkAssignmentFeatureEvaluation,
                compatibilityCandidate = compatibilityCandidate,
                lifecycleStage = trace.lifecycleStage,
                historyPrefixRef = globalPrefix,
            )
        }
    }
}

internal data class DrunkAssignmentQ04V1ReplayRecord(
    val evidenceCheckpoint: EvidenceCheckpointId,
    val decisionId: String,
    val sourceRevision: InformationDecisionRevision,
    val historyPrefixRef: SdeHistoricalPrefixRef.Global,
    val legalCandidateIds: List<String>,
    val compatibilityCandidate: TroubleBrewingDrunkCandidateRef,
    val evaluation: DrunkAssignmentQ04V1Evaluation,
    val actualChoice: DecisionTraceActualChoice,
) {
    val policyVersion: PolicyVersion = evaluation.policyVersion

    init {
        require(DrunkAssignmentQ04V1Policy.candidateId(evaluation.selectedCandidate) in legalCandidateIds) {
            "Q04 replay selection must remain inside the current legal Drunk domain."
        }
        require(DrunkAssignmentQ04V1Policy.candidateId(compatibilityCandidate) in legalCandidateIds) {
            "Q04 replay compatibility candidate must remain inside the current legal Drunk domain."
        }
        if (actualChoice is DecisionTraceActualChoice.Committed) {
            require(actualChoice.candidateId in legalCandidateIds) {
                "Historical Drunk-assignment choice must belong to the replayed legal domain."
            }
        }
    }
}

internal object DrunkAssignmentQ04V1ReplayEngine {
    fun replay(
        sourceTrace: DecisionTrace,
        input: DrunkAssignmentQ04V1ReplayInput,
    ): DrunkAssignmentQ04V1ReplayRecord {
        require(sourceTrace.decisionId == input.decisionId) {
            "Q04 Drunk-assignment replay decision identity does not match the source trace."
        }
        require(sourceTrace.lifecycleStage == input.lifecycleStage) {
            "Q04 Drunk-assignment replay lifecycle does not match the source trace."
        }
        require(sourceTrace.sourceRevision == input.sourceRevision) {
            "Q04 Drunk-assignment replay source revision does not match the source trace."
        }
        require(sourceTrace.historyPrefixRef == input.historyPrefixRef) {
            "Q04 Drunk-assignment replay history prefix does not match the source trace."
        }
        require(sourceTrace.legalCandidateIds == input.legalCandidateIds) {
            "Q04 Drunk-assignment replay legal domain does not match the source trace."
        }

        val evaluation = DrunkAssignmentQ04V1Policy.evaluate(input.toPolicyRequest())
        require(DrunkAssignmentQ04V1Policy.candidateId(evaluation.selectedCandidate) in input.legalCandidateIds) {
            "Q04 Drunk-assignment replay policy must select a rules-legal candidate."
        }

        return DrunkAssignmentQ04V1ReplayRecord(
            evidenceCheckpoint = DrunkAssignmentQ04V1PolicyDefinition.evidenceCheckpoint,
            decisionId = input.decisionId,
            sourceRevision = input.sourceRevision,
            historyPrefixRef = input.historyPrefixRef,
            legalCandidateIds = input.legalCandidateIds,
            compatibilityCandidate = input.compatibilityCandidate,
            evaluation = evaluation,
            actualChoice = sourceTrace.actualChoice,
        )
    }
}
