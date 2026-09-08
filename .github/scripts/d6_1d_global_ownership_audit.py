from pathlib import Path

APP = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
text = APP.read_text(encoding="utf-8")
lines = text.splitlines()


def hits(token: str):
    return [(i + 1, line.strip()) for i, line in enumerate(lines) if token in line]


def show(label: str, token: str):
    found = hits(token)
    print(f"AUDIT {label}: {len(found)}")
    for lineno, line in found:
        print(f"  L{lineno}: {line}")
    return found


def require(condition: bool, message: str):
    if not condition:
        raise SystemExit(f"AUDIT FAIL: {message}")


def require_preceded(anchor: str, required: str, window: int = 900):
    start = 0
    seen = 0
    while True:
        pos = text.find(anchor, start)
        if pos < 0:
            break
        seen += 1
        before = text[max(0, pos - window):pos]
        require(required in before, f"{anchor!r} occurrence {seen} is not preceded by {required!r}")
        start = pos + len(anchor)
    require(seen > 0, f"expected anchor not found: {anchor}")

print("=== D6.1d GLOBAL OWNERSHIP INVENTORY ===")
show("death session sync calls", "synchronizePlayerDeathWithinCurrentRevision(")
show("poison commit calls", "commitPoisonTargetBoundary(")
show("poison +0 sync calls", "synchronizePoisonTargetWithinCurrentRevision(")
show("actual-role commit calls", "commitActualRoleBoundary(")
show("shown-role commit calls", "commitShownRoleBoundary(")
show("eliminatedRound writes", "eliminatedRound =")
show("confirmed poison assignments", "clocktowerConfirmedPoisonTarget =")
show("poison draft assignments", "clocktowerPoisonTarget =")
show("game-state revision advances", "advanceClocktowerGameStateRevision()")
show("session replacements", "clocktowerGameSession =")
show("production session creation", "ClocktowerGameSession.createProduction(")
show("production session restore", "ClocktowerGameSession.restoreProduction(")

# Session-owned canonical fields must not regain writable App-root authority.
for forbidden in (
    "var clocktowerGameStateRevision",
    "var clocktowerPlayerInputRevision",
    "var clocktowerActionTimeline",
    "mutableStateListOf<RecordedEpistemicObservation>",
    "clocktowerEpistemicObservations.clear()",
    "clocktowerEpistemicObservations.addAll(",
):
    require(forbidden not in text, f"forbidden parallel authority returned: {forbidden}")

# Expected production canonical boundary topology after D6.1d.
require(text.count("synchronizePlayerDeathWithinCurrentRevision(") == 4,
        "expected exactly four Clocktower production death sync calls")
require(text.count("commitPoisonTargetBoundary(") == 1,
        "expected exactly one +1 poison commit call")
require(text.count("synchronizePoisonTargetWithinCurrentRevision(") == 3,
        "expected exactly three +0 poison sync calls")
require(text.count("commitActualRoleBoundary(") == 1,
        "expected one active actual-role session boundary")
require(text.count("commitShownRoleBoundary(") == 1,
        "expected one active shown-role session boundary")

# Every audited active Clocktower death mirror must follow canonical session mutation.
for anchor in (
    "cards[targetIndex] = targetCard.copy(eliminatedRound = round)",
    "cards[index] = nominatorCard.copy(eliminatedRound = round)",
    "cards[index] = executedCard.copy(eliminatedRound = round)",
    "cards[index] = nightDeathCard.copy(eliminatedRound = round)",
):
    require(text.count(anchor) == 1, f"expected one active Clocktower death mirror: {anchor}")
    require_preceded(anchor, "synchronizePlayerDeathWithinCurrentRevision(")

# Poison mirrors are projections: the live materialization anchors must follow session ownership.
require_preceded(
    "clocktowerConfirmedPoisonTarget = transaction.checkpoint.confirmedPoisonTarget",
    "commitPoisonTargetBoundary(targetSeat)",
)
require_preceded(
    "clocktowerConfirmedPoisonTarget = poisonTargetName",
    "synchronizePoisonTargetWithinCurrentRevision(",
)

# Active role mirrors must follow their session boundaries.
require_preceded("clocktowerRole = nextRole", "commitActualRoleBoundary(", window=1800)
require_preceded("clocktowerShownRole = nextRole", "commitShownRoleBoundary(", window=1400)

# Recovery must reconstruct the canonical owner before applying downstream Clocktower mechanics mirrors.
restore_pos = text.find("clocktowerGameSession = ClocktowerGameSession.restoreProduction(")
mechanics_pos = text.find("clocktowerConfirmedPoisonTarget = mechanics.confirmedPoisonTarget", restore_pos)
require(restore_pos >= 0 and mechanics_pos > restore_pos,
        "Recovery must restore session owner before mechanics mirrors")

# Semantic history remains routed through the session owner.
require("requireClocktowerGameSession().commitGlobalActionFact(draft)" in text,
        "ActionFact production writes must route through session")
require("session.commitGlobalEpistemicObservation(draft)" in text,
        "global epistemic observation writes must route through session")
require("requireClocktowerGameSession().preflightGlobalEpistemicObservation(" in text,
        "epistemic preflight must route through session")

print("AUDIT PASS: App-root canonical writer topology is single-owner and mirrors follow session boundaries.")
