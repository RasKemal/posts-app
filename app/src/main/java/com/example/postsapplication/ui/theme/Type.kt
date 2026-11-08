package com.example.postsapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.postsapplication.R

private val JakartaSans = FontFamily(
    Font(R.font.jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_sans_bold, FontWeight.Bold),
)

private val Default = Typography()

val Typography = Typography(
    displayLarge = Default.displayLarge.jakarta(),
    displayMedium = Default.displayMedium.jakarta(),
    displaySmall = Default.displaySmall.jakarta(),
    headlineLarge = Default.headlineLarge.jakarta(FontWeight.Bold),
    headlineMedium = Default.headlineMedium.jakarta(FontWeight.Bold),
    headlineSmall = Default.headlineSmall.jakarta(FontWeight.SemiBold),
    titleLarge = Default.titleLarge.jakarta(FontWeight.Bold),
    titleMedium = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = Default.titleSmall.jakarta(FontWeight.SemiBold),
    bodyLarge = Default.bodyLarge.jakarta(),
    bodyMedium = Default.bodyMedium.jakarta(),
    bodySmall = Default.bodySmall.jakarta(),
    labelLarge = Default.labelLarge.jakarta(FontWeight.SemiBold),
    labelMedium = Default.labelMedium.jakarta(FontWeight.Medium),
    labelSmall = Default.labelSmall.jakarta(FontWeight.Medium),
)

private fun TextStyle.jakarta(weight: FontWeight? = null): TextStyle =
    copy(fontFamily = JakartaSans, fontWeight = weight ?: fontWeight)
