package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmationChainFeaturesProjectorTest {
    @Test
    fun `classifies support contradiction independent contribution and no contribution exactly`() {
        val keyA = key(1, 6)
        val keyB = key(2, 6)
        val keyC = key(3, 6)
        val full = diagnostic(
            beforeWorlds = 40,
            afterWorlds = 10,
            beforeKeys = setOf(keyA, keyB),
            afterKeys = setOf(keyA),
        )
        val currentSource = SdeDecisionSourceInteraction(
            interactionId = "current",
            sourceSeat = 3,
            abilityRole = RoleId("current-channel"),
        )

        val support = ConfirmationChainFeaturesProjector.project(
            currentSource = currentSource,
            candidateId = "candidate",
            full = full,
            leaveOneOut = listOf(
                evidence(
                    id = "support",
                    sequence = 1,
                    sourceSeat = 2,
                    sourceAbility = RoleId("support-channel"),
                    without = diagnostic(
                        beforeWorlds = 80,
                        afterWorlds = 10,
                        beforeKeys = setOf(keyA, keyB, keyC),
                        afterKeys = setOf(keyA),
                    ),
                ),
            ),
        )
        assertEquals(
            ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION,
            support.historicalObservationImpacts.single().relation,
        )
        assertEquals(setOf("support"), support.supportingObservationIds)
        assertTrue(support.historicalObservationImpacts.single().authenticatesDistinctSource)
        assertTrue(
            support.historicalObservationImpacts.single().wasIndependentlyConstrainingBefore,
        )

        val contradiction = ConfirmationChainFeaturesProjector.project(
            currentSource = currentSource,
            candidateId = "candidate",
            full = full.copy(
                after = exact(0),
                afterStructure = structure(emptySet()),
            ),
            leaveOneOut = listOf(
                evidence(
                    id = "contradiction",
                    sequence = 2,
                    sourceSeat = 4,
                    sourceAbility = RoleId("contradiction-channel"),
                    without = diagnostic(
                        beforeWorlds = 80,
                        afterWorlds = 12,
                        beforeKeys = setOf(keyA, keyB, keyC),
                        afterKeys = setOf(keyB),
                    ),
                ),
            ),
        )
        assertEquals(
            ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION,
            contradiction.historicalObservationImpacts.single().relation,
        )
        assertEquals(setOf("contradiction"), contradiction.contradictedObservationIds)

        val independent = ConfirmationChainFeaturesProjector.project(
            currentSource = currentSource,
            candidateId = "candidate",
            full = full,
            leaveOneOut = listOf(
                evidence(
                    id = "independent",
                    sequence = 3,
                    sourceSeat = 5,
                    sourceAbility = RoleId("independent-channel"),
                    without = diagnostic(
                        beforeWorlds = 80,
                        afterWorlds = 25,
                        beforeKeys = setOf(keyA, keyB, keyC),
                        afterKeys = setOf(keyA, keyC),
                    ),
                ),
            ),
        )
        val independentImpact = independent.historicalObservationImpacts.single()
        assertEquals(
            ConfirmationObservationRelation.INDEPENDENT_CONTRIBUTION,
            independentImpact.relation,
        )
        assertEquals(BigInteger.valueOf(15), independentImpact.restoration.restoredExactWorldCount)
        assertEquals(setOf(keyC), independentImpact.restoration.restoredStrategicWorldKeys)
        assertEquals(setOf(3), independentImpact.restoration.restoredDemonSeats)

        val none = ConfirmationChainFeaturesProjector.project(
            currentSource = currentSource,
            candidateId = "candidate",
            full = full,
            leaveOneOut = listOf(
                evidence(
                    id = "none",
                    sequence = 4,
                    sourceSeat = 5,
                    sourceAbility = RoleId("independent-channel"),
                    without = full,
                ),
            ),
        )
        assertEquals(
            ConfirmationObservationRelation.NO_CONTRIBUTION,
            none.historicalObservationImpacts.single().relation,
        )
        assertFalse(none.historicalObservationImpacts.single().restoration.restoresAnyAmbiguity)
        assertFalse(
            none.historicalObservationImpacts.single().wasIndependentlyConstrainingBefore,
        )
    }

    @Test
    fun `records candidate ambiguity removal and multi channel collapse without a score`() {
        val keyA = key(1, 6)
        val keyB = key(2, 6)
        val keyC = key(3, 6)
        val full = diagnostic(
            beforeWorlds = 100,
            afterWorlds = 20,
            beforeKeys = setOf(keyA, keyB, keyC),
            afterKeys = setOf(keyA),
        )

        val features = ConfirmationChainFeaturesProjector.project(
            currentSource = SdeDecisionSourceInteraction(
                interactionId = "current",
                sourceSeat = 1,
                abilityRole = RoleId("current-channel"),
            ),
            candidateId = "candidate",
            full = full,
            leaveOneOut = listOf(
                evidence(
                    id = "history-a",
                    sequence = 1,
                    sourceSeat = 2,
                    sourceAbility = RoleId("history-a-channel"),
                    without = diagnostic(
                        beforeWorlds = 140,
                        afterWorlds = 35,
                        beforeKeys = setOf(keyA, keyB, keyC),
                        afterKeys = setOf(keyA, keyB),
                    ),
                ),
                evidence(
                    id = "history-b",
                    sequence = 2,
                    sourceSeat = 3,
                    sourceAbility = RoleId("history-b-channel"),
                    without = diagnostic(
                        beforeWorlds = 150,
                        afterWorlds = 45,
                        beforeKeys = setOf(keyA, keyB, keyC),
                        afterKeys = setOf(keyA, keyC),
                    ),
                ),
            ),
        )

        assertEquals(BigInteger.valueOf(80), features.candidateRemovedExactWorldCount)
        assertEquals(setOf(keyB, keyC), features.candidateRemovedStrategicWorldKeys)
        assertEquals(setOf(2, 3), features.candidateRemovedDemonSeats)
        assertEquals(setOf("history-a", "history-b"), features.independentlyContributingObservationIds)
        assertEquals(3, features.contributingChannels.size)
        assertTrue(features.hasMultiChannelCollapse)
    }

    private fun evidence(
        id: String,
        sequence: Long,
        sourceSeat: Int?,
        sourceAbility: RoleId?,
        without: ExactHypotheticalObservationBundleDiagnostics,
    ) = ConfirmationLeaveOneOutEvidence(
        observationRef = SdeHistoricalObservationRef(
            recordId = id,
            globalSequence = sequence,
        ),
        sourceSeat = sourceSeat,
        sourceAbility = sourceAbility,
        withoutObservation = without,
    )

    private fun diagnostic(
        beforeWorlds: Long,
        afterWorlds: Long,
        beforeKeys: Set<StrategicWorldKey>,
        afterKeys: Set<StrategicWorldKey>,
    ) = ExactHypotheticalObservationBundleDiagnostics(
        bundleId = "bundle",
        recipientSeat = 1,
        before = exact(beforeWorlds),
        after = exact(afterWorlds),
        beforeStructure = structure(beforeKeys),
        afterStructure = structure(afterKeys),
    )

    private fun structure(keys: Set<StrategicWorldKey>) = ExactWorldStructureDiagnostics(
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

    private fun exact(value: Long) =
        WorldCardinality.Exact(BigInteger.valueOf(value))
}
