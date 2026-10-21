package com.codex.campboardgamehost

import androidx.compose.runtime.mutableStateMapOf

/** Draft Virgin/Spy type rulings; null means untouched, false is an explicit actual-type choice. */
internal class ClocktowerRegistrationInteractionState {
    private val spyRegistersTownsfolk = mutableStateMapOf<String, Boolean>()

    fun explicitChoice(key: String?): Boolean? = key?.let(spyRegistersTownsfolk::get)
    fun spyIsGood(key: String?): Boolean = explicitChoice(key) == true
    fun spyHasExplicitChoice(key: String?): Boolean = explicitChoice(key) != null
    fun chooseSpy(key: String, good: Boolean) {
        require(key.isNotBlank())
        spyRegistersTownsfolk[key] = good
    }
}
