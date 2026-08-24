package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.B4DynamicPlayerWorldSetShadow
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowCandidate
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowOutcome
import com.codex.campboardgamehost.clocktower.epistemic.B4ShadowRequest
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerB4HistoricalShadowCoordinatorTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-session-b4-shadow-test",
        sourceRevision = "official",
    )
    private val setupSnapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val perceived = setupSnapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `session shadow coordinator consumes real global history without becoming recommendation authority`() {
        val session = ClocktowerGameSession.create(
            gameId = setupSnapshot.gameId,
            gameSeed = setupSnapshot.gameSeed,
            rulesetRef = rulesetRef,
            initialState = setupSnapshot.gameState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalActionFact(
            ActionFactDraft.Protect(
                actionId = "session-protect",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
                targetSeat = 1,
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.Attack(
                actionId = "session-attack",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 2,
                targetSeat = 2,
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.RoleChange(
                actionId = "session-role-change",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 3,
                targetSeat = 4,
                role = RoleId("Imp"),
                alignment = Alignment.EVIL,
                type = CharacterType.DEMON,
            ),
        )

        val setupFormal = FormalGameState.from(setupSnapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val candidateObservation = EpistemicObservation(
            observationId = "public-alive-seat-two",
            snapshotId = setupFormal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 4,
            sourceSeat = null,
            sourceAbility = null,
            visibility = ObservationVisibility.PUBLIC,
            recipientSeats = emptySet(),
            reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
            proposition = InformationProposition.AliveAt(2, true),
        )
        val candidates = listOf(
            B4ShadowCandidate(
                candidateId = "alive-seat-two",
                recipientSeat = 1,
                observation = candidateObservation,
            ),
        )
        val currentSnapshot = session.snapshot

        assertEquals(
            listOf("session-protect", "session-attack", "session-role-change"),
            currentSnapshot.actionTimeline.reducerFacts().map { it.actionId },
        )

        val expected = B4DynamicPlayerWorldSetShadow(validatedRuleset).evaluate(
            B4ShadowRequest(
                initialSnapshot = setupSnapshot,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = currentSnapshot.actionTimeline,
                perceivedRolesBySeat = perceived,
                observationLog = currentSnapshot.epistemicObservationLog,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
                candidates = candidates,
            ),
        )
        val actual = ClocktowerB4HistoricalShadowCoordinator(validatedRuleset).evaluate(
            setupSnapshot = setupSnapshot,
            currentSnapshot = currentSnapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            perceivedRolesBySeat = perceived,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
            candidates = candidates,
        )

        assertEquals(B4ShadowOutcome.READY, actual.outcome)
        assertEquals(expected, actual)
    }
}
