# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6B COMPLETE / ACCEPTED — MS-S6C REPLANNED / NEXT**

## 1. Live / accepted checkpoints

Campaign baseline `main` at the last audit:

`eed51bade5163790316a31e8295e2e841df90357`

Current campaign branch:

`codex/ms-setup-generic-architecture`

Draft PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

At the last pre-doc audit the PR head was:

`11b18d1ea06fed20490fe1db07f6c189b7ffbe64`

That head contains incomplete S6C work and is **not an accepted S6C checkpoint**. Its CI #1267 / run `33367729757` failed in Android full validation; R2 #1184 succeeded.

A later docs-only replan commit may now be the branch head. Always re-query live `main`, PR head and checks before any production write.

Accepted code/test slice checkpoints:

```text
MS-S1   f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b
MS-S1R  2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e
MS-S2   d4001863f134ebbe7d26819f40ac34c7d1de200c
MS-S3   6b15822e75680fb8e718f5db24358e1a935b5523
MS-S4   6de0e8c99c89a091615c513255adbdb773b3cc69
MS-S5   86c3ce651025de9ccbe1094b161becc171514e69
MS-S6A  5823d66d0eb756a0005df86f1aea7db5902cae60
MS-S6B  d4cf3969aabcea7433b96b5b320171fbc821853e
```

**Last accepted code/test checkpoint remains MS-S6B `d4cf396...`.**

Authoritative S6C design/replan:

`docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`

Other predecessor authority:

```text
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md
docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md
```

## 2. Frozen architecture

The causal order remains:

```text
Composition
-> Identity
-> Information
```

Current target flow:

```text
script + playerCount + setupSeed
-> candidate legality/source                       [S2/S3/S4 COMPLETE]
-> actual-composition diversity selection          [S5 COMPLETE]
-> shown-identity policy/options                    [S6A COMPLETE]
-> deterministic shown-identity commitment          [S6B COMPLETE]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> perceived ability semantics                     [S6C]
-> RELIABLE / DRUNK / POISONED information policy [S6C]
-> recommendation produces information only        [S6C]
```

Governing ownership rule:

> Shown identity is a committed setup fact. Recommendation may consume it but may never choose, replace, reroll or optimize it.

## 3. S6C redesign decision

S6C is no longer a narrow “Drunk shown as Investigator” patch.

It is a controlled migration to:

```text
committed shown identity
-> perceived ability role
-> role-specific ability/display semantics
-> healthy information candidate space
-> InformationReliability.RELIABLE / DRUNK / POISONED
-> generic ImpairedInformationPolicy
-> generic consequence/history ranking inside the selected family
-> deterministic recommendation
```

### Core rule

A role may have specialized **ability semantics**, but not specialized impairment strategy.

For Investigator, preserve the official information shape:

```text
one Minion character
+ exactly two candidate players
+ healthy truth/registration semantics
```

Do **not** preserve a special “Drunk Investigator storyteller strategy”.

## 4. Investigator-specific legacy discovered in audit

The current code contains or recently contained several Drunk-Investigator-specific concepts:

```text
StorytellerDecision.DrunkInvestigatorInfo
StorytellerDecisionKind.DRUNK_INVESTIGATOR_INFO
candidate family "drunk-investigator-info"
drunkInvestigatorShownMinion
clocktowerRecommendedDrunkInvestigatorRoleName
clocktowerRecommendedDrunkInvestigatorSeats
investigatorDisplaySuitability
special real-Evil hit / avoid scoring for Drunk Investigator information
```

Treat these as migration targets, not as the desired long-term architecture.

### Preserve

Keep Investigator-specific code where it expresses:

```text
ability information shape
legal healthy information semantics
night/display presentation
registration semantics
```

### Retire / genericize

Stop active new production ownership where it expresses:

```text
Drunk-Investigator-only generation
Drunk-Investigator-only scoring
Drunk-Investigator-only history similarity
Drunk-Investigator-only provisional recommendation cache
```

Legacy domain/save/history types may temporarily remain for decode/compatibility. Do not perform destructive schema removal inside S6C.

