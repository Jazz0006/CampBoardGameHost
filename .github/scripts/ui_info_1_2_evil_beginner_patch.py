from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected non-LF source")
text = raw.decode("utf-8")

old_guidance = '''        clocktowerBeginnerNightGuidance(
            action = step.action,
            actor = step.actor,
            cards = cards,
            language = language,
        )
'''
new_guidance = '''        clocktowerBeginnerNightGuidance(
            action = step.action,
            actor = step.actor,
            cards = cards,
            language = language,
            groupTeam = step.actor
                ?.clocktowerRole
                ?.team
                ?.takeIf { team ->
                    step.displayKind == ClocktowerDisplayKind.EvilInfo &&
                        team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)
                },
        )
'''
old_evil = '''        val evilInfoSquareTablePresentation = clocktowerEvilInfoSquareTablePresentation(
            step = step,
            actorSeat = actionActorSeat,
            wakeInstruction = command,
        )
'''
new_evil = '''        val evilInfoSquareTablePresentation = clocktowerEvilInfoSquareTablePresentation(
            step = step,
            actorSeat = actionActorSeat,
            wakeInstruction = command,
            beginnerMode = automaticStorytellerInfo,
        )
'''

for label, old in (("beginner guidance wiring", old_guidance), ("evil info filtering wiring", old_evil)):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")

text = text.replace(old_guidance, new_guidance, 1).replace(old_evil, new_evil, 1)
if "groupTeam = step.actor" not in text or "beginnerMode = automaticStorytellerInfo" not in text:
    raise SystemExit("post-patch semantic assertion failed")
path.write_text(text, encoding="utf-8", newline="\n")
