# CampBoardGameHost Historical Documentation Archive

> Historical evidence only. `PASS`, `COMPLETE`, `READY`, and `NEXT` inside this directory describe the state at the time the document was written; they are not current project status.

For current work, start with:

1. `../../AGENTS.md`;
2. [`../CURRENT_DEVELOPMENT_ROADMAP.md`](../CURRENT_DEVELOPMENT_ROADMAP.md);
3. the one active handoff named by the roadmap.

## Archive layout

```text
archive/
  handoffs/                 closed historical handoffs
  checkpoints/              one-time audits / acceptance checkpoints
  completed-campaigns/
    ms-setup/               completed multi-script setup campaign evidence
    tbsp/                   completed Trouble Brewing setup-preset campaign evidence/contracts
    same-night/             detailed completed same-night architecture/transaction documents
    r5-5/                   completed R5.5 closeout material
  superseded-workflows/     replaced development/editing workflows
  superseded-designs/       implementation plans replaced by newer authorities
  deferred/                 unfinished work that is paused, not cancelled
```

Older archive material that predates this structure may remain directly under `archive/`; it is still historical evidence.

## Rules

- Archive is not a deletion mechanism: useful design history, regression evidence, and old decisions remain available.
- Do not resume a deferred file directly. First re-query live `main`, read the current roadmap, and explicitly reactivate the work.
- If an archived document conflicts with a current authority, the current authority wins.
- Old PR numbers, SHAs, test counts, branch names, or status labels are historical snapshots only.
- When a current campaign closes, prefer moving its micro-checkpoints/handoffs here in one docs-only batch rather than letting active `docs/` accumulate stale `NEXT` instructions.
