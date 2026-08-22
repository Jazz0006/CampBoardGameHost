# NEXT DEVELOPMENT HANDOFF — 2026-08-22

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Specialized design: `docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`  
> Current next task: **Impaired Information Semantics**  
> Do not resume PR #27 until this focused slice is merged.

## 1. Current trusted baseline

At the time of this handoff:

```text
PR #28 Drunk/Poison ability correctness        MERGED
PR #24 Production Semantic-History Foundation  MERGED
main after #24                                 9c1996dfc6b615a12014fb11dbb5ca9a43064b99
repository visibility                          PUBLIC
GitHub Actions public runners                  VERIFIED GREEN
```

After the documentation commits in this handoff, always query live `main` before starting implementation; do not assume the SHA above is still HEAD.

Validated GitHub Actions after the repository became public:

```text
R2 main-thread boundary             PASS
Android tests + debug assemble      PASS
ASP contract tests                  PASS
Real Clingo cross-validation        PASS
```

## 2. Why the execution order changed

Field testing found that after the mechanical Drunk/Poison bug was fixed, poisoned/drunk information still feels too often truthful.

The existing balance/style path may have too much influence over whether an impaired information role receives true vs false information.

New priority:

```text
hard official semantics
        >>>
impairment information reliability
        >>>
game-balance preference
```

This must be corrected before PR #27 makes new observations durable through Global session authority.

## 3. Immediate objective

Create a focused tests-first PR for **Impaired Information Semantics**.

The PR should establish one centralized semantic decision seam where:

```text
healthy information role
    → truthful by default

Drunk/Poisoned information role
    → strongly prefer a legal false result

balance/style
    → may rank/select among legal false candidates
    → must not normally flip impaired false preference back to truthful
```

The product may aim for an aggregate 95%–99% false-information behavior for impaired roles, but must not implement that as a single top-level random switch.

## 4. Required RED contracts first

Before production implementation, add executable tests for at least:

1. healthy information subject resolves truthful;
2. poisoned Empath with a legal false result prefers false;
3. Drunk shown as an information role with a legal false result prefers false;
4. structured false information remains role-format legal;
5. large good/evil balance pressure cannot directly restore truthful output for an impaired subject;
6. no legal false candidate permits a truthful fallback;
7. explicit “avoid exposing impairment” reason permits truthful fallback;
8. Spy/Recluse-style registration is not delegated to impairment policy;
9. seeded behavior is deterministic if a small deliberate uncertainty randomizer is introduced.

Capture genuine RED before implementation.

## 5. Preferred production seam

Prefer a pure, testable semantic layer rather than per-role patches.

Possible names are illustrative only:

```text
ImpairedInformationPolicy
InformationReliabilityDecision
InformationCandidateSet
```

The seam should consume already-legal candidate information rather than own UI or persistence.

Conceptual flow:

```text
registration-aware role semantics
        ↓
truthful result + legal false candidates
        ↓
ImpairedInformationPolicy
        ↓
preferred candidate set / reliability reason
        ↓
existing recommendation/presentation adapter
```

## 6. Explicit non-goals for this PR

Do NOT implement:

- PR #27 Global observation producer cutover;
- `CampBoardGameHostApp` Global new-game switch except if a tiny compile-only adaptation is strictly required;
- manual Storyteller information UI;
- recommendation entry-point UI redesign;
- history UI redesign;
- Spy/Recluse registration rewrite;
- Investigator small-player balance tuning;
- broad evil-side win-rate tuning;
- A3/B4/ZDD authority changes;
- personalized/learning behavior.

If a failing test reveals a true official-rules correctness bug in the touched path, classify it separately before expanding scope.

## 7. GameBalanceEvaluator boundary

Audit the current information-style/balance path, especially any logic equivalent to:

```text
configured AGGRESSIVE/BALANCED/GENTLE
        ↓
evilAdvantage adjustment
        ↓
information truthfulness
```

The desired end state is NOT simply “multiply balance weight by 0.1”.

Desired authority:

```text
truthful vs false
    → impairment semantics

which legal false candidate
    → balance/style/disruption preference may participate
```

Preserve balance logic where it is useful; remove it from the wrong layer.

## 8. Registration boundary

Do not merge these concepts:

```text
Spy/Recluse registration semantics
Drunk/Poison impairment semantics
```

Correct ordering:

```text
actual world
    ↓
registration projection
    ↓
truthful result / legal result space
    ↓
impairment information policy
```

Tests should make this boundary explicit where practical.

## 9. Validation gate

For the final implementation head, require:

```text
focused new semantic tests            GREEN
existing AbilityFunctioning tests     GREEN
affected recommendation tests         GREEN
full Android unit tests               GREEN on GitHub Linux runner
assembleDebug                         GREEN
R2 main-thread boundary               GREEN
ASP contract tests                    GREEN
Real Clingo cross-validation          GREEN
exact diff --check                    GREEN
working tree                          CLEAN
```

Because the repository is now public, GitHub Actions must actually execute steps. `steps=null` is no longer an acceptable merge evidence substitute.

## 10. Merge/branch discipline

- Start from latest live `main`.
- Use a new focused branch/PR for impaired information semantics.
- Do not implement this directly inside PR #27.
- Keep PR #27 open/paused.
- After this focused PR merges, integrate updated `main` into PR #27 and re-audit its live head before continuing production Global wiring.

## 11. Following stage after PR #27

After PR #27 is merged, implement **Storyteller Information Decision Unification**.

This replaces the old narrow “Recommendation Entry-Point Unification” concept.

Required future model:

```text
legal information space
        ↓
InformationDecisionContext
   ├── automatic recommendation
   └── manual storyteller selection
        ↓
shared validation
        ↓
EpistemicObservationDraft
        ↓
ClocktowerGameSession
```

Manual information is a first-class product feature, not a bypass.

Initial decision provenance:

```text
MANUAL
RECOMMENDATION_ACCEPTED
```

No manual UI belongs in the immediate impaired-information PR.

## 12. Multi-script long-term capability levels

Use the following model for future script expansion:

```text
LEVEL 1  Flow supported
LEVEL 2  Manual legal information supported
LEVEL 3  Automatic recommendation supported
LEVEL 4  Advanced balance-aware recommendation supported
```

This allows a script to become practically playable with an experienced human Storyteller before sophisticated automatic recommendation exists.

## 13. Stop condition for the next work session

The next implementation session should stop after:

```text
focused impaired-information PR created
RED evidence captured
minimal centralized implementation completed
CI genuinely green
scope audited
PR ready for independent review
```

Do not automatically proceed into #27 in the same implementation slice unless explicitly authorized after review/merge.
