package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Real confirmed action order: typed facts and accepted-step revision must never diverge. */
class ClocktowerGameSessionConfirmedActionBoundaryTest {
    private val base = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `successive confirmations read the live shared cursor without a narration counter`() {
        val session = create("live-input-sequence")
        assertEquals(1, session.nextInputSequence)
        repeat(2) {
            val sequence = session.nextInputSequence
            confirm(session, ActionFactDraft.Attack(
                actionId = "attack-$sequence", phase = StorytellerPhase.NIGHT,
                round = 2, sequence = sequence, targetSeat = 8,
            ))
        }
        assertEquals(listOf("attack-1", "attack-2"), session.state.actionTimeline.entries.map { it.fact.actionId })
        assertEquals(listOf(1, 2), session.state.actionTimeline.entries.map { it.point.sequence })
        assertEquals(3, session.nextInputSequence)
        session.recordPlayerInput()
        assertEquals(3, session.nextInputSequence)
    }

    @Test
    fun `attack and protection confirmations are ordered without duplicating mechanical state`() {
        val session = create("a1-night-actions")
        val original = session.state.gameState
        val attack = ActionFactDraft.Attack(
            actionId = "night-two-attack-eight",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            targetSeat = 8,
        )
        val protect = ActionFactDraft.Protect(
            actionId = "night-two-protect-eight",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 2,
            targetSeat = 8,
        )
        val first = confirm(session, attack)
        val second = confirm(session, protect)
        assertEquals(original, session.state.gameState)
        assertEquals(2L, session.state.gameStateRevision)
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(listOf(first, second), session.state.actionTimeline.entries)
        assertEquals(listOf(0L, 1L), session.state.actionTimeline.entries.map { it.fact.sequence })
        assertEquals(1, session.state.actionTimeline.entries.count { it.fact is ActionFact.Attack })
        assertEquals(1, session.state.actionTimeline.entries.count { it.fact is ActionFact.Protect })

        val accepted = session.state
        assertEquals(first, session.commitConfirmedGameAction(attack, original, 0L))
        assertEquals(accepted, session.state)
        assertNotNull(runCatching {
            session.commitConfirmedGameAction(
                attack.copy(targetSeat = 7),
                session.state.gameState,
                session.state.gameStateRevision,
            )
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
    }

    @Test
    fun `confirmed Butler master is an ordered action and not a duplicate on retry`() {
        val session = create("a1-butler-master")
        val initial = session.state.gameState
        val choice = ActionFactDraft.ButlerMasterChoice(
            actionId = "night-two-butler-four-master-one",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            butlerSeat = 4,
            masterSeat = 1,
        )
        val entry = confirm(session, choice)
        assertEquals(ActionFact.ButlerMasterChoice(
            actionId = choice.actionId, sequence = 0L,
            butlerSeat = 4, masterSeat = 1,
        ), entry.fact)
        assertEquals(initial, session.state.gameState)
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1L, session.state.nextTimelineGlobalSequence)
        val accepted = session.state
        assertEquals(entry, session.commitConfirmedGameAction(choice, initial, 0L))
        assertEquals(accepted, session.state)
        assertNotNull(runCatching {
            session.commitConfirmedGameAction(choice.copy(masterSeat = 2), initial, 1L)
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
        assertNotNull(runCatching {
            ActionFactDraft.ButlerMasterChoice(
                actionId = "illegal-butler-self", phase = StorytellerPhase.NIGHT,
                round = 2, sequence = 2, butlerSeat = 4, masterSeat = 4,
            )
        }.exceptionOrNull())
    }

    @Test
    fun `nomination vote slayer miss and no execution commit as four actual steps`() {
        val session = create("a1-public-actions")
        val facts = listOf(
            ActionFactDraft.Nomination(
                actionId = "day-nomination", phase = StorytellerPhase.DAY, round = 2,
                sequence = 1, nominatorSeat = 1, nomineeSeat = 8,
                firstVirginNomination = false,
            ),
            ActionFactDraft.Vote(
                actionId = "day-vote", phase = StorytellerPhase.DAY, round = 2,
                sequence = 2, nominatorSeat = 1, nomineeSeat = 8,
                voterSeats = listOf(1, 3, 5), ghostVoterSeats = emptyList(),
            ),
            ActionFactDraft.SlayerShot(
                actionId = "slayer-miss", phase = StorytellerPhase.DAY, round = 2,
                sequence = 3, claimantSeat = 2, targetSeat = 5,
                abilityConsumed = true, hit = false,
            ),
            ActionFactDraft.NoExecution(
                actionId = "no-execution-day-two", phase = StorytellerPhase.DAY,
                round = 2, sequence = 4,
            ),
        )
        val committed = facts.map { confirm(session, it) }
        assertEquals(4L, session.state.gameStateRevision)
        assertEquals(4L, session.state.nextTimelineGlobalSequence)
        assertEquals(committed, session.state.actionTimeline.entries)
        assertEquals(base, session.state.gameState)
        assertEquals(1, committed.count { it.fact is ActionFact.NoExecution })
        assertEquals(1, committed.count { it.fact is ActionFact.Vote })
        assertEquals(1, committed.count { it.fact is ActionFact.Nomination })
        assertEquals(1, committed.count { it.fact is ActionFact.SlayerShot })

        val accepted = session.state
        assertNotNull(runCatching {
            session.commitConfirmedGameAction(
                ActionFactDraft.NoExecution(
                    actionId = "stale-day-close", phase = StorytellerPhase.DAY,
                    round = 2, sequence = 5,
                ),
                base,
                expectedGameStateRevision = 0L,
            )
        }.exceptionOrNull())
        assertEquals(accepted, session.state)
    }

    @Test
    fun `published unreliable information uses observation cursor without mechanical revision`() {
        val session = create("a1-unreliable-publication")
        val shown = com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft(
            recordId = "first-night-chef-shown-zero",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 2,
            sourceSeat = 1,
            sourceAbility = com.codex.campboardgamehost.clocktower.domain.RoleId("Chef"),
            visibility = com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability.KNOWN_MALFUNCTIONING,
            proposition = com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.NumericResult(
                metric = com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.ADJACENT_EVIL_PAIRS,
                sourceSeat = 1,
                subjectSeats = (1..8).toList(),
                value = 0,
            ),
        )
        val recorded = session.commitGlobalEpistemicObservation(shown)
        assertEquals(0L, session.state.gameStateRevision)
        assertEquals(1L, session.state.playerInputRevision)
        assertEquals(1L, session.state.nextTimelineGlobalSequence)
        assertEquals(shown.proposition, recorded.proposition)

        val afterPublication = session.state
        assertEquals(recorded, session.commitGlobalEpistemicObservation(shown))
        assertEquals(afterPublication, session.state)

        confirm(session, ActionFactDraft.Attack(
            actionId = "first-night-independent-attack",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 3,
            targetSeat = 3,
        ))
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1L, session.state.playerInputRevision)
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(1L, session.state.actionTimeline.entries.single().point.globalSequence)
    }

    @Test
    fun `explicit day registrations share their action identity and reject stale or changed retry atomically`() {
        val session = create("confirmed-registration")
        // No actual-role, impairment or rule-candidate prerequisite for a confirmed value.
        val shot = ActionFactDraft.SlayerShot(
            "registered-shot", StorytellerPhase.DAY, 2, 1, 1, 2,
            abilityConsumed = false, hit = false, targetRegistersAsDemon = true,
        )
        val first = confirm(session, shot)
        assertEquals(true, (first.fact as ActionFact.SlayerShot).targetRegistersAsDemon)
        val accepted = session.state
        assertEquals(first, session.commitConfirmedGameAction(shot, base, 0L))
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            confirm(session, shot.copy(targetRegistersAsDemon = false))
        }
        org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            session.commitConfirmedGameAction(shot.copy(actionId = "stale-shot", sequence = 2), base, 0L)
        }
        assertEquals(accepted, session.state)
        confirm(session, ActionFactDraft.Nomination(
            "registered-nomination", StorytellerPhase.DAY, 2, 2, 2, 3,
            firstVirginNomination = false, nominatorRegistersAsTownsfolk = false,
        ))
        assertEquals(false, (session.state.actionTimeline.entries.last().fact as
            ActionFact.Nomination).nominatorRegistersAsTownsfolk)
        assertEquals(base, session.state.gameState)
        assertEquals(2L, session.state.gameStateRevision)
        assertEquals(listOf(0L, 1L), session.state.actionTimeline.entries.map { it.point.globalSequence })
    }

