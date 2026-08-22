# R6 Impaired Information + Storyteller Decision Design

> Date: 2026-08-22  
> Status: **CURRENT SPECIALIZED DESIGN**  
> Parent roadmap: `CURRENT_DEVELOPMENT_ROADMAP.md`  
> Purpose: define the next correctness/policy slice before PR #27, and the manual-storyteller information authority that follows PR #27.

## 1. Why this design exists

Recent real-game feedback exposed two distinct product needs:

1. Drunk/Poison mechanical effects must not function. This is a correctness rule and has already been centralized by PR #28.
2. Drunk/Poison information is currently too often truthful because balance/style logic has too much influence over whether misinformation is produced.
3. As the human Storyteller gains experience, automatic recommendation can no longer be treated as final authority. The Storyteller needs a first-class way to manually choose legal information while still receiving rule validation and warnings.

These are related, but they must be solved at different layers.

The design therefore separates:

```text
ability functioning
registration semantics
truthful result / legal result space
impairment information policy
storyteller decision
observation commit/history
```

## 2. Official-rules interpretation used by the product

Blood on the Clocktower does not define a fixed percentage such as “95% of poisoned information must be false”. Drunk/Poisoned players have no functioning ability, but the Storyteller continues to behave as though their ability works. For information roles, the Storyteller may provide false information and will usually want to do so, while occasional truthful information remains legal and can be useful to avoid making impairment obvious.

Product implication:

- mechanical effect semantics are deterministic correctness;
- information reliability is Storyteller policy within the legal rule space;
- the app may strongly prefer false information without pretending the official rules define a numeric probability.

## 3. Core semantic layers

### 3.1 Ability functioning

Existing central rule:

```text
HEALTHY
  → role ability may function according to role rules

DRUNK
  → player may experience/simulate the shown role
  → no true mechanical effect

POISONED
  → player may perform the apparent action
  → no true mechanical effect
```

For one-shot abilities, “attempt/use consumed” and “effect applies” are separate facts.

This layer must stay deterministic and must not consult game balance.

### 3.2 Registration projection

Spy/Recluse-like behavior belongs here.

```text
actual world
     ↓
registration projection
     ↓
truthful ability computation
```

Examples:

- Spy may register as good/Townsfolk/Outsider when a rule allows it.
- Recluse may register as evil/Minion/Demon when a rule allows it.

Registration is not misinformation probability and must not be implemented as a special case in the Drunk/Poison policy.

### 3.3 Legal information space

Before deciding what to show, the engine should derive:

```text
truthful result
legal false candidates
role-specific presentation constraints
```

Examples:

- Empath: legal values are constrained to the role's numerical result format.
- Undertaker: legal candidates are role identities allowed by the information format.
- Investigator: role + two-player pairing must remain structurally valid.
- Fortune Teller: yes/no information must still obey role-specific red-herring/registration semantics before impairment policy is applied.

The legal information space is a rules concern, not a recommendation concern.

### 3.4 Impairment information policy

Input conceptually includes:

```text
ability subject
role / information type
truthful result
legal false candidates
context needed to detect obvious impairment exposure
small explicit uncertainty allowance
```

Output conceptually includes:

```text
preferred reliability: TRUTHFUL / FALSE
eligible candidate set
reason / policy explanation
```

The important authority rule is:

```text
Drunk/Poison policy decides whether false information is strongly preferred.
Game balance does NOT primarily decide truthful-vs-false.
```

### 3.5 Storyteller decision

After the legal result space and impairment policy are known:

```text
InformationDecisionContext
    ├── recommended result(s)
    ├── legal manual alternatives
    ├── truthful result (Storyteller-visible only where appropriate)
    ├── warnings
    └── validation metadata
```

The human Storyteller then chooses:

```text
RECOMMENDATION_ACCEPTED
or
MANUAL
```

The confirmed decision then becomes an observation draft and is committed through session authority.

## 4. Impaired-information product contract

### 4.1 Healthy information roles

Default:

```text
healthy → truthful
```

False information is not allowed merely because balance would prefer it. Any exception must come from the role's real rules/registration semantics, not from recommendation style.

### 4.2 Drunk/Poisoned information roles

Default:

```text
legal false candidate exists
        ↓
strongly prefer false
```

The intended aggregate field behavior is approximately “almost always false”, roughly compatible with a 95%–99% false rate, but this is a target behavior/statistic, not the top-level rule.

Truthful information remains a legal exception when justified.

### 4.3 Allowed truthful exceptions

A truthful result may be selected when at least one explicit condition applies, for example:

