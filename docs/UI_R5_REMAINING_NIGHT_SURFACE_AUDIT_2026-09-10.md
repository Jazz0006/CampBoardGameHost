# UI-R5 Remaining Night Surface Audit — 2026-09-10

> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117  
> Audit basis: live production first-night / other-night interaction flow, current materializers, and current `ClocktowerNightStepUi` ownership after Ravenkeeper + Spy + Clockmaker migration.

## 1. Audit conclusion

The surviving UI-R5 implementation work is now limited to Sage plus legacy retirement.

```text
MIGRATE NEXT:
- Sage

IMPLEMENTED / ACCEPTED SQUARE TABLE:
- Clockmaker
- all other active spatial night interactions listed below

KEEP SPECIALIZED:
- first-night Minion information
- first-night Demon information
- New Demon identity reveal/confirmation

UNREACHABLE LEGACY — RETIRE AFTER SAGE:
- old ClocktowerPairManualSelectionDialog / ClocktowerPairManualCenterControls
  (retain the shared pair-manual seat helpers still used by the accepted pair square-table UI)
```

## 2. Classification principle

UI-R5 is about one coherent **Storyteller operation layer**, not mechanically forcing every player-facing reveal into a table.

Use the square table when the Storyteller must identify a current actor, select/understand player seats, or interpret information spatially. Keep a specialized reveal/confirmation when the step is fundamentally a private information handoff and table geometry would add little or make ownership less clear.

The implemented Spy slice is the reference hybrid:

```text
square-table Storyteller shell
+ existing specialized player-facing reveal retained unchanged
```

## 3. First-night inventory

| Interaction | Current Storyteller owner | Player-facing reveal owner | Classification | Recommended action |
|---|---|---|---|---|
| Minion information | generic night information card | existing evil-information player reveal | KEEP SPECIALIZED | Keep current purpose-built group information handoff. A single `current actor` seat would be misleading because multiple Minions may wake together. |
| Demon information | generic night information card | existing evil-information player reveal | KEEP SPECIALIZED | Keep dedicated Demon/minion/bluff information handoff. No target selection; table adds little semantic value. |
| Poisoner | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Fortune Teller red herring setup | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Clockmaker | dedicated read-only Clockmaker square table | existing numeric player reveal | **ACCEPTED SQUARE TABLE** | Implemented / automated green / device pending. |
| Washerwoman | pair-information square table | existing player reveal | ACCEPTED SQUARE TABLE | No migration. |
| Librarian | pair-information square table | existing player reveal | ACCEPTED SQUARE TABLE | No migration. |
| Investigator | pair-information square table | existing player reveal | ACCEPTED SQUARE TABLE | No migration. |
| Chef | dedicated Chef square table | existing player reveal | ACCEPTED SQUARE TABLE | Complete/device accepted. |
| Empath | dedicated Empath square table | existing player reveal | ACCEPTED SQUARE TABLE | Automated green/device pending. |
| Chambermaid | dedicated Chambermaid square table | existing player reveal | ACCEPTED SQUARE TABLE | No migration. |
| Fortune Teller | dedicated Fortune Teller square table | existing player reveal | ACCEPTED SQUARE TABLE | No migration. |
| Butler | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Spy | dedicated read-only Spy square-table shell | existing Grimoire reveal | ACCEPTED SQUARE TABLE | Keep existing Grimoire page/button behavior per user decision. |

## 4. Other-night inventory

