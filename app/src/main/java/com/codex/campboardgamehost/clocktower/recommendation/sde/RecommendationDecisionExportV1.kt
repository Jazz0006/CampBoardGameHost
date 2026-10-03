package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * Policy-neutral, derived export contract for offline recommendation analysis.
 *
 * The four payload sections intentionally mirror the HOST-ML0 leakage classes. Consumers must not
 * flatten TARGET_OR_LABEL or EVALUATION_METADATA back into model input.
 */
internal data class RecommendationDecisionExportV1(
    val schemaId: String = SCHEMA_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val decisionType: RecommendationDecisionExportTypeV1,
    val inputEligible: RecommendationDecisionInputV1,
    val targetOrLabel: RecommendationDecisionTargetV1,
    val evaluationMetadata: RecommendationDecisionEvaluationV1,
    val provenanceOnly: RecommendationDecisionProvenanceV1,
) {
    init {
        require(schemaId == SCHEMA_ID) { "Unsupported recommendation export schema ID." }
        require(schemaVersion == SCHEMA_VERSION) { "Unsupported recommendation export schema version." }

        val legalCandidateIds = inputEligible.legalCandidateIds
        require(inputEligible.featureProjection.candidateIds == legalCandidateIds) {
            "Recommendation export features must preserve the complete legal-candidate order."
        }
        require(inputEligible.historyPrefixRef.gameId == inputEligible.snapshot.gameId) {
            "Recommendation export history prefix must belong to the canonical snapshot game."
        }
        require(provenanceOnly.gameId == inputEligible.snapshot.gameId) {
            "Recommendation export grouping game ID must match the canonical snapshot."
        }
        require(provenanceOnly.scriptId == inputEligible.snapshot.script.value) {
            "Recommendation export grouping script must match the canonical snapshot."
        }

        when (decisionType) {
            RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT -> {
                val context = inputEligible.context as? RecommendationDecisionContextV1.DrunkAssignment
                    ?: throw IllegalArgumentException(
                        "Drunk export requires a Drunk-assignment typed context.",
                    )
                require(context.legalCandidates.map { it.candidateId } == legalCandidateIds) {
                    "Drunk export typed candidate payload must preserve the complete legal-candidate order."
                }
                val snapshotSeats = inputEligible.snapshot.grimoireSeats.map { it.seat }.toSet()
                require(context.legalCandidates.all { it.seat in snapshotSeats }) {
                    "Drunk export candidate seats must belong to the canonical snapshot."
                }
                require(inputEligible.featureProjection is RecommendationFeatureProjectionV1.DrunkAssignment) {
                    "Drunk export requires the dedicated Drunk-assignment feature surface."
                }
            }

            RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION -> {
                val context = inputEligible.context as? RecommendationDecisionContextV1.FirstNightPairInformation
                    ?: throw IllegalArgumentException(
                        "Pair export requires a first-night pair typed context.",
                    )
                require(context.legalCandidates.map { it.candidateId } == legalCandidateIds) {
                    "Pair export typed candidate payload must preserve the complete legal-candidate order."
                }
                val snapshotSeats = inputEligible.snapshot.grimoireSeats.map { it.seat }.toSet()
                require(context.sourceSeat in snapshotSeats) {
                    "Pair export source seat must belong to the canonical snapshot."
                }
                require(context.legalCandidates.flatMap { it.candidateSeats }.all { it in snapshotSeats }) {
                    "Pair export candidate seats must belong to the canonical snapshot."
                }
                require(inputEligible.featureProjection is RecommendationFeatureProjectionV1.StandardDecision) {
                    "Pair export requires the standard DecisionFeatureEvaluation surface."
                }
            }
        }

        require(
            evaluationMetadata.policyTraces.map { it.policyVersion }.distinct().size ==
                evaluationMetadata.policyTraces.size,
        ) {
            "Recommendation export policy replay versions must be unique."
        }
        require(evaluationMetadata.policyTraces.all { it.candidateIds == legalCandidateIds }) {
            "Recommendation export policy traces must preserve the complete legal-candidate order."
        }
        require(
            provenanceOnly.policyEvidenceCheckpoints ==
                evaluationMetadata.policyTraces.map { trace -> trace.evidenceCheckpoint },
        ) {
            "Recommendation export provenance must preserve policy-trace checkpoint order."
        }

        when (val actual = targetOrLabel.actualChoice) {
            DecisionTraceActualChoice.Pending ->
                require(targetOrLabel.candidateRelations.isEmpty()) {
                    "Pending actual choice cannot manufacture historical candidate relations."
                }

            is DecisionTraceActualChoice.Committed -> {
                require(targetOrLabel.candidateRelations.map { it.candidateId } == legalCandidateIds) {
                    "Committed historical relations must classify every legal candidate in canonical order."
                }
                targetOrLabel.candidateRelations.forEach { relation ->
                    val expected = if (relation.candidateId == actual.candidateId) {
                        RecommendationHistoricalDomainRelationKindV1.OBSERVED_CHOICE
                    } else {
                        RecommendationHistoricalDomainRelationKindV1.LEGAL_UNCHOSEN
                    }
                    require(relation.kind == expected) {
                        "Host-derived historical relations may only distinguish observed choice from legal unchosen."
                    }
                }
            }
        }
    }

    companion object {
        const val SCHEMA_ID: String = "botc.recommendation-decision-export"
        const val SCHEMA_VERSION: Int = 1

        fun fromDrunkAssignment(
            context: DrunkAssignmentDecisionContext,
            replayRecords: List<DrunkAssignmentShadowReplayRecord>,
        ): RecommendationDecisionExportV1 {
            require(replayRecords.isNotEmpty()) {
                "Drunk recommendation export requires at least one replay record."
            }
            val first = replayRecords.first()
            val legalCandidateIds = context.legalCandidateIds
            require(first.decisionId == context.decisionId) {
                "Drunk recommendation export decision identity must match the typed context."
            }
            require(first.lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
                "Drunk recommendation export must remain at SetupPrecommit."
            }
            require(first.sourceRevision == context.sourceRevision) {
                "Drunk recommendation export source revision must match the typed context."
            }
            require(first.legalCandidateIds == legalCandidateIds) {
                "Drunk recommendation export must preserve the rules-owned legal domain."
            }
            require(first.historyPrefixRef.gameId == context.snapshot.gameId) {
                "Drunk recommendation export prefix must belong to the snapshot game."
            }
            require(replayRecords.all { record ->
                record.decisionId == first.decisionId &&
                    record.lifecycleStage == first.lifecycleStage &&
                    record.sourceRevision == first.sourceRevision &&
                    record.historyPrefixRef == first.historyPrefixRef &&
                    record.legalCandidateIds == legalCandidateIds &&
                    record.featureEvaluation == first.featureEvaluation &&
                    record.actualChoice == first.actualChoice
            }) {
                "Drunk recommendation export replay records must share one canonical decision input and actual choice."
            }

            val policyTraces = replayRecords.map { record -> RecommendationPolicyTraceV1.fromDrunkReplay(record) }
            return RecommendationDecisionExportV1(
                decisionType = RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT,
                inputEligible = RecommendationDecisionInputV1(
                    snapshot = context.snapshot,
                    decisionId = context.decisionId,
                    lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
                    sourceRevision = context.sourceRevision,
                    historyPrefixRef = first.historyPrefixRef,
                    legalCandidateIds = legalCandidateIds,
                    context = RecommendationDecisionContextV1.DrunkAssignment(
                        legalCandidates = context.legalCandidates.zip(legalCandidateIds).map { (candidate, candidateId) ->
                            RecommendationDrunkCandidateContextV1(
                                candidateId = candidateId,
                                seat = candidate.seat,
                                shownRoleId = candidate.shownRoleId,
                            )
                        },
                    ),
                    featureProjection = RecommendationFeatureProjectionV1.DrunkAssignment(
                        evaluation = first.featureEvaluation,
                    ),
                ),
                targetOrLabel = targetFrom(first.actualChoice, legalCandidateIds),
                evaluationMetadata = RecommendationDecisionEvaluationV1(policyTraces),
                provenanceOnly = RecommendationDecisionProvenanceV1(
                    gameId = context.snapshot.gameId,
                    scriptId = context.snapshot.script.value,
                    policyEvidenceCheckpoints = policyTraces.map(RecommendationPolicyTraceV1::evidenceCheckpoint),
                ),
            )
        }

        fun fromFirstNightPairInformation(
            request: PairInformationShadowReplayRequest,
            legalCandidates: List<PairInformationLegalCandidate>,
            replayTraces: List<DecisionTrace>,
        ): RecommendationDecisionExportV1 {
            require(replayTraces.isNotEmpty()) {
                "Pair recommendation export requires at least one replay trace."
            }
            val first = replayTraces.first()
            val legalCandidateIds = legalCandidates.map(PairInformationLegalCandidate::candidateId)
            require(legalCandidateIds.isNotEmpty()) {
                "Pair recommendation export requires the complete non-empty legal candidate domain."
            }
            require(legalCandidateIds == first.legalCandidateIds) {
                "Pair recommendation export semantic candidate payload must preserve the replayed legal domain."
            }
            require(
                request.reliability != ReliabilityState.RELIABLE ||
                    legalCandidates.all { candidate -> candidate.semanticTruth == SemanticTruth.TRUE },
            ) {
                "Reliable pair export cannot contain a false semantic candidate."
            }
            val prefix = first.historyPrefixRef as? SdeHistoricalPrefixRef.Global
                ?: throw IllegalArgumentException(
                    "Pair recommendation export requires a canonical Global history prefix.",
                )
            require(first.decisionId == request.decisionId) {
                "Pair recommendation export decision identity must match the typed request."
            }
            require(first.lifecycleStage == request.lifecycleStage) {
                "Pair recommendation export lifecycle must match the typed request."
            }
            require(prefix.gameId == request.context.snapshot.gameId) {
                "Pair recommendation export prefix must belong to the snapshot game."
            }
            require(
                request.context.snapshot.position.gameStateRevision ==
                    SnapshotField.Known(first.sourceRevision.gameStateRevision) &&
                    request.context.snapshot.position.playerInputRevision ==
                    SnapshotField.Known(first.sourceRevision.playerInputRevision),
            ) {
                "Pair recommendation export source revision must match the canonical snapshot."
            }
            require(replayTraces.all { trace ->
                trace.decisionId == first.decisionId &&
                    trace.lifecycleStage == first.lifecycleStage &&
                    trace.sourceRevision == first.sourceRevision &&
                    trace.historyPrefixRef == prefix &&
                    trace.legalCandidateIds == first.legalCandidateIds &&
                    trace.featureEvaluation == first.featureEvaluation &&
                    trace.actualChoice == first.actualChoice
            }) {
                "Pair recommendation export replay traces must share one canonical decision input and actual choice."
            }

            val policyTraces = replayTraces.map { trace -> RecommendationPolicyTraceV1.fromDecisionTrace(trace) }
            return RecommendationDecisionExportV1(
                decisionType = RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION,
                inputEligible = RecommendationDecisionInputV1(
                    snapshot = request.context.snapshot,
                    decisionId = request.decisionId,
                    lifecycleStage = request.lifecycleStage,
                    sourceRevision = first.sourceRevision,
                    historyPrefixRef = prefix,
                    legalCandidateIds = legalCandidateIds,
                    context = RecommendationDecisionContextV1.FirstNightPairInformation(
                        sourceSeat = request.sourceSeat,
                        abilityRole = request.abilityRole,
                        reliability = request.reliability,
                        legalCandidates = legalCandidates.map { candidate ->
                            RecommendationPairCandidateContextV1(
                                candidateId = candidate.candidateId,
                                shownRoleId = candidate.outcome.shownRole?.value,
                                candidateSeats = candidate.outcome.candidateSeats,
                                semanticTruth = candidate.semanticTruth,
                                registrations = candidate.registrations,
                            )
                        },
                    ),
                    featureProjection = RecommendationFeatureProjectionV1.StandardDecision(
                        evaluation = first.featureEvaluation,
                    ),
                ),
                targetOrLabel = targetFrom(first.actualChoice, legalCandidateIds),
                evaluationMetadata = RecommendationDecisionEvaluationV1(policyTraces),
                provenanceOnly = RecommendationDecisionProvenanceV1(
                    gameId = request.context.snapshot.gameId,
                    scriptId = request.context.snapshot.script.value,
                    policyEvidenceCheckpoints = policyTraces.map(RecommendationPolicyTraceV1::evidenceCheckpoint),
                ),
            )
        }

        private fun targetFrom(
            actualChoice: DecisionTraceActualChoice,
            legalCandidateIds: List<String>,
        ): RecommendationDecisionTargetV1 =
            RecommendationDecisionTargetV1(
                actualChoice = actualChoice,
                candidateRelations = when (actualChoice) {
                    DecisionTraceActualChoice.Pending -> emptyList()
                    is DecisionTraceActualChoice.Committed -> legalCandidateIds.map { candidateId ->
                        RecommendationHistoricalDomainRelationV1(
                            candidateId = candidateId,
                            kind = if (candidateId == actualChoice.candidateId) {
                                RecommendationHistoricalDomainRelationKindV1.OBSERVED_CHOICE
                            } else {
                                RecommendationHistoricalDomainRelationKindV1.LEGAL_UNCHOSEN
                            },
                        )
                    }
                },
            )
    }
}

