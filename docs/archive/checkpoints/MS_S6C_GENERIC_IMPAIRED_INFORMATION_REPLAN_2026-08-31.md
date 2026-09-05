# MS-S6C — Generic Impaired Information Replan

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **DESIGN APPROVED / IMPLEMENTATION NOT ACCEPTED**

## 1. Decision

MS-S6C is redefined from a narrow “Drunk shown as Investigator recommendation ownership inversion” into a controlled **generic information-semantics / impairment-ownership migration**.

The frozen causal architecture remains:

```text
Composition
-> Identity
-> Information
```

MS-S5 owns actual-role composition only.  
MS-S6A owns legal shown-identity options only.  
MS-S6B commits shown identity deterministically.  
MS-S6C consumes that committed perceived identity and produces information only.

The new S6C target is:

```text
committed shown identity
-> perceived ability role
-> role-specific ability / display semantics
-> healthy information candidate space
-> RELIABLE / DRUNK / POISONED impairment semantics
-> generic truthful/false family policy
-> generic consequence/history ranking inside the selected family
-> deterministic information recommendation
```

Recommendation must never choose, replace, reroll or optimize shown identity.

## 2. Why S6C is being replanned

Audit of the current branch found that Drunk-as-Investigator is represented as a special recommendation concept across several layers rather than only as Investigator ability semantics.

Current legacy concepts include, directly or indirectly:

```text
StorytellerDecision.DrunkInvestigatorInfo
StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO
candidate family "drunk-investigator-info"
drunkInvestigatorShownMinion
clocktowerRecommendedDrunkInvestigatorRoleName
clocktowerRecommendedDrunkInvestigatorSeats
investigatorDisplaySuitability
Drunk-Investigator evil-hit / evil-avoid recommendation rules
```

This is the wrong long-term boundary.

Investigator may have **specialized ability semantics** because its information shape is “a Minion character plus two candidate players”. It must not have a specialized storyteller strategy merely because the recipient is Drunk or Poisoned.

The desired rule is:

> Investigator can have specialized semantics, but not specialized impairment strategy.

## 3. Preserve Investigator role semantics

Do not delete normal Investigator role behavior.

Investigator-specific code remains valid where it expresses official ability/display semantics such as:

```text
information shape = one Minion character + exactly two candidate players
healthy truth semantics = legal Investigator information under current registration rules
night/display type = pair / EitherOne presentation
```

Likewise Librarian, Washerwoman, Empath, Chef, Fortune Teller and future roles may each own their ability-specific information domain.

Role-specific semantics answer:

```text
What information can this ability display?
What counts as healthy/truthful information?
What registration interactions apply?
```

They must not answer:

```text
How aggressively should Drunk/Poisoned information mislead?
Should this role avoid pointing at real evil?
Should this role prefer a specific misleading character?
```

Those are generic impairment/recommendation concerns.

## 4. Shared Drunk / Poisoned impairment model

Official Drunk and Poisoned information semantics already support a unified model: the ability does not function, the Storyteller behaves as if it does, and displayed information may be false but is not required to be false.

The production direction is therefore:

```text
ability semantics
-> counterfactual healthy result / healthy candidate space
-> enumerate legal display outcomes
-> classify outcomes as truthful / false / partially misleading where applicable
-> InformationReliability.RELIABLE / DRUNK / POISONED
-> ImpairedInformationPolicy
-> within-family ranking
-> deterministic selection
```

For a Drunk:

```text
abilityRole = committed shownRole
```

For a Poisoned player:

```text
abilityRole = current / actual functioning-role identity
```

The same information-shape generator and truth classification should be reused where practical.

## 5. Retire the fixed 97% false / 3% truthful default

The current generic impaired-family policy uses a hard-coded default bias equivalent to:

```text
false    97%
truthful  3%
```

S6C must not preserve this as the long-term default.

Reasons:

- it is close to treating impairment as “almost always lie” rather than “information is unreliable”;
- it encourages a predictable meta where players can treat impaired information as near-inverted truth;
- it is a hidden magic-number policy rather than an explicit product tuning boundary;
- it makes role-special-case balancing more tempting because the global family split is too extreme.

### First-version target

Use an explicit generic product policy whose initial target is:

```text
false-family weight:      90%
truthful-family weight:   10%
```

The exact representation may differ, but the policy must be explicit/configurable rather than scattered numeric literals.

This 90/10 value is an approved **first-version target**, not an eternal game rule. Future simulation/gameplay evidence may justify 85/15, 95/5 or another value without changing the architecture.

### Style does not own truth-vs-false probability

