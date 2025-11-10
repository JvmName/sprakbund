package dev.jvmname.sprakbund

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import dev.jvmname.sprakbund.ui.PasswordGeneratorScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(circuit: Circuit) {
    CircuitCompositionLocals(circuit) {
        MaterialTheme {
            CircuitContent(PasswordGeneratorScreen())
        }
    }
}