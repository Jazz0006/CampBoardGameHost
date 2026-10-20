package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import org.json.JSONArray
import org.json.JSONObject

/** Only the single confirmed Session is saved. Preview, AI requests and UI navigation are absent. */
internal object ClocktowerSessionJson {
    fun encode(s: ClocktowerSessionState): String = JSONObject().apply {
        put("version", 1); put("gameId", s.gameId); put("revision", s.gameStateRevision)
        put("inputRevision", s.playerInputRevision); put("cursor", s.nextTimelineGlobalSequence)
        put("seed", s.gameSeed); put("script", s.gameState.script.value)
        put("players", JSONArray(s.gameState.players.map { p -> JSONObject().apply {
            put("seat", p.seat); put("name", p.name); put("role", p.actualRole.value)
            put("alignment", p.actualAlignment.name); put("type", p.actualType.name)
            put("shown", p.shownRole?.value); put("alive", p.alive); put("poisoned", p.poisoned)
        } }))
        put("actions", JSONArray(s.actionTimeline.entries.map { e -> action(e.fact).apply {
            put("id", e.fact.actionId); put("global", e.point.globalSequence)
            put("phase", e.point.phase.name); put("round", e.point.round); put("local", e.point.sequence)
        } }))
        put("observations", JSONArray(s.epistemicObservationLog.records.map { JSONObject(EpistemicSemanticJson.encode(it)) }))
        put("contexts", JSONArray(s.storytellerPlayerContextBySeat.map { (seat, c) -> JSONObject().apply {
            put("seat", seat); put("experience", c.experienceLevel.name)
            put("claims", JSONArray(c.claimedRoleIds.map { it.value })); put("pressure", c.pressureLevel?.name)
        } }))
    }.toString()

    fun decode(text: String): ClocktowerSessionState {
        val j = JSONObject(text)
        require(j.getInt("version") == 1)
        val seed = j.getLong("seed")
        val game = GameState(ScriptId(j.getString("script")), j.getJSONArray("players").objects().map { p ->
            PlayerState(p.getInt("seat"), p.getString("name"), RoleId(p.getString("role")),
                Alignment.valueOf(p.getString("alignment")), CharacterType.valueOf(p.getString("type")),
                p.stringOrNull("shown")?.let(::RoleId), p.getBoolean("alive"), p.getBoolean("poisoned"))
        }, seed)
        require(game.script.value in setOf("trouble_brewing", "no_greater_joy"))
        require(game.players.map { it.seat }.sorted() == (1..game.players.size).toList())
        require(game.players.map { it.name }.distinct().size == game.players.size)
        require(game.players.size in if(game.script.value == "no_greater_joy") 5..6 else 5..15)
        val actions = ActionFactTimeline(j.getJSONArray("actions").objects().map { e ->
            TimelineBoundActionFact(action(e), TimelinePoint(StorytellerPhase.valueOf(e.getString("phase")),
                e.getInt("round"), e.getInt("local"), e.getLong("global")))
        })
        val observations = EpistemicObservationLog(j.getJSONArray("observations").objects().map {
            EpistemicSemanticJson.decodeRecordedEpistemicObservation(it.toString())
        })
        val currentPhase = actions.entries.lastOrNull { it.fact is ActionFact.PhaseAdvance }?.fact as? ActionFact.PhaseAdvance
        val round = currentPhase?.round ?: 1
        val phase = currentPhase?.phase ?: StorytellerPhase.FIRST_NIGHT
        var targets = ClocktowerNightTargets()
        var standing = ClocktowerVoteStanding()
        var redHerring: Int? = null
        var bluffs = emptyList<RoleId>()
        // Standing uses the alive count at EACH vote, not the final count after later deaths.
        // Deaths are irreversible in the supported scripts; start at all seats alive.
        val dead = mutableSetOf<Int>()
        actions.entries.forEach { e ->
            targets = targets.after(e, null, true)
            when(val f = e.fact) {
                is ActionFact.RedHerringChoice -> redHerring = f.selectedSeat
                is ActionFact.DemonBluffChoice -> bluffs = f.roles
                is ActionFact.Death -> dead.add(f.targetSeat)
                is ActionFact.Execution -> dead.add(f.targetSeat)
                is ActionFact.Vote -> standing = standing.confirm(f.nomineeSeat, f.voterSeats.size, (game.players.size - dead.size + 1) / 2)
                is ActionFact.PhaseAdvance -> standing = ClocktowerVoteStanding()
                else -> Unit
            }
        }
        val contexts = j.getJSONArray("contexts").objects().associate { c -> c.getInt("seat") to StorytellerPlayerContextInputV1(
            PlayerExperienceLevelV1.valueOf(c.getString("experience")), c.getJSONArray("claims").strings().map(::RoleId),
            c.stringOrNull("pressure")?.let(StorytellerDeclaredPressureLevelV1::valueOf)) }
        val restored = ClocktowerSessionState(j.getString("gameId"), j.getLong("revision"), j.getLong("inputRevision"), seed,
            game, actions, observations, ClocktowerSemanticHistoryMode.GLOBAL_V1, j.getLong("cursor"), contexts,
            targets, standing, redHerring, bluffs)
        require(restored.phase == phase && restored.round == round)
        require(actions.entries.all { entry -> when(val f = entry.fact) {
            is ActionFact.Death -> game.playerAt(f.targetSeat)?.alive == false
            is ActionFact.Execution -> game.playerAt(f.targetSeat)?.alive == false
            else -> true
        } })
        return restored
    }

