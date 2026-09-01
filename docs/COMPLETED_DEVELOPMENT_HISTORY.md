# CampBoardGameHost — Completed Development History

> Created: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Purpose: durable archive for completed development slices, accepted contracts, validation checkpoints, and merge evidence.

## Maintenance policy

`docs/CURRENT_DEVELOPMENT_ROADMAP.md` is the active project-status authority and should stay short.

Use this file for completed work:

1. while a slice is active, keep only the current objective, permanent constraints, scope guards, and next route in the active roadmap;
2. when a slice is complete and merged, move its detailed acceptance contract, RED/GREEN evidence, CI checkpoint, merge evidence, and historical notes here in one batch;
3. do not copy large historical details back into the active roadmap unless they become active constraints again;
4. keep permanent architectural invariants in the active roadmap even if they were established by an older slice;
5. always re-query live GitHub state rather than treating historical SHAs below as current branch state.

---

## Completed campaign registry

| Area | Final status |
|---|---|
| MS-SETUP generic multi-script architecture | COMPLETE / MERGED |
| Clue UX-R1 — audit global Automatic/Manual/RecommendationStyle dependencies and legal-domain authority | COMPLETE |
| Clue UX-R2A — shared pair-information legal-domain authority | COMPLETE / MERGED |
| Clue UX-R2B — pair Manual flow -> shared legal-domain authority + typed registration-preserving commit path | COMPLETE / VERIFIED / MERGED (#64) |
| Clue UX-R3 — remove normal global storyteller mode selector | COMPLETE / VERIFIED / MERGED (#65) |

Older MS-SETUP handoff/checkpoint documents remain historical evidence and are not repeated here in full.

---

## UX-R2B — pair Manual authority

### Accepted pair-information contract

For first-night Washerwoman/Librarian/Investigator:

1. `PairInformationLegalDomain` is the sole selectable semantic authority.
2. Manual availability is independent of recommendation coverage or `RecommendationStyle`.
3. Manual and recommendation paths share the same complete legal semantic domain.
4. Legacy/recommended option sets may provide presentation templates or compatibility telemetry only; they do not define legality.
5. Structured selection commits by resolving through the legal domain; localized labels are not parsed to recover legality or registration.
6. Exact Spy/Recluse registration facts are preserved in `AbilityObservation`.
7. Pair-family authoritative publication may intentionally differ from the historical curated shortlist.
8. Non-pair first-night families retain their existing migration/parity gate.
9. Investigator zero-minion remains illegal; Librarian zero-outsider remains legal.

Permanent acceptance condition established by UX-R2B:

```text
recommendation unavailable != manual unavailable
```

### Historical merge evidence

PR #64 established the accepted UX-R2B contract and merged on 2026-09-01.

- final validated executable/code checkpoint: `ad2ec9b4de117ac74c02deb6a5a77e65c2a0e4b4`
- docs-closeout checkpoint: `4dbc1235b1938495bfac97f88ceab55df5307968`
- merge commit: `2c5e55ac708fc36abb2b58f99714efbfe97547ca`

The accepted validation route included focused UX-R2B contract tests, `:app:testFast`, R2 main-thread boundary, ASP contract tests, Real Clingo cross-validation, Android full unit tests + debug APK, and final CI gate.

A temporary workflow was used only for the Ready transition after the direct connector action hit the known GitHub GraphQL compatibility issue; it self-removed and the cleanup tree matched the validated docs-closeout tree.

---

## UX-R3 — global storyteller mode selector removal

### Accepted product-flow contract

The old normal Settings selector is removed.

The legacy persisted enum values remain:

```text
MANUAL
AUTO_BALANCED
AUTO_AGGRESSIVE
AUTO_GENTLE
```

Earlier prose sometimes called the fourth style “Conservative”; the compatibility enum uses `GENTLE`. UX-R3 deliberately did not rename or delete the legacy enum because it remains migration/internal compatibility state.

For normal product UX, every legacy stored value is normalized to:

```text
automaticExecution = false
recommendationStyle = BALANCED   # temporary compatibility ranking input
```

This means:

- recommendation content is available without a global preselection;
- “recommendation always on when supported” does not mean automatically applying Storyteller rulings;
- normal interaction remains Storyteller-confirmed / ASSISTED;
- old AUTO preferences cannot silently restore automatic execution;
- old Aggressive/Gentle preferences cannot survive as hidden front-door policy;
- per-interaction Manual remains available and uses UX-R2B legal-domain authority;
- internal `RecommendationStyle` dimensions may remain temporarily until later ranking replacement.

`StorytellerAutomationMode` and preference plumbing were intentionally retained for compatibility and migration.

### RED evidence

Real PR CI confirmed the behavior-first RED at:

`71638e977cb69066fbe5de09c7825e9254e89d06`

Evidence:

- CI run `33498257864`;
- Android test compilation failed exactly because `StorytellerRecommendationUxPolicy` did not yet exist;
- Real Clingo cross-validation was green;
- an earlier malformed temporary workflow produced zero jobs and was not counted as RED evidence.

### GREEN production cutover

The large `CampBoardGameHostApp.kt` wiring was changed using the repository-approved fail-closed one-shot route.

Successful one-shot run:

`33498816111`

It verified:

- exact branch head;
- locked App-root target blob;
- each exact multiline anchor occurred once;
- only the intended App-root file changed during the patch step;
- focused UX-R3 policy tests passed;
- UX-R2B Manual-authority regressions passed;
- `:app:testFast` passed;
- production commit was created only after those gates;
- temporary Python patch script and workflow self-removed.

A first workflow attempt failed before job creation because an unquoted YAML `if:` expression contained a commit message with `chore:`. No production patch ran in that failed attempt.

### Final executable checkpoint

Final validated executable head:

`6cb9cb542b9e25d718a2a035e37475f99388ed2e`

Validation:

- R2 main-thread boundary run `33499085403`: SUCCESS;
- CI run `33499085434`: SUCCESS;
- Android FAST: SUCCESS;
- Real Clingo cross-validation: SUCCESS;
- CI gate: SUCCESS;
- full Android / ASP: skipped by the repository change classifier for this slice.

Permanent production/test files at the executable checkpoint:

```text
app/src/main/java/com/codex/campboardgamehost/AppSettingsScreen.kt
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicy.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicyTest.kt
```

### PR #65 closeout and merge

- docs-closeout checkpoint: `e84997f48e70e565eddad9b6f14d06b3db1a6efa`
- docs-closeout CI run `33499530332`: SUCCESS
- docs-closeout R2 run `33499530280`: SUCCESS
- the docs-closeout checkpoint was exactly one docs-only commit after the executable checkpoint and changed only `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
- no review threads or submitted review blockers existed
- direct Ready connector hit the known GitHub GraphQL `fullDatabaseId` compatibility error
- a one-shot Ready-transition workflow was used and self-removed
- cleanup head `c67a02ec80889a534254fb5eb40e83da3a8fbf3b` had zero file differences from the validated docs-closeout tree
- PR #65 merged with expected-head protection
- merge commit: `f5a0e2cf8776866441bcd32729fcdc43d4f70f9b`

Post-merge roadmap synchronization produced docs-only successor `cf604f490eb0a4683f641088216e2077426387e9` on `main`.

---

## Historical architecture notes retained as permanent constraints elsewhere

The following were established by completed work but remain active invariants, so their authoritative copy stays in `docs/CURRENT_DEVELOPMENT_ROADMAP.md` rather than being archived away:

- actual identity -> shown identity -> perceived ability ordering;
- healthy legal/truth semantic domain before reliability state;
- interaction-scoped registration semantics;
- recommendation and Manual both downstream of complete legal semantic authority;
- exact Spy/Recluse registration preservation;
- Manual is a user authority path, not a recommendation style;
- A3 exact enumeration remains correctness baseline;
- A4/ZDD remains shadow/prototype until separately validated.

---

## Archive update convention for future slices

When UX-R4, UX-R5, EPI-MQ, UX-R6, or later campaigns complete:

1. append a concise completed-slice section here containing the accepted behavior contract and verification evidence;
2. include final executable checkpoint and merge commit when available;
3. record unusual workflow/CI exceptions only when they affect future auditability;
4. remove corresponding detailed closeout material from `CURRENT_DEVELOPMENT_ROADMAP.md`;
5. leave only the permanent invariants and the newly active next slice in the current roadmap.
