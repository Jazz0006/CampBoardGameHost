package com.codex.campboardgamehost.clocktower.epistemic

/**
 * Releases one pending observation ID only after the active-game snapshot containing it was
 * durably committed. The newest ID is sufficient because every rebuild request carries the
 * complete durable observation log.
 */
class A4ObservationDurabilityGate {
    private var pendingRecordId: String? = null

    fun markPending(recordId: String) {
        require(recordId.isNotBlank()) { "Pending observation record ID cannot be blank." }
        pendingRecordId = recordId
    }

    fun releaseAfterPersistence(persistenceSucceeded: Boolean): String? {
        if (!persistenceSucceeded) return null
        return pendingRecordId.also { pendingRecordId = null }
    }

    fun clear() {
        pendingRecordId = null
    }
}
