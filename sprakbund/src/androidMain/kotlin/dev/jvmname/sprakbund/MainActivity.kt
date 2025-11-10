package dev.jvmname.sprakbund

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jvmname.sprakbund.di.SprakGraph
import dev.zacsweers.metro.createGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val graph = createGraph<SprakGraph>()

        setContent {
            App(graph.circuit)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
//    App()
}