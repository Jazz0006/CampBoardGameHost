from pathlib import Path

SESSION = Path('app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt')
D1 = Path('docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md')
ROADMAP = Path('docs/CURRENT_DEVELOPMENT_ROADMAP.md')
HANDOFF = Path('docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md')
D1E = Path('docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md')


def read(path: Path) -> str:
    raw = path.read_bytes()
    if b'\r\n' in raw or b'\r' in raw:
        raise SystemExit(f'Unexpected line ending in {path}')
    return raw.decode('utf-8')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'Expected exactly one {label}, found {count}')
    return text.replace(old, new, 1)

# 1) Source comments only: reflect completed canonical GameState ownership.
s = read(SESSION)
s = replace_once(
    s,
    ''' * Immutable read model for the D6.1c production cutover-safe subset.\n *\n * It intentionally excludes [GameState]: App-root mechanics are still the live mechanical source until\n * D6.1d proves and completes canonical GameState cutover. Compose may observe this value, but cannot\n * mutate session-owned identity, revision, or semantic chronology through it.\n''',
    ''' * Immutable Compose-facing read model for session identity, revision, and semantic chronology.\n *\n * [GameState] is canonical inside [ClocktowerGameSession], but remains intentionally absent here:\n * production UI still owns [PlayerCard]-style presentation metadata and uses derived GameState\n * projections for pre-session, recommendation, and screen-local reads. Excluding GameState keeps this\n * view narrow and prevents a second broad mechanical read API while preserving the single session writer.\n''',
    'ClocktowerSessionView ownership comment',
)
s = replace_once(
    s,
    '''         * Stateless session transition used by the production Compose adapter until the full game\n         * state is session-owned. It owns the same cursor/log semantics as the instance API without\n         * requiring a synthetic RulesetRef for scripts whose advanced ruleset is not loaded.\n''',
    '''         * Stateless pure transition retained behind the instance API and deterministic replay/contracts.\n         * It owns the same cursor/log semantics without requiring a synthetic RulesetRef for scripts whose\n         * advanced ruleset is not loaded. Production mutation enters through the live session instance.\n''',
    'stateless action transition comment',
)
s = replace_once(
    s,
    '''         * Stateless session transition used by the production Compose adapter until the full game\n         * state is session-owned. It owns the same cursor/log/revision semantics as the instance API\n         * without requiring a synthetic RulesetRef for scripts whose advanced ruleset is not loaded.\n''',
    '''         * Stateless pure transition retained behind the instance API and deterministic replay/contracts.\n         * It owns the same cursor/log/revision semantics without requiring a synthetic RulesetRef for scripts\n         * whose advanced ruleset is not loaded. Production mutation enters through the live session instance.\n''',
    'stateless observation transition comment',
)
if 'until the full game state is session-owned' in s or 'App-root mechanics are still the live mechanical source' in s:
    raise SystemExit('Stale session ownership wording remains')
SESSION.write_text(s, encoding='utf-8', newline='\n')

# 2) Close D6.1d progress while preserving earlier chronological evidence.
d = read(D1)
d = replace_once(
    d,
    '> Status: **IN PROGRESS — role identity ownership slice complete; poison cadence characterization next**',
    '> Status: **D6.1d COMPLETE — canonical dynamic GameState writer cutover + global ownership audit PASS — D6.1e acceptance next**',
    'D6.1d status',
)
marker = '## Remaining canonical GameState fields\n'
if d.count(marker) != 1:
    raise SystemExit('Unexpected D6.1d tail marker count')
