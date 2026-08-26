# Next Development Handoff — Same-Night Effective State Continuation

> Date: 2026-08-27  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable main at handoff: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`

## 1. Startup contract

Before changing code, read:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
8. `docs/TESTING_STRATEGY.md`.

Then re-query live `main`, branch head, PR #54 state/head, and latest checks. Do not rely on this SHA list if GitHub has advanced.

## 2. Last accepted production checkpoints

### SNE-6B2.6 — exact Dawn Demon successor materialization

```text
51179ecca667d5450550375735ca49aae932c06d
fix: materialize exact Demon successor at Dawn
```

Validation:

```text
focused GREEN
:app:testFast GREEN
git diff --check GREEN
R2 #675 SUCCESS
CI #748 SUCCESS
```

### SNE-6B2.5 A–D — current-role consumer migration

```text
5a94c63536c04382f59963843c2ac10544962b02
```

Validation:

```text
focused GREEN
:app:testFast GREEN
git diff --check GREEN
R2 #673 SUCCESS
CI #746 SUCCESS
```

Protected contracts from 6B2.5/6B2.6:

- effective current-role actor lookup;
- Poisoner effect ends when source loses Poisoner role;
- Fortune Teller sees both old dead Demon and new current Demon;
- Spy/Recluse registration follows current role but retains even-if-dead behavior;
- Imp successor exact confirmed fact is projected same-night;
- Dawn materialization uses exact confirmed successor only;
- no draft/fallback first-Minon materialization;
- unresolved mandatory succession blocks premature outcome/Dawn.

## 3. Reverted 6C direction

A RED for Mayor redirect killing the Demon and same-night Scarlet Woman succession was briefly created at:

```text
173eb86967ef8503cc02eef8d10a7e367edf4f9b
```

It was intentionally reverted before any 6C production implementation:

```text
0d165250a5bc6a9dd6cd4edfc5d216663e99263e
revert: defer generic non-self Demon succession
```

Do not resurrect that RED during the current Trouble Brewing closeout.

## 4. Product decision — Mayor cannot redirect to Demon

Current product restriction:

```text
Mayor redirect target cannot be the current Demon.
```

This is an intentional house-rule/product simplification, not official BotC semantics.

Generic non-self Demon death / Scarlet Woman succession remains deferred for future arbitrary dynamic/custom scripts. Examples include Assassin, Godfather, Gossip, Pit-Hag, Fang Gu and future night-death mechanics combined with Scarlet Woman.

See:

```text
docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md
```

## 5. Mayor Demon-exclusion checkpoint — production complete

Small-file rules/recommendation work:

```text
b864db41c2c1aa46728b45925312a7fd10322316
  initial Mayor source-wiring RED

30e00f28fd7b31f74e0b5aca5d8af2dac4aca8bd
  MayorRedirectLegality.kt

625ff5b03593860f920605bd9989281e50e6bb6b
  recommender excludes CharacterType.DEMON
  direct Demon resolveOutcome is rejected

36eb5b1165752b0bc714abe305f342a484eed412
  recommender Demon-exclusion tests
```

Source-test corrections after brittle anchors were discovered:

```text
b8c3d39eac8167725d83d272fb9c271a699dd9e8
2c50aa3f38a89baee7d42526f9162d178e0e91de
7fd5494b18852ffe984f1ed728639ddd8bf9257b
```

Final production Host/UI wiring:

```text
2e8cb6a6a4763f9926956e5407d1c465e112e2bd
fix: enforce Mayor Demon exclusion in host UI
```

Canonical remote audit of `7fd5494b... -> 2e8cb6a6...`:

```text
exactly 1 commit ahead
only 2 files changed:
  ClocktowerHostScreen.kt
  ClocktowerNightStepUi.kt
