package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ClocktowerSessionUiStateTest {
    private fun session() = TroubleBrewingFixtures.eightPlayerExample().let {
        ClocktowerGameSession.createProduction("ui-state", it.seed, it, ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }

    @Test fun `failed atomic save never publishes intermediate death and retry updates cards`() {
        val session = session()
        var errors = 0
        val holder = ClocktowerSessionUiState(session) { errors++ }
        val before = holder.snapshot
        val cards = ClocktowerSessionPlayerCards(emptyList(), { holder.snapshot }, "en")
        var fail = false
        session.persistWith {
            assertSame(before, holder.snapshot)
            assertNull(cards[0].eliminatedRound)
            if (fail) throw IOException("storage unavailable")
        }
        fail = true
        fun confirm() = holder.commit { it.confirmAtomically {
            it.commitConfirmedPlayerDeath(ActionFactDraft.Execution("ui-death", StorytellerPhase.DAY, 2, 1, 1),
                it.state.gameStateRevision)
            true
        } }
        assertNull(confirm())
        assertEquals(1, errors)
        assertSame(before, holder.snapshot)
        assertSame(before, session.state)
        fail = false
        assertEquals(true, confirm())
        assertSame(session.state, holder.snapshot)
        assertEquals(2, cards[0].eliminatedRound)
    }

    @Test fun `consecutive commits preserve false results and publish each accepted revision`() {
        val session = session()
        val holder = ClocktowerSessionUiState(session) { fail("unexpected save error") }
        val revision = holder.snapshot.playerInputRevision
        assertEquals(false, holder.commit { it.recordPlayerInput(); false })
        assertEquals(revision + 1, holder.snapshot.playerInputRevision)
        assertEquals(true, holder.commit { it.recordPlayerInput(); true })
        assertEquals(revision + 2, holder.snapshot.playerInputRevision)
        assertSame(session.state, holder.snapshot)
    }

    @Test fun `late callback from replaced instance cannot run or overwrite restored game`() = runBlocking {
        val old = session()
        var errors = 0
        val oldHolder = ClocktowerSessionUiState(old) { errors++ }
        val before = old.state
        // Same persisted game ID is intentional: instance lifetime, not ID, is the boundary.
        val restored = ClocktowerGameSession.restore(ClocktowerSessionJson.decode(ClocktowerSessionJson.encode(before)))
        val restoredHolder = ClocktowerSessionUiState(restored) { errors++ }
        val response = CompletableDeferred<Unit>()
        var called = false
        val delayed = launch(start = CoroutineStart.UNDISPATCHED) {
            response.await()
            assertNull(oldHolder.commit { called = true; it.recordPlayerInput() })
        }
        oldHolder.deactivate()
        restoredHolder.commit { it.recordPlayerInput() }
        val accepted = restoredHolder.snapshot
        response.complete(Unit)
        delayed.join()
        assertFalse(called)
        assertSame(before, old.state)
        assertSame(before, oldHolder.snapshot)
        assertSame(accepted, restoredHolder.snapshot)
        assertSame(accepted, restored.state)
        assertEquals(0, errors)
    }
}
