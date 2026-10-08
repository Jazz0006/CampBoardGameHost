# GSP-R1C2C-1C6 — Real TB / NGJ Public-Day Coverage and Strict Recovery Audit

> Date: 2026-10-09 Australia/Sydney
> Baseline: live/local `main@800a16c7a81d99fe803f5698e82c2d28221e0030` clean, C2C-1C5C accepted (PR #280, CI #3960 T4, R2 #3599).
> Stage: **EXECUTABLE AUDIT + TWO COVERAGE REPAIRS; ACCEPTANCE PENDING exact-head FULL T4 and independent R2**.
> **Do not infer** C2C-1C closure from a green synthetic roster; use real TB and NGJ script memberships.

## Findings grounded in actual Host callbacks

The real `ClocktowerPresentationRoleCatalog.kt` provides TB Spy/Recluse/Virgin/Slayer and NGJ Klutz/Artist; no real supported script has both Klutz and Spy. Previous unreachable Klutz/Spy writer was retired in 1C5A.

| Day / non-private family | Live owner, state/history boundary | Audit status |
| --- | --- | --- |
| TB Slayer / Recluse | `ClocktowerHostScreen.onSlayerShot` -> `CampBoardGameHostApp.onSlayerShot`, `ActionFact.SlayerShot` before optional `DayAbilityRegistrationRulingProducerV1.confirmSlayer` and possible `ActionFact.Death` | **COVERED / preexisting accepted 1C1 + 1C5B**. Player target ≠ Storyteller's explicit demon-registration ruling. Untouched vs explicit actual vs special distinguished, nonfunctioning shot does not claim special adjudication. Legacy absent shot stays UNKNOWN. |
| TB Virgin / Spy | confirmed `Nomination` before `confirmVirginSpy`/Virgin execution; actual `CHARACTER_TYPE=TOWNSFOLK` registration, no invented role, followed by `Execution` if triggered | **COVERED / preexisting accepted 1C2 + 1C5B**. Candidate witnesses never auto-pick first. Only actual TB Virgin can carry `firstVirginNomination` in strict Recovery. |
| NGJ Klutz | death/Execution event -> explicit Host public announcement `KlutzLearnedDeath` -> public player `KlutzChoice`; script-validated mechanical win/loss | **COVERED / preexisting accepted 1C5A + 1C5C**, not Klutz/Spy registration. Old unproven death stays UNKNOWN. Current NGJ no Poisoner; this is not a future mixed-script poison policy. |
| Public nomination and vote in **both** TB and NGJ | actual shared `CampBoardGameHostApp.onConfirmedNomination/onConfirmedVote` -> typed `ActionFact.Nomination` and `Vote`, with voter seats / ghost voter seats | **GAP FOUND / FIXED IN 1C6**: before this audit both callbacks gated on TB only, so actual NGJ public nominations/votes were omitted from canonical history despite UI gameplay. Both scripts now commit genuine GLOBAL_V1 actions; `firstVirginNomination=false` for NGJ. |
| Explicit end-of-day **no execution** in both scripts | `onConfirmDay` with no selected execution, before Mayor check/outcome and phase advance | **GAP FOUND / FIXED IN 1C6**: prior `ClocktowerEventType.Execution("No execution")` with no player seat produced only localized text; no action meant ambiguous UNKNOWN. Now `ActionFact.NoExecution` is a distinct confirmed public action, persisted with explicit day time and stable canonical identity. No-execution is NOT a Storyteller recommendation or inferred from missing Death. |
| Actual day execution/death, Saint, Mayor | actual `Execution`/`Death` and public `AliveAt` where appropriate; Saint's executed loss / Mayor's no-execution win are mechanically rule-owned | **MECHANICAL FACT CAPTURE COVERED BOUNDEDLY**. NoExecution records Mayor's relevant public nonexecution premise even if game ends without a Night transition. Winner/outcome state is not a new discretionary decision. |
| NGJ Artist player question and shown truthful/false answer | `onConfirmArtistQuestion` currently localized `RoleAction`, current `artistUsed` | **OUT-OF-FAMILY REMAINS INCOMPLETE**: Artist *question/answer* is information publication and belongs under **C2C-2** typed result/provenance, NOT a fabricated day registration or a player-independent mechanical ruling. Artist's player question and explicit shown answer must not be inferred from UI text. |
| NGJ Sage, Clockmaker, Chambermaid; TB private night information | reveal and/or triggered private ability information paths | **C2C-2 information-producer audit**, not public day registration. |
| Drunk precommit, red herring, bluffs, evil information | setup/first night choices | **C2C-3 setup/remaining discretionary coverage**, not C2C-1C. |
| Public claims, narrative and social pressure | current player context/local display only | **Historical public narrative capture not claimed** by this mechanical family. No imaginary observation or result inferred from chat notes. |

## Independent correction evidence in this branch

- Shared NGJ nominator/voter callback now creates the **same** global explicit public actions as TB. It does not claim Virgin can appear in NGJ; a real NGJ production roster forces the hidden Virgin-first marker false.
- `NoExecution` extends the existing action draft, reducer, semantic persistent JSON, formal epistemic JSON, canonical digest and safe player-historical world replay. It is committed **before** local no-execution text and any Mayor win/phase effects.
- Strict Recovery refuses `SlayerShot` outside TB; requires a first-Virgin mark to be actual TB Virgin, not only a non-duplicated Boolean; refuses NoExecution except in day, twice in a round, paired with a real same-day Execution, or followed by additional same-day nominations/votes/shots after day is closed. Old absent NoExecution remains UNKNOWN.
- Tests exercise a **real NGJ** sequence (`Nomination -> Vote -> NoExecution -> Death -> KlutzLearnedDeath`), current-format round-trip and restored public safe chronology, plus forged Virgin-first, unmatched vote, impossible NGJ Slayer shot, duplicate/no-execution vs same-day execution, and preexisting TB typed event tests. Backwards compatibility does not manufacture historic no-execution for older snapshots.

## Closure decision

**1C6 acceptance is CONDITIONAL on exact-head FULL Android JVM + debug APK, ASP golden/Oracle and Real Clingo, R2, clean merge and docs sync.** After acceptance the *bounded C2C-1C actual-script public/day mechanical/registration family* can be marked **COMPLETE / ACCEPTED**, while the larger **C2C-1 / R1C2C / R1C remain IN PROGRESS** because Artist/other information result families (C2C-2), setup/discretion (C2C-3) and cross-game/narrative areas remain unclosed. If any relevant real day mechanical path is found outside this matrix, re-open 1C rather than falsely asserting full coverage.

**Next phase: GSP-R1C2C-2 — actual confirmed information result producer inventory/coverage, with NGJ Artist and Sage prioritized**, preserving exact observed results, multiple legal witness ambiguity, and strict Recovery. Do not enter GSP-R2/R3/R4 or production LLM API.
