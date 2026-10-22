# Host 方桌视觉合同（2026-10-22，长期原则）

> **当前批准的五批合同**：[Host 方桌与信息角色完整规划](HOST_TABLE_AND_INFORMATION_REBUILD_2026-10-22.md) 已从旧 Draft #342 移入主线集成分支。共享卡片视觉 Stage 1 [#343](https://github.com/Jazz0006/CampBoardGameHost/pull/343) 已合并 main `78d2b115`（Android CI #4543 / R2 #4110）；Numeric/Pair Resolver Stage 2 [#344](https://github.com/Jazz0006/CampBoardGameHost/pull/344) 已合并 main `d3a75c99`（Android CI #4544 / R2 #4111）。**Resolver 尚未接入 UI**；下一步 Stage 3 Chef/Empath 数字界面，Stage 4 Pair，Stage 5 其余角色与真机验收。#341 酒鬼方桌的旧 SeatState 仍需迁移，真机视觉仍未验证。

## 唯一设计原则

**方桌是始终稳定的说书人工作区；昼夜主题决定环境颜色，真实阵营决定玩家卡片底色，当前操作只改变边框、标记和交互状态。**

1. 正式游戏 **Day** 一律 Light Theme：白色背景、深色文字；**FirstNight/Night** 一律 Dark Theme：接近黑色背景、浅色文字。由唯一 `ClocktowerGameSession/ClocktowerSessionState.phase` 决定，不因手机系统主题、UI 草稿或额外 phase flag 而变化。白天提名、投票、处决、艺术家、猎手、呆瓜等子界面共用日间主题入口；夜晚各角色共用夜间主题入口。游戏中的“主持工具”和“结束游戏”弹窗也随当前 Session 阶段切换，尚未创建正式 Session 时默认深色。
2. 说书人私有方桌的玩家阵营色使用 **Session 的 `PlayerState.actualAlignment`**：GOOD 蓝 `#24568D`，EVIL 红 `#8F2D3D`，卡片主文字白色。善良阵营包括镇民及外来者；邪恶包括爪牙及恶魔。酒鬼以真实善良身份显示蓝色，间谍或隐士登记裁量、对玩家展示的身份/线索不改变底色；只有已确认的真实阵营变化才改变颜色。
3. 当前唤醒者、可选/已选目标、选中顺序、死亡、中毒和幽灵票通过 **边框、序号、标记** 显示，不因选择状态改变红蓝底色；让颜色不成为辨认交互状态的唯一手段。卡片颜色为持续背景状态而非游戏事实写入点。
4. 目标对比度：通常大小文字 >= 4.5:1，大文字 >= 3:1；选择与禁用状态也要保证真实姓名/身份可读。避免深背景继承浅主题墨色，或用透明度降低玩家姓名对比度；全屏预览时只展示获准给该玩家看的线索，不显示说书人私有红蓝阵营。
5. 5–15 人 TB、5–6 人 NGJ、狭窄竖屏（含 360×520）要仍可完整显示座位与中央控件。调整颜色时不得改变座位物理编号、交互/手势、D2 预览→Next 唯一正式 Observation 与 Session 游戏流程。

## 工程边界与替换规则

- 唯一 Session 的游戏事实 → `HostSeatPresentation` / `ClocktowerSquareTableSeatUiModel` **只读投影** → 唯一 `ClocktowerSquareTableSeat` 渲染。不在 UI 内另造可写 GameState/实际阵营或基于展示角色、侦查登记推测阵营。
- 白天/夜晚仅存在一个共享 MaterialTheme+Surface 配置入口，提供不同 `ColorScheme`；所有日间子页面通过 `ClocktowerDayTableScaffold`，所有夜间步骤通过既有 Night Host。未知阵营（发牌前）才使用中性卡片，不把玩家阵营标签公开到传手机/预览界面。
- 替换原有按选中状态覆盖卡片颜色的唯一调色路径，完成消费者迁移就删除旧实现；禁止长期保留并行主题、并行卡片着色或第二套游戏状态。
- 这条原有的“第一批主题/第二批阵营卡片”属于旧分类，**已由 2026-10-22 新五批重建方案取代**。现行 Stage 1 为统一卡片视觉，Stage 2 为 Numeric/Pair 只读 Resolver，Stage 3 为 Chef/Empath 数字 UI。酒鬼方桌选择在独立 Draft #341；不擅自将不同 PR 的代码覆盖或合并。D2 预览与 Next 边界始终不变。
- 验收以真实代码/构建及高价值交互为主：日夜配色、红蓝文字对比、信息不泄漏、交互边框及死亡标记、TB/NGJ/小屏座位。代码 CI/R2 与真机视觉验收分别标注，不把构建成功等同现场效果。
