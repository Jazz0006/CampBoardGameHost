# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Live main: `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> Active branch: `codex/d6-root-reaudit`
> Draft PR: #113
> Status: **D6.1 ACCEPTED / T4 GREEN — POST-D6.1 RE-AUDIT COMPLETE — MERGE DECISION NEXT, THEN D6.2a ON A NEW BRANCH**

## Read first

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`
5. `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`
6. this handoff
7. older D6.0–D6.1d docs only when historical evidence is needed

Always re-confirm live GitHub state before writes.

## D6.1 — COMPLETE and accepted

Accepted checkpoint:

```text
30adff1ecb195123c7096757c1454c4a4a61ccca
[full-ci] D6.1 acceptance checkpoint
```

Validation:

```text
CI 34223133695 — PASS
R2 34223133706 — PASS
Android full JVM + debug APK build — PASS
ASP contracts — PASS
Real Clingo cross-validation — PASS
aggregate CI gate — PASS
```

Pre-acceptance structural evidence:

```text
D6.1d global ownership audit 34221212685 — PASS
D6.1e compatibility/read-side audit 34222474745 — PASS
```

Final canonical owner:

```text
ClocktowerGameSession
= identity + revisions + semantic chronology + dynamic GameState writable authority
```

App-root cards/flow variables remain presentation/orchestration mirrors and derived read adapters. Do not reopen broad GameState ownership or mechanically replace `cards.toClocktowerGameState(...)` readers.

## Post-D6.1 residual re-audit — COMPLETE

Audit run:

```text
34223904849 — PASS
```

Cleanup head:

```text
b61af7e7029f8b7df441fb32b35c1fed3a2d4b18
```

The cleanup tree is exactly the accepted T4 tree:

```text
dfe4ad99f331361697ea8976c7d72fb529106d54
```

Key measurements:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
App-root clocktower vars 41
```

Conclusion: the dominant residual problem is now UI composition fan-out, not session authority.

## PR #113 decision gate

PR #113 is a complete D6.1 checkpoint. Do not add D6.2 production changes to it.

Preferred sequence:

```text
user explicitly authorizes merge
-> re-confirm #113 head / checks / main
-> merge #113
-> confirm new main
-> create fresh D6.2 branch
```

Until merge is explicitly authorized, keep #113 Draft/open and do not begin D6.2 production work on the current branch.

## D6.2a — NEXT after merge: Judge UI composition characterization

No production edit first.

Build a responsibility/consumption matrix for all 103 `ClocktowerJudgeScreen` parameters and 39 callbacks:

```text
parameter/callback
-> responsibility group
-> actual consumer(s)
-> phase scope
-> transient UI vs durable/domain boundary
-> current owner
-> child-screen forwarding
-> existing tests
-> candidate cohesive contract
```

Also map all `MutableState<T>` parameters and decide where transient UI ownership naturally belongs.

Rank candidate groups by:

- reduction in cross-phase knowledge;
- complete ownership of a real UI responsibility;
- existing child-screen seam;
- behavior/test evidence;
- Compose-only blast radius;
- actual reduction in App + HostScreen fan-out.

### Do not

- create one mega `ClocktowerJudgeActions` object containing the same 39 callbacks;
- create a broad Controller/ViewModel to hide the same dependencies;
- move Compose/UI state into `ClocktowerGameSession`;
- change gameplay or recommendation semantics;
- reopen Recovery v2/PS5 lifecycle topology;
- mix D6.2 into PR #113.

## Frozen invariants

- same game ID/seed/script behavior;
- no fake NGJ RulesetRef;
- exact revision cadence;
- global chronology/idempotency unchanged;
- Recovery/A4 ordering unchanged;
- no hidden-information leak;
- no Compose dependency in session/domain;
- Undercover/Werewolf untouched;
- no intended user-visible behavior change for structural D6.2 slices.

## Suggested continuation prompt

```text
请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md、docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md 和当前 handoff。先重新确认 live main、PR #113 head/state/checks。D6.1 已通过 T4，post-D6.1 residual audit 也完成。不要再扩大 session/GameState/read-side 重构。如果 PR #113 尚未合并，先停在 merge decision；若已合并，则从新 main 建立独立 D6.2 分支，从 D6.2a 开始只读 characterization：完整映射 ClocktowerJudgeScreen 103 个参数、39 个 callbacks、MutableState ownership 和 child-screen consumption，选出最小的真实 cohesive UI boundary。不要用一个 mega Actions/State bag 伪装解耦，先不要改 production。
```