```

Behavior now implemented:

```text
MayorRedirectLegality
→ rules-owned mayorRedirectTargetCards
→ manual UI consumes all legal targets
→ recommender only ranks legal targets
→ restored/raw Demon redirect target fails closed before mechanical death resolution
```

The old manual legality dependency is removed:

```text
assistedDecisionOptions.map(ClocktowerDecisionOption::targetName)
```

Local checkpoint validation reported by Luna:

```text
focused Mayor wiring + recommender tests: BUILD SUCCESSFUL
:app:testFast --rerun-tasks: BUILD SUCCESSFUL
git diff --check: PASS
```

PR #54 was verified after the push as:

```text
open
draft
not merged
head = 2e8cb6a6a4763f9926956e5407d1c465e112e2bd
```

At the time of this documentation closeout, GitHub had not yet exposed workflow runs/statuses for the `2e8cb6a6...` production head. Re-query checks at the start of the next conversation; do not interpret “no run visible yet” as SUCCESS or FAILURE.

## 6. Documentation-only head after production checkpoint

After `2e8cb6a6...`, documentation-only commits may advance the branch head. Distinguish:

```text
last validated production code checkpoint:
2e8cb6a6a4763f9926956e5407d1c465e112e2bd

later branch head:
docs-only closeout commits may follow
```

Always verify the live lineage before using a SHA from this handoff.

## 7. Workflow corrections made today

New/updated authority:

```text
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md
AGENTS.md
```

Rules now explicitly require:

- connector owns safe small/medium edits;
- Luna only executes large/truncated/local-only file changes;
- every Luna instruction is one deterministic fenced block;
- expected JUnit failure is RED PASS when specified;
- no duplicate rerun of a Luna-passed focused test;
- T0 per micro-slice, broad tests at logical checkpoint;
- no old-head CI wait between micro-slices;
- recommendation never defines legality;
- source-wiring tests must be semantic and formatter-independent.

The Mayor closeout added a stronger source-test lesson: after a second failure at the same semantic boundary, stop refining chained positional `substringAfter(...)` anchors and replace them with position-independent semantic checks / whitespace-insensitive regex plus explicit forbidden-legacy assertions.

## 8. Immediate next work — SNE-7 final restore/recomposition matrix

Do not reopen 6C generic non-self Demon succession. The next development slice is **SNE-7 final restore/recomposition matrix**.

Required matrix includes at least:

1. restore before successor confirmation → target remains Minion;
2. restore after successor confirmation → effective current role is Demon;
3. confirmed successor + Previous → confirmed role change remains mechanically authoritative;
4. edit draft without Next → old confirmed successor remains authoritative;
5. upstream reconfirm invalidates stale successor;
6. Poisoner→Demon ends active poison while preserving raw confirmed target;
7. Fortune Teller targeting new Demon → Yes;
8. Fortune Teller targeting old dead Demon → Yes;
9. exactly one normal Demon action remains in canonical plan after succession;
10. Monk-protected Imp self-kill → no successor;
11. functioning Scarlet Woman at 5+ on Imp self-kill → mandatory Scarlet Woman;
12. poisoned/nonfunctioning Scarlet Woman at 5+ → ordinary living-Minion choice;
13. legacy draft-only successor data → no invented RoleChanged / reconfirm required;
14. Mayor restored/confirmed redirect target that is Demon → fail closed and does not kill Demon;
15. same persisted base + same confirmed checkpoint + same canonical plan + same cursor → identical `ClocktowerEffectiveNightState` after recomposition.

Prefer pure/projector/restore tests where possible. Do not add another source-string wiring test when behavior can be tested through a typed seam.

## 9. Stage-completion gate after SNE-7

After SNE-7 focused GREEN:

```text
:app:testFast --rerun-tasks
+ affected T2/T3 if TESTING_STRATEGY triggers them
+ latest production-head GitHub CI / R2
+ exact PR diff / architecture audit
```

Do not merge or mark PR #54 ready without explicit user authorization.

## 10. Do not do

- do not merge or mark PR #54 ready;
- do not resume App-root decomposition;
- do not broaden to arbitrary custom-script Demon death;
- do not let recommendations define Mayor legality;
- do not reintroduce early public role mutation;
- do not run broad CI/test gates after every micro-edit;
- do not ask Luna to redesign or audit the architecture;
- do not treat a docs-only branch head as the last validated code checkpoint.