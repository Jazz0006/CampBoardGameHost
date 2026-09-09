# D6.2l — Retired Day/History UI cleanup

> Date: 2026-09-09
> Status: COMPLETE / VALIDATED; FULL CI and R2 PASS.
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Parent: `1be8f428b795417cdd4fa9c40c7b89798b44daa1`.
> Production/workflow checkpoint: `69655de1d992ec6ec7cf45b2639a048ae4fb32e4`.
> Tree: `2ae09b70fee028bc9b9073629a25fe5ed6ed048a`.
> main: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.

## Scope and evidence before deletion

Implements section 3.B / R0 of `D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md`, following AGENTS.md, TESTING_STRATEGY.md and the D6.2k closeout. The explicit allowlist is the two UI files below plus `.github/workflows/r2-write-probe.yml`.

Whole `app` and workflow reference searches reconfirmed:

| Retired declaration | Production references before deletion |
|---|---|
| ClocktowerNominationScreen | Definition only |
| ClocktowerVoteScreen | Definition only |
| ClocktowerSpecialDayActionScreen | Definition only |
| ClocktowerGameRecordPanel | Definition only |
| ClocktowerTimelineRow | Definition and one call inside retired GameRecordPanel |
| ClocktowerPlayerStatusRow | Definition only |

These declarations had no live entry point or test consumer. The R2 workflow was requiring their existence as evidence of a historical extraction. Those specific checks were obsolete; the implementation retires them together with the code, without keeping empty functions or inventing replacement source-shape tests.

## Result

| File | Bytes before → after | Lines before → after | Diff |
|---|---:|---:|---:|
| ClocktowerDayScreen.kt | 50927 → 31846 | 1033 → 616 | +0 / -417 |
| ClocktowerHistoryScreen.kt | 38365 → 29188 | 725 → 530 | +0 / -195 |
| .github/workflows/r2-write-probe.yml | 11155 → 10887 | 224 → 218 | +2 / -8 |

Production Kotlin: 612 deleted lines, including seven directly orphaned imports. Total three-file diff: +2 / -620. No new abstraction, state owner, dependency, rule or persistence format was introduced.

Retained function blocks were compared with the parent and are byte-identical, excluding separator whitespace outside the blocks:

- ClocktowerDawnSummaryScreen, ClocktowerExecutionConfirmScreen, ClocktowerDayActionHeader.
- clocktowerEventPhaseLabel, ClocktowerResultsDialog, ClocktowerResultPlayerRow.

Current square-table nomination/vote/Slayer/Artist/Klutz rendering and typed state were untouched. App and Host were untouched; their state lifetime, callback and transaction ordering, Recovery and A4 behavior are unchanged. AppGameReviewScreens still consumes the retained phase-label function.

R2 continues to enforce ownership and existence for the active Day/History surfaces, the square-table overview, and all other extracted boundaries. Only membership for retired declarations and the retired TimelineRow assertion were removed. Workflow triggers, permissions, execution steps and other checks are unchanged.

## Validation

- Local exact scope, preserved-function comparison and `git diff --check`: PASS.
- Exact shell body extracted from the edited R2 workflow and executed locally: PASS.
- No references to the six retired symbols remain in `app` or workflows.
- Local and remote complete tree SHAs match; normal fast-forward from the expected parent.
- [FULL CI 34294224391](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34294224391) PASS: full Android JVM tests + debug APK, ASP contracts, Real Clingo and CI gate. Android logs confirm executed full tests and assemble, not FAST-only validation.
- [R2 34294224399](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34294224399) PASS.
- Remote compare: exactly one commit from the expected parent, zero behind and the three-file +2 / -620 diff.

The workflow change explicitly requires the full gate; the commit includes `[full-ci]`. FAST alone is insufficient here. Existing behavior coverage is used; no manufactured RED was introduced for unreachable UI deletion. Local Android compilation cannot run in this workspace because the SDK is absent and Gradle bootstrap network access was blocked. No local Android GREEN or real-device validation is claimed.

## Next

R0 continues with a separate bounded deletion of the private App decoder island and unused `clocktowerRolesFor`. Reconfirm that the actual archive codecs and Recovery v2 planners remain the live owners. Preserve `generateClocktowerAssignments` and every active setup/recovery/archive path. After that cleanup, move to the global audit's information preparation/materializer boundaries rather than further arbitrary file shrinking.
