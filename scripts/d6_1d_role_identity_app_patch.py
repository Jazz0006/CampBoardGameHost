from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label} anchor count was {count}, expected exactly 1")
    text = text.replace(old, new, 1)


replace_once(
    """import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
""",
    """import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary
import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary
import com.codex.campboardgamehost.clocktower.session.NightCheckpointReducer
""",
    "role identity boundary imports",
)

old_helpers = '''    fun setClocktowerActualRole(
        playerName: String,
        nextRole: ClocktowerRole,
        recordSemanticHistory: Boolean = true,
    ) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            val targetSeat = index + 1
            if (recordSemanticHistory) {
                recordClocktowerRoleChangeAction(
                    targetSeat = targetSeat,
                    nextRole = nextRole,
                    actionId = clocktowerActionId(
                        kind = "role-change-${nextRole.enName.lowercase().replace(' ', '-')}",
                        localSequence = clocktowerEventCounter + 1,
                        targetSeat = targetSeat,
                    ),
                )
            }
            advanceClocktowerGameStateRevision()
            cards[index] = cards[index].copy(
                actualRoleLabel = nextRole.nameFor(language),
                clocktowerTeam = nextRole.team,
                clocktowerRole = nextRole,
            )
        }
    }

    fun setClocktowerShownRole(playerName: String, nextRole: ClocktowerRole) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            advanceClocktowerGameStateRevision()
            cards[index] = cards[index].copy(
                roleLabel = nextRole.nameFor(language),
                clocktowerShownRole = nextRole,
                word = context.getString(
                    R.string.clocktower_card_desc_format,
                    nextRole.team.label(context),
                    nextRole.descriptionFor(language),
                ),
            )
        }
    }
'''

new_helpers = '''    fun setClocktowerActualRole(
        playerName: String,
        nextRole: ClocktowerRole,
        recordSemanticHistory: Boolean = true,
    ) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            val targetSeat = index + 1
            if (recordSemanticHistory) {
                recordClocktowerRoleChangeAction(
                    targetSeat = targetSeat,
                    nextRole = nextRole,
                    actionId = clocktowerActionId(
                        kind = "role-change-${nextRole.enName.lowercase().replace(' ', '-')}",
                        localSequence = clocktowerEventCounter + 1,
                        targetSeat = targetSeat,
                    ),
                )
            }
            requireClocktowerGameSession().commitActualRoleBoundary(
                seat = targetSeat,
                actualRole = RoleId(nextRole.enName),
                actualAlignment = when (nextRole.team) {
                    ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> ClocktowerAlignment.GOOD
                    ClocktowerTeam.Minion, ClocktowerTeam.Demon -> ClocktowerAlignment.EVIL
                },
                actualType = when (nextRole.team) {
                    ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
                    ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
                    ClocktowerTeam.Minion -> CharacterType.MINION
                    ClocktowerTeam.Demon -> CharacterType.DEMON
                },
            )
            publishClocktowerSessionView()
            invalidateA4RevisionScope()
            cards[index] = cards[index].copy(
                actualRoleLabel = nextRole.nameFor(language),
                clocktowerTeam = nextRole.team,
                clocktowerRole = nextRole,
            )
        }
    }

    fun setClocktowerShownRole(playerName: String, nextRole: ClocktowerRole) {
        val index = cards.indexOfFirst { it.name == playerName }
        if (index >= 0) {
            val targetSeat = index + 1
            requireClocktowerGameSession().commitShownRoleBoundary(
                seat = targetSeat,
                shownRole = RoleId(nextRole.enName),
            )
            publishClocktowerSessionView()
            invalidateA4RevisionScope()
            cards[index] = cards[index].copy(
                roleLabel = nextRole.nameFor(language),
                clocktowerShownRole = nextRole,
                word = context.getString(
                    R.string.clocktower_card_desc_format,
                    nextRole.team.label(context),
                    nextRole.descriptionFor(language),
                ),
            )
        }
    }
'''

replace_once(old_helpers, new_helpers, "runtime role identity helpers")

if text.count("import com.codex.campboardgamehost.clocktower.session.commitActualRoleBoundary") != 1:
    raise SystemExit("Expected exactly one commitActualRoleBoundary import")
if text.count("import com.codex.campboardgamehost.clocktower.session.commitShownRoleBoundary") != 1:
    raise SystemExit("Expected exactly one commitShownRoleBoundary import")
if text.count("requireClocktowerGameSession().commitActualRoleBoundary(") != 1:
    raise SystemExit("Expected exactly one App actual-role session boundary call")
if text.count("requireClocktowerGameSession().commitShownRoleBoundary(") != 1:
    raise SystemExit("Expected exactly one App shown-role session boundary call")

TARGET.write_text(text, encoding="utf-8", newline="\n")
