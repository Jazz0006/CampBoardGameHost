package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Typed A3 view of the A2.1 golden catalog. This prevents A3 from silently claiming coverage of
 * timeline contracts which remain B4 work, while making every golden ID enter an explicit review
 * bucket.
 */
class A3GoldenContractCatalogTest {
    @Test fun `every official golden contract has an explicit A3 disposition`() {
        val catalog = A3GoldenContractCatalog.load()

        assertEquals(48, catalog.size)
        assertEquals(48, catalog.map(A3GoldenContract::id).distinct().size)
        assertTrue(catalog.all { it.disposition != A3Disposition.UNCLASSIFIED })
        assertEquals(
            setOf("TB-SETUP-01", "TB-SETUP-02", "TB-WW-01", "TB-WW-02", "TB-LIB-01", "TB-LIB-02",
                "TB-LIB-03", "TB-INV-01", "TB-INV-02", "TB-INV-03", "TB-SPY-01", "TB-CHEF-01",
                "TB-CHEF-02", "TB-EMPATH-01", "TB-FT-01", "TB-FT-02", "TB-FT-03", "TB-FT-04",
                "TB-FT-05", "TB-MAL-01"),
            catalog.filter { it.disposition == A3Disposition.EXECUTE_NOW }.mapTo(linkedSetOf(), A3GoldenContract::id),
        )
    }

    @Test fun `A3 deferred contracts are only timeline or knowledge-projection work`() {
        val deferred = A3GoldenContractCatalog.load().filter { it.disposition == A3Disposition.DEFER_TO_B4 }

        assertTrue(deferred.isNotEmpty())
        assertTrue(deferred.all { it.queryKind == "official-contract" })
    }

    @Test fun `all A3 executable official contracts pass through EnumeratedWorldSet`() {
        val catalog = A3GoldenContractCatalog.loadDocument()
        val results = A3GoldenContractRunner(catalog).runAll()
        val failures = results.filterNot(A3ExecutionResult::passed)

        assertEquals(failures.joinToString("\n") { "${it.id}: ${it.detail}" }, 20, results.size)
        assertTrue(failures.joinToString("\n") { "${it.id}: ${it.detail}" }, failures.isEmpty())
    }

    @Test fun `A3 results preserve the frozen Oracle authority classifications`() {
        val catalog = A3GoldenContractCatalog.loadDocument()
        val results = A3GoldenContractRunner(catalog).runAll()
        val scenarios = catalog.getJSONArray("scenarios").objects().associateBy { it.getString("scenarioId") }
        val oracle = catalog.getJSONObject("oracle")
        val comparisons = results.associate { result ->
            val scenario = scenarios.getValue(result.id)
            result.id to when {
                !result.passed -> A3OracleComparison.UNEXPLAINED_MISMATCH
                scenario.has("mismatchDisposition") ->
                    A3OracleComparison.valueOf(scenario.getString("mismatchDisposition"))
                else -> A3OracleComparison.AGREE
            }
        }

        assertEquals(listOf("OFFICIAL", "PROJECT_GOLDEN", "EXTERNAL_ORACLE"),
            catalog.getJSONArray("authorityOrder").strings())
        assertEquals("pnkfelix/botc-asp", oracle.getString("repository"))
        assertEquals("616e61b720cc853af031f2623fd6bde33b869865", oracle.getString("revision"))
        assertEquals(18, comparisons.values.count { it == A3OracleComparison.AGREE })
        assertEquals(setOf("TB-LIB-03"), comparisons.filterValues {
            it == A3OracleComparison.EXPECTED_COVERAGE_GAP
        }.keys)
        assertEquals(setOf("TB-FT-04"), comparisons.filterValues {
            it == A3OracleComparison.KNOWN_ORACLE_VARIANCE
        }.keys)
        assertTrue(comparisons.values.none { it == A3OracleComparison.UNEXPLAINED_MISMATCH })
        assertTrue(comparisons.values.none { it == A3OracleComparison.NOT_RUN })
    }
}

internal enum class A3Disposition { EXECUTE_NOW, DEFER_TO_B4, UNCLASSIFIED }

internal data class A3GoldenContract(
    val id: String,
    val queryKind: String,
    val expectedStatus: String,
    val disposition: A3Disposition,
)

