package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures

internal data class ExpertReviewScenario(
    val id: String,
    val game: GameState,
)

internal object ExpertReviewFixtures {
    private val definitions = TroubleBrewingFixtures.fullRoleDefinitions().associateBy { it.id.value }
    private val roles8A = listOf("Chef", "Empath", "Fortune Teller", "Undertaker", "Virgin", "Drunk", "Scarlet Woman", "Imp")
    private val roles8B = listOf("Washerwoman", "Librarian", "Fortune Teller", "Monk", "Soldier", "Drunk", "Poisoner", "Imp")
    private val roles8C = listOf("Investigator", "Chef", "Empath", "Fortune Teller", "Ravenkeeper", "Butler", "Spy", "Imp")
    private val roles8D = listOf("Washerwoman", "Librarian", "Chef", "Monk", "Virgin", "Drunk", "Baron", "Imp")
    private val roles10A = listOf("Washerwoman", "Chef", "Empath", "Fortune Teller", "Undertaker", "Monk", "Drunk", "Poisoner", "Spy", "Imp")
    private val roles10B = listOf("Investigator", "Chef", "Empath", "Fortune Teller", "Undertaker", "Virgin", "Slayer", "Poisoner", "Baron", "Imp")
    private val roles12A = listOf("Washerwoman", "Librarian", "Chef", "Empath", "Fortune Teller", "Undertaker", "Monk", "Drunk", "Recluse", "Poisoner", "Scarlet Woman", "Imp")
    private val roles12B = listOf("Investigator", "Chef", "Empath", "Fortune Teller", "Ravenkeeper", "Virgin", "Mayor", "Butler", "Saint", "Spy", "Baron", "Imp")
    private val roles7A = listOf("Chef", "Empath", "Fortune Teller", "Virgin", "Drunk", "Poisoner", "Imp")

    val scenarios: List<ExpertReviewScenario> = listOf(
        scenario("8A-0", roles8A, 0, "Investigator"),
        scenario("8A-2", roles8A, 2, "Investigator"),
        scenario("8A-5", roles8A, 5, "Investigator"),
        scenario("8B-0", roles8B, 0, "Investigator"),
        scenario("8B-3", roles8B, 3, "Empath"),
        scenario("8B-6", roles8B, 6, "Investigator"),
        scenario("8C-0", roles8C, 0),
        scenario("8C-4", roles8C, 4),
        scenario("8D-0", roles8D, 0, "Investigator"),
        scenario("8D-3", roles8D, 3, "Empath"),
        scenario("10A-0", roles10A, 0, "Investigator"),
        scenario("10A-2", roles10A, 2, "Washerwoman"),
        scenario("10A-5", roles10A, 5, "Librarian"),
        scenario("10A-8", roles10A, 8, "Investigator"),
        scenario("10B-0", roles10B, 0),
        scenario("10B-3", roles10B, 3),
        scenario("10B-7", roles10B, 7),
        scenario("12A-0", roles12A, 0, "Investigator"),
        scenario("12A-4", roles12A, 4, "Slayer"),
        scenario("12A-8", roles12A, 8, "Investigator"),
        scenario("12B-0", roles12B, 0),
        scenario("12B-5", roles12B, 5),
        scenario("7A-0", roles7A, 0, "Investigator"),
        scenario("7A-2", roles7A, 2, "Washerwoman"),
    )

    init {
        check(scenarios.size == 24)
        check(scenarios.map { it.id }.distinct().size == scenarios.size)
    }

    private fun scenario(
        id: String,
        roles: List<String>,
        rotation: Int,
        drunkShownRole: String? = null,
    ): ExpertReviewScenario {
        val rotated = roles.drop(rotation) + roles.take(rotation)
        val players = rotated.mapIndexed { index, roleName ->
            val definition = requireNotNull(definitions[roleName]) { "Unknown role $roleName" }
            PlayerState(
                seat = index + 1,
                name = "Player ${index + 1}",
                actualRole = definition.id,
                actualAlignment = definition.alignment,
                actualType = definition.type,
                shownRole = RoleId(if (roleName == "Drunk") requireNotNull(drunkShownRole) else roleName),
            )
        }
        return ExpertReviewScenario(
            id = id,
            game = GameState(
                script = TroubleBrewingFixtures.scriptId,
                players = players,
                seed = 20260718L + id.hashCode(),
            ),
        )
    }

}
