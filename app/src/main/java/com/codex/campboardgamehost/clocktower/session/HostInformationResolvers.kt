package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject

/**
 * Read-only information from the ONE current Session. These are possible displays, not a
 * legality gate, a second game state, a precomputed pair universe or a substitute for AI advice.
 */
internal data class HostNumericResolution(
    val values: List<Int>,
    val subjectSeats: List<Int>,
    val sourceAbility: AbilityFunctioningState,
    val relevantSpecialSeats: Set<Int>,
    val productShortlist: Boolean,
)

internal object NumericInformationResolver {
    /** Stage 2 supports Chef/Empath. Clockmaker/Chambermaid require their own later-night semantics. */
    fun resolve(
        session: ClocktowerSessionState,
        metric: NumericMetric,
        sourceSeat: Int,
    ): HostNumericResolution? {
        val role = when (metric) {
            NumericMetric.ADJACENT_EVIL_PAIRS -> "Chef"
            NumericMetric.LIVING_EVIL_NEIGHBOURS -> "Empath"
            else -> return null
        }
        val game = session.gameState
        val actor = game.playerAt(sourceSeat) ?: return null
        val ability = sourceAbility(actor, role) ?: return null
        if (!actor.alive) return null
        val circle = game.players.sortedBy(PlayerState::seat)
        if (circle.isEmpty()) return null
        val relevant = when (metric) {
            NumericMetric.ADJACENT_EVIL_PAIRS -> circle
            NumericMetric.LIVING_EVIL_NEIGHBOURS -> livingNeighbours(circle, sourceSeat)
            else -> emptyList()
        }
        val subjects = relevant.map(PlayerState::seat)
        if (ability != AbilityFunctioningState.FUNCTIONING) {
            // An intoxicated information source may be told an arbitrary fitting number.
            // This finite list is a practical UI range, not a claim of exhaustive rules legality.
            val upper = if (role == "Empath") 2 else circle.size
            return HostNumericResolution((0..upper).toList(), subjects, ability, emptySet(), true)
        }
        val special = relevant.filter(::hasFunctioningSpecialRegistration).mapTo(linkedSetOf(), PlayerState::seat)
        val options = when (metric) {
            NumericMetric.ADJACENT_EVIL_PAIRS -> adjacentEvilPairCounts(circle)
            NumericMetric.LIVING_EVIL_NEIGHBOURS -> possibleEvilCounts(relevant)
            else -> emptySet()
        }
        return HostNumericResolution(options.sorted(), subjects, ability, special, false)
    }

    /**
     * Stage 5, NGJ only. Returns current information from the sole Session.
     * Clockmaker counts physical edges in either direction; Chambermaid counts
     * actors scheduled to wake because of their actual ability this very night.
     * Evil introductions and Storyteller-only setup do not count as an ability.
     * Drunk has no actual waking ability even when shown a waking role.
     */
    fun resolveLaterNight(
        session: ClocktowerSessionState,
        ruleset: ValidatedClocktowerRuleset,
        metric: NumericMetric,
        sourceSeat: Int,
        selectedSeats: List<Int> = emptyList(),
    ): HostNumericResolution? {
        if (session.gameState.script != ruleset.script.id) return null
        val expectedRole = when (metric) {
            NumericMetric.STEPS_TO_NEAREST_MINION -> "Clockmaker"
            NumericMetric.PLAYERS_WAKING_FOR_ABILITY -> "Chambermaid"
            else -> return null
        }
        if (metric == NumericMetric.STEPS_TO_NEAREST_MINION &&
            session.phase != com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.FIRST_NIGHT) return null
        val actor = session.gameState.playerAt(sourceSeat) ?: return null
        val ability = sourceAbility(actor, expectedRole) ?: return null
        if (!actor.alive) return null
        val players = session.gameState.players.sortedBy(PlayerState::seat)
        if (players.isEmpty()) return null
        val subjects = when (metric) {
            NumericMetric.STEPS_TO_NEAREST_MINION -> players.map(PlayerState::seat)
            NumericMetric.PLAYERS_WAKING_FOR_ABILITY -> {
                if (selectedSeats.size != 2 || selectedSeats.distinct().size != 2 ||
                    sourceSeat in selectedSeats || selectedSeats.any {
                        seat -> session.gameState.playerAt(seat)?.alive != true
                    }) return null
                selectedSeats.sorted()
            }
            else -> return null
        }
        if (ability != AbilityFunctioningState.FUNCTIONING) {
            val upper = if (metric == NumericMetric.PLAYERS_WAKING_FOR_ABILITY) 2 else players.size
            return HostNumericResolution((0..upper).toList(), subjects, ability, emptySet(), true)
        }
        val answer = when (metric) {
            NumericMetric.STEPS_TO_NEAREST_MINION -> {
                val demonSeats = players.filter { it.actualType == CharacterType.DEMON }.map(PlayerState::seat)
                val minionSeats = players.filter { it.actualType == CharacterType.MINION }.map(PlayerState::seat)
                if (demonSeats.size != 1 || minionSeats.isEmpty()) return null
                val demonIndex = players.indexOfFirst { it.seat == demonSeats.single() }
                minionSeats.minOf { seat ->
                    val targetIndex = players.indexOfFirst { it.seat == seat }
                    val forward = kotlin.math.abs(demonIndex - targetIndex)
                    minOf(forward, players.size - forward)
                }
            }
            NumericMetric.PLAYERS_WAKING_FOR_ABILITY -> {
                val actualAbilityWakers = session.nightInteractions(ruleset)
                    .filter { it.kind == com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind.ROLE_PHASE_ACTION }
                    .mapNotNull { it.roleId }
                    .toSet()
                subjects.count { seat ->
                    session.gameState.playerAt(seat)?.let { player ->
                        player.actualRole.value != "Drunk" &&
                            player.wakingRole() in actualAbilityWakers
                    } == true
                }
            }
            else -> return null
        }
        return HostNumericResolution(listOf(answer), subjects, ability, emptySet(), false)
    }

