package dev.jvmname.sprakbund.ui.password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.jvmname.sprakbund.ui.theme.PasswordStyle
import dev.jvmname.sprakbund.ui.theme.SprakTheme
import dev.jvmname.sprakbund.ui.theme.SprakTypography
import dev.zacsweers.metro.AppScope

@[Composable CircuitInject(PasswordGeneratorScreen::class, AppScope::class)]
fun PasswordGeneratorUi(state: PasswordGeneratorState, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier.fillMaxWidth().displayCutoutPadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { state.eventSink(PasswordGeneratorEvent.Refresh) }) {
                Icon(Icons.Filled.Refresh, "Refresh")
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
    ) { padding ->

        Column(
            Modifier.padding(padding)
                .fillMaxSize()
                .padding(vertical = 32.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Password Generator", style = SprakTypography.headlineMedium)
            Spacer(Modifier.height(32.dp))
            PasswordList(state.passwords, onCopyClick = {
                state.eventSink(PasswordGeneratorEvent.CopyPassword(it))
            })
            Spacer(Modifier.height(32.dp))
            PasswordSlider(
                passwordLength = state.passwordLength,
                onLengthChange = { state.eventSink(PasswordGeneratorEvent.ChangeLength(it)) }
            )
            CompoundWordsSlider(numCompoundWords = state.compoundWord, onNumChange = {
                state.eventSink(PasswordGeneratorEvent.ChangeCompoundWord(it))
            })
        }
    }

    if (state.snackbarMessage != null) {
        LaunchedEffect(state.snackbarMessage) {
            snackbarHostState.showSnackbar(state.snackbarMessage)
            state.eventSink(PasswordGeneratorEvent.SnackbarShown)
        }
    }
}

@Composable
fun CompoundWordsSlider(
    modifier: Modifier = Modifier,
    numCompoundWords: Int,
    onNumChange: (Int) -> Unit,
) {
    Column {
        Text("Number of words in passphrase: ")
        //TODO: top of range configurable
        val range = numCompoundWords.toFloat()..5f
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(range.start.toInt().toString(), modifier = Modifier.weight(1f))
            var sliderPosition by remember { mutableIntStateOf(numCompoundWords) }
            val sliderState = rememberSliderState(
                value = numCompoundWords.toFloat(),
                steps = (range.endInclusive - range.start).toInt(),
                onValueChangeFinished = { onNumChange(sliderPosition) },
                valueRange = range,
            )
            sliderState.onValueChange = { sliderPosition = it.toInt() }
            Slider(state = sliderState, modifier = Modifier.weight(8f))
            Text(range.endInclusive.toInt().toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PasswordSlider(
    modifier: Modifier = Modifier,
    passwordLength: Int,
    onLengthChange: (Int) -> Unit,
) {
    Column {
        Text("Password Length: ")
        //TODO: top of range configurable
        val range = passwordLength.toFloat()..25f
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(range.start.toInt().toString(), modifier = Modifier.weight(1f))
            var sliderPosition by remember { mutableIntStateOf(passwordLength) }
            val sliderState = rememberSliderState(
                value = passwordLength.toFloat(),
                steps = (range.endInclusive - range.start).toInt(),
                onValueChangeFinished = { onLengthChange(sliderPosition) },
                valueRange = range,
            )
            sliderState.onValueChange = { sliderPosition = it.toInt() }
            Slider(state = sliderState, modifier = Modifier.weight(8f))
            Text(range.endInclusive.toInt().toString(), modifier = Modifier.weight(1f))

        }
    }
}

@Composable
fun PasswordList(
    passwords: List<String>,
    modifier: Modifier = Modifier,
    onCopyClick: (password: String) -> Unit,
) {
    ElevatedCard {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(passwords, key = { it }) {
                PasswordText(it, onCopyClick = onCopyClick)
            }
        }
    }

}


@Composable
fun PasswordText(
    text: String,
    modifier: Modifier = Modifier,
    onCopyClick: (password: String) -> Unit,
) {
    var selected by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = modifier
                .align(Alignment.Center)
                .clickable { selected = !selected },
            text = text,
            style = PasswordStyle,
        )

        AnimatedVisibility(selected) {
            Icon(
                Icons.Outlined.ContentCopy, "Copy",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .clickable { onCopyClick(text) },
            )
        }
    }
}

@Preview
@Composable
fun PasswordGeneratorUiPreview() {
    SprakTheme {
        val state = PasswordGeneratorState(
            snackbarMessage = "test",
            passwords = listOf(
                "LoremIpsum",
                "DolorSitAmet",
                "Consectetur",
                "AdipiscingElit",
                "IntegerSodales",
                "LaoreetCommodo"
            ),
            passwordLength = 12,
            compoundWord = 1,
            eventSink = {},
        )
        PasswordGeneratorUi(state)
    }
}