prefix = d.split(marker, 1)[0]
d = prefix + '''## D6.1d final cutover — COMPLETE\n\nThe remaining dynamic per-seat mechanics were completed after the role-identity slice:\n\n- alive/death: ordinary execution, Virgin, Slayer and Dawn/night-death materialization all synchronize canonical session state before the `PlayerCard.eliminatedRound` mirror changes;\n- poisoned: Poisoner confirmation owns a `+1` accepted session boundary, while ordinary Dawn, Dusk expiry and successor/retry convergence use `+0` synchronization inside an already-accepted revision;\n- role identity: actual-role and shown-role changes remain session-first;\n- session creation/restore remains exactly one live owner per active Clocktower game.\n\nThe important acceptance rule is **writer ownership**, not elimination of every `PlayerCard`-to-`GameState` projection. `cards` still contains UI/presentation fields such as localization and `eliminatedRound`, and pre-session/recommendation/screen-local consumers legitimately construct derived read projections. Those projections are not writable canonical authorities.\n\n## Global ownership audit — PASS\n\nRead-only full-checkout audit run:\n\n```text\n34221212685 — PASS\n```\n\nIt proved the post-cutover topology:\n\n```text\ndeath session sync calls       4\npoison +1 commit calls         1\npoison +0 sync calls           3\nactual-role session writers    1\nshown-role session writers     1\nproduction session create      1\nproduction session restore     1\n```\n\nIt also proved that from `main` the only changed production files are the App root plus the four Clocktower session/boundary files, and that Recovery, Clocktower rules, A4 epistemic production code and Werewolf production code remain unchanged. Combined D6.1d focused contracts and `:app:testFast` both passed in the same audit run.\n\n## D6.1e compatibility/read-side audit — PASS\n\nRead-only callsite audit run:\n\n```text\n34222474745 — PASS\n```\n\nFindings:\n\n1. no production caller uses the stateless `ClocktowerGameSession.commitGlobalActionFact(...)` or `commitGlobalEpistemicObservation(...)` companion forms directly; the live App uses instance methods;\n2. the stateless forms remain useful as pure transitions behind the instance API and deterministic replay/contract tests, so deleting them would not improve ownership;\n3. `updateGameState()` has no production callsite but remains a deliberate equality-based session contract protected by tests;\n4. `toGameSnapshot(...)` remains the strict ruleset-backed projection and is not a competing production owner;\n5. `cards.toClocktowerGameState(...)` has many production read callsites across pre-session, recommendation and UI flows, so wholesale replacement would mix presentation concerns back into session ownership;\n6. the only genuinely stale production items were two ownership comments in `ClocktowerGameSession.kt`; D6.1e updates those comments without changing runtime behavior.\n\n## Final D6.1d ownership model\n\n```text\nClocktowerGameSession\n= canonical writable session + dynamic mechanical GameState authority\n\nPlayerCard / App-root flow variables\n= presentation and orchestration mirrors\n= updated only after the relevant session boundary for canonical mechanics\n\nDerived cards.toClocktowerGameState(...) values\n= read/pre-session/recommendation adapters\n= not writable session authority\n```\n\n## NEXT — D6.1e acceptance\n\nNo additional production refactor is selected before acceptance. The next step is one user-authored `[full-ci]` logical checkpoint, then record the T4 result and re-check PR #113 merge readiness. Do not merge automatically.\n'''
D1.write_text(d, encoding='utf-8', newline='\n')

# 3) Advance roadmap from D6.1d implementation to D6.1e acceptance.
r = read(ROADMAP)
r = replace_once(
    r,
    '''latest tested D6 production checkpoint: bd0161c50a7e4d2c94187c553546696bf6e81aee\npre-closeout cleanup head: 68e8d8c9458a9e9e192c711199e352c9623db712\n''',
    '''latest D6.1d production checkpoint: a7d548991e108ad2adbfa747fc6442e112470321\nglobal ownership audit cleanup head: 6176e681f2245c5dfbfe1068cd6c168fc7bfd9cc\nD6.1e compatibility-audit cleanup head: 8db22dcf35a15f82382e69c50e64937022b0cc2e\n''',
    'roadmap live checkpoint block',
)
r = replace_once(
    r,
    '> **D6.0 COMPLETE → D6.1a COMPLETE → D6.1b COMPLETE → D6.1c COMPLETE → D6.1d canonical GameState ownership re-audit/cutover NEXT.**',
    '> **D6.0 COMPLETE → D6.1a COMPLETE → D6.1b COMPLETE → D6.1c COMPLETE → D6.1d COMPLETE → D6.1e T4 ACCEPTANCE NEXT.**',
    'roadmap priority',
)
r = replace_once(
    r,
    'The strongest existing owner is `ClocktowerGameSession`. D6.1c has now made it the sole writer for common Clocktower identity/revision/semantic-history authority. The remaining major session-authority gap is canonical mechanical `GameState` ownership.',
    'The strongest existing owner is `ClocktowerGameSession`. D6.1c made it the sole writer for identity/revision/semantic-history authority, and D6.1d completed canonical dynamic mechanical `GameState` writer ownership. App-root `PlayerCard` state remains a presentation/orchestration mirror rather than a competing canonical owner.',
    'roadmap owner summary',
)
r = replace_once(
    r,
    '- `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`\n- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`',
    '- `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`\n- `docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md`\n- `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`\n- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`',
    'roadmap authoritative docs',
)
start = r.find('## D6.1d — NEXT:')
end = r.find('## D6.1 invariants')
if start < 0 or end < 0 or end <= start:
    raise SystemExit('Could not locate roadmap D6.1d/D6.1e range')
