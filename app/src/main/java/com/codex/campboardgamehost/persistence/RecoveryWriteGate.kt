package com.codex.campboardgamehost

/**
 * Suppresses duplicate physical Recovery writes while preserving durability semantics.
 *
 * [RecoverySnapshot.savedAtMillis] is freshness metadata rather than game content, so ordinary
 * persistence attempts may reuse the last successful durable result when only that timestamp has
 * changed. Any failed physical write requires another physical attempt before duplicate suppression
 * may resume. Callers may [persist] with [force] for lifecycle last-chance checkpoints that
 * intentionally refresh the Recovery timestamp.
 */
internal class RecoveryWriteGate {
    private var lastDurableContent: RecoverySnapshot? = null
    private var retryRequired: Boolean = false

    fun persist(
        snapshot: RecoverySnapshot,
        force: Boolean = false,
        write: (RecoverySnapshot) -> Boolean,
    ): Boolean {
        val content = snapshot.withoutSaveTimestamp()
        if (!force && !retryRequired && content == lastDurableContent) return true

        if (!write(snapshot)) {
            retryRequired = true
            return false
        }
        lastDurableContent = content
        retryRequired = false
        return true
    }

    fun clear() {
        lastDurableContent = null
        retryRequired = false
    }
}

private fun RecoverySnapshot.withoutSaveTimestamp(): RecoverySnapshot = copy(savedAtMillis = 0L)
