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

UI-NAV-1 global navigation visual unification     CURRENT — 1E + 1F COMPLETE / 1G VALIDATION
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
UI-NAV-1 live-main re-audit: 9c19484c0b6381c94440c2253079d5c2ba5f796f
UI-NAV-1 branch: codex/ui-nav-1-global-navigation-visual-unification
UI-NAV-1 draft PR: #118 — UI: unify Storyteller navigation presentation
UI-NAV-1B code checkpoint: 6ea4f9504e898d67941b3d2e75defe9b54e83c1d
UI-NAV-1B CI: run 34444205127 / CI #2150 — Android FAST + CI gate PASS
UI-NAV-1B R2: run 34444205141 / R2 #2013 — PASS
UI-NAV-1C.2 production checkpoint: 69b34fa3094abd820cdf18c4ff8770f2538699ea
UI-NAV-1C.2 cleanup head: 8b015914182c398f7d8a62bcb8ebd388209fec47
UI-NAV-1C.2 one-shot: run 34455026774 — exact diff + privacy fences + Android FAST + push + cleanup PASS
Werewolf real product deletion: 2c00d03b46a767d3fd38ff596d4f1371b7ee7403
pre-reconciliation branch head: 02342be73d0afbfe681e37c9df95b46812c7c846
UI-NAV-1D live-reconciliation docs checkpoint: 9b3e4474087db150a9ffc0fbeb9412a399ef5cfb
live main / branch head: always re-query before starting a new production slice
```

The exact `02342be...` cleanup head has no ordinary check-runs. Its PR Scope Guard and R2 Actions entries ended `action_required`; this is workflow/start gating and must not be reported as a code/test failure. The user-verified cleanup baseline passed exact diff audit, `:app:testFast`, and `:app:assembleDebug`.

`GameKind.Werewolf` / `WerewolfRecovery` that remain in production are compatibility-only. Recovery remains fail-closed. Werewolf runtime must not be reintroduced and persistence migration must not be expanded as part of UI-NAV.

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

> **CURRENT: UI-NAV-1E and UI-NAV-1F are complete. Execute UI-NAV-1G full acceptance and device/visual closeout before EPI-MQ-0.**

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

### UI-NAV-1D live-code reconciliation

The earlier roadmap wording `1D NEXT` was stale.

Historical work really completed `1D.1 / 1D.1b` for WerewolfJudge, including unified bottom navigation and viewport-bottom placement. That work was subsequently superseded by the deliberate removal of Werewolf runtime. It is therefore neither a remaining surface nor evidence that all of 1D is finished.

The current surviving-code audit proves 1D is still partial:

```text
surviving generic Screen.Game
- still uses root persistent HostToolsTopBar
- GameScreen has no equivalent bottom Host Tools owner yet

SeatingFirstSetupScreen
- still uses existing full-width confirm action
- top Settings gear remains; Settings relocation belongs to 1F

SeatingFirstGameSelectionScreen
- still has local edit/back action inside content
- no reason to invent extra selection/progression state merely for visual uniformity

game-specific setup/settings surfaces
- migrate only where existing Back/Start/Confirm callbacks map directly
- otherwise reserve/disable a slot or defer rather than changing ownership
```

Results/Review modal actions remain outside forced Previous/Host Tools/Next normalization where their semantics differ.

### UI-NAV-1D completion evidence

```text
safe setup: e63250b0791d8905a796d85776be3817431cfbf2
generic Game production: c9d6607c8c3dc3700ee9e4229252b52c74a8cded
generic Game one-shot: 34471666583 PASS (:app:testFast + :app:assembleDebug)
settings audit: 34472496605 PASS
Undercover settings: 11955a254b71d234e820035b7d2a41dd3d608b69
Clocktower settings: 412ab0b08c773767a065f1ca3a1a190b1712fadc
final settings R2: 34472655199 PASS
final settings CI: 34472655245 Android FAST + CI gate PASS
```

The remaining persistent `Screen.Game` top Host Tools chrome is gone. Pre-game surfaces reserve/disable Host Tools rather than inventing a second owner. Settings composition under Host Tools is now complete without moving Settings ownership out of App root.

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

The actual role reveal remains a separate player-facing full-screen view with no Host Tools, Previous/Next, square table, or other-player information.

Backwards identity navigation is intentionally supported so the Storyteller can re-show a role. It moves only the existing `currentDealIndex`; do not add a separate selected/furthest-completed cursor unless implementation evidence proves it necessary.

The last identity seat keeps ordinary `Next`. It leads to a dedicated identity-complete / first-night boundary, where the Storyteller may go Previous to the last identity seat or explicitly start the night.

### UI-NAV-1E / 1F completion evidence

```text
1E identity production: ba2bd1f934dc4f49519fea4f7fbeba309235004b
1E cleanup head: 35cb178c3c85f0b21cbc57360e8979f49330b1d0
1E one-shot: 34474761127 PASS
- exact four-file diff PASS
- privacy contract audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS

