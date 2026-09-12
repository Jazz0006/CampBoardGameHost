from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected non-LF line endings in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")

replacements = [
    (
        "                clocktowerPhase = safeClocktower?.phase ?: game.position.phase\n",
        "                clocktowerPhase = clocktowerVisibleHostPhase(safeClocktower?.phase ?: game.position.phase)\n",
        "recovery Dawn visibility",
    ),
    (
        """    fun recordEpistemicObservation(draft: EpistemicObservationDraft) {\n""",
        """    fun enterClocktowerDayAfterDawn() {\n        check(clocktowerPhase == ClocktowerPhase.Dawn) {\n            \"Dawn-to-Day host transition requires the semantic Dawn boundary first.\"\n        }\n        recordClocktowerPhaseAdvance(ClocktowerPhase.Day)\n        clocktowerPhase = clocktowerVisibleHostPhase(ClocktowerPhase.Dawn)\n        resetClocktowerDayFlow()\n    }\n\n    fun recordEpistemicObservation(draft: EpistemicObservationDraft) {\n""",
        "Dawn-to-Day host transition helper",
    ),
    (
        """                                if (dawnPhaseStateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                    advanceClocktowerGameStateRevision()\n                                }\n""",
        """                                if (dawnPhaseStateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                    enterClocktowerDayAfterDawn()\n                                    advanceClocktowerGameStateRevision()\n                                }\n""",
        "new Demon Dawn completion",
    ),
    (
        """                                    if (clocktowerKlutzReturnToDawn) {\n                                        recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)\n                                        clocktowerPhase = ClocktowerPhase.Dawn\n                                        clocktowerKlutzReturnToDawn = false\n                                    } else {\n""",
        """                                    if (clocktowerKlutzReturnToDawn) {\n                                        recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)\n                                        clocktowerPhase = ClocktowerPhase.Dawn\n                                        enterClocktowerDayAfterDawn()\n                                        clocktowerKlutzReturnToDawn = false\n                                    } else {\n""",
        "night Klutz Dawn completion",
    ),
    (
        """                                if (phaseAdvance.stateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                }\n                                resetClocktowerNightFlow()\n""",
        """                                if (phaseAdvance.stateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                    enterClocktowerDayAfterDawn()\n                                }\n                                resetClocktowerNightFlow()\n""",
        "ordinary night Dawn completion",
    ),
]

for old, new, label in replacements:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor for {label}, found {count}")
    text = text.replace(old, new, 1)

if text.count("enterClocktowerDayAfterDawn()") != 4:
    raise SystemExit("Expected one helper definition plus three Dawn completion calls")
if "clocktowerPhase = clocktowerVisibleHostPhase(safeClocktower?.phase ?: game.position.phase)" not in text:
    raise SystemExit("Recovery Dawn visibility policy was not installed")

path.write_text(text, encoding="utf-8", newline="\n")
