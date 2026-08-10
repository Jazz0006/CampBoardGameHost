package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.config.RecommendationProfiles
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupMigrationTest {
    @Test
    fun `drunk setup candidates have stable families and typed effects`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val candidates = SetupCandidateGenerator.generateDrunkCandidates(
            game,
            TroubleBrewingFixtures.roleDefinitions(),
        )

        assertEquals(114, candidates.size)
        assertEquals(setOf("drunk-shown-role", "drunk-investigator-info"), candidates.map { it.candidateFamilyId }.toSet())
        assertTrue(candidates.all { it.abilityState == AbilityState.MALFUNCTIONING_DRUNK })
        assertTrue(candidates.all { it.truthRelation == TruthRelation.NOT_APPLICABLE })
        assertTrue(candidates.all { it.effects.single() is EffectDraft.Reminder })
        assertEquals(candidates.size, candidates.map { it.candidateId }.distinct().size)
    }

    @Test
    fun `librarian and investigator natural clues enter the setup candidate pipeline`() {
        val librarianGame = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val investigatorGame = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Spy", CharacterType.MINION),
            player(4, "Saint", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )

        val librarian = SetupCandidateGenerator.generatePairInformationCandidates(librarianGame)
        val investigator = SetupCandidateGenerator.generatePairInformationCandidates(investigatorGame)

        assertTrue(librarian.all { (it.outcome as SetupClueOutcome.PairInformation).information.shownRole == RoleId("Recluse") })
        assertTrue(investigator.all { (it.outcome as SetupClueOutcome.PairInformation).information.shownRole == RoleId("Spy") })
        assertTrue((librarian + investigator).all { it.candidateFamilyId == "natural-truth" })
    }

    @Test
    fun `red herring and demon bluffs have dedicated families and effects`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val redHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
        val bluffs = SetupCandidateGenerator.generateDemonBluffCandidates(game, TroubleBrewingFixtures.roleDefinitions())

        assertEquals(6, redHerrings.size)
        assertTrue(redHerrings.all { it.candidateFamilyId == "red-herring" })
        assertEquals(4, bluffs.size)
        assertTrue(bluffs.all { it.candidateFamilyId == "demon-bluffs" })
        assertTrue(bluffs.all {
            val effect = it.effects.single() as EffectDraft.PlayerInformation
            (effect.value as InformationValue.RoleSet).roleIds.size == 3
        })
    }

    @Test
    fun `setup evaluator keeps all recommended drunk roles in the top quality tier`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val roles = TroubleBrewingFixtures.fullRoleDefinitions()
        val evaluated = SetupCandidateGenerator.generateDrunkCandidates(game, roles)
            .filter { (it.outcome as SetupClueOutcome.DrunkShownRole).investigatorInformation == null }
            .map { SetupEvaluator.evaluateClue(game, it, RecommendationProfiles.balanced) }

        assertTrue(evaluated.isNotEmpty())
        assertTrue(evaluated.all { it.qualityTier == QualityTier.RECOMMENDED })
    }

    @Test
    fun `one thousand setup selections are reproducible but not investigator monopolized`() {
        val template = TroubleBrewingFixtures.eightPlayerExample()
        val roles = TroubleBrewingFixtures.roleDefinitions()
        val shownRoleCounts = (0 until 1_000).map { seed ->
            SetupRecommendationService.recommend(template.copy(seed = seed.toLong()), roles)
                .single { it.style == RecommendationStyle.BALANCED }
                .decisions.filterIsInstance<StorytellerDecision.DrunkShownRole>().single().role
        }.groupingBy { it }.eachCount()

        assertTrue("shownRoleCounts=$shownRoleCounts", shownRoleCounts.size >= 2)
        assertTrue((shownRoleCounts[RoleId("Investigator")] ?: 0) < 900)
        val first = SetupRecommendationService.recommend(template.copy(seed = 777), roles)
        val replay = SetupRecommendationService.recommend(template.copy(seed = 777), roles)
        assertEquals(first.map { it.decisions }, replay.map { it.decisions })
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 42,
    )

    private fun player(seat: Int, role: String, type: CharacterType) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = if (type == CharacterType.MINION || type == CharacterType.DEMON) Alignment.EVIL else Alignment.GOOD,
        actualType = type,
        shownRole = RoleId(role),
    )
}
