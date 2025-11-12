package dev.jvmname.sprakbund.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
actual fun SprakTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit),
) {
    val colorScheme =
        when {
            darkTheme -> darkScheme
            else -> lightScheme
        }

    // No custom fonts available in Desktop
    MaterialTheme(colorScheme = colorScheme, content = content)
}