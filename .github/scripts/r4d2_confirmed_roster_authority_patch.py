from pathlib import Path

PATH = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = PATH.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/mixed line endings in CampBoardGameHostApp.kt")
text = raw.decode("utf-8")


def replace_exact(old: str, new: str, expected_count: int = 1) -> None:
    global text
    count = text.count(old)
    if count != expected_count:
        raise SystemExit(
            f"Exact anchor mismatch: expected {expected_count}, found {count}\n--- anchor ---\n{old}"
        )
    text = text.replace(old, new)


replace_exact(
    """    fun startUndercoverGame() {
        if (playerNames.size < MIN_PLAYERS) return
""",
    """    fun startUndercoverGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Undercover)
        if (playerNames.size < MIN_PLAYERS) return
""",
)

replace_exact(
    """    fun startWerewolfGame() {
        if (playerNames.size < MIN_WEREWOLF_PLAYERS) return
""",
    """    fun startWerewolfGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Werewolf)
        if (playerNames.size < MIN_WEREWOLF_PLAYERS) return
""",
)

replace_exact(
    """    fun startTroubleBrewingGame() {
        val preparedSeed = newClocktowerSeed()
""",
    """    fun startTroubleBrewingGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
        val preparedSeed = newClocktowerSeed()
""",
)

replace_exact(
    """    fun startClocktowerGame() {
        if (playerNames.size < MIN_CLOCKTOWER_PLAYERS) return
""",
    """    fun startClocktowerGame() {
        val playerNames = hostSeatingSetupFlow.playerNamesFor(GameKind.Clocktower)
        if (playerNames.size < MIN_CLOCKTOWER_PLAYERS) return
""",
)

PATH.write_text(text, encoding="utf-8", newline="\n")