r = r[:start] + '''## D6.1d — COMPLETE: canonical dynamic GameState ownership\n\nD6.1d moved complete dynamic writer families to the existing `ClocktowerGameSession` authority without replacing `PlayerCard` presentation state wholesale.\n\nFinal writer topology:\n\n```text\nactual role / shown role -> session boundary -> App mirror\nalive/death              -> session synchronization -> App eliminatedRound mirror\nPoisoner confirm          -> session +1 poison boundary -> App poison mirror\nDawn/Dusk/successor poison-> session +0 synchronization -> App poison mirror\n```\n\nThe +1/+0 split preserves the pre-existing accepted revision cadence, including state-only restore/retry convergence. No rule/planner semantics were moved into the session.\n\nGlobal audit evidence:\n\n```text\nD6.1d global ownership audit 34221212685 — PASS\ncombined focused contracts — PASS\n:app:testFast — PASS\nproduction dependency/scope audit — PASS\n```\n\nCompatibility/read-side audit:\n\n```text\nD6.1e callsite audit 34222474745 — PASS\n```\n\nThat audit rejected a size-driven reader migration: many `cards.toClocktowerGameState(...)` callsites are legitimate derived read, pre-session, recommendation or UI projections. They are not writable canonical state. Stateless session transition helpers likewise remain useful internal/test contracts even though production mutation uses instance methods.\n\nDetailed evidence: `docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md`.\n\n## D6.1e — NEXT: cleanup complete, T4 acceptance pending\n\nNo further production ownership migration is selected before acceptance. D6.1e only corrects stale session ownership comments and records the completed D6.1d topology.\n\nNext gate:\n\n1. create one **user-authored** commit whose message contains `[full-ci]`;\n2. confirm CI classifies it as a full checkpoint (`android_full=true`, ASP and Oracle selected);\n3. require complete Android JVM + debug assemble and every selected full-strength external gate to pass;\n4. record exact checkpoint/run IDs in `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`;\n5. re-check PR #113 live head/state/checks and merge readiness;\n6. do not merge automatically.\n\nThe acceptance commit is intentionally user-authored rather than a `GITHUB_TOKEN` push, because bot pushes from a workflow do not reliably trigger a second workflow.\n\n''' + r[end:]
ROADMAP.write_text(r, encoding='utf-8', newline='\n')

# 4) Advance handoff while preserving D6.1c historical checkpoint text.
h = read(HANDOFF)
h = replace_once(
    h,
    '> Status: **D6.0 + D6.1a + D6.1b + D6.1c COMPLETE — D6.1d CANONICAL GAMESTATE OWNERSHIP RE-AUDIT/CUTOVER NEXT — DO NOT MERGE YET**',
    '> Status: **D6.0–D6.1d COMPLETE — D6.1e T4 ACCEPTANCE NEXT — DO NOT MERGE YET**',
    'handoff status',
)
h = replace_once(
    h,
    '7. `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`\n8. this handoff\n9. persistence archive docs only when historical persistence context is needed',
    '7. `docs/D6_1C_SESSION_AUTHORITY_CUTOVER_PROGRESS_2026-09-08.md`\n8. `docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md`\n9. `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`\n10. this handoff\n11. persistence archive docs only when historical persistence context is needed',
    'handoff read list',
)
start = h.find('## D6.1d — NEXT')
end = h.find('## Invariants')
if start < 0 or end < 0 or end <= start:
    raise SystemExit('Could not locate handoff D6.1d/D6.1e range')
