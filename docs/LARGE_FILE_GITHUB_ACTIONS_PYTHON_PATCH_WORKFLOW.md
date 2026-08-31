# Large-file GitHub Actions + Python Patch Workflow

> Role: **NORMATIVE / LARGE-FILE EDITING SOP**  
> Effective: 2026-08-31  
> Applies to: `Jazz0006/CampBoardGameHost`  
> Purpose: safely edit large/truncated source files without replacing the whole file through the connector and without requiring Luna as the first fallback.

## 1. Mandatory writer priority

Use this order unless the user explicitly chooses another execution path:

```text
A. small / medium file, complete connector read/write is safe
   -> GitHub connector direct edit

B. large / truncated file, exact localized edit is possible
   -> GitHub Actions one-shot workflow
   -> separate Python patch script
   -> exact HEAD/blob/anchor locking
   -> tests + diff audit
   -> commit/push
   -> self-remove temporary workflow/script

C. Path B cannot be made safe, repeatedly fails for an environmental reason,
   or the work genuinely requires a complete local worktree / broad mechanical edit
   -> Codex/Luna local implementation
```

**For a large source file, Path B is the first choice. Luna is the fallback, not the default.**

Typical Path B targets include `CampBoardGameHostApp.kt` and other files whose connector output is truncated or whose whole-file replacement would be unnecessarily risky.

## 2. Why this pattern is preferred

The proven pattern separates three concerns:

```text
Chat / connector
  decides exact semantic change and writes small test/workflow/script files

GitHub runner
  owns a complete checkout of the large source file and executes the patch/tests

Python patch script
  makes one fail-closed, exact-anchor textual edit to the complete file
```

This avoids:

- reconstructing a very large file from truncated connector output;
- sending a narrow change to Luna when GitHub can perform it deterministically;
- line-number-based edits drifting after unrelated insertions;
- YAML quoting/indentation corruption from embedding non-trivial Python directly in a workflow;
- silent fuzzy replacements or partial edits.

This pattern has been successfully used by the TBSP campaign, including the 6J large-App cleanup and 6L provenance durability repair.

## 3. Preconditions — lock the exact remote state first

Before creating the one-shot workflow, record all applicable locks:

```text
target repository
exact target branch
exact current branch HEAD
exact target large-file blob SHA
exact test-file blob SHA when a RED/guard commit already exists
changed-file allowlist
exact intended semantic replacement
required focused evidence
checkpoint-level broader evidence only when this slice is a checkpoint
```

For the target file, prefer a Git blob lock over a line-count assumption:

```bash
git hash-object app/src/main/java/.../CampBoardGameHostApp.kt
```

The workflow must compare that value with the expected blob SHA before editing.

Also verify both local checkout identity and live remote branch identity:

```bash
test "$(git rev-parse HEAD)" = "$GITHUB_SHA"
test "$(git ls-remote origin refs/heads/<branch> | cut -f1)" = "$GITHUB_SHA"
```

If either check fails, **stop before patching**.

## 4. If the change is a real bug, establish durable RED first

When the production change fixes a stable behavior/invariant that was not covered, normally create the test in its small/medium test file through the connector first.

Preferred sequence:

```text
connector adds/strengthens durable regression test
-> test commit becomes current branch head
-> one-shot workflow locks that exact test blob
-> runner executes focused test against unmodified production
-> expected assertion/JUnit failure = RED PASS
-> only then apply large-file production patch
-> rerun same focused test = GREEN
```

For a behavior-preserving refactor, do not manufacture RED. Use the existing owning characterization/compile evidence instead.

## 5. Always use TWO temporary files for non-trivial patches

Use:

```text
.github/workflows/<slice>-one-shot.yml
.github/scripts/<slice>_patch.py
```

**Do not place a non-trivial Python patch body directly inside YAML.**

The workflow should contain only orchestration and shell commands. The Python file should contain the multiline source anchors and replacements.

### Proven failure: inline Python broke YAML before any job started

During TBSP-6L, an early workflow embedded Python containing triple-quoted multiline strings directly under a YAML `run: |` block. The Python literal lines did not remain validly indented as YAML block content.

Observed symptom:

```text
workflow run created
status = failure
jobs = []
no checkout/test/patch step ever started
```

Interpretation:

> This is a workflow parse/startup failure, not a RED test failure and not a production failure.

Correct response:

