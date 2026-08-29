# Next Development Handoff — PR #54 Runtime Blockers

> Date: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> PR: #54  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Status: **PR54-P1-1 ACCEPTED; PR54-P1-2 IMPLEMENTATION GREEN / T1 ACCEPTED; FINAL T4 MERGE-READINESS GATE PENDING — PR MUST REMAIN UNMERGED**

## 1. Current blocker status

Later review of the accepted GCR checkpoints found two normal runtime paths outside prior coverage. Both runtime defects now have typed behavioral RED/GREEN evidence. PR54-P1-2 production wiring is GREEN at the FAST/T1 checkpoint and has passed remote exact-diff audit. A fresh T4 full-tree acceptance run is still required before current PR merge-readiness can be refreshed.

```text
PR54-P1-1  First-night Fortune Teller projection crash                 FIXED / ACCEPTED
PR54-P1-2  Imp self-kill pending successor reconstruction crash        FIXED / T1 ACCEPTED / T4 PENDING
```

Do not merge, rebase, force-push, or broaden into GCR-4/5, A3, App-root S9.2, Host/A4/ZDD promotion, generic misinformation, or unrelated recommendation work.

## 2. PR54-P1-1 — First-night Fortune Teller projection crash — ACCEPTED

Accepted contract:

```text
First Night
-> use base/current persisted role state for Fortune Teller matching
-> do not evaluate Other Night chronology projection

Other Night
-> continue using canonical same-night effective-state projection
```

Evidence:

```text
Historical behavioral RED:
779a58ea83ef2fc1a07aae67e63b929454c0c8ab
CI #987 / run 33220660973
- compilation succeeded
- :app:testFast executed
- 907 tests completed, 1 failed
- expected Fortune Teller First Night behavioral failure

Minimal production GREEN:
11cc2c78b336f11e7bb9722b1913c2cfabe8d109
- only ClocktowerHostCoreSemantics.kt and ClocktowerHostScreen.kt changed
- First Night selects base authority
- Other Night projection is lazy and remains canonical

Strengthened typed regression checkpoint:
f97276139ab0123329c1e73c0380048c5ac98e3d
CI #989 / run 33221389915 SUCCESS
R2 #916 / run 33221389931 SUCCESS
```

The projector remains fail-closed. Do not reopen this slice unless new evidence appears.

## 3. PR54-P1-2 — Imp self-kill pending successor Host reconstruction crash — IMPLEMENTATION GREEN

### Confirmed failure lifecycle

A valid pending succession window can be:

```text
old Imp mechanically dead
pending successor exists
phase == Night
successor identity not yet confirmed/materialized
no currently living Demon card
```

The original Host mixed current living-Demon UI authority with current-night transaction/reconstruction authority. Recomposition therefore lost the old Imp role, lost canonical succession requirement/ordering, and could throw before new-Demon identity confirmation.

### Accepted authority split

```text
current living Demon UI authority
-> resolveCurrentDemonHostContext()
-> remains living-only and fail-closed for ambiguity

current-night transaction/reconstruction Demon authority
-> resolveNightReconstructionDemonRoleId(...)
-> living Demon when exactly one exists
-> otherwise, only in the zero-live pending window, explicit confirmed dead Demon attacker
-> never masks multiple-live-Demon ambiguity

canonical succession requirement
-> resolveNightDemonSuccessionForHost(...)
-> delegates to the existing Trouble Brewing self-kill resolver
-> does not require the old Imp to remain mechanically alive

canonical Other Night ordering anchor
-> clocktowerOtherNightWakingRoleIds(...)
-> normal nights contain living current roles + Drunk shown role
-> pending succession temporarily retains the historical Imp role only as an ordering anchor
```

The successor is not role-mutated early. The old Imp remains mechanically dead. `NightTransactionReconstructor` remains fail-closed and unchanged.

### Typed RED/GREEN evidence

Three independent typed seams exposed the lifecycle defect.

```text
1. reconstruction role authority RED
5dcf91e0...
- old Imp dead
- zero live Demon
- expected historical Imp role authority

2. pending succession requirement RED
b9cb90d3...
- pendingNewDemonName / confirmed successor present
- old Imp already dead
- expected canonical Choice(seat 2)

3. Other Night Imp ordering-anchor RED
5ba1c162b8c3c7eb40b5d4ecd01a9dbd935c51b5
CI #1000 / run 33222642480
- production/test compilation succeeded
- :app:testFast executed
- 914 tests completed, 1 failed
- only failure: pending succession retains historical Imp ordering anchor after old Imp is dead
```

The ambiguity guard was also tested: multiple live Demons must remain fail-closed and must not fall back to a dead historical Imp.

Typed seam GREEN checkpoint:

```text
13a5fb03a9e5d49200f0516cbe9db16d3b6f0a11
CI #1001 / run 33223172126 SUCCESS
- Android FAST unit tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS
R2 #928 / run 33223172160 SUCCESS
```

### Final Host wiring GREEN

Production Host wiring checkpoint:

```text
8a6f8cea481edbaef8a5946abb19ef0d0f483005
commit: fix: wire pending Imp succession into night host
```

Exact diff audit from `13a5fb03...`:

```text
ahead_by: 1
behind_by: 0
changed files: exactly 1
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
23 additions / 50 deletions
```

The Host wiring:

- removes duplicate `DemonSuccessionSemantics` / `DemonSuccessionContext` calculation;
- creates one `nightBaseGameState`;
- derives canonical transaction role with `resolveNightReconstructionDemonRoleId(...)`;
- derives succession with `resolveNightDemonSuccessionForHost(...)`;
- builds Other Night waking roles through `clocktowerOtherNightWakingRoleIds(...)`;
- reuses the same canonical Demon role for `NightTransactionReconstructor`;
- reuses that same authority for `ResolvedNightMechanicalEvent.MechanicalDeath`, removing the second living-Demon-only crash point;
- leaves `demonCard` as living-Demon UI authority for wake/display behavior.

T1 evidence:

```text
CI #1002 / run 33223959281 SUCCESS
- Android :app:testFast SUCCESS
- CI gate SUCCESS
R2 #929 / run 33223959279 SUCCESS
```

No unrelated UI, successor mutation, rules, reconstructor, or misinformation behavior was changed.

## 4. Current execution order

```text
A. PR54-P1-1 behavioral RED/GREEN                         DONE
B. PR54-P1-2 typed lifecycle REDs                        DONE
C. PR54-P1-2 typed seam GREEN                            DONE
D. Host final wiring + exact diff audit                  DONE
E. T1 :app:testFast logical checkpoint                   DONE
F. fresh T4 :app:testFull + :app:assembleDebug           NEXT / REQUIRED
G. ASP contract + Real Clingo full gate                  NEXT / REQUIRED
H. if all T4 gates pass, refresh roadmap / merge evidence
```

This commit intentionally carries `[full-ci]` to request the repository's full acceptance route against the current production tree.

Do not merge PR #54 without explicit user authorization even if T4 becomes green.

## 5. Historical acceptance context

Previously accepted checkpoints remain valid evidence for their covered behavior:

```text
GCR-1 executable acceptance:
974f617adffd08cc7de0924f6fea4f96f3d73f0c

GCR-1 + GCR-2 historical full production T4:
474103ed13caaf34a329ca5e80e2f0ba64963b86

GCR-3 final test-quality acceptance:
383ad0e695656124f9dc608fd5ce06b72de6b499
```

The new T4 requested here supersedes those older checkpoints only as current PR merge-readiness evidence; it does not invalidate their historical coverage.