`GENTLE / BALANCED / AGGRESSIVE` should primarily rank **how severe the misinformation is inside a legal false family**, not radically change whether information is truthful.

For example, if a healthy Empath result is `1`:

```text
truthful family: 1
false family: 0, 2
```

The generic impairment policy owns the truthful-vs-false family bias.

Recommendation style may then prefer a lower-pressure or higher-pressure false result, but should not become a second independent truth-probability system.

## 6. Existing infrastructure to reuse

S6C should migrate onto the repository's existing dynamic information infrastructure rather than create a parallel subsystem.

Relevant existing owners include:

```text
AbilityFunctioningSemantics
InformationReliability
ImpairedInformationPolicy
DynamicCandidateGenerator
MalfunctionPolicy
ConsequenceEvaluator
DecisionCandidate
EffectDraft.PlayerInformation
TruthRelation
WeightedStableSelector
```

Current `ImpairedInformationPolicy` already separates truth-family legality/budget from within-family ranking. Preserve that separation while changing the default bias and removing role-specific impairment heuristics.

Current `MalfunctionPolicy` already expresses useful generic severity/continuity concerns for numeric/categorical misinformation.

Current `ConsequenceEvaluator` already expresses generic risks such as repeated-target pressure, one-shot misinformation risk, high-impact misinformation, final-day risk and alignment advantage.

Prefer migration/reuse over introducing a new “Drunk setup misinformation engine”.

## 7. Investigator-special recommendation behavior to retire

S6C should remove or stop generating active behavior for Investigator-specific storyteller heuristics, including the current concepts equivalent to:

```text
real Evil in Drunk-Investigator candidate pair -> special penalty / quality downgrade
no real Evil in pair -> special beginner-safety reward
investigatorDisplaySuitability by shown Minion role
Drunk-Investigator-only candidate-family weighting
Drunk-Investigator-only effect/history similarity ownership
```

A legal Investigator misinformation result may still receive a lower generic consequence score if it causes broadly undesirable game consequences, but **not merely because it is Investigator information that points at actual Evil**.

Manual Storyteller selection remains available for experienced players who deliberately want a particular clue construction.

## 8. Legacy domain / persistence compatibility

Do not aggressively delete legacy schema types during S6C.

`StorytellerDecision.DrunkInvestigatorInfo`, its enum kind, old history fields or old saved-state fields may remain temporarily where required for:

```text
legacy decode
historical compatibility
exhaustive sealed handling
migration adapters
```

But the new production recommendation path should stop depending on them for:

```text
new candidate generation
new scoring
new history generation
new active UI recommendation state
```

A later schema-cleanup slice may physically remove obsolete compatibility types after usage reaches zero and persisted compatibility has been audited.

## 9. Effect / history direction

New information history should describe actual information effects rather than a Drunk-Investigator product concept.

Useful generic dimensions include:

```text
ability / perceived role
shown information character/value
candidate/target seats
truth relation
candidate alignment pattern
seat geometry / distance
misinformation pressure / consequence
```

Do not add a RED that asserts a DTO field name must disappear.

Instead preserve or add behavior evidence that repeated/equivalent information effects still participate correctly in history/diversity regardless of whether the recipient is Drunk or Poisoned.

## 10. UI/session recommendation cache direction

Current App state contains Drunk-Investigator-specific provisional recommendation fields.

The product requirement worth protecting is not the variable name or cache class. It is:

> when an ability-state dependency changes, stale information recommendation must not continue to be displayed as current.

For example:

```text
information recommendation generated
-> Poisoner target changes
-> affected player's reliability / ability state changes
-> information step is revisited
-> stale recommendation is invalidated / regenerated
```

S6C may generalize the directly affected recommendation state/cache, but must not perform a broad App-root rewrite.

## 11. Risk-based RED strategy

S6C uses **risk-based tests-first**, not ceremonial RED for every refactor step.

Before adding a test, ask:

> If the internals were substantially refactored later but product behavior stayed correct, should this test still remain valuable and pass?

If not, do not add it merely to force an implementation shape.

### Core behavior REDs

Keep the new RED set small, approximately 4–5 high-value scenarios.

#### RED A — committed identity ownership

```text
actual Drunk
shownRole = X
-> information recommendation cannot change/reroll X
```

This protects the S6B -> S6C ownership boundary.

#### RED B — shared impairment semantics

For the same perceived ability semantics, Drunk and Poisoned must both be capable of consuming the same legal information domain under `DRUNK` / `POISONED` reliability rather than requiring separate role-specific misinformation engines.

Do not assert they must call the same class/function.

