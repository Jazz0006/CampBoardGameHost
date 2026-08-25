# NEXT DEVELOPMENT HANDOFF — Same-Night Effective Mechanical State Correctness

> Date: 2026-08-25
> Status: **CURRENT / correctness blocker / tests-first**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main` at handoff creation: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`
> Working branch: `codex/clocktower-same-night-effective-state-correctness`
> Parent baseline: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`
> Docs-only planning checkpoint before RED implementation: re-query branch live head before editing.

## 1. Read first

Before implementation, read in this order:

1. root `AGENTS.md`
2. `docs/README.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. this handoff
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`
6. `docs/TESTING_STRATEGY.md`
7. `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`

Then re-query live `main`, branch head/parent lineage and GitHub checks before editing.

## 2. Problem statement

Production already resolves much of a Demon kill correctly:

```text
confirmed attack target
+ Demon functioning state
+ Monk protection
+ Soldier immunity
+ Mayor redirect
-> resolved victim
-> nightDeathWillOccur
-> Ravenkeeper / Sage trigger facts
```

But later interactions still commonly derive mechanics from the public/persisted player state:

```text
PlayerCard.eliminatedRound == null
```

Night death is intentionally written to that public state only at dawn/finalization. Therefore a player can be mechanically dead for later night interactions while production still treats them as alive.

The same missing boundary affects persistent effect lifetime. A Poisoner who acted and then died later that same night should immediately stop poisoning the earlier target, but production can continue carrying the confirmed poison target as an active effect.

This is a system-level correctness problem affecting:

- later normal actor eligibility;
- living-neighbour information;
- target legality;
- reliable/unreliable information truth;
- persistent effect lifetime;
- death-trigger exceptions;
- role changes / Demon succession;
- restore/recomposition consistency.

## 3. Normative architecture

The specialized design authority is:

```text
docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md
```

Required direction:

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable night interaction plan
+ current interaction cursor
        ↓
ClocktowerEffectiveNightState authority/projector
        ↓
actor eligibility
ability functioning
active source-dependent effects
target legality
information evaluator input
conditional/death-trigger eligibility
current role
```

### Hard invariants

1. Mechanical death and public death remain separate.
2. Do not write `eliminatedRound` early merely to make same-night mechanics work.
3. Night interaction order/IDs remain stable; state-dependent eligibility must not reindex completed interactions.
4. Host must not become a second death/effect rules engine.
5. Later consumers must not each add their own `pendingNightDeath` special case.
6. Persistent effects must respect source ability lifetime.
7. Death-trigger / `even if dead` exceptions remain explicit role semantics.
8. Information Decision candidates/contexts must use the effective mechanical snapshot at that interaction cursor.
9. Dynamic scripts composed of known roles must work by role ID/metadata rather than script-name branches.
10. Unknown homebrew mechanics fail closed to assisted/manual Storyteller operation; do not infer executable rules from free text.
11. A state projection for interaction X must describe mechanics **at X**, not merely mechanics at the current Compose screen.
12. Going backward in UI must not retroactively change whether an already-completed earlier interaction was executable when it originally occurred.

## 4. External-project lessons already accepted

Do not repeat the research unless new evidence is needed.

- `pnkfelix/botc-asp`: borrow cursor/time-relative state, delta-vs-state separation, mechanical-vs-public death, role-change inertia.
- `mwc34/botc`: borrow generic action/target contract metadata.
- official `ThePandemoniumInstitute/botc-release`: borrow metadata/special-feature schema boundary, not arbitrary executable ability prose.
- `YuriHKj/botc-solo-simulator`: borrow script-agnostic role registry and narrow engine context.
- `bra1n/townsquare` / `Camuise/clocktower`: borrow stable role-ID/custom-script metadata patterns.

Official Blood on the Clocktower rules remain the behavior oracle. Community implementations are not normative.

## 5. Existing reusable infrastructure — use, do not duplicate

The implementation must audit and preferentially reuse these existing contracts:

### Stable flow identity

`ClocktowerHostInteraction` / `ClocktowerInteractionId` and `ClocktowerProductionNightStepIdentity` already provide canonical role/event interaction identity. The night-step materializer maps projected interactions by stable ID.

