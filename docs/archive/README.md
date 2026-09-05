# CampBoardGameHost 历史文档归档

> 该目录中的文档只用于追溯设计演进、历史审计、阶段验收和已关闭 handoff。  
> **不得把 archive 中的 `PASS / COMPLETE / READY / NEXT` 当作当前开发状态。**

当前开发入口：[`../README.md`](../README.md) 与 [`../CURRENT_DEVELOPMENT_ROADMAP.md`](../CURRENT_DEVELOPMENT_ROADMAP.md)。

## 1. Directory layout

```text
handoffs/     closed or superseded NEXT handoffs
checkpoints/  completed implementation/test/checkpoint evidence
ui/           superseded UI plans and UI campaign closeout evidence
deferred/     unfinished but explicitly deferred future work
workflows/    superseded workflow instructions
```

Files directly under `archive/` are older consolidated closeouts, superseded design versions, or historical reports retained for traceability.

## 2. Handoffs

A handoff moves to `handoffs/` when its execution contract is completed, cancelled, or superseded by a new active handoff.

Even if the filename still contains `NEXT_DEVELOPMENT_HANDOFF`, it is not current. Only the single handoff linked from `../README.md` and `../CURRENT_DEVELOPMENT_ROADMAP.md` is active.

## 3. Checkpoints

`checkpoints/` contains historical slice-level implementation, audit and acceptance records such as completed MS/TBSP/UI checkpoints.

Their detailed SHAs, CI results and file lists remain useful evidence, but they should not be loaded by default in new development sessions.

## 4. Superseded UI plans

`ui/` contains UI plans/closeouts whose implementation campaign has completed or whose product assumptions were later changed.

In particular, pre-PR #100 plans that require a distinct WAKE acknowledgement state are historical. Current Night actor/wake behavior must be read from live code and the current roadmap/handoff.

## 5. Deferred unfinished work

`deferred/` is different from completed history: these files may describe unfinished future work, but current execution is not authorized until the roadmap explicitly reactivates it and a fresh live-state audit confirms the assumptions.

## 6. Superseded workflows

`workflows/` contains older process documents replaced by root `AGENTS.md` and current workflow/testing documents.

Do not resurrect an archived workflow merely because a historical handoff references it.

## 7. Older algorithm / architecture history

Older algorithm versions, previous recommendation implementation descriptions and historical A3/V4 acceptance reports remain archived for traceability. A historical PASS is evidence of that checkpoint only and does not override newer audits or current architecture.

## 8. Archive rule

Archive when any of these is true:

- a higher-version/current specification clearly supersedes the document;
- the corresponding implementation phase is complete and the file is now only evidence;
- an acceptance conclusion was superseded or revoked;
- a newer unified plan replaces the old path;
- the document mainly describes an implementation path that no longer exists;
- a handoff is unfinished but explicitly deferred.

Keep in active docs root when the document is a long-lived semantic/architecture/product/workflow authority or an active/future design that remains intentionally referenced.

Recommended lifecycle:

```text
active status / current handoff
-> docs root

completed slice evidence
-> archive/checkpoints or archive/ui

closed/superseded handoff
-> archive/handoffs

unfinished but deferred
-> archive/deferred

superseded process
-> archive/workflows
```

Any task resumed from archive must first re-query live repository state and re-establish ownership, scope and evidence requirements.
