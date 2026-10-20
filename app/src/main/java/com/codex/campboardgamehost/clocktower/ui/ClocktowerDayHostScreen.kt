package com.codex.campboardgamehost

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.codex.campboardgamehost.clocktower.session.*

/** Day drafts are local presentation; accepted nomination/vote/death facts select the active work. */
@Composable
internal fun ClocktowerDayHostScreen(
    sessionState: ClocktowerSessionState,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    allowRuleDeterminedAutoUi: Boolean,
    onHostTools: () -> Unit,
    onConfirmDay: (Int) -> Unit,
    onConfirmKlutzChoice: (String) -> Unit,
    onConfirmArtistQuestion: (String, Boolean, Boolean) -> Unit,
    onSlayerShot: (String, String, Boolean?) -> Unit,
    onConfirmedNomination: (String, String, Boolean?) -> Boolean,
    onConfirmedVote: (String, String, ClocktowerConfirmedVoteRecord) -> Unit,
) {
    val sessionGameState = sessionState.gameState
    val round = sessionState.round
    val gameId = sessionState.gameId
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String) = if(language == "en") en else zh
    val publicAliveCards = cards.filter { it.eliminatedRound == null }
    val gameOutcome = sessionState.outcome
    val ghostVoteAuthority = ClocktowerGhostVoteAuthority.from(sessionState.actionTimeline)
    val highestVoteName = sessionState.voteStanding.nomineeSeat?.let { sessionGameState.playerAt(it)?.name }
    val highestVoteCount = sessionState.voteStanding.count
    val artistAnswers = sessionState.actionTimeline.entries.mapNotNull { it.fact as? com.codex.campboardgamehost.clocktower.domain.ActionFact.ArtistAnswer }
    val artistUsed = artistAnswers.any { it.abilityConsumed }
    val artistClaimedNames = artistAnswers.mapNotNull { sessionGameState.playerAt(it.claimantSeat)?.name }
    val confirmedNightDeathSeats = sessionState.actionTimeline.entries.filter {
        it.point.phase == com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.NIGHT && it.point.round == round
    }.mapNotNull { (it.fact as? com.codex.campboardgamehost.clocktower.domain.ActionFact.Death)?.targetSeat }
    val registrationState = remember(gameId, round) { ClocktowerRegistrationInteractionState() }
    var dayMode by remember(gameId, round) { mutableStateOf(ClocktowerDayMode.Overview) }
    val nominationDraft = remember(gameId, round) { ClocktowerNominationDraft() }
    val slayerDraft = remember(gameId) { ClocktowerSlayerDraft() }
    val artistDraft = remember(gameId) { ClocktowerArtistDraft() }
    var klutzChoiceName by remember(gameId, round) { mutableStateOf<String?>(null) }
    val executionThreshold = (publicAliveCards.size + 1) / 2
    val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
    val scriptHasSlayer = "Slayer" in scriptRoleNames
    val scriptHasArtist = "Artist" in scriptRoleNames
    val artistClaimantCandidates = publicAliveCards.filter { card ->
        card.name !in artistClaimedNames && !(artistUsed && card.clocktowerRole?.enName == "Artist")
    }
    val pendingKlutz = sessionState.pendingKlutzDeath()
    if (pendingKlutz != null) {
        val klutzTableState = clocktowerKlutzTableState(
            seats = clocktowerDayOverviewTableState(sessionGameState,
                roleDisplayName = { clocktowerRoleLabel(it, language) }, ghostVoteAuthority = ghostVoteAuthority).seats,
            klutzName = sessionGameState.playerAt(pendingKlutz.klutzSeat())?.name,
            alivePlayerNames = publicAliveCards.mapTo(mutableSetOf()) { it.name }, choiceName = klutzChoiceName,
        )
        ClocktowerKlutzTableScreen(round, klutzTableState, gameOutcome == null, onHostTools,
            onSeatClick = { seatId ->
                val name = klutzTableState.playerNameForSeat(seatId)
                klutzChoiceName = if (klutzChoiceName == name) null else name
            }, onConfirm = { onConfirmKlutzChoice(requireNotNull(klutzChoiceName)) })
        return
    }
    sessionState.pendingVote()?.let { nomination ->
        val highestVoteText = when {
            highestVoteName != null -> text(
                "当前最高：${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                "Current highest: ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
            )
            highestVoteCount >= executionThreshold -> text(
                "当前最高为平票：$highestVoteCount 票；暂时无人被处决。",
                "Current high vote is tied at $highestVoteCount; nobody is set for execution.",
            )
            else -> text("当前还没有达到门槛的最高票。", "No qualifying high vote has been recorded yet.")
        }
        ClocktowerVoteTableScreen(
            round = round,
            cards = cards,
            ghostVoteAuthority = ghostVoteAuthority,
            tableState = clocktowerDayOverviewTableState(
                sessionGameState,
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            ),
            executionThreshold = executionThreshold,
            nominatorName = sessionGameState.playerAt(nomination.nominatorSeat)?.name,
            nomineeName = sessionGameState.playerAt(nomination.nomineeSeat)?.name,
            highestVoteText = highestVoteText,
            actionsEnabled = gameOutcome == null,
            onHostTools = onHostTools,
            onConfirm = { voteState ->
                onConfirmedVote(requireNotNull(sessionGameState.playerAt(nomination.nominatorSeat)).name,
                    requireNotNull(sessionGameState.playerAt(nomination.nomineeSeat)).name, voteState.confirmedVoteRecord())
                nominationDraft.clear()
                dayMode = ClocktowerDayMode.Overview
            },

        )
        return
    }
    when (dayMode) {
        ClocktowerDayMode.Overview -> {
            val highestVoteText = when {
                highestVoteName != null -> text(
                    "最高票 · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount 票",
                    "Highest · ${playerSeatLabel(cards, highestVoteName)} · $highestVoteCount",
                )
                highestVoteCount >= executionThreshold -> text(
                    "最高票 · 平票 $highestVoteCount 票",
                    "Highest · tie at $highestVoteCount",
                )
                else -> text("最高票 · 无", "Highest · none")
            }
            val dayTableState = clocktowerDayOverviewTableState(
                sessionGameState,
                roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                ghostVoteAuthority = ghostVoteAuthority,
            )
            ClocktowerDayOverviewScreen(
                round = round,
                tableState = dayTableState,
                aliveCount = publicAliveCards.size,
                executionThreshold = executionThreshold,
                highestVoteText = highestVoteText,
                lastNightDeathLabels = if (round > 1) confirmedNightDeathSeats.mapNotNull { seat ->
                    sessionGameState.playerAt(seat)?.name?.let { playerSeatLabel(cards, it) }
                } else null,
                showSlayerAction = scriptHasSlayer,
                slayerActionEnabled = cards.isNotEmpty(),
                showArtistAction = scriptHasArtist,
                artistActionEnabled = artistClaimantCandidates.isNotEmpty(),
                actionsEnabled = gameOutcome == null,
                diagnosticContent = null,
                onHostTools = onHostTools,
                onNominationGesture = { sourceSeatId, targetSeatId ->
                    val sourceName = dayTableState.seats
                        .firstOrNull { seat -> seat.seatId == sourceSeatId && seat.isAlive }
                        ?.playerName
                    val targetName = dayTableState.seats
                        .firstOrNull { seat -> seat.seatId == targetSeatId && seat.isAlive }
                        ?.playerName
                    if (sourceName != null && targetName != null && sourceName != targetName) {
                        nominationDraft.select(sourceName, targetName)
                        dayMode = ClocktowerDayMode.Nomination
                    }
                },
                onOpenSlayer = {
                    slayerDraft.clear()
                    dayMode = ClocktowerDayMode.Slayer
                },
                onOpenArtist = {
                    artistDraft.selectClaimant(null)
                    dayMode = ClocktowerDayMode.Artist
                },
                onEndDay = { onConfirmDay(round) },
            )
        }

        ClocktowerDayMode.Nomination -> {
            ClocktowerNominationInteraction(
                draft = nominationDraft,
                cards = cards,
                tableState = clocktowerDayOverviewTableState(
                    sessionGameState,
                    roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                    ghostVoteAuthority = ghostVoteAuthority,
                ),
                registrationState = registrationState,
                round = round,
                executionThreshold = executionThreshold,
                sessionState = sessionState,
                actionsEnabled = gameOutcome == null,
                onHostTools = onHostTools,
                onConfirm = { confirmation ->
                    onConfirmedNomination(confirmation.nominator, confirmation.nominee, confirmation.explicitSpyRegistration)
                    dayMode = ClocktowerDayMode.Overview
                },
                onCancel = { dayMode = ClocktowerDayMode.Overview },
            )
        }

        ClocktowerDayMode.Slayer -> {
            ClocktowerSlayerInteraction(
                draft = slayerDraft,
                sessionState = sessionState,
                seats = clocktowerDayOverviewTableState(
                    sessionGameState,
                    roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                    ghostVoteAuthority = ghostVoteAuthority,
                ).seats,
                round = round,
                actionsEnabled = gameOutcome == null,
                onHostTools = onHostTools,
                onSlayerShot = onSlayerShot,
                onBack = { dayMode = ClocktowerDayMode.Overview },
            )
        }

        ClocktowerDayMode.Artist -> {
            ClocktowerArtistInteraction(
                draft = artistDraft,
                cards = cards,
                seats = clocktowerDayOverviewTableState(
                    sessionGameState,
                    roleDisplayName = { roleId -> clocktowerRoleLabel(roleId, language) },
                    ghostVoteAuthority = ghostVoteAuthority,
                ).seats,
                claimantCandidateNames = artistClaimantCandidates.mapTo(mutableSetOf()) { it.name },
                round = round,
                sessionState = sessionState,
                allowRuleDeterminedAutoUi = allowRuleDeterminedAutoUi,
                actionsEnabled = gameOutcome == null,
                onHostTools = onHostTools,
                onConfirm = onConfirmArtistQuestion,
                onBack = { dayMode = ClocktowerDayMode.Overview },
            )
        }

    }
    return
}
