# R5.5 Stage Close — Known Limitations and Field Validation

Date: 2026-08-21

## Final status

R5.5 Script & Dynamic Flow Foundation is **closed and merged to `main`** as a usable milestone.

PR #2 (`R5.5 Script & Dynamic Flow Foundation milestone`) was merged successfully. Merge commit:

```text
7add8569e2484a350f6cf1512a730e9f4db469c5
```

The final documentation head before merge was:

```text
aae5b5198c605bbd00fa064b703bb237b2f21bb9
```

Validation for that head:

- CI #222 — SUCCESS
- R2 main-thread boundary #217 — SUCCESS

The validated pre-close production-code baseline was:

```text
7d06bde318d91e8ad29454b63d254cf5525cbec7
```

with CI #219 and R2 #214 both successful.

The unfinished tests-first redisplay experiment that followed that green code baseline was deliberately removed before stage close and is **not** part of the R5.5 milestone.

## What R5.5 closes

R5.5 establishes the multi-script / dynamic-flow foundation without expanding the possible-worlds engine or completing all information-recommendation UX migration.

Completed release boundaries include:

- Clocktower production night flow order is planner-backed.
- Werewolf production judge flow order is planner-backed.
- Legacy independent flow-order authority has been removed.
- Trouble Brewing and No Greater Joy share the same script/catalog/planner seam.
- Werewolf uses typed board/role registry + planner flow.
- persistence/ruleset identity migration is complete for the R5.5 scope.
- R5.5 software regression and CI/R2 gates are green.

## Known limitation: Clocktower information recommendation UI

The Clocktower information recommendation and presentation UI is still transitional.

- Unified candidate-pool projection currently covers first-night information.
- Later-night information families can still use legacy/fallback presentation paths.
- In automatic mode, later-night information may therefore expose only the existing player-display action rather than the intended final recommendation UX.
- In manual mode, migration-oriented/legacy choices can appear alongside the player-display action. These are not the intended final product labels or final interaction model.

This is a presentation / information-migration boundary limitation. It is **not** a second flow-order authority and is intentionally deferred rather than treated as an R5.5 blocker.

Do not reopen R5.5 merely to hide these labels or cosmetically unify the buttons. Fix this as part of the later recommendation-information migration so there remains one semantic information-decision model rather than two UI-specific paths.

## Field validation — 2026-08-22 real game

The app will be used in a real game on 2026-08-22. Treat that session as field validation for usability and runtime behavior.

Highest-value observations are:

1. actual Clocktower night interaction order;
2. correct appearance/skipping of conditional interactions;
3. Scarlet Woman / Imp succession ordering and lifecycle;
4. Mayor / Ravenkeeper / Undertaker / Sage conditional flow;
5. No Greater Joy first-night → day → later-night continuity;
6. navigation/back/step-index continuity after planner cutover;
7. persistence/restart/restore behavior if exercised;
8. any recommendation/presentation UX friction beyond the already-known limitation above.

Reopen the R5.5 correctness boundary only if field use reveals a core rules-correctness, flow-order, persistence, or game-state defect. Ordinary recommendation/presentation findings belong to the next development stage.

## Next development boundary

All new development should start from **post-merge `main` on a new branch**.

The next session must first read:

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
2. this document
3. the relevant next-stage design document before changing production code

R5.5 should now be treated as historical/released foundation, not an active branch to continue extending.
