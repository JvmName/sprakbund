package dev.jvmname.sprakbund

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.jvmname.sprakbund.di.SprakGraph
import dev.zacsweers.metro.createGraph

fun main() = application {

    val graph = createGraph<SprakGraph>()
    Window(
        onCloseRequest = ::exitApplication,
        title = "sprakbund",
    ) {
        App(graph.circuit)
    }
}