# Stage 5 验收记录：NGJ 钟表匠、侍女数字语义与统一方桌（2026-10-22）

## 基线与范围

- Stage 4 [#349](https://github.com/Jazz0006/CampBoardGameHost/pull/349) 经完整 Android CI #4569 / R2 #4130 成功后 squash 合并 main `97a4179d8461a96e1d38f0fc169b1f5386a4c730`。
- 第五批独立分支 `feature/host-numeric-clockmaker-chambermaid-stage5-20261022`，当前 [Draft #350](https://github.com/Jazz0006/CampBoardGameHost/pull/350)；首批代码 SHA `e64bbb3f0f308b5b964d65d4889c72f80d26a10b`。**最终 CI/R2 和真机验收分别记录，未验证时不得宣称完成**。

## 生产实现与边界

1. `NumericInformationResolver.resolveLaterNight` 只读当前唯一 `ClocktowerGameSession/ClocktowerSessionState`，根据 NGJ 剧本、当前 actor、能力源实际生效状态求有限可能展示数字。不建立新 Rules 引擎或第二份游戏对象。
2. Clockmaker 仅首夜：恶魔至最近爪牙沿物理环形桌座位两方向的最短步数；可靠结果仅一个展示按钮，醉毒源给出有限候选，源角色未唤醒、跨剧本或缺少爪牙则不伪造答案。
3. Chambermaid 先确认两名不同、存活、非自身目标；从 `session.nightInteractions(ruleset)` 查询本夜按能力唤醒的真实角色步骤，排除说书人系统恶人介绍以及没有真实能力的 Drunk 虚假唤醒。该数字依赖当前夜间动态条件（如送葬者前夜是否处决、死亡顺序），不能靠静态剧本表或 AI 猜测。醉毒源可展示 0/1/2 有限备选。**需补边界验证：已在同夜醒过后来死亡者、角色变化与登记例外不能以本轮 CI 代替真机实测**。
4. Clockmaker 复用 `ClocktowerNumericInformationSquareTableDialog`，Chambermaid 保留共享物理座位双选方桌并复用 `ClocktowerResolvedNumericResultUi`；两者统一全屏展示/返回改选/Next 最终一次 Observation 语义。MANUAL、AI_ASSISTED、AI_AUTOMATIC 当前建议仅绑定实际 metric、source、subject seats 与当前 Resolver 值；确定值不需要 AI 自选。
5. **已删** `ClocktowerDirectNumericResultUi` 遗留的任意非负整数编辑 UI 及相关不用的 helper，保留 `clocktowerDirectNumericResult` 仅用于实际展示格式化。D2/完整 App 高价值回归已切换到新数字组件；此前 Stage 1–4 共用 Seat renderer/显示合同保持不变。

## 仍待验收

- Android 完整 CI/R2 应以**最终 SHA** 为准；不能引用先前提交上的成功。
- 5–15 人 TB、5–6 人 NGJ、360×520 紧凑方桌、一局完整 D2、醉毒/Spy/Recluse、死亡/鬼票状态、后台电话/意外退出恢复：需要针对高价值风险继续抽样自动化/实际设备检查。
- 真机 Android / ADB 未执行；真实付费 LLM API 未授权、未执行；不把 CI SUCCESS 当作这两项验收。
- 独立旧 P1：死亡玩家被提名、健康占卜师固定布尔真值审计不包含在第五批，需单独规划。
