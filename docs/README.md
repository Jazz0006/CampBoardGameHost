# CampBoardGameHost 文档入口

> 最后整理：2026-08-24  
> **任何新的开发或审计任务先读本文，再读 `CURRENT_DEVELOPMENT_ROADMAP.md`。**  
> 通过 ChatGPT / GitHub connector / Codex 修改代码时，还必须读根目录 `AGENTS.md` 和对应工作流文档。

## 1. 当前权威顺序

出现冲突时：

1. 游戏规则正确性：官方 Blood on the Clocktower 规则 / Almanac / published rulings；
2. 项目级 AI 执行规范：根目录 `AGENTS.md`；
3. 当前开发状态 / 下一步：`CURRENT_DEVELOPMENT_ROADMAP.md`；
4. 当前下一任务 handoff：`NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`；
5. Possible Worlds 总体架构：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`；
6. R6 timeline / knowledge-safe boundaries：`r6_p1_2_closeout_2026-08-21.md`、`r6_p1_3_closeout_2026-08-21.md`；
7. impaired information / Storyteller decision：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`；
8. 多剧本 / 动态流程：`多剧本多板子与动态游戏流程架构设计_v1.md`；
9. GitHub / Codex 写入流程：`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
10. 大文件本地实现流程：`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
11. 旧 structural handoff / historical audit / `archive/` 仅用于追溯，不得覆盖当前 roadmap。

## 2. 当前必须阅读

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **CURRENT / 唯一状态权威**  
  App-root S0–S6 structural checkpoint 已完成；S7 paused；当前工作是 draft PR #48 的 historical multi-night exact baseline，进入 A3 Architecture Hardening。

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md) — **NEXT TASK HANDOFF**  
  记录 PR #48 当前 exact-baseline architecture、2026-08-24 全局审计发现、H1–H7 hardening 顺序，以及新对话的 first RED contract。

- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — **POSSIBLE WORLDS ARCHITECTURE**

- [`r6_p1_2_closeout_2026-08-21.md`](r6_p1_2_closeout_2026-08-21.md) / [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md) — **GLOBAL TIMELINE / CHRONOLOGY CONTRACTS**

- [`r6_p1_3_closeout_2026-08-21.md`](r6_p1_3_closeout_2026-08-21.md) — **KNOWLEDGE-SAFE WORLD-BUILDER BOUNDARY**

- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — **NORMATIVE**

- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — **NORMATIVE**  
  大文件 declaration move / mechanical cleanup 默认由 Luna 在完整本地 worktree 中实施、测试、commit、push；ChatGPT 负责远端 exact diff 与 CI/R2。

## 3. 当前准确开发状态

```text
PR #39 Storyteller Information Decision Foundation     MERGED
PR #40 Structured Manual UI — Empath numeric slice     MERGED
PR #41 workflow / LF policy docs-infra                 MERGED
PR #42 Historical Action + Observation Capture         MERGED
PR #44 Drunk / Fortune Teller correctness hotfix       MERGED
PR #43 Clocktower host source decomposition            MERGED / A1–A13 GREEN
App-root structural S0–S6                             MERGED CHECKPOINT
App-root S7                                             PAUSED
PR #48 A3 historical multi-night exact baseline        OPEN / DRAFT / ACTIVE
```

Corrected stable `main` checkpoint at this documentation update:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Last fully validated code checkpoint on PR #48 before docs-only updates:

```text
9909e7fc76c0ef700d617ee1c70ae465b1565e67
CI #591 SUCCESS
R2 #524 SUCCESS
Android / ASP / Real Clingo GREEN
```

New conversations must re-query live `main`, PR #48 head and checks because documentation commits may have advanced the branch.

## 4. App-root structural state

The earlier App-root decomposition task is no longer current.

Completed:

```text
S0 dynamic-flow guard
S1 player setup presentation
S2 settings presentation
S3 Clocktower landing presentation
S4 deal / reveal presentation
S5 results / host-tools / history presentation
S6 legacy generic GameScreen presentation
```

