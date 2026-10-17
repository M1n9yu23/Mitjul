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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gyugle.mitjul.core.designsystem.interaction.InteractionRole
import com.gyugle.mitjul.core.designsystem.interaction.pressScale
import com.gyugle.mitjul.core.designsystem.interaction.rememberHapticClick
import com.gyugle.mitjul.core.designsystem.interaction.rememberPressScale
import com.gyugle.mitjul.core.designsystem.theme.AppTheme

@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, AppTheme.motion)
    val click = rememberHapticClick(InteractionRole.Choice, onClick)
    FilterChip(
        selected = selected,
        onClick = click,
        label = { Text(text) },
        modifier = modifier.pressScale(scale),
        interactionSource = interactionSource,
    )
}

@Preview(name = "칩 선택됨")
@Composable
private fun ChoiceChipSelectedPreview() {
    AppTheme { ChoiceChip(text = "읽는 중", selected = true, onClick = {}) }
}

@Preview(name = "칩 선택 안 됨")
@Composable
private fun ChoiceChipUnselectedPreview() {
    AppTheme { ChoiceChip(text = "읽는 중", selected = false, onClick = {}) }
}

@Preview(name = "칩 긴 텍스트", widthDp = 120)
@Composable
private fun ChoiceChipLongTextPreview() {
    AppTheme { ChoiceChip(text = "언젠가 읽고 싶은 책", selected = true, onClick = {}) }
}

@Preview(name = "칩 어두운 테마")
@Composable
private fun ChoiceChipDarkPreview() {
    AppTheme(darkTheme = true) { ChoiceChip(text = "읽는 중", selected = true, onClick = {}) }
}
