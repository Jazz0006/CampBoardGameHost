# NEXT DEVELOPMENT HANDOFF — UX-MODE-1 Beginner / Experienced Storyteller Mode

> Date: 2026-09-11 Australia/Sydney  
> Status: **CURRENT — product design finalized; implementation planning active**  
> Program: Storyteller Experience Modes  
> Branch: `codex/ux-mode-1`  
> Base `main` when branch created: `34b5bd84aae5e7216d1d1460d572b0d60b5da02b`  
> Predecessor decision: EPI-MQ-0 audit completed; EPI-MQ implementation intentionally paused until this task is complete  
> Design record: `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

## 1. Final product goal

Provide one normal default Storyteller experience and one optional **Experienced mode** without creating two gameplay engines, two legality systems, or two recommendation systems.

The user-facing concept is intentionally simple:

```text
Experienced mode
允许说书人手动调整系统推荐的线索
```

The default state is Experienced mode **OFF**.

Conceptually the typed product state may still be represented as:

```kotlin
enum class StorytellerExperienceMode {
    BEGINNER,
    EXPERIENCED,
}
```

but the normal UI should expose a single Experienced-mode switch rather than asking the user to classify themselves as Beginner or Experienced.

### Default / Beginner experience

Target:

- first-time app user;
- inexperienced Blood on the Clocktower Storyteller;
- user who wants the app to make Storyteller strategic decisions automatically.

Core rule:

> **The host should not be asked to make Storyteller strategic choices.**

The host mainly:

```text
performs the physical hosting action
records a player's own choice when the rules require that player to choose
shows the system-selected information/result
continues the flow
```

Ordinary screens should therefore minimize controls. Where the app owns the decision, the normal result should usually reduce to one clear **Show / 展示** action.

### Experienced experience

Experienced mode unlocks Storyteller intervention, not a second recommendation algorithm.

Expected behavior:

- the same underlying recommendation system is used;
- the primary recommendation remains visually dominant;
- normally expose about 2–3 ranked recommended choices where meaningful;
- provide a manual path to remaining rule-valid alternatives;
- keep explanations concise;
- optimize for fast host operation.

## 2. Settings / entry design

The Settings tab under Host Tools should be simplified.

### Language control

Remove the current large three-button language section.

Replace it with compact flat controls in the upper-right area:

```text
中   EN
```

Do not spend a large settings card on language selection.

### Experienced-mode control

Use the reclaimed prominent settings area for:

```text
熟练模式
允许说书人手动调整系统推荐的线索
```

Fresh installs default to Experienced mode **OFF**.

Remove the old user-facing global recommendation-style / automation choices:

```text
Manual / Automatic
Balanced
Gentle
Aggressive
```

The recommendation style is no longer a normal product setting.

## 3. Critical architecture invariant

Experience mode is an **interaction / presentation authority distinction**, not a rules fork.

For identical canonical game state, history and decision inputs, both modes must use the same:

- legal candidate generation;
- candidate legality;
- truth / reliability semantics;
- registration legality;
- recommendation provider and ranking inputs;
- session/history authority;
- stable decision identity.

Conceptual pipeline:

```text
rules -> legal candidates
            |
            v
single recommendation / ranking provider
            |
            v
common decision context
            |
            +--> BEGINNER: auto-commit/use Top 1; no ordinary Storyteller strategic choice
            |
            +--> EXPERIENCED: Top 2–3 recommendations + manual legal alternatives
