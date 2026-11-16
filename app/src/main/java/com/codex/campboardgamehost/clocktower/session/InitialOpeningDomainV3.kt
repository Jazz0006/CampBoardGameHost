package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.setup.TroubleBrewingSetupCommitter
import com.codex.campboardgamehost.clocktowerDemonBluffCandidates

/**
 * The opening choices offered to the model before a Drunk is bound, and the check of its answers.
 *
 * No Session exists yet, so every question is put to the Session's own rules (information
 * resolvers, Red Herring and Demon bluff eligibility) on a transient value "as if this seat were
 * the Drunk". That value is never stored and is not a game fact; it only keeps one rule in one
 * place for the prompt, the answer check and the later committed Session.
 */
internal class InitialOpeningDomainV3(
    private val request: InitialOpeningRequestV2,
    private val ruleset: ValidatedClocktowerRuleset,
) {
    /** Whether the model answers this information seat: always, only as its chosen Drunk, or never. */
    enum class Answer { ALWAYS, IF_DRUNK, NEVER }

    /** [seat] is null for the Drunk, whose seat the same response chooses. */
    data class Truthful(val roleId: String, val seat: Int?)
    data class Registration(val seat: Int, val roleIds: List<String>)

    sealed interface Info {
        val seat: Int
        val roleId: String
        val answer: Answer
    }
    data class Number(
        override val seat: Int, override val roleId: String, override val answer: Answer,
        val metric: NumericMetric, val truthful: List<Int>, val ifDrunk: List<Int>,
    ) : Info
    data class Pair(
        override val seat: Int, override val roleId: String, override val answer: Answer,
        val truthful: List<Truthful>, val mayRegister: List<Registration>, val noneTruthful: Boolean,
    ) : Info
    data class AfterPlayerChoice(override val seat: Int, override val roleId: String) : Info {
        override val answer: Answer get() = Answer.NEVER
    }

    private val registry = ruleset.characterRegistry
    private fun external(role: RoleId): String = requireNotNull(registry.findByRoleId(role)).externalId
    private fun roleOf(externalId: String) = registry.findByExternalId(externalId)

    fun stateFor(drunkSeat: Int?, bluffRoleIds: List<String> = emptyList()): ClocktowerSessionState {
        val drunk = requireNotNull(roleOf("drunk"))
        val players = request.shownRoles.toSortedMap().map { (seat, shownId) ->
            val shown = requireNotNull(roleOf(shownId))
            val actual = if (seat == drunkSeat) drunk else shown
            with(TroubleBrewingSetupCommitter) {
                PlayerState(seat, "Seat $seat", actual.id, actual.team.toDomainAlignment(),
                    actual.team.toDomainCharacterType(), shownRole = shown.id)
            }
        }
        return ClocktowerSessionState(
            gameId = request.snapshot.gameId, gameStateRevision = 0, playerInputRevision = 0,
            gameSeed = request.snapshot.gameSeed,
            gameState = GameState(ruleset.script.id, players, request.snapshot.gameSeed),
            demonBluffRoleIds = bluffRoleIds.map { requireNotNull(roleOf(it)).id },
        )
    }

    val drunkSeats: List<Int> = request.drunkCandidateSeats
    private val unbound = stateFor(null)
    // Good seats and roles in play are the same whichever shown Townsfolk becomes the Drunk.
    private val bound = stateFor(drunkSeats.firstOrNull())
    private val fortuneTellerSeat: Int? =
        request.shownRoles.entries.firstOrNull { roleOf(it.value)?.id?.value == "Fortune Teller" }?.key

    val bluffRoleIds: List<String> = if (!bound.firstNightEvilInformationRequired()) emptyList()
        else clocktowerDemonBluffCandidates(bound.gameState).map { external(RoleId(it.enName)) }

    /** Empty when no Fortune Teller is shown; a Drunk Fortune Teller needs none either. */
    val redHerringSeats: List<Int> =
        if (fortuneTellerSeat == null) emptyList() else bound.redHerringSelectableSeats().toList()

    fun redHerringSeatsFor(drunkSeat: Int?): Set<Int> =
        if (fortuneTellerSeat == null || fortuneTellerSeat == drunkSeat) emptySet()
        else stateFor(drunkSeat).redHerringSelectableSeats()

    val information: List<Info> = request.shownRoles.toSortedMap().mapNotNull { (seat, shownId) ->
        val role = requireNotNull(roleOf(shownId)).id
        val candidate = seat in drunkSeats
        when (role.value) {
            "Fortune Teller" -> AfterPlayerChoice(seat, shownId)
            "Chef", "Empath" -> {
                val metric = metricOf(role)
                val truthful = NumericInformationResolver.resolve(unbound, metric, seat)?.values.orEmpty()
                val ifDrunk = if (!candidate) emptyList()
                    else NumericInformationResolver.resolve(stateFor(seat), metric, seat)?.values.orEmpty()
                Number(seat, shownId, when {
                    truthful.size > 1 -> Answer.ALWAYS
                    candidate -> Answer.IF_DRUNK
                    else -> Answer.NEVER
                }, metric, truthful, ifDrunk)
            }
            "Washerwoman", "Librarian", "Investigator" -> {
                val resolved = PairInformationResolver.resolve(unbound, ruleset, role, seat, null, null)
                    ?: return@mapNotNull null
                val truthful = (resolved.ordinaryCandidateSeats - seat).sorted().map {
                    Truthful(external(requireNotNull(unbound.gameState.playerAt(it)).actualRole), it)
                } + if (role.value == "Librarian" && request.hasDrunk) listOf(Truthful("drunk", null)) else emptyList()
                val mayRegister = (resolved.specialCandidateSeats - seat).sorted().mapNotNull { special ->
                    val other = request.shownRoles.keys.firstOrNull { it != seat && it != special }
                        ?: return@mapNotNull null
                    PairInformationResolver.resolve(unbound, ruleset, role, seat, special, other)
                        ?.options?.map { external(it.role) }?.takeIf { it.isNotEmpty() }
                        ?.let { Registration(special, it) }
                }
                Pair(seat, shownId, when {
                    truthful.isNotEmpty() || mayRegister.isNotEmpty() -> Answer.ALWAYS
                    candidate -> Answer.IF_DRUNK
                    else -> Answer.NEVER
                }, truthful, mayRegister, resolved.canShowNoOutsiders && !request.hasDrunk)
            }
            else -> null
        }
    }

    private fun metricOf(role: RoleId) =
        if (role.value == "Chef") NumericMetric.ADJACENT_EVIL_PAIRS else NumericMetric.LIVING_EVIL_NEIGHBOURS

    private fun needsAnswer(slot: Info, drunkSeat: Int?) =
        slot.answer == Answer.ALWAYS || (slot.answer == Answer.IF_DRUNK && slot.seat == drunkSeat)

    /**
     * One answered information item under the Drunk and bluffs the same response chose.
     * Null means it is not an open question or not a result the Session rules allow.
     */
    fun accepted(
        seat: Int, roleId: String?, seats: List<Int>, number: Int?,
        drunkSeat: Int?, bluffs: List<String>,
    ): HostOpeningSemanticValueV2? {
        val slot = information.firstOrNull { it.seat == seat } ?: return null
        if (!needsAnswer(slot, drunkSeat)) return null
        val state = stateFor(drunkSeat, bluffs)
        val drunk = seat == drunkSeat
        return when (slot) {
            is Number -> number?.takeIf {
                roleId == null && seats.isEmpty() &&
                    it in NumericInformationResolver.resolve(state, slot.metric, seat)?.values.orEmpty()
            }?.let { numberValue(slot, drunk, it, ruleDetermined = false) }
            is Pair -> {
                val role = requireNotNull(roleOf(slot.roleId)).id
                when {
                    number != null -> null
                    roleId == null -> zeroOutsiders(slot, drunk, ruleDetermined = false).takeIf {
                        seats.isEmpty() && PairInformationResolver
                            .resolve(state, ruleset, role, seat, null, null)?.canShowNoOutsiders == true
                    }
                    else -> {
                        val suggested = roleOf(roleId)?.id
                        if (suggested == null || seats.size != 2 || seats.distinct().size != 2 ||
                            seat in seats || seats.any { it !in request.shownRoles } ||
                            !PairInformationResolver.acceptsCurrentRoleAdvice(
                                state, ruleset, role, seat, seats[0], seats[1], suggested)) null
                        else value(slot, drunk, false, "EVIDENCE_PAIR", seats.sorted(), "ROLE", roleId = roleId)
                    }
                }
            }
            is AfterPlayerChoice -> null
        }
    }

    /** Results the rules leave no choice about; the Host states them instead of asking the model. */
    fun ruleDetermined(drunkSeat: Int?): List<HostOpeningSemanticValueV2> {
        val state = stateFor(drunkSeat)
        return information.filterNot { needsAnswer(it, drunkSeat) }.mapNotNull { slot ->
            when (slot) {
                is Number -> NumericInformationResolver.resolve(state, slot.metric, slot.seat)?.values
                    ?.singleOrNull()?.let { numberValue(slot, false, it, ruleDetermined = true) }
                is Pair -> PairInformationResolver.resolve(
                    state, ruleset, requireNotNull(roleOf(slot.roleId)).id, slot.seat, null, null,
                )?.takeIf {
                    it.canShowNoOutsiders && (it.ordinaryCandidateSeats - slot.seat).isEmpty() &&
                        (it.specialCandidateSeats - slot.seat).isEmpty()
                }?.let { zeroOutsiders(slot, false, ruleDetermined = true) }
                is AfterPlayerChoice -> null
            }
        }
    }

    private fun numberValue(slot: Number, drunk: Boolean, number: Int, ruleDetermined: Boolean) = value(
        slot, drunk, ruleDetermined,
        if (slot.metric == NumericMetric.LIVING_EVIL_NEIGHBOURS) "FIXED_NEIGHBORS" else "NONE",
        emptyList(), "NUMBER", number = number)

    private fun zeroOutsiders(slot: Pair, drunk: Boolean, ruleDetermined: Boolean) =
        value(slot, drunk, ruleDetermined, "NONE", emptyList(), "ZERO_OUTSIDERS")

    private fun value(
        slot: Info, drunk: Boolean, ruleDetermined: Boolean, mode: String, seats: List<Int>,
        kind: String, number: Int? = null, roleId: String? = null,
    ) = HostOpeningSemanticValueV2(
        slot.seat, slot.roleId, slot.roleId.takeUnless { drunk },
        when {
            drunk -> "DRUNK_MISINFORMATION"
            ruleDetermined -> "RULE_DETERMINED"
            else -> "FUNCTIONING"
        }, mode, seats, kind, number, roleId)
}
