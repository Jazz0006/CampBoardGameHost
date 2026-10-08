package com.codex.campboardgamehost

import androidx.compose.runtime.mutableStateMapOf

/** UI-local Spy/Recluse selections and per-interaction recording markers. */
internal class ClocktowerRegistrationInteractionState {
    private val spyRegistersGood = mutableStateMapOf<String, Boolean>()
    private val spyRoles = mutableStateMapOf<String, String>()
    private val recordedSpy = mutableStateMapOf<String, Boolean>()
    private val recluseRegistersEvil = mutableStateMapOf<String, Boolean>()
    private val recluseRoles = mutableStateMapOf<String, String>()
    private val recordedRecluse = mutableStateMapOf<String, Boolean>()

    fun spyIsGood(key: String?): Boolean = key != null && spyRegistersGood[key] == true

    fun spyRole(key: String?): String? = key?.let(spyRoles::get)

    /** Explicit manual toggles retain the last role; displayed results do not change the ruling. */
    fun chooseSpy(key: String, good: Boolean, defaultRole: String? = null) {
        spyRegistersGood[key] = good
        if (good && defaultRole != null && spyRoles[key] == null) spyRoles[key] = defaultRole
    }

    fun chooseSpyRole(key: String, role: String) {
        spyRoles[key] = role
    }

    /** Only an explicit manual choice can be committed as a registration ruling. */
    fun spyHasExplicitChoice(key: String?): Boolean = key != null && spyRegistersGood.containsKey(key)

    fun spyWillRecord(key: String?): Boolean =
        spyHasExplicitChoice(key) && recordedSpy[key] != true

    fun markSpyRecorded(key: String): Boolean {
        if (!spyHasExplicitChoice(key) || recordedSpy[key] == true) return false
        recordedSpy[key] = true
        return true
    }

    fun recluseIsEvil(key: String?): Boolean = key != null && recluseRegistersEvil[key] == true

    fun recluseHasExplicitChoice(key: String?): Boolean =
        key != null && recluseRegistersEvil.containsKey(key)

    fun recluseRole(key: String?): String? = key?.let(recluseRoles::get)

    fun chooseRecluse(key: String, evil: Boolean, defaultRole: String? = null) {
        recluseRegistersEvil[key] = evil
        if (evil && defaultRole != null && recluseRoles[key] == null) recluseRoles[key] = defaultRole
    }

    fun chooseRecluseRole(key: String, role: String) {
        recluseRoles[key] = role
    }

    fun markRecluseRecorded(key: String): Boolean {
        if (!recluseHasExplicitChoice(key) || recordedRecluse[key] == true) return false
        recordedRecluse[key] = true
        return true
    }

    /**
     * Validate a manually specified interpretation against ALL explanations for this exact result.
     * A displayed result alone never modifies registration. Empty witness coverage is not evidence
     * of a specific ruling; it is left to the ability's ordinary manual interaction.
     */
    fun manualChoicesMatchResult(
        spyKey: String?,
        recluseKey: String?,
        option: ClocktowerDisplayOption,
    ): Boolean {
        val witnesses = option.legalRegistrationWitnesses
        if (witnesses.isEmpty()) return true
        val spyChosen = spyHasExplicitChoice(spyKey)
        val recluseChosen = recluseHasExplicitChoice(recluseKey)
        if (!spyChosen && !recluseChosen) return true
        val chosenSpyGood = spyKey?.let(spyRegistersGood::get)
        val chosenRecluseEvil = recluseKey?.let(recluseRegistersEvil::get)
        val chosenSpyRole = spyKey?.let(spyRoles::get)
        val chosenRecluseRole = recluseKey?.let(recluseRoles::get)
        return witnesses.any { witness ->
            (!spyChosen || witness.spyRegistersGood == chosenSpyGood) &&
                (!recluseChosen || witness.recluseRegistersEvil == chosenRecluseEvil) &&
                (!spyChosen || witness.spyRegisteredRoleEnName == null ||
                    witness.spyRegisteredRoleEnName == chosenSpyRole) &&
                (!recluseChosen || witness.recluseRegisteredRoleEnName == null ||
                    witness.recluseRegisteredRoleEnName == chosenRecluseRole)
        }
    }
}
