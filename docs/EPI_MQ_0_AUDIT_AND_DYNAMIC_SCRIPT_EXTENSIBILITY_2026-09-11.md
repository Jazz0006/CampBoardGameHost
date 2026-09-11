# EPI-MQ-0 Audit and Dynamic-Script Extensibility Decision

> Date: 2026-09-11 Australia/Sydney  
> Program: Epistemic Misinformation Quality / Productive Uncertainty  
> Audit baseline: live `main` at `42820353f5ea311f10b6e198ba34f6927edfd4ac`  
> Decision: **EPI-MQ direction remains valid; old EPI-MQ-1 implementation proposal is MODIFY**  
> Execution status: **PAUSED until UX-MODE-1 Beginner / Experienced Storyteller Mode is complete**

## 1. Why this document exists

The 2026-09-01 EPI-MQ plan correctly identified the product problem: legal Drunk/Poisoned misinformation should be ranked by the quality of the mistaken world it creates, not merely by surface distance from truth or ad-hoc misinformation pressure.

The live repository has changed materially since that plan. The EPI-MQ-0 re-audit found that several foundations proposed by the older plan already exist in stronger forms. The next implementation must reuse those current owners instead of creating parallel candidate, observation, or world-evaluation systems.

A second design discussion also established two roadmap decisions:

1. introduce an explicit Beginner / Experienced Storyteller experience mode before EPI-MQ implementation;
2. keep EPI-MQ generic enough to survive future custom/dynamic scripts, including roles with cross-night causal state such as Pukka and public delayed effects such as Moonchild.

## 2. EPI-MQ-0 result

### 2.1 Live ownership chain

The current live ownership is effectively:

```text
rules
  -> legal information shape / truthful semantics / registration semantics

recommendation
  -> legal typed candidates
  -> truth/false family
  -> current heuristic scoring, pressure, history and stable selection

session
  -> canonical game state
  -> revisions
  -> GLOBAL semantic timeline
  -> durable observation commit authority

epistemic
  -> recipient-visible knowledge
  -> exact possible worlds
  -> historical replay
  -> hidden-information isolation

UI
  -> presentation and explicit user confirmation
```

EPI-MQ must preserve this dependency direction.

### 2.2 Existing typed candidate-to-observation seam

`InformationDecisionContext<T>` already provides the important candidate-decision boundary:

```text
DecisionEvaluation
-> validated legal candidate IDs
-> immutable decision snapshot
-> explicit manual / recommendation acceptance
-> EpistemicObservationDraft
```

Manual input cannot invent an information value outside the validated candidate set. EPI-MQ should reuse this seam rather than introduce a second `InfoCandidateDTO` or a parallel manual/recommendation candidate model.

First-night pair-information migration still has some older `AbilityObservation` ownership. That path may need an adapter, but not a new legality system.

### 2.3 Existing unbound observation representation

`EpistemicObservationDraft` already represents a newly produced player-visible fact before durable timeline identity is assigned.

This is the correct bridge between recommendation/decision semantics and future hypothetical epistemic evaluation.

Do not confuse recommendation-side actual reliability (`RELIABLE`, `DRUNK`, `POISONED`) with recipient-visible `ObservationReliability`. A player who is secretly drunk or poisoned normally receives the information as functioning; the actual impairment must not be leaked into player knowledge.

### 2.4 Non-mutating session preflight already exists

`ClocktowerGameSession.preflightGlobalEpistemicObservation()` already calculates the would-be globally bound record, next observation log, timeline cursor and input revision without mutating the live session.

This is suitable for obtaining a correct hypothetical observation identity.

Important mutation/duplication hazard:

```text
current durable log
  -> build BEFORE world state

preflight(candidate)
  -> obtain correctly bound hypothetical observation

apply hypothetical observation exactly once
  -> AFTER world state
```

Do not build BEFORE from the preflight result's already-appended observation log and then apply the candidate again. That would consume the same observation twice.

### 2.5 Exact historical BEFORE -> AFTER evaluation already exists

`B4DynamicPlayerWorldSetShadow` already contains a production-isolated exact historical path that:

```text
recipient-visible setup/history
-> EnumeratedHistoricalExactBaseline
-> exact BEFORE worlds
-> hypothetical EpistemicObservation
-> exact AFTER cardinality
```

It intentionally returns cardinality/report data rather than exposing storyteller-hidden targets.

The existing B4 ownership fence explicitly prevents recommendation selectors from directly depending on this shadow service. Therefore EPI-MQ must not simply wire recommendation -> B4.

