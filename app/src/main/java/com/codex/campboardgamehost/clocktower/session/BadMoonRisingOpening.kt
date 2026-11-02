package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.badMoonRisingRoles
import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.setup.clocktowerSetupDistribution
import kotlin.random.Random

/** Pre-Session layout only. Generation, UI review and final creation share this validation. */
internal object BadMoonRisingOpening {
    val script = ScriptId("bad_moon_rising")
    enum class Error { PLAYERS, SEATS, ROLES, DISTRIBUTION, SHOWN_IDENTITY }
    private fun type(role: String) = when (badMoonRisingRoles.single { it.enName == role }.team) {
        ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
        ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
        ClocktowerTeam.Minion -> CharacterType.MINION
        ClocktowerTeam.Demon -> CharacterType.DEMON
    }
    private fun alignment(role: String) = if (type(role) in setOf(CharacterType.MINION, CharacterType.DEMON)) Alignment.EVIL else Alignment.GOOD
    private fun player(seat: Int, name: String, role: String, shown: RoleId) =
        PlayerState(seat, name, RoleId(role), alignment(role), type(role), shown)

    fun generate(names: List<String>, seed: Long): GameState {
        require(names.size in 5..15 && names.distinct().size == names.size && names.none { it.isBlank() })
        val random = Random(seed)
        val base = clocktowerSetupDistribution(names.size)
        fun pool(team: ClocktowerTeam) = badMoonRisingRoles.filter { it.team == team }.map { it.enName }.shuffled(random)
        val demons = pool(ClocktowerTeam.Demon).take(1)
        val minions = pool(ClocktowerTeam.Minion).take(base.minions)
        val outsiderCount = if ("Godfather" in minions)
            listOf(base.outsiders - 1, base.outsiders + 1).filter { it in 0..4 }.random(random) else base.outsiders
        val roles = (demons + minions + pool(ClocktowerTeam.Outsider).take(outsiderCount) +
            pool(ClocktowerTeam.Townsfolk).take(names.size - 1 - minions.size - outsiderCount)).shuffled(random)
        return GameState(script, roles.mapIndexed { index, role ->
            player(index + 1, names[index], role, RoleId(if (role == "Lunatic") demons.single() else role))
        }, seed).also(::requireValid)
    }

    /** Choosing an occupied role explicitly swaps roles, preserving names and physical seats. */
    fun assign(state: GameState, seat: Int, role: String): GameState {
        require(badMoonRisingRoles.any { it.enName == role })
        val old = requireNotNull(state.playerAt(seat))
        val other = state.players.firstOrNull { it.actualRole.value == role }
        return state.copy(players = state.players.map { p -> when {
            p.seat == seat -> player(p.seat, p.name, role,
                other?.shownRole ?: RoleId(if (role == "Lunatic") state.players.firstOrNull { it.actualType == CharacterType.DEMON }?.actualRole?.value ?: "Po" else role))
            other != null && p.seat == other.seat -> player(p.seat, p.name, old.actualRole.value, old.shownRole ?: old.actualRole)
            else -> p
        } })
    }

    fun errors(state: GameState): List<Error> = buildList {
        val players = state.players
        if (state.script != script || players.size !in 5..15 || players.any { it.name.isBlank() } || players.map { it.name }.distinct().size != players.size) add(Error.PLAYERS)
        if (players.map { it.seat }.sorted() != (1..players.size).toList()) add(Error.SEATS)
        val known = badMoonRisingRoles.map { it.enName }.toSet()
        if (players.any { it.actualRole.value !in known } || players.map { it.actualRole }.distinct().size != players.size) {
            add(Error.ROLES); return@buildList
        }
        if (players.any { it.actualType != type(it.actualRole.value) || it.actualAlignment != alignment(it.actualRole.value) }) add(Error.ROLES)
        if (players.size in 5..15) {
            val base = clocktowerSetupDistribution(players.size)
            val outsiders = players.count { it.actualType == CharacterType.OUTSIDER }
            val allowed = if (players.any { it.actualRole.value == "Godfather" }) listOf(base.outsiders - 1, base.outsiders + 1).filter { it in 0..4 } else listOf(base.outsiders)
            if (outsiders !in allowed || players.count { it.actualType == CharacterType.MINION } != base.minions ||
                players.count { it.actualType == CharacterType.DEMON } != 1) add(Error.DISTRIBUTION)
        }
        if (players.any { p -> if (p.actualRole.value == "Lunatic")
                badMoonRisingRoles.none { it.enName == p.shownRole?.value && it.team == ClocktowerTeam.Demon }
            else p.shownRole != null && p.shownRole != p.actualRole }) add(Error.SHOWN_IDENTITY)
    }
    fun requireValid(state: GameState) { require(errors(state).isEmpty()) { "Invalid BMR opening: ${errors(state)}" } }
}
