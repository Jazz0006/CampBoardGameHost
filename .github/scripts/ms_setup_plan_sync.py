from pathlib import Path

roadmap_path = Path('docs/CURRENT_DEVELOPMENT_ROADMAP.md')
readme_path = Path('docs/README.md')


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b'\r\n' in raw or b'\r' in raw:
        raise SystemExit(f'Unexpected non-LF line endings in {path}')
    return raw.decode('utf-8')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected exactly one anchor, found {count}')
    return text.replace(old, new, 1)

roadmap = read_lf(roadmap_path)
old_top = '''```text
main baseline:
0eafa9770ca9391928419dadf835f17a1ab00d29

current branch:
codex/trouble-brewing-setup-presets-v2

current Draft PR / CI carrier:
PR #57 — TBSP: integrate Trouble Brewing setup presets
OPEN / DRAFT / NOT MERGED

last fully validated logical acceptance checkpoint:
45a60a3c32c7471c68d89b7fb886c4dbb00f1781

accepted production checkpoint under that gate:
4c8108c91be188d33435233efb9aba26397f6b87

checkpoint meaning:
TBSP-6L provenance durability repair accepted
CI #1167 / run 33344886176 SUCCESS
Android :app:testFull + :app:assembleDebug SUCCESS
ASP contract tests SUCCESS
Real Clingo cross-validation SUCCESS
R2 #1090 / run 33344886170 SUCCESS

current work:
TBSP campaign COMPLETE
MS-SETUP generic multi-script setup architecture — NEXT / NOT STARTED

active implementation handoff:
none yet — begin MS-SETUP with a fresh live-state audit and explicit architecture slice before production changes

normative TBSP rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md

final TBSP acceptance checkpoint:
docs/TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md
```'''
new_top = '''```text
merged main code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

merged PR:
PR #57 — TBSP: integrate Trouble Brewing setup presets
MERGED / CLOSED

post-merge full validation:
CI #1179 / run 33346311357 SUCCESS
Android :app:testFull + :app:assembleDebug SUCCESS
ASP contract tests SUCCESS
Real Clingo cross-validation SUCCESS
CI aggregate gate SUCCESS

accepted TBSP production checkpoint:
4c8108c91be188d33435233efb9aba26397f6b87

final pre-merge T4 checkpoint:
45a60a3c32c7471c68d89b7fb886c4dbb00f1781

current work:
TBSP campaign COMPLETE / MERGED
MS-SETUP generic multi-script setup architecture — CURRENT PLANNING CAMPAIGN
MS-S0 fresh live-state + ownership audit — NEXT

active implementation handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md

normative TBSP rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md

final TBSP acceptance checkpoint:
docs/TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md
```'''
roadmap = replace_once(roadmap, old_top, new_top, 'roadmap live context')
roadmap = replace_once(
    roadmap,
    'PR #57 remains Draft and unmerged. Do not mark Ready or merge without explicit authorization.',
    'PR #57 is merged into `main` at `98ee982ef3590822cd06ac72a047b49afac3cfd6`. MS-SETUP must continue from fresh live `main` on a new branch after MS-S0 ownership audit.',
    'roadmap merged status',
)
roadmap = replace_once(
    roadmap,
    'MS-SETUP generic multi-script setup architecture             NEXT / NOT STARTED',
    'MS-SETUP generic multi-script setup architecture             CURRENT PLANNING CAMPAIGN\nMS-S0 fresh live-state + ownership audit                       NEXT',
    'roadmap campaign table',
)
roadmap_path.write_text(roadmap, encoding='utf-8', newline='\n')

readme = read_lf(readme_path)
readme = replace_once(readme, '> 最后整理：2026-08-30 Australia/Sydney', '> 最后整理：2026-08-31 Australia/Sydney', 'README date')
start = readme.index('## 2. 当前 active task')
end = readme.index('## 5. Active long-lived architecture / semantic references')
replacement = '''## 2. 当前 active task

当前最高优先级：

```text
MS-SETUP — Generic Multi-Script Setup Architecture
MS-S0 — fresh live-state + ownership audit
```

Active handoff：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md) — **CURRENT HANDOFF**

Merged baseline：

```text
PR #57 — TBSP: integrate Trouble Brewing setup presets
MERGED

main merge checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

post-merge full CI:
CI #1179 / run 33346311357 — SUCCESS
```

Always re-query live `main` before implementation. The current task is planning/audit first; do not begin MS-S1 production work before MS-S0 completes.

## 3. Current setup-architecture references

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md) — current generic setup architecture handoff；
- [`TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`](TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md) — accepted Trouble Brewing behavior to preserve during genericization；
- [`TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`](TBSP_ROTATION_WEIGHT_CONTRACT_V1.md) — accepted TB diversity/rotation semantics；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 validation strategy.

Frozen Trouble Brewing preset dataset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Do not regenerate or reformat it during MS-SETUP genericization.

## 4. Current MS-SETUP sequence

```text
MS-S0  live-state + ownership audit                          NEXT
MS-S1  generic CommittedClocktowerSetup / provenance model  PLANNED
MS-S2  generic SetupCandidate + candidate-source contract   PLANNED
MS-S3  optional TemplateRepository                          PLANNED
MS-S4  deterministic GeneratedSetupCandidateSource          PLANNED
MS-S5  common SetupDiversityHistory / scorer / selector     PLANNED
MS-S6  generic shown-identity policy                        PLANNED
MS-S7  adapt accepted TB pipeline with parity               PLANNED
MS-S8  adapt NGJ/no-template path with parity               PLANNED
MS-S9  generic acceptance / future-script proof             PLANNED
A3 immutable setup snapshot                                 DEFERRED / NOT CURRENT
```

TBSP-1 through TBSP-6L are complete, accepted, and merged. Do not reopen them without concrete regression evidence.

'''
readme = readme[:start] + replacement + readme[end:]
readme = replace_once(
    readme,
    '- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md) — current AI development workflow；',
    '- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md) — current AI development workflow；\n- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) — normative large/truncated-file one-shot workflow + Python patch SOP；',
    'README workflow reference',
)
readme_path.write_text(readme, encoding='utf-8', newline='\n')

for path in [
    Path('docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md'),
    Path('docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6G_B_PRODUCTION_WIRING.md'),
    Path('docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6J_CLEANUP.md'),
    Path('docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6K_FINAL_ACCEPTANCE.md'),
]:
    if not path.exists():
        raise SystemExit(f'Expected completed handoff to exist: {path}')
    path.unlink()
