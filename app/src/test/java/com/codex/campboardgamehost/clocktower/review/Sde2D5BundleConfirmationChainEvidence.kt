package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.recommendation.FirstNightHealthyRecipientExactDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightLeaveOneOutDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightProjectedSignatureGroup
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio

internal data class Sde2D5BundleLeaveOneOutEvidence(
    val omittedObservationKey: String,
    val normalized: NormalizedStrategicDiagnostics,
    val afterStrategicWorldCount: Int,
    val afterDemonCoverCount: Int,
    val restoresDemonCover: Boolean,
    val restoresStrategicTopology: Boolean,
) {
    init {
        require(omittedObservationKey.isNotBlank())
        require(afterStrategicWorldCount >= 0)
        require(afterDemonCoverCount >= 0)
    }
}

internal data class Sde2D5BundleConfirmationRecipientEvidence(
    val recipientSeat: Int,
    val fullBundleNormalized: NormalizedStrategicDiagnostics,
    val fullBundleAfterStrategicWorldCount: Int,
    val fullBundleDemonCoverCount: Int,
    val leaveOneOut: List<Sde2D5BundleLeaveOneOutEvidence>,
) {
    init {
        require(recipientSeat > 0)
        require(fullBundleAfterStrategicWorldCount >= 0)
        require(fullBundleDemonCoverCount >= 0)
        require(leaveOneOut.map { it.omittedObservationKey }.distinct().size == leaveOneOut.size)
    }

    val restoringObservationKeys: Set<String>
        get() = leaveOneOut
            .filter { it.restoresDemonCover || it.restoresStrategicTopology }
            .mapTo(linkedSetOf(), Sde2D5BundleLeaveOneOutEvidence::omittedObservationKey)

    val hasMultiChannelCollapse: Boolean
        get() = restoringObservationKeys.size >= 2
}

internal data class Sde2D5BundleConfirmationChainEvidence(
    val signatureId: String,
    val playerCount: Int,
    val regime: Sde2D5PlayerCountRegime,
    val profileKind: Sde2D5SetupProfileKind,
    val byRecipient: List<Sde2D5BundleConfirmationRecipientEvidence>,
    val worstGoodRecipientSeat: Int,
) {
    init {
        require(signatureId.isNotBlank())
        require(playerCount in 5..15)
        require(regime == Sde2D5PlayerCountRegime.from(playerCount))
        require(byRecipient.isNotEmpty())
        require(byRecipient.map { it.recipientSeat }.distinct().size == byRecipient.size)
        require(worstGoodRecipientSeat in byRecipient.map { it.recipientSeat })
    }

    val worstGoodRecipient: Sde2D5BundleConfirmationRecipientEvidence
        get() = byRecipient.single { it.recipientSeat == worstGoodRecipientSeat }

    val worstGoodRecipientRestoringClueCount: Int
        get() = worstGoodRecipient.restoringObservationKeys.size

    val hasMultiChannelCollapse: Boolean
        get() = worstGoodRecipient.hasMultiChannelCollapse
}

internal enum class Sde2D5BundleConfirmationSelectionReason {
    LEAST_INTERACTION_REFERENCE,
    MOST_INTERACTION_REFERENCE,
}

internal data class Sde2D5BundleConfirmationSelection(
    val evidence: Sde2D5BundleConfirmationChainEvidence,
    val selectionReasons: Set<Sde2D5BundleConfirmationSelectionReason>,
) {
    init {
        require(selectionReasons.isNotEmpty())
    }
}

/**
 * Review-only whole-bundle / leave-one-out projection.
 *
 * It deliberately exposes interactions instead of inventing a scalar confirmation score. A clue
 * removal "restores" cover only when the same exact baseline gains Demon seats or strategic
 * topologies relative to the complete public bundle.
 */
