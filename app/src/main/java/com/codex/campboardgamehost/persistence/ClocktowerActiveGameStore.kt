package com.codex.campboardgamehost

import android.content.Context
import android.util.AtomicFile
import com.codex.campboardgamehost.clocktower.session.*
import java.io.File
import org.json.JSONObject

/** One app-private active Session, without backup/export or a parallel history. */
internal class ClocktowerActiveGameStore(file: File) {
    private val atomic = AtomicFile(file)
    data class Saved(val state: ClocktowerSessionState, val metadata: JSONObject)
    fun load(): Saved? {
        if (!atomic.baseFile.exists() && !File(atomic.baseFile.path + ".bak").exists()) return null
        val json = atomic.openRead().use { JSONObject(it.readBytes().toString(Charsets.UTF_8)) }
        require(json.getInt("version") == 1)
        return Saved(ClocktowerSessionJson.decode(json.getJSONObject("session").toString()), json.getJSONObject("metadata"))
    }
    fun save(state: ClocktowerSessionState, metadata: JSONObject) {
        val bytes = JSONObject().put("version", 1).put("session", JSONObject(ClocktowerSessionJson.encode(state)))
            .put("metadata", metadata).toString().toByteArray(Charsets.UTF_8)
        val output = atomic.startWrite()
        try {
            output.write(bytes)
            atomic.finishWrite(output)
            // AtomicFile logs some rename/sync failures. Never publish memory unless the
            // complete accepted bytes are actually readable from the primary file.
            checkSavedBytes(bytes)
        }
        catch (failure: Throwable) { atomic.failWrite(output); throw failure }
    }
    private fun checkSavedBytes(expected: ByteArray) {
        val actual = atomic.openRead().use { it.readBytes() }
        if (!actual.contentEquals(expected)) throw java.io.IOException("Active game save was not completed")
    }
    fun clear() = atomic.delete()
    companion object {
        fun fromContext(context: Context) = ClocktowerActiveGameStore(File(context.noBackupFilesDir, "active-clocktower.json"))
    }
}
