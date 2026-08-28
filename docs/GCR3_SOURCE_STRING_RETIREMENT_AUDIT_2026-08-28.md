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

## 3. Current classification and status

| Test / source portion | Class | Status / action |
|---|---|---|
| `ClocktowerDawnDurableMaterializationProductionWiringTest` | B | Pending. Highest-priority callable seam candidate: `DawnCommitIntent + durable state -> ProductionDawnMaterializer`. Retire after typed production materialization coverage exists. |
| `ClocktowerGlobalObservationProductionWiringTest` | B/C | **Slimmed.** Retains only new-vs-restored history-mode ownership, App -> canonical session commit authority, and Host draft-vs-durable boundary. Statement ordering/revision/durability implementation details removed. |
| `InformationDecisionProductionAuthorityWiringTest` | B/C | **Slimmed.** Typed information-decision tests own confirmation/revision semantics; source guard now protects only Host publication through the canonical confirmation authority. |
| `ClocktowerDemonSuccessionProductionWiringTest` | C | **Slimmed.** Keeps App -> canonical succession resolver/planner ownership only. Callback-local projection details removed. |
| `ClocktowerHistoricalActionProductionWiringTest` | B/C | **Slimmed.** Retains one action timeline, canonical session commit authority, canonical save/restore/reset ownership, and presence of typed confirmed action drafts. Callback/order assertions removed. |
| `ClocktowerNightRestoreProductionOwnershipTest` | C | Pending. Keep one coarse Host -> `NightTransactionReconstructor` + restored checkpoint ownership assertion. |
| `ClocktowerSameNightEffectiveStateProductionWiringTest` | C | Pending. Retain only as a coarse non-callable Host production-consumer boundary; typed effective-state tests remain primary proof. |
| `ClocktowerMayorDemonExclusionWiringTest` | C | **Slimmed.** Typed Mayor legality/planner tests own behavior; source layer now protects only rules-owned legal target consumption. |
| `ClocktowerProductionOtherNightWiringTest` | C | **Slimmed.** Keeps planner/materializer ownership and absence of the legacy parallel ordering path; role/fact/order implementation mirrors removed. |
| source-wiring portion of `ClocktowerChambermaidSelectionAuthorityTest` | C | **Slimmed.** Typed resolver tests remain; production inspection is one coarse assertion that Host consumes `resolveChambermaidSelection`. |
| `ClocktowerNightTransactionArchitectureGuardTest` | C with A/D detail | **Consolidated.** Reduced to four coarse contracts: typed checkpoint owner, pure-vs-durable boundary, canonical Dawn planner owner, and Host checkpoint-backed death consumer. Callback-local projection and parameter-detail mirrors removed. |
| `ClocktowerCurrentDemonProductionWiringTest` | C temporary | Pending. Typed current-Demon tests own semantics. Consolidate or retire once the Host current-Demon presentation boundary becomes callable or the consolidated architecture guard covers the ownership invariant. |
| `ClocktowerPoisonedSpyFailSafePolicyWiringTest` | C temporary | Pending. Product-policy characterization only. Track explicitly for retirement; do not build a fake-Grimoire subsystem merely to eliminate this guard. |

## 4. Completed test-only cleanup checkpoints

```text
3ffc353d  docs: add GCR-3 retirement audit
c42b88b3  Chambermaid source guard slimming
27e894ae  Other Night source guard slimming
a04f9e9a  Demon succession source guard slimming
321ea100  Mayor source guard slimming
25e0d6db  InformationDecision source guard slimming
326ee38f  Global observation source guard slimming
eda18877  Historical action source guard slimming
84e808de  Night transaction architecture guard consolidation
```

No production file changed in these slices.

Validation checkpoint before the final three commits:

```text
25e0d6db
- R2 run 33176464188 SUCCESS
- CI Android FAST unit tests SUCCESS
```

The newest test-only head must still receive its own FAST acceptance before the next semantic or production slice.

## 5. Remaining GCR-3B queue

1. `ClocktowerNightRestoreProductionOwnershipTest` — already close to desired coarse form; retire details only if redundant.
2. `ClocktowerSameNightEffectiveStateProductionWiringTest` — inspect for typed replacement coverage and reduce to one consumer-boundary assertion if possible.
3. `ClocktowerCurrentDemonProductionWiringTest` — determine whether consolidated architecture coverage is now sufficient; otherwise retain one coarse Host authority call assertion.
4. `ClocktowerPoisonedSpyFailSafePolicyWiringTest` — retain only the explicitly accepted fail-safe policy boundary unless a real typed production presentation seam exists.
5. `ClocktowerDawnDurableMaterializationProductionWiringTest` — decide whether GCR-3C typed production seam extraction is worth doing.

## 6. GCR-3C — optional Dawn production seam

Only if the remaining Dawn source guard is still materially valuable after consolidation:

```text
DawnCommitIntent
+ current durable state
-> ProductionDawnMaterializer
-> thin App/Compose callback
```

This is a narrow seam extraction, not App-root decomposition.

## 7. Stop conditions

- No gameplay semantics change during source-test cleanup.
- No broad App/Host refactor solely to reduce source-string count.
- Do not merge or mark PR #54 ready.
- After each test-only slice, run focused affected tests and `:app:testFast` at the logical checkpoint.
