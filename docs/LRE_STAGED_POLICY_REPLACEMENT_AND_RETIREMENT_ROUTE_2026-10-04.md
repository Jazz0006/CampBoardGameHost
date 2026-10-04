# LRE Staged Policy Replacement / Production Cutover / Legacy Deletion Route — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Status: **CURRENT EXECUTION AUTHORITY — LRE-1 COMPLETE / ACCEPTED; LRE-P RESUMED; INV1-A INVESTIGATOR SLICE CUT OVER / ACCEPTED**
>
> LRE-1 completion authority: `docs/LRE_1_MANUAL_SAFETY_GATE_COMPLETION_2026-10-04.md`
>
> Evidence/audit authority: `docs/LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md`
>
> Product-owner decision: improve and cut over new recommendation policies **family by family**, revoke unsupported legacy automatic authority early through Manual fallback, and delete the old recommendation engine only after production no longer depends on it.

## 1. Core sequencing decision

The project must not choose either of these unsafe extremes:

```text
A. keep old heuristics authoritative until every replacement policy is finished
B. delete the old engine bottom-up before replacement/manual authority is ready
```

The accepted route is:

```text
complete Manual / authority safety gate
    ↓
for each decision family:
    evidence / accepted policy semantics
    → versioned implementation
    → replay / shadow / evaluation
    → production cutover
    → remove old authority for that family
    ↓
when no production path depends on legacy heuristic authority:
    delete retired setup/dynamic/scoring/style infrastructure
    ↓
repository-wide no-legacy acceptance
```

This is **staged replacement plus staged cutover**, followed by **final physical deletion**.

## 2. Production authority invariant

At every decision boundary:

```text
exactly one legal outcome
    -> RULE_DETERMINISTIC
       automatic resolution permitted

multiple legal outcomes
+ accepted versioned policy for this exact scope
    -> POLICY_READY(policyVersion)
       recommendation / automatic selection permitted

multiple legal outcomes
+ no accepted versioned policy
    -> MANUAL_REQUIRED / POLICY_DEFERRED
       expose the complete legal domain
       never fall back to legacy heuristic selection
```

A recommendation provider being unavailable must never make a legal game action unavailable.

Beginner mode is allowed to require temporary Storyteller interaction when recommendation authority is missing. Preserving a zero-click Beginner UX is not more important than avoiding an untrusted automatic decision.

## 3. What is already production-ready

### 3.1 Drunk assignment

Production authority:

`DRUNK_ASSIGNMENT_Q04_V1`

Status: **CUT OVER / ACCEPTED**.

### 3.2 Functioning Librarian first-night information

Production authority:

`FunctioningLibrarianV2ProductionSelector`

Status: **CUT OVER / ACCEPTED** on its admitted scope.

The current generic legacy fallback around this selector is transitional only. Final LRE behavior is Manual/deferred if the new policy is unavailable or out of scope, not silent old-policy fallback.

### 3.3 Functioning Investigator first-night information — INV1-A

Production authority:

`FUNCTIONING_INVESTIGATOR_INV1_V1` via `FunctioningInvestigatorInv1ProductionSelector`

Status: **CUT OVER / ACCEPTED** on its exact admitted scope through PR #222, squash merge `420eef4e21769e3adf8928f187009f2afc1e5fa2`; executable head `89fb4a518260fb838f69a6d6b389869ab5f865f6` passed CI #3759 / R2 #3453.

The admitted predicate is intentionally narrow:

- Trouble Brewing / first night / functioning Investigator;
- exactly one actual Minion;
- exactly one actual Empath;
- the Empath is not poisoned in the canonical snapshot;
- that Empath is seated between the Demon and an actual Townsfolk;
- the truthful Investigator pair containing the Empath's Townsfolk neighbour and the sole real Minion is legal.

Inside that predicate, the versioned policy selects the evidence-backed pair that preserves Demon deniability while retaining a genuine Minion candidate. Outside it, the selector returns unavailable and LRE-1 keeps the multi-choice decision **Manual**. There is no fallback to the retired legacy ranking.

This is **not** a complete Investigator policy. INV1-B / INV1-C Recluse-based constructions remain separate conditional directions and require their own Host predicates before cutover.

## 4. Replacement waves

The order below is a priority, not a requirement to bundle families into one PR.

### Wave 1 — healthy first-night Storyteller information

Highest value because these decisions directly determine clue quality and already have strong Host foundations.

Families:

1. Washerwoman pair information;
2. Investigator pair information;
3. Red Herring selection;
4. Demon bluff triplet selection.

Existing reusable Host infrastructure includes:

- canonical TB Game State / snapshot;
- rules-owned legal candidate domains;
- first-night typed decision contexts;
- pair manual authority;
- exact hypothetical / epistemic / strategic evaluation;
- DecisionTrace / replay;
- HOST-ML neutral export;
- latest-safe commitment barriers for Red Herring / Demon bluffs.

