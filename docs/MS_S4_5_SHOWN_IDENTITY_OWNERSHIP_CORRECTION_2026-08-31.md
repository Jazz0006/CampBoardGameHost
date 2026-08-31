# MS-S4.5 — Shown-Identity Ownership Architecture Correction

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **COMPLETE / ACCEPTED — ARCHITECTURE / DOCS ONLY**

## 1. Why MS-S4.5 exists

MS-S1 through MS-S4 established the correct generic setup-domain foundations, but a fresh global audit before MS-S5 found one remaining legacy ownership inversion:

```text
legacy TB / setup recommendation
shown identity candidate
-> affects setup/preset scoring
-> can be emitted again as a recommendation decision
```

That ordering is wrong for the generic multi-script architecture. A player's shown identity is an initial setup fact, not a downstream recommendation output.

MS-S4.5 therefore freezes the corrected causal order before any MS-S5 production implementation begins:

```text
Composition
-> Identity
-> Information
```

This is a planning/ownership correction only. It does not invalidate or modify the accepted MS-S1, MS-S1R, MS-S2, MS-S3 or MS-S4 code/test checkpoints.

## 2. Live state at the correction checkpoint

Before the S4.5 documentation writes:

```text
main:
eed51bade5163790316a31e8295e2e841df90357

Draft PR #61:
OPEN / DRAFT / NOT MERGED
base = main
base SHA = eed51bade5163790316a31e8295e2e841df90357
branch = codex/ms-setup-generic-architecture
pre-S4.5 branch head = 77a30b12dae1e1484ed614f86d4c11ff81e8bbaf

accepted MS-S4 code/test checkpoint:
6de0e8c99c89a091615c513255adbdb773b3cc69
```

Later S4.5 documentation commits are carrier commits only and do not replace the accepted MS-S4 production checkpoint.

## 3. Frozen target data flow

The generic setup pipeline is now defined as:

```text
script + playerCount + setupSeed
        |
        v
resolve provider / legal candidate source
        |
        v
legal pre-seat actual-role SetupCandidate values
        |
        v
MS-S5 actual-composition diversity selector
        |
        v
one selected SetupCandidate
        |
        v
MS-S6 shown-identity policy/options resolution
        |
        v
seeded deterministic shown-identity commitment
        |
        v
seat/deal materialization
        |
        v
CommittedClocktowerSetup(actualRole + shownRole)
        |
        v
setup / first-night recommendation
        |
        v
consume committed shownRole as input fact
        |
        v
recommend information only
```

The governing rule is:

> Shown identity is a committed setup fact. Information recommendation may consume it, but may not select, replace, reroll or optimize it as a recommendation output.

## 4. Accepted predecessor contracts remain correct

### MS-S1 — no change

`CommittedClocktowerSetup` already stores exact `actualRole` and exact `shownRole` for every committed seat. That remains the final immutable initial-setup authority.

### MS-S1R — no change

Persistence/recovery already restores the exact committed setup directly. Restore must never rerun composition selection, shown-identity selection or recommendation to reconstruct identities.

### MS-S2 — no change

`SetupCandidate` remains a canonical **pre-seat actual-role multiset**. It must not acquire shown-role fields merely to carry template disguise metadata through the pipeline.

### MS-S3 — core contract unchanged

`TemplateRepository` remains responsible only for immutable template-backed actual-role candidate lookup. Shown-identity metadata must be exposed later through a separate companion policy/metadata boundary keyed by durable template provenance.

### MS-S4 — no change

`GeneratedSetupCandidateSource` remains responsible only for deterministic legal actual-role composition generation. It must not choose Drunk shown identity.

## 5. Global audit findings that caused the correction

### 5.1 TB templates already own useful identity metadata

`TroubleBrewingSetupPreset` contains `drunkAsOptions`.

The existing TB validator already protects important template-specific rules:

- a preset without Drunk cannot declare Drunk shown options;
- a preset with Drunk declares exactly three unique options;
- each option resolves to a TB Townsfolk;
- each option is absent from the preset's actual in-play role set.