internal data class A3ExecutionResult(val id: String, val passed: Boolean, val detail: String)
internal enum class A3OracleComparison {
    AGREE, EXPECTED_COVERAGE_GAP, KNOWN_ORACLE_VARIANCE, UNEXPLAINED_MISMATCH, NOT_RUN,
}

/** Keeps the JSON catalog authoritative; this adapter deliberately does not duplicate its fixtures. */
internal object A3GoldenContractCatalog {
    private val executableQueryKinds = setOf("setup-profile", "pair-info", "no-outsiders", "numeric-info", "yes-no")
    private fun defaultCatalogFile(): File = sequenceOf(
        File("tools/asp_oracle/scenarios/trouble_brewing_a2.json"),
        File("../tools/asp_oracle/scenarios/trouble_brewing_a2.json"),
    ).firstOrNull(File::isFile) ?: File("tools/asp_oracle/scenarios/trouble_brewing_a2.json")

    fun load(catalogFile: File = defaultCatalogFile()): List<A3GoldenContract> {
        require(catalogFile.isFile) { "Missing A2.1 golden catalog: ${catalogFile.path}" }
        val root = loadDocument(catalogFile)
        require(root.getInt("schemaVersion") == 2)
        return root.getJSONArray("scenarios").let { scenarios ->
            List(scenarios.length()) { index ->
                val scenario = scenarios.getJSONObject(index)
                val queryKind = scenario.getJSONObject("query").getString("kind")
                A3GoldenContract(
                    id = scenario.getString("scenarioId"),
                    queryKind = queryKind,
                    expectedStatus = scenario.getString("expectedStatus"),
                    disposition = when {
                        queryKind in executableQueryKinds -> A3Disposition.EXECUTE_NOW
                        queryKind == "official-contract" -> A3Disposition.DEFER_TO_B4
                        else -> A3Disposition.UNCLASSIFIED
                    },
                )
            }
        }
    }


    fun loadDocument(catalogFile: File = defaultCatalogFile()): JSONObject {
        require(catalogFile.isFile) { "Missing A2.1 golden catalog: ${catalogFile.path}" }
        return JSONObject(catalogFile.readText()).also { require(it.getInt("schemaVersion") == 2) }
    }
}

internal class A3GoldenContractRunner(private val catalog: JSONObject) {
    private val script = ScriptId("trouble_brewing")
    private val roles: List<RoleDefinition> = buildRoleCatalog(catalog.getJSONObject("formalStates"))

    fun runAll(useZdd: Boolean = false): List<A3ExecutionResult> = catalog.getJSONArray("scenarios").objects()
        .filter { it.getJSONObject("query").getString("kind") in EXECUTABLE_QUERY_KINDS }
        .map { run(it, useZdd) }

    private fun run(scenario: JSONObject, useZdd: Boolean): A3ExecutionResult {
        val id = scenario.getString("scenarioId")
        return try {
            val state = catalog.getJSONObject("formalStates").getJSONObject(scenario.getString("stateRef"))
            val query = scenario.getJSONObject("query")
            val outcome = execute(id, state, scenario.getInt("perspectiveSeat"), query, useZdd)
            val expected = scenario.getString("expectedStatus")
            val passed = when (expected) {
                "SAT" -> outcome.selectedMatches
                "UNSAT" -> !outcome.selectedMatches
                "CHOICE" -> outcome.outputs.containsAll(expectedOutputs(scenario)) && outcome.outputs.size >= 2
                else -> false
            }
            val needsBoundRegistration = scenario.getJSONArray("officialAssertions").strings()
                .any { it.contains("registration_bound_to_output") } || id == "TB-SPY-01"
            A3ExecutionResult(
                id,
                passed && (!needsBoundRegistration || outcome.hasBoundRegistration),
                "expected=$expected selected=${outcome.selectedMatches} outputs=${outcome.outputs} " +
                    "boundRegistration=${outcome.hasBoundRegistration}",
            )
        } catch (error: IllegalArgumentException) {
            A3ExecutionResult(id, scenario.getString("expectedStatus") == "UNSAT", "rejected: ${error.message}")
        }
    }

    private data class Outcome(
        val selectedMatches: Boolean,
        val outputs: Set<String> = emptySet(),
        val hasBoundRegistration: Boolean = false,
    )

