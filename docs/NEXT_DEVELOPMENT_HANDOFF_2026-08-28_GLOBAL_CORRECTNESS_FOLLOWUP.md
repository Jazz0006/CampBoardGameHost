# Next Development Handoff — Global Correctness Review Follow-up

> Date: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Status: **ACTIVE PLAN / RED TO WRITE / PR #54 remains draft and unmerged**

## 1. Why this follow-up exists

SNE-7 same-night effective mechanical state work is **closed / broad green**. Its final accepted broad checkpoint remains:

```text
70935644daf5c06985420f19833dbda3a160bbfa
```

with the later docs-only closeout checkpoint:

```text
83bafdeef2e8445ee6ef92a3e247d63fdf4b58ce
```

A subsequent whole-PR / whole-night correctness review found that the accepted SNE-7 contracts are strong inside one night, but several important behaviors were either outside those contracts or still protected by implementation-shaped tests.

This handoff starts a **new follow-up campaign** rather than reopening SNE-7. Do not reinterpret the findings below as invalidating the accepted SNE-7 same-night/Dawn work.

## 2. Startup contract

Before changing production code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. read `docs/TESTING_STRATEGY.md` and `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
5. re-query live `main`, PR #54 head/state/checks;
6. distinguish executable checkpoints from later docs-only commits;
7. establish a typed RED before each correctness GREEN.

Never merge, mark PR ready, rebase, force-push, or broaden PR #54 without explicit authorization.

## 3. Campaign name and priority

Use the prefix **GCR — Global Correctness Review Follow-up**.

```text
GCR-1  Current Demon authority / cross-night succession         HIGH / MERGE BLOCKER
GCR-2  Poisoned Spy information integrity                       HIGH / MERGE BLOCKER
GCR-3  Typed production acceptance + source-string retirement   MEDIUM
GCR-4  Chambermaid actual wake-history authority                MED-HIGH / FOLLOW-UP
GCR-5  Durable identity + reconstruction API hardening          LOW-MED / FOLLOW-UP
```

Order is intentional. Do not start GCR-4/5 before GCR-1/2 are understood and green unless a RED proves they are required to make a blocker correct.

## 4. GCR-1 — Current Demon authority

### 4.1 Review finding

After Imp succession, historical game state may legitimately contain both:

```text
old Imp: dead, current/actual role Imp
new Imp: alive, current/actual role Imp
```

The review found production callsites that still identify the Demon using implementation assumptions such as:

```text
first Demon-team card
single card whose role is Imp
```

Those assumptions are not equivalent to **the current living Demon** after succession.

Risk:

```text
Night N:
  Imp0 self-kills
  Minion A becomes Imp1

Night N+1:
  Imp1 may fail to be selected as the acting Demon

Later:
  Imp1 self-kills
  succession resolution may fail because historical dead Imp0 + live Imp1
  both satisfy role == Imp
```

The same identity mismatch can also make mechanical resolution and Host explanation consult different Demon cards.

### 4.2 Required design direction

Do **not** patch every `firstOrNull` / `singleOrNull` independently.

Establish one typed authority for the current Demon/current Demon seat, derived from the authoritative current mechanical state. Attack execution, succession, poison/functioning checks, Host explanation and restore/retry consumers must agree on that authority.

Do not erase historical dead Demon role identity merely to recover uniqueness.

### 4.3 Tests-first RED set

Minimum REDs:

#### RED GCR-1A — first successor acts next night

```text
Imp0 dead after valid self-kill
Imp1 alive after valid succession
-> next Other Night identifies Imp1 as the acting Demon
```

The test must exercise the real semantic/production owner, not a helper that reimplements `alive && role == Imp` inside the test.

#### RED GCR-1B — repeated succession

At minimum:

```text
Imp0 -> Imp1 -> Imp2
```

Prove:

- dead historical Imps remain represented correctly;
- exactly one current acting Demon exists at each actionable night;
- Imp1 can later self-kill;
- the next canonically legal successor is resolved correctly.

Prefer a small generation/chain helper that can extend to `Imp0 -> Imp1 -> Imp2 -> Imp3` without duplicating scenario code.

#### RED GCR-1C — poison/explanation authority convergence

After succession, poison/functioning evaluation and Host-visible attack explanation must use the same current Demon identity as the mechanical attack resolver.

No test-local proxy is acceptable as the only proof.

### 4.4 GCR-1 acceptance

GCR-1 is complete only when:

1. current Demon selection is typed and centralized;
2. first successor acts on the next eligible night;
3. repeated succession works with historical dead Imps still present;
4. mechanical attack, poison/functioning semantics, succession and Host explanation agree on current Demon identity;
5. focused and affected regression tests are green.

## 5. GCR-2 — Poisoned Spy information integrity

### 5.1 Review finding

The current poisoned Spy path can wake the Spy while withholding the real Grimoire without providing an equivalent unreliable/fabricated presentation. If the outward result is effectively “you wake, but see nothing”, the player can infer impairment from the interaction protocol itself.

That violates the project’s player-knowledge safety goal even if no true hidden Grimoire data is leaked.

### 5.2 Required semantic contract

The player-facing protocol must not reveal impairment merely through interaction shape.

Required principle:

```text
healthy Spy
-> normal Spy interaction
-> truthful permitted Grimoire view

