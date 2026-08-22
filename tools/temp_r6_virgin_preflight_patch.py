from pathlib import Path
import re

APP = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly 1 anchor, found {count}")
    return text.replace(old, new, 1)


app = APP.read_text(encoding="utf-8")
host = HOST.read_text(encoding="utf-8")

host = replace_once(
    host,
    "    onSlayerShot: (String, String, Boolean) -> Unit,\n"
    "    onVirginNomination: (String, String, Boolean) -> Unit,\n",
    "    onSlayerShot: (String, String, Boolean) -> Unit,\n"
    "    onPreflightVirginExecution: (String, Boolean) -> Unit,\n"
    "    onVirginNomination: (String, String, Boolean) -> Unit,\n",
    "Host callback signature",
)

host = replace_once(
    host,
    "    fun recordSpyRegistration(\n"
    "        key: String?,\n"
    "        teams: List<ClocktowerTeam>,\n"
    "        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,\n"
    "    ) {\n"
    "        if (key == null || recordedSpyRegistrations[key] == true || spyCard == null) return\n",
    "    fun spyRegistrationWillRecord(key: String?): Boolean =\n"
    "        key != null && recordedSpyRegistrations[key] != true && spyCard != null\n"
    "    fun recordSpyRegistration(\n"
    "        key: String?,\n"
    "        teams: List<ClocktowerTeam>,\n"
    "        detail: ClocktowerRegistrationDetail = ClocktowerRegistrationDetail.Role,\n"
    "    ) {\n"
    "        if (key == null || recordedSpyRegistrations[key] == true || spyCard == null) return\n",
    "Spy registration preflight helper",
)

pattern = re.compile(
    r"(?P<indent>^[ \t]+)val chosenNominator = nominatorName\n"
    r"(?P=indent)val chosenNominee = nomineeName\n"
    r"(?P=indent)if \(chosenNominator != null && chosenNominee != null && virginFirstNomination\) \{",
    re.MULTILINE,
)


def inject_preflight(match: re.Match[str]) -> str:
    indent = match.group("indent")
    return (
        f"{indent}val chosenNominator = nominatorName\n"
        f"{indent}val chosenNominee = nomineeName\n"
        f"{indent}if (chosenNominator != null && chosenNominee != null && virginExecutes) {{\n"
        f"{indent}    onPreflightVirginExecution(\n"
        f"{indent}        chosenNominator,\n"
        f"{indent}        spyRegistrationWillRecord(virginRegistrationKey),\n"
        f"{indent}    )\n"
        f"{indent}}}\n"
        f"{indent}if (chosenNominator != null && chosenNominee != null && virginFirstNomination) {{"
    )

host, replaced = pattern.subn(inject_preflight, host)
if replaced != 2:
    raise SystemExit(f"Virgin UI flows: expected exactly 2 anchors, found {replaced}")

old_app = """                        onVirginNomination = { nominatorName, nomineeName, executeNominator ->
                            if (executeNominator) {
                                val preflightIndex = cards.indexOfFirst { it.name == nominatorName }
                                val preflightCard = cards.getOrNull(preflightIndex)
                                if (preflightIndex >= 0 && preflightCard != null && preflightCard.eliminatedRound == null) {
                                    preflightClocktowerPublicAliveObservation(
                                        playerName = nominatorName,
                                        eventSequence = clocktowerEventCounter + 1,
                                    )
                                }
                            }
                            clocktowerVirginUsed = true
"""
new_app = """                        onPreflightVirginExecution = { nominatorName, spyRegistrationWillRecord ->
                            val preflightIndex = cards.indexOfFirst { it.name == nominatorName }
                            val preflightCard = cards.getOrNull(preflightIndex)
                            if (preflightIndex >= 0 && preflightCard != null && preflightCard.eliminatedRound == null) {
                                preflightClocktowerPublicAliveObservation(
                                    playerName = nominatorName,
                                    eventSequence = clocktowerEventCounter + if (spyRegistrationWillRecord) 2 else 1,
                                )
                            }
                        },
                        onVirginNomination = { nominatorName, nomineeName, executeNominator ->
                            clocktowerVirginUsed = true
"""
app = replace_once(app, old_app, new_app, "App Virgin preflight wiring")

APP.write_text(app.rstrip() + "\n", encoding="utf-8")
HOST.write_text(host.rstrip() + "\n", encoding="utf-8")
