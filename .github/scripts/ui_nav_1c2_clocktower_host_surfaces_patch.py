from pathlib import Path
import re

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
APP = ROOT / "CampBoardGameHostApp.kt"
REC = ROOT / "ClocktowerStorytellerRecommendationUi.kt"
UI = ROOT / "clocktower/ui"
HOST = UI / "ClocktowerHostScreen.kt"
NIGHT = UI / "ClocktowerNightScreen.kt"
DAY = UI / "ClocktowerDayScreen.kt"
OVERVIEW = UI / "ClocktowerDayOverviewTableUi.kt"
NOMINATION = UI / "ClocktowerPendingNominationTableUi.kt"
VOTE = UI / "ClocktowerVoteTableUi.kt"
SLAYER = UI / "ClocktowerSlayerTableUi.kt"
ARTIST = UI / "ClocktowerArtistTableUi.kt"
KLUTZ = UI / "ClocktowerKlutzTableUi.kt"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


def replace_exact(text: str, old: str, new: str, *, expected: int = 1, label: str) -> str:
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} exact matches, got {count}")
    return text.replace(old, new)


def sub_exact(text: str, pattern: str, replacement: str, *, expected: int = 1, label: str) -> str:
    result, count = re.subn(pattern, replacement, text, flags=re.MULTILINE | re.DOTALL)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} regex matches, got {count}")
    return result


def insert_function_param(text: str, function_name: str, before_param: str, *, expected: int = 1) -> str:
    pattern = re.compile(rf"(?m)^(?P<indent>\s*){re.escape(before_param)}:\s*")
    search_at = 0
    hits = 0
    while True:
        marker = re.search(rf"(?m)^\s*(?:internal|private) fun {re.escape(function_name)}\(", text[search_at:])
        if marker is None:
            break
        start = search_at + marker.start()
        open_paren = text.index("(", start)
        depth = 0
        close_paren = None
        i = open_paren
        while i < len(text):
            ch = text[i]
            if ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0:
                    close_paren = i
                    break
            i += 1
        if close_paren is None:
            raise SystemExit(f"{function_name}: unterminated parameter list")
        signature = text[open_paren + 1:close_paren]
        if "onHostTools: () -> Unit" in signature:
            raise SystemExit(f"{function_name}: Host Tools parameter already present")
        m = pattern.search(signature)
        if m is None:
            raise SystemExit(f"{function_name}: before parameter {before_param} not found")
        insertion = f"{m.group('indent')}onHostTools: () -> Unit,\n"
        signature = signature[:m.start()] + insertion + signature[m.start():]
        text = text[:open_paren + 1] + signature + text[close_paren:]
        hits += 1
        search_at = close_paren + len(insertion) + 1
    if hits != expected:
        raise SystemExit(f"{function_name}: expected {expected} definitions, got {hits}")
    return text


def find_matching_paren(text: str, open_paren: int) -> int:
    depth = 0
    in_string = False
    escaped = False
    for i in range(open_paren, len(text)):
        ch = text[i]
        if in_string:
            if escaped:
                escaped = False
            elif ch == "\\":
                escaped = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return i
    raise SystemExit("unterminated call")


def insert_call_arg(text: str, callee: str, before_arg: str, *, expected: int = 1) -> str:
    matches = list(re.finditer(rf"\b{re.escape(callee)}\(", text))
    if len(matches) != expected:
        raise SystemExit(f"{callee}: expected {expected} calls, got {len(matches)}")
    for match in reversed(matches):
        open_paren = text.index("(", match.start())
        close_paren = find_matching_paren(text, open_paren)
        block = text[open_paren + 1:close_paren]
        if "onHostTools = onHostTools" in block:
            raise SystemExit(f"{callee}: Host Tools argument already present")
        arg_match = re.search(rf"(?m)^(?P<indent>\s*){re.escape(before_arg)}\s*=", block)
        if arg_match is None:
            raise SystemExit(f"{callee}: before argument {before_arg} not found")
        insertion = f"{arg_match.group('indent')}onHostTools = onHostTools,\n"
        block = block[:arg_match.start()] + insertion + block[arg_match.start():]
        text = text[:open_paren + 1] + block + text[close_paren:]
    return text


# Root: ClocktowerJudge no longer depends on global top chrome after this atomic slice.
app = read(APP)
app = replace_exact(
    app,
    """        screen == Screen.WerewolfJudge ||\n        screen == Screen.ClocktowerJudge ||\n        screen == Screen.Game\n""",
    """        screen == Screen.WerewolfJudge ||\n        screen == Screen.Game\n""",
    label="remove ClocktowerJudge global top bar",
)
write(APP, app)

