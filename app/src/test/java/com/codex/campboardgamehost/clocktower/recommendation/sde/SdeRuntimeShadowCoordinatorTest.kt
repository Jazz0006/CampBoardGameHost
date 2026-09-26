package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerPhase
import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.prepareNumericInformationUiModel
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.io.File
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SdeRuntimeShadowCoordinatorTest {
    private val ruleset = BuiltInClocktowerRulesetCatalog { path ->
        File("src/main/assets/$path").readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = ruleset.toRulesetRef("c3-runtime-shadow", "official")
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `runtime wrapper stores only current eligible within-budget diagnostic result`() = runBlocking {
        val scenario = scenario()
        var writes = 0
        val report = SdeRuntimeShadowCoordinator.evaluate(
            replayInput = scenario.input,
            decisionContext = scenario.model.shadowDecisionContext,
            validatedRuleset = ruleset,
            roleDefinitions = roles,
            currentIdentity = { scenario.identity },
            appendTrace = { writes++; true },
            limits = SdeRuntimeShadowLimits(maxElapsedMillis = 10_000),
        )

        assertEquals(SdeRuntimeShadowOutcome.STORED, report.outcome)
        assertEquals(1, writes)
        assertTrue(report.elapsedMillis >= 0)
        println(
            "SDE_C3_MEASURE players=5 history=0 elapsedMs=${report.elapsedMillis} " +
                "heapDeltaBytes=${report.coarseHeapDeltaBytes} outcome=${report.outcome}",
        )
    }

    @Test
    fun `stale over-budget ineligible and storage failures never publish a trace`() = runBlocking {
        val scenario = scenario()
        val evaluation = SdeOfflineReplayCoordinator.evaluate(
            scenario.input, scenario.model.shadowDecisionContext, ruleset, roles,
        )
        suspend fun run(
            identity: SdeRuntimeShadowIdentity = scenario.identity,
            limits: SdeRuntimeShadowLimits = SdeRuntimeShadowLimits(maxElapsedMillis = 10_000),
            write: (DecisionTrace) -> Boolean = { true },
            nanos: Iterator<Long> = listOf(0L, 1L, 2L).iterator(),
        ) = SdeRuntimeShadowCoordinator.evaluate(
            scenario.input, scenario.model.shadowDecisionContext, ruleset, roles,
            currentIdentity = { identity }, appendTrace = write, limits = limits,
            nanoTime = { nanos.next() }, evaluateOffline = { evaluation },
        )

        assertEquals(
            SdeRuntimeShadowOutcome.STALE,
            run(identity = scenario.identity.copy(playerInputRevision = 99)).outcome,
        )
        assertEquals(
            SdeRuntimeShadowOutcome.OVER_BUDGET,
            run(limits = SdeRuntimeShadowLimits(maxElapsedMillis = 0),
                nanos = listOf(0L, 1_000_001L).iterator()).outcome,
        )
        assertEquals(SdeRuntimeShadowOutcome.STORAGE_REJECTED, run(write = { false }).outcome)
        assertEquals(SdeRuntimeShadowOutcome.FAILED, run(write = { error("I/O") }).outcome)

        listOf(6, 8, 12, 15).forEach { playerCount ->
            val tooLarge = scenario.withPlayerNames(
                (1..playerCount).map { "P$it" },
                scenario.setupWithSeats(playerCount),
            )
            var evaluated = false
            val started = System.nanoTime()
            val ineligible = SdeRuntimeShadowCoordinator.evaluate(
                tooLarge, scenario.model.shadowDecisionContext, ruleset, roles,
                currentIdentity = { scenario.identity }, appendTrace = { true },
                evaluateOffline = { evaluated = true; evaluation },
            )
            val admissionMicros = (System.nanoTime() - started) / 1_000L
            assertEquals(SdeRuntimeShadowOutcome.INELIGIBLE, ineligible.outcome)
            assertTrue(!evaluated)
            println(
                "SDE_C3_ADMISSION players=$playerCount history=0 elapsedMicros=$admissionMicros " +
                    "outcome=${ineligible.outcome}",
            )
        }
    }

    @Test
    fun `mismatched request identity is stale before evaluation or trace persistence`() = runBlocking {
        val scenario = scenario()
        var evaluated = false
        var writes = 0
        val mismatchedIdentity = scenario.identity.copy(
            requestIdentity = scenario.identity.requestIdentity.copy(requestId = "different-request"),
        )

        val report = SdeRuntimeShadowCoordinator.evaluate(
            replayInput = scenario.input,
            decisionContext = scenario.model.shadowDecisionContext,
            validatedRuleset = ruleset,
            roleDefinitions = roles,
            currentIdentity = { mismatchedIdentity },
            appendTrace = { writes++; true },
            evaluateOffline = {
                evaluated = true
                error("mismatched request identity must be rejected before evaluation")
            },
        )

        assertEquals(SdeRuntimeShadowOutcome.STALE, report.outcome)
        assertTrue(!evaluated)
        assertEquals(0, writes)
    }

    @Test
    fun `cancellation propagates and prevents diagnostic persistence`() = runBlocking {
        val scenario = scenario()
        var writes = 0
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            SdeRuntimeShadowCoordinator.evaluate(
                scenario.input, scenario.model.shadowDecisionContext, ruleset, roles,
                currentIdentity = { scenario.identity }, appendTrace = { writes++; true },
                evaluateOffline = { awaitCancellation() },
            )
        }
        job.cancelAndJoin()
        assertEquals(0, writes)
    }

    @Test
    fun `unavailable exact capability remains an explicit stored deferral`() = runBlocking {
        val scenario = scenario()
        val ready = SdeOfflineReplayCoordinator.evaluate(
            scenario.input, scenario.model.shadowDecisionContext, ruleset, roles,
        )
        val candidateIds = ready.pendingTrace.legalCandidateIds
        val deferred = ready.copy(
            pendingTrace = ready.pendingTrace.copy(
                featureEvaluation = DecisionFeatureEvaluation.Deferred(
                    candidateIds,
                    setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
                ),
                policySnapshot = DecisionTracePolicySnapshot.Deferred(
                    PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                    candidateIds,
                    setOf(PolicyDeferralCode("exact-history-unavailable")),
                ),
                policySelection = null,
            ),
        )
        var writes = 0

        val report = SdeRuntimeShadowCoordinator.evaluate(
            scenario.input, scenario.model.shadowDecisionContext, ruleset, roles,
            currentIdentity = { scenario.identity }, appendTrace = { writes++; true },
            evaluateOffline = { deferred },
        )

        assertEquals(SdeRuntimeShadowOutcome.STORED_DEFERRED, report.outcome)
        assertEquals(1, writes)
    }

    private fun scenario(): Scenario {
        val snapshot = A4RuntimeFixtures.snapshot().copy(
            rulesetRef = rulesetRef,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val setup = CommittedClocktowerSetup(
            snapshot.gameState.script, snapshot.gameSeed,
            snapshot.gameState.players.map { CommittedSetupSeat(it.seat, it.actualRole, it.shownRole ?: it.actualRole) },
            SetupProvenance(SetupSourceKind.GENERATED, "c3-test"),
        )
        val input = SdeHistoricalReplayInputFactory.captureFresh(setup, snapshot).input
        val empath = snapshot.gameState.players.single { it.actualRole.value == "Empath" }
        val count = snapshot.gameState.players.size
        val neighbours = listOf(if (empath.seat == 1) count else empath.seat - 1, if (empath.seat == count) 1 else empath.seat + 1)
        val revision = InformationDecisionRevision(snapshot.gameStateRevision, snapshot.playerInputRevision)
        val model = prepareNumericInformationUiModel(
            ClocktowerRecommendationCoordinator(), snapshot.gameId, ClocktowerPhase.FirstNight, 1, 1,
            empath.seat, RoleId("Empath"), com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.LIVING_EVIL_NEIGHBOURS,
            neighbours, 0, 0, 2, InformationReliability.RELIABLE, RecommendationStyle.BALANCED,
            revision, recommendedValue = 0,
        )
        return Scenario(
            input,
            model,
            SdeRuntimeShadowIdentity.from(input, model.shadowDecisionContext.requestIdentity),
        )
    }

    private data class Scenario(
        val input: SdeHistoricalReplayInput,
        val model: com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel,
        val identity: SdeRuntimeShadowIdentity,
    ) {
        fun setupWithSeats(playerCount: Int) = CommittedClocktowerSetup(
            input.committedSetup.script, input.committedSetup.setupSeed,
            (1..playerCount).map { seat ->
                input.committedSetup.assignments.getOrNull(seat - 1)
                    ?: CommittedSetupSeat(seat, RoleId("Chef"), RoleId("Chef"))
            },
            input.committedSetup.provenance,
        )

        fun withPlayerNames(
            names: List<String>,
            setup: CommittedClocktowerSetup,
        ) = SdeHistoricalReplayInput(
            gameId = input.gameId, gameStateRevision = input.gameStateRevision,
            playerInputRevision = input.playerInputRevision, rulesetRef = input.rulesetRef,
            committedSetup = setup, playerNamesBySeat = names, actionTimeline = input.actionTimeline,
            observationLog = input.observationLog, nextTimelineGlobalSequence = input.nextTimelineGlobalSequence,
        )
    }
}
