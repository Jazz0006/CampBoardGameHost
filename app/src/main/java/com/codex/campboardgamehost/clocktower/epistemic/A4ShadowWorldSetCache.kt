package com.codex.campboardgamehost.clocktower.epistemic

/** JVM-only shadow cache. A generation governs writes; a cache key identifies an immutable value. */
class A4ShadowWorldSetCache {
    private val lock = Any()
    private var nextGenerationId = 0L
    private val values = linkedMapOf<A4IdentityRevealPrewarmCacheKey, PlayerWorldSet>()
    private val currentByGame = mutableMapOf<String, A4ShadowCacheGeneration>()

    fun read(key: A4IdentityRevealPrewarmCacheKey): PlayerWorldSet? = synchronized(lock) { values[key] }

    fun beginVersion(scope: A4ShadowCacheScope): A4ShadowCacheGeneration = synchronized(lock) {
        if (currentByGame.keys.any { it != scope.gameId }) {
            values.keys.removeAll { it.gameId != scope.gameId }
            currentByGame.keys.removeAll { it != scope.gameId }
        }
        A4ShadowCacheGeneration(++nextGenerationId, scope).also { currentByGame[scope.gameId] = it }
    }

    /** Atomic current-generation check and write. */
    fun commitIfCurrent(
        generation: A4ShadowCacheGeneration,
        key: A4IdentityRevealPrewarmCacheKey,
        value: PlayerWorldSet,
    ): Boolean = synchronized(lock) {
        if (generation.scope.gameId != key.gameId || currentByGame[key.gameId] != generation) return false
        values[key] = value
        true
    }

    fun cancel(generation: A4ShadowCacheGeneration) = synchronized(lock) {
        if (currentByGame[generation.scope.gameId] == generation) currentByGame.remove(generation.scope.gameId)
    }

    fun isCurrent(generation: A4ShadowCacheGeneration): Boolean = synchronized(lock) {
        currentByGame[generation.scope.gameId] == generation
    }

    fun invalidateGame(gameId: String) = synchronized(lock) {
        values.keys.removeAll { it.gameId == gameId }
        currentByGame.remove(gameId)
    }
}

data class A4ShadowCacheScope(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val formalSnapshotId: String,
    val rollout: A4WorldEngineRollout,
)

class A4ShadowCacheGeneration internal constructor(
    internal val id: Long,
    internal val scope: A4ShadowCacheScope,
)
