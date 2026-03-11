package dev.jvmname.sprakbund.ui.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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

        val validator = remember(passwordLength, numWords) {
            validator(passwordLength, numWords)
        }

        var isRefreshing by remember { mutableStateOf(false) }
        if (isRefreshing) {
            LaunchedEffect(Unit) {
                _passwords.update { buildPasswordList(passwordCount, validator.passwordLength, validator.compoundWord) }
                isRefreshing = false
            }
        }


        val clipboard = LocalClipboard.current //todo this might make testing difficult

        val passwords by remember(passwordCount, validator.passwordLength, validator.compoundWord) {
            _passwords.update { buildPasswordList(passwordCount, validator.passwordLength, validator.compoundWord) }
            _passwords.mapLatest { list ->
                val map = checker.checkPasswords(list)
                list.map { PasswordItem(password = it, pwned = map[it] ?: false) }
            }
        }.collectAsState(emptyList())

        return PasswordGeneratorState(
            passwords = passwords,
            passwordLength = validator.passwordLength,
            passwordLengthRange = validator.passwordLengthRange,
            compoundWord = validator.compoundWord,
            compoundWordRange = validator.compoundWordRange,
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

                    PasswordGeneratorEvent.Refresh -> isRefreshing = true
                    PasswordGeneratorEvent.SnackbarShown -> snackbarMessage = null
                }
            },

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

    private fun validator(passwordLength: Int, compoundWord: Int) = GeneratorValidator(passwordLength, compoundWord)

    @Immutable
    private class GeneratorValidator(
        passwordLength: Int,
        compoundWord: Int
    ) {
        val passwordLengthRange: IntRange = when (compoundWord) {
            1 -> DEFAULT_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
            else -> {
                val start = DEFAULT_PASSWORD_LENGTH - (compoundWord * 2 - 1)
                start.coerceAtLeast(MIN_PASSWORD_LENGTH)..MAX_PASSWORD_LENGTH
            }
        }
        val passwordLength = passwordLength.coerceAtLeast(passwordLengthRange.first)
        val compoundWordRange: IntRange = MIN_NUM_WORDS..MAX_NUM_WORDS
        val compoundWord = compoundWord.coerceAtLeast(compoundWordRange.first)
    }

    companion object {
        const val DEFAULT_PASSWORD_LENGTH = 12
        const val MIN_PASSWORD_LENGTH = 7
        const val MAX_PASSWORD_LENGTH = 25
        const val DEFAULT_NUM_WORDS = 1
        const val MIN_NUM_WORDS = 1
        const val MAX_NUM_WORDS = 5
    }

}