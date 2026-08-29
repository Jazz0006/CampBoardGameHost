package com.codex.campboardgamehost.clocktower.rules

/**
 * Canonical identity authority for the currently acting Demon.
 *
 * Historical Demon identities remain durable. Current action authority is therefore the unique
 * living Demon, not the first historical player who still carries a Demon role. Ambiguous live
 * states fail closed instead of silently choosing by list order.
 */
internal object CurrentDemonAuthority {
    fun <T> resolveLive(
        candidates: List<T>,
        isAlive: (T) -> Boolean,
        isDemon: (T) -> Boolean,
    ): T? = candidates.singleOrNull { candidate ->
        isAlive(candidate) && isDemon(candidate)
    }
}
