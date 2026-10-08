# GSP-R1C2C-1C4 — Live Day / Non-Private Registration Reachability & Trigger Audit

> Date: 2026-10-08 Australia/Sydney
> Baseline: live/local `main@f986f2bdc2e79928961fb2e49a9710d1f55c1582` clean, no open PR.
> Result: **AUDIT COMPLETE / COVERAGE NOT ACCEPTED**. The exact tests and GREEN CI for the earlier 1C1–1C3B code are genuine, but some historical *production reachability claims were invalid*. **Do not close C2C-1C, C2C-1, R1C2C or R1C.**
> This is a docs-only ownership/coverage audit, not a behavioral patch or a new full Android T4 checkpoint.

## 1. Authoritative script roster: the first acceptance correction

Sources: `ClocktowerPresentationRoleCatalog.kt:clocktowerRolesForScript` and `noGreaterJoyRoleNames`, `NoGreaterJoyOfficialCharacterMetadata.kt`, `StorytellerProviderContractV1.kt`.

| Actually supported script | Spy | Recluse | Virgin | Slayer | Klutz | Artist | Result |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `trouble_brewing` | YES | YES | YES | YES | **NO** | **NO** | Virgin/Spy and Slayer/Recluse are reachable in real TB. Klutz/Spy is not. |
| `no_greater_joy` | **NO** | **NO** | **NO** | **NO** | YES | YES | Klutz acts mechanically, but **no Spy exists to receive a special-good registration ruling**. Artist needs an information-result producer, not imaginary Spy registration. |

**Decisive false-positive:** C2C-1C3A/#271 and C2C-1C3B/#273 synthetic JVM fixtures injected **Klutz and Spy into `trouble_brewing`**. In real production `KlutzSpyDayRegistrationProducerV1.confirm` requires `snapshot.script == trouble_brewing`, and its `StorytellerProviderGameStateV1.TroubleBrewing` payload is TB-only; app `onConfirmKlutzChoice` only attempts it when `currentClocktowerScript == TroubleBrewing`. In actual TB Klutz is absent; in No Greater Joy the Spy and TB-only provider boundary are absent. The `KlutzSpyDayRegistrationProducerV1` **cannot produce a real supported game registration**. GREEN means that constructed fixture compiled and passed; it is NOT evidence of live production cutover.

The C2C-1C3B **session-owned predeath evidence embedded in Death/Execution + persistence and strict codec** remains a potentially useful canonical mechanical/history infrastructure, including for the real No Greater Joy Klutz. Do not discard it without dependency review; do not claim it is an independently accepted *Klutz/Spy production use case*. Earlier acceptance docs remain historical records but are **superseded on the reachability/coverage claim by this audit**.

## 2. Timing and behavior correctness, distinct from history capture

**Official Klutz ability text**: “When you learn that you died, publicly choose 1 alive player: if they are evil, your team loses.” Role timing is not mechanically identical to dying. The source says that at night the storyteller informs the Klutz the next morning; a public choice follows. Official: https://wiki.bloodontheclocktower.com/Klutz . Community timing clarification: https://www.reddit.com/r/BloodOnTheClocktower/comments/ut6hf1/ .

`KlutzDeathTriggerEvidenceV1` stores pre-**death** `wasPoisoned`, not a separately evidenced **learned-of-death / choose** ability status. It is therefore incorrect to treat `functioningAtDeath` as a universal test of Klutz ability functioning at choice. The Poisoner (when scripts permit one) can lose ability, die, or poisoning can expire between the two instants; a player can be impaired at death and healthy when they learn, or vice versa. `ClocktowerGameSessionAliveBoundaries.kt` clears `poisoned` on death, so current `PlayerState.poisoned == false` does not solve it. **Never silently upgrade death evidence to learn-time ability proof.**

The *current supported* No Greater Joy list contains **no Poisoner**. This reduces immediate live poisoning exposure but is not permission to hard-code the rule for future mixed scripts. In real `CampBoardGameHostApp.kt:onConfirmKlutzChoice` the mechanical evil-win branch uses `isClocktowerEvil(chosenCard)` and only a Spy-registration exception. It does **not** consult an independently confirmed Klutz-at-learn ability state. Player target, ability trigger, special registration and mechanical win/loss are four distinct semantic events.

## 3. Reachable TB day registrations: current evidence and open gaps

