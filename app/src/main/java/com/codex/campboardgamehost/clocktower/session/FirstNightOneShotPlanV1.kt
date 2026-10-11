package com.codex.campboardgamehost.clocktower.session

import org.json.JSONArray
import org.json.JSONObject

/**
 * Host-scoped single-choice first-night bundle. The LLM sees the complete roster
 * and the Host's decision domains, then selects ONE legal candidate for every
 * decision currently known. This is a proposal; never a Host commit.
 *
 * A future Poisoner target, Fortune Teller query or an otherwise missing legal
 * domain must remain explicitly deferred until a real player action supplies it.
 * No analysis prose or fabricated registration witness belongs in the response.
 */
internal data class FirstNightOneShotDecisionScopeV1(
    val decisionId: String,
    val sourceSeat: Int?,
    val family: String,
    val legalCandidateIds: List<String>,
) {
    init {
        require(decisionId.isNotBlank() && decisionId.length <= 180)
        require(family.isNotBlank() && family.length <= 80)
        require(sourceSeat == null || sourceSeat > 0)
        require(legalCandidateIds.isNotEmpty() &&
            legalCandidateIds.size <= 512 &&
            legalCandidateIds.size == legalCandidateIds.distinct().size &&
            legalCandidateIds.all { it.isNotBlank() && it.length <= 280 })
    }
}

internal data class FirstNightOneShotScopeV1(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val legalDemonBluffRoleIds: List<String>?,
    val availableDecisions: List<FirstNightOneShotDecisionScopeV1>,
    val deferredDecisionIds: List<String>,
) {
    init {
        require(gameId.isNotBlank())
        require(gameStateRevision >= 0L && playerInputRevision >= 0L)
        require(availableDecisions.map { it.decisionId }.distinct().size ==
            availableDecisions.size)
        val allIds = availableDecisions.map { it.decisionId } + deferredDecisionIds
        require(allIds.size == allIds.distinct().size)
        require(deferredDecisionIds.all { it.isNotBlank() })
        legalDemonBluffRoleIds?.let { legal ->
            require(legal.size >= 3 && legal.size == legal.distinct().size)
            require(legal.all { it.isNotBlank() && it.length <= 80 })
        }
    }

    fun providerLegalScope(): JSONObject = JSONObject()
        .put("gameId", gameId)
        .put("gameStateRevision", gameStateRevision)
        .put("playerInputRevision", playerInputRevision)
        .put("legalDemonBluffRoleIds",
            legalDemonBluffRoleIds?.let(::JSONArray) ?: JSONObject.NULL)
        .put("availableDecisions", JSONArray().also { out ->
            availableDecisions.forEach { scope ->
                out.put(JSONObject()
                    .put("decisionId", scope.decisionId)
                    .put("sourceSeat", scope.sourceSeat ?: JSONObject.NULL)
                    .put("family", scope.family)
                    .put("legalCandidateIds", JSONArray(scope.legalCandidateIds)))
            }
        })
        .put("deferredDecisionIds", JSONArray(deferredDecisionIds))
}

internal data class FirstNightOneShotPlanV1(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val demonBluffRoleIds: List<String>?,
    val candidateByDecisionId: Map<String, String>,
    val deferredDecisionIds: List<String>,
) {
    /** Never accept a bundle against another game, revision or legal domain. */
    fun validFor(scope: FirstNightOneShotScopeV1): Boolean =
        runCatching { FirstNightOneShotPlanV1.validate(scope, this) }.isSuccess

    companion object {
        fun decode(raw: JSONObject, scope: FirstNightOneShotScopeV1): FirstNightOneShotPlanV1 {
            require(raw.getString("gameId") == scope.gameId)
            require(raw.getLong("gameStateRevision") == scope.gameStateRevision)
            require(raw.getLong("playerInputRevision") == scope.playerInputRevision)
            val bluffArray = raw.get("demonBluffRoleIds")
            val bluffs = when (bluffArray) {
                JSONObject.NULL -> null
                is JSONArray -> (0 until bluffArray.length()).map(bluffArray::getString)
                else -> error("Invalid Demon bluff recommendation")
            }
            val choices = raw.getJSONArray("choices")
            require(choices.length() <= 64)
            val map = linkedMapOf<String, String>()
            for (i in 0 until choices.length()) {
                val item = choices.getJSONObject(i)
                require(item.length() == 2)
                val id = item.getString("decisionId")
                require(map.put(id, item.getString("candidateId")) == null) {
                    "Duplicate first-night decision"
                }
            }
            val deferredArray = raw.getJSONArray("deferredDecisionIds")
            val deferred = (0 until deferredArray.length()).map(deferredArray::getString)
            return FirstNightOneShotPlanV1(
                scope.gameId, scope.gameStateRevision, scope.playerInputRevision,
                bluffs, map, deferred,
            ).also { validate(scope, it) }
        }

        fun validate(scope: FirstNightOneShotScopeV1, plan: FirstNightOneShotPlanV1) {
            require(plan.gameId == scope.gameId)
            require(plan.gameStateRevision == scope.gameStateRevision &&
                plan.playerInputRevision == scope.playerInputRevision) {
                "Stale first-night bundle"
            }
            if (scope.legalDemonBluffRoleIds == null) {
                require(plan.demonBluffRoleIds == null) {
                    "Demon bluffs are not applicable to this game"
                }
            } else {
                val chosen = requireNotNull(plan.demonBluffRoleIds) {
                    "A required Demon bluff trio cannot be omitted"
                }
                require(chosen.size == 3 && chosen.size == chosen.distinct().size &&
                    chosen.all { it in scope.legalDemonBluffRoleIds }) {
                    "Demon bluff triple is not Host legal"
                }
            }
            val legalById = scope.availableDecisions.associateBy { it.decisionId }
            require(plan.candidateByDecisionId.keys == legalById.keys) {
                "Single-choice package must cover every currently available decision"
            }
            plan.candidateByDecisionId.forEach { (id, candidate) ->
                require(candidate in requireNotNull(legalById[id]).legalCandidateIds) {
                    "First-night candidate is not Host legal"
                }
            }
            require(plan.deferredDecisionIds == scope.deferredDecisionIds) {
                "Unknown player actions must not be silently invented or skipped"
            }
        }
    }
}