The correct architecture is to extract the already-proven hypothetical evaluation mechanics into a neutral epistemic owner, then make both B4 shadow and future EPI-MQ consumers of that owner.

### 2.6 Current correctness oracle

Until a later explicit representation cutover:

```text
Static/setup exact correctness:
TroubleBrewingWorldEnumerator
+ EnumeratedWorldSet

Historical/multi-night exact correctness:
EnumeratedHistoricalExactBaseline
+ EnumeratedHistoricalWorldReplay
```

A4/ZDD remains representation/shadow work and is not authorized as EPI-MQ's production correctness authority.

External ASP/Clingo remains cross-validation evidence under the existing authority order:

```text
OFFICIAL > PROJECT_GOLDEN > EXTERNAL_ORACLE
```

## 3. Hidden-information boundary

Future EPI-MQ evaluation must reason only from recipient-available knowledge.

Storyteller-hidden facts may exist in production state/history but must not become direct recipient constraints, including at least:

- actual hidden role assignment beyond recipient-visible knowledge;
- Poisoner/Pukka hidden target selections;
- hidden protection/attack targets;
- hidden role-transition causes before they become observable;
- red-herring identity except through Fortune Teller semantics;
- demon bluffs not known to the recipient;
- local Spy/Recluse registration choices outside the relevant interaction;
- recommendation seeds, scoring internals, or storyteller-only decision metadata.

Historical exact replay must regenerate hidden mechanics from rules and each possible world's state rather than copying the actual storyteller-selected hidden target into every possible world.

## 4. EPI-MQ-0 behavior corpus

Before any ranking weights are changed, the implementation corpus should cover comparative/invariant behavior rather than a large exact score table.

Minimum cases:

1. **Pair misinformation:** plausible false pair versus a legal-looking but immediately contradictory/disconnected pair.
2. **Equal numeric distance:** two false numeric results equally distant from truth but with materially different epistemic consequences.
3. **Fortune Teller temporal consistency:** repeated/overlapping checks must be evaluated against prior received information rather than scored independently each night.
4. **Drunk durable mistaken world:** false information can create a long-lived coherent mistaken world without revealing actual Drunk identity.
5. **Poison temporary impairment:** one-night false information may later become suspicious, but evaluation must not know the actual hidden poison target.
6. **Spy/Recluse explanation:** interaction-local registration can preserve a coherent explanation and must not become a permanent identity rewrite.
7. **Confirmation lock:** punish candidates that collapse the recipient's world set to an excessive or near-unique conclusion when another legal misinformation candidate preserves several coherent narratives.
8. **Immediate public contradiction:** candidate incompatible with already visible public facts should be a hard contradiction/defer/reject condition, not merely a slightly worse score.
9. **Same cardinality, different explanation structure:** equal AFTER cardinality does not imply equal quality if one candidate leaves only impairment explanations while another preserves normal/registration/impairment alternatives.

The ninth case is important: EPI-MQ-1 may begin with cardinality and SAT/UNSAT, but the API must remain extensible to richer explanation diagnostics.

## 5. Revised EPI-MQ implementation sequence

The old EPI-MQ-1 proposal is **MODIFY**, not GO unchanged and not NO-GO.

After UX-MODE-1 is complete, resume with:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
            - generic epistemic world-engine seam
            - explicit capability / deferred result
            - EPI-MQ must not depend on TroubleBrewing concrete classes

EPI-MQ-1    neutral hypothetical observation evaluator
            - exact
            - recipient-knowledge-safe
            - mutation-free
            - BEFORE / AFTER diagnostics
            - B4 shadow reuses the new neutral owner

EPI-MQ-2    credibility / immediate contradiction / impairment-exposure gates

EPI-MQ-3+   productive-uncertainty metrics and ranking
            only after earlier contracts are stable
