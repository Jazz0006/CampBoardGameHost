# UI-R5 Remaining Night Surface Audit — 2026-09-10

> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117  
> Audit basis: live production first-night / other-night interaction flow, current materializers, and current `ClocktowerNightStepUi` ownership after Ravenkeeper + Spy migration.

## 1. Audit conclusion

The surviving UI-R5 work is now small.

```text
CLEAR MIGRATE:
- Clockmaker
- Sage

KEEP SPECIALIZED:
- first-night Minion information
- first-night Demon information
- New Demon identity reveal/confirmation

UNREACHABLE LEGACY — RETIRE LATER:
- old ClocktowerPairManualSelectionDialog / ClocktowerPairManualCenterControls
  (retain the shared pair-manual seat helpers still used by the accepted pair square-table UI)
```

All other currently active supported night interactions already have an accepted or implemented square-table Storyteller owner.

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
| Clockmaker | generic numeric information controls | existing numeric player reveal | **MIGRATE** | Add thin read-only square-table actor shell with number result control. Preserve current direct/unreliable reveal semantics. |
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
| Sage | generic pair-result recommendation/unreliable controls | existing pair player reveal | **MIGRATE** | Dedicated/read-only square-table pair-information surface; dead Sage remains actor-highlighted. First establish typed Storyteller pair-seat presentation rather than parsing `displaySecondary`. |
| Ravenkeeper | dedicated Ravenkeeper square table | existing role reveal | ACCEPTED SQUARE TABLE | Automated green/device pending. |
| Spy | dedicated read-only Spy square-table shell | existing Grimoire reveal | ACCEPTED SQUARE TABLE | Keep existing Grimoire page/button behavior per user decision. |

## 5. Clockmaker audit

Clockmaker is the simplest remaining migration.

Current properties:

- first-night role information;
- current actor is already authoritative;
- result is one number;
- healthy Clockmaker has a direct number reveal;
- drunk/poisoned Clockmaker uses the existing `recommendedNumberOptions(...)` result set;
- those unreliable options may intentionally have no player-visible proposition;
- current publication/history semantics therefore must remain unchanged.

Recommended design:

```text
read-only square table
-> current Clockmaker actor highlighted
-> centre shows role/instruction
-> healthy: one existing number reveal action
-> impaired manual: existing number options, selectable in centre
-> impaired automatic: only the already-selected automatic option
-> final reveal still uses the existing onShowPlayerDisplay / resolve paths
-> no localized text is parsed back into semantic state
```

This should be a thin presentation migration and should not change Host/session/rules/history behavior.

## 6. Sage audit

Sage is a genuine spatial information role and should migrate, but it is more sensitive than Clockmaker.

Current materializer already owns:

- the dead Sage trigger actor;
- the actual Demon;
- one second player;
- current reliable/unreliable result options.

However current Sage `ClocktowerDisplayOption`s expose the two shown seats only as presentation text (`displaySecondary`) and do not carry a safe typed Storyteller-only pair identity. Parsing that localized/display string in UI is rejected.

Required migration boundary:

1. preserve current recommendation candidate generation and player reveal semantics;
2. add or expose a typed **Storyteller presentation pair** for each existing candidate without converting misleading information into a reliable epistemic proposition;
3. highlight the two candidate seats independently from the dead Sage actor;
4. keep all final player reveal/history behavior on existing paths;
5. fail closed when pair presentation identity is absent or malformed.

Do Clockmaker first; do Sage second.

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

Final retirement should:

```text
remove ClocktowerPairManualSelectionDialog
remove ClocktowerPairManualCenterControls
retain/move shared seat projection helpers
retain ClocktowerPairManualSelectionModel while the accepted pair surface still consumes it
retain existing helper/model tests that remain semantically relevant
```

## 9. Remaining execution order

```text
1. Clockmaker square-table migration
2. Sage typed pair-presentation seam + square-table migration
3. retire unreachable old pair-manual dialog/center controls
4. re-audit generic recommendation/result-first/unreliable/direct blocks for newly unreachable branches
5. exact diff/scope audit
6. final logical UI-R5 T4
7. cross-role real-device acceptance
8. PR #117 closeout / merge only with explicit user authorization
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
```

Real-device acceptance remains pending for Empath, Undertaker, Ravenkeeper, Spy, and the previous dynamic Ravenkeeper-trigger reproduction unless explicitly reported otherwise by the user.
