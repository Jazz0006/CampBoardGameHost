# 当前交接 — 全仓删除阶段（2026-10-21）

#335、#334 均已合并；main 基线 `40050941efc3a9bcfd061d3c91d5cc72c5c1d2e5`。GitHub 独立核实完整 CI #4506（39577688281）和 Field Test APK #157（39577688218）均 SUCCESS。旧“两 PR 仍 Draft、不得合并”记录过时。最小完整游戏第一阶段关闭，具体闭环/限制见 [验收报告](MINIMAL_HOST_MINIMUM_COMPLETE_ACCEPTANCE_2026-10-21.md)。现在全仓遍历/大规模删除，之后才优化界面和流程细节。

## 本批实际清理

工作分支 `codex/whole-tree-cleanup-20261021`，从上述 main 新建，非旧堆叠分支。

- `tools/asp_oracle/`：旧 FormalGameState 静态 corpus、botc-asp 翻译/运行器、专属 14 个 Python 测试和说明整体删除。该工具不调用/比较当前 Android Host 或 Session；实际游戏确定性规则继续由 Session 及 Android 行为测试保护。
- `.github/workflows/ci.yml`：移除旧 ASP/Clingo 输出、路径分类、jobs、needs/env；Android full-ci、未知路径全检、真实 APK 和失败门禁仍保留。R2 原脚本无改动。
- `ui/`：孤立的 HTML/CSS/JS 组件展示样例删除，只有目录内相互引用和历史设计文档，无 App/资源/构建消费者；Android Compose UI 未更改。
- `tools/setup/migrate_tb_drunk_templates.py`：已完成的一次性迁移脚本删除；运行时模板、schema、迁移结果证据和两条跨局轮换链不变。

9 文件整删；包括 CI/setup 说明共 11 文件 +4/-2766，净删 2762 行 / 134078B，不含维护文档的增删。Android 生产/测试 Kotlin 删除 0。App 116000B、Night Host 63787B 仍超 50000B；Session 47578B。

## 本批验证

Cloud `:app:testFull :app:assembleDebug` 成功，654 tests / 147 suites / 38 UI，0 failures/errors/skips；原始 R2 shell 成功。执行实际 CI 分类 shell 的 10 类路径/事件用例及 gate success/skip/failure/cancelled/classifier failure 均通过，workflow bash syntax 和 diff 检查通过。没有收费 API、真机或实际杀进程测试。代码 `280992ca271d7edffcd190834340cd1768fd7ad3` 的[完整 Android CI #4508](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39580230705) 和 [R2 #4083](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39580230512) 均 SUCCESS；已独立核对 full tests、APK 构建/上传及 CI gate。Draft PR [#337](https://github.com/Jazz0006/CampBoardGameHost/pull/337) 保持未合并；本次后续文档提交不替代上述代码验收。

## 库存及下一步

在唯一 [源码台账](MINIMAL_HOST_FULL_SOURCE_AUDIT_2026-10-18.md) 对齐当前 Git 树，删除 54 条前阶段已退休的悬空路径，漏登文件补为待审；不得把路径扫描、历史审计结论或净删除行数冒充现行全树语义审计。历史 artifacts 也纳入库存，原未跟踪文件不动。

继续全仓待审路径与 App/Night Host 混合职责。先确认真实生产消费者及当前确定性规则/AI用途，再移除整条废弃链和专属测试；不机械拆大文件。完成全部源码 <=50000B 和全树零遗留后，进入统一界面/流程细节优化。无新增所有者决策阻塞；新清理 PR 保持 Draft，不自动合并/强推/删分支。

## 环境和保留合同

Codex Cloud `/workspace/CampBoardGameHost` 直接读写、执行；不依赖 MiniMCP/Windows。每个 shell `source /workspace/.cloud-setup/activate.sh`；JDK25 位于 `/workspace/.cloud-setup/jdk25`。本批临时测试 init `/tmp/campboard-tests.init.gradle`、用户目录 `/tmp/campboard-java-user`，复用环境代理/CA；完整日志 `/tmp/campboard-cleanup-full.log`，摘要 `/tmp/campboard-cleanup-tests.json`，CI/R2 本地检查 `/tmp/campboard-ci-check.py`，均不提交。工具缓存可能需要网络补齐，不修改产品构建来绕过环境问题。

保留唯一 ClocktowerGameSession、两内置剧本、现有 AI 全链和模式范围、A/B/C/D2、实际展示信息/顺序/隐私、旧响应隔离、原子/幂等存储，以及开局阵容和同一玩家身份两项跨局防重复。
