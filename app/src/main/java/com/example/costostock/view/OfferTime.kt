package com.example.costostock.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun rememberNowMillis(): State<Long> = produceState(initialValue = System.currentTimeMillis()) {
    while (true) {
        delay(1000L)
        value = System.currentTimeMillis()
    }
}
fun formatRemaining(remainingMillis: Long): String {
    val total = (remainingMillis / 1000L).coerceAtLeast(0L)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
