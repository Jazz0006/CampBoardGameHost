from pathlib import Path
import re

ROOT = Path("app/src/main/java/com/codex/campboardgamehost")
TEST_ROOT = Path("app/src/test/java/com/codex/campboardgamehost")


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8")


def replace_exact(path: Path, old: str, new: str, expected: int = 1, label: str = "anchor") -> None:
    text = read(path)
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{path}: {label}: expected {expected}, found {count}")
    write(path, text.replace(old, new))


def replace_regex(path: Path, pattern: str, replacement, expected: int, label: str) -> None:
    text = read(path)
    next_text, count = re.subn(pattern, replacement, text, flags=re.MULTILINE)
    if count != expected:
        raise SystemExit(f"{path}: {label}: expected {expected}, found {count}")
    write(path, next_text)


# One shared, presentation-only derivation for the durable ghost-vote authority.
ghost_projection = ROOT / "ClocktowerGhostVotePresentation.kt"
if ghost_projection.exists():
    raise SystemExit(f"{ghost_projection}: unexpected pre-existing file")
write(
    ghost_projection,
    '''package com.codex.campboardgamehost

internal fun hostSeatHasUnspentGhostVote(
    seatId: ClocktowerSeatId,
    isAlive: Boolean,
    ghostVoteAuthority: ClocktowerGhostVoteAuthority?,
): Boolean =
    !isAlive &&
        ghostVoteAuthority != null &&
        seatId !in ghostVoteAuthority.spentSeatIds
''',
)

# Carry the derived presentation state with every shared physical seat.
contract = ROOT / "ClocktowerHostTableContract.kt"
replace_exact(
    contract,
    """    val isAlive: Boolean,\n    val actualRole: HostRolePresentation? = null,\n""",
    """    val isAlive: Boolean,\n    val hasUnspentGhostVote: Boolean = false,\n    val actualRole: HostRolePresentation? = null,\n""",
    label="host seat ghost-vote presentation field",
)

projection = ROOT / "ClocktowerHostSeatContentPresentation.kt"
replace_exact(
    projection,
    """internal fun PlayerCard.toStorytellerHostSeatPresentation(\n    seatNumber: Int,\n    language: String,\n): HostSeatPresentation = HostSeatPresentation(\n""",
    """internal fun PlayerCard.toStorytellerHostSeatPresentation(\n    seatNumber: Int,\n    language: String,\n    ghostVoteAuthority: ClocktowerGhostVoteAuthority? = null,\n): HostSeatPresentation = HostSeatPresentation(\n""",
    label="night seat projection signature",
)
replace_exact(
    projection,
    """    playerName = name,\n    isAlive = eliminatedRound == null,\n    actualRole = clocktowerRole?.toHostRolePresentation(language),\n""",
    """    playerName = name,\n    isAlive = eliminatedRound == null,\n    hasUnspentGhostVote = hostSeatHasUnspentGhostVote(\n        seatId = ClocktowerSeatId(seatNumber),\n        isAlive = eliminatedRound == null,\n        ghostVoteAuthority = ghostVoteAuthority,\n    ),\n    actualRole = clocktowerRole?.toHostRolePresentation(language),\n""",
    label="night seat ghost-vote projection",
)

day_state = ROOT / "ClocktowerDayOverviewTableState.kt"
replace_exact(
    day_state,
    """internal fun clocktowerDayOverviewTableState(\n    gameState: GameState,\n    roleDisplayName: (RoleId) -> String = { roleId -> roleId.value },\n): ClocktowerDayOverviewTableState = ClocktowerDayOverviewTableState(\n    seats = gameState.toHostSeatPresentations(roleDisplayName),\n""",
    """internal fun clocktowerDayOverviewTableState(\n    gameState: GameState,\n    roleDisplayName: (RoleId) -> String = { roleId -> roleId.value },\n    ghostVoteAuthority: ClocktowerGhostVoteAuthority? = null,\n): ClocktowerDayOverviewTableState = ClocktowerDayOverviewTableState(\n    seats = gameState.toHostSeatPresentations(roleDisplayName).map { seat ->\n        seat.copy(\n            hasUnspentGhostVote = hostSeatHasUnspentGhostVote(\n                seatId = seat.seatId,\n                isAlive = seat.isAlive,\n                ghostVoteAuthority = ghostVoteAuthority,\n            ),\n        )\n    },\n""",
    label="day seat ghost-vote projection",
)

