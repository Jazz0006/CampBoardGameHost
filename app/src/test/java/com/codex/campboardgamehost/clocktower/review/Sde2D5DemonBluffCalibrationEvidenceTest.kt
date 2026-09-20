package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffJointOutputDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffRoleSupport
import com.codex.campboardgamehost.clocktower.recommendation.sde.DemonBluffTripletRecipientDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D5DemonBluffCalibrationEvidenceTest {
    @Test
    fun `projects bluff support and shared fragility as separate calibration axes`() {
        val monk = RoleId("Monk")
        val soldier = RoleId("Soldier")
        val butler = RoleId("Butler")
        val baseline = structure(
            setOf(
                key(1, 5),
                key(2, 5),
                key(3, 5),
                key(4, 5),
            ),
        )
        val monkAfter = structure(setOf(key(1, 5), key(2, 5), key(3, 5)))
        val soldierAfter = structure(setOf(key(1, 5), key(2, 5)))
        val butlerAfter = structure(setOf(key(1, 5)))

        val supports = listOf(
            support(monk, baseline, monkAfter, afterWorlds = 30),
            support(soldier, baseline, soldierAfter, afterWorlds = 20),
            support(butler, baseline, butlerAfter, afterWorlds = 10),
        )
        val triplet = DemonBluffJointOutputDiagnostics(
            candidateId = "bluff-triplet",
            roles = listOf(monk, soldier, butler),
            roleSupports = supports,
            byRecipient = listOf(
                DemonBluffTripletRecipientDiagnostics(
                    recipientSeat = 1,
                    supportedRoles = setOf(monk, soldier, butler),
                    unionEvilTeamSeatConfigurations = setOf(
                        setOf(1, 5),
                        setOf(2, 5),
                        setOf(3, 5),
                    ),
                    sharedEvilTeamSeatConfigurations = setOf(setOf(1, 5)),
                    distinctRoleTopologyPatternCount = 3,
                    unionStrategicWorldKeys = setOf(
                        key(1, 5),
                        key(2, 5),
                        key(3, 5),
                    ),
                    sharedStrategicWorldKeys = setOf(key(1, 5)),
                    distinctRoleStrategicPatternCount = 3,
                ),
            ),
        )

        val evidence = Sde2D5DemonBluffCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            recipientSeat = 1,
            diagnostic = triplet,
        )

        assertEquals("bluff-triplet", evidence.candidateId)
        assertEquals(Sde2D5EvidenceKind.DEMON_BLUFF_SUPPORT, evidence.evidenceKind)
        assertEquals(setOf(monk, soldier, butler), evidence.supportedRoles)
        assertEquals(3, evidence.supportedRoleCount)
        assertEquals(3, evidence.unionStrategicWorldCount)
        assertEquals(1, evidence.sharedStrategicWorldCount)
        assertEquals(StrategicRatio.Defined(1, 3), evidence.sharedToUnionRetention)
        assertEquals(3, evidence.distinctRoleStrategicPatternCount)
        assertEquals(1, evidence.individualSupportFloorStrategicWorldCount)
        assertEquals(3, evidence.pairwiseStrategicCoverage.size)
        assertTrue(evidence.claimCadenceClassCount >= 1)
        assertTrue(evidence.narrativeRouteClassCount >= 1)
        assertEquals(roles.toSet(), evidence.roleTraits.keys)
        assertEquals(
            StrategicRatio.Defined(3, 4),
            evidence.roleSupportNormalized.getValue(monk).evilTopologyRetention,
        )
        assertEquals(
            StrategicRatio.Defined(2, 4),
            evidence.roleSupportNormalized.getValue(soldier).evilTopologyRetention,
        )
        assertEquals(
            StrategicRatio.Defined(1, 4),
            evidence.roleSupportNormalized.getValue(butler).evilTopologyRetention,
        )
    }

    @Test
    fun `selects deterministic low and high shared support contrasts without thresholds`() {
        val monk = RoleId("Monk")
        val soldier = RoleId("Soldier")
        val butler = RoleId("Butler")
        val baseline = structure(
            setOf(
                key(1, 5),
                key(2, 5),
                key(3, 5),
                key(4, 5),
            ),
        )
        val common = structure(setOf(key(1, 5), key(2, 5), key(3, 5)))
        val robust = projectTriplet(
            candidateId = "robust",
            roles = listOf(monk, soldier, butler),
            supports = listOf(
                support(monk, baseline, common, 30),
                support(soldier, baseline, common, 30),
                support(butler, baseline, common, 30),
            ),
            union = common.strategicWorldKeys,
            shared = common.strategicWorldKeys,
            distinctPatterns = 1,
        )
        val fragile = projectTriplet(
            candidateId = "fragile",
            roles = listOf(monk, soldier, butler),
            supports = listOf(
                support(monk, baseline, common, 30),
                support(soldier, baseline, structure(setOf(key(1, 5), key(2, 5))), 20),
                support(butler, baseline, structure(setOf(key(1, 5))), 10),
            ),
            union = common.strategicWorldKeys,
            shared = setOf(key(1, 5)),
            distinctPatterns = 3,
        )

        val selected = Sde2D5DemonBluffCalibrationEvidenceSelector.selectReviewContrasts(
            listOf(robust, fragile),
        )

        assertEquals(
            setOf(
                Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION_REFERENCE,
                Sde2D5DemonBluffSelectionReason.HIGHEST_SHARED_TO_UNION_REFERENCE,
            ),
            selected.flatMapTo(linkedSetOf()) { it.selectionReasons },
        )
        assertEquals(
            setOf("fragile", "robust"),
            selected.mapTo(linkedSetOf()) { it.evidence.candidateId },
        )
        assertEquals(
            setOf(Sde2D5DemonBluffSelectionReason.LOWEST_SHARED_TO_UNION_REFERENCE),
            selected.single { it.evidence.candidateId == "fragile" }.selectionReasons,
        )
        assertEquals(
            setOf(Sde2D5DemonBluffSelectionReason.HIGHEST_SHARED_TO_UNION_REFERENCE),
            selected.single { it.evidence.candidateId == "robust" }.selectionReasons,
        )
    }

    @Test
    fun `human observed bluff triplet is selected without turning observation into a score`() {
        val saint = RoleId("Saint")
        val monk = RoleId("Monk")
        val investigator = RoleId("Investigator")
        val baseline = structure(setOf(key(1, 5), key(2, 5), key(3, 5)))
        val common = structure(setOf(key(1, 5), key(2, 5)))
        val observed = projectTriplet(
            candidateId = "observed-ct03",
            roles = listOf(saint, monk, investigator),
            supports = listOf(
                support(saint, baseline, common, 20),
                support(monk, baseline, common, 20),
                support(investigator, baseline, common, 20),
            ),
            union = common.strategicWorldKeys,
            shared = common.strategicWorldKeys,
            distinctPatterns = 1,
        )

        assertEquals(setOf("ct-03"), observed.externalHumanObservedCaseIds)
        assertEquals(3, observed.narrativeRouteClassCount)
        assertEquals(3, observed.claimCadenceClassCount)

        val selected = Sde2D5DemonBluffCalibrationEvidenceSelector.selectReviewContrasts(
            listOf(
                observed,
                projectTriplet(
                    candidateId = "other",
                    roles = listOf(RoleId("Soldier"), RoleId("Mayor"), RoleId("Butler")),
                    supports = listOf(
                        support(RoleId("Soldier"), baseline, common, 20),
                        support(RoleId("Mayor"), baseline, common, 20),
                        support(RoleId("Butler"), baseline, common, 20),
                    ),
                    union = common.strategicWorldKeys,
                    shared = common.strategicWorldKeys,
                    distinctPatterns = 1,
                ),
            ),
        )
        assertTrue(
            Sde2D5DemonBluffSelectionReason.EXTERNAL_HUMAN_OBSERVED in
                selected.single { it.evidence.candidateId == "observed-ct03" }.selectionReasons,
        )
    }

    private fun projectTriplet(
        candidateId: String,
        roles: List<RoleId>,
        supports: List<DemonBluffRoleSupport>,
        union: Set<StrategicWorldKey>,
        shared: Set<StrategicWorldKey>,
        distinctPatterns: Int,
    ): Sde2D5DemonBluffCalibrationEvidence {
        val triplet = DemonBluffJointOutputDiagnostics(
            candidateId = candidateId,
            roles = roles,
            roleSupports = supports,
            byRecipient = listOf(
                DemonBluffTripletRecipientDiagnostics(
                    recipientSeat = 1,
                    supportedRoles = roles.toSet(),
                    unionEvilTeamSeatConfigurations = union.mapTo(linkedSetOf()) { key ->
                        setOf(key.demonSeat) + key.minionSeats
                    },
                    sharedEvilTeamSeatConfigurations = shared.mapTo(linkedSetOf()) { key ->
                        setOf(key.demonSeat) + key.minionSeats
                    },
                    distinctRoleTopologyPatternCount = distinctPatterns,
                    unionStrategicWorldKeys = union,
                    sharedStrategicWorldKeys = shared,
                    distinctRoleStrategicPatternCount = distinctPatterns,
                ),
            ),
        )
        return Sde2D5DemonBluffCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            recipientSeat = 1,
            diagnostic = triplet,
        )
    }

    private fun support(
        role: RoleId,
        before: ExactWorldStructureDiagnostics,
        after: ExactWorldStructureDiagnostics,
        afterWorlds: Long,
    ) = DemonBluffRoleSupport(
        role = role,
        byRecipient = listOf(
            ExactHypotheticalObservationBundleDiagnostics(
                bundleId = "support-${role.value}",
                recipientSeat = 1,
                before = WorldCardinality.Exact(BigInteger.valueOf(40)),
                after = WorldCardinality.Exact(BigInteger.valueOf(afterWorlds)),
                beforeStructure = before,
                afterStructure = after,
            ),
        ),
    )

    private fun structure(
        keys: Set<StrategicWorldKey>,
    ) = ExactWorldStructureDiagnostics(
        possibleDemonSeats = keys.mapTo(linkedSetOf(), StrategicWorldKey::demonSeat),
        evilTeamSeatConfigurations = keys.mapTo(linkedSetOf()) { key ->
            setOf(key.demonSeat) + key.minionSeats
        },
        strategicWorldKeys = keys,
        forcedGoodSeats = emptySet(),
        forcedEvilSeats = emptySet(),
        evilCoverSeats = keys.flatMapTo(linkedSetOf()) { key ->
            listOf(key.demonSeat) + key.minionSeats
        },
    )

    private fun key(demon: Int, minion: Int) =
        StrategicWorldKey(demonSeat = demon, minionSeats = listOf(minion))
}