```

Do not implement separate Beginner candidate generation or separate Experienced legality.

Future EPI-MQ replaces/improves the recommendation/ranking provider without redesigning experience mode.

## 4. Temporary recommendation policy before EPI-MQ

The current real-game experience indicates that evil is too difficult for the current group. Until the epistemic/productive-uncertainty algorithm replaces this temporary behavior, use the current **Aggressive** recommendation behavior as the single internal recommendation style.

This is an implementation detail, not a user setting.

Required invariant:

```text
BEGINNER and EXPERIENCED consume the same Aggressive-ranked recommendation set.
BEGINNER exposes/uses Top 1.
EXPERIENCED exposes approximately Top 2–3 plus manual legal alternatives.
```

Do not implement:

```text
BEGINNER -> Aggressive
EXPERIENCED -> Balanced/Gentle/another algorithm
```

There is one recommendation pipeline.

## 5. What is automatic in default mode

The default mode must automatically resolve every **Storyteller strategic decision** for which the app has a legal candidate domain.

### 5.1 Setup / first-night Storyteller decisions

Automatically select and do not show an intermediate choice page for:

- Fortune Teller red herring;
- Drunk shown identity;
- Drunk first-night information such as Investigator information;
- Demon bluffs;
- Washerwoman / Librarian / Investigator and other setup/first-night information choices;
- other equivalent setup-owned Storyteller information decisions.

The user should proceed directly to the physical hosting/showing step.

### 5.2 Ongoing information results

When a player ability requires a player-visible result and the Storyteller has legal discretion, default mode automatically selects the current Top-1 recommendation.

Examples include:

- numeric information;
- Boolean / Fortune Teller result after the Fortune Teller has selected two players;
- role information such as Undertaker / Sage / Ravenkeeper where registration or impairment creates legal alternatives;
- other unreliable or Storyteller-selectable information surfaces.

### 5.3 Spy / Recluse special registration

When special registration is legally available for the current interaction:

```text
90% -> use a legal special / false registration
10% -> use actual registration
```

If multiple legal special registrations exist, choose among the legal special candidates using stable deterministic weighted selection; the temporary policy does not need epistemic optimization among those false identities.

If special registration is not legal for that interaction, use actual registration.

Legality remains owned by the existing rule/candidate layer. The 90/10 policy must never manufacture an illegal registration.

### 5.4 Mayor death redirect

Temporary aggressive default policy:

```text
if at least one eligible living Townsfolk other than the Mayor exists:
    90% -> redirect to one eligible living Townsfolk
    10% -> Mayor dies
else:
    Mayor dies