Primary missing input is not another heuristic scoring function. It is source-backed candidate-comparison / preference / rejection evidence strong enough to authorize explicit versioned policy semantics.

For each family:

```text
EvidenceLab evidence gate
-> define smallest evidence-authorized policy predicate
-> implement versioned Host policy
-> replay / historical oracle
-> shadow or bounded production comparison when useful
-> cut over exact admitted production scope
-> disable legacy authority for that scope
```

Do not wait for the other Wave-1 families before cutting over one accepted family.

### Wave 2 — unreliable / Drunk / Poisoned information

Includes affected information surfaces such as Chef, Empath, Fortune Teller, Washerwoman, Librarian, Investigator, Undertaker, Ravenkeeper and any later TB information family whose malfunctioning ability allows several legal shown results.

This is a larger policy problem because good recommendations may depend on:

- current truth;
- previous information shown;
- misinformation continuity;
- whole-history world structure;
- evil public narrative / bluff support when available;
- player experience enrichment when evidence supports it;
- avoiding clues that are trivially useless or destructively strong.

The old 90/10 and distance/pressure score formulas are specifically **not** the target baseline.

EvidenceLab should prioritize comparative examples such as:

- truthful vs false in the same state;
- false candidate A vs false candidate B;
- why one error magnitude is preferred;
- why a prior false narrative should or should not be continued.

Production cutover occurs family/scope by family/scope; a universal misinformation V1 is not required.

### Wave 3 — lower-frequency / additional-context discretionary decisions

Families:

- Spy / Recluse special registration;
- Mayor death redirect;
- Demon succession.

Mayor and Demon succession already have relatively clean TBGS mechanical/legal contexts; their main blocker is policy evidence.

Special registration additionally needs a durable typed factual history producer before a history-sensitive replacement policy is trusted. Localized UI-title reconstruction must not become the new policy contract.

## 5. LRE implementation phases

### LRE-1 — Manual safety gate / legacy authority revocation

Status: **COMPLETE / ACCEPTED** at executable checkpoint `045b3a6884f765149d4f1802d5e369671d38d938`; CI #3752 / R2 #3448 GREEN.

Completion record: `docs/LRE_1_MANUAL_SAFETY_GATE_COMPLETION_2026-10-04.md`.

The previous product-owner pause after LRE-1 was explicitly lifted on 2026-10-04 after EvidenceLab completed the consolidated verified Host handoff. LRE-P is now active family-by-family. PR #222 is the first resumed LRE-P slice and proves the intended fail-closed pattern: only the evidence-authorized predicate receives new automatic authority; all other multi-choice states remain Manual.

Do this before waiting for all replacement policies.

#### LRE-1A — typed authority result

Introduce the smallest durable boundary that distinguishes conceptually:

- `RULE_DETERMINISTIC`;
- `POLICY_READY(policyVersion)`;
- `MANUAL_REQUIRED(reason)`.

Exact type names are not frozen.

#### LRE-1B — close Manual gaps

At minimum:

- legal-only Demon bluff selection independent of legacy `RecommendationPlan`;
- Beginner fallback for registration;
- Beginner fallback for Mayor / Demon successor;
- verify complete legal/manual information candidates when automatic recommendation is absent;
- ensure Red Herring remains selectable at the canonical latest-safe barrier.

#### LRE-1C — unsupported-family cutoff

After LRE-1A/B are green:

- unsupported multi-choice setup decisions must not use old setup ranking;
- generic unreliable information must not use old malfunction/dynamic heuristic as automatic authority;
- registration must not use temporary 90/10 authority;
- Mayor must not use temporary 10/90 authority;
- Demon successor must not use temporary 4/3/2/1 authority;
- new-policy selectors must not silently fall back to old ranking.

Result: old code may still exist, but it is no longer trusted automatic authority on unsupported surfaces.

Accepted LRE-1 additionally stopped the dead production setup-recommendation prewarm/reveal runtime path, so legacy setup plans are no longer computed by the live Host merely to populate an unused recommendation state.

### LRE-P — iterative replacement policy loop

This is not one giant implementation stage. It repeats for each family.

For every policy:

1. **Evidence gate** — EvidenceLab provides source-backed preference/rejection evidence or an explicitly bounded accepted contract.
2. **Legal-domain gate** — complete rules-owned legal alternatives exist.
3. **Context gate** — required inputs come from canonical/typed pre-decision state; optional enrichment is explicit.
4. **Policy version** — new semantics receive an explicit stable policy identity.
5. **Replay gate** — same frozen context reproduces the same result/reasons.
6. **Evaluation gate** — preserve all candidates and reasons needed for comparison/export; no future-history leakage.
7. **Production cutover** — only admitted scope switches to the new policy.
8. **Legacy authority removal** — old selector cannot regain authority if new policy is unavailable.
9. **Acceptance** — normal exact-head code gate for executable changes.

