package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupDiversitySelectorTest {
    @Test
    fun `history snapshots canonical actual role compositions`() {
        val script = ScriptId("trouble_brewing")
        val mutableRoles = mutableListOf(RoleId("B"), RoleId("A"), RoleId("Imp"))
        val mutableRecords = mutableListOf(
            SetupDiversityRecord(script = script, actualRoles = mutableRoles),
        )

        val history = SetupDiversityHistory(mutableRecords)
        mutableRoles.clear()
        mutableRecords.clear()

        assertEquals(
            listOf(RoleId("A"), RoleId("B"), RoleId("Imp")),
            history.recentSetups.single().actualRoles,
        )
        assertEquals(3, history.recentSetups.single().playerCount)
    }

    @Test
    fun `scoring ignores roles invariant across the candidate pool`() {
        val script = ScriptId("trouble_brewing")
        val repeated = candidate(script, "repeat", "Imp", "A", "B", "C")
        val novel = candidate(script, "novel", "Imp", "D", "E", "F")
        val pool = listOf(repeated, novel)
        val history = SetupDiversityHistory(
            listOf(SetupDiversityRecord(script, repeated.actualRoles)),
        )
        val policy = policy(historyWeights = listOf(100L))

        val repeatedScore = SetupDiversityScorer.score(repeated, pool, history, policy)
        val novelScore = SetupDiversityScorer.score(novel, pool, history, policy)

        assertEquals(SetupDiversityScorer.FIXED_POINT_SCALE, repeatedScore.weightedOverlapFixedPoint)
        assertEquals(0L, novelScore.weightedOverlapFixedPoint)
        assertTrue(novelScore.noveltyWeightFixedPoint > repeatedScore.noveltyWeightFixedPoint)
    }

    @Test
    fun `history from another script or player count does not affect scoring`() {
        val script = ScriptId("trouble_brewing")
        val repeated = candidate(script, "repeat", "Imp", "A", "B", "C")
        val novel = candidate(script, "novel", "Imp", "D", "E", "F")
        val pool = listOf(repeated, novel)
        val history = SetupDiversityHistory(
            listOf(
                SetupDiversityRecord(
                    ScriptId("no_greater_joy"),
                    novel.actualRoles,
                ),
                SetupDiversityRecord(
                    script,
                    listOf(RoleId("Imp"), RoleId("X"), RoleId("Y"), RoleId("Z"), RoleId("Q")),
                ),
                SetupDiversityRecord(script, repeated.actualRoles),
            ),
        )

        val score = SetupDiversityScorer.score(
            candidate = novel,
            candidatePool = pool,
            history = history,
            policy = policy(historyWeights = listOf(100L)),
        )

        assertEquals(0L, score.weightedOverlapFixedPoint)
    }

    @Test
    fun `exact repeat is rejected when an alternative composition exists`() {
        val script = ScriptId("trouble_brewing")
        val repeated = candidate(script, "repeat", "Imp", "A", "B", "C")
        val alternative = candidate(script, "alternative", "Imp", "D", "E", "F")
        val history = SetupDiversityHistory(
            listOf(SetupDiversityRecord(script, repeated.actualRoles)),
        )
        val selector = SetupDiversitySelector(
            policy = policy(exactRepeatPolicy = SetupExactRepeatPolicy.REJECT_WHEN_ALTERNATIVE),
        )

        val selected = selector.select(
            candidates = listOf(repeated, alternative),
            history = history,
            selectionSeed = 7L,
        )

        assertEquals("alternative", selected.provenance.candidateId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `strict exact repeat rejection fails when no composition remains`() {
        val script = ScriptId("trouble_brewing")
        val repeated = candidate(script, "repeat", "Imp", "A", "B", "C")
        val history = SetupDiversityHistory(
            listOf(SetupDiversityRecord(script, repeated.actualRoles)),
        )

        SetupDiversitySelector(
            policy = policy(exactRepeatPolicy = SetupExactRepeatPolicy.REJECT),
        ).select(
            candidates = listOf(repeated),
            history = history,
            selectionSeed = 7L,
        )
    }

    @Test
    fun `last game overlap threshold chooses the first non empty eligibility level`() {
        val script = ScriptId("trouble_brewing")
        val previous = candidate(script, "previous", "Imp", "A", "B", "C")
        val highOverlap = candidate(script, "high", "Imp", "A", "B", "D")
        val lowOverlap = candidate(script, "low", "Imp", "D", "E", "F")
        val history = SetupDiversityHistory(
            listOf(SetupDiversityRecord(script, previous.actualRoles)),
        )
        val selector = SetupDiversitySelector(
            policy = policy(
                exactRepeatPolicy = SetupExactRepeatPolicy.ALLOW,
                lastGameMaxOverlapFixedPoint = 500_000L,
            ),
        )

        val selected = selector.select(
            candidates = listOf(previous, highOverlap, lowOverlap),
            history = history,
            selectionSeed = 123L,
        )

        assertEquals("low", selected.provenance.candidateId)
    }

    @Test
    fun `selection is deterministic and independent of candidate input order`() {
        val script = ScriptId("trouble_brewing")
        val candidates = listOf(
            candidate(script, "c", "Imp", "G", "H", "I"),
            candidate(script, "a", "Imp", "A", "B", "C"),
            candidate(script, "b", "Imp", "D", "E", "F"),
        )
        val selector = SetupDiversitySelector(policy())

        val first = selector.select(candidates, SetupDiversityHistory.EMPTY, 42L)
        val replay = selector.select(candidates.reversed(), SetupDiversityHistory.EMPTY, 42L)

        assertEquals(first, replay)
    }

    @Test
    fun `different seeds explore more than one equally novel candidate`() {
        val script = ScriptId("trouble_brewing")
        val candidates = listOf(
            candidate(script, "a", "Imp", "A", "B", "C"),
            candidate(script, "b", "Imp", "D", "E", "F"),
            candidate(script, "c", "Imp", "G", "H", "I"),
        )
        val selector = SetupDiversitySelector(policy())

        val selectedIds = (0L until 64L).map { seed ->
            selector.select(candidates, SetupDiversityHistory.EMPTY, seed).provenance.candidateId
        }.toSet()

        assertTrue("selectedIds=$selectedIds", selectedIds.size > 1)
    }

    @Test
    fun `single generated candidate is returned without requiring template identity`() {
        val script = ScriptId("no_greater_joy")
        val generated = SetupCandidate(
            script = script,
            actualRoles = listOf(RoleId("A"), RoleId("B"), RoleId("C"), RoleId("D"), RoleId("Imp")),
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "ruleset-generator",
                candidateId = null,
            ),
        )

        val selected = SetupDiversitySelector(policy()).select(
            candidates = listOf(generated),
            history = SetupDiversityHistory.EMPTY,
            selectionSeed = 99L,
        )

        assertEquals(generated, selected)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `candidate pool cannot mix scripts`() {
        val tb = candidate(ScriptId("trouble_brewing"), "tb", "Imp", "A", "B", "C")
        val ngj = candidate(ScriptId("no_greater_joy"), "ngj", "Imp", "A", "B", "C")

        SetupDiversitySelector(policy()).select(
            candidates = listOf(tb, ngj),
            history = SetupDiversityHistory.EMPTY,
            selectionSeed = 1L,
        )
    }

    private fun policy(
        historyWeights: List<Long> = listOf(100L, 65L, 40L, 20L, 10L),
        exactRepeatPolicy: SetupExactRepeatPolicy = SetupExactRepeatPolicy.REJECT_WHEN_ALTERNATIVE,
        lastGameMaxOverlapFixedPoint: Long = SetupDiversityScorer.FIXED_POINT_SCALE,
    ): SetupDiversityPolicy = SetupDiversityPolicy(
        historyWeights = historyWeights,
        exactRepeatPolicy = exactRepeatPolicy,
        lastGameMaxOverlapFixedPoint = lastGameMaxOverlapFixedPoint,
        overlapFallbackStepFixedPoint = 50_000L,
        minimumNoveltyWeightFixedPoint = 200_000L,
    )

    private fun candidate(
        script: ScriptId,
        id: String,
        vararg roles: String,
    ): SetupCandidate = SetupCandidate(
        script = script,
        actualRoles = roles.map(::RoleId),
        provenance = SetupProvenance(
            sourceKind = SetupSourceKind.TEMPLATE,
            providerId = "test-templates",
            candidateId = id,
        ),
    )
}
