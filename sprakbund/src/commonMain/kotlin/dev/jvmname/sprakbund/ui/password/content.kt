package dev.jvmname.sprakbund.ui.password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.jvmname.sprakbund.ui.common.IntSliderWithTextField
import dev.jvmname.sprakbund.ui.password.PasswordGeneratorState.PasswordItem
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
            IntSliderWithTextField(
                title = "Word Length:",
                initialValue = state.passwordLength,
                valueRange = state.passwordLengthRange,
                onValueChange = { state.eventSink(PasswordGeneratorEvent.ChangeLength(it)) }
            )
            IntSliderWithTextField(
                title = "Words in phrase:",
                initialValue = state.compoundWord,
                valueRange = state.compoundWordRange,
                onValueChange = { state.eventSink(PasswordGeneratorEvent.ChangeCompoundWord(it)) })
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
fun PasswordList(
    passwords: List<PasswordItem>,
    modifier: Modifier = Modifier,
    onCopyClick: (password: String) -> Unit,
) {
    ElevatedCard {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            userScrollEnabled = false,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(passwords, key = { it.password }) {
                PasswordText(it.password, pwned = it.pwned, onCopyClick = onCopyClick)
            }
        }
    }

}

@Composable
fun PasswordText(
    text: String,
    pwned: Boolean,
    modifier: Modifier = Modifier,
    onCopyClick: (password: String) -> Unit,
) {
    var selected by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = modifier
                .clickable { selected = !selected },
            text = text,
            autoSize = TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = PasswordStyle.fontSize * 1.2),
            style = PasswordStyle,
        )

        if (pwned) {
            Icon(
                Icons.Default.Clear,
                "",
                tint = MaterialTheme.colorScheme.error
            )
        }

        AnimatedVisibility(
            selected,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Icon(
                Icons.Outlined.ContentCopy, "Copy",
                modifier = Modifier.clickable { onCopyClick(text) },
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
                PasswordItem("LoremIpsum", false),
                PasswordItem("DolorSitAmet", false),
                PasswordItem("Consectetur", true),
                PasswordItem("AdipiscingElit", false),
                PasswordItem("IntegerSodales", false),
                PasswordItem("LaoreetCommodo", false)
            ),
            passwordLength = 12,
            compoundWord = 1,
            eventSink = {},
        )
        PasswordGeneratorUi(state)
    }
}