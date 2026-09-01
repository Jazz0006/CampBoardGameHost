package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.rules.PairInformationDisplaySemantics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPairManualAuthorityTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val investigator = RoleId("Investigator")

    @Test
    fun `manual projection is exactly the legal domain and ignores an illegal presentation template`() {
        val game = game()
        val legalOutcomes = PairInformationDisplaySemantics.legalOutcomes(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = investigator,
        )
        val illegalZeroTemplate = ClocktowerDisplayOption(
            label = "No Minions",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Investigator information",
            displayPrimary = "No Minions",
            displaySecondary = null,
            displayFooter = null,
            proposition = InformationProposition.AllOf(
                listOf(InformationProposition.RoleInPlay(RoleId("Poisoner"), false)),
            ),
        )
        val expected = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = investigator,
            reliability = ReliabilityState.DRUNK,
        )

        val projected = ClocktowerPairManualAuthority.projectLegalOptions(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = investigator,
            reliability = ReliabilityState.DRUNK,
            presentationOptions = legalOutcomes.map(::template) + illegalZeroTemplate,
        )

        assertEquals(expected.size, projected.size)
        assertEquals(
            expected.map { it.outcome.shownRole to it.outcome.candidateSeats }.toSet(),
            projected.mapNotNull(::keyOf).toSet(),
        )
        assertFalse(projected.any { it.displayPrimary == "No Minions" })
    }

    @Test
    fun `selected manual clue commits the exact legal registration fact`() {
        val game = game()
        val legalCandidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = investigator,
            reliability = ReliabilityState.RELIABLE,
        )
        val registeredCandidate = legalCandidates.first { candidate ->
            candidate.registrations.any { it.reason == RegistrationReason.RECLUSE_ABILITY }
        }
        val selectedOption = template(registeredCandidate.outcome)

        val observation = ClocktowerPairManualAuthority.selectedObservation(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = investigator,
            reliability = ReliabilityState.RELIABLE,
            selectedOption = selectedOption,
        )

        assertEquals(registeredCandidate.outcome.shownRole, observation.shownRole)
        assertEquals(registeredCandidate.outcome.candidateSeats, observation.candidateSeats)
        assertEquals(registeredCandidate.semanticTruth, observation.semanticTruth)
        assertEquals(registeredCandidate.registrations.size, observation.registrations.size)
        registeredCandidate.registrations.zip(observation.registrations).forEach { (fact, committed) ->
            assertEquals(fact.subjectSeat, committed.playerSeat)
            assertEquals(investigator, committed.affectedAbility)
            assertEquals(fact.registeredAlignment, committed.registeredAlignment)
            assertEquals(fact.registeredType, committed.registeredType)
            assertEquals(fact.registeredRole, committed.registeredRole)
            assertEquals(fact.reason, committed.reason)
        }
        assertTrue(observation.registrations.any { it.reason == RegistrationReason.RECLUSE_ABILITY })
    }

    private fun game() = GameState(
        script = ScriptId("trouble_brewing"),
        players = listOf(
            player(1, "Investigator", CharacterType.TOWNSFOLK),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Recluse", CharacterType.OUTSIDER),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        ),
        seed = 20260901L,
    )

    private fun player(seat: Int, role: String, type: CharacterType) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )

    private fun template(outcome: PairInformationOutcome): ClocktowerDisplayOption =
        if (outcome.shownRole == null) {
            ClocktowerDisplayOption(
                label = "No Outsiders",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayTitle = "Information",
                displayPrimary = "No Outsiders",
                displaySecondary = null,
                displayFooter = null,
                proposition = InformationProposition.AllOf(
                    listOf(InformationProposition.RoleInPlay(RoleId("Saint"), false)),
                ),
            )
        } else {
            ClocktowerDisplayOption(
                label = "${outcome.shownRole.value}: ${outcome.candidateSeats.joinToString(" / ")}",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayTitle = "Information",
                displayPrimary = outcome.shownRole.value,
                displaySecondary = outcome.candidateSeats.joinToString(" / "),
                displayFooter = null,
                proposition = InformationProposition.AnyOf(
                    outcome.candidateSeats.map { seat ->
                        InformationProposition.RoleAt(seat, outcome.shownRole)
                    },
                ),
            )
        }

    private fun keyOf(option: ClocktowerDisplayOption): Pair<RoleId?, List<Int>>? = when (val proposition = option.proposition) {
        is InformationProposition.AnyOf -> {
            val rolesAt = proposition.alternatives.map { it as? InformationProposition.RoleAt ?: return null }
            val role = rolesAt.map { it.role }.distinct().singleOrNull() ?: return null
            role to rolesAt.map { it.seat }.sorted()
        }
        is InformationProposition.AllOf -> null to emptyList()
        else -> null
    }
}
