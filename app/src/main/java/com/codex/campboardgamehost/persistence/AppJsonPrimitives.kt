package com.codex.campboardgamehost

import org.json.JSONArray
import org.json.JSONObject

internal inline fun <reified T : Enum<T>> enumByName(name: String?): T? {
    if (name.isNullOrBlank()) return null
    return runCatching { enumValueOf<T>(name) }.getOrNull()
}

internal fun JSONObject.putNullableString(key: String, value: String?) {
    put(key, value ?: JSONObject.NULL)
}

internal fun JSONObject.putNullableInt(key: String, value: Int?) {
    put(key, value ?: JSONObject.NULL)
}

internal fun JSONObject.putNullableBoolean(key: String, value: Boolean?) {
    put(key, value ?: JSONObject.NULL)
}

internal fun JSONObject.optNullableString(key: String): String? {
    return if (has(key) && !isNull(key)) optString(key) else null
}

internal fun JSONObject.optNullableInt(key: String): Int? {
    return if (has(key) && !isNull(key)) optInt(key) else null
}

internal fun JSONObject.optNullableBoolean(key: String): Boolean? {
    return if (has(key) && !isNull(key)) optBoolean(key) else null
}

internal fun stringsToJsonArray(values: List<String>): JSONArray {
    val json = JSONArray()
    values.forEach { json.put(it) }
    return json
}

internal fun JSONArray.toStringList(): List<String> = buildList {
    for (index in 0 until length()) {
        optString(index).takeIf { it.isNotBlank() }?.let(::add)
    }
}