These template validation semantics should be preserved.

### 5.2 Legacy TB setup scoring incorrectly lets shown identity affect composition

`TroubleBrewingSetupPresetRotationScorer.scoreFinalWeight(...)` currently accepts `selectedDrunkShownRole` and applies a strong repeated-shown-role multiplier (`0.40`) when the previous game used the same shown role.

That means legacy behavior can effectively do:

```text
candidate Drunk shown role
-> final preset weight
-> which actual-role preset is selected
```

This dependency must not be copied into MS-S5.

### 5.3 TB completion history may retain shown identity as historical data, but not composition authority

`TroubleBrewingSetupRotationRecord` contains `selectedDrunkShownRole`. The field may remain for persistence compatibility, audit or later optional shown-identity diversity work.

However, after migration it must not influence actual-role composition scoring.

Do not perform a persistence migration merely to delete this historical field during MS-SETUP.

### 5.4 Generic recommendation history has the same legacy coupling

`HistoricalClueSignature` currently contains `drunkShownRole`, and `HistoryCooldown` can apply a strong penalty to repeated Drunk shown roles.

Once shown identity becomes an input fact, recommendation history must not treat it as a recommendation-owned choice dimension.

The downstream recommendation history may continue to score information shape that remains genuinely selectable, for example shown Minion information, candidate-seat structure, red herring, demon bluffs and other clue effects.

### 5.5 Recommendation input already carries shown identity

`PlayerState` already contains `shownRole`.

`PlayerCard -> GameState` adaptation already copies `clocktowerShownRole` into `PlayerState.shownRole`.

Therefore the corrected recommendation architecture does not require a second parallel committed-identity context. The existing typed input can become the authority.

### 5.6 Current TB recommendation lock is a migration bridge, not the target design

`TroubleBrewingSetupRecommendationLock` currently converts an already selected/committed TB Drunk shown role back into `StorytellerDecision.DrunkShownRole` so the legacy setup recommendation generator can be constrained to the existing identity.

That bridge is evidence that the production flow already determines the identity before recommendation. After recommendation ownership inversion, the lock should become unnecessary and retire.

### 5.7 First-night information already has role-family infrastructure

The current first-night information migration includes role families for Washerwoman, Librarian, Investigator, Chef, Empath and Fortune Teller and reasons about the player's perceived role.

MS-S6 recommendation work must audit and reuse that role-information ownership rather than create a second complete set of fake-information algorithms inside setup recommendation.

## 6. Corrected TB parity definition

The old roadmap phrase `preserve TB behavior/parity` was too broad because exact old-seed preset parity can depend on the legacy shown-role-to-preset scoring coupling.

MS-S7 parity is now defined as follows.

### Must preserve

- the frozen 480-preset dataset;
- template legality and validation;
- player-count pools;
- actual-role composition facts;
- exact-repeat policy where still applicable;
- actual-role overlap / novelty semantics;
- Minion-set diversity semantics;
- style diversity semantics;
- Baron and TB composition legality;
- `drunkAsOptions` legal option metadata;
- deterministic deal/commit behavior after the corrected selector pipeline;
- true-completion gating and accepted durability behavior.

### Deliberately allowed to change

- a repeated Drunk shown role no longer changes the probability/weight of the actual-role preset;
- same legacy seed/history is therefore not required to select the identical preset when the old result depended on shown-role weighting;
- shown identity is selected only after the actual-role candidate is selected.

This is an intentional semantic correction, not an accidental parity regression.

## 7. Corrected campaign sequence

The remaining MS-SETUP campaign is now:

```text
MS-S0    ownership audit                                         COMPLETE
MS-S0.5  recovery scope reduction audit                          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                   COMPLETE / ACCEPTED
MS-S1R   exact setup persistence authority migration             COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts                     COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                             COMPLETE / ACCEPTED
MS-S4    deterministic generated actual-role source              COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership architecture correction       COMPLETE / ACCEPTED

MS-S5    actual-composition diversity history/scorer/selector    NEXT
MS-S6A   shown-identity policy/options boundary
MS-S6B   deterministic shown-identity commitment
MS-S6C   recommendation ownership inversion
MS-S7    TB 480-template adapter + controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance
```

