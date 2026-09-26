package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TruthCredibilityFeaturesProjectorTest {
    private val chefSource = ConfirmationChannelRef.Source(
        sourceSeat = 1,
        sourceAbility = RoleId("Chef"),
    )

    @Test
    fun `independently constraining healthy source preserves exact descriptive impact`() {
        val impact = TruthDangerSourceImpact(
            source = chefSource,
            exactWorldReduction = BigInteger.valueOf(12L),
            strategicWorldKeysRemoved = setOf(
                StrategicWorldKey(demonSeat = 5, minionSeats = listOf(4)),
            ),
            demonSeatsRemoved = setOf(3),
        )

        val projected = TruthCredibilityFeaturesProjector.project(
            listOf(
                TruthCredibilityCandidateEvidence(
                    candidateId = "red-herring-seat-1",
                    truthDangerSources = listOf(impact),
                ),
            ),
        ).getValue("red-herring-seat-1")

        assertEquals(setOf(impact), projected.truthDangerSources)
        assertTrue(projected.truthDangerSources.single().independentlyConstraining)
        assertTrue(projected.credibilityDisruptions.isEmpty())
        assertTrue(projected.unresolvedSourceRefs.isEmpty())
    }

    @Test
    fun `zero independent contribution remains descriptive without becoming a score`() {
        val impact = TruthDangerSourceImpact(
            source = chefSource,
            exactWorldReduction = BigInteger.ZERO,
            strategicWorldKeysRemoved = emptySet(),
            demonSeatsRemoved = emptySet(),
        )

        val projected = TruthCredibilityFeaturesProjector.project(
            listOf(
                TruthCredibilityCandidateEvidence(
                    candidateId = "candidate",
                    truthDangerSources = listOf(impact),
                ),
            ),
        ).getValue("candidate")

        assertEquals(setOf(impact), projected.truthDangerSources)
        assertFalse(projected.truthDangerSources.single().independentlyConstraining)
    }

    @Test
    fun `red herring commitment creates typed credibility disruption for one source`() {
        val redHerringRef = CommittedDecisionInputRef(
            inputId = "red-herring-seat-1",
            ownerId = "setup:red-herring",
            kind = SdeCommittedDecisionInputKind.RED_HERRING,
        )
        val disruption = CredibilityDisruptionImpact(
            committedInputRef = redHerringRef,
            affectedSource = chefSource,
            mechanism = CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE,
        )

        val projected = TruthCredibilityFeaturesProjector.project(
            listOf(
                TruthCredibilityCandidateEvidence(
                    candidateId = "red-herring-seat-1",
                    credibilityDisruptions = listOf(disruption),
                ),
            ),
        ).getValue("red-herring-seat-1")

        assertEquals(setOf(disruption), projected.credibilityDisruptions)
        assertEquals(
            SdeCommittedDecisionInputKind.RED_HERRING,
            projected.credibilityDisruptions.single().committedInputRef.kind,
        )
    }

    @Test
    fun `unresolved future information source remains explicit rather than neutral`() {
        val unresolved = ConfirmationChannelRef.Source(
            sourceSeat = 3,
            sourceAbility = RoleId("Washerwoman"),
        )

        val projected = TruthCredibilityFeaturesProjector.project(
            listOf(
                TruthCredibilityCandidateEvidence(
                    candidateId = "candidate",
                    unresolvedSourceRefs = setOf(unresolved),
                ),
            ),
        ).getValue("candidate")

        assertEquals(setOf(unresolved), projected.unresolvedSourceRefs)
        assertTrue(projected.truthDangerSources.isEmpty())
        assertTrue(projected.credibilityDisruptions.isEmpty())
    }

    @Test
    fun `red herring disruption cannot be backed by a different committed input kind`() {
        val wrongRef = CommittedDecisionInputRef(
            inputId = "not-red-herring",
            ownerId = "setup",
            kind = SdeCommittedDecisionInputKind.OTHER,
        )

        assertThrows(IllegalArgumentException::class.java) {
            CredibilityDisruptionImpact(
                committedInputRef = wrongRef,
                affectedSource = chefSource,
                mechanism = CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE,
            )
        }
    }
}