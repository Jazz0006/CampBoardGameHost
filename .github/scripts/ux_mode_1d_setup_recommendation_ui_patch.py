from pathlib import Path
import re

path = Path('app/src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt')
s = path.read_text()

pattern = re.compile(
    r'\n    fun styleName\(style: RecommendationStyle\): String = when \(style\) \{.*?\n    \}\n',
    re.S,
)
s, count = pattern.subn('\n', s, count=1)
if count != 1:
    raise SystemExit(f'styleName removal count={count}')

old = '    var showOtherPlans by remember { mutableStateOf(false) }\n'
if old not in s:
    raise SystemExit('showOtherPlans state not found')
s = s.replace(old, '', 1)

old = '''            Text(
                if (automaticStorytellerInfo) {
                    text("全自动模式已采用平衡方案，不显示其他候选裁定。", "Automatic mode has applied the balanced plan; alternative rulings are hidden.")
                } else {
                    text("默认选择平衡方案；熟练说书人可比较三种风格。", "Balanced is the default; experienced Storytellers can compare all three styles.")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
'''
new = '''            Text(
                if (automaticStorytellerInfo) {
                    text("系统推荐已自动采用，无需选择。", "The system recommendation is applied automatically; no choice is required.")
                } else {
                    text("系统推荐已就绪；如有需要，可手动调整具体裁定。", "The system recommendation is ready; adjust individual rulings only when needed.")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
'''
if old not in s:
    raise SystemExit('legacy recommendation intro not found')
s = s.replace(old, new, 1)

pattern = re.compile(
    r'''\n            if \(!automaticStorytellerInfo && plans\.isNotEmpty\(\) && showOtherPlans\) \{.*?\n            \}\n            if \(!automaticStorytellerInfo && plans\.size > 1\) \{.*?\n            \}\n''',
    re.S,
)
s, count = pattern.subn('\n', s, count=1)
if count != 1:
    raise SystemExit(f'legacy style controls removal count={count}')

old = '        "candidate-seat-spacing" -> text("候选座位距离符合当前风格", "Candidate spacing fits this recommendation style")\n'
new = '        "candidate-seat-spacing" -> text("候选座位距离符合推荐目标", "Candidate spacing fits the recommendation")\n'
if old not in s:
    raise SystemExit('candidate spacing copy not found')
s = s.replace(old, new, 1)

old = '                RecommendationUiState.Empty -> Text(text("当前配置没有找到合法推荐，请使用首夜手动流程。", "No legal recommendation was found; use the manual first-night flow."), color = MaterialTheme.colorScheme.secondary)\n'
new = '''                RecommendationUiState.Empty -> Text(
                    if (automaticStorytellerInfo) {
                        text("当前配置没有找到合法推荐，请检查当前配置。", "No legal recommendation was found; check the current setup.")
                    } else {
                        text("当前配置没有找到合法推荐，可检查配置或手动调整。", "No legal recommendation was found; check the setup or adjust it manually.")
                    },
                    color = MaterialTheme.colorScheme.secondary,
                )
'''
if old not in s:
    raise SystemExit('empty-state copy not found')
s = s.replace(old, new, 1)

old = '''                        TextButton(onClick = { showDetails = !showDetails }, modifier = Modifier.weight(1f)) {
                            Text(if (showDetails) text("收起理由", "Hide reasons") else text("查看推荐理由", "Why this plan"))
                        }
'''
new = '''                        if (!automaticStorytellerInfo) {
                            TextButton(onClick = { showDetails = !showDetails }, modifier = Modifier.weight(1f)) {
                                Text(if (showDetails) text("收起理由", "Hide reasons") else text("查看推荐理由", "Why this plan"))
                            }
                        }
'''
if old not in s:
    raise SystemExit('reason button not found')
s = s.replace(old, new, 1)

old = '''                    Text(
                        if (automaticStorytellerInfo) {
                            text("以下首夜步骤将直接使用当前自动模式的信息。", "The first-night steps below will use the selected automatic style.")
                        } else {
                            text("采用后仍可在下方首夜步骤中手动修改具体裁定。", "After applying, you can still edit individual decisions in the first-night steps below.")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
'''
new = '''                    Text(
                        if (automaticStorytellerInfo) {
                            text("系统推荐将在首夜流程中自动使用。", "The system recommendation will be used automatically during the first night.")
                        } else {
                            text("需要时可修改具体裁定；所有调整仍使用相同的合法性校验。", "Adjust individual rulings when needed; all changes use the same legality checks.")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
'''
if old not in s:
    raise SystemExit('legacy bottom copy not found')
s = s.replace(old, new, 1)

path.write_text(s)
