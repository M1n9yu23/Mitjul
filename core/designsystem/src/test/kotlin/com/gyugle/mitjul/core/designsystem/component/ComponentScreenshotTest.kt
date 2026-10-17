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

package com.gyugle.mitjul.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.gyugle.mitjul.core.designsystem.Look
import com.gyugle.mitjul.core.designsystem.captureScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ComponentScreenshotTest(private val look: Look) {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun actionButtons() = composeRule.captureScreen("action_buttons", look) {
        Column {
            PrimaryActionButton(text = "저장", onClick = {})
            PrimaryActionButton(text = "저장", onClick = {}, enabled = false)
            DestructiveActionButton(text = "이 책의 문장까지 지우기", onClick = {})
            DestructiveActionButton(text = "삭제", onClick = {}, enabled = false)
            SecondaryActionButton(text = "이미 계정이 있어서 로그인하기", onClick = {})
        }
    }

    @Test
    fun choiceChips() = composeRule.captureScreen("choice_chips", look) {
        Column {
            ChoiceChip(text = "읽는 중", selected = true, onClick = {})
            ChoiceChip(text = "언젠가 읽고 싶은 책", selected = false, onClick = {})
        }
    }

    @Test
    fun pressableSurface() = composeRule.captureScreen("pressable_surface", look) {
        PressableSurface(onClick = {}, clickLabel = "열기") { ListItem(headlineContent = { Text("코스모스") }) }
    }

    @Test
    fun coverImage() = composeRule.captureScreen("cover_image", look) {
        CoverImage(model = null, contentDescription = null, modifier = Modifier.width(CoverImageDefaults.ThumbnailWidth))
    }

    @Test
    fun loadingState() = composeRule.captureScreen("loading_state", look) {
        LoadingState(label = "서재를 불러오는 중")
    }

    @Test
    fun emptyState() = composeRule.captureScreen("empty_state", look) {
        EmptyState("아직 옮겨 적은 문장이 없다", "책을 펴고 마음에 남은 문장을 한 줄 적어 보자", "문장 적기", {})
    }

    @Test
    fun errorState() = composeRule.captureScreen("error_state", look) {
        ErrorState("문장을 불러오지 못했다", "인터넷 연결을 확인하고 다시 시도해 보자", "다시 시도", {})
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun looks() = Look.entries.map { arrayOf(it) }
    }
}