```

The target within the Townsfolk redirect family is selected stably from legal candidates.

This Townsfolk preference is a temporary recommendation strategy, not a rule-legality statement. The existing rule engine remains authoritative for whether the chosen redirect target actually dies after protection / Soldier / other effects are resolved.

### 5.5 Imp self-kill / Demon succession

Rules legality is evaluated before preference.

If the Scarlet Woman rule mandates succession (healthy eligible Scarlet Woman with the required player-count condition), that forced rule result wins and no temporary weighting may override it.

Otherwise, for multiple legal living Minion successors, use these temporary weights:

```text
Baron          4
Scarlet Woman  3
Spy            2
Poisoner       1
```

Equivalently the preference is:

```text
Baron > Scarlet Woman > Spy > Poisoner
```

If only one legal successor exists, choose that successor with probability 1. If no legal successor exists, normal game-end/rules behavior applies.

## 6. Player choices are never auto-invented

Experience mode must distinguish **Storyteller strategy** from **player agency**.

The app must continue to ask the host to record choices made by players, including examples such as:

- Poisoner target;
- Monk protection target;
- Imp kill target;
- Butler master;
- Fortune Teller's two selected players;
- Chambermaid's two selected players;
- Ravenkeeper target;
- any equivalent player-owned choice.

The app may automatically determine the resulting Storyteller information after the player's choice has been entered, but it must not choose the player's target for them.

## 7. Stable randomness requirement

Probabilistic automatic decisions must be stable and idempotent.

Required behavior:

```text
same game + same semantic decision identity + same relevant revision/history
-> same automatic selection
```

Navigating Previous -> Next, recomposition, reopening a dialog, or restoring the same persisted decision state must not re-roll a 90/10 or weighted decision.

Prefer reuse of the existing deterministic selection infrastructure:

- `DecisionSeedFactory` for stable seed material;
- `WeightedStableSelector` for fixed-point family/candidate weighting.

Do not call ad-hoc `Random.next*` from Compose/UI code.

## 8. Guidance / explanation design

This task does not require significantly more text than the current UI.

The main Beginner/default-mode improvement is to visually emphasize:

```text
WHO / which player is acting
WHAT the host should do now
WHAT should be shown / recorded next
```

Do not turn each night step into a long rules tutorial.

Detailed wording and emphasis may be refined surface-by-surface after the mode/authority behavior is working.

## 9. Ownership audit — current live structure

Read-only audit against `main` at branch creation found:

### 9.1 Legacy mode / persistence owner

`CampBoardGameHostApp.kt` currently owns SharedPreferences keys and load/save behavior for:

```text
AUTOMATIC_STORYTELLER_INFO_KEY
STORYTELLER_AUTOMATION_MODE_KEY
StorytellerAutomationMode
```

The App root holds `storytellerAutomationMode` state and projects it through `StorytellerRecommendationUxPolicy`.

### 9.2 Current compatibility policy

`StorytellerRecommendationUxPolicy.fromLegacyMode(...)` currently ignores all legacy global modes and returns:

```text
automaticExecution = false
recommendationStyle = BALANCED
```

This compatibility policy is the clean replacement point. Do not layer an independent experience Boolean on top of it.

### 9.3 Settings ownership

`AppSettingsScreen.kt` still receives `StorytellerAutomationMode` and a change callback, but the currently rendered Settings content no longer exposes the old automation/style controls. The language section still renders large language buttons and is the intended UI location for the new compact language controls plus Experienced-mode switch.

Settings remains hosted under Host Tools as established by UI-NAV-1.

### 9.4 Judge / Host projection

App root passes:

```text
automaticStorytellerInfo
automaticStorytellerStyle
```

into `ClocktowerJudgeScreen` / Host flow.

This is a transitional projection boundary. The implementation should move toward a typed experience/presentation policy rather than propagate another unrelated Boolean.

### 9.5 Existing automatic-capable surfaces

Existing production paths already contain AUTO/ASSISTED candidate projection for significant areas, including:

- first-night information pools;
- numeric information;
- Boolean / Fortune Teller information;
- Spy / Recluse registration;
- Mayor redirect;
- Demon succession;
- setup recommendation decisions.

`ClocktowerNightStepCardLocalized` is a major integration point and large-file risk surface. Keep new policy/scoring logic outside it.

### 9.6 Existing reusable deterministic infrastructure

The repository already owns:

- `WeightedStableSelector` with fixed-point family/candidate probability allocation;
- `DecisionSeedFactory` with deterministic semantic seed material;
- focused recommender tests for Mayor redirect, Demon succession, special registration and information selection;
- `StorytellerRecommendationUxPolicyTest` as the current typed compatibility-policy test owner.

## 10. Legacy preference migration decision

Introduce one new authoritative persisted Experience-mode value/key.

For the new product behavior:

```text
new key present -> load explicit BEGINNER / EXPERIENCED
new key absent  -> BEGINNER
```

The old `StorytellerAutomationMode` and old automatic Boolean must not preserve a hidden Gentle/Balanced/Aggressive style after this migration. The old style selector is removed from normal product semantics.

After the new experience value is saved, remove/ignore obsolete legacy automation preference keys as appropriate.

This is an intentional product behavior change: default mode now performs automatic Storyteller strategic decisions.

## 11. Implementation plan

### UX-MODE-1A — product/design + ownership audit

Status: **COMPLETE in this document**.

Outputs now established:

- final user-facing mode semantics;
- default behavior;
- legacy ownership;
- temporary Aggressive policy;
- automatic-decision inventory;
- deterministic-random requirement;
- existing reusable selector/seed infrastructure;
- large-file risk boundary.

No production behavior should be changed in this slice.

### UX-MODE-1B — typed experience mode + persistence/presentation policy

Introduce one authoritative `StorytellerExperienceMode` and revise/replace the legacy `StorytellerRecommendationUxPolicy`.

Required behavior:

```text
BEGINNER:
    automatic Storyteller strategic execution enabled
    recommendationStyle = AGGRESSIVE (temporary internal provider choice)
    manual alternatives hidden

EXPERIENCED:
    automatic strategic execution disabled
    recommendationStyle = AGGRESSIVE
    ranked recommendations + manual legal alternatives enabled
