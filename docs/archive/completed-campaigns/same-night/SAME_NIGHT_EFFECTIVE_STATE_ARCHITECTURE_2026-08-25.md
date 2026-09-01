# Clocktower Same-Night Effective State Architecture

> Date: 2026-08-25
> Status: **SPECIALIZED DESIGN / architecture authority for the current same-night correctness campaign**
> Repository: `Jazz0006/CampBoardGameHost`
> Baseline at research start: `main = c8985cb4991f6c7e5ea02adedb932d2d86452da1` (PR #53 merged)
> Scope: same-night mechanical state, role eligibility, information/target legality, persistent-effect lifetime, death-trigger timing, future dynamic scripts/characters

## 1. Why this document exists

A high-impact correctness defect was found after the Information Decision authority hotfix was merged.

The immediate symptom is easy to describe with the Empath:

```text
Imp kills an Empath neighbour
-> that player is mechanically dead immediately
-> Empath acts later in the same night
-> Empath must skip the dead neighbour and use the next living neighbour
```

The current production path can still derive later-night logic from `PlayerCard.eliminatedRound == null`, even though night deaths are not written to that public/persisted field until dawn. Therefore the App can know that a death will occur, but later roles can still consume a stale "alive" state.

The problem is broader than Empath. Official rules require abilities to take effect immediately, abilities to be lost immediately on death/drunkenness/poisoning, and persistent effects to end immediately when their source loses its ability unless the ability explicitly says otherwise. That affects later role eligibility, target legality, information truth, poisoning/protection, death triggers, role changes and succession.

This document records external-project research and defines the architecture direction before implementation starts.

## 2. Normative gameplay semantics

Official Blood on the Clocktower rules remain the behavior oracle. Community projects are architecture references only.

Required semantics for this work:

1. **Ability effects apply immediately.** A player killed before their normal night action does not later wake to use that normal ability.
2. **Mechanical death and public death knowledge are different.** A night death affects mechanics immediately but is normally announced publicly at dawn.
3. **Persistent effects end immediately when their source loses its ability.** Example from the official ability rules: if the Poisoner poisons the Slayer and then dies later that same night, the Slayer is no longer poisoned.
4. **Death-trigger / even-if-dead abilities are exceptions.** Ravenkeeper still acts because the ability triggers on dying; Spy/Recluse registration can continue where the role text explicitly permits it.
5. **Empath reads the state when the Empath wakes.** If the Demon killed a neighbour earlier that night, the Empath skips that now-dead player.
6. **Chambermaid must choose alive players.** Same-night deaths therefore affect its target set when the death occurred earlier in night order.
7. **Fortune Teller may choose alive or dead players.** A dead Demon still produces a positive detection.
8. **Butler may choose a dead player as Master.** Target legality must come from the role's own action contract, not a blanket alive-only policy.
9. **New characters gain the new ability immediately; old persistent effects end immediately.** Exact timing still depends on the night cursor/order and role-specific triggers.

Primary official references used during this audit:

- Blood on the Clocktower Wiki: `Abilities`
- `Empath`
- `Poisoner`
- `Fortune Teller`
- `Butler`
- `Glossary`

## 3. Current production root cause

The production Host already contains useful resolved facts:

```text
confirmed Demon target
+ Demon functioning / poisoned state
+ Monk protection
+ Soldier immunity
+ Mayor redirect
-> resolvedNightDeathName
-> nightDeathWillOccur
-> Ravenkeeper / Sage trigger facts
```

This is important: the defect is **not** that the App has no death-resolution logic.

The missing boundary is:

```text
resolved action result
-> authoritative effective mechanical state for every later interaction
```

Today, major consumers still derive from public/persisted state:

```text
aliveCards = cards.filter { eliminatedRound == null }
abilitySubject.isAlive = eliminatedRound == null
livingNeighbors(cards, ...)
poisonTarget = confirmed Poisoner target for the whole remaining night
```

Night death is only persisted to `eliminatedRound` at dawn/finalization. This is correct for public/persistent presentation, but incorrect as the sole source for same-night mechanics.

The same architectural gap also means a Poisoner killed later in the night can leave its target incorrectly treated as poisoned by subsequent roles.

## 4. External project research

### 4.1 `pnkfelix/botc-asp` — strongest temporal-state reference

This is the closest open-source project found to a formal automated rules model. Its strongest ideas are directly relevant.

#### A. Structured time points

The model represents a night with ordered time points such as:

```text
night(N, RoleOrder, Substep)
```

Roles do not merely ask "what is true tonight?"; they ask what is true at a particular point in the night.

This is the most important architectural lesson for our bug. Same-night correctness is inherently **cursor-relative**.

#### B. Delta events and state are separate concepts

The project explicitly distinguishes event/delta predicates from persistent state predicates.

Examples:

```text
d_died(Player, Time)          // an event happened
alive(Player, Time)           // state at this time
dead_at_time(Player, Time)    // mechanical state
character_assignment_state_at_time(Time, Player, Role)
```

State is derived through temporal progression/inertia rather than being equated with UI presentation.

#### C. Mechanical death and public death are separate tracks

`botc.lp` explicitly documents two death tracks:

```text
ST/mechanical perspective:
  alive / dead_at_time

public knowledge:
  publicly_alive / publicly_dead_at_time
```

The design intent is exactly what CampBoardGameHost needs: a player may be mechanically dead during the night while the public death is not announced until dawn.

#### D. Role assignment is a time-indexed state

`character_assignment_state_at_time(...)` persists by inertia and changes through explicit role-change events. This is a good model for Imp succession, Scarlet Woman takeover, Pit-Hag-style future roles, resurrection and other dynamic-character interactions.

#### E. Role files are modular

Role-specific logic is kept in per-role files while common concepts such as time, death state and registration are shared. Empath, for example, asks the generic living-neighbour/state system rather than owning a separate death model.

#### F. Incremental mode accepts a current state snapshot

`incremental.lp` supports validating one action from `inc_*` state facts instead of replaying the full game. The useful principle is:

```text
same role/rule definitions
+ different state source
= live validation or full historical replay
```

That aligns well with our existing live/session + historical/epistemic architecture.

#### Important caveat

Do **not** copy the current ASP implementation as a gameplay oracle. Its architecture is valuable, but detailed current rules still need verification. For example, its generic Demon death derivation is concentrated around the final night time, and the Poisoner file visibly handles ordinary expiry/reassignment but does not provide a complete source-dies-same-night lifetime rule in that file. This is precisely the class of timing detail we are fixing.

**Decision:** borrow the event/state/time-point separation; do not inherit individual predicates without official-rule validation.

### 4.2 `mwc34/botc` — useful data-driven action contracts

This virtual grimoire supports custom characters with JSON action metadata. Its `nightActions` / `nightActionsScoped` definitions describe generic interaction shape instead of hard-coding every modal.

Useful metadata includes:

```text
players / characters
inPlayers / inCharacters
playerRestrictions / characterRestrictions
alive / dead
others (non-self)
cancel
scope / scopeRestrictions
create
confirm
group
```

This is valuable for future dynamic scripts/characters because **target legality and UI interaction shape can be declarative data**.

However, this project is primarily a virtual grimoire/action UI, not a complete authoritative automated rules engine. Its schema tells us how to describe choices, not how every role effect should resolve.

**Decision:** borrow the generic action-contract concept; keep mechanical resolution in typed rule handlers/engine authority.

### 4.3 Official `ThePandemoniumInstitute/botc-release` — metadata/schema boundary

The official app release repository exposes a custom-character JSON schema. Important ideas:

```text
id / name / team / ability
firstNight / otherNight ordering
firstNightReminder / otherNightReminder
reminders / remindersGlobal
setup
special[]
```

The `special` property uses generic typed features and timing metadata such as:

```text
type = ability / player / signal / vote / reveal / reminder / selection
time = day / night / firstNight / otherNight / ...
global = ...
```

This demonstrates an important boundary: **character metadata and app interaction capabilities can be dynamic without pretending arbitrary natural-language ability text is executable code.**

The official app does not provide a public complete automation engine through this schema. Homebrew behavior outside known generic features still requires Storyteller judgment.

**Decision:** future dynamic-character support should have a metadata layer that can load arbitrary role definitions, but automatic mechanical correctness must require a known executable rule handler or a deliberately defined rule DSL.

### 4.4 `YuriHKj/botc-solo-simulator` — reusable role registry and narrow engine context

This project documents a migration from a monolithic engine to role modules. A role definition can expose:

```text
id
scriptAgnostic
action
misinformationProfile
phaseHooks:
  firstNight
  eachNight
  otherNight
  afterNightDeaths
```

The engine owns shared primitives and passes a narrow context to role modules:

```text
state access
role lookup / registration checks
target helpers
death processing
logging
phase transitions
```

Later work generalizes action input types beyond one/two player targets:

```text
player-target
player-role
role
question
guesses
charge-or-targets
...
```

This is a strong extensibility pattern and is compatible with our existing `ClocktowerCharacterInteractionRegistry` direction.

#### Important caveat

The project's own design notes state that Butler chooses a living non-self player, which conflicts with current official Butler rules allowing a dead Master. It also explicitly has deferred death-trigger work.

**Decision:** borrow registry/context/action-interface architecture only; do not treat its role behavior as normative.

### 4.5 `bra1n/townsquare` / `Camuise/clocktower`

These mature virtual grimoires show that practical custom-script support is best built around stable role IDs, generic first/other-night ordering metadata, reminder metadata and optional custom-character definitions. They deliberately keep the Storyteller as final rules authority rather than attempting to execute arbitrary homebrew text.

**Decision:** dynamic scripts should be easy; dynamic executable characters should be explicit and fail safely when no rule implementation is available.

## 5. Architecture synthesis for CampBoardGameHost

The recommended target is a hybrid of the strongest ideas above, adapted to the existing Android/session architecture.

```text
Persisted/public base state
+ confirmed same-night actions/effects
+ stable night interaction plan
+ current night cursor
        |
        v
EffectiveNightState projector
        |
        +--> actor eligibility
        +--> ability functioning
        +--> active persistent effects
        +--> target legality
        +--> information evaluator input
        +--> conditional/death-trigger eligibility
        +--> role-change/current-role view
```

### 5.1 Three state domains must remain separate

#### A. Public/persisted game state

Examples:

```text
PlayerCard.eliminatedRound
public death history
saved game state
```

Night deaths remain unannounced until dawn. Do not mutate this layer early merely to make mechanics convenient.

#### B. Same-night mechanical state

Transient/rebuildable authoritative mechanics:

```text
effective alive/dead
effective current role
effective functioning state
active poison/protection/other persistent effects
resolved deaths
conditional/death triggers
```

This is the missing layer.

#### C. Player epistemic state

What a specific player has observed or can infer remains in the existing epistemic/history architecture. Same-night mechanical truth must not be confused with player knowledge.

### 5.2 The night plan must be stable; state changes, not list indices

Do not rebuild the night list by deleting a role immediately after that role dies. The current UI uses a stable `nightStepIndex`; re-filtering earlier roles can shift the cursor and replay/skip the wrong interaction.

Preferred behavior:

```text
stable ordered interaction plan
+ effective state at cursor
-> current interaction is active / placeholder / conditional trigger
```

Examples:

- Monk acts, then is killed by the Imp: the already-completed Monk slot stays in history.
- Empath is killed by the Imp before Empath's slot: Empath's normal later slot remains structurally addressable but is not a real normal action.
- Imp self-kills and creates a new Imp: the new role exists immediately, but the stable cursor has already passed the Imp slot, so the new Imp does not act a second time that night.

### 5.3 Use ordered confirmed mechanical events, not ad-hoc role flags

Long-term target types should resemble:

```text
NightMechanicalEvent
  - EffectApplied
  - EffectEnded
  - DeathResolved
  - RoleChanged
  - AbilityStateChanged
  - TriggerResolved
```

and a pure reducer/projector:

```text
EffectiveNightState reduce(baseState, orderedEvents, cursor)
```

The implementation does not have to introduce a large new event-sourcing subsystem in the first hotfix. Existing confirmed night choices/checkpoint/action facts may be sufficient inputs for the first projector. The important contract is that **later consumers ask one state authority**, not each reinvent `pendingNightDeath`/`eliminatedRound` logic.

### 5.4 Persistent effects need source dependency as first-class semantics

A generic active effect needs more than `target + expiresAt`.

Conceptually:

```text
ActiveEffect
  id
  sourceSeat
  sourceRoleId
  target(s)
  effectKind
  nominalWindow
  sourceDependency
```

Default source dependency for ordinary character abilities should reflect the official rule:

```text
active only while source retains the relevant ability
```

Therefore:

- Poisoner dies -> poison ends immediately;
- source becomes drunk/poisoned -> persistent effect ends/suspends as official rules require;
- source changes character -> old-role persistent effect ends;
- explicit exceptions such as "even if dead" or death-trigger abilities override the default through role semantics.

This avoids hard-coding "if Poisoner dead, clear poison" as a one-off.

### 5.5 Dynamic scripts and dynamic characters are different problems

#### Dynamic script

A script that combines already-known roles should require no new engine code:

```text
script JSON role IDs
-> CharacterDefinition registry
-> night-order metadata
-> existing CharacterRuleHandler implementations
-> generated stable interaction plan
```

This should be a first-class design requirement for the current fix.

#### Dynamic / homebrew character

Loading metadata is easy; executing arbitrary ability prose correctly is not.

Recommended capability levels:

```text
Level 1: metadata-only
  name/team/icon/ability text/night order/reminders
  -> manual Storyteller support

Level 2: generic action contract
  target count/type/alive-dead/self restrictions/input shape
  -> generic UI, still manual mechanical outcome if no handler

Level 3: registered executable CharacterRuleHandler
  -> authoritative automation

Future optional Level 4: declarative rule/effect DSL
  -> only after contracts are proven; never infer mechanics from free text at runtime
```

Unknown custom mechanics must **fail closed to assisted/manual Storyteller control**, not guess.

### 5.6 Character runtime seam

The long-term seam should be script-agnostic and role-ID-driven, conceptually:

```text
CharacterDefinition
  metadata
  nightOrder
  actionContract

CharacterRuleHandler
  normalActionEligibility(state, cursor)
  legalTargets(state, intent)
  resolve(intent, state) -> Resolution
  information(state, confirmedChoice) -> legal information space
  triggers(state, event) -> interactions/effects
```

The existing `ClocktowerCharacterInteractionRegistry`, materializer registry, dynamic-decision work and Information Decision authority should converge toward this seam rather than adding more script-name branches in Host.

### 5.7 Information Decision must consume the effective snapshot

After this hotfix, an information decision context must be built from the same mechanical state that determines the current interaction.

For Empath:

```text
EffectiveNightState at Empath cursor
-> effective living neighbours
-> truthful number / legal unreliable candidate space
-> InformationDecisionContext
-> confirmation
-> durable observation
```

This prevents a stale pre-Imp truth from being wrapped in a perfectly valid post-PR#53 confirmation envelope.

### 5.8 Restore/recomposition must be deterministic

The effective state should be rebuildable from persisted public state plus confirmed night checkpoint/action facts. Compose state must not become a second hidden rules authority.

A restore of the same checkpoint at the same cursor must produce the same:

```text
effective alive set
effective current roles
active effects
legal targets
information truth
trigger eligibility
```

Avoid a persistence schema migration in the first slice unless the existing confirmed checkpoint facts are demonstrably insufficient.

## 6. Specific current bugs and risk areas discovered

### Confirmed root-cause family

- Imp kills Empath -> Empath may still be treated as alive at the later normal Empath step.
- Imp kills an Empath neighbour -> neighbour may remain in `livingNeighbors` until dawn.
- Imp kills Poisoner after Poisoner acted -> poison may incorrectly remain active for later roles.
- Any later normal role that is killed before its night slot can be affected by the same alive-state gap (e.g. Undertaker, Fortune Teller, Butler; Chambermaid in scripts where its order is later).
- Chambermaid target candidates can include a player who already died earlier in the same night if they are sourced from persisted `aliveCards`.

### Required exceptions

- Ravenkeeper / Sage-style death-trigger abilities must still run when their trigger condition is met.
- Spy/Recluse registration semantics that explicitly work even if dead must not be disabled by a blanket dead-player rule.
- A protected/immune attempted kill must not create effective death: Monk, Soldier, poisoned Demon and redirect resolution remain part of the authoritative death resolver.

### Independent role-rule defects found during the audit

These are related to the same target/state surfaces but are not the root cause:

1. Fortune Teller result calculation currently uses an alive-only scan in production, although official rules allow checking dead players and a dead Demon still returns Yes.
2. Butler UI currently restricts Master selection to living players, although official rules allow a dead Master.

These should be corrected in the same correctness campaign only if they remain isolated, tests-first fixes and do not obscure the effective-state core.

## 7. RED matrix before production changes

The first implementation must prove failures before GREEN.

### Core mechanical-state RED

1. `Imp kills Empath -> later normal Empath interaction is not executable.`
2. `Imp kills one Empath neighbour -> later Empath truth and subject seats use the next living neighbour.`
3. `Imp targets Soldier while Soldier functions -> no effective death; later state still sees Soldier alive.`
4. `Imp target protected by functioning Monk -> no effective death.`
5. `poisoned Imp selects a target -> no effective death.`
6. `Mayor redirect -> only the resolved final victim becomes mechanically dead.`

### Persistent-effect RED

7. `Poisoner poisons X, then Imp kills Poisoner -> X is healthy for later interactions that night.`
8. Source loses/changes the relevant role -> old persistent effect does not remain active unless role semantics explicitly allow it.

### Role eligibility / trigger RED

9. Later ordinary Undertaker/Fortune Teller/Butler actor killed before their slot -> no normal ability execution.
10. Ravenkeeper killed at night -> death-trigger interaction still executes.
11. Sage killed by Demon in a script containing Sage -> death-trigger interaction still executes.
12. Imp self-kill -> old Imp dead, successor role change effective, but no second Imp normal action later in the same night.

### Target-contract RED

13. Chambermaid cannot target a player who is mechanically dead at Chambermaid's cursor.
14. Fortune Teller can target and correctly detect a dead Demon.
15. Butler can select a dead Master.

### Stability / restore RED

16. Confirming an earlier role action and then killing that role does not remove/reindex already-completed interactions.
17. Same persisted checkpoint + same cursor rebuilds an identical effective state.

## 8. Implementation boundary recommendation

Do not start by patching `ClocktowerHostScreen.kt` role by role.

Preferred first production seam is a **small pure Kotlin same-night state authority/projector**, likely in `clocktower/rules` or `clocktower/session`, that can be unit-tested without Compose.

It should initially own only facts necessary for the current correctness campaign:

```text
resolved effective death
mechanical alive/dead at current cursor
source-functioning-dependent poison/protection lifetime
effective current role where already confirmed
```

Then wire Host/flow/information builders to consume it.

Do not in the first slice:

- build a general homebrew programming language;
- rewrite all night flow;
- migrate persistence schemas unless required;
- fold player epistemic state into mechanical state;
- move unrelated Host/App-root code for decomposition;
- rewrite recommendation/A3/A4/B4 systems;
- duplicate death legality in a second Host helper.

## 9. Long-term design principles accepted by this audit

1. **Official rules are the behavior oracle; external projects are architectural references.**
2. **Mechanical state is time/cursor-relative.**
3. **Mechanical truth and public presentation are separate state domains.**
4. **Events/deltas and persistent state are separate concepts.**
5. **One rules-owned effective-state authority feeds flow, target legality and information.**
6. **Night interaction order remains stable while interaction eligibility is state-dependent.**
7. **Persistent effects carry explicit source/lifetime semantics.**
8. **Role behavior is role-ID-driven and script-agnostic.**
9. **Dynamic scripts composed of known roles require no new behavior code.**
10. **Unknown dynamic character mechanics fail safely to manual/assisted mode.**
11. **UI action shape can be data-driven; authoritative mechanical resolution remains typed.**
12. **Restore/recomposition must deterministically rebuild effective state from confirmed facts.**
13. **Information Decision contexts must use the effective state at the information interaction cursor.**

## 10. External references reviewed

Primary project references:

- `pnkfelix/botc-asp`
  - `botc.lp`
  - `incremental.lp`
  - `night_order.lp`
  - `roles/tb/townsfolk/empath.lp`
  - `roles/tb/minions/poisoner.lp`
  - `roles/tb/demons/imp.lp`
  - `roles/tb/townsfolk/ravenkeeper.lp`
- `mwc34/botc` README/custom-character night action schema
- `ThePandemoniumInstitute/botc-release` README + `script-schema.json`
- `YuriHKj/botc-solo-simulator`
  - `docs/design/ROLE_MODULE_REFACTOR_PHASE1.md`
- `bra1n/townsquare` / `Camuise/clocktower` custom script/character model

The core implementation handoff for this campaign is maintained separately in:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_SAME_NIGHT_EFFECTIVE_STATE_CORRECTNESS.md
```