```text
do not touch production
-> inspect workflow source
-> move Python body to .github/scripts/<slice>_patch.py
-> keep workflow as a simple `python3 <script>` caller
-> rerun from a new exact head
```

This is now the required default pattern.

## 6. YAML quoting and indentation rules

### 6.1 YAML block indentation

For shell steps use:

```yaml
- name: Exact diff audit
  shell: bash
  run: |
    set -euo pipefail
    git diff --check
```

Every line belonging to `run: |` must remain indented under the block. Do not let Python multiline source text escape to the YAML indentation level.

### 6.2 Shell quoting

Use single quotes for literal test names and paths that do not require variable expansion:

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.codex.campboardgamehost.SomeRegressionTest' \
  --rerun-tasks --no-daemon
```

Use double quotes when shell variables must expand:

```bash
test "$(git rev-parse HEAD)" = "$GITHUB_SHA"
```

### 6.3 Python quoting

Multiline Kotlin/source anchors belong in the separate `.py` file. Triple quotes are safe there:

```python
old = """                committedThing = preparedThing
                nextCall(
"""
new = """                committedThing = preparedThing
                persistThing()
                nextCall(
"""
```

Do not add backslash escaping merely because the text contains Kotlin quotes unless Python itself requires it.

### 6.4 Heredoc rule

A tiny read-only audit may use:

```bash
python3 - <<'PY'
...
PY
```

The quoted delimiter `<<'PY'` prevents shell interpolation.

However, **a production patch with multiline source anchors should still use a separate checked-in temporary Python script**. Do not downgrade back to inline Python just because a heredoc can technically work.

## 7. Newline and encoding rules

Large-file patching must not silently rewrite line endings.

Before textual patching, inspect raw bytes and fail closed if the file is not in the expected format:

```python
from pathlib import Path

path = Path("app/src/main/java/.../Target.kt")
raw = path.read_bytes()

if b"\r\n" in raw:
    raise SystemExit("Unexpected CRLF source; do not normalize a large file implicitly")
if b"\r" in raw:
    raise SystemExit("Unexpected CR/mixed line endings")

text = raw.decode("utf-8")
```

For the normal repository LF format, multiline anchors in Python must therefore use `\n`/literal LF exactly.

When writing:

```python
path.write_text(text, encoding="utf-8", newline="\n")
```

Rules:

- preserve UTF-8;
- preserve LF;
- never normalize a large file from CRLF to LF as a side effect of a one-line patch;
- if the repository file unexpectedly uses CRLF, stop and design a byte-preserving patch instead of proceeding automatically.

## 8. Do not patch by absolute line number

Absolute source line numbers are for human reporting only.

Do **not** implement a patch as:

```text
insert at line 2342
replace lines 4100-4104
```

Large files move constantly. A previous line number becomes stale as soon as another change is inserted above it.

Preferred authority order:

```text
1. exact branch HEAD
2. exact target-file blob SHA
3. exact semantic multiline anchor
4. anchor occurrence count == 1
5. post-patch semantic assertions
6. exact diff allowlist
```

If useful, report the resulting line number after the patch, but never use it as the primary mutation locator.

## 9. Python patch script — required fail-closed structure

Recommended template:

```python
from pathlib import Path

path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

old = """<exact old multiline anchor>
"""
new = """<exact replacement multiline text>
"""

count = text.count(old)
if count != 1:
    raise SystemExit(f"Expected exactly one patch anchor, found {count}")

text = text.replace(old, new, 1)

if old in text:
    raise SystemExit("Original patch anchor remains after replacement")

# Add stable semantic postconditions here.
assert "<required new semantic token>" in text

path.write_text(text, encoding="utf-8", newline="\n")
```

For multiple independent edits, use separate `old_N`/`new_N` anchors and require **each** count to equal exactly 1 before applying any replacement. Prefer validating all counts first, then mutating, so the script cannot leave a half-applied multi-anchor patch.

### Never silently relax a failed anchor

If `count == 0`:

```text
likely causes:
- branch/head drift
- whitespace or indentation differs
- newline differs
- source signature changed
```

If `count > 1`:

```text
anchor is ambiguous
```

In either case:

> stop and re-audit the live file; do not switch to fuzzy matching, first-occurrence replacement, broad regex, or guessed line numbers.

## 10. One-shot workflow — required execution order

Recommended skeleton:

```yaml
name: <slice> one-shot large-file patch

