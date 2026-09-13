package com.codex.campboardgamehost

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared bottom-inset contract for immersive Clocktower host surfaces.
 *
 * MainActivity owns system-bar visibility. Host surfaces reserve space only while the navigation
 * bar is actually visible, avoiding a permanent hidden-bar gap on OEMs that report stable insets.
 */
internal enum class ClocktowerHostBottomInsetPolicy {
    VisibleNavigationBars,
}

internal fun clocktowerHostBottomInsetPolicy(): ClocktowerHostBottomInsetPolicy =
    ClocktowerHostBottomInsetPolicy.VisibleNavigationBars

@Composable
internal fun Modifier.clocktowerHostBottomNavigationBarPadding(): Modifier =
    when (clocktowerHostBottomInsetPolicy()) {
        ClocktowerHostBottomInsetPolicy.VisibleNavigationBars ->
            windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
            )
    }
