# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ACTIVE HANDOFF / PLANNING FIRST**

## 1. Live baseline at handoff creation

```text
main:
98ee982ef3590822cd06ac72a047b49afac3cfd6

merged PR:
#57 — TBSP: integrate Trouble Brewing setup presets
MERGED

merge commit:
98ee982ef3590822cd06ac72a047b49afac3cfd6

post-merge full CI:
CI #1179 / run 33346311357 — SUCCESS
Android :app:testFull + :app:assembleDebug — SUCCESS
ASP contract tests — SUCCESS
Real Clingo cross-validation — SUCCESS
CI aggregate gate — SUCCESS
```

Always re-query live GitHub state before implementation. These values are provenance, not a substitute for live state.

## 2. Closed predecessor campaign

Trouble Brewing Setup Presets is complete and merged.

Accepted TBSP checkpoints:

```text
production checkpoint:
4c8108c91be188d33435233efb9aba26397f6b87

final T4 trigger:
45a60a3c32c7471c68d89b7fb886c4dbb00f1781

merge commit:
98ee982ef3590822cd06ac72a047b49afac3cfd6
```

TBSP-1 through TBSP-6L are accepted. Preserve the frozen TB dataset, deterministic preset/deal semantics, selector-owned Drunk shown identity, exact provenance restore, true-completion rotation history, non-blocking reveal/First Night precompute, and the 6L durability invariant.

No Greater Joy remains protected behavior until genericization explicitly proves parity.

## 3. Current goal

Build a **generic Clocktower setup architecture** where every script can use the same setup pipeline regardless of whether curated setup templates exist.

Target contract:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup policy
-> query optional template candidate source
-> if templates exist: build validated template candidates
-> otherwise: build legal generated candidates
-> common deterministic diversity selector
-> commit exact setup including shown-identity decisions
-> persist generic exact setup provenance
```

The App root must not grow new `if (script == ...)` setup branches when future scripts are added.

## 4. Required semantics

- no-template is the default: a newly supported script must remain playable via legal deterministic seeded generation;
- curated templates are optional candidate sources, keyed by script/player count/version as appropriate;
- template-backed and generated candidates use the same common diversity/rotation selection layer;
- candidate identity is based on semantic role composition / shown-identity decisions, not seat reshuffling;
- generated candidates must contain no duplicate roles and must satisfy the script's legal team/setup modifiers;
- curated template candidates must be semantically validated before selection;
- Drunk-like shown identity is committed during setup generation, never lazily rerolled later;
- deterministic seed + committed provenance must reproduce/restore the exact initial setup;
- completion history must record the original committed setup, not a reconstruction;
- setup/ruleset modifiers belong to typed setup metadata/policy rather than App-root conditionals;
- adding a future script with no templates should not require setup-architecture changes;
- later adding templates for that script should require data/provider registration only, not App start rewiring.

## 5. Protected invariants

Preserve all accepted TBSP P1-P16 plus 6L durability, including:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown role is committed once and never replaced by recommendation.
Same seed + inputs reproduce the same committed setup.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore does not select a new setup.
Invalid template data does not silently fall back through a hidden broad-random path.
Identity reveal does not synchronously block on recommendation/First Night computation.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup.
```

Also preserve:

- Dawn poison exactly-once / retry convergence;
- Dusk/next-night poison expiry exactly-once / restore convergence;
- Fortune Teller current/effective-state authority;
- poisoned Spy fail-safe semantics;
- current living-Demon UI authority;
- No Greater Joy setup behavior until explicit parity proof.

## 6. Implementation campaign

Proposed slices remain:

```text
MS-S0  fresh live-state + ownership audit; freeze generic contracts before production changes
MS-S1  generic CommittedClocktowerSetup / provenance model
MS-S2  generic SetupCandidate + candidate-source contract
MS-S3  optional TemplateRepository keyed by script + player count
MS-S4  deterministic seeded GeneratedSetupCandidateSource
MS-S5  common cross-game SetupDiversityHistory / scorer / selector
MS-S6  generic shown-identity policy, including Drunk-style roles
MS-S7  adapt existing TB 480-preset pipeline to generic contract without behavior drift
MS-S8  adapt NGJ/no-template path to generated candidates and prove parity
MS-S9  acceptance: new no-template script requires no App-root setup branch; adding templates requires provider/data registration only
```

These are planning slices, not permission to implement all at once.

## 7. First action in the next conversation — MS-S0 only

Before writing production code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. re-query live `main` and confirm PR #57 remains merged;
5. inspect the current setup ownership chain, including TB and NGJ production paths;
6. identify exact owners for:
   - committed setup/provenance;
   - candidate representation;
   - template repository/provider;
   - generated legal candidate source;
   - common diversity history/scoring/selection;
   - shown-identity policy;
   - App/Host start wiring;
7. identify reusable TB components versus TB-specific components;
8. identify the current NGJ/no-template setup path and parity tests;
9. propose the smallest MS-S1 contract and its evidence strategy;
10. update this handoff/roadmap if the live audit changes the slice boundaries.

**Do not start MS-S1 production implementation until the MS-S0 ownership audit is complete.**

## 8. Development workflow

Follow root `AGENTS.md` and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

For large/truncated files, especially `CampBoardGameHostApp.kt`, use the current mandatory priority:

```text
small/medium safe file
-> GitHub connector direct edit

large/truncated file with stable unique anchors
-> GitHub Actions one-shot workflow + separate Python patch script

a one-shot patch cannot be made safe / complete local worktree genuinely required
-> Codex/Luna
```

Detailed large-file SOP:

`docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

Use risk-based evidence, not RED ceremony. Add a new RED only for a durable new/changed behavior or uncovered invariant.

## 9. Explicit non-goals for MS-S0 / early MS-S1

Do not broaden into:

- Mayor redirect behavior;
- Imp succession redesign;
- A3 immutable setup snapshot campaign;
- A4/ZDD;
- Host/App decomposition for its own sake;
- end-to-end Attack/Protect replay;
- unrelated same-night correctness work;
- regeneration/reformatting of the frozen TB preset dataset.

## 10. Stop conditions

Stop and re-audit before implementation if:

- live `main` moved materially;
- setup ownership differs from this handoff;
- TB or NGJ current behavior is not adequately characterized;
- a proposed generic contract would require changing accepted TB behavior merely to fit the abstraction;
- App-root branching would increase rather than decrease;
- generic provenance cannot preserve exact original setup identity across restore/completion.
