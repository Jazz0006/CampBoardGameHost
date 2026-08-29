# Next Development Handoff — Global Correctness Review Follow-up

> Date: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Status: **GCR-1 / GCR-2 / GCR-3 GREEN / ACCEPTED — PR #54 remains draft and unmerged**

## 1. Campaign result

The GCR follow-up campaign is accepted for the scoped merge-blocking correctness work.

```text
GCR-1  Current Demon authority / cross-night succession         GREEN / ACCEPTED
GCR-2  Poisoned Spy fail-safe information policy                GREEN / ACCEPTED
GCR-3  Typed production acceptance + source-string retirement   GREEN / ACCEPTED
GCR-4  Chambermaid actual wake-history authority                DEFERRED FOLLOW-UP
GCR-5  Durable identity + reconstruction API hardening          DEFERRED FOLLOW-UP
```

Do not reopen GCR-1/2/3 without a new typed regression or an explicit product-policy change.

## 2. Accepted checkpoints

### GCR-1 executable acceptance

```text
974f617adffd08cc7de0924f6fea4f96f3d73f0c
```

Evidence:

```text
CI #959 / run 33174380352 SUCCESS
R2 run 33174380336 SUCCESS
```

### GCR-1 + GCR-2 full production acceptance

```text
474103ed13caaf34a329ca5e80e2f0ba64963b86
```

Evidence:

```text
CI #963 / run 33175600756 SUCCESS
- Android :app:testFull + :app:assembleDebug SUCCESS
- ASP contract tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS

R2 run 33175600749 SUCCESS
```

### GCR-3 final source-test acceptance

```text
383ad0e695656124f9dc608fd5ce06b72de6b499
```

Evidence:

```text
CI #980 / run 33177405639 SUCCESS
- Android FAST unit tests SUCCESS
- CI gate SUCCESS

R2 run 33177405675 SUCCESS
```

## 3. GCR-1 accepted behavior

Canonical current-Demon authority now supports repeated succession:

```text
Imp0 self-kills -> Imp1 becomes current Demon
Imp1 acts on following nights
Imp1 can later self-kill -> Imp2 becomes current Demon
```

Historical dead Demon identity remains represented. Host-facing Demon actor/poison behavior and mechanical paths consume the canonical authority rather than historical-first shortcuts.

## 4. GCR-2 accepted product policy

The app intentionally does **not** simulate fabricated Grimoire misinformation for a poisoned Spy.

Accepted policy:

```text
healthy Spy
-> wake normally
-> receive the true Grimoire

poisoned Spy
-> wake normally
-> receive no Grimoire payload
-> true Grimoire is not exposed through another display path
-> Host may identify the poisoned state to the Storyteller
-> no Spy Grimoire observation is durably published
```

This is an intentional product simplification / house-rule deviation from official poisoned-information semantics. The interaction-shape difference is accepted. Do not build a fake-Grimoire or generic misinformation subsystem unless product policy is explicitly changed later.

## 5. GCR-3 accepted source-string retirement

Detailed audit:

```text
docs/GCR3_SOURCE_STRING_RETIREMENT_AUDIT_2026-08-28.md
```

The retirement campaign reduced implementation-shaped tests to coarse production ownership guards while leaving gameplay semantics with typed tests.

Important decisions:

- exact callback statement ordering is not a long-term correctness contract;
- exact variable names, role lists, UI wording and internal projection fields are not source-test contracts;
- coarse source guards may remain where App/Compose ownership is not callable;
- do not extract a production seam solely to reduce source-string count when the remaining guard is already coarse.

Dawn materializer extraction was therefore deferred.

## 6. Why no second T4 was required after GCR-3

Exact compare:

```text
474103ed -> 383ad0e6
17 commits
13 modified test files
1 added audit doc
0 production files
```

The production tree at `383ad0e6` is therefore the same production tree that already passed the real T4 at `474103ed`.

The final slimmed test tree separately passed Android FAST + CI gate + R2 at `383ad0e6`.

Do not reinterpret skipped full Android/ASP/Clingo routes on later test/docs-only commits as missing production validation; the required full production route actually executed and passed at `474103ed`.

## 7. Live base / branch condition at acceptance

At final audit time:

```text
main: c8985cb4991f6c7e5ea02adedb932d2d86452da1
```

`main` had not moved since the validated PR base.

PR #54 remains:

```text
open
draft
unmerged
```

Never mark ready or merge without explicit user authorization.

## 8. Deferred follow-up — not blockers for PR #54

### GCR-4 — Chambermaid actual wake-history authority

Future target:

```text
canonical night interaction history
-> actual wake/materialization event
-> wake reason = OWN_ABILITY / OTHER
-> Chambermaid query
```

Do not solve by indefinitely expanding a role-name allowlist.

### GCR-5 — durable identity / reconstruction API hardening

Future audit items:

- prove active-game player-name uniqueness/immutability if checkpoint targets remain name-based;
- otherwise design a separate stable-seat identity migration;
- clarify `NightTransactionReconstructor.effectiveState` naming if it represents final reconstructed night state;
- consider systematic Dawn crash cut-point fault injection.

These are explicitly deferred and did not block the validated campaign.

## 9. Other deferred architecture work

Do not mix into PR #54 without a new decision:

- A3 immutable setup snapshot ownership/persistence;
- App Root S9.2 Active Game Persistence Boundary;
- generic custom-script Demon succession;
- generic Mayor redirect-to-Demon succession support;
- Host/A4/ZDD production promotion;
- recommendation tuning;
- history UI / generic misinformation tuning.

## 10. Restart contract

If work resumes after this handoff:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. re-query live `main`, PR #54 head/state/checks;
5. distinguish the accepted production T4 checkpoint (`474103ed`) from later test/docs-only checkpoints;
6. do not reopen accepted GCR behavior without a typed regression;
7. do not merge, mark ready, rebase or force-push without explicit authorization.

## 11. Current next action

There is no remaining GCR-1/2/3 correctness implementation task.

The next action is an explicit PR decision by the user:

```text
A. keep PR #54 draft for additional review;
B. explicitly mark PR #54 ready for review;
C. explicitly authorize merge.
```

Do not start GCR-4/5 in this PR merely because engineering work is available. They are follow-up tasks, not unresolved blockers.
