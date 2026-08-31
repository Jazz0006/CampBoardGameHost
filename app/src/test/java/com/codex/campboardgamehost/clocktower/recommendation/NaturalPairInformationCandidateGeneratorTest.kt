package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalPairInformationCandidateGeneratorTest {
    @Test
    fun `healthy librarian always includes the only recluse as natural truth`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Librarian"))

        assertEquals(3, candidates.size)
        assertTrue(candidates.all { it.outcome.shownRole == RoleId("Recluse") })
        assertTrue(candidates.all { 3 in it.outcome.candidateSeats })
        assertTrue(candidates.all { it.candidateFamilyId == "natural-truth" })
        assertTrue(candidates.all { it.abilityState == AbilityState.FUNCTIONING })
        assertTrue(candidates.all { it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE })
        assertTrue(candidates.all { it.registrations.isEmpty() })
    }

    @Test
    fun `healthy investigator with the only spy never invents another minion`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Spy", CharacterType.MINION),
            player(4, "Saint", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Investigator"))

        assertEquals(3, candidates.size)
        assertEquals(setOf(RoleId("Spy")), candidates.mapNotNull { it.outcome.shownRole }.toSet())
        assertTrue(candidates.all { 3 in it.outcome.candidateSeats })
        assertTrue(candidates.all { it.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE })
        assertTrue(candidates.all { it.registrations.isEmpty() })
    }

    @Test
    fun `healthy investigator may use recluse special registration as minion truth`() {
        val game = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Investigator"))
        val recluseCandidates = candidates.filter { candidate ->
            candidate.registrations.any { it.subjectSeat == 3 }
        }

        assertTrue(recluseCandidates.isNotEmpty())
        assertTrue(recluseCandidates.all { it.outcome.shownRole == RoleId("Poisoner") })
        assertTrue(recluseCandidates.all { 3 in it.outcome.candidateSeats })
        assertTrue(recluseCandidates.all { it.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE })
        assertTrue(recluseCandidates.all { candidate ->
            candidate.registrations.single().let { registration ->
                registration.subjectSeat == 3 &&
                    registration.registeredRole == RoleId("Poisoner") &&
                    registration.registeredType == CharacterType.MINION &&
                    registration.registeredAlignment == Alignment.EVIL &&
                    registration.registrationQuestion == RegistrationQuestion.SPECIFIC_MINION &&
                    registration.reason == RegistrationReason.RECLUSE_ABILITY
            }
        })
    }

    @Test
    fun `healthy investigator can truthfully use out of play minion through recluse registration`() {
        val baseGame = game(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val investigator = RoleId("Investigator")
        val actualMinion = RoleId("Poisoner")
        val observations = (0 until 512).mapNotNull { index ->
            PairInformationAbilityRecommender.recommend(
                game = baseGame.copy(seed = baseGame.seed + index.toLong()),
                roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
                sourceSeat = 1,
                abilityRole = investigator,
                reliability = ReliabilityState.RELIABLE,
                style = RecommendationStyle.BALANCED,
            )
        }
        val registeredTruths = observations.filter { observation ->
            observation.shownRole != actualMinion && 3 in observation.candidateSeats
        }

        assertTrue(registeredTruths.isNotEmpty())
        assertTrue(registeredTruths.all { it.semanticTruth == SemanticTruth.TRUE })
        assertTrue(registeredTruths.all { observation ->
            observation.registrations.any { registration ->
                registration.playerSeat == 3 &&
                    registration.affectedAbility == investigator &&
                    registration.registeredRole == observation.shownRole &&
                    registration.registeredType == CharacterType.MINION &&
                    registration.registeredAlignment == Alignment.EVIL &&
                    registration.reason == RegistrationReason.RECLUSE_ABILITY
            }
        })
    }

    @Test
    fun `washerwoman healthy semantic space names an actual townsfolk with one decoy`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Washerwoman"),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidates = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.all { candidate ->
            candidate.outcome.shownRole in setOf(RoleId("Chef"), RoleId("Empath")) &&
                candidate.outcome.candidateSeats.size == 2 &&
                1 !in candidate.outcome.candidateSeats &&
                candidate.truthRelation == TruthRelation.TRUE_TO_ACTUAL_STATE
        })
    }

    @Test
    fun `drunk shown washerwoman receives legal washerwoman pair information`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Washerwoman"),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val roles = TroubleBrewingFixtures.fullRoleDefinitions()

        val observation = PairInformationAbilityRecommender.recommend(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
            reliability = ReliabilityState.DRUNK,
            style = RecommendationStyle.BALANCED,
        )

        assertNotNull(observation)
        requireNotNull(observation)
        assertEquals(RoleId("Washerwoman"), observation.perceivedRole)
        assertEquals(ReliabilityState.DRUNK, observation.reliability)
        assertEquals(2, observation.candidateSeats.size)
        assertTrue(1 !in observation.candidateSeats)
        assertEquals(
            CharacterType.TOWNSFOLK,
            roles.single { it.id == observation.shownRole }.type,
        )
        assertEquals(RoleId("Drunk"), game.playerAt(1)?.actualRole)
    }

    @Test
    fun `librarian with no outsiders receives one typed zero result`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        val candidate = NaturalPairInformationCandidateGenerator
            .generate(game, 1, RoleId("Librarian"))
            .single()

        assertTrue(candidate.outcome.candidateSeats.isEmpty())
        val information = candidate.effects.single() as EffectDraft.PlayerInformation
        assertEquals(InformationValue.NoCharacters(CharacterType.OUTSIDER), information.value)
    }

    @Test
    fun `poisoned ability does not enter the functioning natural truth generator`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK, poisoned = true),
            player(2, "Recluse", CharacterType.OUTSIDER),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        assertTrue(
            NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Librarian")).isEmpty(),
        )
    }

    @Test
    fun `drunk simulating librarian does not enter the functioning natural truth generator`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Librarian"),
            player(2, "Recluse", CharacterType.OUTSIDER),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )

        assertTrue(
            NaturalPairInformationCandidateGenerator.generate(game, 1, RoleId("Librarian")).isEmpty(),
        )
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20260806L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        poisoned: Boolean = false,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
        poisoned = poisoned,
    )
}
