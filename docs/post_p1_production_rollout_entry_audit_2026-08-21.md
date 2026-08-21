# Post-P1 Production Rollout Entry Audit

Date: 2026-08-21

Status: **COMPLETE / READ-ONLY AUDIT**

Scope: production ownership, persistence, lifecycle and consumer readiness after R6 P1.1 / P1.2 / P1.3 semantic prerequisites closed. This audit does **not** authorize a broad Host/Compose cutover and does not claim historical multi-night Possible Worlds is production-ready.

Baseline audited:

- `main` after PR #22 docs closeout merge: `d56edd1552dc25dc73574c311179b9fe5a9d216b`;
- subsequent docs-only design audit commit: `46ec09c22f0e991cb4ad2a6a51e493c751255f1e`;
- P1.1 semantic close merge: `f77338bc85ae4a81b7e54e456b430e2f7f35c51a`.

## 1. Executive conclusion

The P1 semantic model is largely ready, but production ownership has not been cut over.

```text
P1 semantic contracts
        mostly ready
             ↓
production ownership / persistence cutover
        NOT YET COMPLETE
             ↓
historical multi-night engine
        still shadow / incomplete
```

The most important finding is that the correct shared timeline/session authority already exists in `ClocktowerGameSession` / `GameSnapshot`, while production App/Compose still owns separate mutable observation state and unrelated local counters.

Therefore the next production-rollout slice should **not** be Spy VerifiedExact, B4 productionization, ZDD promotion, or broad Host wiring. The first slice should establish an explicit production semantic-history mode and durable timeline ownership contract without changing Host/Compose behavior.

## 2. Current authority map

| Area | Current production authority | Semantic authority already available | Audit status |
|---|---|---|---|
| Spy Grimoire proposition | Host builds legacy `GrimoireState` directly from player cards | `SpyGrimoireTruthProjector` + `VERIFIED_EXACT` contract | LEGACY / NOT CUT OVER |
| Private observations | Host uses local `nightStepIndex` sequence | `TimelinePoint` + per-game allocator | LEGACY LOCAL |
| Public observations | App uses `clocktowerEventCounter` | same shared allocator | LEGACY LOCAL |
| Durable observation collection | mutable Compose/App list | `GameSnapshot.epistemicObservationLog` | PRODUCTION BYPASSES SESSION |
| Global timeline cursor | `ClocktowerNightCheckpoint.persistedValues()` already emits `clocktowerNextTimelineGlobalSequence`, but the production checkpoint constructor does not pass the live cursor and the restore map does not pass the JSON key back, so production effectively writes/restores the default `0` | `GameSnapshot.nextTimelineGlobalSequence` | EXISTING PERSISTENCE KEY / DISCONNECTED PRODUCTION WIRING |
| Action timeline | no general production-owned formal action history | `ActionFactTimeline` / Global binding | SEMANTIC ONLY / SHADOW CONSUMERS |
| A3 | exact enumerated correctness baseline | shared observation chronology support | NOT HISTORICAL STATE ENGINE |
| ZDD | exact shadow/prototype | shared chronology/filtering seam | SHADOW ONLY |
| B4 | isolated shadow | global action + observation chronology contracts | INCOMPLETE HISTORICAL ENGINE |
| Recommendation UI | mixed transitional paths including legacy direct entry | coordinator/revision-oriented path exists partially | DUAL ENTRY-POINT DEBT |

## 3. Spy Grimoire production truth audit

Current production Host contains two Spy Grimoire construction paths that directly build `InformationProposition.GrimoireState` from current player cards. They do not call `SpyGrimoireTruthProjector`, so they remain `LEGACY_DISPLAY_ONLY` producers.

Production does have useful fragments of truth, including actual role, shown role, alive/death state, Fortune Teller red herring state and Poisoner target state. However those fragments do not form one durable authoritative physical Grimoire ledger containing the complete physical character-token and reminder-token truth required by `VERIFIED_EXACT`.

Important migration caveat: legacy active-game restore can fall back from missing `clocktowerConfirmedPoisonTarget` to the older draft poison target. That compatibility behavior is acceptable for old saves, but it means an old save cannot automatically be promoted to exact physical `POISONED` reminder truth.

**Decision:** keep production Spy observations Legacy until a separate tests-first slice establishes an authoritative durable physical Grimoire source. Do not enable VerifiedExact by changing only the binding flag.

## 4. Global timeline production ownership audit

Current production chronology is fragmented:

- private recipient observations are recorded with local `nightStepIndex`;
- public alive/death observations use `clocktowerEventCounter`;
- observations are stored in an App/Compose mutable list;
- active-game persistence already has the key `clocktowerNextTimelineGlobalSequence` through `ClocktowerNightCheckpoint.persistedValues()`, but production does not connect the live cursor into that checkpoint constructor and does not include the JSON key in the restore map, so the persisted/restored checkpoint cursor collapses to its default `0`;
- there is still no explicit production semantic-history mode distinguishing LegacyLocal from a future Global-v1 game/session.