# Night-ready / first-night recommendation boundary: no rollback, Host Tools remains root-owned.
rec = read(REC)
rec = insert_function_param(rec, "ClocktowerStorytellerRecommendationScreen", "onStartNight")
rec = sub_exact(
    rec,
    r"""            Surface\(color = MaterialTheme\.colorScheme\.surface, shadowElevation = 12\.dp\) \{\n                Button\(\n                    onClick = onStartNight,.*?\n                \}\n            \}\n""",
    """            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {\n                HostBottomActionBar(\n                    previousLabel = text(\"上一步\", \"Previous\"),\n                    hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                    nextLabel = buttonLabel,\n                    onPrevious = {},\n                    onHostTools = onHostTools,\n                    onNext = onStartNight,\n                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),\n                    previousEnabled = false,\n                )\n            }\n""",
    label="night-ready bottom bar",
)
write(REC, rec)

# New Demon host confirmation. Player-facing display remains a separate full-screen surface.
night = read(NIGHT)
night = insert_function_param(night, "ClocktowerNewDemonConfirmationScreen", "onShowPlayerDisplay")
night = sub_exact(
    night,
    r"""                    Button\(\n                        onClick = onConfirm,\n                        enabled = hasNewDemon,.*?\n                    \}\n""",
    """                    HostBottomActionBar(\n                        previousLabel = text(\"上一步\", \"Previous\"),\n                        hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                        nextLabel = text(\"已告知，进入天亮\", \"Informed, continue to dawn\"),\n                        onPrevious = {},\n                        onHostTools = onHostTools,\n                        onNext = onConfirm,\n                        previousEnabled = false,\n                        nextEnabled = hasNewDemon,\n                    )\n""",
    label="new Demon bottom bar",
)
write(NIGHT, night)

# Dawn private host review gets bottom utility chrome. Public announcement remains chrome-free.
day = read(DAY)
day = insert_function_param(day, "ClocktowerDawnSummaryScreen", "onEnterDay")
day = sub_exact(
    day,
    r"""            Surface\(color = MaterialTheme\.colorScheme\.surface, shadowElevation = 12\.dp\) \{\n                Button\(\n                    onClick = onEnterDay,.*?\n                \}\n            \}\n""",
    """            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {\n                HostBottomActionBar(\n                    previousLabel = text(\"上一步\", \"Previous\"),\n                    hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                    nextLabel = text(\"已完成播报，进入白天\", \"Announcement complete — enter day\"),\n                    onPrevious = {},\n                    onHostTools = onHostTools,\n                    onNext = onEnterDay,\n                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),\n                    previousEnabled = false,\n                )\n            }\n""",
    label="Dawn private bottom bar",
)

day = insert_function_param(day, "ClocktowerExecutionConfirmScreen", "onConfirm")
day = sub_exact(
    day,
    r"""            Surface\(color = MaterialTheme\.colorScheme\.surface, shadowElevation = 12\.dp\) \{\n                Column\(\n                    modifier = Modifier\n                        \.fillMaxWidth\(\)\n                        \.padding\(horizontal = 16\.dp, vertical = 12\.dp\),\n                    verticalArrangement = Arrangement\.spacedBy\(8\.dp\),\n                \) \{\n                    Button\(\n                        onClick = onConfirm,.*?\n                    \}\n                    OutlinedButton\(\n                        onClick = onBack,.*?\n                    \}\n                \}\n            \}\n""",
    """            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {\n                HostBottomActionBar(\n                    previousLabel = text(\"返回白天检查\", \"Return to day\"),\n                    hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                    nextLabel = if (targetLabel != null) {\n                        text(\"确认处决 $targetLabel\", \"Confirm execution: $targetLabel\")\n                    } else {\n                        text(\"确认无人被处决，进入夜晚\", \"Confirm no execution and continue\")\n                    },\n                    onPrevious = onBack,\n                    onHostTools = onHostTools,\n                    onNext = onConfirm,\n                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),\n                    nextEnabled = actionsEnabled,\n                )\n            }\n""",
    label="execution confirmation bottom bar",
)
write(DAY, day)