| Actual producer | What works | What is still missing / unsafe to assert |
| --- | --- | --- |
| Virgin first nomination + Spy Townsfolk-type | Real TB roles, explicit special/actual `CHARACTER_TYPE` before execution, strict typed journal and Recovery (#269) | Nomination itself is localized; no distinct globally typed `Nomination` action/first-use anchor. `ClocktowerHostScreen.kt` first-nomination guard + `CampBoardGameHostApp.kt` are separate state owners. Audit recovery/re-entry, stale manual selection and repeated click under later revisions before whole-family closure |
| Slayer shot + Recluse Demon registration | Real TB roles, special-as-Imp producer, pre-shot capture, strict typed journal and Recovery (#267) | UI uses a Boolean `slayerRecluseRegistersDemon` default false with no explicit-false vs untouched distinction; unlike Virgin/Klutz it has no typed `EXPLICIT_ACTUAL` ruling. `onSlayerShot` also logs a localized “Recluse registered as Imp” event when the UI toggle is true even if the shooter is malfunctioning and the ability does not apply. Failed shots / spent ability have no globally typed `SlayerShot` action, only localized history. Reconfirm/Correction and arbitrary Replay of missed shots still lack sufficient causal data |

Neither `RoleAction` text nor `ClocktowerEventType.Nomination`/Vote history is an automatically trustworthy typed Storyteller decision. No information-observation surrogate may be invented for these daytime mechanical actions. The registration engine must continue accepting **multiple legal witnesses** for shown numeric/boolean/role results without selecting the first witness.

## 4. Other daytime/non-private families (no false registration)

- **No Greater Joy Artist**: `CampBoardGameHostApp.kt:onConfirmArtistQuestion` retains truthful and shown yes/no in a **localized** event, without a versioned typed private-to-Artist semantic proposition, pre-confirmation ruling or causal Recovery; absence of Spy means do not invent a registration to solve it. Place under **C2C-2 information family** once a No Greater Joy provider state envelope exists.
- **No Greater Joy Klutz**: the choice is the player's public action. Current `KlutzSpyDayRegistrationProducerV1` is unreachable; the canonical Klutz death proof is **not** a complete choice-action fact, a learned-death trigger or a reliable win/loss ability resolver. Treat these as mechanical/history producer and rules work, **not** a fictitious Storyteller discretion event.
- **TB other public events**: actual executions/deaths have partial `ActionFact.Execution/Death` and `AliveAt`, but nominations, voting, Slayer misses/attempts and one-shot ability-spend changes are not fully typed. Recovering only the current results/UI fields does not reconstruct an exact historical decision prefix.

## 5. Required execution route after this audit

### C2C-1C5A — reachable-path contracts / eliminate impossible producer

1. Write tests deriving actual role/script membership from the production registry: **TB has Spy/Recluse/Virgin/Slayer but no Klutz; NGJ has Klutz/Artist but no Spy**. Tests must reject cross-script synthetic `Klutz + Spy` as production-coverage evidence.
2. Remove or explicitly demote the unreachable `KlutzSpyDayRegistrationProducerV1` live wiring, the unreachable Klutz Spy-alignment UI/code and its TB-only synthetic “production success” contract, **without deleting useful generic death-trigger facts**. Preserve current-archive compatibility explicitly; do not silently reinterpret old sidecars.
3. Verify actual No Greater Joy Klutz **player choice + mechanical win/loss + Recovery**, without inventing Storyteller registrations. Separate dead-player poisoning at death from ability at the `learn` trigger. For current NGJ roster, lack of Poisoner may simplify the live result; for cross-script future, require a genuine learn-time functioning state or return UNKNOWN.
4. Ensure older accepted C2C-1C3A/3B docs reference this audit as *superseding their assertion of reachable Klutz/Spy production coverage*; do not alter historical exact CI/test facts.

### C2C-1C5B — real TB day ruling/action convergence

5. Close Slayer manual `EXPLICIT_ACTUAL` vs untouched status; remove contradictory localized registration on an ineffective shot, and add a distinct canonical player-shot/ability-spend mechanical action where actually needed. Preserve legal rule adjudication as independent of shot target.
6. Re-audit Virgin first nomination and repetition after Recovery. Add a typed mechanical nomination/vote sequence only under its actual action owner, and prove it never duplicates or backdates an explicit registration.
7. Use exact live TB/NGJ integrated fixtures with real catalogs, not invented script/role combinations; add negative replay/tamper and causal-order tests.

### R1C2C later gates

8. **C2C-2** covers confirmed private/Artist information result families and publication-specific Host ownership.
9. **C2C-3** covers Drunk precommit, Red Herring, demon bluffs and remaining actual Storyteller choices.
10. Do **not** start GSP-R2/R3/R4 or live LLM API before real producer coverage, history as-of and current-format Recovery are established.

## Audit acceptance

For this documentation-only stage: update the current roadmap and handoff, get independent docs CI/R2, merge and synchronize local `main` clean. Verdict is **C2C-1C4 AUDIT COMPLETE / COVERAGE GATE FAILED** and **C2C-1C5A NEXT**, not “all registrations complete.” The audit corrects the production-coverage interpretation; it does **not** retroactively assert previous CI gates failed.
