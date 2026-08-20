package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.recommendation.CandidatePoolBuilder
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryCooldownTest {
    @Test
    fun `history retains newest ten games in canonical order`() {
        val history = (0..10).fold(CrossGameHistory()) { current, index ->
            current.append(signature(drunkRole = "Role-$index"))
        }

        assertEquals(10, history.recentSignatures.size)
        assertEquals(RoleId("Role-10"), history.recentSignatures.first().drunkShownRole)
        assertEquals(RoleId("Role-1"), history.recentSignatures.last().drunkShownRole)
        assertEquals(history.digest(), CrossGameHistory(history.recentSignatures).digest())
    }

    @Test
    fun `same drunk role last game receives stronger cooldown than an older match`() {
        val candidate = signature(drunkRole = "Investigator")
        val previousGame = HistoryCooldown.multiplierFixedPoint(
            candidate,
            CrossGameHistory(listOf(candidate)),
        )
        val fourGamesAgo = HistoryCooldown.multiplierFixedPoint(
            candidate,
            CrossGameHistory(
                listOf(
                    signature("Chef"),
                    signature("Empath"),
                    signature("Monk"),
                    candidate,
                ),
            ),
        )

        assertTrue(previousGame < fourGamesAgo)
        assertEquals(WeightedStableSelector.FIXED_POINT_SCALE, fourGamesAgo)
        assertEquals(
            WeightedStableSelector.FIXED_POINT_SCALE,
            HistoryCooldown.multiplierFixedPoint(candidate, CrossGameHistory(listOf(signature("Chef")))),
        )
    }

    @Test
    fun `persistent fingerprint penalties decay with age`() {
        val candidate = signature("Investigator").copy(candidateAlignmentPattern = "EVIL,GOOD")
        val recent = HistoryCooldown.multiplierFixedPoint(candidate, CrossGameHistory(listOf(candidate)))
        val older = HistoryCooldown.multiplierFixedPoint(
            candidate,
            CrossGameHistory(List(5) { signature("Role-$it") } + candidate),
        )

        assertTrue(recent < older)
        assertTrue(older < WeightedStableSelector.FIXED_POINT_SCALE)
    }

    @Test
    fun `cooldown changes only weights inside the already selected quality pool`() {
        val evaluations = listOf(
            evaluation("best", score = 20),
            evaluation("inside", score = 17),
            evaluation("outside", score = 10),
        )
        val pool = CandidatePoolBuilder.build(evaluations, scoreTolerance = 4)
        val history = CrossGameHistory(listOf(signature(drunkRole = "Investigator")))
        val cooled = HistoryCooldown.apply(pool, history) { item ->
            signature(drunkRole = if (item.candidate.candidateId == "best") "Investigator" else "Chef")
        }

        assertEquals(listOf("best", "inside"), cooled.map { it.candidate.candidateId })
        assertTrue(cooled.first { it.candidate.candidateId == "best" }.withinFamilyWeightFixedPoint < 100)
        assertEquals(100, cooled.first { it.candidate.candidateId == "inside" }.withinFamilyWeightFixedPoint)
        assertTrue(cooled.all { it.qualityTier == QualityTier.RECOMMENDED && it.totalScore in setOf(20, 17) })
    }

    @Test
    fun `canonical signature sorts demon bluffs`() {
        val first = signature("Chef").copy(demonBluffs = setOf(RoleId("Monk"), RoleId("Slayer")))
        val second = signature("Chef").copy(demonBluffs = setOf(RoleId("Slayer"), RoleId("Monk")))

        assertEquals(first.canonical(), second.canonical())
    }

    private fun signature(drunkRole: String) = HistoricalClueSignature(
        decisionType = "setup-plan",
        drunkShownRole = RoleId(drunkRole),
    )

    private fun evaluation(id: String, score: Int): DecisionEvaluation<String> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = id,
            candidateFamilyId = "setup-plan",
            outcome = id,
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            metadata = CandidateMetadata("setup-v1", "setup-plan"),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = score,
        withinFamilyWeightFixedPoint = 100,
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = emptyList(),
        explanationCodes = emptyList(),
    )
}
