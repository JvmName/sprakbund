package dev.jvmname.sprakbund.ui.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dev.jvmname.sprakbund.domain.HibpChecker
import dev.jvmname.sprakbund.domain.clipEntryOf
import dev.jvmname.sprakbund.domain.pronounce.PronounceableGenerator
import dev.jvmname.sprakbund.ui.password.PasswordGeneratorState.PasswordItem
import dev.jvmname.sprakbund.ui.util.rememberRetainedCoroutineScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class PasswordGeneratorPresenter(
    @Assisted private val screen: PasswordGeneratorScreen,
    private val generator: PronounceableGenerator,
    private val checker: HibpChecker,
) : Presenter<PasswordGeneratorState> {

    @[AssistedFactory CircuitInject(PasswordGeneratorScreen::class, AppScope::class)]
    fun interface Factory {
        fun create(screen: PasswordGeneratorScreen): PasswordGeneratorPresenter
    }

    private val _passwords = MutableStateFlow(emptyList<String>())

    @Composable
    override fun present(): PasswordGeneratorState {
        val passwordCount = screen.count

        val presenterScope = rememberRetainedCoroutineScope()
        var passwordLength by remember(screen) { mutableIntStateOf(screen.length) }
        var numWords by remember(screen) { mutableIntStateOf(screen.compoundWord) }
        var snackbarMessage: String? by remember { mutableStateOf(null) }
        var isRefreshing by remember { mutableStateOf(false) }
        if (isRefreshing) {
            LaunchedEffect(Unit) {
                _passwords.update { buildPasswordList(passwordCount, passwordLength, numWords) }
                isRefreshing = false
            }
        }

        val clipboard = LocalClipboard.current //todo this might make testing difficult

        val passwords by remember(passwordCount, passwordLength, numWords) {
            _passwords.update { buildPasswordList(passwordCount, passwordLength, numWords) }
            _passwords.mapLatest { list ->
                val map = checker.checkPasswords(list)
                list.map { PasswordItem(password = it, pwned = map[it] ?: false) }
            }
        }
            .collectAsState(emptyList())

        return PasswordGeneratorState(
            passwords = passwords,
            passwordLength = passwordLength,
            compoundWord = numWords,
            snackbarMessage = snackbarMessage,
            eventSink = { event ->
                when (event) {
                    is PasswordGeneratorEvent.ChangeCompoundWord -> numWords = event.numWords
                    is PasswordGeneratorEvent.ChangeLength -> passwordLength = event.length
                    is PasswordGeneratorEvent.CopyPassword -> {
                        presenterScope.launch {
                            clipboard.setClipEntry(clipEntryOf(event.password))
                            snackbarMessage = "Copied to clipboard"
                        }
                    }

                    PasswordGeneratorEvent.Refresh -> {
                        isRefreshing = true
                    }

                    PasswordGeneratorEvent.SnackbarShown -> snackbarMessage = null
                }
            }
        )
    }

    private fun buildPasswordList(
        passwordCount: Int,
        passwordLength: Int,
        numWords: Int
    ): List<String> = buildList(passwordCount) {
        repeat(passwordCount) {
            add(createPassword(passwordLength, numWords))
        }
    }

    private fun createPassword(length: Int, numWords: Int): String {
        return when (numWords) {
            1 -> generator.generate(length)
            else -> (0..<numWords).joinToString(separator = "-") {
                generator.generate(length)
            }
        }.lowercase()
    }

}