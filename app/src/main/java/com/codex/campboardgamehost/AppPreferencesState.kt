package com.codex.campboardgamehost

import androidx.compose.runtime.*
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.session.OpeningPlayerExperienceV1

/** Roster and durable player preferences, separate from an active game's facts/mode. */
internal class AppPreferencesState(private val store: AppPreferencesStore) {
    var languageMode by mutableStateOf(store.loadLanguageMode())
        private set
    val playerNames = mutableStateListOf<String>()
    val commonPlayers = mutableStateListOf<String>().apply { addAll(store.loadCommonPlayers()) }
    val playerExperienceByName = mutableStateMapOf<String, PlayerExperienceLevelV1>().apply {
        putAll(store.loadPlayerExperienceByName())
    }
    var newCommonPlayerName by mutableStateOf("")
    fun setLanguage(mode: LanguageMode) { languageMode = mode; store.saveLanguageMode(mode) }
    fun experience(name: String) = playerExperienceByName[name] ?: PlayerExperienceLevelV1.BEGINNER
    fun openingExperience(name: String) = when (experience(name)) {
        PlayerExperienceLevelV1.BEGINNER -> OpeningPlayerExperienceV1.BEGINNER
        PlayerExperienceLevelV1.NORMAL -> OpeningPlayerExperienceV1.REGULAR
        PlayerExperienceLevelV1.EXPERT -> OpeningPlayerExperienceV1.VETERAN
    }
    fun setExperience(name: String, level: PlayerExperienceLevelV1) {
        require(name.isNotBlank())
        if (level == PlayerExperienceLevelV1.BEGINNER) playerExperienceByName.remove(name)
        else playerExperienceByName[name] = level
        store.savePlayerExperienceByName(playerExperienceByName)
    }
    fun addPlayer(name: String) {
        val value = name.trim()
        if (value.isNotEmpty() && playerNames.size < MAX_PLAYERS && value !in playerNames) playerNames.add(value)
    }
    fun removePlayer(index: Int) { if (index in playerNames.indices) playerNames.removeAt(index) }
    fun movePlayer(index: Int, target: Int) {
        if (index !in playerNames.indices || target !in playerNames.indices || index == target) return
        val reordered = reorderHostTableItems(playerNames, index, target)
        playerNames.clear(); playerNames.addAll(reordered)
    }
    fun addCommonPlayer() {
        val value = newCommonPlayerName.trim()
        if (value.isNotEmpty() && value !in commonPlayers) {
            commonPlayers.add(value); store.saveCommonPlayers(commonPlayers); newCommonPlayerName = ""
        }
    }
    fun removeCommonPlayer(name: String) { commonPlayers.remove(name); store.saveCommonPlayers(commonPlayers) }
}
