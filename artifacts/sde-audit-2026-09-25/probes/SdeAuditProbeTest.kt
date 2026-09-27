package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerPhase
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.prepareNumericInformationUiModel
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.*
import java.io.File
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.*

class SdeAuditProbeTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { File("src/main/assets/$it").readText() }
        .ruleset(ClocktowerScript.TroubleBrewing)
    private val ref = ruleset.toRulesetRef("audit", "official")
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private fun fixture() = A4RuntimeFixtures.snapshot().copy(
        rulesetRef = ref, semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
    private fun setup(snapshot: GameSnapshot) = CommittedClocktowerSetup(
        snapshot.gameState.script, snapshot.gameSeed,
        snapshot.gameState.players.map { CommittedSetupSeat(it.seat, it.actualRole, it.shownRole ?: it.actualRole) },
        SetupProvenance(SetupSourceKind.GENERATED, "audit"),
    )
    private fun model(snapshot: GameSnapshot, gameId: String = snapshot.gameId,
                      round: Int = 1, reliability: InformationReliability = InformationReliability.RELIABLE) =
        prepareNumericInformationUiModel(
            ClocktowerRecommendationCoordinator(), gameId,
            if (round == 1) ClocktowerPhase.FirstNight else ClocktowerPhase.Night, round, 10,
            2, RoleId("Empath"), NumericMetric.LIVING_EVIL_NEIGHBOURS, listOf(1, 3),
            0, 0, 2, reliability, RecommendationStyle.BALANCED,
            InformationDecisionRevision(snapshot.gameStateRevision, snapshot.playerInputRevision), 0,
        )

    @Test fun `replay rejects a decision context from another game`() {
        val snapshot = fixture()
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup(snapshot), snapshot).input
        val foreign = model(snapshot, gameId = "different-game")
        val result = runCatching {
            SdeOfflineReplayCoordinator.evaluate(input, foreign.shadowDecisionContext, ruleset, roles)
        }
        println("AUDIT foreign context accepted=${result.isSuccess}; key=${result.getOrNull()?.pendingTrace?.archiveKey}")
        assertTrue("Replay must reject foreign game identity before deriving a trace", result.isFailure)
    }

    @Test fun `strict replay decoder rejects unknown action semantics`() {
        val initial = fixture()
        val session = ClocktowerGameSession.create(initial.gameId, initial.gameSeed, ref, initial.gameState,
            ClocktowerSemanticHistoryMode.GLOBAL_V1)
        session.commitGlobalActionFact(ActionFactDraft.Poison("poison", StorytellerPhase.FIRST_NIGHT, 1, 0, 2))
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup(initial), session.toGameSnapshot(ref)).input
        val raw = JSONObject(SdeHistoricalReplayInputJsonCodec.encode(input))
        raw.getJSONArray("actionTimeline").getJSONObject(0).getJSONObject("fact")
            .put("futurePoisonSemantics", "must-not-be-ignored")
        val result = runCatching { SdeHistoricalReplayInputJsonCodec.decodeStrict(raw.toString()) }
        println("AUDIT unknown nested action accepted=${result.isSuccess}")
        assertTrue("Strict replay input must reject unrecognized nested semantics", result.isFailure)
    }

    @Test fun `strict replay decoder rejects fractional information values`() {
        val initial = fixture()
        val session = ClocktowerGameSession.create(initial.gameId, initial.gameSeed, ref, initial.gameState,
            ClocktowerSemanticHistoryMode.GLOBAL_V1)
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            "numeric", StorytellerPhase.FIRST_NIGHT, 1, 1, 2, RoleId("Empath"),
            ObservationVisibility.PRIVATE, setOf(2), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 1),
        ))
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup(initial), session.toGameSnapshot(ref)).input
        val raw = JSONObject(SdeHistoricalReplayInputJsonCodec.encode(input))
        raw.getJSONArray("observations").getJSONObject(0).getJSONObject("proposition").put("value", 1.9)
        val result = runCatching { SdeHistoricalReplayInputJsonCodec.decodeStrict(raw.toString()) }
        val decoded = result.getOrNull()?.input?.observationLog?.records?.single()?.proposition
        println("AUDIT fractional observation accepted=${result.isSuccess}; decoded=$decoded")
        assertTrue("Strict replay input must not silently truncate information values", result.isFailure)
    }

    @Test fun `drunk changing from zero to two without state change exposes narrative break`() {
        val original = fixture()
        val snapshot = original.copy(gameState = original.gameState.copy(players = original.gameState.players.map {
            when (it.seat) {
                2 -> it.copy(actualRole = RoleId("Drunk"), shownRole = RoleId("Empath"), actualType = CharacterType.OUTSIDER)
                3 -> it.copy(actualRole = RoleId("Saint"), shownRole = RoleId("Saint"), actualType = CharacterType.OUTSIDER)
                4 -> it.copy(actualRole = RoleId("Baron"), shownRole = RoleId("Baron"))
                else -> it
            }
        }))
        val session = ClocktowerGameSession.create(snapshot.gameId, snapshot.gameSeed, ref, snapshot.gameState,
            ClocktowerSemanticHistoryMode.GLOBAL_V1)
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            "night-one-zero", StorytellerPhase.FIRST_NIGHT, 1, 10, 2, RoleId("Empath"),
            ObservationVisibility.PRIVATE, setOf(2), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 0),
        ))
        session.commitGlobalActionFact(ActionFactDraft.PhaseAdvance(
            "next-night", StorytellerPhase.FIRST_NIGHT, 1, 11, StorytellerPhase.NIGHT, 2,
        ))
        val current = session.toGameSnapshot(ref)
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup(snapshot), current).input
        val model = model(current, round = 2, reliability = InformationReliability.DRUNK)
        val zeroId = model.choices.single { it.value == 0 }.candidateId
        val twoId = model.choices.single { it.value == 2 }.candidateId
        val mechanical = SdeOfflineReplayCoordinator.evaluate(input, model.shadowDecisionContext, ruleset, roles)
        val functioning = SdeOfflineReplayCoordinator.evaluate(input, model.shadowDecisionContext, ruleset, roles,
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY)
        val feature = (mechanical.shadow.featureEvaluation as DecisionFeatureEvaluation.Ready).candidates
            .single { it.candidateId == twoId }.features.impairedNarrative as FeatureProjection.Projected
        val strictConsequences = (functioning.shadow.consequences as ExactConsequenceEvaluation.Ready).consequences
        val unchanged = strictConsequences.single { it.candidateId == zeroId }.diagnostics
        val changed = strictConsequences.single { it.candidateId == twoId }.diagnostics
        println("AUDIT narrative=${feature.value}; functioning 0=${unchanged.after.value} 2=${changed.after.value}")
        assertTrue("Unchanged narrative has functioning explanations", unchanged.after.value.signum() > 0)
        assertEquals("Changed narrative has no functioning explanations", 0, changed.after.value.signum())
        assertEquals(ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE, feature.value.relation)
    }

    @Test fun `elapsed budget includes diagnostic persistence`() = runBlocking {
        val snapshot = fixture()
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup(snapshot), snapshot).input
        val model = model(snapshot)
        val ready = SdeOfflineReplayCoordinator.evaluate(input, model.shadowDecisionContext, ruleset, roles)
        var clock = 0L
        val report = SdeRuntimeShadowCoordinator.evaluate(input, model.shadowDecisionContext, ruleset, roles,
            currentIdentity = { SdeRuntimeShadowIdentity.from(input) },
            appendTrace = { clock += 2_000_000_000L; true },
            nanoTime = { clock }, evaluateOffline = { ready })
        println("AUDIT persistence clockMs=${clock / 1_000_000} report=${report}")
        assertTrue("Elapsed metrics must account for diagnostic persistence", report.elapsedMillis >= 2000)
    }
}
