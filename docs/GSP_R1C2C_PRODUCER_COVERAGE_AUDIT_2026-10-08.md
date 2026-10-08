# GSP-R1C2C — Live Decision-Producer Coverage Audit (2026-10-08)

> Repository: `Jazz0006/CampBoardGameHost`  
> Inspected baseline: `main@e3f76f076e6568e30521fb24bb4ee50fda2207a8`, local/remote equal, clean.  
> Status: **LIVE CODE PRODUCER AUDIT; R1C2C PRODUCTION COVERAGE INCOMPLETE**. Audit is not an assertion of full historical capture, T4, or merge acceptance.

## 1. Classification and ownership

- **1 / COMPLETE** — real typed decision producer: exact predecision frozen GLOBAL_V1 prefix, Host confirmation, causal commit/correction, and current-format Recovery with strict restore.
- **2 / CONFIRMATION WITHOUT CAUSAL CAPTURE** — a real confirmed Host-owned decision exists, but no typed causal producer.
- **3 / TYPED OBSERVATION OR ACTION ONLY** — semantic outcome/fact exists, but its predecision context and choice chronology are absent.
- **4 / UI OR LOCALIZED RECORD ONLY** — a display, UI draft or `ClocktowerEventType.RoleAction` is not accepted semantic history.
- **5 / NOT A STORYTELLER DECISION** — rules-calculated result or player-chosen action; it must not be turned into an artificial Storyteller decision. There may be a separate discretionary ruling on a related interaction.

A player-selected target (Poisoner, Monk, Imp, Butler, voters etc.) is not a Storyteller's choice merely because the Host confirms it. Preserve its typed action fact; separately capture only actual Storyteller discretion (e.g. Mayor redirect, registration, arbitrary information shown when permitted). Mechanical action, observed information and discretionary adjudication must remain distinct.

## 2. Real production entry points and gaps

| Family / live entry | Evidence / owner | Classification and next gate |
| --- | --- | --- |
| Mayor night redirect | `CampBoardGameHostApp.kt:onConfirmMayorRedirectTarget` => `PendingMayorRedirectDecision.confirm` => `StorytellerCausalDecisionJournalV1.captureBeforeDecision/commit/correct`; `ClocktowerCausalJournalPersistence` and `RecoveryRestorePlanner` | **1** — sole complete typed causal production vertical, PR #258; no extrapolation to other families. |
| Setup Drunk selection | `CampBoardGameHostApp.kt:Screen.ClocktowerDrunkSelection/onConfirm` => `commitAndStartTroubleBrewingGame`; prepared setup snapshot | **2** — rules-legal typed setup candidate and explicit confirmation, but game Session is not yet committed at selection time. Needs a dedicated setup-precommit frozen context adapter, not a fake later cutoff. |
| First-night Washerwoman/Librarian/Investigator | `ClocktowerHostScreen.kt:recordReliablePrivateInformation`; `onCommitConfirmedInformationDecision` -> App `commitConfirmedInformationDecision`; fallback `displayProposition` | **3** when Foundation confirmed typed observation exists; **4** for localized-only fallback. Must enumerate actual branches and capture before publishing observation, preserving one displayed result vs multiple legal registration witnesses. |
| Chef/Empath/Fortune Teller information | Same shared reveal handoff; typed numeric/boolean drafts where Foundation confirms; fallback typed propositions or display history otherwise | **3/4** — current observation is not a prior decision with an exact pre-observation capture. Drunk/Poisoned arbitrary results need no invented registration explanation. |
| OtherNight Undertaker/Ravenkeeper/Chambermaid and role display | `ClocktowerHostScreen.kt:recordReliablePrivateInformation`, `onShowPlayerDisplay`, role-specific fallback; `ClocktowerPlayerDisplayResolution.kt` | **3/4**, per path. Never treat `displayKind`/localized UI text as a typed confirmed decision. |
| Spy/Recluse registration on night/first night | `ClocktowerHostScreen.kt:recordSpyRegistration/recordRecluseRegistration`, `ClocktowerRegistrationInteractionState.kt` explicit manual state, onNext and onApplyRecommendedDisplayOption | **4** — only `RoleAction` UI event; no typed interaction-scoped causal ruling, unresolved marker or Recovery. PR #254 removed first-witness automatic selection, not this gap. |
| Spy/Recluse for Virgin/Klutz/Slayer | `ClocktowerHostScreen.kt` day controls and App `onSlayerShot` local event | **4** for any genuinely explicit ruling; **5** for the underlying ability/player action. Same shared registration contract must apply to day interactions. |
| Red herring | `CampBoardGameHostApp.kt:onSelectRedHerring`; `ClocktowerHostScreen.kt` NightAction.RedHerring; persisted current `redHerring` | **2/4** — current selected value in state/Recovery, but no separate durable exact decision event or preselection chronology; distinguish edit from confirmation. |
| Demon bluffs | `CampBoardGameHostApp.kt:onCommitDemonBluffs`, Recovery `demonBluffRoleNames` | **2** — persisted selected set, no typed causal decision chronology; identify precise reveal/commit timing before capture. |
| Demon successor / character replacement | App `onConfirmDemonSuccessorTarget`, `NightCheckpointHostTransaction`, RoleChange action | **2/3** for any actual Storyteller branch; mechanical role change is not a second independent recommendation decision. |
| Mayor attack target vs redirection and death | App `onConfirmDemonAttack` Attack fact + mayor confirmation + Dawn Death | **5** for demon's chosen attack; **1** ONLY for Mayor Storyteller redirect; later death is a mechanical outcome. Do not collapse three events. |
| Poisoner target; Monk protection; Butler master | App `onConfirmPoisonTarget` Poison; `onConfirmMonkProtectedTarget` Protect; `onSelectButlerMaster` | **5** player choices, not a Storyteller decision. Action facts have partial typed coverage, do not double-log. |
| Day executions, nominations/votes, deaths, ghost vote | App day handlers and ActionFact Execution/Death, public AliveAt observations | **5** for rules result and player choice; public narrative/claims remain separate unverified semantic producer coverage. |
| Artist shown yes/no, special day judgments | App `onConfirmArtistQuestion` stores localized truthful/shown detail, GameState revision | **4** for chosen displayed answer (potentially real discretion); actual question/player action is not Storyteller selection. Needs typed proposition/provenance at publication. |
| Slayer shot / Klutz choice / Virgin trigger | App `onSlayerShot`, `onConfirmKlutzChoice`, Host Virgin presentation | **5** for player ability action / rule consequence; **4** when a discrete registration adjudication is explicitly chosen. |
| Player claims/pressure/narrative | Session has player-context overrides; UI history is separate | **4 / PARTIAL** — current values and Recovery do not prove historical public declaration chronology. Out of immediate R1C2C producer batch unless capture is already owned by Host. |

