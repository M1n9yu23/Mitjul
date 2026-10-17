/*
 * Copyright 2026 MinGyu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gyugle.mitjul.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import com.gyugle.mitjul.core.designsystem.R

internal val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

internal val Myeongjo = FontFamily(
    Font(R.font.nanum_myeongjo_regular, FontWeight.Normal),
)

private val Base = Typography()

private val Korean = TextStyle(
    lineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase),
    localeList = LocaleList("ko"),
)

internal val AppTypography = Typography(
    displayLarge = Base.displayLarge.merge(Korean).copy(fontFamily = Myeongjo, lineHeight = 76.sp),
    displayMedium = Base.displayMedium.merge(Korean).copy(fontFamily = Myeongjo, lineHeight = 60.sp),
    displaySmall = Base.displaySmall.merge(Korean).copy(fontFamily = Myeongjo, lineHeight = 50.sp),
    headlineLarge = Base.headlineLarge.merge(Korean).copy(fontFamily = Pretendard),
    headlineMedium = Base.headlineMedium.merge(Korean).copy(fontFamily = Pretendard),
    headlineSmall = Base.headlineSmall.merge(Korean).copy(fontFamily = Pretendard),
    titleLarge = Base.titleLarge.merge(Korean).copy(fontFamily = Pretendard),
    titleMedium = Base.titleMedium.merge(Korean).copy(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.merge(Korean).copy(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.merge(Korean).copy(fontFamily = Pretendard, lineHeight = 26.sp),
    bodyMedium = Base.bodyMedium.merge(Korean).copy(fontFamily = Pretendard, lineHeight = 22.sp),
    bodySmall = Base.bodySmall.merge(Korean).copy(fontFamily = Pretendard),
    labelLarge = Base.labelLarge.merge(Korean).copy(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold),
    labelMedium = Base.labelMedium.merge(Korean).copy(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold),
    labelSmall = Base.labelSmall.merge(Korean).copy(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold),
)