Do not invent a second string-based role sequence or script-specific numeric order in the effective-state layer.

### Confirmed night checkpoint

`ClocktowerNightCheckpoint` already persists current-night confirmed/draft selections and `nightStepIndex`, including:

- confirmed attack target;
- confirmed poison target;
- confirmed Monk target;
- confirmed Mayor redirect target;
- pending/new Demon state;
- current night-step index;
- revisions/timeline cursor.

First implementation must attempt deterministic projection from these existing confirmed values plus persisted base game state. **Do not add a persistence/schema migration in the first slice.**

### Global ActionFact timeline

`ActionFactDraft` already has `Poison`, `Protect`, `Attack`, `Death`, `RoleChange`, etc., and `ClocktowerGameSession` binds committed actions to the global timeline.

However, this hotfix must **not** start committing new early `Death`/RoleChange facts merely to solve live same-night UI mechanics unless a separate reviewed architecture decision authorizes that change. Doing so would alter historical/epistemic chronology and could broaden into A3/B4.

For the first live correctness slice:

```text
confirmed live/checkpoint facts
+ canonical interaction plan/cursor
-> transient deterministic effective state
```

is preferred over:

```text
new durable hidden action history semantics
-> replay it into live state
```

If existing confirmed checkpoint data is insufficient to restore the same effective state, STOP and report the exact missing fact before modifying persistence.

### Existing death resolution

Current Host already knows how to combine Demon functioning, Monk, Soldier and Mayor redirect to obtain the resolved victim / no-death result.

The desired ownership is to extract or delegate that calculation to a **pure rules-owned resolver**, not copy it into `ClocktowerEffectiveNightState` and not leave two independently evolving versions in Host.

## 6. Target implementation shape

### 6.1 Pure owner

Preferred new owner:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerEffectiveNightState.kt
```

If local dependency audit proves that `clocktower/session` is materially better, STOP before implementation and report why. Do not silently relocate the authority.

The pure owner must not import:

- Compose;
- Android UI;
- `PlayerCard`;
- localized role names;
- script display names;
- recommendation packages.

Use `RoleId`, seat identity, canonical interaction IDs and small immutable rule inputs.

### 6.2 Canonical cursor, not ad-hoc index semantics

The effective state should be addressable **per interaction identity**.

Recommended conceptual input/output:

```text
ClocktowerNightPlanSnapshot
  orderedInteractionIds

ClocktowerNightPlayerSeed
  seat
  actualRoleId
  publiclyAlive
  [only other pure mechanical identity required by existing rules]

ClocktowerConfirmedNightFacts
  confirmed poison/protection/attack/redirect/succession inputs
  no UI draft state

ClocktowerEffectiveNightStateProjector.projectAt(
  basePlayers,
  orderedInteractions,
  confirmedFacts,
  interactionId
) -> ClocktowerEffectiveNightState
```

Exact class names may be adjusted only if the same ownership and invariants are preserved.

`nightStepIndex` may remain a UI/checkpoint continuation cursor, but the rules authority must not embed assumptions such as “Empath is always index 7”. Resolve index -> canonical interaction ID at the adapter boundary.

### 6.3 Effective state minimum contract

The first production contract must be sufficient to answer, without per-role `pendingNightDeath` hacks:

```text
isMechanicallyAlive(seat, interaction)
currentRoleId(seat, interaction)
sourceAbilityIsActive(sourceSeat, expectedRoleId, interaction)
mechanicallyLivingSeats(interaction)
```

It may also expose narrowly typed resolved-trigger information if needed for Ravenkeeper/Sage/succession, but do not turn it into a generic rule DSL.

### 6.4 Source-dependent persistent effect

A confirmed effect such as Poison should not be treated as active merely because its target is stored.

Required pattern:

```text
confirmed poison target
+ source player still mechanically has a functioning Poisoner ability at this cursor
-> effective poison target
```

This is intentionally generic in lifetime semantics: future dynamic roles may have a persistent effect whose lifetime is tied to the source ability. Do not solve this by clearing `poisonTarget` in Host when Poisoner dies.

### 6.5 Dynamic scripts / dynamic roles

The effective-state authority must be script-agnostic:

- known role behavior is selected by `RoleId` / registered handler metadata;
- dynamic scripts are compositions of canonical role IDs and interaction definitions;
- do not branch on `Trouble Brewing`, `Bad Moon Rising`, etc. inside the state authority;
- homebrew roles without an executable handler fail closed to assisted/manual operation;
- free-text ability descriptions are not executable rules.

Future direction, not required to fully build in this hotfix:

```text
CharacterDefinition
  metadata
  nightOrder / interaction identity
  action contract

