from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


text = TARGET.read_text(encoding="utf-8")

text = replace_once(
    text,
    """    val motionKey: String = seatId,\n    val badge: String? = null,\n)\n""",
    """    val motionKey: String = seatId,\n    val badge: String? = null,\n    val isAlive: Boolean = true,\n    val hasUnspentGhostVote: Boolean = false,\n)\n""",
    "seat life-state fields",
)

text = replace_once(
    text,
    """    val palette = clocktowerSquareTableSeatPalette(seat.state)\n    val clickModifier = if (canSelect) {\n""",
    """    val palette = clocktowerSquareTableSeatPalette(seat.state)\n    val lifeMarkers = clocktowerSquareTableLifeMarkers(\n        isAlive = seat.isAlive,\n        hasUnspentGhostVote = seat.hasUnspentGhostVote,\n    )\n    val deathMarkerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.72f)\n    val ghostVoteMarkerColor = MaterialTheme.colorScheme.error\n    val clickModifier = if (canSelect) {\n""",
    "seat life-marker presentation",
)

text = replace_once(
    text,
    """    ) {\n        Box(\n            modifier = Modifier.fillMaxSize(),\n        ) {\n            Text(\n                text = seat.seatNumber.toString(),\n""",
    """    ) {\n        Box(\n            modifier = Modifier.fillMaxSize(),\n        ) {\n            if (lifeMarkers.showDeathCross) {\n                Canvas(\n                    modifier = Modifier\n                        .fillMaxSize()\n                        .padding(5.dp),\n                ) {\n                    val strokeWidth = 4.dp.toPx()\n                    drawLine(\n                        color = deathMarkerColor,\n                        start = Offset.Zero,\n                        end = Offset(size.width, size.height),\n                        strokeWidth = strokeWidth,\n                    )\n                    drawLine(\n                        color = deathMarkerColor,\n                        start = Offset(size.width, 0f),\n                        end = Offset(0f, size.height),\n                        strokeWidth = strokeWidth,\n                    )\n                }\n            }\n\n            if (lifeMarkers.showUnspentGhostVote) {\n                Canvas(modifier = Modifier.fillMaxSize()) {\n                    val markerInset = 7.dp.toPx()\n                    drawCircle(\n                        color = ghostVoteMarkerColor,\n                        radius = 4.dp.toPx(),\n                        center = Offset(size.width - markerInset, markerInset),\n                    )\n                }\n            }\n\n            Text(\n                text = seat.seatNumber.toString(),\n""",
    "dead-seat cross and ghost-vote dot",
)

text = replace_once(
    text,
    """            clocktowerSquareTableStateMarker(seat.state)?.let { marker ->\n                Text(\n                    text = marker,\n                    fontSize = 14.sp,\n                    fontWeight = FontWeight.Black,\n                    modifier = Modifier\n                        .align(Alignment.TopEnd)\n                        .padding(end = 3.dp, top = 2.dp),\n                )\n            }\n""",
    """            clocktowerSquareTableStateMarker(seat.state)?.let { marker ->\n                Text(\n                    text = marker,\n                    fontSize = 14.sp,\n                    fontWeight = FontWeight.Black,\n                    modifier = Modifier\n                        .align(Alignment.TopEnd)\n                        .padding(\n                            end = if (lifeMarkers.showUnspentGhostVote) 14.dp else 3.dp,\n                            top = 2.dp,\n                        ),\n                )\n            }\n""",
    "state-marker ghost-vote clearance",
)

TARGET.write_text(text, encoding="utf-8")
print(f"Patched {TARGET}")
