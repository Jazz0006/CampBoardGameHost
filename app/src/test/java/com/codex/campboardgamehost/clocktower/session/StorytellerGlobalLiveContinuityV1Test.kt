package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderOutcomeV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRecommendationV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderResponseV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderValidationV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Host-real action, private publication, subsequent legal decision and stale-response barrier. */
class StorytellerGlobalLiveContinuityV1Test {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    private fun player(seat: Int, role: String, type: CharacterType) = PlayerState(
        seat = seat, name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = if (type == CharacterType.MINION || type == CharacterType.DEMON) {
            Alignment.EVIL
        } else Alignment.GOOD,
        actualType = type, shownRole = RoleId(role), alive = true,
    )

    private fun game() = GameState(
        script = ScriptId("trouble_brewing"), seed = 700L,
        players = listOf(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Librarian", CharacterType.TOWNSFOLK),
            player(3, "Investigator", CharacterType.TOWNSFOLK),
            player(4, "Chef", CharacterType.TOWNSFOLK),
            player(5, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(6, "Recluse", CharacterType.OUTSIDER),
            player(7, "Poisoner", CharacterType.MINION),
            player(8, "Imp", CharacterType.DEMON),
        ),
    )

    private fun snapshot(session: ClocktowerGameSession): TroubleBrewingGameSnapshotV1 =
        TroubleBrewingGameSnapshotV1(
            gameId = session.state.gameId, gameSeed = session.state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.RUNTIME,
                phase = SnapshotField.Known(StorytellerPhase.FIRST_NIGHT),
                round = SnapshotField.Known(1),
                gameStateRevision = SnapshotField.Known(session.state.gameStateRevision),
                playerInputRevision = SnapshotField.Known(session.state.playerInputRevision),
            ),
            grimoireSeats = session.state.gameState.players.map { player ->
                TroubleBrewingSnapshotSeat(
                    seat = player.seat,
                    shownRoleId = SnapshotField.Known((player.shownRole ?: player.actualRole).value.lowercase()),
                    actualRoleId = SnapshotField.Known(player.actualRole.value.lowercase()),
                    alive = SnapshotField.Known(player.alive),
                    poisoned = SnapshotField.Known(player.poisoned),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(
                hasDrunk = SnapshotField.Known(false),
                drunkAssignmentSeat = SnapshotField.NotApplicable,
            ),
        )

    private fun decision(
        session: ClocktowerGameSession,
        sourceSeat: Int,
        role: String,
        reliability: ReliabilityState = ReliabilityState.RELIABLE,
    ): PendingPairInformationDecision =
        PairInformationDecisionBoundary.create(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = session.state.gameId,
                requestId = "first-night:$role:$sourceSeat",
            ),
            revision = StorytellerDecisionRevision(
                session.state.gameStateRevision, session.state.playerInputRevision,
            ),
            game = session.state.gameState,
            roleDefinitions = roles, sourceSeat = sourceSeat,
            abilityRole = RoleId(role), reliability = reliability,
        )

    private fun provider(session: ClocktowerGameSession, pending: PendingPairInformationDecision) =
        StorytellerProviderRequestFactoryV1.fromPairInformation(
            decision = pending,
            snapshot = snapshot(session),
            gameContext = StorytellerProviderGameContextBuilderV1.build(
                snapshot(session),
                StorytellerProviderRevisionV1(
                    session.state.gameStateRevision, session.state.playerInputRevision,
                ),
                session.state,
            ),
        )

    @Test
    fun `real Host events replan next legal choice without retroactively changing observations`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "whole-game-700", gameSeed = 700L, initialState = game(),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalActionFact(
            ActionFactDraft.Poison("first-confirmed-action", StorytellerPhase.FIRST_NIGHT, 1, 0, 2),
        )
        session.commitPoisonTargetBoundary(2)
        val firstPending = decision(session, 1, "Washerwoman")
        val first = provider(session, firstPending)
        val firstJson = JSONObject(StorytellerGlobalDecisionRequestV1.encode(first, null))
        assertEquals("POISON", firstJson.getJSONObject("causalHistory")
            .getJSONArray("events").getJSONObject(0).getJSONObject("action").getString("type"))
        assertTrue(firstJson.getJSONObject("state").getJSONArray("seats")
            .getJSONObject(1).getBoolean("poisoned"))
        assertEquals(1L, firstJson.getJSONObject("causalHistory").getLong("exclusiveGlobalSequence"))
        assertTrue(firstPending.legalCandidates.size >= 2)

        fun commitShownPair(
            pending: PendingPairInformationDecision,
            index: Int,
            recordId: String,
            sequence: Int,
        ) {
            val option = pending.legalCandidates[index]
            val committed = pending.confirm(option.candidateId, pending.revision)
                as PairInformationDecisionConfirmation.Confirmed
            assertEquals(option.candidateId, committed.candidateId)
            val observedRole = requireNotNull(committed.observation.shownRole)
            val seats = committed.observation.candidateSeats
            assertEquals(2, seats.size)
            session.commitGlobalEpistemicObservation(
                EpistemicObservationDraft(
                    recordId = recordId,
                    phase = StorytellerPhase.FIRST_NIGHT, round = 1, sequence = sequence,
                    sourceSeat = pending.sourceSeat, sourceAbility = pending.abilityRole,
                    visibility = ObservationVisibility.PRIVATE,
                    recipientSeats = setOf(pending.sourceSeat),
                    reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                    proposition = InformationProposition.AnyOf(
                        seats.map { seat -> InformationProposition.RoleAt(seat, observedRole) },
                    ),
                ),
            )
        }

        commitShownPair(firstPending, 0, "washerwoman-confirmed", 1)
        val secondPending = decision(session, 2, "Librarian", ReliabilityState.POISONED)
        val second = provider(session, secondPending)
        val secondJson = JSONObject(StorytellerGlobalDecisionRequestV1.encode(second, null))
        val secondHistory = secondJson.getJSONObject("causalHistory").getJSONArray("events")
        assertEquals(2, secondHistory.length())
        assertEquals("observation", secondHistory.getJSONObject(1).getString("kind"))
        assertEquals("PRIVATE", secondHistory.getJSONObject(1).getString("visibility"))
        assertEquals(2L, secondJson.getJSONObject("causalHistory").getLong("exclusiveGlobalSequence"))
        assertTrue(secondPending.legalCandidates.size >= 2)

        val suggested = StorytellerProviderResponseV1(
            decisionId = second.identity.decisionId, sourceRevision = second.sourceRevision,
            outcome = StorytellerProviderOutcomeV1.Recommendation(
                primary = StorytellerProviderRecommendationV1(
                    secondPending.legalCandidates[0].candidateId, listOf("Whole-game rationale"),
                ),
                alternatives = listOf(StorytellerProviderRecommendationV1(
                    secondPending.legalCandidates[1].candidateId, listOf("Distinct world"),
                )),
            ),
        )
        assertTrue(StorytellerGlobalDecisionRequestV1.validateCurrent(
            second, secondPending, secondPending, session.state, suggested,
        ) is StorytellerProviderValidationV1.AcceptedRecommendation)

        // The Host human now overrides the LLM's primary with a distinct legal second result.
        val alternateIndex = secondPending.legalCandidates.indexOfFirst {
            it.candidateId != secondPending.legalCandidates[0].candidateId &&
                it.outcome.shownRole != null && it.outcome.candidateSeats.size == 2
        }
        assertTrue(alternateIndex >= 1)
        commitShownPair(secondPending, alternateIndex, "librarian-manual-override", 2)
        val thirdPending = decision(session, 3, "Investigator")
        val third = provider(session, thirdPending)
        val thirdJson = JSONObject(StorytellerGlobalDecisionRequestV1.encode(third, null))
        val events = thirdJson.getJSONObject("causalHistory").getJSONArray("events")
        assertEquals(3, events.length())
        assertEquals(listOf(0L, 1L, 2L),
            (0 until events.length()).map { events.getJSONObject(it).getLong("globalSequence") })
        assertEquals("librarian-manual-override", events.getJSONObject(2).getString("eventId"))
        // Already confirmed observations cannot be changed by later strategic advice.
        assertEquals("washerwoman-confirmed", secondHistory.getJSONObject(1).getString("eventId"))
        assertNull(StorytellerGlobalDecisionRequestV1.validateCurrent(
            second, secondPending, thirdPending, session.state, suggested,
        ))
        assertTrue(third.legalCandidates.isNotEmpty())
    }
}
