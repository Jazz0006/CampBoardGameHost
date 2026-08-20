package com.codex.campboardgamehost.clocktower.catalog

internal enum class ClocktowerScriptValidationCode {
    INVALID_JSON,
    INVALID_SCRIPT_SIZE,
    INVALID_ENTRY,
    DUPLICATE_META,
    INVALID_FIELD,
    INVALID_CHARACTER_ID,
    UNKNOWN_CHARACTER_ID,
    DUPLICATE_CHARACTER_ID,
    CUSTOM_ID_COLLISION,
    UNKNOWN_CHARACTER_TEAM,
    DUPLICATE_NIGHT_TOKEN,
    INVALID_NIGHT_TOKEN,
    INVALID_NIGHT_SYSTEM_TOKEN,
    INVALID_JINX_TARGET,
}

internal class ClocktowerScriptValidationException(
    val code: ClocktowerScriptValidationCode,
    val path: String? = null,
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

internal fun clocktowerValidationFailure(
    code: ClocktowerScriptValidationCode,
    message: String,
    path: String? = null,
    cause: Throwable? = null,
): Nothing = throw ClocktowerScriptValidationException(code, path, message, cause)

internal inline fun clocktowerValidate(
    condition: Boolean,
    code: ClocktowerScriptValidationCode,
    message: () -> String,
    path: String? = null,
) {
    if (!condition) clocktowerValidationFailure(code, message(), path)
}