on:
  push:
    branches:
      - <target-branch>
    paths:
      - .github/workflows/<slice>-one-shot.yml

permissions:
  contents: write

jobs:
  patch:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - name: Checkout exact branch
        uses: actions/checkout@v4
        with:
          ref: <target-branch>
          fetch-depth: 0

      - name: Verify bootstrap head and blobs
        shell: bash
        run: |
          set -euo pipefail
          test "$(git rev-parse HEAD)" = "$GITHUB_SHA"
          test "$(git ls-remote origin refs/heads/<target-branch> | cut -f1)" = "$GITHUB_SHA"
          test "$(git hash-object <large-file>)" = "<expected-large-file-blob>"
          test "$(git hash-object <test-file>)" = "<expected-test-blob>"

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Prove focused RED
        shell: bash
        run: |
          set -euo pipefail
          set +e
          ./gradlew <focused-test-command> 2>&1 | tee /tmp/red.log
          status=${PIPESTATUS[0]}
          set -e
          test "$status" -ne 0
          grep -F '<expected failing test name or assertion marker>' /tmp/red.log

      - name: Apply exact production patch
        run: python3 .github/scripts/<slice>_patch.py

      - name: Run focused GREEN
        run: ./gradlew <focused-test-command>

      - name: Run checkpoint suite when required
        run: ./gradlew :app:testFast --rerun-tasks --no-daemon

      - name: Exact diff audit
        shell: bash
        run: |
          set -euo pipefail
          git diff --check
          test "$(git diff --name-only | paste -sd ' ' -)" = "<large-file>"
          git diff -- <large-file>

      - name: Reconfirm remote head
        shell: bash
        run: |
          set -euo pipefail
          test "$(git ls-remote origin refs/heads/<target-branch> | cut -f1)" = "$GITHUB_SHA"

      - name: Commit and push production patch
        shell: bash
        run: |
          set -euo pipefail
          git config user.name 'github-actions[bot]'
          git config user.email '41898282+github-actions[bot]@users.noreply.github.com'
          git add <large-file>
          test "$(git diff --cached --name-only | paste -sd ' ' -)" = "<large-file>"
          git commit -m '<exact product commit message>'
          git push origin HEAD:<target-branch>
          echo "PRODUCT_SHA=$(git rev-parse HEAD)" >> "$GITHUB_ENV"

      - name: Remove one-shot files
        shell: bash
        run: |
          set -euo pipefail
          git rm .github/workflows/<slice>-one-shot.yml .github/scripts/<slice>_patch.py
          git commit -m 'chore(ci): remove <slice> one-shot workflow'
          git push origin HEAD:<target-branch>
          echo "CLEANUP_SHA=$(git rev-parse HEAD)" >> "$GITHUB_ENV"

      - name: Report checkpoints
        run: |
          echo "Product checkpoint: $PRODUCT_SHA"
          echo "Cleanup checkpoint: $CLEANUP_SHA"
