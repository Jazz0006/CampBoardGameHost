# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Role: **SINGLE CURRENT PROJECT-STATUS / EXECUTION-SEQUENCE AUTHORITY**  
> Always re-query live GitHub state before implementation or merge.

## 1. Live development context

```text
live main at last verification:
6111ffe3863713895d2b21ab086cf31abcca4a4e

current branch:
codex/clue-ux-r2-manual-pair-selector

current Draft PR:
#63 — UX-R2: establish structured manual pair selection
DRAFT / OPEN / UNMERGED

last fully validated executable/code checkpoint on #63:
b014345afe2b003bad2a2ee23cb2cf809a2a4eb2

validation at that checkpoint:
CI #1373 — SUCCESS
R2 main-thread boundary #1288 — SUCCESS
```

Documentation-only commits may advance the PR head beyond the executable checkpoint above. Do not infer a new executable checkpoint from a docs-only head.

PR #63 remains Draft and must not be merged, marked ready, rebased, force-pushed, or otherwise broadened without explicit user authorization.

## 2. Completed foundation

The following campaigns are complete and no longer current execution work:

- MS-SETUP generic multi-script setup architecture, including TB production cutover and minimal NGJ/no-template architecture proof;
- PR #61 closeout and integration into `main`;
- pair-information display semantics authority for Washerwoman / Librarian / Investigator;
- `PairInformationLegalDomain` as the shared complete legal semantic domain for pair clues;
- final production zero-result legality gate: Librarian may show zero Outsiders, Investigator may not show zero Minions;
- pair recommender migration to consume `PairInformationLegalDomain` rather than owning clue legality;
- PR #62 integration into `main` through the validated tree represented by `6111ffe...`.

Historical MS-SETUP handoffs/checkpoints remain evidence only. They are not active execution instructions unless this roadmap explicitly reactivates them.

## 3. Frozen permanent information architecture

The permanent causal order is:

```text
actual game state
+ committed shown identity
+ perceived ability
+ interaction-scoped registration semantics
        ↓
complete legal semantic candidate domain
        ↓
reliability state: RELIABLE / DRUNK / POISONED
        ↓
shared information-decision authority
        ├── manual selection
        └── recommendation provider
        ↓
confirmed candidate identity + immutable decision snapshot
        ↓
AbilityObservation / durable player-visible history
        ↓
UI / review / epistemic replay
```

Permanent invariants:

- recommendation ranks legal candidates; it never defines the legal candidate set;
- manual selection remains available independently of recommendation coverage, confidence, style, or rollout state;
- manual and recommendation acceptance must converge on the same stable candidate identity and confirmation authority;
- registration is interaction-scoped semantic evidence, not a permanent mutation of Spy/Recluse identity;
- RELIABLE pair clues expose only currently truthful legal outcomes, including legal registration truth;
- DRUNK/POISONED pair clues expose the complete legal display space of the perceived ability;
- UI/presentation code must not recreate role legality rules already owned by semantic/domain layers;
- stale decision contexts must not be confirmable after relevant game/player-input revision changes.

## 4. Pair-information semantic contract

Current pair-role authority is:

```text
PairInformationDisplaySemantics
        ↓
PairInformationLegalDomain
        ├── RELIABLE     -> truthful legal outcomes only
        └── DRUNK/POISONED -> complete legal display space
```

High-value product scenarios that must remain explicitly protected:

### Washerwoman

- functioning, no Spy registration path: only truthful Townsfolk clues;
- functioning with Spy: legal Spy-as-Townsfolk registration truths may also be selected;
- Drunk/Poisoned: every Townsfolk on the current script × every legal unordered player pair excluding the source;
- never a zero-character result.

### Librarian

- functioning with actual Outsider(s): truthful Outsider clues, including legal Spy registration truth where applicable;
- functioning with zero Outsiders and no Spy registration path: exactly `No Outsiders`;
- functioning with zero actual Outsiders plus Spy: `No Outsiders` and any legal Spy-as-Outsider registered truths may coexist;
- Drunk/Poisoned: every Outsider on the current script × every legal unordered pair, plus `No Outsiders`.

### Investigator

- functioning: actual Minion truth plus legal Recluse-as-Minion registration truth;
- Drunk/Poisoned: every Minion on the current script × every legal unordered pair;
- never a `No Minions` result.

These rules belong to semantic/domain tests, not UI string logic.

## 5. Current #63 scope — UX-R2 foundation

PR #63 is a **foundation PR**, not the full product UI cutover.

Already implemented at validated checkpoint `b014345...`:

- `PairInformationManualSelection` provides a structured role/pair view over supplied `PairInformationLegalCandidate` values;
- every supplied legal candidate is preserved exactly once;
- seat order is normalized for manual resolution;
- exact candidate identity and interaction-scoped registration facts are preserved;
- zero-result is exposed only when supplied by the upstream legal domain;
- `ClocktowerNightStepUi` and `ClocktowerInformationStepBuilder` can transport a precomputed typed pair manual model without taking legality ownership.