internal object Sde2D5BundleConfirmationChainEvidenceProjector {
    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        group: FirstNightProjectedSignatureGroup,
    ): Sde2D5BundleConfirmationChainEvidence =
        project(
            playerCount = playerCount,
            profileKind = profileKind,
            signatureId = group.signatureId,
            fullByRecipient = group.recipientDiagnostics,
            leaveOneOut = group.leaveOneOutDiagnostics,
        )

    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        signatureId: String,
        fullByRecipient: List<FirstNightHealthyRecipientExactDiagnostics>,
        leaveOneOut: List<FirstNightLeaveOneOutDiagnostics>,
    ): Sde2D5BundleConfirmationChainEvidence {
        require(fullByRecipient.isNotEmpty())
        require(leaveOneOut.isNotEmpty())
        val fullBySeat = fullByRecipient.associateBy { it.recipientSeat }
        require(fullBySeat.size == fullByRecipient.size)

        val recipients = fullByRecipient.sortedBy { it.recipientSeat }.map { full ->
            val leaveOneOutEvidence = leaveOneOut.map { omitted ->
                val withoutClue = omitted.recipientDiagnostics.single {
                    it.recipientSeat == full.recipientSeat
                }
                require(withoutClue.before == full.before)
                require(withoutClue.beforeStructure == full.beforeStructure)
                require(withoutClue.after.value >= full.after.value) {
                    "Removing a public clue cannot reduce exact surviving worlds."
                }
                Sde2D5BundleLeaveOneOutEvidence(
                    omittedObservationKey = omitted.omittedObservationKey,
                    normalized = NormalizedStrategicDiagnosticsProjector.project(
                        before = full.beforeStructure,
                        after = withoutClue.afterStructure,
                        playerCount = playerCount,
                    ),
                    afterStrategicWorldCount =
                        withoutClue.afterStructure.distinctStrategicWorldCount,
                    afterDemonCoverCount = withoutClue.afterStructure.possibleDemonSeats.size,
                    restoresDemonCover =
                        withoutClue.afterStructure.possibleDemonSeats.size >
                            full.afterStructure.possibleDemonSeats.size,
                    restoresStrategicTopology =
                        withoutClue.afterStructure.distinctStrategicWorldCount >
                            full.afterStructure.distinctStrategicWorldCount,
                )
            }
            Sde2D5BundleConfirmationRecipientEvidence(
                recipientSeat = full.recipientSeat,
                fullBundleNormalized = NormalizedStrategicDiagnosticsProjector.project(
                    before = full.beforeStructure,
                    after = full.afterStructure,
                    playerCount = playerCount,
                ),
                fullBundleAfterStrategicWorldCount =
                    full.afterStructure.distinctStrategicWorldCount,
                fullBundleDemonCoverCount = full.afterStructure.possibleDemonSeats.size,
                leaveOneOut = leaveOneOutEvidence,
            )
        }

        val worst = recipients.minWith(
            Comparator { left, right ->
                val topology = compareRatio(
                    left.fullBundleNormalized.evilTopologyRetention,
                    right.fullBundleNormalized.evilTopologyRetention,
                )
                if (topology != 0) {
                    topology
                } else {
                    val demon = compareRatio(
                        left.fullBundleNormalized.demonCoverRetention,
                        right.fullBundleNormalized.demonCoverRetention,
                    )
                    if (demon != 0) demon else left.recipientSeat.compareTo(right.recipientSeat)
                }
            },
        )

        return Sde2D5BundleConfirmationChainEvidence(
            signatureId = signatureId,
            playerCount = playerCount,
            regime = Sde2D5PlayerCountRegime.from(playerCount),
            profileKind = profileKind,
            byRecipient = recipients,
            worstGoodRecipientSeat = worst.recipientSeat,
        )
    }

    private fun compareRatio(left: StrategicRatio, right: StrategicRatio): Int = when {
        left is StrategicRatio.Undefined && right is StrategicRatio.Undefined -> 0
        left is StrategicRatio.Undefined -> 1
        right is StrategicRatio.Undefined -> -1
        left is StrategicRatio.Defined && right is StrategicRatio.Defined ->
            (left.numerator.toLong() * right.denominator.toLong())
                .compareTo(right.numerator.toLong() * left.denominator.toLong())
        else -> error("Unknown strategic ratio implementation.")
    }
}

internal object Sde2D5BundleConfirmationChainEvidenceSelector {
    fun selectReviewContrasts(
        evidence: List<Sde2D5BundleConfirmationChainEvidence>,
    ): List<Sde2D5BundleConfirmationSelection> {
        require(evidence.isNotEmpty())
        require(evidence.map { it.signatureId }.distinct().size == evidence.size)

        val comparator = compareBy<Sde2D5BundleConfirmationChainEvidence> {
            it.worstGoodRecipientRestoringClueCount
        }.thenComparator { left, right ->
            val pressure = compareRatio(
                left.worstGoodRecipient.fullBundleNormalized.evilTopologyRetention,
                right.worstGoodRecipient.fullBundleNormalized.evilTopologyRetention,
            )
            if (pressure != 0) pressure else left.signatureId.compareTo(right.signatureId)
        }
        val least = evidence.minWith(comparator)
        val most = evidence.maxWith(comparator)
        val selected = linkedMapOf<
            String,
            Pair<Sde2D5BundleConfirmationChainEvidence, MutableSet<Sde2D5BundleConfirmationSelectionReason>>,
        >()

        fun add(
            point: Sde2D5BundleConfirmationChainEvidence,
            reason: Sde2D5BundleConfirmationSelectionReason,
        ) {
            val entry = selected.getOrPut(point.signatureId) { point to linkedSetOf() }
            entry.second += reason
        }
        add(least, Sde2D5BundleConfirmationSelectionReason.LEAST_INTERACTION_REFERENCE)
        add(most, Sde2D5BundleConfirmationSelectionReason.MOST_INTERACTION_REFERENCE)

        return selected.values.map { (point, reasons) ->
            Sde2D5BundleConfirmationSelection(point, reasons)
        }.sortedBy { it.evidence.signatureId }
    }

    private fun compareRatio(left: StrategicRatio, right: StrategicRatio): Int = when {
        left is StrategicRatio.Undefined && right is StrategicRatio.Undefined -> 0
        left is StrategicRatio.Undefined -> 1
        right is StrategicRatio.Undefined -> -1
        left is StrategicRatio.Defined && right is StrategicRatio.Defined ->
            (left.numerator.toLong() * right.denominator.toLong())
                .compareTo(right.numerator.toLong() * left.denominator.toLong())
        else -> error("Unknown strategic ratio implementation.")
    }
}
