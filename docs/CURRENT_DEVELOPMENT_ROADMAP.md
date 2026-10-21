# 当前路线（2026-10-21）

1. **最小完整游戏流程：已关闭。** #334/#335已合并；main `40050941efc3`，完整CI #4506与Field Test APK #157通过。[最小完整验收](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)。
2. **全仓遍历/大规模删除：最后两批实现完成，待本次精确远端验收。** 430/430路径有结论、0待审；215个生产Kotlin全部≤50000B。整个Draft #337相对main净删7635行/376286B、整删30文件。最终Cloud full639/144 suites/38 UI及APK、原R2/CI门禁检查成功。[清理收尾验收](MINIMAL_HOST_WHOLE_TREE_CLEANUP_ACCEPTANCE_2026-10-21.md)是本阶段唯一验收入口；逐路径证据在[审计台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md)。
3. **下一阶段：界面与流程细节。** 在清理的精确代码CI/R2通过后开展；先复核现行实际交互，再做小范围改进。Day提名/Slayer目标范围、提示文案与视觉属于此阶段，不重新开启第一阶段或无限审计循环。

唯一ClocktowerGameSession拥有游戏事实/进度/有序确认，TB+NGJ和现有Android AI（三模式、联合开局/续规划V3、receipt/model/会话延续/记忆/stale与凭据隔离）保留。D2预览仅UI，最终Next唯一Observation；隐私、AtomicFile原子/幂等保存及两项跨局防重复（五局shown阵容与三局同人起始身份/恶魔类别）保持。

没有新所有者决策阻塞。#337仍Draft，不合并/retarget/rebase/强推。真机/实际杀进程/付费API不在本次已验证范围。旧批次计数/CI、退休链与保留原因统一见审计台账和收尾验收，路线不再堆叠历史NEXT。