Before #63 closeout, perform only the following architecture convergence:

### UX-R2A — pair scenario contract corpus

Add durable typed regression tests for the Washerwoman/Librarian/Investigator scenarios in section 4, including Spy/Recluse exceptions and zero-result behavior.

### UX-R2B — adopt the shared InformationDecision Foundation

Pair selection must converge with the existing information-decision lifecycle already used by structured numeric information:

```text
legal pair candidates
-> shared InformationDecisionContext-style authority
-> structured pair manual projection
-> candidateId selection
-> confirm(MANUAL or RECOMMENDATION_ACCEPTED)
-> ConfirmedInformationDecision
```

Do not build a parallel pair-only confirmation lifecycle.

The current `InformationDecisionContext<T>` may be generalized narrowly if required so `PairInformationOutcome` can participate without duplicating it as an artificial `DynamicInformationOutcome.Pair` merely for transport.

Existing numeric behavior must remain unchanged by this generalization.

### UX-R2 documentation closeout

- keep this roadmap, the clue UX decision, Productive Uncertainty plan, root `AGENTS.md`, and the active handoff mutually consistent;
- #63 must remain a small typed/domain/session foundation;
- do not add the full 322 KiB Host/UI production wiring to #63.

## 6. Refined implementation route after #63

The earlier linear `UX-R2 -> UX-R3 -> UX-R4 -> UX-R5` route is refined to avoid temporary compatibility architecture.

```text
UX-R2A  pair semantic scenario contracts
UX-R2B  pair adoption of shared InformationDecision Foundation
        -> close / merge #63 after explicit authorization

UX-R2C  separate production vertical slice
        GameState + role definitions + source seat + reliability
        -> PairInformationLegalDomain
        -> shared decision context
        -> structured manual pair picker
        -> exact confirm / durable observation

UX-R2D  manual-authority coverage audit across major clue families
        -> Number
        -> Yes/No
        -> role/category/reveal families as currently supported
        -> fill only real authority/functionality gaps

UX-R3/R4  remove normal global mode + establish provider-neutral recommendation shell
        -> recommendations always available when provider supports the interaction
        -> prominent primary recommendation
        -> 0–2 useful alternatives
        -> persistent manual control

UX-R5  thin family-specific presentation polish
        -> small numeric domain: primary + every remaining legal value
        -> Yes/No: recommended value + other legal value
        -> combinatorial pair domain: structured manual navigation, never giant flat button lists

EPI-MQ / ALG mainline
        -> scenario corpus
        -> PlayerWorldSet BEFORE/AFTER hypothetical observation seam
        -> hard credibility / contradiction / fairness gates
        -> persistence / breakability / interaction metrics
        -> Productive Uncertainty ranking
        -> shadow provider comparison
        -> controlled rollout

UX-R6  replace legacy ranking behind the stable UI/decision contract
```

### Critical sequencing rule

Do **not** remove the global Automatic/Manual front-door control until every currently supported major information family has a correct manual authority path independent of recommendation coverage.

This prevents a temporary compatibility layer whose only purpose would be to keep un-migrated roles playable after the old Manual mode disappears.

## 7. Recommendation UX contract

The permanent product interaction is per clue, not a persistent global style mode:

```text
Recommended clue
[ primary ]

Other useful choices
[ alternative 1 ]
[ alternative 2 ]

-----------------------------
[ Manually choose clue ]
```

Rules:

- primary and alternatives must belong to the same current legal semantic domain as manual selection;
- normal UI shows at most two alternatives;
- zero alternatives is valid;
- do not invent a temporary legacy Top-3/diversification algorithm merely to fill UI slots;
- low-confidence/no-clear-winner must be representable without pretending certainty;
- Balanced/Aggressive/Conservative may remain temporary internal compatibility features, diagnostics, or tests, but are not permanent front-door product concepts;
- Manual is an interaction path, not a recommendation style.

## 8. Shared decision authority

`InformationDecisionContext` / `ConfirmedInformationDecision` already provide the correct lifecycle pattern for structured information choices:

- immutable semantic identity;
- game/player-input revision freshness;
- validated legal candidate IDs;
- recommendation candidate subset;
- manual vs recommendation provenance;
- stale-context rejection;
- illegal candidate rejection;
- exact decision snapshot carried into confirmation.

New pair work should reuse or narrowly generalize this Foundation rather than reproduce equivalent validation in Host/UI code.

The architectural target is:

```text
CompleteLegalInformationDomain<T>
        ↓
InformationDecisionContext<T>
        ├── recommendation projection
        └── structured manual projection
                 ↓
             candidateId
                 ↓
             confirm(...)
```

## 9. Productive Uncertainty / cognitive-consistency mainline

After the stable manual/decision/recommendation UI boundary exists, algorithm work becomes the primary campaign.

Correctness seam:

```text
legal semantic candidate
-> recipient PlayerWorldSet BEFORE
-> hypothetical player-visible observation
-> recipient PlayerWorldSet AFTER
-> epistemic metrics
```