```

No recommendation weights or production selections change during EPI-MQ-0.5 or the first evaluator extraction.

## 6. Dynamic/custom script extensibility decision

### 6.1 Product direction

Custom script composition should eventually allow a script such as:

```text
Trouble Brewing base
- Butler
+ Moonchild
+ Pukka
```

Game-execution support and epistemic support do not have to arrive in the same commit.

A role may be enabled for actual game flow/UI while advanced epistemic/EPI-MQ evaluation explicitly reports unsupported/deferred semantics until that role's knowledge model exists.

### 6.2 Capability coverage is mandatory

The future generic evaluator must not silently treat unknown mechanics as absent.

It should expose an explicit capability result, conceptually:

```text
READY
before = ...
after = ...
```

or:

```text
DEFERRED / UNSUPPORTED_SEMANTICS
missingCapabilities = [...]
```

Unknown semantics must never be converted into false `UNSAT` or an apparently exact world count.

This preserves the existing design principle that `DEFERRED != UNSAT`.

### 6.3 Why Moonchild and Pukka matter

Moonchild is a useful moderate-complexity extension because its public choice and delayed night consequence introduce time-sensitive public evidence and resolution-time impairment/alignment questions.

Pukka is a stronger architecture stress test because it combines:

```text
hidden nightly target
-> current poison state
-> potentially unreliable information
-> delayed death next night
-> recovery
-> next hidden poisoned target
```

This requires cross-night causal state in possible worlds. It demonstrates why the epistemic engine must evolve beyond a Trouble-Brewing-only static role/alive/ability-state model.

### 6.4 EPI-MQ dependency rule

EPI-MQ must depend on an abstract epistemic evaluation/world-set contract, not directly on classes named `TroubleBrewing...`.

Conceptually:

```text
EPI-MQ
  -> EpistemicWorldEngine / HypotheticalObservationEvaluator
      -> exact script/role semantics
```

The lower layer may evolve from Trouble Brewing exact modules to composable role/script semantics without forcing EPI-MQ scoring policy to be rewritten.

## 7. Role-module direction for future dynamic scripts

Do not solve future scripts by accumulating role conditionals in the large Host/UI file.

The intended ownership is composable role semantics, conceptually separating:

```text
role definition
mechanical rule / transition semantics
recipient-visible epistemic projection semantics
```

Exact file layout is not frozen here, but new role support should move toward this ownership rather than enlarging a central 200+ KiB file with role-specific mechanics.

File size remains a maintainability signal rather than an architecture by itself; extraction should follow real responsibility boundaries.

## 8. EPI-MQ architecture pre-flight for the next implementation phase

```text
Architecture pre-flight:
- current owners:
  - rules/recommendation: legal candidate and truth-family generation
  - session: durable game state, revision, GLOBAL timeline and commit
  - epistemic: recipient knowledge and exact possible-world reasoning
  - B4: shadow-only historical evaluation adapter

- proposed responsibility:
  - mutation-free evaluation of hypothetical recipient-visible observations
    against the exact current recipient world model

- authoritative state owners:
  - ClocktowerGameSession for durable game state/history
  - PlayerKnowledgeSnapshot / historical exact baseline for recipient-visible knowledge

- narrow typed input/output seam:
  - knowledge-safe historical context + recipient + hypothetical observation(s)
  -> READY/DEFERRED + BEFORE/AFTER diagnostics

- keep in current owner / extract:
  - extract exact hypothetical evaluation mechanics from B4-specific ownership
    into neutral epistemic ownership
  - keep persistence/session mutation in ClocktowerGameSession
  - keep candidate legality in rules/recommendation

- reason:
  - exact behavior already exists and is tested
  - recommendation -> B4 would violate current ownership
  - duplicating replay inside EPI-MQ would create competing semantic authorities
  - generic capability/deferred behavior is required for future dynamic scripts
```

## 9. Test direction when EPI-MQ resumes

The neutral evaluator is a durable architectural/semantic seam and should receive typed focused tests.

Minimum proof:

```text
same current history + candidate A -> deterministic exact BEFORE/AFTER
same history + candidate B -> different AFTER where expected
live session state remains unchanged
changing only a hidden poison target does not alter recipient result
hypothetical observation is consumed exactly once
unsupported role/script semantics -> DEFERRED, never fake UNSAT
```

Tests belong primarily under `clocktower/epistemic`, not source-string Host/UI tests.

At logical checkpoints follow `docs/TESTING_STRATEGY.md` escalation for epistemic/enumeration and historical timeline changes. Real Clingo remains an acceptance gate when exact/oracle semantics are changed, not for every mechanical extraction step.

## 10. Immediate roadmap consequence

EPI-MQ-0 audit is complete enough to make the architecture decision above, but EPI-MQ implementation is intentionally paused.

The next independent task is:

**UX-MODE-1 — Beginner / Experienced Storyteller Mode**

After UX-MODE-1 is complete and merged, return to EPI-MQ beginning with the dynamic-script extensibility guard and neutral hypothetical evaluator described above.
