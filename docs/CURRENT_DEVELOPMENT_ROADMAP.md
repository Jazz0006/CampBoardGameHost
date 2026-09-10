# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-10 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 transaction-application viability audit        COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE

UI-R5 square-table convergence                    COMPLETE / merged in PR #117
UI-R5 final logical T4                            PASS
UI-R5 post-merge main CI                          PASS
UI-R5 Field Test APK                              PASS
UI-R5 real-device acceptance                      PARTIAL / follow-up remains

UI-NAV-1 global navigation visual unification     CURRENT — 1B COMPLETE / 1C NEXT
EPI-MQ / Productive Uncertainty                   NEXT after UI-NAV-1 — EPI-MQ-0 baseline re-audit
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign evidence is historical and must not be treated as current execution authority.

## 2. Stable inherited baseline and current UI-NAV branch

```text
UI-R5 merged production baseline: b47b00fd0c727e048dcb1b57260b8dd6fff466a1
merged PR: #117 — UI: unify Storyteller night roles on square table
final branch head: 99bb4e5323f68231eaa1e08e16520a4340f56faf
final logical T4 checkpoint: 2958f334fc7cccd59ed2e75a3bdaa60684492292

UI-NAV-1 campaign-start main: 9c19484c682044bb469d90fb7522810ca49ecac2
UI-NAV-1 branch: codex/ui-nav-1-global-navigation-visual-unification
UI-NAV-1 draft PR: #118 — UI: unify Storyteller navigation presentation
UI-NAV-1B code checkpoint: 6ea4f9504e898d67941b3d2e75defe9b54e83c1d
UI-NAV-1B CI: run 34444205127 / CI #2150 — Android FAST + CI gate PASS
UI-NAV-1B R2: run 34444205141 / R2 #2013 — PASS
live main: always re-query before starting a new slice; docs-only commits may advance it
```

Inherited validation evidence:

```text
T4 CI #2143 / run 34435295215             PASS
- Android full unit tests + debug APK      PASS
- ASP contract tests                       PASS
- Real Clingo cross-validation             PASS
- CI gate                                  PASS
R2 #2010 / run 34435295217                PASS

post-merge main CI #2146 / run 34437247431 PASS
Field Test APK #34 / run 34437247434       PASS
```

PR #117 is closed and merged. Its former branch/handoff/audit documents are historical evidence only.

## 3. Current priority — UI-NAV-1 Global Navigation Visual Unification

> **CURRENT: perform a short presentation-first navigation convergence campaign before EPI-MQ-0.**

Product decision:

```text
No persistent global top bar.

Screen/content owns:
- title / current-stage text
- instructions
- square table / current task content
- optional local progress

Bottom action geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

Visual hierarchy:

```text
Previous   = secondary
Host Tools = tertiary / utility
Next       = primary
```

The three positions are a stable visual language, not a new source of navigation truth. Existing screen/root owners continue to decide capability, callbacks and enabled state.

Progress is **screen-owned and optional**. UI-NAV-1 does not impose a shared identity/night progress model and does not make progress part of this campaign's acceptance gate.

### Identity-delivery product refinement

Clocktower identity delivery should use the square-table visual language rather than retain the old progress-bar/pass-phone controller.

Target:

```text
Storyteller square-table controller
- highlight current player
- center `N / total`
- center `Show identity to seat N · player name`
- explicit `Show identity`
- bottom Previous / Host Tools / Next
```

The actual role reveal remains a separate player-facing full-screen view with no Host Tools, Previous/Next, or other-player information.

Backwards identity navigation is intentionally supported so the Storyteller can re-show a role. It moves only the existing identity-delivery cursor; do not add a separate furthest-completed cursor unless implementation evidence proves it necessary.

The last identity seat keeps ordinary `Next`. It leads to the first-night boundary/prompt, where the Storyteller may go Previous or explicitly start the night.

### Phase-boundary product refinement

The same interaction language applies after the last night action:

```text
last ordinary night action
-> Next
-> existing dawn/broadcast/night-complete prompt
-> Previous / Host Tools / Start Day or Continue
```

Reuse current transition/broadcast ownership. If a safe Previous requires moving the authoritative gameplay phase commit, that is a real flow behavior change and must be characterized before implementation rather than hidden inside UI work.

The accepted read-only audit/product refinement is:

`docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`

## 4. UI-NAV-1 execution contract

Read first:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md`;
4. `docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md` only where a square-table product detail is relevant.

Immediate sequence:

```text
UI-NAV-1A  read-only visual / ownership audit                         COMPLETE / GO
UI-NAV-1B  establish smallest stateless three-slot bottom primitive   COMPLETE / PASS
UI-NAV-1C  migrate Clocktower night / square-table / day host flow    NEXT
UI-NAV-1D  migrate remaining judge / game / safe setup flows
UI-NAV-1E  migrate identity delivery to privacy-safe square table
UI-NAV-1F  isolate Settings-under-Host-Tools composition if still narrow
UI-NAV-1G  focused/full validation + real-device visual acceptance + closeout
```

UI-NAV-1B established `HostBottomActionBar` as a pure presentation component:

```text
Previous = secondary OutlinedButton slot
Host Tools = tertiary TextButton slot
Next = primary Button slot
all slots equal width
independent enabled/visible controls
48dp minimum slot height
single-line labels with overflow protection
no Screen/session/domain/Planner/Reducer inputs
```

UI-NAV-1B intentionally did not wire this component to production flow callbacks; that callback plumbing belongs to 1C.

UI-NAV-1 is primarily presentation work. Do not manufacture a gameplay/domain RED when behavior is intentionally unchanged; follow `docs/TESTING_STRATEGY.md` for focused characterization, compile/static validation and exact diff audit appropriate to UI-only work.

