package dev.jvmname.sprakbund.ui

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.jvmname.sprakbund.parcel.CommonParcelize

@CommonParcelize
data class PasswordGeneratorScreen(
    val count: Int = 7,
    val length: Int = 12,
) : Screen

data class PasswordGeneratorState(
    val passwords: List<String>,
    val eventSink: (PasswordGeneratorEvent) -> Unit,
) : CircuitUiState

sealed class PasswordGeneratorEvent : CircuitUiEvent {

}