Do not collapse these slices merely because their end-to-end behavior is related.

## 8. MS-S5 corrected contract

MS-S5 now has a stricter boundary:

```text
legal SetupCandidate values
+ actual-composition diversity history
+ deterministic selection seed/context
-> one selected SetupCandidate
```

MS-S5 may consider only data that describes actual-role composition diversity or generic candidate identity/provenance needed for deterministic selection.

MS-S5 must not consume or score:

- `drunkAsOptions`;
- selected Drunk shown role;
- shown-role history;
- `PlayerState.shownRole`;
- first-night clue candidates;
- setup recommendation decisions.

A key durable MS-S5 invariant is:

> Changing shown-identity metadata/history must not change actual-role candidate selection.

The selector remains downstream of legality and must not re-apply Baron or any other setup modifier.

## 9. MS-S6A — shown-identity policy/options boundary

MS-S6A should introduce a pure generic boundary conceptually equivalent to:

```text
selected SetupCandidate
+ validated ruleset
+ candidate provenance
-> legal shown-identity options/policy
```

The exact type/API is still implementation work and is not prescribed by this docs-only checkpoint.

Ownership rules are prescribed:

### TEMPLATE candidate

Use durable `(providerId, candidateId)` provenance to obtain template-specific shown-identity metadata such as TB `drunkAsOptions` without expanding `SetupCandidate` itself.

### GENERATED candidate

For the current Drunk mechanic, derive legal options from the validated script/ruleset Townsfolk set minus actual roles already in play.

If no legal shown identity exists where one is required, fail closed. Do not fall back to an actual in-play Townsfolk merely to continue setup.