## 5. Shared Drunk / Poisoned impairment policy

Drunk and Poisoned should reuse the same impairment layer wherever they expose the same perceived ability semantics.

For Drunk:

```text
abilityRole = committed shownRole
```

For Poisoned:

```text
abilityRole = current/actual role ability
```

Both should pass through the same reliability-oriented machinery rather than separate role-specific misinformation engines.

Prefer reuse of existing infrastructure:

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

Do not build a new parallel “setup Drunk misinformation engine”.

## 6. Retire 97% false / 3% truthful default

Current `ImpairedInformationPolicy` uses an extreme default family split equivalent to:

```text
false      97%
truthful    3%
```

S6C must replace this as the long-term default.

Approved first-version target:

```text
false-family weight      90%
truthful-family weight   10%
```

Requirements:

- represent the bias as an explicit generic product policy rather than hidden numeric literals;
- keep it easy to calibrate later through deterministic simulation/gameplay evidence;
- do not treat 90/10 as an official rule or immutable constant;
- do not create role-specific truth probabilities to compensate for a global policy.

`GENTLE / BALANCED / AGGRESSIVE` should mainly rank **severity inside the chosen false family**, not radically alter the truthful-vs-false family probability.

Example:

```text
healthy Empath result = 1
truthful family = 1
false family = 0, 2

ImpairedInformationPolicy chooses family bias.
Recommendation style ranks 0 vs 2 by pressure/consequence/history.
```

## 7. Investigator-specific scoring to remove

S6C should retire special recommendation behavior equivalent to:

```text
actual Evil appears in Drunk-Investigator pair -> special penalty / quality downgrade
no actual Evil appears -> special beginner-safety reward
shown Minion investigatorDisplaySuitability bonus
Drunk-Investigator-only family weighting
```

A legal Investigator outcome may still score differently because of **generic** consequence/history effects, but not merely because it is Investigator information pointing at actual Evil.

Experienced Storytellers remain free to make manual clue choices.

## 8. Risk-based RED strategy

Do not create a RED for every refactor step.

Before adding a test, apply the project rule:

> If internals are substantially refactored later but intended behavior remains correct, should this test still pass and remain valuable?

If not, do not add it as process ceremony.

### High-value S6C behavior REDs

Keep the new RED set to roughly 4–5 durable scenarios:

1. **Committed identity ownership**  
   `actual Drunk + shownRole X -> recommendation cannot change/reroll X`.

2. **Shared impairment semantics**  
   Drunk and Poisoned using the same perceived ability semantics can consume the same legal information domain under their respective reliability state; do not assert an internal class/function call.

3. **No Investigator-specific Evil downgrade**  
   otherwise-equivalent legal impaired Investigator outcomes are not forced into a lower quality tier solely because one pair contains actual Evil; do not assert exact scores/rule IDs.

4. **Non-Investigator Drunk path**  
   choose an already-supported information role such as Empath if appropriate; prove Drunk shown as that role receives role-compatible information without Investigator fallback or shown-role reroll.

5. **Stale recommendation protection**  
   after an ability-state dependency changes (for example Poisoner target changes), stale previously generated information must not remain current/visible.

### Do not RED implementation shape

Do not add tests merely asserting:

```text
DrunkInvestigatorInfo class is absent
a specific generator/helper must be called
a DTO field has a particular name
a source file no longer contains a string
a cache uses a particular implementation type
internal falseWeight literal == 900000
internal truthWeight literal == 100000
```

Use policy/distribution evidence and deterministic simulation for the 90/10 calibration rather than brittle source/constant-shape tests.

## 9. Implementation sequence

In the next conversation, proceed in this order:

```text
S6C-0  re-query live main / PR head / checks
        distinguish docs-only carrier from accepted S6B checkpoint
        capture exact CI #1267 Android failure
        complete current legacy Investigator/Drunk-Investigator audit

S6C-1  establish only the high-value behavior REDs that expose real product gaps

S6C-2  establish/reuse generic role ability-semantics seam

S6C-3  unify Drunk/Poisoned impairment path
        retire 97/3
        introduce explicit 90/10 first-version generic family policy

S6C-4  remove Investigator-specific recommendation heuristics

S6C-5  stop active production generation/ownership of DrunkInvestigatorInfo
        preserve legacy compatibility only where still required

S6C-6  genericize directly affected effect/history representation

S6C-7  genericize directly affected provisional recommendation invalidation
        protect observable stale-information behavior, not variable names

S6C-8  audit healthy Investigator registration correctness
        especially Recluse/Spy interactions
        fix only the smallest correctness seam if required

S6C-9  focused GREEN + triggered T1/T2/T3
        :app:testFull / :app:assembleDebug for acceptance
        CI/R2 + exact diff audit
        checkpoint docs
STOP
```

Do not start S7 until S6C is accepted.

## 10. Investigator registration correctness audit

`NaturalPairInformationCandidateGenerator` currently warrants explicit review because Investigator truth semantics must account for legal registration interactions, not only raw `actualType == MINION`, where relevant.

Audit:

```text
Healthy Investigator
+ Recluse / Spy registration interactions
-> is the truthful candidate space correct?
```

Reuse existing `RegistrationInteractionRules` if it already owns the needed semantics.

If a gap exists and blocks correct generic Investigator semantics, repair the minimum boundary.

Do not turn S6C into a complete registration-system rewrite.

## 11. Strict S6C non-goals

Do not in S6C:

- change `SetupCandidate`;
- rescore/regenerate S5 actual-role composition;
- let shown identity/history feed back into S5;
- change S6A shown-identity legality;
- change S6B identity commitment or namespace;
- allow recommendation to mutate committed identity;
- cut TB production flow — S7;
- cut NGJ/no-template production flow — S8;
- perform broad persistence/recovery redesign;
- aggressively delete legacy persisted schema;
- invent misinformation algorithms for unsupported future roles merely to complete coverage;
- perform broad App/Host decomposition;
- perform broad registration rewrite.

## 12. Protected predecessor invariants

Preserve:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality until explicit migration.

## 13. Validation / workflow

Follow:

```text
AGENTS.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

S6C is a recommendation-semantics checkpoint. Use risk-based evidence, not test-count ceremony.

At acceptance, expect at least:

```text
focused owning behavior tests
:app:testFast
triggered T2/T3 recommendation/rules evidence
:app:testFull
:app:assembleDebug
ASP contract / Real Clingo if triggered by current test strategy/risk router
R2
GitHub CI aggregate gate
exact changed-file / semantic diff audit
```

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.

## 14. Campaign sequence

```text
MS-S0    ownership audit                                         COMPLETE
MS-S0.5  recovery scope reduction audit                          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                   COMPLETE / ACCEPTED
MS-S1R   exact setup persistence authority migration             COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts                     COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                             COMPLETE / ACCEPTED
MS-S4    deterministic generated actual-role source              COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership correction                    COMPLETE / ACCEPTED
MS-S5    actual-composition diversity history/scorer/selector   COMPLETE / ACCEPTED
MS-S6A   shown-identity policy/options boundary                 COMPLETE / ACCEPTED
MS-S6B   deterministic shown-identity commitment               COMPLETE / ACCEPTED
MS-S6C   generic information semantics + impairment ownership   REPLANNED / NEXT
MS-S7    TB 480-template controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

## 15. Documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md
docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 16. New-conversation resume guard

At the next development turn:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. read `docs/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md`;
5. read S4.5, S5, S6A and S6B checkpoint docs;
6. re-query live `main`, Draft PR #61 head/state/checks;
7. distinguish docs-only carrier commits and incomplete S6C commits from accepted S6B checkpoint `d4cf3969aabcea7433b96b5b320171fbc821853e`;
8. inspect the exact current Android full-CI failure before changing production code;
9. follow S6C-0 -> S6C-9 above with only meaningful behavior REDs;
10. preserve the approved first-version 90/10 generic impairment bias direction unless new evidence is explicitly discussed with the user;
11. do not start S7/S8, persistence/recovery redesign or broad App/Host work;
12. keep PR #61 Draft and unmerged.