    /** AI advice only: never reject an actually confirmed Storyteller misplay or observation. */
    fun acceptsCurrentAdvice(
        session: ClocktowerSessionState,
        proposed: InformationProposition.NumericResult,
    ): Boolean {
        val resolved = resolve(session, proposed.metric, proposed.sourceSeat) ?: return false
        return proposed.subjectSeats == resolved.subjectSeats && proposed.value in resolved.values
    }

    private fun livingNeighbours(circle: List<PlayerState>, actorSeat: Int): List<PlayerState> {
        val living = circle.filter(PlayerState::alive)
        val index = living.indexOfFirst { it.seat == actorSeat }
        if (index == -1 || living.size <= 1) return emptyList()
        return listOf(living[(index - 1 + living.size) % living.size],
            living[(index + 1) % living.size]).distinctBy(PlayerState::seat)
    }

    // An examined Spy may register as good; an examined Recluse may register as evil.
    // A poisoned special character registers normally, independently of the information source.
    internal fun evilRegistrations(player: PlayerState): List<Boolean> =
        when {
            hasFunctioningSpecialRegistration(player) && player.actualRole.value == "Spy" ->
                listOf(false, true)
            hasFunctioningSpecialRegistration(player) && player.actualRole.value == "Recluse" ->
                listOf(false, true)
            else -> listOf(player.actualAlignment == Alignment.EVIL)
        }

    private fun possibleEvilCounts(players: List<PlayerState>): Set<Int> {
        var counts = setOf(0)
        for (player in players) {
            counts = counts.flatMapTo(linkedSetOf()) { prior ->
                evilRegistrations(player).map { isEvil -> prior + if (isEvil) 1 else 0 }
            }
        }
        return counts
    }

    /**
     * Dynamic programming around the physical circle, including last -> first.
     * Only (previous registration, adjacent-pair count) is retained, never possible worlds.
     */
    private fun adjacentEvilPairCounts(circle: List<PlayerState>): Set<Int> {
        val result = linkedSetOf<Int>()
        for (first in evilRegistrations(circle.first())) {
            var partial = setOf(first to 0)
            for (player in circle.drop(1)) {
                partial = partial.flatMapTo(linkedSetOf()) { (previousEvil, count) ->
                    evilRegistrations(player).map { currentEvil ->
                        currentEvil to (count + if (previousEvil && currentEvil) 1 else 0)
                    }
                }
            }
            partial.forEach { (lastEvil, count) ->
                result += count + if (lastEvil && first) 1 else 0
            }
        }
        return result
    }
}

/** A distinct role-source label for the future single-column grouped Pair picker. */
internal enum class HostPairRoleSource { IN_PLAY, DEMON_BLUFF, SCRIPT_FALLBACK }
internal data class HostPairRoleOption(val role: RoleId, val source: HostPairRoleSource)
internal data class HostPairResolution(
    val options: List<HostPairRoleOption>,
    val ordinaryCandidateSeats: Set<Int>,
    val specialCandidateSeats: Set<Int>,
    val canShowNoOutsiders: Boolean,
    val sourceAbility: AbilityFunctioningState,
    val productShortlist: Boolean,
)

