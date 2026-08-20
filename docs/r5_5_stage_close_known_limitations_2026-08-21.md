# R5.5 Stage Close — Known Limitations and Field Validation

Date: 2026-08-21

## Decision

R5.5 Script & Dynamic Flow Foundation is closed as a usable milestone. The production Clocktower and Werewolf flow-order authority has been migrated to the new planners, persistence/ruleset identity work is complete, and the validated pre-close code baseline passed CI and the R2 main-thread boundary checks.

The information-recommendation/presentation migration is intentionally not expanded further before field use on 2026-08-22.

## Known limitation: Clocktower information recommendation UI

The Clocktower information recommendation and presentation UI is still transitional.

- Unified candidate-pool projection currently covers first-night information.
- Later-night information families can still use legacy/fallback presentation paths.
- In automatic mode, later-night information may therefore expose only the existing player-display action rather than the intended final recommendation UX.
- In manual mode, migration-oriented/legacy choices can appear alongside the player-display action. These are not the intended final product labels or final interaction model.

This is a presentation/migration-boundary limitation, not a second flow-order authority. It is deferred to the next recommendation-information migration stage rather than treated as an R5.5 release blocker.

## Field validation

The build will be used in a real game on 2026-08-22. That session should be treated as field validation for usability and runtime behavior.

Follow-up findings should normally be recorded for the next stage. Reopen R5.5 only if field use reveals a core rules-correctness, flow-order, persistence, or game-state defect rather than the known recommendation/presentation limitation above.

## Release baseline

The unfinished tests-first redisplay experiment that followed the green baseline is not part of this milestone. The pre-close baseline `7d06bde318d91e8ad29454b63d254cf5525cbec7` passed CI #219 and R2 #214.

After this documentation-only close commit is green, PR #2 can be merged to `main` as the R5.5 milestone. Subsequent R6 and recommendation-information work should branch from post-merge `main`.
