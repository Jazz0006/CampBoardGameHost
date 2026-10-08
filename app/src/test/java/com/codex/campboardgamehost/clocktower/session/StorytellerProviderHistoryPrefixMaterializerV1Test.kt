package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCoverageStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryDimensionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryPrefixV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerProviderHistoryPrefixMaterializerV1Test {
    private fun newSession(
        mode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    ): ClocktowerGameSession {
        val game = TroubleBrewingFixtures.eightPlayerExample()
        return ClocktowerGameSession.createProduction(
            gameId = "r1b-game",
            gameSeed = game.seed,
            initialState = game,
            semanticHistoryMode = mode,
        )
    }

    private fun revision(session: ClocktowerGameSession) = StorytellerProviderRevisionV1(
        session.state.gameStateRevision, session.state.playerInputRevision,
    )

    @Test
    fun `typed mixed events retain global order and exclude future same-revision action`() {
        val session = newSession()
        session.commitGlobalActionFact(
            ActionFactDraft.Poison("poison", StorytellerPhase.FIRST_NIGHT, 1, 8, 2),
        )
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "empath-one",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 9,
                sourceSeat = 1,
                sourceAbility = RoleId("Empath"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.NumericResult(
                    NumericMetric.LIVING_EVIL_NEIGHBOURS, 1, listOf(2, 8), 1,
                ),
            ),
        )
        val priorState = session.state
        val priorRevision = revision(session)
        val before = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(priorState, priorRevision)
        session.commitGlobalActionFact(
            ActionFactDraft.Death("death", StorytellerPhase.DAWN, 1, 10, 3),
        )
        val after = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(session.state, revision(session))

        assertEquals(listOf(0L, 1L), before.entries.map { it.point.globalSequence })
        assertEquals(listOf("poison", "empath-one"), before.entries.map { it.entryId })
        assertEquals(2L, before.exclusiveGlobalSequence)
        assertEquals(StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED, before.cutoffSource)
        assertEquals(listOf(0L, 1L, 2L), after.entries.map { it.point.globalSequence })
        assertEquals(listOf("poison", "empath-one"), before.entries.map { it.entryId })
        assertEquals(priorRevision, revision(session)) // Mechanical fact capture does not bump game/input revision.
        assertEquals(
            StorytellerProviderHistoryCoverageStateV1.UNKNOWN,
            before.coverage.getValue(StorytellerProviderHistoryDimensionV1.REGISTRATION_RULINGS).state,
        )
        assertEquals(
            "NO_TYPED_EXPLICIT_REGISTRATION_PRODUCER",
            before.coverage.getValue(StorytellerProviderHistoryDimensionV1.REGISTRATION_RULINGS).reasonCode,
        )
        assertEquals(
            StorytellerProviderHistoryCoverageStateV1.UNKNOWN,
            before.coverage.getValue(StorytellerProviderHistoryDimensionV1.PRIOR_DECISIONS).state,
        )
        val observation = before.entries[1] as StorytellerProviderHistoryEntryV1.Observation
        assertEquals(1, (observation.proposition as InformationProposition.NumericResult).value)
        assertEquals(ObservationReliability.RECEIVED_AS_FUNCTIONING, observation.reliability)
    }

    @Test
    fun `deep immutable copy prevents externally mutated recipients and nested propositions`() {
        val session = newSession()
        val recipients = linkedSetOf(1)
        val mutableSeats = mutableListOf(2, 8)
        val choices = mutableListOf<InformationProposition>(
            InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, 1, mutableSeats, 1),
            InformationProposition.AliveAt(3, true),
        )
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "compound-observation",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
                sourceSeat = 1,
                sourceAbility = RoleId("Empath"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = recipients,
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.AnyOf(choices),
            ),
        )
        val first = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(session.state, revision(session))
        val second = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(session.state, revision(session))
        assertEquals(first, second)

        recipients.add(4)
        mutableSeats[0] = 5
        choices[1] = InformationProposition.AliveAt(3, false)

        val observation = first.entries.single() as StorytellerProviderHistoryEntryV1.Observation
        assertEquals(setOf(1), observation.recipientSeats)
        val compound = observation.proposition as InformationProposition.AnyOf
        assertEquals(listOf(2, 8), (compound.alternatives[0] as InformationProposition.NumericResult).subjectSeats)
        assertEquals(InformationProposition.AliveAt(3, true), compound.alternatives[1])
        assertThrows(UnsupportedOperationException::class.java) {
            (first.entries as MutableList<StorytellerProviderHistoryEntryV1>).clear()
        }
        assertThrows(UnsupportedOperationException::class.java) {
            (observation.recipientSeats as MutableSet<Int>).add(6)
        }
        assertThrows(UnsupportedOperationException::class.java) {
            (compound.alternatives as MutableList<InformationProposition>).clear()
        }
    }

    @Test
    fun `unfrozen historical cutoff refuses later same-revision facts and corrections`() {
        val session = newSession()
        val oldRevision = revision(session)
        session.commitGlobalActionFact(
            ActionFactDraft.Attack("after-unrecorded-decision", StorytellerPhase.NIGHT, 2, 5, 3),
        )
        val unavailable = StorytellerProviderHistoryPrefixMaterializerV1.withoutFrozenHistoricalCutoff(
            gameId = session.state.gameId,
            sourceRevision = oldRevision,
        )
        assertEquals(StorytellerProviderHistoryCutoffSourceV1.UNAVAILABLE, unavailable.cutoffSource)
        assertEquals(null, unavailable.exclusiveGlobalSequence)
        assertTrue(unavailable.entries.isEmpty())
        assertTrue(unavailable.coverage.values.all {
            it.state == StorytellerProviderHistoryCoverageStateV1.UNRECONSTRUCTABLE &&
                it.reasonCode == "HISTORICAL_CUTOFF_UNAVAILABLE"
        })
        assertEquals(oldRevision, revision(session)) // Same revisions cannot prove chronology.
        assertEquals(1, StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            session.state, revision(session),
        ).entries.size)
    }

    @Test
    fun `ambiguous Spy Recluse Empath observation never becomes a canonical witness`() {
        val game = com.codex.campboardgamehost.clocktower.domain.GameState(
            script = com.codex.campboardgamehost.clocktower.domain.ScriptId("trouble_brewing"),
            players = listOf(
                com.codex.campboardgamehost.clocktower.domain.PlayerState(
                    1, "Spy", RoleId("Spy"),
                    com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.MINION,
                    RoleId("Spy"), true,
                ),
                com.codex.campboardgamehost.clocktower.domain.PlayerState(
                    2, "Empath", RoleId("Empath"),
                    com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD,
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK,
                    RoleId("Empath"), true,
                ),
                com.codex.campboardgamehost.clocktower.domain.PlayerState(
                    3, "Recluse", RoleId("Recluse"),
                    com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD,
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.OUTSIDER,
                    RoleId("Recluse"), true,
                ),
                com.codex.campboardgamehost.clocktower.domain.PlayerState(
                    4, "Chef", RoleId("Chef"),
                    com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD,
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK,
                    RoleId("Chef"), true,
                ),
                com.codex.campboardgamehost.clocktower.domain.PlayerState(
                    5, "Imp", RoleId("Imp"),
                    com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                    com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON,
                    RoleId("Imp"), true,
                ),
            ),
            seed = 99L,
        )
        val witnesses = com.codex.campboardgamehost.clocktowerAlignmentRegistrationWitnesses(
            currentSpyRegistersGood = false, spySelectable = true,
            currentRecluseRegistersEvil = false, recluseSelectable = true,
        ).filter { witness ->
            (if (witness.spyRegistersGood == false) 1 else 0) +
                (if (witness.recluseRegistersEvil == true) 1 else 0) == 1
        }
        assertEquals(2, witnesses.size)
        val session = ClocktowerGameSession.createProduction(
            gameId = "empath-ambiguous",
            gameSeed = game.seed,
            initialState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "empath-result-one",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
                sourceSeat = 2,
                sourceAbility = RoleId("Empath"),
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(2),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.NumericResult(
                    NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 1,
                ),
            ),
        )
        val prefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            session.state, revision(session),
        )
        assertEquals(1, prefix.entries.size)
        assertEquals("empath-result-one", prefix.entries.single().entryId)
        assertEquals(
            StorytellerProviderHistoryCoverageStateV1.UNKNOWN,
            prefix.coverage.getValue(StorytellerProviderHistoryDimensionV1.REGISTRATION_RULINGS).state,
        )
        assertTrue(prefix.entries.none { it is StorytellerProviderHistoryEntryV1.Action })
        // No arbitrarily chosen Spy/Recluse witness can be invented from the observed number.
        val numeric = (prefix.entries.single() as StorytellerProviderHistoryEntryV1.Observation)
            .proposition as InformationProposition.NumericResult
        assertEquals(1, numeric.value)
    }

    @Test
    fun `legacy is explicit unavailable and no local positions become global`() {
        val session = newSession(ClocktowerSemanticHistoryMode.LEGACY_LOCAL)
        val prefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(session.state, revision(session))
        assertEquals(StorytellerProviderHistoryCutoffSourceV1.UNAVAILABLE, prefix.cutoffSource)
        assertEquals(null, prefix.exclusiveGlobalSequence)
        assertTrue(prefix.entries.isEmpty())
        assertTrue(prefix.coverage.values.all {
            it.state == StorytellerProviderHistoryCoverageStateV1.UNRECONSTRUCTABLE
        })
    }

    @Test
    fun `fail closed for mismatched revision and future-at-cutoff entries`() {
        val session = newSession()
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
                session.state, StorytellerProviderRevisionV1(20, 0),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerProviderHistoryPrefixV1.capturedLive(
                gameId = "r1b-game",
                sourceRevision = revision(session),
                exclusiveGlobalSequence = 0L,
                entries = listOf(
                    StorytellerProviderHistoryEntryV1.Action(
                        fact = ActionFact.Death("future", 0L, 2),
                        point = TimelinePoint(StorytellerPhase.DAWN, 1, 0, 0L),
                    ),
                ),
            )
        }
    }
}
