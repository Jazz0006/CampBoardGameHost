# 自动说书人 V4 最终验收报告

> 验收日期：2026-08-10
>
> 验收范围：`CampBoardGameHost_自动说书人算法改进设计_最终实施版_v4.md` 的 PR 0–PR 11
>
> 结论：工程自动验收通过；真实设备 UI 冒烟、熟练说书人盲测和产品灰度仍属于发布前外部验证。

## 1. 验收结论

- Debug 全量单元测试：171 项通过。
- Release 全量单元测试：171 项通过。
- V4 关键验收测试：31 项通过，0 失败、0 错误、0 跳过。
- Debug APK 构建成功。
- `git diff --check` 未发现空白错误；仅报告 Windows 工作区的 LF/CRLF 转换提示。
- `MainActivity` 已不再直接引用旧推荐实现：`RecommendationService`、`DynamicCandidateGenerator`、`MayorRedirectRecommender`、`DemonSuccessorRecommender`、`SetupCandidateGenerator`、`RegistrationPolicy`、`MalfunctionPolicy`。
- 当前 ADB 设备列表为空，因此本轮未执行真实设备点击式 UI 冒烟测试。

## 2. §20.7 指标—证据映射

| 验收指标 | 结果 | 自动化证据 | 说明 |
|---|---|---|---|
| 健康图书管理员面对唯一隐士：100% 自然真实 | 通过 | `NaturalPairInformationCandidateGeneratorTest`、`SetupMigrationTest` | 生成的候选全部显示隐士，且属于 `natural-truth` 家族。 |
| 健康调查员面对唯一间谍：自然真实候选 100% 正确生成 | 通过 | `NaturalPairInformationCandidateGeneratorTest`、`SetupMigrationTest` | 不虚构另一名爪牙，显示角色集合仅包含间谍。 |
| 酒鬼显示调查员比例不再接近 100% | 通过 | `SetupMigrationTest.one thousand setup selections...` | 1000 个固定种子至少产生两个显示角色，调查员次数小于 900。 |
| 相同完整初始模板连续出现原则上不超过 2 局 | 自动代理通过 | `ToleranceCalibrationTest` | 5 万样本最长连续相同候选为 1；仍需在真实建局 UI 流程做产品级抽样确认。 |
| 动态相同状态跨局输出存在多个优质结果 | 通过 | `DynamicCandidateGeneratorTest.same decision replays while different games can vary` | 同一决策重放一致，100 个不同游戏键产生至少两个结果。 |
| 同一玩家连续高压指向显著低于旧算法 | 自动代理通过 | `ConsequenceEvaluatorTest`、`PairInformationRecommenderTest`、`ToleranceCalibrationTest` | 重复针对始终受罚；5 万样本玩家压力 P95 为 3。真人对局改善幅度仍需灰度数据验证。 |
| 同一决策可复现性 100% | 通过 | `DynamicCandidateGeneratorTest`、`RecommendationSearchTest`、`ToleranceCalibrationTest` | 相同完整输入、种子和跨局历史重复运行结果一致。 |
| 特殊登记与显示语义一致 100% | 通过 | `SpecialRegistrationRecommenderTest`、`UnifiedDecisionModelsTest` | 完整候选绑定登记事实，登记与最终显示结果不再二次独立抽取。 |
| 低质量候选被随机选中为 0 | 通过 | `CandidatePoolBuilderTest`、`ToleranceCalibrationTest` | 只保留最高可用质量等级和分数容差内候选；5 万样本容差外选择为 0。 |
| 事件日志可重建最终决策 100% | 通过 | `DecisionEventStoreTest`、`DecisionHistoryRepositoryTest`、`ClocktowerRecommendationCoordinatorTest` | 事件保存结果快照和候选审计，可重建账本、应用纠错并生成赛后复盘。 |

## 3. 5 万局固定种子校准结果

`ToleranceCalibrationTest.formal fifty thousand sample validates final score tolerances` 本轮通过，输出如下：

| 指标 | 结果 |
|---|---:|
| 样本量 | 50,000 |
| 分层数 | 135 |
| 选择熵 | 8.0319 bits |
| 单候选最大占比 | 0.0050 |
| 最长连续相同结果 | 1 |
| 玩家压力 P95 | 3 |
| 新旧配对差异率 | 0.3782 |
| 目标概率落入 95% 置信区间 | 268 / 270（99.26%） |
| 分数容差外选择 | 0 |

最终分数容差：

- 完整设置方案：32
- 单项设置决策：6
- 动态信息：4
- 高冲击动态决策：2
- 纯规则决策：0

## 4. 构建产物

- 文件：`app/build/outputs/apk/debug/app-debug.apk`
- 大小：12,342,783 bytes
- SHA-256：`40749B57B3EB9463F6B82B47F8432E714B754A87CD53278B0418C4D5A7E72C1E`

## 5. 发布前仍需完成

以下项目依赖真实设备、外部参与者或产品环境，不应由当前自动验收替代：

1. 在 Android 真机或模拟器完成建局、首夜、夜间信息、白天动态决策、归档和赛后复盘的端到端点击冒烟。
2. 确认推荐理由和警告在小屏、字体放大及长文本情况下不会遮挡关键操作。
3. 邀请至少 3 名熟练说书人完成匿名盲测，记录直接采用、修改后采用和拒绝原因。
4. 小范围灰度观察模板重复率、玩家压力、决策修改率和异常恢复情况，并保留人工裁定回退路径。

## 6. 最终判定

V4 的代码迁移、算法约束、事件审计、跨局冷却、固定种子模拟、协调层接管和自动化测试均达到工程验收条件。当前可以进入真实设备 UI 冒烟与真人体验验证阶段；在这些外部验证完成前，不建议将结论表述为“产品发布完全验收”。
