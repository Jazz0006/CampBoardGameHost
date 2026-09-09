# Next Development Handoff — D6.2i Day Nomination Transient Ownership

> Date: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Target branch: `codex/d6-2-ui-composition`  
> Draft PR: `#115`  
> Latest validated production checkpoint: `15342f9e22ac204602680e6ef831fb4e95c7b0bf`  
> Status: **D6.2a–g COMPLETE / VALIDATED; D6.2h COMPLETE; D6.2i IMPLEMENTATION NEXT**

## 0. Mandatory first action in a new conversation

Do **not** start from remembered SHAs or guessed source counts.

Read, in order:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md`
5. this handoff
6. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`
7. `docs/D6_2G_RETIRED_STORYTELLER_PLUMBING_PROGRESS_2026-09-09.md`
8. `docs/D6_2F_LEGACY_STORYTELLER_RETIREMENT_PROGRESS_2026-09-09.md`

Then re-query live GitHub state:

```text
main
codex/d6-2-ui-composition
PR #115 state / draft status / head SHA
latest CI + R2 on the live head
```

Known state at handoff preparation:

```text
main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
validated production checkpoint: 15342f9e22ac204602680e6ef831fb4e95c7b0bf
PR #115: OPEN / DRAFT / NOT MERGED
```

The branch has docs-only commits after the production checkpoint. The new conversation must capture the **live docs head immediately before implementation** and call it, conceptually:

```text
BASE_DOCS_HEAD
```

That exact head becomes the intended parent of the final clean D6.2i production commit.

Do not merge PR #115 without explicit user authorization.

## 1. What is already complete

### D6.2f — validated legacy UI retirement

Production checkpoint:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
```

Result:

```text
0 additions / 685 deletions
ClocktowerHostScreen.kt
  330,257 -> 283,849 bytes
  5,491 -> 4,807 lines
```

FULL acceptance:

```text
R2 34288731422 — PASS
CI 34288731376 — PASS
  Android FULL + debug APK — PASS
  ASP — PASS
  Real Clingo — PASS
  CI gate — PASS
```

### D6.2g — validated dead-plumbing cleanup

Production checkpoint:

```text
15342f9e22ac204602680e6ef831fb4e95c7b0bf
```

Removed:

```text
records Judge forwarding
onPhaseChange
onShowResults
phaseTitle / phaseProgress / phaseScript / phaseAction
recordCurrentVote()
```

Current validated metrics:

```text
ClocktowerJudgeScreen
  parameters:              92
  on... callbacks:          34
  provider functions:        3
  MutableState parameters:   8

App-root Clocktower vars:    36
```

Acceptance:

```text
R2 34289616209 — PASS
CI 34289616204 — PASS
  Android FAST — PASS
  CI gate — PASS
```

D6.2f immediately before it already provided the FULL T4 gate.

## 2. D6.2h conclusion — exact next ownership boundary

D6.2h proved that the remaining Day mutable inputs do not share one ownership class.

### Move/localize

```text
nominatorNameState
nomineeNameState
```

These are transient square-table nomination interaction state. App only declares, resets and forwards them. They are not Recovery mechanics.

Recommended Judge lifetime:

```kotlin
var nominatorName by remember(gameId, round) { mutableStateOf<String?>(null) }
var nomineeName by remember(gameId, round) { mutableStateOf<String?>(null) }
```

### Delete

```text
currentVoteCountState
```

Modern `ClocktowerVoteTableScreen` owns pending vote selection/count through typed `ClocktowerTableVoteState`. The outer `currentVoteCount` has no reads and is only reset to zero.

### Keep external

```text
dayModeState
```

because App/Recovery/Klutz/Artist routing still writes it.

Keep durable vote mechanics external:

```text
ghostVoteAuthority
highestVoteNameState
highestVoteCountState
```

because they are persisted/restored by Recovery and affect execution standing across nominations.

## 3. D6.2i exact scope

Production files should be limited to:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
```

Intended changes:

```text
CampBoardGameHostApp.kt
- remove clocktowerNominatorNameState declaration
- remove clocktowerNomineeNameState declaration
- remove clocktowerCurrentVoteCountState declaration
- remove their Day/reset cleanup assignments
- remove their ClocktowerJudgeScreen forwarding arguments

ClocktowerHostScreen.kt
- remove nominatorNameState MutableState parameter
- remove nomineeNameState MutableState parameter
- remove currentVoteCountState MutableState parameter
- replace delegated external nomination pair with remember(gameId, round) locals
- delete currentVoteCount local/delegate completely
- delete all now-dead `currentVoteCount = 0` writes
- preserve explicit nominator/nominee clear on nomination cancel and vote completion
```

Do **not** change:

```text
dayModeState
ghostVoteAuthority
highestVoteNameState
highestVoteCountState
onGhostVoteAuthorityChange
onPreflightVirginExecution
onVirginNomination
vote transaction helpers
Recovery schema/serialization/restore
ClocktowerGameSession
```

Expected final metrics if the live source still matches the audit:

```text
Judge parameters:         92 -> 89
callbacks:                34 unchanged
MutableState params:       8 -> 5
App-root Clocktower vars: 36 -> 33
```

These are **postconditions to verify**, not permission to skip re-counting the live source.

## 4. Lifecycle proof that must be preserved

Current round lifecycle:

```text
FirstNight round 1
-> Day round 1
-> Night round 2
-> Dawn round 2
-> Day round 2
-> Night round 3
...
```

Normal Day completion increments round before entering Night. Therefore `remember(gameId, round)` expires the nomination pair at the same durable boundary as the old App reset.

Special paths already audited:

```text
Virgin immediate execution + continue
  -> increments round before Night
  -> local pair expires

Day Klutz
  -> nomination/vote pair already cleared before Klutz continuation
  -> continuing advances round

Night death -> Klutz -> Dawn
  -> occurs in already advanced round
  -> no Day nomination pair to restore
```

No Recovery contract was found for restoring an in-progress nomination pair after process restart.

## 5. Existing tests — do not manufacture RED

Relevant behavior tests already exist:

```text
ClocktowerDayNominationGestureTest
ClocktowerTableVoteStateTest
ClocktowerVoteTransactionTest
```

This is a behavior-preserving ownership refactor. Do **not** create source-string/snapshot tests merely to say tests-first.

Minimum implementation evidence inside the bootstrap runner:

```text
:app:compileDebugKotlin
focused existing nomination/vote tests when practical
:app:testFast
```

The final clean production checkpoint should preferably use `[full-ci]` because the state lifetime moves across App/Judge.

## 6. Proven GitHub large-file execution method — use this, do not improvise

The current chat/container could not clone the repository directly. The working method is a temporary GitHub Actions bootstrap that edits the complete checkout on GitHub's runner.

Normative background:

- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

For D6.2i, use the following proven sequence.

### Step A — capture exact live base

Before creating any temporary file:

```text
re-query feature branch HEAD -> BASE_DOCS_HEAD
fetch current App/Host blobs or complete relevant source
re-audit every exact occurrence to be changed
record expected changed-file allowlist
```

**Never reuse occurrence counts from this handoff without re-reading live source.**

The D6.2 campaign already caught multiple hidden duplicate consumers this way.

### Step B — bootstrap files

Preferred safe pattern for a non-trivial large-file patch:

```text
.github/workflows/d6-2i-bootstrap.yml
.github/scripts/d6_2i_patch.py
```

The workflow is orchestration only. Put multiline Kotlin anchors/replacements in the separate Python script.

Trigger the workflow from the workflow-file push itself, scoped to:

```text
branch = codex/d6-2-ui-composition
path = .github/workflows/d6-2i-bootstrap.yml
```

The script should already be present in the same branch before the workflow-triggering commit.

### Step C — fail closed before mutation

Workflow/script must verify, before changing production:

```text
checkout HEAD == workflow triggering SHA
remote feature branch HEAD == workflow triggering SHA
expected App/Host blob(s) or exact anchors still match
all intended old anchors have the exact expected occurrence count
all counts are validated before applying the first replacement
```

For multiple edits, never partially patch after only some anchors have been validated.

No fuzzy matching. No guessed line numbers. No broad regex fallback.

### Step D — semantic postconditions after patch

Assert at least:

```text
old App state declarations absent
old Judge MutableState parameters absent
currentVoteCount identifier absent from the intended Judge flow
new remember(gameId, round) nominator/nominee locals present
required durable vote/Recovery names still present
final Judge param count == expected live target
final on-callback count unchanged
final MutableState param count == expected live target
```

Again: determine exact expected counts from the current live source first.

### Step E — compile/test before any product commit

Run:

```text
:app:compileDebugKotlin
focused existing nomination/vote tests if selected
:app:testFast
```

If compile/test fails, the workflow must stop before product commit.

### Step F — exact diff audit

Before commit:

```text
git diff --check
changed production files exactly App + Host
no generated Gradle files
no unrelated cleanup
```

The temporary workflow/script may be staged only for their own deletion/cleanup bookkeeping, not retained in the final production tree.

### Step G — runner commits and self-removes temporary files

The successful runner can:

```text
apply production patch
run tests
remove temporary workflow/script
commit/push final tree back to codex/d6-2-ui-composition
```

The important artifact is the **final tree**, not the bootstrap commit history.

