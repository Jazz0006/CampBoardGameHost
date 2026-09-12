from pathlib import Path


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected non-LF source: {path}")
    return raw.decode("utf-8")


def replace_once(path: Path, old: str, new: str) -> None:
    text = read_lf(path)
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected one anchor in {path}, found {count}")
    updated = text.replace(old, new, 1)
    path.write_text(updated, encoding="utf-8", newline="\n")


night_step = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
old_command = '''    val command = when {
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
'''
new_command = '''    val beginnerGuidance = if (automaticStorytellerInfo) {
        clocktowerBeginnerNightGuidance(
            action = step.action,
            actor = step.actor,
            cards = cards,
            language = language,
        )
    } else {
        null
    }
    val command = beginnerGuidance?.asWakeInstruction() ?: when {
        step.action == ClocktowerNightAction.FortuneTeller && step.actor != null -> {
'''
replace_once(night_step, old_command, new_command)

square_table = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt")
old_renderer = '''@Composable
internal fun ClocktowerNightActionWakeInstruction(instruction: String?) {
    LocalClocktowerNightProgress.current?.takeIf { it.isNotBlank() }?.let { value ->
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
    }
    instruction?.takeIf { it.isNotBlank() }?.let { value ->
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
    }
}
'''
new_renderer = '''@Composable
internal fun ClocktowerNightActionWakeInstruction(instruction: String?) {
    LocalClocktowerNightProgress.current?.takeIf { it.isNotBlank() }?.let { value ->
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
    }
    instruction?.takeIf { it.isNotBlank() }?.let { value ->
        val guidanceLines = value.lines()
            .map(String::trim)
            .filter(String::isNotBlank)
        val isStructuredGuidance = guidanceLines.size in 2..3 &&
            (guidanceLines.first().startsWith("唤醒 ") || guidanceLines.first().startsWith("Wake "))
        if (isStructuredGuidance) {
            Text(
                text = guidanceLines[0],
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = guidanceLines[1],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            guidanceLines.getOrNull(2)?.let { action ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(4.dp))
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}
'''
replace_once(square_table, old_renderer, new_renderer)

night_text = read_lf(night_step)
if "val beginnerGuidance = if (automaticStorytellerInfo)" not in night_text:
    raise SystemExit("Beginner guidance wiring assertion failed")
renderer_text = read_lf(square_table)
for expected in (
    "MaterialTheme.typography.labelMedium",
    "val isStructuredGuidance = guidanceLines.size in 2..3",
    "MaterialTheme.typography.titleLarge",
):
    if expected not in renderer_text:
        raise SystemExit(f"Renderer assertion failed: {expected}")
