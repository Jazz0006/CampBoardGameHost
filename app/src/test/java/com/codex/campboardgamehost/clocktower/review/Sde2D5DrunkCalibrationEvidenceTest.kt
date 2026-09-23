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

    @Test
    fun `same setup contrast keeps truthful mild and stronger false candidates separate`() {
        fun evidence(
            candidateId: String,
            truth: SemanticTruth,
            topologyNumerator: Int,
        ): Sde2D5DrunkCalibrationEvidence {
            val point = Sde2D5CalibrationEvidencePoint(
                pointId = "$candidateId:healthy",
                playerCount = 6,
                regime = Sde2D5PlayerCountRegime.FIVE_TO_SIX,
                profileKind = Sde2D5SetupProfileKind.BARON,
                evidenceKind = Sde2D5EvidenceKind.DRUNK_HEALTHY_CORE,
                contrastId = candidateId,
                beforeStrategicWorldCount = 6,
                afterStrategicWorldCount = 5,
                normalized = com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics(
                    demonCoverRetention = StrategicRatio.Defined(4, 5),
                    evilTopologyRetention = StrategicRatio.Defined(5, 6),
                    evilCoverRetention = StrategicRatio.Defined(5, 6),
                    forcedGoodFraction = StrategicRatio.Defined(1, 6),
                ),
                rawMechanicalBefore = BigInteger.valueOf(100),
                rawMechanicalAfter = BigInteger.valueOf(80),
            )
            return Sde2D5DrunkCalibrationEvidence(
                candidateId = candidateId,
                semanticTruth = truth,
                healthyCore = point,
                fullBundle = point.copy(
                    pointId = "$candidateId:full",
                    evidenceKind = Sde2D5EvidenceKind.DRUNK_FULL_BUNDLE,
                    afterStrategicWorldCount = topologyNumerator,
                ),
                marginalNormalized =
                    com.codex.campboardgamehost.clocktower.recommendation.sde.NormalizedStrategicDiagnostics(
                        demonCoverRetention = StrategicRatio.Defined(topologyNumerator, 5),
                        evilTopologyRetention = StrategicRatio.Defined(topologyNumerator, 5),
                        evilCoverRetention = StrategicRatio.Defined(topologyNumerator, 5),
                        forcedGoodFraction = StrategicRatio.Defined(1, 6),
                    ),
                rawWorldsRemoved = BigInteger.valueOf((5 - topologyNumerator).toLong()),
            )
        }

        val truthful = evidence("value-1", SemanticTruth.TRUE, 4)
        val mildFalse = evidence("value-0", SemanticTruth.FALSE, 3)
        val strongerFalse = evidence("value-2", SemanticTruth.FALSE, 2)

        val contrast = Sde2D5DrunkCalibrationContrastBuilder.build(
            contrastId = "same-setup",
            evidence = listOf(strongerFalse, truthful, mildFalse),
        )

        assertEquals("value-1", contrast.truthful.candidateId)
        assertEquals("value-0", contrast.mildFalse.candidateId)
        assertEquals("value-2", contrast.strongerFalse?.candidateId)
        assertEquals(
            truthful.marginalNormalized,
            contrast.counterfactualHealthyTruthDanger,
        )
        assertEquals(
            listOf(
                Sde2D5DrunkContrastCandidateKind.TRUTHFUL,
                Sde2D5DrunkContrastCandidateKind.MILD_FALSE,
                Sde2D5DrunkContrastCandidateKind.STRONGER_FALSE,
            ),
            contrast.candidates.map { it.kind },
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
