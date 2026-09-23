package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/** Exact lifecycle point for one SDE candidate without owning session state. */
internal sealed interface SdeDecisionLifecycleStage {
    object SetupPrecommit : SdeDecisionLifecycleStage

    data class Interaction(
        val phase: StorytellerPhase,
        val round: Int,
        val sequence: Int,
    ) : SdeDecisionLifecycleStage {
        init {
            require(round > 0) { "Decision interaction round must be positive." }
            require(sequence >= 0) { "Decision interaction sequence cannot be negative." }
        }
    }
}

internal fun TimelinePoint.isStrictlyBeforeSdeDecision(
    decisionPoint: SdeDecisionLifecycleStage.Interaction,
): Boolean = when {
    round != decisionPoint.round -> round < decisionPoint.round
    phase != decisionPoint.phase -> phase.ordinal < decisionPoint.phase.ordinal
    else -> sequence < decisionPoint.sequence
}

/** Stable identity of the interaction that asked for a Storyteller-controlled result. */
internal data class SdeDecisionSourceInteraction(
    val interactionId: String,
    val sourceSeat: Int? = null,
    val abilityRole: RoleId? = null,
    val abilityState: AbilityState? = null,
) {
    init {
        require(interactionId.isNotBlank()) { "Decision source interaction ID cannot be blank." }
        require(sourceSeat == null || sourceSeat > 0) { "Decision source seat must be positive." }
        require(abilityState == null || abilityRole != null) {
            "Decision source ability state requires an ability role."
        }
    }
}

internal enum class SdeCommittedDecisionInputKind {
    SETUP_SHOWN_IDENTITY,
    RED_HERRING,
    DEMON_BLUFFS,
    OTHER,
}

internal enum class SdePlayerControlledDecisionInputKind {
    TARGET_SELECTION,
    OTHER,
}

internal data class CommittedDecisionInputRef(
    val inputId: String,
    val ownerId: String,
    val kind: SdeCommittedDecisionInputKind,
) {
    init {
        require(inputId.isNotBlank()) { "Committed decision input ID cannot be blank." }
        require(ownerId.isNotBlank()) { "Committed decision input owner cannot be blank." }
    }
}

internal data class PlayerControlledDecisionInputRef(
    val inputId: String,
    val ownerId: String,
    val kind: SdePlayerControlledDecisionInputKind,
) {
    init {
        require(inputId.isNotBlank()) { "Player-controlled decision input ID cannot be blank." }
        require(ownerId.isNotBlank()) { "Player-controlled decision input owner cannot be blank." }
    }
}

internal data class SdeHistoricalActionRef(
    val actionId: String,
    val globalSequence: Long,
) {
    init {
        require(actionId.isNotBlank()) { "Historical action ID cannot be blank." }
        require(globalSequence >= 0L) { "Historical action global sequence cannot be negative." }
    }
}

internal data class SdeHistoricalObservationRef(
    val recordId: String,
    val globalSequence: Long,
) {
    init {
        require(recordId.isNotBlank()) { "Historical observation ID cannot be blank." }
        require(globalSequence >= 0L) { "Historical observation global sequence cannot be negative." }
    }
}

/**
 * Stable read-only reference to the canonical committed prefix used for this interaction.
 *
 * This deliberately stores identities and global chronology only. Mechanical/epistemic payloads
 * remain owned by ActionFactTimeline / EpistemicObservationLog and are never copied here.
 */
internal sealed interface SdeHistoricalPrefixRef {
    object NotCaptured : SdeHistoricalPrefixRef

    data class Global(
        val gameId: String,
        val actionRefs: List<SdeHistoricalActionRef>,
        val observationRefs: List<SdeHistoricalObservationRef>,
    ) : SdeHistoricalPrefixRef {
        init {
            require(gameId.isNotBlank()) { "Historical prefix game ID cannot be blank." }
            require(actionRefs.map(SdeHistoricalActionRef::actionId).distinct().size == actionRefs.size) {
                "Historical action refs must have unique action IDs."
            }
            require(observationRefs.map(SdeHistoricalObservationRef::recordId).distinct().size == observationRefs.size) {
                "Historical observation refs must have unique record IDs."
            }
            val globalSequences =
                actionRefs.map(SdeHistoricalActionRef::globalSequence) +
                    observationRefs.map(SdeHistoricalObservationRef::globalSequence)
            require(globalSequences.distinct().size == globalSequences.size) {
                "Historical action/observation refs cannot share global timeline sequences."
            }
        }
    }
}

