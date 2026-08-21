# Design & Development Plan Audit

Date: 2026-08-21

> **Historical-status note:** Sections 1–4 preserve the project state and findings at the time this audit was written. The post-P1 production-rollout entry audit was completed later on 2026-08-21; §5 records the reviewed dispositions. For the current execution point, always use `CURRENT_DEVELOPMENT_ROADMAP.md`.

Scope: read-only audit of the design and development plan of **CampBoardGameHost** — an automatic Storyteller assistant for *Blood on the Clocktower* (血染钟楼) — based on all documents in `docs/` (+ `docs/archive/`), cross-checked against the source tree under `app/src/main/java/com/codex/campboardgamehost/clocktower/` and `tools/asp_oracle/`.

This document records findings only. It does not change any status, spec, or code. `CURRENT_DEVELOPMENT_ROADMAP.md` remains the single status authority.

---

## 1. What the project is and where it stands

**Design intent (finalized; v2.2 is the single normative spec):** a "Possible Worlds" epistemic engine that keeps *productive uncertainty* while respecting *earned advantage* and strict player-knowledge boundaries. Explicitly **not** a dynamic 50:50 win-rate balancer. Stated priorities: correctness > speed; transparency > optimization; player agency > manipulation.

**Phase sequence:** `A0 → A1 → A1.1 → A2 → A2.1 → A3 → A4 → A4.5 → R5.5 → R6 P1 (P1.1/P1.2/P1.3)` → *(next)* post-P1 production-rollout entry audit → production multi-night rollout.

**Authoritative current state** (per `CURRENT_DEVELOPMENT_ROADMAP.md`):
- Phase A, R5.5, and **R6 P1 (all three prerequisites) = CLOSED / PASS / MERGED**.
- Next step = **post-P1 production-rollout entry audit** — *not* production implementation.
- Production multi-night Possible Worlds, VerifiedExact Grimoire producer cutover, global-timeline cutover, and ZDD device validation are all **explicitly NOT AUTHORIZED**.
- Real-game field validation was scheduled **2026-08-22**.

**Design-vs-code reality:** ~80% of Phase A concepts are actively implemented and well tested (≈195 JVM tests, 52 golden-scenario contracts, 11 Python oracle tests). The engine packages (`epistemic/`, `domain/`, `recommendation/`, `flow/`, `catalog/`, `session/`) exist and match the docs. The top-level `player/`, `ui/`, `web/` directories are placeholders.

---

## 2. Findings

### F1 — Stale status in a same-day closeout doc (documentation hygiene) — priority: high
`docs/r6_p1_3_closeout_2026-08-21.md` §7 lists **P1.1 — OPEN** and **P1.2 — OPEN**, and §8 recommends "return to P1.2 next." But `docs/r6_p1_1_closeout_2026-08-21.md`, `docs/r6_p1_2_closeout_2026-08-21.md`, `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md`, and the roadmap all mark P1.1/P1.2 **PASS**. Not a true contradiction — P1.3 was written *before* P1.1/P1.2 were closed the same day — but a reader landing on the P1.3 doc gets the wrong current status. The README authority rule mitigates this but does not fix the doc.

### F2 — Superseded 2026-08-20 handoff instructions still read as live — priority: medium
`docs/r5_5_multiscript_progress_handoff_2026-08-20.md` instructs the next session to *continue on the old `codex/storyteller-algorithm-v4` long branch* and keep PR #2 open/Draft. The 2026-08-21 roadmap and handoff **explicitly override this**: create a new short-lived branch from latest `main`; do not use the old long branch. The correct direction is documented elsewhere, but the older doc lacks a "SUPERSEDED" banner.

### F3 — Revision-driven dynamic decision engine is only PARTIALLY unified — priority: medium (highest *technical* risk)
`docs/storyteller_revision_driven_dynamic_decision_engine_plan.md` wants one unified state/revision/decision model. In code, version tracking exists (`session/DecisionEventStore.kt`, `session/ClocktowerRecommendationCoordinator.kt`) but recompute logic is scattered across Compose call-sites in `MainActivity.kt`; setup-recommendation keys lack full context (poison/protection/alive/observation history); Drunk investigator pre-fetch can lock early. Risk: adding future roles/scripts will scatter dynamic rules across UI code and cause hidden state-version drift. This is production-code work gated behind the entry audit — it belongs in the roadmap's open-items list, not as a doc edit.

### F4 — "52 golden contracts" headline vs. 24 executable — priority: low
Docs headline "52 golden scenarios" (`docs/epistemic_reference_matrix.md`), but only ~24 are Clingo/oracle-executable first-night; ~28 are `ORACLE_NOT_APPLICABLE` (multi-night, deferred to B4). Accurate in detail, but the headline overstates validated coverage (~54% deferred). Worth a one-line caveat wherever "52" appears.

### F5 — ZDD (A4) fails its own latency gate; correctly kept in shadow — priority: low
`docs/storyteller_a4_zdd_prototype.md`: P95 build 114–161 ms vs the 50 ms ceiling (heap OK at ~6.3 MiB); the 6-player benchmark is disabled (256 MB OOM). Production default is `ENUMERATED_ONLY` — design and code agree. Not a defect, but the "next A4 decision" checklist (7 open items) has no owner or trigger date.

