from pathlib import Path

host = Path('app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt')
text = host.read_text()
text = text.replace(
    '?.mapTo(mutableSetOf())(ClocktowerSeatId::renderKey)',
    '?.mapTo(mutableSetOf()) { seatId -> seatId.renderKey() }',
)
host.write_text(text)

square = Path('app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt')
text = square.read_text()
text = text.replace(
    '''                    val angle = atan2(deltaY, deltaX)\n                    val headLength = 12.dp.toPx()\n                    val headSpread = 0.55f\n                    val firstHead = Offset(\n                        x = end.x - headLength * cos(angle - headSpread),\n                        y = end.y - headLength * sin(angle - headSpread),\n                    )\n                    val secondHead = Offset(\n                        x = end.x - headLength * cos(angle + headSpread),\n                        y = end.y - headLength * sin(angle + headSpread),\n                    )\n''',
    '''                    val angle = atan2(deltaY.toDouble(), deltaX.toDouble())\n                    val headLength = 12.dp.toPx()\n                    val headSpread = 0.55\n                    val firstHead = Offset(\n                        x = (end.x - headLength * cos(angle - headSpread)).toFloat(),\n                        y = (end.y - headLength * sin(angle - headSpread)).toFloat(),\n                    )\n                    val secondHead = Offset(\n                        x = (end.x - headLength * cos(angle + headSpread)).toFloat(),\n                        y = (end.y - headLength * sin(angle + headSpread)).toFloat(),\n                    )\n''',
)
square.write_text(text)
