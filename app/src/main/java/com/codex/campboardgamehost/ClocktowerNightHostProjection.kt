package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.rules.*
import com.codex.campboardgamehost.clocktower.session.*

/** Read-only seat/ability presentation. Scheduling and confirmation belong to Session. */
internal class ClocktowerNightHostProjection(
    private val cards: List<PlayerCard>,
    private val state: ClocktowerSessionState,
) {
    private val game get() = state.gameState
    private val deaths: List<TimelineBoundActionFact> get() = state.actionTimeline.entries.filter {
        it.point.phase == state.phase && it.point.round == state.round && it.fact is ActionFact.Death
    }
    private fun deadActor(role: String): PlayerCard? = deaths.firstNotNullOfOrNull { entry ->
        val player = game.playerAt((entry.fact as ActionFact.Death).targetSeat)
        player?.takeIf { it.wakingRole() == RoleId(role) }?.let { cards.getOrNull(it.seat - 1) }
    }
    val mayorTarget get() = state.mayorRulingSeat()?.let { cards.getOrNull(it - 1) }
    val mayorRedirectTargetCards get() = cards.filter { it.name != mayorTarget?.name }
    val ravenkeeperTrigger get() = deadActor("Ravenkeeper")
    val sageNightDeath get() = deadActor("Sage")
    val demonCard get() = game.players.singleOrNull { it.alive && it.actualRole.value == "Imp" }?.let { cards.getOrNull(it.seat - 1) }
    val demonPoisonedForActionExplanation get() = game.players.any { it.alive && it.actualRole.value == "Imp" && it.poisoned }
    val chambermaidTargetCards get() = cards.filterIndexed { index, _ -> game.playerAt(index + 1)?.alive == true }
    val ravenkeeperDeathTriggerAbilityState get() = triggerState("Ravenkeeper", ravenkeeperTrigger)
    val sageDeathTriggerAbilityState get() = triggerState("Sage", sageNightDeath)

    private fun triggerState(role: String, actor: PlayerCard?): AbilityFunctioningState? =
        effectiveAbilitySubjectForRole(role, actor)?.let { AbilityFunctioningSemantics.stateForEstablishedInteraction(it, role) }

    fun effectiveAbilitySubjectForRole(enName: String, actor: PlayerCard?): AbilitySubject? {
        val player = actor?.name?.let { name -> game.players.firstOrNull { it.name == name } } ?: return null
        return AbilitySubject(player.actualRole.value, player.shownRole?.value,
            effectivePoisonForRole(enName) == player.name, player.alive)
    }

    fun effectivePoisonForRole(enName: String): String? {
        val player = game.players.firstOrNull { it.wakingRole() == RoleId(enName) } ?: return null
        if (enName in setOf("Ravenkeeper", "Sage") && !player.alive) {
            val death = deaths.firstOrNull { (it.fact as ActionFact.Death).targetSeat == player.seat } ?: return null
            val poison = state.actionTimeline.entries.lastOrNull {
                it.point.globalSequence < death.point.globalSequence && it.fact is ActionFact.Poison
            }?.fact as? ActionFact.Poison
            return player.name.takeIf { poison?.targetSeat == player.seat }
        }
        return player.name.takeIf { player.poisoned }
    }

    /** Role-specific later-night display only; order and mechanical effects stay with Session. */
    fun otherNightMaterializers(
        informationStepBuilder: ClocktowerInformationStepBuilder,
        text: (String, String) -> String,
        ravenkeeperTarget: String?,
    ): List<ClocktowerNightStepMaterializerRegistry.Entry> {
        val lastExecutedSeat = state.actionTimeline.entries.asReversed().firstNotNullOfOrNull {
            (it.fact as? ActionFact.Execution)?.targetSeat
        }
        val undertakerTarget = lastExecutedSeat?.let(game::playerAt)?.name?.let { name -> cards.firstOrNull { it.name == name } }
        val ravenkeeperTargetCard = ravenkeeperTarget?.let { name -> cards.firstOrNull { it.name == name } }
        val monkProtectedTarget = state.nightTargets.monkSeat?.let(game::playerAt)?.name
        val pendingNightDeath = state.nightTargets.attackSeat?.let(game::playerAt)?.name
        val mayorRedirectTarget = state.nightTargets.mayorRedirectSeat?.let(game::playerAt)?.name
        return listOf(

        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Undertaker")),
            build = {
                informationStepBuilder.build(
                    roleName = "送葬者", enName = "Undertaker", tellPlayer = null,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = undertakerTarget?.let { ClocktowerInformationQuestion.Role(cards.indexOf(it) + 1) },
                )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Monk")),
            build = {
            informationStepBuilder.build(
                roleName = "僧侣",
                enName = "Monk",
                tellPlayer = monkProtectedTarget?.let { text("已选择保护：${playerSeatLabel(cards, it)}。如果恶魔今晚选择该玩家，他不会死亡。", "Protected: ${playerSeatLabel(cards, it)}. If the Demon chooses this player tonight, they will not die.") },
                explanation = text("僧侣每晚选择除自己以外的一名玩家。若恶魔今晚攻击被保护的玩家，天亮时宣布无人死亡；不要透露是僧侣保护导致。", "Each night, the Monk protects one other player from the Demon. If that player is attacked, announce no death at dawn without revealing the protection."),
                action = ClocktowerNightAction.MonkProtect,
                displayKind = ClocktowerDisplayKind.None,
                hostInstruction = text("轻拍僧侣，示意睁眼。让他指一名除自己以外的玩家，在下面记录为今晚保护目标。", "Tap the Monk to wake them. Have them point to another player and record that player as tonight's protected target."),
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Imp")),
            build = {
            ClocktowerNightStepUi(
                title = text("恶魔行动", "Demon action"),
                actor = demonCard,
                isRealAction = demonCard != null,
                reason = if (demonCard == null) text("当前没有存活恶魔。", "There is no living Demon.") else "",
                storytellerAction = demonCard?.let {
                    text("轻拍 ${it.seatLabel(cards)}，示意睁眼。让他指今晚要杀死的玩家，在下面记录；记录后示意闭眼。", "Tap ${it.seatLabel(cards)} to wake them. Have them point to tonight's kill target, record it, then signal them to close their eyes.")
                } ?: text("不要唤醒任何玩家，停顿 2-3 秒后继续。", "Do not wake anyone. Pause for 2–3 seconds, then continue."),
                tellPlayer = if (demonCard != null) {
                    if (demonPoisonedForActionExplanation) {
                        text("恶魔已中毒，今晚杀人会失效。", "The Demon is poisoned, so tonight's kill will fail.")
                    } else {
                        pendingNightDeath?.let { text("已记录：今晚恶魔选择杀死 ${playerSeatLabel(cards, it)}。现在不要宣布死亡，等天亮统一宣布。", "Recorded: the Demon chose ${playerSeatLabel(cards, it)}. Do not announce the death until dawn.") }
                    }
                } else {
                    null
                },
                explanation = if (demonPoisonedForActionExplanation) text("可以记录恶魔选择，但天亮不会因此死亡。", "Record the Demon's choice, but it will not cause a death at dawn.") else text("恶魔选择的死亡目标会在天亮时统一公布。", "The Demon's chosen target is announced at dawn."),
                action = ClocktowerNightAction.DemonKill,
            )
            },
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.mayorRedirect(),
            build = {
            val targetedMayor = requireNotNull(mayorTarget)
                    ClocktowerNightStepUi(
                        title = text("市长死亡裁定", "Mayor death ruling"),
                        actor = null,
                        isRealAction = true,
                        reason = "",
                        storytellerAction = text("市长被恶魔击杀。选择让市长死亡，或将死亡转移给另一名玩家。", "The Demon attacked the Mayor. Let the Mayor die or redirect the death to another player."),
                        tellPlayer = mayorRedirectTarget?.let { target ->
                            if (target == targetedMayor.name) {
                                text("市长死亡", "Mayor dies")
                            } else {
                                text("死亡转移给 ${playerSeatLabel(cards, target)}", "Death redirected to ${playerSeatLabel(cards, target)}")
                            }
                        },
                        explanation = text("市长保持存活时，可以让另一名玩家代替死亡。选择死亡或受保护的玩家，可能导致今夜无人死亡。", "To keep the Mayor alive, another player may die instead. Choosing a dead or protected player can result in no death tonight."),
                        action = ClocktowerNightAction.MayorRedirect,
                        displayKind = ClocktowerDisplayKind.None,
                        roleEnName = "Mayor",
                    )
            },
        ),
        clocktowerSageStepMaterializer(
            builder = informationStepBuilder,
            triggerActor = sageNightDeath,
            abilityState = sageDeathTriggerAbilityState,
            content = ClocktowerSageStepContent(
                explanation = text("贤者被恶魔杀死时，得知恶魔是两名玩家之一。", "When killed by the Demon, the Sage learns that the Demon is one of two players."),
                displayTitle = text("贤者信息", "Sage information"),
                displayPrimary = text("恶魔", "Demon"),
                displayFooter = text("在下面两位玩家之中", "One of these two players"),
                hostInstruction = text("如果恶魔今晚杀死贤者，轻拍贤者，示意睁眼。把两名玩家只给他看；这两人之中有一名是恶魔。", "If the Demon killed the Sage tonight, wake the Sage and show only them two players, one of whom is the Demon."),
            ),
        ),
        ClocktowerNightStepMaterializerRegistry.Entry(
            identity = ClocktowerProductionNightStepIdentity.role(RoleId("Ravenkeeper")),
            build = {
                informationStepBuilder.build(
                    roleName = "守鸦人", enName = "Ravenkeeper", tellPlayer = null,
                    actorOverride = requireNotNull(ravenkeeperTrigger), abilityStateOverride = ravenkeeperDeathTriggerAbilityState,
                    action = ClocktowerNightAction.Ravenkeeper,
                    explanation = text("记录实际展示的角色。", "Record the actual shown character."),
                    displayKind = ClocktowerDisplayKind.RoleReveal,
                    informationQuestion = ravenkeeperTargetCard?.let { ClocktowerInformationQuestion.Role(cards.indexOf(it) + 1) },
                )
            },
        ),

        )
    }
}
