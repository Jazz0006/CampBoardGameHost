# GSP-R1C2C-1C6B — Drunk-shown Virgin Recovery Compatibility Fix

> Date: 2026-10-09 Australia/Sydney
> Source: **P1 post-merge review** on PR #282, thread `PRRT_kwDOTN0Fp86qi8H8`, `RecoveryRestorePlanner.kt`.
> Stage: executable **pending full T4 and independent R2**. Supersedes ONLY the erroneous “first Virgin requires actual Virgin” recovery check introduced in accepted 1C6 #282; the confirmed NGJ no-Virgin restriction remains.

The original 1C6 audit correctly rejected `firstVirginNomination=true` in **No Greater Joy**, which has no Virgin. However its Trouble Brewing check was too strict: production `onConfirmedNomination` marks the first **perceived Virgin** interaction using `AbilityFunctioningSemantics.interactsAs`; a genuine **Drunk shown Virgin** consumes that apparent first-nomination opportunity even though `functionsAs` is FALSE. If the Drunk later dies, reconstructing the interaction from the current alive/poison status is also wrong.

The strict Recovery validator now tests **the same perceived-role semantics** (actual Drunk + shown Virgin, or actual Virgin) against saved canonical actual/shown roles, without substituting current alive or poisoning state. It retains the actual script guard `TroubleBrewing` and rejects a Drunk shown a different role, any NGJ first-Virgin claim and synthetic mixed-script pairings.

The targeted regression uses a **legitimate 8-player TB setup**: 5 Townsfolk, 1 actual Drunk shown Virgin, Spy, Imp. A first perceived-Virgin nomination is recorded while alive, followed by the Drunk's death in a later round. Full current-format Recovery must succeed and preserve the actual/visible role distinction, while a forged change of shown role must reject.

Do not rewrite historical CI evidence for #282: it passed its exact T4, but its later P1 review exposed a missed valid recovery scenario. This repair must pass a fresh independent Android FULL/debug APK, ASP/Clingo T4 and R2 before final acceptance. C2C-1C day family acceptance is **conditionally amended by 1C6B**; C2C-1/R1C2C/R1C remain IN PROGRESS. No policy or LLM integration.
