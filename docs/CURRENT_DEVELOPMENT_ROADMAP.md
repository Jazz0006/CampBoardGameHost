# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-07 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **This file is the single current project-status and execution-priority authority.**

## 1. Live context and integrated work

Audited live main: `ac71cbe392fb542727dc0c2d69ac82c5fdc0435e` (PR #110 merge).
Always re-query GitHub before implementation, validation or merge; do not reuse a merged work branch.

| Integrated PR | Result |
|---|---|
| #99 / #100 | Persistent Host Table, inline actor/wake cue and shared square-table readability |
| #101 / #102 | Dead ordinary-role step omission and Manual Demon bluff consistency |
| #104 / #105 | Obsolete source-wiring guard retirement and committed Drunk shown-identity ownership |
| #106 | D1–D5 Night Step UI / publication ownership decomposition; merged as `8fd6b26e` |
| #107 | Offline crash flight recorder, export and execution preflight breadcrumbs |
| #108 | Recorder installation in Application before Activity startup/restoration |
| #110 | Optional Poisoner source chronology guard plus execution/dusk regression coverage |

#106 merged head was `9e5016d5`; earlier D5 test SHAs are historical evidence, not the final live baseline.
Unmerged remote branches are not part of this baseline.

Verified executable baseline on **merged main `ac71cbe3`**:

- [CI 34069723548](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34069723548): success;
  job steps confirm full Android unit tests and debug APK build, ASP contracts, Real Clingo and CI gate passed.
- [Field Test APK 34069723532](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34069723532): success.
  This establishes workflow success, not physical-device acceptance.
- #110 PR head `72e063dd` has earlier failed CI/R2 runs (34068042776 / 34068042798); jobs API returned
  no jobs, so their cause is not inferred. Later merged-main full CI supplies current product baseline.
  It does not retroactively mark the earlier R2 run successful; next implementation checkpoint needs R2.
- This planning audit did not execute local Android tests or a new device pass.

## 2. Current priority — D6 planning completed, implementation next

The latest user request selects **D6 planning** after #106 and subsequent fixes. This supersedes the
older schedule that deferred all D6 work until UI-R5. Production D6 implementation has not started.
The complete local workspace may edit large files directly as authorized; behavioral and merge gates remain.

Active contract: [D6 App/Host ownership handoff](NEXT_DEVELOPMENT_HANDOFF_2026-09-07_D6_APP_HOST_OWNERSHIP.md).

| Step | Scope | Current status |
|---|---|---|
| D6.0 | Recheck implementation baseline and freeze existing schema v3 effective values | Planned startup check |
| D6.1 | Immutable save capture and complete active-game snapshot encoding boundary | First implementation slice planned |
| D6.2 | Complete restore parsing/validation before App state application | Planned; persistence milestone closes here |
| D6.3 | Cohesive night draft/confirmed state ownership using existing transaction/reducer authorities | Re-audit after persistence milestone |
| D6.4 | Host/App command effect coordination, one flow at a time | Re-audit after state ownership milestone |

D6.1 means **active-game** persistence across Undercover, Werewolf and Clocktower. Keep SharedPreferences
commit timing, A4 durability release, schema v3 and private UI lifetimes unchanged. No giant ViewModel,
application-wide intent/context, algorithm-policy work or new persistence technology.

The audit found duplicate effective checkpoint mapping and interleaved restore parsing/state assignment.
These justify a codec/parser boundary; they are not proof of another currently reproduced crash.
Full setup/archive extraction remains conditional, not a mandatory extra phase.

D6.0 is a startup check within the first implementation slice, not a separate ceremony/PR. D6.1 and D6.2
have separate reviewable checkpoints. Planning does not authorize merge, mark-ready, or changes to main.
After each meaningful checkpoint reassess risk and continue only the selected scope.

## 3. Historical decomposition evidence

D1–D5 are completed and merged through #106. Historical per-slice diffs, CI links, and the pre-merge audit
are preserved in [D1–D5 roadmap record](archive/checkpoints/NIGHT_STEP_D1_D5_ROADMAP_RECORD_2026-09-06.md).
Its NEXT/draft/unmerged directives are historical, not current instructions.
The [old handoff](archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md)
and [Night Step audit](CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md) remain reference evidence.

Drunk shown identity remains a committed setup fact. Recommendation consumes it; it does not select,
replace, emit or lock it. Template history applies to template selection, with no separate Drunk shown
identity cooldown. See `DRUNK_SHOWN_IDENTITY_OWNERSHIP_REPAIR_2026-09-05.md` for the historical repair.

## 4. UI-R5 — stability gate remains open

The audited documents contain no completed device matrix; do not infer a pass from a merge or APK build.
Device work may proceed independently; run affected persistence/restore scenarios at D6 milestones and
complete stabilization before EPI-MQ/UX-R6. Fix concrete defects without mixing a broad visual redesign.

Record installable APK SHA, package/version, build provenance, device/Android version, expected/actual
behavior and failure evidence. Start with one complete 8–10-player manual/assisted game through the
second night, targeted automatic/edge scenarios, then a 15-player layout pass.

| Device scenario | Acceptance condition |
|---|---|
| First-night Minion/Demon reveal | Opens/closes without crash; correct player-facing information and return step. |
| Single-target roles and back/next | Selection stays with the correct role; actor cue is independent of eligibility; no stale child UI across actions. |
| Fortune Teller / Chambermaid | Ordered two-seat selection, edit/deselect and deterministic/discretionary result controls remain correct. |
| Pair Manual | Role/pair/zero-case selection, cancel/reopen and confirm resolve the chosen clue; candidate changes reset appropriately. |
| Drunk/poison and Spy/Recluse routes | Correct actual/shown identity, legal candidate route and registration; no old result after a changed decision. |
| Reopen first-night result | Reveal opens again; history/observation is not duplicated and original migrated fact is retained. |
| Mayor/succession and death-trigger role | Manual/automatic gating, disabled targets and Ravenkeeper reveal remain usable. |
| 15 seats, long names, navigation/lifecycle | Labels/buttons usable, no inset overlap; background/foreground and supported restore flow preserve intended state. |

Also cover seating/start/reorder, game selection, Day Overview/nomination/vote, Player Reveal privacy,
and D6-specific pause/resume/process-restart saves across all three games. Include executed Poisoner,
dusk expiry, absent source chronology, night draft/confirmed separation and spent ghost votes.

## 5. EPI-MQ / Productive Uncertainty — AFTER UI-R5

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Quality ranking remains downstream of legal semantic authority.

A4/ZDD remains shadow/prototype unless separately reactivated and validated.

## 6. UX-R6 — AFTER EPI-MQ

Replace the legacy recommendation provider only after EPI-MQ correctness, quality, performance and rollout gates pass.

Preserve Manual independence, typed outcome identity, stabilized Storyteller UI and safe fallback behavior.

## 7. Explicitly deferred / not part of the current architecture slice

- generic impaired-information / aggregate-plan interaction-quality redesign;
- Demon Bluff recommendation-quality redesign;
- Public Claim History;
- Sequential Vote redesign;
- broad unsupported-script expansion;
- A4/ZDD production rollout;
- broad App-root decomposition unrelated to the active ownership problem;
- recommendation-quality algorithm redesign during D6;
- gameplay-rule changes hidden inside structural refactoring.

## 8. Permanent architecture invariants

### Epistemic / information authority

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent rules include:

- UI/presentation is downstream of legality/truth authority;
- Manual remains permanent Storyteller authority within the legal domain;
- exact typed outcome identity survives presentation and confirmation;
- durable visible observations exclude Storyteller-hidden facts;
- structural refactoring must not alter rules or transaction ordering.

### Persistent Host Table

```text
stable typed ClocktowerSeatId
-> stable physical table position
-> Storyteller-private typed seat presentation
-> phase/action-specific center task
```

Actor/wake cue and target state are orthogonal presentation concepts. The final UI-N1 product decision does not require a separate wake acknowledgement phase.

## 9. Evidence and documentation policy

Follow AGENTS.md and TESTING_STRATEGY.md: owning tests first, durable characterization for actual gaps,
no manufactured RED or per-microstep full runs. Persistence/restore and central transaction checkpoints
require full CI and R2. Maintain schema/identity, Compose/effect lifetime and callback/failure ordering.

`docs/README.md` is navigation; this roadmap is status authority; the D6 handoff defines implementation
boundaries. Historical checkpoint records stay in archive and do not compete with current instructions.
