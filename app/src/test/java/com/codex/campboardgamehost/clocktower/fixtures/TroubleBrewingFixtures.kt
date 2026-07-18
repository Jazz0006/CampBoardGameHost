package com.codex.campboardgamehost.clocktower.fixtures

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId

internal object TroubleBrewingFixtures {
    val scriptId = ScriptId("trouble_brewing")

    fun eightPlayerExample(): GameState = GameState(
        script = scriptId,
        seed = 20260718L,
        players = listOf(
            player(1, "Chef", CharacterType.TOWNSFOLK),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Fortune Teller", CharacterType.TOWNSFOLK),
            player(4, "Undertaker", CharacterType.TOWNSFOLK),
            player(5, "Virgin", CharacterType.TOWNSFOLK),
            player(6, "Drunk", CharacterType.OUTSIDER, shownRole = "Investigator"),
            player(7, "Scarlet Woman", CharacterType.MINION),
            player(8, "Imp", CharacterType.DEMON),
        ),
    )

    fun roleDefinitions(): List<RoleDefinition> = listOf(
        role("Chef", CharacterType.TOWNSFOLK),
        role("Empath", CharacterType.TOWNSFOLK),
        role("Fortune Teller", CharacterType.TOWNSFOLK),
        role("Undertaker", CharacterType.TOWNSFOLK),
        role("Virgin", CharacterType.TOWNSFOLK),
        role("Investigator", CharacterType.TOWNSFOLK),
        role("Monk", CharacterType.TOWNSFOLK),
        role("Soldier", CharacterType.TOWNSFOLK),
        role("Drunk", CharacterType.OUTSIDER),
        role("Butler", CharacterType.OUTSIDER),
        role("Poisoner", CharacterType.MINION),
        role("Spy", CharacterType.MINION),
        role("Baron", CharacterType.MINION),
        role("Scarlet Woman", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )

    fun fullRoleDefinitions(): List<RoleDefinition> = (
        roleDefinitions() + listOf(
            role("Washerwoman", CharacterType.TOWNSFOLK),
            role("Librarian", CharacterType.TOWNSFOLK),
            role("Ravenkeeper", CharacterType.TOWNSFOLK),
            role("Slayer", CharacterType.TOWNSFOLK),
            role("Mayor", CharacterType.TOWNSFOLK),
            role("Recluse", CharacterType.OUTSIDER),
            role("Saint", CharacterType.OUTSIDER),
        )
    ).distinctBy(RoleDefinition::id)

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String? = role,
    ): PlayerState = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = type.alignment(),
        actualType = type,
        shownRole = shownRole?.let(::RoleId),
    )

    private fun role(id: String, type: CharacterType): RoleDefinition = RoleDefinition(
        id = RoleId(id),
        alignment = type.alignment(),
        type = type,
        scriptIds = setOf(scriptId),
    )

    private fun CharacterType.alignment(): Alignment = when (this) {
        CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
        CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
    }
}