CharacterRuleHandler
  eligibility
  legalTargets
  resolution / effects
  information
  triggers
```

The current hotfix should leave a clean seam for this direction without implementing the whole registry/DSL.

## 7. Implementation slices and review gates

Do not attempt one giant GREEN commit. Use the following checkpoints.

### SNE-0 — local entry audit / NO EDITS

Before any source edit, Luna must:

1. synchronize the exact working branch safely;
2. verify live branch head expected by this document/recent Chat instruction;
3. verify branch descends from `c8985cb4991f6c7e5ea02adedb932d2d86452da1`;
4. verify worktree cleanliness or create an isolated worktree without stash/reset;
5. inspect existing:
   - `ClocktowerNightCheckpoint`;
   - `ClocktowerHostInteraction` / IDs;
   - `ClocktowerNightStepMaterializerRegistry`;
   - `ActionFactDraft` / `ActionFactTimeline`;
   - current death-resolution block;
   - `AbilityFunctioningSemantics`;
   - `livingNeighbors` / Empath information path;
6. explicitly report whether existing checkpoint + base state + canonical plan are sufficient for deterministic same-night reconstruction.

If not sufficient, STOP before tests and report the exact missing confirmed fact.

### SNE-1 — RED-only current-production characterization

Goal: prove the live bug using current production surfaces before adding the new authority.

Allowed changes: tests only.

Create focused deterministic tests, preferably a new class such as:

```text
ClocktowerSameNightEffectiveStateProductionWiringTest
```

plus a second role-target contract test if needed.

Required RED assertions should prove at least:

1. production later-night actor/living-neighbour wiring still derives mechanically alive from `eliminatedRound` rather than a same-night effective-state authority;
2. same-night resolved death is not yet consumed by Empath subject/living-neighbour calculation;
3. later information impairment can still use raw confirmed poison even when the Poisoner has mechanically died;
4. Fortune Teller truthful result must not use an alive-only search;
5. Butler Master target list must not be alive-only.

Source-level wiring assertions are acceptable for this first checkpoint because the required pure authority does not yet exist, but they must target the exact known defect and must fail for semantic reasons, not compile/environment reasons.

Also add passing control assertions where cheap to preserve existing correct behavior:

- Soldier/Monk/poisoned Demon can prevent an actual death;
- Ravenkeeper/Sage are conditional death-trigger exceptions, not ordinary alive-only actors.

Run the focused tests with `--rerun-tasks` so the RED execution is real.

**STOP after SNE-1. Do not implement GREEN in the same Luna run.**

Commit/push the RED-only checkpoint, then return to Chat for remote audit.

### SNE-2 — pure death resolver + effective-state authority RED/GREEN

Only after Chat approves SNE-1.

First write pure unit RED for the desired authority. Then implement minimal GREEN.

Required pure behavior matrix:

```text
Imp attacks ordinary player, no protection
-> resolved mechanical death occurs after resolution cursor

Soldier functioning
-> no mechanical death

Monk functioning and protects final victim
-> no mechanical death

Imp poisoned / not functioning
-> no mechanical death

Mayor redirect confirmed
-> original Mayor remains mechanically alive
-> final redirected victim becomes mechanically dead

before death-resolution cursor
-> victim still mechanically alive at earlier interactions

after death-resolution cursor
-> victim mechanically dead

