package dev.jvmname.sprakbund.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.jvmname.sprakbund.ui.theme.SprakTypography


@Composable
fun IntSliderWithTextField(
    modifier: Modifier = Modifier,
    initialValue: Int,
    title: String,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
) {
    Column(modifier) {
        Text(title, style = SprakTypography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var sliderValue by remember { mutableIntStateOf(initialValue) }
            val textFieldState = rememberTextFieldState(sliderValue.toString())

            Slider(
                modifier = Modifier.weight(8f),
                value = sliderValue.toFloat(),
                onValueChange = {
                    sliderValue = it.toInt()
                    textFieldState.setTextAndPlaceCursorAtEnd(it.toInt().toString())
                    onValueChange(sliderValue)
                },
                steps = valueRange.last - valueRange.first,
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat()
            )
            OutlinedTextField(
                modifier = Modifier.weight(1.5f, fill = false),
                state = textFieldState,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = {
                    InputTransformation.maxLength(2)
                        .then { if (asCharSequence().any { !it.isDigit() }) revertAllChanges() }
                        .then {
                            val inRange = toString()
                                .toIntOrNull()
                                ?.let { it in valueRange }
                                ?: false
                            if (!inRange) revertAllChanges()
                        }
                }
            )

            textFieldState.text
                .toString()
                .toIntOrNull()
                ?.let {
                    sliderValue = it
                    onValueChange(it)
                }
        }

    }

}