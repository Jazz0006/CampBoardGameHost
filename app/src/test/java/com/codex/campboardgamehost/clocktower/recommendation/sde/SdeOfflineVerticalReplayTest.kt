package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerPhase
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.prepareNumericInformationUiModel
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SdeOfflineVerticalReplayTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { path ->
        File("src/main/assets/$path").readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = ruleset.toRulesetRef("c2-offline-vertical", "official")
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `offline replay rejects a different game even when revisions match`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val setup = CommittedClocktowerSetup(
            fixture.gameState.script,
            fixture.gameSeed,
            fixture.gameState.players.map {
                CommittedSetupSeat(it.seat, it.actualRole, it.shownRole ?: it.actualRole)
            },
            SetupProvenance(SetupSourceKind.GENERATED, "cr-b-cross-game"),
        )
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup, fixture.copy(
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )).input
        val empath = fixture.gameState.players.single { it.actualRole.value == "Empath" }
        val count = fixture.gameState.players.size
        val neighbours = listOf(
            if (empath.seat == 1) count else empath.seat - 1,
            if (empath.seat == count) 1 else empath.seat + 1,
        )
        val revision = InformationDecisionRevision(input.gameStateRevision, input.playerInputRevision)
        val model = prepareNumericInformationUiModel(
            ClocktowerRecommendationCoordinator(), input.gameId, ClocktowerPhase.FirstNight, 1, 1,
            empath.seat, RoleId("Empath"), NumericMetric.LIVING_EVIL_NEIGHBOURS, neighbours,
            0, 0, 2, InformationReliability.RELIABLE, RecommendationStyle.BALANCED,
            revision, recommendedValue = 0,
        )
        val foreignInput = SdeHistoricalReplayInput(
            schemaVersion = input.schemaVersion,
            gameId = "different-game",
            gameStateRevision = input.gameStateRevision,
            playerInputRevision = input.playerInputRevision,
            rulesetRef = input.rulesetRef,
            committedSetup = input.committedSetup,
            playerNamesBySeat = input.playerNamesBySeat,
            actionTimeline = input.actionTimeline,
            observationLog = input.observationLog,
            nextTimelineGlobalSequence = input.nextTimelineGlobalSequence,
        )

        val failure = runCatching {
            SdeOfflineReplayCoordinator.evaluate(
                foreignInput,
                model.shadowDecisionContext,
                ruleset,
                roles,
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertTrue(failure?.message?.contains("different game") == true)
    }

    @Test
    fun `durable input drives real numeric trace commit reload and replay`() {
        val fixture = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val session = ClocktowerGameSession.create(
            fixture.gameId, fixture.gameSeed, rulesetRef, fixture.gameState,
            ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val empath = fixture.gameState.players.single { it.actualRole.value == "Empath" }
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            "shown-role:${empath.seat}", StorytellerPhase.FIRST_NIGHT, 1, 0, null, null,
            ObservationVisibility.PRIVATE, setOf(empath.seat), ObservationReliability.NOT_ABILITY_INFORMATION,
            InformationProposition.ShownRoleAt(empath.seat, empath.shownRole ?: empath.actualRole),
        ))
        session.commitGlobalActionFact(ActionFactDraft.PhaseAdvance(
            "phase-night-2", StorytellerPhase.FIRST_NIGHT, 1, 1, StorytellerPhase.NIGHT, 2,
        ))
        session.commitGlobalActionFact(ActionFactDraft.Poison(
            "poison-empath", StorytellerPhase.NIGHT, 2, 0, empath.seat,
        ))
        val snapshot = session.toGameSnapshot(rulesetRef)
        val setup = CommittedClocktowerSetup(
            snapshot.gameState.script, snapshot.gameSeed,
            snapshot.gameState.players.map {
                CommittedSetupSeat(it.seat, it.actualRole, it.shownRole ?: it.actualRole)
            },
            SetupProvenance(SetupSourceKind.GENERATED, "c2-test"),
        )
        val durableRaw = SdeHistoricalReplayInputJsonCodec.encode(
            SdeHistoricalReplayInputFactory.captureFresh(setup, snapshot).input,
        )
        val restoredInput = SdeHistoricalReplayInputJsonCodec.decodeStrict(durableRaw).input
        val revision = InformationDecisionRevision(snapshot.gameStateRevision, snapshot.playerInputRevision)
        val count = snapshot.gameState.players.size
        val neighbours = listOf(if (empath.seat == 1) count else empath.seat - 1, if (empath.seat == count) 1 else empath.seat + 1)
        val truth = neighbours.count { snapshot.gameState.playerAt(it)?.actualAlignment == Alignment.EVIL }
        val model = prepareNumericInformationUiModel(
            ClocktowerRecommendationCoordinator(), snapshot.gameId, ClocktowerPhase.Night, 2, 1,
            empath.seat, RoleId("Empath"), NumericMetric.LIVING_EVIL_NEIGHBOURS, neighbours,
            truth, 0, 2, InformationReliability.POISONED, RecommendationStyle.BALANCED,
            revision, recommendedValue = 0,
        )

        val heapBefore = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val started = System.nanoTime()
        val first = SdeOfflineReplayCoordinator.evaluate(
            restoredInput, model.shadowDecisionContext, ruleset, roles,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        )
        val elapsedMillis = (System.nanoTime() - started) / 1_000_000L
        val heapDelta = Runtime.getRuntime().let { runtime ->
            (runtime.totalMemory() - runtime.freeMemory() - heapBefore).coerceAtLeast(0L)
        }
        println(
            "SDE_C3_MEASURE players=${snapshot.gameState.players.size} history=3 " +
                "elapsedMs=$elapsedMillis heapDeltaBytes=$heapDelta outcome=READY",
        )
        var archiveRaw: String? = null
        val store = DecisionTraceArchiveStore({ archiveRaw }, { archiveRaw = it; true })
        assertTrue(store.append(first.pendingTrace))

        val manual = model.choices.first { !it.recommended }
        val confirmed = requireNotNull(model.chooseManually(manual.candidateId, revision).confirmed)
        val committed = session.commitGlobalEpistemicObservation(confirmed.draft)
        assertTrue(store.correlateCommittedChoice(
            first.pendingTrace.archiveKey, confirmed, committed, session.view,
            DecisionTraceOverrideReason(code = "c2-manual-proof"),
        ))
        val source = requireNotNull(store.load().find(first.pendingTrace.archiveKey))
        assertTrue(source.actualChoice is DecisionTraceActualChoice.Committed)

        val second = SdeOfflineReplayCoordinator.evaluate(
            SdeHistoricalReplayInputJsonCodec.decodeStrict(durableRaw).input,
            model.shadowDecisionContext, ruleset, roles,
        )
        assertEquals(first.recomputedInput, second.recomputedInput)
        assertEquals(first.pendingTrace.copy(actualChoice = source.actualChoice),
            MultiPolicyReplayEngine.replay(
                source, second.recomputedInput,
                listOf(PolicyVersions.BEGINNER_CONSERVATIVE_V1),
            ).single())
        val actual = source.actualChoice as DecisionTraceActualChoice.Committed
        assertEquals(manual.candidateId, actual.candidateId)
        assertTrue(actual.manualOverride)
        assertNotNull(actual.overrideReason)
    }
}
