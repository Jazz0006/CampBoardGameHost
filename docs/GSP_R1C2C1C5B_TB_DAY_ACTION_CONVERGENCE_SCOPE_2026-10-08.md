# GSP-R1C2C-1C5B — Real TB Day Action/Registration Convergence

> Date: 2026-10-08 Australia/Sydney
> Implementation branch: `gsp-r1c2c1c5b-tb-day-action-history`
> Status: **COMPLETE / ACCEPTED** — PR #278 squash `94445a586411fcfb24bb328ff639e16fd17c435b`; exact HEAD `ae5f320677d879e270cd5baa353677afdb3b78ca`, CI #3955 T4 / R2 #3596 GREEN.
> Authority: `AGENTS.md`; `docs/TESTING_STRATEGY.md`; `docs/GSP_R1C2C1C4_LIVE_DAY_REGISTRATION_REACHABILITY_AUDIT_2026-10-08.md`.

## Production design / ownership

- **Player/mechanical action authority:** `ClocktowerGameSession` binds immutable `ActionFactDraft` to GLOBAL_V1 with unique globally ordered timeline points. `SlayerShot` (claimant, target, consumed-use, hit), `Nomination` (nominator, nominee, first-Virgin-use marker), and `Vote` (explicit voter seats and ghost voter seats) are mechanical facts, **not Storyteller decisions or private observations**. Localized Event text is never a Recovery oracle. Public player projection carries only public portions, not secret functioning/first-Virgin booleans.
- **Storyteller registration authority:** `DayAbilityRegistrationRulingProducerV1` captures eligible, functioning Slayer/Recluse rulings only when the operator explicitly selected `ACTUAL` or `SPECIAL:Imp`. The UI's nullable choice means `null = UNTOUCHED`, `false = EXPLICIT_ACTUAL`, `true = EXPLICIT_SPECIAL`. An untouched choice or nonfunctioning shot never generates a ruling. An **explicit ACTUAL** ruling by a functioning Slayer targeting a healthy Recluse can validly accompany a **miss**, without special-registration evidence. The separate Virgin/Spy CHARACTER_TYPE ruling retains explicit-only capture and multiple-witness semantics.
- **Rules/result authority:** a Slayer attempt may miss or not consume the real ability. The public action always precedes a resulting special registration and Death; a malfunctioning shot must not narrate a confirmed `Imp` registration. A confirmed nomination is globally ordered ahead of a Virgin ruling/execution; voting is a separate public action and is not a registration.
- **Strict Recovery and replay:** versioned action kinds persist through `ClocktowerSemanticHistoryPersistence`, formal epistemic JSON and the shared chronology. Restore validates seat, day phase, unique first-Virgin and one-shot spent markers, paired nomination/votes and shot-to-Death chronology where a hit occurred. Older archives without these new kinds remain readable; absence stays absence, never synthesized. The retired cross-script Klutz/Spy writer remains unreachable and its old strict read-only validator is unchanged.

## Targeted acceptance matrix

1. Real Trouble Brewing role IDs for Slayer/Recluse, Virgin/Spy and no synthetic TB+Klutz+Spy fixtures. Test `EXPLICIT_ACTUAL` vs special vs untouched without fake registration.
2. Successful and ineffective shots: capture public attempt/spent metadata; miss does not claim Imp registration or cause Death; replay shot before ruling/Death.
3. First Virgin nomination and follow-on vote: typed action and voter/ghost snapshot; no duplicated first-use or voting without a nomination; no implicit Storyteller ruling.
4. Current-format JSON/strict Recovery round-trip, malformed/tampered fields and old-history compatibility; public history projection hides secret ability-state flags.
5. Exact-head **CI T4 Android FULL/debug APK + ASP + Real Clingo** and **independent R2**. Never accept code based solely on local source checks.

## Exact-head T4 escalation

The accepted code used a dedicated final `[full-ci]` commit after the executable and valid eight-player TB fixture checkpoints. The ordinary synchronize classifier may run only FAST on a later test-only commit; such a run is **not** full T4 evidence. The final `[full-ci]` HEAD must independently complete Android `:app:testFull :app:assembleDebug`, ASP golden/Python and Real Clingo cross-validation, plus R2. All subsequent source changes invalidate this checkpoint.

## Deliberately open / next gates

- Do not infer retrospective Storyteller rulings from player actions, numeric witnesses, current UI choice or death snapshots.
- No general retroactive editing of an irreversible shot/nomination after its already committed result: a future correction flow requires its own causal authorization rather than rewriting old facts.
- No Greater Joy Klutz **learned-of-death functioning** and player-choice typed chronology remain an independent later mechanical producer.
- `GSP-R1C2C-1C`, `GSP-R1C2C` and `GSP-R1C` remain **IN PROGRESS**; `C2C-2` private/Artist information and `C2C-3` setup decisions remain open; no GSP-R2 or LLM API.
