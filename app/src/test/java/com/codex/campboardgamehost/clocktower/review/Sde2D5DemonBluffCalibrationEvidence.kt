package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffJointOutputDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnosticsProjector
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio

internal enum class Sde2D5BluffClaimCadence {
    ONE_SHOT,
    RECURRING_NIGHTLY,
    TRIGGERED,
    PASSIVE_OR_SOCIAL,
}

internal enum class Sde2D5BluffNarrativeRouteClass {
    INFORMATION,
    PROTECTION,
    PASSIVE_SURVIVAL,
    PUBLIC_ABILITY,
    SOCIAL_OUTSIDER,
}

internal enum class Sde2D5BluffBurden {
    LOW,
    MODERATE,
    HIGH,
}

internal data class Sde2D5BluffRoleTrait(
    val beginnerExecutionBurden: Sde2D5BluffBurden,
    val claimBurden: Sde2D5BluffBurden,
    val claimCadence: Sde2D5BluffClaimCadence,
    val narrativeRouteClass: Sde2D5BluffNarrativeRouteClass,
)

internal data class Sde2D5BluffPairwiseCoverage(
    val firstRole: RoleId,
    val secondRole: RoleId,
    val unionStrategicWorldCount: Int,
    val sharedStrategicWorldCount: Int,
    val sharedToUnionRetention: StrategicRatio,
) {
    init {
        require(firstRole != secondRole)
        require(unionStrategicWorldCount >= 0)
        require(sharedStrategicWorldCount in 0..unionStrategicWorldCount)
    }
}

internal object Sde2D5DemonBluffRoleTraits {
    fun forRole(role: RoleId): Sde2D5BluffRoleTrait = when (role.value) {
        "Washerwoman", "Librarian", "Investigator" ->
            trait(Sde2D5BluffBurden.MODERATE, Sde2D5BluffBurden.MODERATE, Sde2D5BluffClaimCadence.ONE_SHOT, Sde2D5BluffNarrativeRouteClass.INFORMATION)
        "Chef" ->
            trait(Sde2D5BluffBurden.LOW, Sde2D5BluffBurden.LOW, Sde2D5BluffClaimCadence.ONE_SHOT, Sde2D5BluffNarrativeRouteClass.INFORMATION)
        "Empath", "Fortune Teller" ->
            trait(Sde2D5BluffBurden.HIGH, Sde2D5BluffBurden.HIGH, Sde2D5BluffClaimCadence.RECURRING_NIGHTLY, Sde2D5BluffNarrativeRouteClass.INFORMATION)
        "Undertaker", "Ravenkeeper" ->
            trait(Sde2D5BluffBurden.HIGH, Sde2D5BluffBurden.HIGH, Sde2D5BluffClaimCadence.TRIGGERED, Sde2D5BluffNarrativeRouteClass.INFORMATION)
        "Monk" ->
            trait(Sde2D5BluffBurden.MODERATE, Sde2D5BluffBurden.MODERATE, Sde2D5BluffClaimCadence.RECURRING_NIGHTLY, Sde2D5BluffNarrativeRouteClass.PROTECTION)
        "Virgin", "Slayer" ->
            trait(Sde2D5BluffBurden.MODERATE, Sde2D5BluffBurden.MODERATE, Sde2D5BluffClaimCadence.TRIGGERED, Sde2D5BluffNarrativeRouteClass.PUBLIC_ABILITY)
        "Soldier", "Mayor", "Saint" ->
            trait(Sde2D5BluffBurden.LOW, Sde2D5BluffBurden.LOW, Sde2D5BluffClaimCadence.PASSIVE_OR_SOCIAL, Sde2D5BluffNarrativeRouteClass.PASSIVE_SURVIVAL)
        "Butler", "Recluse", "Drunk" ->
            trait(Sde2D5BluffBurden.LOW, Sde2D5BluffBurden.MODERATE, Sde2D5BluffClaimCadence.PASSIVE_OR_SOCIAL, Sde2D5BluffNarrativeRouteClass.SOCIAL_OUTSIDER)
        else -> error("Missing D5F bluff review trait metadata for ${role.value}.")
    }

    private fun trait(
        execution: Sde2D5BluffBurden,
        claim: Sde2D5BluffBurden,
        cadence: Sde2D5BluffClaimCadence,
        route: Sde2D5BluffNarrativeRouteClass,
    ) = Sde2D5BluffRoleTrait(
        beginnerExecutionBurden = execution,
        claimBurden = claim,
        claimCadence = cadence,
        narrativeRouteClass = route,
    )
}

internal object Sde2D5ExternalHumanBluffTriplets {
    private val observed: Map<Set<RoleId>, Set<String>> = mapOf(
        setOf(RoleId("Chef"), RoleId("Investigator"), RoleId("Saint")) to setOf("ct-01"),
        setOf(RoleId("Saint"), RoleId("Monk"), RoleId("Investigator")) to setOf("ct-03"),
    )

    fun caseIdsFor(roles: Collection<RoleId>): Set<String> =
        observed[roles.toSet()].orEmpty()
}

