package com.codex.campboardgamehost.clocktower.recommendation.setup

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
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
    fun `committed Investigator identity produces information only Drunk candidates`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val candidates = SetupCandidateGenerator.generateDrunkCandidates(
            game,
            TroubleBrewingFixtures.roleDefinitions(),
        )

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.all { it.candidateFamilyId == "drunk-investigator-info" })
        assertTrue(candidates.all { candidate ->
            val outcome = candidate.outcome as SetupClueOutcome.DrunkShownRole
            outcome.shownRole == RoleId("Investigator") && outcome.investigatorInformation != null
        })
        assertTrue(candidates.all { it.abilityState == AbilityState.MALFUNCTIONING_DRUNK })
        assertTrue(candidates.all { it.truthRelation == TruthRelation.NOT_APPLICABLE })
        assertTrue(candidates.all { it.effects.single() is EffectDraft.Reminder })
        assertEquals(candidates.size, candidates.map { it.candidateId }.distinct().size)
    }

    @Test
    fun `non Investigator committed identity does not invent Drunk setup information`() {
        val template = TroubleBrewingFixtures.eightPlayerExample()
        val game = template.copy(
            players = template.players.map { player ->
                if (player.actualRole == RoleId("Drunk")) {
                    player.copy(shownRole = RoleId("Chef"))
                } else {
                    player
                }
            },
        )

        assertTrue(
            SetupCandidateGenerator.generateDrunkCandidates(
                game,
                TroubleBrewingFixtures.roleDefinitions(),
            ).isEmpty(),
        )
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
        assertTrue(bluffs.all {
            val effect = it.effects.single() as EffectDraft.PlayerInformation
            (effect.value as InformationValue.RoleSet).roleIds.size == 3
        })
    }

    @Test
    fun `committed Investigator identity constrains every later setup recommendation`() {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        val committedInformation = StorytellerDecision.DrunkInvestigatorInfo(
            shownMinion = RoleId("Poisoner"),
            candidateSeats = listOf(2, 4),
        )

        val plans = SetupRecommendationService.recommendConstrained(
            game = game,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            lockedDecisions = listOf(committedInformation),
        ).plans

        assertTrue(plans.isNotEmpty())
        assertTrue(plans.all { plan ->
            plan.decisions.none { it is StorytellerDecision.DrunkShownRole } &&
                plan.decisions.filterIsInstance<StorytellerDecision.DrunkInvestigatorInfo>().single() == committedInformation
        })
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
