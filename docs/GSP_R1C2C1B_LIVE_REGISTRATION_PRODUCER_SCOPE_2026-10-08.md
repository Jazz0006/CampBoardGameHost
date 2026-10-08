# GSP-R1C2C-1B — Live Result-First Registration Producer Scope & Acceptance Gate

> Date: 2026-10-08 Australia/Sydney
> Implementation PR: [#265](https://github.com/Jazz0006/CampBoardGameHost/pull/265)
> This document defines **what may be accepted**, not evidence that the full GSP-R1C2C campaign has completed.

## Production ownership and data flow

1. A Storyteller confirms a concrete **player-facing result** in `ClocktowerNightStepCardLocalized`. A manual display option is NOT yet a ruling. Neither preview, UI toggle, night-step advance, nor the first witness in an ordered list may append typed registration history.
2. The generic result-first planner matches the final typed proposition, kind and visible result against **one uniquely identified complete legal result option**, preserving all legal registration witnesses. Independent Spy/Recluse manual choices can be explicit special, explicit actual, or positively unresolved (only after a result is confirmed). An explicitly selected role must have an exact witness; an alignment-only choice never invents a role.
3. The Host preflights **all subjects together** against canonical current player roles and the Trouble Brewing registration rule domain plus **one compatible joint result witness**, before any irreversible result publication. Conflicts block the reveal rather than logging an observation that was never shown.
4. Successful publication first persists the **actual private global typed observation** and then, if it matches the confirmed semantic proposition and most recent global cursor, appends typed, interaction-local registration decisions through the existing frozen causal journal and strict Recovery sidecar. Only explicit special decisions contain a `RegistrationFact`; actual/unresolved are separate outcomes.
5. Reconfirming the identical decision is idempotent. A later **explicit new choice for the same result/interaction/subject** may correct the old decision without rewriting its frozen prefix. A missing transient UI choice after process Recovery must **never erase a previously explicit ruling**. Old sidecar absence remains UNAVAILABLE_OR_UNRECORDED.
6. Known malfunctioning Drunk/Poisoned arbitrary observations carry their actual reliability. A coincidentally legal result must **never manufacture an explanatory witness**. The live producer abstains when a typed, globally ordered observation or a uniquely validated result/witness domain is unavailable.

## Coverage matrix — be conservative

| Surface | Result-first typed registration authority |
| --- | --- |
| Trouble Brewing private night result, numeric, legal witnesses and stable interaction ID | Eligible for typed producer once actually confirmed |
| Trouble Brewing private night boolean result with exact legal candidate witnesses | Eligible once actually confirmed |
| Trouble Brewing private night role-reveal result with exact legal candidate witnesses | Eligible once actually confirmed |
| Multiple Spy/Recluse explanations for one result | Preserve all hypothetical witnesses; commit only actual manual selections or positive unresolved |
| Malfunctioning Drunk/Poisoned arbitrary result | No asserted registration witness; observation itself still canonical |
| Missing typed observation, unstable identity, unrepresented legal witnesses, legacy local history | Fail closed for typed registration, never silently reconstruct |
| Daytime Virgin/Klutz/Slayer registration rulings, Artist, other mechanical interactions | **NOT COVERED by this private result producer**; require separately validated typed decision producers |
| First-night setup Drunk, red herring, demon bluffs; other discretionary information families | **NOT made complete by this slice** |

## Executable acceptance requirements

- Shared result-first Number/YesNo/RoleReveal tests across witness ambiguity and explicit-versus-untouched choices.
- Host-owned validation tests for forged subjects, poison, selected role legality and incompatible joint witnesses.
- Real confirmation writer tests for exact global observation, multiple subjects, duplicate confirmation, explicit correction and older frozen as-of, strict Recovery round-trip and corrupted registration rejection.
- Android **FAST** plus selected full Android / debug APK and independent R2 validation on the exact accepted PR head. Full Android/solver claims are made **only after their exact jobs report GREEN**.
- No legacy heuristic, provider API, permanent role-registration field or speculative interpretation from localized RoleAction history.

## Follow-ons outside this bounded producer

Expand truthful/known malfunctioning information capture in other callback families, improve proven status for explicit `NOT_APPLICABLE`, and add typed daytime registration/ability event producers. Reassess re-confirmation from every UI path and actual Recovery if additional integration gaps are discovered. The broader GSP-R1C2C and R1C campaigns remain in progress until all audited production families and replay gates are closed.
