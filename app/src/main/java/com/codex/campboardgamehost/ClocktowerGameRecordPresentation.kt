package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState

internal enum class GameRecordCategory { INFORMATION, ACTION, VOTE, CHANGE, SETUP }

/** Disposable read-only presentation; IDs and order belong to the confirmed Session. */
internal data class GameRecordRow(
    val id: String,
    val order: Long,
    val phase: StorytellerPhase,
    val round: Int,
    val category: GameRecordCategory,
    val summary: String,
    val details: String,
    val seats: Set<Int>,
)

internal fun gameRecordRows(state: ClocktowerSessionState, language: String): List<GameRecordRow> =
    GameRecordFormatter(state, language).rows()

private class GameRecordFormatter(val state: ClocktowerSessionState, val language: String) {
    fun t(zh: String, en: String) = if (language == "en") en else zh
    fun seat(n: Int) = t("${n}号", "#$n") + state.gameState.playerAt(n)?.name?.let { " $it" }.orEmpty()
    fun seats(ns: Collection<Int>) = ns.joinToString("、") { seat(it) }
    fun role(id: RoleId) = clocktowerRoleByName(id.value)?.nameFor(language) ?: id.value
    fun yes(value: Boolean) = if (value) t("是", "Yes") else t("否", "No")
    fun alignment(value: Alignment) = if (value == Alignment.GOOD) t("善良", "Good") else t("邪恶", "Evil")
    fun type(value: CharacterType) = when (value) {
        CharacterType.TOWNSFOLK -> t("镇民", "Townsfolk")
        CharacterType.OUTSIDER -> t("外来者", "Outsider")
        CharacterType.MINION -> t("爪牙", "Minion")
        CharacterType.DEMON -> t("恶魔", "Demon")
    }
    fun proposition(p: InformationProposition): String = when (p) {
        is InformationProposition.RoleAt -> "${seat(p.seat)}：${role(p.role)}"
        is InformationProposition.ShownRoleAt -> t("${seat(p.seat)} 展示身份：${role(p.role)}", "${seat(p.seat)} shown role: ${role(p.role)}")
        is InformationProposition.AlignmentAt -> "${seat(p.seat)}：${alignment(p.alignment)}"
        is InformationProposition.CharacterTypeAt -> "${seat(p.seat)}：${type(p.characterType)}"
        is InformationProposition.AliveAt -> "${seat(p.seat)}：" + if (p.alive) t("存活", "Alive") else t("死亡", "Dead")
        is InformationProposition.AbilityStateAt -> "${seat(p.seat)} · ${role(p.abilityRole)}：" + when (p.abilityState) {
            AbilityState.FUNCTIONING -> t("能力正常", "Functioning")
            AbilityState.MALFUNCTIONING_DRUNK -> t("醉酒失效", "Malfunctioning: drunk")
            AbilityState.MALFUNCTIONING_POISONED -> t("中毒失效", "Malfunctioning: poisoned")
        }
        is InformationProposition.RoleInPlay -> "${role(p.role)}：" + if (p.inPlay) t("在场", "In play") else t("不在场", "Not in play")
        is InformationProposition.PlayerCount -> t("玩家人数：${p.value}", "Players: ${p.value}")
        is InformationProposition.SetupProfile -> t("镇民 ${p.townsfolk} / 外来者 ${p.outsiders} / 爪牙 ${p.minions} / 恶魔 ${p.demons}", "Townsfolk ${p.townsfolk} / Outsiders ${p.outsiders} / Minions ${p.minions} / Demons ${p.demons}")
        is InformationProposition.AnyOf -> p.alternatives.joinToString(t(" 或 ", " OR ")) { "(${proposition(it)})" }
        is InformationProposition.AllOf -> p.propositions.joinToString(t("；", "; ")) { proposition(it) }
        is InformationProposition.Not -> t("并非（${proposition(p.proposition)}）", "Not (${proposition(p.proposition)})")
        is InformationProposition.NumericResult -> (when (p.metric) {
            NumericMetric.ADJACENT_EVIL_PAIRS -> t("相邻邪恶对数", "Adjacent evil pairs")
            NumericMetric.LIVING_EVIL_NEIGHBOURS -> t("存活邪恶邻居数", "Living evil neighbours")
            NumericMetric.STEPS_TO_NEAREST_MINION -> t("到最近爪牙的距离", "Distance to nearest Minion")
            NumericMetric.PLAYERS_WAKING_FOR_ABILITY -> t("因能力醒来的人数", "Players waking for ability")
        }) + (if (p.subjectSeats.isEmpty()) "" else " · ${seats(p.subjectSeats)}") + "：${p.value}"
        is InformationProposition.BooleanResult -> (if (p.metric == BooleanMetric.PLAYER_QUESTION_ANSWER) t("问题回答", "Question answer") else t("查询 ${seats(p.subjectSeats)}", "Query ${seats(p.subjectSeats)}")) + "：${yes(p.value)}"
    }
    fun referenced(p: InformationProposition): Set<Int> = when (p) {
        is InformationProposition.RoleAt -> setOf(p.seat)
        is InformationProposition.ShownRoleAt -> setOf(p.seat)
        is InformationProposition.AlignmentAt -> setOf(p.seat)
        is InformationProposition.CharacterTypeAt -> setOf(p.seat)
        is InformationProposition.AliveAt -> setOf(p.seat)
        is InformationProposition.AbilityStateAt -> setOf(p.seat)
        is InformationProposition.NumericResult -> p.subjectSeats.toSet() + p.sourceSeat
        is InformationProposition.BooleanResult -> p.subjectSeats.toSet() + p.sourceSeat
        is InformationProposition.AnyOf -> p.alternatives.flatMap { referenced(it) }.toSet()
        is InformationProposition.AllOf -> p.propositions.flatMap { referenced(it) }.toSet()
        is InformationProposition.Not -> referenced(p.proposition)
        else -> emptySet()
    }
    fun number(n: Int) = t("${n}号", "#$n")
    fun compact(p: InformationProposition): String = when (p) {
        is InformationProposition.AnyOf -> {
            val roles = p.alternatives.filterIsInstance<InformationProposition.RoleAt>()
            if (roles.size == p.alternatives.size && roles.map { it.role }.distinct().size == 1)
                role(roles.first().role) + t("，", ", ") + roles.joinToString(t("或", " or ")) { number(it.seat) }
            else proposition(p)
        }
        is InformationProposition.NumericResult -> when (p.metric) {
            NumericMetric.ADJACENT_EVIL_PAIRS -> t("${p.value}对", "${p.value} pairs")
            NumericMetric.LIVING_EVIL_NEIGHBOURS -> t("${p.value}人", "${p.value} players")
            NumericMetric.STEPS_TO_NEAREST_MINION -> t("距离${p.value}", "Distance ${p.value}")
            NumericMetric.PLAYERS_WAKING_FOR_ABILITY -> p.subjectSeats.joinToString("、") { number(it) } + t("，${p.value}人醒来", ", ${p.value} woke")
        }
        is InformationProposition.BooleanResult -> if (p.metric == BooleanMetric.PLAYER_QUESTION_ANSWER) yes(p.value)
            else p.subjectSeats.joinToString("、") { number(it) } + if (p.value) t("，有", ", Yes") else t("，无", ", No")
        is InformationProposition.RoleAt -> "${number(p.seat)}，${role(p.role)}"
        is InformationProposition.Not -> if (p.proposition is InformationProposition.RoleInPlay)
            t("无${role(p.proposition.role)}", "No ${role(p.proposition.role)}") else proposition(p)
        else -> proposition(p)
    }
    fun rows(): List<GameRecordRow> {
        val actions = state.actionTimeline.entries.mapNotNull { entry ->
            val f = entry.fact
            if (f is ActionFact.NightWake || f is ActionFact.ArtistAnswer || f is ActionFact.Vote &&
                state.actionTimeline.entries.any { it.fact is ActionFact.Nomination && f.actionId == "vote:${it.fact.actionId}" }) return@mapNotNull null
            var displayOrder = entry.point.globalSequence
            var category = GameRecordCategory.ACTION
            var involved = emptySet<Int>()
            var details = ""
            fun target(n: Int): String { involved = involved + n; return seat(n) }
            val summary = when (f) {
                is ActionFact.PhaseAdvance -> return@mapNotNull null // phase is the section heading
                is ActionFact.Poison -> f.targetSeat?.let { t("投毒 → ${target(it)}", "Poison → ${target(it)}") } ?: t("未选择投毒目标", "No poison target selected")
                is ActionFact.Protect -> t("保护 → ${target(f.targetSeat)}", "Protect → ${target(f.targetSeat)}")
                is ActionFact.Attack -> t("攻击 → ${target(f.targetSeat)}", "Attack → ${target(f.targetSeat)}")
                is ActionFact.ButlerMasterChoice -> t("${target(f.butlerSeat)} 选择主人 → ${target(f.masterSeat)}", "${target(f.butlerSeat)} chose master → ${target(f.masterSeat)}")
                is ActionFact.Death -> { category = GameRecordCategory.CHANGE; t("${target(f.targetSeat)} 死亡", "${target(f.targetSeat)} died") }
                is ActionFact.Execution -> { category = GameRecordCategory.CHANGE; t("${target(f.targetSeat)} 被处决", "${target(f.targetSeat)} was executed") }
                is ActionFact.NoExecution -> { category = GameRecordCategory.CHANGE; t("今日无人被处决", "No execution today") }
                is ActionFact.RoleChange -> { category = GameRecordCategory.CHANGE; t("${target(f.targetSeat)} 变为 ${role(f.role)} · ${alignment(f.alignment)}", "${target(f.targetSeat)} became ${role(f.role)} · ${alignment(f.alignment)}") }
                is ActionFact.Nomination -> { category = GameRecordCategory.VOTE
                    details = f.nominatorRegistersAsTownsfolk?.let { t("提名者登记为镇民：${yes(it)}", "Nominator registers as Townsfolk: ${yes(it)}") }.orEmpty()
                    val voteEntry = state.actionTimeline.entries.singleOrNull { it.fact.actionId == "vote:${f.actionId}" && it.fact is ActionFact.Vote }
                    val vote = voteEntry?.fact as? ActionFact.Vote
                    val virgin = state.actionTimeline.entries.any { it.fact is ActionFact.Execution && it.fact.actionId == "${f.actionId}:virgin-execution" }
                    if (vote != null) {
                        displayOrder = requireNotNull(voteEntry).point.globalSequence
                        involved = involved + vote.voterSeats
                        details += t("\n幽灵票：", "\nGhost votes: ") + vote.ghostVoterSeats.joinToString("、") { number(it) }.ifEmpty { t("无", "None") }
                    }
                    t("${target(f.nominatorSeat)} 提名 ${target(f.nomineeSeat)}", "${target(f.nominatorSeat)} nominated ${target(f.nomineeSeat)}") + when {
                        virgin -> t("，处女能力触发", ", Virgin triggered")
                        vote != null -> t("（${vote.voterSeats.size}票：${vote.voterSeats.joinToString(" ")}）", " (${vote.voterSeats.size} votes: ${vote.voterSeats.joinToString(" ")})")
                        else -> t("（未完成投票）", " (vote incomplete)")
                    } }
                is ActionFact.Vote -> { category = GameRecordCategory.VOTE; involved = f.voterSeats.toSet()
                    details = t("投票：${seats(f.voterSeats)}\n幽灵票：${seats(f.ghostVoterSeats).ifEmpty { "无" }}", "Voters: ${seats(f.voterSeats)}\nGhost votes: ${seats(f.ghostVoterSeats).ifEmpty { "None" }}")
                    t("${target(f.nominatorSeat)} → ${target(f.nomineeSeat)}：${f.voterSeats.size} 票", "${target(f.nominatorSeat)} → ${target(f.nomineeSeat)}: ${f.voterSeats.size} votes") }
                is ActionFact.SlayerShot -> {
                    details = t("消耗能力：${yes(f.abilityConsumed)}", "Ability consumed: ${yes(f.abilityConsumed)}") + f.targetRegistersAsDemon?.let { t("\n目标登记为恶魔：${yes(it)}", "\nTarget registers as Demon: ${yes(it)}") }.orEmpty()
                    t("${target(f.claimantSeat)} 猎手开枪 → ${target(f.targetSeat)}：${if(f.hit) "命中" else "未命中"}", "${target(f.claimantSeat)} Slayer shot → ${target(f.targetSeat)}: ${if(f.hit) "hit" else "miss"}") }
                is ActionFact.KlutzChoice -> t("${target(f.klutzSeat)} 呆瓜选择 → ${target(f.chosenSeat)}", "${target(f.klutzSeat)} Klutz chose → ${target(f.chosenSeat)}")
                is ActionFact.MayorRedirectChoice -> t("${target(f.mayorSeat)} 市长转移 → ${target(f.targetSeat)}", "${target(f.mayorSeat)} Mayor redirected → ${target(f.targetSeat)}")
                is ActionFact.RedHerringChoice -> { category = GameRecordCategory.SETUP; f.selectedSeat?.let { t("红鲱鱼：${target(it)}", "Red Herring: ${target(it)}") } ?: t("清除红鲱鱼", "Red Herring cleared") }
                is ActionFact.DemonBluffChoice -> { category = GameRecordCategory.SETUP; t("恶魔伪装身份：", "Demon bluffs: ") + f.roles.joinToString("、") { role(it) } }
                is ActionFact.NightWake -> t("${target(f.actorSeat)} · ${role(f.role)} 已查看魔典", "${target(f.actorSeat)} · ${role(f.role)} viewed the Grimoire")
                is ActionFact.ArtistAnswer -> {
                    details = t("消耗能力：${yes(f.abilityConsumed)}；问题原文未记录", "Ability consumed: ${yes(f.abilityConsumed)}; question text not recorded")
                    t("${target(f.claimantSeat)} 使用艺术家问答", "${target(f.claimantSeat)} used Artist question") }
                is ActionFact.GameEnded -> { category = GameRecordCategory.CHANGE
                    f.subjectSeat?.let { target(it) }
                    val reason = when(f.reason) {
                        GameEndReason.NO_DEMON -> t("无存活恶魔", "No living Demon")
                        GameEndReason.TWO_ALIVE -> t("仅剩两名存活玩家", "Two players alive")
                        GameEndReason.SAINT -> t("圣徒被处决", "Saint executed")
                        GameEndReason.MAYOR -> t("市长胜利", "Mayor victory")
                        GameEndReason.KLUTZ -> t("呆瓜选择导致结束", "Klutz choice ended game")
                    }
                    t("游戏结束：$reason", "Game ended: $reason") }
            }
            GameRecordRow("action:${f.actionId}", displayOrder, entry.point.phase, entry.point.round, category, summary, details, involved)
        }
        val observations = state.epistemicObservationLog.records.mapNotNull { r ->
            val point = (r.timelineBinding as? ObservationTimelineBinding.Global)?.point ?: return@mapNotNull null
            if (r.recordId.contains("first_night:system:minion_info") || r.recordId.contains("first_night:system:demon_info") ||
                r.recordId.startsWith("role-notification:")) return@mapNotNull null
            val alive = r.proposition as? InformationProposition.AliveAt
            if (r.visibility == ObservationVisibility.PUBLIC && alive != null && !alive.alive &&
                state.actionTimeline.entries.any { e -> e.point.phase == point.phase && e.point.round == point.round &&
                    when (val f = e.fact) {
                        is ActionFact.Death -> f.targetSeat == alive.seat
                        is ActionFact.Execution -> f.targetSeat == alive.seat
                        else -> false
                    }
                }) return@mapNotNull null
            val recipient = if(r.visibility == ObservationVisibility.PUBLIC) t("公开信息", "Public information") else t("告知 ${seats(r.recipientSeats)}", "Told ${seats(r.recipientSeats)}")
            val ability = r.sourceAbility?.let { role(it) + if (it.value == "Artist") t("回答", " answer") else t("信息", " information") } ?: recipient
            val detail = when(r.reliability) {
                ObservationReliability.KNOWN_MALFUNCTIONING -> t("已记录为能力失效时收到的信息", "Recorded as received while malfunctioning")
                ObservationReliability.RECEIVED_AS_FUNCTIONING -> t("记录玩家实际收到的内容，不代表内容必然为真", "Records what was received; does not establish its truth")
                ObservationReliability.NOT_ABILITY_INFORMATION -> t("非能力信息", "Not ability information")
            }
            val absent = r.proposition as? InformationProposition.AllOf
            val content = if (r.sourceAbility?.value == "Librarian" && absent != null &&
                absent.propositions.all { it is InformationProposition.RoleInPlay && !it.inPlay })
                t("无外来者", "No Outsiders") else compact(r.proposition)
            GameRecordRow("observation:${r.recordId}", point.globalSequence, point.phase, point.round, GameRecordCategory.INFORMATION,
                "$ability：$content", detail,
                r.recipientSeats + referenced(r.proposition) + listOfNotNull(r.sourceSeat))
        }
        val all = actions + observations
        val setup = all.filter { it.category == GameRecordCategory.SETUP }.groupBy {
            state.actionTimeline.entries.single { e -> "action:${e.fact.actionId}" == it.id }.fact.javaClass
        }.values.map { history ->
            val latest = history.maxBy { it.order }
            latest.copy(details = history.sortedByDescending { it.order }.drop(1).joinToString("\n") { it.summary })
        }
        return all.filter { it.category != GameRecordCategory.SETUP }.sortedByDescending { it.order } + setup.sortedByDescending { it.order }
    }
}
