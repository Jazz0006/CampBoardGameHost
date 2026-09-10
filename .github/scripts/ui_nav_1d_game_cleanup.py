from pathlib import Path

root_path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
root = root_path.read_text(encoding="utf-8")

top_bar = '''                    if (
                        !showResults && (
                            screen == Screen.Game
                        )
                    ) {
                        HostToolsTopBar(
                            onOpen = {
                                hostToolTab = HostToolTab.Roles
                                showHostTools = true
                            },
                        )
                    }
'''
assert root.count(top_bar) == 1, "expected exactly one remaining Screen.Game top Host Tools block"
root = root.replace(top_bar, "", 1)

game_call = '''                    Screen.Game -> GameScreen(
                    gameKind = currentGameKind,
                    cards = cards,
'''
game_call_replacement = '''                    Screen.Game -> GameScreen(
                    gameKind = currentGameKind,
                    onHostTools = {
                        hostToolTab = HostToolTab.Roles
                        showHostTools = true
                    },
                    cards = cards,
'''
assert root.count(game_call) == 1, "expected exactly one generic GameScreen call"
root = root.replace(game_call, game_call_replacement, 1)
root_path.write_text(root, encoding="utf-8")

screen_path = Path("app/src/main/java/com/codex/campboardgamehost/AppGameScreen.kt")
screen = screen_path.read_text(encoding="utf-8")

signature = '''    selectedElimination: String?,
    onSelectElimination: (String) -> Unit,
    onConfirmElimination: () -> Unit,
    onShowResults: () -> Unit,
) {'''
signature_replacement = '''    selectedElimination: String?,
    onSelectElimination: (String) -> Unit,
    onConfirmElimination: () -> Unit,
    onHostTools: () -> Unit,
    onShowResults: () -> Unit,
) {'''
assert screen.count(signature) == 1, "expected exactly one GameScreen signature"
screen = screen.replace(signature, signature_replacement, 1)

old_results = '''        item {
            Button(
                onClick = onShowResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(if (gameOutcome == null) stringResource(R.string.end_and_reveal) else stringResource(R.string.view_results))
            }
        }
'''
new_results = '''        item {
            HostBottomActionBar(
                previousLabel = if (gameKind == GameKind.Clocktower) "上一步" else stringResource(R.string.back),
                hostToolsLabel = "主持工具",
                nextLabel = if (gameOutcome == null) stringResource(R.string.end_and_reveal) else stringResource(R.string.view_results),
                onPrevious = {},
                onHostTools = onHostTools,
                onNext = onShowResults,
                previousEnabled = false,
            )
        }
'''
assert screen.count(old_results) == 1, "expected exactly one legacy results button block"
screen = screen.replace(old_results, new_results, 1)

screen = screen.replace("import androidx.compose.foundation.layout.height\n", "", 1)
screen = screen.replace("import androidx.compose.material3.ButtonDefaults\n", "", 1)

assert "HostToolsTopBar(" not in root[root.find("Screen.Game -> GameScreen(") - 3000:]
assert "onHostTools: () -> Unit" in screen
assert "HostBottomActionBar(" in screen
screen_path.write_text(screen, encoding="utf-8")
