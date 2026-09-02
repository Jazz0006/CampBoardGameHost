# BoCT 信息展示与手动选择界面优化设计参考

> Date: 2026-09-02 Australia/Sydney  
> Status: **ACTIVE PRODUCT/UI REFERENCE**  
> Scope: Blood on the Clocktower Storyteller information presentation, Manual clue selection, player-seat interaction, Fortune Teller target/result flow  
> Near-term field-test target: **2026-09-04 (Friday)**

## 1. Purpose

This document freezes the product decisions discussed on 2026-09-02 for the next UI campaign.

The objective is not to redesign the recommendation algorithm. The objective is to make the current, already-correct legal/recommendation/Manual foundation much easier to operate on a real phone during a live game.

Primary user problems to solve:

1. the current information-choice screen shows recommendation reasons that are not yet meaningful enough to help the Storyteller;
2. Manual clue selection is too inline / form-like and does not use the player layout as the main interaction surface;
3. final information display needs much more screen area and clearer seat emphasis;
4. Fortune Teller needs a direct two-target interaction followed by a simple result decision;
5. reliable vs Storyteller-discretionary information needs a clear operational distinction without leaking hidden reliability state to the player-facing display;
6. the UI should be ready for later Productive Uncertainty / cognitive-consistency recommendations without requiring another interaction redesign.

## 2. Frozen product decisions

### 2.1 Remove normal recommendation-reason prose for now

Normal Storyteller UI should not display the current coarse recommendation reason / warning text as if it were strategically meaningful explanation.

For the current campaign:

- keep the recommendation candidate itself;
- keep Top-1 / alternatives behavior established by UX-R4;
- hide or remove normal recommendation-reason prose from the main interaction;
- diagnostics may remain available in debug/internal surfaces;
- later EPI-MQ / Productive Uncertainty may reintroduce explanation when the explanation is grounded in real epistemic metrics.

This is a presentation decision, not a ranking change.

### 2.2 Replace the large round-table concept with a full-screen square-table layout

The player layout should use the phone screen perimeter more efficiently.

Preferred visual model:

```text
+----------------------------------+
| P1          P2          P3       |
|                                  |
| P12                            P4 |
|                                  |
| P11       center content       P5 |
|                                  |
| P10                            P6 |
|                                  |
| P9          P8          P7       |
+----------------------------------+
```

The exact seat distribution is implementation-owned, but the product contract is:

- player seats are arranged around the four edges of a rectangular/square table;
- the table uses most of the available viewport;
- the center is reserved for role / clue / instruction / confirmation content;
- player labels must remain readable on a phone;
- seat identity is stable and never derived from filtered-list position;
- selected / highlighted seats remain visually distinct even on a small screen.

Phone portrait is the primary target. Landscape may improve naturally but is not required for the Friday field-test gate.

### 2.3 Manual clue selection becomes a dedicated interaction surface

Clicking **Manual / 手动选择展示信息** should enter a dedicated selection surface rather than merely expanding more controls inline in the existing night-step card.

For pair-information roles such as Washerwoman / Librarian / Investigator:

```text
Manual
  -> choose clue role / special zero-case
  -> choose first player on square table
  -> choose second player on square table
  -> resolve exact typed legal candidate
  -> confirm / display
```

Rules:

- Manual continues to use the complete legal semantic domain;
- recommendation shortlist never defines Manual legality;
- localized labels must not be parsed to reconstruct semantic identity;
- impossible combinations should be disabled/prevented by the legal domain, not accepted and rejected later by string logic;
- after the first seat is selected, only legal second-seat continuations should remain selectable where practical;
- tapping a selected seat again should allow correction before confirmation;
- Librarian's legal zero-Outsider case remains an explicit non-seat option;
- Investigator zero-Minion remains unavailable because it is not legal.

### 2.4 Final player information display uses the same square-table visual language

When information is ready to show to a player, the display should use the same full-screen table geometry.

The display screen should prioritize:

1. the intended clue/result;
2. the relevant highlighted players, if the proposition targets seats;
3. the shown role / numeric / Yes-No result;
4. minimal surrounding chrome.

The table may occupy most of the screen because the intended use is to physically hand/show the phone to the relevant player.

The player-facing display must never expose hidden Storyteller-only state such as:

- "this information is poisoned";
- "this result is false";
- recommendation ranking reason;
- actual hidden role/target facts not contained in the intended player-visible proposition.

### 2.5 Reliable / unreliable operational distinction belongs to the Storyteller side

The Storyteller needs to know whether the result is mechanically fixed or whether the legal domain allows Storyteller discretion.

Preferred operational wording:

```text
Result determined / 结果已确定
```

or

```text
Storyteller choice / 说书人可裁定
```

A secondary technical badge may say reliable/unreliable if useful, but the primary UI should describe what the Storyteller can do.

Important:

- this indicator is Storyteller-side only;
- it disappears from the player-facing display;
- "unreliable" does not mean "must be false";
- poisoned/drunk information may legally include the truthful result where current semantics allow it;
- legality remains upstream of presentation.

### 2.6 Highlight rules

Use one stable set of seat visual states across Manual, targeting and display:

```text
neutral
selectable
selected-first
selected-second
highlighted-information
unavailable/disabled
```

Implementation may use color, border, fill, icon or label emphasis, but information must not rely on color alone.

Minimum readable identity per seat:

- seat number;
- player name or short player label.

Storyteller-only selection screens may optionally include alive/dead status where already available and useful, but this campaign should not add new game-state semantics solely for decoration.

### 2.7 Information-display lifecycle