| Interaction | Current Storyteller owner | Player-facing reveal owner | Classification | Recommended action |
|---|---|---|---|---|
| Poisoner | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Butler | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Empath | dedicated Empath square table | existing numeric reveal | ACCEPTED SQUARE TABLE | Automated green/device pending. |
| Chambermaid | dedicated Chambermaid square table | existing numeric reveal | ACCEPTED SQUARE TABLE | No migration. |
| Fortune Teller | dedicated Fortune Teller square table | existing Yes/No reveal | ACCEPTED SQUARE TABLE | No migration. |
| Undertaker | dedicated read-only Undertaker square table | existing role reveal | ACCEPTED SQUARE TABLE | Automated green/device pending. |
| Monk | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| New Demon identity event | generic/special identity handoff | existing role reveal / confirmation | **KEEP SPECIALIZED** | Keep as private identity confirmation before current Imp action. It is a canonical conditional event emitted when `SCARLET_WOMAN_BECAME_DEMON`; it is not dead duplication. |
| Imp / Demon kill | shared single-target square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Demon successor event | shared night-ruling square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Mayor redirect event | shared night-ruling square table | none | ACCEPTED SQUARE TABLE | No migration. |
| Sage | generic pair-result recommendation/unreliable controls | existing pair player reveal | **MIGRATE** | Dedicated/read-only square-table pair-information surface; dead Sage remains actor-highlighted. Establish typed presentation-only pair seats rather than parsing `displaySecondary`. |
| Ravenkeeper | dedicated Ravenkeeper square table | existing role reveal | ACCEPTED SQUARE TABLE | Automated green/device pending. |
| Spy | dedicated read-only Spy square-table shell | existing Grimoire reveal | ACCEPTED SQUARE TABLE | Keep existing Grimoire page/button behavior per user decision. |

## 5. Clockmaker — implemented

Clockmaker now uses a dedicated read-only square-table Storyteller surface.

Implemented contract:

- current Clockmaker actor is highlighted independently from seat state;
- healthy Clockmaker keeps the existing direct number reveal;
- drunk/poisoned manual mode preserves the existing opaque `displayOptions` candidate set;
- automatic mode uses only the already-selected `automaticDisplayOption`;
- shown number text remains presentation content and is never parsed back into semantic state;
- final reveal still uses the existing `onShowPlayerDisplay`, `showRecommendedDisplayOption`, or `resolveClocktowerLegacyUnreliablePlayerDisplay` paths;
- generic recommendation/result-first/unreliable/direct controls are suppressed while Clockmaker owns the step;
- no Host/session/rules/history/persistence behavior was changed.

Checkpoint:

```text
production implementation: ae48eecc6c0931ee608cc1402c6355cc807f8e81
verified head:             9119ec83f036432ec9b5f0f3a920690ab9719e16
CI #2106 / run 34417802131              PASS
- Android FAST unit tests                 PASS / executed
- full Android + APK                      skipped / per-role UI slice
- ASP contract                            skipped / UI-only
- Real Clingo                             skipped / UI-only
- CI gate                                 PASS
R2 #1973 / run 34417802113                PASS
```

CI #2105 initially failed only because the pre-existing Spy source-wiring test required `!usesSpySquareTable` and `!usesRavenkeeperSquareTable` to be textually adjacent. The test was made ordering-independent in `9119ec83...`; production code did not change in that fix.

Real-device acceptance remains pending.

## 6. Sage audit — next

Sage is a genuine spatial information role and should migrate, but it is more sensitive than Clockmaker.

Current materializer already owns:

- the dead Sage trigger actor;
- the actual Demon;
- one second player;
- current reliable/unreliable result options.

However current Sage `ClocktowerDisplayOption`s expose the two shown seats only as presentation text (`displaySecondary`) and do not carry a safe typed Storyteller-only pair identity. Parsing that localized/display string in UI is rejected.

Required migration boundary:

1. preserve current recommendation candidate generation and player reveal semantics;
2. add/expose typed **presentation-only subject seats** for each existing Sage candidate without converting misleading information into an epistemic proposition;
3. keep unreliable Sage options `proposition = null` so misinformation cannot become a reliable observation merely because UI needs seat identity;
4. highlight the two information seats independently from the dead Sage actor;
5. keep final player reveal/history behavior on existing paths;
6. fail closed when presentation-seat identity is absent, duplicated, out of range, or malformed.

### Sage architecture pre-flight — recorded before production edit