By contrast, `ClocktowerGameSession` already owns the correct semantic primitives:

```text
GameSnapshot.epistemicObservationLog
GameSnapshot.nextTimelineGlobalSequence
ClocktowerGameSession.allocateTimelinePoint(...)
ClocktowerGameSession.recordEpistemicObservation(...)
```

P1.2 also already provides safe timeline semantics:

- observations are explicitly `LegacyLocal` or `Global(TimelinePoint)`;
- a log cannot mix LegacyLocal and Global records;
- Global action and observation positions share one uniqueness space;
- no consumer is allowed to guess cross-type chronology from local phase/round sequences.

**Decision:** production must migrate ownership to the existing session/global authority rather than inventing another counter or patching individual Host callbacks independently. The existing `clocktowerNextTimelineGlobalSequence` persistence key must be reused and correctly wired; do **not** add a second cursor representation.

## 5. Historical consumer readiness

### A3 / EnumeratedWorldSet

A3 remains the transparent exact correctness baseline. It can replay observations in canonical chronology, but it is not a general historical state-transition engine. Shared chronology support is not equivalent to multi-night historical reasoning.

### ZDD

ZDD remains an exact shadow/prototype. Runtime policy still defaults to Enumerated and shadow results must not drive storyteller/UI decisions. Existing latency/device limits remain unresolved.

### B4

B4 remains deliberately isolated shadow infrastructure. It consumes validated action/observation chronology and can materialize supported history, but important action classes such as attack, protection and role change still defer rather than produce a complete production historical state.

**Decision:** do not promote A3/ZDD/B4 to production recommendation authority as part of the first rollout slice.

## 6. Selected first production-rollout slice

### Production Semantic-History Foundation

Introduce an explicit persisted game-history mode, conceptually:

```text
ClocktowerSemanticHistoryMode
├── LEGACY_LOCAL
└── GLOBAL_V1
```

The exact type/name may change during implementation review; the contract is the important part.

The first slice should be tests-first and should **not** modify production Host/Compose behavior.

Required contracts:

1. Existing v1/v2 saves with no semantic-history mode restore as `LEGACY_LOCAL`.
2. Never infer Global chronology from old local sequence data.
3. `GLOBAL_V1` is explicit in persistence.
4. Reuse the existing `clocktowerNextTimelineGlobalSequence` checkpoint/JSON key as the single production cursor representation; wire the live cursor into checkpoint creation and include that key in restore mapping rather than adding a parallel field.
5. Global mode durably owns/restores the cursor through that existing key and `GameSnapshot.nextTimelineGlobalSequence`.
6. Restored Global cursor must be strictly beyond every committed global position.
7. Global history containing a `LegacyLocal` observation fails closed.
8. Unknown/null mode or incompatible mixed payload fails closed.
9. The first foundation PR does not silently switch existing or newly created production games to Global; behavior remains unchanged until a later producer-cutover slice.
10. No Spy truth, A3/ZDD/B4 authority, Host flow, Compose UI or recommendation UI changes are included.

## 7. Recommended rollout order after the foundation

```text
1. Production Semantic-History Foundation
   explicit mode + reuse/fix existing durable cursor/session ownership contract

2. New-game Global observation ownership cutover
   allocate/commit semantic observations through ClocktowerGameSession

3. Production Recommendation Entry-Point Unification
   remove legacy direct recommendation button/path
   all recommendation entry points use one coordinator/authority

4. Historical action + observation capture
   share the same global allocator namespace

5. A3 historical multi-night exact baseline
   tests-first state-transition semantics

6. Authoritative physical Grimoire ledger
   then production Spy VERIFIED_EXACT cutover

7. B4 historical expansion
   only after unsupported action/state classes are implemented

8. Revision-driven recommendation unification
   centralize recompute/context ownership after semantic-history ownership is stable

9. Reconsider ZDD promotion
   only after exact multi-night baseline and realistic device gate pass
```

The Recommendation Entry-Point Unification stage is intentionally earlier than B4/ZDD. The existing legacy direct recommendation button is not merely cosmetic UI debt: it represents a second production decision entry path that can diverge as revision/context semantics become richer.

## 8. Guardlines

Until a later roadmap entry explicitly changes these states:

```text
Production VerifiedExact Spy producer: NOT AUTHORIZED
Production Global timeline producer cutover: NOT YET WIRED
Production historical multi-night Possible Worlds: NOT AUTHORIZED
B4 production authority: NOT AUTHORIZED
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
Legacy saves -> Global by inference: FORBIDDEN
Second/parallel timeline cursor persistence key: FORBIDDEN
```

Do not broaden the first implementation slice to production Host/Compose.

## 9. Exit decision

The post-P1 entry audit is **COMPLETE** because the current production authorities, missing seams and dependency order are now explicit.

The next actionable development target is the **Production Semantic-History Foundation** described in §6, using tests-first implementation on a new short-lived branch from the latest `main`.
