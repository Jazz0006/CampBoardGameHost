from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

OLD = """    val latestPersistActiveGameState by rememberUpdatedState { persistAndReleaseA4ObservationRebuildIfDurable(force = true) }\n\n    DisposableEffect(lifecycleOwner) {\n        val observer = LifecycleEventObserver { _, event ->\n            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {\n                latestPersistActiveGameState()\n            }\n        }\n"""

NEW = """    val latestPersistActiveGameState by rememberUpdatedState { force: Boolean ->\n        persistAndReleaseA4ObservationRebuildIfDurable(force = force)\n    }\n\n    DisposableEffect(lifecycleOwner) {\n        val observer = LifecycleEventObserver { _, event ->\n            persistRecoveryForLifecycleEvent(event, latestPersistActiveGameState)\n        }\n"""

text = TARGET.read_text(encoding="utf-8")
count = text.count(OLD)
if count != 1:
    raise SystemExit(f"expected exactly one lifecycle persistence anchor, found {count}")
TARGET.write_text(text.replace(OLD, NEW, 1), encoding="utf-8")
