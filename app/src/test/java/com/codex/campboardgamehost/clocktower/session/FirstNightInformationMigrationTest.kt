package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
