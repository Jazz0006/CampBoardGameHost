package com.codex.campboardgamehost

import android.app.Application
import com.codex.campboardgamehost.debug.DebugFlightRecorder

class CampBoardGameHostApplication : Application() {
    override fun onCreate() {
        // Install before any Activity lifecycle/restoration callback can run.
        DebugFlightRecorder.install(this)
        super.onCreate()
    }
}
