# Code Review — 2026-10-22 (main at 991d6344)

Method: four parallel read-only reviews (Session core, UI, AI/security/build, setup/catalog/persistence). The two top bugs were re-verified by hand. Nothing was built or run; findings are from reading code. Items marked "verify" are unconfirmed rule questions.

## Verdict on the three rules

- **Single object: mostly met.** All game facts go through one Session writer; AI only returns suggestions that Host re-verifies. Exceptions: Night→Day is triggered from a UI side effect, and a test-only method (`updateGameState`) can overwrite state without a fact.
- **Minimum implementation: partly met.** Every file is under 50KB, but by splitting, not deleting (~28k lines remain). Dead weight: AI gateway path, custom-script hashing/parsing, unused domain types, a diversity selector used in a single configuration, V1/V2 suffixes.
- **Module borders: weakest.** ~100 files flat in the root package; `clocktower/ui` files declare the root package; `session/` mixes ~90KB game authority with ~120KB AI code; cycles domain↔epistemic and catalog↔rules; domain imports UI-side types.

## Bugs (most serious first)

1. **Imp stops waking after succession.** `ClocktowerNightFlow.kt:34` uses `firstOrNull { wakingRole() == role }` with no alive filter. A dead Imp keeps role Imp; if its seat is lower than the successor's, the Imp step is skipped every later night. Same pattern in `ClocktowerNightHostProjection.kt:43`. Fix: prefer alive holder; allow dead only for Ravenkeeper/Sage triggers. Add a night 2→3 succession test with the old Imp in seat 1.
2. **Dead nominees blocked in Day UI.** `ClocktowerDayHostScreen.kt:144-149` and `ClocktowerDayNominationGesture.kt:22-29` require alive targets; Session (`ClocktowerDayFlow.kt:154`) only requires an alive nominator. Already listed as open in AGENTS.md.
3. **Phase-advance guard hole.** `ClocktowerGameSession.kt:344-350` checks from-phase/round only when an earlier PhaseAdvance exists; also ignores finished game and pending Klutz choice. Night 1 can be skipped.
4. **Night→Day not idempotent**, fired from `LaunchedEffect` (`ClocktowerHostScreen.kt:428-431`); action id includes revision. Add `confirmNightComplete` in Session with a deterministic id.
5. **Legacy history record without identities makes TB setup throw** (`TroubleBrewingSetupRotationHistory.kt:326-328`).
6. **Abandoned games never enter setup history** (`CampBoardGameHostApp.kt:488-497`); requirement 2 silently missed.
7. **One bad history entry wipes all history** (`TroubleBrewingSetupRotationHistoryStore.kt:116-119`).
8. **JSON diversity policy parsed but unused**; selector runs on hardcoded defaults. NGJ has no diversity logic.
9. Smaller: rotation keyed by raw display name; results winner inferred from localized text (`ClocktowerResultsScreen.kt:57-58`); no Back handling on role reveal; joint-opening busy flag can stick (`ClocktowerOpeningDraft.kt:150`); `runCatching` swallows cancellation; no wall-clock deadline on AI requests; `allowBackup=true`; persistence `persistWith` saves before assigning; active-game store can leave disk ahead of memory on read-back mismatch.
10. **Verify rules:** poisoned Virgin consuming first nomination; Klutz death at night; Undertaker after executing a dead player; Klutz ignoring poison.

Security is good: API key AES-GCM in Keystore, noBackup dir, fixed HTTPS endpoint, no redirects, model output verified by Host. Prompts are sent with `store=true`, which should be disclosed in Settings.

## Improvements

- **Duplicated rules** (Monk protection, Scarlet Woman succession, death+promotion, vote threshold, poisoned-at-death) live in 4–5 places each; consolidate into Session functions.
- **UI**: four palettes, two alignment color tables, five hand-built seat models; theme chosen per screen instead of from Session phase (contradicts the visual contract). UI recomputes threshold, ghost votes, neighbours.
- **Giant functions**: `CampBoardGameHostApp` ~840 lines, `ClocktowerJudgeScreen` ~770.
- **Delete**: gateway path and `post()`, `legalCandidates`, `LEGACY_LOCAL`, unused domain types, v2 history migration, custom-script hasher/`parseScript`, test-only Session writers, dead JSON policy fields.
- **Build**: no lint/ktlint/detekt, no release build type, stale Compose BOM, no Gradle wrapper checksum, AGP legacy opt-outs.
- **Packages**: split root into app / ui(theme, table, night, day) / ai / deal / settings; move AI out of `session/`.

## Rules going forward

1. One fact, one function: search before adding a rule check; rules live in Session.
2. Deletion beats splitting; size limits are a smell detector, not a goal.
3. Packages match folders and point one way: ui → session → domain, ai beside session; domain never imports UI/Android.
4. UI draws and forwards taps; it never computes eligibility, thresholds or winners.
5. Every rule bug gets a multi-night regression test.
6. No version suffix unless two versions coexist.
7. One short README per package: its job and what it must not import.
8. Review once per feature for duplicates, dead code, and a test for the changed rule.

Suggested order: bugs 1–4, Session consolidation, package moves, dead-code deletion.

## Hygiene done with this review

- `S001.json` and `S001.prompt.txt` moved to `../CampBoardGameHost-offrepo/review-samples/` and ignored in `.gitignore`.
- `artifacts/` removed from the working tree (still in git history) and ignored.
- Unreferenced top-level docs moved to `docs/archive/`; only current authority docs remain.