The accepted identity-delivery backwards cursor behavior is a narrow product change, not gameplay undo. Any deeper phase-transition timing change remains separately gated by characterization.

## 5. UI-NAV-1 architecture and privacy constraints

Preserve throughout the campaign:

- `Screen` and App-root routing ownership remain unchanged;
- existing `showHostTools` / `hostToolTab` remain the Host Tools overlay owner;
- no Navigation Compose, navigation coordinator, second state owner or callback mega-bag;
- Clocktower night-step `canGoPrevious` / `onPrevious` / `onNext` seams remain presentation callbacks, not a new flow model;
- `ClocktowerGameSession`, Planner and Reducer keep gameplay/session authority;
- persistence/recovery behavior is outside UI-NAV-1 scope;
- special Next enabled/disabled and commit semantics must remain unchanged unless a separately characterized boundary-flow change is opened;
- `Previous` on ordinary gameplay steps must not be reinterpreted as gameplay undo;
- identity delivery may use Previous/Next under Storyteller control to move the current deal/reveal cursor;
- identity Previous must never automatically reveal the previous player's role;
- the identity square table may show only privacy-safe seat/name information;
- Host Tools may be available on the Storyteller identity controller but must not appear on the player-facing role display;
- player-facing identity reveal remains isolated and contains only the selected player's role information;
- the three-slot geometry may reserve disabled positions where capability is intentionally unavailable;
- phase-boundary/broadcast Previous may be added only through the current flow owner; do not introduce a second phase owner or hidden rollback system;
- moving authoritative phase-transition commit timing requires dedicated behavior characterization before production change;
- Settings-under-Host-Tools is allowed only as an isolated composition slice while all Settings state/mutation ownership remains at App root;
- no EPI-MQ ranking/recommendation changes during UI-NAV-1;
- no renewed D6 decomposition for file-size reasons.

The core rule is:

> **Standardize navigation presentation, not navigation ownership.**

## 6. UI-R5 post-merge device follow-up

UI-R5 implementation and automated validation are complete and merged. Real-device acceptance remains a **field-test follow-up**, not an excuse to keep the development campaign open indefinitely.

Already user-reported device PASS:

- Washerwoman / Librarian / Investigator pair-information baseline;
- Chef square-table.

Still worth exercising during normal field testing:

- Empath;
- Undertaker;
- Ravenkeeper;
- Spy;
- Clockmaker;
- Sage;
- original dynamic-trigger regression path:
  `Monk protects Ravenkeeper -> change Monk target -> Demon kills Ravenkeeper -> Ravenkeeper ability step appears`.

UI-NAV-1 real-device checks should naturally re-exercise several of these screens, but a visual PASS must not be treated as proof of gameplay correctness and CI must not be treated as proof of device acceptance.

## 7. Next program after UI-NAV-1 — EPI-MQ-0 baseline re-audit

Immediately after UI-NAV-1 closes, resume the Epistemic Misinformation Quality / Productive Uncertainty program from the merged live architecture.

The long-lived product objective remains:

> For Drunk/Poisoned information, choose misinformation that creates credible, interactive, sustainable, breakable and fair mistaken worlds rather than merely choosing an answer that is locally false.

The existing design plan remains:

`docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

Its historical implementation wording must not override live architecture. EPI-MQ resumes with **re-audit, not immediate production ranking changes**.

The preserved EPI-MQ-0 sequence is:

```text
EPI-MQ-0A  confirm live main / architecture / existing epistemic owners
EPI-MQ-0B  map legal misinformation candidate -> hypothetical visible observation -> world evaluation path
EPI-MQ-0C  build a small Trouble Brewing behavior corpus: good / acceptable / poor misinformation
EPI-MQ-0D  identify the narrow typed seam and test owner for BEFORE/AFTER hypothetical world evaluation
EPI-MQ-0E  record GO / MODIFY / NO-GO for the old EPI-MQ-1 proposal
```

The preserved handoff is:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`

It is queued, not historical, and becomes active again after UI-NAV-1 closes.

## 8. Architectural constraints carried forward into EPI-MQ

Preserve unless a new EPI audit explicitly proves a better boundary:

- `ClocktowerGameSession` remains the canonical writable session owner;
- Planner/Reducer keep gameplay-rule authority;
- role semantics / legal candidate generation remain separate from misinformation-quality ranking;
- player-visible epistemic replay must not ingest Storyteller-hidden action targets merely to make scoring convenient;
- `AbilityObservation`, historical timeline and world-set semantics remain typed; do not reconstruct semantics from localized UI strings;
- timeouts/resource exhaustion in exact/compressed world evaluation must not be interpreted as false UNSAT;
- UI composition must not become the owner of epistemic/ranking truth;
- no broad Host/App-root rewrites or renewed D6 decomposition for file-size reasons;
- no recommendation-provider replacement during EPI-MQ-0 unless the audit proves it is a prerequisite and the roadmap is explicitly updated.

## 9. Historical UI-R5 evidence

UI-R5 handoffs have moved to:

`docs/archive/handoffs/`

UI-R5 audits/acceptance evidence and closeout index have moved to:

`docs/archive/checkpoints/ui-r5/`

The durable product design remains active at:

`docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

Do not load the full UI-R5 archive in a normal UI-NAV or EPI-MQ session.

## 10. Later program after EPI-MQ

`UX-R6 recommendation-provider replacement` remains queued after EPI-MQ, as previously recorded in the documentation index. It is not authorized by UI-NAV-1 and may be reprioritized later only by an explicit roadmap update.

## 11. Current active handoff

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md`

Only this handoff is current.

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md` is a **queued next-program handoff**, not historical evidence; it becomes active again only after UI-NAV-1 closeout.

Any `NEXT_DEVELOPMENT_HANDOFF*` under `docs/archive/` is historical.