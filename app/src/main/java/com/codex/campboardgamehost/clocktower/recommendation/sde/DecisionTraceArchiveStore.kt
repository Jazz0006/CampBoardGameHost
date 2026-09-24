package com.codex.campboardgamehost.clocktower.recommendation.sde

/**
 * Durable adapter for the immutable DecisionTrace archive.
 *
 * The store owns no mutable game/history state. Every operation reconstructs one complete immutable
 * archive from raw persistence, applies the archive contract, and writes a complete replacement only
 * when a new trace was actually admitted.
 */
internal class DecisionTraceArchiveStore(
    private val readRaw: () -> String?,
    private val writeRaw: (String) -> Boolean,
) {
    fun load(): DecisionTraceArchive {
        val raw = readRaw()
        if (raw.isNullOrBlank()) return DecisionTraceArchive()
        return DecisionTraceArchiveJsonCodec.decode(raw)
    }

    fun append(trace: DecisionTrace): Boolean {
        val current = load()
        val updated = current.append(trace)
        if (updated === current) return true
        return writeRaw(DecisionTraceArchiveJsonCodec.encode(updated))
    }
}