### LRE-2 — delete legacy setup ranking

Begin only after active setup-related production decisions no longer require old ranking authority.

Delete/narrow:

- `SetupEvaluator`;
- setup score/profile ranking;
- three-style plan generation/diversification;
- old score tolerance / weighted selection behavior;
- setup recommendation UI dependencies that no longer serve legal/manual control.

Preserve:

- rules/legal setup candidate generation;
- Red Herring legality;
- Demon bluff legality;
- exact SDE consequence evaluation.

### LRE-3 — delete legacy dynamic ranking

After each affected family has either a new accepted policy or Manual fallback authority, remove:

- `MalfunctionPolicy`;
- generic legacy registration ranking;
- legacy pair-information ranking;
- style/pressure weighting in `DynamicCandidateGenerator`;
- old impaired-information fixed family probability authority;
- legacy Mayor / Demon successor recommender scoring;
- `TemporaryAutomaticStorytellerPolicy`.

### LRE-4 — delete obsolete heuristic state / scoring inputs

Only after no accepted policy consumes them as authority:

- `GameBalanceEvaluator`;
- `ConsequenceEvaluator`;
- heuristic-only `evilAdvantage` / `PublicBalanceHint` consumers;
- heuristic information-pressure authority;
- role recommendation metadata;
- score tolerances;
- localized presentation-text history reconstruction.

Typed factual history can survive independently.

### LRE-5 — delete style / compatibility / telemetry shell

After no production family needs the old identity dimension:

- `RecommendationStyle`;
- `RecommendationProfiles`;
- `LegacyRecommendationStyleCompatibility`;
- style fields in request/result/key/telemetry models;
- style labels/default markers;
- style-keyed compatibility shadow maps;
- tests/simulations whose sole contract is retired style behavior.

### LRE-6 — final no-legacy acceptance

Prove repository-wide:

1. no production call path reaches legacy heuristic recommendation authority;
2. every automatic multi-choice Storyteller decision has an accepted explicit policy version;
3. every unsupported multi-choice decision remains playable via complete Manual legal domain;
4. legacy score/style/balance values cannot affect candidate selection;
5. accepted policy replay/export remains deterministic;
6. rules legality is unchanged;
7. Recovery semantics are unchanged unless separately authorized;
8. full T4 passes;
9. historical heuristic docs are archived and clearly non-authoritative.

## 6. Policy readiness is not the same as implementation readiness

For most remaining families, Host does **not** need another broad recommendation framework.

The project already has much of the reusable engineering foundation. The typical remaining sequence is:

```text
source-backed comparison evidence
    -> small explicit policy semantics
    -> Host policy implementation
    -> replay/evaluation
    -> production cutover
```

Therefore do not create arbitrary numeric ranking simply to make an implementation slice “ready”.

If evidence is insufficient, keep the family Manual and continue EvidenceLab collection.

## 7. EvidenceLab handoff priorities

Evidence collection should increasingly focus on **same-state alternatives**, not merely examples where a role appeared.

Highest-value evidence format:

```text
fixed pre-decision state
+ complete or reconstructable legal candidates
+ expert chose A
+ expert rejected/avoided B (explicitly or strongly inferable)
+ reason for the distinction
+ scope/limitations
```

Priority:

1. Washerwoman;
2. Investigator;
3. Demon bluffs;
4. Red Herring;
5. unreliable-information comparative decisions;
6. special registration;
7. Mayor redirect;
8. Demon succession.

This order may change when a strong qualifying evidence item appears.

## 8. Player experience integration

Future per-player:

`BEGINNER / NORMAL / EXPERT`

remains recommendation enrichment, current/default `NORMAL`.

Do not:

- map these levels to GENTLE/BALANCED/AGGRESSIVE;
- create three new hand-tuned global profiles;
- make player level affect legality;
- add player-level effects to a policy without evidence.

A policy may ignore player experience until evidence demonstrates a useful family-specific effect.

## 9. Documentation authority

Current authority order for this program:

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — current priority/status;
2. this document — staged replacement / cutover / deletion execution route;
3. `docs/LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md` — retirement evidence and inventory;
4. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` — long-lived SDE architecture;
5. historical RSR / TBGS post-2E audits under `docs/archive/`.

Where historical documents conflict with this route, this route governs execution.

## 10. Immediate next action

Start **LRE-1A / LRE-1B**.

The first safety milestone is:

> A multi-choice Storyteller decision without an accepted replacement policy remains playable, but can no longer be automatically chosen by the legacy heuristic engine.

After that milestone, run the LRE-P replacement loop continuously: mature one family, cut it over, then proceed to the next. Do not wait for all replacement policies before cutover, and do not physically delete shared legacy infrastructure until all remaining production dependencies have been removed.
