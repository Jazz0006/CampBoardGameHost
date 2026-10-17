package com.codex.campboardgamehost.clocktower.epistemic

/**
 * Synchronous publication guard for A4 shadow work.
 *
 * Revision supersession invalidates the current game's cache generation and cancels any observation
 * rebuild request immediately, while keeping a same-session observation pending until the UI
 * transaction completes. A session boundary additionally clears that pending observation so it
 * cannot cross leave/restart boundaries.
 */
class A4ShadowLifecycleInvalidator(
    private val invalidateGame: (String) -> Unit,
    private val clearPendingObservation: () -> Unit,
    private val cancelObservationRebuild: () -> Unit,
) {
    fun revisionSuperseded(gameId: String) {
        if (gameId.isNotBlank()) invalidateGame(gameId)
        cancelObservationRebuild()
    }

    fun sessionBoundary(gameId: String) {
        if (gameId.isNotBlank()) invalidateGame(gameId)
        clearPendingObservation()
        cancelObservationRebuild()
    }
}
