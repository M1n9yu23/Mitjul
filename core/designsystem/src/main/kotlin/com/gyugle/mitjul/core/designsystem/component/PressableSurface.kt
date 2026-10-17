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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.gyugle.mitjul.core.designsystem.interaction.InteractionRole
import com.gyugle.mitjul.core.designsystem.interaction.pressScale
import com.gyugle.mitjul.core.designsystem.interaction.rememberHapticClick
import com.gyugle.mitjul.core.designsystem.interaction.rememberPressScale
import com.gyugle.mitjul.core.designsystem.theme.AppTheme

@Composable
fun PressableSurface(
    onClick: () -> Unit,
    clickLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource, AppTheme.motion)
    val click = rememberHapticClick(InteractionRole.PressableSurface, onClick)
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .pressScale(scale)
            .clip(AppTheme.shapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClickLabel = clickLabel,
                role = Role.Button,
                onClick = click,
            ),
    ) {
        content()
    }
}

@Preview(name = "목록 항목 기본")
@Composable
private fun PressableSurfacePreview() {
    AppTheme {
        PressableSurface(onClick = {}, clickLabel = "열기") { ListItem(headlineContent = { Text("코스모스") }) }
    }
}

@Preview(name = "목록 항목 어두운 테마")
@Composable
private fun PressableSurfaceDarkPreview() {
    AppTheme(darkTheme = true) {
        PressableSurface(onClick = {}, clickLabel = "열기") { ListItem(headlineContent = { Text("코스모스") }) }
    }
}
