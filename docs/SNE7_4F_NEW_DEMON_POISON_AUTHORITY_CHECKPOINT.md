# SNE-7.4F new-Demon Dawn poison-authority checkpoint

Status: focused GREEN; broad validation pending.

- RED: `84643d5bb12583ad65f688cae3215a70df9efa2c`
  - CI #840: 901 tests, exactly 1 intended failure, 4 skipped.
  - Intended failure: `ClocktowerNewDemonPoisonAuthorityProductionWiringTest.common new Demon Dawn commit does not recompute poison after planner intent`.
  - Real Clingo cross-validation: GREEN.
  - R2 #767: GREEN.
- Production: `b4bf9379db4de3f8fb7dc152fd93db088f857df0`
  - `onConfirmNewDemon` keeps planner `DawnCommitIntent.poisonCarry` authoritative for the planner-backed succession path.
  - Legacy `PoisonEffectLifecycle.afterNight()` handling remains available only in the non-planner path.
  - Durable `poison-after-night` ActionFact recording remains App-owned and was moved with the legacy block rather than rewritten.
  - Common Dawn commit retains shared cleanup / phase advance / revision / night-flow reset only.
- Focused SNE-7.4F poison-authority suite: GREEN.
- Exact RED-to-GREEN audit: ahead by exactly one commit; only `CampBoardGameHostApp.kt` changed.

PR #54 remains draft and unmerged. This checkpoint exists to trigger normal broad CI/R2 for the bot-authored production commit.
