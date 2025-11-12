package dev.jvmname.sprakbund.domain

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun clipEntryOf(string: String): ClipEntry {
    return ClipData.newPlainText("Pronounceable Password", string).toClipEntry()
}