host_table = ROOT / "ClocktowerHostTableUi.kt"
replace_exact(
    host_table,
    """    seatHasUnspentGhostVote: (HostSeatPresentation) -> Boolean = { false },\n""",
    """    seatHasUnspentGhostVote: (HostSeatPresentation) -> Boolean = { seat -> seat.hasUnspentGhostVote },\n""",
    label="shared table default ghost-vote presentation",
)

# Feed durable authority into every Night shared-seat projection.
night_step = ROOT / "ClocktowerNightStepUi.kt"
replace_exact(
    night_step,
    """    cards: List<PlayerCard>,\n    aliveCards: List<PlayerCard>,\n""",
    """    cards: List<PlayerCard>,\n    ghostVoteAuthority: ClocktowerGhostVoteAuthority,\n    aliveCards: List<PlayerCard>,\n""",
    label="night step ghost-vote authority parameter",
)
replace_exact(
    night_step,
    """            card.toStorytellerHostSeatPresentation(\n                seatNumber = index + 1,\n                language = language,\n            )\n""",
    """            card.toStorytellerHostSeatPresentation(\n                seatNumber = index + 1,\n                language = language,\n                ghostVoteAuthority = ghostVoteAuthority,\n            )\n""",
    label="night action shared seat projection",
)

host_screen = ROOT / "clocktower/ui/ClocktowerHostScreen.kt"
replace_exact(
    host_screen,
    """                cards = cards,\n                aliveCards = publicAliveCards,\n""",
    """                cards = cards,\n                ghostVoteAuthority = ghostVoteAuthority,\n                aliveCards = publicAliveCards,\n""",
    label="night step authority wiring",
)

# All live Day table projections in ClocktowerJudgeScreen receive the same authority.
def add_day_authority(match: re.Match) -> str:
    indent = match.group(1)
    close_indent = match.group(2)
    return (
        f"{indent}roleDisplayName = {{ roleId -> clocktowerRoleLabel(roleId, language) }},\n"
        f"{indent}ghostVoteAuthority = ghostVoteAuthority,\n"
        f"{close_indent})"
    )

replace_regex(
    host_screen,
    r"(?m)^(\s*)roleDisplayName = \{ roleId -> clocktowerRoleLabel\(roleId, language\) \},\n(\s*)\)",
    add_day_authority,
    expected=6,
    label="all live day table authority wiring",
)

# Every direct square-table builder must forward shared life/ghost state; interaction state stays independent.
direct_builder_files = [
    "ClocktowerChefSquareTableUi.kt",
    "ClocktowerEmpathSquareTableUi.kt",
    "ClocktowerFortuneTellerSquareTableUi.kt",
    "ClocktowerNightActionSquareTableUi.kt",
    "ClocktowerPairManualSelectionUi.kt",
    "ClocktowerUndertakerSquareTableUi.kt",
]
for filename in direct_builder_files:
    path = ROOT / filename
    pattern = r"(?m)^(\s*)detailLabels = content\.detailLabels,\n(\s*)state ="
    def add_life(match: re.Match) -> str:
        first = match.group(1)
        second = match.group(2)
        if first != second:
            raise SystemExit(f"{path}: unexpected constructor indentation")
        return (
            f"{first}detailLabels = content.detailLabels,\n"
            f"{first}isAlive = seat.isAlive,\n"
            f"{first}hasUnspentGhostVote = seat.hasUnspentGhostVote,\n"
            f"{first}state ="
        )
    replace_regex(path, pattern, add_life, expected=1, label="direct square-table life-state forwarding")