```

Add deterministic persistence ownership and default-to-BEGINNER migration behavior.

Update Settings API away from `StorytellerAutomationMode`.

### UX-MODE-1C — temporary automatic strategic selector

Add the smallest typed policy/seam necessary to express temporary automatic decisions without placing probability logic in Compose.

Cover:

- 90/10 special registration;
- 90/10 Mayor redirect-to-Townsfolk vs Mayor dies;
- 4:3:2:1 non-forced Demon successor weighting;
- stable seed / no reroll.

Reuse existing legal pools and deterministic selector infrastructure. Do not duplicate legality.

Do not change EPI-MQ or build a new world evaluator.

### UX-MODE-1D — information/setup UI convergence

Apply the experience policy consistently to:

- setup/first-night Storyteller decisions;
- red herring;
- Drunk shown role / first-night information;
- Demon bluffs;
- pair information;
- numeric information;
- Boolean / Fortune Teller information;
- role information surfaces such as Undertaker / Sage / Ravenkeeper;
- Spy / Recluse registration;
- Mayor redirect;
- Demon succession;
- any equivalent Storyteller-owned decision found during implementation.

BEGINNER/default:

- automatically commit/use Top 1;
- skip pure Storyteller choice pages where possible;
- retain only physical action / player-choice recording / Show / Next controls.

EXPERIENCED:

- show Top 2–3 ranked recommendations where meaningful;
- provide manual access to remaining legal candidates;
- recommended option remains visually primary.

All confirmation/commit paths must still pass through existing typed validation/session authority.

### UX-MODE-1E — Settings layout + instruction emphasis

Implement:

- compact `中 / EN` language control;
- prominent Experienced-mode switch and finalized copy;
- removal of obsolete automation/style product controls/API;
- small WHO / ACTION emphasis changes where needed.

Do not broaden this into another global UI redesign.

### UX-MODE-1F — acceptance / cleanup

- remove obsolete legacy mode projections that no longer own behavior;
- verify no hidden Balanced/Gentle/Aggressive user setting remains;
- verify both modes use one legal/recommendation pipeline;
- update roadmap/handoff to resume EPI-MQ after merge.

## 12. Tests-first / validation plan

This task changes stable product behavior, persistence default, automatic selection policy and presentation authority. Use meaningful typed tests at their true owners.

### Slice B focused contracts

Revise/replace `StorytellerRecommendationUxPolicyTest` to prove:

1. default mode is BEGINNER;
2. BEGINNER enables automatic Storyteller strategic execution;
3. EXPERIENCED enables manual alternatives;
4. both use the same temporary `AGGRESSIVE` recommendation style;
5. mode itself does not alter legal candidate identity/rules semantics;
6. legacy style cannot survive as hidden Gentle/Balanced behavior.

Add the narrowest persistence/migration test available for the new preference owner if current persistence has a durable callable seam. Do not create source-string tests for SharedPreferences wiring merely for ceremony.

### Slice C focused contracts

Use typed selector/recommender tests to prove:

- special registration family probability mass is 900000 / 100000 when both families are legal;
- no illegal special registration is invented;
- Mayor policy is 900000 redirect-family / 100000 Mayor-dies when an eligible living Townsfolk exists;
- no eligible Townsfolk => Mayor dies;
- non-forced succession uses Baron/Scarlet Woman/Spy/Poisoner weights 4/3/2/1;
- mandatory Scarlet Woman rule overrides weighting;
- same decision seed yields the same selection;
- candidate ordering/recomposition cannot change the result.

Existing `WeightedStableSelectorTest`, `DecisionSeedFactoryTest`, `MayorRedirectRecommenderTest`, `DemonSuccessorRecommenderTest` and `SpecialRegistrationRecommenderTest` are likely reusable evidence owners; add new tests only for uncovered product contracts.

### Slice D/E focused contracts

Prefer typed presentation-model/policy tests proving:

- Beginner exposes only Top 1 / automatic action;
- Experienced exposes ranked recommendations plus manual legal alternatives;
- player-owned target selection remains interactive in both modes;
- stale/illegal candidate validation is unchanged;
- mode switching does not mutate session/history merely by rendering.

Do not create source-string assertions for visual text/layout. Use compile/static/UI evidence for purely visual placement.

### Checkpoint cadence

Per `docs/TESTING_STRATEGY.md`:

```text
per behavior slice:
    meaningful focused T0 RED/GREEN where a real uncovered contract changes
    git diff --check / exact diff audit

