#!/usr/bin/env python3
from pathlib import Path
import subprocess

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
EXPECTED_BLOB = "40c2cc68e339c523c68f1700281c1f8984480693"

OLD = '''        onShowPlayerDisplay(
            step.copy(
                tellPlayer = option.displayPrimary,
                displayKind = option.displayKind,
                displayTitle = option.displayTitle,
                displayPrimary = option.displayPrimary,
                displaySecondary = option.displaySecondary,
                displayFooter = option.displayFooter,
                displayProposition = option.proposition,
                selectedInformationTruthful = option.isTruthful,
                displayOptions = emptyList(),
                recommendedDisplayOptions = emptyList(),
            ),
        )
'''
NEW = '''        onShowPlayerDisplay(resolveClocktowerPlayerDisplay(step, option))
'''


def main() -> None:
    actual_blob = subprocess.check_output(
        ["git", "hash-object", str(TARGET)], text=True
    ).strip()
    if actual_blob != EXPECTED_BLOB:
        raise SystemExit(
            f"Refusing patch: {TARGET} blob is {actual_blob}, expected {EXPECTED_BLOB}"
        )

    text = TARGET.read_text(encoding="utf-8")
    count = text.count(OLD)
    if count != 1:
        raise SystemExit(f"Refusing patch: exact reveal block count is {count}, expected 1")
    if "resolveClocktowerPlayerDisplay(step, option)" in text:
        raise SystemExit("Refusing patch: resolver wiring already exists")

    patched = text.replace(OLD, NEW, 1)
    TARGET.write_text(patched, encoding="utf-8")

    if patched.count("resolveClocktowerPlayerDisplay(step, option)") != 1:
        raise SystemExit("Post-patch audit failed: expected exactly one resolver call")
    if OLD in patched:
        raise SystemExit("Post-patch audit failed: old inline reveal block remains")

    print("F4 Player Reveal wiring patch applied exactly once.")


if __name__ == "__main__":
    main()
