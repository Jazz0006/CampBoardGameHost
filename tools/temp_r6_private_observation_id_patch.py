from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly 1 anchor, found {count}")
    return text.replace(old, new, 1)


host = HOST.read_text(encoding="utf-8")

host = replace_once(
    host,
    "import java.util.Locale\nimport java.util.UUID\n",
    "import java.security.MessageDigest\nimport java.util.Locale\nimport java.util.UUID\n",
    "MessageDigest import",
)

host = replace_once(
    host,
    ").joinToString(\"|\")\n\nprivate data class ClocktowerDecisionOption(\n",
    ").joinToString(\"|\")\n\n"
    "internal fun clocktowerPrivateObservationRecordId(\n"
    "    gameId: String,\n"
    "    phase: ClocktowerPhase,\n"
    "    round: Int,\n"
    "    roleEnName: String,\n"
    "    actorSeat: Int,\n"
    "    proposition: InformationProposition,\n"
    "): String {\n"
    "    val statementKey = MessageDigest\n"
    "        .getInstance(\"SHA-256\")\n"
    "        .digest(EpistemicSemanticJson.encode(proposition).toByteArray(Charsets.UTF_8))\n"
    "        .joinToString(\"\") { byte ->\n"
    "            (byte.toInt() and 0xff).toString(16).padStart(2, '0')\n"
    "        }\n"
    "    return \"private-$gameId-${phase.name}-$round-$roleEnName-$actorSeat-$statementKey\"\n"
    "}\n\n"
    "private data class ClocktowerDecisionOption(\n",
    "private observation record-id helper",
)

host = replace_once(
    host,
    "            recordId = \"private-${gameId}-${phase.name}-${round}-${displayStep.roleEnName}-$actorSeat\",\n",
    "            recordId = clocktowerPrivateObservationRecordId(\n"
    "                gameId = gameId,\n"
    "                phase = phase,\n"
    "                round = round,\n"
    "                roleEnName = requireNotNull(displayStep.roleEnName),\n"
    "                actorSeat = actorSeat,\n"
    "                proposition = proposition,\n"
    "            ),\n",
    "reliable private observation record id",
)

HOST.write_text(host.rstrip() + "\n", encoding="utf-8")
