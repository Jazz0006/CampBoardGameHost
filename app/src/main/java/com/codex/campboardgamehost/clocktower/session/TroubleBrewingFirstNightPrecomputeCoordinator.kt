package com.codex.campboardgamehost.clocktower.session

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

internal enum class TroubleBrewingFirstNightPrecomputeStatus {
    MISS,
    BUSY,
    READY,
}

/**
 * Exact-input lifecycle owner for Trouble Brewing First Night precomputation.
 *
 * Dispatch policy is intentionally owned by the caller: reveal wiring supplies an
 * off-main launcher, while a point-of-use MISS invokes [resultFor] from an existing
 * safe background context. This owner only tracks exact request identity, BUSY await,
 * READY reuse, failure fallback, cancellation, and stale-result rejection.
 */
internal class TroubleBrewingFirstNightPrecomputeCoordinator<Request : Any, Result : Any>(
    private val build: (Request) -> Result,
) {
    private class Entry<Request : Any, Result : Any>(
        val request: Request,
        val completion: CompletableDeferred<Result> = CompletableDeferred(),
    ) {
        var readyResult: Result? = null
        var failed: Boolean = false
    }

    private val lock = Any()
    private var current: Entry<Request, Result>? = null

    fun prewarm(
        request: Request,
        launchBackground: ((() -> Unit) -> Unit),
    ) {
        val entry = synchronized(lock) {
            current?.takeIf { it.request == request }?.let { return }
            Entry<Request, Result>(request).also { current = it }
        }
        launchBackground {
            try {
                val result = build(request)
                synchronized(lock) {
                    if (current === entry) {
                        entry.readyResult = result
                    }
                }
                entry.completion.complete(result)
            } catch (error: Throwable) {
                synchronized(lock) {
                    if (current === entry) {
                        entry.failed = true
                    }
                }
                entry.completion.completeExceptionally(error)
            }
        }
    }

    fun statusFor(request: Request): TroubleBrewingFirstNightPrecomputeStatus = synchronized(lock) {
        val entry = current?.takeIf { it.request == request }
            ?: return@synchronized TroubleBrewingFirstNightPrecomputeStatus.MISS
        when {
            entry.readyResult != null -> TroubleBrewingFirstNightPrecomputeStatus.READY
            entry.failed -> TroubleBrewingFirstNightPrecomputeStatus.MISS
            else -> TroubleBrewingFirstNightPrecomputeStatus.BUSY
        }
    }

    fun readyFor(request: Request): Result? = synchronized(lock) {
        current
            ?.takeIf { it.request == request && !it.failed }
            ?.readyResult
    }

    suspend fun resultFor(request: Request): Result {
        val exact = synchronized(lock) {
            current?.takeIf { it.request == request && !it.failed }
        }
        if (exact != null) {
            exact.readyResult?.let { return it }
            try {
                return exact.completion.await()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                // Only a genuine failed reveal computation falls through to a fresh
                // exact build. Caller context owns the point-of-use background boundary.
            }
        }
        return buildFallback(request)
    }

    private fun buildFallback(request: Request): Result {
        val entry = Entry<Request, Result>(request)
        synchronized(lock) {
            current = entry
        }
        return try {
            val result = build(request)
            synchronized(lock) {
                if (current === entry) {
                    entry.readyResult = result
                }
            }
            entry.completion.complete(result)
            result
        } catch (error: Throwable) {
            synchronized(lock) {
                if (current === entry) {
                    entry.failed = true
                }
            }
            entry.completion.completeExceptionally(error)
            throw error
        }
    }
}
