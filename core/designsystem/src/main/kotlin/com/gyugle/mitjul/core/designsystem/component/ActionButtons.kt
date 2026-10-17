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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gyugle.mitjul.core.designsystem.interaction.InteractionRole
import com.gyugle.mitjul.core.designsystem.interaction.pressScale
import com.gyugle.mitjul.core.designsystem.interaction.rememberHapticClick
import com.gyugle.mitjul.core.designsystem.interaction.rememberPressScale
import com.gyugle.mitjul.core.designsystem.theme.AppTheme

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, AppTheme.motion)
    val click = rememberHapticClick(InteractionRole.PrimaryAction, onClick)
    Button(
        onClick = click,
        modifier = modifier.pressScale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        Text(text)
    }
}

@Preview(name = "저장 기본")
@Composable
private fun PrimaryActionButtonPreview() {
    AppTheme { PrimaryActionButton(text = "저장", onClick = {}) }
}

@Preview(name = "저장 비활성")
@Composable
private fun PrimaryActionButtonDisabledPreview() {
    AppTheme { PrimaryActionButton(text = "저장", onClick = {}, enabled = false) }
}

@Preview(name = "저장 긴 텍스트", widthDp = 200)
@Composable
private fun PrimaryActionButtonLongTextPreview() {
    AppTheme { PrimaryActionButton(text = "읽은 쪽수까지 함께 저장하기", onClick = {}) }
}

@Preview(name = "저장 어두운 테마")
@Composable
private fun PrimaryActionButtonDarkPreview() {
    AppTheme(darkTheme = true) { PrimaryActionButton(text = "저장", onClick = {}) }
}

@Composable
fun DestructiveActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, AppTheme.motion)
    val click = rememberHapticClick(InteractionRole.DestructiveAction, onClick)
    OutlinedButton(
        onClick = click,
        modifier = modifier.pressScale(scale),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = if (enabled) 1f else 0.12f),
        ),
        interactionSource = interactionSource,
    ) {
        Text(text)
    }
}

@Preview(name = "삭제 기본")
@Composable
private fun DestructiveActionButtonPreview() {
    AppTheme { DestructiveActionButton(text = "삭제", onClick = {}) }
}

@Preview(name = "삭제 비활성")
@Composable
private fun DestructiveActionButtonDisabledPreview() {
    AppTheme { DestructiveActionButton(text = "삭제", onClick = {}, enabled = false) }
}

@Preview(name = "삭제 긴 텍스트", widthDp = 200)
@Composable
private fun DestructiveActionButtonLongTextPreview() {
    AppTheme { DestructiveActionButton(text = "이 책의 문장까지 지우기", onClick = {}) }
}

@Preview(name = "삭제 어두운 테마")
@Composable
private fun DestructiveActionButtonDarkPreview() {
    AppTheme(darkTheme = true) { DestructiveActionButton(text = "삭제", onClick = {}) }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, AppTheme.motion)
    val click = rememberHapticClick(InteractionRole.SecondaryAction, onClick)
    TextButton(
        onClick = click,
        modifier = modifier.pressScale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        Text(text)
    }
}

@Preview(name = "보조 기본")
@Composable
private fun SecondaryActionButtonPreview() {
    AppTheme { SecondaryActionButton(text = "나중에", onClick = {}) }
}

@Preview(name = "보조 비활성")
@Composable
private fun SecondaryActionButtonDisabledPreview() {
    AppTheme { SecondaryActionButton(text = "나중에", onClick = {}, enabled = false) }
}

@Preview(name = "보조 긴 텍스트", widthDp = 200)
@Composable
private fun SecondaryActionButtonLongTextPreview() {
    AppTheme { SecondaryActionButton(text = "이미 계정이 있어서 로그인하기", onClick = {}) }
}

@Preview(name = "보조 어두운 테마")
@Composable
private fun SecondaryActionButtonDarkPreview() {
    AppTheme(darkTheme = true) { SecondaryActionButton(text = "나중에", onClick = {}) }
}
