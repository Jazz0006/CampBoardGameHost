# UI-NAV-1 Global Navigation Visual Unification — Closeout

> Finalized: 2026-09-11 Australia/Sydney  
> Branch: `codex/ui-nav-1-global-navigation-visual-unification`  
> PR: `#118 — UI: unify Storyteller navigation presentation`  
> Final status: **IMPLEMENTATION COMPLETE / REAL-DEVICE ACCEPTED / MERGE AUTHORIZED BY USER**

## 1. Product result

UI-NAV-1 standardized Storyteller navigation presentation without introducing a new navigation or gameplay owner.

Accepted visual language:

```text
No persistent global top bar.
Content owns stage title / instructions / table / local progress.
Bottom action geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

`Previous` remains secondary, `Host Tools` remains tertiary/utility, and `Next` remains primary. Existing App-root routing, session, gameplay, persistence and recovery owners remain authoritative.

## 2. Major completed slices

```text
UI-NAV-1B shared HostBottomActionBar                 COMPLETE
UI-NAV-1C Clocktower night/square-table/day flow    COMPLETE
UI-NAV-1D remaining safe setup/game/settings flow   COMPLETE
UI-NAV-1E identity square-table controller          COMPLETE
UI-NAV-1F Settings under existing Host Tools        COMPLETE
UI-NAV-1G validation/device closeout                 COMPLETE
```

Key historical checkpoints remain available in Git history and earlier audit records; this closeout intentionally does not duplicate the full checkpoint ledger.

## 3. Final ownership/privacy contracts

The final implementation preserves these contracts:

- `HostBottomActionBar` is a stateless presentation primitive;
- App root still owns `Screen` routing, `showHostTools`, `hostToolTab`, identity cursor state, Settings state and Settings persistence mutations;
- `HostGameToolsScreen` remains the single Host Tools owner;
- Settings is composed into Host Tools through reusable content rather than duplicate state;
- player-facing role reveal remains isolated from Host Tools, square-table cross-player data and other Storyteller-only information;
- identity Previous/Next moves the Storyteller-controlled identity cursor and never auto-reveals a role;
- ordinary gameplay Previous was not reinterpreted as gameplay rollback;
- authoritative Night -> Day commit timing was not moved for visual uniformity;
- no Navigation Compose coordinator, second phase owner, persistence redesign, EPI-MQ change, or Werewolf runtime restoration was introduced.

## 4. Real-device follow-up fixes

Device testing exposed presentation defects after the initial implementation:

1. the identity-controller center message could collapse to approximately one-character width on a narrow/high-player-count layout;
2. night-flow Previous / Host Tools / Next was still rendered inside the square-table workspace on several role families instead of as a stable screen-bottom row;
3. a later compact-night follow-up adjusted night chrome and localized the Spy grimoire action without changing gameplay ownership.

The identity center now consumes the already-constrained center workspace width instead of applying destructive extra horizontal padding. Night-role families now render the shared navigation row outside/below the square-table area while preserving the existing callbacks and enabled semantics.

Final production follow-up checkpoint before documentation closeout:

```text
e21bf746c72f89754b7051c36b7a6e9a854ea385
ui: compact night chrome and localize Spy grimoire action

cleanup head before docs closeout:
ac43becbabac0957f13ea02c285704cc3e3c55a6
chore: remove compact night fix workflow
```

Temporary device-fix workflows/scripts were removed from the branch after use.

## 5. Acceptance

The user performed another real-device pass after the follow-up fixes and reported no additional issue that should block this campaign. On 2026-09-11 the user explicitly authorized documentation cleanup followed by merge of PR #118.

A fresh logical full-CI checkpoint is required on the final documentation head before merge so the current branch head, rather than an older pre-device-fix checkpoint, receives the repository's T4 acceptance gates.

UI-NAV-1 should not be reopened after merge unless a reproducible regression is found.

## 6. Next priority

The user reprioritized the next development target to **ROLE-ROTATION-1 — consecutive role repeat avoidance**.

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md`

The next conversation should start with a read-only audit of role-set ownership, player assignment, randomness, recent-history persistence and stable human-player identity. Do not jump directly into production changes.

EPI-MQ remains queued behind this work unless explicitly reprioritized again.
