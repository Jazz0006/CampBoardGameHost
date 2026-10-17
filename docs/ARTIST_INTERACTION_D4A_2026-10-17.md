# 第四阶段 D4a：Artist 白天交互归属

基线 #322 `1b5effe3`，完整 CI/R2 已通过。D4a 只拆 Artist 问答交互；D4 的提名/投票/登记和白天早分流尚未完成。

Architecture pre-flight:
- current owner: ClocktowerJudgeScreen 保存 Artist 三个草稿、答案联动、自动填入与界面构建，混在夜间 materializer 中。
- proposed responsibility: Artist 专属交互拥有 claimant/truthful/shown 草稿与其依赖失效，以及渲染/确认；既有 App 确认回调仍提交正式事实。
- authoritative state owner(s): Session 拥有事实；Artist 草稿为局内临时 UI 状态。remember(gameId) 继续放在 Host 原位置，跨子界面/主持工具/日夜的生存期保持不变，不使用条件分支内 remember 重置草稿。
- narrow typed input/output seam: 卡片、已有共享桌面投影、候选姓名、round、poison/自动模式/可操作性；一个 claimant 确认回调与导航回调。不传 Session、nightCheckpoint、规则服务或 AI provider。
- keep in current owner / extract: ArtistInteraction 文件拥有三字段草稿及交互；Host 只保留 remember、入口清空和调用。既有 ArtistTableScreen 保留桌面渲染职责。
- reason: Artist 变更不应要求理解夜间 registry；先收敛独立交互，避免把登记函数连同全局闭包一起搬走。

扇出：草稿仅在 Host 声明、Overview 的进入 Artist 清空、Artist 页面内使用；确认唯一流向 App onConfirmArtistQuestion。取消、重选 claimant、修改真实答案继续清除依赖答案；确认先调用 Host，成功后才清草稿，异常保留。候选、可靠性与自动答案语义保持原样，不扩展未完成的 Artist 事件内容。

验收：编译/现有回归与逐段差异审查；为草稿的依赖清理和确认失败保留有价值的 typed 回归。不以 JVM 结果宣称 Compose 导航或真机生命周期验收通过。

## 实施结果

Artist 草稿与交互集中到 ClocktowerArtistInteraction.kt（152 行 / 6,603 bytes），Host 从 3,022 行 / 179,448 bytes 降到 2,925 行 / 174,272 bytes。生产总行数增加 55 行，来自显式边界、导入和封装；这一步是职责拆分而非净删除，不据此宣称整体减法或 D4 完成。Host 不再构建 Artist 答案/渲染，也不直接修改三个草稿字段。

ClocktowerArtistDraftTest 覆盖依赖答案清理、未完成/拒绝确认不接受及成功确认后清空。原有 UI 文案、候选投影、可靠性判断、自动答案 effect 的依赖与分支保持不变。Session/App 的 Artist 确认契约未改变，尚不完整的事件内容没有在本次补建。

本地完整回归和快速回归、APK 构建通过；最终仅清理导入后补编译打包检查。没有真机导航/Compose 生命周期验收。D4b 应继续梳理日间登记的共享合法域与交互归属，再做白天早返回，不把夜间状态随条件拆分隐式重置。

验证计数：完整 1,335、快速 1,328，失败/错误/跳过均为 0；最终 APK 编译打包成功，`git diff --check` 通过。远端 CI/R2 仍须检查当前提交。
