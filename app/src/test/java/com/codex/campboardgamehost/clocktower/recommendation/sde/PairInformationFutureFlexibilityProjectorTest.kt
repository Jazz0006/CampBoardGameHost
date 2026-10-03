package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationFutureFlexibilityProjectorTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `G10 Librarian pair preserves two recurring information routes without role-name policy`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(2, "Imp", CharacterType.DEMON),
            player(3, "Undertaker", CharacterType.TOWNSFOLK),
            player(4, "Librarian", CharacterType.TOWNSFOLK),
            player(5, "Spy", CharacterType.MINION),
            player(6, "Monk", CharacterType.TOWNSFOLK),
            player(7, "Mayor", CharacterType.TOWNSFOLK),
            player(8, "Virgin", CharacterType.TOWNSFOLK),
            player(9, "Butler", CharacterType.OUTSIDER),
        )
        val legal = legalLibrarian(game = game, sourceSeat = 4)

        assertEquals(40, legal.size)

        val projected = PairInformationFutureFlexibilityProjector.project(
            game = game,
            legalCandidates = legal,
        )
        val observed = legal.single { candidate ->
            candidate.outcome.shownRole == RoleId("Drunk") &&
                candidate.outcome.candidateSeats == listOf(1, 3)
        }
        val lowConsequence = legal.single { candidate ->
            candidate.outcome.shownRole == RoleId("Drunk") &&
                candidate.outcome.candidateSeats == listOf(1, 7)
        }

        val observedFeatures = projected.getValue(observed.candidateId).requireProjected()
        assertEquals(
            setOf(
                "recurring-player-information:seat-1",
                "recurring-player-information:seat-3",
            ),
            observedFeatures.retainedRouteIds,
        )
        assertTrue(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                observedFeatures.reasonCodes,
        )
        assertTrue(observedFeatures.reasonCodes.none { reason -> "undertaker" in reason.lowercase() })

        val lowFeatures = projected.getValue(lowConsequence.candidateId).requireProjected()
        assertEquals(
            setOf("recurring-player-information:seat-1"),
            lowFeatures.retainedRouteIds,
        )
        assertFalse(
            PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES in
                lowFeatures.reasonCodes,
        )
    }

    @Test
    fun `equivalent recurring roles project the same generic future-flexibility reason`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Fortune Teller"),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Librarian", CharacterType.TOWNSFOLK),
            player(4, "Butler", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )
        val legal = legalLibrarian(game = game, sourceSeat = 3)
        val candidate = legal.single { alternative ->
            alternative.outcome.shownRole == RoleId("Drunk") &&
                alternative.outcome.candidateSeats == listOf(1, 2)
        }

        val features = PairInformationFutureFlexibilityProjector
            .project(game = game, legalCandidates = legal)
            .getValue(candidate.candidateId)
            .requireProjected()

        assertEquals(
            setOf(
                "recurring-player-information:seat-1",
                "recurring-player-information:seat-2",
            ),
            features.retainedRouteIds,
        )
        assertEquals(
            setOf(
                PairInformationFutureFlexibilityReasonCodes.PRESERVES_COMPETING_RECURRING_INFORMATION_ROUTES,
            ),
            features.reasonCodes,
        )
    }

    @Test
    fun `zero-Outsider Librarian result is explicitly not applicable`() {
        val game = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val legal = legalLibrarian(game = game, sourceSeat = 1)

        assertEquals(1, legal.size)
        assertEquals(emptyList<Int>(), legal.single().outcome.candidateSeats)
        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE),
            PairInformationFutureFlexibilityProjector
                .project(game = game, legalCandidates = legal)
                .getValue(legal.single().candidateId),
        )
    }

    @Test
    fun `missing shown identity fails closed as missing capability`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Empath"),
            player(2, "Undertaker", CharacterType.TOWNSFOLK, shownRole = null),
            player(3, "Librarian", CharacterType.TOWNSFOLK),
            player(4, "Butler", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )
        val legal = legalLibrarian(game = game, sourceSeat = 3)
        val candidate = legal.single { alternative ->
            alternative.outcome.shownRole == RoleId("Drunk") &&
                alternative.outcome.candidateSeats == listOf(1, 2)
        }

        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.MISSING_CAPABILITY),
            PairInformationFutureFlexibilityProjector
                .project(game = game, legalCandidates = legal)
                .getValue(candidate.candidateId),
        )
    }

    private fun legalLibrarian(game: GameState, sourceSeat: Int) =
        PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = sourceSeat,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
        )

    private fun FeatureProjection<FutureFlexibilityFeatures>.requireProjected(): FutureFlexibilityFeatures =
        (this as FeatureProjection.Projected).value

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20261003L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String? = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = shownRole?.let(::RoleId),
    )
}