### F6 — Open design gaps flagged in prose but not tracked in the roadmap — priority: low
Several unresolved items live only in prose, with no roadmap entry: `StoryDisruptionRisk` thresholds unset (v2.2 §C4); `MANUAL_ONLY` vs `INELIGIBLE` UI text undefined; whether `globalSequence` must enter `PlayerWorldSetIdentity` once evaluation becomes time-aware (`docs/r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`); Drunk shown-role vs identity treatment. These risk being lost.

### F7 — Multi-script foundation is TB-only in practice — priority: low
`flow/ClocktowerFlowPlanner.kt` is script-aware (R5.5), but setup-recommendation metadata and some UI still hardcode Trouble Brewing (`config/TroubleBrewingRecommendationMetadata.kt`). No Greater Joy is scaffolded but unverified. Matches the documented known limitation — flagged so it is not mistaken for full multi-script support.

---

## 3. Recommended remediation (docs-only; no production code)

1. **F1:** Add a status banner to `docs/r6_p1_3_closeout_2026-08-21.md` §7/§8 noting P1.1/P1.2 were closed later the same day and pointing to the roadmap; retain the historical text.
2. **F2:** Add a one-line "SUPERSEDED BY 2026-08-21 roadmap/handoff — do not continue the old long branch" banner atop `docs/r5_5_multiscript_progress_handoff_2026-08-20.md`.
3. **F4:** Add a caveat ("24 oracle-executable; 28 deferred to B4 multi-night") next to the "52 scenarios" headline in `docs/epistemic_reference_matrix.md` and anywhere it is quoted.
4. **F3, F5, F6:** Add an "Open Items / Deferred Decisions" section to `CURRENT_DEVELOPMENT_ROADMAP.md` capturing: revision-engine unification, ZDD device-gate decision, StoryDisruptionRisk thresholds, MANUAL_ONLY UI text, and the globalSequence-in-identity decision — each with a trigger condition (e.g. "before production multi-night rollout").

None of these change the recorded status or any spec; they only add navigational/caveat banners and a tracking list. F3's actual fix is production work gated behind the entry audit.

---

## 4. Overall assessment

The project is unusually disciplined: a single normative spec (v2.2), a declared status authority, phase gates backed by tests and exact commit hashes, and explicit "NOT AUTHORIZED" production guardlines. Design and code agree on the large majority of Phase A concepts. The findings above are predominantly documentation hygiene; the only item with material engineering risk (F3) is already gated behind the planned entry audit.

---

## 5. Disposition after project review

The findings above are retained as the original audit record. The project review on 2026-08-21 accepted the hygiene findings but clarified several technical interpretations before incorporating them into the live roadmap.

### F1 / F2 — accepted

- Keep historical closeout/handoff text intact.
- Add a prominent historical-status / `SUPERSEDED` banner so an agent cannot mistake old instructions for the current execution point.

### F3 — accepted, but split into two layers

The technical risk is real, but it should not be implemented as one broad "revision engine refactor".

```text
F3a — production semantic-history ownership
      timeline/session authority, persistence, restore and migration
      → rollout dependency / first priority

F3b — revision-driven recommendation unification
      recompute triggers, complete context keys, coordinator ownership
      → follow-up after semantic-history ownership is stable
```

The post-P1 production audit found that `ClocktowerGameSession` / `GameSnapshot` already contain the correct global timeline authority, while production App/Compose still owns separate local counters and mutable observation state. Therefore ownership cutover must precede a larger recommendation recompute refactor.

### F4 — caveat accepted; percentage interpretation rejected

`52` is the **contract corpus size**, while `24` is the currently oracle/A3-executable subset and `28` are explicitly deferred / `ORACLE_NOT_APPLICABLE` cases. This should not be summarized as "only ~46% validated" or "54% unvalidated" because contract coverage and external-oracle execution are different dimensions.

Preferred wording:

```text
52 total golden contracts
24 currently oracle/A3 executable
28 deferred/non-oracle timeline or recipient-projection contracts
```

### F5 — accepted with an architecture trigger, not a calendar date

ZDD remains exact shadow/prototype. Reconsider promotion only after:

1. an exact historical multi-night baseline is production-correct;
2. realistic world sizes are known; and
3. the device latency/memory gate passes.

### F6 — accepted as a Deferred Decisions table

The roadmap should distinguish:

- a genuinely unresolved decision;
- a current explicit decision with a future reopen trigger.

For example, `globalSequence` is currently **not** part of `PlayerWorldSetIdentity`; reopen that decision only when historical timeline position changes world semantics.

### F7 — wording corrected

R5.5 did verify a real multi-script structural/flow foundation using Trouble Brewing and No Greater Joy. The remaining limitation is more precise:

```text
multi-script catalog / normalization / registry / flow / ruleset identity: VERIFIED
advanced recommendation metadata / Possible Worlds / role-specific epistemic semantics: TB-FIRST
```

No Greater Joy must not be described as merely an unverified scaffold, because doing so could cause future work to unnecessarily rebuild the already-proven multi-script foundation.

### Additional field-test issue incorporated

The existing legacy direct recommendation button/path is now tracked as **Production Recommendation Entry-Point Unification**, not cosmetic UI polish. It represents a second production decision entry path and must be removed after semantic-history/session ownership is established and before recommendation behavior is expanded into historical multi-night semantics.

The resulting current rollout order is recorded in `CURRENT_DEVELOPMENT_ROADMAP.md` and `post_p1_production_rollout_entry_audit_2026-08-21.md`.
