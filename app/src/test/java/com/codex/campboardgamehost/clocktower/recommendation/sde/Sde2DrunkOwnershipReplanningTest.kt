package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.commitPoisonTargetBoundary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2DrunkOwnershipReplanningTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()
    private val drunkSeat = 6
    private val investigator = RoleId("Investigator")

    @Test
    fun `Poisoner draft invalidates planned Drunk clue through player input revision without changing shown identity`() {
        val session = newSession("sde-2a-drunk-draft")
        val planned = plannedDrunkClue(session, "pair-7-8")
        val beforeGameRevision = session.state.gameStateRevision
        val beforeShownRole = requireNotNull(session.state.gameState.playerAt(drunkSeat)).shownRole

        session.recordPlayerInput()

        assertEquals(beforeGameRevision, session.state.gameStateRevision)
        assertEquals(1L, session.state.playerInputRevision)
        assertFalse(planned.isCurrentFor(currentRevision(session)))
        assertEquals(investigator, beforeShownRole)
        assertEquals(investigator, requireNotNull(session.state.gameState.playerAt(drunkSeat)).shownRole)
    }

    @Test
    fun `confirmed poison invalidates a fresh planned Drunk clue through game revision without changing shown identity`() {
        val session = newSession("sde-2a-drunk-confirm")
        session.recordPlayerInput()
        val plannedAfterDraft = plannedDrunkClue(session, "pair-7-8")
        val beforeInputRevision = session.state.playerInputRevision

        session.commitPoisonTargetBoundary(targetSeat = drunkSeat)

        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(beforeInputRevision, session.state.playerInputRevision)
        assertFalse(plannedAfterDraft.isCurrentFor(currentRevision(session)))
        assertEquals(investigator, requireNotNull(session.state.gameState.playerAt(drunkSeat)).shownRole)
        assertTrue(requireNotNull(session.state.gameState.playerAt(drunkSeat)).poisoned)
    }

    @Test
    fun `committed Drunk clue remains durable across later poison replanning boundaries`() {
        val session = newSession("sde-2a-drunk-committed")
        val committed = session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "sde-2a-drunk-investigator-clue",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 20,
                sourceSeat = drunkSeat,
                sourceAbility = investigator,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(drunkSeat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.AnyOf(
                    listOf(
                        InformationProposition.RoleAt(7, RoleId("Scarlet Woman")),
                        InformationProposition.RoleAt(8, RoleId("Scarlet Woman")),
                    ),
                ),
            ),
        )
        val committedLog = session.state.epistemicObservationLog

        session.recordPlayerInput()
        session.commitPoisonTargetBoundary(targetSeat = drunkSeat)

        assertEquals(committedLog, session.state.epistemicObservationLog)
        assertEquals(committed, session.state.epistemicObservationLog.records.single())
        assertEquals(investigator, requireNotNull(session.state.gameState.playerAt(drunkSeat)).shownRole)
    }

    private fun plannedDrunkClue(
        session: ClocktowerGameSession,
        candidateId: String,
    ): PlannedDecisionRef = PlannedDecisionRef(
        decisionId = "first-night-drunk-investigator-$drunkSeat",
        candidateId = candidateId,
        sourceRevision = currentRevision(session),
        sourceSemanticIdentity = "drunk-investigator|seat:$drunkSeat|shown:${investigator.value}",
    )

    private fun currentRevision(session: ClocktowerGameSession): InformationDecisionRevision =
        InformationDecisionRevision(
            gameStateRevision = session.state.gameStateRevision,
            playerInputRevision = session.state.playerInputRevision,
        )

    private fun newSession(gameId: String): ClocktowerGameSession =
        ClocktowerGameSession.createProduction(
            gameId = gameId,
            gameSeed = initialState.seed,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
}
