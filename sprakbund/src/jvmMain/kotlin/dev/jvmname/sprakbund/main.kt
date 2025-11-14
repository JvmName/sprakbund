package dev.jvmname.sprakbund

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.jvmname.sprakbund.di.SprakGraph
import dev.zacsweers.metro.createGraph

fun main() = application {
    val windowState =
        rememberWindowState(
            width = 1200.dp,
            height = 800.dp,
            position = WindowPosition(Alignment.Center),
        )
    val graph = createGraph<SprakGraph>()
    Window(
        title = "sprakbund",
        onCloseRequest = ::exitApplication,
        state = windowState,
    ) {
        App(graph.circuit)
    }
}