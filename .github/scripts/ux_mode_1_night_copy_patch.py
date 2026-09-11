from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
raw = path.read_bytes()
if b"\r\n" in raw:
    raise SystemExit("ClocktowerNightStepUi.kt must remain LF-only")
text = raw.decode("utf-8")

replacements = {
    "The automatic mode selected this information. Use the button below to show it.":
        "The recommended information has been selected automatically. Use the button below to show it.",
    "已按当前自动模式选定信息；点击下方按钮即可向玩家展示。":
        "推荐信息已自动选定；点击下方按钮即可向玩家展示。",
    "The balanced option is the default; other options apply different pressure. Choosing one also updates this interaction's Spy or Recluse registration.":
        "The recommended information is ready. Choose another legal option only if you want to intervene manually. Choosing one also updates this interaction's Spy or Recluse registration.",
    "平衡方案适合直接采用；其他方案提供不同压力。选择后会同步本次间谍或隐士登记。":
        "推荐信息已就绪；仅在需要手动干预时选择其他合法信息。选择后会同步本次间谍或隐士登记。",
}

for old, new in replacements.items():
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"expected exactly one anchor for {old!r}, found {count}")
    text = text.replace(old, new, 1)

for old in replacements:
    if old in text:
        raise SystemExit(f"legacy copy survived: {old!r}")
for new in replacements.values():
    if new not in text:
        raise SystemExit(f"replacement copy missing: {new!r}")

path.write_text(text, encoding="utf-8", newline="\n")
