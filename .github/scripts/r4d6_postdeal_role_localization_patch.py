from pathlib import Path
import re

path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("ClocktowerHostScreen.kt must use LF endings")
text = raw.decode("utf-8")

call_count = text.count("clocktowerDayOverviewTableState(")
if call_count != 6:
    raise SystemExit(f"Expected exactly 6 Day table projection calls, found {call_count}")

resolver = "roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },"
if resolver in text:
    raise SystemExit("Day role localization resolver already present before patch")

pattern = re.compile(
    r"(clocktowerDayOverviewTableState\(\n"
    r"(?P<arg> +)cards\.toClocktowerGameState\(\n"
    r"(?P<inner> +)script = script,\n"
    r"(?P=inner)seed = gameSeed,\n"
    r"(?P=inner)poisonedPlayerName = poisonTarget,\n"
    r"(?P=arg)\),\n)"
    r"(?P<close> +)\)"
)


def add_resolver(match: re.Match[str]) -> str:
    return (
        match.group(1)
        + match.group("arg")
        + resolver
        + "\n"
        + match.group("close")
        + ")"
    )

patched, substitutions = pattern.subn(add_resolver, text)
if substitutions != 6:
    raise SystemExit(f"Expected to localize exactly 6 Day table calls, patched {substitutions}")
if patched.count(resolver) != 6:
    raise SystemExit("Postcondition failed: expected exactly 6 localized Day role resolvers")
if patched.count("clocktowerDayOverviewTableState(") != 6:
    raise SystemExit("Postcondition failed: Day table call count changed")

path.write_text(patched, encoding="utf-8", newline="\n")