    private fun action(f: ActionFact): JSONObject = JSONObject().apply {
        put("kind", when(f) {
            is ActionFact.Poison -> "Poison"
            is ActionFact.Protect -> "Protect"
            is ActionFact.Attack -> "Attack"
            is ActionFact.Death -> "Death"
            is ActionFact.Execution -> "Execution"
            is ActionFact.NoExecution -> "NoExecution"
            is ActionFact.SlayerShot -> "SlayerShot"
            is ActionFact.Nomination -> "Nomination"
            is ActionFact.Vote -> "Vote"
            is ActionFact.KlutzChoice -> "KlutzChoice"
            is ActionFact.RoleChange -> "RoleChange"
            is ActionFact.ButlerMasterChoice -> "ButlerMasterChoice"
            is ActionFact.RedHerringChoice -> "RedHerringChoice"
            is ActionFact.MayorRedirectChoice -> "MayorRedirectChoice"
            is ActionFact.DemonBluffChoice -> "DemonBluffChoice"
            is ActionFact.NightWake -> "NightWake"
            is ActionFact.PhaseAdvance -> "PhaseAdvance"
            is ActionFact.GameEnded -> "GameEnded"
            is ActionFact.ArtistAnswer -> "ArtistAnswer"
        })
        when(f) {
            is ActionFact.Poison -> put("target", f.targetSeat)
            is ActionFact.Protect -> put("target", f.targetSeat)
            is ActionFact.Attack -> put("target", f.targetSeat)
            is ActionFact.Death -> { put("target", f.targetSeat); proof(f.klutzDeathTrigger)?.let { put("proof", it) } }
            is ActionFact.Execution -> { put("target", f.targetSeat); proof(f.klutzDeathTrigger)?.let { put("proof", it) } }
            is ActionFact.NoExecution -> Unit
            is ActionFact.SlayerShot -> { put("actor", f.claimantSeat); put("target", f.targetSeat); put("consumed", f.abilityConsumed); put("hit", f.hit); put("registration", f.targetRegistersAsDemon) }
            is ActionFact.Nomination -> { put("actor", f.nominatorSeat); put("target", f.nomineeSeat); put("first", f.firstVirginNomination); put("registration", f.nominatorRegistersAsTownsfolk) }
            is ActionFact.Vote -> { put("actor", f.nominatorSeat); put("target", f.nomineeSeat); put("voters", JSONArray(f.voterSeats)); put("ghosts", JSONArray(f.ghostVoterSeats)) }
            is ActionFact.KlutzChoice -> { put("actor", f.klutzSeat); put("target", f.chosenSeat); put("death", f.deathActionId) }
            is ActionFact.RoleChange -> { put("target", f.targetSeat); put("role", f.role.value); put("alignment", f.alignment.name); put("type", f.type.name) }
            is ActionFact.ButlerMasterChoice -> { put("actor", f.butlerSeat); put("target", f.masterSeat) }
            is ActionFact.RedHerringChoice -> put("target", f.selectedSeat)
            is ActionFact.MayorRedirectChoice -> { put("actor", f.mayorSeat); put("target", f.targetSeat) }
            is ActionFact.DemonBluffChoice -> put("roles", JSONArray(f.roles.map { it.value }))
            is ActionFact.NightWake -> { put("actor", f.actorSeat); put("role", f.role.value); put("step", f.stepId) }
            is ActionFact.PhaseAdvance -> { put("nextPhase", f.phase.name); put("nextRound", f.round) }
            is ActionFact.GameEnded -> { put("reason", f.reason.name); put("target", f.subjectSeat) }
            is ActionFact.ArtistAnswer -> { put("actor", f.claimantSeat); put("answer", f.shownAnswer); put("consumed", f.abilityConsumed) }
        }
    }
    private fun action(j: JSONObject): ActionFact {
        val id = j.getString("id"); val seq = j.getLong("global")
        fun target() = j.getInt("target")
        fun actor() = j.getInt("actor")
        fun registration() = if(j.isNull("registration")) null else j.getBoolean("registration")
        return when(j.getString("kind")) {
            "Poison" -> ActionFact.Poison(id, seq, j.intOrNull("target"))
            "Protect" -> ActionFact.Protect(id, seq, target())
            "Attack" -> ActionFact.Attack(id, seq, target())
            "Death" -> ActionFact.Death(id, seq, target(), j.optJSONObject("proof")?.let(::proof))
            "Execution" -> ActionFact.Execution(id, seq, target(), j.optJSONObject("proof")?.let(::proof))
            "NoExecution" -> ActionFact.NoExecution(id, seq)
            "SlayerShot" -> ActionFact.SlayerShot(id, seq, actor(), target(), j.getBoolean("consumed"), j.getBoolean("hit"), registration())
            "Nomination" -> ActionFact.Nomination(id, seq, actor(), target(), j.getBoolean("first"), registration())
            "Vote" -> ActionFact.Vote(id, seq, actor(), target(), j.getJSONArray("voters").ints(), j.getJSONArray("ghosts").ints())
            "KlutzChoice" -> ActionFact.KlutzChoice(id, seq, actor(), target(), j.getString("death"))
            "RoleChange" -> ActionFact.RoleChange(id, seq, target(), RoleId(j.getString("role")), Alignment.valueOf(j.getString("alignment")), CharacterType.valueOf(j.getString("type")))
            "ButlerMasterChoice" -> ActionFact.ButlerMasterChoice(id, seq, actor(), target())
            "RedHerringChoice" -> ActionFact.RedHerringChoice(id, seq, j.intOrNull("target"))
            "MayorRedirectChoice" -> ActionFact.MayorRedirectChoice(id, seq, actor(), target())
            "DemonBluffChoice" -> ActionFact.DemonBluffChoice(id, seq, j.getJSONArray("roles").strings().map(::RoleId))
            "NightWake" -> ActionFact.NightWake(id, seq, actor(), RoleId(j.getString("role")), j.getString("step"))
            "PhaseAdvance" -> ActionFact.PhaseAdvance(id, seq, StorytellerPhase.valueOf(j.getString("nextPhase")), j.getInt("nextRound"))
            "GameEnded" -> ActionFact.GameEnded(id, seq, GameEndReason.valueOf(j.getString("reason")), j.intOrNull("target"))
            "ArtistAnswer" -> ActionFact.ArtistAnswer(id, seq, actor(), j.getBoolean("answer"), j.getBoolean("consumed"))
            else -> error("Unsupported confirmed action")
        }
    }
    private fun proof(p: KlutzDeathTriggerEvidenceV1?): JSONObject? = p?.let {
        JSONObject().put("role", it.actualRole.value).put("alive", it.wasAlive).put("poisoned", it.wasPoisoned).put("revision", it.sourceGameStateRevision)
    }
    private fun proof(j: JSONObject) = KlutzDeathTriggerEvidenceV1(RoleId(j.getString("role")), j.getBoolean("alive"), j.getBoolean("poisoned"), j.getLong("revision"))
    private fun JSONArray.objects() = (0 until length()).map(::getJSONObject)
    private fun JSONArray.ints() = (0 until length()).map(::getInt)
    private fun JSONArray.strings() = (0 until length()).map(::getString)
    private fun JSONObject.intOrNull(key: String) = if(isNull(key)) null else getInt(key)
    private fun JSONObject.stringOrNull(key: String) = if(isNull(key)) null else getString(key)
}
