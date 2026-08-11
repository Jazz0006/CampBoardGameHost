# External solver evaluation and reference freeze

> Project: CampBoardGameHost / Trouble Brewing epistemic engine  
> Milestone: Phase A / PR A0  
> Freeze date: 2026-08-11  
> Status: implementation baseline

## 1. Purpose

This document freezes the external references used to design and cross-validate the player-perspective possible-world engine. A frozen implementation is a test oracle, research reference, or runtime prototype input; it is **not** an official rules authority.

Authority order for resolving disagreements:

1. official Blood on the Clocktower rules, almanac, role text, and published rulings;
2. an explicit CampBoardGameHost product policy or documented house rule;
3. independently agreeing formal implementations;
4. a single external implementation;
5. current CampBoardGameHost behavior.

Any unexplained disagreement at levels 1–3 blocks automatic rollout for the affected interaction and must be recorded as a known variance.

## 2. Frozen software references

| Reference | Frozen revision | License at revision | Intended use | Explicit non-use |
|---|---|---|---|---|
| `pnkfelix/botc-asp` | `616e61b720cc853af031f2623fd6bde33b869865` | dual MIT / Apache-2.0 (`LICENSE-MIT`, `LICENSE-APACHE`) | development-time formal oracle; SAT/UNSAT, legal-output and registration cross-validation | not an Android production dependency; not an official rules source |
| `olarozenfeld/botc` | `fc919f19356f78aa9fd22f036f5fe63257d7fde8` | Apache-2.0 | perspective, game-log, assumptions and mechanically possible-world domain reference; secondary oracle where its Trouble Brewing coverage applies | no probability semantics; no direct production dependency |
| `pnkfelix/botc-zdd-` | `0bbe6fa07afe84ab506e772315d0f7edc305939d` | MIT | ZDD runtime feasibility prototype; incremental observation, count, undo and browser/mobile performance reference | not selected as production engine until A4 device gates pass |

Repository links:

- <https://github.com/pnkfelix/botc-asp/tree/616e61b720cc853af031f2623fd6bde33b869865>
- <https://github.com/olarozenfeld/botc/tree/fc919f19356f78aa9fd22f036f5fe63257d7fde8>
- <https://github.com/pnkfelix/botc-zdd-/tree/0bbe6fa07afe84ab506e772315d0f7edc305939d>

### 2.1 Reuse decision

- A0 imports no third-party source code.
- A1–A3 should define CampBoardGameHost-owned semantic DTOs and adapters, then invoke or compare against external tools at the boundary.
- If code is copied or adapted later, the change must identify the source path and revision, preserve the applicable copyright/license notices, and update the app's third-party notices.
- For `botc-asp`, choose one permitted license consistently for any copied portion; do not silently mix license obligations.
- Blood on the Clocktower names, role text, art, and trademarks are separate from the open-source software licenses. This evaluation grants no rights to redistribute game assets.

## 3. Frozen research references

| Reference | Frozen version | What is adopted | What is deferred |
|---|---|---|---|
| Meng & Lucas, *Deduction Game Framework and Information Set Entropy Search* | arXiv `2407.21178v1`; IEEE CoG 2024 DOI `10.1109/COG60054.2024.10645614` | information-set definition; entropy metrics; budgeted, reproducible sampling as a later search technique | maximizing information gain as storyteller objective; multi-step search before Phase D |
| Xu, Meng, Verbrugge & Lucas, *CSP4SDG* | AAAI-26 proceedings, vol. 40; arXiv `2511.06175v1` | hard mechanically possible worlds separated from future weighted soft evidence | dialogue-derived weights and posterior-like belief overlay before Phase E |

Paper links:

- <https://arxiv.org/abs/2407.21178>
- <https://doi.org/10.1109/COG60054.2024.10645614>
- <https://ojs.aaai.org/index.php/AAAI/article/view/38453>
- <https://arxiv.org/abs/2511.06175v1>

Research papers are design references only. A0 does not copy paper code, figures, datasets, or substantial text.

## 4. Capability evaluation

| Capability | `botc-asp` | `olarozenfeld/botc` | `botc-zdd-` | CampBoardGameHost decision |
|---|---|---|---|---|
| Trouble Brewing setup constraints | primary oracle | secondary check | runtime prototype | cross-validate A3/A4 against ASP |
| Player-perspective mechanically possible worlds | supported through constraints/models | core design | incremental observer/world count | expose only through `PlayerWorldSet` |
| Exact SAT/UNSAT | yes | yes for supported log model | yes within encoded model | exact layer only; never sampling |
| Multiple legal registrations | explicit ASP branches | coverage-dependent | ZDD branches | represent as complete candidate facts |
| Multi-night timeline | supported | game-log based | observation API includes N2/day | add only after B1 first-night shadow mode |
| World count | model enumeration | enumeration | native ZDD count | baseline exact counts first, optimize later |
| Incremental require/exclude/undo | solver rerun/constraints | assumptions/log rerun | native observer/undo | interface must not expose engine nodes |
| Android suitability | no | no | promising but unproven | A4 measures P50/P95, memory and node count |
| Rules authority | no | no | no | official sources remain authoritative |

## 5. Oracle protocol for A2 and later

Each golden scenario must have a stable ID and declare:

```text
scenario input
perspective seat and knowledge boundary
timeline snapshot
queried proposition or candidate observation
expected SAT / UNSAT or legal output set
official-rule rationale
oracle adapters capable of evaluating it
known coverage limits
```

Comparison result states:

```text
AGREE
EXPECTED_COVERAGE_GAP
KNOWN_ORACLE_VARIANCE
ORACLE_NOT_APPLICABLE
UNEXPLAINED_MISMATCH
NOT_RUN
```

Rules:

- `UNEXPLAINED_MISMATCH` fails the cross-validation gate.
- `EXPECTED_COVERAGE_GAP` requires an explanation and does not count as independent agreement.
- `KNOWN_ORACLE_VARIANCE` records an explained conflict with official rules and preserves the official expectation.
- `ORACLE_NOT_APPLICABLE` records that an official contract cannot be faithfully expressed by that Oracle/adapter; it is not `NOT_RUN`.
- Enumeration or sampling limits may produce `NOT_RUN`; they may never be translated to `UNSAT`.
- Scenario serialization, solver versions, command line, timeout and frozen commit must be included in generated reports.
- Oracle output must never be shipped to the player-facing UI as an official ruling.

## 6. Evaluation gates

PR A0 is complete when:

- the three repositories and two research works are frozen above;
- license and trademark boundaries are explicit;
- at least 20 Trouble Brewing scenarios exist in `epistemic_reference_matrix.md`;
- every scenario has an official-rule rationale and at least one planned formal validator;
- difficult cases identify whether they test legality, perspective consistency, registration, malfunction, or timeline state.

Before PR A2 is complete:

- frozen references must be fetched reproducibly;
- adapters must not require the Android production runtime;
- all runnable golden scenarios must emit machine-readable comparison results;
- every mismatch must be classified before merging.

## 7. Update policy

Reference upgrades are deliberate changes, not automatic dependency bumps.

1. propose the new revision and summarize semantic changes;
2. run all existing golden scenarios on old and new revisions;
3. classify every output difference;
4. add scenarios for newly discovered edge cases;
5. update this file and the reference matrix in the same change;
6. keep historic reports tied to their original frozen revisions.