```text
Architecture pre-flight:
- current owner:
  ClocktowerHostScreen currently owns both prepared Sage lifecycle facts and Sage-specific
  pair recommendation/materialization; ClocktowerInformationStepBuilder owns generic
  information-step reliability/selection mechanics; ClocktowerSageSquareTableUi owns
  Sage square-table projection/rendering.
- proposed responsibility:
  Sage-specific pair candidate/recommendation projection, typed presentationSubjectSeats,
  localized Sage display metadata, and Sage NightStep materialization.
- authoritative state owner(s):
  Host/session/canonical night flow remain authoritative for current Demon identity,
  whether Sage actually died to the Demon this night, effective death-trigger ability state,
  cards/seat order, and interaction ordering. The extracted owner must not recreate these facts.
- narrow typed input/output seam:
  prepared immutable Sage trigger actor + current Demon + resolved direct pair + effective
  ability state + card roster, together with narrow recommendation/localization inputs,
  -> ClocktowerDisplayOption / ClocktowerNightStepUi. No Host/session object dependency.
- keep in current owner / extract:
  EXTRACT to a role-local ClocktowerSageStepMaterializer owner.
- reason:
  Sage candidate construction, pair presentation identity, and step metadata form one cohesive
  role-specific change unit. Keeping them in the protected >1000 LOC Host increases change
  context radius and violates the Host growth rule. This does not reopen D6 and does not create
  a one-materializer-per-role policy; other roles still require their own ownership audit.
```

Boundary constraints:

- Host keeps death-trigger/current-Demon/effective-ability authority and only passes prepared facts;
- `ClocktowerInformationStepBuilder` keeps generic reliable/unreliable/manual/automatic mechanics;
- Sage materializer must not depend on the whole Host, screen, session, or a broad context object;
- impaired Sage options remain `proposition = null`; typed seats are presentation-only;
- global `clocktowerInformationCandidateId(...)` remains unchanged; Sage-local pair identity may include typed seats where required;
- the extraction itself is behavior-preserving and does not justify unrelated role cleanup.

Existing `ClocktowerPairPlayerRevealPresentation` already establishes the correct architectural precedent: pair identity comes from typed data, never by parsing localized display text.

## 7. New Demon identity is not dead duplication

`ClocktowerCharacterInteractionRegistry` emits `other_night:event:imp:new_demon_identity` immediately before the current Imp interaction when `SCARLET_WOMAN_BECAME_DEMON` is present.

This is a canonical conditional flow event whose purpose is to privately tell the successor that they are now the Imp before they act. It should not be removed merely because there is also succession/confirmation state elsewhere in Host UI.

Classification: **KEEP SPECIALIZED** unless a later device test shows a concrete usability defect.

## 8. Legacy pair manual UI retirement

`ClocktowerPairManualSelectionDialog` is no longer called by the current `ClocktowerNightStepUi`; the accepted pair path is `ClocktowerPairInformationSquareTableDialog`.

But `ClocktowerPairManualSelectionUi.kt` still contains shared helpers used by the accepted replacement:

- `clocktowerPairManualSquareTableSeat(...)`
- `clocktowerPairManualSeatState(...)`

Therefore do **not** delete the file wholesale.

Final retirement should remove the old dialog/center-controls while retaining or relocating the shared seat helpers and keeping `ClocktowerPairManualSelectionModel` while the accepted pair surface consumes it.

## 9. Remaining execution order

```text
1. Sage typed presentation-seat seam + square-table migration
2. retire unreachable old pair-manual dialog/center controls
3. re-audit generic recommendation/result-first/unreliable/direct blocks for newly unreachable branches
4. exact diff/scope audit
5. final logical UI-R5 T4
6. cross-role real-device acceptance
7. PR #117 closeout / merge only with explicit user authorization
```

## 10. Current checkpoints

```text
latest full-T4 checkpoint:
c1ab5578a2fb64e6506d27b898df9f4bbb1388fc

Ravenkeeper per-role checkpoint:
06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 PASS / R2 #1954 PASS

Spy per-role checkpoint:
d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 PASS / R2 #1959 PASS

Clockmaker per-role verified head:
9119ec83f036432ec9b5f0f3a920690ab9719e16
CI #2106 PASS / R2 #1973 PASS
```

Real-device acceptance remains pending for Empath, Undertaker, Ravenkeeper, Spy, Clockmaker, and the previous dynamic Ravenkeeper-trigger reproduction unless explicitly reported otherwise by the user.
