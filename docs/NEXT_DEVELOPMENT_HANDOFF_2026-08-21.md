# CampBoardGameHost 下一阶段开发交接 — 2026-08-21

> 当前路线权威：`CURRENT_DEVELOPMENT_ROADMAP.md`  
> 当前阶段：**Production Semantic-History Foundation IN PROGRESS / PR #24**  
> 当前 branch：`codex/r6-semantic-history-foundation`  
> 基线 `main`：`3db66482d9367c6b42a3f2550b979c28bfafea42`

## 1. 这次 foundation 的最新决策

2026-08-21 已明确放弃未发布旧 active-game save 的兼容性。

```text
v1 → unsupported
v2 → unsupported
v3 → only supported active-game schema
```

原因：当前程序尚无外部用户，继续维护旧 migration 会增加 epistemic/timeline persistence 的歧义和长期复杂度。

因此必须区分：

```text
LEGACY_LOCAL
    = v3 里显式写入的当前 production chronology mode

missing mode
    != LEGACY_LOCAL
    = corrupted / unsupported payload → fail closed
```

旧 v1/v2 save 在 restore 入口直接拒绝，并清除失效 active save；不做 migration，不从缺失字段猜语义。

## 2. PR #24 当前目标

只建立 production history/persistence foundation：

```text
ClocktowerSemanticHistoryMode
├── LEGACY_LOCAL
└── GLOBAL_V1
```

以及现有 cursor：

```text
clocktowerNextTimelineGlobalSequence
```

这个 key 已存在，必须复用；不得增加第二个 timeline cursor representation。

### v3 必须满足

1. `ActiveGamePersistenceCoordinator.CURRENT_VERSION == 3`；
2. v1/v2 对 Clocktower / Werewolf / Undercover 全部 unsupported；
3. unsupported version 在任何 live-state mutation 前拒绝；
4. Clocktower v3 必须显式写 `clocktowerSemanticHistoryMode`；
5. missing / null / unknown / non-string mode → fail closed；
6. Clocktower v3 必须显式写现有 `clocktowerNextTimelineGlobalSequence`；
7. missing / null / non-integer / negative cursor → fail closed；
8. `GLOBAL_V1` 不能包含 `LegacyLocal` observation；
9. `LEGACY_LOCAL` 不能包含 Global observation；
10. Global cursor 必须严格大于 committed Global observation positions；
11. Trouble Brewing v3 restore 必须有 immutable ruleset basis + matching RulesetRef；
12. 不再重建 v1 setup basis，不再通过旧 hash 猜被 succession 替换的角色；
13. reset / new production game 仍显式 `LEGACY_LOCAL + cursor 0`；
14. 本 PR 不切 Global observation producer。

## 3. Tests-first 证据

初始 semantic-history tests-only commit：

`4759c6ee95bbbae53f4b43412bf75b7ee4cf5768`

CI #308 Android unit-test compilation 明确失败，因为新 contract 尚不存在；这是 foundation 的有效 red evidence。

用户决定 v3-only 后，又把测试更新为：

- `CURRENT_VERSION = 3`；
- only v3 supported；
- v1/v2 全 game kind reject；
- missing mode reject；
- existing cursor key required；
- legacy ruleset migration success tests 删除；
- fail-only helper 直接调用也必须 throw。

## 4. 当前 source 结构

### 新增

- `clocktower/domain/ClocktowerSemanticHistoryMode.kt`
- `persistence/ClocktowerSemanticHistoryPersistence.kt`
- semantic-history domain / persistence / production-wiring tests

### 修改

- `GameSnapshot.kt`：携带 explicit history mode 并校验 observation binding/cursor；
- `ActiveGamePersistenceCoordinator.kt`：v3-only gate；
- `ClocktowerRulesetPersistence.kt`：删除真实 legacy basis reconstruction；
- `CampBoardGameHostApp.kt`：
  - save explicit mode；
  - 把 live cursor 传入现有 `ClocktowerNightCheckpoint`；
  - restore 把现有 cursor key 传回 checkpoint；
  - mode + observation history + cursor 在 live mutation 前验证；
  - reset 仍 LegacyLocal + 0。

### 删除

- `ClocktowerLegacyPersistenceIdentityFactory.kt`
- `ClocktowerLegacyPersistenceMigrationTest.kt`

## 5. 关于大文件中的 legacy dead branch

`CampBoardGameHostApp.kt` 很大，当前仍可能保留一个旧 `when(version)` ruleset branch 的**不可达编译残影**。

这不构成兼容能力，因为：

```text
restore entry
→ isSupportedVersion(v1/v2) == false
→ fail before branch
```

同时小文件里的 transitional legacy helper 已改成 fail-only：直接调用也抛错，不会返回 migration result。

如果后续可以安全做 300KB App 的机械 cleanup，可以删除这些 dead symbols；但不要为了清两段不可达代码扩大本 foundation PR、整文件覆盖或降低写入安全性。

## 6. 明确 non-goals

本 PR 不做：

- production Global observation producer cutover；
- Host/night-flow semantic changes；
- recommendation UI / legacy direct button removal；
- Spy VerifiedExact；
- physical Grimoire ledger；
- A3 historical transitions；
- B4 productionization；
- ZDD promotion；
- revision-engine broad refactor；
- second cursor key；
- v1/v2 migration。

## 7. CI 当前注意事项

最近几次 GitHub Actions 出现 runner-start failure：

```text
job conclusion = failure
steps = null
no checkout/compiler/test logs
```

这种情况属于 infrastructure failure，不能当成代码 red/green。

PR #24 必须保持 Draft，直到真正执行并通过：

```text
R2 main-thread boundary
Android unit tests + debug APK
ASP contract tests
real Clingo cross-validation
```

只有 job 真正出现 steps 并执行测试后，结果才有效。

## 8. Foundation 完成后的下一里程碑

PR #24 merge 后，下一阶段才是：

**New-game Global Observation Ownership Cutover**

目标：

```text
new Clocktower game
→ explicit GLOBAL_V1
→ committed semantic observations
→ ClocktowerGameSession.allocateTimelinePoint(...)
→ one durable global chronology
```

仍然不要立刻扩到 historical A3/B4。

之后再做：

```text
Global observation ownership
↓
Recommendation Entry-Point Unification
↓
historical action + observation capture
↓
A3 multi-night exact baseline
```

## 9. 下一会话可直接使用的起始指令

```text
继续 CampBoardGameHost PR #24 / codex/r6-semantic-history-foundation。先确认最新 main/head 和 exact diff。当前 persistence policy 已改为 active-game v3-only：v1/v2 一律 unsupported；Clocktower v3 必须显式携带 semanticHistoryMode 和现有 clocktowerNextTimelineGlobalSequence cursor key，缺失/非法即 fail closed。LEGACY_LOCAL 只表示新 v3 game 的显式模式，不是旧 save fallback。不要恢复任何 legacy migration。先等待/重跑真正能启动 steps 的 R2 + Android + ASP + real Clingo CI；修复实际 compiler/test failure 后再做 final diff/review audit。不要切 production Global observation producer，不改 recommendation/Spy/A3/B4/ZDD authority。
```
