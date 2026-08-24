package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class B4HistoricalExactShadowBridgeTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "a3-b4-historical-exact-shadow-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }

    @Test
    fun `validated B4 shadow accepts persisted hidden actions through historical exact baseline`() {
        val facts = listOf(
            ActionFact.Protect("actual-protect", 1L, 1),
            ActionFact.Attack("actual-attack", 2L, 2),
            ActionFact.RoleChange(
                actionId = "actual-role-change",
                sequence = 3L,
                targetSeat = 4,
                role = RoleId("Imp"),
                alignment = Alignment.EVIL,
                type = CharacterType.DEMON,
            ),
        )
        val timeline = timelineOf(facts)
        val initialFormal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val setupKnowledge = A4PlayerKnowledgeFactory.createAll(
            formal = initialFormal,
            perceivedRolesBySeat = perceived,
            observationLog = EpistemicObservationLog(),
        ).first { it.recipientSeat == 1 }
        val candidate = EpistemicObservation(
            observationId = "public-alive-candidate",
            snapshotId = initialFormal.snapshotId,
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
        val expected = EnumeratedHistoricalExactBaseline.build(
            validatedRuleset = validatedRuleset,
            rulesetRef = rulesetRef,
            setupKnowledge = setupKnowledge,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = timeline,
            observationLog = EpistemicObservationLog(),
        ).worldSet

        val report = B4DynamicPlayerWorldSetShadow(
            validatedRuleset = validatedRuleset,
        ).evaluate(
            B4ShadowRequest(
                initialSnapshot = snapshot,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = timeline,
                perceivedRolesBySeat = perceived,
                observationLog = EpistemicObservationLog(),
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
                candidates = listOf(B4ShadowCandidate("alive-seat-2", 1, candidate)),
            ),
        )

        assertEquals(B4ShadowOutcome.READY, report.outcome)
        assertEquals(expected.cardinality(), report.queries.single().before)
        assertEquals(expected.require(candidate).cardinality(), report.queries.single().after)
    }

    private fun timelineOf(facts: List<ActionFact>): ActionFactTimeline =
        ActionFactTimeline(
            facts.map { fact ->
                TimelineBoundActionFact(
                    fact = fact,
                    point = TimelinePoint(
                        phase = StorytellerPhase.FIRST_NIGHT,
                        round = 1,
                        sequence = fact.sequence.toInt(),
                        globalSequence = fact.sequence,
                    ),
                )
            },
        )
}
