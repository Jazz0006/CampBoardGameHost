package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * R3 end-to-end golden path. Unlike [A3GoldenContractRunner], these tests start from the
 * schema-v2 fixture catalog and invoke [TroubleBrewingWorldEnumerator] itself before
 * evaluating official information contracts. The fixture remains the source of ruleset,
 * role identity and representative scenario inputs; setup knowledge is intentionally
 * narrowed where needed so this is a correctness contract rather than a benchmark.
 */
class A3EndToEndGoldenContractTest {
    private val script = ScriptId("trouble_brewing")
    private val catalog: JSONObject by lazy { A3GoldenContractCatalog.loadDocument() }
    private val typedStates: Map<String, FormalGameState> by lazy {
        val states = catalog.getJSONObject("formalStates")
        states.keys().asSequence().associateWith { name ->
            EpistemicSemanticJson.decodeFormalGameState(states.getJSONObject(name).toString())
        }
    }
    private val roleCatalog: List<RoleDefinition> by lazy {
        typedStates.values
            .flatMap(FormalGameState::players)
            .associateBy(FormalPlayerState::actualRole)
            .values
            .map { player ->
                RoleDefinition(
                    id = player.actualRole,
                    alignment = player.actualAlignment,
                    type = player.actualType,
                    scriptIds = setOf(script),
                )
            }
    }

    @Test fun `schema v2 golden input reaches real enumerator with hidden Baron and Drunk alternatives`() {
        val state = typedState("baron8")
        val focusedRoleIds = setOf(
            "Chef", "Empath", "Fortune Teller", "Drunk", "Recluse",
            "Poisoner", "Spy", "Baron", "Imp",
        )
        val focusedRoles = roleCatalog.filter { it.id.value in focusedRoleIds }
        assertEquals(focusedRoleIds, focusedRoles.mapTo(linkedSetOf()) { it.id.value })

        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "r3-e2e-hidden-baron-drunk",
            formalSnapshotId = state.snapshotId,
            recipientSeat = 1,
            perceivedRole = RoleId("Chef"),
            setupKnowledge = listOf(InformationProposition.PlayerCount(5)),
        )
        val result = TroubleBrewingWorldEnumerator.enumerate(
            rulesetRef = state.rulesetRef,
            knowledge = knowledge,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = focusedRoles,
        )