# Vote screen no longer owns a special ghost-vote visual path; its authority remains for vote legality/spending.
vote_ui = ROOT / "clocktower/ui/ClocktowerVoteTableUi.kt"
replace_exact(
    vote_ui,
    """            seatHasUnspentGhostVote = { seat ->\n                !seat.isAlive && seat.seatId !in ghostVoteAuthority.spentSeatIds\n            },\n""",
    "",
    label="remove vote-only ghost marker ownership",
)

# Move the persistent ghost-vote marker to the requested top-center position.
square_ui = ROOT / "ClocktowerSquareTableUi.kt"
replace_exact(
    square_ui,
    """                        center = Offset(size.width - markerInset, markerInset),\n""",
    """                        center = Offset(size.width / 2f, markerInset),\n""",
    label="ghost marker top-center position",
)
replace_exact(
    square_ui,
    """                    modifier = Modifier\n                        .align(Alignment.TopEnd)\n                        .padding(\n                            end = if (lifeMarkers.showUnspentGhostVote) 14.dp else 3.dp,\n                            top = 2.dp,\n                        ),\n""",
    """                    modifier = Modifier\n                        .align(Alignment.TopEnd)\n                        .padding(end = 3.dp, top = 2.dp),\n""",
    label="remove obsolete upper-right marker clearance",
)

# Strengthen a pure direct-builder contract so future night pages cannot drop life/ghost presentation again.
pair_test = TEST_ROOT / "ClocktowerPairManualSeatPresentationTest.kt"
replace_exact(
    pair_test,
    """import org.junit.Assert.assertEquals\nimport org.junit.Test\n""",
    """import org.junit.Assert.assertEquals\nimport org.junit.Assert.assertFalse\nimport org.junit.Assert.assertTrue\nimport org.junit.Test\n""",
    label="pair test life assertions imports",
)
replace_exact(
    pair_test,
    """            isAlive = false,\n            actualRole = HostRolePresentation(roleId = \"Drunk\", displayName = \"Drunk\"),\n""",
    """            isAlive = false,\n            hasUnspentGhostVote = true,\n            actualRole = HostRolePresentation(roleId = \"Drunk\", displayName = \"Drunk\"),\n""",
    label="pair test shared life state fixture",
)
replace_exact(
    pair_test,
    """        assertEquals(ClocktowerSquareTableSeatState.Selectable, ui.state)\n""",
    """        assertEquals(ClocktowerSquareTableSeatState.Selectable, ui.state)\n        assertFalse(ui.isAlive)\n        assertTrue(ui.hasUnspentGhostVote)\n""",
    label="pair test shared life state assertions",
)

# Add focused authority/projection coverage.
ghost_test = TEST_ROOT / "ClocktowerGhostVotePresentationTest.kt"
if ghost_test.exists():
    raise SystemExit(f"{ghost_test}: unexpected pre-existing file")
write(
    ghost_test,
    '''package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerGhostVotePresentationTest {
    @Test
    fun `ghost vote marker derives from durable seat authority only for dead unspent seat`() {
        val seatId = ClocktowerSeatId(2)
        val unspent = ClocktowerGhostVoteAuthority()
        val spent = ClocktowerGhostVoteAuthority(spentSeatIds = setOf(seatId))

        assertTrue(hostSeatHasUnspentGhostVote(seatId, isAlive = false, ghostVoteAuthority = unspent))
        assertFalse(hostSeatHasUnspentGhostVote(seatId, isAlive = false, ghostVoteAuthority = spent))
        assertFalse(hostSeatHasUnspentGhostVote(seatId, isAlive = true, ghostVoteAuthority = unspent))
        assertFalse(hostSeatHasUnspentGhostVote(seatId, isAlive = false, ghostVoteAuthority = null))
    }

    @Test
    fun `night storyteller seat carries dead and unspent ghost vote presentation together`() {
        val deadCard = PlayerCard(
            name = "Dana",
            role = Role.Civilian,
            word = "",
            eliminatedRound = 2,
        )
        val seat = deadCard.toStorytellerHostSeatPresentation(
            seatNumber = 3,
            language = "en",
            ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
        )

        assertFalse(seat.isAlive)
        assertTrue(seat.hasUnspentGhostVote)
    }
}
''',
)

print("Applied dead-player night + persistent ghost-vote presentation patch")
