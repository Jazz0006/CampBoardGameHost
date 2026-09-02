from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")

OLD = '''    fun moveCurrentPlayerTo(index: Int, insertIndex: Int) {
        if (index !in playerNames.indices) return
        val name = playerNames.removeAt(index)
        val adjustedIndex = if (insertIndex > index) insertIndex - 1 else insertIndex
        playerNames.add(adjustedIndex.coerceIn(0, playerNames.size), name)
    }
'''

NEW = '''    fun moveCurrentPlayerTo(index: Int, targetIndex: Int) {
        if (index !in playerNames.indices || targetIndex !in playerNames.indices) return
        if (index == targetIndex) return
        val reordered = reorderHostTableItems(
            items = playerNames,
            fromIndex = index,
            targetIndex = targetIndex,
        )
        playerNames.clear()
        playerNames.addAll(reordered)
    }
'''

text = TARGET.read_text(encoding="utf-8")
count = text.count(OLD)
if count != 1:
    raise SystemExit(f"Expected exactly one moveCurrentPlayerTo anchor, found {count}")
TARGET.write_text(text.replace(OLD, NEW, 1), encoding="utf-8")
print("Patched moveCurrentPlayerTo to use final target-index semantics.")