Approximate result:

```text
CampBoardGameHostApp.kt
~325,556 bytes -> ~229,822 bytes
```

S7 was explicitly paused after the corrected PR #47 checkpoint. Do not resume it inside A3.

Old handoffs such as:

```text
NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_S7.md
```

are historical context only for the current execution point.

## 5. Current A3 scope

The original setup/first-night `EnumeratedWorldSet` A3 baseline was completed earlier and remains valid.

PR #48 is the **historical multi-night extension**.

Already established tests-first:

```text
knowledge-safe PlayerHistoricalTimeline
GLOBAL_V1 visible chronology
Execution / Death / PhaseAdvance historical replay
visible observation replay
Poisoner persistent-effect lifecycle
rule-derived later-night Poisoner branching
per-world canonical night schedule
Drunk actual vs shown waking identity split
visible night observation anchoring
Demon direct-attack rules seam
pure hidden Monk/Imp possible-choice helpers
```

Still deliberately fail-closed / not production authority:

```text
Attack integration
Protect integration
RoleChange / Demon succession integration
Mayor redirect integration
Host wiring
A4/ZDD production promotion
```

## 6. 2026-08-24 global audit result

The audit concluded that the overall direction remains correct, but the engine has crossed from a static setup-world model into a time-aware historical-world problem.

Before further Mayor/Demon mechanics expansion, harden these boundaries:

```text
H1  setup seed vs durable observation exactly-once consumption
H2  historical ability eligibility / triggered exceptions
H3  mechanical world identity independent of explanation/provenance
H4  explicit Trouble Brewing support guard
H5  dynamic historical role representation / Demon succession state
H6  incremental state-aware night replay
H7  then wire Monk -> Imp -> Mayor -> successor mechanics
```

Critical invariants:

```text
actual storyteller hidden ActionFacts never constrain player worlds directly
GLOBAL_V1 remains durable chronology authority
hidden choice path != possible-world identity
canonical night rank != historical ability eligibility
setup character identity != necessarily current historical role identity
RoleChange remains fail-closed until dynamic state is modeled
```

## 7. Immediate next slice

The next conversation starts with **H1 only**.

Audit and characterize:

```text
PlayerKnowledgeSnapshot
-> TroubleBrewingWorldEnumerator
-> EnumeratedWorldSet.fromWorlds
-> PlayerHistoricalTimeline
-> EnumeratedHistoricalWorldReplay
```

First RED must prove:

- multi-night durable observations do not enter setup-only enumeration;
- setup-only knowledge still constrains initial worlds;
- each durable visible observation is consumed exactly once by the GLOBAL historical path;
- the fix does not introduce hidden actual ActionFact targets;
- no Host/A4/ZDD authority change.

Do not start Mayor branching before this is GREEN and audited.

## 8. New-conversation startup order

1. read root `AGENTS.md`;
2. read this document;
3. read `CURRENT_DEVELOPMENT_ROADMAP.md`;
4. read `NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`;
5. query live `main` and record current SHA;
6. query draft PR #48/head/checks;
7. distinguish docs-only head advancement from the last validated code checkpoint;
8. audit the H1 observation-consumption path;
9. establish H1 RED first;
10. only then implement the narrow GREEN;
11. do not merge, mark ready, rebase or force-push without explicit user authorization.

## 9. 文档维护规则

- `AGENTS.md` 维护长期 AI 执行规范；
- 只有 `CURRENT_DEVELOPMENT_ROADMAP.md` 维护当前执行点；
- 当前 handoff 服务下一次开发；
- design 文档维护语义/架构，不维护 live branch；
- historical handoff 不得覆盖 current roadmap；
- 每个 A3 hardening slice 完成后更新 validated head / gate / remaining blockers；
- PR #48 merge discussion 前必须同步 roadmap、PR body、handoff 和 exact diff audit。
