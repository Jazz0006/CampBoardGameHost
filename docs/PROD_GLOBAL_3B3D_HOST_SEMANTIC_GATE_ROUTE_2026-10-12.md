# PROD-GLOBAL-3B3D — Host semantic acceptance boundary, staged implementation

> 2026-10-12 · 3B3D-1 IMPLEMENTED (awaiting CI), 3B3D-2 NOT IMPLEMENTED. This is an experimental validator; **not** a production cutover or an independent TB rule-legal verdict.

## 1. Exact ownership and live starting point

- 3B3 upstream PR #300 (`6c2286b7b9ee4c4c6baa960c200f83edf59b7056`) GREEN CI #4114 / R2 #3736, Draft; depends on 3B2 Draft #299 (CI #4112/R2 #3734 GREEN). New branch `experiment/prod-global-3b3d-host-semantic-gate-20261012` starts from #300 and must remain isolated until independently accepted.
- Active authority: `InitialGlobalOpeningScopeV1` and `InitialGlobalOpeningSelectionV1.validate` own precommit branch, complete required current-choice domain, deferred decision inventory, Host legal bluff domain and version/roster fingerprint. Production `FirstNightOneShotRequestV1` is still postcommit and unchanged.
- `FirstNightNumericInformationSemantics`, `FirstNightNumericInformationDisplayDomain`, rules-owned `NaturalPairInformationCandidateGenerator` and existing publication lifecycle remain substantive truth/legality owners; they are **not** replaced by V2.
- No remote or personal API request and no change to Android UI/provider model selection occurs in this branch.

## 2. 3B3D-1: strict semantic gate (experimental)

Implement `session/HostOpeningSemanticGateV2.kt`. The model sees a compact `decisionInventory` of *logical IDs only*, NOT candidate outcome menus. It copies an identity containing `gameId`, `gameStateRevision`, `playerInputRevision`, and `rosterFingerprint` and the separate `asOfRevision`. Identity and logical decision IDs must agree with current Host truth; changing games or stale revision invalidates the whole result.

A Host-side (not model-produced) `HostOpeningSemanticProjectionV2` maps **all legal candidate IDs in the chosen conditional branch** to their unique typed public observation: source seat, shown/effective ability, basis, subject mode and seats, and typed Number/Role/Boolean result. The projection constructor fails if coverage is incomplete, candidate IDs differ from `InitialGlobalOpeningScopeV1`, or public meanings are ambiguous. Real production projection must be derived from the underlying rules owners, **not** decoded from the model or from its prose.

`HostOpeningSemanticGateV2.accept` is fail-closed: strict protocol fields; bounded arrays; revision and roster identity; candidate semantics equality; role references and source-seat binding; one complete current discretionary choice per Host decision; no unexpected extra, duplicate, unknown or missing decision; only registered Host red-herring legal choices and Host bluff domain; all pending dependencies exactly match the authoritative branch; no future player selection may be invented. It returns a validated **private proposal**, including the 3B1 Host selection and separately held `CONDITIONAL_INTENT` values that have not become observations. The method neither commits, publishes, chooses a registration witness nor silently substitutes an arbitrary outcome.

The original V2 experimental schema/input have been extended with request identity and logical decision IDs; experimental 8-player fixtures use an explicitly generated offline-only identity rather than purporting to be a real live Host `rosterFingerprint`. This is a **new** V2 experiment request. The old 3B2 real raw outputs are frozen and do not meet the new V2 format; do not rewrite or score them as if they do.

Tests under `session/HostOpeningSemanticGateV2Test.kt` use fabricated, explicitly labeled Host candidate maps. They prove **interface boundary logic**, not whether any actual TB clue, registration or potential drunk assignment is rules legal.

## 3. 3B3D-2: mandatory independent Host rules integration — NOT YET DONE

Before claiming `HOST_SEMANTIC: PASS` for real plans:

1. Build a typed semantic projection from the *actual current* Host-generated legal first-night candidate domains, conditional on every legal Drunk assignment. Construct these domains using the existing rule owners; handle duplicate registration witnesses by matching the **public observation** rather than manufacturing a unique witness or taking a first candidate as an arbitrary fallback.
2. Reuse the Host's real red-herring and off-board demon bluff legality, ability functioning/poison/drunk/registration ambiguity, known-deterministic numeric domains, character pairing, and event-time barriers. Do not add an E02/E03/E04 role-seat patch or new heuristic scorer.
3. Create a complete decision inventory for all currently applicable first-night information and required actions, including deterministic Host-computed results, real deferred actions, and any `CONDITIONAL_INTENT`; require full coverage. Feed the exact same input revisions/fingerprints into generation and acceptance. A changed `Poisoner` target invalidates the pending proposal and reevaluates before display, but previously published observations remain immutable.
4. Exercise E02–E05 **frozen raw V1** in a separate read-only diagnostic, not against the V2 parser; create new **synthetic** Host semantic parity fixtures in Android and later genuinely new Luna V2 blind cases. Structure, Host rules, strategy, and production/device acceptance remain four separate axes.
5. Only after Host-native semantics, full CI/R2, bounded real Luna study, and phone/manual takeover/no-double-publication tests should production move to the precommit one-shot architecture. Retire replaced production API only at that cutover, no obsolete compatibility shadow.

## 4. Test and merge boundary

- Use Mini MCP direct code reading/edits; GitHub full CI and R2 as independent Android gates (local `clocktower` generic `run_task test/quality` are not allow-listed).
- PR for 3B3D-1 should target the 3B3 PR #300 branch. It may be merged as *isolated experimental code* if tests pass and review agrees, not as an acceptance of the full 3B3D rules adapter or a live AI release.
- No paid API calls until Mini MCP controlled EXPT explicitly provides real paid execution, reviewed per-call timeout, max calls/tokens, total cost, and private result storage.
