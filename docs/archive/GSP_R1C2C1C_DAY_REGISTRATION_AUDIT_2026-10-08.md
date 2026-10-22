# GSP-R1C2C-1C — Public / Daytime Registration Producer Audit & Slayer Vertical

> Date: 2026-10-08 Australia/Sydney
> Stage: **1C1 — Slayer/Recluse actual day confirmation vertical** (acceptance requires exact-head Android FULL/debug APK, ASP, Real Clingo and R2); **1C2 Virgin, 1C3 Klutz remain NEXT**. GSP-R1C2C-1 and R1C2C overall remain IN PROGRESS.

## Confirmed production behavior / no fake night observations

The real source surfaces are `ClocktowerHostScreen.kt` (Day Nomination/Virgin, Day Slayer, Day Klutz) and `CampBoardGameHostApp.kt` (corresponding callbacks). None publishes a private semantic information proposition; localized `RoleAction` is not a substitute for typed global causal history.

| Day ability | Trigger actor and registration subject | Actual ruling question / player-chosen part | Verified status |
| --- | --- | --- | --- |
| Slayer | Actual living functioning Slayer shoots living healthy Recluse | Target is a **player-chosen** seat. Storyteller may explicitly register Recluse as a Demon, allowing the *mechanical* shot to kill the Recluse. Registration uses `DEMON`; Trouble Brewing's exact Demon role is Imp. | **1C1 producer**: capture before mechanical shot/death, type and validate ability-state/subject/rule-owned role, causal journal, strict Recovery. No registration is inferred from a failed shot or false/default toggle. |
| Virgin | First nomination of actual functioning Virgin by Spy nominator | Player chooses nomination. Storyteller may register Spy as a **Townsfolk**, permitting Virgin execution. This is `CHARACTER_TYPE` / Townsfolk, **not** a forced or default Washerwoman role. | **1C2 pending**. Existing UI has `chooseSpy(... defaultRole="Washerwoman")` and localized `recordSpyRegistration`, not a typed causal record. Remove accidental default role and preserve explicit false vs untouched. |
| Klutz | Dying actual Klutz chooses Spy | The Klutz's selection is a **player choice**. Storyteller may register Spy as **GOOD alignment** to avoid evil win. This is `ALIGNMENT`, not an automatically chosen good role. | **1C3 pending**. Existing UI likewise defaults to Washerwoman, creates localized event, and confirmed choice mutates phase/game outcome before a typed ruling capture. |

## 1C1 semantics

- The day registration decision context is a separate `StorytellerProviderDecisionContextV1.DayAbilityRegistration`, **not** `RegistrationResolution` anchored to an invented private observation.
- The actual Host's `onSlayerShot` confirms an explicit Recluse-as-Demon ruling **before** the one-shot ability-use/game-death mutations. The user choosing a target is not a Storyteller recommendation/decision.
- The rule-owned `TroubleBrewingRegistrationDomain` checks the exact Demon candidate; the producer requires a genuine living functioning Slayer, a living functioning Recluse, phase DAY, exact game/revisions and an explicit special role. Only a validated `RegistrationFact` enters the existing journal and Recovery sidecar; no fabricated observation or generic first legal witness.
- Current-format strict Recovery revalidates actor/subject roles, health, DAY phase, registration question, reason, alignment/type/role, exactly one typed registration and candidate against the frozen pre-action snapshot. Repeat irreversible Slayer action is not a valid independent decision; journal refuses a second capture of the same ID.
- No claim is made that `SlayerShot` itself is fully represented in the independent global mechanical action timeline: existing Death/public AliveAt producers capture confirmed death, but an unsuccessful attempt remains localized. That is a separate future action-coverage task.
- Do not carry guessed or unrecorded old daytime choices into the current causal journal; absence is **UNAVAILABLE_OR_UNRECORDED**, not proof of unresolved-by-choice.

## Follow-ups and acceptance boundaries

1. Independently verify in 1C2 that actual Virgin ability still operates when the Spy's special Townsfolk *type* is selected without inventing a specific Townsfolk role; a deliberately false special choice and untouched UI must remain distinguishable. No narrative before the confirmed mutation.
2. In 1C3 verify the Klutz's original death timing/poison and Spy's actual registration at selection, not at a later, reconstructed session; do not commit a ruling if chronology is insufficient.
3. After these bounded producers, re-audit all day/role interactions with Spy/Recluse, additional missing action facts and Day registration Recovery. Only then decide C2C-1 closure.
4. This slice does not enter GSP-R2/R3/R4 or LLM API.