# Day overview: no cross-boundary Previous; End Day remains the existing right-side action.
overview = read(OVERVIEW)
overview = insert_function_param(overview, "ClocktowerDayOverviewScreen", "diagnosticContent")
overview = insert_function_param(overview, "ClocktowerDayOverviewCenterContent", "diagnosticContent")
overview = replace_exact(
    overview,
    """                        actionsEnabled = actionsEnabled,\n                        diagnosticContent = diagnosticContent,\n""",
    """                        actionsEnabled = actionsEnabled,\n                        onHostTools = onHostTools,\n                        diagnosticContent = diagnosticContent,\n""",
    label="overview center Host Tools plumbing",
)
overview = sub_exact(
    overview,
    r"""        TextButton\(\n            onClick = onEndDay,.*?\n        \}\n\n        diagnosticContent\?\.invoke\(\)\n""",
    """        diagnosticContent?.invoke()\n\n        HostBottomActionBar(\n            previousLabel = text(\"上一步\", \"Previous\"),\n            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n            nextLabel = text(\"结束白天\", \"End day\"),\n            onPrevious = {},\n            onHostTools = onHostTools,\n            onNext = onEndDay,\n            previousEnabled = false,\n            nextEnabled = actionsEnabled,\n        )\n""",
    label="day overview bottom bar",
)
write(OVERVIEW, overview)

# Nomination: existing cancel is the valid left-side owner.
nomination = read(NOMINATION)
nomination = insert_function_param(nomination, "ClocktowerPendingNominationTableScreen", "onContinue")
nomination = sub_exact(
    nomination,
    r"""                        Button\(\n                            onClick = onContinue,.*?\n                        \}\n                        TextButton\(\n                            onClick = onCancel,.*?\n                        \}\n""",
    """                        HostBottomActionBar(\n                            previousLabel = text(\"取消提名\", \"Cancel nomination\"),\n                            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                            nextLabel = continueLabel,\n                            onPrevious = onCancel,\n                            onHostTools = onHostTools,\n                            onNext = onContinue,\n                            nextEnabled = actionsEnabled && pendingLink != null,\n                        )\n""",
    label="nomination bottom bar",
)
write(NOMINATION, nomination)

# Vote: invalid target still offers Return + Host Tools; normal vote reuses cancel/confirm owners.
vote = read(VOTE)
vote = insert_function_param(vote, "ClocktowerVoteTableScreen", "onConfirm")
vote = replace_exact(
    vote,
    """                    TextButton(onClick = onCancel) {\n                        Text(text(\"返回提名\", \"Return to nomination\"))\n                    }\n""",
    """                    HostBottomActionBar(\n                        previousLabel = text(\"返回提名\", \"Return to nomination\"),\n                        hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                        nextLabel = text(\"确认投票\", \"Confirm vote\"),\n                        onPrevious = onCancel,\n                        onHostTools = onHostTools,\n                        onNext = {},\n                        nextEnabled = false,\n                    )\n""",
    label="invalid vote target bottom bar",
)
vote = sub_exact(
    vote,
    r"""                        Button\(\n                            onClick = \{ onConfirm\(voteState\) \},.*?\n                        \}\n                        TextButton\(\n                            onClick = onCancel,.*?\n                        \}\n""",
    """                        HostBottomActionBar(\n                            previousLabel = text(\"取消投票\", \"Cancel vote\"),\n                            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                            nextLabel = text(\"确认投票\", \"Confirm vote\"),\n                            onPrevious = onCancel,\n                            onHostTools = onHostTools,\n                            onNext = { onConfirm(voteState) },\n                            nextEnabled = actionsEnabled,\n                        )\n""",
    label="vote bottom bar",
)
write(VOTE, vote)

# Slayer / Artist preserve existing Return-to-day semantics.
slayer = read(SLAYER)
slayer = insert_function_param(slayer, "ClocktowerSlayerTableScreen", "onSeatClick")
slayer = sub_exact(
    slayer,
    r"""                        Button\(\n                            onClick = onResolve,.*?\n                        \}\n\n                        OutlinedButton\(\n                            onClick = onBack,.*?\n                        \}\n""",
    """                        HostBottomActionBar(\n                            previousLabel = text(\"返回白天\", \"Return to day\"),\n                            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                            nextLabel = text(\"结算杀手行动\", \"Resolve Slayer action\"),\n                            onPrevious = onBack,\n                            onHostTools = onHostTools,\n                            onNext = onResolve,\n                            nextEnabled = actionsEnabled &&\n                                tableState.claimantSeatId != null &&\n                                tableState.targetSeatId != null,\n                        )\n""",
    label="Slayer bottom bar",
)
write(SLAYER, slayer)

