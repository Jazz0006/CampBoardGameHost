package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class Sde2D5RoleInformationContrastEvidenceTest {
    @Test
    fun `near raw contrast rejects mathematically equal topology retention ratios`() {
        val four = structure(
            setOf(
                key(1, 6),
                key(2, 6),
                key(3, 6),
                key(4, 6),
            ),
        )
        val five = structure(
            setOf(
                key(1, 6),
                key(2, 6),
                key(3, 6),
                key(4, 6),
                key(5, 6),
            ),
        )
        val first = Sde2D5RoleInformationCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            contrastId = "ratio-equivalence",
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            diagnostic = diagnostic(
                id = "four-of-four",
                beforeWorlds = 100,
                afterWorlds = 50,
                before = four,
                after = four,
            ),
        )
        val second = Sde2D5RoleInformationCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            contrastId = "ratio-equivalence",
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            diagnostic = diagnostic(
                id = "five-of-five",
                beforeWorlds = 100,
                afterWorlds = 50,
                before = five,
                after = five,
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            Sde2D5RoleInformationNearRawContrast(
                first = first,
                second = second,
                rawWorldRemovalDifference = BigInteger.ZERO,
            )
        }
    }

    @Test
    fun `mechanical information can be useful while evil topology is unchanged`() {
        val topology = structure(
            setOf(
                key(1, 6),
                key(2, 6),
                key(3, 6),
                key(4, 6),
            ),
        )
        val evidence = Sde2D5RoleInformationCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            contrastId = "topology-neutral-role-information",
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            diagnostic = diagnostic(
                id = "topology-neutral",
                beforeWorlds = 100,
                afterWorlds = 40,
                before = topology,
                after = topology,
            ),
        )

        assertEquals(Sde2D5EvidenceKind.STRATEGIC_ROLE_INFORMATION_CONTRAST, evidence.point.evidenceKind)
        assertEquals(BigInteger.valueOf(60), evidence.rawWorldsRemoved)
        assertTrue(evidence.hasMechanicalInformationGain)
        assertTrue(evidence.topologyNeutral)
        assertEquals(
            StrategicRatio.Defined(4, 4),
            evidence.point.normalized.evilTopologyRetention,
        )
    }

    @Test
    fun `equal raw world retention can hide materially different evil topology retention`() {
        val before = structure(
            setOf(
                key(1, 6),
                key(2, 6),
                key(3, 6),
                key(4, 6),
            ),
        )
        val topologyNeutral = Sde2D5RoleInformationCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            contrastId = "matched-raw",
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            diagnostic = diagnostic(
                id = "matched-raw-neutral",
                beforeWorlds = 100,
                afterWorlds = 50,
                before = before,
                after = before,
            ),
        )
        val topologyCollapsing = Sde2D5RoleInformationCalibrationEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            contrastId = "matched-raw",
            sourceSeat = 2,
            sourceRole = RoleId("Chef"),
            control = FirstNightBundleEntryControl.RULE_DETERMINED,
            diagnostic = diagnostic(
                id = "matched-raw-collapse",
                beforeWorlds = 100,
                afterWorlds = 50,
                before = before,
                after = structure(setOf(key(1, 6), key(2, 6))),
            ),
        )

        val contrast = Sde2D5RoleInformationMatchedRawContrast.exact(
            topologyNeutral,
            topologyCollapsing,
        )

        assertEquals(BigInteger.valueOf(100), contrast.beforeWorlds)
        assertEquals(BigInteger.valueOf(50), contrast.afterWorlds)
        assertTrue(contrast.first.topologyNeutral)
        assertFalse(contrast.second.topologyNeutral)
        assertEquals(
            StrategicRatio.Defined(4, 4),
            contrast.first.point.normalized.evilTopologyRetention,
        )
        assertEquals(
            StrategicRatio.Defined(2, 4),
            contrast.second.point.normalized.evilTopologyRetention,
        )
    }

    private fun diagnostic(
        id: String,
        beforeWorlds: Long,
        afterWorlds: Long,
        before: ExactWorldStructureDiagnostics,
        after: ExactWorldStructureDiagnostics,
    ) = ExactHypotheticalObservationBundleDiagnostics(
        bundleId = id,
        recipientSeat = 1,
        before = WorldCardinality.Exact(BigInteger.valueOf(beforeWorlds)),
        after = WorldCardinality.Exact(BigInteger.valueOf(afterWorlds)),
        beforeStructure = before,
        afterStructure = after,
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
