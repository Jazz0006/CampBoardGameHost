package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import java.math.BigInteger

internal enum class ConfirmationObservationRelation {
    SUPPORTS_EXISTING_OBSERVATION,
    CONTRADICTS_EXISTING_OBSERVATION,
    INDEPENDENT_CONTRIBUTION,
    NO_CONTRIBUTION,
    BASELINE_ALREADY_COLLAPSED,
}

internal sealed interface ConfirmationChannelRef {
    data class Source(
        val sourceSeat: Int?,
        val sourceAbility: RoleId?,
    ) : ConfirmationChannelRef {
        init {
            require(sourceSeat != null || sourceAbility != null) {
                "A confirmation source channel requires a seat or ability identity."
            }
            require(sourceSeat == null || sourceSeat > 0) {
                "A confirmation source seat must be positive."
            }
        }
    }

    data class Interaction(
        val interactionId: String,
    ) : ConfirmationChannelRef {
        init {
            require(interactionId.isNotBlank()) {
                "A confirmation interaction channel requires a stable interaction ID."
            }
        }
    }

    data class Observation(
        val observationRecordId: String,
    ) : ConfirmationChannelRef {
        init {
            require(observationRecordId.isNotBlank()) {
                "A confirmation observation channel requires a stable record ID."
            }
        }
    }
}

internal data class ConfirmationAmbiguityRestoration(
    val restoredExactWorldCount: BigInteger,
    val restoredStrategicWorldKeys: Set<StrategicWorldKey>,
    val restoredDemonSeats: Set<Int>,
) {
    init {
        require(restoredExactWorldCount >= BigInteger.ZERO) {
            "Restored exact-world count cannot be negative."
        }
        require(restoredDemonSeats.all { it > 0 }) {
            "Restored Demon seats must be positive."
        }
    }

    val restoresAnyAmbiguity: Boolean
        get() =
            restoredExactWorldCount > BigInteger.ZERO ||
                restoredStrategicWorldKeys.isNotEmpty() ||
                restoredDemonSeats.isNotEmpty()
}

internal data class ConfirmationObservationProvenance(
    val observationRef: SdeHistoricalObservationRef,
    val sourceSeat: Int?,
    val sourceAbility: RoleId?,
    val channel: ConfirmationChannelRef,
) {
    init {
        require(sourceSeat == null || sourceSeat > 0) {
            "Historical confirmation source seat must be positive."
        }
    }
}

internal data class HistoricalObservationConfirmationImpact(
    val provenance: ConfirmationObservationProvenance,
    val relation: ConfirmationObservationRelation,
    val restoration: ConfirmationAmbiguityRestoration,
    val authenticatesDistinctSource: Boolean,
    /**
     * Whether this historical observation removed at least one exact world before the current
     * candidate was applied. This is derived from the same leave-one-out exact scan; downstream
     * features can reuse it without re-enumerating history.
     */
    val wasIndependentlyConstrainingBefore: Boolean = false,
)

