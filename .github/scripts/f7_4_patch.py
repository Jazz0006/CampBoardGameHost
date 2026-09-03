from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/SeatingFirstSetupUi.kt")
text = path.read_text()

old = '''            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackToSeating) {
                    Text(text("重新安排座位", "Edit seats"))
                }
                Text(
                    text = text("选择游戏", "Choose game"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(text("${playerCount}人", "$playerCount players"))
            }

            HostTableShell(
                seats = seating.toHostSeatPresentations(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onOpenClocktowerSettings,
                        enabled = playerCount >= MIN_CLOCKTOWER_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("血染钟楼", "Blood on the Clocktower"))
                    }
                    OutlinedButton(
                        onClick = onOpenUndercoverSettings,
                        enabled = playerCount >= MIN_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("谁是卧底", "Who is Undercover"))
                    }
                    OutlinedButton(
                        onClick = onOpenWerewolfSettings,
                        enabled = playerCount >= MIN_WEREWOLF_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("狼人杀", "Werewolf"))
                    }
                }
            }
'''

new = '''            HostTableShell(
                seats = seating.toHostSeatPresentations(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = text("选择游戏", "Choose game"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = text("${playerCount}人", "$playerCount players"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onOpenClocktowerSettings,
                        enabled = playerCount >= MIN_CLOCKTOWER_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("血染钟楼", "Blood on the Clocktower"))
                    }
                    OutlinedButton(
                        onClick = onOpenUndercoverSettings,
                        enabled = playerCount >= MIN_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("谁是卧底", "Who is Undercover"))
                    }
                    OutlinedButton(
                        onClick = onOpenWerewolfSettings,
                        enabled = playerCount >= MIN_WEREWOLF_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("狼人杀", "Werewolf"))
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ) {}
                    TextButton(
                        onClick = onBackToSeating,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("重新安排座位", "Edit seats"))
                    }
                }
            }
'''

count = text.count(old)
if count != 1:
    raise SystemExit(f"expected exactly one Game Selection anchor, found {count}")

path.write_text(text.replace(old, new, 1))
