package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkMarginalDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightDrunkWholeBundleCandidateExactEvaluation
import com.codex.campboardgamehost.clocktower.recommendation.sde.StrategicRatio
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Test

class Sde2D5DrunkCalibrationEvidenceTest {
    @Test
    fun `projects HealthyCore FullBundle and DrunkMarginal as separate calibration axes`() {
        val baseline = structure(
            demons = setOf(1, 2, 3, 4),
            keys = setOf(
                key(1, 5),
                key(1, 6),
                key(2, 5),
                key(2, 6),
                key(3, 5),
                key(4, 5),
            ),
            forcedGood = emptySet(),
            evilCover = setOf(1, 2, 3, 4, 5, 6),
        )
        val healthyAfter = structure(
            demons = setOf(1, 2, 3),
            keys = setOf(
                key(1, 5),
                key(1, 6),
                key(2, 5),
                key(3, 5),
            ),
            forcedGood = setOf(4),
            evilCover = setOf(1, 2, 3, 5, 6),
        )
        val fullAfter = structure(
            demons = setOf(1, 2),
            keys = setOf(
                key(1, 5),
                key(2, 5),
            ),
            forcedGood = setOf(3, 4, 6),
            evilCover = setOf(1, 2, 5),
        )
        val healthy = diagnostic(
            id = "healthy",
            beforeWorlds = 100,
            afterWorlds = 60,
            before = baseline,
            after = healthyAfter,
        )
        val full = diagnostic(
            id = "full",
            beforeWorlds = 100,
            afterWorlds = 30,
            before = baseline,
            after = fullAfter,
        )
        val candidate = FirstNightDrunkWholeBundleCandidateExactEvaluation(
            candidateId = "drunk-candidate",
            semanticTruth = SemanticTruth.FALSE,
            publicObservation = publicObservation(),
            fullBundleByRecipient = listOf(full),
            marginalByRecipient = listOf(
                FirstNightDrunkMarginalDiagnostics(
                    recipientSeat = 1,
                    rawWorldsRemoved = BigInteger.valueOf(30),
                    removedDemonSeats = setOf(3),
                    removedEvilTeamConfigurations = setOf(setOf(1, 6), setOf(3, 5)),
                    newlyForcedGoodSeats = setOf(3, 6),
                    newlyForcedEvilSeats = emptySet(),
                    removedEvilCoverSeats = setOf(3, 6),
                ),
            ),
        )

        val evidence = Sde2D5DrunkCalibrationEvidenceProjector.project(
            playerCount = 6,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            healthyCore = healthy,
            candidate = candidate,
        )

        assertEquals("drunk-candidate", evidence.candidateId)
        assertEquals(SemanticTruth.FALSE, evidence.semanticTruth)
        assertEquals(Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE, evidence.healthyCore.evidenceKind)
        assertEquals(Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE, evidence.fullBundle.evidenceKind)
        assertEquals("drunk-candidate", evidence.healthyCore.contrastId)
        assertEquals("drunk-candidate", evidence.fullBundle.contrastId)
        assertEquals(BigInteger.valueOf(30), evidence.rawWorldsRemoved)

        assertEquals(StrategicRatio.Defined(3, 4), evidence.healthyCore.normalized.demonCoverRetention)
        assertEquals(StrategicRatio.Defined(4, 6), evidence.healthyCore.normalized.evilTopologyRetention)
        assertEquals(StrategicRatio.Defined(2, 3), evidence.marginalNormalized.demonCoverRetention)
        assertEquals(StrategicRatio.Defined(2, 4), evidence.marginalNormalized.evilTopologyRetention)
        assertEquals(StrategicRatio.Defined(3, 5), evidence.marginalNormalized.evilCoverRetention)
        assertEquals(StrategicRatio.Defined(3, 6), evidence.marginalNormalized.forcedGoodFraction)
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
        demons: Set<Int>,
        keys: Set<StrategicWorldKey>,
        forcedGood: Set<Int>,
        evilCover: Set<Int>,
    ) = ExactWorldStructureDiagnostics(
        possibleDemonSeats = demons,
        evilTeamSeatConfigurations = keys.mapTo(linkedSetOf()) { key ->
            setOf(key.demonSeat) + key.minionSeats
        },
        strategicWorldKeys = keys,
        forcedGoodSeats = forcedGood,
        forcedEvilSeats = emptySet(),
        evilCoverSeats = evilCover,
    )

    private fun key(demon: Int, minion: Int) =
        StrategicWorldKey(demonSeat = demon, minionSeats = listOf(minion))

    private fun publicObservation() = EpistemicObservation(
        observationId = "drunk-public",
        snapshotId = "d5c",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 1,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.RoleInPlay(
            role = com.codex.campboardgamehost.clocktower.domain.RoleId("Chef"),
            inPlay = true,
        ),
    )
}