1. no legal false candidate exists;
2. every false candidate would make impairment nearly obvious or create an implausible/impossible presentation;
3. role-specific official semantics constrain the result;
4. a small deliberate uncertainty allowance is intentionally used to prevent “impaired always means false” from becoming mechanically solvable.

Any such exception should be explainable in debug/history metadata even if not shown to players.

### 4.4 Balance/style boundary

`GameBalanceEvaluator` and information style may affect:

- which legal false candidate is selected;
- how disruptive/aggressive the false result is;
- whether a candidate is too revealing or too destructive;
- tie-breaking among legal alternatives.

They should not normally affect:

```text
poisoned/drunk → false vs truthful
```

This is the main architectural correction motivated by field feedback.

## 5. Why not implement a single `95%` random switch

A naive rule:

```kotlin
if (poisoned && random < 0.95) false else true
```

would be simple but creates several long-term problems:

- it cannot explain why true information was chosen;
- it may choose impossible/role-invalid false outputs;
- it ignores whether a false result would reveal impairment;
- it lets RNG replace Storyteller judgment;
- it makes future manual override awkward;
- it becomes difficult to generalize across different information shapes.

Preferred architecture:

```text
legal result generation
     ↓
semantic preference
     ↓
policy-ranked candidates
     ↓
storyteller decision
```

Numeric randomness, if retained at all, belongs only as a small lower-level policy knob.

## 6. Tests-first plan for the next focused PR

This PR occurs **before resuming #27**.

### 6.1 Minimum RED contracts

Add executable tests proving current behavior violates the desired boundary where applicable.

At minimum:

1. healthy Empath/information subject resolves truthful;
2. poisoned Empath with legal false alternative strongly prefers false;
3. Drunk shown as an information role strongly prefers a legal false result;
4. poisoned structured information (for example Undertaker/Investigator) preserves output format;
5. large evil advantage/disadvantage cannot directly flip an impaired false preference back to truthful;
6. no legal false candidate allows truthful fallback;
7. an explicit “avoid exposing impairment” condition allows truthful fallback;
8. registration behavior is not implemented by impairment policy;
9. deterministic seeded behavior remains reproducible if a small uncertainty randomization is used.

### 6.2 Implementation shape

Prefer a small pure semantic seam rather than patching each role separately.

Possible naming (non-binding):

```text
ImpairedInformationPolicy
InformationReliabilityDecision
InformationCandidateSet
```

Do not introduce UI in this PR.

Do not introduce Global producer cutover in this PR.

Do not touch Spy/Recluse registration except where tests prove separation.

### 6.3 Exit criteria

- pure semantic tests green;
- affected production recommendation path consumes the new decision seam or is proven ready to do so;
- no role-specific duplicated “poisoned = maybe lie” random checks remain in the changed scope;
- `GameBalanceEvaluator` no longer owns truthful-vs-false for impaired information;
- full Android / R2 / ASP / Clingo CI green;
- exact diff remains focused.

## 7. PR #27 relationship

PR #27 remains the next production-history ownership change, but it resumes only after the impaired-information semantics PR merges.

Required resume sequence:

```text
new main with impaired-information semantics
        ↓
integrate/rebase-or-merge safely into PR #27 branch
        ↓
revalidate existing EpistemicObservationDraft / session atomic contracts
        ↓
implement production Global observation wiring
```

PR #27 must not become responsible for deciding whether information is truthful. It only owns durable observation identity/commit authority.

## 8. Storyteller Information Decision Unification

This stage follows #27.

It replaces the narrower earlier idea “Recommendation Entry-Point Unification”.

### 8.1 Product objective

The app should support an experienced Storyteller who wants to make the final information choice manually without losing rules assistance.

Recommendation becomes a suggestion, not the final fact.

### 8.2 Unified flow

```text
Actual / registered game state
        ↓
Role-specific legal information builder
        ↓
Impairment policy
        ↓
InformationDecisionContext
   ├── recommended candidate
   └── manual legal candidates
        ↓
Storyteller chooses/confirms
        ↓
shared validator
        ↓
EpistemicObservationDraft
        ↓
ClocktowerGameSession commit
        ↓
semantic history
```

### 8.3 Manual mode requirements

Manual setting must be structured, not an unrestricted text box.

Examples:

- Empath: choose 0 / 1 / 2 from legal values.
- Investigator: choose a Minion identity plus two player targets using a structured selector.
- Undertaker: choose an allowed role identity.
- yes/no roles: choose one of the legal boolean presentations.

The UI may show Storyteller-only truth and warnings, but player-facing information must remain separate.