Quality seam:

```text
candidate
-> credibility
-> immediate contradiction / self-exposure gates
-> mistaken-world persistence
-> breakability / discovery paths
-> cross-role interaction
-> confirmation-lock risk
-> faction impact / player agency
-> Productive Uncertainty tier / ranking
```

Start with a concrete Trouble Brewing scenario corpus rather than fixed numeric weights. Prefer explainable gates, feature vectors, comparative expectations, and Pareto/tier ranking before freezing a weighted sum.

For pair misinformation, early corpus examples should include:

- real shown role + shifted pair;
- absent but bluff/configuration-relevant role;
- Spy/Recluse registration explanation;
- Outsider-count tension;
- plausible but breakable trust/suspicion chains;
- obviously disconnected misinformation;
- excessive confirmation locks.

Drunk and Poisoned share semantic role rules; strategic ranking may later use different persistence/breakability weighting.

## 10. Exact-world baseline and scalability

A3 exact enumeration remains the correctness oracle for early/small states.

A4/ZDD remains shadow/prototype until equivalence and resource behavior are separately proven.

Never interpret:

```text
timeout
resource exhaustion
unknown/degraded evaluation
```

as false `UNSAT`.

The world backend may be replaced later without changing semantic candidate legality, decision confirmation, or the public UX contract.

## 11. Testing strategy

Authority: `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Use risk-based tests-first, not mechanical RED creation for every wiring edit.

High-value current contracts:

- pair reliable/impaired candidate-domain scenarios from section 4;
- exact Spy/Recluse registration identity survives manual selection and confirmation;
- manual cannot confirm a candidate outside the legal domain;
- recommendation acceptance cannot confirm a non-recommended candidate;
- stale revisions block confirmation;
- Number behavior remains unchanged if the generic decision Foundation is widened for pairs;
- production pair UI receives a precomputed legal/decision model rather than recreating legality;
- every supported clue family remains manually playable with recommendation absent;
- removing global mode later does not change clue legality/truth.

Avoid source-shape tests that merely assert helper/class/button placement.

Full CI is for logical acceptance checkpoints or risk-triggered cases according to the testing strategy.

## 12. PR / campaign boundaries

Recommended PR granularity:

| PR / slice | Scope | Stop condition |
|---|---|---|
| #63 | Pair scenario contracts + shared decision Foundation adoption + structured pair model | no full Host UI |
| next pair vertical PR | production pair wiring + structured picker + exact confirm | complete Washerwoman/Librarian/Investigator vertical path |
| manual-authority audit PR | Number / Yes-No / other supported families | no supported clue family depends on global Manual for correctness |
| mode/shell PR | remove user global mode + provider-neutral primary/alternatives/manual shell | no new recommendation-quality algorithm |
| EPI-MQ series | cognitive-consistency / Productive Uncertainty | shadow before production cutover |
| UX-R6 | provider replacement | stable UI unchanged |

Do not combine Productive Uncertainty, A4/ZDD rollout, broad future-script work, Host decomposition, or unrelated persistence work into #63.

## 13. Documentation authority and cleanup policy

### Active authorities

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

### Foundational but not current sequence authority

`docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` remains the detailed Possible Worlds / epistemic foundation. Its old phase ordering and AUTO/ASSISTED front-door model are historical where they conflict with this roadmap or the approved clue UX decision.

### Historical evidence

Dated MS-SETUP closeout files, older `NEXT_DEVELOPMENT_HANDOFF_*` files, earlier architecture-hardening handoffs, and completed decomposition handoffs remain historical evidence. Do not execute them as current instructions unless this roadmap explicitly reactivates them.

The older `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` remains subordinate to `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` and root `AGENTS.md`.

Do not delete useful historical evidence merely to reduce document count. Remove or rewrite a historical file only when it makes a false current-authority claim that cannot be safely neutralized by the authority hierarchy above.

## 14. Immediate next action

On Draft PR #63:

1. add the focused pair semantic scenario contracts from section 4;
2. audit the exact generic constraints in `InformationDecisionContext` and `DynamicInformationOutcome`;
3. narrowly generalize the shared decision Foundation so pair outcomes can use the same confirmation/stale-snapshot lifecycle without duplicating pair semantics;
4. adapt `PairInformationManualSelection` to resolve a structured choice to `candidateId` and confirm through the shared context;
5. preserve existing structured Number behavior;
6. run focused evidence, then T1 / CI/R2 at the logical checkpoint;
7. exact diff / PR scope audit;
8. stop with #63 Draft and request explicit merge authorization.

Do not modify the large production Host/UI in this PR merely to prove transport wiring.

## 15. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read the active UX-R2 handoff named in section 13;
4. re-query live `main`, current PR head/state/checks and distinguish executable vs docs-only head;
5. continue only the current slice in section 14;
6. do not follow older handoffs as active instructions;
7. do not merge without explicit user authorization.