### Step H — rebuild one clean production commit from the final tree

This D6.2 technique is proven and should be reused when connector Git data actions are available:

```text
1. fetch the successful runner head commit
2. record its final tree SHA
3. create a new commit with:
   tree = successful runner final tree
   parent = BASE_DOCS_HEAD
   message = clean production message, preferably with [full-ci]
4. force-update ONLY refs/heads/codex/d6-2-ui-composition to the new clean commit
```

Recommended clean message:

```text
refactor: localize Day nomination selection ownership [full-ci]
```

This removes temporary workflow create/fix/delete commits from the final feature-branch history while preserving the exact tested production tree.

Do **not** force-update `main`.

### Step I — exact post-squash compare

Compare:

```text
BASE_DOCS_HEAD ... CLEAN_PRODUCTION_SHA
```

Require:

```text
ahead_by = 1
behind_by = 0
exactly 2 changed production files
no .github/workflows temp file
no .github/scripts temp file
no unrelated docs/config/source files
```

Only after this exact compare is clean should the clean production checkpoint be considered ready for remote validation.

### Step J — normal clean-head CI/R2

The clean connector-created/force-updated head should trigger normal PR validation.

If the clean commit message contains `[full-ci]`, verify that the CI classifier actually selected and ran the expected FULL jobs; do not infer FULL merely from a green aggregate status.

For a full checkpoint verify:

```text
Android FULL unit tests
:app:assembleDebug / debug APK
ASP contracts when selected
Real Clingo when selected
CI gate
R2 main-thread boundary
```

Do not push docs-only closeout commits until the required clean production run IDs and conclusions are captured.

## 7. Proven failure signatures — interpret them correctly

### `jobs=[]` immediately after workflow trigger

Meaning:

```text
YAML parse/startup failure
```

This happened in D6.2g when inline Python indentation made the workflow invalid.

Correct action:

```text
production was not touched
fix YAML/orchestration only
prefer separate Python patch script
rerun from a new exact bootstrap head
```

Do not treat it as production test failure.

### Exact occurrence assertion fails before patch

This happened in D6.2f when an expected helper occurrence count was guessed as 5 but live source contained 6.

Correct action:

```text
production untouched
inspect complete live source
explain the extra occurrence
update the assertion only after understanding it
```

Do not simply loosen `== N` to `>= N`.

### Additional consumer discovered

D6.2d initially accounted for one Artist UI path but the full Host source contained a second fallback consumer.

The exact assertion prevented a partial unsafe migration.

Rule:

> Before a multi-consumer rename/ownership move, search the complete source and update all live consumers atomically.

### Kotlin compile fails after parameter -> delegated Compose local

D6.2d exposed smart-cast restrictions because a nullable parameter became a delegated mutable property.

The correct repair was to snapshot the delegated nullable value into an immutable local before Boolean calculations. Do not broaden the architecture to fix a compiler stability issue.

### Bot/temporary history versus clean production history

Temporary workflow commits are execution scaffolding, not product history. Preserve the tested final tree, then rebuild the clean product commit with the intended docs head as parent and verify with exact compare.

## 8. Testing cadence

Follow `docs/TESTING_STRATEGY.md`.

Do not run every expensive suite for every failed bootstrap edit.

Recommended D6.2i cadence:

```text
bootstrap source assertions
-> compileDebugKotlin
-> focused nomination/vote tests as useful
-> testFast
-> runner product tree
-> clean one-commit history reconstruction
-> [full-ci] clean-head CI + R2
-> docs closeout only after clean-head acceptance
```

A failed workflow syntax or source-count assertion does not justify FULL CI because production was never changed.

## 9. Stop conditions

Stop and re-audit rather than improvising if:

- live branch moved after `BASE_DOCS_HEAD` was captured;
- App/Host blob or anchor does not match;
- any exact occurrence count differs;
- more than the expected two production files change;
- compile/focused/FAST fails outside an understood in-scope issue;
- Recovery/durable vote state appears to depend on the nomination pair contrary to D6.2h;
- implementing the slice would require moving `dayModeState`, ghost authority or highest-vote mechanics;
- the clean compare is not exactly one production commit from `BASE_DOCS_HEAD`.

Do not expand D6.2i to Night navigation, Recovery redesign, recommendation behavior, Day dispatcher extraction or unrelated helper cleanup.

## 10. Expected closeout after successful D6.2i

Only after clean production CI/R2 passes:

1. update `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
2. update `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`;
3. add a D6.2i progress document with exact diff/metrics/run IDs;
4. update PR #115 body so it no longer describes only D6.2b;
5. distinguish validated production checkpoint from later docs-only branch head;
6. do not merge PR #115 without explicit user approval.