same facts + same plan + same interaction ID
-> identical state
```

The death resolver must have exactly one rules-owned implementation. Host may call it; Host must not duplicate it.

No Compose/Host integration beyond minimal compile wiring in this slice unless required by the pure contract.

### SNE-3 — canonical interaction identity + actor eligibility + Empath

Migrate the first production consumers.

Required behavior:

1. each materialized night step can be associated with its canonical `ClocktowerInteractionId` without localized-name inference;
2. ordinary actor eligibility is evaluated using effective state **at that interaction**;
3. Imp kills Empath before Empath slot -> Empath normal step is not executable;
4. Imp kills one Empath neighbour -> Empath uses the next mechanically living player at the Empath cursor;
5. earlier completed interaction remains semantically unchanged if its actor dies later;
6. structured Empath InformationDecision subject seats/candidate snapshot use the same effective-state projection as the displayed truth.

Do not patch Empath by subtracting `resolvedNightDeathName` locally. The living-neighbour evaluator must receive an effective mechanical snapshot.

### SNE-4 — persistent source-effect lifetime

Migrate confirmed Poison first as the representative persistent effect.

Required behavior:

```text
Poisoner acts -> target poisoned
Imp later kills Poisoner -> target immediately healthy for later interactions
Imp fails to kill Poisoner -> poison remains
Poisoner changes away from Poisoner before later interaction -> old poison ends
```

Do not mutate/clear the stored confirmed poison target to represent expiration. Stored confirmation and effective active effect are different concepts.

If Monk/protection lifetime needs the same helper and is safely expressible, reuse the generic source-ability lifetime seam rather than adding a second special mechanism.

### SNE-5 — target-contract corrections

Correct only the directly exposed role target contracts:

- Chambermaid: current effective alive targets only;
- Fortune Teller: alive or dead targets are legal; dead Demon still yields Yes;
- Butler: dead Master is legal.

Do not apply a blanket alive-only or dead-allowed rule. Target legality belongs to the role action contract.

### SNE-6 — death-trigger and succession exceptions

Verify/migrate:

- Ravenkeeper killed at night still gets the death-trigger interaction;
- Sage killed by Demon still gets its trigger when present;
- ordinary dead actors do not get normal ability actions;
- Imp self-kill: old Imp mechanically dead; successor current role effective; successor does not gain a second normal Imp action that same night.

Do not rebuild/reindex the night plan after succession. Role-state changes affect eligibility/state at later cursor positions, not the identity of completed earlier interactions.

### SNE-7 — restore/recomposition determinism + full regression

Prove:

```text
same persisted base state
+ same existing confirmed checkpoint values
+ same canonical projected plan
+ same interaction ID
-> same EffectiveNightState
```

No schema migration is allowed unless SNE-0/SNE-7 produces explicit evidence that current persisted facts are insufficient and Chat approves the smallest missing-fact contract.

Then run T1/T2/triggered T3 and prepare PR.

## 8. Required RED matrix before final GREEN completion

The campaign is not complete until tests cover:

### Core effective-state

1. `Imp kills Empath -> later normal Empath interaction is not executable.`
2. `Imp kills one Empath neighbour -> Empath subject seats and truthful value use the next living neighbour.`
3. `functioning Soldier is attacked -> no effective death.`
4. `functioning Monk protects the final victim -> no effective death.`
5. `poisoned Imp selects a target -> no effective death.`
6. `Mayor redirect -> only the final resolved victim becomes mechanically dead.`

### Persistent effect

7. `Poisoner poisons X, then Imp kills Poisoner -> X is healthy for later interactions that night.`
8. Source loses/changes the relevant character/ability -> its old source-bound persistent effect ends unless explicit role semantics say otherwise.

### Actor / trigger

9. Later ordinary actor killed before its slot (representative Undertaker / Fortune Teller / Butler) does not execute a normal ability.
10. Ravenkeeper killed at night still receives the death-trigger interaction.
11. Sage killed by Demon still receives the death-trigger interaction when that role/script is present.
12. Imp self-kill makes the old Imp mechanically dead and successor role effective, but the new Imp does not receive a second normal Imp action that night.

### Target contract

13. Chambermaid cannot select a player mechanically dead at the Chambermaid cursor.
14. Fortune Teller can select and correctly detect a dead Demon.
15. Butler can select a dead Master.

### Stability / restore

16. Killing a role later does not remove/reindex its already-completed earlier interaction.
17. Same confirmed checkpoint/facts + same canonical interaction cursor rebuilds an identical effective state.

## 9. Likely production files / ownership allowlist by slice

This is a staged allowlist, not permission to edit every file immediately.

### Pure owner / rules

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerEffectiveNightState.kt       [new, preferred]
app/src/main/java/com/codex/campboardgamehost/clocktower/rules/AbilityFunctioningSemantics.kt       [only narrow reusable contract if required]
```

