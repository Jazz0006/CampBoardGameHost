package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.setup.*
import com.codex.campboardgamehost.clocktower.session.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w480dp-h960dp-mdpi")
class ClocktowerGameLifecycleTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    // Lifecycle owns observable UI state: use Compose's normal scheduler/cleanup lifecycle.
    @Before fun mountSnapshotOwner() { ui.setContent { } }
    private val context get() = RuntimeEnvironment.getApplication()
    private val catalog get() = BuiltInClocktowerRulesetCatalog.fromContext(context)
    private val names = (1..5).map { "P$it" }
    private fun settings(mode: StorytellerOperationMode) = StorytellerAiSettings(mode).apply {
        directEnabled = true; directLoaded = true; directKey = "unused-test-key"
    }
    private fun create(settings: StorytellerAiSettings, store: ClocktowerActiveGameStore,
        seed: () -> Long = { 34L }, errors: () -> Unit = { fail("unexpected save error") },
        conversation: ClocktowerAiConversation = ClocktowerAiConversation()) =
        ClocktowerGameLifecycle(context, catalog, AppPreferencesState(AppPreferencesStore(context)), settings,
            conversation, seed, errors, store)

    @Test fun `restore keeps active mode independent of default and restart reapplies default`() = runBlocking {
        val store = ClocktowerActiveGameStore(File(context.cacheDir, "lifecycle-mode.json"))
        val settings = settings(StorytellerOperationMode.AI_AUTOMATIC)
        val first = create(settings, store)
        assertTrue(first.beginNewGame(ClocktowerScript.TroubleBrewing))
        val prepared = first.prepareTroubleBrewing(names)
        assertEquals(ClocktowerGameLifecycle.StartResult.STARTED, first.commitTroubleBrewingSetup("mode-game", prepared, null))
        val holder = requireNotNull(first.sessionUi)
        holder.commit { session ->
            session.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT,
                1, session.nextInputSequence, StorytellerPhase.DAY, 1), session.state.gameStateRevision)
        }
        first.takeOverManually()
        val saved = requireNotNull(store.load())
        assertEquals("MANUAL", saved.metadata.getString("mode"))
        assertEquals(StorytellerOperationMode.AI_AUTOMATIC, settings.preferredMode)
        first.dispose()
        val restored = create(settings, store)
        assertEquals(ClocktowerGameLifecycle.RestoreResult.HOST, restored.restore())
        assertEquals(StorytellerOperationMode.MANUAL, restored.operationMode)
        assertEquals(saved.state, restored.sessionUi?.snapshot)
        assertNull(holder.commit { it.recordPlayerInput() })
        val restart = restored.prepareRestart() as ClocktowerGameLifecycle.RestartResult.Ready
        assertEquals(names, restart.players)
        assertEquals(ClocktowerScript.TroubleBrewing, restart.script)
        assertNull(store.load())
        assertTrue(restored.beginNewGame(restart.script))
        assertEquals(StorytellerOperationMode.AI_AUTOMATIC, restored.operationMode)
    }

    @Test fun `restore whose re-save fails adopts no restored mode or AI conversation state`() = runBlocking {
        val file = File(context.cacheDir, "lifecycle-restore-fail.json")
        val blockedWrite = File(file.path + ".new")
        file.delete(); File(file.path + ".bak").delete(); blockedWrite.deleteRecursively()
        val store = ClocktowerActiveGameStore(file)
        val settings = settings(StorytellerOperationMode.AI_AUTOMATIC)
        val firstConversation = ClocktowerAiConversation()
        val first = create(settings, store, conversation = firstConversation)
        assertTrue(first.beginNewGame(ClocktowerScript.TroubleBrewing))
        first.commitTroubleBrewingSetup("restore-fail", first.prepareTroubleBrewing(names), null)
        firstConversation.canonicalConversationResponseId = "response-of-saved-game"
        first.takeOverManually()
        assertEquals("response-of-saved-game", requireNotNull(store.load()).metadata.optString("responseId"))
        first.dispose()
        // A non-empty directory at AtomicFile's temporary path: loading still works, the re-save cannot start.
        File(blockedWrite, "occupied").apply { parentFile?.mkdirs(); writeText("x") }
        try {
            val conversation = ClocktowerAiConversation()
            val restored = create(settings, store, conversation = conversation)
            assertEquals(ClocktowerGameLifecycle.RestoreResult.FAILED, restored.restore())
            assertNull(restored.sessionUi)
            assertEquals(StorytellerOperationMode.AI_AUTOMATIC, restored.operationMode)
            assertNull(conversation.canonicalConversationResponseId)
            assertNull(conversation.strategicLastReviewedHost)
        } finally {
            blockedWrite.deleteRecursively()
        }
    }

    @Test fun `failed manual Drunk setup keeps draft and can retry then restore dealing`() = runBlocking {
        val parent = File(context.cacheDir, "lifecycle-storage-blocker").apply { writeText("not a directory") }
        val store = ClocktowerActiveGameStore(File(parent, "game.json"))
        var errors = 0
        var seed = 0L
        val settings = settings(StorytellerOperationMode.MANUAL)
        val lifecycle = create(settings, store, { ++seed }, { errors++ })
        assertTrue(lifecycle.beginNewGame(ClocktowerScript.TroubleBrewing))
        val players = (1..8).map { "P$it" }
        val prepared = generateSequence { lifecycle.prepareTroubleBrewing(players) }.take(40)
            .first { TroubleBrewingDrunkSelectionRouter.route(it, null) is TroubleBrewingDrunkSelectionRoute.ManualSelection }
        val request = (TroubleBrewingDrunkSelectionRouter.route(prepared, null) as TroubleBrewingDrunkSelectionRoute.ManualSelection).request
        val pending = PendingTroubleBrewingDrunkSelection("drunk-retry", prepared, request)
        val draft = ClocktowerOpeningDraft().apply { pendingTroubleBrewingDrunkSelection = pending }
        assertEquals(ClocktowerOpeningDraft.Result.FAILED, draft.confirmManualDrunk(pending, request.candidates.first(), lifecycle))
        assertSame(pending, draft.pendingTroubleBrewingDrunkSelection)
        assertNull(lifecycle.sessionUi)
        assertEquals(1, errors)
        assertTrue(parent.delete()); assertTrue(parent.mkdir())
        assertEquals(ClocktowerOpeningDraft.Result.DEAL, draft.confirmManualDrunk(pending, request.candidates.first(), lifecycle))
        assertNull(draft.pendingTroubleBrewingDrunkSelection)
        val saved = requireNotNull(store.load())
        val cards = ClocktowerSessionPlayerCards(emptyList(), { lifecycle.sessionUi?.snapshot }, "en")
        val chosen = request.candidates.first()
        val drunk = cards[chosen.seat - 1]
        assertEquals("Drunk", drunk.clocktowerRole?.enName)
        assertEquals(chosen.shownRoleId, drunk.clocktowerShownRole?.enName?.filter(Char::isLetterOrDigit)?.lowercase())
        assertEquals(players, cards.map { it.name })
        lifecycle.dispose()
        val restored = create(settings, store)
        assertEquals(ClocktowerGameLifecycle.RestoreResult.DEAL, restored.restore())
        assertEquals(saved.state, restored.sessionUi?.snapshot)
        assertEquals(players, restored.sessionUi?.snapshot?.gameState?.players?.map { it.name })
    }

    @Test fun `assisted Drunk override and automatic opening return explicit destinations without a provider`() {
        for (mode in listOf(StorytellerOperationMode.AI_ASSISTED, StorytellerOperationMode.AI_AUTOMATIC)) {
            val store = ClocktowerActiveGameStore(File(context.cacheDir, "opening-${mode.name}.json"))
            val settings = settings(mode)
            var seed = 0L
            val conversation = ClocktowerAiConversation()
            val lifecycle = create(settings, store, { ++seed }, conversation = conversation)
            lifecycle.beginNewGame(ClocktowerScript.TroubleBrewing)
            val prepared = generateSequence { lifecycle.prepareTroubleBrewing((1..8).map { "P$it" }) }.take(40)
                .first { TroubleBrewingDrunkSelectionRouter.route(it, null) is TroubleBrewingDrunkSelectionRoute.ManualSelection }
            val legal = (TroubleBrewingDrunkSelectionRouter.route(prepared, null) as TroubleBrewingDrunkSelectionRoute.ManualSelection).request
            val request = InitialOpeningRequestV2.fromHostSnapshot(
                TroubleBrewingGameSnapshotProjector.fromIntermediate("joint-${mode.name}", prepared.intermediateSetup),
                catalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry)
            val pending = PendingTroubleBrewingJointOpening(request.snapshot.gameId, prepared, request, emptyList())
            val recommendation = HostOpeningSemanticAcceptanceV2(OpeningSelectionV2(request.snapshot.gameId,
                request.gameStateRevision, request.playerInputRevision, request.rosterFingerprint,
                legal.candidates.first().seat, null, null, emptyMap()), emptyList(), null,
                tradeoffSummary = "Opening plan rationale")
            val draft = ClocktowerOpeningDraft().apply {
                pendingJointOpening = pending
                jointOpeningResult = recommendation // Already-accepted advice fixture; no network call.
                pendingOpeningResponseId = "fixture-response"
                pendingOpeningModel = "fixture-model"
            }
            val result = draft.beginJointOpeningAtDrunkStep(conversation, catalog, lifecycle)
            val chosen = if (mode == StorytellerOperationMode.AI_ASSISTED) {
                assertEquals(ClocktowerOpeningDraft.Result.DRUNK, result)
                assertNull(lifecycle.sessionUi)
                val candidate = legal.candidates.last()
                assertNotEquals(legal.candidates.first().seat, candidate.seat)
                assertEquals(ClocktowerOpeningDraft.Result.HOST,
                    draft.confirmJointOpeningDrunk(pending, candidate, conversation, catalog, lifecycle))
                assertNull(conversation.canonicalConversationResponseId)
                candidate
            } else {
                assertEquals(ClocktowerOpeningDraft.Result.HOST, result)
                assertEquals("fixture-response", conversation.canonicalConversationResponseId)
                legal.candidates.first()
            }
            assertEquals(RoleId("Drunk"), lifecycle.sessionUi?.snapshot?.gameState?.playerAt(chosen.seat)?.actualRole)
            assertEquals(ClocktowerOpeningDraft.Result.UNCHANGED,
                draft.confirmJointOpeningDrunk(pending, chosen, conversation, catalog, lifecycle))
            assertEquals(mode.name, store.load()?.metadata?.getString("mode"))
            assertEquals(recommendation, ClocktowerActiveGameMetadata.opening(requireNotNull(store.load()).metadata))
            val legacyMetadata = requireNotNull(store.load()).metadata
            legacyMetadata.getJSONObject("opening").remove("tradeoffSummary")
            assertEquals(recommendation.copy(tradeoffSummary = null), ClocktowerActiveGameMetadata.opening(legacyMetadata))
        }
    }

    @Test fun `manual takeover after shown-role dealing skips redeal with and without Drunk`() {
        for (withDrunk in listOf(false, true)) {
            val settings = settings(StorytellerOperationMode.AI_AUTOMATIC)
            val conversation = ClocktowerAiConversation()
            var seed = 0L
            val lifecycle = create(settings, ClocktowerActiveGameStore(File(context.cacheDir, "takeover-$withDrunk.json")),
                { if (withDrunk) ++seed else 34L }, conversation = conversation)
            lifecycle.beginNewGame(ClocktowerScript.TroubleBrewing)
            val players = if (withDrunk) (1..8).map { "P$it" } else names
            val prepared = generateSequence { lifecycle.prepareTroubleBrewing(players) }.take(40).first {
                (TroubleBrewingDrunkSelectionRouter.route(it, null) is TroubleBrewingDrunkSelectionRoute.ManualSelection) == withDrunk
            }
            val request = InitialOpeningRequestV2.fromHostSnapshot(
                TroubleBrewingGameSnapshotProjector.fromIntermediate("takeover-$withDrunk", prepared.intermediateSetup),
                catalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry)
            val draft = ClocktowerOpeningDraft().apply {
                pendingJointOpening = PendingTroubleBrewingJointOpening(request.snapshot.gameId, prepared, request, emptyList())
                jointOpeningBusy = true
                pendingOpeningResponseId = "discarded-response"
            }
            val serial = draft.jointOpeningSerial
            val result = draft.takeOverJointOpeningManually(conversation, lifecycle)
            assertTrue(draft.jointOpeningSerial > serial)
            assertFalse(draft.jointOpeningBusy)
            assertNull(draft.pendingOpeningResponseId)
            assertEquals(StorytellerOperationMode.AI_AUTOMATIC, settings.preferredMode)
            assertEquals(StorytellerOperationMode.MANUAL, lifecycle.operationMode)
            if (withDrunk) {
                assertEquals(ClocktowerOpeningDraft.Result.DRUNK, result)
                assertNull(lifecycle.sessionUi)
                val pending = requireNotNull(draft.pendingTroubleBrewingDrunkSelection)
                assertEquals(ClocktowerOpeningDraft.Result.HOST,
                    draft.confirmManualDrunk(pending, pending.request.candidates.first(), lifecycle))
            } else assertEquals(ClocktowerOpeningDraft.Result.HOST, result)
            assertEquals(request.snapshot.gameId, lifecycle.sessionUi?.snapshot?.gameId)
            assertFalse(draft.shownOnlyDealCompleted)
        }
    }

    @Test fun `unready restart preserves current session and save while NGJ keeps manual boundary`() {
        val store = ClocktowerActiveGameStore(File(context.cacheDir, "lifecycle-unready.json"))
        val settings = settings(StorytellerOperationMode.MANUAL)
        val lifecycle = create(settings, store)
        lifecycle.beginNewGame(ClocktowerScript.TroubleBrewing)
        lifecycle.commitTroubleBrewingSetup("preserved", lifecycle.prepareTroubleBrewing(names), null)
        val holder = lifecycle.sessionUi
        val saved = requireNotNull(store.load())
        settings.preferredMode = StorytellerOperationMode.AI_ASSISTED
        settings.directKey = ""
        assertEquals(ClocktowerGameLifecycle.RestartResult.NotReady, lifecycle.prepareRestart())
        assertSame(holder, lifecycle.sessionUi)
        assertEquals(saved.state, store.load()?.state)
        assertTrue(lifecycle.finish())
        assertTrue(lifecycle.beginNewGame(ClocktowerScript.NoGreaterJoy))
        assertEquals(StorytellerOperationMode.MANUAL, lifecycle.operationMode)
        assertEquals(ClocktowerGameLifecycle.StartResult.STARTED, lifecycle.startNoGreaterJoy(names))
        assertEquals(names, lifecycle.sessionUi?.snapshot?.gameState?.players?.map { it.name })
    }
}
