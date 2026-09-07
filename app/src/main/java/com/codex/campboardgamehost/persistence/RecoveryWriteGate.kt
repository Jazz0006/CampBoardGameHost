package com.codex.campboardgamehost

/**
 * Suppresses duplicate physical Recovery writes while preserving durability semantics.
 *
 * [RecoverySnapshot.savedAtMillis] is freshness metadata rather than game content, so ordinary
 * persistence attempts may reuse the last successful durable result when only that timestamp has
 * changed. Failed writes are never remembered as durable. Callers may [persist] with [force] for
 * lifecycle last-chance checkpoints that intentionally refresh the Recovery timestamp.
 */
internal class RecoveryWriteGate {
    private var lastDurableContent: RecoverySnapshot? = null

    fun persist(
        snapshot: RecoverySnapshot,
        force: Boolean = false,
        write: (RecoverySnapshot) -> Boolean,
    ): Boolean {
        val content = snapshot.withoutSaveTimestamp()
        if (!force && content == lastDurableContent) return true

        if (!write(snapshot)) return false
        lastDurableContent = content
        return true
    }

    fun clear() {
        lastDurableContent = null
    }
}

private fun RecoverySnapshot.withoutSaveTimestamp(): RecoverySnapshot = copy(savedAtMillis = 0L)
