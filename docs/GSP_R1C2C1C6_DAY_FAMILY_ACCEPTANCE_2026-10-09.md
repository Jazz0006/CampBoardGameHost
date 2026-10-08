# GSP-R1C2C-1C6 — Real Trouble Brewing / No Greater Joy Day Family Acceptance

> Date: 2026-10-09 Australia/Sydney
> **BOUNDARY: C2C-1C public-day mechanical actions and real special-registration family COMPLETE / ACCEPTED (TB + NGJ only).** **C2C-1 / R1C2C / R1C remain IN PROGRESS.**
> Live executable [PR #282](https://github.com/Jazz0006/CampBoardGameHost/pull/282), squash `a5a6e170a13ba886b396400e5e692d2161892cd4`, exact accepted head `19f74820f69b3d7b2559137d8bd10ed2152e4ba1`.
> **CI #3964 full T4 GREEN** (Android full unit tests/debug APK, ASP contracts, Real Clingo); **R2 #3601 GREEN**, all checks 6/6 successful and no unresolved threads.
> [Real-script audit and coverage matrix](GSP_R1C2C1C6_REAL_DAY_COVERAGE_AUDIT_2026-10-09.md).

## Accepted correction and historical proof

The audit inspected true production script role catalogs and actual Host callbacks, not synthetic TB + Klutz + Spy. The previous **real NGJ** nomination and vote did not produce typed Global actions due to a TB-only branch despite the same public Day interface. The live callbacks now commit `ActionFact.Nomination` and `ActionFact.Vote` in both supported scripts, with NGO/NGJ `firstVirginNomination` forced false and explicit voter/ghost vote seats. True TB Virgin first-nomination, Spy-as-Townsfolk type and Slayer/Recluse registration retain their prior accepted typed causal ruling and player-shot distinction.

`ActionFact.NoExecution` positively records the confirmed day result **before** possible Mayor win/outcome or phase advance. Missing such an action from an older game is still UNKNOWN, never assumed no execution. A one-shot Slayer kill during an otherwise no-execution day remains a distinct `Death`, not an execution; no false contradiction is inferred.

The new fact has its own draft/global action, reducer, canonical fingerprint, strict persistence JSON, formal epistemic representation and public-safe enumeration/projection. Strict Recovery rejects out-of-script NGJ `SlayerShot`, forged NGJ first Virgin use or wrong TB Virgin actual role, duplicate NoExecution, same-day Execution and NoExecution, and actions after public closure. Tests validate actual NGJ `Nomination -> Vote -> NoExecution -> Death -> KlutzLearnedDeath` with old accepted NGJ recovery and public projection; negative forged day events reject.

The existing real **NGJ Klutz** `Death/Execution -> KlutzLearnedDeath -> KlutzChoice` and true TB special registration remain independent. Neither the player-selected shot, vote, nominee nor choice is an independent Storyteller discretionary decision. The Host does not synthesize an observation or first registration witness.

## Coverage boundary / next authority

The bounded **C2C-1C day mechanical/public event and real registration family is complete** for the two current supported scripts. This does NOT mean all daytime work or all historical game state is complete:

- **C2C-2 information-result producers NEXT**, including No Greater Joy **Artist question/true/shown yes/no**, Sage on demon kill, Chambermaid, Clockmaker and real TB day/night information reveal fallbacks. These are information *publication* and optional Storyteller discretionary result issues, not registration or public voting facts. Do not treat Artist as covered merely because it occurs by day.
- **C2C-3** still includes setup Drunk precommit, red herring, Demon bluffs and remaining discretionary families. Public role claims and player social-pressure narrative history remain independently partial.
- **GSP-R1C2C/R1C** remain IN PROGRESS; no LLM provider/API or GSP-R2/R3/R4 yet.
