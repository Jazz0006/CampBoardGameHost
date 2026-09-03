from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
source = path.read_text()

artist_marker = "    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Artist) {"
klutz_marker = "    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Klutz) {"
artist_start = source.index(artist_marker)
artist_end = source.index(klutz_marker, artist_start)
block = source[artist_start:artist_end]

old = '''        ClocktowerSpecialDayActionScreen(
            round = round,
            title = text("艺术家提问", "Artist question"),
            primaryLabel = text("记录艺术家提问", "Record Artist question"),
            primaryEnabled = artistClaimantName != null &&
                artistTruthfulAnswer != null &&
                artistShownAnswer != null &&
                gameOutcome == null,
            onPrimary = onConfirmArtistQuestion,
            onBack = {
                onSelectArtistClaimant(null)
                dayMode = ClocktowerDayMode.Overview
            },
        ) {
            HostActionSection(title = text("选择提问者", "Choose claimant")) {
                SelectablePlayerChips(
                    cards = artistClaimantCandidates,
                    selectedName = artistClaimantName,
                    enabled = gameOutcome == null,
                    allCards = cards,
                    onSelect = { onSelectArtistClaimant(if (artistClaimantName == it) null else it) },
                )
            }
'''

new = '''        val artistTableState = clocktowerArtistTableState(
            seats = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
            ).seats,
            claimantCandidateNames = artistClaimantCandidates.mapTo(mutableSetOf()) { it.name },
            claimantName = artistClaimantName,
        )
        ClocktowerArtistTableScreen(
            round = round,
            tableState = artistTableState,
            actionsEnabled = gameOutcome == null,
            primaryEnabled = artistClaimantName != null &&
                artistTruthfulAnswer != null &&
                artistShownAnswer != null &&
                gameOutcome == null,
            onSeatClick = { seatId ->
                val claimant = artistTableState.playerNameForSeat(seatId)
                onSelectArtistClaimant(if (artistClaimantName == claimant) null else claimant)
            },
            onPrimary = onConfirmArtistQuestion,
            onBack = {
                onSelectArtistClaimant(null)
                dayMode = ClocktowerDayMode.Overview
            },
        ) {
'''

if block.count(old) != 1:
    raise SystemExit(f"Expected exactly one active Artist selector block, found {block.count(old)}")

migrated = block.replace(old, new)
if "SelectablePlayerChips(\n                    cards = artistClaimantCandidates" in migrated:
    raise SystemExit("Legacy Artist claimant chips remain in active Artist block")
if "ClocktowerArtistTableScreen(" not in migrated:
    raise SystemExit("Artist persistent-table owner was not installed")

path.write_text(source[:artist_start] + migrated + source[artist_end:])
