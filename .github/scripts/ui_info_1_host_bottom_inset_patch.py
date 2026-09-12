from pathlib import Path

night_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt")
day_path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayTableScaffoldUi.kt")
policy_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerHostBottomInsetPolicy.kt")

for path in (night_path, day_path):
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {path}")

if policy_path.exists():
    raise SystemExit("ClocktowerHostBottomInsetPolicy.kt already exists")

night = night_path.read_text(encoding="utf-8")
day = day_path.read_text(encoding="utf-8")

night_imports_old = """import androidx.compose.foundation.layout.WindowInsets\nimport androidx.compose.foundation.layout.WindowInsetsSides\nimport androidx.compose.foundation.layout.navigationBarsIgnoringVisibility\nimport androidx.compose.foundation.layout.only\nimport androidx.compose.foundation.layout.windowInsetsPadding\n"""
night_imports_new = ""
night_modifier_old = """            modifier = Modifier\n                .windowInsetsPadding(\n                    WindowInsets.navigationBarsIgnoringVisibility.only(WindowInsetsSides.Bottom),\n                )\n                .padding(horizontal = 16.dp, vertical = 10.dp),\n"""
night_modifier_new = """            modifier = Modifier\n                .clocktowerHostBottomNavigationBarPadding()\n                .padding(horizontal = 16.dp, vertical = 10.dp),\n"""

day_imports_old = """import androidx.compose.foundation.layout.WindowInsets\nimport androidx.compose.foundation.layout.WindowInsetsSides\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.navigationBars\nimport androidx.compose.foundation.layout.only\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.windowInsetsPadding\n"""
day_imports_new = """import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.padding\n"""
day_modifier_old = """                        modifier = Modifier\n                            .windowInsetsPadding(\n                                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),\n                            )\n                            .padding(horizontal = 16.dp, vertical = 10.dp),\n"""
day_modifier_new = """                        modifier = Modifier\n                            .clocktowerHostBottomNavigationBarPadding()\n                            .padding(horizontal = 16.dp, vertical = 10.dp),\n"""

for text_name, text, replacements in [
    ("night", night, [
        (night_imports_old, night_imports_new, "night inset imports"),
        (night_modifier_old, night_modifier_new, "night bottom inset modifier"),
    ]),
    ("day", day, [
        (day_imports_old, day_imports_new, "day inset imports"),
        (day_modifier_old, day_modifier_new, "day bottom inset modifier"),
    ]),
]:
    for old, new, label in replacements:
        count = text.count(old)
        if count != 1:
            raise SystemExit(f"Expected exactly one anchor for {label}, found {count}")
        text = text.replace(old, new, 1)
    if text_name == "night":
        night = text
    else:
        day = text

policy = """package com.codex.campboardgamehost

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
"""

night_path.write_text(night, encoding="utf-8", newline="\n")
day_path.write_text(day, encoding="utf-8", newline="\n")
policy_path.write_text(policy, encoding="utf-8", newline="\n")

if "navigationBarsIgnoringVisibility" in night:
    raise SystemExit("Night still references navigationBarsIgnoringVisibility")
if night.count("clocktowerHostBottomNavigationBarPadding()") != 1:
    raise SystemExit("Night must consume shared bottom inset exactly once")
if day.count("clocktowerHostBottomNavigationBarPadding()") != 1:
    raise SystemExit("Day must consume shared bottom inset exactly once")
