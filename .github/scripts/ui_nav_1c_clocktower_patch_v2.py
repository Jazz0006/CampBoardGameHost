from pathlib import Path

LEGACY = Path(".github/scripts/ui_nav_1c_clocktower_patch.py")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")

source = LEGACY.read_text(encoding="utf-8")
section_start_marker = "# D6 composition owner receives and distributes the root utility callback."
section_end_marker = "# App root remains the sole Host Tools state owner."
start = source.index(section_start_marker)
end = source.index(section_end_marker, start)
section = source[start:end]

if section.count("expected=2") != 1:
    raise SystemExit(
        f"D6 HostScreen section expected=2 count {section.count('expected=2')}, expected exactly 1",
    )
section = section.replace("expected=2", "expected=1")
patched_source = source[:start] + section + source[end:]

# Execute the exact 1C.1 patch with only the proven D6 call-count correction.
exec(compile(patched_source, str(LEGACY), "exec"), {"__name__": "__main__"})

# The other NightStep call is intentionally handled independently: it has a different
# composition shape and must not be folded into the NightActive call-count assumption.
host_text = HOST.read_text(encoding="utf-8")
old = """                onPrevious = onMovePreviousNightStep,
                onNext = advanceNightStep,
                showNavigationActions = false,
"""
new = """                onPrevious = onMovePreviousNightStep,
                onHostTools = onHostTools,
                onNext = advanceNightStep,
                showNavigationActions = false,
"""
count = host_text.count(old)
if count != 1:
    raise SystemExit(f"NightStep HostScreen call anchor count {count}, expected exactly 1")
HOST.write_text(host_text.replace(old, new), encoding="utf-8", newline="\n")

final_host = HOST.read_text(encoding="utf-8")
if final_host.count("onHostTools = onHostTools") < 2:
    raise SystemExit("ClocktowerHostScreen did not wire Host Tools to both night presentation seams")

print("UI-NAV-1C.1 D6-aware wrapper completed")
