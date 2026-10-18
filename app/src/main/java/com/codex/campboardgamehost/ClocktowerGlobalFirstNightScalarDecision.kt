package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
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
    fun typedRoleResult(option: ClocktowerDisplayOption): Pair<Int, String>? {
        if (option.displayKind != ClocktowerDisplayKind.RoleReveal) return null
        (option.proposition as? InformationProposition.RoleAt)?.let {
            return it.seat to it.role.value
        }
        val seat = option.presentationSubjectSeats.singleOrNull() ?: return null
        val role = option.presentationRoleId?.takeIf { it.isNotBlank() } ?: return null
        return seat to role
    }
    val proposition = step.displayProposition ?: run {
        // A registration-sensitive Host step may not yet have a single true
        // role proposition, although its legal *possible* results already
        // identify one confirmed queried target. Preserve that ambiguity.
        val typed = (step.manualInformationCandidates + step.automaticInformationCandidates)
            .mapNotNull(::typedRoleResult)
        if (typed.isNotEmpty() && typed.map { it.first }.distinct().size == 1)
            InformationProposition.RoleAt(typed.first().first, RoleId(typed.first().second))
        else null
    }
    val kind: StorytellerProviderScalarKindV1
    val metric: String
    val subjects: List<Int>
    val selectedSeats = fortuneTellerSelectedNames.mapNotNull(::canonicalSeat)
    when (proposition) {
        is InformationProposition.NumericResult -> {
            if (proposition.sourceSeat != actorSeat) return null
            kind = StorytellerProviderScalarKindV1.NUMBER
            metric = proposition.metric.name
            subjects = proposition.subjectSeats

        }
        is InformationProposition.BooleanResult -> {
            if (proposition.sourceSeat != actorSeat || selectedSeats.size != 2 ||
                selectedSeats.distinct().size != 2 || selectedSeats != proposition.subjectSeats
            ) return null
            kind = StorytellerProviderScalarKindV1.BOOLEAN
            metric = proposition.metric.name
            subjects = proposition.subjectSeats

        }
        is InformationProposition.RoleAt -> {
            // A real target is a prerequisite. Never produce role-result choices
            // for a future player query whose target has not been observed.
            if (proposition.seat !in 1..cards.size ||
                step.displayKind != ClocktowerDisplayKind.RoleReveal &&
                    step.automaticInformationCandidates.none {
                        it.displayKind == ClocktowerDisplayKind.RoleReveal
                    }) return null
            kind = StorytellerProviderScalarKindV1.ROLE
            metric = "REVEALED_CHARACTER"
            subjects = listOf(proposition.seat)

        }
        else -> return null
    }
    if (subjects.any { it !in 1..cards.size }) return null
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
