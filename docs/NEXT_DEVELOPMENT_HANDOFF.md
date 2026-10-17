# 当前开发交接

更新：2026-10-17（Australia/Sydney）。

## 当前检查点

准备基线 #315 `704f7b29`；独立 Recovery 删除 #316 `ef076287`；文字/整局归档删除 #317 `60f61c3b`。#316/#317 完整 CI/R2 已通过，三个 PR 仍未合并。

当前分支 `codex/reduce-runtime-state-copies` 基于 #317。第三阶段 C1 已完成幽灵票、Clocktower phase/round 单源投影；删除 journal 全量历史/玩家/setup 等请求副本、旧 Klutz 存档兼容、Session restore/游标分配/cache 和无消费者 CrossGameHistory。完整 JVM 1,362 项与 APK 构建通过；最终 FAST/远端状态以当前 PR 为准，不据此声称已合并或真机验证。

## 下一检查点 C2

[第三阶段实施与残留审计](STATE_COPY_REMOVAL_IMPLEMENTATION_2026-10-17.md) 列明下一步：App confirmed Monk/Mayor/attack 的有效值和失效语义、尚未生效的新恶魔选择、当日最高票/平票归属。不能仅取最后一条事件替代，因为现有记录未表达全部取消/失效/当时门槛。把必要规则责任归 Session，再删除 App 可写入口；不把整个 checkpoint 装入 Session，不添加兼容存档。

此后执行 D：按真实职责与理解半径审计 App/Host。二者仍约 240 KB/180 KB，大文件治理尚未完成。

## 缺口

仍无退出恢复、完整文字复盘或跨局完整归档；不读取、迁移或擦除旧设备数据。现有结构化动作/实际观察/正式裁定保留，但事件覆盖尚不完整，不能称 AI JSON 可直接恢复游戏。开局轮换历史和 Undercover 正常淘汰/轮数继续保留。没有真机或付费模型验收。