internal enum class RecommendationDecisionExportTypeV1 {
    DRUNK_ASSIGNMENT,
    FIRST_NIGHT_PAIR_INFORMATION,
}

internal data class RecommendationDecisionInputV1(
    val snapshot: TroubleBrewingGameSnapshotV1,
    val decisionId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceRevision: InformationDecisionRevision,
    val historyPrefixRef: SdeHistoricalPrefixRef.Global,
    val legalCandidateIds: List<String>,
    val context: RecommendationDecisionContextV1,
    val featureProjection: RecommendationFeatureProjectionV1,
) {
    init {
        require(decisionId.isNotBlank()) { "Recommendation export decision ID cannot be blank." }
        require(legalCandidateIds.isNotEmpty()) { "Recommendation export requires legal candidates." }
        require(legalCandidateIds.all(String::isNotBlank) && legalCandidateIds.distinct().size == legalCandidateIds.size) {
            "Recommendation export legal candidate IDs must be non-blank and unique."
        }
    }
}

internal sealed interface RecommendationDecisionContextV1 {
    data class DrunkAssignment(
        val legalCandidates: List<RecommendationDrunkCandidateContextV1>,
    ) : RecommendationDecisionContextV1 {
        init {
            require(legalCandidates.isNotEmpty()) {
                "Drunk recommendation export context requires legal candidates."
            }
            require(legalCandidates.map { it.candidateId }.distinct().size == legalCandidates.size) {
                "Drunk recommendation export context candidate IDs must be unique."
            }
        }
    }

