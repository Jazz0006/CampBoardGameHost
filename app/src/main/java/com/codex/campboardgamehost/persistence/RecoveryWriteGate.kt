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
    private var lastDurableContentIdentity: String? = null
    private var retryRequired: Boolean = false

    fun persist(
        snapshot: RecoverySnapshot,
        force: Boolean = false,
        write: (RecoverySnapshot) -> Boolean,
    ): Boolean {
        val contentIdentity = snapshot.durableContentIdentity()
        if (!force && !retryRequired && contentIdentity == lastDurableContentIdentity) return true

        if (!write(snapshot)) {
            retryRequired = true
            return false
        }
        lastDurableContentIdentity = contentIdentity
        retryRequired = false
        return true
    }

    fun clear() {
        lastDurableContentIdentity = null
        retryRequired = false
    }
}

private fun RecoverySnapshot.durableContentIdentity(): String =
    RecoverySnapshotJsonCodec.encode(copy(savedAtMillis = 0L)).toString()
