package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/** The current actual information question, without Host-generated answer candidates. */
internal data class PendingGlobalScalarInformationDecision(
    val requestIdentity: StorytellerDecisionRequestIdentity,
    val revision: StorytellerDecisionRevision,
    val sourceSeat: Int,
    val abilityRole: RoleId,
    val reliability: ReliabilityState,
    val kind: StorytellerProviderScalarKindV1,
    val metric: String,
    val subjectSeats: List<Int>,
)

internal fun pendingGlobalNightScalarDecision(
    step: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    sequence: Int,
    cards: List<PlayerCard>,
    gameId: String,
    revision: StorytellerDecisionRevision,
    fortuneTellerSelectedNames: List<String> = emptyList(),
    canonicalGameState: GameState? = null,
): PendingGlobalScalarInformationDecision? {
    if (phase !in setOf(ClocktowerPhase.FirstNight, ClocktowerPhase.Night) ||
        !step.isRealAction) return null
    val actor = step.actor ?: return null
    // Once a game is committed, the Session owns seat identities. Cards are
    // only the UI's current question and display options, not a second roster.
    if (canonicalGameState != null) {
        require(canonicalGameState.players.sortedBy { it.seat }.map { it.name } ==
            cards.map { it.name }) { "UI card order diverged from canonical Host seats." }
    }
    fun canonicalSeat(name: String): Int? =
        canonicalGameState?.players?.singleOrNull { it.name == name }?.seat
            ?: cards.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.plus(1)
    val actorSeat = canonicalSeat(actor.name) ?: return null
    val question = step.informationQuestion ?: return null
    val subjects = question.subjectSeats
    val kind: StorytellerProviderScalarKindV1
    val metric: String
    when (question) {
        is ClocktowerInformationQuestion.Number -> {
            kind = StorytellerProviderScalarKindV1.NUMBER
            metric = question.metric.name
        }
        is ClocktowerInformationQuestion.Boolean -> {
            val selectedSeats = fortuneTellerSelectedNames.mapNotNull(::canonicalSeat)
            if (selectedSeats.size != 2 || selectedSeats.distinct().size != 2 || selectedSeats != subjects) return null
            kind = StorytellerProviderScalarKindV1.BOOLEAN
            metric = question.metric.name
        }
        is ClocktowerInformationQuestion.Role -> {
            kind = StorytellerProviderScalarKindV1.ROLE
            metric = "REVEALED_CHARACTER"
        }
    }
    if (subjects.distinct().size != subjects.size || subjects.any { it !in 1..cards.size }) return null
    val reliability = step.informationReliability
    return PendingGlobalScalarInformationDecision(
        requestIdentity = StorytellerDecisionRequestIdentity(
            gameId, "night-scalar:${phase.name}:$round:$sequence:$actorSeat:" +
                "$metric:${subjects.joinToString(",")}",
        ),
        revision = revision,
        sourceSeat = actorSeat,
        abilityRole = RoleId(requireNotNull(step.roleEnName)),
        reliability = reliability,
        kind = kind,
        metric = metric,
        subjectSeats = subjects,
    )
}
