package com.codex.campboardgamehost.clocktower.recommendation.sde

/**
 * Evidence-qualified shadow experiment definition for late-bound Drunk assignment.
 *
 * This is intentionally separate from the production policy registry. The selection method is
 * frozen for replay reproducibility if a later experimental revision ever reaches a Ready state,
 * but SHADOW_V1 itself always defers and therefore emits no selection.
 */
internal object DrunkAssignmentExperimentPolicyDefinitions {
    val SHADOW_V1 = StorytellerPolicyDefinition(
        policyVersion = PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1,
        evidenceCheckpoint = EvidenceCheckpointId(
            "clocktower-evidence-lab-c1d-drunk-assignment-2026-09-29",
        ),
        selectionMethod = PolicySelectionMethod.SEEDED_HASH_V1,
    )
}

internal enum class DrunkAssignmentShadowV1DeferralReason {
    LONGITUDINAL_NARRATIVE_MISSING_CAPABILITY,
    ORDERING_EVIDENCE_NOT_AUTHORIZED,
}

internal data class DrunkAssignmentShadowV1Evaluation(
    val candidateIds: List<String>,
    val reasons: Set<DrunkAssignmentShadowV1DeferralReason>,
) {
    val policyVersion: PolicyVersion = PolicyVersions.DRUNK_ASSIGNMENT_SHADOW_V1

    init {
        require(candidateIds.isNotEmpty()) {
            "Drunk-assignment shadow policy evaluation requires candidate IDs."
        }
        require(candidateIds.all(String::isNotBlank) && candidateIds.distinct().size == candidateIds.size) {
            "Drunk-assignment shadow policy candidate IDs must be non-blank and unique."
        }
        require(reasons.isNotEmpty()) {
            "Drunk-assignment shadow policy deferral requires explicit reasons."
        }
    }
}

/**
 * First Drunk-assignment policy experiment.
 *
 * The evidence checkpoint authorizes descriptive feature families only. It does not authorize a
 * candidate ordering. Missing longitudinal capability is also explicit in DLB-3B2. Therefore this
 * policy deliberately cannot produce a survivor band or selection.
 */
internal object DrunkAssignmentShadowV1Policy {
    fun evaluate(
        featureEvaluation: DrunkAssignmentFeatureEvaluation,
    ): DrunkAssignmentShadowV1Evaluation {
        val longitudinalMissing = featureEvaluation.candidates.any { candidate ->
            candidate.features.longitudinalNarrativeOpportunity ==
                FeatureProjection.Unavailable(FeatureUnavailableReason.MISSING_CAPABILITY)
        }

        val reasons = buildSet {
            if (longitudinalMissing) {
                add(
                    DrunkAssignmentShadowV1DeferralReason
                        .LONGITUDINAL_NARRATIVE_MISSING_CAPABILITY,
                )
            }
            add(DrunkAssignmentShadowV1DeferralReason.ORDERING_EVIDENCE_NOT_AUTHORIZED)
        }

        return DrunkAssignmentShadowV1Evaluation(
            candidateIds = featureEvaluation.candidateIds,
            reasons = reasons,
        )
    }
}

internal interface DrunkAssignmentPolicyExperimentRunner {
    val definition: StorytellerPolicyDefinition

    val policyVersion: PolicyVersion
        get() = definition.policyVersion

    fun run(
        featureEvaluation: DrunkAssignmentFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): DecisionPolicyReplayRun
}

internal object DrunkAssignmentShadowV1ReplayRunner :
    DrunkAssignmentPolicyExperimentRunner {
    override val definition: StorytellerPolicyDefinition =
        DrunkAssignmentExperimentPolicyDefinitions.SHADOW_V1

    override fun run(
        featureEvaluation: DrunkAssignmentFeatureEvaluation,
        decisionId: String,
        selectionSeed: Long,
    ): DecisionPolicyReplayRun {
        require(decisionId.isNotBlank()) {
            "Drunk-assignment shadow replay requires a decision ID."
        }

        val evaluation = DrunkAssignmentShadowV1Policy.evaluate(featureEvaluation)
        return DecisionPolicyReplayRun(
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = evaluation.policyVersion,
                candidateIds = evaluation.candidateIds,
                reasons = evaluation.reasons.mapTo(linkedSetOf()) { reason ->
                    reason.toPolicyDeferralCode()
                },
            ),
            policySelection = null,
        )
    }
}

internal class DrunkAssignmentPolicyExperimentRegistry(
    runners: List<DrunkAssignmentPolicyExperimentRunner>,
) {
    private val runnersByVersion: Map<PolicyVersion, DrunkAssignmentPolicyExperimentRunner>

    val supportedVersions: Set<PolicyVersion>

    init {
        require(runners.isNotEmpty()) {
            "Drunk-assignment policy experiment registry requires at least one runner."
        }
        require(
            runners.map(DrunkAssignmentPolicyExperimentRunner::policyVersion)
                .distinct()
                .size == runners.size,
        ) {
            "Drunk-assignment policy experiment registry cannot contain duplicate versions."
        }
        runnersByVersion = runners.associateBy(DrunkAssignmentPolicyExperimentRunner::policyVersion)
        supportedVersions =
            runners.map(DrunkAssignmentPolicyExperimentRunner::policyVersion).toSet()
    }

    fun requireRunner(
        policyVersion: PolicyVersion,
    ): DrunkAssignmentPolicyExperimentRunner =
        requireNotNull(runnersByVersion[policyVersion]) {
            "Unsupported Drunk-assignment experiment policy version '${policyVersion.value}'."
        }

    companion object {
        fun experimental(): DrunkAssignmentPolicyExperimentRegistry =
            DrunkAssignmentPolicyExperimentRegistry(
                listOf(DrunkAssignmentShadowV1ReplayRunner),
            )
    }
}

private fun DrunkAssignmentShadowV1DeferralReason.toPolicyDeferralCode(): PolicyDeferralCode =
    PolicyDeferralCode(
        when (this) {
            DrunkAssignmentShadowV1DeferralReason
                .LONGITUDINAL_NARRATIVE_MISSING_CAPABILITY ->
                "drunk-assignment-shadow-v1.longitudinal-narrative-missing-capability"

            DrunkAssignmentShadowV1DeferralReason.ORDERING_EVIDENCE_NOT_AUTHORIZED ->
                "drunk-assignment-shadow-v1.ordering-evidence-not-authorized"
        },
    )
