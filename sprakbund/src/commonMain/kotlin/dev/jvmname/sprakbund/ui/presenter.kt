package dev.jvmname.sprakbund.ui

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class PasswordGeneratorPresenter(
    @Assisted private val screen: PasswordGeneratorScreen,
) : Presenter<PasswordGeneratorState> {

    @[AssistedFactory CircuitInject(PasswordGeneratorScreen::class, AppScope::class)]
    fun interface Factory {
        fun create(screen: PasswordGeneratorScreen): PasswordGeneratorPresenter
    }

    @Composable
    override fun present(): PasswordGeneratorState {
        TODO("Not yet implemented")
    }

}