    private fun execute(id: String, state: JSONObject, recipientSeat: Int, query: JSONObject, useZdd: Boolean): Outcome {
        val players = state.getJSONArray("players").objects()
        val ruleset = state.getJSONObject("rulesetRef").let {
            RulesetRef(script, it.getString("scriptContentHash"), it.getString("rulesetVersion"),
                it.getString("sourceRevision"), RuleCoverage.valueOf(it.getString("coverage")))
        }
        val profile = setupProfile(players)
        val perceivedRole = players.single { it.getInt("seat") == recipientSeat }.optString("shownRole")
        val knowledge = PlayerKnowledgeSnapshot(
            knowledgeSnapshotId = "knowledge-${id.lowercase()}",
            formalSnapshotId = state.getString("snapshotId"),
            recipientSeat = recipientSeat,
            perceivedRole = RoleId(perceivedRole),
            setupKnowledge = listOf(profile),
        )
        val redHerring = if (query.has("redHerringSeat")) query.getInt("redHerringSeat") else null
        val world = EnumeratedWorld(
            rolesBySeat = players.associateTo(linkedMapOf()) { it.getInt("seat") to RoleId(it.getString("actualRole")) },
            redHerringSeat = redHerring,
            shownRolesBySeat = players.filter { it.optString("shownRole") != it.getString("actualRole") }
                .associate { it.getInt("seat") to RoleId(it.getString("shownRole")) },
            aliveSeats = players.filter { it.getBoolean("alive") }.mapTo(linkedSetOf()) { it.getInt("seat") },
            abilityStatesBySeat = players.mapNotNull { player ->
                when {
                    player.getBoolean("poisoned") -> player.getInt("seat") to AbilityState.MALFUNCTIONING_POISONED
                    player.getString("actualRole").equals("Drunk", true) ->
                        player.getInt("seat") to AbilityState.MALFUNCTIONING_DRUNK
                    else -> null
                }
            }.toMap(),
        )
        val enumerated = EnumeratedWorldSet.fromWorlds(
            ruleset, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, roles, listOf(world),
        )
        val set: PlayerWorldSet = if (useZdd) ZddPlayerWorldSet.fromEnumerated(enumerated) else enumerated
        return when (query.getString("kind")) {
            "setup-profile" -> Outcome(!set.isEmpty() && profile == query.setupProfile())
            "no-outsiders" -> Outcome(profile.outsiders == 0, setOf("no-outsiders"))
            "pair-info" -> evaluatePair(id, state, set, query)
            "numeric-info" -> evaluateNumeric(id, state, set, query, players.size)
            "yes-no" -> evaluateYesNo(id, state, set, query)
            else -> error("Unsupported A3 query")
        }
    }

    private fun evaluatePair(id: String, state: JSONObject, set: PlayerWorldSet, query: JSONObject): Outcome {
        val role = RoleId(query.getString("shownRole"))
        val observation = observation(id, state, query, InformationProposition.AnyOf(
            query.getJSONArray("pairSeats").ints().map { InformationProposition.RoleAt(it, role) },
        ))
        val filtered = set.require(observation)
        return Outcome(!filtered.isEmpty(), setOf(role.value), boundRegistrationFacts(set, observation).isNotEmpty())
    }

    private fun evaluateNumeric(
        id: String, state: JSONObject, set: PlayerWorldSet, query: JSONObject, playerCount: Int,
    ): Outcome {
        val ability = query.getString("ability")
        val source = query.getInt("sourceSeat")
        val subjects = if (ability == "Chef") (1..playerCount).toList() else livingNeighbours(source, state)
        val metric = if (ability == "Chef") NumericMetric.ADJACENT_EVIL_PAIRS else NumericMetric.LIVING_EVIL_NEIGHBOURS
        val max = if (ability == "Chef") playerCount else 2
        val evaluations = (0..max).associate { value ->
            val observation = observation(id, state, query,
                InformationProposition.NumericResult(metric, source, subjects, value), suffix = value.toString())
            observation to !set.require(observation).isEmpty()
        }
        val outputs = evaluations.filterValues { it }.keys.mapTo(linkedSetOf()) { it.proposition.let { p -> (p as InformationProposition.NumericResult).value }.toString() }
        val selected = query.getInt("value").toString() in outputs
        return Outcome(selected, outputs, evaluations.filterValues { it }.keys.any {
            boundRegistrationFacts(set, it).isNotEmpty()
        })
    }