internal data class ConfirmationChainFeatures(
    val candidateRemovedExactWorldCount: BigInteger = BigInteger.ZERO,
    val candidateRemovedStrategicWorldKeys: Set<StrategicWorldKey> = emptySet(),
    val candidateRemovedDemonSeats: Set<Int> = emptySet(),
    val candidateChannel: ConfirmationChannelRef,
    val historicalObservationImpacts: List<HistoricalObservationConfirmationImpact> = emptyList(),
) {
    init {
        require(candidateRemovedExactWorldCount >= BigInteger.ZERO) {
            "Candidate-removed exact-world count cannot be negative."
        }
        require(candidateRemovedDemonSeats.all { it > 0 }) {
            "Candidate-removed Demon seats must be positive."
        }
        require(
            historicalObservationImpacts
                .map { it.provenance.observationRef.recordId }
                .distinct()
                .size == historicalObservationImpacts.size,
        ) {
            "Confirmation-chain history may contain each observation record at most once."
        }
    }

    val supportingObservationIds: Set<String>
        get() = historicalObservationImpacts
            .filter { it.relation == ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION }
            .mapTo(linkedSetOf()) { it.provenance.observationRef.recordId }

    val contradictedObservationIds: Set<String>
        get() = historicalObservationImpacts
            .filter { it.relation == ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION }
            .mapTo(linkedSetOf()) { it.provenance.observationRef.recordId }

    val independentlyContributingObservationIds: Set<String>
        get() = historicalObservationImpacts
            .filter { it.relation == ConfirmationObservationRelation.INDEPENDENT_CONTRIBUTION }
            .mapTo(linkedSetOf()) { it.provenance.observationRef.recordId }

    val contributingChannels: Set<ConfirmationChannelRef>
        get() = buildSet {
            if (
                candidateRemovedExactWorldCount > BigInteger.ZERO ||
                candidateRemovedStrategicWorldKeys.isNotEmpty() ||
                candidateRemovedDemonSeats.isNotEmpty()
            ) {
                add(candidateChannel)
            }
            historicalObservationImpacts
                .filter { it.restoration.restoresAnyAmbiguity }
                .forEach { add(it.provenance.channel) }
        }

    val hasMultiChannelCollapse: Boolean
        get() = contributingChannels.size >= 2
}

internal data class ConfirmationLeaveOneOutEvidence(
    val observationRef: SdeHistoricalObservationRef,
    val sourceSeat: Int?,
    val sourceAbility: RoleId?,
    val withoutObservation: ExactHypotheticalObservationBundleDiagnostics,
) {
    init {
        require(sourceSeat == null || sourceSeat > 0) {
            "Leave-one-out confirmation source seat must be positive."
        }
    }
}

/**
 * Pure, production-owned confirmation-chain projection.
 *
 * This projector receives exact full-bundle diagnostics plus exact leave-one-out diagnostics. It
 * does not own history, replay worlds, candidate legality, or policy. Because the full bundle is a
 * subset of every leave-one-out bundle, exact cardinality equality is sufficient to identify when
 * the current candidate makes an earlier observation redundant (support), while a zero full
 * intersection with a non-zero leave-one-out intersection identifies contradiction.
 */