A separate pure `ClocktowerNightDeathResolver.kt` is allowed if it cleanly removes the existing Host death-calculation duplication and stays rules-owned.

### Flow identity / materialization adapter

```text
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepMaterializerRegistry.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerHostPresentationModels.kt
```

Change only to preserve canonical interaction identity/state wiring. Do not move UI responsibilities here.

### Production Host wiring

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
```

Host changes must remain adapters/wiring/derived-state only. No large new rules algorithm.

### Information / target UI

```text
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt
```

Only when needed for effective target lists / information snapshot wiring.

### Tests

New pure tests should prefer:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/rules/ClocktowerEffectiveNightStateTest.kt
app/src/test/java/com/codex/campboardgamehost/ClocktowerSameNightEffectiveStateProductionWiringTest.kt
```

Directly related existing tests may be minimally updated/extended:

```text
StructuredEmpathInformationAdapterTest.kt
ClocktowerAdvanceNightStepTransactionOwnershipTest.kt
ClocktowerNightStepMaterializerRegistryTest.kt
clocktower/flow/ClocktowerHostInteractionProjectorTest.kt
existing AbilityFunctioningSemantics tests
night checkpoint/restore tests
```

Do not touch `CampBoardGameHostApp.kt` in the planned path.

If a production edit outside this staged allowlist becomes necessary, STOP and report before making it.

## 10. Forbidden scope

Do not combine this correctness campaign with:

- App-root S9.2 persistence extraction;
- file-size/decomposition work;
- A3/A4/B4 work;
- recommendation tuning or authority promotion;
- Possible Worlds expansion;
- a general homebrew rules DSL;
- runtime interpretation of natural-language abilities;
- schema/persistence migration by default;
- history UI changes;
- broad flow/planner/projector rewrite;
- unrelated registration redesign;
- unrelated role-rule cleanup;
- new early durable action-history semantics without a separate reviewed decision.

The Fortune Teller dead-target and Butler dead-Master defects may be corrected in this campaign only as narrow tests-first target-contract fixes because they are directly exposed by the same alive/target boundary.

## 11. STOP gates

Stop and report instead of expanding implementation if any of the following becomes necessary:

1. public `eliminatedRound` must be mutated before dawn;
2. Host needs a second independent death/protection legality engine;
3. the fix requires rebuilding/reindexing the night plan after each event;
4. a persistence/schema migration appears necessary;
5. current confirmed checkpoint/action facts are insufficient to deterministically restore the effective state;
6. a general rule DSL/interpreter appears necessary;
7. the implementation starts adding separate per-role `pendingNightDeath` checks;
8. broad `CampBoardGameHostApp.kt` or Host refactoring becomes necessary;
9. A3/A4/B4/recommendation architecture would have to change;
10. the first GREEN cannot remain a narrow same-night lifecycle/state authority plus adapters;
11. solving live state would require changing existing durable ActionFact global ordering;
12. interaction identity cannot be preserved without localized role names or script-name branching;
13. a state calculation for one interaction would depend on mutable UI draft selections rather than confirmed facts;
14. the new authority would need to import Compose/Android/`PlayerCard`.

If confirmed facts are insufficient for restore, preserve the RED evidence and propose the smallest additional confirmed fact contract before continuing.

## 12. Test / validation ladder

`docs/TESTING_STRATEGY.md` is authoritative.

### Local environment

```bash
export GRADLE_USER_HOME="$PWD/.gradle-codex"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
./gradlew --version
```

Use repository `./gradlew`, not a system Gradle install.

Do not commit `.gradle-codex/`.

### T0 — SNE-1 RED

Use the exact new production-wiring/target-contract RED classes and force actual execution:

```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.codex.campboardgamehost.ClocktowerSameNightEffectiveStateProductionWiringTest" \
  --rerun-tasks \
  --no-daemon
```

If target-contract RED is kept in a second class, add its exact `--tests` selector to the same command or run a second focused command.

The expected result is test assertion failure proving the current bug. Compilation failure, missing SDK/dependency, Gradle lock failure or environment failure is **not** accepted as semantic RED evidence.

### T0 — later GREEN loops

Use exact new pure state/resolver tests plus smallest directly affected consumer tests. Keep the edit loop near the Gradle floor where practical.

### T1

```bash
./gradlew :app:testFast --no-daemon
```

### T2

Because this changes shared game-state authority / night flow / production wiring, T2 must include affected:

```text
pure rules/effective-state tests
AbilityFunctioningSemantics tests
StructuredEmpathInformationAdapterTest
ClocktowerNightStepMaterializerRegistryTest
ClocktowerAdvanceNightStepTransactionOwnershipTest
ClocktowerHostInteractionProjectorTest
relevant production-flow wiring tests
night checkpoint/restore tests
```

Resolve exact existing class names with `rg` after the final diff rather than guessing silently.

Then:

```bash
./gradlew :app:assembleDebug --no-daemon --build-cache
```

### T3

This is central game-mechanics/shared-state authority. Follow `TESTING_STRATEGY.md` change-family routing. Run triggered simulation/exact semantic tests when the final owners require them. Do not run unrelated expensive tests merely because they exist.

If any `clocktower/domain`, `epistemic`, `history`, core `rules`, or exact semantic surface changes in a way selected by CI routing, expect applicable Real Clingo/full semantic gates at PR.

### T4 / PR

Applicable Android FULL:

```bash
./gradlew :app:testFull --no-daemon
./gradlew :app:assembleDebug --no-daemon --build-cache
```

GitHub CI/R2 remains the independent merge gate.

`UP-TO-DATE` / `FROM-CACHE` is not evidence of execution when an explicit execution proof is required.

Always run:

```bash
git diff --check
```

and exact changed-file/numstat audits.

## 13. Exact-diff discipline

At every checkpoint:

```bash
git status --short
git diff --check
git diff --stat
git diff --name-only
git diff --numstat
```

For Host and any large file, inspect the exact diff region. Reject:

- line-ending churn;
- formatting-only churn;
- broad import reorder unrelated to compilation;
- copied death rules;
- role-name/script-name branches added to the new authority;
- hundreds of lines of new Host policy;
- `.gradle-codex`, build outputs, IDE metadata.

## 14. Luna execution ownership

```text
ChatGPT / Chat
  = live-state audit
  = architecture / invariant decisions
  = slice boundaries
  = RED acceptance
  = remote exact-diff / CI review

Luna/local Codex
  = safe worktree sync
  = mechanical test/code edits inside approved slice
  = local Gradle execution
  = exact local diff
  = commit/push only when the slice explicitly permits it
```

Luna must not independently redesign this architecture, invent a DSL, broaden to persistence/history, or continue past an explicit checkpoint stop.

## 15. Immediate next checkpoint — SNE-1 RED ONLY

The next Luna run is intentionally narrow.

Expected result:

```text
current docs-only branch
-> tests-only RED commit
-> push
-> STOP
```

No production GREEN is authorized in that run.

Required final Luna report:

1. actual worktree path;
2. initial `main` and target branch refs after fetch;
3. initial branch HEAD and parent lineage;
4. Java version / JAVA_HOME / GRADLE_USER_HOME;
5. files inspected during SNE-0;
6. determination: existing checkpoint inputs sufficient for deterministic projection? yes/no + evidence;
7. exact RED test files added/changed;
8. exact T0 command(s);
9. failing test names/assertions;
10. explanation why each failure is semantic RED, not setup/compile/environment failure;
11. `git diff --check` result;
12. exact diff stat/name-only/numstat;
13. RED commit SHA;
14. pushed remote head;
15. final `git status --short`;
16. any STOP condition or architecture warning.

After push, stop and wait for Chat remote audit.

## 16. Merge discipline

Do not merge, mark ready, rebase, force-push or broaden scope without explicit user authorization.
