# GCR-3 Source-String Retirement Audit

> Date: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Parent acceptance checkpoint: `474103ed13caaf34a329ca5e80e2f0ba64963b86`

## 1. Entry condition

GCR-1 and GCR-2 blocker acceptance is complete at `474103ed13caaf34a329ca5e80e2f0ba64963b86`.

Full acceptance evidence:

```text
CI #963 / run 33175600756 SUCCESS
- Android :app:testFull + :app:assembleDebug SUCCESS
- ASP contract tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS

R2 run 33175600749 SUCCESS
```

GCR-3 is test-quality cleanup. It must not change accepted gameplay semantics merely to retire source inspection.

## 2. Classification rule

```text
A. typed replacement already proves behavior -> retire source-string test
B. behavior matters but production boundary is not callable -> introduce narrow typed seam, then replace
C. architecture/ownership-only invariant -> keep coarse source guard, remove gameplay/detail/order assertions
D. obsolete implementation-shape assertion -> delete
```

## 3. Current classification

| Test / source portion | Class | Action |
|---|---|---|
| `ClocktowerDawnDurableMaterializationProductionWiringTest` | B | Keep temporarily. Highest-priority callable seam candidate: `DawnCommitIntent + durable state -> ProductionDawnMaterializer`. Retire after typed production materialization coverage exists. |
| `ClocktowerGlobalObservationProductionWiringTest` | B/C | Typed session tests prove global commit semantics, but App save/restore/commit routing remains non-callable. Slim to durable ownership only; do not retain statement ordering and local-variable assertions. |
| `InformationDecisionProductionAuthorityWiringTest` | B/C | Typed information-decision tests prove confirmation/revision semantics. Keep only a coarse Host publication-authority guard until publication is callable. |
| `ClocktowerDemonSuccessionProductionWiringTest` | C | Typed succession/planner tests own semantics. Keep only coarse App -> canonical succession/planner ownership; remove callback-local implementation details. |
| `ClocktowerHistoricalActionProductionWiringTest` | B/C | Typed action/session tests own timeline semantics; App persistence/routing still needs a coarse boundary guard. Remove exact callback/order assertions. |
| `ClocktowerNightRestoreProductionOwnershipTest` | C | Keep one coarse Host -> `NightTransactionReconstructor` + restored checkpoint ownership assertion. |
| `ClocktowerSameNightEffectiveStateProductionWiringTest` | C | Retain only as a coarse non-callable Host production-consumer boundary; typed effective-state tests remain primary proof. |
| `ClocktowerMayorDemonExclusionWiringTest` | C | Typed Mayor legality/planner tests own behavior. Keep only rules-owned candidate-set consumption at Host/UI boundary. |
| `ClocktowerProductionOtherNightWiringTest` | C | Typed flow tests own role ordering/facts. Keep only planner/materializer ownership and absence of the legacy parallel ordering path. |
| source-wiring portion of `ClocktowerChambermaidSelectionAuthorityTest` | C | Typed resolver tests remain. Reduce production source inspection to one coarse assertion that Host consumes `resolveChambermaidSelection`. |
| `ClocktowerNightTransactionArchitectureGuardTest` | C with A/D detail | Retain as the consolidated ownership guard, but remove gameplay-result, local-variable and callback-order assertions already covered by typed reducer/planner/integration tests. |
| `ClocktowerCurrentDemonProductionWiringTest` | C temporary | Typed current-Demon tests own semantics. Consolidate or retire once the Host current-Demon presentation boundary becomes callable or the consolidated architecture guard covers the ownership invariant. |
| `ClocktowerPoisonedSpyFailSafePolicyWiringTest` | C temporary | Product-policy characterization only. Track explicitly for retirement; do not build a fake-Grimoire subsystem merely to eliminate this guard. |

## 4. GCR-3 execution slices

### GCR-3A — safe slimming, tests only

Start with guards where typed semantics are already strong and source assertions are obviously over-specific:

1. Chambermaid source-wiring portion;
2. Other Night production wiring;
3. Demon succession production wiring;
4. Mayor Demon-exclusion production wiring;
5. InformationDecision production authority wiring.

No production edits in this slice.

### GCR-3B — persistence / transaction ownership consolidation

Slim or consolidate:

- global observation production wiring;
- historical action production wiring;
- night restore ownership;
- same-night effective-state wiring;
- night transaction architecture guard;
- temporary current-Demon / poisoned-Spy guards.

Preserve unique App/Host ownership protection where no callable seam exists.

### GCR-3C — optional Dawn production seam

Only if the remaining Dawn source guard is still materially valuable after consolidation:

```text
DawnCommitIntent
+ current durable state
-> ProductionDawnMaterializer
-> thin App/Compose callback
```

This is a narrow seam extraction, not App-root decomposition.

## 5. Stop conditions

- No gameplay semantics change during source-test cleanup.
- No broad App/Host refactor solely to reduce source-string count.
- Do not merge or mark PR #54 ready.
- After each test-only slice, run focused affected tests and `:app:testFast` at the logical checkpoint.
