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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val itemGap: Dp = 8.dp,
    val screenMargin: Dp = 20.dp,
)

@Immutable
data class Motion(
    val pressIn: Int = 90,
    val pressOut: Int = 160,
    val spinnerDelay: Int = 300,
    val fadeIn: Int = 200,
    val valueFill: Int = 800,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalMotion = staticCompositionLocalOf { Motion() }