internal object ConfirmationChainFeaturesProjector {
    fun project(
        currentSource: SdeDecisionSourceInteraction,
        candidateId: String,
        full: ExactHypotheticalObservationBundleDiagnostics,
        leaveOneOut: List<ConfirmationLeaveOneOutEvidence>,
    ): ConfirmationChainFeatures {
        require(candidateId.isNotBlank()) {
            "Confirmation-chain projection requires a stable candidate ID."
        }
        require(full.before.value >= full.after.value) {
            "A hypothetical observation cannot increase exact surviving worlds."
        }
        require(full.afterStructure.strategicWorldKeys.all(full.beforeStructure.strategicWorldKeys::contains)) {
            "Candidate after-topology must be a subset of its exact before-topology."
        }
        require(full.afterStructure.possibleDemonSeats.all(full.beforeStructure.possibleDemonSeats::contains)) {
            "Candidate after Demon cover must be a subset of its exact before Demon cover."
        }

        val candidateChannel = currentSource.toConfirmationChannel()
        val impacts = leaveOneOut.map { evidence ->
            val without = evidence.withoutObservation
            require(without.recipientSeat == full.recipientSeat) {
                "Full and leave-one-out confirmation diagnostics must target the same recipient."
            }
            require(without.before.value >= full.before.value) {
                "Removing historical information cannot reduce exact BEFORE worlds."
            }
            require(without.after.value >= full.after.value) {
                "Removing historical information cannot reduce exact AFTER worlds."
            }
            require(full.beforeStructure.strategicWorldKeys.all(without.beforeStructure.strategicWorldKeys::contains)) {
                "Full BEFORE topology must be a subset of leave-one-out BEFORE topology."
            }
            require(full.afterStructure.strategicWorldKeys.all(without.afterStructure.strategicWorldKeys::contains)) {
                "Full AFTER topology must be a subset of leave-one-out AFTER topology."
            }
            require(full.beforeStructure.possibleDemonSeats.all(without.beforeStructure.possibleDemonSeats::contains)) {
                "Full BEFORE Demon cover must be a subset of leave-one-out BEFORE Demon cover."
            }
            require(full.afterStructure.possibleDemonSeats.all(without.afterStructure.possibleDemonSeats::contains)) {
                "Full AFTER Demon cover must be a subset of leave-one-out AFTER Demon cover."
            }

            val historicalChannel = evidence.toConfirmationChannel()
            val relation = relation(
                full = full,
                without = without,
            )
            HistoricalObservationConfirmationImpact(
                provenance = ConfirmationObservationProvenance(
                    observationRef = evidence.observationRef,
                    sourceSeat = evidence.sourceSeat,
                    sourceAbility = evidence.sourceAbility,
                    channel = historicalChannel,
                ),
                relation = relation,
                restoration = ConfirmationAmbiguityRestoration(
                    restoredExactWorldCount = without.after.value.subtract(full.after.value),
                    restoredStrategicWorldKeys =
                        without.afterStructure.strategicWorldKeys -
                            full.afterStructure.strategicWorldKeys,
                    restoredDemonSeats =
                        without.afterStructure.possibleDemonSeats -
                            full.afterStructure.possibleDemonSeats,
                ),
                authenticatesDistinctSource =
                    relation == ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION &&
                        historicalChannel != candidateChannel,
                wasIndependentlyConstrainingBefore =
                    without.before.value > full.before.value,
            )
        }

        return ConfirmationChainFeatures(
            candidateRemovedExactWorldCount = full.before.value.subtract(full.after.value),
            candidateRemovedStrategicWorldKeys =
                full.beforeStructure.strategicWorldKeys -
                    full.afterStructure.strategicWorldKeys,
            candidateRemovedDemonSeats =
                full.beforeStructure.possibleDemonSeats -
                    full.afterStructure.possibleDemonSeats,
            candidateChannel = candidateChannel,
            historicalObservationImpacts = impacts,
        )
    }

    private fun relation(
        full: ExactHypotheticalObservationBundleDiagnostics,
        without: ExactHypotheticalObservationBundleDiagnostics,
    ): ConfirmationObservationRelation = when {
        full.before.value == BigInteger.ZERO ->
            ConfirmationObservationRelation.BASELINE_ALREADY_COLLAPSED

        full.after.value == BigInteger.ZERO && without.after.value > BigInteger.ZERO ->
            ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION

        without.before.value > full.before.value &&
            without.after.value == full.after.value &&
            full.after.value > BigInteger.ZERO ->
            ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION

        without.after.value > full.after.value ->
            ConfirmationObservationRelation.INDEPENDENT_CONTRIBUTION

        else ->
            ConfirmationObservationRelation.NO_CONTRIBUTION
    }

    private fun SdeDecisionSourceInteraction.toConfirmationChannel(): ConfirmationChannelRef =
        if (sourceSeat != null || abilityRole != null) {
            ConfirmationChannelRef.Source(
                sourceSeat = sourceSeat,
                sourceAbility = abilityRole,
            )
        } else {
            ConfirmationChannelRef.Interaction(interactionId)
        }

    private fun ConfirmationLeaveOneOutEvidence.toConfirmationChannel(): ConfirmationChannelRef =
        if (sourceSeat != null || sourceAbility != null) {
            ConfirmationChannelRef.Source(
                sourceSeat = sourceSeat,
                sourceAbility = sourceAbility,
            )
        } else {
            ConfirmationChannelRef.Observation(observationRef.recordId)
        }
}
