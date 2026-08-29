# CampBoardGameHost — Deferred A3 Setup Snapshot Ownership Handoff

> Refreshed: 2026-08-25 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Status: **DEFERRED / NOT STARTED**
> Current stable baseline at deferral: `5367603d2d7150e7ba88f19d061eb04f8da20aeb`
> Historical exact hardening H1–H7: **COMPLETE / GREEN / merged via PR #48**
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`

This handoff preserves only the remaining unfinished A3 architecture question. The old PR #48 draft/merge instructions and App-root S7 handoff state are historical and intentionally removed.

## 1. Completed A3/B4 foundation

The historical exact baseline is already hardened through H1–H7, including:

```text
H1  setup seed vs exactly-once durable observations
H2  state-aware historical ability eligibility
H3  mechanical convergence independent of provenance
H4  Trouble Brewing-only support guard
H5  immutable setup roles + dynamic currentRolesBySeat
H6  incremental state-aware historical replay
H7  knowledge-safe hidden mechanics integration
    - current-Demon attack branching
    - current-Monk protection branching
    - Other Night materialization
    - Imp self-kill succession
    - Mayor night-death branching
    - public/no-public-death reconciliation
    - hidden-action payload isolation
```

The production-isolated bridge remains:

```text
ClocktowerGameSession
-> GLOBAL action + observation history
-> ClocktowerB4HistoricalShadowCoordinator
-> B4DynamicPlayerWorldSetShadow(validated ruleset)
-> EnumeratedHistoricalExactBaseline
-> B4 shadow report only
```

This path does not own Host recommendation authority or A4/ZDD production selection.

## 2. Knowledge-safety contract that remains protected

Player-facing historical reasoning may consume only player-visible chronology, such as:

```text
PublicExecution
PublicDeath
PhaseAdvance
recipient-visible Observation
```

Persisted Storyteller-hidden facts such as:

```text
Poison
Protect
Attack
RoleChange
```

may exist in GLOBAL history, but actual hidden targets/occurrence details must not become player knowledge. Hidden mechanics used for possible-world replay must be regenerated from rules + possible-world state rather than consuming actual Storyteller-selected hidden payloads.

Also preserve:

- GLOBAL_V1 as durable chronology authority;
- mechanically identical worlds converge even if hidden provenance differs;
- `rolesBySeat` as immutable setup identity;
- `currentRolesBySeat` as historical current-role state;
- Trouble Brewing-only exact support remains fail-closed outside its validated boundary.

## 3. Remaining blocker — immutable setup snapshot ownership

Historical exact replay needs a trustworthy **setup origin** distinct from the mutable/current game snapshot.

Current session/persistence architecture stores the current snapshot plus GLOBAL history, but does not have an explicitly owned, durable immutable setup snapshot suitable as the exact replay seed across restore.

A future tests-first design must decide:

```text
1. Who owns setupSnapshot for a live Clocktower session?
2. Is setupSnapshot persisted explicitly?
3. How does restore recover the exact replay origin?
4. What happens to restored sessions lacking trustworthy setup provenance?
5. When should B4 return DEFER rather than infer/guess a setup?
6. Where, if anywhere, should a real runtime historical shadow invocation live after ownership is solved?
```

The preferred safety posture is fail-closed / defer when exact setup provenance cannot be established. Do not infer immutable setup identity from current mutable state merely to make B4 available.

## 4. Design constraints

Do not solve this by casually adding a second mutable state copy to `ClocktowerGameSession` or by inserting an unversioned JSON blob into persistence.

The design must explicitly address:

- setup snapshot immutability;
- game/session identity binding;
- persistence schema/version implications;
- restore compatibility and fail-closed behavior;
- current vs setup role identity after role changes/Demon succession;
- semantic-history identity/cursor compatibility;
- exactly-once observation chronology;
- no leakage of hidden Storyteller facts into player-world construction.

If persistence representation must change, treat that as a dedicated correctness/schema task, not as a side effect of structural decomposition.

## 5. Tests-first entry plan

When A3 setup-snapshot work is resumed, start with RED tests only.

At minimum characterize:

### RED A — live setup snapshot is immutable

After starting a game and later applying role/state changes, the setup snapshot must remain the original replay origin while current state evolves separately.

### RED B — restored session requires exact setup provenance

A persisted/restored historical-exact session must either:

```text
restore the exact immutable setup snapshot
```

or

```text
fail closed / DEFER_B4
```

It must not reconstruct setup by guessing from current roles, current cards, player count, or accumulated visible history.

### RED C — setup/current identity mismatch fails closed

If setup identity, current session identity, ruleset identity or semantic-history basis disagree, historical exact shadow evaluation must not proceed.

### RED D — role change does not rewrite setup identity

Demon succession/current-role changes must affect `currentRolesBySeat` without mutating `rolesBySeat` / setup snapshot identity.

### RED E — hidden facts remain knowledge-safe

Persisting an immutable setup snapshot must not make actual hidden action targets or Storyteller-private occurrence points visible to player-world construction.

## 6. Forbidden scope expansion

Do not combine setup-snapshot ownership with:

- the current Information Decision correctness hotfix;
- S9.2 App-root persistence decomposition;
- Host recommendation authority promotion;
- A4/ZDD selector promotion;
- other-script support;
- history UI;
- misinformation tuning;
- generic persistence cleanup;
- unrelated schema migration.

If setup snapshot requires a schema change, design and test that change explicitly rather than hiding it inside another task.

## 7. Validation expectations

Follow `docs/TESTING_STRATEGY.md` and escalate conservatively because this area touches persistence, timeline identity and historical replay authority.

Expected families include:

```text
focused A3/B4 setup/history tests
ClocktowerGameSession identity/history tests
persistence/restore tests if representation changes
historical exact replay tests
ASP / Real Clingo cross-validation when triggered
applicable full Android gate before merge
```

## 8. Hard STOP conditions

Stop and re-audit if the implementation would:

- infer setup from current mutable state;
- expose Storyteller-hidden targets to player knowledge;
- change GLOBAL chronology ordering/identity without a dedicated design;
- promote B4/A4/Host recommendation authority as a side effect;
- broaden historical exact beyond validated Trouble Brewing support;
- mix setup-snapshot schema work with unrelated S9.2 structural extraction.

## 9. Resume protocol

When this deferred task is selected again:

1. read root `AGENTS.md`;
2. read `docs/README.md` and `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff and `docs/TESTING_STRATEGY.md`;
4. query live `main` and current persistence/session architecture;
5. re-audit whether another intervening task has already established setup-snapshot ownership;
6. design the RED contract before any production/schema edit;
7. keep Host/A4 authority out of scope unless separately authorized.