/** Query exactly one chosen physical pair, not all N×N pairs. */
internal object PairInformationResolver {
    fun resolve(
        session: ClocktowerSessionState,
        ruleset: ValidatedClocktowerRuleset,
        role: RoleId,
        sourceSeat: Int,
        firstSeat: Int?,
        secondSeat: Int?,
    ): HostPairResolution? {
        if (session.gameState.script != ruleset.script.id) return null
        val kind = when (role.value) {
            "Washerwoman" -> CharacterType.TOWNSFOLK
            "Librarian" -> CharacterType.OUTSIDER
            "Investigator" -> CharacterType.MINION
            else -> return null
        }
        val players = session.gameState.players
        val actor = session.gameState.playerAt(sourceSeat) ?: return null
        val source = sourceAbility(actor, role.value) ?: return null
        if (!actor.alive) return null
        val pair = when {
            firstSeat == null && secondSeat == null -> emptyList()
            firstSeat == null || secondSeat == null || firstSeat == secondSeat -> return null
            else -> listOfNotNull(session.gameState.playerAt(firstSeat),
                session.gameState.playerAt(secondSeat)).takeIf { it.size == 2 } ?: return null
        }
        val ordinary = players.filter { it.actualType == kind }.mapTo(linkedSetOf(), PlayerState::seat)
        val special = if (source == AbilityFunctioningState.FUNCTIONING) players.filter { player ->
            specialRegistersAs(player, kind)
        }.mapTo(linkedSetOf(), PlayerState::seat) else emptySet()
        val relevantSpecial = pair.any { it.seat in special }
        val discretionary = source != AbilityFunctioningState.FUNCTIONING || relevantSpecial
        val registry = ruleset.characters.associateBy { it.id }
        val scriptType = when (kind) {
            CharacterType.TOWNSFOLK -> ClocktowerCatalogTeam.TOWNSFOLK
            CharacterType.OUTSIDER -> ClocktowerCatalogTeam.OUTSIDER
            CharacterType.MINION -> ClocktowerCatalogTeam.MINION
            else -> return null
        }
        val allScriptRoles = ruleset.characters.filter { it.team == scriptType }.map { it.id }
        val actualRoleIds = players.filter { it.actualType == kind }.map { it.actualRole }
            .filter { registry[it]?.team == scriptType }.distinct()

        val options = when {
            !discretionary -> pair.filter { it.actualType == kind }.map { it.actualRole }
                .filter { registry[it]?.team == scriptType }.distinct()
                .map { HostPairRoleOption(it, HostPairRoleSource.IN_PLAY) }
            kind == CharacterType.TOWNSFOLK -> {
                val inPlay = actualRoleIds.map { HostPairRoleOption(it, HostPairRoleSource.IN_PLAY) }
                val bluff = session.demonBluffRoleIds.filter { registry[it]?.team == scriptType }
                    .filterNot { it in actualRoleIds }.distinct()
                    .map { HostPairRoleOption(it, HostPairRoleSource.DEMON_BLUFF) }
                (inPlay + bluff).ifEmpty {
                    // A Drunk information source must not become unplayable in a rare empty setup.
                    if (source != AbilityFunctioningState.FUNCTIONING) allScriptRoles.take(1)
                        .map { HostPairRoleOption(it, HostPairRoleSource.SCRIPT_FALLBACK) }
                    else emptyList()
                }
            }
            else -> allScriptRoles.map { HostPairRoleOption(it, HostPairRoleSource.SCRIPT_FALLBACK) }
        }
        // Librarian's zero means no actual Outsiders exist. Functioning Spy registration
        // may additionally support an Outsider result; never offer a fabricated '0 Minions'.
        val canShowZero = role.value == "Librarian" &&
            (source != AbilityFunctioningState.FUNCTIONING ||
                players.none { it.actualType == CharacterType.OUTSIDER })
        return HostPairResolution(options, ordinary, special, canShowZero, source, discretionary)
    }

    /** Applies the *current* pair query to an AI suggestion. It does not govern manual recording. */
    fun acceptsCurrentRoleAdvice(
        session: ClocktowerSessionState,
        ruleset: ValidatedClocktowerRuleset,
        role: RoleId,
        sourceSeat: Int,
        firstSeat: Int,
        secondSeat: Int,
        suggestedRole: RoleId,
    ): Boolean = resolve(session, ruleset, role, sourceSeat, firstSeat, secondSeat)
        ?.options?.any { it.role == suggestedRole } == true

    private fun specialRegistersAs(player: PlayerState, kind: CharacterType): Boolean =
        hasFunctioningSpecialRegistration(player) && when (player.actualRole.value) {
            "Spy" -> kind == CharacterType.TOWNSFOLK || kind == CharacterType.OUTSIDER
            "Recluse" -> kind == CharacterType.MINION
            else -> false
        }
}

/** Registration survives death but not poisoning; it never inherits source ability state. */
private fun hasFunctioningSpecialRegistration(player: PlayerState): Boolean {
    if (player.actualRole.value !in setOf("Spy", "Recluse")) return false
    return AbilityFunctioningSemantics.stateForEstablishedInteraction(
        AbilitySubject(player.actualRole.value, player.shownRole?.value, player.poisoned, player.alive),
        player.actualRole.value,
    ) == AbilityFunctioningState.FUNCTIONING
}

private fun sourceAbility(player: PlayerState, role: String): AbilityFunctioningState? =
    AbilityFunctioningSemantics.stateFor(
        AbilitySubject(player.actualRole.value, player.shownRole?.value, player.poisoned, player.alive),
        role,
    )
