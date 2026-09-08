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
| Clue UX-R4 — stable Top-1 + 0–2 alternatives + persistent Manual presentation | COMPLETE / VERIFIED / MERGED (#67) |
| Clue UX-R5 — small-domain numeric / Boolean specialization | COMPLETE / VERIFIED / MERGED (#68) |

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

## UX-R4 — unified recommendation presentation

### Accepted presentation contract

UX-R4 established a stable presentation layer downstream of the existing ordered Recommendation Provider rather than creating another ranking engine.

For already-ranked recommendation candidates:

1. preserve provider order and typed candidate identity;
2. primary recommendation = first candidate;
3. alternatives = next 0–2 candidates only;
4. empty / one / two-candidate inputs stay naturally sparse;
5. never create synthetic filler alternatives;
6. selecting Top-1 or an alternative passes the exact `ClocktowerDisplayOption` into the existing structured commit callback;
7. Manual remains a separate persistent authority path backed by the complete `manualInformationCandidates` legal domain;
8. recommendation count or recommendation absence cannot narrow or disable Manual legality;
9. UX-R4 does not add ranking, diversity, Productive Uncertainty, or PlayerWorldSet heuristics.

The first production target was first-night Washerwoman / Librarian / Investigator. Chef / Empath / Fortune Teller / Yes-No and other small-domain interactions were deliberately left for UX-R5.

### RED / GREEN evidence

- RED checkpoint: `93fdaf80861c3630904ecea2bbbc6c83082c50c5` — real Android unit-test compilation failed because the typed `clocktowerRecommendationPresentation` seam did not yet exist.
- GREEN seam checkpoint: `7386cdfd07388d4125eacebbb466e8adbca5a9cd` — pure typed presentation contract and focused tests; `:app:testFast` green.
- presentation UI component commit: `480a9cf409b1d920d85898f62dbcd1907f7edec5`.
- large-file production wiring commit: `d6f9846a78770e893ef67d3a7a3791a9ee461805`.

The `ClocktowerNightStepUi.kt` cutover used the repository-approved fail-closed one-shot route. Successful run `33505774988` verified locked head/blob identities, focused baseline, exact production patch, focused GREEN, `:app:testFast`, exact diff/semantic audit, remote-head recheck, guarded production push, and self-removal of temporary workflow/script.

Cleanup tree checkpoint: `cb4ec48fad2288a9aaa4e4b78791a154afde226e`.

### Final verified checkpoint

Final no-tree-change full-CI checkpoint:

`6f10e8792e9535c1d125fae9b07e32e81fdfa2a3`

Validation:

- R2 run `33506399747`: SUCCESS;
- CI run `33506399624`: SUCCESS;
- full Android unit tests + debug APK: SUCCESS;
- ASP contract tests: SUCCESS;
- Real Clingo 5.8.0 cross-validation: SUCCESS;
- CI gate: SUCCESS.

Final permanent PR diff contained exactly four files:

```text
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerPairRecommendationPresentationUi.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerRecommendationPresentation.kt
app/src/test/java/com/codex/campboardgamehost/ClocktowerRecommendationPresentationTest.kt
```

### PR #67 merge closeout

The direct Ready connector again hit the known GitHub GraphQL `fullDatabaseId` compatibility error. A parent-locked one-shot Ready transition was used instead:

- Ready run `33508684303`: SUCCESS;
- required verified parent `6f10e8792e9535c1d125fae9b07e32e81fdfa2a3`;
- marked PR #67 Ready and self-removed;
- cleanup head `60d6abf8e2e8a3ae76054d2023659f706fadb8b7`;
- compare from verified checkpoint to cleanup head had zero file differences.

PR #67 merged with expected-head protection.

Merge commit:

`d626093f5f527edfba181641cd2b07a50a559929`

---

## UX-R5 — small-domain specialization

### Accepted small-domain contract

UX-R5 extended the stable presentation architecture to numeric and Boolean domains whose complete legal outcome set is small enough to show directly.

Permanent contract:

1. complete legal-domain authority remains upstream of recommendation;
2. recommendation may mark one legal outcome as primary but cannot create, remove, narrow, or expand legal outcomes;
3. recommendation absence must still leave every legal outcome selectable;
4. small domains expose all remaining legal outcomes when the full domain comfortably fits the interaction;
5. numeric and Boolean semantic identity is typed end-to-end and is never reconstructed from localized display labels;
6. Foundation confirmation remains the commit authority;
7. Chef/Empath numeric presentation uses the same legal-domain/confirmation model rather than recommendation-derived legality;
8. Fortune Teller Yes/No remains bound to the exact actor seat and exact two selected subject seats;
9. the Fortune Teller player-pair interaction remains separate from the subsequent Boolean result domain;
10. UX-R5 adds no new recommendation scoring, diversity, Productive Uncertainty, or PlayerWorldSet ranking behavior.

For the stable UX surface this means:

```text
small complete legal domain
        |
        +--> recommendation marks primary
        |
        +--> primary + every remaining legal outcome
        |
        +--> typed Foundation confirmation
```

### Tests-first evidence

UX-R5 was implemented through multiple behavior-first RED/GREEN checkpoints rather than source-shape tests.

Key evidence:

- initial R5.1 small-domain contract RED: `f4aa1cbbf7573f79de0a9550537cc75efa95f371`;
- Fortune Teller Boolean-domain RED: `5d468c59a6b45f67990e2072a21c037b3dfb27a0`;
- typed Boolean information UI model: `111d213c86c4e28275e5d66180676fe1482581ff`;
- structured Boolean decision panel checkpoint: `932a9290547a9cfaed62eab309a47fa5dc1e4649`;
- typed Boolean display-option matcher RED: `dde9a8a13f567ac129f87045fc5cc75fe5f4d0c5`;
- typed Boolean display-option matcher GREEN: `d3b2167803b63956b2b85d1cad2118c575549fae`.

Chef numeric production wiring used the fail-closed large-file route. Successful one-shot run `33513833581` verified exact head/blob locks, focused GREEN, `:app:testFast`, production diff constraints, guarded push, and temporary workflow/script self-removal.

Fortune Teller production wiring likewise used a locked exact-anchor one-shot. Production commit:

`9ba67d4c906b8a94cee80ee7e1e64b33422f38d6`

Cleanup head after self-removing all Fortune Teller patch scaffolding:

`b1b96d9d6ad680101cbef089bbd74ddb62008e63`

The production cutover preserved the Host's authoritative Demon / red-herring / Recluse truth calculation and only added typed proposition/presentation wiring around it.

### Final verified checkpoint

A no-tree-change checkpoint was first used to recover from GitHub's expected bot-recursion suppression after the one-shot cleanup. Because the ordinary zero-change classifier correctly skipped heavy jobs, a second no-tree-change checkpoint with `[full-ci]` forced the complete final validation on the exact cleanup tree.

Final validated checkpoint:

`2f56649e71d38c21f66df598e1e8df0c990090dd`

Validation:

- R2 run `33563538200`: SUCCESS;
- full CI run `33563538249`: SUCCESS;
- full Android unit tests + debug APK: SUCCESS;
- ASP contract tests: SUCCESS;
- Real Clingo 5.8.0 cross-validation: SUCCESS;
- CI gate: SUCCESS.

The final permanent compare from UX-R5 base `d30d0f03cb6be811628938fec921a5c21662e4b8` contained 14 app/test files and no temporary `.github` workflow or patch-script files.

There were no submitted reviews, inline review comments, or review threads blocking merge.

### PR #68 merge closeout

The direct Ready connector again hit the known GitHub GraphQL `fullDatabaseId` compatibility error. A parent-locked Ready one-shot was used:

- required verified parent `2f56649e71d38c21f66df598e1e8df0c990090dd`;
- Ready one-shot run `33563934530`: SUCCESS;
- marked PR #68 Ready;
- self-removed its workflow;
- cleanup head: `240b9e89c2f958f7074de349c8d5156421d47d51`;
- compare from the full-CI checkpoint to cleanup head had zero file differences.

PR #68 merged with expected-head protection.

Merge commit:

`563470a2c3b4e3dc10732e00827e33ebee00884a`

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

When EPI-MQ, UX-R6, or later campaigns complete:

1. append a concise completed-slice section here containing the accepted behavior contract and verification evidence;
2. include final executable checkpoint and merge commit when available;
3. record unusual workflow/CI exceptions only when they affect future auditability;
4. remove corresponding detailed closeout material from `CURRENT_DEVELOPMENT_ROADMAP.md`;
5. leave only the permanent invariants and the newly active next slice in the current roadmap.
