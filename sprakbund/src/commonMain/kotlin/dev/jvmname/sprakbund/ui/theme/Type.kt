package dev.jvmname.sprakbund.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val baseline = Typography()

val SprakTypography = Typography(
    displayLarge = baseline.displayLarge,
    displayMedium = baseline.displayMedium,
    displaySmall = baseline.displaySmall,
    headlineLarge = baseline.headlineLarge,
    headlineMedium = baseline.headlineMedium,
    headlineSmall = baseline.headlineSmall,
    titleLarge = baseline.titleLarge,
    titleMedium = baseline.titleMedium,
    titleSmall = baseline.titleSmall,
    bodyLarge = baseline.bodyLarge,
    bodyMedium = baseline.bodyMedium,
    bodySmall = baseline.bodySmall,
    labelLarge = baseline.labelLarge,
    labelMedium = baseline.labelMedium,
    labelSmall = baseline.labelSmall
)

val PasswordStyle = baseline.headlineSmall.copy(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "c2sc, smcp"
)