artist = read(ARTIST)
artist = insert_function_param(artist, "ClocktowerArtistTableScreen", "onSeatClick")
artist = sub_exact(
    artist,
    r"""                        Button\(\n                            onClick = onPrimary,.*?\n                        \}\n                        OutlinedButton\(\n                            onClick = onBack,.*?\n                        \}\n""",
    """                        HostBottomActionBar(\n                            previousLabel = text(\"返回白天\", \"Return to day\"),\n                            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                            nextLabel = text(\"记录艺术家提问\", \"Record Artist question\"),\n                            onPrevious = onBack,\n                            onHostTools = onHostTools,\n                            onNext = onPrimary,\n                            nextEnabled = actionsEnabled && primaryEnabled,\n                        )\n""",
    label="Artist bottom bar",
)
write(ARTIST, artist)

# Klutz has no safe back owner after the public choice has entered this surface.
klutz = read(KLUTZ)
klutz = insert_function_param(klutz, "ClocktowerKlutzTableScreen", "onSeatClick")
klutz = sub_exact(
    klutz,
    r"""                        Button\(\n                            onClick = onConfirm,.*?\n                        \}\n""",
    """                        HostBottomActionBar(\n                            previousLabel = text(\"上一步\", \"Previous\"),\n                            hostToolsLabel = text(\"主持工具\", \"Host Tools\"),\n                            nextLabel = text(\"确认呆瓜选择\", \"Confirm Klutz choice\"),\n                            onPrevious = {},\n                            onHostTools = onHostTools,\n                            onNext = onConfirm,\n                            previousEnabled = false,\n                            nextEnabled = actionsEnabled && tableState.choiceSeatId != null,\n                        )\n""",
    label="Klutz bottom bar",
)
write(KLUTZ, klutz)

# D6 composition owner distributes the root-owned Host Tools callback to all private host surfaces.
host = read(HOST)
host = insert_call_arg(host, "ClocktowerStorytellerRecommendationScreen", "onStartNight", expected=2)
host = insert_call_arg(host, "ClocktowerNewDemonConfirmationScreen", "onShowPlayerDisplay")
host = insert_call_arg(host, "ClocktowerDawnSummaryScreen", "onEnterDay")
host = insert_call_arg(host, "ClocktowerDayOverviewScreen", "onNominationGesture")
host = insert_call_arg(host, "ClocktowerPendingNominationTableScreen", "onContinue")
host = insert_call_arg(host, "ClocktowerVoteTableScreen", "onConfirm")
host = insert_call_arg(host, "ClocktowerExecutionConfirmScreen", "onConfirm")
host = insert_call_arg(host, "ClocktowerSlayerTableScreen", "onSeatClick")
host = insert_call_arg(host, "ClocktowerArtistTableScreen", "onSeatClick")
host = insert_call_arg(host, "ClocktowerKlutzTableScreen", "onSeatClick")
write(HOST, host)

# Postconditions: all Clocktower host surfaces are covered without leaking utility chrome to player/public displays.
assert "screen == Screen.ClocktowerJudge ||" not in read(APP)
assert "screen == Screen.WerewolfJudge ||" in read(APP)
assert "screen == Screen.Game" in read(APP)

night_after = read(NIGHT)
assert "ClocktowerNewDemonConfirmationScreen" in night_after
assert "previousEnabled = false" in night_after

rec_after = read(REC)
assert "HostBottomActionBar(" in rec_after
assert "previousEnabled = false" in rec_after

day_after = read(DAY)
public_start = day_after.index("if (showPublicAnnouncement) {")
public_end = day_after.index("return@ClocktowerDarkTheme", public_start)
public_block = day_after[public_start:public_end]
assert "HostBottomActionBar(" not in public_block
assert "onHostTools" not in public_block
assert "HostBottomActionBar(" in day_after[public_end:]

for path in (OVERVIEW, NOMINATION, VOTE, SLAYER, ARTIST, KLUTZ):
    content = read(path)
    assert "HostBottomActionBar(" in content, path
    assert "onHostTools: () -> Unit" in content, path

host_after = read(HOST)
for callee in (
    "ClocktowerStorytellerRecommendationScreen",
    "ClocktowerNewDemonConfirmationScreen",
    "ClocktowerDawnSummaryScreen",
    "ClocktowerDayOverviewScreen",
    "ClocktowerPendingNominationTableScreen",
    "ClocktowerVoteTableScreen",
    "ClocktowerExecutionConfirmScreen",
    "ClocktowerSlayerTableScreen",
    "ClocktowerArtistTableScreen",
    "ClocktowerKlutzTableScreen",
):
    marker = f"{callee}("
    assert marker in host_after, callee
assert host_after.count("onHostTools = onHostTools") >= 12

print("UI-NAV-1C.2 Clocktower host-surface patch applied")
for path in (APP, REC, HOST, NIGHT, DAY, OVERVIEW, NOMINATION, VOTE, SLAYER, ARTIST, KLUTZ):
    print(path)
