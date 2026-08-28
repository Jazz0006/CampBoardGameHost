# Next Development Handoff — PR #54 Runtime Blockers

> Date: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> PR: #54  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Status: **PR54-P1-1 ACCEPTED; PR54-P1-2 ACTIVE P1 RUNTIME BLOCKER — PR MUST REMAIN UNMERGED**

## 1. Current blocker status

Later review of the accepted GCR checkpoints found two normal runtime paths outside prior coverage. PR54-P1-1 has now been fixed and independently validated. PR54-P1-2 remains the sole active post-acceptance P1 blocker.

```text
PR54-P1-1  First-night Fortune Teller projection crash   FIXED / ACCEPTED
PR54-P1-2  Imp self-kill pending successor reconstruction crash   ACTIVE / BLOCKING
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

## 3. PR54-P1-2 — Imp self-kill pending successor Host reconstruction crash — ACTIVE

### Confirmed failure path

Normal succession lifecycle can temporarily be:

```text
old Imp mechanically dead
pendingNewDemonName != null
phase == Night
successor identity not yet confirmed/materialized
no currently living Demon card
```

Host reconstruction currently requires a non-null role from the current living Demon before the pending new-Demon confirmation UI is reached. Recomposition in this valid transient state therefore crashes.

### Required contract

- the pending succession window may legally have no currently living Demon;
- reconstruction must derive the succession role authority from the confirmed attacker/current-night historical Demon authority, or route pending confirmation before a living-Demon requirement;
- do not keep the old Imp alive as a workaround;
- do not durably mutate the successor early;
- do not replace the invariant with an unprincipled nullable fallback.

### Tests-first target

Audit these ownership boundaries before creating RED:

```text
ClocktowerCurrentDemonAuthority.kt
clocktower/rules/CurrentDemonAuthority.kt
clocktower/session/NightTransactionReconstructor.kt
clocktower/session/TroubleBrewingDemonSuccessionResolver.kt
ClocktowerHostScreen.kt reconstruction inputs
App mutation path that marks the old Imp dead and sets pendingNewDemonName
```

The preferred RED is typed and lifecycle-oriented. It must represent:

```text
phase == Night
old Imp already mechanically dead
pending successor exists
no living Demon card exists yet
confirmed current-night Demon attacker authority still exists
```

and prove reconstruction/Host input derivation can obtain the canonical Demon role without throwing or prematurely materializing the successor.

Focused regression candidates include:

```text
ClocktowerNewDemonIdentityContractTest
ClocktowerNewDemonProductionWiringTest
ClocktowerCurrentDemonAuthorityTest
TroubleBrewingCurrentDemonRegressionTest
NightTransactionReconstructorSuccessionLegalityTest
ClocktowerDemonSuccessionProductionWiringTest
```

## 4. Execution order

```text
A. audit pending-succession authority ownership
B. establish typed PR54-P1-2 RED
C. prove expected assertion/JUnit behavioral RED
D. minimal production GREEN
E. focused succession + reconstruction regressions
F. T1 :app:testFast logical checkpoint
G. remote exact diff audit
H. refresh merge-readiness evidence
```

Do not merge PR #54 without explicit user authorization.

## 5. Historical acceptance context

Previously accepted checkpoints remain useful historical evidence:

```text
GCR-1 executable acceptance:
974f617adffd08cc7de0924f6fea4f96f3d73f0c

GCR-1 + GCR-2 full production T4:
474103ed13caaf34a329ca5e80e2f0ba64963b86

GCR-3 final test-quality acceptance:
383ad0e695656124f9dc608fd5ce06b72de6b499
```

They do not cover the pending-confirmation no-living-Demon state, so they are not sufficient current merge evidence by themselves.