/**
 * Inputs already fixed before policy selection.
 *
 * [NotCaptured] is deliberately distinct from a captured empty set. A generic adapter must not
 * silently claim that there were no prior commitments or player choices when it has not audited
 * that interaction yet.
 */
internal sealed interface SdeDecisionInputBindings {
    object NotCaptured : SdeDecisionInputBindings

    data class Captured(
        val committedInputRefs: Set<CommittedDecisionInputRef> = emptySet(),
        val playerControlledInputRefs: Set<PlayerControlledDecisionInputRef> = emptySet(),
    ) : SdeDecisionInputBindings {
        init {
            val committedIds = committedInputRefs.mapTo(linkedSetOf(), CommittedDecisionInputRef::inputId)
            val playerIds = playerControlledInputRefs.mapTo(linkedSetOf(), PlayerControlledDecisionInputRef::inputId)
            require(committedIds.intersect(playerIds).isEmpty()) {
                "The same input cannot be owned as both a committed Storyteller input and a player-controlled input."
            }
        }
    }
}

/**
 * Reference to already-materialized hypothetical semantics.
 *
 * The candidate envelope does not copy propositions or regenerate legality. Evaluators continue to
 * consume their typed observation/effect objects from the existing authority; these IDs make the
 * SDE plan auditable and traceable without becoming a second state model.
 */
internal data class SdeDecisionHypotheticalRef(
    val observationRecordIds: List<String> = emptyList(),
    val effectRefs: List<String> = emptyList(),
) {
    init {
        require(observationRecordIds.isNotEmpty() || effectRefs.isNotEmpty()) {
            "An SDE candidate must reference at least one hypothetical observation or effect."
        }
        require(observationRecordIds.all { it.isNotBlank() } && observationRecordIds.distinct().size == observationRecordIds.size) {
            "Hypothetical observation IDs must be non-blank and unique."
        }
        require(effectRefs.all { it.isNotBlank() } && effectRefs.distinct().size == effectRefs.size) {
            "Hypothetical effect refs must be non-blank and unique."
        }
    }
}

/** Provenance of the upstream legal candidate space; never a substitute legality model. */
internal data class SdeDecisionLegalityProvenance(
    val ownerId: String,
    val candidateSpaceIdentity: String,
    val candidateSchemaVersion: String? = null,
) {
    init {
        require(ownerId.isNotBlank()) { "Legality owner ID cannot be blank." }
        require(candidateSpaceIdentity.isNotBlank()) { "Candidate-space identity cannot be blank." }
        require(candidateSchemaVersion == null || candidateSchemaVersion.isNotBlank()) {
            "Candidate schema version cannot be blank when present."
        }
    }
}

/**
 * Score-free SDE planning envelope over one already-legal candidate.
 *
 * This intentionally does not replace domain.DecisionCandidate<T> yet. That legacy type is shared by
 * current setup/dynamic recommendation authority. SDE migration consumes stable identity/provenance
 * beside it until those owners are retired deliberately.
 */
internal data class SdeDecisionCandidate(
    val decisionId: String,
    val candidateId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceInteraction: SdeDecisionSourceInteraction,
    val sourceRevision: InformationDecisionRevision,
    val inputBindings: SdeDecisionInputBindings,
    val historyPrefixRef: SdeHistoricalPrefixRef,
    val legalOutcomeIdentity: String,
    val hypotheticalRef: SdeDecisionHypotheticalRef,
    val legalityProvenance: SdeDecisionLegalityProvenance,
) {
    init {
        require(decisionId.isNotBlank()) { "SDE decision ID cannot be blank." }
        require(candidateId.isNotBlank()) { "SDE candidate ID cannot be blank." }
        require(legalOutcomeIdentity.isNotBlank()) { "SDE legal outcome identity cannot be blank." }
    }
}