## 3. Generic registration ambiguity invariant

Result-first `ClocktowerDisplayOption.legalRegistrationWitnesses` is a *derived set*. Neither the option nor the first entry supplies an explicit ruling. `ClocktowerRegistrationInteractionState.spyHasExplicitChoice/recluseHasExplicitChoice` is UI-local evidence of deliberate input; `mark*Recorded` writes **only localized** text. A future canonical producer must record the ability-interaction identity, actual subject seat, querying ability, deliberate ruling value and provenance, frozen **pre-ruling** revisions/prefix, and correction chain. One result might have multiple legal witness assignments (Chef, Empath, Fortune Teller and role information): `UNRESOLVED_NOT_REQUIRED` is distinct from `UNAVAILABLE_OR_UNRECORDED`. Do not write a special rule for Empath/Spy/Recluse. Drunk/Poisoned information must not require a witness.

The existing `StorytellerCausalDecisionJournalV1.commit` and `ClocktowerCausalJournalPersistence.encode` deliberately reject nonempty `registrations` until a verified producer exists. **Removing these guards without validating a genuine explicit producer is forbidden.** The `RegistrationFact` type already carries an interaction scope but is not itself evidence that the Host committed a ruling.

## 4. Implementation batches (R1C2C only)

- **C2C-0 / first executable slice:** introduce a *generic, typed and explicit-only* registration-resolution authority contract at the Host interaction boundary, with tests proving absence vs manual selection and ambiguity across numeric/boolean/role results. Preserve PR #254 negative invariant. This slice by itself does **not** qualify as durable producer or complete Recovery.
- **C2C-1 / capture + Recovery:** integrate the actual Host confirmation callback to a frozen, typed, interaction-local explicit registration ruling and positive known-unresolved result record. Extend strict causal journal codec, restore planner, correction/reconfirmation handling and provider as-of projection without promoting derived witnesses; test fresh/restore, old-format UNKNOWN, malformed ref/causal linkage rejection, same-revision ordering. This is the first canonical registration producer acceptance gate.
- **C2C-2 / information family:** generalize confirmed typed information decision capture in the shared `onCommitConfirmedInformationDecision` pre-observation seam, audit and bridge fallback role paths; guarantee selected visible result + optional explicit ruling are distinct.
- **C2C-3 / setup and remaining discretionary families:** Drunk precommit, red herring, demon bluffs, discretionary night/day changes and publication-specific typed facts; retain player-selected action fact semantics. Do not fabricate decision history from current state.

Every executable batch requires exact remote diff/fanout review, focused typed regressions, Android FAST and affected CI/R2; full Android/debug APK/ASP/Real Clingo for the affected T4 checkpoint. No recommendation provider API, heuristic scoring or LLM connection.

## 5. Current evidence boundary

This inventory is from source reads at the exact clean main noted above. It is not a proof that every `ClocktowerNightStep` or every day branch emits a proposition; missing paths remain PARTIAL/UNKNOWN. PR #230 and #232 remain historical, superseded Draft PRs per the prior R1A audit; do not merge them without re-audit. No new CI result is claimed by this document.