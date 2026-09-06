package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightInformationMigrationTest {
    @Test fun `all Batch 4 first-night families publish only after shadow parity`() {
        FirstNightInformationFamily.entries.forEachIndexed { index, family ->
            val request = request(family, index + 1, ReliabilityState.RELIABLE, "same")
            val migration = FirstNightInformationMigration().publishIfShadowMatches(request)
            assertTrue("${family.name} should be ready", migration.isReady(request.decisionId))
        }
    }

    @Test fun `shadow mismatch preserves legacy path and does not publish`() {
        val request = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.POISONED, "legacy", "migrated")
        val migration = FirstNightInformationMigration().publishIfShadowMatches(request)
        val result = migration.shadow(request) as FirstNightShadowResult.Mismatch
        assertEquals(setOf("legacy"), result.legacyOnly)
        assertEquals(setOf("migrated"), result.migratedOnly)
        assertFalse(migration.isReady(request.decisionId))
    }

    @Test fun `authoritative pair domain publishes despite intentional legacy mismatch and commits migrated selection`() {
        val request = request(
            family = FirstNightInformationFamily.INVESTIGATOR,
            seat = 2,
            reliability = ReliabilityState.DRUNK,
            legacyId = "legacy-shortlist",
            migratedId = "legal-manual-choice",
        )
        val coordinator = FirstNightInformationMigration()
        assertTrue(coordinator.shadow(request) is FirstNightShadowResult.Mismatch)

        val published = coordinator.publishAuthoritativePairDomain(request)
        assertTrue(published.isReady(request.decisionId))

        val displayed = published.display(request.decisionId, request.selectedCandidateId)
        assertTrue(displayed.isDisplayed(request.decisionId))
        assertFalse(displayed.isReady(request.decisionId))
        assertEquals(
            request.migratedCandidates.single().observation,
            displayed.displayedObservation(request.decisionId),
        )
    }

    @Test fun `authoritative pair domain cannot bypass parity for non-pair families`() {
        val request = request(
            family = FirstNightInformationFamily.CHEF,
            seat = 2,
            reliability = ReliabilityState.POISONED,
            legacyId = "legacy",
            migratedId = "migrated",
        )

        assertThrows(IllegalArgumentException::class.java) {
            FirstNightInformationMigration().publishAuthoritativePairDomain(request)
        }
    }

    @Test fun `poison invalidation preserves displayed observation but removes every unshown family`() {
        val washerwoman = request(FirstNightInformationFamily.WASHERWOMAN, 1, ReliabilityState.DRUNK, "ww")
        val chef = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.POISONED, "chef")
        val before = FirstNightInformationMigration()
            .publishIfShadowMatches(washerwoman)
            .publishIfShadowMatches(chef)
            .display(washerwoman.decisionId, "ww")
        val after = before.invalidateUnshown()
        assertEquals(washerwoman.migratedCandidates.single().observation, after.displayedObservation(washerwoman.decisionId))
        assertFalse(after.isReady(chef.decisionId))
        assertEquals(1, after.generation())
    }

    @Test fun `displaying the same committed decision is idempotent`() {
        val request = request(FirstNightInformationFamily.FORTUNE_TELLER, 4, ReliabilityState.RELIABLE, "yes")
        val first = FirstNightInformationMigration()
            .publishIfShadowMatches(request)
            .display(request.decisionId, "yes")
        val repeated = first.display(request.decisionId, "yes")
        assertEquals(first, repeated)
        assertEquals(request.migratedCandidates.single().observation, repeated.displayedObservation(request.decisionId))
    }

    @Test fun `shadow parity publishes a complete pool but commits only the selected candidate`() {
        val first = candidate(FirstNightInformationFamily.CHEF, 3, ReliabilityState.POISONED, "zero")
        val selected = candidate(FirstNightInformationFamily.CHEF, 3, ReliabilityState.POISONED, "one")
        val request = FirstNightInformationRequest(
            decisionId = "first-night-chef-3",
            family = FirstNightInformationFamily.CHEF,
            sourceSeat = 3,
            reliability = ReliabilityState.POISONED,
            selectedCandidateId = selected.id,
            legacyCandidates = listOf(first, selected),
            migratedCandidates = listOf(first, selected),
        )
        val migration = FirstNightInformationMigration()
            .publishIfShadowMatches(request)
            .display(request.decisionId, request.selectedCandidateId)
        assertEquals(selected.observation, migration.displayedObservation(request.decisionId))
    }

    @Test fun `same candidate IDs with a different tier or rank are a shadow mismatch`() {
        val legacy = candidate(FirstNightInformationFamily.CHEF, 5, ReliabilityState.RELIABLE, "same")
            .copy(qualityTier = QualityTier.RECOMMENDED, rankFixedPoint = 100)
        val migrated = legacy.copy(rankFixedPoint = 99)
        val request = FirstNightInformationRequest(
            decisionId = "first-night-chef-rank",
            family = FirstNightInformationFamily.CHEF,
            sourceSeat = 5,
            reliability = ReliabilityState.RELIABLE,
            selectedCandidateId = "same",
            legacyCandidates = listOf(legacy),
            migratedCandidates = listOf(migrated),
        )

        assertTrue(FirstNightInformationMigration().shadow(request) is FirstNightShadowResult.Mismatch)
    }

    @Test fun `reveal resolution preserves non-pair mismatch fallback without state update`() {
        val request = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.POISONED, "old", "new")
        val migration = FirstNightInformationMigration()
        assertEquals(FirstNightPublicationResolution.LegacyFallback,
            migration.resolvePublication(request, migration.shadow(request)))
        assertFalse(migration.isDisplayed(request.decisionId))
    }

    @Test fun `reveal resolution commits selected authoritative pair despite parity mismatch`() {
        val request = request(FirstNightInformationFamily.INVESTIGATOR, 2, ReliabilityState.DRUNK, "old", "new")
        val migration = FirstNightInformationMigration()
        val result = migration.resolvePublication(request, migration.shadow(request)) as FirstNightPublicationResolution.Published
        assertEquals(request.migratedCandidates.single().observation, result.migration.displayedObservation(request.decisionId))
        assertEquals(FirstNightPublicationResolution.AlreadyDisplayed,
            result.migration.resolvePublication(request, result.migration.shadow(request)))
    }

    @Test fun `reveal resolution never replaces a displayed fact even when request changes`() {
        val original = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.RELIABLE, "first")
        val initial = FirstNightInformationMigration()
        val published = (initial.resolvePublication(original, initial.shadow(original)) as FirstNightPublicationResolution.Published).migration
        val changed = request(FirstNightInformationFamily.CHEF, 2, ReliabilityState.RELIABLE, "other", "new")
        assertEquals(FirstNightPublicationResolution.AlreadyDisplayed,
            published.resolvePublication(changed, published.shadow(changed)))
        assertEquals(original.migratedCandidates.single().observation, published.displayedObservation(original.decisionId))
    }

    private fun request(
        family: FirstNightInformationFamily,
        seat: Int,
        reliability: ReliabilityState,
        legacyId: String,
        migratedId: String = legacyId,
    ): FirstNightInformationRequest {
        return FirstNightInformationRequest(
            decisionId = "first-night-${family.name.lowercase()}-$seat",
            family = family,
            sourceSeat = seat,
            reliability = reliability,
            selectedCandidateId = migratedId,
            legacyCandidates = listOf(candidate(family, seat, reliability, legacyId)),
            migratedCandidates = listOf(candidate(family, seat, reliability, migratedId)),
        )
    }

    private fun candidate(
        family: FirstNightInformationFamily,
        seat: Int,
        reliability: ReliabilityState,
        id: String,
    ) = FirstNightInformationCandidate(id, AbilityObservation(
            sourceSeat = seat,
            perceivedRole = family.role,
            reliability = reliability,
            semanticTruth = SemanticTruth.TRUE,
        ))
}