internal data class Sde2D5DemonBluffCalibrationEvidence(
    val candidateId: String,
    val recipientSeat: Int,
    val playerCount: Int,
    val regime: Sde2D5PlayerCountRegime,
    val profileKind: Sde2D5SetupProfileKind,
    val evidenceKind: Sde2D5EvidenceKind,
    val roles: List<RoleId>,
    val supportedRoles: Set<RoleId>,
    val supportedRoleCount: Int,
    val roleSupportNormalized: Map<RoleId, NormalizedStrategicDiagnostics>,
    val roleStrategicWorldCounts: Map<RoleId, Int>,
    val unionStrategicWorldCount: Int,
    val sharedStrategicWorldCount: Int,
    val sharedToUnionRetention: StrategicRatio,
    val distinctRoleStrategicPatternCount: Int,
    val individualSupportFloorStrategicWorldCount: Int,
    val roleTraits: Map<RoleId, Sde2D5BluffRoleTrait>,
    val claimCadenceClassCount: Int,
    val narrativeRouteClassCount: Int,
    val pairwiseStrategicCoverage: List<Sde2D5BluffPairwiseCoverage>,
    val externalHumanObservedCaseIds: Set<String>,
) {
    init {
        require(candidateId.isNotBlank())
        require(recipientSeat > 0)
        require(playerCount in 5..15)
        require(regime == Sde2D5PlayerCountRegime.from(playerCount))
        require(evidenceKind == Sde2D5EvidenceKind.DEMON_BLUFF_SUPPORT)
        require(roles.size == 3 && roles.distinct().size == 3)
        require(supportedRoles.all(roles::contains))
        require(supportedRoleCount == supportedRoles.size)
        require(roleSupportNormalized.keys == roles.toSet())
        require(roleStrategicWorldCounts.keys == roles.toSet())
        require(sharedStrategicWorldCount in 0..unionStrategicWorldCount)
        require(distinctRoleStrategicPatternCount in 1..3)
        require(individualSupportFloorStrategicWorldCount == roleStrategicWorldCounts.values.min())
        require(roleTraits.keys == roles.toSet())
        require(claimCadenceClassCount == roleTraits.values.map { it.claimCadence }.distinct().size)
        require(narrativeRouteClassCount == roleTraits.values.map { it.narrativeRouteClass }.distinct().size)
        require(pairwiseStrategicCoverage.size == 3)
    }
}

/**
 * Review-only projection over already-exact D2D2 Demon-bluff joint-output diagnostics.
 *
 * It does not own bluff legality or evaluate new counterworlds. Each role keeps its own normalized
 * support evidence, while union/shared support and shared-to-union retention stay separate axes.
 * No fragility threshold or aggregate score is introduced here.
 */
internal object Sde2D5DemonBluffCalibrationEvidenceProjector {
    fun project(
        playerCount: Int,
        profileKind: Sde2D5SetupProfileKind,
        recipientSeat: Int,
        diagnostic: DemonBluffJointOutputDiagnostics,
    ): Sde2D5DemonBluffCalibrationEvidence {
        require(playerCount in 5..15)
        require(recipientSeat > 0)

        val recipient = diagnostic.byRecipient.single { it.recipientSeat == recipientSeat }
        val supportDiagnostics = diagnostic.roleSupports.associate { support ->
            support.role to support.byRecipient.single { it.recipientSeat == recipientSeat }
        }
        require(supportDiagnostics.keys == diagnostic.roles.toSet()) {
            "Every Demon bluff role must expose support for the selected calibration recipient."
        }

        val roleStrategicKeys = diagnostic.roles.associateWith { role ->
            supportDiagnostics.getValue(role).afterStructure.strategicWorldKeys
        }
        val roleTraits = diagnostic.roles.associateWith(Sde2D5DemonBluffRoleTraits::forRole)
        val pairwiseCoverage = buildList {
            diagnostic.roles.indices.forEach { firstIndex ->
                for (secondIndex in firstIndex + 1 until diagnostic.roles.size) {
                    val firstRole = diagnostic.roles[firstIndex]
                    val secondRole = diagnostic.roles[secondIndex]
                    val firstKeys = roleStrategicKeys.getValue(firstRole)
                    val secondKeys = roleStrategicKeys.getValue(secondRole)
                    val union = firstKeys union secondKeys
                    val shared = firstKeys intersect secondKeys
                    add(
                        Sde2D5BluffPairwiseCoverage(
                            firstRole = firstRole,
                            secondRole = secondRole,
                            unionStrategicWorldCount = union.size,
                            sharedStrategicWorldCount = shared.size,
                            sharedToUnionRetention = StrategicRatio.bounded(
                                numerator = shared.size,
                                denominator = union.size,
                            ),
                        ),
                    )
                }
            }
        }

        return Sde2D5DemonBluffCalibrationEvidence(
            candidateId = diagnostic.candidateId,
            recipientSeat = recipientSeat,
            playerCount = playerCount,
            regime = Sde2D5PlayerCountRegime.from(playerCount),
            profileKind = profileKind,
            evidenceKind = Sde2D5EvidenceKind.DEMON_BLUFF_SUPPORT,
            roles = diagnostic.roles,
            supportedRoles = recipient.supportedRoles,
            supportedRoleCount = recipient.supportedRoles.size,
            roleSupportNormalized = diagnostic.roles.associateWith { role ->
                NormalizedStrategicDiagnosticsProjector.project(
                    diagnostic = supportDiagnostics.getValue(role),
                    playerCount = playerCount,
                )
            },
            roleStrategicWorldCounts = diagnostic.roles.associateWith { role ->
                supportDiagnostics.getValue(role).afterStructure.distinctStrategicWorldCount
            },
            unionStrategicWorldCount = recipient.unionStrategicWorldKeys.size,
            sharedStrategicWorldCount = recipient.sharedStrategicWorldKeys.size,
            sharedToUnionRetention = StrategicRatio.bounded(
                numerator = recipient.sharedStrategicWorldKeys.size,
                denominator = recipient.unionStrategicWorldKeys.size,
            ),
            distinctRoleStrategicPatternCount = recipient.distinctRoleStrategicPatternCount,
            individualSupportFloorStrategicWorldCount =
                roleStrategicKeys.values.minOf { it.size },
            roleTraits = roleTraits,
            claimCadenceClassCount = roleTraits.values.map { it.claimCadence }.distinct().size,
            narrativeRouteClassCount =
                roleTraits.values.map { it.narrativeRouteClass }.distinct().size,
            pairwiseStrategicCoverage = pairwiseCoverage,
            externalHumanObservedCaseIds =
                Sde2D5ExternalHumanBluffTriplets.caseIdsFor(diagnostic.roles),
        )
    }
}


