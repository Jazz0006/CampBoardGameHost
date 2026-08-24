# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Active branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Draft PR: **#48 `A3: historical multi-night exact baseline`**  
> Latest fully validated **code** checkpoint: `9eea8142c0dd04c2fdf8164c78b5a1799b0b7698`  
> Gates: **CI #666 SUCCESS / R2 #599 SUCCESS / Android + ASP + Real Clingo GREEN**  
> Current execution point: **A3 Architecture Hardening H1–H7 COMPLETE at the Trouble Brewing exact-baseline boundary; STOP before production authority / Host-A4-ZDD integration**  
> Detailed handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`

> Documentation-only commits may move the branch/PR head beyond the validated code SHA. New conversations must re-query live `main`, PR #48 head/state/checks before editing.

## 1. Project status

```text
Phase A correctness foundation                     PASS
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic prerequisites                          CLOSED
PR #39 Information Decision Foundation             CLOSED / MERGED
PR #40 Structured Manual UI                        CLOSED / MERGED
PR #42 Historical Action + Observation Capture     CLOSED / MERGED
PR #44 Drunk / Fortune Teller hotfix               CLOSED / MERGED
PR #43 Clocktower host decomposition A1–A13        CLOSED / MERGED
App-root decomposition S0–S6                       CLOSED / MERGED CHECKPOINT
App-root S7                                        PAUSED
PR #48 historical multi-night exact baseline       OPEN / DRAFT / ACTIVE
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
Production Host / A4 / ZDD authority integration   NOT STARTED / NOT AUTHORIZED
```

App-root S7 stays paused. Do not mix it into PR #48.

## 2. Protected architecture contracts

### H1 — one durable observation path
Historical construction separates setup seed knowledge from GLOBAL_V1 durable observation replay. Durable observations are consumed exactly once.

### H2 — order != eligibility
Night schedule owns canonical order. Current historical possible-world state and role semantics own actor eligibility. Triggered exceptions such as Ravenkeeper remain explicit.

### H3 — mechanical identity != provenance
Mechanically identical states converge even when hidden paths/explanations differ. Hidden branch count is not exact world count.

### H4 — support boundary
Historical exact construction is explicitly Trouble Brewing-only and fails closed for unsupported scripts.

### H5 — setup role vs current role

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic historical current-role state
```

### H6 — incremental state-aware replay
GLOBAL history is replayed incrementally. Visible night observations are validated against the mechanical world state at their own historical point. Hidden mechanics never invent synthetic `globalSequence` values.

### H7 — knowledge-safe hidden-mechanics replay
Storyteller-hidden choices may exist in persisted history, but player possible worlds reproduce hidden mechanics from rules + possible-world state and reconcile only recipient-visible public outcomes. Actual hidden targets and their occurrence points are not player constraints.

## 3. H7 completed checkpoints

```text
H7.1  dynamic current-Demon attack branching       GREEN
H7.2  dynamic current-Monk protection branching    GREEN
H7.3  mechanics materialization boundary           GREEN
H7.4  Imp self-kill succession branching           GREEN
H7.5  self-kill materializer integration           GREEN
H7.6  Mayor night-death branching primitive        GREEN
H7.7  Mayor materializer integration               GREEN
H7.8  canonical Other Night replay transition      GREEN
H7.9  public night-death reconciliation            GREEN
H7.10 no-public-death dawn reconciliation          GREEN
H7.11 persisted hidden-action payload isolation    GREEN
```

Latest checkpoints:

```text
H7.8 RED   938aeb42196ee886c5fffe142c3dc3a839d37c3c  CI #648 expected FAILURE
     GREEN 4bdf317ec16ab316331a2c322338446620e43631  CI #650 / R2 #583 GREEN
H7.9 RED   6fad4ed5f51590ab884af88437b711a494c2e3c9  CI #656 expected FAILURE
     GREEN a81b5949e00b18a24f4f1b0522a3f41aa892efa1  CI #658 / R2 #591 GREEN
H7.10 RED  28b56c83b3c8efbca570f3b8c3c40219b3076535  CI #661 expected FAILURE
      GREEN 3d446937b7cbef36b3fed679fff64b9582e450ac  CI #662 / R2 #595 GREEN
H7.11 RED  55fd836a7616d9e15a8f57691138ae3ecddc6567  CI #665 expected FAILURE
      GREEN 9eea8142c0dd04c2fdf8164c78b5a1799b0b7698  CI #666 / R2 #599 GREEN
```

Earlier H7.1–H7.7 provenance remains in git history and the PR discussion; no architecture contract from those slices is superseded.

