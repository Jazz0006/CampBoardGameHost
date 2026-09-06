package com.codex.campboardgamehost

import android.app.Application
import com.codex.campboardgamehost.debug.DebugFlightRecorder

class CampBoardGameHostApplication : Application() {
    override fun onCreate() {
        DebugFlightRecorder.install(this)
        super.onCreate()
    }
}