poisoned/drunk Spy
-> outwardly normal Spy interaction shape
-> no unauthorized true Grimoire leak
-> legally fabricated / unreliable presentation
```

The exact unreliable-Grimoire representation must remain rules-safe and must not expose Storyteller-hidden truth accidentally.

### 5.3 Tests-first RED set

#### RED GCR-2A — no poison-status side channel

Compare healthy and impaired Spy player-facing interaction structure. The test should protect at least:

- Spy is still treated as having an information interaction;
- impairment does not collapse the interaction to `None` merely because truthful content is forbidden;
- the visible protocol does not explicitly announce poison/drunk state.

#### RED GCR-2B — no true Grimoire leak

The impaired path must not reuse the real hidden Grimoire payload as its unreliable display.

#### RED GCR-2C — legal unreliable presentation

The result must pass the same information legality/authorization boundary used by other impaired information roles rather than bypassing it with a Spy-specific UI exception.

### 5.4 GCR-2 acceptance

GCR-2 is complete only when:

1. impaired Spy does not learn impairment from a missing-information UI shape;
2. true Grimoire data is not leaked;
3. the presentation is generated/confirmed through typed information authority;
4. healthy Spy behavior is unchanged;
5. focused and affected information/knowledge tests are green.

## 6. GCR-3 — Typed production acceptance and source-string retirement

### 6.1 Goal

SNE-7 created strong typed planners/reducers/reconstruction tests, but several remaining correctness-adjacent tests still inspect `.kt` source text, symbol names or statement order.

Source-string tests may remain only where they protect a coarse architecture/ownership invariant that currently has no callable seam. They must not be treated as gameplay correctness proof.

### 6.2 High-priority retirement candidates from the global review

Re-audit these before deleting or rewriting them:

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
InformationDecisionProductionAuthorityWiringTest
ClocktowerDemonSuccessionProductionWiringTest
ClocktowerHistoricalActionProductionWiringTest
ClocktowerNightRestoreProductionOwnershipTest
ClocktowerSameNightEffectiveStateProductionWiringTest
ClocktowerMayorDemonExclusionWiringTest
ClocktowerProductionOtherNightWiringTest
ClocktowerNightTransactionArchitectureGuardTest
source-wiring portion of ClocktowerChambermaidSelectionAuthorityTest
```

This is a review queue, not an instruction to delete all of them immediately.

For each candidate classify it as:

```text
A. typed replacement already proves the behavior -> retire source-string test
B. behavior matters but no callable production seam exists -> introduce narrow seam, then replace
C. pure architecture/ownership guard -> keep coarse, remove gameplay-detail assertions
D. obsolete implementation-shape assertion -> delete
```

### 6.3 Dawn production seam

The strongest restore/retry acceptance already uses real typed Dawn planning and durable session commits, but its final base-state materialization is still test-local while App production wiring is partly guarded by source inspection.

Preferred direction:

```text
DawnCommitIntent
+ current durable state
-> callable ProductionDawnMaterializer
-> thin App/Compose callback
```

Only extract this seam if it improves typed production acceptance without broad App-root refactoring.

### 6.4 GCR-3 acceptance

- gameplay correctness is not proved only by source text;
- retired tests have typed replacements or are demonstrably obsolete;
- remaining source-string tests are explicitly coarse architecture guards;
- source-string count does not grow during GCR work without documented justification.

## 7. GCR-4 — Chambermaid actual wake-history authority