    data class FirstNightPairInformation(
        val sourceSeat: Int,
        val abilityRole: RoleId,
        val reliability: ReliabilityState,
        val legalCandidates: List<RecommendationPairCandidateContextV1>,
    ) : RecommendationDecisionContextV1 {
        init {
            require(sourceSeat > 0) {
                "Pair recommendation export source seat must be positive."
            }
            require(legalCandidates.isNotEmpty()) {
                "Pair recommendation export context requires legal candidate payloads."
            }
            require(legalCandidates.map { it.candidateId }.distinct().size == legalCandidates.size) {
                "Pair recommendation export context candidate IDs must be unique."
            }
        }
    }
}

internal data class RecommendationPairCandidateContextV1(
    val candidateId: String,
    val shownRoleId: String?,
    val candidateSeats: List<Int>,
    val semanticTruth: SemanticTruth,
    val registrations: List<RegistrationFact>,
) {
    init {
        require(candidateId.isNotBlank()) { "Pair export candidate ID cannot be blank." }
        require(candidateSeats.all { it > 0 } && candidateSeats.distinct().size == candidateSeats.size) {
            "Pair export candidate seats must be positive and unique."
        }
        require(
            (shownRoleId == null && candidateSeats.isEmpty()) ||
                (shownRoleId != null && shownRoleId.isNotBlank() && candidateSeats.size == 2),
        ) {
            "Pair export candidate payload must represent either an empty result or one shown role with two seats."
        }
        require(semanticTruth == SemanticTruth.TRUE || registrations.isEmpty()) {
            "False pair export candidates cannot claim truth-registration witnesses."
        }
    }
}