Template-specific cardinality rules (for example TB's exactly-three-option contract) stay in template validation, not in the generic core.

## 10. MS-S6B — deterministic shown-identity commitment

MS-S6B owns selecting/committing a shown identity after one `SetupCandidate` is already selected.

Required behavior direction:

```text
Drunk absent
-> no identity override needed

Drunk present
-> resolve legal options
-> canonicalize options
-> deterministic seeded selection
-> select exactly once
-> later seat/deal materialization preserves it
```

The deterministic selection key must include stable setup identity/context sufficient to avoid process-global random state and list-order dependence. Exact hashing/versioning is implementation work for MS-S6B.

Required invariants:

- same selected setup + seed/context -> same shown identity;
- multiple seeds can vary the shown identity where multiple legal options exist;
- selected shown role belongs to the script/ruleset;
- selected shown role is legal for the actual selected composition;
- no unseeded `.random()` / `.shuffled()`;
- no recommendation call participates in shown-role commitment;
- no history mechanism is required in the first generic version.

A future optional `ShownIdentityDiversityPolicy` may be considered separately if product evidence justifies it, but it may only rank legal shown options after composition selection and must never feed back into MS-S5.

## 11. MS-S6C — recommendation ownership inversion

MS-S6C is the behavioral migration that makes committed/perceived identity an input fact.

Target behavior:

```text
actual role = Drunk
+ PlayerState.shownRole = X
-> recommendation treats X as the perceived identity
-> recommendation generates only information compatible with X
-> recommendation never emits or replaces X
```

Examples:

```text
Drunk shown as Investigator
-> generate Investigator-compatible false/misleading information decisions
-> do not choose Investigator again

Drunk shown as Monk
-> no first-night information from Monk merely because actual role is Drunk

Drunk shown as a first-night-information Townsfolk
-> route through the appropriate existing role-information ownership where practical
```

During MS-S6C, audit ownership before implementation so the project does not create duplicate role-information algorithms.

Likely legacy concepts to retire or narrow after typed replacement evidence exists include:

- `StorytellerDecision.DrunkShownRole`;
- `StorytellerDecisionKind.DRUNK_SHOWN_ROLE`;
- `SetupClueOutcome.DrunkShownRole`;
- `PlanEffectSignature.drunkShownRole`;
- Drunk shown-role candidate families / family budgets;
- shown-role recommendation history cooldown;
- `TroubleBrewingSetupRecommendationLock`.

Do not delete these mechanically before their surviving consumers and persistence/history implications are audited in the owning slice.

## 12. MS-S7 — TB controlled semantic cutover

The target TB path becomes:

```text
480 validated templates
-> template SetupCandidate values
-> MS-S5 composition selector
-> selected template candidate
-> resolve selected template's drunkAsOptions
-> MS-S6 shown-identity commitment
-> deterministic deal/materialization
-> CommittedClocktowerSetup
-> recommendation reads committed shownRole
```

Retire the legacy `selectedDrunkShownRole -> preset finalWeight` dependency.

Preserve the corrected parity definition in section 6 rather than requiring exact old-seed preset identity in cases where legacy shown-role weighting changed composition selection.

## 13. MS-S8 — NGJ/no-template cutover

The target no-template path becomes:

```text
GeneratedSetupCandidateSource
-> MS-S5 composition selection/policy
-> MS-S6 generated shown-identity options
-> deterministic shown-identity commitment
-> seat/deal materialization
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

Retire legacy NGJ responsibilities only at this explicit cutover, including unseeded role/shown-role selection and recommendation-time shown-role replacement where still present.

The generated shown-role policy must never use the legacy broad fallback that can choose an actual in-play Townsfolk when unused legal Townsfolk are unavailable.

## 14. MS-S9 — generic future-script acceptance

Acceptance remains architectural rather than script-name-specific:

```text
new supported script
+ validated ruleset
+ registered generic setup provider
+ no template bucket
-> generated legal composition
-> generic composition selector
-> generic shown-identity commitment
-> committed setup
```

Adding such a script must not require a new script-specific branch in App root merely to perform setup generation/identity commitment.

## 15. Testing strategy consequences

MS-S4.5 itself is docs-only and requires no manufactured RED or Android unit test.

Future slices should follow the risk-based evidence rules from `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Important future typed evidence includes:

- MS-S5: shown-identity metadata/history cannot influence composition selection;
- MS-S6A/B: legal option derivation, fail-closed empty-option behavior, stable deterministic identity selection and seed variation;
- MS-S6C: `PlayerState.shownRole` is authority and recommendation cannot select a different identity;
- MS-S7: corrected TB parity and explicit removal of shown-role-to-preset weighting;
- MS-S8: deterministic generated setup + legal non-in-play Drunk shown identity.

Existing tests that only protect the obsolete ownership model may be retired or replaced when the corresponding new typed contract becomes authoritative. Do not keep obsolete production behavior merely to keep those tests green.

## 16. Explicit S4.5 non-goals

This correction does not modify:

- production Kotlin;
- unit/integration tests;
- the 480 TB preset asset;
- existing persistence schema/state;
- App or Host wiring;
- NGJ runtime behavior;
- TB runtime behavior;
- recommendation runtime behavior;
- recovery semantics;
- REC-R1;
- Mayor / Imp / Monk / Attack-Protect / A3 / A4 / ZDD work.

It also does not merge, mark Ready, rebase or force-push PR #61.

## 17. Resume point

MS-S4.5 is the new planning authority for shown-identity ownership.

The next production slice is **MS-S5 actual-composition diversity only**.

Before MS-S5 implementation:

1. re-query live `main`, branch/PR head and checks;
2. treat `6de0e8c99c89a091615c513255adbdb773b3cc69` as the accepted MS-S4 production checkpoint unless a later production commit deliberately supersedes it;
3. read this checkpoint together with the roadmap/current handoff;
4. design typed MS-S5 history/scoring/selection contracts that cannot consume shown-role data;
5. do not pull S6A/S6B/S6C production work into S5;
6. keep PR #61 Draft and unmerged unless explicitly authorized otherwise.