This is follow-up scope after the two merge blockers.

The review found that “woke tonight because of own ability” can still be approximated from hardcoded role-name sets. That is not a durable semantic authority for dynamic scripts, role changes or future characters.

Target architecture:

```text
canonical night interaction history
-> actual wake/materialization event
-> wake reason = OWN_ABILITY / OTHER
-> Chambermaid query
```

Do not solve this by continuously expanding a role-name allowlist.

A future RED must distinguish:

- a role that normally wakes but did not actually wake tonight;
- a player woken by another character rather than their own ability;
- role changes/current-role state;
- dynamic-script roles not known to a hardcoded list.

## 8. GCR-5 — Durable identity and reconstruction API hardening

Two lower-priority review findings should be recorded rather than forgotten.

### 8.1 Night checkpoint target identity

Some checkpoint target representation uses player names. This is safe only if the active-game contract guarantees name uniqueness and immutability for the lifetime relevant to restore.

Before changing persistence schema, first add/locate an invariant proving the current assumption. If the invariant is not guaranteed, design a separate stable-seat identity migration rather than silently changing checkpoint JSON during GCR-1/2.

### 8.2 Reconstructor API naming

`NightTransactionReconstructor` exposes an `effectiveState` that can be mistaken for the state at the current checkpoint/cursor even when it represents a final reconstructed canonical night projection.

If confirmed by code audit, prefer a clarity rename such as `finalReconstructedNightState` in a dedicated low-risk refactor. Do not mix the rename into correctness GREEN unless necessary.

## 9. Test strategy additions

The global review recommends adding reusable test patterns rather than only more one-off examples.

### 9.1 Succession generation test

Exercise multiple generations and preserve historical role holders.

### 9.2 Dawn crash cut-point fault injection

Systematically simulate crash/retry around durable boundaries such as:

```text
before Death ActionFact
after Death ActionFact
after mechanical death
after public AliveAt
after RoleChange ActionFact
after role mutation
after PhaseAdvance ActionFact
after phase mutation
```

Each path should converge to the uninterrupted result with exactly-once history.

This is not required to unblock the first GCR-1 RED, but should replace hand-picked partial-state coverage where practical.

### 9.3 Knowledge-leak tests

For impaired information roles, verify both content legality and interaction-shape side channels.

### 9.4 Mutation-style invariants

Construct states that deliberately defeat invalid shortcuts such as:

```text
first Demon-team card
single role == Imp
setup role == current role
```

A correctness test should fail if those shortcuts are substituted for the real authority.

## 10. Scope boundaries

Do not expand GCR-1/2 into:

- generic custom-script Demon succession;
- Mayor redirect to arbitrary Demon with generic succession support;
- A3 setup-snapshot ownership;
- App-root S9.2 persistence extraction;
- A4/ZDD production promotion;
- recommendation tuning;
- history UI;
- broad misinformation redesign;
- unrelated UI cleanup.

The two merge blockers are deliberately narrow: **current Demon continuity** and **impaired Spy information integrity**.

## 11. Validation ladder

Use `docs/TESTING_STRATEGY.md`.

Expected order per blocker:

```text
RED / T0
-> focused assertion-level failure provenance
-> minimal GREEN
-> affected typed regression families
-> T1 :app:testFast
-> T2 affected integration/build checks as required
```

Before declaring the overall GCR merge-blocking follow-up complete, run the current T4 acceptance route:

```text
./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks
```

and require the repository’s applicable full CI semantic families, including ASP/Real Clingo when selected by the full route, plus R2 main-thread-boundary success.

Do not treat `UP-TO-DATE`, `FROM-CACHE`, skipped routes, or zero-job workflow runs as evidence that a required gate actually executed.

## 12. Exit conditions

The merge-blocking portion of GCR is closed only when:

```text
GCR-1 current Demon continuity          GREEN
GCR-2 impaired Spy information integrity GREEN
full affected regression                GREEN
T4 full checkpoint                      GREEN
R2                                      GREEN
```

GCR-3 may be completed in the same PR if changes remain narrow and low-risk; otherwise record remaining retirement debt explicitly.

GCR-4/5 may remain deferred if they do not block the validated PR behavior.

Even after all gates are green:

- do not automatically merge PR #54;
- do not automatically mark it ready for review;
- stop and report the accepted code checkpoint, later docs-only head if any, exact diff and remaining deferred items.
