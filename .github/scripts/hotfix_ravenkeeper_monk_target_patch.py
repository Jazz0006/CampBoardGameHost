from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit normalization")
text = raw.decode("utf-8")

monk_old = '''            ClocktowerNightAction.MonkProtect -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
monk_new = '''            ClocktowerNightAction.MonkProtect -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                ) {
                    SelectablePlayerChips(
                        cards = clocktowerMonkTargetCards(cards, step.actor?.name),
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''

raven_old = '''            ClocktowerNightAction.Ravenkeeper -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
raven_new = '''            ClocktowerNightAction.Ravenkeeper -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                ) {
                    SelectablePlayerChips(
                        cards = clocktowerRavenkeeperTargetCards(cards),
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''

for label, old in [("Monk", monk_old), ("Ravenkeeper", raven_old)]:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")

text = text.replace(monk_old, monk_new, 1)
text = text.replace(raven_old, raven_new, 1)

for token in [
    "cards = clocktowerMonkTargetCards(cards, step.actor?.name)",
    "cards = clocktowerRavenkeeperTargetCards(cards)",
]:
    if token not in text:
        raise SystemExit(f"Missing required postcondition: {token}")

path.write_text(text, encoding="utf-8", newline="\n")
