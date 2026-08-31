package com.codex.campboardgamehost.clocktower.session

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TroubleBrewingFirstNightPrecomputeCoordinatorTest {
    @Test fun `reveal prewarm queues expensive work without building synchronously`() {
        var buildCalls = 0
        val queued = mutableListOf<() -> Unit>()
        val coordinator = TroubleBrewingFirstNightPrecomputeCoordinator<String, String> { request ->
            buildCalls += 1
            "result:$request"
        }

        coordinator.prewarm("committed-game-v1") { work -> queued += work }

        assertEquals(0, buildCalls)
        assertEquals(1, queued.size)
        assertEquals(TroubleBrewingFirstNightPrecomputeStatus.BUSY, coordinator.statusFor("committed-game-v1"))
        assertNull(coordinator.readyFor("committed-game-v1"))
    }

    @Test fun `exact busy consumer awaits queued work then exact ready result is reused`() = runBlocking {
        var buildCalls = 0
        val queued = mutableListOf<() -> Unit>()
        val coordinator = TroubleBrewingFirstNightPrecomputeCoordinator<String, String> { request ->
            buildCalls += 1
            "result:$request"
        }
        coordinator.prewarm("committed-game-v1") { work -> queued += work }

        val waiting = async(start = CoroutineStart.UNDISPATCHED) {
            coordinator.resultFor("committed-game-v1")
        }
        assertFalse(waiting.isCompleted)

        queued.single().invoke()

        assertEquals("result:committed-game-v1", waiting.await())
        assertEquals(1, buildCalls)
        assertEquals(TroubleBrewingFirstNightPrecomputeStatus.READY, coordinator.statusFor("committed-game-v1"))
        assertEquals("result:committed-game-v1", coordinator.readyFor("committed-game-v1"))
        assertEquals("result:committed-game-v1", coordinator.resultFor("committed-game-v1"))
        assertEquals(1, buildCalls)
    }

    @Test fun `miss recomputes exact requested input and stale background cannot overwrite it`() = runBlocking {
        val built = mutableListOf<String>()
        val queued = mutableListOf<() -> Unit>()
        val coordinator = TroubleBrewingFirstNightPrecomputeCoordinator<String, String> { request ->
            built += request
            "result:$request"
        }
        coordinator.prewarm("old-game") { work -> queued += work }

        assertEquals("result:current-game", coordinator.resultFor("current-game"))
        assertEquals(listOf("current-game"), built)
        assertEquals("result:current-game", coordinator.readyFor("current-game"))
        assertEquals(TroubleBrewingFirstNightPrecomputeStatus.MISS, coordinator.statusFor("old-game"))

        queued.single().invoke()

        assertEquals(listOf("current-game", "old-game"), built)
        assertEquals("result:current-game", coordinator.readyFor("current-game"))
        assertNull(coordinator.readyFor("old-game"))
        assertEquals(TroubleBrewingFirstNightPrecomputeStatus.READY, coordinator.statusFor("current-game"))
    }

    @Test fun `cancelled busy consumer propagates cancellation without fallback recompute`() = runBlocking {
        var buildCalls = 0
        val queued = mutableListOf<() -> Unit>()
        val coordinator = TroubleBrewingFirstNightPrecomputeCoordinator<String, String> { request ->
            buildCalls += 1
            "result:$request"
        }
        coordinator.prewarm("committed-game-v1") { work -> queued += work }

        val waiting = async(start = CoroutineStart.UNDISPATCHED) {
            coordinator.resultFor("committed-game-v1")
        }
        assertFalse(waiting.isCompleted)

        waiting.cancel()
        waiting.join()

        assertTrue(waiting.isCancelled)
        assertEquals(0, buildCalls)
        assertEquals(TroubleBrewingFirstNightPrecomputeStatus.BUSY, coordinator.statusFor("committed-game-v1"))
        assertEquals(1, queued.size)
    }
}
