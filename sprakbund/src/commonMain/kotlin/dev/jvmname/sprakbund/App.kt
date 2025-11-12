package dev.jvmname.sprakbund

import androidx.compose.runtime.Composable
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import dev.jvmname.sprakbund.ui.password.PasswordGeneratorScreen
import dev.jvmname.sprakbund.ui.theme.SprakTheme

@Composable
fun App(circuit: Circuit) {
    SprakTheme {
        CircuitCompositionLocals(circuit) {
            ContentWithOverlays {
                CircuitContent(PasswordGeneratorScreen())
            }
        }
    }
}