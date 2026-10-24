# GSP-R1C2C-1C5B — Trouble Brewing Day Action Convergence: Accepted

> Date: 2026-10-08 Australia/Sydney
> Outcome: **COMPLETE / ACCEPTED** for this bounded TB day-action slice.
> Live code: PR [#278](https://github.com/Jazz0006/CampBoardGameHost/pull/278), squash merge `94445a586411fcfb24bb328ff639e16fd17c435b`.
> Exact production T4 checkpoint: `ae5f320677d879e270cd5baa353677afdb3b78ca`; GitHub **CI #3955 GREEN** / independent **R2 #3596 GREEN**.
> Gate: Android `:app:testFull :app:assembleDebug` PASS, ASP golden/Python PASS, Real Clingo PASS; PR checks 6/6 successful, no unresolved review threads. A preceding T4 attempt #3954 **FAILED compilation**, fixed in a subsequent commit; the successful final exact-head run supersedes it.

## Scope completed

- **Actual Host, real-script TB production:** separate a public Slayer shooting attempt from actual capability spent state and resulting hit/Death; canonical `ActionFactDraft.SlayerShot` is committed before subsequent Storyteller ruling and/or death. An ineffective shot does **not** falsely narrate Recluse registered as Imp. A legal self-target is not incorrectly prohibited.
- **Explicit Slayer/Recluse registration:** `UNTOUCHED` UI state is null, `EXPLICIT_ACTUAL` is false, and `EXPLICIT_SPECIAL` is true. Only an explicit ruling during an eligible functioning Slayer/Recluse interaction becomes a `DayAbilityRegistrationRulingProducerV1` causal entry; actual selection carries no fabricated special registration witness. An explicit ACTUAL decision can correctly accompany a *missed* shot, because the absence of the special registration is itself the adjudication. A non-functioning/poisoned shot does not claim special registration.
- **Nomination/vote actions:** the confirmed public nomination is typed (with hidden first-Virgin-consumption marker), ahead of the separate Virgin/Spy type ruling and resulting execution; confirmed voting retains nominator, nominee, voter seats and ghost voter seats. Public player-history projection preserves visible chronology but excludes functioning and Virgin-role secret flags.
- **Durable equality and strict recovery:** added shared action kinds to the GLOBAL_V1 timeline, formal Epistemic JSON, canonical history fingerprints and the strict persistence/Recovery validator. Recovery rejects invalid seats/phase, duplicated actual Slayer spent markers, repeated first-Virgin markers, votes without matching prior nominations, forged ghost voter subsets and Slayer hit without a subsequent canonical Death. It does not manufacture a historical choice from a numeric witness, death or localized text.
- **Regression evidence:** `ClocktowerTypedDayActionHistoryTest` covers typed action round-trip, damaged/missing fields and public-safe projection. `DayAbilityRegistrationRulingProducerV1Test` uses a legal **8-player TB** setup (5 Townsfolk, Recluse, Spy, Imp) for confirmed actual/special/untouched, strict causal Recovery and duplicate confirmation rejection. Prior Virgin/Spy producer and archival Klutz/Spy checks remain part of Android regression.

## Guardrails and handoff

- The *player shot*, *Storyteller registration adjudication*, and *mechanical hit/death* remain distinct facts; they do **not** imply a policy recommender, AI API, or a missing registration witness.
- Old read-only strict Klutz/Spy archive Recovery and Klutz death snapshots are intentionally retained. **NGJ Klutz learned-of-death functioning-time trigger and selected-player typed action chronology remain incomplete**, as do broader missing result/setup producers and general correction authority. Rejected synthetic TB+Klutz+Spy production coverage must not return.
- **GSP-R1C2C**, **GSP-R1C2C-1**, **GSP-R1C**, and larger GSP-R1 remain **IN PROGRESS**. The next bounded planning target is `GSP-R1C2C-1C5C` (NGJ Klutz learned-death/player-choice action and strict Recovery), **not started** by this acceptance. `C2C-2` private/result families and `C2C-3` setup decision coverage remain unresolved. No GSP-R2 or LLM API.
