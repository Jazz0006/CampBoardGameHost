from pathlib import Path

APP = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
DAY = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayScreen.kt")
POLICY = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerDawnPresentationPolicy.kt")


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {path}")
    return raw.decode("utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one anchor for {label}, found {count}")
    return text.replace(old, new, 1)


app = read_lf(APP)
app = replace_once(
    app,
    "                clocktowerPhase = clocktowerVisibleHostPhase(safeClocktower?.phase ?: game.position.phase)\n",
    "                clocktowerPhase = safeClocktower?.phase ?: game.position.phase\n",
    "recovery Dawn visibility",
)
app = replace_once(
    app,
    """    fun enterClocktowerDayAfterDawn() {\n        check(clocktowerPhase == ClocktowerPhase.Dawn) {\n            \"Dawn-to-Day host transition requires the semantic Dawn boundary first.\"\n        }\n        recordClocktowerPhaseAdvance(ClocktowerPhase.Day)\n        clocktowerPhase = clocktowerVisibleHostPhase(ClocktowerPhase.Dawn)\n        resetClocktowerDayFlow()\n    }\n\n""",
    "",
    "premature Dawn-to-Day helper",
)
app = replace_once(
    app,
    """                                if (dawnPhaseStateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                    enterClocktowerDayAfterDawn()\n                                    advanceClocktowerGameStateRevision()\n                                }\n""",
    """                                if (dawnPhaseStateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                    advanceClocktowerGameStateRevision()\n                                }\n""",
    "new Demon Dawn completion",
)
app = replace_once(
    app,
    """                                    if (clocktowerKlutzReturnToDawn) {\n                                        recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)\n                                        clocktowerPhase = ClocktowerPhase.Dawn\n                                        enterClocktowerDayAfterDawn()\n                                        clocktowerKlutzReturnToDawn = false\n                                    } else {\n""",
    """                                    if (clocktowerKlutzReturnToDawn) {\n                                        recordClocktowerPhaseAdvance(ClocktowerPhase.Dawn)\n                                        clocktowerPhase = ClocktowerPhase.Dawn\n                                        clocktowerKlutzReturnToDawn = false\n                                    } else {\n""",
    "night Klutz Dawn completion",
)
app = replace_once(
    app,
    """                                if (phaseAdvance.stateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                    enterClocktowerDayAfterDawn()\n                                }\n                                resetClocktowerNightFlow()\n""",
    """                                if (phaseAdvance.stateMutationRequired) {\n                                    clocktowerPhase = ClocktowerPhase.Dawn\n                                }\n                                resetClocktowerNightFlow()\n""",
    "ordinary night Dawn completion",
)
if "enterClocktowerDayAfterDawn" in app:
    raise SystemExit("Premature Dawn-to-Day helper/call survived patch")
APP.write_text(app, encoding="utf-8", newline="\n")

policy = read_lf(POLICY)
expected_policy = """package com.codex.campboardgamehost\n\n/**\n * Dawn remains a standalone host-facing announcement surface until the Storyteller confirms the\n * public announcement and explicitly enters Day.\n */\ninternal fun clocktowerVisibleHostPhase(phase: ClocktowerPhase): ClocktowerPhase = phase\n"""
POLICY.write_text(expected_policy, encoding="utf-8", newline="\n")

day = read_lf(DAY)
day = replace_once(
    day,
    "    var showPublicAnnouncement by remember(round, pendingNightDeath) { mutableStateOf(false) }\n",
    "",
    "full-screen announcement state",
)
day = replace_once(
    day,
    """        if (showPublicAnnouncement) {\n            Column(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .background(MaterialTheme.colorScheme.background)\n                    .padding(24.dp),\n                horizontalAlignment = Alignment.CenterHorizontally,\n                verticalArrangement = Arrangement.Center,\n            ) {\n                Text(\n                    text(\"天亮了\", \"DAWN\"),\n                    color = MaterialTheme.colorScheme.primary,\n                    style = MaterialTheme.typography.labelLarge,\n                    fontWeight = FontWeight.Black,\n                    letterSpacing = 2.sp,\n                )\n                Spacer(modifier = Modifier.height(28.dp))\n                Text(\n                    text = deathLabel?.let {\n                        text(\"昨晚，$it 死亡。\", \"$it died last night.\")\n                    } ?: text(\"昨晚，没有人死亡。\", \"Nobody died last night.\"),\n                    color = MaterialTheme.colorScheme.onBackground,\n                    style = MaterialTheme.typography.headlineLarge,\n                    fontWeight = FontWeight.Black,\n                    textAlign = TextAlign.Center,\n                )\n                Spacer(modifier = Modifier.height(36.dp))\n                OutlinedButton(\n                    onClick = { showPublicAnnouncement = false },\n                    modifier = Modifier\n                        .fillMaxWidth()\n                        .height(52.dp),\n                    shape = RoundedCornerShape(14.dp),\n                ) {\n                    Text(text(\"收回手机\", \"Return to host\"))\n                }\n            }\n            return@ClocktowerDarkTheme\n        }\n\n""",
    "",
    "full-screen announcement page",
)
day = replace_once(
    day,
    """                            OutlinedButton(\n                                onClick = { showPublicAnnouncement = true },\n                                modifier = Modifier\n                                    .fillMaxWidth()\n                                    .height(50.dp),\n                                shape = RoundedCornerShape(14.dp),\n                            ) {\n                                Text(text(\"全屏展示播报内容\", \"Show announcement full screen\"))\n                            }\n""",
    "",
    "full-screen announcement button",
)
if "showPublicAnnouncement" in day or "全屏展示播报内容" in day or "Show announcement full screen" in day:
    raise SystemExit("Legacy full-screen announcement surface survived patch")
if 'nextLabel = text("已完成播报，进入白天", "Announcement complete — enter day")' not in day:
    raise SystemExit("Dawn announcement completion action was not preserved")
if 'text("公开播报", "PUBLIC ANNOUNCEMENT")' not in day:
    raise SystemExit("Host-facing public announcement panel was not preserved")
DAY.write_text(day, encoding="utf-8", newline="\n")
