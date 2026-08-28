# Next Development Handoff — Global Correctness Review Follow-up

> Date: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Status: **GCR-1 GREEN / GCR-2 FAIL-SAFE POLICY HARDENING / PR #54 remains draft and unmerged**

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
7. establish a typed RED before each behavior-changing correctness GREEN.

For explicit policy clarification where current production already matches the accepted behavior, do not manufacture an artificial RED. Add characterization/regression coverage first; if it exposes a real defect, then establish RED provenance for that defect before production GREEN.

Never merge, mark PR ready, rebase, force-push, or broaden PR #54 without explicit authorization.

## 3. Campaign name and priority

Use the prefix **GCR — Global Correctness Review Follow-up**.

```text
GCR-1  Current Demon authority / cross-night succession         GREEN / ACCEPTED
GCR-2  Poisoned Spy fail-safe information policy                ACTIVE / POLICY HARDENING
GCR-3  Typed production acceptance + source-string retirement   MEDIUM
GCR-4  Chambermaid actual wake-history authority                MED-HIGH / FOLLOW-UP
GCR-5  Durable identity + reconstruction API hardening          LOW-MED / FOLLOW-UP
```

Order is intentional. Do not start GCR-4/5 before GCR-2 is hardened and accepted unless a regression proves they are required to make the active slice correct.

## 4. GCR-1 — Current Demon authority — GREEN

### 4.1 Accepted result

After Imp succession, historical game state may legitimately contain both:

```text
old Imp: dead, current/actual role Imp
new Imp: alive, current/actual role Imp
```

GCR-1 established one canonical current-Demon authority rather than relying on shortcuts such as:

```text
first Demon-team card
single card whose role is Imp
```

Accepted correctness:

```text
Night N:
  Imp0 self-kills
  Minion A becomes Imp1

Night N+1:
  Imp1 acts normally

Later:
  Imp1 can self-kill
  Imp2 becomes the next current Demon
```

Historical dead Demon role identity remains represented correctly.

### 4.2 Accepted checkpoint

```text
974f617adffd08cc7de0924f6fea4f96f3d73f0c
```

Evidence:

```text
CI #959 / run 33174380352 SUCCESS
R2 run 33174380336 SUCCESS
```

The accepted Host GREEN routes Host actor selection, poison explanation and Demon-step materialization through the same current-Demon authority used by the mechanical path.

Do not reopen GCR-1 without a new typed regression.

## 5. GCR-2 — Poisoned Spy fail-safe information policy

### 5.1 Product decision

The earlier global review treated the poisoned Spy interaction-shape difference as a correctness defect because a poisoned Spy who wakes but sees no Grimoire can infer impairment.

That requirement is now intentionally superseded by a simpler product policy.

Product decision, 2026-08-28:

- the app will **not** build a fabricated/fake Grimoire generator for poisoned Spy;
- the app will **not** add a generic misinformation subsystem solely for this edge case;
- intentional Poisoner -> allied Spy targeting is considered outside normal expected play and is treated primarily as a rare/accidental target-selection state;
- in that state the app fails safe by withholding Spy information rather than attempting to simulate official poisoned misinformation.

This is an intentional product simplification / house-rule deviation from official poisoned-information semantics.

### 5.2 Required semantic contract

```text
healthy Spy
-> wake normally
-> show the true Grimoire
-> normal healthy Spy behavior remains unchanged

poisoned Spy
-> wake normally
-> show no Grimoire payload
-> do not expose the true Grimoire through tellPlayer, displayProposition, or another display path
-> Host may explicitly tell the Storyteller that the Spy is poisoned
-> do not create/persist a Spy Grimoire information observation
```

The interaction-shape difference is intentional and accepted. Future audits must not automatically classify it as a player-knowledge leak unless this product policy is deliberately changed.

### 5.3 Current production audit

Current Host production already substantially matches the chosen fail-safe policy in both first-night and other-night Spy materializers:

- poisoned Spy `tellPlayer` is `null`;
- poisoned Spy `displayProposition` is `null`;
- Host instruction explicitly says not to show the real Grimoire;
- healthy Spy still builds the real Grimoire and `InformationProposition.GrimoireState`.

Therefore GCR-2 does **not** currently require fabricated content or a semantic production rewrite.

### 5.4 Regression hardening

Minimum regression contract:

#### GCR-2A — healthy Spy unchanged

```text
healthy Spy
-> true Grimoire payload is available
```

#### GCR-2B — poisoned Spy fail-safe

```text
poisoned Spy
-> no Grimoire payload
-> no true Grimoire through an alternate display field
```

