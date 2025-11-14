package dev.jvmname.sprakbund.ui.password

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.jvmname.sprakbund.parcel.CommonParcelize

@CommonParcelize
data class PasswordGeneratorScreen(
    val count: Int = 7,
    val length: Int = 12,
    val compoundWord: Int = 1
) : Screen

@Immutable
data class PasswordGeneratorState(
    val passwords: List<String>,
    val passwordLength: Int,
    val passwordLengthRange: IntRange = 12..25,
    val compoundWord: Int,
    val compoundWordRange: IntRange = 1..5,
    val snackbarMessage: String?,
    val eventSink: (PasswordGeneratorEvent) -> Unit,
) : CircuitUiState

sealed interface PasswordGeneratorEvent : CircuitUiEvent {
    data object Refresh : PasswordGeneratorEvent
    data class ChangeLength(val length: Int) : PasswordGeneratorEvent
    data class ChangeCompoundWord(val numWords: Int) : PasswordGeneratorEvent
    data class CopyPassword(val password: String) : PasswordGeneratorEvent
    data object SnackbarShown : PasswordGeneratorEvent
}