#### RED C — no Investigator-specific Evil downgrade

Two otherwise-equivalent legal impaired Investigator outcomes must not be assigned a special lower quality tier solely because one pair contains actual Evil.

Do not assert exact scores or rule IDs.

#### RED D — non-Investigator Drunk path

Use a currently supported non-Investigator information role (for example Empath if the owning infrastructure supports it) and prove that a Drunk shown as that role receives information through that role's semantics without shown-role reroll or Investigator fallback.

Do not invent unsupported role algorithms solely to satisfy this test.

#### RED E — stale recommendation protection

After an ability-state dependency changes, a previously generated information recommendation must not remain visible/authoritative when it is no longer valid.

Test observable session/UI behavior or the smallest durable typed session boundary, not specific state-variable names.

### What not to RED

Do not add tests that merely assert:

```text
a class/type named DrunkInvestigatorInfo does not exist
a particular helper/generator must be called
a field must have a particular name
a source file must not contain a string
a cache must use a particular implementation type
falseWeight must equal an internal integer literal
truthWeight must equal an internal integer literal
```

The 90/10 policy should have appropriate policy-level/distribution evidence, but avoid brittle constant-shape tests. Deterministic simulation is preferable for calibration.

## 12. Implementation sequence

```text
S6C-0  live-state + full current legacy audit + capture current CI failure
S6C-1  establish only the high-value behavior REDs that expose real gaps
S6C-2  establish/reuse generic ability-semantics seam
S6C-3  unify Drunk/Poisoned impairment path; replace 97/3 with explicit 90/10 first-version policy
S6C-4  remove Investigator-specific recommendation heuristics
S6C-5  stop active production generation/ownership of DrunkInvestigatorInfo
S6C-6  migrate directly affected effect/history representation to generic information semantics
S6C-7  migrate directly affected provisional UI/session recommendation invalidation
S6C-8  audit healthy Investigator registration correctness, especially Recluse/Spy interactions
S6C-9  focused GREEN + triggered T1/T2/T3 + full acceptance validation + exact diff + checkpoint docs
STOP
```

S6C remains **IN PROGRESS** until the logical checkpoint passes the required validation. Do not mark it accepted merely because a subset of the migration compiles.

## 13. Investigator registration audit

The healthy Investigator information generator currently warrants a focused registration audit because Trouble Brewing contains registration interactions such as Recluse and Spy.

Question to resolve:

```text
Does healthy Investigator candidate generation correctly consume registration semantics,
or does it rely only on actual CharacterType.MINION?
```

If existing `RegistrationInteractionRules` already provide the required semantics, reuse them.

If a gap exists and it is necessary for the generic Investigator semantics to be correct, fix the smallest required boundary.

Do not expand S6C into a complete registration-subsystem rewrite.

## 14. Strict non-goals

Do not in S6C:

- change `SetupCandidate`;
- change S5 actual-composition selection;
- allow shown identity/history to feed back into S5;
- change S6A legal shown-role resolution;
- change S6B identity commitment or `setup-shown-identity-v1` namespace;
- let recommendation change committed setup identity;
- perform the TB 480-template production cutover — S7;
- perform NGJ/no-template production cutover — S8;
- perform broad persistence/recovery redesign;
- aggressively remove legacy persisted schema before compatibility audit;
- invent generic fake-information algorithms for unsupported future roles merely to make S6C appear complete;
- perform broad Host/App decomposition;
- rewrite the registration system beyond the minimum correctness seam required by S6C.

## 15. Validation / acceptance

Use root `AGENTS.md`, `docs/TESTING_STRATEGY.md` and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Evidence should follow risk, not commit count.

At minimum:

```text
focused owning RED/GREEN tests
:app:testFast at logical checkpoint
triggered T2/T3 recommendation/rules evidence
:app:testFull for S6C acceptance
:app:assembleDebug
ASP contract tests when triggered by the risk router/checkpoint
Real Clingo when triggered by the risk router/checkpoint
R2
GitHub CI aggregate gate
exact diff / changed-file / semantic audit
```

Do not start S7 until S6C has an accepted code/test checkpoint and checkpoint documentation.

## 16. Current checkpoint authority

At the time of this replan, the last accepted code/test checkpoint remains:

```text
MS-S6B
d4cf3969aabcea7433b96b5b320171fbc821853e
```

The current branch may contain later S6C work and may have failing CI. Those commits are **not** accepted S6C checkpoints unless later validation explicitly supersedes this statement.

PR #61 must remain Draft and unmerged unless the user explicitly authorizes otherwise.