internal data class RecommendationDrunkCandidateContextV1(
    val candidateId: String,
    val seat: Int,
    val shownRoleId: String,
) {
    init {
        require(candidateId.isNotBlank()) { "Drunk export candidate ID cannot be blank." }
        require(seat > 0) { "Drunk export candidate seat must be positive." }
        require(shownRoleId.isNotBlank()) { "Drunk export shown role cannot be blank." }
    }
}

internal sealed interface RecommendationFeatureProjectionV1 {
    val candidateIds: List<String>

    data class DrunkAssignment(
        val evaluation: DrunkAssignmentFeatureEvaluation,
    ) : RecommendationFeatureProjectionV1 {
        override val candidateIds: List<String>
            get() = evaluation.candidateIds
    }

    data class StandardDecision(
        val evaluation: DecisionFeatureEvaluation,
    ) : RecommendationFeatureProjectionV1 {
        override val candidateIds: List<String>
            get() = evaluation.candidateIds
    }
}

internal data class RecommendationDecisionTargetV1(
    val actualChoice: DecisionTraceActualChoice,
    val candidateRelations: List<RecommendationHistoricalDomainRelationV1>,
)

/**
 * Relations the Host can derive from one committed historical choice plus the complete legal domain.
 *
 * Explicit source-backed rejection/comparison semantics are deliberately absent from HOST-ML1 V1;
 * adding them requires a future machine-readable EvidenceLab seed rather than inference here.
 */
