package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
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
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** One cross-phase Host → same global LLM transport → Host legal decision integration. */
class StorytellerGlobalNightContinuationV1Test {
    private fun seat(n: Int, role: String, kind: CharacterType) = PlayerState(
        seat = n,
        name = "Seat $n",
        actualRole = RoleId(role),
        shownRole = RoleId(role),
        actualType = kind,
        actualAlignment = if (kind == CharacterType.MINION || kind == CharacterType.DEMON) {
            Alignment.EVIL
        } else Alignment.GOOD,
        alive = true,
    )

    private fun game() = GameState(
        script = ScriptId("trouble_brewing"),
        seed = 810L,
        players = listOf(
            seat(1, "Mayor", CharacterType.TOWNSFOLK),
            seat(2, "Chef", CharacterType.TOWNSFOLK),
            seat(3, "Investigator", CharacterType.TOWNSFOLK),
            seat(4, "Washerwoman", CharacterType.TOWNSFOLK),
            seat(5, "Empath", CharacterType.TOWNSFOLK),
            seat(6, "Recluse", CharacterType.OUTSIDER),
            seat(7, "Poisoner", CharacterType.MINION),
            seat(8, "Imp", CharacterType.DEMON),
        ),
    )

    private fun snapshot(session: ClocktowerGameSession) = TroubleBrewingGameSnapshotV1(
        gameId = session.state.gameId, gameSeed = session.state.gameSeed,
        position = TroubleBrewingSnapshotPosition(
            stage = TroubleBrewingSnapshotStage.RUNTIME,
            phase = SnapshotField.Known(StorytellerPhase.NIGHT),
            round = SnapshotField.Known(2),
            gameStateRevision = SnapshotField.Known(session.state.gameStateRevision),
            playerInputRevision = SnapshotField.Known(session.state.playerInputRevision),
        ),
        grimoireSeats = session.state.gameState.players.map {
            TroubleBrewingSnapshotSeat(
                seat = it.seat,
                shownRoleId = SnapshotField.Known((it.shownRole ?: it.actualRole).value.lowercase()),
                actualRoleId = SnapshotField.Known(it.actualRole.value.lowercase()),
                alive = SnapshotField.Known(it.alive),
                poisoned = SnapshotField.Known(it.poisoned),
            )
        },
        setupState = TroubleBrewingSnapshotSetupState(
            hasDrunk = SnapshotField.Known(false),
            drunkAssignmentSeat = SnapshotField.NotApplicable,
        ),
    )

    private fun pending(session: ClocktowerGameSession) = MayorRedirectDecisionBoundary.create(
        requestIdentity = StorytellerDecisionRequestIdentity(
            gameId = session.state.gameId, requestId = "night:2:redirect:cursor-${session.state.nextTimelineGlobalSequence}",
        ),
        revision = StorytellerDecisionRevision(
            session.state.gameStateRevision, session.state.playerInputRevision,
        ),
        game = session.state.gameState,
    )

    private fun provider(session: ClocktowerGameSession, decision: PendingMayorRedirectDecision) =
        StorytellerProviderRequestFactoryV1.fromMayorRedirect(
            decision = decision,
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
    fun `night two legal discretion sees private first night and public day events without future leaks`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "global-cross-phase", gameSeed = 810L, initialState = game(),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "first-night-chef-0",
                phase = StorytellerPhase.FIRST_NIGHT, round = 1, sequence = 0,
                sourceSeat = 2, sourceAbility = RoleId("Chef"),
                visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(2),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.NumericResult(
                    NumericMetric.ADJACENT_EVIL_PAIRS, 2, (1..8).toList(), 0,
                ),
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.Nomination(
                "day-nomination", StorytellerPhase.DAY, 1, 1,
                nominatorSeat = 7, nomineeSeat = 1, firstVirginNomination = false,
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.SlayerShot(
                "public-ineffective-shot", StorytellerPhase.DAY, 1, 2,
                claimantSeat = 6, targetSeat = 8, abilityConsumed = false, hit = false,
            ),
        )
        val current = pending(session)
        val request = provider(session, current)
        val serialized = JSONObject(StorytellerGlobalDecisionRequestV1.encode(request, null))
        assertEquals("mayor-redirect", serialized.getJSONObject("identity").getString("decisionTypeId"))
        assertEquals("NOT_APPLICABLE", serialized.getJSONObject("decisionContext").getString("reliability"))
        val history = serialized.getJSONObject("causalHistory")
        assertEquals(3L, history.getLong("exclusiveGlobalSequence"))
        val events = history.getJSONArray("events")
        assertEquals(3, events.length())
        assertEquals("PRIVATE", events.getJSONObject(0).getString("visibility"))
        assertEquals("PUBLIC", events.getJSONObject(1).getJSONObject("action").getString("visibility"))
        assertEquals(false, events.getJSONObject(2).getJSONObject("action").getBoolean("abilityConsumed"))
        assertEquals("SLAYER_SHOT", events.getJSONObject(2).getJSONObject("action").getString("type"))
        val targets = (0 until serialized.getJSONArray("legalCandidates").length()).map {
            serialized.getJSONArray("legalCandidates").getJSONObject(it).getInt("targetSeat")
        }.toSet()
        assertTrue(1 in targets && 7 in targets && 8 !in targets)
        val options = current.pending.legalCandidates
        val answer = StorytellerProviderResponseV1(
            decisionId = request.identity.decisionId,
            sourceRevision = request.sourceRevision,
            outcome = StorytellerProviderOutcomeV1.Recommendation(
                primary = StorytellerProviderRecommendationV1(
                    options[0].candidateId, listOf("Respond to the full game"),
                ),
                alternatives = listOf(StorytellerProviderRecommendationV1(
                    options[1].candidateId, listOf("Different coherent future world"),
                )),
            ),
        )
        assertTrue(StorytellerGlobalDecisionRequestV1.validateCurrent(
            request, current, current, session.state, answer,
        ) is StorytellerProviderValidationV1.AcceptedRecommendation)
        // A later committed event can invalidate the old advice even when revisions did not change.
        session.commitGlobalActionFact(
            ActionFactDraft.Attack("night-two-confirmed-attack", StorytellerPhase.NIGHT, 2, 3, 1),
        )
        val fresh = pending(session)
        assertNull(StorytellerGlobalDecisionRequestV1.validateCurrent(
            request, current, fresh, session.state, answer,
        ))
        val later = JSONObject(StorytellerGlobalDecisionRequestV1.encode(
            provider(session, fresh), null,
        ))
        assertEquals(4, later.getJSONObject("causalHistory").getJSONArray("events").length())
        assertEquals(3, events.length()) // First snapshot stays immutable.
    }
}