logical UX-MODE checkpoint:
    :app:testFast
    affected flow/session/recommendation tests

acceptance checkpoint:
    broader T2/T4 as selected by the final changed semantic areas
    GitHub CI / R2 before merge
```

Because this changes recommendation/selection behavior, deterministic selection and central Host wiring, affected recommendation/distribution tests should be reviewed for T2 escalation. Do not run every expensive suite after every micro-slice.

## 13. Large-file execution strategy

`CampBoardGameHostApp.kt` and Host/night integration files are large/truncated connector surfaces.

Follow root `AGENTS.md`:

- small new typed mode/policy/selector/test files -> GitHub connector direct writer;
- small/medium complete files such as policy/settings files -> GitHub connector direct writer;
- localized edits in a large truncated file -> first prefer repository one-shot GitHub Actions + separate Python patch script locked to exact HEAD/blob SHA/anchors;
- use Codex/Luna only if the one-shot path cannot be made safe or the change genuinely requires broad local worktree edits.

Do not perform whole-file replacement of large Host/App files through truncated connector content.

Keep recommendation probability/scoring logic out of Compose and out of the large Host source.

## 14. Scope fence

During UX-MODE-1 do not:

- implement EPI-MQ ranking, world-count scoring or hypothetical evaluation;
- build a second beginner recommendation engine;
- make Drunk/Poisoned always false as a mode rule;
- replace the future recommendation provider beyond selecting current Aggressive behavior as the temporary single policy;
- add Moonchild, Pukka or other new roles;
- generalize the world engine for dynamic scripts;
- activate A4/ZDD;
- redesign UI-R5 square-table interaction or UI-NAV-1 global navigation beyond the mode/settings changes;
- auto-invent player-owned choices;
- add ad-hoc UI randomness.

## 15. Exit criteria

UX-MODE-1 is complete when:

```text
[ ] one authoritative StorytellerExperienceMode exists
[ ] fresh/default experience is BEGINNER / Experienced switch OFF
[ ] Settings shows compact 中 / EN and the Experienced-mode switch
[ ] obsolete global Manual/Balanced/Gentle/Aggressive choices are gone
[ ] one internal temporary Aggressive recommendation pipeline serves both modes
[ ] Beginner/default requires no ordinary Storyteller strategic choice
[ ] player-owned choices remain player-owned
[ ] red herring and equivalent pure Storyteller choice pages are skipped in default mode
[ ] Spy/Recluse special registration auto-policy is legal and stable
[ ] Mayor auto-resolution follows the agreed 90/10 temporary policy
[ ] Demon succession honors forced Scarlet Woman semantics then 4:3:2:1 weighting
[ ] random automatic decisions are stable across navigation/recomposition/restore of the same semantic decision
[ ] Experienced exposes Top 2–3 recommendations plus manual legal alternatives
[ ] both modes share candidate legality, recommendation and session/history authority
[ ] focused typed/integration evidence passes
[ ] :app:testFast passes at the logical checkpoint
[ ] required affected/T4 validation and GitHub CI/R2 pass before merge
[ ] roadmap/handoff are updated to resume EPI-MQ
```

## 16. What follows immediately after completion

After UX-MODE-1 is merged and accepted, resume the epistemic program from:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Then continue:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility / contradiction / impairment-exposure gates
EPI-MQ-3+   productive-uncertainty ranking
```

The future EPI-MQ recommendation/ranking output should replace this document's temporary Aggressive policy without changing the Beginner/Experienced interaction architecture.

## 17. Default reading order for implementation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main`, `codex/ux-mode-1`, and any UX-MODE PR state;
6. the small policy/selector/test owners for the current slice;
7. only then the exact Host/App integration anchors required for wiring.

Do not load the full EPI-MQ reference stack until UX-MODE-1 is complete unless a narrow semantic dependency must be checked.
