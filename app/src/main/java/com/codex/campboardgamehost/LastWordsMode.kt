package com.codex.campboardgamehost

internal enum class LastWordsMode {
    None,
    FirstDay,
    FirstTwoDays,
    Always,
}

internal fun LastWordsMode.labelResId(): Int = when (this) {
    LastWordsMode.None -> R.string.last_words_none
    LastWordsMode.FirstDay -> R.string.last_words_first_day
    LastWordsMode.FirstTwoDays -> R.string.last_words_first_two_days
    LastWordsMode.Always -> R.string.last_words_always
}
