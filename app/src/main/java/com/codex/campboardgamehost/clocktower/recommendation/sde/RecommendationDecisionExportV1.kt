package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightPairDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource

/**
 * Policy-neutral, derived export contract for offline recommendation analysis.
 *
 * This contract intentionally does not require or execute any Storyteller policy. Complete legal
 * domains, deterministic feature projections and historical choices are exported independently.
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
        require(evaluationMetadata.evidenceReferences == provenanceOnly.evidenceReferences) {
            "Recommendation export evidence references must be identical across evaluation and provenance sections."
        }

        when (decisionType) {
            RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT -> {
                val context = inputEligible.context as? RecommendationDecisionContextV1.DrunkAssignment
                    ?: throw IllegalArgumentException("Drunk export requires a Drunk-assignment typed context.")
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
                require(inputEligible.lifecycleStage == SdeDecisionLifecycleStage.SetupPrecommit) {
                    "Drunk export must remain at SetupPrecommit."
                }
            }

            RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION -> {
                val context = inputEligible.context as? RecommendationDecisionContextV1.FirstNightPairInformation
                    ?: throw IllegalArgumentException("Pair export requires a first-night pair typed context.")
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
                require(
                    context.legalCandidates
                        .flatMap { it.registrations }
                        .all { registration -> registration.subjectSeat in snapshotSeats },
                ) {
                    "Pair export registration-witness seats must belong to the canonical snapshot."
                }
                require(inputEligible.featureProjection is RecommendationFeatureProjectionV1.StandardDecision) {
                    "Pair export requires the standard DecisionFeatureEvaluation surface."
                }
            }
        }

        when (val actual = targetOrLabel.actualChoice) {
            RecommendationHistoricalChoiceV1.Pending ->
                require(targetOrLabel.candidateRelations.isEmpty()) {
                    "Pending actual choice cannot manufacture historical candidate relations."
                }

            is RecommendationHistoricalChoiceV1.Committed -> {
                require(actual.candidateId in legalCandidateIds) {
                    "Committed historical choice must belong to the complete legal domain."
                }
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
            historyPrefixRef: SdeHistoricalPrefixRef.Global,
            featureEvaluation: DrunkAssignmentFeatureEvaluation,
            actualChoice: RecommendationHistoricalChoiceV1,
            evidenceReferences: List<String> = emptyList(),
        ): RecommendationDecisionExportV1 {
            val legalCandidateIds = context.legalCandidateIds
            require(historyPrefixRef.gameId == context.snapshot.gameId) {
                "Drunk recommendation export prefix must belong to the snapshot game."
            }
            require(featureEvaluation.candidateIds == legalCandidateIds) {
                "Drunk recommendation export features must preserve the rules-owned legal domain."
            }

            return RecommendationDecisionExportV1(
                decisionType = RecommendationDecisionExportTypeV1.DRUNK_ASSIGNMENT,
                inputEligible = RecommendationDecisionInputV1(
                    snapshot = context.snapshot,
                    decisionId = context.decisionId,
                    lifecycleStage = SdeDecisionLifecycleStage.SetupPrecommit,
                    sourceRevision = context.sourceRevision,
                    historyPrefixRef = historyPrefixRef,
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
                        evaluation = featureEvaluation,
                    ),
                ),
                targetOrLabel = targetFrom(actualChoice, legalCandidateIds),
                evaluationMetadata = RecommendationDecisionEvaluationV1(evidenceReferences),
                provenanceOnly = RecommendationDecisionProvenanceV1(
                    gameId = context.snapshot.gameId,
                    scriptId = context.snapshot.script.value,
                    evidenceReferences = evidenceReferences,
                ),
            )
        }

        fun fromFirstNightPairInformation(
            request: RecommendationFirstNightPairExportRequestV1,
            legalCandidates: List<PairInformationLegalCandidate>,
            sourceRevision: InformationDecisionRevision,
            historyPrefixRef: SdeHistoricalPrefixRef.Global,
            featureEvaluation: DecisionFeatureEvaluation,
            actualChoice: RecommendationHistoricalChoiceV1,
            evidenceReferences: List<String> = emptyList(),
        ): RecommendationDecisionExportV1 {
            val legalCandidateIds = legalCandidates.map(PairInformationLegalCandidate::candidateId)
            require(legalCandidateIds.isNotEmpty()) {
                "Pair recommendation export requires the complete non-empty legal candidate domain."
            }
            require(request.context.snapshot.gameId == historyPrefixRef.gameId) {
                "Pair recommendation export prefix must belong to the snapshot game."
            }
            require(
                request.context.snapshot.position.gameStateRevision ==
                    com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(sourceRevision.gameStateRevision) &&
                    request.context.snapshot.position.playerInputRevision ==
                    com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(sourceRevision.playerInputRevision),
            ) {
                "Pair recommendation export source revision must match the canonical snapshot."
            }
            require(featureEvaluation.candidateIds == legalCandidateIds) {
                "Pair recommendation export features must preserve the complete legal-candidate order."
            }
            require(
                request.reliability != ReliabilityState.RELIABLE ||
                    legalCandidates.all { candidate -> candidate.semanticTruth == SemanticTruth.TRUE },
            ) {
                "Reliable pair export cannot contain a false semantic candidate."
            }

            return RecommendationDecisionExportV1(
                decisionType = RecommendationDecisionExportTypeV1.FIRST_NIGHT_PAIR_INFORMATION,
                inputEligible = RecommendationDecisionInputV1(
                    snapshot = request.context.snapshot,
                    decisionId = request.decisionId,
                    lifecycleStage = request.lifecycleStage,
                    sourceRevision = sourceRevision,
                    historyPrefixRef = historyPrefixRef,
                    legalCandidateIds = legalCandidateIds,
                    context = RecommendationDecisionContextV1.FirstNightPairInformation(
                        sourceSeat = request.sourceSeat,
                        abilityRole = request.abilityRole,
                        reliability = request.reliability,
                        legalCandidates = legalCandidates.map { candidate ->
                            RecommendationPairCandidateContextV1(
                                candidateId = candidate.candidateId,
                                shownRoleId = candidate.outcome.shownRole?.value,
                                candidateSeats = candidate.outcome.candidateSeats.toList(),
                                semanticTruth = candidate.semanticTruth,
                                registrations = candidate.registrations.toList(),
                            )
                        },
                    ),
                    featureProjection = RecommendationFeatureProjectionV1.StandardDecision(
                        evaluation = featureEvaluation,
                    ),
                ),
                targetOrLabel = targetFrom(actualChoice, legalCandidateIds),
                evaluationMetadata = RecommendationDecisionEvaluationV1(evidenceReferences),
                provenanceOnly = RecommendationDecisionProvenanceV1(
                    gameId = request.context.snapshot.gameId,
                    scriptId = request.context.snapshot.script.value,
                    evidenceReferences = evidenceReferences,
                ),
            )
        }

        private fun targetFrom(
            actualChoice: RecommendationHistoricalChoiceV1,
            legalCandidateIds: List<String>,
        ): RecommendationDecisionTargetV1 =
            RecommendationDecisionTargetV1(
                actualChoice = actualChoice,
                candidateRelations = when (actualChoice) {
                    RecommendationHistoricalChoiceV1.Pending -> emptyList()
                    is RecommendationHistoricalChoiceV1.Committed -> legalCandidateIds.map { candidateId ->
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

internal data class RecommendationFirstNightPairExportRequestV1(
    val decisionId: String,
    val context: TroubleBrewingFirstNightPairDecisionContext,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
    val lifecycleStage: SdeDecisionLifecycleStage.Interaction,
) {
    init {
        require(decisionId.isNotBlank()) { "Pair recommendation export decision ID cannot be blank." }
        require(sourceSeat > 0) { "Pair recommendation export source seat must be positive." }
    }
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
            require(sourceSeat > 0) { "Pair recommendation export source seat must be positive." }
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

internal sealed interface RecommendationHistoricalChoiceV1 {
    data object Pending : RecommendationHistoricalChoiceV1

    data class Committed(
        val candidateId: String,
        val source: InformationDecisionSource,
        val manualOverride: Boolean,
    ) : RecommendationHistoricalChoiceV1 {
        init {
            require(candidateId.isNotBlank()) { "Committed historical choice candidate ID cannot be blank." }
        }
    }
}

internal data class RecommendationDecisionTargetV1(
    val actualChoice: RecommendationHistoricalChoiceV1,
    val candidateRelations: List<RecommendationHistoricalDomainRelationV1>,
)

/**
 * Relations the Host can derive from one committed historical choice plus the complete legal domain.
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

/**
 * Policy-neutral evidence metadata. Values identify external/static reference material only; they are
 * never executable policy versions and never alter the legal domain or recommendation output.
 */
internal data class RecommendationDecisionEvaluationV1(
    val evidenceReferences: List<String> = emptyList(),
) {
    init {
        require(evidenceReferences.all(String::isNotBlank)) {
            "Recommendation export evidence references cannot be blank."
        }
        require(evidenceReferences.distinct().size == evidenceReferences.size) {
            "Recommendation export evidence references must be unique."
        }
    }
}

internal data class RecommendationDecisionProvenanceV1(
    val gameId: String,
    val scriptId: String,
    val evidenceReferences: List<String> = emptyList(),
) {
    init {
        require(gameId.isNotBlank()) { "Recommendation export provenance game ID cannot be blank." }
        require(scriptId.isNotBlank()) { "Recommendation export grouping script cannot be blank." }
        require(evidenceReferences.all(String::isNotBlank)) {
            "Recommendation export provenance evidence references cannot be blank."
        }
    }
}
