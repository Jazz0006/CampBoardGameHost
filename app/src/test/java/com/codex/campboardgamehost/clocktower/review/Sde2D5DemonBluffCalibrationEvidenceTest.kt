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
