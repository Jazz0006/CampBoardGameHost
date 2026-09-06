package com.codex.campboardgamehost.debug

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.content.FileProvider
import com.codex.campboardgamehost.BuildConfig
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

object DebugFlightRecorder {
    private const val TAG = "DebugFlightRecorder"
    private const val MAX_EVENTS = 400
    private const val MAX_TRACE_BYTES = 512 * 1024L
    private const val ROOT_DIR = "debug-flight-recorder"
    private const val TRACE_FILE = "current-trace.jsonl"
    private const val LATEST_CRASH_DIR = "latest-crash"
    private const val EXPORT_DIR = "debug-flight-recorder-export"

    private val installed = AtomicBoolean(false)
    private val handlingCrash = AtomicBoolean(false)
    private val eventBuffer = DebugEventBuffer(MAX_EVENTS)
    private val stateLock = Any()
    private val ioLock = Any()

    @Volatile
    private var appContext: Context? = null
    private var latestState: Map<String, String> = emptyMap()

    fun install(context: Context) {
        val contextToKeep = context.applicationContext
        appContext = contextToKeep
        if (!installed.compareAndSet(false, true)) return

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (handlingCrash.compareAndSet(false, true)) {
                runCatching {
                    captureCrash(thread, throwable)
                }.onFailure {
                    Log.e(TAG, "Unable to capture crash", it)
                }
            }

            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable)
            } else {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }

        record(
            event = "APP_START",
            fields = mapOf(
                "versionName" to BuildConfig.VERSION_NAME,
                "versionCode" to BuildConfig.VERSION_CODE.toString(),
                "buildType" to BuildConfig.BUILD_TYPE,
            ),
        )
    }

    fun record(
        event: String,
        fields: Map<String, String> = emptyMap(),
    ) {
        val debugEvent = DebugEvent(
            timestampMillis = System.currentTimeMillis(),
            event = event,
            fields = fields,
        )
        eventBuffer.record(debugEvent)
        persistBreadcrumb(debugEvent)
    }

    fun updateState(state: Map<String, String>) {
        synchronized(stateLock) {
            latestState = state.toMap()
        }
    }

    fun shareDebugBundle(context: Context) {
        runCatching {
            val bundle = createDebugBundle(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.debugfileprovider",
                bundle,
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Export debug information")
            if (context !is Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            record("DEBUG_BUNDLE_EXPORTED")
        }.onFailure {
            Log.e(TAG, "Unable to export debug bundle", it)
        }
    }

    private fun persistBreadcrumb(event: DebugEvent) {
        val context = appContext ?: return
        runCatching {
            synchronized(ioLock) {
                val root = File(context.noBackupFilesDir, ROOT_DIR).apply { mkdirs() }
                val trace = File(root, TRACE_FILE)
                trace.appendText(DebugFlightRecorderCodec.encodeEvents(listOf(event)))
                if (trace.length() > MAX_TRACE_BYTES) {
                    trace.writeText(DebugFlightRecorderCodec.encodeEvents(eventBuffer.snapshot()))
                }
            }
        }.onFailure {
            Log.e(TAG, "Unable to persist breadcrumb", it)
        }
    }

    private fun captureCrash(thread: Thread, throwable: Throwable) {
        val context = appContext ?: return
        val capturedAtMillis = System.currentTimeMillis()
        val state = synchronized(stateLock) { latestState.toMap() }
        val snapshot = DebugCrashSnapshot(
            capturedAtMillis = capturedAtMillis,
            threadName = thread.name,
            throwable = throwable,
            state = state,
        )

        synchronized(ioLock) {
            val root = File(context.noBackupFilesDir, ROOT_DIR).apply { mkdirs() }
            val crashDir = File(root, LATEST_CRASH_DIR)
            if (crashDir.exists()) crashDir.deleteRecursively()
            crashDir.mkdirs()

            File(crashDir, "meta.json").writeText(buildMetaJson(capturedAtMillis).toString(2))
            File(crashDir, "crash.txt").writeText(DebugFlightRecorderCodec.encodeCrash(snapshot))
            File(crashDir, "state.json").writeText(DebugFlightRecorderCodec.encodeState(state))

            val persistedTrace = File(root, TRACE_FILE)
            val traceText = if (persistedTrace.exists()) {
                persistedTrace.readText()
            } else {
                DebugFlightRecorderCodec.encodeEvents(eventBuffer.snapshot())
            }
            File(crashDir, "trace.jsonl").writeText(traceText)
        }
    }

    private fun createDebugBundle(context: Context): File {
        val root = File(context.noBackupFilesDir, ROOT_DIR).apply { mkdirs() }
        val crashDir = File(root, LATEST_CRASH_DIR)
        val exportDir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
        exportDir.listFiles()?.forEach { old ->
            if (old.isFile) old.delete()
        }

        val now = System.currentTimeMillis()
        val output = File(exportDir, "storyteller-debug-$now.zip")
        val currentState = synchronized(stateLock) { latestState.toMap() }
        val trace = File(root, TRACE_FILE)

        ZipOutputStream(FileOutputStream(output)).use { zip ->
            if (crashDir.isDirectory && File(crashDir, "crash.txt").isFile) {
                listOf("meta.json", "crash.txt", "state.json", "trace.jsonl").forEach { name ->
                    val source = File(crashDir, name)
                    if (source.isFile) {
                        zip.putNextEntry(ZipEntry(name))
                        source.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            } else {
                writeZipText(zip, "meta.json", buildMetaJson(now).toString(2))
                writeZipText(zip, "state.json", DebugFlightRecorderCodec.encodeState(currentState))
                val traceText = if (trace.isFile) {
                    trace.readText()
                } else {
                    DebugFlightRecorderCodec.encodeEvents(eventBuffer.snapshot())
                }
                writeZipText(zip, "trace.jsonl", traceText)
                writeZipText(
                    zip,
                    "crash.txt",
                    "No uncaught crash has been captured. This bundle contains the current diagnostic trace.\n",
                )
            }
        }
        return output
    }

    private fun writeZipText(zip: ZipOutputStream, name: String, text: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildMetaJson(capturedAtMillis: Long): JSONObject = JSONObject()
        .put("capturedAtMillis", capturedAtMillis)
        .put("appVersionName", BuildConfig.VERSION_NAME)
        .put("appVersionCode", BuildConfig.VERSION_CODE)
        .put("buildType", BuildConfig.BUILD_TYPE)
        .put("androidSdk", Build.VERSION.SDK_INT)
        .put("androidRelease", Build.VERSION.RELEASE)
        .put("manufacturer", Build.MANUFACTURER)
        .put("model", Build.MODEL)
}