        assertEquals(setOf(RoleId("Chef"), RoleId("Drunk")), result.possibleRoles(1))
        assertTrue(
            result.explanationClusters().worldCountByCluster
                .containsKey(WorldExplanationClusterId("baron-setup")),
        )
        assertTrue(result.enumeratedWorlds().any { world ->
            world.rolesBySeat[1] == RoleId("Chef")
        })
        assertTrue(result.enumeratedWorlds().any { world ->
            world.rolesBySeat[1] == RoleId("Drunk") &&
                world.shownRolesBySeat[1] == RoleId("Chef") &&
                world.abilityStatesBySeat[1] == AbilityState.MALFUNCTIONING_DRUNK
        })
    }

    @Test fun `poisoned Empath golden contract is satisfied only by an enumerated poison target variant`() {
        val state = typedState("poisoned_empath8")
        val scenario = scenario("TB-MAL-01")
        val query = scenario.getJSONObject("query")
        val sourceSeat = query.getInt("sourceSeat")
        val observation = privateObservation(
            id = scenario.getString("scenarioId"),
            state = state,
            sourceSeat = sourceSeat,
            sourceAbility = query.getString("ability"),
            proposition = InformationProposition.NumericResult(
                metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat = sourceSeat,
                subjectSeats = livingNeighbours(state, sourceSeat),
                value = query.getInt("value"),
            ),
        )
        val knowledge = fixedAssignmentKnowledge(state, sourceSeat, listOf(observation))

        val result = TroubleBrewingWorldEnumerator.enumerate(
            state.rulesetRef,
            knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleCatalog,
        )

        assertFalse(result.isEmpty())
        assertTrue(result.enumeratedWorlds().all { world ->
            world.abilityStatesBySeat[sourceSeat] == AbilityState.MALFUNCTIONING_POISONED
        })
        assertTrue(result.explanationClusters().worldCountByCluster
            .containsKey(WorldExplanationClusterId("poisoned-explanation")))
    }

    @Test fun `Fortune Teller golden path enumerates only actual-good red herrings and retains registration branch`() {
        val state = typedState("fortune_teller8")
        val knowledge = fixedAssignmentKnowledge(state, recipientSeat = 1)
        val base = TroubleBrewingWorldEnumerator.enumerate(
            state.rulesetRef,
            knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleCatalog,
        )
        val redHerringSeats = base.enumeratedWorlds().mapNotNullTo(linkedSetOf()) { it.redHerringSeat }

        assertTrue(6 in redHerringSeats) // Recluse is actual-good and may legally be selected.
        assertFalse(7 in redHerringSeats) // Spy is actual-evil even though it may register good.
        assertFalse(8 in redHerringSeats) // Imp is actual-evil.

        val scenario = scenario("TB-FT-03")
        val query = scenario.getJSONObject("query")
        val observation = privateObservation(
            id = scenario.getString("scenarioId"),
            state = state,
            sourceSeat = query.getInt("sourceSeat"),
            sourceAbility = query.getString("ability"),
            proposition = InformationProposition.BooleanResult(
                metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                sourceSeat = query.getInt("sourceSeat"),
                subjectSeats = query.getJSONArray("chosenSeats").ints(),
                value = query.getString("answer") == "yes",
            ),
        )

        assertFalse(base.require(observation).isEmpty())
        assertTrue(base.boundRegistrationFacts(observation).isNotEmpty())
    }

    @Test fun `Spy registration golden contract survives the real enumerator boundary`() {
        val state = typedState("base8_registration")
        val knowledge = fixedAssignmentKnowledge(state, recipientSeat = 1)
        val base = TroubleBrewingWorldEnumerator.enumerate(
            state.rulesetRef,
            knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleCatalog,
        )
        val scenario = scenario("TB-SPY-01")
        val query = scenario.getJSONObject("query")
        val shownRole = RoleId(query.getString("shownRole"))
        val observation = privateObservation(
            id = scenario.getString("scenarioId"),
            state = state,
            sourceSeat = query.getInt("sourceSeat"),
            sourceAbility = query.getString("ability"),
            proposition = InformationProposition.AnyOf(
                query.getJSONArray("pairSeats").ints().map { seat ->
                    InformationProposition.RoleAt(seat, shownRole)
                },
            ),
        )

        assertFalse(base.require(observation).isEmpty())
        assertTrue(base.boundRegistrationFacts(observation).isNotEmpty())
    }

    private fun typedState(name: String): FormalGameState = typedStates.getValue(name).also {
        assertEquals(EPISTEMIC_SCHEMA_VERSION, it.schemaVersion)
    }

    private fun scenario(id: String): JSONObject = catalog.getJSONArray("scenarios")
        .objects()
        .single { it.getString("scenarioId") == id }

    private fun fixedAssignmentKnowledge(
        state: FormalGameState,
        recipientSeat: Int,
        privateObservations: List<EpistemicObservation> = emptyList(),
    ): PlayerKnowledgeSnapshot {
        val recipient = state.players.single { it.seat == recipientSeat }
        return PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "r3-e2e-${state.snapshotId}-$recipientSeat-${privateObservations.size}",
            formalSnapshotId = state.snapshotId,
            recipientSeat = recipientSeat,
            perceivedRole = recipient.shownRole ?: recipient.actualRole,
            privateObservations = privateObservations,
            setupKnowledge = listOf(setupProfile(state)) + state.players.map { player ->
                InformationProposition.RoleAt(player.seat, player.actualRole)
            },
        )
    }

    private fun setupProfile(state: FormalGameState): InformationProposition.SetupProfile {
        val counts = state.players.groupingBy(FormalPlayerState::actualType).eachCount()
        return InformationProposition.SetupProfile(
            townsfolk = counts[CharacterType.TOWNSFOLK] ?: 0,
            outsiders = counts[CharacterType.OUTSIDER] ?: 0,
            minions = counts[CharacterType.MINION] ?: 0,
            demons = counts[CharacterType.DEMON] ?: 0,
        )
    }

    private fun privateObservation(
        id: String,
        state: FormalGameState,
        sourceSeat: Int,
        sourceAbility: String,
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = "r3-e2e-${id.lowercase()}",
        snapshotId = state.snapshotId,
        phase = state.phase,
        round = state.round,
        sequence = 1,
        sourceSeat = sourceSeat,
        sourceAbility = RoleId(sourceAbility),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(sourceSeat),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )

    private fun livingNeighbours(state: FormalGameState, sourceSeat: Int): List<Int> {
        val seats = state.players.map(FormalPlayerState::seat).sorted()
        val alive = state.players.filter(FormalPlayerState::alive).mapTo(hashSetOf(), FormalPlayerState::seat)
        val sourceIndex = seats.indexOf(sourceSeat)
        fun seek(step: Int): Int = generateSequence(sourceIndex + step) { it + step }
            .map { seats[(it % seats.size + seats.size) % seats.size] }
            .first { it in alive }
        return listOf(seek(-1), seek(1))
    }

    private fun JSONArray.objects(): List<JSONObject> =
        List(length()) { index -> getJSONObject(index) }

    private fun JSONArray.ints(): List<Int> =
        List(length()) { index -> getInt(index) }
}
