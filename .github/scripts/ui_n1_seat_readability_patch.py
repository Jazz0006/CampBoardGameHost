from pathlib import Path


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF line endings in {path}")
    return raw.decode("utf-8")


def replace_exact(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


def write_lf(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="\n")


path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt")
text = read_lf(path)

text = replace_exact(
    text,
    "import androidx.compose.ui.platform.LocalContext\n",
    "",
    "remove square-table LocalContext import",
)

text = replace_exact(
    text,
    "    val language = LocalContext.current.resources.configuration.locales[0].language\n",
    "",
    "remove square-table badge language lookup",
)

old_content = '''    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = density.horizontalPaddingDp.dp,
                    vertical = density.verticalPaddingDp.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (seat.isCurrentActor) {
                    Text(
                        text = "➤",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 2.dp),
                    )
                }
                clocktowerSquareTableStateMarker(seat.state)?.let { marker ->
                    Text(
                        text = marker,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 1.dp),
                    )
                }
                ClocktowerSeatNumberBadge(
                    seatNumber = seat.seatNumber,
                    languageCode = language,
                    scale = ClocktowerSeatNumberBadgeScale.Compact,
                    contentColor = palette.content,
                    containerColor = palette.content.copy(alpha = 0.08f),
                    borderColor = palette.content.copy(alpha = 0.45f),
                )
                seat.badge?.let { badge ->
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }
            }
            Text(
                text = seat.label,
                maxLines = density.primaryMaxLines,
                overflow = TextOverflow.Ellipsis,
                fontSize = density.primaryFontSizeSp.sp,
                lineHeight = density.primaryLineHeightSp.sp,
                fontWeight = if (seat.isCurrentActor || seat.state in setOf(
                        ClocktowerSquareTableSeatState.SelectedFirst,
                        ClocktowerSquareTableSeatState.SelectedSecond,
                        ClocktowerSquareTableSeatState.Selected,
                        ClocktowerSquareTableSeatState.SelectedHighlighted,
                        ClocktowerSquareTableSeatState.HighlightedInformation,
                    )
                ) {
                    FontWeight.Black
                } else {
                    FontWeight.SemiBold
                },
            )
            seat.detailLabels.forEach { detail ->
                Text(
                    text = detail,
                    maxLines = density.detailMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = density.detailFontSizeSp.sp,
                    lineHeight = density.detailLineHeightSp.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
'''

new_content = '''    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = seat.seatNumber.toString(),
                color = palette.content.copy(alpha = 0.10f),
                fontSize = (density.cardWidth * 0.45f).sp,
                lineHeight = (density.cardWidth * 0.48f).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                modifier = Modifier.align(Alignment.Center),
            )

            if (seat.isCurrentActor) {
                Text(
                    text = "➤",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 3.dp, top = 2.dp),
                )
            }

            clocktowerSquareTableStateMarker(seat.state)?.let { marker ->
                Text(
                    text = marker,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 3.dp, top = 2.dp),
                )
            }

            seat.badge?.let { badge ->
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 3.dp, bottom = 2.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = density.horizontalPaddingDp.dp,
                        vertical = density.verticalPaddingDp.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = seat.label,
                    maxLines = density.primaryMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = density.primaryFontSizeSp.sp,
                    lineHeight = density.primaryLineHeightSp.sp,
                    fontWeight = if (seat.isCurrentActor || seat.state in setOf(
                            ClocktowerSquareTableSeatState.SelectedFirst,
                            ClocktowerSquareTableSeatState.SelectedSecond,
                            ClocktowerSquareTableSeatState.Selected,
                            ClocktowerSquareTableSeatState.SelectedHighlighted,
                            ClocktowerSquareTableSeatState.HighlightedInformation,
                        )
                    ) {
                        FontWeight.Black
                    } else {
                        FontWeight.SemiBold
                    },
                )
                seat.detailLabels.forEach { detail ->
                    Text(
                        text = detail,
                        maxLines = density.detailMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = density.detailFontSizeSp.sp,
                        lineHeight = density.detailLineHeightSp.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
'''

text = replace_exact(
    text,
    old_content,
    new_content,
    "replace square-table seat content with overlay presentation",
)

if "ClocktowerSeatNumberBadge(" in text:
    raise SystemExit("Compact seat-number badge still present in square-table seat renderer")
if "LocalContext" in text:
    raise SystemExit("LocalContext unexpectedly remains in square-table renderer")
if "text = seat.seatNumber.toString()" not in text:
    raise SystemExit("Seat-number watermark was not installed")
if ".align(Alignment.TopStart)" not in text:
    raise SystemExit("Actor overlay cue was not installed")

write_lf(path, text)
