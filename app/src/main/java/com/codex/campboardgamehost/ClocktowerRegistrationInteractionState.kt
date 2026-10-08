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

    /** Manual toggles retain the last role; only a final option can clear its witness. */
    fun chooseSpy(key: String, good: Boolean, defaultRole: String? = null) {
        spyRegistersGood[key] = good
        if (good && defaultRole != null && spyRoles[key] == null) spyRoles[key] = defaultRole
    }

    fun chooseSpyRole(key: String, role: String) {
        spyRoles[key] = role
    }

    fun spyWillRecord(key: String?): Boolean = key != null && recordedSpy[key] != true

    fun markSpyRecorded(key: String): Boolean {
        if (recordedSpy[key] == true) return false
        recordedSpy[key] = true
        return true
    }

    fun recluseIsEvil(key: String?): Boolean = key != null && recluseRegistersEvil[key] == true

    fun recluseRole(key: String?): String? = key?.let(recluseRoles::get)

    fun chooseRecluse(key: String, evil: Boolean, defaultRole: String? = null) {
        recluseRegistersEvil[key] = evil
        if (evil && defaultRole != null && recluseRoles[key] == null) recluseRoles[key] = defaultRole
    }

    fun chooseRecluseRole(key: String, role: String) {
        recluseRoles[key] = role
    }

    fun markRecluseRecorded(key: String): Boolean {
        if (recordedRecluse[key] == true) return false
        recordedRecluse[key] = true
        return true
    }

    /** Witness mutation precedes the caller's registration-recording effects. */
    fun applyRecommendedWitness(spyKey: String?, recluseKey: String?, option: ClocktowerDisplayOption) {
        if (spyKey != null) {
            option.spyRegistersGood?.let { good ->
                spyRegistersGood[spyKey] = good
                if (good) {
                    option.spyRegisteredRoleEnName?.let { spyRoles[spyKey] = it }
                } else {
                    spyRoles.remove(spyKey)
                }
            }
        }
        if (recluseKey != null) {
            option.recluseRegistersEvil?.let { evil ->
                recluseRegistersEvil[recluseKey] = evil
                if (evil) {
                    option.recluseRegisteredRoleEnName?.let { recluseRoles[recluseKey] = it }
                } else {
                    recluseRoles.remove(recluseKey)
                }
            }
        }
    }
}