    @Test
    fun `AI receives only explicit registration answers while public replay keeps them private`() {
        val session = create("private-registration")
        listOf<Boolean?>(null, false, true).forEachIndexed { index, answer ->
            confirm(session, ActionFactDraft.SlayerShot(
                "shot-$index", StorytellerPhase.DAY, 2, session.nextInputSequence, 1, 2,
                abilityConsumed = true, hit = false, targetRegistersAsDemon = answer,
            ))
            confirm(session, ActionFactDraft.Nomination(
                "nomination-$index", StorytellerPhase.DAY, 2, session.nextInputSequence, 2, 3,
                firstVirginNomination = false, nominatorRegistersAsTownsfolk = answer,
            ))
        }
        val prefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            session.state, com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1(
                session.state.gameStateRevision, session.state.playerInputRevision,
            ),
        )
        val encoded = StorytellerGlobalEventProjectionV1.encode(prefix)
        assertEquals(6, encoded.length())
        org.junit.Assert.assertFalse(encoded.getJSONObject(0).has("targetRegistersAsDemon"))
        org.junit.Assert.assertFalse(encoded.getJSONObject(1).has("nominatorRegistersAsTownsfolk"))
        assertEquals(false, encoded.getJSONObject(2).getBoolean("targetRegistersAsDemon"))
        assertEquals(false, encoded.getJSONObject(3).getBoolean("nominatorRegistersAsTownsfolk"))
        assertEquals(true, encoded.getJSONObject(4).getBoolean("targetRegistersAsDemon"))
        assertEquals(true, encoded.getJSONObject(5).getBoolean("nominatorRegistersAsTownsfolk"))
        val delta = StorytellerGlobalEventProjectionV1.encode(prefix, 4L)
        assertEquals(2, delta.length())
        assertEquals("slayer", delta.getJSONObject(0).getString("type"))
    }

    private fun confirm(session: ClocktowerGameSession, draft: ActionFactDraft) =
        session.commitConfirmedGameAction(
            draft = draft,
            nextGameState = session.state.gameState,
            expectedGameStateRevision = session.state.gameStateRevision,
        )

    private fun create(id: String) = ClocktowerGameSession.createProduction(
        gameId = id,
        gameSeed = base.seed,
        initialState = base,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
}
