# GSP-R1C2C-1C6B — Drunk-shown Virgin Recovery Compatibility Fix

> Date: 2026-10-09 Australia/Sydney
> Source: **P1 post-merge review** on PR #282, thread `PRRT_kwDOTN0Fp86qi8H8`, `RecoveryRestorePlanner.kt`.
> **STATUS: COMPLETE / ACCEPTED.** Executable [PR #284](https://github.com/Jazz0006/CampBoardGameHost/pull/284), squash `fc23daf78b70eee824bc9be4d2d35dd75bbbc597`. Exact accepted head `aba672ce51ebb8b9df9c27796edba50e9babfff2`; **CI #3968 FULL T4 GREEN** (Android FULL/debug APK, ASP contracts, Real Clingo), **R2 #3603 GREEN**, zero unresolved PR #284 review threads. Original PR #282 P1 thread `PRRT_kwDOTN0Fp86qi8H8` was replied to and resolved after merge. This supersedes ONLY its erroneous “first Virgin requires actual Virgin” Recovery check; NGJ no-Virgin restriction remains.

The original 1C6 audit correctly rejected `firstVirginNomination=true` in **No Greater Joy**, which has no Virgin. However its Trouble Brewing check was too strict: production `onConfirmedNomination` marks the first **perceived Virgin** interaction using `AbilityFunctioningSemantics.interactsAs`; a genuine **Drunk shown Virgin** consumes that apparent first-nomination opportunity even though `functionsAs` is FALSE. If the Drunk later dies, reconstructing the interaction from the current alive/poison status is also wrong.

The strict Recovery validator now tests **the same perceived-role semantics** (actual Drunk + shown Virgin, or actual Virgin) against saved canonical actual/shown roles, without substituting current alive or poisoning state. It retains the actual script guard `TroubleBrewing` and rejects a Drunk shown a different role, any NGJ first-Virgin claim and synthetic mixed-script pairings.

The targeted regression uses a **legitimate 8-player TB setup**: 5 Townsfolk, 1 actual Drunk shown Virgin, Spy, Imp. A first perceived-Virgin nomination is recorded while alive, followed by the Drunk's death in a later round. Full current-format Recovery must succeed and preserve the actual/visible role distinction, while a forged change of shown role must reject.

Do not rewrite historical CI evidence for #282: it passed its exact T4, but later P1 review exposed a missed valid recovery scenario. The new exact-head CI #3968 and independent R2 #3603 have now passed, and #284 was merged. The accepted C2C-1C day family now includes this correctness repair; C2C-1/R1C2C/R1C remain IN PROGRESS. C2C-1C day family acceptance is **conditionally amended by 1C6B**; C2C-1/R1C2C/R1C remain IN PROGRESS. No policy or LLM integration.