#### GCR-2C — no durable Spy information observation

```text
poisoned Spy
-> no Spy Grimoire information observation is created/persisted
```

Prefer typed/callable coverage when a narrow existing seam is available. Do not introduce a broad presentation-authority architecture solely to make these tests typed. A temporary coarse production-wiring guard is acceptable if needed and should be revisited in GCR-3.

If a regression exposes a real leak, establish focused RED provenance for that leak before changing production.

### 5.5 GCR-2 acceptance

GCR-2 is complete when:

1. healthy Spy true-Grimoire behavior is protected;
2. poisoned Spy has no Grimoire payload;
3. no alternate display path leaks the real Grimoire;
4. no poisoned-Spy Grimoire observation is durably recorded;
5. focused and affected regressions are green;
6. no fake-Grimoire/misinformation subsystem has been introduced.

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

This is follow-up scope after the active GCR-2 policy hardening.

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

Before changing persistence schema, first add/locate an invariant proving the current assumption. If the invariant is not guaranteed, design a separate stable-seat identity migration rather than silently changing checkpoint JSON during GCR-2.

### 8.2 Reconstructor API naming

`NightTransactionReconstructor` exposes an `effectiveState` that can be mistaken for the state at the current checkpoint/cursor even when it represents a final reconstructed canonical night projection.

If confirmed by code audit, prefer a clarity rename such as `finalReconstructedNightState` in a dedicated low-risk refactor. Do not mix the rename into correctness work unless necessary.

## 9. Test strategy additions

The global review recommends adding reusable test patterns rather than only more one-off examples.

### 9.1 Succession generation test

Exercise multiple generations and preserve historical role holders. GCR-1 now has accepted repeated-succession coverage; preserve it.

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

This is follow-up work and is not required for GCR-2 fail-safe policy hardening.

### 9.3 Knowledge-leak tests

For GCR-2, the accepted product contract is narrower than official misinformation semantics: poisoned Spy receives no Grimoire information. Test for true-content suppression and no durable observation; do not require interaction-shape equivalence or fabricated content.

### 9.4 Mutation-style invariants

Construct states that deliberately defeat invalid shortcuts such as:

```text
first Demon-team card
single role == Imp
setup role == current role
```

A correctness test should fail if those shortcuts are substituted for the real authority.

## 10. Scope boundaries

Do not expand GCR-2 into:

- fabricated/fake Grimoire generation;
- generic misinformation architecture;
- generic custom-script Demon succession;
- Mayor redirect to arbitrary Demon with generic succession support;
- A3 setup-snapshot ownership;
- App-root S9.2 persistence extraction;
- A4/ZDD production promotion;
- recommendation tuning;
- history UI;
- unrelated UI cleanup.

The active task is deliberately narrow: **lock the chosen poisoned-Spy fail-safe policy and verify that real Grimoire information cannot leak or persist.**

## 11. Validation ladder

Use `docs/TESTING_STRATEGY.md`.

For behavior-changing correctness work:

```text
RED / T0
-> focused assertion-level failure provenance
-> minimal GREEN
-> affected typed regression families
-> T1 :app:testFast
-> T2 affected integration/build checks as required
```

For GCR-2 policy hardening, because current production already substantially matches the newly accepted policy:

```text
characterization/regression test
-> focused execution
-> if GREEN and audit confirms coverage, no production rewrite is required
-> if RED exposes a real leak, establish defect RED provenance and apply minimal GREEN
```

Before declaring the overall GCR merge-blocking follow-up complete, run the current T4 acceptance route:

```text
./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks
```

and require the repository’s applicable full CI semantic families, including ASP/Real Clingo when selected by the full route, plus R2 main-thread-boundary success.

Do not treat `UP-TO-DATE`, `FROM-CACHE`, skipped routes, or zero-job workflow runs as evidence that a required gate actually executed.

## 12. Exit conditions

The GCR correctness follow-up is closed only when:

```text
GCR-1 current Demon continuity           GREEN / ACCEPTED
GCR-2 poisoned Spy fail-safe policy      GREEN / ACCEPTED
full affected regression                 GREEN
T4 full checkpoint                       GREEN
R2                                       GREEN
```

GCR-3 may be completed in the same PR if changes remain narrow and low-risk; otherwise record remaining retirement debt explicitly.

GCR-4/5 may remain deferred if they do not block the validated PR behavior.

Even after all gates are green:

- do not automatically merge PR #54;
- do not automatically mark it ready for review;
- stop and report the accepted code checkpoint, later docs-only head if any, exact diff and remaining deferred items.