## 4. Final Trouble Brewing historical exact pipeline

The hidden Other Night pipeline is rule-derived:

```text
possible world
-> all legal living-current-Monk protection alternatives
-> all legal living-current-Imp attack alternatives
-> resolve attack outcome
   NO_DEATH                         -> unchanged mechanical world
   TARGET_DIES                      -> death mechanical world
   IMP_SELF_KILL_SUCCESSOR_REQUIRED -> rule-derived successor world(s)
   MAYOR_TARGET_OR_REDIRECT...      -> rule-derived Mayor world(s)
-> H3 mechanical convergence
```

Historical replay places that transition at the canonical Imp boundary relative to visible observations. Ravenkeeper information requires proof of its death-trigger state. Compatible `PublicDeath` confirms/selects an already-derived hidden outcome without killing twice; a later public death proven by GLOBAL chronology is applied at its own point. A completed night with no `PublicDeath` supplies the current producer's public no-death evidence only when the night closes.

No known Trouble Brewing Other Night attack outcome remains unresolved at this exact materializer/replay boundary.

## 5. H7.11 persisted hidden-action isolation

`PlayerHistoricalTimeline` already projects only:

```text
PublicExecution
PublicDeath
PhaseAdvance
recipient-visible Observation
```

and deliberately projects persisted:

```text
Poison
Protect
Attack
RoleChange
```

to **nothing**. Their target payloads and hidden GLOBAL occurrence points therefore never enter player replay.

H7.11 replaced the obsolete fail-closed guard tests with positive invariance contracts.

RED `55fd836a7616d9e15a8f57691138ae3ecddc6567`:

```text
EnumeratedHistoricalExactBaselineTest.kt only
+221 / -60
production changes = 0
docs changes = 0
CI #665: 762 tests completed, exactly 2 failed
R2 #598 / ASP / Real Clingo GREEN
```

The two RED contracts prove:

1. two different persisted `Protect` + `Attack` target payloads must produce exactly the same mechanical player world set as a control history containing no hidden facts;
2. in a fixed 10-player world with two legal living Minion successors, persisted `RoleChange` selecting either successor must not collapse the two rule-derived Imp succession possibilities.

GREEN `9eea8142c0dd04c2fdf8164c78b5a1799b0b7698`:

```text
EnumeratedHistoricalExactBaseline.kt only
+5 / -21 from RED
RED tests unchanged
```

The explicit `Attack` / `Protect` / `RoleChange` rejection block was removed. No projection or replay logic changed. The KDoc now states the actual boundary: hidden facts may be persisted Storyteller truth, but hidden payloads and occurrence points are omitted before player reasoning; mechanics are regenerated from rules/world state.

CI #666 / R2 #599 / Android + ASP + Real Clingo all passed.

## 6. Knowledge-safety invariant after H7 completion

```text
full Storyteller GLOBAL history
-> validate shared timeline identity
-> PlayerHistoricalTimeline projection
   keep public/recipient-visible facts only
   discard hidden targets + hidden occurrence points
-> setup-only exact enumeration
-> rule-derived hidden branching at canonical boundaries
-> reconcile visible public outcomes
-> mechanical convergence
-> exact player world set
```

Persisted hidden truth is **allowed to exist** but is **not evidence for the player**.

Unsupported script boundaries remain fail-closed; H7.11 does not broaden exact support beyond Trouble Brewing.

## 7. Next phase — NOT STARTED / NOT AUTHORIZED

A3 Architecture Hardening has no remaining known H1–H7 correctness blocker at the current Trouble Brewing exact-baseline boundary.

The next work should begin with a **production integration readiness audit**, not immediate wiring. Re-audit current production authority/call sites and determine how the validated exact baseline should interact with Host/A4/ZDD rollout, caching and shadow/authority boundaries before assigning the next implementation slice or number.

Do not start or combine:

```text
production Host authority wiring
A4/ZDD promotion
other scripts
history UI / misinformation expansion
App-root S7
PR merge / ready-for-review transition
```

without explicit authorization.

## 8. Working discipline

For the next phase:

1. recheck live `main` and PR #48 head/state/checks;
2. compare any docs-only head back to code checkpoint `9eea8142c0dd04c2fdf8164c78b5a1799b0b7698`;
3. audit production integration boundaries before choosing the next tests-first slice;
4. do not consume Storyteller-hidden payloads as player knowledge;
5. keep App-root S7 separate;
6. do not merge, mark ready, rebase, force-push, or broaden scope without explicit user authorization.

Documentation should be updated automatically only at meaningful architecture/phase checkpoints; do not create a docs commit for every small code change.
