from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt")
text = path.read_text()
start = text.index("@Composable\nprivate fun ClocktowerPairPlayerRevealContent(")
end = text.index("\n@Composable\nprivate fun ClocktowerPlayerDisplayCenterContent(", start)
old = text[start:end]
new = '''@Composable
private fun ClocktowerPairPlayerRevealContent(
    presentation: ClocktowerPairPlayerRevealPresentation,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        when (presentation.displayKind) {
            ClocktowerDisplayKind.EitherOne -> {
                ClocktowerPairPlayerRevealPrimary(presentation)
                ClocktowerPairPlayerRevealFooter(presentation)
                ClocktowerPairPlayerRevealSeats(presentation, language)
            }

            ClocktowerDisplayKind.Number,
            ClocktowerDisplayKind.YesNo,
            -> {
                ClocktowerPairPlayerRevealFooter(presentation)
                ClocktowerPairPlayerRevealSeats(presentation, language)
                ClocktowerPairPlayerRevealPrimary(presentation)
            }

            else -> {
                ClocktowerPairPlayerRevealSeats(presentation, language)
                ClocktowerPairPlayerRevealPrimary(presentation)
                ClocktowerPairPlayerRevealFooter(presentation)
            }
        }
    }
}

@Composable
private fun ClocktowerPairPlayerRevealSeats(
    presentation: ClocktowerPairPlayerRevealPresentation,
    language: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        presentation.seats.forEach { seat ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1B1F25), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = clocktowerSeatNumberLabel(seat.seatId.number, language),
                    color = Color(0xFFC5A56A),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = seat.playerName,
                    color = Color(0xFFF7F1E6),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerPairPlayerRevealPrimary(
    presentation: ClocktowerPairPlayerRevealPresentation,
) {
    when (presentation.displayKind) {
        ClocktowerDisplayKind.Number,
        ClocktowerDisplayKind.YesNo,
        -> Text(
            text = presentation.primary,
            color = Color(0xFFC5A56A),
            fontSize = 60.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )

        ClocktowerDisplayKind.EitherOne -> Text(
            text = presentation.primary,
            color = Color(0xFFF7F1E6),
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )

        else -> Unit
    }
}

@Composable
private fun ClocktowerPairPlayerRevealFooter(
    presentation: ClocktowerPairPlayerRevealPresentation,
) {
    presentation.footer?.let { footer ->
        Text(
            text = footer,
            color = Color(0xFFAAA397),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
'''
path.write_text(text[:start] + new + text[end:])