    private fun evaluateYesNo(id: String, state: JSONObject, set: PlayerWorldSet, query: JSONObject): Outcome {
        val outputs = linkedSetOf<String>()
        var bound = false
        for (value in listOf(true, false)) {
            val observation = observation(id, state, query, InformationProposition.BooleanResult(
                BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
                query.getInt("sourceSeat"), query.getJSONArray("chosenSeats").ints(), value,
            ), suffix = value.toString())
            if (!set.require(observation).isEmpty()) {
                outputs += if (value) "yes" else "no"
                bound = bound || boundRegistrationFacts(set, observation).isNotEmpty()
            }
        }
        return Outcome(query.getString("answer") in outputs, outputs, bound)
    }

    private fun observation(
        id: String, state: JSONObject, query: JSONObject, proposition: InformationProposition, suffix: String = "selected",
    ) = EpistemicObservation(
        observationId = "golden-${id.lowercase()}-$suffix",
        snapshotId = state.getString("snapshotId"),
        phase = StorytellerPhase.valueOf(state.getString("phase")),
        round = state.getInt("round"), sequence = 1,
        sourceSeat = query.optInt("sourceSeat").takeIf { it > 0 },
        sourceAbility = query.optString("ability").takeIf(String::isNotBlank)?.let(::RoleId),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(query.optInt("sourceSeat").takeIf { it > 0 } ?: 1),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )

    private fun livingNeighbours(source: Int, state: JSONObject): List<Int> {
        val players = state.getJSONArray("players").objects()
        val seats = players.map { it.getInt("seat") }.sorted()
        val alive = players.filter { it.getBoolean("alive") }.mapTo(hashSetOf()) { it.getInt("seat") }
        val index = seats.indexOf(source)
        fun seek(step: Int): Int = generateSequence(index + step) { it + step }
            .map { seats[(it % seats.size + seats.size) % seats.size] }
            .first { it in alive }
        return listOf(seek(-1), seek(1))
    }

    private fun boundRegistrationFacts(
        set: PlayerWorldSet,
        observation: EpistemicObservation,
    ) = when (set) {
        is EnumeratedWorldSet -> set.boundRegistrationFacts(observation)
        is ZddPlayerWorldSet -> set.boundRegistrationFacts(observation)
        else -> emptySet()
    }

    private fun expectedOutputs(scenario: JSONObject): Set<String> = scenario.optJSONArray("outputAssertions")
        ?.objects().orEmpty().flatMap { it.getJSONArray("atoms").strings() }.mapNotNullTo(linkedSetOf()) { atom ->
            Regex("count\\((\\d+)\\)").find(atom)?.groupValues?.get(1)
                ?: Regex("oracle_output\\((yes|no)\\)").find(atom)?.groupValues?.get(1)
        }

    private fun setupProfile(players: List<JSONObject>): InformationProposition.SetupProfile {
        val counts = players.groupingBy { CharacterType.valueOf(it.getString("actualType")) }.eachCount()
        return InformationProposition.SetupProfile(
            counts[CharacterType.TOWNSFOLK] ?: 0, counts[CharacterType.OUTSIDER] ?: 0,
            counts[CharacterType.MINION] ?: 0, counts[CharacterType.DEMON] ?: 0,
        )
    }

    private fun JSONObject.setupProfile(): InformationProposition.SetupProfile = getJSONObject("profile").let {
        InformationProposition.SetupProfile(it.getInt("townsfolk"), it.getInt("outsiders"),
            it.getInt("minions"), it.getInt("demons"))
    }

    private fun buildRoleCatalog(states: JSONObject): List<RoleDefinition> = states.keys().asSequence()
        .flatMap { states.getJSONObject(it).getJSONArray("players").objects().asSequence() }
        .associateBy { it.getString("actualRole") }.values.map { player ->
            RoleDefinition(RoleId(player.getString("actualRole")), Alignment.valueOf(player.getString("actualAlignment")),
                CharacterType.valueOf(player.getString("actualType")), setOf(script))
        }

    companion object {
        private val EXECUTABLE_QUERY_KINDS = setOf("setup-profile", "pair-info", "no-outsiders", "numeric-info", "yes-no")
    }
}

private fun JSONArray.objects(): List<JSONObject> = List(length()) { index -> getJSONObject(index) }
private fun JSONArray.ints(): List<Int> = List(length()) { index -> getInt(index) }
private fun JSONArray.strings(): List<String> = List(length()) { index -> getString(index) }
