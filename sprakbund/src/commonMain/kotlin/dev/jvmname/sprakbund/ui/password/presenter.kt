package dev.jvmname.sprakbund.ui.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalClipboard
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dev.jvmname.sprakbund.domain.clipEntryOf
import dev.jvmname.sprakbund.domain.pronounce.PronounceableGenerator
import dev.jvmname.sprakbund.ui.util.rememberRetainedCoroutineScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch

@AssistedInject
class PasswordGeneratorPresenter(
    @Assisted private val screen: PasswordGeneratorScreen,
    private val generator: PronounceableGenerator,
) : Presenter<PasswordGeneratorState> {

    @[AssistedFactory CircuitInject(PasswordGeneratorScreen::class, AppScope::class)]
    fun interface Factory {
        fun create(screen: PasswordGeneratorScreen): PasswordGeneratorPresenter
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun present(): PasswordGeneratorState {
        val passwordCount = screen.count

        val presenterScope = rememberRetainedCoroutineScope()
        var passwordLength by remember(screen) { mutableIntStateOf(screen.length) }
        var numWords by remember(screen) { mutableIntStateOf(screen.compoundWord) }
        var snackbarMessage: String? by remember { mutableStateOf(null) }

        val clipboard = LocalClipboard.current //todo this might make testing difficult

        var passwords: List<String> by remember(passwordCount, passwordLength, numWords) {
            mutableStateOf(buildPasswordList(passwordCount, passwordLength, numWords))
        }

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
                        passwords = buildPasswordList(passwordCount, passwordLength, numWords)
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