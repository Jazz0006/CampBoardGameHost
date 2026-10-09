# CampBoardGameHost — Branch Retention and Pruning Audit (2026-10-09)

> **Snapshot, not a permanent authority.** Live GitHub / Mini MCP ref state must be queried immediately before any deletion. Baseline GitHub main `2886dcca876ef81a164ef700d748408223c029d3`, local `main` clean/equal; open PR: **0**. This audit does **not** perform deletion.

## Summary

| Observed | Count |
| --- | ---: |
| Local branches | 67 |
| Remote branches, including `main` | 77 |
| Remote non-main branch with matching merged PR | 62 |
| Remote branch with matching closed-unmerged PR | 9 |
| Remote branch with no matched PR in queried 288 PRs | 5 |
| Local-only branches (not in remote) | 45 |

### Hard retention rules

- **Never delete `main`**.
- **Keep `gsp-2b3b-player-context-edit-surface` (#232)**: closed / unmerged; source of future GSP-R2 player editor/profile reference not adopted by main. Inspect afresh before R2; do not merge old branch.
- Keep any unmerged branch with unique code/evidence until specific preservation/migration has been verified. The API0 and MEM0 implementation branches are merged, but keep their corresponding facts in active roadmap rather than using branch existence as progress state.
- Do not delete a branch with an open PR, changing remote HEAD, uncommitted local work or a still-running external worker. **Squash merge is not ancestry equality**: resolve branch to an exact PR, review its accepted content and independently check whether unique changes remain.
- Bulk deletion is only hygiene. If the connected GitHub/Mini MCP tools lack an authorized branch-delete operation, do not imitate deletion via force-ref mutation; use repository branch management after exact review, without blocking MEM0.

## Closed / unmerged remote refs — preserve or individually decide

| Branch | PR | Recommendation |
| --- | --- | --- |
| `c5e-librarian-production-cutover` | [#207](https://github.com/Jazz0006/CampBoardGameHost/pull/207) | REVIEW then remove — superseded C5-E attempts; accepted production history documented |
| `c5e-librarian-production-cutover-r2` | [#210](https://github.com/Jazz0006/CampBoardGameHost/pull/210) | REVIEW then remove — superseded C5-E attempts; accepted production history documented |
| `codex/d6-ownership-plan` | [#111](https://github.com/Jazz0006/CampBoardGameHost/pull/111) | REVIEW — superseded architecture plan |
| `codex/execution-restore-crash-repro` | [#109](https://github.com/Jazz0006/CampBoardGameHost/pull/109) | REMOVE CANDIDATE — historical crash half-state out of product scope |
| `codex/sde-rhe-validation-20260927` | [#162](https://github.com/Jazz0006/CampBoardGameHost/pull/162) | REMOVE CANDIDATE — validation-only, closed |
| `docs/lre-p-inv1a-closure-20261004` | [#223](https://github.com/Jazz0006/CampBoardGameHost/pull/223) | REMOVE CANDIDATE — superseded LRE-P docs |
| `gsp-2b3b-player-context-edit-surface` | [#232](https://github.com/Jazz0006/CampBoardGameHost/pull/232) | **KEEP — R2 reference** |
| `gsp-r1c2c1c6c-drunk-shown-identity-recovery` | [#286](https://github.com/Jazz0006/CampBoardGameHost/pull/286) | **REMOVE CANDIDATE** — owner-rejected redundant hypothetical Recovery check; no merge |
| `sde-2d4-player-count-performance-audit` | [#148](https://github.com/Jazz0006/CampBoardGameHost/pull/148) | REVIEW — preserve distinct D4E performance evidence if not elsewhere |

## Unmatched remote refs — manual provenance audit first

- `codex/app-host-decomposition-audit` @ `a45eea8f7a5a` — no matching head in the 288 PR records queried; no deletion authorization without independent diff/evidence audit.
- `codex/drunk-late-binding-sequencing-audit` @ `5abac479e515` — no matching head in the 288 PR records queried; no deletion authorization without independent diff/evidence audit.
- `codex/ps4-4-validation-red-final` @ `d0ca5d386e31` — no matching head in the 288 PR records queried; no deletion authorization without independent diff/evidence audit.
- `docs/beginner-storyteller-mode-policy` @ `ce6563694e7a` — no matching head in the 288 PR records queried; no deletion authorization without independent diff/evidence audit.
- `sde-2d4-d4e-validation-20260919` @ `bf4f14cb29b6` — no matching head in the 288 PR records queried; no deletion authorization without independent diff/evidence audit.

## Merged-PR remote refs — batch cleanup candidates, pending exact live recheck

These 62 refs matched a PR with `merged_at`. Treat as **eligible for the next branch-pruning review**, not as proof of absence of unique post-merge work. Accepted code and documentation remain on main and their historical PR; check exact current branch tip and no new commits before deleting.

- `codex/app-host-first-wave` — PR #244
- `codex/app-host-history-payload` — PR #245
- `codex/app-host-registration-interaction` — PR #246
- `docs/app-host-decomposition-reaudit-20261007` — PR #240
- `docs/close-superseded-gsp-2b3-prs-20261008` — PR #262
- `docs/gsp-1-closure-20261006` — PR #226
- `docs/gsp-history-recovery-scope-audit-20261009` — PR #287
- `docs/gsp-r1-post-app-host-split-reaudit-20261008` — PR #247
- `docs/gsp-r1a-canonical-history-prefix-coverage-audit` — PR #248
- `docs/gsp-r1b-closure-r1c-handoff-20261008` — PR #251
- `docs/gsp-r1c1-closure-r1c2-handoff-20261008` — PR #253
- `docs/gsp-r1c2a-closure-r1c2b-handoff` — PR #257
- `docs/gsp-r1c2b-mayor-recovery-closure-20261008` — PR #259
- `docs/gsp-r1c2c0-closure-20261008` — PR #261
- `docs/gsp-r1c2c1a-typed-registration-acceptance-20261008` — PR #264
- `docs/gsp-r1c2c1b-acceptance-20261008` — PR #266
- `docs/gsp-r1c2c1c1-slayer-acceptance-20261008` — PR #268
- `docs/gsp-r1c2c1c2-virgin-acceptance-20261008` — PR #270
- `docs/gsp-r1c2c1c3a-klutz-acceptance-20261008` — PR #272
- `docs/gsp-r1c2c1c3b-klutz-acceptance-20261008` — PR #274
- `docs/gsp-r1c2c1c4-live-day-registration-reaudit-20261008` — PR #275
- `docs/gsp-r1c2c1c5a-reachability-acceptance-20261008` — PR #277
- `docs/gsp-r1c2c1c5b-acceptance-handoff-20261008` — PR #279
- `docs/gsp-r1c2c1c5c-ngj-klutz-acceptance-20261009` — PR #281
- `docs/gsp-r1c2c1c6-day-family-acceptance-20261009` — PR #283
- `docs/gsp-r1c2c1c6b-perceived-virgin-closure-20261009` — PR #285
- `docs/gsp-registration-ambiguity-history-contract-20261008` — PR #249
- `docs/gsp-registration-witness-fix-closure-20261008` — PR #255
- `docs/gsp-res5-reentry-audit-20261008` — PR #243
- `docs/gsp-route-reset-20261005` — PR #224
- `docs/res-2-closure-handoff` — PR #238
- `experiment/gsp-api0-responses-runner-20261009` — PR #288
- `fix/registration-result-witness-separation` — PR #254
- `gsp-1-special-policy-authority-revocation` — PR #225
- `gsp-2-provider-context-contract` — PR #227
- `gsp-2b-stateful-game-context` — PR #228
- `gsp-2b2-recovery-player-context` — PR #231
- `gsp-2b2-session-player-context` — PR #229
- `gsp-r1b-neutral-global-history-prefix` — PR #250
- `gsp-r1c-recovery-prefix-safety` — PR #252
- `gsp-r1c2a-frozen-causal-decision-journal` — PR #256
- `gsp-r1c2b-production-durable-causal-recovery` — PR #258
- `gsp-r1c2c-generic-registration-choice-audit` — PR #260
- `gsp-r1c2c1-typed-registration-producer` — PR #263
- `gsp-r1c2c1b-live-registration-confirmation` — PR #265
- `gsp-r1c2c1c-slayer-day-registration-vertical` — PR #267
- `gsp-r1c2c1c2-virgin-spy-townsfok-type` — PR #269
- `gsp-r1c2c1c3-klutz-spy-death-provenance` — PR #271
- `gsp-r1c2c1c3b-klutz-death-trigger-snapshot` — PR #273
- `gsp-r1c2c1c5a-reachable-klutz-retirement` — PR #276
- `gsp-r1c2c1c5b-tb-day-action-history` — PR #278
- `gsp-r1c2c1c5c-ngj-klutz-learned-choice` — PR #280
- `gsp-r1c2c1c6-ngj-public-day-coverage` — PR #282
- `gsp-r1c2c1c6b-drunk-virgin-recovery` — PR #284
- `res-0-engine-recommendation-separation-audit` — PR #233
- `res-1-pair-engine-decision-boundary` — PR #234
- `res-1b-generic-pending-decision-mayor` — PR #235
- `res-2-neutral-provider-contract` — PR #236
- `res-2a-context-memory-closure` — PR #237
- `res-3-legacy-heuristic-purge` — PR #239
- `res-4-named-special-policy-purge` — PR #241
- `res-5-physical-module-dependency-convergence` — PR #242

## Local-only branches — separate, reversible workstation cleanup

45 local branch names do not exist as same-name origin branches, mainly completed SDE/C5/DLB/TBGS/RES campaigns. These are **not auto-deleted** by remote cleanup. Before any local prune, inspect local ancestry/unique commits and other worktrees; keep current `main`, avoid force-deleting live work. Local-only list:

- `c5a-pair-future-flexibility-projector`
- `c5b-pair-sde-shadow-replay`
- `c5c-beginner-conservative-v2`
- `c5d-canonical-v1-v2-replay`
- `codex/current-only-recovery-cleanup`
- `codex/dead-code-cleanup-slice-2`
- `codex/sde-history-prefix-route-closure`
- `dlb-1-visible-roster-deal-cutover`
- `dlb-2-legal-drunk-candidate-projector`
- `dlb-3-shadow-drunk-decision-trace`
- `dlb-4-canonical-drunk-commit`
- `dlb-4a-experienced-drunk-selection`
- `dlb-5-first-night-dependency-barriers`
- `dlb-5h1-first-night-presentation-boundary`
- `dlb-6-old-contract-retirement`
- `dlb-7-final-acceptance`
- `dlb-c3-q04-policy-reentry`
- `dlb-drunk-assignment-cutover-gate-audit`
- `dlb-drunk-recommendation-context-capability-audit`
- `dlb-q04-v1-drunk-policy`
- `docs/host-ml0-readiness-20261003`
- `docs/imp-product-policy-calibration-20261002`
- `docs/legacy-recommendation-engine-retirement-audit`
- `docs/lre-staged-policy-cutover-route`
- `docs/recommendation-style-retirement-player-level-audit`
- `docs/sde-e3-e4-qualification-20260927`
- `docs/tbgs-2-post-2e-style-retirement-audit`
- `fix/host-role-localization`
- `lre-1-authority-manual-cutoff`
- `lre-p-investigator-inv1a-policy`
- `m10/clocktower/fe75692e-0927-44bf-99af-c310363fb6aa`
- `m8g5-mini-mcp-adoption-canary`
- `rsr-1a-storyteller-mode-style-decoupling`
- `sde-3b-beginner-conservative-v1`
- `sde-3c-decision-trace-shadow-replay`
- `tbgs-0-canonical-tb-snapshot-contract`
- `tbgs-1-drunk-snapshot-vertical-slice`
- `tbgs-1-post-interop-cutover-recheck`
- `tbgs-2a-first-night-pair-snapshot-context`
- `tbgs-2b-pair-manual-publication-snapshot-context`
- `tbgs-2c-setup-coordination-snapshot-context`
- `tbgs-2d-demon-succession-audit`
- `tbgs-2d-demon-succession-context`
- `tbgs-2e-mayor-redirect-context`
- `tbgs-2f-balance-style-context`

## Completion contract

Perform branch pruning only through explicit branch-delete tooling/UI with exact ref/PR review, then rerun local/remote branch listings and record before/after counts. This doc-only convergence changes **no Git ref and no Android / Host code**. When the pruning pass actually runs, update this audit with concrete deleted refs and proof, not a blanket statement of completion.