The interaction should have explicit stages rather than mixing selection and player display in one surface.

Conceptual lifecycle:

```text
Night step
  -> configure / choose targets
  -> choose or confirm clue/result
  -> Storyteller preview / ready state
  -> player-facing full-screen display
  -> close/return
  -> existing confirmed observation/history path
```

The exact callback order already protected by current architecture must remain unchanged unless a real bug requires a separately tested change.

Selection state is draft state. Showing/confirming the information is the boundary that commits the existing structured observation/confirmation behavior.

### 2.8 Fortune Teller two-target flow

Fortune Teller should retain the square-table interaction throughout targeting.

Flow:

```text
Fortune Teller wakes
  -> square table
  -> select player 1
  -> select player 2
  -> result decision appears
```

After two distinct legal targets are selected:

#### Fixed result

If the complete legal result domain contains only the determined result, show one primary action only:

```text
[ YES / 有 ]
```

or

```text
[ NO / 没有 ]
```

Do not show a fake Manual alternative when the alternative is not legal.

#### Storyteller-discretionary result

If both Yes and No are legal:

```text
Recommended
[ YES / 有 ]

Other legal result
[ NO / 没有 ]
```

or the reverse according to the existing recommendation provider.

Rules:

- the recommended result is primary, not exclusive;
- the other legal result remains immediately visible;
- no extra Manual drill-down is needed for a two-value domain;
- the result proposition remains bound to the exact selected pair;
- no localized-label parsing;
- the current Fortune Teller Red Herring / Recluse semantic authority is not changed by this UI campaign.

### 2.9 Recommendation and Manual coexistence

The permanent ownership model remains:

```text
complete legal semantic domain
        |
        +--> Manual / direct legal selection
        |
        +--> Recommendation Provider
                  -> presentation
```

For pair/combinatorial domains:

```text
Top-1
+ 0-2 alternatives
+ Manual
```

For small domains:

```text
primary recommendation
+ every other legal value that comfortably fits
```

The UI campaign changes spatial interaction and presentation only.

## 3. Suggested screen structures

### 3.1 Pair recommendation screen

```text
< Back                       Washerwoman

Recommended clue
[ Role X: P2 / P7 ]

Other recommendations
[ ... ]
[ ... ]

[ Manual / 手动选择 ]
```

Recommendation reasons are not shown in normal mode.

### 3.2 Pair Manual screen

```text
< Back                    Manual clue

[ Role selector / special zero-case ]

+----------------------------------+
| P1          P2          P3       |
|                                  |
| P12                            P4 |
|                                  |
|           current role           |
|        selected: P2 + P7         |
|                                  |
| P10                            P6 |
|                                  |
| P9          P8          P7       |
+----------------------------------+

[ Confirm clue ]
```

### 3.3 Final player display

```text
+----------------------------------+
| P1        [P2]          P3       |
|                                  |
| P12                            P4 |
|                                  |
|          WASHERWOMAN              |
|          LIBRARIAN                |
|          etc.                     |
|                                  |
| P10                            P6 |
|                                  |
| P9          P8         [P7]       |
+----------------------------------+
```

The center content changes by proposition family. The relevant seats are strongly highlighted.

### 3.4 Fortune Teller

Before two targets:

```text
Select two players
+ square table
```

After two targets and fixed result:

```text
Selected: P3 + P8

Result determined
[ YES ]
```

After two targets and discretionary result:

```text
Selected: P3 + P8

Storyteller choice
Recommended
[ YES ]

Other legal result
[ NO ]
```

## 4. Engineering interpretation based on current code

The current production UI already has typed candidate and structured confirmation foundations from UX-R2B through UX-R5. The campaign should reuse them rather than introducing a second state model.

Preferred ownership:

```text
ClocktowerNightStepUi
   -> orchestration only

new dedicated square-table / selection composables
   -> seat layout + visual state

existing legal-domain / structured adapters
   -> candidate legality and typed identity

existing display callback / observation commit path
   -> final confirmation / publication
```

Because `ClocktowerNightStepUi.kt` is already large, new reusable table/interaction presentation should prefer dedicated files. Large-file wiring changes should use the repository-approved exact-anchor one-shot route when direct connector replacement would be unsafe.

## 5. Friday field-test acceptance target

For the 2026-09-04 live game, the minimum acceptable result is:

1. square-table seat layout is readable on the user's real phone;
2. pair Manual selection is usable without scrolling through a long inline form;
3. recommendation reasons no longer clutter the normal selection surface;
4. final player display clearly highlights relevant seats and clue/result;
5. Fortune Teller can select two targets directly on the table and then choose the legal result with fixed-vs-discretionary behavior;
6. no regression to typed legal-domain authority, Spy/Recluse registration, reliable/unreliable semantics, or confirmed observation history;
7. no first-night display crash regression.

Visual polish beyond this acceptance target is optional and should not block field testing.

## 6. Deferred after the field-test build

Not required for the first UI campaign checkpoint:

- animation-heavy transitions;
- tablet-specific layout tuning;
- theme redesign unrelated to information interaction;
- new recommendation intelligence;
- Productive Uncertainty explanation text;
- broad accessibility audit beyond readable labels and non-color-only selection states;
- broad role coverage where a role does not already have stable typed production semantics.

## 7. Relationship to EPI-MQ

EPI-MQ remains the next algorithm campaign after this short UI campaign.

The UI should intentionally expose a stable place for future recommendation explanation, but current coarse recommendation reasons stay hidden. When Productive Uncertainty later produces meaningful diagnostics, they can be surfaced without changing Manual authority, square-table targeting, or the final player-display lifecycle.
