package com.codex.campboardgamehost

import androidx.compose.runtime.mutableStateMapOf

/**
 * Only manual toggles authorize a possible explicit registration ruling. This is a UI choice,
 * NOT yet a committed canonical fact: the Host must separately check the ability interaction,
 * legal registration and result, and freeze a causal prefix before publishing a typed ruling.
 *
 * Null from [ClocktowerRegistrationInteractionState.explicitChoice] means no manual choice was
 * made. It must NOT be persisted as a known UNRESOLVED_NOT_REQUIRED result: that status needs
 * positive confirmation of a displayed result. Older saves are UNAVAILABLE_OR_UNRECORDED.
 */
internal enum class ClocktowerRegistrationSubject { SPY, RECLUSE }

internal data class ClocktowerManualRegistrationChoice(
    val subject: ClocktowerRegistrationSubject,
    val interactionKey: String,
    val usesSpecialRegistration: Boolean,
    val selectedRegisteredRoleEnName: String?,
) {
    init {
        require(interactionKey.isNotBlank())
        require(selectedRegisteredRoleEnName == null || usesSpecialRegistration) {
            "An inactive registration cannot carry a stale role choice."
        }
    }
}

/** UI-local Spy/Recluse selections. */
internal class ClocktowerRegistrationInteractionState {
    private val spyRegistersGood = mutableStateMapOf<String, Boolean>()
    private val spyRoles = mutableStateMapOf<String, String>()
    private val recluseRegistersEvil = mutableStateMapOf<String, Boolean>()
    private val recluseRoles = mutableStateMapOf<String, String>()

    /**
     * Explicit-only, interaction-local snapshot for later semantic Host validation. In
     * particular, false is a deliberate manual choice; it is different from an untouched map.
     * Role-only edits never count as a choice, and old role values are discarded when inactive.
     */
    fun explicitChoice(
        subject: ClocktowerRegistrationSubject,
        key: String?,
    ): ClocktowerManualRegistrationChoice? {
        if (key == null) return null
        val choices = when (subject) {
            ClocktowerRegistrationSubject.SPY -> spyRegistersGood
            ClocktowerRegistrationSubject.RECLUSE -> recluseRegistersEvil
        }
        val special = choices[key] ?: return null
        val role = if (special) {
            when (subject) {
                ClocktowerRegistrationSubject.SPY -> spyRoles[key]
                ClocktowerRegistrationSubject.RECLUSE -> recluseRoles[key]
            }?.takeIf(String::isNotBlank)
        } else {
            null
        }
        return ClocktowerManualRegistrationChoice(subject, key, special, role)
    }

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


}