```

For a refactor without RED, replace the RED step with the smallest existing baseline/characterization/compile evidence.

## 11. Diff and scope audit requirements

Before commit, require all applicable checks:

```bash
git diff --check
```

Then require an exact changed-file allowlist. For a one-file product patch:

```bash
test "$(git diff --name-only | paste -sd ' ' -)" = "app/src/main/java/.../Target.kt"
```

Add semantic assertions when useful. Examples:

```text
required new call exists after the ownership/commit anchor
removed legacy signature no longer exists
legacy behavior that must remain still exists
no temporary workflow/script is staged into the product commit
```

The production commit must stage only the intended production allowlist. The temporary workflow/script are removed in a separate cleanup commit.

## 12. Concurrency and stale-run protection

Updating the temporary workflow may trigger more than one historical run while iterating on the workflow itself.

Every run must be unable to push if it is no longer the live branch head.

The required protection is the remote-head recheck immediately before commit/push:

```bash
test "$(git ls-remote origin refs/heads/<target-branch> | cut -f1)" = "$GITHUB_SHA"
```

Therefore an older run may compile or test, but it must fail closed before modifying the branch after a newer connector commit has moved the head.

Also remember normal PR CI concurrency: pushing a newer commit can cancel a still-needed older acceptance run. Once a `[full-ci]` checkpoint is the required acceptance gate, do not push another commit until that gate result has been captured.

## 13. GitHub Actions bot-push behavior — do not trust cleanup head as the final gate

A workflow using `GITHUB_TOKEN` can push the product commit and cleanup commit, but follow-on normal PR workflows from those bot pushes may be suppressed or may appear as `action_required` / zero-job runs.

This has occurred repeatedly in the TBSP one-shot workflow pattern.

Therefore:

```text
one-shot workflow product commit
-> cleanup commit removes workflow/script
-> do NOT assume cleanup bot head has a normal CI/R2 gate
-> create a normal connector/user-authored checkpoint commit
-> let that commit trigger ordinary PR CI/R2
```

For a final/full acceptance checkpoint, the connector checkpoint commit message must include:

```text
[full-ci]
```

Then verify the selected jobs actually ran:

```text
Android :app:testFull + :app:assembleDebug
ASP contracts when selected
Real Clingo when selected
CI aggregate gate
R2
```

A docs-only final carrier after an already accepted T4 can be used only to obtain same-head lightweight CI/R2; it does not replace the prior T4 evidence.

## 14. Failure triage — interpret the failure before changing code

### Case A — workflow run fails with `jobs=[]`

Meaning:

```text
workflow syntax / YAML validation / startup failure
```

Action:

```text
do not edit production
inspect YAML quoting and indentation
prefer separate Python script
repair workflow and rerun
```

### Case B — bootstrap/blob verification fails

Meaning:

```text
branch moved or file content changed
```

Action:

```text
stop
re-query live HEAD/blob
re-audit patch anchors/tests
create a new locked workflow head
```

### Case C — expected RED unexpectedly passes

Meaning:

```text
the regression is already covered/fixed, or the test does not reproduce the claimed gap
```

Action:

```text
stop before production patch
reassess the test/bug assumption
```

### Case D — RED fails, but not at the expected test/assertion

Meaning:

```text
compile/environment/unrelated failure, not valid RED evidence
```

Action:

```text
stop
fix execution/evidence problem first
```

### Case E — Python anchor count is 0 or >1

Meaning:

```text
source drift or ambiguous patch
```

Action:

```text
stop
never broaden to fuzzy replacement automatically
```

### Case F — GREEN or broader tests fail

Meaning:

```text
production patch is not accepted
```

Action:

```text
do not commit/push product patch
inspect failure
repair only within the approved slice or stop/report
```

### Case G — exact diff contains extra files / mass line-ending churn

Meaning:

```text
patch implementation is unsafe
```

Action:

```text
do not commit
check newline handling, generated files, Gradle artifacts, and file allowlist
```

### Case H — bot cleanup head shows `action_required` or no normal jobs

Meaning:

```text
likely workflow-trigger/token behavior, not automatically a code failure
```

Action:

```text
verify one-shot run evidence
create normal connector checkpoint carrier
use its CI/R2 as the remote gate
```

## 15. When Path B should give up and use Luna

Do not use Luna merely because the first workflow file had a syntax mistake. Fixing the orchestration is still cheaper and safer when the production patch remains exact.

Escalate to Luna when at least one is true:

- the large-file edit cannot be expressed as stable unique anchors;
- the change spans many interdependent regions where textual replacement becomes fragile;
- declaration movement/extraction needs IDE/compiler-guided mechanical work across large files;
- the runner cannot access required local-only inputs/tooling;
- repeated failures show the GitHub runner environment itself cannot perform the required operation;
- a complete local worktree diff is necessary before the edit can be specified deterministically.

When falling back, Chat still supplies Luna with the exact branch/head, file allowlist, replacement intent, evidence commands, diff checks, commit message, push target, and stop conditions.

## 16. Cleanup checklist

Before declaring the slice complete, verify:

```text
[ ] intended regression/baseline evidence captured
[ ] exact large-file production diff audited
[ ] product commit contains only allowed product files
[ ] temporary .github/workflows/<slice> file removed
[ ] temporary .github/scripts/<slice> file removed
[ ] cleanup commit pushed
[ ] normal connector/user checkpoint triggers PR CI/R2
[ ] T4/full gate run if required by testing strategy
[ ] current PR head independently re-queried
[ ] no merge/Ready transition without explicit user authorization
```

## 17. Short decision rule

Use this mnemonic:

```text
Can connector safely replace the whole file?
  YES -> connector direct edit
  NO  -> can a complete GitHub checkout apply an exact unique-anchor patch?
           YES -> one-shot workflow + separate Python script
           NO  -> Luna
```

For large files, prefer **exact remote execution over local delegation** whenever the patch can be made fail-closed.