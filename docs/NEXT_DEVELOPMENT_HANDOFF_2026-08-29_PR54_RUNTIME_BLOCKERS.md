# Next Development Handoff — PR #54 Runtime Blockers

> Date: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> PR: #54  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Status: **TWO POST-ACCEPTANCE P1 RUNTIME BLOCKERS — PR MUST REMAIN UNMERGED**

## 1. Why this handoff exists

The earlier GCR acceptance checkpoints remain valid evidence for the behavior they actually exercised, but a later review found two normal runtime paths that were not covered by those tests. Both are P1 merge blockers for PR #54.

Last audited pre-fix PR head:

```text
e9a30ffc353d870df388329986be433268558661
```

The blocker documentation commit begins after that head.

## 2. PR54-P1-1 — First-night Fortune Teller projection crash

### Confirmed failure path

In `ClocktowerHostScreen.kt`, once Fortune Teller has selected two players, the Host unconditionally constructs the Fortune Teller `OTHER_NIGHT` interaction id and calls `effectiveNightStateAt(...)`.

On First Night, `otherNightCanonicalInteractionIds` is intentionally empty. The effective-state projector rejects the requested interaction as unknown, so the normal First Night Fortune Teller path crashes after the second target is selected.

### Required contract

```text
First Night
-> use base/current persisted role state for Fortune Teller matching
-> do not evaluate Other Night chronology projection

Other Night
-> continue using canonical same-night effective-state projection
```

The projector must stay fail-closed for unknown interactions. Do not weaken `ClocktowerEffectiveNightState` to hide the caller bug.

### Tests-first slice

Required RED:

- callable typed seam;
- First Night returns the base role authority;
- an Other Night projection provider that would fail/throw must **not be evaluated** on First Night;
- Other Night must evaluate and return the projected role authority.

Then wire Fortune Teller matching through that seam with the smallest Host edit possible.

Focused GREEN should cover the new seam plus existing Fortune Teller/effective-state/First Night/Other Night regressions. Keep this slice independent of Demon succession.

## 3. PR54-P1-2 — Imp self-kill pending successor Host reconstruction crash

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

This is the **second** micro-slice. Do not mix it into PR54-P1-1.

## 4. Execution order

```text
A. PR54-P1-1 typed RED
B. prove expected RED
C. minimal GREEN
D. focused affected regressions + diff audit
E. PR54-P1-2 typed RED/GREEN as a separate slice
F. logical checkpoint validation
G. refresh PR merge-readiness evidence
```

Do not merge, rebase, force-push, or broaden into GCR-4/5, A3, App-root S9.2, Host/A4/ZDD promotion, generic misinformation, or unrelated recommendation work.

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

They do not cover the two newly identified runtime paths, so they are no longer sufficient current merge evidence by themselves.
