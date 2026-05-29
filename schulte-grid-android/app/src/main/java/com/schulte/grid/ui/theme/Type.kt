package com.schulte.grid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // 标题
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.5).sp,
    ),
    // 计时数字
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        fontFamily = FontFamily.Monospace,
    ),
    // 网格数字
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    // 标签
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = androidx.compose.ui.graphics.Color.Gray,
    ),
)