internal enum class RecommendationHistoricalDomainRelationKindV1 {
    OBSERVED_CHOICE,
    LEGAL_UNCHOSEN,
}

internal data class RecommendationHistoricalDomainRelationV1(
    val candidateId: String,
    val kind: RecommendationHistoricalDomainRelationKindV1,
) {
    init {
        require(candidateId.isNotBlank()) { "Recommendation candidate relation ID cannot be blank." }
    }
}

internal data class RecommendationDecisionEvaluationV1(
    val policyTraces: List<RecommendationPolicyTraceV1>,
) {
    init {
        require(policyTraces.isNotEmpty()) {
            "Recommendation export requires at least one policy/replay trace."
        }
    }
}

internal data class RecommendationPolicyTraceV1(
    val evidenceCheckpoint: EvidenceCheckpointId,
    val policySnapshot: DecisionTracePolicySnapshot,
    val policySelection: PolicySelection?,
) {
    val policyVersion: PolicyVersion
        get() = policySnapshot.policyVersion

    val candidateIds: List<String>
        get() = policySnapshot.candidateIds

    companion object {
        fun fromDecisionTrace(trace: DecisionTrace): RecommendationPolicyTraceV1 =
            RecommendationPolicyTraceV1(
                evidenceCheckpoint = trace.evidenceCheckpoint,
                policySnapshot = trace.policySnapshot,
                policySelection = trace.policySelection,
            )

        fun fromDrunkReplay(record: DrunkAssignmentShadowReplayRecord): RecommendationPolicyTraceV1 =
            RecommendationPolicyTraceV1(
                evidenceCheckpoint = record.evidenceCheckpoint,
                policySnapshot = record.policySnapshot,
                policySelection = record.policySelection,
            )
    }
}

internal data class RecommendationDecisionProvenanceV1(
    val gameId: String,
    val scriptId: String,
    val policyEvidenceCheckpoints: List<EvidenceCheckpointId>,
) {
    init {
        require(gameId.isNotBlank()) { "Recommendation export provenance game ID cannot be blank." }
        require(scriptId.isNotBlank()) { "Recommendation export provenance script ID cannot be blank." }
        require(policyEvidenceCheckpoints.isNotEmpty()) {
            "Recommendation export provenance requires policy evidence checkpoints."
        }
    }
}