### 8.4 Hard block vs soft warning

Hard block:

- result violates official role format;
- healthy ability receives an impossible false result without a valid registration/rule exception;
- selection violates target count/type constraints;
- selection cannot be converted into a valid proposition/observation.

Soft warning:

- Drunk/Poisoned player is being given truthful information even though a strong false candidate exists;
- manual result is legal but dramatically different from the algorithm recommendation;
- result may reveal impairment or heavily distort balance.

The Storyteller may confirm through soft warnings but not hard blocks.

### 8.5 Recommendation and manual are peer inputs

Forbidden architecture:

```text
recommendation path = real semantic pipeline
manual button       = bypass / direct mutable-state write
```

Required architecture:

```text
RECOMMENDATION_ACCEPTED ─┐
                         ├─ shared validation → same draft → same commit authority
MANUAL ──────────────────┘
```

### 8.6 Decision provenance

Initial durable/debug provenance should remain intentionally small:

```text
MANUAL
RECOMMENDATION_ACCEPTED
```

Optional future values such as `AUTO_DEFAULT`, `MANUAL_AFTER_WARNING`, etc. should not be introduced until field data proves they are useful.

## 9. Future learning from Storyteller decisions

The application should not learn/modify policy automatically yet.

However, recording decision provenance enables later offline analysis such as:

```text
how often recommendations are accepted
which roles are manually overridden most often
false-vs-true rate under Drunk/Poison
which information style leads to frequent human correction
```

This creates a future path toward personalized recommendation style without making ML a current dependency.

## 10. Multi-script implications

Manual legal information is a strategic capability for expanding beyond Trouble Brewing.

Support levels:

```text
LEVEL 1 — Flow supported
LEVEL 2 — Manual legal information supported
LEVEL 3 — Automatic recommendation supported
LEVEL 4 — Advanced balance-aware recommendation supported
```

This avoids blocking a new script until sophisticated recommendation logic exists.

A new script can become practically usable at Level 2 if:

- its flow is correct;
- role action eligibility is correct;
- legal information choices can be constructed and validated;
- the human Storyteller can choose the result.

Automatic recommendation can be layered later.

## 11. History/audit requirements

Once #27 Global ownership is live, every confirmed information decision should be able to record enough semantic metadata to answer:

```text
who received information
what role/ability generated the interaction
what information/proposition was shown
whether the subject was healthy/drunk/poisoned
whether the decision was manual or recommendation-accepted
where it sits in the global timeline
```

Do not expose hidden truth to player-facing history.

Storyteller/debug history may keep richer provenance according to future privacy/UI design.

## 12. Non-goals

The following are explicitly outside the next impaired-information PR:

- PR #27 production Global wiring;
- manual information UI;
- history UI redesign;
- Spy/Recluse registration rewrite;
- Investigator small-game balance tuning;
- generalized evil-side win-rate tuning;
- A3/B4 historical engine expansion;
- ZDD production promotion;
- ML/personalized learning.

The following are explicitly outside the later manual-decision stage unless separately authorized:

- arbitrary free-text player information;
- bypassing official role constraints;
- auto-learning from manual choices;
- allowing manual selection to mutate hidden world truth.

## 13. Guardrails

1. Rules legality before balance preference.
2. Ability functioning before recommendation style.
3. Registration before truthful-result computation where official rules require it.
4. Impairment policy before balance candidate ranking.
5. Recommendation never directly becomes durable truth without the unified confirmation path once the manual-decision stage is implemented.
6. Manual selection is an authority input, not a semantic bypass.
7. Global observation identity belongs to `ClocktowerGameSession`, not UI.
8. Player-knowledge-safe cores never receive storyteller-only truth merely to support recommendation/manual UI.
9. A fixed probability must not substitute for legal-result construction.
10. Field feedback can reprioritize policy work, but hard rules correctness remains highest priority.

## 14. Revised execution order

```text
A. Impaired Information Semantics                     NEXT
   tests-first pure policy/boundary correction

B. Resume PR #27 Global Observation Ownership
   integrate latest main and complete production wiring

C. Storyteller Information Decision Unification
   recommendation + manual setting through one authority

D. Historical Action + Observation Capture

E. A3 historical multi-night exact baseline

F. Authoritative physical Grimoire ledger / Spy VerifiedExact

G. B4 expansion

H. recommendation/history revision unification

I. reconsider ZDD production promotion
```

This ordering intentionally fixes “what information should exist” before changing “how that information becomes durable Global history”, and establishes durable history before adding the new manual Storyteller decision UI.