internal enum class Sde2D5DemonBluffSelectionReason {
    LOWEST_SHARED_TO_UNION_REFERENCE,
    HIGHEST_SHARED_TO_UNION_REFERENCE,
    EXTERNAL_HUMAN_OBSERVED,
}

internal data class Sde2D5DemonBluffCalibrationSelection(
    val evidence: Sde2D5DemonBluffCalibrationEvidence,
    val selectionReasons: Set<Sde2D5DemonBluffSelectionReason>,
) {
    init {
        require(selectionReasons.isNotEmpty())
    }
}

/**
 * Deterministic calibration-point selection only.
 *
 * This chooses review contrasts at the observed low/high ends of shared strategic support. It does
 * not classify either end as acceptable/bad and it does not freeze a policy threshold.
 */
internal object Sde2D5DemonBluffCalibrationEvidenceSelector {
    fun selectReviewContrasts(
        evidence: List<Sde2D5DemonBluffCalibrationEvidence>,
    ): List<Sde2D5DemonBluffCalibrationSelection> {
        require(evidence.isNotEmpty())
        require(evidence.map(Sde2D5DemonBluffCalibrationEvidence::candidateId).distinct().size == evidence.size)

        val comparator = Comparator<Sde2D5DemonBluffCalibrationEvidence> { left, right ->
            val ratioOrder = compareRatio(
                left.sharedToUnionRetention,
                right.sharedToUnionRetention,
            )
            if (ratioOrder != 0) ratioOrder else left.candidateId.compareTo(right.candidateId)
        }
        val lowest = evidence.minWith(comparator)
        val highest = evidence.maxWith(comparator)

        val selected = linkedMapOf<String, Pair<Sde2D5DemonBluffCalibrationEvidence, MutableSet<Sde2D5DemonBluffSelectionReason>>>()
        fun add(
            point: Sde2D5DemonBluffCalibrationEvidence,
            reason: Sde2D5DemonBluffSelectionReason,
        ) {
            val entry = selected.getOrPut(point.candidateId) { point to linkedSetOf() }
            entry.second += reason
        }

        add(lowest, Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION_REFERENCE)
        add(highest, Sde2D5DemonBluffSelectionReason.HIGHEST_SHARED_TO_UNION_REFERENCE)
        evidence
            .filter { it.externalHumanObservedCaseIds.isNotEmpty() }
            .forEach { observed ->
                add(observed, Sde2D5DemonBluffSelectionReason.EXTERNAL_HUMAN_OBSERVED)
            }

        return selected.values
            .map { (point, reasons) ->
                Sde2D5DemonBluffCalibrationSelection(
                    evidence = point,
                    selectionReasons = reasons.toSet(),
                )
            }
            .sortedBy { it.evidence.candidateId }
    }

    private fun compareRatio(
        left: StrategicRatio,
        right: StrategicRatio,
    ): Int = when {
        left is StrategicRatio.Undefined && right is StrategicRatio.Undefined -> 0
        left is StrategicRatio.Undefined -> -1
        right is StrategicRatio.Undefined -> 1
        left is StrategicRatio.Defined && right is StrategicRatio.Defined ->
            (left.numerator.toLong() * right.denominator.toLong())
                .compareTo(right.numerator.toLong() * left.denominator.toLong())
        else -> error("Unknown strategic ratio implementation.")
    }
}
