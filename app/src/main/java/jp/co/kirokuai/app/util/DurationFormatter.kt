package jp.co.kirokuai.app.util

import java.util.Locale

fun formatDuration(durationSeconds: Long): String {
    val minutes = durationSeconds / SECONDS_PER_MINUTE
    val seconds = durationSeconds % SECONDS_PER_MINUTE
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

private const val SECONDS_PER_MINUTE = 60L
