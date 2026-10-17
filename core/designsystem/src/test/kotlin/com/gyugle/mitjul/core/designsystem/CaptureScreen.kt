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

package com.gyugle.mitjul.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.gyugle.mitjul.core.designsystem.theme.AppTheme

private const val FROZEN_FRAME_MILLIS = 600L
private const val CAPTURE_TAG = "capture"

enum class Look(val darkTheme: Boolean, val fontScale: Float) {
    Light(darkTheme = false, fontScale = 1f),
    Dark(darkTheme = true, fontScale = 1f),
    LargeFont(darkTheme = false, fontScale = 2f),
}

fun ComposeContentTestRule.captureScreen(name: String, look: Look, content: @Composable () -> Unit) {
    mainClock.autoAdvance = false
    setContent {
        val density = Density(LocalDensity.current.density, look.fontScale)
        CompositionLocalProvider(LocalDensity provides density) {
            AppTheme(darkTheme = look.darkTheme) {
                Surface(Modifier.width(320.dp).testTag(CAPTURE_TAG)) { Box { content() } }
            }
        }
    }
    mainClock.advanceTimeBy(FROZEN_FRAME_MILLIS)
    onNodeWithTag(CAPTURE_TAG).captureRoboImage("src/test/screenshots/${name}_${look.name}.png")
}
