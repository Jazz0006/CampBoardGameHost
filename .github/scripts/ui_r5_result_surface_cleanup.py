from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
source = path.read_text(encoding="utf-8")

non_pair_declaration = '''    val nonPairResultFirstCandidates = resultFirstRegistrationCandidates.takeUnless {
        step.roleEnName in setOf("Washerwoman", "Librarian", "Investigator") ||
            step.action == ClocktowerNightAction.FortuneTeller
    }.orEmpty()

'''
assert source.count(non_pair_declaration) == 1
source = source.replace(non_pair_declaration, "", 1)

non_pair_block = '''            if (!usesSpySquareTable && !usesClockmakerSquareTable && !usesSageSquareTable && !usesRavenkeeperSquareTable && !usesUndertakerSquareTable && !usesNumericSquareTable && nonPairResultFirstCandidates.isNotEmpty()) {
                Text(
                    if (language == "en") "Choose the final information" else "选择最终展示信息",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (language == "en") {
                        "Any Spy or Recluse registration needed for the chosen result is resolved automatically."
                    } else {
                        "选择结果即可；该结果所需的间谍或隐士登记会自动完成。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                nonPairResultFirstCandidates.forEach { option ->
                    OutlinedButton(
                        onClick = { showRecommendedDisplayOption(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(option.label)
                    }
                }
            }

'''
assert source.count(non_pair_block) == 1
source = source.replace(non_pair_block, "", 1)

fortune_only_guard = '''                step.action != ClocktowerNightAction.FortuneTeller
            ) {'''
chambermaid_guard = '''                step.action != ClocktowerNightAction.FortuneTeller &&
                step.action != ClocktowerNightAction.Chambermaid
            ) {'''
assert source.count(fortune_only_guard) == 2
source = source.replace(fortune_only_guard, chambermaid_guard)

assert "nonPairResultFirstCandidates" not in source
assert source.count("step.action != ClocktowerNightAction.Chambermaid") >= 3
path.write_text(source, encoding="utf-8")
