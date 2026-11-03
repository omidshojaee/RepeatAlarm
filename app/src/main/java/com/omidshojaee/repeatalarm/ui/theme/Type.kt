package com.omidshojaee.repeatalarm.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.omidshojaee.repeatalarm.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_light, FontWeight.Light),
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = VazirmatnFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = VazirmatnFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = VazirmatnFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = VazirmatnFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = VazirmatnFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = VazirmatnFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = VazirmatnFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = VazirmatnFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = VazirmatnFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = VazirmatnFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = VazirmatnFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = VazirmatnFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = VazirmatnFontFamily)
)
