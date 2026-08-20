package com.codex.campboardgamehost.clocktower.recommendation.dynamic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.history.CrossGameHistory
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicCandidateGeneratorTest {
    private val poisonedContext = DynamicGenerationContext(
        abilityRole = RoleId("Empath"),
        recipientSeat = 2,
        reliability = InformationReliability.POISONED,
        style = RecommendationStyle.BALANCED,
    )

    @Test
    fun `numeric generation retains every legal value and separates truth families`() {
        val evaluations = DynamicCandidateGenerator.generateNumeric(
            UnreliableNumberContext(trueValue = 1, minimumValue = 0, maximumValue = 2),
            poisonedContext,
        )

        assertEquals(setOf(0, 1, 2), evaluations.map { it.candidate.outcome.value }.toSet())
        assertEquals(3, evaluations.map { it.candidate.candidateId }.distinct().size)
        assertEquals("malfunction-truth", evaluations.single { it.candidate.outcome.value == 1 }.candidate.candidateFamilyId)
        assertTrue(evaluations.filter { it.candidate.outcome.value != 1 }.all {
            it.candidate.candidateFamilyId == "malfunction-falsehood-numeric" &&
                it.candidate.truthRelation == TruthRelation.FALSE_TO_ACTUAL_STATE
        })
        assertTrue(evaluations.all { it.candidate.abilityState == AbilityState.MALFUNCTIONING_POISONED })
    }

    @Test
    fun `categorical and pair information produce typed stable candidates`() {
        val categorical = DynamicCandidateGenerator.generateCategorical(
            listOf(
                UnreliableCategoricalCandidate("yes", true),
                UnreliableCategoricalCandidate("no", false, 3),
            ),
            poisonedContext,
        )
        val pair = DynamicCandidateGenerator.generatePairInformation(
            listOf(
                PairInformationCandidate(
                    id = "seats-1-4",
                    registration = PairInformationRegistration.NONE,
                    isTruthful = true,
                    targetExposure = 1,
                    decoyExposure = 2,
                    discussionValue = 3,
                    misinformationPressure = 0,
                ),
                PairInformationCandidate(
                    id = "seats-2-5",
                    registration = PairInformationRegistration.NONE,
                    isTruthful = false,
                    targetExposure = 1,
                    decoyExposure = 1,
                    discussionValue = 3,
                    misinformationPressure = 2,
                ),
            ),
            poisonedContext,
        )

        assertEquals(setOf("yes", "no"), categorical.map { (it.candidate.outcome as DynamicInformationOutcome.Category).id }.toSet())
        assertEquals(setOf("seats-1-4", "seats-2-5"), pair.map { (it.candidate.outcome as DynamicInformationOutcome.Category).id }.toSet())
        assertTrue((categorical + pair).all { it.candidate.effects.size == 1 })
    }

    @Test
    fun `selection is independent of option order`() {
        val options = listOf(
            Option("truth", true, 0, RecommendationStyle.GENTLE),
            Option("soft-lie", false, 2, RecommendationStyle.BALANCED),
            Option("hard-lie", false, 4, RecommendationStyle.AGGRESSIVE),
        )

        assertEquals(select(options, "game-a:event-1"), select(options.reversed(), "game-a:event-1"))
    }

    @Test
    fun `same decision replays while different games can vary`() {
        val options = listOf(
            Option("truth", true, 0, RecommendationStyle.BALANCED),
            Option("lie", false, 2, RecommendationStyle.BALANCED),
        )
        val replay = select(options, "game-a:night-2:empath")
        assertEquals(replay, select(options, "game-a:night-2:empath"))

        val acrossGames = (0 until 100).map { game -> select(options, "game-$game:night-2:empath") }.toSet()
        assertTrue(acrossGames.size >= 2)
        assertNotEquals(emptySet<Option>(), acrossGames)
    }

    @Test
    fun `dynamic selection replays with the same cross game history`() {
        val options = listOf(
            Option("truth", true, 0, RecommendationStyle.BALANCED),
            Option("lie", false, 2, RecommendationStyle.BALANCED),
        )
        val history = CrossGameHistory(
            listOf(HistoricalClueSignature("dynamic", shownCharacter = RoleId("lie"))),
        )
        fun selectWithHistory() = DynamicCandidateGenerator.select(
            options = options,
            reliability = InformationReliability.POISONED,
            style = RecommendationStyle.BALANCED,
            evilAdvantage = 0,
            stableKey = "game-a:night-2:empath",
            recentMisinformationStreak = 0,
            stableIdOf = Option::id,
            isTruthful = Option::truthful,
            misinformationPressure = Option::pressure,
            styleOf = Option::style,
            history = history,
            historicalSignatureOf = { HistoricalClueSignature("dynamic", shownCharacter = RoleId(it.id)) },
        )

        assertEquals(selectWithHistory(), selectWithHistory())
    }

    private data class Option(
        val id: String,
        val truthful: Boolean,
        val pressure: Int,
        val style: RecommendationStyle,
    )

    private fun select(options: List<Option>, stableKey: String): Option? = DynamicCandidateGenerator.select(
        options = options,
        reliability = InformationReliability.POISONED,
        style = RecommendationStyle.BALANCED,
        evilAdvantage = 0,
        stableKey = stableKey,
        recentMisinformationStreak = 0,
        stableIdOf = Option::id,
        isTruthful = Option::truthful,
        misinformationPressure = Option::pressure,
        styleOf = Option::style,
    )
}