1F Settings production: 6dbb0d5bccd9adca8487947fb7e66f60f64937dc
1F cleanup head: 1abba7b11052ff8d044eef71f115b4d61f4f5ec1
1F one-shot: 34475818260 PASS
- exact three-file diff PASS
- Settings ownership/composition audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS
```

Identity delivery now uses the Storyteller square-table controller with a single App-root cursor and an isolated player reveal surface. Settings now appears as a tab inside the existing Host Tools overlay through reusable `SettingsContent`; App root remains the only Settings state/persistence owner and legacy `Screen.Settings` remains available.

UI-NAV-1G must use a user-authored `[full-ci]` checkpoint because `docs/TESTING_STRATEGY.md` defines T4 as the logical acceptance tier. Automated T4 is not replaced by the preceding 1E/1F FAST one-shots. Real-device visual acceptance remains a separate final product check.

### Phase-boundary product refinement

The same interaction language is preferred after the last night action:

```text
last ordinary night action
-> Next
-> dawn/broadcast/night-complete boundary
-> Previous / Host Tools / Start Day or Continue
```

However, current Dawn confirmation includes meaningful domain commit work. Reuse current transition/broadcast ownership. If a safe Previous requires moving authoritative gameplay phase commit timing, that is a real flow behavior change and must be characterized before implementation rather than hidden inside UI work. A night boundary may therefore be deferred while the identity boundary proceeds.

The accepted original read-only audit/product refinement is:

`docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`

The current live reconciliation is recorded in:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md`

## 4. UI-NAV-1 execution contract

Read first:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md`;
5. `docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md` only where a square-table product detail is relevant.

Immediate sequence:

```text
UI-NAV-1A  read-only visual / ownership audit                         COMPLETE / GO
UI-NAV-1B  establish smallest stateless three-slot bottom primitive   COMPLETE / PASS
UI-NAV-1C  migrate Clocktower night / square-table / day host flow    COMPLETE / PASS
UI-NAV-1D  migrate remaining surviving game / safe setup flows        COMPLETE / PASS
UI-NAV-1E  migrate identity delivery to privacy-safe square table      COMPLETE / PASS
UI-NAV-1F  isolate Settings-under-Host-Tools composition                    COMPLETE / PASS
UI-NAV-1G  T4 full validation + real-device visual acceptance + closeout   CURRENT
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

UI-NAV-1C reused that presentation seam without creating a new flow owner. Ordinary Clocktower night and square-table navigation, night-ready prompts, private Dawn review, New Demon confirmation, Day Overview, Nomination, Vote, EndConfirm, Slayer, Artist and Klutz now expose the unified bottom language where safe. Existing `onBack`/`onCancel` callbacks were reused where they already existed; otherwise the Previous slot remains visibly reserved but disabled. The persistent ClocktowerJudge top Host Tools entry was removed only after equivalent safe bottom access existed.

Privacy fences from 1C remain explicit: the Dawn public announcement surface and player-facing identity display contain no Host Tools navigation chrome. No gameplay phase owner, persistence/recovery owner, Planner/Reducer authority, or EPI-MQ behavior changed.

UI-NAV-1 is primarily presentation work. Do not manufacture a gameplay/domain RED when behavior is intentionally unchanged; follow `docs/TESTING_STRATEGY.md` for focused characterization, compile/static validation and exact diff audit appropriate to UI-only work.

The accepted identity-delivery backwards cursor behavior is a narrow product change, not gameplay undo. Any deeper phase-transition timing change remains separately gated by characterization.

## 5. UI-NAV-1 architecture and privacy constraints

Preserve throughout the campaign:

- `Screen` and App-root routing ownership remain unchanged;
- existing `showHostTools` / `hostToolTab` remain the Host Tools overlay owner;
- reuse `HostGameToolsScreen`; do not create a second Host Tools owner;
- no Navigation Compose, navigation coordinator, second state owner or callback mega-bag;
- Clocktower night-step `canGoPrevious` / `onPrevious` / `onNext` seams remain presentation callbacks, not a new flow model;
- `ClocktowerGameSession`, Planner and Reducer keep gameplay/session authority;
- persistence/recovery behavior is outside UI-NAV-1 scope;
- special Next enabled/disabled and commit semantics must remain unchanged unless a separately characterized boundary-flow change is opened;
- `Previous` on ordinary gameplay steps must not be reinterpreted as gameplay undo;
- identity delivery may use Previous/Next under Storyteller control to move `currentDealIndex`;
- identity Previous/Next must never automatically reveal a role;
- the identity square table may show only privacy-safe seat/name/highlight information;
- Host Tools may be available on the Storyteller identity controller but must not appear on the player-facing role display;
- player-facing identity reveal remains isolated and contains only the selected player's role information and safe hide/finished action;
- hiding identity returns to the same Storyteller seat and does not auto-advance;
- the three-slot geometry may reserve disabled positions where capability is intentionally unavailable;
- phase-boundary/broadcast Previous may be added only through the current flow owner; do not introduce a second phase owner or hidden rollback system;
- moving authoritative phase-transition commit timing requires dedicated behavior characterization before production change;
- Settings-under-Host-Tools is allowed only as an isolated 1F composition slice while all Settings state/mutation ownership remains at App root;
- do not immediately delete legacy `Screen.Settings` during 1F;
- no EPI-MQ ranking/recommendation changes during UI-NAV-1;
- no renewed D6 decomposition for file-size reasons;
- no Werewolf runtime restoration or persistence migration expansion.

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
