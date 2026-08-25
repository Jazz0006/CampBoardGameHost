# NEXT DEVELOPMENT HANDOFF — Same-Night Effective Mechanical State Correctness

> Date: 2026-08-25
> Status: **CURRENT / correctness blocker / tests-first**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main` at handoff creation: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`
> Working branch: `codex/clocktower-same-night-effective-state-correctness`
> Parent baseline: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`

## 1. Read first

Before implementation, read in this order:

1. root `AGENTS.md`
2. `docs/README.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. this handoff
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`
6. `docs/TESTING_STRATEGY.md`

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
+ current cursor
        ↓
EffectiveNightState authority/projector
        ↓
actor eligibility
ability functioning
active effects
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

## 4. External-project lessons already accepted

Do not repeat the research unless new evidence is needed.

- `pnkfelix/botc-asp`: borrow cursor/time-relative state, delta-vs-state separation, mechanical-vs-public death, role-change inertia.
- `mwc34/botc`: borrow generic action/target contract metadata.
- official `ThePandemoniumInstitute/botc-release`: borrow metadata/special-feature schema boundary, not arbitrary executable ability prose.
- `YuriHKj/botc-solo-simulator`: borrow script-agnostic role registry and narrow engine context.
- `bra1n/townsquare` / `Camuise/clocktower`: borrow stable role-ID/custom-script metadata patterns.

Official Blood on the Clocktower rules remain the behavior oracle. Community implementations are not normative.

## 5. Required RED before production changes

Start with pure/mechanical RED rather than Compose patches.

### Core effective-state RED

1. `Imp kills Empath -> later normal Empath interaction is not executable.`
2. `Imp kills one Empath neighbour -> Empath subject seats and truthful value use the next living neighbour.`
3. `functioning Soldier is attacked -> no effective death.`
4. `functioning Monk protects the final victim -> no effective death.`
5. `poisoned Imp selects a target -> no effective death.`
6. `Mayor redirect -> only the final resolved victim becomes mechanically dead.`

### Persistent-effect RED

7. `Poisoner poisons X, then Imp kills Poisoner -> X is healthy for later interactions that night.`
8. Source loses/changes the relevant character/ability -> its old persistent effect ends unless explicit role semantics say otherwise.

### Actor / trigger RED

9. A later ordinary actor killed before its slot (representative: Undertaker / Fortune Teller / Butler) does not execute a normal ability.
10. Ravenkeeper killed at night still receives the death-trigger interaction.
11. Sage killed by the Demon still receives the death-trigger interaction when that role/script is present.
12. Imp self-kill makes the old Imp mechanically dead and successor role effective, but the new Imp does not receive a second normal Imp action that night.

### Target-contract RED

13. Chambermaid cannot select a player mechanically dead at the Chambermaid cursor.
14. Fortune Teller can select and correctly detect a dead Demon.
15. Butler can select a dead Master.

### Stability / restore RED

16. Killing a role later does not remove/reindex its already-completed earlier interaction.
17. Same confirmed checkpoint/facts + same cursor rebuilds an identical effective state.

Observe genuine RED execution before writing GREEN production code.

## 6. First implementation slice

Prefer a small pure Kotlin authority in `clocktower/rules` or, if ownership audit proves stronger, `clocktower/session`.

Conceptually it should expose a narrow immutable state such as:

```text
EffectiveNightState
- mechanically alive/dead seats
- effective current role where already confirmed
- active source-dependent effects needed by current scripts
- resolved conditional/death triggers needed by the cursor
```

and a deterministic projection/reduction from existing confirmed facts.

Do **not** implement a large generic event-sourcing framework in the first slice. A minimal event/fact representation may be introduced only where it removes duplication and remains useful for dynamic scripts/roles.

### Likely production surface — audit before editing

Expected owners include:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/rules/...
app/src/main/java/com/codex/campboardgamehost/clocktower/session/...
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerInformationStepBuilder.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt
```

`ClocktowerNightStepCardLocalized` or its owner may change only for target-list wiring if required by the RED contracts.

`AbilityFunctioningSemantics.kt` may change only if the pure effective-state boundary requires a narrow general input/contract. Do not embed Host-specific pending state into the rules primitive.

`CampBoardGameHostApp.kt` is **not** an expected implementation owner. Touch it only if a minimal already-confirmed checkpoint/fact input must be wired; stop and report before any broad Root change.

## 7. Forbidden scope

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
- unrelated role-rule cleanup.

The Fortune Teller dead-target and Butler dead-Master defects may be corrected in this campaign only as narrow tests-first target-contract fixes because they are directly exposed by the same alive/target boundary.

## 8. STOP gates

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
10. the first GREEN cannot remain a narrow same-night lifecycle/state authority plus adapters.

If confirmed facts are insufficient for restore, preserve the RED evidence and propose the smallest additional confirmed fact contract before continuing.

## 9. Tests / validation ladder

`docs/TESTING_STRATEGY.md` is authoritative.

In Luna/local worktree:

```bash
export GRADLE_USER_HOME="$PWD/.gradle-codex"
```

### T0 — first RED/GREEN loop

Use exact new pure state tests plus the smallest directly affected role/wiring tests. The initial T0 should cover at least:

```text
Imp -> Empath death/neighbor behavior
Poisoner source-death effect expiry
protected/immune failed death
Ravenkeeper death-trigger exception
stable cursor/no reindex
```

Add the Fortune Teller/Butler target-contract RED in the same campaign, but it may be a second focused T0 command if that keeps the first loop fast.

### T1

```bash
./gradlew :app:testFast
```

### T2

Run affected flow/information/session/wiring regression tests and:

```bash
./gradlew :app:assembleDebug
```

Likely affected suites include the relevant existing:

```text
ClocktowerAdvanceNightStepTransactionOwnershipTest
StructuredEmpathInformationAdapterTest
ClocktowerHostInteractionProjectorTest
ClocktowerProductionFlowWiringTest / related flow wiring tests
AbilityFunctioningSemantics tests
night checkpoint/restore tests
```

Choose exact suites from the final diff/dependency surface; do not blindly run every historical test on each edit.

### T3

This is central game-mechanics behavior, so run the triggered semantic/simulation tier if `TESTING_STRATEGY.md` requires it for the actual changed owners. Do not run unrelated expensive suites merely because they exist.

### T4

At PR, run the applicable full Android gate and confirm actual execution rather than relying on `UP-TO-DATE`/cache status alone.

Also always run:

```bash
git diff --check
```

and an exact changed-file audit.

## 10. Suggested execution ownership

Because `ClocktowerHostScreen.kt` is a very large protected orchestration owner and this change is multi-file + Gradle-sensitive:

```text
ChatGPT:
  architecture / RED contracts / scope / remote diff / CI audit

Luna/local Codex:
  constrained implementation / local RED-GREEN / Gradle / exact diff
```

Do not perform broad Host replacement through the GitHub connector.

## 11. Expected first checkpoint

The next meaningful checkpoint should be **RED only**:

```text
new pure effective-state tests
+ narrow production-wiring/role-contract RED where necessary
+ no GREEN production changes yet
```

Report:

1. branch head;
2. exact changed files;
3. exact T0 command;
4. failing test names/assertions;
5. why each failure proves the known bug rather than a test setup error;
6. any architecture STOP condition encountered.

Only after that checkpoint is reviewed should the minimal GREEN implementation proceed.

## 12. Merge discipline

Do not merge, mark ready, rebase, force-push or broaden scope without explicit user authorization.