h = h[:start] + '''## D6.1d — COMPLETE: canonical dynamic GameState writer ownership\n\n`ClocktowerGameSession` is now the canonical writable owner for the dynamic domain mechanics already represented by `GameState`:\n\n- actual/shown role identity;\n- alive/death state;\n- poison state;\n- along with the D6.1c identity/revision/history/global-chronology subset.\n\nApp-root `PlayerCard` and flow variables remain necessary presentation/orchestration mirrors. Session mutation occurs first for canonical mechanics; UI mirrors follow.\n\nGlobal writer/dependency audit:\n\n```text\n34221212685 — PASS\n```\n\nThe audit also reran combined focused D6.1d contracts and `:app:testFast`, both PASS, and proved Recovery/rules/A4-epistemic/Werewolf production packages were not changed by the ownership cutover.\n\nD6.1e compatibility/read-side callsite audit:\n\n```text\n34222474745 — PASS\n```\n\nIt found no external production use of stateless session companion transitions or `updateGameState()`. Those APIs remain valid pure/session test contracts and should not be deleted merely for cleanup. It also found many legitimate `cards.toClocktowerGameState(...)` read/pre-session/recommendation/UI projections; do not mechanically replace them with session reads.\n\n## D6.1e — NEXT: T4 acceptance\n\nBefore any new architecture slice:\n\n1. finish comment/docs closeout only;\n2. create a user-authored `[full-ci]` checkpoint commit;\n3. verify full CI routing and all selected full-strength gates;\n4. record exact T4 evidence;\n5. re-confirm `main`, branch head and PR #113 checks;\n6. stop for merge/readiness review — do not merge automatically.\n\nNo additional GameState production refactor is authorized before this acceptance gate.\n\n''' + h[end:]
# Replace suggested continuation prompt with acceptance-focused prompt if present.
old_prompt_start = h.find('## Suggested continuation prompt')
if old_prompt_start >= 0:
    h = h[:old_prompt_start] + '''## Suggested continuation prompt\n\n```text\n请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md、docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md 和当前 D6 handoff。重新确认 live main、codex/d6-root-reaudit、draft PR #113 和最新 checks。D6.1d 已完成并通过全局 writer/dependency audit；不要继续扩大 GameState/read-side 重构。继续 D6.1e：确认 acceptance closeout diff 只包含 ownership 注释/docs，然后创建用户侧 `[full-ci]` checkpoint，验证完整 T4（Android full + assemble + ASP + Real Clingo 等所有被 classifier 选中的 gate）。全部 GREEN 后记录 exact commit/run/checks，做 PR merge-readiness 审计，但不要自动 merge。\n```\n'''
HANDOFF.write_text(h, encoding='utf-8', newline='\n')

# 5) New acceptance progress record: deliberately says PENDING until the user-authored full-ci commit runs.
D1E.write_text('''# D6.1e Acceptance Progress\n\n> Date: 2026-09-08 Australia/Sydney\n> Repository: `Jazz0006/CampBoardGameHost`\n> Branch: `codex/d6-root-reaudit`\n> Draft PR: #113\n> Status: **PRE-ACCEPTANCE CLOSEOUT COMPLETE — USER-AUTHORED T4 `[full-ci]` CHECKPOINT NEXT — DO NOT MERGE YET**\n\n## Accepted architecture entering T4\n\nD6.1 now has one existing owner rather than a parallel manager/controller:\n\n```text\nClocktowerGameSession\n= canonical writable game identity + revisions + semantic chronology + dynamic GameState mechanics\n\nClocktowerSessionView\n= narrow immutable Compose-facing identity/revision/history projection\n\nApp-root PlayerCard / flow variables\n= presentation and orchestration mirrors\n\nRecovery / recommendation / A4 / UI projections\n= downstream consumers/adapters, not session authority\n```\n\nD6.1d global ownership/dependency audit:\n\n```text\n34221212685 — PASS\n```\n\nD6.1e compatibility/read-side audit:\n\n```text\n34222474745 — PASS\n```\n\n## Why no further cleanup is selected before T4\n\n- stateless session transition functions have zero external production callers but remain pure transition implementations and deterministic test/replay contracts behind the instance API;\n- `updateGameState()` has zero production callers but remains the deliberately equality-based generic session contract;\n- strict `toGameSnapshot(...)` remains useful for ruleset-backed consumers;\n- numerous `cards.toClocktowerGameState(...)` callsites are derived pre-session/recommendation/UI reads, not writable authority;\n- wholesale reader replacement would increase coupling and is not required to establish single-writer ownership.\n\nThe only source cleanup selected is correcting stale KDoc that still described App-root mechanics as canonical. This is behavior-neutral.\n\n## T4 acceptance gate — PENDING\n\nThe next commit must be authored through the user-connected GitHub path and contain `[full-ci]` in its commit message. CI must classify the PR synchronize event as a full checkpoint and run every selected gate at full strength.\n\nAcceptance requires:\n\n- full Android JVM suite;\n- debug assemble/compile gate selected by CI;\n- ASP validation/Python contracts;\n- Real Clingo cross-validation;\n- aggregate CI gate;\n- R2/main-thread validation if selected by the PR workflow;\n- final PR live-state/check audit.\n\nAfter all gates are GREEN, update this file with the exact acceptance commit and run IDs, then stop for merge-readiness review. Do not merge automatically.\n''', encoding='utf-8', newline='\n')

print('D6.1e acceptance closeout patch prepared')
