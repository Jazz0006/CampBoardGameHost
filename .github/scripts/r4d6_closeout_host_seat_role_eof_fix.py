from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt")
raw = path.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected non-LF line ending in HostTableUi")
text = raw.decode("utf-8")
if not text.endswith("\n\n"):
    raise SystemExit("Expected exactly the post-patch extra EOF blank line before normalization")
normalized = text.rstrip("\n") + "\n"
if normalized == text:
    raise SystemExit("EOF normalization made no change")
path.write_text(normalized, encoding="utf-8", newline="\n")
